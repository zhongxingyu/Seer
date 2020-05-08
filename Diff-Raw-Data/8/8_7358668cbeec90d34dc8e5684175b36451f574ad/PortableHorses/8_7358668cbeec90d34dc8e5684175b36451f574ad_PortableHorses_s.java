 package com.norcode.bukkit.portablehorses;
 
 import net.gravitydevelopment.updater.Updater;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Horse;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.inventory.InventoryAction;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.HorseInventory;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.*;
 import java.util.logging.Level;
 
 public class PortableHorses extends JavaPlugin implements Listener {
 
 
     private static final EnumSet<Material> INTERACTIVE_BLOCKS = EnumSet.of(Material.WOODEN_DOOR, Material.IRON_DOOR_BLOCK, Material.FENCE_GATE, Material.WORKBENCH,
                         Material.ENCHANTMENT_TABLE, Material.ENDER_CHEST, Material.ENDER_PORTAL_FRAME, Material.CHEST, Material.TRAPPED_CHEST, Material.REDSTONE_COMPARATOR_OFF,
                         Material.REDSTONE_COMPARATOR_ON, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.BEACON, Material.TRAP_DOOR, Material.NOTE_BLOCK, Material.JUKEBOX,
                         Material.BREWING_STAND, Material.ANVIL, Material.BED_BLOCK, Material.FURNACE, Material.BURNING_FURNACE);
 
     private Updater updater;
     private IPacketListener packetListener;
     private boolean debugMode = false;
     private boolean usePermissions = true;
     private boolean storeArmor = true;
     private boolean storeInventory = true;
     private boolean allowNestedSaddles = false;
     private boolean requireSpecialSaddle = false;
     private boolean craftSpecialSaddle = false;
     private boolean allowSaddleRemoval = true;
 
     private Random random = new Random();
     private HashMap<String, HashMap<Long, List<String>>> loreStorage = new HashMap<String, HashMap<Long, List<String>>>();
     private ShapedRecipe specialSaddleRecipe;
     private NMS nmsHandler;
 
 
 
     private Recipe getSpecialSaddleRecipe() {
         if (this.specialSaddleRecipe == null) {
             ItemStack result = getEmptyPortableHorseSaddle();
             this.specialSaddleRecipe = new ShapedRecipe(result);
             this.specialSaddleRecipe.shape("PPP", "PSP", "PPP");
             this.specialSaddleRecipe.setIngredient('P', getCraftingSupplement());
             this.specialSaddleRecipe.setIngredient('S', Material.SADDLE);
         }
         return this.specialSaddleRecipe;
     }
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         getConfig().options().copyDefaults(true);
         saveConfig();
         initializeNMSHandler();
         initializePacketListener();
         doUpdater();
         getServer().getPluginManager().registerEvents(this, this);
 
     }
 
 	private void initializePacketListener() {
 		String packageName = this.getServer().getClass().getPackage().getName();
 		// Get full package string of CraftServer.
 		// org.bukkit.craftbukkit.versionstring (or for pre-refactor, just org.bukkit.craftbukkit
 		String version = packageName.substring(packageName.lastIndexOf('.') + 1);
 		// Get the last element of the package
 		if (version.equals("craftbukkit")) { // If the last element of the package was "craftbukkit" we are now pre-refactor
 			version = "pre";
 		}
 		try {
 			final Class<?> clazz = Class.forName("com.norcode.bukkit.portablehorses." + version + ".PacketListener");
 			// Check if we have a NMSHandler class at that location.
 			if (IPacketListener.class.isAssignableFrom(clazz)) { // Make sure it actually implements NMS
 				this.packetListener = (IPacketListener) clazz.getConstructor(JavaPlugin.class).newInstance(this); // Set our handler
 			}
 		} catch (final Exception e) {
 			getLogger().log(Level.SEVERE, "Exception loading implementation: ", e);
 
 			this.getLogger().severe("Could not find support for this craftbukkit version " + version + ".");
 			this.getLogger().info("Check for updates at http://dev.bukktit.org/bukkit-plugins/portable-horses/");
 			this.setEnabled(false);
 			return;
 		}
 	}
 
 	private void initializeNMSHandler() {
         String packageName = this.getServer().getClass().getPackage().getName();
         // Get full package string of CraftServer.
         // org.bukkit.craftbukkit.versionstring (or for pre-refactor, just org.bukkit.craftbukkit
         String version = packageName.substring(packageName.lastIndexOf('.') + 1);
         // Get the last element of the package
         if (version.equals("craftbukkit")) { // If the last element of the package was "craftbukkit" we are now pre-refactor
             version = "pre";
         }
         try {
             final Class<?> clazz = Class.forName("com.norcode.bukkit.portablehorses." + version + ".NMSHandler");
             // Check if we have a NMSHandler class at that location.
             if (NMS.class.isAssignableFrom(clazz)) { // Make sure it actually implements NMS
                 this.nmsHandler = (NMS) clazz.getConstructor().newInstance(); // Set our handler
             }
         } catch (final Exception e) {
             getLogger().log(Level.SEVERE, "Exception loading implementation: ", e);
 
             this.getLogger().severe("Could not find support for this craftbukkit version " + version + ".");
             this.getLogger().info("Check for updates at http://dev.bukktit.org/bukkit-plugins/portable-horses/");
             this.setEnabled(false);
             return;
         }
     }
 
     @Override
     public void reloadConfig() {
         super.reloadConfig();
         this.usePermissions = getConfig().getBoolean("use-permissions", true);
         this.debugMode = getConfig().getBoolean("debug", false);
         this.storeArmor = getConfig().getBoolean("store-armor", true);
         this.storeInventory = getConfig().getBoolean("store-inventory", true);
         this.allowNestedSaddles = getConfig().getBoolean("allow-nested-saddles", false);
         this.requireSpecialSaddle = getConfig().getBoolean("require-special-saddle", false);
         this.craftSpecialSaddle = getConfig().getBoolean("craft-special-saddle", false);
         this.allowSaddleRemoval = getConfig().getBoolean("allow-saddle-removal", true);
 
         // Add or remove the crafting recipe for the special saddle as necessary.
         boolean found = false;
         Iterator<Recipe> it = getServer().recipeIterator();
         try {
             while (it.hasNext()) {
                 Recipe r = it.next();
                 if (r.equals(this.specialSaddleRecipe)) {
                     if (!craftSpecialSaddle) {
                         it.remove();
                         break;
                     } else {
                         found = true;
                     }
                 }
             }
         } catch (AbstractMethodError ex) {
             getLogger().warning("abstract method error");
         }
 
 
 
         if (craftSpecialSaddle && !found) {
             getServer().addRecipe(this.getSpecialSaddleRecipe());
         }
 
     }
 
     public void doUpdater() {
         String autoUpdate = getConfig().getString("auto-update", "notify-only").toLowerCase();
         if (autoUpdate.equals("true")) {
             updater = new Updater(this, 64321, this.getFile(), Updater.UpdateType.DEFAULT, true);
         } else if (autoUpdate.equals("false")) {
             getLogger().info("Auto-updater is disabled.  Skipping check.");
         } else {
             updater = new Updater(this, 64321, this.getFile(), Updater.UpdateType.NO_DOWNLOAD, true);
         }
     }
 
     public void debug(String s) {
         if (debugMode) {
             getLogger().info(s);
         }
     }
 
     @EventHandler
     public void onClickHorse(EntityDamageByEntityEvent event) {
         if (!allowSaddleRemoval) return;
         if (event.getDamager() instanceof Player) {
             if (event.getEntity() instanceof Horse) {
                 Player p = (Player) event.getDamager();
                 if (p.isSneaking()) {
                     event.setCancelled(true);
                     Horse h = (Horse) event.getEntity();
                     if (isPortableHorseSaddle(h.getInventory().getSaddle())) {
                         // Remove the saddle and 'disenchant' it.
                         h.getInventory().setSaddle(null);
                         h.getWorld().dropItem(h.getLocation(), new ItemStack(getEmptyPortableHorseSaddle()));
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority= EventPriority.NORMAL, ignoreCancelled = true)
     public void onSaddleEvent(final InventoryClickEvent event) {
 
         if (!(event.getInventory() instanceof HorseInventory)) return;
         Horse horse = ((Horse) event.getInventory().getHolder());
         if (debugMode) {
             debug("Inventory Action:" + event.getAction());
             debug("Cursor:" + event.getCursor());
             debug("CurrentItem:" + event.getCurrentItem());
             debug("Click:" + event.getClick());
         }
         if (event.isShiftClick()) {
             if (event.getRawSlot() != 0) {
                 if (isPortableHorseSaddle(event.getCurrentItem())) {
                     event.setCancelled(true);
                 } else if (isEmptyPortableHorseSaddle(event.getCurrentItem()) && ((HorseInventory) event.getInventory()).getSaddle() == null) {
                     onSaddled(event, horse, event.getCurrentItem());
                 }
             } else if (event.getRawSlot() == 0 && event.getWhoClicked().getInventory().firstEmpty() != -1 && isPortableHorseSaddle(event.getCurrentItem())) {
                 // Removing a saddle by shift-click.
                 onUnsaddled(event, horse, event.getCurrentItem());
             }
         } else if (event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE) {
             if (event.getRawSlot() == 0 && event.getCurrentItem().getType() == Material.AIR) {
                 if (isPortableHorseSaddle(event.getCursor())) {
                     event.setCancelled(true);
                 } else if (isEmptyPortableHorseSaddle(event.getCursor())) {
                     debug("Saddling!");
                     onSaddled(event, horse, event.getCursor());
                 }
             }
         } else if (event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_HALF) {
             if (event.getRawSlot() == 0 && isPortableHorseSaddle(event.getCurrentItem())) {
                 // removed a saddle.
                 onUnsaddled(event, horse, event.getCurrentItem());
             }
         } else if ((event.getAction() == InventoryAction.HOTBAR_SWAP ||
                     event.getAction() == InventoryAction.DROP_ONE_SLOT ||
                     event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD) &&
                     event.getRawSlot() == 0 && isPortableHorseSaddle(event.getCurrentItem())) {
             onUnsaddled(event, horse, event.getCurrentItem());
         }
     }
 
 
     @EventHandler(priority=EventPriority.HIGH)
     public void onInventoryClick(final InventoryClickEvent event) {
 
         if (!(event.getInventory() instanceof HorseInventory)) return;
         if (allowNestedSaddles) {
             return;
         }
         Horse horse = ((Horse) event.getInventory().getHolder());
         if (!horse.isCarryingChest()) {
             return;
         }
         getServer().getScheduler().runTaskLater(this, new Runnable() {
             @Override
             public void run() {
                 for (int i=1;i<event.getInventory().getSize();i++) {
                     ItemStack s = ((HorseInventory) event.getInventory()).getItem(i);
                     if (s != null && isPortableHorseSaddle(s)) {
                         event.getInventory().setItem(i, null);
                         event.getWhoClicked().getInventory().addItem(s);
                         ((Player) event.getWhoClicked()).updateInventory();
                     }
                 }
             }
         }, 0);
     }
 
 
     public void onSaddled(InventoryClickEvent event, Horse horse, ItemStack saddle) {
         debug(horse + "Saddled.");
         if (!usePermissions || event.getWhoClicked().hasPermission("portablehorses.saddle")) {
             nmsHandler.saveToSaddle(horse, saddle);
             horse.getInventory().setSaddle(saddle);
             event.setCurrentItem(null);
         }
     }
 
     public void onUnsaddled(InventoryClickEvent event, Horse horse, ItemStack saddle) {
         debug(horse + "Unsaddled.");
         if (!usePermissions || event.getWhoClicked().hasPermission("portablehorses.unsaddle")) {
             if (!storeArmor) {
                 if (horse.getInventory().getArmor() != null && horse.getInventory().getArmor().getType() != Material.AIR) {
                     horse.getWorld().dropItem(horse.getLocation(), horse.getInventory().getArmor());
                     horse.getInventory().setArmor(null);
                 }
             }
             if (!storeInventory && horse.isCarryingChest()) {
                 ItemStack toDrop;
                 for (int i=2;i<horse.getInventory().getContents().length;i++) {
                    toDrop = horse.getInventory().getItem(i);
                    horse.getWorld().dropItem(horse.getLocation(), toDrop);
                    horse.getInventory().setItem(i, null);
                 }
             }
             nmsHandler.saveToSaddle(horse, saddle);
             horse.remove();
         } else {
             event.setCancelled(true);
         }
     }
 
     public ItemStack getEmptyPortableHorseSaddle() {
         PlayerPickupItemEvent e;
 
         if (requireSpecialSaddle) {
             ItemStack s = new ItemStack(Material.SADDLE);
             ItemMeta meta = getServer().getItemFactory().getItemMeta(Material.SADDLE);
             meta.setDisplayName(nmsHandler.DISPLAY_NAME);
             List<String> lore = new LinkedList<String>();
             lore.add("empty");
             meta.setLore(lore);
             s.setItemMeta(meta);
             return s;
         } else {
             return new ItemStack(Material.SADDLE);
         }
     }
 
     private boolean isEmptyPortableHorseSaddle(ItemStack currentItem) {
         if (currentItem.getType() != Material.SADDLE) {
             return false;
         }
         if (requireSpecialSaddle) {
             if (!currentItem.hasItemMeta()) {
                 return false;
             }
             if (!nmsHandler.DISPLAY_NAME.equals(currentItem.getItemMeta().getDisplayName())) {
                 return false;
             }
             return currentItem.getItemMeta().hasLore() && "empty".equals(currentItem.getItemMeta().getLore().get(0));
         } else {
             return !isPortableHorseSaddle(currentItem);
         }
     }
 
     private boolean isPortableHorseSaddle(ItemStack currentItem) {
         if (currentItem != null && currentItem.getType().equals(Material.SADDLE)) {
             if (currentItem.hasItemMeta()) {
                 if (currentItem.getItemMeta().hasLore()) {
                     List<String> lore = currentItem.getItemMeta().getLore();
                     if (lore.size() >= 1 && lore.get(0).startsWith(nmsHandler.LORE_PREFIX) && lore.get(0).length() > nmsHandler.LORE_PREFIX.length()) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     @EventHandler
     public void onEntityDeath(EntityDeathEvent event) {
         if (event.getEntity() instanceof Horse) {
             Horse h = (Horse) event.getEntity();
             if (isPortableHorseSaddle(h.getInventory().getSaddle())) {
                 h.getInventory().setSaddle(getEmptyPortableHorseSaddle());
             }
         }
     }
 
     @EventHandler
     public void onClickSaddle(PlayerInteractEvent event) {
         if (event.getItem() != null && event.getItem().getType().equals(Material.SADDLE)) {
             if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isPortableHorseSaddle(event.getItem())) {
                 if (INTERACTIVE_BLOCKS.contains(event.getClickedBlock().getType())) {
                     return;
                 }
                 if (event.getPlayer().hasPermission("portablehorses.spawn")) {
                     Location spawnLoc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation();
                     Horse horse = (Horse) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.HORSE);
                     if (horse.isValid()) {
                         nmsHandler.restoreHorseFromSaddle(event.getItem(), horse);
                         horse.getInventory().setSaddle(event.getItem());
                         event.getPlayer().setItemInHand(null);
                     } else {
                         event.getPlayer().sendMessage("Sorry, you can't spawn a horse here.");
                     }
                 } else {
                     event.getPlayer().sendMessage("Sorry, you don't have permission to spawn a horse.");
                 }
             }
         }
     }
 
 
     public MaterialData getCraftingSupplement() {
 
         String mat = getConfig().getString("recipe-extra-item", "ENDER_PEARL");
         int data = -1;
         if (mat.contains(":")) {
             String[] parts = mat.split(":");
             mat = parts[0];
             data = Integer.parseInt(parts[1]);
         }
         Material material = null;
         try {
             material = Material.getMaterial(Integer.parseInt(mat));
         } catch (IllegalArgumentException ex) {
             material = Material.getMaterial(mat);
         }
         MaterialData md = new MaterialData(material);
         if (data != -1) {
             md.setData((byte) data);
         }
         return md;
     }
 }
