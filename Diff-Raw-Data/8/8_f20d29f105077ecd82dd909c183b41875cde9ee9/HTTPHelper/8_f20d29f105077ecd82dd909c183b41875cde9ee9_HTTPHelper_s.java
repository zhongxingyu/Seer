 package se.liu.tdp024.util;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 
 public abstract class HTTPHelper {
 
     public static String get(String path, String... parameters) {
 
         try {
             if (parameters != null) {
                 path += "?";
                 for (int i = 0; i < parameters.length; i += 2) {
                    path += parameters[i] + "="
                            + URLEncoder.encode(parameters[i + 1], "UTF-8") + "&";
                 }
             }
         } catch (Exception e) {
             return null;
         }
 
         try {
             URL url = new URL(path);
 
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setConnectTimeout(60000);
             connection.setRequestMethod("GET");
             connection.connect();
 
             if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
 
                 InputStream is = connection.getInputStream();
                 BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
 
                 StringBuilder result = new StringBuilder();
                 String line = null;
                 while ((line = bufferedReader.readLine()) != null) {
                     result.append(line);
                 }
                 bufferedReader.close();
 
                 return result.toString();
 
             } else {
                 // Server returned HTTP error code.
                 System.out.println("Failed attempt - Response code was: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
                 return null;
             }
         } catch (MalformedURLException e) {
             e.printStackTrace();
             return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 }
