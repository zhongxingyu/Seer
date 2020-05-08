 package com.volkan.interpartitiontraverse;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Path;
 import org.neo4j.graphdb.traversal.TraversalDescription;
 import org.neo4j.kernel.impl.util.StringLogger;
 
 import com.volkan.Utility;
 import com.volkan.db.H2Helper;
 
 public class TraverseHelperAsync extends AbstractTraverseHelper {
 
 	private final StringLogger neo4jLogger = StringLogger.logger(Utility.buildLogFileName());
 	private final H2Helper h2Helper;
 	
 	public TraverseHelperAsync(H2Helper h2Helper) throws ClassNotFoundException, SQLException {
 		this.h2Helper = h2Helper; 
 	}
 
 	@Override
 	public List<String> traverse(GraphDatabaseService db, Map<String, Object> jsonMap) {
 		List<String> realResults = new ArrayList<String>();
 		TraversalDescription traversalDes = TraversalDescriptionBuilder.buildFromJsonMap(jsonMap);
 	
 //		Node startNode = db.getNodeById((int) jsonMap.get("start_node"));
 		Node startNode = fetchStartNodeFromIndex(db, jsonMap);
 
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
 
 	private void updateDBWithResults(Map<String, Object> jsonMap, List<String> realResults) {
 		String resultString = convertResultsListToString(realResults);
 		
 		long jobID = (int) jsonMap.get(JsonKeyConstants.JOB_ID);
 		try {
 			neo4jLogger.logMessage("updateDBWithResults is called. resultString.length=" 
 									+ resultString.length(), true);
 			h2Helper.updateJobWithResults(jobID, resultString);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			neo4jLogger.logMessage(e.toString(), true);
 			realResults.clear();
 			realResults.add(e.toString());
 		}
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
 	
 	protected void delegateQueryToAnotherNeo4j(String previousPath, Path path, Map<String, Object> jsonMap) {
 		Map<String, Object> jsonMapClone = new HashMap<String, Object>();
 		
 		updateRelationships(path, jsonMap, jsonMapClone); 
 		
 		increaseHops(jsonMap, jsonMapClone);
 		
 		updateDepth(path, jsonMap, jsonMapClone);
 		
 		updateStartNode(path, jsonMapClone);
 		
 		updatePath(previousPath, path, jsonMapClone);
 
 		try {
 			long parentJobID = copyParentJobID(jsonMap, jsonMapClone);
 			updateJobID(jsonMapClone, parentJobID);
 			
 			String port = getPortFromEndNode(path);
 			delegateQueryOverRestAsync(port, jsonMapClone);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			neo4jLogger.logMessage(e.toString());
 		}
 	}
 
 	private void updateJobID(Map<String, Object> jsonMapClone, long parentJobID)
 			throws SQLException {
 		long jobID 		 = h2Helper.generateJob(parentJobID, "");
 		jsonMapClone.put(JsonKeyConstants.JOB_ID, jobID);
 	}
 
 	private long copyParentJobID(Map<String, Object> jsonMap,
 			Map<String, Object> jsonMapClone) {
 		long parentJobID = (int) jsonMap.get(JsonKeyConstants.PARENT_JOB_ID);
 		jsonMapClone.put(JsonKeyConstants.PARENT_JOB_ID, parentJobID);
 		return parentJobID;
 	}
 
 	private void updatePath(String previousPath, Path path, Map<String, Object> jsonMapClone) {
 		jsonMapClone.put(JsonKeyConstants.PATH, previousPath +"~"+ path.toString());
 	}
 
 	private void delegateQueryOverRestAsync(
 			final String port, final Map<String, Object> jsonMapClone) {
 		Thread t = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				RestConnector restConnector = new RestConnector(port);
 				restConnector.delegateQueryWithoutResult(jsonMapClone);
 			}
 		});
 
 		t.start();
 	}
 
 }
