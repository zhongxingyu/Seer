 package me.Kruithne.WolfHunt;
 
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class Tracking {
 
 	private Configuration config = null;
 	
 	Tracking (Configuration config)
 	{
 		this.config = config;;
 	}
 	
	public String trackPlayersRelativeTo(Player player)
 	{
 		Location origin = player.getLocation();
 		Iterator<Player> players = player.getWorld().getPlayers().iterator();
 		return getTrackerResult(players, origin, player);
 	}
 	
 	private String getTrackerResult(Iterator<Player> players, Location origin, Player originPlayer)
 	{
 		Hashtable<Double, Player> distances = new Hashtable<Double, Player>();
 		
 		while (players.hasNext())
 		{
 			Player checkPlayer = players.next();
 			if (canTrackPlayer(originPlayer, checkPlayer))
 			{
 				double theDistance = origin.distance(checkPlayer.getLocation());
 				if (theDistance < this.config.trackingRadius)
 					return Constants.messageNearby;
 				
 				distances.put(origin.distance(checkPlayer.getLocation()), checkPlayer);
 			}
 		}
 		
 		if (distances.size() == 0)
 			return Constants.messageNoPlayers;
 		
 		return String.format(Constants.messageDetected, this.getCompassDirection(origin, distances.get(Collections.min(distances.keySet())).getLocation()));
 	}
 	
 	private boolean canTrackPlayer(Player tracker, Player tracked)
 	{
 		if (tracker == tracked)
 			return false;
 		else if (this.config.preventTrackingOps && tracked.isOp())
 			return false;
 		return true;
 	}
 	
 	private String getCompassDirection(Location a, Location b)
 	{
 		double v = a.getX() - b.getX();
 		double h = a.getZ() - b.getZ();
 		
 		String hDir = h < 0 ? Constants.directionWest : Constants.directionEast;
 		String vDir = v < 0 ? Constants.directionSouth : Constants.directionNorth;
 		
 		double angle = Math.asin(Math.abs(v) / a.distance(b));
 		
 		if (angle <= 0.3927)
 			return hDir;
 		else if (angle >= 1.1781)
 			return vDir;
 		else
 			return vDir + "-" + hDir;
 	}
 }
