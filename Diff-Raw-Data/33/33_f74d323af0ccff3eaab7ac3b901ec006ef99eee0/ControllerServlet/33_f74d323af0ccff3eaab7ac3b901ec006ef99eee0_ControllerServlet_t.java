 package ass2;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import contollers.LoginController;
 import contollers.RegistrationController;
 
 
 /**
  * Servlet implementation class ControllerServlet
  */
 @WebServlet(name="ControllerServlet",urlPatterns={"/ControllerServlet","/","/home"})
 public class ControllerServlet extends HttpServlet {
 	
 	private static final String JSP_LOGIN = "/login.jsp";
 	private static final String JSP_HOME = "/index.jsp";
 	private static final String JSP_REGISTRATION = "/registration.jsp";
	private static final String JSP_MESSAGE = "/message.jsp";
 	
 	private static final long serialVersionUID = 1L;
 	
 	private LoginController loginController;
 	private RegistrationController registrationController;
 	
 
 	public ControllerServlet() {
 
 	}
 
 	public void init(){
 
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/html");
 		String action = request.getParameter("action");
 		if ("search".equals(action)) {}
 
 		request.getRequestDispatcher("/index.jsp").forward(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ParameterManager pm = new ParameterManager(request.getParameterMap());
 		FormManager form = new FormManager();
 
 		//useful for debugging
 		pm.printAllValues();
 		
 		//default go home?
 		String forward = JSP_HOME;
 
 		if (pm.hasParameter("action")) {
 			String action = pm.getIndividualParam("action");
 			if ("add_auction".equals(action)) {}
 			else if ("halt_auction".equals(action)) {}
 			else if ("remove_auction".equals(action)) {}
 			else if ("ban_user".equals(action)) {}
 			else if ("logout".equals(action)) {}
 			else if ("login".equals(action)) {
 				loginController = new LoginController(pm);
 				
 				//check just to see if everything is entered okay
 				if (loginController.isInvalidForm()) {
 					//stay on the same page because its invalid
 					forward = JSP_LOGIN;
 					request.setAttribute("message", loginController.getMessage());
 				} else {
 					// check to see if the account is valid
 					if (loginController.isValidAccount()) {
						//TODO create a session
 						
 						//take them home
 						forward = JSP_HOME;
 					} else {
 						request.setAttribute("message", loginController.getMessage());
 					}
 				}
 			}
 			else if ("register".equals(action)) {
 				registrationController = new RegistrationController(pm);
 				if (registrationController.isInvalidForm()) {
 					forward = JSP_REGISTRATION;
 					request.setAttribute("message", registrationController.getMessage());
				} else {
					if (registrationController.isAccountAlreadyExists()) {
						forward = JSP_REGISTRATION;
						request.setAttribute("message", registrationController.getMessage());
					} else {
						forward = JSP_MESSAGE;
						request.setAttribute("message", "You have been registered, an email will be sent to you to confirm your account");
						
						//TODO Create the account in the database
						
						//TODO Email the user
					}
 				}
 				
 			}
 		}
 		
 
 		RequestDispatcher requestDispatcher = request.getServletContext().getRequestDispatcher(forward);
 		System.out.println("Forwarding to: " + forward);
 		requestDispatcher.forward(request, response);
 			}
 
 }
