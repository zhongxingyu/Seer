 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.internal.earcreation.IEARNatureConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EENature;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.server.core.IModule;
 
 /**
  * @version 1.0
  * @author
  */
 public class EnterpriseApplicationDeployableFactory extends J2EEDeployableFactory {
 
 	protected static final String ID = "com.ibm.wtp.server.j2ee.application"; //$NON-NLS-1$
 
 	protected static final IPath[] PATHS = new IPath[]{new Path("META-INF/application.xml"), //$NON-NLS-1$
 				new Path("META-INF/.modulemaps") //$NON-NLS-1$
 	};
 
 	public EnterpriseApplicationDeployableFactory() {
 		super();
 	}
 
 
 
 	public IModule createModule(J2EENature nature) {
 		return null;
 	}
 
 
 	protected IPath[] getListenerPaths() {
 		return PATHS;
 	}
 
 
 	protected List createModuleDelegates(EList workBenchModules, IProject project) {
 		EnterpriseApplicationDeployable moduleDelegate = null;
 		IModule module = null;
 		List moduleList = new ArrayList(workBenchModules.size());
 		// J2EENature nature = (J2EENature)project.getNature(getNatureID());
 
 		for (int i = 0; i < workBenchModules.size(); i++) {
 			try {
 				WorkbenchComponent wbModule = (WorkbenchComponent) workBenchModules.get(i);
 				if (wbModule.getComponentType()==null || wbModule.getComponentType().getComponentTypeId() == null || !wbModule.getComponentType().getComponentTypeId().equals(EnterpriseApplicationDeployable.EAR_MODULE_TYPE))
 					continue;
 				moduleDelegate = new EnterpriseApplicationDeployable(project, ID, wbModule);
 				//moduleDelegate.getModules();
 				module = createModule(wbModule.getName(), wbModule.getName(), moduleDelegate.getType(), moduleDelegate.getVersion(), moduleDelegate.getProject());
 				moduleList.add(module);
 				moduleDelegate.initialize(module);
 				// adapt(moduleDelegate, (WorkbenchComponent) workBenchModules.get(i));
 			} catch (Exception e) {
 				Logger.getLogger().write(e);
 			} finally {
 				if (module != null) {
 					if (getModuleDelegate(module) == null)
 						moduleDelegates.add(moduleDelegate);
 				}
 			}
 		}
 		return moduleList;
 	}
 
 	public IModule[] getModules() {
		//turned off for M4
		/*cacheModules();
 		ArrayList moduleList = new ArrayList();
 		for (Iterator iter = projects.values().iterator(); iter.hasNext();) {
 			IModule[] element = (IModule[]) iter.next();
 			for (int j = 0; j < element.length; j++) {
 				moduleList.add(element[j]);
 			}
 		}
 		IModule[] modules = new IModule[moduleList.size()];
 		moduleList.toArray(modules);
		return modules;*/
       return null;
 
 	}
 
 
 
 	protected String getNatureID() {
 		return IModuleConstants.MODULE_NATURE_ID;
 	}
 
 
 
 }
