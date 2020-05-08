 package com.epam.lab.intouch.web.servlet;
 
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.exception.IllegalProjectStatusException;
 import com.epam.lab.intouch.controller.exception.PermissionException;
 import com.epam.lab.intouch.controller.member.special.ManagerController;
 import com.epam.lab.intouch.model.member.Member;
 

 /**
  * @author Revan
  *
  */
 public class CloseProjectServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public CloseProjectServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Member manager = (Member) request.getSession().getAttribute("member");
 		Long projectID = Long.valueOf(request.getParameter("projectID"));
 		
 		try {
 			new ManagerController().closeProject(manager, projectID);
 		} catch (DataAccessingException | PermissionException | IllegalProjectStatusException ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace();
 		} 
 	}
 
 }
