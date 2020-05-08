 /***************************************************************************************************
  * Copyright (c) 2005 Eteration A.S. and Gorkem Ercan. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Gorkem Ercan - initial API and implementation
  *               
  **************************************************************************************************/
 package org.eclipse.jst.server.generic.core.internal;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jst.server.core.RuntimeClasspathProviderDelegate;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.wst.server.core.IRuntime;
 /**
  * Provides the Classpath containers to be added into project classpaths.
  *
  * @author Gorkem Ercan
  */
 public class GenericServerRuntimeTargetHandler extends RuntimeClasspathProviderDelegate {
 
 	/* (non-Javadoc)
 	 * @see ClasspathRuntimeTargetHandler#getClasspathContainerLabel(IRuntime, java.lang.String)
 	 */
 	public String getClasspathContainerLabel(IRuntime runtime, String id) {
 		ServerRuntime definition= ServerTypeDefinitionUtil.getServerTypeDefinition(runtime);
 		return definition.getName();
 	}
 
 	/* (non-Javadoc)
 	 * @see ClasspathRuntimeTargetHandler#resolveClasspathContainer(IRuntime, java.lang.String)
 	 */
 	public IClasspathEntry[] resolveClasspathContainer(IRuntime runtime,String id){		
		return resolveClasspathContainer(runtime);
	}
	/* (non-Javadoc)
	 * @see ClasspathRuntimeTargetHandler#resolveClasspathContainer(IRuntime)
	 */
	public IClasspathEntry[] resolveClasspathContainer(IRuntime runtime){		
 		return ServerTypeDefinitionUtil.getServerClassPathEntry(runtime);
 	}
     
 	/*
      * @see ClasspathRuntimeTargetHandler#getDelegateClasspathEntries(IRuntime runtime, IProgressMonitor monitor)
 	 */
 	public IClasspathEntry[] getDelegateClasspathEntries(IRuntime runtime, IProgressMonitor monitor) {
 		GenericServerRuntime genericRuntime = (GenericServerRuntime)runtime.loadAdapter(GenericServerRuntime.class, monitor);
 		if (genericRuntime == null)
 			return new IClasspathEntry[0];
 		IVMInstall vmInstall = genericRuntime.getVMInstall();
 		if (vmInstall != null) {
 			String name = vmInstall.getName();
 			String typeId = vmInstall.getVMInstallType().getId();
 			return new IClasspathEntry[] { JavaCore.newContainerEntry(new Path(JavaRuntime.JRE_CONTAINER).append(typeId).append(name)) };
 		}
 		return null;
 	}
 }
