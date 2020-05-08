 package quizweb.message;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import quizweb.Quiz;
 import quizweb.User;
 import quizweb.XMLElement;
 import quizweb.database.DBConnection;
 
 public class ChallengeMessage extends Message {
 	
 	private static final String DBTable = "challenge";
 	private static final String content = "send you a quiz challenge.";
 	public int quizID;
 	public double bestScore;
 	public boolean isRead;
 	
 	public ChallengeMessage(int uid1, int uid2, int qID, double bestscore) {
 		super(uid1, uid2, content);
 		quizID = qID;
 		bestScore = bestscore;
 		isRead = false;
 	}
 
 	public ChallengeMessage(int id, int uid1, int uid2, Timestamp time, int qID, double bestscore, boolean isread) {
 		super(id, uid1, uid2, content, time);
 		quizID = qID;
 		bestScore = bestscore;
 		isRead = isread;
 	}
 	
 	@Override
 	public void addMessageToDB() {
 		try {
 			String statement = new String("INSERT INTO " + DBTable +"" +
 					"(uid1, uid2, time, qid, bestScore, isRead) VALUES (?, ?, NOW(), ?, ?, ?)");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, fromUser);
 			stmt.setInt(2, toUser);
 			stmt.setInt(3, quizID);
 			stmt.setDouble(4, bestScore);
 			stmt.setBoolean(5, isRead);
 			DBConnection.DBUpdate(stmt);
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	public static ArrayList<ChallengeMessage> getMessagesByUserID(int userID) {
 		ArrayList<ChallengeMessage> ChallengMessageQueue = new ArrayList<ChallengeMessage>();
 		try {
			String statement = new String("SELECT * FROM " + DBTable + " WHERE uid2 = ?");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, userID);
 			ResultSet rs = DBConnection.DBQuery(stmt);
 			rs.beforeFirst();
 			while(rs.next()) {
 				ChallengeMessage cm = new ChallengeMessage(
 						rs.getInt("mid"), rs.getInt("uid1"), rs.getInt("uid2"), 
 						rs.getTimestamp("time"), rs.getInt("qid"), rs.getDouble("bestScore"),
 						rs.getBoolean("isRead"));
 				ChallengMessageQueue.add(cm); 
 			}
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 		return ChallengMessageQueue;
 	}
 	
 	public static int getUnreadCount(User user) {
 		int unreadCount = 0;
 		try {
 			String statement = new String("SELECT COUNT(mid) FROM " + DBTable + " WHERE uid2 = ? and isRead = false");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setInt(1, user.userID);
 			ResultSet rs = stmt.executeQuery();
 			if (rs.next())
 				unreadCount = rs.getInt("COUNT(mid)");
 			rs.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}		
 		return unreadCount;		
 	}
 
 	public static Message getChallengeMessageByXMLElem(XMLElement root) {
 		User toUser = null;
 		User fromUser = null;
 		Quiz quiz = null;
 		boolean isRead = false;
 		double bestScore = 0;
 		if (root.attributeMap.containsKey("isread") && root.attributeMap.get("isread").equals("true")) 
 			isRead = true;
 		for (int i = 0; i < root.childList.size(); i++) {
 			XMLElement elem = root.childList.get(i);
 			if (elem.name.equals("to")) {
 				toUser = User.getUserByUsername(elem.content);
 			} else if (elem.name.equals("from")) {
 				fromUser = User.getUserByUsername(elem.content);
 			} else if (elem.name.equals("content")) {
 				// pass
 			} else if (elem.name.equals("quiz")) {
 				quiz = Quiz.getQuizByQuizName(elem.content);
 			} else {
 				System.out.println("Unrecognized field in challenge message " + elem.name);
 			}
 		}
 		if (fromUser == null) {
 			System.out.println("Unrecognized from user");
 			return null;
 		} else if (toUser == null) {
 			System.out.println("Unrecognized to user");
 			return null;
 		} else if (quiz == null) {
 			System.out.println("Unrecognized quiz name");
 			return null;
 		}
 		bestScore = quiz.getUserBestScore(fromUser);
 		ChallengeMessage message = new ChallengeMessage(fromUser.userID, toUser.userID, quiz.quizID, bestScore);
 		message.isRead = isRead;
 		return message;
 	}
 }
