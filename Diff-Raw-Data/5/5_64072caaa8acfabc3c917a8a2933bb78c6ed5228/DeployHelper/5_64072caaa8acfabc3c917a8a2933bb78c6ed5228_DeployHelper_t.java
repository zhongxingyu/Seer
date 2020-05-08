 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.utils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.Enumeration;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Plugin;
 import org.osgi.framework.Bundle;
 
 public class DeployHelper {
 	public static void copy(InputStream input, OutputStream output)
 			throws IOException {
 	  byte[] buffer = new byte[12 * 1024];
 	  int read;
 	  while ((read = input.read(buffer)) != -1) {
 	    output.write(buffer, 0, read);
 	  }
 	}
 
 	public static void copy(InputStream input, File file) throws IOException {
 		OutputStream output = null;
 		try {
 			output = new BufferedOutputStream(new FileOutputStream(file));
 			copy(input, output);
 		}
 		catch( IOException e) {
 			throw e;
 		}
 		finally {
 			if (output != null) {
 				output.close();
 			}
 		}
 	}
 
 	public static void copy(URL url, File file) throws IOException {
	    if (url.toString().indexOf("CVS") != -1 || url.toString().indexOf(".svn") != -1) //$NON-NLS-1$ //$NON-NLS-2$
 	        return;
 
 		InputStream input = null;
 		try {
 			input = new BufferedInputStream(url.openStream());
 			copy(input, file);
 		} finally {
 			if (input != null) {
 				input.close();
 			}
 		}
 	}
 
 	public static IPath deploy(Bundle bundle, String bundlePath, IPath diskPath)
 			throws IOException {
 		// Check if directory
 		final Enumeration paths = bundle.getEntryPaths(bundlePath);
 		final IPath result = diskPath.append(bundlePath);
 
	    if (result.toString().indexOf("CVS") != -1 || result.toString().indexOf(".svn") != -1) //$NON-NLS-1$ //$NON-NLS-2$
             return null;
 
 		File dirFile = result.toFile();
 		if (paths != null) {
 			// result is a directory
 			dirFile.mkdirs();
 			if (!dirFile.exists()) {
 				throw new IOException(MessageFormat.format(Messages.DeployHelper_failedToCreateFolderFor,
 						new Object[] { dirFile.toString() }));
 			}
 
 			while (paths.hasMoreElements()) {
 				final String path = (String) paths.nextElement();
 				if (path.endsWith("/")) { //$NON-NLS-1$
 					deploy(bundle, path, diskPath);
 				} else {
 					copy(bundle.getEntry(path), diskPath.append(path).toFile());
 				}
 			}
 
 			return result;
 		} else {
 			final URL url = bundle.getEntry(bundlePath);
 			if (url != null) {
 				final File file = dirFile;
 
 				if (!file.exists()) {
 					file.getParentFile().mkdirs();
 				}
 
 				copy(url, file);
 				return result;
 			}
 		}
 
 		return null;
 	}
 
 	public static IPath deploy(Plugin plugin, String bundlePath)
 			throws IOException {
 		return deploy(plugin.getBundle(), bundlePath, plugin.getStateLocation());
 	}
 }
