 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package capstone.server;
 
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashSet;
 
 
 /**
  *
  * @author luke
  */
 public class databaseAccess {
     private static Statement createConnection() throws Exception {
 	Class.forName("com.mysql.jdbc.Driver");
         java.sql.Connection con = DriverManager.getConnection("jdbc:mysql://mysql-CapstoneG2.jelastic.servint.net/tictactoedb?useUnicode=yes&characterEncoding=UTF-8", "admin", "capstone2");
         return con.createStatement();
     }
 	
     public static String checkLoginCredentials(String userName, String password) {
 	try {
             Statement st = createConnection();
             ResultSet rs = st.executeQuery("SELECT * FROM players WHERE user ='"+Encryption.encrypt(userName)+"'");
             if(rs.next()) {
                 if(Encryption.decrypt(rs.getString(2)).equals(password)) {
                     return userName;
 		} else {
                     st.close();
                     return "loginError";
 		}
             } else {
 		st.close();
 		return "loginError";
             }
 	}
 	catch (Exception e) {
             e.printStackTrace();
             return "exception";
 	}
     }
     
     public static Map getPlayerDetails(String player) {
         Map details = new HashMap();
         try {
             Statement st = createConnection();
             ResultSet rs = st.executeQuery("SELECT * FROM players WHERE user ='"+Encryption.encrypt(player)+"'");
             if(rs.next()) {
                 details.put("userName", Encryption.decrypt(rs.getString(1)));
                 details.put("password", Encryption.decrypt(rs.getString(2)));
                 details.put("email", Encryption.decrypt(rs.getString(3)));
                 if(Encryption.decrypt(rs.getString(4)).equals("0")) {
                     details.put("fbid", "");
                 } else {
                     details.put("fbid", Encryption.decrypt(rs.getString(4)));
                 }
                 details.put("id", rs.getString(5));
                 if(rs.getString(6).equals("0")) {
                     details.put("friends", "");
                 } else {
                     details.put("friends", rs.getString(6));
                 }
                 if(rs.getString(7).equals("0")) {
                     details.put("friendRequests", "");
                 } else {
                     details.put("friendRequests", rs.getString(7));
                 }
                 if(Encryption.decrypt(rs.getString(8)).equals("0")) {
                     details.put("gid", "");
                 } else {
                     details.put("gid", Encryption.decrypt(rs.getString(8)));
                 }
                 if(Encryption.decrypt(rs.getString(9)).equals("0")) {
                     details.put("gName", "");
                 } else {
                     details.put("gName", Encryption.decrypt(rs.getString(9)));
                 }
                 if(Encryption.decrypt(rs.getString(10)).equals("0")) {
                     details.put("gLink", "");
                 } else {
                     details.put("gLink", Encryption.decrypt(rs.getString(10)));
                 }
                 if(Encryption.decrypt(rs.getString(11)).equals("0")) {
                     details.put("fbName", "");
                 } else {
                     details.put("fbName", Encryption.decrypt(rs.getString(11)));
                 }
                 if(Encryption.decrypt(rs.getString(12)).equals("0")) {
                     details.put("fbLink", "");
                 } else {
                     details.put("fbLink", Encryption.decrypt(rs.getString(12)));
                 }
             }
             st.close();
             return details;
 	}
 	catch (Exception e) {
             e.printStackTrace();
             return details;
 	}
     }
     
     public static boolean addPlayer(Map details) {
         try {
             Statement st = createConnection();
            st.executeUpdate("INSERT into players (user, password, email) VALUES ('"+Encryption.encrypt(details.get("userName").toString())+"','"+Encryption.encrypt(details.get("password").toString())+"','"+Encryption.encrypt(details.get("email").toString())+"'");
             st.close();
             return true;
 	}
 	catch (Exception e) {
             e.printStackTrace();
             return false;
 	}
     }
     
     public static boolean addGooglePlayer(Map details) {
         try {
             Statement st = createConnection();
            st.executeUpdate("INSERT into players (user, password, email, gid, gName, gLink) VALUES ('"+Encryption.encrypt(details.get("userName").toString())+"','"+Encryption.encrypt(details.get("password").toString())+"','"+Encryption.encrypt(details.get("email").toString())+"','"+Encryption.encrypt(details.get("gid").toString())+"','"+Encryption.encrypt(details.get("gName").toString())+"','"+Encryption.encrypt(details.get("gLink").toString())+"'");
             st.close();
             return true;
 	}
 	catch (Exception e) {
             e.printStackTrace();
             return false;
 	}
     }
     
     public static boolean addFBPlayer(Map details) {
         try {
             Statement st = createConnection();
            st.executeUpdate("INSERT into players (user, password, email, fbid, fName, fLink) VALUES ('"+Encryption.encrypt(details.get("userName").toString())+"','"+Encryption.encrypt(details.get("password").toString())+"','"+Encryption.encrypt(details.get("email").toString())+"','"+Encryption.encrypt(details.get("fbid").toString())+"','"+Encryption.encrypt(details.get("fName").toString())+"','"+Encryption.encrypt(details.get("fLink").toString())+"'");
             st.close();
             return true;
 	}
 	catch (Exception e) {
             e.printStackTrace();
             return false;
 	}
     }
     
     public static boolean updatePlayerDetails(Map details) {
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET user='"+Encryption.encrypt(details.get("userName").toString())+"', email='"+Encryption.encrypt(details.get("email").toString())+"', password='"+Encryption.encrypt(details.get("password").toString())+"' WHERE user='"+Encryption.encrypt(details.get("oldUserName").toString())+"'");
             return true;
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
     
     public static boolean playerExists(String player) {
         Map details = getPlayerDetails(player);
         if(details.isEmpty()) {
             return false;
         } else {
             return true;
         }
     }
     
     public static boolean emailExists(String email) {
         try {
             Statement st = createConnection();
             ResultSet rs = st.executeQuery("SELECT * FROM players WHERE email ='"+Encryption.encrypt(email)+"'");
             if(rs.next()) {
                 st.close();
                 return true;   
             } else {
                 st.close();
                 return false;
             }
         } catch (Exception e) {
             e.printStackTrace();
             return true;
         }
     }
     
     public static Map getPlayerDetailsByFBID(String id) {
         Map details = new HashMap();
         try {
             Statement st = createConnection();
             ResultSet rs = st.executeQuery("SELECT * FROM players WHERE fbid ='"+Encryption.encrypt(id)+"'");
             if(rs.next()) {
                 details.put("userName", Encryption.decrypt(rs.getString(1)));
                 details.put("password", Encryption.decrypt(rs.getString(2)));
                 details.put("email", Encryption.decrypt(rs.getString(3)));
                 if(Encryption.decrypt(rs.getString(4))=="0") {
                     details.put("fbid", "");
                 } else {
                     details.put("fbid", Encryption.decrypt(rs.getString(4)));
                 }
             }
             st.close();
             return details;
 	}
 	catch (Exception e) {
             e.printStackTrace();
             return details;
 	}
     }
     
     public static Map getPlayerDetailsByGID(String id) {
         Map details = new HashMap();
         try {
             Statement st = createConnection();
             ResultSet rs = st.executeQuery("SELECT * FROM players WHERE gid ='"+Encryption.encrypt(id)+"'");
             if(rs.next()) {
                 details.put("userName", Encryption.decrypt(rs.getString(1)));
                 details.put("password", Encryption.decrypt(rs.getString(2)));
                 details.put("email", Encryption.decrypt(rs.getString(3)));
             }
             st.close();
             return details;
 	}
 	catch (Exception e) {
             e.printStackTrace();
             return details;
 	}
     }
     
     public static boolean addFBID(String player, String id, String profileName, String link) {
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET fbid='"+Encryption.encrypt(id)+"', fbName='"+Encryption.encrypt(profileName)+"', fbLink='"+Encryption.encrypt(link)+"' WHERE user='"+Encryption.encrypt(player)+"'");
             return true;
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
     
     public static boolean addGID(String player, String id, String profileName, String link) {
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET gid='"+Encryption.encrypt(id)+"', gName='"+Encryption.encrypt(profileName)+"', gLink='"+Encryption.encrypt(link)+"' WHERE user='"+Encryption.encrypt(player)+"'");
             return true;
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
     
     public static boolean removeFBID(String player) {
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET fbid='0', fbName='0', fbLink='0' WHERE user='"+Encryption.encrypt(player)+"'");
             return true;
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
     
     public static boolean removeGID(String player) {
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET gid='0', gName='0', gLink='0' WHERE user='"+Encryption.encrypt(player)+"'");
             return true;
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
     
     public static List<String> getFriends(String player) {
         List<String> friends = new ArrayList<String>();
         String names = "";
         Map details = getPlayerDetails(player);
         names = (String)details.get("friends");
         if(names.contains(",")) {
             String[] namesArray = names.split(",");
             for(int x=0;x<namesArray.length;x++) {
                 String friend = namesArray[x].substring(1);
                 try {
                     Statement st = createConnection();
                     ResultSet rs = st.executeQuery("SELECT * FROM players WHERE playerID ='"+friend+"'");
                     if(rs.next()) {
                         friends.add(Encryption.decrypt(rs.getString(1)));
                     }
                     st.close();
                 }
                 catch (Exception e) {
                     e.printStackTrace();
                     return friends;
                 }
             }
         }
         //remove any duplicates
         HashSet set = new HashSet(friends);
         friends.clear();
         friends.addAll(set);
         return friends;
     }
     
     public static boolean addFriend(String player, String friend) {
         String friendRequests = "";
         Map friendDetails = getPlayerDetails(friend);
         if(friendDetails.isEmpty()) {
             //friend does not exist
             return false;
         }
         friendRequests = (String)friendDetails.get("friendRequests");
         Map playerDetails = getPlayerDetails(player);
         if(playerDetails.isEmpty()) {
             //there was a problem
             return false;
         }
         String playerID = (String)playerDetails.get("id");
         friendRequests = friendRequests + ":" + playerID + ",";
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET friendRequests='"+friendRequests+"' WHERE user='"+Encryption.encrypt(friend)+"'");
             return true;
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
     
     public static boolean acceptFriend(String player, String friend) {        
         //get player details
         Map playerDetails = getPlayerDetails(player);
         String friendRequests = (String)playerDetails.get("friendRequests");
         String friends = (String)playerDetails.get("friends");
         String playerID = (String)playerDetails.get("id");
         playerID = ":" + playerID + ",";
         //get friend details
         Map friendDetails = getPlayerDetails(friend);
         String friendID = (String)friendDetails.get("id");
         String friendFriendRequests = (String)friendDetails.get("friendRequests");
         friendID = ":" + friendID + ",";
         String friendFriends = (String)friendDetails.get("friends");
         
         if(!friendRequests.contains(friendID)) {
             // problem - no request for this friend exists
             return false;
         }
         friends = friends + friendID;
         friendFriends = friendFriends + playerID;
         
         //add friend to player's friends and remove from requests
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET friends='"+friends+"' WHERE user='"+Encryption.encrypt(player)+"'");
             //remove friend from player's requests
             String newRequestList = friendRequests.replace(friendID, "");
             try {
                 st.executeUpdate("UPDATE players SET friendRequests='"+newRequestList+"' WHERE user='"+Encryption.encrypt(player)+"'");
                 //add player to friend's friends
                 try {
                     st.executeUpdate("UPDATE players SET friends='"+friendFriends+"' WHERE user='"+Encryption.encrypt(friend)+"'");
                     //if friend has a request from player, remove it
                     if(friendFriendRequests.contains(playerID)) {
                         String newFriendFriendRequestList = friendFriendRequests.replace(playerID, "");
                         try {
                             st.executeUpdate("UPDATE players SET friendRequests='"+newFriendFriendRequestList+"' WHERE user='"+Encryption.encrypt(friend)+"'");
                         }
                         catch (Exception e) {
                             e.printStackTrace();
                             //if all the rest has worked, we'll still return true at this stage
                             return true;
                         }
                     }
                     return true;
                 }
                 catch (Exception e) {
                     e.printStackTrace();
                     return false;
                 }
             }
             catch (Exception e) {
                 e.printStackTrace();
                 return false;
             }
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
     
     public static List<String> getFriendRequests(String player) {
         List<String> friends = new ArrayList<String>();
         String names = "";
         Map details = getPlayerDetails(player);
         names = (String)details.get("friendRequests");
         if(names.contains(",")) {
             String[] namesArray = names.split(",");
             for(int x=0;x<namesArray.length;x++) {
                 String friend = namesArray[x].substring(1);
                 try {
                     Statement st = createConnection();
                     ResultSet rs = st.executeQuery("SELECT * FROM players WHERE playerID ='"+friend+"'");
                     if(rs.next()) {
                         friends.add(Encryption.decrypt(rs.getString(1)));
                     }
                     st.close();
                 }
                 catch (Exception e) {
                     e.printStackTrace();
                     return friends;
                 }
             }
         }
         //remove any duplicates
         HashSet set = new HashSet(friends);
         friends.clear();
         friends.addAll(set);
         return friends;
     }
     
     public static boolean declineRequest(String player, String friend) {
         Map details = getPlayerDetails(player);
         String requests = (String)details.get("friendRequests");
         Map friendDetails = getPlayerDetails(friend);
         String id = (String)friendDetails.get("id");
         id = ":" + id + ",";
         if(!requests.contains(id)) {
             //problem - no request exists for this player
             return false;
         }
         String newRequests = requests.replace(id, "");
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET friendRequests='"+newRequests+"' WHERE user='"+Encryption.encrypt(player)+"'");
             return true;
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
     
     public static boolean areFriends(String player1, String player2) {
         Map player1Details = getPlayerDetails(player1);
         Map player2Details = getPlayerDetails(player2);
         String player1ID = (String)player1Details.get("id");
         player1ID = ":" + player1ID + ",";
         String player2Friends = (String)player2Details.get("friends");
         if(player2Friends.contains(player1ID)) {
             return true;
         } else {
             return false;
         }
     }
     
     public static boolean removeFriend(String player, String friend) {
         //get player details
         Map playerDetails = getPlayerDetails(player);
         String playerID = (String)playerDetails.get("id");
         playerID = ":" + playerID + ",";
         String playerFriends = (String)playerDetails.get("friends");
         //Get friend details
         Map friendDetails = getPlayerDetails(friend);
         String friendID = (String)friendDetails.get("id");
         friendID = ":" + friendID + ",";
         String friendFriends = (String)friendDetails.get("friends");
         
         String newPlayerFriends = playerFriends.replace(friendID, "");
         String newFriendFriends = friendFriends.replace(playerID, "");
         
         try {
             Statement st = createConnection();
             st.executeUpdate("UPDATE players SET friends='"+newPlayerFriends+"' WHERE user='"+Encryption.encrypt(player)+"'");
             st.executeUpdate("UPDATE players SET friends='"+newFriendFriends+"' WHERE user='"+Encryption.encrypt(friend)+"'");
             return true;
         }
         catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 }
