 
 package org.s4x8.bukkit.proxykicker;
 
 import org.bukkit.entity.Player;
 
 public class Kicker implements Runnable {
	private Player player;
 
	public Kicker(Player player) {
		this.player = player;
 	}
 
 	public void run() {
 		try {
			player.kickPlayer("It is not allowed to use a proxy to connect to this server!");
 		} catch (Exception e) { };
 	}
 }
