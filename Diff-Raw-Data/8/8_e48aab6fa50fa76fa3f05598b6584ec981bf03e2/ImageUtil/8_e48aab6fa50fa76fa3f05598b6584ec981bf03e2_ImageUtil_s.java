package com.monkey.fileupload;
 
 import java.io.File;
 
 public class ImageUtil {
 
     /**
      * 
      */
     public static void createFolderStructure(String path) {
 
 	File file = new File(path);
 
 	if (!file.isDirectory()) {
 	    file.mkdirs();
 	}
     }
 }
