 package experiments;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Test for jumble testing.
  * 
  * @author Tin Pavlinic
  * @version $Revision 1.0 $
  */
 public class FloatReturnTest extends TestCase {
   public void testFloat() {
    assertEquals(-1.0f, new FloatReturn().getFloat());
   }
 
   public void testDouble() {
    assertEquals(-1.0, new FloatReturn().getDouble());
   }
 
   public static Test suite() {
     return new TestSuite(FloatReturnTest.class);
   }
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
   }
 }
