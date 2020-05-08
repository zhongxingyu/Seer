 package gov.nih.nci.cadsr.persist.de;
 
 import gov.nih.nci.cadsr.persist.common.BaseVO;
 import gov.nih.nci.cadsr.persist.common.DBConstants;
 import gov.nih.nci.cadsr.persist.common.ACBase;
 import gov.nih.nci.cadsr.persist.common.DBHelper;
 import gov.nih.nci.cadsr.persist.exception.DBException;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Logger;
 
 @SuppressWarnings("unchecked")
 public class Data_Elements_Mgr extends ACBase {
 
 	private Logger logger = Logger.getLogger(this.getClass());
 
 	/**
 	 * Inserts a single row of Data Element and returns primary key de_IDSEQ
 	 * 
 	 * @param deVO
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public String insert(BaseVO vo, Connection conn) throws DBException {
 		DeVO deVO = (DeVO) vo;
 		PreparedStatement statement = null;
 		String primaryKey = null;
 		// generate de_IDSEQ(primary key) 
 		deVO.setDe_IDSEQ(this.generatePrimaryKey(conn));
 		deVO.setDeleted_ind(DBConstants.RECORD_DELETED_NO);
 		deVO.setDate_created(new java.sql.Timestamp(new java.util.Date().getTime()));
 		try {
 			String sql = "insert into data_elements_view ( de_idseq, version, conte_idseq, preferred_name, vd_idseq, dec_idseq, "
 					+ "preferred_definition, asl_name, long_name, latest_version_ind, deleted_ind, "
 					+ "date_created, begin_date, created_by, end_date, date_modified, modified_by, change_note, origin) "
 					+ "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
 			int column = 0;
 			statement = conn.prepareStatement(sql);
 			statement.setString(++column, deVO.getDe_IDSEQ());
 			statement.setDouble(++column, deVO.getVersion());
 			statement.setString(++column, deVO.getConte_IDSEQ());
 			statement.setString(++column, deVO.getPrefferred_name());
 			statement.setString(++column, deVO.getVd_IDSEQ());
 			statement.setString(++column, deVO.getDec_IDSEQ());
 			statement.setString(++column, deVO.getPrefferred_def());
 			statement.setString(++column, deVO.getAsl_name());
 			statement.setString(++column, deVO.getLong_name());
 			statement.setString(++column, deVO.getLastest_version_ind());
 			statement.setString(++column, deVO.getDeleted_ind());
 			statement.setTimestamp(++column, deVO.getDate_created());
 			statement.setTimestamp(++column, deVO.getBegin_date());
 			statement.setString(++column, deVO.getCreated_by());
 			statement.setTimestamp(++column, deVO.getEnd_date());
 			statement.setTimestamp(++column, deVO.getDate_modified());
 			statement.setString(++column, deVO.getModified_by());
 			statement.setString(++column, deVO.getChange_note());
 			statement.setString(++column, deVO.getOrigin());
 		
 			int count = statement.executeUpdate();
 			if (count == 0) {
 				throw new Exception("Unable to insert the record");
 			} else {
 				primaryKey = deVO.getDe_IDSEQ();
 				if (logger.isDebugEnabled()) {
 					logger.debug("Inserted DE");
 					logger.debug("de_IDSEQ(primary key )-----> " + primaryKey);
 				}
 			}
 
 		} catch (Exception e) {
 			logger.error("Error inserting Data Element " + e);
 			errorList.add(DeErrorCodes.API_DE_500);
 			throw new DBException(errorList);
 		} finally {
 			DBHelper.close(statement);
 		}
 		return primaryKey;
 	}
 
 	/**
 	 * Updates single row of Data Element
 	 * 
 	 * @param deVO
 	 * @param conn
 	 * @throws DBException
 	 */
 	public void update(BaseVO vo, Connection conn) throws DBException {
 		DeVO deVO = (DeVO) vo;
 		Statement statement = null;
 		deVO.setDeleted_ind(DBConstants.RECORD_DELETED_NO);
 		
 		deVO.setDate_modified(new java.sql.Timestamp(new java.util.Date().getTime()));
 		
 		// logger.debug("updateClient()");
 		try {
 			StringBuffer sql = new StringBuffer();
 			sql.append(" update data_elements_view ");
 			sql.append("set date_modified = timestamp '" + deVO.getDate_modified() +"'");
 			sql.append(", modified_by = '" + deVO.getModified_by() + "'");
 			
 			
 			sql.append(", deleted_ind = '" + deVO.getDeleted_ind() + "'");
 
 			if (deVO.getConte_IDSEQ() != null) {
 				sql.append(", conte_idseq = '" + deVO.getConte_IDSEQ() + "'");
 			}
 			if (deVO.getPrefferred_name() != null) {
 				sql.append(", preferred_name = '" + deVO.getPrefferred_name() + "'");
 			}
 			if (deVO.getVd_IDSEQ() != null) {
 				sql.append(", vd_idseq = '" + deVO.getVd_IDSEQ() + "'");
 			}
 			if (deVO.getDec_IDSEQ() != null) {
 				sql.append(", dec_idseq = '" + deVO.getDec_IDSEQ() + "'");
 			}
 			if (deVO.getPrefferred_def() != null) {
 				sql.append(", preferred_definition = '"	+ deVO.getPrefferred_def() + "'");
 			}
 			if (deVO.getAsl_name() != null) {
 				sql.append(", asl_name = '" + deVO.getAsl_name() + "'");
 			}
 			if (deVO.getLong_name() != null) {
 				// allow null update
 				if (deVO.getLong_name() == "") {
 					sql.append(", long_name = ''");
 				} else {
 					sql.append(", long_name = '" + deVO.getLong_name() + "'");
 				}
 			}
 			if (deVO.getLastest_version_ind() != null) {
 				sql.append(", latest_version_ind = '" + deVO.getLastest_version_ind() + "'");
 			}
 			
 			if (deVO.getBegin_date() != null){
 				sql.append(", begin_date = timestamp '" + deVO.getBegin_date() + "'");
 			}else
 				// allow null updates
 				sql.append(", begin_date = ''");
 			
 			if (deVO.getEnd_date() != null){
 				sql.append(",  end_date = timestamp '" + deVO.getEnd_date() + "'");
 			}else
 				// allow null updates
 				sql.append(",  end_date = ''");
 			
 			
 			if (deVO.getChange_note() != null) {
 				// allow null updates
 				if (deVO.getChange_note() == "") {
 					sql.append(",  change_note = ''");
 				} else {
 					sql.append(",  change_note = '" + deVO.getChange_note()	+ "'");
 				}
 			}
 			if (deVO.getOrigin() != null) {
 				// allow null updates
 				if (deVO.getOrigin() == "") {
 					sql.append(",  origin= ''");
 				} else {
 					sql.append(",  origin= '" + deVO.getOrigin() + "'");
 				}
 			}
 
 			sql.append(" where de_idseq = '" + deVO.getDe_IDSEQ() + "'");
 
 			statement = conn.createStatement();
 			int result = statement.executeUpdate(sql.toString());
 
 			if (result == 0) {
 				throw new Exception("Unable to Update");
 			}
 		} catch (Exception e) {
 			logger.error("Error updating Data Element " + deVO.getDe_IDSEQ() + e);
 			errorList.add(DeErrorCodes.API_DE_501);
 			throw new DBException(errorList);
 		} finally {
 			DBHelper.close(statement);
 		}
 
 	}
 
 	/**
 	 * Deletes single row of Data Element
 	 * 
 	 * @param de_IDSEQ
 	 * @param modified_by
 	 * @param conn
 	 * @throws DBException
 	 */
 	public void delete(String idseq, String modified_by, Connection conn) throws DBException {
 		PreparedStatement statement = null;
 
 		try {
 
 			String sql = "update data_elements_view set deleted_ind = ?, modified_by = ?,date_modified = ? where de_idseq = ? ";
 
 			int column = 0;
 			statement = conn.prepareStatement(sql);
 			statement.setString(++column, DBConstants.RECORD_DELETED_YES);
 			statement.setString(++column, modified_by);
 			statement.setDate(++column, new java.sql.Date(new java.util.Date().getTime()));
 			statement.setString(++column, idseq);
 
 			int code = statement.executeUpdate();
 			if (code < 0) {
 				throw new Exception("Unable to delete the DE");
 			} else {
 				if (logger.isDebugEnabled()) {
 					logger.debug("Deleted DE");
 				}
 			}
 		} catch (Exception e) {
 			logger.error("Error deleting Data Element " + idseq + e);
 			errorList.add(DeErrorCodes.API_DE_502);
 			throw new DBException(errorList);
 		} finally {
 			DBHelper.close(statement);
 		}
 
 	}
 
 	/**
 	 * Returns the version of a DE based on preferred name and context
 	 * 
 	 * @param preferred_name
 	 * @param conte_IDSEQ
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public double getDeVersion(String preferred_name, String conte_IDSEQ, Connection conn) throws DBException {
 		StringBuffer sql = new StringBuffer();
 		sql.append("select max(version) from data_elements where");
 		sql.append(" cde_id = ( ");
 		sql.append("select distinct(cde_id) from data_elements ");
 		sql.append("where preferred_name = '").append(preferred_name).append("' and conte_idseq = '").append(conte_IDSEQ).append("' )");
 		double version = this.getVersion(sql.toString(), conn);
 		return version;
 	}
 
 	/**
 	 * Returns the version of a DE based on deIDSEQ
 	 * 
 	 * @param deIDSEQ
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public double getDeVersionByIdseq(String deIDSEQ, Connection conn) throws DBException {
 
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
		long version = 0;
 		try {
 			String sql = "select version from data_elements_view where de_idseq = ?";
 			stmt = conn.prepareStatement(sql.toString());
 			stmt.setString(1, deIDSEQ);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
				version = rs.getLong(1);
 			}
 		} catch (SQLException e) {
 			logger.error(DBException.DEFAULT_ERROR_MSG + " in getDeVersionByIdseq() method in Data_Elements_Mgr " + e);
 			errorList.add(DeErrorCodes.API_DE_000);
 			throw new DBException(errorList);
 		} finally {
 			DBHelper.close(rs, stmt);
 		}
 		return version;
 
 	}
 
 	/**
 	 * Returns asl_name(work-flow status) of DE based on the deIDSEQ
 	 * 
 	 * @param deIDSEQ
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public String getDeAslNameByIdseq(String deIDSEQ, Connection conn) throws DBException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		String aslName = null;
 		try {
 			String sql = "select asl_name from data_elements_view where de_idseq = ?";
 			stmt = conn.prepareStatement(sql);
 			stmt.setString(1, deIDSEQ);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				aslName = rs.getString(1);
 			}
 		} catch (SQLException e) {
 			logger.error(DBException.DEFAULT_ERROR_MSG + "in getDeAslNameByIdseq() of Data_Element_Manager class  "	+ e);
 			errorList.add(DeErrorCodes.API_DE_000);
 			throw new DBException(errorList);
 		} finally {
 			DBHelper.close(rs, stmt);
 		}
 		return aslName;
 
 	}
 
 	/**
 	 * Returns DE based on the de_IDSEQ
 	 * 
 	 * @param de_IDSEQ
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public DeVO getDe(String de_IDSEQ, Connection conn) throws DBException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		DeVO deVO = null;
 		try {
 			String sql = "select * from data_elements_view where de_idseq = ?";
 			stmt = conn.prepareStatement(sql);
 			stmt.setString(1, de_IDSEQ);
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				deVO = new DeVO();
 				deVO.setDe_IDSEQ(rs.getString("DE_IDSEQ"));
 				deVO.setVersion(rs.getDouble("VERSION"));
 				deVO.setConte_IDSEQ(rs.getString("CONTE_IDSEQ"));
 				deVO.setPrefferred_name(rs.getString("PREFERRED_NAME"));
 				deVO.setVd_IDSEQ(rs.getString("VD_IDSEQ"));
 				deVO.setDec_IDSEQ(rs.getString("DEC_IDSEQ"));
 				deVO.setPrefferred_def(rs.getString("PREFERRED_DEFINITION"));
 				deVO.setAsl_name(rs.getString("ASL_NAME"));
 				deVO.setLong_name(rs.getString("LONG_NAME"));
 				deVO.setLastest_version_ind(rs.getString("LATEST_VERSION_IND"));
 				deVO.setDeleted_ind(rs.getString("DELETED_IND"));
 				deVO.setDate_created(rs.getTimestamp("DATE_CREATED"));
 				deVO.setBegin_date(rs.getTimestamp("BEGIN_DATE"));
 				deVO.setCreated_by(rs.getString("CREATED_BY"));
 				deVO.setEnd_date(rs.getTimestamp("END_DATE"));
 				deVO.setDate_modified(rs.getTimestamp("DATE_MODIFIED"));
 				deVO.setModified_by(rs.getString("MODIFIED_BY"));
 				deVO.setChange_note(rs.getString("CHANGE_NOTE"));
 			}
 		} catch (SQLException e) {
 			logger.error(DBException.DEFAULT_ERROR_MSG + " in getDe() method of Data_Elements_Mgr class " + e);
 			throw new DBException("API_DE_000");
 		} finally {
 			try {
 				DBHelper.close(rs, stmt);
 			} catch (Exception e) {
 			}
 		}
 		return deVO;
 	}
 
 	/**
 	 * This method updates so that all other versions have the indicator set to
 	 * 'No' if latest_version_indicator = 'yes'
 	 * 
 	 * @param de_IDSEQ
 	 * @param conn
 	 * @throws DBException
 	 */
 	public void setDeLatestVersionIndicator(String de_IDSEQ, Connection conn) throws DBException {
 
 		StringBuffer sql = new StringBuffer();
 		sql.append("update data_elements_view set latest_version_ind = '").append(DBConstants.RECORD_DELETED_NO).append("'");
 		sql.append(" where de_idseq <> '").append(de_IDSEQ).append("' and cde_id = ( ");
 		sql.append("select cde_id  from data_elements_view ");
 		sql.append("where de_idseq = '").append(de_IDSEQ).append("' )");
 		this.setAcLatesVersionIndicator(sql.toString(), conn);
 	}
 
 	/**
 	 * This method returns the sourceIDSEQ
 	 * 
 	 * @param preferred_name
 	 * @param conte_IDSEQ
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public String getSourceIdseq(String preferred_name, String conte_IDSEQ, Connection conn) throws DBException {
 		String sourceIDSEQ = null;
 		StringBuffer sql = new StringBuffer();
 		double version = this.getDeVersion(preferred_name, conte_IDSEQ, conn);
 		if (version > 0) {
 			sql.append("select de_idseq from data_elements");
 			sql.append("where (version = '").append(version).append("') and (cde_id = (");
 			sql.append("select distinct(cde_id) from data_elements ");
 			sql.append("where (preferred_name = '").append(preferred_name)
 					.append("') and (conte_idseq = '").append(conte_IDSEQ)
 					.append("') )");
 		}
 		return sourceIDSEQ;
 	}
 
 }
