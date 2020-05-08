 package grisu.frontend.control;
 
 import grisu.control.ServiceInterface;
 import grisu.control.ServiceInterfaceCreator;
 import grisu.control.exceptions.ServiceInterfaceException;
 import grisu.frontend.control.jaxws.CommandLogHandler;
 import grisu.frontend.control.login.LoginManager;
 import grisu.settings.Environment;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Service;
 import javax.xml.ws.handler.Handler;
 import javax.xml.ws.handler.MessageContext;
 import javax.xml.ws.soap.MTOMFeature;
 import javax.xml.ws.soap.SOAPBinding;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.python.google.common.collect.Maps;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.xml.ws.developer.JAXWSProperties;
 
 public class JaxWsServiceInterfaceCreator implements ServiceInterfaceCreator {
 
 	static final Logger myLogger = LoggerFactory
 			.getLogger(JaxWsServiceInterfaceCreator.class.getName());
 
 	public static String TRUST_FILE_NAME = Environment
 			.getGrisuClientDirectory().getPath()
 			+ File.separator
 			+ "truststore.jks";
 
 	/**
 	 * configures secure connection parameters.
 	 **/
 	public JaxWsServiceInterfaceCreator() throws ServiceInterfaceException {
 		try {
 			if (!(new File(Environment.getGrisuClientDirectory(),
 					"truststore.jks").exists())) {
 				final InputStream ts = JaxWsServiceInterfaceCreator.class
 						.getResourceAsStream("/truststore.jks");
 				IOUtils.copy(ts, new FileOutputStream(TRUST_FILE_NAME));
 			}
 		} catch (final IOException ex) {
 			throw new ServiceInterfaceException(
 					"cannot copy SSL certificate store into grisu home directory. Does "
 							+ Environment.getGrisuClientDirectory().getPath()
 							+ " exist?", ex);
 		}
 		System.setProperty("javax.net.ssl.trustStore", TRUST_FILE_NAME);
 		System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
 
 	}
 
 	public boolean canHandleUrl(String url) {
 		if (StringUtils.isNotBlank(url) && url.startsWith("http")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public ServiceInterface create(String interfaceUrl, String username,
 			char[] password, String myProxyServer, String myProxyPort,
 			Object[] otherOptions) throws ServiceInterfaceException {
 
 		try {
 
 			final QName serviceName = new QName("http://api.grisu",
 					"GrisuService");
 			final QName portName = new QName("http://api.grisu",
 					// "ServiceInterfaceSOAPPort");
 					"ServiceInterfacePort");
 
 			Service s;
 			try {
 				s = Service.create(
 						new URL(interfaceUrl.replace("soap/GrisuService",
 								"api.wsdl")), serviceName);
 				// "ns1.wsdl")), serviceName);
 			} catch (final MalformedURLException e) {
 				throw new RuntimeException(e);
 			}
 
 			final MTOMFeature mtom = new MTOMFeature();
 			try {
 				s.getPort(portName, ServiceInterface.class, mtom);
 			} catch (final Error e) {
 				// throw new ServiceInterfaceException(
 				// "Could not connect to backend, probably because of frontend/backend incompatibility (Underlying cause: \""
 				// + e.getLocalizedMessage()
 				// +
 				// "\"). Before reporting a bug, please try latest client from: http://code.ceres.auckland.ac.nz/stable-downloads.");
 				throw new ServiceInterfaceException(
 						"Sorry, could not login. Most likely your client version is incompatible with the server.\n\n"
 								+ "Please download the latest version from:\n\nhttp://code.ceres.auckland.ac.nz/stable-downloads\n\n"
 								+ "If you have the latest version and are still experiencing this problem please contact\n\n"
 								+ "eresearch-admin@list.auckland.ac.nz\n\n"
 								+ "with a description of the issue.\n\nUnderlying cause: "
 								+ e.getLocalizedMessage());
 			}
 
 			final ServiceInterface service = s.getPort(portName,
 					ServiceInterface.class);
 
 			final BindingProvider bp = (javax.xml.ws.BindingProvider) service;
 
 			bp.getRequestContext().put(
 					javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
 					interfaceUrl);
 
 			bp.getRequestContext().put(
 					JAXWSProperties.HTTP_CLIENT_STREAMING_CHUNK_SIZE, 4096);
 
 			// just to be sure, I'll keep that in there as well...
 			bp.getRequestContext()
 			.put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size",
 					new Integer(4096));
 
 			bp.getRequestContext().put(BindingProvider.USERNAME_PROPERTY,
 					username);
 			bp.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY,
 					new String(password));
 
 			bp.getRequestContext().put(
 					BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.TRUE);
 
 			final SOAPBinding binding = (SOAPBinding) bp.getBinding();
 
 			binding.setMTOMEnabled(true);
 
 			String clientstring = LoginManager.getClientName()
 					+ " "
 					+ LoginManager.getClientVersion()
 					+ " / "
 					+ "frontend "
 					+ grisu.jcommons.utils.Version
 					.get("grisu-client");
 
 			Map map = Maps.newHashMap();
 
			map.put("X-login-host", Collections.singletonList(myProxyServer));
			map.put("X-login-port", Collections.singletonList(myProxyPort));
 
 			map.put("X-grisu-client", Collections.singletonList(clientstring));
 
 			String session_id = LoginManager.USER_SESSION;
 			if (StringUtils.isNotBlank(session_id)) {
 				map.put("X-client-session-id",
 						Collections.singletonList(session_id));
 			}
 
 			bp.getRequestContext()
 			.put(MessageContext.HTTP_REQUEST_HEADERS, map);
 
 			CommandLogHandler authnHandler = new CommandLogHandler(bp);
 			List<Handler> hc = binding.getHandlerChain();
 			hc.add(authnHandler);
 
 			binding.setHandlerChain(hc);
 
 
 			return service;
 
 		} catch (final Exception e) {
 			// TODO Auto-generated catch block
 			// e.printStackTrace();
 			throw new ServiceInterfaceException(
 					"Could not create JaxwsServiceInterface: "
 							+ e.getLocalizedMessage(), e);
 		}
 
 	}
 
 }
