 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.ejb.provider;
 
 
 import java.util.Collection;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.jst.j2ee.ejb.Entity;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEEditorUtility;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wst.common.frameworks.internal.ui.OverlayIcon;
 
 public abstract class J2EEJavaClassProviderHelper implements IAdaptable {
 	private EnterpriseBean ejb;
 	public static final Class IRESOURCE_CLASS = IResource.class;
 
 	/**
 	 * J2EEJavaClassProviderHelper constructor comment.
 	 */
 	public J2EEJavaClassProviderHelper(EnterpriseBean anEJB) {
 		super();
 		setEjb(anEJB);
 	}
 
 	public static void addChildren(Entity ejb, Collection children) {
 		addChildren((EnterpriseBean) ejb, children);
 		if (ejb.getPrimaryKey() != null)
 			children.add(new PrimaryKeyClassProviderHelper(ejb));
 	}
 
 	public static void addChildren(EnterpriseBean ejb, Collection children) {
 
 		if (ejb.getHomeInterface() != null)
 			children.add(new HomeInterfaceProviderHelper(ejb));
 		if (ejb.getRemoteInterface() != null)
 			children.add(new RemoteInterfaceProviderHelper(ejb));
 		if (ejb.getLocalHomeInterface() != null)
 			children.add(new LocalHomeInterfaceProviderHelper(ejb));
 		if (ejb.getLocalInterface() != null)
 			children.add(new LocalInterfaceProviderHelper(ejb));
 		if (ejb.getEjbClass() != null)
 			children.add(new BeanClassProviderHelper(ejb));
 	}
 
 	protected Image createImage() {
 		ImageDescriptor base = J2EEUIPlugin.getDefault().getImageDescriptor("jcu_obj");//$NON-NLS-1$
 		if (base == null)
 			return null;
 		ImageDescriptor overlay = getOverlayDescriptor();
 		if (overlay == null)
 			return base.createImage();
 		return new OverlayIcon(base, new ImageDescriptor[][]{{overlay}}).createImage();
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (7/11/2001 1:47:24 PM)
 	 * 
 	 * @return org.eclipse.jst.j2ee.internal.internal.ejb.EnterpriseBean
 	 */
 	public org.eclipse.jst.j2ee.ejb.EnterpriseBean getEjb() {
 		return ejb;
 	}
 
 	public Image getImage() {
 		return null;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (6/20/2001 10:30:54 PM)
 	 * 
 	 * @return JavaClass
 	 */
 	public abstract JavaClass getJavaClass();
 
 	protected ImageDescriptor getOverlayDescriptor() {
 		return J2EEUIPlugin.getDefault().getImageDescriptor(getOverlayKey());
 	}
 
 	protected abstract String getOverlayKey();
 
 	protected IProject getProject() {
 		return ProjectUtilities.getProject(getJavaClass());
 	}
 
 	public String getStatusLineMessage() {
 		if (getJavaClass() != null)
 			return getTypeString(getJavaClass().getQualifiedName());
 		return ""; //$NON-NLS-1$
 	}
 
 	public String getText() {
 		if (getJavaClass() != null)
 			return getJavaClass().getName();
 		return ""; //$NON-NLS-1$
 	}
 
 	public abstract String getTypeString(String className);
 
 	public void openInEditor() {
 		IProject project = ProjectUtilities.getProject(getJavaClass());
 		try {
 			J2EEEditorUtility.openInEditor(getJavaClass(), project);
 		} catch (Exception cantOpen) {
 			//Ignore
 		}
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (7/11/2001 1:47:24 PM)
 	 * 
 	 * @param newEjb
 	 *            org.eclipse.jst.j2ee.internal.internal.ejb.EnterpriseBean
 	 */
 	public void setEjb(org.eclipse.jst.j2ee.ejb.EnterpriseBean newEjb) {
 		ejb = newEjb;
 	}
 
 	/**
 	 * @see IAdaptable#EcoreUtil.getAdapter(eAdapters(),Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		if (adapter == IRESOURCE_CLASS)
 			return J2EEEditorUtility.getFile(getJavaClass());
 		return null;
 	}
 
 }
