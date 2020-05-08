 package ru.spbau.bioinf.pfind;
 
 import java.util.HashMap;
 import java.util.Map;
 import ru.spbau.bioinf.tagfinder.Protein;
 import ru.spbau.bioinf.tagfinder.Scan;
 
 public class MatchCandidate implements Comparable<MatchCandidate> {
     
     private Scan scan;
     private Protein protein;
 
 
     public MatchCandidate(Scan scan, Protein protein) {
         this.scan = scan;
         this.protein = protein;
         for (Scoring scoring : Scoring.values()) {
             scores.put(scoring, scoring.getScore(scan, protein));
         }
     }
 
     private Map<Scoring, Double> scores = new HashMap<Scoring, Double>();
 
     public boolean isValid() {
         if (protein.getBEnds()[protein.getBEnds().length - 1] > 2 * scan.getPrecursorMass()) {
             return false;
         }
         return getMaxScore()* Math.sqrt(scan.getPeaks().size()) > 10;
     }
 
     public Scan getScan() {
         return scan;
     }
 
     public Protein getProtein() {
         return protein;
     }
 
     public double getMaxScore() {
         double ans = 0;
         for (Double v : scores.values()) {
             if (v > ans) {
                 ans = v;
             }
         }
         return ans;
     }
     public int compareTo(MatchCandidate o) {
         double diff = getMaxScore() - o.getMaxScore();
         if (diff < 0) {
             return 1;
         } else if (diff > 0) {
             return -1;
         }
         return 0;
     }
 }
