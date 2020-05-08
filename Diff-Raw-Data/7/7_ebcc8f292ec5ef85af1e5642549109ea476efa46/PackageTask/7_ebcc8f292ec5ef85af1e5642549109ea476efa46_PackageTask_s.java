 package org.exoplatform.management.packaging.task;
 
 import org.gatein.management.api.operation.ResultHandler;
 import org.gatein.management.api.operation.model.ExportResourceModel;
 import org.gatein.management.api.operation.model.ExportTask;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.File;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gregorysebert
  * Date: 11/07/12
  * Time: 16:11
  * To change this template use File | Settings | File Templates.
  */
 public class PackageTask implements ExportTask {
     private File file = null;
 
     public PackageTask(File tmpFolder)
     {
        this.file = tmpFolder;
     }
 
     @Override
     public String getEntry()
     {
       return this.file.getPath();
     }
 
     protected String getXmlFileName()
     {
         return this.file.getName();
     }
 
 
     @Override
     public void export(OutputStream outputStream) throws IOException
 
     {
        
 
         ZipOutputStream zos = new ZipOutputStream(outputStream);
    	 
         browse(file, zos);
 
         zos.close();
     }
     private void browse(File currentFile,ZipOutputStream zos) throws IOException {
 		if (currentFile.isDirectory()) {
 			File[] children = currentFile.listFiles();
             zos.putNextEntry(new ZipEntry(currentFile.getName()));
             zos.closeEntry();
 			for (File child : children) {
                 browse(child, zos);
 			}
 
 		} else {
 			//file is a file
 			byte[] buffer = new byte[2048];
 			FileInputStream in = new FileInputStream(currentFile);
 
             // Add ZIP entry to output stream.
             zos.putNextEntry(new ZipEntry(currentFile.getPath()));
     
             // Transfer bytes from the file to the ZIP file
             int len;
             while ((len = in.read(buffer)) > 0) {
                 zos.write(buffer, 0, len);
             }
     
             // Complete the entry
             zos.closeEntry();
             in.close();
 			
 			//String filePathInExtension = currentFile.getPath().replace(tmpFolderPath, ""); 
 
 		}
 	}
 }
