 package hudson.plugins.jwsdp_sqe;
 
 import hudson.FilePath;
 import hudson.FilePath.FileCallable;
 import hudson.Launcher;
 import hudson.remoting.VirtualChannel;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.Result;
 import hudson.model.Action;
 import hudson.model.Project;
 import hudson.tasks.Publisher;
 import hudson.tasks.test.TestResultProjectAction;
 import hudson.util.IOException2;
 import org.apache.tools.ant.types.FileSet;
 import org.kohsuke.stapler.StaplerRequest;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 
 /**
  * Collects SQE test reports and convert them into JUnit format.
  *
  * @author Kohsuke Kawaguchi
  */
 public class SQETestResultPublisher extends Publisher implements Serializable {
 
     private final String includes;
     /**
      * Flag to capture if test should be considered as executable TestObject
      */
     boolean considerTestAsTestObject = false;
 
     public SQETestResultPublisher(String includes, boolean considerTestAsTestObject) {
         this.includes = includes;
         this.considerTestAsTestObject = considerTestAsTestObject;
     }
 
     /**
      * Ant "&lt;fileset @includes="..." /> pattern to specify SQE XML files
      */
     public String getIncludes() {
         return includes;
     }
 
     public boolean getConsiderTestAsTestObject() {
         return considerTestAsTestObject;
     }
 
     public Action getProjectAction(Project project) {
         return new TestResultProjectAction(project);
     }
 
     /**
      * Indicates an orderly abortion of the processing.
      */
     private static final class AbortException extends RuntimeException {
         public AbortException(String s) {
             super(s);
         }
     }
 
     public boolean perform(Build build, Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
         final long buildTime = build.getTimestamp().getTimeInMillis();
 
         listener.getLogger().println("Collecting JWSDP SQE reports");
 
         // target directory
         File dataDir = SQETestAction.getDataDir(build);
         dataDir.mkdirs();
         final FilePath target = new FilePath(dataDir);
 
         try {
             build.getProject().getWorkspace().act(new FileCallable<Void>() {
                 public Void invoke(File ws, VirtualChannel channel) throws IOException {
                     FileSet fs = new FileSet();
                     org.apache.tools.ant.Project p = new org.apache.tools.ant.Project();
                     fs.setProject(p);
                     fs.setDir(ws);
                     fs.setIncludes(includes);
                     String[] includedFiles = fs.getDirectoryScanner(p).getIncludedFiles();
 
                     if(includedFiles.length==0)
                         // no test result. Most likely a configuration error or fatal problem
                         throw new AbortException("No SQE test report files were found. Configuration error?");
 
                     int counter=0;
 
                     // archive report files
                     for (String file : includedFiles) {
                         File src = new File(ws, file);
 
                         if(src.lastModified()<buildTime) {
                             listener.getLogger().println("Skipping "+src+" because it's not up to date");
                             continue;       // not up to date.
                         }
 
                         try {
                             new FilePath(src).copyTo(target.child("report"+(counter++)+".xml"));
                         } catch (InterruptedException e) {
                             throw new IOException2("aborted while copying "+src,e);
                         }
                     }
                     return null;
                 }
                 private static final long serialVersionUID = 1L;
             });
         } catch (AbortException e) {
             listener.getLogger().println(e.getMessage());
             build.setResult(Result.FAILURE);
             return true; /// but this is not a fatal error
         }
 
 
         SQETestAction action = new SQETestAction(build, listener, considerTestAsTestObject);
         build.getActions().add(action);
 
         Report r = action.getResult();
 
         if(r.getTotalCount()==0) {
             listener.getLogger().println("Test reports were found but none of them are new. Did tests run?");
             // no test result. Most likely a configuration error or fatal problem
             build.setResult(Result.FAILURE);
         }
 
         if(r.getFailCount()>0)
             build.setResult(Result.UNSTABLE);
 
         return true;
     }
 
     private static final long serialVersionUID = 1L;
 
     public Descriptor<Publisher> getDescriptor() {
         return DescriptorImpl.DESCRIPTOR;
     }
 
     /*package*/ static class DescriptorImpl extends Descriptor<Publisher> {
         public static final Descriptor<Publisher> DESCRIPTOR = new DescriptorImpl();
 
         public DescriptorImpl() {
             super(SQETestResultPublisher.class);
         }
 
         public String getDisplayName() {
             return "Publish SQE test result report";
         }
 
         public String getHelpFile() {
             return "/plugin/jwsdp-sqe/help.html";
         }
 
         public Publisher newInstance(StaplerRequest req) {
             return new SQETestResultPublisher(req.getParameter("sqetest_includes"),(req.getParameter("sqetest_testobject")!=null));
         }
     }
 }
