 /**
  * 
  */
 package distributedServices;
 
 import java.lang.reflect.Method;
 
 import localServices.FileSystem;
 import localServices.Transaction;
 import microFacebook.Validate;
 import Rio.Protocol;
 import Rio.RIONode;
 import Rio.ReliableInOrderMsgLayer;
 import edu.washington.cs.cse490h.lib.Callback;
 
 /**
  * Class that implements a node that provides file services.
   */
 public class ServiceNode extends RIONode {
 	
 	protected RpcClient rpc;
 	protected FileSystem fileSystem;	
 	protected FileServer fileServer;
 	protected FileClient fileClient;
 	protected Transaction transaction;
 	private TransactionService transactionService;	
 	protected DistributedTransactionManager DtManager;
 	protected Integer keepAliveReasons;
 	protected boolean localReasons;
<<<<<<< HEAD

=======
	protected FileSystem fileSystem;
	
>>>>>>> a4e4becb3b5b8e89c41755dd94d771df8ae31e1e
 	/**
 	 * 
 	 */
 	public ServiceNode() {
 		keepAliveReasons = 0;
 	}
 	
 	/**
 	 * Instantiate server and client file classes.
 	 */
 	@Override 
 	public void start() {
 		rpc = new RpcClient(this);
 		fileSystem = new FileSystem(this);
 		fileServer = new FileServer(this, rpc, fileSystem);
 		fileClient = new FileClient(rpc);
 		transaction = new Transaction(fileSystem);
 		transactionService = new TransactionService(rpc, fileSystem, transaction);
 		// Instantiate the transaction manager and recover it from the log
 		DtManager = new DistributedTransactionManager(this, rpc);
 		registerKeepAlive();
 		DtManager.Recover();
 	}
 
 	@Override
 	public void onCommand(String command) {
 	}
 	
 	TransactionService.Client transactionClient() {
 		return transactionService.client;
 	}
 	
 	public int getTransactionForNode(int node) {
 		return transactionService.server.getTransactionForNode(node);
 	}
 	
 	public void addKeepAliveReasons(int reasons) {
 		Validate.Assert(reasons >= 0);
 		keepAliveReasons += reasons;
 		localReasons = keepAliveReasons > 0 ? true : false;
 		keepAliveCheck();
 	}
 
 	public void removeKeepAliveReasons(int reasons) {
 		Validate.Assert(reasons >= 0);
 		keepAliveReasons -= reasons;
 		localReasons = keepAliveReasons > 0 ? true : false;
 		keepAliveCheck();
 	}
 	
 	public void keepAliveCheck() {
 		if (keepAliveReasons > 0) {
 			broadcast(Protocol.KEEP_ALIVE, new byte[] {keepAliveReasons.byteValue()});
 			if (!localReasons) {
 				keepAliveReasons -= 1;
 			}
 		}
 	}
 	
 	public void registerKeepAlive() {
 		Method onTimeoutMethod = null;
 		try {
 			onTimeoutMethod = Callback.getMethod("keepAliveCheck", this, new String[]{});
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch(NoSuchMethodException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch(SecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		int timeout = 30 * ReliableInOrderMsgLayer.TIMEOUT;
 		if (!localReasons) {
 			timeout *= 2;
 		}
 		addTimeout(
 				new Callback(onTimeoutMethod, this, new Object[]{}), 
 				timeout);
 	}
 	
 	/**
 	 * Distributes the payload to services.
 	 */
 	@Override
 	public void onRIOReceive(Integer from, int protocol, byte[] msg) {
 		Validate.isTrue(protocol == Protocol.RPC_PKT || 
 				        protocol == Protocol.KEEP_ALIVE);
 		
 		if (protocol == Protocol.KEEP_ALIVE) {
 			if (!localReasons) {
 				keepAliveReasons = (int) msg[0];
 			}
 			registerKeepAlive();
 			return;
 		}
 
 		RpcClient.Packet packet = Helper.unpack(msg);
 		INotify notify = rpc.getAndRemoveNotification(packet.requestId);
 		
 		switch(packet.protocol) {
 			case Protocol.IO_PKT:
 				if (notify != null) {
 					fileClient.receiveResponse(from, packet.payload, notify);
 				} else {
 					fileServer.receiveCommand(
 							packet.requestId, 
 							from, 
 							packet.payload);
 				}
 				break;
 			case Protocol.TRANSACTION_PKT:
 				TransactionService.Packet transactionPacket = 
 				Helper.unpack(packet.payload);
 				if (notify != null) {
 					notify.onCompleted(transactionPacket.status, 
 									   String.valueOf(transactionPacket.transactionId));
 				} else {
 					transactionService.server.receive(
 							packet.requestId, 
 							from, 
 							transactionPacket);
 				}
 				break;
 			case Protocol.DTM_PKT:
 				DtPacket dtpacket = Helper.unpack(packet.payload);
 				DtManager.OnReceive(from, dtpacket);
 				break;
 		}
 	}
 }
