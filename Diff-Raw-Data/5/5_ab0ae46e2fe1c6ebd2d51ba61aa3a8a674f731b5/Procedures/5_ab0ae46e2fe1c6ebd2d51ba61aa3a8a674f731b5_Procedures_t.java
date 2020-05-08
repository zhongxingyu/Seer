 /*
  * This code is distributed under terms of GNU GPLv2.
  * *See LICENSE file.
  * ©UKRINFORM 2011-2012
  */
 
 package ribbonserver;
 
 /**
  * Main system procedures
  * @author Stanislav Nepochatov
  */
 public class Procedures {
     
     private static String LOG_ID = "Бібліотека процедур";
     
     /**
      * <b>[RIBBON a1]</b><br>
      * Post given message into the system information stream
      * @param givenMessage message which should be released
      * @return processing status;
      */
     public static synchronized String PROC_POST_MESSAGE(MessageClasses.Message givenMessage) {
         if (RibbonServer.CURR_STATE != RibbonServer.SYS_STATES.READY) {
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
             writeMessage(givenMessage.DIRS, givenMessage.INDEX, givenMessage.CONTENT);
            givenMessage.CONTENT = null;
             indexReader.appendToBaseIndex(givenMessage.returnEntry().toCsv());
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
      */
     public static synchronized void writeMessage(String[] dirArr, String strIndex, String messageContent) {
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
      * <b>[RIBBON a1]</b><br>
      * Delete message from all indexes.
      * @param givenEntry entry to delete
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
         RibbonServer.logAppend(LOG_ID, 3, "повідомлення за індексом " + givenEntry.INDEX + "вилучено з системи.");
     }
     
     /**
      * Post system launch notification
      */
     public static void postInitMessage() {
         String formatLine = "======================================================================================";
         PROC_POST_MESSAGE(new MessageClasses.Message(
             "Системне повідомлення",
             "root",
             "UA",
             new String[] {"СИСТЕМА.Тест"},
             new String[] {"оголошення", "ІТУ"},
             formatLine + "\nСистема \"Стрічка\" " + RibbonServer.RIBBON_VER + "\n" + formatLine + "\n"
                     + "Це повідомлення автоматично генерується системою \"Стрічка\"\n"
                     + "при завантаженні. Зараз система готова для одержання повідомлень."
                     + "\n\n" + RibbonServer.getCurrentDate()));
     }
 }
