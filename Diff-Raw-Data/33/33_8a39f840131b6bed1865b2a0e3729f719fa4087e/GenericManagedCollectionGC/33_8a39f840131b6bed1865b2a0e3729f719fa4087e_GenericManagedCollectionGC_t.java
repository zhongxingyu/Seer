 package org.osjava.collections.managed.generic;
 
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.osjava.collections.managed.ManagedCollectionGC;
 import org.osjava.collections.managed.ManagedGC;
 
 public class GenericManagedCollectionGC implements ManagedCollectionGC {
 
	private static final long SWEEP_TIMEOUT = 500000000;
 
 	private final List<ManagedGC<?>> _managedObjectGC = new CopyOnWriteArrayList<ManagedGC<?>>();
 
 	private final List<ManagedGC<?>> _managedBindingGC = new CopyOnWriteArrayList<ManagedGC<?>>();
 
 	private final Thread _thread;
 
 	private boolean _active;
 
 	private long _nanoTime;
 
 	private GenericManagedCollectionGC() {
 
 		_active = true;
 		_nanoTime = System.nanoTime();
 
 		_thread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				while (_active) {
 					if (System.nanoTime() - _nanoTime > SWEEP_TIMEOUT) {
 
						for (ManagedGC<?> gc : _managedBindingGC) {
 							gc.sweep();
 						}
 
						for (ManagedGC<?> gc : _managedObjectGC) {
 							gc.sweep();
 						}
 
 						_nanoTime = System.nanoTime();
 					}
 				}
 			}
 		});
 		_thread.start();
 	}
 
 	public static GenericManagedCollectionGC newInstance() {
 		return new GenericManagedCollectionGC();
 	}
 
 	@Override
 	public synchronized <E> void addManagedObjectGC(ManagedGC<E> gc) {
 		if (_managedObjectGC.indexOf(gc) < 0) {
 			_managedObjectGC.add(gc);
 		}
 	}
 
 	@Override
 	public <E> void removeManagedObjectGC(ManagedGC<E> gc) {
 		_managedObjectGC.remove(gc);
 	}
 
 	@Override
 	public synchronized <B> void addManagedBindingGC(ManagedGC<B> gc) {
 		if (_managedBindingGC.indexOf(gc) < 0) {
 			_managedBindingGC.add(gc);
 		}
 	}
 
 	@Override
 	public <B> void removeManagedBindingGC(ManagedGC<B> gc) {
 		_managedBindingGC.remove(gc);
 	}
 }
