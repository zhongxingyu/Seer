 package ca.uwaterloo.joos.checker;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Stack;
 
 import ca.uwaterloo.joos.ast.ASTNode;
 import ca.uwaterloo.joos.ast.Modifiers;
 import ca.uwaterloo.joos.ast.body.InterfaceBody;
 import ca.uwaterloo.joos.ast.decl.ClassDeclaration;
 import ca.uwaterloo.joos.ast.decl.InterfaceDeclaration;
 import ca.uwaterloo.joos.ast.decl.MethodDeclaration;
 import ca.uwaterloo.joos.ast.decl.TypeDeclaration;
 import ca.uwaterloo.joos.symboltable.SemanticsVisitor;
 import ca.uwaterloo.joos.symboltable.SymbolTable;
 import ca.uwaterloo.joos.symboltable.TableEntry;
 import ca.uwaterloo.joos.symboltable.TypeScope;
 
 public class HierarchyChecker extends SemanticsVisitor {
 	private TypeScope currentScope;
 	private TypeScope currentSuperScope;
 	private Map<String, TypeScope> interfaceScopes;
 	static Set<TypeDeclaration> checkedClass = new HashSet<TypeDeclaration>();
 	private Stack<TypeScope> hierachyStack = new Stack<TypeScope>();
 	private static Map<TypeDeclaration, Stack<TypeScope>> classHierachyChain = new HashMap<TypeDeclaration, Stack<TypeScope>>();
 
 	public static Map<TypeDeclaration, Stack<TypeScope>> getClassHierachyChain() {
 		return HierarchyChecker.classHierachyChain;
 	}
 
 	public HierarchyChecker(SymbolTable table) {
 		super(table);
 	}
 
 	public boolean visit(ASTNode node) throws Exception {
 
 		if (node instanceof TypeDeclaration) {
 			TypeDeclaration typeDeclNode = (TypeDeclaration) node;
 			this.visitClassDecl(typeDeclNode);
 		}
 		return true;
 	}
 
 	protected void visitClassDecl(TypeDeclaration node) throws Exception {
 		currentScope = (TypeScope) this.getCurrentScope();
 		currentScope.getVisibleSymbols();
 		if (currentScope instanceof TypeScope) {
 			currentSuperScope = ((TypeScope) currentScope).getSuperScope();
 			interfaceScopes = ((TypeScope) currentScope).getInterfaceScopes();
 			Collection<TypeScope> interfaces = interfaceScopes.values();
 			for (TypeScope INTERFACE : interfaces) {
 				if (!(INTERFACE.getReferenceNode() instanceof InterfaceDeclaration)) {
 
 					if (INTERFACE.getName().equals("java.lang.Object") && (currentScope.getReferenceNode() instanceof InterfaceDeclaration)) {
 
 					} else {
 						throw new Exception("can only implement interfaces");
 					}
 				}
 
 			}
 			if (currentSuperScope != null) {
 				if (((TypeDeclaration) currentSuperScope.getReferenceNode()).getModifiers() != null) {
 					if (((TypeDeclaration) currentSuperScope.getReferenceNode()).getModifiers().getModifiers().contains(Modifiers.Modifier.FINAL)) {
 						throw new Exception("can not extend final");
 					}
 				}
 
 				if (currentScope.getReferenceNode() instanceof InterfaceDeclaration) {
 
 					if (currentSuperScope.getReferenceNode() instanceof ClassDeclaration) {
 						if (!currentSuperScope.getName().equals("java.lang.Object")) {
 							throw new Exception("an interface can not extend a class");
 						}
 					}
 				}
 				if (currentScope.getReferenceNode() instanceof ClassDeclaration) {
 					if (currentSuperScope.getReferenceNode() instanceof InterfaceDeclaration) {
 						throw new Exception("a class can not extend an interface");
 					}
 				}
 
 			}
 
 			checkSuperCycle();
 			checkOverRide(node);
 			checkAbstractClass();
 
 		}
 
 	}
 
 	private void checkOverRide(TypeDeclaration node) throws Exception {
 		appendStack(currentScope);
		Stack<TypeScope> hierachyStack2 = new Stack<TypeScope>();
		hierachyStack2.addAll(hierachyStack);
 		if (!classHierachyChain.containsKey(node)) {
 			classHierachyChain.put(node, hierachyStack);
 		}
 
 		while (!hierachyStack2.empty() && !checkedClass.contains(node)) {
 			TypeScope currentTopScope = hierachyStack2.pop();
 			TypeScope currentSuperScope = currentTopScope.getSuperScope();
 
 			Map<String, TypeScope> currentInterfaceScopes = currentTopScope.getInterfaceScopes();
 			ArrayList<TypeScope> currentParentScopes = new ArrayList<TypeScope>(currentInterfaceScopes.values());
 			if (currentSuperScope != null) {
 				currentParentScopes.add(currentSuperScope);
 			}
 			addMethods(currentTopScope, currentParentScopes);
 		}
 
 		checkedClass.add(node);
 
 	}
 
 	private void appendStack(TypeScope currentAppendScope) throws Exception {
 		TypeScope currentSuperScope = currentAppendScope.getSuperScope();
 		if (!hierachyStack.contains(currentScope)) {
 			hierachyStack.add(currentScope);
 		}
 
 		Map<String, TypeScope> currentInterfaceScopes = currentAppendScope.getInterfaceScopes();
 
 		ArrayList<TypeScope> currentParentScopes = new ArrayList<TypeScope>();
 		for (TypeScope INTERFACE : currentInterfaceScopes.values()) {
 			if (!currentParentScopes.contains(INTERFACE)) {
 				currentParentScopes.add(INTERFACE);
 			}
 		}
 		if (currentSuperScope != null) {
 			currentParentScopes.add(currentSuperScope);
 		}
 
 		for (TypeScope currentParentScope : currentParentScopes) {
 
 			if (!hierachyStack.contains(currentParentScope) | currentParentScope.getName().equals("java.lang.Object")) {
 
 				hierachyStack.add(currentParentScope);
 				appendStack(currentParentScope);
 			}
 
 		}
 
 	}
 
 	private void checkAbstractClass() throws Exception {
 		TypeDeclaration currentNode = (TypeDeclaration) currentScope.getReferenceNode();
 		Map<String, TableEntry> curerentVisibleMethods = currentScope.getVisibleSymbols();
 		ArrayList<TableEntry> visibleMethods = new ArrayList<TableEntry>(curerentVisibleMethods.values());
 		if (!currentNode.getModifiers().getModifiers().contains(Modifiers.Modifier.ABSTRACT) && currentNode instanceof ClassDeclaration) {
 			for (TableEntry visibleMethod : visibleMethods) {
 				if (visibleMethod.getNode() instanceof MethodDeclaration) {
 					MethodDeclaration currentVisibleNode = (MethodDeclaration) visibleMethod.getNode();
 					if (currentVisibleNode.getModifiers().getModifiers().contains(Modifiers.Modifier.ABSTRACT)) {
 						throw new Exception(currentNode.fullyQualifiedName + "should be abstract");
 					}
 					if (currentVisibleNode.getParent() instanceof InterfaceBody) {
 						throw new Exception(currentNode.fullyQualifiedName + "should be abstract");
 					}
 				}
 			}
 		}
 
 	}
 
 	private void checkSuperCycle() throws Exception {
 
 		Stack<TypeScope> extendCycle = new Stack<TypeScope>();
 		extendCycle.add(currentScope);
 		TypeScope currentClass = currentScope;
 
 		while (getSuperScope(currentClass) != null) {
 
 			if (!extendCycle.contains(getSuperScope(currentClass))) {
 				extendCycle.add(getSuperScope(currentClass));
 
 			} else {
 				throw new Exception("there is a cycle in extend");
 			}
 
 			currentClass = getSuperScope(currentClass);
 		}
 	}
 
 	private TypeScope getSuperScope(TypeScope currentClass) {
 		if (currentClass.getReferenceNode() instanceof InterfaceDeclaration) {
 			ArrayList<TypeScope> interfaces = new ArrayList<TypeScope>(currentClass.getInterfaceScopes().values());
 			return interfaces.get(0);
 		} else {
 			return currentClass.getSuperScope();
 		}
 
 	}
 
 	private void addMethods(TypeScope currentScope, ArrayList<TypeScope> parentScopes) throws Exception {
 
 		Map<String, ASTNode> parentMethods = new HashMap<String, ASTNode>();
 		for (TypeScope parentScope : parentScopes) {
 
 			// get all the full methods name and parameter in currentScope,the
 			// methods keep updating in the for loop
 			Set<String> currentMethods = currentScope.getVisibleSymbols().keySet();
 			// get all the methods signatures in currentScope
 			Set<String> simpleCurrentMethods = new HashSet<String>();
 			for (String currentMethod : currentMethods) {
 				simpleCurrentMethods.add(getSimpleSignature(currentMethod));
 			}
 
 			if (parentScope != null) {
 
 				// Iterate the methods in parentClass
 				Iterator<Entry<String, TableEntry>> parentMethodIterator = parentScope.getVisibleSymbols().entrySet().iterator();
 
 				while (parentMethodIterator.hasNext()) {
 
 					Map.Entry<String, TableEntry> currentParentMethod = parentMethodIterator.next();
 
 					if (currentParentMethod.getValue().getNode() instanceof MethodDeclaration) {
 						MethodDeclaration parentMethodNode = (MethodDeclaration) currentParentMethod.getValue().getNode();
 						String currentParentMethodSig = currentParentMethod.getKey();
 						String currentSimpleParentMethodSig = getSimpleSignature(currentParentMethodSig);
 
 						// override between different parent classes
 						if (parentMethods.keySet().contains(currentSimpleParentMethodSig)) {
 
 							MethodDeclaration existMethodNode = (MethodDeclaration) parentMethods.get(currentSimpleParentMethodSig);
 							MethodDeclaration newMethodNode = (MethodDeclaration) currentParentMethod.getValue().getNode();
 
 							if ((existMethodNode.getType() != null) && (newMethodNode.getType() != null)) {
 								if (!existMethodNode.getType().getIdentifier().equals(newMethodNode.getType().getIdentifier())) {
 									throw new Exception("same method different return type");
 								}
 							} else {
 								if ((existMethodNode.getType() == null) != (newMethodNode.getType() == null)) {
 									throw new Exception("same method different return type");
 								}
 
 							}
 
 							boolean containProtected = existMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.PROTECTED);
 							boolean containPublic = newMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.PUBLIC);
 
 							if (containProtected == containPublic) {
 								throw new Exception("protected mothed can not override public");
 							}
 						} else {
 
 							parentMethods.put(currentSimpleParentMethodSig, currentParentMethod.getValue().getNode());
 						}
 
 						// do not check already inherited method
 						if (!currentMethods.contains(currentParentMethodSig)) {
 
 							// the method in the current class overwrite the
 							// method in parent class
 							if (simpleCurrentMethods.contains(currentSimpleParentMethodSig)) {
 
 								MethodDeclaration currentMethodNode = null;
 								String CurrentMethodkey = null;
 
 								for (String key : currentScope.getVisibleSymbols().keySet()) {
 									if (key.contains(currentSimpleParentMethodSig)) {
 										CurrentMethodkey = key;
 										currentMethodNode = (MethodDeclaration) currentScope.getVisibleSymbols().get(key).getNode();
 									}
 								}
 								currentMethodNode.setOverideMethod(parentMethodNode);
 
 								if (currentMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.PROTECTED)) {
 									if (parentMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.PUBLIC)) {
 										throw new Exception("protected mothed can not override public");
 									}
 								}
 
 								if (currentScope.getMethod(currentMethodNode) == null) {
 									if (parentMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.PROTECTED)) {
 										if (currentMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.PUBLIC)) {
 											throw new Exception("protected mothed can not override public");
 										}
 									}
 								}
 
 								if (parentMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.FINAL)) {
 									throw new Exception("can not override final method");
 								}
 								if (parentMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.STATIC)) {
 									throw new Exception("can not override static method");
 								}
 								if (currentMethodNode.getModifiers().getModifiers().contains(Modifiers.Modifier.STATIC)) {
 									throw new Exception("static can not override instance method");
 								}
 								if ((currentMethodNode.getType() != null) && (parentMethodNode.getType() != null)) {
 									if (!currentMethodNode.getType().getIdentifier().equals(parentMethodNode.getType().getIdentifier())) {
 										throw new Exception("same method different return type");
 									}
 
 								} else if ((currentMethodNode.getType() == null) != (parentMethodNode.getType() == null)) {
 									throw new Exception("same method different return type");
 
 								}
 
 								if ((currentMethodNode.getParent().getParent() instanceof InterfaceDeclaration && (!parentMethodNode.getParent().getParent().getIdentifier().equals("Object"))) | currentMethodNode.getParent().getParent().getIdentifier().equals("Object")) {
 
 									currentMethods.remove(CurrentMethodkey);
 									currentScope.addVisibleSymbols(currentParentMethod.getKey(), currentParentMethod.getValue());
 								}
 							} else {
 								currentScope.addVisibleSymbols(currentParentMethod.getKey(), currentParentMethod.getValue());
 							}
 						}
 					}
 				}
 			}
 
 		}
 	}
 
 	private String getSimpleSignature(String MethodSig) {
 		if (MethodSig.indexOf("(") > 0) {
 			String methodString = MethodSig.substring(0, MethodSig.indexOf("("));
 			return MethodSig.substring(methodString.lastIndexOf("."));
 		} else {
 			return MethodSig.substring(MethodSig.lastIndexOf("."));
 		}
 
 	}
 }
