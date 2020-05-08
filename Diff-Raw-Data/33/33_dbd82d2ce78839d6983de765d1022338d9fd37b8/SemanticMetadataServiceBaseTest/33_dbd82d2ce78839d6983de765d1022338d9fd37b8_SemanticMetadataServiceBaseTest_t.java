 package pl.psnc.dl.wf4ever.sms;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.naming.NamingException;
 
 import org.apache.log4j.Logger;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.dl.ResourceMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata;
 import pl.psnc.dl.wf4ever.dl.UserMetadata.Role;
 import pl.psnc.dl.wf4ever.vocabulary.FOAF;
 import pl.psnc.dl.wf4ever.vocabulary.RO;
 
 import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
 import com.hp.hpl.jena.ontology.Individual;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntModelSpec;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 public class SemanticMetadataServiceBaseTest {
 
     @SuppressWarnings("unused")
     protected static final Logger log = Logger.getLogger(SemanticMetadataServiceImplTest.class);
 
     protected static UserMetadata userProfile;
     protected static ResourceMetadata workflowInfo;
     protected static ResourceMetadata ann1Info;
     protected static ResourceMetadata resourceFakeInfo;
 
     protected static String WORKFLOW_PATH = "a%20workflow.t2flow";
     protected static String WORKFLOW_ORG_WORFLOW_SCUF_PATH = "http://workflows.org/a%20workflow.scufl";
     protected final static String WORKFLOW_PART_PATH = "a%20workflow.t2flow#somePartOfIt";
     protected final static String WORKFLOW_PATH_2 = "runme.t2flow";
     protected final static String FAKE_PATH = "fake";
     protected final static String FOLDER_PATH = "afolder/";
     protected final static String ANNOTATION_BODY_PATH = ".ro/ann1";
     protected final static String ANNOTATION_PATH = "ann1";
    protected final static String EVO_ANNOTATION = "evo_info";
 
     protected TestStructure test;
 
 
     /**
      * @throws java.lang.Exception
      */
     @BeforeClass
     public static void setUpBeforeClass()
             throws Exception {
         userProfile = new UserMetadata("jank", "Jan Kowalski", Role.AUTHENTICATED);
         workflowInfo = new ResourceMetadata("a%20workflow.t2flow", "a%20workflow.t2flow", "ABC123455666344E", 646365L,
                 "SHA1", null, "application/vnd.taverna.t2flow+xml");
         ann1Info = new ResourceMetadata("ann1", "ann1", "A0987654321EDCB", 6L, "MD5", null, "application/rdf+xml");
         resourceFakeInfo = new ResourceMetadata("xyz", "xyz", "A0987654321EDCB", 6L, "MD5", null, "text/plain");
 
     }
 
 
     /**
      * @throws java.lang.Exception
      */
     @AfterClass
     public static void tearDownAfterClass()
             throws Exception {
         cleanData();
     }
 
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp()
             throws Exception {
         cleanData();
         test = new TestStructure();
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
         } catch (ClassNotFoundException | IOException | NamingException | SQLException e) {
             e.printStackTrace();
         } finally {
             if (sms != null) {
                 sms.close();
             }
         }
     }
 
 
     /***** HELPERS *****/
 
     protected void verifyTriple(Model model, URI subjectURI, URI propertyURI, String object) {
         Resource subject = model.createResource(subjectURI.toString());
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 
     protected void verifyTriple(Model model, String subjectURI, URI propertyURI, String object) {
         Resource subject = model.createResource(subjectURI);
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 
     protected void verifyTriple(Model model, String subjectURI, URI propertyURI, Resource object) {
         Resource subject = model.createResource(subjectURI);
         Property property = model.createProperty(propertyURI.toString());
         Assert.assertTrue(String.format("Annotation body must contain a triple <%s> <%s> <%s>", subject.getURI(),
             property.getURI(), object), model.contains(subject, property, object));
     }
 
 
     /**
      * @param model
      * @param ann1Info2
      * @param resourceURI
      * @throws URISyntaxException
      */
     protected void verifyResource(SemanticMetadataService sms, OntModel model, URI resourceURI,
             ResourceMetadata ann1Info2)
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
         Assert.assertEquals("Name is valid", ann1Info2.getName(), nameLiteral.asLiteral().getString());
 
         Literal filesizeLiteral = resource.getPropertyValue(RO.filesize).asLiteral();
         Assert.assertNotNull("Resource must contain ro:filesize", filesizeLiteral);
         Assert.assertEquals("Filesize type is xsd:long", XSDDatatype.XSDlong, filesizeLiteral.getDatatype());
         Assert.assertEquals("Filesize is valid", ann1Info2.getSizeInBytes(), filesizeLiteral.asLiteral().getLong());
 
         Resource checksumResource = resource.getPropertyValue(RO.checksum).asResource();
         Assert.assertNotNull("Resource must contain ro:checksum", checksumResource);
         URI checksumURN = new URI(checksumResource.getURI());
         Pattern p = Pattern.compile("urn:(\\w+):([0-9a-fA-F]+)");
         Matcher m = p.matcher(checksumURN.toString());
         Assert.assertTrue("Checksum can be parsed", m.matches());
         Assert.assertEquals("Digest method is correct", ann1Info2.getDigestMethod(), m.group(1));
         Assert.assertEquals("Checksum is correct", ann1Info2.getChecksum(), m.group(2));
     }
 
 
     protected Boolean isChangeInTheChangesList(String relatedObjectURI, String rdfClass, OntModel model,
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
 
 
     protected URI getResourceURI(String resourceName)
             throws URISyntaxException {
         String PROJECT_PATH = System.getProperty("user.dir");
         String FILE_SEPARATOR = System.getProperty("file.separator");
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
         public ResearchObject wrongRO;
         public ResearchObject annotatedRO;
         public ResearchObject simpleAnnotatedRO;
         public SemanticMetadataService sms;
         ResearchObject emptyRO;
         ResearchObject emptyRO2;
 
 
         public TestStructure()
                 throws URISyntaxException, FileNotFoundException {
             ro1 = ResearchObject.create(getResourceURI("ro1/"));
             sp1 = ResearchObject.create(getResourceURI("ro1-sp1/"));
             sp2 = ResearchObject.create(getResourceURI("ro1-sp2/"));
             arch1 = ResearchObject.create(getResourceURI("ro1-arch1/"));
             wrongRO = ResearchObject.create(getResourceURI("wrong-ro/"));
             annotatedRO = ResearchObject.create(URI.create("http://www.example.com/annotatedRO/"));
             simpleAnnotatedRO = ResearchObject.create(URI.create("http://www.example.com/simpleAnnotatedRO/"));
 
             InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1/.ro/manifest.ttl");
             sms = new SemanticMetadataServiceImpl(userProfile, ro1, is, RDFFormat.TURTLE);
 
             emptyRO = ResearchObject.create(URI.create("http://example.org/ROs/empty-RO/"));
             emptyRO2 = ResearchObject.create(URI.create("http://example.org/ROs/empty-RO2/"));
             sms.createResearchObject(emptyRO);
             sms.createResearchObject(emptyRO2);
 
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1/.ro/evo_info.ttl");
             sms.addNamedGraph(ro1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-sp1/.ro/manifest.ttl");
             sms.createResearchObject(sp1);
             sms.updateManifest(sp1, is, RDFFormat.TURTLE);
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-sp1/.ro/evo_info.ttl");
             sms.addNamedGraph(sp1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-sp2/.ro/manifest.ttl");
             sms.createResearchObject(sp2);
             sms.updateManifest(sp2, is, RDFFormat.TURTLE);
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-sp2/.ro/evo_info.ttl");
             sms.addNamedGraph(sp2.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-arch1/.ro/manifest.ttl");
             sms.createResearchObject(arch1);
             sms.updateManifest(arch1, is, RDFFormat.TURTLE);
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/ro1-arch1/.ro/evo_info.ttl");
             sms.addNamedGraph(arch1.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/wrong-ro/.ro/manifest.ttl");
             sms.createResearchObject(wrongRO);
             sms.updateManifest(wrongRO, is, RDFFormat.TURTLE);
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/wrong-ro/.ro/evo_info.ttl");
             sms.addNamedGraph(wrongRO.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/manifest.ttl");
             sms.createResearchObject(annotatedRO);
             sms.updateManifest(annotatedRO, is, RDFFormat.TURTLE);
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/evo_info.ttl");
             sms.addNamedGraph(annotatedRO.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
             //sms.addResource(annotatedRO, annotatedRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
             sms.addNamedGraph(annotatedRO.getUri().resolve(".ro/ann1"), is, RDFFormat.TURTLE);
 
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/manifest.ttl");
             sms.createResearchObject(simpleAnnotatedRO);
             sms.updateManifest(simpleAnnotatedRO, is, RDFFormat.TURTLE);
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/evo_info.ttl");
             sms.addNamedGraph(simpleAnnotatedRO.getFixedEvolutionAnnotationBodyPath(), is, RDFFormat.TURTLE);
             //sms.addResource(annotatedRO, annotatedRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
             is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
             sms.addNamedGraph(simpleAnnotatedRO.getUri().resolve(".ro/ann1"), is, RDFFormat.TURTLE);
         }
     }
 }
