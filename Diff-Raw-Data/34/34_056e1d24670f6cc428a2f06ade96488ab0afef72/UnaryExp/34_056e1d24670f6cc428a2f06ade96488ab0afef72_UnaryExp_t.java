 package translator.Expressions;
 
 import translator.JavaType;
 import translator.JavaScope;
import translator.JavaStatic;
 import translator.Statements.JavaStatement;
 import translator.Printer.CodeBlock;
 
 import xtc.tree.GNode;
 
 /**
  * An expression with two operands and an operator.
  */
 public class UnaryExp extends JavaExpression {
 	/**
 	 * the operator
 	 */
 	String operator;
 
 	/**
 	 * the operand.
 	 */
 	JavaExpression operand;
 	public UnaryExp(JavaScope scope, GNode n) {
 		super(scope, n);
 	}
 	protected void onInstantiate(GNode n) {
		JavaStatic.dumpNode(n);
 		this.operand = (JavaExpression)this.dispatch((GNode)n.get(1));
 		this.operator = (String)n.get(0);
		System.out.println(operand.getType().getCppName());
		this.setType(operand.getType());
 	}
 	public String print() {
 		return "(" +  operator  + operand.print()+")";
 	}
 }
