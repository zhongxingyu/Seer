 package fi.iki.photon.longminder;
 
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import javax.annotation.Resource;
 import javax.ejb.EJB;
 import javax.ejb.LocalBean;
 import javax.ejb.Schedule;
 import javax.ejb.Stateless;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import fi.iki.photon.longminder.entity.Alert;
 import fi.iki.photon.longminder.entity.User;
 import fi.iki.photon.longminder.entity.dto.UserDTO;
 
 /**
  * Session Bean implementation class EmailManagerBean
  */
 @Stateless
 @LocalBean
 public class EmailManagerBean implements EmailManager, Serializable {
 
 	private static final long serialVersionUID = 1L;
 	
 	@EJB UserManagerBean um;
 	@EJB AlertManagerBean am;
 	
 	@Resource(name="mail/longminderMail")
 	private Session mailSession;
 
 	private boolean ignoreEmail = false;
 
     /**
      * Default constructor. 
      */
     public EmailManagerBean() {
     	// Do nothing.
     }
 
     /** Sends a verification email to the email specified in UserDTO. */
     
 	@Override
	public boolean sendVerificationEmail(String serverBase, String email) {
 		try {
 			User u = um.findTrueUserForEmail(email);
 			um.createLogin(u);
 			if (u.getLogins() == null || u.getLogins().size() == 0) {
 				throw new Error("Login key should have been created.");
 			}
 			String key = u.getLogins().get(0).getLoginkey();
 			
 			Message msg = new MimeMessage(mailSession);
 			msg.setFrom(new InternetAddress("noreply@example.com"));
 			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(u.getEmail()));
 			
 			msg.setSubject("Please confirm your email address for Longminder");
 			msg.setContent("You can't receive Longminder email notifications before you have confirmed your email address. Please do this by clicking the following link: \n\n" +
 					serverBase + "/verify.xhtml?key=" + key + "\n\n" + 
 					"If you haven't requested this confirmation, please ignore this email.", "text/plain");
 			msg.setSentDate(new Date());
 			
 			Transport.send(msg);
 			
 		} catch (MessagingException e) {
 			e.printStackTrace();
			return false;
 		}
		return true;
 	}
 
 	/** Sends emails for all users in the database whose email has been verified. */
 	
 	@Schedule(minute="*",hour="*")
 	public void sendEmails() {
 		System.out.println("Send emails!");
 		List<User> users = um.getVerifiedUsers();
 		
 		for (User u : users) {
 			sendAlertEmail(u);
 		}
 	}
 
 	private void sendAlertEmail(User u) {
 		try {
 /*			um.createLogin(u);
 			if (u.getLogins() == null || u.getLogins().size() == 0) {
 				throw new Error("Login key should have been created.");
 			}
 			String key = u.getLogins().get(0).getLoginkey();
 */	
 			String alertString = getAlertEmailString(u);
 			
 			
 			if (alertString == null) {
 				System.out.println("Skipping " + u.getEmail());
 				return;
 			}
 			
 			if (ignoreEmail) {
 				System.out.println(alertString);
 				return;
 			}
 
 			Message msg = new MimeMessage(mailSession);
 			msg.setFrom(new InternetAddress("noreply@example.com"));
 			msg.setRecipient(Message.RecipientType.TO, new InternetAddress(u.getEmail()));
 			
 			msg.setSubject("You have new Longminder alerts");
 			msg.setContent(alertString, "text/plain");
 			msg.setSentDate(new Date());
 			
 			Transport.send(msg);
 			
 		} catch (MessagingException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String getAlertEmailString(User u) {
 		List<Alert> alerts = am.getRawAlertsForEmail(u.getEmail());
 
 		if (alerts == null || alerts.size() == 0) return null;
 		
 		boolean alertsFired = false;
 
 		ResourceBundle res = ResourceBundle.getBundle("fi.iki.photon.longminder.Messages");
 		
 		String alertString = res.getString("email.greeting") + " " + u.getFirstname() +", \n\n" + res.getString("email.alertsdue") + ":\n\n";
 		Date now = new Date();
 		DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale(u.getLocale().substring(0, 2)));
 
 		List<Alert> newAlertList = new ArrayList<Alert>();
 		
 		try { 
 		for (Alert a : alerts) {
 				boolean alertLoop = true;
 				boolean newAlerts = true;
 				while (alertLoop && newAlerts) {
 					System.out.println(a.getDescription());
 					if (a.getNextAlert().before(now)) {
 						System.out.println(a.getDescription());
 						alertString += res.getString("email.alert") + " \"" + a.getDescription() + "\" " + res.getString("email.due") + " " + format.format(a.getNextAlert()) + "\n";
 	
 						alertsFired = true;
 						Alert newAlert = a.rotateAlert();
 						a.setFired(true);
 						newAlertList.add(newAlert);
 						
 						if (newAlert != null) {
 							newAlerts = true;
 							a = newAlert;
 						} else {
 							newAlerts = false;
 						}
 					} else {
 						alertLoop = false;
 					}
 				}
 		}
 		u.getAlerts().addAll(newAlertList);
 		} catch (java.util.ConcurrentModificationException e) {
 			e.printStackTrace();
 			System.out.println("VERY WEIRD");
 		}
 		alertString += "\n" + res.getString("email.ending");
 
 		if (! alertsFired) return null;
 		
 		return alertString;
 	}
 }
