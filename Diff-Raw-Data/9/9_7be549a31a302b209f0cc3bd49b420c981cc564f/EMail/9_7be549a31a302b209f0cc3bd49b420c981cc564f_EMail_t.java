 /**
  * 
  */
 package models.mail;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.mail.MessagingException;
 import javax.mail.SendFailedException;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 /**
  * @author Jens N. Rammant
  *
  */
 public class EMail {
 	
 	private String message="";
 	private String subject="";
 	private List<InternetAddress> to;
 	private List<InternetAddress> cc;
 	private List<InternetAddress> replyTo;
 	private List<File> attachments;
 	
 	public EMail(){
 		this.to = new ArrayList<InternetAddress>();
 		this.cc = new ArrayList<InternetAddress>();
 		this.replyTo = new ArrayList<InternetAddress>();
 		this.attachments = new ArrayList<File>();
 	}
 	
 	public void setMessage(String message){
 		this.message=message;
 	}
 	
 	public void appendMessage(String extraMessage){
 		if(this.message==null)this.message="";
 		this.message = this.message + extraMessage;
 	}
 	
 	public void setSubject(String subject){
 		this.subject=subject;
 	}
 	
 	public boolean addToAddress(String email){
 		try {
 			InternetAddress add = new InternetAddress(email);
 			this.to.add(add);
 			return true;
 		} catch (AddressException e) {
 			return false;
 		} 
 		
 	}
 	
 	public boolean addCCAddress(String email){
 		try {
 			InternetAddress cc = new InternetAddress(email);
 			this.cc.add(cc);
 			return true;
 		} catch (AddressException e) {
 			return false;
 		} 
 		
 	}
 	
 	public boolean addReplyTo(String email){
 		try {
 			InternetAddress repl = new InternetAddress(email);
 			this.replyTo.add(repl);
 			return true;
 		} catch (AddressException e) {
 			return false;
 		}
 	}
 	
 	public void addAttachment(File attachment){
 		this.attachments.add(attachment);
 	}
 	
	public void send() throws MessagingException{
 		//TODO actually implement
 		System.out.println("To:");
 		for(InternetAddress ia : this.to){
 			System.out.println(ia.getAddress());
 		}
 		System.out.println("-----");
 		System.out.println("CC:");
 		for(InternetAddress ia : this.cc){
 			System.out.println(ia.getAddress());
 		}
 		System.out.println("-----");
 		System.out.println("Reply To:");
 		for(InternetAddress ia : this.replyTo){
 			System.out.println(ia.getAddress());
 		}
 		System.out.println("-----");
 		System.out.println("Subject:");
 		System.out.println(this.subject);
 		System.out.println("-----");
 		System.out.println("Message:");
 		System.out.println(this.message);
 		System.out.println("------");
 		System.out.println("Attachments:");
 		for(File f : this.attachments){
 			System.out.println(f.getName());
 		}
 	}
 	
 	
 
 }
