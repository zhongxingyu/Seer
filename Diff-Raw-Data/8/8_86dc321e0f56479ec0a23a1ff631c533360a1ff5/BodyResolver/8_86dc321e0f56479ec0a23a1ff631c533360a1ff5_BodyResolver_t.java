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
 
 package org.napile.compiler.lang.resolve.processors;
 
 import static org.napile.compiler.lang.diagnostics.Errors.FINAL_SUPERTYPE;
 import static org.napile.compiler.lang.diagnostics.Errors.SUPERTYPE_APPEARS_TWICE;
 import static org.napile.compiler.lang.diagnostics.Errors.SUPERTYPE_NOT_INITIALIZED;
 import static org.napile.compiler.lang.diagnostics.Errors.SUPERTYPE_NOT_INITIALIZED_DEFAULT;
 import static org.napile.compiler.lang.resolve.BindingContext.DEFERRED_TYPE;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import org.jetbrains.annotations.NotNull;
 import org.napile.compiler.lang.descriptors.*;
 import org.napile.compiler.lang.psi.*;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.resolve.BindingTrace;
 import org.napile.compiler.lang.resolve.BodiesResolveContext;
 import org.napile.compiler.lang.resolve.ObservableBindingTrace;
 import org.napile.compiler.lang.resolve.TopDownAnalysisParameters;
 import org.napile.compiler.lang.resolve.TraceBasedRedeclarationHandler;
 import org.napile.compiler.lang.resolve.calls.CallMaker;
 import org.napile.compiler.lang.resolve.calls.CallResolver;
 import org.napile.compiler.lang.resolve.calls.OverloadResolutionResults;
 import org.napile.compiler.lang.resolve.calls.autocasts.DataFlowInfo;
 import org.napile.compiler.lang.resolve.scopes.JetScope;
 import org.napile.compiler.lang.resolve.scopes.WritableScope;
 import org.napile.compiler.lang.resolve.scopes.WritableScopeImpl;
 import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
 import org.napile.compiler.lang.types.DeferredType;
 import org.napile.compiler.lang.types.ErrorUtils;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lang.types.TypeConstructor;
 import org.napile.compiler.lang.types.TypeUtils;
 import org.napile.compiler.lang.types.expressions.ExpressionTypingServices;
 import org.napile.compiler.lexer.JetTokens;
 import org.napile.compiler.util.Box;
 import org.napile.compiler.util.lazy.ReenteringLazyValueComputationException;
 import org.napile.compiler.util.slicedmap.WritableSlice;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.intellij.psi.PsiElement;
 import com.intellij.util.containers.Queue;
 
 /**
  * @author abreslav
  */
 public class BodyResolver
 {
 	@NotNull
 	private BodiesResolveContext context;
 	@NotNull
 	private TopDownAnalysisParameters topDownAnalysisParameters;
 	@NotNull
 	private DescriptorResolver descriptorResolver;
 	@NotNull
 	private ExpressionTypingServices expressionTypingServices;
 	@NotNull
 	private CallResolver callResolver;
 	@NotNull
 	private ObservableBindingTrace trace;
 	@NotNull
 	private ControlFlowAnalyzer controlFlowAnalyzer;
 	@NotNull
 	private DeclarationsChecker declarationsChecker;
 	@NotNull
 	private AnnotationChecker annotationChecker;
 	@NotNull
 	private ModifiersChecker modifiersChecker;
 	@NotNull
 	private EnumEntryChecker enumEntryChecker;
 
 	@Inject
 	public void setTopDownAnalysisParameters(@NotNull TopDownAnalysisParameters topDownAnalysisParameters)
 	{
 		this.topDownAnalysisParameters = topDownAnalysisParameters;
 	}
 
 	@Inject
 	public void setDescriptorResolver(@NotNull DescriptorResolver descriptorResolver)
 	{
 		this.descriptorResolver = descriptorResolver;
 	}
 
 	@Inject
 	public void setExpressionTypingServices(@NotNull ExpressionTypingServices expressionTypingServices)
 	{
 		this.expressionTypingServices = expressionTypingServices;
 	}
 
 	@Inject
 	public void setCallResolver(@NotNull CallResolver callResolver)
 	{
 		this.callResolver = callResolver;
 	}
 
 	@Inject
 	public void setTrace(@NotNull BindingTrace trace)
 	{
 		this.trace = new ObservableBindingTrace(trace);
 	}
 
 	@Inject
 	public void setControlFlowAnalyzer(@NotNull ControlFlowAnalyzer controlFlowAnalyzer)
 	{
 		this.controlFlowAnalyzer = controlFlowAnalyzer;
 	}
 
 	@Inject
 	public void setDeclarationsChecker(@NotNull DeclarationsChecker declarationsChecker)
 	{
 		this.declarationsChecker = declarationsChecker;
 	}
 
 	@Inject
 	public void setAnnotationChecker(@NotNull AnnotationChecker annotationChecker)
 	{
 		this.annotationChecker = annotationChecker;
 	}
 
 	@Inject
 	public void setModifiersChecker(@NotNull ModifiersChecker modifiersChecker)
 	{
 		this.modifiersChecker = modifiersChecker;
 	}
 
 	@Inject
 	public void setEnumEntryChecker(@NotNull EnumEntryChecker enumEntryChecker)
 	{
 		this.enumEntryChecker = enumEntryChecker;
 	}
 
 	private void resolveBehaviorDeclarationBodies(@NotNull BodiesResolveContext bodiesResolveContext)
 	{
 		// Initialize context
 		context = bodiesResolveContext;
 
 		resolveDelegationSpecifierLists();
 
 		resolvePropertyDeclarationBodies();
 
 		resolveFunctionBodies();
 
 		resolveConstructorBodies();
 
 		if(!topDownAnalysisParameters.isDeclaredLocally())
 		{
 			computeDeferredTypes();
 		}
 	}
 
 	public void resolveBodies(@NotNull BodiesResolveContext bodiesResolveContext)
 	{
 		resolveBehaviorDeclarationBodies(bodiesResolveContext);
 
 		enumEntryChecker.process(bodiesResolveContext);
 		controlFlowAnalyzer.process(bodiesResolveContext);
 		modifiersChecker.process(bodiesResolveContext);
 		declarationsChecker.process(bodiesResolveContext);
 		annotationChecker.process(bodiesResolveContext);
 	}
 
 	private void resolveDelegationSpecifierLists()
 	{
 		for(Map.Entry<NapileClass, MutableClassDescriptor> classEntry : context.getClasses().entrySet())
 			for(NapileConstructor napileConstructor : classEntry.getKey().getConstructors())
 			{
 				ConstructorDescriptor constructorDescriptor = (ConstructorDescriptor) trace.get(BindingContext.DECLARATION_TO_DESCRIPTOR, napileConstructor);
 
 				assert constructorDescriptor != null;
 
				resolveDelegationSpecifierList(napileConstructor, constructorDescriptor, constructorDescriptor.getParametersScope());
 			}
 
 		for(Map.Entry<NapileAnonymClass, MutableClassDescriptor> entry : context.getAnonymous().entrySet())
 			resolveDelegationSpecifierList(entry.getKey(), entry.getValue(), entry.getValue().getScopeForSupertypeResolution());
 	}
 
 	private void resolveDelegationSpecifierList(final NapileDelegationSpecifierListOwner jetElement, @NotNull final DeclarationDescriptor declarationDescriptor, final @NotNull JetScope jetScope)
 	{
 		if(!context.completeAnalysisNeeded(jetElement))
 			return;
 
 		final Map<NapileTypeReference, JetType> supertypes = Maps.newLinkedHashMap();
 		NapileVisitorVoid visitor = new NapileVisitorVoid()
 		{
 			private void recordSupertype(NapileTypeReference typeReference, JetType supertype)
 			{
 				if(supertype == null)
 					return;
 				supertypes.put(typeReference, supertype);
 			}
 
 			@Override
 			public void visitDelegationToSuperCallSpecifier(NapileDelegatorToSuperCall call)
 			{
 				NapileValueArgumentList valueArgumentList = call.getValueArgumentList();
 				PsiElement elementToMark = valueArgumentList == null ? call : valueArgumentList;
 
 				NapileTypeReference typeReference = call.getTypeReference();
 				if(typeReference == null)
 					return;
 
 				OverloadResolutionResults<MethodDescriptor> results = callResolver.resolveFunctionCall(trace, jetScope, CallMaker.makeCall(ReceiverDescriptor.NO_RECEIVER, null, call), TypeUtils.NO_EXPECTED_TYPE, DataFlowInfo.EMPTY);
 				if(results.isSuccess())
 				{
 					JetType supertype = results.getResultingDescriptor().getReturnType();
 					recordSupertype(typeReference, supertype);
 				}
 				else
 				{
 					recordSupertype(typeReference, trace.getBindingContext().get(BindingContext.TYPE, typeReference));
 				}
 			}
 
 			@Override
 			public void visitDelegationToSuperClassSpecifier(NapileDelegatorToSuperClass specifier)
 			{
 				NapileTypeReference typeReference = specifier.getTypeReference();
 				JetType supertype = trace.getBindingContext().get(BindingContext.TYPE, typeReference);
 				recordSupertype(typeReference, supertype);
 				if(supertype == null)
 					return;
 				ClassDescriptor classDescriptor = TypeUtils.getClassDescriptor(supertype);
 				if(classDescriptor == null)
 					return;
 				if(!classDescriptor.getConstructors().isEmpty() && !ErrorUtils.isError(classDescriptor.getTypeConstructor()) )
 				{
 					boolean hasConstructorWithoutParams = false;
 					for(ConstructorDescriptor constructor : classDescriptor.getConstructors())
 						if(constructor.getValueParameters().isEmpty())
 							hasConstructorWithoutParams = true;
 
 					if(!hasConstructorWithoutParams)
 					{
 						trace.report(SUPERTYPE_NOT_INITIALIZED.on(specifier));
 					}
 					else
 					{
 						trace.report(SUPERTYPE_NOT_INITIALIZED_DEFAULT.on(specifier));
 					}
 				}
 			}
 
 			@Override
 			public void visitDelegationToThisCall(NapileDelegatorToThisCall thisCall)
 			{
 				throw new IllegalStateException("This-calls should be prohibited by the parser");
 			}
 
 			@Override
 			public void visitJetElement(NapileElement element)
 			{
 				throw new UnsupportedOperationException(element.getText() + " : " + element);
 			}
 		};
 
 		for(NapileDelegationSpecifier delegationSpecifier : jetElement.getDelegationSpecifiers())
 			delegationSpecifier.accept(visitor);
 
 		Set<TypeConstructor> parentEnum = Collections.emptySet();
 		if(jetElement instanceof NapileEnumEntry)
 			parentEnum = Collections.singleton(((ClassDescriptor) declarationDescriptor.getContainingDeclaration().getContainingDeclaration()).getTypeConstructor());
 
 		checkSupertypeList(supertypes, parentEnum);
 	}
 
 	// allowedFinalSupertypes typically contains a enum type of which supertypeOwner is an entry
 	private void checkSupertypeList(@NotNull Map<NapileTypeReference, JetType> supertypes, Set<TypeConstructor> allowedFinalSupertypes)
 	{
 		Set<TypeConstructor> typeConstructors = Sets.newHashSet();
 		boolean classAppeared = false;
 		for(Map.Entry<NapileTypeReference, JetType> entry : supertypes.entrySet())
 		{
 			NapileTypeReference typeReference = entry.getKey();
 			JetType supertype = entry.getValue();
 
 			TypeConstructor constructor = supertype.getConstructor();
 			if(!typeConstructors.add(constructor))
 			{
 				trace.report(SUPERTYPE_APPEARS_TWICE.on(typeReference));
 			}
 
 			if(constructor.isSealed() && !allowedFinalSupertypes.contains(constructor))
 			{
 				trace.report(FINAL_SUPERTYPE.on(typeReference));
 			}
 		}
 	}
 
 
 	private void resolvePropertyDeclarationBodies()
 	{
 		for(Map.Entry<NapileProperty, PropertyDescriptor> entry : this.context.getProperties().entrySet())
 		{
 			NapileProperty property = entry.getKey();
 			if(!context.completeAnalysisNeeded(property))
 				continue;
 
 			final PropertyDescriptor propertyDescriptor = entry.getValue();
 
 			computeDeferredType(propertyDescriptor.getReturnType());
 
 			JetScope declaringScope = this.context.getDeclaringScopes().get(property);
 
 			NapileExpression initializer = property.getInitializer();
 			if(initializer != null)
 			{
 				resolvePropertyInitializer(property, propertyDescriptor, initializer, declaringScope);
 			}
 
 			resolvePropertyAccessors(property, propertyDescriptor);
 		}
 	}
 
 	private JetScope makeScopeForPropertyAccessor(@NotNull NapilePropertyAccessor accessor, PropertyDescriptor propertyDescriptor)
 	{
 		JetScope declaringScope = context.getDeclaringScopes().get(accessor);
 
 		JetScope propertyDeclarationInnerScope = descriptorResolver.getPropertyDeclarationInnerScope(declaringScope, propertyDescriptor.getTypeParameters(), trace);
 		WritableScope accessorScope = new WritableScopeImpl(propertyDeclarationInnerScope, declaringScope.getContainingDeclaration(), new TraceBasedRedeclarationHandler(trace), "Accessor scope");
 		accessorScope.changeLockLevel(WritableScope.LockLevel.READING);
 
 		return accessorScope;
 	}
 
 	private void resolvePropertyAccessors(NapileProperty property, PropertyDescriptor propertyDescriptor)
 	{
 		ObservableBindingTrace fieldAccessTrackingTrace = createFieldTrackingTrace(propertyDescriptor);
 
 		NapilePropertyAccessor getter = property.getGetter();
 		PropertyGetterDescriptor getterDescriptor = propertyDescriptor.getGetter();
 		if(getter != null && getterDescriptor != null)
 		{
 			JetScope accessorScope = makeScopeForPropertyAccessor(getter, propertyDescriptor);
 			resolveFunctionBody(fieldAccessTrackingTrace, getter, getterDescriptor, accessorScope);
 		}
 
 		NapilePropertyAccessor setter = property.getSetter();
 		PropertySetterDescriptor setterDescriptor = propertyDescriptor.getSetter();
 		if(setter != null && setterDescriptor != null)
 		{
 			JetScope accessorScope = makeScopeForPropertyAccessor(setter, propertyDescriptor);
 			resolveFunctionBody(fieldAccessTrackingTrace, setter, setterDescriptor, accessorScope);
 		}
 	}
 
 	private ObservableBindingTrace createFieldTrackingTrace(final PropertyDescriptor propertyDescriptor)
 	{
 		return new ObservableBindingTrace(trace).addHandler(BindingContext.REFERENCE_TARGET, new ObservableBindingTrace.RecordHandler<NapileReferenceExpression, DeclarationDescriptor>()
 		{
 			@Override
 			public void handleRecord(WritableSlice<NapileReferenceExpression, DeclarationDescriptor> slice, NapileReferenceExpression expression, DeclarationDescriptor descriptor)
 			{
 				if(expression instanceof NapileSimpleNameExpression)
 				{
 					NapileSimpleNameExpression simpleNameExpression = (NapileSimpleNameExpression) expression;
 					if(simpleNameExpression.getReferencedNameElementType() == JetTokens.FIELD_IDENTIFIER)
 					{
 						// This check may be considered redundant as long as $x is only accessible from accessors to $x
 						if(descriptor == propertyDescriptor)
 						{ // TODO : original?
 							trace.record(BindingContext.BACKING_FIELD_REQUIRED, propertyDescriptor); // TODO: this trace?
 						}
 					}
 				}
 			}
 		});
 	}
 
 	private void resolvePropertyInitializer(NapileProperty property, PropertyDescriptor propertyDescriptor, NapileExpression initializer, JetScope scope)
 	{
 		//JetFlowInformationProvider flowInformationProvider = context.getDescriptorResolver().computeFlowData(property, initializer); // TODO : flow JET-15
 		JetType expectedTypeForInitializer = property.getPropertyTypeRef() != null ? propertyDescriptor.getType() : TypeUtils.NO_EXPECTED_TYPE;
 		JetScope propertyDeclarationInnerScope = descriptorResolver.getPropertyDeclarationInnerScope(scope, propertyDescriptor.getTypeParameters(), trace);
 		JetType type = expressionTypingServices.getType(propertyDeclarationInnerScope, initializer, expectedTypeForInitializer, DataFlowInfo.EMPTY, trace);
 		//
 		//        JetType expectedType = propertyDescriptor.getInType();
 		//        if (expectedType == null) {
 		//            expectedType = propertyDescriptor.getType();
 		//        }
 		//        if (type != null && expectedType != null
 		//            && !context.getSemanticServices().getTypeChecker().isSubtypeOf(type, expectedType)) {
 		////            trace.report(TYPE_MISMATCH.on(initializer, expectedType, type));
 		//        }
 	}
 
 	private void resolveFunctionBodies()
 	{
 		for(Map.Entry<NapileNamedFunction, SimpleMethodDescriptor> entry : this.context.getMethods().entrySet())
 		{
 			NapileNamedFunction declaration = entry.getKey();
 			SimpleMethodDescriptor descriptor = entry.getValue();
 
 			computeDeferredType(descriptor.getReturnType());
 
 			JetScope declaringScope = this.context.getDeclaringScopes().get(declaration);
 			assert declaringScope != null;
 
 			resolveFunctionBody(trace, declaration, descriptor, declaringScope);
 
 			assert descriptor.getReturnType() != null;
 		}
 	}
 
 	private void resolveConstructorBodies()
 	{
 		for(Map.Entry<NapileClass, MutableClassDescriptor> entry : context.getClasses().entrySet())
 			for(NapileStaticConstructor constructor : entry.getKey().getStaticConstructors())
 			{
 				JetScope declaringScope = context.getDeclaringScopes().get(constructor);
 				assert declaringScope != null;
 
 				ConstructorDescriptor constructorDescriptor = trace.safeGet(BindingContext.CONSTRUCTOR, constructor);
 
 				resolveFunctionBody(trace, constructor, constructorDescriptor, declaringScope);
 			}
 
 		for(Map.Entry<NapileConstructor, ConstructorDescriptor> entry : context.getConstructors().entrySet())
 		{
 			NapileConstructor declaration = entry.getKey();
 			ConstructorDescriptor descriptor = entry.getValue();
 
 			JetScope declaringScope = context.getDeclaringScopes().get(declaration);
 			assert declaringScope != null;
 
 			resolveFunctionBody(trace, declaration, descriptor, declaringScope);
 		}
 	}
 
 
 	private void resolveFunctionBody(@NotNull BindingTrace trace, @NotNull NapileDeclarationWithBody function, @NotNull MethodDescriptor methodDescriptor, @NotNull JetScope declaringScope)
 	{
 		if(!context.completeAnalysisNeeded(function))
 			return;
 
 		NapileExpression bodyExpression = function.getBodyExpression();
 		JetScope functionInnerScope = FunctionDescriptorUtil.getFunctionInnerScope(declaringScope, methodDescriptor, trace);
 		if(bodyExpression != null)
 		{
 			expressionTypingServices.checkFunctionReturnType(functionInnerScope, function, methodDescriptor, DataFlowInfo.EMPTY, null, trace);
 		}
 
 		List<NapileElement> valueParameters = function.getValueParameters();
 		List<ParameterDescriptor> parameterDescriptors = methodDescriptor.getValueParameters();
 
 		checkDefaultParameterValues(valueParameters, parameterDescriptors, functionInnerScope);
 
 		assert methodDescriptor.getReturnType() != null;
 	}
 
 	private void checkDefaultParameterValues(List<NapileElement> valueParameters, List<ParameterDescriptor> parameterDescriptors, JetScope declaringScope)
 	{
 		for(int i = 0; i < valueParameters.size(); i++)
 		{
 			ParameterDescriptor parameterDescriptor = parameterDescriptors.get(i);
 			if(parameterDescriptor.hasDefaultValue())
 			{
 				NapileElement jetParameter = valueParameters.get(i);
 				NapileExpression defaultValue = jetParameter instanceof NapilePropertyParameter ? ((NapilePropertyParameter) jetParameter).getDefaultValue() : null;
 				if(defaultValue != null)
 					expressionTypingServices.getType(declaringScope, defaultValue, parameterDescriptor.getType(), DataFlowInfo.EMPTY, trace);
 			}
 		}
 	}
 
 	private static void computeDeferredType(JetType type)
 	{
 		// handle type inference loop: function or property body contains a reference to itself
 		// fun f() = { f() }
 		// val x = x
 		// type resolution must be started before body resolution
 		if(type instanceof DeferredType)
 		{
 			DeferredType deferredType = (DeferredType) type;
 			if(!deferredType.isComputed())
 			{
 				deferredType.getActualType();
 			}
 		}
 	}
 
 	private void computeDeferredTypes()
 	{
 		Collection<Box<DeferredType>> deferredTypes = trace.getKeys(DEFERRED_TYPE);
 		if(deferredTypes != null)
 		{
 			// +1 is a work around agains new Queue(0).addLast(...) bug // stepan.koltsov@ 2011-11-21
 			final Queue<DeferredType> queue = new Queue<DeferredType>(deferredTypes.size() + 1);
 			trace.addHandler(DEFERRED_TYPE, new ObservableBindingTrace.RecordHandler<Box<DeferredType>, Boolean>()
 			{
 				@Override
 				public void handleRecord(WritableSlice<Box<DeferredType>, Boolean> deferredTypeKeyDeferredTypeWritableSlice, Box<DeferredType> key, Boolean value)
 				{
 					queue.addLast(key.getData());
 				}
 			});
 			for(Box<DeferredType> deferredType : deferredTypes)
 			{
 				queue.addLast(deferredType.getData());
 			}
 			while(!queue.isEmpty())
 			{
 				DeferredType deferredType = queue.pullFirst();
 				if(!deferredType.isComputed())
 				{
 					try
 					{
 						deferredType.getActualType(); // to compute
 					}
 					catch(ReenteringLazyValueComputationException e)
 					{
 						// A problem should be reported while computing the type
 					}
 				}
 			}
 		}
 	}
 }
