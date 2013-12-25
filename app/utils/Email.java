package utils;

import play.Play;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class Email {

    static String key = Play.application().configuration().getString("mail.auth.key");
    static String secret = Play.application().configuration().getString("mail.auth.secret");

    public static void send(String to, String subject, String body) {
        String from = Play.application().configuration().getString("mail.smtp.from");
        String host = Play.application().configuration().getString("mail.smtp.host");
        int port = Play.application().configuration().getInt("mail.smtp.port");
        boolean auth = Play.application().configuration().getBoolean("mail.smtp.auth");
        send(to, subject, body, from, host, port, auth);
    }

    public static void send(String to, String subject, String body, String from, String host, int port, boolean auth) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", auth);
        properties.put("mail.smtp.socketFactory.port", port);
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Authenticator authenticator = null;
        if (auth) {
            authenticator = new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(key, secret);
                }
            };
        }
        Session session = Session.getInstance(properties, authenticator);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            MimeBodyPart mimeBody = new MimeBodyPart();
            mimeBody.setContent(body, "text/html");
            MimeMultipart mimeMulti = new MimeMultipart();
            mimeMulti.addBodyPart(mimeBody);
            message.setContent(mimeMulti);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}