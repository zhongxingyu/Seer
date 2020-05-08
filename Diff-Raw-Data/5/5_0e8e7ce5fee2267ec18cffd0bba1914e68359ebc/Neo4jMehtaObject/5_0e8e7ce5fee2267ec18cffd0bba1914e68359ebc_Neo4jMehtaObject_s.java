 package de.deepamehta.mehtagraph.impl;
 
 import de.deepamehta.mehtagraph.ConnectedMehtaEdge;
 import de.deepamehta.mehtagraph.ConnectedMehtaNode;
 import de.deepamehta.mehtagraph.MehtaEdge;
 import de.deepamehta.mehtagraph.MehtaGraphIndexMode;
 import de.deepamehta.mehtagraph.MehtaObject;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.traversal.Evaluation;
 import org.neo4j.graphdb.traversal.Evaluator;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.kernel.Traversal;
 import org.neo4j.kernel.Uniqueness;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 
 class Neo4jMehtaObject extends Neo4jBase implements MehtaObject {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     protected final Node node;
 
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     // ---------------------------------------------------------------------------------------------------- Constructors
 
     protected Neo4jMehtaObject(Node node, Neo4jBase base) {
         super(base);
         this.node = node;
     }
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // === MehtaObject Implementation ===
 
     @Override
     public long getId() {
         return node.getId();
     }
 
     // --- Get Attributes ---
 
     @Override
     public String getString(String key) {
         return (String) getObject(key);
     }
 
     @Override
     public String getString(String key, String defaultValue) {
         return (String) getObject(key, defaultValue);
     }
 
     @Override
     public int getInteger(String key) {
         return (Integer) getObject(key);
     }
 
     @Override
     public int getInteger(String key, int defaultValue) {
         return (Integer) getObject(key, defaultValue);
     }
 
     @Override
     public boolean getBoolean(String key) {
         return (Boolean) getObject(key);
     }
 
     @Override
     public boolean getBoolean(String key, boolean defaultValue) {
         return (Boolean) getObject(key, defaultValue);
     }
 
     @Override
     public Object getObject(String key) {
         return node.getProperty(key);
     }
 
     @Override
     public Object getObject(String key, Object defaultValue) {
         return node.getProperty(key, defaultValue);
     }
 
     // ---
 
     @Override
     public Iterable<String> getAttributeKeys() {
         return node.getPropertyKeys();
     }
 
     public boolean hasAttribute(String key) {
         return node.hasProperty(key);
     }
 
     // --- Set Attributes ---
 
     @Override
     public String setString(String key, String value) {
         return (String) setObject(key, value);
     }
 
     @Override
     public Integer setInteger(String key, int value) {
         return (Integer) setObject(key, value);
     }
 
     @Override
     public Boolean setBoolean(String key, boolean value) {
         return (Boolean) setObject(key, value);
     }
 
     @Override
     public Object setObject(String key, Object value) {
         Object oldValue = getObject(key, null);
         node.setProperty(key, value);
         return oldValue;
     }
 
     // --- Indexing ---
 
     @Override
     public void indexAttribute(MehtaGraphIndexMode indexMode, Object value, Object oldValue) {
         indexAttribute(indexMode, null, value, oldValue);
     }
 
     @Override
     public void indexAttribute(MehtaGraphIndexMode indexMode, String indexKey, Object value, Object oldValue) {
         if (indexMode == MehtaGraphIndexMode.OFF) {
             return;
         } else if (indexMode == MehtaGraphIndexMode.KEY) {
             if (oldValue != null) {
                 // FIXME: new index API doesn't work with OSGi
                 // exactIndex.remove(node, indexKey, oldValue);             // remove old
                 exactIndex.removeIndex(node, indexKey, oldValue);           // remove old
             }
             // FIXME: new index API doesn't work with OSGi
             // exactIndex.add(node, indexKey, value);                       // index new
             exactIndex.index(node, indexKey, value);                        // index new
         } else if (indexMode == MehtaGraphIndexMode.FULLTEXT) {
             // Note: all the topic's FULLTEXT properties are indexed under the same key ("default").
             // So, when removing from index we must explicitley give the old value.
             if (oldValue != null) {
                 // FIXME: new index API doesn't work with OSGi
                 // fulltextIndex.remove(node, KEY_FULLTEXT, oldValue);      // remove old
                 fulltextIndex.removeIndex(node, KEY_FULLTEXT, oldValue);    // remove old
             }
             // FIXME: new index API doesn't work with OSGi
             // fulltextIndex.add(node, KEY_FULLTEXT, value);                // index new
             fulltextIndex.index(node, KEY_FULLTEXT, value);                 // index new
         } else if (indexMode == MehtaGraphIndexMode.FULLTEXT_KEY) {
             if (oldValue != null) {
                 // FIXME: new index API doesn't work with OSGi
                 // fulltextIndex.remove(node, indexKey, oldValue);          // remove old
                 fulltextIndex.removeIndex(node, indexKey, oldValue);        // remove old
             }
             // FIXME: new index API doesn't work with OSGi
             // fulltextIndex.add(node, indexKey, value);                    // index new
             fulltextIndex.index(node, indexKey, value);                     // index new
         } else {
             throw new RuntimeException("Index mode \"" + indexMode + "\" not implemented");
         }
     }
 
     // --- Traversal ---
 
     @Override
     public Set<MehtaEdge> getMehtaEdges(String myRoleType) {
         Iterable<Relationship> rels;
         if (myRoleType == null) {
             rels = node.getRelationships(Direction.INCOMING);
         } else {
             rels = node.getRelationships(getRelationshipType(myRoleType), Direction.INCOMING);
         }
         //
         return buildMehtaEdges(rels);
     }
 
     // ---
 
     @Override
     public ConnectedMehtaNode getConnectedMehtaNode(String myRoleType, String othersRoleType) {
         Set<ConnectedMehtaNode> nodes = getConnectedMehtaNodes(myRoleType, othersRoleType);
         switch (nodes.size()) {
         case 0:
             return null;
         case 1:
             return nodes.iterator().next();
         default:
            throw new RuntimeException("Ambiguity: there are " + nodes.size() + " connected nodes (" + node +
                 ", myRoleType=\"" + myRoleType + "\", othersRoleType=\"" + othersRoleType + "\")");
         }
     }
 
     @Override
     public Set<ConnectedMehtaNode> getConnectedMehtaNodes(String myRoleType, String othersRoleType) {
         return new TraveralResultBuilder(node, createTraversalDescription(myRoleType, othersRoleType)) {
             @Override
             Object buildResult(Node connectedNode, Node auxiliaryNode) {
                 return new ConnectedMehtaNode(buildMehtaNode(connectedNode), buildMehtaEdge(auxiliaryNode));
             }
         }.getResult();
     }
 
     // ---
 
     @Override
     public ConnectedMehtaEdge getConnectedMehtaEdge(String myRoleType, String othersRoleType) {
         Set<ConnectedMehtaEdge> edges = getConnectedMehtaEdges(myRoleType, othersRoleType);
         switch (edges.size()) {
         case 0:
             return null;
         case 1:
             return edges.iterator().next();
         default:
            throw new RuntimeException("Ambiguity: there are " + edges.size() + " connected edges (" + node +
                 ", myRoleType=\"" + myRoleType + "\", othersRoleType=\"" + othersRoleType + "\")");
         }
     }
 
     @Override
     public Set<ConnectedMehtaEdge> getConnectedMehtaEdges(String myRoleType, String othersRoleType) {
         return new TraveralResultBuilder(node, createTraversalDescription(myRoleType, othersRoleType)) {
             @Override
             Object buildResult(Node connectedNode, Node auxiliaryNode) {
                 return new ConnectedMehtaEdge(buildMehtaEdge(connectedNode), buildMehtaEdge(auxiliaryNode));
             }
         }.getResult();
     }
 
     // --- Deletion ---
 
     @Override
     public void delete() {
         node.delete();
     }
 
 
 
     // ----------------------------------------------------------------------------------------------- Protected Methods
 
     protected final String getAttributesString(PropertyContainer container) {
         Map<String, Object> properties = getProperties(container);
         //
         StringBuilder builder = new StringBuilder("{");
         for (String key : properties.keySet()) {
             if (builder.length() > 1) {
                 builder.append(", ");
             }
             builder.append(key + "=" + properties.get(key));
         }
         builder.append("}");
         return builder.toString();
     }
 
     protected final RelationshipType getRelationshipType(String typeName) {
         return relTypeCache.get(typeName);
     }
 
 
 
     // ----------------------------------------------------------------------------------------- Package Private Methods
 
     Node getNode() {
         return node;
     }
 
 
 
     // ------------------------------------------------------------------------------------------------- Private Methods
 
     // === Traversal ===
 
     /**
      * The created traversal description allows to find connected mehta nodes/mehta edges that
      * are connected to the start node/edge via the given role types.
      * <p>
      * Called from {@link #getConnectedMehtaNodes} and {@link #getConnectedMehtaEdges}
      *
      * @param   myRoleType      Pass <code>null</code> to switch role type filter off.
      * @param   othersRoleType  Pass <code>null</code> to switch role type filter off.
      */
     private TraversalDescription createTraversalDescription(String myRoleType, String othersRoleType) {
         TraversalDescription desc = Traversal.description().uniqueness(Uniqueness.RELATIONSHIP_GLOBAL);
         // Note: we need to traverse a node more than once. Consider this case: mehta node A
         // is connected with mehta node B via mehta edge C and A is connected to C as well.
         // (default uniqueness is not RELATIONSHIP_GLOBAL, but probably NODE_GLOBAL).
         if (myRoleType != null && othersRoleType != null) {
             return desc.evaluator(new RoleTypeEvaluator(myRoleType, othersRoleType))
                        .relationships(getRelationshipType(myRoleType), Direction.INCOMING)
                        .relationships(getRelationshipType(othersRoleType), Direction.OUTGOING);
         } else if (myRoleType == null && othersRoleType == null) {
             // FIXME: with role type filter switched off only node-to-node traversal is currently supported.
             return desc.evaluator(new AuxiliaryEvaluator());
         } else {
             throw new IllegalArgumentException("Both or none role types must be set");
         }
     }
 
     private class RoleTypeEvaluator implements Evaluator {
 
         private RelationshipType myRoleType;
         private RelationshipType othersRoleType;
 
         private RoleTypeEvaluator(String myRoleType, String othersRoleType) {
             this.myRoleType = getRelationshipType(myRoleType);
             this.othersRoleType = getRelationshipType(othersRoleType);
         }
 
         @Override
         public Evaluation evaluate(Path path) {
             boolean includes = true;
             boolean continues = true;
             Relationship rel = path.lastRelationship();
             Node node = path.endNode();
             if (path.length() == 1) {
                 if (!rel.isType(myRoleType) || rel.getStartNode().getId() != node.getId()) {
                     continues = false;
                 }
             } else if (path.length() == 2) {
                 if (!rel.isType(othersRoleType) || rel.getEndNode().getId() != node.getId()) {
                     includes = false;
                 }
             }
             //
             includes = includes && path.length() == 2;
             continues = continues && path.length() < 2;
             return Evaluation.of(includes, continues);
         }
     }
 
     // === Helper ===
 
     private Set<MehtaEdge> buildMehtaEdges(Iterable<Relationship> relationships) {
         Set edges = new HashSet();
         for (Relationship rel : relationships) {
             Node auxiliaryNode = rel.getStartNode();
             edges.add(buildMehtaEdge(auxiliaryNode));
         }
         return edges;
     }
 
     private Map<String, Object> getProperties(PropertyContainer container) {
         Map properties = new HashMap();
         for (String key : container.getPropertyKeys()) {
             properties.put(key, container.getProperty(key));
         }
         return properties;
     }
 }
