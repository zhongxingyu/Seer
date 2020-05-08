 package edu.ucla.nesl.pact;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.util.Log;
 
 import de.jetsli.graph.reader.OSMReader;
 import de.jetsli.graph.routing.DijkstraBidirection;
 import de.jetsli.graph.routing.Path;
 import de.jetsli.graph.storage.Graph;
 import de.jetsli.graph.storage.Location2IDFullIndex;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * A service that provides a route between two points.
  *
  * Loads a map of Westwood (open-street-maps) and uses GraphHopper for routing.
  * See the companion {@link LocationRouteActivity} for an usage example.
  *
  * @author Kasturi Rangan Raghavan (kastur@gmail.com)
  */
 public class LocationRouteService extends Service {
 
   private static final String TAG = "LocationRouteService";
   final static int MSG_ROUTE = 0;
 
   Graph mGraph;
   Location2IDFullIndex mLocIndex;
   AtomicBoolean mLoaded;
 
 
   @Override
   public IBinder onBind(Intent intent) {
     return mMessenger.getBinder();
   }
 
   @Override
   public void onCreate() {
     super.onCreate();
     mLoaded = new AtomicBoolean(false);
 
     Thread t = new Thread("MapLoader") {
       public void run() {
         Log.d(TAG, "----------Trying to load map...");
         try {
           mGraph = loadWestwood();
         } catch (IOException ex) {
           Log.e(TAG, "onCreate: IOException. Cannot load map!");
         }
 
         mLocIndex = new Location2IDFullIndex(mGraph);
         Log.d(TAG, "----------SUCCESS! Loaded map.");
         mLoaded.set(true);
       }
     };
     t.start();
   }
 
   private Graph loadWestwood() throws IOException, FileNotFoundException {
     final String storageDirectory = Environment.getDataDirectory().getPath() + "/graph_storage";
     new File(storageDirectory).mkdir();
 
     final int storageSize = 5000000;
     OSMReader reader = new OSMReader(storageDirectory, storageSize);
 
     {
       InputStream inputStream = getAssets().open("westwood.osm");
       try {
         reader.preprocessAcceptHighwaysOnly(inputStream);
       } catch (XmlPullParserException ex) {
         System.err.println("Error!");
       }
     }
     {
       InputStream inputStream = getAssets().open("westwood.osm");
       reader.writeOsm2Graph(inputStream);
     }
 
     return reader.getGraph();
   }
 
   final Messenger mMessenger = new Messenger(new IncomingHandler());
   class IncomingHandler extends Handler {
     @Override
     public void handleMessage(Message msg) {
       switch (msg.what) {
         case LocationRouteService.MSG_ROUTE:
           if (mLoaded.get()) {
             handleRouteMessage(msg);
           } else {
             Log.e(TAG, "Map is not loaded yet! Sending empty route!");
             handleMessageWhenNotLoaded(msg);
           }
           break;
         default:
           super.handleMessage(msg);
       }
     }
   }
 
   private void handleMessageWhenNotLoaded(final Message msg) {
     Bundle data = msg.getData();
     double fromLat = data.getDouble("fromLat", 0.0);
     double fromLon = data.getDouble("fromLon", 0.0);
     double toLat = data.getDouble("toLat", 0.0);
     double toLon = data.getDouble("toLon", 0.0);
 
     double latitudes[] = new double[] {fromLat, toLat};
     double longitudes[] = new double[] {fromLon, toLon};
     int nodes[] = new int[] {-1, -1};
 
     Bundle bundle = new Bundle();
     bundle.putIntArray("nodes", nodes);
     bundle.putDoubleArray("latitudes", latitudes);
     bundle.putDoubleArray("longitudes", longitudes);
     sendRouteMessage(msg.replyTo, nodes, latitudes, longitudes);
   }
 
   private void handleRouteMessage(final Message msg) {
     Bundle data = msg.getData();
     double fromLat = data.getDouble("fromLat", 0.0);
     double fromLon = data.getDouble("fromLon", 0.0);
     double toLat = data.getDouble("toLat", 0.0);
     double toLon = data.getDouble("toLon", 0.0);
 
     int fromId = mLocIndex.findID(fromLat, fromLon);
     int toId = mLocIndex.findID(toLat, toLon);
 
    DijkstraBidirection router = new DijkstraBidirection(mGraph);
    Path path = router.calcPath(fromId, toId);
 
     double latitudes[] = new double[path.locations()];
     double longitudes[] = new double[path.locations()];
     int nodes[] = new int[path.locations()];
     for (int ii = 0; ii < path.locations(); ++ii) {
       final int node_id = path.location(ii);
       nodes[ii] = node_id;
       latitudes[ii] = mGraph.getLatitude(node_id);
       longitudes[ii] = mGraph.getLongitude(node_id);
     }
     sendRouteMessage(msg.replyTo, nodes, latitudes, longitudes);
   }
 
   private void sendRouteMessage(
       Messenger replyTo, int[] nodes, double[] latitudes, double[] longitudes) {
     Bundle bundle = new Bundle();
     bundle.putIntArray("nodes", nodes);
     bundle.putDoubleArray("latitudes", latitudes);
     bundle.putDoubleArray("longitudes", longitudes);
     Message responseMsg = Message.obtain(null, LocationRouteService.MSG_ROUTE);
     responseMsg.setData(bundle);
     try {
       replyTo.send(responseMsg);
     } catch (RemoteException ex) {
       Log.e(TAG, "handleMessage (MSG_ROUTE): RemoteException when trying to respond.");
     }
   }
 }
