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
 package com.github.podd.restlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Map;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.Model;
 import org.openrdf.model.URI;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.rio.RDFFormat;
 import org.openrdf.rio.RDFParseException;
 import org.openrdf.rio.Rio;
 import org.openrdf.rio.UnsupportedRDFormatException;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Restlet;
 import org.restlet.data.MediaType;
 import org.restlet.data.Protocol;
 import org.restlet.routing.Router;
 import org.restlet.routing.Template;
 import org.restlet.routing.TemplateRoute;
 import org.restlet.routing.Variable;
 import org.restlet.security.ChallengeAuthenticator;
 import org.restlet.security.Role;
 import org.restlet.security.User;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.ansell.propertyutil.PropertyUtil;
 import com.github.ansell.restletutils.CrossOriginResourceSharingFilter;
 import com.github.ansell.restletutils.RestletUtilMediaType;
 import com.github.podd.api.PoddArtifactManager;
 import com.github.podd.api.PoddRepositoryManager;
 import com.github.podd.api.PoddSchemaManager;
 import com.github.podd.api.file.PoddDataRepositoryManager;
 import com.github.podd.exception.PoddRuntimeException;
 import com.github.podd.resources.AboutResourceImpl;
 import com.github.podd.resources.AddObjectResourceImpl;
 import com.github.podd.resources.ArtifactRolesResourceImpl;
 import com.github.podd.resources.CookieLoginResourceImpl;
 import com.github.podd.resources.DataReferenceAttachResourceImpl;
 import com.github.podd.resources.DeleteArtifactResourceImpl;
 import com.github.podd.resources.DeleteObjectResourceImpl;
 import com.github.podd.resources.EditArtifactResourceImpl;
 import com.github.podd.resources.GetArtifactResourceImpl;
 import com.github.podd.resources.GetMetadataResourceImpl;
 import com.github.podd.resources.GetSchemaResourceImpl;
 import com.github.podd.resources.HelpResourceImpl;
 import com.github.podd.resources.IndexResourceImpl;
 import com.github.podd.resources.ListArtifactsResourceImpl;
 import com.github.podd.resources.ListDataRepositoriesResourceImpl;
 import com.github.podd.resources.SearchOntologyResourceImpl;
 import com.github.podd.resources.UploadArtifactResourceImpl;
 import com.github.podd.resources.UserAddResourceImpl;
 import com.github.podd.resources.UserDetailsResourceImpl;
 import com.github.podd.resources.UserEditResourceImpl;
 import com.github.podd.resources.UserListResourceImpl;
 import com.github.podd.resources.UserPasswordResourceImpl;
 import com.github.podd.resources.UserRolesResourceImpl;
 import com.github.podd.resources.UserSearchResourceImpl;
 import com.github.podd.utils.PoddRdfConstants;
 import com.github.podd.utils.PoddRoles;
 import com.github.podd.utils.PoddUser;
 import com.github.podd.utils.PoddUserStatus;
 import com.github.podd.utils.PoddWebConstants;
 
 import freemarker.template.Configuration;
 
 /**
  * This class handles all requests from clients to the OAS Web Service.
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * 
  *         Copied from OAS project (https://github.com/ansell/oas)
  * 
  */
 public class PoddWebServiceApplicationImpl extends PoddWebServiceApplication
 {
     public static final URI ARTIFACT_MGT_GRAPH = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-graph");
     
     public static final URI SCHEMA_MGT_GRAPH = ValueFactoryImpl.getInstance().createURI("urn:test:schema-graph");
     
     private final Logger log = LoggerFactory.getLogger(this.getClass());
     
     /**
      * The Freemarker template configuration.
      */
     private volatile Configuration freemarkerConfiguration;
     private volatile ChallengeAuthenticator auth;
     private volatile PoddSesameRealm realm;
     
     private PoddRepositoryManager poddRepositoryManager;
     private PoddSchemaManager poddSchemaManager;
     private PoddArtifactManager poddArtifactManager;
     private PoddDataRepositoryManager poddDataRepositoryManager;
     
     private Model aliasesConfiguration = new LinkedHashModel();
     
     private PropertyUtil propertyUtil = new PropertyUtil("podd");
     
     /**
      * Default Constructor.
      * 
      * Adds the necessary file protocols and sets up the template location.
      * 
      * @throws OpenRDFException
      */
     public PoddWebServiceApplicationImpl() throws OpenRDFException
     {
         super();
         
         this.log.info("\r\n" + "============================== \r\n" + "PODD Web Application \r\n" + "starting... \r\n"
                 + "==============================");
         
         // List of protocols required by the application
         this.getConnectorService().getClientProtocols().add(Protocol.HTTP);
         this.getConnectorService().getClientProtocols().add(Protocol.CLAP);
         
         // Define extensions for RDF and Javascript
         // These extensions are also used to identify mediatypes in services
         // For example: @Get("owl") will not be processed without the declaration below
         this.getMetadataService().addExtension("rdf", MediaType.APPLICATION_RDF_XML, true);
         this.getMetadataService().addExtension("rj", RestletUtilMediaType.APPLICATION_RDF_JSON, true);
         this.getMetadataService().addExtension("owl", MediaType.APPLICATION_RDF_XML, false);
         this.getMetadataService().addExtension("json", MediaType.APPLICATION_JSON, true);
         this.getMetadataService().addExtension("ttl", MediaType.APPLICATION_RDF_TURTLE, true);
         this.getMetadataService().addExtension("n3", MediaType.TEXT_RDF_N3, true);
         this.getMetadataService().addExtension("nt", MediaType.TEXT_RDF_NTRIPLES, true);
         this.getMetadataService().addExtension("nq",
                 MediaType.register("text/nquads", "The NQuads extension to the NTriples RDF serialisation"), true);
         
         this.getMetadataService().addExtension("js", MediaType.TEXT_JAVASCRIPT, true);
         this.getMetadataService().addExtension("css", MediaType.TEXT_CSS, true);
         
         this.getMetadataService().addExtension("multipart", MediaType.MULTIPART_FORM_DATA, true);
         this.getMetadataService().addExtension("form", MediaType.APPLICATION_WWW_FORM, false);
         
         // Automagically tunnel client preferences for extensions through the
         // tunnel
         this.getTunnelService().setExtensionsTunnel(true);
         
         // This is setup inside of the PoddRepositoryManager
         // this.nextRepository = ApplicationUtils.getNewRepository();
     }
     
     /**
      * 
      */
     @Override
     public boolean authenticate(final PoddAction action, final Request request, final Response response,
             final URI optionalObjectUri)
     {
         if(!action.isAuthRequired())
         {
             return true;
         }
         else if(!request.getClientInfo().isAuthenticated())
         {
             if(this.getAuthenticator() == null)
             {
                 throw new RuntimeException("Could not find authentication method");
             }
             
             // add challenges to the response and set the status to HTTP 401 Unauthorized
             this.getAuthenticator().challenge(response, false);
             
             // Return false after the challenge and HTTP 401 response have been added to the
             // response
             return false;
         }
         else if(this.isUserInactive(request.getClientInfo().getUser()))
         {
             this.log.error("Authenticated user is Inactive. user={}", request.getClientInfo().getUser());
             return false;
         }
         else if(!action.isRoleRequired())
         {
             return true;
         }
         else if(request.getClientInfo().getRoles().contains(PoddRoles.ADMIN.getRole()))
         {
             // All admins can do everything if they are authenticated
             return true;
         }
         else if(!action.matchesForRoles(request.getClientInfo().getRoles()))
         {
             this.log.error("Authenticated user does not have enough privileges to execute the given action: {}", action);
             
             // FIXME: Implement auditing here
             // this.getDataHandler().addLogDetailsForRequest(message, referenceUri,
             // authenticationScope, get, currentUser, currentRole);
             
             return false;
         }
         else if(!action.requiresObjectUris(request.getClientInfo().getRoles()))
         {
             return true;
         }
         
         else if(optionalObjectUri == null)
         {
             this.log.error("Action requires object URIs and none were given: {}", action);
             
             return false;
         }
         else
         {
             final Map<String, Collection<Role>> rolesForObjectMap =
                     this.getRealm().getRolesForObjectAlternate(request.getClientInfo().getUser().getIdentifier(),
                             optionalObjectUri);
             final Collection<Role> rolesCommonAcrossGivenObjects =
                     rolesForObjectMap.get(request.getClientInfo().getUser().getIdentifier());
             
             if(rolesCommonAcrossGivenObjects == null || !action.matchesForRoles(rolesCommonAcrossGivenObjects))
             {
                 this.log.error("Authenticated user does not have enough privileges to execute the given action: {}"
                         + " on the given objects: {}", action, optionalObjectUri);
                 return false;
             }
         }
         
         if(request.getClientInfo().isAuthenticated() && request.getClientInfo().getRoles().isEmpty())
         {
             // TODO: can this case still occur?
             this.log.warn("Authenticated user did not have any roles: user={}", request.getClientInfo().getUser());
         }
         
         return true;
     }
     
     /**
      * Call this method to clean up resources used by PODD. At present it shuts down the Repository.
      */
     public void cleanUpResources()
     {
         try
         {
             // clear all resources and shut down PODD
             this.getPoddRepositoryManager().shutDown();
         }
         catch(final OpenRDFException e)
         {
             this.log.error("Test repository could not be shutdown", e);
         }
     }
     
     /**
      * Create the necessary connections between the application and its handlers.
      */
     @Override
     public Restlet createInboundRoot()
     {
         final ChallengeAuthenticator authenticator = this.getAuthenticator();
         
         if(authenticator == null)
         {
             throw new RuntimeException("Could not find authentication method");
         }
         
         final Router router = new Router(this.getContext());
         
         // Add a route for Login form. Login service is handled by the authenticator
         // NOTE: This only displays the login form. All HTTP POST requests to the login path should
         // be handled by the Authenticator
         final String loginFormPath = PoddWebConstants.PATH_LOGIN_FORM;
         this.log.debug("attaching login service to path={}", loginFormPath);
         router.attach(loginFormPath, CookieLoginResourceImpl.class);
         
         // Add a route for the About page.
         final String aboutPagePath = PoddWebConstants.PATH_ABOUT;
         this.log.debug("attaching about service to path={}", aboutPagePath);
         router.attach(aboutPagePath, AboutResourceImpl.class);
         
         // Add a route for the Help pages.
         final String helpOverviewPath = PoddWebConstants.PATH_HELP;
         this.log.debug("attaching about service to path={}", helpOverviewPath);
         router.attach(helpOverviewPath, HelpResourceImpl.class);
         final String helpPagePath = PoddWebConstants.PATH_HELP + "/{" + PoddWebConstants.KEY_HELP_PAGE_IDENTIFIER + "}";
         this.log.debug("attaching about service to path={}", helpPagePath);
         router.attach(helpPagePath, HelpResourceImpl.class);
         
         // Add a route for the Index page.
         final String indexPagePath = PoddWebConstants.PATH_INDEX;
         this.log.debug("attaching index service to path={}", indexPagePath);
         router.attach(indexPagePath, IndexResourceImpl.class);
         
         // Add a route for the User Details page.
         final String userDetailsPath = PoddWebConstants.PATH_USER_DETAILS;
         this.log.debug("attaching user details service to path={}", userDetailsPath);
         router.attach(userDetailsPath, UserDetailsResourceImpl.class);
         
         // Add a route for List Users page.
         final String userListPath = PoddWebConstants.PATH_USER_LIST;
         this.log.debug("attaching user list service to path={}", userListPath);
         router.attach(userListPath, UserListResourceImpl.class);
         
         // Add a route for Search Users Service.
         final String userSearchPath = PoddWebConstants.PATH_USER_SEARCH;
         this.log.debug("attaching user search service to path={}", userSearchPath);
         router.attach(userSearchPath, UserSearchResourceImpl.class);
         
         // Add a route for Add User page.
         final String userAddPath = PoddWebConstants.PATH_USER_ADD;
         this.log.debug("attaching user add service to path={}", userAddPath);
         router.attach(userAddPath, UserAddResourceImpl.class);
         
         // Add a route for Edit User page.
         final String userEditPath = PoddWebConstants.PATH_USER_EDIT;
         this.log.debug("attaching user edit service to path={}", userEditPath);
         router.attach(userEditPath, UserEditResourceImpl.class);
         
         // Add a route for Change User Password page.
         final String userChangePasswordPath = PoddWebConstants.PATH_USER_EDIT_PWD;
         this.log.debug("attaching user change password service to path={}", userChangePasswordPath);
         router.attach(userChangePasswordPath, UserPasswordResourceImpl.class);
         
         // Add a route for User Roles page.
         final String userRolesPath = PoddWebConstants.PATH_USER_ROLES;
         this.log.debug("attaching user roles service to path={}", userRolesPath);
         router.attach(userRolesPath, UserRolesResourceImpl.class);
         
         // TODO: add routes for other user management pages. (List/Delete Users)
         
         // Add a route for the List Artifacts page.
         final String listArtifactsPath = PoddWebConstants.PATH_ARTIFACT_LIST;
         this.log.debug("attaching List Artifacts service to path={}", listArtifactsPath);
         router.attach(listArtifactsPath, ListArtifactsResourceImpl.class);
         
         // Add a route for the Upload Artifact page.
         final String uploadArtifactPath = PoddWebConstants.PATH_ARTIFACT_UPLOAD;
         this.log.debug("attaching Upload Artifact service to path={}", uploadArtifactPath);
         router.attach(uploadArtifactPath, UploadArtifactResourceImpl.class);
         
         // Add a route for the Get Artifact page.
         final String getArtifactBase = PoddWebConstants.PATH_ARTIFACT_GET_BASE;
         this.log.debug("attaching Get Artifact (base) service to path={}", getArtifactBase);
         router.attach(getArtifactBase, GetArtifactResourceImpl.class);
         
         final String getArtifactInferred = PoddWebConstants.PATH_ARTIFACT_GET_INFERRED;
         this.log.debug("attaching Get Artifact (inferred) service to path={}", getArtifactInferred);
         router.attach(getArtifactInferred, GetArtifactResourceImpl.class);
         
         // Add a route for the Edit Artifact page.
         final String editArtifact = PoddWebConstants.PATH_ARTIFACT_EDIT;
         this.log.debug("attaching Edit Artifact service to path={}", editArtifact);
         router.attach(editArtifact, EditArtifactResourceImpl.class);
         
         // Add a route for the Artifact Role edit page.
         final String artifactRoles = PoddWebConstants.PATH_ARTIFACT_ROLES;
         this.log.debug("attaching Edit Artifact Roles service to path={}", artifactRoles);
         router.attach(artifactRoles, ArtifactRolesResourceImpl.class);
         
         // Add a route for the Delete Artifact page.
         final String deleteArtifact = PoddWebConstants.PATH_ARTIFACT_DELETE;
         this.log.debug("attaching Delete Artifact service to path={}", deleteArtifact);
         router.attach(deleteArtifact, DeleteArtifactResourceImpl.class);
         
         // Add a route for the Attach File Reference page.
         final String attachFileReference = PoddWebConstants.PATH_ATTACH_DATA_REF;
         this.log.debug("attaching File Reference Attach service to path={}", attachFileReference);
         router.attach(attachFileReference, DataReferenceAttachResourceImpl.class);
         
         // Add a route for the List Data Repositories page.
         final String listDataRepositories = PoddWebConstants.PATH_DATA_REPOSITORY_LIST;
         this.log.debug("attaching List Data Repositories service to path={}", listDataRepositories);
         router.attach(listDataRepositories, ListDataRepositoriesResourceImpl.class);
         
         // Add a route for the Search ontology service.
         final String searchService = PoddWebConstants.PATH_SEARCH;
         this.log.debug("attaching Search Ontology service to path={}", searchService);
         router.attach(searchService, SearchOntologyResourceImpl.class);
         
         // Add a route for the Meta-data retrieval service.
         final String getMetadataService = PoddWebConstants.PATH_GET_METADATA;
         this.log.debug("attaching Metadata service to path={}", getMetadataService);
         router.attach(getMetadataService, GetMetadataResourceImpl.class);
         
         // Add a route for the Add Object service.
         final String addObjectService = PoddWebConstants.PATH_OBJECT_ADD;
         this.log.debug("attaching Add Object service to path={}", addObjectService);
         router.attach(addObjectService, AddObjectResourceImpl.class);
         
         // Add a route for the Delete Object page.
         final String deleteObject = PoddWebConstants.PATH_OBJECT_DELETE;
         this.log.debug("attaching Delete Object service to path={}", deleteObject);
         router.attach(deleteObject, DeleteObjectResourceImpl.class);
         
         // Add a route for the Schema retrieval service.
         final String getSchemaService = PoddWebConstants.PATH_GET_SCHEMA;
         this.log.debug("attaching Schema service to path={}", getSchemaService);
         final TemplateRoute schemaService = router.attach(getSchemaService, GetSchemaResourceImpl.class);
         schemaService.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
         final Map<String, Variable> routeVariables = schemaService.getTemplate().getVariables();
         routeVariables.put("schemaPath", new Variable(Variable.TYPE_URI_PATH));
         
         // Add a route for Logout service
         // final String logout = "logout";
         // PropertyUtils.getProperty(PropertyUtils.PROPERTY_LOGOUT_FORM_PATH,
         // PropertyUtils.DEFAULT_LOGOUT_FORM_PATH);
         // this.log.info("attaching logout service to path={}", logout);
         // FIXME: Switch between the logout resource implementations here based on the authenticator
         // router.attach(logout, CookieLogoutResourceImpl.class);
         
         this.log.debug("routes={}", router.getRoutes().toString());
         
         // put the authenticator in front of the resource router so it can handle challenge
         // responses and forward them on to the right location after locking in the authentication
         // data. Authentication of individual methods on individual resources is handled using calls
         // to PoddWebServiceApplication.authenticate()
         authenticator.setNext(router);
         
         final CrossOriginResourceSharingFilter corsFilter = new CrossOriginResourceSharingFilter();
         corsFilter.setNext(authenticator);
         
         return corsFilter;
     }
     
     @Override
     public Model getAliasesConfiguration(final PropertyUtil propertyUtil)
     {
         // If the aliasConfiguration is empty then populate it with the default aliases here
         if(this.aliasesConfiguration.isEmpty())
         {
             final String aliasesFile =
                     propertyUtil.get(PoddRdfConstants.KEY_ALIASES, PoddRdfConstants.PATH_DEFAULT_ALIASES_FILE);
             try (final InputStream input = ApplicationUtils.class.getResourceAsStream(aliasesFile);)
             {
                 if(input != null)
                 {
                     this.setAliasesConfiguration(Rio.parse(input, "", RDFFormat.TURTLE));
                 }
                 else
                 {
                    this.log.error("Could not find default aliases resource: {}", aliasesFile);
                 }
             }
             catch(IOException | RDFParseException | UnsupportedRDFormatException e)
             {
                this.log.error("Could not load default aliases");
                throw new PoddRuntimeException("Could not load default aliases", e);
             }
         }
         
         return this.aliasesConfiguration;
     }
     
     /**
      * Fetches a ChallengeAuthenticator based on the key defined in
      * PropertyUtils.PROPERTY_CHALLENGE_AUTH_METHOD.
      * 
      * Currently defaults to a DigestAuthenticator.
      * 
      * @return A ChallengeAuthenticator that can be used to challenge unauthenticated requests to
      *         resources that need authenticated access.
      */
     @Override
     public ChallengeAuthenticator getAuthenticator()
     {
         return this.auth;
     }
     
     @Override
     public PoddArtifactManager getPoddArtifactManager()
     {
         return this.poddArtifactManager;
     }
     
     @Override
     public PoddDataRepositoryManager getPoddDataRepositoryManager()
     {
         return this.poddDataRepositoryManager;
     }
     
     @Override
     public PoddRepositoryManager getPoddRepositoryManager()
     {
         return this.poddRepositoryManager;
     }
     
     @Override
     public PoddSchemaManager getPoddSchemaManager()
     {
         return this.poddSchemaManager;
     }
     
     @Override
     public PropertyUtil getPropertyUtil()
     {
         return this.propertyUtil;
     }
     
     @Override
     public PoddSesameRealm getRealm()
     {
         return this.realm;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * net.maenad.oas.webservice.impl.OasWebServiceApplicationInterface#getTemplateConfiguration()
      */
     @Override
     public Configuration getTemplateConfiguration()
     {
         return this.freemarkerConfiguration;
     }
     
     /**
      * @param user
      * @return false if the User's status is ACTIVE, true in all other cases
      */
     private boolean isUserInactive(final User user)
     {
         if(user == null)
         {
             return true;
         }
         
         if(user instanceof PoddUser)
         {
             if(((PoddUser)user).getUserStatus() == PoddUserStatus.ACTIVE)
             {
                 return false;
             }
         }
         else
         {
             final PoddUser findUser = this.getRealm().findUser(user.getIdentifier());
             if(findUser.getUserStatus() == PoddUserStatus.ACTIVE)
             {
                 return false;
             }
         }
         return true;
     }
     
     @Override
     public void setAliasesConfiguration(final Model aliasesConfiguration)
     {
         this.aliasesConfiguration = aliasesConfiguration;
     }
     
     /**
      * @param auth
      *            the auth to set
      */
     @Override
     public void setAuthenticator(final ChallengeAuthenticator auth)
     {
         this.auth = auth;
     }
     
     /**
      * @param poddArtifactManager
      *            the poddArtifactManager to set
      */
     @Override
     public void setPoddArtifactManager(final PoddArtifactManager poddArtifactManager)
     {
         this.poddArtifactManager = poddArtifactManager;
     }
     
     @Override
     public void setPoddDataRepositoryManager(final PoddDataRepositoryManager poddDataRepositoryManager)
     {
         this.poddDataRepositoryManager = poddDataRepositoryManager;
     }
     
     /**
      * @param poddRepositoryManager
      *            the poddRepositoryManager to set
      */
     @Override
     public void setPoddRepositoryManager(final PoddRepositoryManager poddRepositoryManager)
     {
         this.poddRepositoryManager = poddRepositoryManager;
     }
     
     /**
      * @param poddSchemaManager
      *            the poddSchemaManager to set
      */
     @Override
     public void setPoddSchemaManager(final PoddSchemaManager poddSchemaManager)
     {
         this.poddSchemaManager = poddSchemaManager;
     }
     
     /**
      * @param realm
      *            the realm to set
      */
     @Override
     public void setRealm(final PoddSesameRealm realm)
     {
         this.realm = realm;
     }
     
     @Override
     public void setTemplateConfiguration(final Configuration nextFreemarkerConfiguration)
     {
         this.freemarkerConfiguration = nextFreemarkerConfiguration;
     }
     
     @Override
     public void stop() throws Exception
     {
         super.stop();
         this.cleanUpResources();
         this.log.info("== Shutting down PODD Web Application ==");
     }
     
 }
