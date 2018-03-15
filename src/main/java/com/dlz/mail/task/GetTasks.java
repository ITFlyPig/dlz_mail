package com.dlz.mail.task;

import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.Log;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * 连接数据库，获得要执行的sql，并转换为相应的任务放入队列中
 * 需要考虑的情况：
 * 1.正常的情况，也就是距离发送邮件时间未到，提前执行sql
 * 2.紧急情况，在发送时间到了和应该提前执行的这段时间里，怎么快速的检测有新的sql任务
 * 如何监听sql任务的添加和删除
 */
public class GetTasks implements Runnable {
    private BlockingQueue<MailTaskBean> sqlQueue;//待执行的sql任务队列
    private boolean isStop;

    public GetTasks(BlockingQueue<MailTaskBean> queue) {
        this.sqlQueue = queue;
    }

    public void run() {
        while (!isStop) {
            ComboPooledDataSource dataSource = DBUtil.getDataSource();
            QueryRunner queryRunner = new QueryRunner(dataSource);
            List<MailTaskBean> tasks = null;

            try {
                tasks = queryRunner.query(Constant.SQL.GET_TASKS, new BeanListHandler<MailTaskBean>(MailTaskBean.class), Constant.EmailStatus.NEW);
                DBUtil.update("update mail set status = ? where status = ?", Constant.EmailStatus.EXECUTE_ING ,Constant.EmailStatus.NEW);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            addTask(tasks);

            if (tasks == null || tasks.size() == 0){
                try {
                    Thread.sleep(  1000);//休眠一段时间再查询（1分钟）
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }

    }

    /**
     * 将数据库查出的sql任务添加到队列中
     *
     * @param tasks
     */
    private void addTask(List<MailTaskBean> tasks) {
        if (tasks == null) {
            Log.d("要添加的任务列表为空");
            return;
        }
        Log.d("查询到的任务数：" + tasks.size());

        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            MailTaskBean bean = tasks.get(i);
            try {
                sqlQueue.put(bean);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
