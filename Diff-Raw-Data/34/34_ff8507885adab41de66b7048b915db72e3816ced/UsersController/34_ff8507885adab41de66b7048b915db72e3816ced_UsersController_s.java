 package project_control.controllers;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.ConstraintViolation;
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
 
 @Path("users")
 public class UsersController extends AbstractController {
 
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	public Response index() {
 		Map<String, Object> map = new HashMap<String, Object>();
 
 		map.put("users", getUsers());
 		map.put("activePage", 2);
 		map.put("title", "Users list");
		map.put("page", "/users/index.jsp");
 		return Response.ok(new Viewable("/users/router", map)).build();
 	}
 
 	@GET
 	@Path("new")
 	@Produces(MediaType.TEXT_HTML)
 	public Response new_item(@QueryParam("user") User user) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("activePage", 2);
 		map.put("title", "Users list - New");
 		map.put("user", user);
		map.put("page", "/users/new.jsp");
 		return Response.ok(new Viewable("/users/router", map)).build();
 	}
 
 	@POST
 	@Path("create")
 	@Produces(MediaType.TEXT_HTML)
 	public Response create(@FormParam("name") String name,
 			@FormParam("email") String email, @FormParam("phone") String phone,
 			@Context HttpServletResponse servletResponse) {
 		try {
 			Calendar calendar = new Calendar();
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 			User user = new User();
 			try {
 				user.setName(name);
 				user.setEmail(email);
 				user.setPhone(phone);
 				user.setCreatedAt(new Date());
 				pm.makePersistent(user);
 				calendar.addUser(user);
 			} catch (ConstraintViolationException e) {
 				List<String> err = new LinkedList<String>();
 				Iterator<ConstraintViolation<?>> it = e.getConstraintViolations().iterator();
 				while(it.hasNext()){
 					ConstraintViolation<?> hlp = it.next();
 					err.add(hlp.getPropertyPath() +" - "+ hlp.getMessage());
 				}
 				errorMessage(err);
 				return new_item(user);
 			} finally {
 				pm.close();
 			}
 		} catch (IOException e) {
 			return onAuthorization(request, response);
 		}
 		return index();
 	}
 
 	@GET
 	@Path("edit")
 	@Produces(MediaType.TEXT_HTML)
 	public Response edit(@QueryParam("key") String key, @QueryParam("user") User user) {
 		Map<String, Object> map = new HashMap<String, Object>();
 
 		map.put("activePage", 2);
 		if(user != null){
 			map.put("user", user);
 		}else{
 			map.put("user", getUser(key));
 		}
 		map.put("title", "Users list - Edit");
		map.put("page", "/users/edit.jsp");
 		return Response.ok(new Viewable("/users/router", map)).build();
 	}
 
 	@POST
 	@Path("update")
 	@Produces(MediaType.TEXT_HTML)
 	public Response update(@FormParam("defEmail") String defEmail,
 			@FormParam("name") String name, @FormParam("email") String email,
 			@FormParam("phone") String phone,
 			@Context HttpServletResponse servletResponse) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		User u = pm.getObjectById(User.class, defEmail);
 		try {
 	        u.setName(name);
 	        u.setPhone(phone);
 	        pm.makePersistent(u);
 		}catch(ConstraintViolationException e){
 			List<String> err = new LinkedList<String>();
 			Iterator<ConstraintViolation<?>> it = e.getConstraintViolations().iterator();
 			while(it.hasNext()){
 				ConstraintViolation<?> hlp = it.next();
 				err.add(hlp.getPropertyPath() +" - "+ hlp.getMessage());
 			}
 			errorMessage(err);
 			return edit(defEmail, u);
 		} finally {
 			pm.close();
 		}
 		return index();
 	}
 
 	@POST
 	@Path("delete")
 	@Produces(MediaType.TEXT_HTML)
 	public Response delete(@FormParam("key") String key) {
 		deleteUser(key);
 		return index();
 	}
 
 	
 	@DELETE
 	@Path("{id}")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public boolean deleteUser(@PathParam("id") String id) {
 		try {
 			Calendar calendar = new Calendar();
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 			try {
 				User u = pm.getObjectById(User.class, id);
 				pm.deletePersistent(u);
 				calendar.removeUser(u);
 				return true;
 			} finally {
 				pm.close();
 			}
 
 		} catch (IOException e) {
 			onAuthorization(request, response);
 		}
 		return false;
 	}
 
 	@GET
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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
 
 	@GET
 	@Path("{key}")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public User getUser(@PathParam("key") String key) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		User u = pm.getObjectById(User.class, key);
 		return u;
 	}
 
 }
