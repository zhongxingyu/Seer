 package eu.play_project.dcep;
 
 import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedList;
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
 
 import com.hp.hpl.jena.graph.Node;
 
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import eu.play_project.dcep.distributedetalis.utils.EventCloudHelpers;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 import eu.play_project.play_platformservices.api.QueryDispatchApi;
 import eu.play_project.play_platformservices.api.QueryDispatchException;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.Quadruple;
 
 public class CommonsPatternTest {
 
 	public static QueryDispatchApi queryDispatchApi;
 	public static DistributedEtalisTestApi testApi;
 	boolean start = false;
 	static Component root;
 	public static boolean test;
 	private final Logger logger = Logger.getAnonymousLogger();
 	
 	@Test
 	public void Clic2callPatternTest() throws IllegalLifeCycleException,
 			NoSuchInterfaceException, ADLException, InterruptedException, QueryDispatchException {
 
 		String queryString;
 		
 		InstantiatePlayPlatform();
 
 		// Get query.
 		queryString = getSparqlQueries("play-epsparql-clic2call.eprq");
 
 		// Compile query
 		String paternID1 = queryDispatchApi.registerQuery("abc0", queryString);
 		
 		
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
 			CompoundEvent event = createTaxiUCCallEvent("example1" + Math.random());
 			testApi.publish(event);
 		}
 
 		// Wait
 		delay();
 
 		assertTrue(subscriber.getComplexEvents().size()==3);
 		
 
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
 
 	/**
 	 * One events contains multiple topics a person is talking about.
 	 */
 	@Test
 	public void setOperationTest() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException{
 	String queryString;
 		
 		InstantiatePlayPlatform();
 
 		// Get query.
 		queryString = getSparqlQueries("play-bdpl-all-topics-he-talks-about-setoperation-example.eprq");
 
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
 			CompoundEvent event = createFacebookTopicEvent("example1" + Math.random());
 			testApi.publish(event);
 		}
 
 		// Wait
 		delay();
 		
 		//Contains coffee and tea.
 		System.out.println();
 		assertTrue(subscriber.getComplexEvents().size()==5);
 		assertEquals(subscriber.getComplexEvents().get(0).getTriples().get(7).getMatchObject().toString(), "\"Tea\"");
 		assertEquals(subscriber.getComplexEvents().get(0).getTriples().get(8).getMatchObject().toString(), "\"Coffee\"");
 	}
 	//@Test
 	public void Clic2callPatternPlusTweetTest() throws IllegalLifeCycleException,
 			NoSuchInterfaceException, ADLException, InterruptedException, QueryDispatchException {
 
 		String queryString;
 		
 		InstantiatePlayPlatform();
 
 		// Get query.
 		queryString = getSparqlQueries("play-epsparql-clic2call-plus-tweet.eprq");
 
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
 
 	/**
 	 * Aggregate values in time-window. 
 	 * A complex event is created if the average value is >=5 in a 5s window. 
 	 * Event e1,e2,e3 are in window w1 and the average is > 5. 
 	 * For event e4 the average is < 5, because e1 is out of window.
 	 */
 	@Test
 	public void AggregateAverageWindSpeedTest() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, QueryDispatchException, InterruptedException{
 		String queryString;
 		
 		InstantiatePlayPlatform();
 
 		// Get query.
 		queryString = getSparqlQueries("patterns/wether_wind_speed.eprq");
 
 		// Compile query
 		queryDispatchApi.registerQuery("example", queryString);
 		
 		
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
 
 		testApi.publish(createWeatherEvent("example1" + Math.random(), 20)); // e1 start window w1.
 		testApi.publish(createWeatherEvent("example1" + Math.random(), 1));  // e2
 		Thread.sleep(3000);													 // wait 3s
 		testApi.publish(createWeatherEvent("example1" + Math.random(), 1));  // e3
 		Thread.sleep(2000);													 // wait 2s
 		testApi.publish(createWeatherEvent("example1" + Math.random(), 1));  // e4 is out of window w1.
 	
 		
 		// Wait
 		delay();
System.out.println(subscriber.getComplexEvents().size());
 		assertTrue(subscriber.getComplexEvents().size()==3);
 		
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
 		System.out.println("Send 2000 Events ");
 		
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
 		
 		double longitude = 123;
 		double latitude = 345;
 		EventHelpers.setLocationToEvent(event, longitude, latitude);
 		
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
 
 		root = (Component) factory.newComponent("PsDcepComponent", context);
 		GCM.getGCMLifeCycleController(root).startFc();
 
 		queryDispatchApi = ((eu.play_project.play_platformservices.api.QueryDispatchApi) root.getFcInterface("QueryDispatchApi"));
 		testApi = (DistributedEtalisTestApi) root.getFcInterface("DistributedEtalisTestApi");
 		
 		try {
 			Thread.sleep(10000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static CompoundEvent createWeatherEvent(String eventId, double value) {
 
 		LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 
 		Quadruple q1 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://events.event-processing.org/types/stream"),
 				Node.createURI("http://streams.event-processing.org/ids/Srbench#stream"));
 
 		Quadruple q3 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e2"),
 				Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#WindSpeedObservation"));
 
 		Quadruple q4 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e2"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#observedProperty"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/weather.owl#_WindSpeed"));
 
 		Quadruple q5 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e2"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"));
 
 		Quadruple q6 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#ffff"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
 				Node.createURI(value + ""));
 
 //		Quadruple q7 = new Quadruple(
 //				Node.createURI("http://events.event-processing.org/eventId/"
 //						+ eventId),
 //				Node.createURI("http://prefix.example.com/e1"),
 //				Node.createURI("http://events.event-processing.org/types/endTime"),
 //				Node.createURI(new SimpleDateFormat(
 //						eu.play_project.play_commons.constants.Event.DATE_FORMAT_8601)
 //						.format(new Date())));
 		Quadruple q7 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://events.event-processing.org/types/endTime"),
 				Node.createURI(System.currentTimeMillis() + ""));
 
 		Quadruple q8 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"
 						+ eventId),
 				Node.createURI("http://prefix.example.com/e2"),
 				Node.createURI("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#procedure"),
 				Node.createURI("http://sensor.example.com/S1"));
 
 		quads.add(q1);
 		// quads.add(q2);
 		quads.add(q3);
 		quads.add(q4);
 		quads.add(q5);
 		quads.add(q6);
 		quads.add(q7);
 		quads.add(q8);
 
 		return new CompoundEvent(quads);
 	}
 	
 	public static CompoundEvent createFacebookTopicEvent(String eventId) {
 
 		LinkedList<Quadruple> quads = new LinkedList<Quadruple>();
 
 		Quadruple q1 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/" + eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://events.event-processing.org/types/stream"),
 				Node.createURI("http://streams.event-processing.org/ids/FacebookStatusFeed#stream"));
 
 		Quadruple q3 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"+ eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
 				Node.createURI("http://events.event-processing.org/types/FacebookStatusFeedEvent"));
 
 		Quadruple q4 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"+ eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://events.event-processing.org/types/status"),
 				Node.createURI("Tea"));
 		
 		Quadruple q5 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"+ eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://events.event-processing.org/types/status"),
 				Node.createURI("Coffee"));
 
 		Quadruple q6 = new Quadruple(
 				Node.createURI("http://events.event-processing.org/eventId/"+ eventId),
 				Node.createURI("http://prefix.example.com/e1"),
 				Node.createURI("http://graph.facebook.com/schema/user#name"),
 				Node.createURI("Max"));
 	
 		quads.add(q1);
 		quads.add(q3);
 		quads.add(q4);
 		quads.add(q5);
 		quads.add(q6);
 
 		return new CompoundEvent(quads);
 	}
 
 	private String getSparqlQueries(String queryFile){
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
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
