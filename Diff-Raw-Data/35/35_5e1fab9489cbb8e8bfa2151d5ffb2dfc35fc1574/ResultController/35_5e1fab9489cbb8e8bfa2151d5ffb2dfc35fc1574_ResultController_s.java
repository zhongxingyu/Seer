 package linkki.taskswitching;
 
 import linkki.taskswitching.service.ResultService;
 import linkki.taskswitching.view.DataTablesOutput;
 import linkki.taskswitching.dto.TestResult;
 import java.util.ArrayList;
 import java.util.List;
 import linkki.taskswitching.repository.ParticipantRepository;
 import linkki.taskswitching.service.AggregateResultService;
 import linkki.taskswitching.view.AggregateResult;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class ResultController {
 
     @Autowired
     private ParticipantRepository participantRepository;
     @Autowired
     private ResultService resultService;
     @Autowired
     private AggregateResultService aggregateResultService;
 
     @RequestMapping(method = RequestMethod.POST, value = "result", consumes = "application/json")
     @ResponseBody
     public AggregateResult postResult(@RequestBody TestResult result) {
         if (SecurityContextHolder.getContext().getAuthentication() != null
                 && SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
             String username = SecurityContextHolder.getContext().getAuthentication().getName();
             if (username != null) {
                 result.getParticipant().setUsername(username);
             }
         }
 
        result.setParticipant(participantRepository.findByUsername(result.getParticipant().getUsername()));
 
         System.out.println("storing results");
         resultService.save(result);
         System.out.println("done..");
         return aggregateResultService.calculateResult(result);
     }
 
     @RequestMapping(method = RequestMethod.GET, value = "result", produces = "application/json")
     @ResponseBody
     public DataTablesOutput getResults() {
         DataTablesOutput output = new DataTablesOutput();
         List<AggregateResult> aggResults = new ArrayList<AggregateResult>();
         for (TestResult testResult : resultService.list()) {
             aggResults.add(aggregateResultService.calculateResult(testResult));
         }
 
         output.setAaData(aggResults);
         return output;
     }
 }
