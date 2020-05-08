 import org.junit.Test;
 import src.Diamond;
 
 import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
 
 public class DiamondTest {
 
     @Test
     public void shouldMakeDiamondStringWithCenterCountOfOne(){
         assertTrue(new Diamond(1).print().equals("*\n"));
     }
 
     @Test
     public void shouldMakeDiamondStringWithCenterCountOfThree(){
         assertTrue(new Diamond(3).print().equals(" * \n***\n * \n"));
     }
 
     @Test
     public void shouldMakeDiamondStringWithMultipleNonCenterIncrements(){
        assertThat(new Diamond(5).print(), is("  *  \n *** \n*****\n *** \n  *  \n"));
     }
 }
