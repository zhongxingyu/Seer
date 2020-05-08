 package org.muis.core.mgr;
 
 import java.util.IdentityHashMap;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.muis.core.*;
 import org.muis.core.event.MuisEvent;
 
 /** Manages attribute information for an element */
 public class AttributeManager {
 	/** Wraps an attribute and its metadata for this manager */
 	public class AttributeHolder {
 		private AttributeHolder theParent;
 
 		private final MuisAttribute<?> theAttr;
 
 		private IdentityHashMap<Object, Object> theNeeders;
 
 		private IdentityHashMap<Object, Object> theWanters;
 
 		private boolean wasWanted;
 
 		Object theValue;
 
 		AttributeHolder(MuisAttribute<?> attr) {
 			theAttr = attr;
 		}
 
 		AttributeHolder(MuisPathedAttribute<?> attr, AttributeHolder parent) {
 			theParent = parent;
 			theAttr = attr;
 		}
 
 		/** @return The attribute that this holder holds */
 		public MuisAttribute<?> getAttribute() {
 			return theAttr;
 		}
 
 		/** @return The value of the attribute in this manager */
 		public Object getValue() {
 			return theValue;
 		}
 
 		synchronized void addWanter(Object wanter, boolean isNeeder) {
 			if(theParent != null)
 				throw new IllegalStateException("Only root attributes can be wanted");
 			wasWanted = true;
 			IdentityHashMap<Object, Object> set = isNeeder ? theNeeders : theWanters;
 			if(set == null) {
 				set = new IdentityHashMap<>();
 				if(isNeeder)
 					theNeeders = set;
 				else
 					theWanters = set;
 			}
 			set.put(wanter, wanter);
 		}
 
 		/** @return Whether this attribute is required in this manager */
 		public boolean isRequired() {
 			if(theParent != null)
 				return false;
 			IdentityHashMap<Object, Object> needers = theNeeders;
 			return needers != null && !needers.isEmpty();
 		}
 
 		boolean isWanted() {
 			if(theParent != null)
 				return theParent.isWanted();
 			if(isRequired())
 				return true;
 			IdentityHashMap<Object, Object> wanters = theWanters;
 			return wanters != null && !wanters.isEmpty();
 		}
 
 		boolean wasWanted() {
 			if(theParent != null)
 				return theParent.wasWanted();
 			return wasWanted;
 		}
 
 		synchronized void unrequire(Object wanter) {
 			if(theParent != null)
 				throw new IllegalStateException("Non-root attributes cannot be unrequired");
 			if(theNeeders != null)
 				theNeeders.remove(wanter);
 			if(theWanters == null)
 				theWanters = new IdentityHashMap<>();
 			theWanters.put(wanter, wanter);
 		}
 
 		synchronized void reject(Object rejecter) {
 			if(theParent != null)
 				throw new IllegalStateException("Non-root attributes cannot be rejected");
 			if(theNeeders != null)
 				theNeeders.remove(rejecter);
 			if(theWanters != null)
 				theWanters.remove(rejecter);
 		}
 
 		@Override
 		public String toString() {
 			return theAttr.toString() + (isRequired() ? " (required)" : " (optional)");
 		}
 	}
 
 	private ConcurrentHashMap<String, AttributeHolder> theAcceptedAttrs;
 
 	private ConcurrentHashMap<String, String> theRawAttributes;
 
 	private MuisElement theElement;
 
 	/** @param element The element to manage attribute information for */
 	public AttributeManager(MuisElement element) {
 		theAcceptedAttrs = new ConcurrentHashMap<>();
 		theRawAttributes = new ConcurrentHashMap<>();
 		theElement = element;
 		theElement.life().runWhen(new Runnable() {
 			@Override
 			public void run() {
 				setReady();
 			}
 		}, MuisConstants.CoreStage.STARTUP.toString(), 0);
 	}
 
 	/**
 	 * Sets an attribute typelessly
 	 *
 	 * @param attr The name of the attribute to set
 	 * @param value The string representation of the attribute's value
 	 * @return The parsed value for the attribute, or null if the element has not been initialized
 	 * @throws MuisException If the attribute is not accepted in the element, the value is null and the attribute is required, or the
 	 *             element has already been initialized and the value is not valid for the given attribute
 	 */
 	public final Object set(String attr, String value) throws MuisException {
 		AttributeHolder holder = theAcceptedAttrs.get(attr);
 		if(holder != null)
 			return set(holder.theAttr, value);
 		String baseName = attr;
 		int dotIdx = baseName.indexOf('.');
 		if(dotIdx >= 0)
 			baseName = baseName.substring(0, dotIdx);
 		holder = theAcceptedAttrs.get(baseName);
 		if(holder == null) {
 			if(theElement.life().isAfter(MuisConstants.CoreStage.STARTUP.toString()) >= 0)
 				throw new MuisException("Attribute " + attr + " is not accepted in this element");
 			if(value == null)
 				theRawAttributes.remove(attr);
 			else
 				theRawAttributes.put(attr, value);
 			return null;
 		}
 		if(holder.theAttr.getPathAccepter() == null)
 			throw new MuisException("Attribute " + attr + " is not hierarchical");
 		String [] path = attr.substring(dotIdx + 1).split("\\.");
 		if(!holder.theAttr.getPathAccepter().accept(theElement, path))
 			throw new MuisException("Attribute " + attr + " does not accept path \"" + attr.substring(dotIdx + 1) + "\"");
 		return set(new MuisPathedAttribute<>(holder.theAttr, theElement, path), value);
 	}
 
 	/**
 	 * Sets the value of an attribute for the element. If the element has not been fully initialized (by {@link MuisElement#postCreate()},
 	 * the attribute's value will be validated and parsed during {@link MuisElement#postCreate()}. If the element has been initialized, the
 	 * value will be validated immediately and a {@link MuisException} will be thrown if the value is not valid.
 	 *
 	 * @param <T> The type of the attribute to set
 	 * @param attr The attribute to set
 	 * @param value The value for the attribute
 	 * @return The parsed value for the attribute, or null if the element has not been initialized
 	 * @throws MuisException If the attribute is not accepted in the element, the value is null and the attribute is required, or the
 	 *             element has already been initialized and the value is not valid for the given attribute
 	 */
 	public final <T> T set(MuisAttribute<T> attr, String value) throws MuisException {
 		T ret = attr.getType().parse(theElement.getClassView(), value, theElement.msg());
 		set(attr, ret);
 		return ret;
 	}
 
 	/**
 	 * Sets an attribute's type-correct value
 	 *
 	 * @param <T> The type of the attribute to set
 	 * @param attr The attribute to set
 	 * @param value The value to set for the attribute in this element
 	 * @throws MuisException If the attribute is not accepted in this element or the value is not valid
 	 */
 	public final <T> void set(MuisAttribute<T> attr, T value) throws MuisException {
 		if(theRawAttributes != null)
 			theRawAttributes.remove(attr.getName());
 		AttributeHolder holder = theAcceptedAttrs.get(attr.getName());
 		if(holder == null) {
 			if(attr instanceof MuisPathedAttribute) {
 				MuisPathedAttribute<T> pathed = (MuisPathedAttribute<T>) attr;
 				holder = theAcceptedAttrs.get(pathed.getBase().getName());
 				if(holder != null) {
 					if(!holder.theAttr.equals(pathed.getBase()))
 						throw new MuisException("A different attribute named " + pathed.getBase().getName()
 							+ " is already accepted or set in this element");
 				} else {
 					if(theElement.life().isAfter(MuisConstants.CoreStage.STARTUP.toString()) >= 0)
 						throw new MuisException("Attribute " + attr + " is not accepted in this element");
 					holder = new AttributeHolder(pathed.getBase());
 					theAcceptedAttrs.put(pathed.getBase().getName(), holder);
 				}
 				AttributeHolder pathedHolder = new AttributeHolder(pathed, holder);
 				theAcceptedAttrs.put(pathed.getName(), pathedHolder);
 				holder = pathedHolder;
 			} else {
 				if(theElement.life().isAfter(MuisConstants.CoreStage.STARTUP.toString()) >= 0)
 					throw new MuisException("Attribute " + attr + " is not accepted in this element");
 				holder = new AttributeHolder(attr);
 				theAcceptedAttrs.put(attr.getName(), holder);
 				holder.theValue = value;
 				return;
 			}
 		}
 		if(!holder.theAttr.equals(attr))
 			throw new MuisException("A different attribute named " + attr.getName() + " is already accepted or set in this element");
 		if(value == null && holder.isRequired())
 			throw new MuisException("Attribute " + attr + " is required--cannot be set to null");
 		if(value != null) {
 			T newValue = attr.getType().cast(value);
 			if(newValue == null)
 				throw new MuisException("Value " + value + ", type " + value.getClass().getName() + " is not valid for atribute " + attr);
 			if(attr.getValidator() != null)
 				attr.getValidator().assertValid(value);
 		}
 		Object old = holder.theValue;
 		holder.theValue = value;
 		theElement.fireEvent(new MuisEvent<MuisAttribute<?>>(MuisConstants.Events.ATTRIBUTE_SET, attr), false, false);
 		theElement.fireEvent(new org.muis.core.event.AttributeChangedEvent<>(attr, attr.getType().cast(old), value), false, false);
 	}
 
 	/**
 	 * @param name The name of the attribute to get
 	 * @return The value of the named attribute
 	 */
 	public final Object get(String name) {
 		AttributeHolder holder = theAcceptedAttrs.get(name);
 		if(holder == null)
 			return null;
 		return holder.theValue;
 	}
 
 	/**
 	 * @param name The name of the attribute to check
 	 * @return Whether an attribute with the given name is set in this attribute manager
 	 */
 	public final boolean isSet(String name) {
 		AttributeHolder holder = theAcceptedAttrs.get(name);
 		if(holder != null && holder.theValue != null)
 			return true;
 		if(theRawAttributes != null && theRawAttributes.get(name) != null)
 			return true;
 		return false;
 	}
 
 	/**
 	 * @param attr The attribute to check
 	 * @return Whether a value is set in this attribute manager for the given attribute
 	 */
 	public final boolean isSet(MuisAttribute<?> attr) {
 		AttributeHolder holder = theAcceptedAttrs.get(attr.getName());
 		return holder != null && holder.getAttribute().equals(attr) && holder.getValue() != null;
 	}
 
 	/**
 	 * Gets the value of an attribute in this manager
 	 *
 	 * @param <T> The type of the attribute to get
 	 * @param attr The attribute to get the value of
 	 * @return The value of the attribute in this manager, or null if the attribute is not set
 	 */
 	public final <T> T get(MuisAttribute<T> attr) {
 		return get(attr, null);
 	}
 
 	/**
 	 * Gets the value of an attribute in this manager, returning a default value if the attribute is not set
	 *
 	 * @param <T> The type of the attribute to get
 	 * @param attr The attribute to get the value of
 	 * @param def The default value to return if the attribute is not set in this manager
 	 * @return The value of the attribute in this manager, or <code>def</code> if the attribute is not set
 	 */
 	public final <T> T get(MuisAttribute<T> attr, T def) {
 		AttributeHolder storedAttr = theAcceptedAttrs.get(attr.getName());
 		if(storedAttr == null)
 			return def;
		if(!storedAttr.theAttr.equals(attr) || storedAttr.theValue == null)
 			return def; // Same name, but different attribute
 		return (T) storedAttr.theValue;
 	}
 
 	/**
 	 * Specifies a required attribute for this element
 	 *
 	 * @param <T> The type of the attribute to require
 	 * @param <V> The type of the value for the attribute
 	 * @param needer The object that needs the attribute
 	 * @param attr The attribute that must be specified for this element
 	 * @param initValue The value to set for the attribute if a value is not set already
 	 * @throws MuisException If the given value is not acceptable for the given attribute
 	 */
 	public final <T, V extends T> void require(Object needer, MuisAttribute<T> attr, V initValue) throws MuisException {
 		accept(needer, true, attr, initValue);
 	}
 
 	/**
 	 * Specifies a required attribute for this element
 	 *
 	 * @param needer The object that needs the attribute
 	 * @param attr The attribute that must be specified for this element
 	 */
 	public final void require(Object needer, MuisAttribute<?> attr) {
 		try {
 			accept(needer, true, attr, null);
 		} catch(MuisException e) {
 			throw new IllegalStateException("Should not throw MuisException with null initValue");
 		}
 	}
 
 	/**
 	 * Marks an accepted attribute as not required
 	 *
 	 * @param wanter The object that cares about the attribute
 	 * @param attr The attribute to accept but not require
 	 */
 	public final void unrequire(Object wanter, MuisAttribute<?> attr) {
 		AttributeHolder holder = theAcceptedAttrs.get(attr.getName());
 		if(holder != null)
 			holder.unrequire(wanter);
 		else
 			accept(wanter, attr);
 	}
 
 	/**
 	 * Specifies an optional attribute for this element
 	 *
 	 * @param <T> The type of the attribute to accept
 	 * @param <V> The type of the value for the attribute
 	 * @param wanter The object that cares about the attribute
 	 * @param attr The attribute that may be specified for this element
 	 * @param initValue The value to set for the attribute if a value is not set already
 	 * @throws MuisException If the given value is not acceptable for the given attribute
 	 */
 	public final <T, V extends T> void accept(Object wanter, MuisAttribute<T> attr, V initValue) throws MuisException {
 		accept(wanter, false, attr, initValue);
 	}
 
 	/**
 	 * Specifies an optional attribute for this element
 	 *
 	 * @param wanter The object that cares about the attribute
 	 * @param attr The attribute that must be specified for this element
 	 */
 	public final void accept(Object wanter, MuisAttribute<?> attr) {
 		try {
 			accept(wanter, false, attr, null);
 		} catch(MuisException e) {
 			throw new IllegalStateException("Should not throw MuisException with null initValue");
 		}
 	}
 
 	/**
 	 * Sepcifies an optional or required attribute for this element
 	 *
 	 * @param <T> The type of the attribute to accept
 	 * @param <V> The type of the value for the attribute
 	 * @param wanter The object that cares about the attribute
 	 * @param require Whether the attribute should be required or optional
 	 * @param attr The attribute to accept
 	 * @param initValue The value to set for the attribute if a value is not set already
 	 * @throws MuisException If the given value is not acceptable for the given attribute
 	 */
 	public final <T, V extends T> void accept(Object wanter, boolean require, MuisAttribute<T> attr, V initValue) throws MuisException {
 		if(attr instanceof MuisPathedAttribute)
 			throw new IllegalArgumentException("Pathed attributes cannot be accepted or required");
 		if(require && initValue == null && theElement.life().isAfter(MuisConstants.CoreStage.STARTUP.toString()) > 0)
 			throw new IllegalStateException("Attributes may not be required without an initial value after an element is initialized");
 		AttributeHolder holder = theAcceptedAttrs.get(attr.getName());
 		if(holder != null) {
 			if(holder.theAttr.equals(attr))
 				holder.addWanter(wanter, require); // The attribute is already required
 			else
 				throw new IllegalStateException("An attribute named " + attr.getName() + " (" + holder.theAttr
 					+ ") is already accepted in this element");
 		} else {
 			holder = new AttributeHolder(attr);
 			holder.addWanter(wanter, require);
 			theAcceptedAttrs.put(attr.getName(), holder);
 			String strVal = theRawAttributes.remove(attr.getName());
 			if(strVal != null) {
 				try {
 					set((MuisAttribute<Object>) attr, attr.getType().parse(theElement.getClassView(), strVal, theElement.msg()));
 				} catch(MuisException e) {
 					theElement.msg().error("Could not parse pre-set value \"" + strVal + "\" of attribute " + attr.getName(), e,
 						"attribute", attr);
 				}
 			}
 		}
 		if(initValue != null && holder.theValue == null)
 			set(attr, initValue);
 	}
 
 	/**
 	 * Undoes acceptance of an attribute. This method does not remove any attribute value associated with this element. It merely disables
 	 * the attribute. If the attribute is accepted on this element later, this element's value of that attribute will be preserved.
 	 *
 	 * @param wanter The object that used to care about the attribute
 	 * @param attr The attribute to not allow in this element
 	 */
 	public final void reject(Object wanter, MuisAttribute<?> attr) {
 		if(attr instanceof MuisPathedAttribute)
 			throw new IllegalArgumentException("Pathed attributes cannot be rejected");
 		AttributeHolder holder = theAcceptedAttrs.get(attr.getName());
 		if(holder != null) {
 			holder.reject(holder);
 			if(!holder.isWanted())
 				theAcceptedAttrs.remove(attr.getName());
 		}
 	}
 
 	/** @return The number of attributes set for this element */
 	public final int size() {
 		return theAcceptedAttrs.size();
 	}
 
 	/**
 	 * @param attr The attribute to check
 	 * @return Whether the given attribute can be set in this element
 	 */
 	public final boolean isAccepted(MuisAttribute<?> attr) {
 		if(attr instanceof MuisPathedAttribute)
 			return isAccepted(((MuisPathedAttribute<?>) attr).getBase());
 		AttributeHolder holder = theAcceptedAttrs.get(attr.getName());
 		return holder != null && holder.theAttr.equals(attr);
 	}
 
 	/**
 	 * @param attr The attribute to check
 	 * @return Whether the given attribute is required in this element
 	 */
 	public final boolean isRequired(MuisAttribute<?> attr) {
 		if(attr instanceof MuisPathedAttribute)
 			return false;
 		AttributeHolder holder = theAcceptedAttrs.get(attr.getName());
 		return holder != null && !holder.theAttr.equals(attr) && holder.isRequired();
 	}
 
 	/** @return An iterable to iterate through all accepted attributes in this manager */
 	public Iterable<MuisAttribute<?>> attributes() {
 		return new Iterable<MuisAttribute<?>>() {
 			@Override
 			public Iterator<MuisAttribute<?>> iterator() {
 				return new Iterator<MuisAttribute<?>>() {
 					private final Iterator<AttributeHolder> theWrapped = holders().iterator();
 
 					@Override
 					public boolean hasNext() {
 						return theWrapped.hasNext();
 					}
 
 					@Override
 					public MuisAttribute<?> next() {
 						return theWrapped.next().getAttribute();
 					}
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException();
 					}
 				};
 			}
 		};
 	}
 
 	/** @return An iterable to iterate through the metadata of each accepted attribute in this manager */
 	public Iterable<AttributeHolder> holders() {
 		return new Iterable<AttributeHolder>() {
 			@Override
 			public Iterator<AttributeHolder> iterator() {
 				return new Iterator<AttributeHolder>() {
 					private final Iterator<AttributeHolder> theWrapped = theAcceptedAttrs.values().iterator();
 
 					private AttributeHolder theNext;
 
 					private boolean calledNext = true;
 
 					@Override
 					public boolean hasNext() {
 						if(!calledNext)
 							return theNext != null;
 						calledNext = false;
 						theNext = null;
 						while(theNext == null) {
 							if(!theWrapped.hasNext())
 								return false;
 							theNext = theWrapped.next();
 							if(!theNext.isWanted())
 								theNext = null;
 						}
 						return theNext != null;
 					}
 
 					@Override
 					public AttributeHolder next() {
 						if(calledNext && !hasNext())
 							return theWrapped.next();
 						calledNext = true;
 						if(theNext == null)
 							return theWrapped.next();
 						else
 							return theNext;
 					}
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException();
 					}
 				};
 			}
 		};
 	}
 
 	private void setReady() {
 		Iterator<AttributeHolder> holders = holders().iterator();
 		while(holders.hasNext()) {
 			AttributeHolder holder = holders.next();
 			if(!holder.wasWanted()) {
 				holders.remove();
 				theElement.msg().error("Attribute " + holder.getAttribute() + " is not accepted in this element", "value",
 					holder.getValue());
 			}
 		}
 		for(java.util.Map.Entry<String, String> attr : theRawAttributes.entrySet())
 			theElement.msg().error("No attribute named " + attr.getKey() + " is not accepted in this element", "value", attr.getValue());
 		theRawAttributes = null;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder ret = new StringBuilder();
 		org.jdom2.output.EscapeStrategy strategy = new org.jdom2.output.EscapeStrategy() {
 			@Override
 			public boolean shouldEscape(char ch) {
 				if(org.jdom2.Verifier.isHighSurrogate(ch)) {
 					return true; // Safer this way per http://unicode.org/faq/utf_bom.html#utf8-4
 				}
 				return false;
 			}
 		};
 		for(AttributeHolder holder : holders()) {
 			if(!holder.isWanted() || holder.getValue() == null)
 				continue;
 			if(ret.length() > 0)
 				ret.append(' ');
 			ret.append(holder.getAttribute().getName()).append('=');
 			String value;
 			if(holder.getAttribute().getType() instanceof MuisProperty.PrintablePropertyType)
 				value = "\""
 					+ org.jdom2.output.Format.escapeAttribute(strategy, ((MuisProperty.PrintablePropertyType<Object>) holder.getAttribute()
 						.getType()).toString(holder.getValue())) + "\"";
 			else
 				value = String.valueOf(holder.getValue());
 			ret.append(value);
 		}
 		return ret.toString();
 	}
 }
