 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lightningrod.io;
 
 import java.io.File;
 
 
 /**
  *
  * @author kevin
  */
 public class Backup {
     /**
          * Backup the root Dropbox folder.
          * @param root_path The path to the root of the Dropbox folder.
          * @param newdir The new name of the folder.
          * @return True if successfully backed up, false otherwise.
          */
         public static boolean backupFolder(String root_path, String newdir) {
             File f = new File(root_path);
            boolean ret = f.renameTo(new File(newdir));
             if (ret) {
                System.out.println("Backup of Dropbox to " + newdir + " succeeded.");
             } else {
                System.out.println("Backup of Dropbox to " + newdir + " failed.");
             }
             return ret;
         }
 }
