 package uk.ac.cam.dashboard.controllers;
 
 import java.net.SocketException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ws.rs.DELETE;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.Dur;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.property.CalScale;
 import net.fortuna.ical4j.model.property.Organizer;
 import net.fortuna.ical4j.model.property.ProdId;
 import net.fortuna.ical4j.model.property.Version;
 import net.fortuna.ical4j.util.UidGenerator;
 
 import org.hibernate.Session;
 import org.jboss.resteasy.annotations.Form;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.cl.dtg.ldap.LDAPObjectNotFoundException;
 import uk.ac.cam.cl.dtg.ldap.LDAPPartialQuery;
 import uk.ac.cam.dashboard.exceptions.AuthException;
 import uk.ac.cam.dashboard.forms.CreateDeadlineForm;
 import uk.ac.cam.dashboard.forms.GetDeadlineForm;
 import uk.ac.cam.dashboard.models.Deadline;
 import uk.ac.cam.dashboard.models.DeadlineUser;
 import uk.ac.cam.dashboard.models.Group;
 import uk.ac.cam.dashboard.models.User;
 import uk.ac.cam.dashboard.queries.DeadlineQuery;
 import uk.ac.cam.dashboard.util.HibernateUtil;
 import uk.ac.cam.dashboard.util.Util;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.ImmutableMap;
 
 @Path("/api/deadlines")
 @Produces(MediaType.APPLICATION_JSON)
 public class DeadlinesController extends ApplicationController {
 
 	// Create the logger
 	private static Logger log = LoggerFactory.getLogger(GroupsController.class);
 
 	private User currentUser;
 
 	// Get : not archived, not complete
 	@GET
 	@Path("/")
 	public Map<String, ?> getSetDeadlines(@Form GetDeadlineForm deadlineForm) {
 
 		return getDeadlines(deadlineForm, false, false);
 	}
 
 	// Get : complete
 	@GET
 	@Path("/complete")
 	public Map<String, ?> getCompletedDeadlines(
 			@Form GetDeadlineForm deadlineForm) {
 
 		return getDeadlines(deadlineForm, true, false);
 	}
 
 	// Get : archived
 	@GET
 	@Path("/archive")
 	public Map<String, ?> getArchivedSetDeadlines(
 			@Form GetDeadlineForm deadlineForm) {
 
 		return getDeadlines(deadlineForm, true, true);
 	}
 
 	// Manage
 	@GET
 	@Path("/manage")
 	public Map<String, ?> manageDeadlines(@QueryParam("title") String title,
 								@QueryParam("message") String message,
 								@QueryParam("url") String url) {
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 		
 		title = (title == null ? "" : title);
 		message = (message == null ? "" : message);
 		url = (url == null ? "" : url);
 		
 		Map<String, String> presetDeadline = ImmutableMap.of("title", title, "message", message, "url", url);
 
 		return ImmutableMap.of("deadlines", currentUser.createdDeadlinesToMap(), "errors", "undefined", "presetDeadline", presetDeadline);
 	}
 	
 	@GET
 	@Path("/manage/{id}")
 	public Map<String, ?> getDeadline(@PathParam("id") int id) {
 
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 
 		Deadline deadline = DeadlineQuery.get(id);
 
 		if (!deadline.getOwner().equals(currentUser)) {
 			return ImmutableMap.of("redirectTo", "deadlines");
 		}
 
 		return ImmutableMap.of("deadline", deadline.toMap(), "deadlineEdit",
 				deadline.toMap(), "users", deadline.usersToMap(), "errors",
 				"undefined");
 	}
 
 	// Create
 	@POST
 	@Path("/")
 	public Map<String, ?> createDeadline(@Form CreateDeadlineForm deadlineForm)
 			throws Exception {
 
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 
 		ArrayListMultimap<String, String> errors = deadlineForm.validate();
 		ImmutableMap<String, List<String>> actualErrors = Util
 				.multimapToImmutableMap(errors);
 
 		if (errors.isEmpty()) {
 			int id = deadlineForm.handleCreate(currentUser);
 			return ImmutableMap.of("redirectTo", "deadlines/manage/" + id, "success", true);
 		} else {
 			return ImmutableMap.of("deadline", deadlineForm.toMap(-1),
 					"errors", actualErrors, "success", false);
 		}
 	}
 
 	// Update
 	@POST
 	@Path("/{id}")
 	public Map<String, ?> updateDeadline(@Form CreateDeadlineForm deadlineForm,
 			@PathParam("id") int id) {
 
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 
 		ArrayListMultimap<String, String> errors = deadlineForm.validate();
 		ImmutableMap<String, List<String>> actualErrors = Util
 				.multimapToImmutableMap(errors);
 
 		if (errors.isEmpty()) {
 			Deadline deadline = deadlineForm.handleUpdate(currentUser, id);
 			return ImmutableMap.of("deadline", deadline.toMap(), "users",
 					deadline.usersToMap(), "errors", "undefined", "target",
 					"statistics", "success", true);
 		} else {
 			return ImmutableMap.of("errors", actualErrors, "deadline",
 					deadlineForm.toMap(id), "target", "edit");
 		}
 	}
 
 	// Delete
 	@DELETE
 	@Path("/{id}")
 	public Map<String, ?> deleteDeadline(@PathParam("id") int id) {
 
 		Session session = HibernateUtil.getTransactionSession();
 
 		Deadline deadline = DeadlineQuery.get(id);
 
 		session.delete(deadline);
 
 		return ImmutableMap.of("redirectTo", "deadlines/manage", "success", true);
 
 	}
 
 	// Mark as complete/not complete
 	@PUT
 	@Path("/{id}/complete")
 	public Map<String, ?> updateComplete(@PathParam("id") int id) {
 
 		DeadlineUser d = DeadlineQuery.getDUser(id);
 
 		if (d.getComplete()) {
 			d.toggleComplete(false);
 		} else {
 			d.toggleComplete(true);
 		}
 
 		return ImmutableMap.of("complete", d.getComplete());
 	}
 
 	// Find users by crsid
 	@POST
 	@Path("/queryCRSID")
 	public List<HashMap<String, String>> queryCRSId(@FormParam("q") String x) {
 
 		List<HashMap<String, String>> matches = null;
 		try {
 			matches = LDAPPartialQuery.partialUserByCrsid(x);
 		} catch (LDAPObjectNotFoundException e) {
 			log.error("Error performing LDAPQuery: " + e.getMessage());
 			return new ArrayList<HashMap<String, String>>();
 		}
 
 		return matches;
 	}
 
 	// Find groups
 	@POST
 	@Path("/queryGroup")
 	public List<ImmutableMap<String, ?>> queryCRSID(@FormParam("q") String x) {
 
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return new ArrayList<ImmutableMap<String, ?>>();
 		}
 
 		ArrayList<ImmutableMap<String, ?>> matches = new ArrayList<ImmutableMap<String, ?>>();
 
 		for (Group g : currentUser.getGroups()) {
 			matches.add(ImmutableMap.of("group_id", g.getId(), "group_name",
 					g.getTitle()));
 		}
 
 		return matches;
 	}
 
 	public Map<String, ?> getDeadlines(GetDeadlineForm deadlineForm,
 			boolean complete, boolean archived) {
 
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return ImmutableMap.of("error", e.getMessage());
 		}
 
 		ImmutableMap<String, List<String>> errors = deadlineForm.validate();
 
 		if (errors.isEmpty()) {
 			return deadlineForm.handle(currentUser, complete, archived);
 		} else {
 			return ImmutableMap.of("formErrors", errors, "data",
 					deadlineForm.toMap());
 		}
 
 	}
 
 	@GET
 	@Path("/calendar")
 	@Produces("text/calendar")
 	public Object getCalendar() throws SocketException, URISyntaxException {
 
 		try {
 			currentUser = validateUser();
 		} catch (AuthException e) {
 			return new ArrayList<ImmutableMap<String, ?>>();
 		}
 
 		Calendar calendar = new Calendar();
 		calendar.getProperties().add(new ProdId("-//OTT//OTT 1.0//EN"));
 		calendar.getProperties().add(Version.VERSION_2_0);
 		calendar.getProperties().add(CalScale.GREGORIAN);
 
 		Set<DeadlineUser> deadlineUsers = currentUser.getDeadlines();
 		if (deadlineUsers != null) {
 			for (DeadlineUser d : deadlineUsers) {
 				if (!d.getArchived()) {
 					Deadline deadline = d.getDeadline();
 
 					java.util.Calendar datetime = deadline.getDatetime();
 					Date start = datetime.getTime();
 					datetime.add(java.util.Calendar.MINUTE, 1);
 					Date end = datetime.getTime();
 					Dur dur = new Dur(start, end);
 					System.out.println("Start: " + start.toString() + " end: "
 							+ end.toString());
 					String subject = "OTTER: " + deadline.getTitle();
 
 					String hostEmail = deadline.getOwner().getCrsid()
 							+ "@cam.ac.uk";
 					UidGenerator ug = new UidGenerator("1");
 
 					DateTime eventStart = new DateTime(start);
 					VEvent event = new VEvent(eventStart, dur, subject);
 					event.getProperties().add(
 							new Organizer("MAILTO:" + hostEmail));
 					event.getProperties().add(ug.generateUid());
 					calendar.getComponents().add(event);
 				}
 			}
 		} else {
 			return Response.status(401).build();
 		}
 
 		ResponseBuilder response = Response.ok(calendar.toString());
 		response.header("Content-Disposition",
 				"attachment; filename=deadlines_for_" + currentUser.getCrsid()
 						+ ".ics");
 		return response.build();
 	}
 
 }
