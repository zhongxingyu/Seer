 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.typehierarchy;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ITypeHierarchy;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.ScriptModelUtil;
 import org.eclipse.dltk.internal.core.util.MethodOverrideTester;
 import org.eclipse.dltk.internal.ui.IWorkingCopyProvider;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 
 /**
  * Base class for content providers for type hierarchy viewers. Implementors
  * must override 'getTypesInHierarchy'. Java delta processing is also performed
  * by the content provider
  */
 public abstract class TypeHierarchyContentProvider implements
 		ITreeContentProvider, IWorkingCopyProvider {
 	protected static final Object[] NO_ELEMENTS = new Object[0];
 
 	protected TypeHierarchyLifeCycle fTypeHierarchy;
 	protected IMember[] fMemberFilter;
 
 	protected TreeViewer fViewer;
 
 	private ViewerFilter fWorkingSetFilter;
 	private MethodOverrideTester fMethodOverrideTester;
 	private ITypeHierarchyLifeCycleListener fTypeHierarchyLifeCycleListener;
 
 	public TypeHierarchyContentProvider(TypeHierarchyLifeCycle lifecycle) {
 		fTypeHierarchy = lifecycle;
 		fMemberFilter = null;
 		fWorkingSetFilter = null;
 		fMethodOverrideTester = null;
 		fTypeHierarchyLifeCycleListener = new ITypeHierarchyLifeCycleListener() {
 			public void typeHierarchyChanged(
 					TypeHierarchyLifeCycle typeHierarchyProvider,
 					IType[] changedTypes) {
 				if (changedTypes == null) {
 					fMethodOverrideTester = null;
 				}
 			}
 		};
 		lifecycle.addChangedListener(fTypeHierarchyLifeCycleListener);
 	}
 
 	/**
 	 * Sets members to filter the hierarchy for. Set to <code>null</code> to
 	 * disable member filtering. When member filtering is enabled, the hierarchy
 	 * contains only types that contain an implementation of one of the filter
 	 * members and the members themself. The hierarchy can be empty as well.
 	 */
 	public final void setMemberFilter(IMember[] memberFilter) {
 		fMemberFilter = memberFilter;
 	}
 
 	private boolean initializeMethodOverrideTester(IMethod filterMethod,
 			IType typeToFindIn) {
 		IType filterType = filterMethod.getDeclaringType();
 		ITypeHierarchy hierarchy = fTypeHierarchy.getHierarchy();
 
 		boolean filterOverrides = ScriptModelUtil.isSuperType(hierarchy,
 				typeToFindIn, filterType);
 		IType focusType = filterOverrides ? filterType : typeToFindIn;
 
 		if (fMethodOverrideTester == null
 				|| !fMethodOverrideTester.getFocusType().equals(focusType)) {
 			fMethodOverrideTester = new MethodOverrideTester(focusType,
 					hierarchy);
 		}
 		return filterOverrides;
 	}
 
 	private void addCompatibleMethods(IMethod filterMethod, IType typeToFindIn,
 			Collection children) throws ModelException {
 		boolean filterMethodOverrides = initializeMethodOverrideTester(
 				filterMethod, typeToFindIn);
 		IMethod[] methods = typeToFindIn.getMethods();
 		for (int i = 0; i < methods.length; i++) {
 			IMethod curr = methods[i];
 			if (isCompatibleMethod(filterMethod, curr, filterMethodOverrides)
 					&& !children.contains(curr)) {
 				children.add(curr);
 			}
 		}
 	}
 
 	private boolean hasCompatibleMethod(IMethod filterMethod, IType typeToFindIn)
 			throws ModelException {
 		boolean filterMethodOverrides = initializeMethodOverrideTester(
 				filterMethod, typeToFindIn);
 		IMethod[] methods = typeToFindIn.getMethods();
 		for (int i = 0; i < methods.length; i++) {
 			if (isCompatibleMethod(filterMethod, methods[i],
 					filterMethodOverrides)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isCompatibleMethod(IMethod filterMethod, IMethod method,
 			boolean filterOverrides) throws ModelException {
		return false;
 	}
 
 	/**
 	 * The members to filter or <code>null</code> if member filtering is
 	 * disabled.
 	 */
 	public IMember[] getMemberFilter() {
 		return fMemberFilter;
 	}
 
 	/**
 	 * Sets a filter representing a working set or <code>null</code> if working
 	 * sets are disabled.
 	 */
 	public void setWorkingSetFilter(ViewerFilter filter) {
 		fWorkingSetFilter = filter;
 	}
 
 	protected final ITypeHierarchy getHierarchy() {
 		return fTypeHierarchy.getHierarchy();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IReconciled#providesWorkingCopies()
 	 */
 	public boolean providesWorkingCopies() {
 		return true;
 	}
 
 	/*
 	 * Called for the root element
 	 * 
 	 * @see IStructuredContentProvider#getElements
 	 */
 	public Object[] getElements(Object parent) {
 		ArrayList types = new ArrayList();
 		getRootTypes(types);
 		for (int i = types.size() - 1; i >= 0; i--) {
 			IType curr = (IType) types.get(i);
 			try {
 				if (!isInTree(curr)) {
 					types.remove(i);
 				}
 			} catch (ModelException e) {
 				// ignore
 			}
 		}
 		compactTypes(types);
 		return types.toArray();
 	}
 
 	protected void compactTypes(Collection types) {
 		final Map map = new HashMap();
 		for (Iterator i = types.iterator(); i.hasNext();) {
 			final Object item = i.next();
 			if (item instanceof IType) {
 				final IType type = (IType) item;
 				final String qName = type.getTypeQualifiedName();
 				Object value = map.get(qName);
 				if (value == null) {
 					map.put(qName, type);
 				} else if (value instanceof List) {
 					((List) value).add(type);
 				} else {
 					List list = new ArrayList(4);
 					list.add(value);
 					list.add(type);
 					map.put(qName, list);
 				}
 				i.remove();
 			}
 		}
 		final List qNames = new ArrayList(map.keySet());
 		Collections.sort(qNames);
 		for (Iterator i = qNames.iterator(); i.hasNext();) {
 			final String qName = (String) i.next();
 			final Object value = map.get(qName);
 			if (value instanceof List) {
 				final List list = (List) value;
 				types.add(new CumulativeType(qName, (IType[]) list
 						.toArray(new IType[list.size()])));
 			} else {
 				types.add(value);
 			}
 		}
 	}
 
 	protected void getRootTypes(List res) {
 		ITypeHierarchy hierarchy = getHierarchy();
 		if (hierarchy != null) {
 			IType input = hierarchy.getType();
 			if (input != null) {
 				res.add(input);
 			}
 			// opened on a region: dont show
 		}
 	}
 
 	/**
 	 * Hook to overwrite. Filter will be applied on the returned types
 	 */
 	protected abstract void getTypesInHierarchy(IType type, List res);
 
 	/**
 	 * Hook to overwrite. Return null if parent is ambiguous.
 	 */
 	protected abstract IType[] getParentType(IType type);
 
 	private boolean isInScope(IType type) {
 		if (fWorkingSetFilter != null
 				&& !fWorkingSetFilter.select(null, null, type)) {
 			return false;
 		}
 
 		IModelElement input = fTypeHierarchy.getInputElement();
 		int inputType = input.getElementType();
 		if (inputType == IModelElement.TYPE) {
 			return true;
 		}
 
 		IModelElement parent = type.getAncestor(input.getElementType());
 		if (inputType == IModelElement.PROJECT_FRAGMENT) {
 			if (parent == null
 					|| parent.getElementName().equals(input.getElementName())) {
 				return true;
 			}
 		} else if (input.equals(parent)) {
 			return true;
 		}
 		return false;
 	}
 
 	/*
 	 * Called for the tree children.
 	 * 
 	 * @see ITreeContentProvider#getChildren
 	 */
 	public Object[] getChildren(Object element) {
 		if (element instanceof IType) {
 			try {
 				IType type = (IType) element;
 
 				Set children = new HashSet();
 				if (fMemberFilter != null) {
 					addFilteredMemberChildren(type, children);
 				}
 
 				addTypeChildren(type, children);
 				compactTypes(children);
 				return children.toArray();
 			} catch (ModelException e) {
 				// ignore
 			}
 		} else if (element instanceof CumulativeType) {
 			try {
 				final CumulativeType cType = (CumulativeType) element;
 				final IType[] types = cType.getTypes();
 				final Set children = new HashSet();
 				for (int i = 0; i < types.length; ++i) {
 					if (fMemberFilter != null) {
 						addFilteredMemberChildren(types[i], children);
 					}
 					addTypeChildren(types[i], children);
 				}
 				compactTypes(children);
 
 				List result = new ArrayList(children.size());
 				result.addAll(children);
 				cType.insertTo(result, 0);
 				return result.toArray();
 			} catch (ModelException e) {
 				// ignore
 			}
 		}
 		return NO_ELEMENTS;
 	}
 
 	/*
 	 * @see ITreeContentProvider#hasChildren
 	 */
 	public boolean hasChildren(Object element) {
 		if (element instanceof IType) {
 			try {
 				IType type = (IType) element;
 				return hasTypeChildren(type)
 						|| (fMemberFilter != null && hasMemberFilterChildren(type));
 			} catch (ModelException e) {
 				return false;
 			}
 		} else if (element instanceof CumulativeType) {
 			return true;
 		}
 		return false;
 	}
 
 	private void addFilteredMemberChildren(IType parent, Collection children)
 			throws ModelException {
 		for (int i = 0; i < fMemberFilter.length; i++) {
 			IMember member = fMemberFilter[i];
 			if (parent.equals(member.getDeclaringType())) {
 				if (!children.contains(member)) {
 					children.add(member);
 				}
 			} else if (member instanceof IMethod) {
 				addCompatibleMethods((IMethod) member, parent, children);
 			}
 		}
 	}
 
 	private void addTypeChildren(IType type, Collection children)
 			throws ModelException {
 		ArrayList types = new ArrayList();
 		getTypesInHierarchy(type, types);
 		int len = types.size();
 		for (int i = 0; i < len; i++) {
 			IType curr = (IType) types.get(i);
 			if (isInTree(curr)) {
 				children.add(curr);
 			}
 		}
 	}
 
 	protected final boolean isInTree(IType type) throws ModelException {
 		if (isInScope(type)) {
 			if (fMemberFilter != null) {
 				return hasMemberFilterChildren(type) || hasTypeChildren(type);
 			} else {
 				return true;
 			}
 		}
 		return hasTypeChildren(type);
 	}
 
 	private boolean hasMemberFilterChildren(IType type) throws ModelException {
 		for (int i = 0; i < fMemberFilter.length; i++) {
 			IMember member = fMemberFilter[i];
 			if (type.equals(member.getDeclaringType())) {
 				return true;
 			} else if (member instanceof IMethod) {
 				if (hasCompatibleMethod((IMethod) member, type)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean hasTypeChildren(IType type) throws ModelException {
 		ArrayList types = new ArrayList();
 		getTypesInHierarchy(type, types);
 		int len = types.size();
 		for (int i = 0; i < len; i++) {
 			IType curr = (IType) types.get(i);
 			if (isInTree(curr)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/*
 	 * @see IContentProvider#inputChanged
 	 */
 	public void inputChanged(Viewer part, Object oldInput, Object newInput) {
 		Assert.isTrue(part instanceof TreeViewer);
 		fViewer = (TreeViewer) part;
 	}
 
 	public void resetState() {
 	}
 
 	/*
 	 * @see IContentProvider#dispose
 	 */
 	public void dispose() {
 		fTypeHierarchy.removeChangedListener(fTypeHierarchyLifeCycleListener);
 
 	}
 
 	/*
 	 * @see ITreeContentProvider#getParent
 	 */
 	public Object getParent(Object element) {
 		if (element instanceof IMember) {
 			IMember member = (IMember) element;
 			if (member.getElementType() == IModelElement.TYPE) {
 				return getParentType((IType) member);
 			}
 			return member.getDeclaringType();
 		} else if (element instanceof CumulativeType.Part) {
 			return ((CumulativeType.Part) element).getParent();
 		}
 		return null;
 	}
 
 }
