 package com.atlassian.plugin.servlet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.List;
 
 public abstract class AbstractFileServerServlet extends HttpServlet
 {
     public static final String PATH_SEPARATOR = "/";
     public static final String RESOURCE_URL_PREFIX = "resources";
     public static final String SERVLET_PATH = "download";
     private static final Log log = LogFactory.getLog(AbstractFileServerServlet.class);
 
     protected final void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException
     {
         DownloadStrategy downloadStrategy = getDownloadStrategy(httpServletRequest);
         if (downloadStrategy == null)
         {
             httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "The file you were looking for was not found");
             return;
         }
 
         try
         {
             downloadStrategy.serveFile(httpServletRequest, httpServletResponse);
         }
         catch (DownloadException e)
         {
             log.error("Error while serving file", e);
            if (!httpServletResponse.isCommitted())
            {
                httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while serving file");
            }
         }
     }
 
     /**
      * Returns a list of {@link DownloadStrategy} objects in the order that they will be matched against.
      * The list returned should be cached as this method is called for every request.
      */
     protected abstract List<DownloadStrategy> getDownloadStrategies();
 
     private DownloadStrategy getDownloadStrategy(HttpServletRequest httpServletRequest)
     {
         String url = httpServletRequest.getRequestURI().toLowerCase();
         for (DownloadStrategy downloadStrategy : getDownloadStrategies())
         {
             if (downloadStrategy.matches(url))
             {
                 return downloadStrategy;
             }
         }
         return null;
     }
 }
