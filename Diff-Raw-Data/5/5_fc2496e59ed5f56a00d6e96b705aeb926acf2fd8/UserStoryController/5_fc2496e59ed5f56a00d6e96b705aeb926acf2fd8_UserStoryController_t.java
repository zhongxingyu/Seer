 package com.cisco.diddo.web;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
 import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.cisco.diddo.constants.TASK;
 import com.cisco.diddo.dao.ExitCriteraDao;
 import com.cisco.diddo.dao.SprintDao;
 import com.cisco.diddo.dao.TaskDao;
 import com.cisco.diddo.dao.TeamDao;
 import com.cisco.diddo.dao.UserStoryDao;
 import com.cisco.diddo.entity.ExitCriteria;
 import com.cisco.diddo.entity.Sprint;
 import com.cisco.diddo.entity.Task;
 import com.cisco.diddo.entity.Team;
 import com.cisco.diddo.entity.User;
 import com.cisco.diddo.entity.UserStory;
 import com.cisco.diddo.entity.UserStoryDetail;
 
 import flexjson.JSONDeserializer;
 
 @RequestMapping("/userstorys")
 @Controller
 @RooWebScaffold(path = "userstorys", formBackingObject = UserStory.class)
 @RooWebJson(jsonObject = UserStory.class)
 public class UserStoryController {
 	    @Autowired
 	    public UserStoryDao userStoryDao;
 	    
 	    @Autowired
 	    public SprintDao sprintDao;
 	    
 	    @Autowired
 	    public TeamDao teamDao;
 	    
 	    @Autowired
 	    public ExitCriteraDao exitCriteriaDao;
 	    
 	    @Autowired
 	    public TaskDao taskDao;
 	
 	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
     public ResponseEntity<String> deleteFromJson(@PathVariable("id") String idStr) {
 		BigInteger id = new BigInteger(idStr);
         UserStory userStory = findById(id);//findById(id);
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/json");
         if (userStory == null) {
             return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
         }
         userStoryDao.delete(userStory);
         return new ResponseEntity<String>(headers, HttpStatus.OK);
     }
 	
 	 @RequestMapping(method = RequestMethod.POST, headers = "Accept=application/json")
 	    public ResponseEntity<String> createFromJson(@RequestBody String json) {
 	        UserStory userStory = fromJsonToUserStory(json);
 	        userStoryDao.save(userStory);
 	        HttpHeaders headers = new HttpHeaders();
 	        headers.add("Content-Type", "application/json");
 	        return new ResponseEntity<String>(userStory.toJson(),headers, HttpStatus.CREATED);
 	    }
 	
     @RequestMapping(method = RequestMethod.PUT, headers = "Accept=application/json")
     public ResponseEntity<String> updateFromJson(@RequestBody String json) {
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/json");
         UserStory userStory = fromJsonToUserStory(json);
         if (userStoryDao.save(userStory) == null) {
             return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
         }
         return new ResponseEntity<String>(userStory.toJson() , headers, HttpStatus.OK);
     } 
 	 
     @RequestMapping(value = "/{id}",params="userstorydetails", headers = "Accept=application/json")
     @ResponseBody
     public ResponseEntity<String> getUserStoryJson(@PathVariable("id") String id) {
     	UserStory userStory = findById(new BigInteger(id));
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/json; charset=utf-8");
         if (userStory == null) {
             return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
         }
         UserStoryDetail userStoryDetail = new UserStoryDetail();
         userStoryDetail.title = userStory.getTitle();
         userStoryDetail.description = userStory.getDescription();
         userStoryDetail.startDate = userStory.getStartDate();
         userStoryDetail.endDate = userStory.getEndDate();
         List<ExitCriteria> exitCriteriaList = findExitCriteriaByUserStory(userStory);
         for(ExitCriteria exitCriteria : exitCriteriaList){
         	userStoryDetail.exitcriterias.put(exitCriteria.getDescription() , exitCriteria.getDone());
         }
         List<Task> taskList = findTaskByUserStory(userStory);
         int countInprogress = 0;
         int countCompleted = 0;
         for(Task task :taskList){
         	if(task.getStatus() == TASK.IN_PROGRESS){
         		countInprogress ++;
         	}
         	else if(task.getStatus() == TASK.COMPLETED){
         		countCompleted ++ ;
         	}
         }
         userStoryDetail.totalTask = taskList.size();
         userStoryDetail.inprogressTask=countInprogress;
         userStoryDetail.completedTask=countCompleted;
         return new ResponseEntity<String>(userStoryDetail.toJson() , headers, HttpStatus.OK);
     }
     
     @RequestMapping(value = "/{id}",params="tasks", headers = "Accept=application/json")
     @ResponseBody
     public ResponseEntity<String> getTasksJson(@PathVariable("id") String id) {
         UserStory us = findById(new BigInteger(id));
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/json; charset=utf-8");
         if (us == null) {
             return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
         }
         List<Task> taskList = findTaskByUserStory(us);
         //String userJson = new JSONSerializer().exclude("*.class").serialize(userList);
         String jsonStr = Task.toJsonArray(taskList);
         return new ResponseEntity<String>(jsonStr , headers, HttpStatus.OK);
     }
     
     @RequestMapping(value = "/{id}",params="exitcriterias", headers = "Accept=application/json")
     @ResponseBody
     public ResponseEntity<String> getExitCriteriasJson(@PathVariable("id") String id) {
         UserStory us = findById(new BigInteger(id));
         HttpHeaders headers = new HttpHeaders();
         headers.add("Content-Type", "application/json; charset=utf-8");
         if (us == null) {
             return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
         }
         List<ExitCriteria> ecList = findExitCriteriaByUserStory(us);
         //String userJson = new JSONSerializer().exclude("*.class").serialize(userList);
         String jsonStr = ExitCriteria.toJsonArray(ecList);
         return new ResponseEntity<String>(jsonStr , headers, HttpStatus.OK);
     }
     
 	private UserStory findById(BigInteger id){
 		List<UserStory> userStorys = userStoryDao.findAll();
 		for(UserStory userStory : userStorys){
 			if(userStory.getId().equals(id)){
 				return userStory;
 			}
 		}
 		return null;
 	}
 	private Sprint findSprintById(BigInteger id){
 		List<Sprint> sprints = sprintDao.findAll();
 		for(Sprint sprint : sprints){
 			if(sprint.getId().equals(id)){
 				return sprint;
 			}
 		}
 		return null;
 	}
 	private Team findTeamById(BigInteger id){
 		List<Team> teams = teamDao.findAll();
 		for(Team team : teams){
 			if(team.getId().equals(id)){
 				return team;
 			}
 		}
 		return null;
 	}
 	private List<ExitCriteria> findExitCriteriaByUserStory(UserStory us){
 		List<ExitCriteria> list = exitCriteriaDao.findAll();
 		List<ExitCriteria> conList = new ArrayList<ExitCriteria>();
 		for(ExitCriteria ec : list){
			if(ec.getUserStory() != null && ec.getUserStory().getId().equals(us.getId())){
 			   conList.add(ec);
 			}
 		}
 		return conList;
 	}
 	
 	private List<Task> findTaskByUserStory(UserStory us){
 		List<Task> list = taskDao.findAll();
 		List<Task> conList = new ArrayList<Task>();
 		for(Task t : list){
			if(t.getUserStory() != null && t.getUserStory().getId().equals(us.getId())){
 			   conList.add(t);
 			}
 		}
 		return conList;
 	}
 	
 	private UserStory fromJsonToUserStory(String jsonStr){
 		UserStory userStory = null;
 		Map<String, String> deserialized = new JSONDeserializer<Map<String, String>>().deserialize(jsonStr);
 		String id = deserialized.get("id");
 		if(id != null && !id.equals("")){
 			 userStory = findById(new BigInteger(id));
 		}
 		if(userStory == null){
 			userStory = new UserStory();
 		 }
 		userStory.setFriendlyID(deserialized.get("friendlyID"));
 	    userStory.setDescription(deserialized.get("description"));
 	    userStory.setTitle(deserialized.get("title"));
 	    String points = deserialized.get("points");
 	    if(points != null){
 	    	userStory.setPoints(Byte.valueOf(points));
 	    }
 	    userStory.setColor(deserialized.get("color"));
 	    if(deserialized.get("unplanned") != null ){
 	    	Object obj = deserialized.get("unplanned");
 	    	if(obj instanceof ArrayList && ((ArrayList)obj).size() > 0){
 	    	    userStory.setUnplanned(true);
 	    	}
 	    }
 	    if(deserialized.get("spillover") != null ){
 	    	Object obj = deserialized.get("spillOver");
 	    	if(obj instanceof ArrayList && ((ArrayList)obj).size() > 0){
 	    	    userStory.setSpillOver(true);
 	    	}
 	    }
 	    if(deserialized.get("complex") != null ){
 	    	Object obj = deserialized.get("complex");
 	    	if(obj instanceof ArrayList && ((ArrayList)obj).size() > 0){
 	    	    userStory.setComplex(true);
 	    	}
 	    }
 	    String Id = deserialized.get("sprint");
 	    if(Id != null && !Id.equals("")){
 	    	BigInteger iid = new BigInteger(Id);
 	    	userStory.setSprint(findSprintById(iid));
 	    }
 	    Id = deserialized.get("team");
 	    if(Id != null && !Id.equals("")){
 	    	BigInteger iid = new BigInteger(Id);
 	    	userStory.setTeam(findTeamById(iid));
 	    }
 	    
 		return userStory;
 	}
 
 }
