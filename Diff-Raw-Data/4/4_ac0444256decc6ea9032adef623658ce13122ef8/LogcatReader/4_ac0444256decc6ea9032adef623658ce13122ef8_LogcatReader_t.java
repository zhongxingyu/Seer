 package com.albertarmea.handsfreeactions;
 
 import android.util.Log;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * Created by aarmea on 5/27/13.
  */
 public class LogcatReader {
     public static final String TAG = "LogcatReader";
 
     private String logBuffer = "main";
     private String logTag = "*:v";
     private boolean tagHasSpaces = false;
     private long messageExpiration = 1000;
 
     private volatile boolean running = false;
     private volatile OnLogReceiveListener onLogReceiveListener = null;
     private Process logcatProcess = null;
     private Thread readerThread = null;
 
     public interface OnLogReceiveListener {
         public abstract void onLogReceive(Date time, String message, String fullMessage);
     }
 
     public LogcatReader () {
     }
 
     public LogcatReader(String newBuffer, String newTag, long newExpiration) {
         setFilters(newBuffer, newTag, newExpiration);
     }
 
     public void setFilters(String newBuffer, String newTag, long newExpiration) {
         logBuffer = newBuffer;
         logTag = newTag;
         messageExpiration = newExpiration;
     }
 
     public void setOnLogReceiveListener(OnLogReceiveListener listener) {
         onLogReceiveListener = listener;
     }
 
     public boolean start() {
         // Start the logcat process
         ArrayList<String> command = new ArrayList<String>();
         // Java has no fast way to initialize an ArrayList with constant values
         command.add("logcat");
         command.add("-b");
         command.add(logBuffer);
         command.add("-v");
         command.add("time");
         if (logTag.indexOf(' ') >= 0) {
             // logcat cannot handle tags containing spaces, so we have Java check for the tag
             tagHasSpaces = true;
         } else {
             command.add("-s");
             command.add(logTag);
         }
         ProcessBuilder processBuilder = new ProcessBuilder(command);
         try {
             logcatProcess = processBuilder.start();
         } catch (IOException e) {
             Log.wtf(TAG, String.format("logcat threw an IOException: %s", e.toString()));
             return false;
         }
 
         // Read the output from logcatProcess continuously
         Runnable runnable = new Runnable() {
             @Override
             public void run() {
                 Log.d(TAG, String.format("Thread starting with tag %s", logTag));
                 running = true;
                String dateFormatString = "MM-dd hh:mm:ss.SSS";
                 SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
                 BufferedReader logcatOutput = new BufferedReader(new InputStreamReader(logcatProcess.getInputStream()));
                 while (running) {
                     try {
                         // The complete logcat line without any parsing
                         String fullMessage = logcatOutput.readLine();
 
                         // Skip this line if it does not contain the tag
                         if (tagHasSpaces) {
                             // Tag always follows the first '/' in the message
                             if (fullMessage.indexOf('/')+1 != fullMessage.indexOf(logTag)) {
                                 continue;
                             }
                         }
 
                         // The date, represented as a Java Date
                         // Parse the date
                         Date time = dateFormat.parse(fullMessage.substring(0, dateFormatString.length()));
                         // Add the current year because logcat does not do it for you
                         Calendar calendar = Calendar.getInstance();
                         calendar.setTime(time);
                         calendar.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
                         time = calendar.getTime();
 
                         // The message itself, excluding time, priority, tag, PID, etc.
                         String message = fullMessage.substring(fullMessage.indexOf("): ")+3);
 
                         // Send the message
                         Log.d(TAG, String.format("Received message at %s", time.toString()));
                         if (onLogReceiveListener == null) {
                             synchronized(onLogReceiveListener) {
                                 if (onLogReceiveListener == null) {
                                     Log.d(TAG, "Could not send message because listener is not set");
                                 } else if (Math.abs((new Date()).getTime() - time.getTime()) < messageExpiration) {
                                         Log.d(TAG, "Sending message");
                                         onLogReceiveListener.onLogReceive(time, message, fullMessage);
                                 }
                             }
                         }
                     } catch (ParseException e) {
                         // Ignore malformed logcat lines
                         Log.v(TAG, "Read invalid logcat line");
                     } catch (IOException e) {
                         Log.wtf(TAG, String.format("logcat threw an IOException: %s", e.toString()));
                     }
                 }
                 try {
                     logcatOutput.close();
                 } catch (IOException e) {
                     // We're trying to close the stream, so we don't care if it fails
                 }
                 Log.d(TAG, "Thread stopping");
             }
         };
         readerThread = new Thread(runnable);
         readerThread.start();
 
         return true;
     }
 
     public void stop() {
         running = false;
         logcatProcess.destroy();
     }
 }
