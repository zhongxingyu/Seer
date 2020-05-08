 package knn.clean;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 
 import strategies.LastCategoryGeneralization;
 
 import knn.Instance;
 import knn.distance.SemanticPair;
 import db.MysqlIndexConnection;
 import db.WikipediaConnector;
 import dbpedia.similarityStrategies.ValueComparator;
 
 /**This class compute the evaluation for one PathIndex. The evaluation is the one that is
  * included in the journal article.
  * 
  * @author dtorres
  *
  */
 
 public class BlueFinderEvaluation {
 	
 	private KNN knn;
 	
 	public BlueFinderEvaluation(KNN knn){
 		this.knn=knn;
 	}
 	
 	
 	
 	//private void processTest(String piaIndexBase, String typesTable,
 	//		int kValue, int testRowsNumber, String resultTableName)
 	//		throws ClassNotFoundException, SQLException {
 		
 	private void processTest(int proportionOfConnectedPairs, int kValue, int testRowsNumber, String resultTableName)
 			throws ClassNotFoundException, SQLException {
 		
 		
 		ResultSet resultSet = WikipediaConnector.getRandomProportionOfConnectedPairs(proportionOfConnectedPairs);
 		
 		String relatedUFrom = "u_from=0 "; String relatedString = "";
 		
 		while (resultSet.next()) {
 			long time_start, time_end;
 			time_start = System.currentTimeMillis();
 			
 			SemanticPair disconnectedPair = this.knn.generateSemanticPair(resultSet.getString("page"), resultSet.getLong("id"));
 			
 			List<Instance> kNearestNeighbors = this.knn.getKNearestNeighbors(kValue, disconnectedPair);
 			
 			SemanticPairInstance disconnectedInstance = new SemanticPairInstance(0, disconnectedPair);
 			
 			kNearestNeighbors.remove(disconnectedInstance);
 			
 			List<String> knnResults = new ArrayList<String>();
 			for (Instance neighbor : kNearestNeighbors) {
 				
 				relatedUFrom = relatedUFrom + "or u_from = " + neighbor.getId()
 						+ " ";
 				relatedString = relatedString + "(" + neighbor.getDistance()
 						+ ") " + neighbor.getResource() + " ";
 				
 				Statement st = WikipediaConnector.getResultsConnection().createStatement();
 				
 				String queryFixed = "SELECT v_to, count(v_to) suma,V.path from UxV, V_Normalized V where v_to=V.id and ("+ relatedUFrom +") group by v_to order by suma desc";
 				
 				ResultSet paths = st.executeQuery(queryFixed);
 				TreeMap<String, Integer> map = this.genericPath(paths, knnResults.size()+1);
 				// for (String pathGen : map.keySet()) {
 				// System.out.println(map);
 
 				knnResults.add(map.toString());
 				// System.out.println("end ---- k="+kvalue);
 			
 			}
 			time_end = System.currentTimeMillis();
 			// Insert statem
			String insertSentence = "INSERT INTO `dbresearch`.`"+ resultTableName + "` (`v_to`, `related_resources`,`1path`, `2path`, `3path`, `4path`, `5path`, `6path`, `7path`, `8path`, `9path`, `10path`,`time`)"
 					+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
			PreparedStatement st = connection.prepareStatement(insertSentence);
 			st.setString(1, resultSet.getString("resource")+" "+resultSet.getInt("path_query"));
 			st.setString(2, relatedString);
 			int i = 3;
 			for (String string : knnResults) {
 				st.setString(i, string);
 				i++;
 
 			}
 			st.setLong(13, time_end - time_start);
 			st.executeUpdate();
 
			relatedVTo = "v_to=0 ";
 			relatedString = "";
 		}
 	}
 	
 	
 	protected TreeMap<String,Integer> genericPath(ResultSet paths, int kValue) throws SQLException {
 		HashMap<String, Integer> pathDictionary = new HashMap<String, Integer>();
 		ValueComparator bvc =  new ValueComparator(pathDictionary);
         TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
 		LastCategoryGeneralization cg = new LastCategoryGeneralization();
 		
 		while(paths.next()){
 			//SELECT v_to, count(v_to) suma,V.path from UxV, V_Normalized V 
 			String path=paths.getString("path");
 			path=cg.generalizePathQuery(path);
 			int suma = paths.getInt("suma");
 			//
 			if((!path.contains("Articles_") || path.contains("Articles_lis") ) && !path.contains("All_Wikipedia_") && !path.contains("Wikipedia_") && 
 					!path.contains("Non-free") && !path.contains("All_pages_") && !path.contains("All_non") ){
 			if(pathDictionary.get(path)==null){
 				if(suma==kValue){  //all the cases belongs to this path query
 					suma=suma+1000;
 				}
 				pathDictionary.put(path, suma);
 			}else{
 				suma+=pathDictionary.get(path);
 				pathDictionary.put(path, suma);
 			}
 			}
 			
 		}
 		//sorted_map.putAll(pathDictionary);
 		for (String path : pathDictionary.keySet()) {
 			Integer suma = pathDictionary.get(path);
 			sorted_map.put(path,suma);
 		}
 		return sorted_map;
 	}
 	
 
 }
