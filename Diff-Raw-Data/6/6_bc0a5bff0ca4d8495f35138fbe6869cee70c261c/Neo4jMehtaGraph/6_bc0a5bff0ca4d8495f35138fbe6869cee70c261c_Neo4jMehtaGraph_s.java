 package de.deepamehta.mehtagraph.impl;
 
 import de.deepamehta.mehtagraph.MehtaEdge;
 import de.deepamehta.mehtagraph.MehtaGraph;
 import de.deepamehta.mehtagraph.MehtaNode;
 import de.deepamehta.mehtagraph.MehtaObjectRole;
 import de.deepamehta.mehtagraph.MehtaGraphTransaction;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 // FIXME: new index API doesn't work with OSGi
 // import org.neo4j.graphdb.index.Index;
 // import org.neo4j.helpers.collection.MapUtil;
 // Using old index API instead
 import org.neo4j.index.lucene.LuceneIndexService;
 import org.neo4j.index.lucene.LuceneFulltextQueryIndexService;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 
 
 public class Neo4jMehtaGraph extends Neo4jBase implements MehtaGraph {
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private final Logger logger = Logger.getLogger(getClass().getName());
 
     // ---------------------------------------------------------------------------------------------------- Constructors
 
     public Neo4jMehtaGraph(GraphDatabaseService neo4j) {
         super(neo4j);
         this.relTypeCache = new Neo4jRelationtypeCache(neo4j);
         try {
             /* FIXME: new index API doesn't work with OSGi
             // access/create indexes
             this.exactIndex = neo4j.index().forNodes("exact");
             if (neo4j.index().existsForNodes("fulltext")) {
                 this.fulltextIndex = neo4j.index().forNodes("fulltext");
             } else {
                 Map<String, String> configuration = MapUtil.stringMap("provider", "lucene", "type", "fulltext");
                 this.fulltextIndex = neo4j.index().forNodes("fulltext", configuration);
             } */
             this.exactIndex = new LuceneIndexService(neo4j);
             this.fulltextIndex = new LuceneFulltextQueryIndexService(neo4j);
         } catch (Exception e) {
             throw new RuntimeException("Creating database indexes failed", e);
         }
     }
 
     // -------------------------------------------------------------------------------------------------- Public Methods
 
 
 
     // *********************************
     // *** MehtaGraph Implementation ***
     // *********************************
 
 
 
     // === Mehta Nodes ===
 
     @Override
     public MehtaNode createMehtaNode() {
         return buildMehtaNode(neo4j.createNode());
     }
 
     // ---
 
     @Override
     public Neo4jMehtaNode getMehtaNode(long id) {
         return buildMehtaNode(neo4j.getNodeById(id));
     }
 
     @Override
     public MehtaNode getMehtaNode(String key, Object value) {
         if (value == null) {
             throw new IllegalArgumentException("Tried to call getMehtaNode() with a null value Object (key=\"" +
                 key + "\")");
         }
         //
         // FIXME: new index API doesn't work with OSGi
         // Node node = exactIndex.get(key, value).getSingle();
         Node node = exactIndex.getSingleNode(key, value);
         return node != null ? buildMehtaNode(node) : null;
     }
 
     // ---
 
     @Override
     public List<MehtaNode> queryMehtaNodes(Object value) {
         return queryMehtaNodes(null, value);
     }
 
     @Override
     public List<MehtaNode> queryMehtaNodes(String key, Object value) {
         if (key == null) {
             key = KEY_FULLTEXT;
         }
         if (value == null) {
             throw new IllegalArgumentException("Tried to call queryMehtaNodes() with a null value Object (key=\"" +
                 key + "\")");
         }
         //
         List nodes = new ArrayList();
         // FIXME: new index API doesn't work with OSGi
         // for (Node node : fulltextIndex.query(key, value)) {
         for (Node node : fulltextIndex.getNodes(key, value)) {
             nodes.add(buildMehtaNode(node));
         }
         return nodes;
     }
 
 
 
     // === Mehta Edges ===
 
     @Override
     public MehtaEdge createMehtaEdge(MehtaObjectRole object1, MehtaObjectRole object2) {
         Node auxiliaryNode = neo4j.createNode();
         auxiliaryNode.setProperty(KEY_IS_MEHTA_EDGE, true);
         Neo4jMehtaEdge mehtaEdge = buildMehtaEdge(auxiliaryNode);
         //
         mehtaEdge.addMehtaObject(object1);
         mehtaEdge.addMehtaObject(object2);
         return mehtaEdge;
     }
 
     // ---
 
     @Override
     public MehtaEdge getMehtaEdge(long id) {
         return buildMehtaEdge(neo4j.getNodeById(id));
     }
 
     // ---
 
     @Override
     public Set<MehtaEdge> getMehtaEdges(long node1Id, long node2Id) {
         return new TraveralResultBuilder(getMehtaNode(node1Id), traverseToMehtaNode(node2Id)) {
             @Override
             Object buildResult(Node connectedNode, Node auxiliaryNode) {
                 return buildMehtaEdge(auxiliaryNode);
             }
         }.getResult();
     }
 
     @Override
     public Set<MehtaEdge> getMehtaEdges(long node1Id, long node2Id, String roleType1, String roleType2) {
         return new TraveralResultBuilder(getMehtaNode(node1Id), traverseToMehtaNode(node2Id, roleType1,
                                                                                              roleType2)) {
             @Override
             Object buildResult(Node connectedNode, Node auxiliaryNode) {
                 return buildMehtaEdge(auxiliaryNode);
             }
         }.getResult();
     }
 
     @Override
     public Set<MehtaEdge> getMehtaEdgesBetweenNodeAndEdge(long nodeId, long edgeId, String nodeRoleType,
                                                                                     String edgeRoleType) {
         return new TraveralResultBuilder(getMehtaNode(nodeId), traverseToMehtaEdge(edgeId, nodeRoleType,
                                                                                            edgeRoleType)) {
             @Override
             Object buildResult(Node connectedNode, Node auxiliaryNode) {
                 return buildMehtaEdge(auxiliaryNode);
             }
         }.getResult();
     }
 
 
 
     // === Misc ===
 
     @Override
     public MehtaGraphTransaction beginTx() {
         return new Neo4jTransactionAdapter(neo4j);
     }
 
     @Override
     public void shutdown() {
         logger.info("Shutdown DB");
         neo4j.shutdown();
     }
 }
