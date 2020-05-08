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
 /**
  * 
  */
 package com.github.podd.utils.test;
 
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFHandler;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.helpers.StatementCollector;
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLOntologyID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.podd.exception.SchemaManifestException;
 import com.github.podd.utils.DebugUtils;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.OntologyUtils;
 import com.github.podd.utils.PODD;
 
 /**
  * Test for OntologyUtils class that translates between RDF and InferredOWLOntologyID instances.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  */
 public class OntologyUtilsTest
 {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     private URI testOntologyUri1;
     private URI testVersionUri1;
     private URI testInferredUri1;
     private URI testImportOntologyUri1;
     private URI testImportVersionUri1;
     private URI testImportOntologyUri2;
     private URI testImportVersionUri2;
     private URI testImportOntologyUri3;
     private URI testImportVersionUri3;
     private URI testImportOntologyUri4;
     private URI testImportVersionUri4;
     private ValueFactory vf;
     private InferredOWLOntologyID testOntologyID;
     private InferredOWLOntologyID testImportOntologyID1;
     private InferredOWLOntologyID testImportOntologyID2;
     private InferredOWLOntologyID testImportOntologyID3;
     private InferredOWLOntologyID testImportOntologyID4;
     
     @Before
     public void setUp() throws Exception
     {
         this.vf = PODD.VF;
         this.testOntologyUri1 = this.vf.createURI("urn:test:ontology:uri:1");
         this.testVersionUri1 = this.vf.createURI("urn:test:ontology:uri:1:version:1");
         this.testInferredUri1 = this.vf.createURI("urn:inferred:test:ontology:uri:1:version:1");
         this.testOntologyID = new InferredOWLOntologyID(testOntologyUri1, testVersionUri1, testInferredUri1);
         
         this.testImportOntologyUri1 = this.vf.createURI("urn:test:import:ontology:uri:1");
         this.testImportVersionUri1 = this.vf.createURI("urn:test:import:ontology:uri:1:version:1");
         this.testImportOntologyID1 = new InferredOWLOntologyID(testImportOntologyUri1, testImportVersionUri1, null);
         
         this.testImportOntologyUri2 = this.vf.createURI("urn:test:import:ontology:uri:2");
         this.testImportVersionUri2 = this.vf.createURI("urn:test:import:ontology:uri:2:version:1");
         this.testImportOntologyID2 = new InferredOWLOntologyID(testImportOntologyUri2, testImportVersionUri2, null);
         
         this.testImportOntologyUri3 = this.vf.createURI("urn:test:import:ontology:uri:3");
         this.testImportVersionUri3 = this.vf.createURI("urn:test:import:ontology:uri:3:version:1");
         this.testImportOntologyID3 = new InferredOWLOntologyID(testImportOntologyUri3, testImportVersionUri3, null);
         
         this.testImportOntologyUri4 = this.vf.createURI("urn:test:import:ontology:uri:4");
         this.testImportVersionUri4 = this.vf.createURI("urn:test:import:ontology:uri:4:version:1");
         this.testImportOntologyID4 = new InferredOWLOntologyID(testImportOntologyUri4, testImportVersionUri4, null);
     }
     
     @After
     public void tearDown() throws Exception
     {
         this.vf = null;
         this.testOntologyUri1 = null;
         this.testVersionUri1 = null;
         this.testInferredUri1 = null;
         this.testImportOntologyUri1 = null;
         this.testImportVersionUri1 = null;
         this.testOntologyID = null;
         this.testImportOntologyID1 = null;
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#modelToOntologyIDs(org.openrdf.model.Model)}.
      */
     @Test
     public final void testModelToOntologyIDsEmpty()
     {
         final Model input = new LinkedHashModel();
         final Collection<InferredOWLOntologyID> modelToOntologyIDs = OntologyUtils.modelToOntologyIDs(input);
         
         // No statements, should return an empty collection
         Assert.assertEquals(0, modelToOntologyIDs.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#modelToOntologyIDs(org.openrdf.model.Model)}.
      */
     @Test
     public final void testModelToOntologyIDsNoVersion()
     {
         final Model input = new LinkedHashModel();
         input.add(this.vf.createStatement(this.testOntologyUri1, RDF.TYPE, OWL.ONTOLOGY));
         final Collection<InferredOWLOntologyID> modelToOntologyIDs = OntologyUtils.modelToOntologyIDs(input);
         
         // Must have a version to be returned
         Assert.assertEquals(0, modelToOntologyIDs.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#modelToOntologyIDs(org.openrdf.model.Model)}.
      */
     @Test
     public final void testModelToOntologyIDsOneVersion()
     {
         final Model input = new LinkedHashModel();
         input.add(this.vf.createStatement(this.testOntologyUri1, RDF.TYPE, OWL.ONTOLOGY));
         input.add(this.vf.createStatement(this.testVersionUri1, RDF.TYPE, OWL.ONTOLOGY));
         input.add(this.vf.createStatement(this.testOntologyUri1, OWL.VERSIONIRI, this.testVersionUri1));
         final Collection<InferredOWLOntologyID> modelToOntologyIDs = OntologyUtils.modelToOntologyIDs(input);
         
         // 1 ontology returned
         Assert.assertEquals(1, modelToOntologyIDs.size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
      * .
      * 
      * @throws Exception
      */
     @Test
     public final void testOntologyIDsToHandlerAnonymousOntology() throws Exception
     {
         final Model input = new LinkedHashModel();
         
         OntologyUtils.ontologyIDsToHandler(Arrays.asList(new InferredOWLOntologyID((IRI)null, null, null)),
                 new StatementCollector(input));
         
         Assert.assertTrue(input.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
      * .
      * 
      * @throws Exception
      */
     @Test
     public final void testOntologyIDsToHandlerEmptyNotNull() throws Exception
     {
         final Model input = new LinkedHashModel();
         
         OntologyUtils.ontologyIDsToHandler(Collections.<InferredOWLOntologyID> emptyList(), new StatementCollector(
                 input));
         
         Assert.assertTrue(input.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
      * .
      * 
      * @throws Exception
      */
     @Test
     public final void testOntologyIDsToHandlerEmptyNull() throws Exception
     {
         OntologyUtils.ontologyIDsToHandler(Collections.<InferredOWLOntologyID> emptyList(), (RDFHandler)null);
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
      * .
      * 
      * @throws Exception
      */
     @Test
     public final void testOntologyIDsToHandlerNoInferredIRI() throws Exception
     {
         final Model input = new LinkedHashModel();
         
         OntologyUtils.ontologyIDsToHandler(Arrays.asList(new InferredOWLOntologyID(IRI
                 .create("urn:test:ontology:iri:abc"), IRI.create("urn:test:ontology:iri:abc:version:1"), null)),
                 new StatementCollector(input));
         
         Assert.assertEquals(3, input.size());
         Assert.assertTrue(input.contains(null, RDF.TYPE, OWL.ONTOLOGY));
         Assert.assertTrue(input.contains(null, OWL.VERSIONIRI, null));
         Assert.assertEquals(2, input.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
      * .
      * 
      * @throws Exception
      */
     @Test
     public final void testOntologyIDsToHandlerNoVersionIRI() throws Exception
     {
         final Model input = new LinkedHashModel();
         
         OntologyUtils.ontologyIDsToHandler(
                 Arrays.asList(new InferredOWLOntologyID(IRI.create("urn:test:ontology:iri:abc"), null, null)),
                 new StatementCollector(input));
         
         Assert.assertEquals(1, input.size());
         Assert.assertTrue(input.contains(null, RDF.TYPE, OWL.ONTOLOGY));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToHandler(java.util.Collection, org.openrdf.rio.RDFHandler)}
      * .
      * 
      * @throws Exception
      */
     @Test
     public final void testOntologyIDsToHandlerWithInferredIRI() throws Exception
     {
         final Model input = new LinkedHashModel();
         
         OntologyUtils.ontologyIDsToHandler(Arrays.asList(new InferredOWLOntologyID(IRI
                 .create("urn:test:ontology:iri:abc"), IRI.create("urn:test:ontology:iri:abc:version:1"), IRI
                 .create("urn:inferred:test:ontology:iri:abc:version:1:1"))), new StatementCollector(input));
         
         Assert.assertEquals(5, input.size());
         Assert.assertTrue(input.contains(null, RDF.TYPE, OWL.ONTOLOGY));
         Assert.assertTrue(input.contains(null, OWL.VERSIONIRI, null));
         Assert.assertTrue(input.contains(null, PODD.PODD_BASE_INFERRED_VERSION, null));
         Assert.assertEquals(3, input.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
      * .
      */
     @Test
     public final void testOntologyIDsToModelAnonymousOntology() throws Exception
     {
         final Model input = new LinkedHashModel();
         
         final Model ontologyIDsToModel =
                 OntologyUtils
                         .ontologyIDsToModel(Arrays.asList(new InferredOWLOntologyID((IRI)null, null, null)), input);
         
         Assert.assertNotNull(ontologyIDsToModel);
         Assert.assertEquals(input, ontologyIDsToModel);
         Assert.assertTrue(ontologyIDsToModel.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
      * .
      */
     @Test
     public final void testOntologyIDsToModelEmptyNotNull()
     {
         final Model input = new LinkedHashModel();
         
         final Model ontologyIDsToModel =
                 OntologyUtils.ontologyIDsToModel(Collections.<InferredOWLOntologyID> emptyList(), input);
         
         Assert.assertNotNull(ontologyIDsToModel);
         Assert.assertEquals(input, ontologyIDsToModel);
         Assert.assertTrue(ontologyIDsToModel.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
      * .
      */
     @Test
     public final void testOntologyIDsToModelEmptyNull()
     {
         final Model ontologyIDsToModel =
                 OntologyUtils.ontologyIDsToModel(Collections.<InferredOWLOntologyID> emptyList(), (Model)null);
         
         Assert.assertNotNull(ontologyIDsToModel);
         Assert.assertTrue(ontologyIDsToModel.isEmpty());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
      * .
      */
     @Test
     public final void testOntologyIDsToModelNoInferredIRI()
     {
         final Model input = new LinkedHashModel();
         
         final Model ontologyIDsToModel =
                 OntologyUtils.ontologyIDsToModel(
                         Arrays.asList(new InferredOWLOntologyID(IRI.create("urn:test:ontology:iri:abc"), IRI
                                 .create("urn:test:ontology:iri:abc:version:1"), null)), input);
         
         Assert.assertNotNull(ontologyIDsToModel);
         Assert.assertEquals(input, ontologyIDsToModel);
         Assert.assertEquals(3, ontologyIDsToModel.size());
         Assert.assertTrue(ontologyIDsToModel.contains(null, RDF.TYPE, OWL.ONTOLOGY));
         Assert.assertTrue(ontologyIDsToModel.contains(null, OWL.VERSIONIRI, null));
         Assert.assertEquals(2, ontologyIDsToModel.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
      * .
      */
     @Test
     public final void testOntologyIDsToModelNoVersionIRI()
     {
         final Model input = new LinkedHashModel();
         
         final Model ontologyIDsToModel =
                 OntologyUtils.ontologyIDsToModel(
                         Arrays.asList(new InferredOWLOntologyID(IRI.create("urn:test:ontology:iri:abc"), null, null)),
                         input);
         
         Assert.assertNotNull(ontologyIDsToModel);
         Assert.assertEquals(input, ontologyIDsToModel);
         Assert.assertEquals(1, ontologyIDsToModel.size());
         Assert.assertTrue(ontologyIDsToModel.contains(null, RDF.TYPE, OWL.ONTOLOGY));
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#ontologyIDsToModel(java.util.Collection, org.openrdf.model.Model)}
      * .
      */
     @Test
     public final void testOntologyIDsToModelWithInferredIRI()
     {
         final Model input = new LinkedHashModel();
         
         final Model ontologyIDsToModel =
                 OntologyUtils.ontologyIDsToModel(Arrays.asList(new InferredOWLOntologyID(IRI
                         .create("urn:test:ontology:iri:abc"), IRI.create("urn:test:ontology:iri:abc:version:1"), IRI
                         .create("urn:inferred:test:ontology:iri:abc:version:1:1"))), input);
         
         Assert.assertNotNull(ontologyIDsToModel);
         Assert.assertEquals(input, ontologyIDsToModel);
         Assert.assertEquals(5, ontologyIDsToModel.size());
         Assert.assertTrue(ontologyIDsToModel.contains(null, RDF.TYPE, OWL.ONTOLOGY));
         Assert.assertTrue(ontologyIDsToModel.contains(null, OWL.VERSIONIRI, null));
         Assert.assertTrue(ontologyIDsToModel.contains(null, PODD.PODD_BASE_INFERRED_VERSION, null));
         Assert.assertEquals(3, ontologyIDsToModel.filter(null, RDF.TYPE, OWL.ONTOLOGY).size());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#stringToOntologyID(String, RDFFormat)}.
      */
     @Test
     public final void testStringToOntologyIDsInRdfXml() throws Exception
     {
         final InputStream resourceStream = this.getClass().getResourceAsStream("/test/test-ontologyid-2.rdf");
         final String rdfString = IOUtils.toString(resourceStream);
         
         final Collection<InferredOWLOntologyID> ontologyIDs =
                 OntologyUtils.stringToOntologyID(rdfString, RDFFormat.RDFXML);
         
         Assert.assertEquals("Did not find any Ontology IDs", 1, ontologyIDs.size());
         Assert.assertEquals("Version IRI did not match",
                 "http://example.org/purl/c54fbaa5-0767-4f78-88b1-2509ff428f60/artifact:1:version:1", ontologyIDs
                         .iterator().next().getVersionIRI().toString());
     }
     
     /**
      * Test method for
      * {@link com.github.podd.utils.OntologyUtils#stringToOntologyID(String, RDFFormat)}.
      */
     @Test
     public final void testStringToOntologyIDsInTurtle() throws Exception
     {
         final InputStream resourceStream = this.getClass().getResourceAsStream("/test/test-ontologyid-1.ttl");
         final String rdfString = IOUtils.toString(resourceStream);
         
         final Collection<InferredOWLOntologyID> ontologyIDs =
                 OntologyUtils.stringToOntologyID(rdfString, RDFFormat.TURTLE);
         
         Assert.assertEquals("Did not find any Ontology IDs", 1, ontologyIDs.size());
         Assert.assertEquals("Version IRI did not match",
                 "http://example.org/purl/91bb7bff-acd6-4b2e-abf7-ce74d3d91061/artifact:1:version:1", ontologyIDs
                         .iterator().next().getVersionIRI().toString());
     }
     
     @Test
     public final void testGetArtifactImportsNonExistent() throws Exception
     {
         Model model = new LinkedHashModel();
         try
         {
             OntologyUtils.artifactImports(this.testOntologyID, model);
             Assert.fail("Did not find expected exception");
         }
         catch(SchemaManifestException e)
         {
             
         }
     }
     
     @Test
     public final void testGetArtifactImportsNullArtifact() throws Exception
     {
         Model model = new LinkedHashModel();
         try
         {
             OntologyUtils.artifactImports(null, model);
             Assert.fail("Did not find expected exception");
         }
         catch(NullPointerException e)
         {
             
         }
     }
     
     @Test
     public final void testGetArtifactImportsNullModel() throws Exception
     {
         try
         {
             OntologyUtils.artifactImports(this.testOntologyID, null);
             Assert.fail("Did not find expected exception");
         }
         catch(NullPointerException e)
         {
             
         }
     }
     
     @Test
     public final void testGetArtifactImportsNoImports() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
         Assert.assertEquals(0, imports.size());
         Assert.assertTrue(imports.isEmpty());
     }
     
     @Test
     public final void testGetArtifactImportsOneImport() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testOntologyUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         
         DebugUtils.printContents(model);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
         Assert.assertEquals(1, imports.size());
         Assert.assertTrue(imports.contains(this.testImportOntologyID1));
     }
     
     @Test
     public final void testGetArtifactImportsOneImportVersion() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testVersionUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
         Assert.assertEquals(1, imports.size());
         Assert.assertTrue(imports.contains(this.testImportOntologyID1));
     }
     
     @Test
     public final void testGetArtifactImportsOneImportTransitiveSingle() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri1, OWL.IMPORTS, this.testImportVersionUri2);
         model.add(this.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri2, OWL.VERSIONIRI, this.testImportVersionUri2);
         model.add(this.testOntologyUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
         Assert.assertEquals(2, imports.size());
         Assert.assertTrue(imports.contains(this.testImportOntologyID1));
         Assert.assertTrue(imports.contains(this.testImportOntologyID2));
     }
     
     @Test
     public final void testGetArtifactImportsOneImportNonTransitiveSingle() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri2, OWL.VERSIONIRI, this.testImportVersionUri2);
         model.add(this.testOntologyUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
        Assert.assertEquals(2, imports.size());
         Assert.assertTrue(imports.contains(this.testImportOntologyID1));
        Assert.assertTrue(imports.contains(this.testImportOntologyID2));
     }
     
     @Test
     public final void testGetArtifactImportsOneImportTransitiveDouble() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testOntologyUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         
         model.add(this.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri2, OWL.VERSIONIRI, this.testImportVersionUri2);
         model.add(this.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri1, OWL.IMPORTS, this.testImportVersionUri2);
         
         model.add(this.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri3, OWL.VERSIONIRI, this.testImportVersionUri3);
         model.add(this.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri2, OWL.IMPORTS, this.testImportVersionUri3);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
         Assert.assertEquals(3, imports.size());
         Assert.assertTrue(imports.contains(this.testImportOntologyID1));
         Assert.assertTrue(imports.contains(this.testImportOntologyID2));
         Assert.assertTrue(imports.contains(this.testImportOntologyID3));
     }
     
     @Test
     public final void testGetArtifactImportsOneImportTransitiveDoubleDouble() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testOntologyUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         
         model.add(this.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri2, OWL.VERSIONIRI, this.testImportVersionUri2);
         model.add(this.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri1, OWL.IMPORTS, this.testImportVersionUri2);
         
         model.add(this.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri3, OWL.VERSIONIRI, this.testImportVersionUri3);
         model.add(this.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri2, OWL.IMPORTS, this.testImportVersionUri3);
         
         model.add(this.testImportOntologyUri4, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri4, OWL.VERSIONIRI, this.testImportVersionUri4);
         model.add(this.testImportVersionUri4, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri2, OWL.IMPORTS, this.testImportVersionUri4);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
         Assert.assertEquals(4, imports.size());
         Assert.assertTrue(imports.contains(this.testImportOntologyID1));
         Assert.assertTrue(imports.contains(this.testImportOntologyID2));
         Assert.assertTrue(imports.contains(this.testImportOntologyID3));
         Assert.assertTrue(imports.contains(this.testImportOntologyID4));
     }
     
     @Test
     public final void testGetArtifactImportsOneImportVersionTransitiveDoubleDouble() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testVersionUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         
         model.add(this.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri2, OWL.VERSIONIRI, this.testImportVersionUri2);
         model.add(this.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri1, OWL.IMPORTS, this.testImportVersionUri2);
         
         model.add(this.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri3, OWL.VERSIONIRI, this.testImportVersionUri3);
         model.add(this.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri2, OWL.IMPORTS, this.testImportVersionUri3);
         
         model.add(this.testImportOntologyUri4, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri4, OWL.VERSIONIRI, this.testImportVersionUri4);
         model.add(this.testImportVersionUri4, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri2, OWL.IMPORTS, this.testImportVersionUri4);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
         Assert.assertEquals(4, imports.size());
         Assert.assertTrue(imports.contains(this.testImportOntologyID1));
         Assert.assertTrue(imports.contains(this.testImportOntologyID2));
         Assert.assertTrue(imports.contains(this.testImportOntologyID3));
         Assert.assertTrue(imports.contains(this.testImportOntologyID4));
     }
     
     @Test
     public void testGetArtifactImportsRealistic() throws Exception
     {
         Model model =
                 Rio.parse(this.getClass().getResourceAsStream("/test/artifacts/artifact-imports-test.nq"), "",
                         RDFFormat.NQUADS);
         
         model.addAll(Rio.parse(this.getClass().getResourceAsStream("/test/test-podd-schema-manifest.ttl"), "",
                 RDFFormat.TURTLE));
         
         DebugUtils.printContents(model);
         
         Set<OWLOntologyID> imports = OntologyUtils.artifactImports(this.testOntologyID, model);
         
         this.log.info("Imports: {}", imports);
         
         Assert.assertEquals(4, imports.size());
     }
     
     @Test
     public void testImportsOrderFourLevels() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testVersionUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         
         model.add(this.testImportOntologyUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri2, OWL.VERSIONIRI, this.testImportVersionUri2);
         model.add(this.testImportVersionUri2, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri1, OWL.IMPORTS, this.testImportVersionUri2);
         
         model.add(this.testImportOntologyUri3, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri3, OWL.VERSIONIRI, this.testImportVersionUri3);
         model.add(this.testImportVersionUri3, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri2, OWL.IMPORTS, this.testImportVersionUri3);
         
         model.add(this.testImportOntologyUri4, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri4, OWL.VERSIONIRI, this.testImportVersionUri4);
         model.add(this.testImportVersionUri4, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportVersionUri3, OWL.IMPORTS, this.testImportVersionUri4);
         
         Set<URI> schemaOntologyUris = new HashSet<URI>();
         Set<URI> schemaVersionUris = new HashSet<URI>();
         
         schemaOntologyUris.add(this.testOntologyUri1);
         schemaOntologyUris.add(this.testImportOntologyUri1);
         schemaOntologyUris.add(this.testImportOntologyUri2);
         schemaOntologyUris.add(this.testImportOntologyUri3);
         schemaOntologyUris.add(this.testImportOntologyUri4);
         
         schemaVersionUris.add(this.testVersionUri1);
         schemaVersionUris.add(this.testImportVersionUri1);
         schemaVersionUris.add(this.testImportVersionUri2);
         schemaVersionUris.add(this.testImportVersionUri3);
         schemaVersionUris.add(this.testImportVersionUri4);
         
         ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<URI, Set<URI>>();
         // Expected output solution from importsMap after calling orderImports
         // importsMap.put(testVersionUri1, Collections.singleton(this.testImportVersionUri1));
         // importsMap.put(testImportVersionUri1, Collections.singleton(this.testImportVersionUri2));
         // importsMap.put(testImportVersionUri2, Collections.singleton(this.testImportVersionUri3));
         // importsMap.put(testImportVersionUri3, new
         // HashSet<URI>(Arrays.asList(this.testImportVersionUri4)));
         // importsMap.put(testImportVersionUri4, new HashSet<URI>());
         
         List<URI> orderImports = OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
         
         Assert.assertEquals(5, orderImports.size());
         Assert.assertEquals(this.testImportVersionUri4, orderImports.get(0));
         Assert.assertEquals(this.testImportVersionUri3, orderImports.get(1));
         Assert.assertEquals(this.testImportVersionUri2, orderImports.get(2));
         Assert.assertEquals(this.testImportVersionUri1, orderImports.get(3));
         Assert.assertEquals(this.testVersionUri1, orderImports.get(4));
         
         Assert.assertEquals(5, importsMap.size());
         Assert.assertTrue(importsMap.containsKey(this.testImportVersionUri4));
         Assert.assertTrue(importsMap.containsKey(this.testImportVersionUri3));
         Assert.assertTrue(importsMap.containsKey(this.testImportVersionUri2));
         Assert.assertTrue(importsMap.containsKey(this.testImportVersionUri1));
         Assert.assertTrue(importsMap.containsKey(this.testVersionUri1));
         
         Set<URI> imports4 = importsMap.get(this.testImportVersionUri4);
         Assert.assertNotNull(imports4);
         Assert.assertEquals(0, imports4.size());
         
         Set<URI> imports3 = importsMap.get(this.testImportVersionUri3);
         Assert.assertNotNull(imports3);
         Assert.assertEquals(1, imports3.size());
         Assert.assertEquals(this.testImportVersionUri4, imports3.iterator().next());
         
         Set<URI> imports2 = importsMap.get(this.testImportVersionUri2);
         Assert.assertNotNull(imports2);
         Assert.assertEquals(1, imports2.size());
         Assert.assertEquals(this.testImportVersionUri3, imports2.iterator().next());
         
         Set<URI> imports1 = importsMap.get(this.testImportVersionUri1);
         Assert.assertNotNull(imports1);
         Assert.assertEquals(1, imports1.size());
         Assert.assertEquals(this.testImportVersionUri2, imports1.iterator().next());
         
         Set<URI> importsRoot = importsMap.get(this.testVersionUri1);
         Assert.assertNotNull(importsRoot);
         Assert.assertEquals(1, importsRoot.size());
         Assert.assertEquals(this.testImportVersionUri1, importsRoot.iterator().next());
     }
     
     @Test
     public void testImportsOrderOneLevel() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testVersionUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         
         Set<URI> schemaOntologyUris = new HashSet<URI>();
         Set<URI> schemaVersionUris = new HashSet<URI>();
         
         schemaOntologyUris.add(this.testOntologyUri1);
         schemaOntologyUris.add(this.testImportOntologyUri1);
         
         schemaVersionUris.add(this.testVersionUri1);
         schemaVersionUris.add(this.testImportVersionUri1);
         
         ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<URI, Set<URI>>();
         // Expected output solution from importsMap after calling orderImports
         // importsMap.put(testVersionUri1, Collections.singleton(this.testImportVersionUri1));
         // importsMap.put(testImportVersionUri1, new HashSet<URI>());
         
         List<URI> orderImports = OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
         
         Assert.assertEquals(2, orderImports.size());
         Assert.assertEquals(this.testImportVersionUri1, orderImports.get(0));
         Assert.assertEquals(this.testVersionUri1, orderImports.get(1));
         
         Assert.assertEquals(2, importsMap.size());
         Assert.assertTrue(importsMap.containsKey(this.testImportVersionUri1));
         Assert.assertTrue(importsMap.containsKey(this.testVersionUri1));
         
         Set<URI> imports1 = importsMap.get(this.testImportVersionUri1);
         Assert.assertNotNull(imports1);
         Assert.assertEquals(0, imports1.size());
         
         Set<URI> importsRoot = importsMap.get(this.testVersionUri1);
         Assert.assertNotNull(importsRoot);
         Assert.assertEquals(1, importsRoot.size());
         Assert.assertEquals(this.testImportVersionUri1, importsRoot.iterator().next());
     }
     
     @Test
     public void testImportsOrderZeroLevels() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         
         Set<URI> schemaOntologyUris = new HashSet<URI>();
         Set<URI> schemaVersionUris = new HashSet<URI>();
         
         schemaOntologyUris.add(this.testOntologyUri1);
         
         schemaVersionUris.add(this.testVersionUri1);
         
         ConcurrentMap<URI, Set<URI>> importsMap = new ConcurrentHashMap<URI, Set<URI>>();
         // Expected output solution from importsMap after calling orderImports
         // importsMap.put(testVersionUri1, new HashSet<URI>());
         
         List<URI> orderImports = OntologyUtils.orderImports(model, schemaOntologyUris, schemaVersionUris, importsMap, false);
         
         Assert.assertEquals(1, orderImports.size());
         Assert.assertEquals(this.testVersionUri1, orderImports.get(0));
         
         Assert.assertEquals(1, importsMap.size());
         Assert.assertTrue(importsMap.containsKey(this.testVersionUri1));
         
         Set<URI> importsRoot = importsMap.get(this.testVersionUri1);
         Assert.assertNotNull(importsRoot);
         Assert.assertEquals(0, importsRoot.size());
     }
     
     @Test
     public void testGetSchemaManifestImportsZeroLevels() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         
         Set<URI> schemaOntologyUris = new HashSet<URI>();
         Set<URI> schemaVersionUris = new HashSet<URI>();
         
         schemaOntologyUris.add(this.testOntologyUri1);
         
         schemaVersionUris.add(this.testVersionUri1);
         
         Map<URI, Set<OWLOntologyID>> schemaManifestImports =
                 OntologyUtils.schemaManifestImports(model, schemaOntologyUris, schemaVersionUris);
         
         Assert.assertNotNull(schemaManifestImports);
         Assert.assertEquals(1, schemaManifestImports.size());
         Assert.assertTrue(schemaManifestImports.containsKey(this.testVersionUri1));
         
         Set<OWLOntologyID> importsRoot = schemaManifestImports.get(this.testVersionUri1);
         Assert.assertNotNull(importsRoot);
         Assert.assertEquals(0, importsRoot.size());
     }
     
     @Test
     public void testGetSchemaManifestImportsOneLevelCurrentVersion() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testVersionUri1, OWL.IMPORTS, this.testImportOntologyUri1);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, PODD.OMV_CURRENT_VERSION, this.testImportVersionUri1);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         
         DebugUtils.printContents(model);
         
         Set<URI> schemaOntologyUris = new HashSet<URI>();
         Set<URI> schemaVersionUris = new HashSet<URI>();
         
         schemaOntologyUris.add(this.testOntologyUri1);
         schemaOntologyUris.add(this.testImportOntologyUri1);
         
         schemaVersionUris.add(this.testVersionUri1);
         schemaVersionUris.add(this.testImportVersionUri1);
         
         Map<URI, Set<OWLOntologyID>> schemaManifestImports =
                 OntologyUtils.schemaManifestImports(model, schemaOntologyUris, schemaVersionUris);
         
         Assert.assertNotNull(schemaManifestImports);
         Assert.assertEquals(2, schemaManifestImports.size());
         Assert.assertTrue(schemaManifestImports.containsKey(this.testVersionUri1));
         Assert.assertTrue(schemaManifestImports.containsKey(this.testImportVersionUri1));
         
         Set<OWLOntologyID> imports1 = schemaManifestImports.get(this.testImportVersionUri1);
         Assert.assertNotNull(imports1);
         Assert.assertEquals(0, imports1.size());
         
         Set<OWLOntologyID> importsRoot = schemaManifestImports.get(this.testVersionUri1);
         Assert.assertNotNull(importsRoot);
         Assert.assertEquals(1, importsRoot.size());
         Assert.assertEquals(this.testImportOntologyID1, importsRoot.iterator().next());
     }
     
     @Test
     public void testGetSchemaManifestImportsOneLevel() throws Exception
     {
         Model model = new LinkedHashModel();
         OntologyUtils.ontologyIDsToModel(Arrays.asList(this.testOntologyID), model);
         model.add(this.testVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testVersionUri1, OWL.IMPORTS, this.testImportVersionUri1);
         model.add(this.testImportOntologyUri1, RDF.TYPE, OWL.ONTOLOGY);
         model.add(this.testImportOntologyUri1, OWL.VERSIONIRI, this.testImportVersionUri1);
         model.add(this.testImportVersionUri1, RDF.TYPE, OWL.ONTOLOGY);
         
         DebugUtils.printContents(model);
         
         Set<URI> schemaOntologyUris = new HashSet<URI>();
         Set<URI> schemaVersionUris = new HashSet<URI>();
         
         schemaOntologyUris.add(this.testOntologyUri1);
         schemaOntologyUris.add(this.testImportOntologyUri1);
         
         schemaVersionUris.add(this.testVersionUri1);
         schemaVersionUris.add(this.testImportVersionUri1);
         
         Map<URI, Set<OWLOntologyID>> schemaManifestImports =
                 OntologyUtils.schemaManifestImports(model, schemaOntologyUris, schemaVersionUris);
         
         Assert.assertNotNull(schemaManifestImports);
         Assert.assertEquals(2, schemaManifestImports.size());
         Assert.assertTrue(schemaManifestImports.containsKey(this.testVersionUri1));
         Assert.assertTrue(schemaManifestImports.containsKey(this.testImportVersionUri1));
         
         Set<OWLOntologyID> imports1 = schemaManifestImports.get(this.testImportVersionUri1);
         Assert.assertNotNull(imports1);
         Assert.assertEquals(0, imports1.size());
         
         Set<OWLOntologyID> importsRoot = schemaManifestImports.get(this.testVersionUri1);
         Assert.assertNotNull(importsRoot);
         Assert.assertEquals(1, importsRoot.size());
         Assert.assertEquals(this.testImportOntologyID1, importsRoot.iterator().next());
     }
     
 }
