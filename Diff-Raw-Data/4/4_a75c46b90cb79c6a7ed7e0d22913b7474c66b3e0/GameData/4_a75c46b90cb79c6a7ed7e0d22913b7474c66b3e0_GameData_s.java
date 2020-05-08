 package data;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Observable;
 import java.util.Random;
 import java.util.EnumSet;
 
 import javax.swing.SwingUtilities;
 
 import json.simple.JSONArray;
 import json.simple.JSONObject;
 import json.simple.parser.ParseException;
 import admin.Utils;
 import data.bonus.Bonus;
 
 /**
  * GameData is the class that will be used to keep track of the important game
  * information, including the number of weeks passed, the lists of all/active
  * contestants, and the number of weeks remaining.
  * 
  * @author Graem Littleton, Ramesh Raj, Justin McDonald, Jonathan Demelo, Kevin
  *         Brightwell
  */
 
 public class GameData extends Observable {
 
 	private int weeksRem, weeksPassed; // keep track of weeks remaining/weeks passed
 	private int numInitialContestants, betAmount, totalAmount;
 	private boolean seasonStarted = false, elimExists = false;
 	private String[] tribeNames = new String[2]; // string array storing both tribe names
 	private Contestant[] castOffs; // array storing people who were cast off
 	private List<Contestant> allContestants; // List storing all contestants
 	private List<User> allUsers; // list storing all users
 	// store the current running version
 	private static GameData currentGame = null;
 	// store contestant who was cast off
 	private Contestant elimCont;
 	// used for asynchronous calls to notifyObservers()
     private UpdateCall updateExec;
 	
 	public enum UpdateTag {
 		START_SEASON, ADVANCE_WEEK, SET_TRIBE_NAMES, 
 		ADD_CONTESTANT, REMOVE_CONTESTANT, CONTESTANT_CAST_OFF,
 		ADD_USER, REMOVE_USER, 
 		FINAL_WEEK, END_GAME, ALLOCATE_POINTS, SAVE; 
 	}
 	
 	/**
 	 * JSON Keys
 	 */
 	private static final String KEY_CONTESTANTS = "cons",
 			KEY_NUM_CONTEST = "cons_num",
 			KEY_USERS = "users",
 			KEY_WEEKS_REMAIN = "weeks_rem",
 			KEY_WEEKS_PASSED = "weeks_pass",
 			KEY_TRIBES = "tribes_arr",
 			KEY_SEASON_STARTED = "season_started",
 			KEY_BET_AMOUNT = "bet_amount",
 			KEY_POOL_TOTAL = "pool_total",
 			KEY_CAST_OFFS = "cast_offs";
 
 	/**
 	 * Constructor method that takes a set number of contestants. Will not
 	 * proceed if numInitialContestants is NOT between 6 and 15, inclusive. Sets
 	 * number of weeks passed to 0 and weeks remaining to number of contestants
 	 * - 3.
 	 * 
 	 * @param numInitialContestants
 	 *            number of contestants to be in game
 	 */
 	// TODO: Make throw exception, its not enough to return, the object is still
 	// created.
 	public GameData(int numInitialContestants) {
 		// check if within parameters
 		if (numInitialContestants > 15 || numInitialContestants < 6)
 			return; // if not, do not create GameData item
 
 		weeksRem = numInitialContestants - 2;
 		weeksPassed = 0;
 		setBetAmount(0);
 		this.numInitialContestants = numInitialContestants;
 		
 		// containers for contestants and users
 		allContestants = new ArrayList<Contestant>(numInitialContestants);
 		allUsers = new ArrayList<User>(5);
 		castOffs = new Contestant[numInitialContestants];
 
 		currentGame = this;
 	}
 
 	// ----------------- ACCESSOR METHODS -----------------//
 
 						// CONTESTANT //
 	
 	/**
 	 * Returns whether or not a contestant has been selected to be cast off.
 	 * 
 	 * @return elimExists
 	 */
 	public boolean doesElimExist() {
 		return elimExists;
 	}
 	
 	/**
 	 * getAllContestants returns a list of all current and former contestants
 	 * that are/have been involved with the game.
 	 * 
 	 * @return this.allContestants
 	 */
 	public List<Contestant> getAllContestants() {
 		return allContestants;
 	}
 	
 	/**
 	 * Returns the contestant who was cast off on a specified week.
 	 * @param week
 	 * @return  castOffs[week]
 	 */
 	public Contestant getCastOff(int week){
 		return castOffs[week];
 	}
 	
 	/**
 	 * Get contestant based on unique id
 	 * 
 	 * @param id
 	 *            an unique id
 	 * @return the Contestant that matches id or null
 	 */
 	public Contestant getContestant(String id) {
 		int i = getContestantIndexID(id);
 
 		if (i >= 0)
 			return allContestants.get(i);
 		else
 			return null;
 	}
 	
 	/**
 	 * Returns the contestant who is to be eliminated.
 	 * 
 	 * @return elimCont
 	 */
 	protected Contestant getElimCont() {
 		return elimCont;
 	}
 	
 	/**
 	 * getInitialContestants returns an integer of the number of initial
 	 * contestants that are in the game.
 	 * 
 	 * @return The current amount of contestants
 	 */
 	public int getInitialContestants() {
 		return numInitialContestants;
 	}
 	
 	/**
 	 * getNumCurrentContestants returns an integer of the number of contestants
 	 * that are in the game.
 	 * 
 	 * @return The current amount of contestants
 	 */
 	public int getNumCurrentContestants() {
 		return allContestants.size();
 	}
 	
 	
 	                      // SPECIALTY GETTERS - CONTESTANT //
 	
 	/**
 	 * getActiveContestants returns an array (list) of the contestants that are
 	 * still competing in the game.
 	 * 
 	 * @return The contestants active
 	 */
 	public List<Contestant> getActiveContestants(boolean active) {
 
 		List<Contestant> list = new ArrayList<Contestant>(
 				allContestants.size());
 
 		for (Contestant c : allContestants) {
 			if (c != null) {
 				if (active && !c.isCastOff()) {
 					list.add(c);
 				} else if (!active && c.isCastOff()) {
 					list.add(c);
 				}
 			}
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * getContestant takes the first and last name of a contestant as input and
 	 * searches the array of current contestants for him/her. Returns
 	 * information found in the Contestant class to the caller.
 	 * 
 	 * @param first
 	 *            First name
 	 * @param last
 	 *            Last name
 	 * @return contestant or string object
 	 */
 	public Contestant getContestant(String first, String last) {
 		// loop through array
 		for (Contestant j : allContestants) {
 			if (first.equals(j.getFirstName()) && last.equals(j.getLastName())) { // ensure match
 				// return info on player
 				return j;
 			}
 		}
 		// otherwise return message saying contestant is no longer/is not in the game
 		return null;
 	}
 
 	
 			                // USER //
 	
 	/**
 	 * Gets the list of all users.
 	 * 
 	 * @return Vector containing all users.
 	 */
 	public List<User> getAllUsers() {
 		return allUsers;
 	}
 	
 	/**
 	 * Gets a user from the stored users by ID.
 	 * 
 	 * @param ID
 	 *            User ID of the User to get from the stored data.
 	 * @return User if ID found, null otherwise.
 	 */
 	public User getUser(String ID) {
 		for (User u : allUsers) {
 			if (u.getID().equalsIgnoreCase(ID)) {
 				return u;
 			}
 		}
 		return null;
 	}
 	
 	// ----------------- MUTATOR METHODS ----------------- //
 	
 					// CONTESTANT //
 	
 	/**
 	 * Adds a new contestant into the Game, this will maintain the list of
 	 * contestants as sorted by ID.
 	 * 
 	 * @param c
 	 *            New contestant, will not add if ID of contestant is null.
 	 */
 	public void addContestant(Contestant c) throws InvalidFieldException {
 		if (allContestants.size() == numInitialContestants) {
 			System.out.println("Too many contestants.");
 			return;
 		}
 
 		if (isContestantIDInUse(c.getID())) {
 			throw new InvalidFieldException(
 					InvalidFieldException.Field.CONT_ID_DUP,
 					"Contestant ID invald (in use)");
 		}
 
 		allContestants.add(c);
 
 		notifyAdd(UpdateTag.ADD_CONTESTANT);
 	}
 	
 	
 	
 	
 	/**
 	 * Sets the contestant who is to be eliminated.
 	 * 
 	 * @param elimCont 
 	 */
 	protected void setElimCont(Contestant elimCont) {
 		this.elimCont = elimCont;
 	}
 	
 	/**
 	 * Sets the elimExists variable
 	 * 
 	 * @param elimExists  true or false
 	 */
 	protected void setElimExists(boolean elimExists) {
 		this.elimExists = elimExists;
 	}
 
 	/**
 	 * removeContestant takes a Contestant object as input and attempts to
 	 * remove it from the array of active contestants. Maintains order of data
 	 * 
 	 * @param target
 	 *            Contestant to remove
 	 */
 	public void removeContestant(Contestant target) {
 		// is the contestant there?
 		allContestants.remove(target);
 		Collections.sort(allContestants);
 		
 		notifyAdd(UpdateTag.REMOVE_CONTESTANT);
 	}
 	
 	/**
 	 * Sets who was cast off on a certain week
 	 * @param week
 	 */
 	
 	public void setCastOff(int week, Contestant c){
 		castOffs[week] = c;
 		}
 	
 	/**
 	 * setCastOff method specifically for JSON.
 	 */
 	
 	public void setCastOffJSON(int week, Contestant c){
 		castOffs[week] = c;
 	}
 
 	                        // USER //
 	
 	/**
 	 * Adds a user to the list of users.
 	 * 
 	 * @param u
 	 *            New user to add.
 	 * @throws InvalidFieldException
 	 *             Thrown if ID already in use.
 	 */
 	public void addUser(User u) throws InvalidFieldException {
 		if (isUserIDInUse(u.getID())) {
 			throw new InvalidFieldException(
 					InvalidFieldException.Field.CONT_ID_DUP,
 					"Contestant ID invald (in use)");
 		}
 
 		allUsers.add(u);
 
 		notifyAdd(UpdateTag.REMOVE_USER);
 	}
 	
 	/**
 	 * Removes a user from the list.
 	 * 
 	 * @param u
 	 *            User to remove.
 	 */
 	public void removeUser(User u) {
 		allUsers.remove(u);
 
 		notifyAdd(UpdateTag.REMOVE_USER);
 	}
 
 				// ----  GAMEDATA INFORMATION ---- //
 						// ACCESSOR METHODS //
 	
 	/**
 	 * Returns the initial bet amount.
 	 * @return
 	 */
 	public int getBetAmount() {
 		return betAmount;
 	}
 	
 	/**
 	 * Returns the currently stored Game, this removed need to reference the
 	 * game data all the time. But also allows objects to read data, cleanly.
 	 * 
 	 * @return Currently started game, null if none present.
 	 */
 	public static GameData getCurrentGame() {
 		return GameData.currentGame;
 	}
 	
 	/**
 	 * Get the current week in play. Starts from Week 1.
 	 * 
 	 * @return Current week
 	 */
 	public int getCurrentWeek() {
 		return weeksPassed + 1;
 	}
 	
 	
 	
 	
 	/**
 	 * Returns the overall prize pool.
 	 * @return totalAmount
 	 */
 	public int getTotalAmount(){
 		return totalAmount;
 	}
 	
 	/**
 	 * getTribeName returns a String array with two entries: the name of the
 	 * first tribe, and the name of the second tribe.
 	 * 
 	 * @return String array tribe names
 	 */
 
 	public String[] getTribeNames() {
 		return tribeNames;
 	}
 	
 	/**
 	 * Checks if there are any more weeks remaining
 	 * 
 	 * @return true if weeks remaining = 1
 	 */
 	public boolean isFinalWeek() {
 		return weeksRem == 1;
 	}
 	
 	/**
 	 * Checks if there are any more weeks remaining
 	 * 
 	 * @return true if weeks remaining = 0
 	 */
 	public boolean isSeasonEnded() {
 		return weeksRem == 0;
 	}
 	
 	/**
 	 * Checks if a season has been started
 	 * 
 	 * @see startGame to set to true.
 	 * @return true if a season has started(different from created)
 	 */
 
 	public boolean isSeasonStarted() {
 		return seasonStarted;
 	}
 	
 	/**
 	 * weeksLeft returns the number of weeks remaining before the game ends.
 	 * 
 	 * @return this.weeksRem
 	 */
 	public int weeksLeft() {
 		return weeksRem;
 	}
 	
 						// MUTATOR METHODS //
 
 	
 	/**
 	 * Sets the initial bet amount.
 	 * @param betAmount
 	 */
 
 	public void setBetAmount(int betAmount) {
 		this.betAmount = betAmount;
 	}
 	
 	/**
 	 * Sets the overall prize pool.
 	 * @param total  
 	 */
 	public void setTotalAmount(int total){
 		this.totalAmount = total;
 	}
 	
 	/**
 	 * setTribeNames sets both tribe names accordingly and stores them in the
 	 * tribeNames string array. Updates all contestants accordingly
 	 * 
 	 * @param tribeOne
 	 *            name of tribe one
 	 * @param tribeTwo
 	 *            name of tribe two
 	 */
 	public String[] setTribeNames(String tribeOne, String tribeTwo){
 		// temp tribe vars.
 		String oldT1 = tribeNames[0];
 		String oldT2 = tribeNames[1];
 		// set the new tribes (Contestant requires this)
 		tribeNames[0] = Utils.strCapitalize(tribeOne.toLowerCase().trim());
 		tribeNames[1] = Utils.strCapitalize(tribeTwo.toLowerCase().trim());
 		// update all tribes first..
 		for (Contestant c : allContestants) {
 			if (c.getTribe().equalsIgnoreCase(oldT1)) {
 				try {
 					c.setTribe(tribeOne);
 				} catch (InvalidFieldException e) {
 				}
 			} else if (c.getTribe().equalsIgnoreCase(oldT2)) {
 				try {
 					c.setTribe(tribeTwo);
 				} catch (InvalidFieldException e) {
 				}
 			}
 		}
 
 		notifyAdd(UpdateTag.SET_TRIBE_NAMES);
 		return tribeNames;
 	}
 
 
 	// ----------------- HELPER METHODS ----------------- //
 	
 	/**
 	 * advanceWeek sets the number of weeksPassed to weeksPassed + 1.
 	 */
 	public void advanceWeek() {
 		if (elimExists == false)
 			return;
 		/* Fill weekly NULLs */
 		for (User u : allUsers) {
 			if (u.getWeeklyPick().isNull() || u.getWeeklyPick() == null) {
 				try {
 					u.setWeeklyPick(randomContestant(true));
 				} catch (InvalidFieldException e) {
 				} // wont happen
 			}
 			
 			/* Fill ultimate NULLs */
 			if (u.getUltimatePick().isNull()) {
 				try {
 					u.setUltimatePick(randomContestant(true));
 				} catch (InvalidFieldException e) {
 				} // wont happen
 			}
 		}
 		checkPicks();
 		allocatePoints(getElimCont());
 		Contestant nullC = new Contestant();
 		nullC.setNull();
 		
 		/* clear all weekly picks */
 		for (User u : allUsers) {
 			try {
 				u.setWeeklyPick(nullC);				
 			} catch (InvalidFieldException e) {
 			} // wont happen
 
 			/* clear defunct ult picks */
 			if (u.getUltimatePick().getID().equals(getElimCont().getID())) {
 				try {
 					u.setUltimatePick(nullC);			
 				} catch (InvalidFieldException e) {
 				} // wont happen
 			}
 		}
 		weeksRem -= 1; // reduce num of weeks remaining
 		weeksPassed += 1; // increment number of weeks passed
 		elimExists = false;
 		elimCont = null;
 		if (isFinalWeek())
 			notifyAdd(UpdateTag.FINAL_WEEK);
 		else if (isSeasonEnded())
 			notifyAdd(UpdateTag.END_GAME);
 		else
 			notifyAdd(UpdateTag.ADVANCE_WEEK);
 	}
 
 
 	/**
 	 * Iterates through all users on the list. Allocates points based off of
 	 * weekly elimination pick.
 	 * 
 	 * @param c
 	 *            Contestant that was cast off
 	 */
 
 	public void allocatePoints(Contestant c) {
 		Iterator<User> itr = allUsers.iterator();
 		User u;
 		while (itr.hasNext()) {
 			u = itr.next();
 			if (u.getWeeklyPick().equals(c)) {
 				if (this.isFinalWeek()) // final week
 					u.addPoints(40);
 				else  // normal week
 					u.addPoints(20);
 				
 			}
 			// if the end of the game and the person gets the right ultimate pick
 			if (u.getUltimatePick().equals(c) && this.isFinalWeek()){
 				u.addPoints(u.getUltimatePoints());
 			}
 		 u.addPoints(u.getNumBonusAnswer() * 10); // add week's correct bonus questions		
 	     u.setNumBonusAnswer(0); // clears the number of questions
 		}
 
 		notifyAdd(UpdateTag.ALLOCATE_POINTS);
 	}
 	
 	
 	/**
 	 * Casts a contestant from the game.
 	 * @param castOff
 	 */
 	public void castOff(int week, Contestant castOff) {
 		if (castOff.isCastOff()) // can't recast off..
 			return;
 		
 		setElimCont(castOff);
 		setElimExists(true);
 		setCastOff(week,castOff);
 		
 		castOff.setCastDate(week);
 		castOff.setToBeCast(true);
 		
 		notifyAdd(UpdateTag.CONTESTANT_CAST_OFF);
 	}
 	
 	/**
 	 * Checks who was chosen as a weekly or ultimate pick. Sets the contestant's picked status to
 	 * true if selected.
 	 */
 	
 	public void checkPicks(){
 		List<User> choices = getCurrentGame().getAllUsers();
 		for(User u : choices){
 			u.getWeeklyPick().selected();
 			u.getUltimatePick().selected();
 		}
 	}
 	
 	/**
 	 * Returns the prize pool split. Accounts for one winner, two winners, and three winners.
 	 * 
 	 * @return list of int values to give each user.
 	 */
 	
 	public List<Integer> determinePrizePool(){
 		   List<Integer> tempList = new ArrayList<Integer>(); 
 		   int i = getAllUsers().size();
 		    if (i <= 0){ // no users	
 		      return null;	
 		    } else if (i == 1) { // one user, he gets the whole pool	
 		      tempList.add(getTotalAmount());	
 		      return tempList;	
 		    } else if (i == 2) { // two users, first user gets 65% of the 
 		      //winnings, the rest goes to the second	
 		      tempList.add((int) (getTotalAmount()*0.65)); // first 65	
 		      tempList.add(getTotalAmount() - tempList.get(0)); // the rest	
 		    } else { // three or more users	
 		      // split is 60/30/10	
 		      tempList.add((int) (getTotalAmount()*0.60)); // first 60
 		
 		      // total amount - the first amount, which leaves 40% of the original amount
 		      // 30% is now equivalent to 75% of the new amount	
 		      tempList.add((int) ((getTotalAmount()- tempList.get(0)) * 0.75));
 		      // the total minus the first and second place winnings
 		      tempList.add(getTotalAmount() - tempList.get(0) - tempList.get(1));	
 		    }	
 		    return tempList;	
 		  }
 	
 	/**
 	* Iterates through all players on the list, and determines the top three winners. 	
 	*  @param  	 Player within the game.
 	*/
 			 	
 	  public List<User> determineWinners() {
 			
 	    Iterator<User> itr = allUsers.iterator();
 	    User u;	
 	    User first = new User ();	
 	    User second = new User ();
 		User third = new User ();
 	    first.setPoints(-1);
 	    second.setPoints(-1);
 	    third.setPoints(-1);	
 	    
 	    while (itr.hasNext()) {
 			
 	      u = itr.next();
 			
 	      if (u.getPoints() > first.getPoints()) {
 	       third = second;
 	       second = first;
 	       first = u;
 	      } else if (u.getPoints() > second.getPoints()){
 	    	third = second;  
 	        second = u;
 	      } else if (u.getPoints() > third.getPoints()){
 	       third = u;	
 	     }	
 	 }
 	   List<User> tempList = new ArrayList<User>(); 
 	   if (first.getPoints() != -1)
 		   tempList.add(first);	
 	   if (second.getPoints() != -1)
 		   tempList.add(second);	
 	   if (third.getPoints() != -1)
 		   tempList.add(third);	
 	   return tempList;	
 	 }
 	
 	  /**
 		 * Nulls the current game stored, allows a new game to start.
 		 */
 	public void endCurrentGame() {
 		
 		Bonus.deleteAllQuestions(); 
 		// removed tag, as there's a different between END_GAME and a reset. 
 		JSONUtils.resetSeason();
 		GameData.currentGame = null;
 	}
 		
 	/**
 	 * Helper method to get the index of a contestant ID in the
 	 * activeContestants array
 	 * 
 	 * @param searchID
 	 *            Search Contestant ID
 	 * @return Index in activeContestants where ID is stored, else < 0.
 	 */
 	protected int getContestantIndexID(String searchID) {
 		return Utils.BinIDSearchSafe(allContestants, searchID);
 	}
 	
 	/**
 	 * Gets a Users index in the stored list by ID. Uses a binary search for
 	 * speed.
 	 * 
 	 * @param searchID
 	 * @return Index in the list, index <0 if not found.
 	 */
 	protected int getUserIndexID(String searchID) {
 		return Utils.BinIDSearchSafe(allUsers, searchID);
 	}
 
 	
 	/**
 	 * Tells whether a Contestant ID is in use.
 	 * 
 	 * @param id
 	 *            The Contestant ID is in use.
 	 * @return True if in use.
 	 */
 	public boolean isContestantIDInUse(String id) {
 		return (getContestantIndexID(id) >= 0);
 	}
 
 	/**
 	 * Tells whether a User ID is in use.
 	 * 
 	 * @param id
 	 *            The Contestant ID is in use.
 	 * @return True if in use.
 	 */
 	public boolean isUserIDInUse(String id) {
 		return (getUserIndexID(id) >= 0);
 	}
 	
 	/**
 	 * Provides a random contestant.
 	 * 
 	 * @param isActive
 	 * @return random contestant
 	 */
 	public Contestant randomContestant(boolean isActive) {
 		List<Contestant> list = null;
 		if (isActive) {
 			list = getActiveContestants(true);
 			list.add(getElimCont()); // elim contestant still active
 		} else {
 			list = getAllContestants();
 		}
 		Random r = new Random();
 		int index = r.nextInt(list.size());
 		return list.get(index);
 	}
 	
 	
 	
 	/**
 	 * startGame sets gameStarted to true, not allowing the admin to add any
 	 * more players/Contestants to the pool/game.
 	 */
 
 	public void startSeason(int bet) {
 		this.setBetAmount(bet);
 		this.setTotalAmount(bet * allUsers.size());
 		seasonStarted = true;
 	
 		notifyAdd(UpdateTag.START_SEASON);
 	}
 	
 	/**
 	 * Undoes the current contestant that would be cast off.
 	 * @param castOff
 	 */
 	public void undoCastOff(int week, Contestant castOff) {
 		castOff.setToBeCast(false);
 	
 		setElimCont(null);
 		setElimExists(false);
 		
 		castOff.setCastDate(-1);	
 		
 		notifyAdd(UpdateTag.CONTESTANT_CAST_OFF);
 	}
 
 	// ----------------- JSON ----------------- //
 	
 	/**
 	 * Convert GameData to a JSON object
 	 * 
 	 * @return a JSONObject with all the relevant data
 	 * @throws JSONException
 	 */
 	public JSONObject toJSONObject() throws ParseException {
 		JSONObject obj = new JSONObject();
 
 		obj.put(KEY_NUM_CONTEST, new Integer(numInitialContestants));
 		JSONArray cons = new JSONArray();
 		for (Object o : allContestants) {
 			if (o != null)
 				cons.add(((Contestant) o).toJSONObject());
 		}
 
 		JSONArray users = new JSONArray();
 		for (Object o : allUsers) {
 			if (o != null)
 				users.add(((User) o).toJSONObject());
 		}
 		
 		JSONArray coffs = new JSONArray();
 		for(Contestant c : castOffs){
 			if(c != null)
 				coffs.add(c.toJSONObject());
 		}
 		
 		JSONArray ts = new JSONArray();
 		ts.add(tribeNames[0]);
 		ts.add(tribeNames[1]);
 
 		obj.put(KEY_CONTESTANTS, cons);
 		obj.put(KEY_USERS, users);
 		obj.put(KEY_CAST_OFFS, coffs);
 		obj.put(KEY_TRIBES, ts);
 		obj.put(KEY_WEEKS_REMAIN, weeksRem);
 		obj.put(KEY_WEEKS_PASSED, weeksPassed);
 		obj.put(KEY_SEASON_STARTED, seasonStarted);
 		
 		if(seasonStarted){
 			obj.put(KEY_BET_AMOUNT, betAmount);
 			obj.put(KEY_POOL_TOTAL, totalAmount);
 		}
 		
 		return obj;
 	}
 
 	/**
 	 * Update GameData with values from JSONObject
 	 * 
 	 * @param obj
 	 *            a JSONObject that contains all the values
 	 * @throws JSONException
 	 */
 	public void fromJSONObject(JSONObject obj) throws ParseException {
 		numInitialContestants = ((Number) obj.get(KEY_NUM_CONTEST)).intValue();
 
 		// tribes
 		JSONArray ts = (JSONArray) obj.get(KEY_TRIBES);
 		setTribeNames((String) ts.get(0), (String) ts.get(1));
 
 		// week info:
 		weeksRem = Utils.numToInt(obj.get(KEY_WEEKS_REMAIN));
 		weeksPassed = Utils.numToInt(obj.get(KEY_WEEKS_PASSED));
 
 		seasonStarted = (Boolean) obj.get(KEY_SEASON_STARTED);
 		
 		if(seasonStarted){
 			betAmount = Utils.numToInt(obj.get(KEY_BET_AMOUNT));
 			totalAmount = Utils.numToInt(obj.get(KEY_POOL_TOTAL));
 			System.out.println(betAmount + " " + totalAmount);
 		}
 
 		// Contestants must be loaded before users, but after others!
 		allContestants = new ArrayList<Contestant>(numInitialContestants);
 
 		// load the contestant array.
 		JSONArray cons = (JSONArray) obj.get(KEY_CONTESTANTS);
 		for (int i = 0; i < cons.size(); i++) {
 			Contestant c = new Contestant();
 			c.fromJSONObject((JSONObject)cons.get(i));
 			try {
 				addContestant(c);
 			} catch (InvalidFieldException ie) {
 			}
 		}
 		
 		// load the cast offs
 		JSONArray coffs = (JSONArray) obj.get(KEY_CAST_OFFS);
 		if(getCurrentWeek() != 1 && seasonStarted){
 			for(int i = 0; i < coffs.size(); i++){
 				Contestant c = new Contestant();
 				c.fromJSONObject((JSONObject)coffs.get(i));
 				   try{
 					   setCastOffJSON(i,c);
 				   }catch(NullPointerException ie){   
 				   }
 			}
 		}
 
 		// users:
 		JSONArray users = (JSONArray) obj.get(KEY_USERS);
 		allUsers = new ArrayList<User>(users.size());
 		for (int i = 0; i < users.size(); i++) {
 			User u = new User();
 			u.fromJSONObject((JSONObject)users.get(i));
 			try {
 				addUser(u);
 			} catch (InvalidFieldException ie) {
 			}
 			if(getCurrentWeek() >= 2);
 		}
 
 		notifyAdd();
 	}
 
 
 	/**
 	 * Write all DATA into file
 	 */
 	public void writeData() {
 
 		try {
 			JSONUtils.writeJSON(JSONUtils.pathGame, toJSONObject());
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// ---------------- INITIATION METHODS --------------- //
 	
 	/**
 	 * Used by SeasonCreate to create a new season.
 	 * 
 	 * @param num
 	 */
 	public static void initSeason(int num) {
 		currentGame = new GameData(num);
 	}
 
 	/**
 	 * intGameData reads in a data file and builds a GameData object out of it,
 	 * returning it to the user.
 	 * 
 	 * @param inputFile
 	 *            file to be read in
 	 * @return GameData object made out of file or null if season not created
 	 * 
 	 */
 	public static GameData initGameData() {
 		JSONObject json;
 		try {
 			json = JSONUtils.readFile(JSONUtils.pathGame);
 		} catch (FileNotFoundException e) {
 			return currentGame;
 		}
 
 		currentGame = new GameData(
 				Utils.numToInt(json.get(KEY_NUM_CONTEST)));
 		// TODO: Combine?
 		try {
 			GameData.getCurrentGame().fromJSONObject(json);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		
 		return currentGame;
 	}
 	
 	// ---------------- MISC --------------- //
 
 
 	/**
 	 * toString returns a string of the contestant's information in JSON format.
 	 */
 	public String toString() {
 		return new String("GameData<WR:\"" + weeksRem + "\"" + ", WP:\""
 				+ weeksPassed + "\"" + ", #C:\"" + numInitialContestants + "\""
 				+ ", SS: " + "\"" + seasonStarted + "\"" + ", TN: {" + "\""
 				+ tribeNames[0] + "\", \"" + tribeNames[1] + "\"}>");
 	}
 
 	/**
    * Small class used for removing parallel calls to do the same 
    * notification. The update system accounts for multiple modifications	
    * in one update call, so this means those methods are only called once.
    * @author Kevin Brightwell	
    */
 	
   private class UpdateCall implements Runnable {	
     public EnumSet<UpdateTag> mods = EnumSet.noneOf(UpdateTag.class);
 	
     public boolean done = false;
     public void run() {	
       setChanged();
       notifyObservers(mods);
       done = true;	
     }	
   }
 	
   /**	
    * Adds a set of {@link GameData.UpdateTag}s to the next update call. This	
    * method in conjunction with {@link GameData.UpdateCall} works to remove	
    * excess method executions.	
    * @param tags Tags to add to the next call.
    */
   public void notifyAdd(UpdateTag... tags) {
     if (updateExec == null || updateExec.done){ 
        updateExec = new UpdateCall();	
        SwingUtilities.invokeLater(updateExec);	
     }	
     for (UpdateTag ut: tags) {
       if (!updateExec.mods.contains(ut))	
         updateExec.mods.add(ut);	
     }
   }
 
   // ----------------- TEST DRIVER ------------------//
   
 	public static void main(String[] args) {
 		GameData g = new GameData(6);
 
 		String[] tribes = new String[] { "banana", "apple" };
 
 		g.setTribeNames(tribes[0], tribes[1]);
 
 		Contestant c1 = null, c2 = null;
 		try {
 			c1 = new Contestant("a2", "Al", "Sd", tribes[1]);
 			c2 = new Contestant("as", "John", "Silver", tribes[0]);
 		} catch (InvalidFieldException e) {
 			// wont happen.
 		}
 
 		try {
 			g.addContestant(c1);
 			g.addContestant(c2);
 		} catch (InvalidFieldException ie) {
 		}
 
 		g.startSeason(5);
 		User u1;
 		try {
 			u1 = new User("First", "last", "flast");
 			User u2 = new User("Firsto", "lasto", "flasto");
 			g.addUser(u1);
 			g.addUser(u2);
 			u1.setPoints(10);
 			u2.setPoints(1);
 		} catch (InvalidFieldException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 
 		try {
 			System.out.println(g.toJSONObject().toString());
 		} catch (ParseException e1) {
 			e1.printStackTrace();
 		}
 
 	}
 
 
 	/**
 	 * Creates a EnumSet of the tags passed, allowing for flexible notions
 	 * of multiple tags sent.
 	 * @param tags Sets the flags of the set.
 	 * @return EnumSet containing the tags passed.
 	 */
 	/*private EnumSet updateTagSet(UpdateTag... tags) {
 		EnumSet set = 
 		    new EnumSet(UpdateTag.class);
 		    return set.of(tags);
 	}*/
 }
