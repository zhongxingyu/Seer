 package org.suite.search;
 
 import org.instructionexecutor.LogicInstructionExecutor;
 import org.suite.SuiteUtil;
 import org.suite.doer.Prover;
 import org.suite.kb.RuleSet;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.suite.search.ProveSearch.Builder;
 import org.suite.search.ProveSearch.Finder;
 import org.util.FunUtil;
 import org.util.FunUtil.Sink;
 import org.util.FunUtil.Source;
 
 public class CompiledProveBuilder implements Builder {
 
 	@Override
 	public Finder build(RuleSet ruleSet, Node goal) {
 		final Prover lc = new Prover(ruleSet);
 		final Reference code = new Reference();
 
		Node node = SuiteUtil.substitute("compile-logic (.0 >> .1, sink .2) .3" //
 				, SuiteUtil.getRuleList(ruleSet, null) //
				, goal //
 				, Builder.in //
 				, code);
 		new Prover(SuiteUtil.logicalRuleSet()).prove(node);
 
 		return new Finder() {
 			public void find(Node in, final Sink<Node> sink) {
 				Source<Node> source = FunUtil.source(in);
 				new LogicInstructionExecutor(code, lc, source, sink).execute();
 			}
 		};
 	}
 
 }
