package com.appdynamics.extensions.eventsreporter.email;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.eventsreporter.model.Command;
import com.appdynamics.extensions.eventsreporter.model.EmailData;
import com.appdynamics.extensions.eventsreporter.utils.EventsReporterConstants;

public class EmailHelper {

	private static Logger logger = Logger.getLogger("com.singularity.extensions.eventsreporter.email.EmailHelper");

	public boolean sendEmail(EmailData emailData){
		
		// Get system properties
		Properties properties = System.getProperties();

		EmailAction eAction = emailData.getEmailAction();
		Map<String, String> props = eAction.getMailServerProperties();

		final String mailUser = props.get(EventsReporterConstants.MAIL_SMTP_USER);
		final String mailPswd = props.get(EventsReporterConstants.MAIL_SMTP_PASSWORD);
		final String mailAuth = props.get(EventsReporterConstants.MAIL_SMTP_AUTH);

		for(String key : props.keySet()){
			properties.setProperty(key, props.get(key));
		}

		// Get the Session object.
		Session session = null;

		if(mailAuth.equals("false")){
			session = Session.getDefaultInstance(properties);
		}else{
			session = Session.getInstance(properties, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() { 
					return new PasswordAuthentication(mailUser, mailPswd);
				}
			});
		}

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); 
		
		try{			
			//Add handlers for main MIME types
			MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
			mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
			mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
			mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
			mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
			mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
			CommandMap.setDefaultCommandMap(mc);
			
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From
			message.setFrom(new InternetAddress(mailUser));

			// Set To
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailData.getEmailAction().getToAddress()));

			// Set Subject
			message.setSubject(emailData.getSubject());

			Multipart mp = new MimeMultipart();
			MimeBodyPart htmlPart = new MimeBodyPart();

			// Send the email content
			StringBuilder sb = new StringBuilder();
			sb.append("<p><b>Message: '</b>").append(emailData.getCommand()).append("' ").append(emailData.getBody()).append("</p>");
			sb.append("<p><b>Hostname: </b>").append(emailData.getHostname()).append("</p>");
			sb.append("<p><b>Alerting System: </b>").append(emailData.getAlertSystem()).append("</p>");
			sb.append("<p><b>Date/Time: </b>").append(new Date(System.currentTimeMillis()).toString()).append("</p>");

			htmlPart.setContent(sb.toString(), "text/html" );
			mp.addBodyPart(htmlPart);
						                                             
			Thread.currentThread().setContextClassLoader(javax.mail.Session.class.getClassLoader());
			
			message.setContent(mp);	
			
			// Send message
			Transport.send(message);
						
			return true;
		} catch (MessagingException mex) {
			logger.error("Error occured while sending email : " + mex);
			return false;
		} catch (Exception ex) {
			logger.error("Error occured while sending email : " + ex);
			return false;
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
	}

	public boolean verifyEmail(EmailData emailData){
		logger.debug("Verifying Email Action for emailData : " + emailData);
		return sendEmail(emailData);
	}

	public EmailData getEmailData(EmailAction eAction, Command cmd){
		EmailData emailData = new EmailData();
		emailData.setEmailAction(eAction);
		if(cmd != null){
			emailData.setBody(cmd.getPropertyValue(EventsReporterConstants.BODY));
			emailData.setCommand(cmd.getRun());
			emailData.setSubject(cmd.getPropertyValue(EventsReporterConstants.SUBJECT));
		}else{
			emailData.setBody("Dummy Content for Email Verification");
			emailData.setCommand("Dummy Command");
			emailData.setSubject("Test Email for Verification, Kindly Ignore");
		}
		return emailData;

	}
	public static void main(String[] args){
		//EmailHelper.sendEmail();
	}


}
