 package com.epam.lab.intouch.web.servlet;
 
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.intouch.dao.exception.DAOException;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.service.member.MemberService;
 
 /**
  * 
  * @author Revan
  *
  */
 
 public class MemberServlet extends HttpServlet {
 	private static final long serialVersionUID = -3148083755614631111L;
 	
     public MemberServlet() {
         super();
     }
     
 	@Override
 	public void init() throws ServletException {
 		super.init();
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String login = request.getParameter("login");
 
 		
 		Member result = null;
 		try {
 			result = new MemberService().getById(login);
 		} catch (DAOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 			
 		if (result != null) {
 			request.setAttribute("member", result);
 			RequestDispatcher dispatcher = request.getRequestDispatcher("member_profile.jsp");
 			dispatcher.forward(request, response);
 		} else {
 			response.sendRedirect("index.html");
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 			
 	}
 }
