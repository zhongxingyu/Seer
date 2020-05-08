 package net.canadensys.dataportal.vascan;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 
 /**
  * Integration tests of the rendered vernacular page
  * @author canadensys
  *
  */
 public class VernacularPageIntegrationTest extends AbstractIntegrationTest{
 		
 	@FindBy(css = "div#content")
 	private WebElement contentDiv;
 	
 	//make sure we find the footer since this is the last processed element
 	@FindBy(css = "div#footer")
 	private WebElement footerDiv;
 		
 	@Before
 	public void setup() {
 		browser = new FirefoxDriver();
 	}
 	
 	@Test
 	public void testAcceptedVenacularPage() {
 		//Coordinates is the landing page
 		browser.get(TESTING_SERVER_URL + "vernacular/26256");
 		
 		//bind the WebElement to the current page
 		PageFactory.initElements(browser, this);
 		assertEquals("if du Canada",contentDiv.findElement(By.cssSelector("h1")).getText());
 		assertEquals("sprite sprite-accepted",contentDiv.findElement(By.cssSelector("h1 + p")).getAttribute("class"));
 		
		assertEquals("Taxus canadensis Marshall", contentDiv.findElement(By.cssSelector("p.redirect_accepted a")).getText());
 		assertEquals("div",footerDiv.getTagName());
 	}
 	
 	@Test
 	public void testSynonymVernacularPage(){
 		//get a synonym
 		browser.get(TESTING_SERVER_URL + "vernacular/26258");
 				
 		PageFactory.initElements(browser, this);
 		
 		assertEquals("buis", contentDiv.findElement(By.cssSelector("h1")).getText());
 		assertEquals("sprite sprite-synonym",contentDiv.findElement(By.cssSelector("h1 + p")).getAttribute("class"));
 		
		assertEquals("Taxus canadensis Marshall", contentDiv.findElement(By.cssSelector("p.redirect_accepted a")).getText());
 		
 		assertEquals("div",footerDiv.getTagName());		
 	}
 	 
 	@After
 	public void tearDown() {
 		browser.close();
 	}
 
 }
