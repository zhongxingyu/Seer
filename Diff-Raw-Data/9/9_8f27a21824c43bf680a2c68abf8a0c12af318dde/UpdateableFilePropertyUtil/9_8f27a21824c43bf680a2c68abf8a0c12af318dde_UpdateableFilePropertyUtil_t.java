 package org.javaz.util;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Properties;
 
 /**
  * Loads properties from file, and watching for changes;
  * When changes detected, content are reloaded.
  */
 public class UpdateableFilePropertyUtil
 {
     protected static HashMap instances = new HashMap();
 
     public static String DEFAULT_FILE_WATCH_UPDATE_PERIOD = "15000";
 
     public static long FILE_WATCH_UPDATE_PERIOD =
             Long.valueOf(System.getProperty("org.javaz.util.FILE_WATCH_UPDATE_PERIOD", DEFAULT_FILE_WATCH_UPDATE_PERIOD)).longValue();
 
     public static UpdateableFilePropertyUtil getInstance(String file)
     {
         if (!instances.containsKey(file.hashCode()))
         {
             synchronized (UpdateableFilePropertyUtil.class)
             {
                 if (!instances.containsKey(file.hashCode()))
                 {
                     UpdateableFilePropertyUtil util = new UpdateableFilePropertyUtil(file);
                     util.updateFileIfNeeded();
                     instances.put(file.hashCode(), util);
                 }
             }
         }
         return (UpdateableFilePropertyUtil) instances.get(file.hashCode());
     }
 
     protected String file = null;
     protected Properties properties = new Properties();
     protected long fileStampUpdate = 0l;
     protected long fileStampModify = 0l;
 
     protected UpdateableFilePropertyUtil(String file)
     {
         this.file = file;
     }
 
     public Object getProperty(String key)
     {
         updateFileIfNeeded();
         return properties.get(key);
     }
 
     public Properties getPropertiesCopy()
     {
         updateFileIfNeeded();
         return new Properties(properties);
     }
 
     protected boolean updateFileIfNeeded()
     {
         boolean changed = false;
         long currentMillis = System.currentTimeMillis();
         File f = null;
         int fileHash = file.hashCode();
         if (fileStampUpdate == 0 || fileStampUpdate + FILE_WATCH_UPDATE_PERIOD < currentMillis)
         {
             f = new File(file);
             if (f.exists())
             {
                 long lastModified = f.lastModified();
                 if (fileStampModify == 0 || fileStampModify < lastModified)
                 {
                     changed = true;
                     Properties p = new Properties();
                     try
                     {
                         FileReader fr = new FileReader(f);
                         p.load(fr);
                         try
                         {
                             fr.close();
                         }
                         catch (Exception e)
                         {
                         }
 
                         synchronized (this)
                         {
                             properties.clear();
                             properties.putAll(p);
                         }
                         fileStampModify = lastModified;
                        fileStampUpdate = currentMillis;
                     }
                     catch (IOException e)
                     {
                         e.printStackTrace();
                     }
                 }
                 else
                 {
                     fileStampUpdate = currentMillis;
                 }
             }
         }
         return changed;
     }
 
     public long getFileStampModify()
     {
         return fileStampModify;
     }
 }
