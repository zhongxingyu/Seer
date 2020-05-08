 /*
  * Copyright (c) 2012, 2013 All Rights Reserved, www.tiq-solutions.com
  * 
  * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND,
  * EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED 
  * WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A PARTICULAR PURPOSE.
  * 
  * This code is product of:
  * 
  * TIQ Solutions GmbH 
  * Weißenfelser Str. 84
  * 04229 Leipzig, Germany
  * 
  * info@tiq-solutions.com
  * 
  */
 package de.tiq.beeswax.jdbc;
 
 import java.sql.Ref;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.RowId;
 import java.sql.SQLException;
 import java.sql.SQLXML;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.hadoop.hive.metastore.api.Schema;
 import org.apache.thrift.TException;
 
 import com.cloudera.beeswax.api.Query;
 import com.cloudera.beeswax.api.QueryHandle;
 import com.cloudera.beeswax.api.QueryNotFoundException;
 import com.cloudera.beeswax.api.Results;
 import com.cloudera.beeswax.api.ResultsMetadata;
 
 import de.tiq.jdbc.QueryExecutor;
 import de.tiq.jdbc.annotations.JdbcDriver;
import de.tiq.jdbc.defaultimpl.AbstractReadonlyResultSet;
import de.tiq.jdbc.defaultimpl.DefaultResultSetMetaData;
 
 @JdbcDriver(name = "BeeswaxDriver", packageDefinition = "de.tiq.beeswax.jdbc", scheme = "beeswax")
 public class Executor extends QueryExecutor<BeeswaxConnectionHandler> {
 
 	private BeeswaxConnectionHandler handle;
 	private ResultSet resultSet;
 
 	public Executor(BeeswaxConnectionHandler handle) {
 		super(handle);
 		this.handle = handle;
 	}
 
 	@Override
 	public ResultSet executeQuery(final String sql) throws SQLException {
 		execute(sql);
 		return getResultSet();
 	}
 	
 	@Override
 	public boolean execute(String sql) throws SQLException {
 		Query query = new Query().setHadoop_user("hdfs").setQuery(sql);
 		try {
 			QueryHandle queryHandle = handle.getClient().query(query);
 			resultSet = getResultSet(queryHandle);
 			return resultSet != null;
 		} catch (Exception e) {
 			throw new SQLException(e);
 		}
 	}
 	
 	@Override
 	public ResultSet getResultSet() throws SQLException {
 		return resultSet;
 	}
 
 	@Override
 	public int executeUpdate(String sql) throws SQLException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	
 	private ResultSet getResultSet(final QueryHandle queryHandle) {
 		return new AbstractReadonlyResultSet() {
 			
 			private List<String> data = Collections.emptyList();
 			private String row;
 			private String field;
 			private boolean closed = false;
 			
 			@Override
 			public boolean next() throws SQLException {
 				if (closed)
 					return false;
 				
 				if (data.isEmpty()) {
 					try {
 						Results results = handle.getClient().fetch(
 								queryHandle, false, -1);
 						
 						data = results.getData();
 						if (data.isEmpty()) {
 							handle.getClient().close(queryHandle);
 							closed = true;
 						}
 					} catch (Exception e) {
 						throw new SQLException(e);
 					}
 				}
 				
 				if (data.isEmpty()) {
 					row = null;
 				} else {
 					row = data.remove(0);
 				}
 				return row != null;
 			}
 			
 			private String getField(int columnIndex) throws SQLException {
 				String[] split = row.split("\t");
 				if (columnIndex > split.length) {
 					field = "";
 				} else {
 					field = split[columnIndex - 1];
 				}
 				return field;
 			}
 
 			@Override
 			public void close() throws SQLException {
 				try {
 					handle.getClient().close(queryHandle);
 					closed = true;
 				} catch (Exception e) {
 					throw new SQLException(e);
 				}
 			}
 			
 			@Override
 			public String getString(int columnIndex) throws SQLException {
 				return getField(columnIndex);
 			}
 			
 			@Override
 			public Object getObject(int columnIndex) throws SQLException {
 				return getField(columnIndex);
 			}
 			
 			@Override
 			public boolean wasNull() throws SQLException {
 				return field == null || field.trim().length() == 0;
 			}
 			
 			@Override
 			public ResultSetMetaData getMetaData() throws SQLException {
 				return new DefaultResultSetMetaData() {
 					private Schema schema;
 					
 					@Override
 					public int getColumnCount() throws SQLException {
 						try {
 							fetchSchemaIfNeeded(queryHandle);
 							return schema.getFieldSchemasSize();
 						} catch (QueryNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (TException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						return 0;
 					}
 					
 					@Override
 					public String getColumnName(int arg0)
 							throws SQLException {
 						try {
 							fetchSchemaIfNeeded(queryHandle);
 							return schema.getFieldSchemas().get(arg0 - 1)
 									.getName();
 						} catch (QueryNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (TException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						return null;
 					}
 					
 					@Override
 					public int getColumnType(int arg0) throws SQLException {
 						String type = getColumnTypeName(arg0);
 						
 						// TODO
 						if ("string".equals(type)) {
 							return Types.VARCHAR;
 						}
 						if ("bigint".equals(type)) {
 							return Types.BIGINT;
 						}
 						System.out.println("TIQ ResultSetMetaData#getColumnType unknown column type: " + type);
 						return Types.JAVA_OBJECT;
 					}
 					
 					@Override
 					public String getColumnTypeName(int arg0)
 							throws SQLException {
 						try {
 							fetchSchemaIfNeeded(queryHandle);
 							return schema.getFieldSchemas()
 									.get(arg0 - 1).getType();
 						} catch (QueryNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (TException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						// TODO Auto-generated method stub
 						return null;
 					}
 					
 					private void fetchSchemaIfNeeded(QueryHandle queryHandle)
 							throws QueryNotFoundException, TException {
 						if (schema == null) {
 							ResultsMetadata metadata = handle.getClient()
 									.get_results_metadata(queryHandle);
 							schema = metadata.getSchema();
 						}
 					}
 				};
 			}
 
 			@Override
 			public SQLXML getSQLXML(int columnIndex) throws SQLException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public RowId getRowId(int columnIndex) throws SQLException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public Ref getRef(int columnIndex) throws SQLException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public int findColumn(String columnLabel) throws SQLException {
 				// TODO Auto-generated method stub
 				return 0;
 			}
 
 			@Override
 			public Statement getStatement() throws SQLException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 		};
 	}
 }
