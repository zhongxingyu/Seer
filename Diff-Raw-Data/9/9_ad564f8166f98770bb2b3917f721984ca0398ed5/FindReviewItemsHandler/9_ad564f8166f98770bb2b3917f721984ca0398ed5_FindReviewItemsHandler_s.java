 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
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
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.commands.handlers;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.eclipse.compare.rangedifferencer.RangeDifference;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.mylyn.reviews.frame.core.utils.Tracer;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EContextType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewComponent;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.RFSRegistryFactory;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.R4EUIDialogFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.R4ECompareEditorInput;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIDeltaContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUITextPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.preferences.PreferenceConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.Diff;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.DiffUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.MailServicesProxy;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.mylyn.versions.core.Change;
 import org.eclipse.mylyn.versions.core.ChangeSet;
 import org.eclipse.mylyn.versions.core.ScmArtifact;
 import org.eclipse.mylyn.versions.ui.spi.ScmConnectorUi;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.progress.IProgressConstants;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class FindReviewItemsHandler extends AbstractHandler {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field FETCH_JOB_FAMILY. (value is ""fetchFamily"")
 	 */
 	private static final String FETCH_JOB_FAMILY = "fetchFamily";
 
 	/**
 	 * Field DELTA_JOB_FAMILY. (value is ""deltaFamily"")
 	 */
 	private static final String DELTA_JOB_FAMILY = "deltaFamily";
 
 	/**
 	 * Field ADD_JOB_FAMILY. (value is ""addFamily"")
 	 */
 	private static final String ADD_JOB_FAMILY = "addFamily";
 
 	/**
 	 * Field IMPORTING_FILES_MSG. (value is ""Importing Files..."")
 	 */
 	private static final String IMPORTING_FILES_MSG = "Importing Files...";
 
 	/**
 	 * Field FETCHING_FILES_MSG. (value is ""Fetching Files..."")
 	 */
 	private static final String FETCHING_FILES_MSG = "Fetching Files...";
 
 	/**
 	 * Field FETCHING_FILE_MSG. (value is ""Fetching File..."")
 	 */
 	private static final String FETCHING_FILE_MSG = "Fetching File...";
 
 	/**
 	 * Field CALCULATE_DELTAS_MSG. (value is ""Computing differences..."")
 	 */
 	private static final String CALCULATE_DELTAS_MSG = "Computing differences...";
 
 	/**
 	 * Field CALCULATE_DELTAS_FILE_MSG. (value is ""Computing differences for file "")
 	 */
 	private static final String CALCULATE_DELTAS_FILE_MSG = "Computing differences for file ";
 
 	/**
 	 * Field ADD_ELEMENT_MSG. (value is ""Adding Elements to R4E model..."")
 	 */
 	private static final String ADD_ELEMENT_MSG = "Adding Elements to R4E model...";
 
 	/**
 	 * Field ADD_REVIEW_ITEM_MSG. (value is ""Adding Review Item to R4E Model..."")
 	 */
 	private static final String ADD_REVIEW_ITEM_MSG = "Adding Review Item to R4E Model...";
 
 	/**
 	 * Field MAX_CONCURRRENT_JOBS. (value is 20)
 	 */
 	private static final int MAX_CONCURRRENT_JOBS = 20;
 
 	// ------------------------------------------------------------------------
 	// Members
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fLock.
 	 */
 	private final ReentrantLock fLock = new ReentrantLock();
 
 	/**
 	 * Field fRunningJobs.
 	 */
 	private final AtomicInteger fRunningJobs = new AtomicInteger(0);
 
 	/**
 	 * Field fExceptionError.
 	 */
 	private Exception fExceptionError;
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method execute.
 	 * 
 	 * @param aEvent
 	 *            ExecutionEvent
 	 * @return Object
 	 * @throws ExecutionException
 	 * @see org.eclipse.core.commands.IHandler#execute(ExecutionEvent)
 	 */
 	public Object execute(final ExecutionEvent aEvent) {
 
 		fExceptionError = null;
 
 		//Make sure that Reentrant lock is in the proper state
 		if (fLock.isLocked()) {
 			fLock.unlock();
 		}
 
 		// Get project to use (use adapters if needed)
 		final ISelection selection = HandlerUtil.getCurrentSelection(aEvent);
 		if (selection instanceof IStructuredSelection) {
 			final Object selectedElement = ((IStructuredSelection) selection).getFirstElement();
 			IProject project = null;
 
 			// NOTE: The validity tests are done if the ProjectPropertyTester class
 			if (selectedElement instanceof IProject) {
 				project = (IProject) selectedElement;
 			} else if (R4EUIPlugin.isJDTAvailable() && selectedElement instanceof IJavaProject) {
 				project = ((IJavaProject) selectedElement).getProject();
 			} else if (R4EUIPlugin.isCDTAvailable() && selectedElement instanceof org.eclipse.cdt.core.model.ICProject) {
 				project = ((org.eclipse.cdt.core.model.ICProject) selectedElement).getProject();
 			} else if (selectedElement instanceof IPackageFragment || selectedElement instanceof IPackageFragmentRoot) {
 				project = ((IJavaElement) selectedElement).getJavaProject().getProject();
 			} else if (selectedElement instanceof IFolder) {
 				project = ((IFolder) selectedElement).getProject();
 			} else if (selectedElement instanceof IAdaptable) {
 				final IAdaptable adaptableProject = (IAdaptable) selectedElement;
 				project = (IProject) adaptableProject.getAdapter(IProject.class);
 			} else {
 				// Should never happen
 				R4EUIPlugin.Ftracer.traceError("No project defined for selection of class " //$NON-NLS-1$
 						+ ((null != selectedElement) ? selectedElement.getClass() : ""));
 				R4EUIPlugin.getDefault()
 						.logError(
 								"No project defined for selection of class " + ((null != selectedElement) ? selectedElement.getClass() : ""), null); //$NON-NLS-1$
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"Find Review Item Error", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								"No project defined for selection", null), IStatus.ERROR);
 				dialog.open();
 				return null;
 			}
 
 			Cursor cursor = new Cursor(PlatformUI.getWorkbench().getDisplay(), SWT.CURSOR_WAIT);
 			Shell activeShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 			activeShell.setCursor(cursor);
 			final ScmConnectorUi uiConnector = R4EUIDialogFactory.getInstance().getScmUiConnector(project);
 			if (null != uiConnector) {
 				R4EUIPlugin.Ftracer.traceDebug("Resolved Scm Ui connector: " + uiConnector); //$NON-NLS-1$
 				final ChangeSet changeSet = uiConnector.getChangeSet(null, project);
 				activeShell.setCursor(null);
 				cursor.dispose();
 				createReviewItem(aEvent, changeSet);
 			} else {
 				activeShell.setCursor(null);
 				cursor.dispose();
 				// We could not find any version control system, thus no items
 				final String strProject = ((null == project) ? "(no project)" : project.getName()); //$NON-NLS-1$
 				R4EUIPlugin.Ftracer.traceDebug("No Scm Ui connector found for project: " + strProject); //$NON-NLS-1$
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_WARNING,
 						"Cannot find new Review Items", new Status(IStatus.WARNING, R4EUIPlugin.PLUGIN_ID, 0,
 								"No SCM Connector detected for Project " + strProject, null), IStatus.WARNING);
 				dialog.open();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Create and serialize the changeset in a Review Item
 	 * 
 	 * @param aChangeSet
 	 *            ChangeSet
 	 * @param aEvent
 	 *            ExecutionEvent
 	 */
 	private void createReviewItem(final ExecutionEvent aEvent, final ChangeSet aChangeSet) {
 
 		if (null == aChangeSet) {
 			R4EUIPlugin.Ftracer.traceInfo("Received null ChangeSet"); //$NON-NLS-1$
 			return;
 		}
 
 		final int size = aChangeSet.getChanges().size();
 		R4EUIPlugin.Ftracer.traceInfo("Received ChangeSet with " + size + " elements"); //$NON-NLS-1$ //$NON-NLS-2$
 		if (0 == size) {
 			return; // nothing to add
 		}
 
 		//Check if Review Item already exists
 		final R4EUIReviewBasic uiReview = R4EUIModelController.getActiveReview();
 		if (null == uiReview) {
 			return;
 		}
 		for (R4EUIReviewItem uiItem : uiReview.getReviewItems()) {
 			if (null != uiItem) {
 				if (aChangeSet.getId().equals(uiItem.getItem().getRepositoryRef())) {
 					//The commit item already exists so ignore command
 					R4EUIPlugin.Ftracer.traceWarning("Review Item already exists.  Ignoring");
 					final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_WARNING,
 							"Cannot add Review Item", new Status(IStatus.WARNING, R4EUIPlugin.PLUGIN_ID, 0,
 									"Review Item already exists", null), IStatus.WARNING);
 					dialog.open();
 					return;
 				}
 			}
 		}
 
 		try {
 			// Get handle to local storage repository
 			final IRFSRegistry localRepository = RFSRegistryFactory.getRegistry(R4EUIModelController.getActiveReview()
 					.getReview());
 
 			//Create Synchronized list that will temporarily hold the elements to be added
 			final List<TempFileContext> filesToAddlist = Collections.synchronizedList(new ArrayList<TempFileContext>());
 
 			//The Main Job is the parent of all the child jobs that execute the work
 			final Job mainJob = new Job(IMPORTING_FILES_MSG) {
 				@Override
 				public IStatus run(final IProgressMonitor aMonitor) {
 					if (null == aMonitor) {
 						return Status.CANCEL_STATUS;
 					}
 					R4EUIModelController.setJobInProgress(true); //Disable operations on UI
 					aMonitor.beginTask(IMPORTING_FILES_MSG, 3);
 
 					//First fetch base and target files from remote repository and copy them to the R4E local repository using parallel jobs
 					aMonitor.subTask(FETCHING_FILES_MSG);
 
 					//Prepare to collect performance trace information (if enabled).
 					Date startImportingTime = null;
 					Date startFetchTime = null;
 					if (Tracer.isInfo()) {
 						startImportingTime = new Date();
 						startFetchTime = new Date();
 					}
 
 					for (final Change change : aChangeSet.getChanges()) {
 
 						final Job fetchJob = new Job(FETCHING_FILE_MSG) {
 							@Override
 							public boolean belongsTo(Object aFamily) {
 								return FETCH_JOB_FAMILY.equals(aFamily);
 							}
 
 							@Override
 							public IStatus run(IProgressMonitor aFetchMonitor) {
 								if (null == aFetchMonitor) {
 									return Status.CANCEL_STATUS;
 								}
 
 								//If the main task is cancelled, abort here
 								if (aMonitor.isCanceled()) {
 									return Status.CANCEL_STATUS;
 								}
 
 								aFetchMonitor.beginTask(FETCHING_FILE_MSG, 2);
 								try {
 									TempFileContext file = fetchFiles(change, localRepository, aMonitor, aFetchMonitor);
 									filesToAddlist.add(file);
 									aFetchMonitor.done();
 									return Status.OK_STATUS;
 								} catch (final ReviewsFileStorageException e) {
 									R4EUIPlugin.Ftracer.traceError(R4EUIConstants.EXCEPTION_MSG + e.toString()
 											+ " (" + e.getMessage() //$NON-NLS-1$
 											+ ")"); //$NON-NLS-1$
 									R4EUIPlugin.getDefault().logError(R4EUIConstants.EXCEPTION_MSG + e.toString(), e);
 									if (null == fExceptionError) {
 										fExceptionError = e;
 									}
 									aFetchMonitor.done();
 									return Status.CANCEL_STATUS;
 								} catch (final CoreException e) {
 									if (!e.getMessage().equals(R4EUIConstants.CANCEL_EXCEPTION_MSG)) {
 										R4EUIPlugin.Ftracer.traceError(R4EUIConstants.EXCEPTION_MSG + e.toString()
 												+ " (" + e.getMessage() //$NON-NLS-1$
 												+ ")"); //$NON-NLS-1$
 										R4EUIPlugin.getDefault().logError(R4EUIConstants.EXCEPTION_MSG + e.toString(),
 												e);
 										if (null == fExceptionError) {
 											fExceptionError = e;
 										}
 									}
 									aFetchMonitor.done();
 									return Status.CANCEL_STATUS;
 								}
 							}
 						};
 
 						/* Listen to Job's Lifecycle */
 						fetchJob.addJobChangeListener(new JobChangeAdapter() {
 
 							/* Count the number of running Jobs for the Rule */
 							@Override
 							public void running(IJobChangeEvent aEvent) {
 								if (null == aEvent || (null == aEvent.getResult())
 										|| !aEvent.getResult().equals(Status.CANCEL_STATUS)) {
 									fRunningJobs.getAndIncrement();
 								}
 							}
 
 							/* Update Fields when a Job is Done */
 							@Override
 							public void done(IJobChangeEvent aEvent) {
 								fRunningJobs.decrementAndGet();
 							}
 						});
 
 						/* Apply the internal Rule */
 						fetchJob.setRule(new JobQueueSchedulingRule());
 
 						/* Do not interrupt on any Error */
 						fetchJob.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
 
 						fetchJob.setUser(false);
 						fetchJob.schedule();
 					}
 					try {
 						Job.getJobManager().join(FETCH_JOB_FAMILY, null);
 					} catch (OperationCanceledException ex) {
 						return Status.CANCEL_STATUS;
 					} catch (InterruptedException ex) {
 						R4EUIPlugin.getDefault().logError(R4EUIConstants.EXCEPTION_MSG + ex.toString(), ex);
 						return Status.CANCEL_STATUS;
 					}
 
 					//If the task is cancelled, abort here
 					if (aMonitor.isCanceled()) {
 						aMonitor.done();
 						R4EUIModelController.setJobInProgress(false);
 						if (null != fExceptionError) {
 							if (fExceptionError instanceof ReviewsFileStorageException) {
 								UIUtils.displayReviewsFileStorageErrorDialog((ReviewsFileStorageException) fExceptionError);
 							} else if (fExceptionError instanceof CoreException) {
 								UIUtils.displayCoreErrorDialog((CoreException) fExceptionError);
 							}
 						}
 						return Status.CANCEL_STATUS;
 					}
 					aMonitor.worked(1);
 
 					//Tracing add fetch time
 					if (startFetchTime != null) {
 						R4EUIPlugin.Ftracer.traceInfo("Total time to Import/Push files is: " //$NON-NLS-1$
 								+ ((new Date()).getTime() - startFetchTime.getTime()));
 					}
 
 					//Reset Tracing benchmark timer
 					Date startingComputeTime = null;
 					if (Tracer.isInfo()) {
 						startingComputeTime = new Date();
 					}
 
 					//Second calculate deltas for all fetched files using parallel jobs (if configured in preferences)
 					if (R4EUIPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_USE_DELTAS)) {
 						aMonitor.subTask(CALCULATE_DELTAS_MSG);
 						for (final TempFileContext file : filesToAddlist) {
 							if (null == file) {
 								continue;
 							}
 
 							final Job deltaJob = new Job(CALCULATE_DELTAS_MSG) {
 								@Override
 								public boolean belongsTo(Object aFamily) {
 									return DELTA_JOB_FAMILY.equals(aFamily);
 								}
 
 								@Override
 								public IStatus run(IProgressMonitor aDeltaMonitor) {
 									if (null == aDeltaMonitor) {
 										return Status.CANCEL_STATUS;
 									}
 									aDeltaMonitor.beginTask(CALCULATE_DELTAS_FILE_MSG + file.toString(), 1);
 
 									try {
 										updateFilesWithDeltas(file);
 									} catch (CoreException e) {
 										R4EUIPlugin.Ftracer.traceError(R4EUIConstants.EXCEPTION_MSG + e.toString()
 												+ " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
 										R4EUIPlugin.getDefault().logError(R4EUIConstants.EXCEPTION_MSG + e.toString(),
 												e);
 									}
 									aDeltaMonitor.worked(1);
 									aDeltaMonitor.done();
 									return Status.OK_STATUS;
 								}
 							};
 
 							/* Listen to Job's Lifecycle */
 							deltaJob.addJobChangeListener(new JobChangeAdapter() {
 
 								/* Count the number of running Jobs for the Rule */
 								@Override
 								public void running(IJobChangeEvent aEvent) {
 									if (null == aEvent || (null == aEvent.getResult())
 											|| !aEvent.getResult().equals(Status.CANCEL_STATUS)) {
 										fRunningJobs.getAndIncrement();
 									}
 								}
 
 								/* Update Fields when a Job is Done */
 								@Override
 								public void done(IJobChangeEvent aEvent) {
 									fRunningJobs.decrementAndGet();
 								}
 							});
 
 							/* Apply the internal Rule */
 							deltaJob.setRule(new JobQueueSchedulingRule());
 
 							/* Do not interrupt on any Error */
 							deltaJob.setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE);
 
 							deltaJob.setUser(false);
 							deltaJob.schedule();
 						}
 						try {
 							Job.getJobManager().join(DELTA_JOB_FAMILY, null);
 						} catch (OperationCanceledException ex) {
 							return Status.CANCEL_STATUS;
 						} catch (InterruptedException ex) {
 							R4EUIPlugin.getDefault().logError(R4EUIConstants.EXCEPTION_MSG + ex.toString(), ex);
 							return Status.CANCEL_STATUS;
 						}
 					}
 					//If the task is cancelled, exit
 					if (aMonitor.isCanceled()) {
 						aMonitor.done();
 						R4EUIModelController.setJobInProgress(false);
 						return Status.CANCEL_STATUS;
 					}
 					aMonitor.worked(1);
 
 					//Tracing add calculate changes time
 					if (startingComputeTime != null) {
 						R4EUIPlugin.Ftracer.traceInfo("Total time to Compute changes is: " //$NON-NLS-1$
 								+ ((new Date()).getTime() - startingComputeTime.getTime()));
 					}
 
 					//Reset Tracing benchmark timer
 					Date startUpdatingModel = null;
 					if (Tracer.isInfo()) {
 						startUpdatingModel = new Date();
 					}
 
 					//Third add all elements to the UI model
 					aMonitor.subTask(ADD_ELEMENT_MSG);
 					final Job addJob = new Job(ADD_ELEMENT_MSG) {
 						@Override
 						public boolean belongsTo(Object aFamily) {
 							return ADD_JOB_FAMILY.equals(aFamily);
 						}
 
 						@Override
 						public IStatus run(IProgressMonitor aAddMonitor) {
 							if (null == aAddMonitor) {
 								return Status.CANCEL_STATUS;
 							}
 							try {
 								final R4EUIReviewItem uiReviewItem;
 								if (filesToAddlist.size() > 0) {
 									aAddMonitor.beginTask(ADD_ELEMENT_MSG, filesToAddlist.size() + 1);
 
 									aAddMonitor.subTask(ADD_REVIEW_ITEM_MSG);
 									uiReviewItem = uiReview.createCommitReviewItem(aChangeSet, null);
 									aAddMonitor.worked(1);
 
 									//Lock the resource to the user review items to avoid parallel updates from other users
 									Resource resource = uiReviewItem.getItem().eResource();
 									final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(
 											uiReviewItem.getItem(), R4EUIModelController.getReviewer());
 
 									//Prevent serialization for each individual child element and wait till the end
 									R4EUIModelController.stopSerialization(resource);
 
 									for (TempFileContext file : filesToAddlist) {
 										//May need to Test if the target or base files are empty, means this is a folder
 										if (file.getTarget() != null || file.getBase() != null) {
 											addFileToModel(uiReviewItem, file, aAddMonitor);
 										} else {
											R4EUIPlugin.Ftracer.traceInfo("INFO No Base and NO target files, so no ADD"); //$NON-NLS-1$
 										}
 									}
 
 									//Resume serialization
 									R4EUIModelController.resetToDefaultSerialization();
 
 									//Check-in to serialize the whole commit element with children
 									R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 									UIUtils.setNavigatorViewFocus(uiReviewItem, 1);
 
 									//Notify users if need be
 									final List<R4EReviewComponent> addedItems = new ArrayList<R4EReviewComponent>();
 									addedItems.add(uiReviewItem.getItem());
 									final R4EReview review = uiReview.getReview();
 									if (review.getType().equals(R4EReviewType.R4E_REVIEW_TYPE_FORMAL)) {
 										if (((R4EFormalReview) review).getCurrent()
 												.getType()
 												.equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 											MailServicesProxy.sendItemsAddedNotification(addedItems);
 
 										}
 									}
 								}
 							} catch (CoreException e) {
 								UIUtils.displayCoreErrorDialog(e);
 							} catch (ResourceHandlingException e) {
 								UIUtils.displayResourceErrorDialog(e);
 							} catch (OutOfSyncException e) {
 								UIUtils.displaySyncErrorDialog(e);
 							} finally {
 								//Minimize the possibility to remain with serialization off 
 								R4EUIModelController.resetToDefaultSerialization();
 							}
 							return Status.OK_STATUS;
 						}
 					};
 					addJob.setUser(false);
 					addJob.schedule();
 					try {
 						Job.getJobManager().join(ADD_JOB_FAMILY, null);
 					} catch (OperationCanceledException ex) {
 						return Status.CANCEL_STATUS;
 					} catch (InterruptedException ex) {
 						R4EUIPlugin.getDefault().logError(R4EUIConstants.EXCEPTION_MSG + ex.toString(), ex);
 						return Status.CANCEL_STATUS;
 					}
 
 					//Tracing add updating model time
 					if (startUpdatingModel != null) {
 						Date jobEnd = new Date();
 						R4EUIPlugin.Ftracer.traceInfo("Total time to update model is: " //$NON-NLS-1$ 
 								+ (jobEnd.getTime() - startUpdatingModel.getTime()));
 					}
 
 					//Tracing add total time
 					if (startImportingTime != null) {
 						Date jobEnd = new Date();
 						R4EUIPlugin.Ftracer.traceInfo("Total time to fetch files, compute deltas and update model is: " //$NON-NLS-1$
 								+ (jobEnd.getTime() - startImportingTime.getTime()));
 					}
 
 					aMonitor.worked(1);
 					aMonitor.done();
 					R4EUIModelController.setJobInProgress(false);
 					return Status.OK_STATUS;
 				}
 			};
 			mainJob.setUser(true);
 			mainJob.schedule();
 		} catch (ReviewsFileStorageException e) {
 			UIUtils.displayReviewsFileStorageErrorDialog(e);
 		}
 	}
 
 	/**
 	 * Method fetchFiles.
 	 * 
 	 * @param aChange
 	 *            Change
 	 * @param aLocalRepository
 	 *            IRFSRegistry
 	 * @param aMainMonitor
 	 *            IProgressMonitor
 	 * @param aSubMonitor
 	 *            IProgressMonitor
 	 * @return TempFileContext
 	 * @throws CoreException
 	 * @throws ReviewsFileStorageException
 	 * @throws SubJobCancelledException
 	 * @throws MainJobCancelledException
 	 */
 	private TempFileContext fetchFiles(final Change aChange, IRFSRegistry aLocalRepository,
 			IProgressMonitor aMainMonitor, IProgressMonitor aSubMonitor) throws ReviewsFileStorageException,
 			CoreException {
 
 		R4EFileVersion baseLocalVersion = null;
 		R4EFileVersion targetLocalVersion = null;
 
 		//Get Base artifact
 		final ScmArtifact baseArt = aChange.getBase();
 		if (null != baseArt) {
 			// Copy to the local repository
 			aSubMonitor.subTask(FETCHING_FILE_MSG + baseArt.getPath() + " (base) from remote repository"); //$NON-NLS-1$
 			baseLocalVersion = CommandUtils.copyRemoteFileToLocalRepository(fLock, aLocalRepository, baseArt,
 					aMainMonitor);
 		}
 		aSubMonitor.worked(1);
 
 		//Get Target artifact
 		final ScmArtifact targetArt = aChange.getTarget();
 		if (null != targetArt) {
 			aSubMonitor.subTask(FETCHING_FILE_MSG + targetArt.getPath() + " (target) from remote repository"); //$NON-NLS-1$
 			targetLocalVersion = CommandUtils.copyRemoteFileToLocalRepository(fLock, aLocalRepository, targetArt,
 					aMainMonitor);
 		}
 		aSubMonitor.worked(1);
 
 		// Add File Context to the list to be added
 		return new TempFileContext(aLocalRepository, baseLocalVersion, targetLocalVersion,
 				CommandUtils.adaptType(aChange.getChangeType()));
 	}
 
 	/**
 	 * Method updateFilesWithDeltas.
 	 * 
 	 * @param aFile
 	 *            TempFileContext
 	 * @throws CoreException
 	 */
 	private void updateFilesWithDeltas(final TempFileContext aFile) throws CoreException {
 
 		//Find all differences between Base and Target files
 		final R4ECompareEditorInput input = CommandUtils.createCompareEditorInput(aFile.getBase(), aFile.getTarget());
 		input.prepareCompareInputNoEditor();
 
 		final DiffUtils diffUtils = new DiffUtils();
 		final List<Diff> diffs;
 
 		fLock.lock();
 		try {
 			diffs = diffUtils.doDiff(false, true, input);
 		} finally {
 			fLock.unlock();
 		}
 
 		//Add Deltas from the list of differences
 		for (Diff diff : diffs) {
 			IR4EUIPosition position = CommandUtils.getPosition(diff.getPosition(R4EUIConstants.LEFT_CONTRIBUTOR)
 					.getOffset(), diff.getPosition(R4EUIConstants.LEFT_CONTRIBUTOR).getLength(),
 					diff.getDocument(R4EUIConstants.LEFT_CONTRIBUTOR));
 
 			if ((null == position) || (RangeDifference.NOCHANGE == diff.getKind())) {
 				continue; //Cannot resolve position for this delta or no change
 			}
 			aFile.getPositions().add(position);
 		}
 	}
 
 	/**
 	 * Method addFileToModel.
 	 * 
 	 * @param aUiReviewItem
 	 *            R4EUIReviewItem
 	 * @param aFile
 	 *            TempFileContext
 	 * @param aMonitor
 	 *            IProgressMonitor
 	 */
 	private void addFileToModel(R4EUIReviewItem aUiReviewItem, TempFileContext aFile, IProgressMonitor aMonitor) {
 		try {
 			String addedFilename;
 			if (null != aFile.getTarget()) {
 				addedFilename = aFile.getTarget().getName();
 			} else if (null != aFile.getBase()) {
 				addedFilename = aFile.getBase().getName();
 			} else {
 				addedFilename = ""; //Should never happen //$NON-NLS-1$
 			}
 			aMonitor.subTask("Adding file " + addedFilename + " to R4E model"); //$NON-NLS-1$//$NON-NLS-2$
 			final R4EUIFileContext uiFileContext = aUiReviewItem.createFileContext(aFile.getBase(), aFile.getTarget(),
 					aFile.getType());
 
 			for (IR4EUIPosition position : aFile.getPositions()) {
 				//Lazily create the Delta container if not already done
 				R4EUIDeltaContainer deltaContainer = (R4EUIDeltaContainer) uiFileContext.getContentsContainerElement();
 				deltaContainer.createDelta((R4EUITextPosition) position);
 			}
 		} catch (OutOfSyncException e) {
 			R4EUIPlugin.Ftracer.traceError(R4EUIConstants.EXCEPTION_MSG + e.toString() + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
 			R4EUIPlugin.getDefault().logError(R4EUIConstants.EXCEPTION_MSG + e.toString(), e);
 		} catch (ResourceHandlingException e) {
 			R4EUIPlugin.Ftracer.traceError(R4EUIConstants.EXCEPTION_MSG + e.toString() + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
 			R4EUIPlugin.getDefault().logError(R4EUIConstants.EXCEPTION_MSG + e.toString(), e);
 		}
 		aMonitor.worked(1);
 	}
 
 	/**
 	 * Class TempFileContext internal class that aggregates info needed to import files into R4E
 	 * 
 	 * @author Sebastien Dubois
 	 */
 	private static class TempFileContext {
 		/**
 		 * Field base.
 		 */
 		private final R4EFileVersion fBase;
 
 		/**
 		 * Field target.
 		 */
 		private final R4EFileVersion fTarget;
 
 		/**
 		 * Field type.
 		 */
 		private final R4EContextType fType;
 
 		/**
 		 * Field positions.
 		 */
 		private final List<IR4EUIPosition> fPositions;
 
 		/**
 		 * Constructor for TempFileContext.
 		 * 
 		 * @param aRepository
 		 *            IRFSRegistry
 		 * @param aBase
 		 *            R4EFileVersion
 		 * @param aTarget
 		 *            R4EFileVersion
 		 * @param aType
 		 *            R4EContextType
 		 */
 		TempFileContext(IRFSRegistry aRepository, R4EFileVersion aBase, R4EFileVersion aTarget, R4EContextType aType) {
 			fBase = aBase;
 			//Add IFileRevision info
 			if ((null != fBase) && (null != aRepository)) {
 				try {
 					final IFileRevision fileRev = aRepository.getIFileRevision(null, fBase);
 					fBase.setFileRevision(fileRev);
 				} catch (ReviewsFileStorageException e) {
 					R4EUIPlugin.Ftracer.traceInfo(R4EUIConstants.EXCEPTION_MSG + e.toString() + " (" + e.getMessage() //$NON-NLS-1$
 							+ ")"); //$NON-NLS-1$
 				}
 			}
 
 			fTarget = aTarget;
 			//Add IFileRevision info
 			if ((null != fTarget) && (null != aRepository)) {
 				try {
 					final IFileRevision fileRev = aRepository.getIFileRevision(null, fTarget);
 					fTarget.setFileRevision(fileRev);
 				} catch (ReviewsFileStorageException e) {
 					R4EUIPlugin.Ftracer.traceInfo(R4EUIConstants.EXCEPTION_MSG + e.toString() + " (" + e.getMessage() //$NON-NLS-1$
 							+ ")"); //$NON-NLS-1$
 				}
 			}
 
 			fType = aType;
 			fPositions = new ArrayList<IR4EUIPosition>();
 		}
 
 		/**
 		 * Method getBase.
 		 * 
 		 * @return R4EFileVersion
 		 */
 		public R4EFileVersion getBase() {
 			return fBase;
 		}
 
 		/**
 		 * Method getTarget.
 		 * 
 		 * @return R4EFileVersion
 		 */
 		public R4EFileVersion getTarget() {
 			return fTarget;
 		}
 
 		/**
 		 * Method getType.
 		 * 
 		 * @return R4EContextType
 		 */
 		public R4EContextType getType() {
 			return fType;
 		}
 
 		/**
 		 * Method getPositions.
 		 * 
 		 * @return List<IR4EUIPosition>
 		 */
 		public List<IR4EUIPosition> getPositions() {
 			return fPositions;
 		}
 	}
 
 	/**
 	 * Class JobQueueSchedulingRule Job scheduling rule that only allows a certain number of concurrent Jobs
 	 * 
 	 * @author Sebastien Dubois
 	 */
 	class JobQueueSchedulingRule implements ISchedulingRule {
 
 		/**
 		 * Method contains.
 		 * 
 		 * @param aRule
 		 *            - ISchedulingRule
 		 * @return boolean
 		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(ISchedulingRule)
 		 */
 		public boolean contains(ISchedulingRule aRule) {
 			return aRule.equals(this);
 		}
 
 		/**
 		 * Method isConflicting.
 		 * 
 		 * @param aRule
 		 *            - ISchedulingRule
 		 * @return boolean
 		 * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(ISchedulingRule)
 		 */
 		public boolean isConflicting(ISchedulingRule aRule) {
 			if (aRule.equals(this)) {
 				return true;
 			}
 
 			if (!(aRule instanceof JobQueueSchedulingRule)) {
 				return false;
 			}
 
 			/* Conflict if number of running Jobs already greater maximum */
 			return FindReviewItemsHandler.this.fRunningJobs.intValue() >= MAX_CONCURRRENT_JOBS;
 		}
 	}
 }
