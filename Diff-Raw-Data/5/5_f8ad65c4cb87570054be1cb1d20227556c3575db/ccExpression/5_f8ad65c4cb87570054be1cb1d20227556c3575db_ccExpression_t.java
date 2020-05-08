 package oop.JavaGrinder.cc;
 
import oop.ccStatement;
 import xtc.tree.GNode;
 import xtc.tree.Visitor;
 
 public class ccExpression extends Visitor{
 	//Expression statements are any single lines within a java method... i think
 	String line;
 	private ccBlock block;
 	
 	public ccExpression(GNode n){
 		dispatch((GNode)n.get(0));
 	}
 	
 	public ccExpression(GNode n, ccBlock parent){
 		block = parent;
 		dispatch((GNode)n.get(0));
 	}
 	
 	public void visitExpression(GNode n){
 		line = new ccStatement(n, block).publish();
 	}
 	public void visitCallExpression(GNode n){
 		line = new ccStatement(n, block).publish();
 	}
 	public void visitPostfixExpression(GNode n){
 		line = new ccStatement(n, block).publish();
 	}
	public void visitNewClassExpression(GNode n){
		line = new ccStatement(n, block).publish();
	}
 	
 	public String publish(){
 		return line + ";";
 	}
 
 }
