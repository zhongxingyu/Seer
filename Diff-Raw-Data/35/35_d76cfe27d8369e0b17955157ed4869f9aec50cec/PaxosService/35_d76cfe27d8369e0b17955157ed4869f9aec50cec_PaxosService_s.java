 /**
  * 
  */
 package Paxos;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import localServices.Status;
 import microFacebook.Validate;
 import Rio.Protocol;
 import distributedServices.BroacastNotify;
 import distributedServices.Helper;
 import distributedServices.INotify;
 import distributedServices.RpcClient;
 
 public class PaxosService {
 
 	//
 	// Helper types.
 	//
 	
 	public enum MessageType {PROPOSE, PREPARE, ACCEPT, LEARN, GET_HISTORY};
 	
 	static public class Packet implements java.io.Serializable {
 		private static final long serialVersionUID = 1L;
 		final public MessageType type;
 		final public int instance;
 		final public PaxosInstance.Proposal proposal;
 		
 		Packet(MessageType type, int instance, PaxosInstance.Proposal proposal) {
 			this.type = type;
 			this.instance = instance;
 			this.proposal = proposal;
 		}
 	}
 
 	static abstract public class PaxosNotify extends BroacastNotify {
 		int acceptCount = 0;
 		PaxosInstance.Proposal proposal = null;
 		int nodeCount;
 		
 		PaxosNotify(int nodeCount) {
 			this.nodeCount = nodeCount;
 		}
 
 		@Override
 		public void manyComplete(Status result, String data) {
 			
 			// Count the votes, ignore abstains (timeouts).
 			if (result == Status.SUCCESS) {
 				acceptCount += 1;
			} else if (result == Status.DENIED) {
				acceptCount -= 1;
 			}
 			
 			// Remember the highest numbered proposal in the requests.
 			if (data != null) {
 				PaxosInstance.Proposal notifyProposal = PaxosInstance.Proposal.parse(data);
 				if (this.proposal == null || proposal.number < notifyProposal.number) {
 					proposal = notifyProposal;
 				}
 			}
 			
 			if(getNotifyCount() == nodeCount) {
				paxosNotify(this.acceptCount > 0, this.proposal);
 			}
 		}
 		
 		abstract void paxosNotify(boolean isAccepted, PaxosInstance.Proposal proposal);
 	}
 	
 	//
 	// Service data members.
 	//
 
 	private boolean initialized = false;
 	private Map<Integer, PaxosInstance> instances = new HashMap<Integer, PaxosInstance>();
 	private List<Integer> quorum = new ArrayList<Integer>(); 
 	private RpcClient rpc = null;
 	final public Server server = new Server();
 	final public Client client = new Client();
 	
 	//
 	// Service implementation.
 	//
 
 	public PaxosService(RpcClient rpc) {
 		this.rpc = rpc;
 	}
 	
 	public void startPaxos(int nodeCount) {
 		for (int i = 0; i < nodeCount; i++) {
 			quorum.add(i);
 		}
 		loadInstances();
 	}
 	
 	private void sendQuorum(
 			MessageType type, 
 			int instance, 
 			PaxosInstance.Proposal proposal, 
 			PaxosNotify notify) {
 		Packet packet = new Packet(type, instance, proposal);
 		rpc.sendMany(quorum, Protocol.PAXOS, Helper.packToByteArray(packet), notify);
 	}
 	
 	private String instancesToString() {
 		StringBuilder sb = new StringBuilder();
 		for(Integer i : instances.keySet()) {
 			sb.append(String.format("%d:%s\n", i, instances.get(i).toString()));
 		}
 		return sb.toString();
 	}
 	
 	static private Map<Integer, PaxosInstance> parseInstances(String str) {
 		Map<Integer, PaxosInstance> instances = new HashMap<Integer, PaxosInstance>(); 
 		String[] lines = str.split("\n");
 		for(String line : lines) {
 			String[] items = line.split(":");
 			instances.put(Integer.parseInt(items[0]),
 						  PaxosInstance.parse(items[1]));
 		}
 		return instances;
 	}
 	
 	private void merge(Map<Integer, PaxosInstance> remoteInstances) {
 		for(int i : remoteInstances.keySet()) {
 			if (!this.instances.containsKey(i)) {
 				instances.put(i, remoteInstances.get(i));
 			} else if (this.instances.get(i).getLearnedValue() == null &&
 					   remoteInstances.get(i).getLearnedValue() != null) {
 				instances.put(i, remoteInstances.get(i));
 			}
 		}
 	}
 	
 	private void loadInstances() {
 		Packet packet = new Packet(MessageType.GET_HISTORY, 0, null);
 		rpc.sendMany(
 			quorum,
 			Protocol.PAXOS, 
 			Helper.packToByteArray(packet), 
 			new BroacastNotify() {
 				
 				@Override
 				public void manyComplete(Status result, String data) {
 					if (result == Status.SUCCESS) {
 						merge(parseInstances(data));
 					}
 					if (getNotifyCount() == quorum.size()) {
 						initialized = true;
 					}
 				}
 			});
 	}

	private boolean isQuorumMajority(int n) {
		return n > (quorum.size() + 1) / 2;
	}
 	
 	//
 	// Server class.
 	//
 	
 	public class Server {
 		
 		//
 		// Server data members.
 		//
 		
 		int proposalNumber = PaxosInstance.Proposal.START_NUMBER;
 		
 		//
 		// Server implementation.
 		//
 		
 		public void receiveCommand(final Integer from, final RpcClient.Packet rpcPacket) {
 			
 			Validate.isTrue(rpcPacket.status == Status.PendingRequest);
 			Validate.isTrue(rpcPacket.payload != null);
 			if(!initialized) {
 				rpc.respond(rpcPacket, from, Status.OperationTimeout, null);
 				return;
 			}
 			
 			final Packet packet = Helper.unpack(rpcPacket.payload);
 
 			//
 			// Get the paxos instance.
 			//
 			
 			final int instance = packet.instance;
 			if (!instances.containsKey(packet.instance)) {
 				instances.put(instance, new PaxosInstance());
 			}
 			final PaxosInstance paxos = instances.get(instance);
 			
 			//
 			// Do requested action.
 			//
 			
 			Status status;
 			switch(packet.type) {
 			case PROPOSE:
 				handlePropose(from, rpcPacket);
 				break;				
 			case PREPARE:
 				status = paxos.prepare(packet.proposal.number, packet.proposal);
 				rpc.respond(rpcPacket, from, status, packet.proposal.toString());
 				break;
 			case ACCEPT:
 				status = paxos.accept(packet.proposal);
 				rpc.respond(rpcPacket, from, status, packet.proposal.toString());
 				break;				
 			case LEARN:
 				Validate.isTrue(paxos.getLearnedValue() == null);
 				paxos.learn(packet.proposal);
 				break;
 			case GET_HISTORY:
 				rpc.respond(rpcPacket, from, Status.SUCCESS, instancesToString());
 				break;
 			}
 		}
 		
 		/**
 		 * Paxos propose algorithm implementation.
 		 * @param from
 		 * @param rpcPacket
 		 */
 		void handlePropose(final Integer from, final RpcClient.Packet rpcPacket) {
 			final Packet packet = Helper.unpack(rpcPacket.payload);
 			Validate.isTrue(packet.proposal.number == PaxosInstance.Proposal.START_NUMBER);
 			final int instance = packet.instance;
 			
 			// Phase 1. (a) A proposer selects a proposal number n and sends a prepare
 			// request with number n to a majority of acceptors.
 			Validate.isTrue(proposalNumber < PaxosInstance.Proposal.MAX_NUMBER);
 			proposalNumber += 1;
 			sendQuorum(
 					MessageType.PREPARE,
 					instance, 
 					new PaxosInstance.Proposal(proposalNumber, null),
 					new PaxosNotify(quorum.size()) {
 						@Override
 						void paxosNotify(boolean isAccepted, PaxosInstance.Proposal proposal) {
 							// Remember the highest number seen.
 							if (!isAccepted && proposal != null) {
 								Validate.isTrue(proposal.number >= proposalNumber);
 								proposalNumber = proposal.number;
 								
 								// Randomize retries to ensure liveliness:
 								// The famous result of Fischer, Lynch, and Patterson
 								// [1] implies that a reliable algorithm for electing a proposer must use
 								// either randomness or real timefor example, by using timeouts.
 								
 								try {
 									// milliseconds of sleep
 									Thread.sleep((long) (Math.random() * 100.0)); 
 								} catch (InterruptedException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 								handlePropose(from, rpcPacket);
 								return;
 							}
 							
 							// If the proposer receives a response to its prepare requests
 							// (numbered n) from a majority of acceptors, then it sends an accept
 							// request to each of those acceptors for a proposal numbered n with a
 							// value v, where v is the value of the highest-numbered proposal among
 							// the responses, or is any value if the responses reported no proposals.
 						
 							Object value = packet.proposal.value;
 							if (proposal != null) {
 								Validate.isTrue(proposal.number <= proposalNumber);
 								value = proposal.value;  
 							}
 							
 							sendQuorum(
 									MessageType.ACCEPT,
 									instance,
 									new PaxosInstance.Proposal(proposalNumber, value),
 									new PaxosNotify(nodeCount) {
 										@Override
 										void paxosNotify(boolean isAccepted, PaxosInstance.Proposal proposal) {
											if (!isAccepted && proposal != null) {
												Validate.isTrue(proposal.number >= proposalNumber);
												proposalNumber = proposal.number; 
 												
 												//
 												// Act as a learner, inform client of the outcome.
 												//
 												
 												rpc.respond(rpcPacket, 
 															from, 
 															Status.DENIED, 
 															packet.proposal.toString());
 												return;
 											}
											
											Validate.isTrue(proposal != null);
											Validate.isTrue(proposal.number == proposalNumber);
 
 											// Inform learners of the proposal outcome.
											if(isQuorumMajority(getNotifyCount())) {
												sendQuorum(MessageType.LEARN, instance, proposal, null); 
											}
 											
 											//
 											// Act as a learner, inform client of the outcome.
 											//
 											
 											rpc.respond(rpcPacket, 
 														from, 
 														Status.SUCCESS, 
 														packet.proposal.value.toString());
 										}
 									});
 						}
 					});
 		}
 	}
 	
 	//
 	// Client implementation.
 	//
 	
 	public class Client {
 		
 		INotify proposeNotify;
 		
 		private void send(MessageType type, int instance, Object value, INotify notify) {
 			rpc.send(
 					RpcClient.LOOPBACK, 
 					Protocol.PAXOS,
 					Helper.packToByteArray(
 						new Packet(
 								type, 
 								instance, 
 								new PaxosInstance.Proposal(
 										PaxosInstance.Proposal.START_NUMBER, 
 										value))),
 					notify);
 		}
 		
 		public void setDecided(INotify notify) {
 			proposeNotify = notify;
 		}
 		
 		/**
 		 * Asks the Paxos node to initiate voting on a new proposal.
 		 * @param roundId ID of the paxos round
 		 * @param command Command that is to be voted on
 		 */
 		public void propose(int instance, Object value){
 			send(MessageType.PROPOSE, instance, value, proposeNotify);
 		}
 
 		/**
 		 * Returns the history of voting that the current node knows about.
 		 * @return
 		 */
 		public Map<Integer, String> getHistory(){
 			
 			if (!initialized) {
 				return null;
 			}
 			
 			HashMap<Integer, String> history = new HashMap<Integer, String>();
 			for(int i : instances.keySet()) {
 				history.put(i, (String) instances.get(i).getLearnedValue());
 			}
 			return history;
 		}
 	}
 }
