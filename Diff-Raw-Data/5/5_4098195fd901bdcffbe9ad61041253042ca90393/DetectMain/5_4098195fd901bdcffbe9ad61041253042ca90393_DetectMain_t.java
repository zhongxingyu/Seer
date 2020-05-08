 package edu.uc.cs.distsys.idetect;
 
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import edu.uc.cs.distsys.LogHelper;
 import edu.uc.cs.distsys.Node;
 import edu.uc.cs.distsys.comms.MessageListener;
 import edu.uc.cs.distsys.comms.NotifyThread;
 import edu.uc.cs.distsys.ilead.ElectionTracker;
 import edu.uc.cs.distsys.ilead.LeaderChangeListener;
 import edu.uc.cs.distsys.ilead.LeaderMain;
 import edu.uc.cs.distsys.ui.NodeStatusViewThread;
 
 public class DetectMain implements MessageListener<Heartbeat>, LeaderChangeListener	 {
 
 	private static final long HB_INIT_DELAY		 = 0;
 	private static final long FAIL_DETECT_PERIOD = 5;
 	public static final long  HB_PERIOD			 = 1;
 
 	private final ScheduledExecutorService scheduledExecutor;
 
 	private Lock heartbeatLock;
 	private HashMap<Integer, Node> nodes;
 	private List<Node> failedNodes;
 	private Thread detectorThread;
 	private LogHelper logger;
 	private NodeStatusViewThread statusViewThread;
 	private ElectionTracker tracker;
 	private Node myNode;
 
 	public DetectMain(int nodeId, List<Integer> peers) {
 		this.logger = new LogHelper(nodeId, System.out, System.err, null);
 		this.nodes = new HashMap<Integer, Node>();
 		this.failedNodes = new LinkedList<Node>();
 		this.heartbeatLock = new ReentrantLock();
 		this.scheduledExecutor = new ScheduledThreadPoolExecutor(1);	//TODO
 		this.myNode = new Node(nodeId);
 		this.statusViewThread = new NodeStatusViewThread(this.myNode.getId());
 		new Thread(statusViewThread).start();
 		
 		for (int peer : peers) {
 			this.nodes.put(peer, new Node(peer));
 		}
 	}
 
 	public void start() throws UnknownHostException {
 		HeartbeatThread hbThread = new HeartbeatThread(this.myNode.getId(), failedNodes, heartbeatLock, logger); 
 		this.detectorThread = Executors.defaultThreadFactory().newThread(
 				new NotifyThread<Heartbeat>(this.myNode.getId(), hbThread.getCommsWrapper(), this, logger));
 		this.detectorThread.start();
 		this.scheduledExecutor.scheduleAtFixedRate(hbThread, HB_INIT_DELAY, HB_PERIOD, TimeUnit.SECONDS);
 		this.scheduledExecutor.scheduleAtFixedRate(
 				new FailureDetectionThread(nodes, failedNodes, heartbeatLock), 
 				HB_INIT_DELAY, FAIL_DETECT_PERIOD, TimeUnit.SECONDS);
 		
 		List<LeaderChangeListener> listeners = new LinkedList<LeaderChangeListener>();
 		listeners.add(this);
 		listeners.add(hbThread);
 		this.tracker = new LeaderMain(this.myNode.getId(), listeners, logger);
 		this.tracker.start();
 		this.tracker.startNewElection();
 	}
 	
 	public void stop() {
 		this.logger.log("Detector shutting down");
 		this.scheduledExecutor.shutdownNow();
 		this.detectorThread.interrupt();
 	}
 
 	@Override
 	public void notifyMessage(Heartbeat status) {
 		try {
 			this.heartbeatLock.lock();
 			if (!nodes.containsKey(status.getNodeId())) {
 				logger.log("Discovered new node - " + status.getNodeId());
 				Node n = new Node(status);
 				this.nodes.put(status.getNodeId(), n);
 				this.statusViewThread.addMonitoredNode(n);
 			} else {
 				logger.debug("Received heartbeat from node " + status.getNodeId());
 				if (this.nodes.get(status.getNodeId()).updateStatus(status)) {
 					// Go through all reported failed nodes and update local state if necessary
 					for (Node failNode : status.getFailedNodes()) {
 						this.verifyFailedNode(failNode);
 					}
 				} else {
 					logger.error("Warning - Received out-of-order heartbeat from node " + status.getNodeId());
 				}
 			}
 			this.statusViewThread.updateUI();
 		} finally {
 			this.heartbeatLock.unlock();
 		}
 	}
 	
 	@Override
 	public void onNewLeader(int leaderId) {
 		this.logger.log("New Leader: " + leaderId);
 		if (this.myNode.getId() == leaderId) {
 			// update our UI to say we're the current user
 			this.statusViewThread.setUIMessage("Currently The Leader");
 		} else {
 			this.statusViewThread.setUIMessage(null);
 		}
 		// update who we believe the leader is
 		this.myNode.setLeaderId(leaderId);
 		// start telling all the other nodes who we believe the leader should be
 		for(Node n: this.nodes.values()) {
 			n.setLeaderId(leaderId);
 		}
 	}
 	
 	public int getLeaderId() {
 		return this.myNode.getLeaderId();
 	}
 	
 	public int getGroupId() {
 		return this.myNode.getGroupId();
 	}
 	
 	public int getId() {
 		return this.myNode.getId();
 	}
 	
 	/***
 	 * @return Number of known group members including self
 	 */
 	public int getNumGroupNodes() {
 		return this.nodes.size() + 1;
 	}
 	
 	private void verifyFailedNode(Node node) {
 		if (node.getId() == this.myNode.getId())
 			return;
 		try {
 			this.heartbeatLock.lock();
 			if (!nodes.containsKey(node.getId())) {
 				logger.log("Discovered new node (offline) - " + node.getId());
 				this.nodes.put(node.getId(), Node.createFailedNode(node.getId(), node.getSeqHighWaterMark()));
 			} else {
 				Node localNode = nodes.get(node.getId());
 				if (! localNode.isOffline() && localNode.getSeqHighWaterMark() <= node.getSeqHighWaterMark()) {
 					//update our node
 					localNode.markFailed(node.getSeqHighWaterMark());
 				} else if (! localNode.isOffline()) {
 					//discard out-of-date info
 					//DEBUG
 					logger.debug("Reported failed node is actually online (id=" + 
 										node.getId() + ")");
 				}
 			}
 			this.statusViewThread.updateUI();
 		} finally {
 			this.heartbeatLock.unlock();
 		}
 	}
 	
 	public static void main(String[] args) {
 		int node = 0;
 		if (args.length < 1) {
 			//System.err.println("Usage: " + args[0] + "<port#> [peer#1] ... [peer#N]");
 			
 			// DEBUGGING
 			//port = new Random(System.currentTimeMillis()).nextInt(1000) + 1024;
 			node = new Random(System.currentTimeMillis()).nextInt(1000);
		} else {
			if (args.length >= 1) {
				// first arg is node id
				node = Integer.parseInt(args[0]);
			}
 		}
 		
 		List<Integer> peers = new LinkedList<Integer>();
 		for (int i = 1; i < args.length; i++) {
 			peers.add(Integer.parseInt(args[i]));
 		}
 				
 		try {
 			final DetectMain detector = new DetectMain(node, peers);
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				@Override
 				public void run() {
 					detector.stop();
 				}
 			});
 			detector.start();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
