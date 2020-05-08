 package servlets;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import question.Question;
 import quiz.Quiz;
 import quiz.QuizConstants;
 import user.User;
 import database.DataBaseObject;
 import database.MyDB;
 
 /**
  * Servlet implementation class ModifyQuiz
  */
 @WebServlet("/ModifyQuiz")
 public class ModifyQuiz extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ModifyQuiz() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Connection conn = MyDB.getConnection();
 
 		String id = request.getParameter("quiz");
 		String radio = request.getParameter("group1");
 		
 		if (radio.equals("history")) {
 			Statement stmt;
 			try {
 				stmt = conn.createStatement();
 				String query = "Delete FROM Quiz_Result WHERE id=" + id + ";";
 				stmt.executeUpdate(query);
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			
 		} else {
 			Statement stmt;
 			try {
 				stmt = conn.createStatement();
 				String query = "SELECT * from Quiz where id=" + id + ";";
 				ResultSet rs = stmt.executeQuery(query);
 				if (rs.next()) {
 					Quiz quiz = new Quiz(DataBaseObject.getRow(rs, QuizConstants.QUIZ_N_COLS), conn);
 					List<Question> questions = quiz.getQuestions();
 					query = "DELETE FROM Question_Attribute WHERE question_id=";
 					Statement deleter = conn.createStatement();
 					for (int i = 0; i < questions.size(); i++) {
						query += questions.get(i).getID() + ";";
						deleter.executeUpdate(query);
 					}
 					query = "DELETE FROM Question where quiz_id=" + quiz.getID() + ";";
 					deleter.executeUpdate(query);
 				}
 				
 				query = "DELETE FROM Quiz WHERE id=" + id + ";";
 				stmt.executeUpdate(query);
 				query = "DELETE FROM Quiz_Result where quiz_id=" + id + ";";
 				stmt.executeUpdate(query);
 				query = "DELETE FROM Review where quiz_id=" + id + ";";
 				stmt.executeUpdate(query);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			
 		}
 		
 		RequestDispatcher disp = request.getRequestDispatcher("AdminServlet");
 		disp.forward(request, response);
 	}
 
 }
