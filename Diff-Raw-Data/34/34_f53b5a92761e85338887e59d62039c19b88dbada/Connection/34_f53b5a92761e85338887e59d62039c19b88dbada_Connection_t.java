 package com.example.tabletapp;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 
 class Connection {
 
	private final static String HOST = "145.37.56.30";
 	private final static int PORT = 9923;
 	
 	private Socket sock;	
 	private PrintWriter out;
 	private BufferedReader in;
 		
 	public Connection() {		
 		openConnection();
 	}
 	
 	public void openConnection() {
 		try {			
 			sock = new Socket(InetAddress.getByName(HOST), PORT);				
 			out = new PrintWriter(sock.getOutputStream(), true);			
 			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));			
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	public void println(String message) {		
 		try {	
 			out.println(message);			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String readLine() {		
 		try {
 			return in.readLine();
 		} catch (Exception e) { e.printStackTrace(); }	
 		return null;
 	}	
 }
 
