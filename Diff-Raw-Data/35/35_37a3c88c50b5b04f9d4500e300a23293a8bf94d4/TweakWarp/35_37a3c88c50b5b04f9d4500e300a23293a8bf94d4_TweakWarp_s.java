 /*
  * Copyright (c) 2012 GuntherDW
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package com.guntherdw.bukkit.TweakWarp;
 
 import com.guntherdw.bukkit.TweakWarp.DataSource.MySQL;
 import com.guntherdw.bukkit.tweakcraft.TweakcraftUtils;
 // import com.nijikokun.bukkit.Permissions.Permissions;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.*;
 import java.util.logging.Logger;
 
 
 /**
  * @author GuntherDW
  */
 public class TweakWarp extends JavaPlugin {
 
     protected final static Logger log = Logger.getLogger("Minecraft");
     public static final String DEFAULT_WARP_GROUP = "default";
     public static final String DEFAULT_ACCESS_GROUP = "";
 
     // public static Permissions perm = null;
     public Map<String, WarpGroup> warps = new HashMap<String, WarpGroup>();
 
    public List<String> saveWarps = new ArrayList<String>();;
     public TweakcraftUtils tweakcraftutils = null;
 
     private MySQL ds;
 
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
 
     public List<Warp> matchWarp(String warpname, boolean strict) {
         List<Warp> warpList = new ArrayList<Warp>();
         for(WarpGroup w: warps.values()){
             Warp warp = strict?w.getWarp(warpname):w.matchWarp(warpname);
             if(warp != null){
                 warpList.add(warp);
             }
         }
         return warpList;
     }
 
     public List<Warp> matchWarp(String warpname) {
         return this.matchWarp(warpname, false);
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
         List<Warp> warpies = ds.getAllWarps();
         warps.clear();
         for(Warp w : warpies) {
             registerWarp(w);
         }
     }
 
     public MySQL getDataSource() {
         return ds;
     }
 
     public void onDisable() {
         log.info("[TweakWarp] Shutting down!");
     }
 
     public void onEnable() {
         // setupPermissions();
         setupTCUtils();
 
         this.ds = new MySQL(this);
         saveWarps.clear();
         // setupDatabase();
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
 
     /* public void setupPermissions() {
         Plugin plugin = this.getServer().getPluginManager().getPlugin("Permissions");
 
         if (perm == null) {
             if (plugin != null) {
                 perm = (Permissions) plugin;
             }
         }
     }
 
     public boolean check(Player player, String permNode) {
         if (perm == null) {
             return player.isOp();
         } else {
             return player.isOp() ||
                     perm.getHandler().permission(player, permNode);
         }
     } */
 
     public boolean check(Player player, String permNode) {
         /* if (perm == null) {
             return player.isOp();
         } else { */
             return player.isOp() ||
                player.hasPermission(permNode);
         // }
     }
 
 
     public Logger getLogger() {
         return log;
     }
 
     /* public boolean inGroup(Player player, String group) {
         if(group == null || group.trim().equals(""))
             return true;
 
         if(perm == null) {
             return true;
         } else {
             return player.isOp() || perm.getHandler().inGroup(player.getWorld().getName(), player.getName(), group);
         }
     } */
 
 
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings)
    {
         String cmd = command.getName().toLowerCase();
         if(cmd.equals("listwarps")) {
             if(strings.length > 0) {
                 commandSender.sendMessage(ChatColor.GREEN + "Current warps in group '" + strings[0] + "':");
                 commandSender.sendMessage(getWarps(strings[0].toLowerCase()));
             } else {
                 commandSender.sendMessage(ChatColor.GREEN + "Current warps: ("+ChatColor.GOLD+DEFAULT_WARP_GROUP+ChatColor.GREEN+")");
                 commandSender.sendMessage(getWarps(DEFAULT_WARP_GROUP));
                 commandSender.sendMessage(ChatColor.GREEN + "Current warpgroups:");
                 String message = "";
                 for(WarpGroup wg: warps.values()){
                     message += ChatColor.AQUA + wg.getName();
                     message += ChatColor.GOLD + "(" + wg.getWarpCount() + ")";
                 }
                 commandSender.sendMessage(message);
             }
             return true;
         } else if(cmd.equals("removewarp")) {
             String sendername = "CONSOLE";
             if(commandSender instanceof Player && !(check((Player)commandSender, "tweakwarp.removewarp"))) {
                 commandSender.sendMessage(ChatColor.RED+"You do not have the correct permission!");
                 return true;
             }
             if(commandSender instanceof Player) {
                 sendername = ((Player) commandSender).getName();
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
                 log.info("[TweakWarp] Warp '"+warpname+"' removed by "+sendername+"!");
                 commandSender.sendMessage(ChatColor.AQUA + "Warp '"+warpname+"' removed!");
             } else {
                 commandSender.sendMessage(ChatColor.AQUA + "An error occured, contact an admin!");
             }
             // this.reloadWarpTable(); SERIOUSLY ?
             return true;
         } else if(cmd.equals("setwarp")) {
             if(!(commandSender instanceof Player)) {
                 commandSender.sendMessage("You need to be a player to set a warp!");
                 return true;
             }
 
             Player player = (Player) commandSender;
             if(!check(player, "tweakwarp.setwarp")){
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
             Warp warp = null;
             if(strings.length > 1){
                 String warpgroup = strings[1].toLowerCase();
                 warp = matchWarp(warpgroup, warpname);
             } else {
                 // String group = DEFAULT_ACCESS_GROUP;
                 List<Warp> pre = matchWarp(warpname);
                 List<Warp> warpList      = new ArrayList<Warp>();
                 for(Warp w: pre) {
                     // Check for a DIRECT hit
                     if(w.getName().equalsIgnoreCase(warpname)) {
                         warpList.add(w);
                     }
                 }
                 if(warpList.size()==0) warpList=pre;
                 
                 if(warpList.size()>1){
                     String msg = ChatColor.AQUA + "Multiple warps found " + ChatColor.WHITE;
                     for(Iterator<Warp> it = warpList.iterator(); it.hasNext();){
                         Warp temp = it.next();
                         msg += temp.getName() + "(" + temp.getWarpgroup() + "), ";
                     }
                     player.sendMessage(msg);
                     return true;
                 }
                 else if(warpList.size()==1) {
                     warp = warpList.get(0);
                 }
             }
 
             if(warp == null) {
                 log.info("[TweakWarp] "+player.getName()+" tried to warp to '"+warpname+"'!");
                 player.sendMessage(ChatColor.AQUA + "Warp not found!");
                 return true;
             }
 
             /* if(!inGroup(player,warp.getAccessgroup())) {
                 log.info("[TweakWarp] "+player.getName()+" tried to warp to '"+warpname+"'!");
                 player.sendMessage(ChatColor.AQUA + "Warp not found!");
                 return true;
             } */
 
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
         } else if(cmd.equals("regroupwarp")){
             if(commandSender instanceof Player)
                 if(this.check((Player)commandSender, "tweakwarp.regroupwarp")) {
                     commandSender.sendMessage(ChatColor.RED+"You don't have permission for this!");
                     return true;
                 }
             
             if(strings.length!=2) {
                 commandSender.sendMessage(ChatColor.YELLOW+"Incorrect usage!");
                 commandSender.sendMessage(command.getUsage());
                 return true;
             }
             
             String warpname = strings[0];
             String warpgrp  = strings[1];
             
             List<Warp> warplist = matchWarp(warpname, true);
             if(warplist.size()==0) {
                 commandSender.sendMessage(ChatColor.YELLOW+"No warp by that name found!");
                 return true;
             } else if(warplist.size()!=1) {
                 commandSender.sendMessage(ChatColor.YELLOW+"Multiple warps found!");
                 for(Warp w : warplist)
                     commandSender.sendMessage(ChatColor.GOLD+w.getWarpgroup() + ChatColor.AQUA+"->"+ChatColor.GREEN+w.getName());
                 return true;
             }
             
             Warp warp = warplist.get(0);
             String oldGroup = warp.getWarpgroup();
             WarpGroup wgold = warps.get(oldGroup);
             WarpGroup wgnew = warps.get(warpgrp);
             if(wgold!=null && wgnew != null) {
                 if(warp.updateGroup(this, warpgrp))
                     commandSender.sendMessage(ChatColor.GREEN+"This warp has been moved to "+ChatColor.GOLD + wgnew.getName()+ChatColor.GREEN+"!");
                 else
                     commandSender.sendMessage(ChatColor.RED+"Something went wrong, alert a serveradmin!");
             } else {
                 commandSender.sendMessage(ChatColor.RED+"Couldn't find one of the groups!");
             }
             return true;
 
         }
         return false;
     }
 }
