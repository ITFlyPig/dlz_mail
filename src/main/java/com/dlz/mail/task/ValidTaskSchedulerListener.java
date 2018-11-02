package com.dlz.mail.task;

import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.bean.ValidTaskBean;
import com.dlz.mail.queue.ValidTaskContainer;
import com.dlz.mail.utils.Constant;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.*;

import java.util.LinkedHashMap;

/**
 * 描述:
 *
 * @outhor wangyuelin
 * @create 2018-11-02 2:24 PM
 */
public class ValidTaskSchedulerListener implements SchedulerListener {
    private static final Logger logger = LogManager.getLogger(ValidTaskSchedulerListener.class);
    private LinkedHashMap<TriggerKey, ValidTaskBean> triggerValidTaskMap = new LinkedHashMap<>();


    @Override
    public void jobScheduled(Trigger trigger) {//部署JobDetail时调用
        //添加到有效的任务队列
        ValidTaskBean validTaskBean = (ValidTaskBean) trigger.getJobDataMap().get(Constant.Key.OBJ);

        if (validTaskBean == null) {
            return;
        }
        triggerValidTaskMap.put(trigger.getKey(), validTaskBean);
        ValidTaskContainer.addTask(validTaskBean);
        MailTaskBean mailTaskBean = validTaskBean.getMailTaskBean();
        if (mailTaskBean != null) {
            logger.debug("jobScheduled 添加到有效队列：" + (mailTaskBean.getTask_name() == null ? "" : mailTaskBean.getTask_name()) + "----------- UUID:  " + validTaskBean.getUid());
        }


    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {//卸载JobDetail时调用
        //从有效队列中删除
        ValidTaskBean validTaskBean;
        validTaskBean = triggerValidTaskMap.get(triggerKey);
        triggerValidTaskMap.remove(triggerKey);
        if (null == validTaskBean) {
            return;
        }
        ValidTaskContainer.removeByUUID(validTaskBean.getUid());

        MailTaskBean mailTaskBean = validTaskBean.getMailTaskBean();
        if (mailTaskBean != null) {
            logger.debug("jobUnscheduled 从有效队列中删除：" + (mailTaskBean.getTask_name() == null ? "" : mailTaskBean.getTask_name()) + "----------- UUID:  " + validTaskBean.getUid());
        }

    }

    @Override
    public void triggerFinalized(Trigger trigger) {

    }

    @Override
    public void triggerPaused(TriggerKey triggerKey) {

    }

    @Override
    public void triggersPaused(String s) {

    }

    @Override
    public void triggerResumed(TriggerKey triggerKey) {

    }

    @Override
    public void triggersResumed(String s) {

    }

    @Override
    public void jobAdded(JobDetail jobDetail) {

    }

    @Override
    public void jobDeleted(JobKey jobKey) {

    }

    @Override
    public void jobPaused(JobKey jobKey) {

    }

    @Override
    public void jobsPaused(String s) {

    }

    @Override
    public void jobResumed(JobKey jobKey) {

    }

    @Override
    public void jobsResumed(String s) {

    }

    @Override
    public void schedulerError(String s, SchedulerException e) {

    }

    @Override
    public void schedulerInStandbyMode() {

    }

    @Override
    public void schedulerStarted() {

    }

    @Override
    public void schedulerStarting() {

    }

    @Override
    public void schedulerShutdown() {

    }

    @Override
    public void schedulerShuttingdown() {

    }

    @Override
    public void schedulingDataCleared() {

    }
}