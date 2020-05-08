 package ua.com.mcgray.bdd.tools.page;
 
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  * @author orezchykov
  * @since 25.02.13
  */
 public class GitHubRepositoryPage extends Page {
 
 	public GitHubRepositoryPage(WebDriver webDriver) {
 		super(webDriver);
 	}
 
 	private By contentLinksLocator = By.cssSelector("td.content a");
 
	private By fileContentLocator = By.cssSelector("table.lines tr > td:nth-child(2)");
 
 	private void proceedTo(String resourceName) throws InterruptedException {
         List<WebElement> contentLinks = getWebDriver().findElements(contentLinksLocator);
         for (WebElement contentLink : contentLinks) {
             if (contentLink.getText().contains(resourceName)) {
                 System.out.println(contentLink.getText());
                 contentLink.click();
                 waitForAjax();
                 return;
             }
         }
     }
 
     public void proceedToPath(String... path) throws InterruptedException {
 		for (int i = 0; i < path.length; i++) {
 			proceedTo(path[i]);
 		}
 	}
 
 	public boolean searchInFileContent(String text) {
         WebElement fileContent = new WebDriverWait(getWebDriver(), TIME_OUT_IN_SECONDS).until(ExpectedConditions.presenceOfElementLocated(fileContentLocator));
 		return fileContent.getText().contains(text);
 	}
 }
