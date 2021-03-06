 /*
  * Copyright © 2013 VillageReach.  All Rights Reserved.  This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
  *
  * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package org.openlmis.functional;
 
 
 import org.openlmis.UiUtils.CaptureScreenshotOnFailureListener;
 import org.openlmis.UiUtils.TestCaseHelper;
 import org.openlmis.pageobjects.ProgramProductISAPage;
 import org.openlmis.pageobjects.HomePage;
 import org.openlmis.pageobjects.LoginPage;
 import org.openqa.selenium.WebElement;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 import org.testng.annotations.*;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.thoughtworks.selenium.SeleneseTestBase.*;
 
 
 @TransactionConfiguration(defaultRollback = true)
 @Transactional
 
 @Listeners(CaptureScreenshotOnFailureListener.class)
 
 public class ManageProgramProductISA extends TestCaseHelper {
 
   @BeforeMethod(groups = {"functional2"})
   public void setUp() throws Exception {
     super.setup();
     setupProgramProductTestDataWithCategories("P1", "antibiotic1", "C1", "VACCINES");
     setupProgramProductTestDataWithCategories("P2", "antibiotic2", "C2", "VACCINES");
     setupProgramProductTestDataWithCategories("P3", "antibiotic3", "C3", "TB");
     setupProgramProductTestDataWithCategories("P4", "antibiotic4", "C4", "TB");
     dbWrapper.updateProgramToAPushType("TB", false);
   }
 
 
   @Test(groups = {"functional2"}, dataProvider = "Data-Provider-Function")
   public void testMinimumProgramProductISA(String userSIC, String password, String program) throws Exception {
     ProgramProductISAPage programProductISAPage = navigateProgramProductISAPage(userSIC, password, program);
     programProductISAPage.fillProgramProductISA("1", "2", "3", "4", "5", "10");
     String actualISA = programProductISAPage.fillPopulation("1");
     String expectedISA = calculateISA("1", "2", "3", "4", "5", "10", "1");
     assertEquals(actualISA, expectedISA);
     programProductISAPage.cancelISA();
     HomePage homePage = new HomePage(testWebDriver);
     homePage.navigateHomePage();
   }
 
   @Test(groups = {"functional2"}, dataProvider = "Data-Provider-Function")
   public void testProgramProductISA(String userSIC, String password, String program) throws Exception {
     ProgramProductISAPage programProductISAPage = navigateProgramProductISAPage(userSIC, password, program);
     programProductISAPage.fillProgramProductISA("1", "2", "3", "4", "50", "10");
     String actualISA = programProductISAPage.fillPopulation("1");
     String expectedISA = calculateISA("1", "2", "3", "4", "50", "10", "1");
     assertEquals(actualISA, expectedISA);
     programProductISAPage.cancelISA();
     HomePage homePage = new HomePage(testWebDriver);
     homePage.navigateHomePage();
   }
 
   @Test(groups = {"functional2"}, dataProvider = "Data-Provider-Function")
   public void testISAFormula(String userSIC, String password, String program) throws Exception {
     ProgramProductISAPage programProductISAPage = navigateProgramProductISAPage(userSIC, password, program);
     programProductISAPage.fillProgramProductISA("12345678", "2", "0", "4", "-50", "10");
    programProductISAPage.saveISA();
//    programProductISAPage.verifySuccessMessageDiv();
     String formula = programProductISAPage.getISAFormulaFromISAFormulaModal();
//    programProductISAPage.verifyISAFormula(formula);
     HomePage homePage = new HomePage(testWebDriver);
     homePage.navigateHomePage();
   }
 
   @Test(groups = {"functional2"}, dataProvider = "Data-Provider-Function-Verify-Push-Type-Program")
   public void testPushTypeProgramsInDropDown(String userSIC, String password, String program1, String program2) throws Exception {
     ProgramProductISAPage programProductISAPage = navigateConfigureProductISAPage(userSIC, password);
 
     List<WebElement> valuesPresentInDropDown = programProductISAPage.getAllSelectOptionsFromProgramDropDown();
     List<String> programValuesToBeVerified = new ArrayList<String>();
     programValuesToBeVerified.add(program1);
     verifyAllSelectFieldValues(programValuesToBeVerified, valuesPresentInDropDown);
 
     dbWrapper.updateProgramToAPushType(program2, true);
 
     HomePage homePage = new HomePage(testWebDriver);
     homePage.navigateHomePage();
     homePage.navigateProgramProductISA();
     valuesPresentInDropDown = programProductISAPage.getAllSelectOptionsFromProgramDropDown();
     List<String> programValuesToBeVerifiedAfterUpdate = new ArrayList<String>();
     programValuesToBeVerifiedAfterUpdate.add(program1);
     programValuesToBeVerifiedAfterUpdate.add(program2);
     verifyAllSelectFieldValues(programValuesToBeVerifiedAfterUpdate, valuesPresentInDropDown);
 
   }
 
   @Test(groups = {"functional2"}, dataProvider = "Data-Provider-Function-Multiple-Programs")
   public void testProgramProductsMappings(String userSIC, String password, String program1, String program2,
                                           String product1, String product2,
                                           String product3, String product4) throws Exception {
     ProgramProductISAPage programProductISAPage = navigateConfigureProductISAPage(userSIC, password);
 
     programProductISAPage.selectValueFromProgramDropDown(program1);
     String productsList = programProductISAPage.getProductsDisplayingOnConfigureProgramISAPage();
     assertTrue("Product " + product1 + " should be displayed", productsList.contains(product1));
     assertTrue("Product " + product2 + " should be displayed", productsList.contains(product2));
 
     dbWrapper.updateProgramToAPushType(program2, true);
 
     HomePage homePage = new HomePage(testWebDriver);
     homePage.navigateHomePage();
     homePage.navigateProgramProductISA();
     programProductISAPage.selectValueFromProgramDropDown(program2);
     productsList = programProductISAPage.getProductsDisplayingOnConfigureProgramISAPage();
     assertTrue("Product " + product3 + " should be displayed", productsList.contains(product3));
     assertTrue("Product " + product4 + " should be displayed", productsList.contains(product4));
 
   }
 
   @Test(groups = {"functional2"}, dataProvider = "Data-Provider-Function")
   public void testVerifyMandatoryFields(String userSIC, String password, String program) throws Exception {
     ProgramProductISAPage programProductISAPage = navigateProgramProductISAPage(userSIC, password, program);
     programProductISAPage.verifyFieldsOnISAModalWindow();
     programProductISAPage.saveISA();
     programProductISAPage.verifyMandatoryFieldsToBeFilled();
     programProductISAPage.verifyErrorMessageDiv();
   }
 
 
   @Test(groups = {"functional2"}, dataProvider = "Data-Provider-Function")
   public void testVerifyMonthlyRestockAmountFieldAvailability(String userSIC, String password, String program) throws Exception {
     ProgramProductISAPage programProductISAPage = navigateProgramProductISAPage(userSIC, password, program);
     programProductISAPage.fillProgramProductISA("1", "2", "3", "4", "0", "10");
     programProductISAPage.verifyMonthlyRestockAmountPresent();
     programProductISAPage.cancelISA();
     HomePage homePage = new HomePage(testWebDriver);
     homePage.navigateHomePage();
   }
 
   private ProgramProductISAPage navigateProgramProductISAPage(String userSIC, String password, String program) throws IOException {
     LoginPage loginPage = new LoginPage(testWebDriver, baseUrlGlobal);
     HomePage homePage = loginPage.loginAs(userSIC, password);
     ProgramProductISAPage programProductISAPage = homePage.navigateProgramProductISA();
     programProductISAPage.selectProgram(program);
     programProductISAPage.editFormula();
     return programProductISAPage;
   }
 
   private ProgramProductISAPage navigateConfigureProductISAPage(String userSIC, String password) throws IOException {
     LoginPage loginPage = new LoginPage(testWebDriver, baseUrlGlobal);
     HomePage homePage = loginPage.loginAs(userSIC, password);
     ProgramProductISAPage programProductISAPage = homePage.navigateProgramProductISA();
     return programProductISAPage;
   }
 
   public String calculateISA(String ratio, String dosesPerYear, String wastage, String bufferPercentage, String adjustmentValue, String minimumValue, String population) {
     Float calculatedISA = Integer.parseInt(population) * Float.parseFloat(ratio) * Float.parseFloat(dosesPerYear) * Float.parseFloat(wastage) / 12 * Float.parseFloat(bufferPercentage) + Float.parseFloat(adjustmentValue);
 //    if (calculatedISA < Float.parseFloat(minimumValue))
 //      return (minimumValue);
 //    else
     return (new BigDecimal(calculatedISA).setScale(0, BigDecimal.ROUND_CEILING)).toString();
   }
 
 
   private void verifyAllSelectFieldValues(List<String> valuesToBeVerified, List<WebElement> valuesPresentInDropDown) {
     String collectionOfValuesPresentINDropDown = "";
     int valuesToBeVerifiedCounter = valuesToBeVerified.size();
     int valuesInSelectFieldCounter = valuesPresentInDropDown.size();
 
     if (valuesToBeVerifiedCounter == valuesInSelectFieldCounter - 1) {
       for (WebElement webElement : valuesPresentInDropDown) {
         collectionOfValuesPresentINDropDown = collectionOfValuesPresentINDropDown + webElement.getText().trim();
       }
       for (String values : valuesToBeVerified) {
         assertTrue(collectionOfValuesPresentINDropDown.contains(values));
       }
     } else {
       fail("Values in select field are not same in number as values to be verified");
     }
 
   }
 
   @AfterMethod(groups = {"functional2"})
   public void tearDown() throws Exception {
     HomePage homePage = new HomePage(testWebDriver);
     homePage.logout(baseUrlGlobal);
     dbWrapper.deleteData();
     dbWrapper.closeConnection();
   }
 
 
   @DataProvider(name = "Data-Provider-Function")
   public Object[][] parameterIntTestProviderPositive() {
     return new Object[][]{
       {"Admin123", "Admin123", "VACCINES"}
     };
 
   }
 
   @DataProvider(name = "Data-Provider-Function-Verify-Push-Type-Program")
   public Object[][] parameterIntTestPushTypeProgram() {
     return new Object[][]{
       {"Admin123", "Admin123", "VACCINES","TB"}
     };
 
   }
 
   @DataProvider(name = "Data-Provider-Function-Multiple-Programs")
   public Object[][] parameterIntTestMultipleProducts() {
     return new Object[][]{
       {"Admin123", "Admin123", "VACCINES", "TB", "antibiotic1", "antibiotic2", "antibiotic3", "antibiotic4"}
     };
 
   }
 }
 
