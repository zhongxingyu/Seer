 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 22nd October 2009
  *
  * Changelog:
  * - 22/10/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.rig.tests;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.reset;
 import static org.easymock.EasyMock.verify;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.BatchResults;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.BatchState;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveRequest;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveResponse;
 import au.edu.uts.eng.remotelabs.rigclient.rig.control.AbstractBatchRunner;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 
 /**
  * Tests the <code>AbstractControllerRigTester</code> class.
  */
 public class AbstractControlledRigTester extends TestCase
 {
     /** Object of class under test. */
     private MockControlledRig rig;
     
     /** Mock configuration. */
     private IConfig mockConfig;
     /**
      * @throws java.lang.Exception
      */
     @Override
     @Before
     public void setUp() throws Exception
     {
         this.mockConfig = createMock(IConfig.class);
         expect(this.mockConfig.getProperty("Package_Prefixes"))
             .andReturn("");
         expect(this.mockConfig.getProperty("Logger_Type"))
             .andReturn("SystemErr");
         expect(this.mockConfig.getProperty("Log_Level"))
             .andReturn("INFO");
         expect(this.mockConfig.getProperty("Action_Failure_Threshold"))
             .andReturn("2");
         expect(this.mockConfig.getProperty("Default_Log_Format", "[__LEVEL__] - [__ISO8601__] - __MESSAGE__"))
             .andReturn("[__LEVEL__] - [__ISO8601__] - __MESSAGE__");
         expect(this.mockConfig.getProperty("FATAL_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("PRIORITY_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("ERROR_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("WARN_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("INFO_Log_Format")).andReturn(null);
         expect(this.mockConfig.getProperty("DEBUG_Log_Format")).andReturn(null);
         replay(this.mockConfig);
 
         Field configField = ConfigFactory.class.getDeclaredField("instance");
         configField.setAccessible(true);
         configField.set(null, this.mockConfig);
 
         this.rig = new MockControlledRig();
         
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig#performPrimitive(au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveRequest)}.
      */
     @Test
     public void testPerformPrimitive()
     {
         PrimitiveRequest request = new PrimitiveRequest();
         request.setController("au.edu.uts.eng.remotelabs.rigclient.rig.primitive.tests.MockController");
         request.setAction("test");
         request.addParameter("param1", "val1");
         request.addParameter("param2", "val2");
         request.addParameter("param3", "val3");
         
         PrimitiveResponse resp = this.rig.performPrimitive(request);
         assertNotNull(resp);
         assertTrue(resp.wasSuccessful());
         assertEquals(0, resp.getErrorCode());
         assertNull(resp.getErrorReason());
         
         Map<String, String> res = resp.getResults();
         assertTrue(res.containsKey("param1"));
         assertEquals("val1", res.get("param1"));
         assertTrue(res.containsKey("param1"));
         assertEquals("val2", res.get("param2"));
         assertTrue(res.containsKey("param1"));
         assertEquals("val3", res.get("param3"));
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig#expungePrimitiveControllerCache()}.
      */
     @Test
     public void testExpungePrimitiveControllerCache()
     {
         PrimitiveRequest request = new PrimitiveRequest();
         request.setController("au.edu.uts.eng.remotelabs.rigclient.rig.primitive.tests.MockController");
         request.setAction("callCount");
 
         PrimitiveResponse resp = this.rig.performPrimitive(request);
         assertNotNull(resp);
         assertTrue(resp.wasSuccessful());
         assertEquals(0, resp.getErrorCode());
         assertNull(resp.getErrorReason());
         assertEquals(1, Integer.parseInt(resp.getResult("count")));
         
         resp = this.rig.performPrimitive(request);
         assertNotNull(resp);
         assertTrue(resp.wasSuccessful());
         assertEquals(0, resp.getErrorCode());
         assertNull(resp.getErrorReason());
         assertEquals(2, Integer.parseInt(resp.getResult("count")));
         
         resp = this.rig.performPrimitive(request);
         assertNotNull(resp);
         assertTrue(resp.wasSuccessful());
         assertEquals(0, resp.getErrorCode());
         assertNull(resp.getErrorReason());
         assertEquals(3, Integer.parseInt(resp.getResult("count")));
         
         this.rig.expungePrimitiveControllerCache();
         
         resp = this.rig.performPrimitive(request);
         assertNotNull(resp);
         assertTrue(resp.wasSuccessful());
         assertEquals(0, resp.getErrorCode());
         assertNull(resp.getErrorReason());
         assertEquals(1, Integer.parseInt(resp.getResult("count")));
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig#performBatch(java.lang.String)}.
      */
     @Test
     public void testPerformBatch()
     {
         final String instructions = System.getProperty("user.dir") + "/test/resources/Control/instructions.txt";
         try
         {
             /* Set up AbstractRig. */
             String wdBase = System.getProperty("user.dir") + "/test/resources/Control/WorkDirBase";
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Working_Dir"))
                 .andReturn(wdBase);
             expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Timeout", "60"))
                 .andReturn("180");
             replay(this.mockConfig);
             
             /* Set up batch runner. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 // TODO Write a bat script to run the test case
                 fail("Windows batch command script implmenation");
             }
             else
             {
                 this.rig.setComm(System.getProperty("user.dir") + "/test/resources/Control/slow-cat.sh");
             }
             List<String> args = new ArrayList<String>();
             args.add(instructions);
             args.add("10");
             args.add("stdout");
             this.rig.setArgs(args);
             this.rig.setEnv(new HashMap<String, String>());
            
             /* Pre-batch state. */
             assertTrue(this.rig.getBatchState() == BatchState.CLEAR);
             assertFalse(this.rig.isBatchRunning());
             assertEquals(0, this.rig.getBatchProgress());
             BatchResults results = this.rig.getBatchResults();
             assertEquals(BatchState.CLEAR, results.getState());
             
             /* Run the batch process. */
             assertTrue(this.rig.performBatch(instructions, "tmachet"));
             assertTrue(this.rig.isBatchRunning());
             assertTrue(this.rig.getBatchState() == BatchState.IN_PROGRESS);
             
             int progress = -1;
             int oldProgress = -1;
             while (this.rig.isBatchRunning())
             {
                 Thread.sleep(30000);
                 oldProgress = progress;
                 progress = this.rig.getBatchProgress();
                 assertTrue(progress > oldProgress); // This should progress
                 assertTrue(progress > 0 && progress <= 100);
             }
             
             assertFalse(this.rig.isBatchRunning());
             assertEquals(100, this.rig.getBatchProgress());
             assertTrue(BatchState.COMPLETE == this.rig.getBatchState());
             results = this.rig.getBatchResults();
             assertNotNull(results);
             assertTrue(BatchState.COMPLETE == results.getState());
             assertEquals(instructions, results.getInstructionFile());
             
             String stdout = results.getStandardOut();
             assertNotNull(stdout);
             String lines[] = stdout.split("\n");
             assertEquals(40, lines.length);
             assertEquals("Ignorance is Strength", lines[0].substring(2).trim()); // !!!
             
             assertEquals(0, results.getExitCode());
             
             verify(this.mockConfig);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getName() + ", message: " + e.getMessage() + ".");
         }
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig#isBatchRunning()}.
      */
     @Test
     public void testIsBatchRunning()
     {
         final String instructions = System.getProperty("user.dir") + 
                 "/test/resources/Control/instructions.txt";
         try
         {
             /* Set up AbstractRig. */
             String wdBase = System.getProperty("user.dir") + "/test/resources/Control/WorkDirBase";
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Working_Dir"))
                 .andReturn(wdBase);
             expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Timeout", "60"))
                 .andReturn("30");
             replay(this.mockConfig);
             
             /* Set up batch runner. */
             /* Set up batch runner. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 // TODO Write a bat script to run the test case
                 fail("Windows batch command script implmenation");
             }
             else
             {
                 this.rig.setComm(System.getProperty("user.dir") + "/test/resources/Control/slow-cat.sh");
             }
             List<String> args = new ArrayList<String>();
             args.add(instructions);
             args.add("1");
             args.add("stdout");
             this.rig.setArgs(args);
             this.rig.setEnv(new HashMap<String, String>());
             
             /* Run the batch process. */
             assertFalse(this.rig.isBatchRunning());
             assertTrue(this.rig.performBatch(instructions, "tmachet"));
             
             Field field = AbstractControlledRig.class.getDeclaredField("runner");
             field.setAccessible(true);
             AbstractBatchRunner runner = (AbstractBatchRunner)field.get(this.rig);
             field = AbstractBatchRunner.class.getDeclaredField("batchProc");
             field.setAccessible(true);
             
             assertTrue(this.rig.isBatchRunning());
             Process proc = (Process)field.get(runner);
             proc.waitFor();
             Thread.sleep(1000); /* Time for clean up and such to run. */
             assertFalse(this.rig.isBatchRunning());
 
             verify(this.mockConfig);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getName() + ", message: " + e.getMessage() + ".");
         }
     }
     
     /**
      * Tests the <code>AbstractRig.revoke</code> method.
      */
     @Test
     public void testRevokeAbortBatch()
     {
        final String instructions = System.getProperty("user.dir") + 
                 "/test/resources/Control/instructions.txt";
         try
         {
             /* Set up AbstractRig. */
             String wdBase = System.getProperty("user.dir") + "/test/resources/Control/WorkDirBase";
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Working_Dir"))
                 .andReturn(wdBase);
             expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Timeout", "60"))
                 .andReturn("30");
             expect(this.mockConfig.getProperty("Batch_Termination_TimeOut", "10"))
                 .andReturn("10");
             replay(this.mockConfig);
             
             assertTrue(this.rig.assign("mdiponio"));
             /* Set up batch runner. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 // TODO Write a bat script to run the test case
                 fail("Windows batch command script implmenation");
             }
             else
             {
                 this.rig.setComm(System.getProperty("user.dir") + "/test/resources/Control/slow-cat.sh");
             }
             List<String> args = new ArrayList<String>();
             args.add(instructions);
             args.add("10");
             args.add("stdout");
             this.rig.setArgs(args);
             this.rig.setEnv(new HashMap<String, String>());
             
             /* Run the batch process. */
             assertFalse(this.rig.isBatchRunning());
             assertTrue(this.rig.performBatch(instructions, "tmachet"));
             
             assertTrue(this.rig.isBatchRunning());
             Thread.sleep(1000);
             
             assertTrue(this.rig.revoke());
             Thread.sleep(1000);
             assertFalse(this.rig.isBatchRunning());
 
             verify(this.mockConfig);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getName() + ", message: " + e.getMessage() + ".");
         }
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig#abortBatch()}.
      */
     @Test
     public void testAbortBatch()
     {
         final String instructions = System.getProperty("user.dir") + 
                 "/test/resources/Control/instructions.txt";
         try
         {
             /* Set up AbstractRig. */
             String wdBase = System.getProperty("user.dir") + "/test/resources/Control/WorkDirBase";
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Working_Dir"))
                 .andReturn(wdBase);
             expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Timeout", "60"))
                 .andReturn("180");
             expect(this.mockConfig.getProperty("Batch_Termination_TimeOut", "10"))
                 .andReturn("10");
             replay(this.mockConfig);
             
             /* Set up batch runner. */
             if (System.getProperty("os.name").equals("Windows"))
             {
                 // TODO Write a bat script to run the test case
                 fail("Windows batch command script implmenation");
             }
             else
             {
                 this.rig.setComm(System.getProperty("user.dir") + "/test/resources/Control/slow-cat.sh");
             }
             List<String> args = new ArrayList<String>();
             args.add(instructions);
             args.add("5"); // This should take *quite* a while 
             args.add("stderr");
             this.rig.setArgs(args);
             this.rig.setEnv(new HashMap<String, String>());
            
             /* Run batch process. */
             assertFalse(this.rig.isBatchRunning());
             assertTrue(this.rig.performBatch(instructions, "tmachet"));
             assertTrue(this.rig.isBatchRunning());
             
             /* Abort batch. */
             Thread.sleep(10000); // Time for the process to generate should output.
             assertTrue(this.rig.abortBatch());
             Thread.sleep(1000); // Some time for cleanup
             assertFalse(this.rig.isBatchRunning());
             assertEquals(100, this.rig.getBatchProgress());
             assertEquals(BatchState.ABORTED, this.rig.getBatchState());
             
             BatchResults res = this.rig.getBatchResults();
             assertEquals(BatchState.ABORTED, res.getState());
             
             int signum = res.getExitCode() - 128; // Shell gives the exit code of 128 + signal number as the exit
                                                   // code for untrapped signals
             if (System.getProperty("os.name").equals("Windows"))
             {
                 // TODO find out what a JVM uses on Windows
                 fail("Windows error code checking not implemented");
             }
             else
             {
                 assertEquals(15, signum); // SIGTERM is signal 15
             }
            
             verify(this.mockConfig);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getName() + ", message: " + e.getMessage() + ".");
         }
     }
 
     
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig#getBatchProgress()}.
      */
     @Test
     public void testGetBatchExitCode()
     {
         final String instructions = System.getProperty("user.dir") + 
                 "/test/resources/Control/instructions.txt";
         try
         {
             /* Set up AbstractRig. */
             String wdBase = System.getProperty("user.dir") + "/test/resources/Control/WorkDirBase";
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Working_Dir"))
                 .andReturn(wdBase);
             expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Timeout", "60"))
                 .andReturn("180");
             replay(this.mockConfig);
             
             if (System.getProperty("os.name").equals("Windows"))
             {
                 // TODO Write a bat script to run the test case
                 fail("Windows batch command script implmenation");
             }
             else
             {
                 this.rig.setComm(System.getProperty("user.dir") + "/test/resources/Control/slow-cat.sh");
             }
             
             /* Only two arguments is an error, will return an exit code of 10. */
             List<String> args = new ArrayList<String>();
             args.add(instructions);
             args.add("stdout");
             this.rig.setArgs(args);
             this.rig.setEnv(new HashMap<String, String>());
            
             /* Pre-batch state. */
             assertTrue(this.rig.getBatchState() == BatchState.CLEAR);   
             assertFalse(this.rig.isBatchRunning());
             
             /* Run the batch process. */
             assertTrue(this.rig.performBatch(instructions, "tmachet"));
             Thread.sleep(2000); // Should be enough time for the batch process to exit
             
             assertFalse(this.rig.isBatchRunning());
             assertEquals(BatchState.COMPLETE, this.rig.getBatchState());
             BatchResults res = this.rig.getBatchResults();
             assertEquals(10, res.getExitCode());
             assertEquals(BatchState.COMPLETE, res.getState());
             assertEquals("Incorrect number of arguments, should be <file reference> <sleep time> [stdout|stderr]", 
                     res.getStandardOut().trim());
             
             verify(this.mockConfig);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getName() + ", message: " + e.getMessage() + ".");
         }
     }
 
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig#getBatchResults()}.
      */
     @Test
     public void testGetBatchResults()
     {
         final String instructions = System.getProperty("user.dir") + 
                 "/test/resources/Control/instructions.txt";
         try
         {
             /* Set up AbstractRig. */
             String wdBase = System.getProperty("user.dir") + "/test/resources/Control/WorkDirBase";
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Working_Dir"))
                 .andReturn(wdBase);
             expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
                 .andReturn("true"); // Create a batch nested directory
             expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("true");
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Timeout", "60"))
                 .andReturn("180");
             replay(this.mockConfig);
             
             /* Set up batch runner. */
             this.rig.setComm(System.getProperty("user.dir") + "/test/resources/Control/slow-cat.sh");
             List<String> args = new ArrayList<String>();
             args.add(instructions);
             args.add("1");
             args.add("stderr");
             args.add("output"); // Output a file to the process working directory
             this.rig.setArgs(args);
             this.rig.setEnv(new HashMap<String, String>());
            
             /* Run the batch process. */
             assertTrue(this.rig.performBatch(instructions, "tmachet"));
             assertTrue(this.rig.isBatchRunning());
             assertTrue(this.rig.getBatchState() == BatchState.IN_PROGRESS);
             
             while (this.rig.isBatchRunning())
             {
                 Thread.sleep(3000);
             }
             
             assertFalse(this.rig.isBatchRunning());
             assertEquals(100, this.rig.getBatchProgress());
             assertTrue(BatchState.COMPLETE == this.rig.getBatchState());
             BatchResults res = this.rig.getBatchResults();
             assertEquals(0, res.getExitCode());
             assertEquals(BatchState.COMPLETE, res.getState());
             
             assertNotNull(res.getStandardOut());
             assertTrue(res.getStandardOut().length() > 10);
             assertNotNull(res.getStandardErr());
             assertTrue(res.getStandardErr().length() > 10);
             
             String resFiles[] = res.getResultsFiles();
             assertEquals(1, resFiles.length);
             assertTrue(resFiles[0].startsWith("results-"));
             assertTrue(resFiles[0].endsWith(".txt"));
             
             verify(this.mockConfig);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getName() + ", message: " + e.getMessage() + ".");
         }
     }
     
     /**
      * Test method for {@link au.edu.uts.eng.remotelabs.rigclient.rig.AbstractControlledRig#getBatchResults()}.
      */
     @Test
     public void testBatchFailed()
     {
         final String instructions = System.getProperty("user.dir") + 
         "/test/resources/Control/instructions.txt";
         try
         {
             /* Set up AbstractRig. */
             String wdBase = System.getProperty("user.dir") + "/test/resources/Control/WorkDirBase";
             reset(this.mockConfig);
             expect(this.mockConfig.getProperty("Batch_Working_Dir"))
                 .andReturn(wdBase);
             expect(this.mockConfig.getProperty("Batch_Create_Nested_Dir", "true"))
                 .andReturn("true"); // Create a batch nested directory
             expect(this.mockConfig.getProperty("Batch_Flush_Env", "false"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Clean_Up", "false"))
                 .andReturn("true");
             expect(this.mockConfig.getProperty("Batch_Instruct_File_Delete", "true"))
                 .andReturn("false");
             expect(this.mockConfig.getProperty("Batch_Timeout", "60"))
                 .andReturn("100");
             replay(this.mockConfig);
 
             /* Set up batch runner. */
             this.rig.setComm("DOES_NOT_EXIST");
 
             /* Run the batch process. */
             assertTrue(this.rig.performBatch(instructions, "tmachet"));
             assertEquals(BatchState.FAILED, this.rig.getBatchState());
 
             verify(this.mockConfig);
         }
         catch (Exception e)
         {
             fail("Exception: " + e.getClass().getName() + ", message: " + e.getMessage() + ".");
         }
     }
 }
