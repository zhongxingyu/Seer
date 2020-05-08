 // ========================================================================
 // Copyright 2006-20078 Mort Bay Consulting Pty. Ltd.
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cometd.client;
 
 import com.ning.http.client.AsyncHandler;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.FluentCaseInsensitiveStringsMap;
 import com.ning.http.client.HttpResponseBodyPart;
 import com.ning.http.client.HttpResponseHeaders;
 import com.ning.http.client.HttpResponseStatus;
 import com.ning.http.client.RequestBuilder;
 import com.ning.http.client.Response;
 import com.ning.http.client.logging.LogManager;
 import com.ning.http.client.logging.LoggerProvider;
 import org.cometd.Bayeux;
 import org.cometd.Client;
 import org.cometd.ClientListener;
 import org.cometd.Extension;
 import org.cometd.Message;
 import org.cometd.MessageListener;
 import org.cometd.server.MessageImpl;
 import org.cometd.server.MessagePool;
 import org.eclipse.jetty.client.Address;
 import org.eclipse.jetty.client.CachedExchange;
 import org.eclipse.jetty.client.HttpExchange;
 import org.eclipse.jetty.http.HttpFields;
 import org.eclipse.jetty.http.HttpHeaders;
 import org.eclipse.jetty.http.HttpSchemes;
 import org.eclipse.jetty.http.HttpURI;
 import org.eclipse.jetty.io.Buffer;
 import org.eclipse.jetty.util.ArrayQueue;
 import org.eclipse.jetty.util.LazyList;
 import org.eclipse.jetty.util.QuotedStringTokenizer;
 import org.eclipse.jetty.util.Utf8StringBuffer;
 import org.eclipse.jetty.util.ajax.JSON;
 import org.eclipse.jetty.util.component.AbstractLifeCycle;
 import org.eclipse.jetty.util.log.Log;
 
 import javax.servlet.http.Cookie;
 import java.io.IOException;
 import java.rmi.ConnectException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 /* ------------------------------------------------------------ */
 
 /**
  * Bayeux protocol Client.
  * <p/>
  * Implements a Bayeux Ajax Push client as part of the cometd project.
  * <p/>
  * The HttpClient attributes are used to share a Timer and MessagePool instance
  * between all Bayeux clients sharing the same HttpClient.
  *
  * @author gregw
  * @see <href="http://cometd.org">comed.org</a>
  */
 public class BayeuxClient extends AbstractLifeCycle implements Client {
     private final static String __TIMER = "org.cometd.client.Timer";
     private final static String __JSON = "org.cometd.client.JSON";
     private final static String __MSGPOOL = "org.cometd.server.MessagePool";
 
     private final ArrayQueue<Message> _inQ = new ArrayQueue<Message>();  // queue of incoming messages
     private final ArrayQueue<Message> _outQ = new ArrayQueue<Message>(); // queue of outgoing messages
     private final AsyncHttpClient asyncHttpClient;
     private final Buffer _scheme;
     private final Address _cometdAddress;
     private final String _path;
     private Timer _timer;
     protected MessagePool _msgPool;
     private Exchange _pull;
     private Exchange _push;
     private boolean _initialized = false;
     private boolean _disconnecting = false;
     private String _clientId;
     private org.cometd.Listener _listener;
     private List<MessageListener> _mListeners;
     private int _batch;
     private Map<String, ExpirableCookie> _cookies = new ConcurrentHashMap<String, ExpirableCookie>();
     private Advice _advice;
     private int _backoffInterval = 0;
     private int _backoffIncrement = 1000;
     private int _backoffMaxInterval = 60000;
     private Extension[] _extensions;
     private JSON _jsonOut;
 
     static {
 // Brige AsyncHttpClient logger
         LogManager.setProvider( new LoggerProvider()
         {
 
             public com.ning.http.client.logging.Logger getLogger( final Class<?> clazz )
             {
                 return new com.ning.http.client.logging.Logger()
                 {
 
                     public boolean isDebugEnabled()
                     {
                         return Log.isDebugEnabled();
                     }
 
                     public void debug( final String msg, final Object... msgArgs )
                     {
                         Log.debug( String.format( msg, msgArgs ) );
                     }
 
                     public void debug( final Throwable t )
                     {
                         Log.debug( "", t );
                     }
 
                     public void debug( final Throwable t, final String msg, final Object... msgArgs )
                     {
                         Log.debug( String.format( msg, msgArgs ), t );
                     }
 
                     public void info( final String msg, final Object... msgArgs )
                     {
                         Log.debug( String.format( msg, msgArgs ) );
                     }
 
                     public void info( final Throwable t )
                     {
                         Log.debug( "", t );
                     }
 
                     public void info( final Throwable t, final String msg, final Object... msgArgs )
                     {
                         Log.debug( String.format( msg, msgArgs ), t );
                     }
 
                     public void warn( final String msg, final Object... msgArgs )
                     {
                         Log.debug( String.format( msg, msgArgs ) );
                     }
 
                     public void warn( final Throwable t )
                     {
                         Log.debug( "", t );
                     }
 
                     public void warn( final Throwable t, final String msg, final Object... msgArgs )
                     {
                         Log.debug( String.format( msg, msgArgs ), t );
                     }
 
                     public void error( final String msg, final Object... msgArgs )
                     {
                         Log.debug( String.format( msg, msgArgs ) );
 
                     }
 
                     public void error( final Throwable t )
                     {
                         Log.debug( "", t );
                     }
 
                     public void error( final Throwable t, final String msg, final Object... msgArgs )
                     {
                         Log.debug( String.format( msg, msgArgs ), t );
                     }
                 };
             }
         } );
     }
 
 
     /* ------------------------------------------------------------ */
 
     public BayeuxClient(AsyncHttpClient client, String url) {
         this(client, url, null);
     }
 
     /* ------------------------------------------------------------ */
 
     public BayeuxClient(AsyncHttpClient client, String url, Timer timer) {
         HttpURI uri = new HttpURI(url);
         asyncHttpClient = client;
         _scheme = (HttpSchemes.HTTPS.equals(uri.getScheme())) ? HttpSchemes.HTTPS_BUFFER : HttpSchemes.HTTP_BUFFER;
         _cometdAddress = new Address(uri.getHost(), uri.getPort());
         _path = uri.getPath();
         _timer = timer;
     }
 
     /* ------------------------------------------------------------ */
 
     public BayeuxClient(AsyncHttpClient client, Address address, String path, Timer timer) {
         asyncHttpClient = client;
         _scheme = HttpSchemes.HTTP_BUFFER;
         _cometdAddress = address;
         _path = path;
         _timer = timer;
     }
 
     /* ------------------------------------------------------------ */
 
     public BayeuxClient(AsyncHttpClient client, Address address, String uri) {
         this(client, address, uri, null);
     }
 
     /* ------------------------------------------------------------ */
 
     public void addExtension(Extension ext) {
         _extensions = (Extension[]) LazyList.addToArray(_extensions, ext, Extension.class);
     }
 
     public void removeExtension(Extension ext) {
         _extensions = (Extension[]) LazyList.removeFromArray(_extensions, ext);
     }
 
     /* ------------------------------------------------------------ */
 
     Extension[] getExtensions() {
         return _extensions;
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * If unable to connect/handshake etc, even if following the interval in the
      * advice, wait for this interval initially, and try again.
      *
      * @param interval the time to wait before retrying in milliseconds
      */
     public void setBackOffInterval(int interval) {
         _backoffInterval = interval;
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * @return the backoff interval to wait before retrying an unsuccessful
      *         or failed message
      */
     public int getBackoffInterval() {
         return _backoffInterval;
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * @param retries the max number of retries
      * @deprecated We retry an infinite number of times.
      *             use {@link #getBackoffIncrement()} to set limits
      */
     public void setBackoffMaxRetries(int retries) {
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * @return the max number of retries
      * @deprecated
      */
     public int getBackoffMaxRetries() {
         return -1;
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * . Each retry will increment by this
      * interval, until we reach {@link #getBackoffMaxInterval()}
      *
      * @param interval the interval to increase the backoff, in milliseconds
      */
     public void setBackoffIncrement(int interval) {
         _backoffIncrement = interval;
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * @return the backoff interval used to increase the backoff time when
      *         retrying an unsuccessful or failed message.
      */
     public int getBackoffIncrement() {
         return _backoffIncrement;
     }
 
     /* ------------------------------------------------------------ */
 
     public void setBackoffMaxInterval(int interval) {
         _backoffMaxInterval = interval;
     }
 
     public int getBackoffMaxInterval() {
         return _backoffMaxInterval;
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc) Returns the clientId
      *
      * @see dojox.cometd.Client#getId()
      */
 
     public String getId() {
         return _clientId;
     }
 
     /* ------------------------------------------------------------ */
 
     protected void doStart() throws Exception {
 
         synchronized (asyncHttpClient) {
 
             if (_jsonOut == null) {
                 _jsonOut = new JSON();
             }
 
             if (_timer == null) {
                 _timer = _timer = new Timer(__TIMER + "@" + hashCode(), true);
 
             }
 
             if (_msgPool == null) {
                 _msgPool = new MessagePool();
             }
         }
         _disconnecting = false;
         _pull = null;
         _push = null;
         super.doStart();
         synchronized (_outQ) {
             _outQ.clear();
             if (!_initialized && _pull == null) {
                 _pull = new Handshake();
                 send(_pull, false);
             }
         }
     }
 
     /* ------------------------------------------------------------ */
 
     protected void doStop() throws Exception {
         if (!_disconnecting)
             disconnect();
         super.doStop();
     }
 
     /**
      * Aborts the connection with the server without {@link #disconnect() disconnecting}.
      */
     public void abort() {
         synchronized (_outQ) {
             _outQ.clear();
             _batch = 0;
 
             if (_push != null)
                 _push.cancel();
 
             if (_pull != null)
                 _pull.cancel();
 
             _initialized = false;
             _disconnecting = true;
 
             try {
                 stop();
             }
             catch (Exception x) {
                 Log.ignore(x);
             }
         }
     }
 
     /* ------------------------------------------------------------ */
 
     public boolean isPolling() {
         synchronized (_outQ) {
             return isRunning() && (_pull != null);
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * (non-Javadoc)
      *
      * @param from    The client from which the message arrives or null if no such client can be identified
      * @param message The message to deliver
      */
     public void deliver(Client from, Message message) {
         if (!isRunning())
             throw new IllegalStateException("Not running");
 
         if (_mListeners == null) {
             synchronized (_inQ) {
                 if (message instanceof MessageImpl)
                     ((MessageImpl) message).incRef();
                 _inQ.add(message);
             }
         } else {
             for (MessageListener l : _mListeners)
                 notifyMessageListener(l, from, message);
         }
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#deliver(dojox.cometd.Client, java.lang.String,
      * java.lang.Object, java.lang.String)
      */
 
     public void deliver(Client from, String toChannel, Object data, String id) {
         if (!isRunning())
             throw new IllegalStateException("Not running");
 
         MessageImpl message = _msgPool.newMessage();
 
         message.put(Bayeux.CHANNEL_FIELD, toChannel);
         message.put(Bayeux.DATA_FIELD, data);
         if (id != null)
             message.put(Bayeux.ID_FIELD, id);
         deliver(from, message);
         message.decRef();
     }
 
 
     /* ------------------------------------------------------------ */
 
     private void notifyMessageListener(MessageListener listener, Client from, Message message) {
         try {
             listener.deliver(from, this, message);
         }
         catch (Throwable x) {
             Log.debug(x);
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * @return The listener
      * @deprecated
      */
     public org.cometd.Listener getListener() {
         synchronized (_inQ) {
             return _listener;
         }
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#hasMessages()
      */
 
     public boolean hasMessages() {
         synchronized (_inQ) {
             return _inQ.size() > 0;
         }
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#isLocal()
      */
 
     public boolean isLocal() {
         return false;
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#subscribe(java.lang.String)
      */
 
     private void publish(MessageImpl msg) {
         msg.incRef();
         synchronized (_outQ) {
             _outQ.add(msg);
 
             if (_batch == 0 && _initialized && _push == null) {
                 _push = new Publish();
                 try {
                     send(_push);
                 }
                 catch (IOException e) {
                     metaPublishFail(e, ((Publish) _push).getOutboundMessages());
                 }
                 catch (IllegalStateException e) {
                     metaPublishFail(e, ((Publish) _push).getOutboundMessages());
                 }
             }
         }
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#publish(java.lang.String, java.lang.Object,
      * java.lang.String)
      */
 
     public void publish(String toChannel, Object data, String msgId) {
         if (!isRunning() || _disconnecting)
             throw new IllegalStateException("Not running");
 
         MessageImpl msg = _msgPool.newMessage();
         msg.put(Bayeux.CHANNEL_FIELD, toChannel);
         msg.put(Bayeux.DATA_FIELD, data);
         if (msgId != null)
             msg.put(Bayeux.ID_FIELD, msgId);
         publish(msg);
         msg.decRef();
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#subscribe(java.lang.String)
      */
 
     public void subscribe(String toChannel) {
         if (!isRunning() || _disconnecting)
             throw new IllegalStateException("Not running");
 
         MessageImpl msg = _msgPool.newMessage();
         msg.put(Bayeux.CHANNEL_FIELD, Bayeux.META_SUBSCRIBE);
         msg.put(Bayeux.SUBSCRIPTION_FIELD, toChannel);
         publish(msg);
         msg.decRef();
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#unsubscribe(java.lang.String)
      */
 
     public void unsubscribe(String toChannel) {
         if (!isRunning() || _disconnecting)
             throw new IllegalStateException("Not running");
 
         MessageImpl msg = _msgPool.newMessage();
         msg.put(Bayeux.CHANNEL_FIELD, Bayeux.META_UNSUBSCRIBE);
         msg.put(Bayeux.SUBSCRIPTION_FIELD, toChannel);
         publish(msg);
         msg.decRef();
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Disconnect this client.
      *
      * @deprecated use {@link #disconnect()}
      */
     public void remove() {
         disconnect();
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Disconnect this client.
      */
     public void disconnect() {
         if (isStopped() || _disconnecting)
             throw new IllegalStateException("Not running");
 
         MessageImpl msg = _msgPool.newMessage();
         msg.put(Bayeux.CHANNEL_FIELD, Bayeux.META_DISCONNECT);
 
         synchronized (_outQ) {
             _outQ.add(msg);
             _disconnecting = true;
             if (_batch == 0 && _initialized && _push == null) {
                 _push = new Publish();
                 try {
                     send(_push);
                 }
                 catch (IOException e) {
                     Log.warn(e.toString());
                     Log.debug(e);
                     send(_push, true);
                 }
             }
             _initialized = false;
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * @param listener The listener
      * @deprecated
      */
     public void setListener(org.cometd.Listener listener) {
         synchronized (_inQ) {
             if (_listener != null)
                 removeListener(_listener);
             _listener = listener;
             if (_listener != null)
                 addListener(_listener);
         }
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc) Removes all available messages from the inbound queue. If a
      * listener is set then messages are not queued.
      *
      * @see dojox.cometd.Client#takeMessages()
      */
 
     public List<Message> takeMessages() {
         final LinkedList<Message> list;
         synchronized (_inQ) {
             list = new LinkedList<Message>(_inQ);
             _inQ.clear();
         }
         for (Message m : list)
             if (m instanceof MessageImpl)
                 ((MessageImpl) m).decRef();
         return list;
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#endBatch()
      */
 
     public void endBatch() {
         synchronized (_outQ) {
             if (--_batch <= 0) {
                 _batch = 0;
                 if ((_initialized || _disconnecting) && _push == null && _outQ.size() > 0) {
                     _push = new Publish();
                     try {
                         send(_push);
                     }
                     catch (IOException e) {
                         metaPublishFail(e, ((Publish) _push).getOutboundMessages());
                     }
                 }
             }
         }
     }
 
     /* ------------------------------------------------------------ */
     /*
      * (non-Javadoc)
      *
      * @see dojox.cometd.Client#startBatch()
      */
 
     public void startBatch() {
         if (isStopped())
             throw new IllegalStateException("Not running");
 
         synchronized (_outQ) {
             _batch++;
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Customize an Exchange. Called when an exchange is about to be sent to
      * allow Cookies and Credentials to be customized. Default implementation
      * sets any cookies
      *
      * @param exchange The exchange to customize
      */
     protected void customize(HttpExchange exchange) {
         StringBuilder builder = null;
         for (String cookieName : _cookies.keySet()) {
             if (builder == null)
                 builder = new StringBuilder();
             else
                 builder.append("; ");
 
             // Expiration is handled by getCookie()
             Cookie cookie = getCookie(cookieName);
             if (cookie != null) {
                 builder.append(cookie.getName()); // TODO quotes
                 builder.append("=");
                 builder.append(cookie.getValue()); // TODO quotes
             }
         }
 
         if (builder != null)
             exchange.setRequestHeader(HttpHeaders.COOKIE, builder.toString());
 
         if (_scheme != null)
             exchange.setScheme(_scheme);
     }
 
     /* ------------------------------------------------------------ */
 
     public void setCookie(Cookie cookie) {
         long expirationTime = System.currentTimeMillis();
         int maxAge = cookie.getMaxAge();
         if (maxAge < 0)
             expirationTime = -1L;
         else
             expirationTime += TimeUnit.SECONDS.toMillis(maxAge);
 
         ExpirableCookie expirableCookie = new ExpirableCookie(cookie, expirationTime);
         _cookies.put(cookie.getName(), expirableCookie);
     }
 
     public Cookie getCookie(String name) {
         ExpirableCookie cookie = _cookies.get(name);
         if (cookie != null) {
             if (cookie.isExpired()) {
                 _cookies.remove(name);
                 cookie = null;
             }
         }
         return cookie == null ? null : cookie.cookie;
     }
 
 
     class BayeuxAsyncCompletionHandler implements AsyncHandler {
         private final Collection<HttpResponseBodyPart> bodies =
                 Collections.synchronizedCollection(new ArrayList<HttpResponseBodyPart>());
         private HttpResponseStatus status;
         private HttpResponseHeaders headers;
 
         protected Utf8StringBuffer _responseContent;
         protected int _bufferSize = 1024;
         protected Message[] _responses;
 
         public String getResponseContent() {
             return _responseContent.toString();
         }
 
         public int getBufferSize() {
             return _bufferSize;
         }
 
         public Message[] getResponses() {
             return _responses;
         }
 
         public AsyncHandler.STATE onBodyPartReceived(final HttpResponseBodyPart content) throws Exception {
             bodies.add(content);
             if (_responseContent == null)
                 _responseContent = new Utf8StringBuffer(_bufferSize);
 
             _responseContent.append(content.getBodyPartBytes(), 0, content.getBodyPartBytes().length);
             return AsyncHandler.STATE.CONTINUE;
         }
 
         public final AsyncHandler.STATE onStatusReceived(final HttpResponseStatus status) throws Exception {
             this.status = status;
             _responseContent = null;
             return AsyncHandler.STATE.CONTINUE;
         }
 
         public final AsyncHandler.STATE onHeadersReceived(final HttpResponseHeaders headers) throws Exception {
             this.headers = headers;
 
             FluentCaseInsensitiveStringsMap h = headers.getHeaders();
             for (String headerName : h.keySet()) {
                 if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                     _bufferSize = Integer.valueOf(h.getFirstValue(headerName));
                 } else if (headerName.equalsIgnoreCase(HttpHeaders.SET_COOKIE)) {
                     String cname = null;
                     String cvalue = null;
 
                     QuotedStringTokenizer tok = new QuotedStringTokenizer(h.getFirstValue(headerName).toString(), "=;", false, false);
                     tok.setSingle(false);
 
                     if (tok.hasMoreElements())
                         cname = tok.nextToken();
                     if (tok.hasMoreElements())
                         cvalue = tok.nextToken();
 
                     Cookie cookie = new Cookie(cname, cvalue);
 
                     while (tok.hasMoreTokens()) {
                         String token = tok.nextToken();
                         if ("Version".equalsIgnoreCase(token))
                             cookie.setVersion(Integer.parseInt(tok.nextToken()));
                         else if ("Comment".equalsIgnoreCase(token))
                             cookie.setComment(tok.nextToken());
                         else if ("Path".equalsIgnoreCase(token))
                             cookie.setPath(tok.nextToken());
                         else if ("Domain".equalsIgnoreCase(token))
                             cookie.setDomain(tok.nextToken());
                         else if ("Expires".equalsIgnoreCase(token)) {
                             try {
                                 Date date = new SimpleDateFormat("EEE, dd-MMM-yy HH:mm:ss 'GMT'").parse(tok.nextToken());
                                 Long maxAge = TimeUnit.MILLISECONDS.toSeconds(date.getTime() - System.currentTimeMillis());
                                 cookie.setMaxAge(maxAge > 0 ? maxAge.intValue() : 0);
                             }
                             catch (ParseException ignored) {
                             }
                         } else if ("Max-Age".equalsIgnoreCase(token)) {
                             try {
                                 int maxAge = Integer.parseInt(tok.nextToken());
                                 cookie.setMaxAge(maxAge);
                             }
                             catch (NumberFormatException ignored) {
                             }
                         } else if ("Secure".equalsIgnoreCase(token))
                             cookie.setSecure(true);
                     }
 
                     BayeuxClient.this.setCookie(cookie);
                 }
             }
 
             return AsyncHandler.STATE.CONTINUE;
         }
 
 
         public Response onCompleted() throws Exception {
 
             Response response = status.provider().prepareResponse(status, headers, bodies);
             bodies.clear();
             if (status.getStatusCode() == 200) {
                 String content = getResponseContent();
                 if (content == null || content.length() == 0)
                     throw new IllegalStateException("No content in response for " + response.getUri());
                 _responses = _msgPool.parse(content);
 
                 if (_responses != null)
                     for (int i = 0; i < _responses.length; i++)
                         extendIn(_responses[i]);
             }
 
             return response;
         }
 
         public void onThrowable(Throwable t) {
             bodies.clear();
         }
 
         public AsyncHandler.STATE onHeaderWriteCompleted() {
             return AsyncHandler.STATE.CONTINUE;
         }
 
 
         public AsyncHandler.STATE onContentWriteCompleted() {
             return AsyncHandler.STATE.CONTINUE;
         }
 
 
         public AsyncHandler.STATE onContentWriteProgess(long amount, long current, long total) {
             return AsyncHandler.STATE.CONTINUE;
         }
 
         public void setResponses(Message[] _responses) {
             this._responses = _responses;
         }
 
         public synchronized int getResponseStatus()
         {
             if (status == null)
                 throw new IllegalStateException("Response not received yet");
             return status.getStatusCode();
         }
 
         public synchronized HttpFields getResponseFields()
         {
             if (status == null)
                 throw new IllegalStateException("Headers not completely received yet");
 
 
             HttpFields f = new HttpFields();
             FluentCaseInsensitiveStringsMap h = headers.getHeaders();
             for (String headerName : h.keySet()) {
                 f.put(headerName, h.getFirstValue(headerName));
             }        
             return f;
         }
     }
 
 
     /* ------------------------------------------------------------ */
     /* ------------------------------------------------------------ */
     /* ------------------------------------------------------------ */
 
     /**
      * The base class for all bayeux exchanges.
      */
     protected class Exchange extends CachedExchange {
         int _backoff = _backoffInterval;
         String _json;
 
         protected final RequestBuilder requestBuilder;
         protected BayeuxAsyncCompletionHandler asyncHandler;
 
         /* ------------------------------------------------------------ */
 
         Exchange(String info) {
             super(false);
             setMethod("POST");
             setScheme(HttpSchemes.HTTP_BUFFER);
             setAddress(_cometdAddress);
             setURI(_path + "/" + info);
             setRequestContentType(Bayeux.JSON_CONTENT_TYPE);
 
             requestBuilder = new RequestBuilder("POST").setUrl(getScheme() + "://" + getAddress() + getURI())
                     .setHeader("Content-Type", Bayeux.JSON_CONTENT_TYPE);
             asyncHandler = new BayeuxAsyncCompletionHandler();
         }
 
         @Override
         protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException {
             // NOT USED  -- See BayeuxAsyncCompletionHandler
         }
 
         @Override
         protected void onResponseContent(Buffer content) throws IOException {
             // NOT USED  -- See BayeuxAsyncCompletionHandler
         }
 
         /* ------------------------------------------------------------ */
 
         public int getBackoff() {
             return _backoff;
         }
 
         /* ------------------------------------------------------------ */
 
         public void incBackoff() {
             _backoff = Math.min(_backoff + _backoffIncrement, _backoffMaxInterval);
         }
 
         /* ------------------------------------------------------------ */
 
         protected void setMessage(String message) {
             setJson(extendOut(message));
         }
 
         /* ------------------------------------------------------------ */
 
         protected void setJson(String json) {
             try {
                 _json = json;
             }
             catch (Exception e) {
                 Log.ignore(e);
             }
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onResponseHeader(Buffer name, Buffer value) throws IOException {
             // NOT USED  -- See BayeuxAsyncCompletionHandler
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onResponseComplete() throws IOException {
             // NOT USED  -- See BayeuxAsyncCompletionHandler
         }
 
         /* ------------------------------------------------------------ */
 
         protected void resend(boolean backoff) {
             if (!isRunning())
                 return;
 
             final boolean disconnecting;
             synchronized (_outQ) {
                 disconnecting = _disconnecting;
             }
             if (disconnecting) {
                 try {
                     stop();
                 } catch (Exception e) {
                     Log.ignore(e);
                 }
                 return;
             }
 
             setJson(_json);
             if (!send(this, backoff))
                 Log.warn("Retries exhausted"); // giving up
         }
 
         /* ------------------------------------------------------------ */
 
         protected void recycle() {
             Message[] _responses = asyncHandler.getResponses();
             if (_responses != null)
                 for (Message msg : _responses)
                     if (msg instanceof MessageImpl)
                         ((MessageImpl) msg).decRef();
             asyncHandler.setResponses(null);
         }
 
         public RequestBuilder getRequestBuilder() {
             requestBuilder.setBody(_json);
             return requestBuilder;
         }
 
         public BayeuxAsyncCompletionHandler getAsyncHandler() {
             return asyncHandler;
         }
 
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * The Bayeux handshake exchange. Negotiates a client Id and initializes the
      * protocol.
      */
     protected class Handshake extends Exchange {
         public final static String __HANDSHAKE = "[{" + "\"channel\":\"/meta/handshake\"," + "\"version\":\"0.9\"," + "\"minimumVersion\":\"0.9\"" + "}]";
 
         Handshake() {
             super("handshake");
             setMessage(__HANDSHAKE);
             asyncHandler = new BayeuxAsyncCompletionHandler() {
 
                 @Override
                 public final Response onCompleted() throws Exception {
                     Response r = super.onCompleted();
                     if (_disconnecting) {
                         Message error = _msgPool.newMessage();
                         error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
                         error.put("failure", "expired");
                         metaHandshake(false, false, error);
                         try {
                             stop();
                         } catch (Exception e) {
                             Log.ignore(e);
                         }
                         return r;
                     }
 
                     if (r.getStatusCode() == 200 && _responses != null && _responses.length > 0) {
                         MessageImpl response = (MessageImpl) _responses[0];
                         boolean successful = response.isSuccessful();
 
                         // Get advice if there is any
                         Map adviceField = (Map) response.get(Bayeux.ADVICE_FIELD);
                         if (adviceField != null)
                             _advice = new Advice(adviceField);
 
                         if (successful) {
                             if (Log.isDebugEnabled())
                                 Log.debug("Successful handshake, sending connect");
                             _clientId = (String) response.get(Bayeux.CLIENT_FIELD);
 
                             metaHandshake(true, true, response);
                             _pull = new Connect();
                             send(_pull, false);
                         } else {
                             metaHandshake(false, false, response);
                             if (_advice != null && _advice.isReconnectNone())
                                 throw new IOException("Handshake failed with advice reconnect=none :" + _responses[0]);
                             else if (_advice != null && _advice.isReconnectHandshake()) {
                                 _pull = new Handshake();
                                 if (!send(_pull, true))
                                     throw new IOException("Handshake, retries exhausted");
                             } else
                             // assume retry = reconnect?
                             {
                                 _pull = new Connect();
                                 if (!send(_pull, true))
                                     throw new IOException("Connect after handshake, retries exhausted");
                             }
                         }
                     } else {
                         Message error = _msgPool.newMessage();
                         error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
                         error.put("status", getResponseStatus());
                         error.put("content", getResponseContent());
 
                         metaHandshake(false, false, error);
                         resend(true);
                     }
 
                     recycle();
                     return r;
                 }
 
                 @Override
                 public void onThrowable(Throwable t) {
                     super.onThrowable(t);
                     if (ConnectException.class.isAssignableFrom(t.getClass())) {
                         onConnectionFailed(t);
                     } else if (TimeoutException.class.isAssignableFrom(t.getClass())) {
                         onExpire();
                     } else {
                         onException(t);
                     }
                 }
             };
         }
 
         /* ------------------------------------------------------------ */
         /*
          * (non-Javadoc)
          *
          * @see
          * org.cometd.client.BayeuxClient.Exchange#onResponseComplete()
          */
 
         protected void onResponseComplete() throws IOException {
             // NOT USED  -- See BayeuxAsyncCompletionHandler
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onExpire() {
             Message error = _msgPool.newMessage();
             error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
             error.put("failure", "expired");
             error.put("uri", getURI());
             metaHandshake(false, false, error);
             resend(true);
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onConnectionFailed(Throwable ex) {
             Message error = _msgPool.newMessage();
             error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
             error.put("failure", ex.toString());
             error.put("exception", ex);
             error.put("uri", getURI());
             ex.printStackTrace();
             metaHandshake(false, false, error);
             resend(true);
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onException(Throwable ex) {
             Message error = _msgPool.newMessage();
             error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
             error.put("failure", getURI());
             error.put("exception", ex);
             error.put("uri", getURI());
             metaHandshake(false, false, error);
             resend(true);
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * The Bayeux Connect exchange. Connect exchanges implement the long poll
      * for Bayeux.
      */
     protected class Connect extends Exchange {
         String _connectString;
 
         Connect() {
             super("connect");
             _connectString = "[{" + "\"channel\":\"/meta/connect\"," + "\"clientId\":\"" + _clientId + "\"," + "\"connectionType\":\"long-polling\"" + "}]";
             setMessage(_connectString);
 
             asyncHandler = new BayeuxAsyncCompletionHandler() {
 
                 @Override                
                 public final Response onCompleted() throws Exception {
                     Response r = super.onCompleted();
 
                     if (r.getStatusCode() == 200 && _responses != null && _responses.length > 0) {
                         try {
                             startBatch();
 
                             for (int i = 0; i < _responses.length; i++) {
                                 Message msg = _responses[i];
 
                                 // get advice if there is any
                                 Map adviceField = (Map) msg.get(Bayeux.ADVICE_FIELD);
                                 if (adviceField != null)
                                     _advice = new Advice(adviceField);
 
                                 if (Bayeux.META_CONNECT.equals(msg.get(Bayeux.CHANNEL_FIELD))) {
                                     Boolean successful = (Boolean) msg.get(Bayeux.SUCCESSFUL_FIELD);
                                     if (successful != null && successful) {
                                         metaConnect(true, msg);
 
                                         if (!isRunning())
                                             break;
 
                                         synchronized (_outQ) {
                                             if (_disconnecting)
                                                 continue;
 
                                             if (!isInitialized()) {
                                                 setInitialized(true);
                                                 {
                                                     if (_outQ.size() > 0) {
                                                         _push = new Publish();
                                                         send(_push);
                                                     }
                                                 }
                                             }
 
                                         }
                                         // send a Connect (ie longpoll) possibly with
                                         // delay according to interval advice
                                         _pull = new Connect();
                                         send(_pull, false);
                                     } else {
                                         // received a failure to our connect message,
                                         // check the advice to see what to do:
                                         // reconnect: none = hard error
                                         // reconnect: handshake = send a handshake
                                         // message
                                         // reconnect: retry = send another connect,
                                         // possibly using interval
 
                                         setInitialized(false);
                                         metaConnect(false, msg);
 
                                         synchronized (_outQ) {
                                             if (!isRunning() || _disconnecting)
                                                 break;
                                         }
 
                                         if (_advice != null && _advice.isReconnectNone())
                                             throw new IOException("Connect failed, advice reconnect=none");
                                         else if (_advice != null && _advice.isReconnectHandshake()) {
                                             if (Log.isDebugEnabled())
                                                 Log.debug("connect received success=false, advice is to rehandshake");
                                             _pull = new Handshake();
                                             send(_pull, true);
                                         } else {
                                             // assume retry = reconnect
                                             if (Log.isDebugEnabled())
                                                 Log.debug("Assuming retry=reconnect");
                                             resend(true);
                                         }
                                     }
                                 }
 
                                 // It may happen that a message arrives just after a stop() or abort()
                                 // It is not fool proof, but reduces greatly the window of possibility
                                 if (isRunning())
                                     deliver(null, msg);
                             }
                         }
                         finally {
                             endBatch();
                         }
                     } else {
                         Message error = _msgPool.newMessage();
                         error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
                         error.put("status", getResponseStatus());
                         error.put("content", getResponseContent());
                         metaConnect(false, error);
                         resend(true);
                     }
 
                     recycle();
                     return r;
                 }
 
                 @Override
                 public void onThrowable(Throwable t) {
                     super.onThrowable(t);
                     if (ConnectException.class.isAssignableFrom(t.getClass())) {
                         onConnectionFailed(t);
                     } else if (TimeoutException.class.isAssignableFrom(t.getClass())) {
                         onExpire();
                     } else {
                         onException(t);
                     }
                 }
 
             };
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onResponseComplete() throws IOException {
             // NOT USED  -- See BayeuxAsyncCompletionHandler
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onExpire() {
             setInitialized(false);
             Message error = _msgPool.newMessage();
             error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
             error.put("failure", "expired");
             metaConnect(false, error);
             resend(true);
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onConnectionFailed(Throwable ex) {
             setInitialized(false);
             Message error = _msgPool.newMessage();
             error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
             error.put("failure", ex.toString());
             error.put("exception", ex);
             metaConnect(false, error);
             resend(true);
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onException(Throwable ex) {
             setInitialized(false);
             Message error = _msgPool.newMessage();
             error.put(Bayeux.SUCCESSFUL_FIELD, Boolean.FALSE);
             error.put("failure", ex.toString());
             error.put("exception", ex);
             metaConnect(false, error);
             resend(true);
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Publish message exchange. Sends messages to bayeux server and handles any
      * messages received as a result.
      */
     protected class Publish extends Exchange {
         Publish() {
             super("publish");
 
             StringBuffer json = new StringBuffer(256);
             synchronized (_outQ) {
                 int s = _outQ.size();
                 if (s == 0)
                     return;
 
                 for (int i = 0; i < s; i++) {
                     Message message = _outQ.getUnsafe(i);
                     message.put(Bayeux.CLIENT_FIELD, _clientId);
                     extendOut(message);
 
                     json.append(i == 0 ? '[' : ',');
                     _jsonOut.append(json, message);
 
                     if (message instanceof MessageImpl)
                         ((MessageImpl) message).decRef();
                 }
                 json.append(']');
                 _outQ.clear();
                 setJson(json.toString());
             }
             asyncHandler = new BayeuxAsyncCompletionHandler() {
 
                 public final Response onCompleted() throws Exception {
                     Response r = super.onCompleted();
 
                     try {
                         synchronized (_outQ) {
                             startBatch();
                             _push = null;
                         }
 
                         if (r.getStatusCode() == 200 && _responses != null && _responses.length > 0) {
                             for (int i = 0; i < _responses.length; i++) {
                                 MessageImpl msg = (MessageImpl) _responses[i];
 
                                 deliver(null, msg);
                                 if (Bayeux.META_DISCONNECT.equals(msg.getChannel()) && msg.isSuccessful()) {
                                     if (isStarted()) {
                                         try {
                                             stop();
                                         } catch (Exception e) {
                                             Log.ignore(e);
                                         }
                                     }
                                     break;
                                 }
                             }
                         } else {
                             Log.warn("Publish, error=" + getResponseStatus());
                         }
                     }
                     finally {
                         endBatch();
                     }
                     recycle();
                     return r;
                 }
 
                 @Override
                 public void onThrowable(Throwable t) {
                    super.onThrowable(t);                    
                     if (ConnectException.class.isAssignableFrom(t.getClass())) {
                         onConnectionFailed(t);
                     } else if (TimeoutException.class.isAssignableFrom(t.getClass())) {
                         onExpire();
                     } else {
                         onException(t);
                     }
                 }
             };
         }
 
         protected Message[] getOutboundMessages() {
             try {
                 return _msgPool.parse(_json);
             }
             catch (IOException e) {
                 Log.warn("Error converting outbound messages");
                 if (Log.isDebugEnabled())
                     Log.debug(e);
                 return null;
             }
         }
 
         /* ------------------------------------------------------------ */
         /*
          * (non-Javadoc)
          *
          * @see
          * org.cometd.client.BayeuxClient.Exchange#onResponseComplete()
          */
 
         protected void onResponseComplete() throws IOException {
             // NOT USED  -- See BayeuxAsyncCompletionHandler
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onExpire() {
             super.onExpire();
             metaPublishFail(null, this.getOutboundMessages());
             if (_disconnecting) {
                 try {
                     stop();
                 } catch (Exception e) {
                     Log.ignore(e);
                 }
             }
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onConnectionFailed(Throwable ex) {
             super.onConnectionFailed(ex);
             metaPublishFail(ex, this.getOutboundMessages());
             if (_disconnecting) {
                 try {
                     stop();
                 } catch (Exception e) {
                     Log.ignore(e);
                 }
             }
         }
 
         /* ------------------------------------------------------------ */
 
         protected void onException(Throwable ex) {
             super.onException(ex);
             metaPublishFail(ex, this.getOutboundMessages());
             if (_disconnecting) {
                 try {
                     stop();
                 } catch (Exception e) {
                     Log.ignore(e);
                 }
             }
         }
     }
 
     /* ------------------------------------------------------------ */
 
     public void addListener(ClientListener listener) {
         boolean added = false;
         if (listener instanceof MessageListener) {
             added = true;
             if (_mListeners == null)
                 _mListeners = new CopyOnWriteArrayList<MessageListener>();
             _mListeners.add((MessageListener) listener);
         }
 
         if (!added)
             throw new IllegalArgumentException();
     }
 
     /* ------------------------------------------------------------ */
 
     public void removeListener(ClientListener listener) {
         if (listener instanceof MessageListener) {
             if (_mListeners != null)
                 _mListeners.remove((MessageListener) listener);
         }
     }
 
     /* ------------------------------------------------------------ */
 
     public int getMaxQueue() {
         return -1;
     }
 
     /* ------------------------------------------------------------ */
 
     public Queue<Message> getQueue() {
         return _inQ;
     }
 
     /* ------------------------------------------------------------ */
 
     public void setMaxQueue(int max) {
         if (max != -1)
             throw new UnsupportedOperationException();
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Send the exchange, possibly using a backoff.
      *
      * @param exchange The exchange to send
      * @param backoff  if true, use backoff algorithm to send
      * @return whether the exchange has been sent or not
      */
     protected boolean send(final Exchange exchange, final boolean backoff) {
         long interval = (_advice != null ? _advice.getInterval() : 0);
 
         if (backoff) {
             int backoffInterval = exchange.getBackoff();
             interval += backoffInterval;
             exchange.incBackoff();
 
             if (Log.isDebugEnabled())
                 Log.debug("Send with backoff, interval=" + interval + " for " + exchange);
         }
 
         if (interval > 0) {
             TimerTask task = new TimerTask() {
                 public void run() {
                     try {
                         send(exchange);
                     }
                     catch (IOException e) {
                         Log.warn("Delayed send, retry: " + e);
                         Log.debug(e);
                         send(exchange, true);
                     }
                     catch (IllegalStateException e) {
                         Log.warn("Delayed send, retry: " + e);
                         Log.debug(e);
                         send(exchange, true);
                     }
                 }
             };
             if (Log.isDebugEnabled())
                 Log.debug("Delay " + interval + " send of " + exchange);
             _timer.schedule(task, interval);
         } else {
             try {
                 send(exchange);
             }
             catch (IOException e) {
                 Log.warn("Send, retry on fail: " + e);
                 Log.debug(e);
                 return send(exchange, true);
             }
             catch (IllegalStateException e) {
                 Log.warn("Send, retry on fail: " + e);
                 Log.debug(e);
                 return send(exchange, true);
             }
         }
         return true;
 
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Send the exchange.
      *
      * @param exchange The exchange to send
      * @throws IOException If the send fails
      */
     protected void send(Exchange exchange) throws IOException {
         exchange.reset(); // ensure at start state
         customize(exchange);
         if (isRunning()) {
             if (Log.isDebugEnabled())
                 Log.debug("Send: using any connection=" + exchange);
             asyncHttpClient.executeRequest(exchange.getRequestBuilder().build(),
                     exchange.getAsyncHandler()); // use any connection
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * False when we have received a success=false message in response to a
      * Connect, or we have had an exception when sending or receiving a Connect.
      * <p/>
      * True when handshake and then connect has happened.
      *
      * @param value the value for initialized
      */
     protected void setInitialized(boolean value) {
         synchronized (_outQ) {
             _initialized = value;
         }
     }
 
     /* ------------------------------------------------------------ */
 
     protected boolean isInitialized() {
         return _initialized;
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Called with the results of a /meta/connect message
      *
      * @param success Whether the connect response is successful
      * @param message The connect response
      */
     protected void metaConnect(boolean success, Message message) {
         if (!success)
             Log.warn(this.toString() + ": connect failed, " + message.toString());
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Called with the results of a /meta/handshake message
      *
      * @param success     Whether the handshake response is successful
      * @param reestablish Whether the client was previously connected
      * @param message     The handshake response
      */
     protected void metaHandshake(boolean success, boolean reestablish, Message message) {
         if (!success)
             Log.warn(this.toString() + ": handshake failed, " + message.toString());
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Called with the results of a failed publish
      *
      * @param e        The exception that caused the failure
      * @param messages The messages that could not be sent
      */
     protected void metaPublishFail(Throwable e, Message[] messages) {
         Log.warn(this.toString() + ": publish failed, " + Arrays.toString(messages));
         Log.debug(e);
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Called to extend outbound string messages.
      * Some messages are sent as preformatted JSON strings (eg handshake
      * and connect messages).  This extendOut method is a variation of the
      * {@link #extendOut(Message)} method to efficiently cater for these
      * preformatted strings.
      * <p/>
      * This method calls the {@link Extension}s added by {@link #addExtension(Extension)}
      *
      * @param msg The message to pass through extensions
      * @return the extended message
      */
     protected String extendOut(String msg) {
         if (_extensions == null)
             return msg;
 
         try {
             Message[] messages = _msgPool.parse(msg);
             for (int i = 0; i < messages.length; i++)
                 extendOut(messages[i]);
             if (messages.length == 1 && msg.charAt(0) == '{')
                 return _msgPool.getMsgJSON().toJSON(messages[0]);
             return _msgPool.getMsgJSON().toJSON(messages);
         }
         catch (IOException e) {
             Log.warn(e);
             return msg;
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Called to extend outbound messages
      * <p/>
      * This method calls the {@link Extension}s added by {@link #addExtension(Extension)}
      *
      * @param message The message to pass through the extensions
      */
     protected void extendOut(Message message) {
         if (_extensions != null) {
             Message m = message;
             String channelId = m.getChannel();
             if (channelId != null) {
                 if (channelId.startsWith(Bayeux.META_SLASH))
                     for (int i = 0; m != null && i < _extensions.length; i++)
                         m = _extensions[i].sendMeta(this, m);
                 else
                     for (int i = 0; m != null && i < _extensions.length; i++)
                         m = _extensions[i].send(this, m);
             }
 
             if (message != m) {
                 message.clear();
                 if (m != null)
                     for (Map.Entry<String, Object> entry : m.entrySet())
                         message.put(entry.getKey(), entry.getValue());
             }
         }
     }
 
     /* ------------------------------------------------------------ */
 
     /**
      * Called to extend inbound messages
      * <p/>
      * This method calls the {@link Extension}s added by {@link #addExtension(Extension)}
      *
      * @param message The message to pass through the extensions
      */
     protected void extendIn(Message message) {
         if (_extensions != null) {
             Message m = message;
             String channelId = m.getChannel();
             if (channelId != null) {
                 if (channelId.startsWith(Bayeux.META_SLASH))
                     for (int i = _extensions.length; m != null && i-- > 0;)
                         m = _extensions[i].rcvMeta(this, m);
                 else
                     for (int i = _extensions.length; m != null && i-- > 0;)
                         m = _extensions[i].rcv(this, m);
             }
 
             if (message != m) {
                 message.clear();
                 if (m != null)
                     for (Map.Entry<String, Object> entry : m.entrySet())
                         message.put(entry.getKey(), entry.getValue());
             }
         }
     }
 
     private static class ExpirableCookie {
         private final Cookie cookie;
         private final long expirationTime;
 
         private ExpirableCookie(Cookie cookie, long expirationTime) {
             this.cookie = cookie;
             this.expirationTime = expirationTime;
         }
 
         private boolean isExpired() {
             if (expirationTime < 0) return false;
             return System.currentTimeMillis() >= expirationTime;
         }
     }
 
 
 }
