 package ma.eugene.nextbus;
 
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.LinkedList;
 
 import org.xml.sax.SAXException;
 
 public abstract class NextBus {
     /** Live XML feed API. */
     private API api;
 
     /** For mapping stopIds to unique stops. */
     private HashMap<Integer, BusStop> stops = new HashMap<Integer, BusStop>();
 
     /**
      * Subclasses should implement this method which returns the distance in
      * meters to a given BusStop.
      *
      * @param bs the BusStop
      * @return the distance in meters
      */
     protected abstract float stopDistance(BusStop bs);
 
     /**
      * @param agency the NextBus agency
      */
     public NextBus(String agency) throws IOException, SAXException {
         api = new API(agency);
     }
 
     /**
      * A set of lines, where each line contains a BusStop.
      *
      * @return the representation of this NextBus instance as a String.
      */
     public String toString() {
         StringBuilder sb = new StringBuilder();
         for (BusStop bs : stops.values())
             sb.append(bs + "\n");
         if (sb.charAt(sb.length()-1) == '\n')
             sb.deleteCharAt(sb.length()-1);
         return sb.toString();
     }
 
     /**
     * Save my representation as a String to a file.
      *
      * @param pathToDump the path where the dump file should be written
      */
     public void saveStops(String pathToDump) throws IOException {
         writeToFile(new File(pathToDump), toString());
     }
 
     /**
      * Query the NextBus server for stops.
      *
      * This is much slower and more expensive to call, so you should call
      * getStopsLocal() unless cold start or cached stops are out of date.
      */
     public void getStopsRemote() throws IOException, SAXException {
         for (String route : api.getRouteList()) {
             for (RouteConfigInfo info : api.getRouteConfig(route)) {
                 BusStop stop = new BusStop(api, info);
                 stops.put(stop.getStopId(), stop);
             }
         }
     }
 
     /**
      * Read stops from local dump file.
      *
      * @param pathToDump the path where the dump file exists
      */
     public void getStopsLocal(String pathToDump) throws IOException {
         File dumpFile = new File(pathToDump);
         String s = readFromFile(dumpFile);
         for (String line : s.split("\n")) {
             BusStop bs = new BusStop(api, line);
             stops.put(bs.getStopId(), bs);
         }
     }
 
     /**
      * Get all BusStops within range.
      *
      * @param radius distance in meters
      * @return all BusStops within range in meters.
      */
     public List<BusStop> getStopsInRange(int radius) {
         List<BusStop> results = new LinkedList<BusStop>();
         for (BusStop bs : stops.values()) {
             if (stopDistance(bs) < radius)
                 results.add(bs);
         }
         return results;
     }
 
     /**
      * Get all Predictions within range.
      *
      * @param radius distance in meters
      * @return all Predictions arriving at stops within range in meters.
      */
     public List<Prediction> getPredictionsInRange(int radius)
                                     throws IOException, SAXException {
         List<Prediction> predictions = new LinkedList<Prediction>();
         for (BusStop bs : getStopsInRange(radius)) {
             for (Prediction pred : bs.getPredictions())
                 predictions.add(pred);
         }
         return predictions;
     }
 
     private void writeToFile(File file, String s) throws IOException {
         BufferedWriter out = new BufferedWriter(new FileWriter(file));
         try {
             out.write(s, 0, s.length());
         } finally {
             out.close();
         }
     }
 
     private String readFromFile(File file) throws IOException {
         StringBuilder sb = new StringBuilder();
         BufferedReader in = new BufferedReader(new FileReader(file));
         try {
             char[] buf = new char[1024];
             int bytesRead = 0;
             while ((bytesRead = in.read(buf)) >= 0)
                 sb.append(buf, 0, bytesRead);
         } finally {
             in.close();
         }
         return sb.toString();
     }
 }
