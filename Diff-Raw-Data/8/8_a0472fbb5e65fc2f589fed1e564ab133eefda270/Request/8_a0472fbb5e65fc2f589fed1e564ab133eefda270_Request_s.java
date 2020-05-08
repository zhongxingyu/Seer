 package ch.rollis.emma.request;
 
 import java.net.URI;
 import java.util.HashMap;
 
 
 /**
  * Class represents a HTTP request.
  * <p>
  * Supported header fields are denoted in class constructor.
  * 
  * @author mrolli
  */
 public class Request {
     /**
      * Protocol version ("i.e HTTP/1.0").
      */
     private String protocol;
 
     /**
      * HTTP method.
      */
     private HttpMethod method;
 
     /**
      * Request URI.
      */
     private URI requestURI;
 
     /**
      * Entity data of request.
      */
     private String entity;
 
     /**
      * Map consisting of general headers.
      */
     private final HashMap<String, String> headersGeneral;
 
     /**
      * Map consisting of request headers.
      */
     private final HashMap<String, String> headersRequest;
 
     /**
      * Map consisting of entity headers.
      */
     private final HashMap<String, String> headersEntity;
 
     /**
      * Port number the request arrived.
      */
     private int port;
 
     /**
      * True if request was in SSL context.
      */
     private boolean sslSecured = false;
 
     /**
      * Class constructor generates a vanilla request.
      */
     public Request() {
         headersGeneral = new HashMap<String, String>();
         headersGeneral.put("DATE", null);
         headersGeneral.put("PRAGMA", null);
 
         headersRequest = new HashMap<String, String>();
         headersRequest.put("HOST", null);
         headersRequest.put("AUTHORIZATION", null);
         headersRequest.put("FROM", null);
         headersRequest.put("IF-MODIFIED-SINCE", null);
         headersRequest.put("REFERER", null);
         headersRequest.put("USER-AGENT", null);
 
         headersEntity = new HashMap<String, String>();
         headersEntity.put("ALLOW", null);
         headersEntity.put("CONTENT-ENCODING", null);
         headersEntity.put("CONTENT-LENGTH", null);
         headersEntity.put("CONTENT-TYPE", null);
         headersEntity.put("EXPIRES", null);
         headersEntity.put("LAST-MODIFIED", null);
     }
 
     /**
      * Returns the protocol of the request.
      * 
      * @return The protocol version
      */
     public String getProtocol() {
         return protocol;
     }
 
     /**
      * Sets the protocol version.
      * 
      * @param proto
      *            The protocol version of the request
      */
     void setProtocol(final String proto) {
         this.protocol = proto;
     }
 
     /**
      * Returns the method type.
      * 
      * @return the method
      */
     public HttpMethod getMethod() {
         return method;
     }
 
     /**
      * Sets the http method.
      * 
      * @param httpMethod
      *            the method to set
      */
     void setMethod(final HttpMethod httpMethod) {
         method = httpMethod;
     }
 
     /**
      * Check if request is a GET request.
      * <p>
      * This is a convenience method.
      * 
      * @return true if request is GET, false otherwise
      */
     public boolean isGet() {
         return method.equals(HttpMethod.GET);
     }
 
     /**
      * Check if request is a POST request.
      * <p>
      * This is a convenience method.
      * 
      * @return true if request is POST, false otherwise
      */
     public boolean isPost() {
         return method.equals(HttpMethod.POST);
     }
 
     /**
      * Check if request is a HEAD request.
      * <p>
      * This is a convenience method.
      * 
      * @return true if request is HEAD, false otherwise
      */
     public boolean isHead() {
         return method.equals(HttpMethod.HEAD);
     }
 
     /**
      * Check if request is a PUT request.
      * <p>
      * This is a convenience method.
      * 
      * @return true if request is PUT, false otherwise
      */
     public boolean isPut() {
         return method.equals(HttpMethod.PUT);
     }
 
     /**
      * Check if request is a DELETE request.
      * <p>
      * This is a convenience method.
      * 
      * @return true if request is DELETE, false otherwise
      */
     public boolean isDelete() {
         return method.equals(HttpMethod.DELETE);
     }
 
     /**
      * Returns the request URI.
      * 
      * @return the requestURI
      */
     public URI getRequestURI() {
         return requestURI;
     }
 
     /**
      * Sets the request URI.
      * 
      * @param uri
      *            the requestURI to set
      */
     void setRequestURI(final URI uri) {
         this.requestURI = uri;
     }
 
     /**
      * Checks if request is a full request.
      * <p>
      * This is a convenience method.
      * 
      * @return true if request is a full request; false otherwise
      */
     public boolean isFullRequest() {
         return !"HTTP/0.9".equals(getProtocol());
     }
 
     /**
      * Checks if request is a simple request.
      * <p>
      * This is a convenience method.
      * 
     * @see isFullRequst()
      * @return true if request is a simple request; false otherwise
      */
     public boolean isSimpleRequest() {
         return !isFullRequest();
     }
 
     /**
      * Returns the value of a general header field.
      * 
      * @param headerField
      *            Header field to retrieve the value for
      * @return Value of header field
      */
     public String getHeader(final String headerField) {
         String key = headerField.toUpperCase();
         if (headersGeneral.containsKey(key)) {
             return headersGeneral.get(key);
         } else if (headersRequest.containsKey(key)) {
             return headersRequest.get(key);
         } else if (headersEntity.containsKey(key)) {
             return headersEntity.get(key);
         }
         return null;
     }
 
     /**
      * Set the value of a header field to the request.
      * 
      * Values for unknown header fields are stored as entity headers, see
      * rfc1945.
      * 
      * @param headerField
      *            Header field
      * @param value
      *            Value of header field
      */
     void setHeader(final String headerField, final String value) {
         String key = headerField.toUpperCase();
         if (headersGeneral.containsKey(key)) {
             headersGeneral.put(key, value);
         } else if (headersRequest.containsKey(key)) {
             headersRequest.put(key, value);
         } else {
             headersEntity.put(key, value);
         }
     }
 
     /**
      * Returns the entity data received in request.
      * <p>
      * If the request did not send any entity data, a null reference is returned
      * instead.
      * 
      * @return The entity data or a null reference
      */
     public String getEntity() {
         return entity;
     }
 
     /**
      * Sets the request entity.
      * 
      * @param entityData
      *            String received as entity data
      */
     void setEntity(final String entityData) {
         entity = entityData;
     }
 
     /**
      * Returns ture if request was SSL secured.
      * 
      * @return True if request was SSL secured
      */
     public boolean isSslSecured() {
         return sslSecured;
     }
 
     /**
      * Sets the flag that denotes if request was received in SSL context.
      * 
      * @param flag
      *            Denotes if SSL secured request
      */
     public void setIsSslSecured(final boolean flag) {
         sslSecured = flag;
     }
 
     /**
      * Returns the port on which the request was received.
      * 
      * @return the port
      */
     public int getPort() {
         return port;
     }
 
     /**
      * Sets the port the request was received on.
      * 
      * @param portNumber
      *            the port to set
      */
     public void setPort(final int portNumber) {
         port = portNumber;
     }
 }
 
