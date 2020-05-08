 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 
 public class PriorityClassDLTKExtensionManager extends PriorityDLTKExtensionManager {
 
 	private static final String CLASS_ATTR = "class";
 
 	public PriorityClassDLTKExtensionManager(String extensionPoint) {
 		super(extensionPoint, "nature");
 	}
 	public PriorityClassDLTKExtensionManager(String extensionPoint, String id) {
 		super(extensionPoint, id);
 	}
 
	public Object getObject(String natureId) {
		return getInitObject(getElementInfo(natureId));
 	}
 
 	public Object getInitObject(ElementInfo ext) {
 		try {
 			if (ext != null) {
 				if (ext.object != null) {
 					return ext.object;
 				}
 
 				IConfigurationElement cfg = (IConfigurationElement) ext.config;
 				Object object = createObject(cfg);
 				ext.object = object;
 				return object;
 			}
 		} catch (CoreException e) {
 			if( DLTKCore.DEBUG ) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	protected Object createObject(IConfigurationElement cfg)
 			throws CoreException {
 		return cfg.createExecutableExtension(CLASS_ATTR);
 	}
 
 	public Object getObject(IModelElement element) {
 		if (element.getElementType() == IModelElement.SCRIPT_MODEL) {
 			return null;
 		}
 		IProject project = element.getScriptProject().getProject();
 		String natureId = findScriptNature(project);
 		if (natureId != null) {
 			Object toolkit = getObject(natureId);
 			if (toolkit != null) {
 				return toolkit;
 			}
 		}
 		return null;
 	}
 
 	public Object getObjectLower(String natureID) {
 		ElementInfo ext = this.getElementInfo(natureID);
 		if (ext.oldInfo == null) {
 			return null;
 		}
 		return getInitObject(ext.oldInfo);
 	}
 }
