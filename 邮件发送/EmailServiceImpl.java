package com.ruoyi.system.service.impl;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import com.ruoyi.system.service.IEmailService;

/**
 * EmailServiceImpl 邮件发送实现类
 *
 * @author liyutao
 * @version 2023/07/06 08:46
 **/
@Service
public class EmailServiceImpl implements IEmailService {

    @Override
    public void sendEmailDemo(String to) {
        // 发件人邮箱地址  
		String from = "example@qq.com"; 
  
		// 发件人邮箱用户名  
		final String username = "example@qq.com";  
  
		// 授权码  
		final String password = "test";  
  
		// 邮件服务器主机名和端口  
		String host = "smtp.qq.com";  
		int port = 465;

        // 创建属性对象并设置邮件服务器属性
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", port);
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // 创建会话对象，用于与邮件服务器进行通信
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // 创建邮件消息对象
            Message message = new MimeMessage(session);

            // 设置发件人
            message.setFrom(new InternetAddress(from));

            // 设置收件人
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

            // 设置邮件主题
            message.setSubject("测试邮件");

            // 设置邮件正文
            message.setText("这是一封测试邮件。");

            // 发送邮件
            Transport.send(message);

            System.out.println("邮件已发送成功！");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
