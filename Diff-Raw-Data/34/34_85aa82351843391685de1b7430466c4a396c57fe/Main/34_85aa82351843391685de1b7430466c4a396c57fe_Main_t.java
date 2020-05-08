 package com.jkuchta.chat;
 
 public class Main {
    public static void main(String[] args) {
		if(args.length == 1){
			try{
				int port = Integer.parseInt(args[0]);
				Server server = new Server(port);
				server.start();
			}
			catch(Exception ex){
				System.out.println("Error, please enter a valid port. (Ex. 1234)");
				defaultStart();
			}
		}
		else{
			defaultStart();
		}
	}
 	
	public static void defaultStart(){
		System.out.println("No port given, starting the server on the default port");
		Server server = new Server(3456);
		server.start();
 	}
 }
