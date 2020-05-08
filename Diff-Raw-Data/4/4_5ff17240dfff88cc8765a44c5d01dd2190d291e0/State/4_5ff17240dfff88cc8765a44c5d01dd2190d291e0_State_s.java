 package cs555.dht.state;
 
 import cs555.dht.node.PeerNode;
 import cs555.dht.peer.*;
 import cs555.dht.utilities.Constants;
 import cs555.dht.wireformats.*;
 import cs555.dht.wireformats.PredessesorRequest;
 
 public class State {
 	public Peer successor;
 	public Peer predecessor;
 	int thisID;
 	PeerNode myself;
 
 	FingerTable fingerTable;
 
 	//================================================================================
 	// Constructors
 	//================================================================================
 	public State(int h, PeerNode p){
 		thisID = h;
 		myself = p;
 
 		Peer thisPeer = new Peer(myself.hostname, myself.port, thisID);
 		successor = thisPeer;
 		predecessor = thisPeer;
 		
 		fingerTable = new FingerTable(thisID,myself);
 	}
 
 
 	//================================================================================
 	// State manipulation
 	//================================================================================
 	// Check successor candidate
 	public boolean shouldAddNewSuccessor(Peer p, boolean force) {
 
 		if (force) {
 			return true;
 		}
 		
 		if (successor != null) {
 			if (successor.id == p.id) {
 				return false;
 			}
 			
 			if (p.id == thisID) {
 				return false;
 			}
 		}
 
 		if ((successor == null) || (successor.id == thisID)) {
 			return true;
 		}
 
 		// Same side of ring
 		if ((p.id < successor.id) && (p.id > thisID)) {
 			return true;
 		}
 
 		// left side of gap
 		if ((p.id > thisID) && (p.id < (Math.pow(2, Constants.Id_Space)))) {
 			return true;
 		}
 
 		// Right side of gap
 		if ((p.id >= 0) && (p.id < successor.id)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public void addSucessor(Peer p, boolean force) {
 		if (!shouldAddNewSuccessor(p, force)) {
 			return;
 		}
 		
 		successor = p;
 
 		fingerTable.fillTableWith(successor);
 
 		// Send predecesor request if, we're not the only one
 		if (successor.id != thisID) {
 			// Tell our new successor that we're it's predecessor
 			PredessesorRequest req = new PredessesorRequest(myself.hostname, myself.port, myself.id);
 			myself.sendPredessessorRequest(successor, req);
 
 			// Add successor to FT[0]
 			fingerTable.addEntry(0, successor);
 			// Our successor changed, update finger table
 			fingerTable.buildFingerTable();
 		}
 		
 		else {
 			myself.printDiagnostics();
 		}
 
 	}
 
 	// check predeccessor candidate
 	public boolean shouldAddNewPredecessor(Peer p, boolean force) {
 
 		if (force) {
 			return true;
 		}
 		
 		if (p.id == thisID) {
 			return false;
 		}
 		
 		if (predecessor != null) {
 			if (predecessor.id == p.id) {
 				return false;
 			}
 		}
 
 		if ((predecessor == null) || (predecessor.id == thisID)) {
 			return true;
 		}
 
 		if (itemIsMine(p.id)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public void addPredecessor(Peer p, boolean force) {
 
 		if (!shouldAddNewPredecessor(p, force)) {		
 			return;
 		}
 
 		predecessor = p;
 
 		// If our successor is ourself, and p as our successor as well
 		if (successor.id == thisID) {
 			addSucessor(p, false);
 		}
 		
 		// Move relevant data items 
 		myself.transferDataToPredesessor();
 	}
 
 	// Set all values to self
 	public void firstToArrive() {
 		Peer thisPeer = new Peer(myself.hostname, myself.port, myself.id);
 		addSucessor(thisPeer, true);
 		addPredecessor(thisPeer,true);
 
 		// Add thisPeer as all values in FT
 		fingerTable.fillTableWith(thisPeer);
 		myself.printDiagnostics();
 	}
 
 	//================================================================================
 	// Update State
 	//================================================================================
 	public void update() {
 		fingerTable.buildFingerTable();
 	}
 
 	// Decide where to put this peer in Finger Table
 	public void parseState(LookupRequest l) {
 		Peer peer = new Peer(l.hostName, l.port, l.id);
 
 		// If it's our first entry getting back to us, add it as our sucessor
 		if (l.ftEntry == 0) {
 			addSucessor(peer, false);
 		}
 
 		fingerTable.addEntry(l.ftEntry, peer);
 
 	}
 
 	//================================================================================
 	// Resolving
 	//================================================================================
 	public boolean itemIsMine(int h) {
 		
 		// Same side of ring
 		if ((h > predecessor.id) && h <= thisID) {
 			return true;
 		}
 
 		// left side of gap
 		if ((h > predecessor.id) && (h < (Math.pow(2, Constants.Id_Space)))) {
 
 			if (predecessor.id < thisID) {
 				return false;
 			}
 
 			return true;
 		}
 
 		// right side of gap
 		if ((h <= thisID) && (thisID < predecessor.id)) {
 			return true;
 		}
 
 		// If We're currently the only process in the system 
 		if ((thisID == predecessor.id) && (thisID == successor.id)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	// Get the next closest peer to this id from finger table
 	public Peer getNexClosestPeer(int h) {
 		return fingerTable.getNextClosest(h);
 	}
 
 	
 	//================================================================================
 	// Error handling
 	//================================================================================
 	public Peer getNextSuccessor() {
 		Peer p = fingerTable.getNextSuccessor();
 		addPredecessor(p, true);
 		return p;
 	}
 	
 	//================================================================================
 	// House keeping
 	//================================================================================
 	public String toString(){
 		String s = "";
 
 		s += "Diagnostics at node: " + thisID + "\n";
 		s += "Predesessor: " + predecessor.id + "\n";
 		s += "Sucessor: " + successor.id + "\n";
 		s += "\nFT: \n" + fingerTable.toString();
 
 		return s;
 	}
 }
