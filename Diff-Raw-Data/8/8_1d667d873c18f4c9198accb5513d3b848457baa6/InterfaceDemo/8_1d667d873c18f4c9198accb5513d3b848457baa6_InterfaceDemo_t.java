 package Basics;
 
 /**
  * @author Knut Hartmann <BR>
  *         Flensburg University of Applied Sciences <BR>
  *         Knut.Hartmann@FH-Flensburg.DE
  * 
  * @version October 14, 2012
  */
 
import Graphics.*;

 public class InterfaceDemo implements IConversation
 {
 
 	public void send(int senderID, int receiverID, String message) {
 		System.out.println(message);
 	}
 
 	public String createMessage(int sourceID) {
 		String[] messages = { "... Gähn ...", "Ich bin soo müde!",
 				"Lass mich in Frieden!", "Wer bist Du denn überhaupt?" };
 		/*
 		for (String msg : messages) { 
 			System.out.println(msg);
 		}
 		*/		
 		int index = Tools.getNaturalNumber(messages.length);
 		return (messages[index]);
 	}
 
 	public static void main(String[] args) {
 		InterfaceDemo protocoll = new InterfaceDemo();
 		int myID = 0;
 		int partnerID = 1;
 
 		protocoll.send(myID, partnerID, "Hallo!");
 		String msg = protocoll.createMessage(partnerID);
 		System.out.println(msg);
 	}
 }
