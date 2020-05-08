 package jp.haya10;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 
 import jp.vmi.selenium.selenese.Runner;
 import jp.vmi.selenium.selenese.result.Result;
 import jp.vmi.selenium.webdriver.WebDriverManager;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link SeleneseRunnerBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
  * method will be invoked. 
  *
  * @author Hayato Ito
  */
 public class SeleneseRunnerBuilder extends Builder {
 
     private final String seleneseFile;
 
     private final String browser;
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public SeleneseRunnerBuilder(String seleneseFile, String browser) {
         this.seleneseFile = seleneseFile;
         this.browser = browser;
     }
 
     public String getSeleneseFile() {
         return seleneseFile;
     }
 
     public String getBrowser() {
         return browser;
     }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
         listener.getLogger().println("selenese start.");
 
         WebDriverManager manager = WebDriverManager.getInstance();
         try {
             manager.setWebDriverFactory(browser);
             Runner runner = new Runner();
             runner.setDriver(manager.get());
             Result result = runner.run(getSeleneseFile());
             result.getMessage();
             return result.isSuccess();
         } catch (Throwable t) {
             t.printStackTrace(listener.getLogger());
             return false;
         } finally {
             listener.getLogger().println("selenese finished.");
             manager.quitAllDrivers();
         }
     }
 
     // Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
     }
 
     /**
      * Descriptor for {@link SeleneseRunnerBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      *
      * <p>
      * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
      * for the actual HTML fragment for the configuration screen.
      */
     @Extension
     // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         /**
          * Performs on-the-fly validation of the form field 'seleneseFile'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckSeleneseFile(@QueryParameter String value)
             throws IOException, ServletException {
             if (StringUtils.isEmpty(value))
                 return FormValidation.error("Please set a selenese script filename");
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
             return "Run selenese script";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             // To persist global configuration information,
             // set that to properties and call save().
 
             // ^Can also use req.bindJSON(this, formData);
             //  (easier when there are many fields; need set* methods for this, like setUseFrench)
             save();
             return super.configure(req, formData);
         }
     }
 }
