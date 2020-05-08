 package ua.com.mcgray.bdd.tools.page;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  * Created with IntelliJ IDEA. User: orezchykov Date: 10.10.12 Time: 18:37 To
  * change this template use File | Settings | File Templates.
  */
 public class GitHubProfilePage extends Page {
 
 	private WebElement repositories;
 	private By repositoriesLocator = By.cssSelector("ul.repolist.js-repo-list");
 
 	private By repositoriesLineLocator = By.cssSelector("li.public.source h3 a");
 
 	public GitHubProfilePage(WebDriver webDriver) {
 		super(webDriver);
 	}
 
 	@FindBy(xpath = "//div[@class='avatared']/h1/em")
 	private WebElement profileName;
 
	@FindBy(css = "ul.tabnav-tabs span.octicon-repo")
 	private WebElement repoTab;
 
 	public boolean isOnProfilePage(String username) {
 		return profileName.equals(username);
 	}
 
 	public List<String> repoList() {
 		repoTab.click();
 		List<String> result = new ArrayList<String>();
 		repositories = new WebDriverWait(getWebDriver(), TIME_OUT_IN_SECONDS).until(ExpectedConditions.presenceOfElementLocated(repositoriesLocator));
 
 		for (WebElement element : repositories.findElements(repositoriesLineLocator)) {
             result.add(element.getText());
         }
         return result;
 	}
 
     public void proceedToRepo(String repoName) throws InterruptedException {
         for (WebElement element : repositories.findElements(repositoriesLineLocator)) {
             if (element.getText().contains(repoName)) {
                 element.click();
                 waitForAjax();
                 return;
             }
         }
     }
 }
