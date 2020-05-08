 package Model;
 
 import java.util.ArrayList;
 
 import sshaclient.Constants;
 import sshaclient.SocketClient;
 
 import Control.PlayerControl;
 import Model.Arenas.Arena;
 import Model.Arenas.MapHazardCross;
 import Model.Arenas.MapSlaughterField;
 import Model.Classes.ClassHunter;
 import Model.Classes.ClassWarrior;
 import Model.Classes.ClassWizard;
 
 public class MainHub {
 
 	public static final int nbrOfPlayers = 4;
 	public static final int noActionLimit = 400;
 	
 	private boolean isMultiplayer = false;
 	private static MainHub myControl = null;
 	private Player[] players = new Player[nbrOfPlayers];
 	private PlayerControl[] playerControllers = new PlayerControl[nbrOfPlayers];
 	private Thread[] controllerThreads = new Thread[nbrOfPlayers];
 	private SocketClient socketClient;
 	private Thread socketThread;
 	private int activePlayer = 0;
 	private int mapSelected;
 	private int difficultySelected;
 	private Arena[] maps;
	private String playerName = "eeee";
 	//Singleton
 	public static MainHub getController() {
 	if (myControl == null) {
 	   myControl = new MainHub();
 	   // prepare myController here or use setter() methods or a parameterized constructor
 	   }
 	   return myControl;
 	}
 
 	// make constructor private so no one except the getController() can call it
 	private MainHub() {
 		//players[0] = new ClassWizard(getActivePlayerName(), "player", 120, 100, 0);
 	//	players[0] = new Player("name", "player", "No Class", 400, 300, 1600, 0.7, 0.35, 0);
 		maps = new Arena[2];
 		maps[0] = new MapHazardCross();
 		maps[1] = new MapSlaughterField();
 		
 		socketClient = new SocketClient(Constants.hostName, Constants.defaultPort);
 		socketThread = new Thread(socketClient);
 		socketThread.start();
 	}
 	
 	public synchronized void addPlayer(Player player, int index){
 		System.out.println("Added player: " + player.getName() + " and he is a " + player.getType() + " with ID: " + index);
 		players[index] = player;
 	}
 	public synchronized void removePlayer(int index) {
 		players[index] = null;
 	}
 	public synchronized void resetPlayers(){
 		players = new Player[nbrOfPlayers];
 	}
 	public synchronized Player[] getPlayers(){
 		return players;
 	}
 	public synchronized Player getPlayer(int index) {
 		return players[index];
 	}
 	public synchronized void addPlayerController(PlayerControl pc, int index) {
 		playerControllers[index] = pc;
 	}
 	public synchronized void removePlayerController(int index) {
 		playerControllers[index] = null;
 	}
 	public synchronized void resetPlayerControllers() {
 		playerControllers = new PlayerControl[nbrOfPlayers];
 	}
 	public synchronized PlayerControl[] getPlayerControllers() {
 		return playerControllers;
 	}
 	public synchronized PlayerControl getPlayerControl(int index) {
 		return playerControllers[index];
 	}
 	public synchronized void addControllerThread(Thread thread, int index) {
 		controllerThreads[index] = thread;
 	}
 	public synchronized void removeControllerThread(int index) {
 		controllerThreads[index] = null;
 	}
 	public synchronized void resetControllerThreads() {
 		controllerThreads = new Thread[nbrOfPlayers];
 	}
 	public synchronized Thread[] getControllerThreads() {
 		return controllerThreads;
 	}
 	public synchronized Thread getControllerThread( int index) {
 		return controllerThreads[index];
 	}
 	
 	public String getActivePlayerName(){
 		return playerName;
 	}
 	public void setActivePlayerName(String name){
 		playerName = name;
 	}
 	public int getActivePlayerIndex(){
 		return activePlayer;
 	}
 	public void setActivePlayerIndex(int index){
 		activePlayer = index;
 	}
 	public boolean isMulti(){
 		return isMultiplayer;
 	}
 	public void setSingleOrMulti(boolean singleOrMulti){
 		isMultiplayer = singleOrMulti;
 	}
 	public SocketClient getSocketClient() {
 		return socketClient;
 	}
 	public Arena getMapSelected(){
 		return maps[mapSelected];
 	}
 	public int getMapIndex(){
 		return mapSelected;
 	}
 	public void setMapIndex(int mapIndex){
 		mapSelected = mapIndex;
 	}
 	public int getDiffcultySelected(){
 		return difficultySelected;
 	}
 	public void setDifficultySelected(int dif){
 		difficultySelected= dif;
 	}
 }
