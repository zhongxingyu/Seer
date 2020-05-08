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
 
 import static org.napile.compiler.lang.diagnostics.Errors.CYCLIC_INHERITANCE_HIERARCHY;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
import org.napile.asm.resolve.name.Name;
 import org.napile.compiler.lang.descriptors.*;
 import org.napile.compiler.lang.descriptors.annotations.AnnotationDescriptor;
 import org.napile.compiler.lang.diagnostics.Errors;
 import org.napile.compiler.lang.psi.*;
 import org.napile.compiler.lang.resolve.BindingContext;
 import org.napile.compiler.lang.resolve.BindingContextUtils;
 import org.napile.compiler.lang.resolve.BindingTrace;
 import org.napile.compiler.lang.resolve.NamespaceFactoryImpl;
 import org.napile.compiler.lang.resolve.TopDownAnalysisContext;
 import org.napile.compiler.lang.resolve.TraceBasedRedeclarationHandler;
 import org.napile.compiler.lang.resolve.processors.members.AnnotationResolver;
 import org.napile.compiler.lang.resolve.processors.members.TypeParameterResolver;
 import org.napile.compiler.lang.resolve.scopes.JetScope;
 import org.napile.compiler.lang.resolve.scopes.RedeclarationHandler;
 import org.napile.compiler.lang.resolve.scopes.WritableScope;
 import org.napile.compiler.lang.resolve.scopes.WriteThroughScope;
 import org.napile.compiler.lang.types.JetType;
 import org.napile.compiler.lang.types.SubstitutionUtils;
 import org.napile.compiler.lang.types.TypeConstructor;
 import org.napile.compiler.util.PluginKeys;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Sets;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiNameIdentifierOwner;
 
 /**
  * @author abreslav
  */
 public class TypeHierarchyResolver
 {
 	@NotNull
 	private TopDownAnalysisContext context;
 	@NotNull
 	private ImportsResolver importsResolver;
 	@NotNull
 	private DescriptorResolver descriptorResolver;
 	@NotNull
 	private TypeParameterResolver typeParameterResolver;
 	@NotNull
 	private AnnotationResolver annotationResolver;
 	@NotNull
 	private NamespaceFactoryImpl namespaceFactory;
 	@NotNull
 	private BindingTrace trace;
 
 	// state
 	private LinkedList<MutableClassDescriptor> topologicalOrder;
 
 	@Inject
 	public void setContext(@NotNull TopDownAnalysisContext context)
 	{
 		this.context = context;
 	}
 
 	@Inject
 	public void setImportsResolver(@NotNull ImportsResolver importsResolver)
 	{
 		this.importsResolver = importsResolver;
 	}
 
 	@Inject
 	public void setDescriptorResolver(@NotNull DescriptorResolver descriptorResolver)
 	{
 		this.descriptorResolver = descriptorResolver;
 	}
 
 	@Inject
 	public void setNamespaceFactory(@NotNull NamespaceFactoryImpl namespaceFactory)
 	{
 		this.namespaceFactory = namespaceFactory;
 	}
 
 	@Inject
 	public void setTypeParameterResolver(@NotNull TypeParameterResolver typeParameterResolver)
 	{
 		this.typeParameterResolver = typeParameterResolver;
 	}
 
 	@Inject
 	public void setTrace(@NotNull BindingTrace trace)
 	{
 		this.trace = trace;
 	}
 
 	@Inject
 	public void setAnnotationResolver(@NotNull AnnotationResolver annotationResolver)
 	{
 		this.annotationResolver = annotationResolver;
 	}
 
 	public void process(@NotNull JetScope outerScope, @NotNull DescriptorBuilder owner, @NotNull Collection<? extends PsiElement> declarations)
 	{
 
 		{
 			// TODO: Very temp code - main goal is to remove recursion from collectNamespacesAndClassifiers
 			Queue<NapileDeclarationContainer> forDeferredResolve = new LinkedList<NapileDeclarationContainer>();
 			forDeferredResolve.addAll(collectNamespacesAndClassifiers(outerScope, owner, declarations.toArray(new PsiElement[declarations.size()])));
 
 			while(!forDeferredResolve.isEmpty())
 			{
 				NapileDeclarationContainer declarationContainer = forDeferredResolve.poll();
 				assert declarationContainer != null;
 
 				WithDeferredResolve descriptorForDeferredResolve = context.forDeferredResolver.get(declarationContainer);
 				JetScope scope = context.normalScope.get(declarationContainer);
 
 				// Even more temp code
 				if(descriptorForDeferredResolve instanceof MutableClassDescriptorLite)
 				{
 					forDeferredResolve.addAll(collectNamespacesAndClassifiers(scope, ((MutableClassDescriptorLite) descriptorForDeferredResolve).getBuilder(), declarationContainer.getDeclarations()));
 				}
 				else if(descriptorForDeferredResolve instanceof PackageDescriptorImpl)
 				{
 					forDeferredResolve.addAll(collectNamespacesAndClassifiers(scope, ((PackageDescriptorImpl) descriptorForDeferredResolve).getBuilder(), declarationContainer.getDeclarations()));
 				}
 				else
 				{
 					assert false;
 				}
 			}
 		}
 
 		importsResolver.processTypeImports(outerScope);
 
 		createTypeConstructors(); // create type constructors for classes and generic parameters, supertypes are not filled in
 		resolveTypesInClassHeaders(); // Generic bounds and types in supertype lists (no expressions or constructor resolution)
 
 		topologicalOrder = topologicallySortClassesAndObjects();
 
 		// Detect and disconnect all loops in the hierarchy
 		detectAndDisconnectLoops();
 
 		// At this point, there are no loops in the type hierarchy
 
 		checkSupertypesForConsistency();
 		//        computeSuperclasses();
 
 		checkTypesInClassHeaders(); // Check bounds in the types used in generic bounds and supertype lists
 	}
 
 	@Nullable
 	private Collection<NapileDeclarationContainer> collectNamespacesAndClassifiers(@NotNull final JetScope outerScope, @NotNull final DescriptorBuilder owner, @NotNull PsiElement[] declarations)
 	{
 		final Collection<NapileDeclarationContainer> forDeferredResolve = new ArrayList<NapileDeclarationContainer>();
 
 		for(PsiElement declaration : declarations)
 		{
 			declaration.accept(new NapileVisitorVoid()
 			{
 				@Override
 				public void visitNapileFile(NapileFile file)
 				{
 					PackageDescriptorImpl namespaceDescriptor = namespaceFactory.createNamespaceDescriptorPathIfNeeded(file, outerScope, RedeclarationHandler.DO_NOTHING);
 					context.getNamespaceDescriptors().put(file, namespaceDescriptor);
 
 					WriteThroughScope namespaceScope = new WriteThroughScope(outerScope, namespaceDescriptor.getMemberScope(), new TraceBasedRedeclarationHandler(trace), "namespace");
 					namespaceScope.changeLockLevel(WritableScope.LockLevel.BOTH);
 					context.getNamespaceScopes().put(file, namespaceScope);
 
 					prepareForDeferredCall(namespaceScope, namespaceDescriptor, file);
 				}
 
 				@Override
 				public void visitClass(NapileClass declaration)
 				{
 					MutableClassDescriptor mutableClassDescriptor = new MutableClassDescriptor(owner.getOwnerForChildren(), outerScope, ClassKind.CLASS, NapilePsiUtil.safeName(declaration.getName()), annotationResolver.bindAnnotations(outerScope, declaration, trace), NapilePsiUtil.isStatic(declaration));
 
 					context.getClasses().put(declaration, mutableClassDescriptor);
 					trace.record(BindingContext.FQNAME_TO_CLASS_DESCRIPTOR, NapilePsiUtil.getFQName(declaration), mutableClassDescriptor);
 
 					JetScope classScope = mutableClassDescriptor.getScopeForMemberResolution();
 
 					prepareForDeferredCall(classScope, mutableClassDescriptor, declaration);
 
 					owner.addClassifierDescriptor(mutableClassDescriptor);
 				}
 
 				@Override
 				public void visitEnumValue(NapileEnumValue value)
 				{
					MutableClassDescriptor mutableClassDescriptor = new MutableClassDescriptor(owner.getOwnerForChildren(), outerScope, ClassKind.CLASS, Name.identifier(value.getName()), annotationResolver.bindAnnotations(outerScope, value, trace), true);
 
 					ConstructorDescriptor constructorDescriptor = new ConstructorDescriptor(mutableClassDescriptor, Collections.<AnnotationDescriptor>emptyList(), false);
 					constructorDescriptor.initialize(Collections.<TypeParameterDescriptor>emptyList(), Collections.<CallParameterDescriptor>emptyList(), Visibility.PUBLIC);
 					mutableClassDescriptor.addConstructor(constructorDescriptor);
 
 					trace.record(BindingContext.CONSTRUCTOR, value, constructorDescriptor);
 
 					context.getEnumValues().put(value, mutableClassDescriptor);
 
 					trace.record(BindingContext.CLASS, value, mutableClassDescriptor);
 				}
 
 				@Override
 				public void visitAnonymClass(NapileAnonymClass declaration)
 				{
 					MutableClassDescriptor mutableClassDescriptor = new MutableClassDescriptor(owner.getOwnerForChildren(), outerScope, ClassKind.ANONYM_CLASS, NapilePsiUtil.safeName(declaration.getName()), annotationResolver.bindAnnotations(outerScope, declaration, trace), false);
 					context.getAnonymous().put(declaration, mutableClassDescriptor);
 
 					JetScope classScope = mutableClassDescriptor.getScopeForMemberResolution();
 
 					prepareForDeferredCall(classScope, mutableClassDescriptor, declaration);
 
 					ConstructorDescriptor constructorDescriptor = new ConstructorDescriptor(mutableClassDescriptor, Collections.<AnnotationDescriptor>emptyList(), false);
 					constructorDescriptor.initialize(Collections.<TypeParameterDescriptor>emptyList(), Collections.<CallParameterDescriptor>emptyList(), Visibility.PUBLIC);
 					mutableClassDescriptor.addConstructor(constructorDescriptor);
 
 					trace.record(BindingContext.CONSTRUCTOR, declaration, constructorDescriptor);
 
 					owner.addAnonymClassDescriptor(mutableClassDescriptor);
 					trace.record(BindingContext.CLASS, declaration, mutableClassDescriptor);
 				}
 
 				private void prepareForDeferredCall(@NotNull JetScope outerScope, @NotNull WithDeferredResolve withDeferredResolve, @NotNull NapileDeclarationContainer container)
 				{
 					forDeferredResolve.add(container);
 					context.normalScope.put(container, outerScope);
 					context.forDeferredResolver.put(container, withDeferredResolve);
 				}
 			});
 		}
 
 		return forDeferredResolve;
 	}
 
 
 	private void createTypeConstructors()
 	{
 		for(Map.Entry<NapileClass, MutableClassDescriptor> entry : context.getClasses().entrySet())
 		{
 			NapileClass napileClass = entry.getKey();
 			MutableClassDescriptor descriptor = entry.getValue();
 			descriptorResolver.resolveMutableClassDescriptor(napileClass, descriptor, trace);
 			descriptor.createTypeConstructor();
 			napileClass.putUserData(PluginKeys.DESCRIPTOR_KEY, descriptor);
 		}
 
 		for(Map.Entry<NapileAnonymClass, MutableClassDescriptor> entry : context.getAnonymous().entrySet())
 		{
 			NapileAnonymClass napileClass = entry.getKey();
 			MutableClassDescriptor descriptor = entry.getValue();
 			descriptor.setModality(Modality.FINAL);
 			descriptor.setVisibility(Visibility.PUBLIC);
 			descriptor.setTypeParameterDescriptors(new ArrayList<TypeParameterDescriptor>(0));
 			descriptor.createTypeConstructor();
 			napileClass.putUserData(PluginKeys.DESCRIPTOR_KEY, descriptor);
 		}
 
 		for(Map.Entry<NapileEnumValue, MutableClassDescriptor> entry : context.getEnumValues().entrySet())
 		{
 			MutableClassDescriptor descriptor = entry.getValue();
 			descriptor.setModality(Modality.FINAL);
 			descriptor.setVisibility(Visibility.PUBLIC);
 			descriptor.setTypeParameterDescriptors(new ArrayList<TypeParameterDescriptor>(0));
 			descriptor.createTypeConstructor();
 		}
 	}
 
 	private void resolveTypesInClassHeaders()
 	{
 		for(Map.Entry<NapileClass, MutableClassDescriptor> entry : context.getClasses().entrySet())
 		{
 			NapileClass napileClass = entry.getKey();
 			MutableClassDescriptor descriptor = entry.getValue();
 
 			typeParameterResolver.postResolving(napileClass, descriptor.getScopeForSupertypeResolution(), descriptor.getTypeConstructor().getParameters(), trace);
 			descriptorResolver.resolveSupertypesForMutableClassDescriptor(napileClass, descriptor, trace);
 		}
 
 		//for(Map.Entry<NapileAnonymClass, MutableClassDescriptor> entry : context.getAnonymous().entrySet())
 		//{
 		//	NapileClassLike anonymClass = entry.getKey();
 		//	MutableClassDescriptor descriptor = entry.getValue();
 		//	descriptorResolver.resolveSupertypesForMutableClassDescriptor(anonymClass, descriptor, trace);
 		//}
 
 		for(Map.Entry<NapileEnumValue, MutableClassDescriptor> entry : context.getEnumValues().entrySet())
 		{
 			NapileEnumValue enumValue = entry.getKey();
 			MutableClassDescriptor descriptor = entry.getValue();
 			descriptorResolver.resolveSupertypesForMutableClassDescriptor(enumValue, descriptor, trace);
 		}
 	}
 
 	private LinkedList<MutableClassDescriptor> topologicallySortClassesAndObjects()
 	{
 		// A topsort is needed only for better diagnostics:
 		//    edges that get removed to disconnect loops are more reasonable in this case
 		LinkedList<MutableClassDescriptor> topologicalOrder = Lists.newLinkedList();
 		Set<ClassDescriptor> visited = Sets.newHashSet();
 		for(MutableClassDescriptor mutableClassDescriptor : context.getClasses().values())
 		{
 			topologicallySort(mutableClassDescriptor, visited, topologicalOrder);
 		}
 		for(MutableClassDescriptor mutableClassDescriptor : context.getAnonymous().values())
 		{
 			topologicallySort(mutableClassDescriptor, visited, topologicalOrder);
 		}
 		return topologicalOrder;
 	}
 
 	private void detectAndDisconnectLoops()
 	{
 		// Loop detection and disconnection
 		Set<ClassDescriptor> visited = Sets.newHashSet();
 		Set<ClassDescriptor> beingProcessed = Sets.newHashSet();
 		List<ClassDescriptor> currentPath = Lists.newArrayList();
 		for(MutableClassDescriptor mutableClassDescriptor : topologicalOrder)
 		{
 			traverseTypeHierarchy(mutableClassDescriptor, visited, beingProcessed, currentPath);
 		}
 	}
 
 	private static void topologicallySort(MutableClassDescriptor mutableClassDescriptor, Set<ClassDescriptor> visited, LinkedList<MutableClassDescriptor> topologicalOrder)
 	{
 		if(!visited.add(mutableClassDescriptor))
 		{
 			return;
 		}
 		for(JetType supertype : mutableClassDescriptor.getSupertypes())
 		{
 			DeclarationDescriptor declarationDescriptor = supertype.getConstructor().getDeclarationDescriptor();
 			if(declarationDescriptor instanceof MutableClassDescriptor)
 			{
 				MutableClassDescriptor classDescriptor = (MutableClassDescriptor) declarationDescriptor;
 				topologicallySort(classDescriptor, visited, topologicalOrder);
 			}
 		}
 		topologicalOrder.addFirst(mutableClassDescriptor);
 	}
 
 	private void traverseTypeHierarchy(MutableClassDescriptor currentClass, Set<ClassDescriptor> visited, Set<ClassDescriptor> beingProcessed, List<ClassDescriptor> currentPath)
 	{
 		if(!visited.add(currentClass))
 		{
 			if(beingProcessed.contains(currentClass))
 			{
 				markCycleErrors(currentPath, currentClass);
 				assert !currentPath.isEmpty() : "Cycle cannot be found on an empty currentPath";
 				ClassDescriptor subclassOfCurrent = currentPath.get(currentPath.size() - 1);
 				assert subclassOfCurrent instanceof MutableClassDescriptor;
 				// Disconnect the loop
 				for(Iterator<JetType> iterator = subclassOfCurrent.getSupertypes().iterator(); iterator.hasNext(); )
 				{
 					JetType type = iterator.next();
 					if(type.getConstructor() == currentClass.getTypeConstructor())
 					{
 						iterator.remove();
 						break;
 					}
 				}
 			}
 			return;
 		}
 
 		beingProcessed.add(currentClass);
 		currentPath.add(currentClass);
 		for(JetType supertype : Lists.newArrayList(currentClass.getSupertypes()))
 		{
 			DeclarationDescriptor declarationDescriptor = supertype.getConstructor().getDeclarationDescriptor();
 			if(declarationDescriptor instanceof MutableClassDescriptor)
 			{
 				MutableClassDescriptor mutableClassDescriptor = (MutableClassDescriptor) declarationDescriptor;
 				traverseTypeHierarchy(mutableClassDescriptor, visited, beingProcessed, currentPath);
 			}
 		}
 		beingProcessed.remove(currentClass);
 		currentPath.remove(currentPath.size() - 1);
 	}
 
 	private void markCycleErrors(List<ClassDescriptor> currentPath, @NotNull ClassDescriptor current)
 	{
 		int size = currentPath.size();
 		for(int i = size - 1; i >= 0; i--)
 		{
 			ClassDescriptor classDescriptor = currentPath.get(i);
 
 			ClassDescriptor superclass = (i < size - 1) ? currentPath.get(i + 1) : current;
 			PsiElement psiElement = BindingContextUtils.classDescriptorToDeclaration(trace.getBindingContext(), classDescriptor);
 
 			PsiElement elementToMark = null;
 			if(psiElement instanceof NapileClass)
 			{
 				NapileClass napileClass = (NapileClass) psiElement;
 				for(NapileTypeReference typeReference : napileClass.getSuperTypes())
 				{
 					JetType supertype = trace.get(BindingContext.TYPE, typeReference);
 					if(supertype != null && supertype.getConstructor() == superclass.getTypeConstructor())
 						elementToMark = typeReference;
 				}
 			}
 			else if(psiElement instanceof NapileAnonymClass)
 			{
 				NapileAnonymClass anonymClass = (NapileAnonymClass) psiElement;
 				for(NapileDelegationToSuperCall delegationSpecifier : anonymClass.getDelegationSpecifiers())
 				{
 					NapileTypeReference typeReference = delegationSpecifier.getTypeReference();
 					if(typeReference == null)
 						continue;
 					JetType supertype = trace.get(BindingContext.TYPE, typeReference);
 					if(supertype != null && supertype.getConstructor() == superclass.getTypeConstructor())
 						elementToMark = typeReference;
 				}
 			}
 
 			if(elementToMark == null && psiElement instanceof PsiNameIdentifierOwner)
 			{
 				PsiNameIdentifierOwner namedElement = (PsiNameIdentifierOwner) psiElement;
 				PsiElement nameIdentifier = namedElement.getNameIdentifier();
 				if(nameIdentifier != null)
 				{
 					elementToMark = nameIdentifier;
 				}
 			}
 			if(elementToMark != null)
 			{
 				trace.report(CYCLIC_INHERITANCE_HIERARCHY.on(elementToMark));
 			}
 
 			if(classDescriptor == current)
 			{
 				// Beginning of cycle is found
 				break;
 			}
 		}
 	}
 
 	private void checkSupertypesForConsistency()
 	{
 		for(MutableClassDescriptor mutableClassDescriptor : topologicalOrder)
 		{
 			Multimap<TypeConstructor, JetType> multimap = SubstitutionUtils.buildDeepSubstitutionMultimap(mutableClassDescriptor.getDefaultType());
 			for(Map.Entry<TypeConstructor, Collection<JetType>> entry : multimap.asMap().entrySet())
 			{
 				Collection<JetType> projections = entry.getValue();
 				if(projections.size() > 1)
 				{
 					TypeConstructor typeConstructor = entry.getKey();
 					DeclarationDescriptor declarationDescriptor = typeConstructor.getDeclarationDescriptor();
 					assert declarationDescriptor instanceof TypeParameterDescriptor : declarationDescriptor;
 					TypeParameterDescriptor typeParameterDescriptor = (TypeParameterDescriptor) declarationDescriptor;
 
 					// Immediate arguments of supertypes cannot be projected
 					Set<JetType> conflictingTypes = Sets.newLinkedHashSet();
 					for(JetType projection : projections)
 					{
 						conflictingTypes.add(projection);
 					}
 
 					if(conflictingTypes.size() > 1)
 					{
 						DeclarationDescriptor containingDeclaration = typeParameterDescriptor.getContainingDeclaration();
 						assert containingDeclaration instanceof ClassDescriptor : containingDeclaration;
 						NapileClassLike psiElement = (NapileClassLike) BindingContextUtils.classDescriptorToDeclaration(trace.getBindingContext(), mutableClassDescriptor);
 						NapileElement extendTypeListElement = psiElement.getSuperTypesElement();
 						assert extendTypeListElement != null;
 						trace.report(Errors.INCONSISTENT_TYPE_PARAMETER_VALUES.on(extendTypeListElement, typeParameterDescriptor, (ClassDescriptor) containingDeclaration, conflictingTypes));
 					}
 				}
 			}
 		}
 	}
 
 	private void checkTypesInClassHeaders()
 	{
 		for(Map.Entry<NapileClass, MutableClassDescriptor> entry : context.getClasses().entrySet())
 		{
 			NapileClass napileClass = entry.getKey();
 
 			for(NapileTypeReference typeReference : napileClass.getSuperTypes())
 			{
 				JetType type = trace.getBindingContext().get(BindingContext.TYPE, typeReference);
 				if(type != null)
 						descriptorResolver.checkBounds(typeReference, type, trace);
 			}
 
 			for(NapileTypeParameter jetTypeParameter : napileClass.getTypeParameters())
 			{
 				for(NapileTypeReference extendsBound : jetTypeParameter.getExtendsBound())
 				{
 					JetType type = trace.getBindingContext().get(BindingContext.TYPE, extendsBound);
 					if(type != null)
 					{
 						descriptorResolver.checkBounds(extendsBound, type, trace);
 					}
 				}
 			}
 		}
 	}
 }
