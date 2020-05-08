 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.launch.ui.selection;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.cdt.utils.elf.Elf.Attribute;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.tcf.te.core.cdt.elf.ElfUtils;
 import org.eclipse.tcf.te.launch.core.selection.LaunchSelection;
 import org.eclipse.tcf.te.launch.core.selection.ProjectSelectionContext;
 import org.eclipse.tcf.te.launch.core.selection.RemoteSelectionContext;
 import org.eclipse.tcf.te.launch.core.selection.interfaces.ILaunchSelection;
 import org.eclipse.tcf.te.launch.core.selection.interfaces.ISelectionContext;
 import org.eclipse.tcf.te.launch.ui.activator.UIPlugin;
 import org.eclipse.tcf.te.launch.ui.model.LaunchNode;
 import org.eclipse.tcf.te.launch.ui.nls.Messages;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNode;
 import org.eclipse.tcf.te.runtime.model.interfaces.IModelNodeProvider;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.services.interfaces.IPropertiesAccessService;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * Launch selection manager implementation.
  */
 public class LaunchSelectionManager {
 	/**
 	 * Part id: Target Explorer view
 	 */
 	public static final String PART_ID_TE_VIEW = "org.eclipse.tcf.te.ui.views.View"; //$NON-NLS-1$
 
 	/**
 	 * Part id: Project Explorer view
 	 */
 	public static final String PART_ID_PROJECT_VIEW = "org.eclipse.ui.navigator.ProjectExplorer"; //$NON-NLS-1$
 
 	/**
 	 * Part id: Debug view
 	 */
 	public static final String PART_ID_DEBUG_VIEW = "org.eclipse.debug.ui.DebugView"; //$NON-NLS-1$
 
 	// Remember the last remote context and project selections.
 	// <p>
 	// Some operations to determine the corresponding selection contexts may consume
 	// a lot of time. Avoid to redo them if not really needed.
 	private IStructuredSelection lastRemoteCtxInputSelection = null;
 	private Map<IModelNode, Set<IModelNode>> lastRemoteCtxOutputSelections = null;
 	private IStructuredSelection lastProjectInputSelection = null;
 	private long lastProjectHash = 0;
 	private Map<IProject, Set<IPath>> lastProjectOutputSelections = null;
 
 	/*
 	 * Thread save singleton instance creation.
 	 */
 	private static class LazyInstanceHolder {
 		public static LaunchSelectionManager instance = new LaunchSelectionManager();
 	}
 
 	/**
 	 * Returns the singleton instance for the manager.
 	 */
 	public static LaunchSelectionManager getInstance() {
 		return LazyInstanceHolder.instance;
 	}
 
 	/**
 	 * Constructor.
 	 */
 	LaunchSelectionManager() {
 	}
 
 	/**
 	 * Returns a launch selection for the given launch mode and a preferred part.
 	 *
 	 * @param type The launch configuration type or <code>null</code>, to determine defaults.
 	 * @param mode The launch mode to generate the launch selection for.
 	 * @param preferredPartId The part id whose selections should be preferred. Can be <code>null</code>.
 	 * @return A launch selection.
 	 */
 	public ILaunchSelection getLaunchSelection(ILaunchConfigurationType type, String mode, String preferredPartId) {
 		return new LaunchSelection(mode, getSelectionContexts(type, mode, preferredPartId));
 	}
 
 	private ISelectionContext[] getSelectionContexts(ILaunchConfigurationType type, String mode, String preferredPartId) {
 		List<ISelectionContext> contexts = new ArrayList<ISelectionContext>();
 
 		// Get the selected remote contexts
 		contexts.addAll(getSelectionContextsFor(PART_ID_TE_VIEW, type, mode, PART_ID_TE_VIEW.equalsIgnoreCase(preferredPartId)));
 
 		// Get the selected project contexts
 		contexts.addAll(getSelectionContextsFor(PART_ID_PROJECT_VIEW, type, mode, PART_ID_PROJECT_VIEW.equalsIgnoreCase(preferredPartId)));
 
 		return contexts.toArray(new ISelectionContext[contexts.size()]);
 	}
 
 	public List<ISelectionContext> getSelectionContextsFor(String partId, ILaunchConfigurationType type, String mode, boolean preferedPart) {
 		List<ISelectionContext> contexts = new ArrayList<ISelectionContext>();
 
 		if (PART_ID_TE_VIEW.equalsIgnoreCase(partId)) {
 			// Get the selected remote contexts
 			Map<IModelNode, Set<IModelNode>> remoteCtxSelections = getRemoteCtxSelections(getPartSelection(PART_ID_TE_VIEW));
 
 			for (Entry<IModelNode, Set<IModelNode>> remoteCtx : remoteCtxSelections.entrySet()) {
 				contexts.add(new RemoteSelectionContext(remoteCtx.getKey(), remoteCtx.getValue().toArray(), preferedPart));
 			}
 		}
 		else if (PART_ID_PROJECT_VIEW.equalsIgnoreCase(partId)) {
 
 			// Get the selected project contexts
 			Map<IProject, Set<IPath>> projectSelections = getProjectSelections(getPartSelection(PART_ID_PROJECT_VIEW), true);
			for (IProject prj : projectSelections.keySet()) {
				contexts.add(new ProjectSelectionContext(prj, projectSelections.get(prj).toArray(), preferedPart));
 			}
 		}
 
 		return contexts;
 	}
 
 	/**
 	 * Analyze the given UI selection and extract the remote context selection from it.
 	 *
 	 * @param structSel The UI selection or <code>null</code>.
 	 * @return The remote context selections or an empty map.
 	 */
 	private Map<IModelNode, Set<IModelNode>> getRemoteCtxSelections(IStructuredSelection structSel) {
 		if (structSel != null && structSel.equals(lastRemoteCtxInputSelection) && lastRemoteCtxOutputSelections != null) {
 			return lastRemoteCtxOutputSelections;
 		}
 
 		Map<IModelNode, Set<IModelNode>> remoteCtxSelections = new HashMap<IModelNode, Set<IModelNode>>();
 		if (structSel != null && !structSel.isEmpty()) {
 			for (Object sel : structSel.toArray()) {
 				IModelNode remoteCtx = null;
 				IModelNode node = null;
 
 				if (sel instanceof LaunchNode && ((LaunchNode)sel).getModel().getModelRoot() instanceof IModelNode) {
 					node = (IModelNode)((LaunchNode)sel).getModel().getModelRoot();
 				}
 				else if (sel instanceof IModelNodeProvider) {
 					node = ((IModelNodeProvider)sel).getModelNode();
 				} else if (sel instanceof IModelNode) {
 					node = (IModelNode)sel;
 				}
 
 				if (node != null) {
 					IPropertiesAccessService service = ServiceManager.getInstance().getService(node, IPropertiesAccessService.class);
 					if (service != null) {
 						remoteCtx = node;
 						IModelNode parent = (IModelNode)service.getParent(node);
 						while (parent != null) {
 							remoteCtx = parent;
 							parent = (IModelNode)service.getParent(node);
 						}
 					}
 				}
 
 				Set<IModelNode> nodes;
 				if (remoteCtx != null) {
 					if (!remoteCtxSelections.containsKey(remoteCtx)) {
 						nodes = new HashSet<IModelNode>();
 						remoteCtxSelections.put(remoteCtx, nodes);
 					}
 					else {
 						nodes = remoteCtxSelections.get(remoteCtx);
 					}
 					if (node != null) {
 						nodes.add(node);
 					}
 				}
 			}
 		}
 
 		lastRemoteCtxInputSelection = structSel;
 		lastRemoteCtxOutputSelections = remoteCtxSelections;
 
 		return lastRemoteCtxOutputSelections;
 	}
 
 	/**
 	 * Calculates a hash code based on the determined projects of
 	 * the given UI selection.
 	 *
 	 * @param structSel The UI selection or <code>null</code>.
 	 * @return The calculated hash code.
 	 */
 	private long getProjectHash(IStructuredSelection structSel) {
 		long hash = 0;
 		if (structSel != null) {
 			List<IProject> projects = new ArrayList<IProject>();
 			for (Object sel : structSel.toArray()) {
 				IProject prj = null;
 				IResource resource = null;
 
 				if (sel instanceof LaunchNode && ((LaunchNode)sel).getModel().getModelRoot() instanceof IResource) {
 					sel = ((LaunchNode)sel).getModel().getModelRoot();
 				}
 
 				// If the selection is not an IResource itself, try to adapt to it.
 				// This will possibly trigger an plugin activation on loadAdapter(...).
 				if (sel instanceof IProject) {
 					prj = (IProject)sel;
 				}
 				else {
 					if (sel instanceof IResource) {
 						resource = (IResource)sel;
 					}
 					else if (sel instanceof IAdaptable) {
 						resource = (IResource)((IAdaptable)sel).getAdapter(IResource.class);
 					}
 					else {
 						resource = (IResource)Platform.getAdapterManager().loadAdapter(sel, IResource.class.getName());
 					}
 
 					// Get the project from the resource
 					prj = resource != null ? resource.getProject() : null;
 				}
 
 				// If the project could be determined, add the project's
 				// hash code to the cumulative hash code.
 				if (prj != null && !projects.contains(prj)) {
 					projects.add(prj);
 					hash += prj.hashCode();
 				}
 				else {
 					hash += sel.hashCode();
 				}
 			}
 
 			projects.clear();
 		}
 		return hash;
 	}
 
 	/**
 	 * Analyze the given UI selection and extract the project selection from it.
 	 *
 	 * @param structSel The UI selection or <code>null</code>.
 	 * @param storeToCache If <code>true</code> the project selection will be cached.
 	 *
 	 * @return The project context selection or an empty map.
 	 */
 	public Map<IProject, Set<IPath>> getProjectSelections(IStructuredSelection selection, boolean storeToCache) {
 		long projectHash = 0;
 		if (selection != null && selection.equals(lastProjectInputSelection) &&
 						lastProjectOutputSelections != null) {
 			projectHash = getProjectHash(selection);
 			if (lastProjectHash == 0 || lastProjectHash == projectHash) {
 				return lastProjectOutputSelections;
 			}
 		}
 
 		Map<IProject, Set<IPath>> projectSelections = new HashMap<IProject, Set<IPath>>();
 		if (selection != null && !selection.isEmpty()) {
 			for (Object sel : selection.toArray()) {
 				IProject prj = null;
 				IPath location = null;
 
 				if (sel instanceof LaunchNode && ((LaunchNode)sel).getModel().getModelRoot() instanceof IProject) {
 					prj = (IProject)((LaunchNode)sel).getModel().getModelRoot();
 				}
 				else if (sel instanceof IProject) {
 					prj = (IProject)sel;
 				}
 				else if (sel instanceof IFile) {
 					IFile file = (IFile)sel;
 					prj = file.getProject();
 					if (getLocation(file) != null) {
 						File filePath = getLocation(file).toFile();
 						try {
 							int elfType = ElfUtils.getELFType(filePath);
 							if (file.exists() &&
 											(elfType == Attribute.ELF_TYPE_EXE || elfType == Attribute.ELF_TYPE_OBJ)) {
 								location = file.getLocation();
 							}
 						}
 						catch (IOException e) {
 							if (Platform.inDebugMode()) {
 								IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
 												NLS.bind(Messages.LaunchSelectionManager_error_failedToDetermineElfType, filePath.getAbsolutePath()), e);
 								UIPlugin.getDefault().getLog().log(status);
 							}
 						}
 					}
 				} else {
 					// Try to adapt the selection to an resource
 					IResource resource = (IResource)Platform.getAdapterManager().loadAdapter(sel, IResource.class.getName());
 					if (resource != null) {
 						prj = resource.getProject();
 						location = getLocation(resource);
 						if (location != null) {
 							try {
 								int elfType = ElfUtils.getELFType(location.toFile());
 								if (elfType != Attribute.ELF_TYPE_EXE && elfType != Attribute.ELF_TYPE_OBJ) {
 									location = null;
 								}
 							}
 							catch (Exception e) {
 								if (Platform.inDebugMode()) {
 									IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(),
 													NLS.bind(Messages.LaunchSelectionManager_error_failedToDetermineElfType, location.toFile().getAbsolutePath()), e);
 									UIPlugin.getDefault().getLog().log(status);
 								}
 								location = null;
 							}
 						}
 					}
 				}
 
 				if (prj != null && (location == null || location.toFile().isDirectory())) {
 					// Try to get it from the build targets
 					location = getFirstExeLocation(prj);
 				}
 
 				Set<IPath> nodes;
 				if (prj != null) {
 					if (!projectSelections.containsKey(prj)) {
 						nodes = new HashSet<IPath>();
 						projectSelections.put(prj, nodes);
 					}
 					else {
 						nodes = projectSelections.get(prj);
 					}
 					if (location != null) {
 						nodes.add(location);
 					}
 				}
 			}
 		}
 
 		if (storeToCache) {
 			lastProjectInputSelection = selection;
 			lastProjectHash = projectHash != 0 ? projectHash : getProjectHash(selection);
 			lastProjectOutputSelections = projectSelections;
 		}
 
 		return storeToCache ? lastProjectOutputSelections : projectSelections;
 	}
 
 	/**
 	 * Get the selection of a workbench part.
 	 * <p>
 	 * <b>Note:</b> This method will return null if called from a non-UI thread!
 	 *
 	 * @param partId The part id. Must not be <code>null</code>.
 	 * @return The structured selection if the workbench part or <code>null</code>.
 	 */
 	public static IStructuredSelection getPartSelection(String partId) {
 		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if (partId != null && window != null && window.getActivePage() != null) {
 			ISelection sel = window.getActivePage().getSelection(partId);
 
 			if (sel instanceof IStructuredSelection) {
 				return (IStructuredSelection)sel;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return the location on the file system of the first executable
 	 * build-target found in the given project. Existing build-targets have
 	 * precedence over non-existing build targets.
 	 *
 	 * @param prj The project. Must not be <code>null</code>
 	 * @return IPath The first found executable or <code>null</code>.
 	 */
 	public IPath getFirstExeLocation(IProject prj) {
 		return null;
 	}
 
 	/*
 	 * Get the location of an IResource or null.
 	 */
 	private IPath getLocation(IResource resource) {
 		return (resource != null) ? resource.getLocation() : null;
 	}
 }
