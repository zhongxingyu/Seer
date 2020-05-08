 package com.carbonfive.db.migration;
 
 import com.carbonfive.db.jdbc.DatabaseType;
 import com.carbonfive.db.jdbc.DatabaseUtils;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.lang.time.DurationFormatUtils;
 import org.apache.commons.lang.time.StopWatch;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.ConnectionCallback;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.*;
 
 public class DataSourceMigrationManager implements MigrationManager
 {
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     private final JdbcTemplate jdbcTemplate;
     private DatabaseType dbType;
     private VersionStrategy versionStrategy = new SimpleVersionStrategy();
     private MigrationResolver migrationResolver = new ResourceMigrationResolver();
 
     public DataSourceMigrationManager(DataSource dataSource)
     {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
         this.dbType = determineDatabaseType();
     }
 
     public DataSourceMigrationManager(DataSource dataSource, DatabaseType dbType)
     {
        this(dataSource);
         this.dbType = dbType;
     }
 
     protected void enableMigrations()
     {
         try
         {
             jdbcTemplate.execute(new ConnectionCallback()
             {
                 public Object doInConnection(Connection connection) throws SQLException, DataAccessException
                 {
                     versionStrategy.enableVersioning(dbType, connection);
                     return null;
                 }
             });
 
             logger.info("Successfully enabled migrations.");
         }
         catch (DataAccessException e)
         {
             logger.error("Could not enable migrations.", e);
             throw new MigrationException(e);
         }
     }
 
     public boolean validate()
     {
         return pendingMigrations().isEmpty();
     }
 
     public SortedSet<Migration> pendingMigrations()
     {
         Set<String> appliedMigrations = determineAppliedMigrationVersions();
         Set<Migration> availableMigrations = migrationResolver.resolve(dbType);
 
         SortedSet<Migration> pendingMigrations = new TreeSet<Migration>();
         CollectionUtils.select(availableMigrations, new PendingMigrationPredicate(appliedMigrations), pendingMigrations);
 
         return pendingMigrations;
     }
 
     public void migrate()
     {
         Set<String> appliedMigrations = determineAppliedMigrationVersions();
 
         if (appliedMigrations == null)
         {
             enableMigrations();
             appliedMigrations = Collections.EMPTY_SET;
         }
 
         Set<Migration> availableMigrations = migrationResolver.resolve(dbType);
 
         // Which migrations need to be applied (ie: are pending)?
         // TODO This seems like something that could be in its own method.
         final List<Migration> pendingMigrations = new ArrayList<Migration>(availableMigrations.size());
         CollectionUtils.select(availableMigrations, new PendingMigrationPredicate(appliedMigrations), pendingMigrations);
         Collections.sort(pendingMigrations);
 
         if (pendingMigrations.isEmpty())
         {
             logger.info("Database is up to date; no migration necessary.");
             return;
         }
 
         // TODO Check that no two pending migrations are the same version.
         StopWatch watch = new StopWatch();
         watch.start();
 
         logger.info("Migrating database... applying " + pendingMigrations.size() + " migration" + (pendingMigrations.size() > 1 ? "s" : "") + ".");
 
         try
         {
             jdbcTemplate.execute(new ConnectionCallback()
             {
                 public Object doInConnection(Connection connection) throws SQLException, DataAccessException
                 {
                     int successfulCount = 0;
                     Migration currentMigration = null;
 
                     final boolean autoCommit = connection.getAutoCommit();
                     connection.setAutoCommit(false);
 
                     try
                     {
                         for (Migration migration : pendingMigrations)
                         {
                             currentMigration = migration;
                             logger.info("Running migration " + currentMigration.getFilename() + ".");
 
                             final Date startTime = new Date();
                             StopWatch migrationWatch = new StopWatch();
                             migrationWatch.start();
 
                             currentMigration.migrate(dbType, connection);
                             versionStrategy.recordMigration(dbType, connection, currentMigration.getVersion(), startTime, migrationWatch.getTime());
 
                             connection.commit();
 
                             ++successfulCount;
                         }
                     }
                     catch (Throwable e)
                     {
                         assert currentMigration != null;
                         String message = "Migration for version " + currentMigration.getVersion() + " failed, rolling back and terminating migration.";
                         logger.error(message, e);
                         connection.rollback();
                         throw new MigrationException(message, e);
                     }
                     finally
                     {
                         connection.setAutoCommit(autoCommit);
                     }
 
                     return successfulCount;
                 }
             });
         }
         catch (DataAccessException e)
         {
             logger.error("Failed to migrate database.", e);
             throw new MigrationException(e);
         }
 
         watch.stop();
 
         logger.info("Migrated database in " + DurationFormatUtils.formatDurationHMS(watch.getTime()) + ".");
     }
 
     public void setDatabaseType(DatabaseType dbType)
     {
         this.dbType = dbType;
     }
 
     public void setMigrationResolver(MigrationResolver migrationResolver)
     {
         this.migrationResolver = migrationResolver;
     }
 
     public void setVersionStrategy(VersionStrategy versionStrategy)
     {
         this.versionStrategy = versionStrategy;
     }
 
     private DatabaseType determineDatabaseType()
     {
         return (DatabaseType) jdbcTemplate.execute(new ConnectionCallback()
         {
             public Object doInConnection(Connection connection) throws SQLException, DataAccessException
             {
                 return DatabaseUtils.databaseType(connection.getMetaData().getURL());
             }
         });
     }
 
     private Set<String> determineAppliedMigrationVersions()
     {
         return (Set<String>) jdbcTemplate.execute(new ConnectionCallback()
         {
             public Object doInConnection(Connection connection) throws SQLException, DataAccessException
             {
                 return versionStrategy.appliedMigrations(dbType, connection);
             }
         });
     }
 
     private static class PendingMigrationPredicate implements Predicate
     {
         private final Set<String> appliedMigrations;
 
         public PendingMigrationPredicate(Set<String> appliedMigrations)
         {
             this.appliedMigrations = appliedMigrations == null ? Collections.EMPTY_SET : appliedMigrations;
         }
 
         public boolean evaluate(Object input)
         {
             if (input instanceof Migration)
             {
                 return !appliedMigrations.contains(((Migration) input).getVersion());
             }
             else
             {
                 return !appliedMigrations.contains(input.toString());
             }
         }
 
     }
 }
