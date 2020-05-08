 package org.realty;
 
 
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 
 public class AddUserCommand implements Command{
 
 
 	@Override
 	public String execute(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		
 		UserJdbcDAO ad = new UserJdbcDAO();
 		String userName = request.getParameter("userName");
                 String password = request.getParameter("password");
 		String phoneNumber = request.getParameter("phoneNumber");
 	
 		User user = new User();
                 user.setName(userName);
                user.setPassword(password);
                 user.setPhoneNumber(phoneNumber);
 		ad.add(user);
 
 		return CommandFactory.getCommand("allUser").execute(request, response);
 		
 	}
 
 }
