 package Servlets;
 
 import helpers.HTMLHelper;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.SQLException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import Accounts.Account;
 import Accounts.AccountManager;
 
 import model.Quiz;
 
 
 /**
  * Servlet implementation class SinglePageQuizServlet
  */
 @WebServlet("/MultiPageQuiz")
 public class MultiPageQuiz extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private Quiz quiz;
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public MultiPageQuiz() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String quizID;
 		HttpSession session = request.getSession(true);
 		boolean practice = false;
 
 		if (request.getParameter("id") == null){
 			quizID = (String) session.getAttribute("quizID");
 		}
 		else {
 			quizID = request.getParameter("id");
 			if(quizID.startsWith("p")) {
								quizID = quizID.substring(1);
								practice = true;
 			}
 			
 		}
 		
 		session.setAttribute("quizID", quizID);
 		int questionIndex;
 		
 		try {
 			
 			if( request.getParameter("ajax_id")==null){
 			//means the cart hasnt been initialized
 			if(session.getAttribute("quiz_"+quizID) == null){
 				ServletContext sc = request.getServletContext();
 				AccountManager am = (AccountManager) sc.getAttribute("accounts");
 				quiz = new Quiz(Integer.parseInt(quizID), am.getCon());
 				session.setAttribute("quiz_"+quizID, quiz);
 				
 			}
 			else{
 				quiz = (Quiz) session.getAttribute("quiz_"+quizID);
 			}
 			quiz.setPracticeMode(practice);
 			response.setContentType("text/html");
 			PrintWriter out = response.getWriter();
 			out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
 			out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
 					      + " \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
 			out.println("<html xmlns='http://www.w3.org/1999/xhtml'>");
 			out.println("<head>");
 			out.println(HTMLHelper.printCSSLink());
 			out.println("<title>"+quiz.getQuizName()+"</title>");
 			out.println("</head>");
 			out.println("<body>");
 			out.println(HTMLHelper.printHeader((Account)request.getSession().getAttribute("account")));
 			out.println(HTMLHelper.contentStart());
 			out.println("<h1>"+quiz.getQuizName()+"</h1>");
 			out.println(HTMLHelper.contentEnd());
 			
 			if (request.getParameter("questionIndex") != null){
 				questionIndex = Integer.parseInt(request.getParameter("questionIndex"));
 				System.out.println("this question index is not null");
 				if (questionIndex >= quiz.getQuestions().size() -2){
 					out.println("<form action=\"SolveServlet\" method=\"post\">");
 				}
 				
 				else{
 					out.println("<form action=\"MultiPageQuiz\" method=\"post\">");
 				}
 			}
 			
 			else if(quiz.getQuestions().size() == 1){
 				out.println("<form action=\"SolveServlet\" method=\"post\">");
 			}
 			
 			else{
 				out.println("<form action=\"MultiPageQuiz\" method=\"post\">");
 			}
 			
 			if(request.getParameter("startTime") == null){
 				out.println("<input name=\"startTime\" type=\"hidden\" value=\"" + System.currentTimeMillis()+"\"/>");
 				questionIndex = 0;
 				out.println("<input name = \"questionIndex\" type=\"hidden\" value=\"" +questionIndex+"\"/>");
 			}
 			else {
 				out.println("<input name=\"startTime\" type=\"hidden\" value=\"" + request.getParameter("startTime")+"\"/>");
 				questionIndex = Integer.parseInt(request.getParameter("questionIndex")) + 1;
 				out.println("<input name = \"questionIndex\" type=\"hidden\" value=\"" +questionIndex+"\"/>");
 				for(int i = 0; i < questionIndex; i++){
 					model.Question quest = quiz.getQuestions().get(i);
 					String answerString, parameterString;
 					if (quest.getType()==7) {
 						parameterString = "thedata"+Integer.toString(quest.getqID());
 						answerString = (String) request.getParameter(parameterString);
 						out.println("<input name = \""+parameterString+"\" type=\"hidden\" value=\"" +answerString+"\"/>");
 					}
 					else if(quest.getType() == 5) {
 						for(int j = 0; j < quest.getNumAnswers(); j++) {
 							parameterString = Integer.toString(quest.getqID())+"_"+Integer.toString(j);
 							answerString = (String)request.getParameter(parameterString);
 							out.println("<input name = \""+parameterString+"\" type=\"hidden\" value=\"" +answerString+"\"/>");
 						} 
 					}
 					else if(quest.getType() == 6) {
 						for(int k = 0; k < quest.getNumAnswers(); k++) {
 							parameterString = "6_" + Integer.toString(quest.getqID()) + "_" + Integer.toString(k);
 							if(request.getParameter(parameterString) != null) {
 								answerString = (String)request.getParameter(parameterString);
 								out.println("<input name = \""+parameterString+"\" type=\"hidden\" value=\"" +answerString+"\"/>");
 							}
 						}
 					}
 					else {
 						parameterString = Integer.toString(quest.getType())+"_"+Integer.toString(quest.getqID());
 						answerString = request.getParameter(parameterString);
 						out.println("<input name = \""+parameterString+"\" type=\"hidden\" value=\"" +answerString+"\"/>");
 					}
 				}
 				
 			}
 			
 			out.println("<input name=\"quizID\" type=\"hidden\" value=\"" +quizID+"\"/>");
 			out.println(HTMLHelper.contentStart());
 			out.println("<h3>Question "+ (questionIndex+1) +"</h3>");
 			out.println(quiz.getQuestions().get(questionIndex).toHTMLString()); // all the ids in the input fields must be unique
 			if (quiz.isImmediateCorrection()) {
 				if (quiz.getQuestions().get(questionIndex).getType()!=7) {
 				
 					String stringID = quiz.getQuestions().get(questionIndex).getType()+"_"+quiz.getQuestions().get(questionIndex).getqID();
 					out.println(Quiz.ajaxHTMLText(questionIndex,stringID,quiz.isOnePageMultiPage(),quiz.getQuestions().get(questionIndex).getType() ));
 					
 				}
 			}
 			if (questionIndex == quiz.getQuestions().size() -1){
 				out.println("<br /><input type=\"submit\" value=\"Score Exam\"/>");
 			}
 			else {
 				out.println("<br /><input type=\"submit\" value=\"Next Question\"/>");
 			}
 			out.println("</form>");
 			out.println(HTMLHelper.contentEnd());
 			out.println("</body>");
 			out.println("</html>");
 			
 			
 		}
 			else{
 				int i = Integer.parseInt(request.getParameter("ajax_id"));
 
 				String text = quiz.getQuestions().get(i).getCorrectAnswers();
 
 				text = text.trim();
 				response.setContentType("text/plain");  // Set content type of the response so that jQuery knows what it can expect.
 				response.setCharacterEncoding("UTF-8"); // You want world domination, huh?
 				response.getWriter().write(text);       // Write response body.
 
 			}
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request,response);
 	}
 }
 
 
