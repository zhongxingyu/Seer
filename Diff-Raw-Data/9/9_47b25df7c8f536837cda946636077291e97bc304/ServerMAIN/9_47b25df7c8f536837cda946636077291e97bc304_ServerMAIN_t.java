 package server;
 
 import java.awt.Color;
 import java.io.*;
 import java.net.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 //import chatSample.Server.ClientThread;		// dont know why this was here...
 
 /**
  * The server thread reads new users who want to connect and assigns them to a secretary thread. It also contains methods which are used for creating servers and management
  * @author Boys
  *
  */
 public class ServerMAIN
 {
 	protected static int UsrNum = 0;
 	protected static List<Fileio> snct;		// the list of threads (which users are connected to)
 	private static List<GameRoom> gme;				// list of all the gamerooms which are active
 	private int port;							// the current server port number (in general, should be 3865)
 	private static SimpleDateFormat time;				// for the timing (on the server)
 	private static boolean serverState;				// when closing the server
 	//**********************************************
 	public static String resources = "CS320_server";	// file constants
 	public static String stats = "MyStats";
 	public static String ext = ".gme";
 	public static String token = "|";
 	//**********************************************
 	public static BufferedWriter bw;
 	public static String slash;
 	@SuppressWarnings("unused")
 	private static File logFile;
 	public static boolean GUI;
 	public static ServerGui myGUI;
 	public ServerSocket server;
 
 	public ServerMAIN(int port, ServerGui mygui, boolean isgui){
 		this.port = port;
 		myGUI = mygui;
 		GUI = isgui;
 		snct = new CopyOnWriteArrayList<>();
 		gme = new CopyOnWriteArrayList<>();
 		time = new SimpleDateFormat("HH:mm:ss");
 		boolean system = System.getProperty("os.name").startsWith("Windows");
 		if (system)
 			slash = "\\";												// this should take care of some file-searching problems (Windows... :P)
 		else
 			slash = "/";
 		// then, we set up the directory structure
 
 		int ix = findFile(resources);
 		if (ix == 0){								// setting up directory structure (since none found)
 			try{
 				new File(resources).mkdir();
 				System.out.println("##Created a new directory structure");
 			} catch (Exception e){
 				System.out.println("##Cant create home directory! Please restart application");
 				throw new IllegalStateException("##Cant create home directory! Please restart application");
 			}
 		}
 
 		// we then set up the log file
		ix = findFile(resources + slash + "log.sfg");
 		if (ix == 0){								// setting up log file (none found)
 			try{
 				if(!(new File(resources + slash + "log.sfg").createNewFile()))
 					throw new Exception();
 				System.out.println("##Created a new log file");
 			} catch (Exception e){
 				System.out.println("##Cant create log file! Please restart application");
 				throw new IllegalStateException("Error in initializing log file");
 			}
 		}
 
 		// then, we test the log file with a BufferedWriter
 		try{
 			bw = new BufferedWriter(new FileWriter(new File(resources + slash + "log.sfg"), true));
 		} catch (IOException e){
 			Print("##Cant read from log file, please restart the application");
 			throw new IllegalStateException("Error reading log directory");
 		}
 		Print("%%%%%%%%%%%%%%%%%%%%%");
 		Print("%DATE: " + new Date().toString() + "%");
 		Print("%%PORT:\t " + port + "  \t   :%%");
 
 		// that should be all (for now)
 	}
 
 	public void start(){
 		// will look for other users and do other menial stuff 
 		// (most of the user interaction is handled in the individual user threads)
 		serverState = true;
 
 		try{
 			server = new ServerSocket(port);
 			Print("Initialization Complete");
 			while (serverState){
 				// serverState will change to false when the server ends stuff (i guess.., but then we could just break loop if we wanted...)
 				Socket acceptConnect = server.accept();
 				Secretary newUser = null;
 				try{
 					newUser = new Secretary(acceptConnect);
 					if(!serverState)
 						break;
 				}catch (IllegalStateException els){
 					Print("Could not connect user - ending his stream", Color.red);
 					Print(els.getMessage(), Color.red);
 					try{
 						if (newUser.in != null)
 							newUser.in.close();
 					} catch (IOException ioe){
 						Print("---Could not close input stream", Color.red);
 					}
 					try{
 						if (newUser.out != null)
 							newUser.out.close();
 					} catch (IOException ioe){
 						Print("---Could not close output stream", Color.red);
 					}
 					try{
 						if (newUser.clientSocket != null)
 							newUser.clientSocket.close();
 					} catch (IOException ioe){
 						Print("---Could not close Socket", Color.red);
 					}
 				}
 				if(!serverState)
 					break;
 				addRemoveUser(newUser, true);													// save it in the ArrayList of connected players
 				newUser.start();														// and off the client goes (to work)
 
 			}
 
 			//
 			// Should probably put this stopping stuff into a new method. However...., the finally fits so perfect
 			//
 		} catch (IOException ioe){
 			Print("Error with Socket: Abort-> " + ioe.getMessage(), Color.red);
 		} finally {									// this is for when the code exits (by error or regularly)
 			for (GameRoom clnt : gme){			// (hopefully) a clean disconnect of gameroom
 				try{
 					clnt.close();					
 
 				} catch(NullPointerException ie){
 					try{
 						Print("Could not fully disconnect user " + clnt.id, Color.red);
 					} catch (Exception io){
 						Print("Could not fully disconnect unknown user", Color.red);
 					}
 				}
 			}
 			//gme.removeAll(gme);
 			for (Fileio clnt : snct){			// if not, disconnect users individually
 				try{
 					clnt.close();
 
 				} catch(NullPointerException ie){
 					try{
 						Print("Could not fully disconnect user " + clnt.id, Color.red);
 					} catch (Exception io){
 						Print("Could not fully disconnect unknown user", Color.red);
 					}
 				}
 			}
 
 			try {
 				bw.close();
 				server.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	public static void main(String argv[]) throws Exception
 	{
 		//first, do any pre-initialization: gui
 		// then, create server object and initialize
 		//afterwards, do post-initialization: 
 		int port = 3865;						// change this to a 
 		try{
 			ServerMAIN server = new ServerMAIN(port, null, false);
 			server.start();
 			System.out.println("##EXIT COMPLETE: HAVE A NICE DAY");
 		} catch (Exception e){
 			System.out.println("##EXIT INCOMPLETE: FATAL ERROR::\n" + e.getMessage());
 			bw.close();
 		}
 	}
 
 	/**
 	 * Adds or removes a user thread from the ServerMAIN ArrayList and, if there is a GUI running, 
 	 * adds the user thread to the GUI's DefaltListModel.
 	 * @param ur
 	 * @param type
 	 */
 	protected synchronized static void addRemoveUser(Fileio ur, boolean type) {
 		// remove/add the player thread from the player list
 		if (type){
 			snct.add(ur);
 			if (GUI)
 				myGUI.userloader.addElement(ur);
 		} else {
 			snct.remove(ur);
 			if (GUI)
 				myGUI.userloader.removeElement(ur);
 		}
 	}
 
 	/**
 	 * Returns the games which are in session (currently is used by secretary thread/object)
 	 * @return gme the list of gamerooms (do i need this?)
 	 */
 	protected synchronized static List<GameRoom> getGameRoom(){
 		return gme;
 	}																					// dont really need this cause have CopyOnWriteArrayList
 
 	/**
 	 * Adds or removes a game thread from the ServerMAIN ArrayList and, if there is a GUI running, 
 	 * adds the game thread to the GUI's DefaltListModel.
 	 * @param ur
 	 * @param type
 	 */	
 	protected synchronized static void addRemoveGame(GameRoom ur, boolean type) {
 		// remove/add the player thread from the player list
 		if (type){
 			gme.add(ur);
 			if (GUI)
 				myGUI.gameloader.addElement(ur);
 		}else{
 			gme.remove(ur);
 			if (GUI)
 				myGUI.gameloader.removeElement(ur);
 		}
 	}
 
 	static protected synchronized void Print(String string) {
 		Print(string, Color.BLACK);
 	}
 
 	static protected synchronized void Print(String string, Color txtclr) {
 		String newdate = time.format(new Date());
 		if (!GUI)
 			System.out.println(newdate + "-> " + string);
 		else {
 			myGUI.printGUI(newdate + "-> " + string, txtclr);
 
 		}
 		try{
 			bw.write(newdate + "-> " + string);
 			bw.newLine();
 		} catch(IOException e){
 			System.out.println("##ERROR PRINT TO LOG: " + e);
 			try {
 				bw.close();
 			} catch (IOException ioe) {
 				System.out.println("##ERROR IN CLOSING BUFFERWRITER STREAM");
 			}
 		}
 	}
 
 
 	public void CloseServer(){
 		for (GameRoom clnt : gme){			// (hopefully) a clean disconnect of gameroom
 			try{
 				clnt.close();					
 
 			} catch(NullPointerException ie){
 				try{
 					Print("Could not fully disconnect user " + clnt.id);
 				} catch (Exception io){
 					Print("Could not fully disconnect unknown user");
 				}
 			}
 		}
 
 		for (Fileio clnt : snct){			// if not, disconnect users individually
 			try{
 				clnt.close();					
 
 			} catch(NullPointerException ie){
 				try{
 					Print("Could not fully disconnect user " + clnt.id);
 				} catch (Exception io){
 					Print("Could not fully disconnect unknown user");
 				}
 			}
 		}
 
 		try {
 			bw.close();
 			server.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Checks if the filename passed in can be found (or if it is a directory instead)
 	 * Logic:
 	 * 	0: there is no file/directory with that path (or it is unrecognized..)
 	 * 	1: there is a file with that path
 	 * 	2: there is a directory with that path (don't know why I have this here, but...)
 	 * 
 	 * @param filePath the name of the file to find
 	 * @return
 	 */
 	static protected synchronized int findFile(String filePath){
 		int result = 0;
 		//System.out.println(filePath + "III" + new File(filePath).isFile());
 		if (new File(filePath).isFile()){
 			result = 1;
 		}
 		else if (new File(filePath).isDirectory()) result = 2;
 		return result;
 	}
 
 	/**
 	 * Prints a the array list of all the users on the server
 	 */
 	///@SuppressWarnings("unused")
 	public void PrintUsers(){
 		Print("List of users:");
 		for (Fileio clnt : snct){
 			if (clnt.isPlayer)
 				Print("-- " + clnt.id);
 		}
 	}
 
 
 
 
 }
