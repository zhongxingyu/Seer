 package org.bitrepository.pillar.cache;
 
 import org.bitrepository.common.settings.Settings;
 import org.bitrepository.pillar.common.ChecksumDatabaseCreator;
 import org.bitrepository.service.database.DatabaseManager;
 import org.bitrepository.service.database.DatabaseMigrator;
 import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
 
 /**
  * Class to manage the connection to the Checksum database used in the reference and checksum pillars. 
  */
 public class ChecksumDatabaseManager extends DatabaseManager {
 
     private final Settings settings;
    private DatabaseMigrator migrator;
     
     public ChecksumDatabaseManager(Settings settings) {
         this.settings = settings;
     }
     
     @Override
     protected DatabaseSpecifics getDatabaseSpecifics() {
         return settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
     }
 
     @Override
     protected synchronized DatabaseMigrator getMigrator() {
         if(migrator == null) {
             migrator = new ChecksumDBMigrator(connector, settings);
         }
         return migrator;
     }
 
     @Override
     protected boolean needsMigration() {
         return getMigrator().needsMigration();
     }
 
     @Override
     protected String getDatabaseCreationScript() {
         return ChecksumDatabaseCreator.DEFAULT_CHECKSUM_DB_SCRIPT;
     }
 
 }
