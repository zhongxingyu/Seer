 package uk.ac.cam.sup.controllers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.hibernate.criterion.Order;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.sup.models.QuestionSet;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.queries.QuestionQuery;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 import uk.ac.cam.sup.util.SearchTerm;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/sets")
 public class QuestionSetViewController extends GeneralController {
 	
 	private static Logger log = LoggerFactory.getLogger(QuestionSetViewController.class);
 	
 	@GET
 	@Path("/")
 	@Produces("application/json")
 	public Map<String,?> produceFilteredSets (
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
 		
 		return ImmutableMap.of("sets", query.maplist());
 	}
 	
 	@GET
 	@Path("/{id}")
 	@Produces("application/json")
 	public Map<String,?> produceSingleSet(@PathParam("id") int id) {
 		QuestionSet qs = QuestionSetQuery.get(id);
 		Map<String,Object> result = qs.toMap(isCurrentUserSupervisor());
 		Boolean editable = getCurrentUser().getId().equals(qs.getOwner().getId());
 		result.put("editable", editable);
 		return result;
 	}
 	
 	@GET
 	@Path("/{id}/import")
 	@Produces("application/json")
 	public Map<String,?> produceImportPageData(@PathParam("id") int id) {
 		List<Map<String, ?>> questions; 
 		
 		questions = QuestionQuery.all().maplist();
 		
 		return ImmutableMap.of(
 				"success", true,
 				"set", QuestionSetQuery.get(id).toMap(),
 				"questions", questions,
 				"st", new SearchTerm()
 		);
 	}
 	
 	@GET
 	@Path("/{id}/{target}")
 	@Produces("application/json")
 	public Map<String,?> produceSingleSet(
 			@PathParam("id") int id,
 			@PathParam("target") String target
 	) {
 		if (target.equals("import")) { return produceImportPageData(id); }
 		@SuppressWarnings("unchecked")
 		Map<String,Object> result = (Map<String, Object>) produceSingleSet(id);
 		
 		result.put("target", target);
 		return result;
 	}
 	
 	@GET
 	@Path("/mysets")
 	@Produces("application/json")
 	public Map<String,?> produceMySets(@QueryParam("contains") Integer questionID){
 		
 		User user = getCurrentUser();
 		List<User> userlist = new ArrayList<User>();
 		userlist.add(user);
 		
 		List<QuestionSet> resultSets = QuestionSetQuery.all().withUsers(userlist).list();
 		
 		if(questionID == null) {
 			return ImmutableMap.of("sets", resultSets);
 		} else {
 			List<Map<String,?>> maplist = new ArrayList<Map<String,?>>();
 			log.debug("Trying to get all questionSets with those specially marked containing question " + questionID);
 			List<QuestionSet> haveQuestion = QuestionSetQuery.all().have(questionID).list();
 			for(QuestionSet set : resultSets) {
 				maplist.add(ImmutableMap.of(
 						"set", set.toMap(false),
 						"containsQuestion", haveQuestion.contains(set))
 				);
 			}
 			return ImmutableMap.of("maplist", maplist);
 		}
 	}
 
 	@GET
 	@Path("/mysets/limited")
 	@Produces("application/json")
 	public Map<String,?> produceSelectionOfMySets(
 			@QueryParam("contains") Integer qid,
 			@QueryParam("disp") Integer alreadyDisplayed,
 			@QueryParam("amount") Integer amount) {
 		
 		@SuppressWarnings("unchecked")
 		List<Map<String,?>> mySets = (List<Map<String,?>>) produceMySets(qid).get("maplist");
 		
 		if(mySets.size() <= alreadyDisplayed) {
 			return ImmutableMap.of("maplist", new ArrayList<Map<String,?>>(), "exhausted", true, "disp", alreadyDisplayed);
 		} else if(mySets.size() <= alreadyDisplayed + amount) {
 			return ImmutableMap.of(
 					"maplist", mySets.subList(alreadyDisplayed, mySets.size()),
 					"exhausted", true,
 					"disp", mySets.size()
 			);
 		} else {
 			return ImmutableMap.of(
 					"maplist", mySets.subList(alreadyDisplayed, alreadyDisplayed + amount),
 					"exhausted", false,
 					"disp", alreadyDisplayed + amount
 			);
 		}
 	}
 	
 	@GET
 	@Path("/mysets/qlimited")
 	@Produces("application/json")
 	public Map<String,?> produceOnlySetsWithQuestion(@QueryParam("qid") Integer qid) {
 		List<User> userList = new ArrayList<User>();
 		userList.add(getCurrentUser());
 		
 		QuestionSetQuery qsq = QuestionSetQuery.all().withUsers(userList).have(qid);
 		qsq.getCriteria().addOrder(Order.asc("name"));
 		
 		return ImmutableMap.of("sets", qsq.maplist(false));
 	}
 	
 	/*
 	 * Dummy controllers. Don't return anything. Only there to satisfy the router.
 	 */
 	@GET
 	@Path("/add")
	public Map<String,String> dummy1(){
		return ImmutableMap.of();
	}
 }
