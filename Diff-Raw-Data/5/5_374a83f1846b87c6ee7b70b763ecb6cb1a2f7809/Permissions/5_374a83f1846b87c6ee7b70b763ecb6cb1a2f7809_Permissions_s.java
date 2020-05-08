 package com.mrz.dyndns.server.warpsuite.permissions;
 
 import org.bukkit.command.CommandSender;
 
 import com.mrz.dyndns.server.warpsuite.players.WarpSuitePlayer;
 import com.mrz.dyndns.server.warpsuite.util.Util;
 
 public enum Permissions
 {
 	//user
 	HELP			("warpsuite.help"),
 	WARP			("warpsuite.warp"),
 	WARP_SET		("warpsuite.warp.set"),
 	WARP_REMOVE		("warpsuite.warp.remove"),
 	WARP_LIST		("warpsuite.warp.list"),
 	
 	//admin
 	ADMIN_WARP		("warpsuite.admin.warp"),
 	ADMIN_SENDTO	("warpsuite.admin.sendto"),
 	ADMIN_TOMY		("warpsuite.admin.tomy"),
 	ADMIN_SET		("warpsuite.admin.set"),
 	ADMIN_REMOVE	("warpsuite.admin.remove"),
 	ADMIN_LIST		("warpsuite.admin.list"),
 	
 	//public
 	PUBLIC_WARP		("warpsuite.public.warp"),
 	PUBLIC_SET		("warpsuite.public.set"),
 	PUBLIC_REMOVE	("warpsuite.public.remove"),
 	PUBLIC_LIST		("warpsuite.public.list"),
	PUBLIC_BASE		("warpsuite.public"),
 	
 	//misc
 	WARP_INVITE		("warpsuite.warp.invite"),
 	COUNT_INFINITE	("warpsuite.count.infinite"),
 	DELAY_BYPASS	("warpsuite.delay.bypass"),
 	RELOAD			("warpsuite.reload"),
	WARP_BASE		("warpsuite");
 	
 	
 	private Permissions(String node)
 	{
 		this.node = node;
 	}
 	
 	private final String node;
 	
 	public boolean check(WarpSuitePlayer p, boolean notify)
 	{
 		return check(p.getPlayer(), notify);
 	}
 	
 	public boolean check(CommandSender sender, boolean notify)
 	{
 		//I have the message sent here so I don't have to call Bukkit.getPlayer(..) twice each time I check and send invalid permissions message
 		boolean result = sender.hasPermission(node);
 		if(notify && result == false)
 		{
 			Util.invalidPermissions(sender);
 		}
 		return result;
 	}
 	
 	public String getNode()
 	{
 		return node;
 	}
 }
