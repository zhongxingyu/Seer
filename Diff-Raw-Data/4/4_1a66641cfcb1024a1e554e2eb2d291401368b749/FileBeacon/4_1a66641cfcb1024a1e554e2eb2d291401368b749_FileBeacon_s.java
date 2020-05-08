 /*
  * Copyright (c) 2009-2012 Daniel Oom, see license.txt for more info.
  */
 
 package loader;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 public class FileBeacon implements Closeable {
   private static final String DATA_DIR = "data/";
   private static final String DATA_ARCHIVE = "data.zip";
 
   private final File dataDir, dataArchive;
   private final ZipFile zipFile;
 
   public FileBeacon(File dir) throws ZipException, IOException {
     assert dir != null;
 
     if (!dir.exists()) {
       throw new FileNotFoundException(dir.getAbsolutePath());
     }
 
     dataDir = new File(dir, DATA_DIR);
     dataArchive = new File(dir, DATA_ARCHIVE);
 
     if (dataArchive.exists()) {
       zipFile = new ZipFile(dataArchive);
     } else {
       zipFile = null;
     }
 
    if (!dataDir.exists()) {
      throw new FileNotFoundException(dataDir.getAbsolutePath());
     }
   }
 
   public InputStream getReader(String id) throws IOException {
     assert id != null;
 
     File f = new File(dataDir, id);
     if (f.exists()) {
       // Read file from directory
       return new FileInputStream(f);
     }
 
     if (zipFile != null) {
       ZipEntry entry = zipFile.getEntry(id);
       if (entry != null) {
         return zipFile.getInputStream(entry);
       }
     }
 
     throw new FileNotFoundException(id);
   }
 
   @Override
   public void close() throws IOException {
     if (zipFile != null) {
       zipFile.close();
     }
   }
 }
