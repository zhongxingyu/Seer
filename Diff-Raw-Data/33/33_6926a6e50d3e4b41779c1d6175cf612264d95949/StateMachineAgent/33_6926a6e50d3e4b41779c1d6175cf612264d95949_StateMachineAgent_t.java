 package stateMachineAgent;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Vector;
 
 
 public class StateMachineAgent {
 
 	// Instance variables
 	private Path best = null;  //best path from init to goal the agent knows atm
 	private ArrayList<Character> possibleBest;
 	private StateMachineEnvironment env;
 	private char[] alphabet;
 	private ArrayList<Episode> episodicMemory;
 	private Vector<Integer> addedInPlan;
 
 	//These are used as indexes into the the sensor array
 	private static final int IS_NEW_STATE = 0;
 	private static final int IS_GOAL = 1;
 
 	//Sensor values
 	public static final int NO_TRANSITION = 0;
 	public static final int TRANSITION_ONLY = 1;
 	public static final int GOAL = 2;
 
 	//Global state data
 	private ArrayList<int[]> equivalentStates;
 	private ArrayList<int[]> nonEquivalentStates;
 	private ArrayList<int[]> agentTransitionTable;
 	public static final int UNKNOWN_TRANSITION = -1; //Used to represent an unknown transition in the transition table
 	public static final int DELETED = -2;            //Indicates that a given row in the transition table has been deleted
 	public static final int GOAL_STATE = 0;
 	public static final int INIT_STATE = 1;
 	public static final char UNKNOWN_COMMAND = ' '; //a character guaranteed not
 	//to be in the alphabet
 
 
 	// The state the agent is in based off it's own version of the state machine
 	private int currentState = 1;
 	// A path which the agent expects will take it to the goal
 	// In other words, a method of testing it's hypothesis about two states being the same
 	private ArrayList<Episode> currentPlan = null;
 	//next command to execute in the current plan
 	private int planIndex = -1;
 	// The hypothesis that the agent is currently testing
 	// The agent believes currentHypothesis[0] == currentHypothesis[1] where each entry is a state in the FSM
 	private int[] currentHypothesis;
 	// As the agent adds states to it's personal mapping of the environment, it has to number them
 	// accordingly. This variable keeps track of the next stateID it has not yet used
 	private int currentStateID = 1;
 
 
 	//Reset limit
 	public static final int MAX_RESETS = 1;
 
 	//Tells the agent whether or not to use the reorientation reset
 	private boolean reorientation = true;
 
 	// Turns debug printing on and off
 	boolean debug = true;
 
 	//DEBUG
 	int reorientFailures = 0;
 	int resetCount = 0;
 
 	/**
 	 * The constructor for the agent simply initializes it's instance variables
 	 */
 	public StateMachineAgent() {
 		//int[][] testTransitions = new int[][] {{2, 1, 0},{1, 0, 2},{2, 2, 2}};
 		int[][] testTransitions = new int[][]{{0,1},{1,2},{2,2}};
 		//int[][] testTransitions = new int[][]{{0,1},{1,1}};
 		//env = new StateMachineEnvironment(testTransitions, 3, 3);
 		addedInPlan = new Vector<Integer>();
 		env = new StateMachineEnvironment(testTransitions, 2, 2);
 		alphabet = env.getAlphabet();
 		episodicMemory = new ArrayList<Episode>();
 		//Need a first episode for makeMove
 		episodicMemory.add(new Episode(UNKNOWN_COMMAND, NO_TRANSITION, INIT_STATE));
 		equivalentStates = new ArrayList<int[]>();
 		nonEquivalentStates = new ArrayList<int[]>();
 		agentTransitionTable = new ArrayList<int[]>();
 		int[] zeroRow = new int[alphabet.length];
 		int[] firstState = new int[alphabet.length];
 		//%%%TODO: Make the first element in a transition row the number of that state
 		for (int i = 0; i < zeroRow.length; i++) {
 			zeroRow[i] = /*UNKNOWN_TRANSITION*/0;
 			firstState[i] = UNKNOWN_TRANSITION;
 		}
 		agentTransitionTable.add(zeroRow);
 		agentTransitionTable.add(firstState);
 		possibleBest = new ArrayList<Character>();
 	}
 
 	/**
 	 * Runs through the "Brute Force" algorithm for the agent, setting the
 	 * "best" passphrase to the result
 	 */
 	public void bruteForce() {
 		// Generate an initial path
 		generatePath();
 
 		// DEBUG: try the path that was successful (sanity check)
 		//tryPath(best);
 		//best.printpath();
 
 		// Trim moves off the successful path until we only have the
 		// necessary moves remaining. Make this the new best path
 		best = trimPath(best);
 
 		// // DEBUG: Print out what the agent has determined the shortests path is
 		//best.printpath();
 	}
 
 	/**
 	 * Guesses randomly until a path to the goal is generated
 	 * 
 	 * @return
 	 * 		The path to the goal that was found
 	 */
 	public Path generatePath() {
 		ArrayList<Character> randomPath = new ArrayList<Character>();
 
 		//Use our reset method to make random actions until we reach the goal
 		reset();
 		resetCount++;
 
 		//Pull the episodes we've just created out of memory and parse them into
 		//a path
 		for (int i = 0; i < episodicMemory.size(); i++){
 			randomPath.add(i, episodicMemory.get(i).command);
 		}
 
 		best = new Path(randomPath);
 		return best;
 	}
 
 	/**
 	 * Given a full string of moves, tryPath will enter the moves
 	 * one by one and determine if the entered path is successful
 	 *
 	 * CAVEAT:  This method returns 'true' even if the goal is reached
 	 * prematurely (before the path has been passed)
 	 *
 	 * @param best
 	 * 		An ArrayList of Characters representing the path to try
 	 * 
 	 * @return
 	 * 		A boolean which is true if the path was reached the goal and
 	 * 		false if it did not
 	 */
 	public boolean tryPath(Path best) {
 		boolean[] sensors;
 		// Enter each character in the path
 		for (int i = 0; i < best.size(); i++) {
 			sensors = env.tick(best.get(i));
 			int encodedSensorResult = encodeSensors(sensors);
 			episodicMemory.add(new Episode(best.get(i), encodedSensorResult, INIT_STATE));
 
 			if (sensors[IS_GOAL]) {
 				//DEBUG
 				//System.out.println("Given path works");
 
 				// If we successfully find the goal, return true
 				return true;
 			}
 		}
 
 		//DEBUG
 		//System.out.println("Given path fails");
 
 		// If we make it through the entire loop, the path was unsuccessful
 		return false;
 	}
 
 	/**
 	 * trimPassphrase takes in a passphrase (which has been confirmed as
 	 * successful) and removes one character at a time until it is able to
 	 * determine the shortest version of the passphrase that is still
 	 * successful
 	 * 
 	 * @param toTrim
 	 * 		The passphrase to trim characters from
 	 * @return
 	 * 		toTrim reduced to the least amount of characters possible (not including equivalencies)
 	 */
 	public Path trimPath(Path toTrim) {
 		// Make a copy of the passed-in passphrase so as not to modify it
 		Path trimmed = toTrim.copy();
 		char removed; //Allows us to keep track of the removed character and add it back in if necessary
 
 		for (int i = 0; i < trimmed.size(); i++) {
 			// Trim the current character from the passphrase and test the
 			// result
 			removed = trimmed.get(i);
 			trimmed.remove(i); 
 			if (tryPath(trimmed)) {
 				// If the result is successful, decrement the index, as we
 				// have now no longer seen the element at index i
 				i--;
 			}
 			else {
 				// If the result is unsuccessful, the removed element is an
 				// important character and must be added back in to the
 				// passphrase
 				smartReset();
 				resetCount++;
 				trimmed.add(i, removed);
 
 				//Set the best path equal to the reset path if the reset path is shorter
 				Path maybeBest = getMostRecentPath();
 				if (maybeBest.size() < best.size()) {
 					best = maybeBest;
 				}
 			}
 		}
 		return trimmed;
 	}
 
 	/**
 	 * getMostRecentPath
 	 * 
 	 * Gets the most recent path present in Episodic Memory
 	 * @return The most recent path in episodic memory
 	 */
 	public Path getMostRecentPath() {
 		int lastGoal = findLastGoal(episodicMemory.size() - 2) + 1;
 		ArrayList<Character> pathChars = new ArrayList<Character>();
 		for (int i = lastGoal; i < episodicMemory.size(); i++) {
 			pathChars.add(episodicMemory.get(i).command);
 		}
 		return new Path(pathChars);
 	}
 
 	/**
 	 * Resets the agent by having it act randomly until it reaches the goal.
 	 * This will be changed to a more intelligent scheme later on
 	 */
 	public void reset() {
 		char toCheck;
 		boolean[] sensors;
 		int encodedSensorResult;
 
 		//Currently, the agent will just move randomly until it reaches the goal
 		//and magically resets itself
 		do {
 			toCheck = generateRandomAction();
 			sensors = env.tick(toCheck);
 			encodedSensorResult = encodeSensors(sensors);
 			episodicMemory.add(new Episode(toCheck, encodedSensorResult, INIT_STATE));
 			/*if (episodicMemory.size() > 500000000) {
 				System.exit(0);
 			}*/
 
 		} while (!sensors[IS_GOAL]); // Keep going until we've found the goal
 	}
 
 	/**
 	 * Generates a random action for the Agent to take
 	 * 
 	 * @return A random action for the Agent to take
 	 */
 	public char generateRandomAction() {
 		Random random = new Random();
 		return alphabet[random.nextInt(alphabet.length)];
 	}
 
 
 	/**
 	 * A more intelligent reset for the agent that will cause the agent to try to find a path to the goal
 	 * by examining its episodic memory
 	 */
 	public void smartReset() {
 		if (reorientation) {
 			boolean successCode;
 			for(int i = 0; i < MAX_RESETS; i++) {
 				successCode = smartResetHelper();
 				if (successCode) {
 					return;
 				}
 			}
 			reorientFailures++;
 			reset();
 		}
 		else {
 			reset();
 		}
 	}
 
 	/**
 	 * An intelligent reset method for the agent that resets by searching its previous moves
 	 */
 	public boolean smartResetHelper() {
 		int matchedStringEndIndex = maxMatchedStringIndex();
 		char transitionCharacter;
 		boolean[] sensors;
 		int sensorEncoding;
 		int lastGoal = findLastGoal(episodicMemory.size()) + 1;
 		String action;
 		if (matchedStringEndIndex == -1) {
 			return false;
 		}
 		for (int i = matchedStringEndIndex + 1; i < lastGoal; i++) {
 			transitionCharacter = episodicMemory.get(i).command;
 			sensors = env.tick(transitionCharacter);
 			sensorEncoding = encodeSensors(sensors);
 			action = "" + transitionCharacter + sensorEncoding;
 			episodicMemory.add(new Episode(transitionCharacter, sensorEncoding, INIT_STATE));
 			if (sensorEncoding == GOAL) {
 				return true;
 			}
 
 			//System.err.println(episodicMemory.get(i) + " " + action);
 			if (!episodicMemory.get(i).equals(action)) {
 				//We're lost, so attempt another reset
 				return false;
 			}
 
 			//if (episodicMemory.size() > 5000000) {
 			//	System.exit(0);
 			//}
 		}
 
 		return false;
 	}
 
 	/**
 	 * mapStateMachine
 	 * 
 	 * Continually makes moves until the state machine has been mapped
 	 * 
 	 */
 	private void mapStateMachine() {
 		char currCommand;
 		while (!mappingComplete()) {
 			currCommand = selectNextCommand();
 			makeMove(currCommand);
 			printStateMachine();
 		}
 		printStateMachine();
 	}
 
 	/**
 	 * mappingComplete
 	 * 
 	 * Checks to see if the mapping of the state machine has been completed
 	 * 
 	 * @return True if the mapping is complete, else false
 	 */
 	private boolean mappingComplete() {
 
 		for (int i = 0; i < agentTransitionTable.size(); i++) {
 
 			//Skip the check if the current row has been deleted
 			if (agentTransitionTable.get(i)[0] == DELETED) {
 				continue;
 			}
 
 			//Make sure every space in the transition table has been filled
 			for (int j = 0; j < alphabet.length; j++) {
 				if (agentTransitionTable.get(i)[j] == UNKNOWN_TRANSITION) {
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Finds the ending index of the longest substring in episodic memory before
 	 * the previous goal matching the final string of actions the agent has
 	 * taken
 	 *
 	 * @return The ending index of the longest substring matching the final string of actions
 	 *         the agent has taken
 	 */
 	private int maxMatchedStringIndex() {
 		int lastGoalIndex = findLastGoal(episodicMemory.size());
 		if (lastGoalIndex == -1) {
 			return -1;
 		}
 
 		//If we've just reached the goal, then there is nothing to match
 		if (lastGoalIndex == episodicMemory.size() - 1)
 		{
 			return -1;
 		}
 
 		//Find the longest matching subsequence
 		int maxStringIndex = -1;
 		int maxStringLength = 0;
 		int currStringLength;
 		for (int i = lastGoalIndex-1; i >= 0; i--) {
 			currStringLength = matchedMemoryStringLength(i);
 			if (currStringLength > maxStringLength) {
 				maxStringLength = currStringLength;
 				maxStringIndex = i+1;
 			}
 		}//for
 
 		if (maxStringIndex < 0) {
 			return 0;
 		}
 		else {
 			return maxStringIndex;
 		}
 	}//maxMatchedStringIndex
 
 	/**
 	 * Starts from a given index and the end of the Agent's episodic memory and moves backwards, returning
 	 * the number of consecutive matching characters
 	 * @param endOfStringIndex The index from which to start the backwards search
 	 * @return the number of consecutive matching characters
 	 */
 	private int matchedMemoryStringLength(int endOfStringIndex) {
 		int length = 0;
 		int indexOfMatchingAction = episodicMemory.size() - 1;
 		boolean match;
 		for (int i = endOfStringIndex; i >= 0; i--) {			
 			//We want to compare the command from the prev episode and the 
 			//sensors from the "right now" episode to the sequence at the 
 			//index indicated by 'i'
 			char currCmd = episodicMemory.get(indexOfMatchingAction - 1).command;
 			int currSensors = episodicMemory.get(indexOfMatchingAction).sensorValue;
 			char prevCmd = episodicMemory.get(i).command;
 			int prevSensors = episodicMemory.get(i+1).sensorValue;
 
 			match = ( (currCmd == prevCmd) && (currSensors == prevSensors) );
 
 			if (match) {
 				length++;
 				indexOfMatchingAction--;
 			}
 			else {
 				return length;
 			}
 		}//for
 
 		return length;
 	}//matchedMemoryStringLength
 
 
 	/**
 	 * Searches backwards through the list of move-result pairs from the given index
 	 * @param toStart The index from which to start the backwards search
 	 * @return The index of the previous goal
 	 */
 	private int findLastGoal(int toStart) {
 		for (int i = toStart - 1; i > 0; i --) {
 			if (episodicMemory.get(i).sensorValue == GOAL) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * Takes in an agent's sensor data and encodes it into an integer
 	 * @param sensors The agent's sensor data
 	 * @return the integer encoding of that sensor data
 	 */
 	private int encodeSensors(boolean[] sensors) {
 		int encodedSensorResult;
 
 		if (sensors[IS_GOAL]) {
 			encodedSensorResult = GOAL;
 		}
 
 		else if (sensors[IS_NEW_STATE]) {
 			encodedSensorResult = TRANSITION_ONLY;
 		}
 
 		else {
 			encodedSensorResult = NO_TRANSITION;
 		}
 
 		return encodedSensorResult;
 	}
 
 	/**
 	 * hasTransition
 	 * 
 	 * A helper method to determine if one state has a transition to another
 	 * @param fromState The state to transition from
 	 * @param toState The state to tranisition to
 	 * @return The index into the alphabet array for the transition character, or
 	 * 			-1 if no such character exists
 	 */
 	private int hasTransition(int fromState, int toState) {
 		for (int i = 0; i < agentTransitionTable.get(fromState).length; i++) {
 			if (agentTransitionTable.get(fromState)[i] == toState) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	/**
 	 * makePlanToState
 	 *
 	 * creates a new plan to reach a given state (see {@link #currentPlan}) from
 	 * a given state
 	 *
 	 * @param startID   id of the state to start at
 	 * @param targetID  id of the state we want to reach
 	 */
 	private void makePlanToState(int startID, int targetID) {
		
 		//each path is a sequence of commands to reach the target
 		//state from the Nth state
 		String[] paths = new String[agentTransitionTable.size()];
 		for (int i = 0; i < paths.length; i++) {
 			paths[i] = "";
 		}
 
 		//Create a queue that initially only contains the target state
 		ArrayList<Integer> queue = new ArrayList<Integer>();
 		queue.add(targetID);
 		int currState;
 		int transitionChar;
 
 		while (!queue.isEmpty()) {
 			//Grab the element at the front of the queue
 			currState = queue.get(0);
 			queue.remove(0);
 
 			//Move through each state that doesn't have a path yet. Find the
 			//transition from that state to the current state.
 			for (int i = 1; i < agentTransitionTable.size(); i++) {
 
 				//skip the ones that have a path
 				if (!paths[i].equals("")) continue;
 
 				transitionChar = hasTransition(i, currState);
 
 				//If state i has a transition to the current state and has no
 				//path, set the path for state i equal to the transition
 				//character from state i to the current state added to the front
 				//of the shortest path to the current state, and add state i
 				//onto the queue.
 				if (transitionChar != -1) {
 					paths[i] = alphabet[transitionChar] + paths[currState];
 					queue.add(i);
 				}
 			}//for
 
 			//Make sure there is a path from startID, and if there is, parse the plan
 			if (!paths[startID].equals("")) {
 				ArrayList<Episode> plan = new ArrayList<Episode>();
 				String pathToParse = paths[startID];
 				int[] transitionRow = agentTransitionTable.get(startID);
 				int sensorValue = TRANSITION_ONLY;
 				int episodeState = startID;
 
 				//for each command in the path
 				for (int i = 0; i < pathToParse.length(); i++) {
 					//add the current episode
 					plan.add(new Episode(pathToParse.charAt(i), sensorValue, episodeState));
 
 					//define the next sensor values in the plan
 					//TODO: for now this isn't correct.  The code only
 					//cares about whether the agent should sense goal or not. In
 					//the future, we should have the correct sensor values here
 					//so we can recognize if the expected sensor values don't
 					//match actual and abort the plan then rather than waiting
 					//until we should reach the goal
 					if (targetID == 0 && i == pathToParse.length() - 1) {
 						sensorValue = GOAL;
 					}
 					else {
 						sensorValue = TRANSITION_ONLY;
 					}
 
 					//figure out what state the command takes us to
 					int charIndex = findAlphabetIndex(pathToParse.charAt(i));
 					if(charIndex == -1){
 						System.out.println("character: " + pathToParse.charAt(i));
 					}
 					if (transitionRow[charIndex] == GOAL_STATE) {
 						episodeState = INIT_STATE; //magic teleport to goal
 					}
 					else {
 						episodeState = transitionRow[charIndex];
 					}
 
 					//update to transition row assoc'd with new curr state
 					if(transitionRow[charIndex] != -1) {
 						transitionRow = agentTransitionTable.get(transitionRow[charIndex]);
 					}
 				}//for
 
 				//Tack the goal state on the end to complete the plan
 				plan.add(new Episode(UNKNOWN_COMMAND, sensorValue, episodeState));
 
 				//Voila!
 				currentPlan = plan;
 
 				//Any valid plan must have at least two steps
 				if (plan.size() < 2) {
 					currentPlan = null;
 				}
 			}//if
 		}//while
 
 		planIndex = -1;
 
 		//TODO: Debug
 		System.out.println("Plan from " + startID + " to " + targetID);
 		printPlan(currentPlan);
 		if (currentPlan == null) {
 			System.out.println("foo");
 		}
 		//if (currentPlan != null) System.exit(0);
 
 
 	}//makePlanToState
 
 
 	/**
 	 * getFirstUnkown
 	 *
 	 * Given an index into the transition table, this method discovers the first
 	 * unknown transition in that row in the table and
 	 *
 	 * @param rowIndex  index of the row in the transition table
 	 *
 	 * @return the letter in the alphabet that corresponds to that entry or
 	 * UNKNOWN_COMMAND if it was not found
 	 */
 	private char getFirstUnknown(int rowIndex) {
 		int[] row = agentTransitionTable.get(rowIndex);
 		if (row[0] == DELETED) {
 			return UNKNOWN_COMMAND;
 		}
 		for(int i = 0; i < row.length; ++i) {
 			if (row[i] == UNKNOWN_TRANSITION) {
 				return alphabet[i];
 			}
 		}
 
 		return UNKNOWN_COMMAND;  // no unknown transition
 	}//getFirstUnknown
 
 	private char getUnknown(int rowIndex) {
 		char c = generateRandomAction();
 		int[] row = agentTransitionTable.get(rowIndex);
 		if (row[indexOfCharacter(c)] == UNKNOWN_TRANSITION) {
 			return c;
 		}
 		return getUnknown(rowIndex);
 	}
 
 
 	/**
 	 * selectNextCommand
 	 *
 	 * returns the command the agent should take next depending upon its current
 	 * state, progress, plan, knowledge etc.
 	 *
 	 * SIDE EFFECTS:  a new plan may be created or the current plan advanced
 	 *
 	 * @return the command to take
 	 *
 	 */
 	private char selectNextCommand() {
 		char cmd = ' '; //the command to return
 		if (currentPlan != null) {
 			int x = 2;
 		}
 
 		//If I've never found a path to the goal I can only act randomly until I
 		//find the goal
 		if (best == null) {
 			return generateRandomAction();
 		}
 
 		//%%%BUG: Sometimes the WRONG COMMAND is being returned when a plan exists.
 		//If I have an active plan, extract the next action from that plan
 		else if (currentPlan != null && currentPlan.size() != 0) {
 			Episode currEp = currentPlan.get(planIndex+1);
 			return currEp.command;
 		}
 
 		//If there is no plan, then select an action that I've never done before
 		//from the state that I believe I'm in (explore)
 		else {
 			for (int i = 0; i < alphabet.length; i++) {
 				if (agentTransitionTable.get(currentState)[i] == UNKNOWN_TRANSITION) {
 					cmd = getUnknown(currentState);
 					if (cmd != UNKNOWN_COMMAND) return cmd;
 				}
 			}
 		}
 
 		//Find and delete any unreachable states
 		for (int i = 2; i < agentTransitionTable.size(); i++) {
 			if (agentTransitionTable.get(i)[0] != DELETED) {
 				makePlanToState(INIT_STATE, i);
 				if (currentPlan == null) {
 					agentTransitionTable.get(i)[0] = DELETED;
 				}
 			}
 		}
 
 		//if we reach this point there is no unknown transition from the current
 		//state.  Find the lowest numbered state that has an unknown transition
 		//and make a plan to get there
 		int state = 0;
 		while (cmd == UNKNOWN_COMMAND) {
 			state++;
 			cmd = getFirstUnknown(state);
 		}
 
 		//make a plan to reach that unknown state
 		this.currentPlan = null;
 		makePlanToState(this.currentState, state);
 		if (currentPlan != null) {
 			cmd = currentPlan.get(planIndex+1).command;
 			return cmd;
 		}
 
 		//if something went wrong just act randomly
 		//(I don't think this should ever happen.)
 		return generateRandomAction();
 	}
 
 	/**
 	 * Returns the index of the given character in the alphabet array
 	 * @param toCheck the character to find the index of
 	 * @return the index of toCheck
 	 */
 	private int indexOfCharacter(char toCheck) {
 		for (int i = 0; i < alphabet.length; i++) {
 			if (alphabet[i] == toCheck) {
 				return i;
 			}
 		}
 
 		return -1;
 	}
 
 	/**
 	 * acceptCurrentHypothesis
 	 *
 	 * the current hypothetic equivlency is not believed to be true.  Update hte
 	 * list of known equivalents and also update the transition table
 
      From the journal:
      If were testing a state equivalency hypothesis, then your hypothesis
      becomes "fact" and update your equivalencies list and the transition table
      appropriately.  As part of this merging you should check to see if there
      any two rows that match exactly (unknown transitions don't match).  If
      there are, then they are the same state, merge them.
 	 */
 	private void acceptCurrentHypothesis() {
 		//Delete all states added while doing this plan, then reset the list
 		for (int i : addedInPlan) {
 			agentTransitionTable.get(i)[0] = DELETED;
 		}
 
 		//Replace states in previous episodes with correct ones
 		int k = 0;
 		for (int i = episodicMemory.size() - currentPlan.size(); i < episodicMemory.size(); i++) {
 			episodicMemory.get(i).stateID = currentPlan.get(k).stateID;
 			k++;
 		}
 
 		equivalentStates.add(currentHypothesis);
 		if (currentHypothesis[1] == INIT_STATE) {
 			currentHypothesis[1] = currentHypothesis[0];
 			currentHypothesis[0] = INIT_STATE;
 		}
 		mergeTwoStates(currentHypothesis[0], currentHypothesis[1]);
 		for (int i = 0; i < agentTransitionTable.size(); i++) {
 			for (int j = i + 1; j < agentTransitionTable.size(); j++) {
 				if (isCompatibleRow(agentTransitionTable.get(i), agentTransitionTable.get(j), true)) {
 					mergeTwoStates(i, j);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * mergeTwoStates
 	 * 
 	 * Takes two equivalent states and merges them together
 	 */
 	private void mergeTwoStates(int state1, int state2) {
 		//Merge the two states together
 		System.out.println("State " + state1 + " has been merged with State " + state2);
 		for (int i = 0; i < alphabet.length; i++) {
 			if (agentTransitionTable.get(state2)[i] != UNKNOWN_TRANSITION && agentTransitionTable.get(state1)[i] == UNKNOWN_TRANSITION) {
 				agentTransitionTable.get(state1)[i] = agentTransitionTable.get(state2)[i];
 			}
 		}
 
 		//Change all transitions to state2 to transitions to state1
 		for (int i = 0; i < agentTransitionTable.size(); i++) {
 			for (int j = 0; j < alphabet.length; j++) {
 				if (agentTransitionTable.get(i)[j] == state2) {
 					agentTransitionTable.get(i)[j] = state1;
 				}
 			}
 		}
 
 		//Mark state2 as deleted
 		agentTransitionTable.get(state2)[0] = DELETED;
 	}
 
 	/**
 	 * cleanupFailedPlan
 	 *
 	 * if a plan fails, then the current hypotheses are assumed to be
 	 * incorrect.  The two states are added the {@link #nonEquivalentStates}
 	 * list.  A new state is added to the transition table and a new epmem is
 	 * added to the episodic memory that indicates we transitioned to that
 	 * state. this.currentState is also updated.
                  - if you were testing an equivalency then your
                    hypothesis becomes false.  Record the non-equivalency
                    appropriately
 
                  - otherwise you must have been trying to get to a
                    state as per 7d above.  I'm not sure what to do
                    here as this indicates that a previous equivalency
                    is actually false.  Ignore for now.
 	 */
 	private void cleanupFailedPlan(int mergedSensors) {
 		//Reset the list of states added while following this plan
 		addedInPlan = new Vector<Integer>();
 
 		if(currentPlan.size() <= 1)
 		{
 			//ignore for now? reset?
 			System.out.println("Plan should never be of length 1!");
 			System.exit(-1);
 		}
 
 		//Add the current hypothesis to the list of non equivalent states if the hypothesis exists
 		if (currentHypothesis != null)
 		{
 			nonEquivalentStates.add(currentHypothesis);
 		}
 		currentHypothesis = null;
 		Episode lastEpisode = episodicMemory.get(episodicMemory.size() - 1);
 		char action = lastEpisode.command;
 		int lastState = lastEpisode.stateID;
 		int actionIndex = findAlphabetIndex(action);
 
 		//Remove the current plan and reset the plan index
 		currentPlan = null;
 		planIndex = -1;
 
 	}//cleanupFailedPlan
 
 	/**
 	 * A helper method which determines a given letter's
 	 * location in the alphabet
 	 * 
 	 * @param letter
 	 * 		The letter who's index we wish to find
 	 * @return
 	 * 		The index of the given letter (or -1 if the letter was not found)
 	 */
 	private int findAlphabetIndex(char letter) {
 		// Iterate the through the alphabet to find the index of letter
 		for(int i = 0; i < alphabet.length; i++){
 			if(alphabet[i] == letter)
 				return i;
 		}
 
 		// Error if letter is not found
 		return -1;
 	}
 
 
 	/**
 	 * isCompatibleRow
 	 *
 	 * given two rows in the transition table, this method verifies that they
 	 * are "compatible" i.e., all corresponding entries that are both not
 	 * unknown are the same value.
 	 *
 	 */
 	private boolean isCompatibleRow(int[] row1, int[] row2, boolean hypothesisMerged) {
 		//System.out.println("Checking if rows are compatible");
 
 		//A deleted row is incompatible with everything
 		if (row1[0] == DELETED || row2[0] == DELETED) {
 			return false;
 		}
 
 		//%%%IMPORTANT: If the rule that a state must have so many transitions to itself is removed, this will
 		//no longer function properly and will need to be changed
 		//The goal row should NEVER be merged
 		boolean goal1 = true;
 		boolean goal2 = true;
 		for (int i = 0; i < row1.length; i++) {
 			if (row1[i] != GOAL_STATE) {
 				goal1 = false;
 			}
 			if (row2[i] != GOAL_STATE) {
 				goal2 = false;
 			}
 		}
 		if (goal1 || goal2) {
 			return false;
 		}
 
 
 		boolean knownShared = false;
 		if (!hypothesisMerged) knownShared = true;
 		
 		// Go through each entry in the rows to compare them
 		for(int i = 0; i < row1.length; i++) { 
 			// If the rows are not equivalent
 			if(row1[i] != row2[i]) {
 				// And neither of them are unknown, the rows are not equivalent
 				if( !(row1[i] == UNKNOWN_TRANSITION || row2[i] == UNKNOWN_TRANSITION)){
 					return false;
 				}
 			}
 
 			// Ensure there is at least one known match between the two rows
 			//TODO:  Put this back in?  Removed by :AMN: and HNK because the current state is likely to 
 			// be brand new and thus have -1 for all transitions out.  How can this ever pass?
 			else if (row1[i] != UNKNOWN_TRANSITION && row2[i] != UNKNOWN_TRANSITION) {
 				knownShared = true;
 			}
 		}
 		return knownShared;
 		//return true;
 	}
 
 	/**
 	 * makeMove
 	 *
 	 * issues a given command and updates the episodic memory, transition table,
 	 * current plan, etc. as a result.
 	 *
 	 * @param cmd the command to issue
 	 */
 	private void makeMove(char cmd) {
 
         //TODO: REMOVE (DEBUG)
         System.out.println("Executing command: " + cmd);
 
         
 		possibleBest.add(cmd);
 
 		//Complete the current episode with the given command
 		Episode currEp = this.episodicMemory.get(this.episodicMemory.size() - 1);
 		currEp.command = cmd;
 
 		boolean[] sensors = env.tick(cmd);
 		int mergedSensors = encodeSensors(sensors);
 		int commandIndex = findAlphabetIndex(cmd);
 
 		if (mergedSensors == GOAL) {
 			if (best == null || possibleBest.size() < best.size()) {
 				best = new Path(possibleBest);
 			}
 			possibleBest = new ArrayList<Character>();
 		}
 
 		//if we're in the middle of a plan it needs to be updated
 		if (this.currentPlan != null && this.currentPlan.size() != 0) {
 
 			//Advance the plan and verify that the sensors match
 			this.planIndex++;
 			Episode currPlanEp = this.currentPlan.get(this.planIndex+1);
 			//TODO: We are only comparing sensor values for the last step of the plan. At some point we will have to 
 			//check it every step.
 			if (planIndex == currentPlan.size() - 2 && 
 					((currPlanEp.sensorValue == GOAL && mergedSensors != GOAL) || 
 							(currPlanEp.sensorValue != GOAL && currentState != currPlanEp.stateID))) {
 				// %%%DEBUG
 				System.out.println("Our plan failed!");
 				//Plan has failed
 				cleanupFailedPlan(mergedSensors);
 			}
 
 			//Examine the transition to extract what state I believe I'm in
 			Episode prev = episodicMemory.get(episodicMemory.size() - 1);
 			if (prev.stateID < 0) {
 				System.out.println("I'm returning   " + prev.stateID);
 				return;
 			}
 			int[] row = agentTransitionTable.get(prev.stateID);
 			this.currentState = row[commandIndex];
 
 			//If I don't know where I am create a new state and update the table
 			if (this.currentState == UNKNOWN_TRANSITION) {
 				//Create a new state for this new circumstance if we are not at the goal
 				if (mergedSensors != GOAL) {
 					currentStateID++;
 					row[commandIndex] = currentStateID;
 					currentState = currentStateID;
					if (currentPlan != null) {
						addedInPlan.add(currentStateID);
					}
 
 					//%%%TBD  add a row to the transition table to support this
 					int[] newRow = new int[alphabet.length];
 					for (int i = 0; i < newRow.length; i++) {
 						newRow[i] = UNKNOWN_TRANSITION;
 					}
 					agentTransitionTable.add(newRow);
 				}
 			}
 
 			//Magical reset if the agent has hit the goal
 			if (mergedSensors == GOAL) {
 				this.currentState = INIT_STATE;
 			}
 
 			//Add an episode that reflects our belief that this hypothesis is
 			//correct
 			//%%%ISSUE: What if the hypothesis is not correct?
 			//%%%ANSWER: Doesn't matter, it's fucked either way.
 			//%%%TODO: Return to this, replace episodes with plan episodes if the plan was successful
 			Episode now = new Episode(UNKNOWN_COMMAND, mergedSensors, currentState);
 			episodicMemory.add(now);
 
 			//If we've reached the goal episode for the plan the remove it
 			//And verify all hypotheses
 			if (currentPlan != null && this.planIndex >= this.currentPlan.size() - 2) {
 				//if this was a plan to reach the goal then any hypothetic
 				//equivalencies need to be accepted
 				if (currPlanEp.stateID == INIT_STATE) {
 					// %%%DEBUG
 					System.out.println("We're accepting our hypothesis!");
 					acceptCurrentHypothesis();
 				}
 				this.currentPlan = null;
 				this.planIndex = -1;
 			}
 		}
 
 		//This 'else' covers the the case where there was no plan.  The agent
 		//has just taken a random or semi-random action
 		else {
 			//Examine the transition to extract what state I believe I'm in
 			Episode prev = episodicMemory.get(episodicMemory.size() - 1);
 			if (prev.stateID < 0) {
 				System.out.println("I'm returning   " + prev.stateID);
 				return;
 			}
 			int[] row = agentTransitionTable.get(prev.stateID);
 			this.currentState = row[commandIndex];
 
 			//If I don't know where I am create a new state and update the table
 			if (this.currentState == UNKNOWN_TRANSITION) {
 				//Create a new state for this new circumstance if we are not at the goal
 				if (mergedSensors != GOAL) {
 					currentStateID++;
 					row[commandIndex] = currentStateID;
 					currentState = currentStateID;
 
 					//add a row to the transition table to support this
 					int[] newRow = new int[alphabet.length];
 					for (int i = 0; i < newRow.length; i++) {
 						newRow[i] = UNKNOWN_TRANSITION;
 					}
 					agentTransitionTable.add(newRow);
 				}
 			}
 
 			//Magical reset if the goal has been reached
 			if (mergedSensors == GOAL) {
 				this.currentState = INIT_STATE;
 				row[commandIndex] = GOAL_STATE;
 			}
 
 			//Add an episode to reflect what just happened
 			Episode now = new Episode(UNKNOWN_COMMAND, mergedSensors, this.currentState);
 			episodicMemory.add(now);
 			//this.currentState = currentStateID;
 			
 			//TODO: REMOVE (DEBUG)
 			printStateMachine();
 			System.out.print("  ");
 			for(int i = 0; i < this.episodicMemory.size(); ++i) {
 				System.out.print("" + i + "        ");
 			}
 			System.out.println();
 			System.out.println(this.episodicMemory);
 			
 			//If we've reached the GOAL and reset then there's no need to do anything else
 			if (this.currentState == INIT_STATE) return;
 
 			//Find data about previous state that may be the same as the current
 			//state
 
 			int equivIndex = maxMatchedStringIndex();
 			if (equivIndex != -1) {
 				Episode equivEpisode = episodicMemory.get(equivIndex);
 				if (equivEpisode.stateID < 0) {
 					return;
 				}
 				int[] equivRow = agentTransitionTable.get(equivEpisode.stateID);
 
 				//Make sure the equiv episode is not the current one
 				if (equivIndex >= episodicMemory.size() - 1) return;
 
 				//verify this equiv state has a compatible transition table entry to
 				//current state
 				row = agentTransitionTable.get(this.currentState);
 				if (!isCompatibleRow(row, equivRow, false)) return;
 
 				//Make sure that the two states are not the same
 				if (equivEpisode.stateID == this.currentState) return;
 
 				//verify that we haven't already discovered that these
 				//states aren't equal
 				for (int i = 0; i < nonEquivalentStates.size(); i++) {
 					if (nonEquivalentStates.get(i)[0] == equivEpisode.stateID 
 							&& nonEquivalentStates.get(i)[1] == this.currentState) {
 						return;
 					}
 					if (nonEquivalentStates.get(i)[1] == equivEpisode.stateID 
 							&& nonEquivalentStates.get(i)[0] == this.currentState) {
 						return;
 					}
 				}
 
 				//Don't make a hypothesis that any state is equal to the goal state
 				if (equivEpisode.stateID == GOAL_STATE || currentState == GOAL_STATE) {
 					return;
 				}
 
 				//hypothesize that equiv state equals the current state
 				currentHypothesis = new int[2];
 				currentHypothesis[0] = equivEpisode.stateID;
 				currentHypothesis[1] = this.currentState;
 
                 //TODO:  REMOVE (DEBUG)
                 System.out.println("hypothesis: " + equivEpisode.stateID +
                                     " == " + this.currentState);
 
 				//Make a plan to reach the goal based upon the hypothesis
 				makePlanToState(equivEpisode.stateID, GOAL_STATE);
 			}
 
 		}//else
 
 		//MAKE SURE the agent has reset to state 1 if it's at the goal
 		if (mergedSensors == GOAL) {
 			currentState = INIT_STATE;
 		}
 
 	}//makeMove
 
 	public void addNewState()  { 
 
 	}
 
 	/**
 	 * runs multiple trials wherein a random state machine is solved and the
 	 * resulting 'best passphrase' for each is analyzed
 	 */
 	public static void main(String [ ] args)
 	{
 		StateMachineAgent ofSPECTRE;
 		ofSPECTRE = new StateMachineAgent();
 		System.out.println("ENVIRONMENT INFO:");
 		ofSPECTRE.env.printStateMachine();
 		ofSPECTRE.env.printPaths();
 
 
 		ofSPECTRE.mapStateMachine();
 		//ofSPECTRE.best.printpath();
 		ofSPECTRE.episodicMemory = new ArrayList<Episode>();
 
 	}
 
 	/**
 	 * A method which iterates through and prints out
 	 * the two-dimension array that represents the state machine
 	 */
 	public void printStateMachine() {
 		System.out.print("     ");
 		for(int i = 0; i < alphabet.length; ++i) {
 			System.out.printf("%3c", alphabet[i]);
 		}
 		System.out.println();
 
 		for (int i = 0; i < agentTransitionTable.size(); i++) {
 			if (agentTransitionTable.get(i)[0] == DELETED) {
 				continue;
 			}
 			System.out.printf("%s%3d: ", currentState == i ? "*" : " ", i);
 
 			for (int j = 0; j < alphabet.length; j++) {
 				System.out.printf("%3d", agentTransitionTable.get(i)[j]);
 			}
 			System.out.println();
 		}
 
 		System.out.print("     ");
 		for(int i = 0; i < alphabet.length; ++i) {
 			System.out.printf("%3c", alphabet[i]);
 		}
 		System.out.println();
 	}
 
 	/** prints out a plan for debugging */
 	public void printPlan(ArrayList<Episode> plan)
 	{
 		System.out.print("Plan: ");
 		if (plan == null)
 		{
 			System.out.println("null");
 			return;
 		}
 		for(Episode ep : plan)
 		{
 			System.out.print(ep + ",");
 		}//for
 		System.out.println();
 	}
 
 
 }//class StateMachineAgent
