 package sheridan.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import sheridan.BudgetUser;
 
 /**
  * Servlet implementation class AccountSetupServlet
  */
 @WebServlet("/AccountSetupServlet")
 public class AccountSetupServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public AccountSetupServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		BudgetUser user = new BudgetUser();
 		HttpSession session = request.getSession();
 
 		String name = request.getParameter("name");
 		float totalCash;
 		String email = request.getParameter("email");
 
 		boolean hasError = false;
 
 		try {
 			totalCash = Float.parseFloat(request.getParameter("totalCash"));
 			session.setAttribute("totalCashError", null);
 			user.setTotalCash(totalCash);
 		} catch (NumberFormatException e) {
 
			session.setAttribute("totalCashError", "unit price is not a number");
 			hasError = true;
 		} catch (NullPointerException e) {
 
 			session.setAttribute("totalCashError",
 					"please enter the unit price");
 			hasError = true;
 		}
 
 		if (name != null && !"".equals(name)) {
 			user.setName(name);
 			session.setAttribute("nameError", null);
 		} else {
			session.setAttribute("nameError", "please enter the address");
 			user.setName("");
 			hasError = true;
 		}
 
 		if (email != null && !"".equals(email)) {
 			user.setEmail(email);
 			session.setAttribute("emailError", null);
 		} else {
			session.setAttribute("emailError", "please enter the shipment date");
 			user.setEmail("");
 			hasError = true;
 		}
 
 		session.setAttribute("user", user);
 
 		if (hasError) {
 			response.sendRedirect("HomePage.jsp");
 
 			return;
 		}
 
 		response.sendRedirect("SystemPage.jsp");
 
 	}
 
 }
