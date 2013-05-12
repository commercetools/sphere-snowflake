package utils;

import play.Play;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class Email {

    String APIKey = "your Mailjet API Key";
    String SecretKey = "your Mailjet Secret Key";
    public String from;
    public String to;
    public String subject;
    public String body;

    public Email(String to, String from, String subject, String body) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public Email(String to, String subject, String body) {
        this.from = Play.application().configuration().getString("mail.smtp.from");
        this.to = to;
        this.subject = subject;
        this.body = body;
    }

    public void send() {
        String host = Play.application().configuration().getString("mail.smtp.host");
        int port = Play.application().configuration().getInt("mail.smtp.port");
        boolean auth = Play.application().configuration().getBoolean("mail.smtp.auth");
        send(host, port, auth);
    }

    public void send(String host, int port, boolean auth) {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", auth);

        Authenticator authenticator = null;
        if (auth) {
            authenticator = new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(APIKey, SecretKey);
                }
            };
        }
        Session session = Session.getDefaultInstance(properties, authenticator);

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