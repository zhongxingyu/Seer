 /*
  * #%L
  * Bitrepository Service
  * %%
  * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.service.audit;
 
 import static org.bitrepository.service.audit.AuditDatabaseConstants.DATABASE_VERSION_ENTRY;
 import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_TABLE;
 
 import java.util.Map;
 
 import org.bitrepository.service.database.DBConnector;
 import org.bitrepository.service.database.DatabaseMigrator;
 import org.bitrepository.service.database.DatabaseUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Migration class for the AuditTrailContributorDatabase of the ReferencePillar, ChecksumPillar 
  * and the IntegrityService.
  * Will only try to perform the migration on an embedded derby database.
  */
 public class AuditTrailContributorDatabaseMigrator extends DatabaseMigrator {
     /** The log.*/
     private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
     
     /** The name of the script for updating the database from version 1 to 2.*/
     private static final String UPDATE_SCRIPT_VERSION_1_TO_2 = "sql/derby/auditContributorDBUpdate1to2.sql";
     /** The name of the script for updating the database from version 2 to 3.*/
     private static final String UPDATE_SCRIPT_VERSION_2_TO_3 = "sql/derby/auditContributorDBUpdate2to3.sql";
     /** The current version of the database. */
     private final Integer currentVersion = 3;
     
     /**
      * Constructor.
      * @param connector connection to the database.
      */
     public AuditTrailContributorDatabaseMigrator(DBConnector connector) {
         super(connector);
     }
     
     @Override
     public void migrate() {
         Map<String, Integer> versions = getTableVersions();
         
         if(!versions.containsKey(FILE_TABLE)) {
             throw new IllegalStateException("The database does not contain '" + FILE_TABLE 
                     + "' table as required.");
         }
         if(versions.get(FILE_TABLE) == 1) {
             log.warn("Migrating AuditContributorDB from version 1 to 2.");
             migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_1_TO_2);
         }
         
         if(!versions.containsKey(DATABASE_VERSION_ENTRY) || versions.get(DATABASE_VERSION_ENTRY) < 3) {
             log.warn("Migrating AuditContributorDB from version 2 to 3.");
             migrateDerbyDatabase(UPDATE_SCRIPT_VERSION_2_TO_3);
         }
     }
 
     @Override
     public boolean needsMigration() {
         Map<String, Integer> versions = getTableVersions();
         
         if(!versions.containsKey(DATABASE_VERSION_ENTRY)) {
            throw new IllegalStateException("The database does not contain '" + DATABASE_VERSION_ENTRY 
                    + "' table as required.");
        }
        if(versions.get(DATABASE_VERSION_ENTRY) < currentVersion) {
             return true;
         } else {
             return false;
         }
     }
 }
