 package linewars.init;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import linewars.display.Display;
 import linewars.gameLogic.TimingManager;
 import linewars.network.Client;
 import linewars.parser.Parser.InvalidConfigFileException;
 
//TODO test
//TODO document
 public class Game {
 	private Display display;
 	private Client networking;
 	private TimingManager logic;
 	
 	private String mapDefinitionURI;
 	private int numPlayers;
 	private ArrayList<String> raceDefinitionURIs;
 	private String serverAddress;
 	
 	public static void main(String[] args){
 		ArrayList<String> raceURIs = new ArrayList<String>();
 		for(int i = 0; i < Integer.parseInt(args[2]); i++){
 			raceURIs.add(args[3 + i]);
 		}
 		Game toStart = new Game(args[0], Integer.parseInt(args[1]), args[2], raceURIs);
 		
 		toStart.initialize();
 		
 		toStart.run();
 	}
 	
 	public Game(String map, int players, String server, ArrayList<String> races){
 		mapDefinitionURI = map;
 		numPlayers = players;
 		raceDefinitionURIs = races;
 		serverAddress = server;
 	}
 	
 	public void initialize(){
 		try {
 			logic = new TimingManager(mapDefinitionURI, numPlayers, raceDefinitionURIs);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidConfigFileException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		networking = new Client(serverAddress);
		display = new Display(logic, networking);
 		//TODO
 	}
 	
 	public void run(){
 		Thread net = new Thread(networking);
 		net.setDaemon(true);
 		net.start();
 		Thread log = new Thread(logic);
 		log.setDaemon(true);
 		log.start();
 		Thread disp = new Thread(display);
 		disp.start();
 	}
 }
