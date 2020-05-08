 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
  package org.eclipse.vjet.dsf.jstojava.translator.robust.ast2jst;
 
 import java.util.List;
 import java.util.Stack;
 
 import org.eclipse.mod.wst.jsdt.core.ast.IASTNode;
 import org.eclipse.mod.wst.jsdt.core.ast.IBinaryExpression;
 import org.eclipse.mod.wst.jsdt.core.ast.IExpression;
 import org.eclipse.mod.wst.jsdt.core.ast.ILiteral;
 import org.eclipse.mod.wst.jsdt.core.ast.IPostfixExpression;
 import org.eclipse.mod.wst.jsdt.core.ast.IProgramElement;
 import org.eclipse.mod.wst.jsdt.core.compiler.CategorizedProblem;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.AND_AND_Expression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.AllocationExpression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ArrayInitializer;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ConditionalExpression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.EqualExpression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.Expression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.FieldReference;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.FunctionExpression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.MessageSend;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.OR_OR_Expression;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ObjectLiteral;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.ObjectLiteralField;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.SingleNameReference;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.TrueLiteral;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.UnaryExpression;
 import org.eclipse.vjet.dsf.common.exceptions.DsfRuntimeException;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.ISynthesized;
 import org.eclipse.vjet.dsf.jst.JstCommentLocation;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstDefaultConstructor;
 import org.eclipse.vjet.dsf.jst.declaration.JstFactory;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstGlobalFunc;
 import org.eclipse.vjet.dsf.jst.declaration.JstGlobalProp;
 import org.eclipse.vjet.dsf.jst.declaration.JstGlobalVar;
 import org.eclipse.vjet.dsf.jst.declaration.JstInferredType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstModifiers;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.declaration.JstProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstSynthesizedProperty;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeReference;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.FuncExpr;
 import org.eclipse.vjet.dsf.jst.expr.JstArrayInitializer;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.expr.ObjCreationExpr;
 import org.eclipse.vjet.dsf.jst.expr.TextExpr;
 import org.eclipse.vjet.dsf.jst.meta.IJsCommentMeta;
 import org.eclipse.vjet.dsf.jst.meta.JsTypingMeta;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.term.JstLiteral;
 import org.eclipse.vjet.dsf.jst.term.NV;
 import org.eclipse.vjet.dsf.jst.term.SimpleLiteral;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.token.ISimpleTerm;
 import org.eclipse.vjet.dsf.jst.util.JstTypeHelper;
 import org.eclipse.vjet.dsf.jstojava.parser.comments.JsParam;
 import org.eclipse.vjet.dsf.jstojava.translator.BlockTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.JsDocHelper;
 import org.eclipse.vjet.dsf.jstojava.translator.JstUtil;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateConfig;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateCtx;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateHelper;
 import org.eclipse.vjet.dsf.jstojava.translator.TypeTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.IRobustTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.TypeRobustTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.VjoRobustTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletionOnSingleNameReference;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstFieldOrMethodCompletion;
 import org.eclipse.vjet.vjo.meta.VjoKeywords;
 
 public class VjoOLFieldTranslator extends
 		BaseAst2JstTranslator<ObjectLiteralField, Object> {
 	private static String MISSING = TranslateHelper.MISSING_TOKEN;
 
 //	private static ArrayList<String> NOT_NEEDED_IMPORTS = new ArrayList<String>();
 //	static {
 //		NOT_NEEDED_IMPORTS.add("int");
 //		NOT_NEEDED_IMPORTS.add("float");
 //		NOT_NEEDED_IMPORTS.add("byte");
 //		NOT_NEEDED_IMPORTS.add("short");
 //		NOT_NEEDED_IMPORTS.add("double");
 //		NOT_NEEDED_IMPORTS.add("long");
 //		NOT_NEEDED_IMPORTS.add("boolean");
 //		NOT_NEEDED_IMPORTS.add("char");
 //		NOT_NEEDED_IMPORTS.add("Object");
 //		NOT_NEEDED_IMPORTS.add("String");
 //		NOT_NEEDED_IMPORTS.add("Enum");
 //	}
 
 	public VjoOLFieldTranslator() {
 		
 	}
 	
 	public VjoOLFieldTranslator(TranslateCtx ctx) {
 		super.setTranslateCtx(ctx);
 	}
 
 	@Override
 	protected Object doTranslate(ObjectLiteralField astObjectliteralField) {
 
 		if (m_ctx.isSkipJsExtSyntaxArgs()) {
 			return null;
 		}
 
 		IExpression initializer = astObjectliteralField.getInitializer();
 		IExpression fieldName = astObjectliteralField.getFieldName();
 
 		if (ScopeIds.OPTIONS.equals(m_ctx.getCurrentScope()) && m_parent instanceof JstType) {
 			if (fieldName instanceof SingleNameReference) {
 				JstType type = (JstType)m_parent;
 				String name = ((SingleNameReference)fieldName).toString();
 				if (VjoKeywords.METATYPE_OPTION.equals(name)) {
 					type.setMetaType(initializer instanceof TrueLiteral);				
 				}
 				else {
 					type.addOption(name, initializer.toString()); //TODO
 				}
 			}
 			return null;
 		}
 		
 		Object result = null;
 		if (initializer instanceof FunctionExpression) {
 			FunctionExpressionTranslator translator = (FunctionExpressionTranslator) getTranslator(initializer);
 			translator.setParent(this.m_parent);
 			FuncExpr functionExpr = translator
 					.doTranslate((FunctionExpression) initializer, getSimpleName(astObjectliteralField));
 			translator.checkForCompletion((FunctionExpression) initializer);
 			if (m_parent == null && fieldName instanceof SingleNameReference) {
 				return new NV(((SingleNameReference) fieldName).toString(),
 						functionExpr);
 			}
 			JstMethod meth = null;
 			if(ScopeIds.GLOBAL.equals(m_ctx.getCurrentScope())){
 				meth = buildMethod(fieldName, functionExpr.getFunc(), false);
 				meth =  functionExpr.getFunc();
 				
 				JstGlobalFunc globalFunc = new JstGlobalFunc(meth);
 				JstGlobalVar globalVar = new JstGlobalVar(globalFunc);
 				globalVar.setScopeForGlobal(m_ctx.getScopeForGlobals());
 				
 				if(m_parent instanceof JstType){
 					((JstType)m_parent).addGlobalVar(globalVar);
 				}
 				
 			}else{
 				meth = buildMethod(fieldName, functionExpr.getFunc(), true);
 				if (m_parent instanceof JstType && ((JstType) m_parent).isOType()) {
 					
 					System.out.println("doTranslate");
 					JstFunctionRefType ref = new JstFunctionRefType(meth);
 					((JstType) m_parent).addOType(ref);
 					ref.setPackage(new JstPackage(((IJstType) m_parent).getName()));
 					checkIfTypeExistsForFunction(ref);					
 				}
 			}
 			result = meth;
 
 		} else if (isPropertyInitExpression(initializer)) {
 			
 			result = processCorrectProperty(astObjectliteralField);
 		} else if (MISSING.equals(initializer.toString())) {
 			// prepare for possible completion after $missing$
 			if (onlyWhiteSpaces(astObjectliteralField.sourceEnd, m_ctx
 					.getCompletionPos())) {
 				astObjectliteralField.initializer.sourceEnd = m_ctx
 						.getCompletionPos() - 1;
 				astObjectliteralField.sourceEnd = astObjectliteralField.initializer.sourceEnd;
 			}
 		} else if (initializer instanceof FieldReference) {
 			result = checkForProblemAndProcess(astObjectliteralField);
 			if (result == null) {
 				result = processCorrectProperty(astObjectliteralField);
 			}
 		} else if (initializer instanceof ConditionalExpression) {
 			result = checkForProblemAndProcess(astObjectliteralField);
 		} else {
 			Stack<IASTNode> list = JstUtil.createMessageSendStack(initializer);
 			if (JstUtil.isInnerType(list)) {
 				String typeName = JstUtil.getInnerTypeName(list);
 				result = processInnerType(astObjectliteralField, typeName);
 			} else {
 				processCorrectProperty(astObjectliteralField);
 			}
 		}
 
 		return result;
 	}
 
 	private boolean isPropertyInitExpression(IExpression initializer) {
 		return initializer instanceof ILiteral
 				|| initializer instanceof ObjectLiteral
 				|| initializer instanceof ArrayInitializer
 				|| initializer instanceof UnaryExpression
 				|| initializer instanceof IBinaryExpression
 				|| initializer instanceof IPostfixExpression
 				|| initializer instanceof SingleNameReference
 				|| initializer instanceof EqualExpression
 				|| initializer instanceof ConditionalExpression
 				|| initializer instanceof OR_OR_Expression
 				|| initializer instanceof AND_AND_Expression
 				|| initializer instanceof AllocationExpression;
 	}
 
 	private void createJstBlock(IProgramElement elem, IJstType innerType) {
 
 		List<JstBlock> listBlocks = m_ctx.getScriptUnitBlockList();
 
 		if (listBlocks == null) {
 			return;
 		}
 
 		TranslateConfig cfg = new TranslateConfig();
 		cfg.setSkiptImplementation(false);
 		cfg.setSkipJsExtSyntaxArgs(true);
 		TranslateCtx ctx2 = new TranslateCtx(cfg);
 		ctx2.setCurrentType((JstType)innerType);
 		ctx2.setCreatedCompletion(m_ctx.isCreatedCompletion());
 		ctx2.setCompletionPos(m_ctx.getCompletionPos());
 		ctx2.getScopeStack().addAll(m_ctx.getScopeStack());
 		ctx2.setAST(m_ctx.getAST());
 
 		listBlocks.add(BlockTranslator.createJstBlock(ctx2, elem));
 		copyInnerCtxToOut(ctx2, m_ctx);
 
 	}
 
 	/**
 	 * Copy the innerCtx's information to outerCtx, which will happen after inner type was resolved
 	 * @param innerCtx
 	 * @param outerCtx
 	 * 
 	 */
 	 private void copyInnerCtxToOut(TranslateCtx innerCtx, TranslateCtx outerCtx) {
 		outerCtx.setCreatedCompletion(innerCtx.isCreatedCompletion());
 		List<JstCompletion> list = innerCtx.getJstErrors();
 		if (list != null && !list.isEmpty()) {
 			outerCtx.addSyntaxError(list.get(0));
 		}
 	}
 
 	private Object processInnerType(ObjectLiteralField field, String typeName) {
 
 		JstType currentType = m_ctx.getCurrentType();
 
 		String fullName = currentType.getName() + "." + field.getFieldName();
 
 		JstType innerType = JstCache.getInstance().getType(fullName);
 			
 		if (innerType == null) {
 			innerType = createInnerType(field);
 			currentType.addInnerType(innerType);
 			JstCache.getInstance().addType(innerType);
 		} else {
 			// clear inner type before populating it
 			clearInnerType(innerType);
 			updateInnerType(field, innerType);
 			currentType.addInnerType(innerType);
 		}
 				
 		if (typeName != null) {
 			typeName = typeName.trim();
 			//extract generic params from typeName
 			if (typeName.startsWith("<") && typeName.endsWith(">")) {
 				TranslateHelper.addParamsToType(m_ctx, innerType, typeName.substring(1,typeName.length()-1));
 			}			
 		}
 
 		m_ctx.setCurrentType(innerType);
 		processInnerType(field, innerType);
 		createJstBlock(field.getInitializer(), innerType);
 		m_ctx.setCurrentType(currentType);
 
 		List<IJsCommentMeta> meta = getCommentMeta(field);
 		if (meta.size() > 0) {
 			TranslateHelper.setModifiersFromMeta(meta.get(0), innerType
 					.getModifiers());
 		}
 
 		createInnerTypeRefProperty(currentType, innerType);
 		updateInnerTypeReferences(currentType, innerType);
 		
 		// Add a default constructor to the ctype if no constructor is
 		// defined.
 		if (innerType.isClass()) {
 			JstMethod constructor = innerType.getConstructor();
 			if (constructor == null) {
 				innerType.setConstructor(new JstDefaultConstructor(innerType));
 			}
 		}
 
 		return innerType;
 	}
 
 	private void createInnerTypeRefProperty(JstType currentType,
 			JstType innerType) {
 		if (currentType.isOType()) return;
 		
 		JstModifiers mod = innerType.getModifiers().getCopy();
 		//TODO - We need revisit about setting to non-FINAL
 		JstSynthesizedProperty property = 
 			new JstSynthesizedProperty(getTypeRef(innerType), innerType.getSimpleName(),
 					(JstIdentifier)null, mod);
 		property.setParent(currentType);
 		// check if this inner type synthesized property already exists
 		if (currentType.getProperty(property.getName().getName()) != null) {
 			int line=0;
 			int column=0;
 			int start=0;
 			int end=0;
 			JstSource src = innerType.getSource();
 			if (src != null) {
 				line = src.getRow();
 				column = src.getColumn();
 				start = src.getStartOffSet();
 				end = src.getEndOffSet();
 			}
 			m_ctx.getErrorReporter().error(
 					"Inner type: " + innerType.getSimpleName() + " already exists in type: "
 							+ m_ctx.getCurrentType().getName(),
 					m_ctx.getCurrentType().getName(), start, end, line, column);
 		} else {
 			currentType.addProperty(property);
 		}
 	}
 	
 	private IJstRefType getTypeRef(IJstType type) {
 		if (type instanceof IJstRefType) {
 			return (IJstRefType)type;
 		}
 		return JstTypeHelper.getJstTypeRefType(type);
 	}
 
 	private void clearInnerType(JstType innerType) {
 		innerType.clearAll();
 		innerType.setPackage(null);
 	}
 
 	private void updateInnerTypeReferences(JstType currentType,
 			JstType innerType) {
 		if (currentType == null || innerType == null)
 			return;
 
 		// Go back and look at existing properties and methods to see if they
 		// are using this inner type
 		String innerTypeName = innerType.getSimpleName();
 		if (innerTypeName == null)
 			return;
 
 		for (IJstProperty pty : currentType.getProperties()) {
 			if (pty instanceof ISynthesized) {
 				continue;
 			}
 			if (innerTypeName.equals(pty.getType().getName())) {
 				if (pty instanceof JstProperty) {
 					((JstProperty) pty).setType(innerType);
 				}
 			}
 		}
 
 		for (IJstMethod mtd : currentType.getMethods()) {
 			if (mtd instanceof JstMethod) {
 				// Check for return type
 				JstMethod method = ((JstMethod) mtd);
 				IJstType rtnType = method.getRtnType();
 				if (rtnType != null) {
 					if (rtnType instanceof IJstTypeReference) {
 						rtnType = ((IJstTypeReference) rtnType)
 								.getReferencedType();
 					}
 					if (innerTypeName.equals(rtnType.getName())) {
 						if (rtnType instanceof JstType) {
 							method.setRtnType(innerType);
 						}
 					}
 				}
 				// Check for params...
 				for (int i = 0; i < mtd.getArgs().size(); i++) {
 					JstArg arg = mtd.getArgs().get(i);
 					for (IJstType argType : arg.getTypes()) {
 						if (argType == null)
 							continue;
 						if (innerTypeName.equals(argType.getName())) {
 							arg.updateType(argType.getName(), innerType);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private String getSimpleName(ObjectLiteralField field) {
 		Expression expression = field.fieldName;
 		String name = JstUtil.getName(expression);
 		return name;
 	}
 
 	private void processInnerType(ObjectLiteralField field, JstType innerType) {
 
 		TypeTranslator translator = new TypeTranslator(m_ctx);
 		MessageSend obj = (MessageSend) field.initializer;
 
 		IProgramElement rootElem = null;
 		MessageSend send = obj;
 		Stack<IProgramElement> astElements = new Stack<IProgramElement>();
 
 		do {
 			if (JstUtil.isType(String.valueOf(send.selector))) {
 				rootElem = send;
 				astElements.add(send);
 			}
 			if (send.receiver instanceof SingleNameReference) {
 				send = null;
 			} else {
 				astElements.add(send);
 				send = (MessageSend) send.receiver;
 			}
 		} while (send != null);
 
 		if (astElements.size() > 0) {
 			VjoRobustTranslator vjoT = new VjoRobustTranslator(m_ctx);
 			IRobustTranslator t = vjoT.getSubTranslator(rootElem);
 			t.configure(astElements, innerType, translator,
 					TypeRobustTranslator.getIntegerHolder(m_ctx
 							.getCompletionPos()));
 			t.transform();
 			JstSource source = createSource(field.fieldName.sourceStart, field.fieldName.sourceEnd,
 					m_ctx.getSourceUtil());
 			innerType.setSource(source);
 			
 		}
 	}
 
 	private JstType createInnerType(ObjectLiteralField field) {
 		JstType innerType = JstFactory.getInstance().createJstType(false);
 		
 		return updateInnerType(field, innerType);
 	}
 
 	private JstType updateInnerType(ObjectLiteralField field, JstType innerType) {
 		if(m_ctx.getCurrentScope().equals(ScopeIds.PROTOS)){
 			innerType.getModifiers().setStatic(false).setDefault();
 		} else if(m_ctx.getCurrentScope().equals(ScopeIds.PROPS)){
 			innerType.getModifiers().setStatic(true).setDefault();
 		}
 		else if(m_ctx.getCurrentScope().equals(ScopeIds.PROPS)){
 			innerType.getModifiers().setStatic(true).setDefault();
 		}
 		
 		// TODO revist this default status
 		innerType.getModifiers().setAbstract(false).setFinal(false);
 		
 		JstSource source = createSource(field.sourceStart, field.sourceEnd,
 				m_ctx.getSourceUtil());
 		innerType.setSource(source);
 		//TODO
 		String name = getSimpleName(field);
 		innerType.setSimpleName(name);
 		return innerType;
 	}
 
 	private JstObjectLiteralType createOType(ObjectLiteralField field) {
 		String name = getSimpleName(field);
 		JstObjectLiteralType otype = new JstObjectLiteralType(name);
 		if (m_parent instanceof IJstType) {
 			otype.setPackage(new JstPackage(((IJstType) m_parent).getName()));
 		}
 
 		JstSource source = createSource(field.sourceStart, field.sourceEnd,
 				m_ctx.getSourceUtil());
 		otype.setSource(source);
 		otype.setSimpleName(name);
 		return otype;
 	}
 
 	private JstMethod buildMethod(IExpression fieldName, JstMethod jstMethod, boolean addToParent) {
 		jstMethod = TranslateHelper.createRealMethod(fieldName, jstMethod,
 				m_ctx);
 
 		if (addToParent == true && m_parent != null && m_parent instanceof JstType) {
 			if (m_ctx.getCurrentScope() == ScopeIds.PROTOS
 					&& VjoKeywords.CONSTRUCTS.equals(fieldName.toString())) {
 				for (IJstMethod mtd : jstMethod.getOverloaded()) {
 					((JstType) m_parent).removeChild(mtd);
 				}
 				((JstType) m_parent).setConstructor(jstMethod);
 			} else {
 //				for (IJstMethod mtd : jstMethod.getOverloaded()) {
 //					((JstType) m_parent).removeMethod(mtd.getName().getName(), 
 //							mtd.isStatic());
 //				}
 				((JstType) m_parent).addMethod(jstMethod);
 			}
 			jstMethod.setParent(m_parent);
 		}
 		
 		if(jstMethod.getParentNode()==null)
 			throw new DsfRuntimeException("null parent not expected");
 		
 		return jstMethod;
 	}
 
 	private Object processCorrectProperty(
 			ObjectLiteralField astObjectliteralField) {
 		// store completion position
 		int completionPos = m_ctx.getCompletionPos();
 		// turn off completion check for children
 		// m_ctx.setCompletionPos(-1);
 		String simpleName = getSimpleName(astObjectliteralField);
 		IExpr value = null;
 		JstType parent = null;
 		BaseJstNode prevParent = m_parent;
 		List<IJsCommentMeta> metaArr = null;
 		if (!isEnumContext()) {
 			m_ctx.enterBlock(ScopeIds.PROPERTY);
 		}
 		// boolean isOtype = false;
 		try {
 			if (astObjectliteralField.initializer instanceof ObjectLiteral) {
 				if (m_parent != null && m_parent instanceof JstType
 						&& ((JstType) m_parent).isOType()) {
 					String fullName = ((IJstType)m_parent).getName() + "." + simpleName;
 	
 					parent = JstCache.getInstance().getType(fullName);
 	
 					if (parent == null || !(parent instanceof JstObjectLiteralType)) {
 						// old parent will be replaced by OType, set old reference to phantom
 						if (parent != null) {
 							parent.getStatus().setIsPhantom();
 						}
 						
 						parent = createOType(astObjectliteralField);
 						JstCache.getInstance().addOType(parent);
 					} else {
 						// clear oType before populating it
 						clearInnerType(parent);
 						JstSource source = createSource(astObjectliteralField.sourceStart, 
 								astObjectliteralField.sourceEnd,
 								m_ctx.getSourceUtil());
 						parent.setSource(source);
 						parent.setPackage(new JstPackage(((IJstType) m_parent).getName()));
 						parent.setSimpleName(simpleName);
 					}
 					
 					
 					m_ctx.setCurrentType(parent);
 					// isOtype = true;
 					if (m_parent != null && m_parent instanceof IJstType) {
 						// m_parent.addChild(parent);
 						((IJstType) m_parent)
 								.addOType((JstObjectLiteralType) parent);
 					}
 					m_parent = parent;
 				}
 			}
 
 			if (isEnumContext()) {
 				value = createEnumValue(astObjectliteralField.initializer);
 			} else{
 				value = (IExpr) getTranslatorAndTranslate(astObjectliteralField.initializer);
 			}
 		} finally {
 			if (!isEnumContext()) {
 				m_ctx.exitBlock();
 			}
 			// restore previous completion position
 			m_ctx.setCompletionPos(completionPos);
 			metaArr = getCommentMeta(astObjectliteralField);
 		}
 		m_parent = prevParent;
 		if (prevParent instanceof JstType) {
 			m_ctx.setCurrentType((JstType) prevParent);
 		}
 		final boolean isTypedObjLiteral = m_parent instanceof JstObjectLiteralType;
 		final boolean isMeth = metaArr != null 
 			&& metaArr.size() > 0
 			&& metaArr.get(0).isMethod();
 
 		boolean isFnExpr = false;
 		// This allows for function references to be translated correctly ie vjo.NEEDS_IMPL
 		if (value instanceof FieldAccessExpr) {
 			IJstType type = ((FieldAccessExpr) value).getType();
 			IJstType fnType = JstCache.getInstance().getType("Function");
 			if (fnType != null && fnType.equals(type)) {
 				isFnExpr = true;
 			}
 		}
 
 		final boolean isFuncRef = isMeth 
 			&& m_parent instanceof JstType
 			&& ((JstType) m_parent).isOType();
 		Object result;
 		if (m_parent == null || !(m_parent instanceof JstType)) {
 			result = buildNV(astObjectliteralField, value, metaArr);
 		} else if ((m_ctx.getCurrentScope() == ScopeIds.PROPERTY || isFuncRef)
 				&& !isTypedObjLiteral) {
 			result = buildNV(astObjectliteralField, value, metaArr);
 			if (isFuncRef) {
 				addFuncRef(((JstType) m_parent), astObjectliteralField, metaArr);
 			}
 		} else {
 			JstType current = ((JstType) m_parent);
 			if (isFnExpr || isMeth) {
 					// TODO this needs to handle the overloading for optional args
 				
 				JstMethod meth = TranslateHelper.createRealMethod(
 					astObjectliteralField.fieldName,
 					TranslateHelper.MethodTranslateHelper.createJstMethod(
 						astObjectliteralField.initializer,
 						metaArr, m_ctx, simpleName), m_ctx);
 					
				if(meth==null){
					return null;
				}
				if (metaArr != null && metaArr.size() > 1 &&  meth.getArgs()!=null && meth.getArgs().size() == 0){
 					IJsCommentMeta meta = getLongestArgList(metaArr);
 					List<JsParam> params = TranslateHelper.getParams(meta);
 					if (params != null) {
 						for (JsParam param : params) {
 							final IJstType metaDefinedType = TranslateHelper.findType(m_ctx,
 									meta.getTyping(), meta);
 							JstArg arg = new JstArg(metaDefinedType, param.getName(), param.isVariable(),
 									param.isOptional());
 							meth.addArg(arg);
 						}
 					}
 				}					
 				
				
 				
 				meth.setBlock(null); // no real block
 				if(m_ctx.getCurrentScope().equals(ScopeIds.GLOBAL)){
 					
 					JstGlobalFunc globalFunc = new JstGlobalFunc(meth);
 					JstGlobalVar global = new JstGlobalVar(globalFunc);
 					global.setScopeForGlobal(m_ctx.getScopeForGlobals());
 					current.addGlobalVar(global);
 					result = globalFunc;
 				}
 				else{
 					if(isTypedObjLiteral){
 						final JstProperty mtdProperty4Otype = buildProperty(astObjectliteralField,
 								value, metaArr, parent);
 						mtdProperty4Otype.setType(new JstFuncType(meth));
 						current.addProperty(mtdProperty4Otype);
 					}else if(meth.isConstructor()){
 						
 						current.setConstructor(meth);
 					}
 					else{
 						current.addMethod(meth);
 					}
 					result = meth;
 				}
 			} else if(ScopeIds.GLOBAL.equals(m_ctx.getCurrentScope())){
 				JstGlobalVar global = buildGlobalNonFunc(astObjectliteralField,
 						value, metaArr, parent);
 				current.addGlobalVar(global);
 				result = global;
 			} else {
 				JstProperty jstProp = buildProperty(astObjectliteralField,
 						value, metaArr, parent);
 				if (isEnumContext()) {
 					current.addEnumValue(jstProp);
 				} else {
 					current.addProperty(jstProp);
 				}
 				result = jstProp;
 				
 			}
 		}
 		return result;
 	}
 	
 	private IJsCommentMeta getLongestArgList(List<IJsCommentMeta> metaArr) {
 		
 		IJsCommentMeta maxMeta = null;
 		int maxParamCount = 0;
 		List<JsParam> params = null;
 		for (IJsCommentMeta meta : metaArr) {
 			if (maxMeta == null) {
 				maxMeta = meta;
 				params = TranslateHelper.getParams(meta);
 				if (params != null) {
 					maxParamCount = params.size();
 				}
 			}
 			else {
 				params = TranslateHelper.getParams(meta);
 				if (params != null && params.size() > maxParamCount) {
 					maxParamCount = params.size();
 					maxMeta = meta;
 				}
 			}
 		}
 		return maxMeta;
 	}
 
 	private IExpr createEnumValue(Expression valuesExpr) {
 		if (valuesExpr == null) {
 			return null;
 		}
 		if (valuesExpr instanceof ArrayInitializer) {
 			ArrayInitializer values = (ArrayInitializer)valuesExpr;
 			if(values.expressions==null){
 				return new JstArrayInitializer();
 			}
 			JstType type = m_ctx.getCurrentType();
 			IExpr[] args = new IExpr[values.expressions.length];
 			
 			//Create new scope while processing values object literal
 //			m_ctx.enterBlock(ScopeIds.PROPERTY);
 			for (int i = 0; i < values.expressions.length; i++) {
 				Expression expr = values.expressions[i];
 				args[i] = (IExpr) getTranslatorAndTranslate(expr);
 			}
 //			m_ctx.exitBlock();
 
 			final JstIdentifier thisIdentifier = new JstIdentifier("this");
 			final JstIdentifier vj$Identifier = new JstIdentifier("vj$");
 			final FieldAccessExpr thisVj$ = new FieldAccessExpr(vj$Identifier, thisIdentifier);
 			final JstIdentifier simpleNameIdentifier = new JstIdentifier(type.getSimpleName());
 			final FieldAccessExpr constructor = new FieldAccessExpr(simpleNameIdentifier, thisVj$);
 			MtdInvocationExpr mtdInv = new MtdInvocationExpr(constructor, args);
 			ObjCreationExpr val = new ObjCreationExpr(mtdInv);
 			return val;
 		} 
 			// TODO -- validation fix model should not validate
 		System.err.println("Invalid enum values: " + valuesExpr.toString());
 		return new TextExpr(valuesExpr.toString());
 			
 	
 //		return null;
 	}
 
 	private void addFuncRef(JstType type,
 			ObjectLiteralField astObjectliteralField,
 			List<IJsCommentMeta> metaArr) {
 		final JstMethod mtd = TranslateHelper.MethodTranslateHelper.createJstMethod(astObjectliteralField.initializer,
 				metaArr, m_ctx, getSimpleName(astObjectliteralField));
 		final JstSource mtdNameSource = TranslateHelper.getSource(astObjectliteralField.fieldName, m_ctx.getSourceUtil());
 		if(mtd.getName() != null){
 			mtd.getName().setSource(mtdNameSource);
 		}
 
 		type.addMethod(mtd);
 		if(mtd != null){
 			JstFunctionRefType ref = new JstFunctionRefType(mtd);
 			if (m_parent instanceof IJstType) {
 				ref.setPackage(new JstPackage(((IJstType) m_parent).getName()));
 			}
 			type.addOType(ref);
 		}
 	}
 
 	private void checkIfTypeExistsForFunction(JstFunctionRefType ref) {
 		JstType replaceType = JstCache.getInstance().getType(ref.getName());
 		if(replaceType!=null){
 //			System.out.println("adding type to replace in post visit = " + replaceType.getName());
 			m_ctx.addTypeReplacement(replaceType, ref);
 		}
 	}
 
 //	private JstFunctionRef createJstFunctionRefType(JstType parent, JstMethod meth) {
 //		String typeName = ((IJstType) m_parent).getName() + "."+ meth.getName().getName();
 //		
 //		// find type in cache
 //		JstType jstType = JstCache.getInstance().getType(typeName);
 //		
 //		if(jstType==null){
 //			jstType = JstFactory.getInstance().createJstType(typeName, true);
 //		}
 //		
 //		jstType.setOuterType(parent);
 //		jstType.setParent(parent);
 //		
 //		JstFunctionRef ref = new JstFunctionRef(jstType);
 //		ref.setMethodRef(meth);
 //		return ref;
 //	}
 
 	private IJstNode checkForProblemAndProcess(
 			ObjectLiteralField astObjectliteralField) {
 		IExpression initializer = astObjectliteralField.initializer;
 		CategorizedProblem problem = findProblemInNode(initializer);
 		IJstNode res = null;
 		if (problem != null) {
 			BaseAst2JstTranslator translator = getProblemTranslator(
 					initializer, problem.getSourceStart());
 			Object value = translator.translate(initializer);
 			m_recoveredNodes.addAll(translator.getRecoveredNodes());
 			List<IJsCommentMeta> metaArr = getCommentMeta(astObjectliteralField);
 			if (value instanceof JstMethod) {
 				res = buildMethod(astObjectliteralField.getFieldName(),
 						(JstMethod) value, true);
 			} else {
 				JstProperty jstProp = buildProperty(astObjectliteralField,
 						(IExpr) value, metaArr);
 				((JstType) m_parent).addProperty(jstProp);
 				res = jstProp;
 			}
 
 		}
 
 		return res;
 	}
 
 	private NV buildNV(ObjectLiteralField astObjectLiteralField, IExpr value,
 			List<IJsCommentMeta> metaArr) {
 		if (value instanceof JstIdentifier) {
 			JstIdentifier tmp = (JstIdentifier) value;
 			if (tmp.getType() == null) {
 				tmp.setType(getNativeObject());
 			}
 		} else if (value instanceof FieldAccessExpr) {
 			FieldAccessExpr tmp = (FieldAccessExpr) value;
 			if (tmp.getType() == null) {
 				tmp.setType(getNativeObject());
 			}
 		}
 		JstIdentifier id = createId(astObjectLiteralField);
 		NV nv = new NV(id, value);
 
 		return nv;
 	}
 
 	private CategorizedProblem findProblemInNode(IASTNode astNode) {
 		CategorizedProblem[] problems = m_ctx.getAST().compilationResult()
 				.getAllProblems();
 		if (problems != null) {
 			// int completionPos = m_ctx.getCompletionPos();
 			for (CategorizedProblem problem : problems) {
 				if (problem.getSourceStart() <= astNode.sourceEnd()
 						&& problem.getSourceEnd() >= astNode.sourceStart()) {
 					return problem;
 				}
 			}
 		}
 		return null;
 	}
 
 	private boolean onlyWhiteSpaces(int startIdx, int endIdx) {
 		char[] source = m_ctx.getOriginalSource();
 		if (startIdx + 1 >= endIdx) {
 			return false;
 		}
 		for (int i = startIdx + 1; i < endIdx; i++) {
 			if (!Character.isWhitespace(source[i])) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private JstProperty buildProperty(ObjectLiteralField astObjectLiteralField,
 			IExpr value, List<IJsCommentMeta> metaArr) {
 		return buildProperty(astObjectLiteralField, value, metaArr, null);
 	}
 
 	
 	private JstGlobalVar buildGlobalNonFunc(ObjectLiteralField astObjectLiteralField,
 			IExpr value, List<IJsCommentMeta> metaArr, JstType override) {
 		JstProperty global =null;
 		IJsCommentMeta meta = null;
 		if (metaArr != null && metaArr.size() > 0) {
 			meta = metaArr.get(0);
 		}
 		String type = "Object";
 		JsTypingMeta typing = null;
 		if (meta != null && meta.getTyping() != null) {
 			typing = meta.getTyping();
 		}
 		IJstType propType = null;
 
 		if (isEnumContext()) {
 			propType = m_ctx.getCurrentType();
 		} else if (override != null) {
 			propType = override;
 		} else if (typing != null) {
 			propType = TranslateHelper.findType(m_ctx, typing, meta);
 		} else {
 			propType = TranslateHelper.findType(m_ctx, type);
 		}
 
 		if (propType == null) {
 			int begin = 0;
 			int end = 0;
 			if (meta != null) {
 				begin = meta.getBeginOffset();
 				end = ((typing != null) && typing.getTypingToken() != null) ? typing
 						.getTypingToken().endOffset
 						: 0;
 			}
 			m_ctx.getErrorReporter().error(
 					"Can not find type: " + type + " in type: "
 							+ m_ctx.getCurrentType().getName(),
 					m_ctx.getCurrentType().getName(), begin, end);
 		}
 
 		global = new JstProperty(propType, astObjectLiteralField.fieldName
 				.toString());
 		if(meta!=null){
 			JsDocHelper.addJsDoc(meta, global);
 		}
 		JstCommentLocation comment =  m_ctx.getCommentCollector().getCommentLocationNonMeta2(astObjectLiteralField.sourceStart);
 //		JsDocHelper.addJsDoc(comment, global);
 		global.addCommentLocation(comment);
 		
 		value = TranslateHelper
 				.getCastable(value, metaArr, m_ctx);
 		if (value instanceof ISimpleTerm) {
 			global.setValue((ISimpleTerm) value);
 		} else if (value instanceof IExpr) {
 			global.setInitializer(value);
 		} else {
 			JstSource source = ((IJstNode) value).getSource();
 			value = new SimpleLiteral(Object.class,
 					getNativeObject(), value.toExprText());
 			((SimpleLiteral) value).setSource(source);
 			global.setValue((ISimpleTerm) value);
 		}
 		global.addChild(value);
 
 		global.getName().setSource(
 				TranslateHelper.getSource(astObjectLiteralField.fieldName,
 						m_ctx.getSourceUtil()));
 
 		int sourceEnd = value instanceof IJstNode
 				&& ((IJstNode) value).getSource() != null ? ((IJstNode) value)
 				.getSource().getEndOffSet() : astObjectLiteralField.sourceEnd;
 
 				global.setSource(createSource(astObjectLiteralField.sourceStart,
 				sourceEnd, m_ctx.getSourceUtil()));
 
 		if (meta != null) {
 			TranslateHelper.setTypeRefSource((JstTypeReference) global
 					.getTypeRef(), meta);
 		}
 
 		// wrap property in JstGlobalVar object
 		JstGlobalVar gvar = createGlobalVar(value, global, meta);
 		
 		gvar.setParent(m_parent);
 		return gvar;
 		
 	}
 
 	private JstGlobalVar createGlobalVar(IExpr value, JstProperty global, IJsCommentMeta meta) {
 		JstGlobalProp jstGlobalProp = new JstGlobalProp(global);
 		JstGlobalVar gvar = new JstGlobalVar(jstGlobalProp);
 		gvar.setScopeForGlobal(m_ctx.getScopeForGlobals());		
 		return gvar;
 	}
 	
 	private JstProperty buildProperty(ObjectLiteralField astObjectLiteralField,
 			IExpr value, List<IJsCommentMeta> metaArr, JstType override) {
 		JstProperty property;
 
 		IJsCommentMeta meta = null;
 		if (metaArr != null && metaArr.size() > 0) {
 			meta = metaArr.get(0);
 		}
 		
 		
 		boolean isTypeInferred = true;
 		String type = "Object";
 		JsTypingMeta jsTyping = null;
 		if (meta != null) {
 			jsTyping = meta.getTyping();
 			if (jsTyping != null) {
 				isTypeInferred = false;
 			}
 		}
 		IJstType propType = null;
 
 		if (isEnumContext()) {
 			propType = m_ctx.getCurrentType();
 			isTypeInferred = false;
 		} else if (override != null) {
 			propType = override;
 			isTypeInferred = false;
 		} else if (jsTyping != null) {
 			propType = TranslateHelper.findType(m_ctx, jsTyping, meta);
 		} else {
 			propType = TranslateHelper.findType(m_ctx, type);
 		}
 
 		if (propType == null) {
 			int begin = 0;
 			int end = 0;
 			if (meta != null) {
 				begin = meta.getBeginOffset();
 				end = ((jsTyping != null) && jsTyping.getTypingToken() != null) ? 
 					jsTyping.getTypingToken().endOffset : 0;
 			}
 			m_ctx.getErrorReporter().error(
 					"Can not find type: " + type + " in type: "
 							+ m_ctx.getCurrentType().getName(),
 					m_ctx.getCurrentType().getName(), begin, end);
 		}
 		
 		if (isTypeInferred) {
 			propType = new JstInferredType(propType);
 		}
 
 		property = new JstProperty(propType, astObjectLiteralField.fieldName
 				.toString());
 	
 		property.setParent(m_parent);
 		if (m_ctx.getCurrentType().isInterface()) {
 			property.getModifiers().setFinal();
 		}
 
 		// TODO move this to defs translator
 		if (m_parent instanceof JstObjectLiteralType) {
 			property.getModifiers().setPublic();
 			if (jsTyping != null) {
 				JstObjectLiteralType olType = (JstObjectLiteralType)m_parent;
 				if (jsTyping.isOptional()) {
 					olType.addOptionalField(property);
 				}
 			}
 		}
 		value = TranslateHelper
 				.getCastable(value, metaArr, m_ctx);
 		
 		
 		if (value instanceof ISimpleTerm) {
 			property.setValue((ISimpleTerm) value);
 		} else if (value instanceof IExpr) {
 			property.setInitializer(value);
 		} else {
 			JstSource source = ((IJstNode) value).getSource();
 			value = new SimpleLiteral(Object.class,
 					getNativeObject(), value.toExprText());
 			((SimpleLiteral) value).setSource(source);
 			property.setValue((ISimpleTerm) value);
 		}
 		property.addChild(value);
 
 		property.getName().setSource(
 				TranslateHelper.getSource(astObjectLiteralField.fieldName,
 						m_ctx.getSourceUtil()));
 
 		int sourceEnd = value instanceof IJstNode
 				&& ((IJstNode) value).getSource() != null ? ((IJstNode) value)
 				.getSource().getEndOffSet() : astObjectLiteralField.sourceEnd;
 
 		property.setSource(createSource(astObjectLiteralField.sourceStart,
 				sourceEnd, m_ctx.getSourceUtil()));
 
 		if (isEnumContext()) {
 			property.getModifiers().setStatic(true);
 			property.getModifiers().setPublic();
 			property.getModifiers().setFinal();
 		}
 
 		if (meta != null) {
 			TranslateHelper.setModifiersFromMeta(meta, property.getModifiers());
 			TranslateHelper.setTypeRefSource((JstTypeReference) property
 					.getTypeRef(), meta);
 			property.addCommentLocation(meta.getBeginOffset(),meta.getEndOffset(),true);
 //			JsDocHelper.addJsDoc(meta, property);
 			
 		}
 		
 		JstCommentLocation location = m_ctx.getCommentCollector().getCommentLocationNonMeta2(astObjectLiteralField.sourceStart());
 		if(location!=null){
 			property.addCommentLocation(location);
 		}
 //		String comments = getComments(astObjectLiteralField, m_ctx);
 //		if(comments!=null){
 //			JsDocHelper.addJsDoc(comments, property);
 //		}
 
 		if (m_ctx.getCurrentScope() == ScopeIds.PROPS) {
 			property.getModifiers().setStatic(true);
 		} else if (m_ctx.getCurrentScope() == ScopeIds.PROTOS) {
 			property.getModifiers().setStatic(false);
 		}
 		
 		// If type was inferred, check if we can refine the type based on
 		// the assigned value or the initialized used.
 		if (isTypeInferred) {
 			if (property.getInitializer() != null) {
 				IExpr initializer = property.getInitializer();
 				if (initializer.getResultType() != null) {
 					property.setType(new JstInferredType(initializer.getResultType()));
 				}
 			} else if (property.getValue() != null &&
 					property.getValue() instanceof JstLiteral) {
 				JstLiteral val = (JstLiteral) property.getValue();
 				if (val.getResultType() != null) {
 					property.setType(new JstInferredType(val.getResultType()));
 				}
 			}
 		}
 
 		return property;
 	}
 
 	private boolean isEnumContext() {
 		return m_ctx.getCurrentScope() == ScopeIds.VALUES;
 	}
 
 	private List<IJsCommentMeta> getCommentMeta(
 			ObjectLiteralField astObjectLiteralField) {
 		return m_ctx.getCommentCollector().getCommentMeta(
 				astObjectLiteralField.sourceStart,
 				m_ctx.getPreviousNodeSourceEnd(),
 				m_ctx.getNextNodeSourceStart());
 	}
 	private static String getComments(
 			final IASTNode ast, 
 			final TranslateCtx ctx) {
 		return ctx.getCommentCollector().getCommentNonMeta2(ast.sourceStart());
 	}
 
 	@Override
 	protected JstCompletion createCompletion(
 			ObjectLiteralField astObjectLiteralField, boolean isAfterSource) {
 
 //		if (!(astObjectLiteralField.initializer instanceof FunctionExpression)) {
 //			int completionPos = m_ctx.getCompletionPos();
 //			if (isAfterSource
 //					|| astObjectLiteralField.initializer.sourceStart <= completionPos
 //					&& completionPos <= astObjectLiteralField.initializer.sourceEnd + 1) {
 //				// completion after ':'
 //				String initializerStr = astObjectLiteralField.initializer
 //						.toString();
 //				String prefix = new String(m_ctx.getOriginalSource(),
 //						astObjectLiteralField.sourceStart, completionPos
 //								- astObjectLiteralField.sourceStart);
 //				String wholeWord;
 //
 //				if (MISSING.equals(initializerStr)) {
 //					wholeWord = prefix;
 //				} else if (JsCoreKeywords.FUNCTION.startsWith(initializerStr)) {
 //					wholeWord = new String(m_ctx.getOriginalSource(),
 //							astObjectLiteralField.sourceStart,
 //							astObjectLiteralField.sourceEnd + 1
 //									- astObjectLiteralField.sourceStart);
 //				} else {
 //					// no completion
 //					return null;
 //				}
 //
 //				JstCompletion completion = new JstFieldOrMethodCompletion(m_ctx
 //						.getCurrentType(),
 //						m_ctx.getCurrentScope() == ScopeIds.PROPS);
 //				completion.setSource(createSource(
 //						astObjectLiteralField.sourceStart,
 //						astObjectLiteralField.sourceEnd + 1, m_ctx
 //								.getSourceUtil()));
 //				completion.setCompositeToken(wholeWord);
 //				completion.setToken(prefix);
 //				m_ctx.setCompletionPos(-1);
 //				return completion;
 //			}
 //		} else {
 
 			int completionPos = m_ctx.getCompletionPos();
 			String preStr = new String(m_ctx.getOriginalSource(),
 					astObjectLiteralField.sourceStart, completionPos
 							- astObjectLiteralField.sourceStart);
 			if (preStr == null) {
 				return null;
 			}
 			String[] strs = (" " + preStr + " ").split(":");
 			if (strs.length == 0) {
 				// TODO
 				return null;
 			} else if (strs.length == 1) {
 				String token = strs[0].trim();
 				JstCompletion completion = new JstFieldOrMethodCompletion(m_ctx
 						.getCurrentType(),
 						m_ctx.getCurrentScope() == ScopeIds.PROPS);
 				IExpression expr = astObjectLiteralField.getFieldName();
 				JstSource jstSource = null;
 				if (expr != null) {
 					if (expr.sourceEnd() + 1 > completionPos) {
 						jstSource = createSource(
 								expr.sourceStart(),
 								expr.sourceEnd() + 1, m_ctx
 										.getSourceUtil());
 					} else {
 						jstSource = createSource(
 								expr.sourceStart(),
 								completionPos, m_ctx
 										.getSourceUtil());
 					}
 				}
 				completion.setSource(jstSource);
 				completion.setCompositeToken(preStr);
 				completion.setToken(token);
 				m_ctx.setCreatedCompletion(true);
 				completion.setScopeStack(m_ctx.getScopeStack());
 				return completion;
 			} else if (strs.length == 2) {
 				String token = strs[1].trim();
 				//if cursor is After "(", null will be return
 				if(token.indexOf("(") >= 0 || !isJavaIdentifier(token)) {
 					return null;
 				}
 				JstCompletion completion = new JstCompletionOnSingleNameReference(
 						m_ctx.getCurrentType());
 				completion.setToken(token);
 				m_ctx.setCreatedCompletion(true);
 				completion.setScopeStack(m_ctx.getScopeStack());
 				return completion;
 			}
 
 //		}
 		return null;
 	}
 	
 	private IJstType getNativeObject() {
 		return JstCache.getInstance().getType("Object");
 	}
 	
 	private JstIdentifier createId(ObjectLiteralField astObjectliteralField) {
 		JstIdentifier id = new JstIdentifier(astObjectliteralField.getFieldName().toString());
 		int startOffset = astObjectliteralField.sourceStart;
 		int endOffset = astObjectliteralField.sourceEnd;
 		int length = endOffset - startOffset + 1;
 		id.setSource(TranslateHelper.createJstSource(m_ctx.getSourceUtil(), length,startOffset,endOffset));
 		return id;
 	}
 }
