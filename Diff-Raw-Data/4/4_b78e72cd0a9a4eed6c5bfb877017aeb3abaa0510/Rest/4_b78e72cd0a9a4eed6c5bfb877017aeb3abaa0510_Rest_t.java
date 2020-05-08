 package com.powertac.tourney.services;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.stereotype.Service;
 
 import com.powertac.tourney.beans.Game;
 import com.powertac.tourney.beans.Games;
 import com.powertac.tourney.beans.Machines;
 import com.powertac.tourney.beans.Scheduler;
 import com.powertac.tourney.beans.Tournament;
 import com.powertac.tourney.beans.Tournaments;
 import com.powertac.tourney.constants.*;
 
 @Service
 public class Rest {
 	public static String parseBrokerLogin(Map<?, ?> params) {
 		String responseType = ((String[]) params.get(Constants.REQ_PARAM_TYPE))[0];
 		String brokerAuthToken = ((String[]) params
 				.get(Constants.REQ_PARAM_AUTH_TOKEN))[0];
 		String competitionName = ((String[]) params
 				.get(Constants.REQ_PARAM_JOIN))[0];
 
 		String retryResponse;
 		String loginResponse;
 		String doneResponse;
 
 		if (responseType.equalsIgnoreCase("xml")) {
 			retryResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><retry>%d</retry></message>";
 			loginResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><login><jmsUrl>%s</jmsUrl><gameToken>%s</gameToken></login></message>";
 			doneResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<message><done></done></message>";
 		} else {
 			retryResponse = "{\n \"retry\":%d\n}";
 			loginResponse = "{\n \"login\":%d\n \"jmsUrl\":%s\n \"gameToken\":%s\n}";
 			doneResponse = "{\n \"done\":\"true\"\n}";
 		}
 		Database db = new Database();
 		
 		try {
 			List<Game> allGames = db.getGames();
 			if (competitionName != null && allGames != null) {
 				for (Game g : allGames) {
 					// Only consider games that have started and are ready for
 					// brokers to join
 					Tournament t = db.getTournamentByGameId(g.getGameId());
 					//System.out.println("Game: " + g.getGameId() + " Status: " + g.getStatus());
 					if (g.getStartTime().before(new Date())
 							&& g.getStatus().equalsIgnoreCase("game-in-progress")) {
 						
 						if (competitionName.equalsIgnoreCase(t.getTournamentName())
 								&& g.isBrokerRegistered(brokerAuthToken)) {
 
 							db.closeConnection();
 							return String.format(loginResponse, g.getJmsUrl(),
 									"1234");
 						 }else{
 							 
 						 }
 					}
 					// If the game has yet to start and broker is registered
 					// send
 					// retry message
 					if (g.isBrokerRegistered(brokerAuthToken)) {
 						System.out
 								.println("[INFO] Broker: "
 										+ brokerAuthToken
 										+ " attempted to log in, game: "+ g.getGameId() +" with status: " + g.getStatus() + " --sending retry");
						long retry = (g.getStartTime().getTime()- (new Date()).getTime())/1000;
						System.out.println("[INFO] Game starts for Broker: " +brokerAuthToken + " in " + retry + " seconds");
 
 						db.closeConnection();
 						return String.format(retryResponse, retry > 0 ? retry
 								: 20);
 					}
 
 				}
 			}
 
 		} catch (Exception e) {
 			try {
 				db.closeConnection();
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			e.printStackTrace();
 		}
 		
 		return doneResponse;
 	}
 
 	public static String parseServerInterface(Map<?, ?> params) {
 		/*
 		 * System.out.println("Parsing Rest call...");
 		 * 
 		 * for(Object s : params.keySet()){ System.out.println("Key: " +
 		 * s.toString()); }
 		 * 
 		 * for(Object s : params.values()){ System.out.println("Value: " +
 		 * s.toString());
 		 * 
 		 * }
 		 */
 
 		if (params != null) {
 			Properties props = new Properties();
 			try {
 				props.load(Database.class.getClassLoader().getResourceAsStream(
 						"/tournament.properties"));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			String actionString = ((String[]) params
 					.get(Constants.REQ_PARAM_ACTION))[0];
 
 			if (actionString.equalsIgnoreCase("status")) {
 				String statusString = ((String[]) params
 						.get(Constants.REQ_PARAM_STATUS))[0];
 				String gameIdString = ((String[]) params
 						.get(Constants.REQ_PARAM_GAME_ID))[0];
 				int gameId = Integer.parseInt(gameIdString);
 
 				if (statusString.equalsIgnoreCase("bootstrap-running")) {
 					System.out
 							.println("[INFO] Recieved bootstrap running message from game: "
 									+ gameId);
 					Database db = new Database();
 					try {
 						db.updateGameStatusById(gameId, "boot-in-progress");
 						System.out.println("[INFO] Setting game: " + gameId + " to boot-in-progress");
 						db.closeConnection();
 						return "Success";
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 
 				} else if (statusString.equalsIgnoreCase("bootstrap-done")) {
 					System.out
 							.println("[INFO] Recieved bootstrap done message from game: "
 									+ gameId);
 					Database db = new Database();
 
 					String hostip = "http://";
 
 					try {
 						InetAddress thisIp = InetAddress.getLocalHost();
 						hostip += thisIp.getHostAddress() + ":8080";
 					} catch (UnknownHostException e2) {
 						// TODO Auto-generated catch block
 						e2.printStackTrace();
 					}
 
 					try {
 						db.startTrans();
 						db.updateGameBootstrapById(
 								gameId,
 								hostip
 										+ "/TournamentScheduler/faces/poms.jsp?location="
 										+ props.getProperty("fileUploadLocation")
 										+ gameId + "-boot.xml");
 						db.updateGameStatusById(gameId, "boot-complete");
 						System.out.println("[INFO] Setting game: " + gameId + " to boot-complete");
 						Game g = db.getGame(gameId);
 						db.setMachineStatus(g.getMachineId(), "idle");
 						db.commitTrans();
 						db.closeConnection();
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					return "Success";
 
 				} else if (statusString.equalsIgnoreCase("game-ready")) {
 					System.out
 							.println("[INFO] Recieved game ready message from game: "
 									+ gameId);
 					Database db = new Database();
 
 					try {
 						db.startTrans();
 						db.updateGameStatusById(gameId, "game-in-progress");
 						System.out.println("[INFO] Setting game: " + gameId + " to game-in-progress");
 						//Tournament t = db.getTournamentByGameId(gameId);
 						//db.updateTournamentStatus(t.getTournamentId());
 						db.commitTrans();
 						db.closeConnection();
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 					return "success";
 				} else if (statusString.equalsIgnoreCase("game-running")) {
 					// TODO Implement a message from the server to the ts
 
 				} else if (statusString.equalsIgnoreCase("game-done")) {
 					System.out.println("[INFO] Recieved game done message from game: "
 							+ gameId);
 					Database db = new Database();
 
 					try {
 						db.startTrans();
 						db.updateGameStatusById(gameId, "game-complete");
 						System.out.println("[INFO] Setting game: " + gameId + " to game-complete");
 						Game g = db.getGame(gameId);
 						// Do some cleanup
 						db.updateGameFreeBrokers(gameId);
 						System.out.println("[INFO] Freeing Brokers for game: " + gameId);
 						db.updateGameFreeMachine(gameId);
 						System.out.println("[INFO] Freeing Machines for game: " + gameId);
 						
 						db.setMachineStatus(g.getMachineId(), "idle");
 						db.commitTrans();
 						db.closeConnection();
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					return "success";
 				} else if (statusString.equalsIgnoreCase("game-failed")) {
 					System.out.println("[WARN] GAME " + gameId + " FAILED!");
 					Database db = new Database();
 					
 					try {
 						db.startTrans();
 						db.updateGameStatusById(gameId, "game-failed");
 						Game g = db.getGame(gameId);
 						
 						db.updateGameFreeBrokers(gameId);
 						db.updateGameFreeMachine(gameId);
 						db.setMachineStatus(g.getMachineId(), "idle");
 
 						db.commitTrans();
 						db.closeConnection();
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 					return "success";
 				} else if (statusString.equalsIgnoreCase("boot-failed")) {
 					System.out.println("[WARN] GAME " + gameId + " FAILED!");
 					Database db = new Database();
 
 					try {
 						db.startTrans();
 						db.updateGameStatusById(gameId, "boot-failed");
 						Game g = db.getGame(gameId);
 						db.setMachineStatus(g.getMachineId(), "idle");
 						db.commitTrans();
 						db.closeConnection();
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 					return "success";
 				} else {
 					return "ERROR";
 				}
 
 			}
 		}
 		return "Not Yet Implementented";
 	}
 
 	/***
 	 * Returns a properties file string
 	 * 
 	 * @param params
 	 * @return String representing a properties file
 	 */
 	public static String parseProperties(Map<?, ?> params) {
 		String gameId = "0";
 		if (params != null) {
 			try {
 				gameId = ((String[]) params.get(Constants.REQ_PARAM_GAME_ID))[0];
 			} catch (Exception e) {
 
 			}
 		}
 
 		List<String> props = new ArrayList<String>();
 
 		props = CreateProperties.getPropertiesForGameId(Integer
 				.parseInt(gameId));
 
 		String result = "";
 
 		// Location of weather data
 		String weatherLocation = "server.weatherService.weatherLocation = ";
 		// Simulation base time
 		String startTime = "common.competition.simulationBaseTime = ";
 		// Simulation jmsUrl
 		String jms = "server.jmsManagementService.jmsBrokerUrl = ";
 		
 		// Visualizer Settings
 		String remote = "server.visualizerProxyService.remoteVisualizer = ";//true";
 		
 	    String queueName = "server.visualizerProxyService.visualizerQueueName = ";
 	    
 	    // Test Settings
 	    String minTimeslot = "common.competition.minimumTimeslotCount = 220";
 	    String expectedTimeslot = "common.competition.expectedTimeslotCount = 240";
 	    
 	    // Timeout Settings
 	    String serverTimeout = "server.competitionControlService.loginTimeout = 120000";
 
 		if (props.size() == 4) {
 			result += weatherLocation + props.get(0) + "\n";
 			result += startTime + props.get(1) + "\n";
 			result += jms + props.get(2) + "\n";
 			result += serverTimeout + "\n";
 			if (props.get(2).length() > 2){
 				result += remote + "true\n"; 
 			}else{
 				result += remote + "\n";
 			}
 			result += minTimeslot + "\n";
 			result += expectedTimeslot + "\n";
 			result += queueName + props.get(3) + "\n";
 					 
 		}
 
 		return result;
 	}
 
 	/***
 	 * Returns a pom file string
 	 * 
 	 * @param params
 	 * @return String representing a pom file
 	 */
 	public static String parsePom(Map<?, ?> params) {
 		String location = "";
 		if (params != null) {
 			try {
 				location = ((String[]) params.get(Constants.REQ_PARAM_POM))[0];
 			} catch (Exception e) {
 
 			}
 		}
 
 		String result = "";
 
 		try {
 			// Open the file that is the first
 			// command line parameter
 			List<String> path = new ArrayList<String>();
 			String[] pathArray = (location.split("/"));
 			for (String s : pathArray) {
 				path.add(s.replace("..", ""));
 			}
 			Properties props = new Properties();
 			try {
 				props.load(Database.class.getClassLoader().getResourceAsStream(
 						"/tournament.properties"));
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			FileInputStream fstream = new FileInputStream(props.getProperty(
 					"fileUploadLocation", "/export/scratch")
 					+ path.get(path.size() - 1));
 			// Get the object of DataInputStream
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 			// Read File Line By Line
 			while ((strLine = br.readLine()) != null) {
 				// Print the content on the console
 				// System.out.println (strLine);
 				result += strLine + "\n";
 			}
 			// Close the input stream
 			in.close();
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 
 		return result;
 	}
 
 }
