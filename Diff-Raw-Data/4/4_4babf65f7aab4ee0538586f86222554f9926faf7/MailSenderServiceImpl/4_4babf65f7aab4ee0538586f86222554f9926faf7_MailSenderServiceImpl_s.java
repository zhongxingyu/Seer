 package com.twistlet.falcon.model.service;
 
 import java.util.Date;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.mail.MailException;
 import org.springframework.mail.SimpleMailMessage;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.stereotype.Service;
 
 @Service
 public class MailSenderServiceImpl implements MailSenderService {
 
 	private final String senderName;
 	private final String senderAddress;
 	private final String subject;
 	private final JavaMailSender javaMailSender;
 	private final DatabaseLoggingService databaseLoggingService;
 
 	@Autowired
 	public MailSenderServiceImpl(
 			@Value("${smtp.name}") final String senderName,
			@Value("smtp.address") final String senderAddress,
			@Value("smtp.subject") final String subject,
 			final JavaMailSender javaMailSender,
 			final DatabaseLoggingService databaseLoggingService) {
 		this.senderName = senderName;
 		this.senderAddress = senderAddress;
 		this.subject = subject;
 		this.javaMailSender = javaMailSender;
 		this.databaseLoggingService = databaseLoggingService;
 	}
 
 	@Override
 	public void send(final String sendTo, final String message) {
 		final SimpleMailMessage mailMessage = new SimpleMailMessage();
 		final String from = "\"" + senderName + "\" <" + senderAddress + ">";
 		mailMessage.setFrom(from);
 		mailMessage.setTo(sendTo);
 		mailMessage.setSentDate(new Date());
 		mailMessage.setSubject(subject);
 		mailMessage.setText(message);
 		String errorMessage = null;
 		try {
 			javaMailSender.send(mailMessage);
 		} catch (final MailException e) {
 			errorMessage = e.toString();
 			throw e;
 		} finally {
 			databaseLoggingService.logEmailSent(sendTo, message, errorMessage);
 		}
 	}
 }
