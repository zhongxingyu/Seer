 /**
  * 
  */
 package com.github.podd.resources.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.nio.charset.StandardCharsets;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.Rio;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 
 import com.github.ansell.restletutils.SesameRealmConstants;
 import com.github.ansell.restletutils.test.RestletTestUtils;
 import com.github.podd.restlet.PoddRoles;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddWebConstants;
 
 /**
  * @author kutila
  * 
  */
 public class UserAddResourceImplTest extends AbstractResourceImplTest
 {
     
     /**
      * Test adding a PoddUser without using the utility method AbstractResourceImplTest.loadTestUser() 
      */
     @Test
     public void testAddUserRdfBasic() throws Exception
     {
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
         
         // prepare: create a Model of user
         final String testEmail = "testuser@restlet-test.org";
         final String testPassword = "testpassword";
         final String testFirstName = "First";
         final String testLastName = "Last";
         
         final Model userInfoModel = new LinkedHashModel();
         final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                 PoddRdfConstants.VF.createLiteral(testEmail));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                 PoddRdfConstants.VF.createLiteral(testPassword));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                 PoddRdfConstants.VF.createLiteral(testFirstName));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERLASTNAME,
                 PoddRdfConstants.VF.createLiteral(testLastName));
         userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_HOMEPAGE,
                 PoddRdfConstants.VF.createURI("http://nohomepage"));
         userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORGANIZATION,
                 PoddRdfConstants.VF.createLiteral("n/a"));
         userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORCID, PoddRdfConstants.VF.createLiteral("n/a"));
         
         userInfoModel
                 .add(tempUserUri, SesameRealmConstants.OAS_USEREMAIL, PoddRdfConstants.VF.createLiteral(testEmail));
         
         // prepare: add 'Repository Admin User' Role
         final URI authenticatedRoleMapping =
                 PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
         userInfoModel.add(authenticatedRoleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
         userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
         userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                 PoddRoles.ADMIN.getURI());
         
         // prepare: add 'Project Observer' Role of an imaginary project
         final URI observerRoleMapping =
                 PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
         userInfoModel.add(observerRoleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
         userInfoModel.add(observerRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
         userInfoModel.add(observerRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                 PoddRoles.PROJECT_OBSERVER.getURI());
         userInfoModel.add(observerRoleMapping, PoddWebConstants.PODD_ROLEMAPPEDOBJECT,
                 PoddRdfConstants.VF.createURI("urn:podd:some:project"));
         
         final ClientResource userAddClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
         
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         Rio.write(userInfoModel, out, format);
         
         final Representation input = new StringRepresentation(out.toString(), mediaType);
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.POST, input, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         // verify: response has same correct identifier
         final Model model =
                 this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)),
                         RDFFormat.RDFXML, 1);
         Assert.assertEquals("Unexpected user identifier", testEmail,
                 model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
     }
     
     @Test
     public void testAddUserWithAllAttributesRdf() throws Exception
     {
         // prepare: add a Test User account
         final String testIdentifier = "testuser@podd.com";
         final Map<URI, URI> roles = new HashMap<URI, URI>();
         roles.put(PoddRoles.ADMIN.getURI(), null);
         roles.put(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project"));
         String testUserUri =
                 this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                         "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address",
                         "Researcher", roles);
 
         // verify: 
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
 
         final ClientResource userDetailsClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final Model resultsModel =
                 this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 18);
         
         com.github.podd.utils.DebugUtils.printContents(resultsModel);
         Assert.assertEquals("Unexpected user identifier", testIdentifier,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
         Assert.assertEquals("Unexpected user URI", testUserUri,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next().stringValue());
     }
     
     @Test
     public void testAddUserWithOnlyMandatoryAttributesRdf() throws Exception
     {
         // prepare: add a Test User account
         final String testIdentifier = "testuser@podd.com";
         final Map<URI, URI> roles = new HashMap<URI, URI>();
         roles.put(PoddRoles.ADMIN.getURI(), null);
         roles.put(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project"));
         String testUserUri = this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier, null, null,
                 null, null, null, null, null, roles);
 
         // verify: 
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
 
         final ClientResource userDetailsClientResource =
                 new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
         
         final Representation results =
                 RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                         Status.SUCCESS_OK, this.testWithAdminPrivileges);
         
         final Model resultsModel =
                 this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 11);
         
         //com.github.podd.utils.DebugUtils.printContents(resultsModel);
         Assert.assertEquals("Unexpected user identifier", testIdentifier,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
         Assert.assertEquals("Unexpected user URI", testUserUri,
                 resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next().stringValue());
     }
 
     @Test
     public void testErrorAddUserWithIdentifierNotMatchingEmailRdf() throws Exception
     {
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
         
         // prepare: create a Model of user
         final String testIdentifier = "wrong@restlet-test.org";
         final String testEmail = "testuser@restlet-test.org";
         final String testPassword = "testpassword";
         final String testFirstName = "First";
         final String testLastName = "Last";
         
         final Model userInfoModel = new LinkedHashModel();
         final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                 PoddRdfConstants.VF.createLiteral(testEmail));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                 PoddRdfConstants.VF.createLiteral(testPassword));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                 PoddRdfConstants.VF.createLiteral(testFirstName));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERLASTNAME,
                 PoddRdfConstants.VF.createLiteral(testLastName));
         userInfoModel
                 .add(tempUserUri, SesameRealmConstants.OAS_USEREMAIL, PoddRdfConstants.VF.createLiteral(testIdentifier));
         
         // prepare: add 'Authenticated User' Role
         final URI authenticatedRoleMapping =
                 PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
         userInfoModel.add(authenticatedRoleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
         userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
         userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                 PoddRoles.ADMIN.getURI());
         
         final ClientResource userAddClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
         
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         Rio.write(userInfoModel, out, format);
         final Representation input = new StringRepresentation(out.toString(), mediaType);
         
         try
         {
             RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.POST, input, mediaType,
                     Status.SUCCESS_OK, this.testWithAdminPrivileges);
             Assert.fail("Should have failed due to mismatching identifier");
         }
         catch(final ResourceException e)
         {
             // verify: the cause (simple string matching, not checking for valid RDF content)
             Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
             final String body = userAddClientResource.getResponseEntity().getText();
             Assert.assertTrue("Expected cause is missing", body.contains("User Email has to be the same as User Identifier"));
         }
     }
     
     @Test
     public void testErrorAddDuplicateUser() throws Exception
     {
         final String testIdentifier = "testuser@podd.com";
 
         // prepare: add a Test User account
         final Map<URI, URI> roles = new HashMap<URI, URI>();
         roles.put(PoddRoles.ADMIN.getURI(), null);
         roles.put(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project"));
         this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier, null, null,
                 null, null, null, null, null, roles);
         
         // prepare: add another User account with same Identifier/email
         final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
         final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
         
         final Model userInfoModel = new LinkedHashModel();
         final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                 PoddRdfConstants.VF.createLiteral(testIdentifier));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                PoddRdfConstants.VF.createLiteral("testpassword"));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral("First"));
         userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral("Last"));
         userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_HOMEPAGE,
                 PoddRdfConstants.VF.createURI("http://nohomepage"));
         userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORGANIZATION,
                 PoddRdfConstants.VF.createLiteral("n/a"));
         userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORCID, PoddRdfConstants.VF.createLiteral("n/a"));
         userInfoModel
                 .add(tempUserUri, SesameRealmConstants.OAS_USEREMAIL, PoddRdfConstants.VF.createLiteral(testIdentifier));
         
         
         final ClientResource userAddClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
         
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         Rio.write(userInfoModel, out, format);
         
         final Representation input = new StringRepresentation(out.toString(), mediaType);
         
         try
         {
                 RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.POST, input, mediaType,
                        Status.CLIENT_ERROR_CONFLICT, this.testWithAdminPrivileges);
                 Assert.fail("Should throw an exception because Identifier already used");
         }
         catch (ResourceException e)
         {
             // verify: the cause (simple string matching, not checking for valid RDF content)
             Assert.assertEquals(Status.CLIENT_ERROR_CONFLICT, e.getStatus());
             final String body = userAddClientResource.getResponseEntity().getText();
             System.out.println(body);
             Assert.assertTrue("Expected cause is missing", body.contains("User already exists"));
         }
     }
     
 }
