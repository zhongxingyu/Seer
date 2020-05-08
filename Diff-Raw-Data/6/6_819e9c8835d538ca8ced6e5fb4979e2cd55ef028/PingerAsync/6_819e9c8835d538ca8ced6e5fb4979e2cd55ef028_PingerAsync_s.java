 package sibli;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 import java.text.DecimalFormat;
 
 //import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.io.IOException;
 
 //import org.apache.log4j.Logger;
 import java.util.logging.Logger;
 
 
 // Google App Engine specific
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 import static com.google.appengine.api.urlfetch.FetchOptions.Builder.*;
 import com.google.appengine.api.urlfetch.FetchOptions;
 
 import com.google.appengine.api.urlfetch.HTTPMethod;
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.HTTPHeader;
 
 //import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.AsyncDatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Key;
 //import com.google.appengine.api.datastore.KeyFactory;
 
 
 /**
  * PingerAsync class.
  *
  */
 public class PingerAsync {
   private static final Logger LOG = Logger.getLogger(PingerAsync.class.getName());
   
   private static final double requestTimeout = 5.0; // Seconds
   private static final int maxConcurrentRequests = 10;
   private static final String userAgent = "Opera/9.80 (Windows NT 6.1; U; ru) Presto/2.9.168 Version/11.52";
 
   protected AsyncDatastoreService datastore = null;
   protected java.util.Iterator<Entity> hostsIterator = null;
   
   /**
    * @var ArrayList
    */
   protected ArrayList<Entity> hostsQueue = null;
 
   /**
    * Represents hostsQueue host’s Future responses.
    * @var HashMap
    */
   protected HashMap<Long, Future<HTTPResponse>> mapResponses = null;
   
   protected ArrayList<Future<Key>> listHostsPolled = null;
   protected ArrayList<Future<Key>> listQueuesPolled = null;
 
 
   public PingerAsync()
   {
     /**
      * @todo Check throws.
      */
 
     // Oh, Java. This means [{k: v}, {k: v}, {k: v}, ...]
     this.hostsQueue = new ArrayList<Entity>();
     Entity host = null;
 
     /**
      * HashSet have better performance for multiple insertions and deletions
      * but it does not preserve elements ordering.
      */
     this.listHostsPolled = new ArrayList<Future<Key>>();
     this.listQueuesPolled = new ArrayList<Future<Key>>();
 
     // Get the Datastore Service
     this.datastore = DatastoreServiceFactory.getAsyncDatastoreService();
 
     // The Query interface assembles a query.
     Query q = new Query("Host");
     q.addSort("updated", Query.SortDirection.DESCENDING);
 
     // PreparedQuery contains the methods for fetching query results from the datastore.
     PreparedQuery pq = datastore.prepare(q);
 
     // For PreparedQuery.asIterator() results will be fetched asynchronously.
     // https://developers.google.com/appengine/docs/java/datastore/async#Async_Queries
     this.hostsIterator = pq.asIterator();
 
     int i = 0;
 
     // Adding first 10 hosts to queue (maxConcurrentRequests is 10 by default).
     while (this.hostsIterator.hasNext() && i < maxConcurrentRequests) {
       host = this.hostsIterator.next();
       this.hostsQueue.add(host);
       i++;
     }
 
     this.mapResponses = new HashMap<Long, Future<HTTPResponse>>( this.hostsQueue.size(), (float) 1.25 );
 
   } // constructor
 
   public static void main(String[] args)
   {
     System.out.println( "Error: This class should not be run in CLI mode." );
   } // main
 
   /**
    *
    * @todo ? Use FetchOptions.Builder.allowTruncate().followRedirects().doNotValidateCertificate().setDeadline(requestTimeout);
    * (check imports beforehand)
    */
   private final FetchOptions fetchOptions = allowTruncate().followRedirects().doNotValidateCertificate().setDeadline(requestTimeout);
   private final HTTPHeader uaHeader = new HTTPHeader("User-Agent", userAgent);
   private final URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
 
   /**
    * Sends async request to provided Entity host’s URL and saves start time
    * and Future response in mapResponses.
    *
    * @param Entity host
    * @return Entity/Null Modified entity or null on error.
    */
   private Entity fetchQueue(Entity host)
   {
     String url = (String) host.getProperty("url");
 
     try {
       HTTPRequest request = new HTTPRequest(
           new URL(url),
           HTTPMethod.HEAD,
           fetchOptions
       );
       request.setHeader( uaHeader );
 
       Future<HTTPResponse> responseFuture = fetcher.fetchAsync(request);
       long timeStart = System.nanoTime();
 
       host.setUnindexedProperty("timeStart", timeStart);
       this.mapResponses.put( host.getKey().getId(), responseFuture );
 
     } catch (MalformedURLException e) {
       // This should never happen. Can be throwed by new URL().
       LOG.warning( e.getMessage() );
       return null;
     } catch (IllegalArgumentException e) {
       // This should never happen. Can be throwed by h.setUnindexedProperty().
       LOG.warning( e.getMessage() );
       return null;
     } catch (IOException e) {
       // This should never happen. Can be throwed by fetcher.fetchAsync(request).
       LOG.warning( e.getMessage() );
       return null;
     }
 
     return host;
   } // fetchQueue
 
   /**
    * Pings provided url list and returns status code from response.
    *
    */
  public String ping() //throws InterruptedException, ExecutionException
   {
     long timeStart;
     long timeEnd;
     Entity h;
     Entity hostQueue;
 
     ListIterator<Entity> it = this.hostsQueue.listIterator(0);
     // NB for non-GAE implementations: ArrayList is NOT synchronized structure!
     // Queuing first 10 hosts (maxConcurrentRequests is 10 by default):
     while ( it.hasNext() ) {
       h = fetchQueue( it.next() );
       //LOG.info( "Host: " + h.toString() );      
       if ( h != null ) { // if ( h.getClass().getName().equals("Entity") )
          // Overwrite previous value, new fields added.
         it.set(h);
       }
     } // while
 
     Future<HTTPResponse> responseFuture = null;
 
     h = null;
     Entity nextHost;
     HTTPResponse response = null;
     URL finalUrl;
     double time = 0;
     int code = 0;
     long id = 0;
 
 
     long overallStart = System.nanoTime();
 
     /**
      * @todo Check this loop for bottlenecks, e.g. slow operations.
      */
     while ( this.hostsQueue.size() > 0 ) {
 
       it = this.hostsQueue.listIterator(0);
       while ( it.hasNext() ) {
         h = it.next();
         id = h.getKey().getId();
         responseFuture = this.mapResponses.get(id);
         if ( responseFuture.isDone() || responseFuture.isCancelled() ) {
           timeEnd = System.nanoTime();
           timeStart = Long.parseLong( h.getProperty("timeStart").toString() ) ;
           time = (timeEnd - timeStart) / 1000000.0;
           h.removeProperty("timeStart"); // This is important
           
           try {
             response = responseFuture.get(); // Can throw some exceptions, see below.
             finalUrl = response.getFinalUrl(); // final URL or null if was no redirets.
             if (finalUrl != null) {
               h.setProperty( "finalurl", finalUrl.toString() );
             }
             code = response.getResponseCode();
           } catch (ExecutionException e) {
            // java.net.SocketTimeoutException
            if ( e.getClass().getName().equals("SocketTimeoutException") ) {
               code = 598;
             } else {
               // In most cases this means that DNS could not be resolved.
               code = 599;
             }
             LOG.info( e.getMessage() );
           } catch (CancellationException e) {
             // This should never happen.
             code = 666;
             LOG.warning( e.getMessage() );
           } catch (java.lang.InterruptedException e) {
             // This should never happen.
             code = 667;
             LOG.warning( e.getMessage() );
           }
 
           /*
           LOG.info(h.getProperty("url").toString() + "\n"
                    + (new DecimalFormat("#.#####").format(time))
                    + " ms \t CODE: " + String.valueOf(code));
            */
 
           // Saving
           hostQueue = new Entity( "HostQuery", h.getKey() );
           hostQueue.setProperty( "host", h.getKey() ); // This actually is not needed if parent is present.
           hostQueue.setProperty( "executed", new Date() );
           hostQueue.setProperty("status", code);
           hostQueue.setProperty("time", time);
 
           h.setProperty("status", code);
           h.setProperty( "updated", new Date() );
 
           /**
            * Synchronous saves will slow queue, so we need
            * to use async datastore and Futures.
            * Performance gain on local devserver is about 1-2 ms per 30 hosts save,
            * but for App Engine servers and HRD gain is ~100 ms per every 30 hosts.
            */
           listHostsPolled.add( this.datastore.put(h) );
           listQueuesPolled.add( this.datastore.put(hostQueue) );
 
           this.mapResponses.remove( h.getKey().getId() );
           it.remove();
           
           if ( this.hostsIterator.hasNext() ) {
             nextHost = fetchQueue( this.hostsIterator.next() );
             if ( nextHost != null ) {
               it.add(nextHost); //this.hostsQueue.add(nextHost);
             }
           } // ? hostsIterator.hasNext
 
         } // ? responseFuture.isDone
       } // while it.hasNext
     } // while hostsQueue.size > 0
 
 
     //long overallEnd = System.nanoTime();
     //String totalTime = ( new DecimalFormat("#.#####").format( (overallEnd - overallStart) / 1000000.0 ) ) + " ms";
 
     //long saveStart = System.nanoTime();
     for (int i = 0; i < listHostsPolled.size(); i++) {
       try {
         listHostsPolled.get(i).get();
         listQueuesPolled.get(i).get();
       } catch (ExecutionException e) {
         LOG.warning( e.getMessage() );
       } catch (java.lang.InterruptedException e) {
         LOG.warning( e.getMessage() );
       }
     } // for
     //long saveEnd = System.nanoTime();
     //String saveTime = ( new DecimalFormat("#.#####").format( (saveEnd - saveStart) / 1000000.0 ) ) + " ms";
     return "OK";
 
   } // ping
 
 } // PingerAsync class
 
 /**
  * @todo Add indexes to datastore:
  * https://developers.google.com/appengine/docs/java/datastore/queries?hl=en-US#Introduction_to_Indexes
  * 
  */
 
 /**
  Useful links:
 
  http://code.google.com/intl/en/appengine/docs/java/urlfetch/overview.html
  http://code.google.com/appengine/docs/java/javadoc/com/google/appengine/api/urlfetch/package-summary.html
 
  http://ikaisays.com/2010/06/29/using-asynchronous-urlfetch-on-java-app-engine/
 
  */
