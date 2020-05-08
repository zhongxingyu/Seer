 package uk.co.stfo.adriver.integration.form;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.isEmptyString;
 
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keys;
 
 import uk.co.stfo.adriver.element.Element;
 import uk.co.stfo.adriver.integration.AbstractDriverIT;
 
 public abstract class AbstractFormActionIT extends AbstractDriverIT {
 
     @Test
     public void canClickOnCheckboxes() {
 
         final Element top = driver.child(By.id("top"));
         top.child(By.id("content")).assertThat().hasText(isEmptyString());
 
         top.child(By.id("cb")).perform().click();
 
         top.child(By.id("content")).assertThat().hasText(is("Checkbox has been clicked"));
     }
 
 
     @Test
     public void canClickOnRadios() {
         final Element top = driver.child(By.id("top"));
         top.child(By.id("content")).assertThat().hasText(isEmptyString());
 
         top.child(By.id("r1")).perform().click();
         top.child(By.id("content")).assertThat().hasText(is("Radio 1 has been clicked"));
         top.child(By.id("r2")).perform().click();
         top.child(By.id("content")).assertThat().hasText(is("Radio 2 has been clicked"));
     }
 
 
     @Test
     public void canEnterTextIntoTextFields() {
         final Element top = driver.child(By.id("top"));
         top.child(By.id("content")).assertThat().hasText(isEmptyString());
 
         top.child(By.id("tb")).perform().type("This is some text");
 
         top.child(By.id("content")).assertThat().hasText(is("This is some text"));
     }
 
 
     @Test
     public void canEnterSpecificKeysIntoTextFields() {
         final Element top = driver.child(By.id("top"));
         top.child(By.id("content")).assertThat().hasText(isEmptyString());
 
         top.child(By.id("tb")).perform().type("This is some text");
         top.child(By.id("tb")).perform().type(Keys.BACK_SPACE);
         top.child(By.id("tb")).perform().type(Keys.BACK_SPACE);
 
         top.child(By.id("content")).assertThat().hasText(is("This is some te"));
     }
 
 
     @Test
     public void canClearTextFields() {
         final Element top = driver.child(By.id("top"));
         top.child(By.id("content")).assertThat().hasText(isEmptyString());
 
         top.child(By.id("tb")).perform().type("This is some text");
         top.child(By.id("tb")).assertThat().hasAttribute("value", is("This is some text"));
 
         top.child(By.id("tb")).perform().clear();
         top.child(By.id("tb")).assertThat().hasAttribute("value", is(""));
     }
 
 
     @Test
     public void canChooseOptionsInASelectBox() {
         final Element top = driver.child(By.id("top"));
         top.child(By.id("content")).assertThat().hasText(isEmptyString());
 
         top.child(By.id("sel")).perform().select("Option 1");
         top.child(By.id("content")).assertThat().hasText(is("Select box changed to option-1"));
 
         top.child(By.id("sel")).perform().select("Option 2");
         top.child(By.id("content")).assertThat().hasText(is("Select box changed to option-2"));
     }
 }
