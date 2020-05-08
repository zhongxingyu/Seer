 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.core.codeassist;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.ASTNode;
 import org.eclipse.dltk.ast.declarations.Argument;
 import org.eclipse.dltk.ast.declarations.MethodDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.TypeDeclaration;
 import org.eclipse.dltk.ast.expressions.CallExpression;
 import org.eclipse.dltk.ast.references.ConstantReference;
 import org.eclipse.dltk.ast.references.VariableReference;
 import org.eclipse.dltk.codeassist.IAssistParser;
 import org.eclipse.dltk.codeassist.ScriptSelectionEngine;
 import org.eclipse.dltk.compiler.env.ISourceModule;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKModelUtil;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.mixin.IMixinElement;
 import org.eclipse.dltk.core.search.SearchMatch;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.TypeNameMatch;
 import org.eclipse.dltk.core.search.TypeNameMatchRequestor;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.ruby.ast.RubyAssignment;
 import org.eclipse.dltk.ruby.ast.RubyColonExpression;
 import org.eclipse.dltk.ruby.core.model.FakeField;
 import org.eclipse.dltk.ruby.core.utils.RubySyntaxUtils;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinClass;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinElementInfo;
 import org.eclipse.dltk.ruby.internal.parser.mixin.RubyMixinModel;
 import org.eclipse.dltk.ruby.internal.parsers.jruby.ASTUtils;
 import org.eclipse.dltk.ruby.typeinference.RubyClassType;
 import org.eclipse.dltk.ruby.typeinference.RubyModelUtils;
 import org.eclipse.dltk.ruby.typeinference.RubyTypeInferencingUtils;
 import org.eclipse.dltk.ruby.typeinference.evaluators.ColonExpressionEvaluator;
 import org.eclipse.dltk.ruby.typeinference.evaluators.ConstantReferenceEvaluator;
 import org.eclipse.dltk.ruby.typeinference.goals.NonTypeConstantTypeGoal;
 import org.eclipse.dltk.ti.BasicContext;
 import org.eclipse.dltk.ti.DLTKTypeInferenceEngine;
 import org.eclipse.dltk.ti.GoalState;
 import org.eclipse.dltk.ti.goals.AbstractTypeGoal;
 import org.eclipse.dltk.ti.goals.ExpressionTypeGoal;
 import org.eclipse.dltk.ti.goals.IGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 
 public class RubySelectionEngine extends ScriptSelectionEngine {
 	public static boolean DEBUG = DLTKCore.DEBUG_SELECTION;
 
 	protected int actualSelectionStart;
 
 	protected int actualSelectionEnd;
 
 	private Set selectionElements = new HashSet();
 
 	private RubySelectionParser parser = new RubySelectionParser();
 
 	private ISourceModule sourceModule;
 
 	private ASTNode[] wayToNode;
 
 	private DLTKTypeInferenceEngine inferencer;
 
 	private TypeDeclaration getEnclosingType(ASTNode node) {
 		return ASTUtils.getEnclosingType(wayToNode, node, true);
 	}
 
 	private CallExpression getEnclosingCallNode(ASTNode node) {
 		return ASTUtils.getEnclosingCallNode(wayToNode, node, true);
 	}
 
 	public RubySelectionEngine(/*
 								 * ISearchableEnvironment environment, Map
 								 * options, IDLTKLanguageToolkit toolkit
 								 */) {
 		// super();
 		// setOptions(options);
 		// this.nameEnvironment = environment;
 		// this.lookupEnvironment = new LookupEnvironment(this,
 		// nameEnvironment);
 		inferencer = new DLTKTypeInferenceEngine();
 	}
 
 	public IAssistParser getParser() {
 		return null;
 	}
 
 	public IModelElement[] select(ISourceModule sourceUnit,
 			int selectionSourceStart, int selectionSourceEnd) {
 		sourceModule = (ISourceModule) sourceUnit.getModelElement();
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
 		actualSelectionEnd--; // inclusion fix
 		if (DEBUG) {
 			System.out.print("SELECTION - Checked : \""); //$NON-NLS-1$
 			System.out.print(source.substring(actualSelectionStart,
 					actualSelectionEnd + 1));
 			System.out.println('"');
 		}
 
 		try {
 			ModuleDeclaration parsedUnit = this.parser.parse(sourceUnit);
 
 			if (parsedUnit != null) {
 				if (DEBUG) {
 					System.out.println("SELECTION - AST :"); //$NON-NLS-1$
 					System.out.println(parsedUnit.toString());
 				}
 
 				ASTNode node = ASTUtils.findMinimalNode(parsedUnit,
 						actualSelectionStart, actualSelectionEnd);
 
 				if (node == null)
 					return new IModelElement[0];
 
 				this.wayToNode = ASTUtils.restoreWayToNode(parsedUnit, node);
 
 				org.eclipse.dltk.core.ISourceModule modelModule = (org.eclipse.dltk.core.ISourceModule) sourceModule
 						.getModelElement();
 				if (node instanceof TypeDeclaration) {
 					TypeDeclaration typeDeclaration = (TypeDeclaration) node;
 					selectionOnTypeDeclaration(parsedUnit, typeDeclaration);
 				} else if (node instanceof MethodDeclaration) {
 					MethodDeclaration methodDeclaration = (MethodDeclaration) node;
 					selectionOnMethodDeclaration(parsedUnit, methodDeclaration);
 				} else if (node instanceof ConstantReference || node instanceof RubyColonExpression) {
 					selectTypes(modelModule, parsedUnit, node);
 				} else if (node instanceof VariableReference) {
 					selectionOnVariable(modelModule, parsedUnit,
 							(VariableReference) node);
 				} else {
 					CallExpression parentCall = this.getEnclosingCallNode(node);
 					if (parentCall != null) {
 						selectOnMethod(modelModule, parsedUnit, parentCall);
 					} else { // parentCall == null
 					}
 				}
 			}
 		} catch (IndexOutOfBoundsException e) { // work-around internal failure
 			if (DEBUG) {
 				System.out.println("Exception caught by RubySelectionEngine:"); //$NON-NLS-1$
 				e.printStackTrace(System.out);
 			}
 		}
 
 		List resultElements = new ArrayList ();
 		
 		for (Iterator iterator = selectionElements.iterator(); iterator.hasNext();) {
 			IModelElement element = (IModelElement) iterator.next();
			if (sourceUnit.getModelElement().getScriptProject().equals( 
				element.getScriptProject()))
 				resultElements.add(element);
 		}
 		
 		return (IModelElement[]) resultElements
 				.toArray(new IModelElement[resultElements.size()]);
 	}
 
 	private void selectOnColonExpression(ModuleDeclaration parsedUnit,
 			RubyColonExpression node,
 			org.eclipse.dltk.core.ISourceModule modelModule) {
 		BasicContext basicContext = new BasicContext(
 				(org.eclipse.dltk.core.ISourceModule) sourceModule, parsedUnit);
 		ColonExpressionEvaluator evaluator = new ColonExpressionEvaluator(
 				new ExpressionTypeGoal(basicContext, node));
 		IGoal[] init = evaluator.init();
 		if (init == null || init.length == 0) {
 			 // should never be here
 			System.err.println("Why did ColonExpressionEvaluator evaluated so fast?");
 		} else {
 			IEvaluatedType leftType = inferencer.evaluateType((AbstractTypeGoal) init[0], -1);
 			IGoal[] goals = evaluator.subGoalDone(init[0], leftType, GoalState.DONE);
 			if (goals== null || goals.length == 0) { // good, we have type-constant 
 				Object evaluatedType = evaluator.produceResult();
 				if (evaluatedType instanceof RubyClassType) {
 					RubyMixinClass mixinClass = RubyMixinModel.getInstance()
 							.createRubyClass((RubyClassType) evaluatedType);
 					if (mixinClass != null)
 						this.selectionElements.addAll(Arrays.asList(mixinClass.getSourceTypes()));
 				}
 			} else {
 				if (goals[0] instanceof NonTypeConstantTypeGoal) {
 					processNonTypeConstant((NonTypeConstantTypeGoal) (goals[0]));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Uses goal info for selection on non-type goal
 	 * @param ngoal
 	 */
 	private void processNonTypeConstant (NonTypeConstantTypeGoal ngoal) {
 		IMixinElement element = ngoal.getElement();
 		if (element != null) {
 			Object[] eObjects = element.getAllObjects();
 			for (int i = 0; i < eObjects.length; i++) {
 				if (eObjects[i] instanceof RubyMixinElementInfo) {
 					RubyMixinElementInfo info = (RubyMixinElementInfo) eObjects[i];
 					Object obj = info.getObject();
 					if (obj instanceof IModelElement) {
 						this.selectionElements.add(obj);
 					}
 				}
 			}
 		}
 	}
 
 	private void selectOnConstant(ModuleDeclaration parsedUnit,
 			ConstantReference node,
 			org.eclipse.dltk.core.ISourceModule modelModule) {
 		BasicContext basicContext = new BasicContext(
 				(org.eclipse.dltk.core.ISourceModule) sourceModule, parsedUnit);
 		ConstantReferenceEvaluator evaluator = new ConstantReferenceEvaluator(
 				new ExpressionTypeGoal(basicContext, node));
 		IGoal[] init = evaluator.init();
 		if (init == null || init.length == 0) {
 			Object evaluatedType = evaluator.produceResult();
 			if (evaluatedType instanceof RubyClassType) {
 				RubyMixinClass mixinClass = RubyMixinModel.getInstance()
 						.createRubyClass((RubyClassType) evaluatedType);
 				if (mixinClass != null)
 					this.selectionElements.addAll(Arrays.asList(mixinClass.getSourceTypes()));
 			}
 		} else if (init[0] instanceof NonTypeConstantTypeGoal) { // it'a
 																	// non-type
 																	// constant
 			processNonTypeConstant((NonTypeConstantTypeGoal) init[0]);
 		}
 	}
 
 	/**
 	 * Checks, whether giver selection is correct selection, or can be expanded
 	 * to correct selection region. As result will set
 	 * this.actualSelection(Start|End) properly. In case of incorrect selection,
 	 * will return false.
 	 * 
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
 				// return true;
 			}
 			range2 = RubySyntaxUtils.insideMethodOperator(source, start);
 			if (range != null
 					&& (range2 == null || range2.getLength() < range
 							.getLength()))
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
 
 	private void selectTypes(org.eclipse.dltk.core.ISourceModule modelModule,
 			ModuleDeclaration parsedUnit, ASTNode node) {
 		if (node instanceof ConstantReference) {
 			selectOnConstant(parsedUnit, (ConstantReference) node,
 					modelModule);
 		} else if (node instanceof RubyColonExpression) {
 			selectOnColonExpression(parsedUnit, (RubyColonExpression) node, modelModule);
 		}
 		
 		if (this.selectionElements.isEmpty()) {
 			TypeNameMatchRequestor requestor = new TypeNameMatchRequestor() {
 				public void acceptTypeNameMatch(TypeNameMatch match) {
 					selectionElements.add(match.getType());
 				}
 			};
 			String unqualifiedName = null;
 			if (node instanceof RubyColonExpression) {
 				RubyColonExpression expr = (RubyColonExpression) node;
 				unqualifiedName = expr.getName();
 			} else if (node instanceof ConstantReference) {
 				ConstantReference expr = (ConstantReference) node;
 				unqualifiedName = expr.getName();
 			}
 			if (unqualifiedName != null) {
 				DLTKModelUtil.searchTypeDeclarations(modelModule
 						.getScriptProject(), unqualifiedName, requestor);
 			}
 		}
 	}
 
 	private void selectionOnVariable(
 			org.eclipse.dltk.core.ISourceModule modelModule,
 			ModuleDeclaration parsedUnit, VariableReference e) {
 		String name = e.getName();
 		if (name.startsWith("@")) {
 			IField[] fields = RubyModelUtils.findFields(modelModule,
 					parsedUnit, name, e.sourceStart());
 			for (int i = 0; i < fields.length; i++) {
 				selectionElements.add(fields[i]);
 			}
 		} else { // local vars (legacy, saved for speed reasons: we dont need
 			// to use mixin model for local vars)
 			ASTNode parentScope = null;
 			MethodDeclaration methodDeclaration = ASTUtils.getEnclosingMethod(
 					wayToNode, e, false);
 			if (methodDeclaration != null) {
 				List arguments = methodDeclaration.getArguments();
 				for (Iterator iterator = arguments.iterator(); iterator
 						.hasNext();) {
 					Argument arg = (Argument) iterator.next();
 					if (arg.getName().equals(name)) {
 						selectionElements.add(createLocalVariable(name, arg
 								.sourceStart(), arg.sourceEnd()));
 						return;
 					}
 				}
 				parentScope = methodDeclaration;
 			} else if (wayToNode.length >= 2) {
 				parentScope = wayToNode[wayToNode.length - 2];
 			}
 			if (parentScope != null) {
 				RubyAssignment[] assignments = RubyTypeInferencingUtils
 						.findLocalVariableAssignments(parentScope, e, name);
 				if (assignments.length > 0) {
 					RubyAssignment assignment = assignments[0];
 					selectionElements.add(createLocalVariable(name, assignment
 							.getLeft().sourceStart(), assignment.getLeft()
 							.sourceEnd()));
 				} else {
 					selectionElements.add(createLocalVariable(name, e
 							.sourceStart(), e.sourceEnd()));
 				}
 			}
 
 		}
 
 	}
 
 	private IField createLocalVariable(String name, int nameStart, int nameEnd) {
 		return new FakeField((ModelElement) sourceModule, name, nameStart,
 				nameEnd - nameStart);
 	}
 
 	private IType[] getSourceTypesForClass(ModuleDeclaration parsedUnit,
 			ASTNode statement) {
 		ExpressionTypeGoal typeGoal = new ExpressionTypeGoal(
 				new BasicContext(
 						(org.eclipse.dltk.core.ISourceModule) sourceModule,
 						parsedUnit), statement);
 		IEvaluatedType evaluatedType = this.inferencer.evaluateType(typeGoal,
 				5000);
 		if (evaluatedType instanceof RubyClassType) {
 			RubyMixinClass mixinClass = RubyMixinModel.getInstance()
 					.createRubyClass((RubyClassType) evaluatedType);
 			if (mixinClass != null)
 				return mixinClass.getSourceTypes();
 		}
 		return new IType[0];
 	}
 
 	private void selectionOnTypeDeclaration(ModuleDeclaration parsedUnit,
 			TypeDeclaration typeDeclaration) {
 		// if (typeDeclaration instanceof RubyClassDeclaration) {
 		// RubyClassDeclaration rcd = (RubyClassDeclaration) typeDeclaration;
 		// IType[] types = getSourceTypesForClass(parsedUnit, rcd
 		// .getClassName());
 		// selectionElements.addAll(Arrays.asList(types));
 		// }
 		IModelElement elementAt = null;
 		try {
 			elementAt = ((org.eclipse.dltk.core.ISourceModule) sourceModule)
 					.getElementAt(typeDeclaration.sourceStart() + 1);
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (elementAt != null)
 			selectionElements.add(elementAt);
 	}
 
 	private void selectionOnMethodDeclaration(ModuleDeclaration parsedUnit,
 			MethodDeclaration methodDeclaration) {
 		IModelElement elementAt = null;
 		try {
 			elementAt = ((org.eclipse.dltk.core.ISourceModule) sourceModule)
 					.getElementAt(methodDeclaration.sourceStart() + 1);
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (elementAt != null)
 			selectionElements.add(elementAt);
 	}
 
 	private void selectOnMethod(
 			org.eclipse.dltk.core.ISourceModule modelModule,
 			ModuleDeclaration parsedUnit, CallExpression parentCall) {
 		String methodName = ((CallExpression) parentCall).getName();
 		ASTNode receiver = parentCall.getReceiver();
 
 		IMethod[] availableMethods = null;
 		IMethod[] availableMethods2 = null;
 
 		if (receiver == null) {
 			RubyClassType type = RubyTypeInferencingUtils.determineSelfClass(
 					modelModule, parsedUnit, parentCall.sourceStart());
 			availableMethods = RubyModelUtils.searchClassMethods(modelModule,
 					parsedUnit, type, methodName);
 		} else {
 			ExpressionTypeGoal goal = new ExpressionTypeGoal(new BasicContext(
 					modelModule, parsedUnit), receiver);
 			IEvaluatedType type = inferencer.evaluateType(goal, 5000);
 			availableMethods = RubyModelUtils.searchClassMethods(modelModule,
 					parsedUnit, type, methodName);
 			if (receiver instanceof VariableReference) {
 				availableMethods2 = RubyModelUtils.getVirtualMethods(
 						(VariableReference) receiver, parsedUnit, modelModule,
 						methodName);
 			}
 		}
 
 		if (availableMethods2 != null) {
 			IMethod[] newm = new IMethod[((availableMethods != null) ? availableMethods.length
 					: 0)
 					+ availableMethods2.length];
 			int next = 0;
 			if (availableMethods != null) {
 				System.arraycopy(availableMethods, 0, newm, 0,
 						availableMethods.length);
 				next = availableMethods.length;
 			}
 			System.arraycopy(availableMethods2, 0, newm, next,
 					availableMethods2.length);
 			availableMethods = newm;
 		}
 
 		if (availableMethods == null || availableMethods.length == 0) {
 			final Collection methods = new ArrayList();
 			SearchRequestor requestor = new SearchRequestor() {
 
 				public void acceptSearchMatch(SearchMatch match)
 						throws CoreException {
 					IModelElement modelElement = (IModelElement) match.getElement();
 					org.eclipse.dltk.core.ISourceModule sm = (org.eclipse.dltk.core.ISourceModule) modelElement.getAncestor(IModelElement.SOURCE_MODULE);
 					IModelElement elementAt = sm.getElementAt(match.getOffset());
 					methods.add(elementAt);
 				}
 
 			};
 			DLTKModelUtil.searchMethodDeclarations(modelModule
 					.getScriptProject(), methodName, requestor);
 			availableMethods = (IMethod[]) methods.toArray(new IMethod[methods
 					.size()]);
 			if (availableMethods.length > 0)
 				System.out
 						.println("RubySelectionEngine.selectOnMethod() used global search");
 		}
 
 		if (availableMethods != null) {
 			int count = 0;
 			for (int i = 0; i < availableMethods.length; i++) {
 				if (availableMethods[i].getElementName().equals(methodName)) {
 					selectionElements.add(availableMethods[i]);
 					++count;
 				}
 			}
 		}
 
 	}
 
 }
