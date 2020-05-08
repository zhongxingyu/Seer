 package org.processmanagement.scheduling;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 import org.processmanagement.processes.Process;
 import org.processmanagement.processes.ProcessComplex;
 
 public class ShortestJobFirstCom extends SchedulerCom{
 
 	/**
 	 * Sorting class to sort queue by burst time
 	 */
 	private ArrayList<ProcessComplex> sortQueue(ArrayList<ProcessComplex> list){
 		
 		//java sort method with a comparator class to compare next burst times
 		Collections.sort(list, new Comparator<ProcessComplex>() {
 			public int compare(ProcessComplex process1, ProcessComplex process2) {
 				 return process1.getCurBurst() - process2.getCurBurst();
 			}
 		});
 		return list;
 	}
 	
 	/**
 	 * Method that starts the shortest job first scheduler simulation.
 	 */			
 	public void start(){
 		for(ProcessComplex p:pList){
 			for(Integer b:p.getBurst()){
 			totalBurst += p.getCurBurst();
 			
 			}
 			readyQueue.add(p);
 		}
 		
 		printProcesses();
 		size = pList.size();
 		readyQueue = sortQueue(readyQueue);//sorts the processList
 		System.out.println("Running Processes...");
 		//step through the queue and simulate the burst time for each process	
 		sjf();
 		printStats();
 	}
 
 	public void sjf() {
 		int elapsedBurst = 0;
 		boolean idle = false;
 		ProcessComplex curProcess = new ProcessComplex();
 		
 		while(!readyQueue.isEmpty()){
 			sortQueue(readyQueue);
 			
 			//get the first process in the queue that has arrived
 			int i = 0;
 			boolean found = false;
 			while(i<readyQueue.size()&&!found){
 				if(readyQueue.get(i).getArrivalTime()<=elapsedBurst){
 					curProcess = readyQueue.get(i);
 					found = true;
 				}
 				i++;
 			}
 			//end find first process in queue that has arrived
 
 			if(found){//there is a process to run
 				String n = interruptedBy(curProcess, elapsedBurst);
 				if(n.equals(curProcess.getName())){//the process will be able to run in it's entirety
 					if(curProcess.getBurst().size()==1){ //job will finish
 		
 						System.out.print(elapsedBurst + "---[" + curProcess.getName() + "]---");//print nat section
 						//calc the current wait time for the current burst section of the process
 						int curWait = curProcess.getWaitTime() + (elapsedBurst - curProcess.getArrivalTime());
 						curProcess.setWaitTime(curWait);//update total wait time for process
 						
 						elapsedBurst += curProcess.getCurBurst();
 					
 						curProcess.setCompletionTime(elapsedBurst - curProcess.getArrivalTime());
 						curProcess.getBurst().remove(0);
 						readyQueue.remove(curProcess);//remove the process
 						
 						//update totals
 						totalWait += curProcess.getWaitTime();
 						totalComp += curProcess.getCompletionTime();
 						
 					}
 					else{//job will not finish within the current burst segment
 						System.out.print(elapsedBurst + "---[" + curProcess.getName() + "]---");//print nat
 						
 						int curWait = curProcess.getWaitTime() + (elapsedBurst - curProcess.getArrivalTime());//current segment wait
 						curProcess.setWaitTime(curWait); //set total process wait
 					
 						elapsedBurst += curProcess.getCurBurst(); 
 						//stores the burst time just processed before removing it
 						int lastBurst = curProcess.getCurBurst();
 						curProcess.getBurst().remove(0);
 						
 						sendToIO(curProcess, elapsedBurst, lastBurst);//sent the process to the IOQueue
 					}
 				}
 				else{//process will be interrupted
 					int stopTime = interruptedAt(n);
 					System.out.print(elapsedBurst + "---[" + curProcess.getName() + "]---");//print nat
 					curProcess.decCurBurst(stopTime - elapsedBurst);
 					elapsedBurst = stopTime;
 					
 				}
 			}
 			else{
 				System.out.print(elapsedBurst + "---[IDLE]---");
 				elapsedBurst = findNextArrivalTime();
 			}
 			
 		}
 		System.out.print(elapsedBurst);
 	}
 	//this method simulates the process being put through IO
 	public void sendToIO(ProcessComplex curProcess, int elapsedBurst, int lastBurst){
 		int delay = 0;
 		if(elapsedBurst<freeAt){
 			delay = freeAt - elapsedBurst;
 		}
 		curProcess.setArrivalTime(elapsedBurst+curProcess.getCurIoTime()+delay);
		freeAt = curProcess.getArrivalTime();
 		curProcess.getIoTime().remove(0);
 	}
 	public int findNextArrivalTime(){
 		int nextTime = readyQueue.get(0).getArrivalTime();
 		for(ProcessComplex p:readyQueue){
 			if(p.getArrivalTime()<nextTime)
 				nextTime = p.getArrivalTime();
 		}
 		return nextTime;
 	}
 	public String interruptedBy(ProcessComplex curProcess, int elapsedBurst){
 		String result = curProcess.getName();
 		for(ProcessComplex p:readyQueue){
 			if(p.getArrivalTime()<(elapsedBurst + curProcess.getCurBurst())&&p.getCurBurst()<curProcess.getCurBurst()){
 				result = p.getName();
 			}
 		}
 		return result;
 		
 	}
 	public int interruptedAt(String name){
 		for(ProcessComplex p:readyQueue){
 			if(p.getName().equals(name)){
 				return p.getArrivalTime();
 			}
 		}
 		return 0;
 	}
 	
 }
