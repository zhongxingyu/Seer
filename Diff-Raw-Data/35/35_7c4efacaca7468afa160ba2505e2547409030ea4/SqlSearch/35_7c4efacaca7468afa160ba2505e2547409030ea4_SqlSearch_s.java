 package com.sirra.appcore.sql;
 
 import java.util.*;
 
 import org.hibernate.*;
 
 import com.sirra.appcore.util.leakdebugging.*;
 import com.sirra.server.json.*;
 import com.sirra.server.session.*;
 import com.sirra.server.templating.*;
 
 /**
  * Calls raw SQL. Provides these additional services:
  *  - Inserts account constraints where necessary.
  *  
  * @author aris
  */
 public class SqlSearch {
 	
 	// Tables which are shared across multiple accounts.
 	protected static List<String> sharedTables;
 
 	public static Data singleSearch(String sql, Columns columns, SqlParams sqlParams) {
 		List<Data> results = search(sql, columns, sqlParams);
 		
 		if(results.size() > 0) return results.get(0);
 		else return null;
 	}
 	
 	public static List<Data> search(String sql, Columns columns, SqlParams sqlParams) {
 		//new AccountEnabledList(hss);
 		
 		if(sqlParams == null) {
 			sqlParams = new SqlParams();
 		}
 		
		LeakDebugger.check(2.1);
		
 		sql = construct(sql, sqlParams);
		
		LeakDebugger.check(2.1);
		
 		SQLQuery query = SirraSession.get().getHibernateSession().createSQLQuery(sql);
 		
		LeakDebugger.check(2.2);
		
 		query.setFirstResult(sqlParams.getStartIndex());
		
		LeakDebugger.check(2.3);
		
 		query.setMaxResults(sqlParams.getNumItemsToRetrieve());
 		
		LeakDebugger.check(2.4);
		
 		List<Object[]> results = query.list();
 		
		LeakDebugger.check(2.5);
		
 		return convert(results, columns);
 	}
 	
 	/**
 	 * Ultra-convenient search that returns objects in the target type, with attributes all filled in.
 	 * Be sure to use "${columns}" placeholder for the column names to select, i.e. SELECT ${columns} FROM...
 	 */
 	public static <T> List<T> search(Class entityClass, String sql, SqlParams sqlParams) {
 		Columns columns = new Columns(entityClass);
 
		LeakDebugger.check(1);
		
 		sql = Template.replace(sql, "columns", columns.getForSelect());

		LeakDebugger.check(2);
 		
 		List<Data> dataList = SqlSearch.search(sql, columns, sqlParams);
 		
		LeakDebugger.check(3);
		
 		List<T> resultList = columns.materialize(dataList);
 		
		LeakDebugger.check(4);
		
 		return resultList;
 	}
 
 	protected static List<Data> convert(List results, Columns columns) {
 		List<Data> resultList = new ArrayList();
 		
 		for(Object item: results) {
 			
 			Data d = new Data();
 			resultList.add(d);
 			
 			if(item instanceof Object[]) {
 				Object[] arr = (Object[]) item;
 
 				for(int i=0; i<arr.length; i++) {
 					d.put(columns.get(i), arr[i]);
 				}
 			} else {
 				// If only a single field is requested, the result will not be in an array
 				d.put(columns.get(0), item);
 			}
 		}
 		
		LeakDebugger.check(2.6);
		
 		return resultList;
 	}
 	
 	/**
 	 * Really rough right now. Full solution will require JsqlParser type thing.
 	 */
 	protected static String construct(String sql, SqlParams sqlParams) {
 
 		SirraSession ss = SirraSession.get();
 		
 		if(ss.hasAccount()) {
 			String accountId = ss.getAccountId();
 			
 			int fromN = sql.toLowerCase().indexOf("from ") + "from".length();
 			int obN = sql.toLowerCase().indexOf("order by");
 			int wN = sql.toLowerCase().indexOf("where ");
 			
 			String tableStr;
 			
 			if(wN < 0 && obN >= 0) {
 				tableStr = sql.substring(fromN, obN);
 			} else if(obN < 0 && wN >= 0) {
 				tableStr = sql.substring(fromN, wN);
 			} else if(obN < 0 && wN < 0) {
 				tableStr = sql.substring(fromN);
 			} else {
 				int endN = wN<obN?wN:obN;
 				tableStr = sql.substring(fromN, endN);
 			}
 			
 			String[] tablePieces = tableStr.split(",");
 			SortedSet<String> tables = new TreeSet();
 			
 			for(String t: tablePieces) {
 				String table = t.trim().toLowerCase();
 				tables.add(table);
 			}
 			
 			if(sharedTables == null) {
 				sharedTables = new ArrayList();
 				
 				// for now. Will use annotations to get this information later.
 				sharedTables.add("accounts");
 			}
 			
 			for(String table: tables) {
 				if(!sharedTables.contains(table.toLowerCase())) {
 					sqlParams.addConstraint(table + ".accountId = '" + accountId + "'");
 				}
 			}
 		}
 		
 		if(sqlParams.getAllConstraints().size() > 0) {
 			int n = sql.toLowerCase().indexOf("where");
 			
 			String after = "";
 			if(n < 0) {
 				int obIndex = sql.toLowerCase().indexOf("order by");
 				if(obIndex < 0) {
 					sql = sql + " WHERE ";
 				} else {
 					sql = sql.substring(0, obIndex) + " WHERE " + sql.substring(obIndex);
 				}
 				n = sql.toLowerCase().indexOf("where");
 			} else {
 				after = " AND ";
 			}
 			int insertIndex = n + "WHERE".length();
 			
 			Iterator<String> it = sqlParams.getAllConstraints().iterator();
 			StringBuffer conStr = new StringBuffer();
 			while(it.hasNext()) {
 				String con = it.next();
 				conStr.append(con);
 				
 				if(it.hasNext()) {
 					conStr.append(" AND ");
 				}
 			}
 				
 			sql = sql.substring(0, insertIndex) + " " + conStr.toString() + after + " " + sql.substring(insertIndex);
 		}
 		
 		if(sqlParams.getSortColumns().size() > 0) {
 			int n = sql.toLowerCase().indexOf("order by");
 			if(n < 0) {
 				sql = sql + " ORDER BY ";
 			} else {
 				sql = sql + ", ";
 			}
 			
 			Iterator<String> it = sqlParams.getSortColumns().iterator();
 			while(it.hasNext()) {
 				String orderBy = it.next();
 				sql = sql + orderBy;
 				if(it.hasNext()) {
 					sql = sql + ", ";
 				}
 			}
 		}
 		
 		return sql;
 	}
 }
