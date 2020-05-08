 package info.mikaelsvensson.cashbook.server.rest.exceptionmapper;
 
 import info.mikaelsvensson.cashbook.core.CashbookAuthorizationException;
 import info.mikaelsvensson.cashbook.core.exception.ExistingValueConflictException;
 import info.mikaelsvensson.cashbook.core.exception.IdNotFoundException;
 
 import javax.ejb.EJBAccessException;
 import javax.ejb.EJBException;
 import javax.ejb.EJBTransactionRolledbackException;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.ext.ExceptionMapper;
 import javax.ws.rs.ext.Provider;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 @Provider
 public class ThrowableMapper implements ExceptionMapper<Throwable> {
 
     private final static Logger LOGGER = Logger.getLogger(ThrowableMapper.class.getName());
 
     @Override
     public Response toResponse(Throwable e) {
         if (LOGGER.isLoggable(Level.INFO)) {
             LOGGER.info("Caught " + e.getClass().getSimpleName() + ": " + e.getMessage());
             LOGGER.throwing(null, null, e);
         }
         if (e instanceof CashbookAuthorizationException) {
             return toForbiddenResponse(toNormalMessage(e));
         } else if (e instanceof EJBAccessException) {
             return toForbiddenResponse("Unauthorized cashbook request");
         } else if (e instanceof ExistingValueConflictException) {
             return toConflictResponse(toNormalMessage(e));
         } else if (e instanceof IdNotFoundException) {
             return toNotFoundResponse(e.getMessage());
         } else if (e instanceof EJBTransactionRolledbackException) {
             return toServerErrorResponse(toNormalMessage(e));
         } else if (e instanceof EJBException) {
             return toServerErrorResponse(toNormalMessage(e));
         /*} else if (e instanceof WebApplicationException) {
             return toServerErrorResponse(toErrorNameOnlyMessage(e));
         } else if (e instanceof RuntimeException) {
             return toServerErrorResponse(toErrorNameOnlyMessage(e));
         } else if (e instanceof Error) {
             return toServerErrorResponse(toErrorNameOnlyMessage(e));*/
         } else {
             return toServerErrorResponse(toErrorNameOnlyMessage(e));
         }
     }
 
     private Response toForbiddenResponse(String message) {
         return Response.status(Response.Status.FORBIDDEN).entity(message).build();
     }
 
     private Response toConflictResponse(String message) {
         return Response.status(Response.Status.CONFLICT).entity(message).build();
     }
 
     private String toNormalMessage(Throwable e) {
         String message = e.getMessage();
         Throwable cause = e.getCause();
         if (cause != null) {
             message += " (" + cause.getMessage() + ")";
         }
         return message;
     }
 
     private String toErrorNameOnlyMessage(Throwable e) {
         return e.getClass().getSimpleName();
     }
 
     private Response toNotFoundResponse(String message) {
         return Response.status(Response.Status.NOT_FOUND).entity(message).build();
     }
 
     private Response toServerErrorResponse(String message) {
         return Response.serverError().entity(message).build();
     }
 }
