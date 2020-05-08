 package com.epam.lab.intouch.web.servlet;
 
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.project.ProjectController;
 import com.epam.lab.intouch.dao.exception.DAOException;
 import com.epam.lab.intouch.model.member.Member;
 import com.epam.lab.intouch.model.project.Project;
 import com.epam.lab.intouch.service.history.HistoryService;
 
 /**
  * @author Revan
  *
  */
 public class ProjectServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ProjectServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 	
 		//long projectID = Long.parseLong(request.getParameter("projectID"));
 		final long projectID = 2L;
 		
 		final Member member = (Member) request.getSession().getAttribute("member");
 		
 		Project project = null;
 		List<Member> list = null;
 		try {
 			project = new ProjectController().getProject(projectID);
 			list = new HistoryService().getProjectHistory(project);
 		} catch (DataAccessingException | DAOException ex) {
 			// TODO Auto-generated catch block
 			ex.printStackTrace();
 		}
 		
 		if (project != null) {
 			request.setAttribute("project", project);
 			request.setAttribute("history", list);	
 			
 		} else {
 			response.sendRedirect("index.html");
 		}
 		
 		if(project.isClosed() || !member.isManager()) {
 			RequestDispatcher dispatcher = request.getRequestDispatcher("project_view.jsp");
 			dispatcher.forward(request, response);
 		} else {
 			RequestDispatcher dispatcher = request.getRequestDispatcher("project_edit.jsp");
 			dispatcher.forward(request, response);
 		}		
 	}
 	
 	
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
