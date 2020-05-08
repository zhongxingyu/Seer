 package org.lastbamboo.common.http.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.zip.GZIPInputStream;
 
import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.StatusLine;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.io.IOUtils;
 import org.lastbamboo.common.util.Pair;
 import org.lastbamboo.common.util.UriUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Issues a post request using HTTP client. 
  */
 public class BaseHttpClientRequester
     {
 
     private final Logger LOG = LoggerFactory.getLogger(getClass());
     private final HttpClient m_httpClient = new HttpClient();
     private final String m_url;
     
     public BaseHttpClientRequester(final String baseAddress, 
         final Collection<Pair<String, String>> parameters)
         {
         final StringBuilder sb = new StringBuilder ();
         sb.append (baseAddress);
         sb.append (UriUtils.getUrlParameters (parameters));
         this.m_url = sb.toString ();
         LOG.debug("Using URL string: "+this.m_url);
         }
     
 
     public String post() throws IOException
         {
         final PostMethod method = new PostMethod(this.m_url);
         return request(method);
         }
     
     public String get() throws IOException
         {
         final GetMethod method = new GetMethod(this.m_url);
         return request(method);
         }
 
     private String request(final HttpMethod method) throws IOException
         {
         try
             {
             this.m_httpClient.executeMethod(method);
             final int statusCode = method.getStatusCode();
             final StatusLine statusLine = method.getStatusLine();
             final InputStream is;
            final Header encoding = 
                method.getResponseHeader("Content-Encoding");
            if (encoding != null && encoding.getValue().equals("gzip"))
                 {
                 is = new GZIPInputStream(method.getResponseBodyAsStream());
                 }
             else
                 {
                 is = method.getResponseBodyAsStream();
                 }
             final String body = IOUtils.toString(is);
             
             if (statusCode != HttpStatus.SC_OK)
                 {
                 LOG.warn("ERROR ISSUING REQUEST: " + this.m_url + "\n" +
                     statusLine + "\n" + body);
                 }
             else
                 {
                 LOG.debug("Successfully wrote request...");
                 }
             
             return body;
             }
         finally
             {
             method.releaseConnection();
             }
         
         }
 
     }
