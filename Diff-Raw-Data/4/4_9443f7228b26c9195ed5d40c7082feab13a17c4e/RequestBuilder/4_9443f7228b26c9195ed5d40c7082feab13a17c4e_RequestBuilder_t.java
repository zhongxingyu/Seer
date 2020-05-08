 package com.mattbertolini.camclient.request;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Map;
 
 import com.mattbertolini.camclient.net.Parameter;
 
 /**
  * Translates a CamRequest object into a properly formatted HTTP POST request 
  * string.
  * 
  * @author Matt Bertolini
  */
 public class RequestBuilder {
     private static final char NAME_VALUE_SEPARATOR = '=';
     private static final char PARAMETER_SEPARATOR = '&';
     private static final String UTF_8 = "UTF-8";
     private static final String EMPTY_STRING = "";
 
     /**
      * Builds a HTTP POST request string from the given CamRequest object. All 
      * keys and values are URL encoded. If a request value is null, an empty 
      * string is substituted.
      * 
      * @param request The CamRequest object to build a request string for.
      * @return A property formatted string suitable for an HTTP POST request.
      * @throws IllegalArgumentException If the request object is null.
      * @throws IllegalStateException If the request object is not in a complete 
      * state (i.e. Operation is null).
      */
     public String buildRequest(CamRequest request) {
         if(request == null) {
             throw new IllegalArgumentException("Request object cannot be null.");
         }
         if(request.getOperation() == null) {
             throw new IllegalStateException("Operation needs to be set to a non-null value before request can be built.");
         }
         StringBuilder req = new StringBuilder();
         req.append(this.encode(RequestParameter.OPERATION.getName()));
         req.append(NAME_VALUE_SEPARATOR);
         req.append(this.encode(request.getOperation().getName()));
 
         for(Map.Entry<Parameter, String> parameter : request.getParameters().entrySet()) {
             req.append(PARAMETER_SEPARATOR);
             req.append(this.encode(parameter.getKey().getName()));
             req.append(NAME_VALUE_SEPARATOR);
             if(parameter.getValue() == null) {
                 req.append(this.encode(EMPTY_STRING));
             } else {
                 req.append(this.encode(parameter.getValue()));
             }
         }
         return req.toString();
     }
 
     public String appendUserNameAndPassword(String username, String password) {
         if(username == null) {
             throw new IllegalArgumentException("Username cannot be null.");
         }
         if(password == null) {
             throw new IllegalArgumentException("Password cannot be null.");
         }
         StringBuilder sb = new StringBuilder();
         sb.append(PARAMETER_SEPARATOR);
         sb.append(this.encode(RequestParameter.ADMIN_USERNAME.getName()));
         sb.append(NAME_VALUE_SEPARATOR);
         sb.append(this.encode(username));
         sb.append(PARAMETER_SEPARATOR);
         sb.append(this.encode(RequestParameter.ADMIN_PASSWORD.getName()));
         sb.append(NAME_VALUE_SEPARATOR);
         sb.append(this.encode(password));
         return sb.toString();
     }
 
     /**
      * Encodes the given string into an
      * <code>application/x-www-form-urlencoded</code> format using UTF-8
      * encoding. This is a simple utility method so we don't have to repeatedly
      * call URLEncoder's encode method specifying the format each time.
      * 
      * @param s
      *            The string to encode
      * @return Returns a UTF-8 encoded string.
     * @throws RuntimeException If the UTF-8 charset is not found.
      * @see {@link URLEncoder#encode(String, String)}
      */
     private String encode(String s) {
         String ec = null;
         try {
             ec = URLEncoder.encode(s, UTF_8);
         } catch(UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 charset not found.", e);
         }
         return ec;
     }
 }
