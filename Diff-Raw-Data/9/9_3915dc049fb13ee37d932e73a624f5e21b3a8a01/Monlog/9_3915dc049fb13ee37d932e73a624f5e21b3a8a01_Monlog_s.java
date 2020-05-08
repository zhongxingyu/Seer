 package se.liu.tdp024.util;
 
 import java.io.*;
 import java.net.*;
 import java.util.Calendar;
 
 public final class Monlog {
     private static boolean loggingOn = true;
 
     public static void setLoggingOn() { loggingOn = true; }
     public static void setLoggingOff() { loggingOn = false; }
 
    public static abstract class Severity {
         public static final int DEBUG     = 0;
         public static final int INFO      = 1;
         public static final int NOTIFY    = 2;
         public static final int WARNING   = 3;
         public static final int ERROR     = 4;
         public static final int CRITICAL  = 5;
         public static final int ALERT     = 6;
         public static final int EMERGENCY = 7;
     }
 
     public static Monlog getLogger() {
         final Throwable t = new Throwable();
         final StackTraceElement methodCaller = t.getStackTrace()[1];
         return new Monlog(methodCaller.getClassName());
     }
 
     private Monlog(String caller) {
         this.caller = caller;
     }
 
     private String caller;
     private static final String MONLOG_ENDPOINT = "http://www.ida.liu.se/~TDP024/monlog/api/log/";
     private static final String API_KEY = "423b0ef8aa9b0e030e785a63262c06c81d1beaa7";
     private static final String REQUEST_URL = MONLOG_ENDPOINT + "?api_key=" + API_KEY + "&format=json";
 
     
     public void log(int severity, String shortDescriptionArg, String longDescription) {
         if (!loggingOn) { return; }
         String shortDescription = caller + " " + shortDescriptionArg;
 
         StringBuilder dataBuilder = new StringBuilder();
         dataBuilder.append("{");
         dataBuilder.append("\"").append("severity").append("\":").append(severity).append(",");
         dataBuilder.append("\"").append("timestamp").append("\":\"").append(Calendar.getInstance().getTimeInMillis()).append("\",");
         dataBuilder.append("\"").append("short_desc").append("\":\"").append(shortDescription).append("\",");
         dataBuilder.append("\"").append("long_desc").append("\":\"").append(longDescription).append("\"");
         dataBuilder.append("}");
 
         try {
 
             URL url = new URL(REQUEST_URL);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.addRequestProperty("Content-Type", "application/json");
             connection.setDoOutput(true);
             connection.setConnectTimeout(60000);
             connection.setRequestMethod("POST");
 
             // make newlines safe
             String json = dataBuilder.toString().replaceAll("\\\n", "\\\\n");
             OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
             writer.write(json);
             writer.close();
 
             if (connection.getResponseCode() == HttpURLConnection.HTTP_OK || connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
 
                 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                 String line;
                 StringBuilder builder = new StringBuilder();
                 while ((line = reader.readLine()) != null) {
                     builder.append(line);
                 }
                 reader.close();
             } else {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                 String line;
                 StringBuilder builder = new StringBuilder();
                 while ((line = reader.readLine()) != null) {
                     builder.append(line);
                 }
                 reader.close();
             }
 
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 
 
     }
 }
