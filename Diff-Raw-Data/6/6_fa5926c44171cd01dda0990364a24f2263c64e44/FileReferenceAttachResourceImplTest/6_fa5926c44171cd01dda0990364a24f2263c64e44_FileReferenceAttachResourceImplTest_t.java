 /**
  * 
  */
 package com.github.podd.restlet.test;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.StringWriter;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.RDFWriter;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.impl.file.test.SSHService;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author kutila
  * 
  */
 public class FileReferenceAttachResourceImplTest extends AbstractResourceImplTest
 {
     @Rule
     public final TemporaryFolder tempDirectory = new TemporaryFolder();
     
     /** SSH File Repository server for tests */
     protected SSHService sshd;
     
     protected void startRepositorySource() throws Exception
     {
         this.sshd = new SSHService();
         final File tempDirForHostKey = this.tempDirectory.newFolder();
         this.sshd.startTestSSHServer(Integer.parseInt(SSHService.TEST_SSH_SERVICE_PORT), tempDirForHostKey);
     }
     
     protected void stopRepositorySource() throws Exception
     {
         if(this.sshd != null)
         {
             this.sshd.stopTestSSHServer();
         }
     }
     
     /**
      * Test successful attach of a file reference in RDF/XML
      */
     @Test
     public void testAttachFileReferenceRdfWithoutVerification() throws Exception
     {
         // prepare: add an artifact
         // (use one with PURLs so that where to attach the file reference is known in advance)
         final InferredOWLOntologyID artifactID =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource fileRefAttachClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                 .getOntologyIRI().toString());
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                 .getVersionIRI().toString());
         // Query parameter Verification policy - NOT SUPPLIED - defaults to false
         // fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY,
         // Boolean.toString(false));
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                         MediaType.APPLICATION_RDF_XML);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                         MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify: Inferred Ontology ID is received in RDF format
         Assert.assertTrue("Response not in RDF format", body.contains("<rdf:RDF"));
         Assert.assertTrue("Artifact version has not been updated properly", body.contains("artifact:1:version:2"));
         Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
         Assert.assertTrue("Inferred version not in response", body.contains("inferredVersion"));
         
         // verify: new file reference has been added to the artifact
         final String artifactBody =
                 this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_XML);
         Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("Rice tree scan 003454-98"));
         Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("object-rice-scan-34343-a"));
     }
     
     /**
      * Test successful attach of a file reference in RDF/XML with verification This test starts up
      * an SSH server as a File Repository source and can be slow to complete.
      */
     @Test
     public void testAttachFileReferenceRdfWithVerification() throws Exception
     {
         this.startRepositorySource();
         
         // prepare: add an artifact
         // (use one with PURLs so that where to attach the file reference is known in advance)
         final InferredOWLOntologyID artifactID =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         // prepare: build test fragment with correct value set for poddBase:hasPath
         final String fileReferenceString =
                 this.buildFileReferenceString(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_VERIFIABLE);
         Assert.assertFalse("Input FileReference could not be genereated", fileReferenceString.isEmpty());
         
         final ClientResource fileRefAttachClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                 .getOntologyIRI().toString());
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                 .getVersionIRI().toString());
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
         
         try
         {
             final Representation input = new StringRepresentation(fileReferenceString, MediaType.APPLICATION_RDF_XML);
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                             MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
             
             final String body = results.getText();
             
             // verify: Inferred Ontology ID is received in RDF format
             Assert.assertTrue("Response not in RDF format", body.contains("<rdf:RDF"));
             Assert.assertTrue("Artifact version has not been updated properly", body.contains("artifact:1:version:2"));
             Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
             Assert.assertTrue("Inferred version not in response", body.contains("inferredVersion"));
             
             // verify: new file reference has been added to the artifact
             final String artifactBody =
                     this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_XML);
             Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("Rice tree scan 003454-98"));
             Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("object-rice-scan-34343-a"));
         }
         finally
         {
             this.stopRepositorySource();
         }
     }
     
     /**
      * Given the path to a resource containing an incomplete File Reference object, this method
      * constructs a complete File Reference and returns it as an RDF/XML string.
      * 
      * @param fragmentSource
      *            Location of resource containing incomplete File Reference
      * @return String containing RDF statements
      */
     private String buildFileReferenceString(final String fragmentSource) throws Exception
     {
         // read the fragment's RDF statements into a Model
         final InputStream inputStream = this.getClass().getResourceAsStream(fragmentSource);
         final Model model = new LinkedHashModel();
         final RDFParser rdfParser = Rio.createParser(RDFFormat.RDFXML);
         rdfParser.setRDFHandler(new StatementCollector(model));
         rdfParser.parse(inputStream, "");
         
         // path to be set as part of the file reference
         final String completePath = this.getClass().getResource(TestConstants.TEST_REMOTE_FILE_PATH).getPath();
         
         final Resource aliasUri =
                 model.filter(null, PoddRdfConstants.PODD_BASE_ALIAS, null).subjects().iterator().next();
         model.add(aliasUri, PoddRdfConstants.PODD_BASE_FILE_PATH,
                 ValueFactoryImpl.getInstance().createLiteral(completePath));
         model.add(aliasUri, PoddRdfConstants.PODD_BASE_FILENAME,
                 ValueFactoryImpl.getInstance().createLiteral(TestConstants.TEST_REMOTE_FILE_NAME));
         
         // get a String representation of the statements in the Model
         final StringWriter out = new StringWriter();
         final RDFWriter writer = Rio.createWriter(RDFFormat.RDFXML, out);
         writer.startRDF();
         for(final Statement st : model)
         {
             writer.handleStatement(st);
         }
         writer.endRDF();
         return out.getBuffer().toString();
     }
     
     /**
      * Test attach a file reference without authentication
      */
     @Test
     public void testErrorAttachFileReferenceRdfWithoutAuthentication() throws Exception
     {
         // prepare: dummy artifact details
         final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
         
         final ClientResource fileRefAttachClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactUri);
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                         MediaType.APPLICATION_RDF_XML);
         
         // invoke without authentication
         try
         {
             fileRefAttachClientResource.post(input, MediaType.APPLICATION_RDF_XML);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
     }
     
     @Test
     public void testErrorAttachFileReferenceRdfWithoutArtifactID() throws Exception
     {
         // prepare: dummy artifact details
         final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
         
         final ClientResource fileRefAttachClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         
         // Query parameter Artifact ID - NOT SUPPLIED
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactUri);
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
         
         this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                 MediaType.APPLICATION_RDF_XML);
         
         // there is no need to authenticate, have a test artifact or send RDF content as the
         // artifact ID is checked for first
         try
         {
             fileRefAttachClientResource.post(null, MediaType.TEXT_PLAIN);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
     }
     
     @Test
     public void testErrorAttachFileReferenceRdfWithoutVersionIRI() throws Exception
     {
         // prepare: dummy artifact details
         final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
         
         final ClientResource fileRefAttachClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         // Query parameter Version IRI - NOT SUPPLIED
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                         MediaType.APPLICATION_RDF_XML);
         
         // authentication is required as version IRI is checked AFTER authentication
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                     MediaType.APPLICATION_RDF_XML, Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
     }
     
     /**
      * Test attach a file reference which fails verification in RDF/XML
      */
     @Test
     public void testErrorAttachFileReferenceRdfFileVerificationFailure() throws Exception
     {
         // prepare: add an artifact
         // (use one with PURLs so that where to attach the file reference is known in advance)
         final InferredOWLOntologyID artifactID =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
         
         final ClientResource fileRefAttachClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                 .getOntologyIRI().toString());
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                 .getVersionIRI().toString());
         fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
         
         final Representation input =
                 this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                         MediaType.APPLICATION_RDF_XML);
         
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                     MediaType.APPLICATION_RDF_XML, Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
             Assert.fail("Should have thrown a ResourceException");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals(Status.SERVER_ERROR_BAD_GATEWAY, e.getStatus());
            Representation responseEntity = fileRefAttachClientResource.getResponseEntity();
            Assert.assertTrue(responseEntity.getText().contains("File Reference validation resulted in failures"));
         }
     }
     
     /**
      * Test successful attach of a file reference in Turtle
      */
     @Ignore
     @Test
     public void testAttachFileReferenceBasicTurtle() throws Exception
     {
         final ClientResource fileRefAttachClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
         
         Assert.fail("TODO: implement me");
     }
     
 }
