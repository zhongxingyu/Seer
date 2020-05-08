 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 import java.util.concurrent.TimeUnit;
 import org.junit.*;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import org.openqa.selenium.*;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.Select;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class AddTaskTest {
   private WebDriver driver;
   private String baseUrl;
   private boolean acceptNextAlert = true;
   private StringBuffer verificationErrors = new StringBuffer();
 
   @Before
   public void setUp() throws Exception {
     driver = new FirefoxDriver();
     baseUrl = "http://ec2-54-237-98-146.compute-1.amazonaws.com";
     driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
   }
 
   @Test
   public void testAddTask() throws Exception {
 	driver.get(baseUrl + "/automaatnehindaja/");
     WebElement username = driver.findElement(By.name("j_username"));
     username.clear();
     username.sendKeys("henri");
     
    WebElement pass = driver.findElement(By.name("password"));
     pass.clear();
     pass.sendKeys("passwd");
     pass.submit();
     
     driver.findElement(By.cssSelector("a[href='#addTask']")).click();
     
     (new WebDriverWait(driver, 10))
     .until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.has-sub.active > ul > li.odd > a > span")))
     .click();
     
     (new WebDriverWait(driver, 10))
 	.until(ExpectedConditions.presenceOfElementLocated(By.className("Zebra_DatePicker_Icon_Inside")))
 	.click();
     
     WebElement calendar = (new WebDriverWait(driver, 10))
     .until(ExpectedConditions.visibilityOfElementLocated(By.className("Zebra_DatePicker")));
     
     ArrayList<WebElement> dates = (ArrayList<WebElement>) calendar.findElements(By.tagName("td"));
     
     for (int i = 0; i < dates.size(); i++) {
     	if (!dates.get(i).getAttribute("class").startsWith("dp")) {
     		dates.get(i).click();
     		break;
     	}
     }
     
     driver.findElement(By.id("name")).sendKeys("test");
     driver.findElement(By.id("desc")).sendKeys("test");
     driver.findElement(By.id("input0")).sendKeys("test");
     driver.findElement(By.id("output0")).sendKeys("test");
     driver.findElement(By.xpath("//button[@onclick='addio()']")).click();
     driver.findElement(By.id("input1")).sendKeys("test2");
     driver.findElement(By.id("output1")).sendKeys("test2");
     Thread.sleep(1000);
     driver.findElement(By.xpath("//button[@onclick='checkFields()']")).click();
     
     (new WebDriverWait(driver, 10)).
 	until(ExpectedConditions.textToBePresentInElement(By.id("message"), "Ülesande lisamine õnnestus!"));
     
     driver.findElement(By.cssSelector("a[href='#tasksview']")).click();
     
     (new WebDriverWait(driver, 10)).
 	until(ExpectedConditions.presenceOfElementLocated(By.linkText("test")));
   }
 
   @After
   public void tearDown() throws Exception {
     driver.quit();
     
     Connection c = null;
 	PreparedStatement stmt = null;
 	String statement;
 	
 	try {
 		Class.forName("com.mysql.jdbc.Driver");
 		c = DriverManager.getConnection(
 			"jdbc:mysql://localhost:3306/automaatnehindaja",
 			"ahindaja", "k1rven2gu");
 		
 		statement = "DELETE FROM tasks WHERE name='test';";
 		stmt = c.prepareStatement(statement);
 		stmt.executeUpdate();
 		c.close();
 	
 	} catch (SQLException | ClassNotFoundException e) {
 		System.out.println("ERROR: " + e);
 		return;
 	}
     
     String verificationErrorString = verificationErrors.toString();
     if (!"".equals(verificationErrorString)) {
       fail(verificationErrorString);
     }
   }
 }
