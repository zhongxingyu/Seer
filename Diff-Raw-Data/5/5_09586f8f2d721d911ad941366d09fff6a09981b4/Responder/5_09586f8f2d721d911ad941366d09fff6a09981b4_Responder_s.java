 // Copyright 2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.net.http;
 
 import static org.joe_e.array.PowerlessArray.array;
 import static org.ref_send.promise.Fulfilled.ref;
 import static org.waterken.io.Content.chunkSize;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.charset.ASCII;
 import org.ref_send.promise.eventual.Do;
 import org.waterken.http.Failure;
 import org.waterken.http.Request;
 import org.waterken.http.Response;
 import org.waterken.http.Server;
 import org.waterken.io.bounded.Bounded;
 import org.waterken.io.open.Open;
 import org.waterken.uri.Header;
 import org.waterken.uri.URI;
 
 /**
  * Outputs a {@link Response}.  
  */
 final class
 Responder {
 
     private final Server server;
     private       boolean closing;
     private       boolean followed;
     private       OutputStream out;
     private       boolean started;
 
     Responder(final Server server, final OutputStream out) {
         this.server = server;
         this.out = new BufferedOutputStream(out,chunkSize-"0\r\n\r\n".length());
     }
 
     Responder(final Server server) {
         this.server = server;
     }
     
     void
     setClosing() { closing = true; }
     
     Do<Response,Void>
     respond(final String version, final String method, final Responder next) {
         if (followed) { throw new RuntimeException(); }
         followed = true;
         return new Do<Response,Void>() {
 
             public Void
             fulfill(final Response response) throws Exception {
                 if (null == out) { throw new Exception(); }
                 if (started) { return null; }
                 started = true;
                 try {
                     write(version, method, response);
                 } catch (final Exception e) {
                     closing = true;
                     try { out.close(); } catch (final IOException e2) {}
                     throw e;
                 }
                 out.flush();
                 if (closing) { out.close(); }
                 if (response.status.startsWith("1")) {
                     started = false;
                 } else {
                     next.out = out;
                     out = null;
                 }
                 return null;
             }
 
             public Void
             reject(final Exception reason) throws Exception {
                 final String status;
                 final String phrase;
                 if (reason instanceof Failure) {
                     final Failure e = (Failure)reason;
                     status = e.status;
                     phrase = e.phrase;
                 } else {
                     status = "400";
                     phrase = "Bad Request";
                 }
                 final Do<Response,Void> output = this;
                 final String resource = URI.resolve("file:///res/", status); 
                 server.serve(resource,
                              ref(new Request(version, "GET", resource,
                                  PowerlessArray.array(new Header[] {}), null)),
                              new Do<Response,Void>() {
 
                     public Void
                     fulfill(final Response response) throws Exception {
                         return output.fulfill(new Response(
                             response.version, status, phrase,
                             response.header, response.body));
                     }
 
                     public Void
                     reject(final Exception reason) throws Exception {
                         return output.fulfill(new Response(
                             "HTTP/1.1", status, phrase,
                             array(
                                 new Header("Content-Length", "0")
                             ), null));
                     }
                 });
                 return null;
             }
         };
     }
     
     /**
      * Writes an HTTP response.
      * <p>
      * If the connection is to be kept alive, the implementation MUST write a
      * complete response and prevent further writes to the output stream by the
      * <code>response</code>. If the connection is not to be kept alive, the
      * method MUST either set the closing flag, or throw an exception. 
      * </p>
      */
     private void
     write(final String version, final String method,
           final Response response) throws Exception {
         final boolean empty =
             "HEAD".equals(method) || response.status.startsWith("1") ||
             "204".equals(response.status) || "205".equals(response.status) ||
             "304".equals(response.status);
         if (empty && null != response.body) {
             throw new Exception("unexpected entity");
         }
         if (response.status.startsWith("1") &&
                 !"100".equals(response.status)) {
             throw new Exception("Unknown informational response");
         }
         if (response.status.equals("306")) {
             throw new Exception("Illegal status code");
         }
         if (response.status.equals("402")) {
             throw new Exception("Illegal status code");
         }
         if (response.status.length() != 3) {
             throw new Exception("Invalid status code");
         }
         final char major = response.status.charAt(0);
         if ('1' > major || '5' < major) {
             throw new Exception("Unknown status code");
         }
         for (int i = response.status.length(); --i != 0;) {
             final char c = response.status.charAt(i);
             if ('0' > c || '9' < c) {
                 throw new Exception("Invalid status code");
             }
         }
         HTTP.vet("", response.phrase);
 
         // output the Response-Line
         final Writer hrs = ASCII.output(Open.output(out));
         hrs.write("HTTP/1.1 ");
         hrs.write(response.status);
         hrs.write(" ");
         hrs.write(response.phrase);
         hrs.write("\r\n");
 
         // output the header
         boolean contentLengthSpecified = false;
         long length = 0;
         boolean delimited = empty;
         for (final Header h : response.header) {
             if (!contentLengthSpecified &&
                 "Content-Length".equalsIgnoreCase(h.name)) {
                 contentLengthSpecified = true;
                 if (!"HEAD".equals(method)) {
                     length = Long.parseLong(h.value);
                     if (0 > length) { throw new Exception("Bad Length"); }
                     delimited = true;
                 }
             } else {
                 for (final String name : new String[] { "Content-Length",
                                                         "Connection",
                                                         "Transfer-Encoding",
                                                         "TE",
                                                         "Trailer",
                                                         "Upgrade" }) {
                     if (name.equalsIgnoreCase(h.name)) {
                         throw new Exception("Illegal response header");
                     }
                 }
             }
             HTTP.vetHeader(h);
 
             hrs.write(h.name);
             hrs.write(": ");
             hrs.write(h.value);
             hrs.write("\r\n");
         }
         
         // complete the response
         if ("HTTP/1.1".equals(version)) {
             if (delimited) {
                 hrs.write("\r\n");
                 hrs.flush();
                 hrs.close();
 
                 final OutputStream brs = Bounded.output(length, out);
                 if (null != response.body) { response.body.writeTo(brs); }
                 brs.close();
             } else {
                 hrs.write("Transfer-Encoding: chunked\r\n");
                 hrs.write("\r\n");
                 hrs.flush();
                 hrs.close();
 
                 final OutputStream brs = new ChunkedOutputStream(chunkSize,out);
                 if (null != response.body) { response.body.writeTo(brs); }
                 brs.close();
             }
         } else {
             if (delimited) {
                 if (closing) {
                     hrs.write("Connection: close\r\n");
                 } else {
                     hrs.write("Connection: keep-alive\r\n");
                 }
                 hrs.write("\r\n");
                 hrs.flush();
                 hrs.close();
 
                 final OutputStream brs = Bounded.output(length, out);
                 if (null != response.body) { response.body.writeTo(brs); }
                 brs.close();
             } else {
                 closing = true;
                 hrs.write("Connection: close\r\n");
                 hrs.write("\r\n");
                 hrs.flush();
                 hrs.close();
 
                 if (null != response.body) { response.body.writeTo(out); }
             }
         }
     }
 }
