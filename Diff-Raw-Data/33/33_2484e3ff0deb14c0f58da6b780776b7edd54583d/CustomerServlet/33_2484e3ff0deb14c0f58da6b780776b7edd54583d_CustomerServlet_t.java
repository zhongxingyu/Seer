 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import DataAccess.ComponentDataBean;
 import Models.Build;
 import Models.Component;
 import Models.Customer;
 
 /**
  * Servlet implementation class CustomerServlet
  */
 @WebServlet("/CustomerServlet")
 public class CustomerServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CustomerServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			
 		// get parameters from the request
 		String firstName = request.getParameter("firstName");
 		String lastName = request.getParameter("lastName");
 		String email = request.getParameter("email");
 		String address1 = request.getParameter("address1");
 		String address2 = request.getParameter("address2");
 		String zipcode = request.getParameter("zipcode");
 		String city = request.getParameter("city");
 		String state = request.getParameter("state");
		String password = request.getParameter("password");
 
 		// add to customer bean
		Customer customer = new Customer (firstName, lastName, email, password, address1, address2, zipcode, city, state);
 		
 		// store the Customer object in the request object
 		request.setAttribute("customer", customer);
 		// forward request and response objects to JSP page
 		String url = "/main.jsp";
 		RequestDispatcher dispatcher =
 		     getServletContext().getRequestDispatcher(url);
 		dispatcher.forward(request, response);	
 	}
 
 }
