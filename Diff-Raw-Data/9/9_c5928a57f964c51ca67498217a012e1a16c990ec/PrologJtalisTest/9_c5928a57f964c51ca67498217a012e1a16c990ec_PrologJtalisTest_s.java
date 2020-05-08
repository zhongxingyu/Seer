 package eu.play_project.dcep.distributedetalis.test;
 
 import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
 import static eu.play_project.play_commons.constants.Namespace.EVENTS;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.regex.Pattern;
 
 import jpl.Query;
 import jpl.Term;
 
 import org.event_processing.events.types.AvgTempEvent;
 import org.junit.Test;
 import org.ontoware.rdf2go.model.node.impl.URIImpl;
 
 import com.hp.hpl.jena.graph.Node;
 import com.jtalis.core.JtalisContextImpl;
 import com.jtalis.core.event.AbstractJtalisEventProvider;
 import com.jtalis.core.event.EtalisEvent;
 import com.jtalis.core.plengine.PrologEngineWrapper;
 
 import eu.play_project.dcep.distributedetalis.EventCloudHelpers;
 import eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper;
 import eu.play_project.dcep.distributedetalis.PrologSemWebLib;
 import eu.play_project.dcep.distributedetalis.api.UsePrologSemWebLib;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 import fr.inria.eventcloud.api.CompoundEvent;
 import fr.inria.eventcloud.api.Quadruple;
 
 public class PrologJtalisTest {
 	
 	public static JtalisContextImpl ctx;
 	public static UsePrologSemWebLib prologSemWebLib;
 	public static EtalisEvent result;
 	private static Random random = new Random();
 	
 	/**
 	 * Instantiate ETALIS
 	 * @throws InterruptedException 
 	 */
 	@Test
 	public void instantiateJtalis() throws InterruptedException{
 
 		PrologEngineWrapper<?> engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
 		this.ctx = new JtalisContextImpl(engine);
 		
 		ctx.getEngineWrapper().executeGoal("reset_ETALIS");
 		Thread.sleep(3000);
 		
 	}
 	
 	
 	/**
 	 * Instantiate ETALIS and register eventpatterns.
 	 */
 	@Test
 	public void registerEventpatterns(){
 		
 		//Result should be overwritten by an complex event from etalis.
 		result = new EtalisEvent("complex", 42);
 		
 		ctx.registerOutputProvider(new AbstractJtalisEventProvider() {
 			@Override
 			public void outputEvent(EtalisEvent event) {
 				result = event;
 			}
 		});
 		//ctx.addEventTrigger("complex"); // Which events are printed (_ means all)
 		ctx.addDynamicRule("complex(X,'id') <- a(X) seq b(X)");
 		
 		//ctx.pushEvent(new EtalisEvent("a", 1));
 		//ctx.pushEvent(new EtalisEvent("b", 1));
 	
 		delay();
 System.out.println("fffffffffffffffffff" + result);
 System.out.println(new EtalisEvent("complexExample", 1,"'id'"));
 
 		assertTrue(result.equals(new EtalisEvent("complexExample", 1,"id")));
 	}
 	
 	/**
 	 * No event will be generated because b appears to late.
 	 */
 	@Test
 	public void registerEventpatternsWithWindow(){
 		//Result should not be overwritten by an complex event from etalis.
 		result = new EtalisEvent("complex", 42);
 		
 		ctx.registerOutputProvider(new AbstractJtalisEventProvider() {
 			@Override
 			public void outputEvent(EtalisEvent event) {
 				result = event;
 			}
 		});
 
 		ctx.addEventTrigger("_"); // Which events are printed (_ means all)
 		ctx.addDynamicRuleWithId("r2([property(event_rule_window,1)])", "complex2(X) <- a(X) seq b(X)");
 		
 		ctx.pushEvent(new EtalisEvent("a", 1));
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		ctx.pushEvent(new EtalisEvent("b", 1));
 	
 		delay();
 		System.out.println(result);
 		assertFalse(result.equals(new EtalisEvent("complex2", 1)));
 	}
 	
 //	@Test
 //	public void loadSemWebLibTest(){
 //		try{
 //			// Load SWI-Prolog Semantic Web Library
 //			ctx.getEngineWrapper().executeGoal("[library(semweb/rdf_db)]");
 //		}catch (Exception e) {
 //			e.printStackTrace();
 //			fail("It was not possible to load the semweb/rdf_db library ");
 //		}
 //
 //		
 //		delay();
 //	}
 	
 	@Test
 	public void instantiatePrologSemWebLib(){
 
 		prologSemWebLib = new PrologSemWebLib();
 		prologSemWebLib.init(ctx);
 		
 		delay();
 	}
 	
 	
 	@Test
 	public void addEventsInTriplestore(){
 
 		// Create an event ID used in RDF context and RDF subject
 		String eventId = EVENTS.getUri() + "e" + Math.abs(random.nextLong());
 
 		AvgTempEvent event = new AvgTempEvent(
 				// set the RDF context part
 				EventHelpers.createEmptyModel(eventId),
 				// set the RDF subject
 				eventId + EVENT_ID_SUFFIX,
 				// automatically write the rdf:type statement
 				true);
 
 		// Run some setters of the event
 		// Create a Calendar for the current date and time
 		event.setEndTime(Calendar.getInstance());
 		// Set a dummy stream
 		event.setStream(new URIImpl(Stream.FacebookStatusFeed.getUri()));
 
 		
 		// New event.
 		CompoundEvent event1 = EventCloudHelpers.toCompoundEvent(event);
 		
 		try {
 			prologSemWebLib.addEvent(event1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		delay();
 	}
 	
 	/**
 	 * Read data from prolog triplestore. (directly not using PrologSemWebLib class).
 	 */
 	@Test
 	public void getEventsFromTriplestore(){
 		
 		/*
 		 *  Insert data in triplesore
 		 */
 		
 		// Create an event ID used in RDF context and RDF subject
 		String eventId = EVENTS.getUri() + "e" + Math.abs(random.nextLong());
 
 		AvgTempEvent event = new AvgTempEvent(
 				// set the RDF context part
 				EventHelpers.createEmptyModel(eventId),
 				// set the RDF subject
 				eventId + EVENT_ID_SUFFIX,
 				// automatically write the rdf:type statement
 				true);
 
 		// Run some setters of the event
 		// Create a Calendar for the current date and time
 		event.setEndTime(Calendar.getInstance());
 		// Set a dummy stream
 		event.setStream(new URIImpl(Stream.FacebookStatusFeed.getUri()));
 				
 		try {
 			prologSemWebLib.addEvent(EventCloudHelpers.toCompoundEvent(event));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		/*
 		 * Get data back from triplestore
 		 */
 		Query q = new Query(String.format("rdf(S,P,O,'\"%s\"')", eventId));
 		
 	//	assertTrue("The return answer from triplestore should be non-empty.", q.hasMoreElements());
 
 		if( q.hasMoreElements() ) {
 			Hashtable binding = (Hashtable) q.nextElement();
 			Term t = (Term) binding.get("S");
 			System.out.println(t);
 			assertTrue(t.toString().equals("'" + eventId + EVENT_ID_SUFFIX + "'"));
 			
 			q.close(); // To avoid transaction problems with rdf db.
 		}
 
 	}
 	
 	
 	
 	// @Test TODO activate test.
 	public void generateCartesinProductOfTriples(){
 		PlayJplEngineWrapper.getPlayJplEngineWrapper().executeGoal("generateConstructResult([s1,s2],[p],[o1,o2],testDb2)");;
 		
 		// Test event
 		EtalisEvent event = new EtalisEvent("a", "testDb2");
 		event.setRuleID("498929293");
 		//TODO No static variable
 		CompoundEvent result = null; //= new CompoundEvent(JtalisOuitputProvider.getEventData(PlayJplEngineWrapper.getPlayJplEngineWrapper(), event));
 		
 		// Event to compare with result
 		List<Quadruple> quadruple = new ArrayList<Quadruple>();
 		
 		Quadruple q1 = new Quadruple(
 				Node.createURI("'testDb2'"),
 				Node.createURI("s1"),
                 Node.createURI("p"), 
                 Node.createURI("o1"));
 		quadruple.add(q1);
 		
 		q1 = new Quadruple(
 				Node.createURI("'testDb2'"),
 				Node.createURI("s1"),
                 Node.createURI("p"), 
                 Node.createURI("o2"));
 		quadruple.add(q1);
 		
 		q1 = new Quadruple(
 				Node.createURI("'testDb2'"),
 				Node.createURI("s2"),
                 Node.createURI("p"), 
                 Node.createURI("o1"));
 		quadruple.add(q1);
 		
 		q1 = new Quadruple(
 				Node.createURI("'testDb2'"),
 				Node.createURI("s2"),
                 Node.createURI("p"), 
                 Node.createURI("o2"));
 		quadruple.add(q1);
 		
 		q1 = new Quadruple(
 				Node.createURI("'testDb2'"),
 				Node.createURI("false"),
                 Node.createURI("false"), 
                 Node.createURI("false"));
 		quadruple.add(q1);
 		
 		CompoundEvent original = new CompoundEvent(quadruple);
 
 		
 		System.out.println(result);
 		
 		System.out.println((result.getQuadruples().get(3) + "\t" + original.getQuadruples().get(0)));
 
 		assertTrue(result.getQuadruples().get(3).equals(original.getQuadruples().get(0)));
 		assertTrue(result.getQuadruples().get(4).equals(original.getQuadruples().get(1)));
 		assertTrue(result.getQuadruples().get(5).equals(original.getQuadruples().get(2)));
 		assertTrue(result.getQuadruples().get(6).equals(original.getQuadruples().get(3)));
 		assertFalse(result.getQuadruples().get(4).equals(original.getQuadruples().get(4)));
 		
 		delay();
 	}
 	
 	/**
 	 * Load methods from file and add it to prolog.
 	 * @throws InterruptedException 
 	 */
 	@Test
 	public void loadPrologMethods() throws InterruptedException{
 		if(ctx == null) instantiateJtalis();
 		
 		String[] methods = getPrologMethods("constructQueryImp.pl");
 		
 		for (int i = 0; i < methods.length; i++) {
 			System.out.println( methods[i]);
 			ctx.getEngineWrapper().executeGoal(("assert(" +  methods[i] + ")"));
 		}
 		
 		methods = getPrologMethods("ReferenceCounting.pl");
 		
 		for (int i = 0; i < methods.length; i++) {
 			System.out.println( methods[i]);
 			ctx.getEngineWrapper().executeGoal(("assert(" +  methods[i] + ")"));
 		}
 		
 		methods = getPrologMethods("Measurement.pl");
 		
 		for (int i = 0; i < methods.length; i++) {
 			System.out.println( methods[i]);
 			ctx.getEngineWrapper().executeGoal(("assert(" +  methods[i] + ")"));
 		}
 		
 		methods = getPrologMethods("Math.pl");
 		
 		for (int i = 0; i < methods.length; i++) {
 			System.out.println( methods[i]);
 			ctx.getEngineWrapper().executeGoal(("assert(" +  methods[i] + ")"));
 		}
 		
 	}
 	
 //	@Test
 //	public void instantiateDistributedEtalis(){
 //		DistributedEtalis dE = new DistributedEtalis("de1");
 //		
 //		delay();
 //	}
 	
 //	@Test
 //	public void TestMathOperationsWithTriplestoreData(){
 		
 //		((PlayJplEngineWrapper)etalis.getEngineWrapper()).execute("rdf_assert('1', '1', '1', '1')", true);
 //		((PlayJplEngineWrapper)etalis.getEngineWrapper()).execute("rdf_assert('2', '2', '2', '2')", true);
 //
 //		//Query q = new Query("rdf_assert('1', '1', A, '1'), rdf('2', '2', B, '2'), minus(Result, A, B)");
 //		Query q = new Query(" minus(Result, '1', '2')");
 //		if( q.hasMoreElements() ) {
 //			Hashtable binding = (Hashtable) q.nextElement();
 //			Term t = (Term) binding.get("Result");
 //			System.out.println(t);
 //			assertTrue(t.toString().equals("-1"));
 //			
 //			q.close();
 //		}
 		
 //	}
 	
 //	/**
 //	 * Read data from prolog triplestore. (Using PrologSemWebLib class).
 //	 */
 //	@Test
 //	public void getEventsFromTriplestorePrologSemWebLibClass(){
 //		
 //		// Test event
 //		EtalisEvent event = new EtalisEvent("a", "testDb2");
 //		event.setRuleID("498929293");
 //		
 //		CompoundEvent result = new CompoundEvent(JtalisOutputProvider.getEventData(PlayJplEngineWrapper.getPlayJplEngineWrapper(), event));
 //		
 //		assertTrue((result.getQuadruples().get(3).getSubject().toString().equals("s1")));
 //		delay();
 //	}
 	
 	@Test
 	public void GetVariableValues(){
 		
 		if(ctx==null){
 			this.init();
 		}
 		String[] expectedResult = {"'A'","aa1", "aa2", "aa3", "'B'", "bb1", "bb2"};
 		
 		//Save variables and values in Prolog
 		((PlayJplEngineWrapper)ctx.getEngineWrapper()).executeGoal("assert(variableValues('http://patterId.example.com/1234', 'A', 'aa1'))");
 		((PlayJplEngineWrapper)ctx.getEngineWrapper()).executeGoal("assert(variableValues('http://patterId.example.com/1234', 'A', 'aa2'))");
 		((PlayJplEngineWrapper)ctx.getEngineWrapper()).executeGoal("assert(variableValues('http://patterId.example.com/1234', 'A', 'aa3'))");
 		((PlayJplEngineWrapper)ctx.getEngineWrapper()).executeGoal("assert(variableValues('http://patterId.example.com/1234', 'B', 'bb1'))");
 		((PlayJplEngineWrapper)ctx.getEngineWrapper()).executeGoal("assert(variableValues('http://patterId.example.com/1234', 'B', 'bb2'))");
 		
 		//Get variables and values
 		Hashtable<String, Object>[] result = ((PlayJplEngineWrapper)ctx.getEngineWrapper()).execute("variableValues('http://patterId.example.com/1234', VarName, VarValue)");
 		
 		//HashMap with values of variables.
 		Map<String, List<String>> variabelValues = new HashMap<String, List<String>>();
 		
 		// Get all values of a variable
 		for (Hashtable<String, Object> hashtable : result) {
 			if(!variabelValues.containsKey(hashtable.get("VarName").toString())){
 				variabelValues.put(hashtable.get("VarName").toString(), new ArrayList<String>());
 			}
 			
 			// Add new value to list.
 			variabelValues.get(hashtable.get("VarName").toString()).add(hashtable.get("VarValue").toString());
 		}
 		
 		int indexOfExpectedResult = 0;
 		for (String hashKey : variabelValues.keySet()) {
 
 			assertTrue(expectedResult[indexOfExpectedResult].equals(hashKey));
 			indexOfExpectedResult++;
 			for (String variableValue : variabelValues.get(hashKey)) {
 
 				assertTrue(expectedResult[indexOfExpectedResult].equals(variableValue));
 				indexOfExpectedResult++;
 			}
 			
 		}
 	}
 	
 //	/**
 //	 * Simulate performance measurement and test results.
 //	 */
 //	@Test
 //	public void MeasurementTrheadTest(){
 //		
 //		//New prolog engine
 //		PlayJplEngineWrapper engine = PlayJplEngineWrapper.getPlayJplEngineWrapper();
//		engine.consult(System.getProperty("user.dir") + "/src/main/resources/PrologMethods/Measurement.pl");
 //		ExecutorService measureExecutor = Executors.newCachedThreadPool();
 //		
 //		// New task. Measure 5s.
 //		MeasurementThread task = new MeasurementThread(5000, engine, null); //TODO change
 //
 //		// Execute task.
 //		Future<MeasuringResult> future = measureExecutor.submit(task);
 //		
 //		// Simulate events.
 //		for(int i=0; i<10; i++){
 //			
 //			engine.executeGoal("measure('http://example.com/patternID1')");
 //			if(i%2==0){
 //				engine.executeGoal("measure('http://example.com/patternID2')");
 //			}
 //			if(i%3==0){
 //				engine.executeGoal("measure('http://example.com/patternID3')");
 //			}
 //			
 //			delay();
 //		}
 //
 //
 //		NodeMeasuringResult measuredValues = null;
 //		try {
 //			measuredValues = (NodeMeasuringResult) future.get();
 //		} catch (InterruptedException e) {
 //			e.printStackTrace();
 //		} catch (ExecutionException e) {
 //			e.printStackTrace();
 //		}
 //		
 //
 //		assertTrue(measuredValues.getMeasuredValues().get(1).getProcessedEvents()==4);
 //		assertTrue(measuredValues.getMeasuredValues().get(2).getProcessedEvents()==2);
 //		assertTrue(measuredValues.getMeasuredValues().get(3).getProcessedEvents()==1);
 //	}
 	
 	public static void delay(){
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void init(){
 		PlayJplEngineWrapper engine = eu.play_project.dcep.distributedetalis.PlayJplEngineWrapper.getPlayJplEngineWrapper();
 		this.ctx = new JtalisContextImpl(engine);
 
 		// FIXME use zipped files like Vesko
		engine.consult(System.getProperty("user.dir") + "/src/main/resources/PrologMethods/constructQueryImp.pl");
		engine.consult(System.getProperty("user.dir") + "/src/main/resources/PrologMethods/ReferenceCounting.pl");
		engine.consult(System.getProperty("user.dir") + "/src/main/resources/PrologMethods/Measurement.pl");
 	}
 	
 	private String[] getPrologMethods(String methodFile){
 		try {
 			InputStream is = this.getClass().getClassLoader().getResourceAsStream(methodFile);
 			BufferedReader br =new BufferedReader(new InputStreamReader(is));
 			StringBuffer sb = new StringBuffer();
 			String line;
 			
 			while (null != (line = br.readLine())) {
 				if (!(line.equals(" "))) {
 					if (!line.startsWith("%")) { // Ignore comments
 						sb.append(line);
 					}
 				}
 			}
 			//System.out.println(sb.toString());
 			br.close();
 			is.close();
 			
 			String[] methods = sb.toString().split(Pattern.quote( "." ) ); 
 			return methods;
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	
 	}
 
 
 }
