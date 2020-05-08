 package com.guntherdw.bukkit.TweakWarp;
 
 import java.sql.*;
 import java.util.*;
 import java.util.logging.Logger;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 /**
  * @author GuntherDW
  */
 public class TweakWarp extends JavaPlugin {
 
     private final static Logger log = Logger.getLogger("Minecraft");
    private String dbhost, db, user, pass;
     // private static Connection conn;
     
     public static Permissions perm = null;
     public Map<String, Warp> warps;
 
     public Warp searchWarp(String warpname)
     {
         Warp warp = null;
         for(String search : warps.keySet())
         {
             if(search.equalsIgnoreCase(warpname))
             {
                 warp = warps.get(search);
                 return warp;
             }
         }
         return warp;
     }
 
     private void loadDriver() {
         final String driverName = "com.mysql.jdbc.Driver";
         try {
             Class.forName(driverName);
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
             //return null;
         }
     }
 
     private Connection getConnection()
     {
         try {
            String url = "jdbc:mysql://"+dbhost+":3306/" + db;
             return DriverManager.getConnection(url + "?autoReconnect=true&user=" + user + "&password=" + pass);
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
     }
 
     public String getWarpList()
     {
         String msg = "";
         // String[] lijstje = (String[]) warps.keySet().toArray();
         List<String> lijst = new ArrayList<String>();
         for(String m : warps.keySet())
         {
             lijst.add(m);
         }
         Collections.sort(lijst, String.CASE_INSENSITIVE_ORDER);
         for(String m : lijst)
         {
             msg += m+", ";
         }
         if(msg.length()!=0) msg = msg.substring(0, msg.length()-2);
         return msg;
     }
 
     public boolean removeWarp(String warpname)
     {
         try{
             Connection conn = getConnection();
             PreparedStatement st = null;
             ResultSet rs = null;
             st = conn.prepareStatement("SELECT name, world FROM warps WHERE name = ?");
             st.setString(1, warpname);
             rs = st.executeQuery();
             if(rs.next()) // found warp with the exact same name
             {
                 st = conn.prepareStatement("DELETE FROM warps WHERE name = ?");
                 st.setString(1, warpname);
                 st.executeUpdate();
                 return true;
             } else {
                 return false;
             }
         } catch(SQLException e)
         {
             log.warning("[TweakWarp] removeWarp error occurred : " + e.getStackTrace());
             return false;
         }
     }
 
     public boolean addWarp(String warpname, Warp warp)
     {
         try{
             Connection conn = getConnection();
             PreparedStatement st = null;
             ResultSet rs = null;
             st = conn.prepareStatement("SELECT name, world FROM warps WHERE name = ?");
             st.setString(1, warpname);
             rs = st.executeQuery();
             if(rs.next()) // found warp with the exact same name
             {
                 removeWarp(warpname);
             }
             st = conn.prepareStatement("INSERT INTO warps (name,x,y,z,rotX,rotY,world) VALUES (?,?,?,?,?,?,?)");
             st.setString(1, warpname);
             st.setDouble(2, warp.getX());
             st.setDouble(3, warp.getY());
             st.setDouble(4, warp.getZ());
             st.setFloat(5, warp.getPitch());
             st.setFloat(6, warp.getYaw());
             st.setString(7, warp.getWorld());
             st.executeUpdate();
             return true;
         } catch(SQLException e)
         {
             log.warning("[TweakWarp] addWarp error occurred : " + e.getStackTrace());
             return false;
         }
     }
 
     public void reloadWarpTable(boolean sql) {
         try {
 
             if(sql)
             {
                 setupConnection();
             }
             Connection conn = getConnection();
             int count = 0;
             this.warps = new HashMap<String, Warp>();
             PreparedStatement st = null;
             ResultSet rs = null;
             st = conn.prepareStatement("SELECT name, `x`, `y`, `z`, `rotX`, `rotY`, `world`, `group` FROM warps");
             rs = st.executeQuery();
 
             while (rs.next()) {
                 this.warps.put(rs.getString(1), new Warp(rs.getDouble(2), rs.getDouble(3),
                         rs.getDouble(4), rs.getFloat(5), rs.getFloat(6),rs.getString(1), rs.getString(7), rs.getString(8)));
                 count++;
             }
             log.info("[TweakWarp] Loaded " + count + " warps!");
 
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public void onDisable() {
         log.info("[TweakWarp] Shutting down!");
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setupConnection() {
        this.dbhost = getConfiguration().getString("dbhost");
         this.db =  getConfiguration().getString("database");
         this.user = getConfiguration().getString("username");
         this.pass = getConfiguration().getString("password");
     }
 
     public void initConfig()
     {
         try{
             getConfiguration().setProperty("database", "databasename");
             getConfiguration().setProperty("username", "database-username");
             getConfiguration().setProperty("password", "database-password");
         } catch (Throwable e)
         {
             log.severe("[TweakWarp] There was an exception while we were saving the config, be sure to doublecheck!");
         }
     }
 
     public void onEnable() {
         if(getConfiguration() == null)
         {
             log.severe("[TweakWarp] You have to configure me now, reboot the server after you're done!");
             getDataFolder().mkdirs();
             initConfig();
             this.setEnabled(false);
         }
         loadDriver();
         setupConnection();
         setupPermissions();
         reloadWarpTable(false);
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[TweakWarp] TweakWarp v"+pdfFile.getVersion()+" enabled!");
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public void setupPermissions() {
         Plugin plugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (perm == null) {
             if (plugin != null) {
                 perm = (Permissions) plugin;
             }
         }
     }
 
     public boolean check(Player player, String permNode) {
         if (perm == null) {
             return true;
         } else {
             return perm.Security.permission(player, permNode);
         }
     }
 
     public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
     {
 
         if(command.getName().equalsIgnoreCase("listwarps")) {
             commandSender.sendMessage(ChatColor.GREEN + "Current warps:");
             String msg = this.getWarpList();
             commandSender.sendMessage(msg);
             return true;
         } else if(command.getName().equalsIgnoreCase("removewarp")) {
             if(commandSender instanceof Player)
             {
                 Player player = (Player) commandSender;
                 if(check(player, "tweakwarp.removewarp"))
                 {
                 String warpname = strings[0];
                 if(warps.containsKey(warpname)) {
                     if(removeWarp(warpname))
                     {
                         player.sendMessage(ChatColor.AQUA + "Warp '"+warpname+"' removed!");
                     } else {
                         player.sendMessage(ChatColor.AQUA + "An error occured, contact an admin!");
                     }
                 }
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have the correct permissions!");
                 }
                 this.reloadWarpTable(true);
                 return true;
             } else {
                 commandSender.sendMessage("You need to be a player to remove a warp!");
             }
             } else if(command.getName().equalsIgnoreCase("setwarp")) {
                 if(commandSender instanceof Player)
                 {
                     Player player = (Player) commandSender;
                     if(check(player, "tweakwarp.setwarp"))
                     {
                         String warpname = strings[0];
                         Warp tempwarp = new Warp(player.getLocation().getX(),
                                                  player.getLocation().getY(),
                                                  player.getLocation().getZ(),
                                                  player.getLocation().getYaw(),
                                                  player.getLocation().getPitch(),
                                                  warpname,
                                                  player.getLocation().getWorld().getName());
                         if(addWarp(warpname, tempwarp))
                         {
                             log.info("[TweakWarp] Warp '"+warpname+"' created by "+player.getName()+"!");
                             player.sendMessage(ChatColor.AQUA + "Warp '"+warpname+"' created!");
                         } else {
                             player.sendMessage(ChatColor.AQUA + "An error occured, contact an admin!");
                         }
                             this.reloadWarpTable(false);
                     } else {
                         player.sendMessage("You do not have the correct permissions");
                     }
                 } else {
                     commandSender.sendMessage("You need to be a player to set a warp!");
                 }
             return true;
         } else if(command.getName().equalsIgnoreCase("warp")) {
             if(commandSender instanceof Player)
             {
                 Player player = (Player) commandSender;
                 if(check(player, "tweakwarp.warp"))
                 {
                     if(strings.length==1)
                     {
                         String warpname = strings[0];
                         Warp w = searchWarp(warpname);
 
                         if(w != null)
                         {
                             player.sendMessage(ChatColor.AQUA + "Found warp with name "+w.getName());
                             Location loc = new Location(this.getServer().getWorld(w.getWorld()),
                                     w.getX(), w.getY() + 1, w.getZ(), w.getPitch(), w.getYaw());
                             player.teleport(loc);
                             player.sendMessage(ChatColor.AQUA + "WHOOOSH!");
                             log.info("[TweakWarp] "+player.getName()+" warped to "+w.getName()+"!");
                         } else {
                             log.info("[TweakWarp] "+player.getName()+" tried to warp to '"+warpname+"'!");
                             player.sendMessage(ChatColor.AQUA + "Warp not found!");
                         }
                     } else {
                         player.sendMessage(ChatColor.AQUA + command.getUsage());
                     }
                 } else {
                     player.sendMessage("You don't have permission to warp!");
                 }
             } else {
                 commandSender.sendMessage("You need to be a player to warp!");
             }
             return true;
         } else if(command.getName().equalsIgnoreCase("reloadwarps")) {
             if(commandSender instanceof Player)
             {
                 Player player = (Player) commandSender;
                 if(!check(player, "tweakwarp.reloadwarps"))
                     return true;
                 log.info("[TweakWarp] "+player.getName()+" issued /reloadwarps!");
             } else {
                 log.info("[TweakWarp] console issued /reloadwarps!");
             }
 
             commandSender.sendMessage(ChatColor.GREEN + "Reloading warps table");
             this.reloadWarpTable(true);
 
             return true;
         }
         return false;
     }
 }
