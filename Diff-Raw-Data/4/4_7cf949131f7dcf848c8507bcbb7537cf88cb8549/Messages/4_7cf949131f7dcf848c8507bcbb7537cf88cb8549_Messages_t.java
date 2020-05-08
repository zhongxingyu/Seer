 package game;
 
 public class Messages {
 
 	public static Messages messages;
 	
 	public static Messages getInstance(){
 		if (messages == null){
 			messages = new Messages();
 		}
 		return messages;
 	}
 	
	boolean showMessage;
	String messageText = "";
 	
 	public void welcome(){
 		
 		showMessage = true;
 		messageText = "The Demiurge welcomes you to Operation Lapis. \n\nPress enter to continue...";
 		
 	}
 
 	public boolean isShowMessage() {
 		return showMessage;
 	}
 
 	public void setShowMessage(boolean showMessage) {
 		this.showMessage = showMessage;
 	}
 
 	public String getMessageText() {
 		return messageText;
 	}
 
 	public void setMessageText(String messageText) {
 		this.messageText = messageText;
 	}
 	
 }
