package com.dlz.mail.task;

import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.bean.ValidTaskBean;
import com.dlz.mail.db.CSVResultHandler;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.queue.ValidTaskContainer;
import com.dlz.mail.timer.QuartzManager;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.EmailUtil;
import com.dlz.mail.utils.FileUtil;
import com.dlz.mail.utils.TextUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 从任务队列中取出sql任务来执行，然后生成csv文件，最后放入邮件发送队列
 */
public class ExecuteSQL implements Runnable {
    private static final Logger logger = LogManager.getLogger(ExecuteSQL.class);

    private String taskId;
    private ValidTaskBean validTaskBean;

    public ExecuteSQL(String taskId, ValidTaskBean validTaskBean) {
        this.taskId = taskId;
        this.validTaskBean = validTaskBean;
    }

    public void run() {
        //数据库查询任务
        logger.debug("开始查询要执行的task");

            ComboPooledDataSource dataSource = DBUtil.getDataSource();
            QueryRunner queryRunner = new QueryRunner(dataSource);
            List<MailTaskBean> tasks = null;
            try {
                tasks = queryRunner.query(Constant.SQL.GET_TASKS, new BeanListHandler<MailTaskBean>(MailTaskBean.class), taskId);
            } catch (SQLException e) {
                logger.debug("查询要执行的task异常");
                e.printStackTrace();
            }
            if (tasks == null || tasks.size() == 0) {
                logger.debug("查询到的任务为空");
                return;
            }

            logger.debug("开始循环执行sql查询的任务");
            for (MailTaskBean task : tasks) {
                QueryRunner sqlRunner = new QueryRunner(dataSource);
                try {
                    logger.debug("开始---" + task.getTask_name() + "----任务的执行");
                    String result = sqlRunner.query(task.sql, new CSVResultHandler(task.getTask_name(), task.getSql_result_store()));

                    if (task.getSql_result_store() == Constant.SQL_RESULT_TYPE.CONTENT) {
//                    logger.debug("生成的html文件：" + result);
                        logger.debug("html文件生成成功");
                        task.setMailContent(result);
                        handleExecutedTask(task);
                    } else {

                        logger.debug("ExecuteSQL 开始修改加了时间戳的文件");

                        synchronized (ExecuteSQL.class) {//这里的同步意思：之前的查询可以多线程同时进行，但是到了最后的压缩发送邮件阶段得同步，避免两个线程同时写一个文件的问题
                            String newPath = FileUtil.renameFile(result);//真实的文件名

                            logger.debug("ExecuteSQL 删除时间戳后的文件：" + (newPath == null ? "" : newPath));
                            if (TextUtil.isEmpty(newPath)) {
                                continue;
                            }

                            String path = FileUtil.handleZIP(newPath);//检查是否应该压缩
                            logger.debug("压缩处理后的文件路径：" + (path == null ? "" : path));
                            if (!TextUtil.isEmpty(path)) {//立即发送或者定时
                                task.filePath = path;
                                handleExecutedTask(task);
                            } else {
                                logger.debug("任务：" + task.getTask_name() + " 创建csv失败");
                            }
                        }
                    }


                } catch (SQLException e) {
                    logger.debug("任务：" + task.getTask_name() + " 执行sql查询失败");
                    e.printStackTrace();
                }


        }


    }


    /**
     * 处理已经生成csv文件的任务
     *
     * @param mailTaskBean
     */
    private void handleExecutedTask(MailTaskBean mailTaskBean) {
        if (mailTaskBean == null) {
            return;
        }

        boolean result = false;
        if (mailTaskBean.getEnd_time() == null) {
            result = sendEmail(mailTaskBean);
        } else if (mailTaskBean.getEnd_time().getTime() > System.currentTimeMillis()) {
            result = sendEmail(mailTaskBean);

        } else {
            DBUtil.update(Constant.SQL.UPDATE_TASK_STATUS, Constant.EmailStatus.ABANDON, mailTaskBean.getId());
            logger.debug("发送邮件的任务被丢弃：" + mailTaskBean.getTask_name());

            //删除已经被丢弃的任务的定时器
            logger.debug("删除已放弃的任务的定时器：" + mailTaskBean.getTask_name());
            QuartzManager.removeJob(mailTaskBean.getTask_name() + "sql执行", "excute_sql" + String.valueOf(mailTaskBean.getId()));
            return;
        }
        logger.debug("邮件发送结果：" + (result ? "成功" : "失败"));
//        int status = result ? Constant.EmailStatus.SEND_SUCCESS : Constant.EmailStatus.SEND_FAIL;
//        DBUtil.update(Constant.SQL.UPDATE_TASK_STATUS, status, mailTaskBean.getId());

    }

    private boolean sendEmail(MailTaskBean mailTaskBean) {

        if (!ValidTaskContainer.isValid(validTaskBean)) {
            logger.debug("因为此任务是无效的，所以不发送邮件");
            return false;
        }

        logger.debug("开始发送邮件，任务为：" + mailTaskBean.getTask_name());
        List<String> paths = new ArrayList<>();
        if (!TextUtil.isEmpty(mailTaskBean.filePath)) {
            paths.add(mailTaskBean.filePath);
        }

        return EmailUtil.sendSQLEmail(paths,
                mailTaskBean.getSubject(), mailTaskBean.getMailContent(), mailTaskBean.parseReceptions(), mailTaskBean.parseCopyTos(), mailTaskBean.getSql_result_store());

    }




}
