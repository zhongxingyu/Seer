 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.listener;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.ColorTeamingConfig;
 
 /**
  * @author ucchy
  * プレイヤーがリスポーンしたときに、通知を受け取って処理するクラス
  */
 public class PlayerRespawnListener implements Listener {
 
     /**
      * Playerがリスポーンしたときに発生するイベント
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerRespawn(PlayerRespawnEvent event) {
 
         Player player = event.getPlayer();
         String color = ColorTeaming.getPlayerColor(player);
 
         // リスポーンポイントを設定
         Location respawn = ColorTeaming.respawnConfig.get(color);
         if ( respawn != null ) {
             respawn = respawn.add(0.5, 0, 0.5);
             event.setRespawnLocation(respawn);
             player.setNoDamageTicks(ColorTeamingConfig.noDamageSeconds * 20);
         }
     }
 }
