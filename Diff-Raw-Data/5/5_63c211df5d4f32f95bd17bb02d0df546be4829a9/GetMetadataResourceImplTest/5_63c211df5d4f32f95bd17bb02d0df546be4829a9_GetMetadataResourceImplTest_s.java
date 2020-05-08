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
 package com.github.podd.resources.test;
 
 import java.io.ByteArrayInputStream;
 import java.nio.charset.StandardCharsets;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.rio.RDFFormat;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.api.test.TestConstants;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author kutila
  * 
  */
 public class GetMetadataResourceImplTest extends AbstractResourceImplTest
 {
     @Test
     public void testErrorGetWithInvalidObjectType() throws Exception
     {
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "NoSuchPoddConcept";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: response is empty as no such object exists
         Assert.assertNull("Expected NULL for response text", results.getText());
     }
     
     @Test
     public void testGetChildrenWithInvestigationRdf() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE)
                         .getOntologyIRI().toString();
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "Investigation";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "false");
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                 PoddWebConstants.METADATA_ONLY_CONTAINS);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify:
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 64);
         
         Assert.assertEquals(
                 "GrowthConditions not found",
                 1,
                 model.filter(null, OWL.ALLVALUESFROM,
                         PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_PLANT, "GrowthConditions")).subjects()
                         .size());
         
         Assert.assertEquals(
                 "No label for FieldConditions",
                 1,
                 model.filter(PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_PLANT, "FieldConditions"), RDFS.LABEL,
                         null).objects().size());
         
         Assert.assertEquals("Unexpected no. of properties", 12,
                 model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size());
         Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                 model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
     }
     
     @Test
     public void testGetChildrenWithProjectRdf() throws Exception
     {
         final Object[][] testData =
                 { { PoddRdfConstants.PODD_SCIENCE + "Project", 42, 7, 0 },
                         { PoddRdfConstants.PODD_SCIENCE + "Investigation", 64, 12, 0 }, };
         
         for(final Object[] element : testData)
         {
             
             final String objectType = (String)element[0];
             final int expectedModelSize = (int)element[1];
             final int expectedNoOfProperties = (int)element[2];
             final int expectedNoOfDnDProperties = (int)element[3];
             
             final ClientResource createObjectClientResource =
                     new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
             
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES,
                     "false");
             createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                     PoddWebConstants.METADATA_ONLY_CONTAINS);
             
             final Representation results =
                     RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                             MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
             
             final String body = results.getText();
             
             // verify:
             final Model model =
                     this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE,
                             expectedModelSize);
             
             Assert.assertEquals("Unexpected no. of properties", expectedNoOfProperties,
                     model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size());
             Assert.assertEquals("Expected no Do-Not-Display properties", expectedNoOfDnDProperties,
                     model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
         }
     }
     
     @Test
     public void testGetChildrenWithPublicationRdf() throws Exception
     {
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "Publication";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "false");
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                 PoddWebConstants.METADATA_ONLY_CONTAINS);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify:
         Assert.assertNull("No content since Publication cannot have child objects", body);
         // final Model model =
         // this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)),
         // RDFFormat.TURTLE, 7);
         //
         // Assert.assertEquals("Unexpected no. of properties", 1,
         // model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
         // Assert.assertEquals("Expected no Do-Not-Display properties", 0,
         // model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
     }
     
     @Test
     public void testGetWithGenotypeRdf() throws Exception
     {
         // prepare: add an artifact
         final String artifactUri =
                 this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE)
                         .getOntologyIRI().toString();
         
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "Genotype";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, "true");
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_METADATA_POLICY,
                 PoddWebConstants.METADATA_ALL);
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify: received contents are in RDF
         Assert.assertTrue("Result does not have RDF", body.contains("<rdf:RDF"));
         Assert.assertTrue("Result does not have RDF", body.endsWith("</rdf:RDF>"));
         
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.RDFXML, 151);
         
         Assert.assertEquals("Unexpected no. of properties", 18,
                 model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
         Assert.assertEquals("Expected no Do-Not-Display properties", 4,
                 model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
     }
     
     @Test
     public void testGetWithInvestigationRdf() throws Exception
     {
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "Investigation";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         
         // include-do-not-display-properties defaults to false
         // metadata-policy defaults to exclude sub-properties of poddBase:contains
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         System.out.println(body);
         // verify:
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 54);
         
         Assert.assertEquals("Unexpected no. of properties", 6,
                 model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
         Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                 model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
         Assert.assertEquals(
                 "Missing metadata about poddScience::refersToProcess",
                 5,
                 model.filter(PoddRdfConstants.VF.createURI(PoddRdfConstants.PODD_SCIENCE, "refersToProcess"), null,
                         null).size());
     }
     
     @Test
     public void testGetWithProjectRdf() throws Exception
     {
         final ClientResource createObjectClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_GET_METADATA));
         
         final String objectType = PoddRdfConstants.PODD_SCIENCE + "Project";
         createObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, objectType);
         
         // include-do-not-display-properties defaults to false
         // metadata-policy defaults to exclude sub-properties of poddBase:contains
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(createObjectClientResource, Method.GET, null,
                         MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final String body = results.getText();
         
         // verify:
         final Model model =
                this.assertRdf(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), RDFFormat.TURTLE, 86);
         
        Assert.assertEquals("Unexpected no. of properties", 10,
                 model.filter(PoddRdfConstants.VF.createURI(objectType), null, null).size() - 1);
         Assert.assertEquals("Expected no Do-Not-Display properties", 0,
                 model.filter(null, PoddRdfConstants.PODD_BASE_DO_NOT_DISPLAY, null).size());
     }
 }
