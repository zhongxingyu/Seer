 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package eu.larkc.iris.functional;
 
 import java.io.IOException;
 
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import cascading.tuple.TupleEntry;
 import cascading.tuple.TupleEntryIterator;
 import eu.larkc.iris.CascadingTest;
 import eu.larkc.iris.evaluation.EvaluationContext;
 import eu.larkc.iris.imports.Importer;
 import eu.larkc.iris.rules.compiler.CascadingRuleCompiler;
 import eu.larkc.iris.rules.compiler.FlowAssembly;
 import eu.larkc.iris.rules.compiler.IDistributedCompiledRule;
 
 /**
  * 
  * @history Feb 1, 2010, vroman, implement a first version
  * @author Valer Roman
  */
 public class Rdfs7Test extends CascadingTest {
 
 	private static final Logger logger = LoggerFactory.getLogger(Rdfs7Test.class);
 
 	//p( ?X, ?Y ) :- q( ?X, ?Y ), r( ?Y, ?Z ), s( ?X, ?Z ).
 	//q( 1, 2 ).
 	//r( 2, 3 ).
 	//s( 1, 3 ).
 	//?- p(?X, ?Y).
 	
 	public Rdfs7Test(String name) {
 		super(name, false);
 	}
 	
 	/* (non-Javadoc)
 	 * @see eu.larkc.iris.CascadingTest#tearDown()
 	 */
 	@Override
 	protected void tearDown() throws Exception {
 		FileSystem fs = FileSystem.get(defaultConfiguration.hadoopConfiguration);
 		if (fs.exists(new Path("test"))) {
 			fs.delete(new Path("test"), true);
 		}
 		super.tearDown();
 	}
 
 	@Override
 	protected  void createFacts() throws IOException {
 		defaultConfiguration.project = "test";
		defaultConfiguration.doPredicateIndexing = true;
 		if (enableCluster) {
 			new Importer(defaultConfiguration).importFromFile(this.getClass().getResource("/facts/rdfs7.nt").getPath(), "import");
 		} else {
 			new Importer(defaultConfiguration).processNTriple(this.getClass().getResource("/facts/rdfs7.nt").getPath(), "import");
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.larkc.iris.evaluation.distributed.ProgramEvaluationTest#getRulesReader()
 	 */
 	@Override
 	protected String getRulesFile() {
 		return "/rules/rdfs7.xml";
 	}
 
 	public void testEvaluation() throws Exception {
 		
 		CascadingRuleCompiler crc = new CascadingRuleCompiler(defaultConfiguration);
 		IDistributedCompiledRule dcr = crc.compile(rules.get(0));
 		dcr.evaluate(new EvaluationContext(1, 1, 1));
 		FlowAssembly fa = dcr.getFlowAssembly();
 		
 		TupleEntryIterator tei = fa.openSink();
 		int size = 0;
 		while (tei.hasNext()) {
 			TupleEntry te = tei.next();
 			logger.info(te.getTuple().toString());
 			size++;
 		}
 		assertEquals(1, size);
 		//IRelation relation = evaluate("?- p(?X, ?Y).");
 		
 		//verifySink(flow, expects)
 	}
 
 }
