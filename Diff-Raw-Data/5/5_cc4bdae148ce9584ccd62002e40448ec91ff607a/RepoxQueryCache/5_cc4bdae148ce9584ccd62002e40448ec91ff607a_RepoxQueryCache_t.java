 /**
  * 
  */
 package gr.ntua.ivml.mint.concurrent;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Iterator;
 
 /**
 * Efficient mechanism for caching and tranfering data from Repox
  * 
  * @author Georgios Markakis (gwarkx@hotmail.com)
  * @since 16 Jan 2013
  */
 public class RepoxQueryCache implements Iterator<ResultSet>{
 
 	private static final int STEPSIZE = 10000; 
 	private int totalNoOfItems;
 	private int currentpointer;
 	private Connection repoxConnection;
 	private String repoxID;
 	
 	
 	/**
 	 * Privates constructor, instantiate via factory method
 	 */
 	private RepoxQueryCache(){
 		
 	}
 	
 	
 	/**
 	 * Factory method
 	 * 
 	 * @param repoxConnection
 	 * @param repoxID
 	 * @return
 	 * @throws SQLException
 	 */
 	public static RepoxQueryCache getInstance(Connection repoxConnection,String repoxID) throws SQLException{
 	
 		PreparedStatement ps = repoxConnection.prepareStatement("select count(nc) from repox_" + repoxID 
 				+ "_record where deleted = 0");
 		ps.setFetchSize(100);
 		ps.setFetchDirection(ResultSet.FETCH_FORWARD);
 		
 		ResultSet rs = ps.executeQuery();
 		
 		RepoxQueryCache instance = new RepoxQueryCache();
 		instance.repoxID = repoxID;
 		instance.repoxConnection = repoxConnection;
 		
 		while( rs.next()) {
 			Integer count = new Integer(rs.getString("count"));
			instance.totalNoOfItems = count +1;
 			instance.currentpointer = 1;
 		}
 		return instance;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.util.Iterator#hasNext()
 	 */
 	@Override
 	public boolean hasNext() {
 		if(this.currentpointer == totalNoOfItems){
 			return false;
 		}
 		else{
 			return true;
 		}
 			
 		
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.util.Iterator#next()
 	 */
 	@Override
 	public ResultSet next() {
 		PreparedStatement ps;
 		try {
 
 			int from = currentpointer;
 			
 			int to = 0;
 			
 			if((currentpointer + STEPSIZE) < totalNoOfItems){
 				to = currentpointer + STEPSIZE;
 				
 				currentpointer = currentpointer + STEPSIZE;
 			}
 			else{
 				
 				to = totalNoOfItems;
 				currentpointer = totalNoOfItems;
 			}
 			
 			ps = repoxConnection.prepareStatement("select nc, value from repox_" + repoxID 
 					+ "_record where deleted = 0 and (id >=" + from + " AND id < " + to + ");");
 			ps.setFetchSize(100);
 			ps.setFetchDirection(ResultSet.FETCH_FORWARD);
 			ResultSet rs = ps.executeQuery();	
 			return rs;
 
 		} catch (SQLException e) {
 			return null;
 		}
 
 	}
 
 
 	/* (non-Javadoc)
 	 * @see java.util.Iterator#remove()
 	 */
 	@Override
 	public void remove() {
 		throw new UnsupportedOperationException("Operation not implemented");
 	}
 
 
 
 
 
 
 	
 }
