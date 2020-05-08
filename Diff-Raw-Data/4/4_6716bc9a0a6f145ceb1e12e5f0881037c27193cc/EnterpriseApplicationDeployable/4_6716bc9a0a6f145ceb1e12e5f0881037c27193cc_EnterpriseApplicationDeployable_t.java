 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.internal.earcreation.EAREditModel;
 import org.eclipse.jst.j2ee.internal.earcreation.EARNatureRuntime;
 import org.eclipse.jst.j2ee.internal.earcreation.modulemap.UtilityJARMapping;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EENature;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.core.IJ2EEModule;
 import org.eclipse.jst.server.core.ILooseArchive;
 import org.eclipse.jst.server.core.ILooseArchiveSupport;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
 
 
 
 public class EnterpriseApplicationDeployable extends J2EEDeployable implements IEnterpriseApplication, ILooseArchiveSupport {
 	// cached children
 	protected IJ2EEModule[] containedModules;
 
 	protected ILooseArchive[] containedArchives;
 
 	// cached URIs - deployable to String
 	protected Map containedModuleURIs = new HashMap(4);
 
 	protected Map containedArchiveURIs = new HashMap(4);
 
 	/**
 	 * Constructor for EnterpriseApplicationDeployable.
 	 * 
 	 * @param aNature
 	 * @param aFactoryId
 	 */
 	public EnterpriseApplicationDeployable(J2EENature aNature, String aFactoryId) {
 		super(aNature, aFactoryId);
 
 		update();
 	}
 
 	/**
 	 * @see IEnterpriseApplication#containsLooseModules()
 	 */
 	public boolean containsLooseModules() {
 		return true;
 	}
 
 	/**
 	 * @see IEnterpriseApplication#containsLooseArchives()
 	 */
 	public boolean containsLooseArchives() {
 		return true;
 	}
 
 	/*
 	 * @see IEnterpriseApplication#getModules()
 	 */
 	public IJ2EEModule[] getModules() {
 		return containedModules;
 	}
 
 	/*
 	 * @see IEnterpriseApplication#getVendorSupport()
 	 */
 	public String[] getVendorSupport() {
 		return null;
 	}
 
 	protected EARNatureRuntime getEARNature() {
 		return (EARNatureRuntime) getNature();
 	}
 
 	protected static IModule getModule(J2EENature nature) {
 		IModule dep = nature.getModule();
 		if (dep == null) {
 			ModuleFactoryDelegate fac = getModuleFactory(nature);
 			if (fac != null) {
 				if (fac instanceof J2EEDeployableFactory) {
 					dep = ((J2EEDeployableFactory) fac).getModuleProject(nature.getProject());
 				}
 			}
 		}
 		return dep;
 	}
 
 	protected static ModuleFactoryDelegate getModuleFactory(J2EENature nature) {
 		/*
 		 * Iterator factories = ServerCore.getModuleFactories().iterator(); while
 		 * (factories.hasNext()) { ModuleFactory deployableFactory = (ModuleFactory)
 		 * factories.next(); if
 		 * (!deployableFactory.getId().equals(EnterpriseApplicationDeployableFactory.ID)) {
 		 * ModuleFactoryDelegate deployableFactoryDelegate = deployableFactory.getDelegate(); if
 		 * (deployableFactoryDelegate instanceof J2EEDeployableFactory) { J2EEDeployableFactory fac =
 		 * (J2EEDeployableFactory) deployableFactory.getDelegate(); if
 		 * (fac.getNatureID().equals(nature.getNatureID())) { return fac; } } } }
 		 */
 		return null;
 	}
 
 	protected static LooseArchiveDeployableFactory getLooseArchiveDeployableFactory() {
 		/*
 		 * Iterator factories = ServerCore.getModuleFactories().iterator(); while
 		 * (factories.hasNext()) { ModuleFactory deployableFactory = (ModuleFactory)
 		 * factories.next(); if
 		 * (!deployableFactory.getId().equals(EnterpriseApplicationDeployableFactory.ID)) {
 		 * ModuleFactoryDelegate deployableFactoryDelegate = deployableFactory.getDelegate(); if
 		 * (deployableFactoryDelegate instanceof LooseArchiveDeployableFactory) return
 		 * (LooseArchiveDeployableFactory) deployableFactoryDelegate; } }
 		 */
 		return null;
 	}
 
 	/*
 	 * @see IEnterpriseApplication#getURI(IJ2EEModule)
 	 */
 	public String getURI(IJ2EEModule module) {
 		try {
 			return (String) containedModuleURIs.get(module);
 		} catch (Exception e) {
 			// ignore
 		}
 		return null;
 	}
 
 	public String getURI(ILooseArchive archive) {
 		try {
 			return (String) containedArchiveURIs.get(archive);
 		} catch (Exception e) {
 			// ignore
 		}
 		return null;
 	}
 
 	public ILooseArchive[] getLooseArchives() {
 		return containedArchives;
 	}
 
 	protected ILooseArchive getArchiveDeployable(IProject aProject, LooseArchiveDeployableFactory fact) {
 		try {
 			return (ILooseArchive) fact.getModuleProject(aProject);
 		} catch (RuntimeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	protected void update() {
 		IJ2EEModule[] oldModules = containedModules;
 		containedModules = getContainedModules();
 		ILooseArchive[] oldArchives = containedArchives;
 
 		////////////TODO add back looselib support///////////////////////////
 		//containedArchives = getContainedArchives();
 		containedArchives = new ILooseArchive[0];
 		// get add events
 		List add = new ArrayList(2);
 		addAddedObjects(add, oldModules, containedModules);
 		addAddedObjects(add, oldArchives, containedArchives);
 
 		// get remove events
 		List remove = new ArrayList(2);
 		addRemovedObjects(remove, oldModules, containedModules);
 		addRemovedObjects(remove, oldArchives, containedArchives);
 
 		// fire change events
 		int size = containedModules.length;
 		List change = new ArrayList(size);
 		for (int i = 0; i < size; i++) {
 			String newURI = getContainedURI(containedModules[i]);
 			String oldURI = getURI(containedModules[i]);
 
 			if (oldURI != null && !oldURI.equals(newURI)) {
 				change.add(containedModules[i]);
 			}
 			containedModuleURIs.put(containedModules[i], newURI);
 		}
 
 		size = containedArchives.length;
 		for (int i = 0; i < size; i++) {
 			String newURI = getContainedURI(containedArchives[i]);
 			String oldURI = getURI(containedArchives[i]);
 
 			if (oldURI != null && !oldURI.equals(newURI)) {
 				change.add(containedArchives[i]);
 			}
 			containedArchiveURIs.put(containedArchives[i], newURI);
 		}
 
 		if (!add.isEmpty() || !remove.isEmpty() || !change.isEmpty()) {
 			IModule[] added = new IModule[add.size()];
 			getModulesFromDelegates(add).toArray(added);
 			IModule[] changed = new IModule[change.size()];
 			getModulesFromDelegates(change).toArray(changed);
 			IModule[] removed = new IModule[remove.size()];
 			getModulesFromDelegates(remove).toArray(removed);
			////////////TODO add back looselib support///////////////////////////
			//containedArchives = getContainedArchives();
	//		fireModuleChangeEvent(true, added, changed, removed);
 		}
 	}
 
 	protected List getModulesFromDelegates(List delegates) {
 		List result = new ArrayList();
 		for (int i = 0; i < delegates.size(); i++) {
 			Object delegate = delegates.get(i);
 			if (delegate != null && delegate instanceof J2EEDeployable)
 				result.add(((J2EEDeployable) delegate).getModule());
 		}
 		return result;
 	}
 
 	/**
 	 * Return the objects that have been added between array a and array b. Assumes that there are
 	 * no null objects in the array.
 	 */
 	protected static void addAddedObjects(List list, Object[] a, Object[] b) {
 		if (b == null)
 			return;
 		else if (a == null) {
 			int size = b.length;
 			for (int i = 0; i < size; i++)
 				list.add(b[i]);
 			return;
 		}
 		int size = b.length;
 		for (int i = 0; i < size; i++) {
 			Object obj = b[i];
 			boolean found = false;
 			if (a != null) {
 				int size2 = a.length;
 				for (int j = 0; !found && j < size2; j++) {
 					if (obj.equals(a[j]))
 						found = true;
 				}
 			}
 			if (!found)
 				list.add(obj);
 		}
 	}
 
 	/**
 	 * Return the objects that have been removed between array a and array b. Assumes that there are
 	 * no null objects in the array.
 	 */
 	protected static void addRemovedObjects(List list, Object[] a, Object[] b) {
 		if (a == null)
 			return;
 		else if (b == null) {
 			int size = a.length;
 			for (int i = 0; i < size; i++)
 				list.add(a[i]);
 			return;
 		}
 		int size = a.length;
 		for (int i = 0; i < size; i++) {
 			Object obj = a[i];
 			boolean found = false;
 			if (b != null) {
 				int size2 = b.length;
 				for (int j = 0; !found && j < size2; j++) {
 					if (obj.equals(b[j]))
 						found = true;
 				}
 			}
 			if (!found)
 				list.add(obj);
 		}
 	}
 
 	/**
 	 *  
 	 */
 	protected IJ2EEModule[] getContainedModules() {
 		Collection projects = getEARNature().getModuleProjects().values();
 		List mods = new ArrayList(projects.size());
 		Iterator it = projects.iterator();
 		J2EENature nat = null;
 		while (it.hasNext()) {
 			nat = (J2EENature) it.next();
 			if (nat != null) {
 				Object module = getModule(nat);
 				if (module != null && module instanceof IModule) {
 					Object moduleDelegate = ((IModule) module).getAdapter(ModuleDelegate.class);
 					if (moduleDelegate != null)
 						mods.add(moduleDelegate);
 				}
 			}
 		}
 		IJ2EEModule[] result = new IJ2EEModule[mods.size()];
 		mods.toArray(result);
 		return result;
 	}
 
 	/**
 	 *  
 	 */
 	protected ILooseArchive[] getContainedArchives() {
 		EAREditModel editModel = getEARNature().getEarEditModelForRead(this);
 		try {
 			List maps = editModel.getUtilityJARMappings();
 			if (maps == null)
 				return new ILooseArchive[0];
 
 			LooseArchiveDeployableFactory fact = getLooseArchiveDeployableFactory();
 			List arcs = new ArrayList(maps.size());
 			for (int i = 0; i < maps.size(); i++) {
 				UtilityJARMapping map = (UtilityJARMapping) maps.get(i);
 				IProject proj = null;
 				if (map.getProjectName() != null)
 					proj = J2EEPlugin.getWorkspace().getRoot().getProject(map.getProjectName());
 				if (proj != null && proj.exists()) {
 					ILooseArchive archive = getArchiveDeployable(proj, fact);
 					if (archive != null)
 						arcs.add(archive);
 				}
 			}
 			ILooseArchive[] result = new ILooseArchive[arcs.size()];
 			arcs.toArray(result);
 			return result;
 		} finally {
 			if (editModel != null)
 				editModel.releaseAccess(this);
 		}
 	}
 
 	/*
 	 * @see IEnterpriseApplication#getURI(IJ2EEModule)
 	 */
 	protected String getContainedURI(ILooseArchive deployable) {
 		if (deployable instanceof LooseArchiveDeployable) {
 			LooseArchiveDeployable archive = (LooseArchiveDeployable) deployable;
 			EAREditModel editModel = getEARNature().getEarEditModelForRead(this);
 			try {
 				UtilityJARMapping map = editModel.getUtilityJARMapping(archive.getProject());
 				if (map != null)
 					return map.getUri();
 				return null;
 			} finally {
 				editModel.releaseAccess(this);
 			}
 		}
 
 		return null;
 	}
 
 	protected String getContainedURI(IJ2EEModule deployable) {
 		if (deployable instanceof J2EEDeployable) {
 			IProject aProject = ((J2EEDeployable) deployable).getProject();
 			if (aProject != null) {
 				Module m = getEARNature().getModule(aProject);
 				if (m != null)
 					return m.getUri();
 			}
 		}
 		return null;
 	}
 
 	public String getType() {
 		return "j2ee.ear"; //$NON-NLS-1$
 	}
 
 	public String getVersion() {
 		return getNature().getJ2EEVersionText();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.server.j2ee.IEnterpriseApplication#getLocation()
 	 */
 	public IPath getLocation() {
 		if (getProject() != null && getProject().isAccessible())
 			return getProject().getLocation();
 		return null;
 	}
 
 	/**
 	 * Returns the child modules of this module.
 	 * 
 	 * @return org.eclipse.wst.server.core.model.IModule[]
 	 */
 	public IModule[] getChildModules() {
 		List list = new ArrayList();
 
 		if (containedModules != null) {
 			int size = containedModules.length;
 			for (int i = 0; i < size; i++)
 				list.add(containedModules[i]);
 		}
 		if (containedArchives != null) {
 			int size = containedArchives.length;
 			for (int i = 0; i < size; i++)
 				list.add(containedArchives[i]);
 		}
 
 		IModule[] children = new IModule[list.size()];
 		list.toArray(children);
 		return children;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.server.core.IModule#validate(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public IStatus validate(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
