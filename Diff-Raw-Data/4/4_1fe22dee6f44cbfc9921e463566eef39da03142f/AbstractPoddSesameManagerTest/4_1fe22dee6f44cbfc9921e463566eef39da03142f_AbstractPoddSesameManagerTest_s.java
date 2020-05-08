 /**
  * 
  */
 package com.github.podd.api.test;
 
 import info.aduna.iteration.Iterations;
 
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openrdf.model.Model;
import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.util.GraphUtil;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParser;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.openrdf.sail.memory.MemoryStore;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddSesameManager;
 import com.github.podd.exception.UnmanagedArtifactIRIException;
 import com.github.podd.exception.UnmanagedSchemaIRIException;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PoddObjectLabel;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * @author kutila
  * 
  */
 public abstract class AbstractPoddSesameManagerTest
 {
     /* test artifact pair with PURLs */
     private static final String TEST_ARTIFACT_BASIC_20130206_TTL = "/test/artifacts/basic-20130206.ttl";
     private static final String TEST_ARTIFACT_BASIC_20130206_INFERRED_TTL = "/test/artifacts/basic-20130206-inferred.ttl";
 
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     private PoddSesameManager testPoddSesameManager;
     
     private Repository testRepository;
     private RepositoryConnection testRepositoryConnection;
     
     private URI artifactGraph;
     
     private URI schemaGraph;
     
     public abstract PoddSesameManager getNewPoddSesameManagerInstance();
     
     /**
      * Helper method for testing
      * {@link com.github.podd.api.PoddSesameManager#isPublished(OWLOntologyID, RepositoryConnection)}
      * 
      */
     private boolean internalTestIsPublished(final String testResourcePath, final int expectedSize,
             final URI contextCumVersionIRI, final URI managementGraph) throws Exception
     {
         // prepare: load the ontology into the test repository
         final InputStream inputStream = this.getClass().getResourceAsStream(testResourcePath);
         this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, contextCumVersionIRI);
         Assert.assertEquals("Not the expected number of statements in Repository", expectedSize,
                 this.testRepositoryConnection.size(contextCumVersionIRI));
         
         // prepare: build an OWLOntologyID
         final IRI ontologyIRI =
                 this.testPoddSesameManager.getOntologyIRI(this.testRepositoryConnection, contextCumVersionIRI);
         final OWLOntologyID ontologyID = new OWLOntologyID(ontologyIRI.toOpenRDFURI(), contextCumVersionIRI);
         
         return this.testPoddSesameManager.isPublished(ontologyID, this.testRepositoryConnection, managementGraph);
     }
     
     /**
      * Loads the statements in the specified resource paths as the asserted and inferred statements
      * of an ontology. The contexts to load into are identified from the <i>OWL:VersionIRI</i>
      * values in both files.
      * 
      * NOTE: This method does not update any management graphs.
      * 
      * @param resourcePath
      *            Points to a resource containing asserted statements
      * @param inferredResourcePath
      *            Points to a resource containing the inferred statements
      * @param format
      *            The Format of both resources
      * @return An InferredOWLOntologyID for the loaded ontology
      * @throws Exception
      */
     private InferredOWLOntologyID loadOntologyFromResource(String resourcePath, String inferredResourcePath,
             RDFFormat format) throws Exception
     {
         final InputStream resourceStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Resource was null", resourceStream);
         
         // load statements into a Model
         Model concreteModel = new LinkedHashModel();
         RDFParser parser = Rio.createParser(format);
         parser.setRDFHandler(new StatementCollector(concreteModel));
         parser.parse(resourceStream, "");
         
         Model inferredModel = new LinkedHashModel();
         if(inferredResourcePath != null)
         {
             final InputStream inferredResourceStream = this.getClass().getResourceAsStream(inferredResourcePath);
             Assert.assertNotNull("Inferred resource was null", inferredResourceStream);
             
             // load inferred statements into a Model
             RDFParser inferredParser = Rio.createParser(format);
             inferredParser.setRDFHandler(new StatementCollector(inferredModel));
             inferredParser.parse(inferredResourceStream, "");
             
             // extract version IRI which is also the inferred IRI
             this.testRepositoryConnection.add(inferredModel,
                     GraphUtil.getUniqueSubjectURI(inferredModel, RDF.TYPE, OWL.ONTOLOGY));
         }
         
         URI ontologyURI = GraphUtil.getUniqueSubjectURI(concreteModel, RDF.TYPE, OWL.ONTOLOGY);
         log.info("ontology URI: {}", ontologyURI);
         // dump the statements into the correct context of the Repository
         this.testRepositoryConnection.add(concreteModel,
                 GraphUtil.getUniqueObjectURI(concreteModel, ontologyURI, OWL.VERSIONIRI));
         
         Model totalModel = new LinkedHashModel(concreteModel);
         totalModel.addAll(inferredModel);
         
         Collection<InferredOWLOntologyID> results = OntologyUtils.modelToOntologyIDs(totalModel);
         Assert.assertEquals(1, results.size());
         
         return results.iterator().next();
     }
     
     /**
      * This method loads all PODD schema ontologies and their pre-computed inferred statements into
      * the test repository.
      */
     private void loadSchemaOntologies() throws Exception
     {
         String[] schemaResourcePaths =
                 { PoddRdfConstants.PATH_PODD_DCTERMS, PoddRdfConstants.PATH_PODD_FOAF, PoddRdfConstants.PATH_PODD_USER,
                         PoddRdfConstants.PATH_PODD_BASE, PoddRdfConstants.PATH_PODD_SCIENCE,
                         PoddRdfConstants.PATH_PODD_PLANT,
                 // PoddRdfConstants.PATH_PODD_ANIMAL,
                 };
         
         String[] schemaInferredResourcePaths =
                 { "/test/ontologies/dcTermsInferred.rdf", "/test/ontologies/foafInferred.rdf",
                         "/test/ontologies/poddUserInferred.rdf", "/test/ontologies/poddBaseInferred.rdf",
                         "/test/ontologies/poddScienceInferred.rdf", "/test/ontologies/poddPlantInferred.rdf", };
         for(int i = 0; i < schemaResourcePaths.length; i++)
         {
             log.info("Next paths: {} {}", schemaResourcePaths[i], schemaInferredResourcePaths[i]);
             this.loadOntologyFromResource(schemaResourcePaths[i], schemaInferredResourcePaths[i], RDFFormat.RDFXML);
         }
     }
     
     /**
      * Helper method which populates a graph with artifact management triples.
      * 
      * @return The URI of the test artifact management graph
      * @throws Exception
      */
     private URI populateArtifactManagementGraph() throws Exception
     {
         final URI testOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/99-99/artifact:99");
         final URI testVersionURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/99-99/version:1");
         final URI testInferredURI =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/99-99/version:1");
         
         this.testRepositoryConnection.add(testOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
         this.testRepositoryConnection.add(testInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
         this.testRepositoryConnection.add(testOntologyURI, OWL.VERSIONIRI, testVersionURI, this.artifactGraph);
         this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, testVersionURI,
                 this.artifactGraph);
         this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 testInferredURI, this.artifactGraph);
         this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                 testInferredURI, this.artifactGraph);
         
         return this.artifactGraph;
     }
     
     /**
      * Helper method which populates a graph with schema management triples.
      * 
      * @return The URI of the test schema management graph
      * @throws Exception
      */
     private URI populateSchemaManagementGraph() throws Exception
     {
         final URI pbBaseOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase");
         final URI pbVersionURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddBase/1");
         final URI pbInferredURI =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
         
         final URI pScienceOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddScience");
         final URI pScienceVersionURI =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddScience/27");
         final URI pScienceInferredURI =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddScience/43");
         
         final URI pPlantOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddPlant");
         final URI pPlantVersionURI =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddPlant/1");
         final URI pPlantInferredURI =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddPlant/1");
 
         final URI pPlantVersionURIv2 =
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddPlant/2");
         final URI pPlantInferredURIv2 =
                 ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2");
         
         
         // Podd-Base
         this.testRepositoryConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pbInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pbVersionURI,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pbVersionURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                 pbInferredURI, this.schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, pbVersionURI,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pbInferredURI, this.schemaGraph);
         /*
          * The Management graph for Podd-Base created above is as follows.
          * 
          * <http://purl.org/podd/ns/poddBase> <RDF:Type> <OWL:Ontology>
          * 
          * <urn:inferred:http://purl.org/podd/ns/version/poddBase/1> <RDF:Type> <OWL:Ontology>
          * 
          * <http://purl.org/podd/ns/poddBase> <OWL:VersionIRI> <http://purl.org/podd/ns/version/poddBase/1>
          * 
          * <http://purl.org/podd/ns/version/poddBase/1> <poddBase:InferredVersion> <urn:inferred:http://purl.org/podd/ns/version/poddBase/1>
          * 
          * <http://purl.org/podd/ns/poddBase> <OMV:CurrentVersion> <http://purl.org/podd/ns/version/poddBase/1>
          * 
          * <http://purl.org/podd/ns/poddBase> <poddBase:CurrentInferredVersion> <urn:inferred:http://purl.org/podd/ns/version/poddBase/1> 
          */
         
         
         // Podd-Science
         this.testRepositoryConnection.add(pScienceOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pScienceVersionURI,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION,
                 pScienceVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceOntologyURI, OWL.IMPORTS, pbVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pScienceInferredURI, this.schemaGraph);
         this.testRepositoryConnection.add(pScienceVersionURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                 pScienceInferredURI, this.schemaGraph);
         
         // Podd-Plant
         this.testRepositoryConnection.add(pPlantOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantVersionURIv2, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantInferredURIv2, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
 
         this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pPlantVersionURI,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pPlantVersionURIv2,
                 this.schemaGraph);
         
         this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, pPlantVersionURIv2,
                 this.schemaGraph);
         this.testRepositoryConnection.add(pPlantOntologyURI, OWL.IMPORTS, pScienceVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantOntologyURI, OWL.IMPORTS, pbVersionURI, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pPlantInferredURIv2, this.schemaGraph);
         this.testRepositoryConnection.add(pPlantVersionURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                 pPlantInferredURI, this.schemaGraph);
 
         this.testRepositoryConnection.add(pPlantVersionURIv2, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                 pPlantInferredURIv2, this.schemaGraph);
 
         return this.schemaGraph;
     }
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
         this.artifactGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-mgt-graph:");
         this.schemaGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-mgt-graph:");
         
         this.testPoddSesameManager = this.getNewPoddSesameManagerInstance();
         Assert.assertNotNull("Null implementation of test OWLManager", this.testPoddSesameManager);
         
         // create a memory Repository for tests
         this.testRepository = new SailRepository(new MemoryStore());
         this.testRepository.initialize();
         this.testRepositoryConnection = this.testRepository.getConnection();
         this.testRepositoryConnection.begin();
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
         this.testRepositoryConnection.rollback();
         this.testRepositoryConnection.close();
         this.testRepository.shutDown();
         
         this.testPoddSesameManager = null;
     }
     
     @Test
     public void testDeleteOntologiesSingleValid() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentArtifactVersion(
                         IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                         artifactGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getInferredOntologyIRI());
         
         this.testPoddSesameManager.deleteOntologies(Arrays.asList(inferredOntologyID), this.testRepositoryConnection,
                 artifactGraph);
         
         try
         {
             this.testPoddSesameManager.getCurrentArtifactVersion(inferredOntologyID.getOntologyIRI(),
                     this.testRepositoryConnection, artifactGraph);
             Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                     e.getMessage());
             Assert.assertEquals(inferredOntologyID.getOntologyIRI(), e.getOntologyID());
         }
         
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentArtifactVersionWithNullOntologyIRI() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         try
         {
             this.testPoddSesameManager.getCurrentArtifactVersion(null, this.testRepositoryConnection, artifactGraph);
             Assert.fail("Should have thrown a RuntimeException");
         }
         catch(final RuntimeException e)
         {
             Assert.assertTrue("Not a NullPointerException as expected", e instanceof NullPointerException);
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentArtifactVersionWithOntologyIRI() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentArtifactVersion(
                         IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                         artifactGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentArtifactVersionWithUnmanagedOntologyIRI() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         final IRI ontologyIRI = IRI.create("http://purl.org/podd/no-such-artifact:999");
         try
         {
             this.testPoddSesameManager.getCurrentArtifactVersion(ontologyIRI, this.testRepositoryConnection,
                     artifactGraph);
             Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
         }
         catch(final UnmanagedArtifactIRIException e)
         {
             Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                     e.getMessage());
             Assert.assertEquals(ontologyIRI, e.getOntologyID());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentArtifactVersionWithVersionIRI() throws Exception
     {
         // prepare: create artifact management graph
         final URI artifactGraph = this.populateArtifactManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentArtifactVersion(
                         IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                         artifactGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentSchemaVersionWithNullOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         try
         {
             this.testPoddSesameManager.getCurrentSchemaVersion(null, this.testRepositoryConnection, schemaGraph);
             Assert.fail("Should have thrown a RuntimeException");
         }
         catch(final RuntimeException e)
         {
             Assert.assertTrue("Not a NullPointerException as expected", e instanceof NullPointerException);
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentSchemaVersionWithOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentSchemaVersion(IRI.create("http://purl.org/podd/ns/poddBase"),
                         this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version",
                 IRI.create("http://purl.org/podd/ns/version/poddBase/1"), inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentSchemaVersionWithUnmanagedOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         final IRI ontologyIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/999");
         try
         {
             this.testPoddSesameManager.getCurrentSchemaVersion(ontologyIRI, this.testRepositoryConnection, schemaGraph);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                     e.getMessage());
             Assert.assertEquals(ontologyIRI, e.getOntologyID());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Test
     public void testGetCurrentSchemaVersionWithVersionIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentSchemaVersion(
                         IRI.create("http://purl.org/podd/ns/version/poddScience/27"), this.testRepositoryConnection,
                         schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version",
                 IRI.create("http://purl.org/podd/ns/version/poddScience/27"), inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddScience/43"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
 
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      * A non-current version IRI is passed in to sesameManager.getCurrentSchemaVersion() with the
      * aim of getting the current Ontology ID in return.
      */
     @Test
     public void testGetCurrentSchemaVersionWithVersionIRINotCurrent() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID inferredOntologyID =
                 this.testPoddSesameManager.getCurrentSchemaVersion(
                         IRI.create("http://purl.org/podd/ns/version/poddPlant/1"), this.testRepositoryConnection,
                         schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
         Assert.assertEquals("Not the expected current version",
                 IRI.create("http://purl.org/podd/ns/version/poddPlant/2"), inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Not the expected current inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2"),
                 inferredOntologyID.getInferredOntologyIRI());
     }    
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getDirectImports(RepositoryConnection, URI)}.
      */
     @Test
     public void testGetDirectImports() throws Exception
     {
         final String resourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         final Repository testRepository = new SailRepository(new MemoryStore());
         testRepository.initialize();
         
         this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
         
         // invoke method under test:
         final Set<IRI> importedOntologyIRIs =
                 this.testPoddSesameManager.getDirectImports(this.testRepositoryConnection, context);
         
         // verify:
         Assert.assertNotNull("No imports could be found", importedOntologyIRIs);
         Assert.assertEquals("Incorrect number of imports found", 4, importedOntologyIRIs.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectTypes(InferredOWLOntologyID, URI, RepositoryConnection)}
      * .
      */
     @Test
     public void testGetObjectTypes() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         final InferredOWLOntologyID ontologyID1 =
                 this.loadOntologyFromResource(TEST_ARTIFACT_BASIC_20130206_TTL,
                         TEST_ARTIFACT_BASIC_20130206_INFERRED_TTL, RDFFormat.TURTLE);
         
         final String[] objectUris =
                 { "http://purl.org/podd/basic-1-20130206/object:2966",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                         "http://purl.org/podd/ns/poddScience#ANZSRC_NotApplicable", };
         
         final String[] expectedTypes =
                 { "http://purl.org/podd/ns/poddScience#Project", "http://purl.org/podd/ns/poddScience#Genotype",
                         "http://purl.org/podd/ns/poddScience#Material",
                         "http://purl.org/podd/ns/poddScience#ANZSRCAssertion", };
         
         // test in a loop these PODD objects for their types
         for(int i = 0; i < objectUris.length; i++)
         {
             final URI objectUri = ValueFactoryImpl.getInstance().createURI(objectUris[i]);
             
             final List<URI> objectTypes =
                     this.testPoddSesameManager.getObjectTypes(ontologyID1, objectUri, this.testRepositoryConnection);
             
             // verify:
             Assert.assertNotNull("Type was null", objectTypes);
             Assert.assertFalse("No Type found", objectTypes.isEmpty());
             Assert.assertEquals("Wrong type", ValueFactoryImpl.getInstance().createURI(expectedTypes[i]),
                     objectTypes.get(0));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectLabel(InferredOWLOntologyID, URI, RepositoryConnection)}
      * .
      */
     @Test
     public void testGetObjectLabel() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TEST_ARTIFACT_BASIC_20130206_TTL,
                         TEST_ARTIFACT_BASIC_20130206_INFERRED_TTL, RDFFormat.TURTLE);
         
         final String[] objectUris =
                 { "http://purl.org/podd/basic-1-20130206/object:2966",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#Demo-Genotype",
                         "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                         "http://purl.org/podd/ns/poddScience#ANZSRC_NotApplicable", };
         
         final String[] expectedLabels =
                 { "Project#2012-0006_ Cotton Leaf Morphology", "Demo genotype", "Squeekee material", "Not Applicable", };
         final String[] expectedDescriptions = { "Characterising normal and okra leaf shapes", null, null, null };
         
         // test in a loop these PODD objects for their types
         for(int i = 0; i < objectUris.length; i++)
         {
             final URI objectUri = ValueFactoryImpl.getInstance().createURI(objectUris[i]);
             
             final PoddObjectLabel objectLabel =
                     this.testPoddSesameManager.getObjectLabel(ontologyID, objectUri, this.testRepositoryConnection);
             
             // verify:
             Assert.assertNotNull("PoddObjectLabel was null", objectLabel);
             Assert.assertEquals("Incorrect Object URI", objectUri, objectLabel.getObjectURI());
             Assert.assertEquals("Wrong Label", expectedLabels[i], objectLabel.getLabel());
             Assert.assertEquals("Wrong Description", expectedDescriptions[i], objectLabel.getDescription());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectDetailsForDisplay(InferredOWLOntologyID, URI, RepositoryConnection)}
      * .
      */
     @Test
     public void testGetObjectDetailsForDisplayWithTopObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TEST_ARTIFACT_BASIC_20130206_TTL,
                         TEST_ARTIFACT_BASIC_20130206_INFERRED_TTL, RDFFormat.TURTLE);
         
         final URI objectUri =
                 ValueFactoryImpl.getInstance().createURI(
                         "http://purl.org/podd/basic-1-20130206/object:2966");
         
         Model displayModel =
                 this.testPoddSesameManager.getObjectDetailsForDisplay(ontologyID, objectUri,
                         this.testRepositoryConnection);
         
         // verify:
         Assert.assertNotNull("Display Model is null", displayModel);
         Assert.assertFalse("Display Model is empty", displayModel.isEmpty());
         Assert.assertEquals("Display Model not of expected size", 42, displayModel.size());
         Assert.assertEquals("Not the expected no. of statements about object", 17, displayModel.filter(objectUri, null, null).size());
         
         Assert.assertEquals("Expected 1 hasLeadInstitution statement", 1, displayModel.filter(objectUri, 
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#hasLeadInstitution")
                 , null).size());
         
         Assert.assertEquals("Unexpected Lead Institution", "CSIRO HRPPC", displayModel.filter(objectUri, 
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#hasLeadInstitution"), null).objectString());
 
         Assert.assertTrue("Expected content missing in display model",
                displayModel.toString().contains("Proceedings of the IEEE eScience 2010"));
     }
 
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getObjectDetailsForDisplay(InferredOWLOntologyID, URI, RepositoryConnection)}
      * .
      */
     @Test
     public void testGetObjectDetailsForDisplayWithPublicationObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         InferredOWLOntologyID ontologyID =
                 this.loadOntologyFromResource(TEST_ARTIFACT_BASIC_20130206_TTL,
                         TEST_ARTIFACT_BASIC_20130206_INFERRED_TTL, RDFFormat.TURTLE);
         
         final URI objectUri =
                 ValueFactoryImpl.getInstance().createURI(
                         "http://purl.org/podd/basic-2-20130206/artifact:1#publication45");
         
         Model displayModel =
                 this.testPoddSesameManager.getObjectDetailsForDisplay(ontologyID, objectUri,
                         this.testRepositoryConnection);
         
         // verify:
         Assert.assertNotNull("Display Model is null", displayModel);
         Assert.assertFalse("Display Model is empty", displayModel.isEmpty());
         Assert.assertEquals("Display Model not of expected size", 15, displayModel.size());
         Assert.assertEquals("Not the expected no. of statements about object", 7, displayModel.filter(objectUri, null, null).size());
         
         Assert.assertEquals("Expected 1 hasPURL statement", 1, displayModel.filter(objectUri, 
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#hasPURL")
                 , null).size());
         
         Assert.assertEquals("Unexpected PURL value", "http://dx.doi.org/10.1109/eScience.2010.44", displayModel.filter(objectUri, 
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#hasPURL"), null).objectString());
 
         // verify: a string search for some content
         Assert.assertTrue("Expected content missing in display model",
                 displayModel.toString().contains("Proceedings of the IEEE eScience 2010"));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologiesEmptyAllVersions() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final Collection<InferredOWLOntologyID> ontologies =
                 this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
         
         Assert.assertNotNull(ontologies);
         Assert.assertTrue(ontologies.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologiesEmptyOnlyCurrentVersions() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final Collection<InferredOWLOntologyID> ontologies =
                 this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
         
         Assert.assertNotNull(ontologies);
         Assert.assertTrue(ontologies.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologiesSingleAllVersions() throws Exception
     {
         final URI context = this.populateArtifactManagementGraph();
         
         final Collection<InferredOWLOntologyID> ontologies =
                 this.testPoddSesameManager.getOntologies(false, this.testRepositoryConnection, context);
         
         Assert.assertNotNull(ontologies);
         Assert.assertEquals(1, ontologies.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
      * .
      */
     @Test
     public void testGetOntologiesSingleOnlyCurrentVersions() throws Exception
     {
         final URI context = this.populateArtifactManagementGraph();
         
         final Collection<InferredOWLOntologyID> ontologies =
                 this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
         
         Assert.assertNotNull(ontologies);
         Assert.assertEquals(1, ontologies.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getOntologyIRI(RepositoryConnection, URI)}.
      */
     @Test
     public void testGetOntologyIRI() throws Exception
     {
         final String resourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
         
         // invoke method under test:
         final IRI ontologyIRI = this.testPoddSesameManager.getOntologyIRI(this.testRepositoryConnection, context);
         
         // verify:
         Assert.assertNotNull("Ontology IRI was null", ontologyIRI);
         Assert.assertEquals("Wrong Ontology IRI", "urn:temp:uuid:artifact:1", ontologyIRI.toString());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . Null is passed in to sesameManager.getSchemaVersion() and a NullPointerException is
      * expected.
      * 
      */
     @Test
     public void testGetSchemaVersionWithNullOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         try
         {
             this.testPoddSesameManager.getSchemaVersion(null, this.testRepositoryConnection, schemaGraph);
             Assert.fail("Should have thrown a RuntimeException");
             
         }
         catch(final RuntimeException e)
         {
             Assert.assertTrue("Not a NullPointerException as expected", e instanceof NullPointerException);
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . An unknown IRI is passed in to sesameManager.getSchemaVersion() and an
      * UnmanagedSchemaIRIException is expected.
      */
     @Test
     public void testGetSchemaVersionWithUnmanagedVersionIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         IRI unmanagedSchemaVersionIri = IRI.create("http://purl.org/podd/ns/version/poddPlant/9999");
         
         try
         {
             this.testPoddSesameManager.getSchemaVersion(unmanagedSchemaVersionIri, this.testRepositoryConnection,
                     schemaGraph);
             Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
         }
         catch(final UnmanagedSchemaIRIException e)
         {
             Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                     e.getMessage());
             Assert.assertEquals(unmanagedSchemaVersionIri, e.getOntologyID());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . An ontology IRI is passed in to sesameManager.getSchemaVersion(). The current version's
      * Ontology ID should be returned.
      */
     @Test
     public void testGetSchemaVersionWithOntologyIRI() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID ontologyID =
                 this.testPoddSesameManager.getSchemaVersion(IRI.create("http://purl.org/podd/ns/poddPlant"),
                         this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL OntologyID", ontologyID);
         Assert.assertEquals("Not the expected version",
                 IRI.create("http://purl.org/podd/ns/version/poddPlant/2"), ontologyID.getVersionIRI());
         Assert.assertEquals("Not the expected inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2"),
                 ontologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . The current version IRI is passed in to sesameManager.getSchemaVersion() with the aim of
      * getting the Ontology ID of that version in return.
      */
     @Test
     public void testGetSchemaVersionWithVersionIRICurrent() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID ontologyID =
                 this.testPoddSesameManager.getSchemaVersion(IRI.create("http://purl.org/podd/ns/version/poddPlant/2"),
                         this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL ontologyID", ontologyID);
         Assert.assertEquals("Not the expected version",
                 IRI.create("http://purl.org/podd/ns/version/poddPlant/2"), ontologyID.getVersionIRI());
         Assert.assertEquals("Not the expected inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/2"),
                 ontologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getSchemaVersion(IRI, RepositoryConnection, URI)}
      * . A non-current version IRI is passed in to sesameManager.getSchemaVersion() with the aim of
      * getting the Ontology ID of that version in return.
      */
     @Test
     public void testGetSchemaVersionWithVersionIRINotCurrent() throws Exception
     {
         // prepare: create schema management graph
         final URI schemaGraph = this.populateSchemaManagementGraph();
         
         // invoke test method:
         final InferredOWLOntologyID ontologyID =
                 this.testPoddSesameManager.getSchemaVersion(IRI.create("http://purl.org/podd/ns/version/poddPlant/1"),
                         this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertNotNull("Returned NULL ontologyID", ontologyID);
         Assert.assertEquals("Not the expected version",
                 IRI.create("http://purl.org/podd/ns/version/poddPlant/1"), ontologyID.getVersionIRI());
         Assert.assertEquals("Not the expected inferred version",
                 IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddPlant/1"),
                 ontologyID.getInferredOntologyIRI());
     }
 
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getTopObjects(InferredOWLOntologyID, RepositoryConnection)}
      * . Test retrieving Top Objects from an artifact with exactly one top object.
      */
     @Test
     public void testGetTopObjectsFromArtifactWithOneTopObject() throws Exception
     {
         // prepare: load test artifact
         InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(TEST_ARTIFACT_BASIC_20130206_TTL,
                         TEST_ARTIFACT_BASIC_20130206_INFERRED_TTL, RDFFormat.TURTLE);
         
         // DebugUtils.printContexts(testRepositoryConnection);
         // DebugUtils.printContents(testRepositoryConnection,
         // nextOntologyID.getVersionIRI().toOpenRDFURI());
         
         final List<URI> topObjectList =
                 this.testPoddSesameManager.getTopObjects(nextOntologyID, testRepositoryConnection);
         
         Assert.assertEquals("Expected 1 top object", 1, topObjectList.size());
         Assert.assertEquals("Not the expected top object URI",
                 ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/basic-1-20130206/object:2966"),
                 topObjectList.get(0));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getTopObjects(InferredOWLOntologyID, RepositoryConnection)}
      * . Test retrieving Top Objects from an artifact which has more than one top object. A PODD
      * artifact should currently have only 1 top object.
      */
     @Test
     public void testGetTopObjectsFromArtifactWithSeveralTopObjects() throws Exception
     {
         final String testResourcePath = "/test/artifacts/3-topobjects.ttl";
         // prepare: load test artifact
         InferredOWLOntologyID nextOntologyID = this.loadOntologyFromResource(testResourcePath, null, RDFFormat.TURTLE);
         
         final List<URI> topObjectList =
                 this.testPoddSesameManager.getTopObjects(nextOntologyID, testRepositoryConnection);
         
         Assert.assertEquals("Expected 3 top objects", 3, topObjectList.size());
         
         final List<String> expectedUriList =
                 Arrays.asList(new String[] { "http://purl.org/podd/basic-1-20130205/object:2966",
                         "http://purl.org/podd/basic-1-20130205/object:2977",
                         "http://purl.org/podd/basic-1-20130205/object:2988" });
         
         for(final URI topObjectUri : topObjectList)
         {
             Assert.assertTrue("Unexpected top object", expectedUriList.contains(topObjectUri.stringValue()));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getWeightedProperties(InferredOWLOntologyID, URI, RepositoryConnection)}
      * . getWeightedProperties() is invoked for an internal object of an artifact.
      */
     @Test
     public void testGetWeightedPropertiesOfAnInternalObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(TEST_ARTIFACT_BASIC_20130206_TTL,
                         TEST_ARTIFACT_BASIC_20130206_INFERRED_TTL, RDFFormat.TURTLE);
         
         Assert.assertEquals("http://purl.org/podd/basic-2-20130206/artifact:1", nextOntologyID.getOntologyIRI()
                 .toString());
         Assert.assertEquals("http://purl.org/podd/basic-2-20130206/artifact:version:1", nextOntologyID.getVersionIRI()
                 .toString());
         Assert.assertEquals(
                 "urn:podd:inferred:ontologyiriprefix:http://purl.org/podd/basic-2-20130206/artifact:1:version:1",
                 nextOntologyID.getInferredOntologyIRI().toString());
         
         URI internalObjectUri =
                 ValueFactoryImpl.getInstance().createURI(
                         "http://purl.org/podd/basic-2-20130206/artifact:1#publication45");
         
         final List<URI> orderedPropertyUris =
                 this.testPoddSesameManager.getWeightedProperties(nextOntologyID, internalObjectUri,
                         testRepositoryConnection);
         
         // verify:
         Assert.assertEquals("Incorrect number of statements about Internal Object", 6, orderedPropertyUris.size());
         
         final String[] expectedUris =
                 { "http://purl.org/dc/terms/creator", "http://purl.org/dc/terms/created",
                         "http://purl.org/podd/ns/poddBase#hasPURL", "http://purl.org/podd/ns/poddScience#hasAbstract",
                         "http://purl.org/podd/ns/poddScience#publishedIn",
                         "http://purl.org/podd/ns/poddScience#hasYear", };
         for(int i = 0; i < orderedPropertyUris.size(); i++)
         {
             Assert.assertEquals("Property URI not in expected position",
                     ValueFactoryImpl.getInstance().createURI(expectedUris[i]), orderedPropertyUris.get(i));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#getWeightedProperties(InferredOWLOntologyID, URI, RepositoryConnection)}
      * .
      * 
      * getWeightedProperties() is invoked for the top object of an artifact.
      */
     @Test
     public void testGetWeightedPropertiesOfATopObject() throws Exception
     {
         // prepare: load schema ontologies and test artifact
         this.loadSchemaOntologies();
         InferredOWLOntologyID nextOntologyID =
                 this.loadOntologyFromResource(TEST_ARTIFACT_BASIC_20130206_TTL,
                         TEST_ARTIFACT_BASIC_20130206_INFERRED_TTL, RDFFormat.TURTLE);
         
         final URI topObjectUri = this.testPoddSesameManager.getTopObjectIRI(nextOntologyID, testRepositoryConnection);
         
         final List<URI> orderedPropertyUris =
                 this.testPoddSesameManager
                         .getWeightedProperties(nextOntologyID, topObjectUri, testRepositoryConnection);
         
         // verify:
         Assert.assertEquals("Incorrect number of statements about Top Object", 13, orderedPropertyUris.size());
         
         final String[] expectedUris =
                 { "http://purl.org/podd/ns/poddScience#hasANZSRC", "http://purl.org/podd/ns/poddBase#createdAt",
                         "http://purl.org/dc/terms/creator",
                         "http://purl.org/podd/ns/poddBase#hasPrincipalInvestigator",
                         "http://purl.org/podd/ns/poddScience#hasAnalysis",
                         "http://purl.org/podd/ns/poddScience#hasInvestigation",
                         "http://purl.org/podd/ns/poddScience#hasProcess",
                         "http://purl.org/podd/ns/poddScience#hasProjectPlan",
                         "http://purl.org/podd/ns/poddScience#hasPublication",
                         "http://purl.org/podd/ns/poddBase#hasPublicationStatus",
                         "http://purl.org/podd/ns/poddBase#hasLeadInstitution",
                         "http://purl.org/podd/ns/poddBase#hasStartDate",
                         "http://purl.org/podd/ns/poddBase#hasTopObjectStatus" };
         for(int i = 0; i < orderedPropertyUris.size(); i++)
         {
             Assert.assertEquals("Property URI not in expected position",
                     ValueFactoryImpl.getInstance().createURI(expectedUris[i]), orderedPropertyUris.get(i));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testIsPublishedWithEmptyOntology() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final OWLOntologyID emptyOntologyID = new OWLOntologyID();
         
         try
         {
             this.testPoddSesameManager.isPublished(emptyOntologyID, this.testRepositoryConnection, context);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testIsPublishedWithNullOntology() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         try
         {
             this.testPoddSesameManager.isPublished(null, this.testRepositoryConnection, context);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * . This test depends on imported PODD Base ontology being resolvable from
      * http://purl.org/podd/ns/poddBase.
      */
     @Test
     public void testIsPublishedWithPublishedArtifact() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final String testResourcePath = "/test/artifacts/basicProject-1-published.rdf";
         final URI versionUri = ValueFactoryImpl.getInstance().createURI("urn:temp:uuid:artifact:version:55");
         
         final boolean isPublished = this.internalTestIsPublished(testResourcePath, 23, versionUri, context);
         Assert.assertEquals("Did not identify artifact as Published", true, isPublished);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * . This test depends on imported PODD Base ontology being resolvable from
      * http://purl.org/podd/ns/poddBase.
      */
     @Test
     public void testIsPublishedWithUnPublishedArtifact() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
         
         final String testResourcePath = "/test/artifacts/basicProject-1.rdf";
         final URI versionUri = ValueFactoryImpl.getInstance().createURI("urn:temp:artifact:version:1");
         final boolean isPublished = this.internalTestIsPublished(testResourcePath, 23, versionUri, context);
         Assert.assertEquals("Did not identify artifact as Not Published", false, isPublished);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateCurrentManagedSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      */
     @Test
     public final void testUpdateCurrentManagedSchemaOntologyVersionWithoutUpdate() throws Exception
     {
         final IRI pOntologyIRI = IRI.create("http://purl.org/podd/ns/poddBase");
         final IRI pVersionIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/1");
         final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
         final InferredOWLOntologyID nextOntologyID =
                 new InferredOWLOntologyID(pOntologyIRI, pVersionIRI, pInferredVersionIRI);
         
         // invoke method under test
         this.testPoddSesameManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         
         this.verifyManagementGraphContents(6, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRI);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateCurrentManagedSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      */
     @Test
     public final void testUpdateCurrentManagedSchemaOntologyVersionWithUpdate() throws Exception
     {
         final IRI pOntologyIRI = IRI.create("http://purl.org/podd/ns/poddBase");
         final IRI pVersionIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/1");
         final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
         final InferredOWLOntologyID nextOntologyID =
                 new InferredOWLOntologyID(pOntologyIRI, pVersionIRI, pInferredVersionIRI);
         
         // first setting of schema versions in mgt graph
         this.testPoddSesameManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyID, false,
                 this.testRepositoryConnection, this.schemaGraph);
         this.verifyManagementGraphContents(6, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRI);
         
         final IRI pVersionIRIUpdated = IRI.create("http://purl.org/podd/ns/version/poddBase/4");
         final IRI pInferredVersionIRIUpdated = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/5");
         final InferredOWLOntologyID nextOntologyIDUpdated =
                 new InferredOWLOntologyID(pOntologyIRI, pVersionIRIUpdated, pInferredVersionIRIUpdated);
         
         // invoke with "updateCurrent" disallowed
         this.testPoddSesameManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyIDUpdated, false,
                 this.testRepositoryConnection, this.schemaGraph);
         
         // verify only inferred ontology version is updated
         this.verifyManagementGraphContents(9, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRIUpdated);
         
         // invoke with "updateCurrent" allowed
         this.testPoddSesameManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyIDUpdated, true,
                 this.testRepositoryConnection, this.schemaGraph);
         
         // verify both ontology current version and inferred ontology version haven been updated
         this.verifyManagementGraphContents(9, this.schemaGraph, pOntologyIRI, pVersionIRIUpdated,
                 pInferredVersionIRIUpdated);
         
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      * 
      * Tests that when updating an artifact version, repository content for previous versions of the
      * artifact (both asserted and inferred statements) are deleted.
      * 
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionForDeletingPreviousVersionContent() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyIDv1 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // prepare: add dummy statements in relevant contexts to represent test artifact
         final URI subject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
         this.testRepositoryConnection.add(subject, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                 PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pVersionIRIv1.toOpenRDFURI());
         
         final URI inferredSubject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
         this.testRepositoryConnection.add(inferredSubject, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                 PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pInferredVersionIRIv1.toOpenRDFURI());
         
         // verify: contexts populated for test artifact
         Assert.assertEquals("Asserted graph should have 1 statement", 1,
                 this.testRepositoryConnection.size(pVersionIRIv1.toOpenRDFURI()));
         Assert.assertEquals("Inferred graph should have 1 statement", 1,
                 this.testRepositoryConnection.size(pInferredVersionIRIv1.toOpenRDFURI()));
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                 this.testRepositoryConnection, this.artifactGraph);
         
         // verify: artifact management graph
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // prepare: version 2 of test artifact
         final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
         final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
         final InferredOWLOntologyID nextOntologyIDv2 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         // prepare: add dummy statements in relevant contexts for version 2 of test artifact
         final URI subject2 = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
         this.testRepositoryConnection.add(subject2, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                 PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pVersionIRIv2.toOpenRDFURI());
         
         final URI inferredSubject2 = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
         this.testRepositoryConnection.add(inferredSubject2, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                 PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pInferredVersionIRIv2.toOpenRDFURI());
         
         // verify: contexts populated for test artifact
         Assert.assertEquals("Asserted graph should have 1 statement", 1,
                 this.testRepositoryConnection.size(pVersionIRIv2.toOpenRDFURI()));
         Assert.assertEquals("Inferred graph should have 1 statement", 1,
                 this.testRepositoryConnection.size(pInferredVersionIRIv2.toOpenRDFURI()));
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, true,
                 this.testRepositoryConnection, this.artifactGraph);
         
         DebugUtils.printContexts(testRepositoryConnection);
         DebugUtils.printContents(testRepositoryConnection, this.artifactGraph);
         
         // verify:
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         DebugUtils.printContents(testRepositoryConnection, pInferredVersionIRIv1.toOpenRDFURI());
         
         Assert.assertEquals("Old asserted graph should be deleted", 0,
                 this.testRepositoryConnection.size(pVersionIRIv1.toOpenRDFURI()));
         Assert.assertEquals("Old inferred graph should be deleted", 0,
                 this.testRepositoryConnection.size(pInferredVersionIRIv1.toOpenRDFURI()));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      * 
      * Details of an existing artifact are updated in the management graph.
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionWithExistingArtifact() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersion1IRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyIDv1 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersion1IRIv1, pInferredVersionIRIv1);
         
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                 this.testRepositoryConnection, this.artifactGraph);
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersion1IRIv1, pInferredVersionIRIv1);
         
         // prepare: update artifact version
         final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
         final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
         final InferredOWLOntologyID nextOntologyIDv2 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, true,
                 this.testRepositoryConnection, this.artifactGraph);
         
         // verify: new version overwrites all references to the old version, and number of
         // statements stays the same
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      * 
      * Details of a new artifact are added to the management graph.
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionWithNewArtifact() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersionIRI = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyID =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRI, pInferredVersionIRI);
         
         // invoke method under test
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyID, false,
                 this.testRepositoryConnection, this.artifactGraph);
         
         // verify:
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRI, pInferredVersionIRI);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      * 
      * Details of an existing artifact are updated in the management graph, with "updateCurrent" =
      * false. The "current version" does not change for base/asserted ontology while the current
      * inferred version is updated.
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionWithoutUpdateCurrent() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyIDv1 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                 this.testRepositoryConnection, this.artifactGraph);
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // prepare: version 2
         final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
         final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
         final InferredOWLOntologyID nextOntologyIDv2 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         // invoke with "updateCurrent" disallowed
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, false,
                 this.testRepositoryConnection, this.artifactGraph);
         
         // verify:
         this.verifyManagementGraphContents(11, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
      * .
      */
     @Test
     public final void testUpdateManagedPoddArtifactVersionWithUpdate() throws Exception
     {
         // prepare: add entries in the artifact graph for a test artifact
         final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
         final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
         final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
         final InferredOWLOntologyID nextOntologyIDv1 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                 this.testRepositoryConnection, this.artifactGraph);
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // prepare: version 2
         final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
         final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
         final InferredOWLOntologyID nextOntologyIDv2 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
         
         // invoke with "updateCurrent" disallowed
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, false,
                 this.testRepositoryConnection, this.artifactGraph);
         
         DebugUtils.printContexts(testRepositoryConnection);
         DebugUtils.printContents(testRepositoryConnection, artifactGraph);
         
         // verify: version 2 is inserted, as verified by the extra statements, but the current
         // versions are not modified this time
         this.verifyManagementGraphContents(11, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
         
         // update to version 3
         final IRI pVersionIRIv3 = IRI.create("http://purl.org/abc-def/artifact:1:version:3");
         final IRI pInferredVersionIRIv3 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:3");
         final InferredOWLOntologyID nextOntologyIDv3 =
                 new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv3, pInferredVersionIRIv3);
         
         // invoke with "updateCurrent" allowed
         this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv3, true,
                 this.testRepositoryConnection, this.artifactGraph);
         
         this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv3, pInferredVersionIRIv3);
     }
     
     /**
      * Helper method to verify the contents of a management graph
      * 
      * @param graphSize
      *            Expected size of the management graph
      * @param testGraph
      *            The management context/graph
      * @param ontologyIRI
      *            Ontology/artifact IRI to check against
      * @param expectedVersionIRI
      *            Expected current version IRI of the given ontology/artifact
      * @param expectedInferredVersionIRI
      *            Expected inferred version of the given ontology/artifact
      * @throws Exception
      */
     private void verifyManagementGraphContents(final int graphSize, final URI testGraph, final IRI ontologyIRI,
             final IRI expectedVersionIRI, final IRI expectedInferredVersionIRI) throws Exception
     {
         Assert.assertEquals("Graph not of expected size", graphSize, this.testRepositoryConnection.size(testGraph));
         
         Model stmtList =
                 new LinkedHashModel(Iterations.asList(this.testRepositoryConnection.getStatements(null,
                         PoddRdfConstants.OMV_CURRENT_VERSION, null, false, testGraph)));
         Assert.assertEquals("Graph should have one OMV_CURRENT_VERSION statement", 1, stmtList.size());
         Assert.assertEquals("Wrong ontology IRI", ontologyIRI.toOpenRDFURI(), stmtList.subjects().iterator().next());
         Assert.assertEquals("Wrong version IRI", expectedVersionIRI.toOpenRDFURI(), stmtList.objectURI());
         
         Model inferredVersionStatementList =
                 new LinkedHashModel(Iterations.asList(this.testRepositoryConnection.getStatements(null,
                         PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null, false, testGraph)));
         Assert.assertEquals("Graph should have one CURRENT_INFERRED_VERSION statement", 1,
                 inferredVersionStatementList.size());
         Assert.assertEquals("Wrong ontology IRI", ontologyIRI.toOpenRDFURI(), inferredVersionStatementList.subjects()
                 .iterator().next());
         Assert.assertEquals("Wrong version IRI", expectedInferredVersionIRI.toOpenRDFURI(),
                 inferredVersionStatementList.objectURI());
     }
 }
