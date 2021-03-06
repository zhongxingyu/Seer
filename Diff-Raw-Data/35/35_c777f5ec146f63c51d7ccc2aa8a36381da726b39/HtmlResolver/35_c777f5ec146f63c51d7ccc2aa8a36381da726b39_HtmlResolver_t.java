 /*
  * Copyright 2006-2007 Sxip Identity Corporation
  */
 
 package net.openid.discovery;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.htmlparser.Parser;
 import org.htmlparser.Node;
 import org.htmlparser.nodes.TagNode;
 import org.htmlparser.filters.TagNameFilter;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.ParserException;
 import org.htmlparser.util.NodeIterator;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Marius Scurtescu, Johnny Bufu
  */
 public class HtmlResolver
 {
     /**
      * Maximum number of redirects to be followed for the HTTP calls.
      */
     private int _maxRedirects = 10;
 
     /**
      * Maximum length (in bytes) to read when parsing a HTML response.
      */
     private int _maxHtmlSize = 100000;
 
     /**
      * HTTP connect timeout, in milliseconds.
      */
     private int _connTimeout = 3000;
 
     /**
      * HTTP socket (read) timeout, in milliseconds.
      */
     private int _socketTimeout = 5000;
 
     /**
      * Gets the internal limit configured for the maximum number of redirects
      * to be followed for the HTTP calls.
      */
     public int getMaxRedirects()
     {
         return _maxRedirects;
     }
 
     /**
      * Sets the maximum number of redirects to be followed for the HTTP calls.
      */
     public void setMaxRedirects(int maxRedirects)
     {
         this._maxRedirects = maxRedirects;
     }
 
     /**
      * Gets the aximum length (in bytes) to read when parsing a HTML response.
      */
     public int getMaxHtmlSize()
     {
         return _maxHtmlSize;
     }
 
     /**
      * Sets maximum length (in bytes) to read when parsing a HTML response.
      */
     public void setMaxHtmlSize(int maxHtmlSize)
     {
         this._maxHtmlSize = maxHtmlSize;
     }
 
     /**
      * Gets the HTTP connect timeout, in milliseconds.
      */
     public int getConnTimeout()
     {
         return _connTimeout;
     }
 
     /**
      * Sets the HTTP connect timeout, in milliseconds.
      */
     public void setConnTimeout(int connTimeout)
     {
         this._connTimeout = connTimeout;
     }
 
     /**
      * Gets the HTTP socket (read) timeout, in milliseconds.
      */
     public int getSocketTimeout()
     {
         return _socketTimeout;
     }
 
     /**
      * Sets HTTP socket (read) timeout, in milliseconds.
      */
     public void setSocketTimeout(int socketTimeout)
     {
         this._socketTimeout = socketTimeout;
     }
 
     /**
      * Performs HTML discovery on the supplied URL identifier.
      *
      * @param identifier        The URL identifier.
      * @return                  HTML discovery data obtained from the URL.
      */
     public HtmlResult discover(UrlIdentifier identifier)
             throws DiscoveryException
     {
         // initialize the results of the HTML discovery
         HtmlResult result = new HtmlResult();
 
         // get the HTML data (and set the claimed identifier)
         String htmlData = call(identifier.getUrl(), result);
 
         parseHtml(htmlData, result);
 
         return result;
     }
 
     /**
      * Performs a HTTP call on the provided URL identifier.
      *
      * @param url       The URL identifier.
      * @param result    The HTML discovery result, in which the claimed
      *                  identifier is set to the input URL after following
      *                  redirects.
      * @return          The retrieved HTML data.
      */
     private String call(URL url, HtmlResult result) throws DiscoveryException
     {
         HttpClient client = new HttpClient();
         client.getParams().setParameter("http.protocol.max-redirects",
                 new Integer(_maxRedirects));
         client.getParams().setParameter("http.protocol.allow-circular-redirects",
                 Boolean.TRUE);
        client.getParams().setParameter("http.protocol.cookie-policy", 
                CookiePolicy.IGNORE_COOKIES);

         client.getParams().setSoTimeout(_socketTimeout);
         client.getHttpConnectionManager()
                 .getParams().setConnectionTimeout(_connTimeout);
 
         GetMethod get = new GetMethod(url.toString());
         get.setFollowRedirects(true);
 
         try
         {
             int statusCode = client.executeMethod(get);
             if (statusCode != HttpStatus.SC_OK)
                 throw new DiscoveryException(
                         "GET failed on " + url.toString());
 
             result.setClaimed( new UrlIdentifier(get.getURI().toString()) );
 
             InputStream htmlInput = get.getResponseBodyAsStream();
             if (htmlInput == null)
                 throw new DiscoveryException("Cannot download HTML mesage from "
                         + url.toString());
 
             byte data[] = new byte[_maxHtmlSize];
 
             int bytesRead = htmlInput.read(data);
             htmlInput.close();
 
             // parse and extract the needed info
             if (bytesRead <= 0)
                 throw new DiscoveryException("No data read from the HTML message");
 
             return new String(data, 0, bytesRead);
 
         } catch (IOException e)
         {
             throw new DiscoveryException("Fatal transport error: ", e);
         }
         finally
         {
             get.releaseConnection();
         }
     }
 
     /**
      * Parses the HTML data and stores in the result the discovered
      * openid information.
      *
      * @param htmlData          HTML data obtained from the URL identifier.
      * @param result            The HTML result.
      */
     private void parseHtml(String htmlData, HtmlResult result)
             throws DiscoveryException
     {
         URL idp1Endpoint = null;
         URL idp2Endpoint = null;
         UrlIdentifier delegate1 = null;
         UrlIdentifier delegate2 = null;
 
         try
         {
             Parser parser = Parser.createParser(htmlData, null);
 
             NodeList heads = parser.parse(new TagNameFilter("HEAD"));
             if (heads.size() != 1)
                 throw new DiscoveryException(
                         "HTML response must have exactly one HEAD element, " +
                                 "found " + heads.size() + " : " + heads.toHtml());
             Node head = heads.elementAt(0);
             for (NodeIterator i = head.getChildren().elements();
                  i.hasMoreNodes();)
             {
                 Node node = i.nextNode();
                 if (node instanceof TagNode)
                 {
                     TagNode link = (TagNode) node;
                     String href = link.getAttribute("href");
 
                     String rel = link.getAttribute("rel");
                     if (rel == null) continue;
                     List relations = Arrays.asList(rel.split(" "));
                     if (relations == null) continue;
 
                     if (relations.contains("openid.server"))
                     {
                         if (idp1Endpoint != null)
                             throw new DiscoveryException(
                                     "More than one openid.server entries found");
                         try
                         {
                             idp1Endpoint = new URL(href);
                             result.setEndpoint1(idp1Endpoint);
 
                         } catch (MalformedURLException e)
                         {
                             throw new DiscoveryException(
                                     "Invalid openid.server URL: " + href);
                         }
                     }
 
                     if (relations.contains("openid.delegate"))
                     {
                         if (delegate1 != null)
                             throw new DiscoveryException(
                                     "More than one openid.delegate entries found");
 
                         delegate1 = new UrlIdentifier(href);
                         result.setDelegate1(delegate1);
                     }
                     if (relations.contains("openid2.provider"))
                     {
                         if (idp2Endpoint != null)
                             throw new DiscoveryException(
                                     "More than one openid.server entries found");
                         try
                         {
                             idp2Endpoint = new URL(href);
                             result.setEndpoint2(idp2Endpoint);
 
                         } catch (MalformedURLException e)
                         {
                             throw new DiscoveryException(
                                     "Invalid openid2.provider URL: " + href);
                         }
                     }
 
                     if (relations.contains("openid2.local_id"))
                     {
                         if (delegate2 != null)
                             throw new DiscoveryException(
                                     "More than one openid2.local_id entries found");
 
                         delegate2 = new UrlIdentifier(href);
                         result.setDelegate2(delegate2);
                     }
                 }
             }
         }
         catch (ParserException e)
         {
             throw new DiscoveryException("Error parsing HTML message", e);
         }
     }
 }
