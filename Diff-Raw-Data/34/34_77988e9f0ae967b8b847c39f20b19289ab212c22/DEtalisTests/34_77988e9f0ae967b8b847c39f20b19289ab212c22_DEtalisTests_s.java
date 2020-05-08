 package eu.play_project.dcep.distributedetalis.test;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.etsi.uri.gcm.util.GCM;
 import org.junit.After;
 import org.junit.Before;
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
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.graph.NodeFactory;
 
 import eu.play_project.dcep.api.DcepManagmentApi;
 import eu.play_project.dcep.api.DcepMonitoringApi;
 import eu.play_project.dcep.distributedetalis.api.ConfigApi;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisException;
 import eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi;
 import eu.play_project.dcep.distributedetalis.configurations.DetalisConfigLocal;
 import eu.play_project.play_platformservices.api.BdplQuery;
 import eu.play_project.play_platformservices.api.QueryDetails;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.Quadruple;
 
 public class DEtalisTests implements Serializable {
 
 	private static final long serialVersionUID = 100L;
 	private static DistributedEtalisTestApi distributedEtalisTestApi;
 	private static DcepManagmentApi dcepManagmentApi = null;
 	private static PublishApiSubscriber subscriber = null;
 	private static ConfigApi configApi = null;
 	static Component root;
 	private final Logger logger = LoggerFactory.getLogger(DEtalisTests.class);
 	DcepMonitoringApi dEtalis;
 
 
 	@Test
 	public void basicDEtalisComponentTest() throws IllegalLifeCycleException, NoSuchInterfaceException, ADLException, DistributedEtalisException, InterruptedException {
 		
 		/*
 		 *  Check if you get a reference to PublishApi and ManagementApi:
 		 */
 		if (distributedEtalisTestApi == null || dcepManagmentApi == null) {
 			fail("No DCEP Component");
 		}
 
 		/*
 		 *  Register a pattern:
 		 */
 		logger.info("Register pattern.");
 		
 		BdplQuery bdpl = BdplQuery.nonValidatingBuilder()
 				.details(new QueryDetails("queryId42"))
 				.ele("complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1), (xpath(element(sparqlFilter, [keyWord=O], []), //sparqlFilter(contains(@keyWord,'42')), _)))")
 				//.ele("complex(ID1, queryId42) do (generateConstructResult([S], ['http://play-project.eu/is/CepResult'], [O], ID)) <- 'http://events.event-processing.org/types/Event'(ID1) where (rdf(S, P, O, ID1))")
 				.bdpl("")
 				.build();
 		
 		dcepManagmentApi.registerEventPattern(bdpl);
 						
 		/*
 		 * Push an event:
 		 */
 		logger.info("Push events");
 		Quadruple event = new Quadruple(NodeFactory.createURI("id4710"),
 				NodeFactory.createURI("http://play-project.eu/Karlsruhe"),
 				NodeFactory.createURI("http://play-project.eu/is/CepResult"),
 				NodeFactory.createURI("http://play-project.eu/42"));
 
 		ArrayList<Quadruple> list = new ArrayList<Quadruple>();
 		list.add(event);
 
 		distributedEtalisTestApi.publish(new CompoundEvent(list));
 
 		Thread.sleep(1000);
 
 		/*
 		 * Check results:
 		 */
 		logger.info("Check results.");
 		Quadruple eventR = new Quadruple(NodeFactory.createURI("http://events.event-processing.org/ids/id4710"),
 				NodeFactory.createURI("http://play-project.eu/Karlsruhe"),
 				NodeFactory.createURI("http://play-project.eu/is/CepResult"),
 				NodeFactory.createURI("http://play-project.eu/42"));
 
 		if (subscriber.getComplexEvents() != null) {
 			assertTrue(subscriber.getComplexEvents().get(0).get(4).equals(eventR));
 		} else {
 			System.out.println("ERROR: No complex events in test 'checkComplexEvents()'.");
 			fail();
 		}
 	}
 
 	@After
 	public void shutDownComponents() {
 		
 		logger.info("Terminate components");
 		try {
 			// Stop is recursive...
 			GCM.getGCMLifeCycleController(root).stopFc();
 			
 			// Terminate is not recursive:
 			GCM.getGCMLifeCycleController(root).terminateGCMComponent();
 		} catch (IllegalLifeCycleException e) {
 			e.printStackTrace();
 		} catch (NoSuchInterfaceException e) {
 			e.printStackTrace();
 		}
 		
 		try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Before
 	public void instantiateDcepComponent()
 			throws IllegalLifeCycleException, NoSuchInterfaceException,
 			ADLException, DistributedEtalisException {
 
 		// CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue("proactive.java.policy");
 		CentralPAPropertyRepository.JAVA_SECURITY_POLICY.setValue(System
 				.getProperty("user.dir")
 				+ "\\src\\main\\resources\\proactive.java.policy");
 		CentralPAPropertyRepository.GCM_PROVIDER
 				.setValue("org.objectweb.proactive.core.component.Fractive");
 		// setProAktiveHome();
 
 		Factory factory = FactoryFactory.getFactory();
 		HashMap<String, Object> context = new HashMap<String, Object>();
 
 		// GCMApplication gcma = PAGCMDeployment.loadApplicationDescriptor(
 		// new
 		// URL("file:/"+System.getProperty("user.dir")+"/src/main/resources/applicationDescriptor.xml"));
 		// gcma.startDeployment();
 		// gcma.waitReady();
 		//
 		// context.put("deployment-descriptor", gcma);
 
 		root = (Component) factory.newComponent("DistributedEtalis", context);
 		GCM.getGCMLifeCycleController(root).startFc();
 
 		distributedEtalisTestApi = ((eu.play_project.dcep.distributedetalis.api.DistributedEtalisTestApi) root
 				.getFcInterface("DistributedEtalisTestApi"));
 
 		configApi = ((eu.play_project.dcep.distributedetalis.api.ConfigApi) root.getFcInterface("ConfigApi"));
 		configApi.setConfig(new DetalisConfigLocal("play-epsparql-clic2call-plus-tweet-historical-data.trig"));
 
 		dcepManagmentApi = ((eu.play_project.dcep.api.DcepManagmentApi) root.getFcInterface("DcepManagmentApi"));
 		
 		dEtalis = ((eu.play_project.dcep.api.DcepMonitoringApi) root.getFcInterface("DcepMonitoringApi"));
 
 		// Subscribe to get complex events.
 		try {
 			subscriber = PAActiveObject.newActive(PublishApiSubscriber.class,
 					new Object[] {});
 		} catch (ActiveObjectCreationException e) {
 			e.printStackTrace();
 		} catch (NodeException e) {
 			e.printStackTrace();
 		}
 
 		distributedEtalisTestApi.attach(subscriber);
 	}
 }
