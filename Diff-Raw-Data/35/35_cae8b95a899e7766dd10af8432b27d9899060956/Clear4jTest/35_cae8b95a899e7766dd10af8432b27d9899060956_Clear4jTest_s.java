 package clear4j;
 
 import clear4j.beans.Function;
 import clear4j.beans.Workflow;
 import clear4j.config.Functions;
 import clear4j.config.Workflows;
 import clear4j.processors.FileUtils;
 import junit.framework.Assert;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 
 /**
  * User: alexb
  * Date: 24/05/12
  * Time: 09:23
  */
 public class Clear4jTest {
 
     @BeforeClass
     public static void setup(){
         FileUtils.writeTextToFile(TestConfig.TEST_FILE_PATH.getValue(), TestConfig.TEST_FILE_CONTENT.getValue());
     }
 
     @AfterClass
     public static void tearDown(){
         FileUtils.removeFile(TestConfig.TEST_FILE_PATH.getValue());
     }
 
     protected Function[] getFunctions(){
         return new Function[]{ 
     		Functions.loadText(), Function.withArg(Functions.println(), "test-key", "test-value")
         };
     }
 
     @Test
     public void testMonitorFunction(){
         Clear.run(Workflows.getMonitorWorkflow()).waitFor();
     }
     
     @Test
     public void testCheckedException() {
     	Clear.run(new Workflow(Functions.throwCheckedException())).waitFor();
     }
     
     @Test
     public void testRuntimeException() {
     	Clear.run(new Workflow(Functions.throwRuntimeException())).waitFor();
     }
     
     @Test
     public void testPrintlnNull() {
     	Assert.assertNull(Clear.run(new Workflow(Functions.println())).waitFor());
     }
 
     @Test
     public void testSimpleWorkFlow() throws Exception {
         // start the workflow process
         String filePath = TestConfig.TEST_FILE_PATH.getValue();
         Workflow workflow = new Workflow(filePath, getFunctions());
         Clear.run(workflow);
 
         // wait for the result
         String text1 = workflow.waitFor();
 
        // load it the boring way
        String text2 = FileUtils.loadTextFromFile(TestConfig.TEST_FILE_PATH.getValue());
 
        // assert same content
         Assert.assertEquals(text2, text1);
     	
     }
   
 }
