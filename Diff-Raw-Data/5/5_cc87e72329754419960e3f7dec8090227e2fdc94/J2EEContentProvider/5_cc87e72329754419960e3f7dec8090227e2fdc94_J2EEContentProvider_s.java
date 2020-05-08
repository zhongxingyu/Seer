 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.navigator.internal;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jst.j2ee.internal.ejb.provider.BeanClassProviderHelper;
 import org.eclipse.jst.j2ee.internal.provider.MethodsProviderDelegate;
 import org.eclipse.jst.j2ee.navigator.internal.EMFRootObjectProvider.IRefreshHandlerListener;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.navigator.CommonViewer;
 import org.eclipse.ui.progress.UIJob;
 import org.eclipse.wst.common.internal.emfworkbench.integration.DynamicAdapterFactory;
 
 /**
  * <p>
  * The following class is experimental until fully documented.
  * </p>
  */
 public class J2EEContentProvider implements ITreeContentProvider, IRefreshHandlerListener {
 
 	private static final Class IPROJECT_CLASS = IProject.class;
 
 	private final EMFRootObjectProvider rootObjectProvider;
 
 	private AdapterFactoryContentProvider delegateContentProvider;
 	private MethodsProviderDelegate delegateMethodsProvider;
 
 	private String viewerId = null;
 	Viewer viewer;
 
 	/**
 	 *  
 	 */
 	public J2EEContentProvider() {
 		rootObjectProvider = new EMFRootObjectProvider();
 		rootObjectProvider.addRefreshHandlerListener(this);
 	}
 
 	/**
 	 *  
 	 */
 	public J2EEContentProvider(String aViewerId) {
 		rootObjectProvider = new EMFRootObjectProvider();
 		updateContentProviders(aViewerId);
 		rootObjectProvider.addRefreshHandlerListener(this);
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 	 */
 	public Object[] getElements(Object anInputElement) {
 		return getChildren(anInputElement);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider#getChildren(java.lang.Object)
 	 */
 	public Object[] getChildren(Object aParentElement) {
 		IProject project = null;
 		List children = new ArrayList();
 		if (aParentElement instanceof IProject || aParentElement instanceof IJavaProject) {
 			project = (IProject) ((IAdaptable) aParentElement).getAdapter(IPROJECT_CLASS);
 			if (project != null) {
 				Object[] rootObjects = (rootObjectProvider != null) ? rootObjectProvider.getModels(project) : null;
 				if (rootObjects != null) {
 					for (int x=0; x< rootObjects.length ; ++x) {
 						children.add(rootObjects[x]);
 					}
 					
 				}
 			}
 		} else if (MethodsProviderDelegate.providesContentFor(aParentElement))
 			return delegateMethodsProvider.getChildren(aParentElement);
 		else /* if (isEMFEditObject(aParentElement)) */{
 			Object[] siblings = delegateContentProvider.getChildren(aParentElement);
 			if (siblings != null)
 				children.addAll(Arrays.asList(siblings));
 		}
 		return children.toArray();
 	}
 
 	public Object getParent(Object object) {
 		if (MethodsProviderDelegate.providesContentFor(object))
 			return delegateMethodsProvider.getParent(object);
 		Object parent = delegateContentProvider.getParent(object);
 		if (parent == null && object instanceof BeanClassProviderHelper)
 			parent = ((BeanClassProviderHelper) object).getEjb();
 		if (parent == null && object instanceof EObject)
 			parent = ProjectUtilities.getProject((EObject) object);
 		return parent;
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		rootObjectProvider.removeRefreshHandlerListener(this);
 		delegateContentProvider.dispose();
 		rootObjectProvider.dispose();
 		delegateMethodsProvider.dispose();
 
 	}
 
 	/*
 	 * @see ITreeContentProvider#hasChildren(Object)
 	 */
 	public boolean hasChildren(Object element) {
 		if (MethodsProviderDelegate.providesContentFor(element))
 			return delegateMethodsProvider.hasChildren(element);
 		/* else if (isEMFEditObject(element)) */
 		return delegateContentProvider.hasChildren(element);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer aViewer, Object anOldInput, Object aNewInput) {
 		String newViewerId = null;
 		viewer = aViewer;
 		if (aViewer instanceof CommonViewer)
 			newViewerId = ((CommonViewer) aViewer).getNavigatorContentService().getViewerId();
 
 		if (newViewerId != null && (viewerId == null || !viewerId.equals(newViewerId)))
 			updateContentProviders(newViewerId);
 
 		delegateContentProvider.inputChanged(aViewer, anOldInput, aNewInput);
 		delegateMethodsProvider.inputChanged(aViewer, anOldInput, aNewInput);
 	}
 
 	/**
 	 * @param viewerId2
 	 */
 	private void updateContentProviders(String aViewerId) {
 
 		/* Dispose of the existing content providers */
 		if (delegateContentProvider != null)
 			delegateContentProvider.dispose();
 		if (delegateMethodsProvider != null)
 			delegateMethodsProvider.dispose();
 
 		/* Create new content providers using the new viewer id */
 		DynamicAdapterFactory adapterFactory = new DynamicAdapterFactory(aViewerId);
 		delegateContentProvider = new AdapterFactoryContentProvider(adapterFactory);
 		delegateMethodsProvider = new MethodsProviderDelegate(adapterFactory);
 
 		/* Remember the viewer id */
 		viewerId = aViewerId;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.navigator.internal.EMFRootObjectManager.IRefreshHandlerListener#onRefresh(java.lang.Object)
 	 */
 	public void onRefresh(final Object element) {
 		if (viewer instanceof AbstractTreeViewer) {
 			if (Display.getCurrent() != null) {
 				((AbstractTreeViewer) viewer).refresh(element, true);
 			} else {
 				/* Create and schedule a UI Job to update the Navigator Content Viewer */
 				Job job = new UIJob("Update the Navigator Content Viewer Job") { //$NON-NLS-1$
 					public IStatus runInUIThread(IProgressMonitor monitor) {
 						((AbstractTreeViewer) viewer).refresh(element, true);
 						return Status.OK_STATUS;
 					}
 				};
 				ISchedulingRule rule = new ISchedulingRule() {
 					public boolean contains(ISchedulingRule rule) {
						return false;
 					}
 					public boolean isConflicting(ISchedulingRule rule) {
						return false;
 					}
 				};
 				if (rule != null) {
 					job.setRule(rule);
 				}
 				job.schedule();
 			}
 		}
 	}
 
 }
