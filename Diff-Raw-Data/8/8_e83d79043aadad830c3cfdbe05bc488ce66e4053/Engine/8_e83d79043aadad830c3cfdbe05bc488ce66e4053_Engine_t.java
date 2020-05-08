 package org.genericsystem.core;
 
 import org.genericsystem.generic.Attribute;
 import org.genericsystem.generic.Relation;
 import org.genericsystem.generic.Type;
 
 /**
  * Instance of class Engine represents a unit of persistence, but also the root of internal graph attached to it.
  * 
  * @author Nicolas Feybesse
  * @author Michael Ory
  */
 public interface Engine extends Type {
 
 	/**
 	 * Returns the Factory.
 	 * 
 	 * @see Factory
 	 * 
 	 * @return The Factory.
 	 */
 	Factory getFactory();
 
 	/**
 	 * Close engine and do a last snapshot if persistent.
 	 */
 	void close();
 
 	/**
 	 * Returns the meta attribute.
 	 * 
 	 * @return The meta attribute.
 	 */
 	<T extends Attribute> T getMetaAttribute();
 
 	/**
 	 * Returns the meta relation.
 	 * 
 	 * @return The meta relation.
 	 */
 	<T extends Relation> T getMetaRelation();
 
 	/**
 	 * @return A new cache for this engine.
 	 */
 	Cache newCache();
 
 	/**
 	 * Pick a new unique timestamp.
 	 * 
 	 * @return The timestamp.
 	 */
 	long pickNewTs();

	Cache getCurrentCache();
 }
