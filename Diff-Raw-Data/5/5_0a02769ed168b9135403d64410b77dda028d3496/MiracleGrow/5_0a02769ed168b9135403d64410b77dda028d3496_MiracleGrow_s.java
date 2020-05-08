 package com.minecarts.miraclegrow;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.text.MessageFormat;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.ConfigurationSection;
 
 import com.minecarts.dbquery.DBQuery;
 import com.minecarts.dbconnector.providers.Provider;
 
 import com.minecarts.miraclegrow.BlockStateRestore.Cause;
 import com.minecarts.miraclegrow.listener.*;
 import org.bukkit.event.Listener;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.World;
 
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.Event.Priority;
 import static org.bukkit.event.Event.Type.*;
 
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 
 public class MiracleGrow extends org.bukkit.plugin.java.JavaPlugin {
     private static final Logger logger = Logger.getLogger("com.minecarts.miraclegrow"); 
     
     protected boolean debug;
     protected FileConfiguration config;
     protected ConfigurationSection worlds;
     
     protected DBQuery dbq;
     protected Provider provider;
     
     protected HashMap<World, HashSet<BlockStateRestore>> queue = new HashMap<World, HashSet<BlockStateRestore>>();
     protected ArrayList<World> processing = new ArrayList<World>();
     
     
     public void onEnable() {
         dbq = (DBQuery) getServer().getPluginManager().getPlugin("DBQuery");
         reloadConfig();
         
         PluginManager pluginManager = getServer().getPluginManager();
         HashMap<Listener, Type[]> listeners = new HashMap<Listener, Type[]>() {{
             put(new ServerListener(MiracleGrow.this), new Type[]{ PLUGIN_DISABLE });
             put(new WorldListener(MiracleGrow.this), new Type[]{ PORTAL_CREATE });
             put(new BlockListener(MiracleGrow.this), new Type[]{ BLOCK_PLACE, BLOCK_BREAK, BLOCK_FADE, BLOCK_FORM, BLOCK_SPREAD, BLOCK_FROMTO, LEAVES_DECAY, BLOCK_IGNITE, BLOCK_BURN, BLOCK_PISTON_EXTEND, BLOCK_PISTON_RETRACT });
             put(new EntityListener(MiracleGrow.this), new Type[]{ ENTITY_EXPLODE, ENDERMAN_PICKUP, ENDERMAN_PLACE });
         }};
         
         for(Entry<Listener, Type[]> entry : listeners.entrySet()) {
             for(Type type : entry.getValue()) {
                 pluginManager.registerEvent(type, entry.getKey(), Priority.Monitor, this);
             }
         }
         
         // TODO: check and create tables from config IF NOT EXIST
         
         /*
             CREATE TABLE IF NOT EXISTS `MiracleGrow_table_name` (
               `x` smallint(6) NOT NULL DEFAULT '0',
               `y` tinyint(4) NOT NULL DEFAULT '0',
               `z` smallint(6) NOT NULL DEFAULT '0',
               `type` smallint(6) DEFAULT NULL,
               `data` tinyint(4) DEFAULT NULL,
               `when` timestamp DEFAULT NULL,
              PRIMARY KEY (`x`,`y`,`z`)
            ) ENGINE=MyISAM DEFAULT CHARSET=utf8;
          */
         
         
         int interval = Math.max(20, 20 * config.getInt("flushInterval"));
         debug("Flushing block restore queue at a {0} tick interval", interval);
         
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
             public void run() {
                 processQueue();
             }
         }, interval, interval);
         
 
         log("Version {0} enabled.", getDescription().getVersion());
     }
     
     public void onDisable() {
         processQueue(false);
     }
     
     
     @Override
     public void reloadConfig() {
         super.reloadConfig();
         
         if(config == null) config = getConfig();
         
         debug = config.getBoolean("debug");
         worlds = config.getConfigurationSection("worlds");
         provider = dbq.getProvider(config.getString("DBQuery.provider"));
     }
     
     
     public void log(String message) {
         log(Level.INFO, message);
     }
     public void log(Level level, String message) {
         logger.log(level, MessageFormat.format("{0}> {1}", getDescription().getName(), message));
     }
     public void log(String message, Object... args) {
         log(MessageFormat.format(message, args));
     }
     public void log(Level level, String message, Object... args) {
         log(level, MessageFormat.format(message, args));
     }
     
     public void debug(String message) {
         if(debug) log(message);
     }
     public void debug(String message, Object... args) {
         if(debug) log(message, args);
     }
     
     
     
     
     
     public void scheduleRestore(Block block) {
         scheduleRestore(block.getState());
     }
     public void scheduleRestore(Block block, Cause cause) {
         scheduleRestore(block.getState(), cause);
     }
     public void scheduleRestore(Block block, int seconds) {
         scheduleRestore(block.getState(), seconds);
     }
     public void scheduleRestore(BlockState state) {
         scheduleRestore(state, BlockStateRestore.getBlockRestoreTime(state));
     }
     public void scheduleRestore(BlockState state, Cause cause) {
         scheduleRestore(state, BlockStateRestore.getBlockRestoreTime(state, cause));
     }
     public void scheduleRestore(BlockState state, int seconds) {
         BlockStateRestore restore = new BlockStateRestore(state, seconds);
         
         HashSet<BlockStateRestore> set = queue.get(state.getWorld());
         if(set == null) {
             set = new HashSet<BlockStateRestore>();
             queue.put(state.getWorld(), set);
         }
         
         if(set.contains(restore)) set.remove(restore);
         set.add(restore);
     }
     
     
     private void processQueue() {
         processQueue(true);
     }
     private void processQueue(boolean async) {
         if(worlds == null) {
             debug("No table names found in worlds section of configuration file");
             return;
         }
         
         for(Entry<World, HashSet<BlockStateRestore>> entry : queue.entrySet()) {
             
             final World world = entry.getKey();
             String table = worlds.getString(world.getName(), null);
             HashSet<BlockStateRestore> set = entry.getValue();
             
             if(processing.contains(world)) {
                 debug("Waiting to process world {0}...", world.getName());
                 continue;
             }
             
             if(table == null) {
                 debug("No table name found for worlds.{0}, clearing world's block queue", world.getName());
                 set.clear();
                 continue;
             }
             
             if(set.isEmpty()) {
                 debug("No blocks in queue for world {0}, skipping", world.getName());
                 continue;
             }
             
             processing.add(world);
             
             
             StringBuilder sql = new StringBuilder("INSERT INTO `").append(table).append("` (`x`, `y`, `z`, `type`, `data`, `when`) VALUES ");
             ArrayList<Object> params = new ArrayList();
 
             for(BlockStateRestore restore : set) {
                 sql.append("(?, ?, ?, ?, ?, TIMESTAMPADD(SECOND, ?, NOW())), ");
                 params.add(restore.state.getBlock().getX());
                 params.add(restore.state.getBlock().getY());
                 params.add(restore.state.getBlock().getZ());
                 params.add(restore.state.getTypeId());
                 params.add(restore.state.getData().getData());
                 params.add(restore.seconds);
             }
             set.clear();
 
             sql.replace(sql.length() - 2, sql.length(), " ON DUPLICATE KEY UPDATE `when`=VALUES(`when`)");
 
 
             new Query(sql.toString(), async) {
                 private int tries = 0;
 
                 @Override
                 public void onAffected(Integer affected) {
                     debug("{0} rows affected", affected);
                     processing.remove(world);
                 }
 
                 @Override
                 public void onException(Exception x, FinalQuery query) {
                     try {
                         throw x;
                     }
                     catch(java.sql.SQLException e) {
                         if(++tries < 5) {
                             log("SQLException on Query, retrying...");
                             e.printStackTrace();
                             query.run();
                         }
                         else {
                             log("FAILED! SQLException on Query: {0}", query);
                             e.printStackTrace();
                             processing.remove(world);
                         }
                     }
                     catch(com.minecarts.dbquery.NoConnectionException e) {
                         if(++tries < 5) {
                             log("NoConnectionException on Query, retrying...");
                             e.printStackTrace();
                             query.run();
                         }
                         else {
                             log("FAILED! NoConnectionException on Query: {0}", query);
                             e.printStackTrace();
                             processing.remove(world);
                         }
                     }
                     catch(Exception e) {
                         log("FAILED! Exception on Query: {0}", query);
                         e.printStackTrace();
                         processing.remove(world);
                     }
                 }
             }.affected(params.toArray());
             
         }
     }
     
     
     
     class Query extends com.minecarts.dbquery.Query {
         public Query(String sql, boolean async) {
             this(sql);
             this.async = async;
         }
         public Query(String sql) {
             // TODO: configurable provider name
             super(MiracleGrow.this, MiracleGrow.this.provider, sql);
         }
         
         @Override
         public void onException(Exception x, FinalQuery query) {
             try {
                 throw x;
             }
             catch(java.sql.SQLException e) {
                 log("SQLException on Query: {0}", query);
                 e.printStackTrace();
             }
             catch(com.minecarts.dbquery.NoConnectionException e) {
                 log("NoConnectionException on Query: {0}", query);
                 e.printStackTrace();
             }
             catch(Exception e) {
                 log("Exception on Query: {0}", query);
                 e.printStackTrace();
             }
         }
     }
     
 }
