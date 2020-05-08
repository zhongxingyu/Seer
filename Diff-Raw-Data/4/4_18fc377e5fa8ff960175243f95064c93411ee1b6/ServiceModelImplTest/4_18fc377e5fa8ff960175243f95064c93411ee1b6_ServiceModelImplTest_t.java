 package org.eclipse.swordfish.tooling.ui;
 
 import java.net.URL;
 
 import org.eclipse.swordfish.tooling.ui.helper.ServiceModel;
 import org.eclipse.swordfish.tooling.ui.helper.Wsdl4jModel;
 
 import junit.framework.TestCase;
 
 public class ServiceModelImplTest extends TestCase {
 	private static final String BASE_NAME = "/resources/wsdl/airport";
 	private static final String NON_EXISTING = "_hasilein";
 	private static final String NO_SERVICE = "_NoService";
 	private static final String TWO_SERVICES = "_2Services";
 	private static final String SOAP = "_soap";
 	private static final String HTTP = "_http";
 	private static final String SOAP12 = SOAP + "12";
 	private static final String WSDL = ".wsdl";
 	
 	private static final String W_HTTP_SOAP_SOAP12 = BASE_NAME + HTTP + SOAP + SOAP12 + WSDL;
 	private static final String W_HTTP = BASE_NAME + HTTP + WSDL;
 	private static final String W_SOAP_SOAP12_HTTP = BASE_NAME + SOAP + SOAP12 + HTTP + WSDL;
 	private static final String W_SOAP = BASE_NAME + SOAP + WSDL;
 	private static final String W_SOAP12_SOAP_HTTP = BASE_NAME + SOAP12 + SOAP + HTTP + WSDL;
 	private static final String W_SOAP12 = BASE_NAME + SOAP12 + WSDL;
 	private static final String W_TWO_SERVICES = BASE_NAME + TWO_SERVICES + WSDL;
 	private static final String W_NO_SERVICE = BASE_NAME + NO_SERVICE + WSDL;
 	private static final String W_NON_EXISTING = BASE_NAME + NON_EXISTING + WSDL;
 
 	private static final String EXPECTED_SOAP11_SERVICE_URL = "http://localhost:8200/airport.service";
 	private static final String EXPECTED_SOAP12_SERVICE_URL = "http://localhost:8201/airport.service";
 	private static final String EXPECTED_SERVICE_NAME = "airport";
 	private static final String EXPECTED_IMPLEMENTOR_CLASS = "de.sopera.airportsoap.Airport";
 	private static final String EXPECTED_NAMESPACE = "http://airportsoap.sopera.de";
 	
 	private static final String EXPECTED_CARPORT_IMPLEMENTOR_CLASS = "de.sopera.airportsoap.Carport";
 	private static final String EXPECTED_CARPORT_SOAP_SERVICE_URL = "http://localhost:8200/carport.service";
 	private static final String EXPECTED_CARPORT_SERVICE_NAME = "carport";
 	
 	private class ServiceModelMock implements ServiceModel {
 		private URL wsdlName;
 		private String implementorClass;
 		private String serviceName;
 		private String serviceUrl;
 		private String nameSpace;
 		
 		public ServiceModelMock(URL wsdlName) {
 			String wsdlUrlStr = wsdlName.toString();
 
 			this.serviceName = EXPECTED_SERVICE_NAME;
 			this.nameSpace = EXPECTED_NAMESPACE;
 			this.implementorClass = EXPECTED_IMPLEMENTOR_CLASS;
 			
 			if (wsdlUrlStr.endsWith(W_NO_SERVICE) 
 				|| wsdlUrlStr.endsWith(W_NON_EXISTING)
 				|| wsdlUrlStr.endsWith(W_HTTP)) {
 				throw new IllegalArgumentException(wsdlUrlStr + " is invalid");
 
 			} else if (wsdlUrlStr.endsWith(W_SOAP)
 					|| wsdlUrlStr.endsWith(W_HTTP_SOAP_SOAP12)
 					|| wsdlUrlStr.endsWith(W_SOAP_SOAP12_HTTP)) {
 				this.serviceUrl = EXPECTED_SOAP11_SERVICE_URL;
 				
 			} else if (wsdlUrlStr.endsWith(W_SOAP12)
 					|| wsdlUrlStr.endsWith(W_SOAP12_SOAP_HTTP)) {
 				this.serviceUrl = EXPECTED_SOAP12_SERVICE_URL;
 				
 			} else if (wsdlUrlStr.endsWith(W_TWO_SERVICES)) {
 				this.serviceName = EXPECTED_CARPORT_SERVICE_NAME;
 				this.serviceUrl = EXPECTED_CARPORT_SOAP_SERVICE_URL;
 				this.implementorClass = EXPECTED_CARPORT_IMPLEMENTOR_CLASS;
 			}
 
 			this.wsdlName = wsdlName;
 		}
 
 		public String getImplementorClass() {
 			return implementorClass;
 		}
 
 		public String getServiceName() {
 			return serviceName;
 		}
 
 		public String getServiceUrl() {
 			return serviceUrl;
 		}
 
 		public String getTargetNamespace() {
 			return nameSpace;
 		}
 	}
 
 	
 	/**
 	 * If a WSDL file cannot be found the service model throws an IllegalArgumentException 
 	 */
 	public void testNonExistingWsdl() throws Exception {
 		try {
 			new Wsdl4jModel(W_NON_EXISTING);
 			fail("Creating model for wsdl " + W_NON_EXISTING + " worked unexpectedly");
 		} catch(IllegalArgumentException iax) {
 			// it worked as expected
 		}
 	}
 
 	
 	/**
 	 * If there is no service in the wsdl the service model throws an IllegalArgumentException 
 	 */
 	public void testWsdlWithoutService() throws Exception {
 		try {
 			createServiceModelForWsdl(W_NO_SERVICE);
 			fail("Creating model for wsdl " + W_NO_SERVICE + " worked unexpectedly");
 		} catch(IllegalArgumentException iax) {
 			// ok
 		}
 	}
 
 	
 	/**
 	 * If the wsdl does not contain a service with a soap port the service model throws an IllegalArgumentException
 	 */
 	public void testWsdlWithoutSoapPorts() throws Exception {
 		try {
 			createServiceModelForWsdl(W_HTTP);
 			fail("Creating model for wsdl " + W_HTTP + " worked unexpectedly");
 		} catch(IllegalArgumentException iax) {
 			// ok
 		}
 	}
 
 	
 	/**
 	 * If the first service port is a soap 1.1 port, the model should deliver information for that port
 	 */
 	public void testFirstPortIsSoap11Port() throws Exception {
 		ServiceModel sm = createServiceModelForWsdl(W_SOAP);
 		assertEquals(EXPECTED_NAMESPACE, sm.getTargetNamespace());
 		assertEquals(EXPECTED_SOAP11_SERVICE_URL, sm.getServiceUrl());
 		assertEquals(EXPECTED_IMPLEMENTOR_CLASS, sm.getImplementorClass());
 		assertEquals(EXPECTED_SERVICE_NAME, sm.getServiceName());
 		
 		sm = createServiceModelForWsdl(W_HTTP_SOAP_SOAP12);
 		assertEquals(EXPECTED_NAMESPACE, sm.getTargetNamespace());
 		assertEquals(EXPECTED_SOAP11_SERVICE_URL, sm.getServiceUrl());
 		assertEquals(EXPECTED_IMPLEMENTOR_CLASS, sm.getImplementorClass());
 		assertEquals(EXPECTED_SERVICE_NAME, sm.getServiceName());
 		
 		sm = createServiceModelForWsdl(W_SOAP_SOAP12_HTTP);
 		assertEquals(EXPECTED_NAMESPACE, sm.getTargetNamespace());
 		assertEquals(EXPECTED_SOAP11_SERVICE_URL, sm.getServiceUrl());
 		assertEquals(EXPECTED_IMPLEMENTOR_CLASS, sm.getImplementorClass());
 		assertEquals(EXPECTED_SERVICE_NAME, sm.getServiceName());
 	}
 
 	
 	/**
 	 * If the first service port is a soap 1.2 port, the model should deliver information for that port
 	 */
 	public void testFirstPortIsSoap12Port() throws Exception {
 		ServiceModel sm = createServiceModelForWsdl(W_SOAP12);
 		assertEquals(EXPECTED_NAMESPACE, sm.getTargetNamespace());
 		assertEquals(EXPECTED_SOAP12_SERVICE_URL, sm.getServiceUrl());
 		assertEquals(EXPECTED_IMPLEMENTOR_CLASS, sm.getImplementorClass());
 		assertEquals(EXPECTED_SERVICE_NAME, sm.getServiceName());
/*		
 		sm = createServiceModelForWsdl(W_SOAP12_SOAP_HTTP);
 		assertEquals(EXPECTED_NAMESPACE, sm.getTargetNamespace());
 		assertEquals(EXPECTED_SOAP12_SERVICE_URL, sm.getServiceUrl());
 		assertEquals(EXPECTED_IMPLEMENTOR_CLASS, sm.getImplementorClass());
 		assertEquals(EXPECTED_SERVICE_NAME, sm.getServiceName());
*/
 	}
 
 	
 	/**
 	 * If there is more than one service, the model should deliver information for the first soap port in the first service
 	 */
 	public void testMoreThanOneService() throws Exception {
 		ServiceModel sm = createServiceModelForWsdl(W_TWO_SERVICES);
 		assertEquals(EXPECTED_NAMESPACE, sm.getTargetNamespace());
 		assertEquals(EXPECTED_CARPORT_SOAP_SERVICE_URL, sm.getServiceUrl());
 		assertEquals(EXPECTED_CARPORT_IMPLEMENTOR_CLASS, sm.getImplementorClass());
 		assertEquals(EXPECTED_CARPORT_SERVICE_NAME, sm.getServiceName());
 	}
 	
 	
 	private ServiceModel createServiceModelForWsdl(String wsdlPath) throws Exception {
 		String wsdlUrl = getClass().getResource(wsdlPath).toString();
 		ServiceModel res = new Wsdl4jModel(wsdlUrl);
 		return res;
 	}
 }
