 package com.eric4tw.pair3;
 
 import static junit.framework.Assert.assertEquals;
 
 import org.junit.Test;
 
 public class SalaryTest {
 	@Test
 	public void noTaxForMaleIfSalaryInExemptRange() {
 		assertEquals(0.0, new Salary('M', 22, 180000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void noTaxForFemaleIfSalaryInExemptRange() {
 		assertEquals(0.0, new Salary('F', 22, 190000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxOf10PercentForMaleIfSalaryInFirstIncomeSlab() {
 		assertEquals(1000.0, new Salary('M', 22, 190000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxOf10PercentForFemaleIfSalaryInFirstIncomeSlab() {
 		assertEquals(1000.0, new Salary('F', 22, 200000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxForMaleIfSalaryInSecondSlab() {
 		assertEquals(34000.0, new Salary('M', 22, 510000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxForFemaleIfSalaryInSecondSlab() {
 		assertEquals(33000.0, new Salary('F', 22, 510000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxForMaleIfSalaryInThirdSlab() {
 		assertEquals(95000.0, new Salary('M', 22, 810000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxForFemaleIfSalaryInThirdSlab() {
 		assertEquals(94000.0, new Salary('F', 22, 810000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void noTaxForSeniorCitizenIfInExemptedRange() {
 		assertEquals(0.0, new Salary('M', 60, 250000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxOf10PercentForSeniorCitizenIfInFirstIncomeSlab() {
 		assertEquals(1000.0, new Salary('M', 60, 260000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxForSeniorCitizenIfInSecondIncomeSlab() {
 		assertEquals(27000.0, new Salary('M', 60, 510000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxForSeniorCitizenIfInThirdIncomeSlab() {
		assertEquals(78000.0, new Salary('M', 60, 810000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void noTaxForVerySeniorCitizenIfInExemptedRange() {
 		assertEquals(0.0, new Salary('M', 80, 500000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxOf20PercentForVerySeniorCitizenIfInFirstIncomeSlab() {
 		assertEquals(2000.0, new Salary('M', 80, 510000).calculatetax(), 0.1);
 	}
 
 	@Test
 	public void taxForVerySeniorCitizenIfInSecondIncomeSlab() {
 		assertEquals(63000.0, new Salary('M', 80, 810000).calculatetax(), 0.1);
 	}
 }
 // 32 + 60 +
