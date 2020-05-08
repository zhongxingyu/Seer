 package org.lds.md.c2;
 
 import java.util.List;
 
 import org.apache.commons.mail.DefaultAuthenticator;
 import org.apache.commons.mail.Email;
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 @Component("SimpleSendEMail")
 public class SimpleSendEMail implements BeanRequest {
 	private static final Logger log = LoggerFactory
 			.getLogger(SimpleSendEMail.class);
 
 	private void sendEmail(String toEmail, String subject, String message) {
 		Email email = new SimpleEmail();
 		email.setSmtpPort(587);
		email.setAuthenticator(new DefaultAuthenticator("person@gmail.com",
				"PASSWORD"));
 		email.setDebug(true);
 		email.setHostName("smtp.gmail.com");
 		try {
 			email.setFrom("nathan.degraw@gmail.com");
 			email.setSubject(subject);
 			email.setMsg(message);
 			email.addTo(toEmail);
 			// email.setTLS(true);
 			email.setSSLOnConnect(true);
 			email.send();
 		} catch (EmailException e) {
 			log.error("Sending email failed!", e);
 		}
 	}
 
 	@Override
 	public Object get(List<String> parms) {
 		if (parms.size() >= 3) {
 			String toEmail = parms.get(0);
 			String subject = parms.get(1);
 			String message = parms.get(2);
 
 			sendEmail(toEmail, subject, message);
 		}
 
 		return null;
 	}
 
 	@Override
 	public void destroy() throws Exception {
 		// TODO Auto-generated method stub
 
 	}
 }
