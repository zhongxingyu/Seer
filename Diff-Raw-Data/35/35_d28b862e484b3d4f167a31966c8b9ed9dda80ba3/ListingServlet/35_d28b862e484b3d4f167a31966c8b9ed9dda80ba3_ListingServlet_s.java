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
 
 @SuppressWarnings("serial")
 public class ListingServlet extends HttpServlet {
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		resp.setContentType("text/plain");
 
 		String ListingID = req.getParameter("id");
 
 		Key k = KeyFactory.createKey("Listid", ListingID);
 		Listing list = Utils.getListing(k);
 
 		boolean exist = false;
 		String errorname= "Listing ID does not exist.";
 		DatastoreService dstore = DatastoreServiceFactory.getDatastoreService();
 
 		/**
 		 * Checking to see if the listing actually exists in the database
 		 */
 		Query q = new Query("Listid");
 		PreparedQuery p = dstore.prepare(q);
 
 		for (Entity e: p.asIterable()){
 			if (e.getProperty("ListId").toString().equals(req.getParameter("id"))) {
 				exist = true;
 				break;
 			}
 		}
 
 		if(exist){
 
 			PrintWriter out = resp.getWriter();
			String title = "CreateListing_Response";
 
 			out.println(
 
 					"{"+ "\"" + title +"\"" + ": { \n" +
 							"\t"+ "\"" + "status" +"\"" + ":" + "\"" + "success" +"\"" + ", \n" +
 
 
 							"\t"+ "\"" + "Listing" +"\"" + ": { \n" +
 							"\t\t"+ "\"" + "ID" +"\"" + ":" + "\"" + list.getId() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Owner" +"\"" + ":" + "\"" + list.getUser() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "ListingType" +"\""+ ":" + "\"" + list.getListingType() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Origin" +"\"" + ":" + "\"" + list.getOrigin() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Destination" +"\"" + ":" + "\"" + list.getDestination() +"\"" + ", \n"+
 							"\t\t"+ "\"" + "Date" +"\"" + ":" + "\"" + list.getDate() +"\"" +", \n"+
 							"\t\t"+ "\"" + "Time" +"\"" + ":" + "\"" + list.getTime() +"\"" +", \n"+
 							"\t\t"+ "\"" + "Seats" +"\"" + ":" + "\"" + list.getSeats() +"\"" +", \n" +
 							"\t\t"+ "\"" + "Car" +"\"" + ":" + "\"" + list.getCar() +"\"" +", \n" +
 							"\t\t"+ "\"" + "Price" +"\"" + ":" + "\"" + list.getPrice() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "TransMissionAuto" +"\"" + ":" + "\"" + list.getTransAuto() +"\"" +", \n" +
 							"\t\t"+ "\"" + "TransMissionManual" +"\"" + ":" + "\"" + list.getTransMan() +"\"" +", \n" +
 							"\t\t"+ "\"" + "Bags" +"\"" + ":" + "\"" + list.getBags() +"\"" + ", \n" +
 
 							"\t\t"+ "\"" + "SharedDriving" +"\"" + ":" + "\"" + list.getSharedDriving() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Image1" +"\"" + ":" + "\"" + list.getImage1() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Image2" +"\"" + ":" + "\"" + list.getImage2() +"\"" + ", \n" +
 
 							"} \n"+
 							"} }"
 
 
 					);
 		}
 		else{
 			PrintWriter out = resp.getWriter();
			String title = "CreateListing_Response";
 			errorname = "ListingID you entered does not exist.";
 
 			out.println(
 
 					"{"+ "\"" + title +"\"" + ": { \n" +
 
 					"\t"+ "\"" + "status" +"\"" + ":" + "\"" + "fail: " + errorname + " " + "\"" + "\n" +
 					"} }"
 
 					);
 
 		}
 
 	}
 
 	public void doPost(HttpServletRequest req,
 			HttpServletResponse resp)
 					throws ServletException, IOException {
 
 		boolean exist = false;
 		boolean isUser = false;
 		String errorname= "";
 		Listing pj= new Listing();
 		DatastoreService dstore = DatastoreServiceFactory.getDatastoreService();
 
 		/**
 		 * Checking to see if the listing id already exists in the database
 		 */
 		Query q = new Query("Listid");
 		PreparedQuery p = dstore.prepare(q);
 
 		for (Entity e: p.asIterable()){
 			if (e.getProperty("ListId").toString().equals(req.getParameter("id"))) {
 				if(e.getProperty("user").toString().equals(req.getParameter("user"))){
 					isUser = true;
 					break;
 				}
 				else if (!e.getProperty("user").toString().equals(req.getParameter("user"))){
 					errorname = "ListingID used";
 					exist = true;
 					break;
 				}
 			}
 		}
 
 		if(!exist || isUser){
 			pj.setId(req.getParameter("id"));
 			pj.setUser(req.getParameter("user"));
 			pj.setListingType(req.getParameter("listingType"));
 			pj.setOrigin(req.getParameter("origin"));
 			pj.setDestination(req.getParameter("destination"));
 			pj.setDate(req.getParameter("date"));
 			pj.setTime(req.getParameter("time"));
 			pj.setSeats(req.getParameter("seats"));
 			pj.setCar(req.getParameter("car"));
 			pj.setPrice(req.getParameter("price"));
 			pj.setTransAuto(req.getParameter("transAuto"));
 			pj.setTransMan(req.getParameter("transMan"));
 			pj.setBags(req.getParameter("bags"));
 			pj.setSharedDriving(req.getParameter("sharedDriving"));
 			pj.setImage1(req.getParameter("image1"));
 			pj.setImage2(req.getParameter("image2"));
 
 			pj.upData();
 
 
 			PrintWriter out = resp.getWriter();
 			String title = "RegisterListing_response" ;
 			out.println(
 
 					"{"+  "\"" + title + "\"" +  ":   { \n" +
 
 		  		    		   "\t"+  "\"" + "status" +"\"" + ":" +  "\"" + "successful		" +"\"" + "\n"  + 
 		  		    		   "} }"
 
 					);
 		}
 		else{
 			PrintWriter out = resp.getWriter();
 			String title = "RegisterListing_response" ;
 			out.println(
 
 					"{"+  "\"" + title +"\"" +  ":   { \n" +
 
 		  		    		   "\t"+  "\"" + "status" +"\"" + ":" +  "\"" + "fail: " + errorname + " "  + "\"" + "\n"  + 
 		  		    		   "} }"
 
 					);
 		}
 	}
 
 
 	public void doPut(HttpServletRequest req,
 			HttpServletResponse resp)
 					throws ServletException, IOException {
 
 		//		boolean exist = false;
 		//		boolean isUser = false;
 		//		String errorname= "";
 		Listing list= new Listing();
 		DatastoreService dstore = DatastoreServiceFactory.getDatastoreService();
 
 		/**
 		 * Checking to see if the listing id already exists in the database
 		 */
 		Query q = new Query("Listid");
 		PreparedQuery p = dstore.prepare(q);
 
 		PrintWriter out = resp.getWriter();
 		String title = "GetAllListings_Response";
 
 		out.println(
 
 				"{"+ "\"" + title +"\"" + ": { \n" 
 				);
 
 		for (Entity e: p.asIterable()){
 
 			out.println(
 
 							"\t"+ "\"" + "Listing" +"\"" + ": { \n" +
 							"\t\t"+ "\"" + "ID" +"\"" + ":" + "\"" + e.getProperty("ListId").toString() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Owner" +"\"" + ":" + "\"" + e.getProperty("user").toString() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "ListingType" +"\""+ ":" + "\"" + e.getProperty("listingType").toString() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Origin" +"\"" + ":" + "\"" + e.getProperty("origin").toString() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Destination" +"\"" + ":" + "\"" + e.getProperty("destination").toString() +"\"" + ", \n"+
 							"\t\t"+ "\"" + "Date" +"\"" + ":" + "\"" + e.getProperty("date").toString() +"\"" +", \n"+
 							"\t\t"+ "\"" + "Time" +"\"" + ":" + "\"" + e.getProperty("time").toString() +"\"" +", \n"+
 							"\t\t"+ "\"" + "Seats" +"\"" + ":" + "\"" + e.getProperty("seats").toString() +"\"" +", \n" +
 							"\t\t"+ "\"" + "Car" +"\"" + ":" + "\"" + e.getProperty("carType").toString() +"\"" +", \n" +
 							"\t\t"+ "\"" + "Price" +"\"" + ":" + "\"" + e.getProperty("price").toString() +"\"" + " \n" +
 							"\t\t"+ "\"" + "TransMissionAuto" +"\"" + ":" + "\"" + e.getProperty("transAuto").toString() +"\"" +", \n" +
 							"\t\t"+ "\"" + "TransMissionManual" +"\"" + ":" + "\"" + e.getProperty("transMan").toString() +"\"" +", \n" +
 							"\t\t"+ "\"" + "Bags" +"\"" + ":" + "\"" + e.getProperty("bags").toString() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "SharedDriving" +"\"" + ":" + "\"" + e.getProperty("sharedDriving").toString() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Image1" +"\"" + ":" + "\"" + e.getProperty("image1").toString() +"\"" + ", \n" +
 							"\t\t"+ "\"" + "Image2" +"\"" + ":" + "\"" + e.getProperty("image2").toString() +"\"" + ", \n" +
 							"} \n"+
 							"} }"
 					);
 
 		}
 	}
 
 }
