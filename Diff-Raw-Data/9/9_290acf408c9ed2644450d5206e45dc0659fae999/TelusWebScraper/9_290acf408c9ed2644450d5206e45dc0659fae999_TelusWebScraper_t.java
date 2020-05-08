 /*
  * Copyright (C) 2011 by Alexandre Jasmin <alexandre.jasmin@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.github.ajasmin.telususageandroidwidget;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.ProtocolException;
 import org.apache.http.client.RedirectHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HttpContext;
 import org.xml.sax.InputSource;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.github.ajasmin.telususageandroidwidget.TelusWidgetPreferences.PreferencesData;
 
 public class TelusWebScraper {
     private static final long CACHE_LIFETIME = 1 /*hour*/ * 60 * 60 * 1000;
     @SuppressWarnings("serial")
     public static class InvalidCredentialsException extends Exception { }
 
     @SuppressWarnings("serial")
     public static class NetworkErrorException extends Exception {
         public NetworkErrorException(String string, Throwable e) {
             super(string, e);
         }
     }
 
     @SuppressWarnings("serial")
     public static class ParsingDataException extends Exception {
         public ParsingDataException(String string, Throwable e) {
             super(string, e);
         }
     }
 
     public static Map<String, Map<String, String>> retriveUsageSummaryData(PreferencesData prefs) throws InvalidCredentialsException, NetworkErrorException, ParsingDataException {
         Context context = MyApp.getContext();
         String fileName = Integer.toString(prefs.appWidgetId);
 
         if (!context.getFileStreamPath(fileName).exists() ||
                 System.currentTimeMillis() - prefs.lastUpdateTime > CACHE_LIFETIME) {
                         fetchFromTelusSite(prefs);
         }
 
         InputStream summaryHtmlStream;
         try {
             summaryHtmlStream = MyApp.getContext().openFileInput(fileName);
         } catch (FileNotFoundException e) { throw new Error(e); }
 
        // The HTML is valid XHTML except for that one ampersand (&)
        // So we just strip out ampersands from the file. We don't care
        // about the parts of the document containing character entities anyways.
         InputStream stripAmpersandsInputStream = new StripAmpersandInputStream(summaryHtmlStream);
 
         TelusSaxHandler handler = new TelusSaxHandler();
         InputSource inputSource = new InputSource(stripAmpersandsInputStream);
         inputSource.setEncoding("UTF-8");
 
         SAXParser parser = getSAXParserInstance();
         try {
             parser.parse(inputSource, handler);
         } catch (Exception e) {
             throw new ParsingDataException("Error parsing data", e);
         }
 
         if (handler.isLoginError()) {
             throw new InvalidCredentialsException();
         }
 
         return handler.getData();
     }
 
     private static void fetchFromTelusSite(final PreferencesData prefs) throws NetworkErrorException {
         final DefaultHttpClient httpclient = new DefaultHttpClient();
         enableAuto302Redirects(httpclient);
 
         fetchUsageSummaryPage(httpclient, prefs);
 
         prefs.markAsUpdatedNow();
 
         // Log out to avoid session limit
         // on background thread to avoid extra delay
         new Thread(new Runnable() { public void run() {
             try {
                 fetchLogOutPage(httpclient);
                 Log.i("TelusWebScraper", "Logged out " + prefs.email);
             } catch (IOException e) {
                 Log.e("TelusWebScraper", "Couldn't fetch logout page for " + prefs.email, e);
             }
         }}).run();
     }
 
     private static void enableAuto302Redirects(DefaultHttpClient httpclient) {
         httpclient.setRedirectHandler(new RedirectHandler() {
             @Override
             public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
                 if (response.getStatusLine().getStatusCode() == 302)
                     return true;
                 return false;
             }
             @Override
             public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
                 try {
                     return new URI(response.getFirstHeader("Location").getValue());
                 } catch (URISyntaxException e) {
                     throw new Error(e);
                 }
             }
         });
     }
 
     private static void fetchUsageSummaryPage(DefaultHttpClient httpclient, PreferencesData prefs) throws NetworkErrorException {
         try {
             final String url = "https://mobile.telus.com/login.htm";
             HttpPost httpPost = new HttpPost(url);
 
             List<NameValuePair> formparams = new ArrayList<NameValuePair>();
             formparams.add(new BasicNameValuePair("username", prefs.email));
             formparams.add(new BasicNameValuePair("password", prefs.password));
             formparams.add(new BasicNameValuePair("_rememberMe", "on"));
             formparams.add(new BasicNameValuePair("forwardAction", "/index.htm?lang=en"));
 
             UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
             httpPost.setEntity(entity);
 
             HttpResponse response = httpclient.execute(httpPost);
             HttpEntity responseEntity = response.getEntity();
 
            // Save the page to disk
             String fileName = Integer.toString(prefs.appWidgetId);
             FileOutputStream fileOutput = MyApp.getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
             responseEntity.writeTo(fileOutput);
             fileOutput.close();
         } catch (IOException e) {
             throw new NetworkErrorException("Error fetching data from Telus website", e);
         }
     }
 
     private static void fetchLogOutPage(DefaultHttpClient httpclient) throws IOException {
         final String url = "https://mobile.telus.com/logout.htm";
         HttpGet httpGet = new HttpGet(url);
 
         HttpResponse response = httpclient.execute(httpGet);
         HttpEntity responseEntity = response.getEntity();
         // fetch the contents
         responseEntity.getContent().close();
     }
 
     private static SAXParser getSAXParserInstance() {
         try {
             SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
             return parser;
         } catch (Exception e) {
             throw new Error(e);
         }
     }
 }
