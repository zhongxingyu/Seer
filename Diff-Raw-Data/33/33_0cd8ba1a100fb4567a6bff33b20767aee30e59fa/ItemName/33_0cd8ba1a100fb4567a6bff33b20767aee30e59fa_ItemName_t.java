 package com.thepastimers.ItemName;
 
 import com.sun.media.sound.AiffFileReader;
 import com.thepastimers.Permission.Permission;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.*;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: solum
  * Date: 3/2/13
  * Time: 6:47 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ItemName extends JavaPlugin {
     List<ItemData> dataList;
     Permission permission;
 
     @Override
     public void onEnable() {
         getLogger().info("ItemName init");
 
         permission = (Permission)getServer().getPluginManager().getPlugin("Permission");
         if (permission == null) {
             getLogger().warning("Unable to load Permission plugin. Some functionality may not be available.");
         }
 
         dataList = new ArrayList<ItemData>();
         fillList();
 
         getLogger().info("ItemName init complete");
     }
 
     @Override
     public void onDisable() {
         getLogger().info("ItemName disabled");
     }
 
     public String getItemName(ItemStack is) {
         if (is == null) {
             return null;
         }
         String ret = is.getType().name();
 
         for (ItemData data : dataList) {
             if (data.compare(is.getType(),is.getDurability())) {
                 ret = data.getRealName();
                 break;
             }
         }
 
         return ret;
     }
 
     public ItemStack getItemFromName(String name) {
         for (ItemData data : dataList) {
             if (data.compare(name)) {
                 ItemStack is = new ItemStack(data.getMaterial());
                 is.setDurability(data.getDurability());
                 return is;
             }
         }
 
         Material mat = Material.getMaterial(name);
         if (mat == null) {
             return null;
         }
 
         return new ItemStack(mat);
     }
 
     public List<ItemStack> recipeContents(Recipe r) {
         List<ItemStack> ret = new ArrayList<ItemStack>();
 
         if (r == null) return ret;
 
         if (r instanceof FurnaceRecipe) {
             FurnaceRecipe f = (FurnaceRecipe)r;
             ret.add(f.getInput());
         } else if (r instanceof ShapedRecipe) {
             ShapedRecipe s = (ShapedRecipe)r;
             Map<Character,ItemStack> ingredients = s.getIngredientMap();
             for (Character c : ingredients.keySet()) {
                 ItemStack is = ingredients.get(c);
                 ret.add(is);
             }
         } else if (r instanceof ShapelessRecipe) {
             ShapelessRecipe s = (ShapelessRecipe)r;
             for (ItemStack is : s.getIngredientList()) {
                 ret.add(is);
             }
         }
 
         return ret;
     }
 
     public int countInInventory(String item, String player) {
         if (item == null || player == null) {
             return 0;
         }
         Player p = getServer().getPlayer(player);
 
         PlayerInventory pi = p.getInventory();
         ItemStack[] items = pi.getContents();
         int count = 0;
 
         for (ItemStack is : items) {
             if (item.equalsIgnoreCase(getItemName(is))) {
                 count += is.getAmount();
             }
         }
 
         return count;
     }
 
     public boolean giveItem(Player p, String item, int amount) {
         if (p == null || item == null || amount < 0) {
             return false;
         }
 
         ItemStack is = getItemFromName(item);
         return giveItem(p,is,amount);
     }
 
     public boolean giveItem(Player p, ItemStack is, int amount) {
         int empty = 0;
 
         PlayerInventory inv = p.getInventory();
 
         Iterator itor = inv.iterator();
 
         while (itor.hasNext()) {
             ItemStack is2 = (ItemStack)itor.next();
 
             if (is2 == null) {
                 empty ++;
                 continue;
             }
         }
 
         int max = is.getMaxStackSize();
 
         int stacks = (int)Math.ceil(((double)amount)/((double)max));
 
         if (stacks > empty) {
             p.sendMessage("You don't have inventory room for that much");
             return false;
         }
 
         ItemStack[] items = inv.getContents();
 
         int amountDone = 0;
         while (amountDone < amount) {
             int toDoTotal = is.getMaxStackSize();
             if (amountDone + toDoTotal > amount) {
                 toDoTotal = amount-amountDone;
             }
 
             int toDo = toDoTotal;
 
             for (int i=0;i<items.length;i++) {
                 ItemStack is3 = items[i];
                 if (is3 != null && is3.getType() == is.getType() && is3.getDurability() == is.getDurability()) {
                     if (is3.getAmount() + toDo <= is.getMaxStackSize()) {
                         is3.setAmount(is3.getAmount() + toDo);
                         break;
                     } else {
                         toDo -= (is.getMaxStackSize()-is3.getAmount());
                         is3.setAmount(is.getMaxStackSize());
                     }
                 }
                 if (items[i] == null) {
                     ItemStack is2 = new ItemStack(is.getType(),toDo);
                     is2.setDurability(is.getDurability());
                     is2.setData(is.getData());
                     is2.setItemMeta(is.getItemMeta());
                     items[i] = is2;
                     break;
                 }
             }
 
             amountDone += toDoTotal;
         }
 
         inv.setContents(items);
 
         return true;
     }
 
     public boolean takeItem(Player p, String item, int amount) {
         return takeItem(p,item,amount,false);
     }
 
     public boolean takeItem(Player p, String item, int amount, boolean checkDurability) {
         //getLogger().info("Take item " + p + " "+ item + " " + amount);
         if (p == null || item == null || amount < 0) {
             return false;
         }
 
         PlayerInventory inv = p.getInventory();
 
         int total = countInInventory(item,p.getName());
 
         if (total < amount) {
             p.sendMessage("You don't have that much");
             return false;
         }
 
         ItemStack[] items = inv.getContents();
 
         for (int i=0;i<items.length;i++) {
             ItemStack is = items[i];
             String name = getItemName(is);
             if (name != null && item.equalsIgnoreCase(name)) {
                 if (isTool(name)) {
                     if (is.getDurability() > 0) {
                         p.sendMessage(ChatColor.RED + "One of the items you are trying to add to the sign is damaged. At this time, you cannot add damaged items to trade signs.");
                         return false;
                     }
                 }
             }
         }
 
         for (int i=0;i<items.length;i++) {
             ItemStack is = items[i];
             String name = getItemName(is);
             if (name != null && item.equalsIgnoreCase(name)) {
                 if (is.getAmount() > amount) {
                     is.setAmount(is.getAmount()-amount);
                     amount = 0;
                     break;
                 } else {
                     amount -= is.getAmount();
                     is = new ItemStack(Material.AIR);
                 }
                 items[i] = is;
 
                 if (amount <= 0) {
                     break;
                 }
             }
         }
 
         inv.setContents(items);
 
         return true;
     }
 
    public boolean isEnchanted(ItemStack is)
    {
        if (is == null) return false;

        Material t = is.getType();
        if (t == Material.ENCHANTED_BOOK) {
            return true;
        }
        Map<Enchantment, Integer> m = is.getEnchantments();

        if (m.keySet().size() > 0) {
            return true;
        }

        return false;
    }

     public boolean isTool(String item) {
         ItemStack is = getItemFromName(item);
         if (is == null) return false;
         
         Material t = is.getType();
         if (t == Material.WOOD_SPADE || t == Material.STONE_SPADE || t == Material.IRON_SPADE || t == Material.GOLD_SPADE || t == Material.DIAMOND_SPADE
                 || t == Material.WOOD_AXE || t == Material.STONE_AXE || t == Material.IRON_AXE || t == Material.GOLD_AXE || t == Material.DIAMOND_AXE
                 || t == Material.WOOD_PICKAXE || t == Material.STONE_PICKAXE || t == Material.IRON_PICKAXE || t == Material.GOLD_PICKAXE || t == Material.DIAMOND_PICKAXE
                 || t == Material.WOOD_HOE || t == Material.STONE_HOE || t == Material.IRON_HOE || t == Material.GOLD_HOE || t == Material.DIAMOND_HOE
                 || t == Material.WOOD_SWORD || t == Material.STONE_SWORD || t == Material.IRON_SWORD || t == Material.GOLD_SWORD || t == Material.DIAMOND_SWORD) {
             return true;
         }
 
         return false;
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         String playerName = "";
 
         if (sender instanceof Player) {
             playerName = ((Player)sender).getName();
         } else {
             playerName = "CONSOLE";
         }
 
         String command = cmd.getName();
 
         if (command.equalsIgnoreCase("nameitem")) {
             if (permission == null || !permission.hasPermission(playerName,"item_name") || playerName.equalsIgnoreCase("CONSOLE")) {
                 sender.sendMessage("You don't have permissions for this command (item_name)");
                 return true;
             }
 
             Player p = (Player)sender;
 
             ItemStack inHand = p.getItemInHand();
 
             if (inHand == null) {
                 sender.sendMessage("You have nothing in your hand.");
             } else {
                 String name = getItemName(inHand);
                 sender.sendMessage("The item in your hand is " + name);
             }
         } else {
             return false;
         }
 
         return true;
     }
 
     private void fillList() {
         dataList.add(new ItemData("OAK_PLANK",Material.getMaterial(5),0));
         dataList.add(new ItemData("SPRUCE_PLANK",Material.getMaterial(5),1));
         dataList.add(new ItemData("BIRCH_PLANK",Material.getMaterial(5),2));
         dataList.add(new ItemData("JUNGLE_PLANK",Material.getMaterial(5),3));
 
         dataList.add(new ItemData("OAK_SAPLING",Material.getMaterial(6),0));
         dataList.add(new ItemData("SPRUCE_SAPLING",Material.getMaterial(6),1));
         dataList.add(new ItemData("BIRCH_SAPLING",Material.getMaterial(6),2));
         dataList.add(new ItemData("JUNGLE_SAPLING",Material.getMaterial(6),3));
 
         dataList.add(new ItemData("OAK",Material.getMaterial(17),0));
         dataList.add(new ItemData("SPRUCE",Material.getMaterial(17),1));
         dataList.add(new ItemData("BIRCH",Material.getMaterial(17),2));
         dataList.add(new ItemData("JUNGLE",Material.getMaterial(17),3));
 
         dataList.add(new ItemData("OAK_LEAVES",Material.getMaterial(18),0));
         dataList.add(new ItemData("SPRUCE_LEAVES",Material.getMaterial(18),1));
         dataList.add(new ItemData("BIRCH_LEAVES",Material.getMaterial(18),2));
         dataList.add(new ItemData("JUNGLE_LEAVES",Material.getMaterial(18),3));
 
         dataList.add(new ItemData("SANDSTONE",Material.getMaterial(24),0));
         dataList.add(new ItemData("SANDSTONE_CHISEL",Material.getMaterial(24),1));
         dataList.add(new ItemData("SANDSTONE_SMOOTH",Material.getMaterial(24),2));
 
         dataList.add(new ItemData("DEAD_SHRUB",Material.getMaterial(31),0));
         dataList.add(new ItemData("TALL_GRASS",Material.getMaterial(31),1));
         dataList.add(new ItemData("FERN",Material.getMaterial(31),2));
 
         dataList.add(new ItemData("WHITE_WOOL",Material.getMaterial(35),0));
         dataList.add(new ItemData("ORANGE_WOOL",Material.getMaterial(35),1));
         dataList.add(new ItemData("MAGENTA_WOOL",Material.getMaterial(35),2));
         dataList.add(new ItemData("LT_BLUE_WOOL",Material.getMaterial(35),3));
         dataList.add(new ItemData("YELLOW_WOOL",Material.getMaterial(35),4));
         dataList.add(new ItemData("LIME_WOOL",Material.getMaterial(35),5));
         dataList.add(new ItemData("PINK_WOOL",Material.getMaterial(35),6));
         dataList.add(new ItemData("GREY_WOOL",Material.getMaterial(35),7));
         dataList.add(new ItemData("LT_GREY_WOOL",Material.getMaterial(35),8));
         dataList.add(new ItemData("CYAN_WOOL",Material.getMaterial(35),9));
         dataList.add(new ItemData("PURPLE_WOOL",Material.getMaterial(35),10));
         dataList.add(new ItemData("BLUE_WOOL",Material.getMaterial(35),11));
         dataList.add(new ItemData("BROWN_WOOL",Material.getMaterial(35),12));
         dataList.add(new ItemData("GREEN_WOOL",Material.getMaterial(35),13));
         dataList.add(new ItemData("RED_WOOL",Material.getMaterial(35),14));
         dataList.add(new ItemData("BLACK_WOOL",Material.getMaterial(35),15));
 
         // I'm just going to ignore double slabs here, becuase I don't think they actually matter
 
         dataList.add(new ItemData("STONE_SLAB",Material.getMaterial(44),0));
         dataList.add(new ItemData("SANDSTONE_SLAB",Material.getMaterial(44),1));
         dataList.add(new ItemData("WOOD_SLAB",Material.getMaterial(44),2));
         dataList.add(new ItemData("COBBLE_SLAB",Material.getMaterial(44),3));
         dataList.add(new ItemData("BRICK_SLAB",Material.getMaterial(44),4));
         dataList.add(new ItemData("STONE_BRICK_SLAB",Material.getMaterial(44),5));
         dataList.add(new ItemData("NETHER_BRICK_SLAB",Material.getMaterial(44),6));
         dataList.add(new ItemData("QUARTZ_SLAB",Material.getMaterial(44),7));
 
         // also ignoring silverfish cobblestone
 
         dataList.add(new ItemData("STONE_BRICK",Material.getMaterial(98),0));
         dataList.add(new ItemData("MOSSY_STONE_BRICK",Material.getMaterial(98),1));
         dataList.add(new ItemData("CRACKED_STONE",Material.getMaterial(98),2));
         dataList.add(new ItemData("CHISELED_STONE",Material.getMaterial(98),3));
 
         // ignoring double wood slabs
 
         dataList.add(new ItemData("OAK_SLAB",Material.getMaterial(126),0));
         dataList.add(new ItemData("SPRUCE_SLAB",Material.getMaterial(126),1));
         dataList.add(new ItemData("BIRCH_SLAB",Material.getMaterial(126),2));
         dataList.add(new ItemData("JUNGLE_SLAB",Material.getMaterial(126),3));
 
         dataList.add(new ItemData("COBBLE_WALL",Material.getMaterial(139),0));
         dataList.add(new ItemData("MOSSY_WALL",Material.getMaterial(139),1));
 
         // ignoring head block
 
         dataList.add(new ItemData("ANVIL",Material.getMaterial(145),0));
         dataList.add(new ItemData("ANVIL_DAMAGE",Material.getMaterial(145),1));
         dataList.add(new ItemData("ANVIL_VERY_DAMAGE",Material.getMaterial(145),2));
 
         dataList.add(new ItemData("QUARTZ",Material.getMaterial(155),0));
         dataList.add(new ItemData("CHISELED_QUARTZ",Material.getMaterial(155),1));
         dataList.add(new ItemData("QUARTZ_PILLAR",Material.getMaterial(155),2));
 
         dataList.add(new ItemData("WHITE_CLAY",Material.getMaterial(159),0));
         dataList.add(new ItemData("ORANGE_CLAY",Material.getMaterial(159),1));
         dataList.add(new ItemData("MAGENTA_CLAY",Material.getMaterial(159),2));
         dataList.add(new ItemData("LT_BLUE_CLAY",Material.getMaterial(159),3));
         dataList.add(new ItemData("YELLOW_CLAY",Material.getMaterial(159),4));
         dataList.add(new ItemData("LIME_CLAY",Material.getMaterial(159),5));
         dataList.add(new ItemData("PINK_CLAY",Material.getMaterial(159),6));
         dataList.add(new ItemData("GRAY_CLAY",Material.getMaterial(159),7));
         dataList.add(new ItemData("LT_GRAY_CLAY",Material.getMaterial(159),8));
         dataList.add(new ItemData("CYAN_CLAY",Material.getMaterial(159),9));
         dataList.add(new ItemData("PURPLE_CLAY",Material.getMaterial(159),10));
         dataList.add(new ItemData("BLUE_CLAY",Material.getMaterial(159),11));
         dataList.add(new ItemData("BROWN_CLAY",Material.getMaterial(159),12));
         dataList.add(new ItemData("GREEN_CLAY",Material.getMaterial(159),13));
         dataList.add(new ItemData("RED_CLAY",Material.getMaterial(159),14));
         dataList.add(new ItemData("BLACK_CLAY",Material.getMaterial(159),15));
 
         dataList.add(new ItemData("WHITE_CARPET",Material.getMaterial(171),0));
         dataList.add(new ItemData("ORANGE_CARPET",Material.getMaterial(171),1));
         dataList.add(new ItemData("MAGENTA_CARPET",Material.getMaterial(171),2));
         dataList.add(new ItemData("LT_BLUE_CARPET",Material.getMaterial(171),3));
         dataList.add(new ItemData("YELLOW_CARPET",Material.getMaterial(171),4));
         dataList.add(new ItemData("LIME_CARPET",Material.getMaterial(171),5));
         dataList.add(new ItemData("PINK_CARPET",Material.getMaterial(171),6));
         dataList.add(new ItemData("GREY_CARPET",Material.getMaterial(171),7));
         dataList.add(new ItemData("LT_GREY_CARPET",Material.getMaterial(171),8));
         dataList.add(new ItemData("CYAN_CARPET",Material.getMaterial(171),9));
         dataList.add(new ItemData("PURPLE_CARPET",Material.getMaterial(171),10));
         dataList.add(new ItemData("BLUE_CARPET",Material.getMaterial(171),11));
         dataList.add(new ItemData("BROWN_CARPET",Material.getMaterial(171),12));
         dataList.add(new ItemData("GREEN_CARPET",Material.getMaterial(171),13));
         dataList.add(new ItemData("RED_CARPET",Material.getMaterial(171),14));
         dataList.add(new ItemData("BLACK_CARPET",Material.getMaterial(171),15));
 
         dataList.add(new ItemData("COAL",Material.getMaterial(263),0));
         dataList.add(new ItemData("CHARCOAL",Material.getMaterial(263),1));
 
         dataList.add(new ItemData("GOLDEN_APPLE",Material.getMaterial(322),0));
         dataList.add(new ItemData("EPIC_GOLDEN_APPLE",Material.getMaterial(322),1));
 
         dataList.add(new ItemData("INK_SACK",Material.getMaterial(351),0));
         dataList.add(new ItemData("RED_DYE",Material.getMaterial(351),1));
         dataList.add(new ItemData("GREEN_DYE",Material.getMaterial(351),2));
         dataList.add(new ItemData("COCA_BEAN",Material.getMaterial(351),3));
         dataList.add(new ItemData("LAPIS",Material.getMaterial(351),4));
         dataList.add(new ItemData("PURPLE_DYE",Material.getMaterial(351),5));
         dataList.add(new ItemData("CYAN_DYE",Material.getMaterial(351),6));
         dataList.add(new ItemData("LT_GREY_DYE",Material.getMaterial(351),7));
         dataList.add(new ItemData("GREY_DYE",Material.getMaterial(351),8));
         dataList.add(new ItemData("PINK_DYE",Material.getMaterial(351),9));
         dataList.add(new ItemData("LIME_DYE",Material.getMaterial(351),10));
         dataList.add(new ItemData("YELLOW_DYE",Material.getMaterial(351),11));
         dataList.add(new ItemData("LT_BLUE_DYE",Material.getMaterial(351),12));
         dataList.add(new ItemData("MAGENTA_DYE",Material.getMaterial(351),13));
         dataList.add(new ItemData("ORANGE_DYE",Material.getMaterial(351),14));
         dataList.add(new ItemData("BONE_MEAL",Material.getMaterial(351),15));
 
         dataList.add(new ItemData("WATER_BOTTLE",Material.getMaterial(373),0));
         dataList.add(new ItemData("AWKWARD_POTION",Material.getMaterial(373),16));
         dataList.add(new ItemData("THICK_POTION",Material.getMaterial(373),32));
         dataList.add(new ItemData("MUNDANE_POTION",Material.getMaterial(373),64));
         dataList.add(new ItemData("REGEN1_POTION",Material.getMaterial(373),8193));
         dataList.add(new ItemData("SWIFT1_POTION",Material.getMaterial(373),8194));
         dataList.add(new ItemData("FIRE_RESIST_POTION",Material.getMaterial(373),8195));
         dataList.add(new ItemData("POISON1_POTION",Material.getMaterial(373),8196));
         dataList.add(new ItemData("HEALING1_POTION",Material.getMaterial(373),8197));
         dataList.add(new ItemData("NIGHT_VIS_POTION",Material.getMaterial(373),8198));
         dataList.add(new ItemData("WEAK_POTION",Material.getMaterial(373),8200));
         dataList.add(new ItemData("STR1_POTION",Material.getMaterial(373),8201));
         dataList.add(new ItemData("SLOW1_POTION",Material.getMaterial(373),8202));
         dataList.add(new ItemData("HARM1_POTION",Material.getMaterial(373),8204));
         dataList.add(new ItemData("INVIS_POTION",Material.getMaterial(373),8206));
 
         dataList.add(new ItemData("REGEN2_POTION",Material.getMaterial(373),8225));
         dataList.add(new ItemData("SWIFT2_POTION",Material.getMaterial(373),8226));
         dataList.add(new ItemData("POISON2_POTION",Material.getMaterial(373),8228));
         dataList.add(new ItemData("HEALING2_POTION",Material.getMaterial(373),8229));
         dataList.add(new ItemData("STR2_POTION",Material.getMaterial(373),8233));
         dataList.add(new ItemData("HARM2_POTION",Material.getMaterial(373),8236));
 
         dataList.add(new ItemData("REGENL_POTION",Material.getMaterial(373),8257));
         dataList.add(new ItemData("SWITFL_POTION",Material.getMaterial(373),8258));
         dataList.add(new ItemData("FIRE_RESISTL_POTION",Material.getMaterial(373),8259));
         dataList.add(new ItemData("POISONL_POTION",Material.getMaterial(373),8260));
         dataList.add(new ItemData("NIGHT_VISL_POTION",Material.getMaterial(373),8262));
         dataList.add(new ItemData("WEAKL_POTION",Material.getMaterial(373),8264));
         dataList.add(new ItemData("STRL_POTION",Material.getMaterial(373),8265));
         dataList.add(new ItemData("SLOWL_POTION",Material.getMaterial(373),8266));
         dataList.add(new ItemData("INVISL_POTION",Material.getMaterial(373),8270)) ;
 
         dataList.add(new ItemData("REGEN1_SPLASH",Material.getMaterial(373),16385));
         dataList.add(new ItemData("SWIFT1_SPLASH",Material.getMaterial(373),16386));
         dataList.add(new ItemData("FIRE_RESIST_SPLASH",Material.getMaterial(373),16387));
         dataList.add(new ItemData("POISON1_SPLASH",Material.getMaterial(373),16388));
         dataList.add(new ItemData("HEALING1_SPLASH",Material.getMaterial(373),16389));
         dataList.add(new ItemData("NIGHT_VIS_SPLASH",Material.getMaterial(373),16390));
         dataList.add(new ItemData("WEAK_SPLASH",Material.getMaterial(373),16392));
         dataList.add(new ItemData("STR1_SPLASH",Material.getMaterial(373),16393));
         dataList.add(new ItemData("SLOW1_SPLASH",Material.getMaterial(373),16394));
         dataList.add(new ItemData("HARM1_SPLASH",Material.getMaterial(373),16396));
         dataList.add(new ItemData("INVIS_SPLASH",Material.getMaterial(373),16398));
 
         dataList.add(new ItemData("REGEN2_SPLASH",Material.getMaterial(373),16417));
         dataList.add(new ItemData("SWIFT2_SPLASH",Material.getMaterial(373),16418));
         dataList.add(new ItemData("POISON2_SPLASH",Material.getMaterial(373),16420));
         dataList.add(new ItemData("HEALING2_SPLASH",Material.getMaterial(373),16421));
         dataList.add(new ItemData("STR2_SPLASH",Material.getMaterial(373),16425));
         dataList.add(new ItemData("HARM2_SPLASH",Material.getMaterial(373),16428));
 
         dataList.add(new ItemData("REGENL_SPLASH",Material.getMaterial(373),16449));
         dataList.add(new ItemData("SWITFL_SPLASH",Material.getMaterial(373),16450));
         dataList.add(new ItemData("FIRE_RESISTL_SPLASH",Material.getMaterial(373),16451));
         dataList.add(new ItemData("POISONL_SPLASH",Material.getMaterial(373),16452));
         dataList.add(new ItemData("NIGHT_VISL_SPLASH",Material.getMaterial(373),16454));
         dataList.add(new ItemData("WEAKL_SPLASH",Material.getMaterial(373),16456));
         dataList.add(new ItemData("STRL_SPLASH",Material.getMaterial(373),16457));
         dataList.add(new ItemData("SLOWL_SPLASH",Material.getMaterial(373),16458));
         dataList.add(new ItemData("INVISL_SPLASH",Material.getMaterial(373),16462));
 
         dataList.add(new ItemData("CREEPER_EGG",Material.getMaterial(383),50));
         dataList.add(new ItemData("SKELE_EGG",Material.getMaterial(383),51));
         dataList.add(new ItemData("SPIDER_EGG",Material.getMaterial(383),52));
         dataList.add(new ItemData("ZOMBIE_EGG",Material.getMaterial(383),54));
         dataList.add(new ItemData("SLIME_EGG",Material.getMaterial(383),55));
         dataList.add(new ItemData("GHAST_EGG",Material.getMaterial(383),56));
         dataList.add(new ItemData("ZOMBIE_PIG_EGG",Material.getMaterial(383),57));
         dataList.add(new ItemData("ENDERMAN_EGG",Material.getMaterial(383),58));
         dataList.add(new ItemData("CAVE_SPIDER_EGG",Material.getMaterial(383),59));
         dataList.add(new ItemData("SLIVERFISH_EGG",Material.getMaterial(383),60));
         dataList.add(new ItemData("BLAZE_EGG",Material.getMaterial(383),61));
         dataList.add(new ItemData("MAGMA_EGG",Material.getMaterial(383),62));
         dataList.add(new ItemData("BAT_EGG",Material.getMaterial(383),65));
         dataList.add(new ItemData("WITCH_EGG",Material.getMaterial(383),66));
         dataList.add(new ItemData("PIG_EGG",Material.getMaterial(383),90));
         dataList.add(new ItemData("SHEEP_EGG",Material.getMaterial(383),91));
         dataList.add(new ItemData("COW_EGG",Material.getMaterial(383),92));
         dataList.add(new ItemData("CHICKEN_EGG",Material.getMaterial(383),93));
         dataList.add(new ItemData("SQUID_EGG",Material.getMaterial(383),94));
         dataList.add(new ItemData("WOLF_EGG",Material.getMaterial(383),95));
         dataList.add(new ItemData("MOOSHROOM_EGG",Material.getMaterial(383),96));
         dataList.add(new ItemData("CAT_EGG",Material.getMaterial(383),98));
         dataList.add(new ItemData("HORSE_EGG",Material.getMaterial(383),100));
         dataList.add(new ItemData("VILLAGER_EGG",Material.getMaterial(383),120));
 
         dataList.add(new ItemData("SKELE_HEAD",Material.getMaterial(397),0));
         dataList.add(new ItemData("WITHER_HEAD",Material.getMaterial(397),1));
         dataList.add(new ItemData("ZOMBIE_HEAD",Material.getMaterial(397),2));
         dataList.add(new ItemData("STEVE_HEAD",Material.getMaterial(397),3));
         dataList.add(new ItemData("CREEPER_HEAD",Material.getMaterial(397),4));
 
     }
 }
