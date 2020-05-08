 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class TransactionLayer {
 
 	public final static int MASTER_NODE = 0;
 	
 	private DistNode n;
 	public ReliableInOrderMsgLayer RIOLayer;
 	private PaxosLayer paxos;
 	
 	public Map<String, File> cache;
 	
 	//CLIENT FIELDS
 	private int lastTXNnum;
 	private Transaction txn;
 	private int leader;
 	
 	//SERVER FIELDS
 	/**
 	 * key = addr of node trying to commit
 	 * value = commit status
 	 */
 	private Map<Integer, Commit> waitingQueue;
 	
 	/**
 	 * Stores the seqNums received from clients so when paxos finishes
 	 * the server can respond with the correct seqNum
 	 * txnID -> seqNum
 	 */
 	private Map<Integer, Integer> paxosQueue;
 
 	private TimeoutManager timeout;
 	
 	/**
 	 * txnID -> true if txn committed, false if it aborted
 	 */
 	private Map<Integer, Boolean> txnLog;
 
 	private boolean isElected;
 	
 	public TransactionLayer(RIONode n, ReliableInOrderMsgLayer RIOLayer){
 		this.cache = new HashMap<String, File>();
 		this.n = (DistNode)n;
 		this.RIOLayer = RIOLayer;
 		this.lastTXNnum = this.n.addr;
 		this.timeout = new TimeoutManager(12, this.n, this);
 	}
 	
 	public String toString(){
 		return this.paxos.toString();
 	}
 	
 	public void start(){
 		if(this.n.isMaster()){
 			this.paxos = new PaxosLayer(this, this.n, true);
 			this.paxos.start();
 			this.waitingQueue = new HashMap<Integer, Commit>();
 			this.txnLog = new HashMap<Integer, Boolean>();
 			this.paxosQueue = new HashMap<Integer, Integer>();
 		}else{
 			this.paxos = new PaxosLayer(this, this.n, false);
 			this.leader = this.paxos.electLeader();
 		}
 	}
 	
 	public void initializeTimeoutSeqNums(Map<Integer, Integer> seqNums){
 		this.timeout.initializeSeqNums(seqNums);
 	}
 	
 	public void initializeLastTxnNumber(int txnID){
 		this.lastTXNnum = txnID;
 	}
 	
 	public void initializeLog(Map<Integer, Boolean> txnLog){
 		this.txnLog.putAll(txnLog);
 	}
 	
 	public boolean hasElection(){
 		if(this.leader == -1){
 			if(!this.paxos.hasElection())
 				this.leader = this.paxos.electLeader();
 			return true;
 		}else
 			return false;
 	}
 	
 	public void elect(int newLeader, int instanceNum){
 		this.isElected = true;
 		this.leader = newLeader;
 		this.n.printData("Node " + this.n.addr + ": Success: Node: " + this.leader + " elected as leader.");
 		this.executeCommandQueue();
 	}
 	
 	public void send(int dest, int protocol, byte[] payload) {
 		TXNPacket pkt = new TXNPacket(protocol, this.timeout.nextSeqNum(dest), payload);
 		int p = pkt.getProtocol();
 		if(p == TXNProtocol.PAXOS){
 			PaxosPacket p1 = PaxosPacket.unpack(payload);
 			if(p1.getProtocol() == PaxosProtocol.ELECT)
 				this.timeout.createTimeoutListener(dest, pkt);
 		}else if(p == TXNProtocol.WF || p == TXNProtocol.WQ || p == TXNProtocol.CREATE || 
 				(!this.n.isMaster() && (p == TXNProtocol.ABORT || p == TXNProtocol.COMMIT || p == TXNProtocol.START)))
 			this.timeout.createTimeoutListener(dest, pkt);
 		this.RIOLayer.sendRIO(dest, Protocol.TXN, pkt.pack());
 	}
 	
 	public void rtn(int dest, int protocol, int seqNum, byte[] payload) {
 		TXNPacket pkt = new TXNPacket(protocol, seqNum, payload);
 		this.RIOLayer.sendRIO(dest, Protocol.RTN, pkt.pack());
 	}
 	
 	/**
 	 * Starts or stops a heartbeat to a given node
 	 * @param dest node to start or stop the heartbeat
 	 * @param heartbeat true = start, false = stop
 	 */
 	public void setHB(int dest, boolean heartbeat){
 		this.RIOLayer.setHB(dest, heartbeat);
 	}
 	
 	public void onReceive(int from, byte[] payload) {
 		TXNPacket packet = TXNPacket.unpack(payload);
 		if(packet.getProtocol() == TXNProtocol.PAXOS){
 			PaxosPacket pkt = PaxosPacket.unpack(packet.getPayload());
 			if(!this.n.isMaster() && pkt.getProtocol() == PaxosProtocol.ELECT)
 				this.timeout.onRtn(from, packet.getSeqNum());
 			this.paxos.onReceive(from, packet.getSeqNum(), packet.getPayload());
 		}else if(this.n.isMaster()){
 			masterReceive(from, packet);
 		}else
 			slaveReceive(from, packet);
 	}
 	
 	/**
 	 * Called when a message times out on the RIOLayer
 	 * @param dest
 	 * @param payload
 	 */
 	public void onRIOTimeout(int dest, byte[] payload){
 		TXNPacket pkt = TXNPacket.unpack(payload);
 		if(pkt.getProtocol() == TXNProtocol.HB || this.timeout.onRtn(dest, pkt.getSeqNum())){
 			if(this.n.isMaster()){ //This is the server
 				if(pkt.getProtocol() == TXNProtocol.HB){
 					//This is a heartbeat that timed out, meaning either a client has crashed or we assume it has.
 					//We must tell all commits waiting on this client to abort and flag this client as crashed, so if
 					//it didn't and tries to commit, it will have to abort.
 					for(String fileName : this.cache.keySet()){
 						MasterFile f = (MasterFile)this.cache.get(fileName);
 						f.abort(dest);
 					}
 					this.abortWaitingTxns(dest);
 				}else if(pkt.getProtocol() == TXNProtocol.WF){ 
 					//a write forward timed out. We should check to see if we are waiting on any other WF, if not, send a response to the requester.
 					String fileName = Utility.byteArrayToString(pkt.getPayload());
 					MasterFile f = (MasterFile)this.getFileFromCache(fileName);
 					f.changePermissions(dest, File.INV);
 					this.returnWaiting(f);
 				}else
 					this.n.printError(DistNode.buildErrorString(dest, this.n.addr, pkt.getProtocol(), Utility.byteArrayToString(pkt.getPayload()), Error.ERR_20));
 			}else{ //this is the client and we should just print out the message
 				if(pkt.getProtocol() == TXNProtocol.START){
 					this.txn = null;
 					this.n.printError("Node " + this.n.addr + ": Error: Couldn't start transation. Server " + dest + " returned error code " + Error.ERROR_STRINGS[Error.ERR_20]);
 
 					this.leader = this.paxos.electLeader();
 				}else if(pkt.getProtocol() == TXNProtocol.COMMIT){
 					CommitPacket p = CommitPacket.unpack(pkt.getPayload(), this.cache);
 					if(this.txn != null && this.txn.id == p.getTxnNum()){
 						this.n.printError("Node " + this.n.addr + ": Delay: Couldn't commit transation#" + + p.getTxnNum() + ". Server " + dest + 
 								" returned error code " + Error.ERROR_STRINGS[Error.ERR_20] + ". Electing a new leader and sending a retry...");
 						this.leader = this.paxos.electLeader();
 					}
 				}else if(pkt.getProtocol() == TXNProtocol.ABORT){
 					CommitPacket p = CommitPacket.unpack(pkt.getPayload(), this.cache);
 					if(this.txn != null && this.txn.id == p.getTxnNum()){
 						this.n.printError("Node " + this.n.addr + ": Delay: Couldn't abort transation#" + + p.getTxnNum() + ". Server " + dest + 
 								" returned error code " + Error.ERROR_STRINGS[Error.ERR_20] + ". Electing a new leader and sending a retry...");
 						this.leader = this.paxos.electLeader();
 					}
 				}else if(pkt.getProtocol() == TXNProtocol.WQ || pkt.getProtocol() == TXNProtocol.CREATE){
 					String fileName = Utility.byteArrayToString(pkt.getPayload());
 					File f = this.getFileFromCache(fileName);
 					Command c = (Command)f.execute();
 					this.n.printError(c, Error.ERR_20);
 					this.txnExecute();
 					this.executeCommandQueue(f);
 					this.leader = this.paxos.electLeader();
 				}else if(pkt.getProtocol() == TXNProtocol.PAXOS){
 					this.paxos.onTimeout(dest, pkt.getPayload());
 				}
 			}
 		}
 	}
 	
 	private void masterReceive(int from, TXNPacket pkt){
 		MasterFile f;
 		String contents, fileName;
 		int i, lastSpace;
 		
 		switch(pkt.getProtocol()){
 			case TXNProtocol.WQ://payload structure: "[fileName]"
 				fileName = Utility.byteArrayToString(pkt.getPayload());
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(f.getState() == File.INV){ //the file doesn't exist on the server, return an error
 					String payload = fileName + " " + Error.ERR_10;
 					this.rtn(from, TXNProtocol.ERROR, pkt.getSeqNum(), Utility.stringToByteArray(payload));
 				}else if(!f.isCheckedOut()){ //The file hasn't been checked out by anyone, return the last committed version.
 					try{
 						contents = this.n.get(fileName);
 						byte[] payload = Utility.stringToByteArray(f.getName() + " " + f.getVersion() + " " + MASTER_NODE + " " + contents);
 						f.changePermissions(from, MasterFile.FREE);
 						this.rtn(from, TXNProtocol.WD, pkt.getSeqNum(), payload);
 					}catch(IOException e){
 						String payload = fileName + " " + Error.ERR_10;
 						this.rtn(from, TXNProtocol.ERROR, pkt.getSeqNum(), Utility.stringToByteArray(payload));
 					}
 				}else if(f.isWaiting()){ //The server is currently waiting for some WFs to return from clients. Enqueue this request to execute once they have returned.
 					pkt.setSource(from);
 					f.execute(pkt);
 				}else{ //The server must send out WFs to all clients that have copies of the file, and pick the highest version that is returned.
 					pkt.setSource(from);
 					f.execute(pkt);
 					try{
 						f.propose(this.n.get(fileName), f.getVersion(), MASTER_NODE);
 					}catch(IOException e){
 						f.propose("", f.getVersion(), MASTER_NODE);
 					}
 					for(Integer client : f){
 						f.changePermissions(client, MasterFile.WF);
 						this.send(client, TXNProtocol.WF, Utility.stringToByteArray(f.getName()));
 					}
 				}
 				break;
 			case TXNProtocol.WD: //A client has returned its most recent version to the server.
 				contents = Utility.byteArrayToString(pkt.getPayload());
 				i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				lastSpace = i + 1;
 				i = contents.indexOf(' ', lastSpace);
 				int version = Integer.parseInt(contents.substring(lastSpace, i));
 				lastSpace = i + 1;
 				i = contents.indexOf(' ', lastSpace);
 				int txnID = Integer.parseInt(contents.substring(lastSpace, i));
 				contents = i == contents.length() - 1 ? "" : contents.substring(i + 1);
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(this.timeout.onRtn(from, pkt.getSeqNum()) && f.isWaiting()){
 					f.changePermissions(from, MasterFile.FREE);
 					if(f.getVersion() < version){
 						f.propose(contents, version, txnID);
 					}
 					this.returnWaiting(f);
 				}
 				break;
 			case TXNProtocol.ERROR:
 				String[] parts = Utility.byteArrayToString(pkt.getPayload()).split(" ");
 				
 				if(parts.length == 2){
 					fileName = parts[0];
 					int errCode = Integer.parseInt(parts[1]);
 					if(this.timeout.onRtn(from, pkt.getSeqNum()) && errCode == Error.ERR_10){
 						//This is a client saying it doesn't have a file after the server sent it a WF
 						//This means the MasterFile has some corrupted state, change permissions for that client to invalid.
 						f = (MasterFile)this.getFileFromCache(fileName);
 						f.changePermissions(from, File.INV);
 						this.returnWaiting(f);
 					}
 				}
 				break;
 			case TXNProtocol.COMMIT:
 				Transaction txn = CommitPacket.unpack(pkt.getPayload(), this.cache).getTransaction();
 				this.setHB(from, false);
 				if(!this.paxosQueue.containsKey(txn.id)){
 					this.paxosQueue.put(txn.id, pkt.getSeqNum());
 					this.commit(txn, pkt.getSeqNum(), false);
 				}
 				break;
 			case TXNProtocol.CREATE:
 				fileName = Utility.byteArrayToString(pkt.getPayload());
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(f.getState() == File.INV){
 					f.setState(File.RW);
 					f.changePermissions(from, MasterFile.FREE);
 					String payload = fileName + " " + f.getVersion() + " " + MASTER_NODE + " ";
 					this.rtn(from, TXNProtocol.WD, pkt.getSeqNum(), Utility.stringToByteArray(payload));
 				}else{
 					String payload = fileName + " " + Error.ERR_11;
 					this.rtn(from, TXNProtocol.ERROR, pkt.getSeqNum(), Utility.stringToByteArray(payload));
 				}
 				break;
 			case TXNProtocol.ABORT:
 				txnID = Integer.parseInt(Utility.byteArrayToString(pkt.getPayload()));
 				this.setHB(from, false);
 				if(!this.paxosQueue.containsKey(txnID)){
 					txn = new Transaction(txnID);
 					this.paxos.commit(txn);
 				}
 				break;
 		}
 	}
 	
 	private void returnWaiting(MasterFile f){
 		if(!f.isWaiting()){
 			TXNPacket p = (TXNPacket)f.execute();
 			Update u = f.chooseProp(p.getSource());
 			byte[] payload = Utility.stringToByteArray(f.getName() + " " + u.version + " " + u.source + " " + u.contents); //source is txnID
 			this.rtn(p.getSource(), TXNProtocol.WD, p.getSeqNum(), payload);
 			f.changePermissions(p.getSource(), MasterFile.FREE);
 			while(f.peek() != null){ //also return queued requests for the file
 				TXNPacket p1 = (TXNPacket)f.execute();
 				this.rtn(p1.getSource(), TXNProtocol.WD, p1.getSeqNum(), payload);
 				f.changePermissions(p1.getSource(), MasterFile.FREE);
 			}
 		}
 	}
 	
 	private void abortWaitingTxns(int from){
 		boolean logNotUpdated = true;
 		List<Integer> toRemove = new ArrayList<Integer>();
 		for(Integer committer : waitingQueue.keySet()){
 			Commit com = waitingQueue.get(committer);
 			if(com.isWaitingFor(from)){
 				if(logNotUpdated){
 					Integer fromTXNID = com.getDepTXNID(from);
 					if(fromTXNID != null){
 						this.updateLog(fromTXNID, false);
 						logNotUpdated = false;
 					}
 				}
 				
 				int txID = com.getLog().getTXN().id;
 				this.updateLog(txID, false);
 				this.rtn(committer, TXNProtocol.ABORT, com.getSeqNum(), Utility.stringToByteArray(txID + ""));
 				for(String fName : this.cache.keySet()){
 					MasterFile f = (MasterFile)this.cache.get(fName);
 					f.abort(committer);
 				}
 				toRemove.add(committer);
 			}
 		}
 		
 		for(Integer committer : toRemove){
 			this.waitingQueue.remove(committer);
 		}
 	}
 	
 	public boolean paxosFinished(Transaction txn, boolean abort){
 		int seqNum = -1;
 		if(this.paxosQueue.containsKey(txn.id))
 			seqNum = this.paxosQueue.get(txn.id);
 		if(abort)
 			return this.abort(txn, seqNum);
 		return this.commit(txn, seqNum, true);
 	}
 	
 	private boolean commit(Transaction txn, int seqNum, boolean paxosFinished){
 		int client = txn.id % RIONode.NUM_NODES;
 		
 		this.setHB(client, false);
 		this.waitingQueue.remove(client);
 		
 		if(this.txnLog.containsKey(txn.id)){
 			if(this.txnLog.get(txn.id)){
 				if(seqNum != -1)
 					this.rtn(client, TXNProtocol.COMMIT, seqNum, Utility.stringToByteArray(txn.id+""));
 				return true;
 			}else{
 				if(seqNum != -1)
 					this.rtn(client, TXNProtocol.ABORT, seqNum, Utility.stringToByteArray(txn.id+""));
 				return false;
 			}
 		}else if( txn.isEmpty() ) {
 			this.updateLog(txn.id, true);
 			if(seqNum != -1)
 				this.rtn(client, TXNProtocol.COMMIT, seqNum, Utility.stringToByteArray(txn.id+""));
 			return true;
 		}else {
 			Log log = new Log(client, txn);
 			Commit c = new Commit(client, log, this.txnLog, seqNum);
 			
 			if(c.abort()){
 				//txn should abort
 				for(MasterFile f : log)
 					f.abort(client);
 				this.updateLog(txn.id, false);
 				if(seqNum != -1)
 					this.rtn(client, TXNProtocol.ABORT, seqNum, Utility.stringToByteArray(txn.id+""));
 				return false;
 			}else if(c.isWaiting()){
 				//add commit to queue and send heartbeat to nodes that the commit is waiting for
 				if(!paxosFinished){
 					for(Integer addr : c){
 						this.setHB(addr, true);
 					}
 					this.waitingQueue.put(client, c);
 					return false;
 				}else{
 					this.updateLog(txn.id, false);
 					if(seqNum != -1)
 						this.rtn(client, TXNProtocol.ABORT, seqNum, Utility.stringToByteArray(txn.id + ""));
 					return false;
 				}
 			}else{
 				if(paxosFinished)
 					this.commit(client, seqNum, log);
 				else
 					this.paxos.commit(txn);
 				return true;
 			}
 		}
 	}
 	
 	private void commit(int client, int seqNum, Log log){
 		try{
 			Map<MasterFile, Update> updates = new HashMap<MasterFile, Update>();
 			for(MasterFile f : log){
 					int version = f.getVersion();
 					Update u = log.getInitialVersion(f);
 					String contents = u.contents;
 					boolean deleted = false;
 					
 					for(Command cmd : log.getCommands(f)){
 						 if(cmd.getType() == Command.CREATE){
 							 contents = "";
 							 deleted = false;
 							 version++;
 						 }else if(cmd.getType() == Command.APPEND){
 							contents += cmd.getContents();
 							version++;
 						}else if(cmd.getType() == Command.PUT){
 							contents = cmd.getContents();
 							version++;
 						} else if(cmd.getType() == Command.DELETE ) {
 							version++;
 							contents = "";
 							deleted = true;
 						}
 					}
 					if(!deleted)
 						updates.put(f, new Update(contents, version, client));
 					else
 						updates.put(f, new Update("", version, -1));
 					
 			}
 			this.n.write(".wh_log", log.getTXN().id + "\n" + Update.toString(updates), true, true);
 			this.pushUpdatesToDisk(log.getTXN().id, updates);
 		}catch(IOException e){
 			e.printStackTrace();
 			return;
 		}
 		if(seqNum != -1)
 			this.rtn(client, TXNProtocol.COMMIT, seqNum, Utility.stringToByteArray(log.getTXN().id+""));
 		
 		List<Integer> canCommit = new ArrayList<Integer>();
 		//Allow any transactions dependent on this one to commit
 		for(Integer committer : waitingQueue.keySet()){
 			Commit com = waitingQueue.get(committer);
 			if(com.isDepOn(log.getTXN().id)){
 				com.remove(log.getTXN().id);
 				if(!com.isWaiting()){
 					canCommit.add(committer);
 				}
 			}
 		}
 		for(Integer addr : canCommit){
 			Commit com = this.waitingQueue.remove(addr);
 			this.commit(com.getLog().getTXN(), com.getSeqNum(), false);
 		}
 	}
 	
 	public boolean abort(Transaction txn, int seqNum){
 		int client = txn.id % RIONode.NUM_NODES;
 		this.updateLog(txn.id, false);
 		
 		if(seqNum != -1){ //this is the leader
 			for(String fName : this.cache.keySet()){
 				MasterFile f = (MasterFile)this.cache.get(fName);
 				f.abort(client);
 			}
 			if(this.waitingQueue.containsKey(client)){ //This transaction tried to commit, but the client timed out on the return message
 				Commit c = this.waitingQueue.remove(client);
 				for(Integer dep : c){
 					boolean othersAreDep = false;
 					for(Integer committer : waitingQueue.keySet())
 						if(waitingQueue.get(committer).isWaitingFor(committer))
 							othersAreDep = true;
 					if(!othersAreDep)
 						this.setHB(dep, false);
 				}
 			}
 			//Try to commit the transactions dependent on this one (they should all abort b/c the txnlog is updated)
 			for(Integer committer : waitingQueue.keySet()){
 				Commit com = waitingQueue.get(committer);
 				if(com.isDepOn(client)){
 					this.commit(committer, com.getSeqNum(), com.getLog());
 				}
 			}
 			this.rtn(client, TXNProtocol.ABORT, seqNum, Utility.stringToByteArray(txn.id+""));
 		}
 		return false;
 	}
 	
 	public void pushUpdatesToDisk(int txnID, Map<MasterFile, Update> updates) throws IOException{
 		for(MasterFile f : updates.keySet()){
 			Update u = updates.get(f);
 			if(u.source == -1){
 				f.setState(File.INV);
 				this.n.delete(f.getName(), true);
 			}else{
 				f.setState(File.RW);
 				this.n.write(f.getName(), u.contents, false, true);
 			}
 			this.updateFileVersion(f, txnID, u.source, u.version);
 		}
 		this.updateLog(txnID, true);
 		this.n.delete(".wh_log", true);
 	}
 	
 	public void updateFileVersion(MasterFile f, int txnID, int source, int version){
 		f.commit(txnID);
 		f.setVersion(version);
 		this.n.updateFileVersion(f.getName(), txnID, version);
 	}
 	
 	private void updateLog(int txID, boolean committed){
 		this.txnLog.put(txID, committed);
 		String contents = "";
 		for(Integer id : this.txnLog.keySet()){
 			contents += id + " " + (this.txnLog.get(id) ? 1 : 0) + "\n";
 		}
 		try {
 			this.n.write(".txn_log", contents, false, true);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void slaveReceive(int from, TXNPacket pkt){
 		String fileName;
 		File f;
 		String contents;
 		int i;
 		Command c;
 		
 		switch(pkt.getProtocol()){
 			case TXNProtocol.WF:
 				fileName = Utility.byteArrayToString(pkt.getPayload());
 				f = this.getFileFromCache(fileName);
 				
 				if(this.txn == null || this.txn.hasFile(f) || this.txn.isDeleted(f)){
 					this.rtn(from, TXNProtocol.ERROR, pkt.getSeqNum(), Utility.stringToByteArray(fileName + " " + Error.ERR_10));
 				}else{
 					try {
 						byte[] payload = this.txn.getVersion(f, this.n.get(fileName));
 						this.rtn(from, TXNProtocol.WD, pkt.getSeqNum(), payload);
 					} catch (IOException e) {
 						this.rtn(from, TXNProtocol.ERROR, pkt.getSeqNum(), Utility.stringToByteArray(fileName + " " + Error.ERR_10));
 					}
 				}
 				break;
 			case TXNProtocol.WD:
 				if(this.timeout.onRtn(from, pkt.getSeqNum()) && this.leader == from){
 					contents = Utility.byteArrayToString(pkt.getPayload());
 					i = contents.indexOf(' ');
 					fileName = contents.substring(0, i);
 					int lastSpace = i + 1;
 					i = contents.indexOf(' ', lastSpace);
 					int version = Integer.parseInt(contents.substring(lastSpace, i));
 					lastSpace = i + 1;
 					i = contents.indexOf(' ', lastSpace);
 					int sourceTxn = Integer.parseInt(contents.substring(lastSpace, i));
 					contents = i == contents.length() - 1 ? "" : contents.substring(i + 1);
 					
 					f = this.getFileFromCache(fileName);
 					c = (Command)f.execute(); //Get command that originally requested this Query
 					try {
 						this.n.write(fileName, contents, false, true);
 						f.setState(File.RW);
 						f.setVersion(version);
 						this.txn.add(new Command(MASTER_NODE, Command.UPDATE, f, version + " " + sourceTxn + " " + contents));
 						if(c.getType() == Command.GET)
 							c.setContents(contents);
 						this.txn.add(c);
 						this.txnExecute();
 					} catch (IOException e) {
 						this.n.printError("Node " + this.n.addr + ": Fatal Error: Couldn't update file: " + fileName + " to version: " + version);
 					}
 					
 					executeCommandQueue(f);
 				}
 				break;
 			case TXNProtocol.ABORT:
 				int txID = Integer.parseInt(Utility.byteArrayToString(pkt.getPayload()));
 				this.timeout.onRtn(from, pkt.getSeqNum());
 				if(this.txn != null && txID == this.txn.id){
 					this.abort(false, false);
 				}
 				break;
 			case TXNProtocol.COMMIT:
 				txID = Integer.parseInt(Utility.byteArrayToString(pkt.getPayload()));
 				this.timeout.onRtn(from, pkt.getSeqNum());
 				if(this.txn != null && txID == this.txn.id){
 					this.commitChangesLocally();
 					this.commitConfirm();
 				}
 				break;
 			case TXNProtocol.ERROR:
 				contents = Utility.byteArrayToString(pkt.getPayload());
 				i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				contents = contents.substring(i + 1);
 				
 				if(this.timeout.onRtn(from, pkt.getSeqNum())){
 					f = this.getFileFromCache(fileName);
 					c = (Command)f.execute();
 					try{
 						int code = Integer.parseInt(contents.trim());
 						this.n.printError(c, code);
 					}catch(Exception e){
 						this.n.printError(contents);
 					}
 					txnExecute();
 					executeCommandQueue(f);
 				}
 				break;
 		}
 	}
 	
 	//CLIENT METHOD
 	public void commitChangesLocally() {
 		for( Command c : this.txn ) {
 			try {
 				int type = c.getType();
 				switch( type ) {
 				case Command.GET :
 					this.n.printSuccess(c);
 					this.n.printData(c.getContents());
 					break;
 				case Command.APPEND:
 					this.n.write(c.getFileName(), c.getContents(), true, false);
 					break;
 				case Command.PUT:
 					this.n.write(c.getFileName(), c.getContents(), false, false);
 					break;
 				case Command.DELETE:
 					this.n.delete(c.getFileName(), false);
 					break;
 				}
 			} catch(IOException e) {
 				this.n.printError("Node " + this.n.addr + ": Fatal Error: When applying commit locally on: " + c.getFileName() + "  command: " + c ) ;
 			}
 			if(c.getType() != Command.UPDATE && c.getType() != Command.GET)
 				this.n.printSuccess(c);
 		}
 		
 		this.n.printData("Node " + this.n.addr + ": Success: Transaction #" + this.txn.id + " successfully committed on node " + this.n.addr);
 	}
 	
 	private void executeCommandQueue(){
 		if(this.txn != null){
 			boolean commandsWereExecuted = true;
 			for(File f : this.cache.values())
 				commandsWereExecuted = this.executeCommandQueue(f) && commandsWereExecuted;
 			
 			if(!commandsWereExecuted){
 				this.isElected = txnExecute() && this.isElected;
 			}else if(this.txn.willAbort){
 				this.abort(true, false);
 			}else if(this.txn.willCommit){
 				this.commit(false);
 			}else{
 				this.isElected = false;
 			}
 		}
 	}
 	
 	/**
 	 * Executes as many commands as possible for the given File.
 	 * @param f File that contains commands to execute
 	 * @return true if any commands are on the queue in f
 	 */
 	private boolean executeCommandQueue(File f){
 		Command c = (Command)f.peek();
 		boolean stop = false;
 		boolean rtn = c != null;
 		while(c != null && !stop){
 			switch(c.getType()){
 			case Command.APPEND:
 				stop = !append(c, f);
 				break;
 			case Command.CREATE:
 				stop = !create(c, f);
 				break;
 			case Command.DELETE:
 				stop = !delete(c, f);
 				break;
 			case Command.PUT:
 				stop = !put(c, f);
 				break;
 			case Command.GET:
 				stop = !get(c, f);
 				break;
 			}
 			
 			c = (Command)f.peek();
 		}
 		return rtn;
 	}
 
 	
 
 	/*=====================================================
 	 * CLIENT METHODS
 	 * Methods DistNode uses to talk to TXNLayer
 	 *=====================================================*/
 	
 	public boolean get(String fileName){
 		if( assertTXNStarted() && notCommited() && notAborted() ) {
 			File f = this.getFileFromCache(fileName);
 			Command c = new Command(MASTER_NODE, Command.GET, f);
 			
 			if(f.execute(c) && !this.hasElection()){
 				return get(c, f);
 			}
 		}
 		return false;
 	}
 	
 	private boolean get(Command c, File f){
 		if(f.getState() == File.INV){
 			this.send(this.leader, TXNProtocol.WQ, Utility.stringToByteArray(f.getName()));
 			return false;
 		}else{
 			f.execute();
 			if(this.txn.isDeleted(f))
 				this.n.printError(c, Error.ERR_10);
 			else{
 				try{
 					c.setContents(this.txn.getVersionContents(f, this.n.get(f.getName())));
 					this.txn.add( c );
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 			txnExecute();
 			return true;
 		}
 	}
 
 	public boolean create(String filename){
 		boolean rtn = false;
 		if( assertTXNStarted() && notCommited() && notAborted() ) {
 			File f = getFileFromCache( filename );
 			Command c = new Command(MASTER_NODE, Command.CREATE, f, "");
 			
 			if(f.execute(c) && !this.hasElection()){
 				return create(c, f);
 			}
 		}
 		return rtn;
 	}
 	
 	private boolean create(Command c, File f){
 		if(f.getState() == File.INV && !this.txn.isDeleted(f)){
 			this.send(this.leader, TXNProtocol.CREATE, Utility.stringToByteArray(f.getName()));
 			return false;
 		}else{
 			f.execute();
 			if(this.txn.isDeleted(f)){
 				this.txn.add(c);
 				f.setState(File.RW);
 			}else
 				this.n.printError(c, Error.ERR_11);
 			this.txnExecute();
 			return true;
 		}
 	}
 
 	public boolean put(String filename, String content){
 		if( assertTXNStarted() && notCommited() && notAborted() ) {
 			File f = getFileFromCache( filename );
 			Command c = new Command(MASTER_NODE, Command.PUT, f, content);
 			
 			if(f.execute(c) && !this.hasElection()){
 				return put(c, f);
 			}
 		}
 		return false;
 	}
 	
 	private boolean put(Command c, File f){
 		if(f.getState() == File.INV){
 			this.send(this.leader, TXNProtocol.WQ, Utility.stringToByteArray(f.getName())); //WQ
 			return false;
 		}else{
 			f.execute();
 			if(this.txn.isDeleted(f)) {
 				this.n.printError(c, Error.ERR_10);
 			}
 			else {
 				this.txn.add( c );
 			}
 			txnExecute();
 			return true;
 		}
 	}
 
 	public boolean append(String filename, String content){
 		if( assertTXNStarted() && notCommited() && notAborted() ) {
 			File f = getFileFromCache( filename );
 			Command c = new Command(MASTER_NODE, Command.APPEND, f, content);
 			
 			if(f.execute(c) && !this.hasElection()) {
 				return append(c, f);
 			}
 		}
 		return false;
 	}
 	
 	private boolean append(Command c, File f){
 		if(f.getState() == File.INV) {
 			this.send(this.leader, TXNProtocol.WQ, Utility.stringToByteArray(f.getName())); //WQ
 			return false;
 		}else{
 			f.execute();
 			
 			if(this.txn.isDeleted(f)) {
 				this.n.printError(c, Error.ERR_10);
 			} else {
 				this.txn.add( c );
 			}
 			txnExecute();
 			return true;
 		}
 		
 	}
 
 	public boolean delete(String filename){
 		if( assertTXNStarted() && notCommited() && notAborted()) {
 			File f = getFileFromCache( filename );
 			Command c = new Command(MASTER_NODE, Command.DELETE, f);
 		
 			if(f.execute(c) && !this.hasElection()) {
 				return delete(c, f);
 			}
 		}
 		return false;
 	}
 	
 	private boolean delete(Command c, File f){
 		if(f.getState() != File.RW) {
 			this.send(this.leader, TXNProtocol.WQ, Utility.stringToByteArray(f.getName()));
 			return false;//WQ
 		} else {
 			f.execute();
 			this.txn.add(c);
 			txnExecute();
 			return true;
 			
 		}
 	}
 
 	public void abort(boolean notifyServer, boolean makeChecks) {
 		if(this.txn == null && notifyServer && makeChecks){
 			this.assertTXNStarted();
 		}else if(makeChecks && notifyServer && (!this.notCommited() || !this.notAborted())){
 			//^ prints out error message
 		}else if(makeChecks && notifyServer && (this.hasElection() || !this.noQueuedCommands())){
 			this.txn.willAbort = true;
 		}else{
 			if(notifyServer){
 				if(this.txn.isEmpty()){
 					this.isElected = false;
 					this.txn = null;
					this.n.printError("Node " + this.n.addr + " : Transaction aborted, please start a new transaction and try again.");
 				}else if(this.isElected){
 					this.isElected = false;
 					this.send(this.leader, TXNProtocol.ABORT, Utility.stringToByteArray(this.txn.id+""));
 				}else{
 					this.txn.willAbort = true;
 					this.leader = this.paxos.electLeader();
 				}
 			}else{
				this.n.printError("Node " + this.n.addr + " : Transaction aborted, please start a new transaction and try again.");
 				this.txn = null;
 			}
 		}
 	}
 	
 	public boolean txnExecute() {
 		if(this.txn != null && this.txn.willAbort){
 			this.txn.decrementNumQueued();
 			if( this.txn.getNumQueued() == 0 ) {
 				this.abort(true, false);
 			}
 		}else if( this.txn != null && this.txn.willCommit ) {
 			this.txn.decrementNumQueued();
 			if( this.txn.getNumQueued() == 0  && !this.hasElection()) {
 				if(this.txn.isEmpty()){
 					this.n.printData("Node " + this.n.addr + ": Success: Committed empty transaction #" + this.txn.id + ".");
 					this.commitConfirm();
 				}else if(this.isElected){
 					this.isElected = false;
 					this.send(this.leader, TXNProtocol.COMMIT, new CommitPacket(this.txn).pack());
 				}else{
 					this.txn.willCommit = true;
 					this.leader = this.paxos.electLeader();
 				}
 			}
 		}else
 			return false;
 		return true;
 	}
 
 	public void commit(boolean makeChecks) {
 
 		if(!makeChecks || this.assertTXNStarted() && this.notAborted() && this.notCommited()) {
 			//Check to see if there are queued commands before committing
 			if( !noQueuedCommands() || this.hasElection()) {
 				//set will commit to true to that the txn commits after all queued commands complete
 				this.txn.willCommit = true;
 			}else if(this.txn.isEmpty()){
 				this.n.printData("Node " + this.n.addr + ": Success: Committed empty transaction #" + this.txn.id + ".");
 				this.commitConfirm();
 			} else {
 				//Send txn to master node
 				if(this.isElected){
 					this.isElected = false;
 					this.send(this.leader, TXNProtocol.COMMIT, new CommitPacket(this.txn).pack());
 				}else{
 					this.txn.willCommit = true;
 					this.leader = this.paxos.electLeader();
 				}
 			}
 		}
 	}
 	
 	public boolean noQueuedCommands() {
 		int commandCount = 0;
 		for( File f : this.cache.values() ) {
 			commandCount += f.numCommandsOnQueue();
 		}
 		if( commandCount > 0 ) {
 			this.txn.setNumQueued( commandCount );
 			return false;
 		}
 		return true;
 	}
 	
 	public void commitConfirm() {
 		this.txn = null;
 		this.cache.clear();
 	}
 
 	public void txstart() {
 		if(this.txn != null){
 			this.n.printError("Node " + this.n.addr + ": ERROR: Transaction in progress on node " + this.n.addr + " : can not start new transaction");
 		}else{
 			try{
 				this.lastTXNnum = this.lastTXNnum + RIONode.NUM_NODES;
 				this.n.write(".txn_id", this.lastTXNnum + "", false, true);
 				
 				//start a new transaction by creating a new transaction object
 				this.txn = new Transaction( this.lastTXNnum );
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public boolean assertTXNStarted() {
 		if( this.txn == null ) {
 			this.n.printError("Node " + this.n.addr + ": ERROR: No transaction in progress on node " + this.n.addr + " : please start new transaction");
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean notCommited() {
 		if( this.txn.willCommit ) {
 			this.n.printError("Node " + this.n.addr + ": ERROR: Current transaction to be commited on node " + this.n.addr + ". Please wait for it to finish and then start a new transaction.");
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean notAborted() {
 		if( this.txn.willAbort ) {
 			this.n.printError("Node " + this.n.addr + ": ERROR: Current transaction to be aborted on node " + this.n.addr + ". Please wait for it to finish and then start a new transaction.");
 			return false;
 		}
 		return true;
 	}
 	
 	public File getFileFromCache(String fileName) {
 		File f = this.cache.get(fileName);
 		
 		if(f == null){
 			f = this.n.isMaster() ? new MasterFile(File.INV, fileName) : new File(File.INV, fileName);
 			if(!fileName.isEmpty())
 				this.cache.put(fileName, f);
 		}
 		return f;
 	}
 
 	public void setupCache(Map<String, Update> fileList) {
 		for(String fileName : fileList.keySet()){
 			Update u = fileList.get(fileName);
 			MasterFile f = (MasterFile)getFileFromCache(fileName);
 			f.setState(File.RW);
 			f.setVersion(u.version);
 			f.commit(u.source);
 		}
 	}
 
 }
