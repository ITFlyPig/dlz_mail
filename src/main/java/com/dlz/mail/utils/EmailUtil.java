package com.dlz.mail.utils;

import com.dlz.mail.bean.MailConfBean;
import org.apache.commons.mail.*;

import java.io.*;

/**
 * 邮件发送的工具类
 * @author wangyuelin
 *
 */
public class EmailUtil {

    public static MailConfBean mailConf;



    /**
     * 发送带附件的邮件
     * @param attachmentPath 附件的路径
     * @param subject
     * @param content
     * @param recipients 接收邮件的人
     * @param copyTos 邮件的抄送人
     */
    public static boolean sendAttachmentEmail(String attachmentPath,
                                              String subject, String content, String[] recipients, String[] copyTos){
        if (mailConf == null){
            Log.d("发件人邮件的配置为空");
            return false;
        }

        File file = new File(attachmentPath );
        //发送带附件的邮件
        EmailAttachment attachment = new EmailAttachment();
        //附件的路劲
        attachment.setPath(file.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setDescription("excel");
        String fileName = FileUtil.getFileNameWithType(attachmentPath);
        if (TextUtil.isEmpty(fileName)){
            fileName = "查询结果";
        }
        attachment.setName(fileName);
        // Create the email message
        MultiPartEmail email = new MultiPartEmail();
        email.setHostName(mailConf.getHost());
        email.setAuthentication(mailConf.getUser(), mailConf.getPassword());
        try {
            email.addTo(recipients);
            email.setFrom(mailConf.getUser());
            email.setSubject(subject);
            email.setMsg(content);
            email.attach(attachment);
            //添加抄送人
            if (copyTos != null && copyTos.length > 0){
                email.addCc(copyTos);
            }
            email.send();
            return true;
        } catch (EmailException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将错误发送到错误的订阅者
     * @param toAddress
     * @param subject
     * @param content
     */
    public static boolean sendMail(String toAddress, String subject, String content){
        Log.d("开始发送简单邮件");
        if (mailConf == null){
            Log.d("发件人邮件的配置为空");
            return false;
        }
        if (subject == null){
            subject = "";
        }


        Email email = new SimpleEmail();
        email.setDebug(true);
        email.setCharset("utf-8");
        email.setHostName(mailConf.getHost());
        email.setAuthenticator(new DefaultAuthenticator(mailConf.getUser(), mailConf.getPassword()));
        //email.setSSLOnConnect(true);
        //        email.setSSL(true);//commons-mail-1.1支持的方法，1.4中使用setSSLOnConnect(true)代替
        try {
            email.setFrom(mailConf.getUser());
            email.setSubject(subject);
            email.setMsg(content);
            email.addTo(toAddress);
            email.send();
            Log.d("邮件发送成功，接收人：" + toAddress);
            return true;
        } catch (EmailException e) {
            Log.d(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return false;

    }
}
