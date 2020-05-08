 package org.vpac.grisu.client.control;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.soap.SOAPBinding;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.control.ServiceInterfaceCreator;
 import org.vpac.grisu.control.exceptions.ServiceInterfaceException;
 
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
 
 	public ServiceInterface create(String interfaceUrl, String username,
 			char[] password, String myProxyServer, String myProxyPort,
 			Object[] otherOptions) throws ServiceInterfaceException {
 		
 		Class jaxwsServiceInterfaceClass = null;
 
 		try {
 			jaxwsServiceInterfaceClass = Class
 					.forName("org.vpac.grisu.control.impl.EnunciateServiceInterfaceImpl");
 		} catch (ClassNotFoundException e) {
 			myLogger.warn("Could not find jaxws service interface class.");
 			e.printStackTrace();
 			throw new ServiceInterfaceException(
 					"Could not find JaxWsServiceInterface class. Probably grisu-client-jaxws is not in the classpath.",
 					e);
 		}
 
 		Object jaxwsServiceInterface = null;
 		Class interfaceClass = null;
 		
 		try {
 			Constructor jaxwsServiceInterfaceConstructor;
 			interfaceClass = Class.forName("org.vpac.grisu.control.EnunciateServiceInterface");
 
 			
 			jaxwsServiceInterfaceConstructor = jaxwsServiceInterfaceClass
 					.getConstructor(String.class);
 
 			jaxwsServiceInterface = jaxwsServiceInterfaceConstructor
 					.newInstance(interfaceUrl);
 
 			Method getBindingProvider = jaxwsServiceInterface.getClass().getMethod(
 					"_getBindingProvider");
 
 			BindingProvider bp = (BindingProvider) getBindingProvider.invoke(jaxwsServiceInterface);
 			
 			bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
 			bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, new String(password));
 			bp.getRequestContext().put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 4096);
 			SOAPBinding binding = (SOAPBinding)bp.getBinding();
 			binding.setMTOMEnabled(true);
 
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
//			e.printStackTrace();
 			throw new ServiceInterfaceException(
 					"Could not create JaxwsServiceInterface: "
 							+ e.getLocalizedMessage(), e);
 		}
 		
 		return new ProxyServiceInterface(jaxwsServiceInterface);
 
 	}
 
 }
