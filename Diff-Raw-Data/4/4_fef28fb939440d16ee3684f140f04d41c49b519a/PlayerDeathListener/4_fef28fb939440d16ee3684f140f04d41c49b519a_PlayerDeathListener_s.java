 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 import com.github.ucchyocean.cmt.ColorMeTeamingConfig;
 import com.github.ucchyocean.cmt.Utility;
 
 /**
  * @author ucchy
  * プレイヤーが死亡したときに、通知を受け取って処理するクラス
  */
 public class PlayerDeathListener implements Listener {
 
     private static final String PRENOTICE = ChatColor.LIGHT_PURPLE.toString();
 
     /**
      * Playerが死亡したときに発生するイベント
      * @param event
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event) {
 
         Player player = event.getEntity();
         String color = ColorMeTeaming.getPlayerColor(player);
 
         // DeathMessageのプレイヤー名を、色つきで置き換え
         if ( ColorMeTeamingConfig.coloringDeathMessage ) {
             event.setDeathMessage( event.getDeathMessage().replace(
                     player.getName(),
                     Utility.replaceColors(color) + player.getName() + ChatColor.RESET));
         }
 
         // Death数を加算
         if ( !ColorMeTeamingConfig.ignoreGroups.contains(color) ) {
             // グループへ加算
             if ( !ColorMeTeaming.killDeathCounts.containsKey(color) ) {
                 ColorMeTeaming.killDeathCounts.put(color, new int[3]);
             }
             ColorMeTeaming.killDeathCounts.get(color)[1]++;
             // ユーザーへ加算
             if ( !ColorMeTeaming.killDeathUserCounts.containsKey(player.getName()) ) {
                 ColorMeTeaming.killDeathUserCounts.put(player.getName(), new int[3]);
             }
             ColorMeTeaming.killDeathUserCounts.get(player.getName())[1]++;
         }
 
         // 死亡したプレイヤーが、大将だった場合、倒されたことを全体に通知する。
         if ( ColorMeTeaming.leaders.containsKey(color) &&
                 ColorMeTeaming.leaders.get(color).contains(player.getName()) ) {
             String message = String.format(PRENOTICE + "%s チームの大将、%s が倒されました！",
                     color, player.getName());
             ColorMeTeaming.sendBroadcast(message);
             ColorMeTeaming.leaders.get(color).remove(player.getName());
             if ( ColorMeTeaming.leaders.get(color).size() <= 0 ) {
                 ColorMeTeaming.leaders.remove(color);
             }
 
             if ( ColorMeTeaming.leaders.get(color).size() >= 1 ) {
                 message = String.format(PRENOTICE + "%s チームの残り大将は、あと %d 人です。",
                         color, ColorMeTeaming.leaders.get(color).size());
             } else {
                 message = String.format(PRENOTICE + "%s チームの大将は全滅しました！", color);
             }
             ColorMeTeaming.sendBroadcast(message);
         }
 
         // 倒したプレイヤーを取得
         // 直接攻撃で倒された場合は、killerをそのまま使う
         // 間接攻撃で倒された場合は、shooterを取得して使う
         Player killer = player.getKiller();
 
         EntityDamageEvent cause = event.getEntity().getLastDamageCause();
         if ( cause != null && cause instanceof EntityDamageByEntityEvent ) {
             Entity damager = ((EntityDamageByEntityEvent)cause).getDamager();
             if ( damager instanceof Projectile ) {
                 LivingEntity shooter = ((Projectile) damager).getShooter();
                 if ( shooter instanceof Player ) {
                     killer = (Player)shooter;
                 }
             }
         }
 
         if ( killer != null ) {
             String colorKiller = ColorMeTeaming.getPlayerColor(killer);
 
             // DeathMessageのKillerプレイヤー名を、displayMessageで置き換え
             if ( ColorMeTeamingConfig.coloringDeathMessage ) {
                 event.setDeathMessage( event.getDeathMessage().replace(
                         killer.getName(),
                         Utility.replaceColors(colorKiller) + killer.getName() + ChatColor.RESET));
             }
 
             // Kill数を加算
             if ( !ColorMeTeamingConfig.ignoreGroups.contains(colorKiller) ) {
                 // グループへ加算
                 if ( !ColorMeTeaming.killDeathCounts.containsKey(colorKiller) ) {
                     ColorMeTeaming.killDeathCounts.put(colorKiller, new int[3]);
                 }
                 if ( color.equals(colorKiller) ) // 同じグループだった場合のペナルティ
                     ColorMeTeaming.killDeathCounts.get(colorKiller)[2]++;
                 else
                     ColorMeTeaming.killDeathCounts.get(colorKiller)[0]++;
                 // ユーザーへ加算
                 if ( !ColorMeTeaming.killDeathUserCounts.containsKey(killer.getName()) ) {
                     ColorMeTeaming.killDeathUserCounts.put(killer.getName(), new int[3]);
                 }
                 if ( color.equals(colorKiller) ) // 同じグループだった場合のペナルティ
                     ColorMeTeaming.killDeathUserCounts.get(killer.getName())[2]++;
                 else
                     ColorMeTeaming.killDeathUserCounts.get(killer.getName())[0]++;
 
                 // killReachTrophyが設定されていたら、超えたかどうかを判定する
                 if ( ColorMeTeamingConfig.killReachTrophy > 0 &&
                         ColorMeTeaming.leaders.size() == 0 ) {
 
                     if ( ColorMeTeaming.killDeathCounts.get(colorKiller)[0] ==
                             ColorMeTeamingConfig.killReachTrophy ) {
                         int least = ColorMeTeamingConfig.killTrophy -
                                 ColorMeTeamingConfig.killReachTrophy;
                         String message = String.format(
                                PRENOTICE + "%s チームは、あと %d キルで %d キルです。",
                                colorKiller, least, ColorMeTeamingConfig.killTrophy);
                         ColorMeTeaming.sendBroadcast(message);
                     }
                 }
 
                 // killTrophyが設定されていたら、超えたかどうかを判定する
                 if ( ColorMeTeamingConfig.killTrophy > 0 &&
                         ColorMeTeaming.leaders.size() == 0 ) {
 
                     if ( ColorMeTeaming.killDeathCounts.get(colorKiller)[0] ==
                             ColorMeTeamingConfig.killTrophy ) {
                         String message = String.format(
                                 PRENOTICE + "%s チームは、%d キルを達成しました！",
                                 colorKiller, ColorMeTeamingConfig.killTrophy);
                         ColorMeTeaming.sendBroadcast(message);
                     }
                 }
             }
         }
 
         // 色設定を削除する
         if ( ColorMeTeamingConfig.colorRemoveOnDeath ) {
             ColorMeTeaming.removePlayerColor(player);
         }
     }
 }
