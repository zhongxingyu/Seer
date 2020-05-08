 package edu.berkeley.cs.cs162;
 
import java.util.Calendar;

 public class MessageDeliveryTask implements Runnable {
 	String source, destination, message;
 	ChatServerInterface server;
 	
 	MessageDeliveryTask(ChatServerInterface s, String source, 
 				String destination, String message) {
 		this.source = source;
 		this.destination = destination;
 		this.message = message;
 		this.server = s;
 	}
 	
 	
 	@Override
 	public void run() {
		System.out.print("Starting: ");
		System.out.println(Calendar.getInstance().getTime());
 		BaseUser u = this.server.getUser(this.source);
 		if (u == null)
 			return;
 		u.send(this.destination, this.message);
		System.out.print("Finishing: ");
		System.out.println(Calendar.getInstance().getTime());		
 	}
 
 }
