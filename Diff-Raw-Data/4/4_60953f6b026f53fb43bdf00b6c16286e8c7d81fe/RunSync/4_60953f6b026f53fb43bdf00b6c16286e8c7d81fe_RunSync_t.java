 package com.gmail.zariust.otherbounds;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.entity.Player;
 
 import com.gmail.zariust.otherbounds.common.Verbosity;
 
 class RunSync implements Runnable {
 
     public void run() {
     	Main.logInfo("Sync run...", Verbosity.HIGHEST);
     	List<Player> removeList = new ArrayList<Player>();
     	
         // Check if player in list
     	for (Player player : Main.damageList.keySet()) {
     		if (!player.isOnline()) {
     			removeList.add(player);
     			continue;
     		}
     		Effects effects = Main.damageList.get(player);
     		int damage = effects.damagePerCheck + effects.invertedDamagePerCheck;
     		if (damage > 0) { 
    			Main.logInfo("Damaging player ("+player.getName()+") for "+damage+" damage.", Verbosity.HIGHEST);
     			player.damage(damage);
     		} else if (damage < 0) {
     			player.setHealth(player.getHealth()+damage);
     		}
     	}
     	
     	for (Player player : removeList) {
     		Main.damageList.remove(player);
     	}
         // damage player
 
         // update any effects
     }
 }
