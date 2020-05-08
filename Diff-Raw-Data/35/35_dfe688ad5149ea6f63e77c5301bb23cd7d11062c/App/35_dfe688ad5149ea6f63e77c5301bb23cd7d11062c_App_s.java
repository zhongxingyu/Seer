 import java.io.IOException;
 import java.util.Random;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import net.spy.memcached.AddrUtil;
 import net.spy.memcached.ConnectionFactoryBuilder;
 import net.spy.memcached.MemcachedClient;
 import net.spy.memcached.auth.AuthDescriptor;
 import net.spy.memcached.auth.PlainCallbackHandler;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.*;
 
 /**
  * MemCachier (minimal) Java example application.
  *
  * @author MemcCachier Inc.
  */
 public class App extends HttpServlet {
 
   /**
    * Respond to a (any) HTTP GET request.
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp)
       throws ServletException, IOException {
     int digit = (new Random()).nextInt(30);
     Result r = fib(digit);
     StringBuilder sb = new StringBuilder();
 
     // static html...
     sb.append(
         "<html>\n" +
           "<head>\n" +
             "<title>MemCachier Fibonacci Example</title>\n" +
             "<style type='text/css'>\n" +
               "html, body { height: 100%; }\n " +
               "#wrap { min-height: 100%; height: auto !important; height: 100%; margin: 0 auto -60px; }\n" +
               "#push, #footer { height: 60px; }\n" +
               "#footer { background-color: #f5f5f5; }\n" +
               "@media (max-width: 767px) { #footer { margin-left: -20px; margin-right: -20px; padding-left: 20px; padding-right: 20px; }}\n" +
               ".container { width: auto; max-width: 680px; }\n" +
              ".container .credit { margin: 20px 0; }\n" +
             "</style>" +
             "<link href='//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/" +
                        "css/bootstrap-combined.min.css' rel='stylesheet'>\n" +
             "<link rel='shortcut icon' href='https://www.memcachier.com/wp-content/uploads/2013/06/favicon.ico'>\n" +
           "</head>\n" +
           "<body>\n" +
             "<div id='wrap'><div class='container'>\n" +
             "<div class='page-header'><h1>MemCachier Fibonacci Example</h1></div>\n" +
              "<p class='lead'>This script computes a random digit of the Fibonacci " +
                 "sequence. Before computing, though, it checks to see if " +
                 "there's a cached value for the digit and serves the cached "+
                 "value if so.</p>\n");
 
     // actual cache / result values...
     sb.append("<p class='lead'>Digit: <span class='text-info'>" + digit + "</span></p>\n");
     sb.append("<p class='lead'>Value: <span class='text-info'>" + r.val + "</span></p>\n");
     sb.append("<p class='lead'>Was in cache? <span class='text-info'>" + r.cached + "</span></p>\n");
 
     // static html again...
     sb.append("</div></div>\n");
     sb.append("<div id='footer'><div class='container'>\n" +
         "<p class='muted credit'>Example by " +
           "<a href='http://www.memcachier.com'>" +
            "<img class='brand' src='https://www.memcachier.com/wp-content/uploads/2013/06/memcachier-small.png' alt='MemCachier'" +
              "title='MemCachier' style='padding-left:8px;padding-right:3px;padding-bottom:3px;'/>" +
             "MemCachier</a></p></div> </div>");
     sb.append("<script src='//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js'></script>");
     sb.append("</body></html>");
 
     resp.getWriter().print(sb.toString());
   }
 
   /**
   * Start the server.
   */
   public static void main(String[] args) throws Exception {
     Server server = new Server(Integer.valueOf(System.getenv("PORT")));
     ServletContextHandler context = new ServletContextHandler(
         ServletContextHandler.SESSIONS);
     context.setContextPath("/");
     server.setHandler(context);
     context.addServlet(new ServletHolder(new App()), "/*");
 
     // ==============================
     // Connect to MemCachier
     // ==============================
     createMemCachierClient();
     // ==============================
 
     server.start();
     server.join();
   }
 
   /**
   * A class for storing fibonacci results, indicating if they are cached or not.
   */
   private static class Result {
     public int val;
     public boolean cached;
 
     public Result(int val, boolean cached) {
       this.val = val;
       this.cached = cached;
     }
   }
 
   // ==============================
   // Memcachier connection
   // ==============================
   private static MemcachedClient mc;
   // ==============================
 
   /**
    * Create a memcachier connection
    */
   private static void createMemCachierClient() {
     try {
      AuthDescriptor ad = new AuthDescriptor(
          new String[] { "PLAIN" },
          new PlainCallbackHandler(System.getenv("MEMCACHIER_USERNAME"),
                                   System.getenv("MEMCACHIER_PASSWORD")));
      mc = new MemcachedClient(
          new ConnectionFactoryBuilder().setProtocol(
              ConnectionFactoryBuilder.Protocol.BINARY).setAuthDescriptor(ad).build(),
           AddrUtil.getAddresses(System.getenv("MEMCACHIER_SERVERS")));
     } catch (Exception ex) {
       System.err.println(
         "Couldn't create a connection, bailing out:\nIOException "
         + ex.getMessage());
     }
   }
 
   /**
   * Computes a fibonacci result of {@code n}, checking the cache for any answers.
   *
   * Note: You'd actually want to check the cache on recursive calls, but we
   * don't do that here for simplicity.
   */
   private static Result fib(int n) {
     try {
       Object inCache = mc.get("" + n);
       if (inCache == null) {
         return new Result(computeAndSet(n), false);
       } else {
         return new Result((Integer) inCache, true);
       }
     } catch (Exception e) {
       // if any exception we simply shutdown the existing one an reconnect.
       // XXX: This is a very hacky way of dealing with the problem, thought
       // needs to be put in to your application to handle failures gracefully.
       mc.shutdown();
       createMemCachierClient();
     }
 
     return new Result(fibRaw(n), false);
   }
 
   /**
    * Perform fibonacci compuatation and store result in cache.
    */
   private static int computeAndSet(int n) {
     int val = fibRaw(n);
     mc.set("" + n, 3600, val);
     return val;
   }
 
   /**
    * Perform actual fibonacci compuatation.
    */
   private static int fibRaw(int n) {
     if (n < 2) {
       return n;
     } else {
       return fibRaw(n - 1) + fibRaw(n - 2);
     }
   }
 
 }
 
 /* vim: set sw=2 ts=2 sts=2 expandtab: */
 
