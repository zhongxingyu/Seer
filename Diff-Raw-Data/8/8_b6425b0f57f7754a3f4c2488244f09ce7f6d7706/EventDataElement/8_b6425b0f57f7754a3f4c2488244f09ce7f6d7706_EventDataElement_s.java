 /* 
  * Copyright 2008-2009 the original author or authors.
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  */
  
 package com.mtgi.analytics;
 
 import java.io.ObjectStreamException;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 /**
  * Semi-structured (XML-like) data about a {@link BehaviorEvent}.
  * 
  * @see BehaviorEvent#getData()
  */
 public class EventDataElement extends DataLink<EventDataElement> implements Serializable {
 
 	private static final long serialVersionUID = -7479072851562747744L;
 	
 	private String name;
 	private String text;
 
 	//simple linked-list structure for child data elements, which has proven
 	//faster than java collections in hitting our performance test targets
 	private EventDataElement firstChild = ChildListHead.INSTANCE;
 	private EventDataElement lastChild = ChildListHead.INSTANCE;
 
 	//a flat linked list for properties.
 	private Property firstProperty = PropertyListHead.INSTANCE;
 	private Property lastProperty = PropertyListHead.INSTANCE;
 	
 	public EventDataElement(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Get the name of this element
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Get the text of this element, if any
 	 */
 	public String getText() {
 		return text;
 	}
 	
 	public void setText(String text) {
 		this.text = text;
 	}
 	
 	/**
 	 * Set a named attribute on this element.  Values may be null.  Calling
 	 * this method multiple times with the same name will result in previous values
 	 * being overwritten.
 	 */
 	public void add(String name, Object value) {
 		//just append for speed -- we compact to a unique set of names
 		//when iterateProperties() is called.
 		lastProperty.setNext(this, new Property(name, value));
 	}
 
 	/**
 	 * Add a child element with the given name to this element.
 	 */
 	public EventDataElement addElement(String name) {
 		EventDataElement ret = new EventDataElement(name);
 		lastChild.setNext(this, ret);
 		return ret;
 	}
 	
 	public void addElement(EventDataElement child) {
 		lastChild.setNext(this, child);
 	}
 
 	/** @return true if this element has no child properties or child elements */
 	public boolean isEmpty() {
 		return text == null && firstProperty == PropertyListHead.INSTANCE && firstChild == ChildListHead.INSTANCE;
 	}
 	
 	/** @return true if this element is effectively null (should not be rendered in serialized output) */
 	public boolean isNull() {
 		return false;
 	}
 	
 	/** iterate all properties previously added with {@link #add(String, Object)} */
 	public Iterator<? extends Map.Entry<String,Object>> iterateProperties() {
 		return firstProperty.compact().iterate();
 	}
 
 	/** 
 	 * iterate all child elements of this event data element previously added with
 	 * {@link #addElement(EventDataElement)} or {@link #addElement(String)} 
 	 */
 	public Iterator<EventDataElement> iterateChildren() {
 		return firstChild.iterate();
 	}
 	
 	/** set the next sibling in the linked list of children under <code>parent</code>. */
 	protected void setNext(EventDataElement parent, EventDataElement next) {
 		parent.lastChild = this.next = next;
 	}
 	/** iterate the list of sibling event data elements starting at this element */
 	protected Iterator<EventDataElement> iterate() {
 		return new DataLinkIterator<EventDataElement>(this);
 	}
 	
 	/**
 	 * Return a concrete, fully-realized instance of this data, performing any deferred initialization
 	 * of internal data structures.  This will likely be called many times for complex events, so it should
 	 * return quickly.  This implementation simply returns "this".
 	 * @param event the parent event
 	 */
 	protected EventDataElement initialize(BehaviorEvent event) {
 		return this;
 	}
 	
 	private static class ChildListHead extends ImmutableEventDataElement {
 		
 		private static final long serialVersionUID = 1511666816688289823L;
 		
 		static final ChildListHead INSTANCE = new ChildListHead();
 		
 		private ChildListHead() {
 			super("");
 		}
 		
 		@Override
 		protected void setNext(EventDataElement parent, EventDataElement next) {
 			//replace head/tail reference on parent with the provided real data element.
 			parent.firstChild = parent.lastChild = next;
 		}
 		
 		@Override
 		public Iterator<EventDataElement> iterate() {
 			return Collections.<EventDataElement>emptyList().iterator();
 		}
 		
 		private Object readResolve() throws ObjectStreamException {
 			return ChildListHead.INSTANCE;
 		}
 	}
 
 	private static class PropertyListHead extends Property {
 		
 		public static final Property INSTANCE = new PropertyListHead();
 
 		private PropertyListHead() {
 			super(null, null);
 		}
 
 		@Override
 		protected void setNext(EventDataElement parent, Property next) {
 			//replace head / tail elements on parent with the provided link.
 			parent.firstProperty = parent.lastProperty = next;
 		}
 
 		@Override
 		protected Iterator<Property> iterate() {
 			return Collections.<Property>emptyList().iterator();
 		}
 		
 		@Override
 		protected Property compact() {
 			return this;
 		}
 	}
 	
 	private static class Property extends DataLink<Property> 
		implements Map.Entry<String, Object> 
 	{
 		private String key;
 		private Object value;
 
 		protected Property(String key, Object value) {
 			this.key = key;
 			this.value = value;
 		}
 		
 		protected void setNext(EventDataElement parent, Property next) {
 			parent.lastProperty = this.next = next;
 		}
 		
 		public String getKey() {
 			return key;
 		}
 
 		public Object getValue() {
 			return value;
 		}
 
 		public Object setValue(Object value) {
 			Object ret = this.value;
 			this.value = value;
 			return ret;
 		}
 
 		protected Iterator<Property> iterate() {
 			return new DataLinkIterator<Property>(this);
 		}
 		
 		/**
 		 * Eliminate any duplicate keys from the linked list starting at this
 		 * link, returning a reference to the head of the compacted list
 		 * for convenient chaining of method calls.
 		 */
 		protected Property compact() {
 			HashMap<String,Property> uniq = new HashMap<String,Property>();
 			for (Property link = this, prev = null; 
 				 link != null; 
 				 prev = link, link = link.next) {
 				Property prior = uniq.get(link.key);
 				if (prior == null)
 					uniq.put(link.key, this);
 				else {
 					//previous link with same key; overwrite the
 					//value in the older entry and delete this link.
 					prior.value = link.value;
 					prev.next = link.next;
 					link = prev;
 				}
 			}
 			return this;
 		}
 	}
 }
 
 /**
  * base class for a singly-linked list, with an iterator implementation.  provided
  * for a measurable performance improvement over standard java collections.
  */
 class DataLink<T extends DataLink<T>> {
 	
 	protected T next;
 	
 	static class DataLinkIterator<T extends DataLink<T>> implements Iterator<T> {
 
 		private T current;
 		
 		protected DataLinkIterator(T head) {
 			this.current = head;
 		}
 		
 		public boolean hasNext() {
 			return current != null;
 		}
 
 		public T next() {
 			if (current == null)
 				throw new NoSuchElementException();
 			T ret = current;
 			current = ret.next;
 			return ret;
 		}
 
 		public void remove() {
 			throw new UnsupportedOperationException();
 		}
 		
 	}
 	
 }
