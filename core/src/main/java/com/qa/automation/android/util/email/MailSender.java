package com.qa.automation.android.util.email;


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
 * The type Mail sender.
 */
public class MailSender {

    private static final String LOG_TAG = MailSender.class.getSimpleName();
    private static final int PORT = 25;

    /**
     * Send html mail boolean.
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
    public static boolean sendHTMLMail(String sender, String decryptPassword, String smtp, String subject, String content, String file,
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
            props.put("mail.smtp.auth", "true");

            CustomizedAuthenticator authenticator = null;
            authenticator = new CustomizedAuthenticator(sender, decryptPassword);
            Session sendMailSession = Session.getInstance(props, authenticator);

            Message mailMessage = new MimeMessage(sendMailSession);
            Address from = new InternetAddress(sender);
            mailMessage.setFrom(from);
            for (String aMailList : mailList) {
                mailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(aMailList));
            }

            mailMessage.setSubject(subject);
            mailMessage.setSentDate(new Date());

            Multipart multipart = new MimeMultipart();
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(content, "text/html; charset=utf-8");
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
            mailMessage.setContent(multipart, "text/html; charset=utf-8");
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
