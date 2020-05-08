 package com.tehbeard.BeardStat.DataProviders;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.regex.MatchResult;
 
 import net.dragonzone.promise.Deferred;
 import net.dragonzone.promise.Promise;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import com.tehbeard.BeardStat.BeardStat;
 import com.tehbeard.BeardStat.BeardStatRuntimeException;
 import com.tehbeard.BeardStat.DataProviders.metadata.CategoryMeta;
 import com.tehbeard.BeardStat.DataProviders.metadata.DomainMeta;
 import com.tehbeard.BeardStat.NoRecordFoundException;
 import com.tehbeard.BeardStat.containers.EntityStatBlob;
 import com.tehbeard.BeardStat.containers.IStat;
 import com.tehbeard.BeardStat.DataProviders.metadata.StatisticMeta;
 import com.tehbeard.BeardStat.DataProviders.metadata.StatisticMeta.Formatting;
 import com.tehbeard.BeardStat.DataProviders.metadata.WorldMeta;
 import com.tehbeard.BeardStat.utils.HumanNameGenerator;
 import com.tehbeard.utils.misc.CallbackMatcher;
 import com.tehbeard.utils.misc.CallbackMatcher.Callback;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * base class for JDBC based data providers Allows easy development of data
  * providers that make use of JDBC
  *
  * @author James
  *
  */
 public abstract class JDBCStatDataProvider implements IStatDataProvider {
 
     /**
      * SQL SCRIPT NAME BLOCK
      */
     public static final String SQL_METADATA_CATEGORY = "sql/maintenence/metadata/category";
     public static final String SQL_METADATA_STATISTIC = "sql/maintenence/metadata/statistic";
     public static final String SQL_METADATA_STATIC_STATS = "sql/maintenence/metadata/staticstats";
     public static final String SQL_CREATE_TABLES = "sql/maintenence/create.tables";
     public static final String SQL_LOAD_ENTITY = "sql/load/getEntity";
     public static final String SQL_LOAD_ENTITY_DATA = "sql/load/getEntityData";
     public static final String SQL_LOAD_DOMAINS = "sql/load/components/getDomains";
     public static final String SQL_LOAD_WORLDS = "sql/load/components/getWorlds";
     public static final String SQL_LOAD_CATEGORIES = "sql/load/components/getCategories";
     public static final String SQL_LOAD_STATISTICS = "sql/load/components/getStatistics";
     public static final String SQL_SAVE_DOMAIN = "sql/save/components/saveDomain";
     public static final String SQL_SAVE_WORLD = "sql/save/components/saveWorld";
     public static final String SQL_SAVE_CATEGORY = "sql/save/components/saveCategory";
     public static final String SQL_SAVE_STATISTIC = "sql/save/components/saveStatistic";
     public static final String SQL_SAVE_ENTITY = "sql/save/saveEntity";
     public static final String SQL_SAVE_STAT = "sql/save/saveStat";
     public static final String SQL_KEEP_ALIVE = "sql/maintenence/keepAlive";
     public static final String SQL_LIST_ENTITIES = "sql/maintenence/listEntities";
     
     // Database connection
     protected Connection conn;
     // Load components
     protected PreparedStatement loadDomainsList;
     protected PreparedStatement loadWorldsList;
     protected PreparedStatement loadCategoriesList;
     protected PreparedStatement loadStatisticsList;
     // save components
     protected PreparedStatement saveDomain;
     protected PreparedStatement saveWorld;
     protected PreparedStatement saveCategory;
     protected PreparedStatement saveStatistic;
     // Load data from db
     protected PreparedStatement loadEntity;
     protected PreparedStatement loadEntityData;
     // save to db
     protected PreparedStatement saveEntity;
     protected PreparedStatement saveEntityData;
     // Maintenance
     protected PreparedStatement keepAlive;
     protected PreparedStatement listEntities;
     protected PreparedStatement deleteEntity;
     protected PreparedStatement createTable;
     private HashMap<String, EntityStatBlob> writeCache = new HashMap<String, EntityStatBlob>();
     // default connection related configuration
     protected String connectionUrl = "";
     protected Properties connectionProperties = new Properties();
     protected String tblPrefix = "stats";
     private String type = "sql";
     // ID Cache
     private Map<String, DomainMeta> domainMetaMap = new HashMap<String, DomainMeta>();
     private Map<String, WorldMeta> worldMetaMap = new HashMap<String, WorldMeta>();
     private Map<String, CategoryMeta> categoryMetaMap = new HashMap<String, CategoryMeta>();
     private Map<String, StatisticMeta> statisticMetaMap = new HashMap<String, StatisticMeta>();
     // Write queue
     private ExecutorService loadQueue = Executors.newSingleThreadExecutor();
     protected BeardStat plugin;
 
     public JDBCStatDataProvider(BeardStat plugin, String type, String driverClass) {
         try {
             this.type = type;
             this.plugin = plugin;
             Class.forName(driverClass);// load driver
         } catch (ClassNotFoundException ex) {
             throw new BeardStatRuntimeException("Could not locate driver library.", ex, false);
         }
     }
 
     /**
      * Boots up the data provider, this entails:
      * <ol>
      * <li>Open connection</li>
      * <li>Migration check</li>
      * <li>Create tables</li>
      * <li>Load SQL statements</li>
      * <li>Update metadata tables</li>
      * <li>Cache data as needed</li>
      * </ol>
      * @throws BeardStatRuntimeException 
      */
     protected void initialise() throws BeardStatRuntimeException {
         createConnection();
 
         checkForMigration();
 
         checkAndMakeTable();
         prepareStatements();
 
         executeScript(SQL_METADATA_CATEGORY);
         executeScript(SQL_METADATA_STATISTIC);
         executeScript(SQL_METADATA_STATIC_STATS);
 
 
         cacheComponents();
     }
 
     /**
      * checks config in data folder against default (current versions config) If
      * version conflicts it will attempt to run migration scripts sequentially
      * to upgrade
      *
      * @throws SQLException
      */
     private void checkForMigration() {
         int latestVersion = this.plugin.getConfig().getDefaults().getInt("stats.database.sql_db_version");
 
         if (!this.plugin.getConfig().isSet("stats.database.sql_db_version")) {
             this.plugin.getConfig().set("stats.database.sql_db_version", 1);
             this.plugin.saveConfig();
         }
         int installedVersion = this.plugin.getConfig().getInt("stats.database.sql_db_version", 1);
 
         if (installedVersion > latestVersion) {
             throw new RuntimeException("database version > this one, You appear to be running an out of date plugin!");
         }
 
         if (installedVersion < latestVersion) {
             // Swap to transaction based mode,
             // Execute each migration script in sequence,
             // commit if successful,
             // rollback and error out if not
             // Should support partial recovery of migration effort, saves
             // current version if successful commit
 
             this.plugin.printCon("Updating database to latest version");
             this.plugin.printCon("Your database: " + installedVersion + " latest: " + latestVersion);
             for (int i = 0; i < 3; i++) {
                 Bukkit.getConsoleSender().sendMessage(
                         ChatColor.RED + "WARNING: DATABASE MIGRATION WILL TAKE A LONG TIME ON LARGE DATABASES.");
             }
             int migrateToVersion = 0;
             try {
                 this.conn.setAutoCommit(false);
 
                 for (migrateToVersion = installedVersion + 1; migrateToVersion <= latestVersion; migrateToVersion++) {
 
                     Map<String, String> k = new HashMap<String, String>();
                     k.put("OLD_TBL", this.plugin.getConfig().getString("stats.database.table", ""));
 
                     executeScript("sql/maintenence/migration/migrate." + migrateToVersion, k);
 
                     this.conn.commit();
                     this.plugin.getConfig().set("stats.database.sql_db_version", migrateToVersion);
                     this.plugin.saveConfig();
 
                 }
 
             } catch (SQLException e) {
                 this.plugin.printCon("An error occured while migrating the database, initiating rollback to version "
                         + (migrateToVersion - 1));
                 plugin.mysqlError(e, "sql/maintenence/migration/migrate." + migrateToVersion);
                 try {
                     this.conn.rollback();
                     throw new BeardStatRuntimeException("Failed to migrate database", e, false);
                 } catch (SQLException se) {
                     this.plugin.printCon("Failed to rollback");
                     plugin.mysqlError(se, null);
                 }
 
 
             }
 
             this.plugin.printCon("Migration successful");
             try {
                 this.conn.setAutoCommit(true);
             } catch (SQLException e) {
                 throw new BeardStatRuntimeException("Failed to start autocommit", e, false);
             }
 
         }
     }
 
     /**
      * Connects to the database
      * @throws SQLException
      */
     private void createConnection() {
 
         this.plugin.printCon("Connecting....");
 
         try {
             this.conn = DriverManager.getConnection(this.connectionUrl, this.connectionProperties);
 
             // conn.setAutoCommit(false);
         } catch (SQLException e) {
             this.plugin.mysqlError(e, null);
             this.conn = null;
         }
 
     }
 
     /**
      * Returns true if connection is still there.
      * @return
      */
     private synchronized boolean checkConnection() {
         this.plugin.printDebugCon("Checking connection");
         try {
             if ((this.conn == null) || !this.conn.isValid(0)) {
                 this.plugin.printDebugCon("Something is derp, rebooting connection.");
                 createConnection();
                 if (this.conn != null) {
                     this.plugin.printDebugCon("Rebuilding statements");
                     prepareStatements();
                 } else {
                     this.plugin.printDebugCon("Reboot failed!");
                 }
 
             }
         } catch (SQLException e) {
             this.conn = null;
             return false;
         } catch (AbstractMethodError e) {
         }
 
         return this.conn != null;
     }
 
     /**
      * Constructs the tables.
      */
     protected void checkAndMakeTable() {
         this.plugin.printCon("Constructing table as needed.");
         executeScript(SQL_CREATE_TABLES);
     }
 
     /**
      * Load statements from jar
      */
     protected void prepareStatements() {
         this.plugin.printDebugCon("Preparing statements");
 
         this.loadEntity = getStatementFromScript(SQL_LOAD_ENTITY);
         this.loadEntityData = getStatementFromScript(SQL_LOAD_ENTITY_DATA);
 
         // Load components
         this.loadDomainsList = getStatementFromScript(SQL_LOAD_DOMAINS);
         this.loadWorldsList = getStatementFromScript(SQL_LOAD_WORLDS);
         this.loadCategoriesList = getStatementFromScript(SQL_LOAD_CATEGORIES);
         this.loadStatisticsList = getStatementFromScript(SQL_LOAD_STATISTICS);
 
         // save components
         this.saveDomain = getStatementFromScript(SQL_SAVE_DOMAIN, Statement.RETURN_GENERATED_KEYS);
         this.saveWorld = getStatementFromScript(SQL_SAVE_WORLD, Statement.RETURN_GENERATED_KEYS);
         this.saveCategory = getStatementFromScript(SQL_SAVE_CATEGORY,
                 Statement.RETURN_GENERATED_KEYS);
         this.saveStatistic = getStatementFromScript(SQL_SAVE_STATISTIC,
                 Statement.RETURN_GENERATED_KEYS);
 
         // save to db
         this.saveEntity = getStatementFromScript(SQL_SAVE_ENTITY, Statement.RETURN_GENERATED_KEYS);
         this.saveEntityData = getStatementFromScript(SQL_SAVE_STAT);
 
         // Maintenance
         this.keepAlive = getStatementFromScript(SQL_KEEP_ALIVE);
         this.listEntities = getStatementFromScript(SQL_LIST_ENTITIES);
         // deleteEntity =
         // conn.prepareStatement(plugin.readSQL(type,"sql/maintenence/deletePlayerFully",
         // tblPrefix));
 
         this.plugin.printDebugCon("Set player stat statement created");
     }
 
     /**
      * Cache entries for quicker resolvement on our end.
      */
     private void cacheComponents() {
         ResultSet rs;
         try {
             //Domains
             rs = loadDomainsList.executeQuery();
             while (rs.next()) {
                 DomainMeta dm = new DomainMeta(
                         rs.getInt("domainId"),
                         rs.getString("domain"));
                 domainMetaMap.put(rs.getString("domain"), dm);
             }
             rs.close();
         } catch (SQLException e) {
             this.plugin.mysqlError(e, SQL_LOAD_DOMAINS);
         }
         try {
             //Worlds
             rs = loadWorldsList.executeQuery();
             while (rs.next()) {
                 WorldMeta wm = new WorldMeta(
                         rs.getInt("worldId"),
                         rs.getString("world"),
                         rs.getString("name"));
                 worldMetaMap.put(rs.getString("world"), wm);
             }
             rs.close();
         } catch (SQLException e) {
             this.plugin.mysqlError(e, SQL_LOAD_WORLDS);
         }
         try {
             //Worlds
             rs = loadCategoriesList.executeQuery();
             while (rs.next()) {
                 CategoryMeta cm = new CategoryMeta(
                         rs.getInt("categoryId"),
                         rs.getString("category"),
                         rs.getString("statwrapper"));
                 categoryMetaMap.put(rs.getString("category"), cm);
             }
             rs.close();
         } catch (SQLException e) {
             this.plugin.mysqlError(e, SQL_LOAD_CATEGORIES);
         }
         try {
             //Worlds
             rs = loadStatisticsList.executeQuery();
             while (rs.next()) {
                 StatisticMeta sm = new StatisticMeta(
                         rs.getInt("statisticId"),
                         rs.getString("statistic"),
                         rs.getString("name"),
                         Formatting.valueOf(rs.getString("formatting")));
                 statisticMetaMap.put(rs.getString("statistic"), sm);
             }
             rs.close();
 
         } catch (SQLException e) {
             this.plugin.mysqlError(e, SQL_LOAD_STATISTICS);
         }
 
     }
 
     @Override
     public Promise<EntityStatBlob> pullStatBlob(String player, String type) {
         return pullStatBlob(player, type, true);
     }
 
     @Override
     public Promise<EntityStatBlob> pullStatBlob(final String player, final String type, final boolean create) {
 
         final Deferred<EntityStatBlob> promise = new Deferred<EntityStatBlob>();
 
         Runnable run = new Runnable() {
             @Override
             public void run() {
                 try {
                     if (!checkConnection()) {
                         plugin.printCon("Database connection error!");
                         promise.reject(new SQLException("Error connecting to database"));
                         return;
                     }
                     long t1 = (new Date()).getTime();
 
                     // Ok, try to get entity from database
                     loadEntity.setString(1, player);
                     loadEntity.setString(2, type);
 
                     ResultSet rs = loadEntity.executeQuery();
                     EntityStatBlob pb = null;
 
                     if (!rs.next()) {
                         if (!create) {
                             promise.reject(new NoRecordFoundException());// Fail
                             // out
                             // here
                             // instead.
                             return;
                         }
 
                         // No player found! Let's create an entry for them!
                         rs.close();
                         rs = null;
                         saveEntity.setString(1, player);
                         saveEntity.setString(2, type);
                         saveEntity.executeUpdate();
                         rs = saveEntity.getGeneratedKeys();
                         rs.next();// load player id
 
                     }
 
                     // make the player object, close out result set.
                     pb = new EntityStatBlob(player, rs.getInt(1), "player");
                     rs.close();
                     rs = null;
 
                     // load all stats data
                     loadEntityData.setInt(1, pb.getEntityID());
                     loadEntityData.setInt(1, pb.getEntityID());
                     plugin.printDebugCon("executing "
                             + loadEntityData);
                     rs = loadEntityData.executeQuery();
 
                     while (rs.next()) {
                         // `domain`,`world`,`category`,`statistic`,`value`
                         IStat ps = pb.getStat(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                         ps.setValue(rs.getInt(5));
                         ps.clearArchive();
                     }
                     rs.close();
 
                     plugin.printDebugCon("time taken to retrieve: "
                             + ((new Date()).getTime() - t1) + " Milliseconds");
 
                     promise.resolve(pb);
                 } catch (SQLException e) {
                     plugin.mysqlError(e,SQL_LOAD_ENTITY_DATA);
                     promise.reject(e);
                 }
            }
         };
 
         this.loadQueue.execute(run);
 
         return promise;
 
     }
 
     @Override
     public void pushStatBlob(EntityStatBlob player) {
 
         synchronized (this.writeCache) {
 
             EntityStatBlob copy = player.cloneForArchive();
 
             if (!this.writeCache.containsKey(player.getName())) {
                 this.writeCache.put(player.getName(), copy);
             }
         }
 
     }
     /**
      * Runner used to flush to database async.
      */
     private Runnable flush = new Runnable() {
         @Override
         public void run() {
             synchronized (writeCache) {
                 try {
                     keepAlive.execute();
                 } catch (SQLException e1) {
                 }
 
                 if (!checkConnection()) {
                     Bukkit.getConsoleSender()
                             .sendMessage(
                             ChatColor.RED
                             + "Could not restablish connection, will try again later, WARNING: CACHE WILL GROW WHILE THIS HAPPENS");
                 } else {
                     plugin.printDebugCon("Saving to database");
                     for (Entry<String, EntityStatBlob> entry : writeCache
                             .entrySet()) {
                         try {
                             EntityStatBlob pb = entry.getValue();
 
                             saveEntityData.clearBatch();
                             for (IStat stat : pb.getStats()) {
                                 saveEntityData.setInt(1,
                                         pb.getEntityID());
                                 saveEntityData.setInt(2, getDomain(stat.getDomain()).getDbId());
                                saveEntityData.setInt(2, getWorld(stat.getWorld()).getDbId());
                                saveEntityData.setInt(2, getCategory(stat.getCategory()).getDbId());
                                saveEntityData.setInt(2, getStatistic(stat.getStatistic()).getDbId());
 
 
                                 saveEntityData.setInt(6,
                                         stat.getValue());
 
                                 saveEntityData.addBatch();
                             }
                             saveEntityData.executeBatch();
 
                         } catch (SQLException e) {
                             plugin.mysqlError(e,SQL_SAVE_STAT);
                             checkConnection();
                         }
                     }
                     plugin.printDebugCon("Clearing write cache");
                     writeCache.clear();
                 }
             }
 
         }
     };
 
     @Override
     public void flushSync() {
         this.plugin.printCon("Flushing in main thread! Game will lag!");
         this.flush.run();
         this.plugin.printCon("Flushed!");
     }
 
     @Override
     public void flush() {
 
         new Thread(this.flush).start();
     }
 
     @Override
     public void deleteStatBlob(String player) {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public boolean hasStatBlob(String player) {
         try {
             this.loadEntity.clearParameters();
             this.loadEntity.setString(1, player);
             this.loadEntity.setString(2, "player");
 
             ResultSet rs = this.loadEntity.executeQuery();
             boolean found = rs.next();
             rs.close();
             return found;
 
         } catch (SQLException e) {
             checkConnection();
         }
         return false;
     }
 
     @Override
     public List<String> getStatBlobsHeld() {
         List<String> list = new ArrayList<String>();
         try {
             this.listEntities.setString(1, "player");
 
             ResultSet rs = this.listEntities.executeQuery();
             while (rs.next()) {
                 list.add(rs.getString(1));
             }
             rs.close();
 
         } catch (SQLException e) {
             checkConnection();
         }
         return list;
     }
 
     public void executeScript(String scriptName) {
         executeScript(scriptName, new HashMap<String, String>());
     }
 
     /**
      * Execute a script
      *
      * @param scriptName name of script (sql/load/loadEntity)
      * @param keys (list of non-standard keys ${KEY_NAME} to replace)
      *
      * Scripts support # for status comments and #!/script/path/here to execute
      * subscripts
      * @throws SQLException
      */
     public void executeScript(String scriptName, final Map<String, String> keys) {
         CallbackMatcher matcher = new CallbackMatcher("\\$\\{([A-Za-z0-9_]*)\\}");
 
         String[] sqlStatements = this.plugin.readSQL(this.type, scriptName, this.tblPrefix).split("\\;");
         for (String s : sqlStatements) {
             String statement = matcher.replaceMatches(s, new Callback() {
                 @Override
                 public String foundMatch(MatchResult result) {
                     if (keys.containsKey(result.group(1))) {
                         return keys.get(result.group(1));
                     }
                     return "";
                 }
             });
 
             if (statement.startsWith("#!")) {
                 String subScript = statement.substring(2);
                 Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Executing : " + subScript);
                 executeScript(subScript, keys);
                 continue;
             } else if (statement.startsWith("#")) {
                 Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Status : " + statement.substring(1));
             } else {
                 try {
                     this.conn.prepareStatement(statement).execute();
                 } catch (SQLException e) {
                     this.plugin.mysqlError(e, scriptName);
                 }
 
             }
         }
 
     }
 
     public PreparedStatement getStatementFromScript(String scriptName, int flags) {
         try {
             return this.conn.prepareStatement(this.plugin.readSQL(this.type, scriptName, this.tblPrefix), flags);
         } catch (SQLException ex) {
             this.plugin.mysqlError(ex, scriptName);
             throw new BeardStatRuntimeException("Failed to create SQL statement for a script", ex, false);
         }
     }
 
     public PreparedStatement getStatementFromScript(String scriptName) {
         try {
             return this.conn.prepareStatement(this.plugin.readSQL(this.type, scriptName, this.tblPrefix));
         } catch (SQLException ex) {
             this.plugin.mysqlError(ex, scriptName);
             throw new BeardStatRuntimeException("Failed to create SQL statement for a script", ex, false);
         }
     }
 
     @Override
     public DomainMeta getDomain(String gameTag) {
         String qGameTag = sanitizeTag(gameTag);
         if (!domainMetaMap.containsKey(qGameTag)) {
             try {
 
                 saveDomain.setString(1, qGameTag);
                 saveDomain.execute();
                 ResultSet rs = saveDomain.getGeneratedKeys();
                 rs.next();
                 domainMetaMap.put(gameTag, new DomainMeta(rs.getInt(1), gameTag));
                 rs.close();
             } catch (SQLException ex) {
                 plugin.mysqlError(ex,SQL_SAVE_DOMAIN);
             }
         }
 
         return domainMetaMap.get(qGameTag);
     }
 
     @Override
     public WorldMeta getWorld(String gameTag) {
 
         if (!worldMetaMap.containsKey(gameTag)) {
             try {
                 saveWorld.setString(1, gameTag);
                 saveWorld.setString(2, gameTag.replaceAll("_", " "));
                 saveWorld.execute();
                 ResultSet rs = saveWorld.getGeneratedKeys();
                 rs.next();
                 worldMetaMap.put(gameTag, new WorldMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " ")));
                 rs.close();
             } catch (SQLException ex) {
                 plugin.mysqlError(ex,SQL_SAVE_WORLD);
             }
         }
 
         return worldMetaMap.get(gameTag);
     }
 
     @Override
     public CategoryMeta getCategory(String gameTag) {
         if (!categoryMetaMap.containsKey(gameTag)) {
             try {
                 saveCategory.setString(1, gameTag);
                 saveCategory.setString(2, gameTag.replaceAll("_", " "));
                 saveCategory.execute();
                 ResultSet rs = saveCategory.getGeneratedKeys();
                 rs.next();
                 categoryMetaMap.put(gameTag, new CategoryMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " ")));
                 rs.close();
             } catch (SQLException ex) {
                plugin.mysqlError(ex,SQL_SAVE_CATEGORY);
             }
         }
 
         return categoryMetaMap.get(gameTag);
     }
 
     @Override
     public StatisticMeta getStatistic(String gameTag) {
         if (!statisticMetaMap.containsKey(gameTag)) {
             try {
                 saveStatistic.setString(1, gameTag);
                 saveStatistic.setString(2, HumanNameGenerator.getNameOf(gameTag));
                 saveStatistic.setString(3, Formatting.none.toString().toLowerCase());
                 saveStatistic.execute();
                 ResultSet rs = saveStatistic.getGeneratedKeys();
                 rs.next();
                 statisticMetaMap.put(gameTag, new StatisticMeta(rs.getInt(1), gameTag, gameTag.replaceAll("_", " "), Formatting.none));
                 rs.close();
             } catch (SQLException ex) {
                 plugin.mysqlError(ex,SQL_SAVE_STATISTIC);
             }
         }
 
         return statisticMetaMap.get(gameTag);
     }
 
     private String sanitizeTag(String gameTag) {
         String truncatedName = gameTag.toLowerCase();
         if (truncatedName.length() > 64) {
             truncatedName = truncatedName.substring(0, 64);
         }
         return truncatedName;
     }
 }
