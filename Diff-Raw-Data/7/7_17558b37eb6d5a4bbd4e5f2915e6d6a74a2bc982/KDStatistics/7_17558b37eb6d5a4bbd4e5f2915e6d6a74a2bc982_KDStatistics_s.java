 package ru.spbau.bioinf.tagfinder;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class KDStatistics {
 
     private static Configuration conf;
 
     public static void main(String[] args) throws Exception {
         conf = new Configuration(args);
         List<Protein> proteins = conf.getProteins();
         Map<Integer,Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         KDStatistics kdStat = new KDStatistics();
         Map<KD, Integer> stat = new HashMap<KD, Integer>();
         for (Scan scan : scans.values()) {
             int scanId = scan.getId();
             if (msAlignResults.containsKey(scanId)) {
                 String sequence = proteins.get(msAlignResults.get(scanId)).getSimplifiedAcids();
                 KD kd = kdStat.findKd(scan, sequence);
                 System.out.println(scanId + " " + kd.toString());
                 if (stat.containsKey(kd)) {
                     stat.put(kd, 1 + stat.get(kd));
                 } else {
                     stat.put(kd, 1);
                 }
             }
         }
         List<KD> values  = new ArrayList<KD>();
         values.addAll(stat.keySet());
         Collections.sort(values);
         for (KD value : values) {
             System.out.println(value.toString() + " - " + stat.get(value));
         }
     }
 
     public KD findKd(Scan scan, String sequence) {
         List<Peak> peaks = scan.getPeaks();
         Collections.sort(peaks);
         double[] edges = new double[Acid.acids.size()];
         int cur = 0;
         for (Acid a : Acid.acids.values()) {
             edges[cur] = a.getMass();
             cur++;
         }
 
         int n = peaks.size();
         for (int i = 0; i < n; i++) {
             peaks.get(i).setComponentId(i);
         }
 
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double[] limits = conf.getEdgeLimits(peak, next);
                 for (double edge : edges) {
                     if (match(limits, edge)) {
                         peak.addNext(next);
                         break;
                     }
                 }
             }
         }
 
         boolean done;
 
         do {
             done = true;
             for (Peak peak : peaks) {
                 if (peak.updateComponentId()) {
                     done = false;
                 }
             }
         } while (!done);
 
         int[] kValues = new int[n];
         for (Peak peak : peaks) {
             peak.setProcessed(false);
         }
 
         for (Peak peak : peaks) {
             searchK(kValues, 0, peak);
         }
 
         int k = 0;
 
         for (int kValue : kValues) {
             if (kValue > k) {
                 k = kValue;
             }
         }
 
         int d = 0;
 
         for (int i = 0; i < n; i++) {
             Peak peak =  peaks.get(i);
             if (kValues[peak.getComponentId()] == k) {
                 int nextD = getD(peak, sequence);
                 if (nextD > d) {
                     d = nextD;
                 }
             }
         }
 
         return new KD(k, d);
     }
 
     public static boolean match(double[] limits, double edge) {
         return limits[0] < edge && limits[1] > edge;
     }
 
     private void searchK(int[] kValues, int len, Peak peak) {
         if (peak.isProcessed()) {
             return;
         }
 
         int componentId = peak.getComponentId();
         if (kValues[componentId] <= len) {
             kValues[componentId] = len;
         }
 
         for (Peak next : peak.getNext()) {
             searchK(kValues, len + 1, next);
         }
 
         peak.setProcessed(true);
     }
 
     private int getD(Peak peak, String sequence) {
         int ans = 0;
         for (Peak next : peak.getNext()) {
             double[] limits = conf.getEdgeLimits(peak, next);
             for (Acid acid : Acid.values()) {
                 if (match(limits, acid.getMass())) {
                     String pst = acid.name();
                     int cur = sequence.indexOf(pst);
                     while (cur >=0) {
                         int nextAns = getD(next, sequence.substring(cur+1), 1);
                         if (nextAns > ans) {
                             ans = nextAns;
                         }
                         cur = sequence.indexOf(pst, cur + 1);
                     }
                 }
             }
         }
         return ans;
     }
 
     private int getD(Peak peak, String sequence, int matched) {
         if (sequence.length() == 0) {
             return matched;
         }
         int ans = matched;
         for (Peak next : peak.getNext()) {
             double[] limits = conf.getEdgeLimits(peak, next);
             Acid acid = Acid.getAcid(sequence.charAt(0));
             if (match(limits, acid.getMass())) {
                 int nextAns = getD(next, sequence.substring(1), matched + 1);
                 if (nextAns > ans) {
                     ans = nextAns;
                 }
             }
         }
 
         return ans;
     }
 }
