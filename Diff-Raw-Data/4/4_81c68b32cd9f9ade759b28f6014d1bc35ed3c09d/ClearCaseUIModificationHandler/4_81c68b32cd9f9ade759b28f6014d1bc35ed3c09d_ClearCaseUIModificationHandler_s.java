 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Gunnar Wagenknecht - initial API and implementation
  *     IBM Corporation - concepts and ideas from Eclipse
  *******************************************************************************/
 
 package net.sourceforge.eclipseccase.ui;
 
 import java.lang.reflect.InvocationTargetException;
 import net.sourceforge.eclipseccase.*;
 import net.sourceforge.eclipseccase.ui.preferences.ClearCasePreferenceStore;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.team.FileModificationValidationContext;
 import org.eclipse.core.runtime.*;
 import org.eclipse.core.runtime.jobs.ILock;
 import org.eclipse.core.runtime.jobs.MultiRule;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * A ClearCase modification handler that uses the Eclipse UI to show feedback.
  * <p>
  * This class is not intended to be subclassed, instanciated or called outside
  * the Eclipse ClearCase integration.
  * </p>
  */
 class ClearCaseUIModificationHandler extends ClearCaseModificationHandler {
 
 	/** the preference store */
 	static final IPreferenceStore store = new ClearCasePreferenceStore();
 
 	/** the lock to handle concurrent validate edit requests */
 	private final ILock validateEditLock = Platform.getJobManager().newLock();
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 */
 	protected ClearCaseUIModificationHandler() {
 		super();
 	}
 
 	/**
 	 * Checks out the specified files.
 	 * 
 	 * @param files
 	 * @param shell
 	 * @return a status describing the result
 	 */
 	private IStatus checkout(final IFile[] files, final Shell shell) {
 		final ClearCaseProvider provider = getProvider(files);
 		if (PreventCheckoutHelper.isPreventedFromCheckOut(shell, provider, files, true))
 			return CANCEL;
 		// check for provider
 		if (null == provider) {
 			ClearCasePlugin.log(Messages.getString("ClearCaseUIModificationHandler.error.noProvider"), //$NON-NLS-1$
 					new IllegalStateException(Messages.getString("ClearCaseUIModificationHandler.error.noProviderAvailable"))); //$NON-NLS-1$
 			MessageDialog.openError(shell, Messages.getString("ClearCaseUIModificationHandler.errorDialog.title"), //$NON-NLS-1$
 					Messages.getString("ClearCaseUIModificationHandler.errorDialog.message")); //$NON-NLS-1$
 			return CANCEL;
 		}
 
 		final boolean useClearDlg = ClearCasePreferences.isUseClearDlg();
 		final boolean askForComment = ClearCasePreferences.isCommentCheckout() && !ClearCasePreferences.isCommentCheckoutNeverOnAuto();
 
 		// UCM checkout.
 		if (ClearCasePreferences.isUCM() && !ClearCasePreferences.isUseClearDlg()) {
 			if (!UcmActivity.checkoutWithActivity(provider, files, shell))
 				// no checkout
 				return CANCEL;
 		}
 
 		try {
 			// use workbench window as preferred runnable context
 			IRunnableContext context;
 			if (PlatformUI.isWorkbenchRunning() && (null != PlatformUI.getWorkbench().getActiveWorkbenchWindow())) {
 				context = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 			} else {
 				context = new ProgressMonitorDialog(shell);
 			}
 
 			// checkout
 			PlatformUI.getWorkbench().getProgressService().runInUI(context, new IRunnableWithProgress() {
 
 				public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 					try {
 						String comment = null;
 
 						if (!useClearDlg && askForComment) {
 							CommentDialog dlg = new CommentDialog(shell, "Checkout comment");
 							dlg.setRecursive(false);
 							dlg.setRecursiveEnabled(false);
 							if (dlg.open() == Window.CANCEL)
 								throw new InterruptedException("Operation canceled by user.");
 							else {
 								comment = dlg.getComment();
 							}
 						}
 
 						synchronized (provider) {
 							boolean refreshing = setResourceRefreshing(provider, false);
 							try {
 								monitor.beginTask(Messages.getString("ClearCaseUIModificationHandler.task.checkout"), files.length); //$NON-NLS-1$
 								if (ClearCasePreferences.isUseClearDlg()) {
 									monitor.subTask("Executing ClearCase user interface...");
 									ClearDlgHelper.checkout(files);
 								} else {
 									if (null != comment) {
 										provider.setComment(comment);
 									}
 
 									for (int i = 0; i < files.length; i++) {
 										IFile file = files[i];
 										monitor.subTask(file.getName());
 										provider.checkout(new IFile[] { file }, IResource.DEPTH_ZERO, null);
 										file.refreshLocal(IResource.DEPTH_ZERO, null);
 										monitor.worked(i);
 									}
 								}
 							} finally {
 								setResourceRefreshing(provider, refreshing);
 								monitor.done();
 							}
 						}
 					} catch (CoreException ex) {
 						throw new InvocationTargetException(ex);
 					}
 				}
 			}, new MultiRule(files));
 		} catch (InvocationTargetException e) {
 			ClearCasePlugin.log(Messages.getString("ClearCaseUIModificationHandler.error.checkout") //$NON-NLS-1$
 					+ (null != e.getCause() ? e.getCause().getMessage() : e.getMessage()), null != e.getCause() ? e.getCause() : e);
 			MessageDialog.openError(shell, Messages.getString("ClearCaseUIModificationHandler.errorDialog.title"), //$NON-NLS-1$
 					Messages.getString("ClearCaseUIModificationHandler.errorDialog.message")); //$NON-NLS-1$
 			return CANCEL;
 		} catch (InterruptedException e) {
 			return CANCEL;
 		}
 
 		return OK;
 	}
 
 	/**
 	 * Returns the shell context
 	 * 
 	 * @param context
 	 * @return the shell context
 	 */
 	private Shell getShell(final Object context) {
 		if (context instanceof Shell)
 			return (Shell) context;
 		if (context instanceof FileModificationValidationContext)
 			return (Shell) (((FileModificationValidationContext) context).getShell());
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sourceforge.eclipseccase.ClearCaseModificationHandler#validateEdit
 	 * (org.eclipse.core.resources.IFile[],
 	 * org.eclipse.core.resources.team.FileModificationValidationContext)
 	 */
 	@Override
 	public IStatus validateEdit(final IFile[] files, final FileModificationValidationContext context) {
 		System.out.println("In validate edit");
 		if (ClearCasePreferences.isCheckoutAutoNever())
 			return CANCEL;
 		final ClearCaseProvider provider = getProvider(files);
 		final Shell shell = getShell(context);
 		final boolean askForComment = ClearCasePreferences.isCommentCheckout() && !ClearCasePreferences.isCommentCheckoutNeverOnAuto();
 		if (null == shell || !askForComment) {
 
 			if (PreventCheckoutHelper.isPreventedFromCheckOut(shell, provider, files, ClearCasePreferences.isSilentPrevent())) {
 				return CANCEL;
 			}
 			// UCM checkout.
 			if (ClearCasePreferences.isUCM() && !ClearCasePreferences.isUseClearDlg()) {
 				if (!UcmActivity.checkoutWithActivity(provider, files, shell))
 					// no checkout
 					return CANCEL;
 			}
			
			return super.validateEdit(files, context);
 		}
 		try {
 			this.validateEditLock.acquire();
 			final IFile[] readOnlyFiles = getFilesToCheckout(files);
 			if (readOnlyFiles.length == 0)
 				return OK;
 			final IStatus[] status = new IStatus[1];
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 
 				public void run() {
 					status[0] = checkout(readOnlyFiles, shell);
 				}
 			});
 			if (null == status[0])
 				return CANCEL;
 			return status[0];
 		} finally {
 			this.validateEditLock.release();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sourceforge.eclipseccase.ClearCaseModificationHandler#validateSave
 	 * (org.eclipse.core.resources.IFile)
 	 */
 	@Override
 	public IStatus validateSave(final IFile file) {
 		try {
 			this.validateEditLock.acquire();
 			if (!needsCheckout(file))
 				return OK;
 			final IStatus[] status = new IStatus[1];
 			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
 
 				public void run() {
 					status[0] = checkout(new IFile[] { file }, PlatformUI.getWorkbench().getDisplay().getActiveShell());
 				}
 			});
 			if (null == status[0])
 				return CANCEL;
 			return status[0];
 		} finally {
 			this.validateEditLock.release();
 		}
 	}
 
 }
