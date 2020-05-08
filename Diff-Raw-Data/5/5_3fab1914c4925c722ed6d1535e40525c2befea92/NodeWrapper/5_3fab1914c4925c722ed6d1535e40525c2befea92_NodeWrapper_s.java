 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.services;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * <p>
  * Node wrapper
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 class NodeWrapper implements Node {
     private final Node node;
 
     public NodeWrapper(Node node) {
         super();
         this.node = node;
     }
 
     @Override
     public long getId() {
         return node.getId();
     }
 
     @Override
     public void delete() {
         node.delete();
     }
 
     @Override
     public Iterable<Relationship> getRelationships() {
         return node.getRelationships();
     }
 
     @Override
     public boolean hasRelationship() {
         return false;
     }
 
     @Override
     public Iterable<Relationship> getRelationships(RelationshipType... types) {
         return node.getRelationships(types);
     }
 
     @Override
     public boolean hasRelationship(RelationshipType... types) {
         return hasRelationship(types);
     }
 
     @Override
     public Iterable<Relationship> getRelationships(Direction dir) {
         return node.getRelationships(dir);
     }
 
     @Override
     public boolean hasRelationship(Direction dir) {
         return node.hasRelationship(dir);
     }
 
     @Override
     public Iterable<Relationship> getRelationships(RelationshipType type, Direction dir) {
         return node.getRelationships(type, dir);
     }
 
     @Override
     public boolean hasRelationship(RelationshipType type, Direction dir) {
         return node.hasRelationship(type, dir);
     }
 
     @Override
     public Relationship getSingleRelationship(RelationshipType type, Direction dir) {
         return node.getSingleRelationship(type, dir);
     }
 
     @Override
     public Relationship createRelationshipTo(Node otherNode, RelationshipType type) {
         return node.createRelationshipTo(otherNode, type);
     }
 
     @Override
     public org.neo4j.graphdb.Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType relationshipType,
             Direction direction) {
         return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, relationshipType, direction);
     }
 
     @Override
     public org.neo4j.graphdb.Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator, RelationshipType firstRelationshipType,
             Direction firstDirection, RelationshipType secondRelationshipType, Direction secondDirection) {
         return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, firstRelationshipType, firstDirection, secondRelationshipType, secondDirection);
     }
 
     @Override
     public org.neo4j.graphdb.Traverser traverse(Order traversalOrder, StopEvaluator stopEvaluator, ReturnableEvaluator returnableEvaluator,
             Object... relationshipTypesAndDirections) {
         return node.traverse(traversalOrder, stopEvaluator, returnableEvaluator, relationshipTypesAndDirections);
     }
 
     @Override
     public GraphDatabaseService getGraphDatabase() {
         return node.getGraphDatabase();
     }
 
     @Override
     public boolean hasProperty(String key) {
         return node.hasProperty(key);
     }
 
     @Override
     public Object getProperty(String key) {
         return node.getProperty(key);
     }
 
     @Override
     public Object getProperty(String key, Object defaultValue) {
         return node.getProperty(key, defaultValue);
     }
 
     @Override
     public void setProperty(String key, Object value) {
         node.setProperty(key, value);
     }
 
     @Override
     public Object removeProperty(String key) {
         return node.removeProperty(key);
     }
 
     @Override
     public Iterable<String> getPropertyKeys() {
         return node.getPropertyKeys();
     }
 
     @Override
     public Iterable<Object> getPropertyValues() {
         return node.getPropertyValues();
     }
     
     @Override
     public boolean equals(Object o) {
         if (o != null) {
             return node.equals(o);
         }
         
         return false;
     }
 
 }
