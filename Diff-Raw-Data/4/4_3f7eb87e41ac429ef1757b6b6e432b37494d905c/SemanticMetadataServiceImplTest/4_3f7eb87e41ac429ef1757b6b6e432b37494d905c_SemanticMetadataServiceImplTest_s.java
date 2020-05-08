 /**
  * 
  */
 package pl.psnc.dl.wf4ever.sms;
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.naming.NamingException;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.common.HibernateUtil;
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.common.ResourceInfo;
 import pl.psnc.dl.wf4ever.common.UserProfile;
 import pl.psnc.dl.wf4ever.common.UserProfile.Role;
 import pl.psnc.dl.wf4ever.exceptions.ManifestTraversingException;
 import pl.psnc.dl.wf4ever.model.AO.Annotation;
 import pl.psnc.dl.wf4ever.model.ORE.AggregatedResource;
 import pl.psnc.dl.wf4ever.model.RO.Folder;
 import pl.psnc.dl.wf4ever.model.RO.FolderEntry;
 import pl.psnc.dl.wf4ever.vocabulary.AO;
 import pl.psnc.dl.wf4ever.vocabulary.FOAF;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 import pl.psnc.dl.wf4ever.vocabulary.ROEVO;
 
 import com.google.common.collect.Multimap;
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
 import de.fuberlin.wiwiss.ng4j.Quad;
 import de.fuberlin.wiwiss.ng4j.impl.NamedGraphSetImpl;
 
 /**
  * @author piotrhol
  * @author filipwis
  * 
  */
 public class SemanticMetadataServiceImplTest {
 
     @SuppressWarnings("unused")
     private static final Logger log = Logger.getLogger(SemanticMetadataServiceImplTest.class);
 
     private static ResearchObject researchObject;
 
     private static ResearchObject researchObject2URI;
 
     private static ResearchObject snapshotResearchObjectURI;
 
     private static ResearchObject archiveResearchObjectURI;
 
     private static ResearchObject wrongResearchObjectURI;
 
     private static UserProfile userProfile;
 
     private final static URI workflowURI = URI.create("http://example.org/ROs/ro1/a%20workflow.t2flow");
 
     private final static URI workflowPartURI = URI
             .create("http://example.org/ROs/ro1/a%20workflow.t2flow#somePartOfIt");
 
     private final static URI workflow2URI = URI.create("http://example.org/ROs/ro2/runme.t2flow");
 
     private static ResourceInfo workflowInfo;
 
     private final static URI ann1URI = URI.create("http://example.org/ROs/ro1/ann1");
 
     private static ResourceInfo ann1Info;
 
     private final static URI resourceFakeURI = URI.create("http://example.org/ROs/ro1/xyz");
 
     private static ResourceInfo resourceFakeInfo;
 
     private final static URI FOLDER_URI = URI.create("http://example.org/ROs/ro1/afolder/");
 
     private final static URI annotationBody1URI = URI.create("http://example.org/ROs/ro1/.ro/ann1");
 
     private static final String PROJECT_PATH = System.getProperty("user.dir");
 
     private static final String FILE_SEPARATOR = System.getProperty("file.separator");
 
     private TestStructure testStructure;
 
 
     /**
      * @throws java.lang.Exception
      */
     @BeforeClass
     public static void setUpBeforeClass()
             throws Exception {
         HibernateUtil.getSessionFactory().getCurrentSession().beginTransaction();
         researchObject = ResearchObject.create(URI.create("http://example.org/ROs/ro1/"));
         researchObject2URI = ResearchObject.create(URI.create("http://example.org/ROs/ro2/"));
         snapshotResearchObjectURI = ResearchObject.create(URI.create("http://example.org/ROs/sp1/"));
         archiveResearchObjectURI = ResearchObject.create(URI.create("http://example.org/ROs/arch1/"));
         wrongResearchObjectURI = ResearchObject.create(URI.create("http://wrong.example.org/ROs/wrongRo/"));
         userProfile = UserProfile.create("jank", "Jan Kowalski", Role.AUTHENTICATED);
         workflowInfo = ResourceInfo.create("a%20workflow.t2flow", "a%20workflow.t2flow", "ABC123455666344E", 646365L,
             "SHA1", null, "application/vnd.taverna.t2flow+xml");
         ann1Info = ResourceInfo.create("ann1", "ann1", "A0987654321EDCB", 6L, "MD5", null, "application/rdf+xml");
         resourceFakeInfo = ResourceInfo.create("xyz", "xyz", "A0987654321EDCB", 6L, "MD5", null, "text/plain");
     }
 
 
     /**
      * @throws java.lang.Exception
      */
     @AfterClass
     public static void tearDownAfterClass()
             throws Exception {
         cleanData();
         HibernateUtil.getSessionFactory().getCurrentSession().getTransaction().commit();
     }
 
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp()
             throws Exception {
         cleanData();
         testStructure = new TestStructure();
     }
 
 
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown()
             throws Exception {
     }
 
 
     private static void cleanData() {
         SemanticMetadataService sms = null;
         try {
             sms = new SemanticMetadataServiceImpl(userProfile, true);
             try {
                 sms.removeResearchObject(researchObject);
             } catch (IllegalArgumentException e) {
                 // nothing
             }
             try {
                 sms.removeResearchObject(researchObject2URI);
             } catch (IllegalArgumentException e) {
                 // nothing
             }
             try {
                 sms.removeResearchObject(snapshotResearchObjectURI);
             } catch (IllegalArgumentException e) {
                 // nothing
             }
             try {
                 sms.removeResearchObject(archiveResearchObjectURI);
             } catch (IllegalArgumentException e) {
                 // nothing
             }
         } catch (ClassNotFoundException | IOException | NamingException | SQLException e) {
             e.printStackTrace();
         } finally {
             if (sms != null) {
                 sms.close();
             }
         }
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#SemanticMetadataServiceImpl(pl.psnc.dl.wf4ever.dlibra.UserProfile)}
      * .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testSemanticMetadataServiceImpl()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, true);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
         } finally {
             sms.close();
         }
 
         SemanticMetadataService sms2 = new SemanticMetadataServiceImpl(userProfile, true);
         try {
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
 
             model.read(sms2.getManifest(researchObject, RDFFormat.RDFXML), "");
             Individual manifest = model.getIndividual(researchObject.getManifestUri().toString());
             Individual ro = model.getIndividual(researchObject.getUri().toString());
             Assert.assertNotNull("Manifest must contain ro:Manifest", manifest);
             Assert.assertNotNull("Manifest must contain ro:ResearchObject", ro);
             Assert.assertTrue("Manifest must be a ro:Manifest", manifest.hasRDFType(RO.NAMESPACE + "Manifest"));
             Assert.assertTrue("RO must be a ro:ResearchObject", ro.hasRDFType(RO.NAMESPACE + "ResearchObject"));
 
             Literal createdLiteral = manifest.getPropertyValue(DCTerms.created).asLiteral();
             Assert.assertNotNull("Manifest must contain dcterms:created", createdLiteral);
 
             //			Resource creatorResource = manifest.getPropertyResourceValue(DCTerms.creator);
             //			Assert.assertNotNull("Manifest must contain dcterms:creator", creatorResource);
         } finally {
             sms2.close();
         }
     }
 
 
     /**
      * Test method for {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#createResearchObject(java.net.URI)} .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testCreateResearchObject()
             throws IOException {
         testStructure.sms.createResearchObject(researchObject);
         try {
             testStructure.sms.createResearchObject(researchObject);
             fail("Should throw an exception");
         } catch (IllegalArgumentException e) {
             // good
         }
 
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#updateManifest(java.net.URI, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testUpdateManifest()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, resourceFakeURI, resourceFakeInfo);
 
             InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
             sms.updateManifest(researchObject, is, RDFFormat.TURTLE);
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getManifest(researchObject, RDFFormat.RDFXML), null);
 
             Individual manifest = model.getIndividual(researchObject.getManifestUri().toString());
             Individual ro = model.getIndividual(researchObject.getUri().toString());
 
             Assert.assertEquals("Manifest created has been updated", "2011-12-02T16:01:10Z",
                 manifest.getPropertyValue(DCTerms.created).asLiteral().getString());
             Assert.assertEquals("RO created has been updated", "2011-12-02T15:01:10Z",
                 ro.getPropertyValue(DCTerms.created).asLiteral().getString());
 
             Set<String> creators = new HashSet<String>();
             String creatorsQuery = String.format("PREFIX dcterms: <%s> PREFIX foaf: <%s> SELECT ?name "
                     + "WHERE { <%s> dcterms:creator ?x . ?x foaf:name ?name . }", DCTerms.NS,
                 "http://xmlns.com/foaf/0.1/", researchObject.getUri().toString());
             Query query = QueryFactory.create(creatorsQuery);
             QueryExecution qexec = QueryExecutionFactory.create(query, model);
             try {
                 ResultSet results = qexec.execSelect();
                 while (results.hasNext()) {
                     creators.add(results.nextSolution().getLiteral("name").getString());
                 }
             } finally {
                 qexec.close();
             }
 
             Assert.assertTrue("New creator has been added", creators.contains("Stian Soiland-Reyes"));
             Assert.assertTrue("Old creator has been deleted", !creators.contains(userProfile.getName()));
 
             Assert.assertTrue("RO must aggregate resources",
                 model.contains(ro, ORE.aggregates, model.createResource(workflowURI.toString())));
             Assert.assertTrue("RO must aggregate resources",
                 model.contains(ro, ORE.aggregates, model.createResource(ann1URI.toString())));
             Assert.assertTrue("RO must not aggregate previous resources",
                 !model.contains(ro, ORE.aggregates, model.createResource(resourceFakeURI.toString())));
             validateProxy(model, manifest, researchObject.getUri().toString() + "proxy1", workflowURI.toString());
         } finally {
             sms.close();
         }
     }
 
 
     private void validateProxy(OntModel model, Individual manifest, String proxyURI, String proxyForURI) {
         Individual proxy = model.getIndividual(proxyURI);
         Assert.assertNotNull("Manifest must contain " + proxyURI, proxy);
         Assert.assertTrue(String.format("Proxy %s must be a ore:Proxy", proxyURI),
             proxy.hasRDFType("http://www.openarchives.org/ore/terms/Proxy"));
         Assert.assertEquals("Proxy for must be valid", proxyForURI, proxy.getPropertyResourceValue(ORE.proxyFor)
                 .getURI());
         Assert.assertEquals("Proxy in must be valid", researchObject.getUri().toString(), proxy
                 .getPropertyResourceValue(ORE.proxyIn).getURI());
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#removeResearchObject(java.net.URI, java.net.URI)} .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testRemoveManifest()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
             InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
 
             sms.removeResearchObject(researchObject);
 
             //Should not throw an exception
             sms.removeResearchObject(researchObject);
 
             Assert.assertNotNull("Get other named graph must not return null",
                 sms.getNamedGraph(annotationBody1URI, RDFFormat.RDFXML));
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getManifest(java.net.URI, org.openrdf.rio.RDFFormat)} .
      * 
      * @throws IOException
      * @throws SQLException
      * @throws NamingException
      * @throws ClassNotFoundException
      * @throws URISyntaxException
      */
     @Test
     public final void testGetManifest()
             throws IOException, ClassNotFoundException, NamingException, SQLException, URISyntaxException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             Assert.assertNull("Returns null when manifest does not exist",
                 sms.getManifest(researchObject, RDFFormat.RDFXML));
 
             Calendar before = Calendar.getInstance();
             sms.createResearchObject(researchObject);
             Calendar after = Calendar.getInstance();
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
             model.read(sms.getManifest(researchObject, RDFFormat.RDFXML), null);
 
             Individual manifest = model.getIndividual(researchObject.getManifestUri().toString());
             Individual ro = model.getIndividual(researchObject.getUri().toString());
             Assert.assertNotNull("Manifest must contain ro:Manifest", manifest);
             Assert.assertNotNull("Manifest must contain ro:ResearchObject", ro);
             Assert.assertTrue("Manifest must be a ro:Manifest", manifest.hasRDFType(RO.NAMESPACE + "Manifest"));
             Assert.assertTrue("RO must be a ro:ResearchObject", ro.hasRDFType(RO.NAMESPACE + "ResearchObject"));
 
             Literal createdLiteral = manifest.getPropertyValue(DCTerms.created).asLiteral();
             Assert.assertNotNull("Manifest must contain dcterms:created", createdLiteral);
             Assert.assertEquals("Date type is xsd:dateTime", XSDDatatype.XSDdateTime, createdLiteral.getDatatype());
             Calendar created = ((XSDDateTime) createdLiteral.getValue()).asCalendar();
             Assert.assertTrue("Created is a valid date", !before.after(created) && !after.before(created));
 
             //			Resource creatorResource = manifest.getPropertyResourceValue(DCTerms.creator);
             //			Assert.assertNotNull("Manifest must contain dcterms:creator", creatorResource);
             //			Individual creator = creatorResource.as(Individual.class);
             //			Assert.assertTrue("Creator must be a foaf:Agent", creator.hasRDFType("http://xmlns.com/foaf/0.1/Agent"));
             //			Assert.assertEquals("Creator name must be correct", "RODL", creator.getPropertyValue(foafName).asLiteral()
             //					.getString());
 
             Resource creatorResource = ro.getPropertyResourceValue(DCTerms.creator);
             Assert.assertNotNull("RO must contain dcterms:creator", creatorResource);
 
             OntModel userModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             userModel.read(sms.getNamedGraph(new URI(creatorResource.getURI()), RDFFormat.RDFXML), "");
 
             Individual creator = userModel.getIndividual(creatorResource.getURI());
             Assert.assertTrue("Creator must be a foaf:Agent", creator.hasRDFType("http://xmlns.com/foaf/0.1/Agent"));
             Assert.assertEquals("Creator name must be correct", userProfile.getName(),
                 creator.getPropertyValue(FOAF.name).asLiteral().getString());
 
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getManifest(java.net.URI, org.openrdf.rio.RDFFormat)} .
      * 
      * @throws IOException
      * @throws SQLException
      * @throws NamingException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testGetManifestWithAnnotationBodies()
             throws IOException, ClassNotFoundException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             Assert.assertNull("Returns null when manifest does not exist",
                 sms.getManifest(researchObject, RDFFormat.TRIG));
 
             InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
             sms.updateManifest(researchObject, is, RDFFormat.TURTLE);
             is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
 
             NamedGraphSet graphset = new NamedGraphSetImpl();
             graphset.read(sms.getManifest(researchObject, RDFFormat.TRIG), "TRIG", null);
 
             Quad sampleAgg = new Quad(Node.createURI(researchObject.getManifestUri().toString()),
                     Node.createURI(researchObject.getUri().toString()), Node.createURI(ORE.aggregates.getURI()),
                     Node.createURI(workflowURI.toString()));
             Assert.assertTrue("Contains a sample aggregation", graphset.containsQuad(sampleAgg));
 
             Quad sampleAnn = new Quad(Node.createURI(annotationBody1URI.toString()), Node.createURI(workflowURI
                     .toString()), Node.createURI("http://purl.org/dc/terms/license"), Node.createLiteral("GPL"));
             Assert.assertTrue("Contains a sample annotation", graphset.containsQuad(sampleAnn));
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#addResource(java.net.URI, java.net.URI, pl.psnc.dl.wf4ever.dlibra.ResourceInfo)}
      * .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testAddResource()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             Assert.assertTrue(sms.addResource(researchObject, workflowURI, workflowInfo));
             Assert.assertTrue(sms.addResource(researchObject, ann1URI, ann1Info));
             Assert.assertFalse(sms.addResource(researchObject, workflowURI, null));
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#removeResource(java.net.URI, java.net.URI)} .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testRemoveResource()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
             sms.removeResource(researchObject, workflowURI);
             // no longer throws exceptions
             sms.removeResource(researchObject, workflowURI);
             sms.removeResource(researchObject, ann1URI);
 
             InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
             sms.updateManifest(researchObject, is, RDFFormat.TURTLE);
             sms.removeResource(researchObject, workflowURI);
             Assert.assertNull("There should be no annotation body after a resource is deleted",
                 sms.getNamedGraph(annotationBody1URI, RDFFormat.RDFXML));
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
             model.read(sms.getManifest(researchObject, RDFFormat.RDFXML), null);
             Assert.assertFalse(model.listStatements(null, null, model.createResource(ann1URI.toString())).hasNext());
             Assert.assertFalse(model.listStatements(model.createResource(ann1URI.toString()), null, (RDFNode) null)
                     .hasNext());
 
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getResource(java.net.URI, org.openrdf.rio.RDFFormat)} .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      * @throws URISyntaxException
      */
     @Test
     public final void testGetResource()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             Assert.assertNull("Returns null when resource does not exist",
                 sms.getResource(researchObject, workflowURI, RDFFormat.RDFXML));
 
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
             InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getResource(researchObject, workflowURI, RDFFormat.RDFXML), researchObject.getUri()
                     .toString());
             verifyResource(sms, model, workflowURI, workflowInfo);
             verifyTriple(model, workflowURI, URI.create("http://purl.org/dc/terms/title"), "A test");
             verifyTriple(model, workflowURI, URI.create("http://purl.org/dc/terms/title"), "An alternative title");
             verifyTriple(model, workflowURI, URI.create("http://purl.org/dc/terms/license"), "GPL");
 
             model.read(sms.getResource(researchObject, ann1URI, RDFFormat.TURTLE), null, "TTL");
             verifyResource(sms, model, ann1URI, ann1Info);
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * @param model
      * @param resourceInfo
      * @param resourceURI
      * @throws URISyntaxException
      */
     private void verifyResource(SemanticMetadataService sms, OntModel model, URI resourceURI, ResourceInfo resourceInfo)
             throws URISyntaxException {
         Individual resource = model.getIndividual(resourceURI.toString());
         Assert.assertNotNull("Resource cannot be null", resource);
         Assert.assertTrue(String.format("Resource %s must be a ro:Resource", resourceURI),
             resource.hasRDFType(RO.NAMESPACE + "Resource"));
 
         RDFNode createdLiteral = resource.getPropertyValue(DCTerms.created);
         Assert.assertNotNull("Resource must contain dcterms:created", createdLiteral);
         Assert.assertEquals("Date type is xsd:dateTime", XSDDatatype.XSDdateTime, createdLiteral.asLiteral()
                 .getDatatype());
 
         Resource creatorResource = resource.getPropertyResourceValue(DCTerms.creator);
         Assert.assertNotNull("Resource must contain dcterms:creator", creatorResource);
 
         OntModel userModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         userModel.read(sms.getNamedGraph(new URI(creatorResource.getURI()), RDFFormat.RDFXML), "");
         Individual creator = userModel.getIndividual(creatorResource.getURI());
         Assert.assertNotNull("User named graph must contain dcterms:creator", creator);
         Assert.assertTrue("Creator must be a foaf:Agent", creator.hasRDFType("http://xmlns.com/foaf/0.1/Agent"));
         Assert.assertEquals("Creator name must be correct", userProfile.getName(), creator.getPropertyValue(FOAF.name)
                 .asLiteral().getString());
 
         Literal nameLiteral = resource.getPropertyValue(RO.name).asLiteral();
         Assert.assertNotNull("Resource must contain ro:name", nameLiteral);
         Assert.assertEquals("Name type is xsd:string", XSDDatatype.XSDstring, nameLiteral.getDatatype());
         Assert.assertEquals("Name is valid", resourceInfo.getName(), nameLiteral.asLiteral().getString());
 
         Literal filesizeLiteral = resource.getPropertyValue(RO.filesize).asLiteral();
         Assert.assertNotNull("Resource must contain ro:filesize", filesizeLiteral);
         Assert.assertEquals("Filesize type is xsd:long", XSDDatatype.XSDlong, filesizeLiteral.getDatatype());
         Assert.assertEquals("Filesize is valid", resourceInfo.getSizeInBytes(), filesizeLiteral.asLiteral().getLong());
 
         Resource checksumResource = resource.getPropertyValue(RO.checksum).asResource();
         Assert.assertNotNull("Resource must contain ro:checksum", checksumResource);
         URI checksumURN = new URI(checksumResource.getURI());
         Pattern p = Pattern.compile("urn:(\\w+):([0-9a-fA-F]+)");
         Matcher m = p.matcher(checksumURN.toString());
         Assert.assertTrue("Checksum can be parsed", m.matches());
         Assert.assertEquals("Digest method is correct", resourceInfo.getDigestMethod(), m.group(1));
         Assert.assertEquals("Checksum is correct", resourceInfo.getChecksum(), m.group(2));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getNamedGraph(java.net.URI, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testGetNamedGraph()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
             InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getNamedGraph(annotationBody1URI, RDFFormat.TURTLE), null, "TTL");
 
             verifyTriple(model, workflowURI, URI.create("http://purl.org/dc/terms/title"), "A test");
             verifyTriple(model, workflowURI, URI.create("http://purl.org/dc/terms/license"), "GPL");
             verifyTriple(model, URI.create("http://workflows.org/a%20workflow.scufl"),
                 URI.create("http://purl.org/dc/terms/description"), "Something interesting");
             verifyTriple(model, workflowPartURI, URI.create("http://purl.org/dc/terms/description"), "The key part");
         } finally {
             sms.close();
         }
     }
 
 
     private void verifyTriple(Model model, URI subjectURI, URI propertyURI, String object) {
         Resource subject = model.createResource(subjectURI.toString());
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 
     private void verifyTriple(Model model, String subjectURI, URI propertyURI, String object) {
         Resource subject = model.createResource(subjectURI);
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 
     private void verifyTriple(Model model, String subjectURI, URI propertyURI, Resource object) {
         Resource subject = model.createResource(subjectURI);
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#findResearchObjectsByPrefix(java.net.URI)} .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testFindManifests()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.createResearchObject(researchObject2URI);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
 
             Set<URI> result = sms.findResearchObjectsByPrefix(researchObject.getUri().resolve(".."));
             Assert.assertTrue("Find with base of RO", result.contains(researchObject.getUri()));
             Assert.assertTrue("Find with base of RO", result.contains(researchObject2URI.getUri()));
 
             result = sms.findResearchObjectsByPrefix(wrongResearchObjectURI.getUri().resolve(".."));
             Assert.assertFalse("Not find with the wrong base", result.contains(researchObject.getUri()));
             Assert.assertFalse("Not find with the wrong base", result.contains(researchObject2URI.getUri()));
 
             result = sms.findResearchObjectsByCreator(userProfile.getUri());
             Assert.assertTrue("Find by creator of RO", result.contains(researchObject.getUri()));
             Assert.assertTrue("Find by creator of RO", result.contains(researchObject2URI.getUri()));
 
             result = sms.findResearchObjectsByCreator(wrongResearchObjectURI.getUri());
             Assert.assertFalse("Not find by the wrong creator", result.contains(researchObject.getUri()));
             Assert.assertFalse("Not find by the wrong creator", result.contains(researchObject2URI.getUri()));
 
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#isRoFolder(java.net.URI)} .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testIsRoFolder()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
 
             InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
             sms.updateManifest(researchObject, is, RDFFormat.TURTLE);
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getManifest(researchObject, RDFFormat.RDFXML), null);
 
             Assert.assertTrue("<afolder> is an ro:Folder", sms.isRoFolder(researchObject, FOLDER_URI));
             Assert.assertTrue("<ann1> is not an ro:Folder", !sms.isRoFolder(researchObject, ann1URI));
             Assert.assertTrue("Fake resource is not an ro:Folder", !sms.isRoFolder(researchObject, resourceFakeURI));
             Assert.assertTrue("<afolder> is not an ro:Folder according to other RO",
                 !sms.isRoFolder(researchObject2URI, FOLDER_URI));
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#addNamedGraph(java.net.URI, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testAddNamedGraph()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
             InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             Assert.assertTrue(sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE));
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#isROMetadataNamedGraph(java.net.URI)} .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testIsROMetadataNamedGraph()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
             sms.updateManifest(researchObject, is, RDFFormat.TURTLE);
 
             Assert.assertTrue("Only mentioned annotation body is an RO metadata named graph",
                 sms.isROMetadataNamedGraph(researchObject, annotationBody1URI));
 
             is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
             is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI.resolve("fake"), is, RDFFormat.TURTLE);
 
             Assert.assertTrue("Annotation body is an RO metadata named graph",
                 sms.isROMetadataNamedGraph(researchObject, annotationBody1URI));
             Assert.assertTrue("An random named graph is not an RO metadata named graph",
                 !sms.isROMetadataNamedGraph(researchObject, annotationBody1URI.resolve("fake")));
             Assert.assertTrue("Manifest is an RO metadata named graph",
                 sms.isROMetadataNamedGraph(researchObject, researchObject.getManifestUri()));
             Assert.assertTrue("A resource is not an RO metadata named graph",
                 !sms.isROMetadataNamedGraph(researchObject, workflowURI));
             Assert.assertTrue("A fake resource is not an RO metadata named graph",
                 !sms.isROMetadataNamedGraph(researchObject, resourceFakeURI));
         } finally {
             sms.close();
         }
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#removeNamedGraph(java.net.URI, java.net.URI)} .
      * 
      * @throws SQLException
      * @throws NamingException
      * @throws IOException
      * @throws ClassNotFoundException
      */
     @Test
     public final void testRemoveNamedGraph()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
             InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
             Assert.assertNotNull("A named graph exists", sms.getNamedGraph(annotationBody1URI, RDFFormat.RDFXML));
             sms.removeNamedGraph(researchObject, annotationBody1URI);
             Assert.assertNull("A deleted named graph no longer exists",
                 sms.getNamedGraph(annotationBody1URI, RDFFormat.RDFXML));
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testExecuteSparql()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
             sms.updateManifest(researchObject, is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
 
             String describeQuery = String.format("DESCRIBE <%s>", workflowURI.toString());
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             QueryResult res = sms.executeSparql(describeQuery, RDFFormat.RDFXML);
             model.read(res.getInputStream(), null, "RDF/XML");
             Individual resource = model.getIndividual(workflowURI.toString());
             Assert.assertNotNull("Resource cannot be null", resource);
             Assert.assertTrue(String.format("Resource %s must be a ro:Resource", workflowURI),
                 resource.hasRDFType(RO.NAMESPACE + "Resource"));
 
             is = getClass().getClassLoader().getResourceAsStream("direct-annotations-construct.sparql");
             String constructQuery = IOUtils.toString(is, "UTF-8");
             model.removeAll();
             model.read(sms.executeSparql(constructQuery, RDFFormat.RDFXML).getInputStream(), null, "RDF/XML");
             Assert.assertTrue("Construct contains triple 1",
                 model.contains(model.createResource(workflowURI.toString()), DCTerms.title, "A test"));
             Assert.assertTrue("Construct contains triple 2",
                 model.contains(model.createResource(workflowURI.toString()), DCTerms.license, "GPL"));
 
             is = getClass().getClassLoader().getResourceAsStream("direct-annotations-select.sparql");
             String selectQuery = IOUtils.toString(is, "UTF-8");
             String xml = IOUtils.toString(sms.executeSparql(selectQuery, SemanticMetadataService.SPARQL_XML)
                     .getInputStream(), "UTF-8");
             // FIXME make more in-depth XML validation
             Assert.assertTrue("XML looks correct", xml.contains("Marco Roos"));
 
             String json = IOUtils.toString(sms.executeSparql(selectQuery, SemanticMetadataService.SPARQL_JSON)
                     .getInputStream(), "UTF-8");
             // FIXME make more in-depth JSON validation
             Assert.assertTrue("JSON looks correct", json.contains("Marco Roos"));
 
             is = getClass().getClassLoader().getResourceAsStream("direct-annotations-ask-true.sparql");
             String askTrueQuery = IOUtils.toString(is, "UTF-8");
             xml = IOUtils.toString(
                 sms.executeSparql(askTrueQuery, SemanticMetadataService.SPARQL_XML).getInputStream(), "UTF-8");
             Assert.assertTrue("XML looks correct", xml.contains("true"));
 
             is = getClass().getClassLoader().getResourceAsStream("direct-annotations-ask-false.sparql");
             String askFalseQuery = IOUtils.toString(is, "UTF-8");
             xml = IOUtils.toString(sms.executeSparql(askFalseQuery, SemanticMetadataService.SPARQL_XML)
                     .getInputStream(), "UTF-8");
             Assert.assertTrue("XML looks correct", xml.contains("false"));
 
             RDFFormat jpeg = new RDFFormat("JPEG", "image/jpeg", Charset.forName("UTF-8"), "jpeg", false, false);
             res = sms.executeSparql(describeQuery, jpeg);
             Assert.assertEquals("RDF/XML is the default format", RDFFormat.RDFXML, res.getFormat());
             res = sms.executeSparql(constructQuery, jpeg);
             Assert.assertEquals("RDF/XML is the default format", RDFFormat.RDFXML, res.getFormat());
             res = sms.executeSparql(selectQuery, jpeg);
             Assert.assertEquals("SPARQL XML is the default format", SemanticMetadataService.SPARQL_XML, res.getFormat());
             res = sms.executeSparql(askTrueQuery, jpeg);
             Assert.assertEquals("SPARQL XML is the default format", SemanticMetadataService.SPARQL_XML, res.getFormat());
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testGetAllAttributes()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
             sms.updateManifest(researchObject, is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
 
             Multimap<URI, Object> atts = sms.getAllAttributes(workflowURI);
             Assert.assertEquals(6, atts.size());
             Assert.assertTrue("Attributes contain type",
                 atts.containsValue(URI.create("http://purl.org/wf4ever/ro#Resource")));
             Assert.assertTrue("Attributes contain created", atts.get(URI.create(DCTerms.created.toString())).iterator()
                     .next() instanceof Calendar);
             Assert.assertTrue("Attributes contain title",
                 atts.get(URI.create(DCTerms.title.toString())).contains("A test"));
             Assert.assertTrue("Attributes contain title2",
                 atts.get(URI.create(DCTerms.title.toString())).contains("An alternative title"));
             Assert.assertTrue("Attributes contain licence",
                 atts.get(URI.create(DCTerms.license.toString())).contains("GPL"));
             Assert.assertTrue("Attributes contain creator",
                 atts.get(URI.create(DCTerms.creator.toString())).contains("Stian Soiland-Reyes"));
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public void testGetNamedGraphWithRelativeURIs()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, ann1URI, ann1Info);
             InputStream is = getClass().getClassLoader().getResourceAsStream("annotationBody2.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getNamedGraphWithRelativeURIs(annotationBody1URI, researchObject, RDFFormat.RDFXML), "",
                 "RDF/XML");
 
             //FIXME this does not work correctly, for some reason ".." is stripped when reading the model
             verifyTriple(model, /* "../a_workflow.t2flow" */"a%20workflow.t2flow",
                 URI.create("http://purl.org/dc/terms/title"), "A test");
             verifyTriple(model, /* "../a_workflow.t2flow" */"a%20workflow.t2flow",
                 URI.create("http://purl.org/dc/terms/source"), model.createResource(workflow2URI.toString()));
             verifyTriple(model, new URI("manifest.rdf"), URI.create("http://purl.org/dc/terms/license"), "GPL");
             verifyTriple(model, URI.create("http://workflows.org/a%20workflow.scufl"),
                 URI.create("http://purl.org/dc/terms/description"), "Something interesting");
             verifyTriple(model, /* "../a_workflow.t2flow#somePartOfIt" */"a%20workflow.t2flow#somePartOfIt",
                 URI.create("http://purl.org/dc/terms/description"), "The key part");
         } finally {
             sms.close();
         }
 
     }
 
 
     @Test
     public final void testGetRemoveUser()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             OntModel userModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             userModel.read(sms.getUser(userProfile.getUri(), RDFFormat.RDFXML).getInputStream(), "", "RDF/XML");
             Individual creator = userModel.getIndividual(userProfile.getUri().toString());
             Assert.assertNotNull("User named graph must contain dcterms:creator", creator);
             Assert.assertTrue("Creator must be a foaf:Agent", creator.hasRDFType("http://xmlns.com/foaf/0.1/Agent"));
             Assert.assertEquals("Creator name must be correct", userProfile.getName(),
                 creator.getPropertyValue(FOAF.name).asLiteral().getString());
 
             sms.removeUser(userProfile.getUri());
             userModel.removeAll();
             userModel.read(sms.getUser(userProfile.getUri(), RDFFormat.RDFXML).getInputStream(), "", "RDF/XML");
             creator = userModel.getIndividual(userProfile.getUri().toString());
             Assert.assertNull("User named graph must not contain dcterms:creator", creator);
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testIsAggregatedResource()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             Assert.assertTrue("Is aggregated", sms.isAggregatedResource(researchObject, workflowURI));
             Assert.assertFalse("Is not aggregated", sms.isAggregatedResource(researchObject, workflow2URI));
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testIsAnnotation()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             URI ann = sms.addAnnotation(researchObject, Arrays.asList(workflowURI), annotationBody1URI);
             Assert.assertTrue("Annotation is an annotation", sms.isAnnotation(researchObject, ann));
             Assert.assertFalse("Workflow is not an annotation", sms.isAnnotation(researchObject, workflowURI));
             Assert.assertFalse("2nd workflow is not an annotation", sms.isAnnotation(researchObject, workflow2URI));
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testAddAnnotation()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             URI ann = sms.addAnnotation(researchObject, Arrays.asList(workflowURI, workflow2URI), annotationBody1URI);
             Assert.assertNotNull("Ann URI is not null", ann);
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getManifest(researchObject, RDFFormat.RDFXML), null);
             Resource researchObjectR = model.getResource(researchObject.getUri().toString());
             Resource annotation = model.getResource(ann.toString());
             Resource workflow = model.getResource(workflowURI.toString());
             Resource workflow2 = model.getResource(workflow2URI.toString());
             Resource abody = model.getResource(annotationBody1URI.toString());
 
             Assert.assertTrue(model.contains(researchObjectR, ORE.aggregates, annotation));
             Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, workflow));
             Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, workflow2));
             Assert.assertTrue(model.contains(annotation, AO.body, abody));
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testUpdateAnnotation()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             URI ann = sms.addAnnotation(researchObject, Arrays.asList(workflowURI, workflow2URI), annotationBody1URI);
             sms.updateAnnotation(researchObject, ann, Arrays.asList(workflowURI, researchObject.getUri()), workflow2URI);
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getManifest(researchObject, RDFFormat.RDFXML), null);
             Resource researchObjectR = model.getResource(researchObject.getUri().toString());
             Resource annotation = model.getResource(ann.toString());
             Resource workflow = model.getResource(workflowURI.toString());
             Resource workflow2 = model.getResource(workflow2URI.toString());
             Resource abody = model.getResource(annotationBody1URI.toString());
 
             Assert.assertTrue(model.contains(researchObjectR, ORE.aggregates, annotation));
             Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, workflow));
             Assert.assertFalse(model.contains(annotation, RO.annotatesAggregatedResource, workflow2));
             Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, researchObjectR));
             Assert.assertFalse(model.contains(annotation, AO.body, abody));
             Assert.assertTrue(model.contains(annotation, AO.body, workflow2));
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testGetAnnotationBody()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             URI ann = sms.addAnnotation(researchObject, Arrays.asList(workflowURI, workflow2URI), annotationBody1URI);
             URI annBody = sms.getAnnotationBody(researchObject, ann);
             Assert.assertEquals("Annotation body retrieved correctly", annotationBody1URI, annBody);
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testDeleteAnnotation()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             URI ann = sms.addAnnotation(researchObject, Arrays.asList(workflowURI, workflow2URI), annotationBody1URI);
             sms.deleteAnnotation(researchObject, ann);
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getManifest(researchObject, RDFFormat.RDFXML), null);
             Resource annotation = model.createResource(ann.toString());
             Assert.assertFalse("No annotation statements", model.listStatements(annotation, null, (RDFNode) null)
                     .hasNext());
             Assert.assertFalse("No annotation statements", model.listStatements(null, null, annotation).hasNext());
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testIsSnapshot()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         Assert.assertTrue("snapshot does not recognized", testStructure.sms.isSnapshot(testStructure.sp1));
         Assert.assertFalse("snapshot wrongly recognized", testStructure.sms.isSnapshot(testStructure.ro1));
         Assert.assertFalse("snapshot wrongly recognized", testStructure.sms.isSnapshot(testStructure.arch1));
     }
 
 
     @Test
     public final void testIsArchive()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         Assert.assertTrue("archive does not recognized", testStructure.sms.isArchive(testStructure.arch1));
         Assert.assertFalse("archive does not recognized", testStructure.sms.isArchive(testStructure.ro1));
         Assert.assertFalse("archive does not recognized", testStructure.sms.isArchive(testStructure.sp1));
     }
 
 
     @Test
     public final void testGetPreviousSnaphotOrArchive()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         URI sp1Antecessor = testStructure.sms.getPreviousSnaphotOrArchive(testStructure.ro1, testStructure.sp1);
         URI sp2Antecessor = testStructure.sms.getPreviousSnaphotOrArchive(testStructure.ro1, testStructure.sp2);
         Assert.assertNull("wrong antecessor URI", sp1Antecessor);
         Assert.assertEquals("wrong antecessor URI", sp2Antecessor, testStructure.sp1.getUri());
     }
 
 
     @Test
     public final void testGetIndividual()
             throws URISyntaxException, ClassNotFoundException, IOException, NamingException, SQLException {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(getResourceURI("ro1/").resolve(".ro/manifest.ttl").toString(), "TTL");
         model.read(getResourceURI("ro1/").resolve(".ro/evo_info.ttl").toString(), "TTL");
         Individual source = model.getIndividual(getResourceURI("ro1/").toString());
         Individual source2 = testStructure.sms.getIndividual(testStructure.ro1);
         Assert.assertEquals("wrong individual returned", source, source2);
     }
 
 
     @Test
     public final void testGetLiveROfromSnapshotOrArchive()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         URI liveFromRO = testStructure.sms.getLiveURIFromSnapshotOrArchive(testStructure.ro1);
         URI liveFromSP = testStructure.sms.getLiveURIFromSnapshotOrArchive(testStructure.sp1);
         URI liveFromARCH = testStructure.sms.getLiveURIFromSnapshotOrArchive(testStructure.arch1);
         Assert.assertNull("live RO does not have a parent RO", liveFromRO);
         Assert.assertEquals("wrong parent URI", liveFromSP, testStructure.ro1.getUri());
         Assert.assertEquals("wrong parent URI", liveFromARCH, testStructure.ro1.getUri());
     }
 
 
     @Test
     public final void testChangeURIInManifestAndAnnotationBodies()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             InputStream is = getClass().getClassLoader().getResourceAsStream("manifest.ttl");
             sms.updateManifest(researchObject, is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("annotationBody.ttl");
             sms.addNamedGraph(annotationBody1URI, is, RDFFormat.TURTLE);
 
             int cnt = sms.changeURIInManifestAndAnnotationBodies(researchObject, workflowURI, resourceFakeURI);
            // 1 aggregates, 1 ann target, 1 type, 2 dcterms, 3 in ann body
            Assert.assertEquals("6 URIs should be changed", 9, cnt);
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testSMSConstructor()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         URI fakeURI = new URI("http://www.example.com/ROs/");
         File file = new File(PROJECT_PATH + "/src/test/resources/manifest.rdf");
         FileInputStream is = new FileInputStream(file);
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, ResearchObject.create(fakeURI), is,
                 RDFFormat.RDFXML);
         try {
             String manifest = IOUtils.toString(sms.getManifest(
                 ResearchObject.create(new URI("http://www.example.com/ROs/")), RDFFormat.RDFXML));
             Assert.assertTrue(manifest.contains("http://www.example.com/ROs/"));
             Assert.assertTrue(manifest.contains("Marco Roos"));
             Assert.assertTrue(manifest.contains("t2flow workflow annotation extractor"));
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void getAggregatedResources()
             throws URISyntaxException, FileNotFoundException, ManifestTraversingException {
         URI fakeURI = new URI("http://www.example.com/ROs/");
         File file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1/.ro/manifest.ttl");
         FileInputStream is = new FileInputStream(file);
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, ResearchObject.create(fakeURI), is,
                 RDFFormat.TURTLE); //fill up data
 
         try {
             List<AggregatedResource> list = sms.getAggregatedResources(ResearchObject.create(fakeURI));
             Assert.assertTrue(list.contains(new AggregatedResource(fakeURI.resolve("res1"))));
             Assert.assertTrue(list.contains(new AggregatedResource(fakeURI.resolve("res2"))));
             Assert.assertTrue(list.contains(new AggregatedResource(fakeURI.resolve("afinalfolder"))));
             Assert.assertFalse(list.contains(new AggregatedResource(fakeURI.resolve("ann1"))));
 
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void getAnnotations()
             throws FileNotFoundException, URISyntaxException, ManifestTraversingException {
         URI fakeURI = new URI("http://www.example.com/ROs/");
         File file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1/.ro/manifest.ttl");
         FileInputStream is = new FileInputStream(file);
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, ResearchObject.create(fakeURI), is,
                 RDFFormat.TURTLE);
         List<Annotation> list = sms.getAnnotations(ResearchObject.create(fakeURI));
         list.get(0).getUri().equals(fakeURI.resolve("ann1"));
         //@TODO check if body and related resources are these same
         try {
         } finally {
             sms.close();
         }
     }
 
 
     //not a real test.
     //@Test
     public final void generateRDF()
             throws URISyntaxException, IOException {
         URI fakeURI = new URI("http://www.example.com/ROs/");
         File file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1/.ro/manifest.ttl");
         FileInputStream is = new FileInputStream(file);
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, ResearchObject.create(fakeURI), is,
                 RDFFormat.TURTLE);
         try {
             ResearchObject researchObject = ResearchObject.create(fakeURI);
             System.out.println(IOUtils.toString(sms.getNamedGraphWithRelativeURIs(researchObject.getManifestUri(),
                 researchObject, RDFFormat.RDFXML)));
         } finally {
             sms.close();
         }
     }
 
 
     @Test
     public final void testROevo()
             throws URISyntaxException, IOException {
 
         System.out.println(testStructure.sms.storeAggregatedDifferences(testStructure.sp2, testStructure.sp1));
 
         Individual evoInfoSource = testStructure.sms.getIndividual(testStructure.sp2);
         List<RDFNode> nodes = evoInfoSource.getPropertyValue(ROEVO.wasChangedBy).as(Individual.class)
                 .listPropertyValues(ROEVO.hasChange).toList();
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(testStructure.sms.getNamedGraph(testStructure.sp2.getManifestUri(), RDFFormat.RDFXML), null);
         model.read(
             testStructure.sms.getNamedGraph(testStructure.sp2.getFixedEvolutionAnnotationBodyPath(), RDFFormat.RDFXML),
             null);
 
         Assert.assertTrue(isChangeInTheChangesList(getResourceURI("ro1-sp2/ann3").toString(),
             ROEVO.Addition.toString(), model, nodes));
         Assert.assertTrue(isChangeInTheChangesList(getResourceURI("ro1-sp2/afinalfolder").toString(),
             ROEVO.Addition.getURI(), model, nodes));
         Assert.assertTrue(isChangeInTheChangesList(getResourceURI("ro1-sp2/ann2").toString(),
             ROEVO.Modification.getURI(), model, nodes));
         Assert.assertTrue(isChangeInTheChangesList(getResourceURI("ro1-sp2/res3").toString(), ROEVO.Addition.getURI(),
             model, nodes));
         Assert.assertTrue(isChangeInTheChangesList(getResourceURI("ro1-sp1/afolder").toString(),
             ROEVO.Removal.getURI(), model, nodes));
 
         //should not consider any resources added to the research object after the snaphot is done
         Assert.assertFalse(isChangeInTheChangesList(getResourceURI("ro1-sp1/change_annotation").toString(),
             ROEVO.Addition.getURI(), model, nodes));
         Assert.assertFalse(isChangeInTheChangesList(getResourceURI("ro1-sp1/ann3-body").toString(),
             ROEVO.Addition.getURI(), model, nodes));
     }
 
 
     @Test(expected = NullPointerException.class)
     public final void testStoreROhistoryWithWrongParametrs()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         testStructure.sms.storeAggregatedDifferences(null, testStructure.sp1);
     }
 
 
     @Test
     public final void testStoreROhistoryWithNoAccenestor()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         String result = testStructure.sms.storeAggregatedDifferences(testStructure.sp1, null);
         Assert.assertEquals("", result);
     }
 
 
     @Test
     public void testAddFolder()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         Folder folder = new Folder();
         folder.setUri(FOLDER_URI);
 
         SemanticMetadataService sms = new SemanticMetadataServiceImpl(userProfile, false);
         try {
             sms.createResearchObject(researchObject);
             sms.addResource(researchObject, workflowURI, workflowInfo);
             sms.addResource(researchObject, workflow2URI, workflowInfo);
             sms.addResource(researchObject, resourceFakeURI, resourceFakeInfo);
 
             folder.getFolderEntries().add(new FolderEntry(workflowURI, "workflow1"));
             folder.getFolderEntries().add(new FolderEntry(resourceFakeURI, "a resource"));
 
             Folder folder2 = sms.addFolder(researchObject, folder);
             Assert.assertEquals(folder.getUri(), folder2.getUri());
             Assert.assertNotNull(folder2.getProxyUri());
 
             OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
             model.read(sms.getNamedGraph(folder2.getResourceMapUri(), RDFFormat.RDFXML), null);
 
             Resource manifestRes = model.getResource(researchObject.getManifestUri().toString());
             Assert.assertNotNull(manifestRes);
 
             Individual roInd = model.getIndividual(researchObject.getUri().toString());
             Assert.assertNotNull(roInd);
             Assert.assertTrue(roInd.hasRDFType(RO.ResearchObject));
             Assert.assertTrue(model.contains(roInd, ORE.isDescribedBy, manifestRes));
 
             Resource folderRMRes = model.getResource(folder2.getResourceMapUri().toString());
             Assert.assertNotNull(folderRMRes);
 
             Individual folderInd = model.getIndividual(folder2.getUri().toString());
             Assert.assertNotNull(folderInd);
             Assert.assertTrue(folderInd.hasRDFType(RO.Folder));
             Assert.assertTrue(folderInd.hasRDFType(ORE.Aggregation));
             Assert.assertTrue(model.contains(folderInd, ORE.isAggregatedBy, roInd));
             Assert.assertTrue(model.contains(folderInd, ORE.isDescribedBy, folderRMRes));
 
             for (FolderEntry entry : folder2.getFolderEntries()) {
                 Assert.assertTrue(folder.getFolderEntries().contains(entry));
                 Assert.assertNotNull(entry.getUri());
                 Individual entryInd = model.getIndividual(entry.getUri().toString());
                 Assert.assertNotNull(entryInd);
                 Individual resInd = model.getIndividual(entry.getProxyFor().toString());
                 Assert.assertNotNull(resInd);
                 Literal name = model.createLiteral(entry.getEntryName());
 
                 Assert.assertTrue(resInd.hasRDFType(RO.Resource));
                 Assert.assertTrue(model.contains(folderInd, ORE.aggregates, resInd));
                 Assert.assertTrue(model.contains(entryInd, ORE.proxyFor, resInd));
                 Assert.assertTrue(model.contains(entryInd, ORE.proxyIn, folderInd));
                 Assert.assertTrue(model.contains(entryInd, RO.entryName, name));
             }
         } finally {
             sms.close();
         }
     }
 
 
     /***** HELPERS *****/
 
     private Boolean isChangeInTheChangesList(String relatedObjectURI, String rdfClass, OntModel model,
             List<RDFNode> changesList) {
         for (RDFNode change : changesList) {
             Boolean partialResult1 = change.asResource()
                     .getProperty(model.createProperty("http://purl.org/wf4ever/roevo#relatedResource")).getObject()
                     .toString().equals(relatedObjectURI);
             Boolean partialREsult2 = change.as(Individual.class).hasRDFType(rdfClass);
             if (partialResult1 && partialREsult2) {
                 return true;
             }
         }
         return false;
     }
 
 
     private URI getResourceURI(String resourceName)
             throws URISyntaxException {
         String result = PROJECT_PATH;
         result += FILE_SEPARATOR + "src" + FILE_SEPARATOR + "test" + FILE_SEPARATOR + "resources" + FILE_SEPARATOR
                 + "rdfStructure" + FILE_SEPARATOR + resourceName;
         return new URI("file://" + result);
     }
 
 
     class TestStructure {
 
         public ResearchObject ro1;
         public ResearchObject sp1;
         public ResearchObject sp2;
         public ResearchObject arch1;
         public SemanticMetadataService sms;
 
 
         public TestStructure()
                 throws URISyntaxException, FileNotFoundException {
             ro1 = ResearchObject.create(getResourceURI("ro1/"));
             sp1 = ResearchObject.create(getResourceURI("ro1-sp1/"));
             sp2 = ResearchObject.create(getResourceURI("ro1-sp2/"));
             arch1 = ResearchObject.create(getResourceURI("ro1-arch1/"));
             File file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1/.ro/manifest.ttl");
             FileInputStream is = new FileInputStream(file);
             sms = new SemanticMetadataServiceImpl(userProfile, ro1, is, RDFFormat.TURTLE);
             file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1/.ro/evo_info.ttl");
             is = new FileInputStream(file);
             sms.addNamedGraph(ro1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
 
             file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-sp1/.ro/manifest.ttl");
             is = new FileInputStream(file);
             sms.createResearchObject(sp1);
             sms.updateManifest(sp1, is, RDFFormat.TURTLE);
             file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-sp1/.ro/evo_info.ttl");
             is = new FileInputStream(file);
             sms.addNamedGraph(sp1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
 
             file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-sp2/.ro/manifest.ttl");
             is = new FileInputStream(file);
             sms.createResearchObject(sp2);
             sms.updateManifest(sp2, is, RDFFormat.TURTLE);
             file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-sp2/.ro/evo_info.ttl");
             is = new FileInputStream(file);
             sms.addNamedGraph(sp2.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
 
             file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-arch1/.ro/manifest.ttl");
             is = new FileInputStream(file);
             sms.createResearchObject(arch1);
             sms.updateManifest(arch1, is, RDFFormat.TURTLE);
             file = new File(PROJECT_PATH + "/src/test/resources/rdfStructure/ro1-arch1/.ro/evo_info.ttl");
             is = new FileInputStream(file);
             sms.addNamedGraph(arch1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
         }
     }
 
 }
