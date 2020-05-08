 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Boyd Hofman
  * Joëll Portier
  * Michiel Westerbeek
  * Tim Waalewijn
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package nl.han.ica.ap.purify.language.java.callgraph.listeners;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Stack;
 
 import nl.han.ica.ap.purify.language.java.JavaBaseListener;
 import nl.han.ica.ap.purify.language.java.JavaParser;
 import nl.han.ica.ap.purify.language.java.callgraph.CallGraph;
 
 /**
  * This listener gathers all methods and variables of a class and adds them to a new ClassNode in the CallGraph.
  * 
  * @author Tim
  */
 public class ClassNodeListener extends JavaBaseListener {
 	/** Booleans to check if listener is in a method or a type. */
 	private boolean methodfound, typefound;
 	
 	/** Name of the current class. */
 	private String classID;
 	
 	/** Name of the current method. */
 	private String methodID;
 	
 	/** Stack with modifiers */
 	private Stack<String> modifierstack;
 	
 	/** Stack for variabletypes */
 	private Stack<String> variabletypestack;
 	
 	/** Types of found variable */
 	private ArrayList<String> variabletype;
 	
 	/** Names of found variable */
 	private ArrayList<String> variableID;
 	
 	/** HashMap to store all variables of this class. */
 	private HashMap<String,HashMap<String,String>> variables;
 	
 	/** HashMap to store all methods of this class. */
 	private HashMap<String,ArrayList<String>> methods;
 	
 	/** The CallGraph currently in use. */
 	public CallGraph graph;
 	
 	public ClassNodeListener(CallGraph graph) {
 		this.graph = graph;
 	}
 	
 	/** 
 	 * First tree node the listener enters.
 	 * Initialize variables here to make parsing multiple files possible. 
 	 */
 	@Override
 	public void enterCompilationUnit(JavaParser.CompilationUnitContext ctx) {
 		modifierstack = new Stack<String>();
 		variabletype = new ArrayList<String>();
 		variableID = new ArrayList<String>();
 		variabletypestack = new Stack<String>();
 		variables = new HashMap<String,HashMap<String,String>>();
 		methods = new HashMap<String,ArrayList<String>>();
 		methodfound = false;
 		typefound = false;
 		classID = null;
		methodID = null;
 	}
 	
 	/**
 	 * Called when done with parsing.
 	 * Adds a new node of this class to the CallGraph.
 	 * After the new node is created map all variables to the node.
 	 */
 	@Override
 	public void exitCompilationUnit(JavaParser.CompilationUnitContext ctx) {
		if(classID != null) {
			graph.addNode(classID, methods);
			graph.mapVariables(classID, variables);
		}
 	}
 	
 	/**
 	 * Called when entering class declaration.
 	 * Sets the current class name to classID.
 	 * If type != null this class extends another class.
 	 */
 	@Override
 	public void enterNormalClassDeclaration(JavaParser.NormalClassDeclarationContext ctx) {		
 		classID = ctx.Identifier().getText();
 		if(ctx.type() != null) {
 			//TODO: Add 'extends' functionality here.
 		}
 	}
 	
 	/**
 	 * Detects if current ClassBodyDeclaration node has a method sub-tree.
 	 */
 	@Override
 	public void enterClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
 		if(ctx.memberDecl().memberDeclaration() != null) {
 			if(ctx.memberDecl().memberDeclaration().fieldDeclaration() == null) {
 				methodfound = true;
 			}
 		} else {
 			if(ctx.memberDecl().classDeclaration() == null) {
 				methodfound = true;
 			}
 		}
 	}
 	
 	/**
 	 * Used to find method modifiers (public/private/protected/static etc.)
 	 * If a method was found while entering ClassBodyDeclaration sub-tree, add modifiers to stack.
 	 * If ctx.modifier().size() == 0 default to public method. 
 	 */
 	@Override
 	public void enterModifiers(JavaParser.ModifiersContext ctx) {
 		if(methodfound) {
 			if(ctx.modifier().size() > 0) {
 				for(JavaParser.ModifierContext mctx : ctx.modifier()) {
 					modifierstack.push(mctx.getText());
 				}
 			} else {
 				modifierstack.push("public");
 			}
 		}
 	}
 	
 	/**
 	 * Gets the return type and name of a method if one was found.
 	 */
 	@Override
 	public void enterMemberDecl(JavaParser.MemberDeclContext ctx) {
 		if(methodfound) {
 			if(ctx.memberDeclaration() == null) {
 				methodID = ctx.Identifier().getText();
 				modifierstack.push("void");
 			} else if(ctx.memberDeclaration().methodDeclaration() != null) {
 				methodID = ctx.memberDeclaration().methodDeclaration().Identifier().getText();
 				modifierstack.push(ctx.memberDeclaration().type().getText());
 			}
 		}
 	}
 	
 	/**
 	 * If method was found add methodID + all modifiers to methods HashMap
 	 */
 	@Override
 	public void exitMemberDecl(JavaParser.MemberDeclContext ctx) {
 		if(methodfound) {
 			mapMethod();
 			methodfound = false;
 		}
 	}
 	
 	/**
 	 * Called when exiting a section of the class body (global variables/constructor/methods etc.)
 	 * If no method was found yet (methodID == null) add an extra method to represent calls made to constructors.
 	 * Always map any variables that were found. Global variables are marked with 'this' as the method they were in.
 	 */
 	@Override
 	public void exitClassBodyDeclaration(JavaParser.ClassBodyDeclarationContext ctx) {
 		if(methodID == null) {
 			methodID = "this";
 			mapMethod();
 		}
 		mapVariable();
 	}
 	
 	/**
 	 * Called when entering parameter block.
 	 * Appends "( " to the current methodID.
 	 */
 	@Override
 	public void enterFormalParameters(JavaParser.FormalParametersContext ctx) {
 		if(methodfound) {
 			methodID = methodID + "( ";
 		}
 	}
 	
 	/**
 	 * Called when exiting parameter block.
 	 * Appends ")" to close current methodID and maps any variables found.
 	 */
 	@Override
 	public void exitFormalParameters(JavaParser.FormalParametersContext ctx) {
 		if(methodfound) {
 			methodID = methodID + ")";
 			mapVariable();
 		}
 	}
 	
 	/**
 	 * Called when entering parameter declarations.
 	 * If a method was found add any variable types found to the stack and also append it to the methodID.
 	 * exmpl: testmethod(String s, int i) -> testmethod( String -> testmethod( String int
 	 */
 	@Override
 	public void enterFormalParameterDecls(JavaParser.FormalParameterDeclsContext ctx) {
 		if(methodfound) {
 			variabletypestack.push(ctx.type().getText());
 			methodID = methodID + ctx.type().getText() + " ";
 		}
 	}
 	
 	/**
 	 * Called when exiting a primitive type.
 	 * Adds the type to the stack and indicates that a type was found.
 	 */
 	@Override
 	public void exitPrimitiveType(JavaParser.PrimitiveTypeContext ctx) {
 		variabletypestack.push(ctx.getText());
 		typefound = true;
 	}
 	
 	/**
 	 * Called when exiting a class or interface type.
 	 * Adds the type to the stack and indicates that a type was found.
 	 */
 	@Override
 	public void exitClassOrInterfaceType(JavaParser.ClassOrInterfaceTypeContext ctx) {
 		variabletypestack.push(ctx.getText());
 		typefound = true;
 	}
 	
 	/**
 	 * Called when exiting a variable ID.
 	 * Adds the last found variable type to variabletype list and the id to the variableID list.
 	 */
 	@Override
 	public void exitVariableDeclaratorId(JavaParser.VariableDeclaratorIdContext ctx) {
 		variabletype.add(variabletypestack.peek());
 		variableID.add(ctx.getText());
 	}
 	
 	/**
 	 * Called when entering the any primary ID.
 	 * If a type was found map it to the variables HashMap and indicate that the end of the variable was found.
 	 */
 	@Override
 	public void enterPrimaryIdentifier(JavaParser.PrimaryIdentifierContext ctx) {
 		if(typefound) {
 			mapVariable();
 			typefound = false;
 		}
 	}
 	
 	/**
 	 * Maps the current method and its modifiers.
 	 */
 	private void mapMethod() {
 		ArrayList<String> modifiers = new ArrayList<String>();
 		while(modifierstack.size() != 0) {
 			modifiers.add(modifierstack.pop());
 		}
 		methods.put(methodID, modifiers);
 	}
 	
 	/**
 	 * Maps all variables found to the current method.
 	 */
 	private void mapVariable() {
 		HashMap<String, String> map;
 		if(variables.get(methodID) == null) {
 			map = new HashMap<String, String>();
 		} else {
 			map = variables.get(methodID);
 		}
 		for(int i = 0; i < variableID.size(); i++) {
 			map.put(variableID.get(i), variabletype.get(i));
 		}
 		variableID.clear();
 		variabletype.clear();
 		variables.put(methodID,map);
 	}
 }
