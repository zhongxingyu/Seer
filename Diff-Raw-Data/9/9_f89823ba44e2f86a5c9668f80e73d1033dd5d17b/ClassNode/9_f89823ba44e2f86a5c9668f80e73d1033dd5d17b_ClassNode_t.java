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
 package nl.han.ica.ap.purify.language.java.callgraph;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Node containing all data found in a class.
  * 
  * @author Tim
  */
 public class ClassNode {
 	/** classID is the name of this class. */
 	private String classID;
 	
 	/** methods is an ArrayList containing all methods that belong to this class. */
 	private ArrayList<MethodNode> methods;
 	
 	/** globalvariables is a HashMap containing all global variables of this class. */
 	
 	/**
 	 * Creates a ClassNode with the specified name;
 	 * @param classID The name of the class.
 	 */
 	public ClassNode(String classID) {
 		this.classID = classID;
 		methods = new ArrayList<MethodNode>();
 	}
 	
 	/**
 	 * @return Returns the name of this class.
 	 */
 	public String getClassID() {
 		return classID;
 	}
 	 
 	/**
 	 * @param method The name of the method you want to find.
 	 * @return MethodNode m if method was found, otherwise returns null.
 	 */
 	public MethodNode getMethod(String method) {
 		for(MethodNode m : methods) {
 			if(m.getMethodID().equals(method)) {
 				return m;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * @return Returns all methods of this class.
 	 */
 	public ArrayList<MethodNode> getAllMethods() {
 		return methods;
 	}
 	
 	/**
 	 * Adds a new MethodNode to this class.
 	 * 
 	 * @param methodID The name of the method.
 	 * @param modifiers The list with modifiers of the method.
 	 */
 	public void addMethodNode(String methodID, ArrayList<String> modifiers) {
 		methods.add(new MethodNode(classID, methodID, modifiers));
 	}
 	
 	/**
 	 * Map all found variables to the proper scope.
 	 * 
 	 * scope == this (global variables).
 	 * scope == methodname (local variables).
 	 * @param variables HashMap containing all variables for this class.
 	 */
 	public void mapVariables(HashMap<String, HashMap<String, String>> variables) {
 		for(String scope : variables.keySet()) {
 			
 				MethodNode mn = getMethod(scope);
 				mn.setVariables(variables.get(scope));
 		}
 	}
 	
 	/**
 	 * @param scope The scope of the variable ('methodname' for local and 'this' for global).
 	 * @param variableID The name of the variable you want the type of.
 	 * @return Returns the type of the specified variable as a String.
 	 */
 	public String getVariableType(String scope, String variableID) { 
 		for(MethodNode mn : methods) {
 			if(mn.getMethodID().equals(scope)) {
 				String temp = mn.getLocalVariable(variableID);
 				if(temp != null) {
 					return temp;
 				}
 			}
 		}
		
		for(MethodNode mn : methods) {
			if(mn.getMethodID().equals("this")) {
				String temp = mn.getLocalVariable(variableID);
				if(temp != null) {
					return temp;
				}
			}
		}
 		return null;
 	}
 	
 	/**
 	 * Gets variables from MethodNode 'this' if it exists.
 	 * 
 	 * @return HashMap<String,String> with variables of MethodNode 'this'.
 	 */
 	public HashMap<String, String> getGlobalVariables() {
 		MethodNode m = getMethod("this");
 		if(m != null) {
 			return m.getVariables();
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the return type of the specified method.
 	 * 
 	 * @param currentCall the name of the method.
 	 * @return Returns the return type of the specified method.
 	 */
 	public String getMethodReturnType(String currentCall) {
 		MethodNode m = getMethod(currentCall);
 		if(m != null) {
 			return m.getReturnType();
 		}
 		return null;
 	}
 }
