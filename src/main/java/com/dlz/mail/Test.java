package com.dlz.mail;

import com.dlz.mail.bean.MailConfBean;
import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.queue.TaskQueue;
import com.dlz.mail.task.ExecuteSQL;
import com.dlz.mail.task.GetTasks;
import com.dlz.mail.task.MonitorTask;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.EmailUtil;
import com.dlz.mail.utils.Log;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;

public class Test {
    private static final Logger logger;

    static {
        PropertyConfigurator.configure( System.getProperty("user.dir") + Constant.FileConfig.CONF_DIR +"/log4j.properties");
        logger =  LoggerFactory.getLogger(Test.class);;
    }


    private  static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static void main(String[] args) {

        MailConfBean mailConfBean = getSendMail();
        if (mailConfBean == null){
            Log.d("邮件发送者为空，结束程序");
            return;
        }
        EmailUtil.mailConf = mailConfBean;


        TaskQueue taskQueue = new TaskQueue();
        startGetTasks(taskQueue);//开始sql任务的查询

        startMonitorFile(taskQueue);//开始文件的检测

        for (int i = 0; i < 1; i++){
            ExecuteSQL executeSQL = new ExecuteSQL(taskQueue);
            executorService.submit(executeSQL);
        }

    }


    /**
     * 查询获得发件这的邮件配置
     *
     * @return
     */
    private static MailConfBean getSendMail()  {
        Log.d("开始从数据库查询邮件发送者的邮件配置");
        ComboPooledDataSource dataSource = DBUtil.getDataSource();
        QueryRunner queryRunner = new QueryRunner(dataSource);
        List<MailConfBean> mailConfBeans = null;
        try {
            mailConfBeans = queryRunner.query(Constant.SQL.GET_SEND_MAIL, new BeanListHandler<MailConfBean>(MailConfBean.class));
        } catch (SQLException e) {
            e.printStackTrace();
            Log.d("查询邮件发送者的邮件配置出现异常");
        }
        if (mailConfBeans != null && mailConfBeans.size() > 0) {
            Log.d("查询邮件发送者的邮件配置成功");
            return mailConfBeans.get(0);
        }
        Log.d("查询邮件发送者的邮件配置失败");
        return null;
    }

    /**
     * 测试代码
     */
    private static void addEmailUser(){
        String sql = "insert into send_mail(auth, protocol, host, port, user, password) values(?, ?, ?, ?, ?, ?) ";
        ComboPooledDataSource dataSource = DBUtil.getDataSource();
        QueryRunner queryRunner = new QueryRunner(dataSource);
        try {
            queryRunner.update(sql, "1", "test", "test", "test", "测试是否插入中文乱码", "test");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 开始监听文件的变化
     *
     */
    private static void startMonitorFile(final TaskQueue taskQueue){
        String sqlMonitorPath = System.getProperty("user.dir") + Constant.FileConfig.CONF_DIR ;
        MonitorTask monitorTask = new MonitorTask(sqlMonitorPath, new MonitorTask.FileChangeListener() {
            @Override
            public void onCreated(String path) {

            }

            @Override
            public void onDelete(String path) {

            }

            @Override
            public void onModify(String path) {
                Log.d("唤醒查询sql任务的线程");
                taskQueue.startGetSQlTasks();//唤醒查询sql任务的线程

            }
        });
        executorService.submit(monitorTask);

    }


    /**
     * 开始查询sql的
     * @param taskQueue
     */
    private static void startGetTasks(TaskQueue taskQueue){
        GetTasks getTasks = new GetTasks(taskQueue);
        new Thread(getTasks).start();
    }


}
