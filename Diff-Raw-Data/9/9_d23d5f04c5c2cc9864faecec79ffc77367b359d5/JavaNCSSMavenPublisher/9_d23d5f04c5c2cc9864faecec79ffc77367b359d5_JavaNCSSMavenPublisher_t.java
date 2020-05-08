 package hudson.plugins.javancss;
 
 import hudson.Extension;
 import hudson.maven.*;
 import hudson.model.Action;
 import hudson.plugins.helpers.AbstractMavenReporterImpl;
 import hudson.plugins.helpers.Ghostwriter;
 import hudson.plugins.helpers.health.HealthMetric;
 import net.sf.json.JSONObject;
 import org.apache.commons.beanutils.ConvertUtils;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import java.io.File;
 
 /**
  * TODO javadoc.
  *
  * @author Stephen Connolly
  * @since 08-Jan-2008 21:26:06
  */
 public class JavaNCSSMavenPublisher extends AbstractMavenReporterImpl {
     private JavaNCSSHealthTarget[] targets;
 
     @DataBoundConstructor
     public JavaNCSSMavenPublisher(JavaNCSSHealthTarget... targets) {
         this.targets = targets == null ? new JavaNCSSHealthTarget[0] : targets;
     }
 
     public JavaNCSSHealthTarget[] getTargets() {
         return targets;
     }
 
     /**
      * The groupId of the Maven plugin that provides the functionality we want to report on.
      */
     private static final String PLUGIN_GROUP_ID = "org.codehaus.mojo";
     /**
      * The artifactId of the Maven plugin that provides the functionality we want to report on.
      */
     private static final String PLUGIN_ARTIFACT_ID = "javancss-maven-plugin";
 
     protected boolean isExecutingMojo(MojoInfo mojo) {
        return (mojo.pluginName.matches(PLUGIN_GROUP_ID, PLUGIN_ARTIFACT_ID)
                && "report".equals(mojo.getGoal())) || "site".equals(mojo.getGoal());
     }
 
     protected Ghostwriter newGhostwriter(MavenProject pom, MojoInfo mojo) {
         String tempFileName;
         try {
             tempFileName = mojo.getConfigurationValue("tempFileName", String.class);
         } catch (ComponentConfigurationException e) {
             tempFileName = null;
         }
         if (tempFileName == null) {
             tempFileName = "javancss-raw-report.xml";
         }
         System.out.println(tempFileName);
         File baseDir = pom.getBasedir().getAbsoluteFile();
         File xmlOutputDirectory;
         try {
             xmlOutputDirectory = mojo.getConfigurationValue("xmlOutputDirector", File.class);
         } catch (ComponentConfigurationException e) {
             xmlOutputDirectory = null;
         }
         if (xmlOutputDirectory == null) {
             xmlOutputDirectory = new File(pom.getBuild().getDirectory());
         }
         System.out.println(baseDir);
         System.out.println(xmlOutputDirectory);
         String searchPath;
         String targetPath = makeDirEndWithFileSeparator(fixFilePathSeparator(xmlOutputDirectory.getAbsolutePath()));
         String baseDirPath = makeDirEndWithFileSeparator(fixFilePathSeparator(baseDir.getAbsolutePath()));
         if (targetPath.startsWith(baseDirPath)) {
             searchPath = targetPath.substring(baseDirPath.length()) + tempFileName;
         } else {
             searchPath = "**/" + tempFileName;
         }
 
         return new JavaNCSSGhostwriter(searchPath, targets);
     }
 
     private String makeDirEndWithFileSeparator(String baseDirPath) {
         if (!baseDirPath.endsWith(File.separator)) {
             baseDirPath += File.separator;
         }
         return baseDirPath;
     }
 
     private String fixFilePathSeparator(String path) {
         return path.replace(File.separatorChar == '/' ? '\\' : '/', File.separatorChar);
     }
 
     @Override
     public Action getProjectAction(MavenModule module) {
         for (MavenBuild build : module.getBuilds()) {
             if (build.getAction(JavaNCSSBuildIndividualReport.class) != null) {
                 return new JavaNCSSProjectIndividualReport(module);
             }
         }
         return null;
     }
 
     @Extension
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public MavenReporterDescriptor getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final class DescriptorImpl extends MavenReporterDescriptor {
 
         /**
          * Do not instantiate DescriptorImpl.
          */
         private DescriptorImpl() {
             super(JavaNCSSMavenPublisher.class);
         }
 
         /**
          * {@inheritDoc}
          */
         public String getDisplayName() {
             return "Publish " + PluginImpl.DISPLAY_NAME;
         }
 
         @Override
         public MavenReporter newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             ConvertUtils.register(JavaNCSSHealthMetrics.CONVERTER, JavaNCSSHealthMetrics.class);
             return req.bindJSON(JavaNCSSMavenPublisher.class, formData);
         }
 
         public HealthMetric[] getMetrics() {
             return JavaNCSSHealthMetrics.values();
         }
     }
 
 }
