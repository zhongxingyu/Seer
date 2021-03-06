 package hudson.tasks;
 
 import hudson.CopyOnWrite;
 import hudson.FilePath.FileCallable;
 import hudson.Functions;
 import hudson.Launcher;
 import hudson.Launcher.LocalLauncher;
 import hudson.Util;
 import hudson.StructuredForm;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.remoting.Callable;
 import hudson.remoting.VirtualChannel;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormFieldValidator;
 import hudson.util.NullStream;
 import hudson.util.StreamTaskListener;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import javax.servlet.ServletException;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import net.sf.json.JSONObject;
 
 /**
  * Build by using Maven.
  *
  * @author Kohsuke Kawaguchi
  */
 public class Maven extends Builder {
     /**
      * The targets and other maven options.
      * Can be separated by SP or NL.
      */
     private final String targets;
 
     /**
      * Identifies {@link MavenInstallation} to be used.
      */
     private final String mavenName;
 
     @DataBoundConstructor
     public Maven(String targets,String name) {
         this.targets = targets;
         this.mavenName = name;
     }
 
     public String getTargets() {
         return targets;
     }
 
     /**
      * Gets the Maven to invoke,
      * or null to invoke the default one.
      */
     public MavenInstallation getMaven() {
         for( MavenInstallation i : DESCRIPTOR.getInstallations() ) {
             if(mavenName !=null && i.getName().equals(mavenName))
                 return i;
         }
         return null;
     }
 
     /**
      * Looks for <tt>pom.xlm</tt> or <tt>project.xml</tt> to determine the maven executable
      * name.
      */
     private static final class DecideDefaultMavenCommand implements FileCallable<String> {
         // command line arguments.
         private final String arguments;
 
         public DecideDefaultMavenCommand(String arguments) {
             this.arguments = arguments;
         }
 
         public String invoke(File ws, VirtualChannel channel) throws IOException {
             String seed=null;
 
             // check for the -f option
             StringTokenizer tokens = new StringTokenizer(arguments);
             while(tokens.hasMoreTokens()) {
                 String t = tokens.nextToken();
                 if(t.equals("-f") && tokens.hasMoreTokens()) {
                     File file = new File(ws,tokens.nextToken());
                     if(!file.exists())
                         continue;   // looks like an error, but let the execution fail later
                     if(file.isDirectory())
                         // in M1, you specify a directory in -f
                         seed = "maven";
                     else
                         // in M2, you specify a POM file name.
                         seed = "mvn";
                     break;
                 }
             }
 
             if(seed==null) {
                 if(new File(ws,"pom.xml").exists())
                     seed = "mvn";
                 else
                     // err on Maven 1 to be closer to the behavior in < 1.81
                     seed = "maven";
             }
 
             if(Functions.isWindows())
                 seed += ".bat";
             return seed;
         }
     }
 
     public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
         AbstractProject proj = build.getProject();
 
         int startIndex = 0;
         int endIndex;
         do {
             // split targets into multiple invokations of maven separated by |
             endIndex = targets.indexOf('|', startIndex);
             if (-1 == endIndex) {
                 endIndex = targets.length();
             }
 
             Map<String,String> env = build.getEnvVars();
 
             String normalizedTarget = targets
                     .substring(startIndex, endIndex)
                     .replaceAll("[\t\r\n]+"," ");
             normalizedTarget = Util.replaceMacro(normalizedTarget,env);
 
             ArgumentListBuilder args = new ArgumentListBuilder();
             MavenInstallation ai = getMaven();
             if(ai==null) {
                 String execName = proj.getWorkspace().act(new DecideDefaultMavenCommand(normalizedTarget));
                 args.add(execName);
             } else {
                 String exec = ai.getExecutable(launcher);
                 if(exec==null) {
                     listener.fatalError("Couldn't find any executable in "+ai.getMavenHome());
                     return false;
                 }
                 args.add(exec);
             }
             args.addKeyValuePairs("-D",build.getBuildVariables());
             args.addTokenized(normalizedTarget);
 
             if(ai!=null) {
                 // if somebody has use M2_HOME they will get a classloading error
                 // when M2_HOME points to a different version of Maven2 from
                 // MAVEN_HOME (as Maven 2 gives M2_HOME priority)
                 // 
                 // The other solution would be to set M2_HOME if we are calling Maven2 
                 // and MAVEN_HOME for Maven1 (only of use for strange people that
                 // are calling Maven2 from Maven1)
                 env.remove("M2_HOME");
                 env.put("MAVEN_HOME",ai.getMavenHome());
             }
             // just as a precaution
             // see http://maven.apache.org/continuum/faqs.html#how-does-continuum-detect-a-successful-build
             env.put("MAVEN_TERMINATE_CMD","on");
 
             try {
                 int r = launcher.launch(args.toCommandArray(),env,listener.getLogger(),proj.getModuleRoot()).join();
                 if (0 != r) {
                     return false;
                 }
             } catch (IOException e) {
                 Util.displayIOException(e,listener);
                 e.printStackTrace( listener.fatalError("command execution failed") );
                 return false;
             }
             startIndex = endIndex + 1;
         } while (startIndex < targets.length());
         return true;
     }
 
     public Descriptor<Builder> getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends Descriptor<Builder> {
         @CopyOnWrite
         private volatile MavenInstallation[] installations = new MavenInstallation[0];
 
         private DescriptorImpl() {
             super(Maven.class);
             load();
         }
 
 
         protected void convert(Map<String, Object> oldPropertyBag) {
             if(oldPropertyBag.containsKey("installations"))
                 installations = (MavenInstallation[]) oldPropertyBag.get("installations");
         }
 
         public String getHelpFile() {
             return "/help/project-config/maven.html";
         }
 
         public String getDisplayName() {
             return "Invoke top-level Maven targets";
         }
 
         public MavenInstallation[] getInstallations() {
             return installations;
         }
 
         public boolean configure(StaplerRequest req) {
             this.installations = req.bindJSONToList(MavenInstallation.class,StructuredForm.get(req).get("maven"))
                     .toArray(new MavenInstallation[0]);
             save();
             return true;
         }
 
         public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             return req.bindJSON(Maven.class,formData);
         }
 
 
     //
     // web methods
     //
         /**
          * Checks if the MAVEN_HOME is valid.
          */
         public void doCheckMavenHome( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
             // this can be used to check the existence of a file on the server, so needs to be protected
             new FormFieldValidator(req,rsp,true) {
                 public void check() throws IOException, ServletException {
                     File f = getFileParameter("value");
                     if(f.getPath().equals("")) {
                         error("MAVEN_HOME is required");
                         return;
                     }
                     if(!f.isDirectory()) {
                         error(f+" is not a directory");
                         return;
                     }
 
                     // I couldn't come up with a simple logic to test for a maven installation
                     // there seems to be just too much difference between m1 and m2.
 
                     ok();
                 }
             }.process();
         }
     }
 
     public static final class MavenInstallation implements Serializable {
         private final String name;
         private final String mavenHome;
 
         @DataBoundConstructor
        public MavenInstallation(String name, String mavenHome) {
             this.name = name;
            this.mavenHome = mavenHome;
         }
 
         /**
          * install directory.
          */
         public String getMavenHome() {
             return mavenHome;
         }
 
         public File getHomeDir() {
             return new File(mavenHome);
         }
 
         /**
          * Human readable display name.
          */
         public String getName() {
             return name;
         }
 
         /**
          * Gets the executable path of this maven on the given target system.
          */
         public String getExecutable(Launcher launcher) throws IOException, InterruptedException {
             return launcher.getChannel().call(new Callable<String,IOException>() {
                 public String call() throws IOException {
                     File exe = getExeFile("maven");
                     if(exe.exists())
                         return exe.getPath();
                     exe = getExeFile("mvn");
                     if(exe.exists())
                         return exe.getPath();
                     return null;
                 }
             });
         }
 
         private File getExeFile(String execName) {
             if(File.separatorChar=='\\')
                 execName += ".bat";
             return new File(getMavenHome(), "bin/" + execName);
         }
 
         /**
          * Returns true if the executable exists.
          */
         public boolean getExists() {
             try {
                 return getExecutable(new LocalLauncher(new StreamTaskListener(new NullStream())))!=null;
             } catch (IOException e) {
                 return false;
             } catch (InterruptedException e) {
                 return false;
             }
         }
 
         private static final long serialVersionUID = 1L;
     }
 }
