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
 
import net.sf.staccatocommons.collections.stream.Stream;
 import net.sf.staccatocommons.collections.stream.Streams;
 import net.sf.staccatocommons.instrument.handler.AnnotationHandler;
 import net.sf.staccatocommons.lang.block.Block2;
 import net.sf.staccatocommons.lang.predicate.Predicate;
 
 /**
  * @author flbulgarelli
  * 
  */
class AnnotationProcessor<HandlerType extends AnnotationHandler> {
 
	private final Collection<HandlerType> handlers;
 
 	/**
 	 * Creates a new {@link AnnotationProcessor}
 	 */
 	public AnnotationProcessor() {
		this.handlers = new LinkedList<HandlerType>();
 	}
 
	public void processUsing(final Object[] annotations, Block2<Object, HandlerType> block) {
 		for (Object annotation : annotations)
			for (HandlerType handler : getHandlers(annotation)) {
				block.exec(annotation, handler);
			}
 	}
 
	private Stream<HandlerType> getHandlers(final Object annotation) {
		return Streams.from(handlers).filter(//
			new Predicate<HandlerType>() {
				public boolean eval(HandlerType argument) {
 					return argument.getSupportedAnnotationType().isAssignableFrom(annotation.getClass());
 				}
 			});
 	}
 
 	/** Adds a handler */
	public void addHandler(HandlerType handler) {
 		this.handlers.add(handler);
 	}
 
 }
