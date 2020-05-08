 package sibli;
 
 import java.util.*;
 import java.util.concurrent.*;
 
 //import java.text.DecimalFormat;
 
 //import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import java.io.IOException;
 
 //import org.apache.log4j.Logger;
 import java.util.logging.Logger;
 
 
 // Google App Engine specific
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 //import static com.google.appengine.api.urlfetch.FetchOptions.Builder.*;
 
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
  */
 public class PingerAsync {
     private static final Logger LOG = Logger.getLogger(PingerAsync.class.getName());
 
     /**
      * @todo This is used only in constructor, move there?
      */
     protected short maxConcurrentRequests = 10; // 100 for backend, 10 for frontend.
     private static final int maxEntitiesBatchPut = 500; // 500 recommended. // @todo use type short ?
 
 
     protected AsyncDatastoreService datastore = null;
     protected java.util.Iterator<Entity> hostsIterator = null;
 
     // Queue for hosts, contains max maxConcurrentRequests elements at a time.
     protected List<Entity> hostsQueue = null;
 
     /**
      * Represents hostsQueue host’s Future responses.
      *
      * @var HashMap
      */
     protected HashMap<Long, Future<HTTPResponse>> mapResponses = null;
     protected HashMap<Long, Long> mapResponseTimes = null;
 
     protected List<Entity> listHosts = null;
     protected List<Entity> listQueries = null;
 
     protected List<Future> listFutureBatchHostSaves = null;
     protected List<Future> listFutureBatchQuerySaves = null;
     protected int countBatchSavedEntities = 0; // increments by maxEntitiesBatchPut value
 
     /**
      * Request options.
      * fetchOptions and uaHeader defined as final only to improve runtime performance.
      */
     private static final String userAgent = "Opera/9.80 (Windows NT 6.1; U; ru) Presto/2.9.168 Version/11.52";
     private static final double requestTimeout = 5.1; // Seconds
     public static final FetchOptions fetchOptions = FetchOptions.Builder.allowTruncate().followRedirects().doNotValidateCertificate().setDeadline(requestTimeout);
     public static final HTTPHeader uaHeader = new HTTPHeader("User-Agent", userAgent);
     protected static final URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
 
 
     /**
      * Constructor.
      * @param String backend Backend instance name or null if run in frontend environment.
      */
     public PingerAsync(String backend) {
 
        if (backend != null) {
            this.maxConcurrentRequests = 100; // For backends 100 is good.
        }
 
         //LOG.info( "concurrences: " + String.valueOf(this.maxConcurrentRequests) ); // DEBUG
 
         this.countBatchSavedEntities = maxEntitiesBatchPut; // Important.
         this.hostsQueue = new ArrayList<Entity>(this.maxConcurrentRequests);
         Entity host = null;
 
         /**
          * Here will be saved Entities with acquired responses
          * to batch save in datastore by portions of maxEntitiesBatchPut size.
          */
         this.listHosts = new ArrayList<Entity>();
         this.listQueries = new ArrayList<Entity>();
 
         /**
          * Here will be saved Future datastore batch puts.
          */
         this.listFutureBatchHostSaves = new ArrayList<Future>();
         this.listFutureBatchQuerySaves = new ArrayList<Future>();
 
         /**
          * Get the Datastore Service
          * @todo Check throws here and below.
          */
         this.datastore = DatastoreServiceFactory.getAsyncDatastoreService();
 
         // The Query interface assembles a query.
         Query q = new Query("Host");
         // q.addSort("updated", Query.SortDirection.DESCENDING); // field is unindexed now
 
         // PreparedQuery contains the methods for fetching query results from the datastore.
         PreparedQuery pq = datastore.prepare(q);
 
         /**
          * For PreparedQuery.asIterator() results will be fetched asynchronously.
          * @see https://developers.google.com/appengine/docs/java/datastore/async#Async_Queries
          */
         this.hostsIterator = pq.asIterator();
 
         int i = 0;
 
         // Adding first 10 hosts to queue (maxConcurrentRequests is 10 by default).
         while (this.hostsIterator.hasNext() && i < this.maxConcurrentRequests) {
             host = this.hostsIterator.next();
             this.hostsQueue.add(host);
             i++;
         }
 
         this.mapResponses = new HashMap<Long, Future<HTTPResponse>>(this.hostsQueue.size(), (float) 1.25);
         this.mapResponseTimes = new HashMap<Long, Long>(this.hostsQueue.size(), (float) 1.25);
 
     } // constructor
 
     public static void main(String[] args) throws Exception {
         throw new Exception("This class should not be run in CLI mode.");
     } // main
 
 
     /**
      * Sends async request to provided Entity host’s URL and saves start time
      * and Future response in mapResponses.
      *
      * @param Entity host
      * @return Entity/Null Modified entity or null on error.
      */
     private Entity fetchQueue(Entity host) {
         String url = (String) host.getProperty("url");
         long id = host.getKey().getId();
 
         try {
             HTTPRequest request = new HTTPRequest(
                     new URL(url),
                     HTTPMethod.HEAD,
                     fetchOptions
             );
             request.setHeader(uaHeader);
 
             Future<HTTPResponse> responseFuture = fetcher.fetchAsync(request);
 
             this.mapResponseTimes.put( id, System.nanoTime() );
             this.mapResponses.put(id, responseFuture);
 
         } catch (MalformedURLException e) {
             // This should never happen. Can be thrown by new URL().
             LOG.warning(e.getMessage());
             return null;
         } catch (IOException e) {
             // This should never happen. Can be thrown by fetcher.fetchAsync(request).
             LOG.warning(e.getMessage());
             return null;
         }
 
         return host;
     } // fetchQueue
 
     /**
      * Pings provided url list and returns status code from response.
      */
     public void ping() {
         Entity h;
         Entity hQuery;
 
         ListIterator<Entity> it = this.hostsQueue.listIterator(0);
         // NB for non-GAE implementations: ArrayList is NOT synchronized structure!
         // Queuing first 10 hosts (maxConcurrentRequests is 10 by default):
         while (it.hasNext()) {
             fetchQueue(it.next());
         } // while
 
         Future<HTTPResponse> responseFuture = null;
 
         HTTPResponse response;
         URL finalUrl;
         int time;
         int code;
         long id;
         Key key;
         Date dtUpdated;
         int polledSize = 0;
 
         //long overallStart = System.nanoTime();
 
         /**
          * @todo Check this loop for bottlenecks, e.g. slow operations.
          */
         while (this.hostsQueue.size() > 0) {
 
             it = this.hostsQueue.listIterator(0);
             while (it.hasNext()) {
                 h = it.next();
                 key = h.getKey();
                 id = key.getId();
                 responseFuture = this.mapResponses.get(id);
 
                 if ( responseFuture.isDone() || responseFuture.isCancelled() ) {
                     time = (int) (( System.nanoTime() - this.mapResponseTimes.remove(id) ) / 1000000.0);
                     dtUpdated = new Date();
 
                     try {
                         response = responseFuture.get(); // Can throw some exceptions, see below.
                         finalUrl = response.getFinalUrl(); // final URL or null (if there was no redirects).
                         if (finalUrl != null) {
                             if ( !finalUrl.toString().equals( h.getProperty("finalurl") ) )
                                 h.setProperty( "finalurl", finalUrl.toString() );
                         }
                         code = response.getResponseCode();
                     } catch (ExecutionException e) {
                         if ( e.getCause().getClass().getName().equals("java.net.SocketTimeoutException") ) {
                             code = 598; // Timeout.
                         } else {
                             code = 599; // DNS could not be resolved.
                         }
                     } catch (CancellationException e) {
                         // This should never happen.
                         code = 666;
                         LOG.warning(e.getMessage());
                     } catch (java.lang.InterruptedException e) {
                         // This should never happen.
                         code = 667;
                         LOG.warning(e.getMessage());
                     }
 
                     // Saving
                     // It is important to set parent for this entity.
                     hQuery = new Entity("HostQuery", key);
                     hQuery.setProperty("executed", dtUpdated);
                     hQuery.setUnindexedProperty("status", code);
                     hQuery.setUnindexedProperty("time", time);
 
                     // HEAD method not supported.
                     if (code == 405) {
                         h.setUnindexedProperty("useget", true);
                     }
                     h.setProperty("status", code);
                     h.setUnindexedProperty("updated", dtUpdated);
 
                     this.listHosts.add(h);
                     this.listQueries.add(hQuery);
                     polledSize++; // Trick to not call listHosts.size() each time.
 
                     /**
                      * Synchronous saves will slow queue, so we need
                      * to use async datastore and Futures.
                      * Performance gain on local devserver is about 1-2 ms per 30 hosts save,
                      * but for App Engine servers (and HRD) gain is ~100 ms per every 30 hosts.
                      * Also we need batch saves to improve perfomance.
                      *
                      * @todo Check throws.
                      * @todo Can I subList into new List and clear old one to not spread it’s size over time?
                      */
                     if (polledSize >= this.countBatchSavedEntities) {
                         this.listFutureBatchHostSaves.add(
                             this.datastore.put(
                                 this.listHosts.subList(this.countBatchSavedEntities - maxEntitiesBatchPut, polledSize)
                             )
                         );
 
                         this.listFutureBatchQuerySaves.add(
                             this.datastore.put(
                                 this.listQueries.subList(this.countBatchSavedEntities - maxEntitiesBatchPut, polledSize)
                             )
                         ); // Future
 
                         this.countBatchSavedEntities += maxEntitiesBatchPut;
                     }
 
                     this.mapResponses.remove(id);
                     it.remove();
 
                     if ( this.hostsIterator.hasNext() ) {
                         it.add( fetchQueue(this.hostsIterator.next()) );
                     } // ? hostsIterator.hasNext
 
                 } // ? responseFuture.isDone
             } // while it.hasNext
         } // while hostsQueue.size > 0
 
         /**
          * Addind tail.
          * @todo Check throws.
          */
         this.listFutureBatchHostSaves.add(
             this.datastore.put(
                 this.listHosts.subList(this.countBatchSavedEntities - maxEntitiesBatchPut, polledSize)
             )
         ); // Future
 
         this.listFutureBatchQuerySaves.add(
             this.datastore.put(
                 this.listQueries.subList(this.countBatchSavedEntities - maxEntitiesBatchPut, polledSize)
             )
         ); // Future
 
 
         //long overallEnd = System.nanoTime();
         //String totalTime = ( new DecimalFormat("#.#####").format( (overallEnd - overallStart) / 1000000.0 ) ) + " ms";
 
         //long saveStart = System.nanoTime();
         for (int i = 0; i < listFutureBatchHostSaves.size(); i++) {
             try {
                 listFutureBatchHostSaves.get(i).get();
                 listFutureBatchQuerySaves.get(i).get();
             } catch (ExecutionException e) {
                 LOG.warning(e.getMessage());
             } catch (java.lang.InterruptedException e) {
                 LOG.warning(e.getMessage());
             }
         } // for
         //long saveEnd = System.nanoTime();
         //String saveTime = ( new DecimalFormat("#.#####").format( (saveEnd - saveStart) / 1000000.0 ) ) + " ms";
 
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
