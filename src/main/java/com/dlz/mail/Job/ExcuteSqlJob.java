package com.dlz.mail.Job;

import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.queue.TaskQueue;
import com.dlz.mail.task.GetTasks;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.Log;
import com.dlz.mail.utils.TextUtil;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLException;
import java.util.List;

//执行sql查询的job
public class ExcuteSqlJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String emailTaskId = dataMap.getString(Constant.Key.EMAIL_TASK_ID);
        String jobName = dataMap.getString(Constant.Key.TASK_NAME);
        TaskQueue taskQueue = (TaskQueue) dataMap.get("obj");
        if (TextUtil.isEmpty(emailTaskId)) {
            Log.d("id为空，放弃定时任务的执行");
            return;
        }

        Log.d("开始定时任务的执行：" + emailTaskId + (jobName == null ? "" : jobName));
        //据id查询对应的邮件发送任务
        ComboPooledDataSource dataSource = DBUtil.getDataSource();
        QueryRunner queryRunner = new QueryRunner(dataSource);
        List<MailTaskBean> tasks = null;
        try {
            tasks = queryRunner.query(Constant.SQL.GET_TASKS, new BeanListHandler<MailTaskBean>(MailTaskBean.class), emailTaskId);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("定时任务失败，任务id为----" + emailTaskId + " \n" + e.getLocalizedMessage());
        }
        if (tasks == null || tasks.size() == 0) {
            Log.d("定时任务失败，任务id为----" + emailTaskId + " 查询数据库对应的任务为空");
            return;
        }

        //将任务执行或者再次定时
        if (taskQueue != null) {
            taskQueue.addSqlTask(tasks);

        }


    }
}
