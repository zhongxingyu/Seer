 package hemera.core.execution.scalable;
 
 import java.util.concurrent.BlockingDeque;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import hemera.core.execution.ExecutionService;
 import hemera.core.execution.interfaces.IExceptionHandler;
 import hemera.core.execution.interfaces.IExecutor;
 import hemera.core.execution.interfaces.scalable.IScalableService;
 import hemera.core.execution.interfaces.scalable.IScaleExecutor;
 import hemera.core.execution.interfaces.task.IEventTask;
 import hemera.core.execution.interfaces.task.IResultTask;
 import hemera.core.execution.interfaces.task.handle.IEventTaskHandle;
 import hemera.core.execution.interfaces.task.handle.IResultTaskHandle;
 
 /**
  * <code>ScalableService</code> defines a execution
  * service implementation that confirms with defined
  * interface <code>IScalableService</code>.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 public class ScalableService extends ExecutionService implements IScalableService {
 	/**
 	 * The <code>int</code> minimum number of executors
 	 * the service can shrink down to.
 	 */
 	private final int minCount;
 	/**
 	 * The <code>int</code> maximum number of executors
 	 * the service can grow up to.
 	 */
 	private final int maxCount;
 	/**
 	 * The <code>long</code> timeout value used to
 	 * terminate on-demand executors.
 	 */
 	private final long timeoutValue;
 	/**
 	 * The <code>TimeUnit</code> the timeout value
 	 * is in.
 	 */
 	private final TimeUnit timeoutUnit;
 	/**
 	 * The <code>BlockingQueue</code> of instances of
 	 * all <code>IScaleExecutor</code> created.
 	 * <p>
 	 * This data structure guarantees thread-safety and
 	 * high concurrency to allow executors to be added
 	 * with minimum contention, since they may be added
 	 * from different submission threads. This structure
 	 * also provides blocking behavior where if the queue
 	 * is full, adding will block until one is removed.
 	 */
 	private final BlockingQueue<IScaleExecutor> executors;
 	/**
 	 * The <code>BlockingDeque</code> of instances of
 	 * available <code>IScaleExecutor</code>.
 	 * <p>
 	 * This data structure guarantees thread-safety and
 	 * high concurrency to allow executors to be polled
 	 * and added with minimum contention, since they
 	 * may be added and polled from different submission
 	 * threads. This structure also provides blocking
 	 * behavior where if the deque is empty, polling
 	 * will block until one becomes available. Or if the
 	 * deque is full, adding will block until one is
 	 * polled.
 	 * <p>
 	 * Available executors are inserted at the tail and
 	 * polled from the head in a round-robin rotation.
 	 */
 	private final BlockingDeque<IScaleExecutor> availables;
 	/**
 	 * The <code>AtomicInteger</code> of current on-
 	 * demand executor index.
 	 */
 	private final AtomicInteger ondemandIndex;
 
 	/**
 	 * Constructor of <code>ScalableService</code>.
 	 * @param handler The <code>IExceptionHandler</code>
 	 * instance.
 	 * @param min The <code>int</code> minimum number
 	 * of executors the service can shrink down to.
 	 * @param max The <code>int</code> maximum number
 	 * of executors the service can grow up to.
 	 * @param timeoutValue The <code>long</code> time-
 	 * out value used to terminate on-demand executor.
 	 * @param timeoutUnit The <code>TimeUnit</code> the
 	 * timeout value is in.
 	 */
 	public ScalableService(final IExceptionHandler handler, final int min, final int max,
 			final long timeoutValue, final TimeUnit timeoutUnit) {
 		super(handler);
 		this.minCount = min;
 		this.maxCount = max;
 		this.timeoutValue = timeoutValue;
 		this.timeoutUnit = timeoutUnit;
 		if (this.minCount > this.maxCount) {
 			throw new IllegalArgumentException("Maximum executor count must be greater than the minimum executor count.");
 		}
 		this.executors = new LinkedBlockingQueue<IScaleExecutor>(this.maxCount);
 		this.availables = new LinkedBlockingDeque<IScaleExecutor>(this.maxCount);
 		this.ondemandIndex = new AtomicInteger(0);
 	}
 
 	@Override
 	protected void doActivate() {
 		// Create and start initial executors.
 		for (int i = 0; i < this.minCount; i++) {
 			final String name = "Initial-ScaleExecutor-" + i;
 			final ScaleExecutor executor = new ScaleExecutor(name, this.handler, this);
 			boolean succeeded = this.executors.offer(executor);
 			if (!succeeded) {
 				final StringBuilder builder = new StringBuilder();
 				builder.append("Creating initial scale executor failed on count ").append(i);
 				builder.append(" with maximum count ").append(this.maxCount);
 				builder.append(" with minimum count ").append(this.minCount).append(".");
 				throw new IllegalArgumentException(builder.toString());
 			} else {
				// Activate executor. There is no need to push the
				// executor to the available pool, since it will
				// recycle itself on the initial cycle without any
				// task assigned.
 				executor.start();
 			}
 		}
 	}
 
 	@Override
 	protected void doShutdown() {
 		// Gracefully terminate and remove all active executors.
 		while (!this.executors.isEmpty()) {
 			final IExecutor executor = this.executors.poll();
 			executor.terminate();
 		}
 	}
 
 	@Override
 	protected void doShutdownAndWait() throws InterruptedException {
 		// Remove and wait for all executor threads to terminate.
 		while (!this.executors.isEmpty()) {
 			final IExecutor executor = this.executors.poll();
 			executor.terminate();
 			while (!executor.hasTerminated()) {
 				TimeUnit.MILLISECONDS.sleep(5);
 			}
 		}
 	}
 
 	@Override
 	protected void doForceShutdown() {
 		// Forcefully terminate and remove all active executors.
 		while (!this.executors.isEmpty()) {
 			final IExecutor executor = this.executors.poll();
 			executor.forceTerminate();
 		}
 	}
 
 	@Override
 	protected void doForceShutdown(final long time, final TimeUnit unit) throws InterruptedException {
 		// Gracefully terminate all active executors.
 		for (final IExecutor executor : this.executors) {
 			executor.terminate();
 		}
 		// Wait for expiration.
 		unit.sleep(time);
 		// Forcefully terminate and remove all active executors.
 		while (!this.executors.isEmpty()) {
 			final IExecutor executor = this.executors.poll();
 			executor.forceTerminate();
 		}
 	}
 	
 	@Override
 	protected IEventTaskHandle doSubmit(final IEventTask task) {
 		return this.nextScaleExecutor().assign(task);
 	}
 	
 	@Override
 	protected <V> IResultTaskHandle<V> doSubmit(final IResultTask<V> task) {
 		return this.nextScaleExecutor().assign(task);
 	}
 	
 	/**
 	 * Retrieve the next available scale executor.
 	 * <p>
 	 * Invocations of this method has three possible
 	 * outcomes:
 	 * 1. If there is an available executor then it
 	 * is returned immediately.
 	 * 2. If there are no executors available but the
 	 * maximum executor count has not been reached,
 	 * an on-demand executor is created and returned.
 	 * 3. If there are no executors available and the
 	 * maximum executor count has been reached, this
 	 * invocation blocks until an executor becomes
 	 * available, at which time, the executor is then
 	 * returned.
 	 * @return The <code>IScaleExecutor</code> for
 	 * task assignment.
 	 */
 	private IScaleExecutor nextScaleExecutor() {
 		// If there is an available executor then it is returned immediately.
 		final IScaleExecutor existing = this.availables.pollFirst();
 		if (existing != null) return existing;
 		// Try to create and add a new on-demand executor.
 		final IScaleExecutor newexecutor = this.newOndemandExecutor();
 		// New executor created, return the executor.
 		if (newexecutor != null) return newexecutor;
 		// Maximum amount has been reached, wait until an
 		// existing executor becomes available.
 		else {
 			try {
 				return this.availables.takeFirst();
 			} catch (final InterruptedException e) {
 				this.handler.handle(e);
 				return this.nextScaleExecutor();
 			}
 		}
 	}
 	
 	/**
 	 * Try to create and insert a new on-demand executor
 	 * if the maximum amount has not been reached.
 	 * @return The <code>IScaleExecutor</code> created.
 	 * <code>null</code> if the maximum amount has been
 	 * reached.
 	 */
 	private IScaleExecutor newOndemandExecutor() {
 		final String name = "Ondemand-ScaleExecutor-" + this.ondemandIndex.getAndIncrement();
 		final ScaleExecutor executor = new ScaleExecutor(name, this.handler, this, this.timeoutValue, this.timeoutUnit);
 		// Try to insert.
 		final boolean succeeded = this.executors.offer(executor);
 		if (!succeeded) return null;
 		else {
 			executor.start();
 			return executor;
 		}
 	}
 	
 	@Override
 	public void recycle(final IScaleExecutor executor) {
 		// Try to insert into the available pool.
 		final boolean succeeded = this.availables.offerLast(executor);
 		// If failed, then this executor is an excess one, and should not exist.
 		if (!succeeded) {
 			this.logger.warning("Excess executor detected and terminated: " + executor.toString());
 			executor.terminate();
 		}
 	}
 	
 	@Override
 	public int getAvailableCount() {
 		return this.availables.size();
 	}
 	
 	@Override
 	public int getCurrentExecutorCount() {
 		return this.executors.size();
 	}
 }
