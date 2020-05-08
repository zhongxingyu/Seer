 package edu.se.se441.threads;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 
 import edu.se.se441.Office;
 
 public class Clock extends Thread{
 	
 	private CountDownLatch startSignal;
 	private long startTime;	// When the simulation starts.
 	private ArrayList<Long> timeRegistry;
 	private Office office;
 	
	public Clock(CountDownLatch startSignal, Office office){
 		this.startSignal = startSignal;
 		timeRegistry = new ArrayList<Long>();
		this.office = office
 	}
 	
 	public void run(){
 		try {
 			// Starting all threads at the same time (clock == 0 / "8:00AM").
 			startSignal.await();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		Random r = new Random();
 		// Set the start time of the simulation.
 		startTime = System.currentTimeMillis();
 		while(this.getTime() <= 5400){ //Simulation starts at 800 (time 0000) and ends at 1700 (time 5400).
 			for(Long t : timeRegistry){
 				int random = r.nextInt(5);
 				if(this.getTime() >= t){
 					o.notifyWorking();
 				}
 				else{
 					if(random == 0){
 						o.notifyWorking();
 					}
 				}
 				
 			}
 		}
 	}
 	
 	/**
 	 * @return The time of day in ms (0ms is 8:00AM)
 	 */
 	public long getTime(){
 		return System.currentTimeMillis() - startTime;
 	}
 
 	public void addTimeEvent(long timeOfEvent){
 		timeRegistry.add(timeOfEvent);
 	}
 }
