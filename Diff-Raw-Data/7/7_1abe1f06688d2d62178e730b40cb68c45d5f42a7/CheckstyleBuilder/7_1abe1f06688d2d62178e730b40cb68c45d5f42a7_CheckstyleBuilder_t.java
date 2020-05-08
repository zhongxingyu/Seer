 package org.arachna.netweaver.nwdi.checkstyle;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Hudson;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.tasks.Ant;
 import hudson.util.FormValidation;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.StringWriter;
 import java.util.Collection;
 import java.util.HashSet;
 
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.context.Context;
 import org.arachna.netweaver.dc.types.DevelopmentComponent;
 import org.arachna.netweaver.hudson.nwdi.AntTaskBuilder;
 import org.arachna.netweaver.hudson.nwdi.DCWithJavaSourceAcceptingFilter;
 import org.arachna.netweaver.hudson.nwdi.NWDIBuild;
 import org.arachna.netweaver.hudson.nwdi.NWDIProject;
 import org.arachna.netweaver.hudson.util.FilePathHelper;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Sample {@link Builder}.
  * 
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
  * {@link CheckstyleBuilder} is created. The created instance is persisted to
  * the project configuration XML by using XStream, so this allows you to use
  * instance fields (like {@link #name}) to remember the configuration.
  * 
  * <p>
  * When a build is performed, the
  * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
  * invoked.
  * 
  * @author Kohsuke Kawaguchi
  */
 public class CheckstyleBuilder extends AntTaskBuilder {
     /**
      * 
      */
     private static final String CHECKSTYLE_BUILD_ALL_TARGET = "checkstyle-build-all";
 
     /**
      * 
      */
     private static final String CHECKSTYLE_BUILD_ALL_VM =
         "/org/arachna/netweaver/nwdi/checkstyle/checkstyle-build-all.vm";
 
     /**
      * 
      */
     private static final String CHECKSTYLE_BUILD_ALL_XML = "checkstyle-build-all.xml";
 
     /**
      * Descriptor for {@link CheckstyleBuilder}.
      */
     @Extension(ordinal = 1000)
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     /**
      * Name of checkstyle configuration file in workspace.
      */
     private static final String CHECKSTYLE_CONFIG_XML = "checkstyle-config.xml";
 
     /**
      * Data bound constructor. Used for populating a {@link CheckstyleBuilder}
      * instance from form fields in <code>config.jelly</code>.
      */
     @DataBoundConstructor
     public CheckstyleBuilder() {
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     @Override
     public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) {
         boolean result = true;
         PrintStream logger = listener.getLogger();
 
         try {
             final NWDIBuild nwdiBuild = (NWDIBuild)build;
             FilePath workspace = nwdiBuild.getWorkspace();
             FilePath checkstyleConfig = workspace.child(CHECKSTYLE_CONFIG_XML);
             checkstyleConfig.write(getDescriptor().getConfiguration(), "UTF-8");
 
             VelocityEngine engine = getVelocityEngine(logger);
 
             Collection<DevelopmentComponent> components =
                 nwdiBuild.getAffectedDevelopmentComponents(new DCWithJavaSourceAcceptingFilter());
             Collection<String> buildFiles = createBuildFiles(logger, components, checkstyleConfig, engine);
             createBuildFile(workspace, engine, buildFiles);
 
             Ant ant = new Ant("checkstyle-all", null, null, CHECKSTYLE_BUILD_ALL_XML, getAntProperties());
             ant.perform(build, launcher, listener);
         }
         catch (final IOException e) {
             e.printStackTrace(logger);
             result = false;
         }
         catch (final InterruptedException e) {
             e.printStackTrace(logger);
             // finish.
         }
 
         return result;
     }
 
     /**
      * @param listener
      * @param nwdiBuild
      * @param checkstyleConfig
      * @param engine
      * @return
      */
     private Collection<String> createBuildFiles(final PrintStream logger,
         final Collection<DevelopmentComponent> components, FilePath checkstyleConfig, VelocityEngine engine) {
         final BuildFileGenerator executor = createBuildFileGenerator(engine, logger, checkstyleConfig);
 
         for (final DevelopmentComponent component : components) {
             executor.execute(component);
         }
 
         return executor.getBuildFilePaths();
     }
 
     /**
      * Create the build file for calling all the given build files.
      * 
      * @param workspace
      *            workspace where to create the build file
      * @param engine
      *            velocity engine to use for creating the build file
      * @param paths
      *            to build files
      */
     private void createBuildFile(final FilePath workspace, VelocityEngine engine, final Collection<String> buildFiles) {
         try {
             StringWriter buildFile = new StringWriter();
             Context context = new VelocityContext();
             context.put("buildFiles", buildFiles);
             engine
                 .evaluate(context, buildFile, CHECKSTYLE_BUILD_ALL_TARGET, getTemplateReader(CHECKSTYLE_BUILD_ALL_VM));
             workspace.child(CHECKSTYLE_BUILD_ALL_XML).write(buildFile.toString(), "UTF-8");
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Get the properties to use calling ant.
      * 
      * @return the properties to use calling ant.
      */
     protected String getAntProperties() {
         return String.format("checkstyle.dir=%s/plugins/NWDI-Checkstyle-Plugin/WEB-INF/lib", Hudson.getInstance().root
             .getAbsolutePath().replace("\\", "/"));
     }
 
     /**
      * Create a {@link BuildFileGenerator} using the given {@link NWDIBuild}.
      * 
      * @param engine
      *            velocity engine to use for creating build files.
      * @param
      * @return the checkstyle executor object executing the analysis.
      */
     protected BuildFileGenerator createBuildFileGenerator(VelocityEngine engine, final PrintStream logger,
         final FilePath checkstyleConfig) {
         final DescriptorImpl descriptor = getDescriptor();
 
         return new BuildFileGenerator(engine, logger, getAntHelper(), FilePathHelper.makeAbsolute(checkstyleConfig),
             descriptor.getExcludes(), descriptor.getExcludeContainsRegexps());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DescriptorImpl getDescriptor() {
         return DESCRIPTOR;
     }
 
     /**
      * Descriptor for {@link CheckstyleBuilder}.
      */
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         /**
          * Persistent checkstyle configuration.
          * 
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private String configuration;
 
         /**
          * set of filename patterns to exclude from checkstyle checks.
          */
         private final Collection<String> excludes = new HashSet<String>();
 
         /**
          * set of regular expressions to exclude files from checkstyle checks
          * via their content.
          */
         private final Collection<String> excludeRegexps = new HashSet<String>();
 
         /**
          * Create descriptor for NWDI-CheckStyle-Builder and load global
          * configuration data.
          */
         public DescriptorImpl() {
             load();
         }
 
         /**
          * Performs on-the-fly validation of the form field 'name'.
          * 
          * @param value
          *            This parameter receives the value that the user has typed.
          * @return Indicates the outcome of the validation. This is sent to the
          *         browser.
          */
         public FormValidation doCheckConfiguration(@QueryParameter final String value) throws IOException,
             ServletException {
             return value.length() == 0 ? FormValidation.error("Please insert a checkstyle configuration.")
                 : FormValidation.ok();
 
             // FIXME: Add further configuration validation!
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         @Override
         public boolean isApplicable(final Class<? extends AbstractProject> aClass) {
             return NWDIProject.class.equals(aClass);
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         @Override
         public String getDisplayName() {
             return "NWDI Checkstyle Builder";
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
             this.configuration = formData.getString("configuration");
 
             final JSONObject advancedConfig = (JSONObject)formData.get("advancedConfiguration");
 
             if (advancedConfig != null) {
                 excludes.clear();
                 excludes.addAll(getExcludeItemDescriptions(formData, "excludes", "exclude"));
 
                 excludeRegexps.clear();
                 excludeRegexps.addAll(getExcludeItemDescriptions(advancedConfig, "excludeContainsRegexps", "regexp"));
             }
 
             save();
 
             return super.configure(req, formData);
         }
 
         /**
          * Extract item descriptions for exclusion from analysis.
          * 
          * @param advancedConfig
          *            JSON form containing advanced configuration data.
          * @param formName
          *            name of JSON form element to extract item descriptions
          *            from.
          * @param itemName
          *            name of configuration form item.
          * 
          * @return collection of item descriptions to exclude from checkstyle
          *         analysis
          */
         protected Collection<String> getExcludeItemDescriptions(final JSONObject advancedConfig, final String formName,
             final String itemName) {
             final Collection<String> descriptions = new HashSet<String>();
 
             final JSONArray excludes = JSONArray.fromObject(advancedConfig.get(formName));
 
             for (int i = 0; i < excludes.size(); i++) {
                 final JSONObject param = excludes.getJSONObject(i);
 
                 if (!param.isNullObject()) {
                     final String exclude = param.getString(itemName);
 
                     if (exclude.length() > 0) {
                         descriptions.add(exclude);
                     }
                 }
             }
 
             return descriptions;
 
         }
 
         /**
          * Return the checkstyle configuration.
          */
         public String getConfiguration() {
             return configuration;
         }
 
         /**
          * Sets the checkstyle configuration to be used for all NWDI checkstyle
          * builders.
          * 
          * @param configuration
          *            the configuration to set
          */
         public void setConfiguration(final String configuration) {
             this.configuration = configuration;
         }
 
         /**
          * Returns the list of file name patterns to exclude from checkstyle
          * checks.
          * 
          * @return the list of file name patterns to exclude from checkstyle
          *         checks.
          */
         public Collection<String> getExcludes() {
             return excludes;
         }
 
         /**
          * Sets the list of file name patterns to exclude from checkstyle
          * checks.
          * 
          * @param excludes
          *            the list of file name patterns to exclude from checkstyle
          *            checks.
          */
         public void setExcludes(final Collection<String> excludes) {
             this.excludes.clear();
 
             if (excludes != null) {
                 this.excludes.addAll(excludes);
             }
         }
 
         /**
          * @return the excludeContainsRegexps
          */
         public Collection<String> getExcludeContainsRegexps() {
             return excludeRegexps;
         }
 
         /**
          * @param excludeContainsRegexps
          *            the excludeContainsRegexps to set
          */
         public void setExcludeContainsRegexps(final Collection<String> excludeContainsRegexps) {
             excludeRegexps.clear();
 
             if (excludeContainsRegexps != null) {
                 excludeRegexps.addAll(excludeContainsRegexps);
             }
         }
     }
 }
