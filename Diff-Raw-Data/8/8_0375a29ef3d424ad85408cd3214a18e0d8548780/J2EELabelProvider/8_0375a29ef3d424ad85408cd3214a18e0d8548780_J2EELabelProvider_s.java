 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.navigator.internal;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jst.j2ee.internal.common.util.CommonUtil;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.common.internal.emfworkbench.integration.DynamicAdapterFactory;
 import org.eclipse.wst.common.navigator.views.ICommonLabelProvider;
 
 import com.ibm.wtp.emf.workbench.ProjectUtilities;
 
 /**
  * <p>
  * The following class is experimental until fully documented.
  * </p>
  */
 public class J2EELabelProvider implements ICommonLabelProvider {
 
 	private AdapterFactoryLabelProvider delegateLabelProvider;
 
 	/**
 	 *  
 	 */
 	public J2EELabelProvider() {
 		super();
 	}
 
 	/**
 	 *  
 	 */
 	public J2EELabelProvider(String aViewerId) {
 		super();
 		initialize(aViewerId);
 	}
 
 	public void initialize(String aViewerId) {
 		delegateLabelProvider = new AdapterFactoryLabelProvider(new DynamicAdapterFactory(aViewerId));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.extensions.ICommonLabelProvider#getDescription(java.lang.Object)
 	 */
 	public String getDescription(Object anElement) {
 		if (anElement instanceof EObject) {
 			EObject eObj = (EObject) anElement;
 			if (CommonUtil.isDeploymentDescriptorRoot(eObj, true /* include ears */)) {
 				IProject parent = ProjectUtilities.getProject(eObj);
 				String path = new Path(eObj.eResource().getURI().toString()).makeRelative().toString();
 				if (parent == null)
 					return path;
 				int startIndex = path.indexOf(parent.getFullPath().toString());
 				return -1 == startIndex ? path : path.substring(startIndex);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param listener
 	 */
 	public void addListener(ILabelProviderListener listener) {
 		if (delegateLabelProvider != null)
 			delegateLabelProvider.addListener(listener);
 	}
 
 	/**
 	 *  
 	 */
 	public void dispose() {
 		if (delegateLabelProvider != null)
 			delegateLabelProvider.dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj) {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.equals(obj);
 		return false;
 	}
 
 	/**
 	 *  
 	 */
 	public void fireLabelProviderChanged() {
 		if (delegateLabelProvider != null)
 			delegateLabelProvider.fireLabelProviderChanged();
 	}
 
 	/**
 	 * @return
 	 */
 	public AdapterFactory getAdapterFactory() {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.getAdapterFactory();
 		return null;
 	}
 
 	/**
 	 * @param object
 	 * @param columnIndex
 	 * @return
 	 */
 	public Image getColumnImage(Object object, int columnIndex) {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.getColumnImage(object, columnIndex);
 		return null;
 	}
 
 	/**
 	 * @param object
 	 * @param columnIndex
 	 * @return
 	 */
 	public String getColumnText(Object object, int columnIndex) {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.getColumnText(object, columnIndex);
 		return null;
 	}
 
 	/**
 	 * @param element
 	 * @return
 	 */
 	public Image getImage(Object element) {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.getImage(element);
 		return null;
 	}
 
 	/**
 	 * @param element
 	 * @return
 	 */
 	public String getText(Object element) {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.getText(element);
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode() {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.hashCode();
 		return super.hashCode();
 	}
 
 	/**
 	 * @param object
 	 * @param id
 	 * @return
 	 */
 	public boolean isLabelProperty(Object object, String id) {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.isLabelProperty(object, id);
 		return false;
 	}
 
 	/**
 	 * @param notification
 	 */
 	public void notifyChanged(Notification notification) {
 		if (delegateLabelProvider != null)
 			delegateLabelProvider.notifyChanged(notification);
 	}
 
 	/**
 	 * @param listener
 	 */
 	public void removeListener(ILabelProviderListener listener) {
 		if (delegateLabelProvider != null)
 			delegateLabelProvider.removeListener(listener);
 	}
 
 	/**
 	 * @param adapterFactory
 	 */
 	public void setAdapterFactory(AdapterFactory adapterFactory) {
 		if (delegateLabelProvider != null)
 			delegateLabelProvider.setAdapterFactory(adapterFactory);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		if (delegateLabelProvider != null)
 			return delegateLabelProvider.toString();
 		return super.toString();
 	}
 }
