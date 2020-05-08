 package org.google.code.translate;
 
 import com.intellij.openapi.application.ApplicationManager;
 import com.intellij.util.net.HttpConfigurable;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class contains convenient methods for grabbing information
  * from remote translation server.
  *
  * @author Alexander Shvets
  * @version 1.0 04/07/2007
  */
 public class TranslateHelper {
   private static String hostURL = "http://code.google.com";
 
   private static List pairs = new ArrayList();
 
   /**
    * Creates new translate helper.
    *
    * @throws Exception the exception
    */
   public TranslateHelper() throws Exception {
     HttpConfigurable httpConfigurable = (HttpConfigurable)
       ApplicationManager.getApplication().getComponent("HttpConfigurable");
 
     if (httpConfigurable != null) {
       if (httpConfigurable.USE_HTTP_PROXY) {
         System.getProperties().put("proxySet", Boolean.valueOf(httpConfigurable.USE_HTTP_PROXY).toString());
         System.getProperties().put("proxyPort", Integer.toString(httpConfigurable.PROXY_PORT));
         System.getProperties().put("proxyHost", httpConfigurable.PROXY_HOST);
       }
     }
 
     if(pairs.size() == 0) {
       prepareLangPairs();
     }
   }
 
   private void prepareLangPairs() throws Exception {
     String start = "<select name=langpair>";
     String end = "</select>";
 
     String url = hostURL + "/translate_t?langpair=en|ru&text=test";
 
     URLConnection urlConnection = prepareURLConnection(url);
 
     String result = "";
 
     InputStream is = urlConnection.getInputStream();
 
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
     while (true) {
       int ch = is.read();
 
       if (ch == -1) {
         break;
       }
 
       baos.write(ch);
     }
 
     is.close();
     baos.close();
 
     String s = new String(baos.toByteArray(), "UTF-8");
 
     int index = s.indexOf(start);
 
     if (index != -1) {
       String s2 = s.substring(index + start.length());
 
       int index2 = s2.indexOf(end);
 
       if (index2 != -1) {
         result = s2.substring(0, index2);
       }
     }
 
     if (result != null) {
       result = result.trim();
     }
 
     String str1 = "<option value=";
     String str2 = "</option>";
 
     boolean ok = false;
 
     while (!ok) {
       if (result == null || result.trim().length() == 0) {
         ok = true;
       } else {
         int index1 = result.indexOf(str1);
         int index2 = result.indexOf(str2);
 
         String s2 = result.substring(index1 + str1.length(), index2);
 
         int index3 = s2.substring(1).indexOf("\"");
         int index4 = s2.indexOf(">");
 
         String key = s2.substring(1, index3+1);
         String value = s2.substring(index4 + 1);
 
         pairs.add(new KeyValuePair(key, value));
 
         result = result.substring(index2 + str2.length());
       }
     }
   }
 
   private URLConnection prepareURLConnection(String url)
     throws IOException {
     URLConnection urlConnection = new URL(url).openConnection();
 
     urlConnection.setRequestProperty("Accept", "*/*");
     urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; Maxthon; .NET CLR 1.1.4322)");
     urlConnection.setRequestProperty("Pragma", "no-cache");
 
     return urlConnection;
   }
 
   public List getLangPairs() {
     return pairs;
   }
 
   public String translate(String request, String langPair) throws Exception {
     String start = "<div id=result_box dir=ltr>";
     String end = "</div>";
 
     request = preProcess(request);
 
     String url = hostURL + "/translate_t" + "?" + "langpair=" + langPair +
                 "&text=" + request;
 
     TranslateHelper translateHelper = new TranslateHelper();
 
     URLConnection urlConnection = translateHelper.prepareURLConnection(url);
 
     String result = "";
 
     InputStream is = urlConnection.getInputStream();
 
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
 
     while (true) {
       int ch = is.read();
 
       if (ch == -1) {
         break;
       }
 
       baos.write(ch);
     }
 
     is.close();
     baos.close();
 
     String s = new String(baos.toByteArray(), "UTF-8");
 
     int index1 = s.indexOf(start);
 
     if (index1 != -1) {
       String s2 = s.substring(index1 + start.length());
 
       int index2 = s2.indexOf(end);
 
       if (index2 != -1) {
         result = s2.substring(0, index2);
       }
     }
 
     if (result != null) {
       return postProcess(result);
     }
 
     return request;
   }
 
   private String preProcess(String text) {
     //text = text.replaceAll("\"", " ");
     text = text.replaceAll("<", "%20");
     text = text.replaceAll(">", "%20");
     text = text.replaceAll(" ", "%20");
     text = text.replaceAll("\n", "%20");
     text = text.replaceAll("\r", "%20");
 
     return text;
   }
 
   private String postProcess(String text) {
     text = text.trim();
 
     text = text.replaceAll("\\Q&quot;\\E", "\"");
 
     return text;
   }
 
 }
