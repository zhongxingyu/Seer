 package org.ccci.deployment.util;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.StatusLine;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.HttpHostConnectException;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 
 import com.google.common.base.Throwables;
 
 public class PageMatcher
 {
     Logger log = Logger.getLogger(getClass());
 
     private final HttpClient httpclient;
 
     public PageMatcher(HttpClient client)
     {
         this.httpclient = client;
     }
     
     public PageMatcher()
     {
         httpclient = new DefaultHttpClient();
         
         httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, (int) TimeUnit.SECONDS.toMillis(5));
         httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, (int) TimeUnit.SECONDS.toMillis(30));
     }
     
     /**
      * Repeatedly attempts to request the given {@code uri} and match the given {@code regularExpression} against
     * the returned content.  If the page is unavailable, this will try for up to {@code maxWaitTime} seconds before failing.
      * 
      * The exact behavior is a little complicated to describe; read the code for details.
      * 
      * @param uri
      * @param regularExpression
      * @param pageName 
     * @param maxWaitTime number of seconds to wait before failing
      * @throws RuntimeException if the page cannot be loaded and matched
      */
     public Matcher pingUntilPageMatches(String uri, String regularExpression, String pageName, long maxWaitTime)
     {
         log.debug("requesting " + uri);
         HttpGet request = new HttpGet(uri);
         
         long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxWaitTime);
         
         Pattern successPattern = Pattern.compile(regularExpression, Pattern.DOTALL);
 
         int ioFailures = 0;
         
         Exception mostRecentException = null;
         while (true)
         {
             long currentTime = System.currentTimeMillis();
             if (currentTime > deadline)
             {
                throw new RuntimeException("Unable to connect to " + uri + " after " + maxWaitTime + " seconds of trying", mostRecentException);
             }
 
             try
             {
                 HttpResponse response = httpclient.execute(request);
                 try
                 {
                     StatusLine statusLine = response.getStatusLine();
                     if (statusLine.getStatusCode() == 200)
                     {
                         return matchPage(response, successPattern, regularExpression, pageName);
                     }
                     else
                         if (statusLine.getStatusCode() != 404)
                             throw new RuntimeException(pageName + " not available; status line: " + statusLine);
                         else
                             log.debug("received 404; continuing to try to connect");
                 }
                 finally
                 //free up http connection
                 {
                     response.getEntity().consumeContent();
                 }
             }
             catch (HttpHostConnectException e)
             {
                 mostRecentException = e;
                 log.debug("connection exception; continuing to try to connect", e);
             }
             catch (ClientProtocolException e)
             {
                 throw Throwables.propagate(e);
             }
             catch (IOException e)
             {
                 mostRecentException = e;
                 ioFailures++;
                 if (ioFailures > 2)
                 {
                     throw new RuntimeException("Unable to connect to " + uri + " after " + ioFailures + " IO failures; the last one follows:", e);
                 }
                 else
                 {
                     log.debug("IOException retrieving " + pageName + ": " + e.getMessage() + "; retrying request");
                 }
             }
             
             sleepBriefly();
         }
 
     }
     
     private Matcher matchPage(HttpResponse response, Pattern successPattern, String regularExpression, String pageName) throws ParseException, IOException
     {
         String content = EntityUtils.toString(response.getEntity());
         Matcher matcher = successPattern.matcher(content);
         if (matcher.matches())
         {
             return matcher;
         }
         else
         {
             log.error(pageName + " does not contain " + regularExpression + "; page content follows:" + 
                 System.getProperty("line.separator") +
                 content);
             
             throw new RuntimeException(pageName + " does not match '" + regularExpression + "'.  Actual content was logged.");
         }
     }
 
     private void sleepBriefly()
     {
         try
         {
             TimeUnit.MILLISECONDS.sleep(500);
         }
         catch (InterruptedException interruption)
         {
             throw Throwables.propagate(interruption);
         }
     }
 
     
 
     public Matcher matchPage(String uri, String regularExpression, String pageName, long maxWaitTime)
     {
         log.debug("requesting " + uri);
         HttpGet request = new HttpGet(uri);
         
         long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(maxWaitTime);
         
         Pattern successPattern = Pattern.compile(regularExpression, Pattern.DOTALL);
 
         int ioFailures = 0;
         
         Exception mostRecentException = null;
         while (true)
         {
             long currentTime = System.currentTimeMillis();
             if (currentTime > deadline)
             {
                 throw new RuntimeException("Unable to connect to " + uri + " after " + maxWaitTime + "seconds of trying", mostRecentException);
             }
 
             try
             {
                 HttpResponse response = httpclient.execute(request);
                 try
                 {
                     StatusLine statusLine = response.getStatusLine();
                     if (statusLine.getStatusCode() == 200)
                     {
                         return matchPage(response, successPattern, regularExpression, pageName);
                     }
                     else
                     {
                         throw new RuntimeException(pageName + " not available; status line: " + statusLine);
                     }
                 }
                 finally
                 //free up http connection
                 {
                     response.getEntity().consumeContent();
                 }
             }
             catch (HttpHostConnectException e)
             {
                 throw new RuntimeException("unable to connect", e);
             }
             catch (ClientProtocolException e)
             {
                 throw Throwables.propagate(e);
             }
             catch (IOException e)
             {
                 mostRecentException = e;
                 ioFailures++;
                 if (ioFailures > 1)
                 {
                     throw new RuntimeException("Unable to connect to " + uri + " after " + ioFailures + " IO failures; the last one follows:", e);
                 }
                 else
                 {
                     log.debug("IOException retrieving " + pageName + ": " + e.getMessage() + "; retrying request");
                 }
             }
             
             sleepBriefly();
         }
 
     }
 }
