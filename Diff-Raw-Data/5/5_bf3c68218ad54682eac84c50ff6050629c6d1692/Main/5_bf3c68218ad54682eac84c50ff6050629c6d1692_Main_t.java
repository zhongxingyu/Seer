 package fr.areku.tlmd;
 
 import fr.areku.commons.UpdateChecker;
 import java.io.*;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Filter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 
     public static Main instance;
     public MasterFilter masterFilter;
     public boolean force_filters = false;
     public long force_filters_intv = 0;
     public boolean summaryOnStart = true;
     public boolean check_plugin_updates = true;
     public boolean use_color_codes = true;
     public Map<String, Integer> filterCountMap = new HashMap<String, Integer>();
 
     public static void log(Level level, String m) {
         instance.getLogger().log(level, m);
     }
 
     public static void log(String m) {
         log(Level.INFO, m);
     }
 
     public static void logException(Exception e, String m) {
         log(Level.SEVERE, "---------------------------------------");
         log(Level.SEVERE, "--- an unexpected error has occured ---");
         log(Level.SEVERE, "-- please send line below to the dev --");
         log(Level.SEVERE, "ThisLogMustDie! version " + instance.getDescription().getVersion());
         log(Level.SEVERE, "Bukkit version " + Bukkit.getServer().getVersion());
         log(Level.SEVERE, "Message: " + m);
         log(Level.SEVERE, e.toString() + " : " + e.getLocalizedMessage());
         for (StackTraceElement t : e.getStackTrace()) {
             log(Level.SEVERE, "\t" + t.toString());
         }
         log(Level.SEVERE, "---------------------------------------");
     }
 
     @Override
     public void onLoad() {
         instance = this;
         log("ThisIsAreku present "
                 + this.getDescription().getName().toUpperCase() + ", v"
                 + getDescription().getVersion());
         log("= " + this.getDescription().getWebsite() + " =");
 
         new ColorConverter(this);
 
         this.masterFilter = new MasterFilter(this);
 
         loadConfig();
         loadFilters();
         initializeMasterFilter();
 
         if (this.force_filters) {
             Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
                 @Override
                 public void run() {
                     initializeMasterFilter();
                 }
             }, 5, this.force_filters_intv * 20);
         }
     }
 
     @Override
     public void onEnable() {
         this.getCommand("tlmd").setExecutor(this);
         startMetrics();
         if (this.check_plugin_updates) {
             startUpdate();
         }
     }
     
     
 
     public void Disable() {
         this.getPluginLoader().disablePlugin(this);
         this.setEnabled(false);
     }
 
     @Override
     public boolean onCommand(CommandSender cs, Command command, String label,
             String[] args) {
 
         if (!cs.isOp()) {
             sendMessage(cs, ChatColor.RED
                     + "You must be an OP to reload filters");
             return true;
         }
         if ((args.length == 0) || (args.length == 1 && "help".equals(args[0]))) {
             sendMessage(cs, ChatColor.YELLOW + "Usage: /tlmd reload");
             return true;
         }
 
         if ("reload".equals(args[0])) {
             initializeMasterFilter();
             loadFilters();
             sendMessage(cs, ChatColor.GREEN
                     + (this.masterFilter.filterCount() + " filter(s) loaded"));
             return true;
         }
         return super.onCommand(cs, command, label, args);
     }
 
     public void sendMessage(CommandSender cs, String m) {
         cs.sendMessage("[" + this.getName() + "] " + m);
     }
 
     public void startMetrics() {
 
         try {
             log("Starting Metrics");
             Metrics metrics = new Metrics(this);
             Metrics.Graph fitersCount = metrics.createGraph("Number of filters");
             for(final String name : filterCountMap.keySet()){
             	fitersCount.addPlotter(new Metrics.Plotter(name){
 
 					@Override
 					public int getValue() {
 						//System.out.println("metrics:"+name+":"+filterCountMap.get(name));
 						return filterCountMap.get(name);
 					}
             		
             	});
             }
             metrics.start();
         } catch (IOException e) {
             log("Cannot start Metrics...");
         }
     }
 
     public void startUpdate() {
         try {
             UpdateChecker update = new UpdateChecker(this);
             update.start();
         } catch (MalformedURLException e) {
             log("Cannot start Plugin Updater...");
         }
     }
 
     public void initializeMasterFilter() {
         String pname = "";
         try {
             for (Plugin p : this.getServer().getPluginManager().getPlugins()) {
                 pname = p.toString();
                 p.getLogger().setFilter(this.masterFilter);
                 // i++;
             }
             this.getServer().getLogger().setFilter(masterFilter);
             Bukkit.getLogger().setFilter(masterFilter);
             Logger.getLogger("Minecraft").setFilter(masterFilter);
         } catch (Exception e) {
             log(Level.INFO, "Cannot load filter in '" + pname
                     + "'. Retrying later..");
         }
         this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 
             @Override
             public void run() {
                 String pname = "";
                 try {
                     for (Plugin p : getServer().getPluginManager().getPlugins()) {
                         pname = p.toString();
                         p.getLogger().setFilter(masterFilter);
                     }
                     getServer().getLogger().setFilter(masterFilter);
                     Bukkit.getLogger().setFilter(masterFilter);
                     Logger.getLogger("Minecraft").setFilter(
                             masterFilter);
                 } catch (Exception e) {
                     log(Level.WARNING,
                             "Cannot load filter in '"
                             + pname
                             + "'. The logs of this plugin will not be filtered");
                 }
 
             }
         }, 1);
     }
 
     public void loadFilters() {
         try {
             File file = new File(this.getDataFolder(), "filters.yml");
             if (!this.getDataFolder().exists()) {
                 this.getDataFolder().mkdirs();
             }
             if (!file.exists()) {
                 copy(this.getResource("filters.yml"), file);
             }
 
             this.getConfig().load(file);
 
             List<Map<?, ?>> filtersMS = this.getConfig().getMapList("filters");
 
             this.masterFilter.clearFilters();
             this.filterCountMap.clear();
             int i = 0;
             for (Map<?, ?> m : filtersMS) {
                 i++;
                 if (!(m.containsKey("type") && m.containsKey("expression"))) {
                     log("Filter no." + i + " ignored");
                     continue;
                 }
                 String type = m.get("type").toString();
                 String expression = m.get("expression").toString();
                 try {
                     TlmdFilter filter = (TlmdFilter) Class.forName(
                             "fr.areku.tlmd.filters." + type).newInstance();
                     if (filter.initialize(expression, m)) {
                         if (this.summaryOnStart) {
                             log("Filter #" + i + " (" + type + ") initialized");
                         }
                         incrementFilterCount(type);
                         this.masterFilter.addFilter((Filter) filter);
                     } else {
                         log("Configuration of filter #" + i + " is incorrect");
                     }
                 } catch (ClassNotFoundException e) {
                     log("Filter #" + i + " has incorrect type !");
                 } catch (Exception e) {
                     logException(e, "Filter type:" + type);
                 }
             }
             log(this.masterFilter.filterCount() + " filter(s) loaded");
         } catch (FileNotFoundException e) {
             log("Cannot found the filter...");
             this.Disable();
         } catch (IOException e) {
             logException(e, "Cannot create a default filters...");
             this.Disable();
         } catch (InvalidConfigurationException e) {
             logException(e, "Fill filters before !");
             this.Disable();
         }
     }
     private void incrementFilterCount(String name){
     	if(!filterCountMap.containsKey(name))
     		filterCountMap.put(name, 0);
     	filterCountMap.put(name, filterCountMap.get(name)+1);
     	//System.out.println("incrementFilterCount:"+name);
     }
 
     public void loadConfig() {
         try {
             File file = new File(this.getDataFolder(), "config.yml");
             if (!this.getDataFolder().exists()) {
                 this.getDataFolder().mkdirs();
             }
             if (!file.exists()) {
                 copy(this.getResource("config.yml"), file);
             }
 
             this.getConfig().load(file);
 
             YamlConfiguration defaults = new YamlConfiguration();
             defaults.load(this.getResource("config.yml"));
             this.getConfig().addDefaults(defaults);
             this.getConfig().options().copyDefaults(true);
 
             this.force_filters = this.getConfig().getBoolean(
                     "force-filter.enable");
             this.force_filters_intv = this.getConfig().getLong(
                     "force-filter.interval");
             this.summaryOnStart = this.getConfig().getBoolean(
                     "summary-on-start");
             this.check_plugin_updates = this.getConfig().getBoolean(
                     "check-plugin-updates");
 
             this.use_color_codes = this.getConfig().getBoolean(
                     "use-color-codes");
             
            try{
             if (!((CraftServer) getServer()).getReader().getTerminal().isAnsiSupported()) {
                 if (this.use_color_codes) {
                     log(Level.WARNING, "Color codes may not be supported by your system");
                 }
             }
            }catch(Exception e){
            	//silent fail, when launched headless
            }
 
             this.getConfig().save(file);
         } catch (FileNotFoundException e) {
             log("Cannot found the filter...");
             this.Disable();
         } catch (IOException e) {
             logException(e, "Cannot create a default filters...");
             this.Disable();
         } catch (InvalidConfigurationException e) {
             logException(e, "Config error");
             this.Disable();
         }
     }
 
     private void copy(InputStream src, File dst) throws IOException {
         OutputStream out = new FileOutputStream(dst);
 
         // Transfer bytes from in to out
         byte[] buf = new byte[1024];
         int len;
         while ((len = src.read(buf)) > 0) {
             out.write(buf, 0, len);
         }
         src.close();
         out.close();
     }
 }
