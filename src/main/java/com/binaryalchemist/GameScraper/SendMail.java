package com.binaryalchemist.GameScraper;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMail {

	private int port = 465;
	private String host = "smtp.wp.pl";
	private String from = "patdurman@wp.pl";
	private boolean auth = true;
	private String username = "patdurman@wp.pl";
	private String password = "****";
	//private Protocol protocol = Protocol.SMTPS;
	private boolean debug = false;
	

	public void sendEmail(String to, String name, String mailAdress,
			String subject, String body) {
		System.out.println("send");
		
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.socketFactory.port", port);
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", auth);
		props.put("mail.smtp.port", port);
 
		Session session = Session.getDefaultInstance(props,
			new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username,password);
				}
			});
 
		
		session.setDebug(debug);
		
		MimeMessage message = new MimeMessage(session);
		try {
		    message.setFrom(new InternetAddress(from));
		    InternetAddress[] address = {new InternetAddress(to)};
		    message.setRecipients(Message.RecipientType.TO, address);
		    message.setSubject(subject);
		    message.setSentDate(new Date());
		    message.setText(body+" "+ mailAdress);
		    Transport.send(message);
		} catch (MessagingException ex) {
		    ex.printStackTrace();
		}


	}
	
}
