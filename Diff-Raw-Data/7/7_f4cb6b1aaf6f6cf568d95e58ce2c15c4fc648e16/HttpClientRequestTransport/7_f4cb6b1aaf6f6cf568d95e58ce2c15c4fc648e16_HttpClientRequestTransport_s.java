 /*
  *  soapUI, copyright (C) 2004-2011 eviware.com 
  *
  *  soapUI is free software; you can redistribute it and/or modify it under the 
  *  terms of version 2.1 of the GNU Lesser General Public License as published by 
  *  the Free Software Foundation.
  *
  *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU Lesser General Public License for more details at gnu.org.
  */
 
 package com.eviware.soapui.impl.wsdl.submit.transports.http;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.httpclient.URI;
 import org.apache.http.Header;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import com.eviware.soapui.SoapUI;
 import com.eviware.soapui.impl.support.AbstractHttpRequestInterface;
 import com.eviware.soapui.impl.support.http.HttpRequestInterface;
 import com.eviware.soapui.impl.wsdl.AbstractWsdlModelItem;
 import com.eviware.soapui.impl.wsdl.WsdlProject;
 import com.eviware.soapui.impl.wsdl.submit.RequestFilter;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.attachments.MimeMessageResponse;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedDeleteMethod;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedGetMethod;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedHeadMethod;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedOptionsMethod;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPatchMethod;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPostMethod;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedPutMethod;
 import com.eviware.soapui.impl.wsdl.submit.transports.http.support.methods.ExtendedTraceMethod;
 import com.eviware.soapui.impl.wsdl.support.PathUtils;
 import com.eviware.soapui.impl.wsdl.support.http.HttpClientSupport;
 import com.eviware.soapui.impl.wsdl.support.http.SoapUIHttpRoute;
 import com.eviware.soapui.impl.wsdl.support.wss.WssCrypto;
 import com.eviware.soapui.model.iface.Request;
 import com.eviware.soapui.model.iface.Response;
 import com.eviware.soapui.model.iface.SubmitContext;
 import com.eviware.soapui.model.propertyexpansion.PropertyExpander;
 import com.eviware.soapui.model.settings.Settings;
 import com.eviware.soapui.model.support.ModelSupport;
 import com.eviware.soapui.settings.HttpSettings;
 import com.eviware.soapui.support.types.StringToStringMap;
 import com.eviware.soapui.support.types.StringToStringsMap;
 
 import edu.umd.cs.findbugs.annotations.CheckForNull;
 
 /**
  * HTTP transport that uses HttpClient to send/receive SOAP messages
  * 
  * @author Ole.Matzura
  */
 
 public class HttpClientRequestTransport implements BaseHttpRequestTransport
 {
 	private List<RequestFilter> filters = new ArrayList<RequestFilter>();
 
 	public HttpClientRequestTransport()
 	{
 	}
 
 	public void addRequestFilter( RequestFilter filter )
 	{
 		filters.add( filter );
 	}
 
 	public void removeRequestFilter( RequestFilter filter )
 	{
 		filters.remove( filter );
 	}
 
 	public <T> void removeRequestFilter( Class<T> filterClass )
 	{
 		RequestFilter filter = findFilterByType( filterClass );
 
 		if( filter != null )
 		{
 			removeRequestFilter( filter );
 		}
 	}
 
 	public <T> void replaceRequestFilter( Class<T> filterClass, RequestFilter newFilter )
 	{
 		RequestFilter filter = findFilterByType( filterClass );
 
 		if( filter != null )
 		{
 			for( int i = 0; i < filters.size(); i++ )
 			{
 				RequestFilter oldFilter = filters.get( i );
 				if( oldFilter == filter )
 				{
 					filters.remove( i );
 					filters.add( i, newFilter );
 					break;
 				}
 			}
 		}
 	}
 
 	@CheckForNull
 	public <T extends Object> RequestFilter findFilterByType( Class<T> filterType )
 	{
 		for( int i = 0; i < filters.size(); i++ )
 		{
 			RequestFilter filter = filters.get( i );
 			if( filter.getClass() == filterType )
 			{
 				return filter;
 			}
 		}
 		return null;
 	}
 
 	public void abortRequest( SubmitContext submitContext )
 	{
 		HttpRequestBase postMethod = ( HttpRequestBase )submitContext.getProperty( HTTP_METHOD );
 		if( postMethod != null )
 			postMethod.abort();
 	}
 
 	public Response sendRequest( SubmitContext submitContext, Request request ) throws Exception
 	{
 		AbstractHttpRequestInterface<?> httpRequest = ( AbstractHttpRequestInterface<?> )request;
 
 		HttpClientSupport.SoapUIHttpClient httpClient = HttpClientSupport.getHttpClient();
 		ExtendedHttpMethod httpMethod = createHttpMethod( httpRequest );
 
 		boolean createdContext = false;
 		HttpContext httpContext = ( HttpContext )submitContext.getProperty( SubmitContext.HTTP_STATE_PROPERTY );
 		if( httpContext == null )
 		{
 			httpContext = new BasicHttpContext();
 			submitContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, httpContext );
 			createdContext = true;
 		}
 
 		String localAddress = System.getProperty( "soapui.bind.address", httpRequest.getBindAddress() );
 		if( localAddress == null || localAddress.trim().length() == 0 )
 			localAddress = SoapUI.getSettings().getString( HttpSettings.BIND_ADDRESS, null );
 
 		org.apache.http.HttpResponse httpResponse = null;
 		if( localAddress != null && localAddress.trim().length() > 0 )
 		{
 			try
 			{
 				httpMethod.getParams().setParameter( ConnRoutePNames.LOCAL_ADDRESS, InetAddress.getByName( localAddress ) );
 			}
 			catch( Exception e )
 			{
 				SoapUI.logError( e, "Failed to set localAddress to [" + localAddress + "]" );
 			}
 		}
 
 		submitContext.removeProperty( RESPONSE );
 		submitContext.setProperty( HTTP_METHOD, httpMethod );
 		submitContext.setProperty( POST_METHOD, httpMethod );
 		submitContext.setProperty( HTTP_CLIENT, httpClient );
 		submitContext.setProperty( REQUEST_CONTENT, httpRequest.getRequestContent() );
 		submitContext.setProperty( WSDL_REQUEST, httpRequest );
 		submitContext.setProperty( RESPONSE_PROPERTIES, new StringToStringMap() );
 
 		for( RequestFilter filter : filters )
 		{
 			filter.filterRequest( submitContext, httpRequest );
 		}
 
 		try
 		{
 			Settings settings = httpRequest.getSettings();
 
 			// custom http headers last so they can be overridden
 			StringToStringsMap headers = httpRequest.getRequestHeaders();
 
 			// first remove so we don't get any unwanted duplicates
 			for( String header : headers.keySet() )
 			{
 				httpMethod.removeHeaders( header );
 			}
 
 			// now add
 			for( String header : headers.keySet() )
 			{
 				for( String headerValue : headers.get( header ) )
 				{
 					headerValue = PropertyExpander.expandProperties( submitContext, headerValue );
 					httpMethod.addHeader( header, headerValue );
 				}
 			}
 
 			// do request
 			WsdlProject project = ( WsdlProject )ModelSupport.getModelItemProject( httpRequest );
 			WssCrypto crypto = null;
 			if( project != null && project.getWssContainer() != null )
 			{
 				crypto = project.getWssContainer().getCryptoByName(
 						PropertyExpander.expandProperties( submitContext, httpRequest.getSslKeystore() ) );
 			}
 
 			if( crypto != null && WssCrypto.STATUS_OK.equals( crypto.getStatus() ) )
 			{
 				httpMethod.getParams().setParameter( SoapUIHttpRoute.SOAPUI_SSL_CONFIG,
 						crypto.getSource() + " " + crypto.getPassword() );
 			}
 
 			// dump file?
 			httpMethod.setDumpFile( PathUtils.expandPath( httpRequest.getDumpFile(),
 					( AbstractWsdlModelItem<?> )httpRequest, submitContext ) );
 
 			// fix absolute URIs due to peculiarity in httpclient
 			URI uri = ( URI )submitContext.getProperty( BaseHttpRequestTransport.REQUEST_URI );
 			if( uri != null && uri.isAbsoluteURI() )
 			{
 				String str = uri.toString();
 				int ix = str.indexOf( '/', str.indexOf( "//" ) + 2 );
 				if( ix != -1 )
 				{
 					uri = new URI( str.substring( ix ), true );
 					java.net.URI oldUri = httpMethod.getURI();
 					httpMethod.setURI( new java.net.URI( oldUri.getScheme(), oldUri.getUserInfo(), oldUri.getHost(), oldUri
 							.getPort(), ( uri.getPath() ) == null ? "/" : uri.getPath(), oldUri.getQuery(), oldUri
 							.getFragment() ) );
 					submitContext.setProperty( BaseHttpRequestTransport.REQUEST_URI, uri );
 				}
 			}
 
 			// include request time?
 			if( settings.getBoolean( HttpSettings.INCLUDE_REQUEST_IN_TIME_TAKEN ) )
 				httpMethod.initStartTime();
 
 			httpMethod.getMetrics().getTotalTimer().start();
 			httpMethod.getMetrics().setHttpMethod( httpMethod.getMethod() );
 			httpMethod.getMetrics().setIpAddress( InetAddress.getByName( httpMethod.getURI().getHost() ).getHostAddress() );
 
 			// submit!
 			httpResponse = HttpClientSupport.execute( httpMethod, httpContext );
 
 			if( isRedirectResponse( httpResponse.getStatusLine().getStatusCode() ) && httpRequest.isFollowRedirects() )
 			{
 				ExtendedGetMethod returnMethod = followRedirects( httpClient, 0, httpMethod, httpResponse, httpContext );
 				httpMethod = returnMethod;
 				submitContext.setProperty( HTTP_METHOD, httpMethod );
 			}
 		}
 		catch( Throwable t )
 		{
 			httpMethod.setFailed( t );
 
 			if( t instanceof Exception )
 				throw ( Exception )t;
 
 			SoapUI.logError( t );
 			throw new Exception( t );
 		}
 		finally
 		{
 			if( !httpMethod.isFailed() )
 			{
 				if( httpMethod.getMetrics() != null )
 				{
 					httpMethod.getMetrics().getTotalTimer().stop();
 				}
 			}
 			else
 			{
 				httpMethod.getMetrics().reset();
 				httpMethod.getMetrics().setTimestamp( System.currentTimeMillis() );
 			}
 
 			for( int c = filters.size() - 1; c >= 0; c-- )
 			{
 				RequestFilter filter = filters.get( c );
 				filter.afterRequest( submitContext, httpRequest );
 			}
 
 			if( !submitContext.hasProperty( RESPONSE ) )
 			{
 				createDefaultResponse( submitContext, httpRequest, httpMethod );
 			}
 
 			Response response = ( Response )submitContext.getProperty( BaseHttpRequestTransport.RESPONSE );
 			StringToStringMap responseProperties = ( StringToStringMap )submitContext
 					.getProperty( BaseHttpRequestTransport.RESPONSE_PROPERTIES );
 
 			for( String key : responseProperties.keySet() )
 			{
 				response.setProperty( key, responseProperties.get( key ) );
 			}
 
 			if( createdContext )
 			{
 				submitContext.setProperty( SubmitContext.HTTP_STATE_PROPERTY, null );
 			}
 		}
 
 		return ( Response )submitContext.getProperty( BaseHttpRequestTransport.RESPONSE );
 	}
 
 	private boolean isRedirectResponse( int statusCode )
 	{
 		switch( statusCode )
 		{
 		case 301 :
 		case 302 :
 		case 303 :
 		case 307 :
 			return true;
 		}
 
 		return false;
 	}
 
 	private ExtendedGetMethod followRedirects( HttpClient httpClient, int redirectCount, ExtendedHttpMethod httpMethod,
 			org.apache.http.HttpResponse httpResponse, HttpContext httpContext ) throws Exception
 	{
 		ExtendedGetMethod getMethod = new ExtendedGetMethod();
 		for( Header header : httpMethod.getAllHeaders() )
 			getMethod.addHeader( header );
 
 		java.net.URI uri = new java.net.URI( httpResponse.getFirstHeader( "Location" ).getValue() );
 		getMethod.setURI( uri );
 		org.apache.http.HttpResponse response = HttpClientSupport.execute( getMethod, httpContext );
 
 		if( isRedirectResponse( response.getStatusLine().getStatusCode() ) )
 		{
 			if( redirectCount == 10 )
 				throw new Exception( "Maximum number of Redirects reached [10]" );
 
 			try
 			{
 				return followRedirects( httpClient, redirectCount + 1, getMethod, response, httpContext );
 			}
 			finally
 			{
 				//getMethod.releaseConnection();
 			}
 		}
 		else
 			return getMethod;
 
 	}
 
 	private void createDefaultResponse( SubmitContext submitContext, AbstractHttpRequestInterface<?> httpRequest,
 			ExtendedHttpMethod httpMethod )
 	{
 		String requestContent = ( String )submitContext.getProperty( BaseHttpRequestTransport.REQUEST_CONTENT );
 
 		// check content-type for multiplart
 		String responseContentTypeHeader = httpMethod.hasHttpResponse() ? httpMethod.getHttpResponse().getEntity()
 				.getContentType().toString() : null;
 		Response response = null;
 
 		if( responseContentTypeHeader != null && responseContentTypeHeader.toUpperCase().startsWith( "MULTIPART" ) )
 		{
 			response = new MimeMessageResponse( httpRequest, httpMethod, requestContent, submitContext );
 		}
 		else
 		{
 			response = new SinglePartHttpResponse( httpRequest, httpMethod, requestContent, submitContext );
 		}
 
 		submitContext.setProperty( BaseHttpRequestTransport.RESPONSE, response );
 	}
 
 	private ExtendedHttpMethod createHttpMethod( AbstractHttpRequestInterface<?> httpRequest )
 	{
 		if( httpRequest instanceof HttpRequestInterface<?> )
 		{
 			HttpRequestInterface<?> restRequest = ( HttpRequestInterface<?> )httpRequest;
 			switch( restRequest.getMethod() )
 			{
 			case GET :
 				return new ExtendedGetMethod();
 			case HEAD :
 				return new ExtendedHeadMethod();
 			case DELETE :
 				return new ExtendedDeleteMethod();
 			case PUT :
 				return new ExtendedPutMethod();
 			case OPTIONS :
 				return new ExtendedOptionsMethod();
 			case TRACE :
 				return new ExtendedTraceMethod();
 			case PATCH :
 				return new ExtendedPatchMethod();
 			}
 		}
 
 		ExtendedPostMethod extendedPostMethod = new ExtendedPostMethod();
 
 		extendedPostMethod.setAfterRequestInjection( httpRequest.getAfterRequestInjection() );
 		return extendedPostMethod;
 	}
 }
