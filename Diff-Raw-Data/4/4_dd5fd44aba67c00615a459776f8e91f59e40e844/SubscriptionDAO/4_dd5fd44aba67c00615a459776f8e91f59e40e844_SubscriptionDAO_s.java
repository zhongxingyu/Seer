 package dbPackage.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.UUID;
 
 import dbPackage.beans.Dealer;
 import dbPackage.beans.SubscriptionObj;
 
 public class SubscriptionDAO {
 
 	/**
 	 * Connects to the given database and retrieves all the subscriptions.
 	 * 
 	 * @param conn The database to connect
 	 * @return a list of all the subscriptions
 	 */
 	public static synchronized ArrayList<SubscriptionObj> getAppSubs (Connection conn){
 		String query = "";
 		PreparedStatement stmt = null;
 
 		try{
 			query = "SELECT a.subID, b.name FROM subscription_table a JOIN metric_table b ON a.subID = b.subID";
 			stmt = conn.prepareStatement(query);
 			
 			ResultSet rs = stmt.executeQuery();
 			ArrayList<SubscriptionObj> subsList = new ArrayList<SubscriptionObj>();
 			while(rs.next())
 				subsList.add(new SubscriptionObj(rs.getString("subID"),rs.getString("name"), null,null));
 			return subsList;
 		}
 		catch(SQLException e){
 			e.printStackTrace();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		finally{
 			if (stmt != null)
 				try{
 					stmt.close();
 				}catch (SQLException e){
 					e.printStackTrace();
 				} 
 		}
 		return null;
 	}
 	
 	/**
 	 * Connects to the given database and retrieves the metadata of the given subscription.
 	 * 
 	 * @param conn The database to connect to
 	 * @param subID The subscription id
 	 * @return an object with all the subscription's metadata
 	 */
 	public static synchronized SubscriptionObj getSubMetadata (Connection conn, String subID){
 		String query = "";
 		PreparedStatement stmt = null;
 
 		try{
 			query = "SELECT * FROM subscription_table a JOIN metric_table b ON a.subID = b.subID WHERE a.subID = '"+subID+"'";
 			stmt = conn.prepareStatement(query);
 			
 			ResultSet rs = stmt.executeQuery();
 			rs.next();
 			return new SubscriptionObj(rs.getString("subID"),rs.getString("name"),rs.getString("func"),rs.getString("period"),
 					rs.getString("originMetric"),rs.getString("metricID"),rs.getString("type"),rs.getString("units"),rs.getString("mgroup"));
 		}
 		catch(SQLException e){
 			e.printStackTrace();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 		finally{
 			if (stmt != null)
 				try{
 					stmt.close();
 				}catch (SQLException e){
 					e.printStackTrace();
 				} 
 		}
 		return null;
 	}
 	
 	/**
 	 * Calls the monitoring server in order to create a new subscription.
 	 * 
 	 * @param ip The monitoring server's ip
 	 * @param port The monitoring server's port
 	 * @param json a json with the new subscription's characteristics
 	 * @return true if the subscription was created, otherwise false
 	 */
 	public static boolean addSubscription(String ip, String port, String json){  
 		boolean success = SubscriptionDAO.msConnect(ip, port, "SUBSCRIPTION.ADD", json);
 		System.out.println("SUBSCRIPTION.ADD -> " + (success? "success" : "fail"));
 		return success;
 	}
 	
 	/**
 	 * Calls the monitoring server in order to delete a subscription.
 	 * 
 	 * @param ip The monitoring server's ip
 	 * @param port The monitoring server's port
 	 * @param json a json with the subscription's id
 	 * @return true if the subscription was deleted, otherwise false
 	 */
 	public static boolean removeSubscription(String ip, String port, String json){   
		boolean success = SubscriptionDAO.msConnect(ip, port, "SUBSCRIPTION.REMOVE", json);
		System.out.println("SUBSCRIPTION.REMOVE -> " + (success? "success" : "fail"));
 		return success;
 	}
 	
 	/**
 	 * Calls the monitoring server in order to add an agent to a subscription.
 	 * 
 	 * @param ip The monitoring server's ip
 	 * @param port The monitoring server's port
 	 * @param json a json with the subscription's id and agent's id
 	 * @return true if the agent was added to the subscription agents, otherwise false
 	 */
 	public static boolean addAgent(String ip, String port, String json){   
 		boolean success = SubscriptionDAO.msConnect(ip, port, "SUBSCRIPTION.ADDAGENT", json);
 		System.out.println("SUBSCRIPTION.ADDAGENT -> " + (success? "success" : "fail"));
 		return success;
 	}
 	
 	/**
 	 * Calls the monitoring server in order to remove an agent from a subscription.
 	 * 
 	 * @param ip The monitoring server's ip
 	 * @param port The monitoring server's port
 	 * @param json a json with the subscription's id and agent's id
 	 * @return true if the agent was removed from the subscription agents, otherwise false
 	 */
 	public static boolean removeAgent(String ip, String port, String json){   
 		boolean success = SubscriptionDAO.msConnect(ip, port, "SUBSCRIPTION.REMOVEAGENT", json);
 		System.out.println("SUBSCRIPTION.REMOVEAGENT -> " + (success? "success" : "fail"));
 		return success;
 	}
 	
 	/**
 	 * Connects to the monitoring server on the specified ip and port and requests the
 	 * given type of action with the given data from it.
 	 * 
 	 * @param ip The monitoring server's ip
 	 * @param port The monitoring server's port
 	 * @param type The type of action to request from the monitoring server
 	 * @param json_request The request body 
 	 * @return true if the action was successful, otherwise false
 	 */
 	private static boolean msConnect(String ip, String port, String type, String json_request){
 		Dealer dealer = new Dealer(ip,port,"tcp",16,UUID.randomUUID().toString().replace("-", ""));
 		int attempts = 0; 
 		boolean connected = false;
     	String[] response = null;
     	try {			
 			while(((attempts++)<3) && (!connected)){
 	    		dealer.send("",type,json_request);
 	            response = dealer.receive(12000L);
 	            if (response != null){
 	            	for(String s: response) System.out.println("ROUTER RESPONSE: " + s);
 	            	connected = (response[1].contains("OK")) ? true : false;
 	            	break;
 	            }
 				else	
 					Thread.sleep(3000);
 	    	}
 			dealer.close();
 		} 
     	catch (InterruptedException e){
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		return connected;
 	}
 }
