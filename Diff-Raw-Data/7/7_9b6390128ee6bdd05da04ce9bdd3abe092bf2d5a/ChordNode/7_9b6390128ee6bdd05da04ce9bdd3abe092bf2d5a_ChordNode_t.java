 package chord;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import sockets_layer.IO;
 import dht_event.DHTEvent;
 import dht_event.DHTEvent.EventType;
 import dht_event.DHTEventHandler;
 import dht_event.FoundTableEvent;
 import dht_event.JoinEvent;
 import dht_event.LookupEvent;
 import dht_event.LookupTableEvent;
 import dht_event.StabilizeSEvent;
 
 
 public class ChordNode implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 6648930404036643565L;
 	public static final int PORT = 1884;
 	private String id;
 	private ChordKey key;
 	private ChordNode predecessor;
 	private ChordNode successor;
 	private ChordFingerTable fingerTable;
 
 	public ChordNode(String nodeId) {
 		this.id = nodeId;
 		this.key = new ChordKey(Math.abs(id.hashCode()));
 		this.fingerTable = new ChordFingerTable(this);
 		predecessor = null;
 		successor = this;
 	}
 
 	public void join(ChordNode destNode) {
 		predecessor = null;
 		//successor = destNode.findSuccessor(this.getKey());
 		destNode.findSuccessor(this.getKey(), EventType.JOIN, 0, null);
 		
 		if(this != successor){
 			DHTEvent stabilizeEventS = new StabilizeSEvent(this);
 			try {
 			IO comm = new IO(new Socket(successor.getId(), PORT));
 			comm.sendEvent(stabilizeEventS);
 			}catch (Exception e){
 				e.printStackTrace();
 			}
 		}
 
 		
 		destNode.updateTable(successor);
 	}
 	
 	public void findSuccessor(ChordKey target_key, EventType type, int position, String ip) {
 		
 		try{
 			
 			ChordNode node = largestPrecedingNode(target_key);
 			DHTEvent event = null; 
 			if (type == EventType.LOOKUP){
 				event = new LookupEvent(this.getKey(), node.getKey());
 			} else if (type == EventType.JOIN){
				event = new JoinEvent(target_key, node.getKey(), ip); // changed
 			}else {
 				event = new LookupTableEvent(target_key, node, position, ip);
 			}
 			if (node == this) {
 				//return successor.findSuccessor(target_key);			
 				Socket socket = new Socket(successor.getId(), PORT);
 				IO communicator = new IO(socket);
 				communicator.sendEvent(event);
 			} else{
 				//return node.findSuccessor(target_key);
 				Socket socket = new Socket(node.getId(), PORT);
 				IO communicator = new IO(socket);
 				communicator.sendEvent(event);
 			}
 			
 		}catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/*
 	public void findSuccessor(ChordKey target_key) {
 		try{
 //			// if node is its own successor return itself
 //			if (this == successor) {
 //				//return this;
 ////				Socket socket = new Socket(this.getId(), PORT);
 ////				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
 ////				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
 ////				IO communicator = new IO(socket,ois,oos);
 //			}
 //			// if target is between this and successor then return successor or they are equal
 //			if (target_key.isBetween(this.key, successor.getKey()) || target_key.getKey() == successor.getKey().getKey()) {
 //				return successor;
 //			} else { // otherwise get the largest node st the key is in range
 				ChordNode node = largestPrecedingNode(target_key);
 				if (node == this) {
 					//return successor.findSuccessor(target_key);
 					Socket socket = new Socket(successor.getId(), PORT);
 					IO communicator = new IO(socket);
 					communicator.sendMessage("lookup");
 					communicator.sendKey(target_key);
 				} else{
 					//return node.findSuccessor(target_key);
 					Socket socket = new Socket(node.getId(), PORT);
 					IO communicator = new IO(socket);
 					communicator.sendMessage("lookup");
 					communicator.sendKey(target_key);
 				}
 			//}
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	*/
 	
 	public void lookupT(ChordKey key, int position, String ip){
 		this.findSuccessor(key, EventType.LOOKUP_TABLE, position, ip);
 	}
 	
 	public void lookupL(ChordKey key){
 		this.findSuccessor(key, EventType.LOOKUP, 0, null);
 	}
 	
 	public void lookupN(ChordKey key, String ip){
 		this.findSuccessor(key, EventType.JOIN, 0, ip);
 	}
 	
 	public void lookup(File file){
 		lookupL(new ChordKey(file.hashCode()));
 	}
 
 	public void lookup(String filename){
 		lookupL(new ChordKey(filename.hashCode()));
 	}
 	
 	private ChordNode largestPrecedingNode(ChordKey target_key) {
 		for (int i = ChordHash.TABLE_SIZE - 1; i >= 0; i--) {
 			ChordTableEntry entry = fingerTable.getEntry(i);
 			ChordKey table_key = entry.getNode().getKey();
 			if (table_key.isBetween(this.getKey(), target_key)) {
 				return entry.getNode();
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Verifies the successor, and tells the successor about this node. Should be called periodically.
 	 */
 	public void stabilize() {
 		this.predecessor = successor.predecessor;
 		if (this.predecessor == null) {
 			this.predecessor = successor;
 		}
 		this.successor.predecessor = this;
 		this.predecessor.successor = this;
 		
 		
 		
 	}
 
 	/**
 	 * Refreshes finger table entries.
 	 */
 	public void updateTable(ChordNode node) {
 //		for (int i = 0; i < fingerTable.size(); i++) {
 //			ChordTableEntry entry = fingerTable.getEntry(i);
 //			ChordNode corr = findNextTableEntry(entry.getPosition(), node, node);
 //			entry.setNode(corr);
 //		}
 		for (int i = 0; i < fingerTable.size(); ++i){
 			try{
 				ChordTableEntry entry = fingerTable.getEntry(i);
 				DHTEvent LTevent = new LookupTableEvent(new ChordKey(entry.getPosition()), this, i, this.id);
 				//System.out.println("WHICH ONE: "+ getId()+" OR "+successor.getId());
 				IO comm = new IO(new Socket(successor.getId(), PORT));
 				comm.sendEvent(LTevent);
 				System.out.println("Sent Lookup Table event");
				//Thread.sleep(1000);
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 		}
 	}
 
 	private ChordNode findNextTableEntry(int pos, ChordNode node, ChordNode orig) {
 		ChordKey i = new ChordKey(pos);
 		if (i.isBetween(orig.getKey(), node.getKey()) || pos == node.getKey().getKey()) {
 			return node;
 		} else {
 			ChordNode corr = findNextTableEntry(pos, node.successor, orig);
 			return corr;
 		}
 
 	}
 
 	public void printFingerTable() {
 		System.out.println("=======================================================");
 		System.out.println("FingerTable: " + this + " --> position: " + key.getKey());
 		System.out.println("-------------------------------------------------------");
 		System.out.println("Predecessor: " + predecessor);
 		System.out.println("Successor: " + successor);
 		System.out.println("-------------------------------------------------------");
 		System.out.println("i    position          id");
 		System.out.println("-------------------------------------------------------");
 		for (int i = 0; i < fingerTable.size(); i++) {
 			ChordTableEntry entry = fingerTable.getEntry(i);
 			System.out.println(i + "\t " + entry.getPosition() + "\t    " + entry.getNode());
 		}
 		System.out.println("=======================================================");
 	}
 
 	public String toString() {
 		return id;
 	}
 
 	public ChordKey getKey() {
 		return key;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public ChordNode getPredecessor() {
 		return predecessor;
 	}
 	
 	public void setPredecessor(ChordNode node){
 		predecessor = node;
 	}
 
 	public ChordNode getSuccessor() {
 		return successor;
 	}
 	
 	public ChordFingerTable getTable(){
 		return fingerTable;
 	}
 	
 	/*
 	private static void getCmd(Socket socket, ChordNode server) throws Exception {
 		try {
 			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
 			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
 			IO communicator = new IO(socket);
 			Object o = ois.readObject();
 			if (o instanceof String) {
 				if (o.equals("lookup")) {
 					ChordKey key = (ChordKey) ois.readObject();
 
 					if(server == server.successor) {
 						communicator.sendMessage("found");
 						communicator.sendMessage(server.id);
 					} else if(key.isBetween(server.key, server.successor.getKey()) || key.getKey() == server.successor.getKey().getKey()) {
 						communicator.sendMessage("found");
 						communicator.sendMessage(server.successor.getId());
 					} else {
 						server.lookup(key);
 					}
 				} else if (o.equals("store")){
 					communicator.receiveFile();
 				} else if (o.equals("found")) {
 					String location = (String) ois.readObject();
 					communicator.sendMessage(location);
 				}else {
 					throw new Exception(o +" is not a valid command. Commands are \"store\" and \"get\"");
 				}
 			} else {
 				throw new Exception("Expected a String (the command) from stream but got: " + o.getClass());
 			}
 		} finally {
 			
 		}
 	}
 	*/
 
 	public static void main(String[] args) {
 //		System.out.println("okra: "+ new ChordKey("129.82.47.229").getKey());
 //		System.out.println("shavano: "+ new ChordKey("129.82.47.175").getKey());
 //		System.out.println("filbert: "+ new ChordKey("129.82.47.60").getKey());
 		ServerSocket serverSocket = null;
 		ChordNode node = null;
 		try {
 			String ip = InetAddress.getLocalHost().toString();
 			node = new ChordNode(ip.substring(ip.indexOf('/')+1));
 			System.out.println(ip.substring(ip.indexOf('/')+1));
 			serverSocket = new ServerSocket(PORT);
 			boolean joining = (args.length == 1) ? true : false;
 			System.out.println("Created Server socket. Listening..");
 			boolean first = true;
 			while (true) {
 				if(joining) {
 					new Thread(new ChordJoiningThread(args[0], node)).start();
 					new Thread(new ChordStabilizeThread(node)).start();
 					joining = false;
 					//node.updateTable(node);
 				} 
 				if (first && !joining){ // for first node only set pred and succ to itself for stabilzaitons
 					node.setPredecessor(node);
 					node.setSuccessor(node);
 					first = false;
 				}
 				node.printFingerTable();
 				Socket s = serverSocket.accept();
 				System.out.println("Socket accepted");
				System.out.println("NUM OF SOCKETS OPEN: "+ IO.numOfSockets);
 				new Thread(new DHTEventHandler(s, node)).start();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setSuccessor(ChordNode node) {
 		successor = node;
 	}
 }
