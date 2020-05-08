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
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.wst.common.componentcore.StructureEdit;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
import org.eclipse.wst.common.componentcore.internal.ProjectComponents;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 import org.eclipse.wst.server.core.util.ProjectModuleFactoryDelegate;
 
 /**
  * J2EE deployable factory superclass.
  */
 public abstract class J2EEDeployableFactory extends ProjectModuleFactoryDelegate {
 
 	protected HashMap projectModules;
 
 	protected ArrayList moduleDelegates = new ArrayList();
 
 	protected static boolean isFlexableProject(IProject project) {
 		return ModuleCoreNature.getModuleCoreNature(project) != null;
 	}
 
 	public J2EEDeployableFactory() {
 		super();
 	}
 
 	/*
 	 * protected void addModuleProject(IProject project) { if (!isFlexableProject(project)) {
 	 * super.addModuleProject(project); return; } ModuleCoreNature nature = null; try { nature =
 	 * (ModuleCoreNature) project.getNature(IModuleConstants.MODULE_NATURE_ID); } catch
 	 * (CoreException e) { Logger.getLogger().write(e); } List modules = createModules(nature); List
 	 * oldModules = (List) getProjectModules().get(project); if (oldModules != null &&
 	 * !oldModules.isEmpty()) addNewModules(modules, oldModules); else
 	 * getProjectModules().put(project, modules); if (added == null) added = new ArrayList(2);
 	 * added.addAll(modules); }
 	 */
 
 	/*    *//**
 			 * @param newModules
 			 */
 	/*
 	 * private void addNewModules(List newModules, List oldModules) { for (int i = 0; i <
 	 * newModules.size(); i++) { if (!oldModules.contains(newModules.get(i)))
 	 * oldModules.add(newModules.get(i)); }
 	 *  }
 	 */
 
 
 
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
 			
 			moduleCore = StructureEdit.getStructureEditForRead(project);
			ProjectComponents projComp = moduleCore.getComponentModelRoot();
			if (projComp == null)
				return modules;
			EList workBenchModules = projComp.getComponents();						 
 			if (workBenchModules.isEmpty())
 				return modules;
 			modules = createModuleDelegates(workBenchModules, project);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if(moduleCore != null) 
 				moduleCore.dispose();
 		}
 		return modules;
 	}
 
 
 	protected abstract List createModuleDelegates(EList workBenchModules, IProject project) throws CoreException; 
 
 	
 	protected boolean isValidModule(IProject project) {
 		try {
 			return isFlexableProject(project);
 		} catch (Exception e) {
 			//Ignore
 		}
 		return false;
 	}
 
 	protected abstract String getNatureID();
 
 	
 /*	protected IModule createModule(IProject project) {
 		try {
 			J2EENature nature = (J2EENature) project.getNature(getNatureID());
 			return createModule(nature);
 		} catch (Exception e) {
 		}
 		return null;
 	}*/
 
 
 	public ModuleDelegate getModuleDelegate(IModule module) {
 		if (moduleDelegates == null)
 			moduleDelegates = new ArrayList(1);
 		for (Iterator iter = moduleDelegates.iterator(); iter.hasNext();) {
 			ModuleDelegate element = (ModuleDelegate) iter.next();
 			if (module == element.getModule())
 				return element;
 		}
 		return null;
 	}
 
 
 
 }
