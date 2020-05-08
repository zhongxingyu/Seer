 /**
  *  Copyright (C) 2012 K3RNEL Developer Team
  *  
  *  This file is part of Distro Wars (Server).
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Affero General Public License for more details.
  *  
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.k3rnel.arena.server;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.Scanner;
 
 
 import net.k3rnel.arena.server.network.TCPManager;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 
 
 /**
  * Represents a game server.
  * 
  * Starting a server requires a parameter to be passed in, i.e. java GameServer -s low -p 500
  * Here are the different settings:
  * -low
  * 		< 1.86ghz
  * 		< 256MB Ram
  * 		< 1mbps Up/Down Connection
  * 		75 Players
  * -medium
  * 		< 2ghz
  * 		512MB - 1GB Ram
  * 		1mbps Up/Down Connection
  * 		200 Players
  * -high
  * 		> 1.86ghz
  * 		> 1GB Ram
  * 		> 1mbps Up/Down Connection
  * 		> 500 Players
  * 
  * @author shadowkanji
  * @author Nushio
  *
  */
 public class GameServer {
 	private static GameServer m_instance;
 	private static ServiceManager m_serviceManager;
 	private static int m_maxPlayers, m_movementThreads;
 	private static String m_dbServer, m_dbName, m_dbUsername, m_dbPassword, m_serverName;
 	private int m_highest;
 
 	/**
 	 * Load pre-existing settings if any
 	 * NOTE: It loads the database password if available.
 	 * Password is line after serverName
 	 */
 	private void loadSettings(){
 	    System.out.println("Loading settings...");
 		File foo = new File("res/settings.txt");
 		if(foo.exists()) {
 			try {
 				Scanner s = new Scanner(foo);
 				m_dbServer = s.nextLine();
 				m_dbName = s.nextLine();
 				m_dbUsername = s.nextLine();
 				m_serverName = s.nextLine();
 				m_dbPassword = s.nextLine();
				if(m_dbPassword.isEmpty()||m_dbPassword.equals(" ")){
				    ConsoleReader r = new ConsoleReader();
			        System.out.println("Please enter the required information.");
			        System.out.println("Database Password: ");
			        m_dbPassword = r.readToken();
				}
 				s.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}else{
 		    System.out.println("Settings not found.");
 		    getConsoleSettings();
 		}
 	}
 
 	/**
 	 * Asks for Database User/Pass, then asks to save
 	 * NOTE: It doesnt save the database password
 	 */
 	private void getConsoleSettings(){
 		ConsoleReader r = new ConsoleReader();
 		System.out.println("Please enter the required information.");
 		System.out.println("Database Server: ");
 		m_dbServer = r.readToken();
 		System.out.println("Database Name:");
 		m_dbName = r.readToken();
 		System.out.println("Database Username:");
 		m_dbUsername = r.readToken();
 		System.out.println("Database Password:");
 		m_dbPassword = r.readToken();
 		System.out.println("This server's IP or hostname:");
 		m_serverName = r.readToken();
 		System.out.println("Save info? (y/N)");
 		String answer = r.readToken();
 		if(answer.contains("y")||answer.contains("Y")){
 		    System.out.println("Save Password in Plaintext? (y/N)");
 		    answer = r.readToken();
 		    if(answer.contains("y")||answer.contains("Y"))
 		        saveSettings(true);
 		    else
 		        saveSettings(false);
 		}
 		System.out.println();
 		System.err.println("WARNING: When using -nogui, the server should only be shut down using a master client");
 	}
 
 	/**
 	 * Default constructor
 	 */
 	public GameServer(boolean autorun) {
 		m_instance = this;
 		if(autorun){
 			loadSettings();
 			start();
 		}else{
 			ConsoleReader r = new ConsoleReader();
 			System.out.println("Load Settings? y/N");
 			String answer = r.readToken();
 			if(answer.contains("y")||answer.contains("Y")){
 				loadSettings();
 			}else{
 				getConsoleSettings();
 			}
 			start();
 		}
 	}
 
 
 	/**
 	 * Starts the game server
 	 */
 	public void start() {
 		/*
 		 * Store locally
 		 */
 		m_serviceManager = new ServiceManager();
 		m_serviceManager.start();
 	}
 
 	/**
 	 * Stops the game server
 	 */
 	public void stop() {
 		m_serviceManager.stop();
 		try {
 			/* Let threads finish up */
 			Thread.sleep(10000);
 			/* Exit */
 			System.out.println("Exiting server...");
 			System.exit(0);
 		} catch (Exception e) {}
 
 	}
 
 	/**
 	 * Writes server settings to a file
 	 */
 	private void saveSettings(boolean savepass) {
 		try {
 			/*
 			 * Write to file
 			 */
 			File f = new File("res/settings.txt");
 			if(f.exists())
 				f.delete();
 			PrintWriter w = new PrintWriter(f);
 			w.println(m_dbServer);
 			w.println(m_dbName);
 			w.println(m_dbUsername);
 			w.println(m_serverName);
 			if(savepass)
 			    w.println(m_dbPassword);
 			else
 			    w.println(" ");
 			w.flush();
 			w.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Updates the player count information
 	 * @param amount
 	 */
 	public void updatePlayerCount() {
 		int amount = TCPManager.getPlayerCount();
 		System.out.println(amount + " players online");
 		if(amount > m_highest) {
 			m_highest = amount;
 			System.out.println("Highest: " + amount);
 		}
 
 
 	}
 
 	/**
 	 * Returns the instance of game server
 	 * @return
 	 */
 	public static GameServer getInstance() {
 		return m_instance;
 	}
 
 	/**
 	 * If you don't know what this method does, you clearly don't know enough Java to be working on this.
 	 * @param args
 	 */
 	public static void main(String [] args) {
 		/*
 		 * Pipe errors to a file
 		 */
 		try {
 			PrintStream p = new PrintStream(new File("./errors.txt"));
 			System.setErr(p);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		/*
 		 * Server settings
 		 */
 		Options options = new Options();
 		options.addOption("s","settings", true, "Can be low, medium, or high.");
 		options.addOption("p","players", true, "Sets the max number of players.");
 		options.addOption("ng", "nogui", false, "Starts server in headless mode.");
 		options.addOption("ar", "autorun", false, "Runs without asking a single question.");
 		options.addOption("h", "help", false, "Shows this menu.");
 
 		if(args.length > 0) {
 			CommandLineParser parser = new GnuParser();
 			try {
 				// parse the command line arguments
 				CommandLine line = parser.parse( options, args );
 
 				/*
 				 * The following sets the server's settings based on the
 				 * computing ability of the server specified by the server owner.
 				 */
 				if( line.hasOption( "settings" ) ) {
 					String settings = line.getOptionValue( "settings" );
 					if(settings.equalsIgnoreCase("low")) {
 						m_movementThreads = 4;
 					} else if(settings.equalsIgnoreCase("medium")) {
 						m_movementThreads = 8;
 					} else if(settings.equalsIgnoreCase("high")) {
 						m_movementThreads = 12;
 					} else {
 						System.err.println("Server requires a settings parameter");
 						HelpFormatter formatter = new HelpFormatter();
 						formatter.printHelp( "java GameServer [param] <args>", options );
 						System.exit(0);
 					}
 				}else{
 					System.err.println("Server requires a settings parameter");
 					HelpFormatter formatter = new HelpFormatter();
 					formatter.printHelp( "java GameServer [param] <args>", options );
 					System.exit(0);
 				}
 
 				if(line.hasOption("players")) {
 					m_maxPlayers = Integer.parseInt(line.getOptionValue( "players" ));
 					if(m_maxPlayers == 0 || m_maxPlayers == -1)
 						m_maxPlayers = 99999;
 				} else {
 					System.err.println("WARNING: No maximum player count provided. Will default to 500 players.");
 					m_maxPlayers = 500;
 				}
 
 				if(line.hasOption("help")){
 					HelpFormatter formatter = new HelpFormatter();
 					System.err.println("Server requires a settings parameter");
 					formatter.printHelp( "java GameServer [param] <args>", options );
 				}
 
 				/*
 				 * Create the server gui
 				 */
 				@SuppressWarnings("unused")
 				GameServer gs;
 				if(line.hasOption("nogui")){
 					if(line.hasOption("autorun"))
 						gs = new GameServer(true);
 					else
 						gs = new GameServer(false);
 				}else{
 					if(line.hasOption("autorun"))
 						System.out.println("autorun doesn't work with GUI");
 					gs = new GameServer(false);
 				}
 			}
 			catch( ParseException exp ) {
 				// oops, something went wrong
 				System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
 				// automatically generate the help statement
 				HelpFormatter formatter = new HelpFormatter();
 				formatter.printHelp( "java GameServer [param] <args>", options );
 			}
 
 		}else{
 			// automatically generate the help statement
 			HelpFormatter formatter = new HelpFormatter();
 			System.err.println("Server requires a settings parameter");
 			formatter.printHelp( "java GameServer [param] <args>", options );
 		}
 	}
 
 	/**
 	 * Returns the service manager of the server
 	 * @return
 	 */
 	public static ServiceManager getServiceManager() {
 		return m_serviceManager;
 	}
 
 	/**
 	 * Returns the amount of players this server will allow
 	 * @return
 	 */
 	public static int getMaxPlayers() {
 		return m_maxPlayers;
 	}
 
 	/**
 	 * Returns the amount of movement threads running in this server
 	 * @return
 	 */
 	public static int getMovementThreadAmount() {
 		return m_movementThreads;
 	}
 
 	/**
 	 * Returns the database host
 	 * @return
 	 */
 	public static String getDatabaseHost() {
 		return m_dbServer;
 	}
 
 	/**
 	 * Returns the database username
 	 * @return
 	 */
 	public static String getDatabaseUsername() {
 		return m_dbUsername;
 	}
 
 	/**
 	 * Returns the database password
 	 * @return
 	 */
 	public static String getDatabasePassword() {
 		return m_dbPassword;
 	}
 
 	/**
 	 * Returns the name of this server
 	 * @return
 	 */
 	public static String getServerName() {
 		return m_serverName;
 	}
 
 	/**
 	 * Returns the database selected
 	 * @return
 	 */
 	public static String getDatabaseName() {
 		return m_dbName;
 	}
 }
