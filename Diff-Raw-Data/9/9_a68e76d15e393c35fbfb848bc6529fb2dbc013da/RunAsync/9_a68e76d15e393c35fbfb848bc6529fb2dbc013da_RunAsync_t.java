 package com.gmail.zariust.otherbounds;
 
 import java.util.ArrayList;
import java.util.ConcurrentModificationException;
 import java.util.List;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import com.gmail.zariust.otherbounds.boundary.Boundary;
 import com.gmail.zariust.otherbounds.common.Verbosity;
 
 class RunAsync implements Runnable {
     
     private final OtherBounds plugin;
     
     public RunAsync (OtherBounds otherBounds) {
         this.plugin = otherBounds;
     }
 
     // probably better to do on delays - 5 to 10 default?
     public void run() {
     	//Main.logInfo("Async run...", Verbosity.EXTREME);
         for (World world : plugin.getServer().getWorlds()) {
         	List<Player> playerList = new ArrayList<Player>();
        	try {
        		playerList.addAll(world.getPlayers());
        	} catch (ConcurrentModificationException ex) {
        		Log.high("Async - concurrentexception with playerlist: skipping this damage cycle.");
        		continue; // just skip this damage
        	}
             for (Player player : playerList) {
                 String playerName = player.getName();
                 //  onPlayerMove(player) {
                 int boundaryDamage = 0;
                 int invertedBoundaryDamage = 0;
                 boolean playerInSafeZone = false;
                 List <Boundary> boundList = OtherBounds.boundaryList.get(world);
 
             	// First remove player since if there's no boundary, they are safe
                 List<Boundary> playerBoundaries = OtherBounds.boundaryList.get(playerName);
 
                 if (playerBoundaries != null) {
                     List<Boundary> copyPlayerBoundaries = new ArrayList<Boundary>();
                     copyPlayerBoundaries.addAll(playerBoundaries);
                 	for (Boundary bound : copyPlayerBoundaries) {
                 		if (!boundList.contains(bound)) {
                 			player.sendMessage(bound.safeMessage);
                 			OtherBounds.damageList.remove(player);
                 			OtherBounds.boundaryList.remove(playerName, bound);
                 		}
                 	}
                 }
                 
                 // Exit if no boundary for this world
                 if (boundList == null) {
                 	continue;
                 }
                 
                 for (Boundary boundary : boundList) {
                 	//Main.logInfo("Checking boundary ("+boundary.name+")...", Verbosity.EXTREME);
 
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
                             if (!OtherBounds.boundaryList.contains(playerName, boundary)) {
                                 player.sendMessage(message);
                                 OtherBounds.boundaryList.add(playerName, boundary);
                             }
                         } else if (OtherBounds.boundaryList.contains(playerName, boundary)) {
                             // no need to set damage to zero for inverted boundaries
                             OtherBounds.boundaryList.remove(playerName, boundary);
                             player.sendMessage(boundary.safeMessage); // TODO: send message every delaytick or only once when exiting boundary
                         }
                     } else {
                         // Standard Boundaries
                         if (!(boundary.isInside(player, boundary))) {
                         	OtherBounds.logInfo("Player ("+playerName+") is outside boundary ("+boundary.name+"), setting damage to "+boundary.damage+".", Verbosity.HIGHEST);
                         	String message = boundary.dangerMessage;
                         	if (excepted(player, boundary)) {
                         		message = message + " (excepted)";
                         	} else {
                                 boundaryDamage = boundary.damage;
                         	}
                             if (!OtherBounds.boundaryList.contains(playerName, boundary)) {
                                 player.sendMessage(message); // FIXME: add dangermessage to a list and only show if not in safe zone
                                 OtherBounds.boundaryList.add(playerName, boundary);
                             }
                         } else {
                         	OtherBounds.logInfo("Player ("+playerName+") is inside boundary ("+boundary.name+"), setting player as 'safe'.", Verbosity.HIGHEST);
                     		playerInSafeZone = true; // we're inside a boundary so "safe"
                         	if (OtherBounds.boundaryList.contains(playerName, boundary)) {
                         		OtherBounds.boundaryList.remove(playerName, boundary);
                         		player.sendMessage(boundary.safeMessage);
                         	}
                         }
                     }
                 }
 
                 Effects effects = new Effects();
                 // deal the applicable damage for this player
                 if (playerInSafeZone && OtherBoundsConfig.safeInsideBoundary) {
                 	OtherBounds.logInfo("Player is in safe zone.", Verbosity.EXTREME);
                 } else {
                     // only deal damage if the player is not inside a normal boundary
                     effects.damagePerCheck = boundaryDamage;
                 	OtherBounds.logInfo("Player not in safe zone.", Verbosity.EXTREME);
                 }
                 // inverted damage is done even if inside a normal boundary
                 effects.invertedDamagePerCheck = invertedBoundaryDamage;
 
             	OtherBounds.logInfo("Adding to damage list for "+playerName+", damage: "+effects.damagePerCheck+" invertedDamage: "+effects.invertedDamagePerCheck, Verbosity.EXTREME);
                 OtherBounds.damageList.put(player, effects);
             }
         }
     }
 
 	private boolean excepted(Player player, Boundary boundary) {
 		if (hasException(player, boundary) || hasExceptionPermissions(player, boundary)) {
 			OtherBounds.logInfo("Excepted...", Verbosity.EXTREME);
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
