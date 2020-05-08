 package quiz;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import user.User;
 
 import database.QuizBank;
 
 /**
  * Servlet implementation class CreateQuizServlet
  */
 public class CreateQuizServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CreateQuizServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// unicode support
         if (request.getCharacterEncoding() == null) {
             request.setCharacterEncoding("UTF8");
         }
         
 		// get access to front end java classes
         ServletContext application = this.getServletContext();
 		QuizBank quizBank = (QuizBank)application.getAttribute("QuizBank");
 		HttpSession session = request.getSession();
 		User user = (User) session.getAttribute("User");
 		
 		// parse parameters for the quiz
 		int id = 0; // not used
 		String quiz_name = request.getParameter("name");
 		int creator = user.getUserID();
 		boolean random = request.getParameter("random") != null ? true : false;
	    boolean correct = request.getParameter("multiple") != null ? true : false;
	    boolean multi = request.getParameter("correction") != null ? true : false;
 	    String description = request.getParameter("description");
 	    Date createdDate = null; // not used
 	    
 	    // form a quiz object and store it in the session. It will not be submitted until the first question is created.
 		Quiz quiz = new Quiz(id, quiz_name, creator, random, correct, multi, description, createdDate);
 		session.setAttribute("Quiz", quiz);
 		
 		// TODO form a taglist object
 		
 		// initialize question number to one
 		session.setAttribute("QuestionNumber", 1);
 		
 		// redirect to the question creation page
 		RequestDispatcher rd = request.getRequestDispatcher("CreateQuestion.html");
 		rd.forward(request, response);
 		
 	}
 
 }
