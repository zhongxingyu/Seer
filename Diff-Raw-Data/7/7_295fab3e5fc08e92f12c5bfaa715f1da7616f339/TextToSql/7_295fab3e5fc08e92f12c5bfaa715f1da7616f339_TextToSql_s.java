 package persistenty;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 
 import model.Exercise;
 import model.ExerciseCatalog;
 import model.Quiz;
 import model.QuizCatalog;
 
 public class TextToSql {
 
 	private QuizCatalog quizModel;
 	private ExerciseCatalog exerciseModel;
 
 	private void SendToSql(QuizCatalog quizModel, ExerciseCatalog exerciseModel) throws SQLException{
 
 		this.quizModel = quizModel;
 		this.exerciseModel = exerciseModel;
 
 		this.exerciseModel.readExercisesFromFile();
 		this.quizModel.readQuizzesFromFile();
 
 		this.exerciseModel.createQuizExercises(exerciseModel.getExercises(),
 				quizModel.getQuizCatalogs());
 
 		PreparedStatement pstmt = null;
 
 		Connection conn = null;
 
 		try {
 			conn = getConnection();
 			conn.setAutoCommit(false);
 
 			//mapping input
 			pstmt = conn.prepareStatement("insert into Quiz(quiz_id, subject, teacher, date, is_test, is_unique_participation, grades, state) values (?,?,?,?,?,?,?,?)");
 
 			for(Quiz q : quizModel.getQuizCatalogs()){
 
 				pstmt.setInt(1, q.getQuizId());
 				pstmt.setString(2, q.getSubject());
 				pstmt.setObject(3, q.getTeacher().toString());
 			
 				String dateToConvert = q.getDate().getDateInEuropeanFormat();
 				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
 			    java.util.Date date = formatter.parse(dateToConvert);
 			    long dateToLong = date.getTime();
 			    java.sql.Date finalDate = new Date(dateToLong);
 				pstmt.setDate(4, finalDate);
 	
 				pstmt.setBoolean(5, q.isTest());		
 				pstmt.setBoolean(6, q.isUniqueParticipation());
 				pstmt.setInt(7, q.getLeerJaren());		
 				pstmt.setObject(8, q.getStatus().toString());
 
 				pstmt.executeUpdate();
 				conn.commit();
 			}
 			
 			pstmt.close();
 			
 			pstmt = conn.prepareStatement("insert into Exercise(exercise_id, question, author, category, correct_answer, date_registration, descriminator, answer_hints, max_answer_time, max_number_of_attempts) values (?,?,?,?,?,?,?,?,?,?)");
 			
 			for(Exercise ex : exerciseModel.getExercises()){
 				
 				pstmt.setInt(1, ex.getExerciseId());
 				pstmt.setString(2, ex.getQuestion());
 				pstmt.setString(3, ex.getAuthor().toString());
 				pstmt.setString(4, ex.getCategory().toString());
 				pstmt.setString(5, ex.getCorrectAnswer());
 				
 				String dateToConvert = ex.getDateRegistration().getDateInEuropeanFormat();
 				SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
 			    java.util.Date date = formatter.parse(dateToConvert);
 			    long dateToLong = date.getTime();
 			    java.sql.Date finalDate = new Date(dateToLong);
 				pstmt.setDate(6,finalDate);
 
 				pstmt.setString(7, String.valueOf(ex.getDiscriminator()));
 				pstmt.setString(8, ex.getAnswerHints().toString());
 				pstmt.setInt(9, ex.getMaxAnswerTime());
 				pstmt.setInt(10, ex.getMaxNumberOfAttempts());
 				
 				pstmt.executeUpdate();
 				conn.commit();
 				
 			}
 			
 			pstmt.close();
 
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace();
 		} finally {
 
 			
 			conn.close();
 		}
 	}
 
 	public static Connection getConnection() throws Exception {
 
 		String driver = "org.gjt.mm.mysql.Driver";
 		String url = "jdbc:mysql://localhost/quizdb";
 		String username = "root";
 		String password = "root";
 
 		Class.forName(driver);
 		Connection conn = DriverManager.getConnection(url, username, password);
 		return conn;
 	}
 
 	public static void main(String[] args)throws Exception {
 
 		QuizCatalog qc = new QuizCatalog();
 		ExerciseCatalog ec = new ExerciseCatalog();
 
 		new TextToSql().SendToSql(qc, ec);
 	}
 }
 
