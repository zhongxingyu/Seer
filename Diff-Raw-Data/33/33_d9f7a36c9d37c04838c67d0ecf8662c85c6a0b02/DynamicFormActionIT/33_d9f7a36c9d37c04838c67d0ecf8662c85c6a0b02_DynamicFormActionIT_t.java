 package uk.co.stfo.adriver.integration.form;
 
import org.junit.Test;
import org.openqa.selenium.By;
import uk.co.stfo.adriver.element.Element;

import static org.hamcrest.Matchers.containsString;

 public class DynamicFormActionIT extends AbstractFormActionIT {
 
    // This test only occurs in the Dynamic test as intermittent delays were noticed in the static test - most likely because the browser
    // didn't refresh in time
    @Test
    public void canSubmitForms() {
        final Element top = driver.child(By.id("top"));
        top.child(By.tagName("form")).child(By.name("some-text")).perform().type("something");
        top.child(By.tagName("form")).child(By.name("some-text")).perform().submit();

        driver.assertThat().currentUrl(containsString("some-text=something"));
    }

     @Override
     protected String filename() {
         return "form.html";
     }
 
 
     @Override
     protected String folder() {
         return "dynamic";
     }
 
 
     @Override
     protected long timeout() {
         return 1200;
     }
 
 
     @Override
     protected long pollFreq() {
         return 100;
     }
 }
