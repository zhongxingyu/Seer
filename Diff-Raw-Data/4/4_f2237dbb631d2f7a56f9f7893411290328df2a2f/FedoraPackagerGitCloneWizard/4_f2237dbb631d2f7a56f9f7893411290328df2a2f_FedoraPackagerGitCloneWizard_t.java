 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.git.internal.ui;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.egit.core.op.ConnectProviderOperation;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jgit.errors.NoRemoteRepositoryException;
 import org.eclipse.jgit.errors.NotSupportedException;
 import org.eclipse.jgit.errors.TransportException;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.PlatformUI;
 import org.fedoraproject.eclipse.packager.FedoraSSL;
 import org.fedoraproject.eclipse.packager.FedoraSSLFactory;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.git.Activator;
 import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitCloneOperation;
 import org.fedoraproject.eclipse.packager.git.FedoraPackagerGitText;
 import org.fedoraproject.eclipse.packager.git.GitPreferencesConstants;
 import org.fedoraproject.eclipse.packager.git.GitUtils;
 
 /**
  * Wizard to checkout package content from Fedora Git.
  *
  */
 public class FedoraPackagerGitCloneWizard extends Wizard implements IImportWizard {
 
 	private SelectModulePage page;
 	private IStructuredSelection selection;
 
 	/**
 	 * Creates the wizards and sets that it needs progress monitor.
 	 */
 	public FedoraPackagerGitCloneWizard() {
 		super();
 		// Set title of wizard window
 		setWindowTitle(FedoraPackagerGitText.FedoraPackagerGitCloneWizard_wizardTitle);
 		// required to show progress info of clone job
 		setNeedsProgressMonitor(true);
 	}
 
 	@Override
 	public void addPages() {
 		// get Fedora username from cert
 		page = new SelectModulePage();
 		addPage(page);
 		page.init(selection);
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		this.selection = selection;
 	}
 
 	@Override
 	public boolean performFinish() {
 		try {
 			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
 			// Bail out if project already exists
 			IResource project = wsRoot.findMember(new Path(page.getPackageName()));
 			if (project != null && project.exists()) {
 				final String errorMessage = NLS
 						.bind(FedoraPackagerGitText.FedoraPackagerGitCloneWizard_projectExists,
 								project.getName());
 				cloneFailChecked(errorMessage);
 				// let's give user a chance to fix this minor problem
 				return false;
 			}
 			// Make sure to be created directory does not exist or is
 			// empty
 			File newDir = new File(wsRoot.getLocation().toOSString() +
 			IPath.SEPARATOR + page.getPackageName() );
 			if (newDir.exists() && newDir.isDirectory()) {
 				String contents[] = newDir.list();
 				if (contents.length != 0) {
 					// Refuse to clone, give user a chance to correct
 					final String errorMessage = NLS
 							.bind(FedoraPackagerGitText.FedoraPackagerGitCloneWizard_filesystemResourceExists,
 									page.getPackageName());
 					cloneFailChecked(errorMessage);
 					return false;
 				}
 			}
 
 			// prepare the clone op
 			final FedoraPackagerGitCloneOperation cloneOp = new FedoraPackagerGitCloneOperation();
 			cloneOp.setCloneURI(getGitCloneURL()).setPackageName(page.getPackageName());
 			// Make sure we report a nice error if repo not found
 			try {
 				// Perform clone in ModalContext thread with progress
 				// reporting on the wizard.
 				getContainer().run(true, true, new IRunnableWithProgress() {
 					@Override
 					public void run(IProgressMonitor monitor)
 							throws InvocationTargetException,
 							InterruptedException {
 						try {
 							cloneOp.run(monitor);
 						} catch (IOException e) {
 							throw new InvocationTargetException(e);
 						} catch (IllegalStateException e) {
 							throw new InvocationTargetException(e);
 						} catch (CoreException e) {
 							throw new InvocationTargetException(e);
 						}
 						if (monitor.isCanceled())
 							throw new InterruptedException();
 					}
 				});
 			} catch (InvocationTargetException e) {
 				// if repo wasn't found make this apparent
 				if (e.getCause() instanceof NoRemoteRepositoryException) {
 					// Refuse to clone, give user a chance to correct
 					final String errorMessage = NLS
 							.bind(FedoraPackagerGitText.FedoraPackagerGitCloneWizard_repositoryNotFound,
 									page.getPackageName());
 					cloneFailChecked(errorMessage);
 					return false; // let user correct
 					// Caused by: org.eclipse.jgit.errors.NotSupportedException: URI not supported: ssh:///jeraal@alkldal.test.comeclipse-callgraph.git
 				} else if (e.getCause() instanceof NotSupportedException || e.getCause() instanceof TransportException) {
 					final String errorMessage = NLS
 					.bind(FedoraPackagerGitText.FedoraPackagerGitCloneWizard_badURIError,
 							Activator.getStringPreference(GitPreferencesConstants.PREF_CLONE_BASE_URL));
 					cloneFailChecked(errorMessage);
 					return false; // let user correct
 				}
 				throw e;
 			}
 			IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(page.getPackageName());
 			newProject.create(null);
 			newProject.open(null);
 			// Set persistent property so that we know when to show the context
 			// menu item.
 			newProject.setPersistentProperty(PackagerPlugin.PROJECT_PROP,
 					"true" /* unused value */); //$NON-NLS-1$
 			ConnectProviderOperation connect = new ConnectProviderOperation(
 					newProject);
 			connect.execute(null);
 
 			// Add new project to working sets, if requested
 			IWorkingSet[] workingSets = page.getWorkingSets();
 			if (workingSets.length > 0) {
 				PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(newProject, workingSets);
 			}
 
 			// Finally show the Git Repositories view for convenience
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 					.getActivePage().showView(
 							"org.eclipse.egit.ui.RepositoriesView"); //$NON-NLS-1$
 			return true;
 		} catch (InterruptedException e) {
 			MessageDialog.openInformation(getShell(), FedoraPackagerGitText.FedoraPackagerGitCloneWizard_cloneFail, 
 					FedoraPackagerGitText.FedoraPackagerGitCloneWizard_cloneCancel);
 			return false;
 		} catch (Exception e) {
 			org.fedoraproject.eclipse.packager.git.Activator.handleError(
 					FedoraPackagerGitText.FedoraPackagerGitCloneWizard_cloneFail, e, true);
 			return false;
 		}
 	}
 	
 	/**
 	 * Opens error dialog with provided reason in error message.
 	 * 
 	 * @param errorMsg The error message to use.
 	 */
 	private void cloneFailChecked(String errorMsg) {
 		ErrorDialog
 		.openError(
 				getShell(),
 				getWindowTitle() + FedoraPackagerGitText.FedoraPackagerGitCloneWizard_problem,
 				FedoraPackagerGitText.FedoraPackagerGitCloneWizard_cloneFail,
 				new Status(
 						IStatus.ERROR,
 						org.fedoraproject.eclipse.packager.git.Activator.PLUGIN_ID,
 						0, errorMsg, null));
 	}
 
 	/**
 	 * Determine the Git clone URL in the following order:
 	 * <ol>
 	 * <li>Use the Git base URL as set by the preference (if any) or</li>
 	 * <li>Check if ~/.fedora.cert is present, and if so retrieve the user name
 	 * from it.</li>
 	 * <li>If all else fails, construct an anonymous clone URL</li>
 	 * </ol>
 	 * 
 	 * @return The full clone URL based on the package name.
 	 */
 	private String getGitCloneURL() {
 		String gitBaseURL = Activator
 				.getStringPreference(GitPreferencesConstants.PREF_CLONE_BASE_URL);
 		String fasUserName = FedoraSSLFactory.getInstance()
 				.getUsernameFromCert();
 		if (gitBaseURL != null) {
 			return GitUtils.getFullGitURL(gitBaseURL, page.getPackageName());
 		} else if (!fasUserName.equals(FedoraSSL.UNKNOWN_USER)) {
 			return GitUtils.getFullGitURL(
 					GitUtils.getAuthenticatedGitBaseUrl(fasUserName),
 					page.getPackageName());
 		} else {
 			// anonymous
 			return GitUtils.getFullGitURL(GitUtils.getAnonymousGitBaseUrl(),
 					page.getPackageName());
 		}
 	}
 }
