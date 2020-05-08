 package xtc.translator.translation;
 
 import java.lang.Character;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 import xtc.translator.representation.Argument;
 import xtc.translator.representation.CallExpressionPiece;
 import xtc.translator.representation.ClassVisitor;
 import xtc.translator.representation.CompilationUnit;
 import xtc.translator.representation.FieldVisitor;
 import xtc.translator.representation.Method;
 import xtc.translator.representation.MethodMaps;
 import xtc.translator.representation.MethodVisitor;
 import xtc.translator.representation.SourceObject;
 import xtc.translator.representation.VariableVisitor;
 
 import xtc.lang.JavaFiveParser;
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 
 /**
  * Top level class encapsulating all underlying data extracted from relavent
  * java source. Should act as a starting point for subsequent c++ translation.
  */
 public class Collector extends Visitor {
 
 	public ArrayList<File> packDirs;
 	public ArrayList<String> imports;
 	public ArrayList<String> packages;
 	public List<ClassVisitor> classes;
 	public ClassVisitor mainClass;
 
 	/**
 	 * a list of classes that are used this should come from
 	 * visitQualifiedIdentifiers in ImplementationVisitor
 	 */
 	public ArrayList<String> usedClasses;
 	public HashMap<String, String> usedClassesDict;
 	public ArrayList<ClassVisitor> sortedClasses;
 
 	public Collector(List<CompilationUnit> compilationUnits) {
 
 		this.imports = new ArrayList<String>();
 		this.classes = new ArrayList<ClassVisitor>();
 
 		for (CompilationUnit compilationUnit : compilationUnits) {
 			this.classes.add(compilationUnit.getClassVisitor());
 		}
 
 		this.packages = new ArrayList<String>();
 		this.mainClass = null;
 		this.packDirs = new ArrayList<File>();
 		this.sortedClasses = new ArrayList<ClassVisitor>();
 
 	}
 
 	/**
 	 * Main method to begin collection
 	 * 
 	 */
 	public void collect() throws IOException, ParseException {
				
 		// get overloaded methods
 		this.generateMethodOverload();
 		
 		// assign super classes to classVisitors
 		this.assignSuperClass();
 
 		// Use assigned super classes to create implementation
 		// map of all methods in class hierarchy (vtable precursor)
 		this.createImplementationMap();
 
 		// sort the list of classes for printing
 		//this.sort();
 		
 		// Create variable map for each method
 		this.createVariableMaps();
 		
 		// Process method calls for proper overloading
 		this.processCallExpression();
 	}
 	
 	private void assignSuperClass() {
 		for (ClassVisitor cv : classes) {
 			if (cv.getExtension() == null || cv.getExtension().isEmpty()) {
 				cv.setSuperClass(new SourceObject());
 			} else {
 				boolean found = false;
 				for (ClassVisitor cv2 : classes) {
 					if (cv2.getIdentifier().equals(cv.getExtension())) {
 						cv.setSuperClass(cv2.copy());
 						found = true;
 						break;
 					}
 				}
 
 				if (found == false) {
 					cv.setSuperClass(new SourceObject());
 				}
 			}
 		}
 	}
 
 	private void createImplementationMap() {
 		for (ClassVisitor classVisitor : classes) {
 			createImplementationMap(classVisitor, classVisitor);			
 		}
 	}
 
 	/**
 	 * Recursive Helper method, recurses through class hierarchy, from Object
 	 * downward, and updates the implementation table of the original
 	 * classVisitor with the latest overridden implementation of a method with a
 	 * particular identifier.
 	 * 
 	 * @param currentClass
 	 * @param original
 	 */
 	private void createImplementationMap(ClassVisitor currentClass,
 			ClassVisitor original) {
 		if (currentClass == null) {
 			return;
 		} else {
 			createImplementationMap(currentClass.getSuperClass(), original);
 
 			for (MethodVisitor m : currentClass.getMethodList()) {
 				// If key is already in map, it means newer method is overriding
 				// older one
 				if (original.getImplementationMap().containsKey(
 						m.getIdentifier())) {
 					m.setOverride(true);
 				}
 				original.getImplementationMap().put(m.getIdentifier(),
 						currentClass.getFullIdentifier());
 			}
 		}
 	}
 
 	/**
 	 * Driver to sort Collector's classes list
 	 */
 	private void sort() {
 		// only sort if it hasn't already been sorted
 		if (sortedClasses.size() == 0) {
 			HashMap<String, Integer> visited = new HashMap<String, Integer>();
 			for (ClassVisitor currentClass : classes) {
 				if (currentClass.getIdentifier() != "Object") {
 					if (!visited.containsKey(currentClass.getIdentifier())) {
 						sort(currentClass, visited);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Recursive helper method for sorting classes in heirarchical order
 	 */
 	private void sort(ClassVisitor currentClass,
 			HashMap<String, Integer> visited) {
 		if (currentClass.getIdentifier() != "Object") {
 			if (currentClass.getSuperClass() != null) {
 				// have not encountered the superclass, so sort it!
 				if (!visited.containsKey(currentClass.getSuperClass()
 						.getIdentifier())) {
 					sort(currentClass.getSuperClass(), visited);
 				}
 				// put yourself after the your parent
 				else {
 					int index = visited.get(currentClass.getSuperClass()
 							.getIdentifier()) + 1;
 					sortedClasses.add(index, currentClass);
 					visited.put(currentClass.getIdentifier(), index);
 				}
 			}
 			// make sure that the class has not already been visited
 			if (!visited.containsKey(currentClass.getIdentifier())) {
 				// store the location of where you put the class
 				// note that haven't actually put class in sortedClasses yet, so
 				// don't have to
 				// decrement sortedClasses.size()
 				visited.put(currentClass.getIdentifier(), sortedClasses.size());
 				sortedClasses.add(currentClass);
 			}
 		}
 	}
 	
 	private void generateMethodOverload() {
 		
 		for (ClassVisitor classVisitor : classes) {
 
 			for (MethodVisitor m : classVisitor.getMethodList()) {
 								
 				Method method = new Method(m.getIdentifier(), m.getReturnType());
 				
 				// set if method is static
 				method.isStatic = m.isStatic();
 				
 				List<Argument> paramTypes = new ArrayList<Argument>();
 				
 				for (Map<String, String> param : m.getParameters()) {
 					paramTypes.add(new Argument(param.get("type"), null));
 				}
 				
 				method.getArguments().setArguments(paramTypes);
 								
 				method.generateOverloadedIdentifier();
 				
 				// if method is already there, add it to the list
 				if (classVisitor.getOverloadMap().containsKey(method.getIdentifier())){
 					classVisitor.getOverloadMap().get(method.getIdentifier()).add(method);
 				} else {
 					List<Method> methodList = new ArrayList<Method>();
 					methodList.add(method);
 					classVisitor.getOverloadMap().put(method.getIdentifier(), methodList);
 				}
 				
 				if (! m.getIdentifier().equals("main"))
 					m.setIdentifier(method.getOverloadedIdentifier());
 			}
 			
 			MethodMaps.addMethodMapForClass(classVisitor.getIdentifier(), classVisitor.getOverloadMap());
 		}
 	}
 	
 	private void createVariableMaps() {
 		
 		for (ClassVisitor classVisitor : classes) {
 			
 			for (MethodVisitor m : classVisitor.getMethodList()) {
 				
 				VariableVisitor vv = new VariableVisitor();
 				
 				// Put field variables into it
 				for (FieldVisitor fv : classVisitor.getFieldList()) {
 					vv.getVariableMap().put(fv.variableName, fv.variableType);
 				}
 				
 				// get the rest of the variables in method scope
 				vv.dispatch(m.getBaseNode());
 				
 				// set methods variable map
 				m.setVariableMap(vv.getVariableMap());
 			}			
 		}
 	}
 
 	private void processCallExpression() {
 		
 		for (ClassVisitor classVisitor : classes) {
 			
 			for (MethodVisitor m : classVisitor.getMethodList()) {
 				
 				for (CallExpressionPiece c : m.getImplementationVisitor().getCallExpressions()) {
 					c.setMethodMap(classVisitor.getOverloadMap());
 					c.setVariableMap(m.getVariableMap());
 					c.processNode();
 				}	
 			}	
 		}
 	}
 	
 	public ArrayList<ClassVisitor> getSortedClasses() {
 		sort();
 		return sortedClasses;
 	}
 }
