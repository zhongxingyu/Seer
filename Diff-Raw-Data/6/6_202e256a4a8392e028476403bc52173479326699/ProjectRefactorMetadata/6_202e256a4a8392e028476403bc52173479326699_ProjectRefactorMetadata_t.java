 /*******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * rfrost@bea.com - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.j2ee.refactor.operations;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.internal.resources.VirtualComponent;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerUtil;
 
 /**
  * IDataModel object property stored under the property name 
  * {@link ProjectRefactoringDataModelProvider#PROJECT_METADATA}.
  * 
  *  Note: this class is not multi-thread safe.
  */
 public class ProjectRefactorMetadata {
 	
 	public static int NON_CACHING = 0;
 	public static int REF_CACHING = 1;
 	public static int REFERER_CACHING = 2;
 	
 	private int _virtualCompCaching = NON_CACHING;
 	private final IProject _project;
 	private IServer[] servers;
 	private IVirtualComponent virtualComp = null;
 	private final List dependentMetadata = new ArrayList();
 	private boolean javaNature;
 	private boolean moduleCoreNature;
 	private IModule module;
 	private boolean isEAR = false;
 	private boolean isEJB = false;
 	private boolean isWeb = false;
 	private boolean isAppClient = false;
 	private boolean isConnector = false;
 	private boolean isUtility = false;
 	
 	public ProjectRefactorMetadata(final IProject project) {
 		_project = project;
 	}
 	
 	public ProjectRefactorMetadata(final IProject project, final int virtualCompCaching) {
 		this(project);
 		_virtualCompCaching = virtualCompCaching;
 	}
 	
 	public void computeMetadata() {
 		try {
 			javaNature = _project.hasNature("org.eclipse.jdt.core.javanature"); //$NON-NLS-1$
 			moduleCoreNature = ModuleCoreNature.getModuleCoreNature(_project) != null;
 			if (moduleCoreNature) {
 				if (_virtualCompCaching == REF_CACHING) {
 					virtualComp = new RefCachingVirtualComponent(ComponentCore.createComponent(_project));
 				} else if (_virtualCompCaching == REFERER_CACHING) {
 					virtualComp = new RefererCachingVirtualComponent(ComponentCore.createComponent(_project));
 				} else {
 					virtualComp = ComponentCore.createComponent(_project);			
 				}
 				final IFacetedProject facetedProject = ProjectFacetsManager.create(_project);
 				module = ServerUtil.getModule(_project);
 				isEAR = facetedProject.hasProjectFacet(ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_EAR_MODULE)); 
 				isEJB = facetedProject.hasProjectFacet(ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_EJB_MODULE)); 
 				isWeb = facetedProject.hasProjectFacet(ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_WEB_MODULE));
 				isAppClient = facetedProject.hasProjectFacet(ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_APPCLIENT_MODULE)); 
 				isConnector = facetedProject.hasProjectFacet(ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_CONNECTOR_MODULE));  
 				isUtility = facetedProject.hasProjectFacet(ProjectFacetsManager.getProjectFacet(IModuleConstants.JST_UTILITY_MODULE)); 
 			}
 		} catch (CoreException ce) {
 			Logger.getLogger().logError(ce);
 		} 
 	}
 
 	public void computeServers() {
 		servers = ServerUtil.getServersByModule(module, null);
 	}
 	
 	public IServer[] getServers() {
 		return servers;
 	}
 	
 	public void computeDependentMetadata(final int virtualComponentCaching, final IProject[] dependentProjects) {
 		// get all referencing projects and execute the appropriate update operation for each one
 		ProjectRefactorMetadata metadata;
 		for (int i = 0; i < dependentProjects.length; i++) {
 			final IProject dependentProject = dependentProjects[i];
 			if (dependentProject.exists() && dependentProject.isOpen()) {
 				metadata = new ProjectRefactorMetadata(dependentProjects[i], virtualComponentCaching);
 				metadata.computeMetadata();
 				dependentMetadata.add(metadata);
 			}
 		}
 	}
 	
 	/**
 	 * Retrieves the IProject that represents the referenced project.
 	 */
 	public IProject getProject() {
 		return _project;
 	}
 	
 	/**
 	 * Retrieves the IModule for the project, or null if no IModule representation
 	 * exists.
 	 */
 	public IModule getModule() {
 		return module;
 	}
 	
 	/**
 	 * Retrieves the project name.
 	 */
 	public String getProjectName() {
 		return _project.getName();
 	}
 	
 	/**
 	 * Retrieves all ProjectRefactorMetadata for dependent projects.
 	 * Will be empty if not computed.
 	 */
 	public ProjectRefactorMetadata[] getDependentMetadata() {
 		return (ProjectRefactorMetadata[]) dependentMetadata.toArray(new ProjectRefactorMetadata[dependentMetadata.size()]);
 	}
 	
 	/**
 	 * Returns the IVirtualComponent for the project.
 	 */
 	public IVirtualComponent getVirtualComponent() {
 		return virtualComp;
 	}
 	
 	public boolean hasJavaNature() { 
 		return javaNature;
 	}
 	
 	public boolean hasModuleCoreNature() {
 		return moduleCoreNature;
 	}
 	
 	public boolean isEAR() {
 		return isEAR;
 	}
 
 	public boolean isEJB() {
 		return isEJB; 
 		
 	}
 	
 	public boolean isWeb() {
 		return isWeb;
 	}
 	
 	public boolean isAppClient() {
 		return isAppClient;
 	}
 	
 	public boolean isConnector() {
 		return isConnector;
 	}
 	
 	public boolean isUtility() {
 		return isUtility;
 	}
 	
 	public class CachingVirtualComponent implements IVirtualComponent {
 		protected final IVirtualComponent _comp;
 		protected boolean _caching = true;
 		public CachingVirtualComponent(final IVirtualComponent comp) {
 			_comp = comp;
 		}
 		public void addReferences(IVirtualReference[] references) {
 			_comp.addReferences(references);
 		}
 		public void create(int updateFlags, IProgressMonitor aMonitor) throws CoreException {
 			_comp.create(updateFlags, aMonitor);
 		}
 		public boolean exists() {
 			return _comp.exists();
 		}
 		public IVirtualComponent getComponent() {
 			return _comp.getComponent();
 		}
 		public Properties getMetaProperties() {
 			return _comp.getMetaProperties();
 		}
 		public IPath[] getMetaResources() {
 			return _comp.getMetaResources();
 		}
 		public String getName() {
 			return _comp.getName();
 		}
 		public String getDeployedName() {
 			return _comp.getDeployedName();
 		}
 		public IProject getProject() {
 			return _comp.getProject();
 		}
 		public IVirtualReference getReference(String aComponentName) {
 			return _comp.getReference(aComponentName);
 		}
 		public IVirtualReference[] getReferences() {
 			return _comp.getReferences();
 		}
 		public void setCaching(boolean caching) {
 			_caching = caching;
 		}
 		public IVirtualComponent[] getReferencingComponents() {
 			return _comp.getReferencingComponents();
 		}
 		public IVirtualFolder getRootFolder() {
 			return _comp.getRootFolder();
 		}
 		public boolean isBinary() {
 			return _comp.isBinary();
 		}
 		public void setMetaProperties(Properties properties) {
 			_comp.setMetaProperties(properties);
 		}
 		public void setMetaProperty(String name, String value) {
 			_comp.setMetaProperty(name, value);
 		}
 		public void setMetaResources(IPath[] theMetaResourcePaths) {
 			_comp.setMetaResources(theMetaResourcePaths);
 		}
 		public void setReferences(IVirtualReference[] theReferences) {
 			_comp.setReferences(theReferences);
 		}
 		public Object getAdapter(Class adapter) {
 			return _comp.getAdapter(adapter);
 		}
 		
 		public boolean equals(Object o) {
 			return _comp.equals(o);
 		}
 		
 		public int hashCode() {
 			return _comp.hashCode();
 		}
 		
 		public String toString() {
 			return _comp.toString();
 		}
		
		public void removeReference(IVirtualReference aReference) {
			((VirtualComponent)_comp).removeReference(aReference);
		}
 	}
 	
 	public class RefCachingVirtualComponent extends CachingVirtualComponent {
 		private IVirtualReference[] cachedRefs;
 		public RefCachingVirtualComponent(final IVirtualComponent comp) {
 			super(comp);
 			cachedRefs = comp.getReferences();
 		}
 
 		public IVirtualReference getReference(String aComponentName) {
 			IVirtualReference[] refs = getReferences();
 			for (int i = 0; i < refs.length; i++) {
 				IVirtualReference reference = refs[i];
 				if (reference == null || reference.getReferencedComponent() == null) {
 					return null;
 				}
 				if (reference.getReferencedComponent().getName().equals(aComponentName))
 					return reference;
 			}
 			return null;
 		}
 		public IVirtualReference[] getReferences() {
 			if (_caching) {
 				return cachedRefs;
 			}
 			return super.getReferences();
 		}
 	}
 
 	public class RefererCachingVirtualComponent extends CachingVirtualComponent {
 		private IVirtualComponent[] cachedReferers;
 		public RefererCachingVirtualComponent(final IVirtualComponent comp) {
 			super(comp);
 			cachedReferers = comp.getReferencingComponents();
 		}
 
 		public IVirtualComponent[] getReferencingComponents() {
 			if (_caching) {
 				return cachedReferers;
 			}
 			return _comp.getReferencingComponents();
 		}
 	}
 }
