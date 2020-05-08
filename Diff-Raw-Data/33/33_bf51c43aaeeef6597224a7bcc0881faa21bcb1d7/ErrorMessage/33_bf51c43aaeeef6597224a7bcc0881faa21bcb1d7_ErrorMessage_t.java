 package de.tourenplaner.server;
 
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 
 /**
  *  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  */
 public enum ErrorMessage {
 
     // json errors
 
     EBADJSON (
             "Could not parse JSON object or JSON object is missing",
             HttpResponseStatus.BAD_REQUEST),
 
     EBADJSON_NOCONTENT (
             EBADJSON,
             "Content is empty"),
 
     EBADJSON_INCORRECT_USER_OBJ (
             EBADJSON,
             "JSON user object was not correct (needs email, password, firstname, lastname, address)"),
 
     EBADJSON_INCORRECT_USER_OBJ_ADMIN (
             EBADJSON,
             "JSON user object was not correct (\"admin\" should be boolean)"),
 
     EBADJSON_INCORRECT_USER_OBJ_STATUS (
             EBADJSON,
             "JSON user object was not correct (\"status\" was not a valid value)"),
 
     // server errors
 
     EBUSY (
             "This server is currently too busy to fulfill the request",
             HttpResponseStatus.SERVICE_UNAVAILABLE),
 
     ECOMPUTE (
             "An error occurred during computation or result message generation",
             HttpResponseStatus.INTERNAL_SERVER_ERROR),
 
     ECOMPUTE_RESULT_NOT_SENT_OR_STORED (
             ECOMPUTE,
             "The server could not send and not store the compute result"),
 
     EDATABASE (
             "The server cannot contact its database or a database error occurred",
             HttpResponseStatus.INTERNAL_SERVER_ERROR),
 
 
     // parameter errors
 
     ELIMIT (
             "The parameter limit is missing or invalid",
             HttpResponseStatus.BAD_REQUEST),
 
     ELIMIT_MISSING (
             ELIMIT,
             "The parameter limit is missing"),
 
     ELIMIT_NOT_NAT_NUMBER (
             ELIMIT,
             "The given parameter limit is not a natural number (positive or zero)"),
 
 
     EOFFSET (
             "The parameter offset is missing or invalid",
             HttpResponseStatus.BAD_REQUEST),
 
     EOFFSET_MISSING (
             EOFFSET,
             "The parameter offset is missing"),
 
     EOFFSET_NOT_NAT_NUMBER(
             EOFFSET,
             "The given parameter offset is not a natural number (positive or zero)"),
 
 
     ENOREQUESTID (
             "The parameter request id is missing, invalid or unknown to this server",
             HttpResponseStatus.NOT_FOUND),
 
     ENOREQUESTID_NOT_IN_DB (
             ENOREQUESTID,
             "The given parameter id is not in the database"),
 
     ENOREQUESTID_MISSING (
             ENOREQUESTID,
             "The parameter id is missing"),
 
     ENOREQUESTID_NOT_NAT_NUMBER(
             ENOREQUESTID,
             "The given parameter id is not a natural number (positive or zero)"),
 
 
     ENOUSERID (
             "The parameter user id is missing, invalid or unknown to this server",
             HttpResponseStatus.NOT_FOUND),
 
     ENOUSERID_NOT_IN_DB (
             ENOUSERID,
             "The given parameter id is not in the database"),
 
     ENOUSERID_MISSING (
             ENOUSERID,
             "The parameter id is missing"),
 
     ENOUSERID_NOT_NAT_NUMBER(
             ENOUSERID,
             "The given parameter id is not a natural number (positive or zero)"),
 
     ENOUSERID_NOT_NAT_NUMBER_OR_ALL(
             ENOUSERID,
             "The given parameter id is not 'all' or a natural number (positive or zero)"),
 
 
     // forbidden action and authorization errors
 
     EAUTH (
             "Wrong username or password",
             HttpResponseStatus.UNAUTHORIZED),
 
 
     ENOTADMIN (
             "You are not an admin",
             HttpResponseStatus.FORBIDDEN),
 
     ENOTADMIN_OTHER_USER_REQUEST (
             ENOTADMIN,
             "You cannot view requests or responses of other users"),
 
     ENOTADMIN_LIST_USERS (
             ENOTADMIN,
             "You must be admin to list users"),
 
     ENOTADMIN_REGISTER_USER (
             ENOTADMIN,
             "You must first logout yourself to register users if you are not an admin."),
 
     ENOTADMIN_USER_ID_PARAM (
             ENOTADMIN,
             "You must be admin if you want to use the user id parameter"),
 
 
     ENOTVERIFIED (
             "User account is not verified",
             HttpResponseStatus.FORBIDDEN),
 
 
     EREGISTERED (
             "This email is already registered",
             HttpResponseStatus.FORBIDDEN),
 
     // unknown request errors
 
     EUNKNOWNALG (
             "An unknown algorithm was requested",
             HttpResponseStatus.NOT_FOUND),
 
     EUNKNOWNURL (
             "An unknown URL was requested",
             HttpResponseStatus.NOT_FOUND);
 
 
     public final String errorId;
     public final String message;
     public final String details;
     public final HttpResponseStatus status;
 
 
     /**
      * Use this method if you want to create an ErrorMessage with a specified message
     * and a specified status. The details will be an empty string, the errorId will
     * be the name of the enum.
      *
      * @param message The error message
      * @param status The error status
      */
     private ErrorMessage(String message, HttpResponseStatus status) {
        this.errorId = this.name();
        this.message = message;
        this.details = "";
        this.status = status;
     }
 
     /**
     * Use this method if you want to create an ErrorMessage with the errorId, message
      * and status of another ErrorMessage and then add the given details.
      *
      * @param errorMessage The ErrorMessage
      * @param details The error details
      */
     private ErrorMessage(ErrorMessage errorMessage, String details) {
        this.errorId = errorMessage.name();
        this.message = errorMessage.message;
        this.details = details;
        this.status = errorMessage.status;
     }
 
 }
