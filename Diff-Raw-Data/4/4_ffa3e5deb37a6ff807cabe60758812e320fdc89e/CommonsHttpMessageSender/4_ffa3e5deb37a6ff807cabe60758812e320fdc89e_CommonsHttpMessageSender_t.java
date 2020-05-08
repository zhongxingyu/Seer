 package org.codehaus.xfire.transport.http;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Method;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.activation.DataHandler;
 
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpState;
 import org.apache.commons.httpclient.HttpVersion;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.NTCredentials;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.RequestEntity;
 import org.apache.commons.httpclient.params.HttpClientParams;
 import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.MessageContext;
 import org.codehaus.xfire.XFireException;
 import org.codehaus.xfire.attachments.Attachments;
 import org.codehaus.xfire.attachments.JavaMailAttachments;
 import org.codehaus.xfire.attachments.SimpleAttachment;
 import org.codehaus.xfire.attachments.StreamedAttachments;
 import org.codehaus.xfire.exchange.InMessage;
 import org.codehaus.xfire.exchange.OutMessage;
 import org.codehaus.xfire.soap.SoapConstants;
 import org.codehaus.xfire.transport.Channel;
 import org.codehaus.xfire.util.OutMessageDataSource;
 import org.codehaus.xfire.util.STAXUtils;
 
 
 
 /**
  * Sends a http message via commons http client. To customize the
  * HttpClient parameters, set the property <code>HTTP_CLIENT_PARAMS</code>
  * on the MessageContext for your invocation.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @author <a href="mailto:tsztelak@gmail.com">Tomasz Sztelak</a>
  * @since Oct 26, 2004
  */
 public class CommonsHttpMessageSender extends AbstractMessageSender
 {
     private PostMethod postMethod;
 
     private HttpClient client;
 
     private HttpState state;
     
     private static final String GZIP_CONTENT_ENCODING = "gzip";
 
     public static final String DISABLE_KEEP_ALIVE = "disable-keep-alive";
     public static final String DISABLE_EXPECT_CONTINUE = "disable.expect-continue";
     public static final String HTTP_CLIENT_PARAMS = "httpClient.params";
     public static final String USER_AGENT =  
         "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; XFire Client +http://xfire.codehaus.org)";
     public static final String HTTP_PROXY_HOST = "http.proxyHost";
     public static final String HTTP_PROXY_PORT = "http.proxyPort";
     public static final String HTTP_PROXY_USER = "http.proxy.user";
     public static final String HTTP_PROXY_PASS = "http.proxy.password";
     public static final String HTTP_STATE = "httpClient.httpstate";
     public static final String HTTP_CLIENT = "httpClient";
     public static final String HTTP_TIMEOUT = "http.timeout";
     
     /** Enable GZIP on request and response. */
     public static final String GZIP_ENABLED = "gzip.enabled";
     
     /** Request GZIP encoded responses. */
     public static final String GZIP_RESPONSE_ENABLED = "gzip.response.enabled";
     
     /** GZIP the requests. */
     public static final String GZIP_REQUEST_ENABLED = "gzip.request.enabled";
 
     private static final int DEFAULT_MAX_CONN_PER_HOST = 6;
 
     public  static final String MAX_CONN_PER_HOST = "max.connections.per.host";
 
     public  static final String MAX_TOTAL_CONNECTIONS = "max.total.connections";
 
     private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = MultiThreadedHttpConnectionManager.DEFAULT_MAX_TOTAL_CONNECTIONS;
 
     private static final Log log = LogFactory.getLog(CommonsHttpMessageSender.class);
 
 	public static final String HTTP_HEADERS = "http.custom.headers.map";
     
 	
 	public static final String DISABLE_PROXY_UTILS = "http.disable.proxy.utils";
 
 	public static final String PROXY_UTILS_CLASS = "proxy.utils.class";
 	
 	private static final String DEFAULT_PROXY_UTILS_CLASS = "org.codehaus.xfire.transport.http.ProxyUtils";
 	
     private InputStream msgIs;
 
     private OutMessageDataSource source;
 
 	private boolean useProxyUtils=true;
     
     public CommonsHttpMessageSender(OutMessage message, MessageContext context)
     {
         super(message, context);
         Object disableProxyUtils = context.getContextualProperty(DISABLE_PROXY_UTILS);
         if( disableProxyUtils != null ){
           	useProxyUtils = !Boolean.valueOf(disableProxyUtils.toString()).booleanValue();
          }
     }
     
     public void open()
         throws IOException, XFireException
     {
         MessageContext context = getMessageContext();
 
         createClient();
         
         // Pull the HttpState from the context if possible. Otherwise create
         // one in the ThreadLocal
         state = getHttpState();
         
         postMethod = new PostMethod(getUri());
         
         if (Boolean.valueOf((String) context.getContextualProperty(DISABLE_KEEP_ALIVE)).booleanValue()) {
             postMethod.setRequestHeader("Connection", "Close");
         }
 
         // set the username and password if present
         String username = (String) context.getContextualProperty(Channel.USERNAME);
         if (username != null)
         {
             
             client.getParams().setAuthenticationPreemptive(true);
             
             String password = (String) context.getContextualProperty(Channel.PASSWORD);
             
             state.setCredentials(AuthScope.ANY,  getCredentials(username, password));
                         
         }
         
         if (getSoapAction() != null)
         {
             postMethod.setRequestHeader("SOAPAction", getQuotedSoapAction());
         }
         
         OutMessage message = getMessage();
         boolean mtomEnabled = Boolean.valueOf((String) context.getContextualProperty(SoapConstants.MTOM_ENABLED)).booleanValue();
         Attachments atts = message.getAttachments();
         
         if (mtomEnabled || atts != null)
         {
             if (atts == null)
             {
                 atts = new JavaMailAttachments();
                 message.setAttachments(atts);
             }
             
             source = new OutMessageDataSource(context, message);
             DataHandler soapHandler = new DataHandler(source);
             atts.setSoapContentType(HttpChannel.getSoapMimeType(message, false));
             atts.setSoapMessage(new SimpleAttachment(source.getName(), soapHandler));
             
             postMethod.setRequestHeader("Content-Type", atts.getContentType());
         }
         else
         {
             postMethod.setRequestHeader("Content-Type", HttpChannel.getSoapMimeType(getMessage(), true));
         }
         
         if (isGzipResponseEnabled(context))
         {
             postMethod.setRequestHeader("Accept-Encoding", GZIP_CONTENT_ENCODING);
         }
         
         if (isGzipRequestEnabled(context))
         {
             postMethod.setRequestHeader("Content-Encoding", GZIP_CONTENT_ENCODING);
         }
         
         Map headersMap = (Map) context.getContextualProperty(HTTP_HEADERS);
         if (headersMap != null) {
 			for (Iterator iter = headersMap.entrySet().iterator(); iter.hasNext();) {
 				Map.Entry entry = (Entry) iter.next();
 				postMethod.addRequestHeader(entry.getKey().toString(), entry.getValue().toString());
 			}
 		}
         
     }
     
     private int getIntValue(String key, int defaultValue ){
         int result = defaultValue;
         MessageContext context = getMessageContext();
         String str = (String)context.getContextualProperty(key);
         if( str != null )
         {
             result = Integer.parseInt(str);
         }
         return result;
     }
     
     private synchronized void createClient()
     {
         MessageContext context = getMessageContext();
         client = (HttpClient) ((HttpChannel) getMessage().getChannel()).getProperty(HTTP_CLIENT);
         if (client == null)
         {
             client = new HttpClient();
             MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
             HttpConnectionManagerParams conParams = new HttpConnectionManagerParams (); 
             manager.setParams(conParams);
             int maxConnPerHost = getIntValue(MAX_CONN_PER_HOST, DEFAULT_MAX_CONN_PER_HOST);
             conParams.setDefaultMaxConnectionsPerHost(maxConnPerHost );
             int maxTotalConn  = getIntValue(MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_TOTAL_CONNECTIONS);
             conParams.setMaxTotalConnections(maxTotalConn);
             client.setHttpConnectionManager(manager);
             ((HttpChannel) getMessage().getChannel()).setProperty(HTTP_CLIENT, client);
 
             HttpClientParams params = (HttpClientParams) context.getContextualProperty(HTTP_CLIENT_PARAMS);
             if (params == null)
             {
                 params = client.getParams();
                 client.getParams().setParameter("http.useragent", USER_AGENT);
                 boolean disableEC = Boolean.valueOf((String)context.getContextualProperty(DISABLE_EXPECT_CONTINUE)).booleanValue();
                 client.getParams().setBooleanParameter("http.protocol.expect-continue", !disableEC);
                 client.getParams().setVersion(HttpVersion.HTTP_1_1);
                 String timeoutStr = (String) context.getContextualProperty(HTTP_TIMEOUT);
                 if (timeoutStr != null)
                 {
                     client.getParams().setSoTimeout(Integer.parseInt(timeoutStr));
                 }
             }
             else
             {
                 client.setParams(params);
             }
 
             if (isNonProxyHost(getMessage().getUri(), context)) 
             {
                 return;
             }
             
             // Setup the proxy settings
             String proxyHost = (String) context.getContextualProperty(HTTP_PROXY_HOST);
             if (proxyHost == null)
             {
                 proxyHost = System.getProperty(HTTP_PROXY_HOST);
             }
             
             if (proxyHost != null)
             { 
                 String portS = (String) context.getContextualProperty(HTTP_PROXY_PORT);
                 if (portS == null)
                 {
                     portS = System.getProperty(HTTP_PROXY_PORT);
                 }
                 int port = 80;
                 if (portS != null)
                     port = Integer.parseInt(portS);
 
                 client.getHostConfiguration().setProxy(proxyHost, port);
                 
                 String proxyUser = (String) context.getContextualProperty(HTTP_PROXY_USER);
                 String proxyPass = (String) context.getContextualProperty(HTTP_PROXY_PASS);
                if( proxyUser != null && proxyPass != null ) 
                	getHttpState().setProxyCredentials(AuthScope.ANY,getCredentials(proxyUser, proxyPass));
             }
         }
     }
 
     private boolean isNonProxyHost( String strURI, MessageContext context ) 
     {
     	if (!useProxyUtils) {
     		return false;
     	}
     	
     	if (!isJDK5andAbove()) {
     		useProxyUtils = false;
     		return false;
     	}
     	
     	Class clazz;
     	String className = (String) context.getContextualProperty(PROXY_UTILS_CLASS);
     	if( className == null ){
     		className = DEFAULT_PROXY_UTILS_CLASS;
     	}
     	// TODO : make this clazz static
 		try {
 			clazz = Class.forName(className);
 			Object proxyUtils = clazz.newInstance();
 	    	//Method 
 	    	Method method = clazz.getDeclaredMethod("isNonProxyHost", new Class[]{String.class});
 	    	Boolean result = (Boolean) method.invoke(proxyUtils,new Object[]{strURI});
 	    	return result.booleanValue();
 		} catch (Exception  e) {
 			// Don't care what happend, we can't check for proxy
 			log.debug("Could not load ProxyUtils class: "+ className);
 			return false;
 		}
     }
     
 
     boolean isJDK5andAbove()
     {
         String v = System.getProperty("java.class.version", "44.0");
         return ("49.0".compareTo(v) <= 0);
     }
     
     static boolean isGzipRequestEnabled(MessageContext context)
     {
         if (isGzipEnabled(context)) return true;
         
         Object gzipReqEnabled = context.getContextualProperty(GZIP_REQUEST_ENABLED);
         return (gzipReqEnabled != null && gzipReqEnabled.toString().toLowerCase().equals("true"));
     }
     
     static boolean isGzipEnabled(MessageContext context)
     {
         Object gzipEnabled = context.getContextualProperty(GZIP_ENABLED);
         return (gzipEnabled != null && gzipEnabled.toString().toLowerCase().equals("true"));
     }
     
     static boolean isGzipResponseEnabled(MessageContext context)
     {
         if (isGzipEnabled(context)) return true;
         
         Object gzipResEnabled = context.getContextualProperty(GZIP_RESPONSE_ENABLED);
         return (gzipResEnabled != null && gzipResEnabled.toString().toLowerCase().equals("true"));
     }
     
     public void send()
         throws HttpException, IOException, XFireException
     {
         RequestEntity requestEntity;
         
         /**
          * Lots of HTTP servers don't handle chunking correctly, so its turned off by default.
          */
         boolean chunkingOn = Boolean.valueOf((String) getMessageContext()
                 .getContextualProperty(HttpTransport.CHUNKING_ENABLED)).booleanValue();
         if (!chunkingOn)
         {
             requestEntity = getByteArrayRequestEntity();
         }
         else
         {
             requestEntity = new OutMessageRequestEntity(getMessage(), getMessageContext());
         }
         
         getMethod().setRequestEntity(requestEntity);
         
         client.executeMethod(null, postMethod, state);
     }
 
     public int getStatusCode(){
         return  postMethod.getStatusCode();
     }
     /**
      * @return
      */
     
     public boolean hasResponse()
     {
         NameValuePair pair = postMethod.getResponseHeader("Content-Type");
         if(pair == null) return false;
         
         String ct = pair.getValue();
         
         return ct != null && ct.length() > 0;
     }
     
     public HttpState getHttpState()
     {
         HttpState state = (HttpState) ((HttpChannel) getMessage().getChannel()).getProperty(HTTP_STATE);
         if (state == null) 
         {
             state = new HttpState();
             
             ((HttpChannel) getMessage().getChannel()).setProperty(HTTP_STATE, state);
         }
         
         return state;
     }
 
     private RequestEntity getByteArrayRequestEntity()
         throws IOException, XFireException
     {
         OutMessage message = getMessage();
         MessageContext context = getMessageContext();
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         OutputStream os = bos;
 
         if (isGzipRequestEnabled(context))
         {
             os = new GZIPOutputStream(os);
         }
         
         Attachments atts = message.getAttachments();
         if (atts != null)
         {
             atts.write(os);
         }
         else
         {
             HttpChannel.writeWithoutAttachments(context, message, os);
         }
         
         os.close();
         
         return new ByteArrayRequestEntity(bos.toByteArray());
     }
     
     public InMessage getInMessage()
         throws IOException
     {
         String ct = postMethod.getResponseHeader("Content-Type").getValue();
         InputStream in = postMethod.getResponseBodyAsStream();
         Header hce = postMethod.getResponseHeader("Content-Encoding");
         
         if (hce != null && hce.getValue().equals(GZIP_CONTENT_ENCODING))
         {
             in = new GZIPInputStream(in);
         }
         
         if (ct.toLowerCase().indexOf("multipart/related") != -1)
         {
             Attachments atts = new StreamedAttachments(getMessageContext(),in, ct);
 
             msgIs = atts.getSoapMessage().getDataHandler().getInputStream();
             
             InMessage msg = new InMessage(STAXUtils.createXMLStreamReader(msgIs, getEncoding(),getMessageContext()), getUri());
             msg.setAttachments(atts);
             return msg;
         }
         else
         {
             return new InMessage(STAXUtils.createXMLStreamReader(in, getEncoding(),getMessageContext()), getUri());
         }
     }
 
     public PostMethod getMethod()
     {
         return this.postMethod;
     }
 
     public void close()
         throws XFireException
     {
         if (msgIs != null)
         {
             try
             {
                 msgIs.close();
             }
             catch (IOException e)
             {
                 throw new XFireException("Could not close connection.", e);
             }
         }
         
         if (source != null)
         {
             source.dispose();
         }
         
         if (postMethod != null)
             postMethod.releaseConnection();
     }
     
 	private Credentials getCredentials(String username, String password){
 		 
 	         client.getParams().setAuthenticationPreemptive(true);
 	         
 	         int domainIndex = username.indexOf('\\');
 	         if (domainIndex > 0 && username.length() > domainIndex + 1) {
 	
 	                     return new NTCredentials(
 	                    		 username.substring(domainIndex+1), 
 	                             password, 
 	                             "localhost", // TODO: resolve local host name 
 	                             username.substring(0, domainIndex));
 	                     
 	         } 
 	             
 	             return  new UsernamePasswordCredentials(username,password);
 	             
 	         
 	         
 	     
 	}
 	    
 }
