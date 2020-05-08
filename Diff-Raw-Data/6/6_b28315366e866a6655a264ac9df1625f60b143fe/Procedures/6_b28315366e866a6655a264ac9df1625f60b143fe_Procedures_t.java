 /**
  * This file is part of RibbonServer application (check README).
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
 
 package ribbonserver;
 
 import Utils.IOControl;
 
 /**
  * Main system procedures
  * @author Stanislav Nepochatov
  * @since RibbonServer a1
  */
 public class Procedures {
     
     private static String LOG_ID = "ПРОЦЕДУРИ";
     
     /**
      * Post given message into the system information stream
      * @param givenMessage message which should be released
      * @return processing status;
      * @since RibbonServer a1
      */
     public static synchronized String PROC_POST_MESSAGE(MessageClasses.Message givenMessage) {
        if (RibbonServer.CURR_STATE == RibbonServer.SYS_STATES.MAINTAINING || RibbonServer.CURR_STATE == RibbonServer.SYS_STATES.INIT || RibbonServer.CURR_STATE == RibbonServer.SYS_STATES.CLOSING) {
             RibbonServer.logAppend(LOG_ID, 1, "неможливо випустити повідомлення, система не готова!");
             return "RIBBON_ERROR:Система не готова!";
         } else {
            Integer failedIndex = AccessHandler.checkAccessForAll(givenMessage.AUTHOR, givenMessage.DIRS, 1);
             if (failedIndex != null) {
                 return "RIBBON_ERROR:Помилка доступу до напрямку " + givenMessage.DIRS[failedIndex];
             }
             if (givenMessage.ORIG_INDEX.equals("-1")) {
                 givenMessage.ORIG_AUTHOR = givenMessage.AUTHOR;
             } else {
                 givenMessage.ORIG_AUTHOR = Messenger.getMessageEntryByIndex(givenMessage.ORIG_INDEX).AUTHOR;
             }
             Messenger.addMessageToIndex(givenMessage);
             if (RibbonServer.IO_ENABLED && IOControl.dispathcer.checkExport(givenMessage.DIRS)) {
                 IOControl.dispathcer.initExport(givenMessage);
             }
             writeMessage(givenMessage.DIRS, givenMessage.INDEX, givenMessage.CONTENT);
             givenMessage.CONTENT = null;
             IndexReader.appendToBaseIndex(givenMessage.returnEntry().toCsv());
             for (Integer dirIndex = 0; dirIndex < givenMessage.DIRS.length; dirIndex++) {
                 if (givenMessage.DIRS[dirIndex] == null) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо випустити повідомлення" + givenMessage.HEADER + "на напрямок " + givenMessage.DIRS[dirIndex]);
                 } else {
                     RibbonServer.logAppend(LOG_ID, 3, givenMessage.DIRS[dirIndex] + " додано повідомлення: [" + givenMessage.HEADER + "]");
                 }
             }
             return "OK:";
         }
     }
     
     /**
      * Write message content to file and create links
      * @param fullPath full path to message file
      * @param messageContent content of the message
      * @since RibbonServer a1
      */
     public static void writeMessage(String[] dirArr, String strIndex, String messageContent) {
         String currPath = "";
         try {
             for (Integer pathIndex = 0; pathIndex < dirArr.length; pathIndex++) {
                 if (dirArr[pathIndex] == null) {
                     continue;
                 } else {
                     currPath = Directories.getDirPath(dirArr[pathIndex]);
                     if (currPath == null) {
                         continue;
                     }
                     else {
                         java.io.FileWriter messageWriter = new java.io.FileWriter(currPath + strIndex);
                         messageWriter.write(messageContent);
                         messageWriter.close();
                     }
                 }
             }
         } catch (java.io.IOException ex) {
             RibbonServer.logAppend(LOG_ID, 1, "Неможливо записити файл за шлязом: " + currPath + strIndex);
         } catch (UnsupportedOperationException ex) {
             RibbonServer.logAppend(LOG_ID, 1, "Неможливо створити посилання на файл!");
         }
     }
     
     /**
      * Modify message by given template message.
      * @param oldMessage original message to modify;
      * @param newMessage override template message;
      * @since RibbonServer a2
      */
     public static synchronized void PROC_MODIFY_MESSAGE(MessageClasses.MessageEntry oldMessage, MessageClasses.Message newMessage) {
         makeCleanup(oldMessage.DIRS, newMessage.DIRS, oldMessage.INDEX);
         Messenger.modTagIndex(oldMessage, oldMessage);
         oldMessage.modifyMessageEntry(newMessage);
         writeMessage(oldMessage.DIRS, oldMessage.INDEX, newMessage.CONTENT);
         IndexReader.updateBaseIndex();
     }
     
     /**
      * Make cleanup within old unused dirs in modifyed message;
      * @param oldDirs array with old dirs;
      * @param newDirs array with new dirs;
      * @since RibbonServer a2
      */
     private static void makeCleanup(String[] oldDirs, String[] newDirs, String strIndex) {
         for (Integer oldIndex = 0; oldIndex < oldDirs.length; oldIndex++) {
             for (Integer newIndex = 0; newIndex < newDirs.length; newIndex++) {
                 if(oldDirs[oldIndex].equals(newDirs[newIndex])) {
                     oldDirs[oldIndex] = null;
                     break;
                 }
             }
             if (oldDirs[oldIndex] != null) {
                 String path = Directories.getDirPath(oldDirs[oldIndex]) + strIndex;
                 try {
                     java.nio.file.Files.delete(new java.io.File(path).toPath());
                 } catch (java.io.IOException ex) {
                     RibbonServer.logAppend(LOG_ID, 1, "неможливо видалити повідомлення: " + path);
                 }
             }
         }
     }
     
     /**
      * Delete message from all indexes.
      * @param givenEntry entry to delete
      * @since RibbonServer a1
      */
     public static synchronized void PROC_DELETE_MESSAGE(MessageClasses.MessageEntry givenEntry) {
         for (Integer pathIndex = 0; pathIndex < givenEntry.DIRS.length; pathIndex++) {
             String currPath = Directories.getDirPath(givenEntry.DIRS[pathIndex]) + givenEntry.INDEX;
             try {
                 java.nio.file.Files.delete(new java.io.File(currPath).toPath());
             } catch (java.io.IOException ex) {
                 RibbonServer.logAppend(LOG_ID, 1, "неможливо видалити повідомлення: " + currPath);
             }
         }
         Messenger.deleteMessageEntryFromIndex(givenEntry);
         RibbonServer.logAppend(LOG_ID, 3, "повідомлення за індексом " + givenEntry.INDEX + " вилучено з системи.");
     }
     
     /**
      * Post system launch notification.
      * @since RibbonServer a1
      */
     public static void postInitMessage() {
         String formatLine = "======================================================================================";
         PROC_POST_MESSAGE(new MessageClasses.Message(
             "Системне повідомлення",
             "root",
             "UA",
             new String[] {"СИСТЕМА.Тест"},
             new String[] {"оголошення", "ІТУ"},
             formatLine + "\nСистема \"Стрічка\" " + RibbonServer.RIBBON_MAJOR_VER + "\n" + formatLine + "\n"
                     + "Це повідомлення автоматично генерується системою \"Стрічка\"\n"
                     + "при завантаженні. Зараз система готова для одержання повідомлень."
                     + "\n\n" + RibbonServer.getCurrentDate()));
     }
     
     /**
      * Post exception as message to the debug directory.
      * @param desc short description of exceptional situation;
      * @param ex exception object;
      * @since RibbonServer a2
      */
     public static void postException(String desc, Throwable ex) {
         if (RibbonServer.DEBUG_POST_EXCEPTIONS) {
             StringBuffer exMesgBuf = new StringBuffer();
             exMesgBuf.append(desc);
            exMesgBuf.append("\n");
             exMesgBuf.append(ex.getClass().getName() + "\n");
            exMesgBuf.append(ex.getMessage());
             StackTraceElement[] stackTrace = ex.getStackTrace();
             for (StackTraceElement element : stackTrace) {
                 exMesgBuf.append(element.toString() + "\n");
             }
             MessageClasses.Message exMessage = new MessageClasses.Message(
                     "Звіт про помилку", "root", "UA", new String[] {RibbonServer.DEBUG_POST_DIR}, 
                     new String[] {"СТРІЧКА", "ПОМИЛКИ"}, exMesgBuf.toString());
             Procedures.PROC_POST_MESSAGE(exMessage);
             SessionManager.broadcast("RIBBON_UCTL_LOAD_INDEX:" + exMessage.toCsv(), RibbonProtocol.CONNECTION_TYPES.CLIENT);
         }
     }
 }
