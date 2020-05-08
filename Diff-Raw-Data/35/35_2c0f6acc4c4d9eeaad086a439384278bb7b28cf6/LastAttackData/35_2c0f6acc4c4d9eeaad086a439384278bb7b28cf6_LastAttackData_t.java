 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.bp;
 
 import java.util.Date;
 import java.util.Hashtable;
 
 import org.bukkit.entity.Player;
 
 /**
  * @author ucchy
  * 最後に攻撃したプレイヤー情報を保存しておくクラス
  */
 public class LastAttackData {
 
     // 記録したログを、保持しておく期間。単位は秒
     private static final int EXPIRED_SECONDS = 60;
 
     private Hashtable<Player, Player> attackers;
     private Hashtable<Player, Date> attackDate;
 
     /**
      * コンストラクタ
      */
     public LastAttackData() {
         attackers = new Hashtable<Player, Player>();
         attackDate = new Hashtable<Player, Date>();
     }
 
     /**
      * 攻撃した人のログをつける
      * @param defender 攻撃を受けた人
      * @param attacker 攻撃を与えた人
      */
     public void setLastDamage(Player defender, Player attacker) {
         attackers.put(defender, attacker);
         attackDate.put(defender, new Date());
     }
 
     /**
     * 最終攻撃ログを消去する
     * @param player 消去するプレイヤー
     */
    public void removeLastDamage(Player player) {
        if ( attackers.contains(player) ) {
            attackers.remove(player);
        }
        if ( attackDate.contains(player) ) {
            attackDate.remove(player);
        }
    }

    /**
      * 最後に攻撃した人を取得する
      * @param defender 攻撃を受けた人
      * @return 最後に攻撃を与えた人
      */
     public Player getLastAttacker(Player defender) {
         if ( !attackers.containsKey(defender) ) {
             return null;
         }
         if ( ( (new Date()).getTime() - attackDate.get(defender).getTime() ) > 1000 * EXPIRED_SECONDS ) {
             return null;
         }
         return attackers.get(defender);
     }
 }
