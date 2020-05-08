 package jumble.ui;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 
 import jumble.fast.MutationResult;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
/**
 * Tests the corresponding class.
 * 
 * @author Tin Pavlinic
 * @version $Revision 1.0 $
 */
 public class PrinterListenerTest extends TestCase {
   public void testLineWrapping() throws Exception {
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     PrinterListener listener = new PrinterListener(new PrintStream(baos));
     StringBuffer expected = new StringBuffer();
 
     for (int i = 0; i < 50; i++) {
       expected.append(".");
       listener.finishedMutation(new MutationResult(MutationResult.PASS, "dummyClass", i, "dummy description"));
     }
     expected.append(System.getProperty("line.separator"));
     for (int i = 0; i < 25; i++) {
       expected.append("T");
       listener.finishedMutation(new MutationResult(MutationResult.TIMEOUT, "dummyClass", i + 50, "dummy description"));
     }
 
     assertEquals(baos.toString(), expected.toString());
   }
 
   public static Test suite() {
     return new TestSuite(PrinterListenerTest.class);
   }
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
   }
 }
