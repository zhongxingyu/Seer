 package com.epam.lab.intouch.web.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.epam.lab.intouch.controller.exception.InputDataFormatException;
 import com.epam.lab.intouch.controller.project.ProjectController;
 import com.epam.lab.intouch.model.project.Project;
 import com.epam.lab.intouch.web.util.RequestParser;
 

 /**
  * @author Revan
  *
  */
 public class UpdateProjectServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public UpdateProjectServlet() {
         super();
         // TODO Auto-generated constructor stub
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
 		
 		ProjectController controller = new ProjectController();
 		Project updatedProject;
 		
 		try {
 			updatedProject = RequestParser.getUpdatedProject(request);
 			
 			controller.updateProject(updatedProject);
 			
 			request.setAttribute("project", updatedProject);
 			response.sendRedirect("project");
 		} catch (InputDataFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
