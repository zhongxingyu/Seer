 package com.daemonspoint.httpcat;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class HttpCat {
     public static void main(String[] args) throws IOException {
        URL u = new URL(args[0]);
         URLConnection c = u.openConnection();
         InputStream is = c.getInputStream();
         byte[] buffer = new byte[10240];
         int len = 0;
         while ((len = is.read(buffer)) >= 0) {
             System.out.write(buffer, 0, len);
         }
     }
 }
