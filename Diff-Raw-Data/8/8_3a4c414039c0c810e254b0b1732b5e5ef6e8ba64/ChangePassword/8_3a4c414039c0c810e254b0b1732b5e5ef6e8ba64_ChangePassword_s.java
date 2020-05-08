 package org.frederickiwla.utils;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.mongodb.DBObject;
 
 /**
  * Servlet implementation class ChangePassword
  */
 @WebServlet("/ChangePassword")
 public class ChangePassword extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ChangePassword() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String username = request.getParameter("username");
 		String oldPassword = request.getParameter("oldPassword");
 		String newPassword = request.getParameter("newPassword");
		
 		RSODataUtils utils = new RSODataUtils();
 		DBObject rso = utils.getRSO(username);
 		
 		if (utils.checkPassword(username, oldPassword)) {
 		
 			utils.changePassword(username, newPassword);
 			
 			StringBuffer buf = new StringBuffer();
 			buf.append(rso.get("firstName")+",\n\n\tThank you for using RSO OnDuty.  You have changed your password for username "+username+".\n");
 	
 			utils.sendEmail((String) rso.get("email"), "Account infomation changed for RSO OnDuty!", buf.toString());
 			
 			String updateHeader = "You have successfully changed your RSO Account password!";
 			StringBuffer updateMsgBuf = new StringBuffer();
 			updateMsgBuf
 				.append("<p>You have changed you password.<br/>")
 				.append("You should receive an email relating this change.<br/>")
 				.append(" </p> <p><b>Thank you for using RSO OnDuty.</b></p>");
 			
 			request.logout();
 			
 			response.sendRedirect(response.encodeURL(request.getContextPath()+"/rso/update-account-result.jsp?username="+username+"&updateHeader="+updateHeader+"&updateMessage="+updateMsgBuf.toString()));
 
 		
 		} else {
 			String updateHeader = "Your existing password is incorrect";
 			StringBuffer updateMsgBuf = new StringBuffer();
 			updateMsgBuf
 				.append("<p>You have attempted to change the password for the account associated with "+username+".<br/>")
 				.append("You entered an incorrect existing password.<br/>")
 				.append(" </p> <p><b>Thank you for using RSO On Duty.</b></p>");
 			
 			
 			response.sendRedirect(response.encodeURL(request.getContextPath()+"/rso/update-account-result.jsp?username="+username+"&updateHeader="+updateHeader+"&updateMessage="+updateMsgBuf.toString()));
 
 			
 		}
 		
 	}
 
 
 }
