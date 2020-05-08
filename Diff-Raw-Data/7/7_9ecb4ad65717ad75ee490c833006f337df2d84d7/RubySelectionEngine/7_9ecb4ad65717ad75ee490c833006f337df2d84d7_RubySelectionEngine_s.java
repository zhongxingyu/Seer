 package org.eclipse.dltk.ruby.internal.core.codeassist;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.declarations.Argument;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.Assignment;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.codeassist.IAssistParser;
 import org.eclipse.dltk.codeassist.ScriptSelectionEngine;
 import org.eclipse.dltk.compiler.env.ISourceModule;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKModelUtil;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.search.SearchMatch;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.TypeNameMatch;
 import org.eclipse.dltk.core.search.TypeNameMatchRequestor;
 import org.eclipse.dltk.evaluation.types.IClassType;
 import org.eclipse.dltk.evaluation.types.SimpleType;
 import org.eclipse.dltk.internal.compiler.lookup.LookupEnvironment;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.ruby.ast.ColonExpression;
 import org.eclipse.dltk.ruby.core.model.FakeField;
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 import org.eclipse.dltk.ruby.internal.parsers.jruby.ASTUtils;
 import org.eclipse.dltk.ruby.typeinference.RubyClassType;
 import org.eclipse.dltk.ruby.typeinference.RubyEvaluatorFactory;
 import org.eclipse.dltk.ruby.typeinference.RubyMetaClassType;
 import org.eclipse.dltk.ruby.typeinference.RubyModelUtils;
 import org.eclipse.dltk.ruby.typeinference.RubyTypeInferencingUtils;
 import org.eclipse.dltk.ruby.typeinference.goals.ColonExpressionGoal;
 import org.eclipse.dltk.ti.BasicContext;
 import org.eclipse.dltk.ti.TypeInferencer;
 import org.eclipse.dltk.ti.goals.ExpressionTypeGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 
 
 public class RubySelectionEngine extends ScriptSelectionEngine {
 	public static boolean DEBUG = DLTKCore.DEBUG_SELECTION;
 
 	protected int actualSelectionStart;
 
 	protected int actualSelectionEnd;
 
 	private List selectionElements = new ArrayList();
 		
 	private RubySelectionParser parser = new RubySelectionParser();
 	
 	private ISourceModule sourceModule;
 	
 	private ASTNode[] wayToNode;
 
 	private TypeInferencer inferencer;
 
 	private TypeDeclaration getEnclosingType(ASTNode node) {
 		return ASTUtils.getEnclosingType(wayToNode, node, true);
 	}
 
 	private CallExpression getEnclosingCallNode(ASTNode node) {
 		return ASTUtils.getEnclosingCallNode(wayToNode, node, true);
 	}
 	
 	private TypeDeclaration getStrictlyEnclosingType(ASTNode node) {
 		return ASTUtils.getEnclosingType(wayToNode, node, false);
 	}
 	
 	private String getTypeFQN (TypeDeclaration decl) {
 		String fqn = "";
 		while (decl != null) {
 			String curName = decl.getName();
 			if (fqn.length() > 0)
 				fqn = "::" + fqn;
 			fqn = curName + fqn;
 			if (curName.startsWith("::")) {
 				break;
 			}
 			decl = this.getStrictlyEnclosingType(decl);
 		} 
 		if (!fqn.startsWith("::") && fqn.length() > 0)
 			fqn = "::" + fqn;
 		return fqn;
 	}
 	
 	
 
 	public RubySelectionEngine(ISearchableEnvironment environment, Map options,
 			IDLTKLanguageToolkit toolkit) {
 		super(options);
 		this.nameEnvironment = environment;
 		this.lookupEnvironment = new LookupEnvironment(this, nameEnvironment);
 		inferencer = new TypeInferencer(new RubyEvaluatorFactory());
 	}
 
 	public IAssistParser getParser() {
 		return null;
 	}
 	
 	
 	
 	public IModelElement[] select(ISourceModule sourceUnit,
 			int selectionSourceStart, int selectionSourceEnd) {
 		sourceModule = (ISourceModule) sourceUnit
 				.getModelElement();
 		String source = sourceUnit.getSourceContents();
 		if (DEBUG) {
 			System.out.print("SELECTION IN "); //$NON-NLS-1$
 			System.out.print(sourceUnit.getFileName());
 			System.out.print(" FROM "); //$NON-NLS-1$
 			System.out.print(selectionSourceStart);
 			System.out.print(" TO "); //$NON-NLS-1$
 			System.out.println(selectionSourceEnd);
 			System.out.println("SELECTION - Source :"); //$NON-NLS-1$
 			System.out.println(source);
 		}
 		if (selectionSourceStart > selectionSourceEnd) {
 			int x = selectionSourceEnd;
 			selectionSourceEnd = selectionSourceStart;
 			selectionSourceStart = x;
 		}
 		if (!checkSelection(source, selectionSourceStart, selectionSourceEnd)) {
 			return new IModelElement[0];
 		}		
 		actualSelectionEnd--; //inclusion fix
 		if (DEBUG) {
 			System.out.print("SELECTION - Checked : \""); //$NON-NLS-1$
 			System.out.print(source.substring(actualSelectionStart,
 					actualSelectionEnd + 1));
 			System.out.println('"');
 		}
 		
 		try {
 			ModuleDeclaration parsedUnit =	this.parser.parse(sourceUnit);
 			
 			if (parsedUnit != null) {
 				if(DEBUG) {
 					System.out.println("SELECTION - AST :"); //$NON-NLS-1$
 					System.out.println(parsedUnit.toString());
 				}
 												
 				ASTNode node = ASTUtils.findMinimalNode(parsedUnit, actualSelectionStart, actualSelectionEnd);				
 				
 				if (node == null)
 					return new IModelElement[0];
 				
 				this.wayToNode = ASTUtils.restoreWayToNode(parsedUnit, node);
 				
 				org.eclipse.dltk.core.ISourceModule modelModule = (org.eclipse.dltk.core.ISourceModule) sourceModule
 						.getModelElement();
 				if (node instanceof TypeDeclaration) {
 					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
 					selectionOnTypeDeclaration (parsedUnit, typeDeclaration);
 				} else if (node instanceof MethodDeclaration) {
 					MethodDeclaration methodDeclaration = (MethodDeclaration) node;
 					selectionOnMethodDeclaration(parsedUnit, methodDeclaration);
 				} else if (node instanceof ConstantReference) {
 					ConstantReference reference = (ConstantReference) node;
 					selectTypes (modelModule, parsedUnit, reference); //FIXME: add support of non-Type constants
 				} else if (node instanceof ColonExpression) {
 					ColonExpression colonExpression = (ColonExpression) node;					
 					selectTypes (modelModule, parsedUnit, colonExpression);
 				} else if (node instanceof VariableReference) {
 					selectionOnVariable(modelModule, parsedUnit, (VariableReference)node);
 				} else {
 					CallExpression parentCall = this.getEnclosingCallNode(node);						
 					if (parentCall != null) {
 						selectOnMethod(modelModule, parsedUnit, parentCall);
 					} else { // parentCall == null
 					}					
 				}
 			}
 		} catch (IndexOutOfBoundsException e) { // work-around internal failure 		
 			if(DEBUG) {
 				System.out.println("Exception caught by RubySelectionEngine:"); //$NON-NLS-1$
 				e.printStackTrace(System.out);
 			}
 		}  		
 
 		return (IModelElement[]) selectionElements
 				.toArray(new IModelElement[selectionElements.size()]);
 	}
 			
 	
		
	
 
 	/**
 	 * Checks, whether giver selection is correct selection, or can be expanded 
 	 * to correct selection region. As result will set this.actualSelection(Start|End)
 	 * properly. In case of incorrect selection, will return false.
 	 * @param source
 	 * @param start
 	 * @param end
 	 * @return
 	 */
 	protected boolean checkSelection(String source, int start, int end) {
 
 		if (start - 1 == end) {
 			ISourceRange range, range2;			
 			range = RubySyntaxUtils.getEnclosingName(source, start);
 			if (range != null) {
 				this.actualSelectionStart = range.getOffset();
 				this.actualSelectionEnd = this.actualSelectionStart
 						+ range.getLength();
 				//return true;
 			}
 			range2 = RubySyntaxUtils.insideMethodOperator(source, start);
 			if (range != null && (range2 == null || range2.getLength() < range.getLength()))
 				return true;
 			if (range2 != null) {
 				this.actualSelectionStart = range2.getOffset();
 				this.actualSelectionEnd = this.actualSelectionStart
 						+ range2.getLength();
 				return true;
 			}
 		} else {
 			if (start >= 0 && end < source.length()) {
 				String str = source.substring(start, end + 1);
 				if (RubySyntaxUtils.isRubyName(str)) {
 					this.actualSelectionStart = start;
 					this.actualSelectionEnd = end + 1;
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 		
 	private void selectTypes(org.eclipse.dltk.core.ISourceModule modelModule, ModuleDeclaration parsedUnit,
 			ASTNode node) {
 		boolean foundSomething = false;
 		String qualifiedName;
 		if (node instanceof ColonExpression) {
 			ColonExpressionGoal goal = new ColonExpressionGoal(new BasicContext(modelModule, 
 					parsedUnit), (ColonExpression)node);
 			IEvaluatedType type = inferencer.evaluateType(goal, null);
 			if (type != null) {
 				RubyClassType classType = null;
 				if (type instanceof RubyClassType) {
 					classType = (RubyClassType) type;
 				} else if (type instanceof RubyMetaClassType) {
 					classType = (RubyClassType) ((RubyMetaClassType)type).getInstanceType();
 				}
 				if (classType != null) {
 					RubyClassType type2 = RubyTypeInferencingUtils.resolveMethods(modelModule.getScriptProject(), classType);
 					IType[] typeDeclarations = type2.getTypeDeclarations();
 					if (typeDeclarations != null) {
 						for (int i = 0; i < typeDeclarations.length; i++) {
 							selectionElements.add(typeDeclarations[i]);
 							foundSomething = true;
 						}
 					}
 				}
 			}
 		} else if (node instanceof ConstantReference) {
 			ConstantReference constantReference = (ConstantReference) node;
 			qualifiedName = constantReference.getName();
 			IDLTKProject project = modelModule.getScriptProject();		
 			IType[] types = null;
 			if (qualifiedName.startsWith("::")) {
 				types = DLTKModelUtil.getAllTypes(project, qualifiedName, "::");
 			} else {
 				String scopeFQN = getTypeFQN(getStrictlyEnclosingType(node));
 				types = DLTKModelUtil.getAllScopedTypes(project, qualifiedName, "::", scopeFQN); 
 			}
 			
 			if (types != null) {
 				for (int i = 0; i < types.length; i++) {
 					this.selectionElements.add(types[i]);
 					foundSomething = true;
 				}				
 			}
 		} 
 		
 		if (!foundSomething) {
 			TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
 
 				public void acceptTypeNameMatch(TypeNameMatch match) {
 					selectionElements.add(match.getType());
 				}
 				
 			};
 			String unqualifiedName = null;
 			if (node instanceof ColonExpression) {
 				ColonExpression expr = (ColonExpression) node;
 				unqualifiedName = expr.getName();
 			} else if (node instanceof ConstantReference) {
 				ConstantReference expr = (ConstantReference) node;
 				unqualifiedName = expr.getName();
 			}
 			if (unqualifiedName != null) {
 				DLTKModelUtil.searchTypeDeclarations(modelModule.getScriptProject(), unqualifiedName, requestor);
 			}
 		}
 	}
	

	
 	private void selectionOnVariable (org.eclipse.dltk.core.ISourceModule modelModule, 
 			ModuleDeclaration parsedUnit, VariableReference e) {
 		String name = e.getName();
 		if (name.startsWith("@")) {
 			IField[] fields = RubyModelUtils.findFields(modelModule, parsedUnit, name, e.sourceStart());
 			for (int i = 0; i < fields.length; i++) {
 				selectionElements.add(fields[i]);
 			}
 		} else { // local vars
 			ASTNode parentScope = null;
 			MethodDeclaration methodDeclaration = ASTUtils.getEnclosingMethod(wayToNode, e, false);
 			if (methodDeclaration != null) {
 				List arguments = methodDeclaration.getArguments();
 				for (Iterator iterator = arguments.iterator(); iterator.hasNext();) {
 					Argument arg = (Argument) iterator.next();
 					if (arg.getName().equals(name)) {
 						selectionElements.add(createLocalVariable(name, arg.sourceStart(), arg.sourceEnd()));
 						return;
 					}
 				}
 				parentScope = methodDeclaration; 	
 			} else
 			if (wayToNode.length >= 2) {
 				parentScope = wayToNode[wayToNode.length - 2];
 			}
 			if (parentScope != null) {
 				Assignment[] assignments = RubyTypeInferencingUtils.findLocalVariableAssignments(parentScope, e, name);
 				if (assignments.length > 0) {
 					Assignment assignment = assignments[0];
 					selectionElements.add(createLocalVariable(name, assignment.getLeft().sourceStart(), assignment.getLeft().sourceEnd()));
 				} else {
 					selectionElements.add (createLocalVariable(name, e.sourceStart(), e.sourceEnd()));
 				}				
 			}
 			
 			
 		}
 
 	}
 	
 	private IField createLocalVariable(String name, int nameStart, int nameEnd) {		
 		return new FakeField((ModelElement)sourceModule, name, nameStart, nameEnd - nameStart);
 	}
 
 	private void selectionOnTypeDeclaration(ModuleDeclaration parsedUnit,
 			TypeDeclaration typeDeclaration) {
 		String typeFQN = getTypeFQN(typeDeclaration);
 		//TODO: find all classes and methods with such name
 		IType el = (IType)DLTKModelUtil.findType(sourceModule.getModelElement(),
 				typeFQN, "::"); 		
 		Assert.isNotNull(el);
 		selectionElements.add(el);
 	}
 	
 	private void selectionOnMethodDeclaration (ModuleDeclaration parsedUnit, MethodDeclaration methodDeclaration) {		
 		TypeDeclaration typeDeclaration = this.getEnclosingType(methodDeclaration);		
 		if (typeDeclaration == null) {
 			IMethod method = ((org.eclipse.dltk.core.ISourceModule) sourceModule).getMethod(methodDeclaration.getName()); 								
 			selectionElements.add(method);
 		} else {
 			IType el = (IType)DLTKModelUtil.findType(sourceModule.getModelElement(),
 					getTypeFQN(typeDeclaration), "::");
 			Assert.isNotNull(el);
 			IMethod method = el.getMethod(methodDeclaration.getName());
 			selectionElements.add(method);
 		}
 	}
 	
 	private void selectOnMethod (org.eclipse.dltk.core.ISourceModule modelModule, ModuleDeclaration parsedUnit, CallExpression parentCall) {
 		String methodName = ((CallExpression) parentCall).getName();		
 		Statement receiver = parentCall.getReceiver();
 		
 		IMethod[] availableMethods = null;
 		
 		if (receiver == null) {
 			IClassType type = RubyTypeInferencingUtils.determineSelfClass(modelModule, parsedUnit, parentCall.sourceStart());
 			if (type instanceof RubyClassType) {
 				RubyClassType rubyClassType = RubyTypeInferencingUtils.resolveMethods(modelModule.getScriptProject(), (RubyClassType) type);
 				availableMethods = rubyClassType.getAllMethods();				
 			} else if (type instanceof RubyMetaClassType) {
 				RubyMetaClassType metaClassType = RubyTypeInferencingUtils.resolveMethods(modelModule, (RubyMetaClassType) type);				
 				availableMethods = metaClassType.getMethods();
 			}
 		} else {
 			ExpressionTypeGoal goal = new ExpressionTypeGoal(new BasicContext(modelModule, parsedUnit), receiver);
 			IEvaluatedType type = inferencer.evaluateType(goal, null);
 			if (type instanceof RubyClassType) {
 				RubyClassType rubyClassType = (RubyClassType) type;
 				rubyClassType = RubyTypeInferencingUtils.resolveMethods(modelModule.getScriptProject(), rubyClassType);
 				availableMethods = rubyClassType.getAllMethods();
 			} else if (type instanceof RubyMetaClassType) {
 				RubyMetaClassType metaType = (RubyMetaClassType) type;
 				metaType = RubyTypeInferencingUtils.resolveMethods(modelModule, metaType);
 				availableMethods = metaType.getMethods();
 			} else if (type instanceof SimpleType) {
 				SimpleType simpleType = (SimpleType) type;
 				IMethod[] meth = null;
 				switch (simpleType.getType()) {
 					case SimpleType.TYPE_NUMBER:
 						meth = RubyModelUtils.getFakeMethods((ModelElement) modelModule, "Fixnum");
 						break;
 					case SimpleType.TYPE_STRING:
 						meth = RubyModelUtils.getFakeMethods((ModelElement) modelModule, "String");
 						break;
 					case SimpleType.TYPE_ARRAY:
 						meth = RubyModelUtils.getFakeMethods((ModelElement) modelModule, "Array");
 						break;
 				}
 				availableMethods = meth;
 			}
 			
 		}
 		
 		if (availableMethods == null || availableMethods.length == 0) {
 			final Collection methods = new ArrayList();
 			SearchRequestor requestor = new SearchRequestor() {
 
 				public void acceptSearchMatch(SearchMatch match) throws CoreException {
 					IMethod method = (IMethod) match.getElement();
 					methods.add(method);
 				}
 				
 			};
 			DLTKModelUtil.searchMethodDeclarations(modelModule.getScriptProject(), methodName, requestor);
 			availableMethods = (IMethod[]) methods.toArray(new IMethod[methods.size()]);
 			if (availableMethods.length > 0)
 				System.out.println("RubySelectionEngine.selectOnMethod() used global search");
 		}
 		
 		if (availableMethods != null) {
 			int count = 0;
 			for (int i = 0; i < availableMethods.length; i++) {
 				if (availableMethods[i].getElementName().equals(methodName)) {
 					selectionElements.add(availableMethods[i]);
 					++count;
 				}
 			}
 //			if (count == 0) {
 //				for (int i = 0; i < availableMethods.length; i++) {
 //					selectionElements.add(availableMethods[i]);
 //				}
 //			}
 		}
 	
 	}
 
 }
