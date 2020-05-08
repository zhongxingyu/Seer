 package in.di.ce.service;
 
 import in.di.ce.RetrievalPlan;
 import in.di.ce.prevalence.BaseModel;
 import in.di.ce.service.rta.Ok;
 import in.di.ce.service.rta.Respuesta;
 import in.di.ce.service.rta.TodoMal;

import java.util.Set;

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
 
		System.out.println("retrieval plan requested");
		System.out.println(baseModel.getModel().getChunksFrom(videoId, userId));
		Set<Integer> chunks = baseModel.getModel().getChunksFrom(videoId, userId);
		if(CollectionUtils.isNotEmpty(chunks)){
			for(int i : chunks) {
				System.out.println(userId+" -> "+i);
			}
		} else {
			log.error("no chunks in index for user "+userId);
 		}
 		
		log.info("About to build retrieval plan for video: " + videoId);
 		RetrievalPlan retrievalPlan = baseModel.getModel().plan(videoId, userId); 
		log.info("Returning retrieval plan for video: " + videoId);
 		if(retrievalPlan != null) {
 			return new Ok<RetrievalPlan>(retrievalPlan);
 		} 
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
