 package org.realty;
 
 import static java.lang.System.out;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.realty.User;
 import org.realty.UserJdbcDAO;
 
 public class AuthenticationCommand implements Command {
 
 	@Override
 	public String execute(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		HttpSession session = request.getSession();
 		UserJdbcDAO ad = new UserJdbcDAO();
 		List<User> registeredUsers = ad.findAll();
 
		out.printf("baza  %s,%s", registeredUsers.get(0).getName(),
				registeredUsers.get(0).getPassword());
 		out.printf("zapros%s,%s", request.getParameter("name"),
 				request.getParameter("passwordt"));
 
 		Boolean fl = false;
 
 		try {
 
 			for (User user : registeredUsers) {
 
 				if (user.getName().equalsIgnoreCase(
 						request.getParameter("name"))
 						&& user.getPassword().equalsIgnoreCase(
 								request.getParameter("passwordt"))) {
 					out.println("User Authenticated");
 					fl = true;
 					UsrInfo ui = new UsrInfo();
 					ui.Login(request.getParameter("name"));
                     ui.setUserId(user.getUserId());
 
                     if(user.getAdmin()) ui.Admin();
 
 					session.setAttribute("userInfo", ui);
 
 					break;
 				} else {
 					out.println("You are not an authentic person");
 					fl = false;
 				}
 			}
 
 		} catch (Exception e) {
 			System.out.println("Exception is ;" + e);
 		}
 
 		return fl ? "RealtyServlet?command=allUser"
 				: "RealtyServlet?command=indexGuest";
 	}
 }
