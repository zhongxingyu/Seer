 package com.dierkers.schedule.action;
 
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.support.ui.Select;
 
 import com.dierkers.schedule.tools.http.URLConn;
 import com.gargoylesoftware.htmlunit.BrowserVersion;
 
 public class ClassChecker extends Action {
 
 	public ClassChecker(String owner, String data) {
 		super(owner, data);
 	}
 
 	@Override
 	public int getID() {
 		return ActionType.CLASS_CHECKER;
 	}
 
 	@Override
 	public void process() {
 
 		WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME_16);
 
 		driver.get("https://webprod.admin.uillinois.edu/ssa/servlet/SelfServiceLogin?appName=edu.uillinois.aits.SelfServiceLogin&dad=BANPROD1");
 
 		JSONObject obj = getJSONObject();
 		String usernameString = null;
 		String passwordString = null;
 		try {
 			usernameString = obj.getString("username");
 			passwordString = obj.getString("password");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (usernameString == null || passwordString == null) {
 			System.err.println("Invalid username and password while processing class checker");
 			return;
 		}
 
 		WebElement usernameField = driver.findElement(By.name("inputEnterpriseId"));
 		usernameField.sendKeys(usernameString);
 
 		WebElement passwordField = driver.findElement(By.name("password"));
 		passwordField.sendKeys(passwordString);
 
 		passwordField.submit();
 
 		clickLinkWithText(driver, "Registration & Records");
 		driver.findElements(By.partialLinkText("Registration")).get(1).click();
 		;
 
 		clickLinkWithText(driver, "Look-up or Select Classes");
 		clickLinkWithText(driver, "I Agree to the Above Statement");
 
 		Select term = new Select(driver.findElement(By.id("term_input_id")));
 		term.selectByVisibleText("Spring 2013 - Urbana-Champaign");
 
 		WebElement submit = driver.findElement(By.cssSelector(".pagebodydiv form input[type=submit]"));
 		submit.click();
 
 		Select subjects = new Select(driver.findElement(By.id("subj_id")));
 		subjects.selectByVisibleText("Computer Science");
 
 		submit = driver.findElement(By.name("SUB_BTN"));
 		submit.click();
 
 		// On the course registration page
 		WebElement table = driver.findElements(By.cssSelector(".pagebodydiv .datadisplaytable")).get(1);
 		List<WebElement> courses = table.findElements(By.className("dddefault"));
 		WebElement course = null;
 		for (WebElement element : courses) {
 			if (element.getText().equals("210")) {
 				course = element;
 				break;
 			}
 		}
 
 		if (course == null) {
 			System.err.println("Course not found");
 			return;
 		}
 
 		course.findElement(By.xpath("..")).findElement(By.cssSelector("input[type=submit]")).click();
 
 		// on the registration page
 
 		WebElement tbody = driver.findElement(By.cssSelector(".pagebodydiv .datadisplaytable tbody"));
 
 		List<WebElement> courseRows = tbody.findElements(By.cssSelector("tr"));
 
 		// Actual course, in this case hardcoded
 		course = courseRows.get(2);
 
 		List<WebElement> courseCols = course.findElements(By.cssSelector("td.dddefault"));
 		int rem = Integer.parseInt(courseCols.get(12).getText());
 
 		String to = null;
 		try {
 			to = obj.getString("to");
 		} catch (JSONException e) {
 			System.err.println("Error getting to field");
 			e.printStackTrace();
 		}
 
 		if (to == null) {
 			return;
 		}
 
 		String msg = "Ethics is now open! - Spots Remaining: " + rem;
//		if (rem > 0) {
		URLConn.getPage("http://api.tropo.com/1.0/sessions?action=create&token=18aea8471ca47847abbef040d5cc1c14d1684ac3e094c0a7cd95249c5ff8d722489de69cfdb2af56068fe25a&to="
				+ to + "&msg=" + msg);
//		}
 
 	}
 
 	public void clickLinkWithText(WebDriver driver, String text) {
 		driver.findElement(By.partialLinkText(text)).click();
 	}
 }
