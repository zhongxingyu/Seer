 /*
  * Copyright 2010-2012 JetBrains s.r.o.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.napile.compiler.lang.types.expressions;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.napile.asm.lib.NapileLangPackage;
 import org.napile.asm.resolve.name.Name;
import org.napile.compiler.CodeTodo;
 import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
 import org.napile.compiler.lang.descriptors.MethodDescriptor;
 import org.napile.compiler.lang.descriptors.VariableDescriptor;
 import org.napile.compiler.lang.diagnostics.Errors;
 import org.napile.compiler.lang.psi.*;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.resolve.BindingContextUtils;
 import org.napile.compiler.lang.resolve.BindingTrace;
 import org.napile.compiler.lang.resolve.BindingTraceContext;
 import org.napile.compiler.lang.resolve.DescriptorUtils;
 import org.napile.compiler.lang.resolve.calls.CallMaker;
 import org.napile.compiler.lang.resolve.calls.OverloadResolutionResults;
 import org.napile.compiler.lang.resolve.calls.autocasts.DataFlowInfo;
 import org.napile.compiler.lang.resolve.scopes.JetScope;
 import org.napile.compiler.lang.resolve.scopes.WritableScope;
 import org.napile.compiler.lang.resolve.scopes.WritableScopeImpl;
 import org.napile.compiler.lang.resolve.scopes.receivers.ExpressionReceiver;
 import org.napile.compiler.lang.types.CommonSupertypes;
 import org.napile.compiler.lang.types.ErrorUtils;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lang.types.JetTypeInfo;
 import org.napile.compiler.lang.types.TypeUtils;
 import org.napile.compiler.lang.types.checker.JetTypeChecker;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.util.PsiTreeUtil;
 
 /**
  * @author abreslav
  */
 public class ControlStructureTypingVisitor extends ExpressionTypingVisitor
 {
 
 	protected ControlStructureTypingVisitor(@NotNull ExpressionTypingInternals facade)
 	{
 		super(facade);
 	}
 
 	private void checkCondition(@NotNull JetScope scope, @Nullable NapileExpression condition, ExpressionTypingContext context)
 	{
 		if(condition != null)
 		{
 			JetType conditionType = facade.getTypeInfo(condition, context.replaceScope(scope)).getType();
 
 			if(conditionType != null && !ExpressionTypingUtils.isBoolean(conditionType))
 			{
 				context.trace.report(Errors.TYPE_MISMATCH_IN_CONDITION.on(condition, conditionType));
 			}
 		}
 	}
 
 	////////////////////////////////////////////////////////////////////////////////////////////////////
 
 
 	@Override
 	public JetTypeInfo visitIfExpression(NapileIfExpression expression, ExpressionTypingContext context)
 	{
 		return visitIfExpression(expression, context, false);
 	}
 
 	public JetTypeInfo visitIfExpression(NapileIfExpression expression, ExpressionTypingContext contextWithExpectedType, boolean isStatement)
 	{
 		ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE);
 		NapileExpression condition = expression.getCondition();
 		checkCondition(context.scope, condition, context);
 
 		NapileExpression elseBranch = expression.getElse();
 		NapileExpression thenBranch = expression.getThen();
 
 		WritableScopeImpl thenScope = ExpressionTypingUtils.newWritableScopeImpl(context, "Then scope");
 		WritableScopeImpl elseScope = ExpressionTypingUtils.newWritableScopeImpl(context, "Else scope");
 		DataFlowInfo thenInfo = DataFlowUtils.extractDataFlowInfoFromCondition(condition, true, context);
 		DataFlowInfo elseInfo = DataFlowUtils.extractDataFlowInfoFromCondition(condition, false, context);
 
 		if(elseBranch == null)
 		{
 			if(thenBranch != null)
 			{
 				JetTypeInfo typeInfo = context.expressionTypingServices.getBlockReturnedTypeWithWritableScope(thenScope, Collections.singletonList(thenBranch), CoercionStrategy.NO_COERCION, context.replaceDataFlowInfo(thenInfo), context.trace);
 				JetType type = typeInfo.getType();
 				DataFlowInfo dataFlowInfo;
 				dataFlowInfo = typeInfo.getDataFlowInfo().or(elseInfo);
 				return DataFlowUtils.checkImplicitCast(DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL), expression, contextWithExpectedType), expression, contextWithExpectedType, isStatement, dataFlowInfo);
 			}
 			return JetTypeInfo.create(null, context.dataFlowInfo);
 		}
 		if(thenBranch == null)
 		{
 			JetTypeInfo typeInfo = context.expressionTypingServices.getBlockReturnedTypeWithWritableScope(elseScope, Collections.singletonList(elseBranch), CoercionStrategy.NO_COERCION, context.replaceDataFlowInfo(elseInfo), context.trace);
 			JetType type = typeInfo.getType();
 			DataFlowInfo dataFlowInfo;
 			dataFlowInfo = typeInfo.getDataFlowInfo().or(thenInfo);
 			return DataFlowUtils.checkImplicitCast(DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL), expression, contextWithExpectedType), expression, contextWithExpectedType, isStatement, dataFlowInfo);
 		}
 		CoercionStrategy coercionStrategy = isStatement ? CoercionStrategy.COERCION_TO_UNIT : CoercionStrategy.NO_COERCION;
 		JetTypeInfo thenTypeInfo = context.expressionTypingServices.getBlockReturnedTypeWithWritableScope(thenScope, Collections.singletonList(thenBranch), coercionStrategy, contextWithExpectedType.replaceDataFlowInfo(thenInfo), context.trace);
 		JetTypeInfo elseTypeInfo = context.expressionTypingServices.getBlockReturnedTypeWithWritableScope(elseScope, Collections.singletonList(elseBranch), coercionStrategy, contextWithExpectedType.replaceDataFlowInfo(elseInfo), context.trace);
 		JetType thenType = thenTypeInfo.getType();
 		JetType elseType = elseTypeInfo.getType();
 		DataFlowInfo thenDataFlowInfo = thenTypeInfo.getDataFlowInfo();
 		DataFlowInfo elseDataFlowInfo = elseTypeInfo.getDataFlowInfo();
 
 		boolean jumpInThen = thenType != null && false;
 		boolean jumpInElse = elseType != null && false;
 
 		JetTypeInfo result;
 		if(thenType == null && elseType == null)
 		{
 			result = JetTypeInfo.create(null, thenDataFlowInfo.or(elseDataFlowInfo));
 		}
 		else if(thenType == null || (jumpInThen && !jumpInElse))
 		{
 			result = elseTypeInfo;
 		}
 		else if(elseType == null || (jumpInElse && !jumpInThen))
 		{
 			result = thenTypeInfo;
 		}
 		else
 		{
 			result = JetTypeInfo.create(CommonSupertypes.commonSupertype(Arrays.asList(thenType, elseType)), thenDataFlowInfo.or(elseDataFlowInfo));
 		}
 
 		return DataFlowUtils.checkImplicitCast(result.getType(), expression, contextWithExpectedType, isStatement, result.getDataFlowInfo());
 	}
 
 	@Override
 	public JetTypeInfo visitLabelExpression(NapileLabelExpression expression, ExpressionTypingContext context)
 	{
 		NapileExpression body = expression.getBody();
 		if(body != null)
 		{
 			WritableScopeImpl scopeToExtend = ExpressionTypingUtils.newWritableScopeImpl(context, "Scope extended in while's condition");
 
 			context.expressionTypingServices.getType(scopeToExtend, expression, TypeUtils.NO_EXPECTED_TYPE, context.dataFlowInfo, context.trace);
 		}
 
 		return JetTypeInfo.create(TypeUtils.NO_EXPECTED_TYPE, context.dataFlowInfo);
 	}
 
 	@Override
 	public JetTypeInfo visitWhileExpression(NapileWhileExpression expression, ExpressionTypingContext context)
 	{
 		return visitWhileExpression(expression, context, false);
 	}
 
 	public JetTypeInfo visitWhileExpression(NapileWhileExpression expression, ExpressionTypingContext contextWithExpectedType, boolean isStatement)
 	{
 		if(!isStatement)
 			return DataFlowUtils.illegalStatementType(expression, contextWithExpectedType, facade);
 
 		ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE);
 		NapileExpression condition = expression.getCondition();
 		checkCondition(context.scope, condition, context);
 		NapileExpression body = expression.getBody();
 		if(body != null)
 		{
 			WritableScopeImpl scopeToExtend = ExpressionTypingUtils.newWritableScopeImpl(context, "Scope extended in while's condition");
 			DataFlowInfo conditionInfo = condition == null ? context.dataFlowInfo : DataFlowUtils.extractDataFlowInfoFromCondition(condition, true, context);
 			context.expressionTypingServices.getBlockReturnedTypeWithWritableScope(scopeToExtend, Collections.singletonList(body), CoercionStrategy.NO_COERCION, context.replaceDataFlowInfo(conditionInfo), context.trace);
 		}
 		DataFlowInfo dataFlowInfo;
 		if(!containsBreak(expression, context))
 		{
 			dataFlowInfo = DataFlowUtils.extractDataFlowInfoFromCondition(condition, false, context);
 		}
 		else
 		{
 			dataFlowInfo = context.dataFlowInfo;
 		}
 		return DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL), expression, contextWithExpectedType, dataFlowInfo);
 	}
 
 	private boolean containsBreak(final NapileLoopExpression loopExpression, final ExpressionTypingContext context)
 	{
 		final boolean[] result = new boolean[1];
 		result[0] = false;
 		//todo breaks in inline function literals
 		loopExpression.accept(new NapileTreeVisitor<NapileLoopExpression>()
 		{
 			@Override
 			public Void visitBreakExpression(NapileBreakExpression breakExpression, NapileLoopExpression outerLoop)
 			{
 				NapileSimpleNameExpression targetLabel = breakExpression.getTargetLabel();
 				PsiElement element = targetLabel != null ? context.trace.get(BindingContext.LABEL_TARGET, targetLabel) : null;
 				if(element == loopExpression || (targetLabel == null && outerLoop == loopExpression))
 				{
 					result[0] = true;
 				}
 				return null;
 			}
 
 			@Override
 			public Void visitLoopExpression(NapileLoopExpression loopExpression, NapileLoopExpression outerLoop)
 			{
 				return super.visitLoopExpression(loopExpression, loopExpression);
 			}
 		}, loopExpression);
 
 		return result[0];
 	}
 
 	@Override
 	public JetTypeInfo visitDoWhileExpression(NapileDoWhileExpression expression, ExpressionTypingContext context)
 	{
 		return visitDoWhileExpression(expression, context, false);
 	}
 
 	public JetTypeInfo visitDoWhileExpression(NapileDoWhileExpression expression, ExpressionTypingContext contextWithExpectedType, boolean isStatement)
 	{
 		if(!isStatement)
 			return DataFlowUtils.illegalStatementType(expression, contextWithExpectedType, facade);
 
 		ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE);
 		NapileExpression body = expression.getBody();
 		JetScope conditionScope = context.scope;
 		if(body instanceof NapileFunctionLiteralExpression)
 		{
 			NapileFunctionLiteralExpression function = (NapileFunctionLiteralExpression) body;
 			if(!function.getFunctionLiteral().hasParameterSpecification())
 			{
 				WritableScope writableScope = ExpressionTypingUtils.newWritableScopeImpl(context, "do..while body scope");
 				conditionScope = writableScope;
 				context.expressionTypingServices.getBlockReturnedTypeWithWritableScope(writableScope, function.getFunctionLiteral().getBodyExpression().getStatements(), CoercionStrategy.NO_COERCION, context, context.trace);
 				context.trace.record(BindingContext.BLOCK, function);
 			}
 			else
 			{
 				facade.getTypeInfo(body, context.replaceScope(context.scope));
 			}
 		}
 		else if(body != null)
 		{
 			WritableScope writableScope = ExpressionTypingUtils.newWritableScopeImpl(context, "do..while body scope");
 			conditionScope = writableScope;
 			List<NapileElement> block;
 			if(body instanceof NapileBlockExpression)
 			{
 				block = ((NapileBlockExpression) body).getStatements();
 			}
 			else
 			{
 				block = Collections.<NapileElement>singletonList(body);
 			}
 			context.expressionTypingServices.getBlockReturnedTypeWithWritableScope(writableScope, block, CoercionStrategy.NO_COERCION, context, context.trace);
 		}
 		NapileExpression condition = expression.getCondition();
 		checkCondition(conditionScope, condition, context);
 		DataFlowInfo dataFlowInfo;
 		if(!containsBreak(expression, context))
 		{
 			dataFlowInfo = DataFlowUtils.extractDataFlowInfoFromCondition(condition, false, context);
 		}
 		else
 		{
 			dataFlowInfo = context.dataFlowInfo;
 		}
 		return DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL), expression, contextWithExpectedType, dataFlowInfo);
 	}
 
 	@Override
 	public JetTypeInfo visitForExpression(NapileForExpression expression, ExpressionTypingContext context)
 	{
 		return visitForExpression(expression, context, false);
 	}
 
 	public JetTypeInfo visitForExpression(NapileForExpression expression, ExpressionTypingContext contextWithExpectedType, boolean isStatement)
 	{
 		if(!isStatement)
 			return DataFlowUtils.illegalStatementType(expression, contextWithExpectedType, facade);
 
 		ExpressionTypingContext context = contextWithExpectedType.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE);
 		NapilePropertyParameter loopParameter = expression.getLoopParameter();
 		NapileExpression loopRange = expression.getLoopRange();
 		JetType expectedParameterType = null;
 		if(loopRange != null)
 		{
 			ExpressionReceiver loopRangeReceiver = ExpressionTypingUtils.getExpressionReceiver(facade, loopRange, context.replaceScope(context.scope));
 			if(loopRangeReceiver != null)
 				expectedParameterType = checkIterableConvention(loopRangeReceiver, context);
 		}
 
 		WritableScope loopScope = ExpressionTypingUtils.newWritableScopeImpl(context, "Scope with for-loop index");
 
 		if(loopParameter != null)
 		{
 			NapileTypeReference typeReference = loopParameter.getTypeReference();
 			VariableDescriptor variableDescriptor;
 			if(typeReference != null)
 			{
 				variableDescriptor = context.expressionTypingServices.getDescriptorResolver().resolveLocalVariableDescriptor(context.scope.getContainingDeclaration(), context.scope, loopParameter, context.trace);
 				JetType actualParameterType = variableDescriptor.getType();
 				if(expectedParameterType != null &&
 						actualParameterType != null &&
 						!JetTypeChecker.INSTANCE.isSubtypeOf(expectedParameterType, actualParameterType))
 				{
 					context.trace.report(Errors.TYPE_MISMATCH_IN_FOR_LOOP.on(typeReference, expectedParameterType, actualParameterType));
 				}
 			}
 			else
 			{
 				if(expectedParameterType == null)
 				{
 					expectedParameterType = ErrorUtils.createErrorType("Error");
 				}
 				variableDescriptor = context.expressionTypingServices.getDescriptorResolver().resolveLocalVariableDescriptor(context.scope.getContainingDeclaration(), loopParameter, expectedParameterType, context.trace);
 			}
 
 			{
 				// http://youtrack.jetbrains.net/issue/KT-527
 
 				VariableDescriptor olderVariable = context.scope.getLocalVariable(variableDescriptor.getName());
 				if(olderVariable != null && DescriptorUtils.isLocal(context.scope.getContainingDeclaration(), olderVariable))
 				{
 					PsiElement declaration = BindingContextUtils.descriptorToDeclaration(context.trace.getBindingContext(), variableDescriptor);
 					context.trace.report(Errors.NAME_SHADOWING.on(declaration, variableDescriptor.getName().getName()));
 				}
 			}
 
 			loopScope.addVariableDescriptor(variableDescriptor);
 		}
 
 		NapileExpression body = expression.getBody();
 		if(body != null)
 		{
 			context.expressionTypingServices.getBlockReturnedTypeWithWritableScope(loopScope, Collections.singletonList(body), CoercionStrategy.NO_COERCION, context, context.trace);
 		}
 
 		return DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL), expression, contextWithExpectedType, context.dataFlowInfo);
 	}
 
 	@Nullable
     /*package*/ static JetType checkIterableConvention(@NotNull ExpressionReceiver loopRange, ExpressionTypingContext context)
 	{
 		NapileExpression loopRangeExpression = loopRange.getExpression();
 
 		// Make a fake call loopRange.iterator(), and try to resolve it
 		Name iterator = Name.identifier("iterator");
 		OverloadResolutionResults<MethodDescriptor> iteratorResolutionResults = resolveFakeCall(loopRange, context, iterator);
 
 		// We allow the loop range to be null (nothing happens), so we make the receiver type non-null
 		if(!iteratorResolutionResults.isSuccess())
 		{
 			ExpressionReceiver nonNullReceiver = new ExpressionReceiver(loopRange.getExpression(), TypeUtils.makeNotNullable(loopRange.getType()));
 			OverloadResolutionResults<MethodDescriptor> iteratorResolutionResultsWithNonNullReceiver = resolveFakeCall(nonNullReceiver, context, iterator);
 			if(iteratorResolutionResultsWithNonNullReceiver.isSuccess())
 			{
 				iteratorResolutionResults = iteratorResolutionResultsWithNonNullReceiver;
 			}
 		}
 
 		if(iteratorResolutionResults.isSuccess())
 		{
 			MethodDescriptor iteratorMethod = iteratorResolutionResults.getResultingCall().getResultingDescriptor();
 
 			JetType iteratorType = iteratorMethod.getReturnType();
			if(!JetTypeChecker.INSTANCE.isSubtypeOf(iteratorType, TypeUtils.getTypeOfClassOrErrorType(context.scope, CodeTodo.ITERATOR)) || iteratorType.getArguments().size() != 1)
 				return ErrorUtils.createErrorType("Invalid iteration type");
 
 			context.trace.record(BindingContext.LOOP_RANGE_ITERATOR, loopRangeExpression, iteratorMethod);
 
 			return iteratorType.getArguments().get(0);
 		}
 		else
 		{
 			if(iteratorResolutionResults.isAmbiguity())
 			{
 				//                    StringBuffer stringBuffer = new StringBuffer("Method 'iterator()' is ambiguous for this expression: ");
 				//                    for (FunctionDescriptor functionDescriptor : iteratorResolutionResults.getResultingCalls()) {
 				//                        stringBuffer.append(DescriptorRenderer.TEXT.render(functionDescriptor)).append(" ");
 				//                    }
 				//                    errorMessage = stringBuffer.toString();
 				context.trace.report(Errors.ITERATOR_AMBIGUITY.on(loopRangeExpression, iteratorResolutionResults.getResultingCalls()));
 			}
 			else
 			{
 				context.trace.report(Errors.ITERATOR_MISSING.on(loopRangeExpression));
 			}
 		}
 		return null;
 	}
 
 	public static OverloadResolutionResults<MethodDescriptor> resolveFakeCall(ExpressionReceiver receiver, ExpressionTypingContext context, Name name)
 	{
 		NapileReferenceExpression fake = NapilePsiFactory.createSimpleName(context.expressionTypingServices.getProject(), "fake");
 		BindingTrace fakeTrace = new BindingTraceContext();
 		Call call = CallMaker.makeCall(fake, receiver, null, fake, Collections.<ValueArgument>emptyList());
 		return context.replaceBindingTrace(fakeTrace).resolveCallWithGivenName(call, fake, name);
 	}
 
 	@Override
 	public JetTypeInfo visitTryExpression(NapileTryExpression expression, ExpressionTypingContext context)
 	{
 		NapileExpression tryBlock = expression.getTryBlock();
 		List<NapileCatchClause> catchClauses = expression.getCatchClauses();
 		NapileFinallySection finallyBlock = expression.getFinallyBlock();
 		List<JetType> types = new ArrayList<JetType>();
 		for(NapileCatchClause catchClause : catchClauses)
 		{
 			NapileElement catchParameter = catchClause.getCatchParameter();
 			NapileExpression catchBody = catchClause.getCatchBody();
 			if(catchParameter instanceof NapilePropertyParameter)
 			{
 				VariableDescriptor variableDescriptor = context.expressionTypingServices.getDescriptorResolver().resolveLocalVariableDescriptor(context.scope.getContainingDeclaration(), context.scope, ((NapilePropertyParameter) catchParameter)
 						, context.trace);
 				JetType throwableType = TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.THROWABLE, false);
 				DataFlowUtils.checkType(variableDescriptor.getType(), ((NapilePropertyParameter) catchParameter), context.replaceExpectedType(throwableType));
 				if(catchBody != null)
 				{
 					WritableScope catchScope = ExpressionTypingUtils.newWritableScopeImpl(context, "Catch scope");
 					catchScope.addVariableDescriptor(variableDescriptor);
 					JetType type = facade.getTypeInfo(catchBody, context.replaceScope(catchScope)).getType();
 					if(type != null)
 					{
 						types.add(type);
 					}
 				}
 			}
 		}
 		if(finallyBlock != null)
 		{
 			facade.getTypeInfo(finallyBlock.getFinalExpression(), context.replaceExpectedType(TypeUtils.NO_EXPECTED_TYPE));
 		}
 		JetType type = facade.getTypeInfo(tryBlock, context).getType();
 		if(type != null)
 		{
 			types.add(type);
 		}
 		if(types.isEmpty())
 		{
 			return JetTypeInfo.create(null, context.dataFlowInfo);
 		}
 		else
 		{
 			return JetTypeInfo.create(CommonSupertypes.commonSupertype(types), context.dataFlowInfo);
 		}
 	}
 
 	@Override
 	public JetTypeInfo visitThrowExpression(NapileThrowExpression expression, ExpressionTypingContext context)
 	{
 		NapileExpression thrownExpression = expression.getThrownExpression();
 		if(thrownExpression != null)
 		{
 			JetType throwableType = TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.THROWABLE, false);
 			facade.getTypeInfo(thrownExpression, context.replaceExpectedType(throwableType).replaceScope(context.scope));
 		}
 		return DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL, false), expression, context, context.dataFlowInfo);
 	}
 
 	@Override
 	public JetTypeInfo visitReturnExpression(NapileReturnExpression expression, ExpressionTypingContext context)
 	{
 		NapileExpression returnedExpression = expression.getReturnedExpression();
 
 		JetType expectedType = TypeUtils.NO_EXPECTED_TYPE;
 		NapileExpression parentDeclaration = PsiTreeUtil.getParentOfType(expression, NapileDeclaration.class);
 		if(parentDeclaration instanceof NapileFunctionLiteral)
 		{
 			parentDeclaration = (NapileFunctionLiteralExpression) parentDeclaration.getParent();
 		}
 		if(parentDeclaration instanceof NapilePropertyParameter)
 		{
 			context.trace.report(Errors.RETURN_NOT_ALLOWED.on(expression));
 		}
 		assert parentDeclaration != null;
 		DeclarationDescriptor declarationDescriptor = context.trace.get(BindingContext.DECLARATION_TO_DESCRIPTOR, parentDeclaration);
 		MethodDescriptor containingMethodDescriptor = DescriptorUtils.getParentOfType(declarationDescriptor, MethodDescriptor.class, false);
 
 		if(containingMethodDescriptor != null)
 		{
 			PsiElement containingFunction = BindingContextUtils.callableDescriptorToDeclaration(context.trace.getBindingContext(), containingMethodDescriptor);
 			assert containingFunction != null;
 			if(containingFunction instanceof NapileFunctionLiteralExpression)
 			{
 				do
 				{
 					containingMethodDescriptor = DescriptorUtils.getParentOfType(containingMethodDescriptor, MethodDescriptor.class);
 					containingFunction = containingMethodDescriptor != null ? BindingContextUtils.callableDescriptorToDeclaration(context.trace.getBindingContext(), containingMethodDescriptor) : null;
 				}
 				while(containingFunction instanceof NapileFunctionLiteralExpression);
 				context.trace.report(Errors.RETURN_NOT_ALLOWED.on(expression));
 			}
 			if(containingMethodDescriptor != null)
 			{
 				expectedType = DescriptorUtils.getFunctionExpectedReturnType(containingMethodDescriptor, (NapileElement) containingFunction);
 			}
 		}
 		else
 		{
 			context.trace.report(Errors.RETURN_NOT_ALLOWED.on(expression));
 		}
 
 		if(returnedExpression != null)
 		{
 			facade.getTypeInfo(returnedExpression, context.replaceExpectedType(expectedType).replaceScope(context.scope));
 		}
 		else
 		{
 			if(expectedType != TypeUtils.NO_EXPECTED_TYPE && expectedType != null && !TypeUtils.isEqualFqName(expectedType, NapileLangPackage.NULL))
 			{
 				context.trace.report(Errors.RETURN_TYPE_MISMATCH.on(expression, expectedType));
 			}
 		}
 		return DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL, false), expression, context, context.dataFlowInfo);
 	}
 
 	@Override
 	public JetTypeInfo visitBreakExpression(NapileBreakExpression expression, ExpressionTypingContext context)
 	{
 		context.labelResolver.resolveLabel(expression, context);
 		return DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL, false), expression, context, context.dataFlowInfo);
 	}
 
 	@Override
 	public JetTypeInfo visitContinueExpression(NapileContinueExpression expression, ExpressionTypingContext context)
 	{
 		return DataFlowUtils.checkType(TypeUtils.getTypeOfClassOrErrorType(context.scope, NapileLangPackage.NULL, false), expression, context, context.dataFlowInfo);
 	}
 }
