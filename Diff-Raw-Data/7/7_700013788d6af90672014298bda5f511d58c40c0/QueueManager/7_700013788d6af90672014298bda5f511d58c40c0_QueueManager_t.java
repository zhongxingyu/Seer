 import java.util.*; 
 
 /* The QueueManager Class handles the management of the process state queues.
  * It extends the NonPreemptiveScheduler class in order to properly manage the processes within each queue.
  */
 public class QueueManager extends NonPreemptiveScheduler{
 	//The five default queues, one for each process state.
 	private static LinkedList<Process> newQueue = new LinkedList<Process>();
 	private static LinkedList<Process> readyQueue = new LinkedList<Process>();
 	private static LinkedList<Process> runQueue = new LinkedList<Process>();
 	private static LinkedList<Process> waitQueue = new LinkedList<Process>();
 	private static LinkedList<Process> terminateQueue = new LinkedList<Process>();
 	private static int cycleTime = 0;			//Keeps track of which cycle it is currently working in, in order to manage processes
 	
 	public QueueManager(int schedulingType){	
 		super(schedulingType);
 	}
 	
 	public void addProcess(Process process){
 		moveToNew(process, newQueue);
 		moveFromNew();
 	}
 	
 	public void update(){
 		moveFromNew();
 		moveFromRun();
 		moveFromWait();
 		moveFromReady();
 	}
 	
 	public boolean isFinished()
 	{
 		if(newQueue.isEmpty() && readyQueue.isEmpty() && runQueue.isEmpty() && waitQueue.isEmpty())
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void setDefaultDeviceTable()
 	{
 		Device d1 = new Device("d1");
 		Device d2 = new Device("d2");
 		Device d3 = new Device("d3");
 		Device d4 = new Device("d4");
 		Device d5 = new Device("d5");
 		Device d6 = new Device("d6");
 		Device d7 = new Device("d7");
 		Device d8 = new Device("d8");
 		Device d9 = new Device("d9");
 		Device d10 = new Device("d10");
 	}
 	
 	public void setDefaultResourceTable()
 	{
 		Resource r1 = new Resource("r1");
 		Resource r2 = new Resource("r2");
 		Resource r3 = new Resource("r3");
 		Resource r4 = new Resource("r4");
 		Resource r5 = new Resource("r5");
 		Resource r6 = new Resource("r6");
 		Resource r7 = new Resource("r7");
 		Resource r8 = new Resource("r8");
 		Resource r9 = new Resource("r9");
 		Resource r10 = new Resource("r10");
 	}
 	
 	public void cycleIncrement(){
 		cycleTime ++;
 	}
 	
 	public void moveFromNew(){
 		if (!newQueue.isEmpty()){
			while(newQueue.peek().getSubmitTime() < cycleTime){
 				readyQueue = moveFromNewToReady(newQueue.poll(), readyQueue);
 			}
 		}
 	}
 	
 	public void moveFromReady(){
 		while(runQueue.isEmpty() && !readyQueue.isEmpty()){
 			runQueue = moveFromReadyToRunning(readyQueue.poll(), runQueue);
 			moveFromRun();
 		}
 	}
 	
 	public void moveFromRun(){
		if(!runQueue.isEmpty() && (runQueue.peek().getAllDevice().size() != runQueue.peek().getRequiredDevices().length
				|| runQueue.peek().getAllResource().size() != runQueue.peek().getRequiredResources().length)){
 			runQueue.peek().setState("wait");
 			waitQueue = moveFromRunning(runQueue.poll(), waitQueue);
 		}
 		else{
 			runQueue.element().setBurstCycle(runQueue.element().getBurstsCycle()-1);
 			if(runQueue.peek().getBurstsCycle() <= 0){
 				terminateQueue = moveFromRunning(runQueue.poll(), terminateQueue);
 			}
 		}
 	}
 	
 	public void moveFromWait(){
 		Process p;
 		if(!waitQueue.isEmpty()){
 			int i = 0;
 			while(i < waitQueue.size()){
 				p = waitQueue.get(i);
 				if(p.getResourcesAvailable() == true && p.getDevicesAvailable() == true){
 					readyQueue = moveFromWaitingToReady(waitQueue.remove(i), readyQueue);
 				}
 				else{
 					i++;
 				}
 			}
 		}
 	}
 	
 	public String toString(){
 		String returnString = "Current Cycle Time: " + cycleTime,
 				newS, readyS, runS, waitS, termS;
 		
 		returnString += "\n|----New----|---Ready---|--Running--|--Waiting--|-Terminated|";
 		int newSize = newQueue.size(), readySize = readyQueue.size(), 
 			runSize = runQueue.size(), waitSize = waitQueue.size(),
 			terminateSize = terminateQueue.size(), max;
 		
 		List<Integer> findMax = Arrays.asList(newSize, readySize, runSize, waitSize, terminateSize);
 		
 		max = Collections.max(findMax);
 		
 		Process[] newArray = newQueue.toArray(new Process[newQueue.size()]), readyArray = readyQueue.toArray(new Process[readyQueue.size()]),
 				runArray = runQueue.toArray(new Process[runQueue.size()]), waitArray = waitQueue.toArray(new Process[waitQueue.size()]),
 				terminateArray = terminateQueue.toArray(new Process[terminateQueue.size()]);
 		
 		for(int i=0; i < max; i++){
 			if(i < newSize){
 				newS = newArray[i].getName();
 				for(int c = newS.length(); c < 11; c++){
 					newS += ".";
 				}
 			}
 			else{
 				newS = "...........";
 			}
 			if(i < readySize){
 				readyS = readyArray[i].getName();
 				for(int c = readyS.length(); c < 11; c++){
 					readyS += ".";
 				}
 			}
 			else{
 				readyS = "...........";
 			}
 			if(i < runSize){
 				runS = runArray[i].getName();
 				for(int c = runS.length(); c < 11; c++){
 					runS += ".";
 				}
 			}
 			else{
 				runS = "...........";
 			}
 			if(i < waitSize){
 				waitS = waitArray[i].getName();
 				for(int c = waitS.length(); c < 11; c++){
 					waitS += ".";
 				}
 			}
 			else{
 				waitS = "...........";
 			}
 			if(i < terminateSize){
 				termS = terminateArray[i].getName();
 				for(int c = termS.length(); c < 11; c++){
 					termS += ".";
 				}
 			}
 			else{
 				termS = "...........";
 			}
 			
 			
 			returnString += "\n|" + newS + "|" + readyS + "|" 
 							+ runS + "|" + waitS + "|" + termS + "|";
 		}
 		return returnString;
 	}
 }
