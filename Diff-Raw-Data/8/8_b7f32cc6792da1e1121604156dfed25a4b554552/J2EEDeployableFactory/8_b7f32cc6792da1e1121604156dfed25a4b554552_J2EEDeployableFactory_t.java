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
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
 
 /**
  * J2EE deployable factory superclass.
  */
 public class J2EEDeployableFactory extends ProjectModuleFactoryDelegate {
 
 	public static final String ID = "org.eclipse.jst.j2ee.server"; //$NON-NLS-1$
 	protected HashMap projectModules;
 
 	protected final List moduleDelegates = Collections.synchronizedList(new ArrayList(1));
 
 	private long cachedStamp = -2;
 
 	protected static boolean isFlexibleProject(IProject project) {
 		return ModuleCoreNature.getModuleCoreNature(project) != null;
 	}
 	
 	public J2EEDeployableFactory() {
 		super();
 	}
 
 	protected boolean needsUpdating(IProject project) {
 		if(!initialized)
 			return true;
 		IFile component = project.getFile(IModuleConstants.COMPONENT_FILE_PATH);
 		if (!component.exists())
 			return false;
 		if(component.getModificationStamp() != cachedStamp) {
 			cachedStamp = component.getModificationStamp();
 			return true;
 		}
 		return false;
 	}
 
 	protected IModule[] createModules(IProject project) {
 
 		// List modules = createModules(nature);
 		ModuleCoreNature nature = null;
 		try {
 			nature = (ModuleCoreNature) project.getNature(IModuleConstants.MODULE_NATURE_ID);
 		} catch (CoreException e) {
 			Logger.getLogger().write(e);
 		}
 		List modules = createModules(nature);
 		if (modules == null)
 			return new IModule[0];
 		IModule[] moduleArray = new IModule[modules.size()];
 		modules.toArray(moduleArray);
 		return moduleArray;
 	}
 	
 	protected List createModules(ModuleCoreNature nature) {
 		IProject project = nature.getProject();
 		List modules = new ArrayList(1); 
 		StructureEdit moduleCore = null;
 		try {
 			IVirtualComponent comp = ComponentCore.createComponent(project);
 			modules = createModuleDelegates(comp);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if(moduleCore != null) 
 				moduleCore.dispose();
 		}
 		return modules;
 	}
 	
 	protected boolean isValidModule(IProject project) {
 		try {
 			return isFlexibleProject(project);
 		} catch (Exception e) {
 			//Ignore
 		}
 		return false;
 	}
 
 	public ModuleDelegate getModuleDelegate(IModule module) {
 		if(moduleDelegates.size() == 0)
 			return null;
 		ModuleDelegate[] delegates = (ModuleDelegate[])
 			moduleDelegates.toArray(new ModuleDelegate[moduleDelegates.size()]);
 		for (int i=0; i<delegates.length; i++) {			
 			if (module == delegates[i].getModule())
 				return delegates[i];
 		}
 		return null;
 	}
 	
 	public IModule[] getModules() {
 		cacheModules(false);
 		ArrayList moduleList = new ArrayList();
 		for (Iterator iter = projects.values().iterator(); iter.hasNext();) {
 			IModule[] element = (IModule[]) iter.next();
 			for (int j = 0; j < element.length; j++) {
 				moduleList.add(element[j]);
 			}
 		}
 		IModule[] modules = new IModule[moduleList.size()];
 		moduleList.toArray(modules);
 		return modules;
 
 	}
 	
 	protected List createModuleDelegates(IVirtualComponent component) {
 		J2EEFlexProjDeployable moduleDelegate = null;
 		IModule module = null;
 		List moduleList = new ArrayList();
 		try {
 			moduleDelegate = new J2EEFlexProjDeployable(component.getProject(), ID, component);
			if (moduleDelegate.getType() != "") {
				module = createModule(component.getName(), component.getName(), moduleDelegate.getType(), moduleDelegate.getJ2EESpecificationVersion(), moduleDelegate.getProject());
				moduleList.add(module);
				moduleDelegate.initialize(module);
			} else return null;
 			
 			// adapt(moduleDelegate, (WorkbenchComponent) workBenchModules.get(i));
 		} catch (Exception e) {
 			Logger.getLogger().write(e);
 		} finally {
 			if (module != null) {
 				if (getModuleDelegate(module) == null)
 					moduleDelegates.add(moduleDelegate);
 			}
 		}
 		return moduleList;
 	}
 
 }
