 package jp.haya10.jenkins.seleneserunnerplugin;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Descriptor;
 import hudson.remoting.Callable;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 
 import java.io.File;
 import java.io.Serializable;
 import java.util.Map;
 
 import jp.vmi.selenium.selenese.Runner;
 import jp.vmi.selenium.webdriver.DriverOptions;
 import jp.vmi.selenium.webdriver.DriverOptions.DriverOption;
 import jp.vmi.selenium.webdriver.WebDriverManager;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.validator.routines.UrlValidator;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link SeleneseRunnerBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #seleneseFile})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
  * method will be invoked. 
  *
  * @author Hayato Ito
  */
 public class SeleneseRunnerBuilder extends Builder implements Serializable {
 
     private final String seleneseFile;
     private static final long serialVersionUID = -6980802829735878901L;
 
     private final String browser;
 
     private final boolean screenshotAll;
 
     private final boolean screenshotOnFail;
 
     private final String baseUrl;
 
     private final String screenshotDir;
 
     private final String junitresult;
 
     private static class SeleneseRunnerCallable implements Callable<Boolean, Throwable> {
         private static final long serialVersionUID = 2416651790883391162L;
 
         SeleneseRunnerBuilder builder;
         BuildListener listener;
         FilePath junitdir;
         FilePath screenshotDirPath;
         FilePath seleneseFilePath;
         Map<String, String> env;
 
         public SeleneseRunnerCallable(SeleneseRunnerBuilder builder, BuildListener listener, FilePath junitdir,
             FilePath screenshotDirPath, FilePath seleneseFilePath, Map<String, String> env) {
             super();
             this.builder = builder;
             this.listener = listener;
             this.junitdir = junitdir;
             this.screenshotDirPath = screenshotDirPath;
             this.seleneseFilePath = seleneseFilePath;
             this.env = env;
         }
 
         public Boolean call() throws Throwable {
             final Runner runner = new Runner();
             //baseURL
             if (!StringUtils.isEmpty(builder.getBaseUrl())) {
                 runner.setBaseURL(builder.getBaseUrl());
             }
 
             //console log
             runner.setPrintStream(listener.getLogger());
 
             //screenshot dir
             if (builder.screenshotAll && !StringUtils.isEmpty(builder.screenshotDir)) {
                 runner.setScreenshotAllDir(screenshotDirPath.getRemote());
             }
             if (builder.screenshotOnFail && !StringUtils.isEmpty(builder.screenshotDir)) {
                 runner.setScreenshotOnFailDir(screenshotDirPath.getRemote());
             }
             if (!StringUtils.isEmpty(builder.screenshotDir)) {
                 runner.setScreenshotDir(screenshotDirPath.getRemote());
             }
 
             if (junitdir != null) {
                 runner.setJUnitResultDir(junitdir.getRemote());
             }
 
             //driver
             final WebDriverManager manager = WebDriverManager.getInstance();
             try {
                 manager.setWebDriverFactory(builder.browser);
                 listener.getLogger().println("browser:" + builder.browser);
                 if (builder.browser.equals(WebDriverManager.CHROME)) {
                     DriverOptions opt = new DriverOptions();
                     opt.set(DriverOption.CHROMEDRIVER, PathUtils
                         .searchExecutableFile("chromedriver").get(0).getAbsolutePath());
                     listener.getLogger().println("chromedriver:" + opt.get(DriverOption.CHROMEDRIVER));
                 }
                if (builder.browser.equals(WebDriverManager.PHANTOMJS)) {
                    DriverOptions opt = new DriverOptions();
                    opt.set(DriverOption.DEFINE, PhantomJSDriverService.PHANTOMJS_CLI_ARGS + "+=--webdriver-logfile=");
                    manager.setDriverOptions(opt);
                }

                 manager.getEnvironmentVariables().clear();
                 manager.getEnvironmentVariables().putAll(env);
 
                 runner.setDriver(manager.get());
 
                 return runner.run(seleneseFilePath.getRemote()).isSuccess();
             } finally {
                 manager.quitAllDrivers();
             }
         }
 
     }
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     /**
      * @param seleneseFile
      * @param browser
      * @param screenshotAll
      * @param screenshotOnFail
      * @param screenshotDir
      * @param baseUrl
      * @param junitresult
      */
     @DataBoundConstructor
     public SeleneseRunnerBuilder(final String seleneseFile, final String browser, final boolean screenshotAll,
         final boolean screenshotOnFail,
         final String screenshotDir, final String baseUrl, final String junitresult) {
         this.seleneseFile = seleneseFile;
         this.browser = browser;
         this.screenshotAll = screenshotAll;
         this.screenshotOnFail = screenshotOnFail;
         this.screenshotDir = screenshotDir;
         this.baseUrl = baseUrl;
         this.junitresult = junitresult;
     }
 
     public String getSeleneseFile() {
         return seleneseFile;
     }
 
     public boolean isScreenshotAll() {
         return screenshotAll;
     }
 
     public boolean isScreenshotOnFail() {
         return screenshotOnFail;
     }
 
     public String getScreenshotDir() {
         return screenshotDir;
     }
 
     public String getBaseUrl() {
         return StringUtils.trimToEmpty(baseUrl);
     }
 
     public String getJunitresult() {
         return junitresult;
     }
 
     public String getBrowser() {
         return browser;
     }
 
     @Override
     public boolean perform(final AbstractBuild build, Launcher launcher, final BuildListener listener) {
         listener.getLogger().println("selenese start.");
 
         try {
 
             //Environment
             final Map<String, String> env = build.getEnvironment(listener).descendingMap();
 
             //scrennshot
             final FilePath screenshotDirPath = build.getWorkspace().child(screenshotDir);
             screenshotDirPath.mkdirs();
 
             listener.getLogger().println("selenese file : " + getSeleneseFile());
             listener.getLogger().println("override baseUrl : " + baseUrl);
 
             //selenese file
             final FilePath seleneseFilePath = build.getWorkspace().child(getSeleneseFile());
 
             //junitdir
             FilePath junitdir = null;
             if (!StringUtils.isEmpty(getJunitresult())) {
                 junitdir = build.getWorkspace().child(getJunitresult());
                 junitdir.mkdirs();
                 listener.getLogger().println("output junitresult xml to :" + getJunitresult());
             }
 
             //boot selenese-runner on the target.
             SeleneseRunnerCallable callable = new SeleneseRunnerCallable(this, listener, junitdir, screenshotDirPath, seleneseFilePath,
                 env);
             return launcher.getChannel().call(callable);
         } catch (Throwable t) {
             t.printStackTrace(listener.getLogger());
             return false;
         } finally {
             listener.getLogger().println("selenese finished.");
         }
     }
 
     // Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public Descriptor<Builder> getDescriptor() {
         return new SeleneseRunnerBuilder.DescriptorImpl();
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
         public FormValidation doCheckSeleneseFile(@QueryParameter String value) {
             if (StringUtils.isEmpty(value))
                 return FormValidation.error("Please set a selenese script filename");
             if (!new File(value).exists())
                 return FormValidation.warning("File is not exixts.");
             return FormValidation.ok();
         }
 
         public FormValidation doCheckBaseUrl(@QueryParameter String value) {
             String[] schemes = { "http", "https" };
             UrlValidator urlValidator = new UrlValidator(schemes);
             if (StringUtils.isEmpty(value) || urlValidator.isValid(value)) {
                 return FormValidation.ok();
             } else {
                 return FormValidation.error("This url is not valid.");
             }
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
