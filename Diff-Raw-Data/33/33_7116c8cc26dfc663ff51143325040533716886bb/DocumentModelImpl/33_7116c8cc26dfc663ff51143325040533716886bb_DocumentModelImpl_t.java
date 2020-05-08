 /*
  * Mar 6, 2005
  */
 package com.thinkparity.model.parity.model.document;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.UUID;
 import java.util.Vector;
 
 import com.thinkparity.codebase.DateUtil;
 import com.thinkparity.codebase.FileUtil;
 import com.thinkparity.codebase.OSUtil;
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.thinkparity.model.parity.IParityModelConstants;
 import com.thinkparity.model.parity.ParityErrorTranslator;
 import com.thinkparity.model.parity.ParityException;
 import com.thinkparity.model.parity.api.events.CreationEvent;
 import com.thinkparity.model.parity.api.events.CreationListener;
 import com.thinkparity.model.parity.api.events.DeleteEvent;
 import com.thinkparity.model.parity.api.events.UpdateEvent;
 import com.thinkparity.model.parity.api.events.UpdateListener;
 import com.thinkparity.model.parity.api.events.VersionCreationEvent;
 import com.thinkparity.model.parity.model.AbstractModelImpl;
 import com.thinkparity.model.parity.model.io.xml.document.DocumentXmlIO;
 import com.thinkparity.model.parity.model.io.xml.project.ProjectXmlIO;
 import com.thinkparity.model.parity.model.note.Note;
 import com.thinkparity.model.parity.model.project.Project;
 import com.thinkparity.model.parity.model.project.ProjectModel;
 import com.thinkparity.model.parity.model.workspace.Workspace;
 import com.thinkparity.model.parity.util.MD5Util;
 import com.thinkparity.model.parity.util.UUIDGenerator;
 import com.thinkparity.model.xmpp.document.XMPPDocument;
 
 /**
  * Implementation of the document model interface.
  * 
  * @author raykroeker@gmail.com
  * @version 1.5.2.3
  */
 class DocumentModelImpl extends AbstractModelImpl {
 
 	/**
 	 * List of listeners to notify when a document is created or received.
 	 * 
 	 * @see DocumentModelImpl#creationListenersLock
 	 */
 	private static final Collection<CreationListener> creationListeners;
 
 	/**
 	 * Synchronization lock for the creation listeners.
 	 * 
 	 * @see DocumentModelImpl#creationListeners
 	 */
 	private static final Object creationListenersLock;
 
 	/**
 	 * List of listeners to notify when a document has been updated.
 	 * 
 	 * @see DocumentModelImpl#updateListenersLock
 	 */
 	private static final Collection<UpdateListener> updateListeners;
 
 	/**
 	 * Synchronization lock for the update listeners.
 	 * 
 	 * @see DocumentModelImpl#updateListeners
 	 */
 	private static final Object updateListenersLock;
 
 	static {
 		// initialize the creation event listeners
 		creationListeners = new Vector<CreationListener>(7);
 		creationListenersLock = new Object();
 		// initialize the update event listeners
 		updateListeners = new Vector<UpdateListener>(7);
 		updateListenersLock = new Object();
 	}
 
 	/**
 	 * Document xml input\output.
 	 */
 	private final DocumentXmlIO documentXmlIO;
 
 	/**
 	 * Handle to a project model api.
 	 */
 	private final ProjectModel projectModel;
 
 	/**
 	 * Project xml input\output.
 	 */
 	private final ProjectXmlIO projectXmlIO;
 
 	/**
 	 * Create a DocumentModelImpl
 	 * 
 	 * @param workspace
 	 *            The workspace to work within.
 	 */
 	DocumentModelImpl(final Workspace workspace) {
 		super(workspace);
 		this.documentXmlIO = new DocumentXmlIO(workspace);
 		this.projectXmlIO = new ProjectXmlIO(workspace);
 		this.projectModel = ProjectModel.getModel();
 	}
 
 	/**
	 * @see DocumentModel#addListener(CreationListener)
 	 * @param listener
	 *            The listener to add.
 	 */
 	void addListener(final CreationListener listener) {
 		logger.info("addCreationListener(CreationListener)");
 		logger.debug(listener);
 		Assert.assertNotNull("addCreationListener(CreationListener)", listener);
 		synchronized (DocumentModelImpl.creationListenersLock) {
 			Assert.assertNotTrue(
 					"addCreationListener(CreationListener)",
 					DocumentModelImpl.creationListeners.contains(listener));
 			DocumentModelImpl.creationListeners.add(listener);
 		}
 	}
 
 	/**
 	 * Add a document update event listener.
 	 * 
 	 * @param listener
 	 *            The listener to add.
 	 */
 	void addListener(final UpdateListener listener) {
 		logger.info("addUpdateListener(UpdateListener)");
 		logger.debug(listener);
 		Assert.assertNotNull("addUpdateListener(UpdateListener)", listener);
 		synchronized (DocumentModelImpl.updateListenersLock) {
 			Assert.assertNotTrue("Update listener already registered.",
 					DocumentModelImpl.updateListeners.contains(listener));
 			DocumentModelImpl.updateListeners.add(listener);
 		}
 	}
 
 	/**
 	 * Add a note to a document.
 	 * 
 	 * @param document
 	 *            The document to add the note to.
 	 * @param note
 	 *            The note to add.
 	 * @throws ParityException
 	 */
 	void addNote(final Document document, final Note note)
 			throws ParityException {
 		logger.debug(document);
 		logger.debug(note);
 		try {
 			document.add(note);
 			documentXmlIO.update(document);
 			notifyUpdate_objectUpdated(document);
 		}
 		catch(IOException iox) {
 			logger.error("addNote(Document,Note)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("addNote(Document,Note)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Import a document into a project. This will take a name, description and
 	 * location of a document and copy the document into an internal store for
 	 * the project, then returns the newly created document.
 	 * 
 	 * @param project
 	 *            The parent project.
 	 * @param name
 	 *            The name of the document.
 	 * @param description
 	 *            The description of the document.
 	 * @param file
 	 *            The file for the document.
 	 * @return The created document.
 	 * @throws ParityException
 	 */
 	Document create(final Project project, final String name,
 			final String description, final File file) throws ParityException {
 		logger.info("create(Project,String,String,File)");
 		logger.debug(project);
 		logger.debug(name);
 		logger.debug(description);
 		logger.debug(file);
 		try {
 			final UUID id = UUIDGenerator.nextUUID();
 			logger.debug(id);
 			final byte[] content = FileUtil.readFile(file);
 			logger.debug(content);
 			final Document document = new Document(project, name,
 					DateUtil.getInstance(), preferences.getUsername(),
 					description, id, content);
 			// create the document
 			documentXmlIO.create(document);
 			// update the project
 			project.addDocument(document);
 			projectXmlIO.update(project);
 			// create a new version
 			createVersion(document);
 			// fire a creation event
 			notifyCreation_objectCreated(document);
 			return document;
 		}
 		catch(IOException iox) {
 			logger.error("createDocument(Document)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("createDocument(Document)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Delete a document.
 	 * 
 	 * @param document
 	 *            The document to delete.
 	 * @throws ParityException
 	 */
 	void delete(final Document document) throws ParityException {
 		logger.info("delete(Document)");
 		logger.debug(document);
 		try {
 			// remove the document from the project and update
 			final Project project = document.getParent();
 			Assert.assertTrue(
 					"Document cannot be removed from project.",
 					project.removeDocument(document));
 			projectModel.update(project);
 
 			// delete the versions
 			final Collection<DocumentVersion> versions = document.getVersions();
 			for(DocumentVersion version : versions) { delete(version); }
 
 			// delete the xml file
 			documentXmlIO.delete(document);
 			notifyUpdate_objectDeleted(document);
 		}
 		catch(RuntimeException rx) {
 			logger.error("delete(Document)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Delete a document version.  
 	 * @param version The version to delete.
 	 * @throws ParityException
 	 */
 	void delete(final DocumentVersion version) throws ParityException {
 		logger.info("delete(DocumentVersion)");
 		logger.debug(version);
 		try {
 			// update the document
 			final Document document = version.getDocument();
 			document.remove(version);
 			documentXmlIO.update(document);
 			// delete the xml file
 			documentXmlIO.delete(version);
 		}
 		catch(IOException iox) {
 			logger.error("delete(DocumentVersion)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("delete(DocumentVersion)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Take the given document, and export it to the specified file. This will
 	 * obtain the document's content, and save it to the file. Note that if file
 	 * exists, it will be overwritten.
 	 * 
 	 * @param document
 	 *            The document to export.
 	 * @param file
 	 *            The file to export the document to.
 	 * @throws ParityException
 	 */
 	void export(final Document document, final File file)
 			throws ParityException {
 		logger.info("export(Document)");
 		logger.debug(file);
 		logger.debug(document);
 		try {
 			Assert.assertNotTrue("File cannot already exist.", file.exists());
 			Assert.assertTrue("Could not create new file.", file.createNewFile());
 			writeDocumentContent(file, document);
 		}
 		catch(IOException iox) {
 			logger.error("export(Document,File)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("export(Document,File)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	String getRelativePath(final Document document) {
 		logger.info("getRelativePath(Document)");
 		logger.debug(document);
 		return super.getRelativePath(document);
 	}
 
 	Boolean isClosable(final Document document) throws ParityException {
 		logger.info("isClosable(Document)");
 		logger.debug(document);
 		return Boolean.FALSE;
 	}
 
 	Boolean isDeletable(final Document document) throws ParityException {
 		logger.info("isDeletable(Document)");
 		logger.debug(document);
 		return Boolean.FALSE;
 	}
 
 	/**
 	 * Obtain a list of documents for a project.
 	 * 
 	 * @param project
 	 *            The project which contains the documents.
 	 * @return A list of documents for a project.
 	 * @throws ParityException
 	 */
 	Collection<Document> list(final Project project)
 			throws ParityException {
 		logger.info("list(Project)");
 		logger.debug(project);
 		try { return documentXmlIO.list(project); }
 		catch(IOException iox) {
 			logger.error("list(Project)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("list(Project)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Obtain a list of document versions for a document.
 	 * 
 	 * @param document
 	 *            The document which contains the versions.
 	 * @return The list of document versions.
 	 * @throws ParityException
 	 */
 	Collection<DocumentVersion> listVersions(final Document document)
 			throws ParityException {
 		logger.info("listVersions(Document)");
 		logger.debug(document);
 		try { return documentXmlIO.listVersions(document); }
 		catch(IOException iox) {
 			logger.error("listVersions(Document)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("listVersions(Document)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Open the document. First obtain the file from the cache, then open it
 	 * based upon underlying operating system constraints.
 	 * 
 	 * @param document
 	 *            The document to open.
 	 * @throws ParityException
 	 */
 	void open(final Document document) throws ParityException {
 		logger.info("open(Document)");
 		logger.debug(document);
 		try { open(getFileFromDiskCache(document)); }
 		catch(IOException iox) {
 			logger.error("open(Document)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("open(Document)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Use the document model to receive a document from another parity user.
 	 * 
 	 * @param xmppDocument
 	 *            The xmpp document received from another parity user.
 	 * @throws ParityException
 	 */
 	void receive(final XMPPDocument xmppDocument)
 			throws ParityException {
 		logger.info("receiveDocument(XMPPDocument)");
 		logger.debug(xmppDocument);
 		try {
 			/*
 			 * Obtain the inbox parity project within the workspace, and place
 			 * the received document within it, the notify all listeners about
 			 * the new document.
 			 */
 			final Project inbox = projectModel.getInbox();
 	
 			final Document document = new Document(inbox,
 					xmppDocument.getName(), xmppDocument.getCreatedOn(),
 					xmppDocument.getCreatedBy(), xmppDocument.getDescription(),
 					xmppDocument.getId(), xmppDocument.getContent());
 			documentXmlIO.create(document);
 			inbox.addDocument(document);
 			projectXmlIO.update(inbox);
 			// create a new version
 			createVersion(document);
 			// fire a receive event
 			notifyCreation_objectReceived(document);
 		}
 		catch(IOException iox) {
 			logger.error("receiveDocument(XMPPDocument)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("receiveDocument(XMPPDocument)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Remove a document creation event listener.
 	 * 
 	 * @param listener
 	 *            The listener to remove.
 	 */
 	void removeListener(final CreationListener listener) {
 		logger.info("removeCreationListener(CreationListener)");
 		logger.debug(listener);
 		Assert.assertNotNull(
 				"removeCreationListener(CreationListener)", listener);
 		synchronized(DocumentModelImpl.creationListenersLock) {
 			Assert.assertTrue(
 					"removeCreationListener(CreationListener)",
 					DocumentModelImpl.creationListeners.contains(listener));
 			DocumentModelImpl.creationListeners.remove(listener);
 		}
 	}
 
 	/**
 	 * Remove a document update event listener.
 	 * 
 	 * @param listener
 	 *            The listener to remove.
 	 */
 	void removeListener(final UpdateListener listener) {
 		logger.info("removeUpdateListener(UpdateListener)");
 		logger.debug(listener);
 		Assert.assertNotNull("removeUpdateListener(UpdateListener)", listener);
 		synchronized (DocumentModelImpl.updateListenersLock) {
 			Assert.assertTrue(
 					"removeUpdateListener(UpdateListener)",
 					DocumentModelImpl.updateListeners.contains(listener));
 			DocumentModelImpl.updateListeners.remove(listener);
 		}
 	}
 
 	/**
 	 * Update a document.
 	 * 
 	 * @param document
 	 *            The document to update.
 	 * @throws ParityException
 	 */
 	void update(final Document document) throws ParityException {
 		logger.info("update(Document)");
 		logger.debug(document);
 		try {
 			documentXmlIO.update(document);
 			notifyUpdate_objectUpdated(document);
 		}
 		catch(IOException iox) {
 			logger.error("update(Document)", iox);
 			throw ParityErrorTranslator.translate(iox);
 		}
 		catch(RuntimeException rx) {
 			logger.error("update(Document)", rx);
 			throw ParityErrorTranslator.translate(rx);
 		}
 	}
 
 	/**
 	 * Create a new document version based upon an existing document. This will
 	 * check the cache for updates to the document, write the updates to the
 	 * document, then create a new version based upon that document.
 	 * 
 	 * @param document
 	 *            The document to create the version for.
 	 * @return The newly created version.
 	 */
 	private DocumentVersion createVersion(final Document document)
 			throws IOException, ParityException {
 		final File cacheFile = getCacheFile(document);
 		if(cacheFile.exists()) {
 			final byte[] cacheFileBytes = getCacheFileBytes(document);
 			final String cacheFileChecksum = MD5Util.md5Hex(cacheFileBytes);
 			if(!cacheFileChecksum.equals(document.getContentChecksum())) {
 				document.setContent(cacheFileBytes);
 				update(document);
 			}
 		}
 		final DocumentVersion version = DocumentVersionBuilder.create(document);
 		document.add(version);
 		documentXmlIO.update(document);
 		documentXmlIO.create(version);
 		notifyCreation_objectVersionCreated(version);
 		return version;
 	}
 
 	/**
 	 * Obtain the directory used to cache content for the document. If the
 	 * directory does not yet exist it is created.
 	 * 
 	 * @param document
 	 *            The document to obtain the cache directory for.
 	 * @return The cache directory.
 	 */
 	private File getCacheDirectory(final Document document) {
 		logger.debug(document);
 		final File dataDirectory = new File(workspace.getDataURL().getFile());
 		final File cacheDirectory = new File(
 				dataDirectory,
 				IParityModelConstants.DIRECTORY_NAME_CACHE_DATA);
 		if(!cacheDirectory.exists())
 			Assert.assertTrue("", cacheDirectory.mkdir());
 		logger.debug(cacheDirectory);
 		if(!cacheDirectory.exists())
 			Assert.assertTrue("getCacheDirectory(Document)", cacheDirectory.mkdir());
 		return cacheDirectory;
 	}
 
 	/**
 	 * Obtain the file used to store the cached content of the document.
 	 * 
 	 * @param document
 	 *            <code>Document</code>
 	 * @return <code>java.io.File</code>
 	 */
 	private File getCacheFile(final Document document) {
 		final File cacheDirectory = getCacheDirectory(document);
 		return new File(cacheDirectory, document.getCustomName());
 	}
 
 	private byte[] getCacheFileBytes(final Document document)
 			throws IOException {
 		return FileUtil.readFile(getCacheFile(document));
 	}
 
 	/**
 	 * Obtain the document's representative file from the underlying disk cache.
 	 * 
 	 * @param document
 	 *            <code>Document</code.
 	 * @return <code>java.io.File</code>
 	 */
 	private File getFileFromDiskCache(final Document document)
 			throws IOException {
 		final File documentCacheFile = getCacheFile(document);
 		if (!documentCacheFile.exists()) {
 			// write the cache file
 			FileUtil.writeFile(documentCacheFile, document.getContent());
 		}
 		return documentCacheFile;
 	}
 
 	/**
 	 * Fire the objectCreated event for all of the creation listeners.
 	 * 
 	 * @param document
 	 *            The document to use as the event source.
 	 * @see CreationListener#objectCreated(CreationEvent)
 	 */
 	private void notifyCreation_objectCreated(final Document document) {
 		synchronized(DocumentModelImpl.creationListenersLock) {
 			for(CreationListener listener : DocumentModelImpl.creationListeners) {
 				listener.objectCreated(new CreationEvent(document));
 			}
 		}
 	}
 
 	/**
 	 * Fire the objectReceived event for all creation listeners.
 	 * 
 	 * @param document
 	 *            The document to use as the event source.
 	 * @see CreationListener#objectReceived(CreationEvent)
 	 */
 	private void notifyCreation_objectReceived(final Document document) {
 		synchronized (DocumentModelImpl.creationListenersLock) {
 			for (CreationListener listener : DocumentModelImpl.creationListeners) {
 				listener.objectReceived(new CreationEvent(document));
 			}
 		}
 	}
 
 	/**
 	 * Fire the objectVersionCreated event for all of the creation listeners.
 	 * 
 	 * @param documentVersion
 	 *            The document version to use as the event source.
 	 * @see CreationListener#objectVersionCreated(VersionCreationEvent)
 	 */
 	private void notifyCreation_objectVersionCreated(
 			final DocumentVersion documentVersion) {
 		synchronized (DocumentModelImpl.creationListenersLock) {
 			for (CreationListener listener : DocumentModelImpl.creationListeners) {
 				listener.objectVersionCreated(new VersionCreationEvent(
 						documentVersion));
 			}
 		}
 	}
 
 	/**
 	 * Fire the object deleted event for all of the update listeners.
 	 * 
 	 * @param document
 	 *            The document that was deleted.
 	 */
 	private void notifyUpdate_objectDeleted(final Document document) {
 		synchronized(DocumentModelImpl.updateListeners) {
 			for(UpdateListener listener : DocumentModelImpl.updateListeners) {
 				listener.objectDeleted(new DeleteEvent(document));
 			}
 		}
 	}
 
 	/**
 	 * Fire the objectUpdated event for all of the udpate listeners.
 	 * 
 	 * @param document
 	 *            The document to use as the event source.
 	 * @see UpdateListener#objectUpdated(UpdateEvent)
 	 */
 	private void notifyUpdate_objectUpdated(final Document document) {
 		synchronized (DocumentModelImpl.updateListenersLock) {
 			for(UpdateListener listener : DocumentModelImpl.updateListeners) {
 				listener.objectUpdated(new UpdateEvent(document));
 			}
 		}
 	}
 
 	/**
 	 * Use the operating system to open a file. Decide which operating system we
 	 * are using and dispatch to the correct api.
 	 * 
 	 * @param file
 	 *            The file to open.
 	 * @throws IOException
 	 */
 	private void open(final File file) throws IOException {
 		switch(OSUtil.getOS()) {
 		case WINDOWS_2000:
 		case WINDOWS_XP:
 			openWin32(file);
 			break;
 		case LINUX:
 		default:
 			Assert.assertNotYetImplemented("launchFile(File)");
 			break;
 		}
 	}
 
 	/**
 	 * Use the operating system to open a file in a win32 environment.
 	 * 
 	 * @param file
 	 *            The file to open.
 	 * @throws IOException
 	 */
 	private void openWin32(final File file) throws IOException {
 		Runtime.getRuntime().exec(
 				new String[] {
 						"rundll32.exe",
 						"url.dll,FileProtocolHandler",
 						file.getAbsolutePath() });
 	}
 
 	/**
 	 * Write the content of a document to a file.
 	 * 
 	 * @param file
 	 *            The target file.
 	 * @param document
 	 *            The source parity document.
 	 * @throws IOException
 	 */
 	private void writeDocumentContent(final File file, final Document document)
 			throws IOException {
 		FileUtil.writeFile(file, document.getContent());
 	}
 }
