 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.config;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.Color;
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.LeatherArmorMeta;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.Utility;
 import com.github.ucchyocean.ct.item.CustomItem;
 
 /**
  * クラスデータ
  * @author ucchy
  */
 public class ClassData {
 
     /** キルポイント用のメタデータ名 */
     public static final String KILL_POINT_NAME = "ColorTeamingKillPoint";
     /** デスポイント用のメタデータ名 */
     public static final String DEATH_POINT_NAME = "ColorTeamingDeathPoint";
     
     /** ポイントの無効値を示すための数値 */
     private static final int DISABLE_POINT = -99999;
     
     /** カスタムアイテム用のダミーアイテム */
     private static final Material DUMMY_ITEM = Material.BED_BLOCK;
     
     /** タイトル */
     private String title;
     /** 説明 */
     private List<String> description;
     /** アイテムデータ */
     private List<ItemStack> items;
     /** 防具データ */
     private List<ItemStack> armors;
     /** 体力の最大値 */
     private double health;
     /** エフェクトデータ */
     private List<PotionEffect> effects;
     /** 経験値 */
     private int experience;
     /** 経験値（レベル） */
     private int level;
     /** キルしたときの得点 */
     private int killPoint;
     /** デスしたときの得点 */
     private int deathPoint;
     
     /**
      * コンストラクタ
      */
     public ClassData() {
         this.title = "";
         this.description = null;
         this.items = null;
         this.armors = null;
         this.health = -1;
         this.effects = null;
         this.experience = -1;
         this.level = -1;
         this.killPoint = DISABLE_POINT;
         this.deathPoint = DISABLE_POINT;
     }
 
     /**
      * @return title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @return description
      */
     public List<String> getDescription() {
         return description;
     }
 
     /**
      * @return items
      */
     public List<ItemStack> getItems() {
         return items;
     }
 
     /**
      * @return armor
      */
     public List<ItemStack> getArmors() {
         return armors;
     }
 
     /**
      * @return health
      */
     public double getHealth() {
         return health;
     }
 
     /**
      * @return effects
      */
     public List<PotionEffect> getEffects() {
         return effects;
     }
 
     /**
      * @return experience
      */
     public int getExperience() {
         return experience;
     }
 
     /**
      * @return level
      */
     public int getLevel() {
         return level;
     }
 
     /**
      * @return killPoint
      */
     public int getKillPoint() {
         return killPoint;
     }
 
     /**
      * @return deathPoint
      */
     public int getDeathPoint() {
         return deathPoint;
     }
 
     /**
      * @param title set title
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * @param description set description
      */
     public void setDescription(List<String> description) {
         this.description = description;
     }
 
     /**
      * @param items set items
      */
     public void setItems(List<ItemStack> items) {
         this.items = items;
     }
 
     /**
      * @param armor set armor
      */
     public void setArmors(List<ItemStack> armors) {
         this.armors = armors;
     }
 
     /**
      * @param health set health
      */
     public void setHealth(double health) {
         this.health = health;
     }
 
     /**
      * @param effects set effects
      */
     public void setEffects(List<PotionEffect> effects) {
         this.effects = effects;
     }
 
     /**
      * @param experience set experience
      */
     public void setExperience(int experience) {
         this.experience = experience;
     }
 
     /**
      * @param level set level
      */
     public void setLevel(int level) {
         this.level = level;
     }
 
     /**
      * @param killPoint set killPoint
      */
     public void setKillPoint(int killPoint) {
         this.killPoint = killPoint;
     }
 
     /**
      * @param deathPoint set deathPoint
      */
     public void setDeathPoint(int deathPoint) {
         this.deathPoint = deathPoint;
     }
     
     /**
      * 全てのクラスデータファイルを、指定されたフォルダからロードします。
      * @param dir フォルダ
      * @return 全てのロードされたクラスデータ
      */
     public static HashMap<String, ClassData> loadAllClasses(File dir) {
         
         HashMap<String, ClassData> map = new HashMap<String, ClassData>();
         
         if ( dir == null || !dir.exists() ) {
             return map;
         }
         
         File[] files = dir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".yml");
             }
         });
         
         for ( File file : files ) {
             ClassData cd = loadFromConfigFile(file);
             if ( cd != null ) {
                 String name = cd.getTitle();
                 map.put(name, cd);
             }
         }
         
         return map;
     }
     
     /**
      * コンフィグファイルから、クラスデータをロードします。
      * @param file コンフィグファイル
      * @return クラスデータ
      */
     public static ClassData loadFromConfigFile(File file) {
         
         YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
         ClassData cd = loadFromConfig(config);
         
         if ( cd.getTitle() == null || cd.getTitle().equals("") ) {
             String title = file.getName().substring(0, file.getName().length() - 4);
             cd.setTitle(title);
         }
         
         return cd;
     }
     
     /**
      * コンフィグファイルから、クラスデータをロードします。
      * @param config コンフィグファイル
      * @return クラスデータ
      */
     private static ClassData loadFromConfig(YamlConfiguration config) {
         
         ClassData cd = new ClassData();
         
         cd.setTitle(config.getString("title"));
         
         if ( config.contains("description") ) {
             cd.setDescription(config.getStringList("description"));
         }
         
         if ( config.contains("items") ) {
             ArrayList<ItemStack> items = new ArrayList<ItemStack>();
             ConfigurationSection itemsSection = 
                     config.getConfigurationSection("items");
             for ( String sub : itemsSection.getKeys(false) ) {
                 items.add(getItemFromSection(
                         itemsSection.getConfigurationSection(sub)));
             }
             cd.setItems(items);
         }
         
         if ( config.contains("armors") ) {
             ArrayList<ItemStack> armors = new ArrayList<ItemStack>();
             ConfigurationSection armorsSection = 
                     config.getConfigurationSection("armors");
             armors.add(getItemFromSection(
                     armorsSection.getConfigurationSection("boots")));
             armors.add(getItemFromSection(
                     armorsSection.getConfigurationSection("leggings")));
             armors.add(getItemFromSection(
                     armorsSection.getConfigurationSection("chestplate")));
             armors.add(getItemFromSection(
                     armorsSection.getConfigurationSection("helmet")));
             cd.setArmors(armors);
         }
         
         cd.setHealth(config.getDouble("health", -1));
         
         if ( config.contains("effects") ) {
             
             ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
             ConfigurationSection effectsSection = 
                     config.getConfigurationSection("effects");
             
             for ( String name : effectsSection.getKeys(false) ) {
                 
                 PotionEffectType type = PotionEffectType.getByName(name);
                 if ( type == null ) {
                     continue;
                 }
                 
                 int level = effectsSection.getInt(name, 1) - 1;
                 effects.add(new PotionEffect(type, 1000000, level));
             }
             
             cd.setEffects(effects);
         }
         
         if ( config.contains("experience") ) {
             String temp = config.getString("experience");
             int experience = -1;
             int level = -1;
             if ( temp.toLowerCase().endsWith("l") ) {
                 temp = temp.substring(0, temp.length() - 1); // 後ろの1文字を削る
                 if ( temp.matches("[0-9]+") ) {
                     level = Integer.parseInt(temp);
                 }
             } else {
                 experience = config.getInt("experience", -1);
             }
             cd.setExperience(experience);
             cd.setLevel(level);
         }
         
         cd.setKillPoint(config.getInt("kill_point", DISABLE_POINT));
         
         cd.setDeathPoint(config.getInt("death_point", DISABLE_POINT));
         
         return cd;
     }
     
     /**
      * コンフィグセクションから、アイテム設定を読みだして、ItemStackを生成して返します。
      * @param section コンフィグセクション
      * @return ItemStack
      */
     private static ItemStack getItemFromSection(ConfigurationSection section) {
         
         if ( section == null ) {
             return null;
         }
         
         ItemStack item = null;
         
         if ( section.contains("material") ) {
             // 通常のアイテム設定
             
            Material material = Material.getMaterial(section.getString("material"));
             if ( material == null || material == Material.AIR ) {
                 return null;
             }
             
             // データ値は、ここで設定する（たぶん、将来サポートされなくなるので注意）
             short data = (short)section.getInt("data", 0);
             item = new ItemStack(material, 1, data);
             
         } else if ( section.contains("custom_item") ) {
             // カスタムアイテムの設定
             
             String name = section.getString("custom_item");
             item = new ItemStack(DUMMY_ITEM);
             ItemMeta meta = item.getItemMeta();
             meta.setDisplayName(name);
             item.setItemMeta(meta);
             
             return item;
         }
         
         if ( item == null ) {
             return null;
         }
         
         // アイテムの個数
         item.setAmount(section.getInt("amount", 1));
         
         // アイテムの表示名
         if ( section.contains("display_name") ) {
             String dname = section.getString("display_name");
             ItemMeta meta = item.getItemMeta();
             meta.setDisplayName(dname);
             item.setItemMeta(meta);
         }
         
         // アイテムの説明
         if ( section.contains("lore") ) {
             List<String> lore = section.getStringList("lore");
             ItemMeta meta = item.getItemMeta();
             meta.setLore(lore);
             item.setItemMeta(meta);
         }
         
         // 革防具の染色設定
         if ( item.getType() == Material.LEATHER_BOOTS ||
                 item.getType() == Material.LEATHER_LEGGINGS ||
                 item.getType() == Material.LEATHER_CHESTPLATE ||
                 item.getType() == Material.LEATHER_HELMET ) {
             
             LeatherArmorMeta lam = (LeatherArmorMeta)item.getItemMeta();
             int red = section.getInt("red", 160);
             int blue = section.getInt("blue", 101);
             int green = section.getInt("green", 64);
             Color color = Color.fromBGR(blue, green, red);
             lam.setColor(color);
             item.setItemMeta(lam);
         }
         
         // エンチャント
         if ( section.contains("enchants") ) {
             ConfigurationSection enchants_sec = section.getConfigurationSection("enchants");
             for ( String type_str : enchants_sec.getKeys(false) ) {
                 Enchantment enchant = Enchantment.getByName(type_str);
                 
                 if ( enchant != null ) {
                     int level = enchants_sec.getInt(type_str, 1);
                     if ( level < enchant.getStartLevel() ) {
                         level = enchant.getStartLevel();
                     } else if ( level > 1000 ) {
                         level = 1000;
                     }
                     item.addUnsafeEnchantment(enchant, level);
                 }
             }
         }
         
         // 消耗度
         if ( section.contains("remain") ) {
             
             short remain = (short)section.getInt("remain");
             short durability = (short)(item.getType().getMaxDurability() - remain + 1);
             if ( durability < 0 ) {
                 durability = 0;
             }
             item.setDurability(durability);
         }
         
         return item;
     }
     
     /**
      * アイテムの情報を文字列表現で返す
      * @param item アイテム
      * @return 文字列表現
      */
     public static ArrayList<String> getItemInfo(ItemStack item) {
         
         ArrayList<String> message = new ArrayList<String>();
         
         if ( item == null ) {
             return message;
         }
         
         String indent = "    ";
         
         message.add("===== アイテム情報 =====");
         message.add(indent + "material: " + item.getType());
         if ( item.getAmount() > 1 ) {
             message.add(indent + "amount: " + item.getAmount());
         }
         
         @SuppressWarnings("deprecation") // TODO
         byte data = item.getData().getData();
         if ( item.getDurability() > 0 ) {
             int remain = item.getType().getMaxDurability() - item.getDurability() + 1;
             message.add(indent + "remain: " + remain);
         } else if ( data > 0 ) {
             message.add(indent + "data: " + data);
         }
         
         if ( item.hasItemMeta() ) {
             ItemMeta meta = item.getItemMeta();
             if ( meta.hasDisplayName() ) {
                 message.add(indent + "display_name: " + meta.getDisplayName());
             }
             if ( meta.hasLore() ) {
                 message.add(indent + "lore: ");
                 for ( String l : meta.getLore() ) {
                     message.add(indent + "- '" + l + "'");
                 }
             }
         }
         
         if ( item.getEnchantments().size() > 0 ) {
             message.add(indent + "enchants: ");
             for ( Enchantment ench : item.getEnchantments().keySet() ) {
                 message.add(indent + "  " + ench.getName() + ": " + 
                         item.getEnchantmentLevel(ench));
             }
         }
         
         if ( item.getType() == Material.LEATHER_BOOTS ||
                 item.getType() == Material.LEATHER_LEGGINGS ||
                 item.getType() == Material.LEATHER_CHESTPLATE ||
                 item.getType() == Material.LEATHER_HELMET ) {
             
             LeatherArmorMeta lam = (LeatherArmorMeta)item.getItemMeta();
             message.add(indent + "red: " + lam.getColor().getRed());
             message.add(indent + "blue: " + lam.getColor().getBlue());
             message.add(indent + "green: " + lam.getColor().getGreen());
         }
         
         return message;
     }
 
     /**
      * 指定されたプレイヤーに指定されたクラスを設定する
      * @param player プレイヤー
      * @return クラス設定を実行したかどうか。<br/>
      * 指定されたプレイヤーがオフラインの場合は、falseになる。
      */
     public boolean setClassToPlayer(Player player) {
         
         // 設定対象が居ない場合は falseを返す
         if ( player == null || !player.isOnline() ) {
             return false;
         }
         
         boolean needToUpdateInventory = false;
         
         // 全回復の実行
         Utility.resetPlayerStatus(player);
         
         if ( items != null ) {
         
             // インベントリの消去
             player.getInventory().clear();
             
             // アイテムの配布
             for ( ItemStack item : items ) {
                 if ( item != null ) {
                     
                     if ( item.getType() == DUMMY_ITEM ) {
                         // カスタムアイテムをあらかじめ置き換える
                         
                         if ( !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() ) {
                             continue;
                         }
                         
                         String name = item.getItemMeta().getDisplayName();
                         CustomItem citem = ColorTeaming.instance.getAPI().getCustomItem(name);
                         if ( citem == null ) {
                             continue;
                         }
                         item = citem.getItemStack().clone();
                     }
                     
                     player.getInventory().addItem(item);
                 }
             }
             
             needToUpdateInventory = true;
         }
         
         if ( armors != null ) {
             
             // 防具の消去
             player.getInventory().setHelmet(null);
             player.getInventory().setChestplate(null);
             player.getInventory().setLeggings(null);
             player.getInventory().setBoots(null);
             
             // 防具の配布
             if (armors.size() >= 1 && armors.get(0) != null ) {
                 player.getInventory().setBoots(armors.get(0));
             }
             if (armors.size() >= 2 && armors.get(1) != null ) {
                 player.getInventory().setLeggings(armors.get(1));
             }
             if (armors.size() >= 3 && armors.get(2) != null ) {
                 player.getInventory().setChestplate(armors.get(2));
             }
             if (armors.size() >= 4 && armors.get(3) != null ) {
                 player.getInventory().setHelmet(armors.get(3));
             }
             
             needToUpdateInventory = true;
         }
         
         // インベントリ更新
         if ( needToUpdateInventory ) {
             updateInventory(player);
         }
         
         // 体力最大値の設定
         if ( health != -1 ) {
             player.setMaxHealth(health);
             player.setHealth(health);
         }
         
         // ポーション効果の設定
         if ( effects != null ) {
             player.addPotionEffects(effects);
         }
         
         // 経験値の設定
         if ( experience != -1 ) {
             player.setTotalExperience(experience);
             Utility.updateExp(player);
         } else if ( level != -1 ) {
             player.setTotalExperience(0);
             Utility.updateExp(player);
             player.setLevel(level);
         }
         
         // メタデータの設定
         if ( killPoint != DISABLE_POINT ) {
             player.setMetadata(KILL_POINT_NAME, 
                     new FixedMetadataValue(ColorTeaming.instance, killPoint));
         }
         if ( deathPoint != DISABLE_POINT ) {
             player.setMetadata(DEATH_POINT_NAME, 
                     new FixedMetadataValue(ColorTeaming.instance, deathPoint));
         }
         
         return true;
     }
     
     /**
      * プレイヤーのインベントリを更新します。
      * @param player 
      */
     @SuppressWarnings("deprecation")
     private void updateInventory(Player player) {
         player.updateInventory();
     }
 }
