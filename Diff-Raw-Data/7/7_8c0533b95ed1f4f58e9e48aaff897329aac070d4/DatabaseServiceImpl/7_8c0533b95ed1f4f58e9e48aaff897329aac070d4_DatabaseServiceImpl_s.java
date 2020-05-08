 /*******************************************************************************
  * Copyright 2012 Just-Cloud
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.justcloud.osgifier.service.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.CommonDataSource;
 import javax.sql.DataSource;
 import javax.sql.PooledConnection;
 import javax.sql.XADataSource;
 import javax.transaction.TransactionManager;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 
 import com.justcloud.osgifier.annotation.REST;
 import com.justcloud.osgifier.annotation.REST.RESTMethod;
 import com.justcloud.osgifier.annotation.RESTParam;
 import com.justcloud.osgifier.dto.QueryResult;
 import com.justcloud.osgifier.service.DatabaseService;
 
 public class DatabaseServiceImpl implements DatabaseService {
 
 	@Override
 	public String getName() {
 		return "DatabaseService";
 	}
 
 	@Override
 	@REST(url = "/database/query", method = RESTMethod.POST)
 	public QueryResult executeQuery(
 			@RESTParam("jndiDataSource") String jndiDataSource,
 			@RESTParam("query") String query) {
 		Connection conn = null;
 		PooledConnection c = null;
 		try {
 			TransactionManager tm = getTransactionManager(); 
 			tm.begin();
 			boolean resultsAvailable;
 			CommonDataSource commonDs = getDataSource(jndiDataSource);
 			XADataSource xads;
 			DataSource ds;
 
 			if (commonDs instanceof XADataSource) {
 				xads = (XADataSource) commonDs;
 				c = xads.getXAConnection();
 				conn = c.getConnection();
 			} else {
 				ds = (DataSource) commonDs;
 				conn = ds.getConnection();
 			}
 			conn.setAutoCommit(false);
 			PreparedStatement pstmt = conn.prepareStatement(query);
 			resultsAvailable = pstmt.execute();
 			QueryResult result = new QueryResult();
 			result.setResultsAvailable(resultsAvailable);
 			if (resultsAvailable) {
 				while (pstmt.getUpdateCount() > -1) {
 					if (pstmt.getMoreResults()) {
 						break;
 					}
 				}
 				buildResult(result, pstmt);
 			} else {
 				tm.commit();
 				result.setUpdateCount(pstmt.getUpdateCount());
 			}
 			return result;
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		} finally {
 			if (conn != null) {
 				try {
 					conn.close();
 				} catch (SQLException e) {
 					throw new RuntimeException(e);
 				}
 			}
 			if (c != null) {
 				try {
 					c.close();
 				} catch (SQLException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 	}
 
 	private void buildResult(QueryResult result, PreparedStatement pstmt)
 			throws Exception {
 		ResultSet rs = pstmt.getResultSet();
 		ResultSetMetaData meta = rs.getMetaData();
 
 		List<String> headers = new ArrayList<String>(meta.getColumnCount());
 		List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
 
 		for (int i = 1; i < meta.getColumnCount() + 1; i++) {
 			headers.add(meta.getColumnLabel(i));
 		}
 
 		while (rs.next()) {
 			Map<String, Object> row = new HashMap<String, Object>();
 			for (int i = 1; i < headers.size() + 1; i++) {
				row.put(meta.getColumnLabel(i), rs.getObject(i));
 			}
 			values.add(row);
 		}
 
 		result.setHeaders(headers);
 		result.setValues(values);
 
 	}
 
 	private CommonDataSource getDataSource(String dataSource) {
 		try {
 			Context context = new InitialContext();
 			return (CommonDataSource) context.lookup("jdbc/" + dataSource);
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	private TransactionManager getTransactionManager() {
 		BundleContext bc = getBundleContext();
 		ServiceReference sr = bc.getServiceReference(TransactionManager.class
 				.getName());
 		TransactionManager tm = (TransactionManager) bc.getService(sr);
 		return tm;
 	}
 
 	private BundleContext getBundleContext() {
 		return FrameworkUtil.getBundle(DatabaseServiceImpl.class)
 				.getBundleContext();
 	}
 
 }
