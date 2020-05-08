 // Copyright 2005-2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.net.http;
 
 import static org.waterken.io.Stream.chunkSize;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.util.LinkedList;
 
 import org.joe_e.Powerless;
 import org.joe_e.inert;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.charset.ASCII;
 import org.joe_e.var.Milestone;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Receiver;
 import org.waterken.http.Client;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 import org.waterken.http.Server;
 import org.waterken.http.TokenList;
 import org.waterken.io.Stream;
 import org.waterken.io.bounded.Bounded;
 import org.waterken.io.limited.Limited;
 import org.waterken.io.open.Open;
 import org.waterken.net.Locator;
 import org.waterken.uri.Header;
 
 /**
  * The client side of the HTTP protocol.
  * <p>
  * The connection to the remote server will be automatically retried until a non
  * 5xx HTTP response is received for each pending HTTP request. The responses
  * are resolved in the same order as the requests were registered.
  * </p>
  */
 public final class
 ClientSide implements Server {
     
     /**
      * The minimum milliseconds to wait before retrying a connection: {@value}
      * <p>
      * This value reflects the amount of time for a network hiccup to sort
      * itself out. 
      * </p>
      */
     static private final long minSleep = 60 * 1000;
     
     /**
      * The maximum milliseconds to wait before retrying a connection: {@value}
      * <p>
      * This value reflects the amount of time expected for an administrator to
      * intervene.
      * </p>
      */
     static private final long maxSleep = 60 * 60 * 1000;
     
     /**
      * Indicates a configuration error on the remote server.
      */
     static private final class
     Nap extends IOException implements Powerless {
         static private final long serialVersionUID = 1L;
 
         protected
         Nap(final String message) {
             super(message);
         }
     }
 
     /**
      * An operation on the connection's output stream.
      */
     static public interface
     Outbound extends Promise<Void> {}
     
     /**
      * An operation on the connection's input stream.
      */
     static public interface
     Inbound extends Promise<Void> {}
     
     private final String location;
     private final Locator locator;
     private final Receiver<Long> sleep;
     private final Receiver<Outbound> sender;
     private final Receiver<Inbound> receiver;
 
     private
     ClientSide(final String location, final Locator locator,
                final Receiver<Long> sleep, final Receiver<Outbound> sender,
                final Receiver<Inbound> receiver) {
         this.location = location;
         this.locator = locator;
         this.sleep = sleep;
         this.sender = sender;
         this.receiver = receiver;
     }
     
     /*
      * This implementation uses a pair of communicating event loops: the sender
      * event loop and the receiver event loop. Outgoing HTTP requests are put on
      * the wire in the sender event loop. Incoming HTTP responses are taken off
      * the wire in the receiver event loop. Connection initiation is done from
      * the sender event loop. The queue of pending request/response exchanges is
      * owned by the sender event loop. For each pending exchange, a send task is
      * posted to the sender event loop. Whether the send task successfully sends
      * its request or not, it will put a corresponding receive task on the
      * receiver event loop. When the receive task is run, it will pull the HTTP
      * response off the wire, and deliver it to the provided response resolver.
      * A task to pop the exchange from the pending exchange queue is then queued
      * on the sender event loop. Any I/O error with the connection is handled by
      * a receive task. The receive task will close down the socket, mark the
      * current connection as dead, and queue a connection initiation task on the
      * sender event loop, whereupon we start the process from scratch.
      */
     
     class Connection implements Outbound {
         private         SocketAddress mostRecent;
         private         Outbound retry;
         
         private         Socket socket;
         protected       InputStream in;
         protected       OutputStream out;
         
         Connection(@inert final SocketAddress mostRecent,
                    @inert final Outbound retry) {
             this.mostRecent = mostRecent;
             this.retry = retry;
         }
         
         public Void
         call() {
             for (long a = 0, b = minSleep; true;) {
                 try {
                     socket = locator.locate(location, mostRecent);
                     in = socket.getInputStream();
                     out = socket.getOutputStream();
                     break;
                 } catch (final IOException e) {
                     mostRecent = null;
                     if (null != socket) {
                         try { socket.close(); } catch (final IOException e2) {}
                         socket = null;
                     }
                     in = null;
                     out = null;
 
                     // wait and try again
                     if (b < maxSleep) {
                         final long c = a + b;
                         a = b;
                         b = c;
                         b = Math.min(b, maxSleep);
                     }
                     sleep.run(b);
                 }
             }
             mostRecent = null;
             return null;
         }
         
         protected void
         retry() {
             in = null;
             out = null;
             sender.run(retry);
             retry = null;
             try { socket.close(); } catch (final Exception e) {}
             socket = null;
         }
     }
     class Exchange extends Client {
         protected final Request head;
         protected final InputStream body;
         private   final Client client;
         private   final Outbound pop;
         
         Exchange(@inert final Request head,
                  @inert final InputStream body,
                  @inert final Client client,
                  @inert final Outbound pop) {
             this.head = head;
             this.body = body;
             this.client = client;
             this.pop = pop;
         }
         
         public void
         receive(final Response head, final InputStream body) throws Exception {
             client.receive(head, body); // don't pop if there is an I/O error
             sender.run(pop);
         }
     }
     class Receive implements Inbound {
         private final Connection on;
         private final Exchange x;
         
         Receive(@inert final Connection on, @inert final Exchange x) {
             this.on = on;
             this.x = x; 
         }
         
         public Void
         call() throws Exception {
             /*
              * Only proceed if the connection is still active. Once a response
              * has failed, no more can be received. The first failure is
              * responsible for scheduling the connection retry.
              */ 
             if (null == on.in) { return null; }
             try {
                 if (null == x.head) {
                     x.receive(Response.badRequest(), null);
                 } else {
                     if (receive(x.head.method, on.in, x)) { on.retry(); }
                 }
             } catch (final Exception e) {
                 if (e instanceof Nap) {
                     sender.run(new Outbound() {
                         public Void
                         call() throws Exception {
                             sleep.run(maxSleep);
                             return null;
                         }
                     });
                 }
                 on.retry();
                 if (SocketException.class != e.getClass()) { throw e; }
             }
             return null;
         }
     }
     class Send implements Outbound {
         private final Connection on;
         private final Exchange x;
         
         Send(@inert final Connection on, @inert final Exchange x) {
             this.on = on;
             this.x = x;
         }
         
         public Void
         call() throws Exception {
             receiver.run(new Receive(on, x));
             if (null == x.head) {
                 // nothing to send since request failed to render
             } else {
                 try {
                     send(x.head, x.body, on.out);
                 } catch (final Exception e) {
                     final OutputStream tmp = on.out;
                     on.out = null;
                     try { tmp.close(); } catch (final Exception e2) {}
                     if (SocketException.class != e.getClass()) { throw e; }
                 }
             }
             return null;
         }
     }
     class Retry implements Outbound {
         private       SocketAddress mostRecent = null;
         private       Connection current = null;
         private final LinkedList<Exchange> pending = new LinkedList<Exchange>();
         
         public Void
         call() {
             current = new Connection(mostRecent, this);
             sender.run(current);
             sender.run(new Outbound() {
                 public Void
                 call() {
                     mostRecent = current.socket.getRemoteSocketAddress();
                     return null;
                 }
             });
             for (final Exchange x : pending) {sender.run(new Send(current, x));}
             return null;
         }
         
         protected void
         enqueue(final Request head,final InputStream body, final Client client){
             if (null != body) {
                 // ensure request body can be replayed
                 body.mark(Integer.MAX_VALUE);
             }
             final Exchange x = new Exchange(head, body, client, new Outbound() {
                 public Void
                 call() {
                     pending.removeFirst();
                     return null;
                 }
             });
             sender.run(new Outbound() {
                 public Void
                 call() {
                     pending.addLast(x);
                     sender.run(new Send(current, x));
                     return null;
                 }
             });
         }
     }
     
     private Retry entry;
     
     void
     start() {
         entry = new Retry();
         sender.run(entry);
     }
     
     /**
      * Constructs an instance.
      * <p>
      * Each response block will be invoked from the receiver event loop.
      * </p>
      * @param location  URL identifying the remote host  
      * @param locator   socket factory
      * @param sleep     sleep the current thread
      * @param sender    HTTP request event loop
      * @param receiver  HTTP response event loop
      */
     static public Server
     make(final String location,final Locator locator,final Receiver<Long> sleep,
          final Receiver<Outbound> sender, final Receiver<Inbound> receiver) {
         final ClientSide r = new ClientSide(location, locator, sleep,
                                             sender, receiver);
         r.start();
         return r;
     }
 
     public void
     serve(final Request head, final InputStream body, final Client client) {
         entry.enqueue(head, body, client);
     }
     
     /**
      * Sends an HTTP request.
      * @param request       request to send
      * @param connection    connection output stream
      * @throws Exception    any problem sending the request
      */
     static public void
     send(final Request head, final InputStream body,
          final OutputStream connection) throws Exception{
         final OutputStream out = new BufferedOutputStream(connection,
                 chunkSize - "0\r\n\r\n".length());
 
        // output the Request-Line
         TokenList.vet(TokenList.token, TokenList.nothing, head.method);
         TokenList.vet(TokenList.text, TokenList.whitespace, head.uri);
         
         final Writer hout = ASCII.output(Open.output(out));
         hout.write(head.method);
         hout.write(" ");
         hout.write(head.uri);
         hout.write(" HTTP/1.1\r\n");
 
        // output the header
         final Milestone<Boolean> selfDelimiting = Milestone.plan();
         if (null == body) { selfDelimiting.mark(true); }
         final Milestone<Boolean> contentLengthSpecified = Milestone.plan();
         long contentLength = 0;
         for (final Header header : head.headers) {
             if (!contentLengthSpecified.is() &&
                     Header.equivalent("Content-Length", header.name)) {
                 contentLengthSpecified.mark(true);
                 contentLength = Long.parseLong(header.value);
                 if (0 > contentLength) { throw new Exception("Bad Length"); }
                 selfDelimiting.mark(true);
             } else {
                 for (final String name : new String[] { "Content-Length",
                                                         "Connection",
                                                         "Transfer-Encoding",
                                                         "TE",
                                                         "Trailer",
                                                         "Upgrade" }) {
                     if (Header.equivalent(name, header.name)) {
                         throw new Exception("Illegal request header");
                     }
                 }
             }
             TokenList.vet(TokenList.token, "", header.name);
             TokenList.vet(TokenList.text, "\r\n", header.value);
 
             hout.write(header.name);
             hout.write(": ");
             hout.write(header.value);
             hout.write("\r\n");
         }
 
         // output the entity body
         if (selfDelimiting.is()) {
             hout.write("\r\n");
             hout.flush();
             hout.close();
 
             final OutputStream bout = Bounded.output(contentLength, out);
             if (null != body) {
                 body.reset();
                 Stream.copy(body, bout);
             }
             bout.close();
         } else {
             hout.write("Transfer-Encoding: chunked\r\n");
             hout.write("\r\n");
             hout.flush();
             hout.close();
 
             final OutputStream bout = new ChunkedOutputStream(chunkSize, out);
             body.reset();
             Stream.copy(body, bout);
             bout.close();
         }
         out.flush();
     }
     
     /**
      * Receives an HTTP response.
      * @param method    HTTP request method
      * @param cin       input stream
      * @param client    corresponding response processor
      * @return should the connection be closed?
      * @throws Exception    any problem reading the response
      */
     static public boolean
     receive(final String method, final InputStream cin,
             final Client client) throws Exception {
         final LineInput hin = new LineInput(Limited.input(32 * 1024, cin));
 
         // read the Status-Line
         final String statusLine = hin.readln();
         if (!statusLine.startsWith("HTTP/1.")) {throw new Nap("incompatible");}
         final int endStatusLine = statusLine.length();
 
         // parse the Status-Line
         final int beginVersion = 0;
         final int endVersion = TokenList.skip(
                 TokenList.digit, TokenList.nothing, statusLine,
         		"HTTP/1.".length(), endStatusLine);
         final String version = statusLine.substring(beginVersion, endVersion);
         final int beginStatus = endVersion + 1;
         final int endStatus = beginStatus + 3;
         final int beginPhrase = endStatus + 1;
         if (beginPhrase > endStatusLine) { throw new Nap("status line"); }
         if (' ' != statusLine.charAt(endVersion)){throw new Nap("status line");}
         if (' ' != statusLine.charAt(endStatus)) {throw new Nap("status line");}
         final String status = statusLine.substring(beginStatus, endStatus);
         final String phrase = statusLine.substring(beginPhrase);
 
         // sleep on a 5xx response
         if (status.startsWith("5")) { throw new Nap(phrase); }
 
         // parse the response headers
         PowerlessArray<Header> headers = HTTPD.readHeaders(hin);
 
         // check for informational response
         // RFC 2616, section 10.1:
         // "Unexpected 1xx status responses MAY be ignored by a user agent."
         if (status.startsWith("1")) { return receive(method, cin, client); }
 
         // build the response
         final Milestone<Boolean> closing = Milestone.plan();
         headers = HTTPD.forward(version, headers, closing);
         final InputStream entity;
         if ("204".equals(status)) {
             entity = null;
         } else if ("304".equals(status)) {
             entity = null;
         } else if ("HEAD".equals(method)) {
             entity = null;
         } else if ("CONNECT".equals(method)) {
             entity = null;
         } else {
             /*
              * with the exception of the cases handled above, all responses have
              * a message body, which is either explicitly delimited, or
              * terminated by connection close
              */
             final InputStream explicit = HTTPD.input(headers, cin);
             entity = null != explicit ? explicit : cin;
         }
         client.receive(new Response(version, status, phrase, headers), entity);
         
         /*
          * ensure this response has been fully read out of the response stream
          * before reading in the next response
          */
         if (!closing.is() && null != entity) {
             while (entity.read() != -1) { entity.skip(Long.MAX_VALUE); }
             entity.close();
         }
         return closing.is();
     }
 }
