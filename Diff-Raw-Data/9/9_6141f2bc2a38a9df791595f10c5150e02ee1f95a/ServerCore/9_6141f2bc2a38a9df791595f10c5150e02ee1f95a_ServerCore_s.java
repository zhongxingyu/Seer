 package aidancbrady.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 public final class ServerCore
 {
 	public static final int PORT = 3074;
 	public static boolean serverRunning = true;
 	public static boolean programRunning = true;
 	public static ServerSocket serverSocket;
 	public static Scanner scanner = new Scanner(System.in);
 	public static int usersConnected = 0;
 	public static Map<Integer, ServerConnection> connections = new HashMap<Integer, ServerConnection>();
 	
 	//Going to try and use port 3073
 	public static void main(String[] args)
 	{
 		try {
 			Runtime.getRuntime().addShutdownHook(new ShutdownHook());
 			
 			new SocketListener().start();
 			
 			System.out.println("Server initialized, listening on port " + PORT);
 			
 			while(programRunning)
 			{
 				if(scanner.hasNextLine())
 				{
 					String command = scanner.nextLine().trim().toLowerCase();
 					
 					if(command.equals("stop"))
 					{
 						serverRunning = false;
 						System.out.println("Stop command received.");
 					}
 					else if(command.equals("quit"))
 					{
 						programRunning = false;
 						System.out.println("Quit command received.");
 						break;
 					}
 					else if(command.startsWith("user"))
 					{
 						String[] commandArgs = command.split(" ");
 						if(commandArgs.length > 1)
 						{
 							if(commandArgs[1].equals("info"))
 							{
 								if(commandArgs.length == 3)
 								{
 									try {
 										int userID = Integer.parseInt(commandArgs[2]);
 										if(connections.get(userID) != null)
 										{
 											System.out.println("Information on user " + userID + ":");
 											System.out.println("Username: " + (connections.get(userID).hasUsername() ? connections.get(userID).username : "unknown"));
 											if(!connections.get(userID).messages.isEmpty())
 											{
 												System.out.println("Logged messages:");
 												for(String message : connections.get(userID).messages)
 												{
 													System.out.println(message);
 												}
 											}
 											else {
 												System.out.println("No messages found for this user.");
 											}
 										}	
 										else {
 											System.err.println("Unable to find database for user '" + userID + ".'");
 										}
 									} catch(NumberFormatException e) {
 										System.err.println("Invalid characters.");
 									}
 								}
 								else {
 									System.out.println("Usage: 'user info <ID>'");
 								}
 							}
 							else if(commandArgs[1].equals("kick"))
 							{
 								if(commandArgs.length == 3)
 								{
 									try {
 										int userID = Integer.parseInt(commandArgs[2]);
 										if(connections.get(userID) != null)
 										{
 											connections.get(userID).connection.kick();
 											System.out.println("Kicked user '" + userID + ".'");
 										}	
 										else {
 											System.err.println("Unable to find database for user '" + userID + ".'");
 										}
 									} catch(NumberFormatException e) {
 										System.err.println("Invalid characters.");
 									}
 								}
 								else {
 									System.out.println("Usage: 'user kick <ID>'");
 								}
 							}
 							else if(commandArgs[1].equals("list"))
 							{
 								if(commandArgs.length == 2)
 								{
 									System.out.println("Currently connected users:");
 									for(ServerConnection connection : connections.values())
 									{
 										StringBuilder string = new StringBuilder();
 										string.append("User " + connection.userID + " " + (connection.hasUsername() ? "(" + connection.username + ")" : "(no username found)"));
 										System.out.println(string);
 									}
 								}
 								else {
 									System.out.println("Usage: 'user list'");
 								}
 							}
 						}
 						else {
 							System.out.println("-- User Control Panel --");
 							System.out.println("Command help:");
 							System.out.println("'user info <ID>' - displays a user's information.");
 							System.out.println("'user kick <ID>' - kicks a user from the server.");
 							System.out.println("'user list' - lists all currently connected users.");
 						}
 					}
 					else if(command.equals("help"))
 					{
 						System.out.println("-- Server Command Center --");
 						System.out.println("Command help:");
 						System.out.println("'stop' - stops the server if it is running.");
 						System.out.println("'quit' - stops the server if it is running and terminates the program.");
 						System.out.println("'user <params>' - reads information from the user.");
 					}
 					else {
 						System.out.println("Unknown command.");
 					}
 				}
 			}
 			System.out.println("Shutting down...");
 			
 			if(serverSocket != null)
 			{
 				serverSocket.close();
 			}
 			
 			System.out.println("Goodbye!");
 		} catch (Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	public static int newConnection()
 	{
 		usersConnected++;
 		return usersConnected;
 	}
 	
 	public static void removeConnection(int userId)
 	{
 		usersConnected--;
 		connections.remove(userId);
 	}
 }
 
 class ShutdownHook extends Thread
 {
 	public void run()
 	{
 		try {
 			if(ServerCore.serverSocket != null)
 			{
 				ServerCore.serverSocket.close();
 			}
 		} catch (IOException e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace();
 		}
 	}
 }
