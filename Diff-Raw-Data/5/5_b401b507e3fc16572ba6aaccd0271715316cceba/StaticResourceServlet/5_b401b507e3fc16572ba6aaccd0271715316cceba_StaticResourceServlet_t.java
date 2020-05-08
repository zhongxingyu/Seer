 package org.otherobjects.cms.servlet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.mortbay.jetty.MimeTypes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Fast servlet to serve static resources for the webapp. Static resources
  * may come from local project files or the classpath (such as those provided
  * by otherobjects and other plugins).
  * 
  * <p>TODO Check caching and gzip
  * <br>TODO Local file serving  - check not cached
  * <br>TODO Check content types
  * <br>TODO Adapt to mapped path correctly
  * <br>TODO Check this works in non-Jetty containers
  * 
  * @author rich
  */
 public class StaticResourceServlet extends HttpServlet
 {
     private final Logger logger = LoggerFactory.getLogger(StaticResourceServlet.class);
 
     private MimeTypes mimeTypes = new MimeTypes();
     
     private static final long serialVersionUID = 3970455238515527584L;
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
     {
         String path = req.getPathInfo();
 
         if (path == null)
             return;
 
         // FIXME Security check: so that configuration data can't be served
         if ((!path.contains("/static/") && !path.contains("/templates/")) || path.contains(".."))
             return;
 
         logger.info("Requested resource: {}", path);
 
         // Add cache header
         resp.addHeader("Cache-Control", "max-age=3600");
         resp.setContentType(mimeTypes.getMimeByExtension(path).toString());
        resp.setCharacterEncoding("UTF-8");
 
         // FIXME Is there a faster way of servig these?
         // FIXME Cache?
         InputStream in = getClass().getResourceAsStream(path);
         OutputStream out = resp.getOutputStream();
         try
         {
             byte buffer[] = new byte[2048];
             int len = buffer.length;
             while (true)
             {
                 len = in.read(buffer);
                 if (len == -1)
                     break;
                 out.write(buffer, 0, len);
             }
            out.flush();
         }
         catch (Exception e)
         {
             logger.error("Error sending static resource: " + path, e);
         }
         finally
         {
             if (in != null)
                 in.close();
         }
     }
 
 }
