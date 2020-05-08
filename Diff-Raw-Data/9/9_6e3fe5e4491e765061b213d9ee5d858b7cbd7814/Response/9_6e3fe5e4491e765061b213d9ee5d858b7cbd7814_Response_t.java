 package http;
 
 import http.attribute.AttributeHashSetMap;
 import http.attribute.AttributeMap;
 import http.header.Header;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import static http.Cookie.SET_COOKIE;
 
 /**
  * Represents an {@code HTTP} response and provides access to all the standard response components.
  *
  * @author Karl Bennett
  */
 public class Response<T> extends Message<T> {
 
     private int status;
 
     /**
      * Create a {@code Response} with the supplied HTTP status code.
      *
      * @param status the status code for the response.
      */
     public Response(int status) {
 
         this(status, null);
     }
 
     /**
      * Create a {@code Response} with the supplied HTTP status code and body.
      *
      * @param status the status code for the response.
      * @param body   the body of te response.
      */
     public Response(int status, T body) {
 
         this(status, Collections.<Header>emptySet(), body);
     }
 
     /**
      * Create a {@code Response} with the supplied HTTP status code, headers, and body.
      *
      * @param status  the status code for the response.
      * @param headers the headers for the response.
      * @param body    the body of te response.
      */
     public Response(int status, Collection<Header> headers, T body) {
 
         this(status, headers, Collections.<Cookie>emptySet(), body);
     }
 
     /**
      * Create a {@code Response} with the supplied HTTP status code, headers, and body.
      *
      * @param status  the status code for the response.
      * @param headers the headers for the response.
      * @param cookies the cookies for the response.
      * @param body    the body of te response.
      */
     public Response(int status, Collection<Header> headers, Collection<Cookie> cookies, T body) {
        super(SET_COOKIE, headers, new AttributeMap<Cookie>(cookies),
                 body);
 
         this.status = status;
     }
 
     /**
      * @return the status code for the response.
      */
     public int getStatus() {
 
         return status;
     }
 
     /**
      * Set the status code for the response.
      *
      * @param status the status code for the response.
      */
     public void setStatus(int status) {
 
         this.status = status;
     }
 }
