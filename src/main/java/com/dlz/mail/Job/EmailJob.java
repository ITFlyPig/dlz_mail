package com.dlz.mail.Job;

import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.db.CommonUtil;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.EmailUtil;
import com.dlz.mail.utils.Log;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * 邮件定时发送的任务
 */
public class EmailJob implements Job {


    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String emailTaskId = dataMap.getString("email_task_id");
        String jobName = dataMap.getString("jobName");
        Log.d("开始执行发送邮件的定时任务：" + jobName);
        //据id查询对应的邮件发送任务
        ComboPooledDataSource dataSource = DBUtil.getDataSource();
        QueryRunner queryRunner = new QueryRunner(dataSource);
        MailTaskBean task = null;

        try {
            task = queryRunner.query(Constant.SQL.GET_TASKS, new BeanHandler<MailTaskBean>(MailTaskBean.class), emailTaskId);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("定时任务失败，任务id为----" +  emailTaskId + " \n" + e.getLocalizedMessage());
        }
        if (task == null){
            Log.d("定时任务失败，任务id为----" +  emailTaskId + " 查询数据库对应的任务为空");
            return;
        }
        sendMail(task);



    }

    /**
     * 发送邮件
     * @param task
     */
    private void sendMail(MailTaskBean task){
        Log.d("sendMail begin");
        if (task == null){
            Log.d("sendMail 返回，因为任务为空");
            return;
        }

        //判断任务是否应该丢弃
        boolean isAbandon = CommonUtil.isShouldAbandonTask(task);
        if (isAbandon){
            Log.d("邮件任务有更新，放弃当前的任务");
            return;
        }

        long curTime = System.currentTimeMillis();
        boolean isSend = false;
        Timestamp sendT = task.getSend_time();
        Timestamp endT = task.getEnd_time();
        if (endT == null && sendT != null){
            isSend = true;
        }else if (endT != null && sendT != null){
            long sendTime = task.getSend_time().getTime();
            long endTime = task.getEnd_time().getTime();
            if (curTime >= sendTime &&  curTime < endTime && task.getStatus() == Constant.EmailStatus.WAIT_SEND){
                isSend = true;
            }
        }
        if (isSend){
            boolean result = EmailUtil.sendAttachmentEmail(task.filePath,  task.getSubject(),
                    task.getMailContent(), task.parseReceptions(), task.parseCopyTos());
            //对于已发送的邮件，更新状态
            Log.d("邮件：" + task.getTask_name() + " 发送" + (result ? "成功" : "失败"));
            if (result){
                DBUtil.update(Constant.SQL.UPDATE_TASK_STATUS, Constant.EmailStatus.SEND_SUCCESS, task.getId());
            }else {
                DBUtil.update(Constant.SQL.UPDATE_TASK_STATUS, Constant.EmailStatus.SEND_FAIL, task.getId());
            }


            //将发送的结果告诉管理员
            String tip = "邮件：" + task.getTask_name() + " 发送" + ( result ? "成功 ^_^"  : "失败 ::>_<::");
            EmailUtil.sendMail(task.getManagerEmail(), "邮件发送结果", tip);
        }else {//查询到的邮件不满足发送的条件
            Log.d("查询到的邮件不满足发送的条件，邮件为：" + task.getTask_name());
        }

    }


}
