 package com.aptana.rdt.internal.parser.warnings;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.evaluator.Instruction;
 import org.rubypeople.rdt.core.parser.warnings.RubyLintVisitor;
 
 import com.aptana.rdt.AptanaRDTPlugin;
 
 public class MethodMissingWithoutRespondTo extends RubyLintVisitor {
 
	private static final String RESPOND_TO = "respond_to";
 	private static final String METHOD_MISSING = "method_missing";
 	
 	private Map<String, DefnNode> methods = new HashMap<String, DefnNode>();
 	
 	public MethodMissingWithoutRespondTo(String contents) {
 		super(contents);
 	}
 
 	@Override
 	protected String getOptionKey() {
 		return AptanaRDTPlugin.COMPILER_PB_METHOD_MISSING_NO_RESPOND_TO;
 	}
 	
 	@Override
 	public Instruction visitDefnNode(DefnNode iVisited) {
 		methods.put(iVisited.getName(), iVisited);
 		return super.visitDefnNode(iVisited);
 	}
 	
 	@Override
 	public void exitClassNode(ClassNode iVisited) {
 		if (methods.containsKey(METHOD_MISSING) && !methods.containsKey(RESPOND_TO)) {
 			createProblem(methods.get(METHOD_MISSING).getNameNode().getPosition(), "Class defines method_missing, but does not define custom respond_to");
 		}
 		methods.clear();
 		super.exitClassNode(iVisited);
 	}
 
 }
