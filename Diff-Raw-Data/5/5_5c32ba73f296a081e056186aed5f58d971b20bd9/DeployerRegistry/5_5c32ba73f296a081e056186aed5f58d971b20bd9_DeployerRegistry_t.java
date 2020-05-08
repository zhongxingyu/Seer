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
 /*
  * Created on Mar 30, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.deploy;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.server.core.IRuntime;
 
 /**
  * @author cbridgha
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class DeployerRegistry {
 	/**
 	 *  
 	 */
 	private static DeployerRegistry INSTANCE;
 	private HashMap deployModuleExtensions = new HashMap();
 
 	public DeployerRegistry() {
 		super();
 	}
 
 	/**
 	 * @param deployer
 	 * @param serverTarget
 	 * @param natureID
 	 */
 	public void register(IConfigurationElement deployer, List serverTargets, List natures) {
 		HashMap targetDeployers;
 		for (Iterator iter = natures.iterator(); iter.hasNext();) {
 			String natureID = (String) iter.next();
 			for (Iterator iterator = serverTargets.iterator(); iterator.hasNext();) {
 				String runtimeID = (String) iterator.next();
 				targetDeployers = getDeployModuleExtensions(natureID);
 				getTargetDeployers(targetDeployers, runtimeID).add(deployer);
 			}
 		}
 	}
 
 	private List getDeployers(String natureID, String serverTarget) {
 		HashMap targetDeployers = getDeployModuleExtensions(natureID);
 		return getTargetDeployers(targetDeployers, serverTarget);
 	}
 
 	public static DeployerRegistry instance() {
 		if (INSTANCE == null) {
 			INSTANCE = new DeployerRegistry();
 			readRegistry();
 		}
 		return INSTANCE;
 	}
 
 	/**
 	 *  
 	 */
 	private static void readRegistry() {
 		DeployerRegistryReader reader = new DeployerRegistryReader();
 		reader.readRegistry();
 	}
 
 	/**
 	 * @return
 	 */
 	public static List getSelectedModules(Object[] mySelections) {
 		List modules = new ArrayList();
 		for (int i = 0; i < mySelections.length; i++) {
 			Object object = mySelections[i];
 			if (object instanceof EObject) {
 				object = ProjectUtilities.getProject(object);
 			}
 			if (object instanceof IProject) {
 				IVirtualComponent component = ComponentCore.createComponent((IProject)object);
 				EnterpriseArtifactEdit edit = null;
 				try {
 					edit = (EnterpriseArtifactEdit)ComponentUtilities.getArtifactEditForRead(component);
					if (edit == null)
						continue;
 					EObject root = edit.getDeploymentDescriptorRoot();
					if (root == null || modules.contains(root))
 						continue;
 					// Order Ears first...
 					if (J2EEProjectUtilities.isEARProject(component.getProject()))
 						modules.add(0, root);
 					else
 						modules.add(root);
 				} finally {
 					if (edit != null)
 						edit.dispose();
 				}
 			}
 		}
 		return modules;
 	}
 
 	/**
 	 * @param targetDeployers
 	 * @param serverTarget
 	 */
 	private List getTargetDeployers(HashMap targetDeployers, String serverTarget) {
 		if (targetDeployers.get(serverTarget) == null)
 			targetDeployers.put(serverTarget, new ArrayList());
 		return (List) targetDeployers.get(serverTarget);
 	}
 
 	/**
 	 * @param natureID
 	 * @return
 	 */
 	private HashMap getDeployModuleExtensions(String natureID) {
 		if (getDeployModuleExtensions().get(natureID) == null)
 			getDeployModuleExtensions().put(natureID, new HashMap());
 		return (HashMap) getDeployModuleExtensions().get(natureID);
 	}
 
 	/**
 	 * @return Returns the deployExtensions.
 	 */
 	public HashMap getDeployModuleExtensions() {
 		return deployModuleExtensions;
 	}
 
 	/**
 	 * @param deployExtensions
 	 *            The deployExtensions to set.
 	 */
 	public void setDeployModuleExtensions(HashMap deployExtensions) {
 		this.deployModuleExtensions = deployExtensions;
 	}
 
 	/**
 	 * @param module
 	 * @param runtime
 	 * @return
 	 */
 	public List getDeployModuleExtensions(EObject module, IRuntime runtime) {
 		IVirtualComponent comp = ComponentUtilities.findComponent(module);
 		String typeID = ""; //$NON-NLS-1$
 		if (J2EEProjectUtilities.isEARProject(comp.getProject()))
 			typeID = J2EEProjectUtilities.ENTERPRISE_APPLICATION;
 		else if (J2EEProjectUtilities.isApplicationClientProject(comp.getProject()))
 			typeID = J2EEProjectUtilities.APPLICATION_CLIENT;
 		else if (J2EEProjectUtilities.isDynamicWebProject(comp.getProject()))
 			typeID = J2EEProjectUtilities.DYNAMIC_WEB;
 		else if (J2EEProjectUtilities.isStaticWebProject(comp.getProject()))
 			typeID = J2EEProjectUtilities.STATIC_WEB;
 		else if (J2EEProjectUtilities.isEJBProject(comp.getProject()))
 			typeID = J2EEProjectUtilities.EJB;
 		else if (J2EEProjectUtilities.isJCAProject(comp.getProject()))
 			typeID = J2EEProjectUtilities.JCA;
 		else if (J2EEProjectUtilities.isUtilityProject(comp.getProject()))
 			typeID = J2EEProjectUtilities.UTILITY;
 		String runtimeID = runtime.getRuntimeType().getId();
 		return getDeployers(typeID, runtimeID);
 	}
 }
