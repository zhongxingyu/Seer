 package ucbang.network;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import java.util.Stack;
 
 import javax.swing.JOptionPane;
 
 import ucbang.core.Bang;
 import ucbang.core.Player;
 
 public class Server extends Thread {
 	protected HashMap<String, LinkedList<String>> messages = new HashMap<String, LinkedList<String>>();
 	static int numPlayers;
 	ServerSocket me;
 	public int gameInProgress; // 1 = attempting to start game, 2 = game started
 	// for realz lawl
 	public int prompting; // flag for whether people are still being prompting
 	// for something 0 = no, 1 = prompting with no
 	// unchecked updates, 2 = unchecked prompt
 	public ArrayList<int[][]> choice; // int[m][n], where m is player and n is option
         public int[][] ready;
         
 	Bang game; // just insert game stuff here
 	public ArrayList<String> names = new ArrayList<String>();
	ServerListAdder adder = new ServerListAdder();
 	long listLastUpdated;
 	void print(Object stuff) {
 		System.out.println("Server:" + stuff);
 	}
 
 	public Server(int port) {
            //name this server
            adder.name=JOptionPane.showInputDialog("Input server name");
 		try {
 			me = new ServerSocket(port);
 		} catch (IOException e) {
 			System.err.println("Server Socket Error!\n" + e);
 			e.printStackTrace();
 		}
 		print("Game server is listening to port " + port);
 		
 		this.start();
 	}
 
 	public static void main(String Args[]) {
 		new Server(12345);
 	}
 
 	public void run() {
 		while (true) {
 			if(System.currentTimeMillis()-listLastUpdated>60000){
 				listLastUpdated=System.currentTimeMillis();
 				adder.addToServerList();
 			}	
 			if (gameInProgress == 0) {
 				try {
 					Socket client = me.accept();
 					if (client != null) {
 						new ServerThread(client, this);
 						numPlayers++;
 					}
 				} catch (Exception e) {/* e.printStackTrace(); */
 				}
 			} else {
 				// System.out.println("Has it been updated? "+prompting);
 				if (prompting == 2) {
 					boolean flag = true;
 					//System.out.println(choice.length + " " + choice[0].length);
 					for (int n = 0; n < choice.get(choice.size()-1).length; n++) {
 						if (choice.get(choice.size()-1)[n][1] == -2) {
 							flag = false;
 						}
 					}
 					System.out.println("Are we ready to move on? "+flag);
 					if (flag) {
 						prompting = 0;
 						// received all choices, send this to bang.java or w/e
 						if (gameInProgress == 1) {
                                                         choice.remove(choice.size()-1);
 							gameInProgress++;
 
 							// check order of names
 							/*
 							 * for(String s: names){ System.out.println(s); }
 							 */
 							 ready = new int[numPlayers][2];
                                                          
 							// create player objects
 							for (int n = 0; n < names.size(); n++) {
 								sendInfo(n, "SetInfo:newPlayer:"+n+":"+numPlayers);
 							}
                                                         for(int n = 0; n<ready.length; n++){
                                                              ready[n][0] = n;
                                                              ready[n][1] = 0;
                                                         }
 							game = new Bang(numPlayers, this);// FLAG: game
                                                         game.process();
 						}
                                                 else if(gameInProgress==2){ //game has started
                                                         game.process(); //less bleh
                                                         gameInProgress++;
                                                 }
                                                 else if(gameInProgress==3){
                                                         game.process();
                                                 }
 					} else {
 						// still prompting
 						prompting = 1;
 					}
 				}
 			}
 		}
 	}
 
         public void sendInfo(String info){
             for(int n=0; n<numPlayers; n++){
                 sendInfo(n, info);
             }
         }
 	public void sendInfo(int player, String info) { //info can be sent to multiple people at the same time, unlike prompts                
                 if(ready!=null){
                     while(ready[player][1]>0){} //wait
                     ready[player][1]++;
                 }
                 else{
                     for(int n = 0; n<ready.length; n++){
                          ready[n][0] = n;
                          if(n != player){
                             ready[n][1] = 0;
                          }
                          else{
                              ready[n][1] = 1;
                          }
                     }
                 }
 		messages.get(names.get(player)).add(info);
 	}
 
 	void addChat(String string) {
 		Iterator<String> keyter = messages.keySet().iterator();
 		while (keyter.hasNext()) {
 			messages.get(keyter.next()).add("Chat:" + string);
 		}
 	}
 
 	void playerJoin(String player) {
 		names.add(player);
 		Iterator<String> keyter = messages.keySet().iterator();
 		while (keyter.hasNext()) {
 			messages.get(keyter.next()).add("PlayerJoin:" + player);
 		}
 	}
 
 	void playerLeave(String player) {
 		names.remove(player);
 		messages.remove(player);
 		Iterator<String> keyter = messages.keySet().iterator();
 		while (keyter.hasNext()) {
 			messages.get(keyter.next()).add("PlayerLeave:" + player);
 		}
 	}
 
 	void startGame(int host, String name) {
 		gameInProgress = 1;
 		try {
 			me.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		prompting = 1;
 		choice = new ArrayList<int[][]>();
                 choice.add(new int[numPlayers - 1][2]);
 		for (int n = 0, m = 0; m < numPlayers - 1; n++, m++) {// this prompt goes out to everyone except host
 			if (n != host) {
 				choice.get(choice.size()-1)[m][0] = n;
 				choice.get(choice.size()-1)[m][1] = -2;
 			} else
 				m--;
 		}
 		Iterator<String> keyter = messages.keySet().iterator();
 		while (keyter.hasNext()) {
 			String s = keyter.next();
 			if (s != name)
 				messages.get(s).add("Prompt:Start");
 		}
 	}
         public void promptAll(String s){
             System.out.println("waiting for all players");
             prompting = 1;
             choice.add(new int[numPlayers][2]);
             for (int n = 0; n < numPlayers; n++) {         
                     choice.get(choice.size()-1)[n][0] = n;
                     choice.get(choice.size()-1)[n][1] = -2;
             }
             for(int n=0; n<numPlayers; n++){
                 prompt(n, s, false);
             }
         }
 
         public void prompt(int player, String s, boolean one) {
             if(one){    
                 System.out.println("Waiting for one player");
                 choice.add(new int[][]{{player, -2}});
             }
             if(prompting == 0){
                 prompting = 1;
             }
             System.out.println(messages.get(names.get(player))==null);
             messages.get(names.get(player)).add("Prompt:"+s);
         }
 }
 
 class ServerThread extends Thread {
 	// sends HashMap of stuff to clients, gets client's updated positions
 	Socket client;
 	BufferedReader in;
 	BufferedWriter out;
 	Server server;
 	String name = "";
 	int id;
 	String buffer;
 	boolean connected = false;
 	LinkedList<String> newMsgs = new LinkedList<String>();
 
 	void print(Object stuff) {
 		System.out.println("Server:" + stuff);
 	}
 
 	public ServerThread(Socket theClient, Server myServer) {
 		client = theClient;
 		this.server = myServer;
 		id = server.numPlayers;
 		System.out.println("This is client id " + id);
 		try {
 			in = new BufferedReader(new InputStreamReader(client
 					.getInputStream()));
 			out = new BufferedWriter(new OutputStreamWriter(client
 					.getOutputStream()));
 		} catch (Exception e1) {
 
 			e1.printStackTrace();
 			try {
 				client.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		this.start();
 	}
 
 	public synchronized void run() {
 		while (!client.isClosed()) {
 			try {
 				if (in.ready()) {
 					buffer = (String) in.readLine();
 					//System.out.println("Server received from player ID "+id+" "+ buffer);
 					String[] temp = buffer.split(":", 2);
 					if (temp[0].equals("Name")) {
 						if (!connected) {// player was never connected
 							if (server.messages.containsKey(temp[1])) {
 								out.write("Connection:Name taken!");
 								out.newLine();
 								out.flush();
 								print(client.getInetAddress()
 										+ " Attempting joining with taken name.");
 							} else {
 								name = temp[1];
 								print(name + "(" + client.getInetAddress()
 										+ ") has joined the game.");
 								server.playerJoin(name);
 								server.messages.put(name, newMsgs);
 								out.write("Connection:Successfully connected.");
 								out.newLine();
 								out.flush();
 								Iterator<String> players = server.messages
 										.keySet().iterator();
 								out.write("Players:");
 								while (players.hasNext()) {// give player list
 									// of current
 									// players
 									out.write(players.next() + ",");
 								}
 								out.newLine();
 								out.flush();
 							}
 						}
 					} else if (temp[0].equals("Chat")) {
 						if (temp[1].charAt(0) == '/') {
 							// TODO: Send commands
 							if (temp[1].equals("/start")
 									&& client.getInetAddress().toString()
 											.equals("/127.0.0.1")
 									&& server.gameInProgress == 0)
 								server.startGame(id, name);
 							else if (temp[1].startsWith("/rename")) {
                                                             if(server.gameInProgress==2){
                                                                 System.out.println("I'm sorry, Dave.");
                                                             }
                                                             else if (temp[1].length() > 7
 										&& temp[1].charAt(7) == ' ') {
 									String temp1 = temp[1].split(" ", 2)[1];
 									if (server.messages.containsKey(temp1)) {
 										out.write("Connection:Name taken!");
 										out.newLine();
 										out.flush();
 										print(name
 												+ "("
 												+ client.getInetAddress()
 												+ ") Attempting renaming to taken name.");
 									} else {
 										print(name + "("
 												+ client.getInetAddress()
 												+ ") is now known as " + temp1);
 										server.messages.remove(name);
 										server.messages.put(temp1, newMsgs);
 										server.playerLeave(name);
 										server.playerJoin(temp1);
 										name = temp1;
 										out
 												.write("Connection:Successfully renamed.");
 										out.newLine();
 										out.flush();
 									}
 								} 
                                                                 else {
 									// TODO: (Optional) create /help RENAME
 								}
 							}
                                                         else if (temp[1].startsWith("/prompting")) {
                                                             System.out.println("Prompting is "+server.prompting+" "+server.gameInProgress);
                                                         }
 						} else
 							server.addChat(name + ": " + temp[1]);
 					} else if (temp[0].equals("Prompt")) {
 						if (server.prompting >= 1) {
 							int n;
                                                         //if(id>server.choice.length) 
 							for (n = 0; server.choice.get(server.choice.size()-1)[n][0] != id; n++) {
 							    System.out.println("Looking for id: " + id+ " not "+server.choice.get(server.choice.size()-1)[n][0]);
 							}
 							server.choice.get(server.choice.size()-1)[n][1] = Integer.valueOf(temp[1]);
 							server.prompting = 2;
 						} else {
 							System.out.println("Received prompt from player when not prompted!");
 						}
                                         }  else if (temp[0].equals("Ready")) {
                                             if(server.ready!=null){server.ready[id][1]--;}
                                             else {System.out.println("ERROR: Ready for what?");}    
                                         }
                                         else {
 						System.out.println("Error: Junk String received:"
 								+ temp[0] + " " + temp[1]);
 					}
 				}
 				if (!newMsgs.isEmpty()) {
 					Iterator<String> iter = ((LinkedList<String>) newMsgs
 							.clone()).iterator();
 					while (iter.hasNext()) {
 						out.write(iter.next());
 						out.newLine();
 						iter.remove();
 					}
 					newMsgs.clear(); // will this still produce CME?
 				}
 				out.flush();
                             sleep(10); //is this needed?
 			} catch (Exception e) {
 				if (e != null && e.getMessage() != null
 						&& e.getMessage().equals("Connection reset"))
 					try {
 						finalize();
 					} catch (Throwable t) {
                                             t.printStackTrace();
 					}
 				else
 					e.printStackTrace();
 			}
 		}
 	}
 
 	protected void finalize() throws Throwable {
 		print(name + "(" + client.getInetAddress() + ") has left the game.");
 		server.playerLeave(name);
 		try {
 			in.close();
 			out.close();
 			client.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
