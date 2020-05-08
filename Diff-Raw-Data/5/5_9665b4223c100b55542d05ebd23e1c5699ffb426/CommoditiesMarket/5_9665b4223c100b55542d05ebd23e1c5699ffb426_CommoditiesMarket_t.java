 package net.deepbondi.minecraft.market;
 
 import com.avaje.ebean.EbeanServer;
 import com.iCo6.iConomy;
 import com.iCo6.system.Account;
 import com.iCo6.system.Accounts;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import net.deepbondi.minecraft.market.commands.AdminCommand;
 import net.deepbondi.minecraft.market.commands.BuyCommand;
 import net.deepbondi.minecraft.market.commands.PriceCheckCommand;
 import net.deepbondi.minecraft.market.commands.SellCommand;
 import net.deepbondi.minecraft.market.exceptions.CommoditiesMarketException;
 import net.deepbondi.minecraft.market.exceptions.NoSuchCommodityException;
 import net.deepbondi.minecraft.market.exceptions.NotReadyException;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginDisableEvent;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.EventExecutor;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import javax.persistence.PersistenceException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 public class CommoditiesMarket extends JavaPlugin {
     private static final Pattern COLON_PATTERN = Pattern.compile(":");
 
     private Accounts accounts;
     private PermissionHandler permissions;
     private final PriceModel model = new BondilandPriceModel();
     private int initialItemQty = 200;
 
     @Override
     public void onEnable() {
         loadConfig();
         setupDatabase();
         registerPluginListener();
 
         getCommand("commodities").setExecutor(new AdminCommand(this));
         getCommand("pricecheck").setExecutor(new PriceCheckCommand(this));
         getCommand("buy").setExecutor(new BuyCommand(this));
         getCommand("sell").setExecutor(new SellCommand(this));
     }
 
     @Override
     public void onDisable() {
         saveConfig();
     }
 
     private void setupDatabase() {
         try {
             getDatabase().find(Commodity.class).findRowCount();
             getDatabase().find(PlayerCommodityStats.class).findRowCount();
         } catch (PersistenceException ex) {
             installDDL();
         }
     }
 
     @Override
     public List<Class<?>> getDatabaseClasses() {
         final List<Class<?>> list = new ArrayList<Class<?>>();
         list.add(Commodity.class);
         list.add(PlayerCommodityStats.class);
         return list;
     }
 
     private void loadConfig() {
         try {
             final Configuration config = getConfig();
             initialItemQty = config.getInt("itemdefaults.instock", initialItemQty);
         } catch (Exception e) {
             final String pluginName = getDescription().getName();
             getServer()
                     .getLogger()
                     .severe("Exception while loading " + pluginName + "/config.yml");
         }
     }
 
     @Override
     public void saveConfig() {
         getConfig().set("itemdefaults.instock", initialItemQty);
         super.saveConfig();
     }
 
     private final PluginListener pl = new PluginListener();
 
     private class PluginListener implements EventExecutor {
         @Override
         public void execute(final Listener l, final Event e) {
             if (e instanceof PluginEnableEvent)
                 onPluginEnable((PluginEnableEvent) e);
             if (e instanceof PluginDisableEvent)
                 onPluginDisable((PluginDisableEvent) e);
         }
 
         public void onPluginEnable(final PluginEnableEvent event) {
             discover(event.getPlugin());
         }
 
         public void onPluginDisable(final PluginDisableEvent event) {
             undiscover(event.getPlugin());
         }
 
         void discover(final Plugin plugin) {
             discover(plugin, plugin);
         }
 
         void undiscover(final Plugin plugin) {
             discover(plugin, null);
         }
 
         private void discover(final Plugin plugin, final Object surrogate) {
             if (plugin instanceof iConomy) discoverEconomy();
             if (plugin instanceof Permissions) discoverPermissions((Permissions) surrogate);
         }
 
         void discoverEconomy() {
             accounts = new Accounts();
         }
 
         void discoverPermissions(final Permissions plugin) {
             permissions = plugin.getHandler();
         }
     }
 
     private void registerPluginListener() {
         final PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(PluginEnableEvent.class, new Listener() {
         }, EventPriority.MONITOR, pl, this);
 
         final Plugin ic = pm.getPlugin("iConomy");
         if (ic.isEnabled()) pl.discover(ic);
 
         final Plugin perms = pm.getPlugin("Permissions");
         if (perms.isEnabled()) pl.discover(perms);
     }
 
     public PriceModel getPriceModel() {
         return model;
     }
 
     public Account getAccount(final String name)
             throws NotReadyException {
         if (accounts == null)
             throw new NotReadyException(this, "iConomy is not yet enabled");
 
         return accounts.get(name);
     }
 
     private PermissionHandler getPermissions()
             throws NotReadyException {
         if (permissions != null) return permissions;
         throw new NotReadyException(this, "Permissions is not yet enabled");
     }
 
     public boolean hasPermission(final CommandSender sender, final String action)
             throws NotReadyException {
         return !(sender instanceof Player) || getPermissions().has((Player) sender, "commodities." + action);
     }
 
     private synchronized Commodity lookupCommodity(final Material material, final byte byteData) throws NoSuchCommodityException {
         final Material fudgedMaterial = fudgeMaterial(material);
 
         Commodity result = getDatabase().find(Commodity.class)
                 .where().eq("itemId", fudgedMaterial.getId())
                 .where().eq("byteData", byteData)
                 .findUnique();
 
         if (result == null) {
             // check without byteData, if it's unique accept it
             try {
                 result = getDatabase().find(Commodity.class)
                         .where().eq("itemId", fudgedMaterial.getId())
                         .findUnique();
             } catch (PersistenceException e) {
                 // ignore; it means the result wasn't unique
             }
         }
 
         if (result == null) {
             final String name = fudgedMaterial.name();
             final String description = byteData == 0
                     ? name
                     : name + ':' + byteData;
 
             throw new NoSuchCommodityException("Can't find commodity [" + ChatColor.WHITE + description + ChatColor.RED + ']');
         }
 
         return result;
     }
 
     private static Material fudgeMaterial(final Material original) {
         // when picking items by looking at them, we don't always get what the user expects.
         // for the cases we've run across or thought of, we fudge them here.
 
         //noinspection EnumSwitchStatementWhichMissesCases
         switch (original) {
             case BED_BLOCK:
                 return Material.BED;
             case LAVA:
             case STATIONARY_LAVA:
                 return Material.LAVA_BUCKET;
             case REDSTONE_LAMP_ON:
                 return Material.REDSTONE_LAMP_OFF;
             case REDSTONE_TORCH_ON:
                 return Material.REDSTONE_TORCH_OFF;
             case PISTON_EXTENSION:
             case PISTON_MOVING_PIECE:
                 return Material.PISTON_BASE;
             case SIGN_POST:
             case WALL_SIGN:
                 return Material.SIGN;
             case STATIONARY_WATER:
             case WATER:
                 return Material.WATER_BUCKET;
             default:
                 return original;
         }
     }
 
     public Commodity lookupCommodity(final CommandSender sender, final String name) throws NoSuchCommodityException {
         // Accept a few context-dependent commodity name strings in addition to those recognized without a CommandSender
         if (sender instanceof Player) {
             final Player player = (Player) sender;
 
             if (name.toLowerCase().equals("this")) {
                 final ItemStack thisStack = player.getItemInHand();
                 if (thisStack != null && thisStack.getType() != Material.AIR) {
                     return lookupCommodity(thisStack.getType(), thisStack.getData().getData());
                 } else {
                     throw new NoSuchCommodityException("You don't appear to be holding anything");
                 }
             } else if (name.toLowerCase().equals("that")) {
                 final Block thatBlock = player.getTargetBlock(null, 100);
 
                 if (thatBlock != null && thatBlock.getType() != Material.AIR) {
                     return lookupCommodity(thatBlock.getType(), thatBlock.getData());
                 } else {
                     throw new NoSuchCommodityException("You don't appear to be pointing at anything");
                 }
             }
         }
 
         return lookupCommodity(name);
     }
 
     private synchronized Commodity lookupCommodity(final String name) throws NoSuchCommodityException {
         // Accept "names" in several forms:
         // Material:byte and associated forms like in ScrapBukkit's /give
         // Commodity name from database
         final Commodity commodity = getDatabase()
                 .find(Commodity.class)
                 .where().ieq("name", name)
                 .findUnique();
 
         if (commodity != null) {
             return commodity;
         }
 
         final String[] parts = COLON_PATTERN.split(name);
         final Material material;
         byte byteData = 0;
 
         switch (parts.length) {
             case 2:
                 try {
                     byteData = (byte) Integer.parseInt(parts[1]);
                 } catch (NumberFormatException e) {
                     break;
                 }
 
                 // fall through
                 //noinspection fallthrough
             case 1:
                 material = Material.matchMaterial(parts[0]);
 
                 if (material == null) break;
 
                 return lookupCommodity(material, byteData);
         }
 
        throw new NoSuchCommodityException("Can't find commodity [" + ChatColor.WHITE + name + ChatColor.RED + ']');
     }
 
     public synchronized void addCommodity(final String name, final Material material, final byte byteData) throws CommoditiesMarketException {
         final EbeanServer db = getDatabase();
         db.beginTransaction();
         try {
             // Check if commodity already exists
             boolean exists = true;
             try {
                 lookupCommodity(name);
             } catch (NoSuchCommodityException e) {
                 exists = false;
             }
 
             if (exists) {
                 throw new CommoditiesMarketException("Commodity already exists.");
             } else {
                 final Commodity item = new Commodity();
 
                 item.setName(name);
                 item.setItemId(material.getId());
                 item.setByteData(byteData);
                 item.setInStock(initialItemQty);
 
                 db.save(item);
             }
 
             db.commitTransaction();
         } finally {
             db.endTransaction();
         }
     }
 
     public synchronized void adjustStock(final String name, final long stockChange) throws CommoditiesMarketException {
         final EbeanServer db = getDatabase();
         db.beginTransaction();
         try {
             final Commodity item = lookupCommodity(name);
             final long stock = item.getInStock() + stockChange;
             if (stock < 0) {
                 throw new CommoditiesMarketException("Stock level cannot be reduced past zero.");
             }
 
             item.setInStock(stock);
             db.update(item, adjStkUpdateProps);
 
             db.commitTransaction();
         } finally {
             db.endTransaction();
         }
     }
 
     private static final Set<String> pcsUpdateProps;
     private static final Set<String> adjStkUpdateProps;
 
     static {
         pcsUpdateProps = new HashSet<String>();
         pcsUpdateProps.add("numBought");
         pcsUpdateProps.add("numSold");
         pcsUpdateProps.add("moneySpent");
         pcsUpdateProps.add("moneyGained");
 
         adjStkUpdateProps = new HashSet<String>();
         adjStkUpdateProps.add("inStock");
     }
 
     private PlayerCommodityStats lookupPlayerCommodityStats(final String playerName, final int commodityId) {
         return getDatabase().find(PlayerCommodityStats.class).where()
                 .ieq("playerName", playerName)
                 .eq("commodityId", commodityId)
                 .findUnique();
     }
 
     public synchronized void recordPlayerCommodityStats(
             final Player player,
             final Commodity item,
             final long numBought,
             final long numSold,
             final double moneySpent,
             final double moneyGained) {
         final EbeanServer db = getDatabase();
 
         db.beginTransaction();
 
         try {
             final PlayerCommodityStats existing = lookupPlayerCommodityStats(player.getName(), item.getId());
 
             if (existing != null) {
                 existing.update(numBought, numSold, moneySpent, moneyGained);
 
                 db.update(existing, pcsUpdateProps);
             } else {
                 final PlayerCommodityStats stats = new PlayerCommodityStats(
                         player.getName(), item.getId(),
                         numBought, numSold, moneySpent, moneyGained);
 
                 db.save(stats);
             }
 
             db.commitTransaction();
         } finally {
             db.endTransaction();
         }
     }
 }
 
