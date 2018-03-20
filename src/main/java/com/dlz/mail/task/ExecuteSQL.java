package com.dlz.mail.task;

import com.dlz.mail.Job.EmailJob;
import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.db.CSVResultHandler;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.queue.TaskQueue;
import com.dlz.mail.timer.CronTriggerUtil;
import com.dlz.mail.timer.QuartzManager;
import com.dlz.mail.utils.*;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;

import java.io.File;
import java.sql.SQLException;

/**
 * 从任务队列中取出sql任务来执行，然后生成csv文件，最后放入邮件发送队列
 */
public class ExecuteSQL implements Runnable {
    private TaskQueue mTaskQueue;//等待处理的任务队列
    private boolean isStop;//表示线程是否结束

    public ExecuteSQL(TaskQueue taskQueue) {
         mTaskQueue = taskQueue;
    }

    public void run() {
        while (!isStop){
            Log.d("待执行的sql任务数：" + mTaskQueue.getSqlQueue().size());
            MailTaskBean mailTaskBean = null;
            try {
                mailTaskBean = mTaskQueue.getSqlQueue().take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mailTaskBean == null){
                Log.d("取出的待执行sql任务为空");
                continue;
            }
            //检查邮件任务是否合格
            boolean isOk = checkMailTask(mailTaskBean);
            if (!isOk){
                Log.d("待处理的邮件任务不符合规范，放弃任务");
                //TODO 给管理者发送邮件通知一声
                return;
            }

            //查询-------> 生成csv文件----->发送或者定时
            ComboPooledDataSource dataSource = DBUtil.getDataSource();
            QueryRunner queryRunner = new QueryRunner(dataSource);
            try {
                String path = queryRunner.query(mailTaskBean.sql, new CSVResultHandler(mailTaskBean.task_name));
                path = handleZIP(path);//检查是否应该压缩
                Log.d("压缩处理后的文件路径：" + (path == null ? "" : path));

                if (!TextUtil.isEmpty(path)){//立即发送或者定时
                    mailTaskBean.filePath = path;
                    handleExecutedTask(mailTaskBean);
                }else {
                    Log.d("任务：" + mailTaskBean.task_name + " 创建csv失败");
                }
            } catch (SQLException e) {
                Log.d("任务：" + mailTaskBean.task_name + " 执行sql查询失败");
                e.printStackTrace();
            }
        }

    }

    private boolean checkMailTask(MailTaskBean mailTaskBean){
        Log.d("开始检查邮件任务是否合法");
        if (mailTaskBean == null){
            Log.d("待处理的邮件任务为空");
            return false;
        }
        if (mailTaskBean.send_time == null){
            Log.d("邮件的发送时间为空");
            return false;
        }
        if (TextUtil.isEmpty(mailTaskBean.receptions)){
            Log.d("邮件的接收者为空");
            return false;
        }

        if (TextUtil.isEmpty(mailTaskBean.day) || TextUtil.isEmpty(mailTaskBean.min) || TextUtil.isEmpty(mailTaskBean.month)
                || TextUtil.isEmpty(mailTaskBean.hour) ||TextUtil.isEmpty(mailTaskBean.week)){
            Log.d("cron表达式每个元素都不能为空");
            return false;
        }

        String cron = mailTaskBean.generateCron();
        boolean isOk =CronTriggerUtil.isValidExpression("0/10 * * * * ?");
        if (isOk){
            Log.d("邮件任务合法");
        }else {
            Log.d("邮件任务不合法");
        }

        return isOk;
    }

    /**
     * 处理已经生成csv文件的任务
     * @param mailTaskBean
     */
    private void handleExecutedTask(MailTaskBean mailTaskBean){
        if (mailTaskBean == null){
            return;
        }

        long curTime = System.currentTimeMillis();
        long sendTime = mailTaskBean.send_time.getTime();
        if ((mailTaskBean.end_time == null && curTime >= sendTime)//邮件没有截止日期，且已经到了发送的时间
                ||
                mailTaskBean.end_time != null && curTime >= sendTime && curTime < mailTaskBean.end_time.getTime()){//到了发送时间，但是还没到过期时间
            //直接发送
            Log.d("立即发送邮件：" + mailTaskBean.getTask_name() );
            boolean result = sendEmail(mailTaskBean);
            Log.d("邮件：" + mailTaskBean.getTask_name() + " 发送" + (result ? "成功" : "失败"));

            int status = result ? Constant.EmailStatus.SEND_SUCCESS : Constant.EmailStatus.SEND_FAIL;
            DBUtil.update(Constant.SQL.UPDATE_TASK_STATUS, status, mailTaskBean.getId());
            return;

        }

        if (sendTime > curTime){//对发送任务定时并且写入到数据库
            Log.d("定时发送邮件：" + mailTaskBean.getTask_name() );
            QuartzManager.addJob(mailTaskBean.getTask_name() + "邮件发送", String.valueOf(mailTaskBean.getId()), "send_email",EmailJob.class, mailTaskBean.generateCron(), mTaskQueue);
            DBUtil.update("update mail set filePath = ?, status = ? where id = ?", mailTaskBean.getFilePath(), Constant.EmailStatus.WAIT_SEND, mailTaskBean.getId());
            return;
        }
        //丢弃这个任务
        //TODO 对应丢弃的任务还得发送邮件通知管理员
        EmailUtil.sendMail(mailTaskBean.getManagerEmail(), "邮件系统报警", "邮件任务被丢弃：" + mailTaskBean.getTask_name());
        Log.d("丢弃邮件任务：" + mailTaskBean.getTask_name() );

    }

    private boolean sendEmail(MailTaskBean mailTaskBean){
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
