 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04.Presage2Experiments;
 
 import java.io.InvalidClassException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import org.apache.log4j.Level;
 
 import uk.ac.imperial.dws04.Presage2Experiments.RoadAgent.OwnChoiceMethod;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.HasIPConHandle;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConBallotService;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.IPConException;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.ParticipantIPConService;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages.ClusterPing;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.Messages.IPConActionMsg;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.actions.*;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.Chosen;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConAgent;
 import uk.ac.imperial.dws04.Presage2Experiments.IPCon.facts.IPConRIC;
 import uk.ac.imperial.dws04.utils.MathsUtils.MathsUtils;
 import uk.ac.imperial.dws04.utils.misc.MapValueAscComparator;
 import uk.ac.imperial.dws04.utils.record.Pair;
 import uk.ac.imperial.dws04.utils.record.PairBDescComparator;
 import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
 import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
 import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
 import uk.ac.imperial.presage2.core.messaging.Input;
 import uk.ac.imperial.presage2.core.messaging.Performative;
 import uk.ac.imperial.presage2.core.network.Message;
 import uk.ac.imperial.presage2.core.simulator.SimTime;
 import uk.ac.imperial.presage2.core.util.random.Random;
 import uk.ac.imperial.presage2.util.fsm.Action;
 import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
 import uk.ac.imperial.presage2.util.fsm.FSM;
 import uk.ac.imperial.presage2.util.fsm.FSMDescription;
 import uk.ac.imperial.presage2.util.fsm.FSMException;
 import uk.ac.imperial.presage2.util.fsm.StateType;
 import uk.ac.imperial.presage2.util.location.CannotSeeAgent;
 import uk.ac.imperial.presage2.util.location.CellMove;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 /**
  * @author dws04
  * 
  */
 public class RoadAgent extends AbstractParticipant implements HasIPConHandle {
 
 	public enum OwnChoiceMethod {SAFE, PLANNED, SAFE_GOALS};
 	public enum NeighbourChoiceMethod {WORSTCASE, GOALS, INSTITUTIONAL};
 
 	
 	protected Driver driver;
 	protected RoadLocation myLoc;
 	protected int mySpeed;
 	protected final RoadAgentGoals goals;
 	private Integer junctionsLeft;
 	
 	
 	/**
 	 * FSM Stuff
 	 */
 	/**
 	 * Event to trigger the change in an FSM indicating the agent should move to the exit
 	 * @author dws04
 	 *
 	 */
 	private class MoveToExitEvent {
 		MoveToExitEvent(){
 		}
 	}
 	private FSMDescription fsmDesc = FSM.description();
 	private FSM fsm;
 
 	
 	// service variables
 	ParticipantRoadLocationService locationService;
 	ParticipantSpeedService speedService;
 	/*RoadEnvironmentService environmentService;*/
 	
 	ParticipantIPConService ipconService;
 	protected final IPConAgent ipconHandle;
 	
 	IPConBallotService ballotService;
 	/*
 	 * Should be a long from 0-10 representing how likely an agent is to not arrogate this cycle
 	 */
 	private final int startImpatience;
 	private HashMap<String,Integer> impatience;
 	
 	/*
 	 * Collection for RICs - should be populated every cycle with agents that ping
 	 */
 	private HashMap<IPConRIC,ClusterPing> nearbyRICs;
 	private LinkedList<IPConRIC> ricsToJoin;
 	private LinkedList<IPConRIC> ricsToArrogate;
 	private LinkedList<IPConRIC> ricsToResign;
 	private LinkedList<IPConRIC> ricsToLeave;
 	private LinkedList<IPConAction> prospectiveActions;
 	private LinkedList<IPConAction> ipconActions;
 	@SuppressWarnings("rawtypes")
 	private LinkedList<Message> messageQueue;
 	private LinkedList<IPConActionMsg> ownMsgs;
 	/** Issue to Chosen fact **/
 	private HashMap<String,Chosen> institutionalFacts;
 	private OwnChoiceMethod ownChoiceMethod;
 	private NeighbourChoiceMethod neighbourChoiceMethod;
 	 
 	public RoadAgent(UUID id, String name, RoadLocation myLoc, int mySpeed, RoadAgentGoals goals, OwnChoiceMethod ownChoiceMethod, NeighbourChoiceMethod neighbourChoiceMethod) {
 		super(id, name);
 		this.myLoc = myLoc;
 		this.mySpeed = mySpeed;
 		this.goals = goals;
 		if (this.goals.getDest()!=null) {
 			// you want to pass one fewer than your destination
 			this.junctionsLeft = goals.getDest()-1;
 		}
 		else {
 			this.junctionsLeft = null;
 		}
 		this.ipconHandle = new IPConAgent(this.getID(), this.getName());
 		this.startImpatience = (new Long(Math.round(Random.randomDouble()*10))).intValue();
 		this.impatience = new HashMap<String,Integer>();
 		this.nearbyRICs = new HashMap<IPConRIC,ClusterPing>();
 		ricsToJoin = new LinkedList<IPConRIC>();
 		ricsToArrogate = new LinkedList<IPConRIC>();
 		ricsToResign = new LinkedList<IPConRIC>();
 		ricsToLeave = new LinkedList<IPConRIC>();
 		prospectiveActions = new LinkedList<IPConAction>();
 		ipconActions = new LinkedList<IPConAction>();
 		messageQueue = new LinkedList<Message>();
 		ownMsgs = new LinkedList<IPConActionMsg> ();
 		institutionalFacts = new HashMap<String, Chosen>();
 		this.ownChoiceMethod = ownChoiceMethod;
 		this.neighbourChoiceMethod = neighbourChoiceMethod;
 	}
 	
 	public RoadAgent(UUID uuid, String name, RoadLocation startLoc,
 			int startSpeed, RoadAgentGoals goals) {
 		this(uuid, name, startLoc, startSpeed, goals, OwnChoiceMethod.SAFE, NeighbourChoiceMethod.WORSTCASE);
 	}
 
 	/**
 	 * @return the ipconHandle
 	 */
 	public IPConAgent getIPConHandle() {
 		return ipconHandle;
 	}
 
 	@Override
 	protected Set<ParticipantSharedState> getSharedState() {
 		Set<ParticipantSharedState> ss = super.getSharedState();
 		ss.add(ParticipantRoadLocationService.createSharedState(getID(), myLoc));
 		ss.add(ParticipantSpeedService.createSharedState(getID(), mySpeed));
 		// add this here for registration purposes, but don't put a getter anywhere
 		ss.add(ParticipantIPConService.createSharedState(getID(), goals));
 		return ss;
 	}
 	
 	@Override
 	public void initialise() {
 		logger.debug("Initialising RoadAgent " + this.getName() + " / " + this.getID());
 		super.initialise();
 		// get the ParticipantRoadLocationService.
 		try {
 			this.locationService = getEnvironmentService(ParticipantRoadLocationService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}
 		// get the ParticipantRoadSpeedService.
 		try {
 			this.speedService = getEnvironmentService(ParticipantSpeedService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}
 		try {
 			this.driver = new Driver(getID(), this);
 		} catch (UnavailableServiceException e) {
 			e.printStackTrace();
 		}
 		// get the IPConService.
 		try {
 			this.ipconService = getEnvironmentService(ParticipantIPConService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 			e.printStackTrace();
 		}
 		// get the BallotService.
 		try {
 			this.ballotService = getEnvironmentService(IPConBallotService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 			e.printStackTrace();
 		}
 		/*// get the RoadEnvironmentService.
 		try {
 			this.environmentService = getEnvironmentService(RoadEnvironmentService.class);
 		} catch (UnavailableServiceException e) {
 			logger.warn(e);
 		}*/
 		// Init the FSM
 		try {
 			fsmDesc.addState("IDLE", StateType.START)
 				.addState("MOVE_TO_EXIT", StateType.ACTIVE)
 				.addTransition("IDLE_TO_MOVE", new EventTypeCondition(MoveToExitEvent.class), "IDLE", "MOVE_TO_EXIT", Action.NOOP);
 			fsm = new FSM(fsmDesc, null);
 			if ( (junctionsLeft!=null) && (junctionsLeft<=1) ){
 				fsm.applyEvent(new MoveToExitEvent());
 			}
 		} catch (FSMException e) {
 			logger.warn("You can't initialise the FSM like this:" + e);
 		}
 	}
 	
 	@Override
 	public void execute() {
 		// clear temp storage
 		this.nearbyRICs.clear();
 		
 		// pull in Messages from the network
 		enqueueInput(this.network.getMessages());
 		
 		// insert msgs from self (since broadcasts don't self-deliver)
 		this.inputQueue.addAll(ownMsgs);
 		ownMsgs.clear();
 		
 		// check inputs
 		logger.trace(getID() + " has msgs:" + this.inputQueue);
 
 		// process inputs
 		while (this.inputQueue.size() > 0) {
 			this.processInput(this.inputQueue.poll());
 		}
 		
 		
 		/*
 		 * Get physical state
 		 */
 		myLoc = (RoadLocation) locationService.getAgentLocation(getID());
 		mySpeed = speedService.getAgentSpeed(getID());
 		Integer junctionDist = this.locationService.getDistanceToNextJunction();
 		final Collection<IPConRIC> currentRICs = ipconService.getCurrentRICs();
 		
 		/*
 		 * Retrieve (in case we want to change them...) macrogoals
 		 * FIXME TODO 
 		 */
 		
 		/*
 		 * Throw a warning if in more than one cluster, because that doesn't seem to be too useful
 		 */
 		Boolean inMultipleClusters = checkClusterMembership();
 		if (inMultipleClusters) {
 			logger.warn(getID() + " is in multiple clusters ! : " + ipconService.getCurrentRICs(getIPConHandle()));
 		}
 		
 		// check for vehicles infront/behind you with matching issues and compatible values
 		// look in nearbyRICs - possible to get location of those vehicles ? clusterpings are range-less...
 		for (Entry<IPConRIC,ClusterPing> entry : this.nearbyRICs.entrySet()) {
 			
 			
 			// FIXME TODO should remove duplicates around here somewhere
 			
 			
 			
 			
 			// if youre not in the cluster and it has a chosen value
 			if (!currentRICs.contains(entry.getKey()) && entry.getValue().getValue()!=null ) {
 				// try to get their location - if you can, then they're close enough (throwing away the result)
 				locationService.getAgentLocation(entry.getValue().getFrom().getId());
 				int join = 0;
 				int stay = 0;
 				Collection<IPConRIC> clusterRICs = ipconService.getRICsInCluster(entry.getKey().getCluster());
 				// for (rics in cluster)
 				for (IPConRIC ricInCluster : clusterRICs) {
 				// checkAcceptability of chosen value in ric
 					logger.debug(getID() + " is considering " + ricInCluster);
 					if (isPreferableToCurrent(ricInCluster, ipconService.getChosen(ricInCluster.getRevision(), ricInCluster.getIssue(), ricInCluster.getCluster()))) {
 					//if (checkAcceptability(ricInCluster, ipconService.getChosen(ricInCluster.getRevision(), ricInCluster.getIssue(), ricInCluster.getCluster()))) {
 						// if true, increment "join"
 						join++;
 						logger.debug(getID() + " incremented in favour of " + ricInCluster + " - " + join + ":" + stay);
 					}
 					else {
 						// if false, increment stay
 						stay++;
 						logger.debug(getID() + " incremented against " + ricInCluster + " - " + join + ":" + stay);
 					}
 				// end if
 				} // end for
 				if (join>stay) {
 					logger.debug(getID() + " found that it should join cluster " + entry.getKey().getCluster());
 					// FIXME TODO fix this to take rics out of consideration for rest of cycle because youre about to leave it
 					// and also to join all of the RICs in the cluster, not just the one you got the msg from...
 					for (IPConRIC ricInCluster : clusterRICs) {
 						// ricsToLeave should remove yourself from the cluster the RIC is in, not just the RIC
 						ricsToLeave.addAll(getRICsForIssue(ricInCluster.getIssue()));
 						ricsToJoin.add(ricInCluster);
 					}
 				}
 			}
 		}
 					
 		
 		
 		for (IPConRIC ric : currentRICs) {
 			Chosen value = getChosenFact(ric.getRevision(), ric.getIssue(), ric.getCluster());
 			if (value!=null) {
 				// "null" should never be chosen as a value, so we can do this ?
 				institutionalFacts.put(ric.getIssue(), value);
 				logger.trace(getID() + " thinks " + value.getValue() + " has been chosen in " + ric);
 			}
 			else {
 				// "null" should never be chosen as a value, so we can do this ?
 				logger.trace(getID() + " thinks there is no chosen value in " + ric + ", but has " + institutionalFacts.get(ric) + " in memory.");
 			}
 			// Check for leaders
 			ArrayList<IPConAgent> leaders = ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster());
 			// no leaders, maybe arrogate?
 			if (leaders==null) {
 				logger.trace(getID() + " is in RIC " + ric + " which has no leader(s), so is becoming impatient to arrogate (" + getImpatience(ric.getIssue()) + " cycles left).");
 				if (!ricsToArrogate.contains(ric) && isImpatient(ric.getIssue())) {
 					ricsToArrogate.add(ric);
 				}
 				// update impatience whether or not you were impatient (will reset if you were impatient)
 				updateImpatience(ric.getIssue());
 			}
 			else {
 				// multiple leaders and you're one of them
 				if ( leaders.size()>1 && leaders.contains(getIPConHandle()) ) {
 					leaders.remove(leaders.indexOf(getIPConHandle()));
 					for (IPConAgent leader : leaders) {
 						if (leaderIsMoreSenior(leader)) {
 							ricsToResign.add(ric);
 						}
 					}
 				}
 				// else (1 or more leaders and you're not one, or you're the only leader) do nothing
 			}
 		}
 		// For all goals
 		// check if represented by an issue in any RIC youre in
 		// if not,
 		// Check other RICs in cluster
 		// - Join if they do
 		// - arrogate otherwise
 		// - Check if a nearby cluster has issue
 		// - - Join if yes
 		// - - Arrogate if no (always want to be in a RIC)
 		for (String issue : getGoalMap().keySet()) {
 			Boolean found = false;
 			Boolean foundInCluster = false;
 			for (IPConRIC ric : currentRICs) {
 				if (!found && ric.getIssue().equalsIgnoreCase(issue)) {
 					found = true;
 					logger.trace(getID() + " is in a RIC (" + ric + ") for " + issue + ").");
 				}
 			}
 			if (!found) {
 				logger.trace(getID() + " could not find a RIC for " + issue + " so will check RICs in current cluster(s).");
 				for (IPConRIC ric : currentRICs) {
 					Collection<IPConRIC> inClusterRICs = ipconService.getRICsInCluster(ric.getCluster());
 					for (IPConRIC ric1 : inClusterRICs) {
 						if (!foundInCluster && ric1.getIssue().equalsIgnoreCase(issue)) {
 							foundInCluster = true;
 							logger.trace(getID() + " found RIC in current cluster (" + ric1 + ") for " + issue + " so will join it.");
 							ricsToJoin.add(ric1);
 							break;
 						}
 					}
 				}
 				if (!foundInCluster) {
 					logger.trace(getID() + " could not find a RIC to join for " + issue + /*" and is impatient " +*/ " so will arrogate.");
 					// Make a RIC to arrogate
 					// I = issue
 					// C = cluster you are in, if in one
 					// R = ?
 					UUID cluster = null;
 					if (!institutionalFacts.isEmpty()) {
 						// pick a very-psuedo-random cluster you're already in
 						cluster = institutionalFacts.entrySet().iterator().next().getValue().getCluster();
 						logger.trace(getID() + " arrogating in existing cluster " + cluster);
 					}
 					else {
 						// check the clusters you're about to join/arrogate
 						HashSet<IPConRIC> set = new HashSet<IPConRIC>();
 						set.addAll(ricsToArrogate);
 						set.addAll(ricsToJoin);
 						if (!set.isEmpty()) {
 							cluster = set.iterator().next().getCluster();
 							logger.trace(getID() + " arrogating in to-be-joined cluster " + cluster);
 						}
 						else {
 							// pick a psuedo-random cluster that doesn't exist yet
 							cluster = Random.randomUUID();
 							logger.trace(getID() + " arrogating new cluster " + cluster);
 						}
 					}
 					IPConRIC newRIC = new IPConRIC(0, issue, cluster);
 					ricsToArrogate.add(newRIC);
 					resetImpatience(issue);
 				}
 				else {
 					// found a RIC in a cluster youre in, and joined it already
 				}
 				logger.trace(getID() + " found a RIC to join for " + issue);				
 			}
 			// else do stuff for RICs youre in
 			// check for chosen values - if there is nothing chosen then do stuff with impatience and think about proposing/leaving/etc
 			// if the nearby clusters have the same (or an as-acceptable) value for your issues, then join(merge) ?
 			/*if (!foundInCluster) {
 				logger.trace(getID() + " could not find a RIC for " + issue + " so will check nearby clusters.");
 				Collection<IPConRIC> nearbyRICs = getNearbyRICs();
 				for (IPConRIC nearbyRIC : nearbyRICs) {
 					if (!foundNearby && nearbyRIC.getIssue().equalsIgnoreCase(issue)) {
 						foundNearby = true;
 						logger.trace(getID() + " found a nearby RIC (" + nearbyRIC + ") for " + issue + " so will join it.");
 						ricsToJoin.add(nearbyRIC);
 					}
 				}
 			}*/
 			
 			
 		}
 		
 		
 		
 		/*
 		 * Arrogate in all RICS you need to
 		 */
 		while (!ricsToArrogate.isEmpty()) {
 			IPConRIC ric = ricsToArrogate.removeFirst();
 			ArrogateLeadership act = new ArrogateLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		/*for (IPConRIC ric : ricsToArrogate) {
 			ArrogateLeadership act = new ArrogateLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}*/
 		
 		/*
 		 * Join all RICS you need to
 		 */
 		while (!ricsToJoin.isEmpty()) {
 			IPConRIC ric = ricsToJoin.removeFirst();
 			JoinAsLearner act = new JoinAsLearner(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		/*for (IPConRIC ric : ricsToJoin) {
 			JoinAsLearner act = new JoinAsLearner(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}*/
 		
 		/*
 		 * Resign leadership in all RICs you should
 		 */
 		while (!ricsToResign.isEmpty()) {
 			IPConRIC ric = ricsToResign.removeFirst();
 			ResignLeadership act = new ResignLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		/*for (IPConRIC ric : ricsToResign) {
 			ResignLeadership act = new ResignLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}*/
 		
 		/*
 		 * Leave all RICs you should
 		 * FIXME TODO this is probably bad, since most will duplicate clusters ?
 		 * Also you might think that you want to do stuff in these clusters before you leave, so...
 		 * (ie, you will leave a cluster after doing stuff in RICs inside it because you only knew you would leave one RIC)
 		 */
 		while (!ricsToLeave.isEmpty()) {
 			IPConRIC ric = ricsToLeave.removeFirst();
 			LeaveCluster act = new LeaveCluster(getIPConHandle(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		
 		/*
 		 * Get obligations
 		 * Get permissions
 		 * Use permissions to instantiate obligations
 		 * Do something sensible from your permissions (eg responses if you didn't vote yet, and voting itself !)
 		 * Check for conflicting obligations/permissions
 		 * Take note of permission to vote
 		 * Add all relevant actions to queue of actions
 		 * FIXME TODO
 		 */
 		LinkedList<IPConAction> obligatedActions = getInstatiatedObligatedActionQueue();
 		//TODO FIXME probably don't want to do this, but for the time being...
 		while(!obligatedActions.isEmpty()) {
 			ipconActions.add(obligatedActions.removeFirst());
 		}
 		
 		// deal with prospective actions
 		while (!prospectiveActions.isEmpty()) {
 			IPConAction act = instantiateProspectiveAction(prospectiveActions.removeFirst());
 			if (act!=null) {
 				ipconActions.add(act);
 			}
 			// else do nothing
 		}
 		
 		/*
 		 * Derive microgoals to fulfil macrogoals
 		 *  - take into account distance to exit
 		 *  - time to get to exit
 		 *  - fuel economy ?
 		 *  - IPCon agreed speed etc
 		 * Reason actions to fulfil microgoals
 		 * Check for conflicts
 		 * All all relevant actions to queue of actions
 		 * FIXME TODO
 		 */
 		
 		
 
 		/*
 		 * Do all IPConActions
 		 */
 		while(!ipconActions.isEmpty()) {
 			messageQueue.add(generateIPConActionMsg(ipconActions.removeFirst()));
 		}
 		/*for (IPConAction act : ipconActions) {
 			messageQueue.add(generateIPConActionMsg(act));
 		}*/
 		
 		/*
 		 * Generate broadcast msgs indicating which RICs you're in
 		 */
 		for (Entry<String,Chosen> entry : institutionalFacts.entrySet()) {
 			IPConRIC ric = new IPConRIC(entry.getValue().getRevision(), entry.getValue().getIssue(), entry.getValue().getCluster());
 			messageQueue.add(
 					new ClusterPing(
 							Performative.INFORM, getTime(), network.getAddress(), 
 							new Pair<RoadLocation,Pair<IPConRIC,Object>>( myLoc, new Pair<IPConRIC,Object>(ric,entry.getValue()) )
 					)
 			);
 		}
 		
 		/*
 		 * Send all messages queued
 		 */
 		//for (Message msg : messageQueue) {
 		while (!messageQueue.isEmpty()) {
 			sendMessage(messageQueue.removeFirst());
 		}
 		
 		
 	 
 		logger.info("[" + getID() + "] My location is: " + this.myLoc + 
 										", my speed is " + this.mySpeed + 
 										", my goalSpeed is " + this.goals.getSpeed() + 
 										", and I have " + junctionsLeft + " junctions to pass before my goal of " + goals.getDest() +
 										", so I am in state " + fsm.getState());
 		logger.info("I can see the following agents:" + locationService.getNearbyAgents());
 		saveDataToDB();
 
 		
 		CellMove move;
 		// Check to see if you want to turn off, then if you can end up at the junction in the next timecycle, do so
 		if (	(fsm.getState().equals("MOVE_TO_EXIT")) && (junctionDist!=null) ) {
 			//move = driver.turnOff();
 			move = createExitMove(junctionDist, neighbourChoiceMethod);
 		}
 		else {
 			//move = createMove(ownChoiceMethod, neighbourChoiceMethod);
 			move = newCreateMove(ownChoiceMethod, neighbourChoiceMethod);
 		}
 		if ((junctionDist!=null) && (junctionDist <= move.getYInt())) {
 			passJunction();
 		}
 		submitMove(move);
 	}
 		
 	private boolean isPreferableToCurrent(IPConRIC ric, Object value) {
 		if (value instanceof Chosen) {
 			value = ((Chosen) value).getValue();
 		}
 		try {
 			// if other cluster is tolerable and current isnt, then switch
 			if (isWithinTolerance(ric.getIssue(), value) && !isWithinTolerance(ric.getIssue(), institutionalFacts.get(ric.getIssue()))) {
 				return true;
 			}
 			// if current and other cluster are both tolerable... compare leader seniority
 			else if (isWithinTolerance(ric.getIssue(), value) && isWithinTolerance(ric.getIssue(), institutionalFacts.get(ric.getIssue()))) {
 				// check the other leader's seniority
 				IPConAgent currentLead = null;
 				ArrayList<IPConAgent> leads = ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster());
 				for (IPConAgent lead : leads) {
 					// if the leader is more senior (so you might join) 
 					if (leaderIsMoreSenior(lead, currentLead)) {
 						return true;
 						// break out of loop with return, so you only add one at most
 					}
 				}
 				return false; // if none of them are more senior then don't join
 			}
 		} catch (InvalidClassException e) {
 			// TODO Auto-generated catch block
 			logger.debug(e);
 			return false;
 		}
 		// return false if you except or something else goes wrong
 		return false;
 	}
 
 	/**
 	 * @param ric
 	 * @param value
 	 * @return true if the RIC and its chosen value is acceptable, so you may wish to join it
 	 */
 	private boolean checkAcceptability(IPConRIC ric, Object value) {
 		try {
 			if (value instanceof Chosen) {
 				value = ((Chosen) value).getValue();
 			}
 			if (isWithinTolerance(ric.getIssue(), value)) {
 				// the cluster has a chosen value and the agent is "close" - check the leader's seniority
 				ArrayList<IPConAgent> leads = ipconService.getRICLeader(ric.getRevision(), ric.getIssue(), ric.getCluster());
 				for (IPConAgent lead : leads) {
 					// if the leader is more senior (so you might join) 
 					if (leaderIsMoreSenior(lead)) {
 						return true;
 					}
 					else {
 						return false;
 					}
 				}
 			}
 			else {
 				return false;
 			}
 		}
 		catch (InvalidClassException e) {
 			return false;
 		}
 		catch (CannotSeeAgent e) {
 			return false;
 		}
 		return false;
 	}
 
 	private ArrayList<IPConRIC> getRICsForIssue(String issue) {
 		ArrayList<IPConRIC> ricsForIssue = new ArrayList<IPConRIC>();
 		for (IPConRIC ric : ipconService.getCurrentRICs(getIPConHandle())) {
 			if (ric.getIssue().equalsIgnoreCase(issue)) {
 				ricsForIssue.add(ric);
 			}
 		}
 		return ricsForIssue;
 	}
 
 	private Boolean checkClusterMembership() {
 		ArrayList<UUID> clusters = new ArrayList<UUID>();
 		for (IPConRIC ric : ipconService.getCurrentRICs(getIPConHandle())) {
 			clusters.add(ric.getCluster());
 		}
 		return ( (!clusters.isEmpty()) && (clusters.size()==1) );
 	}
 
 	private Collection<IPConRIC> getNearbyRICs() {
 		return this.nearbyRICs.keySet();
 	}
 
 	/**
 	 * @return map of goals <value,tolerance>
 	 */
 	protected HashMap<String, Pair<Integer, Integer>> getGoalMap() {
 		return getGoals().getMap();
 	}
 	
 	private RoadAgentGoals getGoals() {
 		return this.goals;
 	}
 	
 	private Integer getImpatience(String issue) {
 		if (!impatience.containsKey(issue)) {
 			setImpatience(issue, startImpatience);
 		}
 		return impatience.get(issue);
 	}
 	
 	private void setImpatience(String issue, Integer value) {
 		impatience.put(issue,value);
 	}
 
 	/**
 	 * Determines whether or not the agent may arrogate in this cycle.
 	 * Should be defined so that all agents do not try to arrogate the same
 	 * thing at the same time.
 	 */
 	private boolean isImpatient(String issue) {
 		return (getImpatience(issue)==0);
 	}
 	
 	/**
 	 * Call this after checking for impatience !
 	 * 
 	 * Should only change when the agent has something
 	 * to be impatient about, rather than every cycle, but it's really just
 	 * so that agents don't all arrogate the same RIC at once.
 	 * 
 	 * FIXME TODO should reset impatience when nothing to be impatient about
 	 */
 	private void updateImpatience(String issue) {
 		if (getImpatience(issue)==null) {
 			resetImpatience(issue);
 		}
 		setImpatience(issue, getImpatience(issue)-1);
 		if (getImpatience(issue)<0) {
 			resetImpatience(issue);
 		}
 	}
 	
 	/**
 	 * Resets impatience to the startImpatience (when agent has nothing to be impatient about)
 	 */
 	private void resetImpatience(String issue) {
 		setImpatience(issue, startImpatience);
 	}
 	
 	/**
 	 * @param leader IPConAgent to compare to
 	 * @return true if the given agent is more senior than you (so you should resign), false otherwise.
 	 */
 	private boolean leaderIsMoreSenior(IPConAgent leader) {
 		 return ( leader.getIPConID().compareTo(getIPConHandle().getIPConID()) == 1); 
 	}
 	
 	/**
 	 * @param leader1
 	 * @param leader2
 	 * @return true if leader1 is more senior than leader2
 	 */
 	private boolean leaderIsMoreSenior(IPConAgent leader1, IPConAgent leader2) {
 		 return ( leader1.getIPConID().compareTo(leader2.getIPConID()) == 1); 
 	}
 
 	/**
 	 * FIXME TODO should probably not just get this from IPCon (that's sort of cheating)
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return the Chosen IPConFact for the RIC specified.
 	 */
 	private Chosen getChosenFact(Integer revision, String issue, UUID cluster) {
 		return ipconService.getChosen(revision, issue, cluster);
 	}
 	
 	/**
 	 * 
 	 * @param issue name of issue to be checked
 	 * @param value value to be checked
 	 * @return true if the given value is within the tolerance for the goal relating to the given issue, null if no goal for that issue, false otherwise (or if tested value was null, since null cannot be a goal)
 	 * @throws InvalidClassException if value is the wrong type
 	 */
 	protected Boolean isWithinTolerance(String issue, Object value) throws InvalidClassException {
 		if (value==null) {
 			return false;
 		}
 		else if (!(value instanceof Integer)) {
 			throw new InvalidClassException("Only integer goals are supported. Value was a " + value.getClass());
 		}
 		else {
 			if (!this.getGoalMap().containsKey(issue)){
 				return null;
 			}
 			else {
 				Pair<Integer, Integer> pair = this.getGoalMap().get(issue);
 				return Math.abs(((Integer)value)-pair.getA())<=pair.getB();
 			}
 		}
 	}
 	
 
 	/**
 	 * Instantiates any nulls in prospective actions and checks given values are permitted.
 	 * Only works for Response1B and Vote2B
 	 * @param prospectiveAction Should be a Response1B or Vote2B
 	 * @return fully instantiated action, or null if no permitted instantiation could be found
 	 */
 	private IPConAction instantiateProspectiveAction(final IPConAction prospectiveAction) {
 		ArrayList<IPConAction> permissions = new ArrayList<IPConAction>();
 		IPConAction result = null;
 		// Get permissions for class
 		for (IPConAction per : ipconService.getPermissions(getIPConHandle(), null, null, null)) {
 			String type = per.getClass().getSimpleName();
 			if (type.equalsIgnoreCase(prospectiveAction.getClass().getSimpleName())) {
 				permissions.add(per);
 			}
 		}
 		// if no permissions
 		if (permissions.isEmpty()) {
 			logger.warn(getID() + " is not permitted to " + prospectiveAction + " !");
 			result = null;
 		}
 		else {
 			logger.trace(getID() + " found permissions " + permissions);
 			// if only one option
 			if (permissions.size()==1) {
 				result = instantiateFieldsForProspectivePermission(prospectiveAction, permissions.get(0));
 			}
 			else {
 				// more than one permission
 				// get all the valid instantiations (discard nulls)
 				ArrayList<IPConAction> instantiations = new ArrayList<IPConAction>();
 				for (IPConAction per : permissions) {
 					IPConAction inst = instantiateFieldsForProspectivePermission(prospectiveAction, per);
 					if (inst!=null) {
 						instantiations.add(inst);
 					}
 				}
 				if (instantiations.isEmpty()) {
 					logger.warn(getID() + " could not get any valid actions from " + prospectiveAction);
 					result = null;
 				}
 				else {
 				// if you passed it a fully instantiated permitted set of values, just do that.
 				// (this will probably be the most used option...)
 					if (instantiations.contains(prospectiveAction)) {
 						logger.trace("Passed in action " + prospectiveAction + " was valid, so using that.");
 						result = prospectiveAction;
 					}
 					else {
 						// do something different depending on what action it is.
 						String actionType = prospectiveAction.getClass().getSimpleName();
 						if (actionType.equalsIgnoreCase("Response1B")) {
 							// should never be multiple options
 							if (instantiations.size()!=1) {
 								logger.warn(getID() + " had multiple options for Response1B (" + instantiations + ") so pseudorandomly choosing " + instantiations.get(0));
 							}
 							result = instantiations.get(0);
 						}
 						else if (actionType.equalsIgnoreCase("Vote2B")) {
 							logger.warn(getID() + " didn't pass in a fully instantiated Vote2B (" + prospectiveAction + ") so pseudorandomly choosing " + instantiations.get(0));
 							result = instantiations.get(0);
 						}
 						else {
 							logger.warn(getID() + " cannot instantiate prospective actions of type " + actionType + " so psuedorandomly choosing " + instantiations.get(0));
 							result = instantiations.get(0);
 						}
 					}
 				}
 			}
 		}
 		logger.trace(getID() + " instantiated " + result + " from " + prospectiveAction);
 		return result;
 	}
 
 	/**
 	 * @param prospectiveAction
 	 * @param permission
 	 * @return instantiated action, or null if no permitted instantiation could not be found
 	 */
 	private IPConAction instantiateFieldsForProspectivePermission( final IPConAction prospectiveAction, final IPConAction permission) {
 		ArrayList<Pair<Field,Object>> fieldVals = new ArrayList<Pair<Field,Object>>();
 		IPConAction result = prospectiveAction.copy();
 		for (Field f : permission.getClass().getFields()) {
 			try {
 				// if the field is null, get the permitted value
 				if (f.get(result)==null) {
 					fieldVals.add(new Pair<Field,Object>(f,f.get(permission)));
 				}
 				else {
 					// if the field is not null, check it is actually permitted
 					if (! ( f.get(result).equals(f.get(permission) ) ) ) {
 						logger.warn(getID() + " wanted to use " + f.get(result) + " for " + f.getName() + " which is not permitted! (should never happen)");
 						return null;
 					}
 				}
 			} catch (IllegalArgumentException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		if (!fieldVals.isEmpty()) {
 			logger.trace(getID() + " has to instantiate : " + fieldVals);
 			// if there were any nulls in result...
 			// check there are no nulls in fieldVals
 			Boolean nulls = false;
 			
 			for (Pair<Field,Object> pair : fieldVals) {
 				nulls = (nulls || ( pair.getB() == null ) );
 			}
 			// if none then go for it
 			if (!nulls) {
 				// fill in nulls in result
 				for (Pair<Field,Object> pair : fieldVals) {
 					try {
 						pair.getA().set(result, pair.getB());
 					} catch (IllegalArgumentException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (IllegalAccessException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 			// if there are nulls, then... ? (should never happen)
 			else {
 				logger.warn(getID() + " found nulls in their permission (" + permission + ") when trying to instantiate " + prospectiveAction);
 				result = null;
 			}
 		}
 		else {
 			logger.trace(getID() + " has nothing to do to instantiate " + result);
 		}
 		return result;
 	}
 
 	public LinkedList<IPConAction> TESTgetInstantiatedObligatedActionQueue() {
 		return getInstatiatedObligatedActionQueue();
 	}
 	
 	private LinkedList<IPConAction> getInstatiatedObligatedActionQueue() {
 		HashSet<IPConAction> obligations = (HashSet<IPConAction>) ipconService.getObligations(ipconHandle, null, null, null);
 		HashSet<IPConAction> permissions = (HashSet<IPConAction>) ipconService.getPermissions(ipconHandle, null, null, null);
 		HashMap<String, ArrayList<IPConAction>> perMap = new HashMap<String, ArrayList<IPConAction>>();
 		LinkedList<IPConAction> queue = new LinkedList<IPConAction>();
 		
 		// Split permissions by class
 		for (IPConAction per : permissions) {
 			String type = per.getClass().getSimpleName();
 			if (!perMap.containsKey(type)) {
 				perMap.put(type, new ArrayList<IPConAction>());
 			}
 			perMap.get(type).add(per);
 		}
 		
 		// check to see if you have a permission for each obligation, if you do then fill in any nulls
 		for (IPConAction obl : obligations) {
 			logger.trace(getID() + " is attempting to discharge their obligation to " + obl);
 			// Make your proto-action
 			IPConAction actToDo = null;
 			/* Make sure you have permission to do it,
 			 * and dbl check you really do,
 			 * and make sure they're actually the same class, since we'll be reflecting
 			 */
 			if (!(perMap.containsKey(obl.getClass().getSimpleName()))) logger.trace("perMap does not contains right classname");
 			if (!(!perMap.get(obl.getClass().getSimpleName()).isEmpty())) logger.trace("perMap match is empty");
 			if (!(perMap.get(obl.getClass().getSimpleName()).get(0).getClass().isAssignableFrom(obl.getClass()))) {
 				logger.trace("perMap match (" + perMap.get(obl.getClass().getSimpleName()).get(0) + ") has class " + 
 						perMap.get(obl.getClass().getSimpleName()).get(0).getClass() + ", which is not assignable from " + obl.getClass());
 			}
 			
 			
 			if (	(perMap.containsKey(obl.getClass().getSimpleName())) &&
 					(!perMap.get(obl.getClass().getSimpleName()).isEmpty()) &&
 					(perMap.get(obl.getClass().getSimpleName()).get(0).getClass().isAssignableFrom(obl.getClass())) ) {
 				
 				actToDo = obl.copy();
 				Field[] fields = obl.getClass().getFields();
 				for (Field f : fields) {
 					logger.trace(getID() + " currently has " + actToDo);
 					// Chop down the string of the field to something more readable
 					String s = f.toString();
 					String delims = "[.]+"; // use + to treat consecutive delims as one, omit to treat consecutive delims separately
 					String[] tokens = s.split(delims);
 					String fName = (Arrays.asList(tokens)).get(tokens.length-1);
 					logger.trace(getID() + " checking field " + fName + " in " + obl);
 					Object fOblVal = null;
 					try {
 						fOblVal = f.get(obl);
 						logger.trace(getID() + " found the value of field " + fName + " in " + obl + " to be " + fOblVal);
 					} catch (Exception e) {
 						logger.error(getID() + " had a problem extracting the fields of an obligation (this should never happen !)" + obl + "...");
 						e.printStackTrace();
 					}
 					if (fOblVal==null) {
 						logger.trace(getID() + " found a null field (" + fName + ") in " + obl);
 						// Go through the permitted values, and get all non-null ones
 						ArrayList<Object> vals = new ArrayList<Object>();
 						ArrayList<IPConAction> perList = perMap.get(obl.getClass().getSimpleName());
 						for (IPConAction act : perList) {
 							Object fActVal = null;
 							try {
 								fActVal = f.get(act);
 								logger.trace(getID() + " found the value of field " + fName + " in " + act + " to be " + fActVal);
 							} catch (Exception e) {
 								logger.error(getID() + " had a problem extracting the fields of an action (this should never happen !)" + act + "...");
 								e.printStackTrace();
 							}
 							if (fActVal!=null) {
 								vals.add(fActVal);
 							}
 						}
 						
 						// Take the permitted actions and choose one to instantiate with
 						instantiateFieldInObligatedAction(f, actToDo, obl, vals);
 						
 					}
 				}
 			}
 			else {
 				logger.warn(getID() + " is not permitted to discharge its obligation to " + obl + " (this should never happen)!");
 			}
 			queue.add(actToDo);
 		}
 		return queue;
 	}
 	
 	/**
 	 * Sets the field f in actToDo to fulfil the obligation obl depending on your permitted values vals
 	 * @param f the field to be filled in
 	 * @param actToDo copy of the obligation, possibly with some nulls filled in
 	 * @param obl the actual obligation, with nulls
 	 * @param vals the permitted values for f. If empty, indicates that the agent is permitted to use any value (though they might not all make sense !)
 	 */
 	private void instantiateFieldInObligatedAction(final Field f, IPConAction actToDo, final IPConAction obl, final ArrayList<Object> vals) {
 
 		// Make it humanreadable
 		String[] tokens = f.toString().split("[.]+");
 		String fName = (Arrays.asList(tokens)).get(tokens.length-1);
 		// If all the permissions are also null, then you can do anything so pick something at random :P 
 		// (You know there are permissions because we previously checked against the list of permissions being empty)
 		if (vals.size()==0) {
 			// FIXME TODO 
 			logger.trace(getID() + " is not constrained by permission on what to set the value of field " + fName + " to be in " + actToDo);
 			if (fName.equals("ballot")) {
 				// choose a valid ballot number
 				/*
 				 * Possible situations where it will be null:
 				 * Prepare1A - need to pick a ballot number that is higher than all current ballot numbers in the same RIC
 				 * Can either rely on responses to tell you to retry with a higher ballot (obligation not implemented yet)
 				 * or pull highest vote/pre_vote/open_vote for RIC and get highest ballot, then add some value
 				 *  ( TODO ballot number is unique unless an error is encountered )
 				 */
 				Integer bal = null;
 				try {
 					if (!obl.getClass().isAssignableFrom(Prepare1A.class)) {
 						throw new IPConException("Obligation was not to Prepare1A. Class was: " + obl.getClass().getSimpleName());
 					}
 					Integer revision = (Integer)(obl.getClass().getField("revision").get(obl));
 					String issue = (String)(obl.getClass().getField("issue").get(obl));
 					UUID cluster = (UUID)(obl.getClass().getField("cluster").get(obl));
 					
 					bal = ballotService.getNext(revision, issue, cluster);
 					
 					/*Pair<Integer, Integer> pair = ipconService.getHighestRevisionBallotPair(issue, cluster);
 					// If we found some valid ones but not in the right revision, then throw an exception anyway
 					if (pair.getA()!=revision) {
 						// FIXME technically we should check for higher revisions and adjust based on that, 
 						// but you would hope that you never get obligated to do something in an old revision...
 						throw new IPConException("Only found ballots in the wrong revision. Highest was " + pair);
 					}
 					else {
 						// If you found one, then add one to the ballot :D
 						bal = pair.getB()+1;
 					}*/
 				} catch (IPConException e) {
 					// FIXME TODO technically this should guarantee uniqueness
 					// no valid votes, so just go with 0
 					logger.trace(getID() + " couldn't find any ballots so is picking 0 due to error: " + e);
 					bal = 0;
 				} catch (Exception e) {
 					// FIXME TODO technically this should guarantee uniqueness
 					// from the getFields... something went wrong...
 					logger.trace(getID() + " had a problem ( " + e + " ) getting the issue or cluster from " + obl + " so is picking a ballot of 0...");
 					bal = 0;
 				}
 				try {
 					logger.trace(getID() + " set the value of field " + fName + " to be " + bal + " in " + actToDo);
 					f.set(actToDo, bal);
 					logger.trace(getID() + " now has " + actToDo);
 				} catch (Exception e) {
 					logger.error(getID() + " had a problem setting the fields of an action to discharge " + obl + "...");
 					e.printStackTrace();
 				}
 			}
 			// biiiiiig if statement...
 			else if ( (fName.equals("value")) ||
 				// pick one - from your goals ?
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! (value is constrained to one of two options by permissions in SyncAck)
 				 */
 			
 				(fName.equals("agent")) ||
 				// pick an agent to act on
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! (constrained by permission to be yourself, or in case of leader/agent difference, you should never be obligated to do something to *just anyone*)
 				 */
 			
 				(fName.equals("leader")) ||
 				// this should probably be yourself
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! (constrained by permission to be yourself)
 				 */
 			
 				(fName.equals("revision")) ||
 				// pick a revision - probably this one ?
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Always have a specific revision in mind when an obligation is formed....
 				 * If we expand to non-obligated actions, then permissions in general can be null... (eg arrogate)
 				 */
 			
 				(fName.equals("issue")) ||
 				// pick an issue - probably this one ?
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Always have a specific issue in mind when an obligation is formed....
 				 * If we expand to non-obligated actions, then permissions in general can be null... (eg arrogate)
 				 */
 			
 				(fName.equals("cluster")) ||
 				// pick a cluster - probably this one ?
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Always have a specific cluster in mind when an obligation is formed....
 				 * If we expand to non-obligated actions, then permissions in general can be null... (eg arrogate)
 				 */
 			
 				(fName.equals("voteBallot")) ||
 				// pick one
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Will either be 0 (if you didn't vote yet) or the ballot you voted in...
 				 */
 			
 				(fName.equals("voteRevision")) ||
 				// pick one
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Will either be the current revision (if you didn't vote yet) or the revision you voted in...
 				 */
 			
 				(fName.equals("voteValue")) ||
 				// pick one
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! Will either be IPCNV.val() (if you didn't vote yet) or the value you voted for...
 				 */
 				
 				(fName.equals("role")) ) {
 				// pick one
 				/*
 				 * Possible situations where it will be null:
 				 * Should be never ! No obligated actions concern roles...
 				 */
 				logger.warn(getID() + " encountered a null \"" + fName + "\" field, which should never happen! Obligation was " + obl);
 			}
 			else {
 				logger.warn(getID() + " encountered the unrecognised field \"" + fName + "\" in " + obl);
 			}
 		}
 		// If there is only one then use that.
 		else if (vals.size()==1) {
 			try {
 				logger.trace(getID() + " set the value of field " + fName + " to be " + vals.get(0) + " in " + actToDo);
 				f.set(actToDo, vals.get(0));
 				logger.trace(getID() + " now has " + actToDo);
 			} catch (Exception e) {
 				logger.error(getID() + " had a problem setting the fields of an action to discharge " + obl + "...");
 				e.printStackTrace();
 			}
 		}
 		// If there is more than one, pick one at random ?
 		else {
 			if (fName.equals("value")) {
 				Object val = null;
 				try {
 					// choose a valid response
 					/*
 					 * Only possible in a Syncack - two options of IPCNV.val or the value proposed:
 					 * 
 					 */
 	
 					if (!obl.getClass().isAssignableFrom(SyncAck.class)) {
 						throw new IPConException("Obligation was not to SyncAck. Class was: " + obl.getClass().getSimpleName());
 					}
 					if ((vals.size()!=2) || (!vals.contains(IPCNV.val())) ){
 						logger.warn(getID() + " encountered too many, or unexpected, options than usual for a SyncAck (" + vals + ") so is psuedorandomly picking " + vals.get(0));
 						val = vals.get(0);
 					}
 					else {
 						// FIXME TODO other descision-making stuff here...
 						
 						// choose between ipcnv and the given value
 						/*
 						 * Need to work out what the issue is, which goal that corresponds to, and then
 						 * decide whether the given value is close enough to your goal to be acceptable.
 						 * If not (or if you can't work out which goal it matched), then reply IPCNV.val().... 
 						 */
 						String issue = (String)obl.getClass().getField("issue").get(obl);
 						
 						// vals is only 2 values and contains IPCNV
 						//remove the IPCNV so you can get the proposed val
 						vals.remove(IPCNV.val());
 						
 						if (vals.get(0).getClass().isAssignableFrom(Integer.class)) {
 							if (
 								(	(issue.equalsIgnoreCase("speed")) && 
 									(Math.abs( (Integer)vals.get(0) - this.goals.getSpeed()  ) <= this.goals.getSpeedTolerance() )
 								) &&
 								(	(issue.equalsIgnoreCase("spacing")) && 
 									(Math.abs( (Integer)vals.get(0) - this.goals.getSpacing()  ) <= this.goals.getSpacingTolerance() )
 								)
 								) {
 									logger.trace(getID() + " chose the current value " + vals.get(0) + " for the issue " + issue);
 									val = vals.get(0);
 							}
 							else {
 								logger.trace(getID() + " did not like the current value " + vals.get(0) + " for the issue " + issue);
 								val = IPCNV.val();
 							}
 						}
 						else {
 							//vals.remove(IPCNV.val());
 							logger.warn(getID() + " doesn't have a goal for the issue (" + issue + ") or the types didn't match so is happy with the current value " + vals.get(0));
 							val = vals.get(0);
 						}
 					}
 				} catch (IPConException e) {
 					// no valid votes, so just go with 0
 					logger.trace(getID() + " couldn't find any values so is picking " + IPCNV.val() + " due to error: " + e);
 					val = IPCNV.val();
 				} catch (Exception e) {
 					// from the getFields... something went wrong...
 					logger.trace(getID() + " had a problem getting the issue or cluster from " + obl + " so is picking " + IPCNV.val());
 					val = IPCNV.val();
 				}
 				try {
 					logger.trace(getID() + " set the value of field " + fName + " to be " + val + " in " + actToDo);
 					f.set(actToDo, val);
 					logger.trace(getID() + " now has " + actToDo);
 				} catch (Exception e) {
 					logger.error(getID() + " had a problem setting the fields of an action to discharge " + obl + "...");
 					e.printStackTrace();
 				}
 			}
 			else if ((fName.equals("voteBallot")) ||
 			
 				(fName.equals("voteRevision")) ||
 			
 				(fName.equals("voteValue")) ) {
 				// fill this in for prospective Response1B action
 			}
 			
 			// biiiiiig if statement...
 			else if ( (fName.equals("ballot")) ||
 			
 				(fName.equals("agent")) ||
 				
 				(fName.equals("leader")) ||
 			
 				(fName.equals("revision")) ||
 			
 				(fName.equals("issue")) ||
 			
 				(fName.equals("cluster")) ||
 				
 				(fName.equals("role")) ) {
 					logger.warn(getID() + " encountered a multivalue \"" + fName + "\" field (" + vals + "), which should never happen! Obligation was " + obl);
 					logger.trace(getID() + " pesudorandomly picked the value of field " + fName + " to be " + vals.get(0) + " in " + actToDo);
 					try {
 						f.set(actToDo, vals.get(0));
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 			}
 			else {
 				logger.warn(getID() + " encountered the unrecognised field \"" + fName + "\" in " + obl);
 				logger.trace(getID() + " pesudorandomly picked the value of field " + fName + " to be " + vals.get(0) + " in " + actToDo);
 				try {
 					f.set(actToDo, vals.get(0));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	private void passJunction(){
 		if (junctionsLeft!=null) {
 			junctionsLeft--;
 			if ((junctionsLeft<1)&& (!fsm.getState().equals("MOVE_TO_EXIT"))) {
 				try {
 					fsm.applyEvent(new MoveToExitEvent());
 					logger.info("[" + getID() + "] Agent " + getName() + " will move towards the exit in the next cycle.");
 				} catch (FSMException e) {
 					logger.warn("FSM can't handle event type MoveToExitEvent:" + e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Create a safe move with the intention of heading towards the exit
 	 * @return
 	 */
 	private CellMove createExitMove(int nextJunctionDist, NeighbourChoiceMethod neighbourChoiceMethod) {
 		CellMove result = null;
 		if (myLoc.getLane()==0) {
 			if (	(nextJunctionDist>= Math.max((mySpeed-speedService.getMaxDecel()), 1)) &&
 					(nextJunctionDist<= Math.min((mySpeed+speedService.getMaxAccel()), speedService.getMaxSpeed())) ) {
 				result = driver.turnOff();
 				logger.debug("[" + getID() + "] Agent " + getName() + " turning off in " + nextJunctionDist);
 			}
 			else {
 				// FIXME TODO
 				// try to make it so you can end a cycle on the right cell
 				// find a safe move in this lane; this gives you the max safe speed you can move at
 				Pair<CellMove, Integer> maxSpeedMove = createMoveFromNeighbours(myLoc.getLane(), neighbourChoiceMethod);
 				int maxSpeed = maxSpeedMove.getA().getYInt();
 				if (maxSpeedMove.getB().equals(Integer.MAX_VALUE)) {
 					// get a safe move to the exit in the lane
 					result = driver.moveIntoLaneAtSpeed(myLoc.getLane(), safeMoveSpeedToExit(nextJunctionDist, maxSpeed, myLoc.getLane()));
 				}
 				else {
 					logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + (myLoc.getLane()) + " to turn towards the exit, so checking the next lane.");
 					Pair<CellMove, Integer> maxSpeedMove2 = createMoveFromNeighbours(myLoc.getLane()+1, neighbourChoiceMethod);
 					int maxSpeed2 = maxSpeedMove2.getA().getYInt();
 					if ( (maxSpeedMove2.getB().equals(Integer.MAX_VALUE)) || (maxSpeedMove2.getB()>maxSpeedMove.getB())) {
 						// if you can change lanes right, do so.
 						logger.debug("[" + getID() + "] Agent " + getName() + " found a safe(r) move in lane " + (myLoc.getLane()+1) + " so is moving out in hope.");
 						result = driver.moveIntoLaneAtSpeed(myLoc.getLane()+1, safeMoveSpeedToExit(nextJunctionDist, maxSpeed2, myLoc.getLane()+1));
 					}
 					else {
 						// if not, slow down
 						logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + (myLoc.getLane()+1) + ", so is staying in lane with move " + maxSpeedMove.getA());
 						result = maxSpeedMove.getA();
 					}
 				}
 			}
 		}
 		else {
 			// you're not in lane0 (check validity anyway)
 			if (locationService.isValidLane(myLoc.getLane()-1)) {
 				Pair<CellMove, Integer> maxSpeedMove = createMoveFromNeighbours(myLoc.getLane()-1, neighbourChoiceMethod);
 				int maxSpeed = maxSpeedMove.getA().getYInt();
 				if (maxSpeedMove.getB().equals(Integer.MAX_VALUE)) {
 					// if you can change lanes left, do so.
 					logger.debug("[" + getID() + "] Agent " + getName() + " found a safe move in lane " + (myLoc.getLane()-1) + " so is moving towards the exit.");
 					result = driver.moveIntoLaneAtSpeed(myLoc.getLane()-1, safeMoveSpeedToExit(nextJunctionDist, maxSpeed, myLoc.getLane()-1));
 				}
 				else {
 					// if not, work out speed for current lane
 					logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + (myLoc.getLane()-1) + " to turn towards the exit, so is checking the current lane.");
 					Pair<CellMove, Integer> maxSpeedMove2 = createMoveFromNeighbours(myLoc.getLane(), neighbourChoiceMethod);
 					int maxSpeed2 = maxSpeedMove2.getA().getYInt();
 					if (maxSpeedMove2.getB().equals(Integer.MAX_VALUE)) {
 						// if you canstay in current lane, do so.
 						logger.debug("[" + getID() + "] Agent " + getName() + " found a safe move in lane " + (myLoc.getLane()) + " so is moving to the exit.");
 						result = driver.moveIntoLaneAtSpeed(myLoc.getLane(), safeMoveSpeedToExit(nextJunctionDist, maxSpeed2, myLoc.getLane()));
 					}
 					else {
 						// if not, slow down
 						logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + (myLoc.getLane()) + ", so is slowing down in hope.");
 						result = driver.decelerateMax();
 					}
 				}
 			}
 			else {
 				// skip, not a valid lane - this should never happen !
 				logger.warn("[" + getID() + "] Agent " + getName() + " tried to check invalid lane " + (myLoc.getLane()-1) + " for a safe move");
 				result = null;
 			}
 		}
 		if (result==null){
 			logger.warn("Shouldn't get here.");
 			result = driver.decelerateMax();
 		}
 		return result;
 	}
 
 	/**
 	 * @param nextJunctionDist
 	 * @param result
 	 * @param temp
 	 * @return
 	 */
 	private int safeMoveSpeedToExit(int nextJunctionDist, int maxSpeed, int lane) {
 		 logger.debug("[" + getID() + "] Agent " + getName() + " the maximum safe speed in lane " + lane + " is " + maxSpeed);
 		// check to see if nextJunctionDist is a multiple of this speed (mod(nJD,speed)==0)
 		if ((maxSpeed==0) || (MathsUtils.mod(nextJunctionDist,maxSpeed)==0)) {
 			// if it is, yay
 			return maxSpeed;
 		}
 		else {
 			// otherwise, check all the speeds between maxSpeed and yourSpeed-maxDecell for the same thing
 			for (int i = maxSpeed-1; i>=mySpeed-speedService.getMaxDecel(); i--) {
 				if (MathsUtils.mod(nextJunctionDist,i)==0) {
 					// if it is, yay
 					logger.debug("[" + getID() + "] Agent " + getName() + " found a good move in lane " + lane + " at speed " + i);
 					return i;
 				}
 				else {
 					//Level lvl = logger.getLevel();
 					//logger.setLevel(Level.TRACE);
 					logger.trace("[" + getID() + "] Agent " + getName() + " checking speed " + i);
 					//logger.setLevel(lvl);
 				}
 			}
 			// if none of them are good, then decelMax
 			logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a good speed in lane " + lane + " so is decelerating");
 			return driver.decelerateToCrawl().getYInt();
 		}
 	}
 	
 	private CellMove _working_createMove(OwnChoiceMethod ownChoiceMethod, NeighbourChoiceMethod neighbourChoiceMethod){
 		Pair<CellMove, Integer> temp = null;
 		// This is an indirect assumption of only three lanes
 		//  - yes we only want to check in lanes we can move into, but
 		//  - we should also take into account agents not in those lanes which might move into them ahead of us.
 		ArrayList<Integer> availableLanes = new ArrayList<Integer>(3);
 		LinkedList<Pair<CellMove,Integer>> actions = new LinkedList<Pair<CellMove,Integer>>();
 		availableLanes.add(myLoc.getLane());
 		availableLanes.add(myLoc.getLane()+1);
 		availableLanes.add(myLoc.getLane()-1);
 		@SuppressWarnings("unused")
 		Level lvl = logger.getLevel();
 		//logger.setLevel(Level.TRACE);
 		logger.trace("list of lanes is: " + availableLanes);
 		//logger.setLevel(lvl);
 		
 		for (int i = 0; i <=availableLanes.size()-1; i++) {
 			if (locationService.isValidLane(availableLanes.get(i))) {
 				temp = createMoveFromNeighbours(availableLanes.get(i), neighbourChoiceMethod);
 				if (temp.getB().equals(Integer.MAX_VALUE)) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " found a safe move in lane " + availableLanes.get(i) + " : " + temp); 
 				}
 				else {
 					logger.debug("[" + getID() + "] Agent " + getName() + " found an unsafe move in lane " + availableLanes.get(i) + " : " + temp);
 				}
 				actions.add(new Pair<CellMove,Integer>(new CellMove((availableLanes.get(i)-myLoc.getLane()), (int)temp.getA().getY()), temp.getB()));
 			}
 			else {
 				// skip, not a valid lane
 			}
 		}
 		return chooseFromSafeMoves(actions, ownChoiceMethod);
 	}
 	
 	private CellMove newCreateMove(OwnChoiceMethod ownChoiceMethod, NeighbourChoiceMethod neighbourChoiceMethod){
 		// This is an indirect assumption of only three lanes
 		//  - yes we only want to check in lanes we can move into, but
 		//  - we should also take into account agents not in those lanes which might move into them ahead of us.
 		ArrayList<Integer> availableLanes = new ArrayList<Integer>(3);
 		availableLanes.add(myLoc.getLane());
 		availableLanes.add(myLoc.getLane()+1);
 		availableLanes.add(myLoc.getLane()-1);
 		@SuppressWarnings("unused")
 		Level lvl = logger.getLevel();
 		//logger.setLevel(Level.TRACE);
 		logger.trace("list of lanes is: " + availableLanes);
 		//logger.setLevel(lvl);		
 		
 		
 		// set of agents
 		Set<UUID> set = new HashSet<UUID>();
 		// map of agents to their set of possible moves
 		HashMap<UUID,HashMap<CellMove,Pair<RoadLocation,RoadLocation>>> agentMoveMap = new HashMap<UUID,HashMap<CellMove,Pair<RoadLocation,RoadLocation>>>();
 
 		for (int lane = 0; lane<=locationService.getLanes(); lane++) {
 			UUID agentFront = locationService.getAgentToFront(lane);
 			if (agentFront!=null) {
 				logger.debug("[" + getID() + "] Agent " + getName() + " saw " + agentFront + " in front in lane " + lane);
 				set.add( agentFront );
 			}
 			if (lane!=myLoc.getLane()) {
 				UUID agentRear = locationService.getAgentStrictlyToRear(lane);
 				if (agentRear!=null) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " saw " + agentRear + " behind in lane " + lane);
 					set.add( agentRear );
 				}
 			}
 		}
 		for (UUID agent : set) {
 			if (locationService.getAgentLocation(agent).getOffset() >= myLoc.getOffset()) {
 				// generate all possible moves for agents in front of you and save start/end location to set in map
 				agentMoveMap.put( agent, generateMoves(agent, true) );
 			}
 			else {
 				// generate possible moves IN SAME LANE for agents behind you - they won't cut you up if theyre behind you
 				agentMoveMap.put( agent, generateMoves(agent, false) );
 			}
 		}
 		
 		// generate all possible moves for yourself, and save start/end locs for them
 		HashMap<CellMove,Pair<RoadLocation,RoadLocation>> myMoves = generateMoves(this.getID(), true);
 		HashMap<CellMove,Pair<RoadLocation,RoadLocation>> noCollisionMoves = new HashMap<CellMove,Pair<RoadLocation,RoadLocation>>();
 		
 		if (set.isEmpty()) {
 			noCollisionMoves = myMoves;
 		}
 		else {
 			for (Entry<CellMove,Pair<RoadLocation,RoadLocation>> entryMe : myMoves.entrySet()) {
 				Pair<RoadLocation,RoadLocation> myMove = entryMe.getValue();
 				for (UUID agent : set) {
 					for (Entry<CellMove,Pair<RoadLocation,RoadLocation>> entryThem : agentMoveMap.get(agent).entrySet()) {
 						Pair<RoadLocation,RoadLocation> pairThem = entryThem.getValue(); 
 						// check all my moves against all their moves, and keep any of mine which don't cause collisions
 						boolean collision = checkForCollisions(myMove.getA(), myMove.getB(), pairThem.getA(), pairThem.getB());
 						if (!collision) {
 							noCollisionMoves.put( entryMe.getKey(), entryMe.getValue() );
 							logger.debug("[" + getID() + "] Agent " + getName() + " found a move with no collisions : " + entryMe.getKey() + " between " + entryMe.getValue());
 						}
 					}
 				}
 			}
 		}
 		
 		CellMove move;
 		if (noCollisionMoves.isEmpty()) {
 			logger.warn("[" + getID() + "] Agent " + getName() + " could not find any moves without collisions ! Continuing at current speed since will crash either way...");
 			move = driver.constantSpeed();
 		}
 		else {
 			// check for stopping distance (agents to front (& back if diff lane))
 			// return a move with a safety weight - "definitely" safe moves vs moves that you can't stop in time
 			// weight should be the shortfall between your ability to stop in time and where you need to stop
 			HashMap<CellMove,Integer>safetyWeightedMoves = generateStoppingUtilities(noCollisionMoves);  
 	
 			// choose a move from the safe ones, depending on your move choice method
 			move = chooseMove(safetyWeightedMoves, ownChoiceMethod); 
 		}
 		return move;
 	}
 	
 	/**
 	 * 
 	 * @param safetyWeightedMoves map of move to weight. Weight is -ve if unsafe, otherwise bigger is better
 	 * @param ownChoiceMethod
 	 * @return preferred move from map based on ownChoiceMethod
 	 */
 	private CellMove chooseMove(HashMap<CellMove, Integer> safetyWeightedMoves, OwnChoiceMethod ownChoiceMethod) {
 		CellMove result = driver.constantSpeed();
 		if (!safetyWeightedMoves.isEmpty()) {
 			switch (ownChoiceMethod) {
 			case SAFE : {
 				// sort by weighting and return the one with the highest weight
 				LinkedList<Map.Entry<CellMove,Integer>> list = new LinkedList<Entry<CellMove,Integer>>();
 				list.addAll(safetyWeightedMoves.entrySet());
 				Collections.sort(list, new MapValueAscComparator());
 				result = list.getLast().getKey();
 				break;
 			}
 			case SAFE_GOALS : {
 				// sort by weighting
 				LinkedList<Map.Entry<CellMove,Integer>> list = new LinkedList<Entry<CellMove,Integer>>();
 				list.addAll(safetyWeightedMoves.entrySet());
 				// take any that are "safe" (ie not negative weight) and discard the rest if there are any that are safe, otherwise return the least-bad
 				LinkedList<Map.Entry<CellMove,Integer>> safestMoves = getSafestMovesAndSort(list);
 				// sort by difference between moveSpeed and goalSpeed
 				LinkedList<Pair<CellMove,Integer>> sortedList = sortBySpeedDiff(safestMoves);
 				result = sortedList.getFirst().getA();
 				break;
 			}
 			default : {
 				logger.warn("[" + getID() + "] Agent " + getName() + " does not know how to choose by the method \"" + ownChoiceMethod.toString() + "\" so is continuing at current speed");
 				break;
 			}
 			}
 		}
 		else {
 			logger.warn("[" + getID() + "] Agent " + getName() + " was not given any moves to choose from ! Continuing at current speed ...");
 		}
 		logger.debug("[" + getID() + "] Agent " + getName() + " chose to " + result);
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param list - can ignore the value in the entry. Only key (move) is of interest
 	 * @return list sorted in ascending order by the difference between the speed of the move and the agent's goal speed (the Pair.B)
 	 */
 	private LinkedList<Pair<CellMove,Integer>> sortBySpeedDiff(LinkedList<Entry<CellMove, Integer>> list) {
 		LinkedList<Pair<CellMove,Integer>> result = new LinkedList<Pair<CellMove,Integer>>();
 		Integer goalSpeed = this.getGoals().getSpeed();
 		// iterate the list and calculate the difference between the move's speed and the goal speed, then insert to the new list
 		for (Entry<CellMove,Integer> entry : list) {
 			CellMove move = entry.getKey();
 			result.add(new Pair<CellMove, Integer>(move, Math.abs(move.getYInt()-goalSpeed)));
 		}		
 		Collections.sort(result, new PairBDescComparator());
 		return result;
 	}
 
 	/**
 	 * If any of the values in the list entries are +ve (or 0), result will only contain those entries 
 	 * Otherwise, the result will contain only the entries with the highest value
 	 * @param list unsorted
 	 * @return list sorted in descending order with additional constraints as above
 	 */
 	private LinkedList<Entry<CellMove, Integer>> getSafestMovesAndSort(final LinkedList<Entry<CellMove, Integer>> list) {
 		LinkedList<Entry<CellMove,Integer>> result = (LinkedList<Entry<CellMove, Integer>>)list.clone();
 		Collections.sort(result, new MapValueAscComparator());
 		Collections.reverse(result);
 		/* LIST NOW DESCENDING ORDER */
 		// if you have any +ve entries, you can remove all -ves
 		boolean havePositives = list.getFirst().getValue()>=0;
 		Integer highestSoFar = Integer.MIN_VALUE;
 		
 		// Pass 1 : remove negative values if you have any positives, and find +ve value
 		for (Entry<CellMove,Integer> entry : result) {
 			if (entry.getValue()>highestSoFar) {
 				highestSoFar = entry.getValue();
 			}
 			if (havePositives && entry.getValue()<0) {
 				result.remove(entry);
 			}
 		}
 		
 		// Pass 2: if you have no positives, remove all values less than highest value (if positives, then the list is as you want it)
 		if (!havePositives) {
 			for (Entry<CellMove,Integer> entry : result) {
 				if (entry.getValue()<highestSoFar) {
 					result.remove(entry);
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param a_start
 	 * @param a_end
 	 * @param b_start
 	 * @param b_end
 	 * @return true if there would be a collision between a and b, false otherwise
 	 */
 	private boolean checkForCollisions(RoadLocation a_start, RoadLocation a_end, RoadLocation b_start, RoadLocation b_end) {
 		boolean result = false;
 		boolean laneChange = a_start.getLane()==a_end.getLane();
 		if (a_end.equals(b_end)) {
 			result = true;
 		}
 		else {
 			if ( (b_end.getLane() == a_end.getLane()) && !laneChange) {
 				// same lane, if he is behind us then it is a collision
 				int hisOffset = b_end.getOffset();
 				int myOffset = a_end.getOffset();
 				int areaLength = locationService.getWrapPoint();
 				boolean heWrapped = b_end.getOffset() < b_start.getOffset();
 				boolean iWrapped = a_end.getOffset() < a_start.getOffset();
 				if(!iWrapped && heWrapped) {
 					hisOffset += areaLength;
 				}
 				if (hisOffset < myOffset) {
 					result = true;
 				}
 			}
 		}
 		return result;	
 	}
 
 	/**
 	 * 
 	 * @param moves map of own moves to startloc/endloc pair
 	 * @return map of own moves to utilities. If the utility is negative, it's not a safe move. The larger the utility the better.
 	 */
 	private HashMap<CellMove, Integer> generateStoppingUtilities( HashMap<CellMove,Pair<RoadLocation, RoadLocation>> moves) {
 		HashMap<CellMove,Integer> result = new HashMap<CellMove,Integer>();
 		
 		
 		// for all moves in set
 		for (Entry<CellMove,Pair<RoadLocation, RoadLocation>> entry : moves.entrySet()) {
 			int startLane = entry.getValue().getA().getLane();
 			int endLane = entry.getValue().getB().getLane();
 			int speed = entry.getKey().getYInt();
 		// get stopping distance based on the movespeed
 			Integer moveStoppingDist = speedService.getStoppingDistance(speed);
 			Integer frontStoppingDist = getStoppingDistanceFront(endLane);
 			Integer rearStoppingDist;
 			if (endLane!=startLane) {
 				rearStoppingDist = getStoppingDistanceRear(endLane);
 			}
 			else {
 				rearStoppingDist = Integer.MIN_VALUE;
 			}
 		// get difference between stopDist and agentToFront's stopDist
 			int frontDiff = frontStoppingDist - moveStoppingDist;
 		// if moving to another lane, also get difference between stopDist and agentToRear's stopDist
 		// (if no agent behind or if same lane, rearStopDist==MinInt -> rearDiff = MaxInt
 			int rearDiff = Integer.MAX_VALUE;
 			if (rearStoppingDist!=Integer.MIN_VALUE) {
 				rearDiff = rearStoppingDist - moveStoppingDist;
 			}
 			// give the smallest difference as the utility -> if it's negative, then it's not a safe move
 			result.put(entry.getKey(), Math.min(frontDiff, rearDiff));
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param agent agent to check
 	 * @param allMoves true if should return all moves, false if should return only moves in same lane
 	 * @return hashmap of moves that the given agent could make in the next cycle, or emptySet if agent could not be seen. Key is CellMove, Value is pair of start/end loc
 	 */
 	private HashMap<CellMove,Pair<RoadLocation,RoadLocation>> generateMoves(UUID agent, boolean allMoves) {
		logger.info("[" + getID() + "] Agent " + getName() + " trying to generate moves for " + agent);
 		HashMap<CellMove,Pair<RoadLocation,RoadLocation>> result = new HashMap<CellMove,Pair<RoadLocation,RoadLocation>>();
 		
 		RoadLocation startLoc = null;
 		ArrayList<Integer> laneOffsets= new ArrayList<Integer>(1);
 		Integer startSpeed = null;
 		int maxAccel = speedService.getMaxAccel();
 		int maxDecel = speedService.getMaxDecel();
 		int maxSpeed = speedService.getMaxSpeed();
 		int wrapPoint = locationService.getWrapPoint();
 		
 		// get the current location of the agent in question
 		try {
 			startLoc = locationService.getAgentLocation(agent);
 			startSpeed = speedService.getAgentSpeed(agent);
 		} catch (CannotSeeAgent e) {
 			// return empty set
 			logger.info("[" + getID() + "] Agent " + getName() + " tried to generateMoves for " + agent + ", who they cannot see.");
 		}
 		
 		if ( (startLoc!=null) && (startSpeed!=null) ) {
 			// get the lanes to be considered, but in relative terms
 			if (allMoves) {
 				for (int i=-1;i<2;i++) {
 					if (locationService.isValidLane(startLoc.getLane() + i)) {
 						laneOffsets.add(i);
 					}
 				}
 			}
 			else {
 				laneOffsets.add(0);
 			}
 			
 			// for all lane offsets
 			for (int laneMove : laneOffsets) {
 				// for all speeds from currentSpeed-maxDecel to max(currentSpeed+maxAccel,maxSpeed)
 				int topSpeed = Math.max(startSpeed+maxAccel, maxSpeed);
				int minSpeed = Math.max(0, startSpeed-maxDecel);
				for (int speedMove=minSpeed; speedMove<=topSpeed; speedMove++) {
 					// add the move corresponding to the lane offset + speed
 					CellMove move = new CellMove(laneMove,speedMove);
 					RoadLocation endLoc = new RoadLocation(startLoc.getLane()+laneMove,MathsUtils.mod(startLoc.getOffset()+speedMove, wrapPoint) ); 
 					result.put(move, new Pair<RoadLocation,RoadLocation>(startLoc,endLoc));
 				}
 			}
 		}
 		
 		return result;
 	}
 
 	private CellMove createMove(OwnChoiceMethod ownChoiceMethod, NeighbourChoiceMethod neighbourChoiceMethod){
 		Pair<CellMove, Integer> temp = null;
 		// This is an indirect assumption of only three lanes
 		//  - yes we only want to check in lanes we can move into, but
 		//  - we should also take into account agents not in those lanes which might move into them ahead of us.
 		ArrayList<Integer> availableLanes = new ArrayList<Integer>(3);
 		LinkedList<Pair<CellMove,Integer>> actions = new LinkedList<Pair<CellMove,Integer>>();
 		availableLanes.add(myLoc.getLane());
 		availableLanes.add(myLoc.getLane()+1);
 		availableLanes.add(myLoc.getLane()-1);
 		@SuppressWarnings("unused")
 		Level lvl = logger.getLevel();
 		//logger.setLevel(Level.TRACE);
 		logger.trace("list of lanes is: " + availableLanes);
 		//logger.setLevel(lvl);
 		
 		for (int i = 0; i <=availableLanes.size()-1; i++) {
 			if (locationService.isValidLane(availableLanes.get(i))) {
 				temp = createMoveFromNeighbours(availableLanes.get(i), neighbourChoiceMethod);
 				if (temp.getB().equals(Integer.MAX_VALUE)) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " found a safe move in lane " + availableLanes.get(i) + " : " + temp); 
 				}
 				else {
 					logger.debug("[" + getID() + "] Agent " + getName() + " found an unsafe move in lane " + availableLanes.get(i) + " : " + temp);
 				}
 				actions.add(new Pair<CellMove,Integer>(new CellMove((availableLanes.get(i)-myLoc.getLane()), (int)temp.getA().getY()), temp.getB()));
 			}
 			else {
 				// skip, not a valid lane
 			}
 		}
 //		if (temp==null) {
 //			logger.warn("[" + getID() + "] Agent " + getName() + " doesn't think there is a safe move to make ! Decelerating as much as possible...");
 //			return driver.decelerateMax();
 //		}
 //		logger.error("You should never get here (deadcode), but Eclipse is insisting on a return here, so let's pass out a null to throw some exceptions later :D");
 		return chooseFromSafeMoves(actions, ownChoiceMethod);
 	}
 
 	/**
 	 * @param actions list of safe actions to make
 	 * @param ownChoiceMethod Should be OwnChoiceMethod.SAFE, OwnChoiceMethod.PLANNED
 	 * @return 
 	 */
 	private CellMove chooseFromSafeMoves(LinkedList<Pair<CellMove, Integer>> actions, OwnChoiceMethod ownChoiceMethod) {
 		Pair<CellMove, Integer> result = null;
 		if (actions.isEmpty()) {
 			logger.error("[" + getID() + "] Agent " + getName() + " couldn't find any moves at all ! Totally shouldn't be here, so slowing as much as possible.");
 			return driver.decelerateToCrawl();
 		}
 		else {
 			switch (ownChoiceMethod) {
 			// Choose the first safe move you find
 			case SAFE :  {
 				logger.trace("[" + getID() + "] Agent " + getName() + " choosing a safe action...");
 				Collections.sort(actions, new PairBDescComparator<Integer>());
 				if (!actions.isEmpty()) {
 					result = actions.getFirst();
 					if (result.getB().equals(Integer.MAX_VALUE)) {
 						logger.debug("[" + getID() + "] Agent " + getName() + " attempting safe move: " + result.getA());
 						if (result.getA().getX()!=0) {
 							logger.debug("Agent is going to change lanes.");
 						}
 					}
 					else {
 						logger.warn("[" + getID() + "] Agent " + getName() + " doesn't think there is a safe move to make ! Attempting move: " + result.getA());
 						if (result.getA().getX()!=0) {
 							logger.debug("Agent is going to change lanes.");
 						}
 					}
 				}
 				else {
 					logger.warn("[" + getID() + "] Agent " + getName() + " doesn't think there are any safe moves to make ! Slowing as fast as possible !");
 					result = new Pair<CellMove, Integer>(driver.decelerateMax(), 0);
 				}
 				return result.getA();
 			}
 			// Choose a safe move that sets your speed as close to your goal as possible
 			case SAFE_GOALS : {
 				logger.trace("[" + getID() + "] Agent " + getName() + " choosing a safe_goals action...");
 				discardUnsafeActions(actions);
 				if (!actions.isEmpty()) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " choosing from: " + actions);
 					// Reset the value of the move to how close it is to your goalspeed
 					for (Pair<CellMove,Integer> act : actions) {
 						logger.debug("[" + getID() + "] Agent " + getName() + " considering move:" + act.getA()); 
 						act.setB(Math.abs(act.getA().getYInt() - this.goals.getSpeed()));
 					}
 					Collections.sort(actions, new PairBDescComparator<Integer>());
 					result = actions.getLast();
 					logger.debug("[" + getID() + "] Agent " + getName() + " attempting safe_goals move: " + result + " with difference from goalSpeed of " + result.getB());
 					if (result.getA().getX()!=0) {
 						logger.debug("Agent is going to change lanes.");
 					}
 				}
 				else {
 					logger.warn("[" + getID() + "] Agent " + getName() + " doesn't think there are any safe_goals moves to make ! Slowing as fast as possible !");
 					result = new Pair<CellMove, Integer>(driver.decelerateMax(), 0);
 				}
 				return result.getA();
 			}
 			case PLANNED : {
 				//TODO FIXME do this :P
 			}
 			default : {
 				logger.error("[" + getID() + "] Agent " + getName() + " tried to choose a " + ownChoiceMethod.toString() + " which doesn't exist, so slowing as much as possible.");
 				return driver.decelerateToCrawl();
 			}
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param actions list of action/safety pairs - if int is not Integer.MAX_VALUE, then discard it.
 	 */
 	private void discardUnsafeActions(LinkedList<Pair<CellMove, Integer>> actions) {
 		Iterator<Pair<CellMove, Integer>> it = actions.iterator();
 		while (it.hasNext()) {
 			if (it.next().getB()!=Integer.MAX_VALUE) {
 				it.remove();
 			}
 		}
 	}
 
 	/**
 	 * @param neighbourChoiceMethod TODO
 	 * @return a reasoned move/viability pair. Viability is Integer.Max_VALUE if the move is safe, or not if otherwise.
 	 */
 	private Pair<CellMove,Integer> createMoveFromNeighbours(int lane, NeighbourChoiceMethod neighbourChoiceMethod) {
 		switch (neighbourChoiceMethod) {
 		case WORSTCASE : return worstCaseNeighbourChoice(lane);
 		case GOALS : // FIXME TODO
 		case INSTITUTIONAL : // FIXME TODO
 		default : {
 			logger.error("[" + getID() + "] Agent " + getName() + " tried to predict neighbour moves by " + neighbourChoiceMethod.toString() + " which doesn't exist.");
 			return new Pair<CellMove, Integer>(driver.decelerateToCrawl(), 0);
 		}
 		}
 	}
 	
 	/**
 	 * @return a reasoned move/viability pair. Viability is Integer.Max_VALUE if the move is safe, or not if otherwise.
 	 */
 	private Pair<CellMove,Integer> worstCaseNeighbourChoice(int lane) {
 		int newSpeed;
 		
 		
 		
 		// this doesn't really show if something isn't safe, but it does show if something is safe
 		// (ie, only true if you know you can go at your preferred speed instead of one for safety's sake.
 		// TODO FIXME fairly sure I can remove this
 		boolean canMoveAtPreferred = false;
 		
 		
 		
 		logger.debug("[" + getID() + "] Agent " + getName() + " is checking lane " + lane + " for a valid move");
 		
 
 		
 		Pair<Integer,Integer> pairFront = getStoppingSpeedFront(lane);
 		Integer stoppingSpeedFront = pairFront.getA();
 		Integer utilityFront = pairFront.getB();
 		Integer stoppingSpeedRear;
 		Integer utilityRear;
 		Integer utility;
 		// only need to check if you're changing lanes
 		if (lane!=myLoc.getLane()) {
 			Pair<Integer,Integer> pairRear = getStoppingSpeedRear(lane);
 			stoppingSpeedRear = pairRear.getA();
 			utilityRear = pairRear.getB();
 		}
 		else {
 			stoppingSpeedRear = -1;
 			utilityRear = Integer.MAX_VALUE;
 		}
 		logger.debug("[" + getID() + "] Agent " + getName() + " checked lane " + lane + " and got front:" + stoppingSpeedFront + "/" + utilityFront + " rear:" + stoppingSpeedRear + "/" + utilityRear);
 		
 		/*// if either of the choices are unsafe, then set speed based on that
 		if (utilityFront!=Integer.MAX_VALUE) {
 			newSpeed = stoppingSpeedFront;
 			utility = utilityFront;
 			logger.debug("[" + getID() + "] Agent " + getName() + " found speed restriction due to front so deciding to move at speed of " + newSpeed);
 		}
 		else if (utilityRear!=Integer.MAX_VALUE) {
 			newSpeed = stoppingSpeedRear;
 			utility = utilityRear;
 			logger.debug("[" + getID() + "] Agent " + getName() + " found speed restriction due to rear so deciding to move at speed of " + newSpeed);
 		}
 		else {*/
 			
 		// check you can physically reach a speed inside the range
 		/*if (  	( (stoppingSpeedFront < 0) || ( (mySpeed>stoppingSpeedFront) && (mySpeed-speedService.getMaxDecel() > stoppingSpeedFront) ) )  ||
 				( (stoppingSpeedRear > speedService.getMaxSpeed()) || ( (mySpeed<stoppingSpeedRear) && (mySpeed+speedService.getMaxAccel() < stoppingSpeedRear) ) ) ) { 
 			//logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can stop in time in lane " + lane);
 			logger.debug("[" + getID() + "] Agent " + getName() + " got StoppingSpeedFront:" + stoppingSpeedFront + ", mySpeed:" + mySpeed + ". maxDecel:" + speedService.getMaxDecel() + 
 																	" and StoppingSpeedRear:" + stoppingSpeedRear + ", maxSpeed:" + speedService.getMaxSpeed());
 			newSpeed=-1;
 		}
 		else {*/
 			// you can physically hit a speed inside the range
 			/*
 			 * if goalSpeed > front, newSpeed = front
 			 * elif goalSpeed < rear, newSpeed = rear
 			 * else newSpeed = goalSpeed
 			 */
 			if (goals.getSpeed() > stoppingSpeedFront) {
 				newSpeed = stoppingSpeedFront;
 				utility = utilityFront;
 				logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at speed of " + newSpeed);
 			}
 			else if (goals.getSpeed() < stoppingSpeedRear) {
 				newSpeed = stoppingSpeedRear;
 				utility = utilityRear;
 				logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at speed of " + newSpeed);
 			}
 			else {
 				newSpeed = goals.getSpeed();
 				utility = Integer.MAX_VALUE;
 				logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
 				canMoveAtPreferred = true;
 			}
 		//}
 		
 		return convertChosenSpeedToAction(newSpeed, canMoveAtPreferred, utility);
 		
 		//return this.driver.randomValid();
 	}
 	
 	/**
 	 * 
 	 * @param lane
 	 * @return stopping distance required due to agent behind (not) you in given lane. Will be MIN Int (ie big -ve) if no agent found. 
 	 */
 	private Integer getStoppingDistanceRear(int lane) {
 		Integer reqStopDistRear;
 		// get agent to check
 		UUID targetRear = this.locationService.getAgentStrictlyToRear(lane);
 		// if there is someone there
 		if (targetRear!=null) {
 			RoadLocation targetRearLoc = (RoadLocation)locationService.getAgentLocation(targetRear);
 			boolean targetIsAhead = false;
 			if (targetRearLoc.getOffset() > myLoc.getOffset()) {
 				targetIsAhead = true; 
 				logger.debug("[" + getID() + "] Agent " + getName() + " saw that " + targetRear + " is actually ahead");
 			}
 			// get the agent behind's stopping distance
 			logger.debug("[" + getID() + "] Agent " + getName() + " saw agent " + targetRear + " at " + targetRearLoc);
 			int targetStopDist = speedService.getAdjustedStoppingDistance(targetRear, false);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks that target's stopping distance is " + targetStopDist);
 			// get the location they will stop at if they accel this turn then decell until stopping
 			// -> their currentLoc + their stopDist
 			int targetStopOffset = targetStopDist + ((RoadLocation)locationService.getAgentLocation(targetRear)).getOffset() + 1;
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks that target could stop at " + targetStopOffset);
 			
 			/* if this offset is greater than the length of the world then the agent's move will wrap
 			 * so if targetIsAhead then you care, otherwise you don't care
 			 * (if theyre behind you and theyre going to wrap then you should wrap as well... CHECK THIS ?)
 			 * 
 			 * Need to make sure that if you are detecting someone infront of you as being behind you (due to wrap)
 			 * then they are going to wrap - if theyre not going to wrap, then you dont care about them -
 			 * they will be infront of you the whole time...
 			 * 
 			 * ie..
 			 * 
 			 * if (targetIsAhead) {
 			 * 	if (targetStopOffset>length) {
 			 * 		// care
 			 * 	}
 			 * 	else {
 			 * 		// don't care
 			 * 	}
 			 * }
 			 * else {
 			 * 	// theyre actually behind you, so do the below
 			 * }
 			 * 
 			 *  --> if (targetIsAhead && !(targetStopOffset>length)) don't care ; stoppingSpeedRear = -1
 			 *  --> else care
 			 * 
 			 */
 			/* Need to make sure that if you are detecting someone infront of you as being behind you (due to wrap)
 			 * then they are going to wrap - if theyre not going to wrap, then you dont care about them -
 			 * they will be infront of you the whole time..
 			 */
 			if (targetIsAhead && (targetStopOffset<=this.locationService.getWrapPoint())) {
 				// if target is ahead of you and they won't wrap, don't care
 				reqStopDistRear = Integer.MIN_VALUE;
 			}
 			else {
 				targetStopOffset = MathsUtils.mod(targetStopOffset, this.locationService.getWrapPoint());
 				// you need to be able to stop on the location one infront of it (which is why previous plus one), so work out how far that is from you
 				// gives myLoc-theirLoc -> +ve means you have to make a move, will be -ve if they dont end up in front of you
 				reqStopDistRear = locationService.getOffsetDistanceBetween(new RoadLocation(targetStopOffset, lane), myLoc);
 				logger.debug("[" + getID() + "] Agent " + getName() + " is at " + myLoc.getOffset() + " so has a reqStopDistRear of " + reqStopDistRear);
 			}
 		}
 		else {
 			logger.debug("[" + getID() + "] Agent " + getName() + " didn't see anyone behind them");
 			reqStopDistRear = Integer.MIN_VALUE;
 		}
 		return reqStopDistRear;
 	}
  
 	/**
 	 * 
 	 * @param lane
 	 * @return the min speed you can safely move at to avoid a car behind (or beside) in the indicated lane crashing into you. Will be negative if no vehicle found. 2nd val in pair is reqStopDist to allow comparison between bad values (which is MaxInt if no agent behind)
 	 */
 	private Pair<Integer,Integer> getStoppingSpeedRear(int lane) {
 		Integer reqStopDistRear = getStoppingDistanceRear(lane);
 		Integer stoppingSpeedRear;
 		if (reqStopDistRear!=Integer.MIN_VALUE) {
 			// what speed do you need to travel at for that ?
 			stoppingSpeedRear = speedService.getSpeedToStopInDistance(reqStopDistRear);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks it needs to move at " + stoppingSpeedRear + " to stop in " + reqStopDistRear);
 		}
 		// if there is no one there you can go at any speed you want (use negative to indicate this)
 		else {
 			stoppingSpeedRear = -1;
 			// FIXME TODO NOTE THAT THIS SWITCHES FROM BIG -VE TO BIG +VE
 			reqStopDistRear = Integer.MAX_VALUE;
 		}
 		
 		return new Pair<Integer,Integer>(stoppingSpeedRear, reqStopDistRear);
 	}
 	
 	/**
 	 * 
 	 * @param lane
 	 * @return stopping distance required due to agent ahead (or alongside) you in given lane. Will be MaxInt if no agent found.
 	 */
 	private Integer getStoppingDistanceFront(int lane) {
 		Integer reqStopDistFront;
 		// get agent to check
 		UUID targetFront = this.locationService.getAgentToFront(lane);
 		// if there is someone there
 		if (targetFront!=null) {
 			// get agent in front's stopping distance
 			logger.debug("[" + getID() + "] Agent " + getName() + " saw agent " + targetFront + " at " + (RoadLocation)locationService.getAgentLocation(targetFront));
 			int targetStopDist = speedService.getAdjustedStoppingDistance(targetFront, true);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks that target's stopping distance is " + targetStopDist);
 			// add the distance between you and their current location (then minus 1 to make sure you can stop BEFORE them)
 			reqStopDistFront = targetStopDist + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(targetFront)))-1;
 			logger.debug("[" + getID() + "] Agent " + getName() + " got a reqStopDistFront of " + reqStopDistFront
 					+ " ( distanceBetween(" + myLoc + "," + (RoadLocation)locationService.getAgentLocation(targetFront) +")= " + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(targetFront))) + ") ");
 		}
 		else {
 			logger.debug("[" + getID() + "] Agent " + getName() + " didn't see anyone in front of them");
 			reqStopDistFront = Integer.MAX_VALUE;
 		}
 		return reqStopDistFront;
 	}
 
 	/**
 	 * @param lane
 	 * @return the max speed you can safely move at to avoid crashing into a car infront (or beside) in the indicated lane). Will be Int.MaxVal if no vehicle found. 2nd val in pair is reqStopDist to allow comparison between bad values.
 	 */
 	private Pair<Integer,Integer> getStoppingSpeedFront(int lane) {
 		Integer reqStopDistFront = getStoppingDistanceFront(lane);
 		Integer stoppingSpeedFront;
 		if (reqStopDistFront!=Integer.MAX_VALUE) {
 			// work out what speed you can be at to stop in time
 			stoppingSpeedFront = speedService.getSpeedToStopInDistance(reqStopDistFront);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks they need to travel at " + stoppingSpeedFront + " to stop in " + reqStopDistFront);
 		}
 		// if there isn't anyone there, you can go at any speed you want (use Int.MaxVal to indicate this)
 		else {
 			stoppingSpeedFront = Integer.MAX_VALUE;
 		}
 		return new Pair<Integer, Integer>(stoppingSpeedFront, reqStopDistFront);
 	}
 
 	/**
 	 * FIXME TODO FAIRLY CONFIDENT I CAN MASSIVELY REDUCE THE SIZE OF THIS BECAUSE YOU ALREADY CHECKED WHETHER OR NOT ITS POSSIBLE TO REACH THE DESIRED SPEED
 	 * 
 	 * @param newSpeed
 	 * @param reqStopDist
 	 * @return a reasoned move/viability pair. Viability is Integer.Max_VALUE if the move is safe, or not if otherwise.
 	 */
 	private Pair<CellMove, Integer> convertChosenSpeedToAction(int newSpeed, boolean canMoveAtPreferred, Integer utility) {
 		// passed in from previous code - if impossible then it doesn't matter what action you return - it won't be chosen
 		if (newSpeed<0) {
 			logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can stop in time");
 			return new Pair<CellMove,Integer>(driver.decelerateMax(), utility);
 		}
 		else {
 			// get the difference between it and your current speed
 			int speedDelta = mySpeed-newSpeed;
 			// if there isn't a difference, chill
 			if (speedDelta == 0) {
 				logger.debug("[" + getID() + "] Agent " + getName() + " attempting move at constant speed of " + newSpeed);
 				return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
 			}
 			// if it's greater than your current speed, accelerate
 			else if (speedDelta < 0) {
 				// you know which you're in, so now abs() it...
 				speedDelta = Math.abs(speedDelta);
 				// if you're at maxSpeed, don't try and speed up...
 				if (mySpeed == speedService.getMaxSpeed()) {
 					return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
 				}
 				else {
 					// work out if you can change to that speed now
 					if (speedDelta <= speedService.getMaxAccel()) {
 						// if you can, do so
 						if (mySpeed+speedDelta > speedService.getMaxSpeed()) {
 							logger.debug("[" + getID() + "] Agent " + getName() + " cannot accelerate by " + speedDelta + " because that would be greater than maxSpeed so will move at maxSpeed.");
 							return new Pair<CellMove,Integer>(driver.moveAt(speedService.getMaxSpeed()), Integer.MAX_VALUE);
 						}
 						else {
 							logger.debug("[" + getID() + "] Agent " + getName() + " attempting to accelerate by " + speedDelta);
 							return new Pair<CellMove,Integer>(driver.accelerate(speedDelta), Integer.MAX_VALUE);
 						}
 					}
 					else {
 						// if not, just accel as much as you can, and you'll make it up
 						if (mySpeed+speedService.getMaxAccel() > speedService.getMaxSpeed()) {
 							logger.debug("[" + getID() + "] Agent " + getName() + " cannot accelerate by " + speedDelta + " because that is greater than maxAccel, so will move at maxSpeed.");
 							return new Pair<CellMove,Integer>(driver.moveAt(speedService.getMaxSpeed()), Integer.MAX_VALUE);
 						}
 						else {
 							logger.debug("[" + getID() + "] Agent " + getName() + " attempting to accelerate as much as possible to meet speedDelta of " + speedDelta);
 							return new Pair<CellMove,Integer>(driver.accelerateMax(), Integer.MAX_VALUE);
 						}
 					}
 				}
 			}
 			// if it's less than your current speed, decelerate
 			else {
 				// you know which you're in, so now abs() it...
 				speedDelta = Math.abs(speedDelta);
 				// if your current speed is 0, then don't even try attempting to decelerate...
 				if (mySpeed == 0) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " is at zero already...");
 					return new Pair<CellMove,Integer>(driver.constantSpeed(), Integer.MAX_VALUE);
 				}
 				else {
 					// work out if you can change to that speed now
 					if (speedDelta <= speedService.getMaxDecel()) {
 						// if you can, do so (checking to make sure it won't take you below 0)
 						int temp = mySpeed-speedDelta;
 						if (temp<=0) {
 							// if you're going to go below 0, then set your decel to hit 0
 							logger.debug("[" + getID() + "] Agent " + getName() + " adjusting decel from " + speedDelta + " to move at zero.");
 							return new Pair<CellMove,Integer>(driver.moveAt(0), Integer.MAX_VALUE);
 						}
 						else {
 							logger.debug("[" + getID() + "] Agent " + getName() + " attempting to decelerate by " + speedDelta);
 							return new Pair<CellMove,Integer>(driver.decelerate(speedDelta), Integer.MAX_VALUE);
 						}
 					}
 					else {
 						// if not, PANIC ! (just decel max and hope for the best ? maybe change lanes...)
 						if (mySpeed-speedService.getMaxDecel() < 0){
 							logger.debug("[" + getID() + "] Agent " + getName() + " would decelMAx but adjusted to move at zero.");
 							return new Pair<CellMove,Integer>(driver.moveAt(0), Integer.MAX_VALUE);
 						}
 						else {
 							// If you *know* you're safe, then chill.
 							if (!canMoveAtPreferred) {
 								logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can meet speedDelta of " + speedDelta);
 								// not convince we'll get here... (was originally reqStopDist, but switched to any non-MAX_INT value when refactored) 
 								return new Pair<CellMove,Integer>(driver.decelerateMax(), utility);
 							}
 							else {
 								logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can meet speedDelta of " + speedDelta + " so decellerating as much as they can");
 								return new Pair<CellMove,Integer>(driver.decelerateMax(), Integer.MAX_VALUE);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param move
 	 */
 	private void submitMove(CellMove move) {
 		// submit move action to the environment.
 		try {
 			logger.debug("[" + getID() + "] Agent " + getName() + " attempting move: " + move);
 			environment.act(move, getID(), authkey);
 		} catch (ActionHandlingException e) {
 			logger.warn("Error trying to move", e);
 		}
 	}
 
 	/**
 	 * @param <T>
 	 * @param act the IPConAction to be sent as a message
 	 * @return a correctly formed Message to be sent, matching act
 	 */
 	private <T extends IPConAction> IPConActionMsg generateIPConActionMsg(T act) {
 		Performative perf = null; 
 		if (act instanceof Prepare1A || act instanceof SyncReq || act instanceof Submit2A) perf = Performative.REQUEST;
 		else if (act instanceof Request0A) perf = Performative.QUERY_REF;
 		else if (act instanceof SyncAck) {
 			if (((SyncAck)act).getValue().equals(IPCNV.val())) perf = Performative.REFUSE;
 			else perf = Performative.AGREE;
 		}
 		else {
 			perf = Performative.INFORM;
 		}
 		return new IPConActionMsg(perf, getTime(), network.getAddress(), act);
 	}
 	
 	@SuppressWarnings("rawtypes") 
 	private void sendMessage(Message msg) {
 		this.network.sendMessage(msg);
 		if (msg instanceof IPConActionMsg) {
 			this.ownMsgs.add((IPConActionMsg)msg);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void saveDataToDB() {
 		// get current simulation time
 		int time = SimTime.get().intValue();
 		// check db is available
 		if (this.persist != null) {
 			// save our location for this timestep
 			this.persist.getState(time).setProperty("location", this.myLoc.toString());
 			this.persist.getState(time).setProperty("speed", ((Integer)(this.mySpeed)).toString());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.ac.imperial.presage2.util.participant.AbstractParticipant#processInput(uk.ac.imperial.presage2.core.messaging.Input)
 	 */
 	@Override
 	protected void processInput(Input arg0) {
 		if (arg0 instanceof ClusterPing) {
 			process((ClusterPing)arg0);
 		}
 		else if (arg0 instanceof IPConActionMsg) {
 			if ( ((IPConActionMsg)arg0).getType().equalsIgnoreCase("IPConActionMsg[Submit2A]") ) {
 				process((Submit2A)(((IPConActionMsg) arg0).getData()));
 			}
 			else if ( ((IPConActionMsg)arg0).getType().equalsIgnoreCase("IPConActionMsg[Prepare1A]") ) {
 				process((Prepare1A)(((IPConActionMsg) arg0).getData()));
 			}
 		}
 		else {
 			logger.info("[" + getID() + "] Agent " + getName() + " not processing input: " + arg0.toString());
 		}
 	}
 
 	private void process(ClusterPing arg0) {
 		logger.info(getID() + " processing ClusterPing " + arg0);
 		this.nearbyRICs.put(arg0.getRIC(), arg0);
 	}
 
 	/**
 	 * After receiving a Submit2A message, the agent should decide whether or not to vote or abstain.
 	 * This is done by checking whether or not the submitted value is within tolerance or not.
 	 * If so, or if the agent does not have a goal for the issue, the agent will vote for it
 	 * If not, or if the proposed value is the wrong type compared to what the agent expected, it will abstain.
 	 * @param arg0
 	 */
 	private void process(Submit2A arg0) {
 		logger.info(getID() + " processing " + arg0);
 		IPConRIC ric = new IPConRIC(arg0.getRevision(), arg0.getIssue(), arg0.getCluster());
 		if (!ipconService.getCurrentRICs().contains(ric)) {
 			logger.info(getID() + " is not in " + ric);
 		}
 		else {
 			Integer revision = ric.getRevision();
 			String issue = ric.getIssue();
 			UUID cluster = ric.getCluster();
 			Object value = arg0.getValue();
 			Integer ballot = arg0.getBallot();
 			Boolean isWithinTolerance = null;
 			try {
 				isWithinTolerance = isWithinTolerance(issue, value);
 			} catch (InvalidClassException e) {
 				// should warn about it if type is wrong
 				logger.warn(e);
 				isWithinTolerance = false;
 			}
 			if (isWithinTolerance==null || isWithinTolerance) {
 				// vote yes
 				prospectiveActions.add(new Vote2B(getIPConHandle(), revision, ballot, value, issue, cluster));
 			}
 			else {
 				// don't vote
 				// FIXME TODO feed into propose/leaveCluster likelihood ?
 			}
 		}
 	}
 	
 	private void process(Prepare1A arg0) {
 		logger.info(getID() + " processing " + arg0);
 		IPConRIC ric = new IPConRIC(arg0.getRevision(), arg0.getIssue(), arg0.getCluster());
 		if (!ipconService.getCurrentRICs().contains(ric)) {
 			logger.info(getID() + " is not in " + ric);
 		}
 		else {
 			Integer revision = ric.getRevision();
 			String issue = ric.getIssue();
 			UUID cluster = ric.getCluster();
 			Integer ballot = arg0.getBallot();
 			// submit prospective action with nulls to be filled in from permission
 			prospectiveActions.add(new Response1B(getIPConHandle(), null, null, null, revision, ballot, issue, cluster));
 		}
 	}
 }
