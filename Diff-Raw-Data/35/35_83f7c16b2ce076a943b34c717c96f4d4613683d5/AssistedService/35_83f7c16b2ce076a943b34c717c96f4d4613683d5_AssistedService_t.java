 package hemera.core.execution.assisted;
 
 import java.util.concurrent.TimeUnit;
 
 import hemera.core.execution.ExecutionService;
 import hemera.core.execution.interfaces.IExceptionHandler;
 import hemera.core.execution.interfaces.assisted.IAssistExecutor;
 import hemera.core.execution.interfaces.assisted.IAssistedService;
 import hemera.core.execution.interfaces.task.IEventTask;
 import hemera.core.execution.interfaces.task.IResultTask;
 import hemera.core.execution.interfaces.task.handle.IEventTaskHandle;
 import hemera.core.execution.interfaces.task.handle.IResultTaskHandle;
 import hemera.core.utility.AtomicCyclicInteger;
 
 /**
  * <code>AssistedService</code> defines a execution
  * service implementation that confirms with defined
  * interface <code>IAssistedService</code>.
  *
  * @author Yi Wang (Neakor)
  * @version 1.0.0
  */
 public class AssistedService extends ExecutionService implements IAssistedService {
 	/**
 	 * The array of <code>IAssistExecutor</code> pool.
 	 */
 	private final IAssistExecutor[] executors;
 	/**
 	 * The <code>long</code> executor idle time value.
 	 */
 	private final long idletime;
 	/**
 	 * The <code>TimeUnit</code> executor idle time
 	 * unit.
 	 */
 	private final TimeUnit idleunit;
 	/**
 	 * The <code>AtomicCyclicInteger</code> used to
 	 * track the next executor index.
 	 */
 	private final AtomicCyclicInteger index;
 
 	/**
 	 * Constructor of <code>AssistedService</code>.
 	 * @param handler The <code>IExceptionHandler</code>
 	 * instance.
 	 * @param count The <code>int</code> number of
 	 * executors this service should create.
 	 * @param idletime The <code>long</code> eager-
 	 * idling waiting time value.
 	 * @param idleunit The <code>TimeUnit</code> eager-
 	 * idling waiting time unit.
 	 */
 	public AssistedService(final IExceptionHandler handler, final int count, final long idletime,
 			final TimeUnit idleunit) {
 		super(handler);
 		this.executors = new IAssistExecutor[count];
 		this.idletime = idletime;
 		this.idleunit = idleunit;
 		this.index = new AtomicCyclicInteger(0, this.executors.length-1);
 	}
 
 	@Override
 	protected void doActivate() {
		// Create executors first before activation, since once an
		// executor is activated, it'll start assisting but not all
		// executors are created yet.
 		for (int i = 0; i < this.executors.length; i++) {
 			final String name = "AssistExecutor-" + i;
 			final AssistExecutor executor = new AssistExecutor(name, this.handler, this, this.idletime, this.idleunit);
 			this.executors[i] = executor;
		}
		// Activate executors.
		for (int i = 0; i < this.executors.length; i++) {
			this.executors[i].start();
 		}
 	}
 
 	@Override
 	protected void doShutdown() {
 		for (int i = 0; i < this.executors.length; i++) {
 			final IAssistExecutor executor = this.executors[i];
 			executor.terminate();
 		}
 	}
 
 	@Override
 	protected void doShutdownAndWait() throws InterruptedException {
 		for (int i = 0; i < this.executors.length; i++) {
 			final IAssistExecutor executor = this.executors[i];
 			executor.terminate();
 			// Wait for executor thread to terminate.
 			while (!executor.hasTerminated()) {
 				TimeUnit.MILLISECONDS.sleep(5);
 			}
 		}
 	}
 
 	@Override
 	protected void doForceShutdown() {
 		for (int i = 0; i < this.executors.length; i++) {
 			final IAssistExecutor executor = this.executors[i];
 			executor.forceTerminate();
 		}
 	}
 
 	@Override
 	protected void doForceShutdown(final long time, final TimeUnit unit) throws InterruptedException {
 		// Gracefully terminate all executors.
 		this.doShutdown();
 		// Wait for expiration.
 		unit.sleep(time);
 		// Forcefully terminate and remove all active executors.
 		this.doForceShutdown();
 	}
 
 	@Override
 	protected IEventTaskHandle doSubmit(final IEventTask task) {
 		return this.nextAssistExecutor().assign(task);
 	}
 
 	@Override
 	protected <V> IResultTaskHandle<V> doSubmit(final IResultTask<V> task) {
 		return this.nextAssistExecutor().assign(task);
 	}
 	
 	/**
 	 * Retrieve the next assist executor using a round-
 	 * robin rotation.
 	 * @return The <code>IAssistExecutor</code>.
 	 */
 	private IAssistExecutor nextAssistExecutor() {
 		final int index = this.index.getAndIncrement();
 		return this.executors[index];
 	}
 
 	@Override
 	public boolean assist() {
 		boolean assisted = false;
 		for (int i = 0; i < this.executors.length; i++) {
 			final IAssistExecutor executor = this.executors[i];
 			// Assist a single executor until all of its tasks are finished.
 			while (executor.assist()) {
 				if (!assisted) assisted = true;
 			}
 		}
 		return assisted;
 	}
 
 	@Override
 	public double getAverageQueueLength() {
 		double length = 0;
 		for (int i = 0; i < this.executors.length; i++) {
 			final IAssistExecutor executor = this.executors[i];
 			length += executor.getQueueLength();
 		}
 		return (length/(double)this.executors.length);
 	}
 
 	@Override
 	public int getCurrentExecutorCount() {
 		return this.executors.length;
 	}
 }
