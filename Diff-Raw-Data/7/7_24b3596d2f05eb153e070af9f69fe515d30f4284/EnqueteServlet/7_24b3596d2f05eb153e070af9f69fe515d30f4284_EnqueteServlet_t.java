 package servlets;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import service.EnqueteService;
 
 import model.Enquete;
 import model.Question;
 import model.Answer;
 
 public class EnqueteServlet extends HttpServlet
 {
 	private EnqueteService enqueteService;
 
 	public void init() throws ServletException
 	{
 		this.enqueteService = new EnqueteService();
 	}
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException
 	{
 		String user = req.getUserPrincipal().getName();
 
 		if (req.getParameter("id") == null) {
 			resp.sendRedirect("/final/home");
 			return;
 		}
 		int id = Integer.parseInt(req.getParameter("id"));
 		Enquete enquete = this.enqueteService.getEnqueteById(id);
 
 		int q = 0;
 		if (req.getParameter("q") != null) {
 			q = Integer.parseInt(req.getParameter("q"));
 		} else {
 			q = this.enqueteService.getIndexOfStartQuestion(user, id);
 		}
 
		String old = this.enqueteService.getOldGivenAnswer(user, enquete.getId(), enquete.getQuestion(q).getId());
 
 		req.setAttribute("enquete", enquete);
 		req.setAttribute("question", q);
 		req.setAttribute("old", old);
 		getServletContext().getRequestDispatcher("/WEB-INF/pages/enquete.jsp").forward(req, resp);
 	}
 
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException
 	{
 		String user = req.getUserPrincipal().getName();
 
 		if (req.getParameter("id") == null) {
 			resp.sendRedirect("/final/home");
 			return;
 		}
 		int id = Integer.parseInt(req.getParameter("id"));
 		Enquete enquete = this.enqueteService.getEnqueteById(id);
 
 		int q = 0;
 		if (req.getParameter("q") != null) {
 			q = Integer.parseInt(req.getParameter("q"));
 		} else {
 			q = this.enqueteService.getIndexOfStartQuestion(user, id);
 		}
 
 		Answer answer = new Answer();
 		Question question = new Question();
 		question.setId(Integer.parseInt(req.getParameter("q_id")));
 		answer.setQuestion(question);
 		answer.setEnquete(enquete);
 		answer.setUsername(user);
 		answer.setIndex(Integer.parseInt(req.getParameter("q_index")));
 		answer.setAnswer(req.getParameter("q_answer"));
 		answer.setExtra(req.getParameter("q_extra"));
 		this.enqueteService.saveAnswer(answer);
 
 		if (req.getParameter("q_last") != null) {
 			this.enqueteService.finishEnquete(user, enquete.getId());
 			resp.sendRedirect("/final/home");
 			return;
 		}
 
		String old = this.enqueteService.getOldGivenAnswer(user, enquete.getId(), enquete.getQuestion(q).getId());

 		req.setAttribute("enquete", enquete);
 		req.setAttribute("question", q);
 		req.setAttribute("old", old);
 		getServletContext().getRequestDispatcher("/WEB-INF/pages/enquete.jsp").forward(req, resp);
 	}
 }
