 package com.gmail.zariust.otherbounds;
 
 import java.util.List;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.gmail.zariust.otherbounds.boundary.Boundary;
 import com.gmail.zariust.otherbounds.common.Verbosity;
 
 class RunAsync implements Runnable {
     
     private Main plugin;
     
     public RunAsync (Main otherBounds) {
         this.plugin = otherBounds;
     }
 
     // probably better to do on delays - 5 to 10 default?
     public void run() {
     	Main.logInfo("Async run...", Verbosity.EXTREME);
         for (World world : plugin.getServer().getWorlds()) {
            for (Player player : world.getPlayers()) {
                 String playerName = player.getName();
                 //  onPlayerMove(player) {
                 int boundaryDamage = 0;
                 int invertedBoundaryDamage = 0;
                 boolean playerInSafeZone = false;
                 List <Boundary> boundList = Main.boundaryList.get(world);
 
                 // Exit if no boundary for this world
                 if (boundList == null) continue;
                 
                 for (Boundary boundary : boundList) {
                 	Main.logInfo("Checking boundary ("+boundary.name+")...", Verbosity.EXTREME);
 
                     // for each boundary:
                     //   check if player in limits
                     //   add to boundarylist for checking whether to send inmessage/outmessage
                     //   add to damagelist for actual damage to apply each (x) ticks
 
                     // Inverted Boundaries
                     if (boundary.invertLimits) {
                         if (boundary.isInside(player, boundary)) {
                         	String message = boundary.dangerMessage;
                         	if (excepted(player, boundary)) {
                         		message = message + " (excepted)";
                         	} else {
                                 invertedBoundaryDamage = boundary.damage;                        		
                         	}
                             if (!Main.boundaryList.contains(playerName, boundary)) {
                                 player.sendMessage(message);
                                 Main.boundaryList.add(playerName, boundary);
                             }
                         } else if (Main.boundaryList.contains(playerName, boundary)) {
                             // no need to set damage to zero for inverted boundaries
                             Main.boundaryList.remove(playerName, boundary);
                             player.sendMessage(boundary.safeMessage); // TODO: send message every delaytick or only once when exiting boundary
                         }
                     } else {
                         // Standard Boundaries
                         if (!(boundary.isInside(player, boundary))) {
                         	Main.logInfo("Player ("+playerName+") is outside boundary ("+boundary.name+"), setting damage to "+boundary.damage+".", Verbosity.HIGHEST);
                         	String message = boundary.dangerMessage;
                         	if (excepted(player, boundary)) {
                         		message = message + " (excepted)";
                         	} else {
                                 boundaryDamage = boundary.damage;
                         	}
                             if (!Main.boundaryList.contains(playerName, boundary)) {
                                 player.sendMessage(message); // FIXME: add dangermessage to a list and only show if not in safe zone
                                 Main.boundaryList.add(playerName, boundary);
                             }
                         } else {
                         	Main.logInfo("Player ("+playerName+") is inside boundary ("+boundary.name+"), setting player as 'safe'.", Verbosity.HIGHEST);
                     		playerInSafeZone = true; // we're inside a boundary so "safe"
                         	if (Main.boundaryList.contains(playerName, boundary)) {
                         		Main.boundaryList.remove(playerName, boundary);
                         		player.sendMessage(boundary.safeMessage);
                         	}
                         }
                     }
                 }
 
                 Effects effects = new Effects();
                 // deal the applicable damage for this player
                 if (playerInSafeZone && Config.safeInsideBoundary) {
                 	Main.logInfo("Player is in safe zone.", Verbosity.EXTREME);
                 } else {
                     // only deal damage if the player is not inside a normal boundary
                     effects.damagePerCheck = boundaryDamage;
                 	Main.logInfo("Player not in safe zone.", Verbosity.EXTREME);
                 }
                 // inverted damage is done even if inside a normal boundary
                 effects.invertedDamagePerCheck = invertedBoundaryDamage;
 
             	Main.logInfo("Adding to damage list for "+playerName+", damage: "+effects.damagePerCheck+" invertedDamage: "+effects.invertedDamagePerCheck, Verbosity.EXTREME);
                 Main.damageList.put(player, effects);
             }
         }
     }
 
 	private boolean excepted(Player player, Boundary boundary) {
 		if (hasException(player, boundary) || hasExceptionPermissions(player, boundary)) {
 			Main.logInfo("Excepted...", Verbosity.EXTREME);
 			return true;
 		}
 		
 		return false;
 	}
 
 	private boolean hasExceptionPermissions(Player player, Boundary boundary) {
 		for (String permission : boundary.exceptPermissions) {
 			if (player.hasPermission("otherbounds.custom."+permission)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean hasException(Player player, Boundary boundary) {
 		for (String exception : boundary.except) {
 			if (player.getName().equalsIgnoreCase(exception)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 }
