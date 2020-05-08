 /*
  *    Project: The EDNA Kernel
  *             http://www.edna-site.org
  *
  *    File: "$Id:$"
  *
  *    Copyright (C) 2008-2009 European Synchrotron Radiation Facility
  *                            Grenoble, France
  *
  *    Principal authors: Marie-Francoise Incardona (incardon@esrf.fr)
  *                       Olof Svensson (svensson@esrf.fr)
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as published
  *    by the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    and the GNU Lesser General Public License  along with this program.
  *    If not, see <http://www.gnu.org/licenses/>.
  */
 package org.edna.datamodel.transformations.ui.actions;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.mwe.core.WorkflowFacade;
 import org.eclipse.emf.mwe.core.issues.Issues;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionDelegate;
 import org.eclipse.xtext.resource.IResourceDescription;
 import org.eclipse.xtext.resource.IResourceDescriptions;
 import org.eclipse.xtext.resource.containers.IAllContainersState;
 import org.edna.datamodel.datamodel.DatamodelPackage;
 import org.edna.datamodel.datamodel.Model;
 import org.edna.datamodel.transformations.ui.Activator;
 
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 /**
  * Executes the transformation process.
  * @author Karsten Thoms
  */
 public class DSLGenerateDatabindingsAction extends ActionDelegate implements IObjectActionDelegate {
 
 	protected ISelection selection;
 	protected IAction action;
 	protected IWorkbenchPart targetPart;
 
 	@Inject
 	protected IResourceDescriptions index;
 
 	@Inject
 	protected Provider<ResourceSet> resourceSetProvider;
 
 	@Inject
 	protected IAllContainersState containerState;
 
 	/**
 	 * @see ActionDelegate#run(IAction)
 	 */
 	public void run(IAction action) {
 		final IFile file = getFile();
 		final URI fileURI = URI.createPlatformResourceURI(file.getFullPath().toString(), false);
 		final URI targetFile = getTargetFileUri(fileURI);
 		Job j = new Job("EDNA Generate Data Bindings") {
 
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				ResourceSet resourceSet = resourceSetProvider.get();
 				try {
 					HashMap<String, String> args = new HashMap<String, String>();
 					configureArguments(args);
 					WorkflowFacade wfFacade = new WorkflowFacade(getWorkflowFile(), args);
 
 					IResourceDescription resDesc = index.getResourceDescription(fileURI);
 					if (resDesc == null) {
						final String message = "Resource "+fileURI.lastSegment()+" not found in Xtext index. Is the Xtext nature set and was the project built?";
						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
						return Status.CANCEL_STATUS;
 					}
 
 					Map<String, Object> slotContents = new HashMap<String, Object>();
 					Model model = (Model) resDesc.getExportedObjects(DatamodelPackage.Literals.MODEL).iterator().next().getEObjectOrProxy();
 
 					for (IResourceDescription rd : index.getAllResourceDescriptions()) {
 						resourceSet.createResource(rd.getURI());
 					}
 					model = (Model) EcoreUtil.resolve(model, resourceSet);
 
 					slotContents.put("sourceModel", model);
 					// boolean executionResult = wfRunner.run(getWorkflowFile(), new ProgressMonitorAdapter(monitor), args, null);
 					Issues issues = wfFacade.run(slotContents);
 					boolean executionResult = !issues.hasErrors();
 
 					if (executionResult == true) {
 						final String message = "Generated Data Bindings for " + file.getName() + " to "+targetFile.lastSegment()+".";
 						Activator.getDefault().getLog()
 								.log(new Status(IStatus.OK, Activator.PLUGIN_ID, message));
 						// refresh the folder containing changed resources
 						file.getParent().getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
 						return Status.OK_STATUS;
 					} else {
 						final String message = "Transformation of " + file.getName() + " failed.";
 						Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
 						return Status.CANCEL_STATUS;
 					}
 
 				} catch (Exception e) {
 					e.printStackTrace();
 					return Status.CANCEL_STATUS;
 				}
 			}
 		};
 
 		j.addJobChangeListener(new JobChangeAdapter() {
 			public void done(final IJobChangeEvent event) {
 				org.eclipse.swt.widgets.Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						if (event.getResult()==Status.OK_STATUS) {
 							final String message = "Generated Data Bindings for " + file.getName() + " to "+targetFile.lastSegment()+".";
 							MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), "EDNA Datamodel Transformation", message);
 						} else {
 							final String message = "Transformation of " + file.getName() + " failed.";
 							MessageDialog.openWarning(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
 									"EDNA Data Bindings", message + "Please check the Error Log view (Window -> Show view -> Other... -> Error Log).");
 						}
 					}});
 			}
 		});
 		j.schedule();
 	}
 
 	protected URI getTargetFileUri (URI sourceFile) {
 		return URI.createURI(sourceFile.toString().replaceFirst("\\.edna_datamodel", ".py"));
 	}
 
 	protected void configureArguments (HashMap<String, String> args) {
 		final IFile file = getFile();
 		final URI fileURI = URI.createPlatformResourceURI(file.getFullPath().toString(), false);
 		final URI targetFile = getTargetFileUri(fileURI);
 		args.put("standalone", "false");
 		args.put("model", fileURI.toString());
 		args.put("targetFile", targetFile.lastSegment());
 		args.put("targetDir", getTargetDir(file).getAbsolutePath());
 		args.put("includePaths", getDslIncludePaths());
 
 	}
 
 	private File getTargetDir (IFile sourceFile) {
 		// Detect whether a "plugins" directoy exists in file.getLocation().toFile().getParent().getParent(); if so use that one as target dir
 		File srcDir = new File(new File(sourceFile.getLocation().toFile().getParent()).getParent(), "src");
 		File pluginsDir = new File(new File(sourceFile.getLocation().toFile().getParent()).getParent(), "plugins");
 		if (srcDir.exists()) {
 			return srcDir;
 		} else if (pluginsDir.exists()) {
 			return pluginsDir;
 		} else {
 			return new File(sourceFile.getLocation().toFile().getParent());
 		}
 	}
 
 	/**
 	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		this.action = action;
 		this.targetPart = targetPart;
 	}
 
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 		this.selection = selection;
 	}
 
 	public IFile getFile() {
 		return (IFile) ((IStructuredSelection) selection).getFirstElement();
 	}
 
 	protected String getDslIncludePaths() {
 		// search for paths that contain .edna_datamodel files
 		String projectPath = getFile().getProject().getFullPath().toString();
 		Set<IPath> includePaths = Sets.newHashSet();
 		for (String handle : containerState.getVisibleContainerHandles(projectPath)) {
 			for (URI containerUri : containerState.getContainedURIs(handle)) {
 				IResource containerResource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(containerUri.toPlatformString(true)));
 				includePaths.add(containerResource.getParent().getLocation());
 			}
 		}
 		String includePathsValue = includePaths.toString();
 		includePathsValue = includePathsValue.substring(1, includePathsValue.length()-1);
 		return includePathsValue;
 	}
 
 	protected String getWorkflowFile() {
 		return "org/edna/datamodel/transformations/ui/actions/EDGenerateDS_dsl_plugin.mwe";
 	}
 }
