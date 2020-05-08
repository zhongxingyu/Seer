 package session;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import model.User;
 import model.UsersList;
 
 @WebServlet("/search")
 public class SearchController extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response) 
 			throws ServletException, IOException {
		System.out.println(request.getParameter("search"));
 		RequestDispatcher requestDispacher = request.getRequestDispatcher("users.jsp");
 		request.setAttribute("users", search(request.getParameter("search")));
 		requestDispacher.forward(request, response);
 	}
 	
 	private List<User> search(String searchTerm) {
 		List<User> listOfUsers = UsersList.getUsers();
 		List<User> usersToReturn = new ArrayList<User>();
 		
 		for(User u : listOfUsers) {
 			if(u.getUsername().contains(searchTerm))
 				usersToReturn.add(u);
 		}
		System.out.println(usersToReturn.size());
 		return usersToReturn;
 	}
 }
