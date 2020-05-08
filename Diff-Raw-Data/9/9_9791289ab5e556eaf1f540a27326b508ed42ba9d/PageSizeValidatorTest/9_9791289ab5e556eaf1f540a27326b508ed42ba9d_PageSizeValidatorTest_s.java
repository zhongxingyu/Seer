 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.vaadin.data.Validator.InvalidValueException;
 
 import de.uni_leipzig.simba.saim.gui.validator.PageSizeValidator;
 
 
 public class PageSizeValidatorTest {
 
 	String badValues[] = { "0", "-122", "sdhsa", "232.545", "34,6", "21n"};
 	String goodValues[] = {"1000", "-1"};
 	@Test
 	public void testIsValid() {
 		PageSizeValidator valid = new PageSizeValidator("test");
 		for(String value : badValues) {
 			try{
 				 valid.validate(value);
 				 fail("Parsed "+value+" without exception.");
 			} catch(InvalidValueException e) {
 				
 			}
 		}
 		for(String val : goodValues) {
			assertTrue(valid.isValid(val));
 		}
 	}
 
 }
