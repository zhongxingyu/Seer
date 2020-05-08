 package wfm.task;
 
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import org.activiti.engine.delegate.DelegateExecution;
 import org.activiti.engine.delegate.JavaDelegate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import wfm.db.ACT_ID_USER;
 import wfm.db.Course;
 
 
 public class NotifyUserAboutConfirmation implements JavaDelegate {
 	
 	public static Logger log = LoggerFactory.getLogger(NotifyUserAboutConfirmation.class);
 	
 	private Course courseToApprove;
 	private ACT_ID_USER userToApprove;
 	
 	@Override
 	public void execute(DelegateExecution execution) throws Exception {
 		
 		setCourseToApprove((Course) execution.getVariable("courseToApprove"));					
 		setUserToApprove((ACT_ID_USER) execution.getVariable("userToApprove"));
 		
 		log.info("Sending mail...to: "+userToApprove.getEmail_()+" with name "+userToApprove.getId_()+" as subscription confirmation for course: "+courseToApprove.getName());	
 		
 		String msgType = "confirmed";
 
 		initializeMailService(userToApprove.getEmail_(),courseToApprove.getName(),userToApprove.getId_(), msgType);
 		log.info("done...");
 	}
 	
 	//set that for message
 	private void initializeMailService(String add,String cName, String uName, String msgType) throws AddressException, MessagingException {
 		
 		   // Sender's email ID needs to be mentioned
 		   String from = "noreply.sccms@michaelb.at";
 		   String pass = "9c5567f9";
 		   // Recipient's email ID needs to be mentioned.
 		   String to = add;
 
 		   String host = "mail.michaelb.at";
 
 		   // Get system properties
 		   Properties properties = System.getProperties();
 		   // Setup mail server
 		   properties.put("mail.smtp.starttls.enable", "true");
 		   properties.put("mail.smtp.ssl.trust", "mail.michaelb.at");
 		   properties.put("mail.smtp.host", host);
 		   properties.put("mail.smtp.user", from);
 		   properties.put("mail.smtp.password", pass);
 		   properties.put("mail.smtp.port", "587");
 		   properties.put("mail.smtp.auth", "true");
 
 		   // Get the default Session object.
 		   Session session = Session.getInstance(properties);
 
 		   try{
 		      // Create a default MimeMessage object.
 		      MimeMessage message = new MimeMessage(session);
 
 		      // Set From: header field of the header.
 		      message.setFrom(new InternetAddress(from));
 
 		      // Set To: header field of the header.
 		      message.addRecipient(Message.RecipientType.TO,
 		                               new InternetAddress(to));
 
 		      // Set Subject: header field
 		      message.setSubject("SCCMS: Course "+msgType+"!");
 
 		      // Now set the actual message
 		      // set header according to reason
 		      String header = "\nThis Email is a notification that your subscription to the course '"+cName+"' has been "+msgType+"!\n";
 		     
 		      String text = "subscription confirmation"; 
 			     
 		      message.setText("Dear "+uName+"!\n" +
 		    		header+
 		      		"\n"+
 		      		"This is an automatically generated course " +  text + ".\n\n" +
 		      		"Best regards,\nSCCMS");	      
 		      
 		      // Send message
 		      Transport transport = session.getTransport("smtp");
 		      transport.connect(host, from, pass);
 		      transport.sendMessage(message, message.getAllRecipients());
 		      transport.close();
 		      log.info("Sent message successfully....");
 		   }catch (MessagingException mex) {
			   log.error("Sending message failed: "+mex.getMessage());
 		   }			
 		}
 
 	public Course getCourseToApprove() {
 		return courseToApprove;
 	}
 
 	public void setCourseToApprove(Course courseToApprove) {
 		this.courseToApprove = courseToApprove;
 	}
 
 	public ACT_ID_USER getUserToApprove() {
 		return userToApprove;
 	}
 
 	public void setUserToApprove(ACT_ID_USER userToApprove) {
 		this.userToApprove = userToApprove;
 	}
 }
