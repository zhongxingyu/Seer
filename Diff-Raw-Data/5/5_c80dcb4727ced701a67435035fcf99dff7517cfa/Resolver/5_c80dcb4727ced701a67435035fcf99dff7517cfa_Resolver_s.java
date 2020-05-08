 package org.cagrid.identifiers.resolver;
 
 import gov.nih.nci.cagrid.identifiers.client.IdentifiersNAServiceClient;
 import gov.nih.nci.cagrid.identifiers.stubs.types.InvalidIdentifierFault;
 import gov.nih.nci.cagrid.identifiers.stubs.types.NamingAuthorityConfigurationFault;
 import gov.nih.nci.cagrid.identifiers.stubs.types.NamingAuthoritySecurityFault;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.HttpContext;
 import org.cagrid.identifiers.client.Util;
 import org.cagrid.identifiers.namingauthority.InvalidIdentifierException;
 import org.cagrid.identifiers.namingauthority.NamingAuthorityConfigurationException;
 import org.cagrid.identifiers.namingauthority.NamingAuthoritySecurityException;
 import org.cagrid.identifiers.namingauthority.UnexpectedIdentifiersException;
 import org.cagrid.identifiers.namingauthority.domain.IdentifierData;
 import org.cagrid.identifiers.namingauthority.domain.NamingAuthorityConfig;
 import org.cagrid.identifiers.namingauthority.impl.SecurityInfoImpl;
 import org.cagrid.identifiers.namingauthority.util.IdentifierUtil;
 import org.cagrid.identifiers.namingauthority.util.SecurityUtil;
 import org.exolab.castor.xml.MarshalException;
 import org.exolab.castor.xml.Unmarshaller;
 import org.exolab.castor.xml.ValidationException;
 import org.exolab.castor.xml.XMLContext;
 import org.globus.axis.gsi.GSIConstants;
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
 import org.globus.net.GSIHttpURLConnection;
 import org.ietf.jgss.GSSCredential;
 import org.ietf.jgss.GSSException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class Resolver {
 	
 	protected static Log LOG = LogFactory.getLog(Resolver.class.getName());
 	private XMLContext xmlContext = null;
 	private ApplicationContext appCtx = null;
 	private Map<String, NamingAuthorityConfig> naConfigs =
 		new HashMap<String, NamingAuthorityConfig>();
     
 	private final String GSI_FORBIDDEN = "Forbidden";
 	
 	public Resolver() {
 		init( new String[] {Util.DEFAULT_SPRING_CONTEXT_RESOURCE} );
 	}
 	
 	public Resolver( String[] springCtxList ) {
 		init( springCtxList );
 	}
 	
 	private void init(String[] springCtxList) {
 		appCtx = new ClassPathXmlApplicationContext( springCtxList );
         xmlContext = (XMLContext) appCtx.getBean(Util.CASTOR_CONTEXT_BEAN);	
 	}
 
 	private String getResponseString( HttpResponse response ) throws HttpException {
 		
 		StringBuffer responseStr = new StringBuffer();
 		
 		try {
 		HttpEntity entity = response.getEntity();
 		if (entity != null) {
 			BufferedReader reader = new BufferedReader(
 					new InputStreamReader( entity.getContent() ));
 			try {
 				String line;
 				while ( (line = reader.readLine()) != null ) {
 					responseStr.append(line).append("\n");
 				}
 			} finally {
 				reader.close();
 			}
 		}
 		} catch( IOException e ) {
 			throw new HttpException("IOException: " + e, e);
 		}
 		
 		return responseStr.toString();
 	}
 	
 	private DefaultHttpClient getHttpClient() {
 		
 		DefaultHttpClient client = new DefaultHttpClient();
 		
 		// I believe DefaultHttpClient handles redirects by default anyway
 		client.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS,
 				Boolean.TRUE);
 		
 		client.addRequestInterceptor(new HttpRequestInterceptor() {   
 			public void process(
 					final HttpRequest request, 
                     final HttpContext context) throws HttpException, IOException {
 				request.addHeader("Accept", "application/xml");
 			}
 		});
 		
 		SSLSocketFactory.getSocketFactory().setHostnameVerifier( SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
 		
 		return client;
 	}
 	
 	private void checkXMLResponse(HttpResponse response, String errMsg) 
 		throws HttpException, NamingAuthoritySecurityException {
 		
 		int statusCode = response.getStatusLine().getStatusCode();
 	
     	if (statusCode != HttpStatus.SC_OK) {
     		
     		String msg = errMsg + " [" + statusCode + ":" 
     			+ response.getStatusLine().toString() + "]\n"
     			+ getResponseString(response);
     		
     		if (statusCode == HttpStatus.SC_FORBIDDEN) {
     			throw new NamingAuthoritySecurityException( msg );
     		
     		} else {
     			throw new HttpException(msg);
     		}
     	}
     	
     	Header ctHeader = response.getFirstHeader("Content-Type");
     	if (ctHeader == null || ctHeader.getValue() == null ||
     			ctHeader.getValue().indexOf("application/xml") == -1) {
     		throw new HttpException("Response has no XML content (Content-Type: "
     				+ (ctHeader != null ? ctHeader.getValue() : "null") + 
     				"). " + errMsg);
     	}
 	}
 	
 	private String httpGet(URI url, String errMsg) 
 		throws 
 			HttpException,
 			NamingAuthoritySecurityException {
 	
 		DefaultHttpClient client = getHttpClient();
 		HttpGet method = new HttpGet( url );
      	    
 	    try {
 	    	HttpResponse response = client.execute( method );
 	    	checkXMLResponse(response, errMsg);
 	    	return getResponseString(response);
 		} catch (ClientProtocolException e) {
 			throw new HttpException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new HttpException(e.getMessage(), e);
 		} finally {
 	         // Release the connection.
 	         method.abort();
 	         client.getConnectionManager().shutdown();
 	    }  
 	}
 		
 	private synchronized NamingAuthorityConfig retrieveNamingAuthorityConfig( URI identifier ) 
 		throws 
 			InvalidIdentifierException, 
 			HttpException, 
 			NamingAuthorityConfigurationException, NamingAuthoritySecurityException {
 		
 		String idStr = identifier.normalize().toString();
 		if (idStr.endsWith("/")) {
 			throw new InvalidIdentifierException(idStr);
 		}
 		
 		String key = idStr.substring(0, idStr.lastIndexOf('/'));
 		NamingAuthorityConfig config = naConfigs.get(key);
 		
 		if (config != null) {
 			return config;
 		}
 
 		/* Oh well, we don't have it yet */
 		
 		URI configUrl = URI.create(idStr + "?config");
 		
 		String naConfigStr = httpGet(configUrl, 
 				"Unable to retrieve naming authority configuration from " 
 				+ configUrl);
 			
 		// Deserialize response
 		Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
 		unmarshaller.setClass(NamingAuthorityConfig.class);
 		
 		try {
 			config = (NamingAuthorityConfig) 
 				unmarshaller.unmarshal(new StringReader(naConfigStr));
 		} catch (Exception e) {
 			throw new NamingAuthorityConfigurationException(e);
 		}
 		
 		naConfigs.put(key, config);
 		
 		return config;
 	}
 	
 	public IdentifierData resolveGrid( URI identifier ) 
 		throws 
 			NamingAuthorityConfigurationException, 
 			InvalidIdentifierException, 
 			NamingAuthoritySecurityException, 
 			UnexpectedIdentifiersException {
 		
 		try {
 			NamingAuthorityConfig config = retrieveNamingAuthorityConfig( identifier );
 
 			IdentifiersNAServiceClient client = 
 				new IdentifiersNAServiceClient( config.getNaGridSvcURI().normalize().toString() );
 
 			return gov.nih.nci.cagrid.identifiers.common.IdentifiersNAUtil.map(
 					client.resolveIdentifier(new org.apache.axis.types.URI(identifier.toString())) );
 
 		} catch (NamingAuthorityConfigurationFault e) {
 			throw new NamingAuthorityConfigurationException(e);
 		} catch (InvalidIdentifierFault e) {
 			throw new InvalidIdentifierException(e);
 		} catch (NamingAuthoritySecurityFault e) {
 			throw new NamingAuthoritySecurityException(e);
 		} catch (Exception e) {
 			throw new UnexpectedIdentifiersException(e);
 		}
 	}
 	
 	public IdentifierData resolveHttp( URI identifier ) 
 		throws 
 			HttpException, 
 			NamingAuthorityConfigurationException, 
 			NamingAuthoritySecurityException {
 		
 		//
 		// Resolve identifier
 		//
 		String iValuesStr = httpGet(identifier, "Identifier [" 
 				+ identifier + "] failed resolution");
 		
 		//Deserialize the response
 		Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
 		unmarshaller.setClass(IdentifierData.class);
 
 		try {
 			return (IdentifierData) unmarshaller.unmarshal(new StringReader(iValuesStr));
 		} catch (Exception e) {
 			throw new NamingAuthorityConfigurationException(e);
 		}
 	}
 	
 	public IdentifierData resolveHttp( URI identifier, GlobusCredential user ) 
 		throws 
 			InvalidIdentifierException, 
 			HttpException, 
 			NamingAuthorityConfigurationException, 
 			NamingAuthoritySecurityException {
 
 		NamingAuthorityConfig config = retrieveNamingAuthorityConfig( identifier );
 		
 		URI localIdentifier = 
 			IdentifierUtil.getLocalName( config.getNaPrefixURI(), identifier);
 		
 		try {
 			URL url = new URL(config.getNaBaseURI() 
 					+ localIdentifier.normalize().toString() + "?xml");
 
 			LOG.debug(url.toString());
 
 			GlobusGSSCredentialImpl cred;
 
 			cred = new GlobusGSSCredentialImpl(
 					user, GSSCredential.INITIATE_AND_ACCEPT);
 
 			GSIHttpURLConnection connection = new GSIHttpURLConnection(url);		
 			connection.setGSSMode(GSIConstants.MODE_SSL);
 			connection.setCredentials(cred);
 
 			Unmarshaller unmarshaller = xmlContext.createUnmarshaller();
 			unmarshaller.setClass(IdentifierData.class);
 
 			return (IdentifierData) unmarshaller.unmarshal(
 					new InputStreamReader(connection.getInputStream()));
 			
 		} catch (IOException e) {
 			if (e.getMessage().equals(GSI_FORBIDDEN)) {
 				throw new NamingAuthoritySecurityException(
 					SecurityUtil.securityError(new SecurityInfoImpl(user.getIdentity()), 
 					"resolve identifier"));
 			}
 		} catch(Exception e) {
 			throw new HttpException(e.getMessage(), e);
 		}
 		
 		return null;
 	}
 }
