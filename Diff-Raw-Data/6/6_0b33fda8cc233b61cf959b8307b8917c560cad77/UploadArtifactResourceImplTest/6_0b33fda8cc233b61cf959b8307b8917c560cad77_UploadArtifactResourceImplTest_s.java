 /**
  * 
  */
 package com.github.podd.resources.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.StandardCharsets;
 import java.util.Collection;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.ext.html.FormData;
 import org.restlet.ext.html.FormDataSet;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.semanticweb.owlapi.model.OWLException;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.AbstractPoddArtifactManagerTest;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.exception.InconsistentOntologyException;
 import com.github.podd.exception.OntologyNotInProfileException;
 import com.github.podd.exception.PoddException;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author kutila
  * 
  */
 public class UploadArtifactResourceImplTest extends AbstractResourceImplTest
 {
     /**
      * Test Upload attempt with an artifact that is inconsistent. Results in an HTTP 500 Internal
      * Server Error with detailed error causes in the RDF body.
      */
     @Test
     public void testErrorUploadWithInconsistentArtifactRdf() throws Exception
     {
         final ClientResource uploadArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_BAD_2_LEAD_INSTITUTES,
                         MediaType.APPLICATION_RDF_XML);
         
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat responseFormat = RDFFormat.forMIMEType(mediaType.getName(), RDFFormat.RDFXML);
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input, mediaType,
                     Status.SERVER_ERROR_INTERNAL, this.testWithAdminPrivileges);
         }
         catch(final ResourceException e)
         {
             // verify: error details
             Assert.assertEquals("Not the expected HTTP status code", Status.SERVER_ERROR_INTERNAL, e.getStatus());
             
             final String body = uploadArtifactClientResource.getResponseEntity().getText();
             ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
             final Model model = Rio.parse(inputStream, "", responseFormat);
             
             // DebugUtils.printContents(model);
             
             Assert.assertEquals("Not the expected results size", 13, model.size());
             
             final Set<Resource> errors = model.filter(null, RDF.TYPE, PoddRdfConstants.ERR_TYPE_TOP_ERROR).subjects();
             Assert.assertEquals("Not the expected number of Errors", 1, errors.size());
             final Resource topError = errors.iterator().next();
             
             // Resource level error details
             Assert.assertEquals("Not the expected HTTP Status Code", "500",
                     model.filter(topError, PoddRdfConstants.HTTP_STATUS_CODE_VALUE, null).objectString());
             Assert.assertEquals("Not the expected Reason Phrase", "Internal Server Error",
                     model.filter(topError, PoddRdfConstants.HTTP_REASON_PHRASE, null).objectString());
             Assert.assertEquals("Not the expected RDFS:comment", "Error loading artifact to PODD",
                     model.filter(topError, RDFS.COMMENT, null).objectString());
             
             Assert.assertEquals("Expected 1 child error node", 1,
                     model.filter(topError, PoddRdfConstants.ERR_CONTAINS, null).size());
             final Resource errorNode = model.filter(topError, PoddRdfConstants.ERR_CONTAINS, null).objectResource();
             
             // Error cause details
             Assert.assertEquals("Not the expected Exception class", InconsistentOntologyException.class.getName(),
                     model.filter(errorNode, PoddRdfConstants.ERR_EXCEPTION_CLASS, null).objectString());
             
             Assert.assertEquals("Not the expected error source", "urn:temp:inconsistentArtifact:1",
                     model.filter(errorNode, PoddRdfConstants.ERR_SOURCE, null).objectString());
         }
     }
     
     /**
      * Test Upload attempt with an artifact that is inconsistent. Results in an HTTP 500 Internal
      * Server Error with detailed error causes in the RDF body.
      */
     @Test
     public void testErrorUploadWithNotInOwlDlProfileArtifactRdf() throws Exception
     {
         final ClientResource uploadArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_BAD_NOT_OWL_DL,
                         MediaType.APPLICATION_RDF_XML);
         
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat responseFormat = RDFFormat.forMIMEType(mediaType.getName(), RDFFormat.RDFXML);
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input, mediaType,
                     Status.SERVER_ERROR_INTERNAL, this.testWithAdminPrivileges);
         }
         catch(final ResourceException e)
         {
             // verify: error details
             Assert.assertEquals("Not the expected HTTP status code", Status.SERVER_ERROR_INTERNAL, e.getStatus());
             
             final String body = uploadArtifactClientResource.getResponseEntity().getText();
             ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
             final Model model = Rio.parse(inputStream, "", responseFormat);
             
             // DebugUtils.printContents(model);
             
             Assert.assertEquals("Not the expected results size", 18, model.size());
             
             final Set<Resource> errors = model.filter(null, RDF.TYPE, PoddRdfConstants.ERR_TYPE_TOP_ERROR).subjects();
             Assert.assertEquals("Not the expected number of Errors", 1, errors.size());
             final Resource topError = errors.iterator().next();
             
             // Resource level error details
             Assert.assertEquals("Not the expected HTTP Status Code", "500",
                     model.filter(topError, PoddRdfConstants.HTTP_STATUS_CODE_VALUE, null).objectString());
             Assert.assertEquals("Not the expected Reason Phrase", "Internal Server Error",
                     model.filter(topError, PoddRdfConstants.HTTP_REASON_PHRASE, null).objectString());
             Assert.assertEquals("Not the expected RDFS:comment", "Error loading artifact to PODD",
                     model.filter(topError, RDFS.COMMENT, null).objectString());
             
             // Error cause details
             Assert.assertEquals("Not the expected Exception class", OntologyNotInProfileException.class.getName(),
                     model.filter(null, PoddRdfConstants.ERR_EXCEPTION_CLASS, null).objectString());
             
             Assert.assertEquals(
                     "Expected error sources not found",
                     2,
                     model.filter(
                             null,
                             PoddRdfConstants.ERR_SOURCE,
                             PoddRdfConstants.VF
                                     .createLiteral("ClassAssertion(owl:Individual <mailto:helen.daily@csiro.au>)"))
                             .size());
         }
     }
     
     /**
      * Test unauthenticated access to "upload artifact" leads to an UNAUTHORIZED error.
      */
     @Test
     public void testErrorUploadWithoutAuthentication() throws Exception
     {
         final ClientResource uploadArtifactResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation input =
                 this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                         MediaType.APPLICATION_RDF_XML);
         
         final FormDataSet form = new FormDataSet();
         form.setMultipart(true);
         form.getEntries().add(new FormData("file", input));
         
         try
         {
             uploadArtifactResource.post(form, MediaType.TEXT_HTML);
             Assert.fail("Should have thrown a ResourceException with Status Code 401");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
     }
     
     /**
      * Test upload attempt without actual file leads to a BAD_REQUEST error
      */
     @Test
     public void testErrorUploadWithoutFile() throws Exception
     {
         final ClientResource uploadArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final FormDataSet form = new FormDataSet();
         form.setMultipart(true);
         
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                     MediaType.TEXT_HTML, Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
             Assert.fail("Should have thrown a ResourceException with Status Code 400");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
     }
     
     /**
      * Test authenticated access to the upload Artifact page in HTML
      */
     @Test
     public void testGetUploadArtifactPageBasicHtml() throws Exception
     {
         // prepare: add an artifact
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         Assert.assertTrue(body.contains("Upload new artifact"));
         Assert.assertTrue(body.contains("type=\"file\""));
         
         this.assertFreemarker(body);
     }
     
     /**
      * Test successful upload of a new artifact file while authenticated with the admin role.
      * Expects an HTML response.
      */
     @Test
     public void testUploadArtifactBasicHtml() throws Exception
     {
         final ClientResource uploadArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation input =
                 this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                         MediaType.APPLICATION_RDF_XML);
         
         final FormDataSet form = new FormDataSet();
         form.setMultipart(true);
         form.getEntries().add(new FormData("file", input));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // TODO: verify results once a proper success page is incorporated.
         final String body = results.getText();
         Assert.assertTrue(body.contains("Artifact successfully uploaded"));
         this.assertFreemarker(body);
     }
     
     /**
      * Test successful upload of a new artifact file while authenticated with the admin role.
      * Expects a plain text response.
      */
     @Test
     public void testUploadArtifactBasicRdf() throws Exception
     {
         final ClientResource uploadArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation input =
                 this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                         MediaType.APPLICATION_RDF_XML);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                         MediaType.TEXT_PLAIN, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: results (expecting the added artifact's ontology IRI)
         final String body = results.getText();
         Assert.assertTrue(body.contains("http://"));
         Assert.assertFalse(body.contains("html"));
         Assert.assertFalse(body.contains("\n"));
     }
     
     /**
      * Test successful upload of a new artifact file while authenticated with the admin role.
      * Expects a plain text response.
      */
     @Test
     public void testUploadArtifactBasicRdfWithFormData() throws Exception
     {
         final ClientResource uploadArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation input =
                 this.buildRepresentationFromResource("/test/artifacts/basicProject-1-internal-object.rdf",
                         MediaType.APPLICATION_RDF_XML);
         
         final FormDataSet form = new FormDataSet();
         form.setMultipart(true);
         form.getEntries().add(new FormData("file", input));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, form,
                         MediaType.TEXT_PLAIN, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: results (expecting the added artifact's ontology IRI)
         final String body = results.getText();
         Assert.assertTrue(body.contains("http://"));
         Assert.assertFalse(body.contains("html"));
         Assert.assertFalse(body.contains("\n"));
     }
     
     /**
      * Test successful upload of a new artifact file while authenticated with the admin role.
      * Expects a plain text response.
      */
     @Test
     public void testUploadArtifactBasicTurtle() throws Exception
     {
         final ClientResource uploadArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_TTL_1_INTERNAL_OBJECT,
                         MediaType.APPLICATION_RDF_TURTLE);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource, Method.POST, input,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: results (expecting the added artifact's ontology IRI)
         final String body = results.getText();
         
         final Collection<InferredOWLOntologyID> ontologyIDs = OntologyUtils.stringToOntologyID(body, RDFFormat.TURTLE);
         
         Assert.assertNotNull("No ontology IDs in response", ontologyIDs);
         Assert.assertEquals("More than 1 ontology ID in response", 1, ontologyIDs.size());
         Assert.assertTrue("Ontology ID not of expected format",
                 ontologyIDs.iterator().next().toString().contains("artifact:1:version:1"));
     }
     
     @Test
     public final void testLoadArtifactConcurrency() throws Exception
     {
         // load test artifact
         final InputStream inputStream4Artifact =
                 this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1);
         
         Assert.assertNotNull("Could not find test resource: " + TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1,
                 inputStream4Artifact);
         
         final String nextTestArtifact = IOUtils.toString(inputStream4Artifact);
         
         final AtomicInteger threadSuccessCount = new AtomicInteger(0);
         final AtomicInteger perThreadSuccessCount = new AtomicInteger(0);
         final AtomicInteger threadStartCount = new AtomicInteger(0);
         final AtomicInteger perThreadStartCount = new AtomicInteger(0);
         final CountDownLatch openLatch = new CountDownLatch(1);
         // Changing this from 8 to 9 on my machine may be triggering a restlet bug
         final int threadCount = 9;
         final int perThreadCount = 2;
         final CountDownLatch closeLatch = new CountDownLatch(threadCount);
         for(int i = 0; i < threadCount; i++)
         {
             final int number = i;
             Runnable runner = new Runnable()
                 {
                     public void run()
                     {
                         try
                         {
                             openLatch.await(55000, TimeUnit.MILLISECONDS);
                             threadStartCount.incrementAndGet();
                             for(int j = 0; j < perThreadCount; j++)
                             {
                                 perThreadStartCount.incrementAndGet();
                                 ClientResource uploadArtifactClientResource = null;
                                 
                                 try
                                 {
                                     uploadArtifactClientResource =
                                             new ClientResource(
                                                     UploadArtifactResourceImplTest.this
                                                             .getUrl(PoddWebConstants.PATH_ARTIFACT_UPLOAD));
                                     
                                     AbstractResourceImplTest.setupThreading(uploadArtifactClientResource.getContext());
                                     
                                     final Representation input =
                                             UploadArtifactResourceImplTest.this.buildRepresentationFromResource(
                                                     TestConstants.TEST_ARTIFACT_IMPORT_PSCIENCEv1,
                                                     MediaType.APPLICATION_RDF_XML);
                                     
                                     final Representation results =
                                             RestletTestUtils.doTestAuthenticatedRequest(uploadArtifactClientResource,
                                                     Method.POST, input, MediaType.APPLICATION_RDF_XML,
                                                     Status.SUCCESS_OK,
                                                     UploadArtifactResourceImplTest.this.testWithAdminPrivileges);
                                     
                                     // verify: results (expecting the added artifact's ontology IRI)
                                     final String body = results.getText();
                                     
                                     final Collection<InferredOWLOntologyID> ontologyIDs =
                                             OntologyUtils.stringToOntologyID(body, RDFFormat.RDFXML);
                                     
                                     Assert.assertNotNull("No ontology IDs in response", ontologyIDs);
                                     Assert.assertEquals("More than 1 ontology ID in response", 1, ontologyIDs.size());
                                     Assert.assertTrue("Ontology ID not of expected format", ontologyIDs.iterator()
                                             .next().toString().contains("artifact:1:version:1"));
                                     perThreadSuccessCount.incrementAndGet();
                                 }
                                 finally
                                 {
                                     if(uploadArtifactClientResource != null)
                                     {
                                         uploadArtifactClientResource.release();
                                     }
                                 }
                             }
                             threadSuccessCount.incrementAndGet();
                         }
                         catch(Throwable e)
                         {
                             e.printStackTrace();
                             Assert.fail("Failed in test: " + number);
                         }
                         finally
                         {
                             closeLatch.countDown();
                         }
                     }
                 };
             new Thread(runner, "TestThread" + number).start();
         }
         // all threads are waiting on the latch.
         openLatch.countDown(); // release the latch
         // all threads are now running concurrently.
         closeLatch.await(50000, TimeUnit.MILLISECONDS);
        //closeLatch.await();
         // Verify that there were no startup failures
         Assert.assertEquals("Some threads did not all start successfully", threadCount, threadStartCount.get());
         Assert.assertEquals("Some thread loops did not start successfully", perThreadCount * threadCount,
                 perThreadStartCount.get());
         // Verify that there were no failures, as the count is only incremented for successes, where
         // the closeLatch must always be called, even for failures
         Assert.assertEquals("Some thread loops did not complete successfully", perThreadCount * threadCount,
                 perThreadSuccessCount.get());
         Assert.assertEquals("Some threads did not complete successfully", threadCount, threadSuccessCount.get());
     }
     
 }
