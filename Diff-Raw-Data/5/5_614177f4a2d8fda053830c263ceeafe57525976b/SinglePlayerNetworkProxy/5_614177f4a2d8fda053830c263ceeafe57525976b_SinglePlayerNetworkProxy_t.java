 package linewars.network;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import linewars.network.messages.Message;
 
 /**
  * 
  * @author Taylor Bergquist
  *
  */
 public class SinglePlayerNetworkProxy implements MessageHandler {
 	
 	private int currentTick;
 	private HashMap<Integer, ArrayList<Message>> messageBank;
 	
 	public SinglePlayerNetworkProxy()
 	{
 		messageBank = new HashMap<Integer, ArrayList<Message>>();
 		currentTick = 1;
 		messageBank.put(0, new ArrayList<Message>());
 		messageBank.put(1, new ArrayList<Message>());
 	}
 
 	@Override
 	public void addMessage(Message toAdd) {
 		messageBank.get(currentTick).add(toAdd);
 		toAdd.setTimeStep(currentTick);
 	}
 
 	@Override
 	public Message[] getMessagesForTick(int tickID) {
 		if(currentTick == tickID){
 			messageBank.put(++currentTick, new ArrayList<Message>());
 		}
 		
 		
 		return messageBank.get(tickID).toArray(new Message[0]);
 	}
 
 	@Override
 	public void run() {
 		//do nothing
 	}
 
	@Override
	public void terminate() {
		//do nothing
	}

 }
