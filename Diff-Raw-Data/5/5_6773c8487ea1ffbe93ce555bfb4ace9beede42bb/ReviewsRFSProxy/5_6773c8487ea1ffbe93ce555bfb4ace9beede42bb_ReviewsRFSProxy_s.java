 /*******************************************************************************
  * Copyright (c) 2011 Ericsson
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * Contributors:
  *   Alvaro Sanchez-Leon - Initial API
  *******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.core.rfs;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.egit.core.RepositoryUtil;
 import org.eclipse.jgit.lib.Constants;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.ObjectInserter;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.lib.RepositoryCache.FileKey;
 import org.eclipse.jgit.storage.file.FileRepository;
 import org.eclipse.jgit.util.FS;
 import org.eclipse.mylyn.reviews.r4e.core.Activator;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry;
 import org.eclipse.mylyn.reviews.r4e.core.rfs.spi.ReviewsFileStorageException;
 import org.eclipse.mylyn.reviews.r4e.core.utils.IOUtils;
 import org.eclipse.mylyn.reviews.r4e.core.utils.filePermission.FileSupportCommandFactory;
 import org.eclipse.mylyn.reviews.r4e.core.versions.ReviewVersionsException;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.team.core.history.provider.FileRevision;
 
 /**
  * @author lmcalvs
  * 
  */
 public class ReviewsRFSProxy implements IRFSRegistry {
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	private static final String	repoName	= "ReviewsRepo.git";
 	private ObjectInserter		fInserter	= null;
 	protected Repository			fRepository	= null;
 	protected final RepositoryUtil fRepositoryUtil;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 	/**
 	 * @param aParentDir
 	 * @throws IOException
 	 */
 	public ReviewsRFSProxy(File aReviewGroupDir, boolean create) throws ReviewsFileStorageException {
 		File repoLoc = new File(aReviewGroupDir, repoName);
 		if (create) {
 			fRepository = initializeRepo(repoLoc);
 			// Set writing permissions to the shared location
 			try {
 				FileSupportCommandFactory.getInstance().grantWritePermission(aReviewGroupDir.getAbsolutePath());
 			} catch (IOException e) {
 				throw new ReviewsFileStorageException(e);
 			}
 		} else {
 			// permissions must have been already set
 			fRepository = openRepository(repoLoc);
 		}
 		fInserter = fRepository.newObjectInserter();
 		fRepositoryUtil = org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil();
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Validate if the given directory is a valid repository
 	 * 
 	 * @param dir
 	 * @return
 	 */
 	public static boolean isValidRepo(File dir) {
 		File repoDir = new File(dir, repoName);
 		return FileKey.isGitRepository(repoDir, FS.DETECTED);
 	}
 
 	/**
 	 * @param aReviewGroupDir
 	 * @return
 	 * @throws ReviewVersionsException
 	 */
 	private Repository initializeRepo(File aReviewGroupDir) throws ReviewsFileStorageException {
 		try {
 			Repository newRepo = new FileRepository(aReviewGroupDir);
 			newRepo.create(true);
 			newRepo.getConfig().setString("core", null, "sharedrepository", "0666");
 			newRepo.getConfig().save();
 
 			return newRepo;
 		} catch (IOException e) {
 			throw new ReviewsFileStorageException(e);
 		}
 	}
 
 	/**
 	 * @param aReviewGroupDir
 	 * @return
 	 * @throws ReviewVersionsException
 	 */
 	private Repository openRepository(File aReviewGroupDir) throws ReviewsFileStorageException {
 		Repository r;
 		try {
 			r = FileKey.exact(aReviewGroupDir, FS.DETECTED).open(true);
 		} catch (IOException e) {
 			throw new ReviewsFileStorageException(e);
 		}
 		return r;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.core.rrepo.IBlobRegistry#registerReviewBlob(byte[])
 	 */
 	public String registerReviewBlob(final byte[] content) throws ReviewsFileStorageException {
 		String id = null;
 		ObjectId objid = null;
 		try {
 			objid = fInserter.insert(Constants.OBJ_BLOB, content);
 			FileSupportCommandFactory.getInstance().grantWritePermission(fRepository.getDirectory().getAbsolutePath());
 			fInserter.flush();
 		} catch (IOException e) {
 			//Check if the file has been registered in the repository if yes record the id, log the exception and continue
 			//If the id is not registered throw the exception
 			id = blobIdFor(content);
 			InputStream is = getBlobContent(null, id);
 			
 			if (is == null) {
 				//The file was not registered in the local repo
 				throw new ReviewsFileStorageException(e);
 			} else {
 				Activator.getDefault().fTracer.traceError("IOException while registering content however it's already available in the local repository, " + e.getMessage());
 				try {
 					is.close();
 				} catch (IOException e1) {
 					Activator.getDefault().fTracer.traceError("IOException while closing probe stream, " + e1.getMessage());
 				}
 			}
 		} finally {
 			fInserter.release();
 		}
 
 		if (objid != null) {
 			id = objid.getName();
 		}
 
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry#registerReviewBlob(java.io.InputStream)
 	 */
 	public String registerReviewBlob(final InputStream content) throws ReviewsFileStorageException {
 		try {
 			return registerReviewBlob(IOUtils.readFully(content));
 		} catch (IOException e) {
 			throw new ReviewsFileStorageException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.core.rrepo.IBlobRegistry#registerReviewBlob(java.io.File)
 	 */
 	public String registerReviewBlob(final File aFromFile) throws ReviewsFileStorageException {
 		InputStream stream = null;
 		try {
 			stream = new FileInputStream(aFromFile);
 		} catch (FileNotFoundException e) {
 			throw new ReviewsFileStorageException(e);
 		}
 
 		String id = null;
 		ObjectId objid = null;
 		try {
 			objid = fInserter.insert(Constants.OBJ_BLOB, aFromFile.length(), stream);
 			fInserter.flush();
 			FileSupportCommandFactory.getInstance().grantWritePermission(fRepository.getDirectory().getAbsolutePath());
 		} catch (IOException e) {
 			throw new ReviewsFileStorageException(e);
 		} finally {
 			fInserter.release();
 			if (stream != null) {
 				try {
 					stream.close();
 				} catch (IOException e) {
 					StringBuilder sb = new StringBuilder("Exception: " + e.getMessage());
 					Activator.fTracer.traceDebug(sb.toString());
 				}
 			}
 		}
 
 		if (objid != null) {
 			id = objid.getName();
 		}
 
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry#getBlobContent(org.eclipse.core.runtime.IProgressMonitor,
 	 * java.lang.String)
 	 */
 	public InputStream getBlobContent(IProgressMonitor monitor, String id) throws ReviewsFileStorageException {
 		InputStream resStream = null;
 
 		if (id == null) {
 			return null;
 		}
 
 		try {
 			ObjectId objId = ObjectId.fromString(id);
 			resStream = fRepository.open(objId, Constants.OBJ_BLOB).openStream();
 		} catch (Exception e) {
 			throw new ReviewsFileStorageException(e);
 		}
 
 		return resStream;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry#getIFileRevision(org.eclipse.core.runtime.IProgressMonitor
 	 * , org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion)
 	 */
 	public IFileRevision getIFileRevision(IProgressMonitor monitor, final R4EFileVersion fileVersion)
 			throws ReviewsFileStorageException {
 		final String localId = fileVersion.getLocalVersionID();
 
 		// Validation
 		if (localId == null) {
 			return null;
 		}
 
 		try {
 			final IPath path = Path.fromPortableString(fileVersion.getRepositoryPath());
 			return new FileRevision() {
 
 				public IFileRevision withAllProperties(IProgressMonitor monitor) throws CoreException {
 					return this;
 				}
 
 				public boolean isPropertyMissing() {
 					return false;
 				}
 
 				public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
 					return getIStorage(null, fileVersion);
 				}
 
 				public String getName() {
 					return path.lastSegment();
 				}
 			};
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry#getIStorage(org.eclipse.core.runtime.IProgressMonitor,
 	 * org.eclipse.mylyn.reviews.r4e.core.model.R4EFileVersion)
 	 */
 	public IStorage getIStorage(IProgressMonitor monitor, R4EFileVersion fileVersion) {
 
 		final IPath path = Path.fromPortableString(fileVersion.getRepositoryPath());
 		final String localId = fileVersion.getLocalVersionID();
 
 		// Validation
 		if (localId == null) {
 			return null;
 		}
 
 		return new IStorage() {
 
 			@SuppressWarnings("rawtypes")
 			public Object getAdapter(Class adapter) {
 				return null;
 			}
 
 			public boolean isReadOnly() {
 				return true;
 			}
 
 			public String getName() {
 				return path.lastSegment();
 			}
 
 			public IPath getFullPath() {
 				//bug349739:  Here we first need to prepend the repository name to the artifact path to get the full path to the artifact
 				IPath repoPath = new Path(fRepositoryUtil.getRepositoryName(fRepository));
 				String pathString = path.toPortableString();
				return repoPath.append(Path.fromPortableString(pathString));
 			}
 
 			public InputStream getContents() throws CoreException {
 				try {
 					return getBlobContent(null, localId);
 				} catch (Exception e) {
 					e.printStackTrace();
 					throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage()));
 				}
 			}
 		};
 	}
 
 	/**
 	 * @return
 	 */
 	public Repository getRepository() {
 		return fRepository;
 	}
 
 	/**
 	 * Convenience method to close the repository data base
 	 */
 	public void close() {
 		fRepository.close();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry#blobIdFor(java.io.InputStream)
 	 */
 	public String blobIdFor(InputStream content) throws ReviewsFileStorageException {
 		String id = null;
 		try {
 			id = blobIdFor(IOUtils.readFully(content));
 		} catch (IOException e) {
 			throw new ReviewsFileStorageException(e);
 		}
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry#blobIdFor(byte[])
 	 */
 	public String blobIdFor(byte[] content) {
 		String id = fInserter.idFor(Constants.OBJ_BLOB, content).getName();
 		fInserter.release();
 
 		return id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.mylyn.reviews.r4e.core.rfs.spi.IRFSRegistry#blobIdFor(java.io.File)
 	 */
 	public String blobIdFor(File aFromFile) throws ReviewsFileStorageException {
 		InputStream stream = null;
 		try {
 			stream = new FileInputStream(aFromFile);
 		} catch (FileNotFoundException e) {
 			throw new ReviewsFileStorageException(e);
 		}
 
 		String id = null;
 		ObjectId objid = null;
 		try {
 			objid = fInserter.idFor(Constants.OBJ_BLOB, aFromFile.length(), stream);
 			FileSupportCommandFactory.getInstance().grantWritePermission(fRepository.getDirectory().getAbsolutePath());
 		} catch (IOException e) {
 			throw new ReviewsFileStorageException(e);
 		} finally {
 			fInserter.release();
 			if (stream != null) {
 				try {
 					stream.close();
 				} catch (IOException e) {
 					StringBuilder sb = new StringBuilder("Exception: " + e.getMessage());
 					Activator.fTracer.traceDebug(sb.toString());
 				}
 			}
 		}
 
 		if (objid != null) {
 			id = objid.getName();
 		}
 
 		return id;
 	}
 
 }
