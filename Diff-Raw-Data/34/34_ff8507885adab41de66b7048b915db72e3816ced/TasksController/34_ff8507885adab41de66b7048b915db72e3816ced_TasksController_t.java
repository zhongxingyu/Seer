 package project_control.controllers;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import project_control.core.PMF;
 import project_control.models.Calendar;
 import project_control.models.Task;
 import project_control.models.User;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.sun.jersey.api.view.Viewable;
 
 @Path("tasks")
 public class TasksController extends AbstractController {
 
 	@GET
 	@Produces(MediaType.TEXT_HTML)
 	public Response index() {
 		// create();
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("tasks", getTasks());
 		map.put("activePage", 3);
 		map.put("posible", possibleUser());
 		map.put("title", "Task list");
		map.put("page", "tasks/index.jsp");
 		return Response.ok(new Viewable("/tasks/router", map)).build();
 	}
 
 	@GET
 	@Path("new")
 	@Produces(MediaType.TEXT_HTML)
 	public Response new_item(@QueryParam("id") Long id, @QueryParam("task") Task t) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("activePage", 3);
 		map.put("title", "Task list - New");
 		map.put("parent", id);
 		map.put("posible", possibleUser());
 		map.put("users", getUsers());
 		map.put("task", t);
		map.put("page", "tasks/new.jsp");
 		return Response.ok(new Viewable("/users/router", map)).build();
 	}
 
 	@GET
 	@Path("{key}")
 	@Produces(MediaType.TEXT_HTML)
 	public Response show(@PathParam("key") Long key) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("activePage", 3);
 		map.put("title", "Task list - show");
 		map.put("posible", possibleUser());
 		map.put("users", getUsers());
 		map.put("task", getTask(key));
 		map.put("subtasks", getSubtask(key));
		map.put("page", "tasks/show.jsp");
 		return Response.ok(new Viewable("/users/router", map)).build();
 	}
 
 	@GET
 	@Path("{key}/edit")
 	@Produces(MediaType.TEXT_HTML)
 	public Response edit(@PathParam("key") Long key, @QueryParam("task") Task task) {
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("activePage", 3);
 		map.put("title", "Task list - Edit");
 		map.put("posible", possibleUser());
 		map.put("users", getUsers());
 		if(task != null){
 			map.put("task", task);
 		}else{
 			map.put("task", getTask(key));
 		}
		map.put("page", "tasks/edit.jsp");
 		return Response.ok(new Viewable("/users/router", map)).build();
 	}
 
 	@POST
 	@Path("update")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response update(@FormParam("key") Long key,
 			  @FormParam("title") String title,
 		      @FormParam("startAt") String startAt,
 		      @FormParam("deadLineAt") String deadLineAt,
 		      @FormParam("assigned") String assigned,
 		      @FormParam("parent") Long parent,
 		      @Context HttpServletResponse servletResponse) {
 		try {
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 			Calendar calendar = new Calendar();
 			Task t = pm.getObjectById(Task.class, key);
 			try {
 		        t.setTitle(title);
 		        DateFormat formatter = new SimpleDateFormat("MM.dd.yy");
 		        t.setStartAt(formatter.parse(startAt));
 		        t.setDeadLineAt(formatter.parse(deadLineAt));
 		        t.setAssigned(assigned);
 		        pm.makePersistent(t);
 		        calendar.updateEvent(t);
 			}catch(ConstraintViolationException e){
 				List<String> err = new LinkedList<String>();
 				Iterator<ConstraintViolation<?>> it = e.getConstraintViolations().iterator();
 				while(it.hasNext()){
 					ConstraintViolation<?> hlp = it.next();
 					err.add(hlp.getPropertyPath() +" - "+ hlp.getMessage());
 				}
 				errorMessage(err);
 				return edit(key, t);
 			} catch (ParseException e) {
 				request.setAttribute("message", e);
 				return edit(key, t);
 			} finally {
 				pm.close();
 			}
 		} catch (IOException e) {
 			return onAuthorization(request, response);
 		}
 		return show(key);
 	}
 
 	@POST
 	@Path("create")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response create(@FormParam("title") String title,
 			@FormParam("startAt") String startAt,
 			@FormParam("deadLineAt") String deadLineAt,
 			@FormParam("assigned") String assigned,
 			@FormParam("parent") Long parent,
 			@Context HttpServletResponse servletResponse) {
 		try {
 			Calendar calendar = new Calendar();
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 			Task task = new Task();
 			try {
 
 				task.setTitle(title);
 				DateFormat formatter = new SimpleDateFormat("MM.dd.yy");
 				task.setCreatedAt(new Date());
 				task.setStartAt(formatter.parse(startAt));
 				task.setDeadLineAt(formatter.parse(deadLineAt));
 				UserService userService = UserServiceFactory.getUserService();
 				com.google.appengine.api.users.User user = userService.getCurrentUser();
 				task.setCreated(user.getEmail());
 				task.setAssigned(assigned);
 				task.setParentTask(parent);
 				pm.makePersistent(task);
 				task.setCalendarEventId(calendar.addEvent(task));
 				pm.makePersistent(task);
 			}catch(ConstraintViolationException e){
 				List<String> err = new LinkedList<String>();
 				Iterator<ConstraintViolation<?>> it = e.getConstraintViolations().iterator();
 				while(it.hasNext()){
 					ConstraintViolation<?> hlp = it.next();
 					err.add(hlp.getPropertyPath() +" - "+ hlp.getMessage());
 				}
 				errorMessage(err);
 				return new_item(parent, task);
 			} catch (ParseException e) {
 				errorMessage(e);
 				return new_item(parent, task);
 			}
 			finally {
 				pm.close();
 			}
 		} catch (IOException e) {
 			return onAuthorization(request, response);
 		}
 		return index();
 	}
 
 	@POST
 	@Path("delete")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response delete(@FormParam("key") Long key) {
 		try {
 			Calendar calendar = new Calendar();
 			PersistenceManager pm = PMF.get().getPersistenceManager();
 			try {
 				Task t = pm.getObjectById(Task.class, key);
 				String event = t.getCalendarEventId();
 				pm.deletePersistent(t);
 				calendar.removeEvent(event);
 			} finally {
 				pm.close();
 			}
 		} catch (IOException e) {
 			return onAuthorization(request, response);
 		}
 		return index();
 	}
 
 	@POST
 	@Path("fix")
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public Response fix(@FormParam("key") Long key) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Task t = pm.getObjectById(Task.class, key);
 			UserService userService = UserServiceFactory.getUserService();
 			com.google.appengine.api.users.User user = userService
 					.getCurrentUser();
 			t.setFixed(user.getEmail());
 			pm.makePersistent(t);
 		} catch (ConstraintViolationException e) {
 			request.setAttribute("message", e);
 			return show(key);
 		} finally {
 			pm.close();
 		}
 
 		return show(key);
 	}
 
 	@GET
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public List<Task> getTasks() {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(Task.class);
 		try {
 			List<Task> results = (List<Task>) q.execute();
 			System.err.println(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 
 	@GET
 	@Path("calendar")
 	@Produces(MediaType.TEXT_HTML)
 	public Response calendar() {
 		//create();
 		Map<String, Object> map = new HashMap<String, Object>();
 		map.put("tasks", getTasks());
 		map.put("activePage", 5);
 		map.put("posible", possibleUser());
 		map.put("title", "Task list");
		map.put("page", "tasks/calendar.jsp");
 		return Response.ok(new Viewable("/tasks/router", map)).build();
 	}
 	
 	public boolean possibleUser(){
 		UserService userService = UserServiceFactory.getUserService();
 		com.google.appengine.api.users.User user = userService.getCurrentUser();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(User.class);
 		q.setFilter("email == '" + user.getEmail() + "'");
 		try {
 			List<User> results = (List<User>) q.execute();
 			if (results.size() > 0)
 				return true;
 			return false;
 
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
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
 
 	public Task getTask(Long key) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Task t = pm.getObjectById(Task.class, key);
 		return t;
 	}
 
 	public List<Task> getSubtask(Long key) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query q = pm.newQuery(Task.class);
 		try {
 			q.setFilter("parentTask == " + key.toString() + "");
 			List<Task> results = (List<Task>) q.execute();
 			System.out.println(results);
 			return results;
 		} finally {
 			q.closeAll();
 			pm.close();
 		}
 	}
 }
