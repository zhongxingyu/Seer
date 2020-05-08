 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the context-sensitive command to find review items
  * in the parent project to add to the review
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.commands;
 
 import org.eclipse.cdt.core.model.ICProject;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.RFSRegistryFactory;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.dialogs.FindReviewItemsDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.UIUtils;
 import org.eclipse.mylyn.versions.core.Change;
 import org.eclipse.mylyn.versions.core.ChangeSet;
 import org.eclipse.mylyn.versions.core.ScmArtifact;
 import org.eclipse.mylyn.versions.ui.ScmUi;
 import org.eclipse.mylyn.versions.ui.spi.ScmUiConnector;
 import org.eclipse.mylyn.versions.ui.spi.ScmUiException;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class FindReviewItemsHandler extends AbstractHandler {
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Method execute.
 	 * @param event ExecutionEvent
 	 * @return Object
 	 * @throws ExecutionException
 	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
 	 */
 	public Object execute(ExecutionEvent event) {
 
 		//Get project to use (use adapters if needed)
 		final ISelection selection = HandlerUtil.getCurrentSelection(event);
 		final Object selectedElement = ((IStructuredSelection)selection).getFirstElement();
 		IProject project = null;
 		//NOTE:  The valadity testes are done if the ProjectPropertyTester class
 		if (selectedElement instanceof IProject) { 
 			project = (IProject) selectedElement;
 		} else if (selectedElement instanceof IJavaProject) {
 			project = ((IJavaProject)selectedElement).getProject();
 		} else if (selectedElement instanceof ICProject) {
 			project = ((ICProject)selectedElement).getProject();
 		} else if (selectedElement instanceof IPackageFragment || selectedElement instanceof IPackageFragmentRoot) {
 			project = ((IJavaElement)selectedElement).getJavaProject().getProject();
 		} else if (selectedElement instanceof IFolder) {
 			project = ((IFolder)selectedElement).getProject();
 		} else if (selectedElement instanceof IAdaptable) {
 			final IAdaptable adaptableProject = (IAdaptable) selectedElement; 
 			project = (IProject) adaptableProject.getAdapter(IProject.class); 
 		} else {
 			//Should never happen
 			Activator.Ftracer.traceError("No project defined for selection of class " + selectedElement.getClass());
 			Activator.getDefault().logError("No project defined for selection of class " + selectedElement.getClass(), null);
 			final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR, "Find Review Item Error",
     				new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "No project defined for selection", null), IStatus.ERROR);
 			dialog.open();
 			return null;
 		}
 	
 		ScmUiConnector uiConnector = ScmUi.getUiConnector(project);
 		if (uiConnector != null) {
 			ChangeSet changeSet = null;
 			try {
 				changeSet = uiConnector.getChangeSet(null, project, null);
 			} catch (ScmUiException e) {
 				Activator.Ftracer.traceError("Exception: " + e.getMessage());
 				e.printStackTrace();
 				return null;
 			}
 
 			createReviewItem(changeSet);
 			return null;
 		}
 		//We could not find any version control system, thus no items
 
 		R4EUIModelController.setDialogOpen(true);
 
 		// TODO: So this code should be replaced by a Git connector.  The dialog should be moved to the versions UI package
 		final FindReviewItemsDialog dialog = new FindReviewItemsDialog(R4EUIModelController.getNavigatorView().
 				getSite().getWorkbenchWindow().getShell(), project);
     	dialog.open();
     	
 		R4EUIModelController.setDialogOpen(false);
 		return null;
 	}
 
 	/**
 	 * Create and serialize the changeset in a Review Item
 	 * @param changeSet
 	 */
 	private void createReviewItem(ChangeSet changeSet) {
 
 		if (changeSet == null) {
 			Activator.Ftracer.traceInfo("Received null ChangeSet");
 			return;
 		}
 		
 		int size = changeSet.getChanges().size();
 		Activator.Ftracer.traceInfo("Received ChangeSet with " + size + " elements");
 		if (size == 0) return; // nothing to add
 	
 		try {
 			//Add Review Item
 			final R4EUIReviewBasic uiReview = R4EUIModelController.getActiveReview();
 			final R4EUIReviewItem uiReviewItem = uiReview.createReviewItem(changeSet, null);
 			if (null == uiReviewItem) return;
 			
			reviewItem.setDescription(changeSet.getMessage());
			reviewItem.setAuthorRep(changeSet.getAuthor().getId());
			reviewItem.setRepositoryRef(changeSet.getId());
			reviewItem.setSubmitted(changeSet.getDate());
	
 			for (Change change : changeSet.getChanges()) {
 				
 				ScmArtifact baseArt = change.getBase();
 				ScmArtifact targetArt = change.getTarget();
 				if (baseArt == null && targetArt == null) {
 					Activator.Ftracer.traceDebug("Received a Change with no base and target in ChangeSet: " + changeSet.getId()
 							+ ", Date: " + changeSet.getDate().toString());
 				}
 				
 				// Get handle to local storage repository
 				IRFSRegistry localRepository = RFSRegistryFactory.getRegistry(R4EUIModelController.getActiveReview().getReview());
 				
 				String baseLocalVersion = null;
 				String targetLocalVersion = null;			
 				//Copy remote files to the local repository
 				if (baseArt != null) {
 					baseLocalVersion = CommandUtils.copyRemoteFileToLocalRepository(localRepository, baseArt);
 				}
 				if (targetArt != null) {
 					targetLocalVersion = CommandUtils.copyRemoteFileToLocalRepository(localRepository, targetArt);
 				}
 				
 				//Add File Context
 				final R4EUIFileContext uiFileContext = uiReviewItem.createFileContext(baseArt, baseLocalVersion,targetArt, 
 						targetLocalVersion, CommandUtils.adaptType(change.getChangeType()));
 				if (null == uiFileContext) {
 					uiReview.removeChildren(uiReviewItem, false);
 					return;
 				}
 			}
 
 		} catch (ResourceHandlingException e) {
 			UIUtils.displayResourceErrorDialog(e);
 		} catch (ReviewsFileStorageException e) {
 			UIUtils.displayReviewsFileStorageErrorDialog(e);
 		} catch (OutOfSyncException e) {
 			UIUtils.displaySyncErrorDialog(e);
 		} catch (CoreException e) {
 			UIUtils.displayCoreErrorDialog(e);
 		}
 	}
 }
