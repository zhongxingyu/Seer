 package site;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 
 /** Allows users to report quizzes as inappropriate and also give a complaint message
 	public final int reportId;
 	public final int userId;
 	public final int quizId;
 	public final String text;
 	public final Timestamp time;
 
  * */
 public class ReportManager {
 
 	private static Connection con;
 	private static Statement stmt;
 
 
 	public ReportManager(){
 		con = MyDB.getConnection();
 		try {
 			stmt = con.createStatement();
 		} catch (SQLException e) {
 
 		}
 	}
 	
 	
 	
 	public static int reportQuiz(int user_id, int quiz_id, String reportText){
 		String execution = "INSERT INTO reports VALUES( NULL," + user_id + "," + quiz_id + ", '" + reportText + "', NOW())";
 		String execution2 = "SELECT LAST_INSERT_ID()";
 		try {
 			stmt.executeUpdate(execution);
 			ResultSet set = stmt.executeQuery(execution2);
 			set.first();
 			return set.getInt("LAST_INSERT_ID()");
 		} catch (SQLException e) {
 		}
 		return -1;
 	}
 	
 	public static void removeReport(int report_id){
 		String execution = "DELETE FROM reports where report_id=" + report_id;
 		try {
 			stmt.executeUpdate(execution);
 		} catch (SQLException e) {
 		}
 	}
 	
	/**Removes all reports associated with a quiz*/
	public static boolean clearReportsByQuiz(int quiz_id){
		String deleteQuiz = "DELETE FROM reports where quiz_id = " + quiz_id;
		try {
			stmt.executeUpdate(deleteQuiz);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
 	public static int getNumReports(int quiz_id){
 		String execution = "SELECT COUNT(*) from reports GROUP BY quiz_id";
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			set.first();
 			return set.getInt("COUNT(*)");
 		} catch (SQLException e) {
 		}
 		return -1;
 	}
 	
 	public static ArrayList<Report> getReportedQuizzes(){
 		String execution = "SELECT * FROM reports ORDER BY created_timestamp DESC";
 		ArrayList<Report> reports = new ArrayList<Report>();
 		
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			while(set.next()){
 				reports.add(getReportFromSet(set));
 			}
 			return reports;
 		} catch (SQLException e) {
 		}
 		return reports;
 	}
 	
 	public static ArrayList<Report> getReportsForQuiz(int quiz_id){
 		String execution = "SELECT * FROM reports WHERE quiz_id ="+quiz_id+"ORDER BY created_timestamp DESC";
 		ArrayList<Report> reports = new ArrayList<Report>();
 		
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			while(set.next()){
 				reports.add(getReportFromSet(set));
 			}
 			return reports;
 		} catch (SQLException e) {
 		}
 		return reports;
 	}
 	
 	
 	
 	private static Report getReportFromSet(ResultSet set) {
 		try {
 			int report = set.getInt("report_id");
 			int user = set.getInt("user_id");
 			int quiz = set.getInt("quiz_id");
 			String text = set.getString("string");
 			Timestamp time = set.getTimestamp("created_timestamp");
 			return new Report(report, user, quiz, text, time);
 		} catch (SQLException e) {
 		}
 		return null;
 	}
 
 
 
 	public boolean isReported(int quiz_id){
 		String execution = "SELECT * FROM reports WHERE quiz_id ="+quiz_id;
 		try {
 			ResultSet set = stmt.executeQuery(execution);
 			return set.next();
 		} catch (SQLException e) {
 		}
 		return false;
 	}
 	
 	
 }
