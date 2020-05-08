 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.translator;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.eclipse.mod.wst.jsdt.core.ast.IASTNode;
 import org.eclipse.mod.wst.jsdt.core.ast.IAbstractVariableDeclaration;
 import org.eclipse.mod.wst.jsdt.core.ast.IArgument;
 import org.eclipse.mod.wst.jsdt.core.ast.IExpression;
 import org.eclipse.mod.wst.jsdt.core.ast.IProgramElement;
 import org.eclipse.mod.wst.jsdt.core.ast.IStatement;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.AbstractVariableDeclaration;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.EmptyExpression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.Expression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.FieldReference;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.FunctionExpression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.Literal;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.MessageSend;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.MethodDeclaration;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.NullLiteral;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.SingleNameReference;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.Statement;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.UndefinedLiteral;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.common.ScopeId;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstOType;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.JstCommentLocation;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.declaration.JstAnnotation;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstArray;
 import org.eclipse.vjet.dsf.jst.declaration.JstAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstConstructor;
 import org.eclipse.vjet.dsf.jst.declaration.JstFactory;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncArgAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncScopeAttributedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstMixedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstModifiers;
 import org.eclipse.vjet.dsf.jst.declaration.JstName;
 import org.eclipse.vjet.dsf.jst.declaration.JstParamType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPotentialAttributedMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstPotentialOtypeMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyType;
 import org.eclipse.vjet.dsf.jst.declaration.JstSynthesizedMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeReference;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeWithArgs;
 import org.eclipse.vjet.dsf.jst.declaration.JstVariantType;
 import org.eclipse.vjet.dsf.jst.declaration.JstWildcardType;
 import org.eclipse.vjet.dsf.jst.declaration.SynthJstProxyMethod;
 import org.eclipse.vjet.dsf.jst.declaration.SynthJstProxyProp;
 import org.eclipse.vjet.dsf.jst.expr.BoolExpr;
 import org.eclipse.vjet.dsf.jst.expr.CastExpr;
 import org.eclipse.vjet.dsf.jst.expr.FuncExpr;
 import org.eclipse.vjet.dsf.jst.meta.ArgType;
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta;
 import org.eclipse.vjet.dsf.jst.meta.JsAnnotation;
 import org.eclipse.vjet.dsf.jst.meta.JsAnnotation.JsAnnotationType;
 import org.eclipse.vjet.dsf.jst.meta.JsCommentMetaNode;
 import org.eclipse.vjet.dsf.jst.meta.JsType;
 import org.eclipse.vjet.dsf.jst.meta.JsTypingMeta;
 import org.eclipse.vjet.dsf.jst.stmt.BlockStmt;
 import org.eclipse.vjet.dsf.jst.stmt.ExprStmt;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.token.IStmt;
 import org.eclipse.vjet.dsf.jst.util.JstTypeHelper;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsAttributed;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsCommentMeta;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsFuncArgAttributedType;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsFuncScopeAttributedType;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsFuncType;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsMixinType;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsParam;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsVariantType;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.ParseException;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.VjComment;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.JstSourceUtil;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst.ArgumentTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst.BaseAst2JstTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst.FunctionExpressionTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst.OverloadInfo;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst.TranslatorFactory;
 import org.eclipse.vjet.vjo.meta.VjoKeywords;
 
 public class TranslateHelper {
 
 	private static final String EMPTYSTRING = "";
 	private static final String COMMA = ",";
 	private static final String LT = "<";
 	private static final String GT = ">";
 
 	private static final Pattern pattern = Pattern.compile(".[\\s]*");
 
 	public static final String MISSING_TOKEN = "$missing$";
 	private static final String DEFAULT_ARG_PREFIX = "p";
 
 	public static String getMethodName(MessageSend el) {
 		if (el != null && el.selector != null) {
 			IExpression receiver = el.getReceiver();
 			if (receiver instanceof MessageSend) {
 				return getMethodName((MessageSend) receiver);
 			} else {
 				return String.valueOf(el.selector);
 			}
 		}
 		return null;
 	}
 
 	public static String getStringToken(IProgramElement programElement) {
 
 		String token = null;
 
 		if (programElement instanceof SingleNameReference) {
 			if (((SingleNameReference) programElement).getToken() == null) {
 				token = new String();
 			} else {
 				token = new String(
 						((SingleNameReference) programElement).getToken());
 			}
 		} else if (programElement instanceof FieldReference) {
 			if (((FieldReference) programElement).token == null) {
 				token = new String();
 			} else {
 				token = new String(((FieldReference) programElement).token);
 			}
 		} else if (programElement instanceof MessageSend) {
 			if (((MessageSend) programElement).getSelector() == null) {
 				token = new String();
 			} else {
 				token = new String(((MessageSend) programElement).getSelector());
 			}
 		} else if (programElement instanceof EmptyExpression) {
 			token = new String(); // return an empty string
 		}
 
 		if (token.equals(MISSING_TOKEN)) {
 			token = new String();
 		}
 
 		return token;
 	}
 
 	static boolean isVjoFieldRef(IProgramElement programElement) {
 		return (programElement instanceof FieldReference)
 				&& getStringToken(programElement).equals(VjoKeywords.VJO);
 	}
 
 	public static JstSource getSource(IASTNode field,
 			IFindTypeSupport.ILineInfoProvider util) {
 		if (field instanceof IAbstractVariableDeclaration) {
 			return getSource((IAbstractVariableDeclaration) field, util);
 		}
 		return new JstSource(JstSource.JS, util.line(field.sourceStart()),
 				util.col(field.sourceStart()), field.sourceEnd()
 						- field.sourceStart() + 1, field.sourceStart(),
 				field.sourceEnd());
 	}
 
 	public static JstSource getSourceFunc(MethodDeclaration field,
 			IFindTypeSupport.ILineInfoProvider util) {
 		return new JstSource(JstSource.JS, util.line(field.sourceStart()),
 				util.col(field.sourceStart()), field.bodyStart
 						- field.sourceStart() + 1, field.sourceStart(),
 				field.bodyStart);
 	}
 
 	public static JstSource getSource(IAbstractVariableDeclaration field,
 			IFindTypeSupport.ILineInfoProvider util) {
 		int sourceEnd = field.sourceEnd();
 		AbstractVariableDeclaration aField = (AbstractVariableDeclaration) field;
 		int declEnd = ((AbstractVariableDeclaration) aField).declarationEnd;
 		int end = sourceEnd > declEnd ? sourceEnd : declEnd;
 		return new JstSource(JstSource.JS, util.line(field.sourceStart()),
 				util.col(field.sourceStart()), end - field.sourceStart() + 1,
 				field.sourceStart(), end);
 	}
 
 	public static JstSource getIdentifierSource(
 			IAbstractVariableDeclaration field,
 			IFindTypeSupport.ILineInfoProvider util) {
 		return new JstSource(JstSource.JS, util.line(field.sourceStart()),
 				util.col(field.sourceStart()), field.sourceEnd()
 						- field.sourceStart() + 1, field.sourceStart(),
 				field.sourceEnd());
 	}
 
 	public static JstSource getSource(int fullEnd, int nameLength) {
 		return new JstSource(JstSource.JS, -1, -1, nameLength, fullEnd
 				- nameLength + 1, fullEnd);
 	}
 
 	public static void setTypeRefSource(BaseJstNode refType, IJsCommentMeta meta) {
 		JsTypingMeta typing = meta.getTyping();
 		if (typing == null || typing.getTypingToken() == null)
 			return;
 		int commentOffset = meta.getBeginOffset() + 1;// added 1 to fix comment
 														// offset
 		int retTypeBeginOffset = typing.getTypingToken().beginColumn
 				+ commentOffset;
 		int retTypeEndOffset = typing.getTypingToken().endColumn
 				+ commentOffset;
 
 		JstSource source = new JstSource(JstSource.JS, -1, -1, retTypeEndOffset
 				- retTypeBeginOffset + 1, retTypeBeginOffset, retTypeEndOffset);
 		if (refType != null) {
 			refType.setSource(source);
 		}
 	}
 
 	public static void setModifiersFromMeta(IJsCommentMeta meta,
 			JstModifiers modifiers) {
 		JstModifiers mods = meta.getModifiers();
 		modifiers.merge(JstModifiers.getFlag(mods.getAccessScope()));
 		if (mods.isFinal()) {
 			modifiers.merge(JstModifiers.FINAL);
 		}
 		if (mods.isAbstract()) {
 			modifiers.merge(JstModifiers.ABSTRACT);
 		}
 	}
 
 	public static void addSourceInfo(IASTNode field, BaseJstNode jstType,
 			IFindTypeSupport.ILineInfoProvider util) {
 		JstSource source = getSource(field, util);
 		jstType.setSource(source);
 	}
 
 	public static JstSource getMethodSource(char[] originalSource,
 			IFindTypeSupport.ILineInfoProvider util, int sourceStart, int sourceEnd, int length) {
 		// TODO when would this ever happen?
 		if (originalSource == null) {
 			return TranslateHelper.createJstSource(util, sourceEnd
 					- sourceStart + 1, sourceStart, sourceEnd);
 		}
 		int start = 0;
 		for (int i = sourceStart; i < sourceEnd; i++) {
 			if (!Character.isWhitespace(originalSource[i])
 					&& originalSource[i] != '.') {
 				start = i;
 				break;
 			}
 		}
 
 		return TranslateHelper.createJstSource(util, length, start, start
 				+ length - 1);
 	}
 
 	public static BoolExpr buildCondition(IExpr condition) {
 		if (condition instanceof BoolExpr) {
 			return (BoolExpr) condition;
 		} else {
 			return new BoolExpr(condition);
 		}
 	}
 
 	public static JstMethod createRealMethod(IExpression fieldName,
 			JstMethod jstMethod, TranslateCtx translateContext) {
 		JstMethod newMethod;
 		String name = fieldName.toString();
 
 		if (jstMethod == null) {
 			return null;
 		}
 
 		JstArg[] argsArray = null;
 		if (jstMethod.getArgs() != null) {
 			List<JstArg> args = jstMethod.getArgs();
 			argsArray = args.toArray(new JstArg[args.size()]);
 		}
 
 		if (translateContext.getCurrentScope() == ScopeIds.PROTOS
 				&& VjoKeywords.CONSTRUCTS.equals(name)
 				&& !(jstMethod instanceof JstConstructor)) {
 			newMethod = new JstConstructor(jstMethod.getModifiers(), argsArray);
 			// read overloaded methods from jstMethod and populate newMethod
 			List<IJstMethod> omtds = jstMethod.getOverloaded();
 			if (omtds != null) {
 				for (IJstMethod omtd : omtds) {
 					List<JstArg> omargs = omtd.getArgs();
 					JstArg[] omargsArray = omargs.toArray(new JstArg[omargs
 							.size()]);
 					// JstArg[] omargsArray = omargs.toArray(new
 					// JstArg[args.size()]);
 					newMethod.addOverloaded(new JstConstructor(omtd
 							.getModifiers(), omargsArray));
 				}
 			}
 			newMethod.setSource(BaseAst2JstTranslator.createSource(fieldName
 					.sourceStart(), jstMethod.getSource().getEndOffSet(),
 					translateContext.getSourceUtil()));
 
 			// copy return type
 			IJstType retType = jstMethod.getRtnType();
 			if (retType != null) {
 				newMethod.setRtnType(retType);
 				TranslateHelper.setReferenceSource(newMethod, jstMethod
 						.getRtnTypeRef().getSource());
 			}
 
 			// copy all statements
 			JstBlock body = newMethod.getBlock(true);
 			for (IStmt statement : jstMethod.getBlock(true).getStmts()) {
 				body.addStmt(statement);
 			}
 
 			List<? extends IJstNode> list = jstMethod.getBlock(true)
 					.getChildren();
 			IJstNode[] jstNodes = list.toArray(new IJstNode[list.size()]);
 			for (int i = 0; i < jstNodes.length; i++) {
 				IJstNode jstNode = jstNodes[i];
 				if (jstNode instanceof BaseJstNode) {
 					BaseJstNode node = (BaseJstNode) jstNode;
 					node.setParent(body);
 				}
 			}
 		} else {
 			newMethod = jstMethod;
 			newMethod.setName(name);
 			// set the same method name for all overloaded methods.
 			if (jstMethod.getOverloaded() != null) {
 				for (IJstMethod omtd : jstMethod.getOverloaded()) {
 					JstMethod jstMethod2 = (JstMethod) omtd;
 					jstMethod2.setName(name);
 				}
 			}
 			// ensure that block is created
 			newMethod.getBlock(true);
 		}
 
 		newMethod.getName().setSource(
 				TranslateHelper.getSource(fieldName,
 						translateContext.getSourceUtil()));
 
 		return newMethod;
 	}
 
 	public static boolean isWhitespacesFollowsDotSeq(char[] chunk) {
 
 		return pattern.matcher(new String(chunk)).find();
 
 	}
 
 	public static boolean isLetter(char c) {
 		return c >= 'A' && c <= 'z';
 	}
 
 	public static String calculatePrefix(char[] chars, int endPos,
 			int sourceStart) {
 		StringBuffer buffer = new StringBuffer();
 		while (sourceStart < endPos) {
 			char ch = chars[endPos - 1];
 			if (ch != '(') {
 				buffer.append(ch);
 			} else {
 				break;
 			}
 			endPos--;
 		}
 		buffer.reverse();
 		return buffer.toString();
 	}
 
 	/**
 	 * refactored by huzhou to handle array, generics in a general fashion
 	 * 
 	 * @param findSupport
 	 * @param jsTypingMeta
 	 * 
 	 * @param meta
 	 *            (to provide source information) optional (null)
 	 * @return
 	 */
 	public static IJstType findType(final IFindTypeSupport findSupport,
 			final JsTypingMeta jsTypingMeta, final IJsCommentMeta originalMeta) {
 		if (jsTypingMeta == null) {
 			return null;
 		}
 
 		IJstType jstType = null;
 		// handling simple jsType and generics
 		if (jsTypingMeta instanceof JsType) {
 			if (((JsType) jsTypingMeta).isAliasRef()) {
 				jstType = JstCache.getInstance().getAliasType(
 						jsTypingMeta.getType(), true);
 			} else {
 
 				jstType = findType(findSupport, (JsType) jsTypingMeta);
 			}
 		}
 		// handling floating function types
 		else if (jsTypingMeta instanceof JsFuncType) {
 			final JsFuncType jsFuncType = (JsFuncType) jsTypingMeta;
 			final IJstMethod synthesizedFunction = MethodTranslateHelper
 					.createJstSynthesizedMethod(jsFuncType, findSupport,
 							jsFuncType.getFuncName());
 			jstType = createJstFuncType(findSupport, synthesizedFunction);
 		}
 		// handling attributed type
 		else if (jsTypingMeta instanceof JsAttributed) {
 			final JsAttributed jsAttributed = (JsAttributed) jsTypingMeta;
 			final JsType attributor = jsAttributed.getAttributor();
 			final IJstType attributorType = attributor == null ? getGlobalType()
 					: findType(findSupport, attributor, originalMeta);
 			final String attributeName = jsAttributed.getName();
 			if (MISSING_TOKEN.equals(attributeName) && originalMeta != null) {// we
 																				// temporarily
 																				// need
 																				// the
 																				// original
 																				// meta
 																				// to
 																				// find
 																				// the
 																				// position
 																				// of
 																				// error
 				// report error
 				final IJstType translatingType = findSupport.getCurrentType();
 				findSupport.getErrorReporter().error(
 						"Cannot translate attributed meta: " + jsTypingMeta
 								+ " to attributed type declaration " + " in "
 								+ TranslateHelper.class,
 						translatingType != null ? translatingType.getName()
 								: "unknown type",
 						originalMeta.getBeginOffset(),
 						originalMeta.getEndOffset(),
 						findSupport.getLineInfoProvider().line(
 								originalMeta.getBeginOffset()),
 						findSupport.getLineInfoProvider().col(
 								originalMeta.getBeginOffset()));
 			} else {
 				final boolean isStatic = !jsAttributed.isInstance();
 				jstType = new JstAttributedType(attributorType, attributeName,
 						isStatic);
 			}
 		} else if (jsTypingMeta instanceof JsVariantType) {
 			jstType = findType(findSupport, (JsVariantType) jsTypingMeta);
 		} else if (jsTypingMeta instanceof JsMixinType) {
 			jstType = findType(findSupport, (JsMixinType) jsTypingMeta);
 		} else if (jsTypingMeta instanceof JsFuncScopeAttributedType) {
 			jstType = findType(findSupport,
 					(JsFuncScopeAttributedType) jsTypingMeta);
 		} else if (jsTypingMeta instanceof JsFuncArgAttributedType) {
 			jstType = findType(findSupport,
 					(JsFuncArgAttributedType) jsTypingMeta);
 		}
 
 		// handling error case
 		if (jstType == null) {
 			return null;// throw exception?
 		}
 
 		// handling arrays
 		final int dimension = jsTypingMeta.getDimension();
 		if (dimension > 0) {
 			IJstType arrayType = jstType;
 			for (int i = 0, d = dimension; i < d; i++) {
 				arrayType = new JstArray(arrayType);
 			}
 			jstType = arrayType;
 		}
 
 		return jstType;
 	}
 
 	private static IJstType findType(final IFindTypeSupport findSupport,
 			final JsType typing) {
 		IJstType jstType = findType(findSupport, typing.getType());
 		if (typing.getArgs().size() > 0) {
 			final JstTypeWithArgs typeWithArgs = new JstTypeWithArgs(jstType);
 			addArgsToType(findSupport, typeWithArgs, typing);
 			jstType = typeWithArgs;
 		}
 		// allow JstTypeWithArgs to be referenced type
 		if (typing.isTypeRef()) {
 			jstType = JstTypeHelper.getJstTypeRefType(jstType);
 		}
 		return jstType;
 	}
 
 	private static IJstType findType(final IFindTypeSupport findSupport,
 			final JsVariantType typing) {
 		List<IJstType> types = new ArrayList<IJstType>(3);
 		for (JsTypingMeta t : ((JsVariantType) typing).getTypes()) {
 			types.add(findType(findSupport, t, null));
 		}
 		return new JstVariantType(types);
 	}
 
 	private static IJstType findType(final IFindTypeSupport findSupport,
 			final JsMixinType typing) {
 		List<IJstType> types = new ArrayList<IJstType>(3);
 		for (JsTypingMeta t : ((JsMixinType) typing).getTypes()) {
 			types.add(findType(findSupport, t, null));
 		}
 		return new JstMixedType(types);
 	}
 
 	private static IJstType findType(final IFindTypeSupport findSupport,
 			final JsFuncScopeAttributedType typing) {
 		return new JstFuncScopeAttributedType();
 	}
 
 	private static IJstType findType(final IFindTypeSupport findSupport,
 			final JsFuncArgAttributedType typing) {
 		return new JstFuncArgAttributedType(typing.getArgIndex());
 	}
 
 	public static JstFuncType createJstFuncType(IFindTypeSupport ctx,
 			final IJstMethod synthesizedFunction) {
 		return new JstFuncType(synthesizedFunction);
 	}
 
 	/**
 	 * Find unresolved type
 	 * 
 	 * @param findSupport
 	 * @param name
 	 * @return
 	 */
 	// TODO moving to resolver utilities
 	public static IJstType findType(IFindTypeSupport findSupport, String name) {
 		name = name.trim();
 
 		if (name.equals(EMPTYSTRING)) {
 			return null;
 		}
 
 		IJstType type = null;
 		int start = name.indexOf("<");
 		if (start > 1 && name.charAt(name.length() - 1) == '>') {
 			try {
 				final JsCommentMeta commentMeta = VjComment.parse("//<" + name);
 				final JsTypingMeta typingMeta = commentMeta.getTyping();
 				return findType(findSupport, typingMeta, commentMeta);
 			} catch (ParseException e) {
 				name = name.substring(0, start);
 			}
 		}
 
 		type = findTypeRefInCurrentType(findSupport, name);
 
 		if (type == null && findSupport != null) {
 			type = findKnownType(findSupport.getCurrentType(), name);
 		}
 
 		if (name.equals("java.lang.String")) {
 			type = JstCache.getInstance().getType(null, "String");
 			return type;
 		}
		if(name.startsWith("js.")){
			String suffix =name.substring(name.indexOf("js.")+3);
			type = JstCache.getInstance().getType(null,suffix);
		}
 
 		if (type == null) {
 			String fullyQualifiedName = name;
 			if (findSupport != null) {
 				fullyQualifiedName = getFullyQualifiedName(
 						findSupport.getCurrentType(), name);
 			}
 			type = JstFactory.getInstance().createJstType(fullyQualifiedName,
 					true);
 		}
 
 		return type;
 	}
 
 	private static IJstType findTypeRefInCurrentType(
 			IFindTypeSupport findSupport, String longname) {
 
 		if (findSupport == null) {
 			return null;
 		}
 
 		// look through the mapped type name table and see which type name
 		// matches
 		//
 		return findSupport.findTypeByName(longname);
 	}
 
 	protected static IJstType findInnerOrOType(IJstType parentType,
 			String longName) {
 		if (longName == null || longName.length() == 0) {
 			return null;
 		}
 
 		int firstDotIdx = longName.indexOf('.');
 		String shortName = null;
 
 		if (firstDotIdx == -1) {
 			shortName = longName;
 		} else {
 			shortName = longName.substring(0, firstDotIdx); // get short name
 															// before first '.'
 		}
 
 		if (shortName != null && shortName.length() != 0) {
 			IJstType subType = parentType.getOType(shortName);
 
 			if (subType == null) {
 				subType = parentType.getEmbededType(shortName);
 			}
 
 			if (subType != null) {
 
 				if (firstDotIdx == -1) {
 					return subType;
 				} else if (firstDotIdx + 1 < longName.length()) {
 					return findInnerOrOType(subType,
 							longName.substring(firstDotIdx + 1));
 				}
 			}
 		}
 
 		return null;
 	}
 
 	protected static IJstType findOuterType(IJstType currentType, String name) {
 		if (currentType == null || name == null) {
 			return null;
 		}
 
 		if (name.equals(currentType.getSimpleName())) {
 			return currentType;
 		}
 		if (currentType.getOType(name) != null) {
 			return currentType.getOType(name);
 		}
 		for (IJstType importedType : currentType.getImports()) {
 			if (name.equals(importedType.getSimpleName())) {
 				return importedType;
 			}
 			// adding alias support
 			if (name.equals(importedType.getAlias())) {
 				return importedType;
 			}
 		}
 		for (IJstType importedType : currentType.getSatisfies()) {
 			if (name.equals(importedType.getSimpleName())) {
 				return importedType;
 			}
 			// adding alias support
 			if (name.equals(importedType.getAlias())) {
 				return importedType;
 			}
 		}
 		for (IJstType importedType : currentType.getExtends()) {
 			if (name.equals(importedType.getSimpleName())) {
 				return importedType;
 			}
 			// adding alias support
 			if (name.equals(importedType.getAlias())) {
 				return importedType;
 			}
 		}
 		for (IJstType importedType : currentType.getInactiveImports()) {
 			if (name.equals(importedType.getSimpleName())) {
 				return importedType;
 			}
 			// TODO find out if we need to fix this?
 			if (name.equals(importedType.getAlias())) {
 				return importedType;
 			}
 		}
 
 		return null;
 	}
 
 	private static String getFullyQualifiedName(IJstType currentType,
 			String name) {
 
 		if (currentType instanceof IJstOType) {
 			currentType = (JstType) currentType.getParentNode();
 		}
 
 		int start = name.indexOf(".");
 		String simpleName = name;
 
 		if (start != -1) {
 			String[] names = name.split("\\.");
 			String outerTypeName = names[0];
 			simpleName = names[0];
 			IJstType outerType = currentType;
 			IJstType type = null;
 
 			while (outerType != null) {
 
 				if ((type = findOuterType(outerType, outerTypeName)) != null) {
 					String fullyQualifiedName = type.getName();
 
 					return fullyQualifiedName + name.substring(start);
 				}
 
 				outerType = outerType.getOuterType();
 			}
 		}
 
 		// inner type without qualifier
 		if (currentType != null) {
 
 			IJstType innerType = currentType.getEmbededType(simpleName);
 			if (innerType == null) { // try otype
 				innerType = currentType.getOType(simpleName);
 			}
 
 			if (innerType != null) {
 				return currentType.getName() + "." + name;
 			}
 		}
 
 		return name;
 	}
 
 	private static IJstType getParamType(final IJstType ownerType,
 			final String name) {
 		if (name == null) {
 			throw new IllegalArgumentException(
 					"param type's name shouldn't be null");
 		}
 
 		JstParamType paramType = null;
 		final List<JstParamType> m_paramTypes = ownerType.getParamTypes();
 		if (m_paramTypes != null) {
 			for (JstParamType it : m_paramTypes) {
 				if (it.getName().equals(name)) {
 					paramType = it;
 					break;
 				}
 			}
 		}
 
 		if (paramType == null && ownerType.isEmbededType()) {
 			return getParamType(ownerType.getOuterType(), name);
 		} else {
 			return paramType;
 		}
 	}
 
 	private static IJstType findKnownType(IJstType currentType, String name) {
 
 		IJstType type = null;
 		if (currentType instanceof IJstOType) {
 			currentType = (JstType) currentType.getParentNode();
 		}
 
 		if (currentType != null) {
 			IJstType paramType = getParamType(currentType, name);
 
 			if (paramType != null) {
 				return paramType;
 			}
 		}
 
 		String[] names = name.split("\\.");
 
 		// Simple name look up is done here
 		// comments can have simple name rather than fully qualified name
 		// TODO support aliases not just
 		if (name.indexOf(".") == -1) { // short name
 			if (currentType != null) {
 				type = findOuterType(currentType, name);
 
 				if (type != null) {
 					return type;
 				}
 			}
 		} else if (currentType != null && names.length == 2) { // check in
 																// otypes
 			IJstType otype = getTypeFromInactive(name, currentType);
 			if (otype != null) {
 				return otype;
 			}
 		}
 
 		String longName = JstCache.getInstance().getTypeSymbolMapping(name);
 
 		if (longName != null && longName.length() > 0) {
 			name = longName;
 		}
 
 		type = JstCache.getInstance().getType(
 				getFullyQualifiedName(currentType, name));
 
 		return type;
 	}
 
 	// TODO moving to resolver utilities
 	public static IJstTypeReference getType(TranslateCtx ctx, Literal literal) {
 		assert literal != null;
 		String typeName = JstUtil.getCorrectName(literal);
 		// no beginLine and beginColumn in literal object => they are hardcoded
 		// to -1
 		JstSource source = new JstSource(JstSource.JS, -1, -1,
 				typeName.length(), literal.sourceStart + 1, literal.sourceStart
 						+ typeName.length());
 
 		return getType(ctx, typeName, source);
 	}
 
 	// TODO moving to resolver utilities
 	public static IJstTypeReference getType(TranslateCtx ctx, String typeName,
 			JstSource source) {
 		IJstType type = TranslateHelper.getJstType(TranslateHelper.findType(
 				ctx, typeName));
 		// TODO - Justin refactor remove these logic breaks VJET
 		// if (!type.getStatus().hasDecl()) {
 		// try {
 		// //tmp logic to on-demain load the missing type
 		// URL url =ctx.getSourceLocator().getSourceUrl(type.getName());
 		// if (url!=null) {
 		// type = (JstType)new VjoParser()
 		// .parse(ctx.getGroup(), url, ctx.isSkiptImplementation());
 		// }
 		// }
 		// catch (Exception e) {
 		// }
 		// }
 		return TranslateHelper.createRef(type, source);
 	}
 
 	public static IJsCommentMeta getMatchingMetaWithAstFunction(
 			List<IJsCommentMeta> metaArr, Expression astFunctionExpression) {
 		if (astFunctionExpression instanceof FunctionExpression) {
 
 			for (IJsCommentMeta meta : metaArr) {
 				IArgument[] astArgs = ((FunctionExpression) astFunctionExpression)
 						.getMethodDeclaration().getArguments();
 				List<JsParam> metaArgs = getParams(meta);
 				if (astArgs == null
 						&& (metaArgs == null || metaArgs.size() == 0)) {
 					return meta;
 				} else if (astArgs != null && metaArgs != null
 						&& astArgs.length == metaArgs.size()) {
 					return meta;
 				}
 			}
 		}
 
 		// take the meta which has maximum number of parameters
 		IJsCommentMeta maxMeta = null;
 		int maxParamCount = 0;
 		List<JsParam> params = null;
 		for (IJsCommentMeta meta : metaArr) {
 			if (maxMeta == null) {
 				maxMeta = meta;
 				params = getParams(meta);
 				if (params != null) {
 					maxParamCount = params.size();
 				}
 			} else {
 				params = getParams(meta);
 				if (params.size() > maxParamCount) {
 					maxParamCount = params.size();
 					maxMeta = meta;
 				}
 			}
 		}
 		return maxMeta;
 	}
 
 	public static OverloadInfo determineOverloadCount(List<JsParam> params) {
 		OverloadInfo info = new OverloadInfo();
 		if (params == null) {
 			return info;
 		}
 		int optionalCount = 0;
 		int reqCount = 0;
 		for (JsParam p : params) {
 			if (p.isOptional()) {
 				optionalCount++;
 			} else {
 				reqCount++;
 			}
 		}
 		if (optionalCount > 0) {
 			info.requiredParams = reqCount;
 			info.totalOverloads = reqCount + optionalCount + 1;
 		}
 		return info;
 	}
 
 	private static void fixModifiersForDispatchMethod(JstMethod jstMethod) {
 		if (jstMethod.isDispatcher()) {
 			final JstModifiers dispatcherModifiers = jstMethod.getModifiers();
 			for (IJstMethod overload : jstMethod.getOverloaded()) {
 				dispatcherModifiers.merge(overload.getModifiers().getFlags());
 			}
 		}
 	}
 
 	private static void fixRtnTypeForDispatchMethod(JstMethod jstMethod) {
 		if (jstMethod.isDispatcher()) {
 			for (IJstMethod overload : jstMethod.getOverloaded()) {
 				if (overload.getRtnType() != null) {
 					jstMethod.setRtnType(overload.getRtnType());
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Update the argument types and return type of a dispatcher method
 	 * 
 	 * @param jstMethod
 	 *            a dispatcher method
 	 */
 	public static void fixArgsForDispatchMethod(JstMethod jstMethod) {
 		for (int i = 0; i < jstMethod.getArgs().size(); i++) {
 			JstArg arg = jstMethod.getArgs().get(i);
 			List<IJstTypeReference> types = new ArrayList<IJstTypeReference>();
 			for (IJstMethod mtd : jstMethod.getOverloaded()) {
 				if (i < mtd.getArgs().size()) {
 					JstArg a = mtd.getArgs().get(i);
 					if (!doesTypeExist(types, a.getTypeRef()
 							.getReferencedType())) {
 						types.add(a.getTypeRef());
 					}
 				}
 			}
 			if (!types.isEmpty()) {
 				arg.clearTypes();
 				arg.addTypesRefs(types);
 			}
 		}
 
 		// Attempt to set the return type for dispatcher method
 		boolean olRetTypeSame = true;
 		boolean optionalReturn = false;
 		IJstType rtnType = null;
 		for (IJstMethod mtd : jstMethod.getOverloaded()) {
 
 			if (mtd.isReturnTypeOptional()) {
 				optionalReturn = true;
 			}
 
 			IJstType currType = mtd.getRtnType();
 			if (rtnType != null && currType != null) {
 				if (!rtnType.getName().equals(currType.getName())) {
 					olRetTypeSame = false;
 					break;
 				}
 			}
 			rtnType = currType;
 		}
 
 		jstMethod.setReturnOptional(optionalReturn);
 		// If all overloaded methods have the same return type, set the
 		// dispatcher method return
 		// type the same. Otherwise, let the linker figure it out.
 		if (olRetTypeSame && rtnType != null) {
 			jstMethod.setRtnType(rtnType);
 		} else {
 			// let linker figure it out
 			jstMethod.setRtnType(null);
 		}
 	}
 
 	private static boolean doesTypeExist(List<IJstTypeReference> types,
 			IJstType jstType) {
 		for (int i = 0; i < types.size(); i++) {
 			if (null != jstType.getName()
 					&& jstType.getName().equals(
 							types.get(i).getReferencedType().getName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	// private static JstMethod createJstMethod(Expression
 	// astFunctionExpression,
 	// IJsCommentMeta meta, TranslateCtx ctx, boolean useJsAnnotForArgs,final
 	// String methName, JstMethod parentMethod) {
 	// return createJstMethod(astFunctionExpression, meta, ctx,
 	// useJsAnnotForArgs, methName, true, parentMethod);
 	// }
 	//
 	// private static JstMethod createJstMethod(Expression
 	// astFunctionExpression,
 	// IJsCommentMeta meta, TranslateCtx ctx, boolean useJsAnnotForArgs,final
 	// String methName) {
 	// return createJstMethod(astFunctionExpression, meta, ctx,
 	// useJsAnnotForArgs, methName, true, null);
 	// }
 	//
 	// private static JstMethod createJstMethod(Expression
 	// astFunctionExpression,
 	// IJsCommentMeta meta, TranslateCtx ctx, boolean useJsAnnotForArgs,
 	// String methName, boolean createOverloads, JstMethod parentMethod) {
 	//
 	// if(meta != null && !(meta.getTyping() instanceof JsFuncType
 	// || meta.getTyping() instanceof JsAttributed)){
 	// ctx.getErrorReporter().error(
 	// "Cannot translate function meta: "
 	// + meta + " to function declaration " + methName + " in "
 	// + FunctionExpressionTranslator.class,
 	// ctx.getCurrentType().getName(), meta.getBeginOffset(),
 	// meta.getEndOffset(), 0, 0);
 	// meta = null;//by huzhou@ebay.com treat method as no meta at all
 	// useJsAnnotForArgs = false;
 	// }
 	//
 	// String methodName = methName;
 	// if(methName==null){
 	// methodName = (meta!=null) ? meta.getName() : EMPTYSTRING;
 	// }
 	// JstMethod jstMethod;
 	// if (VjoKeywords.CONSTRUCTS.equals(methodName)) {
 	// jstMethod = new JstConstructor();
 	// } else {
 	// jstMethod = new
 	// JstMethod(FunctionExpressionTranslator.DUMMY_METHOD_NAME);
 	// }
 	// jstMethod.setSource(TranslateHelper.getSource(astFunctionExpression,
 	// ctx.getSourceUtil()));
 	// jstMethod.setComments(getComments(astFunctionExpression, ctx));
 	// if (meta != null) {
 	// jstMethod.setHasJsAnnotation(true);
 	// }
 	//
 	// if (meta != null) {
 	// jstMethod.setName(methodName);
 	// TranslateHelper
 	// .setModifiersFromMeta(meta, jstMethod.getModifiers());
 	//
 	// // type params not arguments... meta.getArgs is a little confusing
 	// for (ArgType arg : meta.getArgs()) {
 	// jstMethod.addParam(arg.getName());
 	// }
 	//
 	// if(createOverloads){
 	// createOptionalOverloads(astFunctionExpression, ctx, methodName,
 	// meta, jstMethod, parentMethod);
 	// }
 	//
 	// final JsTypingMeta typing = meta.getTyping();
 	// if (typing != null) {
 	// final JsTypingMeta returnType = getReturnTyping(meta);
 	// final IJstType retType = findType(ctx, returnType);
 	// jstMethod.setRtnType(retType);
 	// }
 	// TranslateHelper.setReferenceSource(jstMethod, meta);
 	// }
 	//
 	// if (ctx.getCurrentScope() == ScopeIds.PROPS) {
 	// jstMethod.getModifiers().merge(JstModifiers.STATIC);
 	// }
 	//
 	// MethodDeclaration astMethodDeclaration = (astFunctionExpression
 	// instanceof FunctionExpression) ? ((FunctionExpression)
 	// astFunctionExpression)
 	// .getMethodDeclaration()
 	// : null;
 	//
 	// final IArgument[] astArgs = astMethodDeclaration!=null ?
 	// astMethodDeclaration.getArguments() : null;
 	// //get the args from function signature and create JstArgs
 	// List<JsTypingMeta> types;
 	// if (astArgs != null) {
 	// if( meta!=null){
 	// int idx = 0;
 	// for (IArgument astArg : astArgs) {
 	// types = new ArrayList<JsTypingMeta>();
 	// if (meta != null) {
 	// List<JsParam> params = getParams(meta);
 	// if (params != null && params.size() > idx) {
 	// types = params.get(idx).getTypes();
 	// }
 	// }
 	// if (!types.isEmpty() || !useJsAnnotForArgs) {
 	//
 	// ArgumentTranslator atrans = (ArgumentTranslator)
 	// TranslatorFactory.getTranslator(astArg, ctx);
 	// atrans.setCommentMetaAndIndex(meta, idx);
 	// JstArg jstArg = atrans.doTranslate(jstMethod,astArg);
 	// jstMethod.addArg(jstArg);
 	// }
 	// idx++;
 	// }
 	// //count of args from signature and annotations are not matchig, we need
 	// to create extra args specified in the annotation
 	// int astArgsCount = idx;
 	// List<JsParam> params = getParams(meta);
 	// int annArgsCount = (params == null)? 0 : params.size();
 	// if(astArgsCount<annArgsCount){
 	// for(int i=astArgsCount;i<annArgsCount;i++){
 	// JstArg jstArg = createJstArg(jstMethod, params.get(i).getName(), null,
 	// meta, i,ctx);
 	// if(jstArg!=null){
 	// jstMethod.addArg(jstArg);
 	// }
 	// }
 	// }
 	// } else {
 	// for (IArgument astArg : astArgs) {
 	// ArgumentTranslator atrans = (ArgumentTranslator)
 	// TranslatorFactory.getTranslator(astArg, ctx);
 	// JstArg jstArg = atrans.doTranslate(jstMethod, astArg);
 	// jstMethod.addArg(jstArg);
 	// }
 	// }
 	// }else{
 	// // Here when function expression has no arguments
 	// if( meta!=null && useJsAnnotForArgs){
 	// //get the args from function annotation and create JstArgs
 	// List<JsParam> params = getParams(meta);
 	// if(params!=null && params.size()>0){
 	// int idx = 0;
 	// types = new ArrayList<JsTypingMeta>();
 	// for(JsParam param: params){
 	// String argName = param.getName() != null ? param.getName() :
 	// DEFAULT_ARG_PREFIX+idx;
 	// JstArg jstArg = createJstArg(jstMethod,argName, null, meta, idx,ctx);
 	// if (params.size() > idx){
 	// types = params.get(idx).getTypes();
 	// }
 	// if(!types.isEmpty()){
 	// jstMethod.addArg(jstArg);
 	// idx++;
 	// }
 	// }
 	// }
 	// }
 	// }
 	//
 	//
 	// if(parentMethod != null){
 	// if(!TranslateHelper.hasOptionalArgs(meta)){
 	// parentMethod.addOverloaded(jstMethod);
 	// }
 	// // do not add overloaded method in the current type
 	// // node tree
 	// jstMethod.setParent(parentMethod, false);
 	// }
 	// return jstMethod;
 	// }
 
 	public static class RenameableSynthJstProxyProp extends SynthJstProxyProp {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private JstName jstRename;
 
 		public RenameableSynthJstProxyProp(final IJstProperty targetProperty,
 				final String rename) {
 			super(targetProperty);
 			if (rename != null) {
 				jstRename = new JstName(rename);
 			}
 		}
 
 		public void setName(final String rename) {
 			jstRename = new JstName(rename);
 		}
 
 		@Override
 		public JstName getName() {
 			if (jstRename != null) {
 				return jstRename;
 			}
 			return super.getName();
 		}
 	}
 
 	public static class RenameableSynthJstProxyMethod extends
 			SynthJstProxyMethod {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		private JstName jstRename;
 
 		public RenameableSynthJstProxyMethod(final IJstMethod targetMethod,
 				final JstName rename) {
 			super(targetMethod);
 
 			if (rename != null) {
 				jstRename = new JstName(rename.getName());
 				jstRename.setSource(rename.getSource());
 			}
 		}
 		
 		public RenameableSynthJstProxyMethod(final IJstMethod targetMethod,
 				final String rename) {
 			super(targetMethod);
 
 			if (rename != null) {
 				jstRename = new JstName(rename);
 			}
 		}
 
 		public void setName(final String rename) {
 			jstRename = new JstName(rename);
 		}
 
 		@Override
 		public JstName getName() {
 			if (jstRename != null) {
 				return jstRename;
 			}
 			return super.getName();
 		}
 
 		@Override
 		// bugfix by huzhou@ebay.com, renamed methods must be applied to its
 		// overloading methods names as well
 		public List<IJstMethod> getOverloaded() {
 			final List<IJstMethod> overloadedMtds = super.getOverloaded();
 			final List<IJstMethod> renamedMtds = new ArrayList<IJstMethod>(
 					overloadedMtds.size());
 			for (IJstMethod overloaded : overloadedMtds) {
 				renamedMtds.add(new RenameableSynthJstProxyMethod(overloaded,
 						jstRename != null ? jstRename.getName() : null));
 			}
 			return renamedMtds;
 		}
 	}
 
 	public static JstFuncType replaceSynthesizedMethodBinding(
 			final JstIdentifier jstIdentifier, final IJstMethod replacement) {
 		jstIdentifier.setJstBinding(replacement);
 		jstIdentifier.addChild(replacement);
 
 		// bugfix by huzhou, replace the JstFuncType with new function
 		// replacement
 		final JstFuncType jstFuncType = new JstFuncType(replacement);
 		jstIdentifier.setType(jstFuncType);
 		return jstFuncType;
 	}
 
 	// private static IJstMethod createJstSynthesizedMethod(
 	// final TranslateCtx ctx,
 	// final String methName,
 	// final IJsCommentMeta meta,
 	// final List<IJsCommentMeta> metaArr) {
 	//
 	// final JstMethod synthesizedMethod = createJstSynthesizedMethod(meta, ctx,
 	// true, methName);
 	//
 	// if(metaArr != null && metaArr.size() > 1){//more than one signature
 	// for(IJsCommentMeta next: metaArr){
 	// if(next.isMethod()){
 	// final JstMethod synthesizedOverloadMethod =
 	// createJstSynthesizedMethod(next, ctx, true, methName);
 	// synthesizedMethod.addOverloaded(synthesizedOverloadMethod);
 	// synthesizedOverloadMethod.setParent(synthesizedMethod, false);
 	// }
 	// }
 	// if (synthesizedMethod.isDispatcher()) {
 	// fixArgsForDispatchMethod(synthesizedMethod);
 	// setModifiersFromMeta(metaArr.get(0), synthesizedMethod.getModifiers());
 	// }
 	// }
 	//
 	// return synthesizedMethod;
 	// }
 	//
 	// //comment by huzhou
 	// //this JstSynthesizedMethod creation util is for function type in return
 	// type or parameter type
 	// private static JstSynthesizedMethod createJstSynthesizedMethod(final
 	// JsFuncType jsFuncType,
 	// final IJsCommentMeta meta,
 	// final TranslateCtx ctx){
 	// return createJstSynthesizedMethod(jsFuncType, meta, ctx, true, null);
 	// }
 	//
 	// private static JstSynthesizedMethod createJstSynthesizedMethod(final
 	// JsFuncType jsFuncType,
 	// final IJsCommentMeta meta,
 	// final TranslateCtx ctx,
 	// final boolean createOverloads,
 	// final JstMethod parentMethod) {
 	//
 	// final String funcName = jsFuncType.getFuncName();
 	// JstSynthesizedMethod jstMethod = new JstSynthesizedMethod(funcName, new
 	// JstModifiers(), null);
 	// jstMethod.getModifiers().setPublic();
 	//
 	// final List<JsParam> params = jsFuncType.getParams();
 	// final JsTypingMeta retTyping = jsFuncType.getReturnType();
 	// final IJstType retType = findType(ctx, retTyping);
 	// jstMethod.setRtnType(retType);
 	//
 	// if(params!=null && params.size()>0){
 	// int idx = 0;
 	// List<JsTypingMeta> types = new ArrayList<JsTypingMeta>();
 	// for(JsParam param: params){
 	// final String argName = param.getName() != null ? param.getName() :
 	// DEFAULT_ARG_PREFIX + idx;
 	// JstArg jstArg = createJstArg(jstMethod, argName, param, meta, ctx);
 	// if(params.size() > idx){
 	// types = params.get(idx).getTypes();
 	// }
 	// if(!types.isEmpty()){
 	// jstMethod.addArg(jstArg);
 	// idx++;
 	// }
 	// }
 	// }
 	// //TODO abstract this logic with createOptionalOverloads
 	// if(createOverloads){
 	// final OverloadInfo determineOverloadCount =
 	// determineOverloadCount(params);
 	// int overloads = determineOverloadCount.totalOverloads;
 	// int requiredCount = determineOverloadCount.requiredParams;
 	// for (int i = requiredCount; i < overloads; i++){
 	// final int paramCount = i;
 	// final JsFuncType overloadJsFuncType = new JsFuncType(retTyping);
 	// overloadJsFuncType.setFuncName(jsFuncType.getFuncName(),
 	// jsFuncType.getTypingToken());
 	// for(int j = 0; j < paramCount; j++){
 	// overloadJsFuncType.addParam(jsFuncType.getParams().get(j));
 	// }
 	// overloadJsFuncType.setDimension(jsFuncType.getDimension());
 	// overloadJsFuncType.setFinal(jsFuncType.isFinal());
 	// overloadJsFuncType.setOptional(jsFuncType.isOptional());
 	// overloadJsFuncType.setVariable(jsFuncType.isVariable());
 	//
 	// final JstMethod jstMtd = createJstSynthesizedMethod(overloadJsFuncType,
 	// meta, ctx, false, parentMethod);
 	// if (parentMethod != null) {
 	// jstMtd.setParent(parentMethod, false);
 	// parentMethod.addOverloaded(jstMtd);
 	// } else {
 	// jstMtd.setParent(jstMethod, false);
 	// jstMethod.addOverloaded(jstMtd);
 	// }
 	// }
 	// }
 	//
 	// return jstMethod;
 	// }
 	//
 	// private static JstSynthesizedMethod createJstSynthesizedMethod(
 	// IJsCommentMeta meta, TranslateCtx ctx, boolean useJsAnnotForArgs,
 	// final String methName) {
 	// return createJstSynthesizedMethod(meta, ctx, useJsAnnotForArgs, methName,
 	// true, null);
 	// }
 	//
 	// private static JstSynthesizedMethod createJstSynthesizedMethod(
 	// IJsCommentMeta meta, TranslateCtx ctx, boolean useJsAnnotForArgs,
 	// final String methName, final boolean createOverloads, final JstMethod
 	// parentMethod) {
 	//
 	// if(meta != null && !(meta.getTyping() instanceof JsFuncType
 	// || meta.getTyping() instanceof JsAttributed)){
 	// ctx.getErrorReporter().error(
 	// "Cannot translate function meta: "
 	// + meta + " to function declaration " + methName + " in "
 	// + FunctionExpressionTranslator.class,
 	// ctx.getCurrentType().getName(), meta.getBeginOffset(),
 	// meta.getEndOffset(), 0, 0);
 	// meta = null;//by huzhou@ebay.com treat method as no meta at all
 	// useJsAnnotForArgs = false;
 	// }
 	//
 	// String methodName = methName;
 	// if(methName==null){
 	// methodName = (meta!=null) ? meta.getName() : EMPTYSTRING;
 	// }
 	//
 	// final JstSynthesizedMethod jstMethod = new
 	// JstSynthesizedMethod(methodName, new JstModifiers(), null);
 	// if (meta != null) {
 	// //handle modifier
 	// TranslateHelper
 	// .setModifiersFromMeta(meta, jstMethod.getModifiers());
 	//
 	// for (ArgType arg : meta.getArgs()) {
 	// jstMethod.addParam(arg.getName());
 	// }
 	//
 	// if (meta.getTyping() != null) {
 	// final JsTypingMeta retTyping = getReturnTyping(meta);
 	// final IJstType retType = findType(ctx, retTyping);
 	// jstMethod.setRtnType(retType);
 	// }
 	// }
 	//
 	// if( meta != null && useJsAnnotForArgs){
 	// //get the args from function annotation and create JstArgs
 	// List<JsParam> params = getParams(meta);
 	// if(params!=null && params.size()>0){
 	// int idx = 0;
 	// List<JsTypingMeta> types = new ArrayList<JsTypingMeta>();
 	// for(JsParam param: params){
 	// String argName = param.getName() != null ? param.getName() :
 	// DEFAULT_ARG_PREFIX+idx;
 	// JstArg jstArg = createJstArg(jstMethod,argName, null, meta, idx,ctx);
 	// if(params.size() > idx){
 	// types = params.get(idx).getTypes();
 	// }
 	// if(!types.isEmpty()){
 	// jstMethod.addArg(jstArg);
 	// idx++;
 	// }
 	// }
 	// }
 	// }
 	//
 	// if(createOverloads){
 	// createOptionalOverloads(null, ctx, methName, meta, jstMethod,
 	// parentMethod);
 	// }
 	//
 	// if(parentMethod != null){
 	// if(!hasOptionalArgs(meta)){
 	// parentMethod.addOverloaded(jstMethod);
 	// }
 	// // do not add overloaded method in the current type
 	// // node tree
 	// jstMethod.setParent(parentMethod, false);
 	// }
 	// return jstMethod;
 	// }
 	//
 
 	private static List<String> splitParamTypeNames(String paramTypeNames) {
 		StringTokenizer tokens = new StringTokenizer(paramTypeNames, "<>,",
 				true);
 		List<String> typeNames = new ArrayList<String>();
 		int level = 0;
 		StringBuffer typeName = new StringBuffer();
 		while (tokens.hasMoreTokens()) {
 			String token = tokens.nextToken();
 			if (level == 0 && COMMA.equals(token)) {
 				typeNames.add(typeName.toString());
 				typeName = new StringBuffer();
 			} else {
 				typeName.append(token);
 				if (LT.equals(token)) {
 					level++;
 				} else if (GT.equals(token)) {
 					level--;
 				}
 			}
 		}
 		if (level == 0) {
 			typeNames.add(typeName.toString());
 		}
 		return typeNames;
 	}
 
 	public static void addParamsToType(TranslateCtx ctx, JstType type,
 			String params) {
 		if (params == null || EMPTYSTRING.equals(params))
 			return;
 		String[] pArr = params.split(",");
 		for (int i = 0; i < pArr.length; i++) {
 			String param = pArr[i];
 			String[] name = param.split(" extends ");
 			if (name.length == 1) {
 				type.addParam(param);
 			} else if (name.length == 2) {
 				JstParamType ptype = type.addParam(name[0]);
 				ptype.addBound(new JstParamType(name[1]));
 			}
 		}
 	}
 
 	// TODO moving to resolver utilities
 	/*
 	 * private static IJstType searchInnerTypes(IJstType currentType, String
 	 * name) { if (currentType != null) { for (IJstType innType :
 	 * currentType.getEmbededTypes()) {
 	 * 
 	 * if (name.equals(innType.getName())) { return innType; }
 	 * 
 	 * String tmp; boolean lookFurther = false; if (name.indexOf(".") > -1) {
 	 * tmp = name.substring(0, name.indexOf(".")); lookFurther = true; } else {
 	 * tmp = name; } if (tmp.equals(innType.getSimpleName())) { if (lookFurther)
 	 * { IJstType type = searchInnerTypes(innType,
 	 * name.substring(name.indexOf(".")+1, name.length())); if (type != null) {
 	 * return type; } } else { return innType; } } } } return null; }
 	 */
 
 	public static void addParamsToType(TranslateCtx ctx, JstType type,
 			JsType jsType) {
 		if (jsType != null && jsType.getArgs().size() > 0) {
 			for (ArgType arg : jsType.getArgs()) {
 				JstParamType ptype = type.addParam(arg.getName());
 				if (arg.getWildCardType() == ArgType.WildCardType.EXTENDS) {
 					JstWildcardType wildcard = new JstWildcardType(JstFactory
 							.getInstance().createJstType(
 									arg.getFamily().getType(), true));
 					ptype.addBound(wildcard);
 				} else if (arg.getWildCardType() == ArgType.WildCardType.SUPER) {
 					JstWildcardType wildcard = new JstWildcardType(JstFactory
 							.getInstance().createJstType(
 									arg.getFamily().getType(), true), false);
 					ptype.addBound(wildcard);
 				}
 			}
 		}
 	}
 
 	@Deprecated
 	public static IExpr getCastable(IExpr expr, IStatement realExpr,
 			TranslateCtx ctx) {
 		return getCastable(expr, realExpr.sourceStart(), realExpr.sourceEnd(),
 				ctx.getPreviousNodeSourceEnd(), ctx.getNextNodeSourceStart(),
 				ctx);
 	}
 
 	public static IExpr getCastable(IExpr expr, IStatement realExpr, int prev,
 			int next, TranslateCtx ctx) {
 		return getCastable(expr, realExpr.sourceStart(), realExpr.sourceEnd(),
 				prev, next, ctx);
 	}
 
 	/**
 	 * helper for {@link #getCastable(IExpr, List, TranslateCtx)}
 	 * 
 	 * @param expr
 	 * @return
 	 */
 	public static List<IJsCommentMeta> findMetaFromExpr(BaseJstNode expr) {
 		for (IJstNode child : expr.getChildren()) {
 			if (child instanceof JsCommentMetaNode) {
 				return ((JsCommentMetaNode) child).getJsCommentMetas();
 			}
 		}
 		return null;
 	}
 
 	public static IExpr getCastable(IExpr expr, int start, int end, int prev,
 			int next, TranslateCtx ctx) {
 		List<IJsCommentMeta> metaList = ctx.getCommentCollector()
 				.getCommentMeta(start, end, prev, next);
 		return getCastable(expr, metaList, ctx);
 	}
 
 	public static IExpr getCastable(final IExpr expr,
 			final List<IJsCommentMeta> metaList, final TranslateCtx ctx) {
 
 		if (metaList.size() > 0 && metaList.get(0).isCast()) { // currently only
 																// look for
 																// first meta
 			IJsCommentMeta originalMeta = metaList.get(0);
 			if (originalMeta != null) {
 				if (originalMeta.getTyping() != null) {
 					return (IExpr) attachMeta(
 							new CastExpr((IExpr) expr,
 									TranslateHelper.findType(fromCtx(ctx),
 											originalMeta.getTyping(),
 											originalMeta)), metaList, ctx);
 				} else {
 					return (IExpr) attachMeta(new CastExpr((IExpr) expr),
 							metaList, ctx);
 				}
 			}
 		}
 		return expr instanceof BaseJstNode ? (IExpr) attachMeta(
 				(BaseJstNode) expr, metaList, ctx) : expr;
 	}
 
 	public static IFindTypeSupport fromCtx(final TranslateCtx ctx) {
 		return ctx;
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
 
 	public static BaseJstNode attachMeta(final BaseJstNode node,
 			final List<IJsCommentMeta> metaList, final TranslateCtx ctx) {
 		if (node != null && metaList != null && metaList.size() > 0) {
 
 			JsCommentMetaNode commentMetaNode = getJsCommentMetaNode(node);
 			if (commentMetaNode == null) {
 				commentMetaNode = new JsCommentMetaNode();
 				node.addChild(commentMetaNode);
 			}
 			commentMetaNode.setJsCommentMetas(metaList);
 
 			int beginOffset = -1, endOffset = -1;
 			for (IJsCommentMeta meta : metaList) {
 				beginOffset = beginOffset < 0 ? meta.getBeginOffset() : meta
 						.getBeginOffset() < beginOffset ? meta.getBeginOffset()
 						: beginOffset;
 				endOffset = meta.getEndOffset() > endOffset ? meta
 						.getEndOffset() : endOffset;
 			}// getting begin/end offsets from the metas bounds
 
 			final JstSource source = createJstSource(ctx.getSourceUtil(),
 					endOffset - beginOffset, beginOffset, endOffset);
 			commentMetaNode.setSource(source);
 		}
 		return node;
 	}
 
 	private static void addArgsToType(IFindTypeSupport findSupport,
 			JstTypeWithArgs type, JsType jsType) {
 		if (jsType != null && jsType.getArgs().size() > 0) {
 			for (ArgType arg : jsType.getArgs()) {
 				if (arg.getWildCardType() == ArgType.WildCardType.EXTENDS) {
 					type.addArgType(new JstWildcardType(findType(findSupport,
 							arg.getFamily().getType()), true));
 				} else if (arg.getWildCardType() == ArgType.WildCardType.SUPER) {
 					type.addArgType(new JstWildcardType(findType(findSupport,
 							arg.getFamily().getType()), false));
 				} else if (arg.getType() == null) {
 					type.addArgType(new JstWildcardType(null));
 				} else {
 					type.addArgType(findType(findSupport, arg.getType(), null));
 				}
 			}
 		}
 	}
 
 	// TODO moving to resolver utilities
 	public static JstTypeWithArgs getJstWithArgs(IFindTypeSupport ctx,
 			IJstType type, String params) {
 		if (params == null || EMPTYSTRING.equals(params))
 			return null;
 		JstTypeWithArgs jstType = new JstTypeWithArgs(type);
 		List<String> plist = splitParamTypeNames(params);
 		for (String param : plist) {
 			boolean isUpper = param.contains(" extends ");
 			boolean isLower = param.contains(" super ");
 			if (isUpper || isLower) {
 				String[] name = (isUpper) ? param.split(" extends ") : param
 						.split(" super ");
 				if ("?".equals(name[0].trim())) {
 					jstType.addArgType(new JstWildcardType(findType(ctx,
 							name[1]), isUpper));
 				} else {
 					JstParamType pType = new JstParamType(name[0]);
 					pType.addBound(new JstWildcardType(findType(ctx, name[1]),
 							isUpper));
 					jstType.addArgType(pType);
 				}
 			} else {
 				jstType.addArgType(findType(ctx, param));
 			}
 		}
 		return jstType;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void addStatementsToJstBlock(Statement[] statements,
 			JstBlock jstBlock, int sourceEnd, TranslateCtx ctx) {
 		if (statements == null) {
 			return;
 		}
 		int i = 0, len = statements.length;
 		for (; i < len; i++) {
 			IStatement astStatement = statements[i];
 			if (i + 1 < len) {
 				ctx.setNextNodeSourceStart(statements[i + 1].sourceStart);
 			} else {
 				ctx.setNextNodeSourceStart(sourceEnd);
 			}
 
 			BaseAst2JstTranslator translator = TranslatorFactory.getTranslator(
 					astStatement, ctx);
 			translator.setParent(jstBlock);
 			Object result = translator.translate(astStatement);
 			if(result instanceof BaseJstNode[]){
 				for (BaseJstNode node : (BaseJstNode[])result) {
 					handleResult(jstBlock, ctx, astStatement, node);
 				}
 			}else{
 				handleResult(jstBlock, ctx, astStatement, result);
 			}
 			
 		}
 	}
 
 	private static void handleResult(JstBlock jstBlock, TranslateCtx ctx,
 			IStatement astStatement, Object result) {
 		if (result instanceof BaseJstNode) {
 			BaseJstNode baseNode = (BaseJstNode) result;
 			if (baseNode.getSource() == null) {
 				baseNode.setSource(TranslateHelper.getSource(astStatement,
 						ctx.getSourceUtil()));
 			}
 			// Adding js annotations to Jst. //rbhogi
 			createJsAnnotations(result, astStatement, ctx);
 		}
 		if (result instanceof IStmt) {
 			jstBlock.addStmt((IStmt) result);
 		} else if (result instanceof IExpr) {
 			if (result instanceof JstIdentifier) {
 				JstIdentifier id = (JstIdentifier) result;
 
 				List<IJsCommentMeta> metaList = ctx.getCommentCollector()
 						.getCommentMeta(id.getSource().getStartOffSet(),
 								id.getSource().getEndOffSet(),
 								ctx.getPreviousNodeSourceEnd(),
 								ctx.getNextNodeSourceStart());
 				attachMeta(id, metaList, ctx);
 			}
 			jstBlock.addStmt(new ExprStmt((IExpr) result));
 		} else if (result instanceof FakeJstWithStmt) {
 			for (IStmt statement : ((FakeJstWithStmt) result)
 					.getStatements()) {
 				jstBlock.addStmt(statement);
 			}
 		} else if (result instanceof JstMethod) {
 			jstBlock.addStmt(new ExprStmt(new FuncExpr((JstMethod) result)));
 		} else if (result instanceof JstBlock) {
 			jstBlock.addStmt(new BlockStmt((JstBlock) result));
 		} else if (result instanceof BaseJstNode) {
 			BaseJstNode node = (BaseJstNode) result;
 			node.setParent(jstBlock);
 		}
 		ctx.setPreviousNodeSourceEnd(astStatement.sourceEnd());
 	}
 
 	private static void createJsAnnotations(Object result,
 			IStatement statement, TranslateCtx ctx) {
 		// Getting JsAnnotation info
 		JsAnnotation jsAnnotation = getJsAnnotation(statement, ctx);
 		if (jsAnnotation == null) {
 			return;
 		}
 		if (result == null) {
 			return;
 		}
 		if (!(result instanceof BaseJstNode)) {
 			return;
 		}
 		// Adding annotation info to jst
 		if (jsAnnotation.isSupressTypeCheck()) {
 			JstAnnotation jstAnno = new JstAnnotation();
 			jstAnno.setName(JsAnnotationType.SUPRESSTYPECHECK.toString());
 			((BaseJstNode) result).addAnnotation(jstAnno);
 		}
 	}
 
 	private static JsAnnotation getJsAnnotation(IStatement statement,
 			TranslateCtx ctx) {
 		List<IJsCommentMeta> annotationMeta = null;
 		annotationMeta = ctx.getCommentCollector().getAnnotationMeta(
 				statement.sourceStart(), ctx.getPreviousNodeSourceEnd(),
 				ctx.getNextNodeSourceStart());
 		if (annotationMeta.isEmpty()) {
 			return null;
 		} else {
 			return annotationMeta.get(0).getAnnotation();// return first ele
 		}
 	}
 
 	// private static boolean isNotCompleteExpression(TranslateCtx m_ctx,
 	// int statEnd) {
 	// return m_ctx.getOriginalSource()[statEnd] != ';';
 	// }
 
 	public static JstTypeReference createRef(IJstType extendedType,
 			JstSource source) {
 		JstTypeReference reference = new JstTypeReference(extendedType);
 		reference.setSource(source);
 		return reference;
 	}
 
 	public static void setReferenceSource(JstProperty method, JstSource source) {
 		((JstTypeReference) method.getTypeRef()).setSource(source);
 	}
 
 	public static void setReferenceSource(JstMethod property, JstSource source) {
 		((JstTypeReference) property.getRtnTypeRef()).setSource(source);
 	}
 
 	public static void setReferenceSource(JstMethod method, IJsCommentMeta meta) {
 		BaseJstNode reference = (BaseJstNode) method.getRtnTypeRef();
 		setTypeRefSource(reference, meta);
 	}
 
 	public static void setReferenceSource(JstProperty property,
 			JsCommentMeta meta) {
 		JstTypeReference reference;
 		reference = (JstTypeReference) property.getTypeRef();
 		setTypeRefSource(reference, meta);
 	}
 
 	public static JstArg createJstArg(JstMethod jstMethod, String argName,
 			JsParam param, IJsCommentMeta meta, TranslateCtx ctx) {
 		final List<JsTypingMeta> types = new ArrayList<JsTypingMeta>(
 				param.getTypes());
 		boolean isOpt = param.isOptional();
 		boolean isVar = param.isVariable();
 		boolean isFinal = param.isFinal();
 		JstSource source = null;
 
 		// TODO meta should be retrievable
 		if (meta != null && ctx != null) {
 			int commentOffset = meta.getBeginOffset() + 1;// added 1
 			final int startOffset = commentOffset + param.getBeginColumn();
 			final int endOffset = commentOffset + param.getEndColumn();
 			final int length = endOffset - startOffset + 1;
 			source = createJstSource(ctx.getSourceUtil(), length, startOffset,
 					endOffset);
 		}
 
 		final List<IJstTypeReference> jstTypes = new ArrayList<IJstTypeReference>();
 		if (types.isEmpty()) {
 			IJstType argType = JstCache.getInstance().getType("Object");
 			JstTypeReference reference = TranslateHelper.createRef(argType,
 					null);
 			reference.setSource(null);
 			jstTypes.add(reference);
 		} else {
 			for (JsTypingMeta pType : types) {
 				JstTypeReference reference = null;
 
 				IJstType paramType = jstMethod.getParamType(pType.getType());
 				if (paramType != null) {
 					reference = TranslateHelper.createRef(paramType, null);
 					reference.setSource(source);
 					jstTypes.add(reference);
 					reference = TranslateHelper.createRef(paramType, null);
 					continue;
 				}
 
 				IJstType argType = TranslateHelper.findType(ctx, pType, meta);
 
 				reference = TranslateHelper.createRef(argType, null);
 
 				reference.setSource(null);
 				jstTypes.add(reference);
 			}
 		}
 		if (argName == null) {
 			argName = param.getName();
 		}
 		JstArg arg = new JstArg(jstTypes, 0, argName, isVar, isOpt, isFinal);
 		arg.setSource(source);
 
 		return arg;
 	}
 
 	public static JstArg createJstArg(JstMethod jstMethod, String argName,
 			IASTNode argSource, IJsCommentMeta meta, int argIndex,
 			IFindTypeSupport ctx) {
 		// process param type
 		List<JsTypingMeta> types = new ArrayList<JsTypingMeta>();
 		JstSource source = null;
 		boolean isOpt = false;
 		boolean isVar = false;
 		boolean isFinal = false;
 		// String nameFromComment = EMPTYSTRING;
 
 		if (meta != null) {
 			List<JsParam> params = getParams(meta);
 			if (params != null && params.size() > argIndex) {
 				JsParam jsParam = params.get(argIndex);
 				types = jsParam.getTypes();
 				isOpt = jsParam.isOptional();
 				isVar = jsParam.isVariable();
 				isFinal = jsParam.isFinal();
 
 				if (ctx != null) {
 					// TODO fix this
 					int commentOffset = meta.getBeginOffset() + 1;// added 1
 					// TODO what to do with fake name params
 
 					int startOffset = commentOffset + jsParam.getBeginColumn();
 					int endOffset = commentOffset + jsParam.getEndColumn();
 					int length = endOffset - startOffset + 1;
 					source = createJstSource(ctx.getLineInfoProvider(), length,
 							startOffset, endOffset);
 				}
 			}
 		}
 
 		List<IJstTypeReference> jstTypes = new ArrayList<IJstTypeReference>();
 		if (types.isEmpty()) {
 			// IJstType argType = TranslateHelper.findType(ctx, "Object"); --
 			// Should get from JstCache
 			IJstType argType = JstCache.getInstance().getType("Undefined");
 
 			JstTypeReference reference = TranslateHelper.createRef(argType,
 					source);
 			reference.setSource(source);
 			jstTypes.add(reference);
 		} else {
 			for (JsTypingMeta pType : types) {
 
 				IJstType paramType = jstMethod.getParamType(pType.getType());
 				if (paramType != null) {
 					// For Generic Type
 					JstTypeReference reference = TranslateHelper.createRef(
 							paramType, source);
 					reference.setSource(source);
 					jstTypes.add(reference);
 					continue;
 				}
 
 				IJstType argType = TranslateHelper.findType(ctx, pType, meta);
 				JstTypeReference reference = TranslateHelper.createRef(argType,
 						source);
 				reference.setSource(source);
 				jstTypes.add(reference);
 			}
 		}
 		if (argName == null && meta != null) {
 			argName = getParams(meta).get(argIndex).getName();
 		}
 		JstArg arg = new JstArg(jstTypes, 0, argName, isVar, isOpt, isFinal);
 		if (argSource != null) {
 			arg.setSource(TranslateHelper.getSource(argSource,
 					ctx.getLineInfoProvider()));
 		} else {
 			arg.setSource(source);
 		}
 
 		return arg;
 	}
 
 	public static JstSource createJstSource(JstSourceUtil util, int length,
 			int startOffset, int endOffset) {
 
 		int line = util.line(startOffset);
 		int col = util.col(startOffset);
 		return new JstSource(JstSource.JS, line, col,
 				length, startOffset, endOffset);
 
 	}
 
 	public static JstSource createJstSource(
 			IFindTypeSupport.ILineInfoProvider provider, int length,
 			int startOffset, int endOffset) {
 		return new JstSource(JstSource.JS, provider.line(startOffset),
 				provider.col(startOffset), length, startOffset, endOffset);
 	}
 
 	public static IJstType getJstType(IJstType type) {
 		IJstType jstType = type;
 		while (jstType != null) {
 			if (jstType instanceof IJstRefType) {
 				break;
 			}
 			if (jstType instanceof JstProxyType) {
 				jstType = ((JstProxyType) jstType).getType();
 			} else {
 				break;
 			}
 		}
 		return jstType;
 	}
 
 	public static IJstType getGlobalType() {
 		IJstType globalJstType = JstCache.getInstance().getType("Global");
 		if (globalJstType == null) {
 			globalJstType = JstFactory.getInstance().createJstType("Global",
 					true);
 		}
 
 		return globalJstType;
 	}
 
 	public static IJstType getObjectType() {
 		IJstType functionJstType = JstCache.getInstance().getType("Object");
 		if (functionJstType == null) {
 			functionJstType = JstFactory.getInstance().createJstType("Object",
 					true);
 		}
 
 		return functionJstType;
 	}
 
 	public static IJstType getFunctionType() {
 		IJstType functionJstType = JstCache.getInstance().getType("Function");
 		if (functionJstType == null) {
 			functionJstType = JstFactory.getInstance().createJstType(
 					"Function", true);
 		}
 
 		return functionJstType;
 	}
 
 	public static IJstType getTypeFromInactive(String typeName,
 			final IJstType type) {
 		String[] names = typeName.split("\\.");
 		if (names.length == 2) { // Potential OType
 			List<? extends IJstType> types = type.getInactiveImports();
 			if (type.isOType() && names[0].equals(type.getSimpleName())
 					&& type.getOType(names[1]) != null) {
 				return type.getOType(names[1]);
 			}
 			IJstType otype = getOTypeFromInactive(names, types);
 			if (otype != null) {
 				return otype;
 			}
 			for (IJstTypeReference mixin : type.getMixinsRef()) {
 				otype = getOTypeFromInactive(names, mixin.getReferencedType()
 						.getInactiveImports());
 				if (otype != null) {
 					return otype;
 				}
 			}
 		}
 		return null;
 	}
 
 	private static IJstType getOTypeFromInactive(final String[] names,
 			final List<? extends IJstType> types) {
 		for (IJstType ineed : types) {
 			if (names[0].equals(ineed.getSimpleName())) {
 				if (ineed.getOType(names[1]) != null) {
 					return ineed.getOType(names[1]);
 				}
 			}
 		}
 		return null;
 	}
 
 	public static JstType getNativeJsObject() {
 		return JstCache.getInstance().getType(
 				org.eclipse.vjet.dsf.jsnative.global.Object.class
 						.getSimpleName());
 	}
 
 	public static boolean isArgsMatch(IArgument[] astArgs,
 			List<JsParam> metaArgs) {
 		if (astArgs == null) {
 			if (metaArgs.size() == 0) {
 				return true;
 			}
 			return false;
 		}
 		int metaArgsSize = 0;
 		for (JsParam p : metaArgs) {
 			if (!p.isOptional()) {
 				metaArgsSize++;
 			}
 		}
 
 		return astArgs.length == metaArgsSize;
 	}
 
 	public static List<JsParam> getParams(IJsCommentMeta meta) {
 		// Special logic for loose function annotation such as //< void
 		JsTypingMeta typing = meta.getTyping();
 		if (typing instanceof JsFuncType) {
 			return ((JsFuncType) typing).getParams();
 		}
 		return null;
 	}
 
 	public static JsTypingMeta getReturnTyping(IJsCommentMeta meta) {
 		// Special logic for loose function annotation such as //< void
 		JsTypingMeta typing = meta.getTyping();
 		if (typing instanceof JsFuncType) {
 			return ((JsFuncType) typing).getReturnType();
 		}
 		return typing;
 	}
 
 	@SuppressWarnings("unused")
 	@Deprecated
 	private static IJstType findTypeDeprecated(IJsCommentMeta meta,
 			TranslateCtx ctx, JstMethod jstMethod, boolean isOtype,
 			final JsTypingMeta returnType, final IJstType currentType) {
 
 		IJstType retType = null;
 		String typeName = returnType == null ? "void" : returnType.getType();
 		String[] names = typeName.split("\\.");
 
 		if (typeName.indexOf('.') == -1) {
 			if (currentType != null) {
 				IJstType importedType = currentType.getImport(typeName);
 				if (importedType != null) {
 					typeName = importedType.getName();
 				}
 			}
 		} else if (meta.getName() == null && meta.getArgs().size() == 0
 				&& names.length == 2) {
 			// check to see if defined by an otype
 			IJstType type = TranslateHelper.getTypeFromInactive(typeName,
 					currentType);
 			if (type != null && type instanceof JstFunctionRefType) {
 				isOtype = true;
 				jstMethod.setOType((JstFunctionRefType) type);
 			}
 
 		}
 
 		if (!isOtype) {
 			for (ArgType a : meta.getArgs()) {
 				if (typeName.equals(a.getName())) {
 					retType = jstMethod.getParamType(typeName);
 				}
 			}
 			if (retType == null) {
 				retType = findTypeInGenerics(typeName, currentType);
 			}
 
 			boolean rtnSet = false;
 			if (meta.getArgs().size() > 0) {
 
 				for (ArgType p : meta.getArgs()) {
 					String name = retType.getName();
 					boolean isA = false;
 					if (retType instanceof JstArray) {
 						name = ((JstArray) retType).getComponentType()
 								.getName();
 						isA = true;
 					}
 					if (p.getName().equals(name)) {
 						IJstType aJst;
 						if (isA) {
 							aJst = new JstArray(new JstParamType(name));
 						} else {
 							aJst = retType;
 						}
 						retType = aJst;
 						rtnSet = true;
 						break;
 					}
 				}
 			}
 			if (!rtnSet) {// if no name
 				if ((meta.getName() == null || EMPTYSTRING.equals(meta
 						.getName())) && "Function".equals(typeName)) {
 					retType = (JstCache.getInstance().getType("void"));
 				}
 			}
 		}
 		return retType;
 	}
 
 	@Deprecated
 	private static IJstType findTypeInGenerics(String typeName,
 			IJstType currentType) {
 		if ("void".equalsIgnoreCase(typeName)) {
 			return null;
 		}
 		IJstType retType = null;
 		for (IJstType argType : currentType.getParamTypes()) {
 			if (typeName.equals(argType.getSimpleName())) {
 				retType = argType;
 				break;
 			}
 		}
 		if (retType == null) {
 			if (currentType.getOuterType() != null
 					&& currentType.getOuterType() != currentType) {
 				retType = findTypeInGenerics(typeName,
 						currentType.getOuterType());
 			}
 		}
 		return retType;
 	}
 
 	public static class MethodTranslateHelper {
 
 		public static JstMethod createJstMethod(final IExpression astExpr,
 				final List<IJsCommentMeta> metaArr, final TranslateCtx ctx,
 				final String methodName) {
 
 			JstMethod method = null;
 			if (astExpr instanceof FieldReference) {
 				if (VjoKeywords.NEEDS_IMPL.equals(String
 						.valueOf(((FieldReference) astExpr).token))
 						&& VjoKeywords.VJO
 								.equals(((FieldReference) astExpr).receiver
 										.toString())) {
 					method = createJstMethodFromAst(astExpr, true, metaArr,
 							ctx, methodName);
 				}
 			}
 			// bugfix by huzhou to handle null/undefined ast cases
 			else if (astExpr instanceof NullLiteral) {
 				method = createJstMethodFromAst(astExpr, true, metaArr, ctx,
 						methodName);
 			} else if (astExpr instanceof UndefinedLiteral) {
 				method = createJstMethodFromAst(astExpr, true, metaArr, ctx,
 						methodName);
 			} else if (astExpr instanceof FunctionExpression) {
 				method = createJstMethod(
 						((FunctionExpression) astExpr).getMethodDeclaration(),
 						metaArr, ctx, methodName);
 			}
 
 			if (method != null) {
 				addMethodCommentsAndSource(ctx, astExpr, method);
 			}
 			return method;// invalid
 		}
 
 		/**
 		 * The only jstMethod signature creation api for all translators
 		 * 
 		 * @param astMtdDecl
 		 * @param metaArr
 		 * @param ctx
 		 * @param methodName
 		 * @return
 		 */
 		public static JstMethod createJstMethod(
 				final MethodDeclaration astMtdDecl,
 				final List<IJsCommentMeta> metaArr, final TranslateCtx ctx,
 				final String methodName) {
 			return createJstMethodFromAst(astMtdDecl, false, metaArr, ctx,
 					methodName);
 		}
 
 		private static JstMethod createJstMethodFromAst(final IASTNode astNode,
 				final boolean isNeedsImpl, final List<IJsCommentMeta> metaArr,
 				final TranslateCtx ctx, final String methodName) {
 			final List<IJsCommentMeta> funcMetaList = filterFuncMetas(ctx,
 					metaArr);
 			final MethodDeclaration astMtdDecl = astNode != null
 					&& astNode instanceof MethodDeclaration ? (MethodDeclaration) astNode
 					: null;
 			final IArgument[] astParams = astMtdDecl == null ? null
 					: astMtdDecl.getArguments();
 			// no meta at all
 			if (funcMetaList.isEmpty()) {
 				// as discussed with Mr.P and Yitao
 				// local functions without meta at all doesn't really declare
 				// what its parameters
 				// therefore, we forced them to be optional, so invocation side
 				// could be loosen up in terms of number of arguments passing in
 				final ScopeId topScope = ctx.getScopeStack().peek();
 				if (ScopeIds.PROPS.equals(topScope)
 						|| ScopeIds.PROTOS.equals(topScope)) {
 					return createJstMethod(astMtdDecl, astParams, null, ctx,
 							true, methodName);
 				} else {
 					return createJstMethodWithoutMeta(astMtdDecl, null, ctx,
 							false, methodName);
 				}
 			}
 			// one meta only and physical arguments matched the meta, create
 			// JstMethod without overloading
 			else if (funcMetaList.size() == 1) {
 				final IJsCommentMeta singleMeta = funcMetaList.get(0);
 				if (singleMeta instanceof PotentialOtypeMemberTypeMeta) {
 					final PotentialOtypeMemberTypeMeta potentialOtypeMemberTypeMeta = (PotentialOtypeMemberTypeMeta) singleMeta;
 					final IJstMethod dumpMtd = createJstMethod(astMtdDecl,
 							astParams, null, ctx, false, methodName);
 					return new JstPotentialOtypeMethod(methodName,
 							potentialOtypeMemberTypeMeta
 									.getPotentialOtypeMemberType(),
 							dumpMtd.getArgs().toArray(
 									new JstArg[dumpMtd.getArgs().size()]));
 				} else if (singleMeta instanceof PotentialAttributedTypeMeta) {
 					final PotentialAttributedTypeMeta potentialAttributedTypeMeta = (PotentialAttributedTypeMeta) singleMeta;
 					final IJstMethod dumpMtd = createJstMethod(astMtdDecl,
 							astParams, null, ctx, false, methodName);
 					return new JstPotentialAttributedMethod(methodName,
 							potentialAttributedTypeMeta
 									.getPotentialAttributedType(),
 							dumpMtd.getArgs().toArray(
 									new JstArg[dumpMtd.getArgs().size()]));
 				} else if ((isNeedsImpl || isMetaMatchingAst(singleMeta,
 						astMtdDecl))) {
 					return createJstMethod(astMtdDecl, astParams, singleMeta,
 							ctx, true, methodName);
 				}
 			}
 
 			// either the physcial arguments not matching,
 			// or more than one meta, which forces the overloading already
 			final JstMethod dispatcher = createJstMethod(astMtdDecl, astParams,
 					null, ctx, false, methodName);
 			// add non vjetdoc comments locations to dispatcher
 //			dispatcher
 //			   .setCommentLocations(getCommentLocations2(astMtdDecl, ctx));
 			
 			for (IJsCommentMeta meta : funcMetaList) {
 				final JstMethod overloaded = createJstMethod(astMtdDecl,
 						astParams, meta, ctx, true, methodName);
 				// if(addedDoc){
 				// overloaded.setDoc(null);
 				// }
 
 				attachOverloaded(dispatcher, overloaded);
 			}
 			fixDispatcher(dispatcher);
 			return dispatcher;
 		}
 
 		/**
 		 * The only jstSynthesizedMethod signature creation api for all
 		 * translators
 		 * 
 		 * @param metaArr
 		 * @param ctx
 		 * @param methName
 		 * @return
 		 */
 		public static IJstMethod createJstSynthesizedMethod(
 				final List<IJsCommentMeta> metaArr, final IFindTypeSupport ctx,
 				final String methName) {
 			final List<IJsCommentMeta> funcMetaList = filterFuncMetas(ctx,
 					metaArr);
 			// no meta at all
 			if (funcMetaList.isEmpty()) {
 				return null;
 			}
 			// only one meta
 			else if (funcMetaList.size() == 1) {
 				final IJsCommentMeta singleMeta = funcMetaList.get(0);
 				if (singleMeta instanceof PotentialOtypeMemberTypeMeta) {
 					final PotentialOtypeMemberTypeMeta potentialOtypeMemberTypeMeta = (PotentialOtypeMemberTypeMeta) singleMeta;
 					return new JstPotentialOtypeMethod(methName,
 							potentialOtypeMemberTypeMeta
 									.getPotentialOtypeMemberType());
 				} else if (singleMeta instanceof PotentialAttributedTypeMeta) {
 					final PotentialAttributedTypeMeta potentialAttributedTypeMeta = (PotentialAttributedTypeMeta) singleMeta;
 					return new JstPotentialAttributedMethod(methName,
 							potentialAttributedTypeMeta
 									.getPotentialAttributedType());
 				} else {
 					return createJstSynthesizedMethod(singleMeta, ctx, true,
 							methName);
 				}
 			}
 			// more than one meta creates overloading
 			else {
 				final JstMethod dispatcher = createJstSynthesizedMethod(null,
 						ctx, true, methName);
 				for (IJsCommentMeta meta : funcMetaList) {
 					final JstMethod overloaded = createJstSynthesizedMethod(
 							meta, ctx, true, methName);
 					attachOverloaded(dispatcher, overloaded);
 				}
 				return dispatcher;
 			}
 		}
 
 		public static void addMethodCommentsAndSource(final TranslateCtx ctx,
 				final IASTNode ast, final JstMethod method) {
 			final JstSource methodSource = TranslateHelper.getSource(ast,
 					ctx.getSourceUtil());
 			JstCommentLocation methodComments = ctx.getCommentCollector()
 					.getCommentLocationNonMeta2(ast.sourceStart());
 			if (!method.isDispatcher()) {
 				method.setSource(methodSource);
 				if (methodComments != null) {
 					method.addCommentLocation(methodComments);
 				}
 				// JsDocHelper.addJsDoc(methodComments, method);
 				// method.setComments(methodComments);
 			} else {
 				method.setSource(methodSource);
 				// method.setComments(methodComments);
 				if (methodComments != null) {
 					method.addCommentLocation(methodComments);
 				}
 				// JsDocHelper.addJsDoc(methodComments, method);
 				for (IJstMethod overload : method.getOverloaded()) {
 					if (overload instanceof JstMethod) {
 						((JstMethod) overload).setSource(methodSource);
 						// method.setComments(methodComments);
 						// JsDocHelper.addJsDoc(methodComments, method);
 					}
 				}
 			}
 		}
 
 		private static List<String> getComments2(final IASTNode ast,
 				final TranslateCtx ctx) {
 			if (ast == null) {
 				return Collections.EMPTY_LIST;
 			}
 			return ctx.getCommentCollector().getCommentNonMeta(
 					ast.sourceStart(), ctx.getPreviousNodeSourceEnd());
 		}
 
 		private static List<JstCommentLocation> getCommentLocations2(
 				final IASTNode ast, final TranslateCtx ctx) {
 			if (ast == null) {
 				return null;
 			}
 			return ctx.getCommentCollector().getCommentLocationNonMeta(
 					ast.sourceStart(), ctx.getPreviousNodeSourceEnd());
 		}
 
 		private static String getComments(final IASTNode ast,
 				final TranslateCtx ctx) {
 			return ctx.getCommentCollector().getCommentNonMeta2(
 					ast.sourceStart());
 		}
 
 		// inner use only
 		private static IJstMethod createJstSynthesizedMethod(
 				final JsFuncType jsFuncType, final IFindTypeSupport ctx,
 				final String methName) {
 			final List<IJsCommentMeta> funcMetaList = new ArrayList<IJsCommentMeta>(
 					1);
 			funcMetaList.add(new OverwritableJsCommentMeta(DUMMY_FUNC_META,
 					jsFuncType));
 			return createJstSynthesizedMethod(funcMetaList, ctx, methName);
 		}
 
 		private static void attachOverloaded(final JstMethod dispatcher,
 				final JstMethod overloaded) {
 			dispatcher.addOverloaded(overloaded);
 			overloaded.setParent(dispatcher, false);
 		}
 
 		private static boolean isMetaMatchingAst(final IJsCommentMeta meta,
 				final MethodDeclaration astMtdDecl) {
 			if (meta == null) {
 				throw new IllegalArgumentException(
 						"meta for method or ast function expression couldn't be null");
 			}
 
 			final IArgument[] astArgs = astMtdDecl != null ? astMtdDecl
 					.getArguments() : null;
 			final JsTypingMeta typing = meta.getTyping();
 			if (!(typing instanceof JsFuncType)) {
 				return false;
 			}
 			if (((JsFuncType) typing).getParams().size() != (astArgs == null ? 0
 					: astArgs.length)) {
 				return false;
 			}
 
 			return true;
 		}
 
 		public static final class PotentialOtypeMemberTypeMeta implements
 				IJsCommentMeta {
 			private final IJsCommentMeta m_originalMeta;
 			private final IJstType m_potentialOtypeMemberType;
 
 			public PotentialOtypeMemberTypeMeta(final IJsCommentMeta meta,
 					final IJstType potentialOtypeMemberType) {
 				this.m_originalMeta = meta;
 				this.m_potentialOtypeMemberType = potentialOtypeMemberType;
 			}
 
 			public IJstType getPotentialOtypeMemberType() {
 				return m_potentialOtypeMemberType;
 			}
 
 			@Override
 			public boolean isMethod() {
 				return m_originalMeta.isMethod();
 			}
 
 			@Override
 			public boolean isCast() {
 				return m_originalMeta.isCast();
 			}
 
 			@Override
 			public boolean isAnnotation() {
 				return m_originalMeta.isAnnotation();
 			}
 
 			@Override
 			public JsTypingMeta getTyping() {
 				return m_originalMeta.getTyping();
 			}
 
 			@Override
 			public String getName() {
 				return m_originalMeta.getName();
 			}
 
 			@Override
 			public JstModifiers getModifiers() {
 				return m_originalMeta.getModifiers();
 			}
 
 			@Override
 			public int getEndOffset() {
 				return m_originalMeta.getEndOffset();
 			}
 
 			@Override
 			public DIRECTION getDirection() {
 				return m_originalMeta.getDirection();
 			}
 
 			@Override
 			public String getCommentSrc() {
 				return m_originalMeta.getCommentSrc();
 			}
 
 			@Override
 			public int getBeginOffset() {
 				return m_originalMeta.getBeginOffset();
 			}
 
 			@Override
 			public List<ArgType> getArgs() {
 				return m_originalMeta.getArgs();
 			}
 
 			@Override
 			public JsAnnotation getAnnotation() {
 				return m_originalMeta.getAnnotation();
 			}
 		}
 
 		public static final class PotentialAttributedTypeMeta implements
 				IJsCommentMeta {
 			private final IJsCommentMeta m_originalMeta;
 			private final IJstType m_potentialAttributedType;
 
 			public PotentialAttributedTypeMeta(final IJsCommentMeta meta,
 					final IJstType potentialAttributedType) {
 				this.m_originalMeta = meta;
 				this.m_potentialAttributedType = potentialAttributedType;
 			}
 
 			public IJstType getPotentialAttributedType() {
 				return m_potentialAttributedType;
 			}
 
 			@Override
 			public boolean isMethod() {
 				return m_originalMeta.isMethod();
 			}
 
 			@Override
 			public boolean isCast() {
 				return m_originalMeta.isCast();
 			}
 
 			@Override
 			public boolean isAnnotation() {
 				return m_originalMeta.isAnnotation();
 			}
 
 			@Override
 			public JsTypingMeta getTyping() {
 				return m_originalMeta.getTyping();
 			}
 
 			@Override
 			public String getName() {
 				return m_originalMeta.getName();
 			}
 
 			@Override
 			public JstModifiers getModifiers() {
 				return m_originalMeta.getModifiers();
 			}
 
 			@Override
 			public int getEndOffset() {
 				return m_originalMeta.getEndOffset();
 			}
 
 			@Override
 			public DIRECTION getDirection() {
 				return m_originalMeta.getDirection();
 			}
 
 			@Override
 			public String getCommentSrc() {
 				return m_originalMeta.getCommentSrc();
 			}
 
 			@Override
 			public int getBeginOffset() {
 				return m_originalMeta.getBeginOffset();
 			}
 
 			@Override
 			public List<ArgType> getArgs() {
 				return m_originalMeta.getArgs();
 			}
 
 			@Override
 			public JsAnnotation getAnnotation() {
 				return m_originalMeta.getAnnotation();
 			}
 		}
 
 		private static List<IJsCommentMeta> filterFuncMetas(
 				final IFindTypeSupport ctx, final List<IJsCommentMeta> original) {
 			if (original == null || original.isEmpty()) {
 				return Collections.emptyList();
 			} else {
 				final List<IJsCommentMeta> filteredFuncMetas = new LinkedList<IJsCommentMeta>();
 				for (IJsCommentMeta originalMeta : original) {
 					if (originalMeta != null) {// can't handle null meta
 						if (originalMeta.isMethod()
 								&& originalMeta.getTyping() instanceof JsFuncType) {
 							filteredFuncMetas
 									.addAll(expandFuncMetas4Params(originalMeta));
 						} else {// attributed type might be a valid method as
 								// well
 							final IJstType furtherAttemptedType = findType(ctx,
 									originalMeta.getTyping(), originalMeta);
 							if (!(furtherAttemptedType instanceof JstAttributedType || furtherAttemptedType instanceof JstFunctionRefType)) {
 								// TODO check with Yitao regarding the potential
 								// issue of this due to the otype function def
 								// translation happens in prior to otype
 								// linking, then the function ref type might yet
 								// be available
 								// causing the syntax error
 								// bugfix by huzhou@ebay.com as translating type
 								// could be null in case of floating javascript
 								// editing
 								filteredFuncMetas
 								.addAll(expandFuncMetas4Params(originalMeta));
 //								final IJstType translatingType = furtherAttemptedType
 //								ctx.getErrorReporter()
 //										.error("Cannot translate function meta: "
 //												+ originalMeta
 //												+ " to function declaration "
 //												+ " in "
 //												+ FunctionExpressionTranslator.class,
 //												translatingType != null ? translatingType
 //														.getName()
 //														: "unknown type",
 //												originalMeta.getBeginOffset(),
 //												originalMeta.getEndOffset(), 0,
 //												0);
 							}
 							else if (furtherAttemptedType instanceof JstAttributedType) {
 								filteredFuncMetas
 										.add(new PotentialAttributedTypeMeta(
 												originalMeta,
 												furtherAttemptedType));
 							} else {
 								filteredFuncMetas
 										.add(new PotentialOtypeMemberTypeMeta(
 												originalMeta,
 												furtherAttemptedType));
 							}
 						}
 					}
 				}
 				return filteredFuncMetas;
 			}
 		}
 
 		private static List<IJsCommentMeta> expandFuncMetas4Params(
 				final IJsCommentMeta original) {
 			final List<IJsCommentMeta> expanded = new LinkedList<IJsCommentMeta>();
 			// check optional
 			if (!hasOptionalArgs(original)) {
 				expanded.add(original);
 			} else {
 				final JsTypingMeta originalTyping = original.getTyping();
 				if (originalTyping != null
 						&& originalTyping instanceof JsFuncType) {
 					final List<JsParam> params = TranslateHelper
 							.getParams(original);
 					final int noneOptionalEnds = getNoneOptionalEnds(params);
 					/*
 					 * notice, the index starts with the noneOptionalStops, ends
 					 * at length of params + 1
 					 */
 					for (int paramCount = noneOptionalEnds, maxParamCount = params
 							.size() + 1; paramCount < maxParamCount; paramCount++) {
 						/*
 						 * bugfix by huzhou@ebay.com, if an optional param is
 						 * followed by a variable lengthed one, this meta should
 						 * be omitted, as the next one covers this one
 						 */
 						if (paramCount > 0
 								&& params.get(paramCount - 1).isOptional()
 								&& paramCount < params.size()
 								&& params.get(paramCount).isVariable()) {
 							continue;
 						}
 						expanded.add(getSynthesizedMethodCommentForOptional(
 								original, paramCount));
 					}
 				}
 			}
 			// check multi-values
 			if (!hasMultiValueParams(original)) {
 				return expanded;
 			} else {
 				final List<IJsCommentMeta> furtherExpanded = new LinkedList<IJsCommentMeta>();
 				for (IJsCommentMeta furtherExpanding : expanded) {
 					if (!hasMultiValueParams(furtherExpanding)) {
 						furtherExpanded.add(furtherExpanding);
 					} else {// the #expandFuncMetas4MultiValues is a relatively
 							// expensive recursion which shouldn't be invoked
 							// unless multi-value params were discovered
 						furtherExpanded
 								.addAll(expandFuncMetas4MultiValues(furtherExpanding));
 					}
 				}
 				return furtherExpanded;
 			}
 		}
 
 		private static int getNoneOptionalEnds(final List<JsParam> params) {
 			int noneOptionalStops = 0;
 			for (JsParam paramIt : params) {
 				if (paramIt.isOptional()) {
 					break;
 				}
 				noneOptionalStops++;
 			}
 			return noneOptionalStops;
 		}
 
 		private static boolean hasOptionalArgs(IJsCommentMeta meta) {
 			List<JsParam> params = TranslateHelper.getParams(meta);
 			if (params == null) {
 				return false;
 			}
 			for (JsParam p : params) {
 				if (p.isOptional()) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		private static boolean hasMultiValueParams(IJsCommentMeta meta) {
 			List<JsParam> params = TranslateHelper.getParams(meta);
 			if (params == null) {
 				return false;
 			}
 			for (JsParam p : params) {
 				if (p.getTypes().size() > 1) {
 					return true;
 				}
 			}
 			return false;
 		}
 
 		private static IJsCommentMeta getSynthesizedMethodCommentForOptional(
 				final IJsCommentMeta meta, final int paramCount) {
 			final List<JsParam> originalParams = TranslateHelper
 					.getParams(meta);
 			assert originalParams != null;
 
 			final List<JsParam> newParams = new ArrayList<JsParam>(
 					originalParams.size());
 			for (int i = 0; i < paramCount; i++) {
 				final JsParam originalParam = originalParams.get(i);
 				final List<JsTypingMeta> originalParamTypes = originalParam
 						.getTypes();
 				newParams.add(buildJsParam(originalParam.getName(),
 						originalParam.isFinal(), originalParam.isOptional(),
 						originalParam.isVariable(), originalParamTypes
 								.toArray(new JsTypingMeta[originalParamTypes
 										.size()])));
 			}
 			return new OverwritableJsCommentMeta(meta,
 					new OverwritableJsFuncType((JsFuncType) meta.getTyping(),
 							null, newParams));
 		}
 
 		private static List<IJsCommentMeta> expandFuncMetas4MultiValues(
 				final IJsCommentMeta original) {
 			final List<JsParam> originalJsParams = TranslateHelper
 					.getParams(original);
 			if (originalJsParams == null) {
 				throw new IllegalStateException(
 						"params should have been checked, and couldn't be null in this context");
 			}
 
 			final List<IJsCommentMeta> expanded = new LinkedList<IJsCommentMeta>();
 			if (originalJsParams == null || originalJsParams.size() == 0) {// should
 																			// never
 																			// be
 																			// here,
 																			// but
 																			// just
 																			// added
 																			// as
 																			// a
 																			// precaution
 				expanded.add(original);
 			} else {
 				for (List<JsParam> expandedParams : getFollowingParamsPermutations(
 						originalJsParams, 0)) {
 					expanded.add(getSynthesizedMethodCommentForMultiValues(
 							original, expandedParams));
 				}
 			}
 			return expanded;
 		}
 
 		private static IJsCommentMeta getSynthesizedMethodCommentForMultiValues(
 				final IJsCommentMeta originalMeta,
 				final List<JsParam> expandedParams) {
 			return new OverwritableJsCommentMeta(originalMeta,
 					new OverwritableJsFuncType(
 							(JsFuncType) originalMeta.getTyping(), null,
 							expandedParams));
 		}
 
 		/**
 		 * get the following params lists
 		 * 
 		 * @param originalJsParams
 		 * @param begin
 		 * @return all the possible following JsParam array permutations
 		 */
 		private static List<List<JsParam>> getFollowingParamsPermutations(
 				final List<JsParam> originalJsParams, int begin) {
 			final int paramsCount = originalJsParams.size();
 			if (begin >= paramsCount) {// error case, there's no corresponding
 										// param at this index at all
 				throw new IllegalArgumentException(
 						"param index exceeds the params count");
 			}
 
 			final JsParam paramAtBegin = originalJsParams.get(begin);
 			if (begin == paramsCount - 1) {// recursion ends at the last param
 				/*
 				 * notice, it uses linked list instead, as we don't exactly now
 				 * how many permutations will be generatedand it won't matter,
 				 * as eventually we're only iteratoring through the permutations
 				 */
 				final List<List<JsParam>> allPermutations = new LinkedList<List<JsParam>>();
 				for (JsTypingMeta paramMetaAtBegin : paramAtBegin.getTypes()) {
 					final List<JsParam> paramsGroup = initParamsGroup(paramsCount);
 					/*
 					 * notice, we filled the paramsGroup with null elements, and
 					 * using @see List#set instead of add which is much more
 					 * efficient
 					 */
 					paramsGroup.set(
 							begin,
 							createSingleTypedJsParam(paramAtBegin,
 									paramMetaAtBegin));
 					allPermutations.add(paramsGroup);
 				}
 				return allPermutations;
 			} else {// recursion happens when there're more following params
 					// permutations
 					// we look for the following permutations of params
 					// and permutate with the number of types the current param
 					// has
 					// grouped the permutations and send back to the parent
 					// recursive call
 				final List<List<JsParam>> allPermutations = new LinkedList<List<JsParam>>();
 				final List<List<JsParam>> followingPermutations = getFollowingParamsPermutations(
 						originalJsParams, begin + 1);
 				for (List<JsParam> paramsGroup : followingPermutations) {
 					if (paramAtBegin.getTypes().size() == 1) {
 						paramsGroup.set(begin, paramAtBegin);/*
 															 * notice, the
 															 * params group has
 															 * been inited,
 															 * using set other
 															 * than add is much
 															 * more efficient
 															 */
 						allPermutations.add(paramsGroup);
 					} else {
 						for (JsTypingMeta paramMetaAtBegin : paramAtBegin
 								.getTypes()) {
 							final JsParam newParamAtBegin = createSingleTypedJsParam(
 									paramAtBegin, paramMetaAtBegin);
 							/*
 							 * notice, this will be a fast copy of the params
 							 * group, it's replicated because the other possible
 							 * head params must be using the unaltered version
 							 * and the new param must be set to the begin index
 							 */
 							final List<JsParam> replicatedParamsGroup = new ArrayList<JsParam>(
 									paramsGroup);
 							replicatedParamsGroup.set(begin, newParamAtBegin);
 							allPermutations.add(replicatedParamsGroup);
 						}
 					}
 				}
 				return allPermutations;
 			}
 		}
 
 		/*
 		 * notice, the size of the list should be exactly the #paramsCountsince
 		 * this list will eventually hold on permutation of the params, which
 		 * will be just sized this much
 		 */
 		private static List<JsParam> initParamsGroup(final int paramsCount) {
 			final JsParam[] paramsArr = new JsParam[paramsCount];
 			Arrays.fill(paramsArr, null);// much faster than creating a List and
 											// adding paramsCount numbered nulls
 											// into it
 			return Arrays.asList(paramsArr);// here we derived a fixed sized
 											// List which is perfect to model
 											// the params array
 		}
 
 		/**
 		 * creates a new JsParam with all the information derived from the
 		 * original one except for the param typing the new
 		 * {@link JsParam#getTypes()} will only and always give back one result
 		 * alone
 		 * 
 		 * @param originalParam
 		 * @param typing
 		 * @return
 		 */
 		private static JsParam createSingleTypedJsParam(
 				final JsParam originalParam, final JsTypingMeta typing) {
 			return buildJsParam(originalParam.getName(),
 					originalParam.isFinal(), originalParam.isOptional(),
 					originalParam.isVariable(), typing);
 		}
 
 		public static JsParam buildJsParam(final String name,
 				final boolean isFinal, final boolean isOptional,
 				final boolean isVariable, final JsTypingMeta... typing) {
 			final JsParam newParam = new JsParam(name);
 			newParam.setFinal(isFinal);
 			newParam.setOptional(isOptional);
 			newParam.setVariable(isVariable);
 			for (JsTypingMeta it : typing) {
 				newParam.addTyping(it);
 			}
 			return newParam;
 		}
 
 		/**
 		 * this is to combine the types of params/return from all overloading
 		 * signatures into the dispatcher. the modifiers doesn't matter for the
 		 * function definition transalation at all but merely for some backwards
 		 * compatibilities required by some tests cases
 		 * 
 		 * @param dispatcher
 		 */
 		public static void fixDispatcher(final JstMethod dispatcher) {
 			fixArgsForDispatchMethod(dispatcher);
 			fixModifiersForDispatchMethod(dispatcher);
 			fixRtnTypeForDispatchMethod(dispatcher);
 		}
 
 		private static JstMethod createJstMethodWithoutMeta(
 				final MethodDeclaration astMtdDecl, IJsCommentMeta meta,
 				final TranslateCtx ctx, boolean useJsAnnotForArgs,
 				final String methName) {
 			final IArgument[] astParams = astMtdDecl == null ? null
 					: astMtdDecl.getArguments();
 			if (astParams == null || astParams.length == 0) {
 				return createJstMethod(astMtdDecl, astParams, meta, ctx,
 						useJsAnnotForArgs, methName);
 			}
 			// due to forcing optional
 			// the ast method is therefore overloaded instead
 			// the dispatcher has the max number of params
 			final JstMethod dispatcher = createJstMethod(astMtdDecl,
 					astMtdDecl.getArguments(), null, ctx, false, methName);
 			for (int paramsLength = 0, paramsMaxLength = astParams.length; paramsLength <= paramsMaxLength; paramsLength++) {
 				final IArgument[] overloadedAstParams = Arrays.copyOf(
 						astParams, paramsLength);
 				final JstMethod overloaded = createJstMethod(astMtdDecl,
 						overloadedAstParams, meta, ctx, true, methName);
 				attachOverloaded(dispatcher, overloaded);
 			}
 			// no need t fix dispatcher at all, as there's no meta to derive
 			// from
 			// fixDispatcher(dispatcher);
 			return dispatcher;
 		}
 
 		/**
 		 * creates the method <b>without</b> check of optional, multi-value,
 		 * overloading etc.
 		 * 
 		 * @param astMtdDecl
 		 * @param meta
 		 * @param ctx
 		 * @param useJsAnnotForArgs
 		 * @param methName
 		 * @return
 		 */
 		public static JstMethod createJstMethodNoMeta(IJsCommentMeta meta,
 				final TranslateCtx ctx, boolean useJsAnnotForArgs,
 				final String methName) {
 			return createJstMethod((MethodDeclaration) null, null, meta, ctx,
 					useJsAnnotForArgs, methName);
 		}
 
 		public static JstMethod createJstMethod(
 				final MethodDeclaration astMtdDecl, final IArgument[] astArgs,
 				IJsCommentMeta meta, final TranslateCtx ctx,
 				boolean useJsAnnotForArgs, final String methName) {
 			String methodName = methName;
 			if (methName == null) {
 				methodName = (meta != null) ? meta.getName()
 						: FunctionExpressionTranslator.DUMMY_METHOD_NAME;
 			}
 			JstMethod jstMethod;
 			if (VjoKeywords.CONSTRUCTS.equals(methodName)) {
 				jstMethod = new JstConstructor();
 			} else {
 				jstMethod = new JstMethod(methodName);
 
 				// astMtdDecl.get
 			}
 
 			
 
 			if (meta != null) {
 				jstMethod.setHasJsAnnotation(true);
 
 				jstMethod.setName(methodName);
 				TranslateHelper.setModifiersFromMeta(meta,
 						jstMethod.getModifiers());
 				
 				jstMethod.addCommentLocation(meta.getBeginOffset(), meta.getEndOffset(), true);
 				
 //				JsDocHelper.addJsDoc(meta, jstMethod);
 
 				// type params not arguments... meta.getArgs is a little
 				// confusing
 				for (ArgType arg : meta.getArgs()) {
 					jstMethod.addParam(arg.getName());
 				}
 
 				final JsTypingMeta typing = meta.getTyping();
 				if (typing != null) {
 					final JsTypingMeta returnType = getReturnTyping(meta);
 					jstMethod.setReturnOptional(typing.isOptional());
 					final IJstType retType = findType(ctx, returnType, meta);
 					jstMethod.setRtnType(retType);
 					if (typing instanceof JsFuncType) {
 						JsFuncType funcType = (JsFuncType) typing;
 						if (funcType.isTypeFactoryEnabled()) {
 							jstMethod.setTypeFactoryEnabled(true);
 						}
 						if (funcType.isFuncArgMetaExtensionEnabled()) {
 							jstMethod.setFuncArgMetaExtensionEnabled(true);
 						}
 					}
 
 				}
 				TranslateHelper.setReferenceSource(jstMethod, meta);
 			}
 
 			if (ctx.getCurrentScope() == ScopeIds.PROPS) {
 				jstMethod.getModifiers().merge(JstModifiers.STATIC);
 			}
 
 			// get the args from function signature and create JstArgs
 			List<JsTypingMeta> types;
 			if (astArgs != null) {
 				if (meta != null) {
 					int idx = 0;
 					for (IArgument astArg : astArgs) {
 						types = new ArrayList<JsTypingMeta>();
 						if (meta != null) {
 							List<JsParam> params = getParams(meta);
 							if (params != null && params.size() > idx) {
 								types = params.get(idx).getTypes();
 							}
 						}
 						if (!types.isEmpty() || !useJsAnnotForArgs) {
 							ArgumentTranslator atrans = (ArgumentTranslator) TranslatorFactory
 									.getTranslator(astArg, ctx);
 							atrans.setCommentMetaAndIndex(meta, idx);
 							JstArg jstArg = atrans.doTranslate(jstMethod,
 									astArg);
 							jstMethod.addArg(jstArg);
 						}
 						idx++;
 					}
 					// count of args from signature and annotations are not
 					// matchig, we need to create extra args specified in the
 					// annotation
 					int astArgsCount = idx;
 					List<JsParam> params = getParams(meta);
 					int annArgsCount = (params == null) ? 0 : params.size();
 					if (astArgsCount < annArgsCount) {
 						for (int i = astArgsCount; i < annArgsCount; i++) {
 							JstArg jstArg = createJstArg(jstMethod,
 									params.get(i).getName(), null, meta, i, ctx);
 							if (jstArg != null) {
 								jstMethod.addArg(jstArg);
 							}
 						}
 					}
 				} else {
 					for (IArgument astArg : astArgs) {
 						ArgumentTranslator atrans = (ArgumentTranslator) TranslatorFactory
 								.getTranslator(astArg, ctx);
 						JstArg jstArg = atrans.doTranslate(jstMethod, astArg);
 						jstMethod.addArg(jstArg);
 					}
 				}
 			} else {
 				// Here when function expression has no arguments
 				if (meta != null && useJsAnnotForArgs) {
 					// get the args from function annotation and create JstArgs
 					List<JsParam> params = getParams(meta);
 					if (params != null && params.size() > 0) {
 						int idx = 0;
 						types = new ArrayList<JsTypingMeta>();
 						for (JsParam param : params) {
 							String argName = param.getName() != null ? param
 									.getName() : DEFAULT_ARG_PREFIX + idx;
 							JstArg jstArg = createJstArg(jstMethod, argName,
 									null, meta, idx, ctx);
 							if (params.size() > idx) {
 								types = params.get(idx).getTypes();
 							}
 							if (!types.isEmpty()) {
 								jstMethod.addArg(jstArg);
 								idx++;
 							}
 						}
 					}
 				}
 			}
 			if (astMtdDecl != null && jstMethod != null) {
 				jstMethod.getName().setSource(
 						TranslateHelper.getSourceFunc(astMtdDecl,
 								ctx.getSourceUtil()));
 			}
 			return jstMethod;
 		}
 
 		/**
 		 * creates the synthesized method <b>without</b> check of optional,
 		 * multi-value, overloading etc.
 		 * 
 		 * @param meta
 		 * @param ctx
 		 * @param useJsAnnotForArgs
 		 * @param methName
 		 * @return
 		 */
 		private static JstSynthesizedMethod createJstSynthesizedMethod(
 				IJsCommentMeta meta, final IFindTypeSupport ctx,
 				boolean useJsAnnotForArgs, final String methName) {
 
 			String methodName = methName;
 			if (methName == null) {
 				methodName = (meta != null) ? meta.getName() : EMPTYSTRING;
 			}
 
 			final JstSynthesizedMethod jstMethod = new JstSynthesizedMethod(
 					methodName, new JstModifiers(), (JstArg[])null);
 			if (meta != null) {
 				// handle modifier
 				TranslateHelper.setModifiersFromMeta(meta,
 						jstMethod.getModifiers());
 
 				for (ArgType arg : meta.getArgs()) {
 					jstMethod.addParam(arg.getName());
 				}
 
 				if (meta.getTyping() != null) {
 					final JsTypingMeta retTyping = getReturnTyping(meta);
 					final IJstType retType = findType(ctx, retTyping, meta);
 					jstMethod.setRtnType(retType);
 					jstMethod.setReturnOptional(retTyping.isOptional());
 				}
 			}
 
 			if (meta != null && useJsAnnotForArgs) {
 				// get the args from function annotation and create JstArgs
 				List<JsParam> params = getParams(meta);
 				if (params != null && params.size() > 0) {
 					int idx = 0;
 					List<JsTypingMeta> types = new ArrayList<JsTypingMeta>();
 					for (JsParam param : params) {
 						String argName = param.getName() != null ? param
 								.getName() : DEFAULT_ARG_PREFIX + idx;
 						JstArg jstArg = createJstArg(jstMethod, argName, null,
 								meta, idx, ctx);
 						if (params.size() > idx) {
 							types = params.get(idx).getTypes();
 						}
 						if (!types.isEmpty()) {
 							jstMethod.addArg(jstArg);
 							idx++;
 						}
 					}
 				}
 			}
 			return jstMethod;
 		}
 	}
 
 	/**
 	 * This JsFuncType overwrites the original JsFuncType's @see
 	 * {@link JsFuncType#getFuncName()} and possibly @see
 	 * {@link JsFuncType#getParams()} which allowed us to expand the meta to
 	 * ensure that each meta's {@link JsFuncType#getParams()}
 	 * {@link JsParam#getTypes()} are single elemented
 	 * 
 	 * 
 	 */
 	private static class OverwritableJsFuncType extends JsFuncType {
 		private final JsFuncType m_originalJsFunc;
 		private String m_overwriteName;
 		private List<JsParam> m_overwriteJsParams;
 
 		public OverwritableJsFuncType(final JsFuncType originalJsFunc,
 				final String overwriteName,
 				final List<JsParam> overwriteJsParams) {
 			super(originalJsFunc.getReturnType());
 
 			m_originalJsFunc = originalJsFunc;
 			m_overwriteName = overwriteName;
 			m_overwriteJsParams = overwriteJsParams;
 
 			if (originalJsFunc != null) {
 				setDimension(originalJsFunc.getDimension());
 				setFinal(originalJsFunc.isFinal());
 				setVariable(originalJsFunc.isVariable());
 			}
 		}
 
 		@Override
 		public boolean isOptional() {
 			return m_originalJsFunc.isOptional();
 		}
 
 		@Override
 		public JsTypingMeta getReturnType() {
 			return m_originalJsFunc.getReturnType();
 		}
 
 		@Override
 		public String getFuncName() {
 			return m_overwriteName != null ? m_overwriteName : m_originalJsFunc
 					.getFuncName();
 		}
 
 		@Override
 		public List<JsParam> getParams() {
 			return m_overwriteJsParams != null ? m_overwriteJsParams
 					: m_originalJsFunc.getParams();
 		}
 
 		@Override
 		public String getType() {
 			return m_originalJsFunc.getType();
 		}
 	}
 
 	/**
 	 * @see MethodTranslateHelper#createJstSynthesizedMethod(JsFuncType,
 	 *      TranslateCtx, String) This dummy meta is just a holder for @see
 	 *      JsFuncType which used for @see JstSynthesizedMethod creation
 	 */
 	private final static IJsCommentMeta DUMMY_FUNC_META = new IJsCommentMeta() {
 		@Override
 		public boolean isMethod() {
 			return true;
 		}
 
 		@Override
 		public boolean isCast() {
 			return false;
 		}
 
 		@Override
 		public boolean isAnnotation() {
 			return false;
 		}
 
 		@Override
 		public JsTypingMeta getTyping() {
 			return null;
 		}
 
 		@Override
 		public String getName() {
 			return "";
 		}
 
 		@Override
 		public JstModifiers getModifiers() {
 			return new JstModifiers();
 		}
 
 		@Override
 		public int getEndOffset() {
 			return 0;
 		}
 
 		@Override
 		public DIRECTION getDirection() {
 			return DIRECTION.FORWARD;
 		}
 
 		@Override
 		public String getCommentSrc() {
 			return "";
 		}
 
 		@Override
 		public int getBeginOffset() {
 			return 0;
 		}
 
 		@Override
 		public List<ArgType> getArgs() {
 			return Collections.emptyList();
 		}
 
 		@Override
 		public JsAnnotation getAnnotation() {
 			return null;
 		}
 	};
 
 	/**
 	 * This meta overwrites the original meta's {@link #getTyping()} information
 	 * The overwritten {@link #getTyping()} usually reduced the number of @see
 	 * {@link JsParam#getTypes()} or reset the @see {@link JsParam#isOptional()}
 	 * , those information will instead be expanded as a new
 	 * 
 	 * @see IJsCommentMeta for the overloading api translation
 	 * 
 	 * 
 	 */
 	private static class OverwritableJsCommentMeta implements IJsCommentMeta {
 		private final IJsCommentMeta m_originalMeta;
 		private final JsFuncType m_jsFuncType;
 
 		public OverwritableJsCommentMeta(final IJsCommentMeta meta,
 				final JsFuncType func) {
 			this.m_originalMeta = meta;
 			this.m_jsFuncType = func;
 		}
 
 		@Override
 		public boolean isMethod() {
 			return m_originalMeta.isMethod();
 		}
 
 		@Override
 		public boolean isCast() {
 			return m_originalMeta.isCast();
 		}
 
 		@Override
 		public boolean isAnnotation() {
 			return m_originalMeta.isAnnotation();
 		}
 
 		@Override
 		public JsTypingMeta getTyping() {
 			return m_jsFuncType;
 		}
 
 		@Override
 		public String getName() {
 			return m_originalMeta.getName();
 		}
 
 		@Override
 		public JstModifiers getModifiers() {
 			return m_originalMeta.getModifiers();
 		}
 
 		@Override
 		public int getEndOffset() {
 			return m_originalMeta.getEndOffset();
 		}
 
 		@Override
 		public DIRECTION getDirection() {
 			return m_originalMeta.getDirection();
 		}
 
 		@Override
 		public String getCommentSrc() {
 			return m_originalMeta.getCommentSrc();
 		}
 
 		@Override
 		public int getBeginOffset() {
 			return m_originalMeta.getBeginOffset();
 		}
 
 		@Override
 		public List<ArgType> getArgs() {
 			return m_originalMeta.getArgs();
 		}
 
 		@Override
 		public JsAnnotation getAnnotation() {
 			return m_originalMeta.getAnnotation();
 		}
 	}
 }
