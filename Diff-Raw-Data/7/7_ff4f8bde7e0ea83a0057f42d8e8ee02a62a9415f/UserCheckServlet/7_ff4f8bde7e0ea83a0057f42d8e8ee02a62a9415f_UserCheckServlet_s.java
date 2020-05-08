 package com.epam.lab.intouch.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.member.common.MemberController;
 
 /**
  * 
  * @author Revan
  * 
  */
 
 public class UserCheckServlet extends HttpServlet {
 	private static final long serialVersionUID = -3148083755614631111L;
 
 	public UserCheckServlet() {
 		super();
 	}
 
 	@Override
 	public void init() throws ServletException {
 		super.init();
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		String  memberLogin =  request.getParameter("memberLogin");
 		MemberController controller = new MemberController();
 		String resultMessage = "";
 		try {
			if (controller.checkMemberIfExists(memberLogin) == false)
				resultMessage = "OK";
 			else
				resultMessage = "Email "+memberLogin+"  is already in use.";
 		} catch (DataAccessingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		response.getWriter().println(resultMessage);
 
 	}
 
 	private class SimpleMember {
 		public String login;
 
 		public SimpleMember(String login) {
 			this.login = login;
 		}
 
 	}
 }
