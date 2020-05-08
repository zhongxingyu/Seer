 package com.bxbservers.BungeeListener;

 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.ProxyServer;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 import net.md_5.bungee.api.plugin.Listener;
 import net.md_5.bungee.api.plugin.Plugin;
 import com.vexsoftware.votifier.model.Vote;
 import com.vexsoftware.votifier.model.VotifierEvent;
 
 
 public class BungeeListener extends Plugin implements Listener
 {
 
 	private static Logger log = Logger.getLogger("Listener");
 	public String host;
 	public String user;
 	public String pass;
 	public String database;
 	public String url;
 	public String port;
 
 	public void onEnable(){
         this.getProxy().getPluginManager().registerListener(this, this);
         bungeeListener();
         
 		@SuppressWarnings("unused")
 		Connection connection = null;
 		
 		try
 		{
 			connection = (Connection)DriverManager.getConnection(url, user, pass);
 		}
 		catch (SQLException sqlException1) {
 			sqlException1.printStackTrace();
 		}
 		
         String commandName = "testvote";
         
     	ProxyServer.getInstance().getPluginManager().registerCommand(this, new Command(commandName, "testVote")
     	{
 
 			@Override
     		public void execute(CommandSender sender, String[] args) 
     		{
     			ProxiedPlayer p = (ProxiedPlayer)sender;
     			
     			 String voteUser = p.getDisplayName();
     			 String userID = null;
     			 int totalVotes = 0;
     			 int remainingVotes = 0;
     			 
     			Connection connection = null;
     			Statement statement = null;
     			try
     				{
     					connection = (Connection)DriverManager.getConnection(url, user, pass);
     				}
     				catch (SQLException sqlException1) {
     					sqlException1.printStackTrace();
     				}
     				
     				try
     				{
     			      statement = (Statement)connection.createStatement();
     			    }
     			    catch (SQLException localSQLException2) {
     			      localSQLException2.printStackTrace();
     			    }
     				
     			    try
     			    {
     			    	
     			      if (!statement.execute("SELECT user_id FROM xf_user_field_value WHERE field_value = '"+ voteUser +"'"))
     			      {
     			    	  p.sendMessage("You must be registered to receive vote tokens.");
     			    	  return;
     			      }
     			      
     			    	  statement.execute("SELECT user_id FROM xf_user_field_value WHERE field_value = '"+ voteUser +"'");
     			    	  ResultSet result1 = statement.getResultSet();
     			    	  result1.next();
     			    	  userID = result1.getString(1);
     			    	  log.info("Found user ID of " + userID);
     			      
     			    	  statement.execute("SELECT field_value FROM xf_user_field_value WHERE user_id = "+ userID + " AND field_id = 'tokens'");
     				      ResultSet result2 = statement.getResultSet();
     				      result2.next();
     				      remainingVotes = result2.getInt(1);
     				      log.info("Found Id of remaining of " + remainingVotes);
     			      
     				      statement.execute("SELECT field_value FROM xf_user_field_value WHERE user_id = "+ userID + " AND field_id = 'totalTokens'");
     				      ResultSet result3 = statement.getResultSet();
     				      result3.next();
     				      totalVotes = result3.getInt(1);
     				      log.info("Found Id of total of " + totalVotes);
     			      }
     			      catch (SQLException sqlException3) {
     			    	  sqlException3.printStackTrace();
     			      }
     			 
     			      totalVotes = totalVotes + 1;
     			      remainingVotes = remainingVotes + 1;
     			    
     			      try
     			      {
     			    	  log.info("Begin Update");
     			    	  log.info(Integer.toString(totalVotes));
     			    	  log.info(Integer.toString(remainingVotes));
     			     	
     			    	  statement.executeUpdate("UPDATE xf_user_field_value SET field_value = " + totalVotes +" WHERE user_id = "+ userID + " AND field_id = 'totalTokens'");
     			    	  statement.executeUpdate("UPDATE xf_user_field_value SET field_value = " + remainingVotes +" WHERE user_id = "+ userID + " AND field_id = 'tokens'");   			     	
     			      }
     			      catch (SQLException sqlException4) 
     			      {
     				      sqlException4.printStackTrace();
     			      }
     			 }
     		}
     		);
     	}
	
 	public void bungeeListener()
 	{
 		Properties settings = new Properties();
 		try
 		{
 			String folder = "plugins/" + this.getDescription().getName();
 			String filename = "/config.ini";
 			
 			File dir = new File(folder);
 			if (!dir.exists())
 				dir.mkdirs();
 			
 			File settingsFile = new File(folder + filename);
 			if (!settingsFile.exists())
 			{
 				settingsFile.createNewFile();
 				
 				settings.load(new FileReader(settingsFile));
 				
 		        settings.setProperty("host", "127.0.0.1");
 		        settings.setProperty("port", "3306");
 		        settings.setProperty("user", "votifier");
 		        settings.setProperty("pass", "votifier");
 		        settings.setProperty("database", "votifier");
 		        
 		        settings.store(new FileWriter(settingsFile), "Listener Default Configuration");
 			}
 			else
 			{
 				settings.load(new FileReader(settingsFile));
 			}
 			
 			this.host = settings.getProperty("host", "127.0.0.1");
 			this.port = settings.getProperty("port", "3306");
 			this.user = settings.getProperty("user", "votifier");
 			this.pass = settings.getProperty("pass", "votifier");
 			this.database = settings.getProperty("database", "votifier");
 			
 			this.url = ("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
 			
 		}
 		catch (Exception localException)
 		{
 		      log.log(Level.WARNING, "Unable to load MySQL\tLogger Listener configuration file, please make sure it exists!  Using default values");
 
 		      this.host = "127.0.0.1";
 		      this.user = "votifier";
 		      this.pass = "votifier";
 		      this.database = "votifier";
 		      this.port = "3306";
 		      this.url = ("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
 		}
 		
 
 		
 	}
 	
 	@Subscribe
     public void onPlayerVote(VotifierEvent e)
     {
         Vote v = e.getVote();
         
 		 String voteUser = v.getUsername();
 		 ProxiedPlayer p = ProxyServer.getInstance().getPlayer(voteUser);
 		 String userID = null;
 		 int totalVotes = 0;
 		 int remainingVotes = 0;
 		 
 		Connection connection = null;
 		Statement statement = null;
 		try
 			{
 				connection = (Connection)DriverManager.getConnection(this.url, this.user, this.pass);
 			}
 			catch (SQLException sqlException1) {
 				sqlException1.printStackTrace();
 			}
 			
 			try
 			{
 		      statement = (Statement)connection.createStatement();
 		    }
 		    catch (SQLException localSQLException2) {
 		      localSQLException2.printStackTrace();
 		    }
 			
 		    try
 		    {
 		    	
 			  if (!statement.execute("SELECT user_id FROM xf_user_field_value WHERE field_value = '"+ voteUser +"'"))
 			  {
 		    	  p.sendMessage("You must be registered to receive vote tokens.");
 				  return;
 			  }
 			  
 		      statement.execute("SELECT user_id FROM xf_user_field_value WHERE field_value = '"+ voteUser +"'");
 		      userID = statement.getResultSet().getString(1);
 		      log.info("Found user ID of " + userID);
 		      
 		      statement.execute("SELECT field_value FROM xf_user_field_value WHERE user_id = "+ userID + " AND field_id = 'tokens'");
 		      remainingVotes = statement.getResultSet().getInt(1);
 		      log.info("Found Id of remaining of " + remainingVotes);
 		      
 		      statement.execute("SELECT field_value FROM xf_user_field_value WHERE user_id = "+ userID + " AND field_id = 'totalTokens'");
 		      totalVotes = statement.getResultSet().getInt(1);
 		      log.info("Found Id of total of " + totalVotes);
 		    }
 		    catch (SQLException sqlException3) {
 		      sqlException3.printStackTrace();
 		    }
 		 
 		    totalVotes = totalVotes + 1;
 		    remainingVotes = remainingVotes + 1;
 		    
 		    try
 		    {
 		     	statement.execute("UPDATE xf_user_field_value SET field_value = " + totalVotes +" WHERE user_id = "+ userID + " AND field_id = 'totalTokens'");
 		     	statement.execute("UPDATE xf_user_field_value SET field_value = " + remainingVotes +" WHERE user_id = '"+ userID + "' AND field_id = 'tokens'");
 		     	
 		    }
 		    catch (SQLException sqlException4) {
 			      sqlException4.printStackTrace();
 			}
     	}
 		 
 	}
