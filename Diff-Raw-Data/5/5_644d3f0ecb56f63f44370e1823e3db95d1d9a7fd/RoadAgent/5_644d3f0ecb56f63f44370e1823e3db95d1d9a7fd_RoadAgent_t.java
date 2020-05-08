 /**
  * See http://www.presage2.info/ for more details on Presage2
  */
 package uk.ac.imperial.dws04.Presage2Experiments;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Level;
 
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
 import uk.ac.imperial.dws04.utils.record.Pair;
 import uk.ac.imperial.dws04.utils.record.PairBDescComparator;
 import uk.ac.imperial.presage2.core.environment.ActionHandler;
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
 import uk.ac.imperial.presage2.util.fsm.Transition;
 import uk.ac.imperial.presage2.util.fsm.TransitionCondition;
 import uk.ac.imperial.presage2.util.location.CellMove;
 import uk.ac.imperial.presage2.util.participant.AbstractParticipant;
 
 /**
  * @author dws04
  * 
  */
 public class RoadAgent extends AbstractParticipant implements HasIPConHandle {
 
 	private enum OwnChoiceMethod {SAFE, PLANNED};
 	private enum NeighbourChoiceMethod {WORSTCASE, GOALS, INSTITUTIONAL};
 
 	
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
 	private HashMap<IPConRIC,Object> nearbyRICs;
 	 
 	public RoadAgent(UUID id, String name, RoadLocation myLoc, int mySpeed, RoadAgentGoals goals) {
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
 		this.nearbyRICs = new HashMap<IPConRIC,Object>();
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
 		return ss;
 	}
 	
 	@Override
 	public void initialise() {
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
 		
 		/*
 		 * Retrieve (in case we want to change them...) macrogoals
 		 * FIXME TODO 
 		 */
 		
 		/*
 		 * Get IPCon info
 		 *  - get the RIC the agent is in
 		 *  - get the current state for each (ie, cheat :P - maybe this should rely on memory ?)
 		 *  - - this is only to get indication of required speed and spacing
 		 *  - If not in an issue/cluster...
 		 *  - - join nearby cluster's issue
 		 *  - - arrogate if no suitable nearby
 		 *  - If RIC has a leader, and you're also leader...
 		 *  - - see if you should resign
 		 *  - If RIC doesn't have a leader, arrogate
 		 * FIXME TODO
 		 */
 		HashMap<IPConRIC,Object> institutionalFacts = new HashMap<IPConRIC,Object>();
 		final Collection<IPConRIC> currentRICs = ipconService.getCurrentRICs();
 		ArrayList<IPConRIC> ricsToJoin = new ArrayList<IPConRIC>();
 		ArrayList<IPConRIC> ricsToArrogate = new ArrayList<IPConRIC>();
 		ArrayList<IPConRIC> ricsToResign = new ArrayList<IPConRIC>();
 		ArrayList<IPConAction> ipconActions = new ArrayList<IPConAction>();
 		for (IPConRIC ric : currentRICs) {
 			Object value = getChosenFact(ric.getRevision(), ric.getIssue(), ric.getCluster()).getValue();
 			if (value!=null) {
 				logger.trace(getID() + " thinks " + value + " has been chosen in " + ric);
 			}
 			else {
 				logger.trace(getID() + " thinks there is no chosen value in " + ric);
 			}
 			institutionalFacts.put(ric, value);
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
 		// - Check if a nearby cluster has issue
 		// - - Join if yes
 		// - - Arrogate if no and mayArrogate()
 		for (String issue : getGoalMap().keySet()) {
 			Boolean found = false;
 			for (IPConRIC ric : currentRICs) {
 				if (!found && ric.getIssue().equalsIgnoreCase(issue)) {
 					found = true;
 					logger.trace(getID() + " is in a RIC (" + ric + ") for " + issue + ").");
 				}
 			}
 			if (!found) {
 				if (isImpatient(issue)) {
 					logger.trace(getID() + " could not find a RIC for " + issue + " and is impatient so will arrogate.");
 					// Make a RIC to arrogate
 					// I = issue
 					// C = cluster you are in, if in one
 					// R = ?
 					UUID cluster = null;
 					if (!institutionalFacts.isEmpty()) {
 						// pick a very-psuedo-random cluster you're already in
 						cluster = institutionalFacts.keySet().iterator().next().getCluster();
 					}
 					else {
 						// pick a psuedo-random cluster that doesn't exist yet
 						cluster = Random.randomUUID();
 					}
 					IPConRIC newRIC = new IPConRIC(0, issue, cluster);
 					ricsToArrogate.add(newRIC);
 					resetImpatience(issue);
 				}
 				else {
 					logger.trace(getID() + " could not find a RIC for " + issue + " so will check nearby clusters.");
 					Collection<IPConRIC> nearbyRICs = getNearbyRICs();
 					Boolean found2 = false;
 					for (IPConRIC nearbyRIC : nearbyRICs) {
 						if (!found2 && nearbyRIC.getIssue().equalsIgnoreCase(issue)) {
 							found2 = true;
 							logger.trace(getID() + " found a nearby RIC (" + nearbyRIC + ") for " + issue + " so will join it.");
 							ricsToJoin.add(nearbyRIC);
 						}
 					}
 					// update impatience if you can't find a cluster to join
 					if (!found2) {
 						updateImpatience(issue);
 					}
 				}
 				
 			}
 			// else do stuff for RICs youre in
 		}
 		
 		
 		
 		/*
 		 * Arrogate in all RICS you need to
 		 */
 		for (IPConRIC ric : ricsToArrogate) {
 			ArrogateLeadership act = new ArrogateLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		/*
 		 * Join all RICS you need to
 		 */
 		for (IPConRIC ric : ricsToJoin) {
 			JoinAsLearner act = new JoinAsLearner(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		
 		/*
 		 * Resign leadership in all RICs you should
 		 */
 		for (IPConRIC ric : ricsToResign) {
 			ResignLeadership act = new ResignLeadership(getIPConHandle(), ric.getRevision(), ric.getIssue(), ric.getCluster());
 			ipconActions.add(act);
 		}
 		
 		/*
 		 * Get obligations
 		 * Get permissions
 		 * Use permissions to instantiate obligations
 		 * IF no obligations, do something sensible from your permissions (eg responses if you didn't vote yet, and voting itself !)
 		 * Check for conflicting obligations/permissions
 		 * Take note of permission to vote
 		 * Add all relevant actions to queue of actions
 		 * FIXME TODO
 		 */
 		ArrayList<IPConAction> obligatedActions = getInstatiatedObligatedActionQueue();
 		
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
 		
 		
 
 		@SuppressWarnings("rawtypes")
 		ArrayList<Message> messageQueue = new ArrayList<Message>();
 		/*
 		 * Do all IPConActions
 		 */
 		for (IPConAction act : ipconActions) {
 			messageQueue.add(generateIPConActionMsg(act));
 		}
 		
 		/*
 		 * Generate broadcast msgs indicating which RICs you're in
 		 */
 		for (Entry<IPConRIC,Object> entry : institutionalFacts.entrySet()) {
 			messageQueue.add(
 					new ClusterPing(
 							Performative.INFORM, getTime(), network.getAddress(), new Pair<IPConRIC,Object>(entry.getKey(),entry.getValue())
 					)
 			);
 		}
 		
 		/*
 		 * Send all messages queued
 		 */
 		for (Message msg : messageQueue) {
 			sendMessage(msg);
 		}
 		
 		
 		// FIXME TODO implement these then choose them properly :P
 		NeighbourChoiceMethod neighbourChoiceMethod = NeighbourChoiceMethod.WORSTCASE;
 		OwnChoiceMethod ownChoiceMethod = OwnChoiceMethod.SAFE;
 	 
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
 			move = createMove(ownChoiceMethod, neighbourChoiceMethod);
 		}
 		if ((junctionDist!=null) && (junctionDist <= move.getYInt())) {
 			passJunction();
 		}
 		submitMove(move);
 	}
 	
 	private Collection<IPConRIC> getNearbyRICs() {
 		return this.nearbyRICs.keySet();
 	}
 
 	private HashMap<String, Pair<Integer, Integer>> getGoalMap() {
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
 	 * FIXME TODO should probably not just get this from IPCon (that's sort of cheating)
 	 * @param revision
 	 * @param issue
 	 * @param cluster
 	 * @return the Chosen IPConFact for the RIC specified.
 	 */
 	private Chosen getChosenFact(Integer revision, String issue, UUID cluster) {
 		return ipconService.getChosen(revision, issue, cluster);
 	}
 
 	public ArrayList<IPConAction> TESTgetInstantiatedObligatedActionQueue() {
 		return getInstatiatedObligatedActionQueue();
 	}
 	
 	private ArrayList<IPConAction> getInstatiatedObligatedActionQueue() {
 		HashSet<IPConAction> obligations = (HashSet<IPConAction>) ipconService.getObligations(ipconHandle, null, null, null);
 		HashSet<IPConAction> permissions = (HashSet<IPConAction>) ipconService.getPermissions(ipconHandle, null, null, null);
 		HashMap<String, ArrayList<IPConAction>> perMap = new HashMap<String, ArrayList<IPConAction>>();
 		ArrayList<IPConAction> queue = new ArrayList<IPConAction>();
 		
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
 			// biiiiiig if statement...
 			else if ( (fName.equals("ballot")) ||
 			
 				(fName.equals("agent")) ||
 				
 				(fName.equals("leader")) ||
 			
 				(fName.equals("revision")) ||
 			
 				(fName.equals("issue")) ||
 			
 				(fName.equals("cluster")) ||
 			
 				(fName.equals("voteBallot")) ||
 			
 				(fName.equals("voteRevision")) ||
 			
 				(fName.equals("voteValue")) ||
 				
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
 		if (MathsUtils.mod(nextJunctionDist,maxSpeed)==0) {
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
 	
 	@SuppressWarnings("unused")
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
 		Level lvl = logger.getLevel();
 		//logger.setLevel(Level.TRACE);
 		logger.trace("list of lanes is: " + availableLanes);
 		//logger.setLevel(lvl);
 		
 		for (int i = 0; i <=availableLanes.size()-1; i++) {
 			if (locationService.isValidLane(availableLanes.get(i))) {
 				temp = createMoveFromNeighbours(availableLanes.get(i), neighbourChoiceMethod);
 				if (temp.getB().equals(Integer.MAX_VALUE)) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " found a safe move in lane " + availableLanes.get(i)); 
 				}
 				else {
 					logger.debug("[" + getID() + "] Agent " + getName() + " couldn't find a safe move in lane " + availableLanes.get(i));
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
 		Pair<CellMove, Integer> result;
 		if (actions.isEmpty()) {
 			logger.error("[" + getID() + "] Agent " + getName() + " couldn't find any moves at all ! Totally shouldn't be here, so slowing as much as possible.");
 			return driver.decelerateToCrawl();
 		}
 		else {
 			switch (ownChoiceMethod) {
 			case SAFE :  {
 				logger.trace("[" + getID() + "] Agent " + getName() + " choosing a safe action...");
 				Collections.sort(actions, new PairBDescComparator<Integer>());
 				result = actions.getFirst();
 				if (result.getB().equals(Integer.MAX_VALUE)) {
 					logger.debug("[" + getID() + "] Agent " + getName() + " attempting safe move: " + result.getA());
 					if (result.getA().getX()!=0) {
 						logger.debug("Agent is going to change lanes.");
 					}
 				}
 				else {
 					logger.warn("[" + getID() + "] Agent " + getName() + " doesn't think there is a safe move to make ! Decelerating as much as possible...");
 					if (result.getA().getX()!=0) {
 						logger.debug("Agent is going to change lanes.");
 					}
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
 		boolean safe = false;
 		Integer reqStopDist = null; // init to null incase we get to needing it and it hasnt been assigned..
 		
 		logger.debug("[" + getID() + "] Agent " + getName() + " is checking lane " + lane + " for a valid move");
 		// get agent in front
 		UUID target = this.locationService.getAgentToFront(lane);
 		// if there is someone there
 		if (target!=null) {
 			// get agent in front's stopping distance
 			logger.debug("[" + getID() + "] Agent " + getName() + " saw agent " + target + " at " + (RoadLocation)locationService.getAgentLocation(target));
 			int targetStopDist = speedService.getStoppingDistance(target);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks that target's stopping distance is " + targetStopDist);
 			// add the distance between you and their current location (then minus 1 to make sure you can stop BEFORE them)
 			reqStopDist = targetStopDist + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(target)))-1;
 			logger.debug("[" + getID() + "] Agent " + getName() + " got a reqStopDist of " + reqStopDist
 					+ " ( distanceBetween(" + myLoc + "," + (RoadLocation)locationService.getAgentLocation(target) +")= " + (locationService.getOffsetDistanceBetween(myLoc, (RoadLocation)locationService.getAgentLocation(target))) + ") ");
 			// work out what speed you can be at to stop in time
 			int stoppingSpeed = speedService.getSpeedToStopInDistance(reqStopDist);
 			logger.debug("[" + getID() + "] Agent " + getName() + " thinks they need to travel at " + stoppingSpeed + " to stop in " + reqStopDist);
 			if ( (stoppingSpeed <0) || ( (mySpeed>stoppingSpeed) && (mySpeed-speedService.getMaxDecel() > stoppingSpeed) ) ) {
 				logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can stop in time in lane " + lane);
 				return new Pair<CellMove,Integer>(driver.decelerateMax(), reqStopDist);
 			}
 			// if this is more than your preferred speed, aim to go at your preferred speed instead
 			if (stoppingSpeed > goals.getSpeed()) {
 				newSpeed = goals.getSpeed();
 				logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
 				safe = true;
 			}// otherwise, aim to go at that speed
 			else {
 				newSpeed = stoppingSpeed; 
 				logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at safe speed of " + newSpeed);
 			}
 		}
 		// if there isn't anyone there, aim to go at your preferred speed
 		else {
 			newSpeed = goals.getSpeed();
 			logger.debug("[" + getID() + "] Agent " + getName() + " deciding to move at preferred speed of " + newSpeed);
 			safe = true;
 		}
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
 						logger.debug("[" + getID() + "] Agent " + getName() + " adjusted acceleration from " + speedDelta + " to move at maxSpeed.");
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
 						logger.debug("[" + getID() + "] Agent " + getName() + " adjusted acceleration from maxAccel to move at maxSpeed.");
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
 						if (!safe) {
 							logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can meet speedDelta of " + speedDelta);
 							// not convince we'll get here...
 							return new Pair<CellMove,Integer>(driver.decelerateMax(), reqStopDist);
 						}
 						else {
 							logger.debug("[" + getID() + "] Agent " + getName() + " doesn't think they can meet speedDelta of " + speedDelta + " so decellerating as much as they can");
 							return new Pair<CellMove,Integer>(driver.decelerateMax(), Integer.MAX_VALUE);
 						}
 					}
 				}
 			}
 		}
 		
 		//return this.driver.randomValid();
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
 	 * Convert IPConAction to a message and send it
 	 * @param act IPConAction to send
 	 */
 	private void submitIPConAction(IPConAction act) {
 		logger.debug("[" + getID() + "] Agent " + getName() + " sending IPConAction msg: " + act);
 		generateIPConActionMsg(act);
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
 	
 	private void sendMessage(Message msg) {
 		this.network.sendMessage(msg);
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
 		else {
 			logger.info("[" + getID() + "] Agent " + getName() + " not processing input: " + arg0.toString());
 		}
 	}
 
 	private void process(ClusterPing arg0) {
 		this.nearbyRICs.put(arg0.getData().getA(), arg0.getData().getB());
 	}
 
 }
