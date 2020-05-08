 package wfm.task;
 
 import java.util.List;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.activiti.engine.delegate.DelegateExecution;
 import org.activiti.engine.delegate.JavaDelegate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import wfm.db.ACT_ID_USER;
 import wfm.db.Course;
 
 
 public class NotifyAdminAboutTwitterException implements JavaDelegate {
 
 	public static Logger log = LoggerFactory.getLogger(NotifyAdminAboutTwitterException.class);
 
 
 
 	@PersistenceContext
 	private EntityManager entityManager = LoginTask.entityManager; 
 
 	private Course courseToApprove;
 	private ACT_ID_USER userToApprove;
 
 	@Override
 	public void execute(DelegateExecution execution) throws Exception {
 
 		log.info("Notify Admin was executed...");
 		setCourseToApprove((Course) execution.getVariable("courseToApprove"));					
 		setUserToApprove((ACT_ID_USER) execution.getVariable("userToApprove"));
 		
 		String errorMsg = (String) execution.getVariable("twitterException");
 
 
 		try {	
 			Query q = entityManager
 					.createNativeQuery("SELECT user_ID_  FROM ACT_ID_MEMBERSHIP a WHERE a.group_ID_ ='admin'");
 
 			@SuppressWarnings("unchecked")
 			List<String> adminList = q.getResultList();
 			for(String user : adminList){
 				ACT_ID_USER admin = entityManager.find(ACT_ID_USER.class, ((String) user));
 
 				log.info("Sending mail...to: "+admin.getEmail_()+" with name "+admin.getId_()+" as notification that posting on Twitter failed. Member: "+userToApprove.getId_()+" Course: "+courseToApprove.getName());	
 
 				initializeMailService(admin.getEmail_(),courseToApprove.getName(),userToApprove.getId_(), admin.getId_(),errorMsg);
 
 			}
 		} catch (Exception e) {
 			log.error("Exception sending Twitter notification to admin: "+e.getMessage());
 
 		}
 
 	}
 
 	//set that for message
 	private void initializeMailService(String add,String cName, String uName, String adminName, String errorMsg) throws AddressException, MessagingException {
 
 		// Sender's email ID needs to be mentioned
 		String from = "noreply.sccms@michaelb.at";
 		String pass = "9c5567f9";
 		// Recipient's email ID needs to be mentioned.
 		String to = add;
 
 		String host = "mail.michaelb.at";
 
 		// Get system properties
 		Properties properties = System.getProperties();
 		// Setup mail server
 		// properties.put("mail.smtp.starttls.enable", "true");
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
 			message.setSubject("SCCMS: Posting on Twitter failed!");
 
 			// Now set the actual message
 			// set header according to reason
 			String header = "\nThis Email is a notification that posting on Twitter by member '"+uName+"' for the course '"+cName+"' failed!\n";
 
 		
 
 			message.setText("Dear "+adminName+"!\n" +
 					header+
 					"\n"+
 					"See error message for more details:\n"+
 					errorMsg+"\n\n"+
 					"This is an automatically generated error notification.\n\n" +
 					"Best regards,\nSCCMS");	      
 
 			// Send message
 			Transport transport = session.getTransport("smtp");
 			transport.connect(host, from, pass);
 			transport.sendMessage(message, message.getAllRecipients());
 			transport.close();
 			log.info("Sent message successfully....");
 		}catch (MessagingException mex) {
			mex.printStackTrace();
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
