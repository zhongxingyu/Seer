 package net.todd.biblestudy;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class SqlBatchCreator implements ISqlBatchCreator {
 	public List<String> createBatchQueries(String sql) {
 		List<String> batchQueries = new ArrayList<String>();
 
 		StringTokenizer tokenizer = new StringTokenizer(sql, "\n");
 		StringBuffer query = new StringBuffer();
 		while (tokenizer.hasMoreTokens()) {
 			String token = tokenizer.nextToken();
 			query.append(token);
 			if (query.charAt(query.length() - 1) == ';') {
 				String queryToExecute = query.toString().substring(0,
 						query.length() - 1);
 
 				batchQueries.add(queryToExecute);
 
 				query = new StringBuffer();
 			}
 		}
 
 		return batchQueries;
 	}
 }
