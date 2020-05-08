 package com.norcode.bukkit.buildinabox;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.h31ix.anticheat.Anticheat;
 import net.h31ix.anticheat.api.AnticheatAPI;
 import net.h31ix.anticheat.manage.CheckType;
 import net.h31ix.updater.Updater;
 import net.h31ix.updater.Updater.UpdateType;
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 import org.bukkit.scheduler.BukkitTask;
 
 import com.norcode.bukkit.buildinabox.datastore.DataStore;
 import com.norcode.bukkit.buildinabox.datastore.EbeanDataStore;
 import com.norcode.bukkit.buildinabox.datastore.EbeanDataStore.ChestBean;
 import com.norcode.bukkit.buildinabox.datastore.YamlDataStore;
 import com.norcode.bukkit.buildinabox.listeners.BlockProtectionListener;
 import com.norcode.bukkit.buildinabox.listeners.ItemListener;
 import com.norcode.bukkit.buildinabox.listeners.PlayerListener;
 import com.norcode.bukkit.buildinabox.util.MessageFile;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 
 import fr.neatmonster.nocheatplus.NoCheatPlus;
 import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
 
 
 public class BuildInABox extends JavaPlugin implements Listener {
     public static String LORE_PREFIX = ChatColor.DARK_GREEN + "" + ChatColor.DARK_RED + "" + ChatColor.DARK_GRAY + "" + ChatColor.DARK_BLUE;
     public static String LORE_HEADER = ChatColor.GOLD + "Build-in-a-Box";
     public static int BLOCK_ID = 130;
     private static BuildInABox instance;
     private DataStore datastore = null;
     private Updater updater = null;
     private boolean debugMode = false;
     private BukkitTask inventoryScanTask;
     private MessageFile messages = null;
     private Economy economy = null;
     private Anticheat antiCheat;
     private NoCheatPlus NCP;
     @Override
     public void onLoad() {
         instance = this;
     }
 
     public void onUnload() {
         instance = null;
     }
 
     @Override
     public void onEnable() {
         saveDefaultConfig();
         reloadConfig();
         enableEconomy();
         setupAntiCheat();
         debugMode = getConfig().getBoolean("debug", false);
         BLOCK_ID = getConfig().getInt("chest-block", 130);
         loadMessages();
         LORE_HEADER = getMsg("display-name"); 
         doUpdater();
         new File(getDataFolder(), "schematics").mkdir();
         if (initializeDataStore()) {
             getServer().getPluginCommand("biab").setExecutor(new BIABCommandExecutor(this));
             getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
             getServer().getPluginManager().registerEvents(new ItemListener(this), this);
             if (getConfig().getBoolean("protect-buildings")) {
                 getServer().getPluginManager().registerEvents(new BlockProtectionListener(), this);
             }
         }
         if (getConfig().getBoolean("carry-effect", false)) {
             inventoryScanTask = getServer().getScheduler().runTaskTimer(this, new Runnable() {
                 List<String> playerNames = null;
                 int listIdx = 0;
                 public void run() {
                     if (playerNames == null || listIdx >= playerNames.size()) {
                         playerNames = new ArrayList<String>();
                         for (Player p: getServer().getOnlinePlayers()) {
                             playerNames.add(p.getName());
                             listIdx = 0;
                         }
                     }
                     if (listIdx < playerNames.size()) {
                         Player p = getServer().getPlayer(playerNames.get(listIdx));
                         if (p != null && p.isOnline() && !p.isDead()) {
                             checkCarrying(p);
                         }
                         listIdx++;
                     }
                     
                 }
             }, 20, 20);
         }
     }
 
     private void setupAntiCheat() {
         if(getServer().getPluginManager().getPlugin("AntiCheat") != null)
         {
             antiCheat = (Anticheat) getServer().getPluginManager().getPlugin("AntiCheat");
         }
         if (getServer().getPluginManager().getPlugin("NoCheatPlus") != null) {
             NCP = (NoCheatPlus) getServer().getPluginManager().getPlugin("NoCheatPlus");
         }
     }
 
     public void exemptPlayer(Player p) {
         if (antiCheat != null) {
             AnticheatAPI.exemptPlayer(p, CheckType.FAST_PLACE);
             AnticheatAPI.exemptPlayer(p, CheckType.LONG_REACH);
         }
         if (NCP != null) {
             NCPExemptionManager.exemptPermanently(p, fr.neatmonster.nocheatplus.checks.CheckType.BLOCKPLACE);
         }
     }
 
     public void unexemptPlayer(Player p) {
         if (antiCheat != null) {
             AnticheatAPI.unexemptPlayer(p, CheckType.FAST_PLACE);
             AnticheatAPI.unexemptPlayer(p, CheckType.LONG_REACH);
         }
         if (NCP != null) {
             NCPExemptionManager.unexempt(p, fr.neatmonster.nocheatplus.checks.CheckType.BLOCKPLACE);
         }
     }
 
 
     private void enableEconomy() {
         if (getServer().getPluginManager().getPlugin("Vault") == null) {
             return;
         }
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
             BuildInABox.getInstance().debug("Found Vault!");
         }
     }
 
     public static Economy getEconomy() {
         return instance.economy;
     }
 
     public static boolean hasEconomy() {
         return instance.economy != null;
     }
 
     private void loadMessages() {
         String lang = getConfig().getString("language", "english").toLowerCase();
         File tDir = new File(getDataFolder(), "lang");
         if (!tDir.exists()) {
             tDir.mkdir();
         }
         messages = new MessageFile(this, "lang/" + lang + ".yml");
         messages.saveDefaultConfig();
         messages.reloadConfig();
         FileConfiguration cfg = messages.getConfig();
         cfg.options().copyDefaults(true);
         messages.saveConfig();
         messages.reloadConfig();
     }
 
     public static String getMsg(String key, Object... args) {
         String tpl = instance.messages.getConfig().getString(key);
         if (tpl == null) {
             tpl = "[" + key + "] ";
             for (int i=0;i< args.length;i++) {
                 tpl += "{"+i+"}, ";
             }
         }
         return new MessageFormat(convertColors(tpl)).format(args);
     }
 
     public void removeCarryEffect(Player p) {
         p.removeMetadata("biab-carryeffect", getInstance());
         p.removePotionEffect(PotionEffectType.getByName(getConfig().getString("carry-effect-type")));
     }
 
     public boolean hasCarryEffect(Player p) {
         return p.hasMetadata("biab-carryeffect");
     }
 
     public void applyCarryEffect(Player p) {
         p.setMetadata("biab-carryeffect", new FixedMetadataValue(getInstance(), true));
         p.addPotionEffect(new PotionEffect(PotionEffectType.getByName(getConfig().getString("carry-effect-type")), 1200, 1));
     }
 
     private boolean initializeDataStore() {
         String storageType = getConfig().getString("storage-backend", "file").toLowerCase();
         if (storageType.equals("file")) {
             datastore = new YamlDataStore(this);
         } else if (storageType.equals("ebean")) {
             datastore = new EbeanDataStore(this);
         } else {
            getLogger().severe("No datastore configured.");
             return false;
         }
         datastore.load();
         long now = System.currentTimeMillis();
         long expiry = getConfig().getLong("data-expiry", 1000*60*60*24*90L);
         long tooOldTime = now - expiry;// if the chest hasn't been touched in 90 days expire the data
         HashSet<Chunk> loadedChunks = new HashSet<Chunk>();
         for (ChestData cd: new ArrayList<ChestData>(datastore.getAllChests())) {
             debug("Checking Chest: " + cd.getId());
             if (cd.getLastActivity() < tooOldTime) {
                 debug("Chest Data is too old: " + cd.getLastActivity() + " vs " + tooOldTime);
                 datastore.deleteChest(cd.getId());
             } else {
                 if (cd.getLocation() != null) {
                     BuildChest bc = new BuildChest(cd);
                     if (bc.getBlock().getTypeId() == BLOCK_ID) {
                         bc.getBlock().setMetadata("buildInABox", new FixedMetadataValue(this, bc));
                         if (getConfig().getBoolean("protect-buildings")) {
                             debug("Protecting Building: " + bc);
                             loadedChunks.addAll(bc.protectBlocks());
                         }
                     }
                 }
             }
         }
         for (Chunk c: loadedChunks) {
             c.getWorld().unloadChunkRequest(c.getX(), c.getZ(), true);
         }
         return true;
     }
 
     @Override
     public void onDisable() {
         getDataStore().save();
         if (inventoryScanTask != null) {
             inventoryScanTask.cancel();
         }
     }
 
     @EventHandler(ignoreCancelled=true)
     public void onPlayerLogin(PlayerLoginEvent event) {
         if (event.getPlayer().hasPermission("biab.admin")) {
             final String playerName = event.getPlayer().getName();
             getServer().getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
                 public void run() {
                     Player player = getServer().getPlayer(playerName);
                     if (player != null && player.isOnline()) {
                         debug("Updater Result: " + updater.getResult());
                         switch (updater.getResult()) {
                         case UPDATE_AVAILABLE:
                             player.sendMessage(getNormalMsg("update-available", "http://dev.bukkit.org/server-mods/build-in-a-box/"));
                             break;
                         case SUCCESS:
                             player.sendMessage(getNormalMsg("update-downloaded"));
                             break;
                         default:
                             // nothing
                         }
                     }
                 }
             }, 20);
         }
     }
 
     static final Pattern colorPattern = Pattern.compile("(&[0-9a-flmnor])", Pattern.CASE_INSENSITIVE);
     public static String convertColors(String s) {
         Matcher m = colorPattern.matcher(s);
         StringBuffer sb = new StringBuffer();
         while (m.find())
             m.appendReplacement(sb, ChatColor.COLOR_CHAR + m.group(1).substring(1));
         m.appendTail(sb);
         return sb.toString();
     }
 
     public void doUpdater() {
         String autoUpdate = getConfig().getString("auto-update", "notify-only").toLowerCase();
         if (autoUpdate.equals("true")) {
             updater = new Updater(this, "build-in-a-box", this.getFile(), UpdateType.DEFAULT, true);
         } else if (autoUpdate.equals("false")) {
             getLogger().info("Auto-updater is disabled.  Skipping check.");
         } else {
             updater = new Updater(this, "build-in-a-box", this.getFile(), UpdateType.NO_DOWNLOAD, true);
         }
     }
 
     public WorldEditPlugin getWorldEdit() {
         return (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
     }
 
     public void debug(String s) {
         if (debugMode) {
             getLogger().info(s);
         }
     }
 
     public DataStore getDataStore() {
         return datastore;
     }
 
     public static BuildInABox getInstance() {
         return instance;
     }
 
     public void checkCarrying(Player p) {
         if (!getConfig().getBoolean("carry-effect", false)) return;
         ChestData data;
         boolean effect = false;
         for (ItemStack stack: p.getInventory().getContents()) {
             data = getDataStore().fromItemStack(stack);
             if (data != null) {
                 effect = true;
                 break;
             }
         }
         if (effect) {
             applyCarryEffect(p);
         } else if (hasCarryEffect(p)) {
             removeCarryEffect(p);
         }
     }
 
     public static String getNormalMsg(String key, Object... args) {
         return getMsg("message-prefix", LORE_HEADER) + ChatColor.GRAY + getMsg(key, args);
     }
 
     public static String getErrorMsg(String key, Object... args) {
         return getMsg("message-prefix", LORE_HEADER) + ChatColor.RED + getMsg(key, args);
     }
 
     public static String getSuccessMsg(String key, Object... args) {
         return getMsg("message-prefix", LORE_HEADER) + ChatColor.GREEN + getMsg(key, args);
     }
 
    public void installDDL() {
       super.installDDL();
    }
 
    @Override
     public List<Class<?>> getDatabaseClasses() {
        List<Class<?>> dbClasses = new ArrayList<Class<?>>();
        dbClasses.add(ChestBean.class);
        return dbClasses;
     } 
 }
