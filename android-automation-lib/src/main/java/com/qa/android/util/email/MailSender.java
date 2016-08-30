package com.qa.android.util.email;


import java.io.File;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * 发送邮件给多个接收者、抄送邮件
 *
 * @author andrewleo
 */
public class MailSender {

    private static final String LOG_TAG = MailSender.class.getSimpleName();
    private static final int PORT = 25;

    /**
     * 以文本格式发送邮件
     * <p/>
     * 待发送的邮件的信息
     *
     * @param sender          the sender
     * @param decryptPassword the decrypt password
     * @param smtp            the smtp
     * @param subject         the subject
     * @param content         the content
     * @param file            the file
     * @param mailList        the mailList
     * @return the boolean
     * @throws MessagingException the messaging exception
     */
    public static boolean sendTextMail(String sender, String decryptPassword, String smtp, String subject, String content, String file,
                                       String[] mailList) throws MessagingException {
        if (mailList == null || mailList.length == 0 || ("".equals(mailList[0].trim()))) {
            return false;
        } else {
            // Get system properties
            Properties props = new Properties();

            // Setup mail server
            props.put("mail.smtp.host", smtp);
            props.put("mail.smtp.port", PORT);
            // Get session
            props.put("mail.smtp.auth", "true"); // 如果需要密码验证，把这里的false改成true

            // 判断是否需要身份认证
            CustomizedAuthenticator authenticator = null;
            // 如果需要身份认证，则创建一个密码验证器
            authenticator = new CustomizedAuthenticator(sender, decryptPassword);
            // 根据邮件会话属性和密码验证器构造一个发送邮件的session
            Session sendMailSession = Session.getInstance(props, authenticator);

            // 根据session创建一个邮件消息
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(sender);
            // 设置邮件消息的发送者
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址，并设置到邮件消息中
            for (String aMailList : mailList) {
                // Message.RecipientType.TO属性表示接收者的类型为TO
                mailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(aMailList));
            }

            // 设置邮件消息的主题
            mailMessage.setSubject(subject);
            // 设置邮件消息发送的时间
            mailMessage.setSentDate(new Date());

            Multipart multipart = new MimeMultipart();
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(content);
            multipart.addBodyPart(bodyPart);

            if (file != null) {
                File attach = new File(file);
                if (attach.exists()) {
                    MimeBodyPart attachPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(attach);
                    attachPart.setDataHandler(new DataHandler(source));
                    attachPart.setFileName(attach.getName());
                    multipart.addBodyPart(attachPart);
                }
            }
            mailMessage.setContent(multipart);
            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
            mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
            mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
            mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
            CommandMap.setDefaultCommandMap(mc);
            Transport.send(mailMessage);
            return true;
        }
    }
}
