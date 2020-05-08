 /*
  * Copyright (c) 2005-2008 Grameen Foundation USA
  * All rights reserved.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  * 
  * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
  * explanation of the license and how it is applied.
  */
 package acceptance.loan;
 
 import junit.framework.Assert;
 
 import org.springframework.test.context.ContextConfiguration;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import framework.pageobjects.ClientDetailPage;
 import framework.pageobjects.DisburseLoanPage;
 import framework.pageobjects.HomePage;
 import framework.pageobjects.LoanDetailPage;
 import framework.pageobjects.LoginPage;
 import framework.test.UiTestCaseBase;
 
 /*
  * Corresponds to story 675 in mingle
  * http://mingle.mifos.org:7070/projects/cheetah/cards/675
  */
 @ContextConfiguration(locations={"classpath:ui-test-context.xml"})
@Test(groups={"userCanDisburseBasicLoanStory","acceptance","ui"})
 public class UserCanDisburseBasicLoanStoryTest extends UiTestCaseBase {
 
 	private LoginPage loginPage;
 	private static final String CLIENT_NAME = "test client";
 	private static final String LOAN_NAME = "test loan";
 	
 	@BeforeMethod
	public void setUp() {
 		super.setUp();
 		loginPage = new LoginPage(selenium);
 	}
 
 	@AfterMethod
 	public void logOut() {
 		loginPage.logout();
 	}
 
	@Test(groups="workInProgress")
 	public void disburseBasicLoanTest() {
 		// preconditions: a loan created for a loan product and a client name "test client" must exist
 		HomePage homePage = loginPage.loginAs("mifos", "testmifos");
 		ClientDetailPage clientDetailPage = homePage.searchForClient(CLIENT_NAME);
 		// verify that we landed on the client detail page
 		Assert.assertTrue(selenium.isTextPresent(CLIENT_NAME));
 		LoanDetailPage loanDetailPage = clientDetailPage.navigateToLoanDetailPageForLoan(LOAN_NAME);
 		// verify that we landed on the loan detail page
 		Assert.assertTrue(selenium.isTextPresent(LOAN_NAME));
 		DisburseLoanPage disburseLoanPage = loanDetailPage.navigateToDisburseLoanPage();
 		// verify that we landed on the disburse loan page
 		// TODO: add verification
 		disburseLoanPage.disburseLoan();
 		Assert.assertTrue(selenium.isTextPresent("Loan has been disbursed"));
 	}
 
 }
