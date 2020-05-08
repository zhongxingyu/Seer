 package org.gwaspi.database;
 
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import org.gwaspi.framework.error.GeneralApplicationException;
 import org.gwaspi.framework.jdbc.connection.ConnectionProvider;
 import org.gwaspi.framework.jdbc.query.QueryExecutor;
 import org.gwaspi.framework.jdbc.resultset.RowMappingResultSetHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DbManagerImpl implements DbManager {
 
 	private final Logger log = LoggerFactory.getLogger(DbManagerImpl.class);
 
 	private QueryExecutor qex;
 	private ConnectionProvider connectionProvider;
 
 	public DbManagerImpl(ConnectionProvider cp) {
 		qex = new QueryExecutor(cp);
 		connectionProvider = cp;
 	}
 
 	public ConnectionProvider getConnectionProvider() {
 		return connectionProvider;
 	}
 
 	public void setConnectionProvider(ConnectionProvider connectionProvider) {
 		this.connectionProvider = connectionProvider;
 		qex = new QueryExecutor(connectionProvider);
 	}
 
 	public int executeStatement(String statement) {
 		try {
 			int affectedRows = qex.executeUpdate(statement);
 			return affectedRows;
 		} catch (GeneralApplicationException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public List<Map<String, Object>> selectMutipleClauses(String schema, String table,
 			String[] fields, String[] clauseFields, Object[] clauseValues)
 	{
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT ");
 
 		// append fields
 		for (int i = 0; i < fields.length; i++) {
 			sql.append(fields[i]);
 			if (i != fields.length - 1) {
 				sql.append(",");
 			}
 		}
 
 		sql.append(" FROM ");
 		sql.append(schema).append(".").append(table);
 		sql.append(" WHERE ");
 
 		// append clauses fields
 		makeWhereClause(sql, clauseFields);
 
 		try {
 			qex.executeQuery(sql.toString(), clauseValues,
 					new RowMappingResultSetHandler());
 			return qex.getResults();
 		} catch (GeneralApplicationException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public List<Map<String, Object>> executeSelectStatement(String statement) {
 		try {
 			qex.executeQuery(statement, new RowMappingResultSetHandler());
 			return qex.getResults();
 		} catch (GeneralApplicationException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	private void makeWhereClause(StringBuilder sql, String[] clauseFields) {
 		sql.append("1=1 ");
 		for (int i = 0; i < clauseFields.length; i++) {
 			sql.append(" AND ");
 			sql.append(clauseFields[i]);
 			sql.append("= ? ");
 		}
 	}
 
 	public boolean insertValuesInTable(String schema, String table, String[] fields, Object[] values) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("INSERT INTO ");
 		sql.append(schema).append(".").append(table);
 		sql.append("(");
 		// append fields
 		for (int i = 0; i < fields.length; i++) {
 			sql.append(fields[i]);
 			if (i != fields.length - 1) {
 				sql.append(",");
 			}
 		}
 		sql.append(") VALUES (");
 		// append values
 		for (int i = 0; i < values.length; i++) {
 			sql.append("?");
 			if (i != values.length - 1) {
 				sql.append(",");
 			}
 		}
 		sql.append(")");
 		//sql.append(")  WITH RR");
 
 		try {
 			int affectedRows = qex.executeUpdate(sql.toString(), values);
 			return affectedRows == 1;
 		} catch (GeneralApplicationException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public boolean updateTable(String schema, String table, String[] updateFields, Object[] updateValues,
 			String[] clauseFields, Object[] clauseValues) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE ");
 				sql.append(schema).append(".").append(table);
 		sql.append(" SET ");
 		// append fields
 		for (int i = 0; i < updateFields.length; i++) {
 			sql.append(updateFields[i]);
 			sql.append("= ? ");
 			if (i != updateFields.length - 1) {
 				sql.append(",");
 			}
 		}
 		sql.append(" WHERE ");
 		makeWhereClause(sql, clauseFields);
 		//sql.append(" TRANSACTION_REPEATABLE_READ");
 
 		List<Object> params = new ArrayList<Object>(updateValues.length + clauseValues.length);
 		params.addAll(Arrays.asList(updateValues));
 		params.addAll(Arrays.asList(clauseValues));
 
 		try {
 			int affectedRows = qex.executeUpdate(sql.toString(), params.toArray());
 			return affectedRows > 0;
 		} catch (GeneralApplicationException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public boolean dropTable(String schema, String table) {
 		try {
 			qex.executeUpdate("DROP TABLE " + schema + "." + table);
 
 			return true;
 		} catch (GeneralApplicationException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public boolean createTable(String schema, String table, String[] fieldStatements) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("CREATE TABLE ");
 		sql.append(schema).append(".").append(table);
 		sql.append(" (");
 		// append fields
 		for (int i = 0; i < fieldStatements.length; i++) {
 			sql.append(fieldStatements[i]);
 			if (i != fieldStatements.length - 1) {
 				sql.append(",");
 			}
 		}
 		sql.append(")");
 
 		try {
 			qex.executeUpdate(sql.toString());
 
 			return true;
 		} catch (GeneralApplicationException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public boolean createSchema(String schema) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("CREATE SCHEMA ");
 		sql.append(schema);
 
 		try {
 			qex.executeUpdate(sql.toString());
 
 			return true;
 		} catch (GeneralApplicationException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 
 	public void shutdownConnection() {
 		try {
 			DriverManager.getConnection("jdbc:derby:;shutdown=true");
 		} catch (SQLException ex) {
			log.error(null, ex);
 		}
 	}
 }
