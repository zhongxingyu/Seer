 package com.carbonfive.db.migration.maven;
 
 import com.carbonfive.db.jdbc.DatabaseType;
 import com.carbonfive.db.jdbc.DatabaseUtils;
 import com.carbonfive.db.migration.DriverManagerMigrationManager;
 import com.carbonfive.db.migration.ResourceMigrationResolver;
 import com.carbonfive.db.migration.SimpleVersionStrategy;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.substring;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.springframework.util.StringUtils;
 
 public abstract class AbstractMigrationMojo extends AbstractMojo
 {
     /**
      * @parameter expression="${project}"
      * @required
      */
     protected MavenProject project;
 
     /** @parameter */
     private String url;
     /** @parameter */
     private String driver;
     /** @parameter */
     private String username;
     /** @parameter */
     private String password = "";
 
     /** @parameter */
     private String databaseType;
 
     /** @parameter */
     private String migrationsPath = "src/main/db/migrations/";
 
     /** @parameter */
     private String versionTable;
     /** @parameter */
     private String versionColumn;
     /** @parameter */
     private String appliedDateColumn;
     /** @parameter */
     private String durationColumn;
     /** @parameter */
     private String createSql;
     /** @parameter */
     private String dropSql;
 
     public abstract void executeMojo() throws MojoExecutionException;
 
     public final void execute() throws MojoExecutionException
     {
         if (isBlank(url) && isBlank(username)) { return; }
 
         password = isBlank(password) ? "" : password;
 
         if (isBlank(driver))
         {
             driver = DatabaseUtils.driverClass(url);
         }
 
         if (databaseType == null)
         {
             databaseType = DatabaseUtils.databaseType(url).toString();
         }
 
         validateConfiguration();
 
         executeMojo();
     }
 
     protected void validateConfiguration() throws MojoExecutionException
     {
         if (isBlank(driver))
         {
             throw new MojoExecutionException("No database driver. Specify one in the plugin configuration.");
         }
 
         if (isBlank(url))
         {
             throw new MojoExecutionException("No database url. Specify one in the plugin configuration.");
         }
 
         if (isBlank(username))
         {
             throw new MojoExecutionException("No database username. Specify one in the plugin configuration.");
         }
 
         try
         {
             DatabaseType.valueOf(databaseType);
         }
         catch (IllegalArgumentException e)
         {
             throw new MojoExecutionException(
                     "Database type of '" + databaseType + "' is invalid.  Valid values: " + StringUtils.arrayToDelimitedString(DatabaseType.values(), ", "));
         }
 
         try
         {
             Class.forName(driver);
         }
         catch (ClassNotFoundException e)
         {
             throw new MojoExecutionException("Can't load driver class " + driver + ". Be sure to include it as a plugin dependency.");
         }
     }
 
     public DriverManagerMigrationManager createMigrationManager()
     {
         DriverManagerMigrationManager manager = new DriverManagerMigrationManager(driver, url, username, password, DatabaseType.valueOf(databaseType));
 
         String path = migrationsPath;
 
         if (path.startsWith("file:"))
         {
             path = substring(path, 5);
         }
         if (!path.startsWith("classpath:") && !path.startsWith("\"") && !path.startsWith("/"))
         {
             path = project.getBasedir().getAbsolutePath() + "/" + path;
         }
         path = separatorsToUnix(path);
 
         manager.setMigrationResolver(new ResourceMigrationResolver(path));
 
         SimpleVersionStrategy strategy = new SimpleVersionStrategy();
         strategy.setVersionTable(defaultIfEmpty(versionTable, SimpleVersionStrategy.DEFAULT_VERSION_TABLE));
         strategy.setVersionColumn(defaultIfEmpty(versionColumn, SimpleVersionStrategy.DEFAULT_VERSION_COLUMN));
         strategy.setAppliedDateColumn(defaultIfEmpty(appliedDateColumn, SimpleVersionStrategy.DEFAULT_APPLIED_DATE_COLUMN));
         strategy.setDurationColumn(defaultIfEmpty(durationColumn, SimpleVersionStrategy.DEFAULT_DURATION_COLUMN));
        manager.setVersionStratgey(strategy);
 
         return manager;
     }
 
     public String getUrl()
     {
         return url;
     }
 
     public String getDriver()
     {
         return driver;
     }
 
     public String getUsername()
     {
         return username;
     }
 
     public String getPassword()
     {
         return password;
     }
 
     public String getDatabaseType()
     {
         return databaseType;
     }
 
     public String getMigrationsPath()
     {
         return migrationsPath;
     }
 
     public String getVersionTable()
     {
         return versionTable;
     }
 
     public String getVersionColumn()
     {
         return versionColumn;
     }
 
     public String getAppliedDateColumn()
     {
         return appliedDateColumn;
     }
 
     public String getDurationColumn()
     {
         return durationColumn;
     }
 
     public String getCreateSql()
     {
         return createSql;
     }
 
     public String getDropSql()
     {
         return dropSql;
     }
 }
