 package manager.dht;
 
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.TreeMap;
 
 import manager.CommunicationInterface;
 import manager.LookupServiceInterface;
 import manager.Message;
 import manager.dht.messages.broadcast.BroadcastMessage;
 import manager.dht.messages.broadcast.KeepAliveBroadcastMessage;
 import manager.dht.messages.broadcast.NotifyJoinBroadcastMessage;
 import manager.dht.messages.unicast.DuplicateNodeIdMessage;
 import manager.dht.messages.unicast.JoinAckMessage;
 import manager.dht.messages.unicast.JoinBusyMessage;
 import manager.dht.messages.unicast.JoinMessage;
 import manager.dht.messages.unicast.JoinResponseMessage;
 import manager.dht.messages.unicast.KeepAliveMessage;
 import manager.dht.messages.unicast.NotifyJoinMessage;
 import manager.dht.messages.unicast.NotifyLeaveMessage;
 import manager.listener.FingerChangeListener;
 
 public class Node extends Thread implements LookupServiceInterface {
 	//Communication
 	private CommunicationInterface communication;
 	private String bootstrapAddress;
 
 	//Own state in the DHT
 	private TreeMap<FingerEntry,FingerEntry> finger;
 	private FingerEntry identity;
 	private FingerEntry successor;
 	private FingerEntry predecessor;
 	
 	//Keep alive
 	private static final int KEEP_ALIVE_PERIOD = 10000;
 	private static final int KEEP_ALIVE_RANDOM_PERIOD = 10000;
 	private static final int JOIN_BLOCK_PERIOD = 15000;
 	
 	Timer timer = null;
 	TimerTask keepAlive;
 	
 	//Connection state
 	private boolean connected = false;
 	private FingerEntry blockJoinFor = null;
 	
 
 	public Node(CommunicationInterface communication,String bootstrapAddress) {
 		this.communication = communication;
 		
 		//Init fingertable
 		finger = new TreeMap<FingerEntry, FingerEntry>();
 		
 		//Generate hash from the local network address
 		//TODO ask stefan if inclusion of port address is reasonable
 		byte[] hash = SHA1Generator.SHA1(communication.getLocalIp());
 		
 		//Init timer
 		this.timer = new Timer();
 
 		//Set identity
 		setIdentity(hash);
 		this.successor = identity;
 		this.predecessor = identity;
 		
 		//Save bootstrap address
 		//No bootstrap means, WE are the beginning of the DHT
 		//If we are a bootstrapping node, that means bootstrapping address is null or is our address,
 		//we are always connected !!
 		if(bootstrapAddress == null || bootstrapAddress.equals(communication.getLocalIp())) {
 			//We are connected and we are our own successor
 			connected = true;
 		}
 		else {
 			this.bootstrapAddress = bootstrapAddress;
 		}
 		
 		//Start thread
 		this.start();
 	}
 
 	@Override
 	public void resolve(String uci) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void register(String uci) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void shutdown() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void handleMessage(Message message) {
 		//Don't process message if it was not for us!!
 		if(!message.getToIp().equals(identity.getNetworkAddress())) {
 			//TODO Remove sysout
 			System.out.println("!!!!! Message from THIS node !!!");
 			return;
 		}
 
 		//Analyse message
 		switch (message.getType()) {
 			//react on a Join message
 			case Message.JOIN:
 				//No action if not connected
 				if(!connected) break;
 				
 				JoinMessage join_msg = (JoinMessage) message;
 				Message answer = null;
 
 				FingerEntry predecessorOfJoiningNode = getPredecessor(join_msg.getKey());
 				
 				//Forward or answer?
 				if(predecessorOfJoiningNode.equals(identity)) {
 					//It's us => reply on JOIN
 
 					//Check if it exists
 					FingerEntry newFingerEntry = new FingerEntry(join_msg.getKey(),join_msg.getOriginatorAddress());
 					FingerEntry tempFinger;
 					
 					synchronized(finger) {
 						//Find finger
 						if(successor.equals(newFingerEntry)) {
 							tempFinger = successor;
 						}
 						else {
 							tempFinger = finger.get(newFingerEntry);
 						}
 					}
 					
 					//If another node tried to enter the DHT with the same key, send duplicate message
 					//Skip, if the same node tried again!
 					if(tempFinger != null) {
 						if(!tempFinger.getNetworkAddress().equals(newFingerEntry.getNetworkAddress())) {
 							//Key not allowed message
 							answer = new DuplicateNodeIdMessage(identity.getNetworkAddress(), join_msg.getFromIp(),join_msg.getKey());
 						}
 					}
 					else {
 						synchronized(this) {
 							if(blockJoinFor != null) {
 								//Send busy message
 								answer = new JoinBusyMessage(identity.getNetworkAddress(),join_msg.getOriginatorAddress());
 							}
 							else {
 								//Prepare answer
 								answer = new JoinResponseMessage(identity.getNetworkAddress(), join_msg.getOriginatorAddress(),join_msg.getKey(), successor.getNetworkAddress(),successor.getNodeID(),identity.getNodeID());
 								blockJoinFor = newFingerEntry;
 								
 								//Start timer for node unblocking
 								timer.schedule(new TimerTask() {
 
 									@Override
 									public void run() {
 										triggerUnblockJoinBlock();
 									}
 									
 								}, JOIN_BLOCK_PERIOD);
 							}
 						}
 						
 						//Send
 						sendMessage(answer);
 					}
 				}
 				else {
 					//Forward to the best fitting predecessor
 					answer = new JoinMessage(identity.getNetworkAddress(),getPredecessor(join_msg.getKey()).getNetworkAddress(), join_msg.getOriginatorAddress(), join_msg.getKey());
 					sendMessage(answer);
 				}
 				break;
 			case Message.JOIN_ACK:
 				JoinAckMessage jam = (JoinAckMessage)message;
 				
 				//Skip if not blocked or it is a faked message
 				if(blockJoinFor != null && jam.getJoinKey().equals(blockJoinFor.getNodeID())) {
 					//Notify everybody of the new node
 					sendBroadcast(new NotifyJoinBroadcastMessage(null,null,null,null,blockJoinFor.getNetworkAddress(),blockJoinFor.getNodeID()),identity.getNodeID(),identity.getNodeID().sub(1));
 					
 					//if I am still my own predecessor, make the new node the predecessor (startup)
 					synchronized (finger) {
 						if(identity.equals(predecessor)) {
 							predecessor = new FingerEntry(jam.getJoinKey(), jam.getFromIp());
 						}
 					}
 					
 					//Set successor to new node and update finger-table with old successor
 					FingerEntry old_successor;
 					
 					synchronized(finger) {
 						old_successor = successor;
 						successor = blockJoinFor;
 					}
 					
 					//TODO REMOVE
 					if(!old_successor.equals(identity)) {
 						fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_REMOVE_WORSE, identity, old_successor);
 						fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD_BETTER, identity, successor);
 					}
 					else {
 						fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, successor);
 					}
 					//Check if we can use the old successor as finger
 					updateFingerTableEntry(old_successor);
 	
 					//Repair finger count
 					// checkFingerTable();
 					
 					//unblock
 					blockJoinFor = null;
 				}
 				
 				break;
 			case Message.JOIN_BUSY:
 				//TODO react on this !?
 				break;
 			case Message.JOIN_RESPONSE:
 				JoinResponseMessage jrm = (JoinResponseMessage) message;
 
 				//Ignore JOIN_RESPONSE message if the node is already connected!
 				if(!connected) {
 					if(jrm.getJoinKey().equals(identity.getNodeID())) {
 						//Add finger
 						FingerEntry newSuccessor = new FingerEntry(jrm.getSuccessor(), jrm.getSuccessorAddress());
 						FingerEntry newPredecessor = new FingerEntry(jrm.getPredecessor(),jrm.getFromIp());
 						
 						synchronized (finger) {
 							successor = newSuccessor;
 							predecessor = newPredecessor;
 						}
 						
 						//TODO remove
 						fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, successor);						
 						connected = true;
 
 						//Inform the node that we got the message
 						sendMessage(new JoinAckMessage(identity.getNetworkAddress(), jrm.getFromIp(), identity.getNodeID()));
 						
 						//Check
 						updateFingerTableEntry(new FingerEntry(jrm.getPredecessor(),jrm.getFromIp()));
 						
 						//Create finger table the first time
 						//buildFingerTable();
 						//checkFingerTable();
 					}
 					else {
 						//Ignore this because the key does not match!!!
 						//TODO react on this
 					}
 				}
 				
 				break;
 			case Message.DUPLICATE_NODE_ID:
 				DuplicateNodeIdMessage dupMsg = (DuplicateNodeIdMessage)message;
 				if(dupMsg.getDuplicateKey().equals(identity.getNodeID())) {
 					//it is okay, a join what I send before has reached my predecessor
 				} else {
 					//If the node is not connected allow the change of the identity
 					//Check the duplicate id also
 					if(!connected && dupMsg.getDuplicateKey().equals(identity.getNodeID())) {
 						//TODO what shall we do here?????
 						assert(false);
 					}
 				}
 
 				break;
 			case Message.BROADCAST:
 				BroadcastMessage bcast_msg = (BroadcastMessage)message;
 				
 				//Forward broadcast
 				sendBroadcast(bcast_msg,bcast_msg.getStartKey(),bcast_msg.getEndKey());
 				
 				//Process broadcast
 				handleMessage(bcast_msg.extractMessage());
 				break;
 			case Message.KEEPALIVE:
 				KeepAliveMessage keep_alive_msg = (KeepAliveMessage)message;
 				
 				//Reset timer
 				resetKeepAliveTimer();
 
 				//Handle keep-alive message
 				updateFingerTableEntry(new FingerEntry(keep_alive_msg.getAdvertisedID(),keep_alive_msg.getAdvertisedNetworkAddress()));
 				
 				break;
 			case Message.NODE_JOIN_NOTIFY:
 				NotifyJoinMessage njm = (NotifyJoinMessage)message;
 				FingerEntry newFinger;
 				
 				//Check if this node can use the newly added node
 				//for the finger table
 				newFinger = new FingerEntry(njm.getHash(),njm.getNetworkAddress());
 				updateFingerTableEntry(newFinger);
 				
 				//Send advertisement if we probably are a finger of the joining node
 				int log2 = NodeID.logTwoFloor(predecessor.getNodeID().sub(newFinger.getNodeID()));
 				log2 = NodeID.logTwoFloor(identity.getNodeID().sub(newFinger.getNodeID()));
 				
 				if(NodeID.logTwoFloor(predecessor.getNodeID().sub(newFinger.getNodeID())) < NodeID.logTwoFloor(identity.getNodeID().sub(newFinger.getNodeID()))) {
 					//Send advertisement
 					sendMessage(new KeepAliveMessage(identity.getNetworkAddress(),newFinger.getNetworkAddress(),identity.getNodeID(),identity.getNetworkAddress()));
 				}
 				
 				//Check finger table
 				//checkFingerTable();
 				
 				break;
 			case Message.NODE_LEAVE_NOTIFY:
 				NotifyLeaveMessage nlm = (NotifyLeaveMessage)message;
 				
 				//Remove finger from finger table and exchange by successor
 				removeFingerTableEntry(new FingerEntry(nlm.getHash(),nlm.getNetworkAddress()),new FingerEntry(nlm.getSuccessorHash(),nlm.getSuccessorNetworkAddress()));
 				
 			default:
 				//TODO Throw a Exception for a unsupported message?!
 		}
 	}
 	
 	@Override
 	public void run() {
 		//Connect DHT node
 		while(connected == false) {
 			//Try to connect to DHT
 			sendMessage(new JoinMessage(identity.getNetworkAddress(),bootstrapAddress,identity.getNetworkAddress(),identity.getNodeID()));
 			
 			try {
 				//Wait for connection and try again
 				Thread.sleep(5000);
 			}
 			catch (InterruptedException e) {
 				//Exit thread
 				break;
 			}
 		}
 		
 		//Connected => Set Keep alive timer
 		resetKeepAliveTimer();
 		
 		//Wait for nothing
 		while(true) {
 			try {
 				//Wait for connection and try again
 				Thread.sleep(5000);
 			}
 			catch (InterruptedException e) {
 				//Exit thread
 				break;
 			}
 		}
 	}
 	
 	
 	public FingerEntry getIdentity() {
 		return identity;
 	}
 	
 	private FingerEntry getPredecessor(NodeID nodeID) {
 		FingerEntry hash = new FingerEntry(nodeID,null);
 		FingerEntry result;
 
 		//Add identity and successor to the finger-table - IMPORTANT: remove them before return
 		synchronized(finger) {
 			finger.put(identity, identity);
 			finger.put(successor, successor);
 			finger.put(predecessor, predecessor);
 
 			//Find predecessor of a node
 			result = finger.lowerKey(hash);
 			if(result == null) {
 				//There is no lower key in the finger tree
 				result = finger.lastKey();
 			}
 			
 			//Remove identity and successor from the finger-table
 			finger.remove(identity);
 			finger.remove(successor);
 			finger.remove(predecessor);
 		}
 		
 		return result;
 	}
 	
 	public FingerEntry getSuccessor(NodeID nodeID) {
 		FingerEntry hash = new FingerEntry(nodeID,null);
 		FingerEntry result;
 
 		synchronized(finger) {
 			//Add identity and successor to the finger-table - IMPORTANT: remove them before return
 			finger.put(identity, identity);
 			finger.put(successor, successor);
 			finger.put(predecessor, predecessor);
 
 			//Get successor of us
 			result = finger.higherKey(hash);
 			if(result == null) { 
 				//There is no higher key in the finger tree
 				result = finger.firstKey();
 			}
 			
 			//Remove identity and successor from the finger-table
 			finger.remove(identity);
 			finger.remove(successor);
 			finger.remove(predecessor);
 		}
 		
 		return result;
 	}
 	
 	private void setIdentity(byte[] hash) {
 		//Set identity
 		identity = new FingerEntry(new NodeID(hash),communication.getLocalIp());
 		
 		//(Re-)initialize finger table
 		//Always add ourselves to the finger table
 		//finger = new TreeMap<FingerEntry,FingerEntry>();
 		//finger.put(identity,identity);
 	}
 	
 	//Check if new node can be inserted into finger table
 	public void updateFingerTableEntry(FingerEntry newFinger) {
 		FingerEntry suc;
 		NodeID hash_finger;
 		NodeID hash_suc;
 		NodeID hash_log2;
 		int log2floor;
 		
 		//Check for dont's
 		synchronized(finger) {
 			if(newFinger.equals(identity)) return;
 			if(newFinger.equals(successor)) return;
 			if(newFinger.equals(predecessor)) return;
 			if(finger.containsKey(newFinger)) return;
 		}
 		
 		
 		if(getPredecessor(newFinger.getNodeID()).getNodeID().equals(predecessor.getNodeID())) {
 			FingerEntry oldPredecessor = predecessor;
 
 			//It is a better predecessor
 			synchronized (finger) {
 				predecessor = newFinger;
 			}
 
 			updateFingerTableEntry(oldPredecessor);
 		}
 		
 		//1 - Rotate hash to the "origin"
 		//2 - Then get the logarithm of base 2, rounded down (floor)
 		//3 - Calculate the new hash
 		
 		hash_finger = newFinger.getNodeID().sub(identity.getNodeID());
 		log2floor = NodeID.logTwoFloor(hash_finger);
 		hash_log2 = NodeID.powerOfTwo(log2floor);
 
 		//Get previous successor - Shift to original position first
 		synchronized(finger) {
 			suc = getSuccessor(hash_log2.add(identity.getNodeID()));
 		}
 		hash_suc = suc.getNodeID().sub(identity.getNodeID());
 		
 		if(suc.equals(identity)) {
 			//In this case, there is no successor => just add the new finger
 			synchronized(finger) {
 				finger.put(newFinger,newFinger);
 			}
 			
 			//Fire event
 			fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, newFinger);
 		}
 		//Check if the new finger is smaller than the successor
 		else if(hash_finger.compareTo(hash_suc) < 0) {
 			//Also add the new node in this case...
 			synchronized(finger) {
 				finger.put(newFinger,newFinger);
 			}
 
 			//...but also check if the successor was the old successor
 			//and, if so, remove it
 			//Old successor means, that it is between [log2floor,log2floor + 1)
 			if(log2floor == ((NodeID.ADDRESS_SIZE * 8) - 1) || hash_suc.compareTo(NodeID.powerOfTwo(log2floor + 1)) < 0) {
 				synchronized(finger) {
 					finger.remove(suc);
 				}
 				
 				//Fire events
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_REMOVE_WORSE, identity, suc);
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD_BETTER, identity, newFinger);
 			}
 			else {
 				//Only fire ADD event, because nothing was removed in change
 				fireFingerChangeEvent(FingerChangeListener.FINGER_CHANGE_ADD, identity, newFinger);
 			}
 		}
 	}
 	
 	public void removeFingerTableEntry(FingerEntry remove,FingerEntry suc) {
 		//TODO create this :-)
 	}
 	
 	public void buildFingerTable() {
 		//TODO and this too
 	}
 	
 	//TODO figure out where and how to use
 	private void checkFingerTable() {
 	}
 	
 	private void sendMessage(Message message) {
 		try {
 			communication.sendMessage(message);
 		} catch(DestinationNotReachableException e) {
 			//TODO handle this for unicast
 		}
 	}
 	
 	private void sendBroadcast(BroadcastMessage bcast_msg, NodeID startKey,NodeID endKey) {
 		FingerEntry suc,next;
 		String from,to;
 		NodeID newStartKey,newEndKey;
 		BroadcastMessage new_bcast_msg;
 		
 		//Don't do...
 		if(!connected) {
 			System.out.println("NOT CONNECTED " + identity.getNetworkAddress());
 			return;
 		}
 		
 		//Prepare packet
 		from = identity.getNetworkAddress();
 
 		//Get first successor
 		suc = getSuccessor(identity.getNodeID());
 		if(suc.equals(identity)) return;
 		next = getSuccessor(suc.getNodeID());
 
 		//For each finger
 		do {
 			//Check and set range
 			if(suc.getNodeID().between(startKey,endKey)) {
 				newStartKey = suc.getNodeID();
 				
 				//Set endKey to next NodeID or the end of the range, whatever is smallest
 				if(next.getNodeID().between(startKey,endKey)) {
 					newEndKey = next.getNodeID().sub(1);
 				}
 				else {
 					newEndKey = endKey;
 				}
 				
 				//Send message
 				to = suc.getNetworkAddress();
 				new_bcast_msg = bcast_msg.cloneWithNewAddresses(from, to,newStartKey,newEndKey);
 				try {
 					communication.sendMessage(new_bcast_msg);
 				} catch(DestinationNotReachableException e) {
 					//TODO handle this for Broadcast!
 				}
 			}
 
 			//Move to next range
 			suc = next;
 			next = getSuccessor(suc.getNodeID());
 		} while(!suc.equals(identity));
 	}
 	
 	//TODO for DEBUG
 	private void fireFingerChangeEvent(int eventType,FingerEntry node,FingerEntry finger) {
 		communication.fireFingerChangeEvent(eventType,node,finger);
 	}
 	
 	//TODO for DEBUG
 	//Remove later
 	public TreeMap<FingerEntry,FingerEntry> getFingerTable() {
 		TreeMap<FingerEntry,FingerEntry> newMap;
 		
 		//We need to clone the map and synchronize this operation!
 		synchronized(finger) {
 			newMap = new TreeMap<FingerEntry,FingerEntry>(finger);
 			newMap.put(successor, successor);
 			newMap.put(identity,identity);
 			newMap.put(predecessor,predecessor);
 		}
 		
 		return newMap;
 	}
 	
 	private void triggerKeepAliveTimer() {
 		KeepAliveBroadcastMessage msg;
 		
 		//Fire event at network layer
 		//TODO remove! only for debugging
 		communication.fireKeepAliveEvent(identity.getNodeID(),identity.getNetworkAddress());
 		
 		//Send broadcast
 		msg = new KeepAliveBroadcastMessage(null,null,null,null,identity.getNodeID(),identity.getNetworkAddress());
 		sendBroadcast(msg, identity.getNodeID(),identity.getNodeID().sub(1));
 		
 		//Reset time
 		resetKeepAliveTimer();
 	}
 
 	private void resetKeepAliveTimer() {
 		int time = KEEP_ALIVE_PERIOD + new Random().nextInt(KEEP_ALIVE_RANDOM_PERIOD);
 		
 		//Cancel and reschedule timer
 		//TODO create a new Timer is not good!!!!!!!!! It every time creates a new Thread! One Timer thread should be enough!
 		if(keepAlive != null) keepAlive.cancel();
 		keepAlive = new TimerTask() {
 
 			@Override
 			public void run() {
 				//Trigger keep alive
 				//triggerKeepAliveTimer();
 			}
 		};
 		timer.schedule(keepAlive, time);
 	}
 	
 	//TODO remove debug function
 	public boolean getStateConnected() {
 		return connected;
 	}
 
 	//TODO remove debug function
 	public FingerEntry getStateBlockJoinFor() {
 		return blockJoinFor;
 	}
 	
 	private void triggerUnblockJoinBlock() {
 		//Unblock node if new successor did not answer
 		synchronized(this) {
 			if(blockJoinFor != null) {
 				blockJoinFor = null;
 			}
 		}
 	}
 	
 }
