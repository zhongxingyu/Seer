 package net.krinsoft.killsuite;
 
 import com.fernferret.allpay.AllPay;
 import com.fernferret.allpay.GenericBank;
 import com.pneumaticraft.commandhandler.CommandHandler;
 import net.krinsoft.killsuite.commands.*;
 import net.krinsoft.killsuite.listeners.EntityListener;
 import net.krinsoft.killsuite.listeners.PlayerListener;
 import net.krinsoft.killsuite.listeners.ServerListener;
 import net.krinsoft.killsuite.listeners.WorldListener;
 import net.krinsoft.killsuite.util.RewardGenerator;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author krinsdeath
  */
 public class KillSuite extends JavaPlugin {
     private static KillSuite plugin;
 
     private boolean debug = false;
     private boolean economy = false;
     private boolean contract = false;
     private boolean report = true;
     private List<String> worlds = new ArrayList<String>();
     private boolean profile = false;
     private int saveTask;
     private int profileTask;
     private int econTask;
 
     private FileConfiguration configuration;
     private File configFile;
 
     private FileConfiguration leaderboards;
     private File leaderFile;
 
     private GenericBank bank;
 
     private CommandHandler commandHandler;
     
     private Manager manager;
 
     private boolean deathcounter;
 
     @Override
     public void onEnable() {
         plugin = this;
         long startup = System.currentTimeMillis();
 
         // simple notices registration
         getServer().getMessenger().registerOutgoingPluginChannel(this, "SimpleNotice");
 
         registerConfig();
 
         if (!deathcounter) {
             if (new File("plugins/DeathCounter/users.db").renameTo(new File(getDataFolder(), "users.db"))) {
                 log("Successfully imported SQLite database.");
             }
             if (new File("plugins/DeathCounter/users.yml").renameTo(new File(getDataFolder(), "users.yml"))) {
                 log("Successfully imported YAML database.");
             }
             if (new File("plugins/DeathCounter/config.yml").renameTo(new File(getDataFolder(), "config.yml"))) {
                 log("Imported DeathCounter config file.");
             }
             registerConfig(true);
             deathcounter = true;
             getConfig().set("plugin.imported", true);
             saveConfig();
         }
 
         // register all the players
         manager = new Manager(this);
 
         // register economy features
         if (economy) {
             if (validateAllPay()) {
                 debug("Economy successfully hooked.");
                 generateRewards();
             }
         }
 
         registerCommands();
 
         // event listeners
 
         EntityListener eListener = new EntityListener(this);
         PlayerListener pListener = new PlayerListener(this);
         ServerListener sListener = new ServerListener(this);
         WorldListener wListener = new WorldListener(this);
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(eListener, this);
         pm.registerEvents(pListener, this);
         pm.registerEvents(sListener, this);
         pm.registerEvents(wListener, this);
 
         saveTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 getManager().save();
                 saveLeaders();
             }
         }, 300L, 300L * 20L);
 
         profileTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 if (profileList.size() > 0) {
                     profile(profileList, profile);
                     profileList.clear();
                 }
             }
         }, 1L, 1L);
 
         econTask = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             @Override
             public void run() {
                 if (transactions.size() > 0) {
                     transact(transactions);
                     transactions.clear();
                 }
             }
         }, 1L, 1L);
         
         log("Enabled successfully. (" + (System.currentTimeMillis() - startup) + "ms)");
     }
 
     @Override
     public void onDisable() {
         saveLeaders();
         getServer().getScheduler().cancelTasks(this);
         getServer().getScheduler().cancelTask(saveTask);
         getServer().getScheduler().cancelTask(profileTask);
         getServer().getScheduler().cancelTask(econTask);
         manager.save();
         manager.disable();
         manager = null;
         bank = null;
         log("Disabled successfully.");
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (label.equalsIgnoreCase("contract")) {
             sender.sendMessage(ChatColor.RED + "Not yet implemented.");
             return false;
         }
         List<String> arguments = new ArrayList<String>(Arrays.asList(args));
         arguments.add(0, label);
         return commandHandler.locateAndRunCommand(sender, arguments);
     }
 
     /**
      * Forces a reload of KillSuite's configuration file
      * @param val true will reload the files; false does nothing
      */
     public void registerConfig(boolean val) {
         if (val) {
             configFile = null;
             configuration = null;
             registerConfig();
         }
     }
 
     void registerConfig() {
         configFile = new File(getDataFolder(), "config.yml");
         if (!configFile.exists()) {
             getConfig().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/config.yml")));
             getConfig().options().copyDefaults(true);
             getConfig().options().header("#########\n" +
                     "#\n" +
                     "# Database:\n" +
                     "#   Available types are YAML, SQLite and MySQL\n" +
                     "#   Database is used for SQLite and MySQL only\n" +
                     "#   YAML entires are stored in users.yml in plugins/DeathCounter/\n" +
                     "#\n" +
                     "# Economy:\n" +
                     "#   players->realism:\n" +
                     "#     If this is true, the money awarded to the killer will be deducted\n" +
                     "#     from the killed player's wallet (default false)\n" +
                     "#   players->percentage:\n" +
                     "#     If this is true, the money awarded to the killer will be a percentage\n" +
                     "#     of the killed player's wallet (default true)\n" +
                     "#   diminish->depth:\n" +
                     "#     For every point underneath this value a player is underground, the 'return' value\n" +
                     "#     will be deducted (as a percentage) from the total payout for that kill\n" +
                     "#   diminish->return:\n" +
                     "#     A value to deduct per kill per point of depth (as a percentage) from each kill\n" +
                     "#     Maximum of 100\n" +
                     "#\n" +
                     "# Contracts:\n" +
                     "#   If contracts is set to true, players will be able to create 'kill' contracts\n" +
                     "#   to have other players killed. When a player with a contract on his head is killed,\n" +
                     "#   the money from the contract is automatically deducted from the contract owner's\n" +
                     "#   account and added to the killer's account, in addition to the normal kill reward. (default true)\n" +
                     "#   Fee:\n" +
                     "#     Fee determines how much it costs to create a contract to kill a player.\n" +
                     "#   Max:\n" +
                     "#     The maximum number of contracts available at a time.\n" +
                     "#\n" +
                     "# Save Interval:\n" +
                     "#   This determines how often (in seconds) the plugin will save all records.\n" +
                     "# Report:\n" +
                     "#   If this is true, the plugin will report kills and earnings to players as they're made\n" +
                     "#\n" +
                     "#########");
             saveConfig();
         }
         if (getConfig().get("economy.animals.ocelot") == null) {
             List<Double> list = new ArrayList<Double>();
             list.add(3.0);
             list.add(5.0);
             getConfig().set("economy.animals.ocelot", list);
             getConfig().set("economy.monsters.irongolem", list);
             saveConfig();
         }
         if (getConfig().get("plugin.leaders") == null) {
             getConfig().set("plugin.leaders", true);
             getConfig().set("plugin.profiler", false);
         }
 
         leaderFile = new File(getDataFolder(), "leaders.yml");
         if (!leaderFile.exists()) {
             getLeaders().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/leaders.yml")));
             getLeaders().options().copyDefaults(true);
             saveLeaders();
         }
         if (getLeaders().get("ocelot") == null) {
             List<String> list = new ArrayList<String>();
             getLeaders().set("ocelot", list);
             getLeaders().set("irongolem", list);
             saveLeaders();
         }
 
         debug = getConfig().getBoolean("plugin.debug", false);
         economy = getConfig().getBoolean("plugin.economy", false);
         contract = getConfig().getBoolean("plugin.contracts", false);
         report = getConfig().getBoolean("plugin.report", true);
         worlds = getConfig().getStringList("plugin.exclude_worlds");
         profile = getConfig().getBoolean("plugin.profiler", false);
     }
 
     private void registerCommands() {
         PermissionsHandler permissionsHandler = new PermissionsHandler();
         commandHandler = new CommandHandler(this, permissionsHandler);
         commandHandler.registerCommand(new DebugCommand(this));
         commandHandler.registerCommand(new LeaderCommand(this));
         commandHandler.registerCommand(new ReloadCommand(this));
         commandHandler.registerCommand(new StatsCommand(this));
         if (contract) {
             //commandHandler.registerCommand(new ContractCommand(this));
         }
     }
     
     public void log(String message) {
         getLogger().info(message);
     }
     
     public void debug(boolean val) {
         getConfig().set("plugin.debug", val);
         debug = val;
         saveConfig();
     }
     
     public void debug(String message) {
         if (debug) {
             String msg = "[Debug] " + message;
             getLogger().info(msg);
         }
     }
 
     public void report(final Player p, final Monster m, final double amt, final boolean pet) {
         final KillSuite plugin = this;
         getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 if (report) {
                     String message = ChatColor.YELLOW + "[Kill] " + ChatColor.WHITE + (!pet ? "You" : "Your pet") + " killed a " + m.getFancyName();
                     if (getBank() != null && amt > 0) {
                         try {
                             message = message + " worth " + getBank().getFormattedAmount(p, amt, -1);
                         } catch (Exception e) {
                             debug("An error occurred while attempting to fetch the currency format string: " + e.getLocalizedMessage());
                         }
                     }
                     message += ".";
                     if (p.getListeningPluginChannels().contains("SimpleNotice")) {
                         p.sendPluginMessage(plugin, "SimpleNotice", message.getBytes(java.nio.charset.Charset.forName("UTF-8")));
                     } else {
                         p.sendMessage(message);
                     }
                 }
             }
         }, 1L);
     }
     
     public void displayLeaderboards(CommandSender s, Monster m, int page) {
         long n = System.nanoTime();
         addProfileMessage("leaders.parse.start", System.nanoTime() - n);
         if (m == null) { return; }
         s.sendMessage("=== Leaderboards: " + m.getFancyName() + " ===");
         try {
             LinkedHashMap<String, Integer> leaders = manager.fetchAll(m.getName());
             int search = page * 5 + 5;
             if (search > leaders.size()) {
                 search = leaders.size();
             }
             if (leaders.size() / 5 < page) {
                 page = 0;
             }
             List<Leader> list = new ArrayList<Leader>();
             String[] keys = leaders.keySet().toArray(new String[leaders.size()]);
             for (int i = page * 5; i < search; i++) {
                 list.add(new Leader(keys[i], leaders.get(keys[i]), i));
             }
             for (Leader l : list) {
                 s.sendMessage(String.format(ChatColor.GREEN + "%1$-4d " +
                         ChatColor.GOLD + "|" + ChatColor.AQUA + " %2$s " +
                         ChatColor.GOLD + "-" + ChatColor.BLUE + " %3$d",
                         l.getRank()+1, l.getName(), l.getKills()));
             }
         } catch (NullPointerException e) {
             debug("Something went wrong. The fetched leader list was null!");
         } catch (IndexOutOfBoundsException e) {
             debug("Some calculation was off; the index wasn't found!");
             e.printStackTrace();
         }
         addProfileMessage("leaders.parse.end", System.nanoTime() - n);
     }
 
     public boolean validateAllPay() {
         if (bank != null) { return true; }
         double allpayVersion = 3.1;
         AllPay allpay = new AllPay(this, "[" + this + "] ");
         if (allpay.getVersion() >= allpayVersion) {
             bank = allpay.loadEconPlugin();
             bank.toggleReceipts(false);
             debug("Hooked economy with AllPay v" + allpay.getVersion() + "...");
             return true;
         }
         return false;
     }
     
     public void validateAllPay(boolean val) {
         if (val) {
             economy = false;
             bank = null;
             debug("Economy plugin unhooked.");
             return;
         }
         validateAllPay();
     }
     
     public GenericBank getBank() {
         if (!economy) { return null; }
         if (bank == null) {
             validateAllPay();
         }
         return bank;
     }
 
     private void generateRewards() {
         debug("Populating random reward generator...");
         ConfigurationSection econ = getConfig().getConfigurationSection("economy");
         for (Monster monster : Monster.values()) {
             List<Double> vals;
             try {
                 if (monster.getName().equals("player")) {
                     vals = econ.getDoubleList("players.reward");
                 } else {
                     vals = econ.getDoubleList(monster.getCategory() + "." + monster.getName());
                 }
                 RewardGenerator reward = new RewardGenerator(vals.get(0), vals.get(1));
                 manager.addReward(monster.getName(), reward);
             } catch (IndexOutOfBoundsException e) {
                 getLogger().warning("An invalid/incomplete economy list was encountered! Probable culprit: " + monster.getCategory() + "/" + monster.getName());
             }
         }
     }
     
     public Manager getManager() {
         return manager;
     }
 
     public FileConfiguration getConfig() {
         if (configuration == null) {
             configuration = YamlConfiguration.loadConfiguration(configFile);
         }
         return configuration;
     }
 
     FileConfiguration getLeaders() {
         if (leaderboards == null) {
             leaderboards = YamlConfiguration.loadConfiguration(leaderFile);
         }
         return leaderboards;
     }
     
     void saveLeaders() {
         try {
             leaderboards.save(new File(getDataFolder(), "leaders.yml"));
         } catch (IOException e) {
             debug("Error saving file 'leaders.yml'");
         }
     }
     
     public boolean validWorld(String world) {
         return !worlds.contains(world);
     }
 
     public double diminishReturn(Player killer, double amount) {
         int ret = getConfig().getInt("economy.diminish.return");
         int depth = getConfig().getInt("economy.diminish.depth");
         int player = (int) Math.floor(killer.getLocation().getY());
         double diminish = ((depth - player) * ret);
         diminish = ((diminish > 0 ? diminish : 0) / 100);
         amount = (amount - (amount * (diminish)));
         return (amount > 0 ? amount : 0);
     }
 
     /*
      * Profiling Data
      */
 
     private final LinkedList<String> profileList = new LinkedList<String>();
 
     void profile(LinkedList<String> list, boolean profile) {
         if (profile) {
             for (String line : list) {
                 getLogger().info(line);
             }
         }
     }
 
     public static void addProfileMessage(String section, long time) {
         if (plugin.profile) {
             plugin.profileList.add(String.format("%1$s took %2$dns (%3$dms)", section, time, time / 1000000));
         }
     }
 
     /*
      * Economy Transactions
      */
 
     private final LinkedList<Transaction> transactions = new LinkedList<Transaction>();
 
     void transact(LinkedList<Transaction> transactions) {
         for (Transaction t : transactions) {
            getBank().give(getServer().getPlayer(t.getName()), t.getAmount(), t.getType());
         }
     }
 
     public static void addBankTransaction(String killer, double amt, int type) {
         plugin.transactions.add(new Transaction(killer, amt, type));
     }
 
 }
