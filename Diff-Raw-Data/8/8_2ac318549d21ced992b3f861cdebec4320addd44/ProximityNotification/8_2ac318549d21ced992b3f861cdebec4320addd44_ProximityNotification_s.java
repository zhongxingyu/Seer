 package com.martinbrook.tesseractuhc.notification;
 
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
		return player.getName() + " is approaching " + target.getName();
 	}
 
 }
