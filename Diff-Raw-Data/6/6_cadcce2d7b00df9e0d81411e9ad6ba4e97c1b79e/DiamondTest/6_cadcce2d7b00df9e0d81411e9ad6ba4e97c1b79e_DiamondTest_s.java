 import org.junit.Test;
 import src.Diamond;
 
 import static junit.framework.Assert.assertTrue;
 
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
        String expectedDiamond = "   *   \n *** \n*****\n *** \n   *   \n";
        assertTrue(new Diamond(5).print().equals(expectedDiamond));
     }
 }
