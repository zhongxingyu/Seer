 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.index.sql.h2;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.index.sql.Element;
 import org.eclipse.dltk.core.index.sql.IElementDao;
 import org.eclipse.dltk.core.index.sql.IElementHandler;
 import org.eclipse.dltk.core.index.sql.h2.H2Index;
 import org.eclipse.dltk.core.index2.search.ISearchEngine.MatchRule;
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Data access object for model element
  * 
  * @author michael
  */
 public class H2ElementDao implements IElementDao {
 
 	private static final Pattern SEPARATOR_PATTERN = Pattern.compile(","); //$NON-NLS-1$
 
 	private static final String Q_INSERT_REF = Schema
 			.readSqlFile("resources/insert_ref.sql"); //$NON-NLS-1$
 
 	private static final String Q_INSERT_DECL = Schema
 			.readSqlFile("resources/insert_decl.sql"); //$NON-NLS-1$
 
 	/** Cache for insert element declaration queries */
 	private static final Map<String, String> R_INSERT_QUERY_CACHE = new HashMap<String, String>();
 
 	/** Cache for insert element reference queries */
 	private static final Map<String, String> D_INSERT_QUERY_CACHE = new HashMap<String, String>();
 
 	private String getTableName(Connection connection, int elementType,
 			String natureId, boolean isReference) throws SQLException {
 
 		Schema schema = new Schema();
 		String tableName = schema.getTableName(elementType, natureId,
 				isReference);
 		schema.createTable(connection, tableName, isReference);
 
 		return tableName;
 	}
 
 	public Element insert(Connection connection, int type, int flags,
 			int offset, int length, int nameOffset, int nameLength,
 			String name, String metadata, String qualifier, String parent,
 			int fileId, String natureId, boolean isReference)
 			throws SQLException {
 
 		String tableName = getTableName(connection, type, natureId, isReference);
 
 		String query;
 		if (isReference) {
 			query = R_INSERT_QUERY_CACHE.get(tableName);
 			if (query == null) {
 				query = NLS.bind(Q_INSERT_REF, tableName);
 				R_INSERT_QUERY_CACHE.put(tableName, query);
 			}
 		} else {
 			query = D_INSERT_QUERY_CACHE.get(tableName);
 			if (query == null) {
 				query = NLS.bind(Q_INSERT_DECL, tableName);
 				D_INSERT_QUERY_CACHE.put(tableName, query);
 			}
 		}
 
 		PreparedStatement statement = connection.prepareStatement(query,
 				Statement.RETURN_GENERATED_KEYS);
 		try {
 			int param = 0;
 
 			if (!isReference) {
 				statement.setInt(++param, flags);
 			}
 
 			statement.setInt(++param, offset);
 			statement.setInt(++param, length);
 
 			if (!isReference) {
 				statement.setInt(++param, nameOffset);
 				statement.setInt(++param, nameLength);
 			}
 
 			statement.setString(++param, name);
 
 			if (!isReference) {
 				StringBuilder camelCaseName = new StringBuilder();
 				for (int i = 0; i < name.length(); ++i) {
 					char ch = name.charAt(i);
 					if (Character.isUpperCase(ch)) {
 						camelCaseName.append(ch);
 					} else if (i == 0) {
 						// not applicable for camel case search
 						break;
 					}
 				}
 				statement.setString(++param,
 						camelCaseName.length() > 0 ? camelCaseName.toString()
 								: null);
 			}
 
 			statement.setString(++param, metadata);
 			statement.setString(++param, qualifier);
 
 			if (!isReference) {
 				statement.setString(++param, parent);
 			}
 
 			statement.setInt(++param, fileId);
 
 			statement.executeUpdate();
 			ResultSet result = statement.getGeneratedKeys();
 			try {
 				if (result.next()) {
 					return new Element(result.getInt(1), type, flags, offset,
 							length, nameOffset, nameLength, name, metadata,
 							qualifier, parent, fileId, isReference);
 				}
 			} finally {
 				result.close();
 			}
 		} finally {
 			statement.close();
 		}
 		return null;
 	}
 
 	private String escapeBackslash(String pattern) {
 		return pattern.replaceAll("\\\\", "\\\\\\\\");
 	}
 
 	public void search(Connection connection, String pattern,
 			MatchRule matchRule, int elementType, int trueFlags,
 			int falseFlags, String qualifier, String parent, int[] filesId,
 			int containersId[], String natureId, int limit,
 			boolean isReference, IElementHandler handler,
 			IProgressMonitor monitor) throws SQLException {
 
 		long timeStamp = System.currentTimeMillis();
 		int count = 0;
 
 		String tableName = getTableName(connection, elementType, natureId,
 				isReference);
 
 		Statement statement = connection.createStatement();
 		try {
 			StringBuilder query = new StringBuilder("SELECT * FROM ")
 					.append(tableName);
 
 			// Dummy pattern
 			query.append(" WHERE 1=1");
 
 			// Name patterns
 			if (pattern != null && pattern.length() > 0) {
 				if (isReference && matchRule == MatchRule.CAMEL_CASE) {
 					H2Index
 							.warn("MatchRule.CAMEL_CASE is not supported by element references search."); //$NON-NLS-1$
 					matchRule = MatchRule.EXACT;
 				}
 
 				// Exact pattern
 				if (matchRule == MatchRule.EXACT) {
 					query.append(" AND NAME='").append(pattern).append('\'');
 				}
 				// Prefix
 				else if (matchRule == MatchRule.PREFIX) {
 					query.append(" AND NAME LIKE '").append(
 							escapeBackslash(pattern)).append("%'");
 				}
 				// Camel-case
 				else if (matchRule == MatchRule.CAMEL_CASE) {
 					query.append(" AND CC_NAME LIKE '").append(
 							escapeBackslash(pattern)).append("%'");
 				}
 				// Set of names
 				else if (matchRule == MatchRule.SET) {
 					String[] patternSet = SEPARATOR_PATTERN.split(pattern);
 					query.append(" AND NAME IN (");
 					for (int i = 0; i < patternSet.length; ++i) {
 						if (i > 0) {
 							query.append(',');
 						}
 						query.append('\'').append(patternSet[i]).append('\'');
 					}
 					query.append(')');
 				}
 				// POSIX pattern
 				else if (matchRule == MatchRule.PATTERN) {
 					query.append(" AND NAME LIKE '").append(
 							escapeBackslash(pattern).replace('*', '%').replace(
 									'?', '_')).append("'");
 				}
 			}
 
 			// Flags
 			if (trueFlags != 0) {
 				query.append(" AND BITAND(FLAGS,").append(trueFlags).append(
 						") <> 0");
 			}
 			if (falseFlags != 0) {
 				query.append(" AND BITAND(FLAGS,").append(falseFlags).append(
 						") = 0");
 			}
 
 			// Qualifier
			if (qualifier != null && qualifier.length() > 0) {
 				query.append(" AND QUALIFIER='").append(qualifier).append('\'');
 			}
 			// Parent
			if (parent != null && qualifier.length() > 0) {
 				query.append(" AND PARENT='").append(parent).append('\'');
 			}
 
 			// Files or container paths
 			if (filesId != null) {
 				query.append(" AND FILE_ID IN(");
 				for (int i = 0; i < filesId.length; ++i) {
 					if (i > 0) {
 						query.append(",");
 					}
 					query.append(filesId[i]);
 				}
 				query.append(")");
 
 			} else if (containersId != null) {
 				query
 						.append(" AND FILE_ID IN(SELECT ID FROM FILES WHERE CONTAINER_ID IN(");
 				for (int i = 0; i < containersId.length; ++i) {
 					if (i > 0) {
 						query.append(",");
 					}
 					query.append(containersId[i]);
 				}
 				query.append("))");
 			}
 
 			// Records limit
 			if (limit > 0) {
 				query.append(" LIMIT ").append(limit);
 			}
 			query.append(";");
 
 			if (H2Index.DEBUG) {
 				System.out.println("Query: " + query.toString());
 			}
 
 			ResultSet result = statement.executeQuery(query.toString());
 			try {
 				while (result.next()) {
 					++count;
 					if (monitor != null && monitor.isCanceled()) {
 						return;
 					}
 
 					int columnIndex = 0;
 					int id = result.getInt(++columnIndex);
 
 					int f = 0;
 					if (!isReference) {
 						f = result.getInt(++columnIndex);
 					}
 
 					int offset = result.getInt(++columnIndex);
 					int length = result.getInt(++columnIndex);
 
 					int nameOffset = 0;
 					int nameLength = 0;
 					if (!isReference) {
 						nameOffset = result.getInt(++columnIndex);
 						nameLength = result.getInt(++columnIndex);
 					}
 
 					String name = result.getString(++columnIndex);
 					if (!isReference) {
 						++columnIndex; // skip CC_NAME
 					}
 
 					String metadata = result.getString(++columnIndex);
 					qualifier = result.getString(++columnIndex);
 
 					if (!isReference) {
 						parent = result.getString(++columnIndex);
 					}
 
 					int fileId = result.getInt(++columnIndex);
 
 					handler.handle(new Element(id, elementType, f, offset,
 							length, nameOffset, nameLength, name, metadata,
 							qualifier, parent, fileId, isReference));
 				}
 			} finally {
 				result.close();
 			}
 		} finally {
 			statement.close();
 		}
 
 		if (H2Index.DEBUG) {
 			System.out.println("Results = " + count + " ; Time taken = "
 					+ (System.currentTimeMillis() - timeStamp) + " ms.");
 		}
 	}
 }
