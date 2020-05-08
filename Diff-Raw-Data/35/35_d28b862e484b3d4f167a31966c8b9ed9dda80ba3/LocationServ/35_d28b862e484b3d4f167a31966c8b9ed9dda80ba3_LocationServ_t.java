 package com.inno.innowebservices;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 
 public class LocationServ extends HttpServlet {
 
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		resp.setContentType("text/plain");
 
 		String ListingID = req.getParameter("id");
 		
 		Key k = KeyFactory.createKey("CurListid", ListingID);
 		Location list = Utils.getLocation(k);
 		
 		
 		DatastoreService dstore = DatastoreServiceFactory.getDatastoreService();
 
 		/**
 		 * Checking to see if the listing actually exists in the database
 		 */
 		Query q = new Query("id");
 		PreparedQuery p = dstore.prepare(q);
 //
 //		for (Entity e: p.asIterable()){
 //			if (e.getProperty("id").equals(req.getParameter("id")))
 //				exist = true;
 //			errorname = "Listing does not exist";
 //		}
 		
 		
 			 PrintWriter out = resp.getWriter();
			    String title = "GetLocation_Response";
 			    
 			    		  out.println(
 					                
 			    				  "{"+  "\"" + title +"\"" +  ":   { \n" +
 			    				"\t"+  "\"" + "status" +"\"" + ":" +  "\"" + "success" +"\"" + ", \n"  +
 			    						  
 					                
 					                "\t"+ "\"" + "Listing" +"\"" + ": { \n" +
 					                "\t\t"+  "\"" + "ID" +"\"" + ":" +  "\"" + list.getId() +"\"" + ", \n"  +
 					                "\t\t"+  "\"" + "Driver" +"\"" + ":" +  "\"" + list.getUser() +"\"" + ", \n"  +
 					                "\t\t"+  "\"" + "Latatude" +"\""+ ":" +   "\"" +  list.getLata() +"\"" + ", \n"  +
 
 					                "\t\t"+  "\"" + "longitude" +"\"" +  ":" +  "\"" + list.getLongi() +"\"" + "\n"+   
 					          
 					                "} \n"+
 					                "} }"
 					                
 					                
 					                );
 		
 		
 	}
 
 	public void doPost(HttpServletRequest req,
 			HttpServletResponse resp)
 					throws ServletException, IOException {
 
 		
 		Location pj= new Location();
 		DatastoreService dstore = DatastoreServiceFactory.getDatastoreService();
 
 		/**
 		 * Checking to see if the listing id already exists in the database
 		 */
 		
 
 //		for (Entity e: p.asIterable()){
 //			if (e.getProperty("").equals(req.getParameter("user")))
 //				error = true;
 //			errorname = "ListingID used";
 //		}
 
 			pj.setId(req.getParameter("id"));
 			pj.setUser(req.getParameter("user"));
 			pj.setLata(req.getParameter("lata"));
 			pj.setLongi(req.getParameter("long"));
 
 			pj.upData();
 
 
 			PrintWriter out = resp.getWriter();
			String title = "Register_location" ;
 			out.println(
 
 					"{"+  "\"" + title +"\"" +  ":   { \n" +
 
 		  		    		   "\t"+  "\"" + "status" +"\"" + ":" +  "\"" + "successful	" +"\"" + "\n"  + 
 		  		    		   "} }"
 
 					);
 		
 		
 		}
 	}
 
 	
 
