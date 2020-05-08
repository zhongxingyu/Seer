 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openmrs.module.mirebalais.smoke.dataModel.User;
 import org.openmrs.module.mirebalais.smoke.helper.UserDatabaseHandler;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class LoginPage {
 	
 	private WebDriver driver;
 	
 	public LoginPage(WebDriver driver) {
 		this.driver = driver;
 	}
 	
 	public void logIn(String user, String password, int location) {
 		driver.findElement(By.id("username")).sendKeys(user);
 		driver.findElement(By.id("password")).sendKeys(password);
 		driver.findElements(By.cssSelector("#sessionLocation li")).get(location).click();
 		driver.findElement(By.id("login-button")).click();
 	}
 	
 	public void logIn(String user, String password) {
 		logIn(user, password, 1);
 	}

 	public void logInAsAdmin() {
 		this.logIn("admin", "Admin123");
 	}

    public void logInAsAdmin(int locationIndex) {
        this.logIn("admin", "Admin123", locationIndex);
    }

 	public void logInAsClinicalUser() throws Exception {
 		User clinical = UserDatabaseHandler.insertNewClinicalUser();
 		this.logIn(clinical.getUsername(), "Admin123");
 	}
 	
 	public void logInAsPharmacistUser() throws Exception {
 		User pharmacist = UserDatabaseHandler.insertNewPharmacistUser();
 		this.logIn(pharmacist.getUsername(), "Admin123");
 	}
 
     public void logInAsArchivistUser() throws Exception{
         User archivist = UserDatabaseHandler.insertNewArchivistUser();
         this.logIn(archivist.getUsername(), "Admin123");
     }
 
 }
