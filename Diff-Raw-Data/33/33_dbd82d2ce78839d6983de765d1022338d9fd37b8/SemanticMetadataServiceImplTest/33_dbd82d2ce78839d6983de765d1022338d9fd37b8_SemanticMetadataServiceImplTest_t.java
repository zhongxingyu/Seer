 /**
  * 
  */
 package pl.psnc.dl.wf4ever.sms;
 
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
 
 import javax.naming.NamingException;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.rio.RDFFormat;
 
 import pl.psnc.dl.wf4ever.common.ResearchObject;
 import pl.psnc.dl.wf4ever.common.util.SafeURI;
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
 import com.hp.hpl.jena.rdf.model.ModelFactory;
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
 public class SemanticMetadataServiceImplTest extends SemanticMetadataServiceBaseTest {
 
     /**
      * Test method for {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#createResearchObject(java.net.URI)} .
      */
     @Test(expected = IllegalArgumentException.class)
     public final void testCreateResearchObject() {
         //repetition
         test.sms.createResearchObject(test.emptyRO);
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#updateManifest(java.net.URI, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testUpdateManifest() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(FAKE_PATH), resourceFakeInfo);
 
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/manifest.ttl");
         test.sms.updateManifest(test.emptyRO, is, RDFFormat.TURTLE);
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getManifest(test.emptyRO, RDFFormat.RDFXML), null);
 
         Individual manifest = model.getIndividual(test.emptyRO.getManifestUri().toString());
         Individual ro = model.getIndividual(test.emptyRO.getUri().toString());
 
         Assert.assertEquals("Manifest created has been updated", "2011-12-02T16:01:10Z",
             manifest.getPropertyValue(DCTerms.created).asLiteral().getString());
         Assert.assertEquals("RO created has been updated", "2011-12-02T15:01:10Z", ro.getPropertyValue(DCTerms.created)
                 .asLiteral().getString());
 
         Set<String> creators = new HashSet<String>();
         String creatorsQuery = String.format("PREFIX dcterms: <%s> PREFIX foaf: <%s> SELECT ?name "
                 + "WHERE { <%s> dcterms:creator ?x . ?x foaf:name ?name . }", DCTerms.NS, "http://xmlns.com/foaf/0.1/",
             test.emptyRO.getUri().toString());
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
 
         Assert.assertTrue(
             "RO must aggregate resources",
             model.contains(ro, ORE.aggregates,
                 model.createResource(test.emptyRO.getUri().resolve(WORKFLOW_PATH).toString())));
         Assert.assertTrue(
             "RO must aggregate resources",
             model.contains(ro, ORE.aggregates,
                 model.createResource(test.emptyRO.getUri().resolve(ANNOTATION_PATH).toString())));
         Assert.assertTrue(
             "RO must not aggregate previous resources",
             !model.contains(ro, ORE.aggregates,
                 model.createResource(test.emptyRO.getUri().resolve(FAKE_PATH).toString())));
         validateProxy(model, manifest, test.emptyRO.getUri().toString() + "proxy1",
             test.emptyRO.getUri().resolve(WORKFLOW_PATH).toString());
     }
 
 
     private void validateProxy(OntModel model, Individual manifest, String proxyURI, String proxyForURI) {
         Individual proxy = model.getIndividual(proxyURI);
         Assert.assertNotNull("Manifest must contain " + proxyURI, proxy);
         Assert.assertTrue(String.format("Proxy %s must be a ore:Proxy", proxyURI),
             proxy.hasRDFType("http://www.openarchives.org/ore/terms/Proxy"));
         Assert.assertEquals("Proxy for must be valid", proxyForURI, proxy.getPropertyResourceValue(ORE.proxyFor)
                 .getURI());
         Assert.assertEquals("Proxy in must be valid", test.emptyRO.getUri().toString(),
             proxy.getPropertyResourceValue(ORE.proxyIn).getURI());
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#removeResearchObject(java.net.URI, java.net.URI)} .
      */
     @Test
     public final void testRemoveManifest() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH), ann1Info);
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
         test.sms.addNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), is, RDFFormat.TURTLE);
         test.sms.removeResearchObject(test.emptyRO);
 
         //Should not throw an exception
         test.sms.removeResearchObject(test.emptyRO);
 
         Assert.assertNotNull("Get other named graph must not return null",
             test.sms.getNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), RDFFormat.RDFXML));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getManifest(java.net.URI, org.openrdf.rio.RDFFormat)} .
      * Expect null in case manifest does not exists.
      */
     @Test
     public final void testGetManifestInCaseRODoesNotExists() {
         Assert.assertNull("Returns null when manifest does not exist",
             test.sms.getManifest(new ResearchObject(URI.create("http://example.org/null/")), RDFFormat.RDFXML));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getManifest(java.net.URI, org.openrdf.rio.RDFFormat)} .
      */
     @Test
     public final void testGetManifest() {
 
         Calendar before = Calendar.getInstance();
         ResearchObject researchObject = ResearchObject.create(URI.create("http://www.example.com/null/"));
         test.sms.createResearchObject(researchObject);
         Calendar after = Calendar.getInstance();
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(test.sms.getManifest(researchObject, RDFFormat.RDFXML), null);
 
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
         Resource creatorResource = ro.getPropertyResourceValue(DCTerms.creator);
         Assert.assertNotNull("RO must contain dcterms:creator", creatorResource);
 
         OntModel userModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         userModel.read(test.sms.getNamedGraph(URI.create(creatorResource.getURI()), RDFFormat.RDFXML), "");
 
         Individual creator = userModel.getIndividual(creatorResource.getURI());
         Assert.assertTrue("Creator must be a foaf:Agent", creator.hasRDFType("http://xmlns.com/foaf/0.1/Agent"));
         Assert.assertEquals("Creator name must be correct", userProfile.getName(), creator.getPropertyValue(FOAF.name)
                 .asLiteral().getString());
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getManifest(java.net.URI, org.openrdf.rio.RDFFormat)} .
      * Expect null in case manifest does not exists.
      */
     @Test
     public final void testGetManifestWithAnnotationBodiesWhenManifestDoesNotExists() {
         Assert.assertNull("Returns null when manifest does not exist",
             test.sms.getManifest(new ResearchObject(URI.create("http://example.org/null/")), RDFFormat.TRIG));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getManifest(java.net.URI, org.openrdf.rio.RDFFormat)} .
      */
     @Test
     public final void testGetManifestWithAnnotationBodies() {
 
         NamedGraphSet graphset = new NamedGraphSetImpl();
         graphset.read(test.sms.getManifest(test.annotatedRO, RDFFormat.TRIG), "TRIG", null);
 
         Quad sampleAgg = new Quad(Node.createURI(test.annotatedRO.getManifestUri().toString()),
                 Node.createURI(test.annotatedRO.getUri().toString()), Node.createURI(ORE.aggregates.getURI()),
                 Node.createURI(test.annotatedRO.getUri().resolve(WORKFLOW_PATH).toString()));
         Assert.assertTrue("Contains a sample aggregation", graphset.containsQuad(sampleAgg));
 
         Quad sampleAnn = new Quad(Node.createURI(test.annotatedRO.getUri().resolve(ANNOTATION_BODY_PATH).toString()),
                 Node.createURI(test.annotatedRO.getUri().resolve(WORKFLOW_PATH).toString()),
                 Node.createURI("http://purl.org/dc/terms/license"), Node.createLiteral("GPL"));
         Assert.assertTrue("Contains a sample annotation", graphset.containsQuad(sampleAnn));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#addResource(java.net.URI, java.net.URI, pl.psnc.dl.wf4ever.dlibra.ResourceInfo)}
      * .
      */
     @Test
     public final void testAddResource()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         Assert.assertTrue(test.sms
                 .addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo));
         Assert.assertTrue(test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH),
             ann1Info));
         Assert.assertFalse(test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), null));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#removeResource(java.net.URI, java.net.URI)} .
      */
     @Test
     public final void testRemoveResource() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH), ann1Info);
         test.sms.removeResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH));
         // no longer throws exceptions
         test.sms.removeResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH));
         test.sms.removeResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH));
 
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/manifest.ttl");
         test.sms.updateManifest(test.emptyRO, is, RDFFormat.TURTLE);
         test.sms.removeResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH));
         Assert.assertNull("There should be no annotation body after a resource is deleted",
             test.sms.getNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_PATH), RDFFormat.RDFXML));
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
         model.read(test.sms.getManifest(test.emptyRO, RDFFormat.RDFXML), null);
         Assert.assertFalse(model.listStatements(null, null,
             model.createResource(test.emptyRO.getUri().resolve(ANNOTATION_PATH).toString())).hasNext());
         Assert.assertFalse(model.listStatements(
             model.createResource(test.emptyRO.getUri().resolve(ANNOTATION_PATH).toString()), null, (RDFNode) null)
                 .hasNext());
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getResource(java.net.URI, org.openrdf.rio.RDFFormat)} .
      * 
      * @throws URISyntaxException
      */
     @Test
     public final void testGetResource()
             throws URISyntaxException {
         Assert.assertNull("Returns null when resource does not exist",
             test.sms.getResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), RDFFormat.RDFXML));
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH), ann1Info);
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
         test.sms.addNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), is, RDFFormat.TURTLE);
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), RDFFormat.RDFXML),
             test.emptyRO.getUri().toString());
         verifyResource(test.sms, model, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         verifyTriple(model, test.emptyRO.getUri().resolve(WORKFLOW_PATH), URI.create("http://purl.org/dc/terms/title"),
             "A test");
         verifyTriple(model, test.emptyRO.getUri().resolve(WORKFLOW_PATH), URI.create("http://purl.org/dc/terms/title"),
             "An alternative title");
         verifyTriple(model, test.emptyRO.getUri().resolve(WORKFLOW_PATH),
             URI.create("http://purl.org/dc/terms/license"), "GPL");
 
         model.read(
             test.sms.getResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH), RDFFormat.TURTLE), null,
             "TTL");
         verifyResource(test.sms, model, test.emptyRO.getUri().resolve(ANNOTATION_PATH), ann1Info);
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#getNamedGraph(java.net.URI, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testGetNamedGraph() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH), ann1Info);
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
         test.sms.addNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), is, RDFFormat.TURTLE);
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), RDFFormat.TURTLE), null,
             "TTL");
 
         verifyTriple(model, test.emptyRO.getUri().resolve(WORKFLOW_PATH), URI.create("http://purl.org/dc/terms/title"),
             "A test");
         verifyTriple(model, test.emptyRO.getUri().resolve(WORKFLOW_PATH),
             URI.create("http://purl.org/dc/terms/license"), "GPL");
         verifyTriple(model, URI.create("http://workflows.org/a%20workflow.scufl"),
             URI.create("http://purl.org/dc/terms/description"), "Something interesting");
         verifyTriple(model, test.emptyRO.getUri().resolve(WORKFLOW_PART_PATH),
             URI.create("http://purl.org/dc/terms/description"), "The key part");
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#findResearchObjectsByPrefix(java.net.URI)} .
      */
     @Test
     public final void testFindManifests() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH), ann1Info);
 
         Set<URI> result = test.sms.findResearchObjectsByPrefix(test.emptyRO.getUri().resolve(".."));
         Assert.assertTrue("Find with base of RO", result.contains(test.emptyRO.getUri()));
         Assert.assertTrue("Find with base of RO", result.contains(test.emptyRO2.getUri()));
 
         result = test.sms.findResearchObjectsByPrefix(test.wrongRO.getUri().resolve(".."));
         Assert.assertFalse("Not find with the wrong base", result.contains(test.emptyRO.getUri()));
         Assert.assertFalse("Not find with the wrong base", result.contains(test.emptyRO2.getUri()));
 
         result = test.sms.findResearchObjectsByCreator(userProfile.getUri());
         Assert.assertTrue("Find by creator of RO", result.contains(test.emptyRO.getUri()));
         Assert.assertTrue("Find by creator of RO", result.contains(test.emptyRO2.getUri()));
 
         result = test.sms.findResearchObjectsByCreator(test.wrongRO.getUri());
         Assert.assertFalse("Not find by the wrong creator", result.contains(test.emptyRO.getUri()));
         Assert.assertFalse("Not find by the wrong creator", result.contains(test.emptyRO2.getUri()));
     }
 
 
     /**
      * Test method for {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#isRoFolder(java.net.URI)} .
      */
     @Test
     public final void testIsRoFolder() {
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/manifest.ttl");
         test.sms.updateManifest(test.emptyRO, is, RDFFormat.TURTLE);
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getManifest(test.emptyRO, RDFFormat.RDFXML), null);
 
         Assert.assertTrue("<afolder> is an ro:Folder",
             test.sms.isRoFolder(test.emptyRO, test.emptyRO.getUri().resolve(FOLDER_PATH)));
         Assert.assertTrue("<ann1> is not an ro:Folder",
             !test.sms.isRoFolder(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH)));
         Assert.assertTrue("Fake resource is not an ro:Folder",
             !test.sms.isRoFolder(test.emptyRO, test.emptyRO.getUri().resolve(FAKE_PATH)));
         Assert.assertTrue("<afolder> is not an ro:Folder according to other RO",
             !test.sms.isRoFolder(test.emptyRO2, test.emptyRO.getUri().resolve(FOLDER_PATH)));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#addNamedGraph(java.net.URI, java.io.InputStream, org.openrdf.rio.RDFFormat)}
      * .
      */
     @Test
     public final void testAddNamedGraph() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH), ann1Info);
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
         Assert.assertTrue(test.sms.addNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), is,
             RDFFormat.TURTLE));
     }
 
 
     /**
      * Test method for {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#isROMetadataNamedGraph(java.net.URI)} .
      */
     @Test
     public final void testIsROMetadataNamedGraph() {
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
         test.sms.addNamedGraph(test.annotatedRO.getUri().resolve(ANNOTATION_BODY_PATH).resolve("fake"), is,
             RDFFormat.TURTLE);
 
         Assert.assertTrue("Annotation body is an RO metadata named graph",
             test.sms.isROMetadataNamedGraph(test.annotatedRO, test.annotatedRO.getUri().resolve(ANNOTATION_BODY_PATH)));
         Assert.assertTrue(
             "An random named graph is not an RO metadata named graph",
             !test.sms.isROMetadataNamedGraph(test.annotatedRO, test.annotatedRO.getUri().resolve(ANNOTATION_BODY_PATH)
                     .resolve("fake")));
         Assert.assertTrue("Manifest is an RO metadata named graph",
             test.sms.isROMetadataNamedGraph(test.annotatedRO, test.annotatedRO.getManifestUri()));
         Assert.assertTrue("A resource is not an RO metadata named graph",
             !test.sms.isROMetadataNamedGraph(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH)));
         Assert.assertTrue("A fake resource is not an RO metadata named graph",
             !test.sms.isROMetadataNamedGraph(test.annotatedRO, test.emptyRO.getUri().resolve(FAKE_PATH)));
     }
 
 
     /**
      * Test method for
      * {@link pl.psnc.dl.wf4ever.sms.SemanticMetadataServiceImpl#removeNamedGraph(java.net.URI, java.net.URI)} .
      */
     @Test
     public final void testRemoveNamedGraph() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_PATH), ann1Info);
         InputStream is = getClass().getClassLoader().getResourceAsStream("rdfStructure/mess-ro/.ro/annotationBody.ttl");
         test.sms.addNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), is, RDFFormat.TURTLE);
         Assert.assertNotNull("A named graph exists",
             test.sms.getNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), RDFFormat.RDFXML));
         test.sms.removeNamedGraph(test.emptyRO, test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH));
         Assert.assertNull("A deleted named graph no longer exists",
             test.sms.getNamedGraph(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH), RDFFormat.RDFXML));
     }
 
 
     @Test
     public final void testExecuteSparql()
             throws IOException {
         String describeQuery = String.format("DESCRIBE <%s>", test.annotatedRO.getUri().resolve(WORKFLOW_PATH)
                 .toString());
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         QueryResult res = test.sms.executeSparql(describeQuery, RDFFormat.RDFXML);
         model.read(res.getInputStream(), null, "RDF/XML");
         Individual resource = model.getIndividual(test.annotatedRO.getUri().resolve(WORKFLOW_PATH).toString());
         Assert.assertNotNull("Resource cannot be null", resource);
         Assert.assertTrue(
             String.format("Resource %s must be a ro:Resource", test.annotatedRO.getUri().resolve(WORKFLOW_PATH)),
             resource.hasRDFType(RO.NAMESPACE + "Resource"));
 
         InputStream is = getClass().getClassLoader().getResourceAsStream("sparql/direct-annotations-construct.sparql");
         String constructQuery = IOUtils.toString(is, "UTF-8");
         model.removeAll();
         model.read(test.sms.executeSparql(constructQuery, RDFFormat.RDFXML).getInputStream(), null, "RDF/XML");
         Assert.assertTrue("Construct contains triple 1", model.contains(
             model.createResource(test.annotatedRO.getUri().resolve(WORKFLOW_PATH).toString()), DCTerms.title, "A test"));
         Assert.assertTrue("Construct contains triple 2", model.contains(
             model.createResource(test.annotatedRO.getUri().resolve(WORKFLOW_PATH).toString()), DCTerms.license, "GPL"));
 
         is = getClass().getClassLoader().getResourceAsStream("sparql/direct-annotations-select.sparql");
         String selectQuery = IOUtils.toString(is, "UTF-8");
         String xml = IOUtils.toString(test.sms.executeSparql(selectQuery, SemanticMetadataService.SPARQL_XML)
                 .getInputStream(), "UTF-8");
         // FIXME make more in-depth XML validation
         Assert.assertTrue("XML looks correct", xml.contains("Marco Roos"));
 
         String json = IOUtils.toString(test.sms.executeSparql(selectQuery, SemanticMetadataService.SPARQL_JSON)
                 .getInputStream(), "UTF-8");
         // FIXME make more in-depth JSON validation
         Assert.assertTrue("JSON looks correct", json.contains("Marco Roos"));
 
         is = getClass().getClassLoader().getResourceAsStream("sparql/direct-annotations-ask-true.sparql");
         String askTrueQuery = IOUtils.toString(is, "UTF-8");
         xml = IOUtils.toString(test.sms.executeSparql(askTrueQuery, SemanticMetadataService.SPARQL_XML)
                 .getInputStream(), "UTF-8");
         Assert.assertTrue("XML looks correct", xml.contains("true"));
         is = getClass().getClassLoader().getResourceAsStream("sparql/direct-annotations-ask-false.sparql");
         String askFalseQuery = IOUtils.toString(is, "UTF-8");
         xml = IOUtils.toString(test.sms.executeSparql(askFalseQuery, SemanticMetadataService.SPARQL_XML)
                 .getInputStream(), "UTF-8");
         Assert.assertTrue("XML looks correct", xml.contains("false"));
 
         RDFFormat jpeg = new RDFFormat("JPEG", "image/jpeg", Charset.forName("UTF-8"), "jpeg", false, false);
         res = test.sms.executeSparql(describeQuery, jpeg);
         Assert.assertEquals("RDF/XML is the default format", RDFFormat.RDFXML, res.getFormat());
         res = test.sms.executeSparql(constructQuery, jpeg);
         Assert.assertEquals("RDF/XML is the default format", RDFFormat.RDFXML, res.getFormat());
         res = test.sms.executeSparql(selectQuery, jpeg);
         Assert.assertEquals("SPARQL XML is the default format", SemanticMetadataService.SPARQL_XML, res.getFormat());
         res = test.sms.executeSparql(askTrueQuery, jpeg);
         Assert.assertEquals("SPARQL XML is the default format", SemanticMetadataService.SPARQL_XML, res.getFormat());
     }
 
 
     @Test
     public final void testGetAllAttributes()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         Multimap<URI, Object> atts = test.sms.getAllAttributes(test.simpleAnnotatedRO.getUri().resolve(WORKFLOW_PATH));
         Assert.assertEquals(6, atts.size());
         Assert.assertTrue("Attributes contain type",
             atts.containsValue(URI.create("http://purl.org/wf4ever/ro#Resource")));
         Assert.assertTrue("Attributes contain created", atts.get(URI.create(DCTerms.created.toString())).iterator()
                 .next() instanceof Calendar);
         Assert.assertTrue("Attributes contain title", atts.get(URI.create(DCTerms.title.toString())).contains("A test"));
         Assert.assertTrue("Attributes contain title2",
             atts.get(URI.create(DCTerms.title.toString())).contains("An alternative title"));
         Assert.assertTrue("Attributes contain licence", atts.get(URI.create(DCTerms.license.toString()))
                 .contains("GPL"));
         Assert.assertTrue("Attributes contain creator",
             atts.get(URI.create(DCTerms.creator.toString())).contains("Stian Soiland-Reyes"));
 
     }
 
 
     @Test
     public void testGetNamedGraphWithRelativeURIs() {
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getNamedGraphWithRelativeURIs(test.annotatedRO.getUri().resolve(ANNOTATION_BODY_PATH),
             test.annotatedRO, RDFFormat.RDFXML), "", "RDF/XML");
         //FIXME this does not work correctly, for some reason ".." is stripped when reading the model
         verifyTriple(model, /* "../a_workflow.t2flow" */"a%20workflow.t2flow",
             URI.create("http://purl.org/dc/terms/title"), "A test");
         verifyTriple(model, URI.create(WORKFLOW_PATH), URI.create(DCTerms.license.getURI()), "GPL");
         verifyTriple(model, URI.create(WORKFLOW_ORG_WORFLOW_SCUF_PATH), URI.create(DCTerms.description.getURI()),
             "Something interesting");
         verifyTriple(model, /* "../a_workflow.t2flow#somePartOfIt" */"a%20workflow.t2flow#somePartOfIt",
             URI.create(DCTerms.description.getURI()), "The key part");
     }
 
 
     @Test
     public final void testGetRemoveUser() {
         OntModel userModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         userModel.read(test.sms.getUser(userProfile.getUri(), RDFFormat.RDFXML).getInputStream(), "", "RDF/XML");
         Individual creator = userModel.getIndividual(userProfile.getUri().toString());
         Assert.assertNotNull("User named graph must contain dcterms:creator", creator);
         Assert.assertTrue("Creator must be a foaf:Agent", creator.hasRDFType("http://xmlns.com/foaf/0.1/Agent"));
         Assert.assertEquals("Creator name must be correct", userProfile.getName(), creator.getPropertyValue(FOAF.name)
                 .asLiteral().getString());
 
         test.sms.removeUser(userProfile.getUri());
         userModel.removeAll();
         userModel.read(test.sms.getUser(userProfile.getUri(), RDFFormat.RDFXML).getInputStream(), "", "RDF/XML");
         creator = userModel.getIndividual(userProfile.getUri().toString());
         Assert.assertNull("User named graph must not contain dcterms:creator", creator);
     }
 
 
     @Test
     public final void testIsAggregatedResource() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         Assert.assertTrue("Is aggregated",
             test.sms.isAggregatedResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH)));
         Assert.assertFalse("Is not aggregated",
             test.sms.isAggregatedResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH_2)));
     }
 
 
     @Test
     public final void testIsAnnotation() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         URI ann = test.sms.addAnnotation(test.emptyRO, Arrays.asList(test.emptyRO.getUri().resolve(ANNOTATION_PATH)),
             test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH));
         Assert.assertTrue("Annotation is an annotation", test.sms.isAnnotation(test.emptyRO, ann));
         Assert.assertFalse("Workflow is not an annotation",
             test.sms.isAnnotation(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH)));
         Assert.assertFalse("2nd workflow is not an annotation",
             test.sms.isAnnotation(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH_2)));
     }
 
 
     @Test
     public final void testAddAnnotation() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         URI ann = test.sms
                 .addAnnotation(
                     test.emptyRO,
                     Arrays.asList(test.emptyRO.getUri().resolve(WORKFLOW_PATH),
                         test.emptyRO.getUri().resolve(WORKFLOW_PATH_2)),
                     test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH));
         Assert.assertNotNull("Ann URI is not null", ann);
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getManifest(test.emptyRO, RDFFormat.RDFXML), null);
         Resource researchObjectR = model.getResource(test.emptyRO.getUri().toString());
         Resource annotation = model.getResource(ann.toString());
         Resource workflow = model.getResource(test.emptyRO.getUri().resolve(WORKFLOW_PATH).toString());
         Resource workflow2 = model.getResource(test.emptyRO.getUri().resolve(WORKFLOW_PATH_2).toString());
         Resource abody = model.getResource(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH).toString());
 
         Assert.assertTrue(model.contains(researchObjectR, ORE.aggregates, annotation));
         Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, workflow));
         Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, workflow2));
         Assert.assertTrue(model.contains(annotation, AO.body, abody));
     }
 
 
     @Test
     public final void testUpdateAnnotation() {
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         URI ann = test.sms
                 .addAnnotation(
                     test.emptyRO,
                     Arrays.asList(test.emptyRO.getUri().resolve(WORKFLOW_PATH),
                         test.emptyRO.getUri().resolve(WORKFLOW_PATH_2)),
                     test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH));
         test.sms.updateAnnotation(test.emptyRO, ann, Arrays.asList(test.emptyRO.getUri().resolve(WORKFLOW_PATH),
             test.emptyRO.getUri()), test.emptyRO.getUri().resolve(WORKFLOW_PATH_2));
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getManifest(test.emptyRO, RDFFormat.RDFXML), null);
         Resource researchObjectR = model.getResource(test.emptyRO.getUri().toString());
         Resource annotation = model.getResource(ann.toString());
         Resource workflow = model.getResource(test.emptyRO.getUri().resolve(WORKFLOW_PATH).toString());
         Resource workflow2 = model.getResource(test.emptyRO.getUri().resolve(WORKFLOW_PATH_2).toString());
         Resource abody = model.getResource(test.emptyRO.getUri().resolve(ANNOTATION_BODY_PATH).toString());
 
         Assert.assertTrue(model.contains(researchObjectR, ORE.aggregates, annotation));
         Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, workflow));
         Assert.assertFalse(model.contains(annotation, RO.annotatesAggregatedResource, workflow2));
         Assert.assertTrue(model.contains(annotation, RO.annotatesAggregatedResource, researchObjectR));
         Assert.assertFalse(model.contains(annotation, AO.body, abody));
         Assert.assertTrue(model.contains(annotation, AO.body, workflow2));
     }
 
 
     /**
      * getAnnotation should return annotation body. TODO explain scenario! what in case body does not exists?
      */
     @Test
     public final void testGetAnnotationBody() {
         URI somewhere = URI.create("http://www.example.com/somewhere/");
         URI ann = test.sms.addAnnotation(
             test.annotatedRO,
             Arrays.asList(test.annotatedRO.getUri().resolve(WORKFLOW_PATH),
                 test.annotatedRO.getUri().resolve(WORKFLOW_PATH_2)), somewhere);
         URI annBody = test.sms.getAnnotationBody(test.annotatedRO, ann);
         Assert.assertEquals("Annotation body retrieved correctly", somewhere, annBody);
     }
 
 
     /**
      * deleteAnnotation should properly delete annotation. TODO explain scnenario! what in case annotation does not
      * exists?
      */
     @Test
     public final void testDeleteAnnotation() {
         URI ann = test.sms.addAnnotation(
             test.annotatedRO,
             Arrays.asList(test.annotatedRO.getUri().resolve(WORKFLOW_PATH),
                 test.annotatedRO.getUri().resolve(WORKFLOW_PATH_2)),
             test.annotatedRO.getUri().resolve(ANNOTATION_BODY_PATH));
         test.sms.deleteAnnotation(test.annotatedRO, ann);
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getManifest(test.annotatedRO, RDFFormat.RDFXML), null);
         Resource annotation = model.createResource(ann.toString());
         Assert.assertFalse("No annotation statements", model.listStatements(annotation, null, (RDFNode) null).hasNext());
         Assert.assertFalse("No annotation statements", model.listStatements(null, null, annotation).hasNext());
 
     }
 
 
     /**
      * isSnaphot method should properly identify archives.
      */
     @Test
     public final void testIsSnapshot() {
         Assert.assertTrue("snapshot does not recognized", test.sms.isSnapshot(test.sp1));
         Assert.assertFalse("snapshot wrongly recognized", test.sms.isSnapshot(test.ro1));
         Assert.assertFalse("snapshot wrongly recognized", test.sms.isSnapshot(test.arch1));
     }
 
 
     /**
      * isArchive method should properly identify archives.
      */
     @Test
     public final void testIsArchive() {
         Assert.assertTrue("archive does not recognized", test.sms.isArchive(test.arch1));
         Assert.assertFalse("archive does not recognized", test.sms.isArchive(test.ro1));
         Assert.assertFalse("archive does not recognized", test.sms.isArchive(test.sp1));
     }
 
 
     /**
      * getPreviousSnaphotOrArchiveMethod should return previous snapshot or null in case previous does not exists.
      * 
      * @throws URISyntaxException
      */
     @Test
     public final void testGetPreviousSnaphotOrArchive() {
         URI sp1Antecessor = test.sms.getPreviousSnaphotOrArchive(test.ro1, test.sp1);
         URI sp2Antecessor = test.sms.getPreviousSnaphotOrArchive(test.ro1, test.sp2);
         Assert.assertNull("wrong antecessor URI", sp1Antecessor);
         Assert.assertEquals("wrong antecessor URI", sp2Antecessor, test.sp1.getUri());
     }
 
 
     /**
      * getIndividual should return mix of manifest and roevo info file.
      */
     @Test
     public final void testGetIndividual()
             throws URISyntaxException, ClassNotFoundException, IOException, NamingException, SQLException {
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(SafeURI.URItoString(test.ro1.getUri().resolve(".ro/manifest.ttl")), "TTL");
         model.read(SafeURI.URItoString(test.ro1.getFixedEvolutionAnnotationBodyPath()), "TTL");
         Individual source = model.getIndividual(test.ro1.getUriString());
         Individual source2 = test.sms.getIndividual(test.ro1);
         Assert.assertEquals("wrong individual returned", source, source2);
     }
 
 
     /**
      * Get liveROfromSnaphotOrArchive method should find a live RO in case where live RO exists.
      */
     @Test
     public final void testGetLiveROfromSnapshotOrArchive()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         URI liveFromSP = test.sms.getLiveURIFromSnapshotOrArchive(test.sp1);
         URI liveFromARCH = test.sms.getLiveURIFromSnapshotOrArchive(test.arch1);
         Assert.assertEquals("wrong parent URI", liveFromSP, test.ro1.getUri());
         Assert.assertEquals("wrong parent URI", liveFromARCH, test.ro1.getUri());
     }
 
 
     /**
      * Get liveROfromSnaphotOrArchive method should return null in case live RO does no exists.
      */
     @Test
     public final void testGetLiveROfromSnapshotOrArchiveWhereLiveRODoesNotExists()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         URI liveFromRO = test.sms.getLiveURIFromSnapshotOrArchive(test.ro1);
         Assert.assertNull("live RO does not have a parent RO", liveFromRO);
     }
 
 
     /**
      * changeURIInManifestAndAnnotationsBodies should change URIs in manifest in bodies if bodies parameter equals true.
      */
     @Test
     public final void testChangeURIInManifestAndAnnotationBodies()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         int cnt = test.sms.changeURIInManifestAndAnnotationBodies(test.annotatedRO,
             test.annotatedRO.getUri().resolve(WORKFLOW_PATH), URI.create("http://www.example.com/complete_new_uri"),
             true);
         Assert.assertEquals("9 URIs should be changed", 9, cnt);
     }
 
 
     /**
      * changeURIInManifestAndAnnotationsBodies should change URIs only in manifest if bodies parameter equals false.
      */
     @Test
     public final void testChangeURIInManifest()
             throws ClassNotFoundException, IOException, NamingException, SQLException {
         int cnt = test.sms.changeURIInManifestAndAnnotationBodies(test.annotatedRO,
             test.annotatedRO.getUri().resolve(WORKFLOW_PATH), URI.create("http://www.example.com/complete_new_uri"),
             false);
         Assert.assertEquals("6 URIs should be changed", 6, cnt);
     }
 
 
     /**
      * getAggregatedResource function should return the complete list of aggregated resources.
      * 
      * @throws ManifestTraversingException
      */
     @Test
     public final void testGetAggregatedResources()
             throws ManifestTraversingException {
         List<AggregatedResource> list = test.sms.getAggregatedResources(test.ro1);
         Assert.assertTrue(list.contains(new AggregatedResource(test.ro1.getUri().resolve(
             "final-agregated-resource-file"))));
         Assert.assertTrue(list.contains(new AggregatedResource(test.ro1.getUri().resolve(".ro/ann-blank.ttl"))));
         Assert.assertTrue(list.contains(new AggregatedResource(test.ro1.getUri().resolve("res2"))));
         Assert.assertTrue(list.contains(new AggregatedResource(test.ro1.getUri().resolve("res1"))));
         Assert.assertTrue(list.contains(new AggregatedResource(test.ro1.getUri().resolve(".ro/evo_info.ttl"))));
         Assert.assertTrue(list.contains(new AggregatedResource(test.ro1.getUri().resolve(".ro/ann1-body.ttl"))));
         Assert.assertTrue(list.contains(new AggregatedResource(test.ro1.getUri().resolve("afinalfolder"))));
 
     }
 
 
     /**
      * GetAnnotation function should return the complete list of annotations.
      * 
      * @throws ManifestTraversingException
      */
     @Test
    public final void testGetAnnotations()
             throws ManifestTraversingException {
         List<Annotation> list = test.sms.getAnnotations(test.ro1);
         int cnt = 0;
         Boolean evo = true;
         Boolean ann = true;
         Boolean blank = true;
         for (Annotation a : list) {
            System.out.println(SafeURI.URItoString(test.ro1.getUri().resolve(ANNOTATION_PATH)));
            System.out.println(SafeURI.URItoString(a.getUri()));
            if (SafeURI.URItoString(test.ro1.getUri().resolve(ANNOTATION_PATH)).equals(SafeURI.URItoString(a.getUri()))
                    && ann) {
                 ann = false;
                 cnt++;
            } else if (SafeURI.URItoString(test.ro1.getUri().resolve(EVO_ANNOTATION)).equals(
                SafeURI.URItoString(a.getUri()))
                    && evo) {
                 evo = false;
                 cnt++;
             } else if (blank) {
                 blank = false;
                 cnt++;
             }
         }
         Assert.assertEquals("Three annotations should be found", cnt, 3);
     }
 
 
     /**
      * Should store roevo information in evo_info.ttl annotation body according to the contract.
      */
     @Test
     public final void testROevo()
             throws URISyntaxException, IOException {
 
         test.sms.storeAggregatedDifferences(test.sp2, test.sp1);
 
         Individual evoInfoSource = test.sms.getIndividual(test.sp2);
         List<RDFNode> nodes = evoInfoSource.getPropertyValue(ROEVO.wasChangedBy).as(Individual.class)
                 .listPropertyValues(ROEVO.hasChange).toList();
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getNamedGraph(test.sp2.getManifestUri(), RDFFormat.RDFXML), null);
         model.read(test.sms.getNamedGraph(test.sp2.getFixedEvolutionAnnotationBodyPath(), RDFFormat.RDFXML), null);
 
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
 
 
     /**
      * If wrong parameters are given then the exception should be raised.
      */
     @Test(expected = IllegalArgumentException.class)
     public final void testStoreROhistoryWithWrongParametrs()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         test.sms.storeAggregatedDifferences(null, test.sp1);
     }
 
 
     /**
      * If wrong parameters are given then the exception should be raised.
      */
     @Test(expected = IllegalArgumentException.class)
     public final void testStoreROhistoryWithParametersGivenConversely()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         test.sms.storeAggregatedDifferences(test.sp1, test.sp2);
     }
 
 
     /**
      * If wrong parameters are given then the exception should be raised.
      */
     @Test(expected = IllegalArgumentException.class)
     public final void testStoreROhistoryBetweenTwoTheSameObjects()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         test.sms.storeAggregatedDifferences(test.sp1, test.sp1);
     }
 
 
     /**
      * If there is a live RO or the first snapshot then the history should not be stored.
      */
     @Test
     public final void testStoreROhistoryWithNoAccenestor()
             throws ClassNotFoundException, IOException, NamingException, SQLException, URISyntaxException {
         String resultSp1 = test.sms.storeAggregatedDifferences(test.sp1, null);
         String resultLive = test.sms.storeAggregatedDifferences(test.ro1, null);
         Assert.assertEquals("", resultSp1);
         Assert.assertEquals("", resultLive);
     }
 
 
     //TODO
     //Explain scenario
     @Test
     public void testAddFolder() {
 
         Folder folder = new Folder();
         folder.setUri(test.emptyRO.getUri().resolve(FOLDER_PATH));
 
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH_2), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(FAKE_PATH), resourceFakeInfo);
 
         folder.getFolderEntries().add(new FolderEntry(test.emptyRO.getUri().resolve(WORKFLOW_PATH), "workflow1"));
         folder.getFolderEntries().add(new FolderEntry(test.emptyRO.getUri().resolve(FAKE_PATH), "a resource"));
 
         Folder folder2 = test.sms.addFolder(test.emptyRO, folder);
         Assert.assertEquals(folder.getUri(), folder2.getUri());
         Assert.assertNotNull(folder2.getProxyUri());
 
         OntModel manifestModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         manifestModel.read(test.sms.getManifest(test.emptyRO, RDFFormat.RDFXML), null);
         Individual roInd = manifestModel.getIndividual(test.emptyRO.getUri().toString());
         Assert.assertNotNull(roInd);
         Individual folderInd = manifestModel.getIndividual(folder2.getUri().toString());
         Assert.assertNotNull(folderInd);
         Assert.assertTrue(manifestModel.contains(roInd, RO.rootFolder, folderInd));
 
         OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM);
         model.read(test.sms.getNamedGraph(folder2.getResourceMapUri(), RDFFormat.RDFXML), null);
 
         Resource manifestRes = model.getResource(test.emptyRO.getManifestUri().toString());
         Assert.assertNotNull(manifestRes);
 
         roInd = model.getIndividual(test.emptyRO.getUri().toString());
         Assert.assertNotNull(roInd);
         Assert.assertTrue(roInd.hasRDFType(RO.ResearchObject));
         Assert.assertTrue(model.contains(roInd, ORE.isDescribedBy, manifestRes));
 
         Resource folderRMRes = model.getResource(folder2.getResourceMapUri().toString());
         Assert.assertNotNull(folderRMRes);
 
         folderInd = model.getIndividual(folder2.getUri().toString());
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
 
     }
 
 
     /**
      * Annotation should be found in easy way based on annotation body.
      */
     @Test
     public void testGetFolder() {
         Folder folder = new Folder();
         folder.setUri(test.emptyRO.getUri().resolve(FOLDER_PATH));
 
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH_2), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(FAKE_PATH), resourceFakeInfo);
 
         folder.getFolderEntries().add(new FolderEntry(test.emptyRO.getUri().resolve(WORKFLOW_PATH), "workflow1"));
         folder.getFolderEntries().add(new FolderEntry(test.emptyRO.getUri().resolve(FAKE_PATH), "a resource"));
         Folder folder2 = test.sms.addFolder(test.emptyRO, folder);
         Folder folder3 = test.sms.getFolder(folder2.getUri());
 
         Assert.assertEquals(folder2.getUri(), folder3.getUri());
         Assert.assertEquals(folder2.getProxyUri(), folder3.getProxyUri());
         Assert.assertEquals(folder2.getAggregationUri(), folder3.getAggregationUri());
         Assert.assertEquals(folder2.getResourceMapUri(), folder3.getResourceMapUri());
         Assert.assertEquals(folder2.getFolderEntries().size(), folder3.getFolderEntries().size());
         for (FolderEntry entry : folder3.getFolderEntries()) {
             Assert.assertTrue(folder2.getFolderEntries().contains(entry));
         }
 
     }
 
 
     //TODO
     //Explain scenario
     @Test
     public void testUpdateFolder() {
         Folder folder = new Folder();
         folder.setUri(test.emptyRO.getUri().resolve(FOLDER_PATH));
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH_2), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(FAKE_PATH), resourceFakeInfo);
 
         folder.getFolderEntries().add(new FolderEntry(test.emptyRO.getUri().resolve(WORKFLOW_PATH), "workflow1"));
         folder.getFolderEntries().add(new FolderEntry(test.emptyRO.getUri().resolve(FAKE_PATH), "a resource"));
 
         Folder folder2 = test.sms.addFolder(test.emptyRO, folder);
         Folder folder3 = test.sms.getFolder(folder2.getUri());
 
         FolderEntry entry1 = folder2.getFolderEntries().get(0);
         FolderEntry entry2 = folder2.getFolderEntries().get(1);
         folder2.getFolderEntries().remove(entry1);
         entry2.setEntryName("foo");
         FolderEntry entry3 = new FolderEntry(test.emptyRO.getUri().resolve(WORKFLOW_PATH_2), "workflow2");
         folder2.getFolderEntries().add(entry3);
 
         test.sms.updateFolder(folder2);
 
         Folder folder4 = test.sms.getFolder(folder2.getUri());
         Assert.assertEquals(folder3.getUri(), folder4.getUri());
         Assert.assertEquals(folder3.getProxyUri(), folder4.getProxyUri());
         Assert.assertEquals(folder3.getAggregationUri(), folder4.getAggregationUri());
         Assert.assertEquals(folder3.getResourceMapUri(), folder4.getResourceMapUri());
         Assert.assertEquals(folder3.getFolderEntries().size(), folder4.getFolderEntries().size());
         for (FolderEntry entry : folder4.getFolderEntries()) {
             Assert.assertTrue(!entry.equals(entry1));
             if (entry.equals(entry2)) {
                 Assert.assertEquals(entry2.getEntryName(), entry.getEntryName());
             } else {
                 Assert.assertEquals(entry3, entry);
             }
         }
     }
 
 
     //TODO
     //Explain scenario
     @Test
     public void testGetRootFolder() {
         Folder folder = new Folder();
         folder.setUri(test.emptyRO.getUri().resolve(FOLDER_PATH));
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(WORKFLOW_PATH_2), workflowInfo);
         test.sms.addResource(test.emptyRO, test.emptyRO.getUri().resolve(FAKE_PATH), resourceFakeInfo);
 
         folder.getFolderEntries().add(new FolderEntry(test.emptyRO.getUri().resolve(WORKFLOW_PATH), "workflow1"));
         folder.getFolderEntries().add(new FolderEntry(test.emptyRO.getUri().resolve(FAKE_PATH), "a resource"));
 
         Assert.assertNull(test.sms.getRootFolder(test.emptyRO));
         Folder folder2 = test.sms.addFolder(test.emptyRO, folder);
         Assert.assertEquals(folder2, test.sms.getRootFolder(test.emptyRO));
     }
 
 
     @Test
     public void testAnnotationForBody() {
         Annotation annotation = test.sms
                 .findAnnotationForBody(test.ro1, test.ro1.getFixedEvolutionAnnotationBodyPath());
         Assert.assertNotNull("Annotation should not be null", annotation);
     }
 
 
     /**
      * Annotation should be null if annotation can not be found.
      */
     @Test
     public void testAnnotationForBodyInCaseAnnotationDoesNotExists()
             throws URISyntaxException {
         Annotation annotation = test.sms.findAnnotationForBody(test.wrongRO,
             test.wrongRO.getUri().resolve(ANNOTATION_BODY_PATH));
         Assert.assertNull("Annotation should not be null", annotation);
     }
 
 
     /**
      * Annotation should be null if body does not exist.
      */
     @Test
     public void testAnnotationForBodyInCaseBodyDoesNotExists()
             throws URISyntaxException {
         Annotation annotation = test.sms.findAnnotationForBody(test.wrongRO,
             test.wrongRO.getUri().resolve("annotated-does-not-exist"));
         Assert.assertNull("Annotation should not be null", annotation);
     }
 }
