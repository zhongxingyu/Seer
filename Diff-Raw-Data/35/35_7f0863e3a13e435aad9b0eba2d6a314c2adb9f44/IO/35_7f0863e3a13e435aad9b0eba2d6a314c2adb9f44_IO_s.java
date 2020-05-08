 package tdt4186.exercise3.code;
 
 public class IO {
 
 	private Queue ioQueue;
 	private Statistics statistics;
 	private long ioWait;
 	private Gui gui;
 
 	private Process activeProcess = null;
 
 
 	public IO(Queue ioQueue, Statistics statistics, EventQueue eventQueue, long ioWait, Gui gui) {
 		this.ioQueue = ioQueue;
 		this.statistics = statistics;
 		this.gui = gui;
 		this.ioWait = ioWait;
 
 	}
 
 	public boolean addProcess(Process p) {
 		ioQueue.insert(p);
 		if (activeProcess == null) {
 			start();
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public Process start() {
 		if (ioQueue.isEmpty()) {
 			return null;
 		}
 		Process p = (Process) ioQueue.removeNext();
 		activeProcess = p;
 		gui.setIoActive(p);
 		return p;
 	}
 
 	public Process getProcess() {
 		Process p = activeProcess;
 		activeProcess = null;
 		gui.setIoActive(activeProcess);
 		return p;
 	}
 
 	public long getIoTime() {
 		return (long) (Math.random() * (ioWait * 2) + ioWait / 2);
 
 	}
 
 	public void updateTime(long timePassed) {
//		statistics.ioQueueLengthTime += ioQueue.getQueueLength() * timePassed;
//		if (ioQueue.getQueueLength() > statistics.largestIOQueue) {
//			statistics.largestIOQueue = ioQueue.getQueueLength();
//		}
 	}
 	
 }
