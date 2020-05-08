 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk, Edgar Mueller, Maximilian Koegel - initial API and implementation
  * Johannes Faltermeier - adaptions for independent storage
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature.Setting;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
 import org.eclipse.emf.ecore.util.EcoreUtil.UsageCrossReferencer;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback;
 import org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback;
 import org.eclipse.emf.emfstore.client.changetracking.ESCommandStack;
 import org.eclipse.emf.emfstore.client.handler.ESRunnableContext;
 import org.eclipse.emf.emfstore.client.observer.ESLoginObserver;
 import org.eclipse.emf.emfstore.client.observer.ESMergeObserver;
 import org.eclipse.emf.emfstore.client.util.ESClientURIUtil;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPoint;
 import org.eclipse.emf.emfstore.internal.client.importexport.impl.ExportChangesController;
 import org.eclipse.emf.emfstore.internal.client.importexport.impl.ExportProjectController;
 import org.eclipse.emf.emfstore.internal.client.model.CompositeOperationHandle;
 import org.eclipse.emf.emfstore.internal.client.model.Configuration;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.merging.ConflictResolver;
 import org.eclipse.emf.emfstore.internal.client.model.changeTracking.notification.recording.NotificationRecorder;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.internal.client.model.controller.CommitController;
 import org.eclipse.emf.emfstore.internal.client.model.controller.ShareController;
 import org.eclipse.emf.emfstore.internal.client.model.controller.UpdateController;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.ChangeConflictException;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.IllegalProjectSpaceStateException;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.MEUrlResolutionException;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.PropertyNotFoundException;
 import org.eclipse.emf.emfstore.internal.client.model.filetransfer.FileDownloadStatus;
 import org.eclipse.emf.emfstore.internal.client.model.filetransfer.FileInformation;
 import org.eclipse.emf.emfstore.internal.client.model.filetransfer.FileTransferManager;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESLocalProjectImpl;
 import org.eclipse.emf.emfstore.internal.client.model.util.WorkspaceUtil;
 import org.eclipse.emf.emfstore.internal.client.observers.DeleteProjectSpaceObserver;
 import org.eclipse.emf.emfstore.internal.client.properties.PropertyManager;
 import org.eclipse.emf.emfstore.internal.common.APIUtil;
 import org.eclipse.emf.emfstore.internal.common.ESDisposable;
 import org.eclipse.emf.emfstore.internal.common.ExtensionRegistry;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.impl.IdentifiableElementImpl;
 import org.eclipse.emf.emfstore.internal.common.model.impl.ProjectImpl;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.common.model.util.SerializationException;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ChangeConflictSet;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ConflictBucket;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ConflictDetector;
 import org.eclipse.emf.emfstore.internal.server.exceptions.FileTransferException;
 import org.eclipse.emf.emfstore.internal.server.exceptions.InvalidVersionSpecException;
 import org.eclipse.emf.emfstore.internal.server.model.FileIdentifier;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.OrgUnitProperty;
 import org.eclipse.emf.emfstore.internal.server.model.url.ModelElementUrlFragment;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESChangePackage;
 
 /**
  * Project space base class that contains custom user methods.
  * 
  * @author koegel
  * @author wesendon
  * @author emueller
  * @author jfaltermeier
  * 
  */
 public abstract class ProjectSpaceBase extends IdentifiableElementImpl implements ProjectSpace, ESLoginObserver,
 	ESDisposable {
 
 	private ESLocalProjectImpl esLocalProjectImpl;
 
 	private boolean initCompleted;
 	private boolean isTransient;
 	private boolean disposed;
 
 	private FileTransferManager fileTransferManager;
 	private OperationManager operationManager;
 
 	private PropertyManager propertyManager;
 	private final Map<String, OrgUnitProperty> propertyMap;
 
 	private ResourceSet resourceSet;
 	private ResourcePersister resourcePersister;
 
 	private ECrossReferenceAdapter crossReferenceAdapter;
 	private ESRunnableContext runnableContext;
 
 	/**
 	 * Constructor.
 	 */
 	public ProjectSpaceBase() {
 		propertyMap = new LinkedHashMap<String, OrgUnitProperty>();
 		initRunnableContext();
 	}
 
 	/**
 	 * <p>
 	 * Provides a context in which a {@link Runnable} is executed.
 	 * </p>
 	 * <p>
 	 * This may be used to provide a context while applying operations on a
 	 * {@link org.eclipse.emf.emfstore.client.ESLocalProject}.
 	 * </p>
 	 * 
 	 * @param runnableContext
 	 *            the runnable context to be set
 	 */
 	public void setRunnableContext(ESRunnableContext runnableContext) {
 		this.runnableContext = runnableContext;
 	}
 
 	private void initRunnableContext() {
 		runnableContext = ExtensionRegistry.INSTANCE.get(
 			RUNNABLE_CONTEXT_ID,
 			ESRunnableContext.class,
 			new DefaultRunnableContext(), true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#addFile(java.io.File)
 	 */
 	public FileIdentifier addFile(File file) throws FileTransferException {
 		return fileTransferManager.addFile(file);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#addOperations(java.util.List)
 	 */
 	public void addOperations(List<? extends AbstractOperation> operations) {
 		getOperations().addAll(operations);
 		updateDirtyState();
 
 		for (final AbstractOperation op : operations) {
 			operationManager.notifyOperationExecuted(op);
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#addTag(org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.internal.server.model.versioning.TagVersionSpec)
 	 */
 	public void addTag(PrimaryVersionSpec versionSpec, TagVersionSpec tag) throws ESException {
 		final ConnectionManager cm = ESWorkspaceProviderImpl.getInstance().getConnectionManager();
 		cm.addTag(getUsersession().getSessionId(), getProjectId(), versionSpec, tag);
 	}
 
 	/**
 	 * Helper method which applies merged changes on the ProjectSpace. This
 	 * method is used by merge mechanisms in update as well as branch merging.
 	 * 
 	 * @param baseSpec
 	 *            new base version
 	 * @param incoming
 	 *            changes from the current branch
 	 * @param myChanges
 	 *            merged changes
 	 * @param progressMonitor
 	 *            an {@link IProgressMonitor} to inform about the progress of the UpdateCallback in case it is called
 	 * @param runChecksumTestOnBaseSpec
 	 *            whether the checksum check is performed while applying the changes
 	 * 
 	 * @throws ESException in case the checksum comparison failed and the activated IChecksumErrorHandler
 	 *             also failed
 	 */
 	public void applyChanges(PrimaryVersionSpec baseSpec, List<ChangePackage> incoming, ChangePackage myChanges,
 		IProgressMonitor progressMonitor, boolean runChecksumTestOnBaseSpec) throws ESException {
 
 		// revert local changes
 		notifyPreRevertMyChanges(getLocalChangePackage());
 		revert();
 		notifyPostRevertMyChanges();
 
 		// apply changes from repo. incoming (aka theirs)
 		applyChangePackages(incoming, false);
		notifyPostApplyTheirChanges(incoming);

 		if (runChecksumTestOnBaseSpec) {
 			runChecksumTests(baseSpec, incoming, progressMonitor);
 		}
 		// reapply local changes
 		applyOperations(myChanges.getOperations(), true);
 		notifyPostApplyMergedChanges(myChanges);
 
 		setBaseVersion(baseSpec);
 		saveProjectSpaceOnly();
 	}
 
 	private void runChecksumTests(PrimaryVersionSpec baseSpec, List<ChangePackage> incoming,
 		IProgressMonitor progressMonitor)
 		throws ESException {
 		progressMonitor.subTask("Computing checksum");
 		if (!performChecksumCheck(baseSpec, getProject())) {
 			progressMonitor.subTask("Invalid checksum.  Activating checksum error handler.");
 			final boolean errorHandled = Configuration.getClientBehavior().getChecksumErrorHandler()
 				.execute(toAPI(), baseSpec.toAPI(),
 					progressMonitor);
 			if (!errorHandled) {
 				// rollback
 				for (int i = incoming.size() - 1; i >= 0; i--) {
 					applyChangePackage(incoming.get(i).reverse(), false);
 				}
 				applyChangePackage(getLocalChangePackage(), true);
 
 				throw new ESException("Update cancelled by checksum error handler due to invalid checksum.");
 			}
 		}
 	}
 
 	private void applyChangePackage(ChangePackage changePackage, boolean addOperations) {
 		applyOperations(changePackage.getOperations(), addOperations);
 	}
 
 	private void applyChangePackages(List<ChangePackage> changePackages, boolean addOperations) {
 		for (final ChangePackage changePackage : changePackages) {
 			applyChangePackage(changePackage, addOperations);
 		}
 	}
 
 	private boolean performChecksumCheck(PrimaryVersionSpec baseVersion, Project project) {
 
 		if (Configuration.getClientBehavior().isChecksumCheckActive()) {
 			final long expectedChecksum = baseVersion.getProjectStateChecksum();
 			try {
 				final long computedChecksum = ModelUtil.computeChecksum(project);
 				return expectedChecksum == computedChecksum;
 			} catch (final SerializationException e) {
 				WorkspaceUtil.logWarning("Could not compute checksum while applying changes.", e);
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Applies a list of operations to the project. The change tracking will be
 	 * stopped meanwhile.
 	 * 
 	 * 
 	 * @param operations
 	 *            the list of operations to be applied upon the project space
 	 * @param addOperations
 	 *            whether the operations should be saved in project space
 	 * 
 	 */
 	public void applyOperations(List<AbstractOperation> operations, boolean addOperations) {
 		executeRunnable(new ApplyOperationsRunnable(this, operations, addOperations));
 	}
 
 	/**
 	 * Executes a given {@link Runnable} in the context of this {@link ProjectSpace}.<br>
 	 * The {@link Runnable} usually modifies the Project contained in the {@link ProjectSpace}.
 	 * 
 	 * @param runnable
 	 *            the {@link Runnable} to be executed in the context of this {@link ProjectSpace}
 	 */
 	public void executeRunnable(Runnable runnable) {
 		getRunnableContext().executeRunnable(runnable);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#beginCompositeOperation()
 	 */
 	public CompositeOperationHandle beginCompositeOperation() {
 		return operationManager.beginCompositeOperation();
 	}
 
 	/**
 	 * Removes the elements that are marked as cutted from the project.
 	 */
 	public void cleanCutElements() {
 		final List<EObject> cutElements = new ArrayList<EObject>(getProject().getCutElements());
 		for (final EObject cutElement : cutElements) {
 			getProject().deleteModelElement(cutElement);
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#commit(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public PrimaryVersionSpec commit(IProgressMonitor monitor) throws ESException {
 		return new CommitController(this, null, null, monitor).execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#commit(java.lang.String,
 	 *      org.eclipse.emf.emfstore.client.callbacks.ESCommitCallback, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public PrimaryVersionSpec commit(String logMessage, ESCommitCallback callback, IProgressMonitor monitor)
 		throws ESException {
 		return new CommitController(this, logMessage, callback, monitor).execute();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public PrimaryVersionSpec commitToBranch(BranchVersionSpec branch, String logMessage,
 		ESCommitCallback callback,
 		IProgressMonitor monitor) throws ESException {
 		return new CommitController(this, branch, logMessage, callback, monitor).execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#exportLocalChanges(java.io.File,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void exportLocalChanges(File file, IProgressMonitor progressMonitor) throws IOException {
 		new ExportChangesController(this).execute(file, progressMonitor);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#exportLocalChanges(java.io.File)
 	 */
 	public void exportLocalChanges(File file) throws IOException {
 		new ExportChangesController(this).execute(file, new NullProgressMonitor());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#exportProject(java.io.File,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void exportProject(File file, IProgressMonitor progressMonitor) throws IOException {
 		new ExportProjectController(this).execute(file, progressMonitor);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#exportProject(java.io.File)
 	 */
 	public void exportProject(File file) throws IOException {
 		new ExportProjectController(this).execute(file, new NullProgressMonitor());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getChanges(org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec,
 	 *      org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec)
 	 */
 	public List<ChangePackage> getChanges(VersionSpec sourceVersion, VersionSpec targetVersion)
 		throws InvalidVersionSpecException, ESException {
 		// TODO: is this a server call?
 		final ConnectionManager connectionManager = ESWorkspaceProviderImpl.getInstance().getConnectionManager();
 		final List<ChangePackage> changes = connectionManager.getChanges(getUsersession().getSessionId(),
 			getProjectId(),
 			sourceVersion, targetVersion);
 		return changes;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getFile(org.eclipse.emf.emfstore.internal.server.model.FileIdentifier)
 	 */
 	public FileDownloadStatus getFile(FileIdentifier fileIdentifier) throws FileTransferException {
 		return fileTransferManager.getFile(fileIdentifier, false);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getFileInfo(org.eclipse.emf.emfstore.internal.server.model.FileIdentifier)
 	 */
 	public FileInformation getFileInfo(FileIdentifier fileIdentifier) {
 		return fileTransferManager.getFileInfo(fileIdentifier);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getLocalChangePackage()
 	 */
 	public ChangePackage getLocalChangePackage(boolean canonize) {
 		final ChangePackage changePackage = VersioningFactory.eINSTANCE.createChangePackage();
 		// copy operations from ProjectSpace
 		for (final AbstractOperation abstractOperation : getOperations()) {
 			final AbstractOperation copy = ModelUtil.clone(abstractOperation);
 			changePackage.getOperations().add(copy);
 		}
 		final LogMessage logMessage = VersioningFactory.eINSTANCE.createLogMessage();
 		if (getUsersession() != null) {
 			logMessage.setAuthor(getUsersession().getUsername());
 		}
 		else {
 			logMessage.setAuthor("<Unkown>");
 		}
 		logMessage.setClientDate(new Date());
 		changePackage.setLogMessage(logMessage);
 		return changePackage;
 	}
 
 	/**
 	 * Get the current notification recorder.
 	 * 
 	 * @return the recorder
 	 */
 	public NotificationRecorder getNotificationRecorder() {
 		return operationManager.getNotificationRecorder();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getOperationManager()
 	 */
 	public OperationManager getOperationManager() {
 		return operationManager;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getOperations()
 	 */
 	public List<AbstractOperation> getOperations() {
 		ChangePackage localChangePackage = getLocalChangePackage();
 		if (localChangePackage == null) {
 			setLocalChangePackage(VersioningFactory.eINSTANCE.createChangePackage());
 			localChangePackage = getLocalChangePackage();
 		}
 		return localChangePackage.getOperations();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getProjectInfo()
 	 */
 	public ProjectInfo getProjectInfo() {
 		final ProjectInfo projectInfo = org.eclipse.emf.emfstore.internal.server.model.ModelFactory.eINSTANCE
 			.createProjectInfo();
 		projectInfo.setProjectId(ModelUtil.clone(getProjectId()));
 		projectInfo.setName(getProjectName());
 		projectInfo.setDescription(getProjectDescription());
 		projectInfo.setVersion(ModelUtil.clone(getBaseVersion()));
 		return projectInfo;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getPropertyManager()
 	 */
 	public PropertyManager getPropertyManager() {
 		if (propertyManager == null) {
 			propertyManager = new PropertyManager(this);
 		}
 
 		return propertyManager;
 	}
 
 	/**
 	 * getter for a string argument - see {@link #setProperty(OrgUnitProperty)}.
 	 */
 	private OrgUnitProperty getProperty(String name) throws PropertyNotFoundException {
 		// sanity checks
 		if (getUsersession() != null && getUsersession().getACUser() != null) {
 			final OrgUnitProperty orgUnitProperty = propertyMap.get(name);
 			if (orgUnitProperty != null) {
 				return orgUnitProperty;
 			}
 		}
 		throw new PropertyNotFoundException(MessageFormat.format(
 			"Property {0} not found.", name));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#importLocalChanges(java.lang.String)
 	 */
 	public void importLocalChanges(String fileName) throws IOException {
 
 		final ResourceSetImpl resourceSet = new ResourceSetImpl();
 		final Resource resource = resourceSet.getResource(URI.createFileURI(fileName), true);
 		final EList<EObject> directContents = resource.getContents();
 		// sanity check
 
 		if (directContents.size() != 1 && !(directContents.get(0) instanceof ChangePackage)) {
 			throw new IOException("File is corrupt, does not contain Changes.");
 		}
 
 		final ChangePackage changePackage = (ChangePackage) directContents.get(0);
 
 		if (!initCompleted) {
 			init();
 		}
 
 		applyOperations(changePackage.getOperations(), true);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#init()
 	 */
 	public void init() {
 		initCrossReferenceAdapter();
 
 		final ESCommandStack commandStack = (ESCommandStack)
 			ESWorkspaceProviderImpl.getInstance().getEditingDomain().getCommandStack();
 
 		fileTransferManager = new FileTransferManager(this);
 		operationManager = new OperationManager(this);
 
 		initResourcePersister();
 
 		commandStack.addCommandStackObserver(operationManager);
 		commandStack.addCommandStackObserver(resourcePersister);
 
 		// initialization order is important!
 		getProject().addIdEObjectCollectionChangeObserver(operationManager);
 		getProject().addIdEObjectCollectionChangeObserver(resourcePersister);
 
 		if (getProject() instanceof ProjectImpl) {
 			((ProjectImpl) getProject()).setUndetachable(operationManager);
 			((ProjectImpl) getProject()).setUndetachable(resourcePersister);
 		}
 
 		initPropertyMap();
 
 		startChangeRecording();
 		cleanCutElements();
 
 		initCompleted = true;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initPropertyMap() {
 		// TODO: deprecated, OrgUnitPropertiy will be removed soon
 		if (getUsersession() != null) {
 			ESWorkspaceProviderImpl.getObserverBus().register(this, ESLoginObserver.class);
 			final ACUser acUser = getUsersession().getACUser();
 			if (acUser != null) {
 				for (final OrgUnitProperty p : acUser.getProperties()) {
 					if (p.getProject() != null && p.getProject().equals(getProjectId())) {
 						propertyMap.put(p.getName(), p);
 					}
 				}
 			}
 		}
 	}
 
 	private void initCrossReferenceAdapter() {
 
 		// default
 		boolean useCrossReferenceAdapter = true;
 
 		for (final ESExtensionElement element : new ESExtensionPoint(
 			"org.eclipse.emf.emfstore.client.inverseCrossReferenceCache")
 			.getExtensionElements()) {
 			useCrossReferenceAdapter &= element.getBoolean("activated");
 		}
 
 		if (useCrossReferenceAdapter) {
 			crossReferenceAdapter = new ECrossReferenceAdapter();
 			getProject().eAdapters().add(crossReferenceAdapter);
 		}
 	}
 
 	private void initResourcePersister() {
 
 		resourcePersister = new ResourcePersister(getProject());
 
 		if (!isTransient) {
 			resourcePersister.addResource(eResource());
 			resourcePersister.addResource(getLocalChangePackage().eResource());
 			resourcePersister.addResource(getProject().eResource());
 			resourcePersister.addDirtyStateChangeLister(new ESLocalProjectSaveStateNotifier(toAPI()));
 			ESWorkspaceProviderImpl.getObserverBus().register(resourcePersister);
 		}
 	}
 
 	/**
 	 * Returns the file transfer manager.
 	 * 
 	 * @return the file transfer manager
 	 */
 	public FileTransferManager getFileTransferManager() {
 		return fileTransferManager;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#initResources(org.eclipse.emf.ecore.resource.ResourceSet)
 	 */
 	public void initResources(ResourceSet resourceSet) {
 		this.resourceSet = resourceSet;
 		initCompleted = true;
 
 		final URI projectSpaceURI = ESClientURIUtil.createProjectSpaceURI(this);
 		final URI operationsURI = ESClientURIUtil.createOperationsURI(this);
 		final URI projectURI = ESClientURIUtil.createProjectURI(this);
 
 		setResourceCount(0);
 
 		final List<Resource> resources = new ArrayList<Resource>();
 		final Resource resource = resourceSet.createResource(projectURI);
 		// if resource splitting fails, we need a reference to the old resource
 		resource.getContents().add(getProject());
 		resources.add(resource);
 		setResourceCount(getResourceCount() + 1);
 
 		for (final EObject modelElement : getProject().getAllModelElements()) {
 			((XMIResource) resource).setID(modelElement, getProject().getModelElementId(modelElement).getId());
 		}
 
 		final Resource localChangePackageResource = resourceSet.createResource(operationsURI);
 		if (this.getLocalChangePackage() == null) {
 			setLocalChangePackage(VersioningFactory.eINSTANCE.createChangePackage());
 		}
 		localChangePackageResource.getContents().add(this.getLocalChangePackage());
 		resources.add(localChangePackageResource);
 
 		final Resource projectSpaceResource = resourceSet.createResource(projectSpaceURI);
 		projectSpaceResource.getContents().add(this);
 		resources.add(projectSpaceResource);
 
 		// save all resources that have been created
 		for (final Resource currentResource : resources) {
 			try {
 				ModelUtil.saveResource(currentResource, WorkspaceUtil.getResourceLogger());
 			} catch (final IOException e) {
 				WorkspaceUtil.logException("Project Space resource init failed!", e);
 			}
 		}
 
 		init();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#delete(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void delete(IProgressMonitor monitor) throws IOException {
 
 		ESWorkspaceProviderImpl.getObserverBus().notify(DeleteProjectSpaceObserver.class).projectSpaceDeleted(this);
 
 		// delete project to notify listeners
 		getProject().delete();
 
 		// remove resources from resource set and delete them
 		deleteResource(getProject().eResource());
 		deleteResource(eResource());
 		deleteResource(getLocalChangePackage().eResource());
 
 		// TODO: remove project space from workspace, this is not the case if delete
 		// is performed via Workspace#deleteProjectSpace
 		ESWorkspaceProviderImpl.getInstance().getInternalWorkspace().getProjectSpaces().remove(this);
 
 		dispose();
 	}
 
 	private void deleteResource(Resource resource) throws IOException {
 		if (resource != null) {
 			resource.delete(null);
 		}
 	}
 
 	/**
 	 * Returns the {@link ECrossReferenceAdapter}, if available.
 	 * 
 	 * @param modelElement
 	 *            the model element for which to find inverse cross references
 	 * 
 	 * @return the {@link ECrossReferenceAdapter}
 	 */
 	public Collection<Setting> findInverseCrossReferences(EObject modelElement) {
 		if (crossReferenceAdapter != null) {
 			return crossReferenceAdapter.getInverseReferences(modelElement);
 		}
 
 		return UsageCrossReferencer.find(modelElement, resourceSet);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getResourceSet()
 	 */
 	public ResourceSet getResourceSet() {
 		return resourceSet;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#setResourceSet(org.eclipse.emf.ecore.resource.ResourceSet)
 	 */
 	public void setResourceSet(ResourceSet resourceSet) {
 		this.resourceSet = resourceSet;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#isTransient()
 	 */
 	public boolean isTransient() {
 		return isTransient;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#isUpdated()
 	 */
 	public boolean isUpdated() throws ESException {
 		final PrimaryVersionSpec headVersion = resolveVersionSpec(Versions.createHEAD(getBaseVersion()),
 			new NullProgressMonitor());
 		return getBaseVersion().equals(headVersion);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void loginCompleted(ESUsersession session) {
 		// TODO Implement possibility in observerbus to register only for
 		// certain notifier
 		if (getUsersession() == null || !getUsersession().toAPI().equals(session)) {
 			return;
 		}
 		try {
 			transmitProperties();
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (final RuntimeException e) {
 			// END SUPRESS CATCH EXCEPTION
 			WorkspaceUtil.logException("Resuming file transfers or transmitting properties failed!", e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#makeTransient()
 	 */
 	public void makeTransient() {
 		if (initCompleted) {
 			throw new IllegalAccessError("Project Space cannot be set to transient after init.");
 		}
 		isTransient = true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void mergeBranch(final PrimaryVersionSpec branchSpec, final ConflictResolver conflictResolver,
 		final IProgressMonitor monitor)
 		throws ESException {
 
 		if (branchSpec == null || conflictResolver == null) {
 			throw new IllegalArgumentException("Arguments must not be null.");
 		}
 
 		if (Versions.isSameBranch(getBaseVersion(), branchSpec)) {
 			throw new InvalidVersionSpecException("Can't merge branch with itself.");
 		}
 
 		final PrimaryVersionSpec commonAncestor = new ServerCall<PrimaryVersionSpec>(this) {
 			@Override
 			protected PrimaryVersionSpec run() throws ESException {
 				return resolveVersionSpec(Versions.createANCESTOR(getBaseVersion(),
 					branchSpec), monitor);
 			}
 		}.execute();
 
 		final List<ChangePackage> baseChanges = getChanges(commonAncestor, getBaseVersion());
 		final List<ChangePackage> branchChanges = getChanges(commonAncestor, branchSpec);
 
 		final ChangeConflictSet conflictSet = new ConflictDetector().calculateConflicts(branchChanges,
 			baseChanges, getProject());
 
 		if (conflictResolver.resolveConflicts(getProject(), conflictSet)) {
 			final ChangePackage resolvedConflicts = mergeResolvedConflicts(conflictSet, branchChanges, baseChanges);
 			RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 
 				public Void call() throws Exception {
 					applyChanges(getBaseVersion(), baseChanges, resolvedConflicts, monitor, false);
 					setMergedVersion(ModelUtil.clone(branchSpec));
 					return null;
 				}
 			});
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#mergeResolvedConflicts(org.eclipse.emf.emfstore.internal.server.conflictDetection.ChangeConflictSet,
 	 *      java.util.List, java.util.List)
 	 */
 	public ChangePackage mergeResolvedConflicts(ChangeConflictSet conflictSet,
 		List<ChangePackage> myChangePackages, List<ChangePackage> theirChangePackages)
 		throws ChangeConflictException {
 
 		final Set<AbstractOperation> accceptedMineSet = new LinkedHashSet<AbstractOperation>();
 		final Set<AbstractOperation> rejectedTheirsSet = new LinkedHashSet<AbstractOperation>();
 
 		for (final ConflictBucket conflict : conflictSet.getConflictBuckets()) {
 			if (!conflict.isResolved()) {
 				throw new ChangeConflictException(
 					"Conflict during update occured and callback failed to resolve all conflicts!",
 					conflictSet);
 			}
 			accceptedMineSet.addAll(conflict.getAcceptedLocalOperations());
 			rejectedTheirsSet.addAll(conflict.getRejectedRemoteOperations());
 		}
 
 		final List<AbstractOperation> acceptedMineList = new LinkedList<AbstractOperation>();
 		for (final ChangePackage locChangePackage : myChangePackages) {
 			for (final AbstractOperation myOp : locChangePackage.getOperations()) {
 				if (conflictSet.getNotInvolvedInConflict().contains(myOp)) {
 					acceptedMineList.add(myOp);
 				} else if (accceptedMineSet.contains(myOp)) {
 					acceptedMineList.add(myOp);
 				}
 				accceptedMineSet.remove(myOp);
 			}
 		}
 		// add all remaining operations in acceptedMineSet (they have been generated during merge)
 		accceptedMineSet.addAll(accceptedMineSet);
 
 		final List<AbstractOperation> rejectedTheirsList = new LinkedList<AbstractOperation>();
 		for (final ChangePackage theirCP : theirChangePackages) {
 			for (final AbstractOperation theirOp : theirCP.getOperations()) {
 				if (rejectedTheirsSet.contains(theirOp)) {
 					rejectedTheirsList.add(theirOp);
 				}
 			}
 		}
 
 		final List<AbstractOperation> mergeResult = new ArrayList<AbstractOperation>(rejectedTheirsList.size()
 			+ acceptedMineList.size());
 		for (final AbstractOperation operationToReverse : rejectedTheirsList) {
 			mergeResult.add(0, operationToReverse.reverse());
 		}
 
 		mergeResult.addAll(acceptedMineList);
 		final ChangePackage result = VersioningFactory.eINSTANCE.createChangePackage();
 		result.getOperations().addAll(mergeResult);
 
 		return result;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#getBranches()
 	 */
 	public List<BranchInfo> getBranches() throws ESException {
 		return new ServerCall<List<BranchInfo>>(this) {
 			@Override
 			protected List<BranchInfo> run() throws ESException {
 				final ConnectionManager cm = ESWorkspaceProviderImpl.getInstance().getConnectionManager();
 				return cm.getBranches(getSessionId(), getProjectId());
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#removeTag(org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.internal.server.model.versioning.TagVersionSpec)
 	 */
 	public void removeTag(PrimaryVersionSpec versionSpec, TagVersionSpec tag) throws ESException {
 		final ConnectionManager cm = ESWorkspaceProviderImpl.getInstance().getConnectionManager();
 		cm.removeTag(getUsersession().getSessionId(), getProjectId(), versionSpec, tag);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#resolve(org.eclipse.emf.emfstore.internal.server.model.url.ModelElementUrlFragment)
 	 */
 	public EObject resolve(ModelElementUrlFragment modelElementUrlFragment) throws MEUrlResolutionException {
 		final ModelElementId modelElementId = modelElementUrlFragment.getModelElementId();
 		final EObject modelElement = getProject().getModelElement(modelElementId);
 		if (modelElement == null) {
 			throw new MEUrlResolutionException();
 		}
 		return modelElement;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#resolveVersionSpec(org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public PrimaryVersionSpec resolveVersionSpec(final VersionSpec versionSpec, IProgressMonitor monitor)
 		throws InvalidVersionSpecException, ESException {
 		return new ServerCall<PrimaryVersionSpec>(this, monitor) {
 			@Override
 			protected PrimaryVersionSpec run() throws ESException {
 				return getConnectionManager().resolveVersionSpec(
 					getSessionId(),
 					getProjectId(),
 					versionSpec);
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#revert()
 	 */
 	public void revert() {
 		while (!getOperations().isEmpty()) {
 			undoLastOperation();
 		}
 		updateDirtyState();
 	}
 
 	/**
 	 * Saves the project space itself only, no containment children.
 	 */
 	public void saveProjectSpaceOnly() {
 		saveResource(eResource());
 	}
 
 	/**
 	 * Saves the project space.
 	 */
 	public void save() {
 		saveProjectSpaceOnly();
 		saveChangePackage();
 		resourcePersister.saveDirtyResources(true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#hasUnsavedChanges()
 	 */
 	public boolean hasUnsavedChanges() {
 
 		if (resourcePersister != null) {
 			return resourcePersister.isDirty();
 		}
 
 		// in case the project space has not been initialized yet
 		return false;
 	}
 
 	private void saveChangePackage() {
 		final ChangePackage localChangePackage = getLocalChangePackage();
 		if (localChangePackage.eResource() != null) {
 			saveResource(localChangePackage.eResource());
 		}
 	}
 
 	/**
 	 * Save the given resource that is part of the project space resource set.
 	 * 
 	 * @param resource
 	 *            the resource
 	 */
 	public void saveResource(Resource resource) {
 		try {
 			if (resource == null) {
 				if (!isTransient) {
 					WorkspaceUtil.logException("Resources of project space are not properly initialized!",
 						new IllegalProjectSpaceStateException("Resource to save is null"));
 				}
 				return;
 			}
 			ModelUtil.saveResource(resource, WorkspaceUtil.getResourceLogger());
 		} catch (final IOException e) {
 			WorkspaceUtil.logException("An error in the data was detected during save!"
 				+ " The safest way to deal with this problem is to delete this project and checkout again.", e);
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#setProperty(org.eclipse.emf.emfstore.internal.server.model.accesscontrol.OrgUnitProperty)
 	 */
 	public void setProperty(OrgUnitProperty property) {
 		// sanity checks
 		if (getUsersession() != null && getUsersession().getACUser() != null) {
 			try {
 				if (property.getProject() == null) {
 					property.setProject(ModelUtil.clone(getProjectId()));
 				} else if (!property.getProject().equals(getProjectId())) {
 					return;
 				}
 				final OrgUnitProperty prop = getProperty(property.getName());
 				prop.setValue(property.getValue());
 			} catch (final PropertyNotFoundException e) {
 				getUsersession().getACUser().getProperties().add(property);
 				propertyMap.put(property.getName(), property);
 			}
 			// the properties that have been altered are retained in a separate
 			// list
 			for (final OrgUnitProperty changedProperty : getUsersession().getChangedProperties()) {
 				if (changedProperty.getName().equals(property.getName())
 					&& changedProperty.getProject().equals(getProjectId())) {
 					changedProperty.setValue(property.getValue());
 					ESWorkspaceProviderImpl.getInstance().getWorkspace().toInternalAPI().save();
 					return;
 				}
 			}
 			getUsersession().getChangedProperties().add(property);
 			ESWorkspaceProviderImpl.getInstance().getWorkspace().toInternalAPI().save();
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#shareProject(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ProjectInfo shareProject(IProgressMonitor monitor) throws ESException {
 		return shareProject(null, monitor);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#shareProject(org.eclipse.emf.emfstore.internal.client.model.Usersession,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ProjectInfo shareProject(Usersession session, IProgressMonitor monitor) throws ESException {
 		return new ShareController(this, session, monitor).execute();
 	}
 
 	/**
 	 * Starts change recording on this workspace, resumes previous recordings if
 	 * there are any.
 	 */
 	public void startChangeRecording() {
 		operationManager.startChangeRecording();
 		updateDirtyState();
 	}
 
 	/**
 	 * Stops current recording of changes and adds recorded changes to this
 	 * project spaces changes.
 	 */
 	public void stopChangeRecording() {
 		if (operationManager != null) {
 			operationManager.stopChangeRecording();
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#transmitProperties()
 	 */
 	public void transmitProperties() {
 		final List<OrgUnitProperty> temp = new ArrayList<OrgUnitProperty>();
 		for (final OrgUnitProperty changedProperty : getUsersession().getChangedProperties()) {
 			if (changedProperty.getProject() != null && changedProperty.getProject().equals(getProjectId())) {
 				temp.add(changedProperty);
 			}
 		}
 		final ListIterator<OrgUnitProperty> iterator = temp.listIterator();
 		while (iterator.hasNext()) {
 			try {
 				ESWorkspaceProviderImpl
 					.getInstance()
 					.getConnectionManager()
 					.transmitProperty(getUsersession().getSessionId(), iterator.next(), getUsersession().getACUser(),
 						getProjectId());
 				iterator.remove();
 			} catch (final ESException e) {
 				WorkspaceUtil.logException("Transmission of properties failed with exception", e);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#undoLastOperation()
 	 */
 	public void undoLastOperation() {
 		undoLastOperations(1);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#undoLastOperation()
 	 */
 	public void undoLastOperations(int numberOfOperations) {
 
 		if (numberOfOperations <= 0) {
 			return;
 		}
 
 		if (!getOperations().isEmpty()) {
 			final List<AbstractOperation> operations = getOperations();
 			final AbstractOperation lastOperation = operations.get(operations.size() - 1);
 
 			applyOperations(Collections.singletonList(lastOperation.reverse()), false);
 			operationManager.notifyOperationUndone(lastOperation);
 
 			operations.remove(lastOperation);
 			undoLastOperations(--numberOfOperations);
 		}
 		updateDirtyState();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#update(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public PrimaryVersionSpec update(IProgressMonitor monitor) throws ESException {
 		return update(Versions.createHEAD(getBaseVersion()), null, monitor);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#update(org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec)
 	 */
 	public PrimaryVersionSpec update(final VersionSpec version) throws ESException {
 		return update(version, null, null);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#update(org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec,
 	 *      org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public PrimaryVersionSpec update(VersionSpec version, ESUpdateCallback callback, IProgressMonitor progress)
 		throws ESException {
 		return new UpdateController(this, version, callback, progress).execute();
 	}
 
 	/**
 	 * Updates the dirty state of the project space.
 	 */
 	public void updateDirtyState() {
 		setDirty(!getOperations().isEmpty());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.common.ESDisposable#dispose()
 	 */
 	@SuppressWarnings("unchecked")
 	public void dispose() {
 
 		if (disposed) {
 			return;
 		}
 
 		stopChangeRecording();
 
 		if (crossReferenceAdapter != null) {
 			getProject().eAdapters().remove(crossReferenceAdapter);
 		}
 
 		final ESCommandStack commandStack = (ESCommandStack)
 			ESWorkspaceProviderImpl.getInstance().getEditingDomain().getCommandStack();
 		commandStack.removeCommandStackObserver(operationManager);
 		commandStack.removeCommandStackObserver(resourcePersister);
 
 		getProject().removeIdEObjectCollectionChangeObserver(operationManager);
 		getProject().removeIdEObjectCollectionChangeObserver(resourcePersister);
 
 		ESWorkspaceProviderImpl.getObserverBus().unregister(resourcePersister);
 		ESWorkspaceProviderImpl.getObserverBus().unregister(this, ESLoginObserver.class);
 		ESWorkspaceProviderImpl.getObserverBus().unregister(this);
 
 		operationManager.dispose();
 		resourcePersister.dispose();
 		disposed = true;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.ProjectSpace#isShared()
 	 */
 	public boolean isShared() {
 		return getUsersession() != null;
 	}
 
 	private void notifyPreRevertMyChanges(final ChangePackage changePackage) {
 		ESWorkspaceProviderImpl.getObserverBus().notify(ESMergeObserver.class)
 			.preRevertMyChanges(toAPI(), changePackage.toAPI());
 	}
 
 	private void notifyPostRevertMyChanges() {
 		ESWorkspaceProviderImpl.getObserverBus().notify(ESMergeObserver.class).postRevertMyChanges(toAPI());
 	}
 
 	private void notifyPostApplyTheirChanges(List<ChangePackage> theirChangePackages) {
 		// TODO ASYNC review this cancel
 		ESWorkspaceProviderImpl.getObserverBus().notify(ESMergeObserver.class)
 			.postApplyTheirChanges(toAPI(), APIUtil.mapToAPI(ESChangePackage.class, theirChangePackages));
 	}
 
 	private void notifyPostApplyMergedChanges(ChangePackage changePackage) {
 		ESWorkspaceProviderImpl.getObserverBus().notify(ESMergeObserver.class)
 			.postApplyMergedChanges(
 				toAPI(), changePackage.toAPI());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.common.api.APIDelegate#toAPI()
 	 */
 	public ESLocalProjectImpl toAPI() {
 		if (esLocalProjectImpl == null) {
 			esLocalProjectImpl = createAPI();
 		}
 		return esLocalProjectImpl;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.common.api.APIDelegate#createAPI()
 	 */
 	public ESLocalProjectImpl createAPI() {
 		return new ESLocalProjectImpl(this);
 	}
 
 	/**
 	 * Returns the {@link ESRunnableContext} operations are applied in.
 	 * 
 	 * @return the runnable context operations are executed in
 	 */
 	public ESRunnableContext getRunnableContext() {
 		return runnableContext;
 	}
 
 }
