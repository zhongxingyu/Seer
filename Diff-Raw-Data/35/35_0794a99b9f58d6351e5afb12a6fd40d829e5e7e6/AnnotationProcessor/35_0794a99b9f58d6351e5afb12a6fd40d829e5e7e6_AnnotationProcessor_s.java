 /*
  Copyright (c) 2010, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.instrument.internal;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import net.sf.staccatocommons.collections.stream.Streams;
 import net.sf.staccatocommons.instrument.handler.AnnotationHandler;
import net.sf.staccatocommons.lang.Option;
 import net.sf.staccatocommons.lang.block.Block2;
 import net.sf.staccatocommons.lang.predicate.Predicate;
 
 /**
  * @author flbulgarelli
  * 
  */
class AnnotationProcessor<T extends AnnotationHandler> {
 
	private final Collection<T> handlers;
 
 	/**
 	 * Creates a new {@link AnnotationProcessor}
 	 */
 	public AnnotationProcessor() {
		this.handlers = new LinkedList<T>();
 	}
 
	public void processUsing(final Object[] annotations, Block2<Object, T> block) {
 		for (Object annotation : annotations)
			getHandler(annotation).ifDefined(block.apply(annotation));
 	}
 
	// FIXME not consistent with documentation, if more than one is found that can
	// process, the rest are ignored
	// TODO linear time, should use a multimap
	private Option<T> getHandler(final Object annotation) {
		return Streams.from(handlers).findOrNone(//
			new Predicate<T>() {
				public boolean eval(T argument) {
 					return argument.getSupportedAnnotationType().isAssignableFrom(annotation.getClass());
 				}
 			});
 	}
 
 	/** Adds a handler */
	public void addHandler(T handler) {
 		this.handlers.add(handler);
 	}
 
 }
