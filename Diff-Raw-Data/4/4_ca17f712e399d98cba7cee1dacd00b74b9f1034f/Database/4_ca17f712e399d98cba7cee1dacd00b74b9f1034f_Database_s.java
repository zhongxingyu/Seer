 package com.powertac.tourney.services;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.TimeZone;
 
 import com.powertac.tourney.beans.Broker;
 import com.powertac.tourney.beans.Game;
 import com.powertac.tourney.beans.Location;
 import com.powertac.tourney.beans.Machine;
 import com.powertac.tourney.beans.Tournament;
 import com.powertac.tourney.constants.Constants;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Service;
 
 @Component("database")
 @Scope("request")
 public class Database
 {
   // Database User container
 
   public static boolean locked = false;
 
   public class User
   {
     private String username;
     private String password;
     private int permission;
     private String salt;
     private String id;
     private String info;
 
     public String getUsername ()
     {
       return username;
     }
 
     public void setUsername (String username)
     {
       this.username = username;
     }
 
     public String getPassword ()
     {
       return password;
     }
 
     public void setPassword (String password)
     {
       this.password = password;
     }
 
     public String getSalt ()
     {
       return salt;
     }
 
     public void setSalt (String salt)
     {
       this.salt = salt;
     }
 
     public String getId ()
     {
       return id;
     }
 
     public void setId (String id)
     {
       this.id = id;
     }
 
     public String getInfo ()
     {
       return info;
     }
 
     public void setInfo (String info)
     {
       this.info = info;
     }
 
     public int getPermission ()
     {
       return permission;
     }
 
     public void setPermission (int permission)
     {
       this.permission = permission;
     }
   }
 
   // Connection Related
   private String dbUrl = "";
   private String database = "";
   private String port = "";
   private String username = "";
   private String password = "";
   private String dbms = "";
 
   // Database Configurations
   private Connection conn = null;
   private PreparedStatement loginStatement = null;
   private PreparedStatement saltStatement = null;
   private PreparedStatement addUserStatement = null;
   private PreparedStatement selectUsersStatement = null;
   private PreparedStatement addBrokerStatement = null;
   private PreparedStatement selectBrokersByUserId = null;
   private PreparedStatement selectBrokerByBrokerId = null;
   private PreparedStatement updateBrokerById = null;
   private PreparedStatement deleteBrokerById = null;
   private PreparedStatement selectPropsById = null;
   private PreparedStatement addPropsById = null;
   private PreparedStatement addPom = null;
   private PreparedStatement selectPoms = null;
 
   SimpleDateFormat dateFormatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
   Properties connectionProps = new Properties();
   Properties prop = new Properties();
 
   public Database ()
   {
 
     try {
       prop.load(Database.class.getClassLoader()
               .getResourceAsStream("/tournament.properties"));
       // System.out.println(prop);
       // Database Connection related properties
       this.setDatabase(prop.getProperty("db.database"));
       this.setDbms(prop.getProperty("db.dbms"));
       this.setPort(prop.getProperty("db.port"));
       this.setDbUrl(prop.getProperty("db.dbUrl"));
       this.setUsername(prop.getProperty("db.username"));
       this.setPassword(prop.getProperty("db.password"));
 
       // System.out.println("Successfully instantiated Database bean!");
 
     }
     catch (FileNotFoundException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
   }
 
   // TODO: Strategy Object find the correct dbms by reflection and call its
   // connection method
   private void checkDb ()
   {
     try {
 
       if (conn == null || conn.isClosed()) {
         // System.out.println("Connection is null");
         if (this.dbms.equalsIgnoreCase("mysql")) {
           // System.out.println("Using mysql as dbms ...");
           try {
             connectionProps.setProperty("user", this.username);
             connectionProps.setProperty("password", this.password);
             Class.forName("com.mysql.jdbc.Driver").newInstance();
             conn =
               DriverManager.getConnection("jdbc:" + this.dbms + "://"
                                                   + this.dbUrl + "/"
                                                   + this.database,
                                           connectionProps);
 
             // System.out.println("Connected Successfully");
           }
           catch (Exception e) {
             System.out.println("Connection Error");
             e.printStackTrace();
           }
         }
         else {
           System.out.println("DBMS: " + this.dbms + " is not supported");
         }
       }
       else {
         // System.out.println("Connection is good");
       }
     }
     catch (SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     }
     // System.out.println("Connection established correctly");
   }
 
   public List<User> getAllUsers () throws SQLException
   {
     checkDb();
     List<User> users = new ArrayList<User>();
 
     if (selectUsersStatement == null || selectUsersStatement.isClosed()) {
       selectUsersStatement = conn.prepareStatement(Constants.SELECT_USERS);
     }
 
     ResultSet rsUsers = selectUsersStatement.executeQuery();
     while (rsUsers.next()) {
       User tmp = new User();
       tmp.setUsername(rsUsers.getString("userName"));
       tmp.setPassword(rsUsers.getString("password"));
       tmp.setId(rsUsers.getString("userId"));
       tmp.setPermission(rsUsers.getInt("permissionId"));
       users.add(tmp);
 
     }
 
     conn.close();
     rsUsers.close();
     selectUsersStatement.close();
 
     return users;
   }
 
   public int updateUser (String username, String key, String value)
   {
     return 0;
 
   }
 
   public int addUser (String username, String password) throws SQLException
   {
     checkDb();
 
     if (addUserStatement == null || addUserStatement.isClosed()) {
       addUserStatement = conn.prepareStatement(Constants.ADD_USER);
       // Use a pool of entropy to secure salts
       String genSalt =
         DigestUtils.md5Hex(Math.random() + (new Date()).toString());
       addUserStatement.setString(1, username);
       addUserStatement.setString(2, genSalt);
       addUserStatement.setString(3, DigestUtils.md5Hex(password + genSalt)); // Store
                                                                              // hashed
                                                                              // and
                                                                              // salted
                                                                              // passwords
       addUserStatement.setInt(4, 3); // Lowest permission level for logged in
                                      // user is 3
 
     }
 
     return addUserStatement.executeUpdate();
   }
 
   public int[] loginUser (String username, String password) throws SQLException
   {
     checkDb();
 
     boolean userExist = false;
     if (saltStatement == null || saltStatement.isClosed()) {
       saltStatement = conn.prepareStatement(Constants.LOGIN_SALT);
       saltStatement.setString(1, username);
     }
 
     ResultSet rsSalt = saltStatement.executeQuery();
     // salt and hash password
     String salt = "";
     String digest = "";
     int userId = -1;
     int permission = 99; // Lowest permission level
     String hashedPass = "";// DigestUtils.md5Hex(password + salt);
     if (rsSalt.next()) {
       digest = rsSalt.getString("password");
       salt = rsSalt.getString("salt");
       permission = rsSalt.getInt("permissionId");
       userId = rsSalt.getInt("userId");
       userExist = true;
     }
     else { // Time resistant attack we need to hash something
       digest = "000000000000000000000000000=";
       salt = "00000000000=";
       userExist = false;
     }
 
     conn.close();
     // TODO: make sure things are inserted correctly in the database;
     if (DigestUtils.md5Hex(password + salt).equalsIgnoreCase(digest)
         && userExist) {
       int[] result = new int[2];
       result[0] = permission;
       result[1] = userId;
       return result;
     }
     else {
       int[] result = new int[2];
       result[0] = -1;
       result[1] = -1;
       return result;
     }
   }
 
   public int addBroker (int userId, String brokerName, String shortDescription)
     throws SQLException
   {
     checkDb();
     com.powertac.tourney.beans.Broker b =
       new com.powertac.tourney.beans.Broker(brokerName, shortDescription);
 
     if (addBrokerStatement == null || addBrokerStatement.isClosed()) {
       addBrokerStatement = conn.prepareStatement(Constants.ADD_BROKER);
     }
 
     addBrokerStatement.setString(1, brokerName);
     addBrokerStatement.setString(2, b.getBrokerAuthToken());
     addBrokerStatement.setString(3, shortDescription);
     addBrokerStatement.setInt(4, userId);
 
     return addBrokerStatement.executeUpdate();
 
   }
 
   public List<Broker> getBrokersByUserId (int userId) throws SQLException
   {
     checkDb();
     List<Broker> brokers = new ArrayList<Broker>();
 
     if (selectBrokersByUserId == null || selectBrokersByUserId.isClosed()) {
       selectBrokersByUserId =
         conn.prepareStatement(Constants.SELECT_BROKERS_BY_USERID);
     }
     selectBrokersByUserId.setInt(1, userId);
     ResultSet rsBrokers = selectBrokersByUserId.executeQuery();
     while (rsBrokers.next()) {
       Broker tmp = new Broker("new");
       tmp.setBrokerAuthToken(rsBrokers.getString("brokerAuth"));
       tmp.setBrokerId(rsBrokers.getInt("brokerId"));
       tmp.setBrokerName(rsBrokers.getString("brokerName"));
       tmp.setShortDescription(rsBrokers.getString("brokerShort"));
       tmp.setNumberInGame(rsBrokers.getInt("numberInGame"));
 
       brokers.add(tmp);
 
     }
     conn.close();
     rsBrokers.close();
     selectBrokersByUserId.close();
 
     return brokers;
 
   }
 
   public int deleteBrokerByBrokerId (int brokerId) throws SQLException
   {
     checkDb();
 
     if (deleteBrokerById == null || deleteBrokerById.isClosed()) {
       deleteBrokerById =
         conn.prepareStatement(Constants.DELETE_BROKER_BY_BROKERID);
     }
     deleteBrokerById.setInt(1, brokerId);
 
     return deleteBrokerById.executeUpdate();
   }
 
   public int updateBrokerByBrokerId (int brokerId, String brokerName,
                                      String brokerAuth, String brokerShort)
     throws SQLException
   {
     checkDb();
 
     if (updateBrokerById == null || updateBrokerById.isClosed()) {
       updateBrokerById =
         conn.prepareStatement(Constants.UPDATE_BROKER_BY_BROKERID);
     }
     updateBrokerById.setString(1, brokerName);
     updateBrokerById.setString(2, brokerAuth);
     updateBrokerById.setString(3, brokerShort);
     updateBrokerById.setInt(4, brokerId);
 
     return updateBrokerById.executeUpdate();
   }
 
   public Broker getBroker (int brokerId) throws SQLException
   {
     checkDb();
     Broker broker = new Broker("new");
 
     PreparedStatement selectBrokerByBrokerId =
       conn.prepareStatement(Constants.SELECT_BROKER_BY_BROKERID);
 
     selectBrokerByBrokerId.setInt(1, brokerId);
 
     ResultSet rsBrokers = selectBrokerByBrokerId.executeQuery();
     if (rsBrokers.next()) {
       broker.setBrokerAuthToken(rsBrokers.getString("brokerAuth"));
       broker.setBrokerId(rsBrokers.getInt("brokerId"));
       broker.setBrokerName(rsBrokers.getString("brokerName"));
       broker.setShortDescription(rsBrokers.getString("brokerShort"));
       broker.setNumberInGame(rsBrokers.getInt("numberInGame"));
     }
     conn.close();
     rsBrokers.close();
     selectBrokerByBrokerId.close();
 
     return broker;
   }
 
   public List<String> getProperties (int gameId) throws SQLException
   {
     checkDb();
     List<String> props = new ArrayList<String>();
 
     if (selectPropsById == null || selectPropsById.isClosed()) {
       selectPropsById =
         conn.prepareStatement(Constants.SELECT_PROPERTIES_BY_ID);
     }
 
     selectPropsById.setInt(1, gameId);
 
     ResultSet rsProps = selectPropsById.executeQuery();
     if (rsProps.next()) {
       props.add(rsProps.getString("location"));
       props.add(rsProps.getString("startTime"));
       props.add(rsProps.getString("jmsUrl"));
       props.add(rsProps.getString("vizQueue"));
     }
     conn.close();
     rsProps.close();
     selectPropsById.close();
 
     return props;
 
   }
 
   public int addProperties (int gameId, String locationKV, String startTimeKV)
     throws SQLException
   {
     checkDb();
 
     PreparedStatement addPropsById =
       conn.prepareStatement(Constants.ADD_PROPERTIES);
 
     addPropsById.setString(1, locationKV);
     addPropsById.setString(2, startTimeKV);
     addPropsById.setInt(3, gameId);
 
     return addPropsById.executeUpdate();
   }
 
   public int updateProperties (int gameId, String jmsUrl, String vizQueue)
     throws SQLException
   {
     checkDb();
     PreparedStatement addPropsById =
       conn.prepareStatement(Constants.UPDATE_PROPETIES);
 
     addPropsById.setInt(3, gameId);
     addPropsById.setString(2, vizQueue);
     addPropsById.setString(1, jmsUrl);
 
     return addPropsById.executeUpdate();
   }
 
   public int addPom (String uploadingUser, String name, String location)
     throws SQLException
   {
     checkDb();
     if (addPom == null || addPom.isClosed()) {
       addPom = conn.prepareStatement(Constants.ADD_POM);
     }
 
     addPom.setString(1, uploadingUser);
     addPom.setString(2, name);
     addPom.setString(3, location);
 
     return addPom.executeUpdate();
   }
 
   public class Pom
   {
     private String name;
     private String location;
     private String uploadingUser;
 
     public String getName ()
     {
       return name;
     }
 
     public void setName (String name)
     {
       this.name = name;
     }
 
     public String getLocation ()
     {
       return location;
     }
 
     public void setLocation (String location)
     {
       this.location = location;
     }
 
     public String getUploadingUser ()
     {
       return uploadingUser;
     }
 
     public void setUploadingUser (String uploadingUser)
     {
       this.uploadingUser = uploadingUser;
     }
 
   }
 
   public List<Pom> getPoms () throws SQLException
   {
     checkDb();
     List<Pom> poms = new ArrayList<Pom>();
 
     if (selectPoms == null || selectPoms.isClosed()) {
       selectPoms = conn.prepareStatement(Constants.SELECT_POMS);
     }
 
     ResultSet rsPoms = selectPoms.executeQuery();
     while (rsPoms.next()) {
       Pom tmp = new Pom();
       tmp.setLocation(rsPoms.getString("location"));
       tmp.setName(rsPoms.getString("name"));
       tmp.setUploadingUser(rsPoms.getString("uploadingUser"));
 
       poms.add(tmp);
     }
 
     conn.close();
     rsPoms.close();
     selectPoms.close();
 
     return poms;
 
   }
 
   public int addTournament (String tourneyName, boolean openRegistration,
                             int maxGames, Date startTime, String type,
                             String pomUrl, String locations, int maxBrokers)
     throws SQLException
   {
     checkDb();
     dateFormatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
     PreparedStatement addTournament =
       conn.prepareStatement(Constants.ADD_TOURNAMENT);
     addTournament.setString(1, tourneyName);
     addTournament.setString(2, dateFormatUTC.format(startTime));
     addTournament.setBoolean(3, openRegistration);
     addTournament.setInt(4, maxGames);
     addTournament.setString(5, type);
     addTournament.setString(6, pomUrl);
     addTournament.setString(7, locations);
     addTournament.setInt(8, maxBrokers);
 
     return addTournament.executeUpdate();
 
   }
 
   public List<Tournament> getTournaments (String status) throws SQLException
   {
     checkDb();
     List<Tournament> ts = new ArrayList<Tournament>();
     PreparedStatement selectAllTournaments =
       conn.prepareStatement(Constants.SELECT_TOURNAMENTS);
 
     selectAllTournaments.setString(1, status);
 
     ResultSet rsTs = selectAllTournaments.executeQuery();
 
     while (rsTs.next()) {
       Tournament tmp = new Tournament(rsTs);
       ts.add(tmp);
     }
     conn.close();
     rsTs.close();
     selectAllTournaments.close();
 
     return ts;
   }
 
   public Tournament getTournamentById (int tourneyId) throws SQLException
   {
     checkDb();
     Tournament ts = new Tournament();
     PreparedStatement selectTournament =
       conn.prepareStatement(Constants.SELECT_TOURNAMENT_BYID);
 
     selectTournament.setInt(1, tourneyId);
 
     ResultSet rsTs = selectTournament.executeQuery();
 
     if (rsTs.next()) {
       Tournament tmp = new Tournament(rsTs);
       ts = tmp;
     }
     conn.close();
     rsTs.close();
     selectTournament.close();
 
     return ts;
   }
 
   public Tournament getTournamentByType (String type) throws SQLException
   {
     checkDb();
     Tournament ts = new Tournament();
     PreparedStatement selectTournament =
       conn.prepareStatement(Constants.SELECT_TOURNAMENT_BYTYPE);
 
     selectTournament.setString(1, type);
 
     ResultSet rsTs = selectTournament.executeQuery();
 
     if (rsTs.next()) {
       Tournament tmp = new Tournament(rsTs);
       ts = tmp;
     }
 
     conn.close();
     rsTs.close();
     selectTournament.close();
 
     return ts;
   }
 
   public Tournament getTournamentByGameId (int gameId) throws SQLException
   {
     checkDb();
     Tournament ts = new Tournament();
     PreparedStatement selectTournament =
       conn.prepareStatement(Constants.SELECT_TOURNAMENT_BYGAMEID);
 
     selectTournament.setInt(1, gameId);
 
     ResultSet rsTs = selectTournament.executeQuery();
 
     if (rsTs.next()) {
       Tournament tmp = new Tournament(rsTs);
       ts = tmp;
     }
 
     conn.close();
     rsTs.close();
     selectTournament.close();
 
     return ts;
   }
 
   public List<Game> getGamesInTourney (int tourneyId) throws SQLException
   {
     checkDb();
     List<Game> gs = new ArrayList<Game>();
     PreparedStatement selectAllGames =
       conn.prepareStatement(Constants.SELECT_GAMES_IN_TOURNEY);
     selectAllGames.setInt(1, tourneyId);
 
     ResultSet rsGs = selectAllGames.executeQuery();
 
     while (rsGs.next()) {
       Game tmp = new Game(rsGs);
       gs.add(tmp);
     }
     conn.close();
     rsGs.close();
     selectAllGames.close();
 
     return gs;
   }
 
   public int updateTournamentStatus (int tourneyId) throws SQLException
   {
     checkDb();
     PreparedStatement updateStatus =
       conn.prepareStatement(Constants.UPDATE_TOURNAMENT_STATUS_BYID);
     updateStatus.setString(1, "in-progress");
     updateStatus.setInt(2, tourneyId);
 
     return updateStatus.executeUpdate();
   }
 
   public int registerBroker (int tourneyId, int brokerId) throws SQLException
   {
     checkDb();
     PreparedStatement register =
       conn.prepareStatement(Constants.REGISTER_BROKER);
     register.setInt(1, tourneyId);
     register.setInt(2, brokerId);
     return register.executeUpdate();
   }
 
   public int unregisterBroker (int tourneyId, int brokerId) throws SQLException
   {
     checkDb();
     PreparedStatement register =
       conn.prepareStatement(Constants.UNREGISTER_BROKER);
     register.setInt(1, tourneyId);
     register.setInt(2, brokerId);
     return register.executeUpdate();
   }
 
   public boolean isRegistered (int tourneyId, int brokerId) throws SQLException
   {
     checkDb();
     PreparedStatement register = conn.prepareStatement(Constants.REGISTERED);
     register.setInt(1, tourneyId);
     register.setInt(2, brokerId);
     ResultSet rs = register.executeQuery();
     boolean result = false;
     if (rs.next()) {
       result = rs.getBoolean("registered");
     }
 
     rs.close();
     register.close();
 
     return result;
   }
 
   public int getMaxGameId () throws SQLException
   {
     checkDb();
     int id = 0;
     PreparedStatement selectMaxId =
       conn.prepareStatement(Constants.SELECT_MAX_GAMEID);
 
     ResultSet rsId = selectMaxId.executeQuery();
     if (rsId.next()) {
       id = rsId.getInt("maxId");
     }
 
     selectMaxId.close();
     rsId.close();
 
     return id;
 
   }
 
   public int getMaxTourneyId () throws SQLException
   {
     checkDb();
     int id = 0;
     PreparedStatement selectMaxId =
       conn.prepareStatement(Constants.SELECT_MAX_TOURNAMENTID);
 
     ResultSet rsId = selectMaxId.executeQuery();
     if (rsId.next()) {
       id = rsId.getInt("maxId");
     }
 
     selectMaxId.close();
     rsId.close();
 
     return id;
   }
 
   public List<Broker> getBrokersRegistered (int tourneyId) throws SQLException
   {
     checkDb();
     List<Broker> result = new ArrayList<Broker>();
 
     PreparedStatement selectBrokers =
       conn.prepareStatement(Constants.GET_BROKERS_BYTOURNAMENTID);
     selectBrokers.setInt(1, tourneyId);
 
     ResultSet rsB = selectBrokers.executeQuery();
 
     while (rsB.next()) {
       Broker tmp = new Broker("new");
       tmp.setBrokerAuthToken(rsB.getString("brokerAuth"));
       tmp.setBrokerId(rsB.getInt("brokerId"));
       tmp.setBrokerName(rsB.getString("brokerName"));
       tmp.setShortDescription(rsB.getString("brokerShort"));
       tmp.setNumberInGame(rsB.getInt("numberInGame"));
 
       result.add(tmp);
     }
     conn.close();
     rsB.close();
     selectBrokers.close();
     return result;
   }
 
   public int getNumberBrokersRegistered (int tourneyId) throws SQLException
   {
     checkDb();
     int result = 0;
 
     PreparedStatement selectBrokers =
       conn.prepareStatement(Constants.GET_NUMBER_REGISTERED_BYTOURNAMENTID);
     selectBrokers.setInt(1, tourneyId);
 
     ResultSet rsB = selectBrokers.executeQuery();
 
     if (rsB.next()) {
       result = rsB.getInt("numRegistered");
     }
     conn.close();
     rsB.close();
     selectBrokers.close();
 
     return result;
 
   }
 
   public List<Game> getGames () throws SQLException
   {
     checkDb();
     List<Game> gs = new ArrayList<Game>();
     PreparedStatement selectAllGames =
       conn.prepareStatement(Constants.SELECT_GAME);
 
     ResultSet rsGs = selectAllGames.executeQuery();
 
     while (rsGs.next()) {
       Game tmp = new Game(rsGs);
       gs.add(tmp);
     }
     conn.close();
     rsGs.close();
     selectAllGames.close();
 
     return gs;
   }
 
   public List<Game> getStartableGames () throws SQLException
   {
     checkDb();
     List<Game> games = new ArrayList<Game>();
 
     PreparedStatement getGames =
       conn.prepareStatement(Constants.GET_RUNNABLE_GAMES);
 
     ResultSet rsGs = getGames.executeQuery();
 
    System.out.println("Parsing games");
     while (rsGs.next()) {
       Game tmp = new Game(rsGs);
       games.add(tmp);
     }
 
     conn.close();
     rsGs.close();
     getGames.close();
 
     return games;
 
   }
 
   public List<Game> getWaitingGames () throws SQLException
   {
     List<Game> games = new ArrayList<Game>();
 
     PreparedStatement getGames =
       conn.prepareStatement(Constants.GET_PENDING_GAMES);
 
     ResultSet rsGs = getGames.executeQuery();
 
     while (rsGs.next()) {
       Game tmp = new Game(rsGs);
       games.add(tmp);
     }
 
     conn.close();
     rsGs.close();
     getGames.close();
 
     return games;
   }
 
   public int addGame (String gameName, int tourneyId, int maxBrokers,
                       Date startTime) throws SQLException
   {
     checkDb();
     java.text.SimpleDateFormat sdf =
       new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
     PreparedStatement insertGame = conn.prepareStatement(Constants.ADD_GAME);
 
     insertGame.setString(1, gameName);
     insertGame.setInt(2, tourneyId);
     // insertGame.setInt(3, machineId);
     insertGame.setInt(3, maxBrokers);
     insertGame.setString(4, dateFormatUTC.format(startTime));
     // insertGame.setString(4, properitesUrl);
 
     return insertGame.executeUpdate();
   }
 
   public Game getGame (int gameId) throws SQLException
   {
     checkDb();
     PreparedStatement selectAllGames =
       conn.prepareStatement(Constants.SELECT_GAMEBYID);
     selectAllGames.setInt(1, gameId);
 
     ResultSet rsGs = selectAllGames.executeQuery();
 
     Game tmp = new Game();
     if (rsGs.next()) {
       tmp = new Game(rsGs);
 
     }
     rsGs.close();
     selectAllGames.close();
 
     return tmp;
   }
 
   public int addBrokerToGame (int gameId, Broker b) throws SQLException
   {
     checkDb();
     PreparedStatement addBrokerToGame =
       conn.prepareStatement(Constants.ADD_BROKER_TO_GAME);
 
     addBrokerToGame.setInt(1, gameId);
     addBrokerToGame.setInt(2, b.getBrokerId());
     addBrokerToGame.setString(3, b.getBrokerAuthToken());
     addBrokerToGame.setString(4, b.getBrokerName());
 
     return addBrokerToGame.executeUpdate();
   }
 
   public List<Broker> getBrokersInGame (int gameId) throws SQLException
   {
     checkDb();
     List<Broker> brokers = new ArrayList<Broker>();
     PreparedStatement getBrokers =
       conn.prepareStatement(Constants.GET_BROKERS_INGAME);
 
     getBrokers.setInt(1, gameId);
 
     ResultSet rs = getBrokers.executeQuery();
     while (rs.next()) {
       Broker tmp = new Broker("new");
       tmp.setBrokerAuthToken(rs.getString("brokerAuth"));
       tmp.setBrokerId(rs.getInt("brokerId"));
       tmp.setBrokerName(rs.getString("brokerName"));
       tmp.setShortDescription(rs.getString("brokerShort"));
       tmp.setNumberInGame(rs.getInt("numberInGame"));
 
       brokers.add(tmp);
     }
     conn.close();
     rs.close();
     getBrokers.close();
 
     return brokers;
   }
 
   public List<Broker> getBrokersInTournament (int tourneyId)
     throws SQLException
   {
     checkDb();
     List<Broker> brokers = new ArrayList<Broker>();
     PreparedStatement getBrokers =
       conn.prepareStatement(Constants.GET_BROKERS_INTOURNAMENT);
 
     getBrokers.setInt(1, tourneyId);
 
     ResultSet rs = getBrokers.executeQuery();
     while (rs.next()) {
       Broker tmp = new Broker("new");
       tmp.setBrokerAuthToken(rs.getString("brokerAuth"));
       tmp.setBrokerId(rs.getInt("brokerId"));
       tmp.setBrokerName(rs.getString("brokerName"));
       tmp.setShortDescription(rs.getString("brokerShort"));
       tmp.setNumberInGame(rs.getInt("numberInGame"));
 
       brokers.add(tmp);
     }
     conn.close();
     getBrokers.close();
     rs.close();
 
     return brokers;
   }
 
   public boolean isGameReady (int gameId) throws SQLException
   {
     checkDb();
 
     PreparedStatement hasBootstrap =
       conn.prepareStatement(Constants.GAME_READY);
 
     hasBootstrap.setInt(1, gameId);
 
     ResultSet rs = hasBootstrap.executeQuery();
 
     boolean result = false; // fail on
     if (rs.next()) {
       result = rs.getBoolean("ready");
     }
 
     rs.close();
     hasBootstrap.close();
 
     return result;
   }
 
   public int updateGameStatusById (int gameId, String status)
     throws SQLException
   {
     checkDb();
     PreparedStatement updateGame = conn.prepareStatement(Constants.UPDATE_GAME);
 
     updateGame.setInt(2, gameId);
     updateGame.setString(1, status);
 
     return updateGame.executeUpdate();
   }
 
   public int updateGameMachine (int gameId, int machineId) throws SQLException
   {
     checkDb();
     PreparedStatement updateGame =
       conn.prepareStatement(Constants.UPDATE_GAME_MACHINE);
 
     updateGame.setInt(2, gameId);
     updateGame.setInt(1, machineId);
 
     return updateGame.executeUpdate();
   }
 
   public int updateGameFreeMachine (int gameId) throws SQLException
   {
     checkDb();
     PreparedStatement updateGame =
       conn.prepareStatement(Constants.UPDATE_GAME_FREE_MACHINE);
 
     updateGame.setInt(1, gameId);
 
     return updateGame.executeUpdate();
   }
 
   public int updateGameFreeBrokers (int gameId) throws SQLException
   {
     checkDb();
     PreparedStatement updateGame =
       conn.prepareStatement(Constants.UPDATE_GAME_FREE_BROKERS);
 
     updateGame.setInt(1, gameId);
 
     return updateGame.executeUpdate();
   }
 
   public int updateGameJmsUrlById (int gameId, String jmsUrl)
     throws SQLException
   {
     checkDb();
     PreparedStatement updateGame =
       conn.prepareStatement(Constants.UPDATE_GAME_JMSURL);
 
     updateGame.setInt(2, gameId);
     updateGame.setString(1, jmsUrl);
 
     return updateGame.executeUpdate();
   }
 
   public int updateGameBootstrapById (int gameId, String bootstrapUrl)
     throws SQLException
   {
     checkDb();
     PreparedStatement updateBoot =
       conn.prepareStatement(Constants.UPDATE_GAME_BOOTSTRAP);
 
     updateBoot.setInt(2, gameId);
     updateBoot.setString(1, bootstrapUrl);
 
     return updateBoot.executeUpdate();
   }
 
   public int updateGamePropertiesById (int gameId) throws SQLException
   {
     checkDb();
     PreparedStatement updateProps =
       conn.prepareStatement(Constants.UPDATE_GAME_PROPERTIES);
 
     String hostip = "http://";
 
     try {
       InetAddress thisIp = InetAddress.getLocalHost();
       hostip += thisIp.getHostAddress() + ":8080";
     }
     catch (UnknownHostException e2) {
       // TODO Auto-generated catch block
       e2.printStackTrace();
     }
 
     updateProps
             .setString(1, hostip
                           + "/TournamentScheduler/faces/properties.jsp?gameId="
                           + String.valueOf(gameId));
     updateProps.setInt(2, gameId);
 
     return updateProps.executeUpdate();
   }
 
   public int updateGameViz (int gameId, String vizUrl) throws SQLException
   {
     checkDb();
     PreparedStatement updateViz =
       conn.prepareStatement(Constants.UPDATE_GAME_VIZ);
 
     updateViz.setString(1, vizUrl);
     updateViz.setInt(2, gameId);
 
     return updateViz.executeUpdate();
   }
 
   public List<Machine> getMachines () throws SQLException
   {
     checkDb();
     List<Machine> machines = new ArrayList<Machine>();
 
     PreparedStatement selectMachines =
       conn.prepareStatement(Constants.SELECT_MACHINES);
 
     ResultSet rsMachines = selectMachines.executeQuery();
 
     while (rsMachines.next()) {
       Machine tmp = new Machine(rsMachines);
       machines.add(tmp);
     }
     conn.close();
     rsMachines.close();
     selectMachines.close();
 
     return machines;
   }
 
   public int setMachineAvailable (int machineId, boolean isAvailable)
     throws SQLException
   {
     checkDb();
 
     PreparedStatement updateMachine =
       conn.prepareStatement(Constants.UPDATE_MACHINE_AVAILABILITY);
 
     updateMachine.setBoolean(1, isAvailable);
     updateMachine.setInt(2, machineId);
 
     return updateMachine.executeUpdate();
 
   }
 
   public int setMachineStatus (int machineId, String status)
     throws SQLException
   {
     checkDb();
 
     PreparedStatement updateMachine =
       conn.prepareStatement(Constants.UPDATE_MACHINE_STATUS_BY_ID);
 
     updateMachine.setString(1, status);
     updateMachine.setInt(2, machineId);
 
     return updateMachine.executeUpdate();
 
   }
 
   public int addMachine (String machineName, String machineUrl,
                          String visualizerUrl, String visualizerQueue)
     throws SQLException
   {
     checkDb();
 
     PreparedStatement addMachine = conn.prepareStatement(Constants.ADD_MACHINE);
     addMachine.setString(1, machineName);
     addMachine.setString(2, machineUrl);
     addMachine.setString(3, visualizerUrl);
     addMachine.setString(4, visualizerQueue);
 
     return addMachine.executeUpdate();
   }
 
   public int deleteMachine (int machineId) throws SQLException
   {
     checkDb();
 
     PreparedStatement deleteMachine =
       conn.prepareStatement(Constants.REMOVE_MACHINE);
     deleteMachine.setInt(1, machineId);
 
     return deleteMachine.executeUpdate();
   }
 
   public List<Location> getLocations () throws SQLException
   {
     checkDb();
     List<Location> locations = new ArrayList<Location>();
     PreparedStatement selectLocations =
       conn.prepareStatement(Constants.SELECT_LOCATIONS);
 
     ResultSet rsLocations = selectLocations.executeQuery();
 
     while (rsLocations.next()) {
       Location tmp = new Location();
       tmp.setLocationId(rsLocations.getInt("locationId"));
       tmp.setName(rsLocations.getString("location"));
       Calendar fromDate = Calendar.getInstance();
       fromDate.setTimeInMillis(rsLocations.getDate("fromDate").getTime());
       tmp.setFromDate(fromDate.getTime());
       Calendar toDate = Calendar.getInstance();
       toDate.setTimeInMillis(rsLocations.getDate("toDate").getTime());
       tmp.setToDate(toDate.getTime());
 
       locations.add(tmp);
 
     }
     conn.close();
     rsLocations.close();
     selectLocations.close();
 
     return locations;
 
   }
 
   public int deleteLocation (int locationId) throws SQLException
   {
     checkDb();
     PreparedStatement deleteLocation =
       conn.prepareStatement(Constants.DELETE_LOCATION);
 
     deleteLocation.setInt(1, locationId);
 
     return deleteLocation.executeUpdate();
   }
 
   public int addLocation (String location, Date newLocationStartTime,
                           Date newLocationEndTime) throws SQLException
   {
     checkDb();
     PreparedStatement addLocations =
       conn.prepareStatement(Constants.ADD_LOCATION);
     addLocations.setString(1, location);
     addLocations.setDate(2, new java.sql.Date(newLocationStartTime.getTime()));
     addLocations.setDate(3, new java.sql.Date(newLocationEndTime.getTime()));
 
     return addLocations.executeUpdate();
   }
 
   public Date selectMinDate (List<Location> locations) throws SQLException
   {
     checkDb();
     PreparedStatement minDate =
       conn.prepareStatement(Constants.SELECT_MIN_DATE);
     Date min = new Date();
 
     for (Location l: locations) {
       minDate.setString(1, l.getName());
       ResultSet rs = minDate.executeQuery();
       if (rs.next()) {
         if (rs.getDate("minDate").before(min)) {
           min = rs.getDate("minDate");
         }
       }
     }
 
     minDate.close();
 
     return min;
   }
 
   public Date selectMaxDate (List<Location> locations) throws SQLException
   {
     checkDb();
     PreparedStatement minDate =
       conn.prepareStatement(Constants.SELECT_MAX_DATE);
     Date max = new Date();
 
     for (Location l: locations) {
       minDate.setString(1, l.getName());
       ResultSet rs = minDate.executeQuery();
       if (rs.next()) {
         if (rs.getDate("maxDate").after(max)) {
           max = rs.getDate("maxDate");
         }
       }
     }
     minDate.close();
 
     return max;
   }
 
   public int startTrans () throws SQLException
   {
     checkDb();
 
     PreparedStatement trans = conn.prepareStatement(Constants.START_TRANS);
     trans.execute();
 
     return 0;
   }
 
   public int commitTrans () throws SQLException
   {
     checkDb();
     PreparedStatement trans = conn.prepareCall(Constants.COMMIT_TRANS);
     trans.execute();
     return 0;
 
   }
 
   public int abortTrans () throws SQLException
   {
     checkDb();
     PreparedStatement trans = conn.prepareCall(Constants.ABORT_TRANS);
     trans.execute();
 
     return 0;
 
   }
 
   public void closeConnection () throws SQLException
   {
     conn.close();
   }
 
   public String getDbUrl ()
   {
     return dbUrl;
   }
 
   public void setDbUrl (String dbUrl)
   {
     this.dbUrl = dbUrl;
   }
 
   public String getDatabase ()
   {
     return database;
   }
 
   public void setDatabase (String database)
   {
     this.database = database;
   }
 
   public String getPort ()
   {
     return port;
   }
 
   public void setPort (String port)
   {
     this.port = port;
   }
 
   public String getUsername ()
   {
     return username;
   }
 
   public void setUsername (String username)
   {
     this.username = username;
   }
 
   public String getPassword ()
   {
     return password;
   }
 
   public void setPassword (String password)
   {
     this.password = password;
   }
 
   public String getDbms ()
   {
     return dbms;
   }
 
   public void setDbms (String dbms)
   {
     this.dbms = dbms;
   }
 
 }
