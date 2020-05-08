  
 package edu.ucsb.cs290.touch.to.chat.crypto;
 
import java.io.IOException;
 import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
 import java.util.Date;
 
 import edu.ucsb.cs290.touch.to.chat.remote.messages.SignedMessage;
 
 public class TimestampedMessage implements Serializable {
 	final SignedMessage message;
 	final Date time;
 	public TimestampedMessage(SignedMessage message, long dateTime) {
 		this.time = new Date(dateTime);
 		this.message = message;
 	}
 	
 	public String toString() {
 		try {
 			return message.toString() + "\n Received at " + time.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "Error fetching message";
 		}
 	}
 }
