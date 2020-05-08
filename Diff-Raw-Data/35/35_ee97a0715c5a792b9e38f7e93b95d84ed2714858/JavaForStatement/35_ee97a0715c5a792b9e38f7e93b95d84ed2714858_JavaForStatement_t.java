 package translator.Statements;
 
 import java.util.ArrayList;
 import xtc.tree.Node;
 import xtc.tree.GNode;
 
class JavaForStatement extends JavaStatement{
 	JavaForStatement(JavaScope scope, GNode n){
 		super(scope, n);
 	}
	public JavaBasicForControl visitBasicForControl(GNode n){
		return new JavaBasicForControl(this,n);
	}
	public CodeBlock printMe(CodeBlock block){
		return new CodeBlock(block,"for (" + header.printMe + ")",true).close();  /** THEN WHERE IS THE BEEF?? **/
		/** i.e. what to do with the printing of subscope(i) **/
 	}
 }
