 package org.hackystat.sensorbase.resource.sensorbase;
 
 import java.util.logging.Logger;
 
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.utilities.stacktrace.StackTrace;
 
 /**
  * Provides standardized strings and formatting for response codes.  
  * This class is intended to make error reporting more uniform and informative. 
  * A good error message will always include an explanation for why the operation failed,
  * and what the requested operation was. 
  * @author Philip Johnson
  */
 public class ResponseMessage {
   
   /**
    * The error message for requests that only the admin can handle. 
    * @param resource The resource associated with this request. 
    * @return A string describing the problem.
    */
    static String adminOnly(SensorBaseResource resource) {
    return String.format("Request requires administrator privileges: %s", 
         resource.getRequest().getResourceRef().toString());
   }
   
   /**
    * The error message for requests where the authorized user must be the same as the user
    * in the URI string, or the authorized use is the admin (and then the user in the URI string
    * can be anyone).
    * @param resource The resource associated with this request.
    * @param authUser The authorized user. 
    * @param uriUser The user in the URI string.  
    * @return A string describing the problem.
    */
   static String adminOrAuthUserOnly(SensorBaseResource resource, String authUser, 
       String uriUser) {
    return String.format("Request requires authorized user (%s) to be the same user as the one" +
        "in the URI string (%s), or else the admin: %s", authUser, uriUser, 
         resource.getRequest().getResourceRef().toString());
   }
   
   /**
    * The error message for requests that generate an unspecified internal error. 
    * @param resource The resource associated with this request. 
    * @param logger The logger. 
    * @param e The exception. 
    * @return A string describing the problem.
    */
   static String internalError(SensorBaseResource resource, Logger logger, Exception e) {
    String message =  String.format("Internal error while processing this request: %s %s",
         resource.getRequest().getMethod().getName(),
         resource.getRequest().getResourceRef().toString());
     logger.info(String.format("%s\n%s", message, StackTrace.toString(e)));
     return message;
   }
 
   /**
    * The error message for miscellaneous "one off" error messages. 
    * @param resource The resource associated with this request. 
    * @param message A short string describing the problem.
    * @return A string describing the problem.
    */
    static String miscError(SensorBaseResource resource, String message) {
    return String.format("Error (%s) while processing this request: %s %s", message,  
         resource.getRequest().getMethod().getName(),
         resource.getRequest().getResourceRef().toString());
   }
   
   /**
    * The error message for unknown users.
    * @param resource The resource associated with this request. 
    * @param user A short string describing the problem.
    * @return A string describing the problem.
    */
   static String undefinedUser(SensorBaseResource resource, String user) {
    return String.format("Undefined user (%s) while processing this request: %s %s", user,
         resource.getRequest().getMethod().getName(),
         resource.getRequest().getResourceRef().toString());
   }
   
   /**
    * The error message for requests involving projects not owned by the specified user. 
    * @param resource The resource associated with this request. 
    * @param user The user. 
    * @param project The project. 
    * @return A string describing the problem.
    */
   static String undefinedProject(SensorBaseResource resource, User user, String project) {
    return String.format("Undefined project %s for user %s while processing this request: %s %s", 
        user.getEmail(), project, 
         resource.getRequest().getMethod().getName(),
         resource.getRequest().getResourceRef().toString());
   } 
   
   /**
    * The error message for requests involving projects not owned by the specified user. 
    * @param resource The resource associated with this request. 
    * @param user The user. 
    * @param project The project. 
    * @return A string describing the problem.
    */
   static String cannotViewProject(SensorBaseResource resource, String user, String project) {
    return String.format("User %s not allowed to view project %s while processing request: %s %s", 
         user, project, 
         resource.getRequest().getMethod().getName(),
         resource.getRequest().getResourceRef().toString());
   }
   
   /**
    * The error message for requests where the requesting user is not the owner. 
    * @param resource The resource associated with this request. 
    * @param user The user. 
    * @param project The project
    * @return A string describing the problem.
    */
   static String notProjectOwner(SensorBaseResource resource, String user, String project) {
    return String.format("Authorized user (%s) is not owner of project (%s) for request: %s %s", 
         user, project,
         resource.getRequest().getMethod().getName(),
         resource.getRequest().getResourceRef().toString());
   }
   
   
   /**
    * The error message for requests where a timestamp is not supplied or is not parsable.
    * @param resource The resource associated with this request.
    * @param timestamp The bogus timestamp. 
    * @return A string describing the problem.
    */
   static String badTimestamp(SensorBaseResource resource, String timestamp) {
    return String.format("Timestamp %s is incorrect in this request: %s %s", 
         timestamp,
         resource.getRequest().getMethod().getName(),
         resource.getRequest().getResourceRef().toString());
   }
 
 }
