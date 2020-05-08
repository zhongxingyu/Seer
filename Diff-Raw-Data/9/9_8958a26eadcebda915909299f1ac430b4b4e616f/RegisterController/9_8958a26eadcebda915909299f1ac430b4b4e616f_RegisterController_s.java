 package servlets;
 
 import helper.CourseDAO;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet implementation class RegisterController
  */
 @WebServlet("/RegisterController")
 public class RegisterController extends HttpServlet {
 private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public RegisterController() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 /**
 * @author Chris Bolton
 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 */
 protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	ServletContext ctx = this.getServletContext();
 	HttpSession session = request.getSession();
 	RequestDispatcher dispatcher = null;
 	//Getting the id from the requirement category
 	int reqMapID = Integer.parseInt(request.getParameter("reqMapId"));

 	//Creating a helper object
 	CourseDAO helper = new CourseDAO();
 	//Setting the course requirement
 	request.setAttribute("reqMap",helper.getReqList(reqMapID));
 	//Forwarding back to the register page
 	dispatcher = ctx.getRequestDispatcher("/register.jsp");
 	dispatcher.forward(request, response);
 }
 
 /**
 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 */
 protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 // TODO Auto-generated method stub
 }
 }
 
