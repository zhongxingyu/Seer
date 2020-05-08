 package models;
 
 public class AdminControl {
 
 	public static AdminControl sharedInstance;
 	public static AdminControl sharedInstance() {
 		if (sharedInstance == null) {
 			sharedInstance = new AdminControl();
 		}
 		
 		return sharedInstance;
 	}
 	
 	public void removeUser(int id, int adminID, String username, String reason) {
 		DBConnection connection = DBConnection.sharedInstance();
 		
 		connection.insert("DELETE FROM friends_join WHERE friend1ID = " + id);
 		connection.insert("DELETE FROM friends_join WHERE friend2ID = " + id);
 		
 		connection.insert("DELETE FROM friend_requests WHERE friendFromID = " + id);
 		connection.insert("DELETE FROM friend_requests WHERE friendToID = " + id);
 		
 		connection.insert("DELETE FROM messages WHERE friendFromID = " + id);
 		connection.insert("DELETE FROM messages WHERE friendToID = " + id);
 		
 		connection.insert("DELETE FROM challenges WHERE friendFromID = " + id);
 		connection.insert("DELETE FROM challenges WHERE friendToID = " + id);
 		
 		connection.insert("DELETE FROM achievementsAwarded WHERE userID = " + id);
 		
 		connection.insert("DELETE FROM quizzes WHERE creator = " + id);
 		
 		connection.insert("DELETE FROM quiz_results WHERE userID = " + id);
 		
 		connection.insert("DELETE FROM users WHERE id = " + id);
 		
 		connection.insert("INSERT INTO terminated_users (userID, username, reason, adminID) " +
 				"VALUES(" + id + ",'" + username + "','" + reason + "'," + adminID + ")");
 	}
 	
 	public void promoteUser(int id) {
 		DBConnection connection = DBConnection.sharedInstance();
 		connection.insert("UPDATE users SET isAdmin = 1 WHERE id = " + id);
 	}
 	
	public void clearQuizHistory(int quizID) {
 		DBConnection connection = DBConnection.sharedInstance();
		connection.insert("DELETE FROM quiz_results WHERE quizID = " + quizID);
 	}
 	
 	public void sendAnouncement(int adminID, String message, boolean isAdminOnly) {
 		Anouncement anouncement = new Anouncement();
 		anouncement.setAdminID(adminID);
 		anouncement.setAnouncement(message);
 		anouncement.setIsAdminOnly(isAdminOnly);
 		AnouncementFactory af = new AnouncementFactory();
 		af.sendAnouncement(anouncement);
 	}
 	
 	public void removeQuiz(int quizID) {
 		DBConnection connection = DBConnection.sharedInstance();
 		connection.insert("DELETE FROM quizzes WHERE id = " + quizID);
 	}
 	
 }
