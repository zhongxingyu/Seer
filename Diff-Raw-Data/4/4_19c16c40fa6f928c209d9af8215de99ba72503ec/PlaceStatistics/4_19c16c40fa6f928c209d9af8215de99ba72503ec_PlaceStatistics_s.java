 package ru.spbau.bioinf.tagfinder.stat;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import ru.spbau.bioinf.tagfinder.Acid;
 import ru.spbau.bioinf.tagfinder.Configuration;
 import ru.spbau.bioinf.tagfinder.GraphUtil;
 import ru.spbau.bioinf.tagfinder.Peak;
 import ru.spbau.bioinf.tagfinder.Protein;
 import ru.spbau.bioinf.tagfinder.Scan;
 import ru.spbau.bioinf.tagfinder.ValidTags;
 
 public class PlaceStatistics {
 
     public static final double TEN_PPM = 0.00001d;
 
     private static NumberFormat df = NumberFormat.getInstance();
 
     static {
         df.setMaximumFractionDigits(2);
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         List<Protein> proteins = conf.getProteins();
         Map<Integer, Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         Set<Integer> usedProteins = new HashSet<Integer>();
         //Map<Integer, List<Peak>> msAlignPeaks = conf.getMSAlignPeaks();
         Map<Integer, Map<Double, String>> msAlignDatas = conf.getMSAlignData();
         double[] global = new double[100];
         int[] count = new int[100];
         //keys.clear(); keys.add(502);
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
             if (msAlignResults.containsKey(scanId)) {
                 Integer proteinId = msAlignResults.get(scanId);
                 if (usedProteins.contains(proteinId)) {
                     continue;
                 }
                 usedProteins.add(proteinId);
 
                 String sequence = proteins.get(proteinId).getSimplifiedAcids();
                 List<Peak> peaks = //msAlignPeaks.get(scanId);
                                     //scan.createSpectrumWithYPeaks(0);
                                     scan.createStandardSpectrum();
 
                 List<Peak> yPeaks = new ArrayList<Peak>();
                 Collections.sort(peaks);
                 for (Peak peak : peaks) {
                     yPeaks.add(peak.getYPeak(scan.getPrecursorMass()));
                 }
                 Collections.sort(yPeaks);
                 Map<Double, String> msAlignData = msAlignDatas.get(scanId);
                 int[][] stat = new int[100][3];
                 updateStatWithGaps(stat, conf, msAlignData, sequence, peaks, 1, scan.getPrecursorMass(), 0);
                 updateStatWithGaps(stat, conf, msAlignData, sequence, yPeaks, 1, scan.getPrecursorMass(), 1);
 
                 System.out.print(scanId + " " +  proteinId);
                 for (int i = 1; i < stat.length; i++) {
                     long total = stat[i][0];
                     if (total > 0) {
                         System.out.print(" " + stat[i][0]);
                         System.out.print(" " + stat[i][1]);
                         System.out.print(" " + stat[i][2]);
                         count[i]++;
                         global[i] += stat[i][2] *100d/stat[i][0];
                     } else {
                         break;
                     }
                 }
                 System.out.println();
             }
         }
         for (int i = 1; i < count.length; i++) {
             int n = count[i];
             if (n > 0) {
                 System.out.print( df.format(2 * global[i]/n) + " ");
             } else {
                 break;
             }
         }
         System.out.println();
     }
 
     private static void updateStatWithGaps(int[][] stat, Configuration conf, Map<Double, String> msAlignData, String sequence, List<Peak> peaks, int gap, double precursorMass, int type) {
         GraphUtil.generateGapEdges(conf, peaks, gap);
         for (Peak peak : peaks) {
             String match = null;
             for (Map.Entry<Double, String> entry : msAlignData.entrySet()) {
                 double limit = type == 0 ? TEN_PPM * peak.getValue() : precursorMass * 1.5 * TEN_PPM;
                 if (Math.abs(peak.getValue() - entry.getKey()) < limit) {
                     match = entry.getValue();
                     break;
                 }
             }
             HashSet<Integer> starts = new HashSet<Integer>();
             for (int i = 0; i < sequence.length() - 1; i++) {
                 starts.add(i);
             }
             processGappedTags(stat, conf, match, sequence, peak, gap, 0, starts);
         }
     }
 
     private static void processGappedTags(int[][] stat, Configuration conf, String match, String sequence, Peak peak, int gap, int len, Set<Integer> starts) {
         stat[len][0]++;
         if (starts.size() > 0) {
             stat[len][1]++;
         }
         if (match != null) {
             stat[len][2]++;
         }
         for (Peak next : peak.getNext()) {
             double[] limits = conf.getEdgeLimits(peak, next);
            Set<Integer> nextStarts = ValidTags.getNextStarts(sequence, starts, limits, gap);
             String nextMatch = null;
             if (match != null) {
                 for (int i = 1; i <= Math.min(match.length(), gap); i++) {
                     double mass = 0;
                     for (int j = 0; j < i; j++) {
                         Acid acid = Acid.getAcid(match.charAt(j));
                         if (acid == null) {
                             mass = -1;
                             break;
                         }
                         mass += acid.getMass();
                     }
                     if (limits[0] < mass && limits[1] > mass) {
                         nextMatch = match.substring(i);
                         break;
                     }
                 }
             }
             processGappedTags(stat, conf, nextMatch, sequence, next, gap, len + 1, nextStarts);
         }
     }
 }
