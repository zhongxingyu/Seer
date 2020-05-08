 package com.polopoly.ps.tools.collections.componentcollection;
 
 import static com.polopoly.util.Require.require;
 
 import java.util.Iterator;
import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.polopoly.ps.tools.collections.ComponentStorage;
 import com.polopoly.ps.tools.collections.componentcollection.MapStorageProvider.Storage;
 import com.polopoly.ps.tools.collections.converter.IntegerConverter;
 import com.polopoly.ps.tools.collections.exception.NoSuchComponentException;
 import com.polopoly.ps.tools.collections.exception.NoSuchEntryException;
 import com.polopoly.util.content.ContentUtil;
 
 public class MapStorageToQueueStorageWrapper<T> implements QueueStorage<T> {
 	private static final Logger LOGGER = Logger.getLogger(MapStorageToQueueStorageWrapper.class.getName());
 
 	private static final String START_COMPONENT = "start";
 	private static final String END_COMPONENT = "end";
 
 	private Storage<Integer, T> delegate;
 	private ComponentStorage<Integer> positionStorage;
 	private ContentUtil content;
 	private String outerKey;
 
 	private int bufferSize;
 
 	public MapStorageToQueueStorageWrapper(Storage<Integer, T> delegate, ComponentStorage<?> storage,
 			ContentUtil content, String outerKey, int bufferSize) {
 		this.delegate = require(delegate);
 		this.positionStorage = require(storage).getOtherwiseTypedStorage(new IntegerConverter());
 		this.content = require(content);
 		this.outerKey = require(outerKey);
 		this.bufferSize = bufferSize;
 	}
 
 	private T get(int index) throws IndexOutOfBoundsException {
 		try {
 			return delegate.get(index);
 		} catch (NoSuchEntryException e) {
 			throw new IndexOutOfBoundsException("Attempt to get element " + index + " in " + this + ": "
 					+ e.toString());
 		}
 	}
 
 	private void set(int index, T value) throws IndexOutOfBoundsException {
 		delegate.put(index, value);
 	}
 
 	private void setStart(int start) {
 		setPosition(start, START_COMPONENT);
 	}
 
 	private int getStart() {
 		return getPosition(START_COMPONENT);
 	}
 
 	private void setEnd(int end) {
 		setPosition(end, END_COMPONENT);
 	}
 
 	private int getEnd() {
 		return getPosition(END_COMPONENT);
 	}
 
 	private void setPosition(int position, String component) {
 		positionStorage.setComponent(content, outerKey, component, position);
 	}
 
 	private int getPosition(String component) {
 		try {
 			return positionStorage.getComponent(content, outerKey, component);
 		} catch (NoSuchComponentException e) {
			LOGGER.log(Level.WARNING, "Internal error: " + component + " not present in " + outerKey + ": "
					+ e);
 		}
 
 		return 0;
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return size() == 0;
 	}
 
 	@Override
 	public int size() {
 		int end = getEnd();
 		int start = getStart();
 
 		if (end >= start) {
 			return end - start;
 		} else {
 			return end - start + bufferSize;
 		}
 	}
 
 	@Override
 	public void push(T value) {
 		int oldEnd = getEnd();
 		int newEnd = (oldEnd + 1) % bufferSize;
 
 		setEnd(newEnd);
 
 		if (newEnd == getStart()) {
 			setStart((getStart() + 1) % bufferSize);
 		}
 
 		set(oldEnd, value);
 	}
 
 	@Override
 	public T pop() {
 		if (isEmpty()) {
 			throw new IllegalStateException("Attempt to pop empty queue.");
 		}
 
 		int oldStart = getStart();
 		int newStart = (oldStart + 1) % bufferSize;
 
 		setStart(newStart);
 
 		return get(oldStart);
 	}
 
 	@Override
 	public Iterator<T> iterator() {
 		return new Iterator<T>() {
 			int at = getStart();
 			int end = getEnd();
 
 			@Override
 			public boolean hasNext() {
 				return at != end;
 			}
 
 			@Override
 			public T next() {
 				if (!hasNext()) {
 					throw new IllegalStateException();
 				}
 
 				try {
 					return get(at);
 				} finally {
 					at = (at + 1) % bufferSize;
 				}
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	public String toString() {
 		StringBuffer result = new StringBuffer(100);
 
 		result.append("[");
 
 		for (T value : this) {
 			if (result.length() > 1) {
 				result.append(",");
 			}
 
 			result.append(value);
 		}
 
 		result.append("]");
 
 		return "List in " + outerKey + " in " + content + ": " + result.toString();
 	}
 }
