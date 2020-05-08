 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
import java.util.List;

 public class UserAdmin extends AbstractPageObject {
 
 	public UserAdmin(WebDriver driver) {
 		super(driver);
 	}
 
 
 	public void createAccount(String firstName, String lastName, String username, String password) {
         driver.findElement(By.linkText("Manage Accounts")).click();
         driver.findElement(By.id("create-account-button")).click();
         driver.findElement(By.name("givenName")).sendKeys(firstName);
         driver.findElement(By.name("familyName")).sendKeys(lastName);
 
         // clicar em user account details
         driver.findElement(By.id("createUserAccountButton")).click();
 
         driver.findElement(By.name("username")).sendKeys(username);
         driver.findElement(By.name("password")).sendKeys(password);
         driver.findElement(By.name("confirmPassword")).sendKeys(password);
 
         WebElement select = driver.findElement(By.name("privilegeLevel"));
         List<WebElement> options = select.findElements(By.tagName("option"));
         for (WebElement option : options) {
             if ("Privilege Level: Full".equals(option.getText()))
                 option.click();
         }
 
        driver.findElement(By.xpath("//*[@name='capabilities']")).click();
 
         // clicar em save button
         driver.findElement(By.id("save-button")).click();
     }
 
 	public boolean isAccountCreatedSuccesfully() {
 		return driver.findElement(By.id("info-message")).getText().contains("Saved account successfully");
 	}
 	
 	@Override
 	public void initialize() {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
