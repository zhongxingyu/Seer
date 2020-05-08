 /**
  * The owner of the original code is SURFnet BV.
  *
  * Portions created by the original owner are Copyright (C) 2011-2012 the
  * original owner. All Rights Reserved.
  *
  * Portions created by other contributors are Copyright (C) the contributor.
  * All Rights Reserved.
  *
  * Contributor(s):
  *   (Contributors insert name & email here)
  *
  * This file is part of the SURFnet7 Bandwidth on Demand software.
  *
  * The SURFnet7 Bandwidth on Demand software is free software: you can
  * redistribute it and/or modify it under the terms of the BSD license
  * included with this distribution.
  *
  * If the BSD license cannot be found with this distribution, it is available
  * at the following location <http://www.opensource.org/licenses/BSD-3-Clause>
  */
 package nl.surfnet.bod.pages.user;
 
 import nl.surfnet.bod.pages.AbstractFormPage;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.FindBy;
 import org.openqa.selenium.support.PageFactory;
 
 public class NewReservationPage extends AbstractFormPage {
 
   private static final String PAGE = "/reservations/create";
 
   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
   private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("H:mm");
 
   @FindBy(id = "_name_id")
   private WebElement labelInput;
 
   @FindBy(id = "_startDate_id")
   private WebElement startDateInput;
 
   @FindBy(id = "_endDate_id")
   private WebElement endDateInput;
 
   @FindBy(id = "_startTime_id")
   private WebElement startTimeInput;
 
   @FindBy(id = "_endTime_id")
   private WebElement endTimeInput;
 
   @FindBy(id = "_bandwidth_id")
   private WebElement bandwidhtInput;
 
   @FindBy(id = "_startDate_error_id")
   private WebElement startDateError;
 
   @FindBy(id = "now_chk")
   private WebElement nowCheckbox;
 
   @FindBy(id = "forever_chk")
   private WebElement foreverCheckbox;
 
   public NewReservationPage(RemoteWebDriver driver) {
     super(driver);
   }
 
   public static NewReservationPage get(RemoteWebDriver driver, String urlUnderTest) {
     driver.get(urlUnderTest + PAGE);
     return get(driver);
   }
 
   public static NewReservationPage get(RemoteWebDriver driver) {
     NewReservationPage page = new NewReservationPage(driver);
     PageFactory.initElements(driver, page);
 
     return page;
   }
 
   public void sendStartDate(LocalDate startDate) {
     sendDate(startDateInput, startDate);
   }
 
   public void sendStartTime(LocalTime startTime) {
     startTimeInput.clear();
     startTimeInput.sendKeys(TIME_FORMATTER.print(startTime));
   }
 
   public void sendEndDate(LocalDate endDate) {
     sendDate(endDateInput, endDate);
   }
 
   private void sendDate(WebElement input, LocalDate date) {
     String dateString = DATE_FORMATTER.print(date);
 
     System.out.println("Entering a date in " + input.getAttribute("name") + " date: " + dateString);
 
     input.click();
    input.clear();
    System.out.println(String.format("Text before send keys of %s: %s", input.getAttribute("name"), input.getAttribute("value")));
     input.sendKeys(dateString);
    System.out.println(String.format("Text after send keys of %s: %s", input.getAttribute("name"), input.getAttribute("value")));
   }
 
   public void sendEndTime(LocalTime endTime) {
     endTimeInput.clear();
     endTimeInput.sendKeys(TIME_FORMATTER.print(endTime));
   }
 
   public void sendBandwidth(String bandwidth) {
     bandwidhtInput.clear();
     bandwidhtInput.sendKeys(bandwidth);
   }
 
   public String getStartDateError() {
     if (startDateError == null) {
       return "";
     }
 
     return startDateError.getText();
   }
 
   public void sendLabel(String label) {
     labelInput.clear();
     labelInput.sendKeys(label);
   }
 
   public void clickStartNow() {
     nowCheckbox.click();
   }
 
   public void clickForever() {
     foreverCheckbox.click();
   }
 
 }
