 package org.code.metrxn.repository.dml;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.StringTokenizer;
 
 import org.code.metrxn.util.DBUtil;
 import org.code.metrxn.util.Logger;
 
 public class InsertRepository {
 
 	Connection dbConnection ;
 
 	public InsertRepository() {
 		dbConnection = DBUtil.getConnection();
 	}
 
 	public InsertRepository(Connection dbConnection) {
 		this.dbConnection = dbConnection;
 	}
 
 	public boolean metabolitesUpload(String workFlowId) {
 		String colNames = "select group_concat(distinct dbColumn order by fileColumn) from entity_mapping " 
 				+ " where mappingStatus = 1 and workFlowId = '" + workFlowId + "'" 
 				+ " group by workFlowId; ";
 		String colName = "";
 		try {
 			Statement fetchCols = dbConnection.createStatement();
 			ResultSet colResult = fetchCols.executeQuery(colNames);
 			colResult.next();
 			colName = colResult.getString(1);
 		} catch (SQLException e) {
 			Logger.error("exception occured while populating the file data to the metabolite", InsertRepository.class);
 			Logger.error(e.getMessage(), InsertRepository.class);
 		}
 		String fetchData = " SELECT group_concat(distinct concat('',columnData,'')  order by columnName SEPARATOR '|||'), M.workFlowId, E.rowId "
 				+ " FROM test_Metrxn_version_2.entity_data E, entity_mapping M " 
 				+ " where E.columnName = M.fileColumn "
 				+ " and M.mappingStatus = 1 "  
 				+ " and M.workFlowId = E.workFlowId"
 				+ " and M.workFlowId = '" + workFlowId + "'" 
 				+ " group by M.workFlowId, E.rowId";
 		try {
 			Statement fetchRows = dbConnection.createStatement();
 			ResultSet cols = fetchRows.executeQuery(fetchData);
 			PreparedStatement insertStmt;
 			while (cols.next()) {
 				StringBuilder placeHolder = new StringBuilder("?");
 				StringTokenizer colTokens = new StringTokenizer(cols.getString(1), "|||");
 				Logger.info(cols.getString(1), InsertRepository.class);
 				for (int i = 1; i < colTokens.countTokens(); i++) 
 					placeHolder.append(", ?");
 				String insertData = "insert into metabolitesUpload(" + colName  + " ) values(" + placeHolder.toString() + ")";
 				insertStmt = dbConnection.prepareStatement(insertData);
 				PreparedStatement insertPS = dbConnection.prepareStatement(insertData);
 				for (int index = 1; colTokens.hasMoreTokens(); index++) {
 					insertPS.setString(index, colTokens.nextToken());
 					Logger.info("inside values appender", InsertRepository.class);
 				}
 				Logger.info(insertData, InsertRepository.class);
				insertStmt.executeUpdate();
 			}
 		} catch (SQLException e) {
 			Logger.error("exception occured while populating the file data to the metabolite", InsertRepository.class);
 			e.printStackTrace();
 		}                
 		return false;
 	}
 }
