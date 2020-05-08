 /*
  * Copyright (c) 2012, Paul Richardson (phantomjinx). All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301  USA
  */
 package org.phantomjinx.project.refresher;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.commands.IHandlerListener;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.IWorkingSetManager;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 public class RefreshProjectHandler implements IHandler {
 
 	private static final String DOT_PROJECT_FILE = ".project"; //$NON-NLS-1$
 
 	private Logger logger = Logger
 			.getLogger(this.getClass().getCanonicalName());
 
 	@Override
 	public Object execute(ExecutionEvent event) {
 
 		// Choose the directory to refresh from
 		final String projectDirectory = getChosenDirectory();
 		if (projectDirectory == null) {
 			return null;
 		}
 
 		Job job = new Job("Project synchronizer") { //$NON-NLS-1$
 
 			@Override
 			public IStatus run(IProgressMonitor monitor) {
 				try {
 					synchronizeProjects(projectDirectory, monitor);
 				}
 				catch (CoreException ex) {
 					logger.severe(ex.getMessage());
 					ex.printStackTrace();
 					return Status.CANCEL_STATUS;
 				}
 				return Status.OK_STATUS;
 			}
 		};
 
 		job.setPriority(Job.LONG);
 		job.schedule();
 		return null;
 	}
 
 	/**
 	 * Synchronize all projects:
 	 * <ul>
 	 * <li>import new projects</li>
 	 * <li>delete invalid projects</li>
 	 * <li>refresh modified projects</li>
 	 * <li>organise projects into their working sets</li>
 	 * </ul>
 	 * 
 	 * @param projectDirectory
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void synchronizeProjects(final String projectDirectory,
 			IProgressMonitor monitor) throws CoreException {
 		// Import those projects not already in the workspace
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
 				.getProjects();
 		final Map<String, IProject> projectMap = new HashMap<String, IProject>();
 
 		if (projects != null) {
 			for (IProject project : projects) {
 				projectMap.put(project.getName(), project);
 			}
 		}
 
 		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
 			@Override
 			public void run(IProgressMonitor monitor) throws CoreException {
 				monitor.beginTask("Synchronizing projects to filesystem. ", 3); //$NON-NLS-1$
 				// Import new projects
 				monitor.subTask("Importing new projects..."); //$NON-NLS-1$
 				importProjects(projectDirectory, projectMap);
 				monitor.worked(1);
 
 				// Refresh all projects in the workspace
 				// Remove those projects no longer in the filesystem
 				monitor.subTask("Refreshing projects..."); //$NON-NLS-1$
 				refreshProjects(projectMap, monitor);
 				monitor.worked(1);
 
 				monitor.subTask("Organising projects in working sets..."); //$NON-NLS-1$
 				organiseProjects(projectMap);
 				monitor.done();
 
 				projectMap.clear();
 			}
 		}, ResourcesPlugin.getWorkspace().getRoot(), IWorkspace.AVOID_UPDATE,
 				monitor);
 	}
 
 	/**
 	 * For each project, identify their parent directory and use this as a
 	 * working set category. Add the working set if required and display all the
 	 * working sets in the package explorer.
 	 * 
 	 * @param projectMap
 	 */
 	private void organiseProjects(final Map<String, IProject> projectMap) {
 		IWorkingSetManager wsManager = PlatformUI.getWorkbench().getWorkingSetManager();
 		Map<String, List<IProject>> wsToProjectsMap = new HashMap<String, List<IProject>>();
 
 		for (IProject project : projectMap.values()) {
 			IPath location = project.getLocation();
 			File fileLocation = location.toFile();
 			if (fileLocation == null) {
 				continue;
 			}
 
			File parentDir = fileLocation.getParentFile();
            File projectDir = parentDir.getParentFile();
            
            String wsName = projectDir != null ? projectDir.getName() + "-" + parentDir.getName() : parentDir.getName();
 			List<IProject> projects = wsToProjectsMap.get(wsName);
 			if (projects == null) {
 				projects = new ArrayList<IProject>();
 				wsToProjectsMap.put(wsName, projects);
 			}
 
 			projects.add(project);
 		}
 
 		final List<IWorkingSet> workingSets = new ArrayList<IWorkingSet>();
 		for (Map.Entry<String, List<IProject>> entry : wsToProjectsMap.entrySet()) {
 
 			IWorkingSet workingSet = wsManager.getWorkingSet(entry.getKey());
 			IAdaptable[] adaptedProjects = entry.getValue().toArray(new IAdaptable[0]);
 
 			if (workingSet == null) {
 				workingSet = wsManager.createWorkingSet(entry.getKey(),
 						adaptedProjects);
 				workingSet.setId("org.eclipse.jdt.ui.JavaWorkingSetPage"); //$NON-NLS-1$
 				wsManager.addWorkingSet(workingSet);
 				workingSets.add(workingSet);
 			}
 			else {
 				IAdaptable[] adaptedNewElements = workingSet.adaptElements(adaptedProjects);
 				if (adaptedNewElements.length > 1) {
 					workingSet.setElements(adaptedProjects);
 				}
 			}
 		}
 
 		Display.getDefault().syncExec(new Runnable() {
 			@Override
 			public void run() {
 				IViewPart viewPart = PlatformUI.getWorkbench()
 						.getActiveWorkbenchWindow().getActivePage()
 						.findView(JavaUI.ID_PACKAGES);
 				if (viewPart == null) {
 					try {
 						viewPart = PlatformUI.getWorkbench()
 								.getActiveWorkbenchWindow().getActivePage()
 								.showView(JavaUI.ID_PACKAGES);
 					}
 					catch (PartInitException ex) {
 						logger.severe("Cannot display Package Explorer view"); //$NON-NLS-1$
 						ex.printStackTrace();
 					}
 				}
 
 				PlatformUI
 						.getWorkbench()
 						.getActiveWorkbenchWindow()
 						.getActivePage()
 						.setWorkingSets(workingSets.toArray(new IWorkingSet[0]));
 			}
 		});
 	}
 
 	/**
 	 * Refresh all the projects in the given map but sync'ing them to the
 	 * location on the filesystem.
 	 * 
 	 * @param projectMap
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void refreshProjects(Map<String, IProject> projectMap,
 			IProgressMonitor monitor) throws CoreException {
 		Collection<IProject> projects = new ArrayList<IProject>(
 				projectMap.values());
 
 		for (IProject project : projects) {
 			IPath location = project.getLocation();
 
 			if (location.toFile().exists() && !isShellProject(project)) {
 				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
 			}
 			else {
 				projectMap.remove(project.getName());
 				project.delete(false, true, monitor);
 			}
 		}
 
 		projects.clear();
 	}
 
 	/**
 	 * Its possible that eclipse's save workspace system kicks in and recreates
 	 * .project files in deleted projects that have not yet been removed.
 	 * However, we want to destroy these two so identify these stale projects
 	 * 
 	 * @param project
 	 * @return
 	 */
 	private boolean isShellProject(IProject project) {
 		IPath location = project.getLocation();
 		if (location == null) {
 			return true;
 		}
 
 		File projectDir = location.toFile();
 		if (projectDir == null || !projectDir.exists()) {
 			return true;
 		}
 
 		for (File projectFile : projectDir.listFiles()) {
 			if (projectFile.getName().equals(DOT_PROJECT_FILE)) {
 				continue;
 			}
 			else {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Find and import any projects from the given root project directory
 	 * 
 	 * @param projectDirectory
 	 * @param projectMap
 	 * @throws CoreException
 	 */
 	private void importProjects(String projectDirectory, Map<String, IProject> projectMap) throws CoreException {
 
 		File projectDir = new File(projectDirectory);
 		if (!projectDir.exists()) {
 			logger.severe("Chosen directory does not exist!"); //$NON-NLS-1$
 			return;
 		}
 
 		importProject(projectDir, projectMap);
 	}
 
 	/**
 	 * Import the project from the given project directory. If this proposed
 	 * directory is not a project then it may contain projects so recurse down.
 	 * 
 	 * @param projectDir
 	 * @param projectMap
 	 * @throws CoreException
 	 */
 	private void importProject(File projectDir, Map<String, IProject> projectMap)
 			throws CoreException {
 		File projectFile = new File(projectDir, DOT_PROJECT_FILE);
 
 		if (projectFile.exists()) {
 			// Import the project
 			Path projectPath = new Path(projectFile.getAbsolutePath());
 			IProjectDescription description = ResourcesPlugin.getWorkspace()
 					.loadProjectDescription(projectPath);
 
 			if (projectMap.containsKey(description.getName())) {
 				// Already imported this project
 				return;
 			}
 
 			IProject project = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(description.getName());
 			project.create(description, null);
 			project.open(null);
 
 			projectMap.put(project.getName(), project);
 		}
 		else {
 			// Not a project but maybe contains projects?
 			for (File subDir : projectDir.listFiles()) {
 				if (!subDir.isDirectory()) {
 					continue;
 				}
 
 				importProject(subDir, projectMap);
 			}
 		}
 	}
 
 	/**
 	 * Get the root directory to refresh projects against
 	 * 
 	 * @return
 	 */
 	private String getChosenDirectory() {
 		DirectoryDialog dlg = new DirectoryDialog(PlatformUI.getWorkbench()
 				.getDisplay().getActiveShell());
 
 		// Change the title bar text
 		dlg.setText("Select directory to be synchronised against."); //$NON-NLS-1$
 
 		// Customizable message displayed in the dialog
 		dlg.setMessage("Select a directory"); //$NON-NLS-1$
 
 		// Calling open() will open and run the dialog.
 		// It will return the selected directory, or
 		// null if user cancels
 		String dir = dlg.open();
 		return dir;
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isHandled() {
 		return true;
 	}
 
 	@Override
 	public void addHandlerListener(IHandlerListener handlerListener) {
 		// Not required
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void removeHandlerListener(IHandlerListener handlerListener) {
 		// No Required
 	}
 
 }
