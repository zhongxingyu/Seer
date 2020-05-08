 package ljas.commons.client;
 
 import ljas.commons.application.Application;
 import ljas.commons.application.LoginParameters;
 import ljas.commons.application.client.ClientApplication;
 import ljas.commons.application.client.ClientApplicationException;
 import ljas.commons.exceptions.ConnectionRefusedException;
 import ljas.commons.exceptions.RequestTimedOutException;
 import ljas.commons.exceptions.SessionException;
 import ljas.commons.session.Session;
 import ljas.commons.session.SessionFactory;
 import ljas.commons.state.RuntimeEnvironmentState;
 import ljas.commons.state.login.LoginAcceptedMessage;
 import ljas.commons.state.login.LoginMessage;
 import ljas.commons.state.login.LoginRefusedMessage;
 import ljas.commons.tasking.Task;
 import ljas.commons.tasking.environment.TaskSystem;
 import ljas.commons.tasking.environment.TaskSystemImpl;
 import ljas.commons.tasking.environment.TaskSystemSessionObserver;
 import ljas.commons.tasking.monitoring.TaskMonitor;
 import ljas.commons.tasking.observation.NullTaskObserver;
 import ljas.commons.threading.ThreadBlocker;
 import ljas.commons.threading.ThreadSystem;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 public class ClientImpl implements Client {
 	private Session session;
 	private TaskSystem taskSystem;
 	private ThreadSystem threadSystem;
 
 	private RuntimeEnvironmentState state;
 	private final ClientApplication application;
 
 	@Override
 	public void setState(RuntimeEnvironmentState state) {
 		this.state = state;
 	}
 
 	@Override
 	public RuntimeEnvironmentState getState() {
 		return state;
 	}
 
 	@Override
 	public TaskSystem getTaskSystem() {
 		return taskSystem;
 	}
 
 	public ClientImpl(ClientApplication application) {
 		this.state = RuntimeEnvironmentState.OFFLINE;
 		TaskMonitor taskMonitor = new TaskMonitor();
 		this.threadSystem = new ThreadSystem(taskMonitor, 1);
 		this.taskSystem = new TaskSystemImpl(threadSystem, taskMonitor);
 
 		this.application = application;
 		this.application.setClient(this);
 
		DOMConfigurator.configure("./log4j.xml");
 	}
 
 	@Override
 	public void connect(String ip, int port, LoginParameters parameters)
 			throws ConnectionRefusedException, SessionException {
 		if (isOnline()) {
 			disconnect();
 		}
 		setState(RuntimeEnvironmentState.STARTUP);
 
 		try {
 			session = SessionFactory.prepareSession(this, threadSystem);
 
 			ClientLoginHandler loginHandler = new ClientLoginHandler(ip, port,
 					session, parameters);
 
 			LoginMessage loginMessage = loginHandler.block();
 
 			if (loginMessage instanceof LoginAcceptedMessage) {
 				setState(RuntimeEnvironmentState.ONLINE);
 				TaskSystemSessionObserver observer = new TaskSystemSessionObserver(
 						this);
 				session.setObserver(observer);
 
 			} else if (loginMessage instanceof LoginRefusedMessage) {
 				setState(RuntimeEnvironmentState.OFFLINE);
 				LoginRefusedMessage message = (LoginRefusedMessage) loginMessage;
 				throw new ConnectionRefusedException(message);
 			}
 
 		} catch (Throwable t) {
 			setState(RuntimeEnvironmentState.OFFLINE);
 			if (ConnectionRefusedException.class.getName().equals(
 					t.getClass().getName())) {
 				throw (ConnectionRefusedException) t;
 			} else {
 				throw new SessionException(t);
 			}
 		}
 	}
 
 	private void sendTask(Task task) throws ClientApplicationException {
 		if (!isOnline()) {
 			throw new ClientApplicationException("Client is not online");
 		}
 
 		taskSystem.scheduleTask(task);
 	}
 
 	@Override
 	public boolean isOnline() {
 		return getState() == RuntimeEnvironmentState.ONLINE && session != null
 				&& session.isConnected();
 	}
 
 	@Override
 	public void disconnect() throws SessionException {
 		if (isOnline()) {
 			session.disconnect();
 			threadSystem.killAll();
 			setState(RuntimeEnvironmentState.OFFLINE);
 		}
 	}
 
 	public Logger getLogger() {
 		return Logger.getLogger(Client.class);
 	}
 
 	/**
 	 * Runs a task, waits for it and throws an exception when it has failed
 	 * 
 	 * @param task
 	 *            The task to execute
 	 * @return The executed task
 	 * @throws ClientApplicationException
 	 *             When the task failed or could not be sended
 	 */
 	@Override
 	public Task runTaskSync(Task task) throws ClientApplicationException {
 		final ThreadBlocker<Task> threadBlocker = new ThreadBlocker<>(
 				Client.REQUEST_TIMEOUT_MS);
 
 		task.addObserver(new NullTaskObserver() {
 
 			@Override
 			public void notifyExecuted(Task task) {
 				threadBlocker.release(task);
 			}
 
 			@Override
 			public void notifyFail(Task task) {
 				ClientApplicationException exception = new ClientApplicationException(
 						task.getResultMessage());
 				threadBlocker.release(exception);
 			}
 		});
 
 		sendTask(task);
 
 		try {
 			return threadBlocker.block();
 		} catch (RequestTimedOutException e) {
 			throw new ClientApplicationException("Request timed out");
 		} catch (Throwable e) {
 			throw new ClientApplicationException(
 					"Unknown exception while executing request");
 		}
 	}
 
 	@Override
 	public void runTaskAsync(Task task) {
 		try {
 			sendTask(task);
 		} catch (ClientApplicationException e) {
 			getLogger().error(e);
 		}
 	}
 
 	@Override
 	public Session getServerSession() {
 		return session;
 	}
 
 	@Override
 	public Application getApplication() {
 		return application;
 	}
 }
