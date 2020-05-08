 package hudson.plugins.googlecode;
 
 import hudson.model.FreeStyleProject;
 import hudson.plugins.googlecode.scm.GoogleCodeSCM;
 import hudson.scm.SCMS;
 
 import org.jvnet.hudson.test.HudsonTestCase;
 
 import com.gargoylesoftware.htmlunit.html.HtmlButton;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
 
 public class ConfigurationTest extends HudsonTestCase {
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         SCMS.SCMS.add(PluginImpl.GOOGLE_CODE_SCM_DESCRIPTOR);
     }
     
     @Override
     protected void tearDown() throws Exception {
         SCMS.SCMS.remove(PluginImpl.GOOGLE_CODE_SCM_DESCRIPTOR);
         super.tearDown();
     }
 
     /**
      * Asserts that configuration works
      */
     public void testConfiguredRepositoryBrowserCanBeCreated() throws Exception {
 
         FreeStyleProject project = createFreeStyleProject();
         project.setScm(new GoogleCodeSCM("path2"));
 
         HtmlForm form = new WebClient().getPage(project,"configure").getFormByName("config");
         form.submit((HtmlButton)last(form.getHtmlElementsByTagName("button")));
         assertEquals("The project's SCM wasnt Google code", GoogleCodeSCM.class,  project.getScm().getClass());
     }
 }
