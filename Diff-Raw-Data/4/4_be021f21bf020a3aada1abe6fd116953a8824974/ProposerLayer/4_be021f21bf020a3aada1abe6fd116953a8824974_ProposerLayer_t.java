 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Queue;
 
 import edu.washington.cs.cse490h.lib.Callback;
 import edu.washington.cs.cse490h.lib.Utility;
 
 public class ProposerLayer {
 
 	private PaxosLayer paxosLayer;
 	private int promises;
 	private int rejects;
 	private static int MAJORITY;
 	private int proposalNumber;
 	private int instanceNumber;
 	private Map<String, Integer> instanceValues;
 	private Map<Integer, String> values;
 	private Queue<String> commits;
 	private HashMap<Integer, PaxosPacket> unACKedPackets;
 	private HashMap<Integer, Integer> pktRetries;
 	private DistNode n;
 	
 
 	public ProposerLayer(PaxosLayer paxosLayer, DistNode n) {
 		this.commits = new LinkedList<String>();
 		this.instanceValues = new HashMap<String, Integer>();
 		this.values = new HashMap<Integer, String>();
 		
 		this.n = n;
 		
 		this.paxosLayer = paxosLayer;
 		ProposerLayer.MAJORITY = PaxosLayer.ACCEPTORS.length / 2 + 1;
 		this.proposalNumber = 0;
 		this.instanceNumber = -1;
 
 		this.promises = 0;
 		this.rejects = 0;
 
 		unACKedPackets = new HashMap<Integer, PaxosPacket>();
 		pktRetries = new HashMap<Integer, Integer>();
 	}
 	
 	public void start(){
 		this.instanceNumber = fillGaps();
 	}
 	
 	public void send(int dest, PaxosPacket pkt){
 		this.paxosLayer.send(dest, pkt);
 	}
 
 	public void receivedPromise(int from, PaxosPacket pkt) {
 		if(pkt.getInstanceNumber() == this.instanceNumber){
 			int propNumber = pkt.getProposalNumber();
 			if(propNumber > this.proposalNumber){
 				this.proposalNumber = propNumber;
 				this.values.put(pkt.getInstanceNumber(), Utility.byteArrayToString(pkt.getPayload()));
 			}
 			
 			if(pkt.getProtocol() == PaxosProtocol.REJECT){
 				rejects++;
 				if(rejects >= MAJORITY && this.paxosLayer.getLearnerLayer().getLargestInstanceNum() < this.instanceNumber){
 					sendPrepares();
 					resetRP();
 				}
 	
 			} else {
 				promises++;
 				if(promises >= MAJORITY && this.paxosLayer.getLearnerLayer().getLargestInstanceNum() < this.instanceNumber){
 					sendProposal();
 					resetRP();
 				}
 			}
 		}
 	}
 	
 	private void resetRP(){
 		this.promises = 0;
 		this.rejects = 0;
 	}
 	
 	
 	public void receivedRecovery(int from, PaxosPacket pkt){
 
 		if(pkt.getProtocol() == PaxosProtocol.RECOVERY_CHOSEN){
 
 			paxosLayer.getLearnerLayer().writeValue(new Proposal(pkt));
 			sendPrepares();
 			this.values.put(this.instanceNumber, commits.peek());
 			
 		} else{
 			String payload = Utility.byteArrayToString(pkt.payload);
 			if(instanceValues.containsKey(payload)){
 				int count = instanceValues.get(payload) + 1;
 				if(count >= MAJORITY){
 					paxosLayer.getLearnerLayer().writeValue(new Proposal(pkt));
 					sendPrepares();
 					this.values.put(this.instanceNumber, commits.peek());
 				} else 
 					instanceValues.put(payload, count);
 			}else
 				instanceValues.put(payload, 1);
 		}
 		//DONE FIXING HOLES IF THIS IS TRUE, READY TO START A NEW INSTANCE!!
 
 		
 	}
 	
 	private void sendProposal() {
 		PaxosPacket pkt = new PaxosPacket(PaxosProtocol.PROPOSE, this.proposalNumber, 
 				this.instanceNumber, Utility.stringToByteArray(this.values.get(this.instanceNumber)));
 		for(int acceptor : PaxosLayer.ACCEPTORS){
 			send(acceptor, pkt);
 		}
 	}
 
 	public void receivedCommit(String commit){
 		commits.add(commit);
 		if(commits.size() == 1){
 			newInstance(commit);
 			this.sendPrepares();
 		}
 	}
 	
 	public void instanceFinished(){
 		String commit = commits.poll();
 		if(commit != null){
 			newInstance(commit);
 			this.sendPrepares();
 		}
 	}
 	
 	private void newInstance(String value){
		this.createTimeoutListener(this.instanceNumber);
 		resetRP();
 		this.instanceNumber = fillGaps() + 1;
 	}
 	
 	private void fixHole(int instance){
 		for(int acceptor : PaxosLayer.ACCEPTORS){
 			PaxosPacket pkt = new PaxosPacket(PaxosProtocol.RECOVERY, 0, instance, new byte[0]);
 			send(acceptor, pkt);
 		}		
 	}
 	
 	/**
 	 * Sends a prepare request to all known acceptors with the highest proposal number we have seen for the current instanceNumber
 	 * and nothing in the payload
 	 */
 	private void sendPrepares(){
 		for(int acceptor : PaxosLayer.ACCEPTORS){
 			PaxosPacket pkt = new PaxosPacket(PaxosProtocol.PREPARE, this.proposalNumber, this.instanceNumber, new byte[0]);
 			send(acceptor, pkt);
 		}
 	}
 	
 	/**
 	 * 
 	 * @return the current instance of paxos you should be using
 	 */
 	private int fillGaps(){
 
 			ArrayList<Integer> missingInst = paxosLayer.getLearnerLayer().getMissingInstanceNums();
 			int largestInst = Math.max(paxosLayer.getAcceptorLayer().getMaxInstanceNumber(), paxosLayer.getLearnerLayer().getLargestInstanceNum());
 			
 			if(missingInst.size() != 0)
 				for(int i = missingInst.get(missingInst.size() - 1) + 1; i < largestInst + 1; i++)
 					missingInst.add(i);
 
 			for(Integer instance : missingInst)
 				fixHole(instance);
 
 
 
 		return largestInst;
 	}
 	
 	private void createTimeoutListener(int instance) {
 		try{
 			Method onTimeoutMethod = Callback.getMethod("onTimeout", this, new String[]{ "java.lang.Integer"});
 			//waits 13 time steps
 			this.n.addTimeout(new Callback(onTimeoutMethod, this.paxosLayer, new Object[]{ instance }), 13);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public void onTimeout(Integer instance) {
 		//it timed out, time to resend packets!
 		if(this.instanceNumber == instance){
 			createTimeoutListener(instance);
 			if(promises > MAJORITY)
 				sendProposal();
 			else
 				sendPrepares();
 		}
 	}
 
 
 
 	
 	
 }
