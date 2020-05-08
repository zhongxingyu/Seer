 package org.osjava.collections.managed;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class AbstractManagedGC<T> implements ManagedGC<T> {
 
	protected static final long SWEEP_TIMEOUT = 5000000l;
 
 	protected final List<T> marked = new ArrayList<T>();
 
 	protected final List<ManagedGCObserver<T>> listeners = new ArrayList<ManagedGCObserver<T>>();
 
 	protected ManagedGCRemoveListener<T> removeListener;
 
 	public AbstractManagedGC() {
 
 	}
 
 	@Override
 	public ManagedGCObserver<T> addObserver(ManagedGCObserver<T> observer) {
 		if (listeners.indexOf(observer) < 0) {
 			listeners.add(observer);
 		}
 		return observer;
 	}
 
 	@Override
 	public ManagedGCObserver<T> removeObserver(ManagedGCObserver<T> observer) {
 		listeners.remove(observer);
 		return observer;
 	}
 
 	@Override
 	public Boolean hasObserver(ManagedGCObserver<T> observer) {
 		return listeners.indexOf(observer) >= 0;
 	}
 
 	@Override
 	public void onRemoveListener(ManagedGCRemoveListener<T> listener) {
 		removeListener = listener;
 	}
 
 	@Override
 	public ManagedIterator<T> iterator() {
 		// TODO : Pool this iterator.
 		return AbstractManagedGCIterator.newInstance(new ArrayList<T>(marked));
 	}
 
 	@Override
 	public void mark(T value) {
 		synchronized (marked) {
 			if (!marked.contains(value)) {
 				marked.add(value);
 			}
 		}
 	}
 
 	@Override
 	public void unmark(T value) {
 		synchronized (marked) {
 			if (marked.contains(value)) {
 				marked.remove(value);
 			}
 		}
 	}
 
 	@Override
 	public void sweep() {
 		for (ManagedGCObserver<T> observer : listeners) {
 			observer.onStartSweep(this);
 		}
 
 		onSweep();
 
 		for (ManagedGCObserver<T> observer : listeners) {
 			observer.onFinishSweep(this);
 		}
 	}
 
 	protected void onSweep() {
 		throw new AbstractMethodError();
 	}
 }
