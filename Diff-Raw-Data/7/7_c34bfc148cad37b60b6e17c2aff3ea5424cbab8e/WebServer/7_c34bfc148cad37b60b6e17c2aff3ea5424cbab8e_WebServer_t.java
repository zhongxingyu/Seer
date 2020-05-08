 package fr.ybo;
 
 
 import fr.ybo.cron.UpdateServer;
import fr.ybo.util.GetTv;
 import fr.ybo.web.DataServer;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.mortbay.jetty.handler.HandlerList;
 import org.mortbay.jetty.handler.ResourceHandler;
 import org.mortbay.resource.Resource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import sun.misc.Signal;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class WebServer extends AbstractHandler {
 
     private DataServer dataServer = new DataServer();
     private UpdateServer updateServer = new UpdateServer();
 
     private static final Logger logger = LoggerFactory.getLogger(WebServer.class);
 
 
     @Override
     public void handle(String s, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, int i) throws IOException, ServletException {
         String path = httpServletRequest.getPathInfo();
 
         if (path.startsWith("/data/")) {
             dataServer.doGet(httpServletRequest, httpServletResponse);
 
         } else if (path.startsWith("/cron/update")) {
             updateServer.doGet(httpServletRequest, httpServletResponse);
         }
     }
 
     public static void main(String[] args) throws Exception {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
         System.err.println(sdf.format(new Date()) + ":ybo-tv-server starting");
         int defaultPort = 9080;
         if (args.length == 1) {
             defaultPort = Integer.parseInt(args[0]);
         }
 
        logger.info("Chargement du cache");
        String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        GetTv.updateFromCron(currentDate);
        logger.info("Fin de chargement du cache");

         Server server = new Server(defaultPort);
 
         Signal.handle(new Signal("TERM"), new StopHandler(server));
 
         ResourceHandler resourceHandler = new ResourceHandler();
         Resource publicResources = Resource.newClassPathResource("/public");
         resourceHandler.setBaseResource(publicResources);
         resourceHandler.setWelcomeFiles(new String[]{"index.html"});
 
         HandlerList handlers = new HandlerList();
         handlers.setHandlers(new Handler[]{new WebServer(), resourceHandler});
         server.setHandler(handlers);
 
         server.start();
         server.join();
 
         System.err.println(sdf.format(new Date()) + ":ybo-tv-server stopped");
     }
 }
