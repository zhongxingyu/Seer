 /*
  * Created on Aug 19, 2004
  */
 package org.cishell.reference.gui.persistence.save;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 
 import org.cishell.framework.CIShellContext;
 import org.cishell.framework.algorithm.AlgorithmProperty;
 import org.cishell.framework.data.Data;
 import org.cishell.framework.data.DataProperty;
 import org.cishell.service.conversion.Converter;
 import org.cishell.service.guibuilder.GUIBuilderService;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.osgi.service.log.LogService;
 
 /**
  * Persist the file to disk for the user
  * 
  * @author Team 
  */
 public class FileSaver {
     private static File currentDir;
 
     private Shell parent;
     
     private GUIBuilderService guiBuilder;
     private LogService log;
 
 
     /**
      * Initializes services to output messages
      * 
      * @param parent
      * @param context
      */
     public FileSaver(Shell parent, CIShellContext context){
         this.parent = parent;
         this.guiBuilder = (GUIBuilderService)context.getService(GUIBuilderService.class.getName());
         this.log = (LogService) context.getService(LogService.class.getName());
     }       
 
     /**
      * File exists, so make sure the user wants to overwrite
      * @param file The file to possibly overwrite
      * @return Whether or not the user decides to overwrite
      */
     private boolean confirmFileOverwrite(File file) {
         String message = "The file:\n" + file.getPath()
             + "\nalready exists. Are you sure you want to overwrite it?";
         return guiBuilder.showConfirm("File Overwrite", message, "");
     }
 
     /**
      * Checks for a valid file destination
      * 
      * @param file to save to
      * @return True on valid file save
      */
     private boolean isSaveFileValid(File file) {
         boolean valid = false;
         if (file.isDirectory()) {
             String message = "Destination cannot be a directory. Please choose a file";
             guiBuilder.showError("Invalid Destination", message, "");
             valid = false;
         } else if (file.exists()) {
             valid = confirmFileOverwrite(file);
         }
         else
             valid = true ;
         return valid;
     }
 
     /**
      * Given a converter, save the data
      * 
      * @param converter Saves the data to a file
      * @param data Data object to save
      * @return Whether or not the save was successful
      */
     public boolean save(Converter converter, Data data) {
     	String outDataStr = (String)converter.getProperties().get(AlgorithmProperty.OUT_DATA);
 
     	String ext = outDataStr.substring(outDataStr.indexOf(':')+1);
         
         if ((""+ext).startsWith(".")) {
             ext = ext.substring(1);
         }
         
         FileDialog dialog = new FileDialog(parent, SWT.SAVE);
         
         if (currentDir == null) {
             currentDir = new File(System.getProperty("user.home") + File.separator + "anything");
         }
         dialog.setFilterPath(currentDir.getPath());
         
      
         if (ext != null && !ext.equals("*")) {
             dialog.setFilterExtensions(new String[]{"*." + ext});
         }
         
         dialog.setText("Choose File");
         
         String fileLabel = (String)data.getMetaData().get(DataProperty.LABEL);
        String suggestedFileName = getFileName(fileLabel);
         dialog.setFileName(suggestedFileName + "." + ext);
         
 //        if (fileLabel == null) {
 //        	dialog.setFileName("*." + ext);
 //        } else {
 //        	dialog.setFileName(fileLabel + '.' + ext);        	
 //        }
 
         boolean done = false;
         
         while (!done) {        
             String fileName = dialog.open();
             if (fileName != null) {
                 File selectedFile = new File(fileName);
                 if (!isSaveFileValid(selectedFile))
                     continue;
                 if (ext != null && ext.length() != 0)
                     if (!selectedFile.getPath().endsWith(ext) && !ext.equals("*"))
                         selectedFile = new File(selectedFile.getPath()+'.'+ ext);
 
                 Data newData = converter.convert(data);
                 
                 copy((File)newData.getData(), selectedFile);
                 
                 if (selectedFile.isDirectory()) {
                 	currentDir = new File(selectedFile + File.separator + "anything");
                 } else {
                 	currentDir = new File(selectedFile.getParent() + File.separator + "anything");
                 }
                     
                 done = true;
        
                 log.log(LogService.LOG_INFO, "Saved: " + selectedFile.getPath());
             } else {
                 done = true;
                 return false;
             }            
         }
         return true;
     }
     
     /**
      * Converter puts it into a temporary directory, this copies it over
      * 
      * @param in The temp file to copy
      * @param out Destination to copy to
      * @return True on successful copy, false otherwise
      */
     private boolean copy(File in, File out) {
     	try {
     		FileInputStream  fis = new FileInputStream(in);
     		FileOutputStream fos = new FileOutputStream(out);
     		
     		FileChannel readableChannel = fis.getChannel();
     		FileChannel writableChannel = fos.getChannel();
     		
     		writableChannel.truncate(0);
     		writableChannel.transferFrom(readableChannel, 0, readableChannel.size());
     		fis.close();
     		fos.close();
     		return true;
     	}
     	catch (IOException ioe) {
     		guiBuilder.showError("Copy Error", "IOException during copy", ioe.getMessage());
             return false;
     	}
     }
     
     private String getFileName(String fileLabel) {
     	
     	//index variables will be -1 if index is not found.
     	int descriptionEndIndex = fileLabel.lastIndexOf(":");
     	int filePathEndIndex = fileLabel.lastIndexOf(File.separator);
 
     	//doesn't matter if either variable is -1, since startIndex will be 
     	//zero and none of the string will be cut off the front.
     	int startIndex = Math.max(descriptionEndIndex, filePathEndIndex) + 1;
     	
     	String fileNameWithExtension = fileLabel.substring(startIndex);
     	
     	
     	//find the first character of the file name extension.
    	int extensionBeginIndex = fileNameWithExtension.indexOf(".");
     	
     	int endIndex;
     	
     	if (extensionBeginIndex != -1) {
     		//we found a period in the file name.
     		endIndex = extensionBeginIndex; //cut off everything after 
     		//first period.
     	} else {
     		//we didn't find an extension on the file name.
    		endIndex = fileLabel.length(); // don't cut any off the end.
     	}
     	
     	String fileNameWithoutExtension = fileNameWithExtension.substring(0, endIndex);
    	
     	String fileName = fileNameWithoutExtension;
     	return fileName;
     }
 }
