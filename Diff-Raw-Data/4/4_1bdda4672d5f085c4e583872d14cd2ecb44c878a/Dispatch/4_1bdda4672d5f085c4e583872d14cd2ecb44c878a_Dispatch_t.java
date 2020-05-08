 package eu.play_project.querydispatcher.epsparql.Test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryFactory;
 
 import eu.play_project.play_platformservices.api.QueryDetails;
 import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.VariableQuadrupleVisitor;
 import eu.play_project.play_platformservices_querydispatcher.epsparql.visitor.realtime.StreamIdCollector;
 import eu.play_project.play_platformservices_querydispatcher.types.H_Quadruple;
 import eu.play_project.play_platformservices_querydispatcher.types.R_Quadruple;
 import fr.inria.eventcloud.api.Quadruple;
 
 public class Dispatch {
 	private static Logger logger;
 
 	
 	@Test
 	public void getIoStreams(){
 		logger = LoggerFactory.getLogger(Dispatch.class);
 		String queryString;
 		String[] expectedInputStreams = {"http://streams.event-processing.org/ids/TwitterFeed", "http://streams.event-processing.org/ids/TaxiUCGeoLocation", "http://streams.event-processing.org/ids/TaxiUCGeoLocation"};
 		
 		// Get query.
 		queryString = getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");
 		
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
 		StreamIdCollector streamIdCollector = new StreamIdCollector();
 	
 		QueryDetails qd = new QueryDetails();
 		streamIdCollector.getStreamIds(query, qd);
 
 		// Test output stream
 		assertTrue(qd.getOutputStream().equals("http://streams.event-processing.org/ids/ContextualizedLatitudeFeed"));
 		
 		// Test input streams
 		for (int i = 0; i < qd.getInputStreams().size(); i++) {
 			assertTrue(qd.getInputStreams().get(i).equals(expectedInputStreams[i]));
 		}
 	}
 	
 	@Test
 	public void getVariablesAndTypes(){
 		if(logger == null){
 			logger= LoggerFactory.getLogger(Dispatch.class);
 		}
 		
 		
 		// Get query.
 		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
 		
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
 
 		
 		VariableQuadrupleVisitor vqv = new VariableQuadrupleVisitor();
 		
 		Map<String, List<Quadruple>> variables = vqv.getVariables(query);
 		
 		//Print all variables and triples in which they occur.
 		for(String key:variables.keySet()){
 			logger.debug("Variable " + key + " occurs in: ");
 
 			for (Quadruple quadruple : variables.get(key)) {
 				//logger.debug(quadruple.toString());
 				logger.debug("Type is: " +quadruple.getClass().getName());
 				
 				//Change Values
 				if(quadruple.getClass().isInstance(H_Quadruple.class)){
 
 				//Node node = quadruple.getObject(). ;
 				//	node = Node.createURI("http");
 				}
 			}
 			System.out.println();
 		}
 	}
 	
 	@Test
 	public void showRealtimeHistoricVariables(){
 		if(logger == null){
 			logger= LoggerFactory.getLogger(Dispatch.class);
 		}
 		
 		
 		// Get query.
 		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
 		
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
 
 		
 		VariableQuadrupleVisitor vqv = new VariableQuadrupleVisitor();
 		
 		Map<String, List<Quadruple>> variables = vqv.getVariables(query);
 		
 		//Print all variables and triples in which they occur.
 		for(String key:variables.keySet()){
 			logger.debug("Variable " + key + " occurs in: ");
 			int type = 0;
 			for (Quadruple quadruple : variables.get(key)) {
 				logger.debug("Type is: " +quadruple.getClass().getName());
 
 				if(quadruple.getClass().isInstance(R_Quadruple.class)){
 					type += 1;
 				}
 				if(quadruple.getClass().isInstance(H_Quadruple.class)){
 					type += 1;
 				}
 				if(quadruple.getClass().isInstance(H_Quadruple.class)){
 					type += 1;
 				}
 				System.out.println(type);
 			}
 			System.out.println();
 		}
 		
 	}
 	
 	@Test
 	public void dispatchQueryHistoricalMultipleClouds() {
 		// Get query.
 		String queryString = getSparqlQuery("EP-SPARQL-Query-Realtime-Historical-multiple-Clouds.eprq");
 
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
 
 		QueryDetails qd = new QueryDetails();
 
 		System.out.println(query.toString());
 	}
 	
 	
 	@Test
 	public void dispatchMissedCallsPlusTwitterQuery(){
 		// Get query.
 		String queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
 		
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxEPSPARQL_20);
 		
 		System.out.println(query);
 		
 		StreamIdCollector streamIdCollector = new StreamIdCollector ();
 		
 		QueryDetails qd = new QueryDetails();
 		streamIdCollector.getStreamIds(query, qd);
 
		assertTrue(qd.getOutputStream().equals("http://streams.event-processing.org/ids/TaxiUCClic2Call"));
		assertTrue(qd.getInputStreams().get(0).equals("http://streams.event-processing.org/ids/TaxiUCCall"));
 
 		
 	}
 	
 	
 	private String getSparqlQuery(String queryFile) {
 		try {
 			InputStream is = this.getClass().getClassLoader().getResourceAsStream(queryFile);
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			StringBuffer sb = new StringBuffer();
 			String line;
 
 			while (null != (line = br.readLine())) {
 				sb.append(line);
 				sb.append("\n");
 			}
 			// System.out.println(sb.toString());
 			br.close();
 			is.close();
 
 			return sb.toString();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
