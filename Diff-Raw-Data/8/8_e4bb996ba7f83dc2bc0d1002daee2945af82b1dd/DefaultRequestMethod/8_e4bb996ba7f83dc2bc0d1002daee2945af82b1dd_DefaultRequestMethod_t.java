 package fm.audiobox.configurations;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import java.util.Observable;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HTTP;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fm.audiobox.core.exceptions.AudioBoxException;
 import fm.audiobox.core.exceptions.LoginException;
 import fm.audiobox.core.exceptions.ServiceException;
 import fm.audiobox.core.models.User;
 import fm.audiobox.core.parsers.ResponseParser;
 import fm.audiobox.interfaces.IAuthenticationHandle;
 import fm.audiobox.interfaces.IConfiguration;
 import fm.audiobox.interfaces.IConnector.IConnectionMethod;
 import fm.audiobox.interfaces.IEntity;
 import fm.audiobox.interfaces.IResponseHandler;
 
 
 /**
  * This is the default connection method used for perfoming each request to AudioBox.fm server
  */
 public class DefaultRequestMethod extends Observable implements IConnectionMethod {
 
   private static Logger log = LoggerFactory.getLogger(DefaultRequestMethod.class);
 
   private volatile transient HttpClient connector;
   private volatile transient HttpRequestBase method;
   private volatile transient IEntity destEntity;
   private volatile transient IConfiguration configuration;
   private volatile transient Future<Response> futureResponse;
   private volatile transient IConfiguration.ContentFormat format;
   private volatile transient User user;
   
   private volatile transient boolean running = false;
   private volatile transient boolean aborted = false;
   private volatile transient IAuthenticationHandle authenticationHandle;
 
   public DefaultRequestMethod(){
     super();
   }
 
 
   public void init(IEntity destEntity, HttpRequestBase method, HttpClient connector, IConfiguration config, IConfiguration.ContentFormat format) {
     this.connector = connector;
     this.method = method;
     this.destEntity = destEntity;
     this.configuration = config;
     this.format = format;
   }
 
   
   public void setUser( User user ){
     this.user = user;
   }
   
   public User getUser() {
     return this.user;
   }
   
   public void setAuthenticationHandle(IAuthenticationHandle handle){
     this.authenticationHandle = handle;
   }
   
   public IAuthenticationHandle getAuthenticationHandle() {
     return this.authenticationHandle != null ? this.authenticationHandle : this.configuration.getAuthenticationHandle();
   }
   
   
   public void addHeader(String header, String value) {
     if ( value != null )
       this.method.addHeader(header, value);
     else
       this.method.removeHeaders(header);
   }
   
   public void addHeader(Header header) {
     this.addHeader( header.getName(), header.getValue() );
   }
 
   public Response send(boolean async) throws ServiceException, LoginException {
     return send(async, null,null);
   }
 
   public Response send(boolean async, List<NameValuePair> params) throws ServiceException, LoginException {
     HttpEntity entity = null;
     if (  (! isGET() && ! isDELETE() )  && params != null ){
       
       if ( log.isInfoEnabled() ) {
         StringBuffer sb = new StringBuffer();
         for ( NameValuePair param : params ){
           sb.append( "[" + param.getName() + ": '" + param.getValue() + "'], " );
         }
         log.info("Params: " + sb.toString() );
       }
       
       try {
         entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
       } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
       }
     }
     return send(async, entity);
   }
 
 
   public Response send(boolean async, HttpEntity params) throws ServiceException, LoginException {
     return send(async, params, null);
   }
 
 
   public synchronized Response send(boolean async, HttpEntity params, final IResponseHandler responseHandler) throws ServiceException, LoginException {
     if (   ( ! isGET() && ! isDELETE() )  && params != null ){
       ((HttpEntityEnclosingRequestBase) getHttpMethod() ).setEntity( params );
     }
     
     this.getAuthenticationHandle().handle( this );
     
     Callable<Response> start = new Callable<Response>() {
 
       public Response call() throws ServiceException, LoginException {
         Response response = null;
         try {
 
           DefaultRequestMethod.this.running = true;
           DefaultRequestMethod.this.aborted = false;
           
           String ecode = "";
           if ( isGET() && configuration.isCacheEnabled() ) {
             String url = getHttpMethod().getRequestLine().getUri();
             ecode = DefaultRequestMethod.this.configuration.getCacheManager().setup(destEntity, url, DefaultRequestMethod.this);
           }
           
           
           return connector.execute( getHttpMethod(), new ResponseParser( DefaultRequestMethod.this.configuration, DefaultRequestMethod.this, responseHandler, ecode), new BasicHttpContext() );
 
         } catch (ClientProtocolException e) {
           
           response = new Response( DefaultRequestMethod.this.format, AudioBoxException.GENERIC_ERROR, e.getMessage(), false );
           log.error("ClientProtocolException thrown while executing request method", e);
           response.setException( new ServiceException(e) );
 
         } catch (IOException e) {
           
           /*
            * An error occurred:
            * we have to catch the exception and cast it to a known custom exception object
            */
           
          log.error("An error occurred while executing request: " + getHttpMethod().getRequestLine().getUri(), e);
          
           response = new Response( DefaultRequestMethod.this.format, AudioBoxException.GENERIC_ERROR, e.getMessage(), false );
           AudioBoxException responseException = null;
           
           if ( e instanceof LoginException ) {
             // A login error occurred
             
             LoginException le = (LoginException) e;
             response = new Response( DefaultRequestMethod.this.format, le.getErrorCode(), le.getMessage(), false );
             responseException =  le;
             
           } else {
             
             // Generic error, we should catch the exception and cast it to a ServiceException
             
             if ( e instanceof ServiceException ) {
               
               ServiceException se = (ServiceException) e;
               response = new Response( DefaultRequestMethod.this.format, se.getErrorCode(), se.getMessage(), false );
               responseException =  se;
               
             } else {
               // This is a generic exception
               
               ServiceException se = new ServiceException( e );
               response = new Response( DefaultRequestMethod.this.format, se.getErrorCode(), se.getMessage(), false );
               responseException =  se;
               
             }
             
           }
           
           // Set the exception into Response (this value will be used in getResponse() method)
           response.setException( responseException );
           
         }
         return response;
       }
 
     };
 
     this.futureResponse = this.configuration.getExecutor().submit( start ); 
 
     return async ? null : this.getResponse();
   }
 
   public Response getResponse()  throws ServiceException, LoginException {
     try {
       Response response = this.futureResponse.get();
       
       this.running = false;
       this.aborted = false;
       
       if ( response != null ) {
         if ( response.getException() != null ) {
           AudioBoxException ex = response.getException();
           ex.setConfiguration( this.configuration );
           
           // try/catch block, in order to correctly throw the exception
           
           if ( ex instanceof LoginException ){
             throw (LoginException) ex;
           } else {
             throw (ServiceException) ex;
           }
           
         }
         return response;
       }
     } catch (InterruptedException e) {
       log.error("Request has been interrupted: " + getHttpMethod().getURI().toString() + " - " + e.getMessage() );
       throw new ServiceException(AudioBoxException.GENERIC_ERROR, "Interrupted: " + e.getMessage() );
     } catch ( CancellationException ce ) {
      log.error("Request has been cancelled: " + getHttpMethod().getRequestLine().getUri() );
       throw new ServiceException(AudioBoxException.GENERIC_ERROR, "Cancelled: " + ce.getMessage() );
     } catch (ExecutionException e) {
      log.error("An error occurred while executing request: " + getHttpMethod().getRequestLine().getUri(), e);
       throw new ServiceException(AudioBoxException.GENERIC_ERROR, "Execution error: " + e.getMessage() );
     }
     
     // A generic error occurred, throw a generic ServiceException
     throw new ServiceException(HttpStatus.SC_PRECONDITION_FAILED, "No response");
   }
 
   public void abort() {
     futureResponse.cancel(true);
     getHttpMethod().abort();
     this.aborted = true;
     this.running = false;
   }
 
 
   public HttpRequestBase getHttpMethod() {
     return this.method;
   }
 
   public IEntity getDestinationEntity() {
     return this.destEntity;
   }
 
 
   public boolean isRunning() {
     return this.running;
   }
   
   public boolean isAborted() {
     return this.aborted;
   }
   
   
   public boolean isGET() {
     return getHttpMethod().getMethod().equals( HttpGet.METHOD_NAME );
   }
 
   public boolean isPOST() {
     return getHttpMethod().getMethod().equals( HttpPost.METHOD_NAME );
   }
 
   public boolean isPUT() {
     return getHttpMethod().getMethod().equals( HttpPut.METHOD_NAME );
   }
 
   public boolean isDELETE() {
     return getHttpMethod().getMethod().equals( HttpDelete.METHOD_NAME );
   }
 
   public void setFollowRedirect(boolean followRedirect) {
     getHttpMethod().getParams().setBooleanParameter( ClientPNames.HANDLE_REDIRECTS, true );
   }
 
 }
