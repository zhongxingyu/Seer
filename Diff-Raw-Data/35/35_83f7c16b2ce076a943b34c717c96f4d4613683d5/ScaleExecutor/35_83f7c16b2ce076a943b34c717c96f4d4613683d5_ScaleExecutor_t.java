 package hemera.core.execution.scalable;
 
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import hemera.core.execution.Executor;
 import hemera.core.execution.executable.EventExecutable;
 import hemera.core.execution.executable.Executable;
 import hemera.core.execution.executable.ResultExecutable;
 import hemera.core.execution.interfaces.IExceptionHandler;
 import hemera.core.execution.interfaces.scalable.IScalableService;
 import hemera.core.execution.interfaces.scalable.IScaleExecutor;
 import hemera.core.execution.interfaces.task.IEventTask;
 import hemera.core.execution.interfaces.task.IResultTask;
 import hemera.core.execution.interfaces.task.handle.IEventTaskHandle;
 import hemera.core.execution.interfaces.task.handle.IResultTaskHandle;
 
 /**
  * <code>ScaleExecutor</code> defines the executor
  * implementation that confirms with the interface
  * <code>IScaleExecutor</code>.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 public class ScaleExecutor extends Executor implements IScaleExecutor {
 	/**
 	 * The <code>IScalableService</code> group.
 	 */
 	private final IScalableService group;
 	/**
 	 * The <code>boolean</code> indicating if this
 	 * executor instance is on-demand.
 	 */
 	private final boolean ondemand;
 	/**
 	 * The <code>long</code> timeout value used to
 	 * terminate the executor if it is an on-demand
 	 * executor after task execution completion.
 	 */
 	private final long timeoutValue;
 	/**
 	 * The <code>TimeUnit</code> the timeout value
 	 * is in.
 	 */
 	private final TimeUnit timeoutUnit;
 	/**
 	 * The timing-out <code>Lock</code>.
 	 */
 	private final Lock lock;
 	/**
 	 * The waiting <code>Condition</code>.
 	 */
 	private final Condition wait;
 	/**
 	 * The <code>AtomicReference</code> of assigned
 	 * <code>Executable</code>.
 	 * <p>
 	 * Atomic check and set operation is needed to
 	 * ensure that only a single instance can be
 	 * assigned at any given time, and allow the
 	 * write and read operations to be performed in
 	 * different threads.
 	 */
 	private final AtomicReference<Executable> task;
 	
 	/**
 	 * Constructor of <code>ScaleExecutor</code>.
 	 * <p>
 	 * This constructor creates an initial executor that
 	 * is part of the initial minimum executor pool.
 	 * @param name The <code>String</code> name of this
 	 * executor thread.
 	 * @param handler The <code>IExceptionHandler</code>
 	 * used for task execution graceful exception
 	 * handling.
 	 * @param group The <code>IScalableService</code>
 	 * shared by all scale executors.
 	 */
 	public ScaleExecutor(final String name, final IExceptionHandler handler, final IScalableService group) {
 		this(name, handler, group, false, -1, null);
 	}
 	
 	/**
 	 * Constructor of <code>ScaleExecutor</code>.
 	 * <p>
 	 * This constructor creates an on-demand executor
 	 * that will terminate if there are no new tasks
 	 * assigned to it within the timeout period.
 	 * @param name The <code>String</code> name of this
 	 * executor thread.
 	 * @param handler The <code>IExceptionHandler</code>
 	 * used for task execution graceful exception
 	 * handling.
 	 * @param group The <code>IScalableService</code>
 	 * shared by all scale executors.
 	 * @param timeoutValue The <code>long</code> time-
 	 * out value used to terminate this on-demand
 	 * executor after task execution completion without
 	 * new task assignment.
 	 * @param timeoutUnit The <code>TimeUnit</code> the
 	 * timeout value is in.
 	 */
 	public ScaleExecutor(final String name, final IExceptionHandler handler, final IScalableService group,
 			final long timeoutValue, final TimeUnit timeoutUnit) {
 		this(name, handler, group, true, timeoutValue, timeoutUnit);
 	}
 
 	/**
 	 * Constructor of <code>ScaleExecutor</code>.
 	 * @param name The <code>String</code> name of this
 	 * executor thread.
 	 * @param handler The <code>IExceptionHandler</code>
 	 * used for task execution graceful exception
 	 * handling.
 	 * @param group The <code>IScalableService</code>
 	 * shared by all scale executors.
 	 * @param ondemand <code>true</code> if this instance
 	 * is created as an on-demand executor.
 	 * @param timeoutValue The <code>long</code> time-
 	 * out value used to terminate the executor if it
 	 * is an on-demand executor after task execution
 	 * completion.
 	 * @param timeoutUnit The <code>TimeUnit</code> the
 	 * timeout value is in.
 	 */
 	private ScaleExecutor(final String name, final IExceptionHandler handler, final IScalableService group, final boolean ondemand,
 			final long timeoutValue, final TimeUnit timeoutUnit) {
 		super(name, handler);
 		this.group = group;
 		this.ondemand = ondemand;
 		this.timeoutValue = timeoutValue;
 		this.timeoutUnit = timeoutUnit;
 		this.lock = new ReentrantLock();
 		this.wait = this.lock.newCondition();
 		this.task = new AtomicReference<Executable>(null);
 	}
 
 	@Override
 	protected final void doRun() throws Exception {
 		// Execute local task and set it to null to allow new assignments.
 		final Executable executable = this.task.getAndSet(null);
		// Initial activation cycle will not have a task yet.
		if (executable != null) executable.execute();
 		// Recycle for more tasks.
 		this.group.recycle(this);
 		// Go into waiting mode.
 		this.lock.lock();
 		try {
 			// Check for termination.
 			if (this.terminated) return;
 			// Do not go into waiting if there is a task.
 			else if (this.task.get() != null) return;
 			// If this executor is on-demand, wait on timeout.
 			else if (this.ondemand) {
 				final boolean signaled = this.wait.await(this.timeoutValue, this.timeoutUnit);
 				// Terminate if timed-out.
 				if (!signaled) this.terminate();
 			}
 			// Otherwise just wait for next task.
 			else {
 				this.wait.await();
 			}
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	@Override
 	public final void terminate() {
 		super.terminate();
 		// Wake up waiting.
 		this.lock.lock();
 		try {
 			this.wait.signalAll();
 		} finally {
 			this.lock.unlock();
 		}
 	}
 
 	@Override
 	public final IEventTaskHandle assign(final IEventTask task) {
 		// Check for termination early to avoid object construction.
 		if (this.terminated) return null;
 		// Try to assign.
 		final EventExecutable executable = new EventExecutable(task);
 		final boolean succeeded = this.task.compareAndSet(null, executable);
 		// There is a task assigned already.
 		if (!succeeded) return null;
 		// Assigned, return executable as handle.
 		else return executable;
 	}
 
 	@Override
 	public final <V> IResultTaskHandle<V> assign(final IResultTask<V> task) {
 		// Check for termination early to avoid object construction.
 		if (this.terminated) return null;
 		// Try to assign.
 		final ResultExecutable<V> executable = new ResultExecutable<V>(task);
 		final boolean succeeded = this.task.compareAndSet(null, executable);
 		// There is a task assigned already.
 		if (!succeeded) return null;
 		// Succeeded.
 		else {
 			// Wake up waiting.
 			this.lock.lock();
 			try {
 				this.wait.signalAll();
 			} finally {
 				this.lock.unlock();
 			}
 			// Return executable as handle.
 			return executable;
 		}
 	}
 	
 	@Override
 	public final boolean isOndemand() {
 		return this.ondemand;
 	}
 }
