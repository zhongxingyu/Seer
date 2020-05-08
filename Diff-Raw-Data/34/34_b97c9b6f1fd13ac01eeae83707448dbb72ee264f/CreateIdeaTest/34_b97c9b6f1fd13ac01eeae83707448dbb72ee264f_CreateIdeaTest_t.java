 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 @RunWith(Parameterized.class)
 public class CreateIdeaTest {
 
     public static final By IDEA_TEXT_SELECTOR = By.id("ideaText");
     public static final By SUBMIT_BUTTON_SELECTOR = By.id("submitBtn");
 
     private FirefoxPreference firefoxPreference;
     private TestHelper testHelper;
 
 
     @Parameterized.Parameters
     public static List<Object[]> firefoxPreferences() {
         return Arrays.asList(
                new Object[][] {
                        { new FirefoxPreference("general.useragent.override", "Mozilla/5.0 (Android; Linux armv7l; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 Fennec/2.0.1") },
                        { new FirefoxPreference("general.useragent.override", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; da-dk) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5") }
                 }
         );
     }
 
     public CreateIdeaTest(FirefoxPreference firefoxPreference) {
         this.firefoxPreference = firefoxPreference;
     }
 
     @Before
     public void setUp() {
         testHelper = new TestHelper(this.firefoxPreference);
     }
 
     @Test
     public void shouldDisplayErrorOnEmptyIdea() {
         testHelper.navigateToCreateIdeaView();
 
         submitIdea();
 
         testHelper.assertDisplayedMessageIs("Please enter some text.");
     }
 
     @Test
    @Ignore("ignored because we cannot control making the IdeaBoardz app fail")
    public void shouldShowErrorMessageAfterFailedSubmission() throws Exception {
        testHelper.navigateToCreateIdeaView();

        addIdeaText("Functional test idea!");
        submitIdea();

        testHelper.assertDisplayedMessageIs("Failed to submit. Please try again in some time.");
    }
    
    @Test
     public void shouldShowCreatedMessageAfterSubmissionOfIdea() {
         testHelper.navigateToCreateIdeaView();
 
         addIdeaText("Functional test idea!");
         submitIdea();
 
         testHelper.assertDisplayedMessageIs("Your idea has been posted.");
     }
 
     @Test
     public void shouldRemainOnCreateIdeaPageAfterSubmitting() {
         testHelper.navigateToCreateIdeaView();
         submitIdea();
 
         assertEquals(testHelper.BOARD_URL + "/createIdea", testHelper.getCurrentUrl());
 
         WebElement ideaTextBox = testHelper.findElement(IDEA_TEXT_SELECTOR);
         assertEquals("", ideaTextBox.getText());
     }
 
     @Test
    public void shouldSeeContributeIdeaPageWithCorrectBoardName() {
         testHelper.navigateToCreateIdeaView();
 
         WebElement boardName = testHelper.findElementByTagName("h1");
         assertEquals("test", boardName.getText());
     }
 
     @After
     public void tearDown() {
         testHelper.closeWebDriver();
     }
 
     private void addIdeaText(String ideaText) {
         WebElement message = testHelper.findElement(IDEA_TEXT_SELECTOR);
         message.sendKeys(ideaText);
     }
 
     public void submitIdea() {
         WebElement button = testHelper.findElement(SUBMIT_BUTTON_SELECTOR);
         button.click();
     }
 }
