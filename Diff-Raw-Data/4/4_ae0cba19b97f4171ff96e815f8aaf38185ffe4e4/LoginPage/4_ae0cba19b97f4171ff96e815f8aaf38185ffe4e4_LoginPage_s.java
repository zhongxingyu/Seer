 package smartpool.functional.page;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.How;
 
 import static junit.framework.Assert.assertEquals;
 
 public class LoginPage extends Page {
 
     public static final String USERNAME_ID = "username";
     public static final String TEST_USERNAME = "test.twu";
     public static final String TEST_PASSWORD = "Th0ughtW0rks@12";
     public static final String INCORRECT_USERNAME = "incorrect";
     public static final String INCORRECT_PASSWORD = "password";
 
     @FindBy(how = How.ID, using = USERNAME_ID)
     private WebElement userName;
 
     @FindBy(how = How.ID, using = "password")
     private WebElement password;
 
     public static final String WRONG_LOGIN_MESSAGE_ID = "errors";
     @FindBy(how = How.CLASS_NAME, using = WRONG_LOGIN_MESSAGE_ID)
     private WebElement errorMessage;
 
     public LoginPage(WebDriver webDriver) {
         super(webDriver);
     }
 
     @Override
     public void waitForThePageToLoad() {
         waitForElementToLoad(By.id(USERNAME_ID));
     }
 
     public HomePage login() {
         return login(TEST_USERNAME, TEST_PASSWORD);
     }
 
     public LoginPage invalidLogin() {
         enterLoginCredentials(INCORRECT_USERNAME, INCORRECT_PASSWORD);
         return this;
     }
 
     public HomePage login(String userNameText, String passwordText) {
         enterLoginCredentials(userNameText, passwordText);
         return new HomePage(webDriver);
     }
 
     private void enterLoginCredentials(String userNameText, String passwordText) {
        assertEquals("CAS – Central Authentication Service", webDriver.getTitle());
         userName.sendKeys(userNameText);
         password.sendKeys(passwordText);
         userName.submit();
     }
 
     public void verifyLoginFailureMessage() {
         assertEquals("The credentials you provided cannot be determined to be authentic.", errorMessage.getText());
     }
 }
