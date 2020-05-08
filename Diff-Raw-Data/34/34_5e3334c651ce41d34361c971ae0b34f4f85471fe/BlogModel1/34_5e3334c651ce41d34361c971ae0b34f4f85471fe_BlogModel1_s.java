 package com.wowodc12.blogdemo.migrations;
 
 import com.webobjects.eocontrol.EOEditingContext;
 import com.webobjects.foundation.NSArray;
 
 import er.extensions.migration.ERXMigrationDatabase;
 import er.extensions.migration.ERXMigrationTable;
 import er.extensions.migration.ERXModelVersion;
 
 public class BlogModel1 extends ERXMigrationDatabase.Migration {
   @Override
   public NSArray<ERXModelVersion> modelDependencies() {
     return null;
   }
   
   @Override
   public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
     // DO NOTHING
   }
 
   @Override
   public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
     ERXMigrationTable delegatePKHistoryTable = database.newTableNamed("DelegatePKHistory");
     delegatePKHistoryTable.newStringColumn("delegatedPrimaryKeyValue", 255, false);
     delegatePKHistoryTable.newIntegerColumn("id", false);
     delegatePKHistoryTable.newIntegerColumn("syncInfoID", false);
     delegatePKHistoryTable.create();
     delegatePKHistoryTable.setPrimaryKey("id");
 
     ERXMigrationTable syncInfoTable = database.newTableNamed("SyncInfo");
    syncInfoTable.newStringColumn("delegatedPrimaryKeyValue", 255, false);
     syncInfoTable.newStringColumn("etag", 255, false);
     syncInfoTable.newIntegerColumn("id", false);
     syncInfoTable.newTimestampColumn("lastModified", false);
     syncInfoTable.newStringColumn("state", 50, false);
     syncInfoTable.create();
     syncInfoTable.setPrimaryKey("id");
 
     delegatePKHistoryTable.addForeignKey("syncInfoID", "SyncInfo", "id");
   }
 }
