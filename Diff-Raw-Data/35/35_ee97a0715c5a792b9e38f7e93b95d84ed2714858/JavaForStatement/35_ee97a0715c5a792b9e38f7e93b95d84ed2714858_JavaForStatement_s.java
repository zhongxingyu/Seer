 package translator.Statements;
 
 import java.util.ArrayList;
import java.util.LinkedHashMap;

 import xtc.tree.Node;
 import xtc.tree.GNode;
 
class JavaForStatement  extends JavaScope{
	JavaBasicForControl ctrl;
 	JavaForStatement(JavaScope scope, GNode n){
 		super(scope, n);
		this.dispatch(n);
 	}
	public void visitBasicForControl(GNode n){
		ctrl=new JavaBasicForControl(this,n);
 	}
 }
