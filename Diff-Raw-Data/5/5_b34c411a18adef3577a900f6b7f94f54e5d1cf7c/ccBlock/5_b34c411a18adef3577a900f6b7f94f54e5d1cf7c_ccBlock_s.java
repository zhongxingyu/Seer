 package oop;
 
 
 /*
  * Current state: Mitigating responsibilities from ccBlock to ccDeclaration and ccExpression. Further functionality going to
  * ccStatement.
  */
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 
 class ccBlock extends Visitor{
 	
 	public LinkedList<String> blockLines;
 	public LinkedList<String> declared;
 	public HashMap<String, ccVariable> variables;
 	public HashMap<String, String> localVariables;
 	public LinkedList<ccClass> classList;
 	public String currentClass;
 	private boolean isConstructorBlock;
 	
 	public ccBlock(){
 		localVariables = new HashMap<String, String>();
 		blockLines = new LinkedList<String>();
 	}
 
 	public ccBlock(GNode n, HashMap var, HashMap<String, String> parameterNamesTypes, LinkedList<ccClass> classes, String currentc, boolean construct) {
 		blockLines = new LinkedList<String>();
 		localVariables = parameterNamesTypes;
 		classList = classes;
 		currentClass = currentc;
 		variables = var;
 		
 		for(String s : parameterNamesTypes.keySet()){
 			variables.put(s, new ccVariable(s,parameterNamesTypes.get(s)));
 		}
 		isConstructorBlock = construct;
 		blockLines.add("{\n");
 		visit(n);
 		blockLines.add("}");
 	}
 	
 	public void visitFieldDeclaration(GNode n){
 		String name = (String)n.getNode(2).getNode(0).getString(0);
 		String type = ccHelper.convertType((String)n.getNode(1).getNode(0).getString(0));
		if(n.getNode(2).getNode(0).getNode(1)!=null && n.getNode(2).getNode(0).getNode(1).hasName("Dimensions")){ 
			type = "__rt::Ptr<__rt::Array<" + type + "> >"; 
 		}
 		variables.put(name, new ccVariable(name, type));
 		localVariables.put(name, type);
 		ccDeclaration declarationStatement = new ccDeclaration(n, this);
 		declarationStatement.changeTypeTo(type);
 		blockLines.add(" " + declarationStatement.publish() + "\n");
 	}
 
 	
 	//TODO: Next step = expression statements and all components
 	public void visitExpressionStatement(GNode n){
 		ccExpression expressionStatement = new ccExpression(n, this);
 		blockLines.add("  " + expressionStatement.publish() + "\n");
 	}
 
 	public void visitBlock(GNode n){
 		ccBlock blockStatement = new ccBlock(n, variables, localVariables, classList, currentClass, isConstructorBlock);
 		blockLines.add("  {\n");
 		blockLines.add("  " + blockStatement.publish());
 		blockLines.add("  }\n");
 	}
 
 
 	public void visitConditionalStatement(GNode n){
 		ccStatement ifLine = new ccStatement(n, this);
 		blockLines.add("  " + ifLine.line + "\n");
 	}
 	public void visitForStatement(GNode n){
 		ccStatement forLine = new ccStatement(n, this);
 		blockLines.add("  " + forLine.line + "\n");
 	}
 	public void visitBreakStatement(GNode n){
 		ccStatement breakLine = new ccStatement(n, this);
 		blockLines.add("  " + breakLine.line + "\n");
 	}
 	public void visitWhileStatement(GNode n){
 		ccStatement whileLine = new ccStatement(n, this);
 		blockLines.add("  " + whileLine.line + "\n");
 	}
 	public void visitReturnStatement(GNode n){
 		ccStatement whileLine = new ccStatement(n, this);
 		blockLines.add("  " + whileLine.line + "\n");
 	}
 	
 	public boolean getIsConstructorBlock(){
 		return isConstructorBlock;
 	}
 	
 	public HashMap<String, String> getLocalVariables(){
 		return localVariables;
 	}
 	public void addLine(String s){
 		blockLines.removeLast();
 		blockLines.add(s);
 		blockLines.add("}");
 	}
 	public void addLineFront(String s){
 		blockLines.removeFirst();
 		blockLines.addFirst(s);
 		blockLines.addFirst("{\n");
 	}
 	public LinkedList<String> publish() {
 		return blockLines;
 	}
 	
 	/**
 	 * HORRIBLE BRUTE FORCE STRING CHECK
 	 */
 	public boolean hasDeclared(String var){
 		for(String iter : blockLines){
 			if (iter.trim().startsWith(var + " =")) return true;
 		}
 		return false;
 	}
 	
 	public void visit(Node n) {
 		for (Object o : n){
 			if (o instanceof Node){
 				dispatch((Node)o);
 			}
 		}
 	}
 }
