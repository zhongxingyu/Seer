 package ljas.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.ArrayList;
 import java.util.List;
 
 import ljas.commons.application.Application;
 import ljas.commons.application.ApplicationAnalyzer;
 import ljas.commons.application.LoginParameters;
 import ljas.commons.application.annotations.LJASApplication;
 import ljas.commons.exceptions.ApplicationException;
 import ljas.commons.exceptions.ConnectionRefusedException;
 import ljas.commons.session.Session;
 import ljas.commons.session.SessionHolder;
 import ljas.commons.state.SystemAvailabilityState;
 import ljas.commons.state.login.LoginRefusedMessage;
 import ljas.commons.tasking.environment.TaskSystem;
 import ljas.commons.tasking.environment.TaskSystemImpl;
 import ljas.commons.threading.ThreadSystem;
 import ljas.server.configuration.ServerConfiguration;
 import ljas.server.login.ClientConnectionListener;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 public final class Server implements SessionHolder {
 	public static final String PROJECT_NAME = "LJAS";
 	public static final String PROJECT_HOMEPAGE = "http://github.com/vl0w/Lightweight-Java-Application-Server";
 	public static final String SERVER_VERSION = "1.2.0-SNAPSHOT";
 
 	private List<Session> sessions;
 	private SystemAvailabilityState state;
 	private ServerSocket serverSocket;
 	private Application application;
 	private ServerConfiguration configuration;
 	private TaskSystem taskSystem;
 	private ThreadSystem threadSystem;
 
 	public Server(Application application, ServerConfiguration configuration)
 			throws IOException {
 		this.configuration = configuration;
 		this.application = application;
 		this.threadSystem = new ThreadSystem(Server.class.getSimpleName(),
 				configuration.getMaxTaskWorkerCount());
 		this.taskSystem = new TaskSystemImpl(threadSystem, application);
 		this.state = SystemAvailabilityState.OFFLINE;
 		this.sessions = new ArrayList<>();
 
 		// Logging
 		DOMConfigurator.configure(getConfiguration().getLog4JFilePath());
 	}
 
 	public ServerSocket getServerSocket() {
 		return serverSocket;
 	}
 
 	public boolean isOnline() {
 		return state == SystemAvailabilityState.ONLINE;
 	}
 
 	public Logger getLogger() {
 		return Logger.getLogger(getClass());
 	}
 
 	public Application getApplication() {
 		return application;
 	}
 
 	public ServerConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	public ThreadSystem getThreadSystem() {
 		return threadSystem;
 	}
 
 	public TaskSystem getTaskSystem() {
 		return taskSystem;
 	}
 
 	public SystemAvailabilityState getState() {
 		return state;
 	}
 
 	public void setState(SystemAvailabilityState state) {
 		this.state = state;
 	}
 
 	@Override
 	public List<Session> getSessions() {
 		return sessions;
 	}
 
 	@Override
 	public void addSession(Session session) {
 		sessions.add(session);
 	}
 
 	public void startup() throws ApplicationException, IOException {
 		if (isOnline()) {
 			shutdown();
 		}
 
 		LJASApplication applicationAnnotation = ApplicationAnalyzer
 				.getApplicationAnnotation(application.getClass());
 		if (applicationAnnotation == null) {
 			throw new ApplicationException("Missing annotation '"
 					+ LJASApplication.class.getSimpleName()
 					+ "' on the Server application.");
 		}
 
 		state = SystemAvailabilityState.STARTUP;
 		logServerInfo();
 
 		getLogger().debug("Getting internet connection, starting socket");
 		serverSocket = new ServerSocket(getConfiguration().getPort());
 
 		createConnectionListeners();
 
 		getLogger().info(this + " has been started");
 		state = SystemAvailabilityState.ONLINE;
 	}
 
 	public void shutdown() {
 		try {
 			getLogger().debug("Closing socket");
 			getServerSocket().close();
 
 			getLogger().info("Closing client sessions");
 			List<Session> sessionsToClose = new ArrayList<>(sessions);
 			for (Session session : sessionsToClose) {
 				session.disconnect();
 			}
 
 			getLogger().debug(
 					"Deactivating " + threadSystem.getClass().getSimpleName());
 			threadSystem.killAll();
 
 			getLogger().info(this + " is offline");
 			state = SystemAvailabilityState.OFFLINE;
 		} catch (Exception e) {
 			getLogger().error(e);
 		}
 	}
 
 	public void checkClient(LoginParameters parameters)
 			throws ConnectionRefusedException {
 
 		// Check server state
 		if (!isOnline()) {
 			throw new ConnectionRefusedException(
 					LoginRefusedMessage.ILLEGAL_STATE);
 		}
 
 		// Check server full
 		if (getSessions().size() >= getConfiguration().getMaximumClients()) {
 			throw new ConnectionRefusedException(
 					LoginRefusedMessage.SERVER_FULL);
 		}
 
 		// Check application
 		if (!ApplicationAnalyzer.areApplicationsEqual(
 				parameters.getClientApplicationClass(), application.getClass())) {
 			throw new ConnectionRefusedException(
 					LoginRefusedMessage.INVALID_APPLICATION);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return PROJECT_NAME + "-server";
 	}
 
 	private void createConnectionListeners() {
 		for (int i = 0; i < 5; i++) {
 
 			ClientConnectionListener connectionListener = new ClientConnectionListener(
 					this);
 			threadSystem.getThreadFactory().createBackgroundThread(
 					connectionListener);
 		}
 	}
 
 	private void logServerInfo() {
 		String applicationName = ApplicationAnalyzer
 				.getApplicationName(application.getClass());
 		String applicationVersion = ApplicationAnalyzer
 				.getApplicationVersion(application.getClass());
 
 		getLogger().info(
 				"Starting " + this + " (v" + SERVER_VERSION
 						+ ") with application " + applicationName + " ("
 						+ applicationVersion + ")");
 
 		getLogger().info(
 				"See \"" + PROJECT_HOMEPAGE + "\" for more information");
 		getLogger().info(
 				"This server is hosted by " + getConfiguration().getHostName());
 
 		getLogger().debug("Configuration: " + getConfiguration().toString());
 	}
 }
