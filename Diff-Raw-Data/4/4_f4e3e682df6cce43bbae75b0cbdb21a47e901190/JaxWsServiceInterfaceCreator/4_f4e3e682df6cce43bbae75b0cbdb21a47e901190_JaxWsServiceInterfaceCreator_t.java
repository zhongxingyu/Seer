 package org.vpac.grisu.frontend.control;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.net.ssl.SSLSocketFactory;
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Service;
 import javax.xml.ws.soap.SOAPBinding;
 
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.ssl.HttpSecureProtocol;
 import org.apache.commons.ssl.TrustMaterial;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.ServiceInterfaceCreator;
 import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
 import org.vpac.grisu.settings.CaCertManager;
 
 
 
 public class JaxWsServiceInterfaceCreator implements ServiceInterfaceCreator {
 	
 	static final Logger myLogger = Logger
 	.getLogger(JaxWsServiceInterfaceCreator.class.getName());
 
 	public boolean canHandleUrl(String url) {
 		if ( StringUtils.isNotBlank(url) && url.startsWith("http") ) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private SSLSocketFactory createSocketFactory(String interfaceUrl) throws ServiceInterfaceException {
 		// Technique similar to
 		// http://juliusdavies.ca/commons-ssl/TrustExample.java.html
 		HttpSecureProtocol protocolSocketFactory;
 		try {
 			protocolSocketFactory = new HttpSecureProtocol();
 
 			TrustMaterial trustMaterial = null;
 
 			// "/thecertificate.cer" can be PEM or DER (raw ASN.1). Can even
 			// be several PEM certificates in one file.
 
 			String cacertFilename = System.getProperty("grisu.cacert");
 			URL cacertURL = null;
 
 			try {
 				if (cacertFilename != null && !"".equals(cacertFilename)) {
 					cacertURL = JaxWsServiceInterfaceCreator.class
 							.getResource("/" + cacertFilename);
 					if (cacertURL != null) {
 						myLogger.debug("Using cacert " + cacertFilename
 								+ " as configured in the -D option.");
 					}
 				}
 			} catch (Exception e) {
 				// doesn't matter
 				myLogger
 						.debug("Couldn't find specified cacert. Using default one.");
 			}
 
 			if (cacertURL == null) {
 
 				cacertFilename = new CaCertManager()
 						.getCaCertNameForServiceInterfaceUrl(interfaceUrl);
 				if (cacertFilename != null && cacertFilename.length() > 0) {
 					myLogger
 							.debug("Found url in map. Trying to use this cacert file: "
 									+ cacertFilename);
 					cacertURL = JaxWsServiceInterfaceCreator.class
 							.getResource("/" + cacertFilename);
 					if (cacertURL == null) {
 						myLogger
 								.debug("Didn't find cacert. Using the default one.");
 						// use the default one
 						cacertURL = JaxWsServiceInterfaceCreator.class
 								.getResource("/cacert.pem");
 					} else {
 						myLogger.debug("Found cacert. Using it. Good.");
 					}
 				} else {
 					myLogger
 							.debug("Didn't find any configuration for a special cacert. Using the default one.");
 					// use the default one
 					cacertURL = JaxWsServiceInterfaceCreator.class
 							.getResource("/cacert.pem");
 				}
 
 			}
 
 			trustMaterial = new TrustMaterial(cacertURL);
 
 			// We can use setTrustMaterial() instead of addTrustMaterial()
 			// if we want to remove
 			// HttpSecureProtocol's default trust of TrustMaterial.CACERTS.
 			protocolSocketFactory.addTrustMaterial(trustMaterial);
 
 			// Maybe we want to turn off CN validation (not recommended!):
 			protocolSocketFactory.setCheckHostname(false);
 
 			Protocol protocol = new Protocol("https",
 					(ProtocolSocketFactory) protocolSocketFactory, 443);
 			Protocol.registerProtocol("https", protocol);
 			
 			return protocolSocketFactory;
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 			throw new ServiceInterfaceException(
 					"Unspecified error while trying to establish secure connection.", e1);
 		}
 	}
 
 	public ServiceInterface create(String interfaceUrl, String username,
 			char[] password, String myProxyServer, String myProxyPort,
 			Object[] otherOptions) throws ServiceInterfaceException {
 		
 		
 		
 		try {
 			
 			QName serviceName = new QName("http://api.grisu.arcs.org.au/", "GrisuService");
 			QName portName = new QName("http://api.grisu.arcs.org.au/", "ServiceInterfaceSOAPPort");
 
 			Service s;
 			try {
 				s = Service.create(new URL(interfaceUrl.replace("soap/GrisuService", "api.wsdl")), serviceName);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 				throw new RuntimeException(e);
 			}
 			
 			ServiceInterface service = (ServiceInterface)s.getPort(portName, ServiceInterface.class);
 
 			
 			BindingProvider bp = (javax.xml.ws.BindingProvider)service;
 			bp.getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, interfaceUrl);
 
			bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", new Integer(4096));
 			
 			bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
 			bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, new String(password));
 			
 			bp.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
 
 			SOAPBinding binding = (SOAPBinding) bp.getBinding();
 			binding.setMTOMEnabled(true);
 			
 			return service;
 			
 //			Constructor jaxwsServiceInterfaceConstructor;
 //			interfaceClass = Class.forName("org.vpac.grisu.control.EnunciateServiceInterface");
 //
 //			
 //			jaxwsServiceInterfaceConstructor = jaxwsServiceInterfaceClass
 //					.getConstructor(String.class);
 //
 //			jaxwsServiceInterface = jaxwsServiceInterfaceConstructor
 //					.newInstance(interfaceUrl);
 //
 //			Method getBindingProvider = jaxwsServiceInterface.getClass().getMethod(
 //					"_getBindingProvider");
 //
 //			BindingProvider bp = (BindingProvider) getBindingProvider.invoke(jaxwsServiceInterface);
 //			
 //			bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
 //			bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, new String(password));
 //			
 //			bp.getRequestContext().put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory", createSocketFactory(interfaceUrl));
 //
 //			bp.getRequestContext().put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);
 //			
 //			bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 4096);
 //			SOAPBinding binding = (SOAPBinding)bp.getBinding();
 //			binding.setMTOMEnabled(true);
 
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 //			e.printStackTrace();
 			throw new ServiceInterfaceException(
 					"Could not create JaxwsServiceInterface: "
 							+ e.getLocalizedMessage(), e);
 		}
 		
 
 	}
 
 }
