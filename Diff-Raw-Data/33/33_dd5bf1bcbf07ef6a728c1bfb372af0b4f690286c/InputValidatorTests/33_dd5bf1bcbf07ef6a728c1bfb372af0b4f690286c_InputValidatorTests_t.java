 package com.VSSBudgetBoss.tests;
 
 import static org.junit.Assert.*;
import com.VSSBudgetBoss.budget.*;
import org.junit.Before;
 import org.junit.Test;
 
 import com.VSSBudgetBoss.cli.InputValidator;
 
 public class InputValidatorTests {
	
	InputValidator validator;
	
	@Before
	public void setup(){
	validator = new InputValidator();
	}
 	@Test
	
	public void testInputIsNotYOrN(){
		//Given: An input is required from a user
		//And: The input needs to be either "y" or "n" (case doesn't matter)
		//When: The user inputs something besides those two
		//Then:The input will not be validated
		assertTrue(validator.inputIsNotYOrN("this ain't yo momma's 'y'"));
 	}
 
 }
