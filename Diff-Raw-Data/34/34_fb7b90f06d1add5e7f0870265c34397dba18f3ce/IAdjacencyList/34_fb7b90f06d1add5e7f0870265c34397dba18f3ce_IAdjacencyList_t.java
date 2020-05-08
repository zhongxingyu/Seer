 /*
   Copyright (C) 2012-2013 the original author or authors.
 
   See the LICENSE.txt file distributed with this work for additional
   information regarding copyright ownership.
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
   http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
 
 package jdeps;
 
 import java.util.List;
 
 /**
  * An adjacency list is essentially an array of size n where A[i] is the list of out-neighbors of node i.
  *
 * Please see <a href="http://www.cs.cmu.edu/~avrim/451f08/lectures/lect1002.pdf">http://www.cs.cmu.edu/~avrim/451f08/lectures/lect1002.pdf</a>
  *
  * @param <TVertex> Type of {@link IVertex}.
  */
 public interface IAdjacencyList<TVertex extends IVertex> extends Iterable<IAdjacencyListPair<TVertex>> {
   /**
    * Determines if the adjacency list is empty.
    *
    * @return <code>true</code> if the adjacency list is empty; <code>false</code> otherwise.
    */
   boolean isEmpty();
 
   /**
    * Gets the list of out-neighbors for a given vertex of <code>TVertex</code>.
    *
    * @param vertex A vertex of <code>TVertex</code> for whom we wish to retrieve its out-neighbors.
    * @return A list of <code>TVertex</code> instances who are the out-neighbors for the provided vertex argument.
    *
    * @see jdeps.IAdjacencyListPair#getOutNeighbors()
    */
   List<TVertex> outNeighborsFor(TVertex vertex);
 
   /**
    * Retrieves an instance of {@link IAdjacencyListPair} at the specified index.
    *
    * @param index The index in the list for which we wish to get the associated {@link IAdjacencyListPair}.
    * @return An instance of {@link IAdjacencyListPair} if found; otherwise <code>null</code>.
    *
    * @see IAdjacencyListPair
    */
   IAdjacencyListPair<TVertex> pairAt(int index);
 
   /**
    * Gets the list of out-neighbors for a given vertex of <code>TVertex</code> at the provided index argument.
    *
    * @param index The index in the list for which we wish to get the associated out-neighbors.
    * @return A list of <code>TVertex</code> instances who are the out-neighbors for an instance of <code>TVertex</code>
    *         at the specified index.
    *
    * @see jdeps.IAdjacencyListPair#getOutNeighbors()
    */
   List<TVertex> outNeighborsAt(int index);
 
   /**
    * Find the index in the {@link IAdjacencyList} at which the provided vertex argument can be found.
    *
    * @param vertex An instance of {@link IVertex} of <code>TVertex</code>.
    * @return The index in the {@link IAdjacencyList} at which the provided vertex argument can be found.
    *         <code>-1</code> if the vertex cannot be found.
    */
   int indexOf(TVertex vertex);
 
   /**
    * Scans the {@link IAdjacencyList} and for each vertex it counts the number of other vertices referencing it.
    *
    * @return An int array of size {@link jdeps.IAdjacencyList#size()} representing the in-degrees for each vertex.
    */
   int[] calculateInDegrees();
 
   /**
    * The size of the {@link IAdjacencyList}. This is the same as the number of vertices in the {@link IGraph}.
    *
    * @return An int representing the size of the {@link IAdjacencyList}.
    */
   int size();
 }
