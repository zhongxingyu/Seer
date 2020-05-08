 package com.samlawton.riotdemo.game;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import com.samlawton.riotdemo.achievements.Achievement;
 import com.samlawton.riotdemo.achievements.BigWinnerAchievement;
 import com.samlawton.riotdemo.achievements.BruiserAchievement;
 import com.samlawton.riotdemo.achievements.SharpshooterAchievement;
 import com.samlawton.riotdemo.achievements.ShootsFirstAchievement;
 import com.samlawton.riotdemo.achievements.VeteranAchievement;
 
 public class Player {
 	
 	private String mUserName;
 	public static final int NAME_IDX = 1;
 	
 	private String mUserCreateDate;
 	public static final int CREATE_DATE_IDX = 2;
 	
 	private int mTotalGames;
 	public static final int TOTAL_GAMES_IDX = 3;
 	
 	private int mTotalWins;
 	public static final int TOTAL_WINS_IDX = 4;
 	
 	private int mTotalLosses;
 	public static final int TOTAL_LOSSES_IDX = 5;
 	
 	private int mTotalAtkAttempts;
 	public static final int TOTAL_ATK_ATTEMPTS_IDX = 6;
 	
 	private double mTotalHitNum;
 	public static final int TOTAL_HIT_NUM_IDX = 7;
 	
 	private int mTotalDmg;
 	public static final int TOTAL_DMG_IDX = 8;
 	
 	private int mTotalKills;
 	public static final int TOTAL_KILLS_IDX = 9;
 	
 	private int mTotalFirstHitKills;
 	public static final int TOTAL_FIRST_HIT_KILLS_IDX = 10;
 	
 	private int mTotalAssists;
 	public static final int TOTAL_ASSISTS_IDX = 11;
 	
 	private int mTotalSpellsCast;
 	public static final int TOTAL_SPELLS_CAST_IDX = 12;
 	
 	private int mTotalSpellDmg;
 	public static final int TOTAL_SPELL_DMG_IDX = 13;
 	
 	private long mTotalPlayTime;
 	public static final int TOTAL_PLAY_TIME_IDX = 14;
 	
 	private int mTotalFirstBloods;
 	public static final int TOTAL_FIRST_BLOOD_IDX = 15;
 	
 	//TODO: New player statistics must be given properties here and a DB index corresponding to the column
 	
 	private ArrayList<Achievement> mPlayerAchievements;
 	
 	public static final int SHARPSHOOTER_IDX = 0;
 	public static final int BRUISER_IDX = 1;
 	public static final int VETERAN_IDX = 2;
 	public static final int BIG_WINNER_IDX = 3;
 	public static final int SHOOTS_FIRST_IDX = 4;
 	
 	public static final int ACHIEVEMENT_DB_OFFSET = 3;
 	public static final int SHARPSHOOTER_DB_IDX = SHARPSHOOTER_IDX + ACHIEVEMENT_DB_OFFSET;
 	public static final int BRUISER_DB_IDX = BRUISER_IDX + ACHIEVEMENT_DB_OFFSET;
 	public static final int VETERAN_DB_IDX = VETERAN_IDX + ACHIEVEMENT_DB_OFFSET;
 	public static final int BIG_WINNER_DB_IDX = BIG_WINNER_IDX + ACHIEVEMENT_DB_OFFSET;
 	public static final int SHOOTS_FIRST_DB_IDX = SHOOTS_FIRST_IDX + ACHIEVEMENT_DB_OFFSET;
 	//TODO: New achievements must be given a DB_IDX and Player Array _IDX
 	
 	/**
 	 * Constructor that uses the default Game database
 	 * connections.
 	 * @param aUserName The Player's name
 	 */
 	public Player(String aUserName) {
 		this(aUserName, null);
 	}
 	
 	/**
 	 * Constructor that takes a specific set of database
 	 * connection information. Only used in test scenarios.
 	 * @param aUserName The Player's name
 	 * @param aJDBCParams A set of JDBC connection parameters.
 	 */
 	public Player(String aUserName, String[] aJDBCParams) {
 		
 		mPlayerAchievements = initAchievements();
 		
		if(aUserName.contains("\\s") || aUserName.contains(";") || aUserName.contains("/") || aUserName.contains("'")) {
 			System.out.println("Error! Player name is invalid!");
			System.out.println("Player names are invalid when they contain a space, ';', '/', or \"'\".");
			System.out.println("Exiting Program.");
 			System.exit(1);
 		}
 		
 		mUserName = aUserName;
 		try {
 			if(aJDBCParams == null) {
 				initializePlayerFromDB(aUserName);
 			} else {
 				if(!(aJDBCParams.length == 4)) {
 					initializePlayerFromDB(aUserName);
 				} else {
 					initializePlayerFromDB(aUserName, aJDBCParams);
 				}
 			}
 		} catch (SQLException aEx) {
 			System.out.println("Slight database issue... but no worries!");
 			aEx.printStackTrace();
 			initializePlayerWithoutDB();
 		}	
 	}
 	
 	private ArrayList<Achievement> initAchievements() {
 		ArrayList<Achievement> newAchievementList = new ArrayList<Achievement>();
 		newAchievementList.add(new SharpshooterAchievement());
 		newAchievementList.add(new BruiserAchievement());
 		newAchievementList.add(new VeteranAchievement());
 		newAchievementList.add(new BigWinnerAchievement());
 		newAchievementList.add(new ShootsFirstAchievement());
 		// TODO: New achievements must be added to Player Achievement List here.
 		return newAchievementList;
 	}
 	
 	/**
 	 * A Player initialization that uses the default
 	 * database configurations.
 	 * @param aUserName The Player's name.
 	 * @throws SQLException
 	 */
 	private void initializePlayerFromDB(String aUserName) throws SQLException {
 		initializePlayerFromDB(aUserName, new String[]{null,null,null,null});
 	}
 	
 	/**
 	 * Initializes a Player using inputted JDBC information.
 	 * Usually called explicitly with tests.
 	 * @param aUserName The Player's name
 	 * @param aDriver The JDBC driver needed.
 	 * @param aURL The JDBC URL.
 	 * @param aUser The database User (SA)
 	 * @param aPass The User's password. 
 	 * @throws SQLException Will occur if there's an issue with the Connection or Statements
 	 */
 	private void initializePlayerFromDB(String aUserName, String[] aJDBCParams) throws SQLException {
 		Connection connection = null;
 		
 		String currentDriver = aJDBCParams[0] == null ? Game.jdbcDriver : aJDBCParams[0];
 		String currentJDBCURL = aJDBCParams[1] == null ? Game.jdbcString : aJDBCParams[1];
 		String currentUser = aJDBCParams[2] == null ? Game.jdbcUser : aJDBCParams[2];
 		String currentPass = aJDBCParams[3] == null ? Game.jdbcPass : aJDBCParams[3];
 		
 		try {
 			
 			Class.forName(currentDriver);
             
 			// Default user of the HSQLDB is 'sa'
             // with an empty password
             connection = DriverManager.getConnection(currentJDBCURL, currentUser, currentPass);
             
             PreparedStatement playerStmt = connection.prepareStatement("select * from players where userName = '" + aUserName + "'");
             ResultSet playerRS = playerStmt.executeQuery();
             
             if(playerRS.next()) {
             	mUserCreateDate = playerRS.getString(CREATE_DATE_IDX);
 	            mTotalGames = playerRS.getInt(TOTAL_GAMES_IDX);
 	            mTotalWins = playerRS.getInt(TOTAL_WINS_IDX);
 	        	mTotalLosses = playerRS.getInt(TOTAL_LOSSES_IDX);
 	        	mTotalAtkAttempts = playerRS.getInt(TOTAL_ATK_ATTEMPTS_IDX);
 	        	mTotalHitNum = playerRS.getDouble(TOTAL_HIT_NUM_IDX);
 	        	mTotalDmg = playerRS.getInt(TOTAL_DMG_IDX);
 	        	mTotalKills = playerRS.getInt(TOTAL_KILLS_IDX);
 	        	mTotalFirstHitKills = playerRS.getInt(TOTAL_FIRST_HIT_KILLS_IDX);
 	        	mTotalAssists = playerRS.getInt(TOTAL_ASSISTS_IDX);
 	        	mTotalSpellsCast = playerRS.getInt(TOTAL_SPELLS_CAST_IDX);
 	        	mTotalSpellDmg = playerRS.getInt(TOTAL_SPELL_DMG_IDX);
 	        	mTotalPlayTime = playerRS.getLong(TOTAL_PLAY_TIME_IDX);
 	        	mTotalFirstBloods = playerRS.getInt(TOTAL_FIRST_BLOOD_IDX);
 	        	//TODO: New player statistics must be grabbed from DB here.
             } else {
             	mUserCreateDate = DateFormat.getDateTimeInstance().format(new Date());
             	mTotalGames = 0;
 	            mTotalWins = 0;
 	        	mTotalLosses = 0;
 	        	mTotalAtkAttempts = 0;
 	        	mTotalHitNum = 0.0;
 	        	mTotalDmg = 0;
 	        	mTotalKills = 0;
 	        	mTotalFirstHitKills = 0;
 	        	mTotalAssists = 0;
 	        	mTotalSpellsCast = 0;
 	        	mTotalSpellDmg = 0;
 	        	mTotalPlayTime = 0;
 	        	mTotalFirstBloods = 0;
 	        	//TODO: New player statistics must be initialized here
 	        	connection.prepareStatement("insert into players values ('" + aUserName + "','" +
 	        			mUserCreateDate + "'," +
 	        			mTotalGames + "," +
 	        			mTotalWins + "," +
 	        			mTotalLosses + "," +
 	        			mTotalAtkAttempts + "," +
 	        			mTotalHitNum + "," +
 	        			mTotalDmg + "," +
 	        			mTotalKills + "," +
 	        			mTotalFirstHitKills + "," +
 	        			mTotalAssists + "," +
 	        			mTotalSpellsCast + "," +
 	        			mTotalSpellDmg + "," +
 	        			mTotalPlayTime + "," +
 	        			mTotalFirstBloods + ")").execute();
 	        			//TODO: New player statistics must be inserted into DB column here (column must already exist)
             }
             
             PreparedStatement achievementStmt = connection.prepareStatement("select * from playerAchievements where userName = '" + aUserName +"'");
             ResultSet achieveRS = achievementStmt.executeQuery();
             
             if(achieveRS.next()) {
             	for(int i = 0; i < mPlayerAchievements.size(); i++) {
             		mPlayerAchievements.get(i).setIsAchieved(achieveRS.getBoolean(ACHIEVEMENT_DB_OFFSET + i));
             	}
             } else {
             	for(int i = 0; i < mPlayerAchievements.size(); i++) {
             		mPlayerAchievements.get(i).setIsAchieved(false);
             	}
             	connection.prepareStatement("insert into playerAchievements values (NULL,'" +
             			aUserName + "'," +
             			mPlayerAchievements.get(SHARPSHOOTER_IDX).getIsAchieved() + "," +
             			mPlayerAchievements.get(BRUISER_IDX).getIsAchieved() + "," +
             			mPlayerAchievements.get(VETERAN_IDX).getIsAchieved() + "," +
             			mPlayerAchievements.get(BIG_WINNER_IDX).getIsAchieved() + "," +
             			mPlayerAchievements.get(SHOOTS_FIRST_IDX).getIsAchieved() + ")").execute();
             			// TODO: New player achievements must be initialized in achievement DB here.
             }
 			
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			if(connection != null) connection.close();
 		}
 	}
 	
 	/**
 	 * Uses non-database information to initialize player.
 	 */
 	private void initializePlayerWithoutDB() {
 		mUserCreateDate = DateFormat.getDateTimeInstance().format(new Date());
 		mTotalGames = 0;
         mTotalWins = 0;
     	mTotalLosses = 0;
     	mTotalAtkAttempts = 0;
     	mTotalHitNum = 0.0;
     	mTotalDmg = 0;
     	mTotalKills = 0;
     	mTotalFirstHitKills = 0;
     	mTotalAssists = 0;
     	mTotalSpellsCast = 0;
     	mTotalSpellDmg = 0;
     	mTotalPlayTime = 0;
     	mTotalFirstBloods = 0;
     	//TODO: New player level stats must be initialized here.
     	
     	for(int i = 0; i < mPlayerAchievements.size(); i++) {
     		mPlayerAchievements.get(i).setIsAchieved(false);
     	}
 	}
 	
 	/**
 	 * Gets the create date of the User
 	 * account.
 	 * @return The date in a long format (millis)
 	 */
 	public String getUserCreateDate() {
 		return mUserCreateDate;
 	}
 	
 	/**
 	 * Updates the Player data in the database with the
 	 * default JDBC settings
 	 * @throws SQLException Will occur if there's an issue with the Connection or Statements
 	 */
 	public void updatePlayerDataInDB() throws SQLException {
 		updatePlayerDataInDB(null);
 	}
 	
 	/**
 	 * Updates the Player data in the database with the
 	 * given JDBC Parameters.
 	 * @param jdbcParams The four JDBC parameters.
 	 * @throws SQLException
 	 */
 	public void updatePlayerDataInDB(String[] jdbcParams) throws SQLException {
 		Connection connection = null;
 		
 		String currentDriver = jdbcParams[0] == null ? Game.jdbcDriver : jdbcParams[0];
 		String currentJDBCURL = jdbcParams[1] == null ? Game.jdbcString : jdbcParams[1];
 		String currentUser = jdbcParams[2] == null ? Game.jdbcUser : jdbcParams[2];
 		String currentPass = jdbcParams[3] == null ? Game.jdbcPass : jdbcParams[3];
 		
 		try {
 			
 			Class.forName(currentDriver);
             
 			// Default user of the HSQLDB is 'sa'
             // with an empty password
             connection = DriverManager.getConnection(currentJDBCURL, currentUser, currentPass);
             
             connection.prepareStatement("update players set " + 
             		"totalGames=" + mTotalGames + "," + 
             		"totalWins=" + mTotalWins + "," +
             		"totalLosses=" + mTotalLosses + "," +
             		"totalAtkAttempts=" + mTotalAtkAttempts + "," +
             		"totalHitNum=" + mTotalHitNum + "," +
             		"totalDmg=" + mTotalDmg + "," +
             		"totalKills=" + mTotalKills + "," +
             		"totalFirstHitKills=" + mTotalFirstHitKills + "," +
             		"totalAssists=" + mTotalAssists + "," +
             		"totalSpellsCast=" + mTotalSpellsCast + "," +
             		"totalSpellDmg=" + mTotalSpellDmg + "," +
             		"totalPlayTime=" + mTotalPlayTime + "," +
             		"totalFirstBloods=" + mTotalFirstBloods + " where userName = '" + mUserName + "'").execute();
             		//TODO: New player level stats must be updated in DB here.            
 			
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			if(connection != null) connection.close();
 		}
 	}
 	
 	/**
 	 * Updates the Player's historical stats using it's
 	 * recent InGame representative.
 	 * @param aJustPlayed The InGamePlayer from the most recent game.
 	 */
 	public void updatePlayerHistoricalStats(InGamePlayer aJustPlayed) {
 		mTotalGames += 1;
 		
 		if(aJustPlayed.isGameWin()) {
 			mTotalWins += 1;
 		} else {
 			mTotalLosses += 1;
 		}
 		
     	mTotalAtkAttempts += aJustPlayed.getGameAtkAttempts();
     	
     	if(mTotalHitNum == 0) {
     		mTotalHitNum = aJustPlayed.getGameHitNum();
     	} else {
     		mTotalHitNum = (mTotalHitNum + aJustPlayed.getGameHitNum())/2;
     	}
     	
     	mTotalDmg += aJustPlayed.getGameDmg();
     	mTotalKills += aJustPlayed.getGameKills();
     	mTotalFirstHitKills += aJustPlayed.getGameFirstHitKills();
     	mTotalAssists += aJustPlayed.getGameAssists();
     	mTotalSpellsCast += aJustPlayed.getGameSpellsCast();
     	mTotalSpellDmg += aJustPlayed.getGameSpellDmg();
     	mTotalPlayTime += aJustPlayed.getGamePlayTime();
     	mTotalFirstBloods += aJustPlayed.getFirstBlood() ? 1 : 0;
     	
     	//TODO: New player level statistics must be updated here by InGamePlayer
 	}
 	
 	public void printAllHistoricalPlayerStats() {
 		printAllHistoricalPlayerStats(new String[]{null,null,null,null});
 	}
 	
 	/**
 	 * Prints out both the Player's stats and their
 	 * Achievement data.
 	 */
 	public void printAllHistoricalPlayerStats(String[] aJDBCParams) {
 		printPlayersHistoricalStats();
 		printPlayersAchievementData();
 		printPlayersGamesPlayed(aJDBCParams);
 	}
 	
 	/**
 	 * Visual test method to check current Player's game stats
 	 */
 	public void printPlayersHistoricalStats() {
 		System.out.println("Player " + mUserName + "'s Historical Stats: ");
 		System.out.println("Total Games: " + mTotalGames);
 		System.out.println("Total Wins: " + mTotalWins);
 		System.out.println("Total Losses: " + mTotalLosses);
 		System.out.println("Attack Attempts: " + mTotalAtkAttempts);
 		System.out.println("Hit Percentage: " + mTotalHitNum);
 		System.out.println("Total Damage: " + mTotalDmg);
 		System.out.println("Kills: " + mTotalKills);
 		System.out.println("First Hit Kills: " + mTotalFirstHitKills);
 		System.out.println("Assists: " + mTotalAssists);
 		System.out.println("Spells Cast: " + mTotalSpellsCast);
 		System.out.println("Spell Damage: " + mTotalSpellDmg);
 		System.out.println("Total Game Time (millis): " + mTotalPlayTime);
 		System.out.println("Total First Bloods: " + mTotalFirstBloods);
 		System.out.println();
 		
 		// TODO: New player statistics must be added here for print out
 	}
 	
 	/**
 	 * Prints out the current Player's achievement Data.
 	 */
 	public void printPlayersAchievementData() {
 		for(int i = 0; i < mPlayerAchievements.size(); i++) {
 			Achievement currAchievement = mPlayerAchievements.get(i);
 			if(currAchievement.getIsAchieved()) {
 				System.out.println(mUserName + " has the " + currAchievement.getAchievementName() + " Achievement.");
 			} else {
 				System.out.println(mUserName + " does not have the " + currAchievement.getAchievementName() + " Achievement.");
 			}
 		}
 		System.out.println();
 	}
 	
 	public void printPlayersGamesPlayed(String[] aJDBCParams) {
 		
 		System.out.println("Player " + mUserName +"'s Played Games.");
 		try {
 			
 			Connection connection = null;
 			
 			String currentDriver = aJDBCParams[0] == null ? Game.jdbcDriver : aJDBCParams[0];
 			String currentJDBCURL = aJDBCParams[1] == null ? Game.jdbcString : aJDBCParams[1];
 			String currentUser = aJDBCParams[2] == null ? Game.jdbcUser : aJDBCParams[2];
 			String currentPass = aJDBCParams[3] == null ? Game.jdbcPass : aJDBCParams[3];
 			
 			try {
 				
 				Class.forName(currentDriver);
 				
 				connection = DriverManager.getConnection(currentJDBCURL, currentUser, currentPass);
 				
 				ResultSet gameResults = connection.prepareStatement("select gameID from gamesPlayed where userName = '" + mUserName + "'").executeQuery();
 				
 				while(gameResults.next()) {
 					System.out.println(gameResults.getString(1));
 				}
 				System.out.println();
 				
 				if(gameResults != null) gameResults.close();
 				
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} finally {
 				connection.close();
 			}
 			
 		} catch(SQLException ex) {
 			
 		}
 	}
 	
 	/**
 	 * Returns the Player's name.
 	 * @return The Player's name.
 	 */
 	public String getUserName() {
 		return mUserName;
 	}
 	
 	/**
 	 * Get Player's Total Wins.
 	 * @return The Player's Total Wins.
 	 */
 	public int getTotalWins() {
 		return mTotalWins;
 	}
 	
 	/**
 	 * Get Player's Total Losses
 	 * @return The Player's Total Losses
 	 */
 	public int getTotalLosses() {
 		return mTotalLosses;
 	}
 	
 	/**
 	 * Get the Player's Total Games
 	 * @return The Player's Total Games.
 	 */
 	public int getTotalGames() {
 		return mTotalGames;
 	}
 	
 	/**
 	 * Get the Player's Achievements
 	 * @return The Player's Achievements
 	 */
 	public ArrayList<Achievement> getPlayerAchievements() {
 		return mPlayerAchievements;
 	}
 	
 	public int getFirstBloods() {
 		return mTotalFirstBloods;
 	}
 
 	@Override
 	public int hashCode() {
 		return mUserName.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		Player otherPlayer = (Player)obj;
 		return otherPlayer.getUserName().equals(mUserName);
 	}
 
 }
