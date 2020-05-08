 package br.com.tealdi.service;
 
 import java.util.GregorianCalendar;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.tealdi.object.Person;
 import br.com.tealdi.util.DependencyRepository;
 
 public class PersonAgeCalculatorIntegrationTest {
 	private IPersonAgeCalculator calculator;
 	private GregorianCalendar twentyFiveYearsAfterNow;
 	private Person person;
 	private int ageExpected;
 
 	@Before
 	public void setUp() {
 		calculator = DependencyRepository.resolve(IPersonAgeCalculator.class);
 		ageExpected = 25;
		twentyFiveYearsAfterNow = yearsBeforeNow(ageExpected);
 	}
 	
 	@Test
 	public void shouldCalculate() {
 		givenAPersonWith25YearsOld();
 		
 		Assert.assertEquals(ageExpected, calculator.calculateFor(person));
 	}
 	
 	private void givenAPersonWith25YearsOld() {
 		person = new Person("john doo", twentyFiveYearsAfterNow);
 	}
 	
	private GregorianCalendar yearsBeforeNow(int quantity) {
 		GregorianCalendar calendar = new GregorianCalendar();
 		calendar.add(GregorianCalendar.YEAR, -quantity);
 		
 		return calendar;
 	}
 }
