 package ru.spbau.bioinf.tagfinder;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 public class ShiftEngine {
 
     public static final double EPSILON = 0.01;
 
     public static double[] getSpectrum(String sequence) {
         double[] ans = new double[sequence.length() + 1];
         ans[0] = 0;
         for (int i = 0; i < sequence.length(); i++) {
             ans[i + 1] = ans[i] + Acid.getAcid(sequence.charAt(i)).getMass();
         }
         return ans;
     }
 
     public static double[] getSpectrum(List<Peak> peaks, double precursorMass) {
         double[] ans = new double[peaks.size() * 2 + 1];
         ans[0] = 0;
         for (int i = 0; i < peaks.size(); i++) {
             Peak peak = peaks.get(i);
             ans[2 * i + 1] = peak.getMass();
             ans[2 * i + 2] = precursorMass - peak.getMass();
         }
         return merge(ans);
     }
 
     public static double[] getSpectrum(List<Peak> peaks) {
         double[] ans = new double[peaks.size() + 1];
         ans[0] = 0;
         for (int i = 0; i < peaks.size(); i++) {
             Peak peak = peaks.get(i);
             ans[i + 1] = peak.getValue();
         }
         return merge(ans);
     }
 
     public static double[] merge(double[] peaks) {
         if (peaks.length <= 1) {
             return peaks;
         }
         Arrays.sort(peaks);
 
         ArrayList<Double> a = new ArrayList<Double>();
         double prev = 0;
         int count = 0;
         double sum = 0;
         for (double s : peaks) {
             if (s - prev < EPSILON) {
                 count++;
                 sum += s;
                 prev = s;
             } else {
                 if (count > 0) {
                     a.add(sum / count);
                 }
                 count = 1;
                 prev = s;
                 sum = s;
             }
         }
         a.add(sum / count);
         double[] ans = new double[a.size()];
         for (int i = 0; i < ans.length; i++) {
             ans[i] = a.get(i);
         }
         return ans;
     }
 
     public static List<double[]> getRanges(double[] shifts) {
         List<double[]> ans = new ArrayList<double[]>();
        if (shifts.length == 0) {
            return ans;
        }
        Arrays.sort(shifts);
         double prev = shifts[0] - 100;
         int count = 0;
         double min = prev;
         for (double s : shifts) {
             if (s - prev < 0.1) {
                 count++;
                 prev = s;
             } else {
                 if (count >= 10) {
                     ans.add(new double[]{min, prev});
                 }
                 count = 1;
                 prev = s;
                 min = s;
             }
         }
         if (count > 10) {
             ans.add(new double[]{min, prev});
         }
         return ans;
     }
 
 
     public static List<Double> getShifts(List<Peak> peaks, double precursorMass, double[] spectrum) {
         List<Double> ans = new ArrayList<Double>();
         for (Peak peak : peaks) {
             for (double v : spectrum) {
                 ans.add(v - peak.getMass());
                 ans.add(v + peak.getMass() - precursorMass);
             }
         }
         return ans;
     }
 
     public static List<Double> getShifts(List<Peak> peaks, double[] spectrum) {
         List<Double> ans = new ArrayList<Double>();
         for (Peak peak : peaks) {
             for (double v : spectrum) {
                 ans.add(v - peak.getValue());
             }
         }
         return ans;
     }
 
     public static double getScore(double[] scan, double[] protein, double shift) {
         int score = 0;
         int i = 0;
         int j = 0;
         do {
             double diff = scan[j] - protein[i] + shift;
             if (diff < -0.1) {
                 j++;
             } else if (diff > 0.1) {
                 i++;
             } else {
                 score++;
                 i++;
                 j++;
             }
         } while (i < protein.length && j < scan.length);
         return score;
     }
 
     public static boolean contains(double[] spectrum, double... values) {
         return  contains(0.1, spectrum, values);
     }
 
     public static boolean contains(double epsilon, double[] spectrum, double... values) {
         for (double p : spectrum) {
             for (double v : values) {
                 if (Math.abs(p - v) < epsilon) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public static double[] getPositions(List<Peak> peaks) {
         double[] positions = new double[peaks.size()];
         for (int i = 0; i < positions.length; i++) {
             positions[i] = peaks.get(i).getValue();
         }
         positions = merge(positions);
         return positions;
     }
 
     public static double getBestShift(Configuration conf, Scan scan, double[] proteinSpectrum) {
         double precursorMass =  scan.getPrecursorMass() + PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan);
         List<Double> shiftsList = getShifts(scan.getPeaks(), precursorMass, proteinSpectrum);
         double[] shifts = new double[shiftsList.size()];
         for (int i = 0; i < shifts.length; i++) {
             shifts[i] = shiftsList.get(i);
         }
         shifts = merge(shifts);
 
         double bestShift = 0;
         double bestScore = 0;
         double[] spectrum = getSpectrum(scan.getPeaks(), precursorMass);
         for (Double shift : shifts) {
             double nextScore = getScore(spectrum, proteinSpectrum, shift);
             if (nextScore > bestScore) {
                 bestScore = nextScore;
                 bestShift = shift;
             }
         }
         return bestShift;
     }
 
     public static double getBestShift(List<Peak> peaks, double[] proteinSpectrum) {
         List<Double> shiftsList = getShifts(peaks, proteinSpectrum);
         double[] shifts = new double[shiftsList.size()];
         for (int i = 0; i < shifts.length; i++) {
             shifts[i] = shiftsList.get(i);
         }
         shifts = merge(shifts);
 
         double bestShift = 0;
         double bestScore = 0;
         double[] spectrum = getSpectrum(peaks);
         for (Double shift : shifts) {
             double nextScore = getScore(spectrum, proteinSpectrum, shift);
             if (nextScore > bestScore) {
                 bestScore = nextScore;
                 bestShift = shift;
             }
         }
         return bestShift;
     }
 
     public static List<Double> getBestShifts(List<Peak> peaks, double precursorMass, double[] proteinSpectrum) {
         List<Peak> allIons = new ArrayList<Peak>();
         for (Peak peak : peaks) {
             allIons.add(peak);
             allIons.add(peak.getYPeak(precursorMass));
         }
         Collections.sort(allIons);
         List<Double> shiftsList = getShifts(allIons, proteinSpectrum);
         double[] shifts = new double[shiftsList.size()];
         for (int i = 0; i < shifts.length; i++) {
             shifts[i] = shiftsList.get(i);
         }
         List<double[]> ranges = getRanges(shifts);
 
         double[] spectrum = getSpectrum(allIons);
         List<Double> ans = new ArrayList<Double>();
         for (double[] range : ranges) {
             int parts = 10;
             double delta;
             while ((delta = (range[1] - range[0]) /parts) > 0.01) {
                 parts *= 2;
             }
             double bestScore = 0;
             double bestShift = Double.NaN;
             for (int i = 0; i <=parts; i++) {
                 double shift = range[0] + delta * i;
                 double nextScore = getScore(spectrum, proteinSpectrum, shift);
                 if (nextScore > bestScore) {
                     bestScore = nextScore;
                     bestShift = shift;
                 }
             }
             if (!Double.isNaN(bestShift)) {
                 if (bestScore > 10) {
                     ans.add(bestShift);
                 }
             } else {
                 System.out.println("No valid shifts in " + range[0] + " " + range[1]);
             }
         }
         Collections.sort(ans);
         return ans;
     }
 
 }
