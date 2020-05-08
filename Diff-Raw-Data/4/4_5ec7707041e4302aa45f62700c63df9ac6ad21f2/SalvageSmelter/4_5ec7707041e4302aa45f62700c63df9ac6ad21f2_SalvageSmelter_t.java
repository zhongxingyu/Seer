 package com.norcode.bukkit.salvagesmelter;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Furnace;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.FurnaceBurnEvent;
 import org.bukkit.event.inventory.FurnaceSmeltEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryMoveItemEvent;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.FurnaceInventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SalvageSmelter extends JavaPlugin implements Listener {
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         loadConfig();
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     private HashMap<Material, SmeltRecipe> recipeMap = new HashMap<Material, SmeltRecipe>();
     private boolean worldWhitelist = true; // blacklist if false
     private HashSet<String> worldList = new HashSet<String>();
 
     private boolean debugMode = false;
     
     public ItemStack parseResultStack(String s) {
         String[] parts = s.split(":");
         Material mat = Material.valueOf(parts[0].toUpperCase());
         short data = 0;
         int qty = 1;
         if (parts.length == 3) {
             data = Short.parseShort(parts[1]);
             qty = Integer.parseInt(parts[2]);
         } else {
             qty = Integer.parseInt(parts[1]);
         }
         if (qty > mat.getMaxStackSize()) {
             getLogger().warning("Recipe Result Cannot exceed Max stack size, setting to " + mat.getMaxStackSize());
             qty = mat.getMaxStackSize();
         }
         return new ItemStack(mat, qty, data);
     }
 
     public boolean enabledInWorld(World w) {
         boolean enabled = ((worldWhitelist && worldList.contains(w.getName().toLowerCase())) || 
                 (!worldWhitelist && !worldList.contains(w.getName().toLowerCase())));
         return enabled;
     }
 
     public void loadConfig() {
         String listtype = getConfig().getString("world-selection", "whitelist").toLowerCase();
         debugMode = getConfig().getBoolean("debug", true);
         getLogger().info("Debugging is " + (debugMode ? "on" : "off") + ".");
         if (listtype.equals("blacklist")) {
             this.worldWhitelist = false;
         } else {
             this.worldWhitelist = true;
         }
         this.worldList.clear();
         for (String wn: getConfig().getStringList("world-list")) {
             this.worldList.add(wn.toLowerCase());
         }
         ConfigurationSection cfg = getConfig().getConfigurationSection("recipes");
         for (String key: cfg.getKeys(true)) {
             Material mat = Material.valueOf(key);
             ItemStack result = parseResultStack(cfg.getString(key));
             if (mat != null && result != null) {
                 SmeltRecipe sr = new SmeltRecipe(mat, result);
                 sr.installFurnaceRecipe(this);
                 recipeMap.put(sr.getSmeltable(), sr);
             }
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
         if (args[0].equalsIgnoreCase("reload")) {
             reloadConfig();
             loadConfig();
             sender.sendMessage(ChatColor.GOLD + "[SalvageSmelter] " + ChatColor.WHITE + "Configuration Reloaded.");
             return true;
         } else if (args[0].equalsIgnoreCase("debug")) {
             debugMode = !debugMode;
             sender.sendMessage(ChatColor.GOLD + "[SalvageSmelter] " + ChatColor.WHITE + "Debug mode is now " + (debugMode ? ChatColor.DARK_GREEN + "on" : ChatColor.DARK_RED + "off") + ".");
             return true;
         }
         return false;
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onInventoryMoveItem(InventoryMoveItemEvent event) {
         if (event.getDestination().getHolder() instanceof Furnace) {
            Furnace f = (Furnace) event.getDestination().getHolder();
             if (recipeMap.containsKey(event.getItem().getType())) {
                 if (!enabledInWorld(f.getWorld())) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGH)
     public void onSmelt(FurnaceSmeltEvent event) {
         ItemStack orig = event.getSource();
         if (debugMode) {
             getLogger().info("SmeltEvent::Source: " + orig);
         }
         if (!enabledInWorld(event.getBlock().getWorld())) {
             if (recipeMap.containsKey(orig.getType())) {
                 event.setCancelled(true);
             }
             return;
         }
        if (!recipeMap.containsKey(orig.getType())) return;
         double percentage = (orig.getType().getMaxDurability() - orig.getDurability()) / (double) orig.getType().getMaxDurability();
         if (debugMode) {
             getLogger().info("SmeltEvent::Damage:" + orig);
         }
         ItemStack result = getSalvage(orig.getType(), event.getResult().getType(), percentage);
         if (result == null || result.getAmount() == 0) {
             event.setResult(new ItemStack(Material.COAL, 1, (short)1));
         } else {
             event.setResult(result);
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onInventoryClick(InventoryClickEvent event) {
         if (event.getInventory().getType().equals(InventoryType.FURNACE)) {
             boolean needsUpdate = event.isShiftClick();
             if (event.getRawSlot() == 0 && !event.isShiftClick()) {
                 if (recipeMap.containsKey(event.getCursor().getType()) && !enabledInWorld(((Furnace) event.getInventory().getHolder()).getWorld())) {
                     if (debugMode) {
                         getLogger().info("disabled in this world");
                     }
                     event.setCancelled(true);
                     needsUpdate = true;
                 }
             }
             if (event.isShiftClick()) {
                 if (recipeMap.containsKey(event.getCurrentItem().getType()) && !enabledInWorld(((Furnace) event.getInventory().getHolder()).getWorld())) {
                     if (debugMode) {
                         getLogger().info("disabled in this world");
                     }
                     event.setCancelled(true);
                     needsUpdate = true;
                 }
             }
             if (needsUpdate) {
                 final Player p = (Player) event.getWhoClicked();
                 getServer().getScheduler().runTaskLater(this, new Runnable() {
                     public void run() {
                         p.updateInventory();
                     }
                 }, 0);
                 
             }
         }
     }
     
     public ItemStack getSalvage(Material product, Material raw, double damagePct) {
         if (debugMode) {
             getLogger().info("getSalvage(" + product + ", " + raw + ", " + damagePct + ")");
         }
         SmeltRecipe recipe = recipeMap.get(product);
         if (raw.equals(recipe.getResult().getType())) {
             int amt = recipe.getResult().getAmount();
             int max = amt;
             amt = (int)(amt * damagePct);
             if (debugMode) {
                 getLogger().info("getSalvage::Mathification:" + max + " * " + damagePct + " = " + amt);
             }
             ItemStack stack = recipe.getResult().clone();
             if (amt == 0) {
                 stack = new ItemStack(Material.COAL,1,(short)1);
             } else {
                 stack.setAmount(amt);
             }
             return stack;
         }
         return null;
     }
 }
