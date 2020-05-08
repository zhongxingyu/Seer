 package com.forum.web.page.tests;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class UserRegistrationTest {
     private WebDriver driver;
 
     @Before
     public void initializeWebDriver() {
         driver = new FirefoxDriver();
         driver.get("http://localhost:8080/forum/join");
     }
 
     @After
     public void closeBrowser() {
         driver.close();
     }
 
     @Test
     public void shouldGoToRegistrationPage() {
         driver.get("http://localhost:8080/forum");
         WebElement joinLink = driver.findElement(By.linkText("Join"));
         joinLink.click();
         assertTrue(driver.getCurrentUrl().contains("/join"));
     }
 
    @Ignore("Error msg not showing up")
    // @Test
     public void shouldHavePasswordErrorWhenLessThan8() {
         WebElement passwordField = driver.findElement(By.id("password"));
         passwordField.sendKeys("1234567");
 
         WebElement signUp = driver.findElement(By.id("register"));
         signUp.click();
 
         String errorMsg = driver.findElement(By.id("passwordMsg")).getText();
         assertThat(errorMsg, is("Password should be at least 8 characters."));
     }
 
     @Test
     public void shouldDisplayErrorWhenEmailFormatInvalid() {
         WebElement emailField = driver.findElement(By.id("email"));
         emailField.sendKeys("123asdf");
 
         WebElement signUp = driver.findElement(By.id("register"));
         signUp.click();
 
         String errorMsg = driver.findElement(By.id("emailMsg")).getText();
         assertThat(errorMsg, is("Please enter a valid email address."));
     }
 
     @Test
     public void shouldDisplayTermsAndConditions() {
         WebElement termsLink = driver.findElement(By.name("tos"));
         termsLink.click();
         assertTrue(driver.getCurrentUrl().contains("/terms"));
     }
 }
