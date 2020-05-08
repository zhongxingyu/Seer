 package edu.teco.dnd.util;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.logging.log4j.Level;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 /**
  * A class that does the boilerplate work for FutureNotifiers.
  * 
  * @author Philipp Adolf
  */
 public abstract class DefaultFutureNotifier<V> extends AbstractFutureNotifier<V> {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(DefaultFutureNotifier.class);
 
 	/**
 	 * Used to track the current state.
 	 * 
 	 * @author Philipp Adolf
 	 */
 	private static enum State {
 		UNFINISHED, SUCCESS, FAILURE
 	}
 
 	/**
 	 * The current state of the FutureNotifier.
 	 */
 	private State state = State.UNFINISHED;
 
 	/**
 	 * The result of the operation.
 	 */
 	private V result = null;
 
 	/**
 	 * The cause for the failure of the operation.
 	 */
 	private Throwable cause = null;
 
 	/**
 	 * The listeners currently registered. Will be emptied after all listeners have been notified.
 	 */
 	private final Collection<FutureListener<? extends FutureNotifier<? super V>>> listeners =
 			new HashSet<FutureListener<? extends FutureNotifier<? super V>>>();
 
 	@Override
 	public synchronized boolean isSuccess() {
 		return state == State.SUCCESS;
 	}
 
 	@Override
 	public synchronized Throwable cause() {
 		return cause;
 	}
 
 	@Override
 	public synchronized void addListener(FutureListener<? extends FutureNotifier<? super V>> listener) {
 		if (isDone()) {
 			notifyListener(listener);
 		} else {
 			listeners.add(listener);
 		}
 	}
 
 	@Override
 	public synchronized void removeListener(FutureListener<? extends FutureNotifier<? super V>> listener) {
 		listeners.remove(listener);
 	}
 
 	@Override
 	public synchronized V getNow() {
 		return result;
 	}
 
 	@Override
 	public void await() throws InterruptedException {
 		if (isDone()) {
 			return;
 		}
 
 		if (Thread.interrupted()) {
 			throw new InterruptedException();
 		}
 
 		synchronized (this) {
 			while (!isDone()) {
 				wait();
 			}
 		}
 	}
 
 	@Override
 	public boolean await(final long timeout, final TimeUnit unit) throws InterruptedException {
 		return false;
 	}
 
 	public boolean await(final long timeoutNano) throws InterruptedException {
 		if (isDone()) {
 			return true;
 		}
 
 		if (timeoutNano <= 0) {
 			return isDone();
 		}
 
 		if (Thread.interrupted()) {
 			throw new InterruptedException();
 		}
 
 		final long starttime = System.nanoTime();
 		long waittime = timeoutNano;
 
 		synchronized (this) {
 			if (isDone()) {
 				return true;
 			}
 
 			while (waittime > 0) {
 				wait(waittime / 1000000L, (int) (waittime % 1000000L));
 
 				if (isDone()) {
 					return true;
 				}
 
 				waittime = timeoutNano - (System.nanoTime() - starttime);
 			}
 		}
 
 		return isDone();
 	}
 
 	@Override
 	public boolean cancel(final boolean mayInterruptIfRunning) {
 		return false;
 	}
 
 	@Override
 	public boolean isCancelled() {
 		return false;
 	}
 
 	@Override
 	public synchronized boolean isDone() {
 		return state != State.UNFINISHED;
 	}
 
 	/**
 	 * This method can be called by subclasses to mark this Future as finished and successful with the given result.
 	 * Will do nothing if the Future is already marked as finished.
 	 * 
 	 * @param result
 	 *            the result of the operation
 	 * @return false if the Future was already marked as finished, true otherwise
 	 */
 	protected synchronized boolean setSuccess(final V result) {
 		if (isDone()) {
 			return false;
 		}
 
 		state = State.SUCCESS;
 		this.result = result;
 
 		notifyListeners();
 
 		listeners.clear();
 
 		return true;
 	}
 
 	/**
 	 * This method can be called by subclasses to mark this Future as finished and failed with the given cause. Will do
 	 * nothing if the Future is already marked as finished.
 	 * 
 	 * @param cause
 	 *            the cause for the failure of the operation
 	 * @return false if the Future was already marked as finished, true otherwise
 	 */
 	protected synchronized boolean setFailure(final Throwable cause) {
 		if (isDone()) {
 			return false;
 		}
 
 		state = State.FAILURE;
 		this.cause = cause;
 
 		notifyListeners();
 
 		listeners.clear();
 
 		return true;
 	}
 
 	/**
 	 * Notifies all registered listeners that the Future has finished.
 	 */
 	private synchronized void notifyListeners() {
 		for (final FutureListener<? extends FutureNotifier<? super V>> listener : listeners) {
 			notifyListener(listener);
 		}
 	}
 
 	/**
 	 * Notifies a single listener that the Future has finished.
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private void notifyListener(final FutureListener<? extends FutureNotifier<? super V>> listener) {
 		assert isDone();
 
 		try {
 			((FutureListener) listener).operationComplete(this);
 		} catch (final Exception e) {
 			LOGGER.catching(Level.WARN, e);
 			LOGGER.warn("listener {} threw exception {}", listener, e);
 		}
 	}
 }
