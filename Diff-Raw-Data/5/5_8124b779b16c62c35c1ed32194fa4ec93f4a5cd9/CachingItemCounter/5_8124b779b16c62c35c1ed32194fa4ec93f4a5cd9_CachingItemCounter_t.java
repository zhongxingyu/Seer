 /*
 Copyright 2012 Brian Romanowski. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
    1. Redistributions of source code must retain the above copyright notice, this list of
       conditions and the following disclaimer.
 
    2. Redistributions in binary form must reproduce the above copyright notice, this list
       of conditions and the following disclaimer in the documentation and/or other materials
       provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY BRIAN ROMANOWSKI ``AS IS'' AND ANY EXPRESS OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BRIAN ROMANOWSKI OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 The views and conclusions contained in the software and documentation are those of the
 authors.
 */
 
 
 package com.pwnetics.helper;
 
 import java.util.Map;
 
 
 /**
  * An {@link ItemCounter} that caches values for some of the moderately computationally expensive method calls.
  *
  * <p>On the first call to a cacheable method, or whenever the counter has changed, the original expensive method will be called.
  * Thereafter, until the counter is changed, the cached value will be returned.
  * </p>
  *
  * <p>While it is possible to use this class as a mutable item counter, every {@link #set(Object, int)} or {@link #increment(Object)}
  * operation incurs a cache-invalidation cost.
 * The recommended usage is to build counts using {@link ItemCounter}, then use {@link #build(ItemCounter, boolean)} to produce an object
  * of this class to analyze the counts.
  * </p>
  *
  * @author romanows
  *
  * @param <K> the type of object being counted
  */
 public class CachingItemCounter<K> extends ItemCounter<K> {
 
 	private boolean isSomethingCached;
 	private long sum;
 	private KeyValuePair min;
 	private KeyValuePair max;
 	private double mean;
 	private double variance;
 	private double variancePopulation;
 
 
 	/**
 	 * Factory method, recommended way to create a {@link CachingItemCounter}.
 	 * One can quickly build a count of items using the non-caching {@link ItemCounter}, then use this to create a {@link CachingItemCounter} with which to perform analysis.
 	 *
 	 * <p>Counts data is shared with the given ItemCounter, which can be a source of bugs.
 	 * Modifications to the given item counter can cause the caching item counter to return inconsistent values.
 	 * To guard against this, one can set isCopyingCount to true, which makes the caching item counter independent from the given item counter.
 	 * </p>
 	 *
 	 * @param ic ItemCounter to "convert" to a {@link CachingItemCounter}
 	 * @param isCopyingCount if false, references the internal state of the given item counter; if true, will become independent of the future operations performed on the given item counter.
 	 *   Setting this to "true" is safest, but does incur a memory storage and copy cost.
	 * @return a new instance of a caching item counter that is either a dependent view or an independent snapshot of the given item counter
 	 */
 	public static <K> CachingItemCounter<K> build(ItemCounter<K> ic, boolean isCopyingCount) {
 		return new CachingItemCounter<K>(ic.count, isCopyingCount);
 	}
 
 	/**
 	 * Constructor.
 	 * Consider using {@link #build(ItemCounter, boolean)}, instead.
 	 */
 	public CachingItemCounter() {
 		super();
 		invalidate();
 	}
 
 	protected CachingItemCounter(Map<K, Integer> count, boolean isCopyingCount) {
 		super(count, isCopyingCount);
 		invalidate();
 	}
 
 	/** Marks all cacheable values as invalid. */
 	protected void invalidate() {
 		sum = -1L;
 		min = null;
 		max = null;
 		mean = -1.0;
 		variance = -1.0;
 		variancePopulation = -1.0;
 		isSomethingCached = false;
 	}
 
 	/**
 	 * Loads all cacheable values, so this is fairly computationally expensive.
 	 * However, future calls to cacheable methods won't incur an initial performance hit, so long as the item counter is not modified after calling this cacheAll() method.
 	 */
 	public void cacheAll() {
 		if(sum < 0) {
 			sum();
 		}
 		if(min == null) {
 			min();
 		}
 		if(max == null) {
 			max();
 		}
 		if(mean < 0) {
 			mean();
 		}
 		if(variance < 0) {
 			variance();
 		}
 		if(variancePopulation < 0) {
 			variancePopulation();
 		}
 	}
 
 	@Override
 	public void set(K item, int count) {
 		if(isSomethingCached) {
 			invalidate();
 		}
 		super.set(item, count);
 	}
 
 	@Override
 	public int increment(K item) {
 		if(isSomethingCached) {
 			invalidate();
 		}
 		return super.increment(item);
 	}
 
 	@Override
 	public long sum() {
 		if(sum < 0) {
 			sum = super.sum();
 			isSomethingCached = true;
 		}
 		return sum;
 	}
 
 	@Override
 	public KeyValuePair min() {
 		if(min == null) {
 			min = super.min();
 			isSomethingCached = true;
 		}
 		return min;
 	}
 
 	@Override
 	public KeyValuePair max() {
 		if(max == null) {
 			max = super.max();
 			isSomethingCached = true;
 		}
 		return max;
 	}
 
 	@Override
 	public double mean() {
 		if(mean < 0) {
 			mean = super.mean();
 			isSomethingCached = true;
 		}
 		return mean;
 	}
 
 	@Override
 	public double variance() {
 		if(variance < 0) {
 			variance = super.variance();
 			isSomethingCached = true;
 		}
 		return variance;
 	}
 
 	@Override
 	public double variancePopulation() {
 		if(variancePopulation < 0) {
 			variancePopulation = super.variancePopulation();
 			isSomethingCached = true;
 		}
 		return variancePopulation;
 	}
 
 	@Override
 	public ItemCounter<K> asUnmodifiable() {
 		return new UnmodifiableCachingItemCounter(this, false);
 	}
 
 	/**
 	 * Wraps the item counter and prevents modification, although the backing item counter can still be modified.
 	 * @author romanows
 	 */
 	protected class UnmodifiableCachingItemCounter extends CachingItemCounter<K> {
 		public UnmodifiableCachingItemCounter(CachingItemCounter<K> itemCounter, boolean isCopyingCount) {
 			super(itemCounter.count, isCopyingCount);
 			sum = itemCounter.sum;
 			min = itemCounter.min;
 			max = itemCounter.max;
 			mean = itemCounter.mean;
 			variance = itemCounter.variance;
 			variancePopulation = itemCounter.variancePopulation;
 		}
 
 		@Override
 		public void set(K item, int count) {
 			throw new UnsupportedOperationException();
 		}
 
 		@Override
 		public int increment(K item) {
 			throw new UnsupportedOperationException();
 		}
 	}
 }
