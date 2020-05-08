 package it.polimi.elet.selflet.message;
 
 import it.polimi.elet.selflet.id.ISelfLetID;
 import it.polimi.elet.selflet.istantiator.IVirtualMachineIPManager;
 import it.polimi.elet.selflet.istantiator.SelfletIstantiatorThread;
 import it.polimi.elet.selflet.istantiator.VirtualMachineIPManager;
 import it.polimi.elet.selflet.negotiation.ReplyRequestData;
 import it.polimi.elet.selflet.negotiation.nodeState.INodeState;
 import it.polimi.elet.selflet.nodeState.INodeStateManager;
 import it.polimi.elet.selflet.nodeState.NodeStateManager;
 import it.polimi.elet.servlet.RequestDispatcherServlet;
 import it.polimi.elet.thread.ThreadPool;
 
 import org.apache.log4j.Logger;
 
 import polimi.reds.Message;
 import polimi.reds.MessageID;
 import polimi.reds.TCPDispatchingService;
 
 /**
  * A thread that receives messages and takes the appropriate action
  * 
  * 
  * @author Nicola Calcavecchia <calcavecchia@gmail.com>
  * */
 public class MessageDispatchingThread extends Thread {
 
 	private static final Logger LOG = Logger
 			.getLogger(MessageDispatchingThread.class);
 	private static final Logger REQSLOG = Logger.getLogger("reqsLogger");
 
 	private static final int WAIT_STEP_MS = 50;
 
 	private final INodeStateManager nodeStateManager = NodeStateManager
 			.getInstance();
 	private final ISelfletNeighbors selfletNeighbors = SelfletNeighbors
 			.getInstance();
 	private final IVirtualMachineIPManager virtualMachineIPManager = VirtualMachineIPManager
 			.getInstance();
 
 	private final TCPDispatchingService dispatchingService;
 	private boolean stop;
 
 	public MessageDispatchingThread(TCPDispatchingService dispatchingService) {
 		this.dispatchingService = dispatchingService;
 		this.stop = false;
 	}
 
 	@Override
 	public void run() {
 
 		try {
 			while (!stop) {
 
 				while (dispatchingService.hasMoreMessages()) {
 					Message message = dispatchingService.getNextMessage();
 
 					if (message instanceof RedsMessage) {
 						RedsMessage redsMessage = (RedsMessage) message;
 						SelfLetMsg selfletMessage = redsMessage.getMessage();
 						analyzeMessage(selfletMessage);
 					} else {
 						LOG.info("Received other kind of message: " + message);
 					}
 				}
 
 				goToSleep();
 
 			}
 		} catch (Exception e) {
 			LOG.error(e);
 		}
 
 	}
 
 	private void goToSleep() {
 		// Wait
 		try {
 			Thread.sleep(WAIT_STEP_MS);
 		} catch (InterruptedException e) {
 			LOG.error(e);
 		}
 
 	}
 
 	private void analyzeMessage(SelfLetMsg selfletMessage) {
 		switch (selfletMessage.getType()) {
 
 		case ALIVE_SELFLET:
 			aliveSelfletMessage(selfletMessage);
 			break;
 
 		case NODE_STATE:
 			nodeStateMessage(selfletMessage);
 			break;
 
 		case ISTANTIATE_NEW_SELFLET:
 			istantiateNewSelfletMessage(selfletMessage);
 			break;
 
 		case REMOVE_SELFLET:
 			removeSelflet(selfletMessage);
 			break;
			
 		case REDIRECT_REQUEST_REPLY:
 			long replyTime = System.currentTimeMillis();
 			computeResponseTime(replyTime, selfletMessage);
 			break;
 
 		default:
 			System.out.println("Message received: " + selfletMessage);
 		}
 	}
 
 	private void removeSelflet(SelfLetMsg selfletMessage) {
 		LOG.debug("Received request to remove selflet "
 				+ selfletMessage.getFrom());
 		ISelfLetID selfletToBeRemoved = selfletMessage.getFrom();
 		removeSelflet(selfletToBeRemoved);
 	}
 
 	private void removeSelflet(ISelfLetID selfletToBeRemoved) {
 		nodeStateManager.removeNodeStateOfNeighbor(selfletToBeRemoved);
 		virtualMachineIPManager.freeIPOfSelflet(selfletToBeRemoved);
 	}
 
 	private void istantiateNewSelfletMessage(SelfLetMsg selfletMessage) {
 		SelfletIstantiatorThread selfletIstantiatorThread = new SelfletIstantiatorThread(
 				dispatchingService, selfletMessage);
 		ThreadPool.submitGenericJob(selfletIstantiatorThread);
 		LOG.debug("SelfletIstantiatorThread started");
 	}
 
 	private void nodeStateMessage(SelfLetMsg selfletMessage) {
 		LOG.debug("Received node state from " + selfletMessage.getFrom());
 		nodeStateManager.addState((INodeState) selfletMessage.getContent());
 	}
 
 	private void aliveSelfletMessage(SelfLetMsg selfletMessage) {
 		ISelfLetID from = selfletMessage.getFrom();
 		selfletNeighbors.addNeighbor(from);
 	}
	
	private void computeResponseTime(long replyTime, SelfLetMsg selfletMessage){
		ReplyRequestData replyData = (ReplyRequestData)selfletMessage.getContent();
		MessageID msgID = replyData.getMessageId();
		
		ISelfLetID from = selfletMessage.getFrom();
		String serviceName = replyData.getServiceName();
		
		long arrivalTime = RequestDispatcherServlet.requestMap.remove(msgID);
		long responseTime = replyTime - arrivalTime;
		REQSLOG.info(System.currentTimeMillis() + "," + serviceName + "," + responseTime + "," + from + ", 1");
 	}
 
 }
