 package sibli;
 
 import java.util.*;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 
 //import org.apache.log4j.Logger;
 import java.util.logging.Logger;
 
import javax.jdo.Query;
import javax.jdo.PersistenceManager;

 import sibli.PingerAsync;
 
 import javax.jdo.Query;
 import javax.jdo.PersistenceManager;
 import sibli.Host;
 import sibli.PMF;
 
 /**
  * PingTask class.
  *
  */
 public class PingTask extends HttpServlet {
   private static final Logger LOG = Logger.getLogger(PingTask.class.getName());
 
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
   throws IOException {
 
 
     LOG.info("Ololo!");
     List<HashMap<String,Object>> hosts = PingerAsync.getSourcesDb();
 
 
 
 /*
     // Oh, Java. This means [{k: v}, {k: v}, {k: v}, ...]
     // List<HashMap<String,Object>>
     hosts = new ArrayList<HashMap<String,Object>>();
     HashMap<String,Object> host = null;
 
     PersistenceManager pm = PMF.get().getPersistenceManager();
 
     Query query = pm.newQuery(Host.class);
     query.setOrdering("updated desc");
 
     try {
         List<Host> results = (List<Host>) query.execute();
         if (!results.isEmpty()) {
             for (Host h : results) {
                 // ...
                 
                LOG.info( h.getUrl() + ", " + h.getAdded().toString() + ", " + h.getStatus().toString() );
             }
         } else {
             // ... no results ...
         }
     } finally {
         query.closeAll();
     }
 */
 
 
 
 
     /**
      * @todo Check for X-AppEngine-Cron: true request header.
      * If not exists, retun text.
      */
     resp.setContentType("text/plain");
     resp.getWriter().println("Task launched");
 
   } // doGet
 
 
 
 } // PingTask class
