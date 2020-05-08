 package smartpool.functional.page;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.How;
 
 import static junit.framework.Assert.assertEquals;
 
 public class ViewCarpoolPage extends Page {
 
     public static final String CARPOOL_CLASS_NAME = "carpoolName";
     public static final String BUDDY_PROFILE_ID = "arnavku";
 
    @FindBy(how = How.ID, using = CARPOOL_CLASS_NAME)
     private WebElement carpoolName;
 
     @FindBy(how = How.ID, using = "ayusht")
     private WebElement ayushtLink;
 
     public ViewCarpoolPage(WebDriver webDriver) {
         super(webDriver);
     }
 
     @Override
     public void waitForThePageToLoad() {
        waitForElementToLoad(By.id(CARPOOL_CLASS_NAME));
     }
 
     public void verifyCarpoolDetails() {
         assertEquals("Carpool carpool-1", carpoolName.getText());
     }
 
     public MyProfilePage goToBuddyProfilePage() {
         ayushtLink.click();
         return new MyProfilePage(webDriver);
     }
 }
