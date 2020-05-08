 package org.docear.syncdaemon.jnotify;
 
 import akka.actor.ActorRef;
 import net.contentobjects.jnotify.JNotifyListener;
 import org.apache.commons.io.IOUtils;
 import org.docear.syncdaemon.fileactors.Messages.FileChangedLocally;
 import org.docear.syncdaemon.fileindex.FileMetaData;
 import org.docear.syncdaemon.hashing.HashAlgorithm;
 import org.docear.syncdaemon.hashing.SHA2;
 import org.docear.syncdaemon.projects.Project;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.*;
 
 public class Listener implements JNotifyListener {
     private static final Logger logger = LoggerFactory.getLogger(Listener.class);
     private final ActorRef recipient;
     private final Project project;
     private final HashAlgorithm hashAlgorithm;
 
     public Listener(Project project, ActorRef recipient) {
         this.recipient = recipient;
         this.project = project;
         this.hashAlgorithm = new SHA2();
     }
 
     @Override
     public void fileCreated(final int wd, final String rootPath, final String name) {
         logger.debug("fileCreated {}/{}", rootPath, name);
        sendFileChangedMessage(createFileMetaData(rootPath, name, false));
     }
 
     @Override
     public void fileDeleted(final int wd, final String rootPath, final String name) {
         logger.debug("fileDeleted {}/{}", rootPath, name);
         sendFileChangedMessage(createFileMetaData(rootPath, name, true));
     }
 
     @Override
     public void fileModified(final int wd, final String rootPath, final String name) {
         logger.debug("fileModified {}/{}", rootPath, name);
         sendFileChangedMessage(createFileMetaData(rootPath, name, false));
     }
 
     @Override
     public void fileRenamed(final int wd, final String rootPath, final String oldName, final String newName) {
         logger.debug("fileRenamed rootpath={}, oldName={}, newName={}", rootPath, oldName, newName);
         sendFileChangedMessage(createFileMetaData(rootPath, oldName, true));
         sendFileChangedMessage(createFileMetaData(rootPath, newName, false));
     }
 
     private void sleep(long millis) {
         try {
             Thread.sleep(millis);
         } catch (InterruptedException e) {
         }
     }
 
     private FileMetaData createFileMetaData(final String path, final String name, final boolean isDeleted) {
         File f = new File(path, name);
 
 
 
         boolean isDirectory = f.isDirectory();
         String hash = "";
         if (!isDeleted && !isDirectory) {
 
             //wait until file is accessible
             OutputStream out = null;
             while(out == null) {
                 try {
                     out = new FileOutputStream(f,true); // -> throws a FileNotFoundException
                 } catch (FileNotFoundException e) {
                     out = null;
                     sleep(100);
                 }
             }
             IOUtils.closeQuietly(out);
 
             try {
                 hash = hashAlgorithm.generate(f);
             } catch (IOException e) {
                 logger.error("Couldn't create Hash for FileMetaData for file \"" + name + "\".", e);
             }
         }
         FileMetaData fileMetaData = new FileMetaData(
                 "/"+name,
                 hash,
                 project.getId(),
                 isDirectory,
                 isDeleted,
                 -1);
         logger.debug(fileMetaData.toString());
         return fileMetaData;
     }
 
     private void sendFileChangedMessage(final FileMetaData fileMetaData) {
         final FileChangedLocally message = new FileChangedLocally(this.project, fileMetaData);
 
         recipient.tell(message, recipient);
     }
 }
