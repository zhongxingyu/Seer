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
        selectMenu("Facility", "New", "newfacility");
     }
 
     public void openSearchFaclityPage() {
        selectMenu("Facility", "Search", "searchfacility");
     }
 
     public void openCreateStaffPage() {
        selectMenu("Staff", "New", "newstaff");
     }
 
     public void openSearchStaffPage() {
        selectMenu("Staff", "Search", "searchstaff");
     }
 
     public void openCreatePatientPage() {
        selectMenu("Patient", "New", "newpatient");
     }
 
     public void openSearchPatientPage() {
        selectMenu("Patient", "Search", "searchpatient");
     }
 
     public boolean isLogoutLinkVisible(){
         try{
             return driver.findElement(By.linkText("Logout")).isDisplayed();
         }catch (Exception e){
             return false;
         }
     }
 
    public void selectMenu(final String menuName, final String menuItemName, final String menuItemId) {
         platformSpecificExecutor.execute(new PlatformSpecificExecutor.Code() {
             @Override
             public void windows() {
                javascriptExecutor.clickOnLink(menuItemName, driver);
             }
 
             @Override
             public void linux() {
                 javascriptExecutor.selectMenu(driver, menuName, menuItemId);
             }
         });
     }
 }
 
 
