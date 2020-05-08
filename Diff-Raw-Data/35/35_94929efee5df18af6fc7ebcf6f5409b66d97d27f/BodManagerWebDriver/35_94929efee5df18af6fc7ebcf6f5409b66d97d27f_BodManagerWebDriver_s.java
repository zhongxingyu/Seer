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
 import static junit.framework.Assert.fail;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import nl.surfnet.bod.pages.manager.*;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalDateTime;
 import org.joda.time.LocalTime;
 import org.openqa.selenium.NoSuchElementException;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 public class BodManagerWebDriver {
 
   private final RemoteWebDriver driver;
 
   public BodManagerWebDriver(RemoteWebDriver driver) {
     this.driver = driver;
   }
 
   public void managerDashboard() {
     driver.get(BodWebDriver.URL_UNDER_TEST + "manager");
   }
 
   public void verifyManagerLabelChanged(String networkElementPk, String managerLabel) {
     ListPhysicalPortsPage listPage = ListPhysicalPortsPage.get(driver);
 
     listPage.findRow(networkElementPk, managerLabel);
   }
 
  public void gotoCreateNewVirtualPortForPhysicalPort(String networkElementPk) {
    ListPhysicalPortsPage listPage = ListPhysicalPortsPage.get(driver);

    listPage.newVirtualPort(networkElementPk);
  }

   public void verifyPhysicalPortSelected(String managerLabel) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver);
 
     String portName = page.getSelectedPhysicalPort();
 
     assertThat(portName, is(managerLabel));
   }
 
   public void changeManagerLabelOfPhyiscalPort(String networkElementPk, String managerLabel) {
     ListPhysicalPortsPage page = ListPhysicalPortsPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
     EditPhysicalPortPage editPage = page.edit(networkElementPk);
 
     editPage.sendMagerLabel(managerLabel);
     editPage.save();
   }
 
   public void verifyOnEditPhysicalResourceGroupPage(String expectedMailAdress) {
     EditPhysicalResourceGroupPage page = EditPhysicalResourceGroupPage.get(driver);
 
     String email = page.getEmailValue();
     assertThat(email, is(expectedMailAdress));
 
     assertThat(page.getInfoMessages(), hasSize(1));
     assertThat(page.getInfoMessages().get(0), containsString("Your Physical Resource Group is not activated"));
   }
 
   public void createNewVirtualPort(String name, int maxBandwidth, String virtualResourceGroup, String physicalResourceGroup, String physicalPort) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
     page.sendName(name);
     page.sendMaxBandwidth(maxBandwidth);
     page.selectVirtualResourceGroup(virtualResourceGroup);
     page.selectPhysicalResourceGroup(physicalResourceGroup);
     page.selectPhysicalPort(physicalPort);
 
     page.save();
   }
 
   public void verifyVirtualPortExists(String... fields) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver);
 
     page.findRow(fields);
   }
 
   public void deleteVirtualPort(String name) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
     page.delete(name);
   }
 
   public void verifyVirtualPortWasDeleted(String name) {
     ListVirtualPortPage page = ListVirtualPortPage.get(driver);
 
     try {
       page.findRow(name);
       fail(String.format("Virtual port with name %s was not deleted", name));
     }
     catch (NoSuchElementException e) {
       // fine
     }
   }
 
   public NewVirtualResourceGroupPage createNewVirtualResourceGroup(String name, String surfConextGroup) {
     NewVirtualResourceGroupPage page = NewVirtualResourceGroupPage.get(driver, BodWebDriver.URL_UNDER_TEST);
     page.sendName(name);
     page.sendSurfConextGroupName(surfConextGroup);
 
     page.save();
 
     return page;
   }
 
   public void deleteVirtualResourceGroup(String vrgName) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
     page.delete(vrgName);
   }
 
   public void verifyVirtualResourceGroupWasCreated(String name) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver);
     page.findRow(name);
   }
 
   public void verifyVirtualResourceGroupWasDeleted(String vrgName) {
     ListVirtualResourceGroupPage page = ListVirtualResourceGroupPage.get(driver);
     try {
       page.findRow(vrgName);
       fail(String.format("Virtual Resource group with vrgName %s was not deleted", vrgName));
     }
     catch (NoSuchElementException e) {
       // fine
     }
   }
 
   public void verifyNewVirtualResoruceGroupHasValidationError() {
     NewVirtualResourceGroupPage page = NewVirtualResourceGroupPage.get(driver);
 
     assertTrue(page.hasNameValidationError());
   }
 
   public void verifyNewVirtualPortHasProperties(String instituteName, Integer bandwidth) {
     NewVirtualPortPage page = NewVirtualPortPage.get(driver);
 
     String group = page.getSelectedPhysicalResourceGroup();
     Integer ban = page.getBandwidth();
 
     assertThat(group, is(instituteName));
   }
 
   public void editVirtualPort(String orignalName, String newName, int bandwidth, String vlanId) {
     ListVirtualPortPage listPage = ListVirtualPortPage.get(driver);
 
     EditVirtualPortPage editPage = listPage.edit(orignalName);
 
     editPage.sendName(newName);
     editPage.sendMaxBandwidth(bandwidth);
     editPage.sendVlanId(vlanId);
 
     editPage.save();
   }
 
   public void verifyReservationExists(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime, LocalDateTime creationDateTime) {
     ListReservationPage page = ListReservationPage.get(driver, BodWebDriver.URL_UNDER_TEST);
 
     String start = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(startDate.toLocalDateTime(startTime));
     String end = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(endDate.toLocalDateTime(endTime));
     String creation = BodWebDriver.RESERVATION_DATE_TIME_FORMATTER.print(creationDateTime);
 
     page.findRow(start, end, creation);
   }
 
 }
