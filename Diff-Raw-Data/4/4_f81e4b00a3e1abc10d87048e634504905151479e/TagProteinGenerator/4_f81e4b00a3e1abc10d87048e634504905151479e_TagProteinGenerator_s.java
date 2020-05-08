 package ru.spbau.bioinf.tagfinder;
 
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class TagProteinGenerator {
 
     private Configuration conf;
 
     private PrintWriter out;
 
 
     public TagProteinGenerator(Configuration conf) throws IOException{
         this.conf = conf;
         out = ReaderUtil.createOutputFile(new File("tags.txt"));
     }
 
     public static void main(String[] args) throws Exception {
         Configuration conf = new Configuration(args);
         TagProteinGenerator generator = new TagProteinGenerator(conf);
         generator.generateTags();
     }
 
     private void generateTags() throws Exception {
         List<Protein> proteins = conf.getProteins();
         Map<Integer,Integer> msAlignResults = conf.getMSAlignResults();
         Map<Integer, Scan> scans = conf.getScans();
         List<Integer> keys = new ArrayList<Integer>();
         keys.addAll(scans.keySet());
         Collections.sort(keys);
         List<String> sequences = new ArrayList<String>();
         for (Protein protein : proteins) {
             sequences.add(protein.getSimplifiedAcids());
         }
 
 
         for (int key : keys) {
             Scan scan = scans.get(key);
             int scanId = scan.getId();
             int matchedProteinId = - 1;
 
             if (msAlignResults.containsKey(scanId)) {
                 matchedProteinId = msAlignResults.get(scanId);
             }
 
             List<Peak> peaks = scan.createSpectrumWithYPeaks(PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan));
             GraphUtil.generateEdges(conf, peaks);
             generateFiveAcidsTags(peaks, sequences, matchedProteinId, scan.getId());
         }
         out.close();
     }
 
     private Set<String> used = new HashSet<String>();
 
     public void generateFiveAcidsTags(List<Peak> peaks, List<String> sequences, int matchedProteinid, int scanId) {
         used.clear();
         for (Peak peak : peaks) {
             generateFiveAcidsTags(peak, "", sequences, matchedProteinid, scanId);
         }
     }
 
     public void generateFiveAcidsTags(Peak peak, String prefix, List<String> sequences, int matchedProteinid, int scanId) {
        if (prefix.length() == 5) {
             if (used.contains(prefix)) {
                 return;
             }
             for (int i = 0; i < sequences.size(); i++) {
                 String s = sequences.get(i);
                 if (s.contains(prefix)) {
                     out.println(scanId + " " + matchedProteinid + " " + i + " " + prefix + " " + s.indexOf(prefix));
                 }
             }
             used.add(prefix);
         } else {
             for (Peak next : peak.getNext()) {
                 for (Acid acid : Acid.values()) {
                     if (acid.match(conf.getEdgeLimits(peak, next))){
                         generateFiveAcidsTags(next, prefix + acid.name(), sequences, matchedProteinid, scanId);
                     }
                 }
             }
 
         }
     }
 }
