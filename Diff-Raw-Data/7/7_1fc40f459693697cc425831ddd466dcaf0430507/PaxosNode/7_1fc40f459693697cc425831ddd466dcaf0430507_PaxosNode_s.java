 
 import edu.washington.cs.cse490h.lib.Callback;
 
 import java.lang.reflect.Method;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableSet;
 import java.util.Queue;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class PaxosNode {
 
   public static final int kLeaderElectionRequestNumber = -1;
   public static final int kLeaderElectionSettleTime = 5;
 
   public static class ProposalState {
     ProposalStatus status;
     ProposalValue value;
     RequestNumber requestId;
     int proposalId;
 
     int numResponsesReceived;
 
     HashSet<Integer> promises;
     HashSet<Integer> accepts;
     HashSet<Integer> rejects;
 
     public ProposalState(RequestNumber requestId, ProposalValue value) {
       this.value = value;
       this.requestId = requestId;
 
       this.status = ProposalStatus.NotSent;
     }
   }
 
   // Number of nodes in Paxos
   private int networkSize;
 
   // Node which owns this Paxos instance.
   private RIONode owner;
 
   // Our log of proposals.
   private ProposalLog log;
   
   // Set of all nodes that are 
   private NavigableSet<Integer> liveNodes;
 
   // Election State
   private boolean electionActive;
 
   // Last active election round number; includes the current round number.
   private int lastElectionRoundNumber;
 
   // Current Designated Proposer, or -1 if not known.
   private int designatedProposer;
   
   // Set of all nodes that have ever been contacted
   private Set<Integer> encounteredNodes;
 
   // Active locally-originated proposals.
   private Map<RequestNumber,ProposalState> activeProposalState;
 
   // One entry per active proposal.
   private Map<Integer,RequestNumber> requestNumberByProposalId;
 
   // Active proposals made when we were the designated proposer.
   private Map<Integer,Proposal> activeProposals;
 
   // Queue of all unsent Proposals. Valid if isDesignatedProposer() == true.
   private Queue<RequestNumber> serverQueuedRequests;
 
   // Next free ID provided to the designated proposer.
   private int nextFreeRequestId;
 
   // Next available proposal number; valid only if isDesignatedProposer() == true
   private int nextFreeProposalNumber;
 
   // Last-accepted proposal #. Valid always.
   private int lastPromisedProposalNumber;
 
   // Last-known proposal #. Also valid always.
   private int lastKnownProposalNumber;
 
   // User callback; fired when a locally-originated proposal is complete.
   private Callback proposalCompleteCb;
 
   /**
    * Instantiate a PaxosNode. Attempts to bootstrap itself onto the
    * network and fast-forward its log of proposals.
    *
    * @param networkSize
    *              Number of nodes in the Paxos network.
    * @param node 
    *              Local node to use.
    * @param bootstrapPeers 
    *              Iterable containing boostrap network addresses to try.
    * @param proposalCompleteCb
    *              Callback called when the PaxosNode discovers that a new
    *              proposal was accepted.
    *              Signature: void(RequestNumber, Proposal, ProposalState)
    * @param leaderElectionCb
    *              Callback called when a leader election terminates.
    *              Signature: void(Integer proposalNumber, Integer newLeader)
    */
   public PaxosNode(int networkSize,
                    RIONode node, 
                    Iterable<Integer> bootstrapPeers, 
                    Callback proposalCompleteCb,
                    Callback leaderElectionCb) {
     this.owner = node;
     log = new ProposalLog(node);
     liveNodes = new TreeSet<Integer>();
     liveNodes.add(owner.addr);
 
     electionActive = false;
     designatedProposer = -1;
 
     encounteredNodes = new HashSet<Integer>();
     activeProposalState = new HashMap<RequestNumber,ProposalState>();
 
     serverQueuedRequests = new LinkedList<RequestNumber>();
 
     lastPromisedProposalNumber = log.getLastCommittedProposalId();
     lastKnownProposalNumber = lastPromisedProposalNumber;
 
     this.proposalCompleteCb = proposalCompleteCb;
 
     for (Integer i : bootstrapPeers)
       addNode(i);
   }
   
   //-----------------------------[ Public API ]---------------------------------
 
   /**
    * Adds the given address to the set of encountered nodes. Each time
    * a leader election occurs, an attempt will be made to contact this
    * node and include it in the election.
    *
    * @param remoteAddr
    *                   New node address to track.
    */
   public void addNode(int remoteAddr) {
     encounteredNodes.add(remoteAddr);
     joinIfDead(remoteAddr);
   }
 
   /**
    * Makes the given proposal to the Paxos network, if the current node is in
    * the live node list. If the current node is not in the live node list, this
    * method will return false.
    *
    * @param p
    *        The proposal to make
    * @return true if the proposal was sent to the designated proposer, or if
    *         a leader election is ongoing, will be sent when it completes.
    */
   public RequestNumber propose(ProposalValue p) {
     if (liveNodes.size() < 2 && !electionActive)
       return null;
 
     RequestNumber requestId = new RequestNumber(owner.addr,
                                                 nextFreeRequestId++);
     activeProposalState.put(requestId, new ProposalState(requestId, p));
     maybeSendActiveProposal(requestId);
     return requestId;
   }
 
   /**
    * Determines if the owner is currently acting as the designated proposer.
    *
    * @return true if the owner is currently the designated proposer.
    */
   public boolean isDesignatedProposer() {
     return !electionActive && owner.addr == designatedProposer;
   }
 
   public boolean isConnected() {
     return electionActive || hasQuorum();
   }
 
   /**
    * Determine if quorum has been met.
    *
    * @return true if quorum exists.
    */
   public boolean hasQuorum() {
     return !electionActive && liveNodes.size() > computeSmallestMajority(networkSize);
   }
 
   //-----------------------[ Protected Helper Methods ]-------------------------
 
   protected <K> HashSet<K> addAndMaybeCreateSet(HashSet<K> set, K x) {
     if (set == null)
       set = new HashSet<K>();
 
     set.add(x);
     return set;
   }
 
   protected int computeSmallestMajority(int size) {
     return size / 2 + 1;
   }
 
   protected int computeLargestMinority(int size) {
     return (size - 1) / 2;
   }
 
   /**
    * If the current node is not live, tries to ping remoteAddr. If a
    * good response is received, initiates an election with remoteAddr
    * to try to join the network, in accordance with the normal
    * response to PaxosNodeStatus messages.
    *
    * @param remoteAddr
    *                   Node address to join.
    */
   protected void joinIfDead(int remoteAddr) {
     if (liveNodes.size() < 2)
       sendQuery(remoteAddr);
   }
 
   protected void sendQuery(int remoteAddr) {
     // NOTE: we don't care if the client fails to respond.
     owner.RIOSend(remoteAddr,
                   Protocol.DATA,
                   new QueryNodeMessage(log.getLastCommittedProposalId()).pack(),
                   null);
   }
 
   protected boolean maybeSendActiveProposal(RequestNumber requestId) {
     ProposalState s = activeProposalState.get(requestId);
     if (s == null || 
         s.status.equals(ProposalStatus.NotSent) || 
         serverQueuedRequests.contains(requestId))
       return false;
 
     if (electionActive)
       return false;
 
     if (isDesignatedProposer()) {
       serverQueuedRequests.offer(requestId);
       maybeProcessQueuedProposals();
 
       return true;
     } else {
       owner.RIOSend(designatedProposer,
                     Protocol.DATA,
                     new ProxiedProposalRequestMessage(requestId, s.value).pack());
       s.status = ProposalStatus.Pending;
     }
 
     return true;
   }
 
   /**
    * Assign a proposal number to the given proposal and send prepare
    * requests for the proposal. The proposal must be in state NotSent.
    *
    * @param activeProposalId id of the proposal to send.
    */
   protected void initiateProposal(RequestNumber activeProposalId) {
     if (designatedProposer != owner.addr)
       throw new IllegalStateException("Shouldn't be initiating a proposal " +
                                       "when we aren't the designated proposer");
 
     ProposalState ps = activeProposalState.get(activeProposalId);
     if (ps == null)
       throw new IllegalArgumentException("No such active proposal");
 
     if (electionActive && !(ps.value instanceof ElectionProposal))
       throw new IllegalStateException("Shouldn't be issuing regular proposals " +
                                       "when elections haven't settled...");
     
     if (!ps.status.equals(ProposalStatus.NotSent))
       throw new IllegalArgumentException("Proposal has already been sent!");
 
     ps.proposalId = nextFreeProposalNumber++;
     ps.status = ProposalStatus.Pending;
     
     Proposal p = new Proposal(ps.proposalId, ps.value);
     activeProposals.put(ps.proposalId, p);
 
     sendToAll(liveNodes, new PrepareRequestMessage(p));
   }
 
   public void sendToAll(Set<Integer> s, DFSMessage m) {
     byte[] msg = m.pack();
 
     for (Integer i : s) {
       if (i != owner.addr)
         owner.RIOSend(i, Protocol.DATA, msg);
     }
   }
 
   protected void refuseProposal(int from, Proposal p) {
     owner.RIOSend(from,
                   Protocol.DATA,
                   new PrepareResponseMessage(p.getId(), 
                                              lastPromisedProposalNumber).pack());
   }
 
   protected void sendProposalPromise(int proposer, Proposal p) {
     lastPromisedProposalNumber = p.getId();
     owner.RIOSend(proposer,
                   Protocol.DATA,
                   new PrepareResponseMessage(p.getId(),
                                              lastPromisedProposalNumber).pack());
   }
 
   protected void handleElectionProposal(int from, Proposal p) {
     if (!electionActive)
       return;
 
     ElectionProposal ep = (ElectionProposal) p.getValue();
     if (ep.getDesignatedProposer() != liveNodes.iterator().next())
       refuseProposal(from, p);
 
     sendProposalPromise(from, p);
   }
 
   protected void callProposalCompleteCallback(RequestNumber rq, 
                                               Proposal p, 
                                               ProposalStatus ps) {
     try {
       proposalCompleteCb.setParams(new Object[]{p, ps});
       proposalCompleteCb.invoke();
     } catch (Exception e) {
       System.err.println("An exception occurred while trying to invoke the " +
                          "user proposal-complete callback:");
       e.printStackTrace();
     }
   }
 
   protected ProposalStatusMessage buildProposalStatus(RequestNumber rq,
                                                       Proposal p,
                                                       ProposalStatus stat) {
     Flags f = new Flags();
     f.set(stat);
     return new ProposalStatusMessage(rq, p, f);
   }
 
   protected void maybeProcessQueuedProposals() {
     if (electionActive || !hasQuorum())
       return;
 
     if (serverQueuedRequests.size() == 0)
       return;
 
     ProposalState ps = activeProposalState.get(serverQueuedRequests.peek());
 
     if (ps.status.equals(ProposalStatus.NotSent))
       initiateProposal(ps.requestId);
   }
 
   protected void handleElectionProposalComplete(Proposal p, ProposalStatus s) {
     ElectionProposal ep = (ElectionProposal) p.getValue();
     if (s.equals(ProposalStatus.Accepted)) {
       electionActive = false;
       designatedProposer = ep.getDesignatedProposer();
       liveNodes = new TreeSet<Integer>(ep.getLiveNodes());
 //      lastElectionRoundNumber = ep.getElectionRoundNumber();
       
       maybeProcessQueuedProposals();
     } else {
       // Start up leader election if we were able to get ahold of anybody...
 
       if (liveNodes.size() > 1)
         startLeaderElection();
     }
   }
 
   protected void maybeActuateProposal(int proposalId) {
     ProposalLog.ProposalLogEntry entry = log.getProposal(proposalId);
     if (entry.isCommitted())
       return;
 
     if (log.getLastCommittedProposalId() != entry.getProposal().getId() - 1)
       return;
 
     if (entry.getProposal().getValue() instanceof ElectionProposal) {
       handleElectionProposalComplete(entry.getProposal(), entry.getStatus());
     } else {
      callProposalCompleteCallback(entry.getProposal(), entry.getStatus());
     }
 
     log.commit(proposalId);
   }
 
 
   protected void handleProposalComplete(ProposalState ps, Proposal p) {
     if (ps != null) {
       // We were the designated proposer for this proposal when it started;
       // broadcast its acceptance
       sendToAll(liveNodes, buildProposalStatus(ps.requestId, p, ps.status));
       maybeProcessQueuedProposals();
     }
 
     if (ps == null || ps.requestId.getNodeAddress() == owner.addr) {
       // Need to commit the proposal as accepted
       log.write(p, ps.status);
       maybeActuateProposal(p.getId());
     }
   }
 
   protected void handleProposalAcceptance(ProposalState ps, Proposal p) {
     ps.status = ProposalStatus.Accepted;
     handleProposalComplete(ps, p);
   }
 
   protected void handleProposalRejection(ProposalState ps, Proposal p) {
     ps.status = ProposalStatus.Failed;
     handleProposalComplete(ps, p);
   }
 
   protected int getDesiredLeader() {
     return liveNodes.iterator().next();
   }
 
   protected void startLeaderElection() {
     if (electionActive)
       return;
 
     System.err.println("Node " + owner.addr + " starting leader election...");
 
     electionActive = true;
     lastElectionRoundNumber++;
 
     liveNodes = new TreeSet<Integer>();
     liveNodes.add(owner.addr);
 
     sendToAll(encounteredNodes,
               new ProposerNominationMessage(getDesiredLeader(),
                                             liveNodes,
                                             lastElectionRoundNumber,
                                             lastKnownProposalNumber));
   }
 
   protected void sendNodeStatus(int to) {
     owner.RIOSend(to,
                   Protocol.DATA,
                   new PaxosNodeStatusMessage(log.getLastCommittedProposalId(),
                                              designatedProposer,
                                              lastElectionRoundNumber).pack());
   }
 
   protected void sendSyncProposalRequestMessage(int to) {
     owner.RIOSend(to,
                   Protocol.DATA,
                   new SyncProposalRequestMessage(log.getLastCommittedProposalId()).pack());
   }
 
   protected void sendProposalsAfter(int to, int remoteLastAcceptedProposalId) {
     owner.RIOSend(to,
                   Protocol.DATA,
                   new SyncProposalReplyMessage(log.getLastCommittedProposalId()).pack());
     
     if (remoteLastAcceptedProposalId >= log.getLastCommittedProposalId())
       return;
 
     List<ProposalLog.ProposalLogEntry> deficientProposals = 
       log.getProposalRange(remoteLastAcceptedProposalId,
                            log.getLastCommittedProposalId());
 
     for (ProposalLog.ProposalLogEntry p : deficientProposals) {
       owner.RIOSend(to,
                     Protocol.DATA,
                     buildProposalStatus(null, p.getProposal(), p.getStatus()).pack());
     }
   }
 
   protected void sendGossip(int sender, boolean correctSender, boolean correctGlobal) {
     DFSMessage msg = new ProposerNominationMessage(getDesiredLeader(),
                                                    liveNodes,
                                                    lastElectionRoundNumber,
                                                    lastKnownProposalNumber);
 
     if (correctSender && !correctGlobal) {
       owner.RIOSend(sender, Protocol.DATA, msg.pack());
     } else if (correctGlobal) {
       sendToAll(liveNodes, msg);
     }
   }
 
   protected void sendLeaderElectionProposal() {
     RequestNumber rq = new RequestNumber(owner.addr, kLeaderElectionRequestNumber);
     if (activeProposalState.containsKey(rq)) {
       System.err.println("Leader election proposal already active for " + owner.addr);
       return;
     }
 
     ElectionProposal ep = new ElectionProposal(liveNodes,
                                                owner.addr,
                                                lastElectionRoundNumber);
     
     ProposalState ps = new ProposalState(rq, ep);
 
     initiateProposal(rq);
   }
 
   //---------------------[ Network Input Handling ]-----------------------------
 
   protected void handleProposerNomination(int from, ProposerNominationMessage msg) {
     boolean correctSender = false;
     boolean correctGlobal = false;
 
     // Message is not valid if round number is in the past...
     if (msg.getElectionRoundNumber() < lastElectionRoundNumber &&
         !electionActive &&
         !liveNodes.contains(from)) {
       sendNodeStatus(from);
       return;
     }
 
     if (!electionActive)
       electionActive = true;
 
     // Ensure everybody is using the latest election round number.
     if (lastElectionRoundNumber != msg.getElectionRoundNumber()) {
       if (msg.getElectionRoundNumber() > lastElectionRoundNumber) {
         lastElectionRoundNumber = msg.getElectionRoundNumber();
         correctGlobal = true;
       } else {
         correctSender = true; 
       }
     }
 
     if (msg.getCandidateProposer() != getDesiredLeader()) {
       if (msg.getCandidateProposer() < getDesiredLeader())
         correctGlobal = true; // liveNodes will be updated about 5 lines down...
       else
         correctSender = true;
     }
 
     for (Integer n : msg.getLiveNodes()) {
       liveNodes.add(n);
     }
 
     if (msg.getLastPromisedProposalId() > lastKnownProposalNumber) {
       lastKnownProposalNumber = msg.getLastPromisedProposalId();
       correctGlobal = true;
     } else {
       correctSender = true;
     }
 
     addLeaderElectionSettlingCb();
     sendGossip(from, correctGlobal, correctSender);
   }
 
   protected void handleQueryNode(int from, QueryNodeMessage msg) {
     sendNodeStatus(from);
   }
 
   protected void handlePaxosNodeStatus(int from, PaxosNodeStatusMessage msg) {
     encounteredNodes.add(from);
 
     if (!isConnected() ||
         msg.getCurrentLeaderAddress() != designatedProposer) {
       startLeaderElection();
       return;
     }
 
     if (isDesignatedProposer() &&
         msg.getLastKnownProposalId() > nextFreeProposalNumber - 1) {
       // Live node thinks it's seen a later proposal than we have. Kick it.
       liveNodes.remove(from);
       // TODO send kick node message.
     } else if (!isDesignatedProposer() &&
                !electionActive &&
                msg.getLastKnownProposalId() > nextFreeProposalNumber) {
       // allow peers to be one more proposal ahead of us; if they are more, sen
       // sync proposal request
       sendSyncProposalRequestMessage(from);
     } else if (msg.getLastKnownProposalId() < nextFreeProposalNumber - 1) {
       sendProposalsAfter(from, msg.getLastKnownProposalId());
     }
   }
 
   protected void handleProxiedProposalRequest(int from, 
                                               ProxiedProposalRequestMessage msg) {
     if (!isDesignatedProposer() || electionActive) {
       Flags f = new Flags();
       f.set(ProposalStatus.Failed);
       owner.RIOSend(from,
                     Protocol.DATA,
                     new ProposalStatusMessage(msg.getRequestNumber(),
                                               null,
                                               f).pack());
       return;
     }
 
     activeProposalState.put(msg.getRequestNumber(),
                             new ProposalState(msg.getRequestNumber(),
                                               msg.getValue()));
 
     serverQueuedRequests.offer(msg.getRequestNumber());
     
     maybeProcessQueuedProposals();
   }
 
   protected void handlePrepareRequest(int from, PrepareRequestMessage msg) {
     if (from != designatedProposer)
       return;
 
     if (electionActive) {
       if (msg.getProposal().getValue() instanceof ElectionProposal) {
         handleElectionProposal(from, msg.getProposal());
         return;
       }
 
       // Proposal was not an election proposal; refuse it.
       refuseProposal(from, msg.getProposal());
     }
 
     if (msg.getProposal().getId() <= lastKnownProposalNumber) {
       refuseProposal(from, msg.getProposal());
       return;
     }
 
     sendProposalPromise(from, msg.getProposal());
   }
 
   protected void handlePrepareResponse(int from, PrepareResponseMessage msg) {
     ProposalState ps = activeProposalState.get(msg.getProposalNumber());
     if (ps == null)
       return;
 
     if (!isDesignatedProposer()) {
       // Not our problem...
       return;
     }
 
     Proposal p = activeProposals.get(msg.getProposalNumber());
     if (p == null) {
       System.err.println("Proposal not found when it should have been?");
       return;
     }
 
     if (msg.getLastPromised() < msg.getProposalNumber()) {
       // weird..
       return;
     } else if (msg.getLastPromised() > msg.getProposalNumber()) {
       ps.rejects = addAndMaybeCreateSet(ps.rejects, from);
       
       if (ps.rejects.size() >= computeLargestMinority(networkSize))
         handleProposalRejection(ps, p);
       return;
     }
 
     ps.promises = addAndMaybeCreateSet(ps.promises, from);
 
     if (ps.promises.size() > computeSmallestMajority(networkSize))
       sendToAll(liveNodes, new AcceptRequestMessage(p));
   }
 
   protected void handleAcceptRequest(int from, AcceptRequestMessage msg) {
     owner.RIOSend(from, 
                   Protocol.DATA, 
                   new AcceptReplyMessage(msg.getProposal().getId(),
                                          lastPromisedProposalNumber).pack());
 
     if (lastPromisedProposalNumber == msg.getProposal().getId()) {
       // We expect this proposal to be accepted; save a copy for the remainder
       // of the session.
       activeProposals.put(msg.getProposal().getId(), msg.getProposal());
     }
   }
 
   protected void handleAcceptReply(int from, AcceptReplyMessage msg) {
     ProposalState ps = activeProposalState.get(msg.getProposalId());
     if (ps == null)
       return;
 
     Proposal p = activeProposals.get(ps.proposalId);
     if (ps == null)
       return;
     
     if (msg.getLastAcceptedProposalId() == msg.getProposalId()) {
       addAndMaybeCreateSet(ps.accepts, from);
       
       if (ps.accepts.size() > computeSmallestMajority(networkSize))
         handleProposalAcceptance(ps, p);
 
     } else {
       addAndMaybeCreateSet(ps.rejects, from);
       
       if (ps.rejects.size() >= computeLargestMinority(networkSize))
         handleProposalRejection(ps, p);
     }
   }
 
   protected void handleProposalStatus(int from, ProposalStatusMessage msg) {
     if (log.containsProposal(msg.getProposal().getId()))
       return;
 
     if (msg.getFlags().isSet(ProposalStatus.Accepted) ^
         msg.getFlags().isSet(ProposalStatus.Failed)) {
       System.err.println("Received ProposalStatus message with no interesting flags...");
       return;
     }
 
     ProposalStatus status = (msg.getFlags().isSet(ProposalStatus.Accepted) ?
                              ProposalStatus.Accepted :
                              ProposalStatus.Failed);
 
     ProposalState ps = activeProposalState.remove(msg.getRequestNumber());
     if (ps == null) {
       // Designated Proposer is informing us of a proposal we had nothing to
       // do with...
       log.write(msg.getProposal(), status);
      callProposalCompleteCallback(msg.getProposal(), status);
       log.commit(msg.getProposal().getId());
       return;
     }
 
     if (activeProposals.containsKey(msg.getProposal().getId()))
       throw new IllegalStateException("Received ProposalStatus message for an active proposal...");
 
     
   }
 
   public void onReceive(int from, DFSMessage msg) {
     encounteredNodes.add(from);
 
     switch (msg.getMessageType()) {
     case ProposerNomination:
       handleProposerNomination(from, (ProposerNominationMessage) msg);
       break;
     case QueryNode:
       handleQueryNode(from, (QueryNodeMessage) msg);
       break;
     case PaxosNodeStatus:
       handlePaxosNodeStatus(from, (PaxosNodeStatusMessage) msg);
       break;
     case ProxiedProposalRequest:
       handleProxiedProposalRequest(from, (ProxiedProposalRequestMessage) msg);
       break;
     case PrepareRequest:
       handlePrepareRequest(from, (PrepareRequestMessage) msg);
       break;
     case PrepareResponse:
       handlePrepareResponse(from, (PrepareResponseMessage) msg);
       break;
     case AcceptRequest:
       handleAcceptRequest(from, (AcceptRequestMessage) msg);
       break;
     case AcceptReply:
       handleAcceptReply(from, (AcceptReplyMessage) msg);
       break;
     case ProposalStatus:
       handleProposalStatus(from, (ProposalStatusMessage) msg);
       break;
     default:
       System.err.println("Paxos subsystem received a message it doesn't know " +
                          "how to handle:");
       System.err.println(msg);
     }
   }
 
   // --------------------------[ Callbacks ]------------------------------------
 
   protected void addLeaderElectionSettlingCb() {
     Callback cb = getNewLeaderElectionSettlingCallback();
 
     cb.setParams(new Object[]{lastElectionRoundNumber,
                               getDesiredLeader(),
                               liveNodes.size()});
     owner.addTimeout(cb, kLeaderElectionSettleTime);
   }
 
   protected Callback getNewLeaderElectionSettlingCallback() {
     try {
       Method method = Callback.getMethod("leaderElectionSettlingCb", this, new String[]{ "java.lang.Integer", "java.lang.Integer", "java.lang.Integer" });
       return new Callback(method, this, null);
     } catch (NoSuchMethodException nsme) {
       nsme.printStackTrace();
       throw new IllegalStateException("Should not get here!");
     } catch (ClassNotFoundException cnfe) {
       cnfe.printStackTrace();
       throw new IllegalStateException("Should not get here!");
     }
   }
 
   public void leaderElectionSettlingCb(Integer expectedElectionRoundNumber,
                                        Integer expectedLeader,
                                        Integer expectedLiveNodeSize) {
     if (expectedElectionRoundNumber != lastElectionRoundNumber ||
         expectedLeader != getDesiredLeader() ||
         expectedLiveNodeSize != liveNodes.size())
       return;
 
     if (expectedLeader != owner.addr)
       return;
 
     // We are the expected leader; we need to:
     // - come up to snuff on proposals (performance benefit)
     // - propose ourselves!
     if (lastKnownProposalNumber > lastPromisedProposalNumber) {
       // Somebody else had a transaction we didn't; need to learn what went
       // on...
       sendToAll(liveNodes, new QueryNodeMessage(log.getLastCommittedProposalId()));
       return;
     }
 
     sendLeaderElectionProposal();
   }
 
   // lol fuck you
 }
 
 
