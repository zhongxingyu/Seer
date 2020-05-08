 package cyberprime.util;
 
 
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import cyberprime.entities.Clients;
 
 
 public class EmailSender {
 		
 	
 	final String jdaySend = "jday.sg@gmail.com";
 	final String jdayPW = "jdayjday";
 
 	Session session = null;
 	Clients c;
 	
 		public EmailSender(Clients c) {
 
 			this.c = c;
 			Properties props = new Properties();
 			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.starttls.enable", true);
			props.put("mail.smtp.ssl.trust", "*");
 			props.put("mail.smtp.host", "smtp.gmail.com");
 			props.put("mail.smtp.port", "587");
 
 			 
 			session = Session.getInstance(props,
 			  new javax.mail.Authenticator() {
 				protected PasswordAuthentication getPasswordAuthentication() {
 					return new PasswordAuthentication(jdaySend, jdayPW);
 				}
 			  });
 			
 	}
 		
 		
 		public void sendActivationLink(String token){
 			
 			String subject="Account activation";
 
 			
 			try {
 	 
 				Message message = new MimeMessage(session);
 
 				message.setFrom(new InternetAddress(jdaySend));
 				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(c.getEmail()));
 				message.setSubject(subject);
 				message.setContent("<h1>Dear client,</h1><p>Please activate your account by clicking " +
						"<a rel='nofollow' target='_blank' href='https://samuelong-pc:443/CyberPrime2/ActivateAccount?token="+token+"&userId="+c.getUserId()+"'>here</a></p>",
                         "text/html" );
 				System.out.println("Before sending");
 				Transport.send(message);
 				System.out.print("send");
 	 
 			} catch (MessagingException e) {
 				throw new RuntimeException(e);
 			}
 			
 		}
 		
 		public void sendResetPattern(String content,String pattern){
 			
 			String subject="Pattern Reset";
 			String newPatternS = "";
 			ArrayList<Character>newPattern = new ArrayList<Character>();
 			
 			for(int i=0; i<pattern.length();i++){
 				char c = pattern.charAt(i);
 				if(c == '+'){
 					newPattern.add('%');
 					newPattern.add('2');
 					newPattern.add('B');
 				}
 				
 				else if(c == '\n'){
 					newPattern.add('%');
 					newPattern.add('0');
 					newPattern.add('D');
 					newPattern.add('%');
 					newPattern.add('0');
 					newPattern.add('A');
 				}
 				
 				else{
 					newPattern.add(c);
 				}
 
 			}
 			
 			
 			for(int i=0; i<newPattern.size();i++){
 				newPatternS += newPattern.get(i);
 			}
 
 			try {
 	 
 				Message message = new MimeMessage(session);
 
 				message.setFrom(new InternetAddress(jdaySend));
 				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(c.getEmail()));
 				message.setSubject(subject);
 				message.setContent("<h1>Dear client,</h1><p>This is the new pattern for your account</p>"+
 						"<p>"+content+"</p>"+
 						"If you did not request to reset your pattern, please click "+
						"<a rel='nofollow' target='_blank' href='https://samuelong-pc:443/CyberPrime2/ResetPattern?pattern="+newPatternS+"&userId="+c.getUserId()+"'>here</a>",
                         "text/html" );
 				Transport.send(message);
 				System.out.print("send");
 	 
 			} catch (MessagingException e) {
 				throw new RuntimeException(e);
 			}
 			
 		}
 }
 
