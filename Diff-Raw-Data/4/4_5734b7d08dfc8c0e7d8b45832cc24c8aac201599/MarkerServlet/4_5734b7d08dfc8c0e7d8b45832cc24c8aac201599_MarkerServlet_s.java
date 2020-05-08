 package com.appspot.thejobmap.server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.appspot.thejobmap.shared.MarkerObj;
 import com.appspot.thejobmap.shared.ResultObj;
 import com.appspot.thejobmap.shared.UserObj;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.gson.Gson;
 
 import java.util.Properties;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 public class MarkerServlet extends HttpServlet {
 	
 	private static final long serialVersionUID = -919160328227007218L;
 	
 	UserServlet userServlet = new UserServlet();
 	
 	/**
 	 * GET - Request of markers.
 	 */
 	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		// Initialize stuff like streams
 		res.setContentType("application/json; charset=UTF-8");
 		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(res.getOutputStream()));
 		DatastoreService db = DatastoreServiceFactory.getDatastoreService();
 		Gson gson = new Gson();
 		UserObj me = new UserObj();
 		List<MarkerObj> markers = new ArrayList<MarkerObj>();
 		
 		// Parse path
 		String path = req.getPathInfo();
 		path = (path==null?"/":path);
 		System.out.println("GET /marker"+path);
 		String[] resource = path.split("/");
 
 		// Fetch user details
 		Entity entityMe = userServlet.getUser();
 		me.convertFromEntity(entityMe);
 
 		// Handle "me"
 		if (resource.length >= 2 && "me".equals(resource[1])) {
 			resource[1] = me.email;
 		}
 		
 		// Is this a private marker?
 		Boolean privMarker = false;
 		if (resource.length >= 2 && !"random".equals(resource[1]) && !"company".equals(resource[1])) {
 			privMarker = true;
 		}
 
 		// Check privileges
 		if (privMarker && (!me.email.equals(resource[1]) || !"admin".equals(me.privileges))) {
 			writer.write(gson.toJson(new ResultObj("fail", "not enough privileges")));
 			writer.close();
 			return;
 		}
 
 		if (resource.length <= 1) {
 			// GET /marker/
 			// Return list of all public markers
 			Query q = new Query("Markers");
 			List<Entity> dbList = db.prepare(q).asList(FetchOptions.Builder.withLimit(1000));
 			for (int i=0; i < dbList.size(); i++) {
 				MarkerObj marker = new MarkerObj();
 				marker.convertFromEntity(dbList.get(i));
 				if ((entityMe == null || !me.email.equals(marker.author)) && !"admin".equals(me.privileges)) {
 					// Remove extra information if not needed
 					marker.author = null;
 				}
 				if (marker.id.equals(me.email)) {
 					marker.id = "me";
 				}
 				markers.add(marker);
 			}
 			writer.write(gson.toJson(markers));
 		}
 		else if (resource.length == 2
 				&& ("random".equals(resource[1]) || "company".equals(resource[1]))) {
 			// GET /marker/<random/company>
 			// Return list of all markers made by randoms (ordinary people) or companies
 			Query q = new Query("Markers");
 			q.addFilter("type", FilterOperator.EQUAL, resource[1]);
 			if (!"company".equals(me.privileges)) {
 				// "Only show my marker to companies"
 				q.addFilter("privacy", FilterOperator.EQUAL, "public");
 			}
 			List<Entity> dbList = db.prepare(q).asList(FetchOptions.Builder.withLimit(1000));
 			for (int i=0; i < dbList.size(); i++) {
 				MarkerObj marker = new MarkerObj();
 				marker.convertFromEntity(dbList.get(i));
 				if (entityMe != null && !"admin".equals(me.privileges) && !me.email.equals(marker.author)) {
 					// Remove extra information if not needed
 					marker.author = null;
 				}
 				markers.add(marker);
 			}
 			writer.write(gson.toJson(markers));
 		}
 		else if (resource.length == 2) {
 			// GET /marker/<id/email>
 			// Return marker details
 			Key markerKey = getMarkerKey(resource[1]);
 			Entity entityMarker = null;
 			try {
 				entityMarker = db.get(markerKey);
 			} catch (EntityNotFoundException e) {
 				writer.write(gson.toJson(new ResultObj("fail", "no such marker")));
 				writer.close();
 				return;
 			}
 			MarkerObj marker = new MarkerObj();
 			marker.convertFromEntity(entityMarker);
 			writer.write(gson.toJson(marker));
 		}
 		else {
 			throw new ServletException("Unimplemented request.");
 		}
 		writer.close();
 	}
 
 	/**
 	 * POST - Addition or update of marker.
 	 */
 	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		// Initialize stuff like streams
 		res.setContentType("application/json; charset=UTF-8");
 		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
 		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(res.getOutputStream()));
 		DatastoreService db = DatastoreServiceFactory.getDatastoreService();
 		Gson gson = new Gson();
 		UserObj me = new UserObj();
 		Entity entityMarker = null;
 		MarkerObj dbMarker = new MarkerObj();
 		
 		// Parse path
 		String path = req.getPathInfo();
 		path = (path==null?"/":path);
 		System.out.println("POST /marker"+path);
 		String[] resource = path.split("/");
 		
 		// Fetch user details
 		Entity entityMe = userServlet.getUser();
 		if (entityMe == null) {
 			writer.write(gson.toJson(new ResultObj("fail", "not logged in")));
 			writer.close();
 			return;
 		}
 		me.convertFromEntity(entityMe);
 		
 		// Handle "me"
 		if (resource.length >= 2 && "me".equals(resource[1])) {
 			resource[1] = me.email;
 		}
 		
 		// Check privileges
 		if ("random".equals(me.privileges) && (resource.length <= 1 || !me.email.equals(resource[1]))) {
 			writer.write(gson.toJson(new ResultObj("fail", "not enough privileges")));
 			writer.close();
 			return;
 		}
 		
 		// Parse input
 		MarkerObj marker = gson.fromJson(reader, MarkerObj.class);
 		reader.close();
 		
 		if (resource.length <= 1) {
 			// POST /marker/
 			// New marker
 			entityMarker = new Entity("Markers");
 			marker.updateEntity(entityMarker);
 			entityMarker.setProperty("numApply", 0);
 			entityMarker.setProperty("creationDate", new Date().getTime());
			entityMarker.setProperty("type", me.privileges);
 			entityMarker.setProperty("author", me.email);
 			marker.convertFromEntity(entityMarker);
 			if (!marker.validate()) {
 				throw new ServletException("Invalid entry.");
 			}
 			
 			// Insert in database
 			db.put(entityMarker);
 			
 			// Send response
 			writer.write(gson.toJson(new ResultObj("ok")));
 		}
 		else if (resource.length == 2) {
 			// POST /marker/<id/email>
 			// Update marker details
 			// Randoms must create their marker this way
 			try {
 				// Try first with id as numeric
 				Long id = Long.parseLong(resource[1]);
 				Key markerKey = KeyFactory.createKey("Markers", id);
 				// Fetch marker
 				try {
 					entityMarker = db.get(markerKey);
 					dbMarker.convertFromEntity(entityMarker);
 					dbMarker.extend(marker, entityMe);
 				} catch (EntityNotFoundException e) {
 					writer.write(gson.toJson(new ResultObj("fail", "no such marker")));
 					writer.close();
 					return;
 				}
 			} catch (NumberFormatException e) {
 				// If it's not numeric, it is a marker by a random
 				Key markerKey = KeyFactory.createKey("Markers", resource[1]);
 				try {
 					entityMarker = db.get(markerKey);
 					dbMarker.convertFromEntity(entityMarker);
 					dbMarker.extend(marker, entityMe);
 				} catch (EntityNotFoundException e2) {
 					// Entity does not exist in database, create a new one
 					entityMarker = new Entity("Markers", me.email);
 					marker.updateEntity(entityMarker);
 					entityMarker.setProperty("numApply", 0);
 					entityMarker.setProperty("creationDate", new Date().getTime());
 					entityMarker.setProperty("type", me.privileges);
 					entityMarker.setProperty("author", me.email);
 					dbMarker.convertFromEntity(entityMarker);
 				}
 			}
 			
 			// Update entity properties
 			if (!dbMarker.validate()) {
 				throw new ServletException("Invalid entry.");
 			}
 			dbMarker.updateEntity(entityMarker);
 			
 			// Insert/update in database
 			db.put(entityMarker);
 			
 			// Send response
 			writer.write(gson.toJson(new ResultObj("ok")));
 		}
 		else if (resource.length == 3
 				&& "apply".equals(resource[1])) {
 			// POST /marker/apply/<id>
 			// Apply for a job
 			// Sends an email to the author of the pin
 			try {
 				entityMarker = db.get(getMarkerKey(resource[2]));
 				dbMarker.convertFromEntity(entityMarker);
 			} catch (EntityNotFoundException e) {
 				writer.write(gson.toJson(new ResultObj("fail", "no such marker")));
 				writer.close();
 				return;
 			}
 			
 			// Send email
 			Properties props = new Properties();
 			Session session = Session.getDefaultInstance(props, null);
 			String msgBody = "Hej Alex!";
 
 			try {
 				Message msg = new MimeMessage(session);
 				msg.setFrom(new InternetAddress(me.email, me.name));
 				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(dbMarker.author));
 				msg.setSubject("Job Application: "+dbMarker.title);
 				msg.setText(msgBody);
 				Transport.send(msg);
 			} catch (AddressException e) {
 				throw new ServletException("AddressException.");
 			} catch (MessagingException e) {
 				throw new ServletException("MessagingException.");
 			}
 			
 			// Update numApply
 			dbMarker.incApply();
 			dbMarker.updateEntity(entityMarker);
 			db.put(entityMarker);
 			
 			// Send response
 			writer.write(gson.toJson(new ResultObj("ok")));
 		}
 		else {
 			throw new ServletException("Unimplemented request.");
 		}
 		writer.close();
 	}
 
 	/**
 	 * DELETE - Delete marker.
 	 */
 	protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		// Initialize stuff like streams
 		res.setContentType("application/json; charset=UTF-8");
 		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(res.getOutputStream()));
 		DatastoreService db = DatastoreServiceFactory.getDatastoreService();
 		Gson gson = new Gson();
 		UserObj me = new UserObj();
 		
 		// Check if logged in
 		Entity entityMe = userServlet.getUser();
 		if (entityMe == null) {
 			writer.write(gson.toJson(new ResultObj("fail", "not logged in")));
 			writer.close();
 			return;
 		}
 		me.convertFromEntity(entityMe);
 		
 		// Parse path
 		String path = req.getPathInfo();
 		System.out.println("localname: "+req.getLocalName());
 		path = (path==null?"/":path);
 		System.out.println("DELETE /marker"+path);
 		String[] resource = path.split("/");
 		
 		// Handle "me"
 		if (resource.length >= 2 && "me".equals(resource[1])) {
 			resource[1] = me.email;
 		}
 		
 		// Check privileges
 		if ((resource.length == 1 || !me.email.equals(resource[1])) && !"admin".equals(me.privileges)) {
 			writer.write(gson.toJson(new ResultObj("fail", "not enough privileges")));
 			writer.close();
 			return;
 		}
 		
 		if (resource.length == 2) {
 			// DELETE /marker/<id>
 			// Delete marker
 			
 			// Check if marker exists
 			Key markerKey = getMarkerKey(resource[1]);
 			try {
 				db.get(markerKey);
 			} catch (EntityNotFoundException e) {
 				writer.write(gson.toJson(new ResultObj("fail", "marker does not exist")));
 				writer.close();
 				return;
 			}
 			
 			// Delete marker
 			db.delete(markerKey);
 			
 			// Send response
 			writer.write(gson.toJson(new ResultObj("ok")));
 		}
 		else {
 			throw new ServletException("Unimplemented request.");
 		}
 		writer.close();
 	}
 	
 	public Key getMarkerKey(String id) {
 		try {
 			// Try first with id as numeric
 			Long num = Long.parseLong(id);
 			return KeyFactory.createKey("Markers", num);
 		} catch (NumberFormatException e) {
 			// If it's not numeric, it is a marker by a random
 			return KeyFactory.createKey("Markers", id);
 		}
 	}
 
 }
