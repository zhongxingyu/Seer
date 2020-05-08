 package edu.cmu.hcii.novo;
 
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.json.JSON;
 
 import ddf.minim.AudioSample;
 import ddf.minim.Minim;
 
 import processing.core.PApplet;
 
 /**
  * The main class for running a mid-fidelity prototype.  This class keeps track 
  * of the active screen and menu for the glasses.  It then sends json objects
  * to the glasses which trigger changes.
  * 
  * @author Chris
  * 
  */
 @SuppressWarnings("serial")
 public class OZ extends PApplet {
 
 	public static int screenW = 640;
 	public static int screenH = 480;
 	private static final int numRetries = 3;
 
 	List<Screen> screens;
 	Map<Character, Screen> menus;
 	int screenIndex;
 	Screen activeMenu;
 	
 	Map<Character, String> quickNav;
 	Character hideKey;
 	boolean hide;
 	
 	Minim minim;
 	AudioSample tone;
 
 	/**
 	 * Run the application.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		PApplet.main(new String[] { "edu.cmu.hcii.novo.OZ" });
 	}
 
 	/**
 	 * Setup the mid-fidelity prototype.
 	 * Grab the screens and connect to the glasses.
 	 */
 	public void setup() {
 		prepareExitHandler();
 		
 		size(screenW, screenH);
 		
 		setupPrototype();
 		JSON json = JSON.load(dataPath("proto.json"));
 		connect(json.getString("ip"));
 		
 		minim = new Minim(this);
 		tone = minim.loadSample("hai.aif");
 		
 		//TODO this needs to wait until the connection is set up.
 		//sendScreenUpdate();
 	}
 
 	/**
 	 * Draw a representation of the current state on the screen so 
 	 * testers can have a view of what the user is seeing.
 	 */
 	public void draw() {
 		rect(0, 0, width, height);
 		if (!hide) {
 			screens.get(screenIndex).draw();
 			if (activeMenu != null) activeMenu.draw();
 		}
 	}
 
 	/**
 	 * Setup the prototype screens and menus.  This stuff is all
 	 * parsed from a json file describing the flow of screens 
 	 * and how input affects each one.  These special interactions 
 	 * give us flexibility in how we can interact with the prototype.
 	 */
 	private void setupPrototype() {
 		screens = new ArrayList<Screen>();
 		menus = new HashMap<Character, Screen>();
 		quickNav = new HashMap<Character, String>();
 
 		try {	
 			JSON json = JSON.load(dataPath("proto.json"));
 			
 			//get the hide key
 			hideKey = json.getString("hide").charAt(0);
 				
 			//load the quick navigation
 			JSON jumps = json.getArray("quickNav");
 				
 			for (int j = 0; j < jumps.length(); j++) {
 				JSON curNav = jumps.getJSON(j);
 				quickNav.put(curNav.getString("key").charAt(0), curNav.getString("path"));
 			}
 	
 			//load the menus
 			JSON jsonMenus = json.getArray("menus");
 			
 			for (int i = 0; i < jsonMenus.length(); i++) {
 				JSON curMenu = jsonMenus.getJSON(i);
 				menus.put(curMenu.getString("key").charAt(0), new Screen(curMenu, this));
 			}
 			
 	
 			//load the screens
 			JSON jsonScreens = json.getArray("screens");
 			
 			for (int i = 0; i < jsonScreens.length(); i++) {
 				screens.add(new Screen(jsonScreens.getJSON(i), this)); 
 			}
 		
 			//set the starting image and active menu
 			screenIndex = 0;
 			activeMenu = null;
 			hide = false;
 			
 		} catch (RuntimeException e) {
 			System.out.println("Error when parsing json file");
 			e.printStackTrace();
 			exit();
 		}
 	}
 
 	/**
 	 * Handle key presses.  Navigate via arrow keys, check for any 
 	 * global menus, and check to see if the active screen has any
 	 * special interactions to follow.
 	 */
 	public void keyPressed() {
 		boolean updated = false;
 		
 		Screen activeScreen = screens.get(screenIndex);
 
 		//Check for arrow key movement
 		if (key == CODED) {
 			if (keyCode == LEFT) {
 				if (screenIndex > 0) {
 					activeScreen.leaveScreen();
 					screenIndex--;
 				}
 				activeMenu = null;
 				updated = true;
 				
 			} else if (keyCode == RIGHT) {
 				if (screenIndex < screens.size()-1) {
 					activeScreen.leaveScreen();
 					screenIndex++;
 				}
 				activeMenu = null;
 				updated = true;
 				
 			} else if (keyCode == UP) {
 				updated = activeMenu == null ? activeScreen.scrollUp() : activeMenu.scrollUp();
 				
 			} else if (keyCode == DOWN) {
 				updated = activeMenu == null ? activeScreen.scrollDown() : activeMenu.scrollDown();
 				
 			}
 		}
 
 		//handle any quick navigation
 		if (quickNav.containsKey(key)) {
 			int newIndex =  getScreenIndex(quickNav.get(key));
 			if (newIndex >= 0) {
 				screenIndex = newIndex;
 				if (activeMenu != null) {
 					activeMenu.leaveScreen();
 					activeMenu = null;
 				}
 				updated = true;
 			}
 			
 		//Check for changes from an active menu
 		} else if (activeMenu != null) {
 			//if it has a special sub screen
 			if (activeMenu.handleKeyPressed(key)) {
 				updated = true;
 				
 			//if its the same command that brought it up, dismiss
 			} else if (activeMenu == menus.get(key)) {
 				activeMenu.leaveScreen();
 				activeMenu = null;
 				updated = true;
 			
 			//check for global menu items
 			} else if (menus.containsKey(key)) {
 				activeMenu = menus.get(key);
 				updated = true;
 			}
 			
 		//Check if the active screen has a special sub screen
 		} else if (activeScreen.handleKeyPressed(key)) {
 			updated = true;
 			
 		//Check for global menus
 		} else if (menus.containsKey(key)) {
 			activeMenu = menus.get(key);
 			updated = true;
 			
 		//Any other key actions
		} else {
 			if (key == hideKey) {
 				hide = !hide;
 				updated = true;
 			}
 		}
 
 		//If updated, play a sound and send an update to the glasses.
 		if (updated) {
 			ohHai();
 			sendScreenUpdate();
 		}
 	}
 
 	/**
 	 * Notify the user that an input has been accepted.
 	 * Just play a beeping sound.
 	 */
 	private void ohHai() {
 		tone.trigger();
 	}
 	
 	/**
 	 * On exit, disconnect the socket from the glasses.
 	 */
 	public void stop() {
 		System.out.println("Stopping application.");
 		tone.close();
 		minim.stop();
 		disconnect();
 	}
 	
 	/**
 	 * Get the index of the screen with the given path.
 	 * 
 	 * @param path
 	 * @return
 	 */
 	private int getScreenIndex(String path) {
 		if (path != null) {
 			for (int i = 0; i < screens.size(); i++) {
 				if (path.equals(screens.get(i).getOriginalPath())) return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * Send an update to the glasses.  The message is a json objects specifying
 	 * the background and foreground images to show.  It just uses the filename of each.
 	 * If no image is to be displayed, send an empty string.
 	 */
 	private void sendScreenUpdate() {
 		String bg = (screens.get(screenIndex) != null && !hide) ? screens.get(screenIndex).getPath() : "";
 		String fg = (activeMenu != null && !hide) ? activeMenu.getPath() : "";
 
 		String msg = "{\"background\": \"" + bg + "\", \"foreground\": \"" + fg + "\"} \n";
 		sendMsg(msg);
 	}
 	
 	/**
 	 * Setup an exit handler.  This is what handles the shutdown of the application and 
 	 * cleans up all sockets/audio channels/etc.
 	 */
 	private void prepareExitHandler() {
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 			public void run () {
 				//System.out.println("SHUTDOWN HOOK");
 				try {
 					stop();
 				} catch (Exception e){
 					e.printStackTrace(); // not much else to do at this point
 				}
 							
 			}
 		}));
 	}
 
 
 
 	/**
 	 * CONNECTION PORTION
 	 * TODO: Put this in another file
 	 */
 
 
 
 	// socket
 	private Socket socket;
 	private String ip;
 	private final int port = 5555;
 	private boolean connected = false; 		// is connected or not
 
 	// connect/send thread
 	private Runnable connectRunnable	= null;
 	private Thread sendThread           = null;
 	private DataOutputStream streamOut  = null;
 	
 	// receive thread
 	private Runnable recRunnable		= null;
 	private Thread recThread            = null;
 	private DataInputStream streamIn	= null;
 
 	/**
 	 * Connect to the given ip address via a java socket.
 	 * 
 	 * TODO: not sure if the streamIn is needed here.
 	 * 
 	 * @param ip_address
 	 */
 	public void connect(String ip_address){
 		System.out.println("Connecting socket to " + ip_address);
 		connected = false;
 		try {
 			if (socket != null) socket.close();
 			if (streamOut != null) streamOut.close();
 			if (streamIn != null) streamIn.close();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		ip = ip_address;
 		socket = new Socket();
 		connectRunnable = new connectSocket();
 		sendThread = new Thread(connectRunnable);
 		sendThread.start();
 	}
 
 	/**
 	 * Sends bytes to connected glasses.
 	 * 
 	 * @param msg
 	 */
 	public void sendMsg(String msg){
 		if (connected) { // if system is connected
 			try {
 				System.out.println("Sending message: " + msg);
 				streamOut.writeBytes(msg);
 				streamOut.flush();
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 		} else{
 			// if system is not connected
 			System.out.println("Not connected.");
 		}
 	} 
 
 	/**
 	 *  Forces the socket to disconnect
 	 */
 	public void disconnect(){
 		System.out.println("Disconnecting socket");
 		try {
 			if (socket != null) socket.close();
 			if (streamOut != null) streamOut.close();
 			if (streamIn != null) streamIn.close();
 			connected = false;
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}  
 	
 	/**
 	 * This sets up connection/send socket
 	 * 
 	 * @author Chris
 	 *
 	 */
 	class connectSocket implements Runnable {
 		@Override
 		public void run() {
 			SocketAddress socketAddress = new InetSocketAddress(ip, port);
 			
 			//Try to connect 3 times.
 			int tries = 0;
 			while(tries < numRetries) {				
 				try {
 					System.out.println("Attempting connection " + (tries+1));
 					socket = new Socket();
 					socket.connect(socketAddress, 3000);
 					streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
 	                recRunnable = new receiveSocket();
 	                recThread = new Thread(recRunnable);
 	                recThread.start();
 	                break;
 	                
 				} catch (IOException e) {
 					e.printStackTrace();
 					disconnect();
 				}
 				
 				tries++;
 				
 				//Wait a bit before connecting again.
 				try {
 					Thread.sleep(1000);
 					
 				} catch (Exception e) {
 					System.out.println("Error trying to sleep");
 				}
 			}
 			
 			//If it wasn't able to connect.
 			if (tries == numRetries && !socket.isConnected()) {
 				System.out.println("Failed to connect to client.");
 				exit();
 				
 			} else if (socket.isConnected()) {
 				System.out.println("Connected to client");
 			}
 		}
 	}
 
 	/**
 	 * Sets up the receive socket
 	 */
 	class receiveSocket implements Runnable {
 		@Override
 		public synchronized void run() {
 			try {  
 				//Log.v(TAG, "receive socket ");
 				streamIn  = new DataInputStream(socket.getInputStream()); // sets up input stream
 				connected = true; // sets connection flag to true
 			
 			} catch(IOException ioe) {  
 				//Log.v(TAG, "Error getting input stream: " + ioe);
 			}
 			
 			// when connected, this thread will stay in this while loop
 			while (connected) {  
 				
 			}
 		}
 	}
 }
