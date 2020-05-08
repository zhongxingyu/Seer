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
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.core.runtime.IPath;
 import org.osgi.framework.Bundle;
 
 public interface IDeployment {
 	/**
 	 * Extract and deploy specific folder or file from plugin bundle to environment file system.
 	 */
 	IPath add(Bundle bundle, String bundlePath) throws IOException;
 	/**
 	 * Deploy specific stream as file into environment file system.
 	 */
 	IPath add(InputStream stream, String filename) throws IOException;
 	
 	/**
 	 * Make specific folders
 	 * @param path
 	 */
 	void mkdirs(IPath path);
 	void dispose();
 	IFileHandle getFile(IPath deploymentPath);
 	IPath getAbsolutePath();
 }
