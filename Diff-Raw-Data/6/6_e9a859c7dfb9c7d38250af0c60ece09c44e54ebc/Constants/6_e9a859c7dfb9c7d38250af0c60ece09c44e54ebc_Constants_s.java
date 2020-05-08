 package org.powertac.tourney.constants;
 
 import org.powertac.tourney.beans.Agent;
 import org.powertac.tourney.beans.Game;
 import org.powertac.tourney.beans.Machine;
 import org.powertac.tourney.beans.Tournament;
 
 public class Constants
 {
   public class Rest
   {
     // Possible Rest Parameters for Broker Login
     public static final String REQ_PARAM_AUTH_TOKEN = "authToken";
     public static final String REQ_PARAM_JOIN = "requestJoin";
     public static final String REQ_PARAM_TYPE = "type";
 
     // Possible Rest Paramenters for Server Interface
     public static final String REQ_PARAM_STATUS = "status";
     public static final String REQ_PARAM_GAME_ID = "gameId";
     public static final String REQ_PARAM_ACTION = "action";
     public static final String REQ_PARAM_FILENAME = "fileName";
 
     // Possible Rest Parameters for pom service
     public static final String REQ_PARAM_POM_ID = "pomId";
   }
 
   // Prepared Statements for Database access
   /***
    * Start / commit / abort db transaction
    */
   public static final String START_TRANS  = "START TRANSACTION;";
   public static final String COMMIT_TRANS = "COMMIT;";
   public static final String ABORT_TRANS  = "ROLLBACK;";
 
   /***
    * @param userName
    *          : User name attempting to login
    * @param password
    *          : salted md5 hash of entered password
    */
   public static final String LOGIN_SALT =
     "SELECT password, salt, permissionId, userId FROM users WHERE userName=?;";
 
   /***
    * @param userName
    *          : The desired username to use (this must be unique)
    * @param password
    *          : The salted md5 hash of the password
    * @param permissionId
    *          : The desired permission level 0=Admin 4=Guest (Recommend Guest)
    */
   public static final String ADD_USER =
     "INSERT INTO users (userName, institution, contactName, contactEmail, "
       + "contactPhone, salt, password, permissionId) VALUES (?,?,?,?,?,?,?,?);";
 
   /***
    * Select all users
    */
   public static final String SELECT_USERS = "SELECT * FROM users;";
 
   /***
    * Select user name
    * @param userId
    *          : The id of the user we want to get the name from
    */
   public static final String SELECT_USER_BYID =
       "SELECT * FROM users WHERE userId=? limit 1;";
 
   /***
    * @param brokerName
    *          : The name of the Broker to use for logins
    * @param brokerAuth
    *          : The md5 hash token to use for broker authorization
    * @param brokerShort
    *          : The short description about the broker
    * @param userId
    *          : The userId of the user that owns this broker
    */
   public static final String ADD_BROKER =
     "INSERT INTO brokers (brokerName,brokerAuth,brokerShort, userId) VALUES (?,?,?,?);";
   
   /***
    * @param brokerName : The name of the broker to check existence
    * 
    */
   public static final String NUM_BROKER_COPIES =
       "SELECT COUNT(*) as num FROM brokers WHERE brokerName=?";
 
   /***
    * Select all brokers by their userId
    * 
    * @param userId
    *          : The userId of the brokers to return
    */
   public static final String SELECT_BROKERS_BY_USERID =
     "SELECT * FROM brokers WHERE userID = ?;";
 
   /***
    * Select broker by their brokerId
    * 
    * @param brokerId
    *          : The brokerId of the broker you wish to return
    */
   public static final String SELECT_BROKER_BY_BROKERID =
     "SELECT * FROM brokers WHERE brokerId = ? LIMIT 1;";
 
   /***
    * Select broker by their brokerAuth
    *
    * @param brokerAuth
    *          : The brokerId of the broker you wish to return
    */
   public static final String SELECT_BROKER_BY_BROKERAUTH =
       "SELECT * FROM brokers WHERE brokers.brokerAuth = ? LIMIT 1;";
 
   /**
    * Delete a broker by their brokerId
    * 
    * @param brokerId
    *          : The brokerId of the broker you wish to delete
    * 
    */
   public static final String DELETE_BROKER_BY_BROKERID =
     "DELETE FROM brokers WHERE brokerId = ?;";
 
   /**
    * Update a broker by their brokerId
    * 
    * @param brokerName
    * @param brokerAuth
    * @param brokerShort
    * @param brokerID
    *          : The brokerId of the broker you wish to update
    */
   public static final String UPDATE_BROKER_BY_BROKERID =
     "UPDATE brokers SET brokerName = ?, brokerAuth = ?, brokerShort = ? WHERE brokerId = ?;";
 
   /***
    * Returns the list of all tournaments in the database of a particular status
    * ( pending, in-progress, complete) possible
    * 
    * @param status
    *          : either "pending", "in-progress", or "complete"
    */
   public static final String SELECT_TOURNAMENTS =
     "SELECT * FROM tournaments WHERE status=?;";
 
   /***
    * Returns the list of all tournaments a broker is registered in
    *
    * @param brokerId
    *          : id of the broker we want the tournaments for
    */
   public static final String SELECT_TOURNAMENTS_BY_BROKERID =
       "SELECT * FROM tournaments "
           + "JOIN registration ON tournaments.tourneyId = registration.tourneyId "
           + "JOIN brokers ON registration.brokerId = brokers.brokerId "
           + "WHERE brokers.brokerId = ? "
           + "AND not tournaments.status='complete';";
 
   /***
    * Selects a tournament from the database by tournamentId
    * 
    * @param tournamentId
    *          : Specify the ID to select a particular tournament
    */
   public static final String SELECT_TOURNAMENT_BYID =
     "SELECT * FROM tournaments WHERE tourneyId=?;";
 
   /***
    * Selects a tournament from the database by gameId
    * 
    * @param gameId
    *          : Specify the ID of the game to select the tournament
    */
   public static final String SELECT_TOURNAMENT_BYGAMEID =
     "SELECT * FROM tournaments "
         + "JOIN games ON tournaments.tourneyId = games.tourneyId WHERE gameId=?;";
 
   /***
    * Select a tournament by type
    * 
    * @param type
    */
   public static final String SELECT_TOURNAMENT_BYTYPE =
     "SELECT * FROM tournaments WHERE type=? AND NOT status = '"
         + Tournament.STATE.complete + "';";
 
   /***
    * Select a tournament by name, case insensitive
    *
    * @param tournamentName
    */
   public static final String SELECT_TOURNAMENT_BYNAME =
       "SELECT * FROM tournaments WHERE tourneyName LIKE ?;";
 
   /***
    * Adds a tournament to the database with pending status by default
    * 
    * @param tourneyName
    *          : The name of the tournament
    * @param startTime
    *          : The timestamp when the tournament scheduler will issue a request
    *          to start the powertac simulation server
    * @param type
    *          : This is either "MULTI_GAME" or "SINGLE_GAME"
    * @param locations
    *          : This is a comma delimited list of the possible locations
    *          available in the tournament (Used for weather models)
    * @param maxBrokers
    *          : Maximum brokers allowed in this tournament round
    * @param pomId
    *          : pomId of the pom (foreignkey)
    */
   public static final String ADD_TOURNAMENT =
     "INSERT INTO tournaments " +
     "(tourneyName, startTime, dateFrom, dateTo, type, locations, maxBrokers, " +
     "status, gameSize1, gameSize2, gameSize3, pomId) VALUES " +
     "(?,?,?,?,?,?,?,'" + Tournament.STATE.pending.toString() + "',?,?,?,?);";
 
   /***
    * Remove a tournament by its Id
    *
    * @param tourneyId
    *          : The Id of the tournament
    */
   public static final String REMOVE_TOURNAMENT =
       "DELETE FROM tournaments WHERE tourneyId=?;";
 
   /***
    * Updates a particular tournament given the id
    * 
    * @param status
    *          : The new status : "pending", "in-progress", or "complete"
    * @param tournamentId
    *          : The id of the tournament you wish to change
    */
   public static final String UPDATE_TOURNAMENT_STATUS_BYID =
     "UPDATE tournaments SET status = ? WHERE tourneyId=?";
 
   /***
    * Updates a particular tournament given the id
    *
    * @param startTime
    *          : The new startTime
    * @param tournamentId
    *          : The id of the tournament you wish to change
    */
   public static final String UPDATE_TOURNAMENT_STARTTIME_BYID =
       "UPDATE tournaments SET startTime = ? WHERE tourneyId=?";
 
   /**
    * Select the max tournament id from all the tournaments
    */
   public static final String SELECT_MAX_TOURNAMENTID =
     "SELECT MAX(tourneyId) as maxId FROM tournaments;";
 
   /***
    * Get the number of brokers registered for a tournament
    * 
    * @param tourneyId
    *          : The id of the tournament you wish to query
    */
   public static final String GET_NUMBER_REGISTERED_BYTOURNAMENTID =
     "SELECT COUNT(brokerId) as numRegistered FROM registration " +
         "WHERE registration.tourneyId=?;";
 
   /***
    * Get a list of all brokers
    */
   public static final String GET_BROKERS =
     "SELECT * FROM brokers ;";
 
   /***
    * Register for a tournament by tourneyId and brokerId
    * 
    * @param tourneyId
    *          : The id of the tournament you wish to register for
    * @param brokerId
    *          : The id of the broker you wish to register
    */
   public static final String REGISTER_BROKER =
     "INSERT INTO registration (tourneyId, brokerId) VALUES (?, ?);";
 
   /***
    * Unregister for a tournament (admin functionality)
    * 
    * @param tourneyId
    *          : The id of the tournament
    * @param brokerId
    *          : The id of the broker
    */
   public static final String UNREGISTER_BROKER =
     "DELETE FROM registration WHERE tourneyId=? AND brokerId=?;";
 
   /***
    * Check if a broker is registered for a tournament
    * 
    * @param tourneyId
    *          : The id of the tournamnet
    * @param brokerId
    *          : The id of the broker
    */
   public static final String REGISTERED =
     "SELECT COUNT(*)=1 as registered FROM registration WHERE tourneyId=? "
         + "AND brokerId=?;";
 
   /***
    * Insert a new game into the database to be run (only ever insert games
    * without bootstraps
    * 
    * @param gameName
    *          : The name of the running game
    * @param tourneyId
    *          : The id of the tournament the game is running under
    * @param maxBrokers
    *          : The maximum number of brokers allowed in this game
    * @param startTime
    *          : The scheduled start time of the sim
    */
   public static final String ADD_GAME =
     "INSERT INTO games (gameName, tourneyId, maxBrokers, startTime, readyTime, "
         + "status, jmsUrl, visualizerUrl, visualizerQueue, serverQueue, "
         + "hasBootstrap) "
         + "VALUES(?,?,?,?,NULL,'" + Game.STATE.boot_pending.toString()
         + "','','','','',false);";
 
   /***
    * Returns a list of the runnable MULTI_GAME games as of now.
    */
   public static final String GET_RUNNABLE_MULTI_GAMES =
     "SELECT * FROM games WHERE startTime<=UTC_TIMESTAMP() "
         + "AND status='" + Game.STATE.boot_complete.toString() + "' "
         + "AND tourneyId=?;";
   
   /***
    * Returns a list of the runnable SINGLE_GAME games as of now.
    */
   public static final String GET_RUNNABLE_SINGLE_GAMES =
       "SELECT * FROM games WHERE startTime<=UTC_TIMESTAMP() AND status='" +
           Game.STATE.boot_complete.toString() + "' " +
           "AND tourneyId in (SELECT tourneyId FROM tournaments WHERE type='" +
           Tournament.TYPE.SINGLE_GAME.toString() + "');";
 
   /***
    * Returns a list of the bootable games as of now
    */
   public static final String GET_BOOTABLE_GAMES =
     "SELECT * FROM games WHERE status='" + Game.STATE.boot_pending.toString() + "';";
 
   public static final String FIND_GAME_BY_STATE_AND_MACHINE =
           "SELECT * FROM games g JOIN machines m "
           + "ON g.machineId = m.machineId "
           + "WHERE g.status=? AND m.machineName=?";
   
   /***
    * Add broker to game in database
    * 
    * @param gameId
    *          : The id of the game you wish to add the broker to
    * @param brokerId
    *          : The id of the broker
    * @param brokerAuth
    *          : The authToke of the broker
    * @param brokerName
    *          : The name of the broker
    */
   public static final String ADD_BROKER_TO_GAME =
     "INSERT INTO agents (gameId, brokerId, brokerQueue, status) "
         + "VALUES (?,?,?,'"+ Agent.STATE.pending.toString() +"')";
 
   /***
    * Get brokers in a game by gameid
    * 
    * @param gameId
    */
   public static final String GET_BROKERS_IN_GAME =
     "SELECT * FROM brokers "
         + "JOIN agents ON brokers.brokerId = agents.brokerId "
         + " WHERE gameId=?";
 
   /***
    * Get status of a broker in a game
    *
    * @param gameId
    */
   public static final String GET_BROKER_STATUS_IN_GAME =
       "SELECT * FROM agents WHERE gameId=? AND brokerId=?";
 
   /***
    * Get total list of rinning broker instances
    *
    * @param brokerId
    */
   public static final String GET_RUNNING_AGENTS =
      "SELECT * FROM agents WHERE brokerId=? "
          + "AND status='"+ Agent.STATE.in_progress.toString() +"'";
 
   /***
    * Get brokers in a completed game by gameid
    *
    * @param gameId
    */
   public static final String GET_BROKERS_IN_GAME_COMPLETE =
       "SELECT * FROM brokers "
           + "JOIN agents ON agents.brokerId = brokers.brokerId "
           + "JOIN games ON games.gameId = agents.gameId "
           + "WHERE games.gameId=? "
           + "AND agents.status='"+ Agent.STATE.complete.toString() +"' "
           + "ORDER BY brokers.brokerName;";
 
   public static final String UDATE_BROKER_INGAME =
       "UPDATE agents SET status = ? WHERE gameId=? and brokerId=?;";
 
   /***
    * Get brokers in a tournament
    * 
    * @param tourneyId
    */
   public static final String GET_BROKERS_INTOURNAMENT =
     "SELECT * FROM brokers JOIN registration "
 				+ "ON registration.brokerId = brokers.brokerId WHERE registration.tourneyId=?";
 
   /**
    * Get brokerQueue for game+broker combo
    * @param gameId
    * @param brokerId
    */
   public static final String GET_BROKER_QUEUE =
       "SELECT * FROM agents WHERE gameId=? AND brokerId=?;";
 
   /***
    * Select game by id
    * 
    * @param gameId
    *          : The id of the game you wish to retrieve from the db
    */
   public static final String SELECT_GAMEBYID =
     "SELECT * FROM games WHERE gameId=?;";
 
   /***
    * Update jmsUrl
    * 
    * @param jmsUrl
    * @param gameId
    */
   public static final String UPDATE_GAME_JMSURL =
     "UPDATE games SET jmsUrl=?, serverQueue=? WHERE gameId=?;";
 
   /***
    * Clear the readyTime of a game
    *
    * @param gameId
    *          : The id of the game
    */
   public static final String CLEAR_GAME_READYTIME =
       "UPDATE games SET readyTime=null WHERE gameId=?;";
 
   /***
    * Update the readyTime of a game
    *
    * @param readyTime
    *          : The dateTime at which the sim says it's ready
    * @param gameId
    *          : The id of the game
    */
   public static final String UPDATE_GAME_READYTIME =
       "UPDATE games SET readyTime=? WHERE gameId=?;";
 
   /***
    * Update the startTime of a game
    *
    * @param readyTime
    *          : The dateTime at which the sim says it's ready
    * @param gameId
    *          : The id of the game
    */
   public static final String UPDATE_GAME_STARTTIME =
       "UPDATE games SET startTime=? WHERE gameId=?;";
 
   /***
    * Update the machine a game is running on
    * 
    * @param machineId
    *          : The id of the machine the game is running on
    * @param gameId
    *          : The id of the game
    */
   public static final String UPDATE_GAME_MACHINE =
     "UPDATE games SET machineId=? WHERE gameId=?;";
   
   public static final String UPDATE_SERVER = "UPDATE GameServers "
       + "SET IsPlaying = 0 WHERE ServerNumber=?;";
 
   /***
    * Update the game to free the machine
    * 
    * @param gameId
    *          : the id of the game
    */
   public static final String UPDATE_GAME_FREE_MACHINE =
     "UPDATE games SET machineId=NULL WHERE gameId=?;";
 
   /***
    * Update the game to free the brokers
    * 
    * @param gameId
    *          : the id of the game
    */
   public static final String UPDATE_AGENT_STATUS =
     "UPDATE agents SET status = ? WHERE gameId=?;";
 
   /***
    * Update the game to free the brokers
    *
    * @param gameId
    *          : the id of the game
    */
   public static final String DELETE_AGENTS_BY_GAMEID =
       "DELETE FROM agents WHERE gameId=?;";
 
 
   /***
    * Update the visualizerUrl for a game that is running
    * 
    * @param visualizerUrl
    *          : The url of the visualizer
    * @param gameId
    *          : The id of the game
    */
   public static final String UPDATE_GAME_VIZ =
     "UPDATE games SET visualizerUrl=?, visualizerQueue=? WHERE gameId=?;";
 
   /***
    * Update Game hasBootstrap
    *
    * @param hasBootstrap
    *          : Does the game has a bootstrap
    * @param gameId
    *          : The id of the game you wish to change
    */
   public static final String UPDATE_GAME_BOOTSTRAP =
     "UPDATE games SET hasBootstrap=? WHERE gameId=?;";
 
   /***
    * Update Game status by gameId
    *
    * @param status
    *          : The new status of the game either
    *          "boot_pending", "boot_in_progress", "boot_complete", "boot_failed",
    *          "game_pending", "game_in_progress", "game_complete", "game_failed"
    * @param gameId
    *          : The id of the game you wish to change
    */
   public static final String UPDATE_GAME =
       "UPDATE games SET status = ? WHERE gameId = ?";
 
   /***
    * Delete a game from the database (may need to do a cascading delete)
    * 
    * @param gameId
    *          : The id of the game to delete
    */
   public static final String REMOVE_GAME =
     "DELETE FROM games WHERE gameId=?;";
 
   /***
    * Select all running and pending games
    */
   public static final String SELECT_GAME =
     "SELECT * FROM games WHERE NOT status = '"
         + Game.STATE.game_complete.toString() + "';";
 
   
   public static final String SELECT_COMPLETE_GAMES = "SELECT * FROM games "
       + "WHERE status='" + Game.STATE.game_complete.toString() + "';";
 
   /***
    * Select all games belonging to a tournament
    * 
    * @param tourneyId
    *          :
    */
   public static final String SELECT_GAMES_IN_TOURNEY_BYID =
     "SELECT * FROM games WHERE tourneyId=?;";
 
   /***
    * Select all games belonging to a tournament, by tournament name
    *
    * @param tourneyName
    *          :
    */
   public static final String SELECT_GAMES_IN_TOURNEY_BYNAME =
       "SELECT * FROM games, tournaments "
           + "WHERE games.tourneyId = tournaments.tourneyId "
           + "AND games.status='" + Game.STATE.game_ready.toString() + "' "
           + "AND tournaments.tourneyName=?;";
 
   /***
    * Get max gameid of all games
    */
   public static final String SELECT_MAX_GAMEID =
     "SELECT MAX(gameId) as maxId FROM games;";
 
   /***
    * Check to see if a gameid has a bootstrap
    * 
    * @param gameId
    *          : The id of the game to check
    */
   public static final String GAME_READY =
     "SELECT hasBootstrap as ready FROM games WHERE gameId=?;";
 
   /***
    * Select the properties given a certain property id
    * 
    * @param propId
    *          : The id of the properties you wish to query
    */
   public static final String SELECT_PROPERTIES_BY_ID =
     "SELECT * FROM properties WHERE gameId=?;";
 
   /***
    * Add properties to the database
    * 
    * @param gameId
    *          : The gameId from which we want to remove properties
    */
   public static final String DELETE_PROPERTIES_BY_GAMEID =
     "DELETE FROM properties WHERE gameId=?;";
 
 
   /***
    * Add properties to the database
    *
    * @param location
    *          : The location key value pair for the properties file as a string
    *          in the database
    * @param startTime
    *          : The startTime key value pair for the properties file as a string
    *          in the database
    * @param gameId
    *          : The gameId that this property file belongs to
    */
   public static final String ADD_PROPERTIES =
       "INSERT INTO properties (jmsUrl,location,startTime,gameId) "
           + "VALUES ('',?,?,?);";
 
 
   /***
    * Update the properties with jmsUrl for sims, this is done as soon as you
    * know the machine you're scheduling on
    * 
    * @param jmsUrl
    *          : The url of the jms connection
    * @param gameId
    *          : The game id of the game you wish to change
    */
   public static final String UPDATE_PROPETIES =
     "UPDATE properties SET jmsUrl=? WHERE gameId=?;";
 
   /***
    * Add pom names and locations
    * 
    * @param uploadingUser
    * @param name
    */
   public static final String ADD_POM =
     "INSERT INTO poms (uploadingUser, name) VALUES (?,?);";
 
   /***
    * Select all poms
    */
   public static final String SELECT_POMS = "SELECT * FROM poms;";
 
   /***
    * Select all machines
    */
   public static final String SELECT_MACHINES =
     "SELECT * FROM machines;";
 
   /***
    * Select machine by id
    * @param machineId
    *          : the id of the machine
    */
   public static final String SELECT_MACHINES_BY_ID =
     "SELECT * FROM machines WHERE machineId=?;";
 
   /***
    * Select machine by id
    * @param machineName
    *          : the id of the machine
    */
   public static final String SELECT_MACHINES_BY_NAME =
       "SELECT * FROM machines WHERE machineName=?;";
   
   
   /***
    * Select servers
    * @param machineId
    *          : the id of the machine
    */
   public static final String SELECT_SERVERS =
     "SELECT * FROM GameServers;";
   
   /***
    * Change a machine's status based on id
    * 
    * @param status
    *          : The new status to change to either "running" or "idle"
    * @param machineId
    *          : The id of the machine to change
    */
   public static final String UPDATE_MACHINE_STATUS_BY_ID =
     "UPDATE machines SET status=? WHERE machineId=?;";
 
   /***
    * Change a machine's status based on name
    * 
    * @param status
    *          : The new status to change to either "running" or "idle"
    * @param machineName
    *          : The name of the machine to change
    * 
    */
   public static final String UPDATE_MACHINE_STATUS_BY_NAME =
     "UPDATE machines SET status=? WHERE machineName=?;";
 
   /***
    * Add a machine into the database, default status is "idle"
    * 
    * @param machineName
    *          : The shorthand name of the machine to be displayed to the users
    *          like "tac04"
    * @param machineUrl
    *          : The fully qualified name of the machine like "tac04.cs.umn.edu"
    */
   public static final String ADD_MACHINE =
     "INSERT INTO machines (machineName, machineUrl, visualizerUrl, "+
     "status, available) VALUES (?,?,?,'" +
     Machine.STATE.idle.toString() + "',false);";
 
   /***
    * Update a machines properties in the database
    * 
    * @param machineName
    *          : The shorthand name of the machine to be displayed to the users
    * @param machineUrl
    *          : The fully qualified name of the machine like "tac04.cs.umn.edu"
    * @param visualizerUrl
    *          : The shorthand name of the machine to be displayed to the users
    * @param machineId
    *          : the machines id in the DB
    */
   public static final String EDIT_MACHINE =
     "UPDATE machines SET machineName=?, machineUrl=?, visualizerUrl=? "
         + "WHERE machineId=?;";
   
   /***
    * Remove a machine from the database by id
    * 
    * @param machineId
    *          : THe id of the machine you wish to remove
    */
   public static final String REMOVE_MACHINE =
     "DELETE FROM machines WHERE machineId=?;";
 
   /***
    * Change a machines availabilty based on name
    * 
    * @param available
    *          : true or false (if true this machine can run sims
    * @param machineId
    *          : the name of the machine
    */
   public static final String UPDATE_MACHINE_AVAILABILITY =
     "UPDATE machines SET available=? WHERE machineId=?;";
 
   /***
    * Return the machineId of the first free machine
    */
   public static final String FIRST_FREE_MACHINE =
       "SELECT * FROM machines WHERE status='"
           + Machine.STATE.idle.toString()
           + "' and available=1 ORDER BY machineId LIMIT 1;";
 
   /***
    * Get the games scheduled for a particular agentType
    * 
    * @param AgentType
    *          :
    */
   public static final String GET_GAMES_FOR_AGENT =
     "SELECT AgentName, AgentType, a.InternalAgentID,b.InternalGameID, GameType,"
         + " ServerNumber"
         + "FROM AgentAdmin a "
         + "JOIN GameLog b ON a.InternalAgentID = b.InternalAgentID"
         + "JOIN GameArchive c ON b.InternalGameID= c.InternalGameID"
         + "WHERE AgentType = ?";
 
   public static final String GET_AGENT_TYPE =
     "SELECT DISTINCT AgentType FROM AgentAdmin;";
 
   
   /**
    * Free the Agent ids that are playing on a server that finished
    * 
    * @param ServerNumber
    */
   public static final String FREE_AGENTS_ON_SERVER = 
      "UPDATE AgentQueue SET IsPlaying=0 WHERE InternalAgentId IN (SELECT * FROM"
          + " (SELECT DISTINCT AgentQueue.InternalAgentId FROM GameLog JOIN"
          + " GameArchive ON GameArchive.InternalGameID = GameLog.InternalGameId"
          + " JOIN AgentQueue ON"
          + " GameLog.InternalAgentID = AgentQueue.InternalAgentId"
          + " WHERE AgentQueue.IsPlaying=1 and GameArchive.ServerNumber=?) AS x)";
 
   
   /***
    * Clear scheduling database to schedule something else
    */
   public static final String CLEAR_SCHEDULE =
     "DELETE FROM AgentAdmin;"
         + " DELETE FROM AgentQueue;"
         + " DELETE FROM GameArchive;"
         + " DELETE FROM GameLog;"
         + " DELETE FROM GameServers;";
 
   /***
    * Select all available locations in the database
    * 
    */
   public static final String SELECT_LOCATIONS =
     "SELECT * FROM locations";
 
   /***
    * Adds a location to the database
    * 
    * @param location
    *          : The name of the location
    * @param fromDate
    *          : The start date of the weather data
    * @param toDate
    *          : The end date of the weather data
    */
   public static final String ADD_LOCATION =
     "INSERT INTO locations (location, fromDate, toDate) VALUES (?,?,?);";
 
   /***
    * Delete a location by id
    * 
    * @param locationId
    *          : The id of the location you wish to remove
    */
   public static final String DELETE_LOCATION =
     "DELETE FROM locations WHERE locationId=?;";
 
   /***
    * Select the minimum date available for a location
    * 
    * @param location
    *          : The location you wish to query
    */
   public static final String SELECT_MIN_DATE =
     "SELECT MIN(fromDate) as minDate WHERE location=?;";
 
   /***
    * Select the maximum date available for a location
    * 
    * @param location
    *          : The location you wish to query
    */
   public static final String SELECT_MAX_DATE =
     "SELECT MAX(toDate) as maxDate WHERE location=?;";
 
 }
