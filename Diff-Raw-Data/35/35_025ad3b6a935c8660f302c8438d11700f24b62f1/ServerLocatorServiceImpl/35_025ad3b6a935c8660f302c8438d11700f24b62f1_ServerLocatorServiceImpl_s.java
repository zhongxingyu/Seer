 package edu.cmu.eventtracker.geoserver;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.caucho.hessian.server.HessianServlet;
 
 public class ServerLocatorServiceImpl extends HessianServlet
 		implements
 			ServerLocatorService {
 	public final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
 	public final String protocol = "jdbc:derby:";
 	public final PreparedStatement locationsStatement;
 	public final PreparedStatement usersStatement;
	public final PreparedStatement userCountStatement;

 
 	private final Connection shardsConnection;
 
 	public ServerLocatorServiceImpl() {
 		try {
 			shardsConnection = DriverManager.getConnection(protocol
 					+ "shardsDB;create=true", null);
 			locationsStatement = shardsConnection
 					.prepareStatement("Select host, min((maxlat-minlat) * (maxlng - minlng)) from shards where minlat <= ? and ? < maxlat and minlng <= ? and ? < maxlng");
 			
 			//the table usershard not shards right? (or same above?)
 			usersStatement = shardsConnection
					.prepareStatement("Select hostname from usershard where nodeid= ?");
			userCountStatement = shardsConnection
					.prepareStatement("Select count(nodeid) as counter");
 			
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	@Override
 	public String getUserShard(String username) {
 		ResultSet rs = null;
		int nodeCounter = 0;
 		try{
			rs = userCountStatement.getResultSet();
			while(rs.next()){
				nodeCounter = rs.getInt("counter");
			}
 			
			usersStatement.setInt(1, username.hashCode() % nodeCounter); //username.hashCode % num of nodes?
 			rs = usersStatement.getResultSet();
 			
			while(rs.next()){
 				return rs.getString("hostname");
 			}
 			return null;
 			
 		}catch (SQLException e) {
 			throw new IllegalStateException(e);
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new IllegalStateException(e);
 				}
 			}
 		}
 	}
 
 	@Override
 	public String getLocationShard(double lat, double lng) {
 		ResultSet rs = null;
 		try {
 			locationsStatement.setDouble(1, lat);
 			locationsStatement.setDouble(2, lat);
 			locationsStatement.setDouble(3, lng);
 			locationsStatement.setDouble(4, lng);
 			rs = locationsStatement.getResultSet();
 			while (rs.next()) {
 				return rs.getString("host");
 			}
 			return null;
 		} catch (SQLException e) {
 			throw new IllegalStateException(e);
 		} finally {
 			if (rs != null) {
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new IllegalStateException(e);
 				}
 			}
 		}
 	}
 
 }
