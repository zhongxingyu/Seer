 
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import java.util.*;
 
 public class FileLatencyModel implements LatencyModel {
     
     private Map<Integer, Map<Double, Long>> latencyBuckets;
     private Map<Integer, Long> maxLatencies;
     private Map<Integer, Map<Double, Long>> cumulativeLatencyBuckets;
     private Map<Integer, Long> totalElements;
 
     public FileLatencyModel(String filename) throws IOException
     {
       latencyBuckets = new HashMap<Integer, Map<Double, Long>>();
       maxLatencies = new HashMap<Integer, Long>();
       cumulativeLatencyBuckets = new HashMap<Integer, Map<Double, Long>>();
       totalElements = new HashMap<Integer, Long>();
       initFromFile(filename);
     }
 
     private void initFromFile(String filename) throws IOException
     {
         BufferedReader br = new BufferedReader(new FileReader(filename));
         String line;
         while ((line = br.readLine()) != null) {
           // Parse the line
           String[] parts = line.split(" ");
           int numReplicas = Integer.parseInt(parts[0]);
           double latency = Double.parseDouble(parts[1]);
           long numElements = Long.parseLong(parts[2]);
           if (latencyBuckets.get(numReplicas) == null) {
             latencyBuckets.put(numReplicas, new TreeMap<Double, Long>());
           }
           latencyBuckets.get(numReplicas).put(latency, numElements);
         }
         // Build cumulativeLatencyBuckets
         for (Integer numReplicas : latencyBuckets.keySet()) {
             cumulativeLatencyBuckets.put(numReplicas, new TreeMap<Double, Long>());
             Long cumulativeNumElements = 0L;
             for (Double lat : latencyBuckets.get(numReplicas).keySet()) {
               cumulativeNumElements += latencyBuckets.get(numReplicas).get(lat);
               cumulativeLatencyBuckets.get(numReplicas).put(lat, cumulativeNumElements);
             }
             totalElements.put(numReplicas, cumulativeNumElements);
             maxLatencies.put(numReplicas, Collections.max(latencyBuckets.get(numReplicas).values()));
         }
     }
 
     public double getLatencyPDF(int numReplicas, double t)
     {
       if (latencyBuckets.containsKey(numReplicas) && 
           latencyBuckets.get(numReplicas).containsKey(t)) {
         Double numElements = (double)latencyBuckets.get(numReplicas).get(t); 
         Double total = (double)totalElements.get(numReplicas);
         return  numElements/total; 
       }
       else {
         return 0.0;
       }
     }
 
     public double getLatencyCDF(int numReplicas, double t)
     {
      if (cumulativeLatencyBuckets.containsKey(numReplicas) && 
          cumulativeLatencyBuckets.get(numReplicas).containsKey(t)) {
        return (double)(cumulativeLatencyBuckets.get(numReplicas).get(t)) / 
          (double)(totalElements.get(numReplicas));
      }
      else if(maxLatencies.get(numReplicas) < t)
       {
           return 1.0;
       }
       else {
         return 0.0;
       }
     }
 
     public List<Double> getRange(int numReplicas)
     {
         return new ArrayList<Double>(latencyBuckets.get(numReplicas).keySet());
     }
 }
