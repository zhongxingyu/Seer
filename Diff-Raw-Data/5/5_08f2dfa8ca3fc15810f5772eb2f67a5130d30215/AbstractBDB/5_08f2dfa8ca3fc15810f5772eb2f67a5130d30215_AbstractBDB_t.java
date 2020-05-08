 /*
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3.0 of the License, or (at your option) any later version.
  *
  *      http://www.gnu.org/licenses/lgpl-3.0.txt
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA
  */
 package fr.nikokode.commons.bdb;
 
 import java.io.File;
 import java.io.FileFilter;
 
 import org.apache.log4j.Logger;
 
 import com.sleepycat.je.DatabaseException;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.EnvironmentConfig;
 
 /**
  * Abstract Berkeley DB storage.
  */
 public abstract class AbstractBDB {
 
     /**
      * A file filter used to detect if the storage folder contains an existing BDB.
      */
     private class BDBFileFilter implements FileFilter {
 
         /**
          * Accept method.
          * @param f the file to test for acceptance.
          * @return true if the file is accepted, false otherwise.
          */
         public boolean accept(File f) {
             String name = f.getName();
             return name.endsWith(".jdb") || name.endsWith(".lck");
         }
 
     }
 
     /**
      * The Berkeley DB JE environment.
      */
     private Environment dbEnvironment;
 
     /**
      * @return the path to the storage folder.
      */
    public abstract String getStorageFolderPath();
 
     /**
      * @return the percentage of JVM memory to use for the cache.
      */
    public abstract int getCachePercentage();
 
     /**
      * The class logger
      */
     private final static Logger LOGGER = Logger.getLogger(AbstractBDB.class);
 
     /**
      * Closes the BDB.
      * @throws DatabaseException when the operation failed.
      */
     protected void stopEnvironment() throws DatabaseException {
         closeStores();
         if (dbEnvironment != null) {
             dbEnvironment.cleanLog();
             dbEnvironment.close();
             dbEnvironment = null;
         }
         LOGGER.info("Closed BDB located in '" + getStorageFolderPath() + "'");
     }
 
     /**
      * Dispose of any allocated resource before the environment is closed.
      */
     protected abstract void closeStores();
 
     /**
      * Initialize entity stores.
      * @param dbEnv the db environment.
      */
     protected abstract void initStores(Environment dbEnv);
 
     /**
      * Initializes the DB environment, by creating the DB if the storage
      * folder does not exist, and attempt to load the DB otherwise.
      * @throws DatabaseException when the operation failed
      */
     protected void startEnvironment() throws DatabaseException {
 
         if (dbEnvironment != null) {
             return; // already initialized
         }
 
         File storageFolder = new File(getStorageFolderPath());
         boolean createEnv = checkStorageFolder(storageFolder);
 
         EnvironmentConfig envConfig = new EnvironmentConfig();
         envConfig.setAllowCreate(createEnv);
         envConfig.setCachePercent(getCachePercentage());
         dbEnvironment = new Environment(storageFolder, envConfig);
 
         initStores(dbEnvironment);
 
         LOGGER.info("Initialised BDB located in '" + getStorageFolderPath()
                 + "'" + (createEnv ? " (created new environment)" : ""));
     }
 
     /**
      * Resets the BDB, creating a new empty BDB.
      * @throws DatabaseException if the operation failed.
      */
     public void reset() throws DatabaseException {
         stopEnvironment();
 
         if (!removeStorageDir()) {
             throw new RuntimeException("Failed to delete storage folder ("
                     + getStorageFolderPath() + ")");
         }
         startEnvironment();
     }
 
     /**
      * Creates the folder if needed, and checks for access permissions.
      * @return true if the database has to be created, false if it already exists.
      * @param folder the folder to check.
      */
     private boolean checkStorageFolder(File folder) {
         if (!folder.exists()) {
             if (!folder.mkdirs()) {
                 throw new RuntimeException("Storage folder creation failed ("
                         + folder.getAbsolutePath() + ")");
             }
             return true;
         } else if (!folder.isDirectory()) {
             throw new RuntimeException("Storage folder is no a directory ("
                     + folder.getAbsolutePath() + ")");
         } else if (!folder.canRead() || !folder.canWrite()) {
             throw new RuntimeException("Storage folder has wrong access rights ("
                     + folder.getAbsolutePath() + ")");
         } else {
             return folder.listFiles(new BDBFileFilter()).length == 0;
         }
 
     }
 
     /**
      * Recursively removes the storage folder and its contents.
      * @return true if deletion succeeded, false otherwise.
      */
     protected boolean removeStorageDir() {
         File bdbDir = new File(getStorageFolderPath());
         if (!bdbDir.exists()) {
             return false;
         }
         for (File f : bdbDir.listFiles()) {
             if (!f.delete()) {
                 f.deleteOnExit();
             }
         }
         return bdbDir.delete();
     }
 
 }
