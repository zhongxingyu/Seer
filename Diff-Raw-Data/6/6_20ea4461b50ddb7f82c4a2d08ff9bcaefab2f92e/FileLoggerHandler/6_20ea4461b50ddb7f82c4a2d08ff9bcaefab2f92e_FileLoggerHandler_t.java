 package jDistsim.utils.logging.handlers;
 
 import jDistsim.utils.logging.LogMessage;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.logging.formatters.DefaultLoggerFormatter;
 import jDistsim.utils.logging.formatters.ILoggerFormatter;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 import java.util.UUID;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 23.9.12
  * Time: 21:53
  */
 public class FileLoggerHandler implements ILoggerHandler {
 
     private String fileName;
     private String directory;
     private ILoggerFormatter loggerFormatter;
 
     public FileLoggerHandler() {
         this(createFileName());
     }
 
     public FileLoggerHandler(String fileName) {
         this(fileName, "");
     }
 
     public FileLoggerHandler(String fileName, String directory) {
         this(new DefaultLoggerFormatter(), fileName, directory);
     }
 
     public FileLoggerHandler(ILoggerFormatter loggerFormatter) {
         this(loggerFormatter, createFileName());
     }
 
     public FileLoggerHandler(ILoggerFormatter loggerFormatter, String fileName) {
         this(loggerFormatter, fileName, "");
     }
 
     public FileLoggerHandler(ILoggerFormatter loggerFormatter, String fileName, String directory) {
         this.loggerFormatter = loggerFormatter;
         this.fileName = fileName;
         this.directory = directory;
     }
 
     @Override
     public void publish(LogMessage logMessage) {
         try {
             File file;
             file = new File(fileName);
             if (!file.exists())
                 file.createNewFile();
 
            PrintWriter output = new PrintWriter(new FileWriter(file));
             output.println(loggerFormatter.applyFormat(logMessage));
             output.close();
 
         } catch (IOException exception) {
             Logger.log(exception);
         } finally {
         }
     }
 
     private static String createFileName() {
         Date currentDate = new Date();
         UUID uuid = UUID.randomUUID();
         String guid = uuid.toString();
         return guid + ".log";
     }
 
 }
