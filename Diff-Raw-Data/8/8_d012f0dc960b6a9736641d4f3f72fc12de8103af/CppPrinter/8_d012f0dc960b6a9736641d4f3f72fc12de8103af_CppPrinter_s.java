 
 /*
  * Object-Oriented Programming
  * Copyright (C) 2010 Robert Grimm
  * Created by Paige Ponzeka
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * version 2 as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
  * USA.
  */
 package xtc.oop;
 
 import xtc.lang.JavaFiveParser;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 import xtc.util.Tool;
 /**A "Stupid" printer, it just prints everything it sees and assumes that every String inside the AST is proper C++ code, there are special catches and cases
  for brackets, etc but there is no intelligent code in here */
 public class CppPrinter extends Visitor
 {
 	private StringBuilder printer; //a StringBuilder that stores the code translated by the printer
 	public final boolean DEBUG = false;
 	public boolean isPrivate; //a global boolean that keeps track of the current modifier status
 	public CppPrinter(GNode n)
 	{
 		printer = new StringBuilder(); //intialize Stringbuilder
 		isPrivate =false; //sets false by default since structs are public by default
 		visit(n); //visit the given node (starts all the visiting)
 		if(DEBUG){System.out.println(printer);}
 	}
 	/*visit cast expression and print the c++ version of the java AST values*/
 	public void visitCastExpression(GNode n)
 	{
 		printer.append("(");
 		//visit the cast type
 		Node type = n.getNode(0);
 		dispatch(type);
 		
 		printer.append(")");
 		//visit the next batch of code
 		Node next=n.getNode(1);
 		dispatch(next);
 		
 	}
 	/***********************Expressions***********************/
 	
 	/*Visit a conditional expression and print the c++ equivalent**/
 	public void visitConditionalExpression(GNode n)
 	{
 		printer.append("(");
 		//visit the expression Node
 		Node express = n.getNode(0);
 		dispatch(express);
 		//append the default strings not in the AST
 		printer.append(") ? ");
 		//get the node at location 1
 		Node one = n.getNode(1);
 		dispatch(one);
 		
 		printer.append(" : ");
 		//get the node at location 2
 		Node two = n.getNode(2);
 		dispatch(two);
 	}	
 	/** Visit the selection expression i.e. java.lang*/
 	public void visitSelectionExpression(GNode n)
 	{
 		//prints the selection expression
 		Node prim = n.getNode(0);
 		dispatch(prim);
 		//print a . at the end of each expression
 		printer.append(".");
 		Object o =n.get(1);
 		if(o instanceof String) //make sure the object o is an string
 		{
 			printer.append((String)o);
 		}
 		else { //other wise its a Node and call dispatch on it to visit it
 			dispatch((Node) o);
 		}
 	}
 	/**calls the default binary behavor on relational expressions */
 	public void visitRelationalExpression(GNode n)
 	{
 		setBinary(n);		
 	}
 	/**prints default c++ code and calls default binary behavor on Equality Expressions */
 	public void visitEqualityExpression(GNode n)
 	{
 		printer.append("(");		
 		setBinary(n);		
 		printer.append("){ \n");
 	}//end of visitCallExpression method
 	
 	/** calls default binary behavior on Expression */
 	public void visitExpression(GNode n)
 	{
 		setBinary(n);
 	}
 	/** calls default unary behavior on Expression */
 	public void visitUnaryExpression(GNode n)
 	{
 		setUnary(n);
 	}
 	/**calls default unary behavior on UnaryExpression*/
 	public void visitUnaryExpressionNotPlusMinus(GNode n)
 	{
 		setUnary(n);
 	}
 	/*calls default unary behavior*/
 	public void visitPreDecrementExpression(GNode n)
 	{
 		setUnary(n);
 	}
 	/**calls default unary behavior */
 	public void visitPreIncrementExpression(GNode n)
 	{
 		setUnary(n);
 	}
 	/**calls default unary behavior */
 	public void visitPostfixExpression(GNode n)
 	{
 		setUnary(n);
 	}
 	/**calls default unary behavior */
 	public void visitShiftExpression(GNode n)
 	{
 		setUnary(n);
 	}
 	/**calls default binary behavior */
 	public void visitConditionalOrExpression(GNode n)
 	{
 		setBinary(n);
 	}
 	/**calls default binary behavior */
 	public void visitConditionalAndExpression(GNode n)
 	{
 		setBinary(n);
 	}
 	/**calls default binary behavior */
 	public void visitInclusiveOrExpression(GNode n)
 	{
 		setBinary(n);
 	}
 	/**calls default binary behavior */
 	public void visitExculsiveOrExpression(GNode n)
 	{
 		setBinary(n);
 	}
 	/**calls default binary behavior */
 	public void visitAndExpression(GNode n)
 	{
 		setBinary(n);
 	}
 	//visits the 3rd node of Expression and dispatches on it to print the subtree properly */
 	public void visitExpression(Node n)
 	{
 		Node b=n.getNode(3);
 		dispatch(b);
 	}
 	/*calls the default binary behavior*/
 	public void visitAdditiveExpression(GNode n)
 	{
 		setBinary(n);
 	}
 	/*replaces "this" with __this for propery c++ conversion*/
 	public void visitThisExpression(GNode n)
 	{
 		printer.append("__this.");
 		visit(n);
 	}
 	/**does nothing but print code *place holder**/
 	public void visitSuperExpression(GNode n)
 	{
 		//printer.append("super.");
 		visit(n);
 	}
 	/**set the default binary behavior*/
 	public void visitMultiplicativeExpression(GNode n)
 	{
 		setBinary(n);
 	}	
 	/*******************Statements ******************************/
 	/**print java asser code currently not a priority*/
 	public void visitAssertStatement(GNode n)
 	{
 		printer.append("assert ");
 		Node express=n.getNode(0);
 		dispatch(express);
 		printer.append(":");
 		Node express2=n.getNode(1);
 		dispatch(express2);
 		printer.append("; \n");
 	}
 	/**print java sychronizedstatement currently not a priority */
 	public void visitSychronizedStatement(GNode n)
 	{
 		printer.append("sychronized (");
 		Node expression=n.getNode(0);
 		dispatch(expression);
 		printer.append("){\n");
 		Node block = n.getNode(1);
 		dispatch(block);
 		printer.append("}\n");
 		
 	}
 	/**print java labeled statement */
 	public void visitLabeledStatement(GNode n)
 	{
 		printer.append(n.getString(0)+":\n");
 		for(int i=1;i<n.size();i++)
 		{
 			Object o=n.get(i);
 			if(o instanceof String) //check if o is an instance of a string
 			{
 			}
 			else if(o instanceof Node)
 			{
 				dispatch((Node) o);
 			}
 		}	
 	}
 	/*print c++ break statement  call setBreCon to get code if inside of break statement*/
 	public void visitBreakStatement(GNode n)
 	{
 		printer.append("break");
 		setBreCon(n);
 	}
 	/**visit contrinue stats ment and call setBreCon to get code if any inside of continue statement*/
 	public void visitContinueStatement(GNode n)
 	{
 		printer.append("continue");
 		setBreCon(n);
 	}
 	/**get code inside of trycatchfinallystatement*/
 	public void visitTryCatchFinallyStatement(GNode n)
 	{
 		
 		printer.append("try {\n");
 		Node block = n.getNode(0);
 		dispatch(block);
 		printer.append("} ");
 		for (int i=1; i<n.size(); i++) {
 			Node catch1 = n.getNode(i);
 			dispatch(catch1);
 		}
 	}
 	//print throw statement (handling excpetions? */
 	public void visitThrowStatement(GNode n)
 	{
 		printer.append("throw ");
 		visit(n);
 		printer.append(";\n}\n");
 	}
 	/**print the c++ return statement */
 	public void visitReturnStatement(GNode n)
 	{
 		printer.append("return ");
 		visit(n);
 		printer.append("; \n");
 	}
 	/**Visit Expression append a ;*/
 	public void visitExpressionStatement(GNode n)
 	{
 		visit(n);
 		printer.append(";\n");
 	}
 	/**visit switch statement and below case clause*/
 	public void visitSwitchStatement(GNode n)
 	{
 		printer.append("switch(");
 		Node h= n.getNode(0);
 		dispatch(h);
 		printer.append(")\n");
 		printer.append("{\n");
 		for(int i=1; i<(n.size());i++)
 		{
 			Node cases= n.getNode(i);
			dispatch(cases);e
 		}
 		printer.append("}\n");
 	}
 	public void visitCaseClause(GNode n)
 	{
 		printer.append("case");
 		Node theCase= n.getNode(0);
 		dispatch(theCase);
 		printer.append(": ");
 		Node theExp= n.getNode(1);
 		dispatch(theExp);	
 		Node break1= n.getNode(2);
 		dispatch(break1);	
 	}
 	/*visit conditionalstatement aka if*/
 	public void visitConditionalStatement(GNode n)
 	{
 		printer.append("if(");
 		Node express=n.getNode(0);
 		dispatch(express);
 		printer.append("){\n");
 		Node block = n.getNode(1);
 		dispatch(block);
 		printer.append("}\n");
 		for(int i=2;i<n.size();i++)
 		{
 			printer.append("else ");
 			Node cond =n.getNode(i);
 			if(cond.getName().equals("Block")){
 				printer.append("{ \n");
 				dispatch(cond);
 				printer.append("} \n");
 			}
 			else
 			{
 				dispatch(cond);
 			}
 			
 		}
 		
 	}	
 	////////////////Loops////////////////////////
 	public void visitDoWhileStatement(GNode n)
 	{
 		printer.append("do{\n");
 		Node block= n.getNode(0);
 		dispatch(block);
 		printer.append("}while(");
 		Node expression= n.getNode(1);
 		dispatch(expression);
 		printer.append(");\n");
 	}
 	public void visitWhileStatement(GNode n)
 	{
 		printer.append("\n while");
 		visit(n);
 		printer.append("} \n");
 	}
 	public void visitForUpdate(Node n)
 	{
 		Node d= n.getNode(4);
 		dispatch(d);
 	}
 	public void visitForInit(Node n)
 	{
 		Node b=n.getNode(0);
 		dispatch(b);
 		Node c= n.getNode(1);
 		dispatch(c);
 		Node d= n.getNode(2);
 		dispatch(d);	
 	}	
 	public void visitBasicForControl(GNode n)
 	{
 		//basic control node
 		Node a=n.getNode(0);
 		printer.append("(");
 		visitForInit(a);
 		printer.append(";");
 		visitExpression(a);
 		printer.append(";");
 		visitForUpdate(a);
 		printer.append(")");
 	}
 	public void visitForStatement(GNode n)
 	{
 		printer.append("for");
 		visitBasicForControl(n);
 		printer.append("{\n");
 		Node f= n.getNode(1);
 		visit(f);
 		printer.append("}\n");	
 	}	/***********************Classes ******************************/
 	public void visitNewClassExpression(GNode n)
 	{
 		printer.append("new ");
 		for(int i=0;i<n.size();i++)
 		{
 			if (i==3) {
 				printer.append("(");
 			}
 			Object o=n.get(i);
 			if(o instanceof String)
 			{
 				printer.append(" __"+(String)o);
 			}
 			else if(o instanceof Node)
 			{
 				dispatch((Node) o);
 			}
 			if (i==3) {
 				printer.append(")");
 			}			
 		}
 	}
 	public void visitCallExpression(GNode n)
 	{
 		Node primary1 = n.getNode(0);
 		dispatch(primary1);
 		Object mid = n.get(1);
 		if (mid instanceof Node) {
 			dispatch((Node) mid);
 		}
 		//printer.append("->vptr->");
 		Object name =n.get(2);
 		if(name instanceof Node)
 		{
 			dispatch((Node)mid);
 		}
 		else if(name instanceof String)
 		{
 			printer.append((String)name);
 		}
 		Object arguments = n.get(3);
 		printer.append("(");
 		if(arguments instanceof Node)
 		{
 			dispatch((Node)arguments);
 		}
 		printer.append(")");
 	}
 	public void visitQualifiedIdentifier(GNode n)
 	{
 		
 		
 		for(int i=0; i<n.size();i++)
 		{
 			String name = n.getString(i);
 			if(i>0){
 				printer.append(".");
 			}
 			if(name.equals("String"))
 			{
 			}
 			else {
 				printer.append("__");
 			}
 			printer.append(name);
 			
 		}	
 	}
 	/**********************Other***************************/
 	public void visitArguments(GNode n)
 	{
 		for (int i=0; i<n.size(); i++) {
 			
 			dispatch(n.getNode(i));
 			if(i!=(n.size()-1))
 			{
 				printer.append(", ");
 			}
 		}
 	}
 	public void visitFormalParameter(GNode n)
 	{
 		for(int i=0;i<n.size();i++)
 		{
 			Object o=n.get(i);
 			if(o instanceof String)
 			{
 				printer.append(" "+(String)o);
 			}
 			else if(o instanceof Node)
 			{
 				dispatch((Node) o);
 			}
 		}
 	}
 			
 	public void visitCatchClause(GNode n)
 	{
 		printer.append("catch(");
 		Node io = n.getNode(0);
 		dispatch(io);
 		printer.append("){\n");
 		Node block = n.getNode(1);
 		dispatch(block);
 	}
 	public void visitDefaultClause(GNode n)
 	{
 		printer.append("default");
 		printer.append(": ");
 		Node theExp= n.getNode(0);
 		dispatch(theExp);	
 		Node break1= n.getNode(1);
 		dispatch(break1);	
 		
 	}
 	public void visitFieldDeclaration(GNode n)
 	{
 		visit(n);
 		printer.append(";\n");
 	}
 	public void visitInitializer(GNode n)
 	{
 		printer.append("static ");
 		Node block= n.getNode(1);
 		dispatch(block);
 	}
 	public void visitLocalVariableDeclaration(GNode n)
 	{
 		printer.append("final ");
 		Node type= n.getNode(1);
 		for(int i=2;i<n.size();i++)
 		{
 			Node vd = n.getNode(i);
 			dispatch(vd);
 			if(i>2 && i<n.size()-1)
 			{
 				printer.append(" , ");
 			}
 		}	
 	}
 	public void visitModifier(GNode n)
 	{
 		//run check to see if isPrivate is one
 		//visit every modifier of the given variable
 		for(int i =0; i<n.size(); i++)
 		{
 			String modifier = n.getString(i);
 			//if the modifier is a public and isPrivate is one print it
 			if(modifier.equals("public")&& isPrivate)
 			{//if the modifier is public and the current modifier status is set to private
 				printer.append("public: \n");
 				isPrivate = false;
 			}
 			else if(modifier.equals("private") && (!isPrivate))
 			{//if the modifier is private and the current modifier status is set to private
 				printer.append("private: \n");
 				isPrivate = true;
 			}
 			else if (!(modifier.equals("public"))&&(!(modifier.equals("private")))&&(!(modifier.equals("protected")))){
 				//if the modifiers are anything but public, private, protected just print them normally
 				printer.append(modifier+ " ");
 			}
 		}
 	}
 	/**Visit the declarators */
 	public void visitDeclarator(GNode n)
 	{
 		//append the declarator name
 		printer.append(" " +n.getString(0));
 		//get the object at position 1 and check to make sure its not null
 		Object one = n.get(1);
 		if(one!=null)
 		{//check the instance of the object and decide what to do with it
 			if (one instanceof String ) {
				printer.append(n.getString(1);
 			}
 			else if (one instanceof Node)
 			{
 				dispatch((Node) one);
 			}
 		}
 		//do the same with object at position 2
 		Object two = n.get(2);
 							   
 		if(two!=null)
 		{
 			printer.append(" = ");
 			if (two instanceof String ) {
				printer.append(n.getString(2);
 							   }
 			else if (two instanceof Node)
 			{
 				dispatch((Node) two);
 			}
 		}
 		printer.append("");
 	}
 							   
 	//edit the visit method to print out instances of strings if not it visits the node						   
 	public void visit(Node n)
 	{
 		for (int i=0; i<n.size(); i++) { //visit every child of Node n if its an instance of a string print it
 			Object k=n.get(i);
 			if(k instanceof Node) 
 			{
 			}
 			else {
 				if(!((n.getName().equals("MethodDeclaration"))||(n.getName().equals("ConstructorDeclaration"))))
 				{
 					if(n.getString(i)!=null)
 					{		////add the string to the printer
 						printer.append(n.getString(i));
 					}
 				}
 			}
 		}
 		for(Object o:n) {
 			if(o instanceof Node) dispatch((Node) o);
 		}
 	}	
 	public void setBreCon(GNode n)
 	{
 		//checks for calls inside break and continue statements and prints those values
 
 		for (int i=0; i<n.size(); i++) {//for every child of N get it and check to see if its an instance of string or Node
 			Object o=n.get(i);
 			if (o!=null) {
 			}
 			
 			if(o instanceof String)//if object o is a string print it
 			{
 				printer.append(" "+(String)o);
 			}
 			else if(o instanceof Node) //if its a node call dispatch on it
 			{
 				dispatch((Node) o);
 			}
 			if (o!=null) {
 				if(i!=(n.size()-1)){
 				}
 							   
 			}
 		}
 		printer.append(";\n"); //append a new link 
 	}
 	/**for expressions "recursively" calls dispatch on the operands and print the middle operator*/						   
 	public void setBinary(GNode n)
 	{
 		//get the first operand
 		Node operand1= n.getNode(0);
 		dispatch(operand1);
 		printer.append(n.getString(1)); //print the operator		
 		//get the second operand
 		Node operand2= n.getNode(2);
 		dispatch(operand2);	
 	}
 	/*public void visitPrimaryPrefix(GNode n)
 	{
 		/* f0 -> Literal()
 		 | "this"
 		 | "super" "." <IDENTIFIER>
 		 | "(" Expression() ")"
 		 | AllocationExpression()
 		 | ResultType() "." "class"
 		 | Name()		 
 		 */
 		
 		
 	//}
 	/**for unary expressions such as -A checks to see intances of children */						   
 	public void setUnary(GNode n)
 	{
 		for(int i=0;i<n.size();i++) //for every child in N check its instance and act acoordingl
 		{
 			Object k=n.get(i);
 			if(k instanceof Node) //visit Nodees
 			{
 				dispatch((Node)k);
 			}
 			else { //else print the strings
 					if(n.getString(i)!=null)
 					{		////add the string to the printer
 						printer.append(n.getString(i));
 					}
 			}
 		}
 	}
 	/**@return the public stringBuilder */						   
 	public StringBuilder getString()
 	{
 		return printer;
 	}	
 }
 
