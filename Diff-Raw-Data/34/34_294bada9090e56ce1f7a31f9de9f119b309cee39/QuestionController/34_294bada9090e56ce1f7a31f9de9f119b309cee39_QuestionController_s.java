 package uk.ac.cam.sup.controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.sup.HibernateUtil;
 import uk.ac.cam.sup.models.Question;
 import uk.ac.cam.sup.queries.QuestionQuery;
 import uk.ac.cam.sup.util.WorldStrings;
 
 import com.google.common.collect.ImmutableMap;
 
@Path("/q")
 public class QuestionController {
 	private static Logger log = LoggerFactory.getLogger(QuestionController.class);
 	
 	@GET
	@Path(WorldStrings.URL_PREFIX + "/json")
 	@Produces("application/json")
 	public List<?> produceFilteredJSON(
 			@QueryParam("tags") String tags,
 			@QueryParam("owners") String owners,
 			@QueryParam("star") Boolean star,
 			@QueryParam("supervisor") Boolean supervisor,
 			@QueryParam("after") Long after,
 			@QueryParam("before") Long before,
 			@QueryParam("usagemin") Integer usageMin,
 			@QueryParam("usagemax") Integer usageMax,
 			@QueryParam("parent") Integer parentId,
 			@QueryParam("durmax") Integer durMax,
 			@QueryParam("durmin") Integer durMin
 			){
 		
 		log.debug("Getting new QuestionQuery");
 		QuestionQuery qq = QuestionQuery.all();
 		
 		log.debug("Filtering for tags");
 		if(tags != null){
 			List<String> lTags = Arrays.asList(tags.split(","));
 			qq.withTagNames(lTags);
 		}
 		
 		log.debug("Filtering for owners");
 		if(owners != null){
 			List<String> lUsers = Arrays.asList(owners.split(","));
 			qq.withUserIDs(lUsers);
 		}
 		
 		log.debug("Filtering for star, role...");
 		if(star != null && star) { qq.withStar(); }
 		if(star != null && !star) { qq.withoutStar(); }
 		if(supervisor != null && supervisor) { qq.bySupervisor(); }
 		if(supervisor != null && !supervisor) { qq.byStudent(); }
 		
 		log.debug("Filtering for date...");
 		if(after != null) { qq.after(new Date(after)); }
 		if(before != null) { qq.before(new Date(before)); }
 		
 		log.debug("Filtering for usages");
 		if(usageMin != null) { qq.minUsages(usageMin); }
 		if(usageMax != null) { qq.maxUsages(usageMax); }
 		
 		log.debug("Fileting for parentID");
 		if(parentId != null) { qq.withParent(parentId); }
 		
 		log.debug("Filtering for duration");
 		if(durMax != null) { qq.maxDuration(durMax); }
 		if(durMin != null) { qq.minDuration(durMin); }
 			
 		return qq.list();
 	}
 
 	private Question getQuestion(int id) {
 		Session session = HibernateUtil.getTransaction();
 		
 		Question r = (Question) session
 				.createQuery("from Question where id = :id")
 				.setParameter("id", id)
 				.uniqueResult();
 		
 		session.getTransaction().commit();
 		return r;
 	}
 	
 	@GET
	@Path(WorldStrings.URL_PREFIX + "/{id}/json")
 	@Produces("application/json")
 	public Question produceSingleQuestionJSON(@PathParam("id") int id) {
 		return getQuestion(id);
 	}
 	
 	@GET
	@Path(WorldStrings.URL_PREFIX + "/{id}/history/json")
 	@Produces("application/json")
 	public Map<String,?> produceHistoryJSON(@PathParam("id") int id) {
 		List<Question> history = new ArrayList<Question>();
 		
 		for (Question q = getQuestion(id); q != null; q = q.getParent()) {
 			history.add(q);
 		}
 		
 		return ImmutableMap.of("history",history);
 	}
 	
 	@GET
	@Path(WorldStrings.URL_PREFIX + "/{id}/forks/json")
 	@Produces("application/json")
 	public List<?> produceForksJSON(@PathParam("id") int id) {
 		return QuestionQuery.all().withParent(id).list();
 	}
 	
 	
 }
