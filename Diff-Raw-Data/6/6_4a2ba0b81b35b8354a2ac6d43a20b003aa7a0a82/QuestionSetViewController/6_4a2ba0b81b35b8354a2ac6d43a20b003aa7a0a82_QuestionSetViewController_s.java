 package uk.ac.cam.sup.controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 
 import org.hibernate.criterion.Order;
 import org.jboss.resteasy.annotations.Form;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.sup.exceptions.FormValidationException;
 import uk.ac.cam.sup.exceptions.ModelNotFoundException;
 import uk.ac.cam.sup.form.SetSearchForm;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.models.QuestionSet;
 import uk.ac.cam.sup.models.Tag;
 import uk.ac.cam.sup.models.User;
 import uk.ac.cam.sup.queries.QuestionQuery;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 import uk.ac.cam.sup.queries.TagQuery;
 import uk.ac.cam.sup.queries.UserQuery;
 
 import com.google.common.collect.ImmutableMap;
 
 @Path("/sets")
 public class QuestionSetViewController extends GeneralController {
 	
 	private static Logger log = LoggerFactory.getLogger(QuestionSetViewController.class);
 	
 	@GET
 	@Path("/")
 	@Produces("application/json")
 	public Map<String,?> produceFilteredSets (@Form SetSearchForm sf) {
 		
 		try {
 			sf.validate().parse();
 			return ImmutableMap.of(
 					"success", true,
 					"sets", sf.getSearchResults(),
 					"form", sf.toMap()
 			);
 		} catch (FormValidationException e) {
 			return ImmutableMap.of("success", false, "error", e.getMessage());
 		}
 	}
 	
 	@GET
 	@Path("/{id}")
 	@Produces("application/json")
 	public Map<String,?> produceSingleSet(@PathParam("id") int id) {
 		QuestionSet qs = QuestionSetQuery.get(id);
 		if (qs == null) {
 			throw new WebApplicationException(Response.Status.NOT_FOUND);
 		}
 		Map<String,Object> result = qs.toMap(!isCurrentUserSupervisor());
 		Boolean editable = getCurrentUser().getId().equals(qs.getOwner().getId());
 		result.put("editable", editable);
 		
		//String deadLineLink = "http://" + getServerName() + ":" + getServerPort()
		//		+ "/dashboard/supervisor/newDeadline?url=" + getCurrentUrl();
		//result.put("deadLineLink", deadLineLink);
 		return result;
 	}
 	
 	@GET
 	@Path("/{id}/import")
 	@Produces("application/json")
 	public Map<String,?> produceImportPageData(@PathParam("id") int id) {
 		List<Map<String, ?>> questions = new ArrayList<Map<String,?>>();
 		QuestionSet qs = QuestionSetQuery.get(id);
 		if (qs == null) {
 			throw new WebApplicationException(Response.Status.NOT_FOUND);
 		}
 		
 		return ImmutableMap.of(
 				"success", true,
 				"set", qs.toMap(),
 				"questions", questions
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
 		
 		List<QuestionSet> resultSets = QuestionSetQuery.all().withOwners(userlist).list();
 		
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
 	@Path("/mysets/amount")
 	@Produces("application/json")
 	public Map<String,?> produceMySetCount(){
 		try{
 			int amount = QuestionSetQuery.all().withUser(getCurrentUser()).size();
 			return ImmutableMap.of("success", true, "amount", amount);
 		} catch(Exception e) {
 			log.error("Error while trying to count the number of sets of user " + getCurrentUserID() + ". Message: " + e.getMessage());
 			return ImmutableMap.of("success", false, "error", e.getMessage());
 		}
 	}
 
 	@GET
 	@Path("/mysets/limited")
 	@Produces("application/json")
 	public Map<String,?> produceSelectionOfMySets(
 			@QueryParam("contains") Integer qid,
 			@QueryParam("page") Integer page,
 			@QueryParam("amount") Integer amount) {
 		return produceSelectionOfUserSets(getCurrentUserID(), qid, page, amount);
 	}
 	
 	@GET
 	@Path("/{crsid}/limited")
 	@Produces("application/json")
 	public Map<String,?> produceSelectionOfUserSets(
 			@PathParam("crsid") String crsid,
 			@QueryParam("contains") Integer qid,
 			@QueryParam("page") Integer page,
 			@QueryParam("amount") Integer amount) {
 		
 		if(qid == null || page == null || amount == null || crsid == null){
 			return ImmutableMap.of("success", false, "error", "An input value was null");
 		}
 		
 		User u;
 		try {
 			u = UserQuery.get(crsid);
 		} catch (ModelNotFoundException e) {
 			u = new User(crsid, false);
 			u.save();
 		}
 		List<QuestionSet> sets = QuestionSetQuery.all().withUser(u).maxResults(amount).offset((page-1)*amount).list();
 		Question q = QuestionQuery.get(qid);
 		List<Map<String,?>> resultSets = new ArrayList<Map<String,?>>();
 		
 		for(QuestionSet s: sets){
 			resultSets.add(ImmutableMap.of(
 					"set", s.toMap(!(getCurrentUser().getSupervisor() || getCurrentUser().getId().equals(u.getId()))),
 					"containsQuestion", s.getQuestions().contains(q)));
 		}
 		
 		return ImmutableMap.of("maplist", resultSets, "sucess", true);
 	}
 	
 	@GET
 	@Path("/mysets/qlimited")
 	@Produces("application/json")
 	public Map<String,?> produceOnlySetsWithQuestion(@QueryParam("qid") Integer qid) {
 		QuestionSetQuery qsq = QuestionSetQuery.all().withUser(getCurrentUser()).have(qid);
 		qsq.getCriteria().addOrder(Order.asc("name"));
 		
 		return ImmutableMap.of("sets", qsq.maplist(false));
 	}
 	
 	/**
 	 * To find the tags not in the current set. Intended for auto-completion feature only. 
 	 * 
 	 * Will return a list of all tags which contain the search term
 	 * given in strInput and which are not in the question.
 	 * 
 	 * @param strInput Format: [questionID]=[tagString]
 	 * @return
 	 */
 	@POST
 	@Path("/tagsnotin/{id}")
 	@Produces("application/json")
 	public List<Map<String, String>> getTagsNotInSet(@FormParam("q") String tag, @PathParam("id") Integer setid) {
 		List<Map<String, String>> results = new ArrayList<Map<String, String>>();
 		
 		try {
 			log.debug("Trying to get all tags containing " + tag
 					+ " which are NOT in set " + setid);
 
 			List<Tag> tags = TagQuery.all().notContainedIn(QuestionSetQuery.get(setid).getTags())
 					.contains(tag).maxResults(10).list();
 			
 			for (Tag t : tags) {
 				results.add(ImmutableMap.of("name", t.getName()));
 			}
 			
 			// The result needs to remain like this - don't change. (Unless you're gonna fix what you break...)
 			return results;
 		} catch (NullPointerException e) {
 			log.warn("There was some invalid input to the tagsNotInQuestion method: NullPointerException");
 			return results;
 		} catch (Exception e) {
 			log.warn("There was some invalid input to the tagsNotInQuestion method. Message: " + e.getMessage());
 			return results;
 		}
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
