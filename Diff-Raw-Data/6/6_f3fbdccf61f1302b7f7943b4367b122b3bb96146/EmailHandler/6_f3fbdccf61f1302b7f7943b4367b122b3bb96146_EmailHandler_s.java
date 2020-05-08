 package gov.usgs.cida.gdp.communication;
 
 import java.util.Properties;
 
 import gov.usgs.cida.gdp.utilities.PropertyFactory;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.slf4j.LoggerFactory;
 
 public class EmailHandler {
 	private static org.slf4j.Logger log = LoggerFactory.getLogger(EmailHandler.class);
 
 	public static void sendMessage(EmailMessage message) throws AddressException, MessagingException {
 		Properties properties = System.getProperties();
 
 //		if (Boolean.parseBoolean(PropertyFactory.getProperty("development"))) {
			properties.put("mail.smtp.host", PropertyFactory.getProperty("development.mail.smtp.host"));
			properties.put("mail.smtp.port", PropertyFactory.getProperty("development.mail.smtp.port"));
 //		} else {
 //			properties.put("mail.smtp.host", PropertyFactory.getProperty("production.mail.smtp.host"));
 //			properties.put("mail.smtp.port", PropertyFactory.getProperty("production.mail.smtp.port"));
 //		}
 
 		Session session = Session.getInstance(properties, null);
 		session.setDebug(true);
 
 
 		Message msg = new MimeMessage(session);
 
 		InternetAddress[] bccList = new InternetAddress[message.getBcc().size()];
 		for (int counter = 0;counter < message.getBcc().size();counter++)  {
 			InternetAddress email = new InternetAddress();
 			email.setAddress(message.getBcc().get(counter));
 			bccList[counter] = email;
 		}
 		msg.setRecipient(Message.RecipientType.TO, new InternetAddress(message.getTo()));
 		msg.setRecipients(Message.RecipientType.BCC, bccList);
 		msg.setFrom(new InternetAddress(message.getFrom()));
 		msg.setSubject(message.getSubject());
 		msg.setContent(message.getContent(), "text/plain");
 
 		Transport.send(msg);
 		log.info("Sent E-Mail From " + message.getFrom() + " to " + message.getTo());
 	}
 }
