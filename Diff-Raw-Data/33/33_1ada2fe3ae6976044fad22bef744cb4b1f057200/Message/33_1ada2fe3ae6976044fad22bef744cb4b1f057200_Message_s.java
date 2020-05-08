 package coeditor;
 
 public class Message {
 	public String clientId;
 	public String action;
 	public String content;
 
 	public Message() {
 		this.clientId = "";
 		this.action = "";
 		this.content = "";
 	}
 
 	public Message(String clientId, String action, String content) {
 		this.clientId = clientId;
 		this.action = action;
 		this.content = content;
 	}
 }
