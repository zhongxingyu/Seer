 package com.epam.lab.intouch.web.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.epam.lab.intouch.controller.exception.DataAccessingException;
 import com.epam.lab.intouch.controller.finder.ProjectFinder;
 import com.epam.lab.intouch.model.project.Project;
 import com.epam.lab.intouch.web.util.request.parser.ProjectSearchParser;
 
 public class ProjectSearchServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	private final static Logger LOG = LogManager.getLogger(ProjectSearchServlet.class);
        
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		getServletConfig().getServletContext().getRequestDispatcher("/pages/projectSearch.jsp").forward(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		ProjectFinder finder = new ProjectFinder();
 		ProjectSearchParser parser = new ProjectSearchParser();
 		String query = parser.getQuery(request);
 
 		LOG.debug("Result query: "+query);
 		List<Project> projects = new ArrayList<Project>();
 		try {
 			projects = finder.findProjects(query);
 		} catch (DataAccessingException e) {
 			LOG.error("Problems with data accessing!", e);
 		}
 
 		request.setAttribute("projects", projects);
 		getServletConfig().getServletContext().getRequestDispatcher("/pages/projectSearch.jsp").forward(request, response);
 	}
 
 }
