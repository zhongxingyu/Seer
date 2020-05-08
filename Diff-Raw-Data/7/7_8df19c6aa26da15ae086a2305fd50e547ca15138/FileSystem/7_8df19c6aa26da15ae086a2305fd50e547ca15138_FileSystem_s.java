 package net.kokkeli.server;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.file.FileAlreadyExistsException;
 
 /**
  * File operations
  * @author Hekku2
  *
  */
 public class FileSystem implements IFileSystem {
 
     @Override
     public void writeToFile(InputStream uploadedInputStream,
             String uploadedFileLocation) throws IOException {
         int read = 0;
         byte[] bytes = new byte[1024];
  
         File file = new File(uploadedFileLocation);
         if (file.exists()){
             throw new FileAlreadyExistsException("File already exists.");
         }
         
         OutputStream out = new FileOutputStream(file);
         while ((read = uploadedInputStream.read(bytes)) != -1) {
             out.write(bytes, 0, read);
         }
         out.flush();
         out.close();
     }
 
     @Override
     public boolean fileExists(String file) {
        return new File(file).exists();
     }
 
 }
