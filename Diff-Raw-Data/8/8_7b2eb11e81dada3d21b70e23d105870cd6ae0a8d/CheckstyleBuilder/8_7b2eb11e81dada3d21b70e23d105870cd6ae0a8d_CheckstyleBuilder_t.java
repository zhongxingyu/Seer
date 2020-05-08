 package org.arachna.netweaver.nwdi.checkstyle;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 
 import javax.servlet.ServletException;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.fileupload.FileItem;
 import org.arachna.netweaver.dc.types.DevelopmentComponent;
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
 public class CheckstyleBuilder extends Builder {
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
 
         try {
             final NWDIBuild nwdiBuild = (NWDIBuild)build;
             final Collection<DevelopmentComponent> components = nwdiBuild.getAffectedDevelopmentComponents();
             nwdiBuild.getWorkspace().child(CHECKSTYLE_CONFIG_XML).write(getDescriptor().getConfiguration(), "UTF-8");
 
             final CheckStyleExecutor executor = createExecutor(nwdiBuild);
 
             for (final DevelopmentComponent component : components) {
                 executor.execute(component);
             }
         }
         catch (final IOException e) {
             e.printStackTrace(listener.getLogger());
             result = false;
         }
         catch (final InterruptedException e) {
             e.printStackTrace(listener.getLogger());
             // finish.
         }
 
         return result;
     }
 
     /**
      * Create a {@link CheckStyleExecutor} using the given {@link NWDIBuild}.
      * 
      * @param nwdiBuild
      *            build object
      * @return the checkstyle executor object executing the analysis.
      */
     protected CheckStyleExecutor createExecutor(final NWDIBuild nwdiBuild) {
         final String pathToWorkspace = FilePathHelper.makeAbsolute(nwdiBuild.getWorkspace());
         final DescriptorImpl descriptor = getDescriptor();
 
         return new CheckStyleExecutor(pathToWorkspace, new File(pathToWorkspace + File.separatorChar
             + CHECKSTYLE_CONFIG_XML), descriptor.getExcludes(), descriptor.getExcludeContainsRegexps());
     }
 
     /**
      * 
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
             return "NWDI Checkstyle";
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         @Override
         public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException {
             try {
                 final String fileKey = formData.getString("checkStyleConfiguration");
                 final FileItem item = req.getFileItem(fileKey);
 
                 if (item != null) {
                     final String content = item.getString();
 
                     if (content != null && content.trim().length() > 0) {
                         configuration = content;
                     }
                 }
             }
             catch (final ServletException e) {
                 throw new FormException(e, "ServletException");
             }
             catch (final IOException e) {
                 throw new FormException(e, "IOException");
             }
 
             excludes.clear();
             excludes.addAll(getExcludeItemDescriptions(formData, "excludes", "exclude"));
 
             excludeRegexps.clear();
             excludeRegexps.addAll(getExcludeItemDescriptions(formData, "excludeContainsRegexps", "regexp"));
 
             save();
 
             return super.configure(req, formData);
         }
 
         /**
          * Extract item descriptions for exclusion from analysis.
          * 
          * @param formData
          *            JSON form containing configuration data.
          * @param formName
          *            name of JSON form element to extract item descriptions
          *            from.
          * @param itemName
          *            name of configuration form item.
          * 
          * @return collection of item descriptions to exclude from checkstyle
          *         analysis
          */
         protected Collection<String> getExcludeItemDescriptions(final JSONObject formData, final String formName,
             final String itemName) {
             final Collection<String> descriptions = new HashSet<String>();
 
            final JSONObject advancedConfig = (JSONObject)formData.get("advancedConfiguration");

            final JSONArray excludes = JSONArray.fromObject(advancedConfig.get(formName));
 
             for (int i = 0; i < excludes.size(); i++) {
                 final JSONObject param = excludes.getJSONObject(i);
                 final String exclude = param.getString(itemName);
 
                 if (exclude.length() > 0) {
                     descriptions.add(exclude);
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
