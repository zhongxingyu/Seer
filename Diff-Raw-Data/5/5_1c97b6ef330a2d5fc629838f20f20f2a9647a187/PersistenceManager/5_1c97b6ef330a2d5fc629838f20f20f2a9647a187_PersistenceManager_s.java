 package database;
 
 import ServerCom.RemoteTalker;
 import data.Monster;
 import data.*;
 import java.security.SecureRandom;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.json.JSONException;
 
 /**
  *
  * @author sjk4
  */
 public class PersistenceManager {
     private final String dbname = "MonsterMash";
     private final String dbuser = "root";
     private final String dbpassword = "root";
     private final String dbhost = "localhost";
     private final String dbport = "1527";
     
     private Connection connection;
     private String error;
     private RemoteTalker remote;
     
     public PersistenceManager(){
         remote = new RemoteTalker();
         String driver = "org.apache.derby.jdbc.EmbeddedDriver";
         String connectionURL = "jdbc:derby://"+dbhost+":"+dbport+"/"+dbname+";create=true;user="+dbuser+";password="+dbpassword;
         try {
             Class.forName(driver);
         } catch (java.lang.ClassNotFoundException e) {
             e.printStackTrace();
         }
         try {
             connection = DriverManager.getConnection(connectionURL);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Generates random string, which will be used as IDs for monsters/players etc.
      * @param length length of a string
      * @return random string with specified length
      */
     private String randomString(int length){
         Random random = new SecureRandom();
         String letters = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ0123456789";
         String pw = "";
         for (int i=0; i<length; i++){
             int index = (int)(random.nextDouble()*letters.length());
             pw += letters.substring(index, index+1);
         }
         return pw;
     }
     
     /**
      * Checks if there is an account with specified email address.
      * @param email user's email address
      * @return true if account exists
      */
     public boolean accountExists(String userID){
         int count = 0;
         try{
             Statement stmt = connection.createStatement();
             stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT count(\"id\") FROM \"Player\" WHERE \"id\" = '"+userID+"'");
             results.next();
             count = results.getInt(1);
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         // TODO: Check other servers! (SERVER<->SERVER)
         if(count > 0){
             return true;
         }
         return false;
     }
     
     /**
      * Store player with monsters and notifications in DB.
      * @param email user's email address
      * @param password encrypted password (md5)
      * @param money start amount of money
      * @return true when account created successfully
      */
     public void storePlayer(Player p){
         try{
             Statement stmt = connection.createStatement();
             // Add player to PLAYER table
             stmt.execute("INSERT INTO \"Player\" (\"id\", \"username\", \"password\", \"money\", \"server_id\") VALUES ('"+p.getUserID()+"', '"+p.getUsername()+"', '"+p.getPassword()+"', "+p.getMoney()+", "+p.getServerID()+")");
             stmt.close();
             // Save first notification in DB
             this.storeNotifications(p);
             // Save initial monster in DB
             this.storeMonsters(p);
         }catch(SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
     }
     
     /**
      * Stores all notifications with ID = 0 (which haven't been saved in DB)
      * @param p object of Player class
      */
     public void storeNotifications(Player p){
         for(Notification n: p.getNotifications()){
             // If ID equals 0, notifications hasn't been saved in DB
             if(n.getId() == 0){
                 try{
                     Statement stmt = connection.createStatement();
                     Timestamp ts = new Timestamp(n.getTimeSent().getTime());
                     // Insert notification into DB
                     stmt.execute("INSERT INTO \"Notification\" (\"short_message\", \"long_message\", \"player_id\", \"time\") VALUES ('"+n.getShortText()+"', '"+n.getLongText()+"', '"+p.getUserID()+"', '"+ts.toString()+"')", Statement.RETURN_GENERATED_KEYS);
                     // Set ID of notification
                     ResultSet rs = stmt.getGeneratedKeys();
                     if(rs != null && rs.next()){
                         n.setId(rs.getInt(1));
                     }
                 }catch(SQLException sqlExcept){
                     System.err.println(sqlExcept.getMessage());
                     this.error = sqlExcept.getMessage();
                 }
             }
         }
     }
     
     /**
      * Stores all monsters with ID = 0 (which haven't been saved in DB)
      * @param p object of Player class
      */
     public void storeMonsters(Player p){
         for(Monster m: p.getMonsters()){
             // If ID equals 0, monster hasn't been saved in DB
             if(m.getId().equals("0")){ 
                 try{
                     Statement stmt = connection.createStatement();
                     // Insert monster into DB
                     m.setId(this.randomString(16));
                     String query = "INSERT INTO \"Monster\" (\"id\", \"name\", \"dob\", \"dod\", \"base_strength\", \"current_strength\", \"base_defence\", \"current_defence\", \"base_health\", \"current_health\", \"fertility\", \"user_id\", \"sale_offer\", \"breed_offer\") VALUES ('"+m.getId()+"', '"+m.getName()+"', "+m.getDob().getTime()+", "+m.getDod().getTime()+", "+m.getBaseStrength()+", "+m.getCurrentStrength()+", "+m.getBaseDefence()+", "+m.getCurrentDefence()+", "+m.getBaseHealth()+", "+m.getCurrentHealth()+", "+m.getFertility()+", '"+p.getUserID()+"', "+m.getSaleOffer()+", "+m.getBreedOffer()+")";
                     stmt.execute(query);
                 }catch(SQLException sqlExcept){
                     System.err.println("Adding monster to DB error:\n"+sqlExcept.getMessage());
                     this.error = sqlExcept.getMessage();
                 }
             }
         }
     }
     
     /**
      * Gets player object of DB, returns null when user doesn't exist
      * @param email user's email address
      * @param password encrypted password using MD5
      * @return object of player class with all monsters, notifications and friends
      */
     public Player doLogin(String email, String password){
         Player selected = null;
         try{
             Statement stmt = connection.createStatement();
             ResultSet r = stmt.executeQuery("SELECT * FROM \"Player\" WHERE \"id\" = '"+email+"' AND \"password\" = '"+password+"'");
             r.next();
             selected = new Player(r.getString("id"), r.getString("username"), r.getString("password"), r.getInt("money"), this.getFriendList(r.getString("id")), this.getNotificationList(r.getString("id")), this.getMonsterList(r.getString("id")), r.getInt("server_id"));
             r.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         return selected;
     }
     
     /**
      * Gets all friends from DB of specified player
      * @param playerID ID of player
      * @return list of friends and friend requests
      */
     public ArrayList<Player> getFriendList(String playerID){
         ArrayList<Player> friendList = new ArrayList<Player>();
         try{
             Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery("SELECT * FROM \"Friendship\" WHERE (\"sender_id\" = '"+playerID+"' OR \"receiver_id\" = '"+playerID+"') AND \"confirmed\" = 'Y'");
             while(result.next()){
                 if(result.getString("sender_id").equals(playerID+"")){
                     // Sender String id, String name, int serverID
                     friendList.add(new Player(result.getString("receiver_id"), this.getPlayerUsername(result.getString("receiver_id"), result.getInt("receiver_server_id")), result.getInt("receiver_server_id")));
                 }else{
                     // Receiver
                     friendList.add(new Player(result.getString("sender_id"), this.getPlayerUsername(result.getString("sender_id"), result.getInt("sender_server_id")), result.getInt("sender_server_id")));
                 }
             }
             result.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             System.err.println("Selecting friendships from DB error:\n"+sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
         
         return friendList;
     }
     
     public ArrayList<FightRequest> getFightRequests(String playerID)
     {
         ArrayList<FightRequest> fightRequests = new ArrayList<FightRequest>();
         
         try{
             Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery("SELECT * FROM \"Fight_request\" WHERE (\"receiver_id\" = '"+playerID+"')");
             while(result.next()){
                 if(result.getString("receiver_id").equals(playerID+"")){
                     // Sender String id, String name, int serverID
                     fightRequests.add(new FightRequest(result.getString("sender_id"), result.getString("receiver_id"), result.getString("id"), result.getString("sender_monster_id"), result.getString("receiver_monster_id"), result.getInt("sender_server_id"), result.getInt("receiver_server_id")));
                     //friendList.add(new Player(result.getString("receiver_id"), this.getPlayerUsername(result.getString("receiver_id"), result.getInt("receiver_server_id")), result.getInt("receiver_server_id")));
                 }
             }
             result.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             System.err.println("Selecting fight requests from DB error:\n"+sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
         return fightRequests;
     }
     
     /**
      * Gets all notifications from DB ordered by date.
      * @param playerID id of player
      * @return list of notifications
      */
     public ArrayList<Notification> getNotificationList(String playerID){
         ArrayList<Notification> notificationList = new ArrayList<Notification>();
         try{
             Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery("SELECT * FROM \"Notification\" WHERE \"player_id\" = '"+playerID+"' ORDER BY \"time\" DESC");
             while(result.next()){
                 notificationList.add(new Notification(result.getInt("id"), result.getString("short_message"), result.getString("long_message"), result.getDate("time")));
             }
             result.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             System.err.println(sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
         return notificationList;
     }
     
     /**
      * Gets a list of monsters owned by this player
      * @param playerID id of player
      * @return list of monsters owned
      */
     public ArrayList<Monster> getMonsterList(String playerID){
         ArrayList<Monster> monsters = new ArrayList<Monster>();
         try{
             Statement stmt = connection.createStatement();
             ResultSet r = stmt.executeQuery("SELECT * FROM \"Monster\" WHERE \"user_id\" = '"+playerID+"'");
             while(r.next()){
                 monsters.add(new Monster(r.getString("id"), r.getString("name"), new Date(r.getLong("dob")*1000), new Date(r.getLong("dod")*1000), r.getDouble("base_strength"), r.getDouble("current_strength"), r.getDouble("base_defence"), r.getDouble("current_defence"), r.getDouble("base_health"), r.getDouble("current_health"), r.getFloat("fertility"), r.getString("user_id"), r.getInt("sale_offer"), r.getInt("breed_offer")));
             }
             r.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             System.err.println(sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
         return monsters;
     }
     
     /**
      * Gets player from DB with all monsters, notifications, friends.
      * @param id id of player
      * @return object of Player class, null when player doesn't exist
      */
     public Player getPlayer(String userID){
         Player selected = null;
         try{
             Statement stmt = connection.createStatement();
             ResultSet r = stmt.executeQuery("SELECT * FROM \"Player\" WHERE \"id\" = '"+userID+"'");
             r.next();
             selected = new Player(r.getString("id"), r.getString("username"), r.getString("password"), r.getInt("money"), this.getFriendList(r.getString("id")), this.getNotificationList(r.getString("id")), this.getMonsterList(r.getString("id")), r.getInt("server_id"));
             r.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         return selected;
     }
     
     /**
      * Returns player id at index 0 and player server id at index 1
      * @param userID player's userID
      * @return player name (index 0) player server id (index 1)
      */
     public int getPlayerServerID(String userID){
         int serverID = 0;
         try{
             Statement stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT count(\"id\") FROM \"Player\" WHERE \"id\" = '"+userID+"'");
             results.next();
             
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             System.err.println(sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
         if(serverID == 0){
             Player user = remote.findUser(userID);
             serverID = user.getServerID();
         }
         return serverID;
     }
     
     /**
      * Checks if friend request has been already sent.
      * @param playerOne userID of first player
      * @param playerTwo userID of second player
      * @return true when such a request was sent
      */
     public boolean isFriendRequestSent(String playerOne, String playerTwo){
         int count = 0;
         try{
             Statement stmt = connection.createStatement();
             stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT \"id\" FROM \"Friendship\" WHERE (\"sender_id\" = '"+playerOne+"' AND \"receiver_id\" = '"+playerTwo+"') OR (\"sender_id\" = '"+playerTwo+"' AND \"receiver_id\" = '"+playerOne+"')");
             while(results.next()){
                 count++;
             }
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
             count = -1;
         }
         if(count > 0){
             return true;
         }else{
             return false;
         }
     }
     
     /**
      * Adds new record to "Friendship" table and if user is on different server, sends JSON object.
      * @param senderID userID of sender (always our server)
      * @param receiverID userID of reciver
      * @param receiverServerID  server ID of receiver
      */
     public void sendFriendRequest(String senderID, String receiverID, int receiverServerID){
         if(receiverServerID == CONFIG.OUR_SERVER){
             try{
                 Statement stmt = connection.createStatement();
                 String id = this.randomString(16);
                 stmt.execute("INSERT INTO \"Friendship\" (\"id\", \"sender_id\", \"receiver_id\", \"sender_server_id\", \"receiver_server_id\", \"confirmed\") VALUES ('"+id+"', '"+senderID+"', '"+receiverID+"', "+CONFIG.OUR_SERVER+", "+receiverServerID+", 'N')");
             }catch(SQLException sqlExcept){
                 System.err.println(sqlExcept.getMessage());
                 this.error = sqlExcept.getMessage();
             }    
         }else{
             remote.remoteFriendRequest(this.getPlayer(senderID), receiverID, receiverServerID);
         }
     }
     
     /**
      * Confirms friendship between players (senderID and receiverID)
      * @param senderID id of player who sent request
      * @param senderServer address of sender's server
      * @param receiverID id of player who accepted request
      * @param receiverServer address of receivers's server
      */
     public void confirmFriendship(String senderID, int senderServer, String receiverID, int receiverServer){
         try{
             Statement stmt = connection.createStatement();
             stmt.execute("UPDATE \"Friendship\" SET \"confirmed\" = 'Y' WHERE \"receiver_id\" = '"+receiverID+"' AND \"receiver_server_id\" = "+receiverServer+" AND \"sender_id\" = '"+senderID+"' AND \"sender_server_id\" = "+senderServer+"");
             stmt.close();
         }catch(SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
     }
     
     /**
      * Cancle friendship request between players (senderID and receiverID)
      * @param senderID id of player who sent request
      * @param senderServer address of sender's server
      * @param receiverID id of player who accepted request
      * @param receiverServer address of receivers's server
      */
     public void rejectFriendship(String senderID, int senderServer, String receiverID, int receiverServer){
         try{
             Statement stmt = connection.createStatement();
             stmt.execute("DELETE FROM \"Friendship\" WHERE \"receiver_id\" = '"+receiverID+"' AND \"receiver_server_id\" = "+receiverServer+" AND \"sender_id\" = '"+senderID+"' AND \"sender_server_id\" = "+senderServer+"");
             stmt.close();
         }catch(SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
     }
     
     
     /**
      * Get highscores for logged player, ordered by amount of money
      * @param playerID id of logged player
      * @return ArrayList of HTML table rows
      */
     public ArrayList<String> getHighscores(String playerID){
         ArrayList<Player> friends =  this.getFriendList(playerID);
         ArrayList<String> friendIDs = new ArrayList<String>();
         ArrayList<String> toReturn = new ArrayList<String>();
         for(Player p: friends){
             if(p.getServerID() == CONFIG.OUR_SERVER){
                 friendIDs.add(p.getUserID());
             }else{
                 // TODO: get player's money amount from different server
             }
         }
         friendIDs.add(playerID);
         // Preparing query
         String query = "SELECT * FROM \"Player\" WHERE ";
         for(String s: friendIDs){
             query += "\"id\" = '"+s+"' OR ";
         }
         query = query.substring(0, query.length()-4);
         query += "ORDER BY \"money\" DESC";
         try{
             Statement stmt = connection.createStatement();
             ResultSet result = stmt.executeQuery(query);
             int i = 1;
             while(result.next()){
                 toReturn.add("<tr><td>"+i+".</td><td><b>"+result.getString("username")+"</b></td><td>"+result.getInt("money")+"$</td></tr>");
                 i++;
             }
             result.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             System.err.println(sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
         return toReturn;
     }
     
     /**
      * Gets player username by player id and server id.
      * @param playerID id of selected player
      * @param serverID id of player's sever
      * @return player's username
      */
     public String getPlayerUsername(String playerID, int serverID){
         if(serverID == CONFIG.OUR_SERVER){
             String email = null;
             try{
                 Statement stmt = connection.createStatement();
                 ResultSet r = stmt.executeQuery("SELECT \"username\" FROM \"Player\" WHERE \"id\" = '"+playerID+"'");
                 r.next();
                 email = r.getString("username");
                 r.close();
                 stmt.close();
             }catch (SQLException sqlExcept){
                 System.err.println(sqlExcept.getMessage());
                 this.error = sqlExcept.getMessage();
             }
             return email;
         }else{
             String address = remote.getRemoteAddress(serverID);
             Player selected = null;
             try {
                 selected = remote.getRemotePlayer(playerID, address);
             } catch (JSONException ex) {
                 System.out.println(ex.toString());
             }
             if(selected != null){
                 return selected.getUsername();
             }
             return null;
         }
     }
     
     /**
      * 
      * @param userID
      * @return 
      */
     public ArrayList<String> getFriendRequestList(String userID){
         ArrayList<String> toReturn = new ArrayList<String>();
         try{
             Statement stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT * FROM \"Friendship\" WHERE \"receiver_id\" = '"+userID+"' AND \"confirmed\" = 'N'");
             while(results.next()){
                 toReturn.add("<li class=\"friend-request\"><a><img src=\"images/avatar.jpg\" alt=\"\" /> "+this.getPlayerUsername(results.getString("sender_id"), results.getInt("sender_server_id"))+"</a><ul class=\"subrequest\"><li><a href=\"main?acceptFriendRequest="+results.getString("id")+"\">Accept Request</a></li><li><a href=\"main?cancelFriendRequest="+results.getString("id")+"\">Cancel Request</a></li></ul></li>");
             }
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             System.err.println(sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
         return toReturn;
     }
     
     /**
      * 
      * @param requestID
      * @param receiverID 
      */
     public void acceptFriendRequest(String requestID, String receiverID){
         try{
             Statement stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT * FROM \"Friendship\" WHERE \"id\" = '"+requestID+"' AND \"receiver_id\" = '"+receiverID+"'");
             if(results.next()){
                 this.confirmFriendship(results.getString("sender_id"), results.getInt("sender_server_id"), receiverID, results.getInt("receiver_server_id"));
                 Player receiver = this.getPlayer(receiverID);
                 String senderUsername = this.getPlayerUsername(results.getString("sender_id"), results.getInt("sender_server_id"));
                 receiver.addNotification(new Notification("Accepted friend request from <b>"+senderUsername+"</b>.", "You have accepted friend request from <b>"+senderUsername+"</b>.", receiver));
                 this.storeNotifications(receiver);
                 Player sender = this.getPlayer(results.getString("sender_id"));
                 if(sender != null){
                     // Sender is from our server, add notification
                     sender.addNotification(new Notification("Accepted friend request from <b>"+receiver.getUsername()+"</b>.", "<b>"+receiver.getUsername()+"</b> has accepted your friend request.", sender));
                     this.storeNotifications(sender);
                 }
             }
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
     }
     
     /**
      * 
      * @param requestID
      * @param receiverID 
      */
     public void cancelFriendRequest(String requestID, String receiverID){
         try{
             Statement stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT * FROM \"Friendship\" WHERE \"id\" = '"+requestID+"' AND \"receiver_id\" = '"+receiverID+"'");
             if(results.next()){
                 this.rejectFriendship(results.getString("sender_id"), results.getInt("sender_server_id"), receiverID, results.getInt("receiver_server_id"));
                 Player receiver = this.getPlayer(receiverID);
                 String senderUsername = this.getPlayerUsername(results.getString("sender_id"), results.getInt("sender_server_id"));
                 receiver.addNotification(new Notification("Rejected friend request from <b>"+senderUsername+"</b>.", "You have rejected friend request from <b>"+senderUsername+"</b>.", receiver));
                 this.storeNotifications(receiver);
                 Player sender = this.getPlayer(results.getString("sender_id"));
                 if(sender != null){
                     // Sender is from our server, add notification
                     sender.addNotification(new Notification("Rejected friend request from <b>"+receiver.getUsername()+"</b>.", "<b>"+receiver.getUsername()+"</b> has rejected your friend request.", sender));
                     this.storeNotifications(sender);
                 }
             }
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
     }
     
     /**
      * @param playerID
      * @return 
      */
     public ArrayList<Monster> getMonstersForSale(String playerID){
         ArrayList<Player> friends =  this.getFriendList(playerID);
        if(friends.size() < 1){
             // Build query
             String query = "SELECT * FROM \"Monster\" WHERE \"sale_offer\" <> 0 AND (";
             for(Player p: friends){
                 if(p.getServerID() == CONFIG.OUR_SERVER){
                     query += "\"user_id\" = '"+p.getUserID()+"' OR ";
                 }
             }
             query = query.substring(0, query.length()-4);
             query += ")";
             ArrayList<Monster> monsters = new ArrayList<Monster>();
             try{
                 Statement stmt = connection.createStatement();
                 ResultSet r = stmt.executeQuery(query);
                 while(r.next()){
                     Monster tmp = new Monster(r.getString("id"), r.getString("name"), new Date(r.getLong("dob")*1000), new Date(r.getLong("dod")*1000), r.getDouble("base_strength"), r.getDouble("current_strength"), r.getDouble("base_defence"), r.getDouble("current_defence"), r.getDouble("base_health"), r.getDouble("current_health"), r.getFloat("fertility"), r.getString("user_id"), r.getInt("sale_offer"), r.getInt("breed_offer"));
                     tmp.setServerID(CONFIG.OUR_SERVER);
                     monsters.add(tmp);
                 }
                 r.close();
                 stmt.close();
             }catch (SQLException sqlExcept){
                 System.err.println(sqlExcept.getMessage());
                 this.error = sqlExcept.getMessage();
             }
             for(Player p: friends){
                 if(p.getServerID() != CONFIG.OUR_SERVER){
                     String address = remote.getRemoteAddress(p.getServerID());
                     ArrayList<Monster> userMonsters = null;
                     try {
                         userMonsters = remote.getRemoteUsersMonsters(p.getUserID(), address);
                         if(userMonsters != null){
                             for(Monster m: userMonsters){
                                 if(m.getSaleOffer() > 0){
                                     monsters.add(m);
                                 }
                             }
                         }
                     } catch (JSONException ex) {
 
                     }
                 }
             }
             return monsters;
         }
         return new ArrayList<Monster>();
     }
     
     /**
      * TODO: check if monster dies
      * @param playerID
      * @return 
      */
     public ArrayList<Monster> getMonstersForBreeding(String playerID){
         ArrayList<Player> friends =  this.getFriendList(playerID);
        if(friends.size() < 1){
             // Build query
             String query = "SELECT * FROM \"Monster\" WHERE \"breed_offer\" <> 0 AND (";
             for(Player p: friends){
                 if(p.getServerID() == CONFIG.OUR_SERVER){
                     query += "\"user_id\" = '"+p.getUserID()+"' OR ";
                 }
             }
             query = query.substring(0, query.length()-4);
             query += ")";
             System.out.println(query);
             ArrayList<Monster> monsters = new ArrayList<Monster>();
             try{
                 Statement stmt = connection.createStatement();
                 ResultSet r = stmt.executeQuery(query);
                 while(r.next()){
                     Monster tmp = new Monster(r.getString("id"), r.getString("name"), new Date(r.getLong("dob")*1000), new Date(r.getLong("dod")*1000), r.getDouble("base_strength"), r.getDouble("current_strength"), r.getDouble("base_defence"), r.getDouble("current_defence"), r.getDouble("base_health"), r.getDouble("current_health"), r.getFloat("fertility"), r.getString("user_id"), r.getInt("sale_offer"), r.getInt("breed_offer"));
                     tmp.setServerID(CONFIG.OUR_SERVER);
                     monsters.add(tmp);
                 }
                 r.close();
                 stmt.close();
             }catch (SQLException sqlExcept){
                 System.err.println(sqlExcept.getMessage());
                 this.error = sqlExcept.getMessage();
             }
             for(Player p: friends){
                 if(p.getServerID() != CONFIG.OUR_SERVER){
                     String address = remote.getRemoteAddress(p.getServerID());
                     ArrayList<Monster> userMonsters = null;
                     try {
                         userMonsters = remote.getRemoteUsersMonsters(p.getUserID(), address);
                         if(userMonsters != null){
                             for(Monster m: userMonsters){
                                 if(m.getBreedOffer() > 0){
                                     monsters.add(m);
                                 }
                             }
                         }
 
                     } catch (JSONException ex) {
 
                     }
                 }
             }
             return monsters;
         }
         return new ArrayList<Monster>();
     }
 
     /**
      * 
      * @param userID
      * @param monsterID
      * @param offerAmount
      * @return 
      */
     public boolean makeNewMarketOffer(String userID, String monsterID, int offerAmount){
         try{
             Statement stmt = connection.createStatement();
             stmt.execute("UPDATE \"Monster\" SET \"sale_offer\" = "+offerAmount+" WHERE \"id\" = '"+monsterID+"' AND \"user_id\" = '"+userID+"'");
             stmt.close();
         }catch(SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         int count = 0;
         try{
             Statement stmt = connection.createStatement();
             stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT count(\"id\") FROM \"Monster\" WHERE \"id\" = '"+monsterID+"' AND \"user_id\" = '"+userID+"'");
             results.next();
             count = results.getInt(1);
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         if(count > 0){
             return true;
         }
         return false;
     }
     
     /**
      * 
      * @param userID
      * @param monsterID
      * @param offerAmount
      * @return 
      */
     public boolean makeNewBreedOffer(String userID, String monsterID, int offerAmount){
         try{
             Statement stmt = connection.createStatement();
             stmt.execute("UPDATE \"Monster\" SET \"breed_offer\" = "+offerAmount+" WHERE \"id\" = '"+monsterID+"' AND \"user_id\" = '"+userID+"'");
             stmt.close();
         }catch(SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         int count = 0;
         try{
             Statement stmt = connection.createStatement();
             stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT count(\"id\") FROM \"Monster\" WHERE \"id\" = '"+monsterID+"' AND \"user_id\" = '"+userID+"'");
             results.next();
             count = results.getInt(1);
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         if(count > 0){
             return true;
         }
         return false;
     }
     
     /**
      * 
      * @param userID
      * @param monsterID
      * @return 
      */
     public boolean cancelMonsterOffer(String userID, String monsterID){
         int count = 0;
         try{
             Statement stmt = connection.createStatement();
             stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT count(\"id\") FROM \"Monster\" WHERE \"id\" = '"+monsterID+"' AND \"user_id\" = '"+userID+"' AND \"sale_offer\" <> 0");
             results.next();
             count = results.getInt(1);
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         if(count < 1){
             return false;
         }
         try{
             Statement stmt = connection.createStatement();
             stmt.execute("UPDATE \"Monster\" SET \"sale_offer\" = 0 WHERE \"id\" = '"+monsterID+"' AND \"user_id\" = '"+userID+"'");
             stmt.close();
         }catch(SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         return true;
     }
     
     /**
      * 
      * @param userID
      * @param monsterID
      * @return 
      */
     public boolean cancelBreedingOffer(String userID, String monsterID){
         int count = 0;
         try{
             Statement stmt = connection.createStatement();
             stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery("SELECT count(\"id\") FROM \"Monster\" WHERE \"id\" = '"+monsterID+"' AND \"user_id\" = '"+userID+"'");
             results.next();
             count = results.getInt(1);
             results.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         if(count < 1){
             return false;
         }
         try{
             Statement stmt = connection.createStatement();
             stmt.execute("UPDATE \"Monster\" SET \"breed_offer\" = 0 WHERE \"id\" = '"+monsterID+"' AND \"user_id\" = '"+userID+"'");
             stmt.close();
         }catch(SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         return true;
     }
     
     /**
      * Only from our DB
      * @param monsterID
      * @return 
      */
     public String getMonsterName(String monsterID){
         String monsterName = null;
         try{
             Statement stmt = connection.createStatement();
             ResultSet r = stmt.executeQuery("SELECT \"name\" FROM \"Monster\" WHERE \"id\" = '"+monsterID+"'");
             r.next();
             monsterName = r.getString("name");
             r.close();
             stmt.close();
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
         }
         return monsterName;
     }
 
     /**
      * 
      * @param userID
      * @param monsterID
      * @param serverID 
      */
     public void buyMonster(String userID, String monsterID, int serverID){
         if(serverID == CONFIG.OUR_SERVER){
             try{
                 Statement stmt = connection.createStatement();
                 ResultSet r = stmt.executeQuery("SELECT \"sale_offer\", \"user_id\" FROM \"Monster\" WHERE \"id\" = '"+monsterID+"'");
                 r.next();
                 int price = r.getInt("sale_offer");
                 String oldOwner = r.getString("user_id");
                 r.close();
                 stmt.execute("UPDATE \"Monster\" SET \"sale_offer\" = 0, \"user_id\" = '"+userID+"' WHERE \"id\" = '"+monsterID+"'");
                 stmt.execute("UPDATE \"Player\" SET \"money\" = \"money\"+"+price+" WHERE \"id\" = '"+oldOwner+"'");
                 stmt.execute("UPDATE \"Player\" SET \"money\" = \"money\"-"+price+" WHERE \"id\" = '"+userID+"'");
                 stmt.close();
                 Player exOwner = this.getPlayer(oldOwner);
                 exOwner.addNotification(new Notification("You have sold <b>"+this.getMonsterName(monsterID) +"</b> for <b>"+price+"$</b>.", "You have sold <b>"+this.getMonsterName(monsterID) +"</b> for <b>"+price+"$</b>. Money are now on your account.", exOwner));
                 this.storeNotifications(exOwner);
                 if(this.getMonsterList(oldOwner).size() < 1){
                     // Old owner doesn't have any monsters
                     String name = NameGenerator.getName();
                     exOwner.addMonster(new Monster(name, oldOwner));
                     this.storeMonsters(exOwner);
                     exOwner.addNotification(new Notification("You run out of monsters.", "You run out of monsters. We have generated new monster for you: <b>"+name+"</b>.", exOwner));
                     this.storeNotifications(exOwner);
                 }
             }catch(SQLException sqlExcept){
                 this.error = sqlExcept.getMessage();
             }
         }else{
             String address = remote.getRemoteAddress(serverID);
             Monster selected;
             try {
                 selected = remote.getRemoteMonster(monsterID, address);
                 selected.setId("0");
                 Player current = this.getPlayer(userID);
                 current.addMonster(selected);
                 this.storeMonsters(current);
             } catch (JSONException ex) {
                 System.err.println(ex.toString());
             }
             
         }
     }
     
     /**
      * 
      * @param monsterID
      * @param serverID
      * @return 
      */
     public Monster getMonster(String monsterID, int serverID){
         if(serverID == CONFIG.OUR_SERVER){
             Monster monster = null;
             try {
                 Statement stmt = connection.createStatement();
                 ResultSet r = stmt.executeQuery("SELECT * FROM \"Monster\" WHERE \"id\" = '" + monsterID + "'");
                 r.next();
                 monster = new Monster(r.getString("id"),
                             r.getString("name"),
                             new java.util.Date(r.getLong("dob")),
                             new java.util.Date(r.getLong("dod")),
                             r.getDouble("base_strength"), 
                             r.getDouble("current_strength"),
                             r.getDouble("base_defence"),
                             r.getDouble("current_defence"),
                             r.getDouble("base_health"),
                             r.getDouble("current_health"), 
                             r.getFloat("fertility"),
                             r.getString("user_id"),
                             r.getInt("sale_offer"),
                             r.getInt("breed_offer"));
                 r.close();
                 stmt.close();
             } catch (SQLException sqlExcept) {
                 System.err.println(sqlExcept.getMessage());
                 this.error = sqlExcept.getMessage();
             }
             return monster;
         }else{
             String address = remote.getRemoteAddress(serverID);
             try {
                 return remote.getRemoteMonster(monsterID, address);
             } catch (JSONException ex) {
                 return null;
             }
         }
     }
     
     /**
      * 
      * @param player 
      */
     public void updateMoney(Player player) {
         try {
             Statement stmt = connection.createStatement();
             stmt.execute("UPDATE \"Player\" SET \"money\" = "+player.getMoney()+" WHERE \"id\" = '"+player.getUserID()+"'"); 
             stmt.close();
         } catch (SQLException sqlExcept) {
             System.err.println(sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
     }
     
     /**
      * 
      */
     public void checkIfAnyMonsterDies(){
         //List of users, who will loose monsters
         ArrayList<String> users = new ArrayList<String>();
         ArrayList<String> monstersToRemove = new ArrayList<String>();
         try{
             Statement stmt = connection.createStatement();
             ResultSet r = stmt.executeQuery("SELECT * FROM \"Monster\"");
             ArrayList<String> queries = new ArrayList<String>();
             while(r.next()){
                 // UPDATE CURRENT HEALTH
                 java.util.Date current = new java.util.Date();
                 double age = (((double)current.getTime()-(double)r.getLong("dob"))/((double)r.getLong("dod")-(double)r.getLong("dob")));
                 double health = 1-Math.exp(age*0.1)+r.getDouble("base_health");
                 //UPDATE CURRENT STRENGTH AND DEFENCE
                 double strength = (Math.exp(0.1*age)-1+r.getDouble("base_strength"))*(2-Math.exp(0.1*age));
                 double defence = (Math.exp(0.1*age)-1+r.getDouble("base_defence"))*(2-Math.exp(0.1*age));
                 queries.add("UPDATE \"Monster\" SET \"current_health\" = "+health+", \"current_strength\" = "+strength+", \"current_defence\" = "+defence+" WHERE \"id\" = '"+r.getString("id")+"'");
                 //CHECK
                 if(r.getLong("dod") < current.getTime() || health <= 0 || strength <= 0 || defence <= 0){
                     monstersToRemove.add(r.getString("id"));
                     if(!users.contains(r.getString("user_id"))){
                         users.add(r.getString("user_id"));
                     }
                 }
             }
             r.close();
             for(String query: queries){
                 stmt.execute(query);
             }
             stmt.close();
         }catch (SQLException sqlExcept){
             System.err.println(sqlExcept.getMessage());
             this.error = sqlExcept.getMessage();
         }
         // Remove monsters
         for(String m: monstersToRemove){
             try{
                 Statement stmt = connection.createStatement();
                 stmt.execute("DELETE FROM \"Monster\" WHERE \"id\" = '"+m+"'");
                 stmt.close();
             }catch(SQLException sqlExcept){
                 this.error = sqlExcept.getMessage();
             }
         }
         // When it was last monster ...
         for(String u: users){
             int count = 0;
             try{
                 Statement stmt = connection.createStatement();
                 stmt = connection.createStatement();
                 ResultSet results = stmt.executeQuery("SELECT count(\"id\") FROM \"Monster\" WHERE \"user_id\" = '"+u+"'");
                 results.next();
                 count = results.getInt(1);
                 results.close();
                 stmt.close();
             }catch (SQLException sqlExcept){
                 this.error = sqlExcept.getMessage();
             }
             if(count < 1){
                 Player p = this.getPlayer(u);
                 String randomName = NameGenerator.getName();
                 p.addMonster(new Monster(randomName, u));
                 this.storeMonsters(p);
                 p.addNotification(new Notification("Your last monster dies.", "Your last monster has died. We generated for you new monster - meet <b>"+randomName+"</b>.", p));
                 this.storeNotifications(p);
             }
         }
     }
     
     
     
     
     
     
     
     
     
     
     
     
     public boolean insert(String query){
         try{
             Statement stmt = connection.createStatement();
             stmt.execute(query);
             stmt.close();
             return true;
         }catch(SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
             return false;
         }
     }
     
     public int count(String query){
         try{
             Statement stmt = connection.createStatement();
             stmt = connection.createStatement();
             ResultSet results = stmt.executeQuery(query);
             int count = 0;
             while(results.next()){
                 count++;
             }
             results.close();
             stmt.close();
             return count;
         }catch (SQLException sqlExcept){
             this.error = sqlExcept.getMessage();
             return -1;
         }
     }
 
     
     public String getErrorMessage(){
         return this.error;
     }
 
     
     
 //    public void addFriend(Friend friend) {
 //        
 //                    // Insert notification into DB
 //        String confirmed = "N";
 //        if(friend.isFriendshipConfirmed()){
 //            confirmed = "Y";
 //        }
 //        Statement stmt;
 //        try {
 //            stmt = connection.createStatement();
 //            stmt.execute("INSERT INTO \"Friendship\" (\"sender_id\", \"receiver_id\", \"sender_server_id\", \"receiver_server_id\", \"CONFIRMED\") VALUES ("+
 //                friend.getRemoteAddress()+", "+friend.getLocalUserID()+", 0, "+friend.getRemoteAddress()+", '"+confirmed+"')", Statement.RETURN_GENERATED_KEYS);
 //        } catch (SQLException ex) {
 //            System.err.println(ex.getMessage());
 //            this.error = ex.getMessage();
 //            Logger.getLogger(PersistenceManager.class.getName()).log(Level.SEVERE, null, ex);
 //        }
 //    }
 }
