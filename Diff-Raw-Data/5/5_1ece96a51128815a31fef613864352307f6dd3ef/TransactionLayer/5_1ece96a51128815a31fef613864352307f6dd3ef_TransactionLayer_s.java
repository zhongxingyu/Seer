 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import edu.washington.cs.cse490h.lib.Utility;
 
 
 public class TransactionLayer {
 
 	public final static int MASTER_NODE = 0;
 	
 	private DistNode n;
 	private ReliableInOrderMsgLayer RIOLayer;
 	private Map<String, File> cache;
 	private int lastTXNnum;
 	private Transaction txn;
 	
 	/**
 	 * key = addr of node uploading its commit
 	 * value = list of commands received so far of the commit
 	 */
 	private Map<Integer, List<Command>> commitQueue;
 	/**
 	 * key = addr of node trying to commit
 	 * value = commit status
 	 */
 	private Map<Integer, Commit> waitingQueue;
 	
 	
 	public TransactionLayer(RIONode n, ReliableInOrderMsgLayer RIOLayer){
 		this.cache = new HashMap<String, File>();
 		this.n = (DistNode)n;
 		this.RIOLayer = RIOLayer;
 		this.lastTXNnum = n.addr;
 		this.commitQueue = this.n.addr == MASTER_NODE ? new HashMap<Integer, List<Command>>() : null;
 		this.waitingQueue = new HashMap<Integer, Commit>();
 	}
 
 	public void send(int server, int protocol, byte[] payload) {
 		TXNPacket pkt = new TXNPacket(protocol, payload);
 		this.RIOLayer.sendRIO(server, Protocol.TXN, pkt.pack());
 	}
 	
 	public void onReceive(int from, byte[] payload) {
 		TXNPacket packet = TXNPacket.unpack(payload);
 		if(packet.getProtocol() == TXNProtocol.HB)
 			this.sendHB(from);
 		else if(this.n.addr == MASTER_NODE){
 			masterReceive(from, packet);
 		}else
 			slaveReceive(packet);
 	}
 	
 	public void sendHB(int dest){
 		this.RIOLayer.sendRIO(dest, TXNProtocol.HB, new byte[0]);
 	}
 	
 	public void onTimeout(int dest, byte[] payload){
 		TXNPacket pkt = TXNPacket.unpack(payload);
 		if(this.n.addr == MASTER_NODE){
 			for(Integer committer : waitingQueue.keySet()){
 				Commit com = waitingQueue.get(committer);
 				for(Integer dep : com){
 					if(dep == dest){
 						this.send(committer, TXNProtocol.ABORT, new byte[0]);
 						break;
 					}
 				}
 			}
 		}else
 			this.n.printError(DistNode.buildErrorString(dest, this.n.addr, pkt.getProtocol(), Utility.byteArrayToString(pkt.getPayload()), Error.ERR_20));
 	}
 	
 	private void masterReceive(int from, TXNPacket pkt){
 		MasterFile f;
 		String contents, fileName;
 		int i, lastSpace;
 		
 		switch(pkt.getProtocol()){
 			case TXNProtocol.WQ:
 				fileName = Utility.byteArrayToString(pkt.getPayload());
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				if(f.getState() == File.INV){
					String payload = DistNode.buildErrorString(this.n.addr, from, TXNProtocol.WQ, fileName, Error.ERR_10);
 					this.send(from, TXNProtocol.ERROR, Utility.stringToByteArray(payload));
 				}else if(!f.isCheckedOut()){
 					try{
 						byte[] payload = Utility.stringToByteArray(f.getName() + " " + f.getVersion() + " " + this.n.get(fileName));
 						f.addDep(from, MASTER_NODE);
 						this.send(from, TXNProtocol.WD, payload);
 					}catch(IOException e){
						String payload = DistNode.buildErrorString(this.n.addr, from, TXNProtocol.WQ, fileName, Error.ERR_10);
 						this.send(from, TXNProtocol.ERROR, Utility.stringToByteArray(payload));
 					}
 				}else{
 					f.requestor = from;
 					for(Integer client : f){
 						f.changePermissions(client, MasterFile.WF);
 						this.send(client, TXNProtocol.WF, Utility.stringToByteArray(f.getName()));
 					}
 				}
 				break;
 			case TXNProtocol.WD:
 				contents = Utility.byteArrayToString(pkt.getPayload());
 				i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				lastSpace = i + 1;
 				int version = Integer.parseInt(contents.substring(lastSpace, i));
 				contents = contents.substring(i + 1);
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				f.changePermissions(from, MasterFile.FREE);
 				if(this.txn.getVersion(f) < version){
 					f.propose(contents, version, from);
 				}
 				if(!f.isWaiting()){
 					Update u = f.chooseProp(f.requestor);
 					this.send(f.requestor, TXNProtocol.WD, Utility.stringToByteArray(fileName + " " + u.version + " " + u.contents));
 					f.requestor = -1;
 				}
 				break;
 			case TXNProtocol.ERROR:
 				String[] parts = Utility.byteArrayToString(pkt.getPayload()).split(" ");
 				
 				if(parts.length == 2){ 
 					fileName = parts[0];
 					int errCode = Integer.parseInt(parts[1]);
 					if(errCode == Error.ERR_10){
 						//This is a client saying it doesn't have a file after the server sent it a WF
 						//This means the MasterFile has some corrupted state, change permissions for that client to invalid.
 						f = (MasterFile)this.getFileFromCache(fileName);
 						f.changePermissions(from, File.INV);
 					}
 				}
 				break;
 			case TXNProtocol.COMMIT_DATA:
 				if(!this.commitQueue.containsKey(from))
 					this.commitQueue.put(from, new ArrayList<Command>());
 				
 				contents = Utility.byteArrayToString(pkt.getPayload());
 				i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				lastSpace = i + 1;
 				int commandType = Integer.parseInt(contents.substring(lastSpace, i));
 				f = (MasterFile)this.getFileFromCache(fileName);
 				
 				Command c;
 				if(commandType == Command.APPEND || commandType == Command.PUT || commandType == Command.UPDATE){
 					contents = contents.substring(i + 1);
 					c = new Command(MASTER_NODE, commandType, f, contents);
 				}else{
 					c = new Command(MASTER_NODE, commandType, f);
 				}
 				
 				this.commitQueue.get(from).add(c);
 				break;
 			case TXNProtocol.COMMIT:
 				this.commit(from, Integer.parseInt(Utility.byteArrayToString(pkt.getPayload())));
 				break;
 		}
 	}
 	
 	private void commit(int client, int size){
 		List<Command> commands = this.commitQueue.get(client);
 		if(size != commands.size()){
 			this.send(client, TXNProtocol.ERROR, Utility.stringToByteArray(Error.ERROR_STRINGS[Error.ERR_40]));
 		}else{
 			Log log = new Log(commands);
 			Commit c = new Commit(client, log);
 			
 			if(c.abort()){
 				for(MasterFile f : log)
 					f.abort(client);
 				this.send(client, TXNProtocol.ABORT, new byte[0]);
 			}else if(c.isWaiting()){
 				//add commit to queue and send heartbeat to nodes that the commit is waiting for
 				for(Integer addr : c){
 					this.send(addr, TXNProtocol.HB, new byte[0]);
 				}
 				this.waitingQueue.put(client, c);
 			}else{
 				//push changes to disk and put most recent version in memory in MasterFile
 				for(MasterFile f : log){
 					try{
 						int version = f.getVersion();
 						String contents = this.n.get(f.getName());
 						for(Command cmd : log.getCommands(f)){
 							if(cmd.getType() == Command.APPEND){
 								contents += cmd.getContents();
 								version++;
 							}else if(cmd.getType() == Command.PUT){
 								contents = cmd.getContents();
 								version++;
 							}
 						}
 						this.n.write(f.getName(), contents, false, true);
 						f.commit(client);
 					}catch(IOException e){
 						//TODO: send back error to client
 						return;
 					}
 				}
 				this.send(client, TXNProtocol.COMMIT, new byte[0]);
 			}
 		}
 	}
 	
 	private void slaveReceive(TXNPacket pkt){
 		String fileName;
 		File f;
 		
 		switch(pkt.getProtocol()){
 			case TXNProtocol.WF:
 				fileName = Utility.byteArrayToString(pkt.getPayload());
 				f = this.getFileFromCache(fileName);
 				try {
 					byte[] payload = this.txn.getVersion(f, this.n.get(fileName));
 					this.send(MASTER_NODE, TXNProtocol.WD, payload);
 				} catch (IOException e) {
 					this.send(MASTER_NODE, TXNProtocol.ERROR, Utility.stringToByteArray(fileName + " " + Error.ERR_10));
 				}
 				break;
 			case TXNProtocol.WD:
 				String contents = Utility.byteArrayToString(pkt.getPayload());
 				int i = contents.indexOf(' ');
 				fileName = contents.substring(0, i);
 				int lastSpace = i + 1;
 				int version = Integer.parseInt(contents.substring(lastSpace, i));
 				contents = contents.substring(i + 1);
 				
 				f = this.getFileFromCache(fileName);
 				Command c = (Command)f.execute(); //Get command that originally requested this Query
 				try {
 					this.n.write(fileName, contents, false, true);
 					f.setState(File.RW);
 					f.setVersion(version);
 					this.txn.add(new Command(MASTER_NODE, Command.UPDATE, f, version + ""));
 					this.txn.add(c);
 					this.n.printSuccess(c);
 				} catch (IOException e) {
 					this.n.printError("Fatal Error: Couldn't update file: " + fileName + " to version: " + version);
 				}
 				
 				executeCommandQueue(f);
 				break;
 			case TXNProtocol.ABORT:
 				this.abort();
 				break;
 			case TXNProtocol.COMMIT:
 				this.commitChangesLocally();
 				this.commitConfirm();
 				break;
 		}
 	}
 	
 	public void commitChangesLocally() {
 		for( Command c : this.txn ) {
 			try {
 				int type = c.getType();
 				switch( type ) {
 				case Command.GET :
 					this.n.printData(this.n.get(c.getFileName() ));
 					break;
 				case Command.APPEND:
 					this.n.write(c.getFileName(), c.getContents(), true, false);
 					break;
 				case Command.PUT:
 					this.n.write(c.getFileName(), c.getContents(), false, false);
 					break;
 				case Command.CREATE:
 					this.n.create(c.getFileName());
 					break;
 				case Command.DELETE:
 					this.n.delete(c.getFileName());
 					break;
 				}
 			} catch(IOException e) {
 				this.n.printError("Fatal Error: When applying commit locally on: " + c.getFileName() + "  command: " + c ) ;
 			}
 
 			this.n.printSuccess(c);
 		}
 		
 	}
 	
 	public void executeCommandQueue(File f){
 		Command c = (Command)f.peek();
 		boolean stop = false;
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
 	}
 
 
 	
 
 	/*=====================================================
 	 * Methods DistNode uses to talk to TXNLayer
 	 *=====================================================*/
 	
 	public boolean get(String filename){
 		File f = this.cache.get( filename );
 		Command c = new Command(MASTER_NODE, Command.GET, f);
 		
 		if(f.execute(c)){
 			return get(c, f);
 		}
 		
 		return false;
 	}
 	
 	private boolean get(Command c, File f){
 		if(f.getState() == File.INV){
 			this.send(MASTER_NODE, RPCProtocol.GET, Utility.stringToByteArray(f.getName()));
 			return false;
 		}else{
 			f.execute();
 			//this.n.printData(this.txn.getVersion( this.n.get(f.getName()) ));
 			this.txn.add( c );
 			return true;
 		}	
 	}
 
 	//TODO: Decide what to do for creates/deletes and transactions
 	public boolean create(String filename){
 		boolean rtn = false;
 		File f = getFileFromCache( filename );
 		Command c = new Command(MASTER_NODE, Command.CREATE, f, "");
 		
 		if(f.execute(c)){
 			return create(c, f);
 		}
 		
 		return rtn;
 	}
 	
 	private boolean create(Command c, File f){
 		if(f.getState() == File.INV){
 			this.send(MASTER_NODE, RPCProtocol.CREATE, Utility.stringToByteArray(f.getName()));
 			return false;
 		}else{
 			f.execute();
 			this.n.printError(c, Error.ERR_11);
 			return true;
 		}
 	}
 
 	public boolean put(String filename, String content){
 		File f = getFileFromCache( filename );
 		Command c = new Command(MASTER_NODE, Command.PUT, f, content);
 		
 		if(f.execute(c)){
 			return put(c, f);
 		}
 		
 		return false;
 	}
 	
 	private boolean put(Command c, File f){
 		if(f.getState() == File.INV){
 			this.send(MASTER_NODE, RPCProtocol.GET, Utility.stringToByteArray(f.getName() + " " + File.RW)); //WQ
 			return false;
 		}else{
 			f.execute();
 			this.txn.add( c );
 			//this.n.printSuccess(c);
 
 			return true;
 		}
 	}
 
 	public boolean append(String filename, String content){
 		File f = getFileFromCache( filename );
 		Command c = new Command(MASTER_NODE, Command.APPEND, f, content);
 		
 		if(f.execute(c)) {
 			return append(c, f);
 		}
 		return false;
 	}
 	
 	private boolean append(Command c, File f){
 		if(f.getState() != File.RW) {
 			this.send(MASTER_NODE, RPCProtocol.GET, Utility.stringToByteArray(f.getName() + " " + File.RW)); //WQ
 			return false;
 		}else{
 			f.execute();
 			this.txn.add(c);
 			//this.n.printSuccess(c);
 			return true;
 		}
 		
 	}
 
 	//TODO: Decide what to do for creates/deletes and transactions
 	public boolean delete(String filename){
 		File f = getFileFromCache( filename );
 		Command c = new Command(MASTER_NODE, Command.DELETE, f);
 	
 		if(f.execute(c)) {
 			return delete(c, f);
 		}
 		return false;
 	}
 	
 	private boolean delete(Command c, File f){
 		if(f.getState() != File.RW) {
 			this.send(MASTER_NODE, RPCProtocol.DELETE, Utility.stringToByteArray(f.getName()));
 			return false;//WQ
 		} else {
 			f.execute();
 			return true;
 			
 		}
 	}
 
 	public void abort() {
 		this.txn = null;
 	}
 
 	public void commit() {
 		//Send all of our commands to the master node
 		for( Command c : this.txn ) {
 			String payload = c.getType() + " " + c.getFileName();
 			if( c.getType() == Command.PUT || c.getType() == Command.APPEND ) {
 				payload += " " + c.getContents();
 			}
 			this.send(MASTER_NODE, TXNProtocol.COMMIT_DATA, Utility.stringToByteArray(payload));
 		}
 		//Send the final commit message
 		this.send(MASTER_NODE, TXNProtocol.COMMIT, Utility.stringToByteArray(this.txn.id + "") );
 	}
 	
 	public void commitConfirm() {
 		this.txn = null;
 	}
 
 	public void start() {
 		if( this.txn != null ) {
 			int newTXNnum = this.lastTXNnum + RIONode.NUM_NODES;
 			
 			//start a new transaction by creating a new transaction object
 			this.txn = new Transaction( newTXNnum );
 		} else {
 			this.n.printError("ERROR: Transaction in progress: can not start new transaction");
 		}
 	}
 	
 	private File getFileFromCache(String fileName) {
 		File f = this.cache.get(fileName);
 		
 		if(f == null){
 			f = this.n.addr == MASTER_NODE ? new MasterFile(fileName, "") : new File(File.INV, fileName);
 			this.cache.put(fileName, f);
 		}
 		return f;
 	}
 
 }
