 package no.ebakke.studycaster.backend;
 
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.ebakke.studycaster.util.Util;
 import org.apache.commons.io.IOUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpRequestRetryHandler;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.util.EntityUtils;
 
 /** Represents a connection to the StudyCaster server, and handles all details of the server API.
 Thread-safe. */
 public class ServerContext {
   public static final String SERVERURI_PROP_NAME = "studycaster.server.uri";
   private static final Logger LOG = Logger.getLogger("no.ebakke.studycaster");
   private static boolean SIMULATE_REPEATED_REQUESTS = false;
   private static final long   SIMULATE_SERVER_AHEAD_NANOS = 0 * 1000000L;
   private static final long   SIMULATE_LATENCY_NANOS      = 0 * 1000000L;
   private static final int    DEF_UPLOAD_CHUNK_SZ = 64 * 1024;
   // TODO: Rename this (legacy from earlier experiments).
   private static final String TICKET_STORE_FILENAME = "sc_7403204709139484951.tmp";
   private final URI    serverScriptURI;
   private final String launchTicket;
   private final TimeSource serverTimeSource;
   /* Keep the HttpClient in common for all requests, as is standard for this interface. This would
   also preserve any session cookies involved, though the API currently does not use any. */
   private final DefaultHttpClient httpClient;
   private final AtomicBoolean closed = new AtomicBoolean(false);
   private Long serverTickTimeOffset;
 
   private static String getServerScriptURIfromProperty() throws StudyCasterException {
     String serverScriptURIs = System.getProperty(SERVERURI_PROP_NAME);
     if (serverScriptURIs == null)
       throw new StudyCasterException("Property " + SERVERURI_PROP_NAME + " not set");
     return serverScriptURIs;
   }
 
   public ServerContext() throws StudyCasterException {
     this(getServerScriptURIfromProperty());
   }
 
   private String getMandatoryHeader(HttpResponse response, String name) throws IOException {
     Header[] ret = response.getHeaders(name);
     if (ret.length != 1 || ret[0].getValue() == null)
       throw new IOException("Expected a single header " + name + " from the server");
     return ret[0].getValue();
   }
 
   private long convertLong(String value) throws StudyCasterException {
     try {
       return Long.parseLong(value);
     } catch (NumberFormatException e) {
       throw new StudyCasterException("Got bad number format from server", e);
     }
   }
 
   private void simulateLatency() throws StudyCasterException {
     if (SIMULATE_LATENCY_NANOS > 0.0) {
       try {
         Util.delayAtLeast(Math.round(Math.random() * SIMULATE_LATENCY_NANOS / 2.0));
       } catch (InterruptedException e) {
         throw new StudyCasterException("Interrupted during latency simulation", e);
       }
     }
   }
 
   private long getServerTimeNanos() throws StudyCasterException {
     HttpResponse response;
     try {
       simulateLatency();
       response = requestHelper("tim", null, null);
       simulateLatency();
       try {
         /* RealTimeNanos is only used on the first request to calculate the offset of the more
         accurate TickTimeNanos timer. */
         final long realTimeNanos =
             convertLong(getMandatoryHeader(response, "X-StudyCaster-RealTimeNanos")) +
             SIMULATE_SERVER_AHEAD_NANOS;
         final long tickTimeNanos =
             convertLong(getMandatoryHeader(response, "X-StudyCaster-TickTimeNanos"));
         final long newServerTickTimeOffset = tickTimeNanos - realTimeNanos;
         synchronized (this) {
           /* If the offset between the two timers has changed substantially, assume that it needs to
           be set again. This can be due to an adjustment of the real-time clock on the server, or
           because the server was restarted. Still, it's unlikely that this will happen during the
           few seconds that the client is requesting timing measurements. */
           if (serverTickTimeOffset == null ||
               Math.abs(serverTickTimeOffset - newServerTickTimeOffset) > 3000000000L)
           {
             serverTickTimeOffset = newServerTickTimeOffset;
           }
           return tickTimeNanos - serverTickTimeOffset;
         }
       } finally {
         EntityUtils.consume(response.getEntity());
       }
     } catch (IOException e) {
       throw new StudyCasterException("Failed to retrieve server time", e);
     }
   }
 
   @SuppressWarnings("AssignmentReplaceableWithOperatorAssignment")
   private TimeSource measureServerTime() throws StudyCasterException {
     /* TODO: Don't depend on System.currentTimeMillis() via TimeSource here. */
     final TimeSource localTimeSource = new TimeSource();
     final int    MIN_ATTEMPTS        = 5;
     final int    MAX_ATTEMPTS        = 15;
     final double MAX_SEM_NANOS       =   50 * 1000000.0;
     final double MAX_ROUNDTRIP_NANOS = 1000 * 1000000.0;
     /* Running average, sum of squares, last number of samples, and last standard error of the
     mean. */
     double A = 0, Q = 0, N = 0, sem = Double.POSITIVE_INFINITY;
     for (int i = 1, attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
       // A single measurement.
       final double x;
       {
         final long nanosBefore = System.nanoTime();
         final long serverTimeNanos = getServerTimeNanos();
         final long nanosAfter  = System.nanoTime();
         final double requestLengthNanos = nanosAfter - nanosBefore;
         if (requestLengthNanos > MAX_ROUNDTRIP_NANOS) {
           // Only disregard if we can still get to MIN_ATTEMPTS.
           boolean disregard = (attempt < MAX_ATTEMPTS - MIN_ATTEMPTS + N);
           LOG.log(Level.INFO, "Long timing roundtrip ({0,number,#}ms), disregard={1}",
               new Object[] { requestLengthNanos / 1000000.0, disregard });
           if (disregard)
             continue;
         }
         final double adjustedLocalTime =
             localTimeSource.currentTimeNanos() - requestLengthNanos / 2.0;
         x = serverTimeNanos - adjustedLocalTime;
       }
       // See http://en.wikipedia.org/wiki/Standard_deviation#Rapid_calculation_methods .
       Q = Q + ((i - 1.0) / i) * (x - A) * (x - A);
       A = A + (x - A) / i;
       N = i;
       i++;
       final double stdev = (N == 1) ? Double.POSITIVE_INFINITY : Math.sqrt(Q / (N - 1.0));
       sem = stdev / Math.sqrt(N);
       if (N >= MIN_ATTEMPTS && sem < MAX_SEM_NANOS)
         break;
     }
     LOG.log(Level.INFO,
         "Measured server time ahead by {0,number,#}+/-{1,number,#}ms with {2} samples",
         new Object[]{ A / 1000000.0, sem / 1000000.0, N});
     return new TimeSource(localTimeSource, Math.round(A));
   }
 
   public ServerContext(String serverScriptURIs) throws StudyCasterException {
     LOG.log(Level.INFO, "Using server URI {0}", serverScriptURIs);
     try {
       serverScriptURI = new URI(serverScriptURIs + "/api");
     } catch (URISyntaxException e) {
       throw new StudyCasterException("Malformed server URI", e);
     }
 
     // Read ticket store.
     File ticketStore = new File(System.getProperty("java.io.tmpdir"), TICKET_STORE_FILENAME);
     boolean writeTicketStore = true;
     String clientCookie = null;
     try {
       BufferedReader br = new BufferedReader(new FileReader(ticketStore));
       try {
         clientCookie = br.readLine();
       } finally {
         br.close();
       }
       writeTicketStore = false;
     } catch (FileNotFoundException e) {
       // Ignore.
     } catch (IOException e) {
       LOG.log(Level.WARNING, "Problem reading ticket store.", e);
     }
 
     // Setup launch session.
     try {
       ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager();
       httpClient = new DefaultHttpClient(connectionManager);
       httpClient.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {
         public boolean retryRequest(IOException exception, int executionCount,
             HttpContext context)
         {
           if (exception instanceof NonRetriableException) {
             LOG.log(Level.WARNING, "Non-retriable error encountered in HttpRequestRetryHandler");
             return false;
           } else {
             LOG.log(Level.INFO, "Waiting to retry request ({0} times)", executionCount);
             try {
               Thread.sleep(10000);
             } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
             }
             return true;
           }
         }
       });
 
       String receivedClientCookie;
       HttpResponse response = requestHelper("gsi", null, clientCookie == null ? "" : clientCookie);
       try {
         launchTicket         = getMandatoryHeader(response, "X-StudyCaster-LaunchTicket");
         receivedClientCookie = getMandatoryHeader(response, "X-StudyCaster-ClientCookie");
       } finally {
         EntityUtils.consume(response.getEntity());
       }
       if (clientCookie != null && !clientCookie.equals(receivedClientCookie))
         throw new StudyCasterException("Server returned odd client cookie");
       clientCookie = receivedClientCookie;
     } catch (IOException e) {
       throw new StudyCasterException("Cannot retrieve server info.", e);
     }
     LOG.log(Level.INFO, "clientCookie = {0}, launchTicket = {1}",
         new Object[] {clientCookie, launchTicket});
 
     // Write ticket store.
     if (writeTicketStore) {
       try {
         FileWriter fw = new FileWriter(ticketStore);
         try {
           fw.write(clientCookie.toString() + "\n");
         } finally {
           fw.close();
         }
         LOG.info("Wrote to ticket store.");
       } catch (IOException e) {
         LOG.log(Level.WARNING, "Problem writing ticket file.", e);
       }
     }
     serverTimeSource = measureServerTime();
     Util.logEnvironmentInfo();
   }
 
   private static String getContentString(HttpResponse resp) throws IOException {
     StringWriter ret = new StringWriter();
     InputStream errContent = resp.getEntity().getContent();
     try {
       Header encoding = resp.getEntity().getContentEncoding();
       IOUtils.copy(errContent, ret, (encoding == null) ? "UTF-8" : encoding.getValue());
     } finally {
       errContent.close();
     }
     return ret.toString();
   }
 
   /** Callers must always remember to consume the response, or future requests may hang. */
   @SuppressWarnings("SleepWhileInLoop")
   private HttpResponse requestHelper(String cmd, ContentBody content, String arg)
       throws IOException
   {
     HttpResponse ret = null;
     do {
       try {
         ret = requestHelperSingle(cmd, content, arg);
       } catch (IOException e) {
         LOG.log(Level.WARNING, "Failed request beyond HttpClient", e);
         if (e instanceof NonRetriableException)
           throw e;
       }
       if (ret == null) {
         LOG.log(Level.INFO, "Waiting to retry request");
         try {
           Thread.sleep(10000);
         } catch (InterruptedException e) {
           throw new IOException("HTTP request retry thread interrupted");
         }
       }
       if (SIMULATE_REPEATED_REQUESTS && ret != null && Math.random() > 0.5) {
         LOG.log(Level.INFO, "Simulating a repeated request ({0})", new Object[] { cmd });
         EntityUtils.consume(ret.getEntity());
         ret = null;
       }
     } while (ret == null);
     return ret;
   }
 
   private HttpResponse requestHelperSingle(String cmd, ContentBody content, String arg)
       throws IOException
   {
     HttpPost httpPost = new HttpPost(serverScriptURI);
     MultipartEntity params = new MultipartEntity();
     params.addPart("cmd", new StringBody(cmd));
     params.addPart("lt", new StringBody(launchTicket == null ? "" : launchTicket));
     if (content != null)
       params.addPart("content", content);
     if (arg != null)
       params.addPart("arg", new StringBody(arg));
     httpPost.setEntity(params);
     HttpResponse response = httpClient.execute(httpPost);
    if (response.getEntity() == null)
      throw new IOException("Failed to get response entity.");
     boolean error = true;
     try {
       if (response.getStatusLine().getStatusCode() != 200) {
         Header disableRetryHeader = response.getFirstHeader("X-StudyCaster-DisableRetry");
         Header errorMessageHeader = response.getFirstHeader("X-StudyCaster-ErrorMessage");
         boolean disableRetry =
             disableRetryHeader == null ? false : disableRetryHeader.getValue().equals("true");
         final String SEPARATOR = "-------------------------------------";
         String errorMessage = errorMessageHeader == null
             ? (":\n" + SEPARATOR + "\n" + getContentString(response) + "\n" + SEPARATOR)
             : ("; " + errorMessageHeader.getValue());
         String exceptionMessage = "Command " + cmd + " got bad status code " +
             response.getStatusLine().getReasonPhrase() + errorMessage;
         if (disableRetry) {
           throw new NonRetriableException(exceptionMessage);
         } else {
           throw new IOException(exceptionMessage);
         }
       }
       String okHeader = getMandatoryHeader(response, "X-StudyCaster-OK");
       if (!okHeader.equals(cmd))
         throw new IOException("Got invalid StudyCaster response header: " + okHeader);
       error = false;
       return response;
     } finally {
      if (error)
         EntityUtils.consume(response.getEntity());
     }
   }
 
   public OutputStream uploadFile(final String remoteName) throws IOException {
     Util.checkClosed(closed);
     EntityUtils.consume(requestHelper("upc", new StringBody(remoteName), null).getEntity());
 
     return new BufferedOutputStream(new OutputStream() {
       private final AtomicBoolean closed  = new AtomicBoolean(false);
       private final AtomicLong    written = new AtomicLong(0);
 
       @Override
       public void write(int b) throws IOException {
          write(new byte[] { (byte) b }, 0, 1);
       }
 
       @Override
       public void write(byte[] b, int off, final int len) throws IOException {
         Util.checkClosed(closed);
 
         // TODO: Deduplicate with chunking logic in WriteOpQueue.
         for (int subOff = 0; subOff < len; ) {
           final int subLen = Math.min(len - subOff, DEF_UPLOAD_CHUNK_SZ);
           byte chunk[] = Util.copyOfRange(b, off + subOff, off + subOff + subLen);
           /* Use a ByteArrayBody rather than an InputStreamBody in case the
           request needs to be repeated. */
           ByteArrayBody bab = new ByteArrayBody(chunk, remoteName);
           EntityUtils.consume(requestHelper("upa", bab, Long.toString(written.get())).getEntity());
           subOff += subLen;
           written.addAndGet(subLen);
         }
       }
 
       @Override
       public void close() throws IOException {
         closed.set(true);
       }
     }, DEF_UPLOAD_CHUNK_SZ);
   }
 
   public InputStream downloadFile(String remoteName) throws IOException {
     final InputStream ret = requestHelper("dnl",
         new StringBody(remoteName), null).getEntity().getContent();
     return new InputStream() {
       private final AtomicBoolean closed = new AtomicBoolean(false);
 
       @Override
       public int read() throws IOException {
         Util.checkClosed(closed);
         return ret.read();
       }
 
       @Override
       public int read(byte[] b, int off, int len) throws IOException {
         Util.checkClosed(closed);
         return ret.read(b, off, len);
       }
 
       @Override
       public void close() throws IOException {
         if (closed.getAndSet(true))
           return;
         ret.close();
       }
     };
   }
 
   public String getLaunchTicket() {
     return launchTicket;
   }
 
   public TimeSource getServerTimeSource() {
     return serverTimeSource;
   }
 
   public void close() {
     if (closed.getAndSet(true))
       return;
     httpClient.getConnectionManager().shutdown();
   }
 
   private static class NonRetriableException extends IOException {
     private static final long serialVersionUID = 1L;
 
     NonRetriableException(String s) {
       super(s);
     }
   }
 
   public static void main(String args[]) throws StudyCasterException {
     LOG.info("Constructing ServerContext");
     ServerContext serverContext = new ServerContext();
     LOG.info("Closing");
     serverContext.close();
   }
 }
