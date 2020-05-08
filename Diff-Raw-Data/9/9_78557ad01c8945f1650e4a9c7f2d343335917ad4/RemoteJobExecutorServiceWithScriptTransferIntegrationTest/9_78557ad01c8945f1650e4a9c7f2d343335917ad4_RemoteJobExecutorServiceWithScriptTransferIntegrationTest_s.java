 package de.otto.jobstore.service;
 
 import de.otto.jobstore.common.RemoteJob;
 import de.otto.jobstore.common.RemoteJobStatus;
 import de.otto.jobstore.service.exception.JobException;
 import de.otto.jobstore.service.exception.RemoteJobAlreadyRunningException;
 import de.otto.jobstore.service.exception.RemoteJobNotRunningException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.testng.annotations.Test;
 
 import javax.annotation.Resource;
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import static org.testng.AssertJUnit.assertEquals;
 import static org.testng.AssertJUnit.assertNotNull;
 import static org.testng.AssertJUnit.assertTrue;
 import static org.testng.AssertJUnit.fail;
 
 @ContextConfiguration(locations = {"classpath:spring/jobs-context.xml"})
 public class RemoteJobExecutorServiceWithScriptTransferIntegrationTest extends AbstractTestNGSpringContextTests {
 
     private static final String JOB_NAME = "jobname";
 
     @Resource
     private RemoteJobExecutorWithScriptTransferService remoteJobExecutorService;
 
     @Test(enabled = false)
     public void testStartingDemoJob() throws Exception {
 
         URI uri = remoteJobExecutorService.startJob(createRemoteJob());
         assertNotNull(uri);
         assertTrue("Expected valid job uri", uri.getPath().startsWith("/jobs/jobname/"));
 
         remoteJobExecutorService.stopJob(uri);
     }
 
     @Test(enabled = false, expectedExceptions = RemoteJobAlreadyRunningException.class)
     public void testStartingDemoJobWhichIsAlreadyRunning() throws Exception {
         URI uri = null;
         try {
             uri = remoteJobExecutorService.startJob(createRemoteJob());
         } catch (JobException e) {
             fail("No exception expected when trying to start job");
         }
         assert uri != null;
         assertTrue("Expected valid job uri", uri.getPath().startsWith("/jobs/jobname/"));
 
         try {
             remoteJobExecutorService.startJob(createRemoteJob());
         } finally {
             remoteJobExecutorService.stopJob(uri);
         }
     }
 
     @Test(enabled = false, expectedExceptions = RemoteJobNotRunningException.class)
     public void testStoppingJobTwice() throws Exception {
         URI uri = remoteJobExecutorService.startJob(createRemoteJob());
         remoteJobExecutorService.stopJob(uri);
         remoteJobExecutorService.stopJob(uri);
     }
 
     @Test(enabled = false, expectedExceptions = RemoteJobNotRunningException.class)
     public void testStoppingNotExistingJob() throws Exception {
         remoteJobExecutorService.stopJob(URI.create("http://localhost:5000/jobs/" + JOB_NAME + "/12345")); // TODO: configure URL
     }
 
 
     class GetRequest implements Runnable {
 
         private URI uri;
 
         public GetRequest(URI uri) {
             this.uri = uri;
         }
 
         @Override
         public void run() {
             RemoteJobStatus status = remoteJobExecutorService.getStatus(uri);
             assertNotNull(status);
             assertEquals(RemoteJobStatus.Status.RUNNING, status.status);
         }
     }
 
     @Test(enabled = false)
     public void testGettingStatusOfRunningJob() throws Exception {
         final URI uri = remoteJobExecutorService.startJob(createRemoteJob());
 
         ExecutorService exec = Executors.newFixedThreadPool(8);
         for (int i = 0; i < 100; i++) {
             exec.submit(new GetRequest(uri));
         }
         exec.shutdown();
         exec.awaitTermination(10, TimeUnit.SECONDS);
 
         remoteJobExecutorService.stopJob(uri);
 
         //assertNull(status.result);
     }
 
     @Test(enabled = false)
     public void testGettingStatusOfFinishedJob() throws Exception {
         URI uri = remoteJobExecutorService.startJob(createRemoteJob());
         remoteJobExecutorService.stopJob(uri);
         RemoteJobStatus status = remoteJobExecutorService.getStatus(uri);
 
         assertNotNull(status);
         assertEquals(RemoteJobStatus.Status.FINISHED, status.status);
         assertNotNull(status.result);
         assertTrue(status.result.ok);
     }
 
     private RemoteJob createRemoteJob() {
         Map<String, String> params = new HashMap<String, String>();
         params.put("sample_file", "/var/log/mongodb/mongodb.log");
         return new RemoteJob(JOB_NAME, "2311", params);
     }
 
 }
