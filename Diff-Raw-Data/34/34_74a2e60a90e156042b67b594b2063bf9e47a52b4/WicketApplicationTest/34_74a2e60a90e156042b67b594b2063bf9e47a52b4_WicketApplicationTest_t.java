 package de.urlaubr.webapp;
 
import de.urlaubr.webapp.page.start.HomePage;
 import org.apache.wicket.util.tester.WicketTester;
 import org.testng.annotations.Test;
 
 /**
  * @author Patrick Groß-Holtwick
  *         Date: 31.07.13
  */
 public class WicketApplicationTest {
     private WicketTester tester;
 
     /**
      * constructor which creates a Wicket-Tester instance with the Application loaded
      */
     public WicketApplicationTest() {
         tester = new WicketTester(new UrlaubrApplication());
     }
 
     /**
      * simple test which loads every page, checks if there were no errors and validates the rendered page.
      */
     @Test
     public void testApplication() {
         tester.startPage(HomePage.class);
         tester.assertNoErrorMessage();
         tester.assertRenderedPage(HomePage.class);
     }
 }
