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
 
 import net.dragonzone.promise.Deferred;
 import net.dragonzone.promise.Promise;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 
 import com.tehbeard.BeardStat.BeardStat;
 import com.tehbeard.BeardStat.NoRecordFoundException;
 import com.tehbeard.BeardStat.containers.EntityStatBlob;
 import com.tehbeard.BeardStat.containers.IStat;
 import com.tehbeard.BeardStat.utils.StatisticMetadata;
 import com.tehbeard.BeardStat.utils.StatisticMetadata.Formatting;
 
 /**
  * base class for JDBC based data providers Allows easy development of data
  * providers that make use of JDBC
  * 
  * @author James
  * 
  */
 public abstract class JDBCStatDataProvider implements IStatDataProvider {
 
     protected Connection                    conn;
 
     // protected static PreparedStatement prepGetPlayerStat;
 
     // Load components
     protected PreparedStatement             getDomains;
     protected PreparedStatement             getWorlds;
     protected PreparedStatement             getCategories;
     protected PreparedStatement             getStatistics;
 
     // save components
     protected PreparedStatement             saveDomain;
     protected PreparedStatement             saveWorld;
     protected PreparedStatement             saveCategory;
     protected PreparedStatement             saveStatistic;
 
     // Load data from db
     protected PreparedStatement             loadEntity;
     protected PreparedStatement             loadEntityData;
 
     // save to db
     protected PreparedStatement             saveEntity;
     protected PreparedStatement             saveEntityData;
 
     // Maintenance
     protected PreparedStatement             keepAlive;
     protected PreparedStatement             listEntities;
     protected PreparedStatement             deleteEntity;
     protected PreparedStatement             createTable;
 
     private HashMap<String, EntityStatBlob> writeCache           = new HashMap<String, EntityStatBlob>();
 
     // default connection related configuration
     protected String                        connectionUrl        = "";
     protected Properties                    connectionProperties = new Properties();
     protected String                        tblPrefix            = "stats";
     private String                          type                 = "sql";
 
     // ID Cache
     private Map<String, Integer>            domains              = new HashMap<String, Integer>();
     private Map<String, Integer>            worlds               = new HashMap<String, Integer>();
     private Map<String, Integer>            categories           = new HashMap<String, Integer>();
 
     // private WorkQueue loadQueue = new WorkQueue(1);
     private ExecutorService                 loadQueue            = Executors.newSingleThreadExecutor();
 
     protected BeardStat                     plugin;
 
     public JDBCStatDataProvider(BeardStat plugin, String type, String driverClass) {
         this.type = type;
         this.plugin = plugin;
         try {
             Class.forName(driverClass);// load driver
         } catch (ClassNotFoundException e) {
             plugin.printCon("JDBC " + driverClass + "Library not found!");
         }
     }
 
     protected void initialise() throws SQLException {
         createConnection();
 
         checkForMigration();
 
         checkAndMakeTable();
         prepareStatements();
 
         String mcver = this.plugin.getConfig().getString("general.mcver");
         String implver = Bukkit.getVersion();
 
         if (!implver.equals(mcver)) {
             this.plugin.printCon("Different version to last boot! Running built in metadata script.");
             String sql[] = this.plugin.readSQL(this.type, "sql/maintenence/updateMetadata", this.tblPrefix)
                     .split("\\;");
             PreparedStatement updateMetadata;
             for (String s : sql) {
                 updateMetadata = this.conn.prepareStatement(s);
                 updateMetadata.execute();
             }
             this.plugin.getConfig().set("general.mcver", implver);
             this.plugin.saveConfig();
 
         }
 
         cacheComponents();
     }
 
     /**
      * checks config in data folder against default (current versions config) If
      * version conflicts it will attempt to run migration scripts sequentially
      * to upgrade
      * 
      * @throws SQLException
      */
     private void checkForMigration() throws SQLException {
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
             this.conn.setAutoCommit(false);
 
             // Begin ze migration
             PreparedStatement migrate;
 
             int curVersion = 0;
             try {
 
                 for (curVersion = installedVersion + 1; curVersion <= latestVersion; curVersion++) {
 
                     String[] sql = this.plugin
                             .readSQL(this.type, "sql/maintenence/migration/migrate." + curVersion, this.tblPrefix)
                             .replaceAll("\\$\\{OLD_TBL\\}", this.plugin.getConfig().getString("stats.database.table"))
                             .split("\\;");
                     for (String s : sql) {
                         if (s.startsWith("#")) {
                             Bukkit.getConsoleSender().sendMessage(
                                     ChatColor.YELLOW + "Migration status : " + s.substring(1));
                         } else {
                             migrate = this.conn.prepareStatement(s);
                             migrate.execute();
                         }
                     }
 
                     this.conn.commit();
                     this.plugin.getConfig().set("stats.database.sql_db_version", curVersion);
                     this.plugin.saveConfig();
 
                 }
 
             } catch (SQLException e) {
                 this.plugin.printCon("An error occured while migrating the database, initiating rollback to version "
                         + (curVersion - 1));
                 this.plugin.printCon("Begining database error dump");
                 // plugin.mysqlError(e);
                 this.conn.rollback();
                 throw e;
 
             }
 
             this.plugin.printCon("Migration successful");
             this.conn.setAutoCommit(true);
 
         }
     }
 
     /**
      * Connection to the database.
      * 
      * @throws SQLException
      */
     private void createConnection() {
 
         this.plugin.printCon("Connecting....");
 
         try {
             this.conn = DriverManager.getConnection(this.connectionUrl, this.connectionProperties);
 
             // conn.setAutoCommit(false);
         } catch (SQLException e) {
             this.plugin.mysqlError(e);
             this.conn = null;
         }
 
     }
 
     /**
      * 
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
         this.plugin.printDebugCon(("Checking is " + this.conn) != null ? "up" : "down");
         return this.conn != null;
     }
 
     protected void checkAndMakeTable() {
         this.plugin.printCon("Constructing table as needed.");
 
         try {
 
             String[] creates = this.plugin.readSQL(this.type, "sql/maintenence/create.tables", this.tblPrefix)
                     .replaceAll("\n|\r", "").split(";");
             for (String sql : creates) {
                 this.plugin.printDebugCon(sql);
                 this.conn.prepareStatement(sql).execute();
             }
         } catch (SQLException e) {
             this.plugin.mysqlError(e);
         }
     }
 
     /**
      * Load statements from jar
      */
     protected void prepareStatements() {
         try {
             this.plugin.printDebugCon("Preparing statements");
 
             this.loadEntity = this.conn.prepareStatement(this.plugin.readSQL(this.type, "sql/load/getEntity",
                     this.tblPrefix));
             this.loadEntityData = this.conn.prepareStatement(this.plugin.readSQL(this.type, "sql/load/getEntityData",
                     this.tblPrefix));
 
             // Load components
             this.getDomains = this.conn.prepareStatement(this.plugin.readSQL(this.type,
                     "sql/load/components/getDomains", this.tblPrefix));
             this.getWorlds = this.conn.prepareStatement(this.plugin.readSQL(this.type, "sql/load/components/getWorlds",
                     this.tblPrefix));
             this.getCategories = this.conn.prepareStatement(this.plugin.readSQL(this.type,
                     "sql/load/components/getCategories", this.tblPrefix));
             this.getStatistics = this.conn.prepareStatement(this.plugin.readSQL(this.type,
                     "sql/load/components/getStatistics", this.tblPrefix));
 
             // save components
             this.saveDomain = this.conn.prepareStatement(
                     this.plugin.readSQL(this.type, "sql/save/components/saveDomain", this.tblPrefix),
                     Statement.RETURN_GENERATED_KEYS);
             this.saveWorld = this.conn.prepareStatement(
                     this.plugin.readSQL(this.type, "sql/save/components/saveWorld", this.tblPrefix),
                     Statement.RETURN_GENERATED_KEYS);
             this.saveCategory = this.conn.prepareStatement(
                     this.plugin.readSQL(this.type, "sql/save/components/saveCategory", this.tblPrefix),
                     Statement.RETURN_GENERATED_KEYS);
             this.saveStatistic = this.conn.prepareStatement(
                     this.plugin.readSQL(this.type, "sql/save/components/saveStatistic", this.tblPrefix),
                     Statement.RETURN_GENERATED_KEYS);
 
             // save to db
             this.saveEntity = this.conn.prepareStatement(
                     this.plugin.readSQL(this.type, "sql/save/saveEntity", this.tblPrefix),
                     Statement.RETURN_GENERATED_KEYS);
             this.saveEntityData = this.conn.prepareStatement(this.plugin.readSQL(this.type, "sql/save/saveStat",
                     this.tblPrefix));
 
             // Maintenance
             this.keepAlive = this.conn.prepareStatement(this.plugin.readSQL(this.type, "sql/maintenence/keepAlive",
                     this.tblPrefix));
             this.listEntities = this.conn.prepareStatement(this.plugin.readSQL(this.type,
                     "sql/maintenence/listEntities", this.tblPrefix));
             // deleteEntity =
             // conn.prepareStatement(plugin.readSQL(type,"sql/maintenence/deletePlayerFully",
             // tblPrefix));
 
             this.plugin.printDebugCon("Set player stat statement created");
             this.plugin.printCon("Initaised MySQL Data Provider.");
         } catch (SQLException e) {
             this.plugin.mysqlError(e);
         }
     }
 
     private void cacheComponents() {
         try {
             cacheComponent(this.domains, this.getDomains);
             cacheComponent(this.worlds, this.getWorlds);
             cacheComponent(this.categories, this.getCategories);
             cacheStatistics();
         } catch (SQLException e) {
             this.plugin.mysqlError(e);
         }
     }
 
     private void cacheStatistics() throws SQLException {
         ResultSet rs = this.getStatistics.executeQuery();
         while (rs.next()) {
            new StatisticMetadata(rs.getInt(1), rs.getString(2).toLowerCase(), rs.getString(3), Formatting.valueOf(rs.getString(4)));
         }
     }
 
     private void cacheComponent(Map<String, Integer> mapTo, PreparedStatement statement) throws SQLException {
         ResultSet rs = statement.executeQuery();
         while (rs.next()) {
             mapTo.put(rs.getString(2), rs.getInt(1));
         }
 
         rs.close();
     }
 
     private int getStatisticId(String name) throws SQLException {
 
         StatisticMetadata meta = StatisticMetadata.getMeta(name);
         if (meta == null) {
             this.plugin.printDebugCon("Recording new component: " + name);
            this.saveStatistic.setString(1, name.toLowerCase());
             this.saveStatistic.setString(2, StatisticMetadata.localizedName(name)); // See
                                                                                     // if
                                                                                     // we
                                                                                     // can
                                                                                     // generate
                                                                                     // a
                                                                                     // localized
                                                                                     // name
                                                                                     // for
                                                                                     // this
                                                                                     // ourselves.
             this.saveStatistic.setString(3, Formatting.none.toString().toLowerCase());
             this.saveStatistic.execute();
             ResultSet rs = this.saveStatistic.getGeneratedKeys();
             rs.next();
             meta = new StatisticMetadata(rs.getInt(1), name, name, Formatting.none);
             rs.close();
         }
 
         return meta.getId();
 
     }
 
     private int getComponentId(Map<String, Integer> mapTo, PreparedStatement statement, String name)
             throws SQLException {
         if (!mapTo.containsKey(name)) {
             this.plugin.printDebugCon("Recording new component: " + name);
             statement.setString(1, name);
             try {
                 statement.setString(2, name);
             } catch (Exception e) {
             }// TODO - Need to seperate out each element to it's own getId
              // system I think :/
             statement.execute();
             ResultSet rs = statement.getGeneratedKeys();
             rs.next();
             mapTo.put(name, rs.getInt(1));
             rs.close();
             this.plugin.printDebugCon(name + " : " + mapTo.get(name));
         }
 
         return mapTo.get(name);
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
                         JDBCStatDataProvider.this.plugin.printCon("Database connection error!");
                         promise.reject(new SQLException("Error connecting to database"));
                         return;
                     }
                     long t1 = (new Date()).getTime();
 
                     // Ok, try to get entity from database
                     JDBCStatDataProvider.this.loadEntity.setString(1, player);
                     JDBCStatDataProvider.this.loadEntity.setString(2, type);
 
                     ResultSet rs = JDBCStatDataProvider.this.loadEntity.executeQuery();
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
                         JDBCStatDataProvider.this.saveEntity.setString(1, player);
                         JDBCStatDataProvider.this.saveEntity.setString(2, type);
                         JDBCStatDataProvider.this.saveEntity.executeUpdate();
                         rs = JDBCStatDataProvider.this.saveEntity.getGeneratedKeys();
                         rs.next();// load player id
 
                     }
 
                     // make the player object, close out result set.
                     pb = new EntityStatBlob(player, rs.getInt(1), "player");
                     rs.close();
                     rs = null;
 
                     // load all stats data
                     JDBCStatDataProvider.this.loadEntityData.setInt(1, pb.getEntityID());
                     JDBCStatDataProvider.this.loadEntityData.setInt(1, pb.getEntityID());
                     JDBCStatDataProvider.this.plugin.printDebugCon("executing "
                             + JDBCStatDataProvider.this.loadEntityData);
                     rs = JDBCStatDataProvider.this.loadEntityData.executeQuery();
 
                     while (rs.next()) {
                         // `domain`,`world`,`category`,`statistic`,`value`
                         IStat ps = pb.getStat(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
                         ps.setValue(rs.getInt(5));
                         ps.clearArchive();
                     }
                     rs.close();
 
                     JDBCStatDataProvider.this.plugin.printDebugCon("time taken to retrieve: "
                             + ((new Date()).getTime() - t1) + " Milliseconds");
 
                     promise.resolve(pb);
                     return;
                 } catch (SQLException e) {
                     JDBCStatDataProvider.this.plugin.mysqlError(e);
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
 
     private Runnable flush = new Runnable() {
 
                                @Override
                                public void run() {
                                    synchronized (JDBCStatDataProvider.this.writeCache) {
                                        try {
                                            JDBCStatDataProvider.this.keepAlive.execute();
                                        } catch (SQLException e1) {
                                        }
 
                                        if (!checkConnection()) {
                                            Bukkit.getConsoleSender()
                                                    .sendMessage(
                                                            ChatColor.RED
                                                                    + "Could not restablish connection, will try again later, WARNING: CACHE WILL GROW WHILE THIS HAPPENS");
                                        } else {
                                            JDBCStatDataProvider.this.plugin.printDebugCon("Saving to database");
                                            for (Entry<String, EntityStatBlob> entry : JDBCStatDataProvider.this.writeCache
                                                    .entrySet()) {
                                                try {
                                                    EntityStatBlob pb = entry.getValue();
 
                                                    JDBCStatDataProvider.this.saveEntityData.clearBatch();
                                                    for (IStat stat : pb.getStats()) {
                                                        JDBCStatDataProvider.this.saveEntityData.setInt(1,
                                                                pb.getEntityID());
                                                        JDBCStatDataProvider.this.saveEntityData.setInt(
                                                                2,
                                                                getComponentId(JDBCStatDataProvider.this.domains,
                                                                        JDBCStatDataProvider.this.saveDomain,
                                                                        stat.getDomain()));
                                                        JDBCStatDataProvider.this.saveEntityData.setInt(
                                                                3,
                                                                getComponentId(JDBCStatDataProvider.this.worlds,
                                                                        JDBCStatDataProvider.this.saveWorld,
                                                                        stat.getWorld()));
                                                        JDBCStatDataProvider.this.saveEntityData.setInt(
                                                                4,
                                                                getComponentId(JDBCStatDataProvider.this.categories,
                                                                        JDBCStatDataProvider.this.saveCategory,
                                                                        stat.getCategory()));
                                                        JDBCStatDataProvider.this.saveEntityData.setInt(5,
                                                                getStatisticId(stat.getStatistic()));
                                                        JDBCStatDataProvider.this.saveEntityData.setInt(6,
                                                                stat.getValue());
 
                                                        JDBCStatDataProvider.this.saveEntityData.addBatch();
                                                    }
                                                    JDBCStatDataProvider.this.saveEntityData.executeBatch();
 
                                                } catch (SQLException e) {
                                                    JDBCStatDataProvider.this.plugin.mysqlError(e);
                                                    checkConnection();
                                                }
                                            }
                                            JDBCStatDataProvider.this.plugin.printDebugCon("Clearing write cache");
                                            JDBCStatDataProvider.this.writeCache.clear();
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
 }
