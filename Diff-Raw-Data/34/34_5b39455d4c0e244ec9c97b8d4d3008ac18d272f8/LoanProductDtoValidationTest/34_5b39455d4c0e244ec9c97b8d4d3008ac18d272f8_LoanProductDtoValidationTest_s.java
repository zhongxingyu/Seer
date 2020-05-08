 package org.mifos.loan.service;
 
 import org.mifos.loan.domain.LoanProductStatus;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
 import org.springmodules.validation.bean.BeanValidator;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 @ContextConfiguration(locations={"classpath:unitTestContext.xml"})
 public class LoanProductDtoValidationTest  extends AbstractTestNGSpringContextTests{
 	
 	private LoanProductDto loanProductDTO;
 	
 	@Autowired
	private BeanValidator validator;
 	
 	@BeforeMethod
 	public void setup() {
 		loanProductDTO = new LoanProductDto();
 	}
 	
 	@Test(groups = { "unit" })
 	public void testValidInputs() {
 		loanProductDTO = new LoanProductDto();
 		loanProductDTO.setLongName("A long name");
 		loanProductDTO.setShortName("a short name");
 		loanProductDTO.setMinInterestRate(1.0);
 		loanProductDTO.setMaxInterestRate(3.0);
 		loanProductDTO.setStatus(LoanProductStatus.ACTIVE);
 		
 		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(loanProductDTO, "loanProduct");
		validator = new BeanValidator();
 		validator.validate(loanProductDTO, errors);
 		Assert.assertEquals(0, errors.getErrorCount());
 	}
 
	//@Test(groups = { "workInProgress" })
 	public void testBlankLongName() {
 		loanProductDTO = new LoanProductDto();
		/*loanProductDTO.setLongName("A long name");
 		loanProductDTO.setShortName("a short name");
 		loanProductDTO.setMinInterestRate(1.0);
 		loanProductDTO.setMaxInterestRate(3.0);
 		loanProductDTO.setStatus(LoanProductStatus.ACTIVE);
		*/
 		BeanPropertyBindingResult errors = new BeanPropertyBindingResult(loanProductDTO, "loanProduct");
		validator = new BeanValidator();
 		validator.validate(loanProductDTO, errors);
 		Assert.assertTrue(errors.getErrorCount() > 0, "Expected an error when longName missing");
 	}
 }
