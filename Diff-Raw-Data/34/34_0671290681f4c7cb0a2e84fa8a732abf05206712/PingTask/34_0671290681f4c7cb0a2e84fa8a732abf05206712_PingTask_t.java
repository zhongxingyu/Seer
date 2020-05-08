 package sibli;
 
 import java.util.*;
 
 import java.io.IOException;
 import javax.servlet.http.*;
 
 //import org.apache.log4j.Logger;
 import java.util.logging.Logger;
 
 import sibli.PingerAsync;
 
/*
 import javax.jdo.Query;
 import javax.jdo.PersistenceManager;
 import sibli.Host;
 import sibli.PMF;
*/

// For statistics.
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

 
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
                 
                LOG.info( h.getUrl() + ", " + h.getAdded().toString() + ", " + String.valueOf( h.getStatus() ) );
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
 
    // Get statistics (https://developers.google.com/appengine/docs/java/datastore/stats):
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery globalStat = datastore.prepare( new Query("__Stat_PropertyType_Kind__") );
    for ( Entity stat : globalStat.asIterable() ) {

      resp.getWriter().println( stat.getProperty("kind_name") + " --- " + stat.getProperty("property_type") + " --- " + stat.getProperty("entity_bytes") );

    }
   } // doGet
 
 
 
 } // PingTask class
