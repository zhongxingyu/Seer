 package edu.berkeley.cs162;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class ThreadPool_Test {
 	
     private static int tasksCompleted = 0;  
 
     @Before
 	public void reset() {
 		tasksCompleted = 0;
 	}
 
 	private static class Job implements Runnable {
  	  	private int n = 0;  
 		public Job (int num) {
 	    	this.n = num;
 	    }
 	    
 	    public void run() {
 	    	tasksCompleted ++;
 	        System.out.println("Jobs Completed = " + n);
 	    }
 	}
 
 	private class failure extends TimerTask {
 		@Override
 		public void run() {
 			fail("Waited too long, timer up!");
 		}
 	}
 
     @Test
     public void testMoreThreadsThanJobs() {
 		ThreadPool threadPool = new ThreadPool(5);
 		ArrayList<Job> jobs = new ArrayList<Job>();
 		jobs.add(new Job(1));
 		jobs.add(new Job(2));
 		jobs.add(new Job(3));
 		
 		try {
 			for(int i=0; i<jobs.size(); i++){
 				threadPool.addToQueue(jobs.get(i));
 			}
 		} catch(InterruptedException ie) {
 			fail("MoreThreadsThanJobs failed!");
 		}
 
 		Timer timer = new Timer();
 		timer.schedule(new failure(), 1000);
 		while(tasksCompleted < 3){}
 		timer.cancel();
 		System.out.println();
     }
 	
 	@Test
 	public void testMoreJobsThanThreads() {
 		ThreadPool threadPool = new ThreadPool(5);
 		ArrayList<Job> jobs = new ArrayList<Job>();
 		jobs.add(new Job(1));
 		jobs.add(new Job(2));
 		jobs.add(new Job(3));
 		jobs.add(new Job(4));
 		jobs.add(new Job(5));
 		jobs.add(new Job(6));
 		jobs.add(new Job(7));
 		jobs.add(new Job(8));
 
 		try {
 			for(int i=0; i<jobs.size(); i++) {
 				threadPool.addToQueue(jobs.get(i));
 			}
 		} catch(InterruptedException ie) {
 			fail("MoreJobsThanThreads failed!");
 		}
 
 		Timer timer = new Timer();
 		timer.schedule(new failure(), 1000);
		while(tasksCompleted < 8){}
 		timer.cancel();
 		System.out.println();
 	}
 
 	@Test
 	public void testEqualJobsAndThreads() {
 		ThreadPool threadPool = new ThreadPool(5);
 		ArrayList<Job> jobs = new ArrayList<Job>();
 		jobs.add(new Job(1));
 		jobs.add(new Job(2));
 		jobs.add(new Job(3));
 		jobs.add(new Job(4));
 		jobs.add(new Job(5));
 
 		try {
 			for(int i=0; i<jobs.size(); i++){
 				threadPool.addToQueue(jobs.get(i));
 			}
 		} catch(InterruptedException ie) {
 			fail("EqualJobsAndThreads failed!");
 		}
 
 		Timer timer = new Timer();
 		timer.schedule(new failure(), 1000);
		while(tasksCompleted < 5){}
 		timer.cancel();
 		System.out.println();
 	}
 }
