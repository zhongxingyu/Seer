 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is S23M.
  *
  * The Initial Developer of the Original Code is
  * The S23M Foundation.
  * Portions created by the Initial Developer are
  * Copyright (C) 2012 The S23M Foundation.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Jorn Bettin
  * ***** END LICENSE BLOCK ***** */
 package org.s23m.cell.properclasses;
 
 import org.s23m.cell.Set;
 import org.s23m.cell.api.EventListener;
import org.s23m.cell.api.computation.Computation;
 
 public interface ProperClassOfGraphs extends EventListener, Computation {
 
 	/**
 	 * COMMANDS
 	 */
 
 	/**
 	 * Adds an abstract Vertex element with a specific category to the {@link Instance}.
 	 * Rule: valid only if the category is an element of the category
 	 * 
 	 * @param category
 	 * @param semanticIdentity
 	 * @return the resulting element
 	 */
 	Set addAbstract(Set category, Set semanticIdentity);
 
 	/**
 	 * Adds a concrete Vertex element with a specific category to the {@link Instance}.
 	 * Rule: valid only if the category is an element of the category
 	 * 
 	 * @param category
 	 * @param semanticIdentity
 	 * @return the resulting element
 	 */
 	Set addConcrete(Set category, Set semanticIdentity);
 
 	Set addToCommands(Set anElement);
 
 	Set addToQueries(Set anElement);
 
 	/**
 	 * Add the provided {@link Set} as a value to the set of values within this {@link Set}
 	 * 
 	 * @param set
 	 */
 	Set addToValues(Set set);
 
 	/**
 	 * Add the provided {@link Set} to the set of variables of this {@link Set}
 	 * 
 	 * @param set
 	 */
 	Set addToVariables(Set set);
 	Set allowableEdgeCategories(Set OrderedPair);
 	Set and(Set b);
 	/**
 	 * assignNewName(newName)
 	 * assigns a new name to a semantic identity
 	 *
 	 * this operation returns is_NOTAPPLICABLE for all sets that are not semantic identities
 	 */
 	Set assignNewName(String newName);
 	Set assignNewPayload(String newPayload);
 	/**
 	 * assignNewPluralName(newName)
 	 * assigns a new plural name to a semantic identity
 	 *
 	 * this operation returns is_NOTAPPLICABLE for all sets that are not semantic identities
 	 */
 	Set assignNewPluralName(String newPluralName);
 	Set commands();
 	Set complement(Set set);
 
 	/**
 	 * QUERIES
 	 */
 
 	/**
 	 * The container {@link Set} of which this container is part of.
 	 * 
 	 * The {@link #container()} is the place where the elements visible to the
 	 * {@link Set} are described via visibility links
 	 * 
 	 * @return the container container
 	 */
 	Set container();
 
 	/**
 	 * containsDecommissionedSet.isEqualTo(is_TRUE)
 	 * indicates that a client has decommissioned at least one set contained in the container,
 	 *
 	 * containsDecommissionedSet.isEqualTo(is_FALSE)
 	 * indicates that a client has not decommissioned any of the sets contained in the container,
 	 */
 	Set containsDecommissionedSets();
 
 	/**
 	 * Indicates whether this {@link Set} contains an Edge
 	 * {@link Set} <code>property</code>
 	 * 
 	 * @param orderedPair
 	 * @return <code>true</code> if the {@link Set} contains an Edge
 	 * {@link Set} <code>orderedPair</code>, and <code>false</code> otherwise
 	 */
 	Set containsEdgeTo(Set orderedPair);
 
 	/**
 	 * containsNewSets.isEqualTo(is_TRUE)
 	 * indicates that a client has added at least one set to the container,
 	 *
 	 * containsNewSets.isEqualTo(is_FALSE)
 	 * indicates that a client has not added any set to the container,
 	 */
 	Set containsNewSets();
 
 	/**
 	 * Attempts to remove this {@link Set} from its container() {@link Set}.
 	 * 
 	 * @return CoreSets.successful if the {@link Set} was successfully
 	 * decommissioned (referential integrity was not violated); otherwise, returns
 	 * the dependent {@link Set}s which need to be decommissioned first.
 	 */
 	Set decommission();
 	Set decommissionPayload();
 
 	/**
 	 * Indicates whether this {@link Set} is a generalization of
 	 * {@link Set} <code>orderedPair</code>
 	 * 
 	 * @param orderedPair
 	 * @return the generalization {@link Set}
 	 */
 	Set directSuperSetOf(Set orderedPair);
 
 	Set executableCommands();
 	Set executableQueries();
 
 	/**
 	 * Retrieves all instances of a given category that are contained in this instance
 	 * 
 	 * @param category
 	 * @return the resulting filtered list {@link Set}
 	 */
 	Set filter(Set category);
 
 	/**
 	 * Retrieves the proper class of the instance
 	 * 
 	 * @param properClass
 	 * @return the resulting filtered list {@link Set}
 	 */
 	Set filterProperClass(Set properClass);
 
 	/**
 	 * Retrieves the set of all instances that are contained in this instance
 	 * 
 	 * @return the resulting list {@link Set}
 	 */
 	Set filterInstances();
 
 	/**
 	 * Retrieves the set of all arrows between Instances that are contained in this instance
 	 * 
 	 * @return the resulting list {@link Set}
 	 */
 	Set filterArrows();
 
 	/**
 	 * Retrieves a filtered set of arrows between Instances that are contained in this instance
 	 * 
 	 * @return the resulting list {@link Set}
 	 */
 	Set filterArrows( Set properSetOrCategory,  Set fromSet,  Set toSet);
 
 	/**
 	 * Retrieves all instances of a given category or a subset of that category from the instances contained within this instance
 	 * 
 	 * @param category
 	 * @return the resulting filtered list {@link Set}
 	 */
 	Set filterPolymorphic(Set category);
 
 	Set hasDecommissionedPayload();
 
 	/**
 	 * hasNewName.isEqualTo(is_TRUE)
 	 * indicates that a client has changed the name of the semantic identity
 	 *
 	 * hasNewName.isEqualTo(is_FALSE)
 	 * indicates that no client has changed the name of the semantic identity
 	 *
 	 * this operation returns is_NOTAPPLICABLE for all sets that are not semantic identities
 	 */
 	Set hasNewName();
 
 	/**
 	 * hasNewPayload.isEqualTo(is_TRUE)
 	 * indicates that a client has changed the payload of the semantic identity
 	 *
 	 * hasNewPayload.isEqualTo(is_FALSE)
 	 * indicates that no client has changed the payload of the semantic identity
 	 *
 	 * this operation returns is_NOTAPPLICABLE for all sets that are not semantic identities
 	 */
 
 	Set hasNewPayload();
 
 	/**
 	 * hasNewPluralName.isEqualTo(is_TRUE)
 	 * indicates that a client has changed the plural name of the semantic identity
 	 *
 	 * hasNewPluralName.isEqualTo(is_FALSE)
 	 * indicates that no client has changed the plural name of the semantic identity
 	 *
 	 * this operation returns is_NOTAPPLICABLE for all sets that are not semantic identities
 	 */
 	Set hasNewPluralName();
 
 	/**
 	 * this has visibility of target,
 	 * i.e. it is permissible to arrow the Set to the target
 	 * via edge/superSetReference connections
 	 */
 	Set hasVisibilityOf(Set target);
 
 	Set includesValue( Set value, Set equivalenceClass);
 
 	Set intersection(Set set);
 
 	boolean is_FALSE();
 	boolean is_NOTAPPLICABLE();
 	boolean is_TRUE();
 
 	boolean is_UNKNOWN();
 
 	/**
 	 * isDecommissioned.isEqualTo(is_TRUE)
 	 * indicates that the container has been decommissioned by a client
 	 *
 	 * isDecommissioned.isEqualTo(is_FALSE)
 	 * indicates that the container has not been decommissioned by a client
 	 */
 	Set isDecommissioned();
 
 	Set isElementOf(Set semanticIdentity);
 
 	Set isEqualTo(Set set, Set equivalenceClass);
 
 	Set isInformation();
 
 	/**
 	 * Indicates whether this {@link Set} is a local generalization of
 	 * {@link Set} <code>orderedPair</code>
 	 * 
 	 * @param orderedPair
 	 * @return <code>true</code> if the {@link Set} is a local generalization of
 	 * {@link Set} <code>orderedPair</code>, and <code>false</code> otherwise
 	 * @see #directSuperSetOf(Set)
 	 */
 	Set isLocalSuperSetOf(Set orderedPair);
 	/**
 	 * isNewInstance.isEqual(is_TRUE) indicates that the container has been instantiated by a client,
 	 * and that the container has not been reconstituted from the datastore
 	 *
 	 * isNewInstance.isEqual(is_FALSE) indicates that the container has been reconstituted from the datastore,
 	 * and that the container has not been instantiated by a client 	 *
 	 */
 	Set isNewInstance();
 	Set isQuality();
 	/**
 	 * Retrieves the generalization of a {@link Set}
 	 * 
 	 * @param orderedPair
 	 * @return the generalization {@link Set}
 	 */
 	Set isSuperSetOf(Set orderedPair);
 	/**
 	 * Retrieves the top-most generalization of orderedPair contained within the Set
 	 * (a recursive application of {@link #directSuperSetOf(Set)})
 	 * 
 	 * @param orderedPair
 	 * @return the root generalization {@link Set}
 	 */
 	Set localRootSuperSetOf(Set orderedPair);
 	Set not();
 	Set or(Set b);
 
 	Set queries();
 	Set removeFromCommands(Set anElement);
 	Set removeFromQueries(Set anElement);
 	Set union(Set set);
 	Set wrapInOrderedSet();
 
 	Set unionOfconnectingArrows( Set instance);
 
 	/**
 	 * Return the value associated with the supplied variable from this {@link Set}
 	 */
 	Set value(Set variable);
 
 	/**
 	 * Return the set of values from this {@link Set}
 	 */
 	Set values();
 
 	/**
 	 * Return contained variables from this {@link Set}
 	 */
 	Set variables();
 
 	/**
 	 * Retrieves the list of all instances
 	 * that are visible from a specific subgraph of this instance
 	 * 
 	 * @param subgraph
 	 * @return the list {@link Set}
 	 */
 	Set visibleInstancesForSubGraph(Set subgraph);
 
 }
