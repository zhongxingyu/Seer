 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.core.model.binary;
 
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.compiler.IBinaryElementRequestor;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.internal.core.DuplicateResolver;
 import org.eclipse.dltk.internal.core.ImportContainer;
 import org.eclipse.dltk.internal.core.MethodParameterInfo;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.ModelElementInfo;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.SourceMethodUtils;
 import org.eclipse.dltk.internal.core.SourceRefElement;
 
 /**
  * @since 2.0
  */
 public class BinaryModuleStructureRequestor implements IBinaryElementRequestor {
 
 	private DuplicateResolver.Resolver counters = DuplicateResolver.create();
 
 	private final static String[] EMPTY = new String[0];
 
 	/**
 	 * The handle to the Binary module being parsed
 	 */
 	private IBinaryModule module;
 
 	/**
 	 * The import container info - null until created
 	 */
 	protected Map<String, ImportContainer> importContainers = null;
 
 	/**
 	 * Stack of parent handles, corresponding to the info stack. We keep both,
 	 * since info objects do not have back pointers to handles.
 	 */
 	private Stack<ModelElement> handleStack;
 	private Stack<ModelElementInfo> infoStack;
 
 	private BinaryModuleElementInfo moduleInfo;
 
 	private SourceMapper mapper;
 	private Map<IModelElement, ModelElementInfo> newElements;
 
 	public BinaryModuleStructureRequestor(IBinaryModule module,
 			BinaryModuleElementInfo moduleInfo, SourceMapper mapper,
 			Map<IModelElement, ModelElementInfo> newElements) {
 		this.module = module;
 		this.moduleInfo = moduleInfo;
 		this.mapper = mapper;
 		this.newElements = newElements;
 	}
 
 	public void enterModule() {
 		this.handleStack = new Stack<ModelElement>();
 		this.infoStack = new Stack<ModelElementInfo>();
 		this.enterModuleRoot();
 	}
 
 	public void enterModuleRoot() {
 		this.handleStack.push((ModelElement) this.module);
 		this.infoStack.push(this.moduleInfo);
 	}
 
 	public void enterField(FieldInfo fieldInfo) {
 		ModelElement parentHandle = this.handleStack.peek();
 		ModelElementInfo parentInfo = this.infoStack.peek();
 		this.createField(fieldInfo, parentHandle, parentInfo);
 	}
 
 	protected void addChild(ModelElementInfo parentInfo, IModelElement handle) {
 		parentInfo.addChild(handle);
 	}
 
 	private void createField(FieldInfo fieldInfo, ModelElement parentHandle,
 			ModelElementInfo parentInfo) {
 		ModelManager manager = ModelManager.getModelManager();
 
 		BinaryField handle = new BinaryField(parentHandle, manager
 				.intern(fieldInfo.name));
 		BinaryFieldElementInfo handleInfo = new BinaryFieldElementInfo();
 		handleInfo.setFlags(fieldInfo.modifiers);
 		handleInfo.setType(fieldInfo.type);
 		resolveDuplicates(handle);
 		addChild(parentInfo, handle);
 		newElements.put(handle, handleInfo);
 		if (mapper != null) {
 			mapper.reportField(fieldInfo, handle);
 		}
 		this.handleStack.push(handle);
 		this.infoStack.push(handleInfo);
 	}
 
 	public void enterMethodRemoveSame(MethodInfo methodInfo) {
 		this.enterMethod(methodInfo);
 	}
 
 	public void enterMethod(MethodInfo methodInfo) {
 		ModelElement parentHandle = this.handleStack.peek();
 		ModelElementInfo parentInfo = this.infoStack.peek();
 		this.processMethod(methodInfo, parentHandle, parentInfo);
 	}
 
 	private void processMethod(MethodInfo methodInfo,
 			ModelElement parentHandle, ModelElementInfo parentInfo) {
 		String nameString = methodInfo.name;
 		ModelManager manager = ModelManager.getModelManager();
 		BinaryMethod handle = new BinaryMethod(parentHandle, manager
 				.intern(nameString));
 		resolveDuplicates(handle);
 
 		String[] parameterNames = methodInfo.parameterNames == null ? EMPTY
 				: methodInfo.parameterNames;
 		BinaryMethodElementInfo handleInfo = new BinaryMethodElementInfo();
 		if (parameterNames.length == 0) {
 			handleInfo.setArguments(SourceMethodUtils.NO_PARAMETERS);
 		} else {
 			final MethodParameterInfo[] params = new MethodParameterInfo[parameterNames.length];
 			for (int i = 0; i < parameterNames.length; ++i) {
 				String type = null;
 				String defaultValue = null;
 				if (methodInfo.parameterTypes != null
 						&& i < methodInfo.parameterTypes.length) {
 					type = methodInfo.parameterTypes[i];
 					if (type != null) {
 						type = manager.intern(type);
 					}
 				}
 				if (methodInfo.parameterInitializers != null
 						&& i < methodInfo.parameterInitializers.length) {
 					defaultValue = methodInfo.parameterInitializers[i];
 					if (defaultValue != null) {
 						defaultValue = manager.intern(defaultValue);
 					}
 				}
 				params[i] = new MethodParameterInfo(manager
 						.intern(parameterNames[i]), type, defaultValue);
 			}
 			handleInfo.setArguments(params);
 		}
		handleInfo.setIsConstructor(methodInfo.isConstructor);
		handleInfo.setFlags(methodInfo.modifiers);
		handleInfo.setReturnType(methodInfo.returnType);
 
 		addChild(parentInfo, handle);
 		newElements.put(handle, handleInfo);
 		if (mapper != null) {
 			mapper.reportMethod(methodInfo, handle);
 		}
 
 		this.handleStack.push(handle);
 		this.infoStack.push(handleInfo);
 	}
 
 	public void enterType(TypeInfo typeInfo) {
 		ModelElement parentHandle = this.handleStack.peek();
 		ModelElementInfo parentInfo = this.infoStack.peek();
 		this.processType(typeInfo, parentHandle, parentInfo);
 	}
 
 	public boolean enterTypeAppend(String fullName, String delimiter) {
 		return false;
 	}
 
 	private void processType(TypeInfo typeInfo, ModelElement parentHandle,
 			ModelElementInfo parentInfo) {
 		String nameString = typeInfo.name;
 		BinaryType handle = new BinaryType(parentHandle, nameString);
 		BinaryTypeElementInfo handleInfo = new BinaryTypeElementInfo();
 		handleInfo.setFlags(typeInfo.modifiers);
 		resolveDuplicates(handle);
 
 		ModelManager manager = ModelManager.getModelManager();
 		String[] superclasses = typeInfo.superclasses;
 		for (int i = 0, length = superclasses == null ? 0 : superclasses.length; i < length; i++) {
 			superclasses[i] = manager.intern(superclasses[i]);
 		}
 		handleInfo.setSuperclassNames(superclasses);
 		newElements.put(handle, handleInfo);
 		addChild(parentInfo, handle);
 		if (mapper != null) {
 			mapper.reportType(typeInfo, handle);
 		}
 		this.handleStack.push(handle);
 		this.infoStack.push(handleInfo);
 	}
 
 	public void exitModule(int declarationEnd) {
 		// determine if there were any parsing errors
 		this.moduleInfo.setIsStructureKnown(true);
 	}
 
 	public void exitModuleRoot() {
 		this.handleStack.pop();
 		this.infoStack.pop();
 	}
 
 	public void exitField(int declarationEnd) {
 		this.exitMember(declarationEnd);
 	}
 
 	public void exitMethod(int declarationEnd) {
 		this.exitMember(declarationEnd);
 	}
 
 	public void exitType(int declarationEnd) {
 		this.exitMember(declarationEnd);
 	}
 
 	protected void exitMember(int declarationEnd) {
 		ModelElement element = this.handleStack.pop();
 		ModelElementInfo elementInfo = this.infoStack.pop();
 		if (mapper != null) {
 			mapper.setRangeEnd(element, declarationEnd);
 		}
 	}
 
 	public void acceptPackage(int declarationStart, int declarationEnd,
 			String name) {
 		ModelElement parentHandle = this.handleStack.peek();
 		ModelElementInfo parentInfo = this.infoStack.peek();
 		BinaryPackageDeclaration handle = new BinaryPackageDeclaration(
 				parentHandle, name);
 		BinaryPackageDeclarationElementInfo info = new BinaryPackageDeclarationElementInfo();
 		addChild(parentInfo, handle);
 		newElements.put(handle, info);
 	}
 
 	public void acceptFieldReference(String fieldName, int BinaryPosition) {
 	}
 
 	public void acceptMethodReference(String methodName, int argCount,
 			int BinaryPosition, int BinaryEndPosition) {
 	}
 
 	public void acceptTypeReference(String typeName, int BinaryPosition) {
 	}
 
 	public void acceptImport(ImportInfo importInfo) {
 
 	}
 
 	private void resolveDuplicates(SourceRefElement handle) {
 		counters.resolveDuplicates(handle);
 		Assert.isTrue(!this.newElements.containsKey(handle));
 	}
 }
