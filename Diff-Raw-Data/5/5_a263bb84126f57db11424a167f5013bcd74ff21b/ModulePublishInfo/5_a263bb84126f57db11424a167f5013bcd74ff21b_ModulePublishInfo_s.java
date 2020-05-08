 /**********************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.core.internal;
 
 import java.util.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.model.IModuleFile;
 import org.eclipse.wst.server.core.model.IModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.server.core.model.IModuleResourceDelta;
 import org.eclipse.wst.server.core.model.ModuleDelegate;
 /**
  * Publish information for a specific module on a specific server.
  */
 public class ModulePublishInfo {
 	private static final String MODULE_ID = "module-ids";
 	private static final String NAME = "name";
 	private static final String MODULE_TYPE_ID = "module-type-id";
 	private static final String MODULE_TYPE_VERSION = "module-type-version";
 	private static final String PATH = "path";
 	private static final String STAMP = "stamp";
 	private static final String FILE = "file";
 	private static final String FOLDER = "folder";
 
 	private String moduleId;
 	private String name;
 	private IModuleResource[] resources = new IModuleResource[0];
 	private IModuleType moduleType;
 
 	private boolean useCache;
 	private IModuleResource[] currentResources = null;
 	private IModuleResourceDelta[] delta = null;
 	private boolean hasDelta;
 
 	/**
 	 * ModulePublishInfo constructor.
 	 * 
 	 * @param moduleId a module id
 	 * @param name the module's name
 	 * @param moduleType the module type
 	 */
 	public ModulePublishInfo(String moduleId, String name, IModuleType moduleType) {
 		super();
 
 		this.moduleId = moduleId;
 		this.name = name;
 		this.moduleType = moduleType;
 	}
 
 	/**
 	 * ModulePublishInfo constructor.
 	 * 
 	 * @param memento a memento
 	 */
 	public ModulePublishInfo(IMemento memento) {
 		super();
 		
 		load(memento);
 	}
 
 	public String getModuleId() {
 		return moduleId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public IModuleType getModuleType() {
 		return moduleType;
 	}
 
 	public IModuleResource[] getResources() {
 		return resources;
 	}
 
 	public void setResources(IModuleResource[] res) {
 		resources = res;
 	}
 
 	/**
 	 * 
 	 */
 	protected void load(IMemento memento) {
 		Trace.trace(Trace.FINEST, "Loading module publish info for: " + memento);
 	
 		try {
 			moduleId = memento.getString(MODULE_ID);
 			name = memento.getString(NAME);
 			String mt = memento.getString(MODULE_TYPE_ID);
 			String mv = memento.getString(MODULE_TYPE_VERSION);
 			if (mt != null && mt.length() > 0)
 				moduleType = new ModuleType(mt, mv);
 	
 			resources = loadResource(memento);
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Could not load module publish info information: " + e.getMessage());
 		}
 	}
 
 	protected IModuleResource[] loadResource(IMemento memento) {
 		if (memento == null)
 			return new IModuleResource[0];
 		
 		List list = new ArrayList(5);
 		
 		// load files
 		IMemento[] children = memento.getChildren(FILE);
 		if (children != null) {
 			int size = children.length;
 			for (int i = 0; i < size; i++) {
 				String name2 = children[i].getString(NAME);
 				IPath path = new Path(children[i].getString(PATH));
 				long stamp = Long.parseLong(children[i].getString(STAMP));
 				ModuleFile file = new ModuleFile(name2, path, stamp);
 				list.add(file);
 			}
 		}
 		
 		// load folders
 		children = memento.getChildren(FOLDER);
 		if (children != null) {
 			int size = children.length;
 			for (int i = 0; i < size; i++) {
 				String name2 = children[i].getString(NAME);
 				IPath path = new Path(children[i].getString(PATH));
 				ModuleFolder folder = new ModuleFolder(null, name2, path);
 				folder.setMembers(loadResource(children[i]));
 				list.add(folder);
 			}
 		}
 		
 		IModuleResource[] resources2 = new IModuleResource[list.size()];
 		list.toArray(resources2);
 		return resources2;
 	}
 
 	/**
 	 * 
 	 */
 	protected void save(IMemento memento) {
 		try {
 			memento.putString(MODULE_ID, moduleId);
 			if (name != null)
 				memento.putString(NAME, name);
 			
 			if (moduleType != null) {
 				memento.putString(MODULE_TYPE_ID, moduleType.getId());
 				memento.putString(MODULE_TYPE_VERSION, moduleType.getVersion());
 			}
 			
 			saveResource(memento, resources);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not save module publish info", e);
 		}
 	}
 
 	protected void saveResource(IMemento memento, IModuleResource[] resources2) {
 		if (resources2 == null)
 			return;
 		int size = resources2.length;
 		for (int i = 0; i < size; i++) {
 			if (resources2[i] instanceof IModuleFile) {
 				IModuleFile file = (IModuleFile) resources2[i];
 				IMemento child = memento.createChild(FILE);
 				child.putString(NAME, file.getName());
 				child.putString(PATH, file.getModuleRelativePath().toPortableString());
 				child.putString(STAMP, "" + file.getModificationStamp());
 			} else {
 				IModuleFolder folder = (IModuleFolder) resources2[i];
 				IMemento child = memento.createChild(FOLDER);
 				child.putString(NAME, folder.getName());
 				child.putString(PATH, folder.getModuleRelativePath().toPortableString());
 				IModuleResource[] resources3 = folder.members();
 				saveResource(child, resources3);
 			}
 		}
 	}
 
 	/**
 	 * Start using the module cache.
 	 */
 	protected void startCaching() {
 		useCache = true;
 		currentResources = null;
 	}
 
 	/**
 	 * Fill the module cache.
 	 * 
 	 * @param module
 	 */
 	private void fillCache(IModule[] module) {
 		if (currentResources != null)
 			return;
 		try {
 			int size = module.length;
 			ModuleDelegate pm = (ModuleDelegate) module[size - 1].loadAdapter(ModuleDelegate.class, null);
			currentResources = pm.members();
 			
 			delta = ServerPublishInfo.getDelta(resources, currentResources);
 			hasDelta = (delta != null && delta.length > 0);
 		} catch (CoreException ce) {
 			Trace.trace(Trace.WARNING, "Couldn't fill publish cache for " + module);
 		}
 	}
 
 	protected void clearCache() {
 		useCache = false;
 		currentResources = null;
 		delta = null;
 	}
 
 	protected IModuleResource[] getModuleResources(IModule[] module) {
 		if (module == null)
 			return new IModuleResource[0];
 		
 		if (useCache) {
 			fillCache(module);
 			return currentResources;
 		}
 		
 		int size = module.length;
 		ModuleDelegate pm = (ModuleDelegate) module[size - 1].loadAdapter(ModuleDelegate.class, null);
 		try {
 			if (pm != null)
 				return pm.members();
 		} catch (CoreException ce) {
 			// ignore
 		}
 		return new IModuleResource[0];
 	}
 
 	protected IModuleResourceDelta[] getDelta(IModule[] module) {
 		if (module == null)
 			return new IModuleResourceDelta[0];
 		
 		if (useCache) {
 			fillCache(module);
 			return delta;
 		}
 		
 		int size = module.length;
 		ModuleDelegate pm = (ModuleDelegate) module[size - 1].loadAdapter(ModuleDelegate.class, null);
 		IModuleResource[] resources2 = null;
 		try {
 			if (pm != null)
 				resources2 = pm.members();
 		} catch (CoreException ce) {
 			// ignore
 		}
 		if (resources2 == null)
 			resources2 = new IModuleResource[0];
 		return ServerPublishInfo.getDelta(getResources(), resources2);
 	}
 
 	protected boolean hasDelta(IModule[] module) {
 		if (module == null)
 			return false;
 		
 		if (useCache) {
 			fillCache(module);
 			return hasDelta;
 		}
 		
 		int size = module.length;
 		ModuleDelegate pm = (ModuleDelegate) module[size - 1].loadAdapter(ModuleDelegate.class, null);
 		IModuleResource[] resources2 = null;
 		try {
 			if (pm != null)
 				resources2 = pm.members();
 		} catch (CoreException ce) {
 			// ignore
 		}
 		if (resources2 == null)
 			resources2 = new IModuleResource[0];
 		return ServerPublishInfo.hasDelta(getResources(), resources2);
 	}
 
 	public void fill(IModule[] module) {
 		if (module == null)
 			return;
 		
 		if (useCache) {
 			fillCache(module);
 			setResources(currentResources);
 			return;
 		}
 		
 		int size = module.length;
 		ModuleDelegate pm = (ModuleDelegate) module[size - 1].loadAdapter(ModuleDelegate.class, null);
 		try {
 			if (pm != null)
 				setResources(pm.members());
 		} catch (CoreException ce) {
 			// ignore
 		}
 	}
 
 	public String toString() {
 		return "ModulePublishInfo [" + moduleId + "]";
 	}
 }
