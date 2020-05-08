 package com.minecraftdimensions.bungeesuiteteleports.managers;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.minecraftdimensions.bungeesuiteteleports.BungeeSuiteTeleports;
 import com.minecraftdimensions.bungeesuiteteleports.tasks.PluginMessageTask;
 
 
 public class TeleportsManager {
 	
 
 public static HashMap<String,Player> pendingTeleports = new HashMap<String,Player>();
 public static HashMap<String,Location> pendingTeleportLocations = new HashMap<String,Location>();
 public static ArrayList<Player> ignoreTeleport= new ArrayList<Player>();
 
 	public static void tpAll(CommandSender sender, String targetPlayer) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TpAll");
 			out.writeUTF(sender.getName());
 			out.writeUTF(targetPlayer);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 
 	}
 
 	public static void tpaRequest(CommandSender sender, String targetPlayer) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TpaRequest");
 			out.writeUTF(sender.getName());
 			out.writeUTF(targetPlayer);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 	}
 
 	public static void tpaHereRequest(CommandSender sender, String targetPlayer) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TpaHereRequest");
 			out.writeUTF(sender.getName());
 			out.writeUTF(targetPlayer);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 
 	}
 
 	public static void tpAccept(CommandSender sender) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TpAccept");
 			out.writeUTF(sender.getName());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 
 	}
 
 	public static void tpDeny(String sender) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TpDeny");
 			out.writeUTF(sender);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 
 	}
 
 	public static void sendDeathBackLocation(Player p) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("PlayersDeathBackLocation");
 			out.writeUTF(p.getName());
 			Location l = p.getLocation();
 			out.writeUTF(l.getWorld().getName());
 			out.writeDouble(l.getX());
 			out.writeDouble(l.getY());
 			out.writeDouble(l.getZ());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 	}
 	
 	public static void sendTeleportBackLocation(Player p, boolean empty) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("PlayersTeleportBackLocation");
 			out.writeUTF(p.getName());
 			Location l = p.getLocation();
 			out.writeUTF(l.getWorld().getName());
 			out.writeDouble(l.getX());
 			out.writeDouble(l.getY());
 			out.writeDouble(l.getZ());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b, empty)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 	}
 
 	public static void sendPlayerBack(CommandSender sender) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("SendPlayerBack");
 			out.writeUTF(sender.getName());
 			out.writeBoolean(sender.hasPermission("bungeesuite.teleports.back.death"));
 			out.writeBoolean(sender.hasPermission("bungeesuite.teleports.back.teleport"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 	}
 
 	public static void toggleTeleports(String name) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("ToggleTeleports");
 			out.writeUTF(name);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 	}
 	
 	public static void teleportPlayerToPlayer(final String player, String target){
 		Player p = Bukkit.getPlayer(player);
 		Player t = Bukkit.getPlayer(target);
 		if(p!=null){
 			p.teleport(t);
 		}else{
 			pendingTeleports.put(player, t);
 			//clear pending teleport if they dont connect
 			Bukkit.getScheduler().runTaskLaterAsynchronously(BungeeSuiteTeleports.instance, new Runnable(){
 				@Override
 				public void run() {
 					if(pendingTeleports.containsKey(player)){
 						pendingTeleports.remove(player);
 					}
 					
 				}
 			}, 100);
 		}		
 	}
 	
 	public static void teleportPlayerToLocation(final String player, String world, double x,double y, double z){
 		Location t = new Location(Bukkit.getWorld(world),x,y,z);
 			pendingTeleportLocations.put(player, t);
 			//clear pending teleport if they dont connect
 			Bukkit.getScheduler().runTaskLaterAsynchronously(BungeeSuiteTeleports.instance, new Runnable(){
 				@Override
 				public void run() {
 					if(pendingTeleportLocations.containsKey(player)){
 						pendingTeleportLocations.remove(player);
 					}
 				}
			}, 100);		
 	}
 
 	public static void teleportToPlayer(CommandSender sender, String player, String target) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TeleportToPlayer");
 			out.writeUTF(sender.getName());
 			out.writeUTF(player);
 			out.writeUTF(target);
 			out.writeBoolean(sender.hasPermission("bungeesuite.teleports.tp.silent"));
 			out.writeBoolean(sender.hasPermission("bungeesuite.teleports.tp.bypass"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 	}
 
 	public static void teleportToLocation(String player, String loc) {
 		ByteArrayOutputStream b = new ByteArrayOutputStream();
 		DataOutputStream out = new DataOutputStream(b);
 		try {
 			out.writeUTF("TeleportToLocation");
 			out.writeUTF(player);
 			out.writeUTF(loc);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new PluginMessageTask(b)
 		.runTaskAsynchronously(BungeeSuiteTeleports.instance);
 		
 	}
 
 }
