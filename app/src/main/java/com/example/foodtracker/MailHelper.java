package com.example.foodtracker;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.sun.mail.smtp.SMTPProvider;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.security.Provider;
import java.security.Security;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.Transport;

public class MailHelper {
    private static final String HOST = "smtp.gmail.com";
    private static final int PORT = 465;
    private static final String USERNAME = "maxb81887@gmail.com";
    private static final String PASSWORD = "aeeihzyqregksqti";

    public static void sendMail(String recipient, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
        message.setSubject(subject);
        message.setText(body);

        Transport.send(message);
    }

}
