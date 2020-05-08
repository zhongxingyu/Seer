 package eu.play_project.querydispatcher.bdpl.tests;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.sparql.expr.Expr;
 import com.hp.hpl.jena.sparql.syntax.Element;
 import com.hp.hpl.jena.sparql.syntax.ElementEventGraph;
 
 import eu.play_platform.platformservices.bdpl.syntax.windows.visitor.ElementWindowVisitor;
 import eu.play_project.play_commons.constants.Namespace;
 import eu.play_project.play_platformservices.QueryTemplateImpl;
 import eu.play_project.play_platformservices.api.BdplQuery;
 import eu.play_project.play_platformservices.api.HistoricalQuery;
 import eu.play_project.play_platformservices.api.QueryDetails;
 import eu.play_project.play_platformservices_querydispatcher.api.EleGenerator;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.code_generator.realtime.EleGeneratorForConstructQuery;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.FilterExpressionCodeGenerator;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.HavingVisitor;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.RdfQueryRepresentativeQueryVisitor;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.UniqueNameManager;
 import eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime.WindowVisitor;
 import eu.play_project.play_platformservices_querydispatcher.playEleParser.ParseException;
 import eu.play_project.play_platformservices_querydispatcher.playEleParser.PlayEleParser;
 
 //import eu.play_project.querydispatcher.bdpl.tests.helpers.FilterExpressionCodeGenerator;
 
 public class BdplEleTest {
 
 	@Test
 	public void testManualParserUsage() {
 
 		String queryString = getSparqlQuery("queries/HavingAvgExp2.eprq");
 		Query query = null;
 
 		try {
 			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		} catch (Exception e) {
 			System.out.println("Exception was thrown: " + e);
 		}
 
 		HavingVisitor v = new HavingVisitor();
 
 		for (Expr el : query.getHavingExprs()) {
 			el.visit(v);
 		}
 
 		// Use custom visitor
 		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
 
 		visitor1.setPatternId("'" + Namespace.PATTERN.getUri() + "123456'");
 
 		visitor1.generateQuery(query);
 		String etalisPattern = visitor1.getEle();
 
 		System.out.println(etalisPattern);
 	}
 
 
 	@Test
 	public void testBasicEleGeneration() {
 
 		String queryString = getSparqlQuery("queries/HavingAvgExp2.eprq");
 		Query query = null;
 
 		System.out.println(queryString);
 
 		// Instantiate code generator
 		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
 
 		// Set id.
 		String patternId = "'" + Namespace.PATTERN.getUri() + Math.random() * 1000000 + "'";
 		visitor1.setPatternId(patternId);
 
 		// Parse query
 		try {
 			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		} catch (Exception e) {
 			System.out.println("Exception was thrown: " + e);
 		}
 
 		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());
 
 		visitor1.generateQuery(query);
 		String etalisPattern = visitor1.getEle();
 
 		// Add query details.
 		QueryDetails details = new QueryDetails(patternId);
 		// Set properties for windows in QueryDetails
 		ElementWindowVisitor windowVisitor = new WindowVisitor(details);
 		query.getWindow().accept(windowVisitor);
 		details.setRdfDbQueries(visitor1.getRdfDbQueries());
 
 		BdplQuery bdplQuery = BdplQuery.builder()
 				.ele(etalisPattern)
 				.details(details)
 				.bdpl("")
 				.constructTemplate(new QueryTemplateImpl())
 				.historicalQueries(new LinkedList<HistoricalQuery>())
 				.build();
 
 		System.out.println(etalisPattern);
 	}
 
 	/**
 	 * Generate code for (AVG(t) >= 30).
 	 */
 	@Test
 	public void testHavingAvg() {
 
 		String queryString = getSparqlQuery("queries/HavingAvgExp2.eprq");
 		Query query = null;
 
 		System.out.println(queryString);
 		// Parse query
 		try {
 			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		} catch (Exception e) {
 			System.out.println("Exception was thrown: " + e);
 		}
 		UniqueNameManager.getVarNameManager().setWindowTime(query.getWindow().getValue());
 
 		// Generate code.
 		HavingVisitor v = new HavingVisitor();
 
 		for (Expr el : query.getHavingExprs()) {
 			el.visit(v);
 		}
 
 		assertEquals("calcAverage(dbId0, 1800, Result10), greaterOrEqual(Result10,30.0)", v
 				.getCode().toString());
 	}
 
 	@Test
 	public void testStartParser() throws InterruptedException {
 		String queryString = getQuery("queries/BDPL-Query-Realtime-Historical-multiple-Clouds.eprq")[0];
 		// queryString =
 		// "PREFIX : <http://example.com> CONSTRUCT{:e :type :FacebookCepResult.} {EVENT ?id{?e1 :location [ :lat ?Latitude1; :long ?Longitude1 ]} GRAPH ?id{?s ?p ?o}}";
 		System.out.println(queryString);
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 
		System.out.println("Querry \n" + query);
 
 		// Use custom visitor
 		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
 		visitor1.setPatternId("'" + Namespace.PATTERN.getUri() + "123456'");
 
 		visitor1.generateQuery(query);
 		String etalisPattern = visitor1.getEle();
 		
 		System.out.println(etalisPattern);
 
 		// try { // FIXME sobermeier: this does not work anymore since 'complex'
 		// events now have individual names
		// parsEtalisPatter(etalisPattern);
 		// } catch (ParseException e) {
 		// e.printStackTrace();
 		// fail("Pars error: " + e.getMessage());
 		// }
 	}
 
	private void parsEtalisPatter(String elePattern) throws ParseException {
 		PlayEleParser parser = new PlayEleParser(new ByteArrayInputStream(elePattern.getBytes()));
 
 		parser.Start();
 	}
 
 	@Test
 	public void testShowQdResult() {
 
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQuery("play-epsparql-contextualized-latitude-01-query.eprq");
 
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 
 		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
 		visitor1.setPatternId(Namespace.PATTERN.getUri() + Math.random() * 1000000);
 		visitor1.generateQuery(query);
 		String etalisPattern = visitor1.getEle();
 
 		System.out.println(etalisPattern);
 	}
 
 	@Test
 	public void testShowEleResult() {
 
 		String queryString;
 
 		// Get query.
 		queryString = getSparqlQuery("play-epsparql-clic2call-plus-tweet.eprq");
 		System.out.println(queryString);
 		// Parse query
 		Query query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 
 		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
 		visitor1.setPatternId(Namespace.PATTERN.getUri() + Math.random() * 1000000);
 		visitor1.generateQuery(query);
 		String etalisPattern = visitor1.getEle();
 
 		System.out.println(etalisPattern);
 	}
 	
 	
 
 	@Test
 	public void testEvaluateFilterExpression() {
 		String queryString = "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> CONSTRUCT{ ?x ?nice ?name. ?e rdf:type ?AlertEvent } WHERE {EVENT ?id{?e1 ?location \"abc\". ?e rdf:type ?AlertEvent} FILTER (abs(?Latitude1 - ?Latitude2) < 0.1 && abs(?Longitude1 - ?Longitude2) < 0.5)}";
 		Query query = null;
 		try {
 			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		} catch (Exception e) {
 
 		}
 		FilterExpressionCodeGenerator visitor = new FilterExpressionCodeGenerator();
 
 		// Get first EventGraph
 		ElementEventGraph eventGraph = (ElementEventGraph) query.getEventQuery().get(0);
 
 		Element filter = eventGraph.getFilterExp();
 
 		visitor.startVisit(filter);
 
 		System.out.println(visitor.getEle());
 
 	}
 
 	// @Test
 	// public void dispatchQuery(){
 	// String queryString =
 	// "CONSTRUCT{ ?x ?nice ?name } WHERE {EVENT ?id{?e1 ?location \"abc\"} FILTER (abs(?Latitude1 - ?Latitude2) < 0.1 && abs(?Longitude1 - ?Longitude2) < 0.5)}";
 	// Query query = QueryFactory.create(queryString,
 	// com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 	//
 	// VariableTypeVisitor visitor = new VariableTypeVisitor();
 	//
 	// Map<String, List<Variable>> variables = visitor.getVariables(query,
 	// VariableTypes.historicType);
 	// System.out.println(variables.values());
 	// }
 
 	@Test
 	public void testRdfQueryRepresentativeQueryVisitor() {
 
 		String queryString = getSparqlQuery("queries/HavingAvgExp2.eprq");
 		Query query = null;
 
 		try {
 			query = QueryFactory.create(queryString, com.hp.hpl.jena.query.Syntax.syntaxBDPL);
 		} catch (Exception e) {
 			System.out.println("Exception was thrown: " + e);
 		}
 
 		RdfQueryRepresentativeQueryVisitor v = new RdfQueryRepresentativeQueryVisitor();
 		query.getEventQuery().get(0).visit(v);
 
 		// Queries for variable t1,e1,friend1,about1.
 		String[] expectedResult = {
 				"rdf(Ve1,'http://events.event-processing.org/types/temperature',Vt1,ViD0)",
 				"rdf(Ve1,'http://www.w3.org/1999/02/22-rdf-syntax-ns#type','http://events.event-processing.org/types/FacebookStatusFeedEvent',ViD0)",
 				"rdf(Ve1,'http://events.event-processing.org/types/name',Vfriend1,ViD0)",
 				"rdf(Ve1,'http://events.event-processing.org/types/status',Vabout1,ViD0)"
 		};
 
 		int i = 0;
 		for (String key : v.getRdfQueryRepresentativeQuery().keySet()) {
 			assertEquals(expectedResult[i], v.getRdfQueryRepresentativeQuery().get(key));
 			i++;
 		}
 
 		System.out.println(v.getRdfQueryRepresentativeQuery());
 
 		// Use custom visitor
 		EleGenerator visitor1 = new EleGeneratorForConstructQuery();
 
 		visitor1.setPatternId("'http://patternId.example.com/123456'");
 
 		visitor1.generateQuery(query);
 		String etalisPattern = visitor1.getEle();
 
 		System.out.println(etalisPattern);
 	}
 
 	/**
 	 * Return the query from given file. If given it returns the message of the
 	 * expected exception.
 	 * 
 	 * @param queryFile
 	 * @return query[0] the query text, query[1] if given in input file the
 	 *         expected exception
 	 */
 	public String[] getQuery(String queryFile) {
 		try {
 			InputStream is = (InputStream) getClass().getClassLoader().getResource(queryFile)
 					.getContent();
 			InputStreamReader isr = new InputStreamReader(is);
 			BufferedReader br = new BufferedReader(isr);
 			StringBuffer sb = new StringBuffer();
 			String line;
 			String[] exeptionName = null;
 
 			while ((line = br.readLine()) != null) {
 
 				if (line.startsWith("#")) { // Line with comment
 					sb.append(line);
 					sb.append("\n"); // new Line after commentline
 					// Extract exceptionname
 					exeptionName = line.split(".*@expectedException<*| *\\\\>*");
 				} else {
 					sb.append(line);
 				}
 			}
 			// System.out.println(sb.toString());
 			br.close();
 			isr.close();
 			is.close();
 
 			if (exeptionName != null && exeptionName.length > 1) {
 				return new String[] { sb.toString(), exeptionName[1] };
 			} else {
 				return new String[] { sb.toString(), null };
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the filenaes of the testfiles depending on the type of the
 	 * testfile. The filename of a file with contains a broken query (a query
 	 * which the parser do not acceapt) must start with "BDPL-BrokenQuery". The
 	 * filename of a file with a regular query must start with "BDPL-Query".
 	 * 
 	 * @param dir
 	 *            Directory of the files.
 	 * @param type
 	 *            Type of the fiel.
 	 * @return All filenames with the specified type.
 	 */
 	public static List<String> getFilenames(File dir) {
 
 		ArrayList<String> filenames = new ArrayList<String>();
 
 		File[] files = dir.listFiles();
 		if (files != null) {
 			for (int i = 0; i < files.length; i++) {
 				if (files[i].getName().endsWith(".ele") && !files[i].isDirectory()) {
 					filenames.add(files[i].getName());
 				}
 			}
 		}
 		return filenames;
 	}
 
 	public static String getSparqlQuery(String queryFile) {
 		try {
 			InputStream is = BdplEleTest.class.getClassLoader().getResourceAsStream(queryFile);
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			StringBuffer sb = new StringBuffer();
 			String line;
 
 			while (null != (line = br.readLine())) {
 				sb.append(line);
 				sb.append("\n");
 			}
 
 			br.close();
 			is.close();
 
 			return sb.toString();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 
 	}
 
 }
