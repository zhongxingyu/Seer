 package com.forum.web.page.tests;
 
 import com.forum.web.page.Browser;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 public class ActivityWallTest {
     private Browser browser;
 
     @Before
     public void initializeWebDriver() {
         browser = new Browser("http://localhost:8080/forum", true);
         browser.open("/");
     }
 
     @After
     public void closeBrowser() {
         browser.stop();
     }
 
     @Test
     public void shouldLoadMoreQuestions() {
         WebElement loadMore = browser.findElement(By.id("loadMoreQuestions"));
 
         WebElement activityWall = browser.findElement(By.id("leftPane"));
         List<WebElement> questions = activityWall.findElements(By.className("questionTitle"));
         assertThat(questions.size(), is(10));
 
         loadMore.click();
        browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
         questions = activityWall.findElements(By.className("questionTitle"));
         assertThat(questions.size(), is(20));
     }
 
     @Test
     public  void shouldGoThroughAdminLoginLogoutProcess() {
        browser.open("/adminDashboard");
         assertTrue(browser.getCurrentUrl().contains("/login"));
 
         WebElement userNameLogin = browser.findElement(By.name("j_username"));
         userNameLogin.sendKeys("jules");
         WebElement passwordLogin = browser.findElement(By.name("j_password"));
         passwordLogin.sendKeys("great!");
 
         WebElement submitButton = browser.findElement(By.name("submit"));
         submitButton.click();
        browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
 
         assertTrue(browser.getCurrentUrl().contains("/login"));
 
 
         WebElement userNameLogin2 = browser.findElement(By.name("j_username"));
         userNameLogin2.sendKeys("Jules");
         WebElement passwordLogin2 = browser.findElement(By.name("j_password"));
         passwordLogin2.sendKeys("Great!");
 
         WebElement submitButton2 = browser.findElement(By.name("submit"));
         submitButton2.click();
        browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
 
         assertTrue(browser.getCurrentUrl().contains("/adminDashboard"));
         assertTrue(browser.findElement(By.id("leftPane")).getText().contains("Welcome to the Admin Dashboard"));
 
         WebElement logoutLink = browser.findElement(By.id("logout"));
         logoutLink.click();
        browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("container")));
 
         assertThat(browser.getCurrentUrl(), is("http://localhost:8080/forum/"));
 
 
     }
 }
