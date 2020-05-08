 /*
  * Copyright 2012, United States Geological Survey or
  * third-party contributors as indicated by the @author tags.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/  >.
  *
  */
 package asl.seedscan.config;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import asl.logging.LogFileConfig;
 import asl.seedscan.scan.Scan;
 
 public class Configuration
 {
     private static final Logger logger = Logger.getLogger("asl.seedscan.config.Configuration");
 
     private File lockFile = null;
     private LogFileConfig logConfig = null;
     private DatabaseConfig dbConfig = null; 
     private ArrayList<Scan> scans = null;
 
  // constructor(s)
     public Configuration()
     {
         scans = new ArrayList<Scan>();
     }
 
  // lock file
     public void setLockFile(String fileName)
     throws FileNotFoundException,
            IOException,
            NullPointerException,
            SecurityException
     {
         setLockFile(new File(fileName));
     }
     
     public void setLockFile(File file)
     throws FileNotFoundException,
            IOException,
            NullPointerException,
            SecurityException
     {
         if (file == null) {
             throw new NullPointerException();
         }
        if (!file.isFile()) {
            throw new IOException("Path '" +file+ "'exists, but it is not a file");
         }
        if (!file.canWrite()) {
             throw new SecurityException("Not permitted to modify file '" +file+ "'");
         }
         logger.config("LockFile: "+file);
         lockFile = file;
     }
 
     public File getLockFile()
     {
         return lockFile;
     }
 
  // log configuration
     public void setLogConfig(LogFileConfig config)
     {
         logger.config("LogFileConfig: "+config);
         logConfig = config;
     }
 
     public LogFileConfig getLogConfig()
     {
         return logConfig;
     }
 
  // database configuration
     public void setDatabaseConfig(DatabaseConfig config)
     {
         logger.config("DatabaseConfig: "+config);
         dbConfig = config;
     }
 
     public DatabaseConfig getDatabaseConfig()
     {
         return dbConfig;
     }
 
  // scans
     public void addScan(Scan scan)
     {
         logger.config("Scan: "+scan);
         scans.add(scan);
     }
 
     public int getScanCount()
     {
         return scans.size();
     }
 
     public Scan getScan(int id)
     throws IndexOutOfBoundsException
     {
         return scans.get(id);
     }
 
     public boolean removeScan(Scan scan)
     {
         return scans.remove(scan);
     }
     
     public Scan removeScan(int id)
     throws IndexOutOfBoundsException
     {
         return scans.remove(id);
     }
 }
 
