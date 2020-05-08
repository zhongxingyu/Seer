 package vinna.response;
 
 public class ClientError extends ResponseBuilder {
     private static enum Kind {
         BAD_REQUEST(400),
         UNAUTHORIZED(401),
         FORBIDDEN(403),
         NOT_FOUND(404),
         METHOD_NOT_ALLOWED(405),
         NOT_ACCEPTABLE(406),
         CONFLICT(409),
         GONE(410),
         TOO_MANY_REQUESTS(429);
 
         public final int status;
 
         private Kind(int status) {
             this.status = status;
         }
     }
 
     public static ClientError badRequest() {
         return new ClientError(Kind.BAD_REQUEST);
     }
 
     public static ClientError unauthorized() {
         return new ClientError(Kind.UNAUTHORIZED);
     }
 
     public static ClientError forbidden() {
         return new ClientError(Kind.FORBIDDEN);
     }
 
     public static ClientError notFound() {
         return new ClientError(Kind.NOT_FOUND);
     }
 
     public static ClientError methodNotAllowed() {
         return new ClientError(Kind.METHOD_NOT_ALLOWED);
     }
 
     public static ClientError notAcceptable() {
         return new ClientError(Kind.NOT_ACCEPTABLE);
     }
 
     public static ClientError conflict() {
         return new ClientError(Kind.CONFLICT);
     }
 
     public static ClientError gone() {
         return new ClientError(Kind.GONE);
     }
 
     public static ClientError tooManyRequest() {
         return new ClientError(Kind.TOO_MANY_REQUESTS);
     }
 
     private ClientError(Kind kind) {
         super(kind.status);
     }
 }
