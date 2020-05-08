 package project_control.controllers;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.ConstraintViolationException;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.PathParam;
 
 import project_control.core.*;
 import project_control.models.*;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.sun.jersey.api.view.Viewable;
 
 @Path("lists")
 public class ListsController extends AbstractController {
 
 	
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	public Response index(@QueryParam("email") String email,
 						@QueryParam("type") String type) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("users", getUsers());
 		map.put("tasks", getTasks(email, type));
 		map.put("activePage", 4);
 		map.put("email", email);
 		map.put("type", type);
 		map.put("title", "List of tasks");
		map.put("page", "lists/index.jsp");
 		return Response.ok(new Viewable("/lists/router", map)).build();
 	}
 	
 
 	
 	public List<User> getUsers() {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(User.class);
 		try {
 			List<User> results = (List<User>) q.execute();
 			System.err.println(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 	
 	public List<Task> getTasks(@PathParam("email") String email,
 						@PathParam("type") String type) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(Task.class);
 		try {
 			if(email != null){
 		
 				if(type.equals("created")){
 					q.setFilter("created == '"+email+"'");
 				}else if(type.equals("assigned")){
 					q.setFilter("assigned == '"+email+"'");
 				}else if(type.equals("fixed")){
 					q.setFilter("fixed == '"+email+"'");
 				}
 			}
 			List<Task> results = (List<Task>) q.execute();
 			System.err.println(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 	
 }
