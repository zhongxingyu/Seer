 package me.Jaryl.FoundBoxx.SQLwrapper;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import me.Jaryl.FoundBoxx.FoundBoxx;
 
 public class SQLwrapper {
 	private FoundBoxx plugin; // NOTE TO SELF: CHANGE THIS ON NEW PROJECTS
 	public SQLwrapper(FoundBoxx pl) // NOTE TO SELF: CHANGE THIS ON NEW PROJECTS
 	{
 		plugin = pl;
 	}
 	
 	public Connection conn;
 	public List<String> dataQueries = new ArrayList<String>();
 	
 	public boolean isQueuing = false;
 	
 	public boolean Connected()
 	{
 		if (conn != null)
 		{
 			try {
 				return !conn.isClosed();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return false;
 	}
 	
 	// DATAQUERIES
 	public void executeQueue()
 	{
 		if (Connected())
 		{
 			isQueuing = true;
 			
 			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable()
 			{
 				public void run()
 				{
 					if (isQueuing == false || !dataQueries.isEmpty())
 					{
						for (int i = 0; i < (dataQueries.size() > plugin.sqlData ? plugin.sqlData : dataQueries.size()); i++)
 						{
							String query = dataQueries.get(0);
 							if (!query.isEmpty())
 							{
 								try {
 									dataQuery(query);
 								} catch (SQLException e) {
 									// TODO Auto-generated catch block
 									e.printStackTrace();
 								}
 							}
							dataQueries.remove(0);
 						}

 						executeQueue();
 					}
 					else
 					{
 						isQueuing = false;
 					}
 				}
			}, 20);
 		}
 	}
 	public void queueData(String query) {
 		if (Connected())
 		{
 			dataQueries.add(query);
 			
 			if (!isQueuing)
 			{
 				executeQueue();
 			}
 		}
 	}
 	public void dataQuery(String query) throws SQLException {
 		if (Connected())
 		{
 			PreparedStatement QueryStatement = conn.prepareStatement(query);
 			QueryStatement.executeUpdate();
 		}
 	}
 	
 	// QUERIES
 	public ResultSet Query(String query) throws SQLException {
 		if (Connected())
 		{
 			PreparedStatement QueryStatement = conn.prepareStatement(query);
 			ResultSet rs = QueryStatement.executeQuery();
 			return rs;
 		}
 		
 		return null;
 	}
 	
 	public void Stop() throws SQLException
 	{
 		if (Connected())
 		{
 			System.out.println("[" + plugin.getDescription().getFullName() + "] Attempting to unload " + (plugin.useSQL.equalsIgnoreCase("h2") ? "H2" : "SQL") + ".");
 			
 			isQueuing = false;
 			if (!dataQueries.isEmpty())
 			{
 				System.out.println("[" + plugin.getDescription().getFullName() + "] There are still some queries in the queue. Attempting to finish.");
 				for (int i = 0; i < dataQueries.size(); i++)
 				{
 					String query = dataQueries.get(i);
 					if (!query.isEmpty())
 					{
 						try {
 							dataQuery(query);
 						} catch (SQLException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 							}
 					}
 					dataQueries.remove(i);
 				}
 				
 				executeQueue();
 			}
 			
 			conn.close();
 			conn = null;
 			
 			System.out.println("[" + plugin.getDescription().getFullName() + "] " + (plugin.useSQL.equalsIgnoreCase("h2") ? "H2" : "SQL") + " unloaded.");
 		}
 	}
 }
