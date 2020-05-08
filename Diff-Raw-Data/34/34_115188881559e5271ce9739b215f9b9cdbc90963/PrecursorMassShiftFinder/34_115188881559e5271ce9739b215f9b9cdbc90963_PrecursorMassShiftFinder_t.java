 package ru.spbau.bioinf.tagfinder;
 
 import ru.spbau.bioinf.tagfinder.util.StatisticsUtil;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class PrecursorMassShiftFinder {
 
     public static void main(String[] args) throws IOException {
         Configuration conf = new Configuration(args);
         Map<Integer, Scan> scans = conf.getScans();
         int none = 0;
         for (Scan scan : scans.values()) {
            double v = getPrecursorMassShift(conf, scan);
             if (v == 0) {
                 none++;
             }
         }
         System.out.println("none = " + none);
     }
 
    public static List<Double> getAllPossibleShifts(Configuration conf, Scan scan) {
        List<Peak> peaks = scan.getPeaks();
        double precursorMass = scan.getPrecursorMass();
        int n = peaks.size();
        List<Double> values = new ArrayList<Double>();
        for (int i = 0; i < n; i++) {
            Peak peak = peaks.get(i);
            for (int j = i+1; j < n; j++) {
                Peak next =  peaks.get(j);
                double sum = peak.getValue() + next.getValue();
                double delta = sum - precursorMass;
                double absDelta = Math.abs(delta);
                for (Acid acid : Acid.values()) {
                    double error = conf.getPpmCoef() * 3 * sum / 2;
                    if (acid.match(new double[]{absDelta - error,  absDelta + error})) {
                        if (delta > 0) {
                            values.add(delta - acid.getMass());
                        } else {
                            values.add(delta + acid.getMass());
                        }
                    }
                }
            }
        }
        return values;
    }

    public static double getPrecursorMassShift(Configuration conf, Scan scan) {
         List<Peak> peaks = scan.getPeaks();
         double precursorMass = scan.getPrecursorMass();
         int n = peaks.size();
         List<Double> values = new ArrayList<Double>();
         for (int i = 0; i < n; i++) {
             Peak peak = peaks.get(i);
             for (int j = i+1; j < n; j++) {
                 Peak next =  peaks.get(j);
                 double sum = peak.getValue() + next.getValue();
                 double delta = sum - precursorMass;
                 double error = conf.getPpmCoef() * 3 * sum / 2;
                 if (-error < delta && error > delta) {
                     values.add(delta);
                 }
             }
         }
         //int scanId = scan.getId();
         /*
         if (scanId == 591 ||scanId == 1562 || scanId == 583 || scanId == 2875 || scanId == 576 || scanId == 1102 ) {
             for (double value : values) {
                 System.out.println(value);
             }
         }
         */
 
         double delta = StatisticsUtil.getAverage(values);
         if (delta != 0) {
             //System.out.println(scanId + " " + delta + " " + " " + precursorMass + " " + values.size());
         }
         return delta;
     }
 }
