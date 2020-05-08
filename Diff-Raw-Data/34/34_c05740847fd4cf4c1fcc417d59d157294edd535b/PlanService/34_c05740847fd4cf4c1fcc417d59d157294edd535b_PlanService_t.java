 package in.di.ce.service;
 
 import in.di.ce.RetrievalPlan;
 import in.di.ce.prevalence.BaseModel;
 import in.di.ce.service.rta.Ok;
 import in.di.ce.service.rta.Respuesta;
 import in.di.ce.service.rta.TodoMal;
 import lombok.Getter;
 import lombok.Setter;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("plan")
 public class PlanService {
 
 	private static final Log log = LogFactory.getLog(PlanService.class); 
 
 	@Autowired @Setter @Getter private BaseModel baseModel;
 
 	@RequestMapping(value="{videoId}/{userId}", method = RequestMethod.GET)
 	public @ResponseBody Respuesta<RetrievalPlan> getRetrievalPlan(@PathVariable String videoId, @PathVariable String userId){
 
		log.debug("Retrieval plan requested by user: "+userId+" for video: "+videoId);
		if(CollectionUtils.isEmpty(baseModel.getModel().getChunksFrom(videoId, userId))){
			log.debug("no chunks in index for requester user "+userId);
 		}
 		
 		RetrievalPlan retrievalPlan = baseModel.getModel().plan(videoId, userId); 
 		if(retrievalPlan != null) {
			log.info("Returning retrieval plan for video: " + videoId);
 			return new Ok<RetrievalPlan>(retrievalPlan);
 		} 
		log.error("Unable to buid retrieval plan for video: " + videoId);
 		return new TodoMal<RetrievalPlan>(retrievalPlan);
 	}
 
 	/*
 	 * TODO quien esta online pára este video para este usuario
 	 */
 //	@RequestMapping(value="{videoId}/{userId}", method = RequestMethod.GET)
 //	public @ResponseBody Respuesta<List<User>> whoIsOnlineForVideo(@PathVariable String videoId, @PathVariable String userId){
 //		return null;
 //	}
 }
