 package gov.nih.nci.cadsr.persist.common;
 
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.persist.de.DeErrorCodes;
 import gov.nih.nci.cadsr.persist.exception.DBException;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 @SuppressWarnings("unchecked")
 public abstract class DBManager {
 
 	private Logger logger = Logger.getLogger(this.getClass());
 	protected ArrayList errorList = new ArrayList();
 	
 	/**
 	 * This method returns primary key
 	 * 
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	protected synchronized String generatePrimaryKey(Connection conn) throws DBException {
 		String key = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 
 		try {
 			stmt = conn.prepareStatement("select sbr.admincomponent_crud.cmr_guid from DUAL");
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				key = rs.getString(1);
 			}
 		} catch (Exception e) {
 			logger.error("Unable to generate primary key " + e);
 			errorList.add(DeErrorCodes.API_DE_400);
 			throw new DBException(errorList);
 		} finally {
 			rs = SQLHelper.closeResultSet(rs);
 			stmt = SQLHelper.closePreparedStatement(stmt);
 		}
 		return key;
 
 	}
 
 	/**
 	 * Gets the version of a component of Admin_Components
 	 * 
 	 * @param sql
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public double getVersion(String sql, Connection conn) throws DBException {
 		double version = 0;
 		Statement statement = null;
 		ResultSet rs = null;
 		try {
 			statement = conn.createStatement();
 			rs = statement.executeQuery(sql);
 			while (rs.next()) {
 				version = rs.getDouble(1);
 			}
 		} catch (Exception e) {
 			logger.error("Error in getVersion() method of DBMaanger class" + e);
 			this.errorList.add(DeErrorCodes.API_DE_000);
 			throw new DBException(this.errorList);
 		} finally {
 			rs = SQLHelper.closeResultSet(rs);
 			statement = SQLHelper.closeStatement(statement);
 		}
 		return version;
 	}
 
 	/**
 	 * Checks to see that AC with IDSEQ already exists if exists, returns true
 	 * else returns false
 	 * 
 	 * @param sql
 	 * @param conn
 	 * @return 
 	 * @throws DBException
 	 */
 	public boolean isExists(String sql, Connection conn) throws DBException {
 
 		Statement statement = null;
 		ResultSet rs = null;
 		boolean isExists = false;
 		try {
 			statement = conn.createStatement();
 			rs = statement.executeQuery(sql);
 			if (rs.next() && rs.getInt(1) > 0)
 				isExists = true;
 		} catch (Exception e) {
 			logger.error(DBException.DEFAULT_ERROR_MSG + "in isExists() method", e);
 			errorList.add(DeErrorCodes.API_DE_000);
 			throw new DBException(errorList);
 		} finally {
 			rs = SQLHelper.closeResultSet(rs);
 			statement = SQLHelper.closeStatement(statement);
 		}
 
 		return isExists;
 
 	}
 
 	/**
 	 * 
 	 * @param sql
 	 * @param conn
 	 * @throws DBException
 	 */
 
 	public void setAcLatesVersionIndicator(String sql, Connection conn)	throws DBException {
 		Statement statement = null;
 
 		try {
 			statement = conn.createStatement();
			statement.executeQuery(sql);
 			if (logger.isDebugEnabled()) {
 				logger.debug("updated so that all other versions have the indicator set to 'No'");
 			}
 		} catch (Exception e) {
 			logger.error(DBException.DEFAULT_ERROR_MSG + " in setAcLatesVersionIndicator() method", e);
 			errorList.add(DeErrorCodes.API_DE_503);
 			throw new DBException(errorList);
 		} finally {
 			statement = SQLHelper.closeStatement(statement);
 		}
 
 	}
 
 	/**
 	 * This method returns the sourceIDSEQ
 	 * 
 	 * @param sql
 	 * @param conn
 	 * @return
 	 * @throws DBException
 	 */
 	public String getSourceIdseq(String sql, Connection conn) throws DBException {
 		String sourceIDSEQ = null;
 		Statement statement = null;
 		ResultSet rs = null;
 		try {
 			statement = conn.createStatement();
 			rs = statement.executeQuery(sql);
 			while (rs.next()) {
 				sourceIDSEQ = rs.getString(1);
 			}
 		} catch (Exception e) {
 			logger.error("Error in getSourceIdseq method of DBMaanger class " + e);
 			this.errorList.add(DeErrorCodes.API_DE_000);
 			throw new DBException(this.errorList);
 		} finally {
 			rs = SQLHelper.closeResultSet(rs);
 			statement = SQLHelper.closeStatement(statement);
 		}
 		return sourceIDSEQ;
 	}
 
 }
