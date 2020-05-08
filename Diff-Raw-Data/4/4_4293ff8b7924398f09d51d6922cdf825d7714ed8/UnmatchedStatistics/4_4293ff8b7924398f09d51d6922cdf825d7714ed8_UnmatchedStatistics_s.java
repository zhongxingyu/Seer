 package ru.spbau.bioinf.tagfinder;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 public class UnmatchedStatistics {
 
     public static void main(String[] args) throws Exception {
         //tableSixteen();
         tableUnident();
     }
 
     public static void tableUnident() throws IOException {
         Configuration conf;
 
         conf = new Configuration(new String[]{});
         Map<Integer, Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Double> evalues = conf.getEvalues();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         List<Scan> unmatched = new ArrayList<Scan>();
         int total = 0;
         int good = 0;
         for (Scan scan : scans.values()) {
             if (scan.getPeaks().size() < 10) {
                 continue;
             }
             if (scan.getPrecursorMass() < 2500) {
                 continue;
             }
 
 
 
             total++;
             if (evalues.get(scan.getId()) > 0.0024) {
                 continue;
             }
 
             good++;
        }
         //System.out.println("total = " + total);
         //System.out.println("good = " + good);
         //System.out.println("msAlignResults = " + msAlignResults.keySet().size());
 
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
 
             if (scan.getPeaks().size() < 10) {
                 continue;
             }
             if (scan.getPrecursorMass() < 2500) {
                 continue;
             }
 
             if (evalues.containsKey(scanId) && evalues.get(scanId) < 0.0024) {
                 continue;
             }
 
             unmatched.add(scan);
 
         }
         //System.out.println(unmatched.size());
 
         printStat(conf, unmatched, "The number and percentage of unidentified spectra with a given maximum tag length~$k$, for all the observed tag lengths.", "unident-tags");
     }
 
     public static void tableSixteen() throws IOException {
         Configuration conf;
 
         List<Scan> mixed = new ArrayList<Scan>();
         conf = new Configuration(new String[]{}, UnmatchedScansGenerator.SHARED_MODE);
         mixed.addAll(conf.getScans().values());
         printStat(conf, mixed, "The number and percentage of candidate mixed spectra with a given maximum tag length $k$ in the reduced spectra, for all the observed tag lengths.", "mixed-tags");
     }
 
     private static void printStat(Configuration conf, List<Scan> unmatched, String caption, String label) {
         int[] stat = new int[100];
         int max = 0;
         int total = 0;
 
         for (Scan scan : unmatched) {
             List<Peak> peaks = scan.createSpectrumWithYPeaks(PrecursorMassShiftFinder.getPrecursorMassShiftForMoreEdges(conf, scan));
             GraphUtil.generateEdges(conf, peaks);
             Peak[] bestTag = GraphUtil.findBestTag(peaks);
             int v = bestTag.length - 1;
             stat[v]++;
             if (max < v) {
                 max = v;
             }
             total++;
         }
 
         System.out.println("\\begin{table}[h]\n" +
                 "\\vspace{3mm}\\\n" +
                 "{\\centering\n" +
                 "\\begin{center}\n" +
                 "\\begin{tabular}{|c|c|");
 
         for (int i = 0; i <= max; i++) {
             System.out.print("c|");
         }
 
         System.out.println("}\n" +
                 "  \\hline\n" +
                 "  \\multicolumn{2}{|c|}{} & \\multicolumn{ " + (max + 1) + "}{|c|}{$k$} \\\\\n" +
                 "  \\cline{3-" + (max  + 3)+ "}\n" +
                 "  \\multicolumn{2}{|c|}{} ");
         for (int i = 0; i <= max; i++) {
             System.out.print(" & " + i);
         }
 
         System.out.println("\\\\\n" +
                 "  \\hline\n" +
                 "  \\multirow{2}{*}{spectra} & \\#");
 
         for (int i = 0; i <= max; i++) {
             System.out.print(" & " + stat[i]);
         }
 
         System.out.println("\\\\\n" +
                 "   & \\%");
 
 
         for (int i = 0; i <= max; i++) {
             System.out.print(" & " + ValidTags.df.format(stat[i] * 100d / total));
         }
 
 
         System.out.println("\\\\\n" +
                 "  \\hline\n" +
                 "\\end{tabular}\n" +
                 "\\end{center}\n" +
                 "\\par}\n" +
                 "\\centering\n" +
                 "\\caption{" + caption + "}\n" +
                 "\\vspace{3mm}\n" +
                 "\\label{table:" + label + "}\n" +
                 "\\end{table}");
     }
 }
