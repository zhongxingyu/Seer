 /*
 * Copyright (c) 2013, Finn Kuusisto
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
 package kuusisto.simpool;
 
 import java.util.LinkedList;
 
 /**
  * The Pool class is used for the actual pooling of Poolable objects.  A Pool
  * must be provided with a PoolableFactory for its Poolable type, can be given
  * an initial size, and can be limited in the number of allocations that it is
  * allowed to make.
  * 
  * @author Finn Kuusisto
  *
  * @param <T> The Poolable type that the Pool holds.
  */
 public class Pool<T extends Poolable> {
 	
 	public static final int NO_LIMIT = Integer.MAX_VALUE;
 
 	private PoolableFactory<T> factory;
 	private LinkedList<T> pool;
 	private int maxAllocate;
 	private int numAllocated;
 	
 	/**
 	 * Create a new empty Pool with no limit on the number of allocations that
 	 * it can make.
 	 * @param factory a PoolableFactory to create the desired Poolable type
 	 * @throws SimpoolException if factory is null
 	 */
 	public Pool(PoolableFactory<T> factory) throws SimpoolException {
 		this(factory, 0, NO_LIMIT);
 	}
 	
 	/**
 	 * Create a new Pool with an initial number of instances and no limit on the
 	 * number of allocations that it can make.
 	 * @param factory a PoolableFactory to create the desired Poolable type
 	 * @param startSize the initial number of Poolable instances to allocate
 	 * @throws SimpoolException if factory is null
 	 */
 	public Pool(PoolableFactory<T> factory, int startSize)
 			throws SimpoolException {
 		this(factory, startSize, NO_LIMIT);
 	}
 	
 	/**
 	 * Create a new Pool with an initial number of instances and a limit on the
 	 * number of allocations that it can make.
 	 * @param factory a PoolableFactory to create the desired Poolable type
 	 * @param startSize the initial number of Poolable instances to allocate,
 	 * up to maxAllocate
 	 * @param maxAllocate the maximum number of Poolable instances this Pool is
 	 * allowed to make (non-positive assumed to be NO_LIMIT)
 	 * @throws SimpoolException if factory is null
 	 */
 	public Pool(PoolableFactory<T> factory, int startSize, int maxAllocate)
 			throws SimpoolException {
 		//won't work if we didn't get a factory
 		if (factory == null) {
 			throw new SimpoolException("Null PoolableFactory");
 		}
 		this.factory = factory;
 		this.pool = new LinkedList<T>();
 		//non-positive should be considered no limit
 		this.maxAllocate = (maxAllocate > 0) ? maxAllocate : NO_LIMIT;
 		this.numAllocated = 0;
 		//we want to make sure we aren't starting larger than the max allowed
 		//number of allocations
 		startSize = (startSize > this.maxAllocate) ? maxAllocate : startSize;
 		for (int i = 0; i < startSize; i++) {
 			this.pool.add(this.factory.create());
 			this.numAllocated++;
 		}
 	}
 	
 	/**
 	 * Get an instance from the Pool.
 	 * @return an instance from this Pool
 	 * @throws SimpoolException if no more instances are available and maximum
 	 * number of allocations has been reached
 	 */
 	public T get() throws SimpoolException {
 		//get an object with Exception on error
 		return this.get(false);
 	}
 	
 	/**
 	 * Get an instance from the Pool, with the option of returning null instead
 	 * of throwing a SimpoolException on error.
 	 * @param quietError true if null should be returned on error instead of
 	 * throwing a SimpoolException
 	 * @return an instance from this Pool
 	 * @throws SimpoolException if no more instances are available, maximum
 	 * number of allocations has been reached, and quietError is false
 	 */
 	public T get(boolean quietError) throws SimpoolException {
 		//if one is available, there is no problem
 		if (this.pool.size() > 0) {
 			return this.pool.remove();
 		}
 		//first see if we can still allocate some
 		if (this.numAllocated < this.maxAllocate) {
 			T object = this.factory.create();
 			this.numAllocated++;
 			return object;
 		}
 		//we're toast, we can only honor quietError
 		if (quietError) {
 			return null;
 		}
 		throw new SimpoolException("No Poolable Available");
 	}
 	
 	/**
	 * Give an instance to the Pool.  This should normally be done with
	 * instances that were originally retrieved from the Pool, but may be done
	 * with instances allocated elsewhere.  This can allow you to overfill the
	 * Pool past the maximum allocation limit.
 	 * @param object the instance to return to the pool (will be reset)
 	 */
 	public void give(T object) {
 		//reset the object before putting it back in the pool
 		object.reset();
 		this.pool.add(object);
 	}
 	
 }
