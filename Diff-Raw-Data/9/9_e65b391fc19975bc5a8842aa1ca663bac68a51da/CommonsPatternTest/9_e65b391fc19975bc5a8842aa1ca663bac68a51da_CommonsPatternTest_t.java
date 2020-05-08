 package eu.play_project.dcep;
 
 import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.etsi.uri.gcm.util.GCM;
 import org.event_processing.events.types.UcTelcoCall;
 import org.junit.Test;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.Factory;
 import org.objectweb.fractal.api.Component;
 import org.objectweb.fractal.api.NoSuchInterfaceException;
 import org.objectweb.fractal.api.control.IllegalLifeCycleException;
 import org.objectweb.proactive.ActiveObjectCreationException;
 import org.objectweb.proactive.api.PAActiveObject;
 import org.objectweb.proactive.core.component.adl.FactoryFactory;
 import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
 import org.objectweb.proactive.core.node.NodeException;
 import org.ontoware.rdf2go.model.node.impl.URIImpl;
 
 import eu.play_project.dcep.distributedetalis.EventCloudHelpers;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 import eu.play_project.play_platformservices.api.QueryDispatchApi;
 import fr.inria.eventcloud.api.CompoundEvent;
 
 public class CommonsPatternTest {
 
 	public static QueryDispatchApi queryDispatchApi;
 	public static DistributedEtalisTestApi testApi;
 	boolean start = false;
 	static Component root;
 	public static boolean test;
 	private Logger logger = Logger.getAnonymousLogger();
 	
 	@Test
 	public void Clic2callPatternTest() throws IllegalLifeCycleException,
 			NoSuchInterfaceException, ADLException, InterruptedException {
 
 		String queryString;
 		
 		InstantiatePlayPlatform();
 
 		// Get query.
 		queryString = getSparqlQuerys("play-epsparql-clic2call.eprq");
 
 		// Compile query
 		String paternID = queryDispatchApi.registerQuery("abc", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		try {
 			subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		} catch (ActiveObjectCreationException e) {
 			e.printStackTrace();
 		} catch (NodeException e) {
 			e.printStackTrace();
 		}
 
 		testApi.attach(subscriber);
 	
 		logger.info("Publish evetns");
		for (int i = 0; i < 5; i++) {
 			CompoundEvent event = createTaxiUCCallEvent("http://example1.com/" + Math.random());
 			System.out.println(i);
 			//logger.fine("Publish event" +  event);
 			testApi.publish(event);
 		}
 
 		// Wait
 		delay();
 		delay();
 		delay();
 		delay();
 		delay();
 
 		System.out.println(subscriber);
 		System.out.println(subscriber.getComplexEvents().size());
 		assertTrue(subscriber.getComplexEvents().size()==9);
 		
 
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			for (Component subcomponent : GCM.getContentController(root)
 					.getFcSubComponents()) {
 				logger.info("Terminating component: "
 						+ subcomponent.getFcType());
 				GCM.getGCMLifeCycleController(subcomponent)
 						.terminateGCMComponent();
 			}
 			
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void Clic2callPatternPlusTweetTest() throws IllegalLifeCycleException,
 			NoSuchInterfaceException, ADLException, InterruptedException {
 
 		String queryString;
 		
 		InstantiatePlayPlatform();
 
 		// Get query.
 		queryString = getSparqlQuerys("play-epsparql-clic2call-plus-tweet.eprq");
 
 		// Compile query
 		String paternID = queryDispatchApi.registerQuery("example", queryString);
 		
 		
 		//Subscribe to get complex events.
 		SimplePublishApiSubscriber subscriber = null;
 		try {
 			subscriber = PAActiveObject.newActive(SimplePublishApiSubscriber.class, new Object[] {});
 		} catch (ActiveObjectCreationException e) {
 			e.printStackTrace();
 		} catch (NodeException e) {
 			e.printStackTrace();
 		}
 		
 		testApi.attach(subscriber);
 		
 		
 		logger.info("Publish evetns");
 		for (int i = 0; i < 10; i++) {
 			CompoundEvent event = createTaxiUCCallEvent("example" + Math.random());
 			logger.fine("Publish event" +  event);
 			testApi.publish(event);
 		}
 
 		// Wait
 		delay();
 
 		assertTrue(subscriber.getComplexEvents().size()==9);
 	
 		
 		
 		// Stop and terminate GCM Components
 		try {
 			GCM.getGCMLifeCycleController(root).stopFc();
 			// Terminate all subcomponents.
 			 for(Component subcomponent : GCM.getContentController(root).getFcSubComponents()){
 				GCM.getGCMLifeCycleController(subcomponent).terminateGCMComponent();
 			 }
 
 			
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 }
 
 	public void sendEvents(){
 		start = true;
 		System.out.println("Start Producer");
 		System.out.println("Send 2000 Events \n\n\n\n\n\n\n");
 		
 	}
 	
 	public static CompoundEvent createTaxiUCCallEvent(String eventId){
 		
 		UcTelcoCall event = new UcTelcoCall(
 				// set the RDF context part
 				EventHelpers.createEmptyModel(eventId),
 				// set the RDF subject
 				eventId + EVENT_ID_SUFFIX,
 				// automatically write the rdf:type statement
 				true);
 
 		// Run some setters of the event
 		event.setUcTelcoCalleePhoneNumber("49123456789");
 		event.setUcTelcoCallerPhoneNumber("49123498765");
 		event.setUcTelcoDirection("incoming");
 		
 		// Create a Calendar for the current date and time
 		event.setEndTime(Calendar.getInstance());
 		event.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));
 
 		//Push events.
 		return EventCloudHelpers.toCompoundEvent(event);
 	}
 
 	public static void InstantiatePlayPlatform()
 			throws IllegalLifeCycleException, NoSuchInterfaceException,
 			ADLException {
 
 		CentralPAPropertyRepository.JAVA_SECURITY_POLICY
 				.setValue("proactive.java.policy");
 
 		CentralPAPropertyRepository.GCM_PROVIDER
 				.setValue("org.objectweb.proactive.core.component.Fractive");
 
 		
 		Factory factory = FactoryFactory.getFactory();
 		HashMap<String, Object> context = new HashMap<String, Object>();
 
 		root = (Component) factory.newComponent("EcDcepPsTest", context);
 		GCM.getGCMLifeCycleController(root).startFc();
 
 		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root.getFcInterface("QueryDispatchApi"));
 		testApi = (DistributedEtalisTestApi) root.getFcInterface("DistributedEtalisTestApi");
 		
 		try {
 			Thread.sleep(10000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private String getSparqlQuerys(String queryFile){
 		try {
 			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
 			BufferedReader br =new BufferedReader(new InputStreamReader(is));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			
 			while (null != (line = br.readLine())) {
 					sb.append(line);
 					sb.append("\n");
 			}
 			//System.out.println(sb.toString());
 			br.close();
 			is.close();
 
 			return sb.toString();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	
 	}
 	
 	private void delay(){
 		try {
 			Thread.sleep(8000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
