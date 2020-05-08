 /*******************************************************************************
  * Copyright (c) 2012 eBay Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     eBay Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.util.TypeCheckUtil;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IInferred;
 import org.eclipse.vjet.dsf.jst.IJstGlobalFunc;
 import org.eclipse.vjet.dsf.jst.IJstGlobalProp;
 import org.eclipse.vjet.dsf.jst.IJstGlobalVar;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstOType;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstArray;
 import org.eclipse.vjet.dsf.jst.declaration.JstAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstDeferredType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstInferredRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstInferredType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstMixedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPotentialAttributedMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstPotentialOtypeMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstSynthesizedMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeReference;
 import org.eclipse.vjet.dsf.jst.declaration.JstVar;
 import org.eclipse.vjet.dsf.jst.declaration.JstVariantType;
 import org.eclipse.vjet.dsf.jst.declaration.JstVars;
 import org.eclipse.vjet.dsf.jst.declaration.SynthOlType;
 import org.eclipse.vjet.dsf.jst.declaration.TopLevelVarTable;
 import org.eclipse.vjet.dsf.jst.declaration.VarTable;
 import org.eclipse.vjet.dsf.jst.expr.ArrayAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.AssignExpr;
 import org.eclipse.vjet.dsf.jst.expr.BoolExpr;
 import org.eclipse.vjet.dsf.jst.expr.CastExpr;
 import org.eclipse.vjet.dsf.jst.expr.ConditionalExpr;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.FuncExpr;
 import org.eclipse.vjet.dsf.jst.expr.InfixExpr;
 import org.eclipse.vjet.dsf.jst.expr.JstArrayInitializer;
 import org.eclipse.vjet.dsf.jst.expr.JstInitializer;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.expr.ObjCreationExpr;
 import org.eclipse.vjet.dsf.jst.expr.PostfixExpr;
 import org.eclipse.vjet.dsf.jst.expr.PrefixExpr;
 import org.eclipse.vjet.dsf.jst.meta.BaseJsCommentMetaNode;
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta;
 import org.eclipse.vjet.dsf.jst.meta.JsCommentMetaNode;
 import org.eclipse.vjet.dsf.jst.meta.JsType;
 import org.eclipse.vjet.dsf.jst.meta.JsTypingMeta;
 import org.eclipse.vjet.dsf.jst.stmt.CatchStmt;
 import org.eclipse.vjet.dsf.jst.stmt.ExprStmt;
 import org.eclipse.vjet.dsf.jst.stmt.ForInStmt;
 import org.eclipse.vjet.dsf.jst.stmt.RtnStmt;
 import org.eclipse.vjet.dsf.jst.stmt.WithStmt;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.term.JstLiteral;
 import org.eclipse.vjet.dsf.jst.term.JstProxyIdentifier;
 import org.eclipse.vjet.dsf.jst.term.NV;
 import org.eclipse.vjet.dsf.jst.term.ObjLiteral;
 import org.eclipse.vjet.dsf.jst.term.SimpleLiteral;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.token.ILHS;
 import org.eclipse.vjet.dsf.jst.token.IStmt;
 import org.eclipse.vjet.dsf.jst.traversal.IJstVisitor;
 import org.eclipse.vjet.dsf.jst.ts.JstTypeSpaceMgr;
 import org.eclipse.vjet.dsf.jst.util.JstTypeHelper;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsAttributed;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsVariantType;
 import org.eclipse.vjet.dsf.jstojava.resolver.IThisScopeContext;
 import org.eclipse.vjet.dsf.jstojava.resolver.ITypeConstructContext;
 import org.eclipse.vjet.dsf.jstojava.resolver.OTypeResolverRegistry;
 import org.eclipse.vjet.dsf.jstojava.resolver.ThisObjScopeResolverRegistry;
 import org.eclipse.vjet.dsf.jstojava.resolver.ThisScopeContext;
 import org.eclipse.vjet.dsf.jstojava.resolver.TypeConstructContext;
 import org.eclipse.vjet.dsf.jstojava.resolver.TypeConstructorRegistry;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper.RenameableSynthJstProxyMethod;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper.RenameableSynthJstProxyProp;
 import org.eclipse.vjet.dsf.ts.group.IGroup;
 
 class JstExpressionTypeLinker implements IJstVisitor {
 
 	private static final String THIS = "this";
 	public static final String WINDOW = "Window";
 	public static final String GLOBAL = "Global";
 	public static final String WINDOW_VAR = "window";
 
 	private final JstExpressionBindingResolver m_resolver;
 	private final JstExpressionTypeLinkerHelper.GlobalNativeTypeInfoProvider m_provider;
 
 	private IJstType m_currentType;
 	private Stack<ScopeFrame> m_scopeStack = new Stack<ScopeFrame>();
 	private HierarcheQualifierSearcher m_searcher = new HierarcheQualifierSearcher();
 
 	private GroupInfo m_groupInfo = null;
 	private boolean m_typeConstructedDuringLink;
 	private final boolean m_hasObjectLiteralResolvers;
 
 	JstExpressionTypeLinker(JstExpressionBindingResolver resolver) {
 		m_hasObjectLiteralResolvers = OTypeResolverRegistry.getInstance()
 				.hasResolvers();
 		m_resolver = resolver;
 		m_provider = new JstExpressionTypeLinkerHelper.GlobalNativeTypeInfoProvider() {
 			@Override
 			public LinkerSymbolInfo findTypeInSymbolMap(final String name,
 					final List<VarTable> varTablesBottomUp) {
 				return JstExpressionTypeLinker.this.findTypeInSymbolMap(name,
 						varTablesBottomUp);
 			}
 		};
 	}
 
 	/**
 	 * getters/setters
 	 */
 	public IJstType getType() {
 		return m_currentType;
 	}
 
 	public void setCurrentType(IJstType currentType) {
 		this.m_currentType = currentType;
 	}
 
 	void setGroupName(String groupName) {
 		List<String> dependentGroups = null;
 		JstTypeSpaceMgr tsMgr = m_resolver.getController().getJstTypeSpaceMgr();
 		IGroup<IJstType> currentGroup = tsMgr.getTypeSpace()
 				.getGroup(groupName);
 		if (currentGroup != null) {
 			List<IGroup<IJstType>> groupDependency = currentGroup
 					.getGroupDependency();
 			if (groupDependency != null && !groupDependency.isEmpty()) {
 				dependentGroups = new ArrayList<String>(groupDependency.size());
 				for (IGroup<IJstType> group : groupDependency) {
 					dependentGroups.add(group.getName());
 				}
 			}
 		}
 		m_groupInfo = new GroupInfo(groupName, dependentGroups);
 	}
 
 	private ScopeFrame getCurrentScopeFrame() {
 		if (m_scopeStack.empty()) {
 			return new ScopeFrame(m_currentType, false);
 		}
 		return m_scopeStack.peek();
 	}
 
 	/**
 	 * get the resolver
 	 * 
 	 * @return
 	 */
 	public JstExpressionBindingResolver resolver() {
 		return m_resolver;
 	}
 
 	/**
 	 * visit the node in prior to the visits of its children nodes
 	 */
 	public void preVisit(IJstNode node) {
 		final IJstType object = JstCache.getInstance().getType("Object"); // get
 																			// global
 
 		if (m_currentType == null) {
 			m_scopeStack.push(new ScopeFrame(object, false)); // scope unknown
 			return;
 		}
 
 		if (node instanceof IJstType) {
 			final IJstType type = (IJstType) node;
 			if (!type.isEmbededType()) { // visiting a top level JstType, clear
 											// scope
 				m_scopeStack.clear();
 			}
 		}
 
 		// inner type
 		if (node instanceof IJstType && node != m_currentType) {
 			setCurrentType((IJstType) node);
 		}
 
 		// push current scope into the ScopeStack
 		//
 		if (node instanceof IJstMethod) { // methods in props or protos
 
 			IJstMethod mtd = (IJstMethod) node;
 			if (mtd.getParentNode() != m_currentType) {
 				m_scopeStack.push(new ScopeFrame(object, false)); // scope
 																	// unknown
 			}
 			// by huzhou@ebay.com pushing unknown scope for ftype#_invoke_
 			else if (m_currentType != null && m_currentType.isFType()
 					&& mtd.getName() != null
 					&& "_invoke_".equals(mtd.getName().getName())) {
 				m_scopeStack.push(new ScopeFrame(object, false));
 			} else {
 				IJstType ownerType = mtd.getOwnerType();
 
 				if (mtd.isStatic()) { // static scope
 					m_scopeStack.push(new ScopeFrame(JstTypeHelper
 							.getJstTypeRefType(ownerType), true, mtd));
 				} else {
 					m_scopeStack.push(new ScopeFrame(ownerType, false, mtd));
 				}
 			}
 		} else if (node instanceof JstBlock
 				&& node.getParentNode() == m_currentType) { // inits block
 			m_scopeStack.push(new ScopeFrame(JstTypeHelper
 					.getJstTypeRefType(m_currentType), true));
 		}
 		// visit with expr in prior to its body
 		else if (node instanceof WithStmt) {
 			m_scopeStack.push(new ScopeFrame(object, false)); // scope unknown
 		}
 		// push new scope for catch statement, inherits current scope
 		else if (node instanceof CatchStmt) {
 			getCurrentScopeFrame().pushCatchVar(
 					((CatchStmt) node).getException());
 		}
 	}
 
 	/**
 	 * visit the node and determines whether its children nodes should be
 	 * visited or not TODO unify with preVisit
 	 * 
 	 * @param node
 	 * @return
 	 */
 	public boolean visit(IJstNode node) {
 		if (node == null) {
 			return false;
 		} else if (node instanceof JstTypeReference) {
 			return true;
 		}
 
 		if (node instanceof JstType) {
 			visitJstType((JstType) node);
 		} else if (node instanceof JstVar) {
 			visitJstVar((JstVar) node);
 			return false;
 		} else if (node instanceof JstIdentifier) {
 			visitIdentifier((JstIdentifier) node);
 			return node instanceof JstProxyIdentifier;
 		} else if (node instanceof JstVars) {
 			visitJstVars((JstVars) node);
 		} else if (node instanceof JstProperty) {
 			visitJstProperty((JstProperty) node);
 		} else if (node instanceof JstMethod) {
 			visitJstMethod((JstMethod) node);
 		} else if (node instanceof FuncExpr) {
 			visitFuncExpr((FuncExpr) node);
 		} else if (node instanceof ForInStmt) { // process var in ForInStmt
 												// before visiting its block
 			visitForInStmt((ForInStmt) node);
 		} else if (node instanceof JstBlock) {
 			visitJstBlock((JstBlock) node);
 		} else if (node instanceof ObjLiteral) {
 			visitObjLiteral((ObjLiteral) node);
 		} else if (node instanceof NV) {
 			visitNV((NV) node);
 		} else if (node instanceof SimpleLiteral) {
 			visitSimpleLiteral((SimpleLiteral) node);
 		} else if (node instanceof ArrayAccessExpr) {
 			visitArrayAccessExpr((ArrayAccessExpr) node);
 		}
 
 		return true;
 	}
 
 	private void visitObjLiteral(ObjLiteral node) {
 
 		// are there any otype field resolvers?
 		// only check one time for speed in
 		if (!m_hasObjectLiteralResolvers) {
 			return;
 		}
 		// bind object literal since there are resolvers
 		// we can be faster here...
 		// get list of type keys from registered resolvers
 		// then do the following if object literal contains literal name
 
 		if (node.getResultType() instanceof SynthOlType) {
 			JstExpressionTypeLinkerHelper.doObjLiteralAndOTypeBindings(node,
 					(SynthOlType) node.getResultType(), null, this, null);
 		}
 
 	}
 
 	private void visitJstMethod(final JstMethod method) {
 		// removed by huzhou@ebay.com, moved to
 		// JstExpressionTypeLinkerHelper#fixMethodTypeRef
 		// final IJstType rtnType = method.getRtnType();
 		// if(rtnType instanceof JstAttributedType){
 		// final IJstNode rtnBinding =
 		// JstExpressionTypeLinkerHelper.look4ActualBinding(m_resolver, rtnType,
 		// m_groupInfo);
 		// if(rtnBinding instanceof IJstOType && rtnBinding != rtnType){
 		// method.setRtnType((IJstOType)rtnBinding);
 		// }
 		// }
 	}
 
 	private void visitSimpleLiteral(final SimpleLiteral literal) {
 		final IJstType literalType = literal.getResultType();
 		final IJstType extendedType = JstExpressionTypeLinkerHelper
 				.getExtendedType(literalType, m_groupInfo);
 		if (extendedType != literalType) {
 			literal.setResultType(extendedType);
 		}
 	}
 
 	private void visitArrayAccessExpr(ArrayAccessExpr node) {
 		final IJstType componentType = node.getResultType();
 		if (componentType instanceof JstType
 				&& !((JstType) componentType).getStatus().isValid()) {
 			final IJstType potentialOtypeMemberType = componentType;
 			IJstOType resolvedOtype = JstExpressionTypeLinkerHelper
 					.getOtype(potentialOtypeMemberType.getName());
 			if (resolvedOtype == null) {
 				resolvedOtype = JstExpressionTypeLinkerHelper
 						.getOtype(JstExpressionTypeLinkerHelper
 								.getFullNameIfShortName4InnerType(getType(),
 										potentialOtypeMemberType));
 			}
 			if (resolvedOtype != null) {
 				node.setType(resolvedOtype);
 			}
 		}
 	}
 
 	private void visitFuncExpr(FuncExpr funcExpr) {
 		final JstMethod function = funcExpr.getFunc();
 		JstExpressionTypeLinkerHelper.fixMethodTypeRef(m_resolver, function,
 				getType(), m_groupInfo);
 	}
 
 	private void visitJstProperty(JstProperty property) {
 		JstExpressionTypeLinkerHelper.fixPropertyTypeRef(m_resolver, this,
 				property, m_groupInfo);
 	}
 
 	private void visitJstType(JstType node) {
 		for (IJstMethod staticMtd : node.getMethods(true, false)) {
 			if (staticMtd instanceof JstMethod) {
 				JstExpressionTypeLinkerHelper.fixMethodTypeRef(m_resolver,
 						(JstMethod) staticMtd, getType(), m_groupInfo);
 			}
 		}
 		for (IJstMethod instanceMtd : node.getMethods(false, false)) {
 			if (instanceMtd instanceof JstMethod) {
 				JstExpressionTypeLinkerHelper.fixMethodTypeRef(m_resolver,
 						(JstMethod) instanceMtd, getType(), m_groupInfo);
 			}
 		}
 		if (node.getConstructor() != null) {
 			JstExpressionTypeLinkerHelper.fixMethodTypeRef(m_resolver,
 					node.getConstructor(), getType(), m_groupInfo);
 		}
 	}
 
 	private void visitJstBlock(final JstBlock jstBlock) {
 		if (jstBlock == null) {
 			return;
 		}
 
 		for (IStmt stmt : jstBlock.getStmts()) {
 			if (stmt instanceof ExprStmt) {
 				final IExpr expr = ((ExprStmt) stmt).getExpr();
 				if (expr instanceof FuncExpr) {
 					final FuncExpr funcExpr = (FuncExpr) expr;
 					final IJstMethod mtd = funcExpr.getFunc();
 
 					final VarTable varTable = JstExpressionTypeLinkerHelper
 							.getVarTable(stmt);
 					final String varName = mtd.getName().getName();
 					if (varTable != null
 							&& varTable.getVarNode(varName) == null) {
 						varTable.addVarNode(varName, mtd/* , true */);
 					}
 				}
 			}
 		}
 	}
 
 	private void visitNV(NV node) {
 		JstIdentifier identifier = node.getIdentifier();
 		IJstType type = identifier.getType();
 		if (type == null) {
 			type = node.getValue().getResultType();
 		}
 		final IJstNode bound = JstExpressionTypeLinkerHelper
 				.look4ActualBinding(m_resolver, type, m_groupInfo);
 		if (bound != null) {
 			identifier.setJstBinding(bound);
 			if (bound instanceof RenameableSynthJstProxyProp) {
 				((RenameableSynthJstProxyProp) bound).setName(identifier
 						.getName());
 			} else if (bound instanceof RenameableSynthJstProxyMethod) {
 				((RenameableSynthJstProxyMethod) bound).setName(identifier
 						.getName());
 			}
 
 			IJstType resolvedType = type;
 			if (type instanceof JstAttributedType) {
 				resolvedType = JstExpressionTypeLinkerHelper
 						.getResolvedAttributedType(m_resolver, identifier,
 								(JstAttributedType) type, bound);
 			}
 			JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver, this,
 					identifier, resolvedType, m_groupInfo);
 		}
 
 		// TODO do we need to do this now? possible revisit
 		if (type instanceof JstObjectLiteralType) {// otype, infer rhs
 			if (node.getValue() != null
 					&& node.getValue() instanceof ObjLiteral) {
 				final ObjLiteral rhsObjLiteral = (ObjLiteral) node.getValue();
 				final IJstType rhsType = rhsObjLiteral.getResultType();
 				if (rhsType instanceof SynthOlType) {
 					((SynthOlType) rhsType).addResolvedOType(type);
 				}
 			}
 		}
 	}
 
 	private void visitJstVars(final JstVars vars) {
 		JstExpressionTypeLinkerHelper.fixVarsTypeRef(m_resolver, vars,
 				m_groupInfo);
 
 		IJstType varType = vars.getType();
 		if (varType instanceof JstType
 				&& !((JstType) varType).getStatus().isValid()) {
 			final IJstType potentialOtypeMemberType = varType;
 			IJstOType resolvedOtype = JstExpressionTypeLinkerHelper
 					.getOtype(potentialOtypeMemberType.getName());
 			if (resolvedOtype == null) {
 				resolvedOtype = JstExpressionTypeLinkerHelper
 						.getOtype(JstExpressionTypeLinkerHelper
 								.getFullNameIfShortName4InnerType(getType(),
 										potentialOtypeMemberType));
 			}
 			if (resolvedOtype != null) {
 				vars.setType(resolvedOtype);
 			}
 		} else if (varType instanceof JstAttributedType) {
 			final IJstNode varBinding = JstExpressionTypeLinkerHelper
 					.look4ActualBinding(m_resolver, varType, m_groupInfo);
 			if (varBinding instanceof IJstOType && varBinding != varType) {
 				varType = (IJstOType) varBinding;
 				vars.setType((IJstOType) varBinding);
 			}
 		}
 
 		JstInitializer initializer = vars.getInitializer();
 		if (initializer != null) {
 			List<AssignExpr> list = initializer.getAssignments();
 			for (AssignExpr assignExpr : list) {
 				ILHS lhs = assignExpr.getLHS();
 				if (lhs instanceof JstIdentifier) {
 					JstIdentifier identifier = (JstIdentifier) lhs;
 					final VarTable varTable = JstExpressionTypeLinkerHelper
 							.getVarTable(identifier);
 					if (varTable != null) {
 						if (varTable.getVarNode(identifier.getName()) == null) {
 							varTable.addVarNode(identifier.getName(), lhs/*
 																		 * ,
 																		 * true
 																		 */);
 						}
 						varTable.addVarType(identifier.getName(), varType);
 					}
 				}
 			}
 		}
 	}
 
 	private void visitJstVar(final JstVar var) {
 		IJstType varType = var.getType();
 		if (varType instanceof JstType
 				&& !((JstType) varType).getStatus().isValid()) {
 			final IJstType potentialOtypeMemberType = varType;
 			IJstOType resolvedOtype = JstExpressionTypeLinkerHelper
 					.getOtype(potentialOtypeMemberType.getName());
 			if (resolvedOtype == null) {
 				resolvedOtype = JstExpressionTypeLinkerHelper
 						.getOtype(JstExpressionTypeLinkerHelper
 								.getFullNameIfShortName4InnerType(getType(),
 										potentialOtypeMemberType));
 			}
 			if (resolvedOtype != null) {
 				var.setType(resolvedOtype);
 			}
 		} else if (varType instanceof JstAttributedType) {
 			final IJstNode varBinding = JstExpressionTypeLinkerHelper
 					.look4ActualBinding(m_resolver, varType, m_groupInfo);
 			if (varBinding instanceof IJstOType && varBinding != varType) {
 				varType = (IJstOType) varBinding;
 				var.setType((IJstOType) varBinding);
 			}
 		}
 
 		final VarTable varTable = JstExpressionTypeLinkerHelper
 				.getVarTable(var);
 		if (varTable != null) {
 			if (varTable.getVarNode(var.getName()) == null) {
 				varTable.addVarNode(var.getName(), var/* , true */);
 			}
 			varTable.addVarType(var.getName(), varType);
 		}
 	}
 
 	private void visitIdentifier(JstIdentifier identifier) {
 		IJstNode parent = identifier.getParentNode();
 		// only resolve identifier without qualifiers, e.g. this.name, resolve
 		// this, not name
 		if (JstExpressionTypeLinkerHelper.isJstIdentifierVisitExcluded(
 				identifier, parent)) {
 			return;
 		}
 		visitIdentifierCommon(identifier, parent);
 
 		if (JstExpressionTypeLinkerHelper.isResolveExcluded(identifier, parent)) {
 			return;
 		}
 		visitIdentifierAndUpdateBindings(identifier);
 	}
 
 	private void visitIdentifierAndUpdateBindings(final JstIdentifier identifier) {
 		final String identifierName = identifier.getName();
 		final LinkerSymbolInfo info = findTypeInSymbolMap(identifierName,
 				JstExpressionTypeLinkerHelper.getVarTablesBottomUp(identifier));
 		if (info != null) {
 			final IJstType knownType = info.getType();
 			final IJstNode knownBinding = info.getBinding();
 			final IJstNode actualBinding = JstExpressionTypeLinkerHelper
 					.look4ActualBinding(m_resolver, knownType, m_groupInfo);
 			if (actualBinding != null && actualBinding != knownBinding) {
 				// update binding
 				identifier.setJstBinding(actualBinding);
 				info.setBinding(actualBinding);
 
 				if (actualBinding instanceof IJstType
 						&& actualBinding != knownType) {
 					final IJstType actualType = (IJstType) actualBinding;
 					identifier.setType(actualType);
 					info.setType(actualType);
 				} else if (actualBinding instanceof IJstProperty) {
 					final IJstType actualType = ((IJstProperty) actualBinding)
 							.getType();
 					identifier.setType(actualType);
 					info.setType(actualType);
 				} else if (actualBinding instanceof IJstMethod) {
 					final IJstType actualType = new JstFuncType(
 							(IJstMethod) actualBinding);
 					identifier.setType(actualType);
 					info.setType(actualType);
 				}
 			}
 		}
 	}
 
 	private void visitIdentifierCommon(JstIdentifier identifier, IJstNode parent) {
 		final String name = identifier.toExprText();
 		// lookup at catch var e.g. catch(ex) first, then current scope second
 		final LinkerSymbolInfo info = findTypeInSymbolMap(name,
 				JstExpressionTypeLinkerHelper.getVarTablesBottomUp(identifier));
 
 		if (info != null) {
 			IJstNode varBinding = info.getBinding();
 			if (varBinding != null && varBinding != identifier) {
 				IJstType varType = JstExpressionTypeLinkerHelper.getVarType(
 						m_resolver, varBinding);
 				if (varType == null) {
 					varType = info.getType();
 				}
 
 				if (varType instanceof JstFuncType) {
 					JstExpressionTypeLinkerHelper.updateFunctionType(
 							(JstFuncType) varType, m_groupInfo);
 				}
 				identifier.setJstBinding(varBinding);
 				identifier.setType(varType);
 				JstExpressionTypeLinkerHelper.look4ActualBinding(m_resolver,
 						varType, m_groupInfo);
 				if (varType != null && varType instanceof JstInferredType
 						&& ((JstInferredType) varType).modified()) {
 					JstInferredType inferredType = (JstInferredType) varType;
 					Set<Object> scopes = new HashSet<Object>();
 					scopes.addAll(m_scopeStack);
 					IJstType currentType = inferredType.getCurrentType(
 							identifier.getSource().getStartOffSet(), scopes);
 					if (!isSameType(currentType, inferredType.getType())) {
 						if (!(currentType instanceof IInferred)) {
 							if (currentType instanceof IJstRefType) {
 								currentType = new JstInferredRefType(
 										(IJstRefType) currentType);
 							} else {
 								currentType = new JstInferredType(currentType);
 							}
 						}
 						JstExpressionTypeLinkerHelper.doExprTypeUpdate(
 								m_resolver, this, identifier, currentType,
 								m_groupInfo);
 					} else {
 						JstExpressionTypeLinkerHelper.doExprTypeUpdate(
 								m_resolver, this, identifier, varType,
 								m_groupInfo);
 					}
 				} else {
 					JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver,
 							this, identifier, varType, m_groupInfo);
 				}
 
 				// bugfix by huzhou@ebay.com only updates the symbol table when
 				// there's a type changes (binding doesn't change in this case)
 				if (varType != info.getType()) {
 					info.setBinding(varBinding);
 					info.setType(varType);
 				}
 			} else {
 				identifier.setJstBinding(info.getBinding());
 				JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver,
 						this, identifier, info.getType(), m_groupInfo);
 			}
 		} else if (name.equals(THIS)) { // this keyword
 			resolveThisIdentifier(identifier);
 		} else if (JstExpressionTypeLinkerHelper.getFromWithVarName(m_resolver,
 				getCurrentScopeFrame(), identifier, m_groupInfo)) {
 
 		} else if (JstExpressionTypeLinkerHelper.getFromGlobalTypeName(
 				m_resolver, getCurrentScopeFrame(), identifier, m_groupInfo)) {
 
 		} else if (JstExpressionTypeLinkerHelper.getFromGlobalVarName(
 				m_resolver, getCurrentScopeFrame(), identifier, m_groupInfo)) {
 
 		}
 	}
 
 	/**
 	 * helper for {@link #visitIdentifier(JstIdentifier)}
 	 * 
 	 * @param identifier
 	 * @return
 	 */
 	private IJstType resolveThisIdentifier(JstIdentifier identifier) {
 		IJstType currentType = getCurrentScopeFrame().getCurrentType();
 
 		if (getCurrentScopeFrame().getNode() != null
 				&& getCurrentScopeFrame().getNode() instanceof IJstMethod) {
 			IJstMethod mtd = (IJstMethod) getCurrentScopeFrame().getNode();
 			String mtdKey = createMtdKey(mtd);
 
 			ThisObjScopeResolverRegistry registry = ThisObjScopeResolverRegistry
 					.getInstance();
 			// TODO built with type constructor check because group dependency
 			// check would be too slow
 			// if(registry.hasResolver(mtdKey)) {
 			IThisScopeContext context = new ThisScopeContext(currentType, mtd);
 			registry.resolve(mtdKey, context);
 			IJstType newType = context.getThisType();
 			if (newType != null) {
 				currentType = newType;
 			}
 			// }
 
 		}
 
 		identifier.setJstBinding(currentType);
 		identifier.setType(currentType);
 		return currentType;
 	}
 
 	private void visitForInStmt(ForInStmt forStmt) {
 		ILHS var = forStmt.getVar();
 		IJstType objType = forStmt.getExpr().getResultType();
 
 		IJstType array = JstCache.getInstance().getType("Array");
 		IJstType integer = JstCache.getInstance().getType("int");
 		IJstType string = JstCache.getInstance().getType("String");
 
 		if (var instanceof JstVar) {
 
 			JstVar jstVar = (JstVar) var;
 
 			if (objType != null
 					&& !(objType instanceof JstInferredType)
 					&& (objType == array || JstTypeHelper.isTypeOf(objType,
 							array))) {
 				jstVar.setType(integer);
 			} else {
 				jstVar.setType(string);
 			}
 
 			String varName = jstVar.getName();
 
 			final LinkerSymbolInfo info = findTypeInSymbolMap(varName,
 					JstExpressionTypeLinkerHelper.getVarTablesBottomUp(jstVar));
 			if (info != null) {
 				info.setBinding(jstVar);
 				info.setType(jstVar.getType());
 			}
 		} else if (var instanceof JstIdentifier) {
 			final JstIdentifier jstIdentifier = (JstIdentifier) var;
 			if (jstIdentifier.getJstBinding() == jstIdentifier) {
 				jstIdentifier.setJstBinding(null); // clear binding to itself
 			}
 			if (jstIdentifier.getJstBinding() == null) { // implicit global, no
 															// previous var
 															// declared with
 															// same name
 
 				if (objType != null
 						&& (objType == array || JstTypeHelper.isTypeOf(objType,
 								array))) {
 					jstIdentifier.setType(integer);
 				} else {
 					jstIdentifier.setType(string);
 				}
 			}
 
 			final LinkerSymbolInfo info = findTypeInSymbolMap(
 					jstIdentifier.toExprText(),
 					JstExpressionTypeLinkerHelper
 							.getVarTablesBottomUp(jstIdentifier));
 			if (info != null) {
 				info.setType(jstIdentifier.getType());
 			}
 		}
 	}
 
 	/**
 	 * @see postVisit
 	 */
 	public void endVisit(IJstNode node) {
 		// emptied by huzhou@ebay.com, moved all into postVisit
 	}
 
 	/**
 	 * visit the node after the visits of its children nodes this
 	 * postVisit(IJstNode) method serves as the dispatcher
 	 */
 	public void postVisit(IJstNode node) {
 		// inner type
 		if (node instanceof IJstType && node == m_currentType) {
 			postVisitCurrentType();
 		} else if (node instanceof JstVars) {
 			postVisitJstVars((JstVars) node);
 		} else if (node instanceof JstVar) {
 			postVisitJstVar((JstVar) node);
 		} else if (node instanceof JstArg) {
 			postVisitJstArg((JstArg) node);
 		} else if (node instanceof MtdInvocationExpr) {
 			postVisitMtdInvocationExpr((MtdInvocationExpr) node);
 		} else if (node instanceof ObjCreationExpr) {
 			postVisitObjCreationExpr((ObjCreationExpr) node);
 		} else if (node instanceof FieldAccessExpr) {
 			postVisitFieldAccessExpr((FieldAccessExpr) node);
 		} else if (node instanceof ArrayAccessExpr) {
 			postVisitArrayAccessExpr((ArrayAccessExpr) node);
 		} else if (node instanceof AssignExpr) {
 			postVisitAssignExpr((AssignExpr) node);
 		} else if (node instanceof JstProxyIdentifier) {
 			postVisitJstProxyIdentifier((JstProxyIdentifier) node);
 		} else if (node instanceof IJstMethod
 				|| node instanceof WithStmt
 				|| (node instanceof JstBlock && node.getParentNode() == m_currentType)) {
 			if (!m_scopeStack.isEmpty()) {
 				m_scopeStack.pop();
 			}
 			if (node instanceof WithStmt) {
 				postVisitWithStmt((WithStmt) node);
 			} // handle potential otype method binding
 			else if (node instanceof JstPotentialOtypeMethod) {
 				postVisitJstPotentialOtypeMethod((JstPotentialOtypeMethod) node);
 			} // handle potential attributed type method binding
 			else if (node instanceof JstPotentialAttributedMethod) {
 				postVisitJstPotentialAttributedMethod((JstPotentialAttributedMethod) node);
 			}
 		} else if (node instanceof CatchStmt) {
 			getCurrentScopeFrame().popCatchVar();
 		} else if (node instanceof JstProperty) {
 			postVisitJstProperty((JstProperty) node);
 		} else if (node instanceof PrefixExpr) {
 			postVisitPrefixExpr((PrefixExpr) node);
 		} else if (node instanceof PostfixExpr) {
 			postVisitPostfixExpr((PostfixExpr) node);
 		} else if (node instanceof InfixExpr) {
 			postVisitInfixExpr((InfixExpr) node);
 		} else if (node instanceof JstArrayInitializer) {
 			postVisitJstArrayInitializer((JstArrayInitializer) node);
 		} else if (node instanceof RtnStmt) {
 			postVisitRtnStmt((RtnStmt) node);
 		} else if (node instanceof FuncExpr) {
 			postVisitFuncExpr((FuncExpr) node);
 		} else if (node instanceof ExprStmt) {
 			postVisitExprStmt((ExprStmt) node);
 		} else if (node instanceof ConditionalExpr) {
 			postVisitConditionalExpr((ConditionalExpr) node);
 		}else if(node instanceof BaseJsCommentMetaNode){
 			((BaseJsCommentMetaNode) node).getParentNode().removeChild(node);
 			node = null;
 		}
 	}
 
 	private void postVisitCurrentType() {
 		IJstType outerType = m_currentType.getOuterType();
 		if (outerType != null && outerType != m_currentType) {
 			setCurrentType(outerType);
 		} else if (m_currentType.getParentNode() instanceof IJstType) {
 			setCurrentType((IJstType) m_currentType.getParentNode());
 		}
 
 	}
 
 	private void postVisitConditionalExpr(final ConditionalExpr condExpr) {
 		final IExpr ifValExpr = condExpr.getThenExpr();
 		final IExpr elseValExpr = condExpr.getElseExpr();
 		if (ifValExpr != null && elseValExpr != null
 				&& ifValExpr.getResultType() != null
 				&& elseValExpr.getResultType() != null
 				&& ifValExpr.getResultType() != elseValExpr.getResultType()) {
 			condExpr.setResultType(new JstVariantType(Arrays.asList(
 					ifValExpr.getResultType(), elseValExpr.getResultType())));
 		}
 	}
 
 	private void postVisitJstPotentialOtypeMethod(
 			final JstPotentialOtypeMethod method) {
 		if (method.getResolvedOtypeMethod() == null) {
 			final IJstType potentialOtypeJstFunctionRefType = method
 					.getPotentialOtypeJstFunctionRefType();
 			if (potentialOtypeJstFunctionRefType != null) {
 				final IJstOType resolvedOtype = JstExpressionTypeLinkerHelper
 						.getOtype(potentialOtypeJstFunctionRefType.getName());
 				if (resolvedOtype instanceof JstFunctionRefType) {
 					// recreate the method to match the JstFunctionRefType
 					// declaration
 					method.setResolvedOtypeMethod(((JstFunctionRefType) resolvedOtype)
 							.getMethodRef());
 					JstExpressionTypeLinkerTraversal.accept(method, this);
 				}
 			}
 		}
 	}
 
 	private void postVisitJstPotentialAttributedMethod(
 			final JstPotentialAttributedMethod method) {
 		if (method.getResolvedAttributedMethod() == null) {
 			final IJstType potentialJstAttributedType = method
 					.getPotentialJstAttributedType();
 			if (potentialJstAttributedType instanceof JstAttributedType) {
 				final JstAttributedType attributedType = ((JstAttributedType) potentialJstAttributedType);
 				final IJstMethod attributedMethod = attributedType
 						.getAttributorType()
 						.getMethod(
 								attributedType.getAttributeName() != null ? attributedType.getAttributeName()
 										: "",
 								attributedType.isStaticAttribute());
 				if (attributedMethod != null) {
 					method.setResolvedAttributedMethod(attributedMethod);
 					JstExpressionTypeLinkerTraversal.accept(method, this);
 				}
 			}
 		}
 	}
 
 	/**
 	 * post visit return statement, which could resolve the JstDeferredType if
 	 * met
 	 * 
 	 * @param rtnStmt
 	 */
 	private void postVisitRtnStmt(final RtnStmt rtnStmt) {
 		final IExpr rtnExpr = rtnStmt.getExpression();
 		final IJstMethod enclosingMtd = JstExpressionTypeLinkerHelper
 				.look4EnclosingMethod(rtnStmt);
 		if (JstExpressionTypeLinkerHelper.doesExprRequireResolve(rtnExpr)) {
 			if (enclosingMtd != null && enclosingMtd.getRtnType() != null) {
 
 				JstExpressionTypeLinkerHelper.doExprTypeResolve(m_resolver,
 						this, rtnExpr, enclosingMtd.getRtnType());
 			}
 		}
 		JstExpressionTypeLinkerHelper.tryDerivingAnonymousFunctionsFromReturn(
 				rtnStmt, enclosingMtd, this);
 		
		if(enclosingMtd!=null){
			inferRtnType(rtnExpr, enclosingMtd);
		}
 
 	}
 
 	private void inferRtnType(final IExpr rtnExpr, final IJstMethod enclosingMtd) {
 		if(rtnExpr==null){
 			return;
 		}
 		IJstType inferType = rtnExpr.getResultType();
 		JstMethod jstMethod = (JstMethod) enclosingMtd;
 		if (enclosingMtd.getRtnType() == null) {
 			
 			if (inferType != null && !(inferType instanceof JstInferredType)) {
 				jstMethod.setReturnOptional(true);
 				jstMethod.setRtnType(new JstInferredType(
 						inferType));
 			} else if (inferType != null) {
 				jstMethod.setReturnOptional(true);
 				jstMethod.setRtnType(new JstInferredType(
 						inferType));
 			}
 		} else if (enclosingMtd.getRtnType() instanceof JstInferredType) {
 			// get existing inferred type
 
 			JstInferredType inferredType = (JstInferredType) enclosingMtd
 					.getRtnType();
 			IJstType originalType = inferredType.getType();
 
 			if (inferType !=null && originalType instanceof JstMixedType) {
 				jstMethod.setReturnOptional(true);
 				JstMixedType mixedTypes = (JstMixedType) originalType;
 				mixedTypes.getMixedTypes().add(inferType);
 
 			}else if(inferType !=null && inferType!=originalType){
 				jstMethod.setReturnOptional(true);
 				Map<IJstType,String> types = new LinkedHashMap<IJstType,String>();
 				types.put(originalType,null);
 				types.put(inferType,null);
 				List<IJstType> list = new ArrayList<IJstType>();
 				list.addAll(types.keySet());
 				JstMixedType mixed = new JstMixedType(list);
 				jstMethod.setRtnType(new JstInferredType(mixed));
 			}
 		}
 	}
 
 	/**
 	 * post visit {@link JstArrayInitializer}
 	 * 
 	 * @param array
 	 */
 	private void postVisitJstArrayInitializer(final JstArrayInitializer array) {
 		final IJstType objectType = JstExpressionTypeLinkerHelper
 				.getNativeObjectJstType(m_resolver);
 		IJstType arrType = array.getResultType();
 		if (array.getResultType() == null) {
 			IJstType candidateComponentType = null;
 			if (array.getExprs() != null) {
 				for (IExpr component : array.getExprs()) {
 					if (component == null) {
 						continue;
 					}
 
 					if (candidateComponentType == null) {
 						candidateComponentType = component.getResultType();
 					} else if (candidateComponentType == component
 							.getResultType()) {
 						continue;
 					} else {
 						candidateComponentType = objectType;
 						break;
 					}
 				}
 			}
 
 			final JstArray arrayType = new JstArray(
 					candidateComponentType != null ? candidateComponentType
 							: objectType);
 			JstExpressionTypeLinkerHelper.updateArrayType((JstArray) arrayType,
 					m_groupInfo);
 			JstExpressionTypeLinkerHelper.updateResultType(
 					(JstArrayInitializer) array, arrayType);// number type?
 		} else if (arrType instanceof JstArray) {
 			JstExpressionTypeLinkerHelper.updateArrayType((JstArray) arrType,
 					m_groupInfo);
 		}
 	}
 
 	/**
 	 * post visit {@link InfixExpr}
 	 * 
 	 * @param expr
 	 */
 	private void postVisitInfixExpr(final InfixExpr expr) {
 		final InfixExpr.Operator op = expr.getOperator();
 		if (InfixExpr.Operator.PLUS.equals(op)) {
 			final IJstType stringType = JstExpressionTypeLinkerHelper
 					.getNativeStringJstType(m_resolver);
 			final IJstType numType = JstExpressionTypeLinkerHelper
 					.getNativeNumberJstType(m_resolver);
 			final IJstType objectType = JstExpressionTypeLinkerHelper
 					.getNativeObjectJstType(m_resolver);
 			final IJstType leftType = expr.getLeft() != null ? expr.getLeft()
 					.getResultType() : null;
 			final IJstType rightType = expr.getRight() != null ? expr
 					.getRight().getResultType() : null;
 			if (numType != null
 					&& leftType != null
 					&& numType.getSimpleName().equals(leftType.getSimpleName())
 					&& rightType != null
 					&& numType.getSimpleName()
 							.equals(rightType.getSimpleName())) {
 				JstExpressionTypeLinkerHelper.updateResultType(expr, numType);// int
 																				// type
 			} else if (stringType != null
 					&& (((leftType != null && stringType.getSimpleName()
 							.equals(leftType.getSimpleName())) || (rightType != null && stringType
 							.getSimpleName().equals(rightType.getSimpleName()))))) {
 				JstExpressionTypeLinkerHelper
 						.updateResultType(expr, stringType);// String type
 			} else if (objectType != null
 					&& (((leftType != null && objectType.getSimpleName()
 							.equals(leftType.getSimpleName())) || (rightType != null && objectType
 							.getSimpleName().equals(rightType.getSimpleName()))))) {
 				JstExpressionTypeLinkerHelper
 						.updateResultType(expr, objectType);// Object type
 			} else {
 				// Don't set the result type here
 			}
 		} else if (InfixExpr.Operator.CONDITIONAL_OR.equals(expr.getOperator())) {
 			final IExpr left = expr.getLeft();
 			final IExpr right = expr.getRight();
 			if (left != null && right != null) {
 				final IJstType inferredResultType = expr.getResultType();
 				final JstDeferredType deferredType = new JstDeferredType(
 						inferredResultType != null ? inferredResultType
 								: JstExpressionTypeLinkerHelper
 										.getNativeObjectJstType(m_resolver));
 				deferredType.addCandidateType(left.getResultType());
 				deferredType.addCandidateType(right.getResultType());
 				JstExpressionTypeLinkerHelper.updateResultType(expr,
 						deferredType);
 			}
 		} else {
 			final IJstType numType = JstExpressionTypeLinkerHelper
 					.getNativeNumberJstType(m_resolver);
 			JstExpressionTypeLinkerHelper.updateResultType(expr, numType);// number
 																			// type?
 		}
 	}
 
 	/**
 	 * post visit PostfixExpr postfixExpr are {++, --} which determines the
 	 * result type has to be Number
 	 * 
 	 * @param expr
 	 */
 	private void postVisitPostfixExpr(final PostfixExpr expr) {
 		final IJstType numType = JstExpressionTypeLinkerHelper
 				.getNativeNumberJstType(m_resolver);
 		JstExpressionTypeLinkerHelper.updateResultType(expr, numType);// number
 																		// type?
 	}
 
 	/**
 	 * post visit PrefixExpr
 	 * 
 	 * @param expr
 	 */
 	private void postVisitPrefixExpr(final PrefixExpr expr) {
 		IJstType type = expr.getResultType();
 		if (PrefixExpr.Operator.TYPEOF.equals(expr.getOperator())) {
 			final IJstType stringType = JstExpressionTypeLinkerHelper
 					.getNativeStringJstType(m_resolver);
 			if (!TypeCheckUtil.isAssignable(stringType, type)) {
 				JstExpressionTypeLinkerHelper
 						.updateResultType(expr, stringType);// string type
 			}
 		} else if (PrefixExpr.Operator.DELETE.equals(expr.getOperator())) {
 			final IJstType objectType = JstExpressionTypeLinkerHelper
 					.getNativeObjectJstType(m_resolver);
 			JstExpressionTypeLinkerHelper.updateResultType(expr, objectType);// object
 																				// type
 		} else if (PrefixExpr.Operator.NOT.equals(expr.getOperator())) {
 			final IJstType boolType = JstCache.getInstance().getType("boolean");
 			if (!TypeCheckUtil.isAssignable(boolType, type)) {
 				JstExpressionTypeLinkerHelper.updateResultType(expr, boolType);// boolean
 																				// type
 			}
 		} else if (PrefixExpr.Operator.VOID.equals(expr.getOperator())) {
 			final IJstType voidType = JstExpressionTypeLinkerHelper
 					.getNativeVoidJstType(m_resolver);
 			JstExpressionTypeLinkerHelper.updateResultType(expr, voidType);// void
 																			// type?
 		} else {
 			final IJstType numType = JstExpressionTypeLinkerHelper
 					.getNativeNumberJstType(m_resolver);
 			if (!TypeCheckUtil.isAssignable(numType, type)) {
 				JstExpressionTypeLinkerHelper.updateResultType(expr, numType);// number
 																				// type?
 			}
 		}
 	}
 
 	/**
 	 * Post visit {@link JstProperty} to update their type as initializer's Expr
 	 * result type
 	 * 
 	 * @param pty
 	 */
 	private void postVisitJstProperty(JstProperty pty) {
 		if (pty.getType() instanceof JstInferredType) {
 			JstInferredType type = (JstInferredType) pty.getType();
 			if ("Object".equals(type.getType().getName())) {
 				if (pty.getInitializer() != null) {
 					IExpr initializer = pty.getInitializer();
 					if (initializer.getResultType() != null) {
 						pty.setType(new JstInferredType(initializer
 								.getResultType()));
 					}
 				} else if (pty.getValue() != null
 						&& pty.getValue() instanceof JstLiteral) {
 					JstLiteral val = (JstLiteral) pty.getValue();
 					if (val.getResultType() != null) {
 						pty.setType(new JstInferredType(val.getResultType()));
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * post visit {@link ArrayAccessExpr} and set the result type as Array's
 	 * component type
 	 * 
 	 * @param aae
 	 */
 	private void postVisitArrayAccessExpr(final ArrayAccessExpr aae) {
 		IJstType qualifierType = JstExpressionTypeLinkerHelper
 				.getQualifierType(m_resolver, aae);
 		if (qualifierType != null) {
 			if (qualifierType instanceof JstArray) {
 				JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver,
 						this, aae,
 						((JstArray) qualifierType).getComponentType(),
 						m_groupInfo);
 			} else if (qualifierType instanceof JstVariantType) {
 				final JstVariantType variantQualifierType = (JstVariantType) qualifierType;
 				final List<IJstType> variantTypes = variantQualifierType
 						.getVariantTypes();
 				final List<JstArray> arrayTypes = new ArrayList<JstArray>(
 						variantTypes.size());
 				for (IJstType variantType : variantTypes) {
 					if (variantType instanceof JstArray) {
 						arrayTypes.add((JstArray) variantType);
 					}
 				}
 				if (arrayTypes.size() == 1) {
 					JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver,
 							this, aae, arrayTypes.get(0).getComponentType(),
 							m_groupInfo);
 				} else if (arrayTypes.size() > 1) {
 					final List<IJstType> componentTypes = new ArrayList<IJstType>(
 							arrayTypes.size());
 					for (JstArray arrayType : arrayTypes) {
 						componentTypes.add(arrayType.getComponentType());
 					}
 					final JstVariantType variantComponentType = new JstVariantType(
 							componentTypes);
 					JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver,
 							this, aae, variantComponentType, m_groupInfo);
 				}
 			}
 
 			// enhancement by huzhou@ebay.com to support type properties'
 			// accessing using array accessing style
 			final IExpr indexExpr = aae.getIndex();
 			if (indexExpr instanceof SimpleLiteral) {
 				final SimpleLiteral indexLiteral = ((SimpleLiteral) indexExpr);
 				if ("String".equals(indexLiteral.getResultType().getName())) {
 					final String indexValue = indexLiteral.getValue();
 					final boolean isStatic = JstExpressionTypeLinkerHelper
 							.isStaticRef(qualifierType);
 					IJstProperty pty = JstExpressionTypeLinkerHelper
 							.getProperty(qualifierType, indexValue, isStatic);
 					if (pty == null) {
 						pty = JstExpressionTypeLinkerHelper
 								.getProperty(qualifierType,
 										'"' + indexValue + '"', isStatic);
 					}
 					if (pty == null) {
 						pty = JstExpressionTypeLinkerHelper
 								.getProperty(qualifierType, "'" + indexValue
 										+ "'", isStatic);
 					}
 					if (pty != null) {
 						aae.setType(pty.getType());
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * post visit {@link JstProxyIdentifier}
 	 * 
 	 * @param id
 	 */
 	private void postVisitJstProxyIdentifier(JstProxyIdentifier id) {
 		postVisit(id.getActualExpr());
 
 		// actual expression could be bound with a function in one of the
 		// following cases:
 		// 1. JstFuncType
 		// 2. JstAttributedType
 		// 3. FuncExpr (should visit FuncExpr and resolve as JstFuncType)
 		// 4. ObjCreationExpr (new Function() corner case)
 		final IExpr actualExpr = id.getActualExpr();
 		if (actualExpr != null) {
 			if (actualExpr.getResultType() instanceof JstFuncType) {
 				final JstFuncType funcType = (JstFuncType) actualExpr
 						.getResultType();
 				if (funcType != null) {
 					id.setJstBinding(funcType.getFunction());
 				}
 			} else if (actualExpr.getResultType() instanceof JstAttributedType) {
 				final JstAttributedType attributedType = (JstAttributedType) actualExpr
 						.getResultType();
 				if (attributedType != null) {
 					final IJstNode attributedBinding = attributedType
 							.getJstBinding();
 					if (attributedBinding != null
 							&& attributedBinding instanceof IJstMethod) {
 						id.setJstBinding(attributedBinding);
 					}
 				}
 			} else if (actualExpr instanceof FuncExpr) {
 				final FuncExpr funcExpr = (FuncExpr) actualExpr;
 				id.setJstBinding(funcExpr.getFunc());
 			} else if (actualExpr instanceof ObjCreationExpr) {
 				final ObjCreationExpr objCreateExpr = (ObjCreationExpr) actualExpr;
 				IJstType resultType = objCreateExpr.getResultType();
 				// TODO look into why result type for new Function() is not binding before reaching this test
 				if (resultType!=null && "Function".equals(resultType
 						.getSimpleName())) {
 					final JstSynthesizedMethod flexMtd = JstExpressionTypeLinkerHelper
 							.createFlexMethod(m_resolver, null);
 					id.setJstBinding(flexMtd);
 				}
 			}
 			id.setType(actualExpr.getResultType());
 		}
 	}
 
 	/**
 	 * post visit {@link MtdInvocationExpr}
 	 */
 	@SuppressWarnings("deprecation")
 	private void postVisitMtdInvocationExpr(MtdInvocationExpr mie) {
 
 		if (JstExpressionTypeLinkerHelper.bindThisMtdInvocationInConstructs(
 				m_resolver, this, mie, getCurrentScopeFrame().getCurrentType(),
 				m_groupInfo)) {
 			return;
 		}
 
 		JstIdentifier methodId = JstExpressionTypeLinkerHelper.getName(mie);
 		if (JstExpressionTypeLinkerHelper.isEmptyExpr(methodId)) {
 			return;
 		}
 
 		IJstType qualifierType = JstExpressionTypeLinkerHelper
 				.getQualifierType(m_resolver, mie);
 		IJstType mtdBindingType = methodId.getType();
 		String methodName = methodId.getName();
 
 		final IJstType vjoSyntacticType = JstExpressionTypeLinkerHelper
 				.processSyntacticCalls(mie, methodName, m_provider);
 		if (mtdBindingType != null) { // constructor
 			if (JstExpressionTypeLinkerHelper.checkConstructorCalls(m_resolver,
 					this, mie, methodId, mtdBindingType)) {
 				return;// constructor handled
 			}
 		}
 
 		// extracted by huzhou for further linking case:
 		// anonymous function argument infer from parameter definition
 		IJstNode mtdBinding = null;
 		// search method in qualifier
 		if (qualifierType != null) {
 			// by huzhou@ebay.com ftype references are always static
 			final boolean isStatic = JstExpressionTypeLinkerHelper
 					.isStaticRef(qualifierType);
 			mtdBinding = JstExpressionTypeLinkerHelper.getCorrectMethod(
 					m_resolver, qualifierType, methodName, isStatic);
 
 			if (mtdBinding != null) {
 				JstExpressionTypeLinkerHelper.bindMtdInvocationExpr(m_resolver,
 						this, mtdBinding, qualifierType, mie, m_groupInfo);
 				if (vjoSyntacticType != null) { // replace result type of
 												// endType()
 					// call
 					mie.setResultType(vjoSyntacticType);
 				}
 				JstExpressionTypeLinkerHelper
 						.tryDerivingAnonymousFunctionsFromParam(mie,
 								mtdBinding, this, m_groupInfo);
 
 				if (mtdBinding instanceof JstMethod) {
 					JstMethod mtd = (JstMethod) mtdBinding;
 					// create type based on java extension
 					// TODO consolidate this CnP from
 					// getReturnTypeFormFactoryEnabled
 					String mtdKey = createMtdKey(mtd);
 
 					constructType(mie, null, mtdKey, MtdInvocationExpr.class);
 				}
 
 				return;
 			} else {
 				// try global ext function in prior
 				if (mie.getQualifyExpr() != null
 						&& mie.getQualifyExpr() instanceof JstIdentifier) {
 					final JstIdentifier qualifier = (JstIdentifier) mie
 							.getQualifyExpr();
 					final IJstNode qualifierBinding = qualifier.getJstBinding();
 					String globalVarName = null;
 					if (qualifierBinding != null) {
 						globalVarName = JstExpressionTypeLinkerHelper
 								.getGlobalVarNameFromBinding(qualifierBinding);
 					}
 					if (globalVarName != null) {
 						if (JstExpressionTypeLinkerHelper.isGlobalVarExtended(
 								m_resolver, globalVarName)) {
 							final IJstGlobalVar extVar = JstExpressionTypeLinkerHelper
 									.getGlobalVarExtensionByName(m_resolver,
 											methodName, globalVarName);
 							if (extVar != null) {
 								mtdBinding = JstExpressionTypeLinkerHelper
 										.look4ActualGlobalVarBinding(
 												m_resolver, extVar, m_groupInfo);
 								JstExpressionTypeLinkerHelper.doMethodBinding(
 										m_resolver, mie, methodId, mtdBinding);
 							}
 						}
 					}
 				}
 
 				IJstType declaringType = m_searcher.searchType(qualifierType,
 						methodName, HierarchyDepth.THIS, isStatic);
 				if (declaringType != null) {
 					mtdBinding = JstExpressionTypeLinkerHelper
 							.getDeclaredMethod(declaringType, methodName,
 									isStatic);
 					if (mtdBinding == null) {
 						mtdBinding = JstExpressionTypeLinkerHelper
 								.getConstructs(declaringType, methodName);
 					}
 
 					if (mtdBinding == null
 							&& qualifierType instanceof IJstRefType) {
 						declaringType = JstExpressionTypeLinkerHelper
 								.getNativeFunctionJstType(m_resolver);
 						mtdBinding = JstExpressionTypeLinkerHelper
 								.getDeclaredMethod(declaringType, methodName,
 										isStatic);
 					}
 
 					if (mtdBinding != null) {
 						JstExpressionTypeLinkerHelper.bindMtdInvocationExpr(
 								m_resolver, this, mtdBinding, qualifierType,
 								mie, m_groupInfo);
 						JstExpressionTypeLinkerHelper
 								.tryDerivingAnonymousFunctionsFromParam(mie,
 										mtdBinding, this, m_groupInfo);
 						return;
 					}
 				}
 			}
 		} else {// no qualifier, global method or local function
 			mtdBinding = methodId.getJstBinding();
 			if (mtdBinding != null) {
 
 				// by huzhou@ebay.com to allow inferred function binding to work
 				if (mtdBindingType instanceof JstInferredType) {
 					mtdBindingType = ((JstInferredType) mtdBindingType)
 							.getType();
 				}
 				// added in the case where args are functions, and used directly
 				if (mtdBindingType instanceof JstFuncType) {
 					final JstFuncType funcType = (JstFuncType) mtdBindingType;
 					mtdBinding = funcType.getFunction();
 				}
 				if (mtdBindingType instanceof JstAttributedType) {
 					mtdBinding = JstExpressionTypeLinkerHelper
 							.look4ActualBinding(m_resolver, mtdBindingType,
 									m_groupInfo);
 				}
 				if (mtdBindingType instanceof JstFunctionRefType) {
 					final JstFunctionRefType funcType = (JstFunctionRefType) mtdBindingType;
 					mtdBinding = funcType.getMethodRef();
 				}
 				if (mtdBindingType != null && mtdBindingType.isFType()) {
 					mtdBinding = JstExpressionTypeLinkerHelper
 							.getFTypeInvokeMethod(m_resolver, mtdBindingType);
 				}
 
 				if (mtdBinding instanceof IJstGlobalProp) {
 					mtdBinding = JstExpressionTypeLinkerHelper
 							.getFurtherGlobalVarBinding(m_resolver,
 									(IJstGlobalProp) mtdBinding, m_groupInfo);
 				}
 				// revisit binding
 				if (mtdBinding instanceof IJstGlobalFunc) {
 					mtdBinding = JstExpressionTypeLinkerHelper
 							.getFurtherGlobalVarBinding(m_resolver,
 									(IJstGlobalFunc) mtdBinding, m_groupInfo);
 				}
 
 				if (mtdBinding instanceof IJstMethod) {
 					IJstMethod mtd = (IJstMethod) mtdBinding;
 					IJstType rtnType = null;
 					if (mtd.isTypeFactoryEnabled()) {
 						rtnType = JstExpressionTypeLinkerHelper
 								.getReturnTypeFormFactoryEnabled(mie, mtd);
 					}
 					if (rtnType == null) {
 						rtnType = JstExpressionTypeLinkerHelper
 								.getBestRtnTypeFromAllOverloadMtds(mie, mtd);
 					}
 
 					mie.setResultType(rtnType);
 					JstExpressionTypeLinkerHelper
 							.tryDerivingAnonymousFunctionsFromParam(mie,
 									mtdBinding, this, m_groupInfo);
 					JstExpressionTypeLinkerHelper.bindMtdInvocations(
 							m_resolver, this, mie, mtdBinding, m_groupInfo);
 					return;
 				}
 			}
 		}
 
 		// check if invocation through full qualified name
 		IJstType fullNamedType = checkFullyQualifierTypeInvocation(mie);
 
 		if (fullNamedType == null) { // check if invocation through this() etc.
 			JstSource source = JstExpressionTypeLinkerHelper
 					.createSourceRef(mie);
 			resolver()
 					.error("Cant find "
 							+ methodName
 							+ " method in "
 							+ JstExpressionTypeLinkerHelper.getTypeName(qualifierType)
 							+ " type.",
 							JstExpressionTypeLinkerHelper
 									.getTypeName(qualifierType),
 							source.getStartOffSet(), source.getEndOffSet(),
 							source.getRow(), source.getColumn());
 		} else {
 			final IJstType currentType = getType();
 			if (currentType != null && currentType instanceof JstType) {
 				((JstType) currentType).addFullyQualifiedImport(fullNamedType);
 			}
 			mie.addChild(new JstTypeReference(currentType));
 		}
 	}
 
 	/**
 	 * @param mtd
 	 * @return
 	 */
 	private String createMtdKey(IJstMethod mtd) {
 		if (mtd == null) {
 			return "";
 		}
 		String mtdKey = mtd.getOwnerType().getName();
 		mtdKey += mtd.isStatic() ? "::" : ":";
 		mtdKey += mtd.getName().getName();
 		return mtdKey;
 	}
 
 	/**
 	 * @param mie
 	 * @param mtdKey
 	 * @param class1
 	 */
 	private void constructType(MtdInvocationExpr mie, IExpr lhs, String mtdKey,
 			Class<? extends IExpr> class1) {
 		// TODO use only with double caret
 		if (m_currentType == null) {
 			return;
 		}
 
 		TypeConstructorRegistry tcr = TypeConstructorRegistry.getInstance();
 
 		if (tcr.hasResolver(mtdKey)) {
 			List<IExpr> exprs = mie.getArgs();
 
 			// TODO pass the reference to JSTCompletion
 			ITypeConstructContext constrCtx = new TypeConstructContext(mie,
 					lhs, exprs, null, class1, m_groupInfo.getGroupName(),
 					m_currentType.getSource(), m_currentType.getName());
 			// resolve
 			tcr.resolve(mtdKey, constrCtx);
 
 			if (constrCtx.getTypes().size() > 0) {
 				// TODO test nested types here
 				setCurrentType(constrCtx.getTypes().get(0));
 				setTypeConstructedDuringLink(true);
 				// TODO how to support multiple return types
 			}
 
 		}
 	}
 
 	private void setTypeConstructedDuringLink(boolean b) {
 		m_typeConstructedDuringLink = b;
 
 	}
 
 	public boolean getTypeConstructedDuringLink() {
 		return m_typeConstructedDuringLink;
 
 	}
 
 	private void postVisitObjCreationExpr(final ObjCreationExpr objCreationExpr) {
 		final JsCommentMetaNode metaNode = JstExpressionTypeLinkerHelper
 				.getJsCommentMetaNode(objCreationExpr);
 		final IJstType metaType = metaNode != null ? metaNode.getResultType()
 				: null;
 		final IExpr mtdInvocationExpr = objCreationExpr.getExpression();
 		if (mtdInvocationExpr != null) {
 			final IJstType prevType = mtdInvocationExpr.getResultType();
 			if (metaType != null && metaType != prevType) {
 				if (mtdInvocationExpr instanceof MtdInvocationExpr) {
 					((MtdInvocationExpr) mtdInvocationExpr)
 							.setResultType(metaType);
 				}
 			}
 		}
 	}
 
 	private void postVisitExprStmt(final ExprStmt stmt) {
 		final IExpr expr = stmt.getExpr();
 		if (expr instanceof JstIdentifier) {
 			JstIdentifier id = (JstIdentifier) expr;
 			handleGlobalVarBinding(id, stmt, id, true);
 		} else if (expr instanceof FuncExpr) {
 			final FuncExpr funcExpr = (FuncExpr) expr;
 			final IJstMethod mtd = funcExpr.getFunc();
 
 			final VarTable varTable = JstExpressionTypeLinkerHelper
 					.getVarTable(stmt);
 			if (varTable != null
 					&& varTable.getVarNode(mtd.getName().getName()) != null) {
 				varTable.addVarType(mtd.getName().getName(),
 						funcExpr.getResultType());
 			}
 		}
 	}
 
 	/**
 	 * helper for {@link #postVisitExprStmt(ExprStmt)}
 	 * 
 	 * @param id
 	 * @param parentNode
 	 * @param parentNodeForMeta
 	 * @param setBinding
 	 */
 	private void handleGlobalVarBinding(JstIdentifier id,
 			BaseJstNode parentNode, IJstNode parentNodeForMeta,
 			boolean setBinding) {
 		if (id.getJstBinding() != null
 				|| (parentNode instanceof AssignExpr
 						&& parentNode.getParentNode() instanceof JstVars && ((AssignExpr) parentNode)
 						.getLHS() == id)) {
 			return;
 		}
 		List<IJsCommentMeta> metaList = JstExpressionTypeLinkerHelper
 				.getJsCommentMeta(parentNodeForMeta);
 		if (metaList != null && metaList.size() > 0) {
 			// TODO need to handle more cases, such as functions, attributed
 			// types
 			IJsCommentMeta meta = metaList.get(0);
 			JsTypingMeta jsTypingMeta = meta.getTyping();
 			IJstType jstType = null;
 			if (jsTypingMeta instanceof JsType) {
 				jstType = JstExpressionTypeLinkerHelper
 						.findType((JsType) jsTypingMeta);
 			} else if (jsTypingMeta instanceof JsAttributed) {
 				final JsAttributed jsAttributed = (JsAttributed) jsTypingMeta;
 				final JsType attributor = jsAttributed.getAttributor();
 				final IJstType attributorType = attributor == null ? TranslateHelper
 						.getGlobalType() : JstExpressionTypeLinkerHelper
 						.findType(attributor);
 				final String attributeName = jsAttributed.getName();
 				final boolean isStatic = !jsAttributed.isInstance();
 				jstType = new JstAttributedType(attributorType, attributeName,
 						isStatic);
 			} else if (jsTypingMeta instanceof JsVariantType) {
 				jstType = JstExpressionTypeLinkerHelper
 						.findType((JsVariantType) jsTypingMeta);
 			}
 			if (jstType != null) {
 				id.setType(jstType);
 				JstExpressionTypeLinkerHelper.look4ActualBinding(m_resolver,
 						id.getType(), m_groupInfo);
 				JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver,
 						this, id, id.getType(), m_groupInfo);
 				if (setBinding) {
 					id.setJstBinding(jstType);
 				} else {
 					id.setJstBinding(null);
 				}
 				JstTypeReference ref = new JstTypeReference(jstType);
 				TranslateHelper.setTypeRefSource(ref, meta);
 				parentNode.addChild(ref);
 			}
 		}
 		// adding to the pseudo global var table (init funciton's var table
 		// instead)
 		final VarTable varTable = JstExpressionTypeLinkerHelper.getVarTable(id);
 		if (varTable != null) {
 			if (varTable.getVarNode(id.getName()) == null) {
 				varTable.addVarNode(id.getName(), id);
 			}
 			varTable.addVarType(id.getName(), id.getType());
 		}
 	}
 
 	/**
 	 * post visit {@link FieldAccessExpr}
 	 * 
 	 * @param fae
 	 */
 	@SuppressWarnings("deprecation")
 	private void postVisitFieldAccessExpr(FieldAccessExpr fae) {
 		final JstIdentifier fieldId = fae.getName();
 		if (JstExpressionTypeLinkerHelper.isEmptyExpr(fieldId)) {
 			return;
 		}
 
 		IJstType qualifierType = JstExpressionTypeLinkerHelper
 				.getQualifierType(m_resolver, fae);
 		String fieldName = "";
 
 		if (qualifierType != null) {
 			final boolean isStatic = JstExpressionTypeLinkerHelper
 					.isStaticRef(qualifierType);
 			fieldName = fieldId.getName();
 			IJstProperty pty = JstExpressionTypeLinkerHelper.getProperty(
 					qualifierType, fieldName, isStatic);
 
 			if (pty != null) {
 				JstExpressionTypeLinkerHelper.bindFieldAccessExpr(m_resolver,
 						pty, qualifierType, fae, m_groupInfo);
 				return;
 			} else {
 				IJstType declaringType = m_searcher
 						.searchPropertyType(qualifierType, fieldName,
 								HierarchyDepth.THIS, isStatic);
 				if (declaringType != null) {
 					pty = declaringType.getProperty(fieldName, isStatic);
 
 					if (pty != null) {
 						JstExpressionTypeLinkerHelper.bindFieldAccessExpr(
 								m_resolver, pty, qualifierType, fae,
 								m_groupInfo);
 						return;
 					}
 				}
 			}
 		}
 
 		IJstType fullNamedType = checkFullyQualifierTypeAccess(fae);
 
 		// by huzhou@ebay.com handling global var extension case
 		final IExpr qualifier = fae.getExpr();// extension has only 1 level
 												// right now
 		final JstIdentifier identifier = fae.getName();
 		if (qualifier instanceof JstIdentifier && identifier != null
 				&& identifier.getName() != null) {
 			final IJstNode qualifierBinding = ((JstIdentifier) qualifier)
 					.getJstBinding();
 			if (qualifierBinding != null) {
 				String globalVarName = null;
 				if (qualifierBinding instanceof IJstGlobalProp) {
 					globalVarName = ((IJstGlobalProp) qualifierBinding)
 							.getName().getName();
 				} else if (qualifierBinding instanceof IJstGlobalFunc) {
 					globalVarName = ((IJstGlobalFunc) qualifierBinding)
 							.getName().getName();
 				}
 				if (globalVarName != null) {
 					final String identifierName = identifier.getName();
 					final List<IJstNode> extensions = m_resolver
 							.getController().getJstTypeSpaceMgr()
 							.getQueryExecutor()
 							.getGlobalExtensions(globalVarName);
 					for (IJstNode ext : extensions) {
 						if (ext instanceof IJstGlobalVar) {
 							IJstGlobalVar extVar = (IJstGlobalVar) ext;
 							if (identifierName.equals(extVar.getName()
 									.getName())) {
 								final IJstNode identifierBinding = JstExpressionTypeLinkerHelper
 										.findGlobalVarBinding(m_resolver,
 												extVar, m_groupInfo);
 								if (identifierBinding != null) {
 									identifier.setJstBinding(identifierBinding);
 								}
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		if (fullNamedType == null) { // unresolved field access expr
 			// check if there is method with same field name
 			if (qualifierType != null
 					&& !checkMethodReferenceByFieldAccess(fae, qualifierType,
 							fieldId)) {
 
 				JstSource source = JstExpressionTypeLinkerHelper
 						.createSourceRef(fae);
 				resolver()
 						.error("Cant find "
 								+ fieldName
 								+ " field in "
 								+ JstExpressionTypeLinkerHelper.getTypeName(qualifierType)
 								+ " type. with source = " + fae.getSource(),
 								JstExpressionTypeLinkerHelper
 										.getTypeName(qualifierType),
 								source.getStartOffSet(), source.getEndOffSet(),
 								source.getRow(), source.getColumn());
 			}
 		} else {// added by huzhou@ebay.com to setup the linkage to the Fully
 				// qualified type references
 			final IJstType currentType = getType();
 			if (currentType != null && currentType instanceof JstType) {
 				((JstType) currentType).addFullyQualifiedImport(fullNamedType);
 			}
 			fae.addChild(new JstTypeReference(currentType));
 		}
 	}
 
 	/**
 	 * helper for {@link #postVisitFieldAccessExpr(FieldAccessExpr)} for
 	 * FieldAccess to methods
 	 * 
 	 * @param fae
 	 * @param qualifierType
 	 * @param fieldId
 	 * @return
 	 */
 	private boolean checkMethodReferenceByFieldAccess(FieldAccessExpr fae,
 			IJstType qualifierType, JstIdentifier fieldId) {
 
 		String fieldName = fieldId.getName();
 		boolean isStatic = qualifierType instanceof IJstRefType;
 		boolean isMethodReference = false;
 
 		// search method with the property name, this.foo.call()
 		// foo refers to function
 		IJstMethod mtd = qualifierType.getMethod(fieldName, isStatic);
 
 		if (mtd != null) {
 			isMethodReference = true;
 		} else {
 			IJstType declaringType = m_searcher.searchType(qualifierType,
 					fieldName, HierarchyDepth.THIS, isStatic);
 			if (declaringType != null) {
 				mtd = declaringType.getMethod(fieldName, isStatic);
 
 				if (mtd != null) {
 					isMethodReference = true;
 				}
 			}
 		}
 
 		if (isMethodReference) {
 			fieldId.setJstBinding(mtd);
 			final JstFuncType funcType = new JstFuncType(mtd);
 			fieldId.setType(funcType);
 			fae.setType(funcType);
 		}
 
 		return isMethodReference;
 	}
 
 	private void postVisitFuncExpr(FuncExpr funcExpr) {
 		IJstType funcType = funcExpr.getResultType();
 
 		if (!(funcType instanceof JstFuncType)) {
 			if (funcType instanceof JstAttributedType) {
 				final JstAttributedType attributedType = (JstAttributedType) funcType;
 				final IJstMethod attributedFunc = attributedType
 						.getAttributorType().getMethod(
 								attributedType.getAttributeName(),
 								attributedType.isStaticAttribute(), true);
 				if (attributedFunc != null) {
 					JstExpressionTypeLinkerHelper
 							.tryDerivingAnonymousFunctionsFromAssignment(
 									funcExpr, attributedFunc, false, this);
 					funcType = new JstFuncType(attributedFunc);
 				} else {
 					funcType = new JstFuncType(funcExpr.getFunc());
 				}
 				funcExpr.setType(funcType);
 			} else if (funcType instanceof JstFunctionRefType) {
 				final JstFunctionRefType otypeFuncType = (JstFunctionRefType) funcType;
 				final IJstMethod otypeFunc = otypeFuncType.getMethodRef();
 				if (otypeFunc != null) {
 					JstExpressionTypeLinkerHelper
 							.tryDerivingAnonymousFunctionsFromAssignment(
 									funcExpr, otypeFunc, false, this);
 					funcType = new JstFuncType(otypeFunc);
 				} else {
 					funcType = new JstFuncType(funcExpr.getFunc());
 				}
 				funcExpr.setType(funcType);
 			}
 		}
 
 		if (funcType instanceof JstFuncType) {
 			JstExpressionTypeLinkerHelper.updateFunctionType(
 					(JstFuncType) funcType, m_groupInfo);
 		}
 	}
 
 	/**
 	 * post visit {@link AssignExpr}
 	 * 
 	 * @param assignExpr
 	 */
 	private void postVisitAssignExpr(AssignExpr assignExpr) {
 
 		// assignment without declaring var will create a global var without
 		// binding
 		final ILHS lhs = assignExpr.getLHS();
 		JstIdentifier identifier = null;
 		if (lhs instanceof JstIdentifier) {
 			identifier = (JstIdentifier) lhs;
 			final LinkerSymbolInfo info = findTypeInSymbolMap(
 					identifier.toExprText(),
 					JstExpressionTypeLinkerHelper
 							.getVarTablesBottomUp(assignExpr));
 			if (info == null) {
 				IExpr rhsExpr = assignExpr.getExpr();
 				if (rhsExpr instanceof CastExpr) {
 					identifier.setType(rhsExpr.getResultType());
 				}
 				handleGlobalVarBinding(identifier, assignExpr, rhsExpr, false);
 				if (identifier.getType() == null) {
 					identifier
 							.setType(JstCache.getInstance().getType("Object"));
 				}
 			}
 		}
 
 		final IJstType lhsType = lhs.getType();
 		final IExpr rhsExpr = assignExpr.getExpr();
 		final boolean rhsResolveNeeded = JstExpressionTypeLinkerHelper
 				.doesExprRequireResolve(rhsExpr);
 		if (lhsType != null && !(lhsType instanceof IInferred)
 				&& rhsResolveNeeded) {
 			// resolved
 			JstExpressionTypeLinkerHelper.doExprTypeResolve(m_resolver, this,
 					rhsExpr, lhsType);
 		} else if (lhsType instanceof IInferred && !rhsResolveNeeded
 				&& identifier != null && rhsExpr != null) {
 			IJstType rhsType = rhsExpr.getResultType();
 			if (rhsType == null) {
 				rhsType = new JstInferredType(JstCache.getInstance().getType(
 						"Object"));
 			} else if (rhsType instanceof JstInferredType) {
 				rhsType = ((JstInferredType) rhsType).getType();
 			}
 			if (!isSameType(lhsType, rhsType)) {
 				IJstNode binding = identifier.getJstBinding();
 				IJstType originalType = null;
 				if (binding instanceof JstIdentifier) {
 					originalType = ((JstIdentifier) binding).getType();
 				}
 				if (originalType instanceof JstInferredType) {
 					int pos = lhs.getSource().getStartOffSet();
 					((JstInferredType) originalType).setCurrentType(rhsType,
 							pos, m_scopeStack.peek());
 				}
 			}
 		} else if (lhsType == null && lhs instanceof FieldAccessExpr
 				&& rhsExpr != null) {
 
 			if (rhsExpr instanceof MtdInvocationExpr) {
 				MtdInvocationExpr mtdInv = (MtdInvocationExpr) rhsExpr;
 				if (mtdInv.getMethod() instanceof JstMethod) {
 					if (((JstMethod) mtdInv.getMethod()).isTypeFactoryEnabled()) {
 						constructForAssigment(lhs, rhsExpr);
 					}
 				}
 			}
 
 			IExpr qualifier = ((FieldAccessExpr) lhs).getExpr();
 			if (qualifier instanceof JstIdentifier) {
 				IJstNode binding = ((JstIdentifier) qualifier).getJstBinding();
 				IJstType qualifierType = null;
 				if (binding instanceof JstIdentifier) {
 					qualifierType = ((JstIdentifier) binding).getType();
 				}
 				if (qualifierType instanceof JstInferredType) {
 					IJstType rhsType = rhsExpr.getResultType();
 					if (rhsType == null) {
 						rhsType = new JstInferredType(JstCache.getInstance()
 								.getType("Object"));
 					}
 
 					int pos = lhs.getSource().getStartOffSet();
 					Set<Object> scopes = new HashSet<Object>();
 					scopes.addAll(m_scopeStack);
 					((JstInferredType) qualifierType).addNewProperty(
 							((FieldAccessExpr) lhs).getName().getName(),
 							rhsType, pos, scopes);
 
 				}
 				// TODO possible revisit here need to remove and test
 				if (rhsResolveNeeded && rhsExpr instanceof ObjLiteral) {
 					IJstType rhsType = rhsExpr.getResultType();
 					if (rhsType == null) {
 						rhsType = new JstInferredType(JstCache.getInstance()
 								.getType("Object"));
 					}
 					JstExpressionTypeLinkerHelper.doObjLiteralAndOTypeBindings(
 							(ObjLiteral) rhsExpr,
 							(SynthOlType) rhsExpr.getResultType(), rhsType,
 							this, null);
 				}
 
 			}
 
 		} else if ((lhs instanceof JstIdentifier || lhs instanceof FieldAccessExpr)
 				&& (rhsExpr != null) && (rhsExpr instanceof MtdInvocationExpr)) {
 
 			constructForAssigment(lhs, rhsExpr);
 
 		}
 
 	}
 
 	/**
 	 * @param lhs
 	 * @param rhsExpr
 	 */
 	private void constructForAssigment(final ILHS lhs, final IExpr rhsExpr) {
 		if (!(rhsExpr instanceof MtdInvocationExpr)) {
 			return;
 		}
 		MtdInvocationExpr mie = (MtdInvocationExpr) rhsExpr;
 		JstMethod mtd = bindMethod(mie);
 		String mtdKey = createMtdKey(mtd);
 
 		constructType(mie, (IExpr) lhs, mtdKey, AssignExpr.class);
 	}
 
 	private JstMethod bindMethod(MtdInvocationExpr mie) {
 
 		JstIdentifier methodId = JstExpressionTypeLinkerHelper.getName(mie);
 		if (JstExpressionTypeLinkerHelper.isEmptyExpr(methodId)) {
 			return null;
 		}
 		IJstType qualifierType = JstExpressionTypeLinkerHelper
 				.getQualifierType(m_resolver, mie);
 		String methodName = methodId.getName();
 
 		IJstNode mtdBinding = null;
 		// search method in qualifier
 		if (qualifierType != null) {
 			// by huzhou@ebay.com ftype references are always static
 			final boolean isStatic = JstExpressionTypeLinkerHelper
 					.isStaticRef(qualifierType);
 			mtdBinding = JstExpressionTypeLinkerHelper.getCorrectMethod(
 					m_resolver, qualifierType, methodName, isStatic);
 
 			if (mtdBinding != null) {
 				JstExpressionTypeLinkerHelper.bindMtdInvocationExpr(m_resolver,
 						this, mtdBinding, qualifierType, mie, m_groupInfo);
 
 				JstExpressionTypeLinkerHelper
 						.tryDerivingAnonymousFunctionsFromParam(mie,
 								mtdBinding, this, m_groupInfo);
 				if (mtdBinding instanceof JstMethod) {
 					return (JstMethod) mtdBinding;
 				}
 
 			}
 
 		}
 		return null;
 	}
 
 	private static boolean isSameType(IJstType t1, IJstType t2) {
 		return t1.getName().equals(t2.getName());
 	}
 
 	/**
 	 * post visit {@link JstVars}
 	 * 
 	 * @param vars
 	 */
 	private void postVisitJstVars(JstVars vars) {
 		final IJstType varType = vars.getType();
 		JstExpressionTypeLinkerHelper.look4ActualBinding(m_resolver, varType,
 				m_groupInfo);
 
 		final boolean isInferred = (varType instanceof IInferred);
 		final JstInitializer initializer = vars.getInitializer();
 		if (initializer != null) {
 			List<AssignExpr> list = initializer.getAssignments();
 			boolean varsSet = false;
 			for (AssignExpr assignExpr : list) {
 				ILHS lhs = assignExpr.getLHS();
 				IExpr rhsExpr = assignExpr.getExpr();
 				if (!isInferred) {
 					JstExpressionTypeLinkerHelper.doJsCommentMetaUpdate(
 							varType, rhsExpr);
 					if (JstExpressionTypeLinkerHelper
 							.doesExprRequireResolve(rhsExpr)) {
 						// resolved
 						JstExpressionTypeLinkerHelper.doExprTypeResolve(
 								m_resolver, this, rhsExpr, varType);
 					}
 				}
 				if (lhs instanceof JstIdentifier) {
 					JstIdentifier identifier = (JstIdentifier) lhs;
 					if (rhsExpr instanceof CastExpr) {
 						varsSet = whenRhsExprIsCast(vars, varsSet, rhsExpr,
 								identifier);
 					} else if (isInferred && rhsExpr != null) {
 						whenRhsExprCanInfer(vars, varType, list, rhsExpr,
 								identifier);
 					} else {
 						identifier.setJstBinding(varType);
 						// bugfix by huzhou@ebay.com to deal with inferred dup
 						if (varType instanceof IInferred) {
 							final IJstType cloneInferredType = (IJstType) JstExpressionTypeLinkerHelper
 									.cloneInferredType((IInferred) varType);
 							identifier.setType(cloneInferredType);
 						} else {
 							identifier.setType(varType);
 						}
 					}
 
 					final VarTable varTable = JstExpressionTypeLinkerHelper
 							.getVarTable(identifier);
 					if (varTable != null) {
 						if (varTable.getVarNode(identifier.getName()) == null) {
 							varTable.addVarNode(identifier.getName(), lhs/*
 																		 * ,
 																		 * true
 																		 */);
 						}
 						varTable.addVarType(identifier.getName(), varType);
 					}
 				}
 			}
 		}
 	}
 
 	private void whenRhsExprCanInfer(JstVars vars, IJstType varType,
 			List<AssignExpr> list, IExpr rhsExpr, JstIdentifier identifier) {
 		IJstType inferredType = varType;
 		IJstType inferingType = rhsExpr.getResultType();
 		if (inferingType != null) {
 			if (inferingType instanceof JstFuncType) {
 				inferredType = new JstInferredType(inferingType);
 				IJstNode jstBinding = ((JstFuncType) inferingType)
 						.getFunction();
 				identifier.setJstBinding(jstBinding);
 			} else {
 				if (inferingType instanceof IJstRefType) {
 					inferredType = new JstInferredRefType(
 							(IJstRefType) inferingType);
 				} else {
 					inferredType = new JstInferredType(inferingType);
 				}
 				identifier.setJstBinding(inferredType);
 			}
 
 		}
 		identifier.setType(inferredType);
 		final LinkerSymbolInfo info = findTypeInSymbolMap(
 				identifier.toExprText(),
 				JstExpressionTypeLinkerHelper.getVarTablesBottomUp(identifier));
 		if (info != null && info.getBinding() == vars) {
 			info.setType(inferredType);
 		}
 		if (list.size() == 1) {
 			vars.setType(inferredType);
 		}
 	}
 
 	private boolean whenRhsExprIsCast(JstVars vars, boolean varsSet,
 			IExpr rhsExpr, JstIdentifier identifier) {
 		IExpr cast = rhsExpr;
 		IJstNode castBinding = null;
 		final IJstType castType = cast.getResultType();
 		identifier.setType(castType);
 		if (castType instanceof JstFuncType) {
 			castBinding = ((JstFuncType) castType).getFunction();
 			identifier.setJstBinding(castBinding);
 		}
 
 		final LinkerSymbolInfo info = findTypeInSymbolMap(
 				identifier.toExprText(),
 				JstExpressionTypeLinkerHelper.getVarTablesBottomUp(identifier));
 		if (info != null && info.getBinding() == vars) {
 			info.setType(castType);
 			if (castBinding != null) {
 				info.setBinding(castBinding);
 			}
 		}
 
 		if (!varsSet) {
 			vars.setType(castType);
 		}
 		varsSet = true;
 		return varsSet;
 	}
 
 	private void postVisitJstVar(JstVar var) {
 		final VarTable varTable = JstExpressionTypeLinkerHelper
 				.getVarTable(var);
 		if (varTable != null) {
 			if (varTable.getVarNode(var.getName()) == null) {
 				varTable.addVarNode(var.getName(), var/* , true */);
 			}
 			varTable.addVarType(var.getName(), var.getType());
 		}
 	}
 
 	private void postVisitJstArg(JstArg parameter) {
 		// removed by huzhou@ebay.com, moved to
 		// JstExpressionTypeLinkerHelper#fixMethodTypeRef
 		// final List<IJstType> parameterTypes = parameter.getTypes();
 		// final List<IJstType> updatedParameterTypes = new
 		// ArrayList<IJstType>(parameterTypes.size());
 		//
 		// boolean updated = false;
 		// for(IJstType parameterType : parameterTypes){
 		// final IJstNode parameterBinding =
 		// JstExpressionTypeLinkerHelper.look4ActualBinding(m_resolver,
 		// parameterType, m_groupInfo);
 		// if(parameterBinding instanceof IJstOType && parameterType !=
 		// parameterBinding){
 		// updatedParameterTypes.add((IJstOType)parameterBinding); updated =
 		// true;
 		// }
 		// else{
 		// updatedParameterTypes.add(parameterType);
 		// }
 		// }
 		//
 		// if(updated){
 		// parameter.clearTypes();
 		// parameter.addTypes(updatedParameterTypes);
 		// }
 		//
 		final VarTable varTable = JstExpressionTypeLinkerHelper
 				.getVarTable(parameter);
 		if (varTable != null) {
 			if (varTable.getVarNode(parameter.getName()) == null) {
 				varTable.addVarNode(parameter.getName(), parameter/* , true */);
 			}
 			varTable.addVarType(parameter.getName(), parameter.getType());
 		}
 	}
 
 	private void postVisitWithStmt(WithStmt with) {
 		// TODO fix this in with
 		final BoolExpr condition = with.getCondition();
 		final IExpr scope = condition.getLeft();
 		final IJstType scopeType = scope.getResultType();
 		if (scopeType != null) {
 			final VarTable varTable = JstExpressionTypeLinkerHelper
 					.getVarTable(with);
 			for (IJstProperty pty : scopeType.getAllPossibleProperties(
 					JstExpressionTypeLinkerHelper.isStaticRef(scopeType), true)) {
 				final String varName = pty != null && pty.getName() != null ? pty
 						.getName().getName() : null;
 				if (varTable != null && varName != null
 						&& varTable.getVarNode(varName) == null) {
 					varTable.addVarNode(varName, pty/* , true */);
 				}
 			}
 			for (IJstMethod mtd : scopeType.getMethods(
 					JstExpressionTypeLinkerHelper.isStaticRef(scopeType), true)) {
 				final String varName = mtd.getName().getName();
 				if (varTable != null && varName != null
 						&& varTable.getVarNode(varName) == null) {
 					varTable.addVarNode(varName, mtd/* , true */);
 				}
 			}
 		}
 	}
 
 	/***************************************************************
 	 * SYMBOL MAP MANAGEMENT
 	 *************************************************************** 
 	 */
 	// check if the field access expression is fully qualifier type name
 	// if there is, bind to the TypeRefType
 	private IJstType checkFullyQualifierTypeAccess(
 			FieldAccessExpr fieldAccessExpr) {
 		final String fullName = fieldAccessExpr.toExprText();
 		IJstType type = JstExpressionTypeLinkerHelper.findFullQualifiedType(
 				m_resolver, fullName, m_groupInfo);
 		if (type != null) {
 			if (type.isSingleton()) {
 				JstInferredType infferedType = new JstInferredType(type);
 				fieldAccessExpr.getName().setJstBinding(infferedType);
 				JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver,
 						this, fieldAccessExpr, infferedType, m_groupInfo);
 				JstExpressionTypeLinkerHelper
 						.setPackageBindingForQualifier(fieldAccessExpr
 								.getExpr()); // set
 												// the
 												// package
 												// binding
 												// for
 												// each
 												// qualifier,
 												// e.g.
 												// a.b.c
 				return infferedType;
 			} else {
 				final JstTypeRefType typeRef = new JstTypeRefType(type);
 				fieldAccessExpr.getName().setJstBinding(typeRef);
 				JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver,
 						this, fieldAccessExpr, typeRef, m_groupInfo);
 				JstExpressionTypeLinkerHelper
 						.setPackageBindingForQualifier(fieldAccessExpr
 								.getExpr()); // set
 												// the
 												// package
 												// binding
 												// for
 												// each
 												// qualifier,
 												// e.g.
 												// a.b.c
 				return typeRef;
 			}
 		}
 		return null;
 	}
 
 	// check if the method id is fully qualifier type name
 	// if there is, bind to the constructor
 	private IJstType checkFullyQualifierTypeInvocation(
 			MtdInvocationExpr mtdInvocationExpr) {
 		final String fullName = JstExpressionTypeLinkerHelper
 				.getFullName(mtdInvocationExpr);
 		IJstType type = JstExpressionTypeLinkerHelper.findFullQualifiedType(
 				m_resolver, fullName, m_groupInfo);
 		if (type != null) {
 			JstExpressionTypeLinkerHelper.bindMtdInvocations(m_resolver, this,
 					mtdInvocationExpr, type.getConstructor(), m_groupInfo);
 			JstExpressionTypeLinkerHelper.doExprTypeUpdate(m_resolver, this,
 					mtdInvocationExpr, type, m_groupInfo);
 			JstExpressionTypeLinkerHelper
 					.setPackageBindingForQualifier(mtdInvocationExpr
 							.getQualifyExpr()); // set the package binding for
 												// each qualifier, e.g. a.b.c
 			return type;
 		}
 		return null;
 	}
 
 	private LinkerSymbolInfo findTypeInSymbolMap(final String symbolName,
 			final List<VarTable> varTablesBottomUp) {
 		// lookup in local cache 1st (bugfix by huzhou@ebay.com, lookup only 1
 		// level)
 		final ScopeFrame frames[] = m_scopeStack
 				.toArray(new ScopeFrame[m_scopeStack.size()]);
 		if (frames.length > 0) {
 			final ScopeFrame frame = frames[frames.length - 1];
 			final LinkerSymbolInfo info = ((ScopeFrame) frame)
 					.getSymbolBinding(symbolName);
 			if (info != null) {
 				return info;
 			}
 		}
 
 		// go to var table to find the available symbol
 		if (varTablesBottomUp != null) {
 			for (VarTable varTable : varTablesBottomUp) {
 				final IJstNode var = varTable instanceof TopLevelVarTable ? ((TopLevelVarTable) varTable)
 						.getSelfVarNode(symbolName) : varTable
 						.getVarNode(symbolName);
 				if (var != null) {
 					final LinkerSymbolInfo lazy = new LinkerSymbolInfo(
 							symbolName, varTable.getVarType(symbolName), var,
 							varTable);
 					getCurrentScopeFrame().addSymbolBinding(symbolName, lazy);
 					return lazy;
 				}
 			}
 		}
 
 		for (int i = frames.length - 1; i >= 0; i--) {
 			final ScopeFrame frame = frames[i];
 			final LinkerSymbolInfo info = ((ScopeFrame) frame)
 					.getSymbolBinding(symbolName);
 			if (info != null) {
 				return info;
 			}
 		}
 
 		return null;
 	}
 
 	/**********************************************************
 	 * inner types helping the scope managements
 	 * ********************************************************
 	 */
 
 	/**
 	 * symbol struct modified by huzhou@ebay.com to shield off the difference
 	 * between 1. local var lookups (nested levels, modified to use VarTables)
 	 * 2. global var lookups
 	 */
 	static class LinkerSymbolInfo {
 		private final String m_name;
 		private final VarTable m_varTable;
 		private IJstType m_type;
 		private IJstNode m_binding;
 
 		public LinkerSymbolInfo(final String name, final IJstType type,
 				final IJstNode binding, final VarTable varTable) {
 			m_name = name;
 			m_type = type;
 			m_binding = binding;
 			m_varTable = varTable;
 		}
 
 		public IJstType getType() {
 			return m_type;
 		}
 
 		public void setType(final IJstType type) {
 			m_type = type;
 			if (m_varTable != null) {
 				m_varTable.addVarType(m_name, m_type);
 			}
 		}
 
 		public IJstNode getBinding() {
 			return m_binding;
 		}
 
 		public void setBinding(final IJstNode binding) {
 			m_binding = binding;
 			if (m_varTable != null) {
 				m_varTable.addVarNode(m_name, m_binding);
 			}
 		}
 
 		public VarTable getVarTable() {
 			return m_varTable;
 		}
 	}
 
 	/**
 	 * frame stack struct
 	 * 
 	 * @author huzhou
 	 * 
 	 */
 	static class ScopeFrame {
 		private IJstType m_currentType;
 		private boolean m_isStatic;
 		private HashMap<String, LinkerSymbolInfo> m_SymbolMap = new HashMap<String, LinkerSymbolInfo>();
 		private Stack<JstVar> m_catchVarStack = new Stack<JstVar>();
 		private String m_name;
 		private IJstNode m_node;
 
 		ScopeFrame(IJstType type, boolean isStatic) {
 			m_currentType = type;
 			m_isStatic = isStatic;
 			m_name = (type == null) ? null : type.getName();
 		}
 
 		ScopeFrame(IJstType type, boolean isStatic, IJstNode currentNode) {
 			m_currentType = type;
 			m_isStatic = isStatic;
 			m_name = (type == null) ? null : type.getName();
 			m_node = currentNode;
 		}
 
 		public IJstNode getNode() {
 			return m_node;
 		}
 
 		void addSymbolBinding(String symbolName, LinkerSymbolInfo nodeBinding) {
 			m_SymbolMap.put(symbolName, nodeBinding);
 		}
 
 		LinkerSymbolInfo getSymbolBinding(String symbolName) {
 			return m_SymbolMap.get(symbolName);
 		}
 
 		IJstType getCurrentType() {
 			return m_currentType;
 		}
 
 		boolean isStatic() {
 			return m_isStatic;
 		}
 
 		String getName() {
 			return m_name;
 		}
 
 		void pushCatchVar(JstVar var) {
 			m_catchVarStack.push(var);
 		}
 
 		void popCatchVar() {
 			if (!m_catchVarStack.isEmpty()) {
 				m_catchVarStack.pop();
 			}
 		}
 	}
 
 	static class HierarchyDepth {
 		public static final int BASE = 1;
 
 		public static final int THIS = 0;
 
 		private int startDepth = 0;
 
 		public HierarchyDepth(int startDepth) {
 			super();
 			this.startDepth = startDepth;
 		}
 
 		public void nextlevel() {
 			this.startDepth++;
 		}
 
 		public int getStartDepth() {
 			return startDepth;
 		}
 
 		public void previousLevel() {
 			this.startDepth--;
 		}
 	}
 
 	/**
 	 * scope search util
 	 * 
 	 * @author huzhou
 	 * 
 	 */
 	static class HierarcheQualifierSearcher {
 		public IJstType searchType(IJstType type, String methodName,
 				int inDepth, boolean isStatic) {
 			HierarchyDepth depth = new HierarchyDepth(inDepth);
 			return searchType(type, methodName, depth, isStatic);
 		}
 
 		public IJstType searchType(IJstType type, String methodName,
 				HierarchyDepth depth, boolean isStatic) {
 
 			if (type == null || methodName == null) {
 				return null;
 			}
 
 			if (depth.getStartDepth() == HierarchyDepth.BASE) {
 				type = type.getExtend();
 				// fixed bug!
 				// This block must be executed once per all recursive calls.
 				// Repetition of this block result in troubles.
 				// Decrease depth to 'THIS' value to refrain from repetition.
 				depth.previousLevel();
 			}
 
 			IJstType resultType = type;
 			IJstMethod jstMethod = null;
 			if (resultType != null) {
 				jstMethod = resultType.getMethod(methodName, isStatic);
 				if (jstMethod == null) {
 					jstMethod = JstExpressionTypeLinkerHelper.getConstructs(
 							resultType, methodName);
 				}
 			}
 
 			if (jstMethod == null && type != null) {
 				// gets extended type
 				IJstType extendType = type.getExtend();
 
 				if (extendType != null && extendType != type) {
 					resultType = searchType(extendType, methodName, depth,
 							isStatic);
 					if (resultType != null)
 						return resultType;
 				}
 
 				// gets mixed types
 				List<? extends IJstTypeReference> mixinTypes = type
 						.getMixinsRef();
 				for (IJstTypeReference mixinType : mixinTypes) {
 					if (mixinType.getReferencedType() == type)
 						continue;
 
 					resultType = searchType(mixinType.getReferencedType(),
 							methodName, depth, isStatic);
 					if (resultType != null)
 						return resultType;
 				}
 
 				List<? extends IJstTypeReference> expectTypes = type
 						.getExpectsRef();
 
 				// gets expect types
 				for (IJstTypeReference expectType : expectTypes) {
 					if (expectType.getReferencedType() == type)
 						continue;
 
 					resultType = searchType(expectType.getReferencedType(),
 							methodName, depth, isStatic);
 					if (resultType != null)
 						return resultType;
 				}
 			}
 			return JstExpressionTypeLinkerHelper.getTargetJstType(resultType);
 		}
 
 		public IJstType searchReturnType(IJstType type, String methodName) {
 			IJstType resultType = null;
 			IJstMethod jstMethod = type.getMethod(methodName);
 
 			if (jstMethod == null) {
 
 				// gets extended type
 				IJstType jstType = type.getExtend();
 				if (jstType != null && jstType != type) {
 					resultType = searchReturnType(jstType, methodName);
 					if (resultType != null)
 						return resultType;
 				}
 
 				// gets mixed types
 				List<? extends IJstTypeReference> mixinTypes = type
 						.getMixinsRef();
 				for (IJstTypeReference mixinType : mixinTypes) {
 
 					if (mixinType.getReferencedType() == type)
 						continue;
 
 					resultType = searchReturnType(
 							mixinType.getReferencedType(), methodName);
 					if (resultType != null)
 						return resultType;
 				}
 				// gets static mixed types
 				// Note: mixinProps no longer supported
 				/*
 				 * List<? extends IJstTypeReference> staticMixinTypes = type
 				 * .getStaticMixins(); for (IJstTypeReference staticMixinType :
 				 * staticMixinTypes) { resultType =
 				 * searchReturnType(staticMixinType .getReferencedType(),
 				 * methodName); if (resultType != null) return (JstType)
 				 * resultType; }
 				 */
 
 			} else {
 				resultType = jstMethod.getRtnType();
 			}
 			return JstExpressionTypeLinkerHelper.getTargetJstType(resultType);
 		}
 
 		public IJstType searchPropertyType(IJstType type, String propertyName,
 				int inDepth, boolean isStatic) {
 			if (type == null || propertyName == null) {
 				return null;
 			}
 			HierarchyDepth depth = new HierarchyDepth(inDepth);
 			return searchPropertyType(type, propertyName, depth, isStatic);
 		}
 
 		public IJstType searchPropertyType(IJstType type, String propertyName,
 				HierarchyDepth depth, boolean isStatic) {
 
 			if (depth.getStartDepth() == HierarchyDepth.BASE) {
 				type = type.getExtend();
 
 				// fixed bug!
 				// This block must be executed once per all recursive calls.
 				// Repetition of this block result in troubles.
 				// Decrease depth to 'THIS' value to refrain from repetition.
 				depth.previousLevel();
 
 			}
 			if (type == null || propertyName == null) {
 				return null;
 			}
 
 			IJstType resultType = type;
 			IJstProperty jstProperty = null;
 
 			if (resultType != null) {
 				jstProperty = type.getProperty(propertyName, isStatic);
 			}
 
 			if (jstProperty == null) {
 				// gets extended type
 				IJstType jstType = type.getExtend();
 
 				if (jstType != null && jstType != type) {
 					resultType = searchPropertyType(jstType, propertyName,
 							depth, isStatic); // RECURSIVE
 					if (resultType != null)
 						return resultType;
 				}
 
 				// gets mixed types
 				List<? extends IJstTypeReference> mixinTypes = type
 						.getMixinsRef();
 				for (IJstTypeReference mixinType : mixinTypes) {
 					if (mixinType.getReferencedType() == type)
 						continue;
 
 					resultType = searchPropertyType(
 							mixinType.getReferencedType(), propertyName, depth,
 							isStatic); // RECURSIVE
 					if (resultType != null)
 						return resultType;
 				}
 
 				List<? extends IJstTypeReference> expectTypes = type
 						.getExpectsRef();
 				// gets expect types
 				for (IJstTypeReference expectType : expectTypes) {
 
 					if (expectType.getReferencedType() == type)
 						continue;
 
 					resultType = searchPropertyType(
 							expectType.getReferencedType(), propertyName,
 							depth, isStatic);
 					if (resultType != null)
 						return resultType;
 				}
 			}
 			return JstExpressionTypeLinkerHelper.getTargetJstType(resultType);
 		}
 	}
 }
