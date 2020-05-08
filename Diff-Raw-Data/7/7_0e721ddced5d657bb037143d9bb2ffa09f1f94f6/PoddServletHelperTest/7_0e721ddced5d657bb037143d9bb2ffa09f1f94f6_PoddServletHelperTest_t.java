 package com.github.podd.prototype.test;
 
 import info.aduna.iteration.Iterations;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.nio.charset.StandardCharsets;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.Map.Entry;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.TemporaryFolder;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.Rio;
 import org.openrdf.sail.memory.MemoryStore;
 import org.semanticweb.owlapi.model.IRI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.prototype.FileReferenceUtils;
 import com.github.podd.prototype.HttpFileReference;
 import com.github.podd.prototype.InferredOWLOntologyID;
 import com.github.podd.prototype.PoddException;
 import com.github.podd.prototype.PoddServlet;
 import com.github.podd.prototype.PoddServletContextListener;
 import com.github.podd.prototype.PoddServletHelper;
 import com.github.podd.prototype.SshFileReference;
 
 public class PoddServletHelperTest
 {
     @Rule
     public TemporaryFolder tempDirectory = new TemporaryFolder();
     
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     PoddServletHelper helper = null;
     final List<Entry<URI, String>> schemaOntologyList = new ArrayList<>();
     
     @Before
     public void setUp() throws Exception
     {
         final Repository repository = new SailRepository(new MemoryStore());
         repository.initialize();
         
         this.helper = new PoddServletHelper();
         this.helper.setUp(repository);
         
         schemaOntologyList.add(new SimpleEntry<URI, String>(repository.getValueFactory().createURI(
                 PoddServletContextListener.URI_PODD_BASE), PoddServletContextListener.PATH_PODD_BASE));
         schemaOntologyList.add(new SimpleEntry<URI, String>(repository.getValueFactory().createURI(
                 PoddServletContextListener.URI_PODD_SCIENCE), PoddServletContextListener.PATH_PODD_SCIENCE));
         schemaOntologyList.add(new SimpleEntry<URI, String>(repository.getValueFactory().createURI(
                 PoddServletContextListener.URI_PODD_PLANT), PoddServletContextListener.PATH_PODD_PLANT));
         this.helper.setSchemaOntologyList(schemaOntologyList);
         
         this.helper.loadSchemaOntologies();
         
         final InputStream inputStream = this.getClass().getResourceAsStream("/test/alias.ttl");
         Assert.assertNotNull("Could not find alias file", inputStream);
         
         final FileReferenceUtils utils = new FileReferenceUtils();
         this.log.info("About to set aliases");
         utils.setAliases(inputStream, RDFFormat.TURTLE);
         this.log.info("Finished setting aliases");
         
         this.helper.setFileReferenceUtils(utils);
     }
     
     @After
     public void tearDown() throws Exception
     {
         this.helper.tearDown();
         // FileReferenceUtils.getInstance().clean();
     }
     
     @Test
     public void testLoadInvalidArtifact() throws Exception
     {
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/cookie.txt");
         Assert.assertNotNull("Could not find test resource", in);
         try
         {
             this.helper.loadPoddArtifact(in, PoddServlet.MIME_TYPE_RDF_XML);
             Assert.fail("Should have thrown an exception here");
         }
         catch(final RDFParseException e)
         {
             Assert.assertNotNull(e);
         }
     }
     
     /**
      * Tests loading a simple PODD artifact. Response is validated based on returned artifact URI no
      * longer having "urn:temp:".
      * 
      * @throws Exception
      */
     @Test
     public void testLoadArtifact() throws Exception
     {
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         
         final InferredOWLOntologyID artifactUriString = this.helper.loadPoddArtifact(in, mimeType);
         Assert.assertNotNull(artifactUriString);
         
         final String resultRDF =
                 this.helper.getArtifact(artifactUriString.getOntologyIRI().toString(), mimeType, false);
         
         Assert.assertNotNull(resultRDF);
         Assert.assertFalse(resultRDF.contains("urn:temp:"));
         Assert.assertTrue(resultRDF.contains(artifactUriString.getOntologyIRI().toString()));
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
         Assert.assertEquals(29, repoConn.size(context));
     }
     
     @Test
     public void testGetNonExistentArtifact() throws Exception
     {
         final String artifactUniqueIRI = "http://purl.org/nosuch/artifact:1";
         
         try
         {
             this.helper.getArtifact(artifactUniqueIRI, PoddServlet.MIME_TYPE_RDF_XML, false);
             Assert.fail("Should have thrown an exception");
         }
         catch(final Exception e)
         {
             Assert.assertNotNull(e);
             Assert.assertTrue(e.getMessage().contains("not found"));
         }
     }
     
     @Test
     public void testGetArtifactRdfXml() throws Exception
     {
         // first, load an artifact using the inner-load method
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
         
         // now retrieve it via the Helper
         final String resultRDF = this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, false);
         
         Assert.assertNotNull(resultRDF);
         Assert.assertFalse(resultRDF.contains("urn:temp:"));
         Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
         Assert.assertEquals(29, repoConn.size(context));
     }
     
     @Test
     public void testGetArtifactWithInferredStatements() throws Exception
     {
         // first, load an artifact using the inner-load method
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
         
         // now retrieve it via the Helper
         final String resultRDF = this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, true);
         
         // validate inferred statements
         Assert.assertNotNull(resultRDF);
         Assert.assertFalse(resultRDF.contains("urn:temp:"));
         Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
         Assert.assertTrue(repoConn.size(context) > 29);
     }
     
     /**
      * Tests trying to retrieve a schema ontology that is not managed by PODD
      */
     @Test
     public void testGetSchemaOntologyNonExistent() throws Exception
     {
         final String ontologyUri = "http://purl.org/podd/ns/poddNoSuchSchemaOntology";
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         
         try
         {
             this.helper.getSchemaOntology(ontologyUri, mimeType, new ByteArrayOutputStream());
             Assert.fail("Should have thrown an exception");
         }
        catch(final PoddException e)
         {
            Assert.assertTrue(e.getMessage().startsWith("Schema Ontology"));
            Assert.assertTrue(e.getMessage().endsWith("not found."));
         }
     }
     
     /**
      * Tests trying to retrieve the PODD Base schema ontology
      */
     @Test
     public void testGetSchemaOntologyPoddBase() throws Exception
     {
         final String ontologyUri = "http://purl.org/podd/ns/poddBase";
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         
         this.helper.getSchemaOntology(ontologyUri, mimeType, out);
         
         // verify
         Assert.assertTrue("Ontology not written to output stream", out.size() > 0);
         
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(out.toString(), mimeType, context);
         Assert.assertEquals(282, repoConn.size(context));
     }
     
     /**
      * Tests trying to retrieve the PODD Science schema ontology
      */
     @Test
     public void testGetSchemaOntologyPoddScience() throws Exception
     {
         final String ontologyUri = "http://purl.org/podd/ns/poddScience";
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         
         this.helper.getSchemaOntology(ontologyUri, mimeType, out);
         
         // verify
         Assert.assertTrue("Ontology not written to output stream", out.size() > 0);
         
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(out.toString(), mimeType, context);
         Assert.assertEquals(1588, repoConn.size(context));
     }
     
     @Test
     public void testDeleteArtifact() throws Exception
     {
         // first, load an artifact using the inner-load method
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
         
         // get artifact to verify it exists
         final String resultRDF = this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, false);
         Assert.assertNotNull(resultRDF);
         
         // delete artifact
         this.helper.deleteArtifact(artifactUniqueIRI.toString());
         
         // get artifact should now give an error
         try
         {
             this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, false);
             Assert.fail("Should have thrown an exception");
         }
         catch(final Exception e)
         {
             Assert.assertNotNull(e);
             Assert.assertTrue(e.getMessage().contains("not found"));
         }
     }
     
     @Test
     public void testDeleteNonExistentArtifact() throws Exception
     {
         final String artifactUniqueIRI = "http://purl.org/nosuch/artifact:1";
         
         try
         {
             this.helper.deleteArtifact(artifactUniqueIRI);
             Assert.fail("Should have thrown an exception");
         }
         catch(final Exception e)
         {
             Assert.assertNotNull(e);
             Assert.assertTrue(e.getMessage().contains("not found"));
         }
     }
     
     @Test
     public void testEditNonExistentArtifact() throws Exception
     {
         final String artifactUniqueIRI = "http://purl.org/nosuch/artifact:1";
         
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/fragment.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         
         try
         {
             this.helper.editArtifact(artifactUniqueIRI, in, PoddServlet.MIME_TYPE_RDF_XML, false, false);
             Assert.fail("Should have thrown an exception");
         }
         catch(final Exception e)
         {
             Assert.assertNotNull(e);
             Assert.assertTrue(e.getMessage().contains("not found"));
         }
     }
     
     @Test
     public void testEditArtifactWithMerge() throws Exception
     {
         final boolean isReplace = false;
         // first, load an artifact using the inner-load method
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/editableProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
         
         // edit it
         final InputStream nextInputStream = this.getClass().getResourceAsStream("/test/artifacts/fragment.rdf");
         Assert.assertNotNull("Could not find test resource", nextInputStream);
         
         final String editedArtifactURI =
                 this.helper.editArtifact(artifactUniqueIRI.stringValue(), nextInputStream, mimeType, isReplace, false);
         
         // check the modifications were persisted
         final String resultRDF = this.helper.getArtifact(editedArtifactURI, mimeType, false);
         // System.out.println(resultRDF);
         
         Assert.assertNotNull(resultRDF);
         Assert.assertFalse(resultRDF.contains("urn:temp:"));
         Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
         Assert.assertTrue(resultRDF.contains("John.Doe@csiro.au"));
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
         
         // with merge, the "OWL Comment" from the ontology is retained
         Assert.assertEquals(33, repoConn.size(context));
     }
     
     @Test
     public void testEditArtifactWithReplace() throws Exception
     {
         final boolean isReplace = true;
         // first, load an artifact using the inner-load method
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/editableProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
         
         // edit it
         final InputStream nextInputStream = this.getClass().getResourceAsStream("/test/artifacts/fragment.rdf");
         Assert.assertNotNull("Could not find test resource", nextInputStream);
         
         final String editedArtifactURI =
                 this.helper.editArtifact(artifactUniqueIRI.stringValue(), nextInputStream, mimeType, isReplace, false);
         
         // check the modifications were persisted
         final String resultRDF = this.helper.getArtifact(editedArtifactURI, mimeType, false);
         
         Assert.assertNotNull(resultRDF);
         Assert.assertFalse(resultRDF.contains("urn:temp:"));
         Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
         Assert.assertTrue(resultRDF.contains("John.Doe@csiro.au"));
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
         
         // with replace, the "OWL Comment" from the ontology is removed
         Assert.assertEquals(32, repoConn.size(context));
     }
     
     /**
      * Test modifying a PODD artifact where the edited fragment adds new File References.
      * 
      * @throws Exception
      */
     @Test
     public void testEditArtifactWithFileReferenceAttachment() throws Exception
     {
         final boolean isReplace = true;
         // first, load an artifact using the inner-load method
         final InputStream in =
                 this.getClass().getResourceAsStream("/test/artifacts/basicProject-1-internal-object.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
         
         // edit it with a fragment that contains "invalid" file references
         final InputStream nextInputStream =
                 this.getClass().getResourceAsStream("/test/artifacts/fragment-invalid-file-reference.rdf");
         Assert.assertNotNull("Could not find test resource", nextInputStream);
         
         try
         {
             this.helper.editArtifact(artifactUniqueIRI.stringValue(), nextInputStream, mimeType, isReplace, true);
             Assert.fail("Did not find expected exception");
         }
         catch(final PoddException e)
         {
             Assert.assertNotNull(e);
         }
         
         // edit it with a fragment that is correct
         final InputStream nextInputStream2 =
                 this.getClass().getResourceAsStream("/test/artifacts/fragment-1-file-reference.rdf");
         Assert.assertNotNull("Could not find test resource", nextInputStream2);
         final String editedArtifactURI =
                 this.helper.editArtifact(artifactUniqueIRI.stringValue(), nextInputStream2, mimeType, isReplace, true);
         
         // check the modifications were persisted
         final String resultRDF = this.helper.getArtifact(editedArtifactURI, mimeType, false);
         
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
         final URI propertyHasFileReference =
                 IRI.create("http://purl.org/podd/ns/poddBase#hasFileReference").toOpenRDFURI();
         final RepositoryResult<Statement> statements =
                 repoConn.getStatements(null, propertyHasFileReference, null, false);
         final List<Statement> fileRefStatements = Iterations.addAll(statements, new ArrayList<Statement>());
         Assert.assertEquals("There should be exactly 1 hasFileReference property", 1, fileRefStatements.size());
     }
     
     @Test
     public void testAttachReferenceToInvalidHttpObject() throws Exception
     {
         // 1. try attaching to a non-existent artifact
         final String artifactToAttachTo = "http://no.such.artifact";
         final String objectToAttachTo = "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0";
         
         final String serverAlias = "salesforce";
         final String path = "/help/doc/en/";
         final String filename = "salesforce_git_developer_cheatsheet.pdf";
         
         final HttpFileReference invalidRef = new HttpFileReference();
         invalidRef.setArtifactUri(artifactToAttachTo);
         invalidRef.setObjectUri(objectToAttachTo);
         invalidRef.setServerAlias(serverAlias);
         invalidRef.setPath(path);
         invalidRef.setFilename(filename);
         invalidRef.setDescription(null);
         
         try
         {
             this.helper.attachReference(invalidRef, false);
             Assert.fail("Did not find expected exception");
         }
         catch(final PoddException e)
         {
             Assert.assertEquals("Artifact <http://no.such.artifact> not found.", e.getMessage());
         }
         
         // 2. try attaching to a non-existent object (inside an artifact)
         final InputStream in =
                 this.getClass().getResourceAsStream("/test/artifacts/basicProject-1-internal-object.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         
         invalidRef.setArtifactUri(addedRDF.getOntologyIRI().toOpenRDFURI().stringValue());
         invalidRef.setObjectUri("urn:poddinternal:no-such-object:0");
         
         try
         {
             this.helper.attachReference(invalidRef, false);
             Assert.fail("Did not find expected exception");
         }
         catch(final PoddException e)
         {
             Assert.assertEquals("Object <urn:poddinternal:no-such-object:0> not found.", e.getMessage());
         }
     }
     
     @Test
     public void testAttachReferenceHttp() throws Exception
     {
         // first, load an artifact using the inner-load method
         final InputStream in =
                 this.getClass().getResourceAsStream("/test/artifacts/basicProject-1-internal-object.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         
         // where to attach the file reference to
         final String artifactToAttachTo = addedRDF.getOntologyIRI().toOpenRDFURI().stringValue();
         final String objectToAttachTo = "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0";
         
         // file reference attributes
         final String serverAlias = "w3";
         final String path = "Protocols/rfc2616";
         final String filename = "rfc2616.html";
         final String description = "http RFC";
         
         final HttpFileReference fileRef = new HttpFileReference();
         fileRef.setArtifactUri(artifactToAttachTo);
         fileRef.setObjectUri(objectToAttachTo);
         fileRef.setServerAlias(serverAlias);
         fileRef.setPath(path);
         fileRef.setFilename(filename);
         fileRef.setDescription(description);
         
         final URI fileReferenceUri = this.helper.attachReference(fileRef, true);
         Assert.assertNotNull(fileReferenceUri);
         // retrieve artifact and verify whether file reference was correctly attached
         final String resultRDF = this.helper.getArtifact(artifactToAttachTo, mimeType, false);
         
         Assert.assertNotNull(resultRDF);
         Assert.assertFalse(resultRDF.contains("urn:temp:"));
         Assert.assertTrue(resultRDF.contains(filename));
         Assert.assertTrue(resultRDF.contains(serverAlias));
         
         final URI context = IRI.create("urn:context").toOpenRDFURI();
         final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
         final URI propertyHasFileReference =
                 IRI.create("http://purl.org/podd/ns/poddBase#hasFileReference").toOpenRDFURI();
         final RepositoryResult<Statement> statements =
                 repoConn.getStatements(null, propertyHasFileReference, null, false);
         final List<Statement> fileRefStatements = Iterations.addAll(statements, new ArrayList<Statement>());
         Assert.assertEquals("There should be exactly 1 hasFileReference property", 1, fileRefStatements.size());
         Assert.assertEquals(objectToAttachTo, fileRefStatements.get(0).getSubject().stringValue());
     }
     
     @Test
     public void testAttachReferenceSSH() throws Exception
     {
         final SSHService sshd = new SSHService();
         try
         {
             sshd.startTestSSHServer(9856, this.tempDirectory.newFolder());
             // first, load an artifact using the inner-load method
             final InputStream in =
                     this.getClass().getResourceAsStream("/test/artifacts/basicProject-1-internal-object.rdf");
             Assert.assertNotNull("Could not find test resource", in);
             final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
             final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
             
             // where to attach the file reference to
             final String artifactToAttachTo = addedRDF.getOntologyIRI().toOpenRDFURI().stringValue();
             final String objectToAttachTo = "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0";
             
             // file reference attributes
             final String serverAlias = "localssh";
             final String path = "src/test/resources/test/artifacts";
             final String filename = "basicProject-1.rdf";
             final String description = "Refers to one of the test artifacts, to be accessed through an ssh server";
             
             final SshFileReference fileRef = new SshFileReference();
             fileRef.setArtifactUri(artifactToAttachTo);
             fileRef.setObjectUri(objectToAttachTo);
             fileRef.setServerAlias(serverAlias);
             fileRef.setPath(path);
             fileRef.setFilename(filename);
             fileRef.setDescription(description);
             
             final URI fileReferenceUri = this.helper.attachReference(fileRef, true);
             Assert.assertNotNull(fileReferenceUri);
             // retrieve artifact and verify whether file reference was correctly attached
             final String resultRDF = this.helper.getArtifact(artifactToAttachTo, mimeType, false);
             
             Assert.assertNotNull(resultRDF);
             Assert.assertFalse(resultRDF.contains("urn:temp:"));
             Assert.assertTrue(resultRDF.contains(filename));
             Assert.assertTrue(resultRDF.contains(serverAlias));
             
             final URI context = IRI.create("urn:context").toOpenRDFURI();
             final RepositoryConnection repoConn = this.loadDataToNewRepository(resultRDF, mimeType, context);
             final URI propertyHasFileReference =
                     IRI.create("http://purl.org/podd/ns/poddBase#hasFileReference").toOpenRDFURI();
             final RepositoryResult<Statement> statements =
                     repoConn.getStatements(null, propertyHasFileReference, null, false);
             final List<Statement> fileRefStatements = Iterations.addAll(statements, new ArrayList<Statement>());
             Assert.assertEquals("There should be exactly 1 hasFileReference property", 1, fileRefStatements.size());
             Assert.assertEquals(objectToAttachTo, fileRefStatements.get(0).getSubject().stringValue());
         }
         finally
         {
             sshd.stopTestSSHServer();
         }
     }
     
     @Test
     public void testResetPodd() throws Exception
     {
         // first, load an artifact using the inner-load method
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
         
         // now retrieve it via the Helper
         final String resultRDF = this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, false);
         Assert.assertNotNull(resultRDF);
         Assert.assertFalse(resultRDF.contains("urn:temp:"));
         Assert.assertTrue(resultRDF.contains(artifactUniqueIRI.toString()));
         
         this.helper.resetPodd();
         
         try
         {
             this.helper.getArtifact(artifactUniqueIRI.toString(), mimeType, false);
             Assert.fail("Should have thrown an exception trying to retrieve non-existent artifact");
         }
         catch(final PoddException e)
         {
             Assert.assertTrue(e.getMessage().startsWith("Artifact <"));
             Assert.assertTrue(e.getMessage().endsWith("> not found."));
         }
         
         final ByteArrayOutputStream bout = new ByteArrayOutputStream();
         this.helper.getSchemaOntology("http://purl.org/podd/ns/poddBase", mimeType, bout);
         Assert.assertNotNull(bout);
         Assert.assertTrue(bout.size() > 0);
     }
     
     /**
      * Tests that schema ontologies are not reloaded if the repository has a non-empty schema
      * ontology management graph.
      * 
      * Also verifies that the helper instance is consistent and can load artifacts after such a
      * start up.
      * 
      * @throws Exception
      */
     @Test
     public void testLoadSchemaOntologies() throws Exception
     {
         this.helper.tearDown();
         
         // create a new Repository and configure the Helper
         final Repository repository = new SailRepository(new MemoryStore());
         repository.initialize();
         
         final RepositoryConnection testRepositoryConnection = repository.getConnection();
         testRepositoryConnection.setAutoCommit(false);
         
         Assert.assertEquals("Repository should be empty at the beginning", 0, testRepositoryConnection.size());
         testRepositoryConnection.rollback();
         
         // create another Helper instance and use it to populate the Repository
         final PoddServletHelper anotherHelper = new PoddServletHelper();
         anotherHelper.setUp(repository);
         anotherHelper.setSchemaOntologyList(schemaOntologyList);
         anotherHelper.loadSchemaOntologies();
         
         Assert.assertEquals(2860, testRepositoryConnection.size());
         testRepositoryConnection.rollback();
         
         // setup the test Helper instance using the same repository (which already has schema
         // ontologies)
         this.helper = new PoddServletHelper();
         this.helper.setUp(repository);
         this.helper.setSchemaOntologyList(schemaOntologyList);
         this.helper.loadSchemaOntologies();
         
         // assert that the Repository is unchanged by loadSchemaOntologies()
         Assert.assertEquals("Repository size should not have changed", 2860, testRepositoryConnection.size());
         testRepositoryConnection.rollback();
         testRepositoryConnection.close();
         
         // check if it is possible to add an artifact to this helper/repository pair
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         
         final InferredOWLOntologyID artifactUriString = this.helper.loadPoddArtifact(in, mimeType);
         Assert.assertNotNull(artifactUriString);
     }
     
     @Test
     public void testIncrementVersion() throws Exception
     {
         final String[] in =
                 { "", "abc", "alpha:1", "http://example.org/ac-d/art%3A55", "http://example.org/ac-d/art%3A99999",
                         "http://example.org/permanenturl/431fd79a-7c8d-487c-9df5-09f4cd386249/artifact%3Aversion%3A1" };
         final String[] expected =
                 { "1", "abc1", "alpha:11", "http://example.org/ac-d/art%3A56", "http://example.org/ac-d/art%3A100000",
                         "http://example.org/permanenturl/431fd79a-7c8d-487c-9df5-09f4cd386249/artifact%3Aversion%3A2" };
         
         for(int i = 0; i < in.length; i++)
         {
             final String extracted = PoddServletHelper.incrementVersion(in[i]);
             Assert.assertEquals(expected[i], extracted);
         }
         
     }
     
     @Test
     public void testGetInferredOWLOntologyIDForArtifact() throws Exception
     {
         // first, load an artifact using the inner-load method
         final InputStream in = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
         Assert.assertNotNull("Could not find test resource", in);
         final String mimeType = PoddServlet.MIME_TYPE_RDF_XML;
         final InferredOWLOntologyID addedRDF = this.helper.loadPoddArtifact(in, mimeType);
         final URI artifactUniqueIRI = addedRDF.getOntologyIRI().toOpenRDFURI();
         // test with a non-existent artifact
         final InferredOWLOntologyID id = this.helper.getInferredOWLOntologyIDForArtifact("http://nosuch:ontology:1");
         Assert.assertNull(id.getInferredOntologyIRI());
         Assert.assertNull(id.getVersionIRI());
         
         // test with the added artifact
         final InferredOWLOntologyID nextId =
                 this.helper.getInferredOWLOntologyIDForArtifact(artifactUniqueIRI.stringValue());
         
         // FIXME: Test nextId
     }
     
     @Test
     public void testExtractUri() throws Exception
     {
         final String[] in =
                 {
                         "http/www.google.com",
                         "http/130.198.34.55:9090/permanenturl",
                         
                         // fragment separation # is not encoded
                         "https/thebank.org/myaccount#55",
                         
                         // the fragment should be encoded
                         "https/thebank.org/myaccount#55abc:alpha",
                         
                         // the query should be encoded
                         "https/purl.org/myaccount?phrase=abc:z",
                         "https/purl.org/myaccount?phrase/z",
                         
                         // the encoded '#' inside fragment should be kept as is
                         "https/thebank.org/myaccount#55abc%23-alpha",
                         
                         // the path has an encoded '#' that should be kept encoded
                         "https/thebank.org/myaccount%2355",
                         
                         // the ":" in the path should be encoded
                         "http/example.org/permanenturl/colon-in-path/artifact:1", "http/purl.org/artifact:4:3abc:99",
                         "http/example.org/alpha/artifact:1:0:5:22",
                         "http/www.podd.org/abc:3",
                         
                         // keep the encoded parts of the path unchanged
                         "http://purl.org/artifact%3A4%3A3abc%3A99",
                         "http/example.org/permanenturl/34cc1c8e-0ece-49f4-ac51-e17aa34648e4/artifact%3A1",
                         
                         // following are kept as they are
                         "urn:alphabeta:34:d", "urn:temp:tree:54#abc", "urn:temp:tree:54%23abc",
                         "ftp://somehost/somepath/somefile:zip", "mailto:folder@machine.csiro.au",
                         
                         // harmless element to terminate the test array
                         "http/purl.org" };
         final String[] expected =
                 {
                         "http://www.google.com",
                         "http://130.198.34.55:9090/permanenturl",
                         
                         // fragment separation # is not encoded
                         "https://thebank.org/myaccount#55",
                         
                         // the fragment should be encoded
                         "https://thebank.org/myaccount#55abc%3Aalpha",
                         
                         // the query should be encoded
                         "https://purl.org/myaccount?phrase%3Dabc%3Az",
                         "https://purl.org/myaccount?phrase%2Fz",
                         
                         // the encoded '#' inside fragment should be kept as is
                         "https://thebank.org/myaccount#55abc%23-alpha",
                         
                         // the path has an encoded '#' that should be kept encoded
                         "https://thebank.org/myaccount%2355",
                         
                         // the ":" in the path should be encoded
                         "http://example.org/permanenturl/colon-in-path/artifact%3A1",
                         "http://purl.org/artifact%3A4%3A3abc%3A99",
                         "http://example.org/alpha/artifact%3A1%3A0%3A5%3A22",
                         "http://www.podd.org/abc%3A3",
                         
                         // keep the encoded parts of the path unchanged
                         "http://purl.org/artifact%3A4%3A3abc%3A99",
                         "http://example.org/permanenturl/34cc1c8e-0ece-49f4-ac51-e17aa34648e4/artifact%3A1",
                         
                         // following are kept as they are
                         "urn:alphabeta:34:d", "urn:temp:tree:54#abc", "urn:temp:tree:54%23abc",
                         "ftp://somehost/somepath/somefile:zip", "mailto:folder@machine.csiro.au",
                         
                         // terminating element of the array
                         "http://purl.org" };
         
         for(int i = 0; i < in.length; i++)
         {
             final String extracted = PoddServletHelper.extractUri(in[i]);
             Assert.assertEquals(expected[i], extracted);
         }
         
         try
         {
             PoddServletHelper.extractUri("htp/a");
             Assert.fail("Did not find expected exception");
         }
         catch(final URISyntaxException e)
         {
             Assert.assertNotNull(e);
         }
     }
     
     // ----- helper methods -----
     
     private RepositoryConnection loadDataToNewRepository(final String data, final String mimeType, final URI context)
         throws Exception
     {
         // create a temporary Repository
         final Repository tempRepository = new SailRepository(new MemoryStore());
         tempRepository.initialize();
         final RepositoryConnection tempRepositoryConnection = tempRepository.getConnection();
         tempRepositoryConnection.setAutoCommit(false);
         
         // add data to Repository
         tempRepositoryConnection.add(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), "",
                 Rio.getParserFormatForMIMEType(mimeType), context);
         tempRepositoryConnection.commit();
         
         return tempRepositoryConnection;
     }
     
 }
