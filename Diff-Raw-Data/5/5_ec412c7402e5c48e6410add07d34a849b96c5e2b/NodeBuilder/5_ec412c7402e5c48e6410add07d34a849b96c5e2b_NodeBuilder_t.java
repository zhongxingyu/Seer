 /*
  * Copyright 2012 Robert Philipp
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.freezedry.persistence.builders;
 
 import javax.sound.midi.MidiDevice.Info;
 
 import org.freezedry.persistence.PersistenceEngine;
 import org.freezedry.persistence.copyable.Copyable;
 import org.freezedry.persistence.tree.InfoNode;
 
 
 /**
  * Interface for the classes used to generate {@link InfoNode}s. These {@link NodeBuilder}s are used by
  * the {@link PersistenceEngine} to create {@link InfoNode} from {@link Object} and vice-versa. These
  * {@link NodeBuilder}s must fit in with the recursive method used by the {@link PersistenceEngine#createNode(Class, Object, String)}
  * method.
  *  
  * @author Robert Philipp
  * 
  * @see AbstractNodeBuilder
  */
 public interface NodeBuilder extends Copyable< NodeBuilder > {
 
 	/**
 	 * Generates an {@link InfoNode} from the specified {@link Object}. The specified containing {@link Class}
 	 * is the {@link Class} in which the specified field name lives. And the object is the value of
 	 * the field name. 
 	 * @param containingClass The {@link Class} that contains the specified field name
 	 * @param object The value of the field with the specified field name
 	 * @param fieldName The name of the field for which the object is the value
 	 * @return The constructed {@link InfoNode} based on the specified information
 	 * @throws ReflectiveOperationException
 	 */
 	InfoNode createInfoNode( final Class< ? > containingClass, final Object object, final String fieldName ) throws ReflectiveOperationException;
	
 	/**
 	 * Creates an object of the specified {@link Class} based on the information in the {@link InfoNode}. Note that
 	 * the {@link Info} may also contain type information about the class to generate. The specified {@link Class}
 	 * overrides that value. This is done to avoid modifying the {@link Info} tree when supplemental information becomes
 	 * available.
 	 * @param clazz The {@link Class} of the object to create 
 	 * @param node The information about the object to create
	 * @return The object constructed based on the info node.
 	 */
 	Object createObject( final Class< ? > containingClass, final Class< ? > clazz, final InfoNode node ) throws ReflectiveOperationException;
 	
 	/**
 	 * Sets the {@link PersistenceEngine} with which this {@link NodeBuilder} must work. Recall, that the
 	 * {@link #createInfoNode(Class, Object, String)} methods is called from within an recursive algorithm 
 	 * managed by the {@link PersistenceEngine#createNode(Class, Object, String)} method. The reference
 	 * to the {@link PersistenceEngine} is used to call be to that method for creating non-leaf nodes (this
 	 * essentially allows the recursion to continue).
 	 * @param engine The {@link PersistenceEngine} used to create the DOM model from the object
 	 */
 	void setPersistenceEngine( final PersistenceEngine engine );
 }
