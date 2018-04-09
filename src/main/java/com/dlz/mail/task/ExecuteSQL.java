package com.dlz.mail.task;

import com.dlz.mail.Job.EmailJob;
import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.db.CSVResultHandler;
import com.dlz.mail.db.CommonUtil;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.queue.TaskQueue;
import com.dlz.mail.timer.QuartzManager;
import com.dlz.mail.utils.*;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;

/**
 * 从任务队列中取出sql任务来执行，然后生成csv文件，最后放入邮件发送队列
 */
public class ExecuteSQL implements Runnable {
    private static final Logger logger = LogManager.getLogger(ExecuteSQL.class);

    private TaskQueue mTaskQueue;//等待处理的任务队列
    private boolean isStop;//表示线程是否结束

    public ExecuteSQL(TaskQueue taskQueue) {
         mTaskQueue = taskQueue;
    }

    public void run() {
        while (!isStop){
            logger.debug("待执行的sql任务数：" + mTaskQueue.getSqlQueue().size());
            MailTaskBean mailTaskBean = null;
            try {
                mailTaskBean = mTaskQueue.getSqlQueue().take();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //判断任务是否应该丢弃
            boolean isAbandon = CommonUtil.isShouldAbandonTask(mailTaskBean);
            if (isAbandon){
                logger.debug("放弃任务：" + mailTaskBean.getId());
                continue;
            }

            if (mailTaskBean == null){
                logger.debug("取出的待执行sql任务为空");
                continue;
            }
            //检查邮件任务是否合格
            boolean isOk = checkMailTask(mailTaskBean);
            if (!isOk){
                logger.debug("待处理的邮件任务不符合规范，放弃任务");
                //TODO 给管理者发送邮件通知一声
                return;
            }

            //查询-------> 生成csv文件----->发送或者定时
            ComboPooledDataSource dataSource = DBUtil.getDataSource();
            QueryRunner queryRunner = new QueryRunner(dataSource);
            try {
                String path = queryRunner.query(mailTaskBean.sql, new CSVResultHandler(mailTaskBean.task_name));
                path = handleZIP(path);//检查是否应该压缩
                logger.debug("压缩处理后的文件路径：" + (path == null ? "" : path));

                if (!TextUtil.isEmpty(path)){//立即发送或者定时
                    mailTaskBean.filePath = path;
                    handleExecutedTask(mailTaskBean);
                }else {
                    logger.debug("任务：" + mailTaskBean.task_name + " 创建csv失败");
                }
            } catch (SQLException e) {
                logger.debug("任务：" + mailTaskBean.task_name + " 执行sql查询失败");
                e.printStackTrace();
            }
        }

    }

    private boolean checkMailTask(MailTaskBean mailTaskBean){
        logger.debug("开始检查邮件任务是否合法");
        if (mailTaskBean == null){
            logger.debug("待处理的邮件任务为空");
            return false;
        }
        if (mailTaskBean.send_time == null){
            logger.debug("邮件的发送时间为空");
            return false;
        }
        if (TextUtil.isEmpty(mailTaskBean.receptions)){
            logger.debug("邮件的接收者为空");
            return false;
        }


        return true;
    }

    /**
     * 处理已经生成csv文件的任务
     * @param mailTaskBean
     */
    private void handleExecutedTask(MailTaskBean mailTaskBean){
        if (mailTaskBean == null){
            return;
        }

        //判断任务是否应该丢弃
        boolean isAbandon = CommonUtil.isShouldAbandonTask(mailTaskBean);
        if (isAbandon){
            logger.debug("放弃任务：ID:" + mailTaskBean.getId());
            return;
        }

        long curTime = System.currentTimeMillis();
        long sendTime = mailTaskBean.send_time.getTime();
        if ((mailTaskBean.end_time == null && curTime >= sendTime)//邮件没有截止日期，且已经到了发送的时间
                ||
                mailTaskBean.end_time != null && curTime >= sendTime && curTime < mailTaskBean.end_time.getTime()){//到了发送时间，但是还没到过期时间
            //直接发送
            logger.debug("立即发送邮件：" + mailTaskBean.getTask_name() );
            boolean result = sendEmail(mailTaskBean);
            logger.debug("邮件：" + mailTaskBean.getTask_name() + " 发送" + (result ? "成功" : "失败"));

            int status = result ? Constant.EmailStatus.SEND_SUCCESS : Constant.EmailStatus.SEND_FAIL;
            DBUtil.update(Constant.SQL.UPDATE_TASK_STATUS, status, mailTaskBean.getId());
            return;

        }

        if (sendTime > curTime){//对发送任务定时并且写入到数据库
            logger.debug("定时发送邮件：" + mailTaskBean.getTask_name() );
            QuartzManager.addJob(mailTaskBean.getTask_name() + "邮件发送", String.valueOf(mailTaskBean.getId()), "send_email" + String.valueOf(mailTaskBean.getId()),EmailJob.class, mailTaskBean.generateSendEmailCron(), mTaskQueue);
            DBUtil.update("update mail set filePath = ?, status = ? where id = ?", mailTaskBean.getFilePath(), Constant.EmailStatus.WAIT_SEND, mailTaskBean.getId());
            return;
        }
        //丢弃这个任务
        //TODO 对应丢弃的任务还得发送邮件通知管理员
        EmailUtil.sendMail(mailTaskBean.getManagerEmail(), "邮件系统报警", "邮件任务被丢弃：" + mailTaskBean.getTask_name());
        logger.debug("丢弃邮件任务：" + mailTaskBean.getTask_name() );

    }

    private boolean sendEmail(MailTaskBean mailTaskBean){

        //判断任务是否应该丢弃
        boolean isAbandon = CommonUtil.isShouldAbandonTask(mailTaskBean);
        if (isAbandon){
            logger.debug("因为数据库有更新，所以放弃邮件任务");
            return false;
        }
       return EmailUtil.sendAttachmentEmail(mailTaskBean.filePath,
               mailTaskBean.getSubject(), mailTaskBean.getMailContent(), mailTaskBean.parseReceptions(), mailTaskBean.parseCopyTos());

    }

    /**
     * 压缩文件
     * @param filePath
     * @return
     */
    private String handleZIP(String filePath){
        if( FileUtil.shouldZIP(filePath)){
            File srcFile = new File(filePath);
            File[] files = new File[]{srcFile};

            String zipName = FileUtil.getFileName(filePath) + ".zip";
            String zipPath = srcFile.getParent() + File.separator + zipName;
            ZipFileUtil.compressFiles2Zip(files, zipPath);

            return zipPath;
        }
        return filePath;

    }


}
