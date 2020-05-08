 /**
  * @author psrinivasan
  *         Date: 8/25/12
  *         Time: 9:35 PM
  */
 
 package com.sfdc.http.client;
 
 import com.ning.http.client.*;
 import com.ning.http.client.Cookie;
 import com.sfdc.http.client.filter.ThrottlingRequestFilter;
 import com.sfdc.http.client.filter.ThrottlingResponseFilter;
 import com.sfdc.http.client.handler.GenericAsyncHandler;
 import com.sfdc.http.client.handler.ThrottlingGenericAsyncHandler;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.Semaphore;
 
 public class NingAsyncHttpClientImpl extends com.ning.http.client.AsyncHttpClient {
 
     private static final boolean BLOCKING = false;
     private static final boolean THREADED = false;
     private static final String PUSH_ENDPOINT = "/cometd/25.0";
    private static final int IO_THREAD_MULTIPLIER = 10;
     private static final int MAX_CONNECTIONS_TOTAL = 100000;
     private static final int MAX_CONNECTIONS_PER_HOST = 100000;
     private Semaphore semaphore;
     private static final String ENV_START =
             "<soapenv:Envelope xmlns:soapenv='http://schemas.xmlsoap.org/soap/envelope/' "
                     + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
                     "xmlns:urn='urn:partner.soap.sforce.com'><soapenv:Body>";
     private static final String ENV_END = "</soapenv:Body></soapenv:Envelope>";
 
     /*
      * todo: how does one refactor the ugly boiler plate code that's shared across constructors?
      */
     public NingAsyncHttpClientImpl(Semaphore concurrencyPermit) {
         super(new AsyncHttpClientConfig.Builder()
                 .setIOThreadMultiplier(IO_THREAD_MULTIPLIER)
                 .setMaximumConnectionsTotal(MAX_CONNECTIONS_TOTAL)
                 .setMaximumConnectionsPerHost(MAX_CONNECTIONS_PER_HOST)
                 .addRequestFilter(new ThrottlingRequestFilter())
                         //.setIdleConnectionTimeoutInMs(125000)  // calling this did not help cope with a 120s long poll.
                 .setRequestTimeoutInMs(125000)         //ditto.
                 .addResponseFilter(new ThrottlingResponseFilter(concurrencyPermit))
                 .build());
     }
 
     public NingAsyncHttpClientImpl() {
         super(new AsyncHttpClientConfig.Builder()
                 .setIOThreadMultiplier(IO_THREAD_MULTIPLIER)
                 .setMaximumConnectionsTotal(MAX_CONNECTIONS_TOTAL)
                 .setMaximumConnectionsPerHost(MAX_CONNECTIONS_PER_HOST)
                 .addRequestFilter(new ThrottlingRequestFilter())
                         //.setIdleConnectionTimeoutInMs(125000)  // calling this did not help cope with a 120s long poll.
                 .setRequestTimeoutInMs(125000)         //ditto.
                 .build());
         semaphore = null;
     }
 
     public BoundRequestBuilder prepareQuery(String instance, String soql, String sessionId) {
         StringBuilder builder = new StringBuilder();
         builder.append(instance).append(SfdcConstants.REST_QUERY_URI);
         return prepareGet(builder.toString())
                 .addHeader("Authorization", "Bearer " + sessionId)
                 .addQueryParameter("q", soql);
     }
 
     public Future<Response> soql(String instance, String soql, String sessionId) {
         ListenableFuture<Response> future = null;
         try {
             future = prepareQuery(instance, soql, sessionId).execute(returnAppropriateHandler());
         } catch (IOException e) {
             e.printStackTrace();
         }
         if (BLOCKING) {
             try {
                 future.get();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } catch (ExecutionException e) {
                 e.printStackTrace();
             }
         }
         return future;
     }
 
     public Future<Response> login(String instance, String userName, String password) {
         return login(instance, userName, password, returnAppropriateHandler());
     }
 
     public Future<Response> login(String instance, String userName, String password, AsyncHandler asyncHandler) {
         ListenableFuture<Response> future = null;
         try {
             future = preparePost(instance + SfdcConstants.SERVICES_SOAP_PARTNER_ENDPOINT)
                     .addHeader("Content-Type", "text/xml")
                     .addHeader("SOAPAction", "''")
                     .addHeader("PrettyPrint", "Yes")
                     .setBody(soapXmlForLogin(userName, password))
                     .execute(asyncHandler);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return future;
     }
 
     public Future<Response> streamingHandshake(String instance, String sessionId) {
         return streamingHandshake(instance, sessionId, returnAppropriateHandler());
     }
 
     public Future<Response> streamingHandshake(String instance, String sessionId, AsyncHandler asyncHandler) {
         ListenableFuture<Response> future = null;
 
         try {
             future = preparePost(instance + SfdcConstants.DEFAULT_PUSH_ENDPOINT + SfdcConstants.HANDSHAKE)
                     .addHeader("Authorization", "Bearer " + sessionId)
                     .addHeader("Content-Type", "application/json")
                     .setBody(SfdcConstants.HANDSHAKE_MESSAGE)
                     .execute(asyncHandler);
         } catch (IOException e) {
             e.printStackTrace();
         }
         if (BLOCKING) {
             try {
                 future.get();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } catch (ExecutionException e) {
                 e.printStackTrace();
             }
         }
         return future;
     }
 
     public Future<Response> streamingConnect(String instance, String sessionId, List<Cookie> cookies, String clientId) {
         return streamingConnect(instance, sessionId, cookies, clientId, returnAppropriateHandler());
     }
 
     public Future<Response> streamingConnect(String instance, String sessionId, List<Cookie> cookies, String clientId, AsyncHandler asyncHandler) {
         Future<Response> future = null;
         BoundRequestBuilder requestBuilder = preparePost(instance + SfdcConstants.DEFAULT_PUSH_ENDPOINT + SfdcConstants.CONNECT)
                 .addHeader("Authorization", "Bearer " + sessionId)
                 .addHeader("Content-Type", "application/json")
                 .setBody(SfdcConstants.CONNECT_PREFIX_MESSAGE + clientId + SfdcConstants.CONNECT_POST_MESSAGE);
         for (Cookie cookie : cookies) {
             requestBuilder.addCookie(cookie);
         }
         try {
             future = requestBuilder.execute(asyncHandler);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return future;
     }
 
     public Future<Response> streamingSubscribe(String instance, String sessionId, List<Cookie> cookies, String clientId, String channel) {
         return streamingSubscribe(instance, sessionId, cookies, clientId, channel, returnAppropriateHandler());
     }
 
     public Future<Response> streamingSubscribe(String instance, String sessionId, List<Cookie> cookies, String clientId, String channel, AsyncHandler asyncHandler) {
         ListenableFuture<Response> future = null;
         BoundRequestBuilder requestBuilder = preparePost(instance + SfdcConstants.DEFAULT_PUSH_ENDPOINT + SfdcConstants.SUBSCRIBE)
                 .addHeader("Authorization", "Bearer " + sessionId)
                 .addHeader("Content-Type", "application/json")
                 .setBody(SfdcConstants.SUBSCRIBE_PREFIX_MESSAGE + channel + SfdcConstants.SUBSCRIBE_IN_1_MESSAGE + clientId + SfdcConstants.SUBSCRIBE_POST_MESSAGE);
         for (Cookie cookie : cookies) {
             requestBuilder.addCookie(cookie);
         }
         try {
             future = requestBuilder.execute(asyncHandler);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return future;
     }
 
     public Future<Response> streamingDisconnect(String instance, String sessionId, List<Cookie> cookies, String clientId) {
         return streamingDisconnect(instance, sessionId, cookies, clientId, returnAppropriateHandler());
     }
 
     public Future<Response> streamingDisconnect(String instance, String sessionId, List<Cookie> cookies, String clientId, AsyncHandler asyncHandler) {
         ListenableFuture<Response> future = null;
         BoundRequestBuilder requestBuilder = preparePost(instance + SfdcConstants.DEFAULT_PUSH_ENDPOINT + SfdcConstants.DISCONNECT)
                 .addHeader("Authorization", "Bearer " + sessionId)
                 .addHeader("Content-Type", "application/json")
                 .setBody(SfdcConstants.DISCONNECT_PRE_MESSAGE + clientId + SfdcConstants.DISCONNECT_POST_MESSAGE);
         for (Cookie cookie : cookies) {
             requestBuilder.addCookie(cookie);
         }
         try {
             future = requestBuilder.execute(asyncHandler);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return future;
     }
 
     private AsyncHandler returnAppropriateHandler() {
         return (semaphore == null) ? new GenericAsyncHandler() : new ThrottlingGenericAsyncHandler(semaphore);
     }
 
     private byte[] soapXmlForLogin(String username, String password)
             throws UnsupportedEncodingException {
         return (ENV_START +
                 "  <urn:login>" +
                 "    <urn:username>" + username + "</urn:username>" +
                 "    <urn:password>" + password + "</urn:password>" +
                 "  </urn:login>" +
                 ENV_END).getBytes("UTF-8");
     }
 
     /**
      * @param soapLoginResponse
      * @return string[0] is the session id
      *         string[1] is the instance url
      */
     public String[] getLoginCredentials(String soapLoginResponse) throws IOException, UnsupportedEncodingException, SAXException {
 
         SAXParserFactory spf = SAXParserFactory.newInstance();
         spf.setNamespaceAware(true);
         SAXParser saxParser = null;
         try {
             saxParser = spf.newSAXParser();
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         }
 
         LoginResponseParser parser = new LoginResponseParser();
         saxParser.parse(new ByteArrayInputStream(
                 soapLoginResponse.getBytes("UTF-8")), parser);
 
         if (parser.sessionId == null || parser.serverUrl == null) {
             System.out.println("Login Failed!\n" + soapLoginResponse);
             return null;
         }
         URL soapEndpoint = new URL(parser.serverUrl);
         StringBuilder endpoint = new StringBuilder()
                 .append(soapEndpoint.getProtocol())
                 .append("://")
                 .append(soapEndpoint.getHost());
 
         if (soapEndpoint.getPort() > 0) endpoint.append(":")
                 .append(soapEndpoint.getPort());
         return new String[]{parser.sessionId, endpoint.toString()};
 
     }
 
     private static class LoginResponseParser extends DefaultHandler {
 
         private boolean inSessionId;
         private String sessionId;
 
         private boolean inServerUrl;
         private String serverUrl;
 
         @Override
 
         public void characters(char[] ch, int start, int length) {
             if (inSessionId) sessionId = new String(ch, start, length);
             if (inServerUrl) serverUrl = new String(ch, start, length);
         }
 
         @Override
 
         public void endElement(String uri, String localName, String qName) {
             if (localName != null) {
                 if (localName.equals("sessionId")) {
                     inSessionId = false;
                 }
 
                 if (localName.equals("serverUrl")) {
                     inServerUrl = false;
                 }
             }
         }
 
         @Override
 
         public void startElement(String uri, String localName,
                                  String qName, Attributes attributes) {
             if (localName != null) {
                 if (localName.equals("sessionId")) {
                     inSessionId = true;
                 }
 
                 if (localName.equals("serverUrl")) {
                     inServerUrl = true;
                 }
             }
         }
     }
 }
