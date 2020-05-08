 package edu.teco.dnd.module;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.blocks.AssignmentException;
 import edu.teco.dnd.blocks.ConnectionTarget;
 import edu.teco.dnd.blocks.FunctionBlock;
 import edu.teco.dnd.blocks.InvalidFunctionBlockException;
 import edu.teco.dnd.blocks.Output;
 import edu.teco.dnd.module.messages.values.ValueMessage;
 import edu.teco.dnd.module.messages.values.WhoHasBlockMessage;
 import edu.teco.dnd.network.ConnectionManager;
 
 public class Application {
 
 	private static final Logger LOGGER = LogManager.getLogger(Application.class);
 	public static final long MODULE_LOCATION_REQUEST_DELAY = 500;
 	public static final int SEND_REPETITIONS_UPON_UNKNOWN_MODULE_LOCATION = 2;
 
 	private final UUID ownAppId;
 	private final String name;
 
 	private final ScheduledThreadPoolExecutor scheduledThreadPool;
 	private final ConnectionManager connMan;
 	private final Map<UUID/* funcBlockId */, UUID/* ModuleId */> moduleForFuncBlock;
	private final Set<UUID /* funcBlockId */> ownBlocks;
 	private final ApplicationClassLoader classLoader;
 	/** mapping of active blocks to their ID, used e.g. to pass values to inputs. */
 	private final Map<UUID, FunctionBlock> funcBlockById;
 
 	/**
 	 * @return all blocks, this app is currently executing.
 	 */
 	public Collection<FunctionBlock> getAllBlocks() {
 		return funcBlockById.values();
 	}
 
 	public Application(UUID appId, String name, ScheduledThreadPoolExecutor scheduledThreadPool,
 			ConnectionManager connMan, ApplicationClassLoader classloader) {
 		this.ownAppId = appId;
 		this.name = name;
 		this.scheduledThreadPool = scheduledThreadPool;
 		this.connMan = connMan;
 		this.classLoader = classloader;
 
 		this.moduleForFuncBlock = new HashMap<UUID, UUID>();
		this.ownBlocks = new HashSet<UUID>();
 		this.funcBlockById = new HashMap<UUID, FunctionBlock>();
 	}
 
 	/**
 	 * called from this app, when a value is supposed to be send to another block (potentially on another Module).
 	 * 
 	 * @param funcBlock
 	 *            the receiving functionBlock.
 	 * @param input
 	 *            the input on the given block to receive the message.
 	 * @param val
 	 *            the value to be send.
 	 * @return true iff setting was successful.
 	 */
 	public void sendValue(final UUID funcBlock, final String input, final Serializable value) {
 		if (isExecuting(funcBlock)) { // block is local
 			try {
 				receiveValue(funcBlock, input, value);
 			} catch (NonExistentFunctionblockException e) {
 				// probably racecondition with app killing. Ignore.
 				LOGGER.trace(e);
 			} catch (NonExistentInputException e) {
 				LOGGER.trace("the given input {} does not exist on the local functionBlock {}", input, funcBlock);
 			}
 		} else {
 			Runnable resender = new Runnable() {
 				@Override
 				public void run() {
 					resendValue(funcBlock, input, value, SEND_REPETITIONS_UPON_UNKNOWN_MODULE_LOCATION);
 				}
 			};
 
 			UUID modUid = moduleForFuncBlock.get(funcBlock);
 			if (modUid == null) { // location of funcBlock unknown.
 				for (UUID mod : connMan.getConnectedModules()) {
 					connMan.sendMessage(mod, new WhoHasBlockMessage(ownAppId, funcBlock));
 				}
 				scheduledThreadPool.schedule(resender, MODULE_LOCATION_REQUEST_DELAY, TimeUnit.SECONDS);
 			} else {
 				ValueMessage message = new ValueMessage(ownAppId, funcBlock, input, value);
 				connMan.sendMessage(modUid, message);
 			}
 		}
 	}
 
 	/**
 	 * We did not find the address in the list. So we send out a request for the location and schedule the next try in
 	 * MODULE_LOCATION_REQUEST_DELAY milliseconds. If necessary we repeat this procedure repetitions times.
 	 * 
 	 * @param funcBlock
 	 *            see sendValue
 	 * @param input
 	 *            see sendValue
 	 * @param value
 	 *            see sendValue
 	 * @param repetitions
 	 *            how often to repeat sending upon failure.
 	 */
 	private void resendValue(final UUID funcBlock, final String input, final Serializable value, final int repetitions) {
 		Runnable resender = new Runnable() {
 			@Override
 			public void run() {
 				resendValue(funcBlock, input, value, repetitions - 1);
 			}
 		};
 
 		UUID modUid = moduleForFuncBlock.get(funcBlock);
 		if (modUid == null) {
 			if (repetitions > 0) {
 				scheduledThreadPool.schedule(resender, MODULE_LOCATION_REQUEST_DELAY, TimeUnit.SECONDS);
 				LOGGER.info("funcblock {} not found, remaining sendtries: {}", funcBlock, repetitions);
 			} else {
 				LOGGER.info("funcblock {} not found, disregarding message {} , to input {}", funcBlock, value, input);
 				// TODO special treatment of queued messaged -> parameter/differing method for such.
 			}
 		} else {
 			ValueMessage message = new ValueMessage(ownAppId, funcBlock, input, value);
 			connMan.sendMessage(modUid, message);
 		}
 	}
 
 	/**
 	 * Called when an error message has been received indicating, that the given FuncBlock ModId mapping is not valid
 	 * anymore
 	 * 
 	 * @param funcBlock
 	 *            the functionBlock
 	 * @param moduleId
 	 *            the module ID
 	 */
 	public void invalidateBlockModulePair(UUID funcBlock, UUID moduleId) {
 		LOGGER.entry(funcBlock, moduleId);
 		synchronized (moduleForFuncBlock) {
 			UUID oldModuleMapping = moduleForFuncBlock.get(funcBlock);
 			if (oldModuleMapping == moduleId) {
 				moduleForFuncBlock.remove(funcBlock);
 			}
 		}
 	}
 
 	/**
 	 * loads a class into this app
 	 * 
 	 * @param classname
 	 *            name of the class to load
 	 * @param classData
 	 *            bytecode of the class to be loaded
 	 */
 	public void loadClass(String classname, byte[] classData) {
 		classLoader.appLoadClass(classname, classData);
 	}
 
 	/**
 	 * starts the given function block on the Module. Also triggers removing it from runnable blocks
 	 * 
 	 * @param block
 	 *            the block to be started.
 	 * @return true iff block was successfully started.
 	 */
 	public void startBlock(final FunctionBlock block) {
 		funcBlockById.put(block.getID(), block);
 		scheduledThreadPool.execute(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					block.init();
 
 					for (Output<?> output : block.getOutputs().values()) {
 						for (ConnectionTarget ct : output.getConnectedTargets()) {
 							if (ct instanceof RemoteConnectionTarget) {
 								RemoteConnectionTarget rct = (RemoteConnectionTarget) ct;
 								rct.setApplication(Application.this);
 							}
 						}
 					}
 				} catch (InvalidFunctionBlockException e) {
 					LOGGER.warn("User supplied block {} initialization failed.", block.getID());
 					LOGGER.catching(e);
 
 				}
 			}
 		});
 		Runnable updater = new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					block.doUpdate();
 				} catch (AssignmentException e) {
 					LOGGER.catching(e);
 				}
 
 			}
 		};
 		long period = block.getTimebetweenSchedules();
 		try {
 			if (period < 0) {
 				scheduledThreadPool.schedule(updater, 0, TimeUnit.SECONDS);
 			} else {
 				scheduledThreadPool.scheduleAtFixedRate(updater, period, period, TimeUnit.SECONDS);
 			}
 		} catch (RejectedExecutionException e) {
 			LOGGER.info("Received start block after initiating shutdown. Not scheduling block {}.", block);
 		}
 	}
 
 	/**
 	 * passes a received value the given input of a local block.
 	 * 
 	 * @param funcBlockId
 	 *            Id of the block to pass the message to.
 	 * @param input
 	 *            input on the block receiving the message.
 	 * @param value
 	 *            the value to give to the input.
 	 * @return true iff value was successfully passed on.
 	 * @throws IllegalAccessException
 	 * @throws NonExistentFunctionblockException
 	 * @throws NonExistentInputException
 	 */
 	public void receiveValue(final UUID funcBlockId, String input, Serializable value)
 			throws NonExistentFunctionblockException, NonExistentInputException {
 
 		if (funcBlockById.get(funcBlockId) == null) {
 			LOGGER.info("FunctionBlockID not existent. ({})", funcBlockId);
 			throw LOGGER.throwing(new NonExistentFunctionblockException());
 		}
 		ConnectionTarget ct = funcBlockById.get(funcBlockId).getConnectionTargets().get(input);
 		if (ct == null) {
 			LOGGER.warn("specified input does not exist: {} on {}", input, funcBlockId);
 			throw LOGGER.throwing(new NonExistentInputException());
 		}
 		ct.setValue(value);
 		Runnable updater = new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					funcBlockById.get(funcBlockId).doUpdate();
 				} catch (AssignmentException e) {
 					LOGGER.catching(e);
 				}
 
 			}
 		};
 		try {
 			scheduledThreadPool.schedule(updater, 0, TimeUnit.SECONDS);
 		} catch (RejectedExecutionException e) {
 			LOGGER.info("Received message after initiating shutdown. Not rescheduling {}.", funcBlockId);
 		}
 	}
 
 	/**
 	 * called to indicate, that the application is being shut down. Quits the scheduling of it.
 	 * 
 	 * @return
 	 */
 	public void shutdown() {
 		scheduledThreadPool.shutdown();
 	}
 
 	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
 		return scheduledThreadPool.awaitTermination(timeout, unit);
 	}
 
 	public UUID getOwnAppId() {
 		return ownAppId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public ApplicationClassLoader getClassLoader() {
 		return classLoader;
 	}
 
 	public ScheduledThreadPoolExecutor getThreadPool() {
 		return scheduledThreadPool;
 	}
 
 	/**
 	 * @param blockId
 	 *            the blockId to check for.
 	 * @return true iff the the given block is executing on this Module.
 	 */
 	public boolean isExecuting(UUID blockId) {
		return ownBlocks.contains(blockId);
 	}
 }
