 package edu.berkeley.cs.cs162;
 
import java.net.UnknownHostException;
 
 public class BenchmarkReceiver extends AbstractChatClient {
 	@Override
 	protected synchronized void benchmark(TransportObject recObject) {
 		send(recObject.getSender(),recObject.getSQN(),recObject.getMessage() + "ghost");
 	}
 	
 	@Override
 	protected void send(String dest, int sqn, String msg){
 		if(!connected || !isLoggedIn)
 			return;
 		TransportObject toSend = new TransportObject(Command.send,dest,sqn,msg);
 		try {
 			isWaiting = true;
 			reply = Command.send;
 			sent.writeObject(toSend);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
	
	public static void main(String[] args) throws UnknownHostException{
		new BenchmarkReceiver();
	}
 }
