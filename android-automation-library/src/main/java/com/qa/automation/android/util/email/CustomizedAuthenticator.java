package com.qa.automation.android.util.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * The type Customized authenticator.
 */
public class CustomizedAuthenticator extends Authenticator {
    /**
     * The User name.
     */
    String userName = null;
    /**
     * The Password.
     */
    String password = null;

    /**
     * Instantiates a new Customized authenticator.
     */
    public CustomizedAuthenticator() {
    }

    /**
     * Instantiates a new Customized authenticator.
     *
     * @param username the username
     * @param password the password
     */
    public CustomizedAuthenticator(String username, String password) {
        this.userName = username;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password);
    }
}
