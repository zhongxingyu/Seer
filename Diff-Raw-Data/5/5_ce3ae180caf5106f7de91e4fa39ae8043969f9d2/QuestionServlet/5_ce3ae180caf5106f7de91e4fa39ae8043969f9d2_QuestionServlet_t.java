 package main.java.browniepoints.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import main.java.browniepoints.model.CompositeQuestion;
 import main.java.browniepoints.model.helper.QuestionHelper;
 import main.java.browniepoints.model.helper.UserHelper;
 import main.java.browniepoints.util.Util;
 
 /**
  * Servlet implementation class QuestionServlet
  */
 public class QuestionServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public QuestionServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		Integer uid = UserHelper.getInstance().getLoggedInUid();
 		List<CompositeQuestion> questions = QuestionHelper.getInstance()
 				.getQuestionsForUser(uid);
		if (null == questions || questions.size() == 0) {
			questions = QuestionHelper.getInstance().getRandomQuestions();
		}
 
 		List<CompositeQuestion> ret = new ArrayList<CompositeQuestion>();
 		try {
 			for (CompositeQuestion q : questions) {
 				ret.add(Util.toNonRevealMode(q));
 			}
 
 			request.setAttribute("questions", Util.convertToJSON(ret));
 		} catch (CloneNotSupportedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			response.getWriter().println(e.getMessage());
 		}
 
 		Util.convertToJSON(ret, response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String like = request.getParameter("like");
 		if (like == null) {
 		} else {
 			QuestionHelper.getInstance().likeQuestion(Integer.parseInt(like));
 		}
 	}
 
 }
