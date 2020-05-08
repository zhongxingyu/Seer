 package com.dumptruckman.bartersigns.listener;
 
 import com.dumptruckman.bartersigns.BarterSignsPlugin;
 import com.dumptruckman.bartersigns.config.ConfigPath;
 import com.dumptruckman.bartersigns.locale.LanguagePath;
 import com.dumptruckman.bartersigns.sign.BarterSign;
 import com.dumptruckman.bartersigns.sign.BarterSignManager;
 import com.palmergames.bukkit.towny.object.TownBlockType;
 import com.palmergames.bukkit.towny.object.TownyUniverse;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockBurnEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.SignChangeEvent;
 
 /**
  * @author dumptruckman
  */
 public class BarterSignsBlockListener implements Listener {
 
     private BarterSignsPlugin plugin;
 
     public BarterSignsBlockListener(BarterSignsPlugin plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onSignChange(SignChangeEvent event) {
         // Throw out unimportant events immediately
         if (event.isCancelled()) return;
         BarterSignManager.remove(event.getBlock());
         if (!event.getLine(0).equalsIgnoreCase("[Barter]") && !event.getLine(0).equalsIgnoreCase("Barter Shop")) return;
 
         if (plugin.config.getConfig().getBoolean(ConfigPath.USE_PERMS.getPath(), (Boolean) ConfigPath.USE_PERMS.getDefault())
                 && !event.getPlayer().hasPermission("bartersigns.create")) {
             plugin.sendMessage(event.getPlayer(), LanguagePath.NO_PERMISSION.getPath());
             return;
         }
 
         if (plugin.towny != null && plugin.config.getConfig().getBoolean(ConfigPath.TOWNY_SHOP_PLOTS.getPath(), (Boolean) ConfigPath.TOWNY_SHOP_PLOTS.getDefault())) {
             Location loc = event.getBlock().getLocation();
             try {
                 if (TownyUniverse.getTownBlock(loc).getType() != TownBlockType.COMMERCIAL) {
                     plugin.sendMessage(event.getPlayer(), LanguagePath.SHOP_PLOT_ONLY.getPath());
                     return;
                 }
             } catch (Exception ignore) {}
         }
 
         BarterSign barterSign = BarterSignManager.getBarterSignFromBlock(event.getBlock());
         if (barterSign == null) {
             barterSign = new BarterSign(plugin, event.getBlock());
         }
         barterSign.init(event.getPlayer());
         plugin.signAndMessage(event, event.getPlayer(),
                 plugin.lang.lang(LanguagePath.SIGN_STOCK_SETUP.getPath(), event.getPlayer().getName()));
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled()) return;
         Block block = event.getBlockAgainst();
         if (!(block.getState() instanceof Sign)) return;
         if (!BarterSign.exists(plugin, block)) return;
         event.setCancelled(true);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockBreak(final BlockBreakEvent event) {
         //if (event.isCancelled()) return;
 
         Block block = event.getBlock();
         if (!(block.getState() instanceof Sign)) return;
 
         if (!BarterSign.exists(plugin, block)) return;
 
         BarterSign sign = BarterSignManager.getBarterSignFromBlock(event.getBlock());
         if (sign == null) {
             sign = new BarterSign(plugin, block);
         }
         final BarterSign barterSign = sign;
 
         if (!barterSign.isReady()) return;
         
         if (barterSign.getMenuIndex() != barterSign.REMOVE) {
             event.setCancelled(true);
         }
 
         if (!event.isCancelled() && plugin.config.getConfig().getBoolean(ConfigPath.SIGN_DROPS_ITEMS.getPath(),
                 (Boolean) ConfigPath.SIGN_DROPS_ITEMS.getDefault())) {
             barterSign.drop();
             return;
         }
 
         if (event.isCancelled()) {
             if (BarterSign.SignPhase.READY.equalTo(barterSign.getPhase())) {
                 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                     @Override
                     public void run() {
                         event.getPlayer().sendBlockChange(barterSign.getLocation(), 0, (byte) 0);
                         barterSign.showMenu(null);
                     }
                 }, 20L);
             } else {
                 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                     @Override
                     public void run() {
                         event.getPlayer().sendBlockChange(barterSign.getLocation(), 0, (byte) 0);
                         plugin.signAndMessage(barterSign.getSign(), event.getPlayer(),
                                 LanguagePath.SIGN_STOCK_SETUP.getPath(), barterSign.getOwner());
                         barterSign.getBlock().getState().update(true);
                     }
                 }, 20L);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockDamage(BlockDamageEvent event) {
         //if (event.isCancelled()) return;
 
         Block block = event.getBlock();
         if (!(block.getState() instanceof Sign)) return;
 
         if (!BarterSign.exists(plugin, block)) return;
 
         BarterSign barterSign = BarterSignManager.getBarterSignFromBlock(event.getBlock());
         if (barterSign == null) {
             barterSign = new BarterSign(plugin, block);
         }
        if (!barterSign.isReady()) return;
        if (barterSign.getMenuIndex() != barterSign.REMOVE) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockBurn(BlockBurnEvent event) {
         if (event.isCancelled()) return;
 
         Block block = event.getBlock();
         if (!(block.getState() instanceof Sign)) return;
 
         if (!BarterSign.exists(plugin, block)) return;
 
         event.setCancelled(plugin.config.getConfig().getBoolean(ConfigPath.SIGN_INDESTRUCTIBLE.getPath(),
                 (Boolean) ConfigPath.SIGN_INDESTRUCTIBLE.getDefault()));
 
         if (!event.isCancelled() && plugin.config.getConfig().getBoolean(ConfigPath.SIGN_DROPS_ITEMS.getPath(),
                 (Boolean) ConfigPath.SIGN_DROPS_ITEMS.getDefault())) {
             BarterSign barterSign = BarterSignManager.getBarterSignFromBlock(event.getBlock());
             if (barterSign == null) {
                 return;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockFade(BlockFadeEvent event) {
         if (event.isCancelled()) return;
 
         Block block = event.getBlock();
         if (!(block.getState() instanceof Sign)) return;
 
         if (!BarterSign.exists(plugin, block)) return;
 
         event.setCancelled(plugin.config.getConfig().getBoolean(ConfigPath.SIGN_INDESTRUCTIBLE.getPath(),
                 (Boolean) ConfigPath.SIGN_INDESTRUCTIBLE.getDefault()));
 
         if (!event.isCancelled() && plugin.config.getConfig().getBoolean(ConfigPath.SIGN_DROPS_ITEMS.getPath(),
                 (Boolean) ConfigPath.SIGN_DROPS_ITEMS.getDefault())) {
             BarterSign barterSign = BarterSignManager.getBarterSignFromBlock(event.getBlock());
             if (barterSign == null) {
                 return;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockPhysics(BlockPhysicsEvent event) {
         if (event.isCancelled()) return;
 
         Block block = event.getBlock();
         if (!(block.getState() instanceof Sign)) return;
 
         if (!BarterSign.exists(plugin, block)) return;
 
         event.setCancelled(true);
 
         if (!event.isCancelled() && plugin.config.getConfig().getBoolean(ConfigPath.SIGN_DROPS_ITEMS.getPath(),
                 (Boolean) ConfigPath.SIGN_DROPS_ITEMS.getDefault())) {
             BarterSign barterSign = BarterSignManager.getBarterSignFromBlock(event.getBlock());
             if (barterSign == null) {
                 return;
             }
         }
     }
 }
