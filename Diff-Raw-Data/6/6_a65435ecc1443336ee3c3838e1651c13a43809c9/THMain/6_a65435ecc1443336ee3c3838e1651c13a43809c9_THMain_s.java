 package me.cnaude.plugin.TrophyHeads;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockState;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Skeleton.SkeletonType;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.PrepareItemCraftEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.CraftingInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author cnaude
  */
 public class THMain extends JavaPlugin implements Listener {
 
     public static String LOG_HEADER;
     static final Logger log = Logger.getLogger("Minecraft");
     private static Random randomGenerator;
     private File pluginFolder;
     private File configFile;
     private static int dropChance = 50;
     private static int zombieDropChance = 0;
     private static int skeletonDropChance = 0;
     private static int creeperDropChance = 0;
     private static ArrayList<String> deathTypes = new ArrayList<String>();
     private static boolean debugEnabled = false;
     private static boolean renameEnabled = false;
     private static boolean playerSkin = true;
     private static boolean sneakPunchInfo = true;
     private static ArrayList<String> itemsRequired = new ArrayList<String>();
     private static Material renameItem = Material.PAPER;
 
     @Override
     public void onEnable() {
         LOG_HEADER = "[" + this.getName() + "]";
         randomGenerator = new Random();
         pluginFolder = getDataFolder();
         configFile = new File(pluginFolder, "config.yml");
         createConfig();
         this.getConfig().options().copyDefaults(true);
         saveConfig();
         loadConfig();
         getServer().getPluginManager().registerEvents(this, this);
         getCommand("headspawn").setExecutor(this);
 
         if (renameEnabled) {
             ItemStack resultHead = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
             ShapelessRecipe shapelessRecipe = new ShapelessRecipe(resultHead);
             shapelessRecipe.addIngredient(1, Material.SKULL_ITEM, -1);
             shapelessRecipe.addIngredient(1, renameItem, -1);
             getServer().addRecipe(shapelessRecipe);
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (player.hasPermission("trophyheads.spawn")) {
                 String pName = player.getName();
                 int count = 1;
                 if (args.length >= 1) {
                     pName = args[0];
                     if (args.length == 2) {
                         if (args[1].matches("\\d+")) {
                             count = Integer.parseInt(args[1]);
                         }
                     }
                 }
                 ItemStack item = new ItemStack(Material.SKULL_ITEM, count, (byte) 3);
                 Location loc = player.getLocation().clone();
                 World world = loc.getWorld();
                 ItemMeta itemMeta = item.getItemMeta();
                 ((SkullMeta) itemMeta).setOwner(pName);
                 item.setItemMeta(itemMeta);
                 world.dropItemNaturally(loc, item);
 
             } else {
                 player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
 
             }
         } else {
             sender.sendMessage("Only a player can use this command!");
         }
         return true;
     }
 
     @EventHandler
     public void onPrepareItemCraftEvent(PrepareItemCraftEvent event) {
         if (!renameEnabled) {
             return;
         }
         if (event.getRecipe() instanceof Recipe) {
            CraftingInventory ci = event.getInventory();
             ItemStack result = ci.getResult();
             if (result.getType().equals(Material.SKULL_ITEM)) {
                 for (ItemStack i : ci.getContents()) {
                     if (i.getType().equals(Material.SKULL_ITEM)) {
                         if (i.getData().getData() != (byte) 3) {
                             ci.setResult(new ItemStack(0));
                             return;
                         }
                     }
                 }
                 for (ItemStack i : ci.getContents()) {
                     if (i.hasItemMeta() && i.getType().equals(renameItem)) {
                         ItemMeta im = i.getItemMeta();
                         if (im.hasDisplayName()) {
                             ItemStack res = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
                             ItemMeta itemMeta = res.getItemMeta();
                             ((SkullMeta) itemMeta).setOwner(im.getDisplayName());
                             res.setItemMeta(itemMeta);
                             ci.setResult(res);
                             break;
                         }
                     }
                 }
             }
         }
     }
 
     @EventHandler
     public void onPlayerInteractEvent(PlayerInteractEvent event) {
         if (!sneakPunchInfo) {
             return;
         }
         Player player = event.getPlayer();
         if (!player.isSneaking()) {
             return;
         }
         if (!player.hasPermission("trophyheads.info")) {
             return;
         }
         if (event.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             org.bukkit.block.Block block = event.getClickedBlock();                            
             if (block.getType() == Material.SKULL) {                
                 BlockState bs = block.getState();
                 org.bukkit.block.Skull skull = (org.bukkit.block.Skull) bs;            
                 String pName;            
                 if (skull.hasOwner()) {
                     pName = skull.getOwner();
                 } else {
                     pName = "Unknown";
                 }
                 player.sendMessage(ChatColor.YELLOW + "This head once belonged to " + ChatColor.RED + pName);
             }
         }
     }
 
     @EventHandler
     public void onPlayerDeathEvent(PlayerDeathEvent event) {
         Player player = (Player) event.getEntity();
         if (!player.hasPermission("trophyheads.drop")) {
             return;
         }
         if (randomGenerator.nextInt(100) >= dropChance) {
             return;
         }
 
         boolean dropOkay = false;
         DamageCause dc = player.getLastDamageCause().getCause();
         logDebug("DamageCause: " + dc.toString());
 
         if (deathTypes.contains(dc.toString())) {
             dropOkay = true;
         }
         if (deathTypes.contains("ALL")) {
             dropOkay = true;
         }
 
         if (player.getKiller() instanceof Player) {
             if (deathTypes.contains("PVP")) {
                 if (itemsRequired.contains("ANY")) {
                     dropOkay = true;
                 }
                 Material mat = player.getKiller().getItemInHand().getType();
                 if (mat != null) {
                     if (itemsRequired.contains(String.valueOf(mat.getId()))) {
                         dropOkay = true;
                     } else if (itemsRequired.contains(mat.toString())) {
                         dropOkay = true;
                     }
                 }
             }
         }
 
         if (dropOkay) {
             logDebug("Match: true");
             Location loc = player.getLocation().clone();
             World world = loc.getWorld();
             String pName = player.getName();
 
             ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
             ItemMeta itemMeta = item.getItemMeta();
             ArrayList<String> itemDesc = new ArrayList<String>();
             itemMeta.setDisplayName("Head of " + pName);
             itemDesc.add(event.getDeathMessage());
             itemMeta.setLore(itemDesc);
             if (playerSkin) {
                 ((SkullMeta) itemMeta).setOwner(pName);
             }
             item.setItemMeta(itemMeta);
             world.dropItemNaturally(loc, item);
         } else {
             logDebug("Match: false");
         }
     }
 
     @EventHandler
     public void onEntityDeathEvent(EntityDeathEvent event) {
         EntityType et = event.getEntityType();
         Entity e = event.getEntity();
         int sti;
 
         if (et.equals(EntityType.SKELETON)) {
             if (((Skeleton) e).getSkeletonType().equals(SkeletonType.NORMAL)) {
                 if (randomGenerator.nextInt(100) >= skeletonDropChance) {
                     return;
                 }
                 sti = 0;
             } else {
                 return;
             }
         } else if (et.equals(EntityType.ZOMBIE)) {
             if (randomGenerator.nextInt(100) >= zombieDropChance) {
                 return;
             }
             sti = 2;
         } else if (et.equals(EntityType.CREEPER)) {
             if (randomGenerator.nextInt(100) >= creeperDropChance) {
                 return;
             }
             sti = 4;
         } else {
             return;
         }
 
         ItemStack item = new ItemStack(Material.SKULL_ITEM, 1, (byte) sti);
         Location loc = e.getLocation().clone();
         World world = loc.getWorld();
         world.dropItemNaturally(loc, item);
     }
 
     private void createConfig() {
         if (!pluginFolder.exists()) {
             try {
                 pluginFolder.mkdir();
             } catch (Exception e) {
                 logError(e.getMessage());
             }
         }
 
         if (!configFile.exists()) {
             try {
                 configFile.createNewFile();
             } catch (Exception e) {
                 logError(e.getMessage());
             }
         }
     }
 
     private void loadConfig() {
         debugEnabled = getConfig().getBoolean("debug-enabled");
         logDebug("Debug enabled");
 
         dropChance = getConfig().getInt("drop-chance");
         logDebug("Chance to drop head: " + dropChance + "%");
 
         playerSkin = getConfig().getBoolean("player-skin");
         logDebug("Player skins: " + playerSkin);
 
         sneakPunchInfo = getConfig().getBoolean("sneak-punch-info");
         logDebug("Sneak punch info: " + sneakPunchInfo);
 
         zombieDropChance = getConfig().getInt("zombie-heads.drop-chance");
         logDebug("Zombie chance to drop head: " + zombieDropChance + "%");
 
         skeletonDropChance = getConfig().getInt("zombie-heads.drop-chance");
         logDebug("Skeleton chance to drop head: " + skeletonDropChance + "%");
 
         creeperDropChance = getConfig().getInt("zombie-heads.drop-chance");
         logDebug("Creeper chance to drop head: " + creeperDropChance + "%");
 
         renameEnabled = getConfig().getBoolean("rename-enabled");
         if (renameEnabled) {
             try {
                 renameItem = Material.getMaterial(getConfig().getInt("rename-item"));
             } catch (Exception e) {
                 renameItem = Material.PAPER;
             }
             logDebug("Rename recipe enabled: head + " + renameItem.toString());
         }
 
         for (String s : getConfig().getStringList("items-required")) {
             itemsRequired.add(s.toUpperCase());
             logDebug("Valid PVP weapon: " + s.toUpperCase());
         }
 
         for (String s : getConfig().getStringList("death-types")) {
             deathTypes.add(s);
             logDebug("Valid death type: " + s);
         }
     }
 
     public void logInfo(String _message) {
         log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
     }
 
     public void logError(String _message) {
         log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
     }
 
     public void logDebug(String _message) {
         if (debugEnabled) {
             log.log(Level.INFO, String.format("%s [DEBUG] %s", LOG_HEADER, _message));
         }
     }
 }
