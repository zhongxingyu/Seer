 package jumble.util;
 
 import java.util.Properties;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 /**
  * Tests the corresponding class
  * 
  * @author Tin Pavlinic
  * @version $Revision$
  */
 public class JavaRunnerTest extends TestCase {
   Process mProcess = null;
 
   public void tearDown() {
     if (mProcess != null) {
       mProcess.destroy();
       mProcess = null;
     }
   }
 
   public void testStart() throws IOException {
     Properties props = System.getProperties();
     mProcess = new JavaRunner("jumble.util.DisplayEnvironment").start();
     BufferedReader out = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
     BufferedReader err = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
 
     assertEquals("java.home " + props.getProperty("java.home"), out.readLine());
     assertEquals("java.class.path " + props.getProperty("java.class.path"), out.readLine());
 
     if (err.readLine() != null) {
       fail();
     }
   }
 
   public void testArguments() throws Exception {
     mProcess = new JavaRunner("jumble.util.DisplayArguments", new String[] {"one", "two", "three" }).start();
 
     BufferedReader out = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
 
     assertEquals("one", out.readLine());
     assertEquals("two", out.readLine());
     assertEquals("three", out.readLine());
     assertEquals(null, out.readLine());
   }
 
   public void testConstructor() {
     JavaRunner jr = new JavaRunner("jumble.util.DisplayEnvironment");
     String[] args = jr.getArguments();
     assertEquals(0, args.length);
 
     args = jr.getJvmArguments();
     if (JumbleUtils.isAssertionsEnabled()) {
       assertEquals(3, args.length);
    } else {
      assertEquals(2, args.length);
     }
     assertEquals("-cp", args[0]);
     assertEquals(System.getProperty("java.class.path"), args[1]);
   }
 
   public void testSpaces() throws IOException {
     mProcess = new JavaRunner("jumble.util.DisplayArguments", new String[] {"word1 word2", "word3\tword4" }).start();
 
     BufferedReader out = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
 
     assertEquals("word1 word2", out.readLine());
     assertEquals("word3\tword4", out.readLine());
     assertEquals(null, out.readLine());
   }
 
   public void testAssertions() throws IOException {
     String ls = System.getProperty("file.separator");
     String java = System.getProperty("java.home") + ls + "bin" + ls + "java";
 
     Runtime r = Runtime.getRuntime();
     BufferedReader reader;
 
     Process p = r.exec(new String[] {java, "-ea", "-cp", System.getProperty("java.class.path"), "jumble.util.CheckAssertions" });
     reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
     assertEquals("Assertions on", reader.readLine());
     reader.close();
 
     p = r.exec(new String[] {java, "-cp", System.getProperty("java.class.path"), "jumble.util.CheckAssertions" });
     reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
     assertEquals("Assertions off", reader.readLine());
     reader.close();
   }
 
   public static Test suite() {
     TestSuite suite = new TestSuite(JavaRunnerTest.class);
     return suite;
   }
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
   }
 }
