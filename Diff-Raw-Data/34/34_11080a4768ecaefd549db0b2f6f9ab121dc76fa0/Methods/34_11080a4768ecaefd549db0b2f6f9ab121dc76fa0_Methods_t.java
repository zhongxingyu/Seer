 /**
  *
  * Copyright by Samuel Castro, 2011
  * All Rights Reserved
  *
  * NOTES:
  * Please do not redistribute this plugin without my permission
  */
 package com.SySammy.triggercmds;
 
 import com.avaje.ebean.QueryIterator;
 import java.util.ArrayList;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class Methods {
 
     tCmds plugin;
 
     public Methods(tCmds aThis) {
         this.plugin = aThis;
     }
 
     private tReg OpenDataBase(String p, String iName) {
         tReg persistenceClass = plugin.getDatabase().find(tReg.class).where().ieq("PlayerName", p).ieq("intName", iName).findUnique();
         if (persistenceClass == null) {
             persistenceClass = new tReg();
         }
         return persistenceClass;
     }
 
     private boolean iNameExist(String p, String iName) {
         tReg persistenceClass = plugin.getDatabase().find(tReg.class).where().ieq("PlayerName", p).ieq("intName", iName).findUnique();
         if (persistenceClass == null) {
             return false;
         }
         return true;
     }
 
     private boolean IsRegOn(Player p) {
         return plugin.iNames.containsKey(p);
     }
 
     public void RegEditInteraction(Player p, String iName) {
         if (IsRegOn(p)) {
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.RED + "You're already editing a interaction" + ChatColor.GOLD + " ]");
             return;
         }
         if (iNameExist(p.getName(), iName)) {
             plugin.iNames.put(p, iName);
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.GREEN + "Interaction Opened" + ChatColor.GOLD + " ]");
             p.sendMessage(ChatColor.GREEN + "You are now editing: " + ChatColor.BLUE + iName);
             return;
         } else {
             tReg plyReg = OpenDataBase(p.getName(), iName);
             plyReg.setPlayerName(p.getName());
             plyReg.setIntName(iName);
             plugin.getDatabase().save(plyReg);
             plugin.iNames.put(p, iName);
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.GREEN + "Interaction Registered" + ChatColor.GOLD + " ]");
             p.sendMessage(ChatColor.GREEN + "You are now editing: " + ChatColor.BLUE + iName);
         }
     }
 
     public void RegCmd(Player p, String cmd) {
         if (!IsRegOn(p)) {
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.RED + "Please start the edit function first" + ChatColor.GOLD + " ]");
             return;
         }
         tReg plyReg = OpenDataBase(p.getName(), plugin.iNames.get(p));
         plyReg.setCmd(cmd);
         plugin.getDatabase().save(plyReg);
 
         if (getLoc(p) != null) {
             Location loc = getLoc(p);
             plugin.Cmds.put(loc, plyReg.getCmd());
         }
         p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.GREEN + "Details saved to the database: " + ChatColor.GOLD + " ]");
         p.sendMessage(ChatColor.GREEN + "Command: " + ChatColor.BLUE + cmd);
     }
 
     public void RegLocation(Player p, Location loc) {
         if (!IsRegOn(p)) {
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.RED + "Please start the edit function first" + ChatColor.GOLD + " ]");
             return;
         }
         plugin.Cmds.remove(getLoc(p));
         tReg plyReg = OpenDataBase(p.getName(), plugin.iNames.get(p));
         if (iNameExist(p.getName(), plugin.iNames.get(p))) {
             plyReg.setWorld(loc.getWorld().getName());
             plyReg.setX(loc.getX());
             plyReg.setY(loc.getY());
             plyReg.setZ(loc.getZ());
             plugin.getDatabase().save(plyReg);
             plugin.Cmds.put(loc, plyReg.getCmd());
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.GREEN + "Details saved to the database" + ChatColor.GOLD + " ]");
             p.sendMessage(ChatColor.GREEN + "Location: " + ChatColor.BLUE + loc.getWorld().getName() + "[" + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "]");
         }
         DelRegState(p);
     }
 
     public boolean HasInteration(String p, Location loc) {
         return plugin.Cmds.containsKey(loc);
     }
 
     /*public String GetCmds(String p, String[] arg) {
     String cmd = "";
     for (int x = 1; x < arg.length; x++) {
     cmd = cmd + " " + arg[x];
     }
     String replace = "";
     if (cmd.contains("$me:")) {
     replace = cmd.replace("$me:", p + ":");
     }
     if (cmd.contains("$me")) {
     replace = cmd.replace("$me", p);
     } else {
     replace = cmd;
     }
     return replace.trim();
     }*/
     public void DelRegState(Player p) {
         if (!IsRegOn(p)) {
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.RED + "No edit function started" + ChatColor.GOLD + " ]");
             return;
         }
         plugin.iNames.remove(p);
         p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.GREEN + "Iteraction edit closed" + ChatColor.GOLD + " ]");
     }
 
     public void ExecuteCmd(Player p, Location loc) {
         String[] InitCmds = plugin.Cmds.get(loc).split("&");
         ArrayList<String> FinalCmds = new ArrayList<String>();
         Player sender = null;
         for (int x = 0; InitCmds.length > x; x++) {
             String[] MidCmd = InitCmds[x].split("/");
             if (MidCmd[0].equalsIgnoreCase("$me:")) {
            	sender = plugin.getServer().getPlayer()
             }
         }
     }
 
     /*  public Player TcmdsBot(Player p) {
     CraftServer cServer = (CraftServer) plugin.getServer();
     CraftWorld cWorld = (CraftWorld) p.getWorld();
     EntityPlayer fakeEntityPlayer = new EntityPlayer(cServer.getHandle().server,
     cWorld.getHandle(), "tcmdsbot", new ItemInWorldManager(cWorld.getHandle()));
     fakeEntityPlayer.netServerHandler = ((CraftPlayer) p).getHandle().netServerHandler;
     Player TcmdsBot = (Player) fakeEntityPlayer.getBukkitEntity();
     TcmdsBot.setDisplayName("tcmdsbot");
     ((CraftPlayer) TcmdsBot).getHandle().name = "tcmdsbot";
     TcmdsBot.saveData();
     TcmdsBot.loadData();
     return TcmdsBot;
     }
      */
     public Location getLoc(Player p) {
         try {
             tReg plyReg = OpenDataBase(p.getName(), plugin.iNames.get(p));
             Location loc = new Location(plugin.getServer().getWorld(plyReg.getWorld()), plyReg.getX(), plyReg.getY(), plyReg.getZ());
             return loc;
         } catch (NullPointerException e) {
             return null;
         }
     }
 
     public void iList(Player p) {
         p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.GREEN + "List of Triggers and Links" + ChatColor.GOLD + " ]");
         QueryIterator<tReg> findIterate = plugin.getDatabase().find(tReg.class).where().ieq("PlayerName", p.getName()).findIterate();
         while (findIterate.hasNext()) {
             tReg plyReg = findIterate.next();
             p.sendMessage(ChatColor.GREEN + "Trigger Name: " + ChatColor.BLUE + plyReg.getIntName());
             p.sendMessage(ChatColor.GREEN + "Command: " + ChatColor.BLUE + plyReg.getCmd());
             if (plyReg.getWorld() == null) {
                 p.sendMessage(ChatColor.GREEN + "Location: " + ChatColor.BLUE + "Not yet placed.");
             } else {
                 p.sendMessage(ChatColor.GREEN + "Location: " + ChatColor.BLUE + plyReg.getWorld() + "[" + plyReg.getX() + "," + plyReg.getY() + "," + plyReg.getZ() + "]");
             }
             p.sendMessage("");
         }
         findIterate.close();
     }
 
     public void delTrigger(Player p) {
         if (!IsRegOn(p)) {
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.RED + "Please start the edit function first" + ChatColor.GOLD + " ]");
             return;
         }
         tReg plyReg = OpenDataBase(p.getName(), plugin.iNames.get(p));
         plugin.Cmds.remove(getLoc(p));
         plugin.iNames.remove(p);
         plugin.getDatabase().delete(tReg.class, plyReg.getInteractionId());
         p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.GREEN + "Trigger deleted from the database" + ChatColor.GOLD + " ]");
     }
 
     public void delLink(Player p) {
         if (!IsRegOn(p)) {
             p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.RED + "Please start the edit function first" + ChatColor.GOLD + " ]");
             return;
         }
         plugin.Cmds.remove(getLoc(p));
         tReg plyReg = OpenDataBase(p.getName(), plugin.iNames.get(p));
         plyReg.setWorld(" ");
         plugin.getDatabase().save(plyReg);
         p.sendMessage(ChatColor.GOLD + "[ " + ChatColor.GREEN + "Link deleted, your trigger no longer has a location" + ChatColor.GOLD + " ]");
         DelRegState(p);
     }
 }
