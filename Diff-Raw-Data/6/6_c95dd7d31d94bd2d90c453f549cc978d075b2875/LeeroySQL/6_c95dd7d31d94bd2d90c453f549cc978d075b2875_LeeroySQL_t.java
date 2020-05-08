 package net.loadingchunks.plugins.Leeroy;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.bukkit.Location;
 
 public class LeeroySQL {
 	public Leeroy plugin;
 	public Connection con;
 	public Statement stmt;
 	public boolean success;
 	public long lastCommandGet = 0;
 	public LeeroyHomeCommand[] commCache;
 	
 	public LeeroySQL(Leeroy plugin)
 	{
 		this.plugin = plugin;
 		this.success = true;
 
 		this.plugin.log.info("[LEEROY] Connecting to MySQL Server with " + this.plugin.getConfig().getString("db.addr") + " and user " + this.plugin.getConfig().getString("db.user"));
 
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.con = DriverManager.getConnection(this.plugin.getConfig().getString("db.addr"), this.plugin.getConfig().getString("db.user"), this.plugin.getConfig().getString("db.pass"));
 		} catch ( SQLException e )
 		{
 			e.printStackTrace();
 			this.success = false;
 		} catch (ClassNotFoundException e) { e.printStackTrace(); this.success = false; }
 	}
 
 	public void AddNPC(String id, String name, String type, Location loc, String world)
 	{
 		try {
 			PreparedStatement stat = con.prepareStatement("INSERT INTO `leeroy_npc` (id, name, type, x, y, z, yaw, pitch,world) VALUES (?,?,?,?,?,?,?,?,?)");
 
 			stat.setString(1, id);
 			stat.setString(2, name);
 			stat.setString(3, type);
 			stat.setDouble(4, loc.getX());
 			stat.setDouble(5, loc.getY());
 			stat.setDouble(6, loc.getZ());
 			stat.setFloat(7, loc.getYaw());
 			stat.setFloat(8, loc.getPitch());
 			stat.setString(9, world);
 
 			stat.execute();
 		} catch ( SQLException e ) { e.printStackTrace(); }
  	}
 	
 	public void RemoveNPC(String id)
 	{
 		try {
 			this.plugin.log.info("[LEEROY] Deleting NPC from Database. (ID: " + id + ")");
 			PreparedStatement stat = con.prepareStatement("DELETE FROM `leeroy_npc` WHERE id = ?");
 			
 			stat.setString(1, id);
 			
 			stat.execute();
 		} catch ( SQLException e ) { e.printStackTrace(); }
 	}
 	
 	public void PopNPCs()
 	{
 		try {
 			PreparedStatement stat = con.prepareStatement("SELECT * FROM `leeroy_npc`");
 			ResultSet result = stat.executeQuery();
 		
 			if(!result.last())
 				this.plugin.log.info("[LEEROY] No NPCs found.");
 			else {
 				result.first();
 				do
 				{
 					this.plugin.npcs.spawn(result.getString("type"), result.getString("name"), new Location(this.plugin.getServer().getWorld(result.getString("world")), result.getDouble("x"), result.getDouble("y"), result.getDouble("z"), result.getFloat("yaw"), result.getFloat("pitch")), result.getString("message1"), result.getString("message2"), result.getString("message3"), result.getString("message4"), false, result.getString("world"), result.getString("id"));
 				} while(result.next());
 			}
 		} catch ( SQLException e ) { e.printStackTrace(); }
 	}
 	
 	public LeeroyHomeCommand GetCommand(String command)
 	{
 		try {
 			PreparedStatement stat = con.prepareStatement("SELECT * FROM `leeroy_commands` WHERE command = ?");
 			stat.setString(1, command);
 			ResultSet result = stat.executeQuery();
 			
 			if(!result.last())
 				return null;
 			else {
 				result.first();
 				LeeroyHomeCommand comm = new LeeroyHomeCommand();
 				comm.commandDescription = result.getString("description");
 				comm.commandExec = result.getString("execute");
 				comm.commandPrice = result.getInt("price");
 				comm.commandString = result.getString("command");
 				comm.commandUsage = result.getString("usage");
 				
 				comm.commandCheck = result.getString("checks").split("\n");
 				
 				return comm;
 			}
 		} catch (SQLException e) { e.printStackTrace(); return null; }
 	}
 	
 	public LeeroyHomeCommand[] GetCommands()
 	{
 		ArrayList<LeeroyHomeCommand> commands = new ArrayList<LeeroyHomeCommand>();
 		
 		if(this.lastCommandGet > ((System.currentTimeMillis() / 1000L) - 60))
 			return this.commCache;
 
 		try {
 			PreparedStatement stat = con.prepareStatement("SELECT * FROM `leeroy_commands` ORDER BY `command` ASC");
 			ResultSet result = stat.executeQuery();
 			
 			if(!result.last())
 			{
 				LeeroyHomeCommand[] tmparr = new LeeroyHomeCommand[commands.size()];
 				return commands.toArray(tmparr);	
 			}
 			else {
 				result.first();
 				do
 				{
 					LeeroyHomeCommand cmdtmp = new LeeroyHomeCommand();
 					cmdtmp.commandCheck = result.getString("checks").split("\n");
 					cmdtmp.commandDescription = result.getString("description");
 					cmdtmp.commandExec = result.getString("execute");
 					cmdtmp.commandPrice = result.getInt("price");
 					cmdtmp.commandString = result.getString("command");
 					cmdtmp.commandUsage = result.getString("usage");
 					
 					commands.add(cmdtmp);
 				} while(result.next());
 				
 				this.lastCommandGet = System.currentTimeMillis() / 1000L;
 				LeeroyHomeCommand[] tmparr = new LeeroyHomeCommand[commands.size()];
				
				this.commCache = commands.toArray(tmparr);
				
 				return commands.toArray(tmparr);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			LeeroyHomeCommand[] tmparr = new LeeroyHomeCommand[commands.size()];
 			return commands.toArray(tmparr);	
 		}
 	}
 	
 	public Boolean PlayerHasCommand(String command, String player)
 	{
 		try {
 			PreparedStatement stat = con.prepareStatement("SELECT * FROM `leeroy_commandpurchases` WHERE command = ? AND player = ?");
 			stat.setString(1, command);
 			stat.setString(2, player);
 			ResultSet result = stat.executeQuery();
 			
 			if(!result.last())
 				return false;
 			else {
 				return true;
 			}
 		} catch (SQLException e) { e.printStackTrace(); return false; }
 	}
 	
 	public Boolean PurchaseCommand(String command, String player)
 	{
 		try {
 			PreparedStatement stat = con.prepareStatement("INSERT INTO `leeroy_commandpurchases` (`command`,`player`,`time`) VALUES (?,?,NOW())");
 			stat.setString(1,command);
 			stat.setString(2,player);
 			
 			return stat.execute();
 		} catch (SQLException e) { e.printStackTrace(); return false; }
 	}
 }
