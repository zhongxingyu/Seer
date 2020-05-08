 package org.alex73.android.bel;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 public class MyLog {
     final static private String DATETIME_PATTERN = "-yyyyMMdd-HHmmss";
     static private File outFile;
     static private BufferedWriter wr;
 
     public synchronized static void create() {
        outFile = new File(LocalStorage.BACKUP_DIR + "log"
                 + new SimpleDateFormat(DATETIME_PATTERN, Locale.US).format(new Date()) + ".txt");
         outFile.getParentFile().mkdirs();
     }
 
     private synchronized static void logOpen() throws IOException {
         wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile, true), "UTF-8"));
     }
 
     private synchronized static void logClose() {
         try {
             wr.flush();
         } catch (Throwable ex) {
         }
         try {
             wr.close();
         } catch (Throwable ex) {
         }
     }
 
     public synchronized static void log(Throwable error) {
         try {
             logOpen();
             try {
                 wr.write("####### EXECUTION ERROR: " + error.getClass().getName() + "\n");
                 PrintWriter w = new PrintWriter(wr);
                 error.printStackTrace(w);
                 w.flush();
             } finally {
                 logClose();
             }
         } catch (Throwable ex) {
         }
     }
 
     public synchronized static void log(String log) {
         try {
             logOpen();
             try {
                 wr.write(log);
                 wr.write('\n');
             } finally {
                 logClose();
             }
         } catch (Throwable ex) {
         }
     }
 
     public synchronized static void log(List<String> log) {
         try {
             logOpen();
             try {
                 for (String s : log) {
                     wr.write(s);
                     wr.write('\n');
                 }
             } finally {
                 logClose();
             }
         } catch (Throwable ex) {
         }
     }
 }
