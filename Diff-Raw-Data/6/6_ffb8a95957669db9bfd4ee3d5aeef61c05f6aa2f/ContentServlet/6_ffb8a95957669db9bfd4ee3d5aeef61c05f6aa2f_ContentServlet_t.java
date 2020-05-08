 package net.contextfw.web.commons.minifier;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.contextfw.web.application.ResourceCleaner;
 import net.contextfw.web.application.WebApplicationException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Inject;
 
 abstract class ContentServlet extends HttpServlet {
 
     private static final int SECOND = 1000;
 
     private static final long serialVersionUID = 1L;
     
     Logger logger = LoggerFactory.getLogger(ContentServlet.class);
     
     private ThreadLocal<SimpleDateFormat> format = new ThreadLocal<SimpleDateFormat>() {
         protected SimpleDateFormat initialValue() {
             return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
         }
     };
 
     private volatile String content;
     
     private final String host;
     
     private final String minifiedPath;
     
     private final String version;
     
     private final long started;
     
     private static final long EXPIRATION = 60 * 60 * 1000 * 8;
     
     private ResourceCleaner cleaner;
     
     protected ContentServlet(String host,
                              String minifiedPath,
                              long started,
                              String version) {
         this.host = host;
         this.started = started;
         this.version = version;
         this.minifiedPath = minifiedPath;
         this.modifiedSince = new Date(started);
     }
     
     protected URL getUrl(String src) {
         try {
             return new URL(host + src);
         } catch (MalformedURLException e) {
             throw new WebApplicationException(e);
         }
     }
     
     String getMinifiedPath() {
         return minifiedPath.replaceFirst("<version>", version);
     }
     
     private final Date modifiedSince;
     
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
             IOException {
         
         if (content == null) {
            synchronized (this) {
                if (content == null) {
                    cleaner.clean();        
                }
            }
         }
         
         resp.setContentType(getContentType());
         String modifiedHeader = req.getHeader("If-Modified-Since");
         Date mod = null;
         
         if (modifiedHeader != null) {
             try {
                 mod = format.get().parse(modifiedHeader);
             } catch (ParseException e) {
                 logger.error("Error while parsing", e);
             }
         }
 
         HttpServletResponse httpResponse = (HttpServletResponse) resp;
         httpResponse.setDateHeader("Expires", started + EXPIRATION);
         httpResponse.setDateHeader("Date", started);
         httpResponse.setDateHeader("Last-Modified", started+SECOND);
         httpResponse.setHeader("Cache-Control", "max-age=2246400, must-revalidate");
         httpResponse.setHeader("Pragma", "cache");
         
         if (content != null && mod != null && modifiedSince.before(mod)) {
             resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
         } else {
             resp.getWriter().print(content);
         }
     }
     
     protected abstract String getContentType();
 
     public void setContent(String content) {
         this.content = content;
     }
 
     @Inject
     public void setCleaner(ResourceCleaner cleaner) {
         this.cleaner = cleaner;
     }
 }
