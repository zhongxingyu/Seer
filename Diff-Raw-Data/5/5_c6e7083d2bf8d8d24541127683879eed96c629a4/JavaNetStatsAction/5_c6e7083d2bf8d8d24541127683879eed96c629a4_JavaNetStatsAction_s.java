 package hudson.plugins.javanet;
 
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.Hudson;
 import static hudson.plugins.javanet.PluginImpl.DAY;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.util.regex.Pattern;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class JavaNetStatsAction implements Action {
     /**
      * Project that owns this action.
      */
     public final AbstractProject<?,?> project;
 
     /**
      * Java.net project name.
      */
     private final String projectName;
 
     public JavaNetStatsAction(AbstractProject<?, ?> project, String projectName) {
         this.project = project;
         this.projectName = projectName;
     }
 
     public void scheduleGeneration() {
         new ReportGenerator(projectName,getReportDirectory()).schedule();
     }
 
     public String getIconFileName() {
         return "a.png";
     }
 
     public String getDisplayName() {
         return "java.net statistics";
     }
 
     public String getUrlName() {
         return "java.net-stats";
     }
 
     /**
      * Schedules the re-generation of the report if the report is too old.
      */
     public void upToDateCheck() {
         File indexHtml = new File(getReportDirectory(),"index.html");
         if(!indexHtml.exists() || (System.currentTimeMillis()-indexHtml.lastModified()>7*DAY)) {
             scheduleGeneration();
         }
     }
 
     /**
      * Directory in which the report is stored.
      */
     private File getReportDirectory() {
         return new File(Hudson.getInstance().getRootDir(), "java.net/issue-tracker-stats/" + projectName);
     }
 
     /**
      * Serves static files in the report directory.
      */
     public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
 
         // make sure we are not serving anything strange
         if(!PATH.matcher(path).matches()) {
             rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
             return;
         }
 
         rsp.serveFile(req,new File(getReportDirectory(),path).toURL());
     }
 
    private static final Pattern PATH = Pattern.compile("[A-Za-z0-9\\-]+");
 }
