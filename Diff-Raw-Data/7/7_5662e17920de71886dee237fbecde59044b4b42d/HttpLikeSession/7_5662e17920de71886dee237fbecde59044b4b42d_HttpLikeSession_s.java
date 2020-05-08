 package com.jrodeo.remote;
 
 
 import com.jrodeo.nio.NioControlRequest;
 import com.jrodeo.nio.RequestReader;
 import com.jrodeo.nio.ResponseWriter;
 import com.jrodeo.nio.Session;
 import com.jrodeo.restlike.RestlikeServlet;
 
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.net.SocketException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: brad_hlista
  */
 
 public class HttpLikeSession implements Session {
 
     HttpLikeServletService httpLikeServletService;
 
     SelectionKey clientSelectionKey;
     SocketChannel sc;
    int BUF_SIZE = 7;
     ByteBuffer bb = ByteBuffer.allocate(BUF_SIZE);
     ByteBuffer outBB = ByteBuffer.allocate(BUF_SIZE);
 
     boolean keepOpen = true;
     boolean useWorkQueue = false;
 
     HttpLikeServletRequest request;
     RequestReader requestReader;
     static final int HEADERS = 0;
     static final int CONTINUE = 1;
     static final int USER_READING = 2;
     int state = HEADERS;
     String requestMethod;
     String urlString;
     byte[] trackedReadBuf;
     int trackedReadIndex;
     int trackedReadTo;
 
 
     HttpLikeServletResponse response;
     ResponseWriter responseWriter;
     String trackedStringToWrite;
     int trackedStringIndex;
     int trackedStringWritten;
     int trackStringToWrite;
 
     static final String continue100;
 
     static {
         StringBuffer sb = new StringBuffer();
         sb.append("HTTP/1.1 ");
         sb.append(HttpServletResponse.SC_CONTINUE);
         sb.append("\r\n");
         sb.append("\r\n");
 
         continue100 = sb.toString();
     }
 
     public HttpLikeSession() {
         reset();
     }
 
     void reset() {
         state = HEADERS;
         headersParser = startLineA;
         requestReader = null;
         urlString = null;
         trackedReadBuf = null;
 
         bb.clear();
         outBB.clear();
         response = new HttpLikeServletResponse(this);
         trackedStringToWrite = null;
     }
 
     public void setClientSelectionKey(SelectionKey clientSelectionKey) throws SocketException {
         this.clientSelectionKey = clientSelectionKey;
         sc = (SocketChannel) clientSelectionKey.channel();
         sc.socket().setKeepAlive(true);
     }
 
     public void setHttpLikeServletService(HttpLikeServletService httpLikeServletService) {
         this.httpLikeServletService = httpLikeServletService;
     }
 
     public void setReadComplete() {
         bb.clear();
         turnOffReads();
     }
 
     public void setRequestReader(RequestReader requestReader) {
         state = USER_READING;
         this.requestReader = requestReader;
     }
 
     public void doRead() {
         try {
             switch (state) {
                 case HEADERS:
                     int n = sc.read(bb);
                     if (n == -1) {
                         // done...eos, can't continue parsing bb
                         clientSelectionKey.cancel();
                         return;
                     } else if (n == 0) {
                         return;
                     }
                     bb.flip();
                     boolean ready = headersParser.parse();
                     if (!ready) {
                         bb.clear();
                         return;
                     }
 
                     String value = headers.get("Expect");
                     if (value != null && ("100-Continue".equals(value) || "100-continue".equals(value))) {
                         state = CONTINUE;
                         clientSelectionKey.interestOps(SelectionKey.OP_WRITE);
 
                         setResponseWriter(new ResponseWriter() {
                             public void write() throws IOException {
                                 if (trackStringWrite(continue100)) {
                                     setResponseWriter(null);
                                     outBB.clear();
                                     clientSelectionKey.interestOps(SelectionKey.OP_READ);
                                 }
                             }
 
                             public void handleException(Exception e) {
                                 // todo: log
                             }
                         });
 
                         return;
                     }
                     // we provide the remainder of bb to the request.getInputStream() or RequestReader
 
                     // if there is no workQueue, ie no worker thread pool, we'll handle it here
                     // else put the request onto the workQ
 
                 case CONTINUE:
                     parseStartLine();
 
                     RestlikeServlet servlet = httpLikeServletService.findServlet(urlString);
 
                     request = new HttpLikeServletRequest(this, servlet, requestMethod, urlString);
 
                     if ("GET".equals(request.getMethod())) {
                         servlet.invokeGet(request, response);
                     } else if ("PUT".equals(request.getMethod())) {
                         servlet.invokePut(request, response);
                     } else if ("POST".equals(request.getMethod())) {
                         servlet.invokePost(request, response);
                     }
                     break;
 
                 case USER_READING:
                     requestReader.read();
                     break;
             }
         } catch (Exception e) {
             // todo: log
             clientSelectionKey.cancel();
         }
     }
 
     public void doWrite() throws IOException {
         // if there is a payload, write it in friendly fashion, keeping the OP_WRITE on, until we are done
         responseWriter.write(); // user maintains all state and finishes when done by calling completeResponseWriter
     }
 
     public void handleWriteException(Exception e) {
         responseWriter.handleException(e);
     }
 
     public int read(ByteBuffer byteBuffer) throws IOException {
         int cnt = 0;
         if (bb.hasRemaining()) {
             while (byteBuffer.hasRemaining()) {
                 byteBuffer.put(bb.get());
                 cnt++;
                 if (!bb.hasRemaining()) {
                     return cnt;
                 }
             }
         }
         cnt += sc.read(byteBuffer);
         return cnt;
     }
 
     public boolean trackedByteBufRead(byte[] buf, int initialOffset, int toRead) throws IOException {
         if (trackedReadBuf != buf) {
             trackedReadBuf = buf;
             trackedReadIndex = initialOffset;
             trackedReadTo = initialOffset + toRead;
         }
 
         int bbMax = bb.remaining();
 
         while (trackedReadIndex < trackedReadTo) {
             if (bbMax > 0) {
                 buf[trackedReadIndex] = bb.get();
                 trackedReadIndex++;
                 bbMax--;
             } else {
                 bb.clear();
                 if ((bbMax = sc.read(bb)) == 0) {
                     return false;
                 }
                 bb.flip();
             }
         }
 
         return true;
     }
 
     public int write(ByteBuffer byteBuffer) throws IOException {
         return sc.write(byteBuffer);
     }
 
     public boolean trackStringWrite(String stringToWrite) throws IOException {
         if (stringToWrite != trackedStringToWrite) {
             trackedStringToWrite = stringToWrite;
             trackedStringIndex = 0;
             trackedStringWritten = -outBB.position();
             trackStringToWrite = trackedStringToWrite.length();
         }
 
         while (trackedStringWritten < trackStringToWrite) {
             int stopAt;
             if (outBB.remaining() < trackStringToWrite - trackedStringIndex) {
                 stopAt = trackedStringIndex + outBB.remaining();
             } else {
                 stopAt = trackStringToWrite;
             }
 
             for (; trackedStringIndex < stopAt; trackedStringIndex++) {
                 outBB.put((byte) stringToWrite.charAt(trackedStringIndex));
             }
 
             outBB.flip();
             trackedStringWritten += sc.write(outBB);
             outBB.compact();
             if (trackedStringIndex != trackedStringWritten) {
                 return false;
             }
         }
 
         trackedStringToWrite = null;
         return true;
     }
 
     NioControlRequest turnOffReadsRequest = new NioControlRequest() {
         ByteBuffer writeBuf = ByteBuffer.allocate(1).put((byte) 1);
 
         public ByteBuffer getWriteBuf() {
             return writeBuf;
         }
 
         public void handle() throws InterruptedException {
             clientSelectionKey.interestOps(0);
         }
     };
 
     NioControlRequest readRequest = new NioControlRequest() {
         ByteBuffer writeBuf = ByteBuffer.allocate(1).put((byte) 1);
 
         public ByteBuffer getWriteBuf() {
             return writeBuf;
         }
 
         public void handle() throws InterruptedException {
             clientSelectionKey.interestOps(SelectionKey.OP_READ);
         }
     };
 
     NioControlRequest writeRequest = new NioControlRequest() {
         ByteBuffer writeBuf = ByteBuffer.allocate(1).put((byte) 1);
 
         public ByteBuffer getWriteBuf() {
             return writeBuf;
         }
 
         public void handle() throws InterruptedException {
             clientSelectionKey.interestOps(SelectionKey.OP_WRITE);
         }
     };
 
     public void turnOffReads() {
         if (httpLikeServletService.onMainThread()) {
             clientSelectionKey.interestOps(0);
         } else {
             httpLikeServletService.scheduleNioControlRequest(turnOffReadsRequest);
         }
 
     }
 
     public void scheduleRequestReader(RequestReader requestReader) {
         setRequestReader(requestReader);
         if (httpLikeServletService.onMainThread()) {
             clientSelectionKey.interestOps(SelectionKey.OP_READ);
         } else {
             httpLikeServletService.scheduleNioControlRequest(readRequest);
         }
 
     }
 
     public void scheduleResponseWriter(ResponseWriter responseWriter) {
         setResponseWriter(responseWriter);
         if (httpLikeServletService.onMainThread()) {
             clientSelectionKey.interestOps(SelectionKey.OP_WRITE);
         } else {
             httpLikeServletService.scheduleNioControlRequest(writeRequest);
         }
     }
 
 
     public void scheduleCancel() {
         if (httpLikeServletService.onMainThread()) {
             doCancel();
         } else {
             NioControlRequest cancelRequest = new NioControlRequest() {
                 ByteBuffer writeBuf = ByteBuffer.allocate(1).put((byte) 1);
 
                 public ByteBuffer getWriteBuf() {
                     return writeBuf;
                 }
 
                 public void handle() throws InterruptedException {
                     doCancel();
                 }
             };
             httpLikeServletService.scheduleNioControlRequest(cancelRequest);
         }
     }
 
     // only runs on NIO main thread //
 
     void doCancel() {
         clientSelectionKey.cancel();
         try {
             clientSelectionKey.channel().close();
         } catch (IOException ioe) {
             // do nothing
         }
     }
 
 
     void doFlushing() {
         try {
             outBB.flip();
             flusher.write();
             if (state != HEADERS) {
                 setResponseWriter(flusher);
             }
         } catch (IOException ioe) {
             // todo: log
             clientSelectionKey.cancel();
         }
     }
 
 
     //////////////////////
 
 
     public void setResponseComplete() {
 
         if (httpLikeServletService.onMainThread()) {
             doFlushing();
         } else {
             NioControlRequest responseComplete = new NioControlRequest() {
                 ByteBuffer writeBuf = ByteBuffer.allocate(1).put((byte) 1);
 
                 public ByteBuffer getWriteBuf() {
                     return writeBuf;
                 }
 
                 public void handle() throws InterruptedException {
                     doFlushing();
                 }
             };
             httpLikeServletService.scheduleNioControlRequest(responseComplete);
         }
 
     }
 
     /////////////////// Package Protected ///////////////////
 
 
     void setResponseWriter(ResponseWriter responseWriter) {
         this.responseWriter = responseWriter;
     }
 
 
     ///   Non Preferred Use  ///
 
     /*
     int read() throws IOException {
         if (bb.hasRemaining()) {
             return bb.get();
         }
         bb.clear();
         if (sc.read(bb) == 0) {
             bb.flip();
             throw new ReadEmpty();
         }
         bb.flip();
         return bb.get();
     }
 
     void write(int b) throws IOException {
         boolean put = true;
 
         if (outBB.remaining() > 0) {
             outBB.put((byte) b);
         } else {
             put = false;
         }
 
         if (outBB.remaining() == 0) {
             flush();
         }
 
         if (!put) {
             if (outBB.remaining() > 0) {
                 outBB.put((byte) b);
             } else {
                 throw new WriteFull();
             }
         }
     }
     */
 
     void flush() throws IOException {
         outBB.flip();
         while (outBB.hasRemaining()) {
             if (sc.write(outBB) == 0) {
                 // todo: problems if not written...
             }
         }
         outBB.compact();
     }
 
 
     /////////////////// Private Scope ///////////////////
 
     private final ResponseWriter flusher = new ResponseWriter() {
         public void write() throws IOException {
             // the outBB has already been flipped
 
             while (outBB.hasRemaining()) {
                 if (sc.write(outBB) == 0) {
                     // don't outBB.compact(), the data is already flipped
                     return;
                 }
             }
 
 
             // done
             if (keepOpen) {
                 clientSelectionKey.interestOps(SelectionKey.OP_READ);
                 reset();
             } else {
                 clientSelectionKey.channel().close();
                 clientSelectionKey.cancel();
             }
         }
 
         public void handleException(Exception e) {
             // todo: log
         }
     };
 
 
     String startLine = null;
     char[] chars = new char[1024];  // max length
     int charsIndex = 0;
     int splitChar = -1;
 
     interface HeadersParser {
         public boolean parse();
     }
 
     HeadersParser headersParser;
 
     private final HeadersParser headersB = new HeadersParser() {
         public boolean parse() {
             if (bb.hasRemaining()) {
                 bb.get();
                 if (charsIndex > 1) {
                     doHeader();
                     charsIndex = 0;
                 } else {
                     return true;
                 }
                 headersParser = headersA;
                 return headersParser.parse();
             }
             return false;
         }
     };
 
 
     private final HeadersParser headersA = new HeadersParser() {
         public boolean parse() {
             while (bb.hasRemaining()) {
                 if ((chars[charsIndex] = (char) bb.get()) == '\r') {
                     if (!bb.hasRemaining()) {
                         headersParser = headersB;
                         return false;
                     }
                     bb.get();
                     if (charsIndex > 1) {
                         doHeader();
                         charsIndex = 0;
                         continue;
                     } else {
                         return true;
                     }
                 } else if (chars[charsIndex] == ':' && splitChar == -1) {
                     splitChar = charsIndex;
                 }
                 charsIndex++;
             }
             return false;
         }
     };
 
     private final HeadersParser startLineB = new HeadersParser() {
         public boolean parse() {
             if (bb.hasRemaining()) {
                 bb.get();
                 startLine = new String(chars, 0, charsIndex);
                charsIndex = 0;
                 headersParser = headersA;
             }
             return false;
         }
     };
 
     private HeadersParser startLineA = new HeadersParser() {
         public boolean parse() {
             while (bb.hasRemaining()) {
                 if ((chars[charsIndex] = (char) bb.get()) == '\r') {
                     if (!bb.hasRemaining()) {
                         headersParser = startLineB;
                         return false;
                     }
                     bb.get();
                     startLine = new String(chars, 0, charsIndex);
                     charsIndex = 0;
                     headersParser = headersA;
                     return headersParser.parse();
                 }
                 charsIndex++;
             }
             return false;
         }
     };
 
     private void parseStartLine() {
         if (startLine.startsWith("G")) {
             requestMethod = "GET";
         } else if (startLine.startsWith("PU")) {
             requestMethod = "PUT";
         } else if (startLine.startsWith("PO")) {
             requestMethod = "POST";
         } else {
             throw new RuntimeException("invalid request: " + startLine);
         }
 
         int i1 = startLine.indexOf(' ');
         int i2 = startLine.indexOf(' ', i1 + 1);
 
         urlString = startLine.substring(i1 + 1, i2);
     }
 
     private void doHeader() {
         String name = new String(chars, 0, splitChar);
         splitChar++;
         while (chars[splitChar] == ' ' || chars[splitChar] == '\t') {
             splitChar++;
         }
         String value = new String(chars, splitChar, charsIndex - splitChar);
         headers.put(name, value);
         splitChar = -1;
     }
 
     Map<String, String> headers = new HashMap<String, String>();
 
 
 }
