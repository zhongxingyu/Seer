 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package week4.multicast.messages;
 
 import java.net.InetSocketAddress;
 
 /**
  *
  * @author larss
  */
 public class AcknowledgeMessage extends AbstractLamportMessage {
	public AcknowledgeMessage(InetSocketAddress addr){
 		super(addr);
 	}
 	
 	public String toString(){
 		return String.format("[Acknowledge from %s]", getSender());
 	}
 }
