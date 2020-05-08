 
 package org.s4x8.bukkit.proxykicker;
 
 import org.bukkit.entity.Player;
 
 public class Kicker implements Runnable {
	private Player player;
	private String reason;
 
	public Kicker(Player player, String reason) {
		this.player = player;
		this.reason = reason;
 	}
 
 	public void run() {
 		try {
			player.kickPlayer(reason);
 		} catch (Exception e) { };
 	}
 }
