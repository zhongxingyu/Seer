 package com.martinbrook.tesseractuhc.notification;
 
import org.bukkit.ChatColor;

 import com.martinbrook.tesseractuhc.PlayerTarget;
 import com.martinbrook.tesseractuhc.UhcPlayer;
 
 public class ProximityNotification extends UhcNotification {
 
 	private UhcPlayer player;
 	private PlayerTarget target;
 	
 
 
 	public ProximityNotification(UhcPlayer player, PlayerTarget target) {
 		super();
 		this.player = player;
 		this.target = target;
 	}
 
 
 
 	@Override
 	public String formatForPlayers() {
		if (target instanceof UhcPlayer)
			return ChatColor.GOLD + player.getName() + ChatColor.GREEN + " and " + ChatColor.GOLD + target.getName() + ChatColor.GREEN + " are close together";
		else
			return ChatColor.GOLD + player.getName() + ChatColor.GREEN  + " is close to " + target.getName();
 	}
 
 }
