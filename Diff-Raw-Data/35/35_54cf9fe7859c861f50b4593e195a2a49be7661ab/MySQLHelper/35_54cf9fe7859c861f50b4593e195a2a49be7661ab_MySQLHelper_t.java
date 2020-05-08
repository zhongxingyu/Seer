 package com.common;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.model.Answer;
 import com.model.Auth;
 import com.model.Course;
 import com.model.Grade;
 import com.model.Profile;
 import com.model.Question;
 import com.model.Quiz;
 import com.model.StudentAnswer;
 
 public class MySQLHelper
 {
 	private static MySQLHelper instance;	
 	private Connection con = null;
 	
 	private MySQLHelper()
 	{
 		PropertiesLoader prop = PropertiesLoader.getInstance();
 		
 		String url = prop.getValue("dbURL");
 		String user = prop.getValue("dbUser");
 		String password = prop.getValue("dbPassword");		
 		String connectionString = String.format("jdbc:mysql://%s:3306/%s", url,user);
 		
 		try
 		{
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			con = DriverManager.getConnection(connectionString, user, password);
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 				
 	}
 	
 	public static synchronized MySQLHelper getInstance()
 	{
 		if(instance == null)
 			return new MySQLHelper();
 		
 		return instance;
 	}
 	
 	private ResultSet executeSelect(String query)
 	{
 		try
 		{
 			return con.createStatement().executeQuery(query);
 		} 
 		catch (SQLException e)
 		{			
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	private int executeNonQuery(String sql)
 	{
 		try
 		{
 			return con.createStatement().executeUpdate(sql);
 		} 
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return 0;
 	}
 	
 	public boolean authorize(Auth auth) 
 	{
 		String query = 
 				String.format("SELECT count(*) as 'Count' FROM q_users where username = '%s' and password = '%s'", 
 						auth.getUsername(),auth.getPassword());		
 		try
 		{
 			ResultSet rs = executeSelect(query);
 			rs.next();
 			return rs.getInt("Count") == 1;
 		} 
 		catch (SQLException e)
 		{			
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public Profile getProfile(String username)
 	{
 		String query = String.format("SELECT * FROM q_users where username = '%s'", username);
 		try
 		{
 			ResultSet rs = executeSelect(query);
 			rs.next();
 			Profile prof = new Profile();
 			prof.setFirstname(rs.getString("first_name"));
 			prof.setLastname(rs.getString("last_name"));
 			prof.setEmail(rs.getString("email"));
 			return prof;
 		} 
 		catch (SQLException e)
 		{			
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public int setProfile(Profile profile)
 	{
 		String query = String.format("UPDATE q_users set first_name = '%s', last_name = '%s', email = '%s' where username = '%s'",
 				profile.getFirstname(),profile.getLastname(),profile.getEmail(), profile.getAuth().getUsername());
 		if(profile.getAuth().getPassword().length() > 1)
 			query = String.format("UPDATE q_users set first_name = '%s', last_name = '%s', email = '%s', password = '%s' where username = '%s'",
 					profile.getFirstname(),profile.getLastname(),profile.getEmail(),profile.getAuth().getPassword(), profile.getAuth().getUsername());
 		
 		return executeNonQuery(query);
 	}
 	
 	public List<Quiz> getQuizes(String username)
 	{
 		List<Quiz> ret = new ArrayList<Quiz>();
 		
 		String query = String.format("SELECT * "
 				+ "FROM "
 				+ 	"q_quiz join q_roster on q_roster.course = q_quiz.course join "
 				+ 	"q_course on q_course.course_code = q_roster.course "
 				+ 	"where student = '%s'", username);		
 		try
 		{
 			ResultSet rs = executeSelect(query);
 			while(rs.next())
 			{
 				Quiz quiz = new Quiz(rs.getInt("id"), rs.getString("description"), new Course(rs.getString("course_code"), rs.getString("course_description"), rs.getString("teacher")));
 				quiz.setQuestions(getQuestions(quiz.getId()));
 				ret.add(quiz);
 			}		
 		} 
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return ret;
 	}
 	
 	private List<Question> getQuestions(int id) throws SQLException
 	{
 		List<Question> ret = new ArrayList<Question>();
 		
 		String query = String.format("SELECT * FROM q_question where quiz = %s", id);
 		ResultSet rs = executeSelect(query);
 		while(rs.next())
 		{
 			Question question = new Question(rs.getInt("id"), rs.getString("text"));
 			question.setAnswers(getAnswers(question.getId()));
 			
 			ret.add(question);
 		}
 		
 		Collections.shuffle(ret);
 		return ret;
 	}
 
 	private List<Answer> getAnswers(int id) throws SQLException
 	{
 		List<Answer> ret = new ArrayList<Answer>();
 		
 		String query = String.format("SELECT * FROM q_answer where question = %s", id);
 		ResultSet rs = executeSelect(query);
 		while(rs.next())
 		{
 			Answer answer = new Answer(rs.getInt("id"), rs.getString("text"), rs.getBoolean("is_correct"));
 			ret.add(answer);
 		}
 		
 		return ret;
 	}
 	
 	public void insertStudentAnswer(StudentAnswer studentAnswer)
 	{
 		String sql = String.format("INSERT INTO q_student_answer values('%s',%s)", studentAnswer.getStudent(), studentAnswer.getAnswer());
 		executeNonQuery(sql);
 	}
 
 	public boolean isTeacher(String username)
 	{
 		String query = String.format("SELECT * FROM q_users where username = '%s'",username);
 		ResultSet rs = executeSelect(query);
 		try
 		{
 			rs.next();
 			return rs.getInt("is_teacher") == 1;
 		} 
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public List<Course> getCourses()
 	{
 		List<Course> ret = new ArrayList<Course>();
 		String query = String.format("SELECT * FROM q_course");	
 		try
 		{
 			ResultSet rs = executeSelect(query);
 			while(rs.next())
 			{
 				Course course = new Course(rs.getString("course_code"),rs.getString("course_description"), rs.getString("teacher"));
 				ret.add(course);
 			}		
 		} 
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	public boolean createQuiz(Quiz quiz)
 	{		
 		try
 		{
 			String insertQuery = String.format("INSERT INTO q_quiz (course, description) values('%s','%s')", quiz.getCourse().getId(),quiz.getDescription());
 			executeNonQuery(insertQuery);
 			
 			
 			String queryForLastID = "select last_insert_id() as last_id from q_quiz";
 			int lastid;
 			
 			ResultSet rs = executeSelect(queryForLastID);
 			rs.next();
 			lastid = rs.getInt("last_id");
 			quiz.setId(lastid);
 			
 			insertQuestions(lastid, quiz.getQuestions());
 			
 			return true;
 		} 
 		catch (SQLException e)
 		{			
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	private void insertQuestions(int quiz, List<Question> questions) throws SQLException
 	{
 		for(Question question : questions)
 		{	
 			String insertQuery = String.format("INSERT INTO q_question (quiz, text) values('%s','%s')", quiz,question.getText());
 			executeNonQuery(insertQuery);
 				
 			String queryForLastID = "select last_insert_id() as last_id from q_question";
 			int lastid;
 			
 			ResultSet rs = executeSelect(queryForLastID);
 			rs.next();
 			lastid = rs.getInt("last_id");
 			question.setId(lastid);
 			
 			insertAnswers(lastid, question.getAnswers());
 		}
 	}
 	
 	private void insertAnswers(int question, List<Answer> answers) throws SQLException
 	{
 		for(Answer answer : answers)
 		{	
 			String insertQuery = String.format("INSERT INTO q_answer (question, text, is_correct) values('%s','%s',%s)", question,answer.getText(),answer.isCorrect() ? 1 : 0);
 			executeNonQuery(insertQuery);				
 		}
 	}
 	
 	public void insertPing(String student, String teacher)
 	{
 		String insertQuery = String.format("INSERT INTO q_ping (student, teacher) values('%s','%s')", student, teacher);
 		executeNonQuery(insertQuery);
 	}
 	
 	public String getMyPings(String teacher)
 	{	
 		try
 		{		
 			String query = String.format("SELECT * from q_ping join q_users on student = username WHERE teacher = '%s'",teacher);
 		
 			ResultSet rs = executeSelect(query);	
 			if(rs.next())
 			{
 				int id = rs.getInt("id");
 				
 				String delete = String.format("DELETE FROM q_ping where id = %s",id);
 				executeNonQuery(delete);
 				return String.format("Student - %s %s needs your attention", rs.getString("first_name"),rs.getString("last_name"));
 			}
 			
 			return "";
 		} 
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return "";
 		}
 	}
 	
 	public List<Grade> getStudentGrades(String username, String course)
 	{
 		List<Grade> ret = new ArrayList<Grade>();
 		try
 		{		
 			String query = String.format("SELECT * from q_grade_per_quiz WHERE student = '%s' and course = '%s'",username, course);
 		
 			ResultSet rs = executeSelect(query);	
 			while(rs.next())
 			{
 				Grade grade = new Grade(rs.getString("description"),rs.getString("student"),rs.getString("course"),rs.getInt("Points Possible"),rs.getInt("Points Earned"),rs.getDouble("Percent"),rs.getString("Letter Grade"));
 				ret.add(grade);
 			}
 			
 			return ret;
 		} 
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
	
	public List<Grade> getTeacherGrades()
	{
		List<Grade> ret = new ArrayList<Grade>();
		try
		{		
			String query = String.format("SELECT * FROM mbayles.q_grade_per_quiz order by course, `Student Name`");
		
			ResultSet rs = executeSelect(query);	
			while(rs.next())
			{
				Grade grade = new Grade(rs.getString("description"),rs.getString("student"),rs.getString("course"),rs.getInt("Points Possible"),rs.getInt("Points Earned"),rs.getDouble("Percent"),rs.getString("Letter Grade"));
				ret.add(grade);
			}
			
			return ret;
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}
 }
