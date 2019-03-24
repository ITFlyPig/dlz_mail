package com.dlz.mail.Job;

import com.dlz.mail.Test;
import com.dlz.mail.bean.ValidTaskBean;
import com.dlz.mail.task.ExecuteSQL;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.TextUtil;
import org.apache.log4j.LogManager;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

//执行sql查询的job
public class ExcuteSqlJob implements Job {
    private static final org.apache.log4j.Logger logger = LogManager.getLogger(ExecuteSQL.class);
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String emailTaskId = dataMap.getString(Constant.Key.EMAIL_TASK_ID);
        String jobName = dataMap.getString(Constant.Key.TASK_NAME);
        ValidTaskBean validTaskBean = (ValidTaskBean) dataMap.get(Constant.Key.OBJ);
        logger.debug("开始执行定时任务：" + jobName);
        if (TextUtil.isEmpty(emailTaskId)) {
            logger.debug("id为空，放弃定时任务的执行");
            return;
        }

        //开始查询对应的任务并执行
        Test.executorService.submit(new ExecuteSQL(emailTaskId, validTaskBean));

    }
}
