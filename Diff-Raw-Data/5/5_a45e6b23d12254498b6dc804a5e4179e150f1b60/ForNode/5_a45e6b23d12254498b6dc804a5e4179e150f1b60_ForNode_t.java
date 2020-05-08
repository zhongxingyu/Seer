 package tree;
 
 import java.util.List;
 
 import output.OutputManager;
 import semantic.SemanticManager;
 
 public class ForNode extends ControlFlowNode {
 	private StatementNode statement1; //statementnode
 	private ExpressionNode expression; //expressionnode
	private ExpressionNode statement2; //expression
 	private String recursiveString;
 
	public ForNode(StatementNode s1, ExpressionNode e, ExpressionNode s2) {
 		this.statement1 = s1;
 		this.expression = e;
 		this.statement2 = s2;
 	}
 	
 	@Override
 	public void evaluatePre(int depth, OutputManager out) {
 		// inOrder traversals to get Strings
 //		recursiveString = "";
 //		getFor(statement1);
 //		String for1 = recursiveString;
 //		recursiveString = "";
 //		getFor(expression);
 //		String for2 = recursiveString;
 //		recursiveString = "";
 //		getFor(statement2);
 //		String for3 = recursiveString;
 		
 //		out.writeStatement("for(" + for1 + "; " + for2 + "; " + for3 + ") {\n");
 	}
 
 	@Override
 	public void evaluatePost(int depth, OutputManager out) {
 		out.writeStatement("}\n");
 //		out.closeBlock();
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void analyzePre() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void analyzePost() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void getFor(Node root) {
 		if (root.numChildren() > 0)
 			getFor(root.getChildren().get(0));
 		recursiveString += root.getData();
 		if (root.numChildren() > 1)
 			getFor(root.getChildren().get(1));
 	}
 }
