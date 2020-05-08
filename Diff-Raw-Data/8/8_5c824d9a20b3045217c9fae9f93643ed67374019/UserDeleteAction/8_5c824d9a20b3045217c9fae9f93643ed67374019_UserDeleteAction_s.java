 /**
  * 
  */
 package com.globalmesh.action.user;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.globalmesh.dao.UserDAO;
 import com.globalmesh.dto.User;
 import com.globalmesh.util.Constants;
 import com.globalmesh.util.Utility;
 
 /**
  * @author Dil
  *
  */
 @SuppressWarnings("serial")
 public class UserDeleteAction extends HttpServlet {
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String id = req.getParameter("id");
 		try {
 			User u = UserDAO.INSTANCE.getUserById(id);
 			UserDAO.INSTANCE.remove(id);
 			String message = MessageFormat.format(Utility.getCONFG().getProperty(Constants.USER_REMOVED_MESSAGE), u.getFirstName()+ " " + u.getLastName());
 			req.setAttribute("msgClass", Constants.MSG_CSS_WARNING);
 			req.setAttribute("message", message);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    //resp.sendRedirect("/TodoApplication.jsp");
 	}
 }
