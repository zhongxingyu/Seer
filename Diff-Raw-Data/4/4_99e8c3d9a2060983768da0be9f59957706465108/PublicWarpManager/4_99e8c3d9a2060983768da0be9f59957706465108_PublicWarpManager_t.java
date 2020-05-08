 package com.mrz.dyndns.server.warpsuite.managers;
 
 import org.bukkit.entity.Player;
 
 import com.mrz.dyndns.server.warpsuite.WarpSuite;
 import com.mrz.dyndns.server.warpsuite.permissions.Permissions;
 import com.mrz.dyndns.server.warpsuite.players.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.util.MyConfig;
 
 public class PublicWarpManager extends WarpManager
 {
 	public PublicWarpManager(WarpSuite plugin)
 	{
 		super(new MyConfig("public", plugin));
 	}
 	
 	public boolean checkPlayer(WarpSuitePlayer player, String warpName)
 	{
 		if(warpIsSet(warpName) == false)
 		{
 			return false;
 		}
 		
 		Player p = player.getPlayer();
 		return p.hasPermission(Permissions.PUBLIC_BASE.getNode() + warpName) 
				|| p.hasPermission(Permissions.PUBLIC_BASE.getNode() + "*")
				|| p.hasPermission(Permissions.WARP_BASE.getNode() + "*")
 				|| p.hasPermission("*");
 	}
 }
