 
 package net.slreynolds.ds.util;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Some utility methods on files
  */
 public class FileUtil {
     
     public static File createEmptyWritableFile(String path) throws IOException {
         File out = new File(path);
         if (out.exists()) {
             if (!out.isFile()) {
                 throw new IOException(path + " is not a file");
             }
             if (!out.delete()) {
                 throw new IOException("Unable to delete existing file: "+path);
             }
         }
         // invariant: file does not exist 
        File parent = new File(out.getParent());
        if (!parent.exists()) {
             parent.mkdirs();
         }
         
         out.createNewFile();
         
         if (!out.canWrite()) {
             throw new IOException("Cannot write to file " + path);
         }
         return out;
     }
 }
