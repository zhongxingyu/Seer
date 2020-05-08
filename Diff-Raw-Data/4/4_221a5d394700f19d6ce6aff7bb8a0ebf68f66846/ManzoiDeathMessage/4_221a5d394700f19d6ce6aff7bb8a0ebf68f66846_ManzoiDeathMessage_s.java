 /*
  * @author     ucchy
  * @license    GPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.mdm;
 
 import java.io.File;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author ucchy
  * manzoiサーバー用、デスメッセージカスタマイズプラグイン
  */
 public class ManzoiDeathMessage extends JavaPlugin implements Listener {
 
     private static boolean loggingDeathMessage;
     private static boolean suppressDeathMessage;
 
     /**
      * プラグイン有効時に呼び出されるメソッド
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
     @Override
     public void onEnable(){
         loadFiles();
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     /**
      * config.ymlが存在するかどうかチェックし、
      * 存在しないならデフォルトのconfig.ymlをコピーするメソッド
      */
     private void loadFiles() {
 
         // フォルダやファイルがない場合は、作成したりする
         File dir = new File(getDataFolder().getAbsolutePath());
         if ( !dir.exists() ) {
             dir.mkdirs();
         }
 
         File file = new File(getDataFolder(), "config.yml");
         if ( !file.exists() ) {
             Utility.copyFileFromJar(getFile(), file, "config.yml", false);
         }
 
         file = new File(getDataFolder(), "messages.yml");
         if ( !file.exists() ) {
             Utility.copyFileFromJar(getFile(), file, "messages.yml", false);
         }
 
         // 再読み込み処理
         reloadConfig();
 
         // 設定の取得
         FileConfiguration config = getConfig();
 
        loggingDeathMessage = config.getBoolean("loggingDeathMessage", false);
         suppressDeathMessage = config.getBoolean("suppressDeathMessage", true);
     }
 
     /**
      * デスメッセージを取得するメソッド
      * @param cause プレイヤー死亡理由
      * @return 理由に応じたメッセージ。
      */
     public String getMessage(String cause) {
 
         File file = new File(getDataFolder(), "messages.yml");
         YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
         return config.getString(cause, cause + "(%p_%k_%i_%o)");
     }
 
     /**
      * プレイヤーが死亡したときに呼び出されるメソッド
      * @param event プレイヤー死亡イベント
      */
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerDeath(PlayerDeathEvent event){
 
         // プレイヤーとプレイヤーが最後に受けたダメージイベントを取得
         Player deader = event.getEntity();
         final EntityDamageEvent cause = event.getEntity().getLastDamageCause();
 
         // 死亡メッセージ
         String deathMessage = event.getDeathMessage();
 
         // ダメージイベントを受けずに死んだ 死因不明
         if (cause == null) {
             deathMessage = getMessage("unknown"); // Unknown
         }
         // ダメージイベントあり 原因によってメッセージ変更
         else {
             // ダメージイベントがEntityDamageByEntityEvent(エンティティが原因のダメージイベント)かどうかチェック
             if (cause instanceof EntityDamageByEntityEvent) {
                 // EntityDamageByEventのgetDamagerメソッドから原因となったエンティティを取得
                 Entity killer = ((EntityDamageByEntityEvent) cause).getDamager();
 
                 // エンティティの型チェック 特殊な表示の仕方が必要
                 if (killer instanceof Player){
                     // この辺に倒したプレイヤー名取得
                     Player killerP = (Player)killer;
                     //killerが持ってたアイテム
                     ItemStack hand = killerP.getItemInHand();
                     String handItemName = hand.getType().toString();
                     if ( hand.getType().equals(Material.AIR) ) {
                         handItemName = "素手";
                     }
                     deathMessage = getMessage("pvp");
 
                     deathMessage = deathMessage.replace("%k", killerP.getName());
                     deathMessage = deathMessage.replace("%i", handItemName);
                 }
                 // 飼われている狼
                 else if (killer instanceof Wolf && ((Wolf) killer).isTamed()){
                     //  飼い主取得
                     String tamer = ((Wolf)killer).getOwner().getName();
 
                     deathMessage = getMessage("tamewolf");
                     deathMessage = deathMessage.replace("%o", tamer);
                 }
                 // プレイヤーが打った矢
                 else if (killer instanceof Arrow && ((Arrow)killer).getShooter() instanceof LivingEntity) {
                     LivingEntity shooter = ((Arrow)killer).getShooter();
                     String killerName;
                     if ( shooter instanceof Player ) {
                         killerName = ((Player)shooter).getName();
                     } else if ( shooter instanceof Skeleton ) {
                         killerName = "スケルトン";
                     } else {
                         killerName = "ディスペンサー";
                     }
 
                     deathMessage = getMessage("arrow");
                     deathMessage = deathMessage.replace("%k", killerName);
                 }
                 // プレイヤーが投げた雪玉など
                 else if (killer instanceof Projectile && ((Projectile) killer).getShooter() instanceof Player) {
                     // 投げたプレイヤー取得
                     Player sh = (Player) ((Projectile)killer).getShooter();
 
                     deathMessage = getMessage("throw");
                     deathMessage = deathMessage.replace("%k", sh.getName());
                 }
                 // そのほかのMOBは直接設定ファイルから取得
                 else {
                     // 直接 getMessage メソッドを呼ぶ
                     deathMessage = getMessage(killer.getType().getName().toLowerCase());
                    getLogger().info("else entity message!! " + killer.getType().getName());
                 }
             }
             // エンティティ以外に倒されたメッセージは別に設定
             else {
                 if (cause.getCause() == DamageCause.FIRE_TICK) {
                     deathMessage = getMessage("fire");
                 } else {
                     deathMessage = getMessage(cause.getCause().toString());
                 }
             }
         }
 
         // %p を、死亡した人の名前で置き換えする
         deathMessage = deathMessage.replace("%p", deader.getName());
 
         // カラーコードを置き換える
         deathMessage = Utility.replaceColorCode(deathMessage);
 
         if ( loggingDeathMessage ) {
             // ロギング
             getLogger().info(ChatColor.stripColor(deathMessage));
         }
 
         if ( suppressDeathMessage ) {
             // メッセージを消去して、OPにだけ送信する
             event.setDeathMessage("");
             Player[] players = getServer().getOnlinePlayers();
             for ( Player player : players ) {
                 if ( player.isOp() ) {
                     player.sendMessage(deathMessage);
                 }
             }
         } else {
             // メッセージを再設定する
             event.setDeathMessage(deathMessage);
         }
     }
 }
