 package com.VolunteerCoordinatorApp;
 
 import java.util.*;
 import java.io.*;
 
 import javax.servlet.http.*;
 import javax.jdo.Query; 
 import javax.jdo.PersistenceManager;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 
 @SuppressWarnings("serial")
 public class VolunteerCoordinatorServlet extends HttpServlet {
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
     throws IOException {
         doPost(req, resp); 
     }
     
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
		String queryStr = req.getQueryString();
 		Object taskObj = req.getAttribute("task"); 
 		Object nameObj = req.getAttribute("name"); 
 		String task;
 		String name;
 		if (taskObj == null) {
 			name = req.getParameter("name"); 
			if (queryStr != null && queryStr.contains("task=")) {
				task = req.getQueryString().split("task=")[1];
			} else {
				task = req.getParameter("task");
			}
			
 		}
 		else {
 			name = nameObj.toString(); 
 			task = taskObj.toString(); 
 		}
 		
 		if( name.equals( "" ) ) //Check if there's a name
 		{
 		    String redirect = "/index.jsp?name=none";
 		    resp.sendRedirect( redirect );
 		}
 		else if(!userExists(name)) { //Check if a user exists with that name
 			String splitName[] = name.split(" ");
 			String redirect = "/newUser.jsp?name=";
 			for( int i = 0; i < splitName.length; i++ )
 			{
 			    if( i > 0 )
 			        redirect += "+";
 			    redirect += splitName[i];
 			}
 			redirect += "&task=" + task;
 			resp.sendRedirect( redirect );
 		} //If so, carry out the task
 		else if(task.equals("volunteer")) {
 			resp.sendRedirect("/volunteer.jsp?pageNumber=1&resultIndex=1"
 					+ "&name=" + name);
 		} else if(task.equals("initiate")) {
 			resp.sendRedirect("/add.jsp?"
 			        + "name=" + name); 
 		} else if(task.equals("manage")) {
 			resp.sendRedirect("/manProj.jsp?pageNumber=1&resultIndex=1"
 					+ "&name=" + name); 
 		} else if(task.equals("dashboard")) {
 			resp.sendRedirect("/underConstruction.jsp?"
 			        + "name=" + name);  
 		} else if(task.equals("preferences")) {
 			resp.sendRedirect("/prefs.jsp?name=" + name
 				    + "&email=" + getEmail(name)
 				    + "&phone=" + getPhone(name)
 				    + "&reminder=" + getReminder(name)); 
 		} else { //If none of those tasks given, 'task' contains a url, so go to it instead
 			//get url that brought user here and extract the redirect url from it
 			String url = req.getQueryString().split("task=")[1];
 			url += "&name=" + name;
 			
 			resp.sendRedirect(url);
 		}
 
 	}
 	
 	private boolean userExists(String name) { 
 		PersistenceManager pm = PMF.get().getPersistenceManager(); 
 		Query query = pm.newQuery(Volunteer.class); 
 		query.setFilter("name == nameParam"); 
 		query.declareParameters("String nameParam"); 
 		List<Volunteer> results = (List<Volunteer>)query.execute(name); 
 		
 		if(results.isEmpty()) {
 			return false; 
 		} else {
 			return true; 
 		}
 	}
 	
 	private String getEmail(String name) {
 		PersistenceManager pm = PMF.get().getPersistenceManager(); 
 
 	    Key k = KeyFactory.createKey(Volunteer.class.getSimpleName(), name);
 	    Volunteer v = pm.getObjectById(Volunteer.class, k);
 	    
 		return v.getEmail();
 	}
 	
 	private String getPhone(String name) {
 		PersistenceManager pm = PMF.get().getPersistenceManager(); 
 
 	    Key k = KeyFactory.createKey(Volunteer.class.getSimpleName(), name);
 	    Volunteer v = pm.getObjectById(Volunteer.class, k);
 	    
 		return v.getPhone();
 	}
 	
 	private String getReminder(String name) {
 
 		PersistenceManager pm = PMF.get().getPersistenceManager(); 
 
 	    Key k = KeyFactory.createKey(Volunteer.class.getSimpleName(), name);
 	    Volunteer v = pm.getObjectById(Volunteer.class, k);
 	    
 		return v.getReminder();
 	}
 }
