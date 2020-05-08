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
 import model.Choice;
 import model.Question;
 
 public class AdminServlet extends HttpServlet
 {
 	private EnqueteService enqueteService;
 
 	public void init() throws ServletException
 	{
 		this.enqueteService = new EnqueteService();
 	}
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException
 	{
 		if (req.isUserInRole("admin")) {
 			getServletContext().getRequestDispatcher("/WEB-INF/pages/createEnquete.jsp").forward(req, resp);
 			return;
 		} else {
 			resp.sendRedirect("/final/home");
 		}
 	}
 
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 	throws ServletException, IOException
 	{
 		if(req.getParameter("e_name") != null) {
 			Enquete enquete = new Enquete();
 			enquete.setName(req.getParameter("e_name"));
 			int id = enqueteService.createEnquete(enquete.getName());
 			req.setAttribute("enquete_id", id);
 			getServletContext().getRequestDispatcher("/WEB-INF/pages/createQuestion.jsp").forward(req, resp);
 			return;
 		} else if(req.getParameter("e_index") != null) {
 			Enquete enquete = new Enquete();
 			int enquete_id = Integer.parseInt(req.getParameter("e_index"));
 			enquete.setId(enquete_id);
 			req.setAttribute("enquete_id", enquete_id);
 
 			Question question = new Question();
 			question.setEnquete(enquete);
 			question.setQuestion(req.getParameter("q_question"));
 			question.setType(Integer.parseInt(req.getParameter("q_type")));
 			int id = enqueteService.createQuestion(question);
			question.setId(id);
 			if(Integer.parseInt(req.getParameter("q_type")) == 0) {
 				String[] c_values = req.getParameterValues("c_value");
				for(int i = 0; i < c_values.length; i++) {
 					Choice choice = new Choice();
 					choice.setQuestion(question);
					choice.setChoice(c_values[i]);
 					enqueteService.createChoice(choice);
 				}
 			}
 			getServletContext().getRequestDispatcher("/WEB-INF/pages/createQuestion.jsp").forward(req, resp);
 			return;
 		}
 		resp.sendRedirect("/final/all");
 	}
 }
