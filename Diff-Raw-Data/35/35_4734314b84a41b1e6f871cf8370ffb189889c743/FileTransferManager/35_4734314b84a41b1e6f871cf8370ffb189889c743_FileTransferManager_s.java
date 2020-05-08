 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Jan Finis - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.filetransfer;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.emfstore.internal.client.model.impl.ProjectSpaceBase;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.internal.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.internal.server.exceptions.FileTransferException;
 import org.eclipse.emf.emfstore.internal.server.model.FileIdentifier;
 import org.eclipse.emf.emfstore.internal.server.model.ModelFactory;
 
 /**
  * The main managing class on the client side for file transfers. Each project
  * space has an associated FileTransferManager. All file-related request from
  * the project space are delegated to that manager. The manager provides methods
  * to add files, get files and retrieve additional information about files.
  * 
  * @author jfinis
  */
 public class FileTransferManager {
 
 	/**
 	 * The associated cache manager.
 	 */
 	private final FileTransferCacheManager cacheManager;
 
 	/**
 	 * The associated project space.
 	 */
 	private final ProjectSpaceBase projectSpace;
 
 	/**
 	 * Constructor that creates a file transfer manager for a specific project
 	 * space. Only to be called in the init of a project space!
 	 * 
 	 * @param projectSpaceImpl
 	 *            the project space to which this transfer manager belongs
 	 */
 	public FileTransferManager(ProjectSpaceBase projectSpaceImpl) {
 		cacheManager = new FileTransferCacheManager(projectSpaceImpl);
 		projectSpace = projectSpaceImpl;
 	}
 
 	/**
 	 * Adds a file to be transferred (uploaded).
 	 * 
 	 * @param file
 	 *            the file to be transferred
 	 * 
 	 * @return the {@link FileIdentifier} that associates the file with its ID
 	 * 
 	 * @throws FileTransferException in case the {@code file} is either {@code null}, a directory
 	 *             or does not exist
 	 */
 	public FileIdentifier addFile(File file) throws FileTransferException {
 		return addFile(file, null);
 	}
 
 	/**
 	 * Adds a file to be transferred (uploaded).
 	 * 
 	 * @param file
 	 *            the file to be transferred
 	 * @param id
 	 *            the ID that will be associated with the file being uploaded
 	 * 
 	 * @return the {@link FileIdentifier} that associates the file with its ID
 	 * 
 	 * @throws FileTransferException in case the {@code file} is either {@code null}, a directory
 	 *             or does not exist
 	 */
 	public FileIdentifier addFile(File file, String id) throws FileTransferException {
 		if (file == null) {
 			throw new FileTransferException("File to be added is null!");
 		}
 		if (file.isDirectory()) {
 			throw new FileTransferException("Can only upload files! File is a directory.\nPath:"
 				+ file.getAbsolutePath());
 		}
 		if (!file.exists()) {
 			throw new FileTransferException("The file to be uploaded does not exist.\nPath:" + file.getAbsolutePath());
 		}
 
 		// Create the file identifier
 		final FileIdentifier identifier = ModelFactory.eINSTANCE.createFileIdentifier();
 		if (id != null) {
 			identifier.setIdentifier(id);
 		}
 
 		// Move file to cache
 		try {
 			cacheManager.cacheFile(file, identifier);
 		} catch (final IOException e) {
 			throw new FileTransferException("An exception occurred while trying to cache an incoming file:\n"
 				+ e.getMessage(), e);
 		}
 
 		// Add the file to the queue for files that should be
 		// transmitted when a commit is done
 		addToCommitQueue(identifier);
 
 		return identifier;
 	}
 
 	/**
 	 * Adds a file to the queue of pending uploads.
 	 * 
 	 * @param identifier
 	 */
 	private void addToCommitQueue(final FileIdentifier identifier) {
 		for (final FileIdentifier f : projectSpace.getWaitingUploads()) {
 			if (f.getIdentifier().equals(identifier.getIdentifier())) {
 				return;
 			}
 
 		}
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				projectSpace.getWaitingUploads().add(identifier);
 				projectSpace.saveProjectSpaceOnly();
 			}
 		}.run(true);
 
 	}
 
 	/**
 	 * Uploads all files in the commit queue. Is called upon committing the
 	 * project space.
 	 * 
 	 * @param progress
 	 *            progress monitor
 	 */
 	public void uploadQueuedFiles(IProgressMonitor progress) {
 		try {
 			final EList<FileIdentifier> uploads = projectSpace.getWaitingUploads();
 			while (!uploads.isEmpty()) {
 				final FileIdentifier fi = uploads.get(0);
 
 				// Is the file present in cache?
 				// (it should be, unless there is a severe bug or the user has
 				// manually deleted it)
 				if (!cacheManager.hasCachedFile(fi)) {
 					WorkspaceUtil.logException("The file with the id " + fi.getIdentifier()
 						+ " was not found in cache. It was queued for upload but"
 						+ " is now removed from the queue. The file will NOT be on the server.", null);
 					// Remove from commit queue
 					new EMFStoreCommand() {
 						@Override
 						protected void doRun() {
 							projectSpace.getWaitingUploads().remove(fi);
 							projectSpace.saveProjectSpaceOnly();
 						}
 					}.run(true);
 					continue;
 
 				}
 				final FileUploadJob job = new FileUploadJob(this, fi, true);
 				final IStatus result = job.run(progress);
 
 				if (job.getException() != null) {
 					WorkspaceUtil.logException("An exception occurred while trying to upload a file to the server",
 						job.getException());
 					return;
 				}
 
 				if (result.getCode() == IStatus.CANCEL) {
 					return;
 				}
 			}
 		} catch (final FileTransferException e) {
 			WorkspaceUtil.logException("Uploading the waiting files did not succeed", e);
 		}
 	}
 
 	/**
 	 * Returns the download status of the file that is associated with the given {@link FileIdentifier}.
 	 * 
 	 * @param fileIdentifier
 	 *            the file identifier whose download status should be retrieved
 	 * 
 	 * @return the download status of the file
 	 * 
 	 * @throws FileTransferException in case the given file identifier is {@code null}
 	 */
	public FileDownloadStatus getFile(FileIdentifier fileIdentifier) throws FileTransferException {
 
 		if (fileIdentifier == null) {
 			throw new FileTransferException("File identifier may not be null,");
 		}
 
 		// If the file is cached locally, get it
 		if (cacheManager.hasCachedFile(fileIdentifier)) {
 			return FileDownloadStatus.Factory.createAlreadyFinished(projectSpace, fileIdentifier,
 				cacheManager.getCachedFile(fileIdentifier));
 		}
 
 		// Otherwise, start a download
		return startDownload(fileIdentifier);
 	}
 
 	/**
 	 * Starts a download of a specific file. Returns a status object that can be
 	 * queried to check how far the download is.
 	 * 
 	 * @param fileIdentifier
 	 *            the file to be downloaded
 	 * @param monitor
 	 *            a progress monitor for the download
 	 * @return the status
 	 */
	private FileDownloadStatus startDownload(FileIdentifier fileIdentifier) {
 		final FileDownloadStatus fds = FileDownloadStatus.Factory.createNew(projectSpace, fileIdentifier);
 		// TODO Check if true is correct here
		final FileDownloadJob job = new FileDownloadJob(fds, this, fileIdentifier, true);
 		job.schedule();
 		return fds;
 	}
 
 	/**
 	 * Returns the cache manager.
 	 * 
 	 * @return the associated cache manager
 	 */
 	FileTransferCacheManager getCache() {
 		return cacheManager;
 	}
 
 	/**
 	 * Gets the index of a waiting file upload in the upload queue or -1 if this
 	 * upload is not in the queue.
 	 * 
 	 * @param fileId
 	 *            the index to be looked up in the queue
 	 * @return the index in the queue or -1
 	 */
 	private int getWaitingUploadIndex(FileIdentifier fileId) {
 		if (fileId == null) {
 			return -1;
 		}
 
 		int i = 0;
 		/*
 		 * We need to loop over the pending uploads here. This is because we
 		 * cannot use .remove(fileId) because remove uses equals to check for
 		 * the element. Equals is not well-defined for EObjects, so we cannot
 		 * use it here.
 		 */
 		for (final FileIdentifier upload : projectSpace.getWaitingUploads()) {
 			// This is our equals: Compare the strings!
 			if (upload.getIdentifier().equals(fileId.getIdentifier())) {
 				return i;
 			}
 			i++;
 		}
 		return -1;
 	}
 
 	/**
 	 * Removes a waiting upload from the queue. Throws a file transfer exception
 	 * if the fil is not in the list.
 	 * 
 	 * @param fileId
 	 *            the file to remove from the queue
 	 * @throws FileTransferException
 	 *             if the file is not in the queue
 	 */
 	void removeWaitingUpload(FileIdentifier fileId) throws FileTransferException {
 		final int index = getWaitingUploadIndex(fileId);
 		if (index != -1) {
 			projectSpace.getWaitingUploads().remove(index);
 			projectSpace.saveProjectSpaceOnly();
 
 		} else {
 			// Not found in list? exception!
 			throw new FileTransferException("Could not remove pending upload with id " + fileId
 				+ ": No upload with that id is pending");
 		}
 	}
 
 	/**
 	 * Return if a specific file is in the pending upload queue.
 	 * 
 	 * @param fileIdentifier
 	 *            the file to be looked up
 	 * @return true, if the file is in the queue
 	 */
 	public boolean hasWaitingUpload(FileIdentifier fileIdentifier) {
 		return getWaitingUploadIndex(fileIdentifier) != -1;
 	}
 
 	/**
 	 * Cancels a pending upload. That means that the upload is removed from the
 	 * queue and deleted from cache. If the file is not in the queue, nothing is
 	 * done. If it is in the queue but not in the cache, then it is only removed
 	 * from the queue.
 	 * 
 	 * @param fileIdentifier
 	 *            the file to be canceled
 	 */
 	public void cancelPendingUpload(FileIdentifier fileIdentifier) {
 		// Remove from the waiting queue
 		try {
 			removeWaitingUpload(fileIdentifier);
 		} catch (final FileTransferException e) {
 			return;
 		}
 
 		// Remove from cache
 		cacheManager.removeCachedFile(fileIdentifier);
 	}
 
 	/**
 	 * returns a file information object for a specific file identifier.
 	 * 
 	 * @param fileIdentifier
 	 *            the identifier
 	 * @return the file information for that identifier
 	 */
 	public FileInformation getFileInfo(FileIdentifier fileIdentifier) {
 		return new FileInformation(fileIdentifier, this);
 	}
 
 	/**
 	 * Returns the associated project space.
 	 * 
 	 * @return the project to which this file transfer manager belongs to
 	 */
 	ProjectSpaceBase getProjectSpace() {
 		return projectSpace;
 	}
 
 }
