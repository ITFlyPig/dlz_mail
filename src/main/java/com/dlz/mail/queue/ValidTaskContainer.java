package com.dlz.mail.queue;

import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.bean.ValidTaskBean;
import com.dlz.mail.utils.TextUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * 描述:全局的容器类
 *
 * @outhor wangyuelin
 * @create 2018-10-31 7:28 PM
 */
public class ValidTaskContainer {
    private static final Logger logger = LogManager.getLogger(ValidTaskContainer.class);
    /**
     * 保存全局的有效的任务------任务无效：那些已经删除的定时任务，但是由于新开线程还在执行，但是属于无效的
     * 定时任务的时候将这个任务添加到有效队列中，
     * 当任务删除的或者执行完成的时候从有效队列中删除
     * 当同一个任务，有两个线程执行的时候（比如线程1是定时后，执行SQL正在创建文件，线程2是线程1的任务更新后也在执行创建文件，这是就可以查询有效队列，将无效的文件删除，使用有效的文件）
     */
    public static LinkedList<ValidTaskBean> validTasks = new LinkedList<ValidTaskBean>();

    /**
     * 据id删除所有的任务
     * @param taskId
     */
    public static synchronized void removeInvalidTask(int taskId) {
        logger.debug("removeInvalidTask 开始删除有效队列中对应的任务");
        if (taskId < 0) {
            return;
        }

        Iterator<ValidTaskBean> it = validTasks.iterator();
        while (it.hasNext()) {
            ValidTaskBean item = it.next();
            MailTaskBean mailTaskBean = item.getMailTaskBean();
            if (mailTaskBean.getId() == taskId) {
                it.remove();
                logger.debug("removeInvalidTask 删除任务ID为：" + taskId);
            }
        }


    }

    /**
     * 据UUID删除对应的有效任务
     * @param uuid
     */
    public static synchronized void removeByUUID( String uuid) {
        if (TextUtil.isEmpty(uuid)) {
            return;
        }

        Iterator<ValidTaskBean> it = validTasks.iterator();
        while (it.hasNext()) {
            ValidTaskBean item = it.next();
            String itemUUID = item.getUid();
            if (TextUtil.isEmpty(itemUUID)) {
                continue;
            }
            if (itemUUID.equals(uuid)) {
                it.remove();
                MailTaskBean mailTaskBean =item.getMailTaskBean();
                logger.debug("removeByUUID 删除任务为：" + (mailTaskBean == null ? "" : mailTaskBean.getTask_name()) + " UUID:" + uuid);
            }

        }


    }

    /**
     * 记录有效的任务
     * @param validTaskBean
     */
    public static synchronized void addTask(ValidTaskBean validTaskBean) {
        if (validTaskBean == null) {
            return;
        }
        validTasks.add(validTaskBean);

    }

    /**
     * 据实际的邮件任务构造这里使用的有效任务
     * @param mailTaskBean
     * @return
     */
    public static synchronized ValidTaskBean getValidTaskBean(MailTaskBean mailTaskBean) {
        if (mailTaskBean == null) {
            return null;
        }
        ValidTaskBean validTaskBean = new ValidTaskBean();
        validTaskBean.setUid(UUID.randomUUID().toString());
        validTaskBean.setMailTaskBean(mailTaskBean);
        return validTaskBean;

    }

    /**
     * 判断一个任务是否有效
     * @param validTaskBean
     * @return
     */
    public static boolean isValid(ValidTaskBean validTaskBean) {
        if (validTaskBean == null) {
            return false;
        }

        //测试
        printValidTaskInfo(validTaskBean);

        for (ValidTaskBean validTask : validTasks) {
            if (validTask.getUid().equals(validTaskBean.getUid())) {
                return true;
            }
        }
        return false;

    }


    /**
     * 打印出有效队列中的信息
     * @param validTaskBean
     */
    public static void printValidTaskInfo(ValidTaskBean validTaskBean) {
        if (validTaskBean != null) {
            logger.debug("printValidTaskInfo 要判断的任务：" + validTaskBean.getUid() + "--------" + validTaskBean.getMailTaskBean().getTask_name());
        }


        logger.debug("printValidTaskInfo 队列中的有效任务");
        for (ValidTaskBean validTask : validTasks) {
            logger.debug(validTask.getUid() + "--------" + validTask.getMailTaskBean().getTask_name());

        }

    }



}