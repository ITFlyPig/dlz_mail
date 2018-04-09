package com.dlz.mail.task;

import com.dlz.mail.Job.EmailJob;
import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.queue.TaskQueue;
import com.dlz.mail.timer.QuartzManager;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.Log;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 连接数据库，获得要执行的sql，并转换为相应的任务放入队列中
 * 需要考虑的情况：
 * 1.正常的情况，也就是距离发送邮件时间未到，提前执行sql
 * 2.紧急情况，在发送时间到了和应该提前执行的这段时间里，怎么快速的检测有新的sql任务,使用检测文件的方式来间接检测
 * 如何监听sql任务的添加和删除
 * <p>
 * 查询任务的时机：
 * 1.检测文件被修改
 * 2.到了定时执行的时间（定时执行的时间由用户设置）
 */
public class GetTasks implements Runnable {
    private static TaskQueue mTaskQueue ;//待执行的sql任务队列
    private boolean isStop;

    public GetTasks(TaskQueue taskQueue) {
        mTaskQueue = taskQueue;
    }

    public void run() {
        synchronized(this) {
            while (!isStop) {
                try {
                    ComboPooledDataSource dataSource = DBUtil.getDataSource();
                    QueryRunner queryRunner = new QueryRunner(dataSource);

                    List<MailTaskBean> tasks = queryRunner.query("select * from mail where status = ? or status = ?", new BeanListHandler<MailTaskBean>(MailTaskBean.class), Constant.EmailStatus.NEW, Constant.EmailStatus.UPDATED);
//                    List<MailTaskBean> tasks = queryRunner.query("select * from mail", new BeanListHandler<MailTaskBean>(MailTaskBean.class));

                    mTaskQueue.removeOldTimers(tasks);
                    mTaskQueue.addSqlTask(tasks);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
