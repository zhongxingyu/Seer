 package org.jvnet.hudson.plugins;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.util.FormFieldValidator;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.Result;
 import hudson.tasks.Builder;
 import hudson.tasks.CommandInterpreter;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.QueryParameter;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 
 import org.python.core.PySystemState;
 import org.python.core.PyObject;
 import org.python.util.PythonInterpreter;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link HelloWorldBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method
  * will be invoked. 
  *
  * @author R. Tyler Ballance
  */
 public class Jython extends Builder {
     private final String command;
 
     private Jython(String command) {
         this.command = command;
     }
 
    protected String getContents() {
         return command;
     }
 
     public Descriptor<Builder> getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends Descriptor<Builder> {
         private DescriptorImpl() {
             super(Jython.class);
         }
 
         public Builder newInstance(StaplerRequest req, JSONObject formData) {
             return new Jython(formData.getString("jython"));
         }
 
         public String getDisplayName() {
             return "Execute Jython script";
         }
 
         @Override
         public String getHelpFile() {
             return "/plugin/jython/help.html";
         }
     }
 
     public boolean perform(Build build, Launcher launcher, BuildListener listener)  throws IOException, InterruptedException {
         PySystemState sys = new PySystemState();
         sys.setCurrentWorkingDir(build.getProject().getWorkspace().getRemote());
         PythonInterpreter interp = new PythonInterpreter(null, sys);
 
         interp.setOut(listener.getLogger());
         interp.setErr(listener.getLogger());
        interp.exec(this.getContents());
         interp.cleanup();
 
         build.setResult(Result.SUCCESS);
         return true;
     }
 }
