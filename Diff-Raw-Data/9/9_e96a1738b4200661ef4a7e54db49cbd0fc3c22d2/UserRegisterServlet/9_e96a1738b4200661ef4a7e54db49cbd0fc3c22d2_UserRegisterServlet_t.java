 package org.joe.ifttt.server.servlet;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.joe.ifttt.server.manager.UserManager;
 import org.joe.ifttt.server.servlet.model.SimpleUser;
 
 public class UserRegisterServlet extends HttpServlet {
 
 	/**
 	 * Constructor of the object.
 	 */
 	public UserRegisterServlet() {
 		super();
 	}
 
 	/**b
 	 * Destruction of the servlet. <br>
 	 */
 	public void destroy() {
 		super.destroy(); // Just puts "destroy" string in log
 		// Put your code here
 	}
 
 	/**
 	 * The doGet method of the servlet. <br>
 	 *
 	 * This method is called when a form has its tag value method equals to get.
 	 * 
 	 * @param request the request send by the client to the server
 	 * @param response the response send by the server to the client
 	 * @throws ServletException if an error occurred
 	 * @throws IOException if an error occurred
 	 */
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		
 		String username = request.getParameter("username");
 		String password = request.getParameter("password");
 		String mail = request.getParameter("mail");
 		
 		HttpSession httpSession = request.getSession();
 		SimpleUser user = new SimpleUser(username, password, mail);
 		httpSession.setAttribute("user", user);
 		
 		out.println("You enter the following data");
 		out.println("<p>Username: " + username);
 		out.println("<p>Password: " + password);
 		out.println("<p>Email	: " + mail);
 		
 		out.println("<p><form method=\"post\" action=" + 
 				 "UserRegisterServlet");
 		out.println("<p><input type=\"submit\" value=\"Confirm\" >");
 		out.println("</form>");
 		
 		out.close();
 	}
 
 	/**
 	 * The doPost method of the servlet. <br>
 	 *
 	 * This method is called when a form has its tag value method equals to post.
 	 * 
 	 * @param request the request send by the client to the server
 	 * @param response the response send by the server to the client
 	 * @throws ServletException if an error occurred
 	 * @throws IOException if an error occurred
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
 		
 		HttpSession httpSession = request.getSession();
 		
 		SimpleUser user = (SimpleUser)(httpSession.getAttribute("user"));
 		
 		storeUser(user);
 		out.println("Username : " + user.getUsername() + "\n" 
 				  + "Password : " + user.getPassword() + "\n"
 				  + "Email	  : " + user.getMail() + "\n");
 		
 	}
 
 	private void storeUser(SimpleUser user) {
 		// TODO Auto-generated method stub
		//UserManager.getInstance().createUser(user.getUsername(), user.getPassword(), user.getMail());
 	}
 
 	/**
 	 * Initialization of the servlet. <br>
 	 *
 	 * @throws ServletException if an error occurs
 	 */
 	public void init() throws ServletException {
 		// Put your code here
 	}
 
 }
