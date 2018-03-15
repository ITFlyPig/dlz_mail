package com.dlz.mail;

import com.dlz.mail.bean.MailConfBean;
import com.dlz.mail.bean.MailTaskBean;
import com.dlz.mail.db.DBUtil;
import com.dlz.mail.task.ExecuteSQL;
import com.dlz.mail.task.GetTasks;
import com.dlz.mail.utils.Constant;
import com.dlz.mail.utils.EmailUtil;
import com.dlz.mail.utils.Log;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.*;

public class Test {
    private  static ExecutorService executorService = Executors.newFixedThreadPool(5);

    public static void main(String[] args) {

        MailConfBean mailConfBean = getSendMail();
        if (mailConfBean == null){
            Log.d("邮件发送者为空，结束程序");
            return;
        }
        EmailUtil.mailConf = mailConfBean;


        BlockingQueue<MailTaskBean> sqlQueue = new ArrayBlockingQueue<MailTaskBean>(30);
        GetTasks getTasks = new GetTasks(sqlQueue);
        new Thread(getTasks).start();

        BlockingQueue<MailTaskBean> sendMailQueue = new ArrayBlockingQueue<MailTaskBean>(30);
        for (int i = 0; i < 1; i++){
            ExecuteSQL executeSQL = new ExecuteSQL(sendMailQueue, sqlQueue);
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
}
