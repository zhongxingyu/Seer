 /*
  * Copyright (c) 2002-2009 "Neo Technology,"
  *     Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  * 
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.index;
 
 import org.neo4j.graphdb.Node;
 
 /**
  * Index service to index nodes with a key and a value. The Neo4j Kernel has no
  * indexing features built-in. Instead you'll have to manage indexing manually.
  * IndexService is a means of providing those indexing capabilities for a Neo4j
  * graph and integrate it as tightly as possible with the graph engine.
 * 
  * See more at <a
  * href="http://wiki.neo4j.org/content/Indexing_with_IndexService"> The Neo4j
  * wiki page on "Indexing with IndexService"</a>.
  */
 public interface IndexService
 {
     /**
      * Index <code>node</code> with <code>key</code> and <code>value</code>. A
      * node can be associated with any number of key-value pairs.
      * <p>
      * Note about updating an index: If you've indexed a value from a property
      * on a {@link Node} and that value gets updated, you'll have to remove the
      * old value in addition to indexing the new value, else both values (the
      * new and the old) will be indexed for that node.
      * 
      * @param node node to index
      * @param key the key in the key-value pair to associate with {@code node}.
      * @param value the value in the key-value pair to associate with {@code
      *            node}.
      */
     void index( Node node, String key, Object value );
 
     /**
      * Returns a single node indexed with associated with <code>key</code> and
      * <code>value</code>. If no such node exist <code>null</code> is returned.
      * If more then one node is found a runtime exception is thrown.
      * 
      * @param key the key for index
      * @param value the value for index
      * @return node that has been indexed with key and value or
      *         <code>null</code>
      */
     Node getSingleNode( String key, Object value );
 
     /**
      * Returns all nodes indexed with <code>key</code> and <code>value</code>.
      * 
      * @param key the key for index
      * @param value the value for index
      * @return nodes that have been indexed with key and value
      */
     IndexHits<Node> getNodes( String key, Object value );
 
     /**
      * Dissociates a key-value pair from {@code node}. If no such association
      * exist this method silently returns.
      * 
      * @param node the node to dissociate from the key-value pair.
      * @param key the key in the key-value pair.
      * @param value the value in the key-value pair.
      */
     void removeIndex( Node node, String key, Object value );
 
     /**
      * Changes isolation level for the running transaction. This method must be
      * invoked before any index ({@link #index(Node, String, Object)},
      * {@link #removeIndex(Node, String, Object)}) has been performed in the
      * current transaction. The default isolation level is
      * {@link Isolation#SAME_TX} .
      * 
      * @param level new isolation level
      */
     void setIsolation( Isolation level );
 
     /**
      * Stops this indexing service committing any asynchronous requests that are
      * currently queued (see {@link Isolation}). After this method has been
      * invoked any following method invocation on this instance is invalid.
      */
     void shutdown();
 }
