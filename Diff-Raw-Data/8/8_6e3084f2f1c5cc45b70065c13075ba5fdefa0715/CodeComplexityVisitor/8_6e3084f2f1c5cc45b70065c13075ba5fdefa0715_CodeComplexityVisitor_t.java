 package com.aptana.rdt.internal.parser.warnings;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.jruby.ast.CaseNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.IfNode;
 import org.jruby.ast.LocalAsgnNode;
 import org.jruby.ast.ReturnNode;
 import org.jruby.ast.WhenNode;
 import org.jruby.evaluator.Instruction;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.rubypeople.rdt.internal.core.RubyModelManager;
 import org.rubypeople.rdt.internal.core.parser.warnings.RubyLintVisitor;
 import org.rubypeople.rdt.internal.core.util.ASTUtil;
 
 import com.aptana.rdt.AptanaRDTPlugin;
 
 public class CodeComplexityVisitor extends RubyLintVisitor {
 
 	private int maxArgLength;
 	private int maxLines;
 	private int maxReturns;
 	private int maxBranches;
 	private int maxLocals;
 	private int returnCount;
 	private int branchCount;
 	private Set locals;
 	private Map fOptions;
 
 	public CodeComplexityVisitor(String contents) {
 		this(AptanaRDTPlugin.getDefault().getOptions(), contents);		
 	}
 	
 	public CodeComplexityVisitor(Map options, String contents) {
 		super(contents);
 		fOptions = options;
 		maxArgLength = getInt(AptanaRDTPlugin.COMPILER_PB_MAX_ARGUMENTS, 5); 
 		maxLines = getInt(AptanaRDTPlugin.COMPILER_PB_MAX_LINES, 20); 
 		maxReturns = getInt(AptanaRDTPlugin.COMPILER_PB_MAX_RETURNS, 5); 
 		maxBranches = getInt(AptanaRDTPlugin.COMPILER_PB_MAX_BRANCHES, 5); 
 		maxLocals = getInt(AptanaRDTPlugin.COMPILER_PB_MAX_LOCALS, 4); 
 		returnCount = 0;
 		branchCount = 0;
 	}
 	private int getInt(String key, int defaultValue) {
 		try {
 			return Integer.parseInt((String) fOptions.get(key));
 		} catch (NumberFormatException e) {
 			return defaultValue;
 		}
 	}
 
 	@Override
 	protected String getOptionKey() {
 		// TODO Break this visitor up into multiple! One for each key.
 		return AptanaRDTPlugin.COMPILER_PB_MAX_ARGUMENTS;
 	}
 
 	@Override
 	public Instruction visitDefnNode(DefnNode iVisited) {
 		returnCount = 0;
 		branchCount = 0;
 		locals = new HashSet();
 
 		String[] args = ASTUtil.getArgs(iVisited.getArgsNode(), iVisited.getScope());
 		if (args != null && args.length > maxArgLength) {
 			createProblem(iVisited.getArgsNode().getPosition(), "Too many method arguments: " + args.length);
 		}
 		ISourcePosition pos = iVisited.getPosition();
 		int lines = (pos.getEndLine() - pos.getStartLine()) - 1;
 		if (lines > maxLines) {
 			createProblem(iVisited.getNameNode().getPosition(), "Too many lines in method: " + lines);
 		}
 		return super.visitDefnNode(iVisited);
 	}
 
 	@Override
 	public Instruction visitIfNode(IfNode iVisited) {
 		// TODO Make sure this doesn't count modifiers
 		if (iVisited.getThenBody() != null) {
 			branchCount++;
 		}
 		if (iVisited.getElseBody() != null) {
 			branchCount++;
 		}
 		return super.visitIfNode(iVisited);
 	}
 
 	@Override
 	public Instruction visitCaseNode(CaseNode iVisited) {
 		WhenNode when = (WhenNode) iVisited.getFirstWhenNode();
 		while (when != null) {
 			branchCount++;
 			when = (WhenNode) when.getNextCase();
 		}
 		return super.visitCaseNode(iVisited);
 	}
 	
 	@Override
 	public Instruction visitLocalAsgnNode(LocalAsgnNode iVisited) {
 		locals.add(iVisited.getName());
 		return super.visitLocalAsgnNode(iVisited);
 	}
 
 	public void exitDefnNode(DefnNode iVisited) {
 		if (returnCount > maxReturns) {
			createProblem(iVisited.getNameNode().getPosition(), "Too many explicit returns: " + returnCount);
 		}
 		if (branchCount > maxBranches) {
			createProblem(iVisited.getNameNode().getPosition(), "Too many branches: " + branchCount);
 		}
 		if (locals.size() > maxLocals) {
			createProblem(iVisited.getNameNode().getPosition(), "Too many local variables: " + locals.size());
 		}
 		returnCount = 0;
 		branchCount = 0;
 		locals.clear();
 	}
 
 	@Override
 	public Instruction visitReturnNode(ReturnNode iVisited) {
 		returnCount++;
 		return super.visitReturnNode(iVisited);
 	}
 
 }
