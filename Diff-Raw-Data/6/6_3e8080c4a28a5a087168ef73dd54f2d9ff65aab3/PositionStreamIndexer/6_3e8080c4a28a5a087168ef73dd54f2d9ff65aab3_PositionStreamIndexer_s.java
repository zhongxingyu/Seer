 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.device.scannable;
 
 import gda.device.DeviceException;
 
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
import java.util.Vector;
 import java.util.concurrent.Callable;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * Used as a component in {@link PositionCallableProvider}s that implement {@link PositionInputStream}s an
  * indexer can be deferred to to very easily implement {@link PositionCallableProvider#getPositionCallable()}. 
  * 
  * @param <T>
  */
 public class PositionStreamIndexer<T> implements PositionCallableProvider<T> {
 
 	private final PositionInputStream<T> stream;
 
 	private final int maxElementsToReadInOneGo;
 
 	Map<Integer, T> readValuesNotGot = new HashMap<Integer, T>();
 
 	private int lastIndexGivenOut = -1;
 	
 	private int lastIndexRead = -1;
 	
 	private Lock fairGetLock = new ReentrantLock(true);
 
 	public PositionStreamIndexer(PositionInputStream<T> stream) {
 		this(stream, Integer.MAX_VALUE);
 	}
 
 	public PositionStreamIndexer(PositionInputStream<T> stream, int maxElementsToReadInOneGo) {
 		this.stream = stream;
 		this.maxElementsToReadInOneGo = maxElementsToReadInOneGo;
 	}
 	/**
 	 * Can only be called once for each index
 	 */
 	public T get(int index) throws NoSuchElementException, InterruptedException, DeviceException {
 		fairGetLock.lock();
 		// Keep reading until the indexed element is read from the stream.
		while (index > readValuesNotGot.size() - 1) {
 			List<T> values = stream.read(maxElementsToReadInOneGo);
 			for (T value : values) {
 				readValuesNotGot.put(++lastIndexRead, value);
 				
 			}
 		}
 		if (!readValuesNotGot.containsKey(index)) {
 			throw new IllegalStateException("Element " + index + " is not available. Values can only be got once (to avoid excessive memory use).");
 		}
 		T value = readValuesNotGot.remove(index);
 		fairGetLock.unlock();
 		return value;
 	}
 
 	@Override
 	public Callable<T> getPositionCallable() throws DeviceException {
 		lastIndexGivenOut += 1;
 		return new PositionStreamIndexPuller<T>(lastIndexGivenOut, this);
 	}
 
 }
 
 class PositionStreamIndexPuller<T> implements Callable<T> {
 
 	private final int index;
 
 	private final PositionStreamIndexer<T> indexer;
 
 	private T value;
 	
 	private boolean called = false;
 
 	public PositionStreamIndexPuller(int index, PositionStreamIndexer<T> indexer) {
 		this.index = index;
 		this.indexer = indexer;
 	}
 
 	@Override
 	public T call() throws Exception {
 		if (!called) {
 			value = indexer.get(index);
 			called = true;
 		}
 		return value;
 	}
 
 }
