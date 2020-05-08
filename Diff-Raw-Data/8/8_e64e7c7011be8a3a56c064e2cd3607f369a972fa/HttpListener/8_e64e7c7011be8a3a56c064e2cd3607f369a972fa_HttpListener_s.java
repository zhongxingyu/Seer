 package com.rz.metrics.core.listeners;
 
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * Author: richardz
  * Date: 6/7/12 Time: 4:21 PM
  */
 public class HttpListener implements IListener {
     private String url;
     private int connectTimeout = 1000;
 
     public HttpListener(String url) {
         this.url = url;
     }
 
     public void setConnectTimeout(int timeout) {
         this.connectTimeout = timeout;
     }
 
     public void onTimeUnit(String data) {
         try {
             URL url = new URL(this.url);
             URLConnection connection = url.openConnection();
             connection.setDoOutput(true);
             connection.setConnectTimeout(this.connectTimeout);
 
             OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
             writer.write(data);
             writer.close();
         } catch (Exception e) {
             //
         }
     }
 }
