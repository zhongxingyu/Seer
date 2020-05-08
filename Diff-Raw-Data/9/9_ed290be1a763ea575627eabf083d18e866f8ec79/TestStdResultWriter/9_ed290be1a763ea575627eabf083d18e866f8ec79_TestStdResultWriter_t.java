 package hudson.plugins.sctmexecutor;
 
 import static org.junit.Assert.assertEquals;
 import hudson.FilePath;
 
 import java.io.File;
 
import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.borland.sctm.ws.execution.entities.ExecutionResult;
 import com.borland.sctm.ws.execution.entities.TestDefinitionResult;
 
 public class TestStdResultWriter {
   
   private static final String TMP_PATH = System.getenv("tmp")+"/hudsontest";
   private static File root;
   private volatile int i = 0;
 
   @BeforeClass
   public static void init() {
     root = new File(TMP_PATH);  
   }
   
  @After
  public void despose() {
     for (String path : root.list()) {
       new File(root.getAbsolutePath()+"\\"+path).delete();
     }
     root.delete();
   }
 
   @Test
   public void testWrite() {
     StdXMLResultWriter w = new StdXMLResultWriter(new FilePath(root), "http://localhost:19120/Service1.0/services", null, true);
     ExecutionResult result = new ExecutionResult();
     result.setExecDefName("TestExecDef");
     result.setExecServerName("MyVirtualExecSrv");
     result.setSetupTestDef(new TestDefinitionResult(5, 0, 41, "Setup", "http://www.borland.com", 0, 3, 66, 1, 0));
     TestDefinitionResult goodTestDefResult = new TestDefinitionResult(5, 0, 42, "GoodTestDef", "http://www.borland.com", 0, 17, 1234, 1, 0);
     TestDefinitionResult badTestDefResult = new TestDefinitionResult(5, 1, 43, "BadTestDef", "http://www.borland.com", 1, 4, 1235, 1, 0);
     result.setCleanupTestDef(new TestDefinitionResult(5, 0, 44, "Cleanup", "http://borland.com", 0, 21, 99, 1, 0));
     result.setTestDefResult(new TestDefinitionResult[] {goodTestDefResult, badTestDefResult});
     w.write(result);
   }
   
   @Test
   public void testIgnoreSetupCleanup() throws Exception {
     StdXMLResultWriter w = new StdXMLResultWriter(new FilePath(root), "http://localhost:19120/Service1.0/services", null, false);
     ExecutionResult result = new ExecutionResult();
     result.setExecDefName("TestExecDef");
     result.setExecServerName("MyVirtualExecSrv");
     result.setSetupTestDef(new TestDefinitionResult(5, 0, 41, "Setup", "http://www.borland.com", 0, 3, 66, 1, 0));
     TestDefinitionResult goodTestDefResult = new TestDefinitionResult(5, 0, 42, "GoodTestDef", "http://www.borland.com", 0, 17, 1234, 1, 0);
     TestDefinitionResult badTestDefResult = new TestDefinitionResult(5, 1, 43, "BadTestDef", "http://www.borland.com", 1, 4, 1235, 1, 0);
     result.setCleanupTestDef(new TestDefinitionResult(5, 0, 44, "Cleanup", "http://borland.com", 0, 21, 99, 1, 0));
     result.setTestDefResult(new TestDefinitionResult[] {goodTestDefResult, badTestDefResult});
     w.write(result);
   }
   
   @Test
   public void testParallelWrite() throws Exception {
     final StdXMLResultWriter w = new StdXMLResultWriter(new FilePath(root), "http://localhost:19120/Service1.0/services", null, true);
     
     Runnable run = new Runnable() {
       @Override
       public void run() {
         ExecutionResult result = new ExecutionResult();
         result.setExecDefName("thread"+(i++));    
         result.setExecServerName("MyVirtualExecSrv");
         TestDefinitionResult goodTestDefResult = new TestDefinitionResult(5, 0, 42, "GoodTestDef", "http://www.borland.com", 0, 17, 1234, 1, 0);
         TestDefinitionResult badTestDefResult = new TestDefinitionResult(5, 1, 43, "BadTestDef", "http://www.borland.com", 1, 4, 1235, 1, 0);
         result.setTestDefResult(new TestDefinitionResult[] {goodTestDefResult, badTestDefResult});
         w.write(result);
       }
     };
     
     Thread thread1 = new Thread(run);
     Thread thread2 = new Thread(run);
     thread1.start();
     thread2.start();
     
     thread1.join();
     thread2.join();
     
    assertEquals(2, root.list().length);
   }
 }
