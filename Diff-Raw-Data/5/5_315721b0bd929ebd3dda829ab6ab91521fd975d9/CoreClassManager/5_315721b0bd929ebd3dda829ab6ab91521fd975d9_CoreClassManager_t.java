 package me.limebyte.battlenight.core.managers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.managers.ClassManager;
 import me.limebyte.battlenight.api.util.PlayerClass;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.util.SimplePlayerClass;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class CoreClassManager implements ClassManager {
 
     private static final Config configFile = Config.CLASSES;
     private static final int MAX_ENCHANT = 1000;
     private static final int INV_SIZE = 36;
 
     private List<PlayerClass> classes = new ArrayList<PlayerClass>();
     private BattleNightAPI api;
 
     public CoreClassManager(BattleNightAPI api) {
         this.api = api;
         reloadClasses();
     }
 
     @Override
     public List<PlayerClass> getClasses() {
         return classes;
     }
 
     @Override
     public PlayerClass getRandomClass() {
         Random random = new Random();
         int classNum = random.nextInt(classes.size());
         return classes.get(classNum);
     }
 
     @Override
     public void loadClasses() {
         api.getMessenger().debug(Level.INFO, "Loading classes...");
         ConfigManager.reload(configFile);
         FileConfiguration config = ConfigManager.get(configFile);
         for (String className : config.getConfigurationSection("classes").getKeys(false)) {
             String items = "classes." + className + ".items";
             String armour = "classes." + className + ".armour";
             String effects = config.getString("classes." + className + ".effects");
             classes.add(new SimplePlayerClass(className, parseItems(config, items), parseArmour(config, armour), parseEffects(effects)));
         }
     }
     
     @Override
     public void saveClasses() {
         api.getMessenger().debug(Level.INFO, "Saving classes...");
         FileConfiguration config = ConfigManager.get(configFile);
         for (PlayerClass c : classes) {
             createItems(config, "classes." + c.getName() + ".items", c.getItems(), false);
             createItems(config, "classes." + c.getName() + ".armour", c.getArmour(), true);
             createEffects(config, "classes." + c.getName() + ".effects", c.getEffects());
         }
         ConfigManager.save(configFile);
     }
 
     private List<ItemStack> parseItems(FileConfiguration config, String path) {
         List<ItemStack> items = new ArrayList<ItemStack>();
         
         for (int i = 0; i < INV_SIZE; i++) {
             items.add(new ItemStack(Material.AIR, 1));
         }
         
         ConfigurationSection section = config.getConfigurationSection(path);
         if (section == null) return items;
         Set<String> slots = section.getKeys(false);
         
         for (String slot : slots) {
            parseItem(config, path + "." + slot, items, slot);
         }
         
         return items;
     }
     
     private List<ItemStack> parseArmour(FileConfiguration config, String path) {
         List<ItemStack> armour = new ArrayList<ItemStack>();
         
         for (int i = 0; i < 4; i++) {
             armour.add(new ItemStack(Material.AIR, 1));
         }
         
         ConfigurationSection section = config.getConfigurationSection(path);
         if (section == null) return armour;
         Set<String> slots = section.getKeys(false);
         
         for (String slot : slots) {
             String newSlot = slot;
             if (slot.equalsIgnoreCase("helmet")) newSlot = "slot0";
             if (slot.equalsIgnoreCase("chestplate")) newSlot = "slot1";
             if (slot.equalsIgnoreCase("leggings")) newSlot = "slot2";
             if (slot.equalsIgnoreCase("boots")) newSlot = "slot3";
             
            parseItem(config, path + "." + newSlot, armour, newSlot);
         }
 
         return armour;
     }
     
     private void parseItem(FileConfiguration config, String path, List<ItemStack> items, String slot) {
             String type = config.getString(path + ".type");
             short data = (short) config.getInt(path + ".data");
             int amount = config.getInt(path + ".amount", 1);
             String enchantments = config.getString(path + ".enchantments");
             String name = config.getString(path + ".name");
             String lore = config.getString(path + ".lore");
             
             api.getMessenger().log(Level.INFO, "Path is: " + path);
             api.getMessenger().log(Level.INFO, "Slot is: " + slot);
             
             int slotId;
             Material mat;
             Map<Enchantment, Integer> encs = new HashMap<Enchantment, Integer>();
             List<String> lre = new ArrayList<String>();
             
             try {
                 slotId = Integer.parseInt(slot.replace("slot", "").trim());
             } catch(NumberFormatException ex) {
                 return;
             }
             
             if (slotId < 0 || slotId > INV_SIZE - 1) return;
             api.getMessenger().log(Level.INFO, "In range");
             if (type == null) return;
             api.getMessenger().log(Level.INFO, "Type is:" + type);
             mat = Material.getMaterial(type.toUpperCase());
             if (mat == null) return;
             
             api.getMessenger().log(Level.INFO, "Got: " + mat.toString());
             
             items.get(slotId).setType(mat);
             items.get(slotId).setAmount(amount);
             items.get(slotId).setDurability(data);
             
             if (enchantments != null) {
                 String[] enchantment = enchantments.split(", ");
 
                 for (String s : enchantment) {
                     String[] splitEnchantment = s.split("~");
                     int e = -1;
                     int lvl = 1;
                     Enchantment enc;
 
                     if (splitEnchantment.length == 0) continue;
 
                     try {
                         e = Integer.parseInt(splitEnchantment[0]);
                     } catch (NumberFormatException ex) {
                         continue;
                     }
 
                     enc = Enchantment.getById(e);
 
                     if (enc == null) continue;
 
                     if (splitEnchantment.length == 2) {
                         try {
                             lvl = Integer.parseInt(splitEnchantment[1]);
                         } catch (NumberFormatException ex) {
                         }
                     } else {
                         lvl = enc.getStartLevel();
                     }
 
                     encs.put(enc, lvl > MAX_ENCHANT ? MAX_ENCHANT : lvl);
                 }
             }
             
             if (lore != null) {
                 String[] lores = lore.split(", ");
                 for (String s : lores) {
                     lre.add(s);
                 }
             }
             
             if (!encs.isEmpty()) {
                 try {
                     items.get(slotId).addUnsafeEnchantments(encs);
                 } catch (Exception ex) {
                 }
             }
             
             ItemMeta meta = items.get(slotId).getItemMeta();
             if (name != null) meta.setDisplayName(name);
             if (!lre.isEmpty()) meta.setLore(lre);
             items.get(slotId).setItemMeta(meta);
     }
 
     private static List<PotionEffect> parseEffects(String effects) {
         List<PotionEffect> parsedEffects = new ArrayList<PotionEffect>();
         
         if (effects != null) {
             String[] split = effects.split(", ");
             for (String s : split) {
                 String[] effLvl = s.split("~");
                 PotionEffectType type = PotionEffectType.getByName(effLvl[0].toUpperCase());
                 if (type == null) {
                     type = PotionEffectType.getById(Integer.parseInt(effLvl[0]));
                     if (type == null) continue;
                 }
                 int level = 1;
                 if (effLvl.length > 1) level = Integer.parseInt(effLvl[1]);
                 parsedEffects.add(new PotionEffect(type, Integer.MAX_VALUE, level, true));
             }
         }
         
         return parsedEffects;
     }
 
     private static void createItems(FileConfiguration config, String path, List<ItemStack> items, boolean armour) {
         for (int i = 0; i < items.size(); i++) {
             ItemStack item = items.get(i);
             
             String slot = ".slot" + i;
             
             if (armour) {
                 slot = slot.replace("slot0", "helmet");
                 slot = slot.replace("slot1", "chestplate");
                 slot = slot.replace("slot2", "leggings");
                 slot = slot.replace("slot3", "boots");
             }
             
             if (item.getType() == Material.AIR) continue;
             String type = item.getType().toString().toLowerCase();
             short data = item.getDurability();
             int amount = item.getAmount();
             Map<Enchantment, Integer> enchantments = item.getEnchantments();
             
             config.set(path + slot + ".type", type);
             if (data != 0) config.set(path + slot + ".data", data);
             if (amount > 1) config.set(path + slot + ".amount", amount);
 
             if (!enchantments.isEmpty()) {
                 String rawEnchantments = "";
                 for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                     rawEnchantments += ", " + enchantment.getKey().getId() + "~" + enchantment.getValue();
                 }
                 config.set(path + slot + ".enchantments", rawEnchantments.substring(2));
             }
             
             if (item.hasItemMeta()) {
                 ItemMeta meta = item.getItemMeta();
                 if (meta.hasDisplayName()) config.set(path + slot + ".name", meta.getDisplayName());
                 
                 if (meta.hasLore()) {
                     String lore = "";
                     for (String loreItem : meta.getLore()) {
                         lore += ", " + loreItem;
                     }
                     config.set(path + slot + ".lore", lore.substring(2));
                 }
             }
         }
     }
 
     private void createEffects(FileConfiguration config, String path, List<PotionEffect> effects) {
         if (effects.isEmpty()) return;
         
         String effectList = "";
         for (PotionEffect effect : effects) {
             effectList += ", " + effect.getType().getName().toLowerCase();
             int level = effect.getAmplifier();
             if (level > 1) effectList += "~" + level;
         }
         config.set(path, effectList.substring(2));
     }
 
     @Override
     public void reloadClasses() {
         loadClasses();
         saveClasses();
     }
 }
