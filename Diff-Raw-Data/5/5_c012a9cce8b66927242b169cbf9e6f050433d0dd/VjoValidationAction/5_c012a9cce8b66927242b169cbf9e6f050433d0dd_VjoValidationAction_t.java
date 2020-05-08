 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.internal.ui.actions;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.vjet.eclipse.codeassist.CodeassistUtils;
 import org.eclipse.vjet.eclipse.core.VjetPlugin;
 import org.eclipse.vjet.eclipse.core.VjoNature;
 import org.eclipse.vjet.vjo.tool.typespace.SourceTypeName;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExecutableExtension;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.dltk.mod.core.IModelElement;
 import org.eclipse.dltk.mod.core.IProjectFragment;
 import org.eclipse.dltk.mod.core.ISourceModule;
 import org.eclipse.dltk.mod.core.IType;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.internal.core.ModelElement;
 import org.eclipse.dltk.mod.internal.core.ScriptProject;
 import org.eclipse.dltk.mod.internal.core.builder.StandardScriptBuilder;
 import org.eclipse.dltk.mod.internal.corext.util.Messages;
 import org.eclipse.dltk.mod.internal.ui.DLTKUIMessages;
 import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 
 /**
  * This class is uesed to valdiate the VJET validation
  * 
  * @author <a href="mailto:liama@ebay.com">liama</a>
  * @since JDK 1.5
  * 
  */
 public class VjoValidationAction implements IExecutableExtension,
 		IObjectActionDelegate {
 
 	private static final String VJOSUBFIX = ".js";
 
 	ISelection selection;
 
 	private List<IProject> m_project;
 
 	// private static ScriptProject sProject;
 
 	public VjoValidationAction() {
 	}
 
 	public void run(IAction action) {
 		if (!(selection instanceof IStructuredSelection))
 			return;
 
 		if (!TypeSpaceMgr.getInstance().isLoaded())
 			return;
 
 		ScriptProject sProject = null;
 
 		for (IProject project : this.m_project) {
 			sProject = CodeassistUtils.getScriptProject(project.getName());
 			if (sProject == null)
 				return;
 			ArrayList<ISourceModule> selectedSourceModules = new ArrayList<ISourceModule>();
 			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 			Object[] selectionElements = structuredSelection.toArray();
 			for (int i = 0; i < selectionElements.length; i++) {
 				Object selectionElement = selectionElements[i];
 				if (selectionElement instanceof IFile) {
 					addSourceModules(selectedSourceModules,
 							(IResource) selectionElement, sProject);
 				} else if (selectionElement instanceof IFolder) {
 					getFilesFromFolder(selectedSourceModules,
 							(IFolder) selectionElement, sProject);
 				} else {
 					getAllFilesFromProject(selectedSourceModules, sProject);
 				}
 			}
 
 			new VJetValidateBuildJob(DLTKUIMessages.CoreUtility_job_title,
 					project, selectedSourceModules).schedule();
 		}
 	}
 
 	/**
 	 * 
 	 * Get resource file source type name from IResource
 	 * 
 	 * The preJudge is:if resource is not IFiel and not end with .js return
 	 * null;
 	 * 
 	 * @param resource
 	 *            {@link IResource}
 	 * @return {@link SourceTypeName}
 	 */
 	private SourceTypeName getFileQulifieName(IResource resource) {
 		if (resource instanceof IFile
				&& resource.getLocationURI().getPath().endsWith(VJOSUBFIX)) {
 			return CodeassistUtils.getTypeName(resource);
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * Get all files from child folder. with loop
 	 * 
 	 * @param resourceList
 	 * @param folder
 	 */
 	private void getAllFiles(ArrayList<ISourceModule> resourceList,
 			IFolder folder, ScriptProject sProject) {
 		try {
 			IResource[] memebrs = folder.members();
 			for (IResource resource : memebrs) {
 				if (resource instanceof IFile) {
 					IFile file = (IFile) resource;
 					if (file.getLocation().toOSString().endsWith(VJOSUBFIX)) {
 						addSourceModules(resourceList, file, sProject);
 					}
 				} else if (resource instanceof IFolder) {
 					IFolder resourceFolder = (IFolder) resource;
 					getAllFiles(resourceList, resourceFolder, sProject);
 				}
 			}
 		} catch (CoreException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * Get all js files from folder
 	 * 
 	 * @param selectedSourceModules
 	 * 
 	 * @param folder
 	 * @return
 	 */
 	private void getFilesFromFolder(
 			ArrayList<ISourceModule> selectedSourceModules, IFolder folder,
 			ScriptProject sProject) {
 		getAllFiles(selectedSourceModules, folder, sProject);
 	}
 
 	/**
 	 * Get all js files from fragment
 	 * 
 	 * @param resourceList
 	 * @param element
 	 * @throws ModelException
 	 */
 	private void getChildrenFromFragment(ArrayList<ISourceModule> resourceList,
 			IModelElement[] element, ScriptProject sProject)
 			throws ModelException {
 		IResource resource = null;
 		for (IModelElement pf : element) {
 			IModelElement[] elements = ((ModelElement) pf).getChildren();
 			for (IModelElement modelElement : elements) {
 				IModelElement[] files = ((ModelElement) modelElement)
 						.getChildren();
 				for (IModelElement file : files) {
 					if (file.getElementType() == IProjectFragment.SOURCE_MODULE) {
 						resource = file.getResource();
						if (resource != null && resource.getLocationURI().getPath().endsWith(
 								VJOSUBFIX)) {
 							addSourceModules(resourceList, resource, sProject);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * Get all js files from project. via user select project
 	 * 
 	 * @param selectedSourceModules
 	 * @param sp
 	 *            TODO
 	 * @return ArrayList<IResource>
 	 */
 	private void getAllFilesFromProject(
 			ArrayList<ISourceModule> selectedSourceModules, ScriptProject sp) {
 		try {
 			IModelElement[] frags = sp.getChildren();
 			getChildrenFromFragment(selectedSourceModules, frags, sp);
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * Add source module's source type name into collection The prejudge is :
 	 * not added yet. typeName must not be null;
 	 * 
 	 * @param selectedSourceModules
 	 * @param selectionElement
 	 */
 	private void addSourceModules(List<ISourceModule> selectedSourceModules,
 			IResource selectionElement, ScriptProject sProject) {
 		ISourceModule module = null;
 		if (!selectedSourceModules.contains(selectionElement)) {
 			module = getModuleFromResource(selectionElement, sProject);
 			if (module != null
 					&& module.getElementType() == IModelElement.SOURCE_MODULE) {
 				selectedSourceModules.add(module);
 			}
 		}
 	}
 
 	private ISourceModule getModuleFromResource(IResource selectionElement,
 			ScriptProject sProject) {
 		SourceTypeName typeName = null;
 		ISourceModule module = null;
 		IType type = null;
 		typeName = getFileQulifieName(selectionElement);
 		if (typeName != null) {
 			type = CodeassistUtils.findType(sProject, typeName.typeName());
 			if (type != null) {
 				module = type.getSourceModule();
 			}
 		}
 		return module;
 	}
 
 	public void selectionChanged(IAction action, ISelection selection) {
 		this.selection = selection;
 
 		this.m_project = new ArrayList<IProject>();
 		IStructuredSelection selections = ((IStructuredSelection) selection);
 
 		Iterator t = selections.iterator();
 		while (t.hasNext()) {
 
 			IAdaptable adaptable = (IAdaptable) t.next();
 
 			// Modify by Eirc.MA on 20090623
 			if (adaptable == null)
 				return;
 			// End of modification
 			IResource resource = (IResource) adaptable
 					.getAdapter(IResource.class);
 			IProject project = resource.getProject();
 
 			if (project == null)
 				return;
 			this.m_project.add(project);
 			boolean hasVJONature = this.hasVJONature(project);
 			boolean hasVJOBuilder = this.hasVJOBuilder(project);
 			if (hasVJONature && hasVJOBuilder) {
 				action.setEnabled(true);
 			} else {
 				action.setEnabled(false);
 			}
 		}
 	}
 
 	private boolean hasVJONature(IProject project) {
 		final String builderID = VjetPlugin.BUILDER_ID;
 		try {
 			IProjectDescription description = project.getDescription();
 			ICommand[] buildCommands = description.getBuildSpec();
 			for (int i = 0; i < buildCommands.length; i++) {
 				if (builderID.equals(buildCommands[i].getBuilderName()))
 					return true;
 			}
 			return false;
 		} catch (CoreException e) {
 			return false;
 		}
 	}
 
 	private boolean hasVJOBuilder(IProject project) {
 		try {
 			IProjectDescription description = project.getDescription();
 			return description.hasNature(VjoNature.NATURE_ID);
 		} catch (CoreException e) {
 			return false;
 		}
 	}
 
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 	}
 
 	public void setInitializationData(IConfigurationElement config,
 			String propertyName, Object data) throws CoreException {
 	}
 
 	/**
 	 * This class is used to create a new Vjet Validate Build job
 	 * 
 	 * @author <a href="mailto:liama@ebay.com">liama</a>
 	 * @since JDK 1.5
 	 */
 	private static final class VJetValidateBuildJob extends Job {
 		private final IProject fProject;
 		private ArrayList<ISourceModule> selectedSourceModules = null;
 
 		private VJetValidateBuildJob(String name, IProject project,
 				ArrayList<ISourceModule> selectedSourceModules) {
 			super(name);
 			fProject = project;
 			this.selectedSourceModules = selectedSourceModules;
 		}
 
 		public boolean isCoveredBy(VJetValidateBuildJob other) {
 			if (other.fProject == null) {
 				return true;
 			}
 			return fProject != null && fProject.equals(other.fProject);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
 		 */
 		protected IStatus run(IProgressMonitor monitor) {
 			synchronized (getClass()) {
 				if (monitor.isCanceled()) {
 					return Status.CANCEL_STATUS;
 				}
 				Job[] buildJobs = Job.getJobManager().find(
 						ResourcesPlugin.FAMILY_MANUAL_BUILD);
 				for (int i = 0; i < buildJobs.length; i++) {
 					Job curr = buildJobs[i];
 					if (curr != this && curr instanceof VJetValidateBuildJob) {
 						VJetValidateBuildJob job = (VJetValidateBuildJob) curr;
 						if (job.isCoveredBy(this)) {
 							curr.cancel(); // cancel all other build jobs of
 							// our kind
 						}
 					}
 				}
 			}
 			try {
 				if (fProject != null) {
 					monitor.beginTask(Messages.format(
 							DLTKUIMessages.CoreUtility_buildproject_taskname,
 							fProject.getName()), 2);
 					StandardScriptBuilder sb = new StandardScriptBuilder();
 					ScriptProject sProject = CodeassistUtils
 							.getScriptProject(fProject.getName());
 					sb.initialize(sProject);
 					sb.buildModelElements(sProject, selectedSourceModules,
 							new SubProgressMonitor(monitor, 1), 1);
 					DLTKUIPlugin.getWorkspace().build(
 							IncrementalProjectBuilder.INCREMENTAL_BUILD,
 							new SubProgressMonitor(monitor, 1));
 				}
 			} catch (CoreException e) {
 				return e.getStatus();
 			} catch (OperationCanceledException e) {
 				return Status.CANCEL_STATUS;
 			} finally {
 				monitor.done();
 			}
 			return Status.OK_STATUS;
 		}
 
 		public boolean belongsTo(Object family) {
 			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
 		}
 	}
 
 	/**
 	 * @return the m_project
 	 */
 	public List<IProject> getM_project() {
 		return m_project;
 	}
 
 }
