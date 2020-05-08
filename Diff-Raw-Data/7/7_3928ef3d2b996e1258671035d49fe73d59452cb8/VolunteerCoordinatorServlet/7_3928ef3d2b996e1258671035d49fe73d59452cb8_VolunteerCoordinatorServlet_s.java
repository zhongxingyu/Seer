 package com.VolunteerCoordinatorApp;
 
 import java.util.*;
 import java.io.*;
 import javax.servlet.http.*;
 import java.text.SimpleDateFormat;
 import javax.jdo.Query; 
 import javax.jdo.PersistenceManager;
 import com.google.gdata.client.*;
 import com.google.gdata.client.calendar.*;
 import com.google.gdata.data.*;
 import com.google.gdata.data.acl.*;
 import com.google.gdata.data.calendar.*;
 import com.google.gdata.data.extensions.*;
 import com.google.gdata.util.*;
 import com.google.common.collect.Maps;
 
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import java.net.URL;
 
 @SuppressWarnings("serial")
 public class VolunteerCoordinatorServlet extends HttpServlet {
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		Object taskObj = req.getAttribute("task"); 
 		Object nameObj = req.getAttribute("name"); 
 		String task;
 		String name;
 		if (taskObj == null) {
 			name = req.getParameter("name"); 
 			task = req.getParameter("task"); 
 		}
 		else {
 			name = nameObj.toString(); 
 			task = taskObj.toString(); 
 		}
 		
		if(!userExists(name)) {
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
 		} 
 		else if(task.equals("volunteer")) {
 			resp.sendRedirect("/volunteer.jsp?pageNumber=1&resultIndex=1");
 		} else if(task.equals("initiate")) {
 			resp.sendRedirect("/add.jsp"); 
 		} else if(task.equals("manage")) {
 			resp.sendRedirect("/manProj.jsp?pageNumber=1&resultIndex=1"); 
 		} else if(task.equals("dashboard")) {
 			resp.sendRedirect("/underConstruction.jsp"); 
 		} else if(task.equals("preferences")) {
 			resp.sendRedirect("/prefs.jsp?name=" 
 				+ name.substring(0, name.indexOf(" ")) + "+"
 				+ name.substring(name.indexOf(" "), name.length()).trim()
 				+ "&email=" + getEmail(name)
 				+ "&phone=" + getPhone(name)
 				+ "&reminder=" + getReminder(name)); 
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
