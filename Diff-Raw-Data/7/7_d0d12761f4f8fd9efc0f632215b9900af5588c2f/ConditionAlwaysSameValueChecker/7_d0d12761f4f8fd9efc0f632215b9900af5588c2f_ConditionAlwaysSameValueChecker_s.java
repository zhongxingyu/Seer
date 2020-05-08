 package core.jtester.ontology.checker;
 
 import java.util.List;
 
 import core.common.cfg.javacfg.JavaControlFlowGraph;
 import core.common.model.functionblock.ConditionExpression;
 import core.common.model.jobflow.JobConst;
 import core.common.model.test.TestData;
 import core.common.model.test.TestFile;
 import core.jtester.ontology.reasoner.IChecker;
 
 public class ConditionAlwaysSameValueChecker implements IChecker{
 
 	public void check(TestData data) {
 		TestFile file = data.getCurrentTestFile();
 		List<JavaControlFlowGraph> cfgs = (List<JavaControlFlowGraph>) file.get(JobConst.CONTROL_FLOW_GRAPH);
 		generateReport(cfgs);
 	}
 	
 	private void generateReport(List<JavaControlFlowGraph> cfgs){
		System.err.println("Warning: ֵʼղ䣡");
 		for(JavaControlFlowGraph cfg: cfgs){
 			List<ConditionExpression> conditions = cfg.getConditions();
 			if(conditions != null && !conditions.isEmpty()){
 				for(ConditionExpression ce: conditions){
 					System.err.println("\t" + ce + " \t");
 				}
 			}
 		}
 	}
 }
