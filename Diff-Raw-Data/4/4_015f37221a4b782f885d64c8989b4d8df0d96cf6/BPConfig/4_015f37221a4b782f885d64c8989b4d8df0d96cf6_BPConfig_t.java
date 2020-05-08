 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.bp;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 
 /**
  * @author ucchy
  * BattlePointsの設定ファイルを管理するクラス
  */
 public class BPConfig {
 
     public static int initialPoint;
     public static int winBasePoint;
     public static int winBonusPointPercent;
     public static Hashtable<String, String> rankSymbols;
     public static Hashtable<String, ChatColor> rankColors;
     public static ArrayList<Entry<String, Integer>> rankBorders;
 
     /**
      * config.ymlの読み出し処理。
      */
     public static void reloadConfig() {
 
         // フォルダやファイルがない場合は、作成したりする
         File dir = new File(
                 BattlePoints.instance.getDataFolder().getAbsolutePath());
         if ( !dir.exists() ) {
             dir.mkdirs();
         }
 
         File file = new File(
                 BattlePoints.instance.getDataFolder() +
                 File.separator + "config.yml");
 
         if ( !file.exists() ) {
             Utility.copyFileFromJar(BattlePoints.getPluginJarFile(),
                     file, "config.yml", false);
         }
 
         // 再読み込み処理
         BattlePoints.instance.reloadConfig();
         FileConfiguration config = BattlePoints.instance.getConfig();
 
         // 各コンフィグの取得
         initialPoint = config.getInt("initialPoint", 1500);
         winBasePoint = config.getInt("winBasePoint", 16);
         winBonusPointPercent = config.getInt("winBonusPointPercent", 4);
 
         rankSymbols = new Hashtable<String, String>();
         rankColors = new Hashtable<String, ChatColor>();
         Hashtable<String, Integer> rankBorders_temp = new Hashtable<String, Integer>();
         ConfigurationSection section = config.getConfigurationSection("ranks");
         if ( section != null ) {
             Iterator<String> i = section.getValues(false).keySet().iterator();
             while (i.hasNext()) {
                 String rankName = i.next();
                 if ( config.contains("ranks." + rankName + ".symbol") ) {
                     rankSymbols.put(rankName, config.getString("ranks." + rankName + ".symbol") );
                 } else {
                     rankSymbols.put(rankName, rankName.substring(0, 1));
                 }
                 String color = config.getString("ranks." + rankName + ".color", "white");
                 rankColors.put(rankName, Utility.replaceColors(color));
                 if ( config.contains("ranks." + rankName + ".border") ) {
                     rankBorders_temp.put(rankName, config.getInt("ranks." + rankName + ".border") );
                 }
             }
         }
 
         // もしrankが正しくロードされていなければ、ここで初期値を設定する
         if ( rankBorders_temp.size() <= 0 ) {
             rankSymbols = new Hashtable<String, String>();
             rankSymbols.put("novice", "N");
             rankSymbols.put("bronze", "B");
             rankSymbols.put("silver", "S");
             rankSymbols.put("gold", "G");
             rankSymbols.put("platinum", "P");
             rankColors = new Hashtable<String, ChatColor>();
             rankColors.put("novice", ChatColor.BLUE);
             rankColors.put("bronze", ChatColor.AQUA);
             rankColors.put("silver", ChatColor.GOLD);
             rankColors.put("gold", ChatColor.RED);
             rankColors.put("platinum", ChatColor.LIGHT_PURPLE);
             rankBorders_temp = new Hashtable<String, Integer>();
             rankBorders_temp.put("novice", 0);
             rankBorders_temp.put("bronze", 1400);
             rankBorders_temp.put("silver", 1700);
             rankBorders_temp.put("gold", 1900);
             rankBorders_temp.put("platinum", 2200);
         }
 
         // rankBorders は、ここでソートを実行しておく
         rankBorders = new ArrayList<Entry<String,Integer>>(rankBorders_temp.entrySet());
 
         Collections.sort(rankBorders, new Comparator<Entry<String, Integer>>(){
             public int compare(Entry<String, Integer> ent1, Entry<String, Integer> ent2){
                 Integer val1 = ent1.getValue();
                 Integer val2 = ent2.getValue();
                 return val1.compareTo(val2);
             }
         });
     }
 
     /**
      * イロレーティングで、変動するポイント数を算出する。
      * @param winnerPoint 勝者の変動前ポイント
      * @param loserPoint 敗者の変動前ポイント
      * @return
      */
     public static int getEloRating(int winnerPoint, int loserPoint) {
 
        int rate = winBasePoint + (int)Math.round(
                 (double)(loserPoint - winnerPoint) * winBonusPointPercent / 100 );

         if ( rate <= 0 ) {
             rate = 1;
         }
         return rate;
     }
 
     /**
      * ポイント数から、称号名を取得する。
      * @param point ポイント
      * @return 称号名
      */
     public static String getRankFromPoint(int point) {
 
         for ( int i = rankBorders.size()-1; i>=0; i-- ) {
             int border = rankBorders.get(i).getValue();
             if ( point >= border ) {
                 return rankBorders.get(i).getKey();
             }
         }
         return "";
     }
 }
