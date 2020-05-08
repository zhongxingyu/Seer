 package com.eucalyptus.webui.server.user;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import com.eucalyptus.webui.server.db.DBProcWrapper;
 import com.eucalyptus.webui.server.db.ResultSetWrapper;
 import com.eucalyptus.webui.server.dictionary.DBTableColName;
 import com.eucalyptus.webui.server.dictionary.DBTableName;
 import com.eucalyptus.webui.shared.user.EnumUserAppResult;
 import com.eucalyptus.webui.shared.user.EnumUserAppState;
 import com.eucalyptus.webui.shared.user.UserApp;
 import com.eucalyptus.webui.shared.user.UserAppStateCount;
 import com.google.common.base.Strings;
import com.google.gwt.thirdparty.guava.common.collect.Lists;
 
 public class UserAppDBProcWrapper {
 	public void addUserApp(UserApp userApp) throws UserSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		String sql = addUserAppSql(userApp);
 		
 		System.out.println(sql);
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			throw new UserSyncException ("Database fails");
 		}
 	}
 	
 	public void updateUserApp(UserApp userApp) throws UserSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		String sql = updateUserAppSql(userApp);
 		
 		System.out.println(sql);
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			throw new UserSyncException ("Database fails");
 		}
 	}
 	
 	public ResultSetWrapper queryUserApp(int accountId, int userId, EnumUserAppState state) throws UserSyncException {
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		StringBuilder sql = userAppAccountUserViewSql();
 				
 		//EnumUserAppState.DEFAULT means that query all the user applications
 		if (state != EnumUserAppState.NONE) {
 			sql.append(" AND ").
 			append(DBTableName.USER_APP).
 			append(".").
 			append(DBTableColName.USER_APP.STATE).
 			append(" = ").
 			append(state.ordinal());
 		}
 		
 		if (accountId != 0) {
 			sql.append(" AND ").
 			append(DBTableName.USER).
 			append(".").
 			append(DBTableColName.USER.ACCOUNT_ID).
 			append(" = ").
 			append(accountId);
 		}
 		
 		if (userId != 0) {
 			sql.append(" AND ").
 			append(DBTableName.USER_APP).
 			append(".").
 			append(DBTableColName.USER_APP.USER_ID).
 			append(" = ").
 			append(userId);
 		}
 		
 		System.out.println(sql.toString());
 		
 		try {
 			ResultSetWrapper result = dbProc.query(sql.toString());
 			
 			return result;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserSyncException("Fail to query user apps");
 		}
 	}
 	
 	public void delUserApps(ArrayList<String> ids) throws UserSyncException {
 		if (ids == null || ids.size() == 0)
 			return;
 		
 		String sql = delUserAppSql(ids);
 		
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserSyncException("Database fails");
 		}
 	}
 	
 	public void updateUserState(ArrayList<String> ids, EnumUserAppState state) throws UserSyncException {
 		if (ids == null || ids.size() == 0)
 			return;
 		
 		String sql = updateUserAppStateSql(ids, state);
 		
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		try {
 			dbProc.update(sql);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserSyncException("Database fails");
 		}
 	}
 	
 	public ArrayList<UserAppStateCount> countUserAppByState(int accountId, int userId) throws UserSyncException {
 		String sql = countUserAppByStateSql(accountId, userId);
 		
 		DBProcWrapper dbProc = DBProcWrapper.Instance();
 		
 		try {
 			ResultSetWrapper rsw = dbProc.query(sql);
 			ResultSet rs = rsw.getResultSet();
 			
             if (rs != null) {
             	ArrayList<UserAppStateCount> counts = Lists.newArrayList();
             	
             	while (rs.next()) { 
             		
             		EnumUserAppState appState = EnumUserAppState.NONE;
 					
 					String appStateStr = rs.getString(DBTableColName.USER_APP.STATE);
 					if (!Strings.isNullOrEmpty(appStateStr))
 						appState = EnumUserAppState.values()[Integer.valueOf(appStateStr)];
 					
 					int count = 0;
 					String countStr = rs.getString("count");
 					if (!Strings.isNullOrEmpty(countStr))
 						count = Integer.valueOf(countStr);
 						
 					UserAppStateCount userAppState = new UserAppStateCount();
 					userAppState.setCountValue(appState, count);
 					
 					counts.add(userAppState);
                 }
             	
             	rsw.close();
             	return counts;
             } 
             else {	
 				rsw.close();
 				return null;
 			}
 			
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			throw new UserSyncException("Database fails");
 		}
 	}
 	
 	private String addUserAppSql(UserApp userApp) {
 		StringBuilder str = new StringBuilder();
 		str.append("INSERT INTO ").
 		append(DBTableName.USER_APP).append(" ( ").
 		append(DBTableColName.USER_APP.ID).append(", ").
 		append(DBTableColName.USER_APP.APP_TIME).append(", ").
 		append(DBTableColName.USER_APP.SRV_STARTINGTIME).append(", ").
 		append(DBTableColName.USER_APP.SRV_ENDINGTIME).append(", ").
 		append(DBTableColName.USER_APP.STATE).append(", ").
 		append(DBTableColName.USER_APP.RESULT).append(", ").
 		append(DBTableColName.USER_APP.DEL).append(", ").
 		append(DBTableColName.USER_APP.COMMENT).append(", ").
 		append(DBTableColName.USER_APP.USER_ID).append(", ").
 		append(DBTableColName.USER_APP.TEMPLATE_ID).append(", ").
 		append(DBTableColName.USER_APP.VM_IMAGE_TYPE_ID).
 		append(") VALUES (null, ");
 		
 		str.append("'");
 		str.append(dateformat.format(userApp.getAppTime()).toString());
 		str.append("', '");
 		
 		str.append(dateformat.format(userApp.getSrvStartingTime()).toString());
 		str.append("', '");
 		
 		str.append(dateformat.format(userApp.getSrvEndingTime()).toString());
 		str.append("', ");
 		
 		str.append(userApp.getState().ordinal());
 		str.append(", ");
 		
 		str.append(userApp.getResult().ordinal());
 		str.append(", ");
 		
 		str.append(userApp.getDelState());
 		str.append(", ");
 		
 		if (Strings.isNullOrEmpty(userApp.getComments())) {
 			str.append("null, ");
 		}
 		else {
 			str.append("'").append(userApp.getComments());
 			str.append("', ");
 		}
 		
 		str.append(userApp.getUserId());
 		str.append(", ");
 		
 		str.append(userApp.getTemplateId());
 		str.append(", ");
 		
 		str.append(userApp.getVmIdImageTypeId());
 		
 		str.append(")");
 		
 		return str.toString();
 	}
 	
 	private String updateUserAppSql(UserApp userApp) {
 		StringBuilder str = new StringBuilder("UPDATE ").append(DBTableName.USER_APP).append(" SET ");
 		
 		if (userApp.getState() != EnumUserAppState.NONE) {
 			str.append(DBTableColName.USER_APP.STATE).append(" = '").
 			append(userApp.getState().ordinal()).
 			append("', ");
 		}
 		
 		if (userApp.getResult() != EnumUserAppResult.NONE) {
 			str.append(DBTableColName.USER_APP.RESULT).append(" = '").
 			append(userApp.getResult().ordinal()).
 			append("', ");
 		}
 		
 		if (userApp.getAppTime() != null) {
 			str.append(DBTableColName.USER_APP.APP_TIME).append(" = '").
 			append(dateformat.format(userApp.getAppTime()).toString()).
 			append("', ");
 		}
 		
 		if (userApp.getSrvStartingTime() != null) {
 			str.append(DBTableColName.USER_APP.SRV_STARTINGTIME).append(" = '").
 			append(dateformat.format(userApp.getSrvStartingTime()).toString()).
 			append("', ");
 		}
 		
 		if (userApp.getSrvEndingTime() != null) {
 			str.append(DBTableColName.USER_APP.SRV_ENDINGTIME).append(" = '").
 			append(dateformat.format(userApp.getSrvEndingTime())).
 			append("', ");
 		}
 		
 		if (userApp.getComments() != null) {
 			str.append(DBTableColName.USER_APP.COMMENT).append(" = '").
 			append(userApp.getComments()).
 			append("', ");
 		}
 		
 		if (userApp.getTemplateId() != 0) {
 			str.append(DBTableColName.USER_APP.TEMPLATE_ID).append(" = ").
 			append(userApp.getTemplateId()).
 			append(", ");
 		}
 		
 		if (userApp.getVmIdImageTypeId() != 0) {
 			str.append(DBTableColName.USER_APP.USER_ID).append(" = ").
 			append(userApp.getVmIdImageTypeId()).
 			append(", ");
 		}
 		
 		if (str.length() > 2)
 			str.delete(str.length() -2, str.length());
 		
 		str.append(" WHERE ").append(DBTableColName.USER_APP.ID).append(" = ").
 		append(userApp.getUAId());
 		
 		return str.toString();
 	}
 	
 	private StringBuilder userAppAccountUserViewSql() {
 		StringBuilder sql = new StringBuilder("SELECT ").
 				append(DBTableName.USER_APP).append(".*").
 				append(", ").
 				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.NAME).
 				append(", ").
 				append(DBTableName.USER).append(".*").
 				append(", ").
 				append(DBTableName.TEMPLATE).append(".*").
 				append(", ").
 				append(DBTableName.VM_IMAGE_TYPE).append(".*").
 				
 				append(" FROM ").
 				append("( ").
 				append(DBTableName.USER_APP).
 				
 				append(" LEFT JOIN ").
 				append(DBTableName.USER).
 				append(" ON ").
 				append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
 				append(" = ").
 				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.USER_ID).
 				append(" ) ").
 				
 				append(" LEFT JOIN ").
 				append(DBTableName.ACCOUNT).
 				append(" ON ").
 				append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
 				append(" = ").
 				append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
 				
 				append(" LEFT JOIN ").
 				append(DBTableName.TEMPLATE).
 				append(" ON ").
 				append(DBTableName.TEMPLATE).append(".").append(DBTableColName.TEMPLATE.ID).
 				append(" = ").
 				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.TEMPLATE_ID).
 				
 				append(" LEFT JOIN ").
 				append(DBTableName.VM_IMAGE_TYPE).
 				append(" ON ").
 				append(DBTableName.VM_IMAGE_TYPE).append(".").append(DBTableColName.VM_IMAGE_TYPE.ID).
 				append(" = ").
 				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.VM_IMAGE_TYPE_ID).
 				
 				append(" WHERE ").
 				append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.DEL).
 				append(" = 0 ");
 		
 		return sql;
 	}
 	
 	private String delUserAppSql(ArrayList<String> ids) {
 
 		StringBuilder sql = new StringBuilder("UPDATE ").append(DBTableName.USER_APP).append(" SET ");
 		
 		sql.append(DBTableColName.USER_APP.DEL).append(" = 1 ");
 		sql.append(" WHERE ");
 		
 		for (String str : ids) {
 			sql.append(DBTableColName.USER_APP.ID).append(" = ").
 			append(str).
 			append(" OR ");
 		}
 		
 		sql.delete(sql.length() -3 , sql.length());
 		
 		System.out.println(sql);
 		
 		return sql.toString();
 	}
 	
 	private String updateUserAppStateSql(ArrayList<String> ids, EnumUserAppState state) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("UPDATE ").
 		append(DBTableName.USER_APP).
 		append(" SET ").
 		append(DBTableColName.USER_APP.STATE).
 		append(" = ").
 		append(state.ordinal()).
 		append(" WHERE ");
 		
 		for (String str : ids) {
 			sql.append(DBTableColName.USER_APP.ID).
 			append(" = '").
 			append(str).
 			append("' or ");
 		}
 		
 		sql.delete(sql.length() -3 , sql.length());
 		
 		System.out.println(sql);
 		
 		return sql.toString();
 	}
 	
 	private String countUserAppByStateSql(int accountId, int userId) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("SELECT ").
 		append(DBTableColName.USER_APP.STATE).append(", ").
 		append(" COUNT(*) AS count").
 		append(" FROM ").
 		append("( ").
 		append(DBTableName.USER_APP).
 		append(" LEFT JOIN ").
 		append(DBTableName.USER).
 		append(" ON ").
 		append(DBTableName.USER).append(".").append(DBTableColName.USER.ID).
 		append(" = ").
 		append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.USER_ID).
 		append(" ) ").
 		append(" LEFT JOIN ").
 		append(DBTableName.ACCOUNT).
 		append(" ON ").
 		append(DBTableName.USER).append(".").append(DBTableColName.USER.ACCOUNT_ID).
 		append(" = ").
 		append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).
 		append(" WHERE 1=1 AND ").
 		append(DBTableName.USER_APP).append(".").append(DBTableColName.USER_APP.DEL).append(" = 0 ");;
 		
 		if (accountId > 0)
 			sql.append(" AND ").append(DBTableName.ACCOUNT).append(".").append(DBTableColName.ACCOUNT.ID).append(" = ").append(accountId);
 		
 		if (userId > 0)
 			sql.append(" AND ").append(DBTableName.USER).append(".").append(DBTableColName.USER_APP.USER_ID).append(" = ").append(userId);
 		
 		sql.append(" GROUP BY ").
 		append(DBTableColName.USER_APP.STATE);
 				
 		System.out.println(sql);
 		
 		return sql.toString();
 	}
 	
 	SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
 }
