 /**********************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
 *
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.core;
 
 import java.util.*;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 
 import org.eclipse.wst.server.core.internal.ModuleFactory;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.internal.Trace;
 /**
  * Server utility methods.
  */
 public class ServerUtil {
 	/**
 	 * Static class - cannot create an instance.
 	 */
 	private ServerUtil() {
 		// do nothing
 	}
 
 	/**
 	 * Returns true if the given server currently contains the given module.
 	 *
 	 * @param server org.eclipse.wst.server.core.IServer
 	 * @param module org.eclipse.wst.server.core.IModule
 	 * @return boolean
 	 */
 	public static boolean containsModule(IServer server, IModule module, IProgressMonitor monitor) {
 		if (server == null)
 			return false;
 		Trace.trace(Trace.FINEST, "containsModule() " + server + " " + module);
 		try {
 			IModule[] modules = getAllContainedModules(server, monitor);
 			if (modules != null) {
 				int size = modules.length;
 				for (int i = 0; i < size; i++) {
 					Trace.trace(Trace.FINEST, "module: " + modules[i] + " " + module.equals(modules[i]));
 					if (module.equals(modules[i]))
 						return true;
 				}
 			}
 		} catch (Throwable t) {
 			// ignore
 		}
 		return false;
 	}
 
 	/**
 	 * Returns all projects contained by the server. This included the
 	 * projects that are in the configuration, as well as their
 	 * children, and their children...
 	 *
 	 * @param server org.eclipse.wst.server.core.IServer
 	 * @return java.util.List
 	 */
 	public static IModule[] getAllContainedModules(IServer server, IProgressMonitor monitor) {
 		//Trace.trace("> getAllContainedModules: " + getName(configuration));
 		List modules = new ArrayList();
 		if (server == null)
 			return new IModule[0];
 
 		// get all of the directly contained projects
 		IModule[] deploys = server.getModules(monitor);
 		if (deploys == null || deploys.length == 0)
 			return new IModule[0];
 
 		int size = deploys.length;
 		for (int i = 0; i < size; i++) {
 			if (deploys[i] != null && !modules.contains(deploys[i]))
 				modules.add(deploys[i]);
 		}
 
 		//Trace.trace("  getAllContainedModules: root level done");
 
 		// get all of the module's children
 		int count = 0;
 		while (count < modules.size()) {
 			IModule module = (IModule) modules.get(count);
 			try {
 				IModule[] children = server.getChildModules(module, monitor);
 				if (children != null) {
 					size = children.length;
 					for (int i = 0; i < size; i++) {
 						if (children[i] != null && !modules.contains(children[i]))
 							modules.add(children[i]);
 					}
 				}
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Error getting child modules for: " + module.getName(), e);
 			}
 			count ++;
 		}
 
 		//Trace.trace("< getAllContainedModules");
 
 		IModule[] modules2 = new IModule[modules.size()];
 		modules.toArray(modules2);
 		return modules2;
 	}
 	
 	/**
 	 * Returns a list of all servers that this module is configured on.
 	 *
 	 * @param module org.eclipse.wst.server.core.model.IModule
 	 * @return java.util.List
 	 */
 	public static IServer[] getServersByModule(IModule module, IProgressMonitor monitor) {
 		if (module == null)
 			return new IServer[0];
 
 		// do it the slow way - go through all servers and
 		// see if this module is configured in it
 		List list = new ArrayList();
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				if (containsModule(servers[i], module, monitor))
 					list.add(servers[i]);
 			}
 		}
 		
 		IServer[] allServers = new IServer[list.size()];
 		list.toArray(allServers);
 		return allServers;
 	}
 	
 	/**
 	 * Returns the project modules attached to a project.
 	 */
 	public static IModule[] getModules(IProject project) {
 		if (project == null)
 			return null;
 
 		List list = new ArrayList();
 		IModule[] modules = getModules();
 		if (modules != null) {
 			int size = modules.length;
 			for (int i = 0; i < size; i++) {
 				if (modules[i] != null && project.equals(modules[i].getProject()))
 					list.add(modules[i]);
 			}
 		}
 		
 		IModule[] modules2 = new IModule[list.size()];
 		list.toArray(modules2);
 		return modules2;
 	}
 
 	/**
 	 * Returns a module from the given moduleId.
 	 * 
 	 * @param java.lang.String moduleId
 	 * @return the module
 	 */
 	public static IModule getModule(String moduleId) {
 		int index = moduleId.indexOf(":");
 		if (index <= 0)
 			return null;
 		
 		String factoryId = moduleId.substring(0, index);
 		ModuleFactory moduleFactory = ServerCore.findModuleFactory(factoryId);
 		if (moduleFactory == null)
 			return null;
 
 		String moduleSubId = moduleId.substring(index+1);
 		IModule module = moduleFactory.getModule(moduleSubId);
 		if (module != null)
 			return module;
 		return null;
 	}
 	
 	/**
 	 * Returns true if the given class name s is a superclass or interface of
 	 * the class obj.
 	 * 
 	 * @param c java.lang.Class
 	 * @param s java.lang.String
 	 * @return boolean
 	 */
 	protected static boolean isEnabled(Class c, String s) {
 		if (s == null || s.length() == 0)
 			return true;
 
 		while (c != null && c != Object.class) {
 			if (s.equals(c.getName()))
 				return true;
 			Class[] inf = c.getInterfaces();
 			if (inf != null) {
 				int size = inf.length;
 				for (int i = 0; i < size; i++) {
 					if (s.equals(inf[i].getName()))
 						return true;
 				}
 			}
 			c = c.getSuperclass();
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns a module for the given object that may be launchable on
 	 * a server using the Run on Server option. Throws an exception if it
 	 * can't determine the object without possible plugin loading.
 	 * 
 	 * @param obj java.lang.Object
 	 * @return IModule
 	 * @throws java.lang.Exception
 	 */
 	public static IModule getModule(Object obj, boolean initialized) throws Exception {
 		if (obj == null)
 			return null;
 		Trace.trace(Trace.FINEST, "ServerUtil.getModule()");
 		IModuleArtifactAdapter[] adapters = ServerCore.getModuleArtifactAdapters();
 		if (adapters != null) {
 			int size = adapters.length;
 			for (int i = 0; i < size; i++) {
 				if (isEnabled(obj.getClass(), adapters[i].getObjectClassName())) {
 					if (!initialized) {
 						Trace.trace(Trace.FINEST, "getModule(): " + obj.getClass() + " " + adapters[i].getObjectClassName());
 						throw new Exception();
 					}
 					IModuleArtifact moduleObject = adapters[i].getModuleObject(obj);
 					if (moduleObject != null)
 						return moduleObject.getModule();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns a list of the launchable clients for the given object.
 	 *
 	 * @return java.util.List
 	 */
 	public static IModuleArtifact[] getModuleObjects(Object obj) {
 		List list = new ArrayList();
 		Trace.trace(Trace.FINEST, "ServerUtil.getModuleObjects()");
 		IModuleArtifactAdapter[] adapters = ServerCore.getModuleArtifactAdapters();
 		if (adapters != null) {
 			int size = adapters.length;
 			for (int i = 0; i < size; i++) {
 				if (isEnabled(obj.getClass(), adapters[i].getObjectClassName())) {
 					IModuleArtifact moduleObject = adapters[i].getModuleObject(obj);
 					Trace.trace(Trace.FINEST, "moduleObject: " + moduleObject);
 					if (moduleObject != null)
 						list.add(moduleObject);
 				}
 			}
 		}
 		
 		IModuleArtifact[] mo = new IModuleArtifact[list.size()];
 		list.toArray(mo);
 		return mo;
 	}
 
 	/**
 	 * Returns the first launchable object for the given server and module
 	 * object.
 	 * 
 	 * @param server
 	 * @param moduleObject
 	 * @return ILaunchable
 	 */
 	/*public static ILaunchable getLaunchable(IServer server, IModuleArtifact moduleObject) {
 		ILaunchableAdapter[] adapters = ServerCore.getLaunchableAdapters();
 		if (adapters != null) {
 			int size = adapters.length;
 			for (int i = 0; i < size; i++) {
 				try {
 					ILaunchable launchable = adapters[i].getLaunchable(server, moduleObject);
 					Trace.trace(Trace.FINEST, "adapter= " + adapters[i] + ", launchable= " + launchable);
 					if (launchable != null)
 						return launchable;
 				} catch (Exception e) {
 					Trace.trace(Trace.SEVERE, "Error in launchable adapter", e);
 				}
 			}
 		}
 		return null;
 	}*/
 
 	/**
 	 * Returns the first launchable object for the given server and module
 	 * object.
 	 * 
 	 * @param server
 	 * @param moduleObject
 	 * @return ILaunchable
 	 */
 	/*public static ILaunchable getLaunchable(IServer server, List moduleObjects) {
 		Iterator iterator = moduleObjects.iterator();
 		while (iterator.hasNext()) {
 			IModuleArtifact moduleObject = (IModuleArtifact) iterator.next();
 			ILaunchable launchable = getLaunchable(server, moduleObject);
 			if (launchable != null)
 				return launchable;
 		}
 		return null;
 	}*/
 
 	/**
 	 * Returns the factory that created the given module.
 	 *
 	 * @param org.eclipse.wst.server.core.IModule
 	 * @return org.eclipse.wst.server.core.IModuleFactory
 	 */
 	/*public static IModuleFactory getModuleFactory(IModule module) {
 		String id = module.getFactoryId();
 		if (id == null)
 			return null;
 
 		IModuleFactory[] factories = ServerCore.getModuleFactories();
 		if (factories != null) {
 			int size = factories.length;
 			for (int i = 0; i < size; i++) {
 				if (id.equals(factories[i].getId()))
 					return factories[i];
 			}
 		}
 		return null;
 	}*/
 
 	/**
 	 * Return all the available modules from all factories whose
 	 * type includes the given id.
 	 * 
 	 * @param type java.lang.String
 	 * @param onlyProjectModules boolean
 	 * @return java.util.List
 	 */
 	public static IModule[] getModules(IModuleType[] moduleTypes) {
 		List list = new ArrayList();
 
 		ModuleFactory[] factories = ServerCore.getModuleFactories();
 		if (factories != null) {
 			int size = factories.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(factories[i].getModuleTypes(), moduleTypes)) {
 					IModule[] modules = factories[i].getModules();
 					if (modules != null) {
 						int size2 = modules.length;
 						for (int j = 0; j < size2; j++)
 							list.add(modules[j]);
 					}
 				}
 			}
 		}
 		IModule[] modules = new IModule[list.size()];
 		list.toArray(modules);
 		return modules;
 	}
 	
 	public static IModule[] getModules(String type) {
 		List list = new ArrayList();
 
 		ModuleFactory[] factories = ServerCore.getModuleFactories();
 		if (factories != null) {
 			int size = factories.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(factories[i].getModuleTypes(), type, null)) {
 					IModule[] modules = factories[i].getModules();
 					if (modules != null) {
 						int size2 = modules.length;
 						for (int j = 0; j < size2; j++)
 							list.add(modules[j]);
 					}
 				}
 			}
 		}
 		IModule[] modules = new IModule[list.size()];
 		list.toArray(modules);
 		return modules;
 	}
 	
 	/*public static boolean isSupportedModule(IServerType serverType, IModuleType moduleType) {
 		IRuntimeType runtimeType = serverType.getRuntimeType();
 		return isSupportedModule(runtimeType.getModuleTypes(), moduleType.getId(), moduleType.getVersion());
 	}*/
 	
 	public static boolean isSupportedModule(IModuleType[] moduleTypes, String type, String version) {
 		if (moduleTypes != null) {
 			int size = moduleTypes.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(moduleTypes[i], type, version))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	protected static boolean isSupportedModule(IModuleType[] moduleTypes, IModuleType[] mt) {
 		if (mt != null) {
 			int size = mt.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(moduleTypes, mt[i]))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean isSupportedModule(IModuleType[] moduleTypes, IModuleType mt) {
 		if (moduleTypes != null) {
 			int size = moduleTypes.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(moduleTypes[i], mt))
 					return true;
 			}
 		}
 		return false;
 	}
 	
 	protected static boolean isSupportedModule(IModuleType moduleType, String type, String version) {
 		String type2 = moduleType.getId();
 		if (matches(type, type2)) {
 			String version2 = moduleType.getVersion();
 			if (matches(version, version2))
 				return true;
 		}
 		return false;
 	}
 
 	public static boolean isSupportedModule(IModuleType moduleType, IModuleType mt) {
 		String type2 = moduleType.getId();
 		if (matches(mt.getId(), type2)) {
 			String version2 = moduleType.getVersion();
 			if (matches(mt.getVersion(), version2))
 				return true;
 		}
 		return false;
 	}
 
 	protected static boolean matches(String a, String b) {
 		if (a == null || b == null || "*".equals(a) || "*".equals(b) || a.startsWith(b) || b.startsWith(a))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Return all the available modules from all factories.
 	 * 
 	 * @return java.util.List
 	 */
 	protected static IModule[] getModules() {
 		List list = new ArrayList();
 		
 		ModuleFactory[] factories = ServerCore.getModuleFactories();
 		if (factories != null) {
 			int size = factories.length;
 			for (int i = 0; i < size; i++) {
 				IModule[] modules = factories[i].getModules();
 				if (modules != null) {
 					int size2 = modules.length;
 					for (int j = 0; j < size2; j++) {
						if (!list.contains(modules[j]))
							list.add(modules[j]);
 					}
 				}
 			}
 		}
 		IModule[] modules = new IModule[list.size()];
 		list.toArray(modules);
 		return modules;
 	}
 
 	/**
 	 * Adds or removes modules from a server. Will search for the first parent module
 	 * of each module and add it to the server instead. This method will handle multiple
 	 * modules having the same parent (the parent will only be added once), but may not
 	 * handle the case where the same module or parent is being both added and removed.
 	 * 
 	 * @param server
 	 * @param add
 	 * @param remove
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	public static void modifyModules(IServerWorkingCopy server, IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
 		if (add == null)
 			add = new IModule[0];
 		if (remove == null)
 			remove = new IModule[0];
 		
 		int size = add.length;
 		List addParentModules = new ArrayList();
 		for (int i = 0; i < size; i++) {
 			boolean found = false;
 			try {
 				IModule[] parents = server.getParentModules(add[i], monitor);
 				if (parents != null) {
 					found = true;
 					if (parents.length > 0) {				
 						Object parent = parents[0];
 						found = true;
 						if (!addParentModules.contains(parent))
 							addParentModules.add(parent);
 					}
 				} 
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Could not find parent module", e);
 			}
 			
 			if (!found)
 				addParentModules.add(add[i]);
 		}
 		
 		size = remove.length;
 		List removeParentModules = new ArrayList();
 		for (int i = 0; i < size; i++) {
 			boolean found = false;
 			try {
 				IModule[] parents = server.getParentModules(remove[i], monitor);
 				if (parents != null) {
 					found = true;
 					if (parents.length > 0) {				
 						Object parent = parents[0];
 						found = true;
 						if (!removeParentModules.contains(parent))
 							removeParentModules.add(parent);
 					}
 				} 
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Could not find parent module 2", e);
 			}
 			
 			if (!found)
 				removeParentModules.add(remove[i]);
 		}
 		
 		IModule[] add2 = new IModule[addParentModules.size()];
 		addParentModules.toArray(add2);
 		IModule[] remove2 = new IModule[removeParentModules.size()];
 		removeParentModules.toArray(remove2);
 		
 		server.modifyModules(add2, remove2, monitor);
 	}
 
 	/**
 	 * Returns true if the given server is already started in the given
 	 * mode, or could be (re)started in the start mode.
 	 * 
 	 * @param server org.eclipse.wst.server.core.IServer
 	 * @param launchMode java.lang.String
 	 * @return boolean
 	 */
 	public static boolean isCompatibleWithLaunchMode(IServer server, String launchMode) {
 		if (server == null || launchMode == null)
 			return false;
 
 		int state = server.getServerState();
 		if (state == IServer.STATE_STARTED && launchMode.equals(server.getMode()))
 			return true;
 
 		if (server.getServerType().supportsLaunchMode(launchMode))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Filters the servers to those that are already started in the given launchMode
 	 * or can be (re)started in the given launchMode.
 	 * 
 	 * @param server org.eclipse.wst.server.core.IServer[]
 	 * @param launchMode java.lang.String
 	 * @return org.eclipse.wst.server.core.IServer[]
 	 */
 	/*public static IServer[] filterServersByLaunchMode(IServer[] servers, String launchMode) {
 		if (servers == null || servers.length == 0)
 			return servers;
 		
 		int size = servers.length;
 		List list = new ArrayList();
 		for (int i = 0; i < size; i++) {
 			if (isCompatibleWithLaunchMode(servers[i], launchMode))
 				list.add(servers[i]);
 		}
 		IServer[] temp = new IServer[list.size()];
 		list.toArray(temp);
 		return temp;
 	}*/
 
 	/**
 	 * Visit all the modules in the server configuration.
 	 */
 	public static void visit(IServerAttributes server, IModuleVisitor visitor, IProgressMonitor monitor) {
 		if (server == null)
 			return;
 		
 		IModule[] modules = server.getModules(monitor);
 		if (modules != null) { 
 			int size = modules.length;
 			for (int i = 0; i < size; i++) {
 				if (!visitModule(server, new IModule[0], modules[i], visitor, monitor))
 					return;
 			}
 		}
 	}
 
 	/**
 	 * Returns true to keep visiting, and false to stop.
 	 */
 	private static boolean visitModule(IServerAttributes server, IModule[] parents, IModule module, IModuleVisitor visitor, IProgressMonitor monitor) {
 		if (server == null || module == null || parents == null)
 			return true;
 		
 		if (!visitor.visit(parents, module))
 			return false;
 		
 		IModule[] children = server.getChildModules(module, monitor);
 		if (children != null) {
 			int size = children.length;
 			for (int i = 0; i < size; i++) {
 				IModule module2 = children[i];
 				IModule[] parents2 = new IModule[parents.length + 1];
 				System.arraycopy(parents, 0, parents2, 0, parents.length);
 				parents2[parents.length] = module;
 				
 				if (!visitModule(server, parents2, module2, visitor, monitor))
 					return false;
 			}
 		}
 			
 		return true;
 	}
 
 	public static void setRuntimeDefaultName(IRuntimeWorkingCopy wc) {
 		String typeName = wc.getRuntimeType().getName();
 		
 		String name = ServerPlugin.getResource("%defaultRuntimeName", new String[] {typeName});
 		int i = 2;
 		while (isNameInUse(name)) {
 			name = ServerPlugin.getResource("%defaultRuntimeName2", new String[] {typeName, i + ""});
 			i++;
 		}
 		wc.setName(name);
 	}
 
 	public static void setServerDefaultName(IServerWorkingCopy wc) {
 		String typeName = wc.getServerType().getName();
 		String host = wc.getHost();
 		
 		String name = ServerPlugin.getResource("%defaultServerName", new String[] {typeName, host});
 		int i = 2;
 		while (isNameInUse(name)) {
 			name = ServerPlugin.getResource("%defaultServerName2", new String[] {typeName, host, i + ""});
 			i++;
 		}
 		wc.setName(name);
 	}
 
 	private static boolean isValidFilename(String name) {
 		IStatus status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FILE);
 		if (status != null && !status.isOK())
 			return false;
 		
 		status = ResourcesPlugin.getWorkspace().validateName(name, IResource.FOLDER);
 		if (status != null && !status.isOK())
 			return false;
 		
 		return true;
 	}
 
 	private static String getValidFileName(String name) {
 		if (isValidFilename(name))
 			return name;
 	
 		// remove invalid characters
 		String[] s = new String[] {".", "\\", "/", "?", ":", "*", "\"", "|", "<", ">"};
 		int ind = 0;
 		while (ind < s.length) {
 			int index = name.indexOf(s[ind]);
 			while (index >= 0) {
 				name = name.substring(0, index) + name.substring(index+1);
 				index = name.indexOf(s[ind]);
 			}
 			ind++;
 		}
 		return name;
 	}
 
 	public static IFile getUnusedServerFile(IProject project, IServerType type) {
 		String typeName = getValidFileName(type.getName());
 		String name = ServerPlugin.getResource("%defaultServerName3", new String[] {typeName})+ "."  + IServer.FILE_EXTENSION;
 		int i = 2;
 		while (isFileNameInUse(project, name)) {
 			name = ServerPlugin.getResource("%defaultServerName4", new String[] {typeName, i + ""}) + "."  + IServer.FILE_EXTENSION;
 			i++;
 		}
 		return project.getFile(name);
 	}
 
 	/**
 	 * Returns true if an element exists with the given name.
 	 *
 	 * @param name java.lang.String
 	 * @return boolean
 	 */
 	private static boolean isNameInUse(String name) {
 		if (name == null)
 			return true;
 	
 		List list = new ArrayList();
 		
 		addAll(list, ServerCore.getRuntimes());
 		addAll(list, ServerCore.getServers());
 
 		Iterator iterator = list.iterator();
 		while (iterator.hasNext()) {
 			IElement element = (IElement) iterator.next();
 			if (name.equalsIgnoreCase(element.getName()))
 				return true;
 		}
 
 		return false;
 	}
 	
 	private static void addAll(List list, Object[] obj) {
 		if (obj == null)
 			return;
 		
 		int size = obj.length;
 		for (int i = 0; i < size; i++) {
 			list.add(obj[i]);
 		}
 	}
 	
 	/**
 	 * Returns true if an element exists with the given name.
 	 *
 	 * @param name java.lang.String
 	 * @return boolean
 	 */
 	private static boolean isFileNameInUse(IProject project, String name) {
 		if (name == null || project == null)
 			return false;
 		
 		if (project.getFile(name).exists())
 			return true;
 		if (project.getFolder(name).exists())
 			return true;
 	
 		return false;
 	}
 
 	/**
 	 * Return a list of all runtime targets that match the given type and version.
 	 * If type or version is null, it matches all of that type or version.
 	 * 
 	 * @param type
 	 * @param version
 	 * @return 
 	 */
 	public static IRuntime[] getRuntimes(String type, String version) {
 		List list = new ArrayList();
 		IRuntime[] runtimes = ServerCore.getRuntimes();
 		if (runtimes != null) {
 			int size = runtimes.length;
 			for (int i = 0; i < size; i++) {
 				IRuntimeType runtimeType = runtimes[i].getRuntimeType();
 				if (runtimeType != null && isSupportedModule(runtimeType.getModuleTypes(), type, version)) {
 					list.add(runtimes[i]);
 				}
 			}
 		}
 		
 		IRuntime[] runtimes2 = new IRuntime[list.size()];
 		list.toArray(runtimes2);
 		return runtimes2;
 	}
 
 	/**
 	 * Return a list of all runtime types that match the given type and version.
 	 * If type or version is null, it matches all of that type or version.
 	 * 
 	 * @param type
 	 * @param version
 	 * @return 
 	 */
 	public static IRuntimeType[] getRuntimeTypes(String type, String version) {
 		List list = new ArrayList();
 		IRuntimeType[] runtimeTypes = ServerCore.getRuntimeTypes();
 		if (runtimeTypes != null) {
 			int size = runtimeTypes.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(runtimeTypes[i].getModuleTypes(), type, version)) {
 					list.add(runtimeTypes[i]);
 				}
 			}
 		}
 		
 		IRuntimeType[] rt = new IRuntimeType[list.size()];
 		list.toArray(rt);
 		return rt;
 	}
 	
 	/**
 	 * Return a list of all runtime types that match the given type, version,
 	 * and partial runtime type id. If type, version, or runtimeTypeId is null,
 	 * it matches all of that type or version.
 	 * 
 	 * @param type
 	 * @param version
 	 * @return 
 	 */
 	public static IRuntimeType[] getRuntimeTypes(String type, String version, String runtimeTypeId) {
 		List list = new ArrayList();
 		IRuntimeType[] runtimeTypes = ServerCore.getRuntimeTypes();
 		if (runtimeTypes != null) {
 			int size = runtimeTypes.length;
 			for (int i = 0; i < size; i++) {
 				if (isSupportedModule(runtimeTypes[i].getModuleTypes(), type, version)) {
 					if (runtimeTypeId == null || runtimeTypes[i].getId().startsWith(runtimeTypeId))
 						list.add(runtimeTypes[i]);
 				}
 			}
 		}
 		
 		IRuntimeType[] rt = new IRuntimeType[list.size()];
 		list.toArray(rt);
 		return rt;
 	}
 
 	/**
 	 * Returns a list of all servers that this deployable is not currently
 	 * configured on, but could be added to. If includeErrors is true, this
 	 * method return servers where the parent deployable may throw errors. For
 	 * instance, this deployable may be the wrong spec level.
 	 *
 	 * @param module com.ibm.etools.server.core.IModule
 	 * @return com.ibm.etools.server.core.IServer[]
 	 */
 	public static IServer[] getAvailableServersForModule(IModule module, boolean includeErrors, IProgressMonitor monitor) {
 		if (module == null)
 			return new IServer[0];
 
 		// do it the slow way - go through all servers and
 		// see if this deployable is not configured in it
 		// but could be added
 		List list = new ArrayList();
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				if (!containsModule(servers[i], module, monitor)) {
 					try {
 						IModule[] parents = servers[i].getParentModules(module, monitor);
 						if (parents != null && parents.length > 0) {
 							boolean found = false;
 							if (parents != null) {
 								int size2 = parents.length;
 								for (int j = 0; !found && j < size2; j++) {
 									IModule parent = parents[j];
 									IStatus status = servers[i].canModifyModules(new IModule[] { parent }, new IModule[0], monitor);
 									if (status == null || status.isOK()){
 										list.add(servers[i]);
 										found = true;
 									}
 								}
 							}
 						}
 					} catch (Exception se) {
 						if (includeErrors)
 							list.add(servers[i]);
 					}
 				}
 			}
 		}
 		
 		// make sure that the preferred server is the first one
 		//IServer server = ServerCore.getServerPreferences().getDeployableServerPreference(deployable);
 		//if (server != null && list.contains(server) && list.indexOf(server) != 0) {
 		//	list.remove(server);
 		//	list.add(0, server);
 		//}
 
 		IServer[] allServers = new IServer[list.size()];
 		list.toArray(allServers);
 		return allServers;
 	}
 
 	/*public static boolean isDefaultAvailable(IServerType serverType, IModuleType moduleType) {
 		if (!isSupportedModule(serverType, moduleType))
 			return false;
 	
 		// remote servers won't automatically work (i.e. without hostname)
 		if (!serverType.supportsLocalhost())
 			return false;
 		
 		// if it needs a runtime, check if there is one
 		if (serverType.hasRuntime()) {
 			IRuntime[] runtimes = ServerCore.getResourceManager().getRuntimes(serverType.getRuntimeType());
 			if (runtimes == null || runtimes.length == 0)
 				return false;
 		}
 		return true;
 	}*/
 	
 	/**
 	 * Returns an array of all known runtime instances of
 	 * the given runtime type. This convenience method filters the list of known
 	 * runtime ({@link #getRuntimes()}) for ones with a matching
 	 * runtime type ({@link IRuntime#getRuntimeType()}). The array will not
 	 * contain any working copies.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * <p>
 	 * [issue: Is this convenience method really necessary?
 	 * It's straightforward enough for a client to do.]
 	 * </p>
 	 * 
 	 * @param runtimeType the runtime type
 	 * @return a possibly-empty list of runtime instances {@link IRuntime}
 	 * of the given runtime type
 	 */
 	public static IRuntime[] getRuntimes(IRuntimeType runtimeType) {
 		List list = new ArrayList();
 		IRuntime[] runtimes = ServerCore.getRuntimes();
 		if (runtimes != null) {
 			int size = runtimes.length;
 			for (int i = 0; i < size; i++) {
 				if (runtimes[i].getRuntimeType() != null && runtimes[i].getRuntimeType().equals(runtimeType))
 					list.add(runtimes[i]);
 			}
 		}
 		
 		IRuntime[] r = new IRuntime[list.size()];
 		list.toArray(r);
 		return r;
 	}
 	
 	/**
 	 * Returns the server that came from the given file, or <code>null</code>
 	 * if none. This convenience method searches the list of known
 	 * servers ({@link #getServers()}) for the one with a matching
 	 * location ({@link IServer#getFile()}). The file may not be null.
 	 * <p>
 	 * [issue: Is this convenience method really necessary?
 	 * It's straightforward enough for a client to do.]
 	 * </p>
 	 *
 	 * @param a server file
 	 * @return the server instance, or <code>null</code> if 
 	 * there is no server associated with the given file
 	 */
 	public static IServer findServer(IFile file) {
 		if (file == null)
 			throw new IllegalArgumentException();
 		
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				if (file.equals(servers[i].getFile()))
 					return servers[i];
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns an array of all known server instances of
 	 * the given server type. This convenience method filters the list of known
 	 * servers ({@link #getServers()}) for ones with a matching
 	 * server type ({@link IServer#getServerType()}). The array will not contain
 	 * any working copies.
 	 * <p>
 	 * A new array is returned on each call, so clients may store or modify the result.
 	 * </p>
 	 * 
 	 * @param serverType the server type
 	 * @return a possibly-empty array of server instances {@link IServer}
 	 * of the given server type
 	 */
 	/*public static IServer[] getServers(IServerType serverType) {
 		List list = new ArrayList();
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				if (servers[i].getServerType().equals(serverType))
 					list.add(servers[i]);
 			}
 		}
 		
 		IServer[] s = new IServer[list.size()];
 		list.toArray(s);
 		return s;
 	}*/
 }
