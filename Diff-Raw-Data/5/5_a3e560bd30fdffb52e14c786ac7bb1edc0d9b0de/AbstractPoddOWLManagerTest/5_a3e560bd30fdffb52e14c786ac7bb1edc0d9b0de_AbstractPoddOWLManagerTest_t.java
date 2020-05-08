 /**
  * PODD is an OWL ontology database used for scientific project management
  * 
  * Copyright (C) 2009-2013 The University Of Queensland
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.podd.api.test;
 
 import info.aduna.iteration.Iterations;
 
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.openrdf.model.BNode;
 import org.openrdf.model.Model;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.TreeModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.util.ModelUtil;
 import org.openrdf.model.util.Namespaces;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.sail.SailRepository;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.openrdf.sail.memory.MemoryStore;
 import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
 import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
 import org.semanticweb.owlapi.io.StreamDocumentSource;
 import org.semanticweb.owlapi.io.UnparsableOntologyException;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntology;
 import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.semanticweb.owlapi.model.OWLOntologyManager;
 import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
 import org.semanticweb.owlapi.profiles.OWLProfile;
 import org.semanticweb.owlapi.reasoner.OWLReasoner;
 import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
 import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.api.PoddOWLManager;
 import com.github.podd.exception.EmptyOntologyException;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * Abstract test to verify that the PoddOWLManager API contract is followed by implementations.
  * 
  * TODO: add test cases for non-default cases (e.g. empty/null/invalid/non-matching values)
  * 
  * @author kutila
  * 
  */
 public abstract class AbstractPoddOWLManagerTest
 {
     protected Logger log = LoggerFactory.getLogger(this.getClass());
     
     protected PoddOWLManager testOWLManager;
     
     private Repository testRepository;
     
     protected RepositoryConnection testRepositoryConnection;
     
     /**
      * @return A new OWLReasonerFactory instance for use with the PoddOWLManager
      */
     protected abstract OWLReasonerFactory getNewOWLReasonerFactoryInstance();
     
     /**
      * @return A new instance of PoddOWLManager, for each call to this method
      */
     protected abstract PoddOWLManager getNewPoddOWLManagerInstance();
     
     protected OWLOntology independentlyLoadOntology(final OWLOntologyManager testOWLOntologyManager,
             final String resourcePath) throws Exception
     {
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         return testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
     }
     
     /**
      * Helper method which loads podd:dcTerms, podd:foaf and podd:User schema ontologies.
      */
     private void loadDcFoafAndPoddUserSchemaOntologies() throws Exception
     {
         this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_DCTERMS, RDFFormat.RDFXML,
                 TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE,
                 TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED);
         this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_FOAF, RDFFormat.RDFXML,
                 TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE, TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_INFERRED);
         this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_USER, RDFFormat.RDFXML,
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE,
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED);
     }
     
     /**
      * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
      * 
      * @param resourcePath
      * @param format
      * @param assertedStatements
      * @param inferredStatements
      * @return
      * @throws Exception
      */
     private InferredOWLOntologyID loadInferStoreOntology(final String resourcePath, final RDFFormat format,
             final long assertedStatements, final long inferredStatements) throws Exception
     {
         // load ontology to OWLManager
         final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
         Assert.assertNotNull("Could not find resource", inputStream);
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         format.getDefaultMIMEType()));
         final OWLOntology loadedBaseOntology = this.testOWLManager.loadOntology(owlSource);
         
         this.testOWLManager.dumpOntologyToRepository(loadedBaseOntology, this.testRepositoryConnection);
         
         // infer statements and dump to repository
         final InferredOWLOntologyID inferredOntologyID =
                 this.testOWLManager.inferStatements(loadedBaseOntology, this.testRepositoryConnection);
         
         // verify statement counts
         final URI versionURI = loadedBaseOntology.getOntologyID().getVersionIRI().toOpenRDFURI();
         Assert.assertEquals("Wrong statement count", assertedStatements, this.testRepositoryConnection.size(versionURI));
         
         final URI inferredOntologyURI = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
         
         // DebugUtils.printContents(testRepositoryConnection, inferredOntologyURI);
         Assert.assertEquals("Wrong inferred statement count", inferredStatements,
                 this.testRepositoryConnection.size(inferredOntologyURI));
         
         return inferredOntologyID;
     }
     
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception
     {
         this.testOWLManager = this.getNewPoddOWLManagerInstance();
         Assert.assertNotNull("Null implementation of test OWLManager", this.testOWLManager);
         
         // set an OWLOntologyManager for this PoddOWLManager
         final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
         this.testOWLManager.setOWLOntologyManager(manager);
         
         // set a ReasonerFactory for this PoddOWLManager
         final OWLReasonerFactory reasonerFactory = this.getNewOWLReasonerFactoryInstance();
         Assert.assertNotNull("Null implementation of reasoner factory", reasonerFactory);
         this.testOWLManager.setReasonerFactory(reasonerFactory);
         
         // create a memory Repository for tests
         this.testRepository = new SailRepository(new MemoryStore());
         this.testRepository.initialize();
         this.testRepositoryConnection = this.testRepository.getConnection();
        // this.testRepositoryConnection.begin();
     }
     
     /**
      * @throws java.lang.Exception
      */
     @After
     public void tearDown() throws Exception
     {
        // this.testRepositoryConnection.rollback();
         this.testRepositoryConnection.close();
         this.testRepository.shutDown();
         
         this.testOWLManager = null;
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testCacheSchemaOntology() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: load, infer and store PODD-Base ontology
         final InferredOWLOntologyID inferredOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_BASE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
         
         // prepare: remove from cache
         this.testOWLManager.removeCache(inferredOntologyID.getBaseOWLOntologyID());
         this.testOWLManager.removeCache(inferredOntologyID.getInferredOWLOntologyID());
         
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(inferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(inferredOntologyID.getInferredOntologyIRI()));
         
         this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
         
         // verify:
         Assert.assertTrue("Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(inferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertTrue("Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(inferredOntologyID.getInferredOWLOntologyID()));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testCacheSchemaOntologyAlreadyInCache() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: load, infer and store a schema ontology
         final InferredOWLOntologyID inferredOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_BASE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
         
         Assert.assertNotNull("Ontology should already be in memory", this.testOWLManager.getOWLOntologyManager()
                 .getOntology(inferredOntologyID));
         
         // this call will silently return since the ontology is already in cache
         this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
         
         // verify:
         Assert.assertNotNull("Ontology should still be in memory", this.testOWLManager.getOWLOntologyManager()
                 .getOntology(inferredOntologyID));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testCacheSchemaOntologyNotInRepository() throws Exception
     {
         // prepare: a new InferredOWLOntologyID
         final InferredOWLOntologyID inferredOntologyID =
                 new InferredOWLOntologyID(IRI.create("http://purl.org/podd/ns/poddBase"),
                         IRI.create("http://purl.org/podd/ns/version/poddBase/1"),
                         IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1"));
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(inferredOntologyID.getBaseOWLOntologyID()));
         
         try
         {
             this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
             Assert.fail("Should have thrown an EmptyOntologyException");
         }
         catch(final EmptyOntologyException e)
         {
             Assert.assertEquals("Not the expected Exception", "No statements to create an ontology", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testCacheSchemaOntologyWithEmptyOntologyID() throws Exception
     {
         final InferredOWLOntologyID inferredOntologyID = new InferredOWLOntologyID((IRI)null, null, null);
         
         try
         {
             this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * . E.g. Test caching schema ontology "A" A :imports B B :imports C C :imports D
      */
     @Ignore
     @Test
     public void testCacheSchemaOntologyWithIndirectImports() throws Exception
     {
         Assert.fail("TODO: implement me");
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testCacheSchemaOntologyWithNullOntologyID() throws Exception
     {
         try
         {
             this.testOWLManager.cacheSchemaOntology(null, this.testRepositoryConnection, null);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testCacheSchemaOntologyWithOneImport() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: 1) load, infer, store PODD-Base ontology
         final InferredOWLOntologyID pbInferredOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_BASE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
         final URI pbBaseOntologyURI = pbInferredOntologyID.getOntologyIRI().toOpenRDFURI();
         final URI pbVersionURI = pbInferredOntologyID.getVersionIRI().toOpenRDFURI();
         
         // prepare: 2) load, infer, store PODD-Science ontology
         final InferredOWLOntologyID pScienceInferredOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_SCIENCE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED);
         final URI pScienceBaseOntologyURI = pScienceInferredOntologyID.getOntologyIRI().toOpenRDFURI();
         final URI pScienceVersionURI = pScienceInferredOntologyID.getVersionIRI().toOpenRDFURI();
         
         // prepare: 3) remove ontologies from manager cache
         this.testOWLManager.removeCache(pbInferredOntologyID.getBaseOWLOntologyID());
         this.testOWLManager.removeCache(pbInferredOntologyID.getInferredOWLOntologyID());
         this.testOWLManager.removeCache(pScienceInferredOntologyID.getBaseOWLOntologyID());
         this.testOWLManager.removeCache(pScienceInferredOntologyID.getInferredOWLOntologyID());
         
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pbInferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pScienceInferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertFalse(
                 "Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(
                         pScienceInferredOntologyID.getInferredOntologyIRI()));
         
         // prepare: 4) create schema management graph
         final URI schemaGraph = PoddRdfConstants.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
         
         // Podd-Base
         this.testRepositoryConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pbVersionURI,
                 schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pbInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
         
         // Podd-Science
         this.testRepositoryConnection.add(pScienceBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
         this.testRepositoryConnection.add(pScienceBaseOntologyURI, PoddRdfConstants.OWL_VERSION_IRI,
                 pScienceVersionURI, schemaGraph);
         this.testRepositoryConnection.add(pScienceBaseOntologyURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
         this.testRepositoryConnection.add(pScienceBaseOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pScienceInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
         
         // invoke method to test
         this.testOWLManager.cacheSchemaOntology(pScienceInferredOntologyID, this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertTrue("Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pScienceInferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertTrue("Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pbInferredOntologyID.getInferredOntologyIRI()));
         Assert.assertTrue(
                 "Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(
                         pScienceInferredOntologyID.getInferredOWLOntologyID()));
         Assert.assertTrue("Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pbInferredOntologyID.getBaseOWLOntologyID()));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      * Tests caching a schema ontology which (for some reason) does not have an inferred Graph in
      * the repository.
      */
     @Test
     public void testCacheSchemaOntologyWithoutInferences() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: load and store a schema ontology
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         final OWLOntology loadedOntology =
                 this.testOWLManager.getOWLOntologyManager().loadOntologyFromOntologyDocument(inputStream);
         this.testOWLManager.dumpOntologyToRepository(loadedOntology, this.testRepositoryConnection);
         
         final InferredOWLOntologyID inferredOntologyID =
                 new InferredOWLOntologyID(loadedOntology.getOntologyID().getOntologyIRI(), loadedOntology
                         .getOntologyID().getVersionIRI(), null);
         
         this.testOWLManager.removeCache(inferredOntologyID.getBaseOWLOntologyID());
         
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(inferredOntologyID.getBaseOWLOntologyID()));
         
         // invoke method to test
         this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
         
         // verify:
         Assert.assertTrue("Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(inferredOntologyID.getBaseOWLOntologyID()));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      * Tests the following hierarchy of imports when caching PoddPlant schema ontology.
      * 
      * PoddPlant :imports PoddScience :imports PoddBase PoddScience :imports PoddBase
      */
     @Test
     public void testCacheSchemaOntologyWithTwoLevelImports() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: 1) load, infer, store PODD-Base ontology
         final InferredOWLOntologyID pbInferredOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_BASE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
         final URI pbBaseOntologyURI = pbInferredOntologyID.getOntologyIRI().toOpenRDFURI();
         final URI pbVersionURI = pbInferredOntologyID.getVersionIRI().toOpenRDFURI();
         
         // prepare: 2) load, infer, store PODD-Science ontology
         final InferredOWLOntologyID pScienceInferredOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_SCIENCE, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED);
         final URI pScienceBaseOntologyURI = pScienceInferredOntologyID.getOntologyIRI().toOpenRDFURI();
         final URI pScienceVersionURI = pScienceInferredOntologyID.getVersionIRI().toOpenRDFURI();
         
         // prepare: 3) load, infer, store PODD-Plant ontology
         final InferredOWLOntologyID pPlantInferredOntologyID =
                 this.loadInferStoreOntology(PoddRdfConstants.PATH_PODD_PLANT, RDFFormat.RDFXML,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_CONCRETE,
                         TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_INFERRED);
         final URI pPlantBaseOntologyURI = pPlantInferredOntologyID.getOntologyIRI().toOpenRDFURI();
         final URI pPlantVersionURI = pPlantInferredOntologyID.getVersionIRI().toOpenRDFURI();
         
         // prepare: 4) remove ontologies from manager cache
         this.testOWLManager.removeCache(pbInferredOntologyID.getBaseOWLOntologyID());
         this.testOWLManager.removeCache(pbInferredOntologyID.getInferredOWLOntologyID());
         this.testOWLManager.removeCache(pScienceInferredOntologyID.getBaseOWLOntologyID());
         this.testOWLManager.removeCache(pScienceInferredOntologyID.getInferredOWLOntologyID());
         this.testOWLManager.removeCache(pPlantInferredOntologyID.getBaseOWLOntologyID());
         this.testOWLManager.removeCache(pPlantInferredOntologyID.getInferredOWLOntologyID());
         
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pbInferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pbInferredOntologyID.getInferredOntologyIRI()));
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pScienceInferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertFalse(
                 "Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(
                         pScienceInferredOntologyID.getInferredOntologyIRI()));
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pPlantInferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertFalse("Ontology should not be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pPlantInferredOntologyID.getInferredOntologyIRI()));
         
         // prepare: 4) create schema management graph
         final URI schemaGraph = PoddRdfConstants.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
         
         // Podd-Base
         this.testRepositoryConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pbVersionURI,
                 schemaGraph);
         this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pbInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
         
         // Podd-Science
         this.testRepositoryConnection.add(pScienceBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
         this.testRepositoryConnection.add(pScienceBaseOntologyURI, PoddRdfConstants.OWL_VERSION_IRI,
                 pScienceVersionURI, schemaGraph);
         this.testRepositoryConnection.add(pScienceBaseOntologyURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
         this.testRepositoryConnection.add(pScienceBaseOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pScienceInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
         
         // Podd-Plant
         this.testRepositoryConnection.add(pPlantBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
         this.testRepositoryConnection.add(pPlantBaseOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pPlantVersionURI,
                 schemaGraph);
         this.testRepositoryConnection.add(pPlantBaseOntologyURI, OWL.IMPORTS, pScienceVersionURI, schemaGraph);
         this.testRepositoryConnection.add(pPlantBaseOntologyURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
         this.testRepositoryConnection.add(pPlantBaseOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                 pPlantInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
         
         // invoke method to test
         this.testOWLManager.cacheSchemaOntology(pPlantInferredOntologyID, this.testRepositoryConnection, schemaGraph);
         
         // verify:
         Assert.assertTrue("Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pScienceInferredOntologyID.getBaseOWLOntologyID()));
         Assert.assertTrue(
                 "Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(
                         pScienceInferredOntologyID.getInferredOWLOntologyID()));
         Assert.assertTrue("Ontology should be in memory",
                 this.testOWLManager.getOWLOntologyManager().contains(pbInferredOntologyID.getBaseOWLOntologyID()));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
      * .
      * 
      */
     @Test
     public void testCreateReasoner() throws Exception
     {
         // prepare: load an Ontology independently
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_DCTERMS);
         Assert.assertNotNull("Could not find resource", inputStream);
         final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         final OWLOntology loadedOntology = testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
         
         final OWLReasoner reasoner = this.testOWLManager.createReasoner(loadedOntology);
         
         // verify:
         Assert.assertNotNull("Created reasoner was NULL", reasoner);
         Assert.assertEquals(this.testOWLManager.getReasonerFactory().getReasonerName(), reasoner.getReasonerName());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
      * .
      * 
      */
     @Test
     public void testCreateReasonerFromEmptyOntology() throws Exception
     {
         // prepare: load an Ontology independently
         final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         final OWLOntology emptyOntology = testOWLOntologyManager.createOntology();
         
         final OWLReasoner reasoner = this.testOWLManager.createReasoner(emptyOntology);
         
         // verify:
         Assert.assertNotNull("Created reasoner was NULL", reasoner);
         Assert.assertEquals(this.testOWLManager.getReasonerFactory().getReasonerName(), reasoner.getReasonerName());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
      * .
      * 
      */
     @Test
     public void testCreateReasonerWithNull() throws Exception
     {
         try
         {
             this.testOWLManager.createReasoner(null);
             Assert.fail("Should have thrown a Runtime Exception");
         }
         catch(final RuntimeException e)
         {
             Assert.assertTrue("Exception not expected type", e instanceof NullPointerException);
             // this exception is thrown by the OWL API with a null message
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#dumpOntologyToRepository(OWLOntology, RepositoryConnection, URI...)}
      * .
      * 
      */
     @Test
     public void testDumpOntologyToRepository() throws Exception
     {
         // prepare: load, infer and store PODD:dcTerms, foaf and User ontologies to testOWLManager
         final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                 PoddRdfConstants.PATH_PODD_DCTERMS));
         testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                 PoddRdfConstants.PATH_PODD_FOAF));
         testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                 PoddRdfConstants.PATH_PODD_USER));
         
         // prepare: load Podd-Base Ontology independently
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         final OWLOntology nextOntology = testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
         
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:test:dump:context:");
         
         this.testOWLManager.dumpOntologyToRepository(nextOntology, this.testRepositoryConnection, context);
         
         // verify:
         Assert.assertEquals("Dumped statement count not expected value",
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE, this.testRepositoryConnection.size(context));
         
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#dumpOntologyToRepository(OWLOntology, RepositoryConnection, URI...)}
      * .
      * 
      */
     @Test
     public void testDumpOntologyToRepositoryWithEmptyOntology() throws Exception
     {
         // prepare: load an Ontology independently
         final OWLOntology nextOntology = this.testOWLManager.getOWLOntologyManager().createOntology();
         
         try
         {
             this.testOWLManager.dumpOntologyToRepository(nextOntology, this.testRepositoryConnection);
             Assert.fail("Should have thrown an IllegalArgumentException");
         }
         catch(final IllegalArgumentException e)
         {
             Assert.assertEquals("Cannot dump anonymous ontologies to repository", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#dumpOntologyToRepository(OWLOntology, RepositoryConnection, URI...)}
      * .
      * 
      */
     @Test
     public void testDumpOntologyToRepositoryWithoutContext() throws Exception
     {
         // prepare: load, infer and store PODD:dcTerms, foaf and User ontologies to testOWLManager
         final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                 PoddRdfConstants.PATH_PODD_DCTERMS));
         testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                 PoddRdfConstants.PATH_PODD_FOAF));
         testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                 PoddRdfConstants.PATH_PODD_USER));
         
         // prepare: load an Ontology independently
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         final OWLOntology nextOntology = testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
         
         this.testOWLManager.dumpOntologyToRepository(nextOntology, this.testRepositoryConnection);
         
         // verify:
         final URI context = nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI();
         Assert.assertEquals("Dumped statement count not expected value",
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE, this.testRepositoryConnection.size(context));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testGenerateInferredOntologyID() throws Exception
     {
         // prepare: create an OntologyID
         final OWLOntologyID ontologyID =
                 new OWLOntologyID(IRI.create("http://purl.org/podd/ns/poddBase"),
                         IRI.create("http://purl.org/podd/ns/version/poddBase/1"));
         
         final InferredOWLOntologyID inferredOntologyID = this.testOWLManager.generateInferredOntologyID(ontologyID);
         
         // verify:
         Assert.assertNotNull("InferredOntologyID was null", inferredOntologyID);
         Assert.assertNotNull("Inferred Ontology IRI was null", inferredOntologyID.getInferredOntologyIRI());
         Assert.assertEquals("Inferred IRI was not as expected",
                 IRI.create(PoddRdfConstants.INFERRED_PREFIX + "http://purl.org/podd/ns/version/poddBase/1"),
                 inferredOntologyID.getInferredOntologyIRI());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testGenerateInferredOntologyIDWithEmptyOntologyID() throws Exception
     {
         // prepare: create an OntologyID
         final OWLOntologyID ontologyID = new OWLOntologyID();
         
         try
         {
             this.testOWLManager.generateInferredOntologyID(ontologyID);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testGenerateInferredOntologyIDWithNullOntologyID() throws Exception
     {
         try
         {
             this.testOWLManager.generateInferredOntologyID(null);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#getCurrentVersion(org.semanticweb.owlapi.model.IRI)}
      * .
      * 
      */
     @Ignore
     @Test
     public void testGetCurrentVersion() throws Exception
     {
         Assert.fail("TODO: Implement me");
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#getOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Ignore
     @Test
     public void testGetOntology() throws Exception
     {
         Assert.fail("TODO: Implement me");
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddOWLManager#getOWLOntologyManager()} .
      * 
      */
     @Test
     public void testGetOWLOntologyManagerWithMockObject() throws Exception
     {
         this.testOWLManager.setOWLOntologyManager(null);
         Assert.assertNull("OWLOntologyManager should have been null", this.testOWLManager.getOWLOntologyManager());
         
         final OWLOntologyManager mockOWLOntologyManager = Mockito.mock(OWLOntologyManager.class);
         this.testOWLManager.setOWLOntologyManager(mockOWLOntologyManager);
         
         Assert.assertNotNull("OWLOntologyManager was not set", this.testOWLManager.getOWLOntologyManager());
         Assert.assertEquals("Not the expected mock OWLManager", mockOWLOntologyManager,
                 this.testOWLManager.getOWLOntologyManager());
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddOWLManager#getReasonerFactory()} .
      * 
      */
     @Test
     public void testGetReasonerFactoryWithMockObject() throws Exception
     {
         this.testOWLManager.setReasonerFactory(null);
         Assert.assertNull("ReasonerFactory should have been null", this.testOWLManager.getReasonerFactory());
         
         final OWLReasonerFactory mockReasonerFactory = Mockito.mock(OWLReasonerFactory.class);
         
         this.testOWLManager.setReasonerFactory(mockReasonerFactory);
         
         Assert.assertNotNull("The reasoner factory was not set", this.testOWLManager.getReasonerFactory());
         Assert.assertEquals("Not the expected mock ReasonerFactory", mockReasonerFactory,
                 this.testOWLManager.getReasonerFactory());
     }
     
     /**
      * Test method for {@link com.github.podd.api.PoddOWLManager#getReasonerProfile()} .
      * 
      */
     @Test
     public void testGetReasonerProfile() throws Exception
     {
         final OWLProfile profile = this.testOWLManager.getReasonerProfile();
         Assert.assertNotNull("OWLProfile was null", profile);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#getVersions(org.semanticweb.owlapi.model.IRI)} .
      * 
      * @throws Exception
      */
     @Ignore
     @Test
     public void testGetVersion() throws Exception
     {
         Assert.fail("TODO: Implement me");
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#inferStatements(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testInferStatements() throws Exception
     {
         // prepare: load an ontology into a StreamDocumentSource
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_DCTERMS);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         RDFFormat.RDFXML.getDefaultMIMEType()));
         
         final OWLOntology loadedOntology = this.testOWLManager.loadOntology(owlSource);
         Assert.assertEquals("Nothing should be in the Repository at this stage", 0,
                 this.testRepositoryConnection.size());
         
         this.testOWLManager.dumpOntologyToRepository(loadedOntology, this.testRepositoryConnection);
         
         final InferredOWLOntologyID inferredOntologyID =
                 this.testOWLManager.inferStatements(loadedOntology, this.testRepositoryConnection);
         
         // verify:
         Assert.assertNotNull("Inferred Ontology ID was null", inferredOntologyID);
         Assert.assertNotNull("Inferred Ontology Version IRI was null", inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Incorrect no. of inferred statements",
                 TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED,
                 this.testRepositoryConnection.size(inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI()));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#inferStatements(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testInferStatementsTwiceForSameOntology() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         final long repoSizeAfterPreparation = this.testRepositoryConnection.size();
         
         // prepare: load an ontology into a StreamDocumentSource
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         RDFFormat.RDFXML.getDefaultMIMEType()));
         
         final OWLOntology loadedOntology = this.testOWLManager.loadOntology(owlSource);
         Assert.assertEquals("Repository should not have changed at this stage", repoSizeAfterPreparation,
                 this.testRepositoryConnection.size());
         
         this.testOWLManager.dumpOntologyToRepository(loadedOntology, this.testRepositoryConnection);
         
         final InferredOWLOntologyID inferredOntologyID =
                 this.testOWLManager.inferStatements(loadedOntology, this.testRepositoryConnection);
         
         Assert.assertNotNull("Inferred Ontology ID was null", inferredOntologyID);
         Assert.assertNotNull("Inferred Ontology Version IRI was null", inferredOntologyID.getVersionIRI());
         Assert.assertEquals("Incorrect no. of inferred statements",
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED,
                 this.testRepositoryConnection.size(inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI()));
         
         // try to infer same ontology again
         try
         {
             this.testOWLManager.inferStatements(loadedOntology, this.testRepositoryConnection);
             Assert.fail("Should have thrown an OWLOntologyAlreadyExistsException");
         }
         catch(final OWLOntologyAlreadyExistsException e)
         {
             Assert.assertTrue(e.getMessage().contains("Ontology already exists"));
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#inferStatements(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
      * .
      * 
      */
     @Test
     public void testInferStatementsWithNullOntology() throws Exception
     {
         try
         {
             this.testOWLManager.inferStatements(null, this.testRepositoryConnection);
             Assert.fail("Should have thrown a NullPointerException");
         }
         catch(final NullPointerException e)
         {
             Assert.assertNull("Not the expected Exception", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
      * . Attempts to load an RDF resource which does not contain an ontology.
      */
     @Test
     public void testLoadOntologyFromEmptyOWLOntologyDocumentSource() throws Exception
     {
         // prepare: load an ontology into a StreamDocumentSource
         final InputStream inputStream = this.getClass().getResourceAsStream("/test/ontologies/empty.owl");
         Assert.assertNotNull("Could not find resource", inputStream);
         
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         RDFFormat.RDFXML.getDefaultMIMEType()));
         
         try
         {
             this.testOWLManager.loadOntology(owlSource);
             Assert.fail("Should have thrown an OWLOntologyCreationException");
         }
         catch(final EmptyOntologyException e)
         {
             Assert.assertEquals("Unexpected message in expected Exception", "Loaded ontology is empty", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
      * .
      * 
      */
     @Test
     public void testLoadOntologyFromOWLOntologyDocumentSource() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: load an ontology into a StreamDocumentSource
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         RDFFormat.RDFXML.getDefaultMIMEType()));
         
         final OWLOntology loadedOntology = this.testOWLManager.loadOntology(owlSource);
         
         // verify:
         Assert.assertNotNull(loadedOntology);
         Assert.assertEquals("<http://purl.org/podd/ns/poddBase>", loadedOntology.getOntologyID().getOntologyIRI()
                 .toQuotedString());
         Assert.assertEquals("<http://purl.org/podd/ns/version/poddBase/1>", loadedOntology.getOntologyID()
                 .getVersionIRI().toQuotedString());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
      * .
      * 
      */
     @Test
     public void testLoadOntologyFromRioMemoryTripleSource() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: load an ontology into a RioMemoryTripleSource via the test repository
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:context:test");
         
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
         final Model statements = new LinkedHashModel();
         this.testRepositoryConnection.export(new StatementCollector(statements), context);
         Assert.assertEquals("Not the expected number of statements in Repository",
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE - 9, statements.size());
         
         final RioMemoryTripleSource owlSource = new RioMemoryTripleSource(statements.iterator());
         
         final OWLOntology loadedOntology = this.testOWLManager.loadOntology(owlSource);
         
         // verify:
         Assert.assertNotNull(loadedOntology);
         Assert.assertEquals("<http://purl.org/podd/ns/poddBase>", loadedOntology.getOntologyID().getOntologyIRI()
                 .toQuotedString());
         Assert.assertEquals("<http://purl.org/podd/ns/version/poddBase/1>", loadedOntology.getOntologyID()
                 .getVersionIRI().toQuotedString());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
      * . Attempts to load a non-RDF resource.
      */
     @Test
     public void testLoadOntologyFromTextDocumentSource() throws Exception
     {
         // prepare: load an ontology into a StreamDocumentSource
         final InputStream inputStream = this.getClass().getResourceAsStream("/test/ontologies/justatextfile.owl");
         Assert.assertNotNull("Could not find resource", inputStream);
         
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         RDFFormat.RDFXML.getDefaultMIMEType()));
         
         try
         {
             this.testOWLManager.loadOntology(owlSource);
             Assert.fail("Should have thrown an OWLOntologyCreationException");
         }
         catch(final OWLOntologyCreationException e)
         {
             Assert.assertTrue("Exception not expected type", e instanceof UnparsableOntologyException);
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
      * . Attempts to pass NULL value into loadOntlogy().
      */
     @Test
     public void testLoadOntologyWithNull() throws Exception
     {
         try
         {
             this.testOWLManager.loadOntology(null);
             Assert.fail("Should have thrown a RuntimeException");
         }
         catch(final RuntimeException e)
         {
             Assert.assertTrue("Exception not expected type", e instanceof NullPointerException);
             // this exception is thrown by the OWL API with a null message
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#parseRDFStatements(org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)}
      * .
      * 
      */
     @Test
     public void testParseRDFStatements() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: load poddBase schema ontology into the test repository
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         
         final URI contextOriginal = ValueFactoryImpl.getInstance().createURI("urn:test:context:original:");
         
         Model statementsOriginal = new TreeModel(Rio.parse(inputStream, "", RDFFormat.RDFXML, contextOriginal));
         
         Assert.assertEquals("Not the expected number of statements in Repository",
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE - 9, statementsOriginal.size());
         this.testRepositoryConnection.add(statementsOriginal);
         Assert.assertEquals("Not the expected number of statements in Repository",
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE - 9,
                 this.testRepositoryConnection.size(contextOriginal));
         
         final OWLOntologyID loadedOntologyID =
                 this.testOWLManager.parseRDFStatements(this.testRepositoryConnection, contextOriginal);
         
         // verify:
         Assert.assertNotNull("OntologyID was null", loadedOntologyID);
         Assert.assertEquals("<http://purl.org/podd/ns/poddBase>", loadedOntologyID.getOntologyIRI().toQuotedString());
         Assert.assertEquals("<http://purl.org/podd/ns/version/poddBase/1>", loadedOntologyID.getVersionIRI()
                 .toQuotedString());
         
         final OWLOntology loadedOntology = this.testOWLManager.getOntology(loadedOntologyID);
         Assert.assertNotNull("Ontology not in memory", loadedOntology);
         
         final URI contextOwlapi = ValueFactoryImpl.getInstance().createURI("urn:test:context:owlapi:");
         this.testOWLManager.dumpOntologyToRepository(loadedOntology, this.testRepositoryConnection, contextOwlapi);
         
         // verify:
         Assert.assertEquals("Dumped statement count not expected value",
                 TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                 this.testRepositoryConnection.size(contextOwlapi));
         
         Model statementsOwlapi = new TreeModel();
         
         this.testRepositoryConnection.export(new StatementCollector(statementsOwlapi), contextOwlapi);
         
         System.out.println("------------");
         System.out.println("RDF statements");
         System.out.println("------------");
         
         StringWriter originalWriter = new StringWriter();
         Rio.write(statementsOriginal, originalWriter, RDFFormat.NTRIPLES);
         
         System.out.println("------------");
         System.out.println("OWLAPI statements");
         System.out.println("------------");
         
         StringWriter owlapiWriter = new StringWriter();
         Rio.write(statementsOwlapi, owlapiWriter, RDFFormat.NTRIPLES);
         
         System.out.println("------------");
         System.out.println("Mismatched statements");
         System.out.println("------------");
         
         Set<URI> displayedPredicates = new HashSet<URI>();
         
         for(Statement nextOwlapiStatement : statementsOwlapi)
         {
             if(!(nextOwlapiStatement.getSubject() instanceof BNode)
                     && !(nextOwlapiStatement.getObject() instanceof BNode))
             {
                 if(!statementsOriginal.contains(nextOwlapiStatement.getSubject(), nextOwlapiStatement.getPredicate(),
                         nextOwlapiStatement.getObject()))
                 {
                     System.out.println(nextOwlapiStatement);
                 }
             }
             else
             {
                 Model originalFilter = statementsOriginal.filter(null, nextOwlapiStatement.getPredicate(), null);
                 Model owlapiFilter = statementsOwlapi.filter(null, nextOwlapiStatement.getPredicate(), null);
                 
                 if(originalFilter.size() != owlapiFilter.size())
                 {
                     if(!displayedPredicates.contains(nextOwlapiStatement.getPredicate()))
                     {
                         displayedPredicates.add(nextOwlapiStatement.getPredicate());
                         System.out.println("Original statements for predicate");
                         DebugUtils.printContents(originalFilter);
                         System.out.println("OWLAPI statements for predicate");
                         DebugUtils.printContents(owlapiFilter);
                     }
                 }
             }
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#parseRDFStatements(org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)}
      * .
      * 
      */
     @Test
     public void testParseRDFStatementsFromEmptyRepository() throws Exception
     {
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:test:context:");
         try
         {
             this.testOWLManager.parseRDFStatements(this.testRepositoryConnection, context);
         }
         catch(final EmptyOntologyException e)
         {
             Assert.assertEquals("No statements to create an ontology", e.getMessage());
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#parseRDFStatements(org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)}
      * . This test asserts that when the RepositoryConnection has just one statement in it, a
      * non-empty and anonymous Ontology is loaded to the memory.
      */
     @Test
     public void testParseRDFStatementsFromRepositoryWithOneStatement() throws Exception
     {
         // prepare: add a single statement to the Repository so that it is not empty
         final URI context = ValueFactoryImpl.getInstance().createURI("urn:test:context:");
         
         this.testRepositoryConnection.add(ValueFactoryImpl.getInstance().createURI("urn:dummy:subject"), RDF.TYPE,
                 ValueFactoryImpl.getInstance().createURI("urn:dummy:object"), context);
         
         final OWLOntologyID loadedOntologyID =
                 this.testOWLManager.parseRDFStatements(this.testRepositoryConnection, context);
         
         // verify:
         Assert.assertNotNull("OntologyID was null", loadedOntologyID);
         
         Assert.assertNull("Was not an anonymous ontology", loadedOntologyID.getOntologyIRI());
         Assert.assertNull("Was not an anonymous ontology", loadedOntologyID.getVersionIRI());
         
         final OWLOntology loadedOntology = this.testOWLManager.getOntology(loadedOntologyID);
         Assert.assertNotNull("Ontology not in memory", loadedOntology);
         Assert.assertFalse("Ontology is empty", loadedOntology.isEmpty());
         Assert.assertEquals("Not the expected number of axioms", 1, loadedOntology.getAxiomCount());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#removeCache(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testRemoveCache() throws Exception
     {
         this.loadDcFoafAndPoddUserSchemaOntologies();
         
         // prepare: load an ontology into the OWLManager
         final InputStream inputStream = this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE);
         Assert.assertNotNull("Could not find resource", inputStream);
         final OWLOntologyDocumentSource owlSource =
                 new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                         RDFFormat.RDFXML.getDefaultMIMEType()));
         final OWLOntology loadedOntology = this.testOWLManager.loadOntology(owlSource);
         
         final OWLOntologyID ontologyID = loadedOntology.getOntologyID();
         final OWLOntology ontologyLoadedFromMemory = this.testOWLManager.getOntology(ontologyID);
         Assert.assertNotNull("Ontology should be in memory", ontologyLoadedFromMemory);
         
         final boolean removed = this.testOWLManager.removeCache(ontologyID);
         
         // verify:
         Assert.assertTrue("Ontology could not be removed from cache", removed);
         
         final OWLOntology ontologyFromMemoryShouldBeNull = this.testOWLManager.getOntology(ontologyID);
         Assert.assertNull("Ontology is still in cache", ontologyFromMemoryShouldBeNull);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#removeCache(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testRemoveCacheWithEmptyOntology() throws Exception
     {
         // prepare: create an empty ontology inside this OWLManager
         final OWLOntologyID ontologyID = this.testOWLManager.getOWLOntologyManager().createOntology().getOntologyID();
         final OWLOntology theOntologyFromMemory = this.testOWLManager.getOntology(ontologyID);
         Assert.assertNotNull("The ontology was not in memory", theOntologyFromMemory);
         Assert.assertTrue("Ontology was not empty", theOntologyFromMemory.isEmpty());
         
         final boolean removed = this.testOWLManager.removeCache(ontologyID);
         
         // verify:
         Assert.assertTrue("Ontology could not be removed from cache", removed);
         
         final OWLOntology ontologyFromMemoryShouldBeNull = this.testOWLManager.getOntology(ontologyID);
         Assert.assertNull("Ontology is still in cache", ontologyFromMemoryShouldBeNull);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#removeCache(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testRemoveCacheWithNullOntology() throws Exception
     {
         try
         {
             this.testOWLManager.removeCache(null);
             Assert.fail("Should have thrown a RuntimeException");
         }
         catch(final RuntimeException e)
         {
             Assert.assertTrue("Not the expected type of Exception", e instanceof NullPointerException);
             // this exception is thrown by the OWL API with a null message
         }
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#removeCache(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Test
     public void testRemoveCacheWithOntologyNotInMemory() throws Exception
     {
         // prepare: create an ontology externally
         final OWLOntology ontologyLoadedFromMemory =
                 OWLOntologyManagerFactoryRegistry.createOWLOntologyManager().createOntology();
         Assert.assertNotNull("Ontology should not be in memory", ontologyLoadedFromMemory);
         
         final OWLOntologyID ontologyID = ontologyLoadedFromMemory.getOntologyID();
         final boolean removed = this.testOWLManager.removeCache(ontologyID);
         
         // verify:
         Assert.assertFalse("Ontology should not have existed in memory/cache", removed);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#setCurrentVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      */
     @Ignore
     @Test
     public void testSetCurrentVersion() throws Exception
     {
         // omv.ontoware/currentversion
         Assert.fail("TODO: Implement me");
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#setOWLOntologyManager(org.semanticweb.owlapi.model.OWLOntologyManager)}
      * .
      * 
      */
     @Test
     public void testSetOWLOntologyManager() throws Exception
     {
         // set null to forget the manager being set in setUp()
         this.testOWLManager.setOWLOntologyManager(null);
         Assert.assertNull("OWLOntologyManager could not be set to NULL", this.testOWLManager.getOWLOntologyManager());
         
         final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
         Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
         
         this.testOWLManager.setOWLOntologyManager(manager);
         
         Assert.assertNotNull("OWLOntologyManager was not set", this.testOWLManager.getOWLOntologyManager());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#setOWLOntologyManager(org.semanticweb.owlapi.model.OWLOntologyManager)}
      * .
      * 
      */
     @Test
     public void testSetOWLOntologyManagerWithNull() throws Exception
     {
         this.testOWLManager.setOWLOntologyManager(null);
         Assert.assertNull("OWLOntologyManager could not be set to NULL", this.testOWLManager.getOWLOntologyManager());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#setPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
      * .
      * 
      * @throws Exception
      */
     @Ignore
     @Test
     public void testSetPublished() throws Exception
     {
         Assert.fail("TODO: Implement me");
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#setReasonerFactory(org.semanticweb.owlapi.reasoner.OWLReasonerFactory)}
      * .
      * 
      */
     @Test
     public void testSetReasonerFactory() throws Exception
     {
         // set null to forget the reasoner factory being set in setUp()
         this.testOWLManager.setReasonerFactory(null);
         Assert.assertNull("The reasoner factory could not be set to NULL", this.testOWLManager.getReasonerFactory());
         
         final OWLReasonerFactory reasonerFactory = this.getNewOWLReasonerFactoryInstance();
         Assert.assertNotNull("Null implementation of reasoner factory", reasonerFactory);
         
         this.testOWLManager.setReasonerFactory(reasonerFactory);
         
         Assert.assertNotNull("The reasoner factory was not set", this.testOWLManager.getReasonerFactory());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.api.PoddOWLManager#setReasonerFactory(org.semanticweb.owlapi.reasoner.OWLReasonerFactory)}
      * .
      * 
      */
     @Test
     public void testSetReasonerFactoryWithNull() throws Exception
     {
         this.testOWLManager.setReasonerFactory(null);
         Assert.assertNull("The reasoner factory could not be set to NULL", this.testOWLManager.getReasonerFactory());
     }
 }
