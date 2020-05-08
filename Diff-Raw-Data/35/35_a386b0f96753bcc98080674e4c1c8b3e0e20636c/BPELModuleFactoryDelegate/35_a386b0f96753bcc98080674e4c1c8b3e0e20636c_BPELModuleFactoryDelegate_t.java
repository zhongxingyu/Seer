 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.bpel.runtimes.module;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.util.FacetedProjectUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.internal.ModuleFactory;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
 import org.eclipse.wst.web.internal.deployables.FlatComponentDeployable;
 import org.jboss.tools.bpel.runtimes.IBPELModuleFacetConstants;
 
 public class BPELModuleFactoryDelegate  extends ProjectModuleFactoryDelegate implements IResourceChangeListener {
 	protected Map <IModule, FlatComponentDeployable> moduleDelegates = new HashMap<IModule, FlatComponentDeployable>(5);
 
 	public static final String FACTORY_ID = "org.jboss.tools.bpel.runtimes.module.moduleFactory";
 	public static final String MODULE_TYPE = IBPELModuleFacetConstants.BPEL_MODULE_TYPE;
 	public static BPELModuleFactoryDelegate FACTORY;
 	public static BPELModuleFactoryDelegate factoryInstance() {
 		if( FACTORY == null ) {
 			ensureFactoryLoaded(FACTORY_ID);
 		}
 		return FACTORY;
 	}
 	public static void ensureFactoryLoaded(String factoryId) {
         ModuleFactory[] factories = ServerPlugin.getModuleFactories();
         for( int i = 0; i < factories.length; i++ ) {
                 if( factories[i].getId().equals(factoryId)) {
                         factories[i].getDelegate(new NullProgressMonitor());
                 }
         }
 	}
 	
 	public BPELModuleFactoryDelegate() {
 		super();
 	}
 	@Override
 	public void initialize() {
 		super.initialize();
 		if( getId().equals(FACTORY))
 			FACTORY = this;
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 	}
 	
 	@Override
 	protected IModule[] createModules(IProject project) {
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		if(component != null)
 			return createModuleDelegates(component);
 		return null;
 	}
 
 
 	@Override
 	public ModuleDelegate getModuleDelegate(IModule module) {
 		if (module == null)
 			return null;
 
 		ModuleDelegate md = moduleDelegates.get(module);
 //		if( md == null && ((Module)module).getInternalId().startsWith(BINARY_PREFIX))
 //			return createDelegate(module);
 		
 		if (md == null) {
 			createModules(module.getProject());
 			md = moduleDelegates.get(module);
 		}
 		return md;
 	}
 
 	protected boolean canHandleProject(IProject p) {
 		// https://issues.jboss.org/browse/JBIDE-8533
 		// Added support for deprecated jbt.bpel.facet.core
		return //FacetedProjectUtilities.isProjectOfType(p, IBPELModuleFacetConstants.BPEL_PROJECT_FACET) ||
 			FacetedProjectUtilities.isProjectOfType(p, IBPELModuleFacetConstants.JBT_BPEL_PROJECT_FACET);
 	}
 	
 	protected IModule[] createModuleDelegates(IVirtualComponent component) {
 		if(component == null){
 			return null;
 		}
 		
 		List<IModule> projectModules = new ArrayList<IModule>();
 		try {
 			if (canHandleProject(component.getProject())) {
 				String type = IBPELModuleFacetConstants.BPEL_MODULE_TYPE;
 				String version = IBPELModuleFacetConstants.BPEL20_VERSION;
 				IModule module = createModule(component.getName(), component.getName(), type, version, component.getProject());
 				FlatComponentDeployable moduleDelegate = createModuleDelegate(component.getProject(), component);
 				moduleDelegates.put(module, moduleDelegate);
 				projectModules.add(module);
 			} else {
 				return null;
 			}
 		} catch (Exception e) {
 //			e.printStackTrace();
 //			J2EEPlugin.logError(e);
 		}
 		return projectModules.toArray(new IModule[projectModules.size()]);
 	}
 
 	protected FlatComponentDeployable createModuleDelegate(IProject project, IVirtualComponent component) {
 		return new BPELDeployable(project, component);
 	}
 	
 	/**
 	 * Returns the list of resources that the module should listen to for state
 	 * changes. The paths should be project relative paths. Subclasses can
 	 * override this method to provide the paths.
 	 * 
 	 * @return a possibly empty array of paths
 	 */
 	@Override
 	protected IPath[] getListenerPaths() {
 		return new IPath[] { new Path(".project"), // nature //$NON-NLS-1$
 				new Path(StructureEdit.MODULE_META_FILE_NAME), // component
 				new Path(".settings/org.eclipse.wst.common.project.facet.core.xml") // facets //$NON-NLS-1$
 		};
 	}
 
 	@Override
 	protected void clearCache(IProject project) {
 		super.clearCache(project);
 		List<IModule> modulesToRemove = null;
 		for (Iterator<IModule> iterator = moduleDelegates.keySet().iterator(); iterator.hasNext();) {
 			IModule module = iterator.next();
 			if (module.getProject().equals(project)) {
 				if (modulesToRemove == null) {
 					modulesToRemove = new ArrayList<IModule>();
 				}
 				modulesToRemove.add(module);
 			}
 		}
 		if (modulesToRemove != null) {
 			for (IModule module : modulesToRemove) {
 				moduleDelegates.remove(module);
 			}
 		}
 	}
 
 	public void resourceChanged(IResourceChangeEvent event) {
 		cleanAllDelegates();
 	}
 	
 	protected void cleanAllDelegates() {
 		Iterator<FlatComponentDeployable> i = moduleDelegates.values().iterator();
 		while(i.hasNext()) {
 			i.next().clearCache();
 		}
 		modulesChanged();
 	}
 }
