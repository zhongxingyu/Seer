 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.core.internal;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.model.InternalInitializer;
 import org.eclipse.wst.server.core.model.ModuleFactoryDelegate;
 /**
  * 
  */
 public class ModuleFactory implements IOrdered {
 	private IConfigurationElement element;
 	public ModuleFactoryDelegate delegate;
 	private List moduleTypes;
 
 	/**
 	 * ModuleFactory constructor comment.
 	 * 
 	 * @param element a configuration element
 	 */
 	public ModuleFactory(IConfigurationElement element) {
 		super();
 		this.element = element;
 	}
 
 	/**
 	 * Returns the id of this factory.
 	 *
 	 * @return java.lang.String
 	 */
 	public String getId() {
 		return element.getAttribute("id");
 	}
 
 	/**
 	 * Returns the index (ordering) of this task.
 	 *
 	 * @return int
 	 */
 	public int getOrder() {
 		try {
 			return Integer.parseInt(element.getAttribute("order"));
 		} catch (NumberFormatException e) {
 			return -1;
 		}
 	}
 
 	/**
 	 * Return the supported module types.
 	 * 
 	 * @return an array of module types
 	 */
 	public IModuleType[] getModuleTypes() {
 		if (moduleTypes == null)
 			moduleTypes = ServerPlugin.getModuleTypes(element.getChildren("moduleType"));
 		
 		IModuleType[] mt = new IModuleType[moduleTypes.size()];
 		moduleTypes.toArray(mt);
 		return mt;
 	}
 
 	/**
 	 * Returns true if this modules factory produces project modules.
 	 *
 	 * @return boolean
 	 */
 	public boolean isProjectModuleFactory() {
 		return "true".equalsIgnoreCase(element.getAttribute("projects"));
 	}
 
 	/*
 	 * @see IModuleFactoryDelegate#getDelegate()
 	 */
 	public ModuleFactoryDelegate getDelegate(IProgressMonitor monitor) {
 		if (delegate == null) {
 			try {
 				long time = System.currentTimeMillis();
 				delegate = (ModuleFactoryDelegate) element.createExecutableExtension("class");
 				InternalInitializer.initializeModuleFactoryDelegate(delegate, this, monitor);
 				Trace.trace(Trace.PERFORMANCE, "ModuleFactory.getDelegate(): <" + (System.currentTimeMillis() - time) + "> " + getId());
 			} catch (Throwable t) {
 				Trace.trace(Trace.SEVERE, "Could not create delegate " + toString() + ": " + t.getMessage());
 			}
 		}
 		return delegate;
 	}
 
 	/*
 	 * @see ModuleFactoryDelegate#getModules()
 	 */
	public IModule[] getModules() {
		return getModules(null);
	}

	/*
	 * @see ModuleFactoryDelegate#getModules()
	 */
 	public IModule[] getModules(IProgressMonitor monitor) {
 		try {
 			return getDelegate(monitor).getModules();
 		} catch (Throwable t) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate " + toString(), t);
 			return new IModule[0];
 		}
 	}
 
 	/*
 	 * @see ModuleFactoryDelegate#getModules(IProject)
 	 */
 	public IModule[] getModules(IProject project, IProgressMonitor monitor) {
 		try {
 			return getDelegate(monitor).getModules(project);
 		} catch (Throwable t) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate " + toString(), t);
 			return new IModule[0];
 		}
 	}
 
 	/*
 	 * @see ModuleFactoryDelegate#findModule(String)
 	 */
 	public IModule findModule(String id, IProgressMonitor monitor) {
 		try {
 			return getDelegate(monitor).findModule(id);
 		} catch (Throwable t) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate " + toString(), t);
 			return null;
 		}
 	}
 
 	/**
 	 * Return a string representation of this object.
 	 * 
 	 * @return java.lang.String
 	 */
 	public String toString() {
 		return "ModuleFactory[" + getId() + "]";
 	}
 }
