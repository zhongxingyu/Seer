 /*
  * Copyright (c) 2010, University of Innsbruck, Austria.
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * You should have received a copy of the GNU Lesser General Public License along
  * with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package at.sti2.spark.rete;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
import junit.framework.TestCase;

 import org.apache.log4j.Logger;
 
 import at.sti2.spark.core.stream.Triple;
 import at.sti2.spark.core.triple.RDFTriple;
 import at.sti2.spark.core.triple.RDFURIReference;
 import at.sti2.spark.core.triple.RDFVariable;
 import at.sti2.spark.core.triple.TripleCondition;
 import at.sti2.spark.core.triple.TripleConstantTest;
 import at.sti2.spark.grammar.pattern.GroupGraphPattern;
 import at.sti2.spark.grammar.pattern.Pattern;
 
 public class WeatherTest {
 	
 	private RETENetwork reteNetwork = null;
 	private List <RDFTriple> triples = null;
 	
 	static Logger logger = Logger.getLogger(WeatherTest.class);
 
 	public void init() throws Exception {
 		
 		/**
 		 * A test case which examines how the network is built for the following graph pattern
 		 * 
 		 *     <?x     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://knoesis.wright.edu/ssw/ont/weather.owl#WindObservation">
 		 *     <?x     "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"		?y>
 		 *     <?x     "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#samplingTime"  ?z>
 		 *     <?y     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#MeasureData">
 		 *     <?y     "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"	?floatValue>
 		 *     <?z     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://www.w3.org/2006/time#Instant">
 		 *     <?z     "http://www.w3.org/2006/time#inXSDDateTime"								?instant>
 		 */
 		
 		logger.debug("Building up representation for triple <?x \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\" \"http://knoesis.wright.edu/ssw/ont/weather.owl#WindObservation\">");
 		
 		RDFTriple triple1 = new RDFTriple();
 		triple1.setSubject(new RDFVariable("?x"));
 		triple1.setPredicate(new RDFURIReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
 		triple1.setObject(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/weather.owl#WindObservation"));
 		
 		TripleConstantTest tripleConstantTest1 = new TripleConstantTest("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDFTriple.Field.PREDICATE);
 		TripleConstantTest tripleConstantTest2 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/weather.owl#WindObservation", RDFTriple.Field.OBJECT);
 		
 		TripleCondition tripleCondition1 = new TripleCondition();
 		tripleCondition1.setConditionTriple(triple1);
 		tripleCondition1.addConstantTest(tripleConstantTest1);
 		tripleCondition1.addConstantTest(tripleConstantTest2);
 		
 		
 		logger.debug("Building up representation for triple <?x \"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result\" ?y>");
 		
 		RDFTriple triple2 = new RDFTriple();
 		triple2.setSubject(new RDFVariable("?x"));
 		triple2.setPredicate(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result"));
 		triple2.setObject(new RDFVariable("?y"));
 		
 		TripleConstantTest tripleConstantTest3 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#result", RDFTriple.Field.PREDICATE);
 		
 		TripleCondition tripleCondition2 = new TripleCondition();
 		tripleCondition2.setConditionTriple(triple2);
 		tripleCondition2.addConstantTest(tripleConstantTest3);
 		
 		logger.debug("Building up representation for triple <?x \"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#samplingTime\" ?z>");
 		
 		RDFTriple triple3 = new RDFTriple();
 		triple3.setSubject(new RDFVariable("?x"));
 		triple3.setPredicate(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#samplingTime"));
 		triple3.setObject(new RDFVariable("?z"));
 		
 		TripleConstantTest tripleConstantTest4 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#samplingTime", RDFTriple.Field.PREDICATE);
 		
 		TripleCondition tripleCondition3 = new TripleCondition();
 		tripleCondition3.setConditionTriple(triple3);
 		tripleCondition3.addConstantTest(tripleConstantTest4);
 		
 		/**
 		 * <?y     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#MeasureData">
 		 */
 		RDFTriple triple4 = new RDFTriple();
 		triple4.setSubject(new RDFVariable("?y"));
 		triple4.setPredicate(new RDFURIReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
 		triple4.setObject(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#MeasureData"));
 		
 		TripleConstantTest tripleConstantTest5 = new TripleConstantTest("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDFTriple.Field.PREDICATE);
 		TripleConstantTest tripleConstantTest6 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#MeasureData", RDFTriple.Field.OBJECT);
 		
 		TripleCondition tripleCondition4 = new TripleCondition();
 		tripleCondition4.setConditionTriple(triple4);
 		tripleCondition4.addConstantTest(tripleConstantTest5);
 		tripleCondition4.addConstantTest(tripleConstantTest6);
 		
 		
 		/**
 		 * <?y     "http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"	?floatValue>
 		 */
 		RDFTriple triple5 = new RDFTriple();
 		triple5.setSubject(new RDFVariable("?y"));
 		triple5.setPredicate(new RDFURIReference("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"));
 		triple5.setObject(new RDFVariable("?floatValue"));
 		
 		TripleConstantTest tripleConstantTest7 = new TripleConstantTest("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue", RDFTriple.Field.PREDICATE);
 		
 		TripleCondition tripleCondition5 = new TripleCondition();
 		tripleCondition5.setConditionTriple(triple5);
 		tripleCondition5.addConstantTest(tripleConstantTest7);
 		
 		/**
 		 * 	   <?z     "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"						"http://www.w3.org/2006/time#Instant">
 		 */
 		RDFTriple triple6 = new RDFTriple();
 		triple6.setSubject(new RDFVariable("?z"));
 		triple6.setPredicate(new RDFURIReference("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
 		triple6.setObject(new RDFURIReference("http://www.w3.org/2006/time#Instant"));
 		
 		TripleConstantTest tripleConstantTest8 = new TripleConstantTest("http://www.w3.org/1999/02/22-rdf-syntax-ns#type", RDFTriple.Field.PREDICATE);
 		TripleConstantTest tripleConstantTest9 = new TripleConstantTest("http://www.w3.org/2006/time#Instant", RDFTriple.Field.OBJECT);
 		
 		TripleCondition tripleCondition6 = new TripleCondition();
 		tripleCondition6.setConditionTriple(triple6);
 		tripleCondition6.addConstantTest(tripleConstantTest8);
 		tripleCondition6.addConstantTest(tripleConstantTest9);
 		
 		/**
 		 *     <?z     "http://www.w3.org/2006/time#inXSDDateTime"								?instant>
 		 */
 		RDFTriple triple7 = new RDFTriple();
 		triple7.setSubject(new RDFVariable("?z"));
 		triple7.setPredicate(new RDFURIReference("http://www.w3.org/2006/time#inXSDDateTime"));
 		triple7.setObject(new RDFVariable("?instant"));
 		
 		TripleConstantTest tripleConstantTest10 = new TripleConstantTest("http://www.w3.org/2006/time#inXSDDateTime", RDFTriple.Field.PREDICATE);
 		
 		TripleCondition tripleCondition7 = new TripleCondition();
 		tripleCondition7.setConditionTriple(triple7);
 		tripleCondition7.addConstantTest(tripleConstantTest10);		
 		
 		reteNetwork = new RETENetwork();
 		
 		// ----- BUILDING A TRIPLE GRAPH PATTERN -----
 		
 		GroupGraphPattern wherePattern = new GroupGraphPattern();
 		wherePattern.addWhereCondition(tripleCondition1);
 		wherePattern.addWhereCondition(tripleCondition2);
 		wherePattern.addWhereCondition(tripleCondition3);
 		wherePattern.addWhereCondition(tripleCondition4);
 		wherePattern.addWhereCondition(tripleCondition5);
 		wherePattern.addWhereCondition(tripleCondition6);
 		wherePattern.addWhereCondition(tripleCondition7);
 		
 		Pattern patternGraph = new Pattern();
 		patternGraph.setWherePattern(wherePattern);
 		
 		reteNetwork.addTriplePatternGraph(patternGraph);
 		
 //		List <TripleCondition> conditions = new ArrayList <TripleCondition> ();
 //		conditions.add(tripleCondition1);
 //		conditions.add(tripleCondition2);
 //		conditions.add(tripleCondition3);
 //		conditions.add(tripleCondition4);
 //		conditions.add(tripleCondition5);
 //		conditions.add(tripleCondition6);
 //		conditions.add(tripleCondition7);
 //		
 //		reteNetwork.addProduction(conditions);
 		
 		reteNetwork.printNetworkStructure();
 		
 		triples = new ArrayList <RDFTriple> ();
 		
 		//Setup data
 		N3FileInput n3FileInput = new N3FileInput("target/test-classes/./resources/moretriples.n3");
 		n3FileInput.parseTriples();
 		triples = n3FileInput.getTriples();
 	}
 	
 //	@Test
 	public void testPerformance(){
 		
 		Date startDate = new Date();
 		
 		logger.info("Processing " + triples.size() + " triples.");
 		
 		for (RDFTriple wmeTriple : triples){
 			
 			WorkingMemoryElement wme = new WorkingMemoryElement();
 			wme.setStreamedTriple(new Triple(wmeTriple, 0l, false, 0l));
 			reteNetwork.getWorkingMemory().addWorkingMemoryElement(wme);
 		}
 		
 		Date endDate = new Date();
 		
 		logger.info("Processed " + triples.size() + " triples in " + (endDate.getTime() - startDate.getTime()) + " ms.");
 	}
 }
