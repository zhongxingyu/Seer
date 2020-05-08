 package com.inno.innowebservices;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 
 public class CommentSer extends HttpServlet {
 	
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		resp.setContentType("text/plain");
 
 		String comment = req.getParameter("id");
 		
 		Key k = KeyFactory.createKey("Commentid", comment);
 		Comment list = Utils.getComment(k);
 		
 		
 		DatastoreService dstore = DatastoreServiceFactory.getDatastoreService();
 
 		/**
 		 * Checking to see if the listing actually exists in the database
 		 */
 		Query q = new Query("Commentid");
 		PreparedQuery p = dstore.prepare(q);
 		String comments = "";
 
 	for (Entity e: p.asIterable()){
 			if (e.getProperty("ListId").equals(req.getParameter("id"))){
 				comments = comments + "\t\t"+  "\"" + "comment" +"\"" + ":" +  "\"" + e.getProperty("comment") +"\"" + ", \n";
 			}
	 
 			
 		}
 		
	comments = comments.substring(0,comments.length()-3) + "\n";
	
	
 			 PrintWriter out = resp.getWriter();
			    String title = "Comment_Get";
 			    
 			    		  out.println(
 					                
 			    				  "{"+  "\"" + title +"\"" +  ":   { \n" +
 			    				"\t"+  "\"" + "status" +"\"" + ":" +  "\"" + "success" +"\"" + ", \n"  +
 			    						  
 					                
					                "\t"+ "\"" + "Comments" +"\"" + ": { \n" +
 					                comments +
 					          
 					                "} \n"+
 					                "} }"
 					                
 					                
 					                );
 		
 		
 	}
 
 	public void doPost(HttpServletRequest req,
 			HttpServletResponse resp)
 					throws ServletException, IOException {
 
 		
 		Comment pj= new Comment();
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
 			pj.setComment(req.getParameter("comment"));
 //			pj.setLongi(req.getParameter("long"));
 
 			pj.upData();
 
 
 			PrintWriter out = resp.getWriter();
 			String title = "Comment_sent" ;
 			out.println(
 
 					"{"+  "\"" + title +"\"" +  ":   { \n" +
 
		  		    		   "\t"+  "\"" + "status" +"\"" + ":" +  "\"" + "successful	" +"\"" + "\n"  + 
 		  		    		   "} }"
 
 					);
 		
 		
 		}
 
 }
