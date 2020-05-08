 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Adam Saleh (asaleh at redhat dot com)
  *          Jan Rusnacko (jrusnack at redhat dot com)
  */
 
 package NitrateIntegration;
 
 import com.redhat.engineering.jenkins.testparser.Parser;
 import com.redhat.engineering.jenkins.testparser.results.TestResults;
 import com.redhat.nitrate.TcmsAccessCredentials;
 import com.redhat.nitrate.TcmsConnection;
 import com.redhat.nitrate.TcmsException;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Recorder;
 import hudson.util.FormValidation;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * @author asaleh
  * @author jrusnack
  */
 public class TcmsPublisher extends Recorder {
 
     public final String serverUrl;
     public final String reportLocationPattern;
     public final String plan;
     public final String product;
     public final String product_v;
     public final String category;
     public final String priority;
     public final String manager;
     public final String env;
     static final Object lock = new Object();
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public TcmsPublisher(String serverUrl, String username, String password,
             String plan,
             String product,
             String product_v,
             String category,
             String priority,
             String manager,
             String env,
             String testPath) {
 
         this.serverUrl = serverUrl;
         this.reportLocationPattern = testPath;
 
         this.category = category;
         this.manager = manager;
         this.plan = plan;
         this.priority = priority;
         this.product = product;
         this.product_v = product_v;
 
         this.env = env;
 
     }
 
     /**
      * Check whether TcmsReviewAction has been added to the build, if not, fix
      * that. This method contains mutex, so that TcmsReviewAction is added only
      * once.
      *
      * @param build
      * @param listener
      * @return Always true, so Jenkins may continue building.
      */
     public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
         AbstractBuild agregateBuild = build;
         if (build instanceof MatrixRun) {
             MatrixRun mrun = (MatrixRun) build;
             agregateBuild = mrun.getParentBuild();
         }
         synchronized (lock) {
             if (agregateBuild.getAction(TcmsReviewAction.class) == null) {
                 agregateBuild.getActions().add(new TcmsReviewAction(agregateBuild,                         
                         serverUrl,
                         plan,
                         product,
                         product_v,
                         category,
                         priority,
                         manager,
                         env,
                         reportLocationPattern));
             }
         }
 
         return true;
     }
 
     /**
      * Locates, checks and saves reports after build finishes, and adds result
      * to TcmsReviewAction corresponding to the build.
      *
      * @param build
      * @param launcher
      * @param listener
      * @return
      * @throws IOException
      * @throws InterruptedException
      */
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
         listener.getLogger().println("Starting TCMS integration plugin");
         listener.getLogger().println("Looking for TestNG results report in workspace using pattern: "
                 + reportLocationPattern);
 
 
 
         FilePath[] paths = null;
         paths = Parser.locateReports(build.getWorkspace(), reportLocationPattern);
 
         if (paths.length == 0) {
             listener.getLogger().println("Did not find any matching files.");
             return true;
         }
 
         boolean filesSaved = Parser.saveReports(Parser.getReportDir(build), paths, listener.getLogger(), "test-results");
         if (!filesSaved) {
             listener.getLogger().println("Failed to save TestNG XML reports");
             return true;
         }
 
         TestResults results = Parser.loadResults(build, null, "test-results");
 
         TcmsReviewAction action = build instanceof MatrixRun
                 ? ((MatrixRun) build).getParentBuild().getAction(TcmsReviewAction.class)
                :  build.getAction(TcmsReviewAction.class);
 
         Map<String, String> vars = new HashMap<String, String>();
         vars.putAll(build.getBuildVariables());
         
         if(build instanceof MatrixRun){
             //check if combination is included in build-vars
             Map<String, String> combination = ((MatrixRun)build).getParent().getCombination();
             for(Entry<String,String> e:combination.entrySet()){
                 if(vars.entrySet().contains(e)==false){
                     vars.put(e.getKey(),e.getValue());
                 }
             }
         } 
         
         action.report.addTestRun(results, build, vars);
         return true;
     }
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.STEP;
     }
 
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<hudson.tasks.Publisher> {
 
        
        public FormValidation doTestConnection(@QueryParameter("serverUrl") final String serverUrl,
                 @QueryParameter("username") final String username,
                 @QueryParameter("password") final String password,
                 @QueryParameter("plan") final String plan,
                 @QueryParameter("product") final String product,
                 @QueryParameter("product_v") final String product_v,
                 @QueryParameter("category") final String category,
                 @QueryParameter("priority") final String priority,
                 @QueryParameter("manager") final String manager) {
            
             List<String> problems = new LinkedList();
             
             try {
                 TcmsAccessCredentials credentials = new TcmsAccessCredentials(serverUrl, username, password);
                 TcmsProperties properties = new TcmsProperties(plan, product, product_v, category, priority, manager);
                 TcmsConnection connection = TcmsConnection.connect(serverUrl, credentials, Definitions.krbv, Definitions.enforceHttps);
                 boolean test = connection.testTcmsConnection();
 
                 properties.setConnection(connection);
                 properties.reload();
                 problems = TcmsProperties.checkUsersetProperties(properties);
 
             } catch (TcmsException ex) {
                 return FormValidation.error(ex.getMessage());
             } catch (IOException ex) {
                 return FormValidation.error(ex.toString());
             }
             
             if(!problems.isEmpty()){
                 return FormValidation.error(problems.toString().replace("[", "").replace("]", ""));
             }
 
             return FormValidation.ok();
         }
 
         public FormValidation doTestEnv(@QueryParameter("serverUrl") final String serverUrl,
                 @QueryParameter("username") final String username,
                 @QueryParameter("password") final String password,
                 @QueryParameter("env") final String env) {
             
             TcmsAccessCredentials credentials = new TcmsAccessCredentials(serverUrl, username, password);
             TcmsEnvironment environment = new TcmsEnvironment(env);
             
             try {
                 TcmsConnection connection = TcmsConnection.connect(serverUrl, credentials, Definitions.krbv, Definitions.enforceHttps);
 
                 environment.setConnection(connection);
                 environment.reloadEnvId();
 
                 if (!environment.env.isEmpty() && environment.getEnvId() == null) {
                     throw new TcmsException("Possibly wrong environment group: \"" + environment.env + "\"");
                 }
 
             } catch (TcmsException ex) {
                 return FormValidation.error(ex.getMessage());
             }
             return FormValidation.ok();
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // Indicates that this builder can be used with all kinds of project types 
             return true;
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "Integration with Nitrate TCMS";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             save();
             return super.configure(req, formData);
         }
     }
 }
