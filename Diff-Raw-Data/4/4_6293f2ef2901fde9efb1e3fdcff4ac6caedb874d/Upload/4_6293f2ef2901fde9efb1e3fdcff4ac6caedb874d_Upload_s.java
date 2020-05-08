 package upload;
 
 import static org.testng.AssertJUnit.assertEquals;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.KeyEvent;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.support.PageFactory;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.annotations.Test;
 
 import pageFactory.ContentPage;
 import basic.BasicTestCase;
 
 public class Upload extends BasicTestCase {
 	private ContentPage obj = PageFactory.initElements(getWebDriver(), ContentPage.class);
 	WebDriverWait wait = new WebDriverWait(driver, 30);
 	
 	
 //Upload track                     
@Test(priority = 1, groups={"Upload"}, description="Проверяет загрузку треков на сайт")
 	public void UploadTrack () throws Exception {
 	driver.get(musicUrl);
 	assertPage(musicUrl);
 	obj.Upload.click(); //go to upload link on head
 	setClipboard("C:\\test.mp3");
 	driver.findElement(By.xpath("//*[contains(@id,'SWFUpload_0')]")).click();//click on upload button
 	keySendToWinForm();
 	wait.until(ExpectedConditions.elementToBeClickable(By.xpath(".//*[@id='file_complete_status']/a"))).click(); // go to play list after download
 	driver.navigate().refresh();
 	assertEquals("Неизвестный исполнитель — test" ,driver.findElement(By.xpath("//*[@class='left']//*[@class='advanced_playlist']/li[1]")).getAttribute("audio_name")); //check track
 	obj.DelTrack.click(); //delete track from play list
 	}
 
 //Функция для копирования в обуфер обмена Windows
 public static void setClipboard(String str) {
     StringSelection ss = new StringSelection(str);
     Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
 }
 //Функция для отправки строки из буфера обмена в Win форму (аля на хуй скрипт)
 public void keySendToWinForm () throws AWTException {
 	Robot robot = new Robot();
          robot.delay(3000);
          robot.keyPress(KeyEvent.VK_CONTROL);
          robot.keyPress(KeyEvent.VK_V);
          robot.keyRelease(KeyEvent.VK_V);
          robot.keyRelease(KeyEvent.VK_CONTROL);
          robot.keyPress(KeyEvent.VK_ENTER);
 }
 }
