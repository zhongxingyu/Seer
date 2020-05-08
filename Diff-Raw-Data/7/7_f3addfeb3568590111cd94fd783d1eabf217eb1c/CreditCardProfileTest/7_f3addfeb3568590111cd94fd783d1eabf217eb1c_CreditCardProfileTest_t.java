 package com.sk.domain;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 
 import org.junit.Test;
 
 import com.sk.util.builder.CreditCardProfileBuilder;
 import com.sk.util.builder.InstallmentPlanBuilder;
 
 
 public class CreditCardProfileTest {
 	
 	@Test
 	public void shouldDeleteInstallmentPlan() {
 		InstallmentPlan installmentPlan = new InstallmentPlanBuilder().build();
 		CreditCardProfile cardProfile = new CreditCardProfileBuilder().installmentPlans(installmentPlan).build();
 		
		
 		cardProfile.deleteInstallmentPlan(installmentPlan);
 		
 		assertThat(cardProfile.getInstallmentPlans().size(),equalTo(0));
 	}
 	

	@Test
	public void shouldTestSKWorkshop5() {
		System.out.println("Testing");
	}
 	@Test
 	public void shouldAddInstallmentPlan() {
 		InstallmentPlan installmentPlan = new InstallmentPlanBuilder().build();
 		CreditCardProfile cardProfile = new CreditCardProfileBuilder().build();
 		
 		cardProfile.addInstallmentPlan(installmentPlan);
 		
 		assertThat(cardProfile.getInstallmentPlans().size(),equalTo(1));
 	}
 	
 	@Test
 	public void shouldCalculateMonthlyPaymentAsTotalCostDividedByMonthsWhenInterestRateIsZero() {
 		InstallmentPlan installmentPlan = new InstallmentPlanBuilder().interestRate(0.0).months(3).build();
 		CreditCardProfile cardProfile = new CreditCardProfileBuilder().installmentPlans(installmentPlan).build();
 		
 		double monthlyPayment = cardProfile.monthlyPaymentOf(90d,3);
 		
 		assertThat(monthlyPayment, equalTo(30d));
 	}
 	
 	@Test
 	public void shouldRoundToTwoDigitsAfterDecimalPoint() {
 		InstallmentPlan installmentPlan = new InstallmentPlanBuilder().interestRate(0.0).months(3).build();
 		CreditCardProfile cardProfile = new CreditCardProfileBuilder().installmentPlans(installmentPlan).build();
 		
 		double monthlyPayment = cardProfile.monthlyPaymentOf(100d,3);
 		assertThat(monthlyPayment, equalTo(33.33d));
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void shouldNotCalculateMonthlyPaymentWhenInstallmentPlanDoesNotExist() {
 		CreditCardProfile cardProfile = new CreditCardProfileBuilder().build();
 		
 		cardProfile.monthlyPaymentOf(100d,3);
 	}
 	
 	@Test
 	public void shouldBeIssuerWhenBinDigitsMatch() {
 		CreditCardProfile cardProfile = new CreditCardProfileBuilder().bin("121212").build();
 		
 		assertThat(cardProfile.issuerOf("121212121212121212"),equalTo(true));
 	}
 
 
 }
