 package com.fatwire.dta.sscrawler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.nio.charset.Charset;
 import java.util.concurrent.Callable;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class UrlRenderingCallable implements Callable<ResultPage> {
     private final Log log = LogFactory.getLog(getClass());
 
     private final HttpClient client;
 
     private final String uri;
     
     private final QueryString qs;
 
     /**
      * @param client
      * @param uri
      */
     public UrlRenderingCallable(final HttpClient client, final String uri,final QueryString qs) {
         super();
         this.client = client;
         this.uri = uri;
         this.qs=qs;
     }
 
     public ResultPage call() throws Exception {
         if (log.isDebugEnabled())
             log.debug("downloading " + uri);
         final long startTime = System.currentTimeMillis();
         final ResultPage page = new ResultPage(qs);
         final GetMethod httpGet = new GetMethod(uri);
         httpGet.setFollowRedirects(true);
 
         try {
             final int responseCode = client.executeMethod(httpGet);
             page.setResponseCode(responseCode);
             //log.info(iGetResultCode);
             page.setResponseHeaders(httpGet.getResponseHeaders());
             if (responseCode == 200) {
                 final String charSet = httpGet.getResponseCharSet();
                 //log.info(charSet);
 
                 final InputStream in = httpGet.getResponseBodyAsStream();
                 if (in != null) {
                     final Reader reader = new InputStreamReader(in, Charset
                             .forName(charSet));
                     final String responseBody = copy(reader);
                     in.close();
                     page.setReadTime(System.currentTimeMillis() - startTime);
                     if (responseBody != null) {
                         if (log.isTraceEnabled()) {
                             log.trace(responseBody);
                         }
                         page.setBody(responseBody);
                     }
 
                 }
             } else {
                 
                 page.setReadTime(System.currentTimeMillis() - startTime);
                 log.error("reponse code is " + responseCode + " for "
                         + httpGet.getURI().toString());
             }

         } finally {
             httpGet.releaseConnection();
         }
         return page;
     }
 
     /**
      * @param builder
      * @param reader
      * @throws IOException
      */
     private String copy(final Reader reader) throws IOException {
         final StringBuilder builder = new StringBuilder();
         final char[] c = new char[1024];
         int s;
         while ((s = reader.read(c)) != -1) {
             builder.append(c, 0, s);
 
         }
         return builder.toString();
     }
 
     /**
      * @return the uri
      */
     public String getUri() {
         return uri;
     }
 
 }
