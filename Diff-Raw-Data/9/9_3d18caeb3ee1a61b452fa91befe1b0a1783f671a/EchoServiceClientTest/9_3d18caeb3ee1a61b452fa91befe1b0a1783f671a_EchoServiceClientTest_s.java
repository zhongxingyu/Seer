 /*
  * Copyright (c) Members of the EGEE Collaboration. 2004. See
  * http://www.eu-egee.org/partners/ for details on the copyright holders.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package eu.emi.security.canl.axis2.test;
 
 import java.util.ArrayList;
 import java.util.Properties;
 
 import javax.net.ssl.SSLSocketFactory;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.junit.Test;
 import java.lang.Exception;
 
 import eu.emi.security.authn.x509.CrlCheckingMode;
 import eu.emi.security.authn.x509.NamespaceCheckingMode;
 import eu.emi.security.authn.x509.OCSPParametes;
 import eu.emi.security.authn.x509.ProxySupport;
 import eu.emi.security.authn.x509.RevocationParameters;
 import eu.emi.security.authn.x509.RevocationParameters.RevocationCheckingOrder;
 import eu.emi.security.authn.x509.StoreUpdateListener;
 import eu.emi.security.authn.x509.X509Credential;
 import eu.emi.security.authn.x509.impl.OpensslCertChainValidator;
 import eu.emi.security.authn.x509.impl.PEMCredential;
 import eu.emi.security.authn.x509.impl.SocketFactoryCreator;
 import eu.emi.security.authn.x509.impl.ValidatorParams;
 import eu.emi.security.canl.axis2.CANLAXIS2SocketFactory;
 
 public class EchoServiceClientTest extends TestCase {
 
 	@Test
 	public void testEndEntityConnection() throws Exception {
 		Axis2JettyServer.run();
 		Properties props = new Properties();
 		props.setProperty("canl.truststore", "src/test/certificates");
 		props.setProperty("canl.cert", "src/test/cert/trusted_client.cert");
 		props.setProperty("canl.key", "src/test/cert/trusted_client.priv");
 		props.setProperty("canl.password", "changeit");
 		props.setProperty("canl.updateinterval", "0");
 		CANLAXIS2SocketFactory.setCurrentProperties(props);
 		CANLAXIS2SocketFactory factory = new CANLAXIS2SocketFactory();
 		try {
 			Protocol.registerProtocol("https", new Protocol("https", factory,
 					8443));
 			EchoServiceStub stub = new EchoServiceStub(
 					"https://localhost:8888/services/EchoService");
 			GetAttributesResponseDocument doc = stub.getAttributes();
 
 			System.out.println(doc.getGetAttributesResponse().getReturn());
 			stub.cleanup();
 		} catch (Exception e) {
 			Axis2JettyServer.stop();
 			e.printStackTrace();
 			throw e;
 		}
 
 		Axis2JettyServer.stop();
 	}
 
 	@Test
 	public void testProxyConnection() throws Exception {
 		Axis2JettyServer.run();
 
 		Properties props = new Properties();
 		props = new Properties();
 		props.setProperty("canl.truststore", "src/test/certificates");
 		props.setProperty("canl.proxy",
 				"src/test/cert/trusted_client.proxy.grid_proxy");
 		props.setProperty("canl.updateinterval", "0");
 		CANLAXIS2SocketFactory.setCurrentProperties(props);
 		CANLAXIS2SocketFactory factory = new CANLAXIS2SocketFactory();
 		try {
 			Protocol.registerProtocol("https", new Protocol("https", factory,
 					8443));
 			EchoServiceStub stub = new EchoServiceStub(
 					"https://localhost:8888/services/EchoService");
 			GetAttributesResponseDocument doc = stub.getAttributes();
 
 			System.out.println(doc.getGetAttributesResponse().getReturn());
 			stub.cleanup();
 			System.out.println("end of output");
 		} catch (Exception e) {
 			Axis2JettyServer.stop();
 			e.printStackTrace();
 			throw e;
 		}
 		System.out.println("end");
 		Axis2JettyServer.stop();
 
 	}
 
 	@Test
 	public void testFactory() throws Exception {
 		Axis2JettyServer.run();
 		CANLAXIS2SocketFactory.clearCurrentProperties();
 		StoreUpdateListener listener = new StoreUpdateListener() {
 			public void loadingNotification(String location, String type,
 					Severity level, Exception cause) {
 				if (level != Severity.NOTIFICATION) {
 					System.out
 							.println("Error when creating or using SSL socket. Type "
 									+ type
 									+ " level: "
 									+ level
 									+ " cause: "
 									+ cause.getClass()
 									+ ":"
 									+ cause.getMessage());
 				} else {
 					// log successful (re)loading
 				}
 			}
 		};
 
 		ArrayList<StoreUpdateListener> listenerList = new ArrayList<StoreUpdateListener>();
 		listenerList.add(listener);
 		RevocationParameters revParam = new RevocationParameters(
 				CrlCheckingMode.REQUIRE, new OCSPParametes(), false,
 				RevocationCheckingOrder.CRL_OCSP);
 		ProxySupport proxySupport = ProxySupport.ALLOW;
 		ValidatorParams validatorParams = new ValidatorParams(revParam,
 				proxySupport, listenerList);
 		NamespaceCheckingMode namespaceMode = NamespaceCheckingMode.EUGRIDPMA_AND_GLOBUS;
 		long intervalMS = 3600000; // update ever hour
 		OpensslCertChainValidator validator = new OpensslCertChainValidator(
 				"src/test/certificates", namespaceMode, intervalMS,
 				validatorParams);
 		X509Credential credentials = new PEMCredential(
				"src/test/cert/trusted_client.proxy.grid_proxy", null);
 		SSLSocketFactory newFactory = SocketFactoryCreator.getSocketFactory(
 				credentials, validator);
 
 		CANLAXIS2SocketFactory factory = new CANLAXIS2SocketFactory(newFactory);
 		try {
 			Protocol.registerProtocol("https", new Protocol("https", factory,
 					8443));
 			EchoServiceStub stub = new EchoServiceStub(
 					"https://localhost:8888/services/EchoService");
 			GetAttributesResponseDocument doc = stub.getAttributes();
 
 			System.out.println(doc.getGetAttributesResponse().getReturn());
 			stub.cleanup();
 			System.out.println("end of output");
 		} catch (Exception e) {
 			Axis2JettyServer.stop();
 			e.printStackTrace();
 			throw e;
 		}
 		System.out.println("end");
 		Axis2JettyServer.stop();
 
 	}
 
 	@Test
 	public void testSystemProps() throws Exception {
 		Axis2JettyServer.run();
 		CANLAXIS2SocketFactory.clearCurrentProperties();
 
 		System.setProperty("canl.truststore", "src/test/certificates");
 		System.setProperty("canl.proxy",
 				"src/test/cert/trusted_client.proxy.grid_proxy");
 		System.setProperty("canl.updateinterval", "0");
 		CANLAXIS2SocketFactory factory = new CANLAXIS2SocketFactory();
 		try {
 			Protocol.registerProtocol("https", new Protocol("https", factory,
 					8443));
 			EchoServiceStub stub = new EchoServiceStub(
 					"https://localhost:8888/services/EchoService");
 			GetAttributesResponseDocument doc = stub.getAttributes();
 
 			System.out.println(doc.getGetAttributesResponse().getReturn());
 			stub.cleanup();
 			System.out.println("end of output");
 		} catch (Exception e) {
 			Axis2JettyServer.stop();
 			e.printStackTrace();
 			throw e;
 		}
 		System.out.println("end");
 		Axis2JettyServer.stop();
 
 	}
 
 	public static void main(final String[] args) throws java.lang.Exception {
 		EchoServiceClientTest client = new EchoServiceClientTest();
 		client.testEndEntityConnection();
 		System.exit(0);
 	}
 }
