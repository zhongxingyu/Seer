 package functional.com.thoughtworks.twu.utils;
 
 import org.junit.internal.matchers.StringContains;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import static com.thoughtworks.twu.utils.WaitHelper.waitForElement;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 
 public class Talk {
     private WebDriver webDriver;
     private static final String SUCCESS_MESSAGE = "New Talk Successfully Created";
     private static final String ERROR_CSS_VALUE = "rgb(255, 0, 0) 0px 0px 12px 0px";
     public Talk(WebDriver webDriver) {
         this.webDriver = webDriver;
     }
 
 
     public void newTalk(String title, String description, String venue, String date, String time){
 
         waitForElement(webDriver,"my_talks_button").click();
         waitForElement(webDriver,"new_talk").click();
 
         waitForElement(webDriver, "title").sendKeys(title);
         waitForElement(webDriver, "description").sendKeys(description);
         waitForElement(webDriver, "venue").sendKeys(venue);
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('"+date+"')");
         javascriptExecutor.executeScript("$('#timepicker').val('"+time+"')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
     }
 
     public void assertCreationSuccess() {
         WebElement text = waitForElement(webDriver, "message_box_success");
         assertThat(text.getText(), is(SUCCESS_MESSAGE));
     }
 
     public void loadTalkDetails(String testTitle) {
         waitForElement(webDriver,"my_talks_button").click();
 
         waitForElement(webDriver, "my_talks_list");
 
         webDriver.findElement(By.linkText(testTitle)).click();
 
         waitForElement(webDriver, "talk_details");
     }
 
     public String getHeader() {
         return webDriver.findElement(By.xpath("//div[@id='talk_details']//span[@class='ui-btn-text']")).getText();
     }
 
     public void clickTitle(String title) {
         webDriver.findElement(By.linkText(title)).click();
     }
 
     public void expandDetails() {
         webDriver.findElement(By.xpath("//div[@id='talk_details']//span[@class='ui-btn-text']")).click();
     }
 
     public String getDescription() {
         return webDriver.findElement(By.xpath("//*[@id='description']")).getText();
     }
 
     public String getVenue() {
         return webDriver.findElement(By.xpath("//*[@id='venue']")).getText();
 
     }
 
     public String getDate() {
         return webDriver.findElement(By.xpath("//*[@id='date']")).getText();
     }
 
     public String getTime() {
         return webDriver.findElement(By.xpath("//*[@id='time']")).getText();
     }
 
     public String getContact() {
         return webDriver.findElement(By.xpath("//*[@id='email']")).getText();
     }
 
     public String getLastModifiedTime() {
         return webDriver.findElement(By.xpath("//*[@id='last_modified_time']")).getText();
     }
 
     public void assertDetailsMatch(String description, String venue, String date, String time, String email) {
         waitForElement(webDriver,"talk_details");
         assertThat(getDescription(), StringContains.containsString(description));
         assertThat(getVenue(), StringContains.containsString(venue));
         assertThat(getDate(), StringContains.containsString(date));
         assertThat(getTime(), StringContains.containsString(time));
         assertThat(getContact(), StringContains.containsString(email));
         assertFalse(getLastModifiedTime().isEmpty());
     }
 
     public void assertHeaderMatch(String testTitle, String owner) {
         waitForElement(webDriver, "talk_details");
         assertThat(getHeader(), StringContains.containsString(testTitle));
         assertThat(getHeader(), StringContains.containsString(owner));
     }
 
     public void assertCreationFailForElement(String elementId) {
         assertThat(webDriver.findElement(By.id(elementId)).getCssValue("box-shadow"), is(ERROR_CSS_VALUE));
     }
 
     public void enterFeedbackToTalk(String feedbackComment) {
         WebElement feedbackTextBox = waitForElement(webDriver, "feedback_text");
         feedbackTextBox.sendKeys("");
         WebElement feedbackSubmitButton = waitForElement(webDriver, "add_feedback_submit");
         feedbackSubmitButton.click();
     }
 }
