 package user;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import database.DataBaseObject;
 
 import quiz.QuizConstants;
 import quiz.QuizResult;
 
 
 public class Challenge extends Message {
 	private QuizResult result;
 
 	public Challenge(String[] args, Connection conn) {
 		super(args, conn);
 		int resultId = Integer.parseInt(args[4]);
 		getResult(conn, resultId);
 	}
 	
 	public Challenge(String sender, String recipient, QuizResult result) {
 		super(sender, recipient);
 		this.result = result;
 	}
 
 	@Override
 	public void saveToDataBase(Connection conn) {
 		saveToMsg(conn);
 		saveToChallenge(conn);
 
 	}
 	
 	public QuizResult getChallengerResult() {
 		return result;
 	}
 
 	
 	private void getResult(Connection conn, int id) {
 		try {
 			Statement stmt = conn.createStatement();      
 			String query = "SELECT * from Quiz_Result WHERE id=" + id + ";";     
 			
 			ResultSet rs = stmt.executeQuery(query);
 			while (rs.next()) {
 				String[] row = DataBaseObject.getRow(rs, QuizConstants.CHALLENGE_N_COLS);
 				result = new QuizResult(row, conn);
 			}
 			   
 		} catch (SQLException e) {     
 			e.printStackTrace();
 		}
 	}
 	
 	private void saveToChallenge(Connection conn) {
		
		String query;
 		try {
 			Statement stmt = conn.createStatement();
			if (dbID == -1) {
				generateID(conn, "Message");
				query = "Insert into Challenge VALUES (" + dbID + ", " + result.getID() + ");";
				stmt.executeUpdate(query);
				query = "Insert into Message VALUES ('" + dbID + "', '" + sender + "', '" + recipient + "', " + beenRead + ");";
				stmt.executeUpdate(query);
			} 
 			
 			stmt.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
		
 	}
 	
 	public String getHTMLSummary() {
 		return sender + " has challenged you to take: " + this.result.getQuiz().getName();
 	}
 }
