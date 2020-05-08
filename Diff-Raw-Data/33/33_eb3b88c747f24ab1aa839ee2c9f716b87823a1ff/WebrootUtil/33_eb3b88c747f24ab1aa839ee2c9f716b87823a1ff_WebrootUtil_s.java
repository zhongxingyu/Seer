 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui.util;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * This utility class is used to make a bridge between the relative path from
  * webroot and physical path in the project.
  * 
  * @author Xiao-guang Zhang
  * 
  */
 public class WebrootUtil extends
 		org.eclipse.jst.jsf.facesconfig.common.utils.WebrootUtil {
 
 	/**
 	 * get the the project path for webpath The project path is something like
 	 * "/projectname/webroot/filename.jsp", or "/projectname/webroot/folder".
 	 * The project information should be removed from project path, e.g,
 	 * "/filename.jsp" or "/folder/*";
 	 * 
 	 * @param strPath -
 	 *            the web path
 	 * @return - project path from "/projectname/webroot"
 	 */
 	static public String getProjectPath(EObject model, String strPath) {
 
 		IProject project = getProject(model);
 
 		String strProjectPath = "";
 		if (strPath != null) {
 			IPath path = new Path(strPath);
 			// jsp file
 			if (path.getFileExtension() != null) {
 				if (isValidWebFile(path)) {
 					IPath webContentPath = getWebContentPath(project);
 					if (webContentPath != null)
 						strProjectPath = webContentPath.toString() + strPath;
 
 				}
 			} else
 			// jsp folder
 			{
 				strPath = new String(strPath.getBytes(), 0,
 						strPath.length() - 1);
 				IPath webContentPath = getWebContentPath(project);
 				if (webContentPath != null)
 					strProjectPath = webContentPath.toString() + strPath;
 			}
 		}
 		return strProjectPath;
 	}
 
	

 	/**
 	 * get the current resource according to EMF model
 	 * 
 	 * @param model -
 	 *            emf model
 	 * @return
 	 */
 	static public IResource getResource(EObject model) {
 		IResource resource = null;
 
 		if (model != null && model.eResource() != null) {
 			URI uri = model.eResource().getURI();
			Path path = new Path(URI.decode(uri.devicePath()));
 
 			IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
 					.getRoot();
 			resource = workspaceRoot.findMember(path);
 		}
 
 		return resource;
 	}
	
 	/**
 	 * get the current project according to EMF model
 	 * 
 	 * @param model -
 	 *            emf model
 	 * @return
 	 */
 	static public IProject getProject(EObject model) {
 		IProject project = null;
 
 		IResource resource = getResource(model);
 
 		if (resource != null) {
 			project = resource.getProject();
 		}
 
 		return project;
 	}

 }
