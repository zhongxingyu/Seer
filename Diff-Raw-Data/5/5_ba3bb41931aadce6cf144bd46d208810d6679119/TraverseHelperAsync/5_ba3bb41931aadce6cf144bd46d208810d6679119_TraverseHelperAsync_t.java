 package com.volkan.interpartitiontraverse;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.kernel.impl.util.StringLogger;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.volkan.Utility;
 import com.volkan.db.H2Helper;
 
 public class TraverseHelperAsync extends AbstractTraverseHelper {
 
 	private final StringLogger neo4jLogger = StringLogger.logger(Utility.buildLogFileName());
 	private final H2Helper h2Helper;
 	
 	public TraverseHelperAsync(H2Helper h2Helper) throws ClassNotFoundException, SQLException {
 		this.h2Helper = h2Helper; 
 	}
 
 	@Override
 	public List<String> traverse(GraphDatabaseService db, Map<String, Object> jsonMap) 
 			throws Exception 
 	{
 		List<String> realResults = new ArrayList<String>();
 		TraversalDescription traversalDes = TraversalDescriptionBuilder.buildFromJsonMap(jsonMap);
 	
 		Node startNode = getStartNode(db, jsonMap);
 
 		String previousPath = (String) jsonMap.get(JsonKeyConstants.PATH);
 		int toDepth 		= (Integer) jsonMap.get(JsonKeyConstants.DEPTH);
 		for (Path path : traversalDes.traverse(startNode)) {
 //			neo4jLogger.logMessage(path.toString() + " # " + path.length(), true);
 			Node endNode = path.endNode();
 			if (didShadowComeInUnfinishedPath(toDepth, path, endNode)) {
 				delegateQueryToAnotherNeo4j(previousPath, path, jsonMap);
 			} else {
 				if (path.length() >= toDepth) { //if it is a finished path
 					realResults.add( previousPath + " " + 
 									 appendEndingToFinishedPath(jsonMap, path, endNode) );
 				} //else, a real node but unfinished path. No need to care
 			}
 		}
 
 		updateDBWithResults(jsonMap, realResults);
 		return realResults;
 	}
 
 	/**
 	 * Returns the start node according to nodeFetchStyle property of the 
 	 * interpartitiontraverse.properties file. If no key with the name nodeFetchStyle is found
 	 * then returns "BY_INDEX" as default value.
 	 * @author volkan
 	 * @param db
 	 * @param jsonMap
 	 * @return
 	 */
 	private Node getStartNode(GraphDatabaseService db, Map<String, Object> jsonMap) 
 			throws Exception
 	{
 		Node startNode;
 		String defaultValue = "BY_INDEX";
 		String nodeFetchStyle = Utility.getValueOfProperty("nodeFetchStyle", defaultValue);
 		if (nodeFetchStyle.equalsIgnoreCase("BY_ID")) {
 			startNode = db.getNodeById((int) jsonMap.get("start_node"));
 		} else {
 			startNode = fetchStartNodeFromIndex(db, jsonMap);
 		}
 		
 		if (startNode == null) {
 			throw new NoSuchElementException(defaultValue+" returned kein Node");
 		}
 		
 		return startNode;
 	}
 
 	private void updateDBWithResults(Map<String, Object> jsonMap, List<String> realResults) 
 			throws SQLException 
 	{
 		String resultString = convertResultsListToString(realResults);
 		
		long jobID = (long) jsonMap.get(JsonKeyConstants.JOB_ID);
 //		try {
 		neo4jLogger.logMessage("updateDBWithResults is called. resultString.length=" 
 									+ resultString.length(), true);
 		h2Helper.updateJobWithResults(jobID, resultString);
 //		} catch (SQLException e) {
 //			e.printStackTrace();
 //			neo4jLogger.logMessage(e.toString(), true);
 //			realResults.clear();
 //			
 //			//BU ISE YARAMAZ CUNKU BUNU CAGIRAN traverse() GERIYE(MyService) 
 //			//BIR SEY DONMUYOR 2013.3.1
 //			realResults.add(e.toString());
 //		}
 	}
 
 	private String convertResultsListToString(List<String> realResults) {
 		StringBuilder sb 			= new StringBuilder();
 		boolean hasMultipleResults 	= realResults.size() > 1;
 		for (String result : realResults) {
 			sb.append(result);
 			if (hasMultipleResults) {
 				sb.append("\n");
 			}
 		}
 		return sb.toString();
 	}
 	
 	protected void delegateQueryToAnotherNeo4j(
 			String previousPath, Path path, Map<String, Object> jsonMap) 
 					throws SQLException, Exception 
 	{
 		Map<String, Object> jsonMapClone = new HashMap<String, Object>();
 		
 		updateRelationships(path, jsonMap, jsonMapClone); 
 		
 		increaseHops(jsonMap, jsonMapClone);
 		
 		updateDepth(path, jsonMap, jsonMapClone);
 		
 		updateStartNode(path, jsonMapClone);
 		
 		updatePath(previousPath, path, jsonMapClone);
 		
 		String port = getPortFromEndNode(path);
 		jsonMapClone.put(JsonKeyConstants.START_PORT, port);
 
 		long parentJobID = copyParentJobID(jsonMap, jsonMapClone);
 		updateJobID(jsonMapClone, parentJobID);
 		
 		delegateQueryOverRestAsync(port, jsonMapClone);
 	}
 
 	private void updateJobID(Map<String, Object> jsonMapClone, long parentJobID)
 			throws SQLException, Exception 
 	{
 		ObjectMapper mapper = new ObjectMapper();
 		String json 		= mapper.writeValueAsString(jsonMapClone);
 		long jobID 			= h2Helper.generateJob(parentJobID, json);
 		jsonMapClone.put(JsonKeyConstants.JOB_ID, jobID);
 	}
 
 	private long copyParentJobID(Map<String, Object> jsonMap,
 			Map<String, Object> jsonMapClone) {
 		long parentJobID = (int) jsonMap.get(JsonKeyConstants.PARENT_JOB_ID);
 		jsonMapClone.put(JsonKeyConstants.PARENT_JOB_ID, parentJobID);
 		return parentJobID;
 	}
 
 	private void updatePath(String previousPath, Path path, Map<String, Object> jsonMapClone) {
 		String formattedPathString = formatPathString(path);
 		jsonMapClone.put(JsonKeyConstants.PATH, previousPath +"~"+ formattedPathString);
 	}
 
 	private void delegateQueryOverRestAsync(
 			final String port, final Map<String, Object> jsonMapClone) 
 	{
 		Thread t = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				RestConnector restConnector = new RestConnector(port);
 				ClientResponse response = restConnector.delegateQueryWithoutResult(jsonMapClone);
 				if( response.getStatus() == 500 ){
 					String aapn = "NaN";
 					try {
 						aapn = Utility.getAutomaticallyAssignedPortNumber();
 					} catch (IOException e) {
 						neo4jLogger.logMessage("AAPN is not accessible\n"+e.toString());
 					}
					long jobID =  (long) jsonMapClone.get(JsonKeyConstants.JOB_ID);
 					try {
 						h2Helper.updateJobWithResults(jobID, aapn+" - jobID:" + jobID + " CAKILDI-" +
 								response.getEntity(String.class));
 					} catch (SQLException e1) {
 						neo4jLogger.logMessage(aapn +" H2Helper error: " + e1.toString(), true);
 					}
 				}
 				//TODO the result of above query shall be inserted into 
 				//db(updateDBWithResults(jsonMap, realResults)) in order to inform
 				//about an exception occurred for the hops 1 and beyond.
 			}
 		});
 
 		t.start();
 	}
 
 }
