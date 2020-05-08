 package ycp.edu.seniordesign.webapp.admin2;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ycp.edu.seniordesign.controller.admin.AdminChangeUserTypeController;
 
 public class AdminChangeUserTypeServlet extends HttpServlet{
 
 	private static final long serialVersionUID = 1L;
 	
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		// Must be logged in as an admin to view this page
 		if (req.getSession().getAttribute("admin") == null){
 			getServletContext().getRequestDispatcher("/view/admin/login.jsp").forward(req, resp);
 		} else {
 			req.getRequestDispatcher("/view/admin/changeUserType.jsp").forward(req, resp);
 		}	
 		
 	}
 	
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
 	{
 		// Must be logged in as an admin to view this page
 		if (req.getSession().getAttribute("admin") == null){
 			getServletContext().getRequestDispatcher("/view/admin/login.jsp").forward(req, resp);
 		} else {
 			String name = req.getParameter("name");
 			String emailAddress = req.getParameter("emailAddress");
 			String userType = req.getParameter("userType");
 			
 			AdminChangeUserTypeController controller = new AdminChangeUserTypeController();
 							
			if (req.getParameter("acceptButton") != null){
 				try {
 					if (controller.changeUserType(name, emailAddress, userType)){
 						req.setAttribute("updateMessage", "User type changed successfully");
 					} else {
 						req.setAttribute("errorMessage", "No such user exists");
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else if (req.getParameter("rejectButton") != null){
 				// TODO:
 			}
 			
 			req.getRequestDispatcher("/view/admin/changeUserType.jsp").forward(req, resp);
 		}
 	}
 }
