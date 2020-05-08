 /**
  * This file is part of ImportPlain library (check README).
  * Copyright (C) 2012-2013 Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package ImportModules;
 
 import Utils.IOControl;
 
 /**
  * Plain import class.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class Plain extends Import.Importer {
     
     public static String type = "PLAIN";
     
     public static String propertyType = "IMPORT_PLAIN";
     
     private java.util.ArrayList<String> banned = new java.util.ArrayList();
     
     public Plain(java.util.Properties givenConfig) {
         super(givenConfig);
     }
 
     @Override
     protected void resetState() {
         this.banned = new java.util.ArrayList<>();
         this.dirtyStatus = false;
         IOControl.serverWrapper.disableDirtyState(type, importerName, importerPrint);
     }
 
     @Override
     public void tryRecover() {
         //Checking directory access.
         if (null == new java.io.File(currConfig.getProperty("plain_path")).list()) {
             return;
         }
         //Delete file which were not deleted last time.
         for(String banPath : banned) {
             try {
                 java.nio.file.Files.delete(new java.io.File(banPath).toPath());
             } catch (Exception ex) {
                 return;
             }
         }
         //Reset state of the module of there is no exceptions of errors.
         this.resetState();
     }
     
     /**
      * File filter with mask support;
      */
     private class MaskFilter implements java.io.FilenameFilter {
 
         @Override
         public boolean accept(java.io.File dir, String name) {
             if (new java.io.File(dir.getAbsolutePath() + "/" +  name).isDirectory()) {
                 return false;
             }
             if (MaskCalculate(name, currConfig.getProperty("plain_mask"))) {
                 if (currConfig.getProperty("plain_ignore_mask") != null) {
                     return !MaskCalculate(name, currConfig.getProperty("plain_ignore_mask"));
                 } else {
                     return true;
                 }
             } else {
                 return false;
             }
         }
         
         // Mask calculation section
         
         /**
          * Current mask to calculate
          */
         private String curr_mask;
         
         /**
          * Current position of parser cursor;
          */
         private Integer curr_position;
         
         /**
          * Current char in parser;
          */
         private char curr_char;
         
         /**
          * Next char in parser (may be null);
          */
         private char next_char;
         
         /**
          * Main mask parser method;
          * @param name name of file;
          * @param mask mask to check;
          * @return true if filename equal to mask;
          */
         private Boolean MaskCalculate(String name, String mask) {
             if (mask.equals("*")) {
                 return true;
             } else {
                 curr_mask = mask;
                 curr_position = 0;
                 curr_char = curr_mask.charAt(0);
                 next_char = curr_mask.charAt(1);
                 for (Integer index=0; index < name.length(); index++) {
                     if (index == (name.length() - 1)) {
                         if (curr_char == '*' && curr_position == curr_mask.length() - 1) {
                             return true;
                         } else if (name.charAt(name.length() - 1) == curr_char && curr_position == curr_mask.length() - 1) {
                             return true;
                         } else {
                             return false;
                         }
                     }
                     char curr_nchar = name.charAt(index);
                     char next_nchar = name.charAt(index +1);
                     if (curr_char == '*') {
                         if (next_nchar == next_char) {
                             MoveForward();
                         } else {
                             continue;
                         }
                     } else if (curr_char == '?') {
                         MoveForward();
                     } else if (curr_nchar == curr_char) {
                         MoveForward();
                     } else {
                         return false;
                     }
                 }
                 return true;
             }
         }
         
         /**
          * Move parser stack forward;
          */
         private void MoveForward() {
            if (curr_position < curr_mask.length() - 1) {
                curr_char = curr_mask.charAt(++curr_position);
            }
             if (curr_position == curr_mask.length() - 1) {
                 next_char = '\0';
             } else {
                 next_char = curr_mask.charAt(curr_position + 1);
             }
         }
     }
 
     @Override
     protected void doImport() {
         //Stage indicator initialization.
         Integer stage = 0;
         java.io.File[] files = new java.io.File(currConfig.getProperty("plain_path")).listFiles(new MaskFilter());
         //Check if files equals to null. If true then module switch on DIRTY state on the server.
         if (files == null) {
             IOControl.serverWrapper.log(IOControl.IMPORT_LOGID + ":" + importerName, 1, "невожливо отримати доступ до теки імпорту: " + currConfig.getProperty("plain_path"));
             this.dirtyStatus = true;
             IOControl.serverWrapper.enableDirtyState(type, importerName, importerPrint);
             //Exit function to prevent NullPointerException.
             return;
         }
         //Main loop
         for (java.io.File currFile : files) {
             //Pass banned file pathes (which were posted but cann't be removed).
             if (this.banned.contains(currFile.getAbsolutePath())) {
                 continue;
             }
             try {
                 //(1) File opening/reading stage
                 stage = 1;
                 String fileContent = new String(java.nio.file.Files.readAllBytes(currFile.toPath()));
                 //(2) File removing stage
                 stage = 2;
                 java.nio.file.Files.delete(currFile.toPath());
                 MessageClasses.Message plainMessage = new MessageClasses.Message(
                         currFile.getName(), "root", "UKN", new String[] {currConfig.getProperty("plain_dir")}, new String[] {"тест"}, 
                         fileContent);
                 plainMessage.PROPERTIES.add(new MessageClasses.MessageProperty(propertyType, "root", this.importerPrint, IOControl.serverWrapper.getDate()));
                 if (this.currConfig.containsKey("plain_copyright_override")) {
                     plainMessage.PROPERTIES.add(new MessageClasses.MessageProperty("COPYRIGHT", "root", currConfig.getProperty("plain_copyright_override"), IOControl.serverWrapper.getDate()));
                 }
                 IOControl.serverWrapper.addMessage(plainMessage);
                 //Log this event if such behavior specified by config.
                 if ("1".equals(currConfig.getProperty("opt_log"))) {
                     IOControl.serverWrapper.log(IOControl.IMPORT_LOGID + ":" + importerName, 3, "прозведено імпорт файлу " + currFile.getAbsolutePath());
                 }
             } catch (java.io.IOException ex) {
                 //If file deletion failed.
                 if (stage == 2) {
                     IOControl.serverWrapper.log(IOControl.IMPORT_LOGID + ":" + importerName, 1, "невожливо видалити файл " + currFile.getAbsolutePath() + ". Файл долучено до чорного списку!");
                     //Add file path to banned list and switch dirty state.
                     this.banned.add(currFile.getAbsolutePath());
                     this.dirtyStatus = true;
                     IOControl.serverWrapper.enableDirtyState(type, importerName, importerPrint);
                 //Or if file cann't be open/read.
                 } else {
                     IOControl.serverWrapper.log(IOControl.IMPORT_LOGID + ":" + importerName, 1, "невожливо открити файл " + currFile.getAbsolutePath());
                 }
             }
         }
     }
 }
