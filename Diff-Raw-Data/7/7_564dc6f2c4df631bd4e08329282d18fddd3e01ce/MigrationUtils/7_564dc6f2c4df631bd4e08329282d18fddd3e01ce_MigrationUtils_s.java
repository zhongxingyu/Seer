 package play.modules.carbonate;
 
 import com.carbonfive.db.migration.DataSourceMigrationManager;
 import com.carbonfive.db.migration.ResourceMigrationResolver;
 import org.apache.commons.lang.StringUtils;
 import play.Play;
 import play.db.DB;
 import play.exceptions.UnexpectedException;
 
 import java.io.*;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * @author huljas
  */
 public class MigrationUtils {
 
     public static void runMigrations(String pattern) {
         try {
             DataSourceMigrationManager manager = new DataSourceMigrationManager(DB.datasource);
             ResourceMigrationResolver resolver = new ResourceMigrationResolver(pattern);
             manager.setMigrationResolver(resolver);
             manager.migrate();
         } catch (Exception e) {
             throw new UnexpectedException("Database migrations failed", e);
         }
     }
 
     public static String getPath() {
        return Play.applicationPath.getPath() + "/" + Play.configuration.getProperty("carbonate.path");
     }
 
     public static String getDefaultDialect(String driver) {
         String dialect = Play.configuration.getProperty("jpa.dialect");
         if (dialect != null) {
             return dialect;
         } else if (driver.equals("org.hsqldb.jdbcDriver")) {
             return "org.hibernate.dialect.HSQLDialect";
         } else if (driver.equals("com.mysql.jdbc.Driver")) {
             return "play.db.jpa.MySQLDialect";
         } else if (driver.equals("org.postgresql.Driver")) {
             return "org.hibernate.dialect.PostgreSQLDialect";
         } else if (driver.toLowerCase().equals("com.ibm.db2.jdbc.app.DB2Driver")) {
             return "org.hibernate.dialect.DB2Dialect";
         } else if (driver.equals("com.ibm.as400.access.AS400JDBCDriver")) {
             return "org.hibernate.dialect.DB2400Dialect";
         } else if (driver.equals("com.ibm.as400.access.AS390JDBCDriver")) {
             return "org.hibernate.dialect.DB2390Dialect";
         } else if (driver.equals("oracle.jdbc.driver.OracleDriver")) {
             return "org.hibernate.dialect.Oracle9iDialect";
         } else if (driver.equals("com.sybase.jdbc2.jdbc.SybDriver")) {
             return "org.hibernate.dialect.SybaseAnywhereDialect";
         } else if ("com.microsoft.jdbc.sqlserver.SQLServerDriver".equals(driver)) {
             return "org.hibernate.dialect.SQLServerDialect";
         } else if ("com.sap.dbtech.jdbc.DriverSapDB".equals(driver)) {
             return "org.hibernate.dialect.SAPDBDialect";
         } else if ("com.informix.jdbc.IfxDriver".equals(driver)) {
             return "org.hibernate.dialect.InformixDialect";
         } else if ("com.ingres.jdbc.IngresDriver".equals(driver)) {
             return "org.hibernate.dialect.IngresDialect";
         } else if ("progress.sql.jdbc.JdbcProgressDriver".equals(driver)) {
             return "org.hibernate.dialect.ProgressDialect";
         } else if ("com.mckoi.JDBCDriver".equals(driver)) {
             return "org.hibernate.dialect.MckoiDialect";
         } else if ("InterBase.interclient.Driver".equals(driver)) {
             return "org.hibernate.dialect.InterbaseDialect";
         } else if ("com.pointbase.jdbc.jdbcUniversalDriver".equals(driver)) {
             return "org.hibernate.dialect.PointbaseDialect";
         } else if ("com.frontbase.jdbc.FBJDriver".equals(driver)) {
             return "org.hibernate.dialect.FrontbaseDialect";
         } else if ("org.firebirdsql.jdbc.FBDriver".equals(driver)) {
             return "org.hibernate.dialect.FirebirdDialect";
         } else {
             throw new UnsupportedOperationException("I do not know which hibernate dialect to use with "
                     + driver + " and I cannot guess it, use the property jpa.dialect in config file");
         }
     }
 }
