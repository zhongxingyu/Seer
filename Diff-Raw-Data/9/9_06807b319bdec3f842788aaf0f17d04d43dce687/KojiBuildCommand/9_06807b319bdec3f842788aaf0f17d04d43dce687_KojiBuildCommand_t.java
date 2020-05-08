 package org.fedoraproject.eclipse.packager.koji.api;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.osgi.util.NLS;
 import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
 import org.fedoraproject.eclipse.packager.NonTranslatableStrings;
 import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
 import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
 import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
 import org.fedoraproject.eclipse.packager.koji.KojiText;
 import org.fedoraproject.eclipse.packager.koji.api.errors.BuildAlreadyExistsException;
 import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
 import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;
 import org.fedoraproject.eclipse.packager.koji.api.errors.TagSourcesException;
 import org.fedoraproject.eclipse.packager.koji.api.errors.UnpushedChangesException;
 
 /**
  * Fedora Packager koji build command. Supports scratch builds
  * and regular builds.
  */
 public class KojiBuildCommand extends FedoraPackagerCommand<BuildResult> {
 
 	/**
 	 *  The unique ID of this command.
 	 */
 	public static final String ID = "KojiBuildCommand"; //$NON-NLS-1$
 	/**
 	 * The XMLRPC based client to use for Koji interaction.
 	 */
 	private IKojiHubClient kojiClient;
 	/**
 	 * Set to true if scratch build should be pushed instead of a regular build.
 	 */
 	private boolean scratchBuild = false;
 	/**
 	 * The URL into the VCS repo which should be used for the build. 
 	 */
 	private String scmUrl;
 	/**
 	 * The distribution tag (e.g. dist-rawhide)
 	 */
 	private String buildTarget;
 	/**
 	 * The name-version-release token to push a build for
 	 */
 	private String nvr;
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand#checkConfiguration()
 	 */
 	@Override
 	protected void checkConfiguration() throws CommandMisconfiguredException {
 		// require a client
 		if (kojiClient == null) {
 			throw new CommandMisconfiguredException(NLS.bind(
 					KojiText.KojiBuildCommand_configErrorNoClient,
 					NonTranslatableStrings.getBuildToolName(this.projectRoot)));
 		}
 		// we also require scmURL to be set
 		if (scmUrl == null) {
 			throw new CommandMisconfiguredException(KojiText.KojiBuildCommand_configErrorNoScmURL);
 		}
 		// distribution can't be null
 		if (buildTarget == null) {
			throw new CommandMisconfiguredException(KojiText.KojiBuildCommand_configErrorNoBuildTarget);
 		}
 		// nvr can't be null
 		if (nvr == null) {
 			throw new CommandMisconfiguredException(KojiText.KojiBuildCommand_configErrorNoNVR);
 		}
 	}
 	
 	/**
 	 * Sets the XMLRPC based client, which will be used for Koji interaction.
 	 * 
 	 * @param client
 	 * @return This instance.
 	 */
 	public KojiBuildCommand setKojiClient(IKojiHubClient client) {
 		this.kojiClient = client;
 		return this;
 	}
 	
 	/**
 	 * Set this to {@code true} if a scratch build should be pushed instead
 	 * of a regular build.
 	 * 
 	 * @param newValue
 	 * @return This instance.
 	 */
 	public KojiBuildCommand isScratchBuild(boolean newValue) {
 		this.scratchBuild = newValue;
 		return this;
 	}
 	
 	/**
 	 * Sets the URL into the source control management system, in order to
 	 * be able to determine which tag/revision to build.
 	 * 
 	 * @param scmUrl The URL into the VCS repository.
 	 * @return This instance.
 	 */
 	public KojiBuildCommand scmUrl(String scmUrl) {
 		this.scmUrl = scmUrl;
 		return this;
 	}
 	
 	/**
 	 * Sets the build target for which to push the build for.
 	 * 
 	 * @param buildTarget The target to build for.
 	 * @return This instance.
 	 */
 	public KojiBuildCommand buildTarget(String buildTarget) {
 		this.buildTarget = buildTarget;
 		return this;
 	}
 	
 	/**
 	 * Sets the name-version-release token for which a build should be pushed.
 	 * 
 	 * @param nvr
 	 * @return This instance.
 	 */
 	public KojiBuildCommand nvr(String nvr) {
 		this.nvr = nvr;
 		return this;
 	}
 
 	/**
 	 * Implementation of the {@code KojiBuildCommand}.
 	 * 
 	 * @param monitor
 	 *            The main progress monitor. Each other task is executed as a
 	 *            subtask.
 	 * @throws BuildAlreadyExistsException
 	 *             If a build which would have otherwise be pushed already
 	 *             existed in Koji.
 	 * @throws CommandMisconfiguredException
 	 *             If the command was not properly configured when it was
 	 *             called.
 	 * @throws UnpushedChangesException
 	 *             If the download of some source failed.
 	 * @throws TagSourcesException
 	 *             If tagging of sources failed.
 	 * @throws CommandListenerException
 	 *             If some listener detected a problem.
 	 * @throws KojiHubClientLoginException
 	 *             If some error occured during login.
 	 * @throws KojiHubClientException
 	 *             If some other error occured while pushing a build.
 	 * @return The result of this command.
 	 */
 	@Override
 	public BuildResult call(IProgressMonitor monitor)
 			throws CommandMisconfiguredException, BuildAlreadyExistsException,
 			UnpushedChangesException, TagSourcesException,
 			CommandListenerException, KojiHubClientLoginException,
 			KojiHubClientException {
 		try {
 			callPreExecListeners();
 		} catch (CommandListenerException e) {
 			if (e.getCause() instanceof CommandMisconfiguredException) {
 				// explicitly throw the specific exception
 				throw (CommandMisconfiguredException)e.getCause();
 			} else if (e.getCause() instanceof TagSourcesException) {
 				throw (TagSourcesException)e.getCause();
 			} else if (e.getCause() instanceof UnpushedChangesException) {
 				throw (UnpushedChangesException)e.getCause();
 			}
 			throw e;
 		}
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		
 		// main monitor worked for 20
 		BuildResult result = new BuildResult();
 		monitor.subTask(NLS.bind(KojiText.KojiBuildCommand_kojiLogInTask,
 				NonTranslatableStrings.getBuildToolName(this.projectRoot)));
 		// login 
 		this.kojiClient.login();
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		
 		monitor.worked(30);
 		monitor.subTask(KojiText.KojiBuildCommand_sendBuildCmd);
 		FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 		if (this.scratchBuild) {
 			logger.logInfo(KojiText.KojiBuildCommand_scratchBuildLogMsg);
 		} else {
 			logger.logInfo(KojiText.KojiBuildCommand_buildLogMsg);
 		}
 		// attempt to push build
 		int taskId = this.kojiClient.build(buildTarget, scmUrl.toString(), nvr, scratchBuild);
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 		result.setTaskId(taskId);
 		monitor.worked(80);
 		monitor.subTask(KojiText.KojiBuildCommand_kojiLogoutTask);
 		this.kojiClient.logout();
 		monitor.worked(90);
 		callPostExecListeners();
 		setCallable(false); // reuse of instance's call() not allowed
 		result.setSuccessful();
 		monitor.done();
 		return result;
 	}
 
 }
