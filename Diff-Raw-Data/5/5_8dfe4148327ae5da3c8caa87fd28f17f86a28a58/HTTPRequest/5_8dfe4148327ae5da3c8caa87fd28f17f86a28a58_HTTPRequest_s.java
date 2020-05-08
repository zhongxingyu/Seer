 // Copyright (C) 2001, 2002 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.plugin.http;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 
 import HTTPClient.HTTPConnection;
 import HTTPClient.HTTPResponse;
 import HTTPClient.ModuleException;
 import HTTPClient.NVPair;
 import HTTPClient.ParseException;
 import HTTPClient.ProtocolNotSuppException;
 import HTTPClient.URI;
 
 import net.grinder.common.GrinderException;
 import net.grinder.common.Logger;
 import net.grinder.engine.process.PluginRegistry;
 import net.grinder.plugininterface.PluginException;
 import net.grinder.plugininterface.PluginProcessContext;
 
 
 /**
  * An individual HTTP request for use in scripts.
  *
  * <p>Scripts can set default values for the URL, headers, and data.
  * There are several overloaded methods corresponding to each HTTP
  * method (GET, POST, ...) that allow specific values to override the
  * defaults.</p>
  *
  * @author Philip Aston
  * @version $Revision$
  */ 
 public class HTTPRequest
 {
     private static final PluginProcessContext s_processContext;
 
     static
     {
 	try {
 	    s_processContext =
 		PluginRegistry.getInstance().register(HTTPPlugin.class);
 	}
 	catch (GrinderException e) {
 	    throw new RuntimeException("Failed to register HTTPPlugin: " +
 				       e.getMessage());
 	}
     }
 
     private URI m_defaultURL;
     private NVPair[] m_defaultHeaders = new NVPair[0];
     private byte[] m_defaultData;
     private NVPair[] m_defaultFormData;
 
     /**
      * Creates a new <code>HTTPRequest</code> instance.
      */
     public HTTPRequest()
     {
     }
 
     /**
      * Gets the default URL.
      *
      * @return The default URL to be used for this request.
      */
     public final String getUrl()
     {
 	return m_defaultURL.toString();
     }
 
     /**
      * Sets the default URL. The value given must be an absolut URL,
      * including protoocl and the server information.
      *
      * @param url The URL to be used for this request.
      * @exception ParseException If the URL cannot be parsed.
      * @exception PluginException If the URL is not absolute.
      */
     public final void setUrl(String url) throws ParseException, PluginException
     {
 	if (!isAbsolute(url)) {
 	    throw new PluginException("URL must be absolute");
 	}
 
 	m_defaultURL = new URI(url);
     }
 
     /**
      * Gets the default headers.
      *
      * @return The default headers to be used for this request.
      */
     public final NVPair[] getHeaders() 
     {
 	return m_defaultHeaders;
     }
 
     /**
      * Sets the default headers.
      *
      * @param headers The default headers to be used for this request.
      */
     public final void setHeaders(NVPair[] headers)
     {
 	m_defaultHeaders = headers;
     }
 
     /**
      * Gets the default data.
      *
      * @return The default data to be used for this request.
      */
     public final byte[] getData() 
     {
 	return m_defaultData;
     }
 
     /**
      * Sets the default data.
      *
      * @param data The default data to be used for this request.
      */
     public final void setData(byte[] data)
     {
 	m_defaultData = data;
     }
 
     /**
      * Gets the default form data.
      *
      * @return The default form or query data to be used for this
      * request.
      */
     public final NVPair[] getFormData() 
     {
 	return m_defaultFormData;
     }
 
     /**
      * Sets the default form data.
      *
      * @param formData The default form or query data to be used for
      * this request.
      */
     public final void setFormData(NVPair[] formData)
     {
 	m_defaultFormData = formData;
     }
 
     /**
      * Makes an HTTP <code>DELETE</code> request.
      *
      * @return Contains details of the servers response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse DELETE() throws Exception
     {
 	return DELETE(null, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>DELETE</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse DELETE(String uri) throws Exception
     {
 	return DELETE(uri, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>DELETE</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse DELETE(final String uri, final NVPair[] headers)
 	throws Exception
     {
 	final RequestState requestState = new RequestState(uri);
 
 	return requestState.processResponse(
 	    requestState.getConnection().Delete(
 		requestState.getPath(), headers));
     }
 
     /**
      * Makes an HTTP <code>GET</code> request.
      *
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse GET() throws Exception
     {
 	return GET(null, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>GET</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse GET(String uri) throws Exception
     {
	return GET(uri, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>GET</code> request.
      *
      * @param queryData Request headers. Replaces all the values set
      * by {@link #setFormData}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse GET(NVPair[] queryData) throws Exception
     {
 	return GET(null, queryData, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>GET</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param queryData Request headers. Replaces all the values set
      * by {@link #setFormData}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse GET(final String uri, final NVPair[] queryData)
 	throws Exception
     {
 	return GET(uri, queryData, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>GET</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param queryData Request headers. Replaces all the
      * values set by {@link #setFormData}.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse GET(final String uri,
 				  final NVPair[] queryData,
 				  final NVPair[] headers) throws Exception
     {
 	final RequestState requestState = new RequestState(uri);
 
 	return requestState.processResponse(
 	    requestState.getConnection().Get(
 		requestState.getPath(), queryData, headers));
     }
 
     /**
      * Makes an HTTP <code>HEAD</code> request.
      *
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse HEAD() throws Exception
     {
	return HEAD(null, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>HEAD</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse HEAD(String uri) throws Exception
     {
 	return HEAD(uri, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>HEAD</code> request.
      *
      * @param queryData Request headers. Replaces all the values set
      * by {@link #setFormData}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse HEAD(NVPair[] queryData) throws Exception
     {
 	return HEAD(null, queryData, getHeaders());
     }
 
 
     /**
      * Makes an HTTP <code>HEAD</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param queryData Request headers. Replaces all the values set
      * by {@link #setFormData}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse HEAD(final String uri, final NVPair[] queryData)
 	throws Exception
     {
 	return HEAD(uri, queryData, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>HEAD</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param queryData Request headers. Replaces all the values set
      * by {@link #setFormData}.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse HEAD(final String uri,
 				   final NVPair[] queryData,
 				   final NVPair[] headers) throws Exception
     {
 	final RequestState requestState = new RequestState(uri);
 
 	return requestState.processResponse(
 	    requestState.getConnection().Head(
 		requestState.getPath(), queryData, headers));
     }
 
     /**
      * Makes an HTTP <code>OPTIONS</code> request.
      *
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse OPTIONS() throws Exception
     {
 	return OPTIONS(null, getHeaders(), getData());
     }
 
     /**
      * Makes an HTTP <code>OPTIONS</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse OPTIONS(String uri) throws Exception
     {
 	return OPTIONS(uri, getHeaders(), getData());
     }
 
     /**
      * Makes an HTTP <code>OPTIONS</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse OPTIONS(final String uri, final NVPair[] headers)
 	throws Exception
     {
 	return OPTIONS(uri, headers, getData());
     }
 
     /**
      * Makes an HTTP <code>OPTIONS</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param data Data to be submitted in the body of the request.
      * Overrides the value set with {@link #setData}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse OPTIONS(final String uri, final byte[] data)
 	throws Exception
     {
 	return OPTIONS(uri, getHeaders(), data);
     }
 
     /**
      * Makes an HTTP <code>OPTIONS</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @param data Data to be submitted in the body of the request.
      * Overrides the value set with {@link #setData}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse OPTIONS(final String uri,
 				      final NVPair[] headers,
 				      final byte[] data) throws Exception
     {
 	final RequestState requestState = new RequestState(uri);
 
 	return requestState.processResponse(
 	    requestState.getConnection().Options(
 		requestState.getPath(), headers, data));
     }
 
     /**
      * Makes an HTTP <code>POST</code> request.
      *
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse POST() throws Exception
     {
 	return POST((String)null);
     }
 
     /**
      * Makes an HTTP <code>POST</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse POST(String uri) throws Exception
     {
 	final byte[] data = getData();
 
 	if (data != null) {
 	    return POST(uri, data, getHeaders());
 	}
 	else {
 	    return POST(uri, getFormData(), getHeaders());
 	}
     }
 
     /**
      * Makes an HTTP <code>POST</code> request.
      *
      * @param formData Data to be submitted as an
      * <code>application/x-www-form-urlencoded</code> encoded request
      * body.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse POST(NVPair[] formData) throws Exception
     {
 	return POST(null, formData, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>POST</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param formData Data to be submitted as an
      * <code>application/x-www-form-urlencoded</code> encoded request
      * body.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse POST(String uri, NVPair[] formData)
 	throws Exception
     {
 	return POST(uri, formData, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>POST</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param formData Data to be submitted as an
      * <code>application/x-www-form-urlencoded</code> encoded request
      * body.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse POST(final String uri,
 				   final NVPair[] formData,
 				   final NVPair[] headers) throws Exception
     {
 	final RequestState requestState = new RequestState(uri);
 
 	return requestState.processResponse(
 	    requestState.getConnection().Post(
 		requestState.getPath(), formData, headers));
     }
 
     /**
      * Makes an HTTP <code>POST</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param data Data to be submitted in the body of the request.
      * Overrides the value set with {@link #setData}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse POST(String uri, byte[] data) throws Exception
     {
 	return POST(uri, data, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>POST</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param data Data to be submitted in the body of the request.
      * Overrides the value set with {@link #setData}.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse POST(final String uri,
 				   final byte[] data,
 				   final NVPair[] headers) throws Exception
     {
 	final RequestState requestState = new RequestState(uri);
 
 	return requestState.processResponse(
 	    requestState.getConnection().Post(uri, data, headers));
     }
 
     /**
      * Makes an HTTP <code>PUT</code> request.
      *
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse PUT() throws Exception
     {
 	return PUT(null, getData(), getHeaders());
     }
 
     /**
      * Makes an HTTP <code>PUT</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse PUT(String uri) throws Exception
     {
 	return PUT(uri, getData(), getHeaders());
     }
 
     /**
      * Makes an HTTP <code>PUT</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param data Data to be submitted in the body of the request.
      * Overrides the value set with {@link #setData}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse PUT(String uri, byte[] data) throws Exception
     {
 	return PUT(uri, data, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>PUT</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse PUT(String uri, NVPair[] headers)
 	throws Exception
     {
 	return PUT(uri, getData(), headers);
     }
 
     /**
      * Makes an HTTP <code>PUT</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param data Data to be submitted in the body of the request.
      * Overrides the value set with {@link #setData}.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse PUT(final String uri,
 				  final byte[] data,
 				  final NVPair[] headers) throws Exception
     {
 	final RequestState requestState = new RequestState(uri);
 
 	return requestState.processResponse(
 	    requestState.getConnection().Put(
 		requestState.getPath(), data, headers));
     }
 
     /**
      * Makes an HTTP <code>TRACE</code> request.
      *
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse TRACE() throws Exception
     {
 	return TRACE(null, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>TRACE</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse TRACE(String uri) throws Exception
     {
 	return TRACE(uri, getHeaders());
     }
 
     /**
      * Makes an HTTP <code>TRACE</code> request.
      *
      * @param uri The URI. If a default URL has been specified with
      * {@link #setUrl}, this value need not be absolute and, if
      * relative, it will be resolved relative to the default URL.
      * Otherwise this value must be an absolute URL.
      * @param headers Request headers. Replaces all the values set by
      * {@link #setHeaders}.
      * @return Contains details of the server's response.
      * @exception Exception If an error occurs.
      */
     public final HTTPResponse TRACE(final String uri, final NVPair[] headers)
 	throws Exception
     {
 	final RequestState requestState = new RequestState(uri);
 
 	return requestState.processResponse(
 	    requestState.getConnection().Trace(
 		requestState.getPath(), headers));
     }
 
     private final class RequestState
     {
 	private final HTTPPluginThreadState m_threadState;
 	private final HTTPConnectionWrapper m_connectionWrapper;
 	private final String m_path;
 
 	public RequestState(String uri)
 	    throws GrinderException, ParseException, ProtocolNotSuppException
 	{
 	    m_threadState =
 		(HTTPPluginThreadState)
 		s_processContext.getPluginThreadListener();
 
 	    final URI url;
 	    
 	    if (uri == null) {
 		if (m_defaultURL == null) {
 		    throw new PluginException("URL not specified");
 		}
 
 		url = m_defaultURL;
 	    }
 	    else if (isAbsolute(uri)) {
 		url = new URI(uri);
 	    }
 	    else {
 		if (m_defaultURL == null) {
 		    throw new PluginException("URL must be absolute");
 		}
 
 		url = new URI(m_defaultURL, uri);
 	    }
 
 	    m_connectionWrapper = m_threadState.getConnectionWrapper(url);
 	    m_path = url.getPathAndQuery(); // And for fragment, paramaters?
 	}
 
 	public final HTTPConnection getConnection()
 	{
 	    return m_connectionWrapper.getConnection();
 	}
 
 	public String getPath()
 	{
 	    return m_path;
 	}
 
 	public final HTTPResponse processResponse(HTTPResponse httpResponse)
 	    throws IOException, ModuleException
 	{
 	    httpResponse.getData();
 	    httpResponse.getInputStream().close();
 	
 	    final int statusCode = httpResponse.getStatusCode();
 
 	    final String message =
 		httpResponse.getOriginalURI() + " -> " + statusCode + " " +
 		httpResponse.getReasonLine();
 
 	    final Logger logger = m_threadState.getThreadContext();
 	    
 	    switch (statusCode) {
 	    case HttpURLConnection.HTTP_MOVED_PERM:
 	    case HttpURLConnection.HTTP_MOVED_TEMP:
 	    case 307:
 		// It would be possible to perform the check
 		// automatically, but for now just chuck out some
 		// information.
 		logger.output(message +
 			      " [Redirect, ensure the next URL is " + 
 			      httpResponse.getHeader("Location") + "]");
 		break;
 
 	    default:
 		logger.output(message);
 		break;
 	    }
 
 	    return httpResponse;
 	}
     }
 
     private static final boolean isAbsolute(String uri)
     {
 	char ch = '\0';
 	int  pos = 0;
 	int len = uri.length();
 
 	while (pos < len &&
 	       (ch = uri.charAt(pos)) != ':' && 
 	       ch != '/' &&
 	       ch != '?' &&
 	       ch != '#') {
 	    pos++;
 	}
 	    
 	return (ch == ':');
     }
 }
