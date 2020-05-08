 package com.epam.lab.intouch.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.exception.IllegalProjectStatusException;
 import com.epam.lab.intouch.controller.exception.PermissionException;
 import com.epam.lab.intouch.controller.member.special.ManagerController;
 import com.epam.lab.intouch.controller.project.ProjectController;
 import com.epam.lab.intouch.dao.exception.DAOException;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.model.project.Project;
 import com.epam.lab.intouch.service.member.MemberService;
 
 /**
  * @author Revan
  *
  */
 public class AddMember extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private final static Logger LOG = LogManager.getLogger(AddMember.class);
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddMember() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		final long projectId = Long.valueOf(request.getParameter("projectId"));
 
 		final String memberLogin = request.getParameter("memberLogin");
 		
 		final Member manager = (Member) request.getSession().getAttribute("member");
 		
 		try {
 			Project project = new ProjectController().getProject(projectId);
 			Member member = new MemberService().getById(memberLogin);
 		
 			new ManagerController().addMemberToProject(manager, member, project);
			
			//response.sendRedirect("project?id=" + projectId);
 		} catch (DataAccessingException | DAOException | PermissionException | IllegalProjectStatusException ex) {			
 			LOG.error("An error occurred, can't add member to project", ex);
 		}
 	}
 
 }
