 package com.weibogrep.crawler;
 
 import java.io.*;
 import java.net.URLEncoder;
 import java.util.List;
 
 import weibo4j.User;
 import weibo4j.Paging;
 import weibo4j.Status;
 import weibo4j.Weibo;
 import weibo4j.WeiboException;
 import weibo4j.http.AccessToken;
 import weibo4j.http.RequestToken;
 
 public class WeiboGate {
     public static String baseDir = "/tmp/weibogrep";
 
     public static String getUserTimeline(AccessToken access) {
         String ret = "";
         Weibo wb = new Weibo();
         User u;
         wb.setToken(access.getToken(), access.getTokenSecret());
         try {
             u = wb.verifyCredentials();
             List<Status> statuses = wb.getUserTimeline("" + u.getId(), new Paging(1, 200));
             for (Status st: statuses) {
                 ret += st.getText() + "\n\n";
             }
         } catch (WeiboException e) {
             e.printStackTrace();
             return ret;
         }
         try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(baseDir + '/' + u.getId() + ".txt")));
             out.write(ret);
             out.flush();
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return ret;
     }
 
     public static String getHomeTimeline() {
         return "";
     }
 }
 
