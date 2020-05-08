 /**
  * 
  */
 package org.purl.wf4ever.wf2ro.rest;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletResponse;
 
 import junit.framework.Assert;
 
 import org.apache.http.HttpStatus;
 import org.junit.After;
 import org.junit.Test;
 import org.purl.wf4ever.rosrs.client.common.ROSRSException;
 import org.purl.wf4ever.rosrs.client.common.ROSRService;
 import org.purl.wf4ever.wf2ro.rest.Job.State;
 
 import uk.org.taverna.scufl2.translator.t2flow.T2FlowReader;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.representation.Form;
 import com.sun.jersey.test.framework.JerseyTest;
 
 /**
  * This test verifies the correctness of the REST API. It creates 2 jobs, one of which is cancelled. The other job is
  * expected to finish within predefined time, e.g. 120 seconds.
  * 
  * This test can only be run as a maven run (goal=test) and requires run parameters, as described in
  * http://jersey.java.net/nonav/documentation/latest/test-framework.html
  * 
  * To run the test using the embedded Grizzly 2 server, launched at run time, set: jersey.test.containerFactory =
  * com.sun.jersey.test.framework.spi.container.grizzly2.GrizzlyTestContainerFactory jersey.test.port = 8080 (or other)
  * 
  * @author piotrekhol
  * 
  */
 public class RestApiTest extends JerseyTest {
 
     /** an example workflow from myExperiment. */
     private static final URI WF_URI = URI.create("http://www.myexperiment.org/workflows/2648/download?version=1");
 
     /** workflow format MIME type. */
     private static final String TAVERNA_FORMAT = T2FlowReader.APPLICATION_VND_TAVERNA_T2FLOW_XML;
 
     /** RODL URI. */
     private static final URI RODL_URI = URI.create("http://sandbox.wf4ever-project.org/rodl/");
 
     /** RO URI, with a random UUID as ro id. */
     private static final URI RO_URI = RODL_URI.resolve("ROs/" + UUID.randomUUID().toString() + "/");
 
     /** RO URI, with a random UUID as ro id. */
     private static URI ro2Uri;
 
     /** RODL access token, currently assigned to Wf4Ever Test User. */
     private static final String TOKEN = "32801fc0-1df1-4e34-b";
 
     /**
      * Maximum time that the test waits for a job to finish. After that the test fails.
      */
     private static final long MAX_JOB_TIME_S = 480;
 
 
     @After
     @Override
     public void tearDown()
            throws Exception {
         ROSRService rosrs = new ROSRService(RODL_URI, TOKEN);
         rosrs.deleteResearchObject(RO_URI);
         if (ro2Uri != null) {
             rosrs.deleteResearchObject(ro2Uri);
         }
        super.tearDown();
     }
 
 
     /**
      * Constructor.
      */
     public RestApiTest() {
         super("org.purl.wf4ever.wf2ro.rest");
     }
 
 
     /**
      * Create a job and for it to finish.
      * 
      * @throws InterruptedException
      *             interrupted while waiting for a job to finish
      */
     @Test
     public void testCreateAndWait()
             throws InterruptedException {
         WebResource webResource;
         if (resource().getURI().getHost().equals("localhost")) {
             webResource = resource();
         } else {
             webResource = resource().path("wf-ro/");
         }
 
         Form f = new Form();
         f.add("resource", WF_URI);
         f.add("format", TAVERNA_FORMAT);
         f.add("ro", RO_URI);
         f.add("token", TOKEN);
 
         //        JobConfig config = new JobConfig(WF_URI, TAVERNA_FORMAT, RO2_URI, TOKEN.getToken());
 
         ClientResponse response = webResource.path("jobs").post(ClientResponse.class, f);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         URI jobURI = response.getLocation();
         response.close();
 
         JobStatus status = null;
 
         for (int i = 0; i < MAX_JOB_TIME_S; i++) {
             System.out.print(".");
             status = webResource.uri(jobURI).get(JobStatus.class);
             assertTrue("Status is: " + status.getStatus().toString(),
                 status.getStatus() == State.RUNNING || status.getStatus() == State.DONE);
             assertEquals(WF_URI, status.getResource());
             assertEquals(TAVERNA_FORMAT, status.getFormat());
             assertEquals(RO_URI, status.getRo());
             if (status.getStatus() == State.DONE) {
                 System.out.println();
                 break;
             }
             Thread.sleep(1000);
         }
         System.out.println(webResource.uri(jobURI).get(String.class));
         if (status.getStatus() == State.RUNNING) {
             fail("The job hasn't finished on time");
         }
         assertNotNull(status.getAdded());
         // this workflow has 3 inner annotations, plus roevo & wfdesc & link, plus the workflow itself, plus 16 folders = 22
         Assert.assertEquals(23, status.getAdded().size());
     }
 
 
     /**
      * Work on an existing RO with a workflow. The conversion should be successful and the wf should be deleted.
      * 
      * @throws ROSRSException
      *             error creating the ro
      * @throws IOException
      *             error downloading the workflow
      * @throws InterruptedException
      *             interrupted while waiting for a job to finish
      */
     @Test
     public void testExistingRoWithWf()
             throws ROSRSException, IOException, InterruptedException {
         WebResource webResource;
         if (resource().getURI().getHost().equals("localhost")) {
             webResource = resource();
         } else {
             webResource = resource().path("wf-ro/");
         }
 
         ROSRService rosrs = new ROSRService(RODL_URI, TOKEN);
         ro2Uri = rosrs.createResearchObject(UUID.randomUUID().toString()).getLocation();
         Client client = new Client();
         URI wfUri = null;
         try (InputStream wf = client.resource(WF_URI.toString()).get(InputStream.class)) {
             wfUri = rosrs.createResource(ro2Uri, "http://example.org/workflow.t2flow", wf, TAVERNA_FORMAT)
                     .getLocation();
         }
 
         Form f = new Form();
         f.add("resource", wfUri);
         f.add("format", TAVERNA_FORMAT);
         f.add("ro", ro2Uri);
         f.add("token", TOKEN);
 
         ClientResponse response = webResource.path("jobs").post(ClientResponse.class, f);
         assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
         URI jobURI = response.getLocation();
         response.close();
 
         JobStatus status = null;
 
         for (int i = 0; i < MAX_JOB_TIME_S; i++) {
             System.out.print(".");
             status = webResource.uri(jobURI).get(JobStatus.class);
             assertTrue("Status is: " + status.getStatus().toString(),
                 status.getStatus() == State.RUNNING || status.getStatus() == State.DONE);
             assertEquals(wfUri, status.getResource());
             assertEquals(TAVERNA_FORMAT, status.getFormat());
             assertEquals(ro2Uri, status.getRo());
             if (status.getStatus() == State.DONE) {
                 System.out.println();
                 break;
             }
             Thread.sleep(1000);
         }
         System.out.println(webResource.uri(jobURI).get(String.class));
         if (status.getStatus() == State.RUNNING) {
             fail("The job hasn't finished on time");
         }
         assertNotNull(status.getAdded());
         // this workflow has 3 inner annotations, plus roevo & wfdesc & link, plus the workflow itself, plus 16 folders = 22
         Assert.assertEquals(23, status.getAdded().size());
 
         response = client.resource(wfUri).get(ClientResponse.class);
         Assert.assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatus());
         response.close();
     }
 }
