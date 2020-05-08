package org.jenkinsci.plugins.mongodbdocumentupload;
 
 import com.mongodb.MongoClient;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.tasks.*;
 import hudson.util.FormValidation;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.StringTokenizer;
 
 /**
  * UploadPublisher {@link hudson.tasks.Recorder}.
  *
  * <p>
  * When the user configures the project and enables this recorder,
  * {@link DescriptorImpl#newInstance(org.kohsuke.stapler.StaplerRequest)} is invoked
  * and a new {@link UploadPublisher} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #serverName})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(hudson.model.AbstractBuild, hudson.Launcher, hudson.model.BuildListener)}
  * method will be invoked.
  *
  * @author Kohsuke Kawaguchi
  */
 public class UploadPublisher extends Recorder {
 
     private String serverName;
     private String databaseName;
     private String collectionName;
     private String files;
     private transient final String filesSeparator = ",";
 
     @DataBoundConstructor
     public UploadPublisher(String serverName, String databaseName, String collectionName, String files) {
         this.serverName = serverName;
         this.databaseName = databaseName;
         this.collectionName = collectionName;
         this.files = files;
     }
 
     public String getServerName() {
         if(serverName == null) {
             return getDescriptor().getServerName();
         }
 
         return serverName;
     }
 
     public String getDatabaseName() {
         if(databaseName == null) {
             return getDescriptor().getDatabaseName();
         }
 
         return databaseName;
     }
 
     public String getCollectionName() {
         if( collectionName == null) {
             return getDescriptor().getCollectionName();
         }
 
         return collectionName;
     }
 
     public String getFiles() {
         return files;
     }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
 
         FilePath wsPath		= build.getWorkspace();
         StringTokenizer strTokens	= new StringTokenizer(getFiles(), filesSeparator);
         FilePath[]      paths 		= null;
 
         MongoClient client = null;
 
         try {
             client = new MongoClient(getServerName());
         }
         catch (UnknownHostException e) {
             listener.getLogger().println("Unknown host " + getServerName());
             return false;
         }
 
         JsonInsert insert = new JsonInsert(client, getDatabaseName(), getCollectionName());
 
         while (strTokens.hasMoreElements()) {
             try {
                 paths = wsPath.list(strTokens.nextToken());
             }
             catch (Exception exception) {
                 listener.getLogger().println(exception.toString());
                 continue;
             }
 
             if	(paths.length != 0) {
                 for (FilePath src : paths) {
                     try {
                         listener.getLogger().println("Uploading " + src);
                         insert.Insert(src.readToString());
                     } catch (IOException e) {
                         listener.getLogger().println("Failed to read document " + src);
                         e.printStackTrace();
                     }
                 }
             }
         }
 
         return true;
     }
 
     // Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
     public BuildStepMonitor getRequiredMonitorService() {
         return BuildStepMonitor.STEP;
     }
 
     /**
      * Descriptor for {@link UploadPublisher}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      *
      * <p>
      * See <tt>src/main/resources/hudson/plugins/hello_world/UploadBuilder/*.jelly</tt>
      * for the actual HTML fragment for the configuration screen.
      */
     @Extension // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private String serverName;
         private String databaseName;
         private String collectionName;
 
         public String getServerName() {
             return serverName;
         }
 
         public String getDatabaseName() {
             return databaseName;
         }
 
         public String getCollectionName() {
             return collectionName;
         }
 
         /**
          * Performs on-the-fly validation of the form fields.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckName(@QueryParameter String value)
                 throws IOException, ServletException {
 
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
             return "Upload JSON files to MongoDB";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             this.serverName = formData.getString("serverName");
             this.databaseName = formData.getString("databaseName");
             this.collectionName = formData.getString("collectionName");
 
             save();
 
             return super.configure(req,formData);
         }
     }
 }
 
