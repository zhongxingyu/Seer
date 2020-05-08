 package com.ags.pirate.web.downloader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * @author AGS
  *         Date: 4/10/12
  */
 public class HTMLDownloader implements Downloader {
 
     private static Logger LOGGER = LoggerFactory.getLogger(HTMLDownloader.class);
     private int reconnect = 0;
 
 
     public String getHtml(String urlString) throws Exception {
 
         LOGGER.info("connecting to " + urlString);
         try {
 
             if (reconnect < 3) {
 
                 URL url = new URL(urlString);
                 URLConnection urlConnection = url.openConnection();
                //fix for 403: simulate browser connection
                urlConnection.setRequestProperty("User-agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
                 BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                 
                 LOGGER.info("connected to " + urlString);
                 StringBuilder sb = new StringBuilder();
                 String inputLine;
                 while ((inputLine = in.readLine()) != null) sb.append(inputLine.replace("&nbsp;"," "));
                 in.close();
                 return sb.toString();
 
             } else {
                 LOGGER.error("connection lost after 3 retries");
                 throw new Exception("connection lost to " + urlString);
             }
         } catch (SocketTimeoutException exception) {
             LOGGER.warn("retying connection... ");
             reconnect++;
             return getHtml(urlString);
         } catch (IOException exception) {
             LOGGER.warn("retying connection... ");
             reconnect++;
             return getHtml(urlString);
         }
     }
 }
