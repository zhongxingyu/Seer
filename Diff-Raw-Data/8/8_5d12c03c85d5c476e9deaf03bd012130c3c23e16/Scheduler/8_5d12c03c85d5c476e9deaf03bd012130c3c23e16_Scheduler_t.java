 package Model;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 public class Scheduler {
 	private List<Pair<Task, Integer>> executionLog;
 	private List<SThread> threads, freeThreads;	
 	private long begin, end;
 	private List<Node> lock;
 	private Graph graph;
 	private double timeElapsed;
 	
 	public void addExecution(int thread, Task task){
 		executionLog.add(new Pair(task, thread));
 	}
 	
 	//TODO make it work for many threads
 	public void execute() throws Exception {
 		begin = System.currentTimeMillis();
 		executeAux();
 	}
 	
 	private void executeAux() throws Exception {
 		if(graph == null) return;
 		while(graph.hasOrphan() || !allThreadsFree()){
 			while(graph.hasOrphan() && this.hasFreeThread()){
 				//Bind a thread and a task and run it
 				Node node = graph.popOrphan();
 				SThread t = this.popFreeThread();
 				this.addExecution(threads.indexOf(t), node.getTask());
 				//System.out.println("NumParents:"+node.getNumParents()+" "+node.getTaskName());
 				t.addTask(node);
 				t.setLock(lock);
 				t.start();
 			}
 			
 			synchronized(lock){
 				//System.out.println("waiting");
				if(!allThreadsFree()){
					lock.wait();
				}
				//System.out.println("going");
 				while(!lock.isEmpty()){
 					Node n = lock.get(0);
 					graph.removeNode(n);
 					lock.remove(n);
 				}
 			}
 		}
 //		synchronized(lock){
 //			while(!allThreadsFree()){
 //				System.out.println("waiting the completion");
 //				lock.wait();
 //			}
 //		}
 		end = System.currentTimeMillis();
 		timeElapsed += (end-begin)/1000.;
 		//System.out.println("NumThreads:"+numThreadsRunning);
 	}
 	
 	//It removes a thread from the free threads list and execute a task on it
 	private synchronized SThread popFreeThread() throws Exception {
 		if(freeThreads.isEmpty()) throw new Exception("Trying to use more threads than what is available.");
 		SThread t = freeThreads.get(0);
 		freeThreads.remove(0);
 		return t;
 	}
 
 	public boolean hasFreeThread() {
 		return !freeThreads.isEmpty();
 	}
 	
 	public boolean allThreadsFree(){
 		return freeThreads.size() == threads.size();
 	}
 	
 	public void addFreeThread(SThread thread){
 		SThread newt = new SThread(this);
 		this.freeThreads.add(0, newt);
 		Collections.replaceAll(this.threads, thread, newt);
 	}
 
 	public synchronized SThread getThread(int index){
 		return this.threads.get(index);
 	}
 	
 	public Graph getGraph(){
 		return graph;
 	}
 	
 	public int getNumThreads(){
 		return this.threads.size();
 	}
 	
 	public double getTimeElapsed(){ return timeElapsed;}
 	
 	public Scheduler(int numThreads, Graph g){
 		executionLog = new LinkedList();
 		this.graph = g;
 		this.lock = new LinkedList();
 		this.threads = new LinkedList();
 		this.freeThreads = new LinkedList();
 		this.timeElapsed = 0;
 		for(int i = 0; i < numThreads; i++){
 			SThread n = new SThread(this);
 			this.threads.add(n);
 			this.freeThreads.add(n);
 		}
 	}
 	
 	public Scheduler(int numThreads){
 		this(numThreads, null);
 	}
 	
 	@Override
 	public String toString(){
 		String out = "Schedule:\n";
 		for(Pair<Task, Integer> order: executionLog){
 			out += String.format("\tTask: %s\t Thread: %s Status: %s\n", order.getKey().getName(), order.getValue(), order.getKey().getStatus());
 		}
 		out += String.format("----------------\nElapsed: %7.3f sec\n", timeElapsed);
 		return out;
 	}
 }
