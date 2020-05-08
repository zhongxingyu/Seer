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
 package org.dada.core;
 
 import java.util.Collection;
 import java.util.concurrent.ConcurrentMap;
 
 import org.dada.slf4j.Logger;
 import org.dada.slf4j.LoggerFactory;
 
 // TODO - key should be parameter to create, not ctor...
 /**
  * Pretend to be a View and, when prodded, create a real one, delegate all extant invocations onto it
  * and replace ourselves in an upstream ConcurrentMap with it...
  *
  * @author jules
  *
  * @param <K>
  * @param <V>
  */
 public class LazyView<K, V> implements View<V> {
 
 	private static final Logger LOG = LoggerFactory.getLogger(LazyView.class);
 
 	private final ConcurrentMap<K, View<V>> map;
 	private final K key;
 	private final Factory<K, View<V>> factory;
 
 	private volatile View<V> view; // allocated lazily - MUST be volatile - see below
 
 	public LazyView(ConcurrentMap<K, View<V>> map, K key, Factory<K, View<V>> factory) {
 		this.map = map;
 		this.key = key;
 		this.factory = factory;
 	}
 
 	protected synchronized void init() throws Exception {
 		// IMPORTANT: double checked locking (method is sync) - OK because 'view' is volatile... - requires >=1.5 JVM
 		if (view == null) {
 			view = factory.create(key);
			map.replace(key, this, view);
 		}
 	}
 
 	@Override
 	public void update(Collection<Update<V>> insertions, Collection<Update<V>> alterations, Collection<Update<V>> deletions) {
 		try {
 			if (view == null) init(); // check VOLATILE field without locking - see comment above
 			view.update(insertions, alterations, deletions);
 		} catch (Exception e) {
 			LOG.error("problem creating new View: {}", e, key);
 		}
 	}
 
 }
