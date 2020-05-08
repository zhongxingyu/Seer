 package net.sourceforge.eclipseccase.ui.actions;
 
 import net.sourceforge.eclipseccase.ClearCasePreferences;
 
 import org.eclipse.core.runtime.IStatus;
 
 import net.sourceforge.clearcase.ClearCase;
 
 import java.io.File;
 
 import net.sourceforge.eclipseccase.ClearDlgHelper;
 
 import java.util.Arrays;
 import net.sourceforge.eclipseccase.ClearCasePlugin;
 import net.sourceforge.eclipseccase.ClearCaseProvider;
 import net.sourceforge.eclipseccase.ui.CommentDialog;
 import net.sourceforge.eclipseccase.ui.DirectoryLastComparator;
 import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.window.Window;
 
 public class CheckInAction extends ClearCaseWorkspaceAction {
 
 	/*
 	 * Method declared on IActionDelegate.
 	 */
 	@Override
 	public void execute(IAction action) {
 		String maybeComment = "";
 		int maybeDepth = IResource.DEPTH_ZERO;
 		CommentDialog dlg;
 		if (!ClearCasePreferences.isUseClearDlg() && ClearCasePreferences.isCommentCheckin()) {
 			IResource[] resources = getSelectedResources();
 			String extCoComment = getLastExtCoComment(resources);
 			if (!extCoComment.equalsIgnoreCase("")) {
 				dlg = new CommentDialog(getShell(), "Checkin comment", extCoComment);
 			} else {
 				dlg = new CommentDialog(getShell(), "Checkin comment");
 			}
 			if (dlg.open() == Window.CANCEL)
 				return;
 			maybeComment = dlg.getComment();
 			maybeComment = dlg.getComment();
 			maybeDepth = dlg.isRecursive() ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
 		}
 
 		// bug 859094 and feature request 718203: prompt for saving dirty
 		// editors
 		boolean canContinue = true;
 		IFile[] unsavedFiles = getUnsavedFiles();
 		if (unsavedFiles.length > 0) {
 			canContinue = saveModifiedResourcesIfUserConfirms(unsavedFiles);
 		}
 
 		if (canContinue) {
 			final String comment = maybeComment;
 			final int depth = maybeDepth;
 
 			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 
 				public void run(IProgressMonitor monitor) throws CoreException {
 
 					try {
 						IResource[] resources = getSelectedResources();
 						if (resources.length > 0) {
 							beginTask(monitor, "Checking in...", resources.length);
 
 							if (ClearCasePreferences.isUseClearDlg()) {
 								monitor.subTask("Executing ClearCase user interface...");
 								ClearDlgHelper.checkin(resources);
 							} else {
 								ConsoleOperationListener opListener = new ConsoleOperationListener(monitor);
 
 								// Sort resources with directories last so that
 								// the
 								// modification of a
 								// directory doesn't abort the modification of
 								// files
 								// within it.
 								Arrays.sort(resources, new DirectoryLastComparator());
 
 								for (int i = 0; i < resources.length; i++) {
 									IResource resource = resources[i];
 									ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 									if (provider != null) {
 										provider.setComment(comment);
 										provider.setOperationListener(opListener);
 										provider.checkin(new IResource[] { resource }, depth, subMonitor(monitor));
 									}
 								}
 							}
 						}
 					} finally {
 						monitor.done();
 						updateActionEnablement();
 					}
 				}
 			};
 
 			executeInBackground(runnable, "Checking in ClearCase resources");
 		}
 	}
 
 	@Override
 	public boolean isEnabled() {
 		IResource[] resources = getSelectedResources();
 		if (resources.length == 0)
 			return false;
 		for (int i = 0; i < resources.length; i++) {
 			IResource resource = resources[i];
 			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearCaseElement(resource))
 				return false;
 			if (!provider.isCheckedOut(resource))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Method retrieves the check-out comment for the last modified resource
 	 * outside the eclipse workspace.
 	 * 
 	 * @param resources
 	 * @return comment
 	 */
 	private String getLastExtCoComment(IResource[] resources) {
 		long lastModificationTime = 0L;
 		IResource lastModifiedResource = null;
		StringBuffer comment = new StringBuffer();
 		String lastComment = "";
 		for (IResource iResource : resources) {
 			String path = iResource.getLocation().toOSString();
 			File file = new File(path);
 			long modificationTime = file.lastModified();
 			if (modificationTime == 0L) {
 				ClearCasePlugin.log(IStatus.WARNING, "Could not access resource," + iResource.getName(), null);
 			}
 			if (modificationTime > lastModificationTime) {
 				lastModificationTime = modificationTime;
 				lastModifiedResource = iResource;
 			}
 
 		}
 
 		// get comment for last modified resource.
 		ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(lastModifiedResource);
 		if (provider != null) {
 			String element = lastModifiedResource.getLocation().toOSString();
 			String[] output = provider.describe(element, ClearCase.FORMAT, "%c");
 			if (output.length > 0) {
				for (int i = 0; i < output.length; i++) {
					comment.append(output[i] + "\n");
				}

 			}
			lastComment = comment.toString();
 		}
 		return lastComment;
 	}
 
 }
