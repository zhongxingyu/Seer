 package me.tehbeard.BeardAch;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Scanner;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import me.tehbeard.BeardAch.Metrics.Graph;
 import me.tehbeard.BeardAch.Metrics.Plotter;
 import me.tehbeard.BeardAch.achievement.*;
 import me.tehbeard.BeardAch.achievement.rewards.IReward;
 import me.tehbeard.BeardAch.achievement.triggers.*;
 import me.tehbeard.BeardAch.achievement.rewards.*;
 import me.tehbeard.BeardAch.commands.*;
 import me.tehbeard.BeardAch.dataSource.*;
 import me.tehbeard.BeardAch.dataSource.configurable.Configurable;
 import me.tehbeard.BeardAch.dataSource.configurable.IConfigurable;
 import me.tehbeard.BeardAch.dataSource.json.editor.EditorJSON;
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 import me.tehbeard.utils.addons.AddonLoader;
 import me.tehbeard.utils.factory.ConfigurableFactory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.*;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 import de.hydrox.bukkit.DroxPerms.DroxPerms;
 import de.hydrox.bukkit.DroxPerms.DroxPermsAPI;
 
 public class BeardAch extends JavaPlugin {
 
     public static BeardAch self;
     private PlayerStatManager stats = null;
     private AddonLoader<IConfigurable> addonLoader;
     private Metrics metrics;
 
     public static int triggersMetric = 0;
     public static int rewardsMetric = 0;
 
     public static DroxPermsAPI droxAPI = null;
     private WorldGuardPlugin worldGuard;
     private AchievementManager achievementManager;
 
     
     private EditorJSON json = new EditorJSON();
     /**
      * Load BeardAch
      */
     @SuppressWarnings("unchecked")
     public void onEnable() {
         self = this;
         achievementManager = new AchievementManager();
         //Load config
         printCon("Starting BeardAch");
         /*if(!getConfig().getKeys(false).contains("achievements")){
             
         }*/
         getConfig().options().copyDefaults(true);
         saveConfig();
         reloadConfig();
 
         EnableBeardStat();
 
 
 
 
 
         //check DroxPerms
         DroxPerms droxPerms = ((DroxPerms) this.getServer().getPluginManager().getPlugin("DroxPerms"));
         if (droxPerms != null) {
             droxAPI = droxPerms.getAPI();
         }
 
         //check WorldGuard
         worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
 
 
 
 
 
         printCon("Loading Data Adapters");
         ConfigurableFactory<IDataSource,DataSourceDescriptor> dataSourceFactory = new ConfigurableFactory<IDataSource, DataSourceDescriptor>(DataSourceDescriptor.class) {
 
             @Override
             public String getTag(DataSourceDescriptor annotation) {
                 return annotation.tag();
             }
         };
         dataSourceFactory.addProduct(YamlDataSource.class);
         dataSourceFactory.addProduct(SqlDataSource.class);
         dataSourceFactory.addProduct(NullDataSource.class);
 
         achievementManager.database = dataSourceFactory.getProduct(getConfig().getString("ach.database.type",""));
 
         if(achievementManager.database == null){
             printError("NO SUITABLE DATABASE SELECTED!!");
             printError("DISABLING PLUGIN!!");
 
             //onDisable();
             setEnabled(false);
             return;
         }
         printCon("Installing default triggers");
         //Load installed triggers
         addTrigger(AchCheckTrigger.class);
         addTrigger(AchCountTrigger.class);
         addTrigger(CuboidCheckTrigger.class);
         addTrigger(LockedTrigger.class);
         addTrigger(NoAchCheckTrigger.class);
         addTrigger(PermCheckTrigger.class);
         addTrigger(StatCheckTrigger.class);
         addTrigger(StatWithinTrigger.class);
         addTrigger(EconomyTrigger.class);
         addTrigger(SpeedRunTrigger.class);
         addTrigger(CuboidKingOfTheHillTrigger.class);
         addTrigger(WorldGuardRegionTrigger.class);
         addTrigger(InteractTrigger.class);
 
 
         printCon("Installing default rewards");
         //load installed rewards
         addReward(CommandReward.class);
         addReward(CounterReward.class);
         addReward(DroxSubGroupReward.class);
         addReward(DroxTrackReward.class);
         addReward(EconomyReward.class);
         addReward(SetGroupReward.class);
         addReward(TeleportReward.class);
         addReward(PotionReward.class);
         addReward(PlayerCommandReward.class);
 
         
         
 
 
         //Load built in extras
         InputStream bundle = getResource("bundle.properties");
 
         if(bundle!=null){
             try{
                 printCon("Loading bundled addons");
 
                 Scanner scanner;
 
                 scanner = new Scanner(bundle);
                 while(scanner.hasNext()){
                     String ln = scanner.nextLine();
                     String[] l = ln.split("=");
                     if(l[0].equalsIgnoreCase("name")){
                         BeardAch.printCon("Loading bundled addon " + l[1]);
                     }else if(l[0].equalsIgnoreCase("class")){
                         Class<?> c = getClassLoader().loadClass(l[1]);
                         if(c!=null){
                             if(ITrigger.class.isAssignableFrom(c)){
                                 triggersMetric ++;
                                 addTrigger((Class<? extends ITrigger>) c);
                             }else if(IReward.class.isAssignableFrom(c)){
                                 rewardsMetric ++;
                                 addReward((Class<? extends IReward>) c);
                             }
                         }
                     }
 
 
                 }
                 scanner.close();
             } catch (ClassNotFoundException e) {
                 printCon("[PANIC] Could not load a class listed in the bundle file");
             }
         }        
 
 
         printCon("Preparing to load addons");
         //Create addon dir if it doesn't exist
         File addonDir = (new File(getDataFolder(),"addons"));
         if(!addonDir.exists()){
             addonDir.mkdir();
         }
 
         //create the addon loader
         addonLoader = new BeardAchAddonLoader(addonDir);
 
         printCon("Loading addons");
         addonLoader.loadAddons();
 
         printCon("Writing editor settings");
         new File(getDataFolder(),"editor").mkdirs();
         try {
 			json.write(new File(getDataFolder(),"editor/settings.js"));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
         exportEditor();
 
         printCon("Loading Achievements");
 
         achievementManager.loadAchievements();
 
 
         //metrics code
         if(!getConfig().getBoolean("general.plugin-stats-opt-out",false)){
             try {
                 metrics = new Metrics(this);
 
 
                 //set up custom plotters for custom triggers and rewards
                 SimplePlotter ct = new SimplePlotter("Custom Triggers");
                 SimplePlotter cr = new SimplePlotter("Custom Rewards");
                 ct.set(triggersMetric);
                 cr.set(rewardsMetric);
                 if(getStats()!=null){
                     metrics.addCustomData(new Plotter("BeardStat installed") {
 
                         @Override
                         public int getValue() {
                             return 1;
                         }
                     });
                 }
                 metrics.addCustomData(ct);
                 metrics.addCustomData(cr);
 
                 //total achievements on server
                 SimplePlotter totalAchievments = new SimplePlotter("Total Achievements");
                 totalAchievments.set(achievementManager.getAchievementsList().size());
 
                 //Triggers per achievement
                 Graph triggersGraph = metrics.createGraph("triggers");
                 for(final String trig : AchievementLoader.triggerFactory.getTags()){
                     SimplePlotter p = new SimplePlotter(trig + " Trigger");
 
                     for(Achievement a : achievementManager.getAchievementsList()){
                         for(ITrigger t : a.getTrigs()){
                             Configurable c = t.getClass().getAnnotation(Configurable.class);
                             if(c!=null){
                                 if(trig.equals(c.tag())){
                                     p.increment();
                                 }
                             }
                         }
                     }
 
                     triggersGraph.addPlotter(p);
 
                 }
 
 
                 //Rewards per achievement
                 Graph rewardsGraph = metrics.createGraph("rewards");
                 for(final String reward : AchievementLoader.rewardFactory.getTags()){
                     SimplePlotter p = new SimplePlotter(reward + " Reward");
 
                     for(Achievement a : achievementManager.getAchievementsList()){
                         for(IReward r : a.getRewards()){
                             Configurable c = r.getClass().getAnnotation(Configurable.class);
                             if(c!=null){
                                 if(reward.equals(c.tag())){
                                     p.increment();
                                 }
                             }
                         }
                     }
 
                     rewardsGraph.addPlotter(p);
                 }
 
                 DataSourceDescriptor c = achievementManager.database.getClass().getAnnotation(DataSourceDescriptor.class);
                 Graph g = metrics.createGraph("storage system");
                 g.addPlotter(new Plotter(c.tag() + " storage"){
 
                     @Override
                     public int getValue() {
                         return 1;
                     }}
                         );
 
                 metrics.start();
 
             } catch (Exception e) {
                 printCon("Could not load metrics :(");
                 printCon("Please send the following stack trace to Tehbeard");
                 printCon("=======================");
                 e.printStackTrace();
                 printCon("=======================");
             }
 
         }
 
         printCon("Starting achievement checker");
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
 
             public void run() {
                 achievementManager.checkPlayers();
             }
 
         }, 200L,200L);
 
         //setup events
         getServer().getPluginManager().registerEvents(achievementManager,this);
 
         printCon("Loading commands");
         //commands
 
         getCommand("ach-reload").setExecutor(new AchReloadCommand());
         getCommand("ach").setExecutor(new AchCommand());
         getCommand("ach-fancy").setExecutor(new AchFancyCommand());
         printCon("Loaded Version:" + getDescription().getVersion());
 
 
     }
 
     /**
      * Unload BeardAch
      */
     public void onDisable() {
 
         achievementManager.database.flush();
 
     }
 
     /**
      * Handle unfinished commands
      */
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
 
         sender.sendMessage("COMMAND NOT IMPLEMENTED");
         return true;
     }
 
     
 
     /**
      * Add a trigger
      * @param trigger
      */
     public void addTrigger(Class<? extends ITrigger > trigger){
       
         AchievementLoader.triggerFactory.addProduct(trigger);
         json.addTrigger(trigger);
     }
     /**
      * Add a reward
      * @param reward
      */
     public void addReward(Class<? extends IReward >  reward){
         AchievementLoader.rewardFactory.addProduct(reward);
         json.addReward(reward);
     }
 
     /**
      * Colorises strings containing &0-f
      * @param msg
      * @return
      */
     public static String colorise(String msg){
 
         for(int i = 0;i<=9;i++){
             msg = msg.replaceAll("&" + i, ChatColor.getByChar(""+i).toString());
         }
         for(char i = 'a';i<='f';i++){
             msg = msg.replaceAll("&" + i, ChatColor.getByChar(i).toString());
         }
         return msg;
     }
 
     /**
      * Print error message
      * @param errMsg
      */
     public static void printError(String errMsg){
         self.getLogger().severe("[ERROR] " + errMsg);
     }
     /**
      * Print error message with an exception
      * @param errMsg
      * @param e
      */
     public static void printError(String errMsg,Exception e){
         self.getLogger().severe("[ERROR] " + errMsg);
         self.getLogger().severe("[ERROR] ==Stack trace dump==");
         e.printStackTrace();
         self.getLogger().severe("[ERROR] ==Stack trace dump==");
     }
 
 
     /**
      * return the achievement manager
      * @return
      */
     public AchievementManager getAchievementManager(){
         return achievementManager;
     }
 
 
     /**
      * Try to load BeardStat
      */
     private void EnableBeardStat(){
         BeardStat bs = (BeardStat) Bukkit.getServer().getPluginManager().getPlugin("BeardStat");
         if(bs!=null && bs.isEnabled()){
             stats = bs.getStatManager();
         }
         else
         {
             printError("BeardStat not installed! stat and statwithin triggers will not function!");
         }
     }
 
     /**
      * Return WorldGuard instance
      * @return
      */
     public WorldGuardPlugin getWorldGuard() {
         return worldGuard;
     }
 
     /**
      * Returns BeardStat instance
      * @return
      */
     public PlayerStatManager getStats(){
         return stats;
     }
 
     /**
      * Print console message
      * @param line
      */
     public static void printCon(String line){
         self.getLogger().info(line);
     }
 
     /**
      * Print message for debug
      * @param line
      */
     public static void printDebugCon(String line){
         if(self.getConfig().getBoolean("general.debug")){
             printCon("[DEBUG] " + line);
         }
     }
 
 
     public static final int BUFFER_SIZE = 8192;
     
     private void exportEditor(){
     	try {
     	ZipInputStream zis = new ZipInputStream(getResource("editor.zip"));
     	ZipEntry entry = null;
 
     	File outputDir = new File(getDataFolder(),"editor");
         
 			while((entry = zis.getNextEntry()) != null)
 			{
 				File outputFile = new File(outputDir,entry.getName());
				outputFile.mkdirs();
 				
 				int count;
 
                 byte data[] = new byte[BUFFER_SIZE];
 
                 //write the file to the disk
                 FileOutputStream fos = new FileOutputStream(outputFile);
                 BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);
 
                 while((count = zis.read(data, 0, BUFFER_SIZE)) != -1)
                 {
                     dest.write(data, 0, count);
                 }
 
                 //close the output streams
                 dest.flush();
                 dest.close();
 			}
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}
     }
 
 }
