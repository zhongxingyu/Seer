 package com.github.thereverend403;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Sound;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 public class PlayerListener implements Listener {
 
 	public chatsounds plugin;
 	boolean b;
     String cat;
     String catpurr;
     String dog;
     String doggrowl;
     String chicken;
     String cow;
     String sheep;
     String pig;
     String creeper;
     String ender;
     String explosion;
     String dragondeath;
 	static float pitch = 1;
 	static float volume = 10;
 	public PlayerListener(chatsounds instance) {
 		plugin = instance;
 	}
 
 	@EventHandler
 	public void onChat(AsyncPlayerChatEvent e) {
 		// There has to be a more efficient way to do this...
 		// Only triggering if the player has permission, but still checking on
 		// every chat event...
 		Player player = e.getPlayer();
         if (player.hasPermission("chatsounds.allow")) {
             cat = plugin.getConfig().getString("chatsounds.aliases.cat");
             catpurr = plugin.getConfig().getString("chatsounds.aliases.catpurr");
             dog = plugin.getConfig().getString("chatsounds.aliases.dog");
             doggrowl = plugin.getConfig().getString("chatsounds.aliases.doggrowl");
             chicken = plugin.getConfig().getString("chatsounds.aliases.chicken");
             cow = plugin.getConfig().getString("chatsounds.aliases.cow");
             sheep = plugin.getConfig().getString("chatsounds.aliases.sheep");
             pig = plugin.getConfig().getString("chatsounds.aliases.pig");
             creeper = plugin.getConfig().getString("chatsounds.aliases.creeper");
             ender = plugin.getConfig().getString("chatsounds.aliases.enderman");
             explosion = plugin.getConfig().getString("chatsounds.aliases.explosion");
             dragondeath = plugin.getConfig().getString("chatsounds.aliases.dragondeath");
 		    boolean b = plugin.getConfig().getBoolean("chatsounds.global");
 			String message = e.getMessage();
			if (message.contains(cat)| (message.contains(catpurr) || (message.contains(dog) || (message.contains(doggrowl) || (message.contains(creeper) || (message.contains(cow) || (message.contains(chicken) || (message.contains(ender) || (message.contains(pig) || (message.contains(sheep))))))))))) {
 				if(message.contains(cat)){
 					Sound s = Sound.CAT_MEOW;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(catpurr)){
 					Sound s = Sound.CAT_PURR;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(dog)){
 					Sound s = Sound.WOLF_BARK;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(doggrowl)){
 					Sound s = Sound.WOLF_GROWL;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(creeper)){
 					Sound s = Sound.FUSE;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(cow)){
 					Sound s = Sound.COW_HURT;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(chicken)){
 					Sound s = Sound.CHICKEN_IDLE;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(ender)){
 					Sound s = Sound.ENDERMAN_STARE;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(pig)){
 					Sound s = Sound.PIG_IDLE;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
 				if(message.contains(sheep)){
 					Sound s = Sound.SHEEP_IDLE;
 					if(b){
 						for(Player p : Bukkit.getOnlinePlayers()){
 							playSound(p, s);
 						}
 					}else{
 						playSound(player, s);
 					}
 				}
                 if(message.contains(explosion)){
                     Sound s = Sound.EXPLODE;
                     if(b){
                         for(Player p : Bukkit.getOnlinePlayers()){
                             playSound(p, s);
                         }
                     }else{
                         playSound(player, s);
                     }
                 }
                 if(message.contains(dragondeath)){
                     Sound s = Sound.ENDERDRAGON_DEATH;
                     if(b){
                         for(Player p : Bukkit.getOnlinePlayers()){
                             playSound(p, s);
                         }
                     }else{
                         playSound(player, s);
                     }
                 }
 			}
 		}
 	}
 	public static void playSound(Player p, Sound s){
 		p.playSound(p.getLocation(), s, volume, pitch);
 	}
 }
 
 //empty line to force change for Maven
