 package com.jloisel.collection.map;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ForwardingIterator;
 import com.jloisel.collection.map.persistence.Persistence;
 
 final class PersistentMapSetIterator<E> extends ForwardingIterator<E> {
 	private final Iterator<E> delegate;
 	private final Persistence persistence;
 	private final Function<E, String> toKey;
 
 	private E current = null;
 
 	PersistentMapSetIterator(final Iterator<E> delegate, final Persistence persistence, final Function<E, String> toKey) {
 		super();
 		this.delegate = checkNotNull(delegate);
 		this.persistence = checkNotNull(persistence);
 		this.toKey = checkNotNull(toKey);
 	}
 
 	@Override
 	public E next() {
 		current = super.next();
 		return current;
 	}
 
 	@Override
 	public void remove() {
 		try {
 			super.remove();
 		} finally {
 			try {
 				persistence.remove(toKey.apply(current));
 			} catch (final IOException e) {
 				// ignored
 			}
 		}
 	}
 
 	@Override
 	protected Iterator<E> delegate() {
 		return delegate;
 	}
 }
