 package edu.teco.dnd.blocks;
 
 import java.io.Serializable;
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 public class Input<T extends Serializable> implements OutputTarget<T> {
 	private boolean newestOnly = false;
 
 	// TODO: Maybe change to (Soft)Reference, but then a wrapper is needed for null
 	private final Deque<T> values = new ArrayDeque<T>();
 
 	public synchronized T popValue() {
 		return values.poll();
 	}
 
 	public synchronized boolean hasMoreValues() {
 		return !values.isEmpty();
 	}
 
 	public synchronized void setValue(T value) {
		//TODO check for null
 		if (newestOnly) {
 			values.clear();
 		}
 		values.add(value);
 	}
 
 	public synchronized void setNewestOnly(final boolean state) {
 		this.newestOnly = state;
 		if (newestOnly) {
 			final T value = values.peekLast();
 			values.clear();
 			values.add(value);
 		}
 	}
 }
