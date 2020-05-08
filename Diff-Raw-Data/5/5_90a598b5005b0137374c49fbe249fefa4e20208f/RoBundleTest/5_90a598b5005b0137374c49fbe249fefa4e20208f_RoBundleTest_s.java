 package pl.psnc.dl.wf4ever.integration.rosrs;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.zip.ZipFile;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpStatus;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.integration.IntegrationTest;
 import pl.psnc.dl.wf4ever.model.RO.RoBundle;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.sun.jersey.api.client.ClientResponse;
 
 /**
  * Integration tests for nested ROs and RO Bundles.
  * 
  * @author piotrekhol
  * 
  */
 @Category(IntegrationTest.class)
 public class RoBundleTest extends RosrsTest {
 
 	/** An annotation body path. */
     //private final String testDatabundlePath = "singleFiles/databundle.zip";
     private final String testDatabundlePath = "singleFiles/musicWorkflowRun5.bundle.zip";
 
 
     /**
      * Test you can add the annotation body.
      * 
      * @throws IOException
      *             when there is a communication problem
      */
     @Test
     public void shouldCreateANestedRO()
             throws IOException {
         try (InputStream is = getClass().getClassLoader().getResourceAsStream(testDatabundlePath)) {
             ClientResponse response = addFile(ro, "bundle.zip", is, RoBundle.MIME_TYPE);
             Assert.assertEquals(response.getEntity(String.class), HttpStatus.SC_CREATED, response.getStatus());
         }
 
         URI nestedRO = findNestedROUri();
         Model nestedModel = ModelFactory.createDefaultModel();
         try (InputStream in = getManifest(nestedRO, RDFFormat.RDFXML)) {
             nestedModel.read(in, null);
         }
         System.out.println("NESTED");
         nestedModel.write(System.out, "TURTLE");
     }
 
 
     /**
      * Find the URI of the nested RO.
      * 
      * @return the URI
      * @throws IOException
      *             when the manifest couldn't be downloaded
      */
     private URI findNestedROUri()
             throws IOException {
         Model parentModel = ModelFactory.createDefaultModel();
         try (InputStream in = getManifest(ro, RDFFormat.RDFXML)) {
             parentModel.read(in, null);
         }
         System.out.println("PARENT");
         parentModel.write(System.out, "TURTLE");
         String queryString = String
                 .format(
                     "SELECT ?nestedRO  WHERE { <%s> <http://www.openarchives.org/ore/terms/aggregates> ?nestedRO . ?nestedRO a <http://purl.org/wf4ever/ro#ResearchObject> . }",
                     ro);
         Query query = QueryFactory.create(queryString);
         QueryExecution qe = QueryExecutionFactory.create(query, parentModel);
         try {
             ResultSet results = qe.execSelect();
             if (!results.hasNext()) {
                 return null;
             }
             QuerySolution solution = results.next();
             RDFNode nestedROR = solution.get("?nestedRO");
             return URI.create(nestedROR.asResource().getURI());
         } finally {
             qe.close();
         }
     }
 
 
     /**
      * A nested RO should be available as an RO bundle.
      * 
      * @throws IOException
      *             when there is a communication problem
      */
     @Test
     public void shouldReturnRoBundleFormat()
             throws IOException {
         try (InputStream is = getClass().getClassLoader().getResourceAsStream(testDatabundlePath)) {
             ClientResponse response = addFile(ro, "bundle.zip", is, RoBundle.MIME_TYPE);
             Assert.assertEquals(response.getEntity(String.class), HttpStatus.SC_CREATED, response.getStatus());
         }
         URI nestedRO = findNestedROUri();
         try (InputStream in = webResource.uri(nestedRO).accept("application/vnd.wf4ever.robundle+zip")
                 .get(InputStream.class)) {
             File file = File.createTempFile("downloaded-bundle", ".zip");
             FileOutputStream outputStream = new FileOutputStream(file);
             IOUtils.copy(in, outputStream);
             try (ZipFile zipFile = new ZipFile(file)) {
                 assertThat(zipFile.size(), greaterThan(0));
             }
         }
     }
 
 
     /**
      * The parent RO should not be available as an RO bundle.
      * 
      * @throws IOException
      *             when there is a communication problem
      */
     @Test
     public void shouldReturn415UnsupportedMediaType()
             throws IOException {
         try (InputStream is = getClass().getClassLoader().getResourceAsStream(testDatabundlePath)) {
             ClientResponse response = addFile(ro, "bundle.zip", is, RoBundle.MIME_TYPE);
             Assert.assertEquals(response.getEntity(String.class), HttpStatus.SC_CREATED, response.getStatus());
         }
         ClientResponse response = webResource.uri(ro).accept("application/vnd.wf4ever.robundle+zip")
                 .get(ClientResponse.class);
         Assert.assertEquals(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
     }
 
 
     /**
      * Test that absolute URIs in annotations are resolved against the data bundle.
      * 
      * @throws IOException
      *             when there is a communication problem
      */
     @Test
     public void shouldResolveAbsoluteURIs()
             throws IOException {
         try (InputStream is = getClass().getClassLoader().getResourceAsStream(testDatabundlePath)) {
             ClientResponse response = addFile(ro, "bundle.zip", is, RoBundle.MIME_TYPE);
             Assert.assertEquals(response.getEntity(String.class), HttpStatus.SC_CREATED, response.getStatus());
         }
 
         URI nestedRO = findNestedROUri();
         Model nestedModel = ModelFactory.createDefaultModel();
         //try (InputStream in = webResource.uri(nestedRO.resolve(".ro/annotations/workflow.link.ttl"))
         try (InputStream in = webResource.uri(nestedRO.resolve(".ro/annotations/5ba47862-b2de-494c-b19f-fa9f82b4ef4f.ttl"))
                 .accept("application/rdf+xml").get(InputStream.class)) {
             nestedModel.read(in, null);
         }
         nestedModel.write(System.out, "TURTLE");
         Resource s = nestedModel
         		.createResource("http://ns.taverna.org.uk/2010/workflowBundle/e3a117b3-ee21-47c3-b25e-d3d85fe817a8/workflow/MusicClassificationExperiment/");
                 //.createResource("http://ns.taverna.org.uk/2010/workflowBundle/e2b20c03-a538-4797-8768-45dbba022644/workflow/MusicClassificationExperiment/");
         Property p = nestedModel.createProperty("http://purl.org/wf4ever/wfdesc#hasWorkflowDefinition");
         RDFNode o = nestedModel.createResource(nestedRO.resolve("workflow.wfbundle").toString());
         Assert.assertTrue(nestedModel.contains(s, p, o));
     }
 
 
     /**
      * Test that the bundle is deaggregated from the parent RO after deleting.
      * 
      * @throws IOException
      *             when there is a communication problem
      */
     @Test
     public void shouldDeleteBundleFromParent()
             throws IOException {
         try (InputStream is = getClass().getClassLoader().getResourceAsStream(testDatabundlePath)) {
             ClientResponse response = addFile(ro, "bundle.zip", is, RoBundle.MIME_TYPE);
             Assert.assertEquals(response.getEntity(String.class), HttpStatus.SC_CREATED, response.getStatus());
         }
 
         URI nestedRO = findNestedROUri();
        webResource.uri(nestedRO).delete();
         Assert.assertNull(findNestedROUri());
     }
 
 
     /**
      * Test that the bundle is deleted when the parent RO is deleted.
      * 
      * @throws IOException
      *             when there is a communication problem
      */
     @Test
     public void shouldDeleteBundleWhenParentIsDeleted()
             throws IOException {
         try (InputStream is = getClass().getClassLoader().getResourceAsStream(testDatabundlePath)) {
             ClientResponse response = addFile(ro, "bundle.zip", is, RoBundle.MIME_TYPE);
             Assert.assertEquals(response.getEntity(String.class), HttpStatus.SC_CREATED, response.getStatus());
         }
 
         URI nestedRO = findNestedROUri();
        webResource.uri(ro).delete();
         ClientResponse response = webResource.uri(nestedRO).get(ClientResponse.class);
         Assert.assertEquals(HttpStatus.SC_GONE, response.getStatus());
     }
 
 }
