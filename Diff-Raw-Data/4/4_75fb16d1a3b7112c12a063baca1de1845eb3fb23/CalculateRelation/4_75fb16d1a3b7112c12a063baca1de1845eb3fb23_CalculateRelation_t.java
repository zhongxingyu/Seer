 package ru.spbau.bioinf.tagfinder;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import ru.spbau.bioinf.tagfinder.util.ReaderUtil;
 
 public class CalculateRelation {
 
     static final int ALL_TO_ALL = 10;
     static final int GOOD_TO_GOOD = 5;
 
     public static final int MAX_TAG = 100;
 
     public static void main(String[] args) throws Exception {
         //tableCorrectVsProper();
         //tableLongestCorrect();
         //tableMono();
         tableMonoCorrect();
     }
 
     public static void tableCorrectVsProper() throws Exception {
         double[][] res = new double[6][];
         generateRelationData(res, 0, "bar_exp_annotated_correct_none", "bar_exp_annotated_proper_none", GOOD_TO_GOOD);
         generateRelationData(res, 3, "bar_virt_annotated_correct_zero", "bar_virt_annotated_proper_zero", GOOD_TO_GOOD);
         TexTableGenerator.createSixRowsTable(res, "Average percentage of correct $\\ell$-tags w.r.t. proper $\\ell$-tags (basic spectrum graphs)", "correct $\\ell$-tags", 0, "correct-vs-proper-l-tags");
     }
 
     public static void tableLongestCorrect() throws Exception {
         double[][] res;
         res = new double[6][];
        correctD(res, 0, "bar_exp_annotated_correct_none");
        correctD(res, 3, "bar_virt_annotated_correct_zero");
         TexTableGenerator.createSixRowsTable(res, "Percentage of spectra, the longest correct tag in which has a given length", "spectra", -1, "longest-correct-l");
     }
 
     public static void tableMono() throws Exception {
         double[][] res = new double[3][];
         generateRelationData(res, 0, "bar_virt_annotated_correct_none", "bar_virt_annotated_correct_zero", ALL_TO_ALL);
         TexTableGenerator.createThreeRowsTable(res, "Average percentage of mono-$\\ell$-tags w.r.t. all $\\ell$-tags", "mono-$\\ell$-tags", "l-mono");
     }
 
     public static void  tableMonoCorrect() throws Exception {
         double[][] res = new double[3][];
         generateRelationData(res, 0, "bar_virt_annotated_correct_none", "bar_virt_annotated_correct_zero", GOOD_TO_GOOD);
         TexTableGenerator.createThreeRowsTable(res, "Average percentage of correct mono-$\\ell$-tags w.r.t. correct $\\ell$-tags", "mono-$\\ell$-tags", "l-mono-correct-rel");
     }
 
     private static void generateRelationData(double[][] res, int start, String firstTable, String secondTable, int mode) throws Exception {
         //System.out.println("% relation for " + firstTable + " / " + secondTable);
         for (int gap = 1; gap <= 3; gap++) {
             res[start + gap - 1] = compare("share_" + firstTable + "_" + gap + ".txt", "share_" + secondTable + "_" + gap + ".txt", mode);
         }
     }
 
     public static double[] compare(String fileFirst, String fileSecond, int mode) throws Exception {
         //System.out.println("%" +fileFirst + " " + fileSecond);
         BufferedReader inOne = ReaderUtil.createInputReader(new File("res", fileFirst));
         BufferedReader inTwo = ReaderUtil.createInputReader(new File("res", fileSecond));
         List<long[]> pairs = new ArrayList<long[]>();
         long[][][] stat = new long[10000][MAX_TAG][4];
         int n = 0;
         List<Double>[] goodToAll = new List[100];
         List<Double>[] goodToGood = new List[100];
         List<Double>[] allToAll = new List[100];
         for (int i = 0; i < MAX_TAG; i++) {
             goodToAll[i] = new ArrayList<Double>();
             goodToGood[i] = new ArrayList<Double>();
             allToAll[i] = new ArrayList<Double>();
         }
 
         do {
             String s1 = inOne.readLine();
             String s2 = inTwo.readLine();
             if (s1.indexOf(" ") == 0) {
                 break;
             }
             long[] d1 = getData(s1);
             long[] d2 = getData(s2);
             pairs.add(new long[]{d1[0], d1[1]});
             int pos = 2;
             int d = 1;
 
             while (pos < d1.length) {
                 stat[n][d][0] = d1[pos];
                 stat[n][d][1] = d1[pos + 1];
                 d++;
                 pos += 2;
             }
 
             d = 1;
             pos = 2;
             while (pos < d2.length) {
                 stat[n][d][2] = d2[pos];
                 stat[n][d][3] = d2[pos + 1];
                 d++;
                 pos += 2;
             }
 
             d = 1;
             do {
                 long[] q = stat[n][d];
                 double total = q[2] + q[3];
                 if (total == 0) {
                     break;
                 }
                 goodToAll[d].add((q[0]) / total);
                 allToAll[d].add((q[0] + q[1]) / total);
                 if (q[2] > 0) {
                     double v = q[2];
                     goodToGood[d].add(q[0] / v);
                 }
 
                 d++;
             } while (true);
             n++;
         } while (true);
         //System.out.println("Good to All: ");
         //getPercentage(goodToAll);
         //System.out.println("Good to Good: ");
         switch (mode) {
             case GOOD_TO_GOOD:
                 return getPercentage(goodToGood);
             case ALL_TO_ALL:
                 return getPercentage(allToAll);
         }
         return null;
         //System.out.println("All to All: ");
         //getPercentage(allToAll);
     }
 
     public static void correctD(double[][] res, int start, String file) throws Exception {
         //System.out.println("% longest tags distribution for  " + file);
         for (int gap = 1; gap <= 3; gap++) {
             BufferedReader in = ReaderUtil.createInputReader(new File("res", "share_" + file + "_" + gap + ".txt"));
 
             List<long[]> pairs = new ArrayList<long[]>();
             int n = 0;
             int[] dShare = new int[MAX_TAG];
             boolean[] exists = new boolean[50];
             exists[0] = true;
             do {
                 String s1 = in.readLine();
 
                 if (s1.indexOf(" ") == 0) {
                     break;
                 }
                 long[] d1 = getData(s1);
                 pairs.add(new long[]{d1[0], d1[1]});
                 int pos = 2;
                 int d = 1;
 
                 int maxD = 0;
                 while (pos < d1.length) {
                     exists[d] = true;
                     if (d1[pos] > 0) {
                         maxD = d;
                     }
                     d++;
                     pos += 2;
                 }
                 dShare[maxD]++;
                 if (maxD > n) {
                     n = maxD;
                 }
             } while (true);
             double total = pairs.size();
             //System.out.println("Distribution of longest tags: ");
             double[] ans = new double[n + 1];
             for (int i = 0; i <= n; i++) {
                 int v = dShare[i];
                 //if (v > 0) {
                 //System.out.println(i + " " + v + " " + df.format(100 * v / total));
                 //}
                 ans[i] = 100 * v / total;
             }
             res[start + gap - 1] = ans;
         }
     }
 
     private static double[] getPercentage(List<Double>[] stat) {
         List<Double> v = new ArrayList<Double>();
         for (int d = 1; d <= 1000; d++) {
             List<Double> values = stat[d];
             if (values.size() == 0) {
                 break;
             }
             double total = 0;
             for (double value : values) {
                 total += value;
             }
             if (total > 0) {
                 v.add(100 * total / values.size());
             } else {
                 break;
             }
         }
         double ans[] = new double[v.size()];
         for (int i = 0; i < ans.length; i++) {
             ans[i] = v.get(i);
         }
         return ans;
     }
 
     public static long[] getData(String s) {
         String[] d = s.split(" ");
         long[] ans = new long[d.length];
         for (int i = 0; i < ans.length; i++) {
             ans[i] = Long.parseLong(d[i]);
         }
         return ans;
     }
 }
