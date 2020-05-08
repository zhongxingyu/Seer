 package site;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 public class QuizManager {
 
 	private Connection con;
 	private Quiz quiz;
 	private HttpServletRequest request;
 	private HttpSession session;
 	private int user_id;
 	private int quiz_id;
 	private boolean practice_mode;
 	private String description;
 	private String title;
 	private int max_score;
 	private boolean random_question;
 	private boolean one_page;
 	private boolean immediate_correction;
 	
 	public int getQuizId() {
 		return this.quiz_id;
 	}
 	
 	public QuizManager() {
 		con = MyDB.getConnection();
 	}
 	
 	public QuizManager(Quiz quiz) {
 		this.quiz = quiz;
 	}
 	
 	public QuizManager(HttpServletRequest request, Quiz quiz) {
 		con = MyDB.getConnection();
 		this.request = request;
 		this.session = request.getSession();
 		this.user_id = ((User)session.getAttribute("user")).getId();
 		if(quiz.isPractice_mode()) this.practice_mode = true;
 		else this.practice_mode = false;
 		if(quiz.getDescription() != null) this.description = quiz.getDescription();
 		else this.description = "";
 		if(quiz.getTitle() != null) this.title = quiz.getTitle();
 		else this.title = "";
 		if(quiz.getMax_score() != 0) this.max_score = quiz.getMax_score();
 		else this.max_score = 0;
 		if(quiz.isRandom_question()) this.random_question = true;
 		else this.random_question = false;
 		if(quiz.isOne_page()) this.one_page = true;
 		else this.one_page = false;
 		if(quiz.isImmediate_correction()) this.immediate_correction = true;
 		else this.immediate_correction = false;
 		this.quiz = quiz;
 	}
 	
 	public void addQuizToDataBase() {
 		try {
 			Statement stmt = con.createStatement();
 			String exeStr = "INSERT INTO quizzes (user_id, practice_mode, description, title, max_score," +
 					"random_question, one_page, immediate_correction) VALUES(" + user_id + "," + practice_mode + ",\"" +
 					description + "\",\"" + title + "\"," + max_score + "," + random_question + "," + one_page + "," + immediate_correction + ");";
 			stmt.executeUpdate(exeStr, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			if(rs.next())
 				this.quiz_id = rs.getInt(1);
 		}
 		catch(Exception e) { e.printStackTrace(); }
 	}
 	
 	public void addQuestionResponseToDataBase(String str, String answer) {
 		try {
 			Statement stmt = con.createStatement();
 			String addingToQuestionDB = "INSERT INTO questions (quiz_id, point_value, question_type)"
 					+ "VALUES(" + this.quiz_id + ",1,1)";
 			stmt.executeUpdate(addingToQuestionDB, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			int question_id = 0;
 			if (rs.next()){
 				question_id = rs.getInt(1);
 			}
 			String addingToQRDB = "INSERT INTO question_responses (question_id, string)"
 					+ " VALUES(" + question_id + ",\"" + str + "\")";
 			stmt.executeUpdate(addingToQRDB, Statement.RETURN_GENERATED_KEYS);
 			rs = stmt.getGeneratedKeys();
 			String addingToAnswersDB = "INSERT INTO answers (question_id, string)" +
 					" VALUES(" + question_id + ",\"" + answer + "\")";
 			stmt.executeUpdate(addingToAnswersDB);
 		} catch(Exception e) { }
 	}
 	
 	/** OVERLOADED FOR ANSWERS **/
 	public void addQuestionResponseToDataBase(String str, Answer answers) {
 		try {
 			Statement stmt = con.createStatement();
 			String addingToQuestionDB = "INSERT INTO questions (quiz_id, point_value, question_type)"
 					+ "VALUES(" + this.quiz_id + ",1,1)";
 			stmt.executeUpdate(addingToQuestionDB, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			int question_id = 0;
 			if (rs.next()){
 				question_id = rs.getInt(1);
 			}
 			String addingToQRDB = "INSERT INTO question_responses (question_id, string)"
 					+ " VALUES(" + question_id + ",\"" + str + "\")";
 			stmt.executeUpdate(addingToQRDB, Statement.RETURN_GENERATED_KEYS);
 			rs = stmt.getGeneratedKeys();
 			for(String answer : answers.getAnswers()) {
 				String addingToAnswersDB = "INSERT INTO answers (question_id, string)" +
 						" VALUES(" + question_id + ",\"" + answer + "\")";
 				stmt.executeUpdate(addingToAnswersDB);
 			}
 		} catch(Exception e) { }
 	}
 	
 	public void addFillInTheBlankToDataBase(String front, String back, String answer) {
 		try {
 			Statement stmt = con.createStatement();
 			String addingToQuestionDB = "INSERT INTO questions (quiz_id, point_value, question_type)"
 					+ "VALUES(" + this.quiz_id + ",1,2)";
 			stmt.executeUpdate(addingToQuestionDB, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			int question_id = 0;
 			if (rs.next()){
 				question_id = rs.getInt(1);
 			}
 			String addingToFITBDB = "INSERT INTO fill_in_the_blanks (question_id, string_1,"
 					+ "string_2) VALUES(" + question_id + ",\"" + front + "\",\"" + back + "\")";
 			stmt.executeUpdate(addingToFITBDB);
 			String addingToAnswersDB = "INSERT INTO answers (question_id, string)" +
 					" VALUES(" + question_id + ",\"" + answer + "\")";
 			stmt.executeUpdate(addingToAnswersDB);
 		} catch (Exception e) {
 			
 		}
 	}
 	
 	/** OVERLOADED FOR ANSWERS **/
 	public void addFillInTheBlankToDataBase(String front, String back, Answer answers) {
 		try {
 			Statement stmt = con.createStatement();
 			String addingToQuestionDB = "INSERT INTO questions (quiz_id, point_value, question_type)"
 					+ "VALUES(" + this.quiz_id + ",1,2)";
 			stmt.executeUpdate(addingToQuestionDB, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			int question_id = 0;
 			if (rs.next()){
 				question_id = rs.getInt(1);
 			}
 			String addingToFITBDB = "INSERT INTO fill_in_the_blanks (question_id, string_1,"
 					+ "string_2) VALUES(" + question_id + ",\"" + front + "\",\"" + back + "\")";
 			stmt.executeUpdate(addingToFITBDB);
 			for(String answer : answers.getAnswers()) {
 				String addingToAnswersDB = "INSERT INTO answers (question_id, string)" +
 						" VALUES(" + question_id + ",\"" + answer + "\")";
 				stmt.executeUpdate(addingToAnswersDB);
 			}
 		} catch (Exception e) {
 			
 		}
 	}
 
 	public void addMultipleChoiceToDataBase(String question, String choices, String answer) {
 		try {
 			Statement stmt = con.createStatement();
 			String addingToQuestionDB = "INSERT INTO questions (quiz_id, point_value, question_type)"
 					+ "VALUES(" + this.quiz_id + ",1,3)";
 			stmt.executeUpdate(addingToQuestionDB, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			int question_id = 0;
 			if (rs.next()){
 				question_id = rs.getInt(1);
 			}
 			String addingToMCDB = "INSERT INTO multiple_choices (question_id, string) VALUES(" 
 					+ question_id + ",\"" + question + "\")";
 			stmt.executeUpdate(addingToMCDB, Statement.RETURN_GENERATED_KEYS);
 			rs = stmt.getGeneratedKeys();
 			int multiple_choices_id = 0;
 			if (rs.next()){
 				multiple_choices_id = rs.getInt(1);
 			}
 			String addingToMCCDB = "INSERT INTO multiple_choices_choices (" +
 					"multiple_choices_id, string) VALUES(" + multiple_choices_id + ",\""
 					+ choices + "\")";
 			stmt.executeUpdate(addingToMCCDB);
 			String addingToAnswersDB = "INSERT INTO answers (question_id, string)" +
 					" VALUES(" + question_id + ",\"" + answer + "\")";
 			stmt.executeUpdate(addingToAnswersDB);
 		} catch (Exception e) {
 			
 		}
 	}
 	
 	/** OVERLOADED FOR ANSWERS **/
 	public void addMultipleChoiceToDataBase(String question, ArrayList<MultipleChoiceChoices> choices, Answer answers) {
 		try {
 			Statement stmt = con.createStatement();
 			String addingToQuestionDB = "INSERT INTO questions (quiz_id, point_value, question_type)"
 					+ "VALUES(" + this.quiz_id + ",1,3)";
 			stmt.executeUpdate(addingToQuestionDB, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			int question_id = 0;
 			if (rs.next()){
 				question_id = rs.getInt(1);
 			}
 			String addingToMCDB = "INSERT INTO multiple_choices (question_id, string) VALUES(" 
 					+ question_id + ",\"" + question + "\")";
 			stmt.executeUpdate(addingToMCDB, Statement.RETURN_GENERATED_KEYS);
 			rs = stmt.getGeneratedKeys();
 			int multiple_choices_id = 0;
 			if (rs.next()){
 				multiple_choices_id = rs.getInt(1);
 			}
 			for(MultipleChoiceChoices choice : choices) {
 				String addingToMCCDB = "INSERT INTO multiple_choices_choices (" +
 						"multiple_choices_id, string) VALUES(" + multiple_choices_id + ",\""
 						+ choice.getChoiceString() + "\")";
 				stmt.executeUpdate(addingToMCCDB);
 			}
 			for(String answer : answers.getAnswers()) {
 				String addingToAnswersDB = "INSERT INTO answers (question_id, string)" +
 						" VALUES(" + question_id + ",\"" + answer + "\")";
 				stmt.executeUpdate(addingToAnswersDB);
 			}
 		} catch (Exception e) {
 			
 		}
 	}
 
 	public void addPictureResponseToDataBase(String str, String answer) {
 		try {
 			Statement stmt = con.createStatement();
 			String addingToQuestionDB = "INSERT INTO questions (quiz_id, point_value, question_type)"
 					+ "VALUES(" + this.quiz_id + ",1,4)";
 			stmt.executeUpdate(addingToQuestionDB, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			int question_id = 0;
 			if (rs.next()){
 				question_id = rs.getInt(1);
 			}
 			String addingToPRDB = "INSERT INTO picture_responses (question_id, string)"
 					+ " VALUES(" + question_id + ",\"" + str + "\")";
 			stmt.executeUpdate(addingToPRDB);
 			String addingToAnswersDB = "INSERT INTO answers (question_id, string)" +
 					" VALUES(" + question_id + ",\"" + answer + "\")";
 			stmt.executeUpdate(addingToAnswersDB);
 		} catch(Exception e) { }
 	}
 	
 	/** OVERLOADED FOR ANSWERS AND QUESTION-STRING
 	 * @param str - URL string
 	 * @param question_string - question string
 	 * @param Answers - answers **/
 	public void addPictureResponseToDataBase(String str, String question_string, Answer answers) {
 		try {
 			Statement stmt = con.createStatement();
 			String addingToQuestionDB = "INSERT INTO questions (quiz_id, point_value, question_type)"
 					+ "VALUES(" + this.quiz_id + ",1,4)";
 			stmt.executeUpdate(addingToQuestionDB, Statement.RETURN_GENERATED_KEYS);
 			ResultSet rs = stmt.getGeneratedKeys();
 			int question_id = 0;
 			if (rs.next()){
 				question_id = rs.getInt(1);
 			}
 			String addingToPRDB = "INSERT INTO picture_responses (question_id, string, question_string)"
					+ " VALUES(" + question_id + ",\"" + str + "\",\"" + question_string + "\")";
 			stmt.executeUpdate(addingToPRDB);
 			for(String answer : answers.getAnswers()) {
 				String addingToAnswersDB = "INSERT INTO answers (question_id, string)" +
 						" VALUES(" + question_id + ",\"" + answer + "\")";
 				stmt.executeUpdate(addingToAnswersDB);
 			}
		} catch(Exception e) { System.out.println(e); }
 	}
 	
 	public ArrayList<Quiz> getQuizzesByUserId(int user_id) {
 		
 		int quiz_id = 0;
 		//int user_id = 0;
 		int max_score = 0;
 		boolean practice_mode = false;
 		String description = "";
 		String title = "";
 		boolean random_question = false;
 		boolean one_page = false;
 		boolean immediate_correction = false;
 		Timestamp created_timestamp = null;
 		
 		ArrayList<Quiz> quizzes = new ArrayList<Quiz>();
 		
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			ResultSet rs = stmt.executeQuery("SELECT * FROM quizzes WHERE user_id="+user_id+" ORDER BY created_timestamp DESC");
 			while(rs.next()) {
 				quiz_id = rs.getInt("quiz_id");
 				//user_id = rs.getInt("user_id");
 				max_score = rs.getInt("max_score");
 				practice_mode = rs.getBoolean("practice_mode");
 				description = rs.getString("description");
 				title = rs.getString("title");
 				random_question = rs.getBoolean("random_question");
 				one_page = rs.getBoolean("one_page");
 				immediate_correction = rs.getBoolean("immediate_correction");
 				created_timestamp = rs.getTimestamp("created_timestamp");
 				
 				Quiz quiz = new Quiz(quiz_id, user_id, max_score, practice_mode, description, title, random_question, one_page, immediate_correction, created_timestamp);
 				quizzes.add(quiz);
 			}
 		}
 		catch(Exception e) {
 			System.out.println(e);
 		} 
 		
 		return quizzes;
 	}
 	
 public ArrayList<Quiz> getRecentQuizzesByUserId(int user_id, int interval) {
 		
 		int quiz_id = 0;
 		//int user_id = 0;
 		int max_score = 0;
 		boolean practice_mode = false;
 		String description = "";
 		String title = "";
 		boolean random_question = false;
 		boolean one_page = false;
 		boolean immediate_correction = false;
 		Timestamp created_timestamp = null;
 		
 		ArrayList<Quiz> quizzes = new ArrayList<Quiz>();
 		
 		try {
 			Statement stmt = con.createStatement(); //construct search query based on inputs
 			ResultSet rs = stmt.executeQuery("SELECT * FROM quizzes WHERE user_id="+user_id+" AND created_timestamp >= ( CURDATE() - INTERVAL "+interval+" DAY ) ORDER BY created_timestamp DESC");
 			while(rs.next()) {
 				quiz_id = rs.getInt("quiz_id");
 				//user_id = rs.getInt("user_id");
 				max_score = rs.getInt("max_score");
 				practice_mode = rs.getBoolean("practice_mode");
 				description = rs.getString("description");
 				title = rs.getString("title");
 				random_question = rs.getBoolean("random_question");
 				one_page = rs.getBoolean("one_page");
 				immediate_correction = rs.getBoolean("immediate_correction");
 				created_timestamp = rs.getTimestamp("created_timestamp");
 				
 				Quiz quiz = new Quiz(quiz_id, user_id, max_score, practice_mode, description, title, random_question, one_page, immediate_correction, created_timestamp);
 				quizzes.add(quiz);
 			}
 		}
 		catch(Exception e) {
 			System.out.println(e);
 		} 
 		
 		return quizzes;
 	}
 	
 	public Quiz getQuizByQuizId(int quiz_id) {
 		try{
 		Statement stmt = con.createStatement();
 		ResultSet rs = stmt.executeQuery("SELECT * FROM quizzes WHERE quiz_id="+quiz_id);
 		rs.next();
 		Quiz quiz = new Quiz(rs.getInt("quiz_id"), rs.getInt("user_id"), rs.getInt("max_score"),
 					rs.getBoolean("practice_mode"), rs.getString("description"), rs.getString("title"), 
 					rs.getBoolean("random_question"), rs.getBoolean("one_page"), rs.getBoolean("immediate_correction"), 
 					rs.getTimestamp("created_timestamp"));
 		return quiz;
 		} catch (SQLException e) {
 			
 		}
 		return null;
 	}
 	
 	public ArrayList<Quiz> getWholeQuizTableByDate() {
 		ArrayList<Quiz> table = new ArrayList<Quiz>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM quizzes ORDER BY created_timestamp DESC");
 			while(rs.next()) {
 				Quiz quiz = new Quiz(rs.getInt("quiz_id"), rs.getInt("user_id"), rs.getInt("max_score"),
 						rs.getBoolean("practice_mode"), rs.getString("description"), rs.getString("title"), 
 						rs.getBoolean("random_question"), rs.getBoolean("one_page"), rs.getBoolean("immediate_correction"), 
 						rs.getTimestamp("created_timestamp"));
 				table.add(quiz);
 			}
 		} catch (Exception e) { }
 		return table;
 	}
 	
 	public ArrayList<Quiz> getRecentWholeQuizTableByDate(int interval) {
 		ArrayList<Quiz> table = new ArrayList<Quiz>();
 		try {
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM quizzes "+" WHERE created_timestamp >= ( CURDATE() - INTERVAL "+interval+" DAY ) ORDER BY created_timestamp DESC");
 			while(rs.next()) {
 				Quiz quiz = new Quiz(rs.getInt("quiz_id"), rs.getInt("user_id"), rs.getInt("max_score"),
 						rs.getBoolean("practice_mode"), rs.getString("description"), rs.getString("title"), 
 						rs.getBoolean("random_question"), rs.getBoolean("one_page"), rs.getBoolean("immediate_correction"), 
 						rs.getTimestamp("created_timestamp"));
 				table.add(quiz);
 			}
 		} catch (Exception e) { }
 		return table;
 	}
 }
