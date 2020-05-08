 package testingbot;
 
 /**
  *
  * @author testingbot.com
  */
 import hudson.model.AbstractBuild;
 import hudson.tasks.junit.CaseResult;
 import hudson.tasks.junit.TestAction;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Show videos for the tests.
  *
  */
 public class TestingBotReport extends TestAction {
     public final CaseResult parent;
     /**
      * Session IDs.
      */
     private final List<String> ids;
 
     public TestingBotReport(CaseResult parent, List<String> ids) {
         this.parent = parent;
         this.ids = ids;
     }
 
     public AbstractBuild<?, ?> getBuild() {
         return parent.getOwner();
     }
 
     public List<String> getIDs() {
         return Collections.unmodifiableList(ids);
     }
 
     public String getId() {
         return ids.get(0);
     }
     
     public String getClientKey() {
         try {
           FileInputStream fstream = new FileInputStream(System.getProperty("user.home") + "/.testingbot");
           DataInputStream in = new DataInputStream(fstream);
           BufferedReader br = new BufferedReader(new InputStreamReader(in));
           String strLine = br.readLine();
           String[] data = strLine.split(":");
           return data[0];
         } catch (Exception e) {}
         
         return "";
     }
 
     public String getIconFileName() {
        return (ids.size() > 0) ? "/plugin/testingbot/images/24x24/logo.jpg" : null;
     }
 
     public String getDisplayName() {
         return "TestingBot Report";
     }
 
     public String getUrlName() {
        return (ids.size() > 0) ? "testingbot" : null;
     }
 }
