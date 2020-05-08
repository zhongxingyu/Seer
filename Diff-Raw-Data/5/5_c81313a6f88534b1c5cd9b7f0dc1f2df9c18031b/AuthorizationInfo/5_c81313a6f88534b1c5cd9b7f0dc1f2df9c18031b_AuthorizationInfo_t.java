 package org.alt60m.ministry.servlet;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.*;
 
 import org.alt60m.ministry.model.dbio.StaffChangeRequest;
 import org.alt60m.util.DBConnectionFactory;
 
 public class AuthorizationInfo {
 
 	public Collection getRegionalPendingApprovals(String region) throws Exception {
 		String sql =
 			"SELECT scr.ChangeRequestID, scr.requestdate, scr.effectivedate, scr.applieddate, type, fk_requestedBy, updateStaff" +
 			" FROM ministry_changerequest scr "
 				+ "INNER JOIN ministry_staff staff ON scr.updateStaff = staff.accountNo "
 				+ "INNER JOIN ministry_authorization auth ON scr.ChangeRequestID = auth.fk_changeRequestID "
 				+ "WHERE staff.region= '"+region+"' AND "
 					+ "auth.authorized = '' AND "
 					+ "auth.role= 'HRRD' AND "
 					+ "auth.sequence = 1 AND "
 					+ "scr.appliedDate IS NULL";
 
 		String sql2 =
 			"SELECT scr.ChangeRequestID, scr.requestdate, scr.effectivedate, scr.applieddate, type, fk_requestedBy, updateStaff FROM ministry_changerequest scr "
 				+ "INNER JOIN ministry_staff staff ON scr.updateStaff = staff.accountNo "
 				+ "INNER JOIN ministry_authorization auth ON scr.ChangeRequestID = auth.fk_changeRequestID "
 				+ "INNER JOIN ministry_fieldchange fc ON scr.ChangeRequestID = fc.Fk_hasFieldChanges "
 				+ "WHERE fc.field = 'region' "
 					+ "AND fc.newValue = '"+region+"' "
 					+ "AND auth.role= 'HRRD' "
 					+ "AND auth.authorized = '' "
 					+ "AND auth.sequence= 2 "
 					+ "AND scr.appliedDate IS NULL";
 		
 		String sql3 =
 			"SELECT scr.ChangeRequestID, scr.requestdate, scr.effectivedate, scr.applieddate, type, fk_requestedBy, updateStaff FROM ministry_changerequest scr "
 				+ "INNER JOIN ministry_staff staff ON scr.updateStaff = staff.accountNo "
 				+ "INNER JOIN ministry_authorization auth ON scr.ChangeRequestID = auth.fk_changeRequestID "
 				+ "INNER JOIN ministry_fieldchange fc ON scr.ChangeRequestID = fc.Fk_hasFieldChanges "
 				+ "WHERE fc.field = 'region' "
 					+ "AND fc.newValue = '"+region+"' "
 					+ "AND auth.role= 'HRRD' "
 					+ "AND auth.authorized = '' "
 					+ "AND auth.sequence= 3 "
 					+ "AND scr.appliedDate IS NULL";
 
 		String sql4 =
 			"SELECT scr.ChangeRequestID, scr.requestdate, scr.effectivedate, scr.applieddate, type, fk_requestedBy, updateStaff FROM ministry_changerequest scr " +
 				"INNER JOIN ministry_staff staff ON scr.updateStaff = staff.accountNo " +
 				"INNER JOIN ministry_authorization auth ON scr.ChangeRequestID = auth.fk_changeRequestID " +
 			"WHERE staff.region = '"+region+"' " +
 				"AND auth.role= 'HRRD' " +
 				"AND auth.authorized = '' " +
 				"AND auth.sequence= 3 " +
 				"AND scr.appliedDate IS NULL " +
 				"AND scr.ChangeRequestID NOT IN " +
 					"(SELECT scr.ChangeRequestID FROM ministry_changerequest scr " +
 							"INNER JOIN ministry_staff staff ON scr.updateStaff = staff.accountNo " +
 							"INNER JOIN ministry_authorization auth ON scr.ChangeRequestID = auth.fk_changeRequestID " +
 							"INNER JOIN ministry_fieldchange fc ON scr.ChangeRequestID = fc.Fk_hasFieldChanges " +
 							"WHERE fc.field = 'region' " +
 								"AND auth.role= 'HRRD' " +
 								"AND auth.authorized = '' " +
 								"AND auth.sequence= 3 " +
 								"AND scr.appliedDate IS NULL)";
 		
 		String sql5 =
 			"SELECT scr.ChangeRequestID, scr.requestdate, scr.effectivedate, scr.applieddate, type, fk_requestedBy, updateStaff" +
 			" FROM ministry_changerequest scr "
 				+ "INNER JOIN ministry_staff staff ON scr.updateStaff = staff.accountNo "
 				+ "INNER JOIN ministry_authorization auth ON scr.ChangeRequestID = auth.fk_changeRequestID "
 				+ "WHERE scr.region= '"+region+"' AND "
 					+ "auth.authorized = '' AND "
 					+ "auth.role= 'HRRD' AND "
 					+ "auth.sequence = 1 AND "
 					+ "scr.appliedDate IS NULL";
 		
 		Collection requests = getRequests(sql);
 		requests.addAll(getRequests(sql2));
 		requests.addAll(getRequests(sql3));
 		requests.addAll(getRequests(sql4));
 		requests.addAll(getRequests(sql5));
 		return requests;
 	}
 
 	public Collection getNonCampusPendingApprovals() throws Exception {
 		String sql =
 			"SELECT scr.ChangeRequestID, scr.requestdate, scr.effectivedate, scr.applieddate, type, fk_requestedBy, updateStaff FROM ministry_changerequest scr " +
 				"INNER JOIN ministry_authorization auth ON scr.ChangeRequestID = auth.fk_changeRequestID " +
 				"WHERE scr.appliedDate IS NULL  " +
 				"AND auth.role = 'HRNC'";
 		return getRequests(sql);
 	}
 
 	public Collection getNationalPendingApprovals() throws Exception {
 
 		String subQuery =
 			"SELECT scro.ChangeRequestID FROM ministry_changerequest scro LEFT OUTER JOIN ministry_authorization auth ON scro.ChangeRequestID = auth.fk_changeRequestID "
				+ "WHERE (auth.authorized = '' OR auth.authorized = 'N') AND (auth.role='HRNC' OR auth.role='HRRD') AND auth.sequence = '1'";
 		String query =
 			"SELECT ChangeRequestID, requestdate, effectivedate, applieddate, type, fk_requestedBy, updateStaff FROM ministry_changerequest scr LEFT OUTER JOIN ministry_authorization auth ON scr.ChangeRequestID = auth.fk_changeRequestID "
				+ "WHERE (auth.authorized = '' OR auth.authorized is null) AND auth.role='HRND' AND (scr.applieddate = '' OR scr.applieddate is null) AND scr.ChangeRequestID not in("+subQuery+")";
 
 		return getRequests(query);
 	}
 	
 	public Collection getRequests(String query) throws Exception {
 
 		Connection conn = DBConnectionFactory.getDatabaseConn();
 		Statement statement = conn.createStatement();
 		ResultSet rs;
 		rs = statement.executeQuery(query);
 
 		Vector v = new Vector();
 		while (rs.next()) {
 			StaffChangeRequest scr = new StaffChangeRequest();
 			scr.setChangeRequestId(rs.getString(1));
 			scr.setRequestDate(rs.getDate(2));
 			scr.setEffectiveDate(rs.getDate(3));
 			scr.setAppliedDate(rs.getDate(4));
 			scr.setType(rs.getString(5));
 			scr.setRequestedById(rs.getString(6));
 			scr.setUpdateStaffId(rs.getString(7));
 			v.add(scr);	
 		}
 
 		rs.close();
 		statement.close();
 		conn.close();
 		
 		return v;
 	}
 }
