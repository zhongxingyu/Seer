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
 package au.org.plantphenomics.podd.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.concurrent.ConcurrentMap;
 import java.util.regex.Matcher;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.queryrender.RenderUtils;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 
 import au.org.plantphenomics.podd.HrppcPoddClient;
 
 import com.github.podd.client.api.test.AbstractPoddClientTest;
 import com.github.podd.client.impl.restlet.test.RestletPoddClientImplIntegrationTest;
 import com.github.podd.utils.InferredOWLOntologyID;
 import com.github.podd.utils.PoddRdfConstants;
 
 /**
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  */
 public class HrppcPoddClientIntegrationTest extends RestletPoddClientImplIntegrationTest
 {
     @Override
     protected HrppcPoddClient getNewPoddClientInstance()
     {
         return new HrppcPoddClient();
     }
     
     @Test
     public final void testRegexProject() throws Exception
     {
         final Matcher matcher = HrppcPoddClient.REGEX_PROJECT.matcher("Project#2014-0001");
         Assert.assertTrue(matcher.matches());
         
     }
     
     @Test
     public final void testRegexPosition() throws Exception
     {
         final Matcher matcher1 = HrppcPoddClient.REGEX_POSITION.matcher("B2");
         Assert.assertTrue(matcher1.matches());
         
         final Matcher matcher2 = HrppcPoddClient.REGEX_POSITION.matcher("AB23454");
         Assert.assertTrue(matcher2.matches());
     }
     
     @Test
     public final void testRegexTray() throws Exception
     {
         final Matcher matcher =
                 HrppcPoddClient.REGEX_TRAY
                         .matcher("Project#2014-0001_Experiment#0001_IArabidopsis.thaliana_Tray#00009");
         Assert.assertTrue(matcher.matches());
         
     }
     
     @Test
     public final void testTemplateProject() throws Exception
     {
         final String formattedProject = String.format(HrppcPoddClient.TEMPLATE_PROJECT, 4, 6);
         
         Assert.assertEquals("Project#0004-0006", formattedProject);
     }
     
     @Test
     public final void testTemplateSparqlExperiments() throws Exception
     {
         final String formattedQueryString =
                 String.format(HrppcPoddClient.TEMPLATE_SPARQL_BY_TYPE,
                         RenderUtils.getSPARQLQueryString(PoddRdfConstants.PODD_SCIENCE_INVESTIGATION));
         
         Assert.assertEquals(
                 "CONSTRUCT { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } WHERE { ?object a ?type . OPTIONAL { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . } } VALUES (?type) { ( <http://purl.org/podd/ns/poddScience#Investigation> ) }",
                 formattedQueryString);
     }
     
     /**
      * Test method for
      * {@link au.org.plantphenomics.podd.HrppcPoddClient#processTrayScanList(java.io.InputStream)}.
      * 
      * @throws Exception
      * @throws
      */
     @Test
     public final void testUploadPlantScanList() throws Exception
     {
         final HrppcPoddClient poddClient = this.getNewPoddClientInstance();
         poddClient.setPoddServerUrl(this.getTestPoddServerUrl());
         poddClient.login(AbstractPoddClientTest.TEST_ADMIN_USER, AbstractPoddClientTest.TEST_ADMIN_PASSWORD);
         
         final InputStream input = this.getClass().getResourceAsStream("/test/artifacts/basicProject-3.rdf");
         Assert.assertNotNull("Test resource missing", input);
         
         final InferredOWLOntologyID newArtifact = poddClient.uploadNewArtifact(input, RDFFormat.RDFXML);
         Assert.assertNotNull(newArtifact);
         Assert.assertNotNull(newArtifact.getOntologyIRI());
         Assert.assertNotNull(newArtifact.getVersionIRI());
         // Must not be leaking the inferred ontology information to users
         Assert.assertNull(newArtifact.getInferredOntologyIRI());
         
         ConcurrentMap<InferredOWLOntologyID, Model> uploadQueue =
                 poddClient.processTrayScanList(this.getClass()
                         .getResourceAsStream("/test/hrppc/PlantScan-Template.csv"));
         
         Assert.assertEquals(1, uploadQueue.size());
         
         ConcurrentMap<InferredOWLOntologyID, InferredOWLOntologyID> uploadedArtifacts =
                 poddClient.uploadToPodd(uploadQueue);
         
         Assert.assertEquals(1, uploadedArtifacts.size());
         
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
         
         // Dump for debugging
         poddClient.downloadArtifact(uploadedArtifacts.get(newArtifact), outputStream, RDFFormat.RDFJSON);
         
        this.parseRdf(new ByteArrayInputStream(outputStream.toByteArray()), RDFFormat.RDFJSON, 1397);
     }
 }
