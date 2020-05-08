 import java.util.ArrayList;
 import java.util.PriorityQueue;
 
 /**
 	Priority Queues Driver
  	@author Tom Ravenscroft
 	@version version 0.0
 	@see {@link queue_1.java High Priority Queue}
 	@see {@link queue_2.java Low Priorit Queue}
  */
 
 public class driver {
 
 	// Test files.
 	static String file1 = "../tests/test1.txt";
 	static String file2 = "../tests/test2.txt";
 	static String file3 = "../tests/test3.txt";
 	static Proc[] test1;
 	static Proc[] test2;
 	static Proc[] test3;
 	
 	static ArrayList<Proc> results;
 	
 	static Proc cpuTask;
 	static Proc processedTask;
 	static int quantum = 10;
 	static int time = 0;
 	
 	/*
 		Driver method
 	*/
 	public static void main(String[] args) {
 	
 		System.out.println("Driver online");
 		
 		// Read all the tests.
 		testParser reader = new testParser();
 		
 		test1 = reader.parseTests(file1);
 		test2 = reader.parseTests(file2);
 		test3 = reader.parseTests(file3);
 		
 		results = new ArrayList<Proc>();
 		
 		reader.testFileParse(test1); System.out.println();
 		reader.testFileParse(test2); System.out.println();
 		reader.testFileParse(test3); System.out.println();
 		
 		queue_1 Q1 = new queue_1();
 		queue_2 Q2 = new queue_2();
 		
 		 //testRR();
 		//testHPFRR();
 		
 		dispatcher(Q1, Q2, test1);
 	}
 	
 	public static void dispatcher(queue_1 HPQ, queue_2 LPQ, Proc[] test){
 		
 		// Count the order in which processes arrive.
 		int nextProc = 0;
 
 		// Flag to indicate which queue is active. T = HPQ, F = LPQ.
 		boolean queueSelect = false;
 
 		// Execute while there are processes still to finish.
 		while(!HPQ.isEmpty() || !LPQ.isEmpty() || nextProc < test.length){	
 			
 			// Print time and current queue size.
 			System.out.println("Time: " + time);
 		
 			while(nextProc < test.length && test[nextProc].creationTime == time){
 					
 				// Check the priority threshold.
 				if(test1[nextProc].priority > 3){
 				
 					HPQ.add(test[nextProc]);	
 					System.out.println("Added  " + test[nextProc].name + " to high priority queue.");
 				}
 				else{
 					
 					LPQ.add(test[nextProc]);	
 					System.out.println("Added  " + test[nextProc].name + " to low priority queue.");
 				}
 				
 				nextProc ++;
 			}
 			
 			// Print the upated queue sizes.
 			System.out.println("HPQ size: " + HPQ.size());
 			System.out.println("LPQ size: " + LPQ.size());
 			
 			// Check to see which queue has access.
 			// Get the next process from the active queue and run it.
 			if(!HPQ.isEmpty()){
 				
 				cpuTask = HPQ.remove();
 				queueSelect = true;
 			}
 			else if(!LPQ.isEmpty()){
 				
 				cpuTask = LPQ.remove();
 				queueSelect = false;
 			}
 			
 			// Run the process.
 			processedTask = cpuExecute(cpuTask, quantum, queueSelect);
 			
 			// Check if the process has finished.
 			if(processedTask.runTime < processedTask.burstTime){
 				
 				// If the task's priority is > 3 add to HPQ. Else add to LPQ.
 				if(processedTask.priority > 3){
 					HPQ.add(processedTask);
 				}
 				else{
 					LPQ.add(processedTask);
 				}
 			}
 			else{
 
 				System.out.println("\n ***** Task: " + processedTask.name + " done @ t = " + time + "\n");
 				results.add(processedTask);
 			}
 			
 			// Update the priorities in the low priority queue (every 8 runs of other processes).
 			for(int i = 0; i < LPQ.size(); i++){
 				
 				if((time - LPQ.get(i).lastRunTime) % 80 == 0) {  
 					
 					System.out.println("///// LPQ raising priority of: " + LPQ.get(i).name);
 					LPQ.get(i).priority++;
 				}
 			}
 		}
 		
 		// Print the results.
 		printResults(results);
 	}
 	
 	// Test the HPF/RR method on the high priority queue only.
 	public static void testHPFRR(){
 		
 		queue_1 HPQ = new queue_1();
 		int nextProc = 0;
 		
 		// Add everything to the queue.
 		//for(int i = 0; i < test1.length; i++){
 			
 			// Add element to start of queue.
 			//HPQ.add(test1[i]);
 		//}
 		
 		// Keep working until the queue is empty. (This won't be the condition in the final version).
 		while(!HPQ.isEmpty() || nextProc < test1.length){
 			
 			
 			// Print time and current queue size.
 			System.out.println("Time: " + time);
 			System.out.println("HPQ size: " + HPQ.size());
 			
 			// Add all processes processes starting now @ time: t = now.
 			while(nextProc < test1.length && test1[nextProc].creationTime == time){
 				
 							// Check the priority threshold.
 							if(test1[nextProc].priority > 3){
 								
 								HPQ.add(test1[nextProc]);	
 							}
 							else{
 								
 								System.out.println("##### Task: " + test1[nextProc].name + " is not important enough.");
 							}
 				
 				//System.out.println("+++++ Process: " + test1[nextProc].name + " begins @ t = " + test1[nextProc].creationTime);
 				nextProc ++;
 			}
 					
 			// Move the head of the queue into the CPU.
 			cpuTask = HPQ.remove();
 			
 			// Simulate executing the task.
 			processedTask = cpuExecute(cpuTask, quantum, true);
 			
 			// Check if the current task has completed in the RR queue.
 			if(processedTask.runTime < processedTask.burstTime){
 				
 				if(processedTask.priority > 3){
 					HPQ.add(processedTask);
 				}
 				else{
 					System.out.println("##### Task: " + processedTask.name + " is not important enough.");
 				}
 			}
 			else{
 				
 				System.out.println("\n ***** Task: " + cpuTask.name + " done @ t = " + time + "\n");
 			}
 		}
 		
 		// Tests added.
 			// Run the highest priority task for 10 units of time.
 			// If the process is finished. Don't re-add it to the queue.
 			// If the processes total run-time is a multiple of 30, reduce it's priority to a minimum of 1.
 		
 		// If the CPU process has priority <= 3. Add it to the end of queue 2.
 		
 		// Queue 2
 		
 		// Process is run.
 			// Set it's age to 0.
 			
 			// Need to have an UPDATE_LPQ method to implement aging and change the priorities.
 			
 			// If the time run by the process is divisible by 8, decrease it's priority by 1.
 	}
 	
 	public static Proc cpuExecute(Proc p, int q, boolean hp){
 		
 		// Set the process' initial ready time.
 		if(p.readyTime == -1) { p.readyTime = time; }
 		
 		// Update the running process' time to completion.
 		p.runTime  += q;
 		p.lastRunTime = time;
 		
		// Update the current time.
 		time+= quantum;
 		
 		p.age = time - cpuTask.creationTime; // Age calculation needs to be changed (maybe).
 		p.order = ++Proc.numProcs;
 		
 		// Print the current task in the CPU.
 		printTask(cpuTask);
 		
 		// Post processing for tasks from high priority queue.
 		if(hp){
 			
 			// Decrease the priority of the task every 3 runs.
 			if(cpuTask.runTime%30 == 0){ 
 
 				System.out.println("----- Reducing priority of: " + p.name + "\n");
 				p.priority--; 
 				}
 		}
 		
 		// Add the aging process for queue 2 here.
 		
 		return p;
 	}
 	
 	// Test the RR method on the lowest priority queue only.
 	public static void testRR(){
 		
 		// Low priority.
 		queue_2 LPQ = new queue_2();
 		
 		// Add everything to the queue.
 		for(int i = 0; i < test1.length; i++){
 			
 			// Add element to start of queue.
 			LPQ.add(test1[i]);
 		}
 
 		// Keep working until the queue is empty. (This won't be the condition in the final version).
 		while(!LPQ.isEmpty()){
 			
 			System.out.println("Time: " + time);
 			System.out.println("LPQ size: " + LPQ.size());
 			
 			// Remove the head of the queue.
 			cpuTask = LPQ.remove();
 			
 			processedTask = cpuExecute(cpuTask, quantum, false);
 			
 			// Check if the current task has completed in the RR queue.
 			if(processedTask.runTime < processedTask.burstTime){
 				
 				LPQ.add(processedTask);
 			}
 			else{
 				
 				System.out.println("\n ***** Task: " + cpuTask.name + " done @ t = " + time + "\n");
 			}
 		}
 	}
 	
 	public static void printTask(Proc t){
 		
 		// Print name, priority and burst time.
 		System.out.println("Moved " + t.name + " from queue to CPU.");
 		System.out.println("Priority: " + t.priority);
 		System.out.println("Executed for : " + t.runTime + "/" +t.burstTime + " cycles.");
 		System.out.println();
 	}
 
 	public static void printResults(ArrayList<Proc> r){
 		
 		// Stats for individual proceses.
 		int age;
 		int start;
 		int finish;
 		int run;
 		int waiting;
 		int ready;
 		
 		// Stats for the whole simulation.
 		int totalProcesses = r.size();
 		int maxTurnAroundTime = 0;
 		int avgTurnAroundTime = 0;
 		int maxWaitingTime = 0;
 		int averageWaitingTime = 0;
 		
 		// Print stats about each process.
 		System.out.println("-----------------------------------------------------");
 		System.out.printf("%4s %7s %7s %7s %7s %7s %7s\n","Name","Start","End","Age", "Ready", "Run", "Wait");
 		System.out.println("-----------------------------------------------------");
 		
 		for (int i = 0; i < r.size(); i++) {
 			
 			age = r.get(i).age;
 			run = r.get(i).runTime;
 			start = r.get(i).creationTime;
 			finish = start + age;
			waiting = age - run;
 			ready = r.get(i).readyTime;
 
 			// Update max turnaround time.
 			if(finish - start > maxTurnAroundTime) { maxTurnAroundTime = finish - start; }
 			
 			// Update max wait time.
 			if(ready + waiting > maxWaitingTime) { maxWaitingTime =  ready + waiting; }
 			
 			// Update average turnaround time,
 			avgTurnAroundTime += (finish - start);
 			
 			// Update average waiting time.
 			averageWaitingTime += (ready + waiting);
 
 			// Print process statistics.
 			System.out.printf("%4s %7d %7d %7d %7d %7d %7d\n", r.get(i).name, start, finish, age, ready, run, waiting);
 		}
 		
 		System.out.println("-----------------------------------------------------");
 			
 			// Finalise statistics.
 			avgTurnAroundTime /= totalProcesses;
 			averageWaitingTime /= totalProcesses;
 			
 			// Print stats about the whole simulation.
 			System.out.printf("%25s : %5d \n","Total Processes", totalProcesses);
 			System.out.printf("%25s : %5d \n","Max Turn Around Time", maxTurnAroundTime);
 			System.out.printf("%25s : %5d \n","Avg Turn Around Time", avgTurnAroundTime);
 			System.out.printf("%25s : %5d \n","Max Waiting Time", maxWaitingTime);
 			System.out.printf("%25s : %5d \n","Average Waiting Time", averageWaitingTime);
 	}
 }
 
 // ready = time at which the process first gets the processor. finish - age.
 
 
 
 
 
