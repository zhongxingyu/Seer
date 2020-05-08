 /*
  * Created On:  Sun Mar 06, 2005
  */
 package com.thinkparity.ophelia.model.document;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import com.thinkparity.codebase.StreamUtil;
 import com.thinkparity.codebase.Constants.ChecksumAlgorithm;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.event.EventNotifier;
 import com.thinkparity.codebase.jabber.JabberId;
 
 import com.thinkparity.codebase.model.DownloadMonitor;
 import com.thinkparity.codebase.model.artifact.ArtifactFlag;
 import com.thinkparity.codebase.model.artifact.ArtifactState;
 import com.thinkparity.codebase.model.artifact.ArtifactVersion;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentDraft;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.stream.StreamOpener;
 import com.thinkparity.codebase.model.stream.StreamUploader;
 import com.thinkparity.codebase.model.util.codec.MD5Util;
 
 import com.thinkparity.ophelia.model.Model;
 import com.thinkparity.ophelia.model.Constants.DirectoryNames;
 import com.thinkparity.ophelia.model.artifact.InternalArtifactModel;
 import com.thinkparity.ophelia.model.events.DocumentEvent;
 import com.thinkparity.ophelia.model.events.DocumentListener;
 import com.thinkparity.ophelia.model.io.IOFactory;
 import com.thinkparity.ophelia.model.io.handler.DocumentIOHandler;
 import com.thinkparity.ophelia.model.util.Opener;
 import com.thinkparity.ophelia.model.util.Printer;
 import com.thinkparity.ophelia.model.util.UUIDGenerator;
 import com.thinkparity.ophelia.model.util.sort.ComparatorBuilder;
 import com.thinkparity.ophelia.model.workspace.Workspace;
 
 /**
  * Implementation of the document model interface.
  * 
  * @author raykroeker@gmail.com
  * @version $Revision$
  */
 public final class DocumentModelImpl extends
         Model<DocumentListener> implements DocumentModel,
         InternalDocumentModel {
 
     
 	/** The default document version comparator. */
 	private final Comparator<ArtifactVersion> defaultVersionComparator;
 
     /** A document reader/writer. */
 	private DocumentIOHandler documentIO;
 
     /** A document event generator for local events. */
     private final DocumentModelEventGenerator localEventGen;
 
     /** The directory beneath which all files are stored. */
     private File localFilesDirectory;
 
 	private final DocumentNameGenerator nameGenerator;
 
     /**
 	 * Create a DocumentModelImpl
 	 * 
 	 * @param workspace
 	 *            The workspace to work within.
 	 */
 	public DocumentModelImpl() {
 		super();
 		final ComparatorBuilder comparatorBuilder = new ComparatorBuilder();
 		this.defaultVersionComparator = comparatorBuilder.createVersionById(Boolean.TRUE);
         this.localEventGen = new DocumentModelEventGenerator(DocumentEvent.Source.LOCAL);
         this.nameGenerator = new DocumentNameGenerator();
 	}
 
 	/**
      * @see com.thinkparity.ophelia.model.Model#addListener(com.thinkparity.codebase.event.EventListener)
      *
 	 */
     @Override
     public void addListener(final DocumentListener listener) {
         super.addListener(listener);
     }
 
     /**
      * Create a document.
      * 
      * @param name
      *            The document name.
      * @param content
      *            The document's content input stream.
      * @return The document.
      */
     public Document create(final String name, final InputStream content) {
         try {
             assertIsSetCredentials();
             // create
             final Document document = create(UUIDGenerator.nextUUID(), name,
                     content, localUserId(), currentDateTime());
             // fire event
             notifyDocumentCreated(document, localEventGen);
             return document;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#createDraft(com.thinkparity.codebase.model.document.DocumentLock,
      *      java.lang.Long)
      * 
      */
     public DocumentDraft createDraft(final DocumentFileLock lock, final Long documentId) {
         try {
             final Document document = read(documentId);
             final DocumentVersion latestVersion = readLatestVersion(documentId);
             writeVersion(document.getId(), latestVersion.getVersionId(), lock);
             return readDraft(lock, documentId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#createVersion(com.thinkparity.ophelia.model.document.DocumentFileLock, java.lang.Long, java.io.InputStream, java.lang.Integer, java.util.Calendar)
      *
      */
     public DocumentVersion createVersion(final DocumentFileLock lock,
             final Long documentId, final InputStream stream,
             final Calendar createdOn) {
         try {
             assertDraftIsModified(lock, documentId, "Draft has not been modified.");
 
             return createVersion(documentId, readNextVersionId(documentId),
                         stream, localUserId(), createdOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#createVersion(java.lang.Long,
      *      java.io.InputStream, java.lang.Integer, java.util.Calendar)
      * 
      */
     public DocumentVersion createVersion(final Long documentId,
             final InputStream stream, final Calendar createdOn) {
         try {
             assertDraftIsModified(documentId, "Draft has not been modified.");
 
             return createVersion(documentId, readNextVersionId(documentId),
                         stream, localUserId(), createdOn);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#delete(com.thinkparity.codebase.model.document.DocumentLock,
      *      java.lang.Long)
      * 
      */
     public void delete(final DocumentFileLock lock,
             final Map<DocumentVersion, DocumentFileLock> versionLocks,
             final Long documentId) {
         try {
             release(lock);
             deleteLocal(lock, versionLocks, documentId);
             // fire event
             notifyDocumentDeleted(null, localEventGen);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#deleteDraft(com.thinkparity.codebase.model.document.DocumentLock,
      *      java.lang.Long)
      * 
      */
     public void deleteDraft(final DocumentFileLock lock, final Long documentId) {
         try {
             assertDoesExistDraft(documentId, "Draft does not exist.");
             release(lock);
             Assert.assertTrue(lock.getFile().delete(),
                     "Could not delete draft file {0}.", lock.getFile());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Determine whether or not a draft exists.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @return True if the draft exists.
      */
     public Boolean doesExistDraft(final Long documentId) {
         logger.logApiId();
         logger.logVariable("documentId", documentId);
         try {
             return getDraftFile(read(documentId)).exists();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.DocumentModel#get(java.lang.Long)
      *
      */
     public Document get(final Long documentId) {
         return read(documentId);
     }
 
     /**
      * Obtain a document name generator.
      * 
      * @return A <code>DocumentNameGenerator</code>.
      */
     public DocumentNameGenerator getNameGenerator() {
         logger.logApiId();
         try {
             return new DocumentNameGenerator();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#handleDocumentPublished(java.lang.Long,
      *      com.thinkparity.codebase.model.document.DocumentVersion,
      *      java.lang.String, com.thinkparity.codebase.jabber.JabberId,
      *      java.util.Calendar)
      * 
      */
     public DocumentVersion handleDocumentPublished(final Long containerId,
             final DocumentVersion version, final String streamId,
             final JabberId publishedBy, final Calendar publishedOn) {
         logger.logApiId();
         logger.logVariable("version", version);
         logger.logVariable("streamId", streamId);
         try {
             final InternalArtifactModel artifactModel  = getArtifactModel();
             final Document document;
             final DocumentVersion localVersion;
             if (artifactModel.doesExist(version.getArtifactUniqueId())) {
                 logger.logInfo("Document {0} already exists.",
                         version.getArtifactUniqueId());
                 document = read(version.getArtifactUniqueId());
                 if (artifactModel.doesVersionExist(document.getId(),
                         version.getVersionId())) {
                     logger.logWarning(
                             "Document version {0}:{1} already exists.",
                             version.getArtifactUniqueId(), version.getVersionId());
                     localVersion = readVersion(document.getId(), version.getVersionId());
                 } else {
                     final File streamFile = downloadStream(new DownloadMonitor() {
                         public void chunkDownloaded(final int chunkSize) {
                             logger.logInfo("Downloaded {0} bytes", chunkSize);
                         }
                     }, streamId);
                     final InputStream stream = new FileInputStream(streamFile);
                     try {
                         localVersion = createVersion(document.getId(),
                                 version.getVersionId(), stream, publishedBy,
                                 publishedOn);
                     } finally {
                         stream.close();
                     }
                 }
             }
             else {
                 document = create(version.getArtifactUniqueId(),
                         version.getName(), publishedBy, publishedOn);
                 final File streamFile = downloadStream(new DownloadMonitor() {
                     public void chunkDownloaded(final int chunkSize) {}
                 }, streamId);
                 final InputStream stream = new FileInputStream(streamFile);
                 try {
                     localVersion = createVersion(document.getId(),
                             version.getVersionId(), stream,
                             publishedBy, publishedOn);
                 } finally {
                     stream.close();
                 }
                 getIndexModel().indexDocument(containerId, document.getId());
             }
             return localVersion;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#isDraftModified(com.thinkparity.ophelia.model.document.DocumentFileLock, java.lang.Long)
      *
      */
     public Boolean isDraftModified(final DocumentFileLock lock,
             final Long documentId) {
         try {
             final File draftFile = getDraftFile(lock);
             if (draftFile.exists()) {
                 final DocumentVersion latestVersion = readLatestVersion(documentId);
                 if (null == latestVersion) {
                     return Boolean.TRUE;
                 } else {
                     final long latestVersionCreatedOn =
                         latestVersion.getCreatedOn().getTimeInMillis();
                     /* the time stamp is checked first because it is fast;
                      * however documents are considered different only if the
                      * checksums are different */
                     if (draftFile.lastModified() == latestVersionCreatedOn) {
                         return Boolean.FALSE;
                     } else {
                         return !latestVersion.getChecksum().equals(checksum(lock));
                     }
                 }
             } else {
                 return Boolean.FALSE;
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Determine whether or not the draft of the document is different from the
      * latest version.
      * 
      * @return True if the draft is different from the latest version.
      */
     public Boolean isDraftModified(final Long documentId) {
         try {
             final File draftFile = getDraftFile(read(documentId));
             if (draftFile.exists()) {
                 final DocumentVersion latestVersion = readLatestVersion(documentId);
                 if (null == latestVersion) {
                     return Boolean.TRUE;
                 } else {
                     final long latestVersionCreatedOn =
                         latestVersion.getCreatedOn().getTimeInMillis();
                     /* the time stamp is checked first because it is fast;
                      * however documents are considered different only if the
                      * checksums are different */
                     if (draftFile.lastModified() == latestVersionCreatedOn) {
                         return Boolean.FALSE;
                     } else {
                         final ByteBuffer buffer = workspace.getDefaultBuffer();
                         synchronized (buffer) {
                             return !latestVersion.getChecksum().equals(
                                     checksum(draftFile, buffer));
                         }
                     }
                 }
             } else {
                 return Boolean.FALSE;
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#lock(com.thinkparity.codebase.model.document.Document)
      *
      */
     public DocumentFileLock lock(final Document document)
             throws CannotLockException {
         try {
             final DocumentFileLock lock = new DocumentFileLock();
             final File draftFile = getDraftFile(document);
             draftFile.setWritable(true, true);
             final FileChannel draftFileChannel = new RandomAccessFile(draftFile, "rws").getChannel();
             final FileLock draftFileLock = draftFileChannel.tryLock();
             if (null == draftFileLock) {
             	draftFileChannel.close();
                 throw new CannotLockException(document.getName());
             }
             lock.setFile(draftFile);
             lock.setFileChannel(draftFileChannel);
             lock.setFileLock(draftFileLock);
             return lock;
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#lockVersion(com.thinkparity.codebase.model.document.DocumentVersion)
      * 
      */
     public DocumentFileLock lockVersion(final DocumentVersion version)
             throws CannotLockException {
         try {
             return lockVersion(version, "rws");
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }        
         
     /**
 	 * Open a document.
 	 * 
 	 * @param documentId
 	 *            The document unique id.
 	 * @throws ParityException
 	 */
     public void open(final Long documentId, final Opener opener) {
 		logger.logApiId();
         logger.logVariable("documentId", documentId);
         logger.logVariable("opener", opener);
 		try {
 			final Document document = read(documentId);
 
 			// open the local file
 			final File draftFile = getDraftFile(document);
             if (!draftFile.exists()) {
                 final DocumentVersion latestVersion = readLatestVersion(documentId);
                 final DocumentFileLock lock = lock(document);
                 try {
                     writeVersion(documentId, latestVersion.getVersionId(), lock);
                     draftFile.setLastModified(latestVersion.getCreatedOn().getTimeInMillis());
                     draftFile.setWritable(true, true);
                 } finally {
                     release(lock);
                 }
             }
             opener.open(draftFile);
 		} catch (final Throwable t) {
             throw panic(t);
 		}
 	}
 
     /**
      * Open an input stream to read the document draft. Note: It is a good
      * idea to buffer the input stream.
      * 
      * @param documentId
      *            A document id.
      * @return A document draft content <code>InputStream</code>.
      */
     public InputStream openDraft(final Long documentId) {
         try {
             final Document document = read(documentId);
             final File localFile = getDraftFile(document);
             return openFile(localFile);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.DocumentModel#openVersion(java.lang.Long,
      *      java.lang.Long, com.thinkparity.ophelia.model.util.Opener)
      * 
      */
     public void openVersion(final Long documentId, final Long versionId,
             final Opener opener) {
 		try {
 			final Document document = read(documentId);
 			final DocumentVersion version = readVersion(documentId, versionId);
 			final File versionFile = getVersionFile(document, version);
             if (!versionFile.exists()) {
                 final DocumentFileLock lock = lockVersion(version);
                 try {
                     writeVersion(documentId, versionId, lock);
                     versionFile.setLastModified(
                             version.getCreatedOn().getTimeInMillis());
                 } finally {
                     release(lock);
                 }
             }
 			opener.open(versionFile);
 		} catch (final Throwable t) {
             throw panic(t);
 		}
 	}
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#openVersion(java.lang.Long,
      *      java.lang.Long, com.thinkparity.codebase.model.stream.StreamOpener)
      * 
      */
     public void openVersion(final Long documentId, final Long versionId,
             final StreamOpener opener) {
         try {
             documentIO.openStream(documentId, versionId, opener);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Print a document draft.
      * 
      * @param documentId
      *            A document id.
      * @param printer
      *            A document printer.
      */
     public void printDraft(final Long documentId, final Printer printer) {
         logger.logApiId();
         logger.logVariable("documentId", documentId);
         logger.logVariable("printer", printer);
         try {
             final Document document = read(documentId);
             final File draftFile = getDraftFile(document);
             final File copyTo = workspace.createTempFile(document.getName());
             try {
                 copyFile(draftFile, copyTo);
                 printer.print(copyTo);
             } finally {
                 Assert.assertTrue(copyTo.delete(),
                         "Could not delete temporary file {0}.", copyTo);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Print a document version.
      * 
      * @param documentId
      *            A document id.
      * @param versionId
      *            A document version id.
      * @param printer
      *            A document printer.
      */
     public void printVersion(final Long documentId, final Long versionId,
             final Printer printer) {
         logger.logApiId();
         logger.logVariable("documentId", documentId);
         logger.logVariable("versionId", versionId);
         logger.logVariable("printer", printer);
         try {
             final Document document = read(documentId);
             final DocumentVersion version = readVersion(documentId, versionId);
             final File versionFile = getVersionFile(document, version);
 
             final File copyTo = workspace.createTempFile(document.getName());
             try {
                 copyFile(versionFile, copyTo);
                 printer.print(copyTo);
             } finally {
                 Assert.assertTrue(copyTo.delete(),
                         "Could not delete temporary file {0}.", copyTo);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#read()
      *
      */
     public List<Document> read() {
         try {
             return documentIO.read();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Read a document.
      * 
      * @param documentId
      *            A document id.
      * @return A document.
      */
     public Document read(final Long documentId) {
         try {
             return documentIO.read(documentId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
 	 * Obtain a document with the specified unique id.
 	 * 
 	 * @param documentUniqueId
 	 *            The document unique id.
 	 * @return The document.
 	 */
     public Document read(final UUID uniqueId) {
 		try {
             return documentIO.read(uniqueId);
 		} catch (final Throwable t) {
             throw panic(t);
 		}
 	}
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#readDraft(com.thinkparity.ophelia.model.document.DocumentFileLock)
      * 
      */
     public DocumentDraft readDraft(final DocumentFileLock lock,
             final Long documentId) {
         try {
             if (doesExistDraft(lock)) {
                 final File draftFile = getDraftFile(lock);
                 final DocumentDraft draft = new DocumentDraft();
                 draft.setChecksum(checksum(lock));
                 draft.setChecksumAlgorithm(getChecksumAlgorithm());
                 draft.setDocumentId(documentId);
                 draft.setSize(draftFile.length());
                 return draft;
             } else {
                 return null;
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#readDraft(java.lang.Long)
      *
      */
     public DocumentDraft readDraft(final Long documentId) {
         try {
             if (doesExistDraft(documentId)) {
                 final File draftFile = getDraftFile(read(documentId));
                 final DocumentDraft draft = new DocumentDraft();
                 final ByteBuffer buffer = workspace.getDefaultBuffer();
                 synchronized (buffer) {
                     draft.setChecksum(checksum(draftFile, buffer));
                 }
                 draft.setChecksumAlgorithm(getChecksumAlgorithm());
                 draft.setDocumentId(documentId);
                 draft.setSize(draftFile.length());
                 return draft;
             } else {
                 return null;
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * Obtain the first available version.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @return A <code>DocumentVersion</code>.
      */
     public DocumentVersion readEarliestVersion(final Long documentId) {
         logger.logApiId();
         logger.logVariable("documentId", documentId);
         try {
             final Long versionId = getArtifactModel().readEarliestVersionId(documentId);
             if (null == versionId) {
                 return null;
             } else {
                 return readVersion(documentId, versionId);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
 	 * Obtain the latest document version.
 	 * 
 	 * @param documentId
 	 *            The document id.
 	 * @return The latest version.
 	 */
     public DocumentVersion readLatestVersion(final Long documentId) {
 		logger.logApiId();
 		logger.logVariable("documentId", documentId);
 		try {
             if (doesExistLatestVersion(documentId)) {
                 return documentIO.readLatestVersion(documentId);
             } else {
                 return null;
             }
 		} catch (final Throwable t) {
             throw panic(t);
 		}
 	}
 
     /**
      * Read a document version.
      * 
      * @param documentId
      *            A document id.
      * @param versionId
      *            A version id.
      * @return A document version.
      */
     public DocumentVersion readVersion(final Long documentId, final Long versionId) {
         logger.logApiId();
         logger.logVariable("documentId", documentId);
         logger.logVariable("versionId", versionId);
         try {
             return documentIO.getVersion(documentId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#readVersions(java.lang.Long)
      *
      */
     public List<DocumentVersion> readVersions(final Long documentId) {
         try {
             return readVersions(documentId, defaultVersionComparator);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#readVersions(java.lang.Long, java.util.Comparator)
      *
      */
     public List<DocumentVersion> readVersions(Long documentId,
             Comparator<? super ArtifactVersion> comparator) {
         try {
             final List<DocumentVersion> versions =
                 documentIO.listVersions(documentId);
             Collections.sort(versions, comparator);
             return versions;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * Read the version size.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @return The version size <code>Integer</code>.
      */
     public Long readVersionSize(final Long documentId, final Long versionId) {
         logger.logApiId();
         logger.logVariable("documentId", documentId);
         logger.logVariable("versionId", versionId);
         try {
             return documentIO.readVersionSize(documentId, versionId);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#release(com.thinkparity.codebase.model.document.Document)
      *
      */
     public void release(final DocumentFileLock lock) {
         try {
             lock.release();
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#remove(com.thinkparity.codebase.model.document.DocumentLock,
      *      java.lang.Long)
      * 
      */
     public void remove(final DocumentFileLock lock, final Long documentId) {
         try {
             final Document document = read(documentId);
             Assert.assertTrue(lock.getFile().setReadOnly(),
                     "Cannot remove document {0}.", document.getName());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * @see com.thinkparity.ophelia.model.Model#removeListener(com.thinkparity.ophelia.model.util.EventListener)
      */
     @Override
     public void removeListener(final DocumentListener listener) {
         super.removeListener(listener);
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.DocumentModel#rename(java.lang.Long,
      *      java.lang.String)
      * 
      */
     public void rename(final Long documentId, final String renameTo)
             throws CannotLockException {
         try {
             final Document document = read(documentId);
             final DocumentFileLock lock = lock(document);
             try {
                 // rename the document
                 document.setName(renameTo);
                 documentIO.update(document);
             } finally {
                 release(lock);
             }

            // rename the local file
            renameFile(lock, renameTo);
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#revertDraft(com.thinkparity.codebase.model.document.DocumentLock,
      *      java.lang.Long)
      * 
      */
     public void revertDraft(final DocumentFileLock lock, final Long documentId) {
         try {
             revertDraft(lock, documentId, readLatestVersion(documentId).getVersionId());
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
 	/**
      * @see com.thinkparity.ophelia.model.document.InternalDocumentModel#updateDraft(com.thinkparity.ophelia.model.document.DocumentFileLock,
      *      java.lang.Long, java.io.InputStream)
      * 
      */
     public void updateDraft(final DocumentFileLock lock, final Long documentId,
             final InputStream content) {
         try {
             try {
                 writeFile(lock, content);
                 lock.getFile().setLastModified(currentDateTime().getTimeInMillis());
             } finally {
                 release(lock);
             }
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.document.DocumentModel#updateDraft(java.lang.Long,
      *      java.io.InputStream)
      * 
      */
     public void updateDraft(final Long documentId, final InputStream content)
             throws CannotLockException {
         try {
             final DocumentFileLock lock = lock(documentId);
             updateDraft(lock, documentId, content);
         } catch (final CannotLockException clx) {
             throw clx;
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * Save a version to an output stream.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @param uploader
      *            An <code>StreamUploader</code> to upload to.
      */
     public void uploadVersion(final Long documentId, final Long versionId,
             final StreamUploader uploader) {
         try {
             documentIO.uploadVersion(documentId, versionId, uploader);
         } catch (final Throwable t) {
             throw panic(t);
         }
     }
 
     /**
      * @see com.thinkparity.ophelia.model.Model#initializeModel(com.thinkparity.codebase.model.session.Environment, com.thinkparity.ophelia.model.workspace.Workspace)
      *
      */
     @Override
     protected void initializeModel(final Environment environment,
             final Workspace workspace) {
         this.documentIO = IOFactory.getDefault(workspace).createDocumentHandler();
         this.localFilesDirectory = new File(workspace.getDataDirectory(), DirectoryNames.Workspace.Data.LOCAL);
         if(!localFilesDirectory.exists())
             Assert.assertTrue(localFilesDirectory.mkdir(),
                     "Cannot create directory {0}.", localFilesDirectory);
     }
 
     /**
      * Assert that the document's draft exists.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @param assertMessage
      *            An assertion message <code>String</code>.
      * @param assertArguments
      *            An assertion message's arguments <code>Object...</code>.
      */
     private void assertDoesExistDraft(final Long documentId,
             final String assertMessage, final Object... assertArguments) {
         Assert.assertTrue(doesExistDraft(documentId), assertMessage,
                 assertArguments);
     }
 
     /**
      * Assert that the document's draft is modified.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @param assertMessage
      *            An assertion message <code>String</code>.
      * @param assertArguments
      *            An assertion message's arguments <code>Object...</code>.
      */
     private void assertDraftIsModified(final DocumentFileLock lock,
             final Long documentId, final String assertMessage,
             final Object... assertArguments) {
         Assert.assertTrue(isDraftModified(lock, documentId), assertMessage,
                 assertArguments);
     }
 
     /**
      * Assert that the document's draft is modified.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @param assertMessage
      *            An assertion message <code>String</code>.
      * @param assertArguments
      *            An assertion message's arguments <code>Object...</code>.
      */
     private void assertDraftIsModified(final Long documentId,
             final String assertMessage, final Object... assertArguments) {
         Assert.assertTrue(isDraftModified(documentId), assertMessage,
                 assertArguments);
     }
 
     /**
      * Calculate a checksum for the lock.
      * 
      * @param lock
      *            A <code>DocumentFileLock</code>.
      * @return A checksum <code>String</code>.
      */
     private String checksum(final DocumentFileLock lock) throws IOException {
         final FileChannel fileChannel = lock.getFileChannel();
         fileChannel.position(0);
         return checksum(fileChannel, getDefaultBufferSize());
     }
 
     /**
      * Copy a file.
      * 
      * @param file
      *            A <code>File</code>.
      * @param copyTo
      *            A <code>File</code> to copy to.
      * @throws IOException
      */
     private void copyFile(final File file, final File copyTo)
             throws IOException {
         final FileOutputStream outputStream = new FileOutputStream(copyTo);
         try {
             final InputStream inputStream = new FileInputStream(file);
             try {
                 final ByteBuffer buffer = workspace.getDefaultBuffer();
                 synchronized (buffer) {
                     StreamUtil.copy(inputStream, outputStream, buffer);
                 }
             } finally {
                 inputStream.close();
             }
         } finally {
             outputStream.close();
         }
     }
 
     /**
      * Create a document. Simply create the document; the artifact remote info
      * and stream the content to the draft file.
      * 
      * @param uniqueId
      *            A unique id <code>UUID</code>.
      * @param name
      *            A name <code>String</code>.
      * @param content
      *            The draft content <code>InputStream</code>.
      * @param createdBy
      *            A created by user id <code>JabberId</code>.
      * @param createdOn
      *            A created on <code>Calendar</code>.
      * @return A <code>Document</code>.
      */
     private Document create(final UUID uniqueId, final String name,
             final InputStream content, final JabberId createdBy,
             final Calendar createdOn) throws CannotLockException, IOException {
         // create document
         final Document document = create(uniqueId, name, createdBy, createdOn);
         final DocumentFileLock lock = lock(document);
         try {
             writeFile(lock, content);
             lock.getFile().setLastModified(createdOn.getTimeInMillis());
         } finally {
             release(lock);
         }
         return read(document.getId());
     }
 
     /**
      * Create a document. Simply create the document and the artifact remote
      * info object in the database.
      * 
      * @param uniqueId
      *            A unique id <code>UUID</code>.
      * @param name
      *            A name <code>String</code>.
      * @param createdBy
      *            A created by user id <code>JabberId</code>.
      * @param createdOn
      *            A created on <code>Calendar</code>.
      * @return A <code>Document</code>.
      */
     private Document create(final UUID uniqueId, final String name,
             final JabberId createdBy, final Calendar createdOn) {
         // create document
         final Document document = new Document();
         document.setCreatedBy(createdBy);
         document.setCreatedOn(createdOn);
         document.setFlags(Collections.<ArtifactFlag>emptyList());
         document.setUniqueId(uniqueId);
         document.setName(name);
         document.setUpdatedBy(document.getCreatedBy());
         document.setUpdatedOn(document.getCreatedOn());
         document.setState(ArtifactState.ACTIVE);
         documentIO.create(document);
         return read(document.getId());
     }
 
     /**
      * Create a document version.
      * 
      * @param documentId
      *            A document id.
      * @param versionId
      *            A document version id.
      * @param content
      *            The document's content.
      * @param createdBy
      *            Who creatd the version.
      * @param createdOn
      *            When the version was created.
      * @return A new document version.
      */
 	private DocumentVersion createVersion(final Long documentId,
             final Long versionId, final InputStream stream,
             final JabberId createdBy, final Calendar createdOn)
             throws CannotLockException, IOException {
 	    final File temp = workspace.createTempFile();
         try {
             // create a temp file containing the stream
             final OutputStream os = new FileOutputStream(temp);
             try {
                 final ByteBuffer buffer = workspace.getDefaultBuffer();
                 synchronized (buffer) {
                     StreamUtil.copy(stream, os, buffer);
                 }
             } finally {
                 os.close();
             }
 
     		// create version
             final Document document = read(documentId);
     		final DocumentVersion version = new DocumentVersion();
     		version.setArtifactId(documentId);
     		version.setArtifactType(document.getType());
     		version.setArtifactUniqueId(document.getUniqueId());
             final InputStream checksumStream = new FileInputStream(temp);
 		    try {
                 final ByteBuffer buffer = workspace.getDefaultBuffer();
                 synchronized (buffer) {
                     version.setChecksum(MD5Util.md5Hex(checksumStream, buffer));
                 }
             } finally {
                 checksumStream.close();
             }
             version.setChecksumAlgorithm(ChecksumAlgorithm.MD5.name());
     		version.setCreatedBy(createdBy);
     		version.setCreatedOn(createdOn);
     		version.setName(document.getName());
     		version.setUpdatedBy(version.getCreatedBy());
     		version.setUpdatedOn(version.getCreatedOn());
             version.setSize(temp.length());
             version.setVersionId(versionId);
             // create version content
             InputStream versionStream = new FileInputStream(temp);
             try {
                 documentIO.createVersion(version, versionStream,
                         getDefaultBufferSize());
             } finally {
                 versionStream.close();
             }
     		// write local version file
             final DocumentFileLock versionLock = lockVersion(version, "rws");
             try {
                 versionStream = new FileInputStream(temp);
                 try {
                     writeFile(versionLock, versionStream);
                     versionLock.getFile().setLastModified(version.getCreatedOn().getTimeInMillis());
                     versionLock.getFile().setReadOnly();
                 } finally {
                     versionStream.close();
                 }
             } finally {
                 release(versionLock);
             }
     		// update document
     		document.setUpdatedBy(version.getUpdatedBy());
     		document.setUpdatedOn(version.getUpdatedOn());
     		documentIO.update(document);
     		return readVersion(documentId, versionId);
         } finally {
             Assert.assertTrue(temp.delete(), "Cannot delete temp file {0}.", temp);
         }
 	}
 
     /**
      * Delete a file.
      * 
      * @param lock
      *            A <code>DocumentFileLock</code>.
      */
     private void deleteFile(final DocumentFileLock lock) {
     	release(lock);
         Assert.assertTrue(lock.getFile().delete(), "Could not delete file {0}.", lock.getFile());
         final File parent = lock.getFile().getParentFile();
         if (0 == parent.list().length)
             Assert.assertTrue(parent.delete(), "Could not delete directory {0}.", parent);
     }
 
     /**
      * Delete the local document data.
      * 
      * @param lock
      * @param versionLocks
      * @param documentId
      */
     private void deleteLocal(final DocumentFileLock lock,
             final Map<DocumentVersion, DocumentFileLock> versionLocks,
             final Long documentId) {
         // delete versions
         final List<DocumentVersion> versions = readVersions(documentId);
 		for (final DocumentVersion version : versions) {
             deleteFile(versionLocks.get(version));
 			documentIO.deleteVersion(version.getArtifactId(), version.getVersionId());
         }
         // delete  index
         getIndexModel().deleteDocument(documentId);
         // delete document
         deleteFile(lock);
 		documentIO.delete(documentId);
     }
 
     /**
      * Determine whether or not a draft exists.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @return True if the draft exists.
      */
     private Boolean doesExistDraft(final DocumentFileLock lock) {
         final File draftFile = getDraftFile(lock);
         return draftFile.exists();
     }
 
     /**
      * Obtain the document root directory.
      * 
      * @param document
      *            A <code>Document</code>.
      * @return A directory <code>File</code>.
      */
     private File getDocumentRoot(final Document document) {
         final File parent = new File(localFilesDirectory,
                 nameGenerator.localDirectoryName(document));
         if(!parent.exists()) {
             Assert.assertTrue(parent.mkdir(),
                     "Cannot create directory {0}.", parent);
         }
         return parent;
     }
 
     /**
      * Obtain the draft file.
      * 
      * @param document
      *            A <code>Document</code>.
      * @return A draft <code>File</code>.
      */
     private File getDraftFile(final Document document) {
         final String child = nameGenerator.fileName(document);
         return new File(getDocumentRoot(document), child);
     }
 
     /**
      * Obtain the draft file.
      * 
      * @param document
      *            A <code>Document</code>.
      * @return A draft <code>File</code>.
      */
     private File getDraftFile(final DocumentFileLock lock) {
         return lock.getFile();
     }
 
     /**
      * Obtain the version file.
      * 
      * @param document
      *            A <code>Document</code>.
      * @param version
      *            A <code>DocumentVersion</code>.
      * @return A draft <code>File</code>.
      */
     private File getVersionFile(final Document document,
             final DocumentVersion version) {
         final String child = nameGenerator.localFileName(version);
         return new File(getDocumentRoot(document), child);
     }
 
     /**
      * Obtain a document file lock.
      * 
      * @param documentId
      *            A document id <code>Long</code>.
      * @return A <code>DocumentFileLock</code>.
      * @throws CannotLockException
      */
     private DocumentFileLock lock(final Long documentId) throws CannotLockException {
         return lock(read(documentId));
     }
 
     /**
      * Obtain an exclusive lock on a document.
      * 
      * @param version
      *            A <code>DocumentVersion</code>.
      * @param mode
      *            An access mode as defined by <code>RandomAccessFile</code>.
      * @return A <code>DocumentVersionLock</code>.
      * @throws CannotLockException
      *             if an exclusive lock cannot be obtained
      */
     private DocumentFileLock lockVersion(final DocumentVersion version,
             final String mode) throws CannotLockException, IOException {
         final DocumentFileLock lock = new DocumentFileLock();
         final Document document = read(version.getArtifactId());
         final File versionFile = getVersionFile(document, version);
         versionFile.setWritable(true, true);
         final FileChannel versionFileChannel = new RandomAccessFile(versionFile, mode).getChannel();
         final FileLock versionFileLock = versionFileChannel.tryLock();
         if (null == versionFileLock)
             throw new CannotLockException(new StringBuffer(document.getName())
                 .append(":").append(version.getVersionId()).toString());
         lock.setFile(versionFile);
         lock.setFileChannel(versionFileChannel);
         lock.setFileLock(versionFileLock);
         return lock;
     }
 
     /**
 	 * Fire document created.
 	 * 
 	 * @param document
      *      A document.
      * @param eventGen
      *      The event generator.
 	 */
 	private void notifyDocumentCreated(final Document document,
             final DocumentModelEventGenerator eventGen) {
         notifyListeners(new EventNotifier<DocumentListener>() {
             public void notifyListener(final DocumentListener listener) {
 				listener.documentCreated(eventGen.generate(document));
 			}
 		});
 	}
 
     /**
 	 * Fire document deleted.
 	 * 
 	 * @param document
      *      The document.
      * @param eventGen
      *      The event generator.
 	 */
 	private void notifyDocumentDeleted(final Document document,
             final DocumentModelEventGenerator eventGen) {
         notifyListeners(new EventNotifier<DocumentListener>() {
             public void notifyListener(final DocumentListener listener) {
 				listener.documentDeleted(eventGen.generate(document));
 			}
 		});
 	}
 
     /**
      * Open a file in an input stream.
      * 
      * @param file
      *            A <code>File</code>.
      * @return An <code>InputStream</code>.
      * @throws IOException
      */
     private InputStream openFile(final File file) throws IOException {
         return new FileInputStream(file);
     }
 
     /**
      * Rename a file.
      * 
      * @param lock
      *            A <code>DocumentFileLock</code> lock.
      * @param renameTo
      *            The new name <code>String</code>.
      */
     private void renameFile(final DocumentFileLock lock, final String renameTo) {
        final File renameToFile = new File(
                lock.getFile().getParentFile(), renameTo);
        Assert.assertTrue(lock.getFile().renameTo(renameToFile),
                "Could not rename file from {0} to {1}.", lock.getFile(),
                renameToFile);
     }
 
     /**
      * Revert a draft file to its latest version content.
      * 
      * @param lock
      *            A <code>DocumentFileLock</code>.
      * @param documentId
      *            A document id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @throws IOException
      */
     private void revertDraft(final DocumentFileLock lock, final Long documentId,
             final Long versionId) throws IOException {
         writeVersion(documentId, versionId, lock);
         final DocumentVersion version = readVersion(documentId, versionId);
         lock.getFile().setLastModified(
                 version.getCreatedOn().getTimeInMillis());
     }
 
     /**
      * Write a file.
      * 
      * @param lock
      *            A <code>DocumentFileLock</code> to write to.
      * @param stream
      *            The content <code>InputStream</code> to write.
      * @throws IOException
      */
     private void writeFile(final DocumentFileLock lock,
             final InputStream stream) throws IOException {
         final ByteBuffer buffer = workspace.getDefaultBuffer();
         synchronized (buffer) {
             final FileChannel fileChannel = lock.getFileChannel();
             fileChannel.position(0);
             StreamUtil.copy(stream, fileChannel, buffer);
             fileChannel.force(true);
         }
     }
 
     /**
      * Write the contents of the document version to the document file lock's
      * underlying file channel.
      * 
      * @param documentId
      *            A document id <code>Long<code>.
      * @param versionId A version id <code>Long<code>.
      * @param lock  A <code>DocumentFileLock<code>.
      */
     private void writeVersion(final Long documentId, final Long versionId,
             final DocumentFileLock lock) {
         final ByteBuffer buffer = workspace.getDefaultBuffer();
         synchronized (buffer) {
             openVersion(documentId, versionId, new StreamWriterHelper(this,
                     lock.getFileChannel(), buffer));
         }
     }
 }
