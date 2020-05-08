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
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.util.TypeCheckUtil;
 import org.eclipse.vjet.dsf.jsnative.global.PrimitiveBoolean;
 import org.eclipse.vjet.dsf.jst.IInferred;
 import org.eclipse.vjet.dsf.jst.IJstDoc;
 import org.eclipse.vjet.dsf.jst.IJstGlobalFunc;
 import org.eclipse.vjet.dsf.jst.IJstGlobalProp;
 import org.eclipse.vjet.dsf.jst.IJstGlobalVar;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstOType;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstResultTypeModifier;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstArray;
 import org.eclipse.vjet.dsf.jst.declaration.JstAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstConstructor;
 import org.eclipse.vjet.dsf.jst.declaration.JstDeferredType;
 import org.eclipse.vjet.dsf.jst.declaration.JstExtendedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncArgAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncScopeAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstInferredRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstInferredType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstMixedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstModifiers;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.declaration.JstParamType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPotentialAttributedMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstPotentialOtypeMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyType;
 import org.eclipse.vjet.dsf.jst.declaration.JstSynthesizedMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeWithArgs;
 import org.eclipse.vjet.dsf.jst.declaration.JstVar;
 import org.eclipse.vjet.dsf.jst.declaration.JstVariantType;
 import org.eclipse.vjet.dsf.jst.declaration.JstVars;
 import org.eclipse.vjet.dsf.jst.declaration.JstWildcardType;
 import org.eclipse.vjet.dsf.jst.declaration.SynthJstProxyMethod;
 import org.eclipse.vjet.dsf.jst.declaration.SynthOlType;
 import org.eclipse.vjet.dsf.jst.declaration.TopLevelVarTable;
 import org.eclipse.vjet.dsf.jst.declaration.VarTable;
 import org.eclipse.vjet.dsf.jst.expr.ArrayAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.AssignExpr;
 import org.eclipse.vjet.dsf.jst.expr.ConditionalExpr;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.FuncExpr;
 import org.eclipse.vjet.dsf.jst.expr.JstArrayInitializer;
 import org.eclipse.vjet.dsf.jst.expr.JstInitializer;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.expr.ObjCreationExpr;
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta;
 import org.eclipse.vjet.dsf.jst.meta.JsCommentMetaNode;
 import org.eclipse.vjet.dsf.jst.meta.JsType;
 import org.eclipse.vjet.dsf.jst.meta.JsTypingMeta;
 import org.eclipse.vjet.dsf.jst.stmt.BlockStmt;
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
 import org.eclipse.vjet.dsf.jst.traversal.IJstNodeVisitor;
 import org.eclipse.vjet.dsf.jst.traversal.IJstVisitor;
 import org.eclipse.vjet.dsf.jst.ts.JstQueryExecutor;
 import org.eclipse.vjet.dsf.jst.ts.JstTypeSpaceMgr;
 import org.eclipse.vjet.dsf.jst.util.JstTypeHelper;
 import org.eclipse.vjet.dsf.jstojava.controller.JstExpressionTypeLinker.LinkerSymbolInfo;
 import org.eclipse.vjet.dsf.jstojava.controller.JstExpressionTypeLinker.ScopeFrame;
 import org.eclipse.vjet.dsf.jstojava.mixer.TypeExtensionRegistry;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsVariantType;
 import org.eclipse.vjet.dsf.jstojava.resolver.FunctionMetaRegistry;
 import org.eclipse.vjet.dsf.jstojava.resolver.FunctionParamsMetaRegistry;
 import org.eclipse.vjet.dsf.jstojava.resolver.IMetaExtension;
 import org.eclipse.vjet.dsf.jstojava.resolver.OTypeResolverRegistry;
 import org.eclipse.vjet.dsf.jstojava.resolver.TypeResolverRegistry;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper.RenameableSynthJstProxyMethod;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper.RenameableSynthJstProxyProp;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst.FunctionExpressionTranslator;
 import org.eclipse.vjet.dsf.ts.ITypeSpace;
 import org.eclipse.vjet.dsf.ts.group.IGroup;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 import org.eclipse.vjet.vjo.meta.VjoKeywords;
 
 
 public class JstExpressionTypeLinkerHelper {
 
 	/**********************************************************************
 	 * HELPERS FOR: bind attributed types, jsttypewithargs types
 	 * ********************************************************************
 	 */
 
 	/**
 	 * bind attributed type is tricky as attributed type could be nested in
 	 * function type, array type, generics type etc.
 	 * 
 	 * the method here navigates through those variety of types and locate the
 	 * attributed types to bind
 	 * 
 	 * @see JstExpressionTypeLinkerHelper#doAttributedTypeBindings(JstExpressionBindingResolver,
 	 *      JstAttributedType) for the actual binding work
 	 * 
 	 * @param type
 	 * @return the attributed type bound if and only if it's in 1st level of
 	 *         recursion
 	 */
 	public static IJstNode look4ActualBinding(
 			final JstExpressionBindingResolver resolver, final IJstType type) {
 		return look4ActualBinding(resolver, type, null);
 	}
 
 	public static IJstNode look4ActualBinding(
 			final JstExpressionBindingResolver resolver, final IJstType type,
 			final GroupInfo groupInfo) {
 		if (type == null) {
 			return null;
 		} else if (type instanceof JstArray) {
 			look4ActualBinding(resolver, ((JstArray) type).getComponentType(),
 					groupInfo);
 			return null;
 		} else if (type instanceof JstTypeWithArgs) {
 			for (IJstType argType : ((JstTypeWithArgs) type).getArgTypes()) {
 				look4ActualBinding(resolver, argType);
 			}
 			return null;
 		}
 		/*
 		 * bugfix by huzhou, JstWildcardType#boundType could hide attributed
 		 * type as well
 		 */
 		else if (type instanceof JstWildcardType) {
 			look4ActualBinding(resolver, ((JstWildcardType) type).getType(),
 					groupInfo);
 			return null;
 		} else if (type instanceof JstFuncType) {
 			final IJstMethod func = ((JstFuncType) type).getFunction();
 			bindAttributedType(resolver, func, groupInfo);
 			return null;// bugfix by huzhou, defer the actual binding till
 						// mtdinvocationvisit
 		} else if (type instanceof JstAttributedType) {
 			return doAttributedTypeBindings(resolver, (JstAttributedType) type,
 					groupInfo);
 		}
 		// by huzhou@ebay.com, enhancement for inferred type to go on bindings
 		else if (type instanceof JstInferredType) {
 			return look4ActualBinding(resolver,
 					((JstInferredType) type).getType(), groupInfo);
 		}
 		// by huzhou@ebay.com, enhancement for ftype binding
 		else if (type.isFType()) {
 			return type;// getFTypeInvokeMethod(resolver, type);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * bind JstParamType with actual resolved type based on the following
 	 * possibilities
 	 * <ol>
 	 * <li>if in a method invocation, and method has param types, match param
 	 * type with arguments (return type is yet supported)</li>
 	 * <li>if in a method invocation, and invocation has a qualifier type, with
 	 * arg types, try matching with arg type</li>
 	 * <li>if type is complex type, as {JstArray, JstTypeWithArgs, JstFuncType,
 	 * JstAttributedType, JstInferredType, JstWildcardType, FType} the bind
 	 * attempt will be done recursively</li>
 	 * <li>if eventually, no type could be resolved, JstInferredType will be
 	 * used as an indication that binding wasn't successful</li>
 	 * </ol>
 	 * 
 	 * @param resolver
 	 * @param mtd
 	 * @param invocation
 	 * @param qualifierType
 	 * @param type
 	 * @return <pre>
 	 * Be noted that, all the resolved types, in all the recursive cases were replicated instead of modifying the original type
 	 * including the function if involved, this is to keep clean of the type system while allowing the maximum info of binding for validation and proposal
 	 * </pre>
 	 */
 	protected static IJstType bindParamTypes(
 			final JstExpressionBindingResolver resolver, final IJstNode mtd,
 			final MtdInvocationExpr invocation, final IJstType qualifierType,
 			final IJstType type) {
 		if (type == null) {
 			return null;
 		} else if (type instanceof JstParamType) { // matching condition
 			// type for
 			// parameterized
 			// type
 			return resolveParamType(resolver, (JstParamType) type, mtd,
 					invocation, qualifierType);
 		} else if (type instanceof JstArray) { // further looking
 			return new JstArray(bindParamTypes(resolver, mtd, invocation,
 					qualifierType, ((JstArray) type).getComponentType()));
 		} else if (type instanceof JstTypeWithArgs) {
 			final JstTypeWithArgs typeWithArgs = (JstTypeWithArgs) type;
 			final IJstType rtnType = bindParamTypes(resolver, mtd, invocation,
 					qualifierType, typeWithArgs.getType());
 			final JstTypeWithArgs resolvedTypeWithArgs = new JstTypeWithArgs(
 					rtnType);
 			for (IJstType argType : typeWithArgs.getArgTypes()) {
 				resolvedTypeWithArgs.addArgType(bindParamTypes(resolver, mtd,
 						invocation, qualifierType, argType));
 			}
 			return resolvedTypeWithArgs;
 		} else if (type instanceof JstFuncType) {
 			final JstFuncType funcType = (JstFuncType) type;
 			final IJstMethod function = funcType.getFunction();
 			if (function != null) {
 				OverwritableSynthJstProxyMethod resolvedFunction = resolveParamFunction(
 						resolver, mtd, invocation, qualifierType, function);
 				return new JstFuncType(resolvedFunction);
 			}
 		} else if (type instanceof JstAttributedType) {
 			final JstAttributedType attributedType = (JstAttributedType) type;
 			final IJstType resolvedAttributorType = bindParamTypes(resolver,
 					mtd, invocation, qualifierType,
 					attributedType.getAttributorType());
 			return new JstAttributedType(resolvedAttributorType,
 					attributedType.getAttributeName(),
 					attributedType.isStaticAttribute());
 		} else if (type instanceof JstWildcardType) {
 			final JstWildcardType wildcardType = (JstWildcardType) type;
 			final IJstType resolvedBoundType = bindParamTypes(resolver, mtd,
 					invocation, qualifierType, wildcardType.getType());
 			return new JstWildcardType(resolvedBoundType,
 					wildcardType.isUpperBound());
 		} else if (type instanceof JstInferredType) {
 			final JstInferredType inferredType = (JstInferredType) type;
 			final IJstType resolvedInferredType = bindParamTypes(resolver, mtd,
 					invocation, qualifierType, inferredType.getType());
 			return new JstInferredType(resolvedInferredType);
 		} else if (type.isFType()) {
 			final IJstMethod invoke = getFTypeInvokeMethod(resolver, type);
 			if (invoke != null) {
 				OverwritableSynthJstProxyMethod resolvedFunction = resolveParamFunction(
 						resolver, mtd, invocation, qualifierType, invoke);
 				return new OverwritableFType(type, resolvedFunction);
 			}
 		}
 		return type;
 	}
 
 	/**
 	 * replicate a function with its overloading functions if any resolve all of
 	 * its parameter types, return types based on the context
 	 * 
 	 * @param resolver
 	 * @param mtd
 	 * @param invocation
 	 * @param qualifierType
 	 * @param function
 	 * @return
 	 */
 	private static OverwritableSynthJstProxyMethod resolveParamFunction(
 			final JstExpressionBindingResolver resolver, final IJstNode mtd,
 			final MtdInvocationExpr invocation, final IJstType qualifierType,
 			final IJstMethod function) {
 
 		if (function == null) {
 			throw new IllegalArgumentException(
 					"proxy function could not be null");
 		}
 
 		if (!function.isDispatcher()) {
 			final IJstType resolvedRtnType = bindParamTypes(resolver, mtd,
 					invocation, qualifierType, function.getRtnType());
 			final List<JstArg> resolvedJstArgs = new ArrayList<JstArg>(function
 					.getArgs().size());
 			for (JstArg arg : function.getArgs()) {
 				final JstArg resolvedArg = new JstArg(arg.getTypes(),
 						arg.getName(), arg.isVariable(), arg.isOptional(),
 						arg.isFinal());
 				for (IJstType argType : arg.getTypes()) {
 					resolvedArg.updateType(
 							arg.getName(),
 							bindParamTypes(resolver, mtd, invocation,
 									qualifierType, argType));
 				}
 				resolvedJstArgs.add(resolvedArg);
 			}
 
 			final OverwritableSynthJstProxyMethod resolvedFunction = new OverwritableSynthJstProxyMethod(
 					function);
 			resolvedFunction.setRtnType(resolvedRtnType);
 			resolvedFunction.setArgs(resolvedJstArgs);
 			return resolvedFunction;
 		} else {
 			OverwritableSynthJstProxyMethod host = null;
 			for (IJstMethod overload : function.getOverloaded()) {
 				final OverwritableSynthJstProxyMethod resolvedOverload = resolveParamFunction(
 						resolver, mtd, invocation, qualifierType, overload);
 				if (host == null) {
 					host = resolvedOverload;
 				}
 				host.addOverloaded(resolvedOverload);
 			}
 			return host;
 		}
 	}
 
 	private static IJstType resolveParamType(
 			final JstExpressionBindingResolver resolver,
 			JstParamType paramType, IJstNode node,
 			MtdInvocationExpr invocation, IJstType qualifierType) {
 		if (paramType == null) {
 			throw new IllegalArgumentException(
 					"param type to be resolved should not be null");
 		}
 		if (node instanceof IJstMethod) {
 			final IJstMethod mtd = (IJstMethod) node;
 			final List<JstParamType> methodParamTypes = mtd.getParamTypes();
 			for (JstParamType methodParamType : methodParamTypes) {
 				if (paramType.equals(methodParamType)) {
 					// look for param type matching in the invocation
 					for (int i = 0, len = mtd.getArgs().size(); i < len; i++) {
 						final JstArg param = mtd.getArgs().get(i);
 						if (paramType.equals(param.getType())) {
 							final List<IExpr> args = invocation.getArgs();
 							if (args.size() > i) {
 								return args.get(i).getResultType();
 							}
 						}
 					}
 					// TODO unify this logic with Expect type concept
 				}
 			}
 		}
 
 		if (qualifierType instanceof JstTypeWithArgs) {
 			final JstTypeWithArgs jstTypeWithArgs = (JstTypeWithArgs) qualifierType;
 			final IJstType resolved = jstTypeWithArgs
 					.getParamArgType(paramType);
 			if (resolved != null) {
 				return resolved;
 			}
 		} else {// qualifierType missing args, using Object type implicitly
 			final IJstType objectType = getNativeTypeFromTS(resolver, "Object");
 			return new JstInferredType(objectType);
 		}
 		return paramType;
 	}
 
 	/**
 	 * AttributedType feature helpers
 	 * 
 	 * @param resolver
 	 * @param method
 	 * @return
 	 */
 	protected static IJstNode bindAttributedType(
 			final JstExpressionBindingResolver resolver,
 			final IJstMethod method, GroupInfo groupInfo) {
 		if (method != null) {
 			look4ActualBinding(resolver, method.getRtnType(), groupInfo);
 			for (JstArg arg : method.getArgs()) {
 				for (IJstType argType : arg.getTypes()) {
 					look4ActualBinding(resolver, argType, groupInfo);
 				}
 			}
 			return method;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * do the binding of {@link JstAttributedType}
 	 * 
 	 * @param attributedType
 	 * @return
 	 */
 	protected static IJstNode doAttributedTypeBindings(
 			final JstExpressionBindingResolver resolver,
 			final JstAttributedType attributedType, final GroupInfo groupInfo) {
 		IJstNode bound = null;
 		IJstType attributorType = attributedType.getType();
 
 		final String attributeName = attributedType.getAttributeName();
 		final boolean staticAttribute = attributedType.isStaticAttribute();
 
 		if (attributorType != null && attributeName != null) {
 			// by huzhou@ebay.com, adding the logic of dealing with otype here
 			if (attributorType.isOType()) {
 				final IJstType objLiteralOrFunctionRefType = attributorType
 						.getOType(attributeName);
 				if (objLiteralOrFunctionRefType != null) {
 					bound = objLiteralOrFunctionRefType;
 				}
 			} else {
 				// NOTE by huzhou@ebay.com, discussed with Mr.P
 				// attributed type reference here doesn't need to check
 				// static/none-static reference
 				// as only the attribute's type was concerned, not how it was
 				// supposed to be scoped
 				IJstProperty pty = attributorType.getProperty(attributeName,
 						staticAttribute);
 				pty = pty == null ? attributorType.getProperty(attributeName,
 						!staticAttribute) : pty;
 				if (pty != null) {
 					bound = new RenameableSynthJstProxyProp(pty, null);
 				} else {
 					IJstMethod mtd = attributorType.getMethod(attributeName,
 							staticAttribute);
 					mtd = mtd == null ? attributorType.getMethod(attributeName,
 							!staticAttribute) : mtd;
 					if (mtd != null) {
 						bound = new RenameableSynthJstProxyMethod(mtd, mtd.getName());
 
 						look4ActualBinding(resolver, mtd.getRtnType(),
 								groupInfo);
 						for (JstArg arg : mtd.getArgs()) {
 							for (IJstType argType : arg.getTypes()) {
 								look4ActualBinding(resolver, argType, groupInfo);
 							}
 						}
 					}
 					// handle global attributor case, where property/method
 					// requires
 					// extra logic for lookups
 					else if ("Global".equals(attributorType.getSimpleName())) {
 						// look under WINDOW & GLOBAL
 						bound = getFromGlobalVarName(resolver, attributeName,
 								true, groupInfo);
 						if (bound == null) {
 							// look up in TS
 							bound = getFromGlobalTypeName(resolver,
 									attributeName, groupInfo);
 							if (bound != null) {
 								if (bound instanceof IJstGlobalFunc) {
 									bound = getFurtherGlobalVarBinding(
 											resolver, (IJstGlobalFunc) bound,
 											groupInfo);
 								} else if (bound instanceof IJstGlobalProp) {
 									bound = getFurtherGlobalVarBinding(
 											resolver, (IJstGlobalProp) bound,
 											groupInfo);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		// set the bound as jst binding of attributed type eventually
 		if (bound != null) {
 			attributedType.setJstBinding(bound);
 		}
 
 		return bound;
 	}
 
 	public static IJstType getResolvedAttributedType(
 			final JstExpressionBindingResolver resolver,
 			final JstIdentifier identifier, final JstAttributedType type,
 			final IJstNode bound) {
 		IJstType attributedType = type;
 		if (bound instanceof IJstProperty) {
 			attributedType = ((IJstProperty) bound).getType();
 		} else if (bound instanceof IJstMethod) {
 			attributedType = new JstFuncType((IJstMethod) bound);
 		}
 		return attributedType;
 	}
 
 	/**********************************************************************
 	 * HELPERS FOR: global var, local var lookups
 	 * ********************************************************************
 	 */
 	// bugfix by roy, method/property under global/window must be taken into
 	// account
 	public static IJstNode getFromGlobalVarName(
 			final JstExpressionBindingResolver resolver, final String name,
 			final boolean overwiteBindings, GroupInfo groupInfo) {
 		// handle native or global name directly without qualifier,
 		// in this case it's the type name itself, e.g Number
 		IJstNode bound = findIdentifierBinding(
 				getNativeTypeFromTS(resolver,
 						groupInfo != null ? groupInfo.getGroupName() : null,
 						JstExpressionTypeLinker.WINDOW), name);
 
 		if (bound != null) {
 			return bound;
 		} else {
 			bound = findIdentifierBinding(
 					getNativeTypeFromTS(resolver,
 							JstExpressionTypeLinker.GLOBAL), name);
 		}
 
 		return bound;
 	}
 
 	// bugfix by roy, method/property introduced by with statement must be taken
 	// into account
 	public static boolean getFromWithVarName(
 			final JstExpressionBindingResolver resolver,
 			final ScopeFrame scope, final JstIdentifier identifier,
 			final GroupInfo groupInfo) {
 		// handle local variable came through with statement
 		IJstType nodeType = null;
 		// lookup the closest with (nested with not to support at the moment)
 		// stop at the 1st method (nested function not to support at the moment)
 
 		IJstNode it = identifier.getParentNode();
 		while (it != null && !(it instanceof WithStmt)) {
 			it = it.getParentNode();
 		}
 
 		if (it != null && it instanceof WithStmt) {
 			final WithStmt withStmt = (WithStmt) it;
 			final IExpr withExpr = withStmt.getCondition().getLeft();
 			nodeType = withExpr.getResultType();
 
 			if (mapVarToTypeMember(resolver, scope, nodeType, identifier,
 					identifier.getName(), false, groupInfo)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	// overload added by huzhou to handle attributed type case in which
 	// attribute name and identifier are seperated
 	public static boolean getFromGlobalTypeName(
 			final JstExpressionBindingResolver resolver,
 			final ScopeFrame scope, final JstIdentifier identifier,
 			GroupInfo groupInfo) {
 		final IJstNode bound = getFromGlobalTypeName(resolver,
 				identifier.getName(), groupInfo);
 		if (bound != null) {
 			final List<IJstType> symbolTypes = collectBindingTypes(bound);
 			for (int i = 0, len = symbolTypes.size(); i < len; i++) {
 				// from the symbol types, only the 1st one could update the
 				// bound
 				look4ActualBinding(resolver, symbolTypes.get(i), groupInfo);
 			}
 			if (symbolTypes.size() > 0) {
 				bindIdentifier(resolver, scope, identifier,
 						identifier.getName(), bound, symbolTypes.get(0), true,
 						groupInfo);
 			} else {
 				return false;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public static IJstNode getFromGlobalTypeName(
 			final JstExpressionBindingResolver resolver, final String name,
 			final GroupInfo groupInfo) {
 		// handle native or global name directly without qualifier,
 		// in this case it's the type name itself, e.g Number
 		IJstNode bound; // getNativeTypeFromTS(resolver, groupName, name);
 		// if (bound != null) {
 		// return bound instanceof IJstType && !(bound instanceof IJstRefType) ?
 		// JstTypeHelper.getJstTypeRefType((IJstType)bound) : bound;
 		// }
 
 		// final JstTypeSpaceMgr tsMgr =
 		// resolver.getController().getJstTypeSpaceMgr();
 		// final JstQueryExecutor queryExecutor = tsMgr.getQueryExecutor();
 		// bound =
 		// tsMgr.getTypeSpace().getVisibleGlobal(name,tsMgr.getTypeSpace().getGroup(groupName)
 		// );
 		// if (bound != null) {
 		// return bound instanceof IJstType && !(bound instanceof IJstRefType) ?
 		// JstTypeHelper.getJstTypeRefType((IJstType)bound) : bound;
 		// }
 
 		ITypeSpace<IJstType, IJstNode> typeSpace = resolver.getController()
 				.getJstTypeSpaceMgr().getTypeSpace();
 		IGroup<IJstType> currentGroup = typeSpace
 				.getGroup(groupInfo != null ? groupInfo.getGroupName() : null);
 		bound = typeSpace.getVisibleGlobal(name, currentGroup);
 		bound = findGlobalVarBinding(resolver, bound, groupInfo);
 
 		if (bound == null && name.indexOf('.') == -1) {
 			List<IJstType> types = resolver.getController()
 					.getJstTypeSpaceMgr().getTypeSpace()
 					.getVisibleType(name, currentGroup);
 			if (types != null && types.size() == 1) {
 				bound = types.get(0);
 			}
 		}
 		bound = bound instanceof IJstType && !(bound instanceof IJstRefType)
 				&& !((IJstType) bound).isSingleton() ? JstTypeHelper
 				.getJstTypeRefType((IJstType) bound) : bound;
 
 		return bound;
 	}
 
 	public static IJstNode getFromGlobalTypeName2(
 			final JstExpressionBindingResolver resolver, final String name,
 			final GroupInfo groupInfo) {
 
 		// handle native or global name directly without qualifier,
 		// in this case it's the type name itself, e.g Number
 		IJstNode bound; // getNativeTypeFromTS(resolver, groupName, name);
 		// if (bound != null) {
 		// return bound instanceof IJstType && !(bound instanceof IJstRefType) ?
 		// JstTypeHelper.getJstTypeRefType((IJstType)bound) : bound;
 		// }
 
 		// final JstTypeSpaceMgr tsMgr =
 		// resolver.getController().getJstTypeSpaceMgr();
 		// final JstQueryExecutor queryExecutor = tsMgr.getQueryExecutor();
 		// bound =
 		// tsMgr.getTypeSpace().getVisibleGlobal(name,tsMgr.getTypeSpace().getGroup(groupName)
 		// );
 		// if (bound != null) {
 		// return bound instanceof IJstType && !(bound instanceof IJstRefType) ?
 		// JstTypeHelper.getJstTypeRefType((IJstType)bound) : bound;
 		// }
 
 		ITypeSpace<IJstType, IJstNode> typeSpace = resolver.getController()
 				.getJstTypeSpaceMgr().getTypeSpace();
 
 		bound = typeSpace.getVisibleGlobal(name,
 				typeSpace.getGroup(groupInfo.getGroupName()));
 		bound = findGlobalVarBinding(resolver, bound, groupInfo);
 		if (bound != null) {
 			bound = bound instanceof IJstType
 					&& !(bound instanceof IJstRefType) ? JstTypeHelper
 					.getJstTypeRefType((IJstType) bound) : bound;
 		}
 
 		if (bound != null && name.indexOf('.') == -1) {
 			List<IJstType> visibleType = resolver
 					.getController()
 					.getJstTypeSpaceMgr()
 					.getTypeSpace()
 					.getVisibleType(name,
 							typeSpace.getGroup(groupInfo.getGroupName()));
 			if (visibleType.size() == 1) {
 				bound = JstTypeHelper.getJstTypeRefType((visibleType.get(0)));
 				if (bound != null) {
 					bound = bound instanceof IJstType
 							&& !(bound instanceof IJstRefType) ? JstTypeHelper
 							.getJstTypeRefType((IJstType) bound) : bound;
 				}
 
 			}
 		}
 		return bound;
 	}
 
 	public static List<IJstType> collectBindingTypes(IJstNode bound) {
 		final List<IJstType> toBindTypes = new ArrayList<IJstType>(2);
 		if (bound instanceof IJstType) {
 			if (bound instanceof IJstRefType
 					|| ((IJstType) bound).isSingleton()) {
 				toBindTypes.add((IJstType) bound);
 			} else {
 				toBindTypes.add(JstTypeHelper
 						.getJstTypeRefType((IJstType) bound));
 			}
 		} else if (bound instanceof IJstProperty) {
 			toBindTypes.add(((IJstProperty) bound).getType());
 		} else if (bound instanceof IJstMethod) {
 			toBindTypes.add(((IJstMethod) bound).getRtnType());
 			for (JstArg arg : ((IJstMethod) bound).getArgs()) {
 				for (IJstType argType : arg.getTypes()) {
 					toBindTypes.add(argType);
 				}
 			}
 		} else if (bound instanceof IExpr) {
 			toBindTypes.add(((IExpr) bound).getResultType());
 		}
 
 		return toBindTypes;
 	}
 
 	public static IJstNode findGlobalVarBinding(
 			final JstExpressionBindingResolver resolver, final IJstNode node,
 			final GroupInfo groupInfo) {
 
 		if (node instanceof IJstGlobalVar) {
 			IJstGlobalVar jstGlobalVar = ((IJstGlobalVar) node);
 			if (jstGlobalVar.isFunc()) {
 				final IJstMethod method = jstGlobalVar.getFunction();
 				bindAttributedType(resolver, method, groupInfo);
 				return method;
 
 			} else {
 				final IJstGlobalProp property = jstGlobalVar.getProperty();
 				look4ActualBinding(resolver, property.getType(), groupInfo);
 				return property;
 			}
 		}
 		return node;
 	}
 
 	public static String getGlobalVarNameFromBinding(
 			final IJstNode qualifierBinding) {
 		String globalVarName = null;
 		if (qualifierBinding instanceof IJstGlobalFunc) {
 			globalVarName = ((IJstGlobalFunc) qualifierBinding).getName()
 					.getName();
 		} else if (qualifierBinding instanceof IJstGlobalProp) {
 			globalVarName = ((IJstGlobalProp) qualifierBinding).getName()
 					.getName();
 		}
 		return globalVarName;
 	}
 
 	public static boolean isGlobalVarExtended(
 			final JstExpressionBindingResolver resolver,
 			final String globalVarName) {
 		return resolver.getController().getJstTypeSpaceMgr().getQueryExecutor()
 				.hasGlobalExtension(globalVarName);
 	}
 
 	public static IJstGlobalVar getGlobalVarExtensionByName(
 			final JstExpressionBindingResolver resolver, final String extName,
 			final String globalVarName) {
 		final List<IJstNode> extensions = resolver.getController()
 				.getJstTypeSpaceMgr().getQueryExecutor()
 				.getGlobalExtensions(globalVarName);
 		if (extensions != null) {
 			for (IJstNode ext : extensions) {
 				if (ext instanceof IJstGlobalVar) {
 					final IJstGlobalVar extVar = (IJstGlobalVar) ext;
 					if (extName.equals(extVar.getName().getName())) {
 						return extVar;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public static IJstNode look4ActualGlobalVarBinding(
 			final JstExpressionBindingResolver resolver,
 			final IJstGlobalVar extVar, GroupInfo groupInfo) {
 		IJstNode mtdBinding;
 		if (extVar.isFunc()) {
 			final IJstGlobalFunc extFunc = extVar.getFunction();
 			mtdBinding = JstExpressionTypeLinkerHelper
 					.getFurtherGlobalVarBinding(resolver, extFunc, groupInfo);
 		} else {
 			final IJstGlobalProp extProp = extVar.getProperty();
 			mtdBinding = JstExpressionTypeLinkerHelper
 					.getFurtherGlobalVarBinding(resolver, extProp, groupInfo);
 		}
 		return mtdBinding;
 	}
 
 	public static void doMethodBinding(
 			final JstExpressionBindingResolver resolver,
 			final MtdInvocationExpr mie, final JstIdentifier methodId,
 			final IJstNode mtdBinding) {
 		if (mtdBinding != null) {
 			if (mtdBinding instanceof IJstMethod) {
 				methodId.setJstBinding(mtdBinding);
 				mie.setResultType(((IJstMethod) mtdBinding).getRtnType());
 			} else if (mtdBinding instanceof IJstType
 					&& ((IJstType) mtdBinding).isFType()) {
 				final IJstMethod _invoke_ = JstExpressionTypeLinkerHelper
 						.getFTypeInvokeMethod(resolver, (IJstType) mtdBinding);
 				methodId.setJstBinding(_invoke_);
 				mie.setResultType(_invoke_.getRtnType());
 			}
 		}
 	}
 
 	public static IJstNode getFurtherGlobalVarBinding(
 			final JstExpressionBindingResolver resolver,
 			final IJstGlobalFunc method, final GroupInfo groupInfo) {
 		return bindAttributedType(resolver, method, groupInfo);
 	}
 
 	public static IJstNode getFurtherGlobalVarBinding(
 			final JstExpressionBindingResolver resolver,
 			final IJstGlobalProp property, final GroupInfo groupInfo) {
 		final IJstNode furtherBinding = look4ActualBinding(resolver,
 				property.getType(), groupInfo);
 		return furtherBinding != null ? furtherBinding : property;
 	}
 
 	public static IJstNode findIdentifierBinding(final IJstType nodeType,
 			final String name) {
 
 		if (nodeType != null) {
 			final IJstProperty namedProperty = nodeType.getProperty(name,
 					nodeType instanceof IJstRefType, true);
 
 			if (namedProperty != null) {
 				return namedProperty;
 			} else {
 				final IJstMethod namedMethod = nodeType.getMethod(name,
 						nodeType instanceof IJstRefType, true);
 				return namedMethod;
 			}
 		}
 		return null;
 	}
 
 	public static boolean mapVarToTypeMember(
 			final JstExpressionBindingResolver resolver,
 			final ScopeFrame scope, final IJstType nodeType,
 			final JstIdentifier identifier, final String name,
 			final boolean overwriteBindings, final GroupInfo groupInfo) {
 		boolean found = false;
 		if (nodeType != null) {
 			final IJstNode bound = findIdentifierBinding(nodeType, name);
 			if (bound != null) {
 				final List<IJstType> symbolTypes = collectBindingTypes(bound);
 				bindIdentifier(resolver, scope, identifier, name, bound,
 						symbolTypes.get(0), false, groupInfo);
 				found = true;
 			}
 		}
 
 		return found;
 	}
 
 	// overload added by huzhou to support attributed type case in which name
 	// and identifier are separated
 	public static boolean getFromGlobalVarName(
 			final JstExpressionBindingResolver resolver,
 			final ScopeFrame scope, final JstIdentifier identifier,
 			GroupInfo groupInfo) {
 		final String name = identifier.getName();
 		final IJstNode bound = getFromGlobalVarName(resolver, name, false,
 				groupInfo);
 
 		if (bound != null) {
 			final List<IJstType> symbolTypes = collectBindingTypes(bound);
 			// bindAttributedType(symbolType);
 			bindIdentifier(resolver, scope, identifier, name, bound,
 					symbolTypes.get(0), false, groupInfo);
 			return true;
 		}
 		return false;
 	}
 
 	public static IJstMethod getFTypeInvokeMethod(
 			final JstExpressionBindingResolver resolver, final IJstType ftype) {
 		if (ftype == null || !ftype.isFType()) {
 			throw new IllegalArgumentException(
 					"the vjo type in this context must be a non-null ftype");
 		}
 		// always static reference, fixed _invoke_ naming
 		final IJstMethod _invoke_ = ftype.getMethod("_invoke_", true);
 		if (_invoke_ != null) {
 			return _invoke_;
 		} else {
 			// fix by huzhou@ebay.com, to ensure the flex method belongs to the
 			// ftype
 			final JstSynthesizedMethod flexMtd = createFlexMethod(resolver,
 					"_invoke_");
 			if (ftype instanceof JstType) {
 				((JstType) ftype).addMethod(flexMtd);
 			}
 			return flexMtd;
 		}
 	}
 
 	// it must be created whereever it's referenced
 	public static JstSynthesizedMethod createFlexMethod(
 			final JstExpressionBindingResolver resolver, final String name) {
 		final JstModifiers flexModifiers = new JstModifiers();
 		flexModifiers.setPublic();
 		final JstSynthesizedMethod flexMtd = new JstSynthesizedMethod(
 				name != null ? name : "flex", flexModifiers, null);
 		final IJstType objType = getNativeObjectJstType(resolver);
 		flexMtd.setRtnType(objType);
 		final JstArg flexArg = new JstArg(objType, name != null ? name + "Arg"
 				: "flexArg", true);
 		flexMtd.addArg(flexArg);
 		return flexMtd;
 	}
 
 	// it must be created whereever it's referenced
 	public static JstConstructor createFlexConstructor(
 			final JstExpressionBindingResolver resolver) {
 		final JstSynthesizedMethod flexMethod = createFlexMethod(resolver,
 				"constructs");
 		return new JstConstructor(flexMethod.getModifiers(), flexMethod
 				.getArgs().toArray(new JstArg[flexMethod.getArgs().size()]));
 	}
 
 	public static IJstNode getCorrectMethod(
 			final JstExpressionBindingResolver resolver,
 			final IJstType nodeType, final String methodName,
 			final boolean isStatic) {
 
 		IJstNode method = nodeType.getMethod(methodName, isStatic, true);
 		if (method == null) { // lookup property which can be a JstTypeRefType
 			IJstProperty pty = nodeType.getProperty(methodName, isStatic, true);
 
 			if (pty != null) {
 				IJstType ptyType = pty.getType();
 
 				if (ptyType instanceof IJstRefType) {
 					method = ptyType.getConstructor();
 				} else if (ptyType instanceof JstFuncType) {
 					method = ((JstFuncType) ptyType).getFunction();
 					getMethodMetaFromProperty(method, pty);
 				} else {
 					method = pty; // calls property name in mtd invocation expr
 				}
 			}
 		}
 
 		// bugfix by huzhou to lookup Array method/properties for JstArray type?
 		return method;
 	}
 
 	private static void getMethodMetaFromProperty(final IJstNode mtd,
 			final IJstProperty pty) {
 		if (mtd instanceof JstMethod) {
 			final JstMethod mtdNode = (JstMethod) mtd;
 			mtdNode.setParent(pty.getParentNode());
 			mtdNode.getModifiers().merge(pty.getModifiers().getFlags());
 		}
 	}
 
 	public static IJstOType getOtype(final String otypeName) {
 		final List<String> subs = new LinkedList<String>();
 		final IJstType otype = getOtypeParentType(otypeName, subs);
 		if (otype == null) {
 			return null;
 		} else {
 			IJstType otypeSubType = otype;
 			for (final Iterator<String> it = subs.iterator(); it.hasNext()
 					&& otypeSubType != null;) {
 				final String sub = it.next();
 				if (it.hasNext()) {
 					otypeSubType = otypeSubType.getEmbededType(sub);
 				} else {
 					otypeSubType = otypeSubType.getOType(sub);
 				}
 			}
 			return otypeSubType instanceof IJstOType ? (IJstOType) otypeSubType
 					: null;
 		}
 	}
 
 	private static IJstType getOtypeParentType(final String otypeName,
 			final List<String> subs) {
 		// success
 		final IJstType potentialOtypeJstFunctionRefType = JstCache
 				.getInstance().getType(otypeName);
 		if (potentialOtypeJstFunctionRefType instanceof JstType
 				&& ((JstType) potentialOtypeJstFunctionRefType).getStatus()
 						.hasResolution()) {
 			return potentialOtypeJstFunctionRefType;
 		}
 		// fail
 		final int lastDotAt = otypeName != null ? otypeName.lastIndexOf('.')
 				: -1;
 		if (lastDotAt < 0) {
 			return null;
 		}
 		// recursion
 		final String otypeParentTypeName = otypeName.substring(0, lastDotAt);
 		if (lastDotAt < otypeName.length()) {
 			final String sub = otypeName.substring(lastDotAt + 1);
 			subs.add(0, sub);
 		}
 		return getOtypeParentType(otypeParentTypeName, subs);
 	}
 
 	public static String getFullNameIfShortName4InnerType(
 			final IJstType currentType, final IJstType potentialOtypeMemberType) {
 		if (currentType == null || potentialOtypeMemberType == null) {
 			return "";
 		}
 		return new StringBuilder().append(currentType.getName()).append('.')
 				.append(potentialOtypeMemberType.getName()).toString();
 	}
 
 	public static void fixMethodTypeRef(
 			final JstExpressionBindingResolver resolver,
 			final JstMethod method, final IJstType currentType,
 			GroupInfo groupInfo) {
 
 		IJstType rtnType = method.getRtnType();
 		IJstType rtnCorrectType = rtnType;
 		if (rtnType instanceof JstType
 				&& !((JstType) rtnType).getStatus().isValid()) {
 			final IJstType potentialOtypeMemberType = rtnType;
 			IJstOType resolvedOtype = getOtype(potentialOtypeMemberType
 					.getName());
 			if (resolvedOtype == null) {
 				resolvedOtype = getOtype(getFullNameIfShortName4InnerType(
 						currentType, potentialOtypeMemberType));
 			}
 			if (resolvedOtype != null) {
 				rtnCorrectType = resolvedOtype;
 			}
 		}
 		rtnCorrectType = getCorrectType(resolver, rtnType, groupInfo);
 
 		if (rtnCorrectType != rtnType) {
 			method.setRtnType(rtnCorrectType);
 		}
 
 		List<JstArg> args = method.getArgs();
 		if (args != null) {
 			for (JstArg arg : args) {
 				for (IJstType parameterType : arg.getTypes()) {
 					IJstType parameterCorrectType = parameterType;
 					if (parameterType instanceof JstMixedType
 							&& !((JstType) parameterType).getStatus().isValid()) {
 						final JstMixedType mixedOTypes = (JstMixedType) parameterType;
 						for (IJstType mixedType : mixedOTypes.getMixedTypes()) {
 							look4ActualBinding(resolver, mixedType, groupInfo);
 						}
 					} else if (parameterType instanceof JstType
 							&& !((JstType) parameterType).getStatus().isValid()) {
 						final IJstType potentialOtypeMemberType = parameterType;
 						IJstOType resolvedOtype = getOtype(potentialOtypeMemberType
 								.getName());
 						if (resolvedOtype == null) {
 							resolvedOtype = getOtype(getFullNameIfShortName4InnerType(
 									currentType, potentialOtypeMemberType));
 						}
 						if (resolvedOtype != null) {
 							parameterCorrectType = resolvedOtype;
 						}
 					}
 					parameterCorrectType = getCorrectType(resolver,
 							parameterCorrectType, groupInfo);
 					if (parameterCorrectType != parameterType) {
 						arg.updateType(parameterType.getName(),
 								parameterCorrectType);
 					}
 				}
 			}
 		}
 
 		final List<IJstMethod> overloaded = method.getOverloaded();
 		if (overloaded != null && !overloaded.isEmpty()) {
 			for (IJstMethod mtd : overloaded) {
 				if (mtd instanceof JstMethod) {
 					fixMethodTypeRef(resolver, (JstMethod) mtd, currentType,
 							groupInfo);
 				}
 			}
 		}
 
 		updateMethodSignature(method);
 	}
 
 	private static void updateMethodSignature(JstMethod method) {
 
 		if (method.hasJsAnnotation()/* || method.isDispatcher() */) { // only
 																		// update
 																		// methods
 																		// without
 																		// annotation
 			return;
 		}
 
 		IJstNode parent = method.getParentNode();
 		if (!(parent instanceof IJstType)) { // only update method in JstType
 			return;
 		}
 
 		// Parag:Justin - commenting to avoid copying the child method signature
 		// with the one from parent
 		// IJstType ownerType = method.getOwnerType();
 		// String mtdName = method.getName().getName();
 
 		// IJstMethod parentMtd = searchParentMethodWithAnnotation(ownerType,
 		// mtdName, method.isStatic());
 
 		// if (parentMtd != null && parentMtd != method) {
 		// method.getModifiers().copy(parentMtd.getModifiers());
 		// JstTypeHelper.populateMethod(method, parentMtd, true);
 		// }
 	}
 
 	private static IJstMethod searchParentMethodWithAnnotation(IJstType type,
 			String mtdName, boolean isStatic) {
 
 		if (type != null) {
 			IJstMethod method = type.getMethod(mtdName, isStatic);
 
 			if (method != null) {
 				if (method.hasJsAnnotation()/* || method.isDispatcher() */) {
 					return method;
 				}
 			}
 
 			List<IJstType> parentTypes = new ArrayList<IJstType>();
 
 			parentTypes.addAll(type.getExtends());
 			parentTypes.addAll(type.getSatisfies());
 
 			for (IJstType parentType : parentTypes) {
 				if (parentType == type)
 					continue;
 
 				IJstMethod parentMtd = searchParentMethodWithAnnotation(
 						parentType, mtdName, isStatic);
 
 				if (parentMtd != null) {
 					return parentMtd;
 				}
 			}
 			return method;
 		}
 		return null;
 	}
 
 	public static void updateResultType(final IJstResultTypeModifier expr,
 			final IJstType resultType) {
 		expr.setType(resultType);
 	}
 
 	public static void updateArrayType(JstArray arrayType, GroupInfo groupInfo) {
 		IJstType nativeArrType = arrayType.getExtend();
 		IJstType extendedType = JstExpressionTypeLinkerHelper.getExtendedType(
 				nativeArrType, groupInfo);
 		if (extendedType != nativeArrType) {
 			arrayType.clearExtends();
 			arrayType.addExtend(extendedType);
 		}
 	}
 
 	public static void updateFunctionType(JstType ftype, GroupInfo groupInfo) {
 		final IJstType nativeFuncType = ftype.getExtend();
 		final IJstType extendedType = JstExpressionTypeLinkerHelper
 				.getExtendedType(nativeFuncType, groupInfo);
 		if (extendedType != nativeFuncType) {
 			ftype.clearExtends();
 			ftype.addExtend(extendedType);
 		}
 	}
 
 	public static void updateFunctionType(JstFuncType funcType,
 			GroupInfo groupInfo) {
 		final IJstType nativeFuncType = funcType.getExtend();
 		final IJstType extendedType = JstExpressionTypeLinkerHelper
 				.getExtendedType(nativeFuncType, groupInfo);
 		if (extendedType != nativeFuncType) {
 			funcType.clearExtends();
 			funcType.addExtend(extendedType);
 		}
 	}
 
 	public static void fixPropertyTypeRef(
 			final JstExpressionBindingResolver resolver,
 			IJstVisitor jstExpressionTypeLinker, JstProperty pty,
 			GroupInfo groupInfo) {
 		final IJstType ptyType = pty.getType();
 		final IJstType correctType = getCorrectType(resolver, ptyType,
 				groupInfo);
 		if (pty.getValue() != null && pty.getValue() instanceof IExpr) {
 			doExprTypeResolve(resolver, jstExpressionTypeLinker,
 					(IExpr) pty.getValue(), correctType);
 		} else if (pty.getInitializer() != null) {
 			doExprTypeResolve(resolver, jstExpressionTypeLinker,
 					pty.getInitializer(), correctType);
 		}
 
 		if (correctType != ptyType) {
 			pty.setType(correctType);
 		}
 	}
 
 	public static void fixVarsTypeRef(
 			final JstExpressionBindingResolver resolver, JstVars var,
 			GroupInfo groupInfo) {
 		IJstType varType = var.getType();
 		IJstType correctType = getCorrectType(resolver, varType, groupInfo);
 
 		if (correctType != varType) {
 			var.setType(correctType);
 		}
 	}
 
 	/**
 	 * pure helper methods to deal with types
 	 * 
 	 * @param expr
 	 * @return
 	 */
 	public static IJstType getQualifierType(
 			final JstExpressionBindingResolver resolver, final IExpr expr) {
 		if (expr == null) {
 			return null;
 		}
 
 		IExpr qualifier = null;
 		if (expr instanceof FieldAccessExpr) {
 			qualifier = ((FieldAccessExpr) expr).getExpr();
 		} else if (expr instanceof MtdInvocationExpr) {
 			qualifier = ((MtdInvocationExpr) expr).getQualifyExpr();
 		} else if (expr instanceof JstIdentifier) {
 			qualifier = ((JstIdentifier) expr).getQualifier();
 		} else if (expr instanceof ObjCreationExpr) {
 			qualifier = ((ObjCreationExpr) expr).getInvocationExpr();
 		} else if (expr instanceof ArrayAccessExpr) {
 			qualifier = ((ArrayAccessExpr) expr).getExpr();
 		}
 
 		if (qualifier != null) {
 			// enhancement by huzhou to handle JstAttributedType as a qualifier
 			// type
 			// this is used for field accessing and method invocation expr
 			// binding resolutions
 			IJstType qualifierType = qualifier.getResultType();
 			if (qualifierType instanceof JstAttributedType) {
 				final IJstNode binding = ((JstAttributedType) qualifierType)
 						.getJstBinding();
 				if (binding != null) {
 					if (binding instanceof IJstProperty) {
 						return ((IJstProperty) binding).getType();
 					} else if (binding instanceof IJstMethod) {
 						return new JstFuncType((IJstMethod) binding);
 					}
 				}
 			}
 
 			return qualifierType;
 		}
 		return null;
 	}
 
 	public static String getSimpleTypeName(IJstType nodeType) {
 
 		String simpleName = null;
 		if (nodeType instanceof IJstRefType) {
 			simpleName = ((IJstRefType) nodeType).getReferencedNode()
 					.getSimpleName();
 		} else {
 			simpleName = nodeType.getSimpleName();
 		}
 
 		return (simpleName == null) ? "" : simpleName;
 	}
 
 	public static IJstMethod getConstructs(IJstType nodeType, String methodName) {
 		if (nodeType != null && methodName != null) {
 			IJstMethod construct = nodeType.getConstructor();
 
 			if (construct != null) {
 				if (getSimpleTypeName(nodeType).equals(methodName)
 						|| construct.getName().getName().equals(methodName)) {
 
 					return construct;
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public static IJstMethod getDeclaredMethod(IJstType declaringType,
 			String methodName, boolean isStatic) {
 		return declaringType.getMethod(methodName, isStatic);
 	}
 
 	public static String getTypeName(IJstType declaringType) {
 
 		if (declaringType == null) {
 			return "";
 		}
 
 		return declaringType.getName();
 	}
 
 	private static final JstSource UNKNOWNBINDING = new JstSource(JstSource.JS,
 			1, 1, 1, 1, 1);
 
 	public static JstSource createSourceRef(IExpr mtdInvocationExpr) {
 		if (mtdInvocationExpr == null) {
 			return UNKNOWNBINDING;
 		}
 
 		JstSource src = mtdInvocationExpr.getSource();
 		if (src == null) {
 			return UNKNOWNBINDING;
 		}
 
 		return src;
 	}
 
 	public static IJstType getVarType(
 			final JstExpressionBindingResolver resolver, IJstNode var) {
 		IJstType type = null;
 
 		if (var instanceof JstVar) {
 			type = ((JstVar) var).getType();
 		} else if (var instanceof JstVars) {
 			type = ((JstVars) var).getType();
 		} else if (var instanceof JstArg) {
 			JstArg arg = (JstArg) var;
 			List<IJstType> listTypes = arg.getTypes();
 			if (listTypes.size() > 1) {
 				type = new JstVariantType(listTypes);
 			} else {
 				type = arg.getType();
 			}
 		} else if (var instanceof IJstMethod) {// local method
 			type = new JstFuncType((IJstMethod) var);
 		} else if (var instanceof JstIdentifier) {
 			type = ((JstIdentifier) var).getType();
 		} else if (var instanceof IJstType) {
 			type = (IJstType) var;
 		}
 
 		if (var instanceof ILHS) {
 			final IJstNode parent = var.getParentNode();
 			if (parent instanceof AssignExpr) {
 				final IJstNode grandParent = parent.getParentNode();
 				if (grandParent instanceof JstInitializer) {
 					final IJstNode jstVars = grandParent.getParentNode();
 					if (jstVars instanceof JstVars) {
 						type = ((JstVars) jstVars).getType();
 					}
 				}
 			}
 		}
 		return type;
 	}
 
 	public static IInferred cloneInferredType(final IInferred type) {
 		// ugly fix by huzhou@ebay.com to make sure inferred type are separated
 		// in their 1st lookup
 		// the subsequent lookup are from symbol tables, therefore it's ok
 		if (type instanceof JstInferredRefType) {
 			return new JstInferredRefType(
 					(IJstRefType) ((JstInferredRefType) type).getType());
 		} else if (type instanceof JstInferredType) {
 			return new JstInferredType(((JstInferredType) type).getType());
 		}
 
 		return type;
 	}
 
 	public static IJstType getReturnTypeFormFactoryEnabled(
 			MtdInvocationExpr mie, IJstMethod mtd) {
 		String mtdKey = mtd.getOwnerType().getName();
 		mtdKey += mtd.isStatic() ? "::" : ":";
 		mtdKey += mtd.getName().getName();
 
 		TypeResolverRegistry trr = TypeResolverRegistry.getInstance();
 		if (trr.hasResolver(mtdKey)) {
 			List<IExpr> exprs = mie.getArgs();
 			if (exprs.size() > 0) {
 				String[] args = new String[exprs.size()];
 				for (int i = 0; i < exprs.size(); i++) {
 					args[i] = exprs.get(i).toExprText();
 				}
 				// String typeName = trr.resolve(mtdKey, args);
 				IJstType type = trr.resolve(mtdKey, args);
 				if (type != null) {
 					return type;
 					// boolean isArray = typeName.endsWith("[]");
 					// if(isArray){
 					// typeName = typeName.substring(0, typeName.length()-2);
 					// }
 					//
 					// boolean isTypeRefType = false;
 					// if (typeName.startsWith("type::")) {
 					// isTypeRefType = true;
 					// typeName = typeName.substring(6);
 					// }
 					// JstType rtnType =
 					// JstCache.getInstance().getType(typeName);
 					// if (rtnType != null) {
 					// IJstType type = isTypeRefType? new
 					// JstTypeRefType(rtnType): rtnType;
 					// if(isArray){
 					// type = new JstArray(type);
 					// }
 					// return type;
 					// }
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * This util helps to find the best matching return type from all the
 	 * overloading apis with the arguments in {@link MtdInvocationExpr}
 	 * 
 	 * @param mie
 	 * @param mtd
 	 * @return
 	 */
 	public static IJstType getBestRtnTypeFromAllOverloadMtds(
 			MtdInvocationExpr mie, IJstMethod mtd) {
 
 		final IJstType returnType = getRtnTypeFromSingleMtd(mie, mtd);
 		final List<IJstMethod> overloads = mtd.getOverloaded();
 
 		if (!mtd.isDispatcher() || checkAllOverloadsRtnTypeTheSame(overloads)) {
 			return returnType;
 		}
 
 		final List<IExpr> arguments = mie.getArgs();
 		final int argumentsLength = arguments.size();
 		final Map<IJstMethod, List<JstArg>> overloads2ParamsMap = new HashMap<IJstMethod, List<JstArg>>(
 				overloads.size());
 
 		// init the overloads, filling in only whose parameter size matched
 		// arguments' length
 		// or the next param is a variable lengthed one
 		initOverloadsWithCorrectParamSize(overloads, argumentsLength,
 				overloads2ParamsMap);
 
 		// filter out overloads whose parameter types are not assignable from
 		// the arguments'
 		filterOverloadsWithMismatchingParamTypes(arguments, argumentsLength,
 				overloads2ParamsMap);
 
 		// find the best overloads from the rest
 		// get it's return type
 		if (overloads2ParamsMap.keySet().size() > 0) {
 			return getRtnTypeFromSingleMtd(mie,
 					getBestOverloadFromAllValid(overloads2ParamsMap, arguments));
 		}
 
 		return returnType;
 
 	}
 
 	private static boolean checkAllOverloadsRtnTypeTheSame(
 			final List<IJstMethod> allOverloads) {
 		if (allOverloads == null || allOverloads.size() < 1) {
 			return true;
 		}
 
 		final IJstType prevRtnType = allOverloads.get(0).getRtnType();
 		for (IJstMethod it : allOverloads) {
 			if (prevRtnType != it.getRtnType()) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static void initOverloadsWithCorrectParamSize(
 			final List<IJstMethod> overloads, final int argumentsLength,
 			final Map<IJstMethod, List<JstArg>> overloads2ParamsMap) {
 		for (IJstMethod overload : overloads) {
 			// bugfix by huzhou@ebay.com,
 			// when parameter lengths exceeds argument length
 			// or the param at argument length isn't variable lengthed
 			// the overload will be excluded
 			if (preCheckOverloadParamSize(argumentsLength, overload)) {
 				overloads2ParamsMap.put(overload,
 						paddingParams(overload.getArgs(), argumentsLength));
 			}
 		}
 	}
 
 	private static boolean preCheckOverloadParamSize(final int argumentsLength,
 			IJstMethod overload) {
 		final List<JstArg> parameters = overload.getArgs();
 		return parameters.size() <= argumentsLength
 				|| (parameters.size() == argumentsLength + 1 && parameters.get(
 						parameters.size() - 1).isVariable());
 	}
 
 	/**
 	 * helper for
 	 * {@link #getBestRtnTypeFromAllOverloadMtds(MtdInvocationExpr, IJstMethod)}
 	 * its responsibilities are to eliminate the map's entries who has a param
 	 * and argument.getResultType() isn't assignable to param.getType()
 	 * 
 	 * @param arguments
 	 * @param argumentsLength
 	 * @param overloads2ParamsMap
 	 */
 	private static void filterOverloadsWithMismatchingParamTypes(
 			final List<IExpr> arguments, final int argumentsLength,
 			final Map<IJstMethod, List<JstArg>> overloads2ParamsMap) {
 		for (int i = 0; i < argumentsLength; i++) {
 			final IJstType argumentType = arguments.get(i).getResultType();
 			final List<IJstMethod> badOverloads = new LinkedList<IJstMethod>();
 			for (Map.Entry<IJstMethod, List<JstArg>> overloadEntry : overloads2ParamsMap
 					.entrySet()) {
 				final List<JstArg> overloadParams = overloadEntry.getValue();
 				final IJstType overloadParamType = overloadParams.get(i)
 						.getType();
 				// when overloadParamType = argumentType isn't valid, we add it
 				// to bad overloads
 				if (!TypeCheckUtil
 						.isAssignable(overloadParamType, argumentType)) {
 					badOverloads.add(overloadEntry.getKey());
 				}
 			}
 			// filtered bad overloads before checking next argument
 			for (IJstMethod invalidKey : badOverloads) {
 				overloads2ParamsMap.remove(invalidKey);
 			}
 		}
 	}
 
 	/**
 	 * Helper for
 	 * {@link #getBestRtnTypeFromAllOverloadMtds(MtdInvocationExpr, IJstMethod)}
 	 * its responsibilities are to check if method is a factory method and using
 	 * factory method to generate the return type otherwise using the return
 	 * type directly
 	 * 
 	 * @param mie
 	 * @param mtd
 	 * @return
 	 */
 	private static IJstType getRtnTypeFromSingleMtd(MtdInvocationExpr mie,
 			IJstMethod mtd) {
 		IJstType returnType = null;
 		if (mtd.isTypeFactoryEnabled()) {
 			returnType = getReturnTypeFormFactoryEnabled(mie, mtd);
 			if (returnType != null) {
 				return returnType;
 			}
 		}
 		returnType = mtd.getRtnType();
 
 		return resolvingFuncReturnType(mie, returnType);
 	}
 
 	private static IJstType resolvingFuncReturnType(MtdInvocationExpr mie,
 			IJstType returnType) {
 		if (returnType instanceof JstFuncArgAttributedType) {
 			int argIndex = ((JstFuncArgAttributedType) returnType)
 					.getArgPosition() - 1;
 			List<IExpr> args = mie.getArgs();
 			if (args.size() > argIndex) {
 				returnType = args.get(argIndex).getResultType();
 			}
 		} else if (returnType instanceof JstFuncScopeAttributedType) {
 			IExpr scope = mie.getQualifyExpr();
 			if (scope != null) {
 				returnType = scope.getResultType();
 			}
 		} else if (returnType instanceof JstMixedType) {
 			returnType = resolve(mie, (JstMixedType) returnType);
 		}
 		return returnType;
 	}
 
 	private static IJstType resolve(MtdInvocationExpr mie,
 			JstMixedType mixedType) {
 		List<IJstType> mTypes = mixedType.getMixedTypes();
 		List<IJstType> resolvedTypes = new ArrayList<IJstType>(mTypes.size());
 		boolean needResolve = false;
 		for (IJstType mType : mixedType.getMixedTypes()) {
 			if (mType instanceof JstFuncArgAttributedType
 					|| mType instanceof JstFuncScopeAttributedType) {
 				needResolve = true;
 			}
 			IJstType resolved = resolvingFuncReturnType(mie, mType);
 			if (resolved != null) {
 				resolvedTypes.add(resolved);
 			}
 		}
 		if (needResolve && resolvedTypes.size() > 0) {
 			mixedType = new JstMixedType(resolvedTypes);
 		}
 		return mixedType;
 	}
 
 	/**
 	 * simple matching results in one of the {@link ParamMatchingArgCase}
 	 * 
 	 * @param arg
 	 * @param param
 	 * @return
 	 */
 	private static ParamMatchingArgCase argMatchingParam(final IExpr arg,
 			final JstArg param) {
 		final IJstType argumentType = arg.getResultType();
 		final IJstType parameterType = param.getType();
 
 		if (argumentType == parameterType || argumentType == null
 				|| parameterType == null) {
 			return ParamMatchingArgCase.exact;
 		} else if (isSubType(parameterType, argumentType)) {
 			return ParamMatchingArgCase.subtype;
 		}
 		// worst case that assignable is due to proxy/object type parameter
 		else if ("proxy".equals(param.getName())
 				|| "Object".equals(parameterType.getName())) {
 			return ParamMatchingArgCase.object;
 		} else {// if(TypeCheckUtil.isAssignable(parameterType, argumentType))
 			return ParamMatchingArgCase.implicitConversion;
 		}
 	}
 
 	private static IJstMethod getBestOverloadFromAllValid(
 			final Map<IJstMethod, List<JstArg>> validOverloads2ParamsMap,
 			final List<IExpr> arguments) {
 		assert validOverloads2ParamsMap.size() >= 1;
 		assert arguments.size() >= 0;// it's ok for arguments length to be zero
 
 		// #1 only one candidate
 		if (validOverloads2ParamsMap.size() == 1) {
 			return validOverloads2ParamsMap.keySet().iterator().next();
 		}
 		// #2 check same return type
 		// #2 NOTE: check has been upreved to #getBestRtnTypeFromAllOverloads
 		// #2 as this logic needs to be excluded from parameter best matching
 		// flow
 
 		// #3 best matching begins
 		// the best overloads should meet the following
 		// #3.1. Number of parameter matches the number of arguments
 		// #3.2. Higher order parameter type is closer to the argument type than
 		// the other methods
 		final Set<OverloadBestMatchCandidate> scoreSortedOverloads = new TreeSet<OverloadBestMatchCandidate>();
 		final Set<OverloadBestMatchCandidate> filteredOverloads = new HashSet<OverloadBestMatchCandidate>();
 		for (Map.Entry<IJstMethod, List<JstArg>> scoreEntry : validOverloads2ParamsMap
 				.entrySet()) {
 			// if arg types match exactly
 		
 			final IJstMethod key = scoreEntry.getKey();
 			final List<JstArg> value = scoreEntry.getValue();
 //			if (key.getArgs().size() < value.size()) {// number dosn't match
 //				filteredOverloads.add(new OverloadBestMatchCandidate(key,
 //						value, arguments));
 //			} else {
 			if(paramsMatch(key.getArgs(),value)){
 				scoreSortedOverloads.add(new OverloadBestMatchCandidate(key,
 						value, arguments));
 			}else{
 				filteredOverloads.add(new OverloadBestMatchCandidate(key,
 						value, arguments));
 			}
 		}
 
 		// when no method has matched the number of arguments
 		// we check the type matching instead
 		if (scoreSortedOverloads.size() == 0) {
 			scoreSortedOverloads.addAll(filteredOverloads);
 		}
 
 		return scoreSortedOverloads.iterator().next().getMethod();
 	}
 
 	private static boolean paramsMatch(List<JstArg> args, List<JstArg> args2) {
 		int count = 0;
 		boolean argsMatch = false;
 		for(JstArg a:args){
 			if(a.getType().equals(args2.get(count).getType())){
 				argsMatch = true;
 			}
 		}
 		return argsMatch;
 	}
 
 	private static List<JstArg> paddingParams(final List<JstArg> params,
 			final int argumentsLength) {
 		final int paramsLength = params.size();
 		if (paramsLength >= argumentsLength) {
 			return params;
 		}
 
 		final JstArg lastParam = paramsLength > 0 ? params
 				.get(paramsLength - 1) : null;
 		// it's an arraylist as we know the exact length (arguments length)
 		// and there will be subsequent indexing based accessing to the list
 		final List<JstArg> paddingParams = new ArrayList<JstArg>(
 				argumentsLength);
 		paddingParams.addAll(params);
 
 		final JstArg paddingParam = lastParam != null && lastParam.isVariable() ? new JstArg(
 				lastParam.getType(), "proxy", true) : new JstArg(JstCache
 				.getInstance().getType("Undefined"), "proxy", false);
 		for (int i = paramsLength; i < argumentsLength; i++) {
 			paddingParams.add(paddingParam);
 		}
 		return paddingParams;
 	}
 
 	public static List<VarTable> getVarTablesBottomUp(final IJstNode scopedVars) {
 		final List<VarTable> varTables = new LinkedList<VarTable>();
 		if (scopedVars instanceof IJstMethod) {
 			final JstBlock block = ((IJstMethod) scopedVars).getBlock();
 			addVarTableRecursively(varTables, block.getVarTable());
 		} else if (scopedVars instanceof FuncExpr) {
 			final JstBlock block = ((FuncExpr) scopedVars).getFunc().getBlock();
 			addVarTableRecursively(varTables, block.getVarTable());
 		}
 
 		for (IJstNode parent = scopedVars; parent != null; parent = parent
 				.getParentNode()) {
 			if (parent instanceof JstBlock) {
 				final IJstNode grandParent = parent.getParentNode();
 				if (grandParent instanceof IJstMethod
 						|| grandParent instanceof FuncExpr
 						|| grandParent instanceof JstType) {
 					addVarTableRecursively(varTables,
 							((JstBlock) parent).getVarTable());
 				}
 			}
 		}
 		return varTables;
 	}
 
 	private static void addVarTableRecursively(final List<VarTable> varTables,
 			final VarTable varTable) {
 		varTables.add(varTable);
 		if (varTable instanceof TopLevelVarTable) {
 			final TopLevelVarTable topLevelVarTable = (TopLevelVarTable) varTable;
 			for (VarTable linkedVarTable : topLevelVarTable
 					.getLinkedVarTables()) {
 				addVarTableRecursively(varTables, linkedVarTable);
 			}
 		}
 	}
 
 	public static VarTable getVarTable(final IJstNode scopedVars) {
 		for (IJstNode parent = scopedVars; parent != null; parent = parent
 				.getParentNode()) {
 			final IJstNode grandParent = parent.getParentNode();
 			if (parent instanceof JstBlock
 					&& (grandParent instanceof JstMethod
 							|| grandParent instanceof FuncExpr || grandParent instanceof JstType)) {
 				return ((JstBlock) parent).getVarTable();
 			} else if (parent instanceof WithStmt
 					&& grandParent instanceof BlockStmt) {
 				return ((BlockStmt) grandParent).getBody().getVarTable();
 			} else if (parent instanceof JstArg
 					&& grandParent instanceof JstMethod) {
 				final JstBlock block = ((JstMethod) grandParent).getBlock();
 				if (block != null) {
 					return block.getVarTable();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public static IJstType findType(JsVariantType jsTypingMeta) {
 		List<IJstType> types = new ArrayList<IJstType>(3);
 		for (JsTypingMeta t : ((JsVariantType) jsTypingMeta).getTypes()) {
 			if (t instanceof JsType) {
 				types.add(findType((JsType) t));
 			}
 		}
 		return new JstVariantType(types);
 	}
 
 	public static IJstType findType(JsType typing) {
 		IJstType jstType = JstCache.getInstance().getType(typing.getType());
 		if (typing.isTypeRef()) {
 			jstType = JstTypeHelper.getJstTypeRefType(jstType);
 		}
 		return jstType;
 	}
 
 	/**
 	 * helper for {@link #postVisitMtdInvocationExpr(MtdInvocationExpr)}
 	 * 
 	 * @param mie
 	 * @param methodId
 	 * @param mtdBindingType
 	 * @return
 	 */
 	public static boolean checkConstructorCalls(
 			final JstExpressionBindingResolver resolver,
 			final IJstVisitor revisitor, MtdInvocationExpr mie,
 			JstIdentifier methodId, IJstType mtdBindingType) {
 		if (mtdBindingType instanceof JstTypeRefType
 				&& !mtdBindingType.isFType()) { // invocation
 			final JstTypeRefType type = (JstTypeRefType) mtdBindingType;
 			// bugfix by huzhou to set the constructor binding only when the
 			// constructor is available
 			final IJstType boundType = bindConstructor(resolver, revisitor,
 					type, methodId, mie.getArgs());
 			// methodId.setType(null); leave type as is
 			mie.setResultType(boundType);
 			return true;
 		} else if ("this".equals(methodId.getName())) {
 			final JstTypeRefType type = new JstTypeRefType(mtdBindingType);
 			bindConstructor(resolver, revisitor, type, methodId, mie.getArgs());
 			// methodId.setType(null); leave type as is
 			mie.setResultType(type.getType());
 			return true;
 		} else if (VjoKeywords.BASE.equals(methodId.getName())
 				&& mie.getMethodIdentifier() instanceof FieldAccessExpr) {
 			final JstTypeRefType type = new JstTypeRefType(mtdBindingType);
 			bindConstructor(resolver, revisitor, type, methodId, mie.getArgs());
 			// methodId.setType(null); leave type as is
 			((FieldAccessExpr) mie.getMethodIdentifier()).setType(type);
 			mie.setResultType(type.getType());
 			return true;
 		} else if (mie.getParentNode() instanceof ObjCreationExpr) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**********************************************************************
 	 * HELPERS FOR: bind identifier, fieldaccess, mtdinvocation expressions
 	 * ********************************************************************
 	 */
 
 	/**
 	 * @param resolver
 	 * @param scope
 	 * @param identifier
 	 * @param name
 	 * @param bound
 	 * @param symbolType
 	 * @param override
 	 */
 	public static void bindIdentifier(
 			final JstExpressionBindingResolver resolver,
 			final ScopeFrame scope, final JstIdentifier identifier,
 			final String name, IJstNode bound, final IJstType symbolType,
 			final boolean override, final GroupInfo groupInfo) {
 		if (override || identifier.getJstBinding() == null) {
 			identifier.setJstBinding(bound);
 			IJstType varType = JstExpressionTypeLinkerHelper.getVarType(
 					resolver, identifier.getJstBinding() );
 			setExprType(resolver, identifier, varType, groupInfo);
 			addToSymbolMap(scope, name, new LinkerSymbolInfo(name, varType,
 					bound, null));
 		}
 	}
 
 	/**
 	 * helper for identifier binding
 	 * 
 	 * @param scope
 	 * @param symbolName
 	 * @param globalNativeType
 	 */
 	protected static void addToSymbolMap(final ScopeFrame scope,
 			final String symbolName, final LinkerSymbolInfo globalNativeType) {
 
 		if (globalNativeType.getBinding() != null
 				&& globalNativeType.getType() != null) {
 
 			scope.addSymbolBinding(symbolName, globalNativeType);
 		}
 	}
 
 	public static final IJstType bindConstructor(
 			final JstExpressionBindingResolver resolver,
 			final IJstVisitor revisitor, final JstTypeRefType type,
 			final JstIdentifier methodId, final List<IExpr> arguments) {
 		final IJstMethod constructor = type.getConstructor();
 		if (constructor == null) {
 			return null;
 		}
 		methodId.setJstBinding(constructor);
 		final List<JstParamType> paramTypes = type.getParamTypes();
 		final List<IJstMethod> matchingMtds = getMatchingMtdFromOverloads(
 				constructor, arguments);
 		if (matchingMtds.size() == 1) {
 			final IJstMethod matchingMtd = matchingMtds.iterator().next();
 			final List<JstArg> parameters = matchingMtd.getArgs();
 			for (int i = 0, len = arguments.size(); i < len; i++) {
 				final IExpr argExpr = arguments.get(i);
 				if (parameters.size() > i) {
 					if (doesExprRequireResolve(argExpr)) {
 						doExprTypeResolve(resolver, revisitor, argExpr,
 								parameters.get(i).getTypes());
 					}
 				}
 				methodId.setJstBinding(matchingMtd);
 			}
 
 			if (paramTypes.size() > 0) {
 				final JstTypeWithArgs withArgs = new JstTypeWithArgs(
 						type.getReferencedNode());
 				final IJstMethod matchingConstructor = look4MatchingConstructor(
 						constructor, paramTypes, arguments);
 				final List<JstArg> matchingParameters = matchingConstructor
 						.getArgs();
 				for (int i = 0, len = matchingParameters.size(); i < len; i++) {
 					final JstArg param = matchingParameters.get(i);
 					if (paramTypes.contains(param.getType())) {
 						if (arguments.size() > i) {
 							final IExpr arg = arguments.get(i);
 
 							if (doesExprRequireResolve(arg)) {
 								doExprTypeResolve(resolver, revisitor, arg,
 										matchingParameters.get(i).getType());
 							}
 
 							withArgs.addArgType(arg.getResultType());
 
 						}
 					}
 				}
 				return withArgs;
 			}
 			return type.getReferencedNode();
 		}
 		return null;
 	}
 
 	// TODO support overloading
 	private static IJstMethod look4MatchingConstructor(IJstMethod constructor,
 			List<JstParamType> paramTypes, List<IExpr> arguments) {
 		return constructor;
 	}
 
 	public static void bindFieldAccessExpr(
 			final JstExpressionBindingResolver resolver,
 			final IJstProperty pty, final IJstType qualifierType,
 			final FieldAccessExpr fae, final GroupInfo groupInfo) {
 		IJstType type = pty.getType();
 
 		if (pty.getType() instanceof JstParamType) {
 			type = JstTypeHelper.resolveTypeWithArgs(pty, qualifierType);
 		}
 
 		JstIdentifier fieldId = fae.getName();
 		if (fieldId != null) {
 			fieldId.setJstBinding(pty);
 			fieldId.setType(type);
 		}
 		setExprType(resolver, fae, type, groupInfo);
 		return;
 	}
 
 	public static boolean bindThisMtdInvocationInConstructs(
 			final JstExpressionBindingResolver resolver,
 			final IJstVisitor revisitor, final MtdInvocationExpr mie,
 			final IJstType currentType, final GroupInfo groupInfo) {
 
 		IJstNode parent = mie.getParentNode();
 		while (parent != null && !(parent instanceof IJstType)
 				&& !(parent instanceof IJstMethod)) {
 			if (parent instanceof ObjCreationExpr) { // in new
 				return false;
 			}
 			parent = parent.getParentNode();
 		}
 
 		// in constructor and not in new
 		if (JstTypeHelper.isConstructor(parent)) {
 			IExpr mtdId = mie.getMethodIdentifier();
 			IJstType type = null;
 
 			if (mtdId instanceof JstIdentifier) {
 				String mtdName = mtdId.toExprText();
 				if (mtdName.equals(VjoKeywords.BASE)) { // this.base keyword
 					IExpr qualifier = mie.getQualifyExpr();
 					IJstType resultType = qualifier.getResultType();
 					if (resultType instanceof JstInferredType) {
 						resultType = ((JstInferredType) resultType).getType();
 					}
 					if (resultType == currentType) {
 						IJstProperty basepty = resultType
 								.getProperty(VjoKeywords.BASE);
 						if (basepty != null) {
 							type = basepty.getType();
 						}
 					}
 				} else {
 					IJstType resultType = mtdId.getResultType();
 					if (resultType instanceof JstInferredType) {
 						resultType = ((JstInferredType) resultType).getType();
 					}
 					if (resultType == currentType) { // this keyword
 						type = mtdId.getResultType();
 					}
 				}
 			}
 
 			if (type != null) {
 				bindMtdInvocations(resolver, revisitor, mie,
 						type.getConstructor(), groupInfo);
 				setExprType(resolver, mie, type, groupInfo);
 				return true;
 			}
 		}
 
 		return false;
 
 	}
 
 	public static void bindMtdInvocationExpr(
 			final JstExpressionBindingResolver resolver,
 			final IJstVisitor revisitor, final IJstNode mtd,
 			final IJstType qualifierType, final MtdInvocationExpr mie,
 			final GroupInfo groupInfo) {
 		IJstType type = null;
 
 		if (mtd instanceof JstConstructor) {
 			type = mtd.getOwnerType();
 		} else if (mtd instanceof IJstMethod) {
 			type = look4ReturnType(resolver, mtd, qualifierType, mie);
 		}
 
 		bindMtdInvocations(resolver, revisitor, mie, mtd, groupInfo);
 		if(mie.getResultType()==null){
 			setExprType(resolver, mie, type, groupInfo);
 		}
 	}
 
 	/**
 	 * helper for
 	 * {@link #bindMtdInvocationExpr(JstExpressionBindingResolver, IJstNode, IJstType, MtdInvocationExpr)}
 	 * 
 	 * @param resolver
 	 * @param mtd
 	 * @param qualifierType
 	 * @param mie
 	 * @return
 	 */
 	private static IJstType look4ReturnType(
 			final JstExpressionBindingResolver resolver, final IJstNode mtd,
 			final IJstType qualifierType, MtdInvocationExpr mie) {
 		IJstType type;
 		type = getBestRtnTypeFromAllOverloadMtds(mie, (IJstMethod) mtd);
 		type = bindParamTypes(resolver, mtd, mie, qualifierType, type);
 		return type;
 	}
 
 	public static void bindMtdInvocations(
 			final JstExpressionBindingResolver resolver,
 			final IJstVisitor revisitor, final MtdInvocationExpr mtdExpr,
 			final IJstNode mtd, final GroupInfo grpInfo) {
 
 		IJstNode updateBinding = mtd;
 		// handle argument types
 		if (mtd instanceof IJstMethod) {
 			final IJstMethod bindMtd = (IJstMethod) mtd;
 			// TODO enhancement needed to bind based on invocation
 			// when number of arguments and parameters could match unique
 			// overloading api, overloading shouldn't be a problem either
 			final List<IJstMethod> matchingMtds = getMatchingMtdFromOverloads(
 					bindMtd, mtdExpr.getArgs());
 			if (matchingMtds.size() == 1) {
 
 				final IJstMethod matchingMtd = matchingMtds.iterator().next();
 				final List<IExpr> arguments = mtdExpr.getArgs();
 				List<JstArg> parameters = matchingMtd.getArgs();
 				// check if function arg mapping is supported
 				boolean supportArgTypeExt = matchingMtd
 						.isFuncArgMetaExtensionEnabled();
 				// if support function args pass first argument and get 2nd
 				// through n arguments from mapping rather than from orginal
 				// definition
 				IJstMethod extMtd = null;
 				if (supportArgTypeExt && mtdExpr.getArgs().size() > 1) {
 
 					extMtd = bindArgumentMappng(revisitor, grpInfo,
 							matchingMtd, mtdExpr.getArgs().get(0));
 					if(extMtd!=null && extMtd.isDispatcher()){
 						final List<IJstMethod> matchingJstMtds = getMatchingMtdFromOverloads(
 								extMtd, mtdExpr.getArgs());
 						if(matchingJstMtds.size()==1){
 							extMtd = matchingJstMtds.get(0);
 						}
 					}
 					
 					if (extMtd != null) {
 						parameters = extMtd.getArgs();
 					}
 					
 					
 				}
 
 				for (int i = 0, len = arguments.size(); i < len; i++) {
 					final IExpr argExpr = arguments.get(i);
 					if (parameters.size() > i) {
 						if (doesExprRequireResolve(argExpr)) {
 							doExprTypeResolve(resolver, revisitor, argExpr,
 									parameters.get(i).getTypes());
 						}
 					}
 				}
 				if (extMtd != null) {
 					updateBinding = extMtd;
 				} else {
 					updateBinding = matchingMtd;
 				}
 			}
 		}
 
 		if (mtdExpr.getMethodIdentifier() instanceof JstIdentifier) {
 			((JstIdentifier) mtdExpr.getMethodIdentifier())
 					.setJstBinding(updateBinding);
 		} else if (mtdExpr.getMethodIdentifier() instanceof FieldAccessExpr) {
 			FieldAccessExpr expr = (FieldAccessExpr) mtdExpr
 					.getMethodIdentifier();
 			expr.getName().setJstBinding(updateBinding);
 		}
 	}
 
 	private static IJstMethod bindArgumentMappng(IJstVisitor revisitor,
 			GroupInfo groupInfo, IJstMethod callingMethod, IExpr keyArg) {
 		// TODO Auto-generated method stub
 		String targetFunc = callingMethod.getOwnerType().getName()
 				+ (callingMethod.isStatic() ? "::" : ":")
 				+ callingMethod.getName().getName();
 		FunctionParamsMetaRegistry fmr = FunctionParamsMetaRegistry
 				.getInstance();
 
 		if (fmr.isFuncMetaMappingSupported(targetFunc)) {
 			String key = keyArg.toExprText();
 			key = unquote(key);
 			IMetaExtension metaExt = fmr.getExtentedArgBinding(targetFunc, key,
 					groupInfo.getGroupName(), groupInfo.getDependentGroups());
 			if (metaExt != null) {
 				IJstMethod extFunc = metaExt.getMethod();
 				JstExpressionTypeLinkerTraversal.accept(extFunc, revisitor);
 				IJstMethod resolved = unwrapMethod(extFunc);
 				return resolved;
 			}
 		}
 		return null;
 	}
 
 	public static List<IJstMethod> getMatchingMtdFromOverloads(
 			IJstMethod bindMtd, List<IExpr> arguments) {
 
 		if (!bindMtd.isDispatcher()) {
 			return Arrays.asList(bindMtd);
 		}
 
 		final List<IJstMethod> overloads = bindMtd.getOverloaded();
 		final int argumentsLength = arguments.size();
 		final Map<IJstMethod, List<JstArg>> overloads2ParamsMap = new HashMap<IJstMethod, List<JstArg>>(
 				overloads.size());
 
 		// init the overloads, filling in only whose parameter size matched
 		// arguments' length
 		// or the next param is a variable lengthed one
 		initOverloadsWithCorrectParamSize(overloads, argumentsLength,
 				overloads2ParamsMap);
 
 		// filter out overloads whose parameter types are not assignable from
 		// the arguments
 		// but this version tolerates more errors as:
 		// argument is Object typed
 		// argument is Function (JstFuncType) but not matching paremeter's
 		// function api
 		// therefore less accurate, but leaving more chances finding the correct
 		// inferencing
 		filterOverloadsWithMismatchingParamTypesToleratingErrors(arguments,
 				argumentsLength, overloads2ParamsMap);
 		if (overloads2ParamsMap.size() > 0) {
 			final IJstMethod bestMatch = getBestOverloadFromAllValid(
 					overloads2ParamsMap, arguments);
 			if (bestMatch != null) {
 				return Arrays.asList(bestMatch);
 			}
 			return new ArrayList<IJstMethod>(overloads2ParamsMap.keySet());
 		}
 		// failed to find the best, matching the number of arguments only
 		else {
 			initOverloadsWithCorrectParamSize(overloads, argumentsLength,
 					overloads2ParamsMap);
 			if (overloads2ParamsMap.size() > 0) {
 				return new ArrayList<IJstMethod>(overloads2ParamsMap.keySet());
 			}
 			// failed to find the best, using all
 			else {
 				return Collections.unmodifiableList(overloads);
 			}
 		}
 	}
 
 	/**
 	 * helper for
 	 * {@link #getMatchingMtdFromOverloads(MtdInvocationExpr, IJstMethod)} its
 	 * responsibilities are to eliminate the map's entries who has a param and
 	 * argument.getResultType() isn't assignable to param.getType()
 	 * 
 	 * @param arguments
 	 * @param argumentsLength
 	 * @param overloads2ParamsMap
 	 */
 	private static void filterOverloadsWithMismatchingParamTypesToleratingErrors(
 			final List<IExpr> arguments, final int argumentsLength,
 			final Map<IJstMethod, List<JstArg>> overloads2ParamsMap) {
 		for (int i = 0; i < argumentsLength; i++) {
 			final IJstType argumentType = arguments.get(i).getResultType();
 			final List<IJstMethod> badOverloads = new LinkedList<IJstMethod>();
 			for (Map.Entry<IJstMethod, List<JstArg>> overloadEntry : overloads2ParamsMap
 					.entrySet()) {
 				final List<JstArg> overloadParams = overloadEntry.getValue();
 				final IJstType overloadParamType = overloadParams.get(i)
 						.getType();
 				// loosen type check when either argument or parameter type is
 				// null
 				if (argumentType == null || overloadParamType == null
 						|| "Object".equals(argumentType.getName())) {
 					continue;
 				}
 				// loosen type check when both argumentType and parameterType
 				// are functions
 				else if ((argumentType instanceof JstFuncType
 						|| argumentType instanceof JstFunctionRefType
 						|| "Function".equals(argumentType.getName()) || argumentType
 							.isFType())
 						&& (overloadParamType instanceof JstFuncType
 								|| overloadParamType instanceof JstFunctionRefType || "Function"
 									.equals(overloadParamType.getName()))
 						|| overloadParamType.isFType()) {
 					continue;
 				}
 				// when argumentType is Object, ignore
 				// when argumentType is JstFuncType, check if argument is
 				// FuncExpr
 				// if FuncExpr is without Meta, any Function, JstFuncType,
 				// JstFunctionRefType as parameterType will be ok
 				else if (TypeCheckUtil.isAssignable(overloadParamType,
 						argumentType)) {
 					continue;
 				}
 
 				// finally excluding the particular overload
 				badOverloads.add(overloadEntry.getKey());
 			}
 			// filtered bad overloads before checking next argument
 			for (IJstMethod invalidKey : badOverloads) {
 				overloads2ParamsMap.remove(invalidKey);
 			}
 		}
 	}
 
 	public static void doExprTypeUpdate(
 			final JstExpressionBindingResolver resolver,
 			final IJstVisitor revisitor, final IExpr expr, final IJstType type,
 			final GroupInfo groupInfo) {
 		/**
 		 * refactored by huzhou@ebay.com to 1. update the expression's result
 		 * type 2. check if the result type is one of {JstDeferredType,
 		 * SynthOlType} and update their resolved type accordingly
 		 * 
 		 */
 		if (doesExprRequireResolve(expr)) {
 			doExprTypeResolve(resolver, revisitor, expr, type);
 		} else {
 			setExprType(resolver, expr, type, groupInfo);
 		}
 	}
 
 	public static void doJsCommentMetaUpdate(final IJstType resolvedMetaType,
 			final IJstNode srcNode) {
 		final JsCommentMetaNode metaNode = getJsCommentMetaNode(srcNode);
 		if (metaNode != null) {
 			metaNode.setResultType(resolvedMetaType);
 		}
 	}
 
 	public static void doExprTypeResolve(
 			final JstExpressionBindingResolver resolver,
 			final IJstVisitor revisitor, final IExpr expr, final IJstType type) {
 		if (expr instanceof ConditionalExpr) {
 			final ConditionalExpr condExpr = (ConditionalExpr) expr;
 			doExprTypeResolve(resolver, revisitor, condExpr.getThenExpr(), type);
 			doExprTypeResolve(resolver, revisitor, condExpr.getElseExpr(), type);
 		}
 
 		final IJstType exprType = expr.getResultType();
 		if (exprType != null) {
 			if (exprType instanceof JstDeferredType) {
 				((JstDeferredType) exprType).setResolvedType(type);
 			} else if (expr instanceof ObjLiteral
 					&& exprType instanceof SynthOlType
 					&& type instanceof JstMixedType) {
 
 //				doObjLiteralAndOTypeBindingsMixedTypes((ObjLiteral) expr,
 //						(SynthOlType) exprType, (JstMixedType) type, revisitor);
 			} else if (expr instanceof ObjLiteral
 					&& exprType instanceof SynthOlType) {
				doObjLiteralAndOTypeBindings((ObjLiteral) expr,
						(SynthOlType) exprType, type, revisitor, null);
 			} else if (expr instanceof FuncExpr
 					&& exprType instanceof JstFuncType) {
 				if (type instanceof JstFuncType) {
 					tryDerivingAnonymousFunctionsFromAssignment(
 							(FuncExpr) expr,
 							((JstFuncType) type).getFunction(), true, revisitor);
 				} else if (type instanceof JstFunctionRefType) {
 					tryDerivingAnonymousFunctionsFromAssignment(
 							(FuncExpr) expr,
 							((JstFunctionRefType) type).getMethodRef(), true,
 							revisitor);
 				}
 			} else if (expr instanceof JstIdentifier
 					&& (type.isFType() && !(type instanceof IJstRefType))) {
 				((JstIdentifier) expr).setType(JstTypeHelper
 						.getJstTypeRefType(type));
 			} else if ((expr instanceof ObjCreationExpr || expr instanceof MtdInvocationExpr)
 					&& exprType instanceof JstTypeWithArgs
 					&& type instanceof JstTypeWithArgs) {
 				// matching the exprType's argtypes with type's
 				final List<IJstType> inferArgTypes = ((JstTypeWithArgs) exprType)
 						.getArgTypes();
 				final List<IJstType> targetArgTypes = ((JstTypeWithArgs) type)
 						.getArgTypes();
 				for (int i = 0, len = targetArgTypes.size(); i < len; i++) {
 					final IJstType targetArgType = targetArgTypes.get(i);
 					if (i >= inferArgTypes.size()) {
 						((JstTypeWithArgs) exprType).addArgType(targetArgType);
 					} else {
 						final IJstType inferArgType = inferArgTypes.get(i);
 						if (isSubType(targetArgType, inferArgType)) {
 							// TODO if inferArgType is a sub type of
 							// targetArgType, use targetArgType
 							try {
 								replaceArgType(((JstTypeWithArgs) exprType),
 										inferArgType, i);
 							} catch (Exception e) {
 								// do nothing
 							}
 						}
 						// else remain the arg type as it is
 					}
 				}
 			}
 			// resolving JstArrayInitializer
 			else if (expr instanceof JstArrayInitializer
 					&& exprType instanceof JstArray && type instanceof JstArray) {
 				final JstArray exprArray = (JstArray) exprType;
 				final JstArray trueArray = (JstArray) type;
 				if (exprArray.getComponentType() != trueArray
 						.getComponentType()) {
 					((JstArrayInitializer) expr).setType(trueArray);
 				}
 			}
 		}
 	}
 
 	public static void doExprTypeResolve(
 			final JstExpressionBindingResolver resolver,
 			final IJstVisitor revisitor, final IExpr expr,
 			final List<IJstType> types) {
 		for (IJstType iJstType : types) {
 
 			doExprTypeResolve(resolver, revisitor, expr, iJstType);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void replaceArgType(JstTypeWithArgs jstTypeWithArgs,
 			IJstType inferArgType, int i) throws SecurityException,
 			NoSuchFieldException, IllegalArgumentException,
 			IllegalAccessException {
 		final Field argTypesField = JstTypeWithArgs.class
 				.getField("m_argTypes");
 		argTypesField.setAccessible(true);
 		final List<IJstType> argTypes = (List<IJstType>) argTypesField
 				.get(jstTypeWithArgs);
 		if (i < argTypes.size()) {
 			argTypes.remove(i);
 			argTypes.add(i, inferArgType);
 		}
 	}
 
 	// should be instead using isAssignable logic
 	private static boolean isSubType(final IJstType candidateSuperType,
 			final IJstType candidateSubType) {
 		if (candidateSuperType == candidateSubType) {
 			return true;
 		}
 		// TODO handle proxy type
 		for (IJstType inherit : candidateSubType.getExtends()) {
 			if (isSubType(candidateSuperType, inherit)) {
 				return true;
 			}
 		}
 		for (IJstType inherit : candidateSubType.getSatisfies()) {
 			if (isSubType(candidateSuperType, inherit)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private static void doObjLiteralAndOTypeBindingsMixedTypes(
 			final ObjLiteral objLiteral, final SynthOlType synthOlType,
 			final JstMixedType mtype, final IJstVisitor revisitor) {
 		for (IJstType type : mtype.getMixedTypes()) {
 			if (type instanceof JstAttributedType) {
 				JstAttributedType atype = (JstAttributedType) type;
 				IJstOType otype = atype.getOType(atype.getAttributeName());
 				doObjLiteralAndOTypeBindings(objLiteral, synthOlType, otype,
 						revisitor, mtype);
 			} else if (type instanceof JstObjectLiteralType) {
 				doObjLiteralAndOTypeBindings(objLiteral, synthOlType,
 						(IJstOType) type, revisitor, null);
 			}
 		}
 	}
 
 	static void doObjLiteralAndOTypeBindings(ObjLiteral objLiteral,
 			final SynthOlType synthOlType, IJstType otype,
 			final IJstVisitor revisitor, JstMixedType mtype) {
 
 		IJstType override = resolveOtype(objLiteral);
 
 		if (otype instanceof JstAttributedType) {
 			JstAttributedType atype = (JstAttributedType) otype;
 			otype = atype.getOType(atype.getAttributeName());
 		}
 
 		if (override != null) {
 			otype = override;
 		}
 
 		// support nested obj literals
 		if (otype != null && (otype instanceof SynthOlType)) {
 			objLiteral.setJstType(otype);
 		} else if (synthOlType != null && otype != null
 				&& (otype instanceof JstObjectLiteralType)) {
 			synthOlType.addResolvedOType(otype);
 		} else if (synthOlType != null && otype != null
 				&& (otype instanceof JstMixedType)) {
 			JstMixedType mixed = (JstMixedType) otype;
 			for (final IJstType type : mixed.getMixedTypes()) {
 				IJstType mixedOType = null;
 				if (type instanceof JstAttributedType) {
 					JstAttributedType atype = (JstAttributedType) type;
 					mixedOType = atype.getOType(atype.getAttributeName());
 				}
 
 				if (synthOlType != null && mixedOType != null
 						&& (mixedOType instanceof JstObjectLiteralType)) {
 					synthOlType.addResolvedOType(mixedOType);
 				}
 			}
 		} else {
 			return;
 		}
 
 		// now we traverse the object literal to look 4 further bindings like:
 		// functions, embedded obj literals etc.
 		for (NV nv : objLiteral.getNVs()) {
 			final JstIdentifier id = nv.getIdentifier();
 			final String name = id.getName();
 			if (otype != null) {
 				doObjLiteralNameBinding(otype, id, name,mtype);
 
 				final IExpr valueExpr = nv.getValue();
 				if (valueExpr != null) {
 					doObjLiteralValueBinding(otype, revisitor, name, valueExpr,
 							mtype);
 				}
 			}
 		}
 	}
 
 	private static IJstType resolveOtype(ObjLiteral objLiteral) {
 
 		IJstType otype = null;
 		OTypeResolverRegistry otypeResolver = OTypeResolverRegistry
 				.getInstance();
 
 		if (objLiteral.getNVs().size() > 0) {
 			Set<String> keys = OTypeResolverRegistry.getInstance().getKeys();
 			for (String field : keys) {
 				NV firstPosition = objLiteral.getNV(field);
 				if (firstPosition == null) {
 					return null;
 				}
 				String key = firstPosition.getName();
 				if (otypeResolver.hasResolver(key)) {
 
 					otype = otypeResolver.resolve(key, firstPosition);
 					if (otype instanceof JstAttributedType) {
 						JstAttributedType atype = (JstAttributedType) otype;
 						otype = convertAttributedTypeToOtype(atype);
 					}
 				}
 			}
 
 		}
 		return otype;
 	}
 
 	private static IJstType convertAttributedTypeToOtype(
 			JstAttributedType atype) {
 		final String attributeName = atype.getAttributeName();
 		if (atype.isOType()) {
 			final IJstType objLiteralOrFunctionRefType = atype
 					.getOType(attributeName);
 			if (objLiteralOrFunctionRefType != null) {
 				return objLiteralOrFunctionRefType;
 			}
 		}
 		return null;
 	}
 
 	private static void doObjLiteralNameBinding(final IJstType otype,
 			final JstIdentifier id, final String name,JstMixedType mtype) {
 
 		// TODO we need a multiple node binding here since multiple 
 		// bug 399299
 		if(id.getJstBinding()!=null){
 			// do not rebind again in the jstmixed type case where there are 2 names.
 			// first one in wins
 			return;
 		}
 		// end bug 399299
 		IJstNode oBinding = otype.getProperty(name, false);
 		if (oBinding != null) {
 			id.setJstBinding(oBinding);
 		} else {
 			oBinding = otype.getMethod(name, false);
 			if (oBinding != null) {
 				id.setJstBinding(oBinding);
 			}
 
 
 		}
 	}
 
 	private static void doObjLiteralValueBinding(final IJstType otype,
 			final IJstVisitor revisitor, final String name,
 			final IExpr valueExpr, JstMixedType mtype) {
 		if (valueExpr instanceof JstArrayInitializer) {
 			JstArrayInitializer arrayValueExpr = (JstArrayInitializer) valueExpr;
 			for (IExpr element : arrayValueExpr.getExprs()) {
 				if (element instanceof ObjLiteral) {
 					doObjLiteralAndOTypeBindings((ObjLiteral) element,
 							(SynthOlType) element.getResultType(), otype,
 							revisitor, mtype);
 				}
 
 			}
 
 		}
 
 		if (valueExpr instanceof FuncExpr
 				&& isAnonymousFunction(((FuncExpr) valueExpr).getFunc())) {
 			final IJstProperty matchingOTypePty = otype.getProperty(name,
 					false, true);
 			final JstMethod func = ((FuncExpr) valueExpr).getFunc();
 			if (matchingOTypePty != null && func != null
 					&& matchingOTypePty.getType() != null) {
 
 				if (matchingOTypePty.getType() instanceof JstFunctionRefType) {
 					deriveAnonymousFunction(
 							((JstFunctionRefType) matchingOTypePty.getType())
 									.getMethodRef(),
 							func, matchingOTypePty.getDoc());
 					JstExpressionTypeLinkerTraversal.accept(valueExpr,
 							revisitor);
 				} else if (matchingOTypePty.getType() instanceof JstFuncType) {
 					deriveAnonymousFunction(
 							((JstFuncType) matchingOTypePty.getType())
 									.getFunction(),
 							func, matchingOTypePty.getDoc());
 					JstExpressionTypeLinkerTraversal.accept(valueExpr,
 							revisitor);
 				}
 
 			}
 		} else if (valueExpr instanceof ObjLiteral
 				&& valueExpr.getResultType() != null
 				&& valueExpr.getResultType() instanceof SynthOlType) {
 			// TODO this needs to support mixed type only one otype is checked
 			// for first type found
 			IJstType propertyType = null;
 			IJstProperty prop = otype.getProperty(name, false, true);
 			if (prop != null && mtype == null) {
 				propertyType = otype.getProperty(name, false, true).getType();
 			}
 			if (mtype != null) {
 				propertyType = findLiteralFieldTypeFromMixedType(mtype, name);
 			}
 			if (propertyType != null) {
 				doObjLiteralAndOTypeBindings((ObjLiteral) valueExpr,
 						(SynthOlType) valueExpr.getResultType(), propertyType,
 						revisitor, null);
 			} else {
 				doObjLiteralAndOTypeBindings((ObjLiteral) valueExpr,
 						(SynthOlType) valueExpr.getResultType(), otype,
 						revisitor, null);
 			}
 		}
 	}
 
 	private static IJstType findLiteralFieldTypeFromMixedType(
 			JstMixedType mtype, String name) {
 		List<IJstType> types = new ArrayList<IJstType>();
 		for (IJstType type : mtype.getMixedTypes()) {
 			if(type instanceof JstAttributedType){
 				IJstType otype = convertAttributedTypeToOtype((JstAttributedType)type);
 				if(otype!=null){
 					IJstProperty prop = otype.getProperty(name, false, true);
 					if(prop!=null){
 						if(prop.getType() instanceof JstMixedType){
 							types.addAll(((JstMixedType)prop.getType()).getMixedTypes());
 						}else{
 							types.add(prop.getType());
 						}
 					}
 				}
 			}
 		}
 		if(!types.isEmpty()){
 			return new JstMixedType(types);
 		}
 		return null;
 	}
 
 	public static boolean isFunctionMetaAvailable(final FuncExpr funcExpr) {
 		for (IJstNode child : funcExpr.getChildren()) {
 			if (child != null && child instanceof JsCommentMetaNode) {
 				final JsCommentMetaNode commentMetaNode = (JsCommentMetaNode) child;
 				if (commentMetaNode.getJsCommentMetas() != null
 						&& commentMetaNode.getJsCommentMetas().size() > 0) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static JsCommentMetaNode getJsCommentMetaNode(final IJstNode node) {
 		if (node != null) {
 			for (IJstNode child : node.getChildren()) {
 				if (child != null && child instanceof JsCommentMetaNode) {
 					return (JsCommentMetaNode) child;
 				}
 			}
 		}
 		return null;
 	}
 
 	public static List<IJsCommentMeta> getJsCommentMeta(final IJstNode node) {
 		final JsCommentMetaNode metaNode = getJsCommentMetaNode(node);
 		return metaNode != null ? metaNode.getJsCommentMetas() : null;
 	}
 
 	public static boolean isAnonymousFunction(final IJstMethod func) {
 		if (func == null || func.getName() == null) {
 			return false;
 		}
 
 		return FunctionExpressionTranslator.DUMMY_METHOD_NAME.equals(func
 				.getName().getName());
 	}
 
 	/**
 	 * @see #doExprTypeUpdate(JstExpressionBindingResolver, IExpr, IJstType)
 	 *      this method helps to check if the rhs expr could be resolved to some
 	 *      type in case of {@see JstDeferredType, @see SynthOlType, etc.} the
 	 *      resolved type could be from {MtdInvocationExpr, RtnStmt,
 	 *      Assignment(JstVars)} right now
 	 * 
 	 * @param rhsExpr
 	 * @return
 	 */
 	public static boolean doesExprRequireResolve(final IExpr rhsExpr) {
 		if (rhsExpr != null && rhsExpr.getResultType() != null) {
 			final IJstType resultType = rhsExpr.getResultType();
 			return resultType instanceof JstDeferredType
 					|| resultType instanceof SynthOlType
 					|| resultType instanceof JstFuncType
 					|| (resultType.isFType()
 							&& !(resultType instanceof IJstRefType)
 							|| resultType instanceof JstTypeWithArgs || resultType instanceof JstArray);
 		} else {
 			return false;
 		}
 	}
 
 	public static void tryDerivingAnonymousFunctionsFromAssignment(
 			final FuncExpr funcExpr, final IJstMethod lhsFunc,
 			final boolean checkAnonymous, final IJstVisitor revisitor) {
 		final JstMethod func = funcExpr.getFunc();
 		if (func != null
 				&& (!checkAnonymous || (checkAnonymous && isAnonymousFunction(func)))
 				&& func instanceof JstMethod) {
 			deriveAnonymousFunction(lhsFunc, (JstMethod) func, func.getDoc());
 			// bugfix by huzhou@ebay.com, needs to revisit the func expression
 			// to allow the correct binding inside after the inference
 			JstExpressionTypeLinkerTraversal.accept(func, revisitor);
 		}
 	}
 
 	private static JstFuncType s_fakeFunc;
 
 	private static JstFuncType getFakeFunc() {
 		if (s_fakeFunc == null) {
 			IJstMethod method = new JstMethod(new JstArg(JstCache.getInstance()
 					.getType("Object"), "p", true));
 			s_fakeFunc = new JstFuncType(method);
 		}
 		return s_fakeFunc;
 	}
 
 	public static void tryDerivingAnonymousFunctionsFromParam(
 			final MtdInvocationExpr mie, final IJstNode mtdBinding,
 			final IJstVisitor revisitor, final GroupInfo groupInfo, IJstType qualifierType) {
 		if (mtdBinding != null && mie != null
 				&& mtdBinding instanceof IJstMethod) {
 			// handle anonymous function argument inference
 			final IJstMethod callingMethod = (IJstMethod) mtdBinding;
 			final List<IExpr> arguments = mie.getArgs();
 			if (callingMethod != null && arguments != null) {
 				boolean supportArgTypeExt = callingMethod
 						.isFuncArgMetaExtensionEnabled();
 				final List<JstFuncType> paramTypes = getFilteredParamTypes(
 						callingMethod, arguments.size());
 				int paramLen = paramTypes.size(), argLen = arguments.size();
 				if (supportArgTypeExt && paramLen >= 2 && argLen >= 2) {
 					if (paramTypes.get(1) == null
 							&& (arguments.get(1) instanceof FuncExpr || arguments.get(1) instanceof JstIdentifier)) {
 						paramTypes.set(1, getFakeFunc());
 					}
 				}
 				for (int paramIdx = 0, argIdx = 0; paramIdx < paramLen
 						&& argIdx < argLen; // parameter list isn't emptied,
 											// argument list isn't emptied
 				paramIdx++, argIdx++) {
 
 					JstFuncType paramType = paramTypes.get(paramIdx);
 					if (paramType != null) {
 						final IExpr arg = arguments.get(argIdx);
 
 						if (arg instanceof FuncExpr) {
 							processFunctionExpression(mie, revisitor,
 									groupInfo, callingMethod, arguments,
 									supportArgTypeExt, paramIdx, paramType, arg,qualifierType);
 						}else if(arg instanceof JstIdentifier){
 							
 //							IJstType qualiferType = mtdBinding.getRootType();
 									
 //							IJstType qualiferType = mie.getQualifyExpr()
 //									.getResultType();
 							
 							if (qualifierType != null) {
 								
 								IExpr keyArg = arguments.get(0);
 								if (keyArg instanceof JstLiteral) {
 									paramType = deriveFunctionType(revisitor, groupInfo,
 											callingMethod, paramType, keyArg, qualifierType);
 									// should the parent 
 									((JstIdentifier) arg).setType(paramType);
 									((JstIdentifier) arg).setJstBinding(paramType.getFunction());
 								}
 							}
 						
 							
 							//arg = new FuncExpr(func)
 //							IJstType qualiferType = mie.getQualifyExpr()
 //									.getResultType();
 //							JstFuncType funcTye = deriveFunctionType(revisitor, groupInfo, callingMethod, paramType, arg, qualiferType);
 ////							deriveAnonymousFunction(paramType, (JstIdentifier) arg, (IJstDoc)null);
 //							// bugfix by huzhou@ebay.com, needs to revisit
 //							// the func expression
 //							// to allow the correct binding inside after the
 //							// inference
 //							JstExpressionTypeLinkerTraversal.accept(arg, revisitor);
 						}
 						// tricks the loop to continue till all arguments are
 						// consumed
 						// removed because the trick is moved into the filter
 						// param types
 						// the arguments length is used as the length of the
 						// list
 						// the gap will be filled with last parameter who is
 						// variable lengthed if available
 						// if(paramIdx == paramLen - 1 && param.isVariable()){
 						// paramIdx--;
 						// }
 					}
 				}
 			}
 		}
 	}
 
 	private static void processFunctionExpression(final MtdInvocationExpr mie,
 			final IJstVisitor revisitor, final GroupInfo groupInfo,
 			final IJstMethod callingMethod, final List<IExpr> arguments,
 			boolean supportArgTypeExt, int paramIdx, JstFuncType paramType,
 			final IExpr arg, IJstType qualifierType) {
 		// it actually could be
 		// new Function as
 		// ObjCreateExpr but
 		// it's useless as we
 		// won't inspect the
 		// string definition
 		final FuncExpr funcArg = (FuncExpr) arg;
 		final IJstMethod func = funcArg.getFunc();
 		// infer when function has no annotation only
 		// TODO check how to verify function has no
 		// annotation
 		if (func != null && isAnonymousFunction(func)
 				&& func instanceof JstMethod) {
 
 			if (paramIdx == 1 && supportArgTypeExt) {
 				IExpr keyArg = arguments.get(0);
 				if (keyArg instanceof JstLiteral) {
 					//if(mie.getQualifyExpr()!=null){
 					// NPE here when accessing function reference rather than direct invoke?
 					
 					
 					if (qualifierType != null) {
 						paramType = deriveFunctionType(revisitor, groupInfo,
 								callingMethod, paramType, keyArg, qualifierType);
 					}
 				}
 			}
 			deriveAnonymousFunction(paramType, (JstMethod) func, func.getDoc());
 			// bugfix by huzhou@ebay.com, needs to revisit
 			// the func expression
 			// to allow the correct binding inside after the
 			// inference
 			JstExpressionTypeLinkerTraversal.accept(func, revisitor);
 		}
 	}
 
 	private static JstFuncType deriveFunctionType(final IJstVisitor revisitor,
 			final GroupInfo groupInfo, final IJstMethod callingMethod,
 			JstFuncType paramType, IExpr keyArg, IJstType qualiferType) {
 		if (qualiferType instanceof IJstRefType) {
 			qualiferType = ((IJstRefType) qualiferType)
 					.getReferencedNode();
 		}
 		String targetFunc = qualiferType.getName()
 				+ (callingMethod.isStatic() ? "::" : ":")
 				+ callingMethod.getName().getName();
 		FunctionMetaRegistry fmr = FunctionMetaRegistry
 				.getInstance();
 		if (fmr.isFuncMetaMappingSupported(targetFunc) && keyArg instanceof JstLiteral){
 				
 			String key = ((JstLiteral) keyArg).toString();
 			key = unquote(key);
 			IMetaExtension metaExt = fmr.getExtentedArgBinding(
 					targetFunc, key, groupInfo.getGroupName(),
 					groupInfo.getDependentGroups());
 			if (metaExt != null) {
 				// if the metaExt is attributed type need to get method from that attributed type
 				
 				IJstMethod extFunc = metaExt.getMethod();
 				
 				
 				if (extFunc != null) {
 					
 					JstExpressionTypeLinkerTraversal.accept(
 							extFunc, revisitor);
 					// when referencing attributed type unwrapMethod is not resolving the ftype
 					IJstMethod resolved = unwrapMethod(extFunc);
 					
 					if (resolved != null) {
 						paramType = new JstFuncType(
 								resolved);
 					}
 				}
 			}
 		}
 		return paramType;
 	}
 
 	private static IJstMethod unwrapMethod(IJstMethod extFunc) {
 		if (extFunc instanceof JstPotentialOtypeMethod) {
 			return ((JstPotentialOtypeMethod) extFunc).getResolvedOtypeMethod();
 		} else if (extFunc instanceof JstPotentialAttributedMethod) {
 			return ((JstPotentialAttributedMethod) extFunc)
 					.getResolvedAttributedMethod();
 		}
 		return extFunc;
 	}
 
 	private static String unquote(String val) {
 		if ((val.startsWith("\"") && val.endsWith("\""))
 				|| (val.startsWith("'") && val.endsWith("'"))) {
 			return val.substring(1, val.length() - 1);
 		}
 		return val;
 	}
 
 	/**
 	 * helper for
 	 * {@link #tryDerivingAnonymousFunctionsFromParam(MtdInvocationExpr, IJstNode, IJstVisitor)}
 	 * it looks through the method signatures and return the list of
 	 * JstFuncTypes it leaves null in positions where: 1. arg type isn't unique
 	 * 2. arg type isn't JstFuncType
 	 * 
 	 * @param callingMethod
 	 * @param argumentsLength
 	 *            for variable lengthed arguments usage
 	 * @return
 	 */
 	private static List<JstFuncType> getFilteredParamTypes(
 			final IJstMethod callingMethod, final int argumentsLength) {
 		final List<JstFuncType> filteredParamTypes = new ArrayList<JstFuncType>(
 				argumentsLength);
 		// CASE 0: Method is not overloaded at all
 		if (!callingMethod.isDispatcher()) {
 			final List<JstArg> parameters = callingMethod.getArgs();
 			final int paramSize = parameters.size();
 			for (int i = 0, len = paramSize; i < len && i < argumentsLength; i++) {
 				final JstArg param = parameters.get(i);
 				if (param != null) {
 					final JstFuncType funcType = getFilteredFuncType(param
 							.getTypes());
 					filteredParamTypes.add(funcType);
 				}
 			}
 			if (argumentsLength > paramSize && paramSize > 0) {
 				final JstArg lastParam = parameters.get(paramSize - 1);
 				if (lastParam.isVariable()) {
 					final JstFuncType lastParamFuncType = getFilteredFuncType(lastParam
 							.getTypes());
 					for (int i = paramSize; i < argumentsLength; i++) {
 						filteredParamTypes.add(lastParamFuncType);
 					}
 				} else {
 					for (int i = paramSize; i < argumentsLength; i++) {
 						filteredParamTypes.add(null);
 					}
 				}
 			}
 		}
 		// CASE 1: Method is overloaded, we get the info from all the
 		// overloading functions signatures
 		else {
 			final List<IJstMethod> overloads = callingMethod.getOverloaded();
 			for (int i = 0; i < argumentsLength; i++) {
 				JstFuncType filteredFuncType = null;
 				for (int j = 0, len = overloads.size(); j < len; j++) {
 					final IJstMethod overload = overloads.get(j);
 					final List<JstArg> overloadParams = overload.getArgs();
 					final int overloadParamSize = overloadParams.size();
 					if (overloadParamSize > i) {
 						final JstArg overloadParam = overloadParams.get(i);
 						if (overloadParam != null
 								&& overloadParam.getType() instanceof JstFuncType) {
 							if (filteredFuncType == null) {
 								filteredFuncType = (JstFuncType) overloadParam
 										.getType();
 							} else if (isEqualFuncType(filteredFuncType,
 									(JstFuncType) overloadParam.getType())) {
 								continue;
 							} else {
 								filteredFuncType = null;
 								break;
 							}
 						}
 					} else if (overloadParamSize > 0) {
 						final JstArg lastParam = overloadParams
 								.get(overloadParamSize - 1);
 						if (lastParam != null && lastParam.isVariable()
 								&& lastParam.getType() instanceof JstFuncType) {
 							if (filteredFuncType == null) {
 								filteredFuncType = (JstFuncType) lastParam
 										.getType();
 							} else if (isEqualFuncType(filteredFuncType,
 									(JstFuncType) lastParam.getType())) {
 								continue;
 							} else {
 								filteredFuncType = null;
 								break;
 							}
 						}
 					}
 				}
 				filteredParamTypes.add(filteredFuncType);
 			}
 		}
 		return filteredParamTypes;
 	}
 
 	private static JstFuncType getFilteredFuncType(final List<IJstType> types) {
 		if (types == null || types.isEmpty()) {
 			return null;
 		}
 		JstFuncType funcType = null;
 		for (IJstType type : types) {
 			if (type != null && type instanceof JstFuncType) {
 				if (funcType == null) {
 					funcType = (JstFuncType) type;
 				} else if (isEqualFuncType(funcType, (JstFuncType) type)) {
 					continue;
 				} else {
 					return null;
 				}
 			}
 		}
 		return funcType;
 	}
 
 	private static boolean isEqualFuncType(final JstFuncType func1,
 			final JstFuncType func2) {
 		if (func1 == null || func2 == null) {
 			return false;
 		}
 		final IJstMethod f1 = func1.getFunction();
 		final IJstMethod f2 = func2.getFunction();
 		if (f1 == null || f2 == null) {
 			return false;
 		}
 		
 		if(f1.getOverloaded().size() == f2.getOverloaded().size() && f1.getOverloaded().size()>0){
 			
 			boolean equal= true;
 			for (int i = 0; i < f1.getOverloaded().size(); i++) {
 				IJstMethod f1ovld = f1.getOverloaded().get(i);
 				IJstMethod f2ovld = f2.getOverloaded().get(i);
 				if(!isEqualMethodArgs(f1ovld, f2ovld)){
 					equal = false;
 				
 			}
 			}
 				
 			return equal;
 			
 		}
 		else{
 		
 			return isEqualMethodArgs(f1, f2);
 		}
 
 
 		
 	}
 
 	private static boolean isEqualMethodArgs(final IJstMethod f1,
 			final IJstMethod f2) {
 		if (!f1.getRtnType().toString().equals(f2.getRtnType().toString())) {
 			return false;
 		}
 		if (!f1.getArgs().toString().equals(f2.getArgs().toString())) {
 			return false;
 		}
 		return true;
 	}
 
 	public static void tryDerivingAnonymousFunctionsFromReturn(
 			final RtnStmt rtnStmt, final IJstNode mtdBinding,
 			final IJstVisitor revisitor) {
 		if (mtdBinding != null && rtnStmt != null
 				&& mtdBinding instanceof IJstMethod) {
 			final Set<IJstType> returnTypes = getDedupedReturnTypes((IJstMethod) mtdBinding);
 			if (returnTypes.size() == 1) {// single return type
 				// handle anonymous function argument inference
 				final IExpr rtnExpr = rtnStmt.getExpression();
 				final IJstType rtnType = returnTypes.iterator().next();
 				if (rtnType instanceof JstFuncType
 						&& rtnExpr instanceof FuncExpr) {
 					final FuncExpr funcArg = (FuncExpr) rtnExpr;
 					final IJstMethod func = funcArg.getFunc();
 					if (func != null && isAnonymousFunction(func)
 							&& func instanceof JstMethod) {
 						deriveAnonymousFunction((JstFuncType) rtnType,
 								(JstMethod) func, func.getDoc());
 						JstExpressionTypeLinkerTraversal
 								.accept(func, revisitor);
 					}
 				}
 			}
 		}
 	}
 
 	private static Set<IJstType> getDedupedReturnTypes(final IJstMethod mtd) {
 		final Set<IJstType> returnTypes = new HashSet<IJstType>(8);
 		if (mtd.isDispatcher()) {
 			for (IJstMethod overload : mtd.getOverloaded()) {
 				returnTypes.add(overload.getRtnType());
 			}
 		} else {
 			returnTypes.add(mtd.getRtnType());
 		}
 		return returnTypes;
 	}
 
 	private static void deriveAnonymousFunction(
 			final JstFuncType functionDefType,
 			final JstMethod anonymousFunction, IJstDoc doc) {
 		final IJstMethod paramFunction = functionDefType.getFunction();
 		deriveAnonymousFunction(paramFunction, anonymousFunction, doc);
 	}
 
 	/**
 	 * by huzhou@ebay.com renamed to derived, as no infer wrapping types are
 	 * being created in this case
 	 * 
 	 * @param paramFunction
 	 * @param anonymousFunction
 	 */
 	private static void deriveAnonymousFunction(final IJstMethod paramFunction,
 			final JstMethod anonymousFunction, IJstDoc doc) {
 		if (anonymousFunction == paramFunction) {
 			return;
 		}
 
 		// deal with return type inference and argument type inferences
 		final IJstType paramFunctionRtnType = paramFunction.getRtnType();
 		anonymousFunction.setRtnType(paramFunctionRtnType);
 		anonymousFunction.setReturnOptional(paramFunction
 				.isReturnTypeOptional());
 
 		if(paramFunction instanceof JstMethod){
 			((JstMethod) paramFunction).setDoc(doc);
 		}
 		if (!paramFunction.isDispatcher()) {
 			final List<JstArg> params = paramFunction.getArgs();
 			final List<JstArg> inferParams = anonymousFunction.getArgs();
 			deriveAnonymousFunctionParams(params, inferParams, true);
 			if (anonymousFunction.isDispatcher()) {
 				for (IJstMethod anonymousFunctionOverload : anonymousFunction
 						.getOverloaded()) {
 					deriveAnonymousFunctionOverload(anonymousFunctionOverload,
 							inferParams, paramFunctionRtnType);
 				}
 			}
 		} else {
 			final List<JstArg> inferParams = anonymousFunction.getArgs();
 			boolean firstOverload = true;
 			for (IJstMethod paramFunctionOverload : sortByNumberOfParams(paramFunction)) {
 				final List<JstArg> params = paramFunctionOverload.getArgs();
 				deriveAnonymousFunctionParams(params, inferParams,
 						firstOverload);
 				firstOverload = false;
 			}
 			if (anonymousFunction.isDispatcher()) {
 				for (IJstMethod anonymousFunctionOverload : anonymousFunction
 						.getOverloaded()) {
 					deriveAnonymousFunctionOverload(anonymousFunctionOverload,
 							inferParams, paramFunctionRtnType);
 				}
 			}
 		}
 	}
 
 	private static List<IJstMethod> sortByNumberOfParams(
 			final IJstMethod paramFunction) {
 		final List<IJstMethod> overloads = new ArrayList<IJstMethod>(
 				paramFunction.getOverloaded());
 		final List<IJstMethodSortable> sorting = new ArrayList<IJstMethodSortable>(
 				overloads.size());
 		for (IJstMethod overload : overloads) {
 			sorting.add(new IJstMethodSortable(overload));
 		}
 		Collections.sort(sorting);
 		overloads.clear();
 		for (IJstMethodSortable sort : sorting) {
 			overloads.add(sort.getMethod());
 		}
 		return overloads;
 	}
 
 	private static final class IJstMethodSortable implements
 			Comparable<IJstMethodSortable> {
 
 		private final IJstMethod m_method;
 
 		public IJstMethodSortable(final IJstMethod m) {
 			m_method = m;
 		}
 
 		public final IJstMethod getMethod() {
 			return m_method;
 		}
 
 		@Override
 		public int compareTo(IJstMethodSortable o) {
 			return o.m_method.getArgs().size() - m_method.getArgs().size();
 		}
 
 	}
 
 	private static void deriveAnonymousFunctionOverload(
 			final IJstMethod anonymousFunctionOverload,
 			final List<JstArg> inferParams, final IJstType paramFunctionRtnType) {
 		final List<JstArg> anonymousFunctionOverloadParams = anonymousFunctionOverload
 				.getArgs();
 		for (Iterator<JstArg> anonymousFunctionOverloadParamsIt = anonymousFunctionOverloadParams
 				.iterator(), inferParamsIt = inferParams.iterator(); anonymousFunctionOverloadParamsIt
 				.hasNext() && inferParamsIt.hasNext();) {
 			final JstArg anonymousFunctionOverloadParam = anonymousFunctionOverloadParamsIt
 					.next();
 			final JstArg inferParam = inferParamsIt.next();
 			anonymousFunctionOverloadParam.clearTypes();
 			anonymousFunctionOverloadParam.addTypes(inferParam.getTypes());
 		}
 
 		if (anonymousFunctionOverload instanceof JstMethod) {
 			((JstMethod) anonymousFunctionOverload)
 					.setRtnType(paramFunctionRtnType);
 		}
 	}
 
 	// private static JstArg[] replicateJstArgs(IJstMethod
 	// paramFunctionOverload) {
 	// final List<JstArg> originalParams = paramFunctionOverload.getArgs();
 	// final JstArg[] replicated = new JstArg[originalParams.size()];
 	// for(int i = 0; i < replicated.length; i++){
 	// final JstArg originalParam = originalParams.get(i);
 	// replicated[i] = new JstArg(originalParam.getTypes(),
 	// originalParam.getName(), originalParam.isVariable(),
 	// originalParam.isOptional(), originalParam.isFinal());
 	// }
 	// return replicated;
 	// }
 
 	private static void deriveAnonymousFunctionParams(
 			final List<JstArg> params, final List<JstArg> inferParams,
 			final boolean clearTypes) {
 		if (params != null && inferParams != null) {
 			for (int paramIdx = 0, paramLen = params.size(), inferParamLen = inferParams
 					.size(); paramIdx < paramLen && paramIdx < inferParamLen; paramIdx++) {
 
 				final JstArg param = params.get(paramIdx);
 				final JstArg inferParam = inferParams.get(paramIdx);
 				final List<IJstType> inferParamTypes = new ArrayList<IJstType>(
 						param.getTypes());
 				if (clearTypes) {
 					inferParam.clearTypes();
 				}
 				inferParam.addTypes(inferParamTypes);
 			}
 		}
 	}
 
 	/**
 	 * TODO this is incomplete to inject the _invoke_ method in all possible
 	 * places yet
 	 * 
 	 * @author huzhou
 	 * 
 	 */
 	public static class OverwritableFType extends JstProxyType {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 6883185350087966254L;
 
 		private IJstMethod _invoke;
 
 		protected OverwritableFType(final IJstType targetType,
 				final IJstMethod invoke) {
 			super(targetType);
 			_invoke = invoke;
 		}
 
 		@Override
 		public IJstMethod getMethod(final String name) {
 			if ("_invoke_".equals(name)) {
 				return _invoke;
 			}
 			return super.getMethod(name);
 		}
 
 		@Override
 		public IJstMethod getMethod(final String name, boolean isStatic) {
 			if ("_invoke_".equals(name)) {
 				return _invoke;
 			}
 			return super.getMethod(name, isStatic);
 		}
 
 		@Override
 		public IJstMethod getMethod(final String name, boolean isStatic,
 				boolean recursive) {
 			if ("_invoke_".equals(name)) {
 				return _invoke;
 			}
 			return super.getMethod(name, isStatic, recursive);
 		}
 
 		@Override
 		public void accept(IJstNodeVisitor visitor) {
 			return;
 		}
 	}
 
 	public static class OverwritableSynthJstProxyMethod extends
 			SynthJstProxyMethod {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		public OverwritableSynthJstProxyMethod(IJstMethod targetType) {
 			super(targetType);
 		}
 
 		private IJstType _rtnType;
 
 		private List<JstArg> _jstArgs;
 
 		private List<IJstMethod> _overloaded;
 
 		@Override
 		public IJstType getRtnType() {
 			if (_rtnType != null) {
 				return _rtnType;
 			}
 			return super.getRtnType();
 		}
 
 		public void setRtnType(final IJstType rtnType) {
 			_rtnType = rtnType;
 		}
 
 		@Override
 		public List<JstArg> getArgs() {
 			if (_jstArgs != null) {
 				return Collections.unmodifiableList(_jstArgs);
 			}
 			return super.getArgs();
 		}
 
 		public void setArgs(final List<JstArg> args) {
 			_jstArgs = new ArrayList<JstArg>(args);
 		}
 
 		@Override
 		public List<IJstMethod> getOverloaded() {
 			if (_overloaded != null) {
 				return Collections.unmodifiableList(_overloaded);
 			}
 			return super.getOverloaded();
 		}
 
 		public void addOverloaded(final IJstMethod overload) {
 			if (_overloaded == null) {
 				_overloaded = new ArrayList<IJstMethod>(2);
 			}
 			_overloaded.add(overload);
 		}
 	}
 
 	/************************************************************
 	 * MISC HELPERS FOR TYPE DECORATIONS, RESULT TYPE UPDATS ETC.
 	 * **********************************************************
 	 */
 
 	public static boolean isStaticRef(IJstType qualifierType) {
 		if(qualifierType instanceof JstInferredType){
 			qualifierType = ((JstInferredType) qualifierType).getType();
 		}
 		
 		boolean isStatic = qualifierType instanceof IJstRefType
 				|| qualifierType.isFType();
 		return isStatic;
 	}
 
 	/**
 	 * 
 	 * @param resolver
 	 * @param expr
 	 * @param type
 	 */
 	public static void setExprType(final JstExpressionBindingResolver resolver,
 			final IExpr expr,  IJstType type, final GroupInfo groupInfo) {
 		// modification by huzhou to stop resolving of JstAttributedType in
 		// linker
 		if(expr.toExprText().equals("undefined")){
 			type = SimpleLiteral.getUndefinedLiteral().getResultType();
 		}
 		
 		IJstType realType = type instanceof JstAttributedType
 				|| type instanceof JstFuncType ? type : getBindedJstType(type);
 
 		realType = getCorrectType(resolver, realType, groupInfo); // find real
 																	// type in
 																	// cache
 
 		if (expr instanceof JstIdentifier) {
 			JstIdentifier identifier = (JstIdentifier) expr;
 			identifier.setType(realType);
 		} else if (expr instanceof FieldAccessExpr) {
 			FieldAccessExpr identifier = (FieldAccessExpr) expr;
 			identifier.setType(realType);
 		} else if (expr instanceof MtdInvocationExpr) {
 			MtdInvocationExpr mtdExpr = (MtdInvocationExpr) expr;
 			mtdExpr.setResultType(realType);
 		} else if (expr instanceof ArrayAccessExpr) {
 			ArrayAccessExpr arrayAccessExpr = (ArrayAccessExpr) expr;
 			arrayAccessExpr.setType(realType);
 		} else {
 
 			// JstSource source = createSourceRef(expr);
 			// resolver().error("Unprocessed type : " + expr,
 			// type.getSource().getBinding().getName(),
 			// source.getStartOffSet(), source.getEndOffSet(),
 			// source.getRow(), source.getColumn());
 			// // System.err.println("Unprocessed type : " + expr);
 		}
 	}
 
 	public static IJstType getBindedJstType(IJstType type) {
 
 		if (type instanceof JstTypeWithArgs || type instanceof IJstRefType
 				|| type instanceof JstWildcardType) {
 			return type;
 		}
 
 		return getTargetJstType(type);
 	}
 
 	public static IJstType getTargetJstType(IJstType type) {
 
 		while (type instanceof JstProxyType && !(type instanceof IInferred)
 				&& !(type instanceof JstParamType)) {
 			type = ((JstProxyType) type).getType();
 		}
 
 		return type;
 	}
 
 	// find the real type in JstCache
 	public static IJstType getCorrectType(
 			final JstExpressionBindingResolver resolver, final IJstType type,
 			final GroupInfo groupInfo) {
 		if (type == null) {
 			return null;
 		}
 		
 		
 		// bugfix for otype using attributed presentation
 		 if (type instanceof JstMixedType) {
 			for(IJstType mixedType: ((JstMixedType)type).getMixedTypes()){
 				getCorrectType(resolver, mixedType, groupInfo);
 
 			}
 			return type;
 		}
 		else if (type instanceof JstAttributedType) {
 			final IJstNode rtnBinding = look4ActualBinding(resolver, type,
 					groupInfo);
 			if (rtnBinding instanceof IJstOType && rtnBinding != type) {
 				return (IJstOType) rtnBinding;
 			} else if (rtnBinding instanceof JstProxyMethod) {
 				return new JstFuncType((JstProxyMethod) rtnBinding);
 			}
 		} else if (type instanceof JstTypeRefType) {
 			IJstType target = ((JstTypeRefType) type).getReferencedNode();
 			IJstType extended = getExtendedType(target, groupInfo);
 			if (extended != target) {
 				return new JstTypeRefType(extended);
 			}
 		} else if (type instanceof JstFuncType) {
 			updateFunctionType((JstFuncType) type, groupInfo);
 		}
 
 		if (!(type instanceof JstType)) {
 			return type;
 		}
 
 		JstType jstType = (JstType) type;
 		if (!jstType.getStatus().isPhantom()) {
 			if (jstType instanceof JstArray) {
 				updateArrayType((JstArray) jstType, groupInfo);
 			} else if (jstType.isFType()
 					|| jstType instanceof JstFunctionRefType) {
 				updateFunctionType(jstType, groupInfo);
 			}
 			return getExtendedType(jstType, groupInfo);
 		}
 
 		// only check for phantom JstTypes which are replaced by OType in
 		// JstCache
 		JstType typeInCache = JstCache.getInstance().getType(jstType.getName());
 		if (typeInCache != null) {
 			if (typeInCache instanceof JstArray) {
 				updateArrayType((JstArray) typeInCache, groupInfo);
 			} else if (jstType.isFType()
 					|| jstType instanceof JstFunctionRefType) {
 				updateFunctionType(jstType, groupInfo);
 			}
 			return getExtendedType(typeInCache, groupInfo);
 		}
 		return getExtendedType(jstType, groupInfo);
 	}
 
 	public static IJstType getExtendedType(final IJstType targetType,
 			final GroupInfo groupInfo) {
 		if (targetType == null || targetType instanceof JstExtendedType) {
 			return targetType;
 		}
 		if (targetType instanceof JstArray
 				|| targetType instanceof JstVariantType
 				|| targetType instanceof JstMixedType) {
 			return targetType; // TODO
 		}
 		String typeName = targetType.getName();
 		TypeExtensionRegistry ter = TypeExtensionRegistry.getInstance();
 		if (groupInfo != null
 				&& !ter.isNonExtendedType(typeName, groupInfo.getGroupName())) {
 			List<String> baseTypes = new ArrayList<String>();
 			IJstType base = targetType.getExtend();
 			while (base != null) {
 				baseTypes.add(base.getName());
 				if (base != base.getExtend()) {
 					base = base.getExtend();
 				} else {
 					base = null;
 				}
 			}
 			List<String> extensions = ter.getExtension(typeName, baseTypes,
 					groupInfo.getGroupName(), groupInfo.getDependentGroups());
 			if (extensions != null && extensions.size() > 0) {
 				List<IJstType> extTypes = new ArrayList<IJstType>(
 						extensions.size());
 				for (String extName : extensions) {
 					IJstType extType = JstCache.getInstance().getType(extName);
 					if (extType != null) {
 						extTypes.add(extType);
 					}
 				}
 				return new JstExtendedType(targetType, extTypes);
 			}
 		}
 		return targetType;
 	}
 
 	// public static void decorateFTypeWithFunctionType(final
 	// JstExpressionBindingResolver resolver, final IJstType ftype){
 	// if(ftype == null
 	// || !(ftype.isFType() || ftype instanceof JstFunctionRefType)){
 	// throw new
 	// IllegalArgumentException("the vjo type in this context must be a non-null ftype");
 	// }
 	//
 	// final IJstType functionType = getNativeFunctionJstType(resolver);
 	// if(functionType != null
 	// && ftype instanceof JstType){
 	// final JstType fJstType = (JstType)ftype;
 	// for(IJstProperty functionProperty:
 	// functionType.getAllPossibleProperties(false, true)){
 	// if(fJstType.getProperty(functionProperty.getName().getName(), true) ==
 	// null){
 	// final IJstProperty functionPropertyProxy = new
 	// SynthJstProxyProp(functionProperty){
 	// private static final long serialVersionUID = 1L;
 	//
 	// @Override
 	// public boolean isStatic(){
 	// return true;
 	// }
 	// };
 	// fJstType.addProperty(functionPropertyProxy);
 	// }
 	// }
 	// for(IJstMethod functionMethod: functionType.getMethods(false, true)){
 	// if(fJstType.getMethod(functionMethod.getName().getName(), true) == null){
 	// final IJstMethod functionMethodProxy = new
 	// SynthJstProxyMethod(functionMethod){
 	// private static final long serialVersionUID = 1L;
 	//
 	// @Override
 	// public boolean isStatic(){
 	// return true;
 	// }
 	// };
 	// fJstType.addMethod(functionMethodProxy);
 	// }
 	// }
 	// }
 	// }
 
 	public static String getFullName(MtdInvocationExpr mie) {
 		if (mie.getMethodIdentifier() == null) {
 			return "";
 		}
 		IExpr qualifier = mie.getQualifyExpr();
 		String fullName = mie.getMethodIdentifier().toExprText();
 
 		if (qualifier != null) {
 			fullName = qualifier.toExprText() + "." + fullName;
 		}
 		return fullName;
 	}
 
 	public static JstIdentifier getName(IExpr expr) {
 		JstIdentifier name = null;
 
 		if (expr instanceof MtdInvocationExpr) {
 			MtdInvocationExpr expr2 = (MtdInvocationExpr) expr;
 			IExpr mtdId = expr2.getMethodIdentifier();
 
 			if (mtdId instanceof JstIdentifier) {
 				name = (JstIdentifier) mtdId;
 			} else if (mtdId instanceof FieldAccessExpr) {
 				name = ((FieldAccessExpr) mtdId).getName();
 			}
 		} else if (expr instanceof FieldAccessExpr) {
 			name = ((FieldAccessExpr) expr).getName();
 		}
 
 		return name;
 	}
 
 	public static IJstType findFullQualifiedType(
 			final JstExpressionBindingResolver resolver, String fullName,
 			GroupInfo groupInfo) {
 		JstTypeSpaceMgr tsMgr = resolver.getController().getJstTypeSpaceMgr();
 		ITypeSpace<IJstType, IJstNode> ts = tsMgr.getTypeSpace();
 
 		if (groupInfo == null) {
 			return null;
 		}
 
 		List<IJstType> typeList = ts.getVisibleType(fullName,
 				ts.getGroup(groupInfo.getGroupName()));
 
 		if (typeList != null && typeList.size() != 0) {
 			IJstType type = typeList.get(0);
 
 			if (type != null) {
 				return type;
 			}
 		}
 
 		// do an extra lookup in JstCache if type is not found in TS
 		//
 		// IJstType typeInCache = JstCache.getInstance().getType(fullName);
 		//
 		// if (typeInCache != null) {
 		// return typeInCache;
 		// }
 
 		return null;
 	}
 
 	public static void setPackageBindingForQualifier(IExpr expr) {
 		if (expr == null)
 			return;
 
 		if (expr instanceof FieldAccessExpr) {
 			FieldAccessExpr fieldAccessExpr = (FieldAccessExpr) expr;
 			JstIdentifier pkgName = fieldAccessExpr.getName();
 			pkgName.setJstBinding(new JstPackage(pkgName.getName()));
 			pkgName.setType(null);
 			setPackageBindingForQualifier(fieldAccessExpr.getExpr());
 		} else if (expr instanceof JstIdentifier) {
 			JstIdentifier pkgName = (JstIdentifier) expr;
 			pkgName.setJstBinding(new JstPackage(pkgName.getName()));
 			pkgName.setType(null);
 			setPackageBindingForQualifier(pkgName.getQualifier());
 		}
 	}
 
 	public static IJstProperty getProperty(IJstType nodeType, String fieldName,
 			boolean isStatic) {
 		IJstProperty property = nodeType.getProperty(fieldName, isStatic);
 		if (property == null && nodeType.isEnum()) {
 			property = nodeType.getEnumValue(fieldName);
 		}
 		return property;
 	}
 
 	public static boolean isEmptyExpr(IExpr fieldName) {
 		if (fieldName == null) {
 			return true;
 		} else {
 			String fieldTxt = fieldName.toExprText();
 			if (fieldTxt == null || fieldTxt.isEmpty()) {
 				return true;
 			}
 			return false;
 		}
 	}
 
 	/**
 	 * helper for {@link #visitIdentifier(JstIdentifier)}
 	 * 
 	 * @param identifier
 	 * @param parent
 	 */
 	public static boolean isJstIdentifierVisitExcluded(
 			final JstIdentifier identifier, final IJstNode parent) {
 		if (identifier instanceof JstProxyIdentifier) {
 			return true;
 		} else if (parent instanceof FieldAccessExpr) {
 			IExpr qualifier = ((FieldAccessExpr) parent).getExpr();
 			if (qualifier != null && qualifier != identifier) {
 				return true;
 			}
 		} else if (parent instanceof MtdInvocationExpr) {
 			MtdInvocationExpr mie = (MtdInvocationExpr) parent;
 			IExpr qualifier = mie.getQualifyExpr();
 			IExpr mtdIdentifier = mie.getMethodIdentifier();
 
 			// skip method id but need to resolve method args
 			if (mtdIdentifier == identifier && qualifier != null
 					&& qualifier != identifier) {
 				return true;
 			}
 		} else if (parent instanceof NV) {// bugfix by huzhou, no need to bind
 											// obj literal's names here
 			// @see visitNV
 			if (identifier == ((NV) parent).getIdentifier()) {
 				return true;
 			}
 		} else if (parent instanceof AssignExpr
 				&& ((AssignExpr) parent).getLHS() == identifier
 				&& parent.getParentNode() instanceof JstVars) {
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isResolveExcluded(final JstIdentifier identifier,
 			final IJstNode parent) {
 		// we won't further resolve var declarations
 		if (parent != null && parent instanceof AssignExpr) {
 			IJstNode grandParent = parent.getParentNode();
 			if (grandParent != null
 					&& (grandParent instanceof JstVars || grandParent instanceof JstVar)) {
 				return identifier == ((AssignExpr) parent).getLHS();
 			}
 		}
 		// we won't be able to determine ftype's actual usage till
 		// FieldAccessExpr, MtdInvocationExpr is further explored
 		if (identifier.getType() != null && identifier.getType().isFType()) {
 			return true;
 		}
 
 		return false;
 	}
 
 	public static IJstMethod look4EnclosingMethod(final IJstNode child) {
 		if (child == null) {
 			return null;
 		} else if (child instanceof IJstMethod) {
 			return (IJstMethod) child;
 		} else {
 			return look4EnclosingMethod(child.getParentNode());
 		}
 	}
 
 	/**********************************************************
 	 * HELPERS FOR: vjo.make, vjo.mixin handling and vjo.ctype().endType()
 	 * handling ********************************************************
 	 */
 
 	public static interface GlobalNativeTypeInfoProvider {
 		LinkerSymbolInfo findTypeInSymbolMap(final String name,
 				final List<VarTable> varTablesBottomUp);
 	}
 
 	public static IJstType processSyntacticCalls(MtdInvocationExpr mie,
 			String methodName, final GlobalNativeTypeInfoProvider provider) {
 		if (mie.getQualifyExpr() != null
 				&& VjoKeywords.VJO.equals(mie.getQualifyExpr().toExprText())) {
 			final List<IExpr> args = mie.getArgs();
 			if (VjoKeywords.MIXIN.equals(methodName)) {
 				if (args.size() == 2) {
 					IExpr arg1 = args.get(0);
 					IExpr arg2 = args.get(1);
 					IJstType targetType = arg2.getResultType();
 					if (arg1 instanceof SimpleLiteral
 							&& arg2 instanceof JstIdentifier
 							&& targetType != null) {
 						String mtypeName = ((SimpleLiteral) arg1).getValue();
 						IJstType mType = JstCache.getInstance().getType(
 								mtypeName);
 						if (mType != null) {
 							IJstType newType = JstTypeHelper.mixin(targetType,
 									mType);
 							String varName = ((JstIdentifier) arg2).getName();
 							LinkerSymbolInfo info = provider
 									.findTypeInSymbolMap(varName,
 											JstExpressionTypeLinkerHelper
 													.getVarTablesBottomUp(mie));
 							if (info != null) { // replace old type with new
 												// type from mixin
 								info.setType(newType);
 								info.setBinding(newType);
 							}
 						}
 					}
 				}
 			}
 		} else if (VjoKeywords.ENDTYPE.equals(methodName)) {
 			return handleVjoEndType(mie);
 		}
 		return null;
 	}
 
 	protected static IJstType handleVjoEndType(MtdInvocationExpr mie) {
 		MtdInvocationExpr current = mie;
 		IExpr qualifier = mie.getQualifyExpr();
 		IExpr mtdId = mie.getMethodIdentifier();
 
 		while (qualifier != null && (qualifier instanceof MtdInvocationExpr)) {
 			MtdInvocationExpr mieQualifier = (MtdInvocationExpr) qualifier;
 			IExpr nextQualifier = mieQualifier.getQualifyExpr();
 
 			if (nextQualifier != null) {
 				current = mieQualifier;
 				qualifier = nextQualifier;
 				mtdId = current.getMethodIdentifier();
 			} else {
 				break;
 			}
 		}
 //  TODO methods/properties added to anon class are not added to newType
 		// making anon class dynamic for now
 		if (qualifier != null && mtdId != null) {
 			if (VjoKeywords.VJO.equals(qualifier.toExprText())
 					&& VjoKeywords.MAKE.equals(mtdId.toExprText())) {
 				final List<IExpr> args = current.getArgs();
 				if (args.size() >= 2) {
 					IExpr arg2 = args.get(1);
 					if (arg2 instanceof SimpleLiteral) {
 						String typeName = ((SimpleLiteral) arg2).getValue();
 						IJstType parentType = JstCache.getInstance().getType(
 								typeName);
 						if (parentType != null) {
 							IJstType newType = JstTypeHelper.make(parentType);
 							newType.getModifiers().setDynamic();
 							return newType;
 						}
 						// should we make this Undefined ?
 					} else if (arg2 instanceof FieldAccessExpr) {
 						IJstType resultType = ((FieldAccessExpr) arg2)
 								.getResultType();
 						if (resultType instanceof IJstRefType) {
 							IJstType newType = JstTypeHelper
 									.make(((IJstRefType) resultType)
 											.getReferencedNode());
 							newType.getModifiers().setDynamic();
 							return newType;
 						}
 					}
 				}
 			} else {
 				// check if it's a local type creation
 				IJstType mtdResultType = mie.getQualifyExpr().getResultType();
 				if (mtdResultType != null
 						&& mtdResultType.getMethod(VjoKeywords.ENDTYPE) != null) {
 					mtdResultType = mtdResultType
 							.getMethod(VjoKeywords.ENDTYPE).getRtnType();
 				}
 				if (mtdResultType != null
 						&& mtdResultType.getPackage() != null
 						&& "VjoSelfDescribed".equals(mtdResultType.getPackage()
 								.getGroupName())
 						&& !(mtdResultType instanceof IJstRefType)) {
 					// forcing to be typereftype
 					return JstTypeHelper.getJstTypeRefType(mtdResultType);
 				}
 			}
 		}
 		return null;
 	}
 
 	/**********************************************************************
 	 * HELPERS FOR: find types in native type spaces
 	 * ********************************************************************
 	 */
 
 	/**
 	 * GET TYPES FROM NATIVE TS
 	 * 
 	 * @param resolver
 	 * @return
 	 */
 	public static IJstType getNativeArrayJstType(
 			final JstExpressionBindingResolver resolver) {
 		return getNativeTypeFromTS(resolver, "Array");
 	}
 
 	public static IJstType getNativeNumberJstType(
 			final JstExpressionBindingResolver resolver) {
 		return getNativeTypeFromTS(resolver, "Number");
 	}
 
 	public static IJstType getNativeStringJstType(
 			final JstExpressionBindingResolver resolver) {
 		return getNativeTypeFromTS(resolver, "String");
 	}
 
 	public static IJstType getNativeObjectJstType(
 			final JstExpressionBindingResolver resolver) {
 		return getNativeTypeFromTS(resolver, "Object");
 	}
 
 	public static IJstType getNativeBooleanJstType(
 			final JstExpressionBindingResolver resolver) {
 		return getNativeTypeFromTS(resolver,
 				PrimitiveBoolean.class
 						.getSimpleName());
 	}
 
 	public static IJstType getNativeVoidJstType(
 			final JstExpressionBindingResolver resolver) {
 		return getNativeTypeFromTS(resolver, "void");
 	}
 
 	public static IJstType getNativeFunctionJstType(
 			final JstExpressionBindingResolver resolver) {
 		return getNativeTypeFromTS(resolver, "Function");
 	}
 
 	public static IJstType getNativeTypeFromTS(
 			final JstExpressionBindingResolver resolver, final String name) {
 		if (name == null) {
 			return null;
 		}
 
 		IJstType jstType = getNativeTypeFromTS(resolver,
 				JstTypeSpaceMgr.JS_NATIVE_GRP, name);
 		// if (jstType == null) {
 		// jstType = getNativeTypeFromNativeLib(resolver,
 		// JstTypeSpaceMgr.JS_BROWSER_GRP, name);
 		// }
 		// if (jstType == null) {
 		// jstType = getNativeTypeFromNativeLib(resolver,
 		// JstTypeSpaceMgr.VJO_SELF_DESCRIBED, name);
 		// }
 		return jstType;
 	}
 
 	public static IJstType getNativeTypeFromTS(
 			final JstExpressionBindingResolver resolver,
 			final String groupName, final String name) {
 		TypeName typeName = new TypeName(groupName, name);
 
 		JstTypeSpaceMgr tsMgr = resolver.getController().getJstTypeSpaceMgr();
 
 		JstQueryExecutor queryExecutor = tsMgr.getQueryExecutor();
 		return queryExecutor.findType(typeName,
 				tsMgr.getTypeSpace().getGroup(groupName));
 
 		// return queryExecutor.findType(typeName);
 	}
 
 	public static IJstType getNativeElementType(
 			final JstExpressionBindingResolver resolver, final String lastExpr) {
 		// by default
 		IJstType type = null;
 		type = getNativeTypeFromTS(resolver, lastExpr);
 		if (type != null) {
 			type = JstTypeHelper.getJstTypeRefType(type); // wrap in TypeRefType
 		}
 		return type;
 	}
 
 	// exact match means param and argument has the same type
 	// subtype means param type is more general than argument type
 	// implicitConversion suggests that though subtype relation doesn't exist
 	// argument type is still assignable to param type with some implicit
 	// conversion
 	// for example: var b = true;//<boolean; b = 1;//implicitly converting
 	// number to boolean:true
 	// the order suggests that exact match is the highest priority
 	// when comparing 2 overloading signatures
 	// one is a better match than the other iff
 	// for all its param argument matching result, it has a better or equivalent
 	// case
 	public static enum ParamMatchingArgCase {
 		exact, subtype, implicitConversion, object
 	}
 
 	public static class OverloadBestMatchCandidate implements
 			Comparable<OverloadBestMatchCandidate> {
 		private final IJstMethod m_method;
 		private final List<JstArg> m_parameters;
 		private final List<IExpr> m_arguments;
 
 		public OverloadBestMatchCandidate(final IJstMethod method,
 				final List<JstArg> parameter, final List<IExpr> arguments) {
 			assert method != null;
 			assert parameter != null;
 			assert arguments != null;
 			m_method = method;
 			m_parameters = new ArrayList<JstArg>(parameter);
 			m_arguments = new ArrayList<IExpr>(arguments);
 		}
 
 		public IJstMethod getMethod() {
 			return m_method;
 		}
 
 		public List<JstArg> getParameters() {
 			return Collections.unmodifiableList(m_parameters);
 		}
 
 		public List<IExpr> getArguments() {
 			return Collections.unmodifiableList(m_arguments);
 		}
 
 		// for each argument
 		// compare argument and parameter matching case
 		// exact > subtype > implicit conversion
 		// if self's lower index parameter type is better it's better,
 		// generates: -1,
 		// if on the contrary, then other is better, generates: 1
 		// otherwise, check the higher index parameter till exceeding the length
 		@Override
 		public int compareTo(final OverloadBestMatchCandidate other) {
 			return compareToBeginsAt(0, m_arguments, m_parameters,
 					other.m_parameters);
 		}
 
 		private int compareToBeginsAt(final int argumentIndex,
 				final List<IExpr> arguments, final List<JstArg> selfParameters,
 				final List<JstArg> otherParameters) {
 
 			// bugfix by huzhou@ebay.com
 			// though argumentIndex exceeds the arguments length
 			// we'd like to know if parameter size exceeds or not
 			// exceeding means there's variable lengthed parameter at this
 			// position
 			// when otherParameters length is greater than self's
 			// self is a better match
 			if (argumentIndex >= arguments.size()) {
 				return selfParameters.size() - otherParameters.size();
 			}
 
 			final IExpr argument = m_arguments.get(argumentIndex);
 			final JstArg selfParameter = m_parameters.get(argumentIndex);
 			final JstArg otherParameter = otherParameters.get(argumentIndex);
 
 			final ParamMatchingArgCase selfCase = argMatchingParam(argument,
 					selfParameter);
 			final ParamMatchingArgCase otherCase = argMatchingParam(argument,
 					otherParameter);
 			final int compared = selfCase.compareTo(otherCase);
 
 			return compared == 0 ? compareToBeginsAt(argumentIndex + 1,
 					arguments, selfParameters, otherParameters) : compared;
 		}
 
 		@Override
 		public boolean equals(final Object other) {
 			if (other instanceof OverloadBestMatchCandidate) {
 				final OverloadBestMatchCandidate otherScoreEntry = (OverloadBestMatchCandidate) other;
 				return m_method == otherScoreEntry.m_method
 						&& m_parameters == otherScoreEntry.m_parameters
 						&& m_arguments == otherScoreEntry.m_arguments;
 			}
 			return false;
 		}
 
 		@Override
 		public int hashCode() {
 			return m_method.hashCode() + m_parameters.hashCode()
 					+ m_arguments.hashCode();
 		}
 	}
 
 	public static IJstType getNativeUndefinedType(
 			JstExpressionBindingResolver resolver) {
 		return getNativeTypeFromTS(resolver, "Undefined");
 	}
 }
