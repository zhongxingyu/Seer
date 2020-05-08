 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.controller;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.callbacks.ESUpdateCallback;
 import org.eclipse.emf.emfstore.client.exceptions.ESProjectNotSharedException;
 import org.eclipse.emf.emfstore.client.observer.ESUpdateObserver;
 import org.eclipse.emf.emfstore.internal.client.common.UnknownEMFStoreWorkloadCommand;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.ChangeConflictException;
 import org.eclipse.emf.emfstore.internal.client.model.impl.ProjectSpaceBase;
 import org.eclipse.emf.emfstore.internal.common.APIUtil;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ConflictBucketCandidate;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ConflictDetector;
 import org.eclipse.emf.emfstore.internal.server.conflictDetection.ModelElementIdToEObjectMappingImpl;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESChangePackage;
 
 /**
  * Controller class for updating a project space.
  * 
  * @author ovonwesen
  * @author emueller
  */
 public class UpdateController extends ServerCall<PrimaryVersionSpec> {
 
 	private VersionSpec version;
 	private ESUpdateCallback callback;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param projectSpace
 	 *            the project space to be updated
 	 * @param version
 	 *            the target version
 	 * @param callback
 	 *            an optional update callback instance
 	 * @param progress
 	 *            a progress monitor that is used to indicate the progress of the update
 	 */
 	public UpdateController(ProjectSpaceBase projectSpace, VersionSpec version, ESUpdateCallback callback,
 		IProgressMonitor progress) {
 		super(projectSpace);
 
 		if (!projectSpace.isShared()) {
 			throw new ESProjectNotSharedException();
 		}
 
 		// SANITY CHECKS
 		if (version == null) {
 			version = Versions.createHEAD(projectSpace.getBaseVersion());
 		}
 		if (callback == null) {
 			callback = ESUpdateCallback.NOCALLBACK;
 		}
 
 		this.version = version;
 		this.callback = callback;
 		setProgressMonitor(progress);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall#run()
 	 */
 	@Override
 	protected PrimaryVersionSpec run() throws ESException {
 		return doUpdate(version);
 	}
 
 	private PrimaryVersionSpec doUpdate(VersionSpec version) throws ChangeConflictException, ESException {
 		getProgressMonitor().beginTask("Updating Project...", 100);
 		getProgressMonitor().worked(1);
 		getProgressMonitor().subTask("Resolving new version");
 		final PrimaryVersionSpec resolvedVersion = getProjectSpace().resolveVersionSpec(version, getProgressMonitor());
 		if (resolvedVersion.compareTo(getProjectSpace().getBaseVersion()) == 0) {
 			return resolvedVersion;
 		}
 		getProgressMonitor().worked(5);
 
 		if (getProgressMonitor().isCanceled()) {
 			return getProjectSpace().getBaseVersion();
 		}
 
 		getProgressMonitor().subTask("Fetching changes from server");
 		List<ChangePackage> changes = new UnknownEMFStoreWorkloadCommand<List<ChangePackage>>(getProgressMonitor()) {
 			@Override
 			public List<ChangePackage> run(IProgressMonitor monitor) throws ESException {
 				return getConnectionManager().getChanges(getSessionId(), getProjectSpace().getProjectId(),
					getProjectSpace().getBaseVersion(), resolvedVersion);
 			}
 		}.execute();
 
 		ChangePackage localChanges = getProjectSpace().getLocalChangePackage(false);
 
 		// build a mapping including deleted and create model elements in local and incoming change packages
 		ModelElementIdToEObjectMappingImpl idToEObjectMapping = new ModelElementIdToEObjectMappingImpl(
 			getProjectSpace().getProject(), changes);
 		idToEObjectMapping.put(localChanges);
 
 		getProgressMonitor().worked(65);
 
 		if (getProgressMonitor().isCanceled()) {
 			return getProjectSpace().getBaseVersion();
 		}
 
 		getProgressMonitor().subTask("Checking for conflicts");
 
 		ConflictDetector conflictDetector = new ConflictDetector();
 
 		List<ESChangePackage> copy = APIUtil.mapToAPI(ESChangePackage.class, changes);
 
 		// TODO ASYNC review this cancel
 		if (getProgressMonitor().isCanceled()
 			|| !callback.inspectChanges(getProjectSpace().toAPI(), copy, idToEObjectMapping.toAPI())) {
 			return getProjectSpace().getBaseVersion();
 		}
 
 		ESWorkspaceProviderImpl
 			.getObserverBus()
 			.notify(ESUpdateObserver.class)
 			.inspectChanges(getProjectSpace().toAPI(), copy, getProgressMonitor());
 
 		boolean potentialConflictsDetected = false;
 		if (getProjectSpace().getOperations().size() > 0) {
 			Set<ConflictBucketCandidate> conflictBucketCandidates = conflictDetector
				.calculateConflictCandidateBuckets(Collections.singletonList(localChanges), changes, idToEObjectMapping);
 			potentialConflictsDetected = conflictDetector.containsConflictingBuckets(conflictBucketCandidates);
 			if (potentialConflictsDetected) {
 				getProgressMonitor().subTask("Conflicts detected, calculating conflicts");
 				ChangeConflictException conflictException = new ChangeConflictException(new ChangeConflict(
 					getProjectSpace(), Arrays.asList(localChanges), changes, conflictBucketCandidates,
 					idToEObjectMapping).toAPI());
 				if (callback.conflictOccurred(conflictException.getChangeConflict(), getProgressMonitor())) {
 					return getProjectSpace().getBaseVersion();
 				} else {
 					throw conflictException;
 				}
 			}
 		}
 
 		getProgressMonitor().worked(15);
 
 		getProgressMonitor().subTask("Applying changes");
 
 		getProjectSpace().applyChanges(resolvedVersion, changes, localChanges, callback, getProgressMonitor());
 
 		ESWorkspaceProviderImpl.getObserverBus().notify(ESUpdateObserver.class)
 			.updateCompleted(getProjectSpace().toAPI(), getProgressMonitor());
 
 		return getProjectSpace().getBaseVersion();
 	}
 }
