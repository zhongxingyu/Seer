 package me.nexttonothing.next.mcx;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.entity.Player;
 
 import me.nexttonothing.next.software.ForumSoftware;
 import me.nexttonothing.next.software.Software;
 import me.nexttonothing.next.util.LocalisationUtility;
 import me.nexttonothing.next.util.Version;
 import me.nexttonothing.next.configs.LocaleConfig;
 import me.nexttonothing.next.configs.MainConfig;
 import me.nexttonothing.next.mcx.EconomyUpdater;
 
 public class McX extends JavaPlugin {
     final Version Version = new Version("2.3.0.2");
     public State ac;
     public ArrayList<Player> grey = new ArrayList<Player>();
     public static LocalisationUtility lang;
     private final GreyPlayerListener gPlayerListener = new GreyPlayerListener(this);
     private final WhitePlayerListener wPlayerListener = new WhitePlayerListener(this);
     private final RegisteredPlayerListener rPlayerListener = new RegisteredPlayerListener(this);
     public EconomyUpdater EconomyUpdater;
     private McXCommands McXExc;
     public static Economy economy = null;
     public static Permission permission = null;
     public static State debug;
     public static String ForumType;
     public File configF;
     public File localeF;
     public YamlConfiguration config;
     public YamlConfiguration locale;
     
     @Override
     public void onEnable() {
         debug = State.Off;
         initializeConfigs();
         lang = new LocalisationUtility(this);
         System.out.println(lang.get("locale.misc.enabled") + Version);
         if(getServer().getPluginManager().getPlugin("Vault") != null){
             if(getMainConfig().getOption("economy.enabled").equalsIgnoreCase("true"))
                 if(setupEconomy())
                     System.out.println(lang.get("locale.plugins.economy"));
             if(setupPermissions()){
                 System.out.println(lang.get("locale.plugins.perms"));
             }
         } else {
             if(getMainConfig().getOption("economy.enabled").equalsIgnoreCase("true"))
                 System.out.println(lang.get("locale.plugins.economyMissing"));
         }
         setupForum();
         try {
             if(testMySql()) {
                 System.out.println(lang.get("locale.validate.mysql.testSuccess"));
             } else {
                 System.out.println(lang.get("locale.validate.mysql.testFail"));
                 this.getPluginLoader().disablePlugin(this);
                 return;
             }
         } catch (Exception e) {
             System.out.println(lang.get("locale.validate.mysql.testFail"));
             this.getPluginLoader().disablePlugin(this);
         }
         if(getMainConfig().getOption("general.type").equalsIgnoreCase("greylist")){
             addGreyListeners();
             System.out.println(lang.get("locale.player.notification.greyActivated"));
         } else if(getMainConfig().getOption("general.type").equalsIgnoreCase("whitelist")){
             addWhiteListeners();
             System.out.println(lang.get("locale.player.notification.whiteActivated"));
         } else {
             System.out.println(lang.get("locale.validate.unknownType"));
             this.getPluginLoader().disablePlugin(this);
             return;
         }
         addRegisteredListeners();
         registerCommands();
         Updater.newUpdate(this, true);
         if(getServer().getPluginManager().getPlugin("Vault") != null){
             if(getMainConfig().getOption("economy.enabled").equalsIgnoreCase("true"))
                 if(setupEconomy())
                     EconomyUpdater = new EconomyUpdater(this.getMainConfig());
         }
     }
 
     private void setupForum() {
         ForumType = this.getMainConfig().getString("mysql.forumtype");
         System.out.println("[McX] ForumType: " + ForumType);
     }
     
     private Boolean setupPermissions() {
            RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
            if (permissionProvider != null) {
                permission = permissionProvider.getProvider();
            }
            return (permission != null);
     }
     
     private Boolean setupEconomy() {
         RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
         return (economy != null);
     }
      
     @Override
     public void onDisable() {
         for (Player p: grey) {
                p.kickPlayer(lang.get("locale.player.notification.kick"));
             }
        EconomyUpdater.stop();
         System.out.println(lang.get("locale.misc.disabled")  + Version);
     }
     
     private void addGreyListeners(){
         Bukkit.getServer().getPluginManager().registerEvents(gPlayerListener, this);
     }
     
     private void addWhiteListeners(){
         Bukkit.getServer().getPluginManager().registerEvents(wPlayerListener, this);
     }
     
     private void addRegisteredListeners(){
         Bukkit.getServer().getPluginManager().registerEvents(rPlayerListener, this);
     }
     
     void initializeConfigs() {
         localeF = new File(getDataFolder(), "locale.yml");
         configF = new File(getDataFolder(), "config.yml");
 
         System.out.println("[McX] Loading configuration file locale.yml");
         locale = YamlConfiguration.loadConfiguration(localeF);
         getLocale().load();
         System.out.println(getLocale().getOption("locale.conf.loadSuccess") + "locale.yml");
         System.out.println(getLocale().getOption("locale.conf.loading") + "config.yml");
         config = YamlConfiguration.loadConfiguration(configF);
         getMainConfig().load();
         System.out.println(getLocale().getOption("locale.conf.loadSuccess") + "config.yml");
 
         locale.options().indent(4);
         config.options().indent(4);
     }
 
     public void reloadConfigs() {
         getMainConfig().reload();
         getLocale().reload();
     }
     
     // Main Config (config.yml)
     public MainConfig getMainConfig() {
         return new MainConfig(this);
     }
 
     // Locale Config (locale.yml)
     public LocaleConfig getLocale() {
         return new LocaleConfig(this);
     }
     
     private void registerCommands(){
         McXExc = new McXCommands(this);
         getCommand("mcx").setExecutor(McXExc);
         ac = State.On;
     }
     
     private boolean testMySql() {
         try {
             Software anonymous;
             anonymous = ForumSoftware.getSoftwareObject(ForumType, this.getMainConfig().getString("mysql.verifyuser"), this.getMainConfig());
 
             return anonymous.testMysql();
         } catch(Exception e) {
             System.out.println(lang.get("locale.validate.mysql.testFail"));
         }
         return false;
     }
 
     public boolean status(CommandSender sender) {
         if(ac == State.On)
             sender.sendMessage(lang.get("locale.player.notification.on"));
         if(ac == State.Off)
             sender.sendMessage(lang.get("locale.player.notification.off"));
         return true;
     }
     
     public boolean setOn() {
         ac = State.On;
         System.out.println(lang.get("locale.player.action.seton"));
         return true;
     
     }
 
     public boolean setOff() {
         ac = State.Off;
         System.out.println(lang.get("locale.player.action.setoff"));
         return true;
     }
 }
