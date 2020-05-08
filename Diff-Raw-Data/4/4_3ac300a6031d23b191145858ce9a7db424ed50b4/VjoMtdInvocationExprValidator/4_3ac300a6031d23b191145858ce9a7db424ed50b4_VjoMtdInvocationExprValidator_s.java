 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.validator;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.vjet.dsf.jsgen.shared.util.JstDisplayUtils;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoSemanticValidator;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoValidationCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.VjoValidationRuntimeException;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.VjoSemanticRuleRepo;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.rulectx.BaseVjoSemanticRuleCtx;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.util.AccessControlUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.rules.util.TypeCheckUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.symbol.EVjoSymbolType;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.symbol.IVjoSymbol;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.symbol.VjoSymbol;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.semantic.symbol.VjoSymbolTable;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.util.MixinValidationUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.util.VjoValidationVisitorCtxUpdateUtil;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationPostAllChildrenListener;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationPostChildListener;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.vjo.visitor.IVjoValidationVisitorEvent;
 import org.eclipse.vjet.dsf.jst.IInferred;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstConstructor;
 import org.eclipse.vjet.dsf.jst.declaration.JstFuncType;
 import org.eclipse.vjet.dsf.jst.declaration.JstFunctionRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstInferredType;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstMixedType;
 import org.eclipse.vjet.dsf.jst.declaration.JstObjectLiteralType;
 import org.eclipse.vjet.dsf.jst.declaration.JstParamType;
 import org.eclipse.vjet.dsf.jst.declaration.JstProxyType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeRefType;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeWithArgs;
 import org.eclipse.vjet.dsf.jst.declaration.JstVariantType;
 import org.eclipse.vjet.dsf.jst.declaration.JstVars;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.FuncExpr;
 import org.eclipse.vjet.dsf.jst.expr.JstArrayInitializer;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.expr.ObjCreationExpr;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.term.JstProxyIdentifier;
 import org.eclipse.vjet.dsf.jst.term.NV;
 import org.eclipse.vjet.dsf.jst.term.ObjLiteral;
 import org.eclipse.vjet.dsf.jst.term.SimpleLiteral;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.util.JstTypeHelper;
 import org.eclipse.vjet.vjo.meta.VjoKeywords;
 
 public class VjoMtdInvocationExprValidator 
 	extends VjoSemanticValidator 
 	implements IVjoValidationPostChildListener,
 		IVjoValidationPostAllChildrenListener {
 	
 	private static final String VJO_ETYPE1 = "vjo.etype1";
     private static final String VALUES = "values";
     private static List<Class<? extends IJstNode>> s_targetTypes;
 	
 	static{
 		s_targetTypes = new ArrayList<Class<? extends IJstNode>>();
 		s_targetTypes.add(MtdInvocationExpr.class);
 	}
 	
 	public List<Class<? extends IJstNode>> getTargetNodeTypes(){
 		return s_targetTypes;
 	}
 
 	@Override
 	public void onPostChildEvent(final IVjoValidationVisitorEvent event){
 		final VjoValidationCtx ctx = event.getValidationCtx();
 		final IJstNode jstNode = event.getVisitNode();
 		final IJstNode child = event.getVisitChildNode();
 		
 		if(!(jstNode instanceof MtdInvocationExpr)){
 			return;
 		}
 		else if(jstNode.getParentNode() != null && jstNode.getParentNode() instanceof ObjCreationExpr){
 			return;//obj creation expr will be validated separately
 		}
 		
 		final MtdInvocationExpr expr = (MtdInvocationExpr)jstNode;
 		if(child != expr.getQualifyExpr()){
 			return;
 		}
 		
 		final IExpr qualifier = expr.getQualifyExpr();
 		
 		IJstType qualifierType = qualifier.getResultType();
 		if (qualifierType == null) {
 			return; //skip validation
 		}
 		
 		validateVjoMake(ctx, expr, getTargetType(qualifierType));
 		validateVjoMixin(ctx, expr, getTargetType(qualifierType));
 	}
 	
 	@Override
 	public void onPostAllChildrenEvent(final IVjoValidationVisitorEvent event){
 		final VjoValidationCtx ctx = event.getValidationCtx();
 		final IJstNode jstNode = event.getVisitNode();
 		boolean isInferred = false;
 		if(!(jstNode instanceof MtdInvocationExpr)){
 			return;
 		}
 		
 		final MtdInvocationExpr expr = (MtdInvocationExpr)jstNode;
 		final IExpr qualifier = expr.getQualifyExpr();
 		IExpr identifier = expr.getMethodIdentifier();
 		final List<IExpr> args = expr.getArgs();
 		VjoSemanticRuleRepo ruleRepo = VjoSemanticRuleRepo.getInstance();
 		
 		if(jstNode.getParentNode() != null 
 				&& jstNode.getParentNode() instanceof ObjCreationExpr){
 			if (identifier instanceof FieldAccessExpr) {
 				identifier = ((FieldAccessExpr) identifier).getName();
 			}
 			if(identifier != null && identifier instanceof JstIdentifier){
 				JstIdentifier mtdIdentifier = (JstIdentifier)identifier;
 				final IJstNode mtdBinding = mtdIdentifier.getJstBinding();
 				if (mtdBinding instanceof JstConstructor) {
 					JstConstructor cons = (JstConstructor) mtdBinding;
 					// add error arguments
 					final String[] arguments = new String[2];
 					arguments[0] = cons.getName().getName();
 					arguments[1] = expr.toExprText();
 					validateBoundMethod(ctx, expr, identifier, args, cons,
 							ruleRepo, arguments);
 				}
 			}
 			return;//obj creation expr will be validated separately
 		}
 		
 		if(qualifier != null){
 			IJstType qualifierType = qualifier.getResultType();				
 			if(qualifierType == null){
 				//couldn't do further validations
 				return;
 			}	
 			if(qualifierType instanceof JstInferredType){
 				qualifierType = ((JstInferredType)qualifierType).getType();
 				isInferred = true;
 			}
 				
 			if(identifier != null && identifier instanceof JstIdentifier){
 				JstIdentifier mtdIdentifier = (JstIdentifier)identifier;
 				final IJstNode mtdBinding = mtdIdentifier.getJstBinding();
 				final String mtdName = mtdIdentifier.getName();
 				
 				
 				// add error arguments
 				final String[] arguments = new String[2];
 				arguments[0] = mtdName != null ? mtdName : "NULL";
 				arguments[1] = expr.toExprText();
 				
 				if (mtdBinding != null 
 						&& !(mtdBinding instanceof IJstMethod) 
 						&& !validateMtdBinding(mtdBinding)) {
 					satisfyRule(ctx, 
 							ruleRepo.FUNCTION_SHOULD_BE_DEFINED,
 							new BaseVjoSemanticRuleCtx
 								(expr, ctx.getGroupId(), arguments));
 					return;
 					
 				}
 				
 				if (mtdBinding == null) {
 
 					
 					if (!"Object".equals(qualifierType.getName()) 
 							&& !"ERROR_UNDEFINED_TYPE".equals(qualifierType.getName())
 							&& !qualifierType.getModifiers().isDynamic()) {
 						
 						// qualifier type is class type
 						if (qualifierType instanceof IJstRefType) {
 							//NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE
 							if (qualifierType.getMethod(mtdName, false, true) != null) {					
 								satisfyRule(ctx, 
 									ruleRepo.NONE_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_STATIC_SCOPE,
 									new BaseVjoSemanticRuleCtx
 										(expr, ctx.getGroupId(), arguments));
 								return;
 							}
 			
 						}
 						else  {
 							//STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE
 							if(qualifierType instanceof IInferred){
 								if (qualifierType.getMethod(mtdName, true, true) != null) {					
 									satisfyRule(ctx, 
 										ruleRepo.INFERRED_STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE,
 										new BaseVjoSemanticRuleCtx
 											(expr, ctx.getGroupId(), arguments));
 									return;
 								}
 							}else{
 								if (qualifierType.getMethod(mtdName, true, true) != null) {					
 									satisfyRule(ctx, 
 										ruleRepo.STATIC_METHOD_SHOULD_NOT_BE_ACCESSED_FROM_NONE_STATIC_SCOPE,
 										new BaseVjoSemanticRuleCtx
 											(expr, ctx.getGroupId(), arguments));
 									return;
 								}
 							}
 						}
 						
 						if ((qualifierType instanceof JstObjectLiteralType &&
 							"ObjLiteral".equals(qualifierType.getSimpleName()))){
 							return; //OK ???
 						}
 						
 						// TODO support mixed and variant type method / property checking 
 						// make sure to make dynamic types not give error
 						// if isInferred && give error on inferred type ) give error 
 						if(!ctx.getMissingImportTypes().contains(qualifierType) && !(qualifierType instanceof JstVariantType)
 								 && !(qualifierType instanceof JstMixedType)){
 							//METHOD_SHOULD_BE_DEFINED
 							if(isInferred){
 							satisfyRule(ctx, ruleRepo.INFERRED_TYPE_METHOD_SHOULD_BE_DEFINED,
 								new BaseVjoSemanticRuleCtx
 									(identifier, ctx.getGroupId(), arguments));
 							}else{
 								satisfyRule(ctx, ruleRepo.METHOD_SHOULD_BE_DEFINED,
 										new BaseVjoSemanticRuleCtx
 											(identifier, ctx.getGroupId(), arguments));
 							}
 						}
 					}
 					return;
 				}
 				
 				validateBoundMethod(ctx, expr, identifier, args, mtdBinding,
 						ruleRepo, arguments);
 				
 				//added by huzhou@ebay.com to verify vjo.getType's argument
 				//the type declared should be in the dependencies or global type space.
 				validateVjoGetType(ctx, expr, qualifierType);
 			}
 		}
 		else if(identifier != null && identifier instanceof JstIdentifier){
 			JstIdentifier mtdIdentifier = (JstIdentifier)identifier;
 			final IJstNode mtdBinding = mtdIdentifier.getJstBinding();
 			final String mtdName = mtdIdentifier.getName();
 			
 			// add error arguments
 			final String[] arguments = new String[2];
 			arguments[0] = mtdName != null ? mtdName : "NULL";
 			arguments[1] = expr.toExprText();
 			
 			if (mtdBinding != null && !(mtdBinding instanceof IJstMethod) && !validateMtdBinding(mtdBinding)) {
 				satisfyRule(ctx, 
 						ruleRepo.FUNCTION_SHOULD_BE_DEFINED,
 						new BaseVjoSemanticRuleCtx
 							(expr, ctx.getGroupId(), arguments));
 				return;
 				
 			}
 			//temp fix by huzhou as proxy identifier's use cases are causing more undefined function now
 			//we reports error only when 
 			if (mtdBinding == null) {
 				if(!(mtdIdentifier instanceof JstProxyIdentifier)){
 					//METHOD_SHOULD_BE_DEFINED
 					satisfyRule(ctx, ruleRepo.FUNCTION_SHOULD_BE_DEFINED,
 						new BaseVjoSemanticRuleCtx
 							(identifier, ctx.getGroupId(), arguments));
 				}
 			}
 			else{
 				checkArgsOfMethod(ctx, mtdBinding, args, expr);
 			}
 		}
 	}
 	
 	private static boolean validateMtdBinding(IJstNode mtdBinding) {
 		IJstType resultType = null;
 		
 		if (mtdBinding instanceof IJstProperty) {
 			resultType = ((IJstProperty)mtdBinding).getType();
 		}
 		else if (mtdBinding instanceof JstVars) {
 			resultType = ((JstVars)mtdBinding).getType();
 		}
 		else if (mtdBinding instanceof JstArg) {
 			resultType = ((JstArg)mtdBinding).getType();
 		}
 		
 		if (resultType == null) {
 			return true;
 		}
 		if (resultType instanceof IInferred) {
 			return true; //skip inferred type
 		}
 		if (resultType instanceof JstProxyType) {
 			resultType = ((JstProxyType)resultType).getType();
 		}
 		
 		//Added by Eric.Ma 20100525 for jira 263 OType.function can't be recognized.
 		if (resultType instanceof JstFunctionRefType) {
 			return true;
 		}
 		//End of added, bugfix by huzhou as JstExtendedType could be from Function/Object
 		if (resultType != null &&
 				("Function".equals(resultType.getName()) || "Undefined".equals(resultType.getName()))){
 			return true;
 		}
 		
 		return false;		
 	}
 
 	private void validateBoundMethod(final VjoValidationCtx ctx,
 			final MtdInvocationExpr expr, final IExpr identifier,
 			final List<IExpr> args, final IJstNode mtdBinding,
 			VjoSemanticRuleRepo ruleRepo, final String[] arguments) {
 		if (mtdBinding instanceof JstMethod && !(expr.getParentNode() instanceof ObjCreationExpr)) {
 			final JstMethod method = (JstMethod)mtdBinding;
 			final IJstType callerType = expr.getOwnerType();
 			final IJstType mtdOwnerType = method.getOwnerType();
 			final boolean visible = AccessControlUtil.isVisible(method, mtdOwnerType, callerType);
 			if (!visible) {
 				satisfyRule(ctx, ruleRepo.METHOD_SHOULD_BE_VISIBLE,
 					new BaseVjoSemanticRuleCtx
 						(identifier, ctx.getGroupId(), arguments));
 				return;
 			}
 		}
         // Added by Eric.Ma on 20100611 for etype vlues A:"DD" issue
         if (mtdBinding instanceof JstMethod && args.size() > 0) {
             IExpr valuesExprArgs = args.get(0);
             JstMethod method = (JstMethod)mtdBinding;
             checkETypeValuesArgs(ctx, valuesExprArgs, method);
         }
         // End of added
 
 		checkArgsOfMethod(ctx, mtdBinding, args, expr);
 		//bugfix by roy, reference count increment
 		if(mtdBinding instanceof JstMethod){
 			ctx.getMethodInvocationTable().reference(getDispatcher(mtdBinding));
 		}
 	}
 
 	private IJstMethod getDispatcher(final IJstNode mtdBinding) {
 		if(mtdBinding instanceof IJstMethod){
 			final IJstMethod mtd = (IJstMethod)mtdBinding;
 			if(mtd.isDispatcher()){
 				return mtd;
 			}
 			final IJstNode parent = mtd.getParentNode();
 			if(parent instanceof IJstMethod){//dispatcher lookup
 				final IJstMethod parentMtd = (IJstMethod)parent;
 				if(parentMtd.isDispatcher() && parentMtd.getOverloaded().contains(mtd)){
 					return parentMtd;
 				}
 			}
 			return mtd;
 		}
 		return null;
 	}
 
 //	private static boolean isMethodAnnotated(IJstMethod mtd){
 //		final JstModifiers staticModifiers = new JstModifiers(JstModifiers.STATIC);
 //		if(mtd == null){
 //			return false;
 //		}
 //		else if(mtd instanceof ISynthesized){
 //			return true;
 //		}
 //		else if((mtd.getModifiers().isNone() || staticModifiers.toString().equals(mtd.getModifiers().toString()))
 //				&& mtd.getRtnType() == null){
 //			return false;
 //		}
 //		else{
 //			return true;
 //		}
 //	}
 	
 	protected static class MatchMethodArgsResult{
 		private boolean m_numberMatched;
 		private boolean m_typeMatched;
 		private List<IJstType> m_expectedMatches;
 		
 		public List<IJstType> getExpectedMatches() {
 			if(m_expectedMatches==null){
 				m_expectedMatches = new ArrayList<IJstType>();
 			}
 			return m_expectedMatches;
 		}
 
 		public void setExpectedMatches(List<IJstType> expectedMatches) {
 			m_expectedMatches = expectedMatches;
 		}
 
 		public void addExpectedMatch(IJstType type){
 			getExpectedMatches().add(type);
 		}
 		
 		private BaseVjoSemanticRuleCtx m_ruleCtx;
 		
 		public boolean isNumberMatched(){
 			return m_numberMatched;
 		}
 		
 		public void setNumberMatched(boolean matched){
 			m_numberMatched = matched;
 		}
 		
 		public boolean isTypeMatched(){
 			return m_typeMatched;
 		}
 		
 		public void setTypeMatched(boolean matched){
 			m_typeMatched = matched;
 		}
 		
 		public BaseVjoSemanticRuleCtx getParamTypeMismatchRuleCtx(){
 			return m_ruleCtx;
 		}
 		
 		public void setParamTypeMismatchRuleCtx(BaseVjoSemanticRuleCtx ruleCtx){
 			m_ruleCtx = ruleCtx;
 		}
 	}
 	
 	private IJstType getParameterType(final VjoValidationCtx ctx,
 			final IJstMethod method,
 			final JstArg param, 
 			final IJstType qualifierType){
 		IJstType argType = param.getType();
 		if (qualifierType != null){
 			if(argType instanceof JstParamType){
 				//check lower bound or upper bound
 				final JstParamType paramType = (JstParamType)argType;
 				//bugfix, paramType could be from JstMethod
 				if(method.getParamTypes().contains(paramType)){
 					final IJstType resolvedType = ctx.getResolvedTypeByParamType(paramType);
 					if(resolvedType != null){
 						argType = resolvedType;
 					}
 					else{
 						argType = paramType;
 					}
 				}
 			}
 			if(qualifierType instanceof JstTypeWithArgs){
 				if (argType instanceof JstParamType) {
 					argType = (((JstTypeWithArgs)qualifierType).getParamArgType(((JstParamType)argType)));
 				}
 			}
 			else if(qualifierType instanceof JstParamType){
 				final JstParamType qualifierParamType = (JstParamType)qualifierType;
 				if(qualifierParamType.getBounds().size() > 0){
 					final IJstType realQualifierType = qualifierParamType.getBounds().get(0);
 					if(realQualifierType instanceof JstTypeWithArgs){
 						if (argType instanceof JstParamType) {
 							argType = (((JstTypeWithArgs)realQualifierType).getParamArgType(((JstParamType)argType)));
 						}
 					}
 				}
 			}
 			else{
 				argType = JstTypeHelper.resolveJstArgType(param, qualifierType);
 			}
 		}
 		
 		return argType;
 	}
 	
 //	/**
 //	 * <p>
 //	 * 	validate arguments against method parameter definition for one particular method (overloading method excluded)
 //	 * </p>
 //	 * @param ctx
 //	 * @param dupProtectSet
 //	 * @param method
 //	 * @param arguments
 //	 * @param mtdInvocationExpr
 //	 * @return
 //	 */
 //	protected void matchMethodArgs(final VjoValidationCtx ctx,
 //			final IJstMethod method, 
 //			final List<IExpr> arguments, 
 //			final List<JstArg> parameters,
 //			final IExpr mtdInvocationExpr,
 //			final List<MatchMethodArgsResult> matchResults,
 //			final boolean prevTypeMatched,
 //			final BaseVjoSemanticRuleCtx prevTypeMismatchRuleCtx){
 //		
 //		final MatchMethodArgsResult result = new MatchMethodArgsResult();
 //		result.setTypeMatched(prevTypeMatched);
 //		if(!prevTypeMatched && prevTypeMismatchRuleCtx != null){
 //			result.setParamTypeMismatchRuleCtx(prevTypeMismatchRuleCtx);
 //		}
 //		result.setNumberMatched(true);
 //		
 //		IJstType qualifierType = null;
 //		if (mtdInvocationExpr instanceof MtdInvocationExpr) {
 //			final IExpr qualiferExpr = ((MtdInvocationExpr)mtdInvocationExpr).getQualifyExpr();
 //			qualifierType = qualiferExpr != null ? qualiferExpr.getResultType() : null;
 //			
 //			//handle constructor calls where
 //			final IExpr idExpr = ((MtdInvocationExpr) mtdInvocationExpr).getMethodIdentifier();
 //			if(qualiferExpr == null && idExpr.getResultType() instanceof JstTypeRefType){
 //				qualifierType = mtdInvocationExpr.getResultType();
 //			}
 //		}
 //		
 //
 //		final List<JstArg> mtdParams = new ArrayList<JstArg>();
 //		mtdParams.addAll(parameters);
 //		final List<IExpr> actualArgs = new ArrayList<IExpr>();
 //		actualArgs.addAll(arguments);
 //		
 //		/**
 //		 * loop parameters //removed by huzhou@ebay.com as each param will have one and only one type now
 //		 * 		loop arguments
 //		 * 			if param is variable lengthed
 //		 * 				then
 //		 * 					match as many arguments as possible
 //		 * 					add back the 1st argument to argument list
 //		 * 				else
 //		 * 					match next argument if possible
 //		 * 			end
 //		 * 		end
 //		 * end
 //		 * 
 //		 * compare parameter list and argument list to see if all parameters/arguments have been consumed
 //		 * length other than zero indicates a number mismatch
 //		 */
 //		
 //		if(mtdParams.size() > 0){
 //			final JstArg frontParam = mtdParams.remove(0);
 //			final IJstType frontParamType = getParameterType(ctx, method, frontParam, qualifierType);
 //			if(frontParam.isVariable()){//eat as much arguments as it can
 //				while(actualArgs.size() > 0){
 //					final IExpr frontArg = actualArgs.remove(0);
 //					final List<IJstType> frontArgTypes = new ArrayList<IJstType>();
 //					final IJstNode frontArgBinding = JstBindingUtil.getJstBinding(frontArg);
 //					if(frontArgBinding != null && frontArgBinding instanceof JstArg){
 //						frontArgTypes.addAll(((JstArg)frontArgBinding).getTypes());
 //					}
 //					else if(frontArg.getResultType() != null){
 //						frontArgTypes.add(frontArg.getResultType());
 //					}
 //					
 //					if(frontArgTypes.size() <= 0){
 //						//no arg value held, take it as assignable
 //						continue;//continue the inner while loop
 //					}
 //					
 //					boolean typeMatched = false;
 //					for(IJstType frontArgType: frontArgTypes){
 //						if(frontParamType instanceof JstParamType){
 //							//bind front param type with frontArg's type
 //							final JstParamType paramType = (JstParamType)frontParamType;
 //							ctx.setResolvedTypeForParamType(paramType, frontArgType);
 //							final IVjoValidationPostAllChildrenListener cleanUpListener = new IVjoValidationPostAllChildrenListener(){
 //								public void onPostAllChildrenEvent(
 //										IVjoValidationVisitorEvent event)
 //										throws VjoValidationRuntimeException {
 //									ctx.resetResolvedTypeByParamType(paramType);
 //								}
 //	
 //								public List<Class<? extends IJstNode>> getTargetNodeTypes() {
 //									return s_targetTypes;
 //								}
 //	
 //								public void onEvent(IVjoValidationVisitorEvent event)
 //										throws VjoValidationRuntimeException {
 //									onPostAllChildrenEvent(event);
 //								}
 //							};
 //							
 //							VjoSemanticValidatorRepo.getInstance().appendPostAllChildrenListener(mtdInvocationExpr, cleanUpListener);
 //							
 //							// argument is JstParamType, qualifier type is unresolved generic type
 //							// actual argument has to match exactly to the generic paramater type
 //							typeMatched = paramType == frontArgType || paramType.getType() == frontArgType;
 //						}
 //						else if(TypeCheckUtil.isAssignable(frontParamType, frontArgType)){
 //							typeMatched = true;
 //						}
 //					}
 //					
 //					if(!typeMatched){
 //						final String[] ruleCtxArgs = {
 //								frontArg.toExprText(),
 //								frontParam.getName(),
 //								mtdInvocationExpr.toExprText()
 //						};
 //						BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(frontArg, ctx.getGroupId(), ruleCtxArgs);
 //						result.setParamTypeMismatchRuleCtx(ruleCtx);
 //						result.setTypeMatched(typeMatched);
 //						break;
 //					}
 //				}
 //				
 //				result.setNumberMatched(actualArgs.size() == 0);
 //				matchResults.add(result);
 //				return;
 //			}
 //			else{//eat 1 and 1 only
 //				final IExpr frontArg = actualArgs.size() > 0 ? actualArgs.remove(0) : null;
 //				if(frontArg == null){//number mismatch
 //					result.setNumberMatched(false);
 //					matchResults.add(result);
 //					return;
 //				}
 //				else{
 //					final List<IJstType> frontArgTypes = new ArrayList<IJstType>();
 //					final IJstNode frontArgBinding = JstBindingUtil.getJstBinding(frontArg);
 //					if(frontArgBinding != null && frontArgBinding instanceof JstArg){
 //						frontArgTypes.addAll(((JstArg)frontArgBinding).getTypes());
 //					}
 //					else if(frontArg.getResultType() != null){
 //						frontArgTypes.add(frontArg.getResultType());
 //					}
 //					
 //					
 //					if(frontArgTypes.size() > 0){
 //						//no arg value held, take it as assignable
 //						boolean typeMatched = false;
 //						for(IJstType frontArgType: frontArgTypes){
 //							if(frontParamType instanceof JstParamType){
 //							    //Added by Eric.Ma on 20100622 for handle invoke static method with generic parameter 
 //                                if (method.isStatic()) {
 //                                    typeMatched = true;
 //                                    continue;
 //                                }
 //                                //End of added.
 //
 //								//bind front param type with frontArg's type
 //								final JstParamType paramType = (JstParamType)frontParamType;
 //								ctx.setResolvedTypeForParamType(paramType, frontArgType);
 //								final IVjoValidationPostAllChildrenListener cleanUpListener = new IVjoValidationPostAllChildrenListener(){
 //									public void onPostAllChildrenEvent(
 //											IVjoValidationVisitorEvent event)
 //											throws VjoValidationRuntimeException {
 //										ctx.resetResolvedTypeByParamType(paramType);
 //									}
 //		
 //									public List<Class<? extends IJstNode>> getTargetNodeTypes() {
 //										return s_targetTypes;
 //									}
 //		
 //									public void onEvent(IVjoValidationVisitorEvent event)
 //											throws VjoValidationRuntimeException {
 //										onPostAllChildrenEvent(event);
 //									}
 //									
 //								};
 //								
 //								VjoSemanticValidatorRepo.getInstance().appendPostAllChildrenListener(mtdInvocationExpr, cleanUpListener);
 //								//good state
 //								
 //								// argument is JstParamType, qualifier type is unresolved generic type
 //								//added by Eric.Ma 20100401 Handle two args both as generic type situation
 //                                if (paramType == frontArgType || paramType.getType() == frontArgType ){
 //                                    typeMatched = true;
 //                                }
 //                                else if(TypeCheckUtil.isAssignable(frontParamType, frontArgType)){
 //                                	typeMatched = TypeCheckUtil.isSuperType(frontArgType,paramType.getType());
 //                                }
 //                                else if(frontArgType instanceof JstType){//Handle ctype<E extends XX> situation
 //                                	typeMatched = paramType.getSimpleName().equals(frontArgType.getSimpleName());
 //                                }
 //                                //end of added
 //							}
 //							else if(TypeCheckUtil.isAssignable(frontParamType, frontArgType)){
 //								//good state
 //								typeMatched = true;
 //							}
 //						}
 //						if(!typeMatched){
 //							//hold the problem
 //							final String[] ruleCtxArgs = {
 //									frontArg.toExprText(),
 //									frontParam.getName(),
 //									mtdInvocationExpr.toExprText()
 //							};
 //							BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(frontArg, ctx.getGroupId(), ruleCtxArgs);
 //							matchMethodArgs(ctx, method, actualArgs, mtdParams, mtdInvocationExpr, matchResults, false, ruleCtx);
 //							return;
 //						}
 //					}
 //				}
 //				
 //				matchMethodArgs(ctx, method, actualArgs, mtdParams, mtdInvocationExpr, matchResults, prevTypeMatched, prevTypeMismatchRuleCtx);
 //			}
 //		}
 //		else{
 //			result.setNumberMatched(actualArgs.size() == 0);
 //			matchResults.add(result);
 //		}
 //	}
 //	
 	protected boolean checkArgsOfMethod(final VjoValidationCtx ctx,
 			final IJstNode node, 
 			List<IExpr> actualArgs, 
 			final MtdInvocationExpr expr){
 		if(node instanceof IJstMethod){
 			if(((IJstMethod) node).isTypeFactoryEnabled()){
 				//by huzhou@ebay.com, this ^ must have been bounded
 				return true;
 			}
 			return checkArgsOfMethod(ctx, (IJstMethod)node, actualArgs, expr);
 		}
 		//Added by Eric.Ma 20100525 for Otype validation rules
 		if(node instanceof JstArg){
 			IJstTypeReference tr = ((JstArg) node).getTypeRef();
 			IJstType t = null;
 			if(tr != null){
 				t = tr.getReferencedType();
 				if(t != null  && t instanceof JstFunctionRefType){
 					return checkArgsOfMethod(ctx, ((JstFunctionRefType)t).getMethodRef(), actualArgs, expr);
 				}
 			}
 		}
 		//End added.
 		return false;
 	}
 	
 	/**
 	 * 
 	 * @param ctx
 	 * @param dupProtect [overloading method dup visit protection]
 	 * @param method
 	 * @param actualArgs
 	 * @param expr
 	 * @return
 	 */
 	protected boolean checkArgsOfMethod(final VjoValidationCtx ctx,
 			final IJstMethod method, 
 			List<IExpr> arguments, 
 			final MtdInvocationExpr expr){
         // avoid NPE
 		if (arguments == null){
 			arguments = new ArrayList<IExpr>(0);
 		}
 
 		final List<IJstMethod> candidates = new LinkedList<IJstMethod>();
 		final IJstMethod dispatcher = getDispatcher(method);
 		if(dispatcher.isDispatcher()){
 			candidates.addAll(dispatcher.getOverloaded());
 		}
 		else{
 			candidates.add(dispatcher);
 		}
 		
 		final int argumentsLength = arguments.size();
 		final Map<IJstMethod, List<JstArg>> overloads2ParamsMap = new HashMap<IJstMethod, List<JstArg>>(candidates.size());
 
 		//init the overloads, filling in only whose parameter size matched arguments' length
 		//or the next param is a variable lengthed one
 		initOverloadsWithCorrectParamSize(candidates, argumentsLength, overloads2ParamsMap);
 		
 		//if there's no entries in the map, it means there's no argument matching parameter's number
 		if(overloads2ParamsMap.size() == 0){
 			final String[] messages = {
 					String.valueOf(expr.getArgs() == null ? 0 : expr.getArgs().size()),
 					determineMaxArgs(dispatcher),
 					expr.toExprText()
 			};
 			
 			final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(expr.getMethodIdentifier(), ctx.getGroupId(), messages);
 			satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().METHOD_WRONG_NUMBER_OF_ARGS, ruleCtx);
 			return false;
 		}
 		
 		IJstType qualifierType = null;
 		if (expr instanceof MtdInvocationExpr) {
 			final IExpr qualiferExpr = ((MtdInvocationExpr)expr).getQualifyExpr();
 			qualifierType = qualiferExpr != null ? qualiferExpr.getResultType() : null;
 			
 			//handle constructor calls where
 			final IExpr idExpr = ((MtdInvocationExpr) expr).getMethodIdentifier();
 			if(qualiferExpr == null && idExpr.getResultType() instanceof JstTypeRefType){
 				qualifierType = expr.getResultType();
 			}
 		}
 		
 		for(int argIt = 0; argIt < argumentsLength; argIt++){
 			final IExpr argAtIt = arguments.get(argIt);
 			final Map<IJstMethod, IJstType> badEntries = new LinkedHashMap<IJstMethod, IJstType>();
 			final IJstType argumentType = argAtIt.getResultType();
 			if(argumentType == null){//cannot compare
 				continue;
 			}
 			else{
 				for(Map.Entry<IJstMethod, List<JstArg>> overloadEntry : overloads2ParamsMap.entrySet()){
 					final IJstMethod overloadMethod = overloadEntry.getKey();
 					final List<JstArg> overloadParams = overloadEntry.getValue();
 					final JstArg parameter = overloadParams.get(argIt);
 					final IJstType parameterType = getParameterType(ctx, dispatcher, parameter, qualifierType);
 					if(!TypeCheckUtil.isAssignable(parameterType, argumentType)){
 						badEntries.put(overloadMethod, parameterType);
 					}
 				}
 				
 				if(overloads2ParamsMap.size() <= badEntries.size()){
 					//report error immediately
 					final String[] ruleCtxArgs = {
 							String.valueOf(argIt+1),
 							getSemanticTypeName(argumentType),
 							buildParameterTypes(badEntries.values())
 					};
 					BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(argAtIt, ctx.getGroupId(), ruleCtxArgs);
 					satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().METHOD_ARGS_TYPE_SHOULD_MATCH, ruleCtx);
 					return false;
 				}
 				else{
 					for(Map.Entry<IJstMethod, IJstType> badKey : badEntries.entrySet()){
 						overloads2ParamsMap.remove(badKey.getKey());
 					}
 				}
 			}
 		}
 		
 		return true;
 	}
 	
 	private String buildParameterTypes(final Collection<IJstType> parameterTypes){
 		final StringBuilder sb = new StringBuilder();
 		if(parameterTypes.size() > 1){sb.append('{');}
 		for(Iterator<IJstType> it = parameterTypes.iterator(); it.hasNext();){
 			final IJstType parameterType = it.next();
 			sb.append(getSemanticTypeName(parameterType));
 			if(it.hasNext()){
 				sb.append(',');
 			}
 		}
 		if(parameterTypes.size() > 1){sb.append('}');}
 		return sb.toString();
 	}
 
 	private String getSemanticTypeName(final IJstType parameterType) {
 		if(parameterType == null){
 			return "NULL";
 		}
 		
 		if(parameterType instanceof JstFuncType){
 			final IJstMethod function = ((JstFuncType)parameterType).getFunction();
 			if(!function.isDispatcher()){
 				return JstDisplayUtils.getFullMethodString(function, function.getOwnerType(), false);
 			}
 			else{
 				return "expected function signature";
 			}
 		}
 		else{
 			return parameterType.getName();
 		}
 	}
 
 	public static void initOverloadsWithCorrectParamSize(
 			final List<IJstMethod> overloads, final int argumentsLength,
 			final Map<IJstMethod, List<JstArg>> overloads2ParamsMap) {
 		for(IJstMethod overload : overloads){
 			//bugfix by huzhou@ebay.com, 
 			//when parameter lengths exceeds argument length
 			//or the param at argument length isn't variable lengthed
 			//the overload will be excluded
 			if(preCheckOverloadParamSize(argumentsLength, overload)){
 				overloads2ParamsMap.put(overload, paddingParams(overload.getArgs(), argumentsLength));
 			}
 		}
 	}
 
 	private static boolean preCheckOverloadParamSize(final int argumentsLength,
 			IJstMethod overload) {
 		final List<JstArg> parameters = overload.getArgs();
 		return parameters.size() == argumentsLength //exact length matched
 				|| (parameters.size() < argumentsLength //args exceeds params, but last param is variable
 						&& parameters.size() > 0
 						&& parameters.get(parameters.size() - 1).isVariable())
 				|| (parameters.size() == argumentsLength + 1 //args one less than params, but last param is variable
 					&& parameters.get(parameters.size() - 1).isVariable());
 	}
 	
 	private static List<JstArg> paddingParams(final List<JstArg> params, final int argumentsLength) {
 		final int paramsLength = params.size();
 		if(paramsLength >= argumentsLength){
 			return params;
 		}
 		
 		final JstArg lastParam = paramsLength > 0 ? params.get(paramsLength - 1): null;
 		//it's an arraylist as we know the exact length (arguments length)
 		//and there will be subsequent indexing based accessing to the list
 		final List<JstArg> paddingParams = new ArrayList<JstArg>(argumentsLength);
 		paddingParams.addAll(params);
 		
 		final JstArg paddingParam = lastParam != null && lastParam.isVariable() ? new JstArg(lastParam.getType(), "proxy", true) : new JstArg(JstCache.getInstance().getType("Object"), "proxy", false);
 		for(int i = paramsLength; i < argumentsLength; i++){
 			paddingParams.add(paddingParam);
 		}
 		return paddingParams;
 	}
 
 	private String determineMaxArgs(IJstMethod method) {
 		int count = method.getArgs().size();
 		List<? extends IJstMethod> mtds = JstTypeHelper.getSignatureMethods(method);
 		for(IJstMethod m: mtds){
 			int size = m.getArgs().size();
 			if(size>count){
 				count = size;
 			}
 		}
 		
 		return String.valueOf(count);
 	}
 
 	
 	private void validateVjoMake(final VjoValidationCtx ctx, 
 			final MtdInvocationExpr expr,
 			final IJstType qualifierType){
 		final IExpr identifier = expr.getMethodIdentifier();
 		
 		if(identifier != null && identifier instanceof JstIdentifier){
 			final String mtdId = ((JstIdentifier)identifier).getName();
 			
 			final IJstType ownerType = ctx.getScope().getClosestTypeScopeNode();
 			final IJstNode ownerScope = ctx.getScope().getClosestScopeNode();
 			if(isVjoType(qualifierType)
 					&& "make".equals(mtdId)){
 				
 				final IJstType anonymousType = new JstObjectLiteralType("$anonymous_vjo_make$");
 				ctx.setMakeParentType(anonymousType, ownerType);
 				if(ownerScope instanceof JstMethod){
 					anonymousType.getModifiers().setStatic(((JstMethod)ownerScope).getModifiers().isStatic());
 				}
 				
 				final List<IExpr> vjoMakeArgs = expr.getArgs();
 				if(vjoMakeArgs.size() < 2){
 					//report problem and return error;
 					return;
 				}
 				
 				final IExpr scopeArg = vjoMakeArgs.get(0);
 				final IExpr sourceArg = vjoMakeArgs.get(1);
 				final List<IExpr> sourceTypeConstructorArgs = new ArrayList<IExpr>(vjoMakeArgs.size() - 2);
 				for(int i = 2; i < vjoMakeArgs.size(); i ++){
 					sourceTypeConstructorArgs.add(vjoMakeArgs.get(i));
 				}
 				
 				final MtdInvocationExpr implicitSourceTypeConstructorInvocation = new MtdInvocationExpr("constructs");
 				implicitSourceTypeConstructorInvocation.setSource(identifier.getSource());
 				//implicitly uses the jst source of the vjo.make
 				
 				final IVjoValidationPostAllChildrenListener postVjoMakeCtxArgListener = new IVjoValidationPostAllChildrenListener(){
 					public void onPostAllChildrenEvent(
 							IVjoValidationVisitorEvent event)
 							throws VjoValidationRuntimeException {
 						final IJstNode visitNode = event.getVisitNode();
 						if(visitNode instanceof IExpr){
 							final IJstType exprValue = ((IExpr)visitNode).getResultType();
 							if(exprValue != null){
 								ctx.setMakeScopeType(anonymousType, exprValue);
 							}
 						}
 					}
 
 					public List<Class<? extends IJstNode>> getTargetNodeTypes() {
 						return Collections.emptyList();
 					}
 
 					public void onEvent(IVjoValidationVisitorEvent event)
 							throws VjoValidationRuntimeException {
 						onPostAllChildrenEvent(event);
 					}
 				};
 				VjoSemanticValidatorRepo.getInstance().appendPostAllChildrenListener(scopeArg, postVjoMakeCtxArgListener);
 				
 				final IVjoValidationPostAllChildrenListener postVjoMakeSourceArgListener = new IVjoValidationPostAllChildrenListener(){
 					public void onPostAllChildrenEvent(
 							IVjoValidationVisitorEvent event)
 							throws VjoValidationRuntimeException {
 						final IJstNode visitNode = event.getVisitNode();
 						if(visitNode instanceof IExpr){
 							final IJstType exprValue = ((IExpr)visitNode).getResultType();
 							if(exprValue != null){
 								ctx.setMakeSourceType(anonymousType, exprValue);
 							}
 						}
 					}
 
 					public List<Class<? extends IJstNode>> getTargetNodeTypes() {
 						return Collections.emptyList();
 					}
 
 					public void onEvent(IVjoValidationVisitorEvent event)
 							throws VjoValidationRuntimeException {
 						onPostAllChildrenEvent(event);
 					}
 					
 				};
 				VjoSemanticValidatorRepo.getInstance().appendPostAllChildrenListener(sourceArg, postVjoMakeSourceArgListener);
 								
 				final IVjoValidationPostAllChildrenListener postVjoMakeListener = new IVjoValidationPostAllChildrenListener(){
 					public void onPostAllChildrenEvent(
 							IVjoValidationVisitorEvent event)
 							throws VjoValidationRuntimeException {
 						//semantically entered the embedded type
 						VjoValidationVisitorCtxUpdateUtil.updateCtxBeforeType(ctx, anonymousType);
 						
 						IJstNode exprParent = expr.getParentNode();
 						if(exprParent != null && exprParent instanceof MtdInvocationExpr){
 							//deal with protos/endType, no props, no inits
 							final VjoSymbolTable symbolTable = ctx.getSymbolTable();
 							do{
 								final MtdInvocationExpr mtdExprParent = (MtdInvocationExpr)exprParent;
 								if(mtdExprParent.getMethodIdentifier() instanceof JstIdentifier){
 									final JstIdentifier mtdIdExprParent = (JstIdentifier)mtdExprParent.getMethodIdentifier();
 									
 									if("protos".equals(mtdIdExprParent.getName())){
 										final List<IExpr> args = mtdExprParent.getArgs();
 										if(args.size() > 0){
 											final IExpr argExpr = args.get(0);
 											if(argExpr instanceof ObjLiteral){
 												final ObjLiteral argObjLiteral = (ObjLiteral)argExpr;
 												ctx.setMakeTypeForObjLiteral(argObjLiteral, anonymousType);
 												
 												for(NV nv : argObjLiteral.getNVs()){
 													final String name = nv.getName();
 													final IExpr value = nv.getValue();
 													
 													IVjoSymbol propertySymbol = new VjoSymbol();
 													propertySymbol.setName(name);
 													if(value instanceof FuncExpr){
 														propertySymbol.setSymbolType(EVjoSymbolType.INSTANCE_FUNCTION);
 														propertySymbol.setDeclareType(new JstFunctionRefType(((FuncExpr)value).getFunc()));
 													}
 													else{
 														propertySymbol.setSymbolType(EVjoSymbolType.INSTANCE_VARIABLE);
 													}
 													propertySymbol.setDeclareNode(value);
 													propertySymbol.setStaticReference(false);
 													symbolTable.addSymbolInScope(anonymousType, propertySymbol);
 												}
 											}
 										}
 									}
 									else if("endType".equals(mtdIdExprParent.getName())){
 										final IVjoValidationPostAllChildrenListener postEndTypeListner = new IVjoValidationPostAllChildrenListener(){
 											public void onPostAllChildrenEvent(
 													IVjoValidationVisitorEvent event)
 													throws VjoValidationRuntimeException {
 												
 												VjoValidationVisitorCtxUpdateUtil.updateCtxAfterType(ctx, anonymousType);
 											}
 
 											public List<Class<? extends IJstNode>> getTargetNodeTypes() {
 												return s_targetTypes;
 											}
 
 											public void onEvent(
 													IVjoValidationVisitorEvent event)
 													throws VjoValidationRuntimeException {
 												onPostAllChildrenEvent(event);
 											}
 										};
 										
 										VjoSemanticValidatorRepo.getInstance().appendPostAllChildrenListener(mtdExprParent, postEndTypeListner);
 										break;
 									}
 									else{
 										break;
 									}
 								}
 								
 								exprParent = exprParent.getParentNode();
 							}
 							while(exprParent != null 
 									&& exprParent instanceof MtdInvocationExpr
 									&& ((MtdInvocationExpr)exprParent).getMethodIdentifier() instanceof JstIdentifier);
 						}
 
 						//add validation logic for constructor invocation
 						if(ctx.getMakeSourceType(anonymousType) != null && 
 								ctx.getMakeSourceType(anonymousType).getConstructor() != null){
 							checkArgsOfMethod(ctx, ctx.getMakeSourceType(anonymousType).getConstructor(), sourceTypeConstructorArgs, implicitSourceTypeConstructorInvocation);
 						}
 						
 					}
 
 					public List<Class<? extends IJstNode>> getTargetNodeTypes() {
 						return s_targetTypes;
 					}
 
 					public void onEvent(IVjoValidationVisitorEvent event)
 							throws VjoValidationRuntimeException {
 						onPostAllChildrenEvent(event);
 					}
 				};
 				VjoSemanticValidatorRepo.getInstance().appendPostAllChildrenListener(expr, postVjoMakeListener);
 			}
 		}
 	}
 	
 	private void validateVjoMixin(final VjoValidationCtx ctx, 
 			final MtdInvocationExpr expr,
 			final IJstType qualifierType){
 		final IExpr identifier = expr.getMethodIdentifier();
 		
 		if(identifier != null && identifier instanceof JstIdentifier){
 			final String mtdId = ((JstIdentifier)identifier).getName();
 			
 			//support vjo.mixin
 			if(isVjoType(qualifierType)
 					&& "mixin".equals(mtdId)){
 				//do post declaration mixin, all mtype's properties/methods will be replicated as target type's symbol
 				//target type must be ctype
 				//mixin type must be mtype
 				//mtype's expects must be satisfied
 				final List<IExpr> args = expr.getArgs();
 				if(args.size() == 2){
 					final IExpr targetArg = args.get(0);
 					final IExpr mixinArg = args.get(1);
 					//arguments are string literals
 					if(targetArg instanceof SimpleLiteral
 							&& mixinArg instanceof SimpleLiteral){
 						final String targetValue = ((SimpleLiteral)targetArg).getValue();
 						final String mixinValue = ((SimpleLiteral)mixinArg).getValue();
 						if(targetValue != null && mixinValue != null){
 							validateVjoMixin(ctx, ctx.getCacheType(targetValue), ctx.getCacheType(mixinValue), false);
 						}
 					}
 					
 					//arguments are IJstTypes
 					else if(mixinArg instanceof FieldAccessExpr){
 						final IVjoValidationPostAllChildrenListener postAllListener = new IVjoValidationPostAllChildrenListener(){
 							public void onPostAllChildrenEvent(final IVjoValidationVisitorEvent event){
 								final IJstType targetValue = targetArg.getResultType();
 								final IJstType mixinValue = mixinArg.getResultType();
 								if(targetValue != null
 										&& mixinValue != null 
 										&& mixinValue instanceof IJstRefType){
 									//replicate symbols in mtype to target type
 									validateVjoMixin(ctx, targetValue, mixinValue, !(targetValue instanceof IJstRefType));
 								}
 //								VjoSemanticValidatorRepo.getInstance().removePostAllChildrenListener(expr, this);
 							}
 
 							public List<Class<? extends IJstNode>> getTargetNodeTypes() {
 								return s_targetTypes;
 							}
 
 							public void onEvent(IVjoValidationVisitorEvent event)
 									throws VjoValidationRuntimeException {
 								//do nothing
 								onPostAllChildrenEvent(event);
 							}
 						};
 						
 						VjoSemanticValidatorRepo.getInstance().registerPostAllChildrenListener(expr, postAllListener);
 					}
 				}
 			}
 		}
 	}
 	
 	private void validateVjoMixin(final VjoValidationCtx ctx, final IJstType targetType, final IJstType mixinType, final boolean instanceMixin){
 		
 		//check expects and satisfies, @see VjoJstTypeValidator
 		if(!MixinValidationUtil.validateMixin(ctx, this, targetType, mixinType, instanceMixin)){
 			return;//mixin prerequisit not met!
 		}
 		
 		//doing actual replication
 		if(!instanceMixin){//type mixin
 //			JstSymbolReplicateUtil.replicate(ctx, mixinType, targetType);
 		}
 		else{
 			//the target instance if mixin
 			//which should affect the runtime only
 			//yet supported
 		}
 	}
 	
 	private void validateVjoGetType(final VjoValidationCtx ctx, 
 			final MtdInvocationExpr expr,
 			final IJstType qualifierType){
 		final String mtdId = ((JstIdentifier)expr.getMethodIdentifier()).getName();
 		
 		if(isVjoType(qualifierType)
 				&& "getType".equals(mtdId)){
 			
 			if(expr.getArgs().size() > 0){
 				final IExpr arg = expr.getArgs().get(0);
 				if(arg instanceof SimpleLiteral){
 					final SimpleLiteral sl = (SimpleLiteral)arg;
 					IJstType exprOwnerType = expr.getOwnerType();
 					if(exprOwnerType == null){
 						List<IJstType> scopes = ctx.getScope().getTypeScopeNodes();
 						for(Iterator<IJstType> it = scopes.iterator(); it.hasNext();){
 							final IJstType scopeType = it.next();
 							if(scopeType != null && !(scopeType instanceof JstObjectLiteralType)){
 								exprOwnerType = scopeType;
 								break;
 							}
 						}
 					}
 					validateTypeResolution(this, ctx, exprOwnerType, ctx.getDependencyVerifier(exprOwnerType), sl, sl.getValue());
 				}
 			}
 		}
 	}
 	
 	private boolean isVjoType(final IJstType type) {
 		final IJstType vjoType = JstCache.getInstance().getType(VjoKeywords.VJO);
 		if(vjoType == null){
 			return false;
 		}
 		if(type instanceof JstProxyType){
 			return vjoType.equals(((JstProxyType)type).getType());
 		}
 		
 		return vjoType.equals(type);
 	}
 	
 	// ML isEnum() method is bug now
     private void checkETypeValuesArgs(final VjoValidationCtx ctx, IExpr valuesExprArgs, JstMethod method) {
        if (method.getName().toString().equalsIgnoreCase(VALUES)) {
             if (method.getRtnType().getName().equalsIgnoreCase(VJO_ETYPE1)) {
                 if (valuesExprArgs instanceof ObjLiteral) {
                     List<NV> nVs = ((ObjLiteral) valuesExprArgs).getNVs();
                     if (nVs.size() > 0) {
                         for (NV nv : nVs) {
                             if (!(nv.getValue() instanceof JstArrayInitializer)) {
                                 final BaseVjoSemanticRuleCtx ruleCtx = new BaseVjoSemanticRuleCtx(nv, ctx
                                         .getGroupId(), new String[] {});
                                 satisfyRule(ctx, VjoSemanticRuleRepo.getInstance().ETYPE_VALUES_MUST_BE_ARRAY,
                                         ruleCtx);
                                 return;
                             }
                         }
                     }
                 }
             }
         }
     }
 	
 }
