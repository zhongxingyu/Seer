 package de.flower.rmt.test;
 
 import de.flower.common.test.wicket.WicketTester;
 import de.flower.rmt.model.RSVPStatus;
 import de.flower.rmt.model.type.Notification;
 import de.flower.rmt.model.type.Password;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.mockito.Mockito;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 /**
  * Base class for ui-test that require full application stack down to database.
  *
  * @author flowerrrr
  */
 public abstract class AbstractWicketIntegrationTests extends AbstractIntegrationTests {
 
     protected WicketTester wicketTester = null;
 
     @Autowired
     private WebApplication webApp;
 
     protected WebApplication createWebApp(ApplicationContext ctx) {
 //        return new TestApplication(ctx);
         return webApp;
     }
 
     @BeforeMethod
     public void init() {
        createTester(applicationContext);
     }
 
     @AfterMethod
     public void cleanup() {
         Mockito.validateMockitoUsage();
     }
 
     private void createTester(ApplicationContext ctx) {
         WebApplication webApp = createWebApp(ctx);
         wicketTester = new WicketTester(webApp);
         // TODO (flowerrrr - 02.04.12) redundant information! use settings from tested app
         wicketTester.getLoggingSerializerFilter().addInclusion("\"de\\.flower\\.rmt\\.model\\.[^-]*?\"");
         wicketTester.getLoggingSerializerFilter().addExclusion(RSVPStatus.class.getName());
         wicketTester.getLoggingSerializerFilter().addExclusion(Password.class.getName());
         wicketTester.getLoggingSerializerFilter().addExclusion(Notification.class.getName());
 
     }
 
 }
