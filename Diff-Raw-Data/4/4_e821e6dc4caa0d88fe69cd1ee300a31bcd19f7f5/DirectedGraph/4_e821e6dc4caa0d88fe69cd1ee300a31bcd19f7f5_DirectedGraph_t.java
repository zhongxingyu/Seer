 /**
  * DirectedGraph.java
  * 
  * Copyright 2009, 2010 Jeffrey Finkelstein
  * 
  * This file is part of jmona.
  * 
  * jmona is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * jmona is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * jmona. If not, see <http://www.gnu.org/licenses/>.
  */
 package jmona.graph;
 
 /**
  * A directed graph.
  * 
  * @param <V>
  *          The type of vertex in this Graph.
  * @param <E>
  *          The type of edge in this Graph.
  * @author Jeffrey Finkelstein
  * @since 0.1
  */
 public interface DirectedGraph<V, E> extends Graph<V, E> {
   /**
    * Get the edge between the two specified vertices, directed from the source
    * vertex to the target vertex.
    * 
    * @param sourceVertex
    *          The source vertex of the edge to get.
    * @param targetVertex
    *          The target vertex of the edge to get.
    * @return The edge from the source vertex to the target vertex.
    */
   E edgeBetween(final V sourceVertex, final V targetVertex);

 }
