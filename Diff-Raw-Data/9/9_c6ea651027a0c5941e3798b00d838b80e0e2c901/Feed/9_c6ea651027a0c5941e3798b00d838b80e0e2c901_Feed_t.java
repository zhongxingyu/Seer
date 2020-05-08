 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.demo;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.dada.core.AbstractModel;
 import org.dada.core.Metadata;
 import org.dada.core.Range;
 import org.dada.core.Update;
 
 public class Feed<K, V> extends AbstractModel<K, V> {
 
 	// Feed
 
 	public interface Strategy<K, V> {
 
 		K getKey(V item);
 		Collection<V> createNewValues(Range<K> range);
 		V createNewVersion(V original);
 
 	}
 
 	protected final Map<K, V> vs = new HashMap<K, V>();
	protected final List<K> ks = new ArrayList<K>(); 
 	protected final Range<K> range;
 	protected final long delay;
 	protected final Strategy<K, V> strategy;
 	protected final Timer timer = new Timer();
 	protected final TimerTask task = new TimerTask() {
 
 		@Override
 		public void run() {
			int index = ((int)(ks.size() * Math.random()) - 1);
			K id  = ks.get(index);
 			V oldValue = vs.get(id);
 			V newValue = strategy.createNewVersion(oldValue);
 			vs.put(strategy.getKey(newValue), newValue);
 			logger.trace("{}: new version: {}", name, newValue);
 			notifyUpdate(empty, Collections.singleton(new Update<V>(oldValue, newValue)), empty);
 		}
 	};
 
 	public Feed(String name, Metadata<K, V> metadata, Range<K> range, long delay, Strategy<K, V> feedStrategy) {
 		super(name, metadata);
 		this.range = range;
 		this.delay = delay;
 		this.strategy = feedStrategy;
 	}
 
 	// Lifecycle
 
 	// IDEAS
 	// get range stuff going
 	// use topics as well as queues
 	// reference count listeners only
 	// make void invocations async - no return value
 
 	protected Collection<Update<V>> empty = new ArrayList<Update<V>>();
 
 	@Override
 	public void start() {
 		logger.info("creating values...");
 		Collection<V> newValues = strategy.createNewValues(range);
 		Collection<Update<V>> insertions = new ArrayList<Update<V>>();
 		for (V newValue : newValues) {
 			vs.put(strategy.getKey(newValue), newValue);
 			insertions.add(new Update<V>(null, newValue));
 		}
		ks.addAll(vs.keySet());
 		logger.info("notifying {} values...", newValues.size());
 		long start = System.currentTimeMillis();
 		notifyUpdate(insertions, empty, empty);
 		//for (Update<V> insertion : insertions)
 		//	notifyUpdates(Collections.singleton(insertion), empty, empty);
 		long end = System.currentTimeMillis();
 		logger.info("{} values in {} millis", insertions.size(), end - start);
 		logger.info("starting timer...");
 		timer.scheduleAtFixedRate(task, 0, delay);
 	}
 
 	@Override
 	public void stop() {
 		timer.cancel();
 	}
 
 	// Model
 
 	@Override
 	public Collection<V> getData() {
 		return new ArrayList<V>(vs.values());
 	}
 
 }
