 package Utilities;
 import java.util.*;
 import java.util.regex.Pattern;
 
 
 public class InputReader implements Runnable {
 	Scanner sc;
 	Pattern p;
 	String buffer;
 	String ready;
 	boolean done;
 	public InputReader(){
 		sc=new Scanner(System.in);
 		done=false;
 		buffer="";
 		ready="";
 	}
 	
 	public String getSubmitted(){
 		String store=ready;
 		ready="";
 		return store;
 	}
 	
 	public void stop(){
 		done=true;
 	}
 	
 	public void run() {
 		while(!done){
			if(sc.hasNextLine()){
				ready=sc.nextLine();
 			}
 			else{
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {}
 			}
 		}
 	}
 }
