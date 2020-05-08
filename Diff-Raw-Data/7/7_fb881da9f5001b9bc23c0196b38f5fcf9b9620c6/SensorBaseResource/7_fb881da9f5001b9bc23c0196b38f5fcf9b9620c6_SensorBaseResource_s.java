 package org.hackystat.sensorbase.resource.sensorbase;
 
 import org.hackystat.sensorbase.resource.projects.ProjectManager;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.sensordata.SensorDataManager;
 import org.hackystat.sensorbase.resource.sensordatatypes.SdtManager;
 import org.hackystat.sensorbase.resource.users.UserManager;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.server.Server;
 import org.restlet.Context;
 import org.restlet.data.CharacterSet;
 import org.restlet.data.Language;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Resource;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.resource.Variant;
 
 /**
  * An abstract superclass for all SensorBase resources that supplies common 
  * initialization and validation processing. 
  * <p>
  * Initialization processing includes:
  * <ul>
  * <li> Extracting the authenticated user identifier (when authentication available)
  * <li> Extracting the user email from the URI (when available)
  * <li> Declares that the TEXT/XML representational variant is supported.
  * <li> Providing instance variables bound to the ProjectManager, SdtManager, UserManager, and 
  * SensorDataManager.
  * </ul>
  * <p>
  * Validation processing involves a set of "validated" methods. These check the values
  * of various parameters in the request, potentially initializing instance variables
  * as a result.  If the validation process fails, these methods set the Restlet 
  * Status value appropriately and return false. 
  * 
  * @author Philip Johnson
  *
  */
 public abstract class SensorBaseResource extends Resource {
   
   /** The authenticated user, retrieved from the ChallengeResponse, or null. */
   protected String authUser = null;
   
   /** To be retrieved from the URL as the 'user' template parameter, or null. */
   protected String uriUser = null; 
   
   /** The user instance corresponding to the user indicated in the URI string, or null. */
   protected User user = null;
   
   /** The projectName found within the URL string, or null. */
   protected String projectName = null;
   
   /** The project corresponding to the projectName, or null. */
   protected Project project = null;
   
   /** The ProjectManager. */
   protected ProjectManager projectManager = null;
   
   /** The UserManager. */
   protected UserManager userManager = null;
   
   /** The SdtManager. */
   protected SdtManager sdtManager = null;
   
   /** The SensorDataManager. */
   protected SensorDataManager sensorDataManager = null;
   
   /** The server. */
   protected Server server; 
   
   /** Everyone generally wants to create one of these, so declare it here. */
   protected String responseMsg;
   
   /**
    * Provides the following representational variants: TEXT_XML.
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public SensorBaseResource(Context context, Request request, Response response) {
     super(context, request, response);
     if (request.getChallengeResponse() != null) {
       this.authUser = request.getChallengeResponse().getIdentifier();
     }
     this.server = (Server)getContext().getAttributes().get("SensorBaseServer");
     this.uriUser = (String) request.getAttributes().get("user");
     this.projectName = (String) request.getAttributes().get("projectname");
     
     this.projectManager = (ProjectManager)getContext().getAttributes().get("ProjectManager");
     this.userManager = (UserManager)getContext().getAttributes().get("UserManager");
     this.sdtManager = (SdtManager)getContext().getAttributes().get("SdtManager");
     this.sensorDataManager = 
       (SensorDataManager)getContext().getAttributes().get("SensorDataManager");
     getVariants().clear(); // copied from BookmarksResource.java, not sure why needed.
     getVariants().add(new Variant(MediaType.TEXT_XML));
   }
   
 
   /**
    * The Restlet getRepresentation method which must be overridden by all concrete Resources.
    * @param variant The variant requested.
    * @return The Representation. 
    */
   @Override
   public abstract Representation getRepresentation(Variant variant);
   
   /**
    * Creates and returns a new Restlet StringRepresentation built from xmlData.
    * The xmlData will be prefixed with a processing instruction indicating UTF-8 and version 1.0.
    * @param xmlData The xml data as a string. 
    * @return A StringRepresentation of that xmldata. 
    */
   public static StringRepresentation getStringRepresentation(String xmlData) {
     StringBuilder builder = new StringBuilder(500);
     builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
     builder.append(xmlData);
     return new StringRepresentation(builder, MediaType.TEXT_XML, Language.ALL, CharacterSet.UTF_8);
   }
   
   /**
    * Returns true if the authorized user is the administrator.
    * Otherwise sets the Response status and returns false. 
    * @return True if the authorized user is the admin. 
    */
   protected boolean validateAuthUserIsAdmin() {
     try {
       if (userManager.isAdmin(this.authUser)) {
         return true;
       }
       else {
         this.responseMsg = ResponseMessage.adminOnly(this);
         getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, this.responseMsg);
         return false;
       }
     }
     catch (RuntimeException e) {
       this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
       getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
     }
     return false;
   }
   
   /**
    * Returns true if the user in the URI string is defined in the UserManager.
    * Otherwise sets the Response status and returns false.
    * If it returns true, then this.user has the corresponding User instance. 
    * @return True if the URI user is a real user.
    */
   protected boolean validateUriUserIsUser() {
     try {
       this.user = this.userManager.getUser(this.uriUser);
       if (this.user == null) {
         this.responseMsg = ResponseMessage.undefinedUser(this, this.uriUser);
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
         return false;
       }
       else {
         return true;
       }
     }
     catch (RuntimeException e) {
       this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
       getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
     }
     return false;
   }
   
   
   /**
    * Returns true if the project name in the URI string is defined in the ProjectManager.
    * Otherwise sets the Response status and returns false.
    * If it returns true, then this.project has the corresponding Project instance. 
    * @return True if the URI project name is a real project.
    */
   protected boolean validateUriProjectName() {
     try {
       this.project = projectManager.getProject(this.user, this.projectName);
       if (this.project == null) {
         this.responseMsg = ResponseMessage.undefinedProject(this, this.user, this.projectName);
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
         return false;
       }
       else {
         return true;
       }
     }
     catch (RuntimeException e) {
       this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
       getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
     }
     return false;
   }
   
   /**
    * Returns true if the authorized user is the owner of the project in the URL string.
    * Otherwise sets the Response status and returns false.
    * @return True if the authorized user is the owner of the Project.
    */
   protected boolean validateProjectOwner() {
     try {
       if (project.getOwner().equals(this.authUser)) {
         return true;
       }
       else {
         this.responseMsg = ResponseMessage.notProjectOwner(this, this.authUser, this.projectName);
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
         return false;
       }
     }
     catch (RuntimeException e) {
       this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
       getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
     }
     return false;
   }
   
   /**
    * Returns true if the authorized user is either the admin or user in the URI string.
    * Otherwise sets the Response status and returns false.
    * @return True if the authorized user is the admin or the URI user. 
    */
   protected boolean validateAuthUserIsAdminOrUriUser() {
     try {
       if (userManager.isAdmin(this.authUser) || this.uriUser.equals(this.authUser)) {
         return true;
       }
       else {
         this.responseMsg = ResponseMessage.adminOrAuthUserOnly(this, this.authUser, this.uriUser);
         getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
         return false;
       }
     }
     catch (RuntimeException e) {
       this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
       getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
     }
     return false;
   }
   
   /**
    * Returns true if the authorized user can view the project definition. 
    * This is true if the authorized user is the admin, the project owner, or member,
    * spectator, or invitee.
    * Otherwise sets the Response status and returns false.
    * @return True if the authorized user is a project participant.
    */
   protected boolean validateProjectViewer() {
     try {
       if (userManager.isAdmin(this.authUser) || 
           uriUser.equals(this.authUser) ||
           projectManager.isMember(this.user, this.projectName, this.authUser) ||
           projectManager.isInvited(this.user, this.projectName, this.authUser) ||
           projectManager.isSpectator(this.user, this.projectName, this.authUser)) {
         return true;
       }
       else {
         setStatusMiscError(String.format("User %s not authorized to view project %s", this.authUser,
             this.projectName));
         return false;
       }
     }
     catch (RuntimeException e) {
       this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
       getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
     }
     return false;
   }
   
   /**
    * Called when an exception is caught while processing a request.
    * Just sets the response code.  
    * @param timestamp The timestamp that could not be parsed.
    */
   protected void setStatusBadTimestamp (String timestamp) { 
     this.responseMsg = ResponseMessage.badTimestamp(this, timestamp);
     getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
   }
   
   
   /**
    * Called when an exception is caught while processing a request.
    * Just sets the response code.  
    * @param e The exception that was caught.
    */
   protected void setStatusInternalError (Exception e) { 
     this.responseMsg = ResponseMessage.internalError(this, this.getLogger(), e);
     getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, this.responseMsg);
   }
   
   /**
    * Called when a miscellaneous "one off" error is caught during processing.
    * @param msg A description of the error.
    */
   protected void setStatusMiscError (String msg) { 
     this.responseMsg = ResponseMessage.miscError(this, msg);
     getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, this.responseMsg);
   }
 }
