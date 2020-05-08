 /**
  * 
  */
 package back_end;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import util.type.Types;
 
 import util.ast.AbstractSyntaxTree;
 import util.ast.node.ArgumentsNode;
 import util.ast.node.BiOpNode;
 import util.ast.node.CatchesNode;
 import util.ast.node.ConstantNode;
 import util.ast.node.DerivedTypeNode;
 import util.ast.node.ElseIfStatementNode;
 import util.ast.node.ElseStatementNode;
 import util.ast.node.ExceptionTypeNode;
 import util.ast.node.ExpressionNode;
 import util.ast.node.FunctionNode;
 import util.ast.node.GuardingStatementNode;
 import util.ast.node.IdNode;
 import util.ast.node.IfElseStatementNode;
 import util.ast.node.IterationStatementNode;
 import util.ast.node.JumpStatementNode;
 import util.ast.node.JumpStatementNode.JumpType;
 import util.ast.node.MockExpressionNode;
 import util.ast.node.MockNode;
 import util.ast.node.Node;
 import util.ast.node.ParametersNode;
 import util.ast.node.PostfixExpressionNode;
 import util.ast.node.PrimaryExpressionNode;
 import util.ast.node.PrimitiveTypeNode;
 import util.ast.node.ProgramNode;
 import util.ast.node.RelationalExpressionNode;
 import util.ast.node.ReservedWordTypeNode;
 import util.ast.node.SectionNode;
 import util.ast.node.SectionTypeNode;
 import util.ast.node.SelectionStatementNode;
 import util.ast.node.StatementListNode;
 import util.ast.node.StatementNode;
 import util.ast.node.SwitchStatementNode;
 import util.ast.node.TypeNode;
 import util.ast.node.UnOpNode;
 
 /**
  * Visitor class for error checking.
  * 
  * This is the second walk performed after construction of the AST from source.
  * 
  * Performs the following validations: - no dead code (statements after a return
  * statement in the same basic block) - no break/continue statements outside of
  * iteration loops - non-void functions have adequate number of return
  * statements - no case/default statements outside of immediate switch statement
  * - can't catch same exception type more than once
  * 
  * 
<<<<<<< HEAD
 * @author paul
 * 
=======
  * @author Paul Tylkin
  *
>>>>>>> 5830584145c1e2457307f8f8e3b27274720c49c3
  */
 
 public class ErrorCheckingVisitor implements Visitor {
 
 	protected AbstractSyntaxTree tree;
 
 	protected final static Logger LOGGER = Logger
 			.getLogger(ErrorCheckingVisitor.class.getName());
 
 	private static List<Boolean> returnFlagStack = new ArrayList<Boolean>();
 
 	private static void pushReturnStack() {
 		returnFlagStack.add(returnFlagStack.get(returnFlagStack.size() - 1));
 	}
 
 	private static void popReturnStack() {
 		returnFlagStack.remove(returnFlagStack.size() - 1);
 	}
 
 	public ErrorCheckingVisitor(AbstractSyntaxTree tree) {
 		this.tree = tree;
 	}
 
 	public void walk() {
 		returnFlagStack.add(false);
 		ProgramNode treeRoot = (ProgramNode) this.tree.getRoot();
 		visitReturnChildren(treeRoot);
 		visitFunctionReturns(treeRoot);
 	}
 
 	private void visitReturnChildren(Node node) {
 		/*
 		 * Constructs of the form
 		 * 
 		 * int doubleint (int x){ return 2*x;a x = 2*x; }
 		 * 
 		 * will not compile into Java, and so if a Hog program has this
 		 * construct, Hog will throw an unreachable code exception. Similarly,
 		 * 
 		 * int max(int a, int b){ if (a > b){ return a; } else{ return b; }
 		 * return 0; }
 		 * 
 		 * will also throw an error in Java.
 		 * 
 		 * This pass through the program will check that there is no unreachable
 		 * code in a Hog program.
 		 */
 		if (node.isNewScope()) {
 			LOGGER.finer("We are in a new scope now in node " + node);
 			this.pushReturnStack();
 		}
 		if (returnFlagStack.get(returnFlagStack.size() - 1)) {
 			// throw new UnreachableCodeError(
 			// "The following statement is unreachable: "+ node.toSource());
 		}
 		if (node instanceof JumpStatementNode
 				&& ((JumpStatementNode) node).getJumpType() == JumpType.RETURN) { // TODO
 																					// check
 																					// that
 																					// is
 																					// a
 																					// return
 																					// node
 			if (returnFlagStack.get(returnFlagStack.size() - 1)) {
 				// throw new UnreachableCodeError(
 				// "The following statement is unreachable: "+ node.toSource());
 			} else {
 				returnFlagStack.set(returnFlagStack.size() - 1, true);
 			}
 		} else {
 			List<Node> children = node.getChildren();
 			for (Node n : children) {
 				visitReturnChildren(n);
 			}
 		}
 
 		if (node.isNewScope()) {
 			this.popReturnStack();
 		}
 	}
 
 	private boolean nonVoidFunctionFlag;
 
 	private void visitFunctionReturns(Node node) {
 		/*
 		 * Constructs of the form int max(int a, int b){ if (a > b){ return a; }
 		 * if (b >= a){ return b; } } will not compile into Java, saying that
 		 * there is an error in the return type, even though the function does
 		 * return the correct value. This pass through the Hog program will
 		 * throw Hog errors on this type of input.
 		 */
 		if (node instanceof FunctionNode) {
 			List<Node> children = node.getChildren();
 			/*
 			 * if (!Types.isVoidType(((FunctionNode)node).getType())){ //if
 			 * return type is not void this.nonVoidFunctionFlag = true; for
 			 * (Node n : children) { if(n instanceof JumpStatementNode &&
 			 * ((JumpStatementNode) n).getJumpType() == JumpType.RETURN){
 			 * this.nonVoidFunctionFlag = false; } }
 			 * 
 			 * 
 			 * if (this.nonVoidFunctionFlag){ // throw newMissingReturnError(
 			 * "The following function is missing a return statement: "+
 			 * node.toSource()); } }
 			 */
 		}
 		// System.out.println(nonVoidFunctionFlag);
 		List<Node> children = node.getChildren();
 
 		for (Node n : children) {
 			visitFunctionReturns(n);
 		}
 	}
 
 	@Override
 	public void visit(ArgumentsNode node) {
 		LOGGER.finer("visit(ArgumentsNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(BiOpNode node) {
 		LOGGER.finer("visit(BiOpNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(CatchesNode node) {
 		LOGGER.finer("visit(CatchesNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(ConstantNode node) {
 		LOGGER.finer("visit(ConstantNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(DerivedTypeNode node) {
 		LOGGER.finer("visit(DerivedTypeNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(ElseIfStatementNode node) {
 		LOGGER.finer("visit(ElseIfStatementNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(ElseStatementNode node) {
 		LOGGER.finer("visit(ElseStatementNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(ExceptionTypeNode node) {
 		LOGGER.finer("visit(ExceptionTypeNode node) called on " + node);
 	}
 
 	public void visit(ExpressionNode node) {
 		LOGGER.finer("visit(ExpressionNode node) called on " + node);
 	}
 
 	public void visit(FunctionNode node) {
 		LOGGER.finer("visit(FunctionNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(GuardingStatementNode node) {
 		LOGGER.finer("visit(GuardingStatementNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(IdNode node) {
 		LOGGER.finer("visit(IdNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(IfElseStatementNode node) {
 		LOGGER.finer("visit(IfElseStatementNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(IterationStatementNode node) {
 		LOGGER.finer("visit(IterationStatementNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(JumpStatementNode node) {
 		LOGGER.finer("visit(JumpStatementNode node) called on " + node);
 
 	}
 
 	@Override
 	public void visit(MockExpressionNode node) {
 		LOGGER.finer("visit(MockExpressionNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(MockNode node) {
 		LOGGER.finer("visit(MockNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(Node node) {
 		LOGGER.finer("visit(Node node) called on " + node);
 	}
 
 	@Override
 	public void visit(ParametersNode node) {
 		LOGGER.finer("visit(ParametersNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(PostfixExpressionNode node) {
 		LOGGER.finer("visit(PostfixExpressionNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(PrimaryExpressionNode node) {
 		LOGGER.finer("visit(PrimaryExpressionNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(PrimitiveTypeNode node) {
 		LOGGER.finer("visit(PrimitiveTypeNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(ProgramNode node) {
 		LOGGER.finer("visit(ProgramNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(RelationalExpressionNode node) {
 		LOGGER.finer("visit(RelationalExpressionNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(SectionNode node) {
 		LOGGER.finer("visit(SectionNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(SectionTypeNode node) {
 		LOGGER.finer("visit(SectionTypeNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(SelectionStatementNode node) {
 		LOGGER.finer("visit(SelectionStatementNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(StatementListNode node) {
 		LOGGER.finer("visit(StatementListNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(StatementNode node) {
 		LOGGER.finer("visit(StatementNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(SwitchStatementNode node) {
 		LOGGER.finer("visit(SwitchStatementNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(TypeNode node) {
 		LOGGER.finer("visit(TypeNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(UnOpNode node) {
 		LOGGER.finer("visit(UnOpNode node) called on " + node);
 	}
 
 	@Override
 	public void visit(ReservedWordTypeNode node) {
 		LOGGER.finer("visit(ReservedWordTypeNode node) called on " + node);
 	}
 }
