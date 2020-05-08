 package org.dancres.peers.primitives;
 
 import com.ning.http.client.AsyncHttpClient;
 import org.dancres.peers.Peer;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.util.internal.ConcurrentWeakKeyHashMap;
 
 import java.net.URI;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  * InProcessPeer shares a single address and port with a number of other InProcessPeers (e.g. By sharing a single
  * webserver with a common URL base space and mapping to some subspace underneath the base).
  */
 public class InProcessPeer implements Peer {
     private final AsyncHttpClient _client;
     private final HttpServer _server;
     private final Timer _timer;
     private final ConcurrentMap<String, ServiceDispatcher> _dispatchers =
             new ConcurrentWeakKeyHashMap<String, ServiceDispatcher>();
     private final ConcurrentMap<Class, Service> _services = new ConcurrentHashMap<Class, Service>();
     private final URI _fullAddress;
    private final String _peerAddress;
 
     /**
      * @param aServer is the HttpServer to share in
      * @param aPeerAddress is the sub-space to occupy under the HttpServer's base URL - starting with a "/"
      */
     public InProcessPeer(HttpServer aServer, AsyncHttpClient aClient,
                          final String aPeerAddress, Timer aTimer) throws Exception {
         _server = aServer;
         _client = aClient;
         _timer = aTimer;
        _peerAddress = aPeerAddress;
         _fullAddress = new URI(_server.getBase().toString() + aPeerAddress);
 
         _server.add(aPeerAddress, new HttpServer.Handler() {
             public void process(HttpRequest aRequest, HttpResponse aResponse) {
                 String myServicePath =
                         aRequest.getUri().substring(aRequest.getUri().indexOf(aPeerAddress) + aPeerAddress.length());
 
                 for (Map.Entry<String, ServiceDispatcher> kv : _dispatchers.entrySet()) {
                     if (myServicePath.startsWith(kv.getKey())) {
                         kv.getValue().dispatch(myServicePath, aRequest, aResponse);
                         break;
                     }
                 }
             }
         });
     }
 
     public Timer getTimer() {
         return _timer;
     }
 
     public void stop() {
        _server.remove(_peerAddress);
         _timer.cancel();
     }
 
     public URI getURI() {
         return _fullAddress;
     }
 
     public String getAddress() {
         return _fullAddress.toString();
     }
 
     public AsyncHttpClient getClient() {
         return _client;
     }
 
     public Service find(Class aServiceClass) {
         return _services.get(aServiceClass);
     }
 
     public void add(Service aService) {
         if (_dispatchers.putIfAbsent(aService.getAddress(), aService.getDispatcher()) != null)
             throw new IllegalStateException("Already got a dispatcher rooted at: " + aService);
 
         _services.put(aService.getClass(), aService);
     }
 }
