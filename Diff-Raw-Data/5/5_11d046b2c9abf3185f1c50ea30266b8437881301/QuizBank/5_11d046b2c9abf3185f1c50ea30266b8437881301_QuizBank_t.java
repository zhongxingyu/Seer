 package database;
 /*
  * Class: QuizBank
  * -------------------------------
  * This class encapsulates the Quizicle database of 
  * quizzes. Methods are exported to check if a uiz 
  * exists, add a quiz, or retrieve a quiz from the quiz 
  * bank. Utilizes the singleton DBConnection stored as 
  * an instance variable to retreive and add data to the
  * underlying MySQL database containing the quiz data.
  */
 
 import java.sql.*;
 import java.util.Date;
 
 import quiz.*;
 
 public class QuizBank {
 
 	private DBConnection connection;
 	
 	/*
 	 * Initializes the database Connection for later use.
 	 */
 	public QuizBank() {
 		connection = DBConnection.getDBConnection();
 	}
 	
 	/*
 	 * Checks if a quiz exists for a given quiz id. Returns
 	 * the result as a boolean to the client.
 	 */
 	public boolean QuizExists(int quiz_id) {
 		ResultSet rs = connection.ExecuteQuery("SELECT COUNT(*) FROM quiz WHERE id=" + quiz_id);
 		int size = 0;
 		try {
 			rs.next();
 			size = rs.getInt(1);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return (size == 1);
 	}
 
 	
 	/*
 	 * Takes in a Quiz object
 	 * as a parameter and then passes the Quiz object's info 
 	 * into the database for storage.
 	 */
 	/**
 	 * Adds a quiz to the database. Thread Safe method.
 	 * @param quiz contains quiz parameters to be passed into the database for storage
 	 * @return the QuizID assigned to the new quiz 
 	 * @throws SQLException 
 	 */
 	public synchronized int addQuiz(Quiz quiz) throws SQLException {
 		// create some parameters for the command
 		int id = 0; // autogenerate a unique quiz id
 		String random = quiz.isRandom() ? "true" : "false";
 		String multipage = quiz.isMultiPage() ? "true" : "false";
 		String feedback = quiz.isAutoCorrect() ? "true" : "false";
 		
 		// form an Insert sql statement and execute it
 		String sql = new String("INSERT INTO quiz VALUES (" + id + ",\""
 		+ quiz.getName() + "\"," + random + "," + multipage + ","
 		+ feedback + ",\"" + quiz.getDescription() + "\","
 		+ quiz.getCreator_userID() + "," + quiz.getMax_score()
 		+ ", CURRENT_DATE);");
 
 		connection.ExecuteUpdate(sql);
 		
 		// get the last inserted id
 		ResultSet rs = connection.ExecuteQuery("SELECT LAST_INSERT_ID();");
		if (rs.next())
			id = rs.getInt(1);
		else
			System.err.println("Failed to obtain quiz id of created quiz.");
 		
 		return id;
 	}
 	
 	
 	/*
 	 * Retrieves a quiz from the database based on a passed-in
 	 * quiz id. Parses the quiz's information form the database
 	 * and creates a new Quiz object with the quiz's data. Returns
 	 * quiz object to client. 
 	 */
 	public Quiz getQuiz(int quiz_id) {
 		Quiz quiz = null;
 		if (!QuizExists(quiz_id)) return quiz;
 		
 		//Get quiz metadata
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM quiz WHERE id=" + quiz_id);
 		try {
 			rs.next();
 			String name = rs.getString("name");
 			boolean random = rs.getBoolean("random");
 			boolean multiPage = rs.getBoolean("multipage");
 			boolean feedback = rs.getBoolean("feedback");
 			String description = rs.getString("description");
 			int userID = rs.getInt("userid");
 			Date created = rs.getDate("created");
 			quiz = new Quiz(quiz_id, name, userID,random, feedback, multiPage, description, created);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		//Now get question ids
 		rs = connection.ExecuteQuery("SELECT * FROM question WHERE quizid=" + quiz_id);
 		try {
 			while (rs.next()) {
 				int question_id = rs.getInt("id");
 				quiz.addQuestion(question_id);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		//Now get tags
 		rs = connection.ExecuteQuery("SELECT * FROM tags WHERE quizid=" + quiz_id);
 		try {
 			while (rs.next()) {
 				String tag = rs.getString("tag");
 				quiz.addTag(tag);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return quiz;
 	}
 }
