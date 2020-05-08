 package dk.aersoe.jobfinder.crud.helpers;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException; // Not used for now! but will be when verbose logging is implemented
 import javax.sql.DataSource;
 
 import dk.aersoe.jobfinder.model.JobEntry;
 
 /**
  * Database helper that enables read functionality
  * 
  * @author morten@aersoe.dk
  *
  */
 public class DataReader {
 	
 	
 	/**
 	 * Gets a JobEntry from the database based on the id of the JobEntry
 	 * @param id the id of the requested JobEntry
 	 * @return a JobEntry object or null if the id is not found in the table
 	 */
 	public JobEntry getEntry(int id) throws Throwable{
 		JobEntry result = null;
 		String dsName = "JobFinderResource";
 		DataSource ds = null;
 		Connection con = null;
 		try {
 			ds = (javax.sql.DataSource)new InitialContext().lookup(dsName);
 			con = ds.getConnection();
 			PreparedStatement pstmt = con.prepareStatement("select * from JobEntries where id = ? order by id");
 			pstmt.setInt(1, id);
 			ResultSet rs = pstmt.executeQuery();
 			if (rs != null){
 				Set<JobEntry> extractedData = extractData(rs);
 				Iterator<JobEntry> it = extractedData.iterator();
 				if (it.hasNext()){
 					result = it.next();
 				}
 			}
 		} 
 		catch (Exception e) {
 			// TODO Could be handled a little nicer with some serious logging - Implement in due cause
 			throw e;
 		}
 		finally{
 			try {
 				con.close();
 			} catch (SQLException e) {
 				// TODO Add error handling - this should never happen though! (famous last words - I know!)
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Gets all the job entries in the database
 	 * @return a Set containing all the job entries in the database or null if nothing is found
 	 */
 	public Set<JobEntry> getEntries(){
 		Set<JobEntry> result = null;
 		String dsName = "JobFinderResource";
 		DataSource ds = null;
 		Connection con = null;
 		try {
 			ds = (javax.sql.DataSource)new InitialContext().lookup(dsName);
 			con = ds.getConnection();
 			PreparedStatement pstmt = con.prepareStatement("select * from JobEntries order by id asc");
 			ResultSet rs = pstmt.executeQuery();
 			if (rs != null){ // unsure if it may happen that the result set is null
 				result = extractData(rs);
 			}
 		}
 		catch(Exception e){
 			// TODO add error handling
 		}
 		finally{
 			try {
 				con.close();
 			} catch (SQLException e) {
 				// TODO Add error handling - this should never happen though! (famous last words - I know!)
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Gets all the job entries in the database that meet the search criterias
 	 * @param fieldName the name of the requested column
 	 * @param value the value to be searched for. This will be based on a SQL LIKE query
 	 * @return a Set containing all the job entries in the database or null if nothing is found
 	 */
 	public Set<JobEntry> getEntriesFromString(String fieldName, String value){
 		Set<JobEntry> result = null;
 		String dsName = "JobFinderResource";
 		DataSource ds = null;
 		Connection con = null;
 		try {
 			ds = (javax.sql.DataSource)new InitialContext().lookup(dsName);
 			con = ds.getConnection();
 			PreparedStatement pstmt = con.prepareStatement("select * from JobEntries where ? like ?");
 			pstmt.setString(1, fieldName);
 			pstmt.setString(2, value);
 			ResultSet rs = pstmt.executeQuery();
 			if (rs != null){ // unsure if it may happen that the result set is null
 				result = extractData(rs);
 			}
 		}
 		catch(Exception e){
 			// TODO add error handling
 		}
 		finally{
 			try {
 				con.close();
 			} catch (SQLException e) {
 				// TODO Add error handling - this should never happen though! (famous last words - I know!)
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Gets all the job entries in the database that meet the specified date
 	 * @param fieldName the name of the requested column
 	 * @param date the specific date to be searched for
 	 * @return a Set containing all the job entries in the database or null if nothing is found
 	 */
 	public Set<JobEntry> getEntriesFromDate(String fieldName, Date date){
 		Set<JobEntry> result = null;
 		try{
 			// TODO Add search logic
 			result = new HashSet<JobEntry>();
 		}
 		catch(Exception e){
 			// TODO Add error handling
 		}
 		return result;
 	}
 
 	/**
 	 * Gets all the job entries in the database that meet the search criterias based on a date range
 	 * @param fieldName the name of the requested column
 	 * @param startDate the Date that the search range starts with
 	 * @param endDate the Date that the search range ends with
 	 * @return a Set containing all the job entries in the database or null if nothing is found
 	 */
 	public Set<JobEntry> getEntriesFromDateRange(String fieldName, Date startDate, Date endDate){
 		Set<JobEntry> result = null;
 		try{
 			ResultSet rs = null;
 			if (rs != null){
 				// TODO Add search logic
 				result = new HashSet<JobEntry>();
 			}
 		}
 		catch(Exception e){
 			// TODO Add error handling
 		}
 		return result;
 	}
 	
 	/**
 	 * Method to extract a Set containing the rows in the ResultSet from the query
 	 * @param rs ResultSet containing the query result
 	 * @return Set containing JobEntry objects representing the result of th equery 
 	 * @throws SQLException
 	 */
 	private Set<JobEntry> extractData(ResultSet rs) throws SQLException{
 		Set<JobEntry> result = new HashSet<JobEntry>();
 		while (rs.next()){
 			JobEntry entry = new JobEntry();
 			entry.setId(rs.getInt("id"));
 			entry.setCreationDate(rs.getDate("creation_date"));
 			entry.setModifyDate(rs.getDate("modify_date"));
 			entry.setDeletedDate(rs.getDate("deleted_date"));
 			entry.setForeignDate(rs.getDate("foreign_date"));
 			entry.setStatus(rs.getInt("status"));
 			entry.setDeadline(rs.getDate("deadline"));
 			entry.setCategory(rs.getString("category"));
 			entry.setTitle(rs.getString("title"));
 			entry.setCompany(rs.getString("company"));
 			entry.setDescription(rs.getString("description"));
 			entry.setUrl(rs.getString("url"));
 			entry.setSource(rs.getString("source"));
 			result.add(entry);
 		}
 		return result;
 	}
 }
