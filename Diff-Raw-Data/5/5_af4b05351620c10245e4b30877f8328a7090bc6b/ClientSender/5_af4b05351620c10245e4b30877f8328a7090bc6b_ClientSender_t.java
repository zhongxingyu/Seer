 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Scanner;
 
 
 public class ClientSender {
 	private static int connectionPort = 8989;
 	private HashMap<String,String> aliases;
 	private HashMap<String,String> poses;
 	
 	
 	Session s;
 	PrintWriter out;
 	
 	public ClientSender(PrintWriter out, Session session) {
 		s = session;
 		this.out = out;
 		//initialize the aliases map
 		aliases = new HashMap<String,String>();
 		//first all legal commands
 		//local commands
 		aliases.put("help", "help");
 		aliases.put("show", "show");
 		aliases.put("exit", "exit");
 		//remote commands
 		aliases.put("list", "list");
 		aliases.put("chat", "chat");
 		aliases.put("host", "host");
 		aliases.put("join", "join");
 		aliases.put("move", "move");
 		
 		//now some aliases
 		aliases.put("halp", "help");
 		aliases.put("h", "help");
 		aliases.put("s", "show");
 		aliases.put("q", "exit");
 		aliases.put("quit", "exit");
 		aliases.put("c", "chat");
 		aliases.put("j", "join");
 		aliases.put("m", "move");
 		
 		//initialize poses map
 		poses = new HashMap<String,String>();
 		poses.put("b8","1");
 		poses.put("d8","2");
 		poses.put("f8","3");
 		poses.put("h8","4");
 		poses.put("a7","5");
 		poses.put("c7","6");
 		poses.put("e7","7");
 		poses.put("g7","8");
 		poses.put("b6","9");
 		poses.put("d6","10");
 		poses.put("f6","11");
 		poses.put("h6","12");
 		poses.put("a5","13");
 		poses.put("c5","14");
 		poses.put("e5","15");
 		poses.put("g5","16");
 		poses.put("b4","17");
 		poses.put("d4","18");
 		poses.put("f4","19");
 		poses.put("h4","20");
 		poses.put("a3","21");
 		poses.put("c3","22");
 		poses.put("e3","23");
 		poses.put("g3","24");
 		poses.put("b2","25");
 		poses.put("d2","26");
 		poses.put("f2","27");
 		poses.put("h2","28");
 		poses.put("a1","29");
 		poses.put("c1","30");
 		poses.put("e1","31");
 		poses.put("g1","32");
 	}
 	
 	/*
 	 * Command methods
 	 */
 	
 	//print help menu or something
 	//does not change program state
 	private void help(String input) {
 		String[] comm = input.split(" ");
 		String helps = 
 				"Available commands:\n" +
 		" chat - chat to your opponent.\n" +
 		" exit - disconnect from game or exit program.\n" +
 		" help - brings up this menu.\n" +
 		" host - create a new game.\n" +
 		" join - join an existing game.\n" +
 		" list - get a list of active game sessions.\n" +
 		" move - make a move.\n" +
 		" show - show the current board.\n" +
 		"Type help <command> to get details about usage.\n";
 		
 		
 		if(comm.length == 2) {
 			String arg = comm[1];
 			if(arg.equals("chat")) {
 				helps = "chat usage: chat <message>.\n";
 			} else if(arg.equals("exit")) {
 				helps = "exit usage: exit.\n";
 			} else if(arg.equals("help")) {
 				helps = "help usage: just help or help <command>.\n";
 			} else if(arg.equals("host")) {
 				helps = "host usage: host <gamename>.\n";
 			} else if(arg.equals("join")) {
 				helps = "join usage: join <gamename>.\n";
 			} else if(arg.equals("list")) {
 				helps = "list usage: list.\n";
 			} else if(arg.equals("move")) {
 				helps = "move usage: move <from> <to>. Example: move d4 e5.\n";
 			} else if(arg.equals("show")) {
 				helps = "show usage: show.\n";
 			}
 		} 
 		s.print(helps);
 	}
 	
 	private void show() {
 		int state = s.getCurrentState();
 		if(state == Session.STATE_LOBBY) {
 			s.println("You are not in a game.");
 			return;
 		}
 		
 		if(state == Session.STATE_HOST_WAITING) {
 			s.println("Game has not begun yet.");
 			return;
 		}
 		
 		//if we got here we are in a game currently
 		String gamestate = s.getBoard();
 		//parse it
 		String[] tokens = gamestate.split(" ");
 		
 		String boards = tokens[0];
 		String res = s.parseBoard(boards);
 		
 		if(state == Session.STATE_HOST_GAME && tokens[2].equals("r")) {
 			res += "It is YOUR turn, you are red\n";
 		} else if(state == Session.STATE_HOST_GAME && tokens[2].equals("w")) {
 			res += "It is your opponent's turn, you are red\n";
 		} else if(state == Session.STATE_PEER_GAME && tokens[2].equals("r")) {
 			res += "It is your opponent's turn, you are white\n";
 		} else if(state == Session.STATE_PEER_GAME && tokens[2].equals("w")) {
 			res += "It is YOUR turn, you are white\n";
 		}
 		
 		
 		
 		s.print(res);
 	}
 	
 	private void quit() {
 		int state = s.getCurrentState();
 		if(state == Session.STATE_LOBBY) {
 			send("exit");
 			s.setCurrentState(Session.STATE_DISCONNECTED);
 		} else if(state == Session.STATE_DISCONNECTED) {
 			//do nothing
 		} else {
 			//in a game
 			s.setWaiting(true);
 			send("exit");
 			s.setWaitingFor(Session.WAIT_EXIT);
 			waitForResponse();
 		}
 	}
 	
 	private void list() {
 		int state = s.getCurrentState();
 		if(state == Session.STATE_LOBBY) {
 			s.setWaiting(true);
 			send("list");
 			s.setWaitingFor(Session.WAIT_LIST);
 			waitForResponse();
 		} else {
 			s.println("You are not in the lobby, this does nothing!");
 		}
 	}
 	
 	private void chat(String input) {
 		int state = s.getCurrentState();
 		if(state == Session.STATE_LOBBY) {
 			s.println("Cannot chat outside of a game!");
 		} else if(state != Session.STATE_DISCONNECTED) {
 			send(input); //we are not waiting for a response here.
 		}
 	}
 	
 	private void host(String input) {
 		int state = s.getCurrentState();
 		if(state == Session.STATE_LOBBY) {
 			s.setWaiting(true);
 			send(input);
 			s.setWaitingFor(Session.WAIT_HOST);
 			waitForResponse();
 		} else {
 			s.println("You are not in the lobby, cannot host.");
 		}
 	}
 	
 	private void join(String input) {
 		int state = s.getCurrentState();
 		if(state == Session.STATE_LOBBY) {
 			s.setWaiting(true);
 			send(input);
 			s.setWaitingFor(Session.WAIT_JOIN);
 			waitForResponse();
 		} else {
 			s.println("You are not in the lobby, cannot join.");
 		}
 	}
 	
 	private void move(String input) {
 		int state = s.getCurrentState();
 		if(state == Session.STATE_LOBBY) {
 			s.println("Cannot make moves outside of games!");
 		} else if(state != Session.STATE_DISCONNECTED){
 			String[] tokens = input.split(" ");
 			StringBuilder sb = new StringBuilder();
 			for(int i = 2; i < tokens.length;i++) {
 				sb.append(mapPos(tokens[i]) + " ");
 			}
 			s.setWaiting(true);
 			send("move " + sb.toString());
 			s.setWaitingFor(Session.WAIT_MOVE);
 			waitForResponse();
 		}
 	}
 	
 	/*
 	 * Wait until the receiver confirms we got a response from server
 	 */
 	private void waitForResponse() {
 		while(s.getWaiting()) {}
 	}
 	
 	public String mapPos(String pos) {
 		String ret = poses.get(pos);
 		if(ret == null)
 			ret = pos;
 		return ret;
 	}
 	
 	private void send(String msg) {
		out.println(msg);
 	}
 	
 	public void run() {
 		Scanner sc = new Scanner(System.in);
 		String input;
 		while(s.getCurrentState() != Session.STATE_DISCONNECTED) {
 			input = sc.nextLine();
 			
 			if(s.getCurrentState() == Session.STATE_DISCONNECTED) {
 				//random disconnect while waiting for user input
 				s.println("It appears that connection to server was lost, exiting program.");
 				break;
 			}
 			
 			input = input.trim().toLowerCase();
 			String[] tokens = input.split(" ");
 			String command = aliases.get(tokens[0]);
 			if(!command.equals("")) {
 				String parsedCommand = aliases.get(command);
 				if(parsedCommand == null) { //unknown command
 					s.println("Unrecognized command \"" + command + "\". Type \"help\" for help.");
 				} else {
 					//valid command
 					//check which command it is
 					if(parsedCommand.equals("help")) {
 						help(input);
 					} else if(parsedCommand.equals("show")) {
 						show();
 					} else if(parsedCommand.equals("exit")) {
 						quit();
 					} else if(parsedCommand.equals("list")) {
 						list();
 					} else if(parsedCommand.equals("chat")) {
 						chat(input);
 					} else if(parsedCommand.equals("host")) {
 						host(input);
 					} else if(parsedCommand.equals("join")) {
 						join(input);
 					} else if(parsedCommand.equals("move")) {
 						move(input);
 					}
 				}
 			}
 		}
 		
 		s.println("Goodbye");
 		System.exit(0);
 	}
 	
     public static void main(String[] args) throws IOException {
 		Socket chSocket = null;
         String adress;
         PrintWriter out = null;
         BufferedReader in = null;
         
         try {
             adress = args[0];
         } catch (ArrayIndexOutOfBoundsException e) {
         	System.err.println("Using localhost.");
         	adress = "127.0.0.1";
         }
         try {
             chSocket = new Socket(adress, connectionPort); 
             out = new PrintWriter(chSocket.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader
                                    (chSocket.getInputStream()));
         } catch (UnknownHostException e) {
             System.err.println("Unknown host: " +adress);
             System.exit(1);
         } catch (IOException e) {
             System.err.println("Couldn't open connection to " + adress);
             System.exit(1);
         }
         Scanner scanner = new Scanner(System.in);
         
         //TODO set name exchange here
         String name = "player";
         
         //start with the initial name establishment
         boolean gotname = false;
         //boolean firsttime = true;
         
         while(!gotname) {
         	String serverresponse = in.readLine(); //should be "entername" but we dont care
         	
         	
             //prompt user to enter name:
             System.out.print("Enter name: ");
             name = scanner.nextLine();
        	out.println(name);
         	
         	serverresponse = in.readLine().trim().toLowerCase();
         	String[] tokens = serverresponse.split(" ");
         	if(tokens[0].equals("reg") && tokens[1].equals("ok")) {
         		gotname = true;
         	} else {
         		
         		System.out.println("Error occured, message from server:\n");
         		StringBuilder sb = new StringBuilder();
         		for(int i = 2; i < tokens.length;i++) {
         			sb.append(tokens[i] + " ");
         		}
         		System.out.println(sb.toString());
         	}
         	
         }
         
         scanner.close();
 
         
         //now that the name has been set
         Session s = new Session(name);
         
         ClientReceiver cr = new ClientReceiver(in,s);
         ClientSender cs = new ClientSender(out,s);
         
         s.println("Welcome to the lobby! Type list to see a list of games!");
         cr.start(); //important to start this one first as it is a new thread!
         
         cs.run();//the current main thread will become the client receiver
         //we will get out of the run method once the user has typed exit.
         //cr.interrupt();
 	}
 	
 }
