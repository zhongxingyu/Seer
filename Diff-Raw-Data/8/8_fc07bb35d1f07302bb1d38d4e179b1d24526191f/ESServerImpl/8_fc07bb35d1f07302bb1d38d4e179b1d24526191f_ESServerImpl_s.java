 /*******************************************************************************
  * Copyright (c) 2013 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.model.impl.api;
 
 import static org.eclipse.emf.emfstore.internal.common.APIUtil.copy;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.ESRemoteProject;
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.ESWorkspaceProvider;
 import org.eclipse.emf.emfstore.internal.client.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.connectionmanager.ServerCall;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommand;
 import org.eclipse.emf.emfstore.internal.client.model.util.EMFStoreCommandWithException;
 import org.eclipse.emf.emfstore.internal.client.model.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.common.APIUtil;
 import org.eclipse.emf.emfstore.internal.common.api.AbstractAPIImpl;
 import org.eclipse.emf.emfstore.internal.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.internal.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.VersioningFactory;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.server.model.ESGlobalProjectId;
 
 /**
  * Mapping between {@link ESServer} and {@link ServerInfo}.
  * 
  * @author emueller
  * 
  */
 public class ESServerImpl extends AbstractAPIImpl<ESServerImpl, ServerInfo> implements ESServer {
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param serverInfo
 	 *            the delegate
 	 */
 	public ESServerImpl(ServerInfo serverInfo) {
 		super(serverInfo);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#getName()
 	 */
 	public String getName() {
 		return getInternalAPIImpl().getName();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#setName(java.lang.String)
 	 */
 	public void setName(final String serverName) {
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getInternalAPIImpl().setName(serverName);
 			}
 		}.run(false);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#getPort()
 	 */
 	public int getPort() {
 		return getInternalAPIImpl().getPort();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#setPort(int)
 	 */
 	public void setPort(final int port) {
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getInternalAPIImpl().setPort(port);
 			}
 		}.run(false);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#getURL()
 	 */
 	public String getURL() {
 		return getInternalAPIImpl().getUrl();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#setURL(java.lang.String)
 	 */
 	public void setURL(final String url) {
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getInternalAPIImpl().setUrl(url);
 			}
 		}.run(false);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#getCertificateAlias()
 	 */
 	public String getCertificateAlias() {
 		return getInternalAPIImpl().getCertificateAlias();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#setCertificateAlias(java.lang.String)
 	 */
 	public void setCertificateAlias(final String alias) {
 		new EMFStoreCommand() {
 			@Override
 			protected void doRun() {
 				getInternalAPIImpl().setCertificateAlias(alias);
 			}
 		}.run(false);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#getLastUsersession()
 	 */
 	public ESUsersession getLastUsersession() {
 		return getInternalAPIImpl().getLastUsersession().getAPIImpl();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#createRemoteProject(java.lang.String,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESRemoteProject createRemoteProject(final String projectName, IProgressMonitor monitor) throws ESException {
 		ProjectInfo projectInfo = getCreateRemoteProjectServerCall(projectName)
 			.setServer(getInternalAPIImpl()).execute();
 		return new ESRemoteProjectImpl(getInternalAPIImpl(), projectInfo);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#createRemoteProject(org.eclipse.emf.emfstore.client.ESUsersession,
 	 *      java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public ESRemoteProject createRemoteProject(ESUsersession usersession, final String projectName,
 		final IProgressMonitor progressMonitor) throws ESException {
 		ESUsersessionImpl session = (ESUsersessionImpl) validateUsersession(usersession);
 		ProjectInfo projectInfo = getCreateRemoteProjectServerCall(projectName)
 			.setUsersession(session.getInternalAPIImpl()).execute();
 		return new ESRemoteProjectImpl(getInternalAPIImpl(), projectInfo);
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#login(java.lang.String, java.lang.String)
 	 */
 	public ESUsersession login(String name, String password) throws ESException {
 
 		final ESWorkspaceImpl workspace = (ESWorkspaceImpl) ESWorkspaceProvider.INSTANCE.getWorkspace();
 		workspace.addServer(this);
 
 		final Usersession usersession = ModelFactory.eINSTANCE.createUsersession();
 		usersession.setUsername(name);
 		usersession.setPassword(password);
 		usersession.setServerInfo(getInternalAPIImpl());
 		ESUsersessionImpl esSession = usersession.getAPIImpl();
 
 		EMFStoreCommandWithException<ESException> cmd =
 			new EMFStoreCommandWithException<ESException>() {
 				@Override
 				protected void doRun() {
 					workspace.getInternalAPIImpl().getUsersessions().add(usersession);
 					try {
 						usersession.logIn();
 					} catch (AccessControlException e) {
 						setException(e);
 					} catch (ESException e) {
 						setException(e);
 					}
 				}
 			};
 
 		cmd.run(false);
 
 		if (cmd.hasException()) {
 			if (cmd.getException() instanceof AccessControlException) {
 				throw cmd.getException();
 			} else {
 				throw cmd.getException();
 			}
 		}
 
 		workspace.getInternalAPIImpl().save();
 
 		return esSession;
 	}
 
 	private List<ESRemoteProject> mapToRemoteProject(List<ProjectInfo> projectInfos) {
 		List<ESRemoteProject> remoteProjects = new ArrayList<ESRemoteProject>();
 		for (ProjectInfo projectInfo : projectInfos) {
 			ESRemoteProjectImpl wrapper = new ESRemoteProjectImpl(getInternalAPIImpl(), projectInfo);
 			remoteProjects.add(wrapper);
 		}
 		return remoteProjects;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#getRemoteProjects()
 	 */
 	public List<ESRemoteProject> getRemoteProjects() throws ESException {
 		List<ProjectInfo> projectInfos = getRemoteProjectsServerCall().setServer(getInternalAPIImpl()).execute();
 		return copy(mapToRemoteProject(projectInfos));
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.client.ESServer#getRemoteProjects(org.eclipse.emf.emfstore.client.ESUsersession)
 	 */
 	public List<ESRemoteProject> getRemoteProjects(ESUsersession usersession)
 		throws ESException {
 
 		final ServerCall<List<ProjectInfo>> serverCall = getRemoteProjectsServerCall()
 			.setUsersession(((ESUsersessionImpl) usersession).getInternalAPIImpl());
 
 		List<ProjectInfo> projectInfos = RunESCommand.WithException
 			.runWithResult(ESException.class, new Callable<List<ProjectInfo>>() {
 				public List<ProjectInfo> call() throws Exception {
 					return serverCall.execute();
 				}
 			});
 
 		return APIUtil.copy(mapToRemoteProject(projectInfos));
 	}
 
 	private ServerCall<List<ProjectInfo>> getRemoteProjectsServerCall() {
 		return new ServerCall<List<ProjectInfo>>() {
 			@Override
 			protected List<ProjectInfo> run() throws ESException {
 				return getConnectionManager().getProjectList(getSessionId());
 			}
 		};
 	}
 
 	private ServerCall<ProjectInfo> getCreateRemoteProjectServerCall(final String projectName) {
 		return new ServerCall<ProjectInfo>() {
 			@Override
 			protected ProjectInfo run() throws ESException {
 				return getConnectionManager().createEmptyProject(getSessionId(), projectName, "",
					createLogmessage(getUsersession(), projectName));
 			}
 		};
 	}
 
 	private LogMessage createLogmessage(Usersession usersession, final String projectName) {
 		final LogMessage log = VersioningFactory.eINSTANCE.createLogMessage();
 		log.setMessage("Creating project '" + projectName + "'");
 		log.setAuthor(usersession.getUsername());
 		log.setClientDate(new Date());
 		return log;
 	}
 
 	private ESUsersession validateUsersession(ESUsersession usersession) throws ESException {
 		if (usersession == null || !this.equals(usersession.getServer())) {
 			// TODO OTS custom exception
 			throw new ESException("Invalid usersession for given server.");
 		}
 		return usersession;
 	}
 
 	/**
 	 * Returns the remote project with the given ID.
 	 * 
 	 * @param projectId
 	 *            the ID of the project
 	 * @return the remote project or {@code null}, if no project with the given ID has been found
 	 * 
 	 * @throws ESException in case an error occurs while retrieving the remote project
 	 */
 	public ESRemoteProject getRemoteProject(final ESGlobalProjectId projectId) throws ESException {
 
 		for (ESRemoteProject remoteProject : getRemoteProjects()) {
 			if (remoteProject.getGlobalProjectId().equals(projectId)) {
 				return remoteProject;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Returns the remote project with the given ID.
 	 * 
 	 * @param usersession
 	 *            the {@link ESUsersession} that should be used to fetch the remote project.<br/>
 	 *            If <code>null</code>, the session manager will try to inject a session.
 	 * @param projectId
 	 *            the ID of the project
 	 * @return the remote project or {@code null}, if no project with the given ID has been found
 	 * 
 	 * @throws ESException in case an error occurs while retrieving the remote project
 	 */
 	public ESRemoteProject getRemoteProject(final ESUsersession usersession,
 		final ESGlobalProjectId projectId) throws ESException {
 
 		for (ESRemoteProject remoteProject : getRemoteProjects(usersession)) {
 			if (remoteProject.getGlobalProjectId().equals(projectId)) {
 				return remoteProject;
 			}
 		}
 
 		return null;
 	}
 }
