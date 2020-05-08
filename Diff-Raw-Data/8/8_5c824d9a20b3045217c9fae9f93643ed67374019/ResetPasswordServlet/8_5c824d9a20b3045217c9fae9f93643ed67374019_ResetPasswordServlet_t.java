 package com.globalmesh.action.login;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.UUID;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.globalmesh.dao.UserDAO;
 import com.globalmesh.dto.User;
 import com.globalmesh.util.Constants;
 import com.globalmesh.util.MD5HashGenerator;
 import com.globalmesh.util.Utility;
 
 public class ResetPasswordServlet extends HttpServlet {
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String email = req.getParameter("resetEmale");
 		
 		try {
 			User user = UserDAO.INSTANCE.getUserByEmail(email);
 			
 			if(user != null) {
 				String newPassword = Utility.shortUUID();
 				user.setPassword(MD5HashGenerator.md5(newPassword));
 				
 				String messaageBody = MessageFormat.format(Utility.getCONFG().getProperty(Constants.RESET_PASSWORD_EMAIL_BODY), newPassword);
 				
 				Utility.sendEmail(Utility.getCONFG().getProperty(Constants.RESET_PASSWORD_EMAIL_SUBJECT), messaageBody, user.getEmail(), Utility.getCONFG().getProperty(Constants.SITE_EMAIL));
 				
 				req.setAttribute("resetMsg", Utility.getCONFG().getProperty(Constants.RESET_PASSWORD_SUCCESS_MESSAGE));
 				req.getRequestDispatcher("/resetPassword.jsp").forward(req, resp);
 			} else {
 				req.setAttribute("msgClass", Constants.MSG_CSS_ERROR);
 				req.setAttribute("message", Utility.getCONFG().getProperty(Constants.RESET_PASSWORD_WRONG_USER));
				req.getRequestDispatcher("/messages.jsp").forward(req, resp);
 			}
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 }
