 ﻿/*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.enchantments.EnchantmentWrapper;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 import com.github.ucchyocean.cmt.ColorMeTeamingConfig;
 
 /**
  * @author ucchy
  * colorclass(cclass)コマンドの実行クラス
  */
 public class CClassCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
     private static final String PREINFO = ChatColor.GRAY.toString();
 
     private static final String REGEX_ITEM_PATTERN = "([0-9]+)(?:@([0-9]+))?(?::([0-9]+))?|([0-9]+)((?:\\^[0-9]+-[0-9]+)+(?:@([0-9]+))?)";
     private static final String REGEX_ENCHANT_PATTERN = "\\^([0-9]+)-([0-9]+)";
 
     private static Pattern pattern;
     private static Pattern patternEnchant;
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length < 2 ) {
             return false;
         }
 
         String group = args[0];
         String clas = args[1];
 
         Hashtable<String, ArrayList<Player>> members = ColorMeTeaming.getAllColorMembers();
 
         // 有効なグループ名かユーザー名か'all'が指定されたかを確認する
         boolean isAll = false;
         boolean isGroup = false;
         if ( group.equalsIgnoreCase("all") ) {
             // 全プレイヤー指定
             isAll = true;
         } else if ( members.containsKey(group)  ) {
             // グループ指定
             isGroup = true;
         } else if ( ColorMeTeaming.getAllPlayers().contains(ColorMeTeaming.getPlayerExact(group)) ) {
             // ユーザー指定
         } else {
             sender.sendMessage(PREERR + "グループまたはプレイヤー " + group + " が存在しません。");
             return true;
         }
 
         // 有効なクラス名が指定されたか確認する
         if ( !ColorMeTeamingConfig.classItems.containsKey(clas) ) {
             sender.sendMessage(PREERR + "クラス " + clas + " が存在しません。");
             return true;
         }
 
         // クラス設定を実行する
         String items = ColorMeTeamingConfig.classItems.get(clas);
         String armor = ColorMeTeamingConfig.classArmors.get(clas);
         ArrayList<ItemStack> itemData = parseClassItemData(sender, items);
 
         ArrayList<Player> playersToSet;
         if ( isAll ) {
             playersToSet = ColorMeTeaming.getAllPlayers();
         } else if ( isGroup ) {
             playersToSet = members.get(group);
         } else {
             playersToSet = new ArrayList<Player>();
             playersToSet.add(ColorMeTeaming.getPlayerExact(group));
         }
 
         for ( Player p : playersToSet ) {
 
             // インベントリの消去
             p.getInventory().clear();
             p.getInventory().setHelmet(null);
             p.getInventory().setChestplate(null);
             p.getInventory().setLeggings(null);
             p.getInventory().setBoots(null);
 
             // アイテムの配布
             for ( ItemStack item : itemData ) {
                 if ( item != null ) {
                     p.getInventory().addItem(item);
                 }
             }
 
             // 防具の配布
             if ( armor != null ) {
                 ArrayList<ItemStack> armorData = parseClassItemData(sender, armor);
                 if (armorData.get(0) != null ) {
                     p.getInventory().setHelmet(armorData.get(0));
                 }
                 if (armorData.get(1) != null ) {
                     p.getInventory().setChestplate(armorData.get(1));
                 }
                 if (armorData.get(2) != null ) {
                     p.getInventory().setLeggings(armorData.get(2));
                 }
                 if (armorData.get(3) != null ) {
                     p.getInventory().setBoots(armorData.get(3));
                 }
             }
         }
 
         String type = "グループ";
         if ( !isGroup ) {
             type = "ユーザー";
         }
 
         sender.sendMessage(PREINFO +
                 String.format("%s %s に、%s クラスの装備とアイテムを配布しました。", type, group, clas));
 
         return true;
     }
 
 
     /**
      * Classのアイテムデータ文字列を解析し、ItemStack配列に変換する。
      * @param data 解析元の文字列　例）"44:64,44@2:64,281:10"
      * @return ItemStackの配列
      */
     private ArrayList<ItemStack> parseClassItemData(CommandSender sender, String data) {
 
         if ( pattern == null ) {
             pattern = Pattern.compile(REGEX_ITEM_PATTERN);
             patternEnchant = Pattern.compile(REGEX_ENCHANT_PATTERN);
         }
 
         ArrayList<ItemStack> items = new ArrayList<ItemStack>();
 
         String[] array = data.split("[,]");
         for (int i = 0; i < array.length; i++) {
 
             Matcher matcher = pattern.matcher(array[i]);
 
             if ( matcher.matches() ) {
 
                 if ( matcher.group(1) != null ) {
                     // group1 が null でないなら、id@damage:amount 形式の指定である。
 
                     int item = 0, amount = 1;
                     short damage = 0;
                     item = Integer.parseInt(matcher.group(1));
                     if ( matcher.group(2) != null ) {
                         damage = Short.parseShort(matcher.group(2));
                     }
                     if ( matcher.group(3) != null ) {
                         amount = Integer.parseInt(matcher.group(3));
                     }
 
                     // item id が0なら、nullを設定して次へ進む
                     if ( item == 0 ) {
                         items.add(null);
                         continue;
                     }
 
                     // Materialの取得をして、正しいIDが指定されたかどうかを確認する
                     Material m = Material.getMaterial(item);
                     if (m == null) {
                         sender.sendMessage(PREERR + "指定されたItemID " + item + " が見つかりません。");
                         return null;
                     }
 
                     // 65個を超える量の場合は、64個ごとにItemStackにする。
                     while (amount > 64) {
                         items.add(getItemStack(item, 64, damage));
                         amount -= 64;
                     }
 
                     items.add(getItemStack(item, amount, damage));
 
                 } else if ( matcher.group(4) != null ) {
                     // group4、group5 が null でないなら、ID^Ench-Level... 形式の指定である。
 
                     int item = 0;
                     short damage = 0;
                     HashMap<Integer, Integer> enchants = new HashMap<Integer, Integer>();
 
                     item = Integer.parseInt(matcher.group(4));
                     if ( matcher.group(6) != null ) {
                         damage = Short.parseShort(matcher.group(6));
                     }
 
                     // item id が0なら、nullを設定して次へ進む
                     if ( item == 0 ) {
                         items.add(null);
                         continue;
                     }
 
                     // Materialの取得をして、正しいIDが指定されたかどうかを確認する
                     Material m = Material.getMaterial(item);
                     if (m == null) {
                         sender.sendMessage(PREERR + "指定されたItemID " + item + " が見つかりません。");
                         return null;
                     }
 
                     // 指定エンチャントの解析
                     Matcher matcherEnchant = patternEnchant.matcher(matcher.group(5));
                     while ( matcherEnchant.find() ) {
                         int enchantID = Integer.parseInt(matcherEnchant.group(1));
                         int enchantLevel = Integer.parseInt(matcherEnchant.group(2));
                         enchants.put(enchantID, enchantLevel);
                     }
 
                     items.add(getEnchantedItem(item, enchants, damage));
 
                 } else {
 
                     sender.sendMessage(PREERR + "指定された形式 " + matcher.group(0) + " が正しく解析できません。");
                     return null;
                 }
             }
         }
 
         return items;
     }
 
     /**
      * ItemStackインスタンスを返す
      * @param item 配布するアイテムのID
      * @param amount 配布するアイテムの数量
      * @param damage 配布するアイテムのダメージ値（指定しない場合は0にする）
      * @return ItemStackインスタンス
      */
     private ItemStack getItemStack(int item, int amount, short damage) {
         if ( damage > 0 )
             return new ItemStack(item, amount, damage);
         else
             return new ItemStack(item, amount);
     }
 
     /**
      * エンチャント付きのItemStackインスタンスを返す
      * @param item 配布するアイテムのID
      * @param enchants 付与するエンチャントIDと、そのレベルのセット
      * @return ItemStackインスタンス
      */
     private ItemStack getEnchantedItem(int item, HashMap<Integer, Integer> enchants, short damage) {
 
         ItemStack i = getItemStack(item, 1, damage);
 
         Set<Integer> keys = enchants.keySet();
         for ( int eid : keys ) {
             int level = enchants.get(eid);
             Enchantment ench = new EnchantmentWrapper(eid);
             if ( level < ench.getStartLevel() ) {
                 level = ench.getStartLevel();
             } else if ( level > 127 ) {
                 level = 127;
             }
             i.addUnsafeEnchantment(ench, level);
         }
 
         return i;
     }
 }
