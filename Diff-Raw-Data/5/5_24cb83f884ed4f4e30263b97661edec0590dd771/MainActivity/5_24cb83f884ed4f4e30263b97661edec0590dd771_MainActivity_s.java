 /*
  * Copyright Stanislav Belogolov 2011.
  *
  * This file is a part of Bilinger. Bilinger is free software distributed under
  * the terms of GNU General Public License version 3. You should have received
  * a copy of the GNU General Public License along with Bilinger. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 
 package ru.alepar.bilinger;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.webkit.JsResult;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 
 import java.io.*;
 
 public class MainActivity extends Activity {
   static final String PAGE_HEADER = "<!DOCTYPE html>\n<html><head><meta charset='utf-8'>" +
       "<link rel='stylesheet/less' type='text/css' href='file:///android_res/raw/styles.less'>" +
       "<script src='file:///android_res/raw/less.js' type='text/javascript'></script>" +
       "</head><body>";
 
   private static final String LOG_TAG = "MainActivity";
   
   private Handler handler = new Handler();
 
   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
 
     // Prepare page
     final StringBuilder pageBuffer = new StringBuilder(PAGE_HEADER);
     final BufferedReader bibook;
     int i = 0;
     try {
       bibook = new BufferedReader(
           new InputStreamReader(new FileInputStream("/mnt/sdcard/bibooks/sample.txt"), "UTF-8"));
      for (String str; (str = bibook.readLine()) != null; ) {
         final String[] sentences = str.split("\t");
        if (i++ == 4)
           addSentence(pageBuffer, sentences[1], String.valueOf(i));
         else
           addSentence(pageBuffer, sentences[0], String.valueOf(i));
       }
       bibook.close();
     } catch (FileNotFoundException e) {
       pageBuffer.append("File not found.");
     } catch (IOException e) {
       pageBuffer.append("IOException: ").append(e.getMessage());
     }
     pageBuffer.append("</body></html>");
 
     // Render it.
     final WebView webView = (WebView) findViewById(R.id.visible_page);
     webView.setWebChromeClient(new LogWebChromeClient());
     webView.addJavascriptInterface(new JavaScriptInterface(), "activity");
     final WebSettings webSettings = webView.getSettings();
     webSettings.setJavaScriptEnabled(true);
 
     webView.loadDataWithBaseURL("file:///android_res/raw/", pageBuffer.toString(), "text/html", "UTF-8", null);
   }
   
   private static void addSentence(StringBuilder pageBuffer, String sentence, String id) {
     pageBuffer.append("<a onClick='window.activity.selectSentence(\"").append(id).append("\")'>")
         .append(sentence).append("</a> ");
   }
 
   /**
    * Provides a hook for calling "alert" from javascript. Useful for
    * debugging your javascript.
    */
   final class LogWebChromeClient extends WebChromeClient {
     @Override
     public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
       Log.d(LOG_TAG, message);
       result.confirm();
       return true;
     }
   }
   
   final class JavaScriptInterface {
     public void selectSentence(String id) {
       final String str_id = id;
       handler.post( new Runnable() {
         @Override
         public void run() {
           Log.d(LOG_TAG, "Clicked sentence " + str_id);
         }
       });
     }
   }
 }
