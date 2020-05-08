 package uk.ac.cam.sup.controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import uk.ac.cam.sup.models.QuestionSet;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 import uk.ac.cam.sup.util.WorldStrings;
 
@Path(WorldStrings.URL_PREFIX + "/sets")
 public class QuestionSetController {
 	
 	@GET
	@Path("/json")
 	@Produces("application/json")
 	public List<QuestionSet> produceFilteredJSON (
 			@QueryParam("tags") String tags,
 			@QueryParam("owners") String users,
 			@QueryParam("star") boolean star,
 			@QueryParam("supervisor") Boolean supervisor,
 			@QueryParam("after") Long after,
 			@QueryParam("before") Long before,
 			@QueryParam("durmin") Integer minduration,
 			@QueryParam("durmax") Integer maxduration
 	) {
 		QuestionSetQuery query = QuestionSetQuery.all();
 		
 		if (tags != null) {
 			String[] tagstrings = tags.split(",");
 			List<Tag> tagset = new ArrayList<Tag>();
 			for (String t: tagstrings) {
 				tagset.add(new Tag(t));
 			}
 			query.withTags(tagset);
 		}
 		
 		if (users != null) {
 			String[] userstrings = users.split(",");
 			List<User> userset = new ArrayList<User>();
 			for (String u: userstrings) {
 				userset.add(new User(u));
 			}
 			query.withUsers(userset);
 		}
 		
 		if (star) {	query.withStar(); }
 		if (supervisor != null) {
 			if (supervisor) { query.bySupervisor(); }
 			else { query.byStudent(); }
 		}
 		
 		if (after != null) { query.after(new Date(after)); }
 		if (before != null) { query.before(new Date(before)); }
 		
 		if (minduration != null) { query.minDuration(minduration); }
 		if (maxduration != null) { query.maxDuration(maxduration); }
 		
 		return query.list();
 	}
 	
 }
