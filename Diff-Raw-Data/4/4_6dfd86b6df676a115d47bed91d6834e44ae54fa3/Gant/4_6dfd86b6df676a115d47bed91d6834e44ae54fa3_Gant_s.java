 package hudson.plugins.gant;
 
 import hudson.tasks.Builder;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Project;
 import hudson.model.Descriptor;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.CopyOnWrite;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormFieldValidator;
 
 import java.io.IOException;
 import java.io.File;
 import java.util.Map;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 
 /**
  * @author Kohsuke Kawaguchi
  */
 public class Gant extends Builder {
     /**
      * The targets, properties, and other Gant options.
      * Either separated by whitespace or newline.
      */
     private final String targets;
 
     /**
      * Identifies {@link GantInstallation} to be used.
      */
     private final String gantName;
 
     @DataBoundConstructor
     public Gant(String targets,String gantName) {
         this.targets = targets;
         this.gantName = gantName;
     }
 
     public String getTargets() {
         return targets;
     }
 
     /**
      * Gets the Gant to invoke,
      * or null to invoke the default one.
      */
     public GantInstallation getGant() {
         for( GantInstallation i : DESCRIPTOR.getInstallations() ) {
             if(gantName!=null && i.getName().equals(gantName))
                 return i;
         }
         return null;
     }
 
     public boolean perform(Build<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
         Project proj = build.getProject();
 
         ArgumentListBuilder args = new ArgumentListBuilder();
 
         String execName;
         if(launcher.isUnix())
             execName = "gant";
         else
             execName = "gant.bat";
 
         String normalizedTarget = targets.replaceAll("[\t\r\n]+"," ");
 
         GantInstallation ai = getGant();
         if(ai==null) {
             args.add(execName);
         } else {
             File exec = ai.getExecutable();
             if(!ai.getExists()) {
                 listener.fatalError(exec+" doesn't exist");
                 return false;
             }
             args.add(exec.getPath());
         }
         args.addKeyValuePairs("-D",build.getBuildVariables());
         args.addTokenized(normalizedTarget);
 
         Map<String,String> env = build.getEnvVars();
         if(ai!=null)
             env.put("GROOVY_HOME",ai.getGroovyHome());
 
         if(!launcher.isUnix()) {
             // on Windows, executing batch file can't return the correct error code,
             // so we need to wrap it into cmd.exe.
             // double %% is needed because we want ERRORLEVEL to be expanded after
             // batch file executed, not before. This alone shows how broken Windows is...
             args.prepend("cmd.exe","/C");
             args.add("&&","exit","%%ERRORLEVEL%%");
         }
 
         try {
             int r = launcher.launch(args.toCommandArray(),env,listener.getLogger(),proj.getModuleRoot()).join();
             return r==0;
         } catch (IOException e) {
             Util.displayIOException(e,listener);
             e.printStackTrace( listener.fatalError("command execution failed") );
             return false;
         }
     }
 
     public Descriptor<Builder> getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends Descriptor<Builder> {
         @CopyOnWrite
         private volatile GantInstallation[] installations = new GantInstallation[0];
 
         private DescriptorImpl() {
             super(Gant.class);
             load();
         }
 
         public String getHelpFile() {
             return "/plugin/gant/help.html";
         }
 
         public String getDisplayName() {
             return "Invoke Gant script";
         }
 
         public GantInstallation[] getInstallations() {
             return installations;
         }
 
         public boolean configure(StaplerRequest req) {
             installations = req.bindParametersToList(
                 GantInstallation.class,"gant.").toArray(new GantInstallation[0]);
             save();
             return true;
         }
 
        public Builder newInstance(StaplerRequest req) {
            return req.bindParameters(Gant.class,"gant.");
        }

     //
     // web methods
     //
         /**
          * Checks if the GROOVY_HOME is valid.
          */
         public void doCheckGroovyHome( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
             // this can be used to check the existence of a file on the server, so needs to be protected
             new FormFieldValidator(req,rsp,true) {
                 public void check() throws IOException, ServletException {
                     File f = getFileParameter("value");
                     if(!f.isDirectory()) {
                         error(f+" is not a directory");
                         return;
                     }
 
                     if(!new File(f,"bin/groovy").exists() && !new File(f,"bin/groovy.bat").exists()) {
                         error(f+" doesn't look like a Groovy directory");
                         return;
                     }
 
                     if(!new File(f,"bin/gant").exists() && !new File(f,"bin/gant.bat").exists()) {
                         error(f+" looks like a Groovy but Gant is not found in here");
                         return;
                     }
 
                     ok();
                 }
             }.process();
         }
     }
 
 }
