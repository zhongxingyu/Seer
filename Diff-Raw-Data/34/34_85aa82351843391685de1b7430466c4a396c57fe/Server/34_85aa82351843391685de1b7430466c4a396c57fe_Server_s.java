 package com.jkuchta.chat;
 import java.net.*;
 import java.io.*;
 
 public class Server{
	private int port = 6969, maxConnections = 10;
 	
 	public void start(){
 		System.out.println(String.format("Starting server on port %s", port));
 		
 		try{
 			ServerSocket ss = new ServerSocket(port);
 		
 			int i = 0;
 			while (i++ < maxConnections){
 				Socket client = new Socket();
 				client = ss.accept();
 			}
 			ss.close();
 		}
 		catch(IOException ioe){
 			System.out.println(String.format("Could not start server: %s", ioe));
 			System.exit(-1);
 		}
 		
 	}
 	
 }
