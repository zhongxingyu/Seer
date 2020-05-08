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
 package nl.surfnet.bod;
 
 import nl.surfnet.bod.support.TestExternalSupport;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class PhysicalPortTestSelenium extends TestExternalSupport {
 
   private static final String GROUP_NAME = "2COLLEGE";
 
   @Before
   public void setup() {
     getNocDriver().createNewPhysicalResourceGroup(GROUP_NAME, ICT_MANAGERS_GROUP, "test@example.com");
   }
 
   @Test
   public void allocatePhysicalPortFromInstitutePage() {
     getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC label", "Mock_Poort 1de verdieping toren1a");
 
     getNocDriver().verifyPhysicalPortWasAllocated(NMS_PORT_ID_1, "NOC label");
 
     getNocDriver().unlinkPhysicalPort(NMS_PORT_ID_1);
   }
 
   @Test
   public void createRenameAndDeleteAPhysicalPort() {
     String nocLabel = "My Selenium Port (Noc)";
     String managerLabel1 = "My Selenium Port (Manager 1st)";
     String managerLabel2 = "My Selenium Port (Manager 2nd)";
 
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, nocLabel, managerLabel1, GROUP_NAME);
 
     getNocDriver().verifyPhysicalPortWasAllocated(NMS_PORT_ID_1, nocLabel);
 
     getNocDriver().verifyPhysicalPortHasEnabledUnallocateIcon(NMS_PORT_ID_1, nocLabel);
 
     getNocDriver().gotoEditPhysicalPortAndVerifyManagerLabel(NMS_PORT_ID_1, managerLabel1);
 
     getNocDriver().switchToManager();
 
     getManagerDriver().changeManagerLabelOfPhyiscalPort(NMS_PORT_ID_1, managerLabel2);
 
     getManagerDriver().verifyManagerLabelChanged(NMS_PORT_ID_1, managerLabel2);
 
     getManagerDriver().switchToNoc();
 
     getNocDriver().gotoEditPhysicalPortAndVerifyManagerLabel(NMS_PORT_ID_1, managerLabel2);
 
     getNocDriver().unlinkPhysicalPort(NMS_PORT_ID_1);
   }
 
   @Test
   public void checkUnallocateState() {
     final String vpOne = "VirtualPort One";
     String nocLabel = "My Selenium Port (Noc)";
     String managerLabel1 = "My Selenium Port (Manager 1st)";
 
     getNocDriver().linkPhysicalPort(NMS_PORT_ID_1, nocLabel, managerLabel1, GROUP_NAME);
     getWebDriver().clickLinkInLastEmail();
     getNocDriver().verifyPhysicalPortHasEnabledUnallocateIcon(NMS_PORT_ID_1, nocLabel);
 
     getManagerDriver().verifyPhysicalPortHasEnabledUnallocateIcon(NMS_PORT_ID_1, managerLabel1);
 
     // Link a VirtualPort to the PhysicalPort, PhysicalPort cannot be
     // unallocated anymore
     getManagerDriver().switchToUser();
     getUserDriver().requestVirtualPort("selenium-users");
     getUserDriver().selectInstituteAndRequest(GROUP_NAME, 1000, "Doe mijn een nieuw poort...");
     getUserDriver().switchToManager(GROUP_NAME);
     getWebDriver().clickLinkInLastEmail();
     getManagerDriver().acceptVirtualPort(vpOne);
 
     getManagerDriver().verifyPhysicalPortHasDisabeldUnallocateIcon(NMS_PORT_ID_1, managerLabel1, "related");
 
     getManagerDriver().switchToNoc();
     getNocDriver().verifyPhysicalPortIsNotOnUnallocatedPage(NMS_PORT_ID_1, nocLabel);
 
     // Delete Vp
     getNocDriver().switchToManager();
     getManagerDriver().deleteVirtualPort(vpOne);
 
     // After delete VP, the PysicalPort should be able to unallocate
     getManagerDriver().verifyPhysicalPortHasEnabledUnallocateIcon(NMS_PORT_ID_1, managerLabel1);
     getManagerDriver().switchToNoc();
     getNocDriver().unlinkPhysicalPort(NMS_PORT_ID_1);
   }
 
   @Test
   public void searchPorts() {
     try {
       getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 1 label", "Mock_Poort 1de verdieping toren1a");
       getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 2 label", "Mock_Poort 2de verdieping toren1b");
       getNocDriver().addPhysicalPortToInstitute(GROUP_NAME, "NOC 3 label", "Mock_Poort 3de verdieping toren1c");
 
       getNocDriver().verifyAllocatedPortsBySearch("1", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);
       getNocDriver().verifyAllocatedPortsBySearch("*1*", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);

      getNocDriver().verifyAllocatedPortsBySearch("'NOC 1 label'", BOD_PORT_ID_1);
      getNocDriver().verifyAllocatedPortsBySearch("'NOC ? label'", BOD_PORT_ID_1, BOD_PORT_ID_2, BOD_PORT_ID_4);
     }
     finally {
       getNocDriver().unlinkPhysicalPort(NMS_PORT_ID_1);
       getNocDriver().unlinkPhysicalPort(NMS_PORT_ID_2);
       getNocDriver().unlinkPhysicalPort(NMS_PORT_ID_4);
     }
   }
 }
