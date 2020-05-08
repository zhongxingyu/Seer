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
	AbstractLamportMessage msg;
	
	public AcknowledgeMessage(InetSocketAddress addr, AbstractLamportMessage msg){
 		super(addr);
		this.msg = msg;
 	}
 	
 	public String toString(){
 		return String.format("[Acknowledge from %s]", getSender());
 	}
	
	public AbstractLamportMessage getMessage(){
		return msg;
	}
 }
