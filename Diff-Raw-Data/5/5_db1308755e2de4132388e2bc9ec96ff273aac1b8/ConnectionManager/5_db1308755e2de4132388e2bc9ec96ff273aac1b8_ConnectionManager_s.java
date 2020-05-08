 package edu.teco.dnd.network;
 
 import java.util.Collection;
 import java.util.UUID;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Future;
 
 import edu.teco.dnd.network.messages.Message;
 import edu.teco.dnd.network.messages.Response;
 import edu.teco.dnd.util.FutureNotifier;
 
 /**
  * A ConnectionManager is used to create and use connections between modules. It is responsible for initiating
  * connections, accepting incoming connections, route incoming messages to the appropriate handlers and send messages to
  * the right modules.
  * 
  * @author Philipp Adolf
  */
 public interface ConnectionManager {
 	/**
 	 * The application ID that is used if no application ID was specified.
 	 */
 	public static final UUID APPID_DEFAULT = UUID.fromString("00000000-0000-0000-0000-000000000000");
 
 	/**
 	 * Sends a message to the given Module.
 	 * 
 	 * @param uuid
 	 *            the UUID of the module the message should be sent to
 	 * @param message
 	 *            the message that should be sent
	 * @return a FutureNotifier that will return the Response for the message. Will return null if it is used to send a
	 *         Response
 	 */
 	public FutureNotifier<Response> sendMessage(UUID uuid, Message message);
 
 	/**
 	 * Adds an handler for a given application ID. If another handler was registered for the ID it is replaced.
 	 * 
 	 * @param <T>
 	 *            type of Message that should be handled
 	 * @param appid
 	 *            the ID of the application. Use {@link #APPID_DEFAULT} for non application messages
 	 * @param msgType
 	 *            the class of Messages that the handler will receive. Must be an exact match.
 	 * @param handler
 	 *            the handler that should receive the Messages
 	 * @param executor
 	 *            the executor that should execute {@link MessageHandler#handleMessage(Message)}. Can be null.
 	 */
 	public <T extends Message> void addHandler(UUID appid, Class<? extends T> msgType,
 			MessageHandler<? super T> handler, Executor executor);
 
 	/**
 	 * Adds an handler for a given application ID. If another handler was registered for the ID it is replaced.
 	 * 
 	 * @param <T>
 	 *            type of Message that should be handled
 	 * @param appid
 	 *            the ID of the application. Use {@link #APPID_DEFAULT} for non application messages
 	 * @param msgType
 	 *            the class of Messages that the handler will receive. Must be an exact match.
 	 * @param handler
 	 *            the handler that should receive the Messages
 	 */
 	public <T extends Message> void addHandler(UUID appid, Class<? extends T> msgType, MessageHandler<? super T> handler);
 
 	/**
 	 * Adds an handler for the {@link #APPID_DEFAULT}. If another handler was registered for the ID it is replaced.
 	 * 
 	 * @param <T>
 	 *            type of Message that should be handled
 	 * @param msgType
 	 *            the class of Messages that the handler will receive. Must be an exact match.
 	 * @param handler
 	 *            the handler that should receive the Messages
 	 * @param executor
 	 *            the executor that should execute {@link MessageHandler#handleMessage(Message)}. Can be null.
 	 */
 	public <T extends Message> void addHandler(Class<? extends T> msgType, MessageHandler<? super T> handler,
 			Executor executor);
 
 	/**
 	 * Adds an handler for the {@link #APPID_DEFAULT}. If another handler was registered for the ID it is replaced.
 	 * 
 	 * @param <T>
 	 *            type of Message that should be handled
 	 * @param appid
 	 *            the ID of the application. Use {@link #APPID_DEFAULT} for non application messages
 	 * @param msgType
 	 *            the class of Messages that the handler will receive. Must be an exact match.
 	 * @param handler
 	 *            the handler that should receive the Messages
 	 */
 	public <T extends Message> void addHandler(Class<? extends T> msgType, MessageHandler<? super T> handler);
 
 	/**
 	 * Returns a collection of connected modules.
 	 * 
 	 * @return a collection of connected modules
 	 */
 	public Collection<UUID> getConnectedModules();
 
 	/**
 	 * Adds a listener that is informed if new connections are made or old connections are lost.
 	 * 
 	 * @param listener
 	 *            the listener to add
 	 */
 	public void addConnectionListener(ConnectionListener listener);
 
 	/**
 	 * Removes a listener.
 	 * 
 	 * @param listener
 	 *            the listener to remove
 	 */
 	public void removeConnectionListener(ConnectionListener listener);
 
 	/**
 	 * Tells this TCPConnectionManager to shut down. This will close all listening sockets and all connections to other
 	 * TCPConnectionManagers.
 	 * 
 	 * @see #isShuttingDown()
 	 * @see #isShutDown()
 	 */
 	public void shutdown();
 
 	/**
 	 * Returns true if this TCPConnectionManager is shutting down or has finished shutting down. This basically returns
 	 * whether or not {@link #shutdown()} has been called.
 	 * 
 	 * @return true if this TCPConnectionManager is shutting down or has finished shutting down
 	 */
 	public boolean isShuttingDown();
 
 	/**
 	 * Returns a Future that is done when the ConnectionManager has shut down.
 	 * 
 	 * @return a Future that is done when the ConnectionManager has shut down
 	 */
 	public Future<Void> getShutdownFuture();
 }
