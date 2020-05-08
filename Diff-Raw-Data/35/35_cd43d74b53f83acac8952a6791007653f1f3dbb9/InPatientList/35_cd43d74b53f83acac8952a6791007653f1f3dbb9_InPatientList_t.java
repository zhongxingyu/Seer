 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openmrs.module.mirebalais.smoke.dataModel.Visit;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 public class InPatientList extends AbstractPageObject {
 
 	public InPatientList(WebDriver driver) {
 		super(driver);
 	}
 
 	public int getPatientCount() {
        String text = driver.findElement(By.cssSelector("h3.inpatient-count")).getText();
        return Integer.parseInt(text.replaceAll("[^\\d]", ""));
 	}

 	public List<Visit> getVisits() {
 		List<Visit> visits = new ArrayList<Visit>();
 		List<WebElement> tds = driver.findElements(By.cssSelector("#active-visits td"));
 		for (int i = 0; i < tds.size(); i+=4) {
			visits.add(new Visit(tds.get(i).getText(),
								 tds.get(i+1).getText(),
								 cleanDispositionPlace(tds.get(i+2).getText()),
 								 cleanDispositionPlace(tds.get(i+3).getText())));
 		}
 		return visits;
 	}

 
 	public String getCurrentWard(String patientIdentifier) throws Exception {
 		return getVisit(patientIdentifier).getCurrentWard();
 	}
 
 	public String getFirstAdmitted(String patientIdentifier) throws Exception {
 		return getVisit(patientIdentifier).getFirstAdmitted();
 	}

 	public boolean isListFilteredBy(String ward) {
 		for (Visit visit : getVisits()) {
 			if (!visit.getCurrentWard().contains(ward)) {
 				return false;
 			}
 		}
 		return true;
 	}

 	public void filterBy(String ward) throws Exception {
 		clickOnOptionLookingForText(ward, By.cssSelector("#inpatients-filterByLocation-field option"));
 	}

 	private Visit getVisit(String patientIdentifier) throws Exception {
 		List<Visit> visits = this.getVisits();
 		for (Visit visit : visits) {
 			if (visit.getPatientId().contains(patientIdentifier)) {
 				return visit;
 			}
 		}
 		throw new Exception(String.format("Visit not found for patient %s", patientIdentifier));
 	}

 	private String cleanDispositionPlace(String rawText) {
 		return rawText.substring(0,rawText.indexOf('\n')).trim();
 	}
 	
 }
