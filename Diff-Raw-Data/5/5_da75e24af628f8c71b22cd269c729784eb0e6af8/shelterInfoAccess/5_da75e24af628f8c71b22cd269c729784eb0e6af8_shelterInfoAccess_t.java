 package edu.kgu.aeon.access;
 
 import java.sql.ResultSet;
 import java.util.ArrayList;
 
 import edu.kgu.aeon.bean.shelterInfoBean;
 import edu.kgu.log.LogLogger;
 
 public class shelterInfoAccess extends BaseAccess {
 	public int deleteShelterinfo(shelterInfoBean value) {
 		int rtn = -1;
 		
 		dbConn = conn.getDBConnect();
 		StringBuffer query = new StringBuffer();
 		query.append("DELETE FROM shelterInfo ");
 		query.append(" WHERE userID = '" + value.getUserID() + "'");
 		query.append("   AND DLNo = '" + value.getDlNo() + "'");
 		
 		try {
 			dbConn.BeginTransaction();
 			dbConn.Execute(query.toString());
 			dbConn.Commit();
 			rtn = 0;
 		} catch (Exception e) {
 			LogLogger.error(e);
 		} finally {
 			dbConn.Close();
 		}
 		
 		return rtn;
 	}
 	
 	public int insertShelterInfo(shelterInfoBean value) {
 		int rtn = -1;
 		
 		dbConn = conn.getDBConnect();
 		StringBuffer query = new StringBuffer();
 		query.append(" INSERT INTO shelterInfo ");
 		query.append("   	 (userID ");
 		query.append("       ,DLNo");
 		query.append("       ,Lat");
 		query.append("       ,Lng");
 		query.append("       ,PlaceName");
 		query.append("       ,FID");
 		query.append("       ,Address");
 		query.append("       ,TEL");
 		query.append("		 ,comment)");
 		query.append("   VALUES ('" + value.getUserID() + "'");
 		query.append("			,'" + value.getDlNo() +"'");
 		query.append("			,'" + value.getLat() + "'");
 		query.append("			,'" + value.getLng() + "'");
 		query.append("			,'" + value.getPlaceName() + "'");
 		query.append("			,'" + value.getFid() + "'");
 		query.append("			,'" + value.getAddress() + "'");
 		query.append("			,'" + value.getTel() + "'");
 		query.append("			,'" + value.getComment() + "')");
 		
 		try {
 			dbConn.BeginTransaction();
 			dbConn.Execute(query.toString());
 			dbConn.Commit();
 			rtn = 0;
 		} catch (Exception e) {
 			LogLogger.error(e);
 		} finally {
 			dbConn.Close();
 		}
 		
 		return rtn;
 	}
 	
 	public String getMaxDLNo(String userID) {
 		String rtn = "0";
 		ResultSet result;
 		
 		dbConn = conn.getDBConnect();
 		StringBuffer query = new StringBuffer();
 		query.append(" SELECT IFNULL(MAX(DLNo),0) DLNo ");
 		query.append("   FROM shelterInfo ");
 		query.append("  WHERE userID = '" + userID + "'");
 		
 		try {
 			result = dbConn.ExecuteQuery(query.toString());
 			if (result.next()) {
 				rtn = result.getString("DLNo");
 			}
 		} catch (Exception e) {
 			LogLogger.error(e);
 		} finally {
 			dbConn.Close();
 		}
 		
 		return rtn;
 	}
 	
 	public ArrayList<shelterInfoBean> getShelterListByUserID(String userID) {
 		ArrayList<shelterInfoBean> rtn = new ArrayList<shelterInfoBean>();
 		ResultSet result;
 		
 		dbConn = conn.getDBConnect();
 		StringBuffer query = new StringBuffer();
 		query.append(" SELECT DLNo ");
		query.append("		 ,lat ");
		query.append("		 ,lng ");
 		query.append("       ,PlaceName ");
 		query.append("       ,FID ");
 		query.append("       ,Address ");
 		query.append("       ,TEL ");
 		query.append("       ,comment ");
 		query.append("   FROM shelterInfo ");
 		query.append("  WHERE userID = '" + userID + "'");
 		query.append("  ORDER BY DLNo");
 		
 		try {
 			result = dbConn.ExecuteQuery(query.toString());
 			
 			while (result.next()) {
 				shelterInfoBean bean = new shelterInfoBean();
 				bean.setUserID(userID);
 				bean.setDlNo(result.getString("DLNo"));
				bean.setLat(result.getString("lat"));
				bean.setLng(result.getString("lng"));
 				bean.setPlaceName(result.getString("PlaceName"));
 				bean.setFid(result.getString("FID"));
 				bean.setAddress(result.getString("Address"));
 				bean.setTel(result.getString("TEL"));
 				bean.setComment(result.getString("comment"));
 				rtn.add(bean);
 			}
 		} catch (Exception e) {
 			LogLogger.error(e);
 		} finally {
 			dbConn.Close();
 		}
 		
 		return rtn;
 	}
 }
