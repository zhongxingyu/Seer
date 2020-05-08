 package edu.se.se441.threads;
 
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 
 import edu.se.se441.Office;
 
 public class Clock extends Thread{
 	
 	private CountDownLatch startSignal;
 	private long startTime;	// When the simulation starts.
 	private Vector<Long> timeRegistry;
 	private Office office;
 	private int millisTime;
 	private long currentMillisTime;
 	
 	public Clock(CountDownLatch startSignal){
 		this.startSignal = startSignal;
 		timeRegistry = new Vector<Long>();
 	}
 	
 	public void run(){
 		try {
 			// Starting all threads at the same time (clock == 0 / "8:00AM").
 			startSignal.await();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		startTime = System.currentTimeMillis();
 		Random r = new Random();
 		// Set the start time of the simulation.
 		System.out.println("CLOCK STARTED");
 		int numOfQuestions = 0;
 		while(this.getTime() <= 5400){ //Simulation starts at 800 (time 0000) and ends at 1700 (time 5400).
			currentMillisTime = System.currentTimeMillis();
 			synchronized(timeRegistry){
 				Iterator<Long> iter = timeRegistry.iterator();
 				while(iter.hasNext()){
 					Long t = iter.next();
 					if(this.getTime() >= t && office != null){
 						iter.remove();
 						office.notifyWorking();
 					}
 				}
 			}
 			if(currentMillisTime + 1 >= System.currentTimeMillis()){
 				numOfQuestions++;
 				int random = r.nextInt(100000);
 				if(random == 0 && office != null){	// Firing random questions.
 					numOfQuestions++;
 					office.notifyWorking();
 				}	
 			}
 		}
 		System.out.println(numOfQuestions);
 		System.out.println("CLOCK ENDED");
 	}
 	
 	/**
 	 * @return The time of day in ms (0ms is 8:00AM)
 	 */
 	public long getTime(){
 		return System.currentTimeMillis() - startTime;
 	}
 
 	public void addTimeEvent(long timeOfEvent){
 		synchronized(timeRegistry){
 			timeRegistry.add(timeOfEvent);
 		}
 	}
 	public void setOffice(Office o){
 		office = o;
 	}
 }
