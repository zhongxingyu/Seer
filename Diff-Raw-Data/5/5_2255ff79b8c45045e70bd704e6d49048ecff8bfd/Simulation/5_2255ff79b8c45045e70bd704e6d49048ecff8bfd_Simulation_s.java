 /******************************************/
 //      Operating Systems Simulation      //
 //                                        //
 //        Created by Trevor Suarez        //
 // Project Designed by Dr. Roger Marshall //
 //                                        //
 //     Plymouth State University 2012     //
 /******************************************/
 
 // Imports (libraries and utilities)
 import java.util.List;
 import java.util.Arrays;
 import java.util.Random;
 
 // External imports
 import com.google.common.collect.*;
 import org.apache.commons.lang3.StringUtils;
 
 // Simulation main engine class
 public class Simulation {
 	// Declare final variables (constants)
 	public static final int VERSION_MAJOR_NUMBER = 1; // The version's major number
 	public static final int VERSION_MINOR_NUMBER = 2; // The version's minor number
 	public static final int VERSION_REVISION_NUMBER = 0; // The version's revision number
 
 	public static final int MAX_MEMORY = 2048; // Total available user memory
 	public static final int MAX_EVENTS = 500; // Maximum number of events to be fired before quitting
 
 	private static final String[] MEMORY_ALGORITHMS = {"Best", "Worst", "First"};
 	private static final String[] STATE_NAMES = {"Hold", "Ready", "Run", "Suspend_System", "Suspend_User", "Blocked", "Done"}; // The names of each possible state
 
 	private static final int[] INITIAL_JOB_STATES = {1, 3, 5}; // The initially active job states (correspond with the state names key/index)
 	private static final int INITIAL_JOB_SIZE = 320; // The amount of memory that each initial ACTIVE job has
 	private static final int INITIAL_JOB_TIME = 6; // The CPU time requirement of each initially ACTIVE job
 	private static final int INITIAL_NUM_HELD = 10; // The number of initially inactive/held jobs
 	private static final int TOTAL_NUM_JOBS = INITIAL_JOB_STATES.length + INITIAL_NUM_HELD; // The total number of jobs in the OS Simulation
 
 	private static final int PROCESS_RUN_TIME = 3; // The number of "CPU Time Units" that the currently running process uses on each event cycle
 
 	private static final int NUM_TIMES_RUN = 3; // The number of times that the system should run before quitting
 
 	// Program wide objects
 	public static boolean debugMode;
 	public static Random random;
 	public static int numOfProcesses; // Use this so we can always have a UNIQUE identifier
 
 	// Class wide objects
 	private static boolean runOnce;
 	private static boolean seeFinishConditions;
 	private static boolean helpMode;
 	private static boolean versionMode;
 	private static Long randomSeed;
 	private static EventManager states;
 	private static MemoryManager memory;
 	private static List<Event> events;
 	private static boolean systemRunning;
 	private static int generatedEventCount;
 	private static int firedEventCount;
 
 	// Constructor
 	private static void run() {
 		// Let's declare the run times
 		int numberTimesToRun = NUM_TIMES_RUN;
 
 		// If runOnce is set, only run once
 		if (runOnce) {
 			numberTimesToRun = 1;
 		}
 
 		// Let's loop through until we've reached the desired number of times ran
 		for (int i = 0; i < numberTimesToRun; i++) {
 			// Let's declare some properties
 			int bestRun = MAX_EVENTS + 1; // Set to an impossible event number
 			int worstRun = -1;
 
 			// Let's create a random number seed manually, so we can re-use it later
 			randomSeed = System.currentTimeMillis(); // Use this seed for debugging: 0x00000000fffffff1L
 
 			// Ok, let's do EACH of the memory algorithms
 			for (String memoryAlgorithm : MEMORY_ALGORITHMS) {
 				// Let's say what algorithm we're using
 				System.out.println("Running system using memory algorithm \"" + memoryAlgorithm + "\"");
 
 				// Let's initialize/reset some variables
 				numOfProcesses = 0;
 				generatedEventCount = 0;
 				firedEventCount = 0;
 
 				// Instanciate program wide objects
 				random = new Random(randomSeed);
 
 				// Let's create/start our event manager
 				states = new EventManager();
 
 				// Let's create our memory manager
 				memory = new MemoryManager(memoryAlgorithm);
 
 				// Let's fill our event array list with our randomized events
 				buildEventsList();
 
 				// Let's initialize the system with our initial conditions
 				initialConditions();
 
 				// Ok. Everything's set up, so let's run the system
 				startSystem();
 
 				if (generatedEventCount < bestRun) {
 					bestRun = generatedEventCount;
 				}
 				if (generatedEventCount > worstRun) {
 					worstRun = generatedEventCount;
 				}
 			}
 
 			if (bestRun != worstRun) {
 				System.out.println("DIFFERENT!!!");
 			}
 			else {
 				i--;
 			}
 		}
 	}
 
 	// Private function to setup the initial conditions
 	private static void initialConditions() {
 		// Only show if debugMode is on
 		if (debugMode) {
 			System.out.println("System showing pre-inital conditions:");
 
 			outputMemoryTable();
 			outputStateTable();
 		}
 
 		// Let's create our initially active processes
 		for (int state : INITIAL_JOB_STATES) {
 			// Create the process
 			Process job = new Process(INITIAL_JOB_SIZE, INITIAL_JOB_TIME);
 
 			// Add the process to the event manager's map
 			states.addProcess(job, STATE_NAMES[state]);
 
 			// Only show if debugMode is on
 			if (debugMode) {
 				System.out.println("Process created at state: \"" + STATE_NAMES[state] + "\" with ID: " + job.getId() + ", Size: " + job.getSize() + "k, and Time: " + job.getReqTime());
 			}
 
 			// Add the process to the system's memory
 			memory.addProcess(job);
 
 			// Only show if debugMode is on
 			if (debugMode) {
 				System.out.println("Process " + job.getId() + " added to memory with size " + job.getSize() + "k");
 			}
 		}
 
 		// Now, let's create our initially inactive/held jobs
 		for (int i = 0; i < INITIAL_NUM_HELD; i++) {
 			// Create the process
 			Process job = new Process();
 
 			// Add the process to the event manager's map
 			states.addProcess(job, "Hold");
 
 			// Only show if debugMode is on
 			if (debugMode) {
 				System.out.println("Process created at state: \"Hold\" with ID: " + job.getId() + ", Size: " + job.getSize() + "k, and Time: " + job.getReqTime());
 			}
 		}
 
 		// Print initial tables
 		System.out.println("Initial Conditions:");
 		outputMemoryTable();
 		outputStateTable();
 	}
 
 	// Private function to build the event list
 	private static void buildEventsList() {
 		// First of all, let's instanciate an array list
 		events = Lists.newArrayList();
 
 		// Let's add our events to the array list
 		events.add(new Event("Hold", "Ready")); // Event from and to
 		events.add(new Event("Ready", "Run"));
 		events.add(new Event("Run", "Blocked"));
 		events.add(new Event("Blocked", "Ready"));
 		events.add(new Event("Run", "Suspend_User")); // User
 		events.add(new Event("Run", "Suspend_System")); // Timer/System
 		events.add(new Event("Blocked", "Done")); // System killed
 		events.add(new Event("Suspend_User", "Done")); // User killed
 		events.add(new Event("Suspend_User", "Ready")); // User
 		events.add(new Event("Suspend_System", "Ready")); // Timer/System
 		events.add(new Event("Run", "Done"));
 		events.add(new Event("Ready", "Hold"));
 	}
 
 	// Private function to run the process that is currently granted the CPU
 	private static void runProcess() {
 		// Let's get the process in the Run state
 		Process process = states.getProcess("Run"); // May be null
 
 		// If we actually got back a process
 		if (process != null) {
 			// Only show if debugMode is on
 			if (debugMode) {
 				System.out.println("Running process: " + process.toString());
 			}
 
 			// Let's run the process for a set "time"
 			process.useTime(PROCESS_RUN_TIME);
 
 			// If the process is "DONE" (its used time has reached its required time)
 			if (process.isDone()) {
 				// We need to fire a Run->Done event
 				fireEvent(new Event("Run", "Done"));
 
 				// Only show if debugMode is on
 				if (debugMode) {
 					System.out.println("Process finished after running");
 				}
 			}
 		}
 		else {
 			// Only show if debugMode is on
 			if (debugMode) {
 				System.out.println("WARNING!! No process to run");
 				System.out.println("Trying to load a process");
 			}
 
 			// We REALLY shouldn't let the OS just sit dormant for the user, so
 			// Let's fire a Ready->Run event
 			fireEvent(new Event("Ready", "Run"));
 		}
 	}
 
 	// Private function to generate a random event from the events list
 	private static Event generateRandomEvent() {
 		// Create the number's maximum range
 		int randMax = events.size();
 
 		// Generate a random int within the constraints
 		int n = random.nextInt(randMax);
 
 		// Let's get the event at that random position n
 		Event generatedEvent = events.get(n);
 
 		// Return the randomly generated event
 		return generatedEvent;
 	}
 
 	// Private function to fire the event passed to it
 	private static boolean fireEvent(Event event) {
 		// Let's get the first process in the "from" location
 		Process process = states.getProcess(event.from); // May be null
 
 		// If we actually got back a process
 		// Let's make sure its not a Ready->Run... because that may need to bring in a process (so the process would be null right now)
 		if (process != null || (event.from == "Ready" && event.to == "Run")) {
 			// Only show if debugMode is on
 			if (debugMode && process != null) {
 				System.out.println("Event " + event.toString() + " firing on process: " + process.toString());
 			}
 			else if (debugMode && (event.from == "Ready" && event.to == "Run")) {
 				System.out.println("Event " + event.toString() + " firing");
 			}
 
 			// For a hold->ready event, let's make sure the system has enough memory to hold the new process
 			if (event.from == "Hold" && event.to == "Ready") {
 				// Let's make sure this is all possible
 				if (memory.isAddPossible(process) && states.isAddPossible(process, event.to)) {
 					// If the process is successfully added to memory AND the process successfully changed state 
 					if (memory.addProcess(process) && states.changeProcessState(event)) {
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Ready->Run event
 			else if (event.from == "Ready" && event.to == "Run") {
 				// Let's first make sure that the Run state isn't full
 				if (states.isStateFull(event.to)) {
 					// We need to fire an event to get the process out of the run state
 					fireEvent(new Event(event.to, "Suspend_System"));
 				}
 
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// Let's first make sure that the Ready state isn't empty
 					if (states.isStateEmpty(event.from)) {
 						// We need to fire an event to get a process in the ready state
 						// Let's see if we can grab one from Suspend_System
 						if (fireEvent(new Event("Suspend_System", event.from)) != true) {
 							// If we couldn't grab one from Suspend_System, we should try to grab one from Hold
 							fireEvent(new Event("Hold", event.from));
 						}
 					}
 
 					// If the process successfully changed state 
 					if (states.changeProcessState(event)) {
 						// Only show if debugMode is on
 						if (debugMode) {
 							System.out.println("Event " + event.toString() + " worked!");
 						}
 
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Run->Blocked event
 			else if (event.from == "Run" && event.to == "Blocked") {
 				// Let's first make sure that the Blocked state isn't full
 				if (states.isStateFull(event.to)) {
 					// We need to fire an event to get the process out of the blocked state
 					fireEvent(new Event(event.to, "Done"));
 				}
 
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process successfully changed state 
 					if (states.changeProcessState(event)) {
 						// Ok, it succeeded, but now there's nothing "running" (in the run event), so let's fix that
 						fireEvent(new Event("Ready", event.from));
 
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Blocked->Ready event
 			else if (event.from == "Blocked" && event.to == "Ready") {
 				// Let's first make sure that the Ready state isn't full
 				if (states.isStateFull(event.to)) {
 					// We need to fire an event to get the process out of the blocked state
 					fireEvent(new Event(event.to, "Hold"));
 				}
 
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process successfully changed state 
 					if (states.changeProcessState(event)) {
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Run->Suspend_User event
 			else if (event.from == "Run" && event.to == "Suspend_User") {
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process successfully changed state 
 					if (states.changeProcessState(event)) {
 						// Ok, it succeeded, but now there's nothing "running" (in the run event), so let's fix that
 						fireEvent(new Event("Ready", event.from));
 
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Run->Suspend_System event
 			else if (event.from == "Run" && event.to == "Suspend_System") {
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process successfully changed state 
 					if (states.changeProcessState(event)) {
 						// Ok, it succeeded, but now there's nothing "running" (in the run event), so let's fix that
 						fireEvent(new Event("Ready", event.from));
 
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Blocked->Done event
 			else if (event.from == "Blocked" && event.to == "Done") {
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process is successfully removed from memory AND the process successfully changed state 
 					if (memory.removeProcess(process) && states.changeProcessState(event)) {
 						// Ok, it succeeded, but now there's nothing "running" (in the run event), so let's fix that
						fireEvent(new Event("Ready", event.from));
 
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Suspend_User->Done event
 			else if (event.from == "Suspend_User" && event.to == "Done") {
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process is successfully removed from memory AND the process successfully changed state 
 					if (memory.removeProcess(process) && states.changeProcessState(event)) {
 						// Ok, it succeeded, but now there's nothing "running" (in the run event), so let's fix that
						fireEvent(new Event("Ready", event.from));
 
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Suspend_User->Ready event
 			else if (event.from == "Suspend_User" && event.to == "Ready") {
 				// Let's first make sure that the Ready state isn't full
 				if (states.isStateFull(event.to)) {
 					// We need to fire an event to get the process out of the blocked state
 					fireEvent(new Event(event.to, "Hold"));
 				}
 
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process successfully changed state 
 					if (states.changeProcessState(event)) {
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Suspend_System->Ready event
 			else if (event.from == "Suspend_System" && event.to == "Ready") {
 				// Let's first make sure that the Ready state isn't full
 				if (states.isStateFull(event.to)) {
 					// We need to fire an event to get the process out of the blocked state
 					fireEvent(new Event(event.to, "Hold"));
 				}
 
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process successfully changed state 
 					if (states.changeProcessState(event)) {
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Run->Done event
 			else if (event.from == "Run" && event.to == "Done") {
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// If the process is successfully removed from memory AND the process successfully changed state 
 					if (memory.removeProcess(process) && states.changeProcessState(event)) {
 						// Ok, it succeeded, but now there's nothing "running" (in the run event), so let's fix that
 						fireEvent(new Event("Ready", event.from));
 
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 			// For a Ready->Hold event, let's remove the largest process in memory
 			else if (event.from == "Ready" && event.to == "Hold") {
 				// Let's get the largest process in the "from" location
 				Process largestReadyProcess = states.getLargestProcess(event.from); // May be null
 
 				// If we actually got back a process
 				if (largestReadyProcess != null) {
 					// Let's make sure this is all possible
 					if (states.isAddPossible(process, event.to)) {
 						// If the process is successfully removed from memory AND the process successfully changed state 
 						if (memory.removeProcess(largestReadyProcess) && states.changeProcessStateToHold(largestReadyProcess, event.from)) {
 							// If we made it here, the event has succeeded
 							return true;
 						}
 					}
 				}
 			}
 			// All other events
 			else {
 				// Let's make sure this is all possible
 				if (states.isAddPossible(process, event.to)) {
 					// Let's change the processes state
 					if (states.changeProcessState(event)) {
 						// If we made it here, the event has succeeded
 						return true;
 					}
 				}
 			}
 		}
 
 		// Increment the fired event counter
 		firedEventCount++;
 		
 		return false;
 	}
 
 	// Private function to check if the system has finished its job
 	private static boolean checkFinished() {
 		// If the total number of generated events has hit 500
 		if (generatedEventCount == MAX_EVENTS) {
 			// Let's print out the memory and state table
 			System.out.println("State and Memory at generated event #" + generatedEventCount);
 			outputMemoryTable();
 			outputStateTable();
 
 			// Only show if debugMode is on
 			if (debugMode || seeFinishConditions) {
 				System.out.println("STOPPING! The maximum number of events: " + MAX_EVENTS + " have been generated. We're not getting anywhere.");
 			}
 
 			return true;
 		}
 
 		// If every job is in the "Done" state
 		if (TOTAL_NUM_JOBS == states.getProcessCount("Done")) {
 			// Let's print out the memory and state table
 			System.out.println("State and Memory at generated event #" + generatedEventCount);
 			outputMemoryTable();
 			outputStateTable();
 
 			// Only show if debugMode is on
 			if (debugMode || seeFinishConditions) {
 				System.out.println("STOPPING! The OS is \"finished\". Every process is in the \"Done\" state.");
 				System.out.println("Finished by generating " + generatedEventCount + " events and successfully firing " + firedEventCount + " events.");
 			}
 
 			return true;
 		}
 
 		// If it got here, the system hasn't finished yet
 		return false;
 	}
 
 	// Private function to output the memory manager's memory table
 	public static void outputMemoryTable() {
 		// Let's set our column padding
 		int colPadding = 14;
 		String string;
 
 		// Let's create a couple of new lines
 		System.out.println("\r\n");
 
 		// Let's create our header
 		String[] colHeaders = {"Block", "Process", "Size"};
 
 		// Let's loop through and print our header
 		for (String header : colHeaders) {
 			// Let's create a string that's padded and centered
 			string = StringUtils.center(header, colPadding);
 			System.out.print(string);
 		}
 
 		// Let's create a couple of new lines
 		System.out.println("\r\n");
 		
 		// Now, let's loop through our memory manager
 		int i = 0;
 		List<Process> systemMemory = memory.getMemoryArrayList();
 		for (Process process : systemMemory) {
 			// Let's print out the block number
 			string = StringUtils.center("" + i, colPadding);
 			System.out.print(string);
 
 			// If the process isn't dead
 			if (process.isProcessDead() != true) {
 				// Let's print out the process id
 				string = StringUtils.center("#" + process.getId(), colPadding);
 				System.out.print(string);
 			}
 			// Otherwise, just print out that its free
 			else {
 				string = StringUtils.center("--", colPadding);
 				System.out.print(string);
 			}
 
 			// Let's print out the block's size
 			string = StringUtils.center(process.getSize() + "k", colPadding);
 			System.out.print(string);
 
 			// Let's end the line
 			System.out.println();
 
 			// Increment i
 			i++;
 		}
 
 		// Finally, to finish, let's create a couple of new lines
 		System.out.println("\r\n");
 	}
 
 	// Private function to output the event manager's state table
 	private static void outputStateTable() {
 		// Let's set our column padding
 		int colPadding = 14;
 
 		// Let's create a couple of new lines
 		System.out.println("\r\n");
 
 		// Let's create our header
 		for (String state : STATE_NAMES) {
 			// skip the other suspend, so we can just merge them later
 			if (state != "Suspend_System" && state != "Suspend_User") {
 				// Let's create a string that's padded and centered
 				String string = StringUtils.center(state, colPadding);
 				System.out.print(string);
 			}
 			else if (state == "Suspend_System") {
 				// Let's create a string that's padded and centered
 				String string = StringUtils.center("Suspend_Sys", colPadding);
 				System.out.print(string);
 			}
 			else if (state == "Suspend_User") {
 				// Let's create a string that's padded and centered
 				String string = StringUtils.center("Suspend_Usr", colPadding);
 				System.out.print(string);
 			}
 		}
 
 		// Let's create a couple of new lines
 		System.out.println("\r\n");
 
 		// Let's create our body, now
 		// Let's create as many rows as their are the most amount of processes in one state
 		for (int i = 0; i < states.getMostFilledStateCount(); i++) {
 			// Loop through each state name
 			for (String state : STATE_NAMES) {
 				// Let's create a process
 				Process process = null;
 
 				// Let's get the process
 				process = states.getProcessAtIndex(state, i);
 
 				// We could get back a null process
 				if (process != null) {
 					// Let's create a string that's padded and centered
 					String string = StringUtils.center(process.toString(), colPadding);
 					System.out.print(string);
 				}
 				else {
 					// Let's create a string that's padded and centered
 					String string = StringUtils.center("", colPadding);
 					System.out.print(string);
 				}
 			}
 
 			// Let's create a couple of new lines
 			System.out.println();
 		}
 
 		// Finally, to finish, let's create a couple of new lines
 		System.out.println("\r\n");
 	}
 
 	// Private function to actually start the system
 	private static void startSystem() {
 		// Mark the system as running
 		systemRunning = true;
 
 		// While the system is still running
 		while (systemRunning) {
 			// First, let's run our process
 			runProcess();
 
 			// Let's generate a random event
 			Event generatedEvent = generateRandomEvent();
 
 			// Let's increment the total number of events that have been generated
 			generatedEventCount++;
 			
 			// Only show if debugMode is on
 			if (debugMode) {
 				System.out.println("GENERATED Event " + generatedEvent.toString() + " firing");
 			}
 
 			// Let's actually fire the event that's been generated
 			boolean eventSucceeded = fireEvent(generatedEvent);
 
 			// If the event succeeded
 			if (eventSucceeded) {
 				// Only show if debugMode is on
 				if (debugMode) {
 					System.out.println("Event succeeded: " + generatedEvent.toString());
 				}
 			}
 			else {
 				// Only show if debugMode is on
 				if (debugMode) {
 					System.out.println("Event failed: " + generatedEvent.toString());
 				}
 			}
 
 			// Let's check to see if the system has finished its job
 			if (checkFinished()) {
 				systemRunning = false;
 			}
 			// Every 25 generated events, we should output the memory and state table
 			else if ((generatedEventCount % 25) == 0) {
 				System.out.println("State and Memory at generated event #" + generatedEventCount);
 				outputMemoryTable();
 				outputStateTable();
 			}
 		}
 	}
 
 	// Private function to print the help psuedo-manual to the screen
 	private static void printHelp() {
 		// Let's first print the usage
 		String usage = "Usage: ossim [OPTION]";
 		System.out.println(usage);
 
 		// Now let's print the summary of the program
 		String summary = "Program to simulate a typical operating system's event driven state model.";
 		summary += "\r\nThe program starts with processes in an initial state and runs random events until they are all \"done\"";
 		System.out.println(summary + "\r\n");
 
 		// Optional parameters/arguments
 
 		// Debug
 		String shortCode = "-d";
 		String longCode = "--debug";
 		String description = "Enable a very verbose debug-style output";
 		System.out.format("%4s, %-14s%-40s\r\n", shortCode, longCode, description);
 
 		// Run Once
 		shortCode = "-r";
 		longCode = "--runonce";
 		description = "Set the simulation to only run once for each memory allocation algorithm";
 		System.out.format("%4s, %-14s%-40s\r\n", shortCode, longCode, description);
 
 		// See Finish
 		shortCode = "-f";
 		longCode = "--seefinish";
 		description = "See the finishing condition output. Automatically enabled with debug mode enabled.";
 		System.out.format("%4s, %-14s%-40s\r\n", shortCode, longCode, description);
 
 		// Help
 		shortCode = "";
 		longCode = "--help";
 		description = "Display this help and exit";
 		System.out.format("%4s  %-14s%-40s\r\n", shortCode, longCode, description);
 
 		// Version
 		shortCode = "";
 		longCode = "--version";
 		description = "Output version information and exit";
 		System.out.format("%4s  %-14s%-40s\r\n", shortCode, longCode, description);
 	}
 
 	// Private function to print the version information to the screen
 	private static void printVersion() {
 		// Let's decide the line width (padding)
 		int lineWidth = 62;
 
 		// Let's print out the version number
 		String versionNum = StringUtils.center("OSSIM - Operating Systems Simulation Project - Version " + VERSION_MAJOR_NUMBER + "." + VERSION_MINOR_NUMBER, lineWidth);
 		System.out.println(versionNum + "\r\n");
 
 		// Now let's print out the creator's information
 		String createdBy = StringUtils.center("Created by Trevor Suarez", lineWidth);
 		System.out.println(createdBy);
 
 		// Now let's print out the designer's information
 		String designedBy = StringUtils.center("Project designed by Dr. Roger Marshall", lineWidth);
 		System.out.println(designedBy + "\r\n");
 
 		// Finally, let's print out the organization information
 		String organization = StringUtils.center("Plymouth State University 2012", lineWidth);
 		System.out.println(organization + "\r\n");
 	}
 
 	// Private function to check the passed arguments
 	private static void checkArguments(String[] args) {
 		// Let's get all the arguments as an array
 		List<String> arguments = Arrays.asList(args);
 		
 		// If debug has been passed, lets enable it
 		if (arguments.contains("--debug") || arguments.contains("-d")) {
 			debugMode = true;
 		}
 
 		// If runonce has been passed, lets enable it
 		if (arguments.contains("--runonce") || arguments.contains("-r")) {
 			runOnce = true;
 		}
 
 		// If seefinish has been passed, lets enable it
 		if (arguments.contains("--seefinish") || arguments.contains("-f")) {
 			seeFinishConditions = true;
 		}
 
 		// If help has been passed, lets enable it
 		if (arguments.contains("--help")) {
 			helpMode = true;
 		}
 
 		// If version has been passed, lets enable it
 		if (arguments.contains("--version")) {
 			versionMode = true;
 		}
 		
 	}
 
 	// Main function
 	public static void main(String[] args) {
 		// Let's check for arguments
 		checkArguments(args);
 
 		// If help mode hasn't been enabled, actually run the system
 		if (!helpMode && !versionMode) {
 			// Begin the simulation
 			run();
 		}
 		else if (helpMode) {
 			printHelp();
 		}
 		else if (versionMode) {
 			printVersion();
 		}
 	}
 
 } // End Simulation class
