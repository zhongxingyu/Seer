 package hudson.plugins.sctmexecutor;
 
 import static org.junit.Assert.assertTrue;
 import hudson.plugins.sctmexecutor.exceptions.SCTMException;
 import hudson.plugins.sctmexecutor.service.ISCTMService;
 
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 import com.borland.sctm.ws.execution.entities.ExecutionHandle;
 import com.borland.sctm.ws.execution.entities.ExecutionResult;
 import com.borland.sctm.ws.execution.entities.TestDefinitionResult;
 
 public class TestResultCollector {
   
   private static final int DEFAULT_BUILDNUMBER = -1;
 
   @Test
   public void collectingResults() throws SCTMException, FileNotFoundException {
     ISCTMService serviceMock = createServiceMock();
     ExecutionResult result = createDummyResult();
     EasyMock.expect(serviceMock.getExecutionResult((ExecutionHandle)EasyMock.notNull())).andReturn(result);
     
     ITestResultWriter resultWriterMock = EasyMock.createStrictMock(ITestResultWriter.class);
     resultWriterMock.write(result);
     
     ExecutionRunnable aut = new ExecutionRunnable(serviceMock, 1, DEFAULT_BUILDNUMBER, resultWriterMock, new PrintStream("test.log"));
     aut.setResultCollectingDelay(1);
     
     EasyMock.replay(serviceMock);
     EasyMock.replay(resultWriterMock);
     aut.run();
     EasyMock.verify(serviceMock);
     EasyMock.verify(resultWriterMock);
   }
   
   @Test
   public void runCollectorThread() throws SCTMException, FileNotFoundException {
     ISCTMService serviceMock = EasyMock.createStrictMock(ISCTMService.class);
     Collection<ExecutionHandle> handles = new ArrayList<ExecutionHandle>();
     handles.add(new ExecutionHandle(1, System.currentTimeMillis()));
     EasyMock.expect(serviceMock.buildNumberExists(1234, 1)).andReturn(false);
     EasyMock.expect(serviceMock.addBuildNumber(1234, 1)).andReturn(true);
     EasyMock.expect(serviceMock.start(EasyMock.eq(1), EasyMock.cmpEq("1234"))).andReturn(handles);
     EasyMock.expect(serviceMock.isFinished((ExecutionHandle) EasyMock.notNull())).andReturn(false);
     EasyMock.expectLastCall().times(2).andReturn(false);
     EasyMock.expectLastCall().andReturn(true);
     ExecutionResult result = createDummyResult();
     EasyMock.expect(serviceMock.getExecutionResult((ExecutionHandle)EasyMock.notNull())).andReturn(result);
     
     ITestResultWriter resultWriterMock = EasyMock.createStrictMock(ITestResultWriter.class);
     resultWriterMock.write(result);
     
     ExecutionRunnable aut = new ExecutionRunnable(serviceMock, 1, 1234, resultWriterMock, new PrintStream("test.log"));
     aut.setResultCollectingDelay(1);
     
     EasyMock.replay(serviceMock);
     EasyMock.replay(resultWriterMock);
     long start = System.currentTimeMillis();
     aut.run();
     long duration = (System.currentTimeMillis()-start)/1000;
     assertTrue(duration > 8 && duration < 16);
     EasyMock.verify(serviceMock);
     EasyMock.verify(resultWriterMock);
   }
   
   @Test(expected=RuntimeException.class)
   public void testBadRun() throws Exception {
     ISCTMService serviceMock = EasyMock.createStrictMock(ISCTMService.class);
     EasyMock.expect(serviceMock.isFinished((ExecutionHandle) EasyMock.notNull())).andThrow(new RemoteException());
     
     ExecutionRunnable aut = new ExecutionRunnable(serviceMock, 1, DEFAULT_BUILDNUMBER, null, new PrintStream("test.log"));
     aut.setResultCollectingDelay(1);
     
     EasyMock.replay(serviceMock);
     aut.run();
     EasyMock.verify(serviceMock);
   }
   
   @Test(expected=RuntimeException.class)
   public void testBadResult() throws Exception {
     ISCTMService serviceMock = createServiceMock();
     EasyMock.expect(serviceMock.getExecutionResult((ExecutionHandle)EasyMock.notNull())).andThrow(new RemoteException());
     EasyMock.expect(serviceMock.isFinished((ExecutionHandle) EasyMock.notNull())).andReturn(true);
     EasyMock.expect(serviceMock.getExecutionResult((ExecutionHandle)EasyMock.notNull())).andThrow(new RemoteException());
     EasyMock.expect(serviceMock.isFinished((ExecutionHandle) EasyMock.notNull())).andReturn(true);
     EasyMock.expect(serviceMock.getExecutionResult((ExecutionHandle)EasyMock.notNull())).andThrow(new RemoteException());
     
     ExecutionRunnable aut = new ExecutionRunnable(serviceMock, 1, DEFAULT_BUILDNUMBER, null, new PrintStream("test.log"));
     aut.setResultCollectingDelay(1);
     
     EasyMock.replay(serviceMock);
     aut.run();
     EasyMock.verify(serviceMock);
   }
   
   private ExecutionResult createDummyResult() {
     TestDefinitionResult cleanUp = new TestDefinitionResult(1, 0, 1, "test", "", 1, 1, 1, 1, 0);
     TestDefinitionResult setup = new TestDefinitionResult(1, 0, 1, "test", "", 1, 1, 1, 1, 0);
     TestDefinitionResult[] results = new TestDefinitionResult[] {
         new TestDefinitionResult(1, 0, 1, "test", "", 1, 1, 1, 1, 0),
         new TestDefinitionResult(1, 0, 1, "test", "", 1, 1, 1, 1, 0)
     };
     ExecutionResult result = new ExecutionResult("test", cleanUp, 10, "testExecDef", "unknown", new String[] {"unknown"}, "", setup, results);
     return result;
   }
 
   private ISCTMService createServiceMock() throws SCTMException {
     ISCTMService serviceMock = EasyMock.createStrictMock(ISCTMService.class);
     Collection<ExecutionHandle> handles = new ArrayList<ExecutionHandle>();
     handles.add(new ExecutionHandle(1, System.currentTimeMillis()));
     
 //    EasyMock.expect(serviceMock.buildNumberExists(EasyMock.eq(DEFAULT_BUILDNUMBER))).andReturn(false);
 //    serviceMock.addBuildNumber(EasyMock.eq(DEFAULT_BUILDNUMBER));
     EasyMock.expect(serviceMock.start(EasyMock.gt(0))).andReturn(handles );
     EasyMock.expect(serviceMock.isFinished((ExecutionHandle) EasyMock.notNull())).andReturn(false);
     EasyMock.expectLastCall().andReturn(true);
     return serviceMock;
   }
 
   private ITestResultWriter createTestResultWriterMock(ExecutionResult result) {
     ITestResultWriter resultWriterMock = EasyMock.createStrictMock(ITestResultWriter.class);
     resultWriterMock.write(result);
     return resultWriterMock;
   }
 
 }
