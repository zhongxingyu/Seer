 package com.ags.pirate.web.downloader;
 
 import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * @author AGS
  *         Date: 4/10/12
  */
 public class HTMLProxyDownloader implements Downloader {
 
     private static Logger LOGGER = LoggerFactory.getLogger(HTMLProxyDownloader.class);
     private int reconnect = 0;
     private String user;
     private String password;
 
     public HTMLProxyDownloader(String user,String password, String proxyHost, String proxyPort) {
         this.user=user;
         this.password=password;
         System.setProperty("http.proxyHost", proxyHost);
         System.setProperty("http.proxyPort", proxyPort);
     }
 
     public String getHtml(String urlString) throws Exception {
         LOGGER.info("connecting to " + urlString);
         try {
             URL url = new URL(urlString);
             URLConnection urlConnection = url.openConnection();
            //fix for 403: simulate browser connection
            urlConnection.setRequestProperty("User-agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36");
             String authString = user+":"+password;
             String auth = Base64.encode(authString.getBytes());
             urlConnection.setRequestProperty("Proxy-Connection", "Keep-Alive");
             urlConnection.setRequestProperty("Proxy-Authorization", "Basic " + auth);
             BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
             
             LOGGER.info("connected to "+urlString);
             StringBuilder sb = new StringBuilder();
             String inputLine;
             while ((inputLine = in.readLine()) != null) sb.append(inputLine);
             in.close();
             return sb.toString();
 
 
         } catch (Exception e) {
             reconnect++;
             if (reconnect > 3) {
                 LOGGER.error("connection lost to " + urlString);
                 throw new Exception("connection lost to " + urlString);
             } else {
                 LOGGER.warn("retrying connectiong to " + urlString);
                 return getHtml(urlString);
             }
         }
     }
 }
