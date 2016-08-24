package test;

import com.qa.automation.android.util.email.MailSender;

import javax.mail.MessagingException;

/**
 * Created by kcgw001 on 2016/8/19.
 */
public class EmailTest {
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        try {
            MailSender.sendTextMail("android_automation@126.com", "Automation123", "smtp.126.com",
                    "mail from android_automation@126.com", "mail to lwfwind@126.com",
                    null, (new String[]{"lwfwind@126.com"}));
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
