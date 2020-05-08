 package game;
 
 import game.Console.StdFd;
 import game.commands.CloseCommand;
 import game.commands.ClsCommand;
 import game.commands.Command;
 import game.commands.ConnectCommand;
 import game.commands.HelpCommand;
 import game.commands.PeersCommand;
 import game.networking.GameMessage;
 import game.networking.UDPMessage;
 import game.networking.UdpChannelFactory;
 
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import vsFramework.Channel;
 import vsFramework.Message;
 import console.GameConsole;
 
 public class Game implements InputHandler {
 	protected Console con;
 	protected Map<Channel,String> connectedPeers = new HashMap<Channel,String>();
 	private List<Channel> pendingPeers = new LinkedList<Channel>();
 	private Channel listenChannel;
 	
 	final Map<String, Command> commands = new HashMap<String, Command>();
 	
 	final protected String name;
 	final private int LOCALPORT = 4711;
 
 	private int silly = 0;
 	
 	public Game() {
 		createGUI();
 		initCommands();
 		name = con.waitForName();
 		con.setVisible(true);
 		con.println("Welcome! You are located at planet \"" + name + "\"");
 		con.println(StdFd.Planets, "Planetlist: \n\n>> No planets in reach.");
 	}
 
 	private void createGUI() {
 		con = new GameConsole();
 		con.setInputHandler(this);
 	}
 	
 	private void initCommands(){
 		commands.put("peers", new PeersCommand(this));
 		commands.put("connect", new ConnectCommand(this));
 		commands.put("cls", new ClsCommand(this));
 		commands.put("help", new HelpCommand(this));
 		commands.put("close", new CloseCommand(this));
 	}
 
 	@Override
 	public void onInput(String input) {
 		
 		String[] commandParts = input.split(" ");
 		Command command = commands.get(commandParts[0]);
 		if (command != null) {
 			try {
 			command.execute(commandParts);
 			} catch (IllegalArgumentException e) {
//				if (!e.getMessage().equals("")) {
//					con.println(e.getMessage());
//				}
 				con.println(command.usage());
 			}
 			
 		} else {
 			con.println("Unknown Command");
 			con.println(" >>\"help\"");
 //			con.println(this.listCommands());
 		}
 	}
 	
 	
 	protected String listCommands() {
 		StringBuilder sb = new StringBuilder("-------- Commands ---------\n");
 		for (String command : this.commands.keySet()) {
 			sb.append(command);
 			sb.append("\n");
 		}
 		sb.append("---------------------------");
 		return sb.toString();
 	}
 	
 // ----------------- Handle new Connections ------------------------------
 	
 //Ich mach ersteinmal alles per Hand, aber Commandpattern wr auch an dieser Stelle gut	
 	
 protected class Communicaton extends Thread{
 	Message actMessage;
 	
 	public Communicaton(){
 		listenChannel = UdpChannelFactory.newUdpChannel(LOCALPORT);
 		this.start();
 	}
 	
 	public void run(){
 		while(true){
 			try {
 				UdpChannelFactory.waitOnPort(LOCALPORT);
 			} catch (InterruptedException e) {
 				continue;
 			}
 			if((actMessage = listenChannel.nrecv()) != null){
 				String[] m = actMessage.getData().toString().split(" ");
 				if(m.length != 2) continue;
 				if(m[0].equals(GameMessage.HELLO.toString())){
 					connectedPeers.put(listenChannel,m[1]);
 					listenChannel.send(new UDPMessage((GameMessage.OLLEH.toString()+" "+name).getBytes()));
 					listenChannel = UdpChannelFactory.newUdpChannel(LOCALPORT);
 				}
 				//Wenn nicht verwerfen
 				for(Channel c : connectedPeers.keySet()){
 					if((actMessage = c.nrecv()) != null){
 						onMessage(c,actMessage);
 					}
 				}
 			}
 		}
 	}
 	
 }
 
 // ----------------- Incomming Message Handl -----------------------------
 
 public void onMessage(Channel c, Message m){
 	String[] inc = m.getData().toString().split(" ");
 	if(inc.length == 0) return;
 	
 	if(inc[1].equals(GameMessage.HELLO)){
 		
 	}else if(inc[1].equals(GameMessage.OLLEH)){
 		
 	}else if(inc[1].equals(GameMessage.PEERS)){
 		
 	}else if(inc[1].equals(GameMessage.SREEP)){
 		
 	}
 }
 
 // ----------------- Jump Points for Incomming Message ------------------
 
 public void onHello(Channel c, String[] inc){
 	//Unntzer Versuch, denn wir mssten ja schon connected sein, aber in Ordnung
 	
 	connectedPeers.put(c, inc[2]);
 	c.send(new UDPMessage((GameMessage.OLLEH.toString()+" "+name).getBytes()));
 }
 
 public void onOlleh(Channel c, String[] inc){
 	connectedPeers.put(c, inc[2]);
 }
 
 public void onPeers(Channel c, String[] inc){
 	// TODO
 }
 
 public void onSreep(Channel c, String[] inc){
 	// TODO
 }
 
 // ----------------- Jump Points for Command Classes ----------------------
 	
 	public void peers() {
 		switch(this.silly){
 			case 0 :
 				con.println("No planets found.");
 				++silly;
 				break;
 			case 1 :
 				con.clear(StdFd.Planets);
 				con.println(StdFd.Planets, "No planets found here either.");
 				++silly;
 				break;
 			case 2 :
 				con.println("STOP IT.");
 				++silly;
 				break;
 			case 3 :
 				con.println("If yout don't ...");
 				++silly;
 				break;
 			case 4 :
 				con.clear();
 				con.clear(StdFd.Planets);
 				con.println("ok, Ok, OK\nHere are your damn planets.\nHave fun with them.");
 				con.println(StdFd.Planets, "Planetlist: \n\n");
 				con.println(StdFd.Planets, "Alpha Centauri");
 				con.println(StdFd.Planets, "Beta Geuze");
 				con.println(StdFd.Planets, "Mars");
 				con.println(StdFd.Planets, "Snickers");
 				con.println(StdFd.Planets, "Kinderriegel");
 				++silly;
 				break;
 			default :
 				con.println("I don't talk to you anymore.\n...\nGo away!");
 				break;
 		}
 	}
 
 	public void connect(InetAddress host, int port) {
 		Channel chan = UdpChannelFactory.newUdpChannel(LOCALPORT, host, port);
 		pendingPeers.add(chan);
 		chan.send(GameMessage.HELLO);
 		
 		this.con.println("connect ausgeführt");
 	}
 	
 	public void close(){
 		this.con.println("Bye");
 		System.exit(0);
 	}
 	
 	public void help(){
 		StringBuilder sb = new StringBuilder("\n\n-------- Commands ---------\n");
 		for (String command : this.commands.keySet()) {
 			sb.append(command);
 			sb.append(" - ");
 			sb.append(this.commands.get(command).description());
 			sb.append("\n");
 			sb.append("   ->");
 			sb.append(this.commands.get(command).usage());
 			sb.append("\n");
 		}
 		sb.append("---------------------------");
 		this.con.println(sb.toString());
 	}
 	
 	public void clearScreen(){
 		this.con.clear();
 	}
 }
