 package de.indiplex.multiworlds;
 
 import de.indiplex.manager.IPMPlugin;
 import de.indiplex.multiworlds.generators.MWGenerator;
 import de.indiplex.multiworlds.generators.MidiGenerator;
 import de.indiplex.multiworlds.generators.ObjGenerator;
 import de.indiplex.multiworlds.generators.PicGenerator;
 import de.indiplex.multiworlds.generators.WaterGenerator;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginManager;
 
 /**
  * World Manager for Bukkit
  *
  * @author IndiPlex
  */
 public class MultiWorlds extends IPMPlugin {
 
     public static final String pre = "[MW] ";
     public YamlConfiguration config;
     public boolean mustCreateConfig = false;
     public List<MWWorld> worlds = new ArrayList<MWWorld>();
     public HashMap<String, Class<? extends MWGenerator>> generators = new HashMap<String, Class<? extends MWGenerator>>();
     public HashMap<String, MWGenerator> genHashMap = new HashMap<String, MWGenerator>();
     private MultiWorldsAPI mwAPI;
 
     @Override
     public void onDisable() {
         for (MWWorld w : worlds) {
             getServer().unloadWorld(w.getWorld(), true);
         }
         printDisabled(pre);
     }
 
     public static void iLog(String msg) {
         log.info(pre + msg);
     }
 
     public static void wLog(String msg) {
         log.warning(pre + msg);
     }
 
     @Override
     public void onLoad() {
         MultiWorldsAPI MWAPI = new MultiWorldsAPI(this);
         getAPI().registerAPI(MWAPI);
         mwAPI = MWAPI;
         config = getAPI().getConfig();
         registerGenerators(MWAPI);
     }
 
     private void registerGenerators(MultiWorldsAPI API) {
         API.setGenerator("midi", MidiGenerator.class);
         API.setGenerator("pic", PicGenerator.class);
         API.setGenerator("water", WaterGenerator.class);
         API.setGenerator("obj", ObjGenerator.class);
     }
 
     @Override
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         MWWorldListener wListener = new MWWorldListener(this);
         pm.registerEvent(Type.PORTAL_CREATE, wListener, Priority.Normal, this);
         loadConfig();
         loadWorlds(false);
         printEnabled(pre);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
             return true;
         }
         Player player = (Player) sender;
 
         if (args.length < 1 || args.length > 2) {
             return false;
         }
         if (args[0].equalsIgnoreCase("tp")) {
             if (args.length != 2) {
                 return false;
             }
             if (getServer().getWorld(args[1]) == null) {
                 player.sendMessage(args[1] + " is not a world!");
                 return true;
             }
             player.teleport(getServer().getWorld(args[1]).getSpawnLocation());
             return true;
         } else if (args[0].equalsIgnoreCase("reload")) {
             if (args.length == 2) {
                 if (args[1].equalsIgnoreCase("config")) {
                     loadConfig();
                     player.sendMessage("Config reloaded!");
                     return true;
                 } else if (args[1].equalsIgnoreCase("worlds")) {
                     loadWorlds(true);
                     player.sendMessage("Worlds reloaded!");
                     return true;
                 }
             }
             loadConfig();
             loadWorlds(true);
             player.sendMessage("Config and worlds reloaded!");
             return true;
         } else if (args[0].equalsIgnoreCase("reset")) {
             World w = null;
             int rX = 0;
             int rY = 0;
             switch (args.length) {
                 case 1:
                     w = player.getWorld();
                     rX = 20;
                     rY = 20;
                     break;
                 case 2:
                     w = getServer().getWorld(args[1]);
                     rX = 20;
                     rY = 20;
                     break;
                 case 3:
                     w = player.getWorld();
                     try {
                         rX = Integer.parseInt(args[1]);
                         rY = Integer.parseInt(args[2]);
                     } catch (NumberFormatException e) {
                         w = null;
                     }
                     break;
                 case 4:
                     w = getServer().getWorld(args[1]);
                     try {
                         rX = Integer.parseInt(args[2]);
                         rY = Integer.parseInt(args[3]);
                     } catch (NumberFormatException e) {
                         w = null;
                     }
                     break;
             }
             if (w == null) {
                 player.sendMessage("World doesn't exist or number can't be parsed");
                 return true;
             }
             for (int x = 0 - rX; x < rX; x++) {
                 for (int y = 0 - rY; y < rY; y++) {
                     if (w.isChunkLoaded(x, y)) {
                         w.regenerateChunk(x, y);
                     }
                 }
             }
             return true;
         } else if (args[0].equalsIgnoreCase("list")) {
             int i = 0;
             if (args.length == 2) {
                 if (args[1].equalsIgnoreCase("mwws")) {
                     for (MWWorld w : worlds) {
                         i++;
                         player.sendMessage(String.valueOf(i) + ": Name=" + w.getName() + " Generator=" + w.getGenerator().getClass().getSimpleName() + " Environment= " + w.getEnv().name());
                     }
                     return true;
                 }
             }
             List<World> worldList = getServer().getWorlds();
             for (World w : worldList) {
                 i++;
                 player.sendMessage(w.getName());
             }
             return true;
         }
         return false;
     }
 
     private void createConfig() {
         ConfigurationSection cs = config.getConfigurationSection("worlds");
         ArrayList<String> wos = new ArrayList<String>();
         if (cs != null) {
             wos = new ArrayList<String>(cs.getKeys(false));
         }
         if (mustCreateConfig || cs == null) {
             log.info(pre + "No config found. Creating one...");
 
             List<String> ws = new ArrayList<String>();
             ArrayList<String> Envs = new ArrayList<String>();
             for (World w : getServer().getWorlds()) {
                 String world = w.getName();
                 if (config.getString(w.getName()) != null) {
                     continue;
                 }
                 ws.add(world);
                 String env = w.getEnvironment().toString().toLowerCase();
                 Envs.add(env);
                 log.info(pre + "Added world \"" + world + "\" with environment \"" + env + "\"");
             }
             wos.addAll(ws);
 
             for (int i = 0; i < Envs.size(); i++) {
                 config.set("worlds." + ws.get(i) + ".environment", Envs.get(i));
             }
             mustCreateConfig = false;
         }
     }
 
     public void loadConfig() {
         log.info(pre + "Loading config...");
         createConfig();
         ConfigurationSection configurationSection = config.getConfigurationSection("worlds");
         Set<String> t = configurationSection.getKeys(false);
         List<String> ws = new ArrayList<String>(t);
 
         worlds.clear();
         int l = 0;
         for (int i = 0; i < ws.size(); i++) {
             String world = ws.get(i);
             MWGenerator cg = null;
             try {
                 Class<? extends MWGenerator> get = generators.get(config.getString("worlds." + world + ".generator"));
                 if (get != null) {
                     cg = get.newInstance();
                     cg.initAPI(mwAPI);
                     genHashMap.put(world, cg);
                 }
             } catch (InstantiationException ex) {
                 cg = null;
             } catch (IllegalAccessException ex) {
                 cg = null;
             }
 
             if (cg == null && config.getBoolean("worlds." + world + ".extern", false)) {
                 continue;
             }
             World.Environment env = null;
             try {
                 env = World.Environment.valueOf(config.getString("worlds." + world + ".environment").toUpperCase());
             } catch (Exception e) {
                 log.warning(pre + "There is no environment for world " + world + " set.");
             }
             MWWorld mww = new MWWorld(world, cg, env);
 
             worlds.add(mww);
             l++;
         }
         log.info(pre + "Config loaded (" + l + " worlds found)...");
        getAPI().saveConfig(config);
     }
 
     public void loadWorlds(boolean reload) {
         log.info(pre + "Loading worlds...");
         int i = 0;
         for (MWWorld w : worlds) {
             if (!w.createWorld()) {
                 log.warning(pre + "Can't load world " + w.getName());
             } else {
                 if (!reload) {
                     log.info(pre + "Loaded world " + w.getName());
                 }
                 i++;
             }
         }
         log.info(pre + i + " worlds loaded...");
     }
 
     boolean save() {
        return getAPI().saveConfig(config);
     }
 }
