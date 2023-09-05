	在实际开发中，我们可能需要使用邮件发送来实现一些二步验证的功能
	这里，以QQ邮箱为例，实现了一个简单的邮件发送Demo

# 相关配置

## SMTP功能开启

首先，我们需要开启QQ邮箱的**SMTP**功能：
![](https://raw.githubusercontent.com/NoFatter/NoFat-Pic/img/img/202309051632585.png)
![](https://raw.githubusercontent.com/NoFatter/NoFat-Pic/img/img/202309051632227.png)

>SMTP协议
>>是SMTP是一组用于从源地址到目的地址传送邮件的规则，并且控制信件的中转方式。SMTP协议属于TCP/IP协议族，它帮助每台计算机在发送或中转信件时找到下一个目的地。通过SMTP协议所指定的服务器，我们就可以把E—mail寄到收信人的服务器上了，整个过程只需要几分钟。SMTP服务器是遵循SMTP协议的发送邮件服务器，用来发送或中转用户发出的电子邮件。

## 邮箱授权码的配置和生成

接下来，我们需要在*管理服务*-*生成授权码*中生成授权码，并在Demo中完成对应的配置：
![](https://raw.githubusercontent.com/NoFatter/NoFat-Pic/img/img/202309051641562.png)
![](https://raw.githubusercontent.com/NoFatter/NoFat-Pic/img/img/202309051641598.png)

```java
// 代码配置
// 发件人邮箱地址  
String from = "example@qq.com"; 
  
// 发件人邮箱用户名  
final String username = "example@qq.com";  
  
// 授权码  
final String password = "test";  
  
// 邮件服务器主机名和端口  
String host = "smtp.qq.com";  
int port = 465;
```

# 最终Demo代码实现

## Maven引入

```xml
<dependency>  
    <groupId>com.sun.mail</groupId>  
    <artifactId>jakarta.mail</artifactId>  
</dependency>
```

## Demo实现

```java 
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
```

