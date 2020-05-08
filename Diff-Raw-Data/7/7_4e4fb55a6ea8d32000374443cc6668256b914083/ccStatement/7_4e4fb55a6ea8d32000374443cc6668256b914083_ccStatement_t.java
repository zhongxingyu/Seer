 package oop;
 
 import oop.ccBlock;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 public class ccStatement extends Visitor{
 	String line="";
 	private ccBlock block;
 	
 	public ccStatement(GNode n, ccBlock parent) {
 		block = parent;
 		dispatch(n);
 	}
 	
 	public ccStatement(GNode n) {
 		dispatch(n);
 	}
 	public void visitExpression(GNode n){
 		visit(n);
 	}
 	public void visitReturnStatement(GNode n){
 		line += "return ";
 		visit(n);
 		line+=";";
 	}
 	public void visitCallExpression(GNode n){
 		if(n.getString(2).contains("print")){
 			line+="std::cout << ";
 			dispatch(n.getGeneric(3));
 			if(n.getString(2).equals("println")){
 				line+=" << std::endl";
 			}
 		}
 		else	
 			visit(n);
 	}
 	
 	public void visitMultiplicativeExpression(GNode n){
 		visit(n);
 	}
	
 	public void visitIntegerLiteral(GNode n){
 		line+=(String) n.get(0);
 	}
 	public void visitFloatingPointLiteral(GNode n){
 		line+=(String) n.get(0);
 	}
 	public void visitStringLiteral(GNode n){
 		line+=(String) n.get(0);
 	}
 	public void visitBooleanLiteral(GNode n){
 		line+=(String) n.get(0);		
 	}
 	
 	public void visitBlock(GNode n){
 		ccBlock blockStatement = new ccBlock(n);
 		while (!blockStatement.blockLines.isEmpty())
 			line += blockStatement.blockLines.remove()+"\n";
 	}
 	
 	public void visitConditionalStatement(GNode n){
 		line += "if("+new ccStatement((GNode)n.get(0)).publish()+")\n";
 		dispatch(n.getNode(1));
 		line += "\n else ";
 		dispatch(n.getNode(2));
 		System.out.println(line);
 	}
 	public void visitForStatement(GNode n){
 		line = "for(";
 		dispatch(n.getGeneric(0));
 		line+=")\n";
 		dispatch(n.getGeneric(1));
 	}
 	public void visitBasicForControl(GNode n){
 		dispatch(n.getNode(0));
 		dispatch(n.getNode(1));
 		dispatch(n.getNode(2));
 		dispatch(n.getNode(3));
 		line+=";";
 		dispatch(n.getNode(4));
 	}
 	public void visitBreakStatement(GNode n){
 		line = "break;";
 	}
 	public void visitWhileStatement(GNode n){
 		line = "while("+new ccStatement((GNode)n.get(0)).publish()+")";
 //		System.out.println(line);
 	}
 	public void visitDeclarator(GNode n){
 		line+=n.getString(0);
 		if(n.getNode(1)==null)
 			line+="=";
 		else
 			line+=n.getString(1);
 		dispatch(n.getGeneric(2));
 		line+=";";
 	}
 	public void visitRelationalExpression(GNode n){
 		visit(n);
 	}
 	public void visitBitwiseOrExpression(GNode n){
 		dispatch(n.getNode(0));
 		line+="|";
 		dispatch(n.getNode(1));
 	}
 	public void visitBitwiseAndExpression(GNode n){
 		dispatch(n.getNode(0));
 		line+=" & ";
 		dispatch(n.getNode(1));
 	}
 	public void visitExpressionStatement(GNode n){
 		visit(n);
 		line+=";";
 	}
 	public void visitArguments(GNode n){
 		visit(n);
 	}
 	
 	public void visitSelectionExpression(GNode n){
 		visit(n);
 	}
 	public void visitThisExpression(GNode n){
 		line+="this->";
 	}
 	public void visitPrimaryIdentifier(GNode n){
 		if(block!=null&&!block.getLocalVariables().contains(n.get(0))){
 			line+=(String) "__this::" + n.get(0)+" ";
 		}	
 
 		else{
 			line+=(String) n.get(0)+" ";
 		}
 	}
 	
	public void visitNewClassExpression(GNode n){
		line+= "new __" + n.getNode(2).getString(0) + "()";
	}
	
 	public  String publish() {
 		return line;
 	}
 
 	public void visit(Node n) {
 		for (Object o : n){
 			if (o instanceof Node){
 				dispatch((Node)o);
 			}
 			else if(o instanceof String){
 				line+=o+" ";
 			}
 		}
 	}
 }
