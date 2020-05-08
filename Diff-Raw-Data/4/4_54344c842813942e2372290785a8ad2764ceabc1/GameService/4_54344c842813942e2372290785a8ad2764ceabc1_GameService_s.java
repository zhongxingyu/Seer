 package edu.wm.werewolf.service;
 
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 
 import edu.wm.werewolf.HomeController;
 import edu.wm.werewolf.dao.IGameDAO;
 import edu.wm.werewolf.dao.IKillDAO;
 import edu.wm.werewolf.dao.IPlayerDAO;
 import edu.wm.werewolf.dao.IUserDAO;
 import edu.wm.werewolf.domain.GPSLocation;
 import edu.wm.werewolf.domain.Game;
 import edu.wm.werewolf.domain.JsonResponse;
 import edu.wm.werewolf.domain.Player;
 import edu.wm.werewolf.domain.PlayerBasic;
 import edu.wm.werewolf.domain.Score;
 import edu.wm.werewolf.domain.User;
 import edu.wm.werewolf.exceptions.GameNotFoundException;
 import edu.wm.werewolf.exceptions.NoRemainingPlayersException;
 import edu.wm.werewolf.exceptions.PlayerNotFoundException;
 
 public class GameService {
 	
 	@Autowired private IPlayerDAO playerDAO;
 	@Autowired private IUserDAO userDAO;
 	@Autowired private IKillDAO killDAO;
 	@Autowired private IGameDAO gameDAO;
 	private Game activeGame = null;
 	private boolean atNight;
 	private int scentRadius = 800;
 	private int killRadius = 500;
 	private int disqualificationInterval = 30;
 	private boolean testingMode = false;
 	
 	public void setTesting() {
 		testingMode = true;
 	}
 	
 	public List<PlayerBasic> getAllAlive() {
 		
 		List<Player> players = playerDAO.getAllAlive();
 		List<PlayerBasic> playersBasic = new ArrayList<PlayerBasic>();
 		
 		for (int i = 0; i < players.size(); i++) {
 			User u = userDAO.getUserByUsername(players.get(i).getUsername());
 			playersBasic.add(new PlayerBasic(u.getUsername(), u.getFirstname(), u.getLastname()));
 		}
 		
 		return playersBasic;
 	}
 	
 	public JsonResponse vote(String voterUsername, String votingForUsername) {
 		
 		Player voter;
 		try {
 			voter = playerDAO.getPlayerByUsername(voterUsername);
 		} catch (PlayerNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return new JsonResponse(false, "Error: your username could not be found.");
 		}
 		
 		Player votingFor;
 		try {
 			votingFor = playerDAO.getPlayerByUsername(votingForUsername);
 		} catch (PlayerNotFoundException e) {
 			e.printStackTrace();
 			return new JsonResponse(false, "Error: the player you voted for could not be found.");
 		}
 
 		if (voter.isDead())
 			return new JsonResponse (false, "Error: you must be alive to vote for other players.");
 		if (votingFor.isDead())
 			return new JsonResponse (false, "Error: the player you have voted for is already dead.");
 		if (atNight)
 			return new JsonResponse (false, "Error: you can only vote during the daytime.");
 		
 		playerDAO.vote(voter, votingFor);
 		return new JsonResponse(true, "Vote registered successfully.");	
 		
 	}
 	
 	public JsonResponse kill(String killerUsername, String victimUsername) {
 		
 		Player killer;
 		try {
 			killer = playerDAO.getPlayerByUsername(killerUsername);
 		} catch (PlayerNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return new JsonResponse(false, "Error: your username could not be found.");
 		}
 		
 		Player victim;
 		try {
 			victim = playerDAO.getPlayerByUsername(victimUsername);
 		} catch (PlayerNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return new JsonResponse(false, "Error: your victim's username could not be found.");
 		}
 		
 		if (!killer.isWerewolf())
 			return new JsonResponse (false, "Error: must be a werewolf to kill other players.");
 		if (victim.isDead())
 			return new JsonResponse (false, "Error: victim is already dead.");
 		if (killer.isDead())
 			return new JsonResponse (false, "Error: you must be alive to kill other players.");
 		if (!atNight)
 			return new JsonResponse (false, "Error: you can only kill players at night.");
 		
 		double playerDistance;
 		try {
 			playerDistance = playerDAO.getDistanceBetween(killer, victim);
 		} catch (PlayerNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return new JsonResponse(false, "Error: player not found");
 		}
 		catch (SQLException e) {
 			return new JsonResponse(false, "Error");
 		}
 		
 		if (playerDistance < 0 || playerDistance > killRadius)
 			return new JsonResponse(false, "Error: victim is not within range.");
 		else  {
 			playerDAO.setDead(victim);
 			return new JsonResponse(true, "Victim killed successfully.");
 		}
 		
 	}
 
 	public List<PlayerBasic> getAllNearby(String username) {
 
 		Player p;
 		try {
 			p = playerDAO.getPlayerByUsername(username);
 		} catch (PlayerNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 		
 		if (!p.isWerewolf() || p.isDead() || !atNight) {
 			return null;
 		}
 		
 		List<Player> players = playerDAO.getAllNear(new GPSLocation(p.getLat(), p.getLng()), scentRadius);
 		List<PlayerBasic> playersBasic = new ArrayList<PlayerBasic>();
 		
 		for (int i = 0; i < players.size(); i++) {
 			User u = userDAO.getUserByUsername(players.get(i).getUsername());
 			playersBasic.add(new PlayerBasic(u.getUsername(), u.getFirstname(), u.getLastname()));
 		}
 		
 		return playersBasic;
 		
 		
 	}
 
 	public List<PlayerBasic> getAllVotable() {
 		
 		List<Player> players = playerDAO.getAllAlive();
 		List<PlayerBasic> playersBasic = new ArrayList<PlayerBasic>();
 		
 		for (int i = 0; i < players.size(); i++) {
 			User u = userDAO.getUserByUsername(players.get(i).getUsername());
 			playersBasic.add(new PlayerBasic(u.getUsername(), u.getFirstname(), u.getLastname()));
 		}
 		
 		return playersBasic;
 	}
 
 	public List<Score> getScores() {
 		return userDAO.getScores();
 	}
 
 	public JsonResponse newGame(String adminUsername, int dayNightFrequency) {
 		
 		playerDAO.deleteAllPlayers();
 		List<User> users = userDAO.getAllUsers();
 		
 		List<Player> players = new ArrayList<Player>();
 		
 		for (int i = 0; i < users.size(); i++) {
 			Player p = new Player();
 			p.setUsername(users.get(i).getUsername());
 			players.add(p);
 		}
 		
 		Collections.shuffle(players, new Random(System.currentTimeMillis()));
 		int numberOfWerewolves = players.size() / 3;
 		
 		for (int i = 0; i < players.size(); i++) {
 			
 			if (i < numberOfWerewolves)
 				players.get(i).setWerewolf(true);
 			playerDAO.insertPlayer(players.get(i));
 			
 		}
 		
 		atNight = false;		
 		activeGame = new Game(adminUsername, Calendar.getInstance().getTime(), dayNightFrequency, null);
 		String gameID = gameDAO.newGame(activeGame);
 		activeGame.setGameID(gameID);
 		
 		JsonResponse r = new JsonResponse(true, "Sucessfully created new game");
 		return r;
 		
 	}
 
 	public JsonResponse restartGame(String username) {
 		
 		if (!activeGame.getAdmin().equals(username)) {
 			return new JsonResponse(false, "Error: only the admin is allowed to restart the game.");
 		}
 		
 		List<String> IDs = playerDAO.getAllIDs();
 		playerDAO.deleteAllPlayers();
 		List<Player> players = new ArrayList<Player>();
 		
 		for (int i = 0; i < IDs.size(); i++) {
 			Player p = new Player();
 			p.setUsername(IDs.get(i));
 			players.add(p);
 		}
 		
 		Collections.shuffle(players, new Random(System.currentTimeMillis()));
 		int numberOfWerewolves = players.size() / 3;
 		
 		for (int i = 0; i < players.size(); i++) {
 			
 			if (i < numberOfWerewolves)
 				players.get(i).setWerewolf(true);
 			playerDAO.insertPlayer(players.get(i));
 			
 		}
 		
 		try {
			gameDAO.restartGameByID(activeGame.getGameID());
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 			return new JsonResponse(false, "Error: could not restart game.");
 		}
 		
 		return new JsonResponse(true, "Successfully restarted the game.");
 	}
 	
 	public JsonResponse updatePosition(String username, GPSLocation location)  {
 		
 		playerDAO.moveByUsername(username, location);
 		return new JsonResponse(true, "Player location updated successfully.");
 		
 	}
 	
 	public void forceNight() {
 		atNight = true;
 		tallyVotes();
 		checkForEndOfGame();
 	}
 	
 	public void forceDay() {
 		atNight = false;
 		checkForEndOfGame();
 	}
 	
 	public boolean atNight() {
 		return atNight;
 	}
 	
 	@Scheduled(fixedRate=5000)
 	public void checkGameOperation() {
 		
 		if (testingMode)
 			return;
 		
 		//HomeController.logger.info("Checking game operation");
 		
 		if (!isActive())
 			return;
 		
 		playerDAO.removeInactivePlayers(disqualificationInterval);
 		
 		if (!atNight && activeGame.atNight()) {
 			HomeController.logger.info("Entering night cycle");
 			atNight = true;
 			tallyVotes();
 			checkForEndOfGame();
 		}
 		
 		if (atNight && !activeGame.atNight()) {
 			HomeController.logger.info("Entering day cycle");
 			atNight = false;
 			checkForEndOfGame();
 		}
 		
 	}
 	
 	private void checkForEndOfGame() {
 		
 		List<Player> aliveWerewolves;
 		List<Player> aliveTownspeople;
 		
 		try {
 			aliveWerewolves = playerDAO.getAliveWerewolves();
 			aliveTownspeople = playerDAO.getAliveTownspeople();
 		}
 		catch (SQLException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		int numWerewolves = aliveWerewolves.size();
 		int numTownspeople = aliveTownspeople.size();
 	
 		if (numWerewolves == 0)  {
 			gameOver(aliveTownspeople);
 		}
 		
 		if (numWerewolves > numTownspeople)  {
 			gameOver(aliveWerewolves);
 		}
 		
 	}
 
 	private void gameOver(List<Player> winners) {
 
 		for (int i = 0; i < winners.size(); i++)  {
 			User u = new User();
 			u.setUsername(winners.get(i).getUsername());
 			userDAO.logWin(u);
 		}
 		
 		playerDAO.deleteAllPlayers();
 		activeGame = null;
 		
 	}
 
 	public void tallyVotes() {
 		
 		try {
 			playerDAO.logVotes();
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 			return;
 		} catch (NoRemainingPlayersException e1) {
 			e1.printStackTrace();
 			return;
 		}
 		
 	}
 
 	public boolean isActive() {
 		return activeGame != null;
 	}
 
 }
