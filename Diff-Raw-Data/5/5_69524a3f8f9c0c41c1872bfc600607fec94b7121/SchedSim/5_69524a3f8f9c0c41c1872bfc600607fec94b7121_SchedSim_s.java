 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 class SchedSim {
 
 	private static int maxProcesses; 	// cap on total processes for simulation
 	private static int maxCPUbursts; 	// cap on total CPU bursts per process
 
 	private enum Algorithm { // algorithm to use for entire run of simulation
 		FCFS, SJF, SRTF
 	}	
 	private static Algorithm algorithm;
 
 	//Data structures for event simulation
 	private static Queue<Event> eventQueue = new PriorityQueue<Event>();		//The event queue. Holds arrivals, IO returns, bursts, etc
 	private static Queue<Process> newProcesses = new LinkedList<Process>();		//The new process list for the simulator.
 	private static Queue<Process> IOQueue = new LinkedList<Process>(); 			//IOQueue. 
 	private static Queue<Process> FCFSreadyQueue = new LinkedList<Process>();
 
 	public static void main(String [] args){
 		//Args: filename maxProcesses maxCPUbursts algorithm [quantum]
 		if(!parseRandomFile(args)){//Parse the file passed into the program. If the parse fails (file does not exist, etc), return
 			return;
 		}
 		//If parsing was successful...
 		
 		if(algorithm == algorithm.FCFS){
 			System.out.println("FCFS completed in " + runFCFS() + " seconds");
 		}
 		if(algorithm == algorithm.SJF){
 			System.out.println("SJF completed in " + runSJF() + " seconds");
 		}
 		if(algorithm == algorithm.SRTF){
 			System.out.println("SRTF completed in " + runSRTF() + " seconds");
 		}
 	}
 
 
 	private static double runFCFS(){	//if simulating an FCFS queue
 		System.out.println("Read successful. Starting FCFS simulation");								//Seems good to print out what type of simulation the program is running
 		double runtime = 0;																				//Total runtime for the simulation
 		//Main event loop
 		//For every arrival, take a process off of the new queue and add to the ready queue
 		//If both the CPU and IO device are not busy, remove a process from the ready queue and generate events for the process
 		//Used to track the state of the CPU and IO device. In this simulation, one is busy, or none are (not both)
 		boolean CPUBusy = false;
 		boolean IOBusy = false;
 		Process activeProcess = null;
 		int burst = 0;//Used to keep track of which burst the process will execute next
 		while(!(eventQueue.isEmpty())){
 			Event nextEvent = eventQueue.remove();
 			runtime = nextEvent.time;//Update runtime
 			if(nextEvent.type == Event.Type.ARRIVAL){//Process the arrival case
 				//Change the process state to ready and place on the ready queue
 				Process enqueueProcess = newProcesses.remove();
 				enqueueProcess.state = Process.State.READY;
 				FCFSreadyQueue.add(enqueueProcess);
 			}
 			//Track the currently running process
 			if(activeProcess == null){
 				//Set the current process to run on the CPU and add a new event
 				activeProcess = FCFSreadyQueue.remove();
 				activeProcess.state = Process.State.RUNNING;
 				Event newEvent = new Event();
 				newEvent.type = Event.Type.CPU_DONE;
 				newEvent.time = runtime + activeProcess.lengthOfCPUbursts[burst];
 				eventQueue.add(newEvent);
 			}
 			//burst is a variable used to keep track of the burst that has just finished.
 			//burst is incremented every time an IO burst finishes
 			//If burst is incremented to the highest value, the next CPU completion event will be the last for that process.
 			//Therefore the activeprocess is set to null. This will make the scheduler pull the next process off of the ready queue
 			if(nextEvent.type == Event.Type.CPU_DONE){//Process the CPU event completion case
 				CPUBusy = false;
 				Event newEvent = new Event();
 				newEvent.time = runtime + activeProcess.lengthOfIObursts[burst];
 				newEvent.type = Event.Type.IO_DONE;
 				activeProcess.state = Process.State.WAITING; //Process is now waiting on IO
 				eventQueue.add(newEvent);
 			}
 			if(nextEvent.type == Event.Type.IO_DONE){
 				IOBusy = false;
 				Event newEvent = new Event();
 				burst++;
 				newEvent.time = runtime + activeProcess.lengthOfCPUbursts[burst];
 				newEvent.type = Event.Type.CPU_DONE;
 				activeProcess.state = Process.State.RUNNING; //IO has completed, process is now running CPU burst
 				eventQueue.add(newEvent);
 				if(burst == activeProcess.lengthOfIObursts.length){
 					activeProcess = null;
 				}
 			}
 		}
 		return runtime;
 	}
 
 	private static double runSJF(){
 		return 0;
 	}
 	
 	private static double runSRTF(){
 		return 0;
 	}
 	
 	private static boolean parseRandomFile(String [] args){
 		FileInputStream binaryFile = null;
 		String simulationType;									//The type of simulated scheduler being run
 		//Here is where the random file is parsed and new tasks are generated
 		try {
 			binaryFile = new FileInputStream(new File(args[0]));
 			double nextArrivalTime = 0;							//When the next process arrives after the one being read from the file
 			maxProcesses = Integer.parseInt(args[1]);
 			maxCPUbursts = Integer.parseInt(args[2]);
 			simulationType = args[3];							//MLFQ, FCFS, SJF, SRTF, etc
 
 			for(int i = 0; i < maxProcesses;i++){	//Get the information for each process from the random file
 				Process randomProcess = new Process();
 				Event newProcessArrival = new Event();
 				newProcessArrival.time = nextArrivalTime;
 				newProcessArrival.type = Event.Type.ARRIVAL;//Event indicating a new process has been added to the job list
 
 				eventQueue.add(newProcessArrival);
 				nextArrivalTime += ((double)((int)binaryFile.read() & 0xff))/10;						//Set up the nextArrivalTime for the next process. This is the time that the next process arrives *after* the current one arrives
 
 				randomProcess.cpubursts = (((int)binaryFile.read() & 0xff) % maxCPUbursts) + 1;		//Set up the number of CPU bursts this process will have. Called 'n'
 				randomProcess.lengthOfCPUbursts = new double[randomProcess.cpubursts];					//There will be "n" bursts
 				randomProcess.lengthOfIObursts = new double[randomProcess.cpubursts - 1];				//There are "n" - 1 IO Bursts (one between each CPU burst)
 
 				//Populate arrays with the duration of IO and CPU bursts. 
 				//Times for each are produced from sequential bytes from random file
 				for(int j = 0; j < randomProcess.cpubursts; j++){									//Read in the size of each cpu burst
 					randomProcess.lengthOfCPUbursts[j] = ((double)((int)binaryFile.read() & 0xff))/25.6;
 				}
 				for(int j = 0; j < randomProcess.cpubursts - 1; j++){								//Read in the size of each IO burst
 					randomProcess.lengthOfIObursts[j] = ((double)((int)binaryFile.read() & 0xff))/25.6;
 				}
 				randomProcess.state = Process.State.NEW;
 				newProcesses.add(randomProcess);		//Enqueue the process				
 			}
 			binaryFile.close();							//Close the FileInputStream
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;			//Read failed
 		}
 		//Set the value of the enum algorithm (Currently supports FCFS, SJF, and SRTF)
 		if(simulationType.compareTo("FCFS") == 0){
 			algorithm = algorithm.FCFS;
 		}
 		if(simulationType.compareTo("SJF") == 0){
 			algorithm = algorithm.SJF;
 		}
 		if(simulationType.compareTo("SRTF") == 0){
 			algorithm = algorithm.SRTF;
 		}
 		return true;//Read was successful
 	}
 }
