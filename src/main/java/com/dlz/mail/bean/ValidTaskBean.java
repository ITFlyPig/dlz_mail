package com.dlz.mail.bean;

import java.io.Serializable;

/**
 * 描述:有效的任务
 *
 * @outhor wangyuelin
 * @create 2018-10-31 7:31 PM
 */
public class ValidTaskBean implements Serializable {
    private String uid;    //任务的唯一标识
    private MailTaskBean mailTaskBean;  //具体的任务

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public MailTaskBean getMailTaskBean() {
        return mailTaskBean;
    }

    public void setMailTaskBean(MailTaskBean mailTaskBean) {
        this.mailTaskBean = mailTaskBean;
    }
}