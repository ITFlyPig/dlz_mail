package com.dlz.mail.bean;

import com.dlz.mail.Test;
import com.dlz.mail.utils.Log;
import com.dlz.mail.utils.TextUtil;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

public class MailTaskBean {
    public int id;
    public int status;//表示邮件任务的状态 0：为执行sql 1：表示正在执行sql中 2：sql执行完成未定时   3：已定时  99：放弃这个任务
    public String sql;//sql语句
    public String cron;//cron定时表达式
    public Timestamp send_time;//邮件发送时间
    public Timestamp end_time;//邮件结束时间
    public Timestamp new_time;//邮件新建的时间
    public Timestamp excuteTime;//执行sql的时间
    public String receptions;//邮件的接受者
    public String copy_to_mails;//邮件的抄送者列表

    /**
     * 用于设置cron表达式
     */
    public String second;
    public String min;
    public String hour;
    public String day;
    public String month;
    public String week;

    public String task_name;//任务的名称,

    public String filePath;//邮件附件的保存路劲

    public String subject;//邮件的主题
    public String mailContent;//邮件的内容
    public String managerEmail;//管理员的邮件


    public String getSql() {
        return sql;
    }

    public String getCron() {
        return cron;
    }

    public Timestamp getSend_time() {
        return send_time;
    }

    public Timestamp getEnd_time() {
        return end_time;
    }

    public Timestamp getNew_time() {
        return new_time;
    }

    public String getMin() {
        return min;
    }

    public String getHour() {
        return hour;
    }

    public String getDay() {
        return day;
    }

    public String getMonth() {
        return month;
    }

    public String getWeek() {
        return week;
    }


    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public void setSend_time(Timestamp send_time) {
        this.send_time = send_time;
    }

    public void setEnd_time(Timestamp end_time) {
        this.end_time = end_time;
    }

    public void setNew_time(Timestamp new_time) {
        this.new_time = new_time;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public String getReceptions() {
        return receptions;
    }

    public void setReceptions(String receptions) {
        this.receptions = receptions;
    }

    public String getCopy_to_mails() {
        return copy_to_mails;
    }

    public void setCopy_to_mails(String copy_to_mails) {
        this.copy_to_mails = copy_to_mails;
    }

    public String getTask_name() {
        return task_name;
    }

    public void setTask_name(String task_name) {
        this.task_name = task_name;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getSubject() {
        return subject;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getMailContent() {
        return mailContent;
    }

    public void setMailContent(String mailContent) {
        this.mailContent = mailContent;
    }


    public String getManagerEmail() {
        return managerEmail;
    }

    public void setManagerEmail(String managerEmail) {
        this.managerEmail = managerEmail;
    }

    public Timestamp getExcuteTime() {
        return excuteTime;
    }

    public void setExcuteTime(Timestamp excuteTime) {
        this.excuteTime = excuteTime;
    }

    /**
     * 生成cron表达式
     * @return
     */
    public String generateCron(){
        if (TextUtil.isEmpty(second) || TextUtil.isEmpty(min) || TextUtil.isEmpty(hour) || TextUtil.isEmpty(day)
                || TextUtil.isEmpty(month) || TextUtil.isEmpty(week)){
            Log.d("生成cron表达式失败");
            return "";
        }else {
            return second + min + hour + day + month + week;
        }
    }

    /**
     * 生成执行sql的表达式
     * @return
     */
    public String generateExecutSQlCron(){
        if (getExcuteTime() == null){
            Log.d("生成cron表达式失败");
            return "";
        }else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(getExcuteTime());
            String cron = calendar.get(Calendar.SECOND)  + " "+ calendar.get(Calendar.MINUTE) + " "+ calendar.get(Calendar.HOUR_OF_DAY)+ " "+ calendar.get(Calendar.DAY_OF_MONTH)
                    +" "+ (calendar.get(Calendar.MONTH) + 1)+ " "+ "?" + " " + calendar.get(Calendar.YEAR);
            Log.d("生成的定时执行slq的cron表达式：" + cron);
            return cron;
        }
    }

    /**
     * 解析得到收件人的列表
     * @return
     */
    public String[] parseReceptions(){
        if (TextUtil.isEmpty(receptions)){
            return null;
        }
        receptions = receptions.replaceAll(" ", "");
        String[] reArray = receptions.split(";");
        return reArray;

    }

    /**
     * 邮件抄送者
     * @return
     */
    public String[] parseCopyTos(){
        if (TextUtil.isEmpty(copy_to_mails)){
            return null;
        }
        copy_to_mails = copy_to_mails.replaceAll(" ", "");
        String[] reArray = copy_to_mails.split(";");
        return reArray;
    }
}
