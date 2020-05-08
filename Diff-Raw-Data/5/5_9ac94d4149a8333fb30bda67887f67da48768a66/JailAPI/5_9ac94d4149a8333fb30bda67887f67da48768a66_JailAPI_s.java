 package com.tyzoid.jailr.api;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.tyzoid.jailr.JailrPlugin;
 import com.tyzoid.jailr.models.Meta;
 import com.tyzoid.jailr.models.Prisoner;
 import com.tyzoid.jailr.serialization.LocationSerializer;
 import com.tyzoid.jailr.util.Log;
 import com.tyzoid.jailr.util.Messenger;
 import com.tyzoid.jailr.util.Time;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 /**
  * API methods for accessing functions in jailr. These API methods
  * can be used both internally and externally.
  *
  * @author Sushi
  */
 public class JailAPI {
     /**
      * Set the location that is used for the jail point (the location
      * where players are teleported after being jailed)
      *
      * @param jailPoint The location that is to be used for the jail
      *                  point.
      */
     public static void setJailPoint(Location jailPoint) {
         LocationSerializer locationSerializer = new LocationSerializer(jailPoint);
 
         Meta.removeWhere("key='jailPoint'");
         Meta jailPointModel = new Meta("jailPoint", locationSerializer.getString());
         jailPointModel.save();
     }
 
     /**
      * Set the location that is used for the unjail point (the location
      * where players are teleported after being unjailed)
      *
      * @param unJailPoint The location that is to be used for the unjail
      *                    point.
      */
     public static void setUnJailPoint(Location unJailPoint) {
         LocationSerializer locationSerializer = new LocationSerializer(unJailPoint);
 
         Meta.removeWhere("key='unJailPoint'");
         Meta unJailPointModel = new Meta("unJailPoint", locationSerializer.getString());
         unJailPointModel.save();
     }
 
     /**
      * Get the location that is used for the jail point (the location
      * where players are teleported after being jailed)
      *
      * @return The location that is to be used for the jail
      * point.
      */
     public static Location getJailPoint() {
         if (!isJailPointSet()) {
             Location makeshiftLocation = new Location(Bukkit.getWorlds().get(0), 0, 0, 0, 0, 0);
             Log.warning(String.format("It appears that a jail point is not set, so I am going to use the location %s instead.", makeshiftLocation.toString()));
 
             return makeshiftLocation;
         }
 
         return (new LocationSerializer(Meta.selectWhere("key='jailPoint'").get(0).getValue())).getLocation();
     }
 
     /**
      * Get the location that is used for the unjail point (the location
      * where players are teleported after being unjailed)
      *
      * @return The location that is to be used for the unjail
      * point.
      */
     public static Location getUnJailPoint() {
         if (!isUnJailPointSet()) {
             Location makeshiftLocation = new Location(Bukkit.getWorlds().get(0), 0, 0, 0, 0, 0);
             Log.warning(String.format("It appears that an unjail point is not set, so I am going to use the location %s instead.", makeshiftLocation.toString()));
 
             return makeshiftLocation;
         }
 
         return (new LocationSerializer(Meta.selectWhere("key='unJailPoint'").get(0).getValue())).getLocation();
     }
 
     /**
      * Get a boolean describing whether the jail point is currently
      * set.
      *
      * @return A boolean describing whether the jail point is currently
      * set or not.
      */
     public static boolean isJailPointSet() {
         List<Meta> matches = Meta.selectWhere("key='jailPoint'");
         return matches.size() > 0;
     }
 
     /**
      * Get a boolean describing whether the unjail point is currently
      * set.
      *
      * @return A boolean describing whether the unjail point is currently
      * set or not.
      */
     public static boolean isUnJailPointSet() {
         List<Meta> matches = Meta.selectWhere("key='unJailPoint'");
         return matches.size() > 0;
     }
 
     /**
      * Get a boolean describing whether the player is currently
      * quarantined. Quarantine status means that the player can move,
      * but the player can't interact with the world (open chests,
      * break blocks, etc).
      *
      * This method will return false if a player is offline and not
      * a prisoner, whether the player has the permission jailr.quarantined
      * or not.
      *
      * @param player The username of the Player that you want to check
      *               the status of.
      * @return A boolean describing whether Player "player" is quarantined
      * or not.
      */
     public static boolean isQuarantined(String player) {
         if (Bukkit.getPlayer(player) == null) {
             return isJailed(player) && JailrPlugin.getPlugin().getConfig().getBoolean("auto-quarantine");
         } else {
             Player playerObject = Bukkit.getPlayer(player);
             return playerObject.hasPermission("jailr.quarantine") || isJailed(player) && JailrPlugin.getPlugin().getConfig().getBoolean("auto-quarantine");
         }
     }
 
     /**
      * Get a boolean describing whether the player is currently
      * frozen. Frozen status means that the player can look around,
      * but can't move.
      *
      * This method will return false if a player is offline and not
      * a prisoner, whether the player has the permission jailr.frozen
      * or not
      *
      * @param player The username of the Player that you want to check
      *               the status of.
      * @return A boolean describing whether Player "player" is frozen
      * or not.
      */
     public static boolean isFrozen(String player) {
         if (Bukkit.getPlayer(player) == null) {
        	if(isJailed(player)) {
             	return true;
             }
         	return false;
         } else {
             Player playerObject = Bukkit.getPlayer(player);
             if(playerObject.hasPermission("jailr.frozen")) {
             	return true;
             }
            if(isJailed(player)) {
             	return true;
             }
             return false;
         }
     }
 
     //TODO Implement jailPlayer | this is called after isJailed() is called and it is returned false | This jails a player
     public static void jailPlayer(String name, String jailr, String reason, String usergroup, String inventory) {
     	Prisoner jailPlayer = new Prisoner(Time.getTime(), name, 0, 0, reason != null ? reason : "No reason specified", jailr, "usergroup", "inventory");
         jailPlayer.save();
         if(Bukkit.getServer().getPlayer(name) != null) {
         	Player player = Bukkit.getServer().getPlayer(name);
         	player.teleport(JailAPI.getJailPoint());
         }
     }
 
     //TODO Implement unjailPlayer | this is called after isJailed() is called and it is returned true | This unjails a player
     public static void unjailPlayer(String name) {
     	Prisoner.removeWhere("player='"+name+"'");
     	Player player = Bukkit.getServer().getPlayer(name);
         player.teleport(JailAPI.getUnJailPoint());
     }
     
     public static String formatArgs(String[] args) {
     	StringBuilder sb = new StringBuilder();
     	for (int i = 2; i < args.length; i++){
     		sb.append(args[i]).append(" ");
     	}
     	return sb.toString().trim();
     }
 
     /**
      * Get a boolean describing whether the player is currently
      * jailed.
      *
      * @param player The username of the Player that you want to
      *               perform the jail check on.
      * @return A boolean describing whether Player "player" is
      * currently jailed or not.
      */
     public static boolean isJailed(String player) {
     	List<Prisoner> matches = Prisoner.selectAll();
 	    for(Prisoner pri : matches) {
 	    	if(pri.getPlayer() != null) {
 	    		if(pri.getPlayer().equalsIgnoreCase(player)) {
 		    		return true;
 		    	}
 	    	}
 	    }
         return false;
     }
 
     // TODO Implement getRemainingJailTime | this is called after isJailed() is called and it is returned true |void needs to be changed to appropriate variable
     public static void getRemainingJailTime() {
     	
     }
 
     //TODO Implement getJailMates | Returns a list of all jailed players by name to list them on comamnd
     public static ArrayList<String> getJailMates() {
     	ArrayList<String> d = new ArrayList<String>();
     	ArrayList<Prisoner> pri = (ArrayList<Prisoner>)Prisoner.selectAll();
     	for(Prisoner prisoner : pri) {
     		d.add(prisoner.getPlayer());
     	}
     	return d;
     }
 }
