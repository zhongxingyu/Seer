 /*
  * UserServlet - creating and updating users
  * req: /user?user_id=...&action=...
  *  	-Action specifies what you want to do
  *  	-current operations:
  *  		-create: &name=... (first_last)-> initializes user 
  *  		-update: &status=... -> set (global) status (note: going red deletes status and time)
  *  		-circle_statuses: &circles=circle1.id,#;circle2.id,# (comma between circle id and status, semicolon
  *  														between different circles)
  *  		-status_loc_time: &status=...&location=...&time=...
  *  		-loc_time: &location=...&time=... (does not affect status)
  *  
  *  -Derek Salama
  */
 package com.foodcirclesserver;
 
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 
 public class UserServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -5751629299347261001L;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) {
 		String userID = req.getParameter(UserManager.USER_ID);
 		if (userID == null || userID.length() <= 0)
 			return; //invalid user parameter
 		
 		String action = req.getParameter("action");
 		if (action == null)
 			return;
 		
 		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
 		
 		Integer status;
 		String location;
 		
 		
 		if (action.equals("create")) {
 			String name = req.getParameter(UserManager.NAME);
 			String parsedName = name.replace('_', ' '); //can't do request with spaces, so use underscores and replace
 			UserManager.createUser(userID, parsedName, ds);
 		} else if (action.equals("update_status")) {
 			status = Integer.parseInt(req.getParameter(UserManager.STATUS));
 			UserManager.updateStatus(userID, status, ds);
 		} else if (action.equals("circle_statuses")) {
 			//first set status to other
 			UserManager.updateStatus(userID, UserManager.OTHER, ds);
 			String circleList = req.getParameter("circles");
 			String[] circles = circleList.split(";");
 			for(String circle : circles) {
 				String[] pair = circle.split(",");
 				Long circleID = Long.parseLong(pair[0]);
 				Integer circleStatus = Integer.parseInt(pair[1]);
 				CircleManager.setStatusForCircle(userID, circleStatus, circleID, ds);
 			}
 		} else if (action.equals("status_loc_time")) {
 			status = Integer.parseInt(req.getParameter(UserManager.STATUS));
 			UserManager.updateStatus(userID, status, ds);
 			//don't break - fall through to do location and time
 			if (action.equals("loc_time")) {
 				String timeString = req.getParameter(UserManager.DESIRED_TIME);
 				location = req.getParameter(UserManager.DESIRED_LOCATION);
 				UserManager.updateLocationAndTime(userID, timeString, location, ds);
 			}
		} else if (action.equals("loc_time")) {
			String timeString = req.getParameter(UserManager.DESIRED_TIME);
			location = req.getParameter(UserManager.DESIRED_LOCATION);
			UserManager.updateLocationAndTime(userID, timeString, location, ds);
 		} else {
 			System.out.println("User Servlet: no action matched");
 		}			
 	}
 
 }
 	
