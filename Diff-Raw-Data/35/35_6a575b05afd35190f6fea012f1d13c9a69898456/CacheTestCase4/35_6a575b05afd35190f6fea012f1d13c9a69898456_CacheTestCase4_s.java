 package CacheSystemTestCases;
 
 import CacheSystem.CacheSystem;
import CacheSystem.util.*;
 import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 
 class CacheTestCaseThread2 extends Thread {
 	
 	CacheSystem cs;
 	File f;
 	int threadID = 0;
 	byte [] junk = null;
 	int totalThreads = 0;
 	int totalKeysForThisThread = 0;
 	CacheTestCase4 pw;
 	
 	CacheTestCaseThread2 (CacheSystem cs, int threadID, int totalThreads, int totalKeysForThisThread, CacheTestCase4 controller)
 	{
 		this.cs =  cs;
 		this.threadID = threadID;
 		this.totalThreads = totalThreads;
 		this.totalKeysForThisThread = totalKeysForThisThread;
 		this.pw = controller;
 		
 		f = new File   ("C:\\Users\\aswini\\Desktop\\desktop\\apk\\BlockPuzzle2-22.apk");
 		junk = new byte[1024];
 		
 		this.start();
 	}
 		// Insert 25 numbers into the Cache
 	public void run () {
 		long time_marker_ = System.currentTimeMillis();
 		
 		for (int i=60; i < totalKeysForThisThread; i++) {
 			try {
 			int TTL = i + 60; 	//	Making sure no key lives for less than 60ms.
 			int randomThread = new java.util.Random ().nextInt (totalThreads);
 			time_marker_ = System.currentTimeMillis();
 			cs.add(randomThread+"key"+i, new String (junk), TTL);
 			time_marker_ = System.currentTimeMillis() - time_marker_;
 			pw.println (threadID+", add, "+time_marker_);
 			}
 			catch (Exception e) {
 				e.printStackTrace ();
 			}
 		}
 		// Sleep 3 minutes to let a few of them expire
 		pw.flush ();
 		try {
 			System.out.println ("Sleeping at "+(new java.util.GregorianCalendar ()).getTime());
 			Thread.sleep (360000);
 		}
 		catch (Exception e) {}
 
 		// Read 15 of them in random
 		
 		for (int j = 0; j < totalKeysForThisThread; j++) {
 			int randomKey 		= new java.util.Random ().nextInt (totalKeysForThisThread);
 			int randomThreadID  = new java.util.Random ().nextInt (totalThreads);
 			
 			try {
 			time_marker_ = System.currentTimeMillis();
 			System.out.println ("Gotten a random element ("+randomThreadID+"key"+randomKey+", "+cs.get (randomThreadID+"key"+randomKey)+")");
 			time_marker_ = System.currentTimeMillis() - time_marker_;
 			pw.println (threadID+", get, "+time_marker_);
 			}
 			catch (Exception e) {
 				e.printStackTrace ();
 			}
 		}
 		pw.flush ();
 	}
 
 }
  
 
 public class CacheTestCase4 {

 	CacheSystem cs;
	int totalThreads = 2000;
 	int totalKeysForEachThread = 500;
 	File csv;
 	PrintWriter pw;
 	private int printRequestCounter = 0;
 	StringBuffer logMessages = new StringBuffer ();
		
 	public CacheTestCase4 ()
 	{
 		cs =  new CacheSystem (100000, 75000);
 		CacheTestCaseThread2 [] ctct = new CacheTestCaseThread2 [totalThreads];
 		csv = new File ("C:\\Users\\aswini\\Desktop\\Perf.csv");
 		for (int threadID=0; threadID < totalThreads; threadID++) {
 			ctct [threadID] = new CacheTestCaseThread2(cs, threadID, totalThreads, totalKeysForEachThread, this);
 		}
 		
 		try {
 			pw = new PrintWriter (new FileWriter (csv));
 		}
 		catch (Exception e) {
 			e.printStackTrace ();
 		}
 //		pw.close ();
 	}
 	
 	public synchronized void println (String message) {
 		printRequestCounter++;
 		logMessages.append(message+"\n");
 //		pw.println (message);
		if (printRequestCounter%1000 == 0) { 
 			pw.println (logMessages);
 			this.flush();
			logMessages.delete(1, logMessages.length());
 		}
 	}
 	
 	public synchronized void flush () {
 		System.out.println ();
 		System.out.println ("printRequestCounter is: "+printRequestCounter+". Flushing now.");
 		System.out.println ();
 		pw.flush ();
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
		CacheTestCase4 ctc = new CacheTestCase4();
//		ctc.pw.close();
 	}
 
 }
