 package com.sampullara.db;
 
 import com.sampullara.cli.Args;
 import com.sampullara.cli.Argument;
 
 import javax.sql.DataSource;
 import java.io.*;
 import java.sql.*;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Migrate databases from one version to the next to ensure that your client code always matches your database.
  * <p/>
  * User: sam
  * Date: Sep 8, 2007
  * Time: 12:26:10 PM
  */
 public class Migrate {
 
     // Logging
     public static final Logger logger = Logger.getLogger("com.sampullara.db.Migrate");
 
     // Properties
     @Argument(required = true, description = "The database URL")
     private String url;
     @Argument(required = true, description = "The database driver classname")
     private String driver;
     @Argument(required = true, description = "The database user")
     private String user;
     @Argument(required = true, description = "The database password")
     private String password;
     @Argument(description = "The client version")
     private Integer version;
     @Argument(description = "Automatically update the database to the latest possible")
     private Boolean auto = false;
     @Argument(description = "The name of the table within the database to store the db version within")
     private String tablename = "db_version";
     @Argument(required = true, alias = "package", description = "Package or directory within which the database migration scripts/classes are stored")
     private String packageName;
 
     // Internal state
     private DataSource datasource;
     private Connection connection;
     private Properties properties;
 
     // Static state
     private static Pattern pattern = Pattern.compile(".*?;");
     private static Properties db;
 
     static {
         db = new Properties();
         InputStream is =
                 Thread.currentThread().getContextClassLoader().getResourceAsStream("com/sampullara/db/db.properties");
         try {
             db.load(is);
             is.close();
         } catch (IOException e) {
             throw new RuntimeException("Failed to initialize migration, no db.properties found", e);
         }
     }
 
     /**
      * Command line migration tool
      *
      * @param args Pass the arguments you need.
      * @throws MigrationException If it fails to migrate the database given
      */
     public static void main(String[] args) throws MigrationException {
         Migrate migrate;
         try {
             migrate = new Migrate(args);
         } catch (Exception e) {
             return;
         }
         migrate.migrate();
     }
 
     /**
      * Command line version
      *
      * @param args Command line to do the migration
      */
     public Migrate(String[] args) {
         try {
             Args.parse(this, args);
         } catch (IllegalArgumentException iae) {
             System.err.println(iae);
             Args.usage(this);
             throw iae;
         }
         properties = new Properties();
         properties.put("user", user);
         properties.put("password", password);
     }
 
     /**
      * Uses a property file to initialize the migration
      *
      * @param p The properties read from the property file
      */
     public Migrate(Properties p) {
         Args.parse(this, p);
         this.properties = p;
     }
 
     /**
      * Full API for the migration class
      *
      * @param packageName Package of the migration scripts / classes
      * @param url         URL of the database
      * @param driver      Class name of the database driver
      * @param version     Current version of the client classes
      * @param properties  Database connection properties
      */
     public Migrate(String packageName, String url, String driver, int version, Properties properties) {
         this.url = url;
         this.driver = driver;
         this.properties = properties;
         this.version = version;
         this.packageName = packageName;
     }
 
     /**
      * Full API for the migration class
      *
      * @param packageName Package of the migration scripts / classes
      * @param datasource  data source (e.g. from a JNDI lookup)
      * @param version     Current version of the client classes
      */
     public Migrate(String packageName, DataSource datasource, int version) {
         this.datasource = datasource;
         this.version = version;
         this.packageName = packageName;
     }
 
     /**
      * Migrate the database from the current database version to the client version using
      * the following resources in this order of operations:
      * <p/>
      * <ol>
      * <li>Attempt to use a migration class: packageName + databaseName + ".Migrate" + dbVersion</li>
      * <li>If class in 1 not found, use a migration script: pacakge dir + "/" + databaseName + "/migrate" + dbVersion + ".sql"</li>
      * <li>If script in 2 not found, attempt to use a generic migration class: packageName + ".Migrate" + dbVersion</li>
      * <li>If class in 3 not found, use a generic migration script: pacakge dir + "/migrate" + dbVersion + ".sql"</li>
      * </ol>
      *
      * @throws MigrationException Will fail if the migration is unsuccessful
      */
     public boolean migrate() throws MigrationException {
         if (!auto && version == null) {
             throw new MigrationException("You must either set a client version or enable auto migration");
         }
         boolean migrated = false;
         Connection conn = getConnection();
         try {
             // We will try and do all the DDL in a transaction so that we can
             // roll it back on failure.  Not all databases will support this,
             // be very careful.
             conn.setAutoCommit(false);
         } catch (SQLException e) {
             throw new MigrationException("Failed to set autocommit to false", e);
         }
 
         // Have to see if we can lock
         int dbVersion = getDBVersion();
 
         // Here we need to LOCK the database version table so that if someone else tries to update
         // at the same time they are blocked until we complete the operation.  Need to verify
         // the right way to do this for various databases. If you are starting a new database with
         // no version table this will not protect you.
         if (dbVersion > 0) lockDB(conn);
 
         try {
             // Get the current database version and check to make sure we need to do work.
             while (needsMigrate(dbVersion = getDBVersion())) {
                 if (databaseSpecificClassMigrationFrom(conn, dbVersion) ||
                         databaseSpecificScriptMigrationFrom(conn, dbVersion) ||
                         genericClassMigrationFrom(conn, dbVersion) ||
                         genericScriptMigrationFrom(conn, dbVersion) ||
                         databaseSpecificClassMigrationTo(conn, dbVersion) ||
                         databaseSpecificScriptMigrationTo(conn, dbVersion) ||
                         genericClassMigrationTo(conn, dbVersion) ||
                         genericScriptMigrationTo(conn, dbVersion)
                         ) {
                     advanceVersion(dbVersion);
                     migrated = true;
                 } else {
                     if (auto) break;
                     throw new MigrationException("No migration found: " + dbVersion);
                 }
             }
         } finally {
             // Complete the transaction
             unlockDB(conn);
             try {
                 conn.commit();
             } catch (SQLException e) {
                 logger.log(Level.SEVERE, "Failed to commit", e);
             } finally {
                 try {
                     conn.close();
                 } catch (SQLException e) {
                     // Do nothing if we can't close the connection
                     logger.log(Level.WARNING, "Couldn't close a database connection, we may be leaking them");
                 }
                 connection = null;
             }
         }
         return migrated;
     }
 
     private void lockDB(Connection conn) throws MigrationException {
         try {
             String dbname = getDatabaseName(conn);
             String lockSQL = (String) db.get("lock_" + dbname);
             if (lockSQL != null) {
                 Statement st = conn.createStatement();
                 lockSQL = lockSQL.replace(":table", tablename);
                 st.execute(lockSQL);
             }
         } catch (SQLException e) {
             throw new MigrationException("Could not lock database", e);
         }
     }
 
     private void unlockDB(Connection conn) throws MigrationException {
         try {
             String dbname = getDatabaseName(conn);
             String unlockSQL = (String) db.get("unlock_" + dbname);
             if (unlockSQL != null) {
                 Statement st = conn.createStatement();
                 unlockSQL = unlockSQL.replace(":table", tablename);
                 st.execute(unlockSQL);
             }
         } catch (SQLException e) {
             throw new MigrationException("Could not unlock database", e);
         }
     }
 
     private boolean classMigrator(Connection conn, String className) throws MigrationException {
         // Remove dashes from the classnames
         className = className.replace("-", "");
         try {
             Class migratorClass = Class.forName(className);
             Migrator migrator;
             try {
                 migrator = (Migrator) migratorClass.newInstance();
             } catch (InstantiationException e) {
                 throw new MigrationException("Failure constructing migrator: " + className, e);
             } catch (IllegalAccessException e) {
                 throw new MigrationException("Migrator constructor not accessible: " + className, e);
             }
             logger.info("Using class: " + className);
             migrator.migrate(conn);
             return true;
         } catch (ClassNotFoundException e) {
             // Go on to the next test
         }
         return false;
     }
 
     private void advanceVersion(int dbVersion) throws MigrationException {
         int newVersion = getDBVersion();
         if (newVersion == dbVersion) {
             // Auto advance the version
             Connection conn = getConnection();
             PreparedStatement ps = null;
             try {
                 newVersion = dbVersion + 1;
                 ps = conn.prepareStatement("UPDATE " + tablename + " SET version=?");
                 ps.setInt(1, newVersion);
                 int rows = ps.executeUpdate();
                 if (rows != 1) {
                     throw new MigrationException("Failed to update database version from " + dbVersion + " to " + newVersion);
                 }
             } catch (SQLException e) {
                 throw new MigrationException("Failed to update database version from " + dbVersion + " to " + (dbVersion + 1), e);
             } finally {
                 if (ps != null) try {
                     ps.close();
                 } catch (SQLException e) {
                     // Ignore failures to close things
                 }
             }
             logger.log(Level.INFO, "Automatically incremented database from " + dbVersion + " to " + newVersion);
         } else {
             logger.log(Level.INFO, "Manually updated database from " + dbVersion + " to " + newVersion);
         }
     }
 
     private boolean databaseSpecificClassMigrationFrom(Connection conn, int dbVersion) throws MigrationException {
         String databaseName = getDatabaseName(conn);
         String className = packageName + "." + databaseName + ".MigrateFrom" + dbVersion;
         return classMigrator(conn, className);
     }
 
     private boolean genericClassMigrationFrom(Connection conn, int dbVersion) throws MigrationException {
         String className = packageName + ".MigrateFrom" + dbVersion;
         return classMigrator(conn, className);
     }
 
     private boolean databaseSpecificScriptMigrationFrom(Connection conn, int dbVersion) throws MigrationException {
         String databaseName = getDatabaseName(conn);
         String scriptName = packageName.replace(".", "/") + "/" + databaseName + "/migratefrom" + dbVersion + ".sql";
         return scriptMigrator(conn, scriptName);
     }
 
     private boolean genericScriptMigrationFrom(Connection conn, int dbVersion) throws MigrationException {
         String scriptName = packageName.replace(".", "/") + "/" + "migratefrom" + dbVersion + ".sql";
         return scriptMigrator(conn, scriptName);
     }
 
     private boolean databaseSpecificClassMigrationTo(Connection conn, int dbVersion) throws MigrationException {
         String databaseName = getDatabaseName(conn);
         String className = packageName + "." + databaseName + ".MigrateTo" + (dbVersion + 1);
         return classMigrator(conn, className);
     }
 
     private boolean genericClassMigrationTo(Connection conn, int dbVersion) throws MigrationException {
         String className = packageName + ".MigrateTo" + (dbVersion + 1);
         return classMigrator(conn, className);
     }
 
     private boolean databaseSpecificScriptMigrationTo(Connection conn, int dbVersion) throws MigrationException {
         String databaseName = getDatabaseName(conn);
         String scriptName =
                 packageName.replace(".", "/") + "/" + databaseName + "/migrateto" + (dbVersion + 1) + ".sql";
         return scriptMigrator(conn, scriptName);
     }
 
     private boolean genericScriptMigrationTo(Connection conn, int dbVersion) throws MigrationException {
         String scriptName = packageName.replace(".", "/") + "/" + "migrateto" + (dbVersion + 1) + ".sql";
         return scriptMigrator(conn, scriptName);
     }
 
     /**
      * Pass the database connection and then a script name that will either be in the classpath or relative
      * to the current directory.
      *
      * @param conn       The database connection against which to execute the sql statements
      * @param scriptName The name of the file or resource to execute
      * @return Script found
      * @throws MigrationException If the script was found but could not be executed to completion.
      */
     public static boolean scriptMigrator(Connection conn, String scriptName) throws MigrationException {
         InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(scriptName);
         if (is == null) {
             File file = new File(scriptName);
             if (file.exists()) {
                 try {
                     is = new BufferedInputStream(new FileInputStream(file));
                 } catch (FileNotFoundException e) {
                     throw new MigrationException("Found script but it is unreadable: " + file, e);
                 }
             }
         }
         if (is != null) {
             logger.info("Using script: " + scriptName);
             // Pull the entire script file into a char buffer
             // Skip lines that start with #
             StringBuilder sb = new StringBuilder();
             int num = 1;
             try {
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                 String line;
                 while ((line = br.readLine()) != null) {
                     if (!line.startsWith("#") && !line.startsWith("--")) {
                         sb.append(line);
                         if (!line.endsWith(";")) {
                             sb.append(" ");
                         }
                     }
                     num++;
                 }
             } catch (IOException e) {
                 throw new MigrationException(scriptName + ":\n" + sb + "\nFailed to read script at line: " + num, e);
             } finally {
                 try {
                     is.close();
                 } catch (IOException e) {
                     // Ignore exceptions on close
                 }
             }
             // Now we have read the whole script, now we need to parse it.
             Matcher matcher = pattern.matcher(sb);
            num = 1;
             while (matcher.find()) {
                 String sql = matcher.group();
                 sql = sql.substring(0, sql.length() - 1).trim();
                 Statement st = null;
                 try {
                     st = conn.createStatement();
                     st.execute(sql);
                 } catch (SQLException e) {
                     throw new MigrationException("Failed to execute SQL statement #" + num + ": " + sql, e);
                 } finally {
                     if (st != null) try {
                         st.close();
                     } catch (SQLException e) {
                         logger.log(Level.WARNING, "Failed to close statement, might be leaking them", e);
                     }
                 }
                num++;
             }
             return true;
         }
         return false;
     }
 
     private String getDatabaseName(Connection conn) throws MigrationException {
         String databaseName;
         try {
             databaseName = conn.getMetaData().getDatabaseProductName();
         } catch (SQLException e) {
             throw new MigrationException("Could not get database name", e);
         }
         return databaseName.toLowerCase();
     }
 
     /**
      * Does the database need to be migrated?  You only should use this if you need to interact with the user
      * or fail if its needs to be migrated.  For automated migration, call migrate() directly.
      *
      * @return true if you need to call migrate
      * @throws MigrationException Will fail if it can't get the version from the database
      */
     public boolean needsMigrate() throws MigrationException {
         boolean needsMigrate;
         int dbVersion = getDBVersion();
         needsMigrate = needsMigrate(dbVersion);
         return needsMigrate;
     }
 
     private boolean needsMigrate(int dbVersion) throws MigrationException {
         if (auto) {
             return true;
         } else {
             boolean needsMigrate;
             if (dbVersion == version) {
                 needsMigrate = false;
             } else {
                 if (dbVersion > version) {
                     throw new MigrationException("Client version older than database version: " + version + " < " + dbVersion);
                 }
                 needsMigrate = true;
             }
             return needsMigrate;
         }
     }
 
     /**
      * Get the current database version
      *
      * @return returns the current version of the database
      * @throws MigrationException Will fail if there is more than one row or the table is lacking a version
      */
     public int getDBVersion() throws MigrationException {
         int dbVersion;
         PreparedStatement ps;
         try {
             Connection conn = getConnection();
             ps = conn.prepareStatement("SELECT version FROM " + tablename);
             try {
                 ResultSet rs = ps.executeQuery();
                 try {
                     if (rs.next()) {
                         dbVersion = rs.getInt(1);
                         if (rs.next()) {
                             throw new MigrationException("Too many version in table: " + tablename);
                         }
                     } else {
                         ps.close();
                         ps = conn.prepareStatement("INSERT INTO " + tablename + " (version) VALUES (?)");
                         ps.setInt(1, 1);
                         try {
                             ps.executeUpdate();
                         } finally {
                             ps.close();
                         }
                         dbVersion = 1;
                     }
                 } finally {
                     rs.close();
                 }
             } finally {
                 ps.close();
             }
         } catch (SQLException e) {
             // We are going to have to make an assumption that the database exists but there is no current
             // database version and use the migrate0 script.
             dbVersion = 0;
             // We should reset the connection state at this point
             Connection conn = getConnection();
             try {
                 if (!conn.getAutoCommit()) {
                     conn.rollback();
                 }
                 conn.setAutoCommit(false);
             } catch (SQLException e1) {
                 throw new MigrationException("Could not reset transaction state", e1);
             }
         }
         return dbVersion;
     }
 
    Connection getConnection() throws MigrationException {
         try {
             if (connection == null || connection.isClosed()) {
                 if (datasource == null) {
                     Driver dbdriver = (Driver) Class.forName(driver).newInstance();
                     try {
                         connection = dbdriver.connect(url, properties);
                     } catch (SQLException e) {
                         throw new MigrationException("Could not connect to database: " + url, e);
                     }
                 } else {
                     try {
                         logger.log(Level.INFO, "Using supplied datasource: " + datasource);
                         connection = datasource.getConnection();
                     } catch (SQLException e) {
                         throw new MigrationException("Could not connect to datasource: " + datasource, e);
                     }
                 }
             }
         } catch (SQLException e) {
             throw new MigrationException("Could not examine connection", e);
         } catch (InstantiationException e) {
             throw new MigrationException("Could not instantiate driver", e);
         } catch (IllegalAccessException e) {
             throw new MigrationException("Could not access driver constructor", e);
         } catch (ClassNotFoundException e) {
             throw new MigrationException("Could not find driver class in classpath: " + driver, e);
         } catch (Exception e) {
             throw new MigrationException("Some other failure to connect: " + url + ", " + properties, e);
         }
         return connection;
     }
 
 }
