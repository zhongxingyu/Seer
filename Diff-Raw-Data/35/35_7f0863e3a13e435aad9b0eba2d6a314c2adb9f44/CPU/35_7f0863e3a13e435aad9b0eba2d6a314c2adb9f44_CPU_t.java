 package tdt4186.exercise3.code;
 
 public class CPU {
 
 	/** The queue of processes waiting for free memory */
 	private Queue cpuQueue;
 	/** A reference to the statistics collector */
 	private Statistics statistics;
 	/** The amount of memory in the memory device */
 	/** The amount of free memory in the memory device */
 	private Process activeProcess;
 	
 	private Gui gui;
 	private long maxCpuTime;
 
 	/**
 	 * Creates a new memory device with the given parameters.
 	 * @param memoryQueue	The memory queue to be used.
 	 * @param memorySize	The amount of memory in the memory device.
 	 * @param statistics	A reference to the statistics collector.
 	 */
     public CPU(Queue cpuQueue, long maxCpuTime, Statistics statistics, Gui gui) {
 		this.cpuQueue = cpuQueue;
 		this.statistics = statistics;
 		this.gui = gui;
 		this.maxCpuTime = maxCpuTime;
     }
     
     public void insertProcess(Process p) {
 		cpuQueue.insert(p);
 	}
     
     public boolean isIdle() {
     	boolean isIdle = false;
     	if (this.activeProcess == null) isIdle = true;
     	return isIdle;
     }
     
     public Process getActive() {
     	Process p = this.activeProcess;
     	this.activeProcess = null;
     	return p;
     }
 
 	public long getMaxCpuTime() {
 		return maxCpuTime;
 	}
     
 	public Process start() {
 		if (!cpuQueue.isEmpty()) {
 			Process p = (Process) cpuQueue.removeNext();
 			this.activeProcess = p;
 			gui.setCpuActive(p);
 			return p;
 		}
 		else {
 			this.activeProcess = null;
 			gui.setCpuActive(null);
 			return this.activeProcess;
 		}
 	}
 	
 	public void updateTime(long timePassed) {
        this.statistics.cpuQueueLengthTime += this.cpuQueue.getQueueLength() * timePassed;
        if (this.cpuQueue.getQueueLength() > this.statistics.longestCpuQueueLength)
        	this.statistics.longestCpuQueueLength = this.cpuQueue.getQueueLength();
     }
 
 }	
