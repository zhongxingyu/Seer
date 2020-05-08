 package org.eclipse.swordfish.core.httproundtrip.test;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ScheduledExecutorService;
 
 import javax.xml.transform.Source;
 
 import org.apache.cxf.BusFactory;
 import org.apache.cxf.binding.BindingFactoryManager;
 import org.apache.cxf.binding.soap.SoapBindingFactory;
 import org.apache.cxf.transport.ConduitInitiatorManager;
 import org.apache.cxf.transport.http.ClientOnlyHTTPTransportFactory;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.nmr.api.Endpoint;
 import org.apache.servicemix.nmr.api.NMR;
 import org.apache.servicemix.nmr.core.ExchangeImpl;
 import org.eclipse.swordfish.core.test.util.OsgiSupport;
 import org.eclipse.swordfish.core.test.util.SwordfishTestCase;
 import org.eclipse.swordfish.core.test.util.domain.ClientUtil;
 import org.eclipse.swordfish.core.test.util.domain.HelloWorld;
 import org.eclipse.swordfish.internal.core.util.smx.SimpleClient;
 import org.junit.Test;
 
 public class HttpRoundtripScenarioTest extends SwordfishTestCase {
 	private  ScheduledExecutorService scheduler;
 	private boolean checkHttpEndpointsStarted() {
         NMR nmr = OsgiSupport.getReference(bundleContext, NMR.class);
         Map<String, String> props = new HashMap<String, String>();
         props.put(Endpoint.ENDPOINT_NAME, "httpConsumerEndpoint");
         List<Endpoint> endpoints1 = nmr.getEndpointRegistry().query(props);
         props.put(Endpoint.ENDPOINT_NAME, "cxfEndpointHttpProvider");
         List<Endpoint> endpoints2 = nmr.getEndpointRegistry().query(props);
         return endpoints1.size() == 1 && endpoints2.size() == 1;
     }
 
    /* @Override
 	protected List<Pattern> getExcludeBundlePatterns() {
         return Arrays.asList(
         	Pattern.compile("org\\.eclipse\\.osgi_.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.samples\\.conf.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.samples\\.bpel.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.samples\\.dynamic.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.samples\\.jaxws.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.samples\\.exception.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.plugins\\.resolver\\.policy.*"),
         	Pattern.compile("org\\.eclipse\\.swordfish\\.plugins\\.ws\\.wsdlgenerator\\.t.*"));
     }*/
 
 
 
 
 
     private void waitUntilEndpointsStarted() throws InterruptedException {
     	boolean started = false;
 		for (int i = 0; i < 15; i++) {
 			started = checkHttpEndpointsStarted();
 			if (started) {
 				break;
 			}
 			System.out.println("sleep");
 			Thread.sleep(1000);
 		}
         assertTrue("Endpoints have not been started", started);
     }
 
 
     @Test
     public void test0JustEmptyTest() throws Exception {
     }
 
 
     @Test
     public void test1Provider() throws Exception {
     	waitUntilEndpointsStarted();
     	/*for (Bundle bundle : bundleContext.getBundles()) {
         	//System.out.println(bundle.getSymbolicName());
         	if (bundle.getSymbolicName().contains(".resolver.policy_")) {
         		System.out.println("!!!!Stoping bundle");
         		bundle.stop();
         		Thread.sleep(1000);
         	}
         }*/
     	//http://schemas.xmlsoap.org/wsdl/soap/
     	BindingFactoryManager bindingFactoryManager = 	BusFactory.getThreadDefaultBus().getExtension(BindingFactoryManager.class);
     	SoapBindingFactory soapBindingFactory = new SoapBindingFactory();
     	soapBindingFactory.setBus(BusFactory.getThreadDefaultBus());
     	soapBindingFactory.setActivationNamespaces(Arrays.asList("http://schemas.xmlsoap.org/wsdl/soap/", "http://schemas.xmlsoap.org/soap/",
                 "http://schemas.xmlsoap.org/wsdl/soap/",
                 "http://schemas.xmlsoap.org/wsdl/soap12/",
                 "http://www.w3.org/2003/05/soap/bindings/HTTP/",
                 "http://schemas.xmlsoap.org/wsdl/soap/http</"
             ));
     	bindingFactoryManager.registerBindingFactory("http://schemas.xmlsoap.org/wsdl/soap/", soapBindingFactory);
     	ConduitInitiatorManager conduitInitiatorManager = BusFactory.getThreadDefaultBus().getExtension(ConduitInitiatorManager.class);//.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", new HttpBindingFactory());
         ClientOnlyHTTPTransportFactory clientOnlyHTTPTransportFactory = new ClientOnlyHTTPTransportFactory();
         clientOnlyHTTPTransportFactory.setBus(BusFactory.getThreadDefaultBus());
         conduitInitiatorManager.registerConduitInitiator("http://schemas.xmlsoap.org/soap/http", clientOnlyHTTPTransportFactory);
         LOG.info("conduitInitiatorManager = " + conduitInitiatorManager);
         String wsdlUrl = HelloWorld.class.getClassLoader().getResource("HelloWorld.wsdl").toString();
         Thread.currentThread().setContextClassLoader(HttpRoundtripScenarioTest.class.getClassLoader());
         HelloWorld helloWorld = ClientUtil.createWebServiceStub(HelloWorld.class, wsdlUrl, "http://localhost:8196/testsample/");
         assertEquals("Hello Swordfish", helloWorld.sayHi("Swordfish"));
     }
 
 
    @Test
    public void test2RoundTrip() throws Exception {
         waitUntilEndpointsStarted();
 
         SimpleClient simpleClient = new SimpleClient();
         simpleClient.setNmr(OsgiSupport.getReference(bundleContext, NMR.class));
         simpleClient.setUriToSend("http://localhost:8196/testsample/");
         simpleClient.setDelayBeforeSending(0);
         simpleClient.setDataToSend("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:sayHi xmlns:ns2=\"http://domain.util.test.core.swordfish.eclipse.org/\"><arg0>Swordfish</arg0></ns2:sayHi></soap:Body></soap:Envelope>");
         simpleClient.setTargetEndpointName("cxfEndpointHttpProvider");
         LOG.info("Sending second request");
         ExchangeImpl exchangeImpl = simpleClient.performSynchronousRequest();
         assertNotNull(exchangeImpl);
         String response = new SourceTransformer().toString(exchangeImpl.getOut().getBody(
                 Source.class));
         checkHttpEndpointsStarted();
         LOG.info("!!!" + exchangeImpl.getOut());
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><ns2:sayHiResponse xmlns:ns2=\"http://domain.util.test.core.swordfish.eclipse.org/\" xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\"><return>Hello Swordfish</return></ns2:sayHiResponse>", response);
     }
 
 
 
 }
