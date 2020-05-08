 package com.marcindziedzic.bdd.webdriver.pages;
 
 import org.jbehave.core.annotations.Given;
 import org.jbehave.core.annotations.When;
 import org.jbehave.web.selenium.WebDriverProvider;
 import org.openqa.selenium.By;
 
 public class HomePage extends Page {
 	
 	public HomePage(WebDriverProvider driverProvider) {
		super(driverProvider, "Hosting, Rejestracja domen");
 	}
 	
 	@Given("opened home page")
 	public void open() {
 		open("");
 	}
 
 	@When("I insert $domainName in search field")
 	public void insertTextInSearchField(String domainName) {
         findElement(By.name("domains")).sendKeys(domainName);
 	}
 	
 	@When("press search button")
 	public void pressSearchButton() {
         findElement(By.tagName("button")).click();
 		forceWait();           // TODO replace force wait with active waiting
 	}
 
 }
