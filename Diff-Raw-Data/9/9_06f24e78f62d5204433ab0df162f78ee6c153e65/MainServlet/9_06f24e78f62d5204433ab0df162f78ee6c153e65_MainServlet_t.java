 package servlets;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import utils.MockCreation;
 
 import model.Company;
 import model.CompanyType;
 import model.User;
 
 import daos.CompanyDao;
 import daos.UserDao;
 
 /**
  * Servlet implementation class MonitorWebApp
  */
 public class MainServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private static final String HTTP_METHOD_GET = "GET";
 	private static final String HTTP_METHOD_POST = "POST";
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public MainServlet() {
 		super();
 
 		/*
 		 * Create new DB helper. This DB helper instance will supply us web
 		 * application info and statistics.
 		 */
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
		List<User> users = MockCreation.createMockUsers(); 
 
 		UserDao.getInstance().addUsers(users);
 
 		String servletPath = request.getServletPath();
 
 		yada(request, response);
 
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		doGet(request, response);
 	}
 
 	private void yada(HttpServletRequest request, HttpServletResponse response)  {
 
 		RequestDispatcher applicationListDispatcher;
 
 		applicationListDispatcher = request.getRequestDispatcher("/mainPage.jsp");
 
 		try {
 			applicationListDispatcher.forward(request, response);
 		} catch (ServletException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
