 package cyberwaste.kuzoff.web.jsf;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import cyberwaste.kuzoff.core.DatabaseManager;
 import cyberwaste.kuzoff.core.domain.Row;
 import cyberwaste.kuzoff.core.domain.Table;
 import cyberwaste.kuzoff.core.impl.DatabaseManagerImpl;
 
 public class AllTables {
 
     private DatabaseManager databaseManager;
     
     private String database;
     
     public String getDatabase() {
         return database;
     }
     
     public void setDatabase(String database) {
         this.database = database;
     }
     
     public List<String> getDatabases() {
         List<String> databases = new ArrayList<String>();
         for (File databaseFolder : DatabaseManagerImpl.KUZOFF_HOME.listFiles()) {
             databases.add(databaseFolder.getName());
         }
         return databases;
     }
     
     public Collection<Table> getTables() throws IOException {
         try {
             databaseManager.forDatabaseFolder(database);
             return databaseManager.listTables();
         } catch (Exception e) {
             return Collections.emptyList();
         }
     }
     
     public List<Row> data(Table table) throws IOException {
         return databaseManager.loadTableData(table.getName());
     }
     
    public List<String> schema(Table table) {
        return table.getColumnTypeNames();
    }
    
     public void setDatabaseManager(DatabaseManager databaseManager) {
         this.databaseManager = databaseManager;
     }
 }
