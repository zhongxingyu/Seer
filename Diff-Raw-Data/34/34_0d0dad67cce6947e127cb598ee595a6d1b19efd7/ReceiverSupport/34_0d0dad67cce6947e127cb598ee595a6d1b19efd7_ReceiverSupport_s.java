 package me.itzgeoff.vidsync.client;
 
 import java.io.File;
 import java.nio.file.Path;
 
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 @Component
 public class ReceiverSupport {
 
     @Value("${client.baseDirectory}")
     private File baseDirectory;
     
     @Value("${client.videoFileSuffix}")
     private String suffix;
 
     public Path resolveTitleToFilePath(String title) {
         return  baseDirectory.toPath().resolve(normalizeTitleToFilename(title)+"."+suffix);
     }
     
     private String normalizeTitleToFilename(String title) {
        return title.replaceAll(":", "_");
     }
 
 }
