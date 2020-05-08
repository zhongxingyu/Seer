 package fr.gimmick.velib;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 
 /** Heartbeat prevents the (cloud) server hibernation by simulating requests from the outside */
 @WebFilter("/*")
 public final class Heartbeat implements Filter {
 
     private static final Logger LOG = Logger.getLogger(Heartbeat.class.getName());
     private ScheduledExecutorService heart;
     private URL url;
 
     private static URL rootUrl(HttpServletRequest req) throws MalformedURLException {
 
         String rootUrl = req.getRequestURL().toString();
         int endIndex = rootUrl.length();
 
         // Strip out pathInfo
         if(req.getPathInfo() != null && rootUrl.endsWith(req.getPathInfo())) {
             endIndex -= req.getPathInfo().length();
             rootUrl = rootUrl.substring(0, endIndex);
         }
         // Strip out servletPath
         if(rootUrl.endsWith(req.getServletPath())) {
             endIndex -= req.getServletPath().length();
             rootUrl = rootUrl.substring(0, endIndex);
         }
         // Strip out end slashes
         while(rootUrl.endsWith("/")) {
             --endIndex;
             rootUrl = rootUrl.substring(0, endIndex);
         }
 
         return new URL(rootUrl);
     }
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
         heart = Executors.newSingleThreadScheduledExecutor();
         heart.scheduleAtFixedRate(new Runnable() {
             @Override
             public void run() {
                if(url != null) {
                     HttpURLConnection beat = null;
                     try {
                         beat = (HttpURLConnection) url.openConnection();
                     } catch (IOException e) {
                        LOG.severe(e.getMessage());
                         e.printStackTrace(System.err);
                     } finally {
                         if (beat != null) {
                             LOG.info("Heartbeat: " + url.toString());
                             beat.disconnect();
                         }
                     }
                 }
             }
         }, 1, 1, TimeUnit.HOURS);
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         if(url == null && request instanceof HttpServletRequest) {
             url = rootUrl((HttpServletRequest) request);
         }
         chain.doFilter(request, response);
     }
 
     @Override
     public void destroy() {
         heart.shutdown();
     }
 }
