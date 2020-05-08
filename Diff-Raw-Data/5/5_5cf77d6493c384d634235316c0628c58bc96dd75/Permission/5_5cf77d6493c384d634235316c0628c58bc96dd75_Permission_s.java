 package com.dsh105.sparktrail.util;
 
 import com.dsh105.sparktrail.trail.EffectHolder;
 import com.dsh105.sparktrail.trail.ParticleType;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 
 public enum Permission {
 
     UPDATE("sparktrail.update"),
     TRAIL("sparktrail.trail"),
     RELOAD("sparktrail.trail.reload"),
 
     PLAYER_TRAIL("sparktrail.player.trail"),
     PLAYER_INFO("sparktrail.trail.player.info"),
     PLAYER_START("sparktrail.trail.player.start"),
     PLAYER_STOP("sparktrail.trail.player.stop"),
     PLAYER_CLEAR("sparktrail.trail.player.clear"),
     PLAYER_LIST("sparktrail.trail.player.list"),
 
     LOC_TRAIL("sparktrail.trail.location"),
     LOC_START("sparktrail.trail.location.start"),
     LOC_STOP("sparktrail.trail.location.stop"),
     LOC_STOP_ALL("sparktrail.trail.location.stop.all"),
     LOC_CLEAR("sparktrail.trail.location.clear"),
     LOC_LIST("sparktrail.trail.location.list"),
 
     MOB_TRAIL("sparktrail.trail.mob"),
     MOB_START("sparktrail.trail.mob.start"),
     MOB_STOP("sparktrail.trail.mob.stop"),
     MOB_CLEAR("sparktrail.trail.mob.clear"),
     MOB_LIST("sparktrail.trail.mob.list"),
 
     SOUND("sparktrail.trail.sound"),
     TIMEOUT("sparktrail.trail.timeout"),
     CLEAR("sparktrail.trail.clear"),
     STOP("sparktrail.trail.stop"),
     START("sparktrail.trail.start"),
     DEMO("sparktrail.trail.demo"),
     INFO("sparktrail.trail.info"),;
 
     String perm;
 
     Permission(String perm) {
         this.perm = perm;
     }
 
     public boolean hasPerm(CommandSender sender, boolean sendMessage, boolean allowConsole) {
         if (sender instanceof Player) {
             return hasPerm(((Player) sender), sendMessage);
         } else {
             if (!allowConsole && sendMessage) {
                 Lang.sendTo(sender, Lang.IN_GAME_ONLY.toString());
             }
             return allowConsole;
         }
     }
 
     public boolean hasPerm(Player player, boolean sendMessage) {
         if (player.hasPermission(this.perm)) {
             return true;
         }
         if (sendMessage) {
             Lang.sendTo(player, Lang.NO_PERMISSION.toString().replace("%perm%", this.perm));
         }
         return false;
     }
 
     public static boolean hasEffectPerm(Player player, boolean sendMessage, String perm) {
         if (player.hasPermission(perm)) {
             return true;
         }
         if (sendMessage) {
             Lang.sendTo(player, Lang.NO_PERMISSION.toString().replace("%perm%", perm));
         }
         return false;
     }
 
     public static boolean hasEffectPerm(Player player, boolean sendMessage, ParticleType particleType, String effectType) {
        String perm = "sparktrail.trail" + effectType == null ? "" : "." + effectType.toLowerCase() + ".type." + particleType.toString().toLowerCase();
         return hasEffectPerm(player, sendMessage, perm);
     }
 
     public static boolean hasEffectPerm(Player player, boolean sendMessage, ParticleType particleType, String data, String effectType) {
        String perm = "sparktrail.trail" + effectType == null ? "" : "." + effectType.toString().toLowerCase() + ".type." + particleType.toString().toLowerCase();
         return hasEffectPerm(player, sendMessage, perm + "." + data);
     }
 }
