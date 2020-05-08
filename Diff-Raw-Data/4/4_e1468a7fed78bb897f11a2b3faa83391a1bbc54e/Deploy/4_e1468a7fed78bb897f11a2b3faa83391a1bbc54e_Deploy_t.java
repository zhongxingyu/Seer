 package edu.teco.dnd.deploy;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.blocks.ValueDestination;
 import edu.teco.dnd.deploy.Distribution.BlockTarget;
 import edu.teco.dnd.graphiti.model.FunctionBlockModel;
 import edu.teco.dnd.graphiti.model.InputModel;
 import edu.teco.dnd.graphiti.model.OptionModel;
 import edu.teco.dnd.graphiti.model.OutputModel;
 import edu.teco.dnd.module.ModuleInfo;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationAck;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationMessage;
 import edu.teco.dnd.module.messages.joinStartApp.StartApplicationAck;
 import edu.teco.dnd.module.messages.joinStartApp.StartApplicationMessage;
 import edu.teco.dnd.module.messages.killApp.KillAppMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockAck;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockNak;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassAck;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassMessage;
 import edu.teco.dnd.network.ConnectionManager;
 import edu.teco.dnd.network.messages.Response;
 import edu.teco.dnd.util.ClassFile;
 import edu.teco.dnd.util.DefaultFutureNotifier;
 import edu.teco.dnd.util.Dependencies;
 import edu.teco.dnd.util.FileCache;
 import edu.teco.dnd.util.FinishedFutureNotifier;
 import edu.teco.dnd.util.FutureListener;
 import edu.teco.dnd.util.FutureNotifier;
 import edu.teco.dnd.util.JoinedFutureNotifier;
 import edu.teco.dnd.util.MapUtil;
 
 /**
  * This class provides functionality to deploy applications to modules. The process is this:
  * 
  * <ol>
  * <li>Send {@link JoinApplicationMessage}s to all modules involved</li>
  * <li>Once a module has responded positively to a JoinApplicationMessage, send all classes needed on the ModuleInfo via
  * {@link LoadClassMessage}</li>
  * <li>When classes have been successfully loaded, all {@link FunctionBlock}s are sent to the ModuleInfo via
  * {@link BlockMessage}s</li>
  * <li>When all Blocks have been sent to the corresponding Modules, a {@link StartApplicationMessage} is sent to every
  * ModuleInfo</li>
  * </ol>
  * 
  * If any of the steps fails the deployment is aborted and a {@link KillAppMessage} is sent to all Modules.
  * 
  * @author Philipp Adolf
  */
 public class Deploy {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(Deploy.class);
 
 	/**
 	 * All listeners that want to be informed about the deployment process.
 	 */
 	private final Set<DeployListener> listeners = new HashSet<DeployListener>();
 
 	/**
 	 * Used to synchronize access to {@link #listeners}.
 	 */
 	private final ReadWriteLock listenerLock = new ReentrantReadWriteLock();
 
 	/**
 	 * The ConnectionManager that will be used to sent messages.
 	 */
 	private final ConnectionManager connectionManager;
 
 	/**
 	 * The distribution that should be deployed.
 	 */
 	private final Map<FunctionBlockModel, BlockTarget> distribution;
 
 	/**
 	 * The name of the application.
 	 */
 	private final String appName;
 
 	/**
 	 * The UUID of the application.
 	 */
 	private final UUID appId;
 
 	/**
 	 * Used to resolve dependencies.
 	 */
 	private final Dependencies dependencies;
 
 	/**
 	 * Stores the ClassFiles each ModuleInfo needs. Filled by {@link #deploy()}.
 	 */
 	private Map<ModuleInfo, Set<ClassFile>> neededFilesPerModule;
 
 	/**
 	 * Used to cache the contents of the class files.
 	 */
 	private final FileCache fileCache = new FileCache();
 
 	/**
 	 * This future represents the state of the deployment process. It is set to done once all Modules have started the
 	 * application or an error occurs.
 	 */
 	private final DeployFutureNotifier deployFutureNotifier = new DeployFutureNotifier();
 
 	/**
 	 * The number of Modules that have not received all Blocks yet. This does <em>not</em> including sending/receiving a
 	 * {@link StartApplicationMessage}.
 	 */
 	private final AtomicInteger unfinishedModules = new AtomicInteger();
 
 	/**
 	 * Stores the FunctionBlocks that are to be sent to each ModuleInfo.
 	 */
 	private final Map<ModuleInfo, Set<FunctionBlockModel>> moduleMap;
 
 	/**
 	 * Used to make sure that the deployment process is only started once.
 	 */
 	private final AtomicBoolean hasBeenStarted = new AtomicBoolean(false);
 
 	/**
 	 * Creates a new Deploy object.
 	 * 
 	 * @param connectionManager
 	 *            the ConnectionManager to use
 	 * @param distribution
 	 *            the Distribution that should be deployed
 	 * @param name
 	 *            the name of the Application
 	 * @param dependencies
 	 *            used to resolve Dependencies
 	 * @param appId
 	 *            the UUID of the Application
 	 */
 	public Deploy(final ConnectionManager connectionManager, final Map<FunctionBlockModel, BlockTarget> distribution,
 			final String name, final Dependencies dependencies, final UUID appId) {
 		LOGGER.entry(connectionManager, distribution, name, dependencies, appId);
 		this.connectionManager = connectionManager;
 		this.distribution = distribution;
 		this.appName = name;
 		this.dependencies = dependencies;
 		this.appId = appId;
 		this.moduleMap = MapUtil.invertMap(getModuleMapping());
 		LOGGER.exit();
 	}
 
 	/**
 	 * Creates a new Deploy object with a random UUID.
 	 * 
 	 * @param connectionManager
 	 *            the ConnectionManager to use
 	 * @param distribution
 	 *            the Distribution that should be deployed
 	 * @param name
 	 *            the name of the Application
 	 * @param dependencies
 	 *            used to resolve Dependencies
 	 */
 	public Deploy(final ConnectionManager connectionManager, final Map<FunctionBlockModel, BlockTarget> distribution,
 			final String name, final Dependencies dependencies) {
 		this(connectionManager, distribution, name, dependencies, UUID.randomUUID());
 	}
 
 	/**
 	 * Adds a listener to this object. The listener will be informed about the deployment process.
 	 * 
 	 * @param listener
 	 *            the listener to add
 	 */
 	public void addListener(final DeployListener listener) {
 		LOGGER.entry(listener);
 		listenerLock.writeLock().lock();
 		try {
 			listeners.add(listener);
 		} finally {
 			listenerLock.writeLock().unlock();
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Removes a listener from this object.
 	 * 
 	 * @param listener
 	 *            the listener to remove
 	 */
 	public void removeListener(final DeployListener listener) {
 		LOGGER.entry(listener);
 		listenerLock.writeLock().lock();
 		try {
 			listeners.add(listener);
 		} finally {
 			listenerLock.writeLock().unlock();
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Returns the name of the Application.
 	 * 
 	 * @return the name of the Application
 	 */
 	public String getName() {
 		return this.appName;
 	}
 
 	/**
 	 * Returns the UUID of the Application.
 	 * 
 	 * @return the UUID of the Application
 	 */
 	public UUID getApplicationUUID() {
 		return this.appId;
 	}
 
 	/**
 	 * Returns the number of Modules that will be used by the Application.
 	 * 
 	 * @return the number of Modules that will be used by the Application
 	 */
 	public int getModuleCount() {
 		return this.moduleMap.size();
 	}
 
 	/**
 	 * Returns all Modules used by the application.
 	 * 
 	 * @return all Modules used by the application
 	 */
 	public Set<ModuleInfo> getModules() {
 		return Collections.unmodifiableSet(this.moduleMap.keySet());
 	}
 
 	/**
 	 * Returns the UUIDs of the Modules used by the application.
 	 * 
 	 * @return the UUIDs of the Modules used by the application
 	 */
 	public Collection<UUID> getModuleUUIDs() {
 		final Collection<UUID> uuids = new ArrayList<UUID>(moduleMap.size());
 		for (final ModuleInfo module : moduleMap.keySet()) {
 			uuids.add(module.getUUID());
 		}
 		return uuids;
 	}
 
 	/**
 	 * Returns a FutureNotifier that represents the state of the deployment process: When the FutureNotifier is finished
 	 * the deployment process is finished and the {@link FutureNotifier#isSuccess()} method indicates whether or not the
 	 * deployment was successful.
 	 * 
 	 * @return a FutureNotifier representing the state of the deployment process
 	 */
 	public FutureNotifier<Void> getDeployFutureNotifier() {
 		return deployFutureNotifier;
 	}
 
 	/**
 	 * Starts the deployment process. If it is called multiple times only the first time will do anything.
 	 * 
 	 * @return true if this method was called for the first time, false if called again later
 	 */
 	public boolean deploy() {
 		LOGGER.entry();
 
 		if (!hasBeenStarted.compareAndSet(false, true)) {
 			LOGGER.warn("tried to start deployment of {} twice");
 			LOGGER.exit(false);
 			return false;
 		}
 
 		unfinishedModules.set(moduleMap.size());
 
 		final Map<FunctionBlockModel, Set<ClassFile>> neededFiles = getNeededFiles();
 		neededFilesPerModule = MapUtil.transitiveMapSet(moduleMap, neededFiles);
 
 		for (final ModuleInfo module : moduleMap.keySet()) {
 			sendJoin(module.getUUID()).addListener(new FutureListener<FutureNotifier<Response>>() {
 				@Override
 				public void operationComplete(final FutureNotifier<Response> future) {
 					handleJoinFinished(future, module);
 				}
 			});
 		}
 		LOGGER.exit(true);
 		return true;
 	}
 
 	/**
 	 * Creates a mapping from FunctionBlock to ModuleInfo using {@link #distribution}.
 	 * 
 	 * @return a mapping from FunctionBlock to corresponding ModuleInfo
 	 */
 	private Map<FunctionBlockModel, ModuleInfo> getModuleMapping() {
 		final Map<FunctionBlockModel, ModuleInfo> moduleMapping = new HashMap<FunctionBlockModel, ModuleInfo>();
 		for (final Entry<FunctionBlockModel, BlockTarget> entry : distribution.entrySet()) {
 			moduleMapping.put(entry.getKey(), entry.getValue().getModule());
 		}
 		return moduleMapping;
 	}
 
 	/**
 	 * Returns a mapping that contains all ClassFiles needed for each block in {@link #distribution}.
 	 * 
 	 * @return a mapping that contains all ClassFiles needed for each block
 	 */
 	private Map<FunctionBlockModel, Set<ClassFile>> getNeededFiles() {
 		final Map<FunctionBlockModel, Set<ClassFile>> neededFiles = new HashMap<FunctionBlockModel, Set<ClassFile>>();
 		for (final FunctionBlockModel block : distribution.keySet()) {
 			neededFiles.put(block, new HashSet<ClassFile>(dependencies.getDependencies(block.getBlockClass())));
 		}
 		return neededFiles;
 	}
 
 	/**
 	 * Sends a {@link JoinApplicationMessage} to the ModuleInfo with the given UUID.
 	 * 
 	 * @param moduleUUID
 	 *            the UUID of the ModuleInfo the message should be sent to
 	 * @return a FutureNotifier that will return the Response of the ModuleInfo
 	 */
 	private FutureNotifier<Response> sendJoin(final UUID moduleUUID) {
 		LOGGER.entry(moduleUUID);
 		final FutureNotifier<Response> futureNotifier =
 				connectionManager.sendMessage(moduleUUID, new JoinApplicationMessage(appName, appId));
 		LOGGER.exit(futureNotifier);
 		return futureNotifier;
 	}
 
 	/**
 	 * This method is called when a ModuleInfo's Response to a {@link JoinApplicationMessage} is received. If a positive
 	 * Response is received {@link #sendClasses(ModuleInfo)} is called, otherwise {@link #deployFutureNotifier} is marked as
 	 * failed.
 	 * 
 	 * @param future
 	 *            the FutureNotifier for the Response
 	 * @param module
 	 *            the ModuleInfo the Response is from
 	 */
 	private void handleJoinFinished(final FutureNotifier<Response> future, final ModuleInfo module) {
 		LOGGER.entry(future, module);
 		if (deployFutureNotifier.isDone() && !deployFutureNotifier.isSuccess()) {
 			LOGGER.debug("deployFutureNotifier failed, aborting");
 			return;
 		}
 
 		if (future.isSuccess()) {
 			if (!(future.getNow() instanceof JoinApplicationAck)) {
 				deployFutureNotifier.setFailure0(null);
 				LOGGER.exit();
 				return;
 			}
 
 			informJoined(module.getUUID());
 
 			LOGGER.debug("sending classes to {}", module);
 			sendClasses(module).addListener(new FutureListener<FutureNotifier<Collection<Response>>>() {
 				@Override
 				public void operationComplete(final FutureNotifier<Collection<Response>> future) {
 					handleClassSendingFinished(future, module);
 				}
 			});
 		} else {
 			deployFutureNotifier.setFailure0(future.cause());
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Calls {@link DeployListener#moduleJoined(UUID)} on all listeners.
 	 * 
 	 * @param moduleUUID
 	 *            the UUID of the ModuleInfo that joined
 	 */
 	private void informJoined(final UUID moduleUUID) {
 		listenerLock.readLock().lock();
 		try {
 			for (final DeployListener listener : listeners) {
 				listener.moduleJoined(appId, moduleUUID);
 			}
 		} finally {
 			listenerLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Sends all classes needed by the given ModuleInfo to the ModuleInfo.
 	 * 
 	 * @param module
 	 *            the that should be handled
 	 * @return a FutureNotifier that will return a Collection of the Responses to all {@link LoadClassMessage}s
 	 */
 	private FutureNotifier<Collection<Response>> sendClasses(final ModuleInfo module) {
 		final UUID moduleUUID = module.getUUID();
 		final Collection<FutureNotifier<? extends Response>> futureNotifiers =
 				new ArrayList<FutureNotifier<? extends Response>>();
 		for (final ClassFile classFile : neededFilesPerModule.get(module)) {
 			futureNotifiers.add(sendClass(moduleUUID, classFile));
 		}
 		return new JoinedFutureNotifier<Response>(futureNotifiers);
 	}
 
 	/**
 	 * Sends a single class to a ModuleInfo. Used {@link #fileCache} to cache the class file contents.
 	 * 
 	 * @param moduleUUID
 	 *            the UUID of the ModuleInfo the class should be sent to
 	 * @param classFile
 	 *            the class to send
 	 * @return a FutureNotifier that will return the Response of the ModuleInfo
 	 */
 	private FutureNotifier<Response> sendClass(final UUID moduleUUID, final ClassFile classFile) {
 		byte[] classData;
 		try {
 			classData = fileCache.getFileData(classFile.getFile());
 		} catch (final IOException e) {
 			return new FinishedFutureNotifier<Response>(e);
 		}
 		LOGGER.trace("sending class {} to module {}", classFile, moduleUUID);
 		return connectionManager.sendMessage(moduleUUID, new LoadClassMessage(classFile.getClassName(), classData,
 				appId));
 	}
 
 	/**
 	 * This method is called when a FutureNotifier for Responses to {@link LoadClassMessage}s has finished. If a
 	 * positive Reponse is received {@link #sendBlocks(ModuleInfo)} is called, otherwise {@link #deployFutureNotifier} is
 	 * marked as failed.
 	 * 
 	 * @param future
 	 *            the FutureNotifier that returns the Responses
 	 * @param module
 	 *            the ModuleInfo the Responses are from
 	 */
 	private void handleClassSendingFinished(final FutureNotifier<Collection<Response>> future, final ModuleInfo module) {
 		LOGGER.entry(future, module);
 		if (deployFutureNotifier.isDone() && !deployFutureNotifier.isSuccess()) {
 			LOGGER.debug("deployFutureNotifier failed, aborting");
 			return;
 		}
 
 		if (future.isSuccess()) {
 			for (final Response response : future.getNow()) {
 				if (!(response instanceof LoadClassAck)) {
 					deployFutureNotifier.setFailure0(null);
 					LOGGER.exit();
 					return;
 				}
 			}
 
 			informClassesLoaded(module.getUUID());
 
 			LOGGER.debug("sending blocks to {}", module);
 			sendBlocks(module).addListener(new FutureListener<FutureNotifier<Collection<Response>>>() {
 				@Override
 				public void operationComplete(final FutureNotifier<Collection<Response>> future) {
 					handleBlockSendingFinished(future, module);
 				}
 			});
 		} else {
 			deployFutureNotifier.setFailure0(future.cause());
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Calls {@link DeployListener#moduleLoadedClasses(UUID)} on all listeners.
 	 * 
 	 * @param moduleUUID
 	 *            the UUID of the ModuleInfo that loaded the classes
 	 */
 	private void informClassesLoaded(final UUID moduleUUID) {
 		listenerLock.readLock().lock();
 		try {
 			for (final DeployListener listener : listeners) {
 				listener.moduleLoadedClasses(appId, moduleUUID);
 			}
 		} finally {
 			listenerLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Sends all Blocks that are assigned to the given ModuleInfo to the ModuleInfo.
 	 * 
 	 * @param module
 	 *            the ModuleInfo to handle
 	 * @return a FutureNotifier returning a Collection of all Responses to the {@link BlockMessage}s
 	 */
 	private FutureNotifier<Collection<Response>> sendBlocks(final ModuleInfo module) {
 		final UUID moduleUUID = module.getUUID();
 		final Collection<FutureNotifier<? extends Response>> futureNotifiers =
 				new ArrayList<FutureNotifier<? extends Response>>();
 		for (final FunctionBlockModel block : moduleMap.get(module)) {
 			LOGGER.debug("sending block {}", block);
 			futureNotifiers.add(sendBlock(moduleUUID, block));
 		}
 		return new JoinedFutureNotifier<Response>(futureNotifiers);
 	}
 
 	/**
 	 * Sends a single {@link FunctionBlock} to the ModuleInfo with the given UUID.
 	 * 
 	 * @param moduleUUID
 	 *            the UUID of the ModuleInfo
 	 * @param block
 	 *            the FunctionBlock to send
 	 * @return a FutureNotifier that will return the Response of the ModuleInfo
 	 */
 	private FutureNotifier<Response> sendBlock(final UUID moduleUUID, final FunctionBlockModel block) {
 		final Map<String, String> options = new HashMap<String, String>();
 		for (final OptionModel option : block.getOptions()) {
 			options.put(option.getName(), option.getValue());
 		}
 		final Map<String, Collection<ValueDestination>> outputs = new HashMap<String, Collection<ValueDestination>>();
 		for (final OutputModel output : block.getOutputs()) {
 			final Collection<ValueDestination> destinations = new ArrayList<ValueDestination>();
 			for (final InputModel input : output.getInputs()) {
 				destinations.add(new ValueDestination(input.getFunctionBlock().getID(), input.getName()));
 			}
 			if (!destinations.isEmpty()) {
 				outputs.put(output.getName(), destinations);
 			}
 		}
 		final BlockMessage blockMsg = new BlockMessage(appId, block.getBlockClass(), block.getBlockName(), block.getID(), options, outputs, distribution.get(block).getBlockTypeHolder().getIdNumber());
 		return connectionManager.sendMessage(moduleUUID, blockMsg);
 	}
 
 	/**
 	 * This method is called when all {@link FunctionBlock} that are assigned to a ModuleInfo have been sent. If a positive
 	 * Response was received {@link #unfinishedModules} is decremented. If it is zero afterwards
 	 * {@link #sendStartApplication()} is called. If a negative Response is received {@link #deployFutureNotifier} is
 	 * marked as failed.
 	 * 
 	 * @param future
 	 *            the FutureNotifier returning the Responses
 	 * @param module
 	 *            the ModuleInfo the Responses are from
 	 */
 	private void handleBlockSendingFinished(final FutureNotifier<Collection<Response>> future, final ModuleInfo module) {
 		LOGGER.entry(future, module);
 		if (deployFutureNotifier.isDone() && !deployFutureNotifier.isSuccess()) {
 			LOGGER.exit();
 			return;
 		}
 
 		if (future.isSuccess()) {
 			for (final Response response : future.getNow()) {
 				if (!(response instanceof BlockAck)) {
 					if (response instanceof BlockNak) {
 						deployFutureNotifier.setFailure0(new BlockNotAcceptedException(((BlockNak) response).getErrorMessage()));
 					} else {
 						deployFutureNotifier.setFailure0(null);
 					}
 					LOGGER.exit();
 					return;
 				}
 			}
 
 			informBlocksLoaded(module.getUUID());
 
 			LOGGER.debug("module {} finished successfully", module);
 			if (unfinishedModules.decrementAndGet() <= 0) {
 				LOGGER.debug("last module finished, sending StartApplication");
 				sendStartApplication().addListener(new FutureListener<FutureNotifier<Collection<Response>>>() {
 					@Override
 					public void operationComplete(FutureNotifier<Collection<Response>> future) throws Exception {
 						handleStartApplicationFinished(future);
 					}
 				});
 			}
 		} else {
 			deployFutureNotifier.setFailure0(future.cause());
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Calls {@link DeployListener#moduleLoadedBlocks(UUID)} on all listeners.
 	 * 
 	 * @param moduleUUID
 	 *            the UUID of the ModuleInfo that loaded its blocks
 	 */
 	private void informBlocksLoaded(final UUID moduleUUID) {
 		listenerLock.readLock().lock();
 		try {
 			for (final DeployListener listener : listeners) {
 				listener.moduleLoadedBlocks(appId, moduleUUID);
 			}
 		} finally {
 			listenerLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Sends a {@link StartApplicationMessage} to all Modules and adds a listener to each future to call
 	 * {@link #informModuleStarted(UUID)} if the ModuleInfo started the application successfully.
 	 * 
 	 * @return a FutureNotifier returning the Responses of all Modules
 	 */
 	private FutureNotifier<Collection<Response>> sendStartApplication() {
 		final Collection<FutureNotifier<? extends Response>> futureNotifiers =
 				new ArrayList<FutureNotifier<? extends Response>>();
 		for (final ModuleInfo module : moduleMap.keySet()) {
 			final UUID moduleUUID = module.getUUID();
 			final FutureNotifier<Response> futureNotifier =
					connectionManager.sendMessage(module.getUUID(), new StartApplicationMessage(appId));
 			futureNotifier.addListener(new FutureListener<FutureNotifier<Response>>() {
 				@Override
 				public void operationComplete(final FutureNotifier<Response> future) {
 					if (future.isSuccess() && future.getNow() instanceof StartApplicationAck) {
 						informModuleStarted(moduleUUID);
 					}
 				}
 			});
 			futureNotifiers.add(futureNotifier);
 		}
 		return new JoinedFutureNotifier<Response>(futureNotifiers);
 	}
 
 	/**
 	 * This method is called when the FutureNotifier for the Responses to the {@link StartApplicationMessage} is
 	 * finished. If a positive Response is received, {@link #deployFutureNotifier} is marked as finished successfully,
 	 * otherwise it is marked as failed.
 	 * 
 	 * @param future
 	 *            the FutureNotifier that returns the Responses
 	 */
 	private void handleStartApplicationFinished(final FutureNotifier<Collection<Response>> future) {
 		LOGGER.entry();
 		if (deployFutureNotifier.isDone() && !deployFutureNotifier.isSuccess()) {
 			LOGGER.exit();
 			return;
 		}
 
 		if (future.isSuccess()) {
 			for (final Response response : future.getNow()) {
 				if (!(response instanceof StartApplicationAck)) {
 					deployFutureNotifier.setFailure0(null);
 					LOGGER.exit();
 					return;
 				}
 			}
 
 			LOGGER.debug("all modules started succesfully");
 			deployFutureNotifier.setSuccess0();
 		} else {
 			deployFutureNotifier.setFailure0(future.cause());
 		}
 		LOGGER.exit();
 	}
 
 	/**
 	 * Calls {@link DeployListener#moduleStarted(UUID)} on all listeners.
 	 * 
 	 * @param moduleUUID
 	 *            the UUID of the ModuleInfo that started
 	 */
 	private void informModuleStarted(final UUID moduleUUID) {
 		listenerLock.readLock().lock();
 		try {
 			for (final DeployListener listener : listeners) {
 				listener.moduleStarted(appId, moduleUUID);
 			}
 		} finally {
 			listenerLock.readLock().unlock();
 		}
 	}
 
 	/**
 	 * Calls {@link DeployListener#deployFailed(Throwable)} on all listeners.
 	 * 
 	 * @param cause
 	 *            the cause for the failure
 	 */
 	private void informDeployFailed(final Throwable cause) {
 		listenerLock.readLock().lock();
 		try {
 			for (final DeployListener listener : listeners) {
 				listener.deployFailed(appId, cause);
 			}
 		} finally {
 			listenerLock.readLock().unlock();
 		}
 	}
 
 	private void killApplication() {
 		LOGGER.info("killing failed application");
 		for (final ModuleInfo module : moduleMap.keySet()) {
 			final KillAppMessage killAppMsg = new KillAppMessage(appId);
 			connectionManager.sendMessage(module.getUUID(), killAppMsg);
 		}
 	}
 
 	/**
 	 * This class is used to provide a FutureNotifier for the deployment process.
 	 * 
 	 * @author Philipp Adolf
 	 */
 	private class DeployFutureNotifier extends DefaultFutureNotifier<Void> {
 		/**
 		 * Sets this FutureNotifier to be finished successfully (if it hasn't been set as finished already).
 		 */
 		protected void setSuccess0() {
 			LOGGER.trace("setting success");
 			setSuccess(null);
 		}
 
 		/**
 		 * Sets this FutureNotifier to be failed (if it hasn't been set as finished already). It also sends a
 		 * {@link KillAppMessage} to all Modules.
 		 * 
 		 * @param cause
 		 *            the cause for the failure
 		 */
 		protected boolean setFailure0(final Throwable cause) {
 			LOGGER.warn("setting failure, cause: {}", cause);
 			final boolean setFailureSucceeded = setFailure(cause);
 			if (setFailureSucceeded) {
 				killApplication();
 				informDeployFailed(cause);
 			}
 			return setFailureSucceeded;
 		}
 
 		@Override
 		public boolean cancel(boolean mayInterruptIfRunning) {
 			if (!mayInterruptIfRunning) {
 				return false;
 			}
 			return setFailure0(new CancellationException());
 		}
 	}
 }
