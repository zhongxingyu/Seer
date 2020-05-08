 package ru.aemelin.awt.robot.remote;
 
import org.junit.*;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import java.awt.event.KeyEvent;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dolf
  * Date: 22.02.13
  * Time: 19:08
  */
 public class RobotTest {
     private static final WebDriver driver = new FirefoxDriver();
     private static final Robot robot = new Robot("http://localhost:6080");
 
     @Before
     public void openPage(){
         driver.get("http://ya.ru");
         robot.mouseClick(200, 200); // фокус на окно браузера
     }
 
     @Test
     public void getBrowserOffsetTest() throws Exception{
         int[] offset = robot.getBrowserOffset(driver);
         assertTrue("Координата x смещения окна браузера должна быть положительна", offset[0] > 0);
         assertTrue("Координата y смещения окна браузера должна быть положительна", offset[1] > 0);
     }
 
     @Test
     public void getRGBPixelColorTest(){
         assertThat("Неверный цвет пикселя [200, 200]",
                 Integer.toHexString(robot.getRGBPixelColor(200, 200).getRGB()), is("ffffffff"));
     }
 
     @Test
     public void mouseClickTest() throws Exception{
         WebElement logo = driver.findElement(By.xpath("//table[contains(@class, 'b-search')]//a/img"));
         robot.getBrowserOffset(driver);
         robot.mouseClick(logo);
         Thread.sleep(2000);
         assertThat("Не сработал клик по логотипу", driver.getCurrentUrl(), is("http://www.yandex.ru/"));
     }
 
     @Test
     public void sendKeysTest() throws Exception{
         String txt = "Test";
         WebElement input = driver.findElement(By.xpath("//table[contains(@class, 'b-search')]//input[@id = 'text']"));
         robot.getBrowserOffset(driver);
         robot.mouseClick(input);
         robot.sendKeys(txt);
         robot.keyPress(KeyEvent.VK_ENTER);
         Thread.sleep(2000);
         assertThat("В заголовке открывшегося окна нет текста '" + txt + "'", driver.getTitle(), containsString(txt));
     }
 
     @Test
     public void mouseMoveTest() throws Exception{
         WebElement link = driver.findElement(By.xpath("//li[@id = 'mail']/a"));
         String color = "rgba(255, 0, 0, 1)";
         robot.getBrowserOffset(driver);
         robot.mouseMove(link);
         assertThat("Ссылка на почту при наведении должна покраснеть", link.getCssValue("color"), is(color));
     }
 
     @Test
     public void copyPasteTest() throws Exception{
         WebElement link = driver.findElement(By.id("sethome"));
         WebElement input = driver.findElement(By.xpath("//table[contains(@class, 'b-search')]//input[@id = 'text']"));
         robot.getBrowserOffset(driver);
         robot.select(link);
         robot.copy();
         robot.mouseClick(input);
         robot.paste();
         assertThat("Текст ссылки не скопировался в строку поиска", input.getAttribute("value"),
                 containsString(link.getText()));
     }
 
     @AfterClass
     public static void stopServer(){
         driver.close();
     }
 }
