 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.provider;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.ManagedBeanType;
 import org.eclipse.jst.jsf.facesconfig.ui.section.ManagedBeanScopeTreeItem;
 
 /**
  * Managed bean Content Provider.
  * <p>
  * <b>Provides grouping by Scope.</b>
  * </p>
  * 
  * @author Xiao-guang Zhang, sfshi
  * @version 1.5
  */
 public class ManagedBeanContentProvider implements ITreeContentProvider {
 
 	private List scopeItemList = null;
 
 
 	/**
 	 * The constructor
 	 */
 	public ManagedBeanContentProvider() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object
 	 *      parent)
 	 */
 	public Object[] getChildren(Object parent) {
 
 		if (parent instanceof FacesConfigType) {
 			if (scopeItemList == null) {
 				scopeItemList = new ArrayList();
 				for (int i = 0; i < ManagedBeanScopeTreeItem.scopeItems.length; i++) {
 					ManagedBeanScopeTreeItem scopeTreeItem = new ManagedBeanScopeTreeItem(
 							ManagedBeanScopeTreeItem.scopeItems[i],
 							(FacesConfigType) parent);
 					scopeItemList.add(scopeTreeItem);
 				}
 			}
 			return scopeItemList.toArray();
 		} else if (parent instanceof ManagedBeanScopeTreeItem) {
 			return ((ManagedBeanScopeTreeItem) parent).getChildren().toArray();
 		}
 
 		return new Object[0];
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(Object
 	 *      parent)
 	 */
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
 	 */
 	public Object getParent(Object element) {
 		if (element instanceof ManagedBeanScopeTreeItem) {
 			return ((ManagedBeanScopeTreeItem) element).getParent();
 		} else if (element instanceof ManagedBeanType) {
			String scope = null;
			if (((ManagedBeanType) element).getManagedBeanScope() != null) {
				scope = ((ManagedBeanType) element).getManagedBeanScope()
						.getTextContent();
			}

 			if (scope != null) {
 				for (int i = 0; i < scopeItemList.size(); i++) {
 					if (((ManagedBeanScopeTreeItem) scopeItemList.get(i))
 							.getScope().equals(scope)) {
 						return scopeItemList.get(i);
 					}
 
 				}
 			}
 		}
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
 	 */
 	public boolean hasChildren(Object element) {
 		if (element instanceof FacesConfigType) {
 			return true;
 		} else if (element instanceof ManagedBeanScopeTreeItem) {
 			return ((ManagedBeanScopeTreeItem) element).hasChildren();
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		if (newInput != null) {
 //			initialize(newInput);
 		}
 	}
 
 }
