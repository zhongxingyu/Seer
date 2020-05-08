 package org.osjava.collections.managed.generic;
 
 import org.osjava.collections.managed.AbstractManagedGC;
 
 public final class GenericManagedGC<T> extends AbstractManagedGC<T> {
 
 	private GenericManagedGC() {
 		super();
 	}
 
 	public static <T> GenericManagedGC<T> newInstance() {
 		return new GenericManagedGC<T>();
 	}
 
 	@Override
 	protected void onSweep() {
		long time = System.nanoTime();
 		synchronized (marked) {

			System.out.println(">>> " + marked.size());

 			for (int i = marked.size() - 1; i >= 0; i--) {
 				removeListener.onRemove(marked.remove(i));
 
 				if (System.nanoTime() - time > SWEEP_TIMEOUT)
 					break;
 			}

			System.out.println(">>>> " + marked.size());
 		}
 	}
 }
