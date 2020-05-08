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
 
 	List<Screen> screens;
 	Map<Character, Screen> menus;
 	int screenIndex;
 	Screen activeMenu;
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
 		size(screenW, screenH);
 
 		setupScreens();
 		JSON json = JSON.load(dataPath("proto.json"));
 		connect(json.getString("ip"));
 		
 		minim = new Minim(this);
 		tone = minim.loadSample("hai.aif");
 		
 		//TODO this needs to wait until the connection is set up.
 		sendScreenUpdate();
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
 	 * On exit, disconnect the socket from the glasses.
 	 */
 	public void exit() {
 		disconnect();
 	}
 
 	/**
 	 * Setup the prototype screens and menus.  This stuff is all
 	 * parsed from a json file describing the flow of screens 
 	 * and how input affects each one.  These special interactions 
 	 * give us flexibility in how we can interact with the prototype.
 	 */
 	private void setupScreens() {
 		screens = new ArrayList<Screen>();
 		menus = new HashMap<Character, Screen>();
 
 		JSON json = JSON.load(dataPath("proto.json"));
 
 		//load the menus
 		JSON jsonMenus = json.getArray("menus");
 		for (int i = 0; i < jsonMenus.length(); i++) {
 			JSON curMenu = jsonMenus.getJSON(i);
 			//setup a map describing how special keys affect an individual screen.
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
 	}
 
 	/**
 	 * Handle key presses.  Navigate via arrow keys, check for any 
 	 * global menus, and check to see if the active screen has any
 	 * special interactions to follow.
 	 */
 	public void keyPressed() {
 		boolean updated = false;
 
 		if (key == CODED) {
 			if (keyCode == LEFT) {
 				if (screenIndex > 0) screenIndex--;
 				activeMenu = null;
 				updated = true;
 			} else if (keyCode == RIGHT) {
 				if (screenIndex < screens.size()-1) screenIndex++;
 				activeMenu = null;
 				updated = true;
 			} else if (keyCode == UP) {
 				updated = activeMenu == null ? screens.get(screenIndex).scrollUp() : activeMenu.scrollUp();
 			} else if (keyCode == DOWN) {
 				updated = activeMenu == null ? screens.get(screenIndex).scrollDown() : activeMenu.scrollDown();
 			}
 		}
 
 		//Check if the active screen has a special menu
 		if (screens.get(screenIndex).hasMenu(key)) {
 			if (activeMenu != screens.get(screenIndex).getMenu(key)) {
 				activeMenu = screens.get(screenIndex).getMenu(key);
 			} else {
 				activeMenu = null;
 			}
 			updated = true;
 		//Then check if there are global menus to show
 		} else if (menus.containsKey(key)) {
 			if (activeMenu != menus.get(key)) {
 				activeMenu = menus.get(key);
 			} else {
 				activeMenu = null;
 			}
 			updated = true;
 		//Any other key actions
 		} else {
 			if (key == 'h') {
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
 	 * CONNECTION PORTION
 	 * TODO: Put this in another file
 	 */
 
 
 
 	// socket
 	private Socket socket;
 	private String ip;
	private final int port = 5556;
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
 		System.out.println("Connecting to " + ip_address);
 		connected = false;
 		try {
 			if (socket!= null) socket.close();
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
 		try {
 			if (socket!= null) socket.close();
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
 			try {               
 				socket.connect(socketAddress, 1000);
 				streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                 recRunnable = new receiveSocket();
                 recThread = new Thread(recRunnable);
                 recThread.start();
 			} catch (IOException e) {
 				disconnect();
 				e.printStackTrace();
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
 			}
 			catch(IOException ioe) {  
 				//Log.v(TAG, "Error getting input stream: " + ioe);
 			}
 			// when connected, this thread will stay in this while loop
 			while (connected) {  
 				
 			}
 		}
 	}
 }
