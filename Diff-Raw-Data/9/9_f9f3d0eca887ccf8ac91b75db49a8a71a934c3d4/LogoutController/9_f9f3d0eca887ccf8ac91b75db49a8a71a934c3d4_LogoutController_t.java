 package org.softwaresynthesis.mytalk.server.authentication.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import javax.security.auth.login.LoginContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.softwaresynthesis.mytalk.server.AbstractController;
 
 /**
  * Controller che gestisce il logout
  * dal sistema MyTalk
  * 
  * @author 	Andrea Meneghinello
  * @version	3.0
  */
 public final class LogoutController extends AbstractController 
 {
 	/**
 	 * Esegue l'operazione di logout dal
 	 * sistema MyTalk
 	 */
 	@Override
 	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws IOException 
 	{
 		HttpSession session = null;
 		LoginContext context = null;
 		PrintWriter writer = null;
 		String result = null;
 		try
 		{
 			session = request.getSession(false);
 			context = (LoginContext)session.getAttribute("context");
 			context.logout();
 			result = "true";
 		}
 		catch (Exception ex)
 		{
			result = "null";
 		}
 		finally
 		{
 			if (session != null)
 			{
 				session.invalidate();
 			}
 			writer = response.getWriter();
 			writer.write(result);
 		}
 	}
 }
