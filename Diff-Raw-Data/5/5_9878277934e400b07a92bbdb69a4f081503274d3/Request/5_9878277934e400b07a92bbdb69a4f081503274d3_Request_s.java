 package com.atlassian.sal.api.net;
 
 import com.atlassian.sal.api.net.auth.Authenticator;
 
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * Interface Request represents a request to retrieve data. To execute a request call {@link Request#execute(ResponseHandler)}.
  *
 * @param <T> the type of request used for method chaing
  * @since 2.0
  */
 public interface Request<T extends Request<?, ?>, R extends Response>
 {
     /**
      * Represents type of network request
      */
     public static enum MethodType
     {
         GET, POST, PUT, DELETE, HEAD, TRACE, OPTIONS
     }
 
     /**
      * Setting connection timeout in milliseconds.
      *
      * @param connectionTimeout The timeout in milliseconds
      * @return a reference to this object.
      */
     T setConnectionTimeout(int connectionTimeout);
 
     /**
      * Setting socket timeout in milliseconds.
      *
      * @param soTimeout the timeout in milliseconds
      * @return a reference to this object.
      */
     T setSoTimeout(int soTimeout);
 
     /**
      * @param url the url to request
      * @return a reference to this object.
      */
     T setUrl(String url);
 
     /**
      * Sets the body of the request. In default implementation only requests of type {@link MethodType#POST} and {@link MethodType#POST} can have request body.
      *
      * @param requestBody the body of the request
      * @return a reference to this object.
      */
     T setRequestBody(String requestBody);
 
     /**
      * Sets Content-Type of the body of the request. In default implementation only requests of type {@link MethodType#POST} and {@link MethodType#POST} can have request body.
      *
      * @param contentType the contentType of the request
      * @return a reference to this object.
      */
     T setRequestContentType(String contentType);
 
     /**
      * Sets parameters of the request. In default implementation only requests of type {@link MethodType#POST} and {@link MethodType#POST} can have parameters.
     * For other requests include parameters in url. This method accepts odd number of arguments, in form of name, value, name, value, name, value, etc
      *
      * @param params parameters of the request in as name, value pairs
      * @return a reference to this object.
      */
     T addRequestParameters(String... params);
 
     /**
      * Adds generic Authenticator to the request.
      *
      * @param authenticator the authenticator to use for authentication
      * @return a reference to this object.
      */
     T addAuthentication(Authenticator authenticator);
 
     /**
      * Adds TrustedTokenAuthentication to the request. Trusted token authenticator uses current user to make a trusted application call.
      *
      * @return a reference to this object.
      */
     T addTrustedTokenAuthentication();
 
     /**
      * Adds TrustedTokenAuthentication to the request. Trusted token authenticator uses the passed user to make a trusted application call.
      *
      * @param username The user to make the request with
      * @return this
      */
     T addTrustedTokenAuthentication(String username);
 
     /**
      * Adds basic authentication to the request.
      *
      * @param username The user name
      * @param password The password
      * @return a reference to this object.
      */
     T addBasicAuthentication(String username, String password);
 
     /**
      * Adds seraph authentication to the request.
      *
      * @param username The user name
      * @param password The password
      * @return a reference to this object.
      */
     T addSeraphAuthentication(String username, String password);
 
     /**
      * Adds the specified header to the request, not overwriting any previous value.
      * Support for this operation is optional.
      *
      * @param headerName  the header's name
      * @param headerValue the header's value
      * @return a reference to this object
      * @see RequestFactory#supportsHeader()
      */
     T addHeader(String headerName, String headerValue);
 
     /**
      * Sets the specified header to the request, overwriting any previous value.
      * Support for this operation is optional.
      *
      * @param headerName  the header's name
      * @param headerValue the header's value
      * @return a reference to this object
      * @see RequestFactory#supportsHeader()
      */
     T setHeader(String headerName, String headerValue);
 
     /**
      * @return an immutable Map of headers added to the request so far
      * @since 2.1
      */
     Map<String, List<String>> getHeaders();
 
     /**
      * Executes the request.
      *
      * @param responseHandler Callback handler of the response.
      * @throws ResponseException If the response cannot be retrieved
      */
     void execute(ResponseHandler<R> responseHandler) throws ResponseException;
 
     /**
      * Executes a request and if response is successful, returns response as a string. @see {@link Response#getResponseBodyAsString()}
      *
      * @return response as String
      * @throws ResponseException If the response cannot be retrieved
      */
     String execute() throws ResponseException;
 
 
 }
