 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.navigator.internal.dnd;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.operations.IUndoableOperation;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.application.internal.operations.IAddComponentToEnterpriseApplicationDataModelProperties;
import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.navigator.ui.Messages;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.provider.J2EEItemProvider;
 import org.eclipse.jst.j2ee.navigator.internal.plugin.J2EENavigatorPlugin;
 import org.eclipse.jst.j2ee.project.facet.EARFacetUtils;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.TransferData;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.navigator.CommonDropAdapter;
 import org.eclipse.ui.navigator.CommonDropAdapterAssistant;
 import org.eclipse.ui.part.PluginTransfer;
 import org.eclipse.ui.progress.IProgressService;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public class AddProjectToEARDropAssistant extends CommonDropAdapterAssistant {
 
 	private static final Class IPROJECT_CLASS = IProject.class;
 
 	public AddProjectToEARDropAssistant() {
 		super();
 	}
 	
 	public boolean isSupportedType(TransferData aTransferType) {	
 		return LocalSelectionTransfer.getTransfer().isSupportedType(aTransferType);
 	}
 
 	public IStatus handleDrop(CommonDropAdapter aDropAdapter, DropTargetEvent aDropTargetEvent, final Object aTarget) {
 
 		if (LocalSelectionTransfer.getTransfer().isSupportedType(aDropAdapter.getCurrentTransfer())) {
 
 			final IStructuredSelection selection = (IStructuredSelection) LocalSelectionTransfer.getTransfer().getSelection();
 			IProgressService service = PlatformUI.getWorkbench().getProgressService();
 			
 			IProject earProject = getProject(aTarget);
 			IVirtualComponent earComponent = ComponentCore.createComponent(earProject);
 
 			Job addProjectToEarJob = new Job(getJobTitle(earComponent)) {
 				protected IStatus run(IProgressMonitor monitor) {
 
 					IStatus status = null;
 					try {
 						IProject earProject = getProject(aTarget);
 
 						List projects = new ArrayList();
 						 
 						for (Iterator selectionIterator = selection.iterator(); selectionIterator.hasNext();) {
 							Object sourceObject = selectionIterator.next();
 
 							IProject projectToAdd = getProject(sourceObject);
 							if (projectToAdd != null) 
 								projects.add(projectToAdd); 
 						}
 
 						IDataModel dataModel = getAddModuleDataModel(earProject, projects);
 						IUndoableOperation dropOperation = dataModel.getDefaultOperation();
 						status = dropOperation.execute(monitor, null);
 
 						if (!status.isOK())
 							return status;
 					} catch (ExecutionException e) {
 						String msg = e.getMessage() != null ? e.getMessage() : e.toString();
 						status = J2EENavigatorPlugin.createErrorStatus(0, msg, e);
 					}
 					return status;
 				}
 			};
 			service.showInDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), addProjectToEarJob);
 			addProjectToEarJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
 			addProjectToEarJob.schedule();
 		}
 		return Status.OK_STATUS;
 	}
 
 	public IStatus validateDrop(Object target, int operation, TransferData transferType) {
 
 		if (LocalSelectionTransfer.getTransfer().isSupportedType(transferType) || PluginTransfer.getInstance().isSupportedType(transferType)) {
 			ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
 			if (selection != null && !selection.isEmpty() && (selection instanceof IStructuredSelection)) {
 				
 				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 				IProject earProject = getProject(target);
 				int earVersion = getEarVersion(earProject);
 				IStatus status = null;
 				for(Iterator iterator = structuredSelection.iterator(); iterator.hasNext();) {
 					Object next = iterator.next();
 					IProject projectToAdd = getProject(next);
 					if( (status = validateProjectMayBeAdded(earProject, projectToAdd, earVersion)).isOK()) 
 						return status;
 				}
 			}
 			
 		}
 		return Status.CANCEL_STATUS;
 	}
 
 	/**
 	 * @param target
 	 */
 	private IStatus validateProjectMayBeAdded(IProject earProject, IProject projectToAdd, int earVersion) {
 		
 		if (earProject == null || projectToAdd == null || earVersion < 0)
 			return J2EENavigatorPlugin.createErrorStatus(0, Messages.AddProjectToEARDropAssistant_Could_not_add_module_to_Enterprise_, null);
 		else if (!earProject.isAccessible()) {
 			return J2EENavigatorPlugin.createErrorStatus(0, NLS.bind(Messages.AddProjectToEARDropAssistant_The_project_0_cannot_be_accesse_, earProject.getName()), null);
 		} else if (!projectToAdd.isAccessible()) {
 			return J2EENavigatorPlugin.createErrorStatus(0, Messages.AddProjectToEARDropAssistant_The_dragged_project_cannot_be_added_, null);
 		}
 
 		IStatus isValid = validateProjectToAdd(projectToAdd, earVersion);
 		if (!isValid.isOK()) {
 			return isValid;
 		}
 		// TODO Check if the project is already attached to the *.ear
 		// if (editModel.hasMappingToProject(projectToAdd)) {
 		// return false;
 		// } 
 		return Status.OK_STATUS;
 	}	
 	
 	/**
 	 * 
 	 * @return -1 on error 
 	 */
 	protected final int getEarVersion(IProject earProject) {
 		int earVersion = -1;
		EnterpriseArtifactEdit earArtifactEdit = EARArtifactEdit.getEARArtifactEditForRead(earProject);
 		try {
 			if(earArtifactEdit != null)
 				earVersion = earArtifactEdit.getJ2EEVersion();
 			else {
 				J2EENavigatorPlugin.logError(0, "Could not acquire model elements for project \""+earProject.getName()+"\".", null); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		} finally {
 			if (earArtifactEdit != null) {
 				earArtifactEdit.dispose();
 			}
 		}
 		return earVersion;
 	}
 
 	/**
 	 * @param facetedProject
 	 * @return
 	 */
 	protected final boolean hasEarFacet(IProject project) {
 		IFacetedProject facetedProject = null;
 		try {
 			facetedProject = ProjectFacetsManager.create(project);
 		} catch (CoreException e1) { 
 		}
 		return facetedProject != null && facetedProject.hasProjectFacet(EARFacetUtils.EAR_FACET);
 	}
 
 	protected final String calculateValidProjectName(final String originalName) { 
 
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		String validName = originalName;
 		int count = 1;
 		while (root.getProject(validName).exists()) {
 			validName = originalName + count++;
 		}
 		return validName;					
 	}
 	
 	protected IDataModel getAddModuleDataModel(IProject earProject, List projects) {
 		IDataModel datamodel = DataModelFactory.createDataModel(new AddComponentToEnterpriseApplicationDataModelProvider());
 		
 		IVirtualComponent earComponent = ComponentCore.createComponent(earProject);
 		Map componentToURIMap = new HashMap();
 		List components = new ArrayList();
 		IVirtualComponent moduleComponent = null;
 		for(Iterator itr = projects.iterator(); itr.hasNext(); ) {
 			moduleComponent = ComponentCore.createComponent((IProject)itr.next()); 
 			components.add(moduleComponent);
 			componentToURIMap.put(moduleComponent, getDefaultURI(moduleComponent)); 
 		}		
 		datamodel.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComponent);
 		datamodel.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, components);		
 		datamodel.setProperty(IAddComponentToEnterpriseApplicationDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP, componentToURIMap);
 		return datamodel;
 	}
 
 	protected String getJobTitle(IVirtualComponent earComponent) { 
 		return NLS.bind(Messages.AddModuleToEarDropAssistant_Adding_module_to_ea_, earComponent.getName());
 	}  
 	
 	protected IStatus validateProjectToAdd(IProject projectToAdd, int earVersion) {
 		IStatus status = null;
 		try {
 			// check if the project to add is not an EAR itself
 			IFacetedProject facetedProject = ProjectFacetsManager.create(projectToAdd);  
 			if( facetedProject.hasProjectFacet(EARFacetUtils.EAR_FACET) ) 
 				status = Status.CANCEL_STATUS;
 			else 
 				status = Status.OK_STATUS;
 			
 			// check if the project to add is with Java EE version equal or lesser than that of the EAR
 			String verStr = J2EEProjectUtilities.getJ2EEProjectVersion(projectToAdd);
 			if (verStr != null) {
 				int version;
 				if (J2EEProjectUtilities.isApplicationClientProject(projectToAdd))
 					version = J2EEVersionUtil.convertAppClientVersionStringToJ2EEVersionID(verStr);
 				else if (J2EEProjectUtilities.isEJBProject(projectToAdd))
 					version = J2EEVersionUtil.convertEJBVersionStringToJ2EEVersionID(verStr);
 				else if (J2EEProjectUtilities.isDynamicWebProject(projectToAdd))
 					version = J2EEVersionUtil.convertWebVersionStringToJ2EEVersionID(verStr);
 				else if (J2EEProjectUtilities.isJCAProject(projectToAdd))
 					version = J2EEVersionUtil.convertConnectorVersionStringToJ2EEVersionID(verStr);
 				else 
 					version = J2EEVersionUtil.convertVersionStringToInt(verStr);
 				
 				if (version > earVersion) 
 					status = Status.CANCEL_STATUS;
 				else 
 					status = Status.OK_STATUS;
 			}
 		} catch (CoreException e) {
 			String msg = e.getMessage() != null ? e.getMessage() : e.toString();
 			status = J2EENavigatorPlugin.createErrorStatus(0, msg, e);
 		}
 		return status;
 		 
 	}
 
 	protected static IProject getProject(Object element) {
 		if (element == null)
 			return null;
 		IProject project = null;
 		if (element instanceof IAdaptable)
 			project = (IProject) ((IAdaptable) element).getAdapter(IPROJECT_CLASS);
 		else
 			project = (IProject) Platform.getAdapterManager().getAdapter(element, IPROJECT_CLASS);
 		if (project == null) {
 			if(element instanceof EObject) {
 				project = ProjectUtilities.getProject((EObject) element);
 			} else if (element instanceof J2EEItemProvider) {
 				IFile associatedFile = ((J2EEItemProvider)element).getAssociatedFile();
 				if(associatedFile != null)
 					project = associatedFile.getProject();				
 			}
 		}
 		return project;
 	}
 
 	protected static String getDefaultURI(IVirtualComponent component) {
 		IProject project = component.getProject();
 		String name = component.getName();
 
 		if (J2EEProjectUtilities.isDynamicWebProject(project)) {
 			name += IModuleExtensions.DOT_WAR;
 		} else if (J2EEProjectUtilities.isEARProject(project)) {
 			name += IModuleExtensions.DOT_EAR;
 		} else if (J2EEProjectUtilities.isJCAProject(project)) {
 			name += IModuleExtensions.DOT_RAR;
 		} else {
 			name += IModuleExtensions.DOT_JAR;
 		}
 		return name;
 	}
 
 }
