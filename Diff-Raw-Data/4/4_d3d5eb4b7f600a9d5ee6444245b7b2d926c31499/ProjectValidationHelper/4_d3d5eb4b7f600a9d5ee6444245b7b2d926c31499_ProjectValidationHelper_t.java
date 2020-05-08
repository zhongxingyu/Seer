 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.validation;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.validation.internal.IProjectValidationHelper;
 
 public class ProjectValidationHelper implements IProjectValidationHelper {
 
 	private static IContainer[] EMPTY_RESULT = new IContainer[] {};
 	private static ProjectValidationHelper INSTANCE;
 	private IContainer[] outputs;
 	private IContainer[] sources;
 	
 	public static ProjectValidationHelper getInstance(){
 		if (INSTANCE == null)
 			INSTANCE = new ProjectValidationHelper();
 		return INSTANCE;
 	}
 	public void disposeInstance(){
 		INSTANCE = null;
 	}
 	private IContainer[] getCachedOutputContainers(IProject project) {
 		if (outputs != null)
 			return outputs;
 		if (project == null || JemProjectUtilities.getJavaProject(project)==null) {
 			outputs = EMPTY_RESULT;
 			return EMPTY_RESULT;
 		}
 		IVirtualComponent comp = ComponentCore.createComponent(project);
 		if (comp == null || !comp.exists()) {
 			outputs = EMPTY_RESULT;
 			return EMPTY_RESULT;
 		}
 		outputs = J2EEProjectUtilities.getOutputContainers(project);
 		return outputs;
 	
 	}
 	public IContainer[] getOutputContainers(IProject project) {
		ProjectValidationHelper inst = getInstance();
		if(inst == null){
			return null;
		}
 		return getInstance().getCachedOutputContainers(project);
 	}
 	
 	private IContainer[] getCachedSourceContainers(IProject project) {
 		if (sources != null)
 			return sources;
 		if (project == null || JemProjectUtilities.getJavaProject(project)==null) {
 			sources = EMPTY_RESULT;
 			return EMPTY_RESULT;
 		}
 		IVirtualComponent comp = ComponentCore.createComponent(project);
 		if (comp == null || !comp.exists()) {
 			sources = EMPTY_RESULT;
 			return EMPTY_RESULT;
 		}
 		IPackageFragmentRoot[] roots = J2EEProjectUtilities.getSourceContainers(project);
 		List result = new ArrayList();
 		for (int i=0; i<roots.length; i++) {
 			if (roots[i].getResource() != null && roots[i].getResource() instanceof IContainer)
 				result.add(roots[i].getResource());
 		}
 		sources = (IContainer[]) result.toArray(new IContainer[result.size()]);
 		return sources;
 		
 	}
 	public IContainer[] getSourceContainers(IProject project) {
 		return getInstance().getCachedSourceContainers(project);
 	}
 
 }
