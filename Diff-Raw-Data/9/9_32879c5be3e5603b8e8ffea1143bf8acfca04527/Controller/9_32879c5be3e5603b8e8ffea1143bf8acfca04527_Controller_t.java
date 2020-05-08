 package webapp.help.controller;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import javax.servlet.http.*;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 
 import webapp.help.dao.Model;
 import webapp.help.utility.BrowserType;
 
 import com.google.appengine.api.users.User;
 @SuppressWarnings("serial")
 public class Controller extends HttpServlet {	
 	Model model;
 	
 	@Override
 	public void init(){
 		model = new Model();
 		Action.add(new AddContactAction(model));
 		Action.add(new EditContactAction(model));
 		Action.add(new ViewCategoryAction(model));
 		Action.add(new ViewContactAction(model));
 		Action.add(new SendMessageAction(model));
 		Action.add(new LoginAction(model));
 		Action.add(new UpdateContact(model));
 		Action.add(new StartAdd(model));
 		System.err.println("initialized .... ");
 	}
 	@Override
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException , ServletException {
 		doGet(req, resp);
 	}
 	
 	@Override
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException , ServletException {
 		String nextPage = performTheAction(req);
 		detectBrowser(req);
 		sendToNextPage(nextPage,req,resp);
 	}
 	
 	private String performTheAction(HttpServletRequest request) {
 	    String      servletPath = request.getServletPath();
 	    UserService userService = UserServiceFactory.getUserService();
 	    User 		user 		= userService.getCurrentUser(); 
 	    String      actionName  = getActionName(servletPath);
 
 	    // first landing
 	    if(actionName == null || actionName.length() == 0){
 	    	if(user == null){
 	    		if(detectBrowser(request) == BrowserType.Desktop){
 	    			return "landing.jsp";
 	    		}else{
 	    			return userService.createLoginURL("/cellView/menu.jsp");
 	    		}
 	    	}
 	    	else{
 	    		if(detectBrowser(request) == BrowserType.Desktop){
 	    			return "viewCategory.do";
 	    		}else{
 	    			return "/cellView/menu.jsp";
 	    		}
 	    	}
 	    }
 	    
 	    // trying to access any other page and if not logged in, must log in.
 	    if(user == null){
 	    	request.setAttribute("redirect", "/"+actionName);
 	    	actionName = LoginAction.actionName;
 	    }
 	    
 	    
 		return Action.perform(actionName,request);
 	}
 	
 	private String getActionName(String path) {
         int slash = path.lastIndexOf('/');
         return path.substring(slash+1);
     }
 	
 	 private void sendToNextPage(String nextPage, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 	    	// System.out.println("nextPage="+nextPage);
 	    	String safePage;
 	    	if (nextPage == null) {
 	    		response.sendError(HttpServletResponse.SC_NOT_FOUND,request.getServletPath());
 	    		return;
 	    	}
 	    	
 	    	if (nextPage.charAt(0) == '/') {
 				String host  = request.getServerName();
 				String port  = ":"+String.valueOf(request.getServerPort());
 				if (port.equals(":80")) port = "";
 				response.sendRedirect("http://"+host+port+nextPage);
 				return;
 	    	}
 	    	if(nextPage.length()>7){
 	    		if(nextPage.substring(0, 7).equals("http://")){
 	    			safePage = "https://" + nextPage.substring(7, nextPage.length());
 	    			response.sendRedirect(safePage);
 	    			return;
 	    		}
 	    		if(nextPage.substring(0, 8).equals("https://")){
 	    			response.sendRedirect(nextPage);
 	    			return;
 	    		}
 	    	}
 	   		RequestDispatcher d = request.getRequestDispatcher("/"+nextPage);
 	   		d.forward(request,response);
 	 }
 	 private static Pattern mobileDevicePattern = Pattern.compile(".*(iphone|ipod|blackberry|android|palm|windows\\s+ce).*");
 	 public static BrowserType detectBrowser(HttpServletRequest request){
 		 Matcher matcher = mobileDevicePattern.matcher(request.getHeader("User-Agent").toLowerCase());
		 if(matcher.matches()){			 
 			 return BrowserType.Mobile;
 		 }
 		 else
 			 return BrowserType.Desktop;
 	 }
 }
