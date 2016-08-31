package com.qa.test;

import com.qa.automation.android.util.email.MailSender;

import org.junit.Test;

import javax.mail.MessagingException;

/**
 * The type Email test.
 */
public class EmailTest {
    /**
     * Mail test.
     */
    @Test
    public void mailTest() {
        try {
            MailSender.sendHTMLMail("android_automation@126.com", "Automation123", "smtp.126.com",
                    "mail from android_automation@126.com", "<font color=\"red\">mail</font> to lwfwind@126.com",
                    null, (new String[]{"lwfwind@126.com"}));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
