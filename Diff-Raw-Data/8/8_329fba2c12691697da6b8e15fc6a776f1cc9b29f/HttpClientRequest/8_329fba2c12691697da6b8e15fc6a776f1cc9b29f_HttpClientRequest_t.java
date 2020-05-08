 package com.atlassian.sal.core.net;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpConnectionManager;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.methods.DeleteMethod;
 import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.HeadMethod;
 import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
 import org.apache.commons.httpclient.methods.OptionsMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.TraceMethod;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.sal.api.net.Request;
 import com.atlassian.sal.api.net.Response;
 import com.atlassian.sal.api.net.ResponseException;
 import com.atlassian.sal.api.net.ResponseHandler;
 import com.atlassian.sal.api.net.auth.Authenticator;
 import com.atlassian.sal.api.user.UserManager;
 import com.atlassian.sal.core.net.auth.BaseAuthenticator;
 import com.atlassian.sal.core.net.auth.HttpClientAuthenticator;
 import com.atlassian.sal.core.net.auth.SeraphAuthenticator;
 import com.atlassian.sal.core.net.auth.TrustedTokenAuthenticator;
 
 /**
  *	HttpClient implementation of Request interface
  */
 public class HttpClientRequest implements Request<HttpClientRequest>
 {
 	private static final Logger log = Logger.getLogger(HttpClientRequest.class);
 
 	public static final int MAX_ATTEMPTS = 2;
 	public static final int MAX_REDIRECTS = 3;
 	
 	private final Request.MethodType methodType;
 	private String url;
 	private final Map<String, List<String>> parameters = new HashMap<String, List<String>>();
 	private final List<HttpClientAuthenticator> authenticators = new ArrayList<HttpClientAuthenticator>();
 
 	private final HttpClient httpClient;
 	private String requestBody;
 	private String requestContentType;
 
 	public HttpClientRequest(HttpClient httpClient, MethodType methodType, String url)
 	{
 		this.httpClient = httpClient;
 		this.methodType = methodType;
 		this.url = url;
 	}
 
 	public HttpClientRequest setUrl(String url)
 	{
 		this.url = url;
 		return this;
 	}
 
 	// ------------------------ authenticators -------------------------------------------
 	public HttpClientRequest addAuthentication(Authenticator authenticator)
 	{
 		if (authenticator instanceof HttpClientAuthenticator)
 			this.authenticators.add((HttpClientAuthenticator) authenticator);
 		else
 			log.warn("Authenticator '"+authenticator+"'is not instance of " + HttpClientAuthenticator.class.getName());
 		return this;
 	}
 	
 	public HttpClientRequest addTrustedTokenAuthentication()
 	{
 		final UserManager userManager = ComponentLocator.getComponent(UserManager.class);
 		final TrustedTokenAuthenticator trustedTokenAuthenticator = new TrustedTokenAuthenticator(userManager.getRemoteUsername());
 		this.authenticators.add(trustedTokenAuthenticator);
 		return this;
 	}
 	
 	public HttpClientRequest addBasicAuthentication(String username, String password)
 	{
 		this.authenticators.add(new BaseAuthenticator(username, password));
 		return this;
 	}
 	
 	public HttpClientRequest addSeraphAuthentication(String username, String password)
 	{
 		this.authenticators.add(new SeraphAuthenticator(username, password));
 		return this;
 	}
 	
 	// ------------------------ various setters -------------------------------------------
 
 	public HttpClientRequest setConnectionTimeout(int connectionTimeout)
 	{
 		final HttpConnectionManagerParams params = httpClient.getHttpConnectionManager().getParams();
 		params.setConnectionTimeout(connectionTimeout);
 		return this;
 	}
 	
 	public HttpClientRequest setSoTimeout(int soTimeout)
 	{
 		final HttpConnectionManagerParams params = httpClient.getHttpConnectionManager().getParams();
 		params.setSoTimeout(soTimeout);
 		return this;
 	}
 	
 	public HttpClientRequest setRequestBody(String requestBody)
 	{
 		this.requestBody = requestBody;
 		if (methodType != MethodType.POST && methodType != MethodType.PUT)
 		{
 			throw new IllegalArgumentException("Only POST and PUT methods can have request body");
 		} 
 		return this;
 	}
 	
 	public HttpClientRequest setRequestContentType(String requestContentType)
 	{
 		this.requestContentType = requestContentType;
 		return this;
 	}
 	
 	public HttpClientRequest addRequestParameters(String... params)
 	{
 		if (params.length%2!=0)
 		{
 			throw new IllegalArgumentException("You must enter even number of arguments");
 		}
 		
 		if (methodType != MethodType.POST && methodType != MethodType.PUT)
 		{
 			throw new IllegalArgumentException("Only POST and PUT methods accept req");
 		} 
 		
 		for (int i = 0; i < params.length; i+=2)
 		{
 			final String name = params[i];
 			final String value = params[i+1];
 			List<String> list = parameters.get(name);
 			if (list==null)
 			{
 				list = new ArrayList<String>();
 				parameters.put(name, list);
 			}
 			list.add(value);
 		}
 		return this;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.atlassian.sal.api.net.Request#execute()
 	 */
 	public void execute(ResponseHandler responseHandler) throws ResponseException
 	{
 		Throwable lastException = null;
 		for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++)
 		{
 			final HttpMethod method = makeMethod();
 			processAuthenticator(method);
 			processParameters(method);
 			if (log.isDebugEnabled())
 			{
 				
 				final Header[] requestHeaders = method.getRequestHeaders();
 				log.debug("Calling " + method.getName() + " " +  this.url + " with headers " + (requestHeaders==null?"none":Arrays.asList(requestHeaders).toString()));
 			}
 			method.setRequestHeader("Connection", "close");
 			try
 			{
 				executeMethod(method, 0);
 				responseHandler.handle(new HttpClientResponse(method));
 				return;	// success lets get out of here
 			} catch (final RetryAgainException e)
 			{
 				// this exception occurs during executeMethod(). keep retrying.
 				lastException = e.getCause();
 				log.debug(e,e);
 			} catch (final ResponseException e)
 			{
 				// this exception occurs during responseHandler.handle(). throw it all the way out.
 				// this catch block is here only to improve readability of the code
 				throw e;
 			}
 			finally
 			{
 				exhaustResponseContents(method);
 				method.releaseConnection();
 				// see https://extranet.atlassian.com/display/~doflynn/2008/05/19/HttpClient+leaks+sockets+into+CLOSE_WAIT
 				final HttpConnectionManager httpConnectionManager = httpClient.getHttpConnectionManager();
 				if (httpConnectionManager!=null)
 					httpConnectionManager.closeIdleConnections(0);
 			}
 		}
 		throw new ResponseException("Maximum number of retries ("+MAX_ATTEMPTS+") reached.", lastException);
 	}
 
     private static void exhaustResponseContents(HttpMethod response)
     {
         InputStream body = null;
         try
         {
             body = response.getResponseBodyAsStream();
             if (body==null)
             {
             	return;
             }
             final byte[] buf = new byte[512];
             @SuppressWarnings("unused")
 			int bytesRead = 0;
             while ((bytesRead = body.read(buf)) != -1)
             {
                 // throw the bytes away! :)
             }
         }
         catch (final IOException e)
         {
             // Ignore, we're already done with the response anyway.
         }
         finally
         {
             shutdownStream(body);
         }
     }
     
     /**
      * Unconditionally close an <code>InputStream</code>.
      * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
      * @param input A (possibly null) InputStream
      */
     public static void shutdownStream( final InputStream input )
     {
         if( null == input )
         {
             return;
         }
 
         try
         {
             input.close();
         }
         catch( final IOException ioe )
         {
         }
     }    
     
 
 	public String execute() throws ResponseException
 	{
 		final Set<String> stringHolder = new HashSet<String>();
 		execute(new ResponseHandler()
 		{
 			public void handle(Response response) throws ResponseException
 			{
 				if (!response.isSuccessful())
 				{
 					throw new ResponseException("Unexpected response received. Status code: " + response.getStatusCode());
 				}
 				stringHolder.add(response.getResponseBodyAsString());
 			}
 		});
 		
 		return stringHolder.isEmpty()?"":stringHolder.iterator().next();
 	}
 	// ------------------------------------------------------------------------------------------------------------------------------------
 	// -------------------------------------------------- private methods ------------------------------------------------------------------
 	// ------------------------------------------------------------------------------------------------------------------------------------
 	
 	private class RetryAgainException extends Exception
 	{
 		public RetryAgainException(Exception e)
 		{
 			super(e);
 		}
 	}
 
 	protected HttpMethod makeMethod()
 	{
 		final HttpMethod method;
 		switch (methodType)
 		{
 		case POST:
 			method = new PostMethod(url);
 			break;
 		case PUT:
 			method = new PutMethod(url);
 			break;
 		case DELETE:
 			method = new DeleteMethod(url);
 			break;
 		case OPTIONS:
 			method = new OptionsMethod(url);
 			break;
 		case HEAD:
 			method = new HeadMethod(url);
 			break;
 		case TRACE:
 			method = new TraceMethod(url);
 			break;
 		default:
 			method = new GetMethod(url);
 		break;
 		}
 		return method;
 	}
 	
 	private void executeMethod(final HttpMethod method, int redirectCounter) throws RetryAgainException
 	{
 		try
 		{
 			if (++redirectCounter>MAX_REDIRECTS)
 				throw new IOException("Maximum number of redirects ("+MAX_REDIRECTS+") reached.");
 			
 			// execute the method.
 			final int statusCode = httpClient.executeMethod(method);
 			
 			if (statusCode >= 300 && statusCode <= 399)
 			{
 				String redirectLocation;
 				final Header locationHeader = method.getResponseHeader("location");
 				if (locationHeader != null)
 				{
 					redirectLocation = locationHeader.getValue();
 					method.setURI(new URI(redirectLocation, true));
 					executeMethod(method, redirectCounter);
 				}
 				else
 				{
 					// The response is invalid and did not provide the new location for
 					// the resource.  Report an error or possibly handle the response
 					// like a 404 Not Found error.
 					throw new IOException("HTTP response returned redirect code " + statusCode + " but did not provide a location header");
 				}
 			}
 		} catch (final URIException e)
 		{
 			throw new RetryAgainException(e);
 		} catch (final HttpException e)
 		{
 			throw new RetryAgainException(e);
 		} catch (final NullPointerException e)
 		{
 			throw new RetryAgainException(e);
 		} catch (final IOException e)
 		{
 			throw new RetryAgainException(e);
 		}
 	}
 	
 	private void processParameters(final HttpMethod method)
 	{
 		if (!(method instanceof EntityEnclosingMethod))
 		{
 			return;	// only POST and PUT method can apply
 		}
 		// Add post parameters
 		if ((method instanceof PostMethod) && !this.parameters.isEmpty())
 		{
 			final PostMethod postMethod = (PostMethod) method;
 			postMethod.setRequestHeader("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
 			for (final String parameterName : this.parameters.keySet())
 			{
 				for (final String parameterValue : this.parameters.get(parameterName))
 				{
 					postMethod.addParameter(parameterName, parameterValue);
 				}
 			}
 			return;
 		}
 		
 		// Set request body
 		if ((method instanceof EntityEnclosingMethod) && this.requestBody!=null)
 		{
 			final EntityEnclosingMethod entityEnclosingMethod = (EntityEnclosingMethod) method;
 			final String contentType = requestContentType + "; charset=UTF-8";
 			ByteArrayInputStream inputStream;
 			try
 			{
 				inputStream = new ByteArrayInputStream(requestBody.getBytes("UTF-8"));
 			} catch (final UnsupportedEncodingException e)
 			{
 				throw new RuntimeException(e);
 			}
 			entityEnclosingMethod.setRequestEntity(new InputStreamRequestEntity(inputStream, contentType));
 			
 		}
 	}
 	
 	private void processAuthenticator(final HttpMethod method)
 	{
 		for (final HttpClientAuthenticator authenticator : authenticators)
 		{
 			authenticator.process(httpClient, method);
 		}
 	}
 
	
	@Override
	public String toString()
	{
		return methodType + " " + url+", Parameters: " + parameters + (StringUtils.isBlank(requestBody)?"":"\nRequest body:\n" + requestBody);
	}
 }
