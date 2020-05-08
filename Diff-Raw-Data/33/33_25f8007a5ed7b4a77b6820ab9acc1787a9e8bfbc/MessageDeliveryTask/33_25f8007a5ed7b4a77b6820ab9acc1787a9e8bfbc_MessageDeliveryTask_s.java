 package edu.berkeley.cs.cs162;
 
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
 		BaseUser u = this.server.getUser(this.source);
 		if (u == null)
 			return;
 		u.send(this.destination, this.message);
 	}
 
 }
