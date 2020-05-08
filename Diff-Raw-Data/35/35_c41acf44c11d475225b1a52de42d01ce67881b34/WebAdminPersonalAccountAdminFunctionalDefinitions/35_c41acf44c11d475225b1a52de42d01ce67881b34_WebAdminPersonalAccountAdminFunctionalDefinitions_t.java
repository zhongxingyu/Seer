 package npg.webadmin.acceptance.test.navigation.stepdefs;
 
 
 //import java.util.concurrent.TimeUnit;
 import java.util.List;
 import java.util.Set;
 import java.util.Iterator;
 import static org.junit.Assert.assertTrue;
 import org.openqa.selenium.By;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.Alert;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.NoAlertPresentException;
 import com.google.inject.Inject; 
 import cucumber.api.java.After;
 import cucumber.api.java.Before;
 import cucumber.api.java.en.Given;
 import cucumber.api.java.en.Then;
 import cucumber.api.java.en.When;
 import cucumber.api.java.en.And;
 import npg.webadmin.acceptance.test.util.LinkElementsVerificationService;
 import npg.webadmin.acceptance.test.util.LoginService;
 import npg.webadmin.acceptance.test.util.TextElementsVerificationService;
 import npg.webadmin.acceptance.test.service.NavigationService;
 import npg.webadmin.acceptance.test.service.SearchService;
 import npg.webadmin.acceptance.test.util.WebDriverFactory;
 import npg.webadmin.acceptance.test.WebDriverWrapper;
 import npg.webadmin.acceptance.test.util.WebElementsVerificationBaseAbstract;
 import npg.webadmin.acceptance.test.util.WebElementsVerificationBaseAbstract.SearchItem;
 import npg.webadmin.acceptance.test.util.WebElementsVerificationBaseAbstract.FieldItem;
 import npg.webadmin.acceptance.test.util.WebElementsVerificationBaseAbstract.License;
 
 public class WebAdminPersonalAccountAdminFunctionalDefinitions {
 	private WebDriverWrapper webDriver;	
 	private LoginService loginService;
 	private NavigationService navigationService;
 	private SearchService searchService;
 	private LinkElementsVerificationService linkElementsVerificationService;
 	private TextElementsVerificationService textElementsVerificationService;
 	
 	@Inject
 	public WebAdminPersonalAccountAdminFunctionalDefinitions(
 			LoginService loginService,
 			NavigationService navigationService,
 			LinkElementsVerificationService linkElementsVerificationService,
 			TextElementsVerificationService textElementsVerificationService,
 			SearchService searchService) {
 		
 		this.loginService = loginService;
 		this.navigationService = navigationService;
 		this.searchService = searchService;
 		this.linkElementsVerificationService = linkElementsVerificationService;
 		this.textElementsVerificationService = textElementsVerificationService;
 	}
 	
 	@Before(value="@InitializeWebAdminPAFunc")
 	public void initializeDriver() {
 		webDriver = WebDriverFactory.getWebDriver();
 		if (webDriver.getDriver() instanceof HtmlUnitDriver) {
 			((HtmlUnitDriver)webDriver.getDriver()).setJavascriptEnabled(true);
 		}
 	}
 	 
 	@After(value="@CloseWebAdminPAFunc")
     public void clear() { 
 		if (webDriver != null) {
 			webDriver.manage().deleteAllCookies();
 			webDriver.quit();
 			webDriver = null;
 	    }	
 	} 
    
 	 
 	@Given("^user is logged-in as \"([^\"]*)\" password \"([^\"]*)\" for webadmin personal account admin$")
 	public void logInForWebAdminPersonalAccountAdmin(String username, String password) {
 		loginService.userLogInWebAdmin(webDriver, username, password);
 	}
 	
 	@When("^user is on personal account search page$")
 	public void toPersonalAccountSearch() {
 		navigationService.toWebAdminPersonalAccountSearch(webDriver);
 	}
 	
	@Then("^ensure result item is in the search result page$")
	public void verifyPersonalAccountSearch(List<SearchItem> searchItems) {	
 		for (SearchItem item : searchItems) {
 			
 			
 			searchService.fillAndSubmitInputField(
 					webDriver, 
 					searchService.mapOperationFieldPersonalAccount(item.field), 
 					searchService.mapOperationConditionPersonalAccount(item.operation), 
 					searchService.mapInputFieldPersonalAccount(item.field), 
 					item.fieldvalue, 
 					"//input[@value=' Search ' and @type=\"SUBMIT\" and @id=\"search1\"]");
 			FieldItem fieldItem =  (new WebElementsVerificationBaseAbstract()).new FieldItem();
			fieldItem.value = item.result;
 			textElementsVerificationService.verifyElementText(webDriver, fieldItem);
 			navigationService.navigateToWebAdminPersonalAccountSearch(webDriver);
 		}
 		
 	}
 	
 	@Then("^ensure orcid account \"([^\"]*)\" is in the search result page$")
 	public void verifyPersonalAccountSearchUsingOrcid(String textToVerify, List<SearchItem> searchItems) {
 		for (SearchItem item : searchItems) {
 			searchService.fillAndSubmitInputField(
 					webDriver,
 					searchService.mapOperationFieldPersonalAccount(item.field),
					searchService.mapOperationConditionPersonalAccount(item.operation==null?"":item.operation),					 
 					searchService.mapInputFieldPersonalAccount(item.field),					
 					item.fieldvalue, 
 					"//input[@value=' Search ' and @type=\"SUBMIT\" and @id=\"search2\"]");
 			
 			FieldItem fieldItem =  (new WebElementsVerificationBaseAbstract()).new FieldItem();
 			fieldItem.value = textToVerify;
 			
 			textElementsVerificationService.verifyElementText(webDriver, fieldItem);
 			navigationService.navigateToWebAdminPersonalAccountSearch(webDriver);
 		}
 	}
 	  
 }
