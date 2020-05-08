 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk, Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.impl.api;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.emfstore.client.ESRemoteProject;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.observer.ESCheckoutObserver;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.client.common.UnknownEMFStoreWorkloadCommand;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ConnectionManager;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.internal.common.APIUtil;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.query.ESHistoryQueryImpl;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.versionspec.ESPrimaryVersionSpecImpl;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.versionspec.ESTagVersionSpecImpl;
 import org.eclipse.emf.emfstore.internal.server.model.impl.api.versionspec.ESVersionSpecImpl;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.BranchInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersionSpec;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESBranchInfo;
 import org.eclipse.emf.emfstore.server.model.ESGlobalProjectId;
 import org.eclipse.emf.emfstore.server.model.ESHistoryInfo;
 import org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec;
 
 /**
  * Represents a remote project that is located on a remote server.
  * 
  * @author wesendon
  * @author emueller
  */
 public class ESRemoteProjectImpl implements ESRemoteProject {
 
 	/**
 	 * The current connection manager used to connect to the server(s).
 	 */
 	private final ProjectInfo projectInfo;
 	private final ServerInfo server;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param serverInfo
 	 *            the server the remote project is located on
 	 * @param projectInfo
 	 *            information about which project to access
 	 */
 	public ESRemoteProjectImpl(ServerInfo serverInfo, ProjectInfo projectInfo) {
 		server = serverInfo;
 		this.projectInfo = projectInfo;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getGlobalProjectId()
 	 */
 	public ESGlobalProjectId getGlobalProjectId() {
 		return getProjectInfo().getProjectId().toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getProjectName()
 	 */
 	public String getProjectName() {
 		return getProjectInfo().getName();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getBranches(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESBranchInfo> getBranches(IProgressMonitor monitor) throws ESException {
 		return new UnknownEMFStoreWorkloadCommand<List<ESBranchInfo>>(monitor) {
 			@Override
 			public List<ESBranchInfo> run(IProgressMonitor monitor) throws ESException {
 				return APIUtil.mapToAPI(ESBranchInfo.class, new ServerCall<List<BranchInfo>>(getServerInfo()) {
 					@Override
 					protected List<BranchInfo> run() throws ESException {
 						final ConnectionManager connectionManager = ESWorkspaceProviderImpl.getInstance()
 							.getConnectionManager();
 						return connectionManager.getBranches(
 							getSessionId(),
 							projectInfo.getProjectId());
 					}
 				}.execute());
 			}
 		}.execute();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#getBranches(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESBranchInfo> getBranches(ESUsersession usersession, IProgressMonitor monitor) throws ESException {
 		return APIUtil.mapToAPI(ESBranchInfo.class, new ServerCall<List<BranchInfo>>(getServerInfo()) {
 			@Override
 			protected List<BranchInfo> run() throws ESException {
 				return getConnectionManager().getBranches(
 					getSessionId(),
 					projectInfo.getProjectId());
 			}
 		}.execute());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#resolveVersionSpec(org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec resolveVersionSpec(final ESVersionSpec versionSpec, IProgressMonitor monitor)
 		throws ESException {
 
 		final ESVersionSpecImpl<?, ? extends VersionSpec> versionSpecImpl = (ESVersionSpecImpl<?, ?>) versionSpec;
 
 		final PrimaryVersionSpec primaryVersionSpec = new ServerCall<PrimaryVersionSpec>(getServerInfo(), monitor) {
 			@Override
 			protected PrimaryVersionSpec run() throws ESException {
 				return getConnectionManager().resolveVersionSpec(getSessionId(),
 					projectInfo.getProjectId(),
 					versionSpecImpl.toInternalAPI());
 			}
 		}.execute();
 
 		return primaryVersionSpec.toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#resolveVersionSpec(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESVersionSpec, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec resolveVersionSpec(ESUsersession session, final ESVersionSpec versionSpec,
 		IProgressMonitor monitor) throws ESException {
 
 		final Usersession usersession = ((ESUsersessionImpl) session).toInternalAPI();
 		final ESVersionSpecImpl<?, ? extends VersionSpec> versionSpecImpl = (ESVersionSpecImpl<?, ?>) versionSpec;
 
 		final PrimaryVersionSpec primaryVersionSpec = new ServerCall<PrimaryVersionSpec>(usersession) {
 			@Override
 			protected PrimaryVersionSpec run() throws ESException {
 				return getConnectionManager().resolveVersionSpec(getSessionId(),
 					projectInfo.getProjectId(),
 					versionSpecImpl.toInternalAPI());
 			}
 		}.execute();
 
 		return primaryVersionSpec.toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#getHistoryInfos(org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESHistoryInfo> getHistoryInfos(final ESHistoryQuery<? extends ESHistoryQuery<?>> query,
 		IProgressMonitor monitor)
 		throws ESException {
 
 		@SuppressWarnings("unchecked")
 		final ESHistoryQueryImpl<ESHistoryQuery<? extends ESHistoryQuery<?>>, ?> queryImpl =
 			(ESHistoryQueryImpl<ESHistoryQuery<? extends ESHistoryQuery<?>>, ?>) query;
 
 		return APIUtil.mapToAPI(ESHistoryInfo.class, new ServerCall<List<HistoryInfo>>(getServerInfo(), monitor) {
 			@Override
 			protected List<HistoryInfo> run() throws ESException {
 				return getConnectionManager().getHistoryInfo(
 					getSessionId(),
 					projectInfo.getProjectId(),
 					queryImpl.toInternalAPI());
 			}
 		}.execute());
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#getHistoryInfos(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.emf.emfstore.server.model.query.ESHistoryQuery, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public List<ESHistoryInfo> getHistoryInfos(ESUsersession session,
 		final ESHistoryQuery<? extends ESHistoryQuery<?>> query,
 		IProgressMonitor monitor) throws ESException {
 
 		final Usersession usersession = ((ESUsersessionImpl) session).toInternalAPI();
 		final ESHistoryQueryImpl<?, ?> queryImpl = (ESHistoryQueryImpl<?, ?>) query;
 
 		final List<HistoryInfo> historyInfos = new ServerCall<List<HistoryInfo>>(usersession,
 			monitor) {
 			@Override
 			protected List<HistoryInfo> run() throws ESException {
 				return getConnectionManager().getHistoryInfo(
 					getUsersession().getSessionId(),
 					getProjectInfo().getProjectId(),
 					queryImpl.toInternalAPI());
 			}
 		}.execute();
 
 		return APIUtil.mapToAPI(ESHistoryInfo.class, historyInfos);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#addTag(org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void addTag(final ESPrimaryVersionSpec primaryVersionSpec, final ESTagVersionSpec tagVersionSpec,
 		final IProgressMonitor monitor)
 		throws ESException {
 
 		final ESPrimaryVersionSpecImpl primaryVersionSpecImpl = (ESPrimaryVersionSpecImpl) primaryVersionSpec;
 		final ESTagVersionSpecImpl tagVersionSpecImpl = (ESTagVersionSpecImpl) tagVersionSpec;
 
 		RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 			public Void call() throws Exception {
 				return new ServerCall<Void>(getServerInfo(), monitor) {
 					@Override
 					protected Void run() throws ESException {
 						getConnectionManager().addTag(
 							getUsersession().getSessionId(),
 							getProjectInfo().getProjectId(),
 							primaryVersionSpecImpl.toInternalAPI(),
 							tagVersionSpecImpl.toInternalAPI());
 						return null;
 					}
 				}.execute();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESProject#removeTag(org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESTagVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void removeTag(final ESPrimaryVersionSpec versionSpec, final ESTagVersionSpec tag,
 		final IProgressMonitor monitor)
 		throws ESException {
 
 		final ESPrimaryVersionSpecImpl versionSpecImpl = (ESPrimaryVersionSpecImpl) versionSpec;
 		final ESTagVersionSpecImpl tagVersionSpecImpl = (ESTagVersionSpecImpl) tag;
 
 		RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 
 			public Void call() throws Exception {
 				new ServerCall<Void>(getServerInfo(), monitor) {
 					@Override
 					protected Void run() throws ESException {
 						getConnectionManager().removeTag(
 							getUsersession().getSessionId(),
 							projectInfo.getProjectId(),
 							versionSpecImpl.toInternalAPI(),
 							tagVersionSpecImpl.toInternalAPI());
 						return null;
 					}
 				}.execute();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#checkout(java.lang.String,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESLocalProjectImpl checkout(final String name, final IProgressMonitor monitor)
 		throws ESException {
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<ESLocalProjectImpl>() {
 			public ESLocalProjectImpl call() throws Exception {
 				return new ServerCall<ESLocalProjectImpl>(getServerInfo()) {
 					@Override
 					protected ESLocalProjectImpl run() throws ESException {
 						return checkout(name, getUsersession().toAPI(), monitor);
 					}
 				}.execute();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#checkout(java.lang.String,
 	 *      org.eclipse.emf.emfstore.client.ESUsersession, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESLocalProjectImpl checkout(final String name, final ESUsersession usersession,
 		final IProgressMonitor monitor)
 		throws ESException {
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<ESLocalProjectImpl>() {
 			public ESLocalProjectImpl call() throws Exception {
 				final ESPrimaryVersionSpec primaryVersionSpec = resolveVersionSpec(usersession, Versions.createHEAD()
 					.toAPI(), monitor);
 				return checkout(name, usersession, primaryVersionSpec, monitor);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#fetch(java.lang.String,
 	 *      org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESLocalProjectImpl fetch(final String name, final ESUsersession session,
 		final ESPrimaryVersionSpec versionSpec, final IProgressMonitor progressMonitor) throws ESException {
		final SubMonitor parentMonitor = SubMonitor.convert(progressMonitor,
			Messages.ESRemoteProjectImpl_Fetching_Title, 100);
 
 		final Usersession usersession = ((ESUsersessionImpl) session).toInternalAPI();
 		final ESPrimaryVersionSpecImpl primaryVersionSpecImpl = (ESPrimaryVersionSpecImpl) versionSpec;
 		final ProjectInfo projectInfoCopy = ModelUtil.clone(getProjectInfo());
 
 		projectInfoCopy.setVersion(primaryVersionSpecImpl.toInternalAPI());
 
 		return RunESCommand.WithException.runWithResult(ESException.class, new Callable<ESLocalProjectImpl>() {
 
 			public ESLocalProjectImpl call() throws Exception {
 
 				Project project = null;
 
 				final SubMonitor childMonitor = parentMonitor.newChild(40);
 				parentMonitor.subTask(Messages.ESRemoteProjectImpl_Fetching_Project);
 				project = new UnknownEMFStoreWorkloadCommand<Project>(childMonitor) {
 					@Override
 					public Project run(IProgressMonitor monitor) throws ESException {
 						return new ServerCall<Project>(usersession) {
 							@Override
 							protected Project run() throws ESException {
 								return getConnectionManager().getProject(usersession.getSessionId(),
 									getProjectInfo().getProjectId(),
 									projectInfoCopy.getVersion());
 							}
 						}.execute();
 					}
 				}.execute();
 
 				if (project == null) {
 					throw new ESException(Messages.ESRemoteProjectImpl_Server_Returned_Null_Project);
 				}
 
 				return initProjectSpace(usersession, projectInfoCopy, project, name).toAPI();
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#checkout(java.lang.String,
 	 *      org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.emf.emfstore.server.model.versionspec.ESPrimaryVersionSpec,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESLocalProjectImpl checkout(final String name, final ESUsersession session,
 		final ESPrimaryVersionSpec versionSpec, final IProgressMonitor progressMonitor)
 		throws ESException {
 		final ESLocalProjectImpl project = fetch(name, session, versionSpec, progressMonitor);
 		project.addToWorkspace(progressMonitor);
 		ESWorkspaceProviderImpl.getObserverBus().notify(ESCheckoutObserver.class)
 			.checkoutDone(project);
 
 		return project;
 	}
 
 	private ProjectSpace initProjectSpace(final Usersession usersession, final ProjectInfo projectInfoCopy,
 		Project project, String projectName) {
 
 		final ProjectSpace projectSpace = ModelFactory.eINSTANCE.createProjectSpace();
 		projectSpace.setProjectId(projectInfoCopy.getProjectId());
 		projectSpace.setProjectName(projectName);
 		projectSpace.setProjectDescription(projectInfoCopy.getDescription());
 		projectSpace.setBaseVersion(projectInfoCopy.getVersion());
 		projectSpace.setLastUpdated(new Date());
 		projectSpace.setUsersession(usersession);
 		projectSpace.setProject(project);
 		projectSpace.setResourceCount(0);
 
 		final ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 		final XMIResource res = (XMIResource) resourceSetImpl.createResource(ModelUtil.VIRTUAL_URI);
 		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
 		res.getContents().add(project);
 
 		projectSpace.setResourceSet(resourceSetImpl);
 
 		return projectSpace;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#delete(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void delete(final IProgressMonitor monitor) throws ESException {
 		RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 			public Void call() throws Exception {
 				getDeleteProjectServerCall()
 					.setProgressMonitor(monitor)
 					.setServer(getServerInfo())
 					.execute();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#delete(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void delete(ESUsersession session, final IProgressMonitor monitor) throws ESException {
 		final Usersession usersession = ((ESUsersessionImpl) session).toInternalAPI();
 		RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 			public Void call() throws Exception {
 				getDeleteProjectServerCall()
 					.setProgressMonitor(monitor)
 					.setUsersession(usersession)
 					.execute();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#getServer()
 	 */
 	public ESServerImpl getServer() {
 		return getServerInfo().toAPI();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESRemoteProject#getHeadVersion(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESPrimaryVersionSpec getHeadVersion(IProgressMonitor monitor) throws ESException {
 		return resolveVersionSpec(Versions.createHEAD().toAPI(), monitor);
 	}
 
 	private ServerCall<Void> getDeleteProjectServerCall() {
 		return new ServerCall<Void>() {
 			@Override
 			protected Void run() throws ESException {
 				new UnknownEMFStoreWorkloadCommand<Void>(getProgressMonitor()) {
 					@Override
 					public Void run(IProgressMonitor monitor) throws ESException {
 						getConnectionManager().deleteProject(getSessionId(), getProjectInfo().getProjectId(), true);
 						return null;
 					}
 				}.execute();
 
 				return null;
 			}
 		};
 	}
 
 	/**
 	 * Returns the project info of this remote project.
 	 * 
 	 * @return the project info of this remote project
 	 */
 	public ProjectInfo getProjectInfo() {
 		return projectInfo;
 	}
 
 	/**
 	 * Returns the server info of this remote project.
 	 * 
 	 * @return the server info of this remote project
 	 */
 	public ServerInfo getServerInfo() {
 		return server;
 	}
 }
