 package com.powertac.tourney.actions;
 
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.myfaces.custom.fileupload.UploadedFile;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Controller;
 
 import com.powertac.tourney.beans.Competition;
 import com.powertac.tourney.beans.Game;
 import com.powertac.tourney.beans.Games;
 import com.powertac.tourney.beans.Machine;
 import com.powertac.tourney.beans.Machines;
 import com.powertac.tourney.beans.Scheduler;
 import com.powertac.tourney.beans.Tournament;
 import com.powertac.tourney.beans.Tournaments;
 import com.powertac.tourney.services.CreateProperties;
 import com.powertac.tourney.services.Database;
 import com.powertac.tourney.services.RunBootstrap;
 import com.powertac.tourney.services.RunGame;
 import com.powertac.tourney.services.Upload;
 
 @Component("actionTournament")
 @Scope("session")
 public class ActionTournament {
 
 	@Autowired
 	private Upload upload;
 	
 	@Autowired
 	private Scheduler scheduler;
 
 	public enum TourneyType {
 		SINGLE_GAME, MULTI_GAME;
 	}
 	
 	private String selectedPom;
 
 	private Calendar initTime = Calendar.getInstance();
 	
 	
 	private Date startTime = new Date(); // Default to current date/time
 	private Date fromTime = new Date();
 	private Date toTime = new Date();
 	
 	private String tournamentName;
 	private int maxBrokers; // -1 means inf, otherwise integer specific
 	//private List<Integer> machines;
 	private List<String> locations;
 	private String pomName;
 	private String bootName;
 	private String propertiesName;
 	private UploadedFile pom;
 	private UploadedFile boot;
 	private UploadedFile properties;
 	private TourneyType type = TourneyType.SINGLE_GAME;
 	
 	
 	private int size1 = 2;
 	private int numberSize1;
 	private int size2 = 4;
 	private int numberSize2;
 	private int size3 = 8;
 	private int numberSize3;
 	
 	public ActionTournament(){
 		
 		initTime.set(2009, 2, 3);
 		fromTime.setTime(initTime.getTimeInMillis());
 		initTime.set(2011, 2, 3);
 		toTime.setTime(initTime.getTimeInMillis());
 		
 	}
 	
 	public void formType(ActionEvent event){
 		 
 		//Get submit button id
 		SelectItem ls = (SelectItem) event.getSource();
 		ls.getValue();
 		
 	}
 	
 	public TourneyType getMulti(){
 		return TourneyType.MULTI_GAME;
 	}
 	
 	public TourneyType getSingle(){
 		return TourneyType.SINGLE_GAME;
 	}
 
 	/**
 	 * @return the properties
 	 */
 	public UploadedFile getProperties() {
 		return properties;
 	}
 
 	/**
 	 * @param properties
 	 *            the properties to set
 	 */
 	public void setProperties(UploadedFile properties) {
 		this.properties = properties;
 	}
 
 	/**
 	 * @return the boot
 	 */
 	public UploadedFile getBoot() {
 		return boot;
 	}
 
 	/**
 	 * @param boot
 	 *            the boot to set
 	 */
 	public void setBoot(UploadedFile boot) {
 		this.boot = boot;
 	}
 
 	/**
 	 * @return the pom
 	 */
 	public UploadedFile getPom() {
 		return pom;
 	}
 
 	/**
 	 * @param pom
 	 *            the pom to set
 	 */
 	public void setPom(UploadedFile pom) {
 		this.pom = pom;
 	}
 
 	/**
 	 * @return the propertiesName
 	 */
 	public String getPropertiesName() {
 		return propertiesName;
 	}
 
 	/**
 	 * @param propertiesName
 	 *            the propertiesName to set
 	 */
 	
 	public void setPropertiesName(String propertiesName) {
 
 		// Generate MD5 hash
 		this.propertiesName = DigestUtils.md5Hex(propertiesName
 				+ (new Date()).toString() + Math.random());
 	}
 
 	/**
 	 * @return the bootName
 	 */
 	public String getBootName() {
 		return bootName;
 	}
 
 	/**
 	 * @param bootName
 	 *            the bootName to set
 	 */
 	public void setBootName(String bootName) {
 		// Generate MD5 hash
 		
 		this.bootName = DigestUtils.md5Hex(bootName + (new Date()).toString()
 				+ Math.random());
 		
 	}
 
 	public String getPomName() {
 		return pomName;
 	}
 
 	
 	public void setPomName(String pomName) {
 		// Generate MD5 hash
 		this.pomName = DigestUtils.md5Hex(pomName
 				+ (new Date()).toString() + Math.random());
 	}
 
 	
 
 	// Method to list the type enumeration in the jsf select Item component
 	public SelectItem[] getTypes() {
 		SelectItem[] items = new SelectItem[TourneyType.values().length];
 		int i = 0;
 		for (TourneyType t : TourneyType.values()) {
 			items[i++] = new SelectItem(t, t.name());
 		}
 		return items;
 	}
 
 	public TourneyType getType() {
 		return type;
 	}
 
 	public void setType(TourneyType type) {
 		this.type = type;
 	}
 
 	public Date getStartTime() {
 		return startTime;
 	}
 
 	public void setStartTime(Date startTime) {
 		this.startTime = startTime;
 	}
 
 	public int getMaxBrokers() {
 		return maxBrokers;
 	}
 
 	public void setMaxBrokers(int maxBrokers) {
 		this.maxBrokers = maxBrokers;
 	}
 
 	public String getTournamentName() {
 		return tournamentName;
 	}
 
 	public void setTournamentName(String tournamentName) {
 		this.tournamentName = tournamentName;
 	}
 
 	public String createTournament() {
 		// Create a tournament and insert it into the application context
 		Tournament newTourney = new Tournament();
 		if (type == TourneyType.SINGLE_GAME) {
 		
 		
 			/*this.setPomName(pom.getName());
 			upload.setUploadedFile(getPom());
 			String finalFile = upload.submit(this.getPomName());*/
 			newTourney.setPomName(selectedPom);
 			
 			String hostip = "http://";
 			
 			try {
 				InetAddress thisIp =InetAddress.getLocalHost();
 				hostip += thisIp.getHostAddress() + ":8080";
 			} catch (UnknownHostException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}
 			
 		
 			Database db = new Database();
 			
 			
 			
 			
 			newTourney.setPomUrl(hostip+"/TournamentScheduler/faces/pom.jsp?location="+newTourney.getPomName());
 			newTourney.setMaxBrokers(getMaxBrokers());
 			newTourney.setStartTime(getStartTime());
 			newTourney.setTournamentName(getTournamentName());
 
 			// Add one game to the global context and to the tournament
 			
 			
 			
 
 			
 
 			// Add game to all games and to Tournament
 			//allGames.addGame(newGame);
 			//newTourney.addGame(newGame);
 
 			//Tournaments.getAllTournaments().addTournament(newTourney);
 
 			// Start a single game and send jenkins request to kick the server
 			// at the appropriate time
 			String allLocations = "";
 			for (String s : locations){
 				allLocations += s + ",";
 			}
 			
 			int tourneyId = 0;
 			int gameId = 0;
 			
 			try {
 				//Starts new transaction to prevent race conditions
 				System.out.println("Starting transaction");
 				db.startTrans();
 				//Adds new tournament to the database
 				System.out.println("Adding tourney");
 				db.addTournament(newTourney.getTournamentName(), true, 1, new java.sql.Date(newTourney.getStartTime().getTime()), "SINGLE_GAME", newTourney.getPomUrl(), allLocations, maxBrokers);
 				//Grabs the tourney Id
 				
 				System.out.println("Getting tourneyId");
 				tourneyId = db.getMaxTourneyId();
 				// Adds a new game to the database
 				
 				
 				
 				System.out.println("Adding game");
 					
 				db.addGame(newTourney.getTournamentName(), tourneyId, size1, new java.sql.Date(startTime.getTime()));
 				// Grabs the game id
 				System.out.println("Getting gameId");
 				gameId = db.getMaxGameId();
 				System.out.println("Creating game: "+ gameId +" properties");
 				CreateProperties.genProperties(gameId, locations, fromTime, toTime);
 				
 				// Sets the url for the properties file based on the game id.
 				// Properties are created at random withing specified parameters
 				System.out.println("Updating properties game: " + gameId);
 				db.updateGamePropertiesById(gameId);
 				
 				
 				System.out.println("Committing transaction");
 				db.commitTrans();
 				
 				Properties props = new Properties();
 				try {
 					props.load(Database.class.getClassLoader().getResourceAsStream(
 							"/tournament.properties"));
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				db.closeConnection();
 				// Only schedule the bootstrap and sim if db was updated successfully
 				
				Scheduler.getScheduler().schedule(new RunBootstrap(gameId, hostip+"/TournamentScheduler/", newTourney.getPomUrl(), props.getProperty("destination")), new Date());
 				
 		
 				// A sim will only run if the bootstrap exists
				Scheduler.getScheduler().schedule(new RunGame(gameId, hostip+"/TournamentScheduler/", newTourney.getPomUrl(), props.getProperty("destination")), startTime);
 				
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			
 			
 			
 			
 			//Scheduler.getScheduler().schedule(
 				//	new StartServer(newGame, Machines.getAllMachines(),
 					//		Tournaments.getAllTournaments()),
 					//newGame.getStartTime());
 			try {
 				// TODO:REMOVE this is only to simulate the message from the
 				// server
 				// Thread.sleep(6000);
 				// URL test = new
 				// URL("http://localhost:8080/TournamentScheduler/faces/serverInterface.jsp?action=status&status=ready&gameId="+newGame.getGameId());
 				// test.openConnection().getInputStream();
 
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		} else if (type == TourneyType.MULTI_GAME) {
 			// First create the tournament
 			
 			// Use schedule code to create the set of games and place them in the database as placeholders
 			
 			// Create a timer to check for idle machines and schedule games
 			
 
 		} else {
 
 		}
 
 		// Tournaments allTournaments = (Tournaments)
 		// FacesContext.getCurrentInstance()
 		// .getExternalContext().getApplicationMap().get(Tournaments.getKey());
 
 		// allTournaments.addTournament(newTourney);
 
 		return "Success";
 
 	}
 	
 	public List<Database.Pom> getPomList(){
 		List<Database.Pom> poms = new ArrayList<Database.Pom>();
 		
 		Database db = new Database();
 		
 		try {
 			poms = db.getPoms();
 			db.closeConnection();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return poms;
 		
 	}
 	
 	public List<Machine> getAvailableMachineList(){
 		List<Machine> machines = new ArrayList<Machine>();
 		
 		Database db = new Database();
 		try {
 			List<Machine> all = db.getMachines();
 			for(Machine m : all){
 				if(m.isAvailable()){
 					machines.add(m);
 				}
 			}
 			db.closeConnection();
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return machines;
 	}
 
 	public Date getFromTime() {
 		return fromTime;
 	}
 
 	public void setFromTime(Date fromTime) {
 		this.fromTime = fromTime;
 	}
 
 	public Date getToTime() {
 		return toTime;
 	}
 
 	public void setToTime(Date toTime) {
 		this.toTime = toTime;
 	}
 
 	public List<String> getLocations() {
 		return locations;
 	}
 
 	public void setLocations(List<String> locations) {
 		this.locations = locations;
 	}
 
 
 	public String getSelectedPom() {
 		return selectedPom;
 	}
 
 	public void setSelectedPom(String selectedPom) {
 		this.selectedPom = selectedPom;
 	}
 
 	public int getSize1() {
 		return size1;
 	}
 
 	public void setSize1(int size1) {
 		this.size1 = size1;
 	}
 
 	public int getNumberSize1() {
 		return numberSize1;
 	}
 
 	public void setNumberSize1(int numberSize1) {
 		this.numberSize1 = numberSize1;
 	}
 
 	public int getSize2() {
 		return size2;
 	}
 
 	public void setSize2(int size2) {
 		this.size2 = size2;
 	}
 
 	public int getNumberSize2() {
 		return numberSize2;
 	}
 
 	public void setNumberSize2(int numberSize2) {
 		this.numberSize2 = numberSize2;
 	}
 
 	public int getSize3() {
 		return size3;
 	}
 
 	public void setSize3(int size3) {
 		this.size3 = size3;
 	}
 
 	public int getNumberSize3() {
 		return numberSize3;
 	}
 
 	public void setNumberSize3(int numberSize3) {
 		this.numberSize3 = numberSize3;
 	}
 
 }
