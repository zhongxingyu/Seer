 /**
  * 
  */
 package com.github.podd.restlet.test;
 
 import java.io.ByteArrayInputStream;
 import java.nio.charset.StandardCharsets;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import com.github.ansell.restletutils.RestletUtilMediaType;
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * Test various forms of GetArtifact
  * 
  * @author kutila
  * 
  */
 public class GetArtifactResourceImplTest extends AbstractResourceImplTest
 {
     
     // private static final String TEST_ARTIFACT_WITH_1_INTERNAL_OBJECT =
     // "/test/artifacts/basicProject-1-internal-object.rdf";
     
     /**
      * Test access without artifactID parameter gives a BAD_REQUEST error.
      */
     @Test
     public void testErrorGetArtifactWithoutArtifactId() throws Exception
     {
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         try
         {
             getArtifactClientResource.get(MediaType.TEXT_HTML);
             Assert.fail("Should have thrown a ResourceException with Status Code 400");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
         }
     }
     
     /**
      * Test unauthenticated access gives an UNAUTHORIZED error.
      */
     @Test
     public void testErrorGetArtifactWithoutAuthentication() throws Exception
     {
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                 "http://purl.org/podd/ns/artifact/artifact89");
         
         try
         {
             getArtifactClientResource.get(MediaType.TEXT_HTML);
             Assert.fail("Should have thrown a ResourceException with Status Code 401");
         }
         catch(final ResourceException e)
         {
             Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
         }
     }
     
     /**
      * Test authenticated access to get Artifact in HTML
      */
     @Test
     public void testGetArtifactBasicHtml() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify:
         Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
         Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
         
         Assert.assertTrue("Missing: Project Details", body.contains("Project Details"));
         Assert.assertTrue("Missng: ANZSRC FOR Code", body.contains("ANZSRC FOR Code:"));
         Assert.assertTrue("Missng: Project#2012...", body.contains("Project#2012-0006_ Cotton Leaf Morphology"));
         
         this.assertFreemarker(body);
     }
     
     /**
      * Test authenticated access to get Artifact in HTML with a check on the RDFa
      */
     @Test
     public void testGetArtifactBasicHtmlRDFa() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify:
         Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
         Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
         
         Assert.assertTrue("Missing: Project Details", body.contains("Project Details"));
         Assert.assertTrue("Missng: ANZSRC FOR Code", body.contains("ANZSRC FOR Code:"));
         Assert.assertTrue("Missng: Project#2012...", body.contains("Project#2012-0006_ Cotton Leaf Morphology"));
         
         this.assertFreemarker(body);
         
         final Model model =
                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.RDFA, 14);
         
         // RDFa generates spurious triples, use at your own risk
         // Only rely on numbers from actual RDF serialisations
         Assert.assertEquals(7, model.subjects().size());
         Assert.assertEquals(12, model.predicates().size());
        Assert.assertEquals(14, model.objects().size());
         
         if(this.log.isDebugEnabled())
         {
             DebugUtils.printContents(model);
         }
     }
     
     /**
      * Test authenticated access to get Artifact in RDF/JSON
      */
     @Test
     public void testGetArtifactBasicJson() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                         RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         // System.out.println(body);
         
         // verify: received contents are in RDF/JSON
         // Assert.assertTrue("Result does not have @prefix", body.contains("@prefix"));
         
         // verify: received contents have artifact's ontology and version IRIs
         Assert.assertTrue("Result does not contain artifact URI", body.contains(artifactUri));
         
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.RDFJSON, 28);
         
         Assert.assertEquals(5, model.subjects().size());
         Assert.assertEquals(15, model.predicates().size());
         Assert.assertEquals(24, model.objects().size());
         
         if(this.log.isDebugEnabled())
         {
             DebugUtils.printContents(model);
         }
     }
     
     /**
      * Test authenticated access to get Artifact in RDF/XML
      */
     @Test
     public void testGetArtifactBasicRdf() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify: received contents are in RDF
         Assert.assertTrue("Result does not have RDF", body.contains("<rdf:RDF"));
         Assert.assertTrue("Result does not have RDF", body.endsWith("</rdf:RDF>"));
         
         // verify: received contents have artifact URI
         Assert.assertTrue("Result does not contain artifact URI", body.contains(artifactUri));
         
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.RDFXML, 28);
         
         Assert.assertEquals(5, model.subjects().size());
         Assert.assertEquals(15, model.predicates().size());
         Assert.assertEquals(24, model.objects().size());
         
         if(this.log.isDebugEnabled())
         {
             DebugUtils.printContents(model);
         }
     }
     
     /**
      * Test authenticated access to get Artifact in RDF/Turtle
      */
     @Test
     public void testGetArtifactBasicTurtle() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify: received contents are in Turtle
         Assert.assertTrue("Result does not have @prefix", body.contains("@prefix"));
         
         // verify: received contents have artifact's ontology and version IRIs
         Assert.assertTrue("Result does not contain artifact URI", body.contains(artifactUri));
         
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 28);
         
         Assert.assertEquals(5, model.subjects().size());
         Assert.assertEquals(15, model.predicates().size());
         Assert.assertEquals(24, model.objects().size());
         
         if(this.log.isDebugEnabled())
         {
             DebugUtils.printContents(model);
         }
     }
     
     /**
      * Test authenticated access to get an internal podd object in HTML
      */
     @Test
     public void testGetArtifactHtmlAnalysisObject() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
         
         final String objectUri = "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0";
         
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify:
         Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
         Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
         
         Assert.assertTrue("Missing: Analysis Details", body.contains("Analysis Details"));
         Assert.assertTrue("Missng title: poddScience#Analysis 0", body.contains("poddScience#Analysis 0"));
         
         this.assertFreemarker(body);
     }
     
     /**
      * Test authenticated access to get an internal podd object in HTML
      */
     @Test
     public void testGetArtifactHtmlPublicationObject() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri = this.loadTestArtifact("/test/artifacts/basic-2.rdf");
         
         final String objectUri = "urn:hardcoded:purl:artifact:1#publication45";
         final ClientResource getArtifactClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
         
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                         MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         System.out.println(body);
         // verify:
         Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
         Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
         
         Assert.assertTrue("Publication title is missing",
                 body.contains("Towards An Extensible, Domain-agnostic Scientific Data Management System"));
         Assert.assertTrue("#publishedIn value is missing", body.contains("Proceedings of the IEEE eScience 2010"));
         Assert.assertTrue("Publicatin's PURL value is missing",
                 body.contains("http://dx.doi.org/10.1109/eScience.2010.44"));
         
         this.assertFreemarker(body);
     }
     
     /**
      * Test parsing a simple RDFa document
      */
     @Test
     public void testSimpleRDFaParse() throws Exception
     {
         final StringBuilder sb = new StringBuilder();
         
         sb.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">");
         sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"");
         sb.append(" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"");
         sb.append(" version=\"XHTML+RDFa 1.0\">");
         sb.append("<head>");
         sb.append(" <link rel=\"stylesheet\" href=\"http://localhost:8080/test/styles/mystyle.css\" media=\"screen\" type=\"text/css\" />");
         sb.append("</head>");
         sb.append("<body>");
         sb.append("</body>");
         sb.append("</html>");
         this.assertRdf(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)), RDFFormat.RDFA, 1);
     }
     
 }
