 package org.cloudname.copkg.util;
 
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Enumeration;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Simple utility for unpacking ZIP files.  Will extract a given ZIP
  * file into a destination directory with a minimum of fuss.
  *
  * @author borud
  */
 public class Unzip {
     private static final Logger log = Logger.getLogger(Unzip.class.getName());
 
     public static final int BUFFER_SIZE = (16 * 1024);
 
     /**
      * Unpack a ZIP file into the target directory.  If the target
      * directory does not exist we will create it.
      *
      * @param sourceFile the ZIP-file we wish to extract
      * @param targetDirectory the target directory into which we will extract the ZIP file
      */
    public static void unzip(File sourceFile, File targetDirectory) throws IOException {
         if (! sourceFile.exists()) {
             throw new FileNotFoundException("Source file not found: " + sourceFile.getAbsolutePath());
         }
 
         // if the target directory does not exist we create it
         if (! (targetDirectory.exists())) {
             boolean ok = targetDirectory.mkdirs();
             if (! ok) {
                 throw new IOException("Unable to create directory: " + targetDirectory.getAbsolutePath());
             }
         }
 
         // Invariants: sourceFile exists and target directory exists
 
         ZipFile zipFile = new ZipFile(sourceFile);
        Enumeration<? extends ZipArchiveEntry> entries = zipFile.getEntries();
 
         while (entries.hasMoreElements()) {
            final ZipArchiveEntry zipEntry = entries.nextElement();
 
             // Prepare target name and make sure that we create any
             // directories that are needed.
             File destinationFile = new File(targetDirectory, zipEntry.getName());
             File parent = destinationFile.getParentFile();
             if (! parent.exists()) {
                 boolean ok = parent.mkdirs();
                 if (! ok) {
                     throw new IOException("Unable to create directory: " + parent.getAbsolutePath());
                 }
             }
 
             // If the entry is a directory we need not process any further.
             if (zipEntry.isDirectory()) {
                 destinationFile.mkdir();
                 log.fine(" - Created dir " + destinationFile.getAbsolutePath());
                 continue;
             }
 
            InputStream in = zipFile.getInputStream(zipEntry);

             // Copy the data
             OutputStream outs = new FileOutputStream(destinationFile);
             byte buffer[] = new byte[BUFFER_SIZE];
             int numBytes = 0;
             int totalBytes = 0;
             while((numBytes = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                 outs.write(buffer, 0, numBytes);
                 totalBytes += numBytes;
             }
 
             // Log a warning if the file was different size than expected.
             if (zipEntry.getSize() != totalBytes) {
                 log.warning("Expected " + zipEntry.getSize()
                             + " bytes, got " + numBytes
                             + " for " + destinationFile.getAbsolutePath());
             }
 
            // Make sure executable files are executable
            if ((zipEntry.getUnixMode() & 0100) != 0) {
                destinationFile.setExecutable(true);
                log.fine(" - Making executable " + destinationFile.getAbsolutePath());
            }

             log.fine(" - Extracted " + destinationFile.getAbsolutePath() + " [" + totalBytes + "]");
 
             outs.flush();
             outs.close();
             in.close();
            destinationFile.setLastModified(zipEntry.getTime());
         }
     }
 }
