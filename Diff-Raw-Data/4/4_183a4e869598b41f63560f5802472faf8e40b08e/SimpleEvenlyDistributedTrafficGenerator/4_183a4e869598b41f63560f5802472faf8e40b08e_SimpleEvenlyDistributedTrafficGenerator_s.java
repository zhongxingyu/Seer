 package be.kuleuven.cs.gridlock.examples.csvgenerator;
 
 import be.kuleuven.cs.gridlock.configuration.Configuration;
 import be.kuleuven.cs.gridlock.examples.MatLabLoader;
 import be.kuleuven.cs.gridlock.simulation.api.NodeReference;
 import be.kuleuven.cs.gridlock.traffic.AbstractTrafficGenerator;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Kristof Coninx <kristof.coninx@student.kuleuven.be>
  */
 public class SimpleEvenlyDistributedTrafficGenerator extends AbstractTrafficGenerator {
 
     public static final String GENERATOR_MATRIX_LOCATION_KEY = "traffic.generator.matrix.file";
     private Configuration configuration;
 
     public SimpleEvenlyDistributedTrafficGenerator(Configuration config) {
         super(null, null, config);
         this.configuration = config;
     }
 
     @Override
     protected void generateHourlyTraffic(int hour, SortedMap<Long, List<VehicleInsertion>> insertions) {
         final long hourlyTimespan = 3600000;
         
         for (TrafficSummaryInstance tsi : readSummary()) {
             NodeReference startRef = tsi.getOrigin();
             NodeReference endRef = tsi.getDestination();
            int totalquota = tsi.getValue();
            
            int hourlyQuota = getHourLimit() == 0 ? 1 : totalquota / getHourLimit();
             long startHour = hour * 60 * 60 * 1000;
             long delta = hourlyQuota == 0 ? hourlyTimespan : hourlyTimespan / hourlyQuota;
             
             for (int i = 0; i < hourlyQuota; i++) {
                 long time = (i * delta) + startHour;
                 List<VehicleInsertion> list = new ArrayList<VehicleInsertion>();
                 list.add(new VehicleInsertion(startRef, endRef));
                 insertions.put(time, list);
             }
         }
     }
 
     private Collection<TrafficSummaryInstance> readSummary() {
         String nextLine = null;
         List<TrafficSummaryInstance> toReturn = new ArrayList<TrafficSummaryInstance>();
         
         try {
             String summaryLocation = configuration.getString(GENERATOR_MATRIX_LOCATION_KEY, null);
             BufferedReader linkReader = new BufferedReader(new InputStreamReader(MatLabLoader.class.getResourceAsStream(summaryLocation)));
             
             while ((nextLine = linkReader.readLine()) != null) {
                 Map<String, Object> attributes = new HashMap<String, Object>();
                 String[] pieces = nextLine.split(",", 3);
 
                 NodeReference origin = new NodeReference(Long.parseLong(pieces[0]));
                 NodeReference destination = new NodeReference(Long.parseLong(pieces[1]));
                 Integer vehicles = Integer.parseInt(pieces[2]);
 
                 toReturn.add(new TrafficSummaryInstance(origin, destination, vehicles));
             }
         } catch (IOException ex) {
             Logger.getLogger(MatLabLoader.class.getName()).log(Level.SEVERE, null, ex);
         }
         return toReturn;
     }
 
     protected static class TrafficSummaryInstance {
 
         public final NodeReference origin;
         public final NodeReference destination;
         public final int value;
 
         public TrafficSummaryInstance(NodeReference origin, NodeReference destination, int value) {
             this.origin = origin;
             this.destination = destination;
             this.value = value;
         }
 
         public NodeReference getDestination() {
             return destination;
         }
 
         public NodeReference getOrigin() {
             return origin;
         }
 
         public int getValue() {
             return value;
         }
     }
 }
