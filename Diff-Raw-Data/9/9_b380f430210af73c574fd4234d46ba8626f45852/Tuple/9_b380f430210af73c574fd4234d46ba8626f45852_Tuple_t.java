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
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import clojure.lang.Indexed;
 
 /**
  * Temporary class, needed whilst Clojure sequences are not Serializable - due to be remedied soon.
  * I will use this for compound keys etc which must be Comparable (unlike standard Java Collections) and Serializable (unlike current Clojure sequences).
  * We also implement Indexed, so we can be deconstructed in Clojure expressions e.g. (let [[a b c] (Tuple. 1 2 3)]...)
  *  
  * @author jules
  *
  * @param <V>
  */
 public class Tuple<V extends Comparable<V>> implements Collection<V>, Serializable, Indexed, Comparable<Tuple<V>> {
 	
 	private final List<V> values;
 	
 	public Tuple(V... values) {
 		this.values = new ArrayList<V>(values.length);
 		for (V value : values) this.values.add(value);
 	}
 
 	public Tuple(Collection<V> values) {
 		this.values = new ArrayList<V>(values);
 	}
 	
 	public Tuple(V value1) {
 		this.values = new ArrayList<V>(1);
 		values.add(value1);
 	}
 	
 	public Tuple(V value1, V value2) {
 		this.values = new ArrayList<V>(2);
 		values.add(value1);
 		values.add(value2);
 	}
 	
 	public Tuple(V value1, V value2, V value3) {
 		this.values = new ArrayList<V>(3);
 		values.add(value1);
 		values.add(value2);
 		values.add(value3);
 	}
 	
 	public Tuple(V value1, V value2, V value3, V value4) {
 		this.values = new ArrayList<V>(4);
 		values.add(value1);
 		values.add(value2);
 		values.add(value3);
 		values.add(value4);
 	}
 	
 	public Tuple(V value1, V value2, V value3, V value4, V value5) {
 		this.values = new ArrayList<V>(5);
 		values.add(value1);
 		values.add(value2);
 		values.add(value3);
 		values.add(value4);
 		values.add(value5);
 	}
 	
 	public Tuple(V value1, V value2, V value3, V value4, V value5, V value6) {
 		this.values = new ArrayList<V>(1);
 		values.add(value1);
 		values.add(value2);
 		values.add(value3);
 		values.add(value4);
 		values.add(value5);
 		values.add(value6);
 	}
 	
 	// Indexed ...
 	
 	@Override
 	public Object nth(int i) {
 		return values.get(i);
 	}
 
 	@Override
 	public int count() {
 		return values.size();
 	}
 
 	// Collection
 	
 	@Override
 	public boolean add(V e) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public boolean addAll(Collection<? extends V> c) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public void clear() {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public boolean contains(Object o) {
 		return values.contains(o);
 	}
 
 	@Override
 	public boolean containsAll(Collection<?> c) {
 		return values.containsAll(c);
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return values.isEmpty();
 	}
 
 	@Override
 	public Iterator<V> iterator() {
 		return values.iterator();
 	}
 
 	@Override
 	public boolean remove(Object o) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public boolean removeAll(Collection<?> c) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public boolean retainAll(Collection<?> c) {
 		throw new UnsupportedOperationException("Tuples are Immutable");
 	}
 
 	@Override
 	public int size() {
 		return values.size();
 	}
 
 	@Override
 	public Object[] toArray() {
 		return values.toArray();
 	}
 
 	@Override
 	public <T> T[] toArray(T[] a) {
 		return values.toArray(a);
 	}
 
 	// Comparable
 	
 	@Override
 	public int compareTo(Tuple<V> that) {
 		int diff = this.size() - that.size();
 		if (diff != 0)
 			return diff;
 		else {
 			int size = values.size();
 			for (int i = 0; i < size; i++) {
 				int comparison = this.values.get(i).compareTo(that.values.get(i));
 				if (comparison != 0) return comparison;
 			}
 			return 0;
 		}
 	}
 	
 	// Object
 	
 	@Override
 	public boolean equals(Object that) {
 		return this.values.equals(((Tuple<V>)that).values);
 	}
 
 	@Override
 	public int hashCode() {
	    // TODO: we might want to implement this for ourselves...
 		return values.hashCode();
 	}
 	
 	@Override
 	public String toString() {
 		return values.toString();
 	}
 	
 }
