 package com.readytalk.olive.servlet;
 
 import com.readytalk.olive.logic.OliveLogic;
 import com.readytalk.olive.model.Project;
 import com.readytalk.olive.model.User;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.logging.Logger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * So tomcat will forward any request here that meets a certain pattern.
  * 
  * To tell tomcat how to do this, you will want to setup the web.xml file.
  * 
  * Web.xml is filled with what Sun(now Oracle) calls "deployment descriptors"
  * that tell tomcat about the code you wrote. It tells tomcat how to direct
  * requests and where resources are located.
  * 
  * So these are the entries I made for this Servlet under the WEB-INF directory,
  * in web.xml.
  * 
  * First, I have to tell the web container about my servlet, so it can be called
  * by a shorter name elsewhere in the config file:
  * 
  * <servlet> <servlet-name>OliveServlet</servlet-name>
  * <servlet-class>com.readytalk.olive.servlet.OliveServlet</servlet-class>
  * </servlet>
  * 
  * That was easy enough, but now I have to tell the container what requests
  * should go here. This isn't the easiest thing to explain, so look at the XML
  * first:
  * 
  * <servlet-mapping> <servlet-name>OliveServlet</servlet-name>
  * <url-pattern>/OliveServlet</url-pattern> </servlet-mapping>
  * 
  * This means that if tomcat is runnint at olive.readytalk.com and our root
  * context (see the other .xml file in the WEB-INF directory... that is the
  * "sun-web.xml" for more on this) is "MattsBadExample" (which it is), then any
  * requests directed to http://olive.readytalk.com/MattsBadExample/OliveServlet
  * will be mapped to this class.
  * 
  * Of course, in the index.jsp file we only refer to it locally (as index.jsp
  * sits at the root of our application, in terms of URL structure).
  * 
  * I hope that makes sense. So if you make a new Serlvet class you want a form
  * on a jsp page to reference, you must first give it a short name for the
  * container (which can actually be anything) and then you will want to map that
  * to a URL. The servlet-name values have to match, but the url-pattern and
  * servlet name can be anything, the servlet-class just has to point at the
  * fully qualified class name. That is the name of the package + the class.
  * 
  * These entries would be perfectly valid:
  * 
  * <servlet> <servlet-name>NotMyServletsNameAtAll</servlet-name>
  * <servlet-class>com.readytalk.olive.servlet.OliveServlet</servlet-class>
  * </servlet>
  * 
  * And
  * 
  * <servlet-mapping> <servlet-name>NotMyServletsNameAtAll</servlet-name>
  * <url-pattern>/What</url-pattern> </servlet-mapping>
  * 
  * This, however, is confusing, but just illustrating a point. It is practice to
  * not do this but keep it as I have.
  * 
  * @author mweaver
  */
 public class OliveServlet extends HttpServlet {
 
 	/*
 	 * This will be visible in your catalina.out file. (HINT: type
 	 * "tail -F catalina.out" to see it active in a shell).
 	 * 
 	 * Look inside the doPost method I overrode to see how to use it.
 	 */
 	private static Logger log = Logger.getLogger(OliveServlet.class.getName());
 
 	/*
 	 * Don't store anything as a member variable in the Servlet.
 	 * 
 	 * This will not work.
 	 * 
 	 * Why?
 	 * 
 	 * Well, you can really only store things in a session, or refer to them
 	 * within the scope of a doPost or doGet (or whatever you want to handle).
 	 * This is because a Servlet is "re-entrant". That means that multiple users
 	 * can call an instance at any given time. It also means that because
 	 * tomcat, not you, handles the creation and destruction of Servlet
 	 * instances, you never know if you are getting the same one.
 	 * 
 	 * If this is confusing, ask me (matt.weaver@readytalk.com) more about it.
 	 * Just know not to do this!
 	 */
 	private Object dontDoThis;
 
 	/**
 	 * The meat and potatoes of Servlets. This is where the action happens!
 	 * 
 	 * Everything inside of a web (servlet) container is controlled by "users
 	 * asking for things". Or, in other words, a users browser asking for a
 	 * resource at a given URL. If the form (see the index.jsp file) is of
 	 * method="POST" then this is the method that will be called if it points at
 	 * the URL mapped out by web.xml.
 	 * 
 	 * That entry
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		/*
 		 * The log will have .severe (very bad) .warning (uh oh) and .info
 		 * 
 		 * You probably want to always use the .info method to log.
 		 */
 		String id = request.getParameter("FormName");
 		log.info("This is a servlet responding to an Http POST request from form: "+id);
 
 		/*
 		 * These came from the form you submitted to this URL
 		 */
 		Boolean isAuthorized;
 		
 		if(id.equals("LoginUser")){
 			String username = OliveLogic.sanitize(request.getParameter("username"));
 			String password = OliveLogic.sanitize(request.getParameter("password"));
 
 			User user = new User(username, password);
 
 			isAuthorized = OliveLogic.isAuthorized(user);
 			/*
 			 * The session object is bound to your user's IP/MAC address by tomcat.
 			 * That means that it should be unique for each user. The timeout (when
 			 * tomcat destroys a session) can be configured in web.xml
 			 */
 			
 
 			/*
 			 * Don't go to crazy with this, but you can set the information learned
 			 * from the OliveLogic to an attribute that will be forwarded on
 			 * throughout the session.
 			 */
 			session.setAttribute("isAuthorized", isAuthorized);	// Do not redisplay user name (XSS vulnerability).
 			if (isAuthorized) {	// Take the user to the projects page.
 				session.setAttribute("username", username);
 				session.setAttribute("password", password);
 				response.sendRedirect("projects.jsp");
 			}
 			else {	// Keep the user on the same page.
 				response.sendRedirect("index.jsp");
 			}
 		}
 		else if(id.equals("EditUser")){
 			Object user = session.getAttribute("username");
 			String username = (String) user;
 			String newName = OliveLogic.sanitize(request.getParameter("new-name"));
 			String newEmail = OliveLogic.sanitize(request.getParameter("new-email"));
 			String newPassword = OliveLogic.sanitize(request.getParameter("new-password"));
 			User updateUser = new User(username, newPassword, newEmail, newName);
 			Boolean editSuccesfully = OliveLogic.editAccount(updateUser);
 			session.setAttribute("editSuccesfully", editSuccesfully);
 			response.sendRedirect("account.jsp");
 		}
 		else if(id.equals("AddUser")){
 			String username = OliveLogic.sanitize(request.getParameter("name"));
 			String password = OliveLogic.sanitize(request.getParameter("password"));
 			String email = OliveLogic.sanitize(request.getParameter("email"));
 			
 			User newUser = new User(username,password,email,username);
 			Boolean addSuccesfully = OliveLogic.AddAccount(newUser);
			if(addSuccesfully){
				session.setAttribute("isAuthorized", true);
				session.setAttribute("username", username);
				session.setAttribute("password", password);
				response.sendRedirect("projects.jsp");
			}
			else{
				response.sendRedirect("index.jsp");
				//TODO Add error message here
			}
 		}
			
 		else if(id.equals("AddProject")){
 			PrintWriter out = response.getWriter();
 		    response.setContentType("text/plain");
 		    out.println("Project Created. Please close this window and refresh the projects page.");
 			String projectName = OliveLogic.sanitize(request.getParameter("ProjectName"));
 			User user = new User((String)session.getAttribute("username"),(String)session.getAttribute("password"));
 			Project project = new Project(projectName, user);
 			OliveLogic.AddProject(project, user);
 		}
 		
 		
 
 	}
 	//http://www.apl.jhu.edu/~hall/java/Servlet-Tutorial/Servlet-Tutorial-Form-Data.html		
 	@Override
 	protected void doGet(HttpServletRequest request,
             HttpServletResponse response)
 			throws ServletException, IOException {
 		response.setContentType("text/html");
 		HttpSession session = request.getSession();
 		session.setAttribute("projectTitle", OliveLogic.sanitize(request.getParameter("projectTitle")));
 		String page = OliveLogic.sanitize((String)request.getParameter("page"))+".jsp";
 		response.sendRedirect(page);
 	
 	
 	}
 }
 
