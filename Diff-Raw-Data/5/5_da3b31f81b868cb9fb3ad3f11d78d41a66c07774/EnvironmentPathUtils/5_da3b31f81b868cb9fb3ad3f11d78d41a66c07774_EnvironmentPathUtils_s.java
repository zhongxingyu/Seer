 /*******************************************************************************
  * Copyright (c) 2005, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.core.environment;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.osgi.util.NLS;
 
 public class EnvironmentPathUtils {
 	public static final String PATH_DELIMITER = ";"; //$NON-NLS-1$
 	private static final char SEPARATOR = '/';
 
 	public static IPath getFullPath(IEnvironment env, IPath path) {
 		if (isFull(path)) {
 			throw new RuntimeException(NLS.bind(
 					Messages.EnvironmentPathUtils_invalidPath, path));
 		}
 		// if( path.segment(0).startsWith("#special#")) {
 		// return path;
 		// }
 		String device = path.getDevice();
 		if (device == null)
 			device = Character.toString(IPath.DEVICE_SEPARATOR);
 
 		return path.setDevice(env.getId() + SEPARATOR + device);
 	}
 
 	public static IPath getFullPath(String envId, IPath path) {
 		if (isFull(path)) {
 			throw new RuntimeException(NLS.bind(
 					Messages.EnvironmentPathUtils_invalidPath, path));
 		}
 		// if( path.segment(0).startsWith("#special#")) {
 		// return path;
 		// }
 		String device = path.getDevice();
 		if (device == null)
 			device = Character.toString(IPath.DEVICE_SEPARATOR);
 
 		return path.setDevice(envId + SEPARATOR + device);
 	}
 
 	public static boolean isFull(IPath path) {
 		String device = path.getDevice();
 		return device != null && device.indexOf(SEPARATOR) >= 0;
 	}
 
 	public static IEnvironment getPathEnvironment(IPath path) {
 		if (!isFull(path)) {
 			return null;
 		}
 
 		String envId = path.getDevice();
 		if (envId == null)
 			return null;
 
 		int index = envId.indexOf(SEPARATOR);
 		envId = envId.substring(0, index);
 		return EnvironmentManager.getEnvironmentById(envId);
 	}
 
 	public static IPath getLocalPath(IPath path) {
 		// if( path.segment(0).startsWith("#special#")) {
 		// return path;
 		// }
 		if (!isFull(path)) {
 			return path;
 			// throw new RuntimeException("Invalid path");
 		}
 
 		String device = path.getDevice();
 		int index = device.indexOf(SEPARATOR);
 		Assert.isTrue(index >= 0);
 		device = device.substring(index + 1);
 		if (device.length() == 1 && device.charAt(0) == IPath.DEVICE_SEPARATOR)
 			device = null;
 
 		return path.setDevice(device);
 	}
 
 	public static String getLocalPathString(IPath path) {
 		IEnvironment env = getPathEnvironment(path);
 		IPath localPath = getLocalPath(path);
		return env.convertPathToString(localPath);
 	}
 
 	public static IFileHandle getFile(IPath fullPath) {
 		IEnvironment env = getPathEnvironment(fullPath);
 		if (env == null)
 			return null;
 
 		IPath path = getLocalPath(fullPath);
 		return env.getFile(path);
 	}
 
 	public static Map decodePaths(String concatenatedPaths) {
 		Map result = new HashMap();
 		if (concatenatedPaths != null) {
 			String[] paths = concatenatedPaths
 					.split(EnvironmentPathUtils.PATH_DELIMITER);
 			for (int i = 0; i < paths.length; i++) {
 				IPath path = Path.fromPortableString(paths[i]);
 				IEnvironment env = EnvironmentPathUtils
 						.getPathEnvironment(path);
 				if (env != null) {
 					String localPath = EnvironmentPathUtils
 							.getLocalPathString(path);
 					result.put(env, localPath);
 				}
 			}
 		}
 		return result;
 	}
 
 	public static String encodePaths(Map env2path) {
 		Iterator it = env2path.entrySet().iterator();
 		StringBuffer concatenatedPaths = new StringBuffer();
 		while (it.hasNext()) {
 			Map.Entry entry = (Map.Entry) it.next();
 			IEnvironment key = (IEnvironment) entry.getKey();
 			String localPath = (String) entry.getValue();
 			IPath path = EnvironmentPathUtils.getFullPath(key, new Path(
 					localPath));
 			concatenatedPaths.append(path.toPortableString());
 			if (it.hasNext()) {
 				concatenatedPaths.append(EnvironmentPathUtils.PATH_DELIMITER);
 			}
 		}
 		return concatenatedPaths.toString();
 	}
 
 	public static IFileHandle getFile(IEnvironment environment, IPath path) {
 		if (isFull(path)) {
 			return getFile(path);
 		}
 		return environment.getFile(path);
 	}
 }
