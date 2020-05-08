 package npg.webadmin.acceptance.test.service;
 
 import java.util.ResourceBundle;
 import java.util.concurrent.TimeUnit;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.Select;
 
 import npg.webadmin.acceptance.test.WebDriverWrapper;
 
 
 public class NavigationService {
      private ResourceBundle resource = null;     
      
 	 public NavigationService(ResourceBundle resource) {
 	    this.resource = resource;	 
 	 }
  
 	 public void toMyAccountProfile(WebDriverWrapper webDriver) {
 		 webDriver.findElement(By.xpath("//span[contains(text(), 'Profile')][parent::a]")).click(); 
 	 }
 	 
 	 public void toMyAccountProfileEditPassword(WebDriverWrapper webDriver) {
 		// go to Profile
 	   	webDriver.findElement(By.xpath("//span[contains(text(), 'Profile')][parent::a]")).click(); 
 	   	// go to Edit email page
 	   	webDriver.findElement(By.xpath("//a[contains(text(), 'Edit')][@title='Edit password']")).click(); 
 	 }
 	 
 	 public void toMyAccountProfileEditContactInformation(WebDriverWrapper webDriver) {
 		// go to Profile
 	   	webDriver.findElement(By.xpath("//span[contains(text(), 'Profile')][parent::a]")).click(); 
 	   	// go to Edit Contact Information
 	   	webDriver.findElement(By.xpath("//a[contains(text(), 'Edit')][@title='Edit contact information']")).click();
 	 }
 	 
 	 public void toMyAccountProfileEditEmail(WebDriverWrapper webDriver) {
 		// go to Profile
 	   	webDriver.findElement(By.xpath("//span[contains(text(), 'Profile')][parent::a]")).click(); 
     	// go to Edit email page
     	webDriver.findElement(By.xpath("//a[contains(text(), 'Edit')][@title='Edit email']")).click();  
 	 }
 	 
 	 public void toMyAccountProfileEditWorkDetails(WebDriverWrapper webDriver) {
 		// go to Profile
 	   	webDriver.findElement(By.xpath("//span[contains(text(), 'Profile')][parent::a]")).click(); 
     	// go to Work Details page 
     	webDriver.findElement(By.xpath("//a[contains(text(), 'Edit')][@title='Edit Education and Work details']")).click(); 
 	 } 	 
 	 
 	 public void toMyAccountAlerts(WebDriverWrapper webDriver) {
 		 webDriver.findElement(By.xpath("//span[contains(text(), 'Alerts')][parent::a]")).click();      
 	 }
 	 
 	 public void toMyAccountAlertsEditAlerts(WebDriverWrapper webDriver) {
 	   	// go to Alerts
 		webDriver.findElement(By.xpath("//span[contains(text(), 'Alerts')][parent::a]")).click();    
 	    // go to Modify Alerts
 	   	webDriver.findElement(By.xpath("//a[contains(text(), 'Modify')][@title='Modify alert settings']")).click();    	
 	 }
 
 	 public void toMyAccountSubscriptionsPurchases(WebDriverWrapper webDriver) {
 	    webDriver.findElement(By.xpath("//span[contains(text(), 'Alerts')][parent::a]")).click();
 		webDriver.findElement(By.xpath("//span[contains(text(), 'Subscriptions and Purchases')][parent::a]")).click();  
 	 }
 	 
 	 
 	 
 	 public void toWebAdminMainPage(WebDriverWrapper webDriver) {
 	    String environment = resource.getString("environment");
 		String targetHost = resource.getString("host." + environment);
 		webDriver.navigate().to("http://" + targetHost + "/webadmin"); 
 	 }
 	 
 	 public void toWebAdminPersonalAccountSearch(WebDriverWrapper webDriver) {		 
 		 webDriver.findElement(By.xpath("//a[contains(text(), 'Main Personal Account Search')]")).click();  	
 	 }	 
 	 
 	 public void toWebAdminMainSiteLicenseSearch(WebDriverWrapper webDriver) {		 
 		 webDriver.findElement(By.xpath("//a[contains(text(), 'Main Site License Search')]")).click();  	
 	 }	 
 	 	 
 	 /*
 	  <SELECT NAME="site_id_op" SIZE=1><option VALUE="iseq" SELECTED>=
     </option><option VALUE="swth">Begins With
     </option><option VALUE="ewth">Ends With
     </option><option VALUE="cont" selected>Contains</option></SELECT> 
 	  */
 	 public void toWebAdminMainSiteLicenseSearchSiteId(WebDriverWrapper webDriver, String siteId) {
 		
 		(new Select(webDriver.findElement(By.name("site_id_op")))).selectByValue("iseq");
 		 
 		WebElement searchElement = webDriver.findElement(By.xpath("//input[@name='site_id']"));		 
 		searchElement.sendKeys(siteId);		 
 		webDriver.findElement(By.id("search1")).click();
 		webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS); 	
 	 }	
 	 	 
 	 public void toWebAdminSlamsSiteGivenSiteId(WebDriverWrapper webDriver, String siteId) {
 		webDriver.findElement(By.xpath("//a[contains(text(), '"+ siteId + "')]")).click();
 	 }
 	 
 	 
 	 
 	 public void webAdminSearchOrcidAccount(WebDriverWrapper webDriver, String orcid) {
 		WebElement inputOrcidElement = webDriver.findElement(By.xpath("//input[@name='orcid_id']"));		 
 		inputOrcidElement.sendKeys(orcid);		 
 		webDriver.findElement(By.id("search2")).click();
 		webDriver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
 	 }
 	 
 	 public void toWebAdminMyAccountGivenEmail(WebDriverWrapper webDriver, String email) {
 		 webDriver.findElement(By.xpath("//a[contains(text(), '" + email + "') and @title='Edit account in My Account page']")).click();
 		 webDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);		 
 	 }
 	 
 	 public void toWebAdminMyAccountEditProfileMaximumLogin(WebDriverWrapper webDriver) {
 		// go to Profile
 	 	webDriver.findElement(By.xpath("//span[contains(text(), 'Profile')][parent::a]")).click(); 
 	   	// go to Work Details page 
 	   	webDriver.findElement(By.xpath("//a[contains(text(), 'Edit') and @title='Edit max logins']")).click(); 	 
 	 }	 
 	 
 	 
 	 
 	 public void toSlamsAccountSummary(WebDriverWrapper webDriver) {		 
 	 	webDriver.findElement(By.linkText("Account Summary")).click();	     
 	 }	 
 	 	 
 	 public void toSlamsAccountSummaryEditUserName(WebDriverWrapper webDriver, String prefix) {
 		toSlamsAccountSummary(webDriver);		  
 		webDriver.findElement(By.xpath(
 	    	"//a[@href ='" + prefix +"/nams/svc/mysiteaccount/edit/userName']")).click();      	
 	 }	 
 	 
 	 public void toSlamsAccountSummaryEditPassword(WebDriverWrapper webDriver, String prefix) {
 		toSlamsAccountSummary(webDriver);		  
     	webDriver.findElement(By.xpath(
     			"//a[@href ='" + prefix + "/nams/svc/mysiteaccount/edit/passwd']")).click();    
 	 }	 
 	 
 	 
 	 public void toSlamsAccountSummaryEditAccountDetails(WebDriverWrapper webDriver, String prefix) {
 		toSlamsAccountSummary(webDriver);		  
     	webDriver.findElement(By.xpath(
     			"//a[@href ='" + prefix + "/nams/svc/mysiteaccount/edit/details']")).click();   
 	 }	  
 	 
 	 public void toSlamsAccountSummaryEditOpenUrl(WebDriverWrapper webDriver, String prefix) {
 		toSlamsAccountSummary(webDriver);
     	webDriver.findElement(By.xpath(
     			"//a[@href ='" + prefix + "/nams/svc/mysiteaccount/edit/opurl']")).click();        	
 	 }	
 
 	 public void toSlamsAccountSummaryEditEmail(WebDriverWrapper webDriver, String prefix) {
 		toSlamsAccountSummary(webDriver);	   	
 	   	webDriver.findElement(By.xpath(
     			"//a[@href ='" + prefix + "/nams/svc/mysiteaccount/edit/email']")).click();      
 	 }		 
 
 	 public void toSlamsAccountSummaryEditUserAddress(WebDriverWrapper webDriver, String prefix) {
 		toSlamsAccountSummary(webDriver);
     	webDriver.findElement(By.xpath(
     			"//a[@href ='"+ prefix + "/nams/svc/mysiteaccount/edit/address']")).click();
 	 }
 	 
 
 	 public void toSlamsAccountAddress(WebDriverWrapper webDriver) {
 		 webDriver.findElement(By.linkText("Address Book")).click();
 	 }
 	 
 	 public void toSlamsAccountAddressEditAddress(WebDriverWrapper webDriver) {
 		 toSlamsAccountAddress(webDriver);
 		 webDriver.findElement(By.linkText("Modify")).click();
 	 }	 
  	 
 	 
 	 public void toSlamsAccountLicenses(WebDriverWrapper webDriver) {
 		 webDriver.findElement(By.linkText("Licenses")).click(); 	
 	 }
 	 
 	 public void toSlamsAccountLicensesJournals(WebDriverWrapper webDriver, String prefix, String productCode) { 
 		 toSlamsAccountLicenses(webDriver);	    	
 		 webDriver.findElement(By.xpath(
 	    	  "//a[@href ='" + prefix + "/nams/svc/mysiteaccount/show/product_license?product=" + productCode + "']")).click();      		
 	 }	 
 	 
 	 
 	 public void toSlamsAccountArticleOnDemand(WebDriverWrapper webDriver) {
 		 webDriver.findElement(By.linkText("AOD")).click();  		 
 	 }
 	 
 	 public void toSlamsAccountArticleOnDemandLicenses(WebDriverWrapper webDriver, String licenseName) {
 		 toSlamsAccountArticleOnDemand(webDriver);
 		 webDriver.findElement(By.linkText(licenseName)).click();
 	 }
 	 
 	 
 	 public void toSlamsAccountIPRanges(WebDriverWrapper webDriver) {
 		 webDriver.findElement(By.linkText("IP Ranges")).click(); 	  		 
 	 }
 	 
 	 public void toSlamsAccountIPRangesEditIPRange(WebDriverWrapper webDriver) {
 		 toSlamsAccountIPRanges(webDriver);
 		 webDriver.findElement(By.linkText("Edit")).click();
 	 }
 	 
 	 
 	 public void toSlamsAccountTokens(WebDriverWrapper webDriver) {
 		 webDriver.findElement(By.linkText("Tokens")).click();  	  		 
 	 }
 	 
 	 public void toSlamsAccountTokensEditNatureToken(WebDriverWrapper webDriver) {
 		 //toNatureMySiteAccount(webDriver);
 		 toSlamsAccountTokens(webDriver);
 		 webDriver.findElement(By.xpath(
 	    	"//input[@src ='/store/images/button_modify.gif'][ancestor::form//input[@value='NPG']]")).click();
 	 }
 	 
 	 public void toSlamsAccountTokensEditPalgraveConnectToken(WebDriverWrapper webDriver) {
 		 //toPalgraveConnectMySiteAccount(webDriver);
 		 toSlamsAccountTokens(webDriver);
 		 webDriver.findElement(By.xpath(
	    			"//input[@src ='/store/images/button_modify.gif']")).submit(); //[ancestor::form//input[@value='Palgrave Connect']]")).click();
 	 }	 
 	 
 	 
 	 // ????? 'Palgrave Connect' is wrong. Shouldn't it be 'Palgrave Journal"?
 	 public void toSlamsAccountTokensEditPalgraveJournalToken(WebDriverWrapper webDriver) {
 		 //toPalgraveJournalsMySiteAccount(webDriver);
 		 toSlamsAccountTokens(webDriver);
 	     webDriver.findElement(By.xpath(
	    	 "//input[@src ='/store/images/button_modify.gif']")).submit(); //[ancestor::form//input[@value='Palgrave Connect']]")).click();
 	 }		 
 	  
 	 
 	 public void toSlamsAccountStatistics(WebDriverWrapper webDriver) {
 		webDriver.findElement(By.linkText("Statistics")).click();	  		 
 	 }	 	 
 	 
 	 
 	 public void toSlamsAccountEAlerts(WebDriverWrapper webDriver) {
 	    webDriver.findElement(By.linkText("E-alerts")).click();  		 
 	 }	 
 
 	 public void toSlamsAccountEAlertsSignup(WebDriverWrapper webDriver) {
 		toSlamsAccountEAlerts(webDriver);
 	    webDriver.findElement(By.linkText("Modify")).click(); 
 	 }	 
 	 
 	 public void toSlamsAccountCap(WebDriverWrapper webDriver) {
 		 webDriver.findElement(By.linkText("CAP")).click();	 
 	 }
 	 
 	 public void toSlamsAccountCapEditCap(WebDriverWrapper webDriver, String prefix) {
 		toSlamsAccountCap(webDriver);
     	webDriver.findElement(By.xpath(
     	    	  "//a[contains(text(),'Modify')][@href ='" + prefix + "/nams/svc/mysiteaccount/editcapuserdetail']")).click();
 	 }
 	 
 	 public void toSlamsAccountCapEditPassword(WebDriverWrapper webDriver, String prefix) {
 		toSlamsAccountCap(webDriver);
 		webDriver.findElement(By.xpath(
   	    	  "//a[contains(text(),'Modify')][@href ='" + prefix + "/nams/svc/mysiteaccount/editcapuserpassword']")).click();
 	 }
 	 
 	 
 	 
 	 
 	 public void toNatureMySiteAccount(WebDriverWrapper webDriver) {
 	    String environment = resource.getString("environment");
 	 	String targetHost = resource.getString("host." + environment); 	    
 	 	webDriver.navigate().to("http://" + targetHost + "/nams/svc/mysiteaccount"); 
 	 }
 
 	 public void toPalgraveJournalsMySiteAccount(WebDriverWrapper webDriver) { 
     	String environment = resource.getString("environment");
  	    String targetHost = resource.getString("host.palgravejournals." + environment); 	    
  	    webDriver.navigate().to("http://" + targetHost + "/nams/svc/mysiteaccount");		
 	 }
 
 	 public void toPalgraveConnectMySiteAccount(WebDriverWrapper webDriver) {  
     	String environment = resource.getString("environment");
  	    String targetHost = resource.getString("host.palgraveconnect." + environment); 	    
  	    webDriver.navigate().to("http://" + targetHost + "/nams/svc/mysiteaccount?siteName=pc");
 		
 	 }
 	 
 }
