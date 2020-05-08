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
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import quiz.*;
 import user.User;
 
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
 		
 		// add the list of tags to the tag table
 		Set<String> tagSet = quiz.getTags();
 		
 		for (String tag : tagSet) {
 			sql = "INSERT INTO tags VALUES (" + id + ",\"" + tag + "\");";
 			connection.ExecuteUpdate(sql);
 		}
 		
 		return id;
 	}
 	
 	
 	/*
 	 * Retrieves all quiz ids from the database and returns them 
 	 * as a list of Integers to the client. Used for debugging, not
 	 * likely to be used in production code.
 	 */
 	public List<Integer> getAllQuizIDs() {
 		List<Integer> quizIDs = new ArrayList<Integer>();
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM quiz");
 		try {
 			while (rs.next()) {
 				int id = rs.getInt("id");
 				quizIDs.add(id);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return quizIDs;
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
 
 	public void updateScore(int quizID, int score) {
 		String sql = "UPDATE quiz SET maxscore=" + score + " WHERE id=" + quizID + ";";
 		connection.ExecuteUpdate(sql);
 	}
 	
 	/**
 	 * Just gets the numQuiz most recent quizzes.
 	 * 
 	 * @param numQuiz
 	 * @return
 	 */
 	public List<Integer> getRecentQuizzes(int numQuiz) {
 		List<Integer> quizList = new ArrayList<Integer>();
 		StringBuffer sb = new StringBuffer("SELECT * from quiz order by created desc limit ");
 		sb.append(numQuiz + ";");
 		
 		try {
 			ResultSet rs = connection.ExecuteQuery(sb.toString());
 			while(rs.next()) {
 				int quizId = rs.getInt("id");
 				quizList.add(quizId);			
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return quizList;
 	}
 	
 	/**
 	 * Returns the most popularquizzes from all time
 	 * --TODO: Give it a time limit parameter (get popular quizzes from last month, etc.)
 	 * @param numQuiz
 	 * @return
 	 */
 	public List<Integer> getPopularQuizzes(int numQuiz) {
 		List<Integer> quizList = new ArrayList<Integer>();
 		StringBuffer sb = new StringBuffer("SELECT quizid, count(*) as count FROM score GROUP BY quizid ORDER BY count DESC limit ");
 		sb.append(numQuiz + ";");
 		
 		try {
 			ResultSet rs = connection.ExecuteQuery(sb.toString());
 			while(rs.next()) {
 				int quizId = rs.getInt("quizid");
 				quizList.add(quizId);			
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return quizList;
 	}	
 	
 	public static final String[] CATEGORIES = {
 		"Geography",
 		"Entertainment",
 		"Science",
 		"History",
 		"Literature",
 		"Sports",
 		"Language",
 		"Just For Fun",
 		"Religion",
 		"Movies",
 		"Television",
 		"Music",
 		"Gaming",
 		"Miscellaneous",
 		"Holiday"
 	};
 
 	/**
 	 * Get a list of quizzes that the user may be interested in taking.
 	 * @param userID quizzes are found that match this user's interest.
 	 * @param n Number of suggestions to find.
 	 * @return a list of Quiz objects. Note that these Quiz objects do not 
 	 * have questions or tags included in their structure.
 	 */
 	public List<Quiz> getSuggestedQuizes(int userID, int n) {
 		List<Quiz> quizes = new ArrayList<Quiz>();
 
 		// TODO SQL Query that finds rivals
 		String sql = "select t2.quizid, t4.name, t4.userid, count(t2.quizid) as count " +
 				"from tags as t1 " +
 				"join tags as t2 " +
 				"on t1.tag like t2.tag " +
 				"join (select distinct userid, quizid from score) as t3 " +
 				"on t1.quizid = t3.quizid " +
 				"join quiz as t4 " +
 				"on t2.quizid = t4.id " +
 				"where not t1.quizid = t2.quizid " +
 				"and t2.quizid not in (select quizid from score where userid=3) " +
				"and t3.userid = " + userID + " " +
 				"group by t2.quizid " +
 				"order by count desc " +
				"limit " + n + ";";	
 		ResultSet rs = connection.ExecuteQuery(sql);
 
 		try {
 			while(rs.next()) {
 				int quiz_id = rs.getInt("quizid");
 				String name = rs.getString("name");
 				boolean random = false;
 				boolean multiPage = false;
 				boolean feedback = false;
 				String description = "";
 				int creator = rs.getInt("userid");
 				Date created = null;
 				Quiz quiz = new Quiz(quiz_id, name, creator,random, feedback, multiPage, description, created);
 				quizes.add(quiz);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return quizes;
 	}
 	
 	
 	/**
 	 * Gets a list of quizzes for a particular category
 	 * @param category Category string to get quizzes for
 	 * @return a list of Quiz objects for the category
 	 */
 	public List<Quiz> getCategoryQuizzes(String category) {
 		List<Quiz> quizzes = new ArrayList<Quiz>();
 		ResultSet rs = connection.ExecuteQuery("SELECT * FROM quiz LEFT JOIN tags ON quiz.id=tags.quizid WHERE tag='" + category + "'");
 
 		try {
 			while(rs.next()) {
 				int quiz_id = rs.getInt("quizid");
 				String name = rs.getString("name");
 				boolean random = false;
 				boolean multiPage = false;
 				boolean feedback = false;
 				String description = "";
 				int creator = rs.getInt("userid");
 				Date created = null;
 				Quiz quiz = new Quiz(quiz_id, name, creator,random, feedback, multiPage, description, created);
 				quizzes.add(quiz);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return quizzes;
 	}
 	
 	/**
 	 * Get a list of quizes that are similar to a particular quiz.
 	 * The metric used to relate quizes is tags in common.
 	 * @param quizID Target Quiz to find similar quizes for.
 	 * @param n Number of results to find.
 	 * @return List of Quiz objects. These quiz object have quizid and quiz name as valid data.
 	 */
 	public List<Quiz> getSimilarQuizes(int quizID, int n) {
 		List<Quiz> quizes = new ArrayList<Quiz>();
 
 		// SQL Query that finds similar quizzes
 		String sql = "select t3.*, count(t2.quizid) as count " +
 				"from tags as t1 " +
 				"join tags as t2 " +
 				"on t1.tag like t2.tag " +
 				"join quiz as t3 " +
 				"on t2.quizid = t3.id " +
 				"where not t1.quizid = t2.quizid " +
 				"and t1.quizid = " + quizID + " " +
 				"group by t2.quizid " +
 				"order by count desc " +
 				"limit " + n + ";";
 		ResultSet rs = connection.ExecuteQuery(sql);
 
 		try {
 			while(rs.next()) {
 				int quiz_id = rs.getInt("id");
 				String name = rs.getString("name");
 				boolean random = rs.getBoolean("random");
 				boolean multiPage = rs.getBoolean("multipage");
 				boolean feedback = rs.getBoolean("feedback");
 				String description = rs.getString("description");
 				int creator = rs.getInt("userid");
 				Date created = rs.getDate("created");
 				Quiz quiz = new Quiz(quiz_id, name, creator,random, feedback, multiPage, description, created);
 				quizes.add(quiz);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return quizes;
 	}
 
 	
 	/**
 	 * Retrieves summary stats for a quiz. Returns them in the form 
 	 * of a list with the following elements: Number of Times Taken, 
 	 * Average Score, Average Time To Complete
 	 * @param quizID quiz to get stats for
 	 * @return List of 3 summary stats as integers
 	 */
 	public List<Double> GetSummaryStats(int quizID) {
 		List<Double> stats = new ArrayList<Double>();
 		int count = 0;
 		int score_sum = 0;
 		int time_sum = 0;
 		int maxscore = 0;
 		ResultSet rs = connection.ExecuteQuery("SELECT score.value, score.time, quiz.maxscore FROM score LEFT JOIN quiz ON score.quizid=quiz.id WHERE quizid=" + quizID);
 		try {
 			while (rs.next()) {
 				count++;
 				score_sum += rs.getInt("value");
 				time_sum += rs.getInt("time");
 				if (maxscore == 0) maxscore = rs.getInt("maxscore");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		//Add Number of times taken to stats list
 		stats.add((double)count);
 		//Calc Average score and add to stats list
 		double avg_score = ((score_sum / (double)count) / maxscore);
 		stats.add(avg_score);
 		//Calc Average time to complete
 		double avg_time = (time_sum / (double)count);
 		stats.add(avg_time);
 
 		return stats;
 	}
 }
