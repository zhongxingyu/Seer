 package com.crypticbit.javelin.neo4j.nodes;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.neo4j.graphdb.Relationship;
 
 import com.crypticbit.javelin.History;
 import com.crypticbit.javelin.IllegalJsonException;
 import com.crypticbit.javelin.JsonPersistenceException;
 import com.crypticbit.javelin.MergeableBlock;
 import com.crypticbit.javelin.neo4j.nodes.json.ComplexGraphNode;
 import com.crypticbit.javelin.neo4j.nodes.json.JsonNodeFactory;
 import com.crypticbit.javelin.neo4j.strategies.FundementalDatabaseOperations;
 import com.crypticbit.javelin.neo4j.strategies.VectorClock;
 import com.crypticbit.javelin.neo4j.strategies.VectorClockAdapter;
 import com.crypticbit.javelin.neo4j.strategies.operations.JsonWriteUpdateOperation;
 import com.crypticbit.javelin.neo4j.strategies.operations.WriteVectorClock;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.jayway.jsonpath.internal.PathToken;
 
 public class ComplexNode implements ComplexGraphNode {
 
     private RelationshipHolder incomingRelationship;
     private FundementalDatabaseOperations fdo;
     private JsonNodeFactory jsonNodeFactory;
 
     public ComplexNode(RelationshipHolder incomingRelationship, FundementalDatabaseOperations fdo) {
 	this.incomingRelationship = incomingRelationship;
 	this.fdo = fdo;
 	this.jsonNodeFactory = new JsonNodeFactory(this, incomingRelationship);
     }
 
     public ComplexGraphNode getJsonNode() {
 	return jsonNodeFactory;
     }
 
     public FundementalDatabaseOperations getStrategy() {
 	return fdo;
     }
 
     public Relationship getIncomingRelationship() {
 	return incomingRelationship.getRelationship();
     }
 
    public ComplexGraphNode navigate(PathToken token) {
	return jsonNodeFactory;
     }
 
     private static final String DATE_FORMAT = "H:mm:ss.SSS yy-MM-dd";
     private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
 
     public List<History> getHistory() {
 	List<History> history = new LinkedList<History>();
 
 	// System.out.println("History for: "+getGraphNode().getDatabaseNode().getId());
 	//
 	// for (Relationship r :
 	// getGraphNode().getDatabaseNode().getRelationships(RelationshipTypes.VERSION,
 	// Direction.OUTGOING)) {
 
 	// Relationship readRelationship = getStrategy().read(r);
 	// final Neo4JGraphNode endNode =
 	// NodeTypes.wrapAsGraphNode(readRelationship.getEndNode(), r,
 	// getStrategy());
 	// history.add(new History() {
 	//
 	// @Override
 	// public long getTimestamp() {
 	// return endNode.getTimestamp();
 	// }
 	//
 	// public String toString() {
 	// return sdf.format(new Date(getTimestamp()));
 	// }
 	//
 	// @Override
 	// public GraphNode getVersion() {
 	// return endNode;
 	// }
 	// });
 
 	// }
 	// return history;
 	return null;
     }
 
     public long getTimestamp() {
 	return (long) getIncomingRelationship().getProperty("timestamp");
     }
 
     public VectorClock getVectorClock() {
 	// FIXME - what if VC is not at top of stack?
 	return null;
 	// return ((VectorClockAdapter)
 	// getStrategy()).getVectorClock(getGraphNode().getDatabaseNode());
     }
 
     public void merge(MergeableBlock block) throws JsonProcessingException, IOException {
 	// FIXME - what if VC is not at top of stack?
 	// FIXME Factor out Object Mapper
 	VectorClockAdapter vca2 = ((VectorClockAdapter) getStrategy());
 	vca2.addIncoming(getIncomingRelationship(),
 		new JsonWriteUpdateOperation(new ObjectMapper().readTree(block.getJson())).add(new WriteVectorClock(
 			block.getVectorClock())));
 
     }
 
     public MergeableBlock getExtract() {
 	return null;
 	// new MergeableBlock() {
 	// private String json = getGraphNode().toJsonNode().toString();
 	// private VectorClock vc = getGraphNode().getVectorClock();
 	//
 	// @Override
 	// public VectorClock getVectorClock() {
 	// return vc;
 	// }
 	//
 	// @Override
 	// public String getJson() {
 	// return json;
 	// }
 	//
 	// public String toString() {
 	// return json + " (" + vc + ")";
 	// }
 	// };
     }
 
     @Override
     public ComplexNode put(String key) throws IllegalJsonException, JsonPersistenceException {
 	return getJsonNode().put(key);
     }
 
     @Override
     public ComplexNode add() throws IllegalJsonException, JsonPersistenceException {
 	return getJsonNode().add();
     }
 
     @Override
     public JsonNode toJsonNode() {
 	return getJsonNode().toJsonNode();
     }
 
     @Override
     public ComplexGraphNode navigate(String jsonPath) throws IllegalJsonException {
 	return getJsonNode().navigate(jsonPath);
     }
 
     @Override
     public String toJsonString() {
 	return getJsonNode().toJsonString();
     }
 
     @Override
     public void write(String json) throws IllegalJsonException, JsonPersistenceException {
 	getJsonNode().write(json);
     }
 
 }
