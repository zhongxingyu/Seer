 package edu.unh.cs.tact_progs;
 
 import edu.unh.cs.tact.*;
 
 public class test2{
 	@GuardedBy("this") public int number;
 	public BasicThread[] threadArray;
 	public final int NUMTHREADS = 4;
 
 	test2(){
 		threadArray = new BasicThread[NUMTHREADS];
 
 		for(int i = 0; i < NUMTHREADS; i++){
 			BasicThread bt = new BasicThread();
 			threadArray[i] = bt;
 		}
 	}
 
 
 
 	public void method(){
 		for(BasicThread bt : threadArray){
 			Checker.releaseAndStart(bt);
 		}
 
 		for(BasicThread bt : threadArray){
 			try{
 				bt.join();
 			}
 			catch(Exception e){
 				System.out.println("EXCEPTION: " + e);
 			}
 		}
 	}
 
 	public void print(){
 		synchronized(this){
 			System.out.println(number);
 		}
 	}
 
 	private class BasicThread extends Thread{
 		BasicThread(){}
 
 		public void run(){
 			for(int i = 0; i < 10000; i++){
				number ++;
 			}
 		}
 	}
 
 	public static void main(String args[]){
 		Checker.init();
 		test2 t2 = new test2();
 		t2.method();
 		t2.print();		
 	}
 }
