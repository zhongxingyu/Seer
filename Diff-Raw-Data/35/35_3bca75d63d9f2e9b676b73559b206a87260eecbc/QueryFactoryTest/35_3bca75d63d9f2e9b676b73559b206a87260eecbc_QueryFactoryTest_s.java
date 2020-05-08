 /*
  * Copyright (c) 2011 Miguel Ceriani
  * miguel.ceriani@gmail.com
 
  * This file is part of Semantic Web Open datatafloW System (SWOWS).
 
  * SWOWS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of
  * the License, or (at your option) any later version.
 
  * SWOWS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
 
  * You should have received a copy of the GNU Affero General
  * Public License along with SWOWS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.swows.test;
 
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.transform.TransformerException;
 
 import org.swows.function.Factory;
 import org.swows.reader.ReaderFactory;
 import org.swows.spinx.SpinxFactory;
 
 import com.hp.hpl.jena.graph.Graph;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.query.Dataset;
 import com.hp.hpl.jena.query.DatasetFactory;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.util.FileManager;
 
 public class QueryFactoryTest {
 
 	static {
 		ReaderFactory.initialize();
 	}
 	
     public static void main(final String[] args) throws TransformerException {
     	
 //    	String baseUri = "/home/miguel/TUIO/circles/";
 //    	String baseUri = "/home/miguel/pampers3/dataflow/test_query_svg/";
     	String baseUri = "/home/miguel/svg2canvas/";
 
 //    	PropertyFunctionRegistry registry = PropertyFunctionRegistry.get();
 //		registry.put(Factory.getBaseURI() + "bnode", Factory.getInstance());
     	
 //    	Query inputQuery = QueryFactory.read("file:/home/miguel/TUIO/test3-1.sparql");
 //    	Query inputQuery = QueryFactory.read(baseUri + "svgBoxesLayersTest.sparql");
 //    	Query inputQuery = QueryFactory.read(baseUri + "positions2test.sparql");
 //    	Query inputQuery = QueryFactory.read(baseUri + "colorsTest.sparql");
 //		Query inputQuery = QueryFactory.read(baseUri + "circlesTest.sparql");
 //    	Query inputQuery = QueryFactory.read(baseUri + "quantityHistoryCurr_T.sparql");
    	Query inputQuery = QueryFactory.read(baseUri + "svg2canvas.sparql");
     	
     	Factory.getInstance();
     	
     	Graph queryGraph = SpinxFactory.fromQuery(inputQuery);
     	Node queryRootNode = Node.createURI("#defaultQuery");
     	
     	
     	System.out.println();
     	System.out.println("*************************");
     	System.out.println("*** Input Query in N3 ***");
     	System.out.println("*************************");
     	ModelFactory.createModelForGraph(queryGraph).write(System.out,"N3");
     	System.out.println("*************************");
     	System.out.println();
     	
     	//Node queryRootNode = queryGraph.find(Node.ANY, RDF.type.asNode(), SP.Query.asNode()).next().getSubject();
     	
     	Query outputQuery = org.swows.spinx.QueryFactory.toQuery(queryGraph, queryRootNode);
     	
     	outputQuery.serialize(System.out);
     	
     	System.out.println();
     	System.out.println("**************************");
     	System.out.println("*** Output Query in N3 ***");
     	System.out.println("**************************");
     	ModelFactory.createModelForGraph(SpinxFactory.fromQuery(outputQuery)).write(System.out,"N3");
     	System.out.println("**************************");
     	System.out.println();
 //    	
 //    	System.out.println();
 //    	System.out.println("****************************");
 //    	System.out.println("*** Input Query Prefixes ***");
 //    	System.out.println("****************************");
 //    	Map<String,String> inputPrefixMap = inputQuery.getPrefixMapping().getNsPrefixMap();
 //    	for (String prefix: inputPrefixMap.keySet()) {
 //    		System.out.println("Prefix: " + prefix + " URI: " + inputPrefixMap.get(prefix));
 //    	}
 //    	System.out.println("****************************");
 //    	System.out.println();
 //    	
 //    	System.out.println();
 //    	System.out.println("****************************");
 //    	System.out.println("*** Output Query Prefixes ***");
 //    	System.out.println("****************************");
 //    	Map<String,String> outputPrefixMap = outputQuery.getPrefixMapping().getNsPrefixMap();
 //    	for (String prefix: outputPrefixMap.keySet()) {
 //    		System.out.println("Prefix: " + prefix + " URI: " + outputPrefixMap.get(prefix));
 //    	}
 //    	System.out.println("****************************");
 //    	System.out.println();
 //    	
     	outputQuery.setPrefixMapping(inputQuery.getPrefixMapping());
 //    	outputQuery.setQueryPattern(inputQuery.getQueryPattern());
 // 
 
     	inputQuery.setResultVars() ;
     	outputQuery.setResultVars() ;
 
     	QueryCompare.PrintMessages = true;
     	if (QueryCompare.equals(inputQuery, outputQuery))
     		System.out.println("OK! :)");
     	else 
     		System.out.println("KO :(");
 		
 //    	String defaultGraphUri = baseUri + "tuio_input_new.n3";
 //    	
 //		List<String> namedGraphUris = new Vector<String>();
 //		namedGraphUris.add(baseUri + "viewPortInfo.n3");
 //		namedGraphUris.add(baseUri + "config.n3");
 //		namedGraphUris.add(baseUri + "circleAges.n3");
 //		namedGraphUris.add(baseUri + "circles.n3");
 
 //    	String defaultGraphUri = baseUri + "circleAges.n3";
 //    	String defaultGraphUri = baseUri + "defaultInput.n3";
 //    	
 //		List<String> namedGraphUris = new Vector<String>();
 //		namedGraphUris.add(baseUri + "tuio_input.n3");
 //		namedGraphUris.add(baseUri + "config.n3");
     	
 //    	String defaultGraphUri = baseUri + "input.n3";
 //		List<String> namedGraphUris = new Vector<String>();
 //		namedGraphUris.add(baseUri + "range.n3");
 		
 //    	String defaultGraphUri = baseUri + "quantityHistoryStart_T.n3";
 //		List<String> namedGraphUris = new Vector<String>();
 		
 //    	String defaultGraphUri = baseUri + "input.n3";
     	String defaultGraphUri = baseUri + "BlankMapWithRadioBox.svg";
 		List<String> namedGraphUris = new Vector<String>();
 		Model defaultModel = FileManager.get().loadModel(defaultGraphUri,defaultGraphUri,"http://www.swows.org/syntaxes/XML");
 //		namedGraphUris.add(baseUri + "config.n3");
 //		namedGraphUris.add(baseUri + "selectedPage.n3");
 
 		System.out.println();
     	System.out.println("**************************");
     	System.out.println("*** Input Model ***");
     	System.out.println("**************************");
     	defaultModel.write(System.out,"N3");
     	System.out.println("****************************");
     	System.out.println();
 
     	//    	Dataset inputDataset = DatasetFactory.create(defaultGraphUri, namedGraphUris);
     	Dataset inputDataset = DatasetFactory.create(defaultModel);
 		
 		long queryStart = System.currentTimeMillis();
 		QueryExecution queryExecution =
 				QueryExecutionFactory.create(inputQuery, inputDataset);
 		Model inputQueryResult = queryExecution.execConstruct();
 		long queryEnd = System.currentTimeMillis();
 		System.out.println("Input Query execution time: " + (queryEnd - queryStart) );
 
 		System.out.println();
     	System.out.println("**************************");
     	System.out.println("*** Input Query Result ***");
     	System.out.println("**************************");
     	inputQueryResult.write(System.out,"N3");
     	System.out.println("****************************");
     	System.out.println();
     	
 //		queryStart = System.currentTimeMillis();
 //		QueryExecution outQueryExecution =
 //				QueryExecutionFactory.create(outputQuery, inputDataset);
 //		Model outputQueryResult = outQueryExecution.execConstruct();
 //		queryEnd = System.currentTimeMillis();
 //		System.out.println("Output Query execution time: " + (queryEnd - queryStart) );
 		
 //    	System.out.println();
 //    	System.out.println("**************************");
 //    	System.out.println("*** Output Query Result ***");
 //    	System.out.println("**************************");
 //    	outputQueryResult.write(System.out,"N3");
 //    	System.out.println("****************************");
 //    	System.out.println();
 
     }
 		
 }
