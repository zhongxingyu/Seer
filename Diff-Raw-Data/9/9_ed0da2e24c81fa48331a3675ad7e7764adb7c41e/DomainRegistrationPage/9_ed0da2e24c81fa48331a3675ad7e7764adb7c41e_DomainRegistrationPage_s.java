 package com.marcindziedzic.bdd.webdriver.pages;
 
 import org.jbehave.core.annotations.Given;
 import org.jbehave.core.annotations.Named;
 import org.jbehave.core.annotations.When;
 import org.jbehave.web.selenium.WebDriverProvider;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 public class DomainRegistrationPage extends Page {
 
 	public DomainRegistrationPage(WebDriverProvider driverProvider) {
		super(driverProvider, "Domeny");
 	}
 
 	@Given("domain registration page")
 	public void openPage() {
 		open("domeny/");
 	}
 
     @Given("domain <domain> in search result")
     public void domainInSearchResult(@Named("domain") String domainName) {
         openPage();
         searchForDomain(domainName);
     }
 
 	@When("I search for domain $domainName")
 	public void searchForDomain(String domainName) {
         WebElement searchField = findElement(By.name("domains"));
         searchField.sendKeys(domainName);
         searchField.submit();
 
 		forceWait(); // TODO use active wait if needed
 	}
 
 }
