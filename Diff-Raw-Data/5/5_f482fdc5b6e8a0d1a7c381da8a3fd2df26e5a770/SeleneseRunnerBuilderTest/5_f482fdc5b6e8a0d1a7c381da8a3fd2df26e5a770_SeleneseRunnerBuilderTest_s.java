 package jp.haya10.jenkins.seleneserunnerplugin;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 import hudson.FilePath;
 import hudson.model.FreeStyleBuild;
 import hudson.model.Result;
 import hudson.model.FreeStyleProject;
 import hudson.model.labels.LabelAtom;
 import hudson.slaves.DumbSlave;
 
 import java.io.File;
 
 import jp.vmi.selenium.webdriver.DriverOptions;
 import jp.vmi.selenium.webdriver.WebDriverManager;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Assume;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.internal.AssumptionViolatedException;
 import org.jvnet.hudson.test.JenkinsRule;
 import org.openqa.selenium.WebDriverException;
 
 public class SeleneseRunnerBuilderTest {
     @Rule
     public JenkinsRule j = new JenkinsRule();
 
     private static boolean noDisplay = false;
 
     @Test
     public void testAbsolutePathSelenese() throws Exception {
         FreeStyleProject p = j.createFreeStyleProject();
         DumbSlave slave = j.createOnlineSlave(new LabelAtom("test"));
         p.setAssignedLabel(new LabelAtom("test"));
 
         String file = TestUtils.getScriptFile(this.getClass(), "Simple");
         p.getBuildersList().add(
            new SeleneseRunnerBuilder(file, WebDriverManager.FIREFOX, true, true, "./screenshot", "", "junitresult"));
 
         assertThat(new File(file).exists(), is(true));
         try {
             j.assertBuildStatus(Result.SUCCESS, p.scheduleBuild2(0).get());
         } finally {
             for (String log : p.getLastBuild().getLog(100)) {
                 System.out.println(log);
             }
         }
 
         FilePath screenshot = p.getSomeWorkspace().child("./screenshot");
         assertThat(screenshot.list().isEmpty(), is(false));
     }
 
     @Test
     public void testRelPathSelenese() throws Exception {
         FreeStyleProject p = j.createFreeStyleProject();
         DumbSlave slave = j.createOnlineSlave(new LabelAtom("test"));
         p.setAssignedLabel(new LabelAtom("test"));
 
         //null build and getworkspace
         FilePath workspace = p.scheduleBuild2(0).get().getWorkspace();
 
         //copy selenese to workspace
         File src = new File(TestUtils.getScriptFile(this.getClass(), "Simple"));
         File target = new File(workspace.createTempFile("selenese", ".html").getRemote());
         FileUtils.copyFile(src, target);
         assertThat(new File(target.getAbsolutePath()).exists(), is(true));
 
         assertThat(target.getName().substring(0, 8), is("selenese"));
 
         p.getBuildersList().add(
            new SeleneseRunnerBuilder(target.getName(), WebDriverManager.FIREFOX, true, true, "./screenshot", "", "junitresult"));
 
         try {
             FreeStyleBuild build = p.scheduleBuild2(0).get();
             j.assertBuildStatus(Result.SUCCESS, build);
             FilePath screenshot = build.getWorkspace().child("screenshot");
             assertNotNull(screenshot);
             assertThat(screenshot.list().isEmpty(), is(false));
 
             // test logging to console log.
             assertThat(StringUtils.join(p.getLastBuild().getLog(100).toArray()), containsString("[INFO]"));
         } finally {
             for (String log : p.getLastBuild().getLog(100)) {
                 System.out.println(log);
             }
         }
 
     }
 
     /**
      * Check Firefox connected.
      */
     @Before
     public void assumeConnectFirefox() throws Exception {
         if (noDisplay)
             throw new AssumptionViolatedException("no display specified");
 
         setupWebDriverManager();
         try {
             WebDriverManager.getInstance().get();
         } catch (WebDriverException e) {
             if (e.getMessage().contains("no display specified")) {
                 noDisplay = true;
                 Assume.assumeNoException(e);
             }
         }
     }
 
     private void setupWebDriverManager() {
         WebDriverManager manager = WebDriverManager.getInstance();
         manager.setWebDriverFactory(WebDriverManager.FIREFOX);
         manager.setDriverOptions(new DriverOptions());
     }
 }
