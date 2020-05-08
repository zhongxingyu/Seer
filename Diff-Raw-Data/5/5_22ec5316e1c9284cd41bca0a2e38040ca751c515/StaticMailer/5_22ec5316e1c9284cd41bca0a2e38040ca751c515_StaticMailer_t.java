 package ca.brood.brootils.mail;
 
 public class StaticMailer {
 	private static Emailer emailer;
 	
 	static {
 		emailer = new Emailer();
 	}
 	
 	private StaticMailer() {
 		
 	}
 	
	public static synchronized void configureSmtp(String host, String user, String password) {
 		emailer.configureSmtp(host, user, password);
 	}
 	
	public static synchronized boolean sendEmail(String to, String from, String subject, String body) {
 		return emailer.sendEmailSimple(to, from, subject, body);
 	}
 }
