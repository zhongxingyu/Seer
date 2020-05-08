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
 package org.eclipse.jst.j2ee.internal.provider;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.edit.provider.ItemProvider;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.common.componentcore.util.ComponentUtilities;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 /**
  * @author jsholl
  * 
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates. To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 public class J2EEUtilityJarItemProvider extends J2EEItemProvider {
 
 	public final static String UTILITY_JARS = J2EEUIMessages.getResourceString("Utility_JARs_UI_"); //$NON-NLS-1$
 
 	private boolean childrenLoaded = false;
 	private Application application = null;
 
 	/**
 	 * Constructor for J2EEUtilityJarItemProvider.
 	 */
 	public J2EEUtilityJarItemProvider(Application app, AdapterFactory adapterFactory, Object parent) {
 		super(adapterFactory);
 		setParent(parent);
 		application = app;
 		UtilityJarResourceChangeListener.INSTANCE.addUtilityJarItemProvider(ProjectUtilities.getProject(application), this);
 	}
 
 	public boolean hasChildren(Object object) {
 		getChildren(object);
 		return !children.isEmpty();
 	}
 
 	public Collection getChildren(final Object object) {
 		if (!childrenLoaded) {
 			try {
 				disableNotification();
 				org.eclipse.swt.custom.BusyIndicator.showWhile(null, new Runnable() {
 					public void run() {
 						computeChildren();
 					}
 				});
 			} finally {
 				enableNotification();
 			}
 		}
 		return children;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.edit.provider.ItemProvider#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 		UtilityJarResourceChangeListener.INSTANCE.removeUtilityJarItemProvider(ProjectUtilities.getProject(application), this);
 
 	}
 
 	/**
 	 * @see ItemProvider#getChildren(Object)
 	 */
 	private void computeChildren() {
 		childrenLoaded = true;
 		IVirtualComponent ear = ComponentUtilities.findComponent(application);
 		if (ear!=null) {
 			IVirtualReference[] modules = ear.getReferences();
 			for (int i=0; i<modules.length; i++) {
 				IVirtualComponent module = modules[i].getReferencedComponent();
				if (module.getProject() == null || !module.getProject().isAccessible())
					continue;
 				// return only jars for utility components
 				if (IModuleConstants.JST_UTILITY_MODULE.equals(module.getComponentTypeId())) {
 					IProject project = ProjectUtilities.getProject(application);
 					if (project == null)
 						continue;
 					// we will assume the component name is in synch with the module uri
 					IFile utilityJar = project.getFile(module.getName()+".jar"); //$NON-NLS-1$
 					if (utilityJar !=null)
 						children.add(utilityJar);
 				}	
 			}
 		}
 	}
 
 //	private Collection getJars(List list, IResource[] members) {
 //		for (int i = 0; i < members.length; i++) {
 //			if (isJarFile(members[i])) {
 //				list.add(members[i]);
 //			} else if (members[i].getType() == IResource.FOLDER) {
 //				try {
 //					getJars(list, ((IFolder) members[i]).members());
 //				} catch (CoreException e) {
 //					Logger.getLogger().logError(e);
 //				}
 //			}
 //		}
 //		return list;
 //	}
 
 	public static boolean isJarFile(IResource member) {
 		return member.getType() == IResource.FILE && member.getName().toLowerCase().endsWith(".jar"); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see ItemProviderAdapter#getImage(Object)
 	 */
 	public Object getImage(Object object) {
 		return J2EEPlugin.getPlugin().getImage("folder"); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see ItemProviderAdapter#getText(Object)
 	 */
 	public String getText(Object object) {
 		return UTILITY_JARS;
 	}
 
 	// assume this resource is a jar resource
 	public void utilityJarChanged(IResource resource, IResourceDelta delta) {
 		if (childrenLoaded) {
 			if (delta.getKind() == IResourceDelta.ADDED && !children.contains(resource)) {
 				children.add(resource);
 			} else if (delta.getKind() == IResourceDelta.REMOVED && children.contains(resource)) {
 				children.remove(resource);
 			}
 		}
 	}
 
 	protected static class UtilityJarResourceChangeListener implements IResourceChangeListener, IResourceDeltaVisitor {
 
 		protected static final UtilityJarResourceChangeListener INSTANCE = new UtilityJarResourceChangeListener();
 
 		private boolean listening = false;
 		private Map earProjectsToUtilityJarProviderMap;
 
 		public void addUtilityJarItemProvider(IProject project, J2EEUtilityJarItemProvider utilityJarItemProvider) {
 			List providers = getProviders(project);
 			if (providers != null)
 				providers.add(utilityJarItemProvider);
 			if (!listening) {
 				ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 				listening = true;
 			}
 		}
 
 		/**
 		 * @param project
 		 * @return
 		 */
 		private List getProviders(IProject project) {
 			List result = (List) getEarProjectsToUtilityJarProviderMap().get(project);
 			if (result == null && project != null)
 				getEarProjectsToUtilityJarProviderMap().put(project, (result = new ArrayList()));
 			return result;
 		}
 
 		/**
 		 * @return
 		 */
 		private Map getEarProjectsToUtilityJarProviderMap() {
 			if (earProjectsToUtilityJarProviderMap == null)
 				earProjectsToUtilityJarProviderMap = new HashMap();
 			return earProjectsToUtilityJarProviderMap;
 		}
 
 		public void removeUtilityJarItemProvider(IProject project, J2EEUtilityJarItemProvider utilityJarItemProvider) {
 			List providers = getProviders(project);
 			providers.remove(utilityJarItemProvider);
 			if (providers.isEmpty())
 				getEarProjectsToUtilityJarProviderMap().remove(project);
 
 			if (getEarProjectsToUtilityJarProviderMap().isEmpty()) {
 				ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 				listening = false;
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 		 */
 		public void resourceChanged(IResourceChangeEvent event) {
 			try {
 				event.getDelta().accept(this);
 			} catch (CoreException e) {
 				e.printStackTrace();
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 		 */
 		public boolean visit(IResourceDelta delta) {
 			switch (delta.getResource().getType()) {
 				case IResource.ROOT :
 				case IResource.FOLDER :
 					return true;
 
 				case IResource.PROJECT :
 					return getEarProjectsToUtilityJarProviderMap().containsKey(delta.getResource());
 				case IResource.FILE : {
 					IResource resource = delta.getResource();
 					if (isJarFile(resource)) {
 						List utilityJarItemProviders = getProviders(resource.getProject());
 						for (int i = 0; i < utilityJarItemProviders.size(); i++)
 							((J2EEUtilityJarItemProvider) utilityJarItemProviders.get(i)).utilityJarChanged(resource, delta);
 					}
 					return false;
 				}
 
 			}
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		if (adapter == IRESOURCE_CLASS || adapter == IPROJECT_CLASS)
 			return (application != null) ? ProjectUtilities.getProject(application) : null;
 		return null;
 	}
 
 }
