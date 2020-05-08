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
 package nl.surfnet.bod.support;
 
 import static junit.framework.Assert.assertTrue;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.not;
 
 import java.io.File;
 import java.util.concurrent.TimeUnit;
 
 import nl.surfnet.bod.domain.ReservationStatus;
 import nl.surfnet.bod.pages.ListReservationPage;
 import nl.surfnet.bod.pages.NewReservationPage;
 import nl.surfnet.bod.pages.physical.ListPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.physical.NewPhysicalResourceGroupPage;
 import nl.surfnet.bod.pages.virtual.ListVirtualPortPage;
 import nl.surfnet.bod.pages.virtual.ListVirtualResourceGroupPage;
 import nl.surfnet.bod.pages.virtual.NewVirtualPortPage;
 import nl.surfnet.bod.pages.virtual.NewVirtualResourceGroupPage;
 
 import org.hamcrest.Matcher;
 import org.hamcrest.Matchers;
import org.hamcrest.core.CombinableMatcher;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.openqa.selenium.OutputType;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import com.google.common.io.Files;
 
 public class BodWebDriver {
 
   private static final String URL_UNDER_TEST = withEndingSlash(System.getProperty("selenium.test.url",
       "http://localhost:8080/bod"));
 
   private FirefoxDriver driver;
 
   public synchronized void initializeOnce() {
     if (driver == null) {
       this.driver = new FirefoxDriver();
       this.driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
       Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
           if (driver != null) {
             driver.quit();
           }
         }
       });
     }
   }
 
   public void takeScreenshot(File screenshot) throws Exception {
     if (driver != null) {
       File temp = driver.getScreenshotAs(OutputType.FILE);
       Files.copy(temp, screenshot);
     }
   }
 
   public void createNewPhysicalGroup(String institute, String adminGroup, String email) throws Exception {
     NewPhysicalResourceGroupPage page = NewPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.sendInstitute(institute);
     page.sendAdminGroup(adminGroup);
     page.sendEmail(email);
 
     page.save();
   }
 
   private static String withEndingSlash(String path) {
     return path.endsWith("/") ? path : path + "/";
   }
 
   public void deletePhysicalGroup(String institute, String adminGroup, String email) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     page.delete(institute, adminGroup, email);
   }
 
   public void verifyGroupWasCreated(String institute, String adminGroup, String email) {
     assertListTable(Matchers.<String> allOf(containsString(institute), containsString(adminGroup),
         containsString(email)));
   }
 
   public void verifyGroupWasDeleted(String institute, String adminGroup, String email) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);
 
     if (page.containsAnyItems()) {
       assertListTable(not(Matchers.<String> allOf(containsString(institute), containsString(adminGroup),
           containsString(email))));
     }
   }
 
   private void assertListTable(Matcher<String> tableMatcher) {
     ListPhysicalResourceGroupPage page = ListPhysicalResourceGroupPage.get(driver);
     String row = page.getTable();
 
     assertThat(row, tableMatcher);
   }
 
   public NewVirtualResourceGroupPage createNewVirtualResourceGroup(String name) throws Exception {
     NewVirtualResourceGroupPage page = NewVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
     page.sendName(name);
     page.sendSurfConextGroupName(name);
     page.save();
 
     return page;
   }
 
   public void deleteVirtualResourceGroup(String vrgName) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver, URL_UNDER_TEST);
 
     page.delete(vrgName);
   }
 
   public void verifyVirtualResourceGroupWasCreated(String name) {
     assertVirtualResourceGroupListTable(containsString(name));
   }
 
   public void verifyVirtualResourceGroupWasDeleted(String vrgName) {
     assertVirtualResourceGroupListTable(not(containsString(vrgName)));
   }
 
   private void assertVirtualResourceGroupListTable(Matcher<String> tableMatcher) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver);
     String row = page.getTable();
 
     assertThat(row, tableMatcher);
   }
 
   public void verifyHasValidationError() {
     NewVirtualResourceGroupPage page = NewVirtualResourceGroupPage.get(driver);
 
     assertTrue(page.hasNameValidationError());
   }
 
   public void createNewVirtualPort(String name, String maxBandwidth) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     page.sendName(name);
     page.sendMaxBandwidth(maxBandwidth);
     page.save();
   }
 
   public void verifyVirtualPortWasCreated(String name, String maxBandwidth) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver);
 
     String table = page.getTable();
 
     assertThat(table, containsString(name));
     assertThat(table, containsString(maxBandwidth));
   }
 
   public void createNewReservation(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
     NewReservationPage page = NewReservationPage.get(driver, URL_UNDER_TEST);
 
     page.sendStartDate(startDate);
     page.sendStartTime(startTime);
     page.sendEndDate(endDate);
     page.sendEndTime(endTime);
     page.sendBandwidth("500");
 
     page.save();
   }
 
   public void verifyReservationWasCreated(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
     ListReservationPage page = ListReservationPage.get(driver);
 
     String start = ListReservationPage.DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
     String end = ListReservationPage.DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));
 
     String table = page.getTable();
 
     assertThat(
         table,
         allOf(
            CombinableMatcher.<String>either(containsString(ReservationStatus.REQUESTED.name()))
              .or(containsString(ReservationStatus.SCHEDULED.name())),
             containsString(start),
             containsString(end)));
   }
 
   public void verifyReservationStartDateHasError(String string) {
     NewReservationPage page = NewReservationPage.get(driver);
     String error = page.getStartDateError();
 
     assertThat(error, containsString(string));
   }
 
   public void cancelReservation(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
     ListReservationPage page = ListReservationPage.get(driver, URL_UNDER_TEST);
 
     page.deleteByDates(startDate, endDate, startTime, endTime);
   }
 
   public void verifyReservationWasCanceled(LocalDate startDate, LocalDate endDate, LocalTime startTime,
       LocalTime endTime) {
     ListReservationPage page = ListReservationPage.get(driver);
 
     page.reservationShouldBe(startDate, endDate, startTime, endTime, ReservationStatus.CANCELLED);
   }
 
   public void deleteVirtualPort(String name) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver, URL_UNDER_TEST);
 
     page.delete(name);
   }
 
   public void verifyVirtualPortWasDeleted(String name) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver);
 
     if (page.containsAnyItems()) {
       assertListTable(not(containsString(name)));
     }
   }
 
 }
