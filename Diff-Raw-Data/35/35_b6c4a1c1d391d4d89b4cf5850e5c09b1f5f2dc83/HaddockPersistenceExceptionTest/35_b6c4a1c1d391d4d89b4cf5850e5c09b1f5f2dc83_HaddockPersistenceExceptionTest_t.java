 package nu.danielsundberg.persistence.haddock.exception;
 
import nu.danielsundberg.persistence.AbstractTest;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
public class HaddockPersistenceExceptionTest extends AbstractTest {
 
     private static final String TEST_TEXT = "Test text.";
 
     private HaddockPersitenceException exception;
     private Exception otherException = new Exception() {};
 
     @Test
     public void testConstructionText() throws Exception {
         exception = new HaddockPersitenceException(TEST_TEXT);
         try {
             throw exception;
         } catch (HaddockPersitenceException e) {
             assertThat(e.getMessage(), is(TEST_TEXT));
         }
     }
 
 
     @Test
     public void testConstructionTextAndSubException() throws Exception {
         exception = new HaddockPersitenceException(TEST_TEXT, otherException);
     }
 
     @Test
     public void testConstructionSubException() throws Exception {
         exception = new HaddockPersitenceException(otherException);
     }
 
 }
