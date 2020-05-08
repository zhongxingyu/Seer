 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.generator.base;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * @author Henrik Rentz-Reichert
  *
  */
 public class FileSystemHelpers {
 
 	/**
 	 * calls {@link #getMarkerFileURI(EObject, String)} to determine the URI of the
 	 * parent Eclipse project containing this object
 	 * 
 	 * @param obj an objected which should be located in a resource
 	 * @return the URI of the Eclipse project
 	 */
 	public static URI getProjectURI(EObject obj) {
 		return getMarkerFileURI(obj, ".project");
 	}
 	
 	/**
 	 * determines the URI of the first directory in the path of an object's resource
 	 * which contains a file with a given name
 	 * 
 	 * @param obj an objected which should be located in a resource
 	 * @param markerFileName name of marker file
 	 * @return the URI of the first directory containing marker or <code>null</code> if not found
 	 */
 	public static URI getMarkerFileURI(EObject obj, String markerFileName) {
 		URI mainPath = null;
 		if (obj!=null && obj.eResource()!=null) {
 			mainPath = obj.eResource().getURI().trimSegments(1);
 			File parent = null;
 			if (mainPath.isPlatform()) {
 				// HOWTO: get absolute OS path suitable for java.io.File from platform scheme EMF URI
 				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 				IFolder file = workspaceRoot.getFolder(new Path(mainPath.toPlatformString(true)));
 				String osString = file.getLocation().toOSString();
 				parent = new File(osString);
 			}
 			else {
 				parent = new File(mainPath.toFileString()).getAbsoluteFile();
 				mainPath = URI.createFileURI(parent.getAbsolutePath());
 			}
 			
 			boolean isProject = false;
 			int nUp = 0;
 			while (!isProject && parent!=null) {
 				String[] contents = parent.list();
 				for (int i = 0; i < contents.length; i++) {
 					if (contents[i].equals(markerFileName)) {
 						isProject = true;
 						break;
 					}
 				}
 				if (isProject)
 					break;
 				
 				parent = parent.getParentFile();
 				++nUp;
 			}
 			if (isProject && nUp>0)
 				mainPath = mainPath.trimSegments(nUp);
 		}
 		return mainPath;
 	}
 	
 	private static boolean bothNullOrEqual(String s1, String s2) {
 		if (s1==null && s2==null)
 			return true;
 		if (s1==null || s2==null)
 			return false;
 		return s1.equals(s2);
 	}
 
 	/**
 	 * the given paths are converted to file URIs (using {@link URI#createFileURI(String)}
 	 * and then {@link #getRelativePath(URI, URI, boolean)} is called with <code>goUpIfNeeded=false</code>.
 	 * 
 	 * @param base the base path
 	 * @param path the path for which the relative path is computed
 	 * @return relative path (<code>null</code>if there is none)
 	 */
 	public static String getRelativePath(String base, String path) {
 		return getRelativePath(URI.createFileURI(base), URI.createFileURI(path), false);
 	}
 
 	/**
 	 * the given paths are converted to file URIs (using {@link URI#createFileURI(String)}
 	 * and then {@link #getRelativePath(URI, URI, boolean)} is called.
 	 * 
 	 * @param base the base path
 	 * @param path the path for which the relative path is computed
 	 * @param goUpIfNeeded allow also ascending to parent directories
 	 * @return relative path (<code>null</code>if there is none)
 	 */
 	public static String getRelativePath(String base, String path, boolean goUpIfNeeded) {
 		return getRelativePath(URI.createFileURI(base), URI.createFileURI(path), goUpIfNeeded);
 	}
 	
 	/**
 	 * {@link #getRelativePath(URI, URI, boolean)} is called with
 	 * <code>goUpIfNeeded=false</code>
 	 * 
 	 * @param base the base path
 	 * @param path the path for which the relative path is computed
 	 * @return relative path (<code>null</code>if there is none)
 	 */
 	public static String getRelativePath(URI base, URI path) {
 		return getRelativePath(base, path, false);
 	}
 	
 	/**
 	 * compute a relative path to a given base path.
 	 * Both paths must be of the same scheme and absolute and the given
 	 * path has to have the first segments identical with the base path.
 	 * Returned is a relative path separated by / characters.
 	 * If there is no such relative path <code>null</code> is returned.
 	 * 
 	 * @param base the base path
 	 * @param path the path for which the relative path is computed
 	 * @param goUpIfNeeded allow also ascending to parent directories
 	 * @return relative path (<code>null</code>if there is none)
 	 */
 	public static String getRelativePath(URI base, URI path, boolean goUpIfNeeded) {
 		if (base==null || path==null)
 			return null;
 		
 		if (!bothNullOrEqual(base.scheme(),path.scheme()))
 			return null;
 		
 		if (!base.hasAbsolutePath())
 			return null;
 		
 		if (!path.hasAbsolutePath())
 			return null;
 		
		if (!path.device().equals(base.device()))
 			return null;
 		
 		StringBuffer result = new StringBuffer();
 		if (goUpIfNeeded) {
 			int max = base.segmentCount()<path.segmentCount()? base.segmentCount():path.segmentCount();
 			int common;
 			for (common=0; common<max; ++common) {
 				if (!base.segment(common).equals(path.segment(common)))
 					break;
 			}
 			for (int i=common; i<base.segmentCount(); ++i) {
 				result.append("../");
 			}
 			for (int i=common; i<path.segmentCount(); ++i) {
 				result.append(path.segment(i)+"/");
 			}
 			
			if(result.length()==0)
 				return "";
 			
 			return result.substring(0, result.length()-1);
 		}
 		else {
 			if (path.segmentCount()<base.segmentCount())
 				return null;
 			
 			for (int i=0; i<base.segmentCount(); ++i) {
 				if (!base.segment(i).equals(path.segment(i)))
 					return null;
 			}
 			
 			for (int i=base.segmentCount(); i<path.segmentCount(); ++i) {
 				result.append(path.segment(i)+"/");
 			}
 
 			if(result.length()==0)
 				return "";
 			
 			return result.substring(0, result.length()-1);
 		}
 	}
 }
