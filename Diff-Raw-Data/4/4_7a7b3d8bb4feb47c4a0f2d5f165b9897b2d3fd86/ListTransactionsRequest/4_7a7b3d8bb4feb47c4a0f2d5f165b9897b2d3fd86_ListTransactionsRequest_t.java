 package com.winthier.simpleshop.sql;
 
 import com.winthier.libsql.SQLRequest;
 import com.winthier.simpleshop.SimpleShopPlugin;
 import com.winthier.simpleshop.Util;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.DateFormatSymbols;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import net.milkbowl.vault.item.ItemInfo;
 import net.milkbowl.vault.item.Items;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.meta.EnchantmentStorageMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.scheduler.BukkitRunnable;
 
 public class ListTransactionsRequest extends BukkitRunnable implements SQLRequest {
         private final SimpleShopPlugin plugin;
         private final CommandSender sender;
         private final String owner;
         private final int page;
         public static final int PAGE_SIZE = 10;
 
         // Result
         private ResultSet result = null;
         private int count;
         private static Map<Integer, String> enchantmentNameMap = new HashMap<Integer, String>();
 
         private static void addMapping(Enchantment enchantment, String name) {
                 enchantmentNameMap.put(enchantment.getId(), name);
         }
 
         public static String getEnchantmentName(Enchantment enchantment) {
                 String result = enchantmentNameMap.get(enchantment.getId());
                 if (result == null) result = enchantment.getName();
                 return result;
         }
 
         static {
                 addMapping(Enchantment.ARROW_DAMAGE, "Power");
                 addMapping(Enchantment.ARROW_FIRE, "Flame");
                 addMapping(Enchantment.ARROW_INFINITE, "Infinity");
                 addMapping(Enchantment.ARROW_KNOCKBACK, "Knockback");
                 addMapping(Enchantment.DAMAGE_ALL, "Sharpness");
                 addMapping(Enchantment.DAMAGE_ARTHROPODS, "Bane of Arthropods");
                 addMapping(Enchantment.DAMAGE_UNDEAD, "Smite");
                 addMapping(Enchantment.DIG_SPEED, "Efficiency");
                 addMapping(Enchantment.DURABILITY, "Unbreaking");
                 addMapping(Enchantment.FIRE_ASPECT, "Fire Aspect");
                 addMapping(Enchantment.KNOCKBACK, "Knockback");
                 addMapping(Enchantment.LOOT_BONUS_BLOCKS, "Fortune");
                 addMapping(Enchantment.LOOT_BONUS_MOBS, "Looting");
                 addMapping(Enchantment.OXYGEN, "Respiration");
                 addMapping(Enchantment.PROTECTION_ENVIRONMENTAL, "Protection");
                 addMapping(Enchantment.PROTECTION_EXPLOSIONS, "Blast Protection");
                 addMapping(Enchantment.PROTECTION_FALL, "Feather Falling");
                 addMapping(Enchantment.PROTECTION_FIRE, "Fire Protection");
                 addMapping(Enchantment.PROTECTION_PROJECTILE, "Projectile Protection");
                 addMapping(Enchantment.SILK_TOUCH, "Silk Touch");
                 addMapping(Enchantment.THORNS, "Thorns");
                 addMapping(Enchantment.WATER_WORKER, "Water Affinity");
         }
 
         public ListTransactionsRequest(SimpleShopPlugin plugin, CommandSender sender, String owner, int page) {
                 this.plugin = plugin;
                 this.sender = sender;
                 this.owner = owner;
                 this.page = page;
         }
 
         @Override
         public void execute(Connection c) throws SQLException {
                 PreparedStatement s;
                 ResultSet result;
                 s = c.prepareStatement("SELECT COUNT(*) from simpleshop_transactions WHERE owner = ?");
                 s.setString(1, owner);
                 result = s.executeQuery();
                 if (result.next()) {
                         this.count = result.getInt(1);
                 }
                 s.close();
 
                 s = c.prepareStatement(
                         "SELECT * from `simpleshop_transactions`" +
                         " WHERE `owner` = ?" +
                         " ORDER BY `id` DESC" +
                         " LIMIT ?,?");
                 int i = 1;
                 s.setString(i++, owner);
                 s.setInt(i++, page * PAGE_SIZE);
                 s.setInt(i++, PAGE_SIZE);
                 this.result = s.executeQuery();
                 runTask(plugin);
         }
 
         @Override
         public void run() {
                 try {
                         result(result);
                 } catch (SQLException sqle) {
                         sqle.printStackTrace();
                 }
         }
 
         public void result(ResultSet result) throws SQLException {
                 Util.sendMessage(sender, "&bTransaction log for %s (page %d/%d)", owner, page + 1, (count - 1) / PAGE_SIZE + 1);
                 while (result.next()) {
                         String name;
                         final String matName = result.getString("material");
                         final Material mat = Material.matchMaterial(matName);
                         if (mat == null) {
                                 System.err.println("[SimpleShop] invalid material in database: " + matName);
                                 continue;
                         }
                         ItemInfo info = Items.itemByType(mat, (short)result.getInt("itemdata"));
                         if (info == null) {
                                 name = "" + matName + ":" + result.getInt("itemdata");
                         } else {
                                 name = info.getName();
                         }
                         String displayName = result.getString("display_name");
                         if (displayName != null) name += " Name=\"" + ChatColor.stripColor(displayName) + "\"";
                         StringBuilder sb = new StringBuilder();
                         for (Enchantment enchantment : Enchantment.values()) {
                                String enchantmentName = "enchantment_" + enchantment.getName().toLowerCase();
                                if (enchantmentName.contains("unknown")) continue;
                                int level = result.getInt(enchantmentName);
                                 if (level > 0) {
                                         if (sb.length() > 0) sb.append(", ");
                                         sb.append(getEnchantmentName(enchantment));
                                         sb.append(" ").append(level);
                                 }
                         }
                         if (sb.length() > 0) name += " " + sb.toString();
                         String player = result.getString("player");
                         String shopType = result.getString("shop_type");
                         Date date = result.getTimestamp("time");
                         Calendar cal = Calendar.getInstance();
                         int amount = result.getInt("amount");
                         double price = result.getDouble("price");
                         cal.setTime(date);
                         String[] months = DateFormatSymbols.getInstance().getShortMonths();
                         Util.sendMessage(sender, "[&b%s %02d&r] &b%s &3%s &b%d&3x&b%s&3 for &b%s", months[cal.get(Calendar.MONTH)], cal.get(Calendar.DAY_OF_MONTH), player, (shopType.equals("buy") ? "bought" : "sold"), amount, name, SimpleShopPlugin.formatPrice(price));
                 }
         }
 }
