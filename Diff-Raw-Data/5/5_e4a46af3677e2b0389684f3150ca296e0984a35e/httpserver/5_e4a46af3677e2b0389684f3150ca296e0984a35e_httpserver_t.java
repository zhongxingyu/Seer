 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Vector;
 
 public class Server{
 	public static void main(String args[]) throws Exception{
 		int port = 8080;
 		
 		/* Reads in the Args from the project call
 		*  Will read in all args, does not matter the order as
 		*  long as each call is preceded by the proper - identifier
 		* Will set the log file, doc root, and port properly
 		* if no port, port is preset to 8080.
 		*/ 
 		String docroot = "";
 		String logfile = "";
 		int x = 0;
 		while(args[x] != NULL){
 			switch(args[x]){
 				case"-p"{
 					x++;
 					port = Integer.parseInt(args[x]);
 					break;
 				}case"-docroot"{
 					x++;
 					docroot = args[x];
 					break;
 				}case"-logfile"
 					x++;
 					logfile = args[x];
 					break;
 				}default{
 					System.out.println("Error reading args");
 				}
 				x++;
 			}
 		}
 		ServerSocket listenSocket = new ServerSocket(port);
 		System.out.println("HTTP server started on port " + port + ".");
 		while(true){			
 			Socket s = listenSocket.accept();
 			Clienthandler c = new Clienthandler (s, docroot, logfile);
 			Thread t = new Thread (c);
 			t.start();
 		}
 	}
 }
 
 class Clienthandler implements Runnable{
 	
 	Socket connectionSocket;
 	String docroot;
 	String logfile;
 	
 	/* 
 	* Passed Args into thread, for data on logfile and docroot
 	* Create printwriter to logfile, method will print one string
 	* Then close the file with input of "close log"
 	*/
 	
 	Clienthandler(Socket s, String pdocroot, String plogfile){
 		connectionSocket = s;
 		docroot = pdocroot;
 		logfile = plogfile;		
 	}
 	PrintWriter out = new PrintWriter(new FileWriter(logfile))); 
 	
	public void writeToLog(String s){		
 		out.println(s); 
		if(s.equals("Close Log")){
 			out.close();
 		}
 	}
 	
 	void run(){
 		try{
 	
 		} catch (SocketTimeoutException e) {
 			sendPacket(.....);
 		}
 	}
 	
 
 	returntype sendPacket(int statusCode,  , something content){
 		outToClient.
 	}
 
 }
