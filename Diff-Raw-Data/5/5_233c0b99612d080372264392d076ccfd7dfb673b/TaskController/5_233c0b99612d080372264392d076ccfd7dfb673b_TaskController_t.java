 package aic.appengine.sentimentanalysis.controller;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import aic.appengine.sentimentanalysis.domain.Task;
 import aic.appengine.sentimentanalysis.service.DataStoreAccess;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 @Controller
 public class TaskController {
 
     /**
      * TaskService would be a service bean that exposes the task DAO methods
      * (e.g loading tasks from the data store)
      */
     // @Autowired
     // private ITaskService taskService;
 
     /**
      * Here we should lookup all currently executing tasks and return a list of
      * tasks to the tasks view.
      */
     @RequestMapping(value = "/tasks", method = RequestMethod.GET)
     public ModelAndView getTasks(HttpServletRequest request,
             HttpServletResponse response) {
         ModelAndView mav = new ModelAndView("tasks");
         // mav.addObject(taskService.getTasks());
         
         UserService userService = UserServiceFactory.getUserService();
         String email = userService.getCurrentUser().getEmail();
         
         Key emailKey = KeyFactory.createKey("user", email);
         
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
         Query dataQuery = new Query("task", emailKey).addSort("date",
                 Query.SortDirection.DESCENDING);
         List<Entity> tasksEntity = datastore.prepare(dataQuery).asList(
                 FetchOptions.Builder.withLimit(20));
         
         List<Task> tasks = new ArrayList<Task>(tasksEntity.size());
         for(Entity entity : tasksEntity) {
             Task task = new Task();
             if(entity.getProperty("name") instanceof String)
                 task.setName((String) entity.getProperty("name"));
             if(entity.getProperty("query") instanceof String)
                 task.setQuery((String) entity.getProperty("query"));
             if(entity.getProperty("sentiment") instanceof Integer)
                task.setSentiment((String) entity.getProperty("sentiment"));
             if(entity.getProperty("status") instanceof String)
                 task.setStatus((String) entity.getProperty("status"));
             if(entity.getProperty("date") instanceof Date)
                 task.setDate((Date) entity.getProperty("date"));
             if(entity.getProperty("duration") instanceof Long)
                task.setDuration((Long) entity.getProperty("duration"));
             tasks.add(task);
         }
 
         mav.addObject("tasks", tasks);
 
         return mav;
     }
 
     /**
      * This is the method that receives the task name and query information from
      * the form on the index page, when the form has been submitted.
      */
     @RequestMapping(value = "/tasks", method = RequestMethod.POST)
     public String create(@RequestParam("taskname") String taskname,
             @RequestParam("query") String query) {
 
         UserService userService = UserServiceFactory.getUserService();
         String email = userService.getCurrentUser().getEmail();
 
         Key emailKey = KeyFactory.createKey("user", email);
 
         System.out.println("taskname: " + taskname);
         System.out.println("query: " + query);
         Entity task = new Entity("task", emailKey);
         task.setProperty("date", new Date());
         task.setProperty("name", taskname);
         task.setProperty("query", query);
         task.setProperty("status", "PENDING");
 
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
         datastore.put(task);
 
         DataStoreAccess.startSentimentAnalysis(query,email,task.getKey().getId());
 
         return "redirect:/tasks";
     }
 }
