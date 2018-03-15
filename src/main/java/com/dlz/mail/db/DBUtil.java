package com.dlz.mail.db;

import com.dlz.mail.utils.Log;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mysql.cj.jdbc.PreparedStatement;
import org.apache.commons.dbutils.QueryRunner;

import java.beans.PropertyVetoException;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 获得操作数据的dataSource
 */
public class DBUtil {
    private static ComboPooledDataSource dataSource;

    static {
        dataSource = new ComboPooledDataSource();
        Properties props = new Properties();
        String root = System.getProperty("user.dir");
        Log.d("获取到的root:" + root);

        try {
            InputStream in = new BufferedInputStream(new FileInputStream(root + "/conf/db.properties"));
            props.load(in);
            in.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String driver = props.getProperty("db.driver");
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String passsord = props.getProperty("db.pwd");

        Log.d("driver:" + driver + " url:" + url + " user:" + user + " passsord:" + passsord);

        try {
            dataSource.setDriverClass(driver);
            dataSource.setJdbcUrl(url);
            dataSource.setUser(user);
            dataSource.setPassword(passsord);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
        }

    }

    public static ComboPooledDataSource getDataSource(){
        return dataSource;

    }


    //取得链接
    public static Connection getConn() {
        try {
            return dataSource.getConnection();
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // 关闭链接
    public static void close(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    public static void close(PreparedStatement pstate) throws SQLException {
        if(pstate!=null){
            pstate.close();
        }
    }
    public static void close(ResultSet rs) throws SQLException {
        if(rs!=null){
            rs.close();
        }
    }

    /**
     * 更新
     * @param sql
     * @param params
     */
    public static boolean update(String sql, Object... params){
        ComboPooledDataSource dataSource = DBUtil.getDataSource();
        QueryRunner queryRunner = new QueryRunner(dataSource);
        try {
            queryRunner.update(sql, params);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

