 package org.realty;
 
 
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 public class AllUserCommand implements Command{
 
 	@Override
 	public String execute(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		
 		UserJdbcDAO ad = new UserJdbcDAO();
		List<User> users = ad.findAll;
 		request.setAttribute("users", users);
 			
 		return "index.jsp";
 		
 	}
 
 }
