 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.Keys;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 public class ArchivesRoomApp extends AbstractPageObject {
 
 	public ArchivesRoomApp(WebDriver driver) {
 		super(driver);
 	}

 	public boolean isPatientInList(String patientIdentifier, String list) {
 		try {
 			findPatientInTheList(patientIdentifier, list);
 		} catch (Exception e) {
 			return false;
 		}
 		return true;
 	}
 	
 	public WebElement findPatientInTheList(String patientIdentifier, String list) throws Exception {
 		List<WebElement> elements = driver.findElement(By.id(list)).findElements(By.tagName("span"));
 		for(WebElement element : elements) {
 			if (element.getText().contains(patientIdentifier)) {
 				return element;
 			}
 		}
 		
		throw new Exception("Patient not found");
 	}
 	
 	public String getDossieNumber(String patientName) throws Exception {
 		List<WebElement> elements = driver.findElements(By.cssSelector("#assigned_create_requests_table td"));
 		for(int i = elements.size()-1; i>=0; i--) {
 			if (elements.get(i).getText().contains(patientName)) {
 				return elements.get(i+1).getText();
 			}
 		}
		throw new Exception("Patient not found");
 	}
 
 	public void sendDossie(String dossieNumber) {
 		driver.findElement(By.name("mark-as-pulled-identifier")).sendKeys(dossieNumber);
 		driver.findElement(By.name("mark-as-pulled-identifier")).sendKeys(Keys.RETURN);
 	}
 
 	public void returnRecord(String dossieNumber) {
 		driver.findElement(By.id("tab-selector-return")).click();
 		driver.findElement(By.name("mark-as-returned-identifier")).sendKeys(dossieNumber);
 		driver.findElement(By.name("mark-as-returned-identifier")).sendKeys(Keys.RETURN);
 	}
 
 	public void createRecord(String patientIdentifier) throws Exception {
 		findPatientInTheList(patientIdentifier, "create_requests_table").click();
 		driver.findElement(By.id("assign-to-create-button")).click();
 	}
 	
 	/* It will probably not clear all of them, because the script is faster than the response.
 	   At least, it will keep the list smaller. */
 	public void clearRequestList() {
 		List<String> dossierNumbers = filterDossierNumbers(driver.findElements(By.cssSelector("#assigned_create_requests_table span")));
 		for (String dossieNumber : dossierNumbers) {
 			sendDossie(dossieNumber);
 		}
 	}
 
 	private List<String> filterDossierNumbers(List<WebElement> elements) {
 		List<String> dossierNumbers = new ArrayList<String>();
 		for (WebElement e : elements) {
 			String text = e.getText();
 			if (text.length() == 7 && text.startsWith("A")) {
 				dossierNumbers.add(text);
 			}
 		}
 		return dossierNumbers;
 	}
 
 }
