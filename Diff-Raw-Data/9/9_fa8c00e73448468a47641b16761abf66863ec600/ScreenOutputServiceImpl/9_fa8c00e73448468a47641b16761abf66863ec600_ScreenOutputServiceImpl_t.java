 package lv.jake.jiw.output;
 
 import lv.jake.jiw.DueDateChecker;
 import lv.jake.jiw.TimeServiceImpl;
 import org.apache.log4j.Logger;
 
 import java.util.Map;
 
 /**
  * User: Jaker
  * Date: 2010.14.4
  * Time: 22:03:49
  */
 public class ScreenOutputServiceImpl implements OutputService {
     private static org.apache.log4j.Logger log = Logger.getLogger(ScreenOutputServiceImpl.class);
     Object[] filters = null;
     Map issues = null;
     DueDateChecker dueDateChecker = null;
 
     public void setData(Object[] filters, Map issues){
         this.filters = filters;
         this.issues = issues;
         dueDateChecker = new DueDateChecker(new TimeServiceImpl());
     }
 
     public void printOutput() {
         for (Object filter : filters) {
             Map currentFilter = (Map) filter;
             log.info("\n///////--------------- Filter " + currentFilter.get("name") + " ----------------///////");
             showIssueDetail(issues, (String) currentFilter.get("id"));
         }
     }
 
     public void showIssueDetail(Map issues, String currentIssues){
         for (Object currentIssue : (Object[]) issues.get(currentIssues)) {
             Map issue = (Map) currentIssue;
             log.info("Key: " + issue.get("key") /*+ " - created: " +
                     issue.get("created") + " - updated: " + issue.get("updated")"*/ +
                     " - Due date: " + issue.get("duedate") + "- Priority: " +
                     getPriorityById(issue.get("priority").toString()) +
                     " /-/ STATUS: " + dueDateChecker.getDueDateStatus(issue.get("created").toString(),
                     (String) issue.get("duedate"), issue.get("priority").toString(),
                     issue.get("updated").toString()));
            if (!DueDateChecker.OK.equals(dueDateChecker.getDueDateStatus(issue.get("created").toString(),
                     (String) issue.get("duedate"), issue.get("priority").toString(),
                    issue.get("updated").toString())))
             log.info("||--Summary: " + issue.get("summary"));
 //            Object[] comments = null;
 //                comments = lv.jake.jiw.getComments(rpcclient, loginToken, (String) issue.get("key"));
 //                log.info("comments count: " + comments.length);
         }
 
     }
 
     public String getPriorityById(String id) {
         if (Integer.valueOf(id) == 1) {
             return "Blocker";
         }
         if (Integer.valueOf(id) == 2) {
             return "Critical";
         }
         if (Integer.valueOf(id) == 3) {
             return "Major";
         }
         if (Integer.valueOf(id) == 4) {
             return "Minor";
         }
         if (Integer.valueOf(id) == 5) {
             return "Trivial";
         }
         return "not found";
     }
 }
