 package manager.dht.messages.broadcast;
 
 import manager.Message;
 import manager.dht.NodeID;
 
 public abstract class BroadcastMessage extends Message {
 	
 	//Definition of the region the broadcast is responsible for
 	private NodeID startKey;
 	private NodeID endKey;
 
 	//Type of the delivered message
 	private int internalType;
 	
 	public BroadcastMessage(String from, String to,NodeID startKey,NodeID endKey,int internalType) {
 		super(from,to,Message.BROADCAST);
 		this.internalType = internalType;
 		this.startKey = startKey;
 		this.endKey = endKey;
 	}
 	
 	public NodeID getStartKey() {
 		return startKey;
 	}
 
 	public NodeID getEndKey() {
 		return endKey;
 	}
 
 	protected String toString(String text) {
		return "BROADCAST: ({" + startKey.toString() + "}->{" + endKey.toString() + "}) | " + extractMessage().toString();
 	}
 	
 	//Extract unicast message
 	public abstract Message extractMessage();
 	
 	//Clone with new addresses to easily forward messages
 	public abstract BroadcastMessage cloneWithNewAddresses(String from,String to,NodeID startKey,NodeID endKey);
 }
