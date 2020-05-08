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
 import com.epam.lab.intouch.dao.exception.DAOException;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.model.project.Project;
 import com.epam.lab.intouch.service.member.MemberService;
 import com.epam.lab.intouch.service.project.ProjectService;
 
 /**
  * @author Revan
  *
  */
 public class DeleteMemberServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public DeleteMemberServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		Long projectID = Long.valueOf(request.getParameter("projectID"));
 		String memberLogin = request.getParameter("memberLogin");
 		
 		Member memberToDelete = null;
 		Project project = null;
 		try {
 			memberToDelete = new MemberService().getById(memberLogin);
 			project = new ProjectService().getById(projectID);
 		} catch (DAOException e1) {
 			e1.printStackTrace();
 		}
 
 		
 		
 		Member manager = (Member) request.getSession().getAttribute("member");
 		
 		try {
 			
 			new ManagerController().removeMemberFromProject(manager, memberToDelete, project);
 
 		} catch (DataAccessingException | IllegalProjectStatusException | PermissionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
