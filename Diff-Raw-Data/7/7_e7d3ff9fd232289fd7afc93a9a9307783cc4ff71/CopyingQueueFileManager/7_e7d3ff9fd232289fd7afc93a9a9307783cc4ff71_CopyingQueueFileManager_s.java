 /*
 * Copyright 2012, CMM, University of Queensland.
 *
 * This file is part of AclsLib.
 *
 * AclsLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AclsLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AclsLib. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.queue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import au.edu.uq.cmm.paul.PaulConfiguration;
 
 public class CopyingQueueFileManager implements QueueFileManager {
     private static final Logger LOG = LoggerFactory.getLogger(CopyingQueueFileManager.class);
     private static final int RETRY = 10;
 
     private final File archiveDirectory;
     private final File captureDirectory;
     
     public CopyingQueueFileManager(PaulConfiguration config) {
         this.archiveDirectory = new File(config.getArchiveDirectory());
         this.captureDirectory = new File(config.getCaptureDirectory());
     }
     
     @Override
     public File enqueueFile(File source, String suffix, boolean regrabbing) 
                 throws QueueFileException, InterruptedException {
         // TODO - if the time taken to copy files is a problem, we could 
         // potentially improve this by using NIO or memory mapped files.
         File target = generateUniqueFile(suffix, regrabbing);
         long size = source.length();
         try (FileInputStream is = new FileInputStream(source);
                 FileOutputStream os = new FileOutputStream(target)) {
             byte[] buffer = new byte[(int) Math.min(size, 8192)];
             int nosRead;
             long totalRead = 0;
             while ((nosRead = is.read(buffer, 0, buffer.length)) > 0) {
                 os.write(buffer, 0, nosRead);
                 totalRead += nosRead;
             }
 
             // If these happen there is something wrong with our copying, locking
             // and / or file settling heuristics.
             if (totalRead != size) {
                 LOG.error("Copied file size discrepancy - initial file size was " + size +
                         "bytes but we copied " + totalRead + " bytes");
             } else if (size != source.length()) {
                 LOG.error("File size changed during copy - initial file size was " + size +
                         "bytes and current size is " +  source.length());
             }
             LOG.info("Copied " + totalRead + " bytes from " + source + " to " + target);
             return target;
         } catch (IOException ex) {
             throw new QueueFileException("Problem while copying file to queue", ex);
         }
     }
     
     @Override
     public void enqueueFile(String contents, File target)
             throws QueueFileException {
        if (!target.exists()) {
            throw new QueueFileException("File " + target + " no longer exists");
        }
        if (!isQueuedFile(target)) {
            throw new QueueFileException("File " + target + " is not in the queue");
         }
         try (Writer w = new FileWriter(target)) {
             w.write(contents);
             w.close();
         } catch (IOException ex) {
             throw new QueueFileException("Problem while saving to a queue file", ex);
         }
     }
 
     @Override
     public File renameGrabbedDatafile(File file) throws QueueFileException {
         String extension = FilenameUtils.getExtension(file.toString());
         if (!extension.isEmpty()) {
             extension = "." + extension;
         }
         for (int i = 0; i < RETRY; i++) {
             File newFile = generateUniqueFile(extension, false);
             if (!file.renameTo(newFile)) {
                 if (!newFile.exists()) {
                     throw new QueueFileException(
                             "Unable to rename " + file + " to " + newFile);
                 }
             } else {
                 return newFile;
             }
         }
         throw new QueueFileException(RETRY + " attempts to rename file failed!");
     }
 
     @Override
     public File archiveFile(File file) throws QueueFileException {
         if (!file.exists()) {
             throw new QueueFileException("File " + file + " no longer exists");
         }
         if (!isQueuedFile(file)) {
             throw new QueueFileException("File " + file + " is not in the queue");
         }
         File dest = new File(archiveDirectory, file.getName());
         if (dest.exists()) {
             throw new QueueFileException("Archived file " + dest + " already exists");
         } 
         if (file.renameTo(dest)) {
             LOG.info("File " + file + " archived as " + dest);
             return dest;
         } else {
             throw new QueueFileException("File " + file + " could not be archived - " +
                     "it remains in the queue area");
         }
     }
 
     @Override
     public void removeFile(File file) throws QueueFileException {
         if (!file.exists()) {
             throw new QueueFileException("File " + file + " no longer exists");
         }
         if (!isQueuedFile(file)) {
             throw new QueueFileException("File " + file + " is not in the queue");
         }
         if (file.delete()) {
             LOG.info("File " + file + " deleted from queue area");
         } else {
             throw new QueueFileException("File " + file + " could not be deleted from queue area");
         }
     }
 
     @Override
     public boolean isCopiedFile(File file) {
         return file.getParentFile().equals(captureDirectory) ||
                 file.getParentFile().equals(archiveDirectory);
     }
 
     @Override
     public boolean isQueuedFile(File file) {
         return file.getParentFile().equals(captureDirectory);
     }
 
     @Override
     public boolean isArchivedFile(File file) {
         return file.getParentFile().equals(archiveDirectory);
     }
     
     @Override
     public File generateUniqueFile(String suffix, boolean regrabbing) throws QueueFileException {
         String template = regrabbing ? "regrabbed-%d-%d-%d%s" : "file-%d-%d-%d%s";
         long threadId = Thread.currentThread().getId();
         for (int i = 0; i < RETRY; i++) {
             long now = System.currentTimeMillis();
             String name = String.format(template, now, threadId, i, suffix);
             File file = new File(captureDirectory, name);
             if (!file.exists()) {
                 return file;
             }
         }
         throw new QueueFileException(
                 RETRY + " attempts to generate a unique filename failed!");
     }
 }
