 package org.motechproject.functional.pages.home;
 
 import org.motechproject.functional.base.WebDriverProvider;
 import org.motechproject.functional.pages.BasePage;
 import org.motechproject.functional.util.PlatformSpecificExecutor;
 import org.openqa.selenium.By;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 
 @Component
 public class HomePage extends BasePage {
     @Value("#{functionalTestProperties['host']}")
     private String host;
 
     @Value("#{functionalTestProperties['port']}")
     private String port;
 
     @Autowired
     public HomePage(WebDriverProvider driverProvider) {
         super(driverProvider.getWebDriver());
     }
 
     public void logout() {
         driver.findElement(By.linkText("Logout")).click();
     }
 
     public void openCreateFaclityPage() {
        selectMenu("Facility", "newfacility");
     }
 
     public void openSearchFaclityPage() {
        selectMenu("Facility", "searchfacility");
     }
 
     public void openCreateStaffPage() {
        selectMenu("Staff", "newstaff");
     }
 
     public void openSearchStaffPage() {
        selectMenu("Staff", "searchstaff");
     }
 
     public void openCreatePatientPage() {
        selectMenu("Patient", "newpatient");
     }
 
     public void openSearchPatientPage() {
        selectMenu("Patient", "searchpatient");
     }
 
     public boolean isLogoutLinkVisible(){
         try{
             return driver.findElement(By.linkText("Logout")).isDisplayed();
         }catch (Exception e){
             return false;
         }
     }
 
    public void selectMenu(final String menuName, final String menuItemId) {
         platformSpecificExecutor.execute(new PlatformSpecificExecutor.Code() {
             @Override
             public void windows() {
                javascriptExecutor.clickOnLink(driver, menuName, menuItemId);
             }
 
             @Override
             public void linux() {
                 javascriptExecutor.selectMenu(driver, menuName, menuItemId);
             }
         });
     }
 }
 
 
