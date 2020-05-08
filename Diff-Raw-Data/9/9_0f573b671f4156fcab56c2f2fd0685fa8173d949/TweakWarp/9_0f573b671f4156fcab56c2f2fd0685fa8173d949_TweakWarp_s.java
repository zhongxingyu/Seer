 package com.guntherdw.bukkit.TweakWarp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 
 /**
  * @author GuntherDW
  */
 public class TweakWarp extends JavaPlugin {
 
     protected final static Logger log = Logger.getLogger("Minecraft");
     public static final String DEFAULT_WARP_GROUP = "default";
     public static final String DEFAULT_ACCESS_GROUP = "";
 
     public static Permissions perm = null;
     public Map<String, WarpGroup> warps = new HashMap<String, WarpGroup>();
 
     public List<String> saveWarps = new ArrayList<String>();;
     public TweakcraftUtils tweakcraftutils = null;
 
     public boolean registerWarp(Warp warp) {
     	WarpGroup group = getWarpGroup(warp.getWarpgroup());
     	if(group == null) {
     		group = new WarpGroup(warp.getWarpgroup());
     		warps.put(group.getName(), group);
     	}
     	return group.registerWarp(warp);
     }
     
     public boolean forgetWarp(Warp warp) {
     	WarpGroup group = getWarpGroup(warp.getWarpgroup());
     	if(group == null) 
     		return false;
     	
     	return group.forgetWarp(warp);
     }
     
     public Warp getWarp(String warpgroup, String warpname) {
     	WarpGroup group = getWarpGroup(warpgroup);
     	return group == null ? null : group.getWarp(warpname);
     }
     
     public Warp matchWarp(String warpgroup, String warpname) {
     	WarpGroup group = matchWarpGroup(warpgroup);
     	return group == null ? null : group.matchWarp(warpname);
     }
     
     public WarpGroup getWarpGroup(String warpgroup) {
     	return warps.get(warpgroup);
     }
     
     public WarpGroup matchWarpGroup(String warpgroup) {
     	WarpGroup rt = getWarpGroup(warpgroup);
     	if(rt == null) {
     		int delta = Integer.MAX_VALUE;
     		for(WarpGroup group : warps.values()) {
     			if(group.getName().contains(warpgroup) && Math.abs(group.getName().length() - warpgroup.length()) < delta) {
     				delta = Math.abs(group.getName().length() - warpgroup.length());
     				rt = group;
     				if(delta == 0) break;
     			}
     		}
     	}
     	return rt;
     }
 
     public String getWarps(String warpgroup) {
     	String rt = "";
     	WarpGroup group = matchWarpGroup(warpgroup);
     	if(group == null) return rt;
     	
     	List<String> orderedList = new ArrayList<String>();
         
     	for(Warp warp : group.getWarps())
         	orderedList.add(warp.getName());
 
         Collections.sort(orderedList, String.CASE_INSENSITIVE_ORDER);
         for(String m : orderedList)
             rt += m+", ";
 
         if(rt.length()!=0) rt = rt.substring(0, rt.length()-2);
         return rt;
     }
 
     public void loadAllWarps() {
         List<Warp> warpies =  this.getDatabase().find(Warp.class).findList();
         warps.clear();
         for(Warp w : warpies) {
             registerWarp(w);
         }
     }
 
     public void onDisable() {
         log.info("[TweakWarp] Shutting down!");
     }
 
     private void setupDatabase() {
         try {
             getDatabase().find(Warp.class).findRowCount();
         } catch (PersistenceException ex) {
             log.info("Installing database for " + getDescription().getName() + " due to first time usage");
             installDDL();
         }
     }
 
     @Override
     public List<Class<?>> getDatabaseClasses() {
         List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(Warp.class);
         return list;
     }
 
     public void onEnable() {
         setupPermissions();
         setupTCUtils();
 
         saveWarps.clear();
         setupDatabase();
         loadAllWarps();
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[TweakWarp] TweakWarp v"+pdfFile.getVersion()+" enabled!");
     }
 
     public void setupTCUtils() {
         Plugin plugin = this.getServer().getPluginManager().getPlugin("TweakcraftUtils");
 
         if (tweakcraftutils == null) {
             if (plugin != null) {
                 tweakcraftutils = (TweakcraftUtils) plugin;
             }
         }
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
             return player.isOp() ||
                     perm.getHandler().permission(player, permNode);
         }
     }
     
     public boolean inGroup(Player player, String group) {
     	if(group == null || group.trim().equals(""))
     		return true;
     	
     	if(perm == null) {
     		return player.isOp();
     	} else {
     		return player.isOp() || perm.getHandler().inGroup(player.getWorld().getName(), player.getName(), group);
     	}
     }
     
 
     public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
     {
     	String cmd = command.getName().toLowerCase();
         if(cmd.equals("listwarps")) {
         	if(strings.length > 0) {
         		commandSender.sendMessage(ChatColor.GREEN + "Current warps in group '" + strings[0] + "':");
         		commandSender.sendMessage(getWarps(strings[0].toLowerCase()));
         	} else {
         		commandSender.sendMessage(ChatColor.GREEN + "Current warps:");
         		commandSender.sendMessage(getWarps(DEFAULT_WARP_GROUP));
         	}
             return true;
         } else if(cmd.equals("removewarp")) {
             if(commandSender instanceof Player && !(check((Player)commandSender, "tweakwarp.removewarp"))) {
                 commandSender.sendMessage(ChatColor.RED+"You do not have the correct permission!");
                 return true;
             }
             if(strings.length < 1) {
             	commandSender.sendMessage(ChatColor.AQUA + command.getUsage());
             	return true;
             }
 
             String warpname = strings[0].toLowerCase();
             String warpgroup = DEFAULT_WARP_GROUP;
             if(strings.length > 1) warpgroup = strings[1].toLowerCase();
             Warp warp = getWarp(warpgroup, warpname);
             if(warp == null) {
             	commandSender.sendMessage(ChatColor.AQUA + "Warp with name '"+warpname+"' not found!");
             	return true;
             }
             
             if(warp.delete(this)) {
                 commandSender.sendMessage(ChatColor.AQUA + "Warp '"+warpname+"' removed!");
             } else {
                 commandSender.sendMessage(ChatColor.AQUA + "An error occured, contact an admin!");
             }
             // this.reloadWarpTable(); SERIOUSLY ?
             return true;
         } else if(cmd.equals("setwarp")) {
            if(commandSender instanceof Player) {
             	commandSender.sendMessage("You need to be a player to set a warp!");
             	return true;
             }
             
             Player player = (Player) commandSender;
             if(check(player, "tweakwarp.setwarp")){
             	player.sendMessage("You do not have the correct permissions");
             	return true;
             }
             
             if(strings.length < 1) {
             	player.sendMessage(ChatColor.AQUA + command.getUsage());
             	return true;
             }
                 
             String warpname = strings[0];
             
             String warpgroup = DEFAULT_WARP_GROUP;
             if(strings.length > 1) warpgroup = strings[1].toLowerCase();
             
             String accessgroup = DEFAULT_ACCESS_GROUP;
             if(strings.length > 2) accessgroup = strings[2].toLowerCase();
             
             Warp tempwarp = new Warp(player.getLocation(), warpname, warpgroup, accessgroup);
             if(tempwarp.save(this)) {
                 log.info("[TweakWarp] Warp '"+warpname+"' created by "+player.getName()+"!");
                 player.sendMessage(ChatColor.AQUA + "Warp '"+warpname+"' created!");
             } else {
                 player.sendMessage(ChatColor.AQUA + "An error occured, contact an admin!");
             }
             // this.reloadWarpTable(); SERIOUSLY ?
             return true;
         } else if(cmd.equals("warp")) {
             if(!(commandSender instanceof Player)) {
             	commandSender.sendMessage("You need to be a player to warp!");
             	return true;
             }
             Player player = (Player) commandSender;
             if(!check(player, "tweakwarp.warp")) {
             	player.sendMessage("You don't have permission to warp!");
             	return true;
             }
             if(strings.length < 1) {
             	player.sendMessage(ChatColor.AQUA + command.getUsage());
             	return true;
             }
             String warpname = strings[0].toLowerCase();
             String warpgroup = DEFAULT_WARP_GROUP;
             if(strings.length > 1) warpgroup = strings[1].toLowerCase();
             Warp warp = matchWarp(warpgroup, warpname);
             
             if(warp == null) {
             	log.info("[TweakWarp] "+player.getName()+" tried to warp to '"+warpname+"'!");
                 player.sendMessage(ChatColor.AQUA + "Warp not found!");
                 return true;
             }
             
             if(!inGroup(player,warp.getAccessgroup())) {
             	log.info("[TweakWarp] "+player.getName()+" tried to warp to '"+warpname+"'!");
                 player.sendMessage(ChatColor.AQUA + "Warp not found!");
                 return true;
             }
             
             player.sendMessage(ChatColor.AQUA + "Found warp with name "+warp.getName());
             Location loc = warp.getLocation(getServer());
             if(tweakcraftutils != null && !saveWarps.contains(player.getName())) {
                 tweakcraftutils.getTelehistory().addHistory(player.getName(), player.getLocation());
             }
             player.teleport(loc);
             player.sendMessage(ChatColor.AQUA + "WHOOOSH!");
             log.info("[TweakWarp] "+player.getName()+" warped to "+warp.getName()+"!");
             
             return true;
         } else if(cmd.equals("reloadwarps")) {
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
             loadAllWarps();
 
             return true;
         } else if(cmd.equals("warpback")) {
             if(!(commandSender instanceof Player)) {
             	commandSender.sendMessage("Consoles need a tp history nowadays?");
             	return true;
             }
             Player p = (Player) commandSender;
             if(!check(p, "tweakcraftutils.tpback")) {
                 p.sendMessage("You don't have permission to tpback, so this would be useless!");
                 return true;
             }
 
             if(saveWarps.contains(p.getName())) {
                 p.sendMessage(ChatColor.GOLD+"Warping will no longer save a TPBack instance!");
                 saveWarps.add(p.getName());
             } else {
                 p.sendMessage(ChatColor.GOLD+"Warping will save a TPBack instance!");
                 saveWarps.remove(p.getName());
             }
             return true;
         }
         return false;
     }
 }
